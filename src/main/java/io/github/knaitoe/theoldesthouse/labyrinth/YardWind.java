package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.345, the Windbreak (docs/YARD_STOCK_0_4_345.md), the yard's deep hazard, from the owner's draft: "Gusts move across the grass before
 * they hit. A loose sheet of barn roofing scrapes between gusts, then tears free and strikes the fence at head height. Stone walls, tractor
 * ruts, and the lee of the poplars offer cover. ... In a deeper version, one useful piece of shelter starts to come apart in view, giving
 * you time to choose the next one."
 *
 * <p>An open field crossed south to north under a wind from the west. Up its west side a line of shelter, poplars, a lean-to of barn roofing
 * and a stone wall, with short gaps between to time; along its east side tractor ruts a reader can crouch in; the straight way up the
 * middle is open to every gust. The grass shows each gust coming across from the west before it hits; in a gust a reader out of shelter
 * is pushed east toward the wire. The lean-to's loose sheet lifts, scrapes, and on the third gust tears free and crosses the field at head
 * height into the wire, hurting whoever it meets but never past three hearts; then the lean-to's back boards go, two gusts' worth, and that
 * shelter is gone. Back as it was once the field is empty and unwatched.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class YardWind {
    private YardWind(){}
    public static final String STATE="yard_wind_0345";
    public static final LabyrinthPlace PLACE=LabyrinthPlace.YARD_WINDBREAK;
    public static final int CYCLE=220,RIPPLE=150,GUST=190,RESET=200,FLIGHT=18;
    /** The loose sheet on the lean-to's roof, where it lands against the wire, and the shelter line's x. */
    public static final BlockPos LOOSE=new BlockPos(-5,3,-20),LANDED=new BlockPos(9,0,-20);
    public static final int SHELTER_X=-8,FENCE_X=11;
    /** The lean-to's back boards that work loose: the top row on the fourth gust, the middle row on the fifth. */
    public static final int[] WALL_Z={-17,-18,-19,-20,-21,-22,-23};
    public static final float SHEET_DAMAGE=4,FLOOR=6;

    public static boolean is(LabyrinthPlace p){return p==PLACE;}
    public static CompoundTag state(ServerLevel l){return LabyrinthData.get(l.getServer()).stateEntry(STATE,PLACE.id());}
    static void save(ServerLevel l,CompoundTag s){LabyrinthData.get(l.getServer()).setStateEntry(STATE,PLACE.id(),s);}
    /** 0 still, 1 the gust coming across the grass, 2 the gust. */
    public static int phase(long clock){int t=(int)Math.floorMod(clock,CYCLE);return t<RIPPLE?0:t<GUST?1:2;}
    static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState st){YardRooms.box(l,b,x0,y0,z0,x1,y1,z1,st);}
    static void put(ServerLevel l,BlockPos b,int x,int y,int z,BlockState st){YardRooms.set(l,b,x,y,z,st);}

    // ------------------------------------------------------------------ building
    public static void build(ServerLevel l,BlockPos b){
        var air=Blocks.AIR.defaultBlockState();
        box(l,b,-14,-3,-46,14,-2,0,Blocks.DIRT.defaultBlockState());box(l,b,-14,-1,-46,14,-1,0,Blocks.GRASS_BLOCK.defaultBlockState());
        box(l,b,-14,0,-46,14,9,0,YardRooms.leaves());
        // 0.4.403, the owner's playtest: "players can get stuck on other side of fence". The field ran a block past the wire, and a gust or the
        // sheet threw readers over it into a strip they could not climb back out of. The hedge stands right behind the wire now.
        box(l,b,-12,0,-44,FENCE_X,12,-2,air);box(l,b,-1,0,-2,1,4,-1,air);
        // Long grass over the field, thinner along the trodden ways.
        var rnd=new Random(b.asLong());
        for(int x=-12;x<=10;x++)for(int z=-44;z<=-3;z++)if(rnd.nextInt(x>=-7&&x<=-5||x==0?7:3)==0)put(l,b,x,0,z,Blocks.SHORT_GRASS.defaultBlockState());
        // The wire along the east side.
        for(int z=-44;z<=-3;z++){put(l,b,FENCE_X,0,z,Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH,z>-44).setValue(FenceBlock.SOUTH,z<-3));}
        for(int z=-43;z<=-3;z+=5)put(l,b,FENCE_X,1,z,Blocks.OAK_FENCE.defaultBlockState());
        // The poplars, close-set, their crowns one screen from the ground up.
        for(int z=-13;z<=-5;z+=2)box(l,b,SHELTER_X,0,z,SHELTER_X,8,z,Blocks.BIRCH_LOG.defaultBlockState());
        var crown=Blocks.BIRCH_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true);
        for(int z=-14;z<=-4;z++)for(int y=1;y<=9;y++)if(l.getBlockState(b.offset(SHELTER_X,y,z)).isAir())put(l,b,SHELTER_X,y,z,crown);
        for(int z=-13;z<=-5;z++)for(int y=4;y<=9;y++)put(l,b,SHELTER_X-1,y,z,crown);
        for(int z=-13;z<=-5;z+=2)for(int y=5;y<=8;y++)put(l,b,SHELTER_X+1,y,z,TruantFittingBlock.of(TruantFittingBlock.Kind.BOUGH,Direction.EAST,0));
        // The lean-to: back boards to the wind, two posts, the roof of barn sheets with one loose at its east edge.
        leanTo(l,b);
        // The stone wall, chest high.
        box(l,b,SHELTER_X,0,-37,SHELTER_X,1,-27,Blocks.MOSSY_COBBLESTONE.defaultBlockState());
        for(int z=-37;z<=-27;z+=3)put(l,b,SHELTER_X,1,z,Blocks.COBBLESTONE.defaultBlockState());
        // The ruts, a tractor's two wheels, wet in the bottom.
        for(int x:new int[]{4,6})for(int z=-30;z<=-12;z++){put(l,b,x,0,z,air);put(l,b,x,-1,z,air);put(l,b,x,-2,z,Blocks.MUD.defaultBlockState());}
        LabyrinthBuilder.entrance(l,b,Blocks.SPRUCE_PLANKS.defaultBlockState(),Blocks.GRASS_BLOCK.defaultBlockState(),Blocks.SPRUCE_SLAB.defaultBlockState());
        LabyrinthBuilder.doors(l,b,PLACE);
        var s=state(l);s.putLong("Clock",0);s.putInt("Gusts",0);s.putInt("Drawn",-1);save(l,s);
    }
    static void leanTo(ServerLevel l,BlockPos b){
        for(int z:WALL_Z)box(l,b,SHELTER_X,0,z,SHELTER_X,2,z,Blocks.SPRUCE_PLANKS.defaultBlockState());
        for(int z:new int[]{-17,-23})box(l,b,-5,0,z,-5,2,z,Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState());
        for(int x=SHELTER_X;x<=-5;x++)for(int z=-23;z<=-17;z++)put(l,b,x,3,z,YardBlock.of(YardBlock.Kind.ROOF_SHEET,Direction.EAST,0));
        put(l,b,LOOSE.getX(),LOOSE.getY(),LOOSE.getZ(),YardBlock.of(YardBlock.Kind.ROOF_SHEET,Direction.EAST,0));
    }

    // ------------------------------------------------------------------ the wind
    /** In the lee of something solid within four blocks upwind (the field's own hedge doesn't count: the wind comes over it), or crouched in a rut. */
    public static boolean sheltered(ServerLevel l,BlockPos b,Player p){
        var r=p.position().subtract(Vec3.atLowerCornerOf(b));
        if(p.isCrouching()&&r.y<-.4&&(Math.floor(r.x)==4||Math.floor(r.x)==6))return true;
        var eye=p.getEyePosition();var hit=l.clip(new ClipContext(eye,eye.add(-4,0,0),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
        if(hit.getType()!=HitResult.Type.BLOCK)return false;
        return !l.getBlockState(hit.getBlockPos()).is(Blocks.SPRUCE_LEAVES);
    }
    static boolean inField(Vec3 r){return r.x>-12.5&&r.x<FENCE_X+.5&&r.z<-2.5&&r.z>-44.5&&r.y>-3&&r.y<5;}

    static void tickWind(ServerLevel l,BlockPos b,CompoundTag s){
        long clock=s.getLong("Clock")+1;s.putLong("Clock",clock);int phase=phase(clock);int t=(int)Math.floorMod(clock,CYCLE);
        if(phase!=s.getInt("Drawn")){
            for(int z=-13;z<=-5;z+=2)for(int y=5;y<=8;y++){var at=b.offset(SHELTER_X+1,y,z);var st=l.getBlockState(at);if(TruantFittingBlock.is(st,TruantFittingBlock.Kind.BOUGH))l.setBlock(at,st.setValue(TruantFittingBlock.STAGE,phase),Block.UPDATE_CLIENTS);}
            s.putInt("Drawn",phase);
        }
        if(t==RIPPLE)l.playSound(null,b.getX()-10,b.getY()+3,b.getZ()-22,PuzzleRegistry.YARD_WIND.get(),SoundSource.AMBIENT,1.4F,.85F);
        // The gust coming across the grass, west to east, before it hits.
        if(phase==1&&t%2==0){double front=-12+(t-RIPPLE)*(24.0/(GUST-RIPPLE));var grass=new BlockParticleOption(ParticleTypes.BLOCK,Blocks.SHORT_GRASS.defaultBlockState());
            for(int z=-43;z<=-4;z+=2)l.sendParticles(grass,b.getX()+front,b.getY()+.4,b.getZ()+z+.5,2,.3,.2,.6,.05);}
        if(t==GUST){int gusts=s.getInt("Gusts")+1;s.putInt("Gusts",gusts);gustEvent(l,b,s,gusts);}
        if(phase==2){
            for(var p:l.players()){if(p.isSpectator()||!p.isAlive())continue;var r=p.position().subtract(Vec3.atLowerCornerOf(b));if(!inField(r)||sheltered(l,b,p))continue;
                var v=p.getDeltaMovement();p.setDeltaMovement(Math.min(.55,v.x+.07),v.y,v.z);p.hurtMarked=true;p.fallDistance=0;}
            if(t%6==0)l.sendParticles(ParticleTypes.CLOUD,b.getX()-4,b.getY()+2,b.getZ()-24,6,4,1,12,.25);
        }
        // Between gusts, once it has lifted, the loose sheet scrapes on the roof.
        if(phase==0&&t%30==0&&s.getInt("Gusts")>=2&&!s.getBoolean("Torn"))l.playSound(null,b.offset(LOOSE),SoundEvents.IRON_TRAPDOOR_OPEN,SoundSource.BLOCKS,.7F,.55F);
        if(s.contains("Flight"))fly(l,b,s);
    }
    /** What each gust does: the second lifts the loose sheet, the third tears it away, the fourth and fifth strip the lean-to's back boards. */
    static void gustEvent(ServerLevel l,BlockPos b,CompoundTag s,int gusts){
        if(gusts==2){var at=b.offset(LOOSE);if(YardBlock.is(l.getBlockState(at),YardBlock.Kind.ROOF_SHEET))l.setBlock(at,l.getBlockState(at).setValue(YardBlock.STAGE,1),Block.UPDATE_CLIENTS);l.playSound(null,at,SoundEvents.IRON_TRAPDOOR_OPEN,SoundSource.BLOCKS,1F,.5F);}
        if(gusts==3&&!s.getBoolean("Torn"))tear(l,b,s);
        if(gusts==4||gusts==5){int y=gusts==4?2:1;
            for(int z:WALL_Z){if(gusts==4&&Math.floorMod(z,2)!=0)continue;var at=b.offset(SHELTER_X,y,z);if(l.getBlockState(at).is(Blocks.SPRUCE_PLANKS)){l.setBlock(at,Blocks.AIR.defaultBlockState(),Block.UPDATE_ALL);
                l.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,Blocks.SPRUCE_PLANKS.defaultBlockState()),at.getX()+.5,at.getY()+.5,at.getZ()+.5,12,.3,.3,.3,.1);}}
            if(gusts==5)for(int z:WALL_Z){var at=b.offset(SHELTER_X,2,z);if(l.getBlockState(at).is(Blocks.SPRUCE_PLANKS))l.setBlock(at,Blocks.AIR.defaultBlockState(),Block.UPDATE_ALL);}
            l.playSound(null,b.offset(SHELTER_X,1,-20),SoundEvents.WOOD_BREAK,SoundSource.BLOCKS,1.2F,.7F);
            s.putBoolean("Stripped",gusts==5);
        }
    }
    /** The sheet torn from the roof, carried across the field at head height. */
    static void tear(ServerLevel l,BlockPos b,CompoundTag s){
        var at=b.offset(LOOSE);l.setBlock(at,Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);s.putBoolean("Torn",true);
        var d=EntityType.BLOCK_DISPLAY.create(l);if(d==null)return;var tag=new CompoundTag();d.saveWithoutId(tag);
        tag.put("block_state",NbtUtils.writeBlockState(YardBlock.of(YardBlock.Kind.ROOF_SHEET,Direction.EAST,1)));tag.putInt("teleport_duration",2);d.load(tag);
        d.moveTo(at.getX(),at.getY(),at.getZ(),0,0);if(!l.addFreshEntity(d))return;
        s.putUUID("Flight",d.getUUID());s.putInt("Flown",0);s.putIntArray("Hit",new int[0]);
        l.playSound(null,at,SoundEvents.IRON_DOOR_OPEN,SoundSource.BLOCKS,1.4F,.5F);
    }
    /** Where the sheet is {@code tick} ticks after it tears free: east along its row, down from the roof to head height, into the wire. */
    static Vec3 flightAt(int tick){double f=Math.min(1,tick/(double)FLIGHT);return new Vec3(LOOSE.getX()+f*(FENCE_X-1-LOOSE.getX()),LOOSE.getY()-Math.min(1,f/.3)*1.4,LOOSE.getZ());}
    static void fly(ServerLevel l,BlockPos b,CompoundTag s){
        var e=l.getEntity(s.getUUID("Flight"));int tick=s.getInt("Flown")+1;s.putInt("Flown",tick);
        var at=flightAt(tick);var world=Vec3.atLowerCornerOf(b).add(at);
        if(e instanceof Display.BlockDisplay d)d.teleportTo(world.x,world.y,world.z);
        // Whoever it meets at head height, once each: two hearts, never past three left, and thrown on with it.
        var sheet=new AABB(world.x,world.y-.2,world.z,world.x+1,world.y+1.1,world.z+1);var hit=new ArrayList<Integer>();for(int i:s.getIntArray("Hit"))hit.add(i);
        for(var p:l.players()){if(p.isSpectator()||!p.isAlive()||hit.contains(p.getId())||!p.getBoundingBox().intersects(sheet))continue;
            hit.add(p.getId());float dmg=Math.min(SHEET_DAMAGE,p.getHealth()-FLOOR);if(dmg>0)p.hurt(l.damageSources().generic(),dmg);
            p.setDeltaMovement(.9,.3,0);p.hurtMarked=true;l.playSound(null,p.blockPosition(),SoundEvents.PLAYER_HURT,SoundSource.PLAYERS,1F,1F);}
        s.putIntArray("Hit",hit.stream().mapToInt(Integer::intValue).toArray());
        if(tick<FLIGHT)return;
        if(e!=null)e.discard();s.remove("Flight");
        l.playSound(null,b.offset(FENCE_X,1,LOOSE.getZ()),SoundEvents.ANVIL_LAND,SoundSource.BLOCKS,.8F,.6F);
        l.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,Blocks.OAK_FENCE.defaultBlockState()),b.getX()+FENCE_X+.5,b.getY()+1.5,b.getZ()+LOOSE.getZ()+.5,16,.2,.3,.3,.1);
        var landed=b.offset(LANDED);if(l.getBlockState(landed).isAir()||l.getBlockState(landed).is(Blocks.SHORT_GRASS))l.setBlock(landed,YardBlock.of(YardBlock.Kind.ROOF_SHEET,Direction.EAST,2),Block.UPDATE_CLIENTS);
    }
    /** As it was: the lean-to whole, the sheet back on its roof, the gusts counted again from none. */
    public static void reset(ServerLevel l,BlockPos b,CompoundTag s){
        if(s.contains("Flight")){var e=l.getEntity(s.getUUID("Flight"));if(e!=null)e.discard();s.remove("Flight");}
        var landed=b.offset(LANDED);if(YardBlock.is(l.getBlockState(landed),YardBlock.Kind.ROOF_SHEET))l.setBlock(landed,Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);
        leanTo(l,b);s.putInt("Gusts",0);s.putBoolean("Torn",false);s.putBoolean("Stripped",false);s.putLong("Clock",0);s.putInt("Drawn",-1);save(l,s);
    }

    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();var origin=HouseSavedData.get(server).houseOrigin();var l=server.getLevel(HouseDimensions.OUTSIDE);if(origin==null||l==null)return;
        var b=LabyrinthPlaces.base(origin,PLACE);boolean present=false,near=false;
        for(var p:l.players()){if(!p.isSpectator()&&LabyrinthPlaces.placeAt(origin,p.blockPosition())==PLACE)present=true;if(p.position().distanceToSqr(Vec3.atLowerCornerOf(b))<64*64)near=true;}
        var s=state(l);if(!s.contains("Clock"))return;
        if(!present){if(!near&&(s.getInt("Gusts")>0||s.contains("Flight"))){int idle=s.getInt("Idle")+1;s.putInt("Idle",idle);if(idle>=RESET&&l.hasChunkAt(b.offset(SHELTER_X,0,-20))){reset(l,b,s);s.putInt("Idle",0);}save(l,s);}return;}
        s.putInt("Idle",0);
        if(!l.hasChunkAt(b.offset(SHELTER_X,0,-20))||!l.hasChunkAt(b.offset(FENCE_X,0,-20)))return;
        tickWind(l,b,s);save(l,s);
    }
}
