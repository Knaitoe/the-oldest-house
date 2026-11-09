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
 *
 * <p>0.4.386, the soft floor (docs/SOFT_FLOOR_0_4_386.md): squares of sodden carpet over a passage three down. A square sinks while
 * anyone stands on it and at two seconds gives way; two on one square sink it twice as fast. Low beams to crouch under and staggered
 * half-walls slow a reader down. The passage leads back to two steps up beside the entry. Keep moving.
 *
 * <p>0.4.387, the rising stair (docs/RISING_STAIR_0_4_387.md): a shaft climbed by treads let into its walls, a gap here and there.
 * While anyone is on the stair the water rises a block every second and a half. It has an undertow, so nobody floats up on it, and
 * nobody jumps a gap with it over their feet. Two pockets in the walls hold air, and while the only readers in the shaft are in
 * them, or past the water on the landing at the head, the water goes down. Out of air, a reader is back on the landing at the foot.
 *
 * <p>0.4.388, the hall of many lengths (docs/MANY_LENGTHS_0_4_388.md): five identical lengths of hall, each nine blocks on and a
 * bend aside, like the long hallway's. A reader who hurries through the second, third or fourth length (sprints or jumps in it) is
 * moved back to the start of it as they leave it, seamlessly, out of everyone else's sight; a reader who walks crosses in fixed
 * time. Readers who go in together come out apart, and the first through hear the others' steps in the walls.
 *
 * <p>0.4.389, the door that follows (docs/FOLLOWING_DOOR_0_4_389.md): a papered room with nine doors in three walls, all painted
 * shut but one, and that one is always where nobody in the room is looking. Turn to it and it shuts, and another opens behind.
 * A passage behind the walls leads from every door to the far one. Back into it, or watch it in one of the two tall mirrors.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class TrialRooms {
    private TrialRooms(){}
    private static final int FLAGS=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    public static boolean is(LabyrinthPlace place){return place==LabyrinthPlace.STILL_LIGHT||place==LabyrinthPlace.SOFT_FLOOR||place==LabyrinthPlace.RISING_STAIR||place==LabyrinthPlace.MANY_LENGTHS||place==LabyrinthPlace.FOLLOWING_DOOR;}
    static void build(ServerLevel level,BlockPos base,LabyrinthPlace place){
        if(place==LabyrinthPlace.STILL_LIGHT)buildStillLight(level,base);else if(place==LabyrinthPlace.SOFT_FLOOR)buildSoftFloor(level,base);
        else if(place==LabyrinthPlace.RISING_STAIR)buildRisingStair(level,base);
        else if(place==LabyrinthPlace.MANY_LENGTHS)buildLengths(level,base);
        else if(place==LabyrinthPlace.FOLLOWING_DOOR)buildFollowing(level,base);
    }
    static void tick(ServerLevel level,BlockPos base,LabyrinthPlace place,CompoundTag room,long clock,List<ServerPlayer> readers){
        // A room is ticked only while someone is in it. Coming back to one left empty, it is as it was before anyone came.
        long now=level.getGameTime();boolean back=now-room.getLong("TrialSeen")>1;room.putLong("TrialSeen",now);
        if(back)rest(level,base,place,room);
        if(place==LabyrinthPlace.STILL_LIGHT)stillLight(level,base,room,clock,readers);
        else if(place==LabyrinthPlace.SOFT_FLOOR)softFloor(level,base,clock,readers);
        else if(place==LabyrinthPlace.RISING_STAIR)risingStair(level,base,room,clock,readers);
        else if(place==LabyrinthPlace.MANY_LENGTHS)lengths(level,base,clock,readers);
        else if(place==LabyrinthPlace.FOLLOWING_DOOR)following(level,base,room,readers);
    }

    /** Everything as it was before anyone came: the boards and squares whole and the lamps out, the shaft dry. */
    static void rest(ServerLevel l,BlockPos b,LabyrinthPlace place,CompoundTag room){
        if(place==LabyrinthPlace.STILL_LIGHT){settle(l,b);room.putInt("StillFade",0);if(lit(room))lamps(l,b,room,false);}
        else if(place==LabyrinthPlace.SOFT_FLOOR)softSettle(l,b,true);
        else if(place==LabyrinthPlace.RISING_STAIR){flood(l,b,0);room.putInt("RisingEmpty",0);}
        else if(place==LabyrinthPlace.FOLLOWING_DOOR){for(int i=0;i<DOORWAYS.size();i++)shutDoorway(l,b,i);room.putInt("FollowOpen",-1);}
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
    /**
     * The holes in the floor over the steps up out of the trough: two beside the entry, and (0.4.403, the owner's playtest: "Too low
     * for players to climb back up") one against each side wall down the room, the east a third of the way, the west two thirds. A
     * reader who has fallen climbs out partway along, onto the steps' upper tread, where standing still brings the lamps up.
     */
    public static boolean stairWell(int x,int z){return x>=3&&(z==-3||z==-4)||x==HALF&&(z==-16||z==-17)||x==-HALF&&(z==-28||z==-29);}

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
        // 0.4.403: two more ways up, against the side walls, each the same two steps under its own hole in the floor; and a faint glow
        // at the foot of every way up, so a reader in the dark under the boards can see where they are.
        set(l,b,HALF,-3,-17,under);set(l,b,HALF,-3,-16,under);set(l,b,HALF,-2,-16,under);
        set(l,b,-HALF,-3,-29,under);set(l,b,-HALF,-3,-28,under);set(l,b,-HALF,-2,-28,under);
        var glow=Blocks.LIGHT.defaultBlockState().setValue(net.minecraft.world.level.block.LightBlock.LEVEL,6);
        set(l,b,HALF,-3,-5,glow);set(l,b,HALF,-3,-18,glow);set(l,b,-HALF,-3,-30,glow);
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
    // ---- The soft floor ----

    /** The squares run from the entry landing (z -1, -2) to the far landing (z -39 to -41), seven wide. */
    public static final int SOFT_NEAR=-3,SOFT_FAR=-38,SOFT_HALF=3,SOFT_FAR_WALL=-42;
    /** Ticks of weight (one a tick for each reader standing on it) at which a square sinks a little, sinks more, and gives way. */
    public static final int SINK=10,SINK_MORE=25,DROP=40;
    /** The low beams run across the room at these rows, at shoulder height (y 1, their lower half open). */
    public static final List<Integer> BEAM_Z=List.of(-10,-22,-33);
    public static final List<Integer> SOFT_LAMP_Z=List.of(-2,-8,-14,-20,-26,-32,-37);
    /** The two half-walls: at z -16 from the west wall to x 0, at z -28 from x 0 to the east wall. */
    public static boolean halfWall(int x,int z){return z==-16&&x<=0||z==-28&&x>=0;}
    /** The holes over the steps up out of the passage: two beside the entry, and (0.4.403, as in the still light) one against each side wall down the room. */
    public static boolean softWell(int x,int z){return x>=2&&(z==-3||z==-4)||x==SOFT_HALF&&(z==-19||z==-20)||x==-SOFT_HALF&&(z==-31||z==-32);}
    /** The stage a square's weight has brought it to. */
    public static int softStage(int load){return load>=DROP?3:load>=SINK_MORE?2:load>=SINK?1:0;}

    private static void buildSoftFloor(ServerLevel l,BlockPos b){
        var wall=mat("soft_wall");var ceiling=mat("soft_ceiling");var landing=mat("soft_landing");var under=mat("soft_under");var air=Blocks.AIR.defaultBlockState();
        box(l,b,-4,-4,SOFT_FAR_WALL,4,4,0,wall);
        box(l,b,-SOFT_HALF,4,SOFT_FAR_WALL+1,SOFT_HALF,4,-1,ceiling);
        box(l,b,-SOFT_HALF,0,SOFT_FAR_WALL+1,SOFT_HALF,3,-1,air);
        box(l,b,-SOFT_HALF,-4,SOFT_FAR,SOFT_HALF,-4,SOFT_NEAR,under);
        box(l,b,-SOFT_HALF,-3,SOFT_FAR,SOFT_HALF,-2,SOFT_NEAR,air);
        box(l,b,-SOFT_HALF,-1,-2,SOFT_HALF,-1,-1,landing);
        box(l,b,-SOFT_HALF,-1,SOFT_FAR_WALL+1,SOFT_HALF,-1,SOFT_FAR-1,landing);
        for(int z=SOFT_FAR;z<=SOFT_NEAR;z++)for(int x=-SOFT_HALF;x<=SOFT_HALF;x++)
            set(l,b,x,-1,z,softWell(x,z)?air:halfWall(x,z)?wall:TrialBlock.of(TrialBlock.Kind.SOFT_PLATE));
        for(int z:List.of(-16,-28))for(int x=-SOFT_HALF;x<=SOFT_HALF;x++)if(halfWall(x,z))box(l,b,x,0,z,x,3,z,wall);
        for(int z:BEAM_Z)box(l,b,-SOFT_HALF,1,z,SOFT_HALF,1,z,TrialBlock.of(TrialBlock.Kind.SOFT_BEAM));
        box(l,b,2,-3,-4,SOFT_HALF,-3,-4,under);
        box(l,b,2,-3,-3,SOFT_HALF,-2,-3,under);
        // 0.4.403: the same two steps up under each wall's hole partway down, and a faint glow at the foot of every way up.
        set(l,b,SOFT_HALF,-3,-20,under);set(l,b,SOFT_HALF,-3,-19,under);set(l,b,SOFT_HALF,-2,-19,under);
        set(l,b,-SOFT_HALF,-3,-32,under);set(l,b,-SOFT_HALF,-3,-31,under);set(l,b,-SOFT_HALF,-2,-31,under);
        var glow=Blocks.LIGHT.defaultBlockState().setValue(net.minecraft.world.level.block.LightBlock.LEVEL,6);
        set(l,b,SOFT_HALF,-3,-5,glow);set(l,b,SOFT_HALF,-3,-21,glow);set(l,b,-SOFT_HALF,-3,-33,glow);
        for(int z:SOFT_LAMP_Z)set(l,b,0,3,z,TrialBlock.of(TrialBlock.Kind.STILL_LAMP,1));
        LabyrinthBuilder.entrance(l,b,wall,landing,ceiling);
    }

    /** How much weight each square has borne, by position; squares nobody is on give it back a tick at a time. */
    private static final Map<Long,Integer> LOAD=new HashMap<>();
    public static int load(BlockPos at){return LOAD.getOrDefault(at.asLong(),0);}

    private static void softFloor(ServerLevel l,BlockPos b,long clock,List<ServerPlayer> readers){
        var weight=new HashMap<Long,Integer>();
        for(var p:readers){
            var on=p.getOnPos();var s=l.getBlockState(on);
            if(p.onGround()&&TrialBlock.is(s,TrialBlock.Kind.SOFT_PLATE)&&s.getValue(TrialBlock.STAGE)<3)weight.merge(on.asLong(),1,Integer::sum);
        }
        for(var e:weight.entrySet()){int load=LOAD.merge(e.getKey(),e.getValue(),Integer::sum);sink(l,BlockPos.of(e.getKey()),softStage(load));}
        for(var it=LOAD.entrySet().iterator();it.hasNext();){
            var e=it.next();if(weight.containsKey(e.getKey()))continue;
            var at=BlockPos.of(e.getKey());var s=l.getBlockState(at);
            if(!TrialBlock.is(s,TrialBlock.Kind.SOFT_PLATE)||s.getValue(TrialBlock.STAGE)==3){it.remove();continue;}
            int load=e.getValue()-1;if(load<=0)it.remove();else e.setValue(load);sink(l,at,softStage(Math.max(0,load)));
        }
        if(clock%60==0)softSettle(l,b,false);
    }
    /** Squares nobody is bearing down on are whole again; one that gave way only with nothing in its cell. Coming back, every square. */
    private static void softSettle(ServerLevel l,BlockPos b,boolean all){
        for(int z=SOFT_FAR;z<=SOFT_NEAR;z++)for(int x=-SOFT_HALF;x<=SOFT_HALF;x++){
            var at=b.offset(x,-1,z);var s=l.getBlockState(at);
            if(all)LOAD.remove(at.asLong());
            if(!TrialBlock.is(s,TrialBlock.Kind.SOFT_PLATE)||s.getValue(TrialBlock.STAGE)==0||LOAD.containsKey(at.asLong()))continue;
            if((all||s.getValue(TrialBlock.STAGE)==3)&&!l.getEntitiesOfClass(Entity.class,new AABB(at).expandTowards(0,1,0)).isEmpty())continue;
            l.setBlock(at,s.setValue(TrialBlock.STAGE,0),FLAGS);
        }
    }
    private static void sink(ServerLevel l,BlockPos at,int stage){
        var s=l.getBlockState(at);if(!TrialBlock.is(s,TrialBlock.Kind.SOFT_PLATE))return;int was=s.getValue(TrialBlock.STAGE);if(was==stage||was==3)return;
        l.setBlock(at,s.setValue(TrialBlock.STAGE,stage),FLAGS);
        if(stage==3){LOAD.remove(at.asLong());l.playSound(null,at,SoundEvents.MUD_BREAK,SoundSource.BLOCKS,1F,.6F);
            l.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,mat("soft_under")),at.getX()+.5,at.getY()+.6,at.getZ()+.5,12,.35,.1,.35,.05);}
        else if(stage>was)l.playSound(null,at,SoundEvents.MUD_STEP,SoundSource.BLOCKS,.8F,.5F+.1F*stage);
    }

    // ---- The rising stair ----

    /** The shaft is nine by nine (x -4..4, z -12..-4); the landing at its foot (z -1, -2) is behind a door in its south wall. */
    public static final int SHAFT_HALF=4,SHAFT_NEAR=-4,SHAFT_FAR=-12,SHAFT_TOP=24;
    /** The treads, one a cell round the walls, each half a block above the last: forty-four of them, up to the landing at the head. */
    public static final int TREADS=44;
    /** The treads missing, each a single stride to jump. */
    public static final Set<Integer> GAPS=Set.of(6,14,22,30,38,41);
    /** Ticks between one layer of water and the next; the layers the water fills, from the shaft floor up; ticks between draining layers. */
    public static final int RISE=30,LAYERS=21,DRAIN=4;
    /** The two pockets: the doorway a reader steps through from a tread, and the air trapped above the cell behind it. */
    public static final BlockPos POCKET_LOW=new BlockPos(5,4,-8),POCKET_HIGH=new BlockPos(-5,12,-8);
    /** Where the water is measured: the middle of the shaft, clear of every tread. */
    public static final BlockPos PROBE=new BlockPos(-2,0,-8);
    /** The cell of the ring at a step round it, clockwise from the foot of the stair beside the door. */
    public static int[] ring(int i){
        i=Math.floorMod(i,32);
        if(i<4)return new int[]{1+i,SHAFT_NEAR};
        if(i<12)return new int[]{SHAFT_HALF,SHAFT_NEAR-1-(i-4)};
        if(i<20)return new int[]{SHAFT_HALF-1-(i-12),SHAFT_FAR};
        if(i<28)return new int[]{-SHAFT_HALF,SHAFT_FAR+1+(i-20)};
        return new int[]{-SHAFT_HALF+1+(i-28),SHAFT_NEAR};
    }
    /** A tread's top, in half blocks above the shaft floor. */
    public static int treadTop2(int i){return i+1;}
    /** Where a tread stands: its block and whether it is a half step. */
    public static BlockPos tread(int i){int[] c=ring(i);int t2=treadTop2(i);return new BlockPos(c[0],t2%2==0?t2/2-1:(t2-1)/2,c[1]);}
    public static boolean halfTread(int i){return treadTop2(i)%2==1;}
    /** The pockets' cells: the doorway in the wall and the cell behind it fill with the water; the two above that never do. */
    public static List<BlockPos> pocketWater(BlockPos door){int out=door.getX()>0?1:-1;return List.of(door,door.above(),door.offset(out,0,0),door.offset(out,1,0));}
    public static List<BlockPos> pocketAir(BlockPos door){int out=door.getX()>0?1:-1;return List.of(door.offset(out,2,0),door.offset(out,3,0));}

    private static void buildRisingStair(ServerLevel l,BlockPos b){
        var wall=mat("rising_wall");var ceiling=mat("rising_ceiling");var floor=mat("rising_floor");var landing=mat("rising_landing");var window=mat("rising_window");var air=Blocks.AIR.defaultBlockState();
        box(l,b,-7,-1,SHAFT_FAR-1,7,SHAFT_TOP+1,0,wall);
        box(l,b,-SHAFT_HALF,SHAFT_TOP+1,SHAFT_FAR,SHAFT_HALF,SHAFT_TOP+1,SHAFT_NEAR,ceiling);
        box(l,b,-SHAFT_HALF,0,SHAFT_FAR,SHAFT_HALF,SHAFT_TOP,SHAFT_NEAR,air);
        box(l,b,-SHAFT_HALF,-1,SHAFT_FAR,SHAFT_HALF,-1,SHAFT_NEAR,floor);
        box(l,b,-1,0,-2,1,3,-1,air);box(l,b,-1,-1,-2,1,-1,-1,landing);
        box(l,b,0,0,-3,0,1,-3,air);LabyrinthBuilder.placeDoor(l,b.offset(0,0,-3),net.minecraft.core.Direction.SOUTH);
        for(int i=0;i<TREADS;i++)if(!GAPS.contains(i)){var at=tread(i);set(l,b,at.getX(),at.getY(),at.getZ(),TrialBlock.of(TrialBlock.Kind.RISING_TREAD,halfTread(i)?1:0));}
        box(l,b,0,21,SHAFT_FAR,3,21,SHAFT_FAR+1,landing);
        for(var door:List.of(POCKET_LOW,POCKET_HIGH)){for(var at:pocketWater(door))set(l,b,at.getX(),at.getY(),at.getZ(),air);for(var at:pocketAir(door))set(l,b,at.getX(),at.getY(),at.getZ(),air);}
        for(int y:List.of(2,8,14,20)){set(l,b,5,y,-11,window);set(l,b,-5,y,-5,window);set(l,b,-2,y,SHAFT_FAR-1,window);}
        LabyrinthBuilder.entrance(l,b,wall,landing,ceiling);
    }

    /** How many layers the water stands at, measured in the middle of the shaft. */
    public static int water(ServerLevel l,BlockPos b){int n=0;while(n<LAYERS&&l.getFluidState(b.offset(PROBE).above(n)).is(net.minecraft.tags.FluidTags.WATER))n++;return n;}
    /** On the stair: in the shaft below the highest the water goes. Not in a pocket, and not on the landing at the head. */
    public static boolean onStair(BlockPos b,net.minecraft.world.phys.Vec3 at){double x=at.x-b.getX(),z=at.z-b.getZ(),y=at.y-b.getY();return x>=-SHAFT_HALF&&x<SHAFT_HALF+1&&z>=SHAFT_FAR&&z<SHAFT_NEAR+1&&y>=-1&&y<LAYERS;}
    /** Fills or drains one layer of the shaft: open cells to water, treads waterlogged, the pockets' lower cells with them. */
    public static void layer(ServerLevel l,BlockPos b,int y,boolean wet){
        var water=Blocks.WATER.defaultBlockState();var air=Blocks.AIR.defaultBlockState();
        var cells=new ArrayList<BlockPos>();
        for(int z=SHAFT_FAR;z<=SHAFT_NEAR;z++)for(int x=-SHAFT_HALF;x<=SHAFT_HALF;x++)cells.add(new BlockPos(x,y,z));
        for(var door:List.of(POCKET_LOW,POCKET_HIGH))for(var at:pocketWater(door))if(at.getY()==y)cells.add(at);
        for(var rel:cells){var at=b.offset(rel);var s=l.getBlockState(at);
            if(s.is(TrialRegistry.FITTING.get())){if(s.getValue(TrialBlock.WATERLOGGED)!=wet)l.setBlock(at,s.setValue(TrialBlock.WATERLOGGED,wet),FLAGS);}
            else if(wet&&s.isAir())l.setBlock(at,water,FLAGS);
            else if(!wet&&s.is(Blocks.WATER))l.setBlock(at,air,FLAGS);
        }
    }
    /** Test and command hook: the water at a given number of layers at once. */
    public static void flood(ServerLevel l,BlockPos b,int layers){for(int y=0;y<LAYERS;y++)layer(l,b,y,y<layers);}

    private static void risingStair(ServerLevel l,BlockPos b,CompoundTag room,long clock,List<ServerPlayer> readers){
        boolean anyone=false;
        for(var p:readers)if(onStair(b,p.position()))anyone=true;
        int level=water(l,b);
        if(anyone){room.putInt("RisingEmpty",0);
            if(clock%RISE==0&&level<LAYERS){layer(l,b,level,true);l.playSound(null,b.offset(0,level,-8),SoundEvents.BUCKET_EMPTY,SoundSource.BLOCKS,.9F,.5F);}
        }else{int empty=room.getInt("RisingEmpty")+1;room.putInt("RisingEmpty",empty);
            if(empty>=20&&level>0&&clock%DRAIN==0){layer(l,b,level-1,false);if(level%4==1)l.playSound(null,b.offset(0,level,-8),SoundEvents.BUCKET_FILL,SoundSource.BLOCKS,.7F,.5F);}
        }
        for(var p:readers){
            var rel=p.position().subtract(b.getX(),b.getY(),b.getZ());
            boolean shaft=rel.x>=-SHAFT_HALF&&rel.x<SHAFT_HALF+1&&rel.z>=SHAFT_FAR&&rel.z<SHAFT_NEAR+1;
            if(shaft&&l.getFluidState(p.blockPosition()).is(net.minecraft.tags.FluidTags.WATER)){p.push(0,-.05,0);p.hurtMarked=true;}
            if(l.getFluidState(BlockPos.containing(p.getEyePosition())).is(net.minecraft.tags.FluidTags.WATER)&&p.getAirSupply()<=0){
                p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-1.5,180,0);p.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);p.resetFallDistance();p.setAirSupply(p.getMaxAirSupply());
                p.displayClientMessage(Component.literal("The landing again, and the taste of the water.").withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);
            }
        }
    }

    // ---- The hall of many lengths ----

    /** One length of the hall: nine blocks on, then a bend three aside to the next, as in the long hallway (LabyrinthLoops). */
    public static final BlockPos LENGTH=new BlockPos(3,0,-12);
    /** Where the first length starts. The bend behind the entry door is length -1, so every length has the same bend behind it. */
    public static final BlockPos FIRST_LENGTH=new BlockPos(3,0,-3);
    public static final int LENGTH_STRAIGHT=9,LENGTHS=5;
    /** Leaving a length into this one or later, up to the last, a reader who hurried through it walks it again. The first and last lengths are free. */
    public static final int FIRST_FOLD=2,LAST_FOLD=4;
    /** Ticks between the steps the first through hear in the walls. */
    public static final int STEPS=7;
    public static BlockPos lengthOrigin(int k){return FIRST_LENGTH.offset(LENGTH.getX()*k,0,LENGTH.getZ()*k);}
    /** Which length a z coordinate lies in: -1 the bend behind the door, LENGTHS the far wall. */
    public static int lengthOf(BlockPos b,double z){return (int)Math.floor((b.getZ()+FIRST_LENGTH.getZ()+1-z)/-LENGTH.getZ());}
    /** Whether a column (relative to the base) is open floor: standing room inside the entry door, each length's straight, and every bend from the one behind the door to the last. */
    public static boolean lengthOpen(int x,int z){
        if(x>=-1&&x<=1&&z<=0&&z>=-3)return true;
        for(int k=-1;k<LENGTHS;k++){var o=lengthOrigin(k);int lx=x-o.getX(),lz=z-o.getZ();
            if(k>=0&&lx>=-1&&lx<=1&&lz<=0&&lz>=-(LENGTH_STRAIGHT-1))return true;
            if(lx>=-1&&lx<=4&&lz<=-9&&lz>=-11)return true;}
        return false;
    }
    /** The far door: where the sixth length would begin, round the last bend from the fifth's straight. */
    public static BlockPos lengthsDoor(){return lengthOrigin(LENGTHS);}

    private static void buildLengths(ServerLevel l,BlockPos b){
        var wall=mat("lengths_wall");var rule=mat("lengths_rule");var floor=mat("lengths_floor");var ceiling=mat("lengths_ceiling");var air=Blocks.AIR.defaultBlockState();
        var door=lengthsDoor();
        box(l,b,-2,-1,door.getZ(),door.getX()+2,3,0,wall);
        for(int x=-1;x<=door.getX()+1;x++)for(int z=door.getZ()+1;z<=0;z++){if(!lengthOpen(x,z))continue;
            set(l,b,x,-1,z,floor);for(int y=0;y<=2;y++)set(l,b,x,y,z,air);set(l,b,x,3,z,ceiling);
            for(var d:net.minecraft.core.Direction.Plane.HORIZONTAL){int nx=x+d.getStepX(),nz=z+d.getStepZ();
                if(nz<=0&&nz>=door.getZ()&&!lengthOpen(nx,nz))set(l,b,nx,1,nz,rule);}
        }
        for(int k=0;k<LENGTHS;k++){var o=lengthOrigin(k);set(l,b,o.getX(),2,o.getZ()-4,TrialBlock.of(TrialBlock.Kind.STILL_LAMP,1));}
        LabyrinthBuilder.entrance(l,b,wall,floor,ceiling);
    }

    /** Each reader's pace: the length they were last in, whether they hurried in it, a move back owed, their jumps and moves back so far. */
    private static final class Pace{int length,jumps,folds;boolean hurried,owed;long seen;double x,z,moved;}
    private static final Map<ServerPlayer,Pace> PACE=new WeakHashMap<>();
    private static int jumps(ServerPlayer p){return p.getStats().getValue(net.minecraft.stats.Stats.CUSTOM.get(net.minecraft.stats.Stats.JUMP));}
    /** How many times the hall has moved a reader back, this time through. */
    public static int folds(ServerPlayer p){var s=PACE.get(p);return s==null?0:s.folds;}

    private static void lengths(ServerLevel l,BlockPos b,long clock,List<ServerPlayer> readers){
        long now=l.getGameTime();
        for(var p:readers){
            int k=lengthOf(b,p.getZ());
            var s=PACE.get(p);
            if(s==null||now-s.seen>1){s=new Pace();s.length=k;s.jumps=jumps(p);s.x=p.getX();s.z=p.getZ();PACE.put(p,s);}
            s.seen=now;s.moved=Math.hypot(p.getX()-s.x,p.getZ()-s.z);s.x=p.getX();s.z=p.getZ();
            int j=jumps(p);if(p.isSprinting()||j>s.jumps)s.hurried=true;s.jumps=j;
            if(k!=s.length){
                if(k==s.length+1&&k>=FIRST_FOLD&&k<=LAST_FOLD&&s.hurried)s.owed=true;
                else if(k<s.length)s.owed=false;
                s.length=k;s.hurried=false;
            }
            if(!s.owed)continue;
            // Owed a move back: only from inside the straight of a length that has one behind it, and only unseen.
            if(k<FIRST_FOLD||k>LAST_FOLD||p.getZ()-b.getZ()<=lengthOrigin(k).getZ()-(LENGTH_STRAIGHT-1)){s.owed=false;continue;}
            var to=p.position().subtract(LENGTH.getX(),0,LENGTH.getZ());
            if(seenByAnother(l,p,p.getEyePosition(),p.position().add(0,1,0),to.add(0,p.getEyeHeight(),0),to.add(0,1,0)))continue;
            io.github.knaitoe.theoldesthouse.house.HouseInternalTeleport.shift(p,to,p.getYRot());
            s.x=to.x;s.z=to.z;s.length=k-1;s.hurried=false;s.owed=false;s.folds++;
            if(s.folds==1)line(p,"The same bend, again.");else if(s.folds==3)line(p,"No nearer for hurrying.");
        }
        // The first through hear the others in the walls, as long as anyone behind them is still walking.
        if(clock%STEPS==0)for(var ahead:readers){int ka=lengthOf(b,ahead.getZ());if(ka<LENGTHS-1)continue;
            for(var behind:readers){var s=PACE.get(behind);if(behind==ahead||s==null||lengthOf(b,behind.getZ())>=LENGTHS-1||s.moved<.05)continue;
                double side=(clock/STEPS)%2==0?2.5:-2.5;
                LiteraryVignettes.heard(ahead,SoundEvents.WOOD_STEP,BlockPos.containing(ahead.getX()+side,ahead.getY(),ahead.getZ()),.35F,.75F+.1F*(float)((clock/STEPS)%3));
                break;}}
    }
    private static void line(ServerPlayer p,String text){p.displayClientMessage(Component.literal(text).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}
    /** Whether anyone else could see a point, a spectator's camera included: within 48 blocks, inside a wide cone, nothing solid between. */
    static boolean seenByAnother(ServerLevel l,ServerPlayer self,net.minecraft.world.phys.Vec3... points){
        for(var o:l.players()){if(o==self||o.isSleeping())continue;var cam=o.getCamera();if(cam.level()!=l)continue;var eye=cam.getEyePosition();var look=cam.getLookAngle();
            for(var pt:points){var to=pt.subtract(eye);double d=to.length();if(d>48)continue;if(d<1.5)return true;if(look.dot(to.scale(1/d))<.26)continue;
                var hit=l.clip(new net.minecraft.world.level.ClipContext(eye,pt,net.minecraft.world.level.ClipContext.Block.VISUAL,net.minecraft.world.level.ClipContext.Fluid.NONE,cam));
                if(hit.getType()==net.minecraft.world.phys.HitResult.Type.MISS)return true;}}
        return false;
    }

    // ---- The door that follows ----

    /** The room is eleven by eleven (x -5..5, z -1..-11) and four high; behind its west, north and east walls a passage runs to the far door. */
    public static final int FOLLOW_HALF=5,FOLLOW_FAR=-11;
    /** The nine doors, three in each of the west, north and east walls: the lower cell of each doorway, in the wall. */
    public static final List<BlockPos> DOORWAYS=List.of(new BlockPos(-6,0,-3),new BlockPos(-6,0,-6),new BlockPos(-6,0,-9),new BlockPos(-3,0,-12),
        new BlockPos(0,0,-12),new BlockPos(3,0,-12),new BlockPos(6,0,-3),new BlockPos(6,0,-6),new BlockPos(6,0,-9));
    /** Shut doors seen shutting before a reader is told once where the open one always is. */
    public static final int FOLLOW_HINT=6;
    /** The way into the room from a doorway. */
    public static net.minecraft.core.Direction into(BlockPos doorway){return doorway.getZ()==-12?net.minecraft.core.Direction.SOUTH:doorway.getX()<0?net.minecraft.core.Direction.EAST:net.minecraft.core.Direction.WEST;}
    /** The doorway standing open, or -1. */
    public static int openDoorway(CompoundTag room){return room.contains("FollowOpen")?room.getInt("FollowOpen"):-1;}
    public static boolean inFollowRoom(BlockPos b,net.minecraft.world.phys.Vec3 at){double x=at.x-b.getX(),z=at.z-b.getZ();return x>=-FOLLOW_HALF&&x<FOLLOW_HALF+1&&z>=FOLLOW_FAR&&z<0;}
    /** A point just inside the room in front of a doorway, at a height: where anyone looking at the door would be looking. */
    public static net.minecraft.world.phys.Vec3 doorwayFront(BlockPos b,BlockPos doorway,double y){var d=into(doorway);return net.minecraft.world.phys.Vec3.atLowerCornerOf(b.offset(doorway)).add(.5+d.getStepX()*.55,y,.5+d.getStepZ()*.55);}

    private static void buildFollowing(ServerLevel l,BlockPos b){
        var wall=mat("follow_wall");var floor=mat("follow_floor");var ceiling=mat("follow_ceiling");var trim=mat("follow_trim");var back=mat("follow_back");var air=Blocks.AIR.defaultBlockState();
        box(l,b,-8,-1,-14,8,4,0,back);
        box(l,b,-6,0,-12,6,3,0,wall);
        box(l,b,-FOLLOW_HALF,0,FOLLOW_FAR,FOLLOW_HALF,3,-1,air);box(l,b,-FOLLOW_HALF,-1,FOLLOW_FAR,FOLLOW_HALF,-1,-1,floor);box(l,b,-FOLLOW_HALF,4,FOLLOW_FAR,FOLLOW_HALF,4,-1,ceiling);
        // The passage behind the walls: down the west and east sides and across the north, to the far door.
        box(l,b,-7,0,-13,-7,2,-1,air);box(l,b,7,0,-13,7,2,-1,air);box(l,b,-7,0,-13,7,2,-13,air);
        box(l,b,-7,-1,-13,-7,-1,-1,trim);box(l,b,7,-1,-13,7,-1,-1,trim);box(l,b,-7,-1,-13,7,-1,-13,trim);
        for(var d:DOORWAYS){var side=into(d).getClockWise();
            for(int y=0;y<=2;y++){set(l,b,d.getX()+side.getStepX(),y,d.getZ()+side.getStepZ(),trim);set(l,b,d.getX()-side.getStepX(),y,d.getZ()-side.getStepZ(),trim);}
            set(l,b,d.getX(),2,d.getZ(),trim);
            set(l,b,d.getX(),0,d.getZ(),TrialBlock.of(TrialBlock.Kind.FOLLOW_PANEL,0));set(l,b,d.getX(),1,d.getZ(),TrialBlock.of(TrialBlock.Kind.FOLLOW_PANEL,1));
        }
        for(int x:new int[]{-3,3}){set(l,b,x,1,-1,TrialMirrorBlock.of(net.minecraft.core.Direction.NORTH,false));set(l,b,x,2,-1,TrialMirrorBlock.of(net.minecraft.core.Direction.NORTH,true));}
        for(var lamp:List.of(new BlockPos(0,3,-6),new BlockPos(-3,3,-3),new BlockPos(3,3,-3),new BlockPos(-3,3,-9),new BlockPos(3,3,-9),new BlockPos(-7,2,-7),new BlockPos(7,2,-7),new BlockPos(0,2,-13)))
            set(l,b,lamp.getX(),lamp.getY(),lamp.getZ(),TrialBlock.of(TrialBlock.Kind.STILL_LAMP,1));
        LabyrinthBuilder.entrance(l,b,wall,floor,ceiling);
    }
    private static void shutDoorway(ServerLevel l,BlockPos b,int i){var d=b.offset(DOORWAYS.get(i));
        if(!TrialBlock.is(l.getBlockState(d),TrialBlock.Kind.FOLLOW_PANEL))l.setBlock(d,TrialBlock.of(TrialBlock.Kind.FOLLOW_PANEL,0),FLAGS);
        if(!TrialBlock.is(l.getBlockState(d.above()),TrialBlock.Kind.FOLLOW_PANEL))l.setBlock(d.above(),TrialBlock.of(TrialBlock.Kind.FOLLOW_PANEL,1),FLAGS);}
    private static void openDoorwayAt(ServerLevel l,BlockPos b,int i){var d=b.offset(DOORWAYS.get(i));l.setBlock(d,Blocks.AIR.defaultBlockState(),FLAGS);l.setBlock(d.above(),Blocks.AIR.defaultBlockState(),FLAGS);}
    /** Shut doors each reader has seen shutting. */
    private static final Map<ServerPlayer,Integer> SHUT_SEEN=new WeakHashMap<>();

    private static void following(ServerLevel l,BlockPos b,CompoundTag room,List<ServerPlayer> readers){
        // Who could see a door: every camera in the room itself (spectators too), not anyone already through into the passage.
        var watchers=new ArrayList<Entity>();
        for(var o:l.players()){var cam=o.getCamera();if(!o.isSleeping()&&cam.level()==l&&inFollowRoom(b,cam.position()))watchers.add(cam);}
        int open=openDoorway(room);
        if(open>=0&&!doorwaySeen(l,watchers,b,DOORWAYS.get(open)))return;
        if(open>=0&&!l.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,new AABB(b.offset(DOORWAYS.get(open))).expandTowards(0,1,0)).isEmpty())return;
        int next=-1;double best=Double.MAX_VALUE;
        for(int i=0;i<DOORWAYS.size();i++){if(i==open)continue;var d=DOORWAYS.get(i);if(doorwaySeen(l,watchers,b,d))continue;
            double score=-1;for(var w:watchers){var to=doorwayFront(b,d,1).subtract(w.getEyePosition());score=Math.max(score,w.getLookAngle().dot(to.normalize()));}
            if(score<best){best=score;next=i;}}
        if(open>=0){shutDoorway(l,b,open);l.playSound(null,b.offset(DOORWAYS.get(open)),SoundEvents.WOODEN_DOOR_CLOSE,SoundSource.BLOCKS,.7F,.8F);
            for(var w:watchers)if(w instanceof ServerPlayer p&&!p.isSpectator()){int n=SHUT_SEEN.merge(p,1,Integer::sum);if(n==FOLLOW_HINT)line(p,"It is always the one behind you.");}}
        if(next>=0){openDoorwayAt(l,b,next);l.playSound(null,b.offset(DOORWAYS.get(next)),SoundEvents.WOODEN_DOOR_OPEN,SoundSource.BLOCKS,.7F,.8F);}
        room.putInt("FollowOpen",next);
    }
    /** Whether any camera in the room could see a doorway: the room just in front of it, low or high, in a wide cone with nothing between. */
    public static boolean doorwaySeen(ServerLevel l,List<? extends Entity> watchers,BlockPos b,BlockPos doorway){
        for(var w:watchers){var eye=w.getEyePosition();var look=w.getLookAngle();
            for(double y:new double[]{.5,1.5}){var pt=doorwayFront(b,doorway,y);var to=pt.subtract(eye);double d=to.length();if(d>48||d<1E-3)continue;
                if(look.dot(to.scale(1/d))<.26)continue;
                var hit=l.clip(new net.minecraft.world.level.ClipContext(eye,pt,net.minecraft.world.level.ClipContext.Block.VISUAL,net.minecraft.world.level.ClipContext.Fluid.NONE,w));
                if(hit.getType()==net.minecraft.world.phys.HitResult.Type.MISS)return true;}}
        return false;
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
