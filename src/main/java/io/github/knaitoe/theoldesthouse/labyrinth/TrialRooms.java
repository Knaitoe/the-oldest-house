package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * The new trial rooms of docs/HALLS_TRIALS_AND_TIME_DESIGN.md, each a physical trial (LabyrinthPacing.physicalTrial) and so a mark
 * (TrialMarks) for whoever comes through by its far door. Built here, run by HazardVariety's shared occupied clock.
 *
 * <p>0.4.385, the still light (docs/STILL_LIGHT_0_4_385.md): a long dark room floored in boards over a trough three deep. Its lamps
 * come up while any reader in it stands still and go out a breath after everyone moves. One way across is worn pale, and holds;
 * the dusty boards either side hold in the light and give way under a reader who stands on them in the dark. The trough runs back
 * under the floor to two steps up beside the entry. Nothing that burns will stay alight in the room.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class TrialRooms {
    private TrialRooms(){}
    private static final int FLAGS=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    public static boolean is(LabyrinthPlace place){return place==LabyrinthPlace.STILL_LIGHT;}
    static void build(ServerLevel level,BlockPos base,LabyrinthPlace place){if(place==LabyrinthPlace.STILL_LIGHT)buildStillLight(level,base);}
    static void tick(ServerLevel level,BlockPos base,LabyrinthPlace place,CompoundTag room,long clock,List<ServerPlayer> readers){
        if(place==LabyrinthPlace.STILL_LIGHT)stillLight(level,base,room,clock,readers);
    }

    // ---- The still light ----

    /** The board field runs from the entry landing (z -1, -2) to the far landing (z -41 to -43), nine boards wide. */
    public static final int FIELD_NEAR=-3,FIELD_FAR=-40,HALF=4,FAR_WALL=-44;
    /** Ticks a reader must hold still before the lamps come up, and ticks they stay up after everyone moves. */
    public static final int STILL=10,FADE=6;
    /** Ticks on a dusty board in the dark before it goes. */
    public static final int GIVE=2;
    /** The worn way across, as stretches from the entry: the column it runs down and how many rows; each turns on its last row. */
    private static final int[][] STRETCHES={{0,5},{2,5},{-1,5},{-3,5},{1,5},{3,4},{0,5},{-2,4}};
    /** Lamps hang from the ceiling down the middle of the room. */
    public static final List<Integer> LAMP_Z=List.of(-2,-8,-14,-20,-26,-32,-38);
    private static final Set<Long> WAY=way();
    private static long key(int x,int z){return ((long)x<<32)|(z&0xffffffffL);}
    private static Set<Long> way(){
        var out=new HashSet<Long>();int z=FIELD_NEAR;
        for(int i=0;i<STRETCHES.length;i++){
            int x=STRETCHES[i][0],rows=STRETCHES[i][1],last=z-rows+1;
            for(int k=0;k<rows;k++)out.add(key(x,z-k));
            if(i+1<STRETCHES.length){int next=STRETCHES[i+1][0];for(int s=Math.min(x,next);s<=Math.max(x,next);s++)out.add(key(s,last));}
            z-=rows;
        }
        return Collections.unmodifiableSet(out);
    }
    /** Whether the board at (x, z) of the field lies on the worn way. */
    public static boolean onWay(int x,int z){return WAY.contains(key(x,z));}
    /** The worn way, in order from the entry to the far landing, as floor cells (y -1) of the room. */
    public static List<BlockPos> wayInOrder(){
        var out=new ArrayList<BlockPos>();int z=FIELD_NEAR;
        for(int i=0;i<STRETCHES.length;i++){
            int x=STRETCHES[i][0],rows=STRETCHES[i][1],last=z-rows+1;
            for(int k=0;k<rows;k++)out.add(new BlockPos(x,-1,z-k));
            if(i+1<STRETCHES.length){int next=STRETCHES[i+1][0],step=next>x?1:-1;for(int s=x+step;s!=next+step;s+=step)out.add(new BlockPos(s,-1,last));}
            z-=rows;
        }
        return out;
    }
    /** The two holes in the floor beside the entry, over the steps up out of the trough. */
    public static boolean stairWell(int x,int z){return x>=3&&(z==-3||z==-4);}

    private static BlockState mat(String name){return TrialRegistry.material(name).defaultBlockState();}
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,FLAGS);}
    private static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){BuildBlocks.box(l,b.offset(x0,y0,z0),b.offset(x1,y1,z1),s,FLAGS);}

    private static void buildStillLight(ServerLevel l,BlockPos b){
        var wall=mat("still_wall");var ceiling=mat("still_ceiling");var worn=mat("still_path");var under=mat("still_under");var air=Blocks.AIR.defaultBlockState();
        box(l,b,-5,-4,FAR_WALL,5,4,0,wall);
        box(l,b,-HALF,4,FAR_WALL+1,HALF,4,-1,ceiling);
        box(l,b,-HALF,0,FAR_WALL+1,HALF,3,-1,air);
        // The trough: brick underfoot, two clear blocks under the boards, closed at both ends under the landings.
        box(l,b,-HALF,-4,FIELD_FAR,HALF,-4,FIELD_NEAR,under);
        box(l,b,-HALF,-3,FIELD_FAR,HALF,-2,FIELD_NEAR,air);
        box(l,b,-HALF,-1,-2,HALF,-1,-1,worn);
        box(l,b,-HALF,-1,FAR_WALL+1,HALF,-1,FIELD_FAR-1,worn);
        for(int z=FIELD_FAR;z<=FIELD_NEAR;z++)for(int x=-HALF;x<=HALF;x++)
            set(l,b,x,-1,z,stairWell(x,z)?air:onWay(x,z)?worn:TrialBlock.of(TrialBlock.Kind.STILL_BOARD));
        // The way back up, beside the entry: one step of the trough's brick, then another, then the landing.
        box(l,b,3,-3,-4,HALF,-3,-4,under);
        box(l,b,3,-3,-3,HALF,-2,-3,under);
        for(int z:LAMP_Z)set(l,b,0,3,z,TrialBlock.of(TrialBlock.Kind.STILL_LAMP));
        LabyrinthBuilder.entrance(l,b,wall,worn,ceiling);
    }

    /** Each reader's last place and how long they have held still and stood on a dusty board in the dark. */
    private static final Map<ServerPlayer,double[]> MOTION=new WeakHashMap<>();
    public static boolean lit(CompoundTag room){return room.getBoolean("StillLit");}

    private static void stillLight(ServerLevel l,BlockPos b,CompoundTag room,long clock,List<ServerPlayer> readers){
        boolean still=false;
        for(var p:readers){
            var m=MOTION.computeIfAbsent(p,k->new double[]{p.getX(),p.getZ(),0,0});
            double dx=p.getX()-m[0],dz=p.getZ()-m[1];m[0]=p.getX();m[1]=p.getZ();
            m[2]=dx*dx+dz*dz<1.0E-4&&p.onGround()?m[2]+1:0;
            if(m[2]>=STILL)still=true;
        }
        boolean lit=lit(room);
        if(still){room.putInt("StillFade",FADE);if(!lit)lamps(l,b,room,true);}
        else if(lit){int fade=room.getInt("StillFade");if(fade>0)room.putInt("StillFade",fade-1);else lamps(l,b,room,false);}
        else if(clock%100==0)lamps(l,b,room,false);
        lit=lit(room);
        for(var p:readers){
            var m=MOTION.get(p);var on=p.getOnPos();
            if(!lit&&p.onGround()&&TrialBlock.is(l.getBlockState(on),TrialBlock.Kind.STILL_BOARD,0)){
                if(++m[3]>=GIVE){m[3]=0;give(l,on);}
            }else m[3]=0;
        }
        if(clock%60==0)settle(l,b);
    }
    private static void lamps(ServerLevel l,BlockPos b,CompoundTag room,boolean on){
        boolean was=lit(room);room.putBoolean("StillLit",on);
        for(int z:LAMP_Z){var at=b.offset(0,3,z);var s=l.getBlockState(at);
            if(TrialBlock.is(s,TrialBlock.Kind.STILL_LAMP)&&s.getValue(TrialBlock.STAGE)!=(on?1:0))l.setBlock(at,s.setValue(TrialBlock.STAGE,on?1:0),FLAGS);}
        if(was!=on)l.playSound(null,b.offset(0,3,-20),on?SoundEvents.COPPER_BULB_TURN_ON:SoundEvents.COPPER_BULB_TURN_OFF,SoundSource.BLOCKS,.7F,on?.6F:.5F);
    }
    private static void give(ServerLevel l,BlockPos at){
        var s=l.getBlockState(at);l.setBlock(at,s.setValue(TrialBlock.STAGE,1),FLAGS);
        l.playSound(null,at,SoundEvents.WOOD_BREAK,SoundSource.BLOCKS,.9F,.55F);
        l.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,mat("still_path")),at.getX()+.5,at.getY()+.6,at.getZ()+.5,10,.35,.1,.35,.05);
    }
    /** Boards that gave are back, unseen, once nothing is where they were. */
    private static void settle(ServerLevel l,BlockPos b){
        for(int z=FIELD_FAR;z<=FIELD_NEAR;z++)for(int x=-HALF;x<=HALF;x++){
            var at=b.offset(x,-1,z);var s=l.getBlockState(at);
            if(!TrialBlock.is(s,TrialBlock.Kind.STILL_BOARD,1))continue;
            if(!l.getEntitiesOfClass(Entity.class,new AABB(at)).isEmpty())continue;
            l.setBlock(at,s.setValue(TrialBlock.STAGE,0),FLAGS);
        }
    }
    /** Nothing that burns will stay alight in the still light: a torch, a lantern or a lamp set down there goes out in the hand. */
    @SubscribeEvent public static void place(BlockEvent.EntityPlaceEvent event){
        if(!(event.getEntity() instanceof ServerPlayer p)||!(event.getLevel() instanceof ServerLevel l)||!l.dimension().equals(HouseDimensions.INTERIOR))return;
        var origin=HouseSavedData.get(p.server).houseOrigin();
        if(origin==null||LabyrinthPlaces.placeAt(origin,event.getPos())!=LabyrinthPlace.STILL_LIGHT)return;
        if(event.getPlacedBlock().getLightEmission(l,event.getPos())<=0)return;
        event.setCanceled(true);
        p.displayClientMessage(Component.literal("It goes out as it leaves your hand.").withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);
    }
}
