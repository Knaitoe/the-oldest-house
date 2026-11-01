package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.network.HouseFadePayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.208, the dark halls (docs/DARK_HALLS_0_4_208.md): the owner's third series of ordinary halls, on the road down to the great
 * staircase. They begin at depth sixteen, are the only ordinary halls from twenty-one, and the staircase is offered from
 * twenty-two. The house the book describes: smooth ash-grey walls, no fixtures, no light, cold; long, with forks, short real dead
 * ends and ceilings that rise as you go; always one true way on and the way back always open.
 *
 * <p>Five halls: the long hall, which just keeps going; the forking hall, three branches and one through; the ash maze, a small
 * labyrinth with two ways out; a spiral that goes down round itself and comes out under where it started; and the edge of a great
 * dark room, a walkway along a drop nobody can see the bottom of, the last of the series before the staircase.
 *
 * <p>What the House does there, sparingly: now and then it takes a torch a reader set, out of their sight, with a low rumble of
 * its own (never the Minotaur's); the Minotaur is heard more often than in the stone halls, not much more; and at most one echo
 * a reader every several halls: footsteps behind them in the long hall and the spiral, a door slamming late behind them, and at the
 * edge, a thing thrown that lands far too late or never.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class DarkHalls {
    private DarkHalls(){}
    /** Where the dark halls begin, and where they are the only ordinary halls. */
    public static final int DARK_DEPTH=LabyrinthPacing.ABYSS_DEPTH,ONLY_DEPTH=21;
    /** The Minotaur in the dark halls: on average this many ticks apart (half to one and a half times it), never closer than the minimum. */
    public static final long ROAR_MEAN=3000,ROAR_MIN=1500;
    /** Dark-hall arrivals between echoes, at the least. */
    public static final int ECHO_SPACING=4;
    /** Test hooks: every eligible hall takes a torch; every eligible hall has an echo. */
    public static boolean TEST_TAKE=false,TEST_ECHO=false;
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;

    /** 0.4.209 onward: the horrors dealt only in the dark band (docs/DARK_HALL_HORRORS_PROPOSAL.md), built whole and left alone by the polish passes. */
    public static boolean horror(LabyrinthPlace p){return p==LabyrinthPlace.CLOSING_HALL||p==LabyrinthPlace.STOREROOM||p==LabyrinthPlace.STILLNESS;}
    public static boolean isDark(@Nullable LabyrinthPlace p){return p==LabyrinthPlace.DARK_LONG||p==LabyrinthPlace.DARK_FORK||p==LabyrinthPlace.DARK_MAZE||p==LabyrinthPlace.DARK_SPIRAL||p==LabyrinthPlace.DARK_EDGE;}
    public static final List<LabyrinthPlace> HALLS=List.of(LabyrinthPlace.DARK_LONG,LabyrinthPlace.DARK_FORK,LabyrinthPlace.DARK_MAZE,LabyrinthPlace.DARK_SPIRAL,LabyrinthPlace.DARK_EDGE);
    /** How often each is dealt as an ordinary way on: none above sixteen, then more as the band goes on; the edge only at the end. */
    public static int weight(LabyrinthPlace p,int depth){
        if(depth<DARK_DEPTH)return 0;boolean only=depth>=ONLY_DEPTH;int ramp=Math.min(10,(depth-DARK_DEPTH+1)*2);
        return switch(p){
            case DARK_LONG,DARK_FORK->only?22:4+ramp;
            case DARK_MAZE,DARK_SPIRAL->only?16:3+ramp;
            case DARK_EDGE->depth<ONLY_DEPTH-1?0:only?18:8;
            default->0;
        };
    }

    // ------------------------------------------------------------------------------------------------ the halls
    static BlockState ash(){return DarkHallRegistry.material("ash").defaultBlockState();}
    static BlockState floor(){return DarkHallRegistry.material("floor").defaultBlockState();}
    static BlockState ceiling(){return DarkHallRegistry.material("ceiling").defaultBlockState();}
    private static final BlockState AIR=Blocks.AIR.defaultBlockState();
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void fill(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){NovelRooms.box(l,b,x0,y0,z0,x1,y1,z1,s);}
    /** A column of passage: floor, air up to a ceiling height, the ceiling. */
    private static void column(ServerLevel l,BlockPos b,int x,int z,int floorY,int height){
        set(l,b,x,floorY-1,z,floor());for(int y=floorY;y<floorY+height;y++)set(l,b,x,y,z,AIR);set(l,b,x,floorY+height,z,ceiling());
    }
    public static void build(ServerLevel l,BlockPos b,LabyrinthPlace place){
        var box=place.room();fill(l,b,box.minX(),box.minY(),box.minZ(),box.maxX(),box.maxY(),Math.min(box.maxZ(),0),ash());
        switch(place){
            case DARK_LONG->longHall(l,b);
            case DARK_FORK->fork(l,b);
            case DARK_MAZE->maze(l,b);
            case DARK_SPIRAL->spiral(l,b);
            case DARK_EDGE->edge(l,b);
            default->throw new IllegalArgumentException(place.id());
        }
        LabyrinthBuilder.entrance(l,b,ash(),floor(),ceiling());
        LabyrinthBuilder.doors(l,b,place);
    }
    /** The long hall: three wide, a hundred and four long, the ceiling rising from three to eight as it goes. */
    private static void longHall(ServerLevel l,BlockPos b){
        for(int z=-1;z>=-104;z--){int h=3+(int)(5*(-z-1)/103.0);for(int x=-1;x<=1;x++)column(l,b,x,z,0,h);}
    }
    /** The forking hall: a way in, a cross passage, three branches north; west and middle end in rock, east goes on. */
    private static void fork(ServerLevel l,BlockPos b){
        for(int z=-1;z>=-20;z--)for(int x=-1;x<=1;x++)column(l,b,x,z,0,4);
        for(int z=-21;z>=-23;z--)for(int x=-17;x<=17;x++)column(l,b,x,z,0,4);
        int[][] branches={{-16,-44},{0,-50},{16,-58}};
        for(var br:branches)for(int z=-24;z>=br[1];z--){int h=4+(int)(3*(-z-24)/34.0);for(int x=br[0]-1;x<=br[0]+1;x++)column(l,b,x,z,0,h);}
    }
    /** The ash maze: eleven by eleven cells a block wide, carved by a fixed walk so every reader meets the same one; two ways out at its far side. */
    public static final int MAZE_N=11;
    static int cellX(int i){return -10+2*i;}
    static int cellZ(int j){return -3-2*j;}
    private static void maze(ServerLevel l,BlockPos b){
        var rnd=new Random(208);boolean[][] seen=new boolean[MAZE_N][MAZE_N];var stack=new ArrayDeque<int[]>();
        int[][] steps={{1,0},{-1,0},{0,1},{0,-1}};
        stack.push(new int[]{5,0});seen[5][0]=true;column(l,b,cellX(5),cellZ(0),0,3);
        while(!stack.isEmpty()){
            var c=stack.peek();var open=new ArrayList<int[]>();
            for(var s:steps){int i=c[0]+s[0],j=c[1]+s[1];if(i>=0&&j>=0&&i<MAZE_N&&j<MAZE_N&&!seen[i][j])open.add(new int[]{i,j});}
            if(open.isEmpty()){stack.pop();continue;}
            var n=open.get(rnd.nextInt(open.size()));seen[n[0]][n[1]]=true;
            column(l,b,(cellX(c[0])+cellX(n[0]))/2,(cellZ(c[1])+cellZ(n[1]))/2,0,3);column(l,b,cellX(n[0]),cellZ(n[1]),0,3);stack.push(n);
        }
        // In from the door to the first cell; out from the two far corners to their doors.
        column(l,b,0,-1,0,3);column(l,b,0,-2,0,3);
        for(int x:new int[]{-10,10})for(int z=-24;z>=-25;z--)column(l,b,x,z,0,3);
    }
    /** The spiral: round a solid core, one block wide, a block lower every six steps, under itself twice, out to the north ten down. */
    public static final BlockPos SPIRAL_CENTRE=new BlockPos(0,0,-9);
    public static List<BlockPos> ring(){
        var out=new ArrayList<BlockPos>();int cz=SPIRAL_CENTRE.getZ();
        out.add(new BlockPos(0,0,cz+3));for(int x=-1;x>=-3;x--)out.add(new BlockPos(x,0,cz+3));
        for(int z=cz+2;z>=cz-3;z--)out.add(new BlockPos(-3,0,z));
        for(int x=-2;x<=3;x++)out.add(new BlockPos(x,0,cz-3));
        for(int z=cz-2;z<=cz+3;z++)out.add(new BlockPos(3,0,z));
        for(int x=2;x>=1;x--)out.add(new BlockPos(x,0,cz+3));
        return out;
    }
    public static final int SPIRAL_STEPS=60;
    public static int spiralHeight(int step){return -step/6;}
    private static void spiral(ServerLevel l,BlockPos b){
        for(int z=-1;z>=-5;z--)column(l,b,0,z,0,3);
        var ring=ring();BlockPos prev=null;
        for(int s=0;s<=SPIRAL_STEPS;s++){var c=ring.get(s%ring.size());int h=spiralHeight(s);
            column(l,b,c.getX(),c.getZ(),h,3);
            if(s>0&&s%6==0&&prev!=null){var back=Direction.getNearest(prev.getX()-c.getX(),0,prev.getZ()-c.getZ());
                set(l,b,c.getX(),h,c.getZ(),Blocks.POLISHED_DEEPSLATE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,back).setValue(StairBlock.HALF,Half.BOTTOM));}
            prev=c;}
        int out=spiralHeight(SPIRAL_STEPS);for(int z=SPIRAL_CENTRE.getZ()-4;z>=-15;z--)column(l,b,0,z,out,3);
    }
    /** The edge: a walkway three wide along the west wall of a room it cannot see, a drop of thirteen beside it, no rail. */
    public static final int EDGE_DROP=-12,EDGE_CAUGHT=-4;
    private static void edge(ServerLevel l,BlockPos b){
        for(int z=-1;z>=-64;z--)for(int x=-1;x<=1;x++)column(l,b,x,z,0,z>-4?4:9);
        for(int z=-4;z>=-63;z--)for(int x=2;x<=19;x++){set(l,b,x,EDGE_DROP-1,z,floor());for(int y=EDGE_DROP;y<=8;y++)set(l,b,x,y,z,AIR);set(l,b,x,9,z,ceiling());}
    }
    public static boolean overDrop(Vec3 r){return r.x>=1.7&&r.x<20&&r.z<=-3.5&&r.z>=-63.5;}

    // ------------------------------------------------------------------------------------------------ the House in them
    private record Light(BlockPos at,LabyrinthPlace place){}
    /** The lights each reader set in the dark halls this visit, oldest first. */
    private static final Map<UUID,List<Light>> LIGHTS=new HashMap<>();
    private static final Map<UUID,LabyrinthPlace> HERE=new HashMap<>();
    private static final Map<UUID,Integer> SINCE_ECHO=new HashMap<>(),SINCE_TAKE=new HashMap<>();
    /** This visit's plan for each reader: whether the House will take a torch, and the echo due (0 none, 1 footsteps, 2 the slam) and when. */
    private static final Map<UUID,int[]> PLAN=new HashMap<>();
    private static final Map<ItemEntity,UUID> FALLING=new HashMap<>();
    private static final Map<UUID,long[]> LANDINGS=new HashMap<>();

    @Nullable static BlockPos origin(MinecraftServer s){return HouseSavedData.get(s).houseOrigin();}
    @Nullable public static LabyrinthPlace placeOf(ServerPlayer p){var o=origin(p.server);if(o==null||!p.level().dimension().equals(HouseDimensions.INTERIOR))return null;var place=LabyrinthPlaces.placeAt(o,p.blockPosition());return isDark(place)?place:null;}
    static void heard(ServerPlayer p,Vec3 at,net.minecraft.core.Holder<SoundEvent> s,float volume,float pitch){p.connection.send(new ClientboundSoundPacket(s,SoundSource.AMBIENT,at.x,at.y,at.z,volume,pitch,p.getRandom().nextLong()));}

    /** A light set in a dark hall is remembered, so the House can take it later. */
    @SubscribeEvent public static void placed(BlockEvent.EntityPlaceEvent e){
        if(e.isCanceled()||!(e.getEntity() instanceof ServerPlayer p)||!LabyrinthLighting.isPortableLight(e.getPlacedBlock()))return;var place=placeOf(p);if(place==null)return;
        remember(p,e.getPos(),place);
    }
    public static void remember(ServerPlayer p,BlockPos at,LabyrinthPlace place){LIGHTS.computeIfAbsent(p.getUUID(),k->new ArrayList<>()).add(new Light(at.immutable(),place));}
    public static int remembered(ServerPlayer p){return LIGHTS.getOrDefault(p.getUUID(),List.of()).size();}

    /** A reader walks into a dark hall: the House decides, now, whether it will take a torch here and whether there is an echo. */
    public static void arrived(ServerPlayer p,LabyrinthPlace place){
        var id=p.getUUID();var rnd=p.getRandom();int sinceEcho=SINCE_ECHO.merge(id,1,Integer::sum),sinceTake=SINCE_TAKE.merge(id,1,Integer::sum);
        boolean take=TEST_TAKE||sinceTake>=2&&rnd.nextInt(3)==0;
        int echo=0;if(TEST_ECHO||sinceEcho>=ECHO_SPACING&&rnd.nextInt(2)==0)echo=place==LabyrinthPlace.DARK_LONG||place==LabyrinthPlace.DARK_SPIRAL?(rnd.nextBoolean()?1:2):place==LabyrinthPlace.DARK_EDGE?0:2;
        if(echo!=0)SINCE_ECHO.put(id,0);
        PLAN.put(id,new int[]{take?1:0,echo,echo==2?80+rnd.nextInt(40):40+rnd.nextInt(80),0});
    }
    /** Takes one of the reader's lights in this hall that is far from them, out of their sight and every other reader's: no drop, a low rumble. */
    public static boolean takeTorch(ServerPlayer p,ServerLevel l){
        var plan=PLAN.get(p.getUUID());if(plan==null||plan[0]!=1)return false;var lights=LIGHTS.get(p.getUUID());if(lights==null||lights.size()<2)return false;
        for(var it=lights.iterator();it.hasNext();){var light=it.next();var state=l.getBlockState(light.at());if(!LabyrinthLighting.isPortableLight(state)){it.remove();continue;}
            var centre=Vec3.atCenterOf(light.at());if(centre.distanceTo(p.getEyePosition())<10)continue;boolean seen=false;
            for(var q:l.players())if(q.getEyePosition().distanceTo(centre)<128&&q.getViewVector(1F).dot(centre.subtract(q.getEyePosition()).normalize())>.2)seen=true;
            if(seen)continue;
            l.setBlock(light.at(),Blocks.AIR.defaultBlockState(),Block.UPDATE_ALL);it.remove();plan[0]=2;SINCE_TAKE.put(p.getUUID(),0);
            for(var q:l.players())if(q.distanceTo(p)<24)heard(q,centre,DarkHallRegistry.RUMBLE,1F,.7F+q.getRandom().nextFloat()*.15F);
            return true;}
        return false;
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var s=e.getServer();long tick=s.getTickCount();var l=s.getLevel(HouseDimensions.INTERIOR);if(l==null)return;
        for(var p:l.players()){
            if(p.isSpectator())continue;var place=placeOf(p);var id=p.getUUID();var was=HERE.get(id);
            if(place!=was){if(place==null){HERE.remove(id);PLAN.remove(id);LIGHTS.remove(id);continue;}HERE.put(id,place);arrived(p,place);
                // 0.4.209: the first door of a visit to the dark halls, for a reader who left a child in the closing hall.
                if(was==null){var at=LabyrinthPlaces.base(origin(s),place);if(at!=null)ClosingHall.firstDarkDoor(p,at);}}
            if(place==null)continue;var base=LabyrinthPlaces.base(origin(s),place);if(base==null)continue;var r=p.position().subtract(base.getX(),base.getY(),base.getZ());
            if(tick%20==0)takeTorch(p,l);
            echo(p,l,base,place,r);
            if(place==LabyrinthPlace.DARK_EDGE)edge(p,l,base,r);
            // Cold: breath in the air now and then.
            if(tick%50==0)l.sendParticles(p,ParticleTypes.WHITE_SMOKE,true,p.getX()+p.getViewVector(1F).x*.4,p.getEyeY()-.1,p.getZ()+p.getViewVector(1F).z*.4,1,.02,.02,.02,.005);
        }
        // A thing thrown into the edge's dark: gone, and heard landing far too late, or never.
        for(var it=FALLING.entrySet().iterator();it.hasNext();){var en=it.next();var item=en.getKey();if(item.isRemoved()){it.remove();continue;}
            var place=LabyrinthPlaces.placeAt(origin(s),item.blockPosition());var base=place==LabyrinthPlace.DARK_EDGE?LabyrinthPlaces.base(origin(s),place):null;
            if(base==null){it.remove();continue;}if(item.getY()-base.getY()<EDGE_CAUGHT){item.discard();it.remove();var thrower=s.getPlayerList().getPlayer(en.getValue());
                if(thrower!=null&&thrower.getRandom().nextInt(3)!=0)LANDINGS.put(thrower.getUUID(),new long[]{tick+80+thrower.getRandom().nextInt(60),(long)item.getX(),(long)item.getZ(),base.getY()+EDGE_DROP});}}
        for(var it=LANDINGS.entrySet().iterator();it.hasNext();){var en=it.next();if(tick<en.getValue()[0])continue;it.remove();var p=s.getPlayerList().getPlayer(en.getKey());
            if(p!=null)heard(p,new Vec3(en.getValue()[1],en.getValue()[3],en.getValue()[2]),DarkHallRegistry.LAND,.35F,.8F);}
    }
    /** The echo this visit holds, if any: footsteps behind the reader while they walk, stopping a beat after they do; or a door slamming behind them, late. */
    static void echo(ServerPlayer p,ServerLevel l,BlockPos base,LabyrinthPlace place,Vec3 r){
        var plan=PLAN.get(p.getUUID());if(plan==null||plan[1]==0)return;plan[3]++;if(plan[3]<plan[2])return;
        if(plan[1]==2){heard(p,Vec3.atCenterOf(base.offset(0,1,1)),DarkHallRegistry.SLAM,.9F,.9F);plan[1]=0;return;}
        // Footsteps: for six seconds, every eight ticks the reader is moving, a step behind them.
        if(plan[3]>plan[2]+120){plan[1]=0;return;}
        if(plan[3]%8==0&&p.getDeltaMovement().horizontalDistanceSqr()>1e-4){var back=p.getViewVector(1F).multiply(-1,0,-1).normalize().scale(5);
            heard(p,p.position().add(back),DarkHallRegistry.STEPS,.6F,.95F+p.getRandom().nextFloat()*.1F);}
    }
    /** The edge: a reader who goes over is put back on the walkway at its start, unhurt; a thing that goes over is watched. */
    static void edge(ServerPlayer p,ServerLevel l,BlockPos base,Vec3 r){
        if(r.y<EDGE_CAUGHT){HousePackets.send(p,new HouseFadePayload(2,20,30));p.teleportTo(l,base.getX()+.5,base.getY(),base.getZ()-2.5,180,0);p.setDeltaMovement(Vec3.ZERO);p.fallDistance=0;
            p.displayClientMessage(Component.literal("The dark takes your feet out from under you. You never land. You are at the start of the walkway, on your knees.").withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);return;}
        for(var item:l.getEntitiesOfClass(ItemEntity.class,new AABB(base.getX()+1.5,base.getY()-1,base.getZ()-64,base.getX()+20,base.getY()+3,base.getZ()-3)))
            if(!FALLING.containsKey(item)&&item.getOwner()!=null&&item.getOwner().getUUID().equals(p.getUUID()))FALLING.put(item,p.getUUID());
    }
    /** The Minotaur, heard in the dark halls more often than in the stone ones, not much more. */
    public static long roarGap(RandomSource random){return Math.max(ROAR_MIN,(long)(ROAR_MEAN*(.5+random.nextDouble())));}
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){var id=e.getEntity().getUUID();LIGHTS.remove(id);HERE.remove(id);PLAN.remove(id);LANDINGS.remove(id);}
    public static void clearAll(){LIGHTS.clear();HERE.clear();SINCE_ECHO.clear();SINCE_TAKE.clear();PLAN.clear();FALLING.clear();LANDINGS.clear();TEST_TAKE=false;TEST_ECHO=false;}
    /** Test hook: the plan this visit holds for a reader (take, echo, due, elapsed). */
    public static int[] plan(ServerPlayer p){return PLAN.get(p.getUUID());}
}
