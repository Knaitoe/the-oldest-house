package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import static io.github.knaitoe.theoldesthouse.labyrinth.FoldBlock.Kind.*;

/**
 * 0.4.250, the Folding Rooms (docs/FOLDING_ROOMS_0_4_250.md), the owner's first proposal of 2 November: "A former dining room has heavy
 * wooden partitions on floor and ceiling tracks. Moving one opens a passage while closing another; gaps above the panels let you see
 * where you're trying to reach. The early version teaches one sliding partition. The deep version has several connected spaces, with
 * wall recesses where you can stand while shifting them. Worn tracks and furniture impressions show how the room used to fit together.
 * Solving it leaves an understandable, usable space."
 * <p>
 * A partition is a wall of hinged panels as wide as the room and two high, hung from a brass track in the ceiling and guided by
 * another in the floor, both running the room's length. Above it there is a gap to the ceiling: the far side can be seen, not
 * reached. A hand on it pushes it one length of panel along its track, away from the reader. It stops at the end of its track, at
 * furniture and at anyone in its way; it never moves through a body. From beside it, in a recess or a doorway off the track, it
 * goes the way the reader faces, so it can be pushed right past whoever stands there and they step out on its other side.
 * <p>
 * <b>The folding room</b> (early): a dining room cut in two by one partition, standing against its far stop in front of the far
 * door, with the pantry round the side. Pushed from the side it was not pushed from, it clears the door, and comes to rest across the
 * serving hatch. <b>The folded rooms</b> (deep): a long dining room in three by two partitions, a recess in its wall where the
 * sideboard has stood for years, and a morning room off it whose doorway is the other place to stand off the track.
 * <p>
 * Reaching the far door folds the partitions back on their hinges into the corners, and the deep room is laid for dinner where its
 * floor shows the table and chairs stood. Like every puzzle room nothing counts down, hurts or is used up; solved stays solved, for
 * everyone; no Witness.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class FoldingRooms {
    private FoldingRooms(){}
    public static final LabyrinthPlace EARLY=LabyrinthPlace.FOLDING_ROOM,DEEP=LabyrinthPlace.FOLDED_ROOMS;
    public static final String STATE="folding_rooms_0250";
    private static final int FLAGS=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;

    /** One partition: the width it spans, the track it runs on (its north and south stops), where it starts, and where it folds away to. */
    public record Partition(int x0,int x1,int north,int south,int start,int home){
        public boolean on(int z){return z>=north&&z<=south;}
        int[] hangers(){return x1-x0>=6?new int[]{x0,(x0+x1)/2,x1}:new int[]{x0,x1};}
    }

    // ------------------------------------------------------------------ the folding room (early)
    /** The dining room (x -3 to 3, z -1 to -16) and its partition, against its north stop in front of the far door; the pantry (x -8 to -5) and its two doorways; the serving hatch. */
    public static final Partition EARLY_P=new Partition(-3,3,-13,-5,-13,-12);
    public static final BlockPos EARLY_DOOR_FRONT=new BlockPos(3,0,-13),SERVING_HATCH=new BlockPos(-4,1,-12),PANTRY_SOUTH=new BlockPos(-4,0,-3),PANTRY_NORTH=new BlockPos(-4,0,-15);

    // ------------------------------------------------------------------ the folded rooms (deep)
    /** The long room (x -2 to 2, z -1 to -25): the first partition's track and the recess beside it, the gap between the tracks, the second's track, the far end. */
    public static final Partition DEEP_P1=new Partition(-2,2,-10,-4,-4,-4),DEEP_P2=new Partition(-2,2,-20,-12,-16,-20);
    public static final BlockPos RECESS=new BlockPos(-3,0,-6),MORNING_SOUTH=new BlockPos(-3,0,-11),MORNING_NORTH=new BlockPos(-3,0,-16);
    /** The far end of the long room: anyone standing here has come through both partitions. */
    public static final int FAR_END=-21;
    /** The dinner: the table down the middle of the room, the chairs either side, as the worn floor shows them. */
    public static final int TABLE_FROM=-8,TABLE_TO=-16;
    public static final int[] CHAIR_ROWS={-9,-11,-13,-15};

    public static List<Partition> partitions(LabyrinthPlace place){return place==EARLY?List.of(EARLY_P):List.of(DEEP_P1,DEEP_P2);}
    static BlockState mat(String name){return PuzzleRegistry.foldMaterial(name).defaultBlockState();}
    static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){l.setBlock(b.offset(at),s,FLAGS);}
    static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){l.setBlock(b.offset(x,y,z),s,FLAGS);}
    static boolean solved(ServerLevel l,LabyrinthPlace place){return PuzzleRooms.solved(l.getServer(),place);}

    // ------------------------------------------------------------------ building
    /** Walls panelled to the dado and papered above, a parquet floor, a plastered ceiling, everything inside them air. */
    static void shell(ServerLevel l,BlockPos b,int x0,int z0,int x1,int z1){
        var panelling=mat("panelling");var paper=mat("paper");var parquet=mat("parquet");var ceiling=mat("ceiling");
        for(int x=x0;x<=x1;x++)for(int z=z1;z<=z0;z++)for(int y=-1;y<=5;y++){
            boolean wall=x==x0||x==x1||z==z0||z==z1;
            set(l,b,x,y,z,y==-1?parquet:y==5?ceiling:wall?(y<=1?panelling:paper):Blocks.AIR.defaultBlockState());
        }
    }
    /** A partition's floor and ceiling tracks, worn where it has most often stood. */
    static void tracks(ServerLevel l,BlockPos b,Partition q,int... worn){
        int mid=(q.x0()+q.x1())/2;
        for(int z=q.north();z<=q.south();z++){boolean w=false;for(int k:worn)w|=k==z;set(l,b,mid,-1,z,mat(w?"track_worn":"track"));set(l,b,mid,5,z,mat("ceiling_track"));}
    }
    static void lamp(ServerLevel l,BlockPos b,int x,int z){set(l,b,x,4,z,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));}
    static BlockState furniture(HouseholdFurnitureBlock.Kind kind,Direction facing){return HouseholdFurnitureBlock.state(kind,facing);}

    public static void buildEarly(ServerLevel l,BlockPos b){
        boolean solved=solved(l,EARLY);
        shell(l,b,-4,0,4,-17);
        // The pantry round the west side: its own walls, two doorways into the dining room, and the hatch between.
        shell(l,b,-9,-2,-4,-17);
        for(int z=-16;z<=-3;z++)for(int y=0;y<=4;y++)set(l,b,-4,y,z,y<=1?mat("panelling"):mat("paper"));
        for(var door:List.of(PANTRY_SOUTH,PANTRY_NORTH))for(int y=0;y<=1;y++)set(l,b,door.above(y),Blocks.AIR.defaultBlockState());
        set(l,b,SERVING_HATCH,FoldBlock.of(HATCH,Direction.EAST));
        for(int z=-14;z>=-16;z--)set(l,b,-8,0,z,Blocks.BARREL.defaultBlockState());
        for(int z=-5;z>=-12;z--){set(l,b,-8,1,z,Blocks.SPRUCE_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.HALF,Half.TOP).setValue(TrapDoorBlock.FACING,Direction.EAST).setValue(TrapDoorBlock.OPEN,false));
            set(l,b,-8,3,z,Blocks.SPRUCE_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.HALF,Half.TOP).setValue(TrapDoorBlock.FACING,Direction.EAST).setValue(TrapDoorBlock.OPEN,false));}
        set(l,b,-5,0,-9,Blocks.CAULDRON.defaultBlockState());set(l,b,-5,0,-10,Blocks.SMOOTH_STONE_SLAB.defaultBlockState());
        lamp(l,b,-6,-6);lamp(l,b,-6,-13);
        // The dining room: its table and chairs at the south end, a lamp each side of the partition so the far side is lit.
        for(int x=-1;x<=1;x++)set(l,b,x,0,-8,FoldBlock.of(TABLE,Direction.NORTH));
        for(int x:new int[]{-1,1}){set(l,b,x,0,-7,FoldBlock.of(CHAIR,Direction.NORTH));set(l,b,x,0,-9,FoldBlock.of(CHAIR,Direction.SOUTH));}
        lamp(l,b,0,-4);lamp(l,b,0,-15);
        tracks(l,b,EARLY_P,-13,-12);
        if(solved)foldAway(l,b,EARLY_P,EARLY_P.start()+1);else place(l,b,EARLY_P,EARLY_P.start());
        LabyrinthBuilder.entrance(l,b,mat("panelling"),mat("parquet"),mat("ceiling"));
        LabyrinthBuilder.doors(l,b,EARLY);
    }
    public static void buildDeep(ServerLevel l,BlockPos b){
        boolean solved=solved(l,DEEP);
        shell(l,b,-3,0,3,-26);
        // The morning room off the west side, its two doorways into the long room.
        shell(l,b,-9,-10,-3,-18);
        for(int z=-25;z<=-1;z++)for(int y=0;y<=4;y++)set(l,b,-3,y,z,y<=1?mat("panelling"):mat("paper"));
        for(var door:List.of(MORNING_SOUTH,MORNING_NORTH))for(int y=0;y<=1;y++)set(l,b,door.above(y),Blocks.AIR.defaultBlockState());
        set(l,b,-8,0,-13,furniture(HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,Direction.EAST));set(l,b,-8,0,-15,furniture(HouseholdFurnitureBlock.Kind.FLORAL_ARMCHAIR,Direction.EAST));
        set(l,b,-6,0,-17,furniture(HouseholdFurnitureBlock.Kind.READING_DESK,Direction.SOUTH));set(l,b,-7,0,-14,furniture(HouseholdFurnitureBlock.Kind.FOOTSTOOL,Direction.EAST));
        lamp(l,b,-6,-14);
        // The recess in the west wall beside the first track: two blocks of the panelling cut back, and the floor in it worn where the sideboard has always stood.
        for(int z=RECESS.getZ()-1;z<=RECESS.getZ()+1;z++)for(int y=-1;y<=5;y++)set(l,b,RECESS.getX()-1,y,z,y<=1?mat("panelling"):mat("paper"));
        for(int y=0;y<=1;y++)set(l,b,RECESS.above(y),Blocks.AIR.defaultBlockState());
        set(l,b,RECESS.below(),mat("marks"));
        // Where the table and chairs used to stand, the floor is less worn.
        for(int z:CHAIR_ROWS)for(int x:new int[]{-1,1})set(l,b,x,-1,z,mat("marks"));
        tracks(l,b,DEEP_P1,DEEP_P1.home(),-7,-6,-5);tracks(l,b,DEEP_P2,DEEP_P2.home(),-17,-16,-15);
        lamp(l,b,0,-2);lamp(l,b,0,-8);lamp(l,b,0,-14);lamp(l,b,0,-23);
        if(solved){foldAway(l,b,DEEP_P1,DEEP_P1.home());foldAway(l,b,DEEP_P2,DEEP_P2.home());set(l,b,RECESS,FoldBlock.of(SIDEBOARD,Direction.EAST));lay(l,b);}
        else{place(l,b,DEEP_P1,DEEP_P1.start());place(l,b,DEEP_P2,DEEP_P2.start());set(l,b,RECESS,FoldBlock.of(SIDEBOARD,Direction.EAST));}
        LabyrinthBuilder.entrance(l,b,mat("panelling"),mat("parquet"),mat("ceiling"));
        LabyrinthBuilder.doors(l,b,DEEP);
    }

    // ------------------------------------------------------------------ partitions
    /** Where a partition stands on its track, or {@link Integer#MIN_VALUE} if it is folded away. */
    public static int at(ServerLevel l,BlockPos b,Partition q){
        for(int z=q.north();z<=q.south();z++){var s=l.getBlockState(b.offset(q.x0(),0,z));if(FoldBlock.is(s,PANEL))return z;}
        return Integer.MIN_VALUE;
    }
    static void place(ServerLevel l,BlockPos b,Partition q,int z){
        for(int x=q.x0();x<=q.x1();x++){
            set(l,b,x,0,z,FoldBlock.of(PANEL,Direction.NORTH,FoldBlock.LOWER));
            set(l,b,x,1,z,FoldBlock.of(PANEL,Direction.NORTH,x==q.x0()||x==q.x1()?FoldBlock.PULL:FoldBlock.UPPER));
        }
        for(int x:q.hangers())for(int y=2;y<=4;y++)set(l,b,x,y,z,FoldBlock.of(HANGER,Direction.NORTH,y==4?1:0));
    }
    static void clear(ServerLevel l,BlockPos b,Partition q,int z){
        for(int x=q.x0();x<=q.x1();x++)for(int y=0;y<=4;y++){var s=l.getBlockState(b.offset(x,y,z));if(FoldBlock.partition(s))set(l,b,x,y,z,Blocks.AIR.defaultBlockState());}
    }
    /** The partition folded back on its hinges into the two corners of the row it is folded to. */
    static void foldAway(ServerLevel l,BlockPos b,Partition q,int z){
        for(int y=0;y<=1;y++){set(l,b,q.x0(),y,z,FoldBlock.of(FOLDED,Direction.EAST,y));set(l,b,q.x1(),y,z,FoldBlock.of(FOLDED,Direction.WEST,y));}
    }
    /** Anyone alive and present in a row of the room, or the air above it to a partition's height. */
    static boolean occupied(ServerLevel l,BlockPos b,int x0,int x1,int z){
        var box=new AABB(b.getX()+x0,b.getY(),b.getZ()+z,b.getX()+x1+1,b.getY()+3,b.getZ()+z+1).deflate(.01);
        return !l.getEntitiesOfClass(LivingEntity.class,box,e->e.isAlive()&&!e.isSpectator()).isEmpty();
    }
    /** Why a partition cannot move into a row, or null when it can. */
    static @Nullable String blocked(ServerLevel l,BlockPos b,Partition q,int to){
        if(!q.on(to))return "It is at the end of its track.";
        for(int x=q.x0();x<=q.x1();x++)for(int y=0;y<=1;y++){
            var s=l.getBlockState(b.offset(x,y,to));if(s.isAir())continue;
            if(FoldBlock.is(s,SIDEBOARD))return "It runs up against the sideboard and stops.";
            if(FoldBlock.is(s,TABLE)||FoldBlock.is(s,CHAIR))return "It runs up against the table and chairs and stops.";
            return "Something stops it.";
        }
        for(int x:q.hangers())for(int y=2;y<=4;y++)if(!l.getBlockState(b.offset(x,y,to)).isAir())return "Something stops it.";
        if(occupied(l,b,q.x0(),q.x1(),to))return "Someone is in its way.";
        return null;
    }
    /**
     * A hand on a partition. In front of it, it goes away from the reader; beside it (in a recess or a doorway, off the track), the
     * way the reader faces. One panel's length a push.
     */
    static boolean push(ServerPlayer p,ServerLevel l,BlockPos b,LabyrinthPlace place,Partition q){
        int z=at(l,b,q);if(z==Integer.MIN_VALUE)return false;
        double rel=p.getZ()-(b.getZ()+z+.5),px=p.getX()-b.getX();int dir;
        // Off the track to either side of it (a recess, a doorway), the reader pushes it the way they face, even past themselves.
        boolean beside=px<q.x0()||px>q.x1()+1;
        if(!beside&&Math.abs(rel)>=.6)dir=rel>0?-1:1;
        else{var look=p.getLookAngle();if(Math.abs(look.z)<.25){PuzzleRooms.say(p,"It only runs along its track. Face the way you want it to go.");return true;}dir=look.z<0?-1:1;}
        int to=z+dir;var why=blocked(l,b,q,to);
        if(why!=null){PuzzleRooms.say(p,why);l.playSound(null,b.offset((q.x0()+q.x1())/2,1,z),SoundEvents.WOOD_HIT,SoundSource.BLOCKS,.8F,.6F);return true;}
        clear(l,b,q,z);place(l,b,q,to);
        var at=b.offset((q.x0()+q.x1())/2,1,to);
        l.playSound(null,at,SoundEvents.PISTON_CONTRACT,SoundSource.BLOCKS,.45F,.5F);l.playSound(null,at,SoundEvents.WOODEN_TRAPDOOR_CLOSE,SoundSource.BLOCKS,.6F,.6F);
        var own=own(l,p);if(!own.getBoolean("Told")){own.putBoolean("Told",true);save(l,p,own);PuzzleRooms.say(p,"The panels flex on their hinges and the whole partition rumbles along its track.");}
        return true;
    }
    @Nullable static Partition partitionAt(LabyrinthPlace place,BlockPos rel){
        for(var q:partitions(place))if(rel.getX()>=q.x0()&&rel.getX()<=q.x1()&&q.on(rel.getZ()))return q;
        return null;
    }

    // ------------------------------------------------------------------ the sideboard
    /** Pulled a block toward the reader like the dust rooms' chair, never onto anyone; crouching, turned. */
    static boolean sideboard(ServerPlayer p,ServerLevel l,BlockPos pos,BlockState s){
        if(p.isShiftKeyDown()){l.setBlock(pos,s.setValue(FoldBlock.FACING,s.getValue(FoldBlock.FACING).getClockWise()),FLAGS);l.playSound(null,pos,SoundEvents.WOOD_HIT,SoundSource.BLOCKS,.7F,.7F);return true;}
        double dx=p.getX()-(pos.getX()+.5),dz=p.getZ()-(pos.getZ()+.5);
        var toward=Math.abs(dx)>=Math.abs(dz)?(dx>0?Direction.EAST:Direction.WEST):(dz>0?Direction.SOUTH:Direction.NORTH);
        var to=pos.relative(toward);
        if(!l.getBlockState(to).isAir()||!l.getBlockState(to.below()).isFaceSturdy(l,to.below(),Direction.UP)||!l.getBlockState(to.above()).isAir()){PuzzleRooms.say(p,"It won't go that way.");return true;}
        if(!l.getEntitiesOfClass(LivingEntity.class,new AABB(to).deflate(.05)).isEmpty()){PuzzleRooms.say(p,"There is no room to pull it to.");return true;}
        l.setBlock(pos,Blocks.AIR.defaultBlockState(),FLAGS);l.setBlock(to,s,FLAGS);
        l.playSound(null,to,SoundEvents.WOOD_STEP,SoundSource.BLOCKS,1F,.5F);l.playSound(null,to,SoundEvents.WOOD_PLACE,SoundSource.BLOCKS,.6F,.55F);
        if(l.getBlockState(to.below()).is(PuzzleRegistry.foldMaterial("marks")))PuzzleRooms.say(p,"It settles onto the worn marks where its feet stood for years.");
        return true;
    }

    // ------------------------------------------------------------------ solving
    /** Whether anyone in the room has reached its far door: in front of it in the early room, past both partitions in the deep one. */
    static boolean reached(ServerLevel l,BlockPos b,LabyrinthPlace place){
        for(var p:l.players()){
            if(!p.isAlive()||p.isSpectator()||PuzzleRooms.at(p)!=place)continue;
            var rel=p.blockPosition().subtract(b);
            if(place==EARLY?rel.getX()==EARLY_DOOR_FRONT.getX()&&rel.getZ()==EARLY_DOOR_FRONT.getZ()&&at(l,b,EARLY_P)!=EARLY_P.start():rel.getZ()<=FAR_END&&Math.abs(rel.getX())<=2)return true;
        }
        return false;
    }
    /** The partitions fold back on their hinges: home, where their tracks are worn, if nobody is in the way there, else where they stand. */
    static void fold(ServerLevel l,BlockPos b,LabyrinthPlace place){
        for(var q:partitions(place)){
            int z=at(l,b,q);if(z==Integer.MIN_VALUE)continue;
            int to=q.home();
            boolean free=to!=z&&l.getBlockState(b.offset(q.x0(),0,to)).isAir()&&l.getBlockState(b.offset(q.x1(),0,to)).isAir()&&l.getBlockState(b.offset(q.x0(),1,to)).isAir()&&l.getBlockState(b.offset(q.x1(),1,to)).isAir()
                    &&!occupied(l,b,q.x0(),q.x0(),to)&&!occupied(l,b,q.x1(),q.x1(),to);
            clear(l,b,q,z);foldAway(l,b,q,free||to==z?to:z);
            l.playSound(null,b.offset((q.x0()+q.x1())/2,1,z),SoundEvents.WOODEN_DOOR_CLOSE,SoundSource.BLOCKS,.8F,.55F);
        }
    }
    /** The deep room laid for dinner where its floor shows the table and chairs stood, and the sideboard back on its marks. Places what it can; the rest when nobody is in the way. */
    static boolean lay(ServerLevel l,BlockPos b){
        boolean done=true;
        java.util.Set<Integer> folded=new java.util.HashSet<>();
        for(var q:partitions(DEEP))for(int z=q.north();z<=q.south();z++)if(FoldBlock.is(l.getBlockState(b.offset(q.x0(),0,z)),FOLDED))folded.add(z);
        for(int z=TABLE_TO;z<=TABLE_FROM;z++)done&=put(l,b,new BlockPos(0,0,z),FoldBlock.of(TABLE,Direction.NORTH));
        for(int z:CHAIR_ROWS){if(folded.contains(z))continue;done&=put(l,b,new BlockPos(-1,0,z),FoldBlock.of(CHAIR,Direction.EAST));done&=put(l,b,new BlockPos(1,0,z),FoldBlock.of(CHAIR,Direction.WEST));}
        if(!FoldBlock.is(l.getBlockState(b.offset(RECESS)),SIDEBOARD)){
            if(l.getBlockState(b.offset(RECESS)).isAir()&&!occupied(l,b,RECESS.getX(),RECESS.getX(),RECESS.getZ())){
                for(int x=-8;x<=2;x++)for(int z=-25;z<=-1;z++){var at=b.offset(x,0,z);if(FoldBlock.is(l.getBlockState(at),SIDEBOARD))l.setBlock(at,Blocks.AIR.defaultBlockState(),FLAGS);}
                set(l,b,RECESS,FoldBlock.of(SIDEBOARD,Direction.EAST));
            }else done=false;
        }
        return done;
    }
    static boolean put(ServerLevel l,BlockPos b,BlockPos rel,BlockState s){
        var at=b.offset(rel);var now=l.getBlockState(at);if(now.equals(s))return true;
        if(!now.isAir()||!l.getEntitiesOfClass(LivingEntity.class,new AABB(at).deflate(.05),e->e.isAlive()).isEmpty())return false;
        l.setBlock(at,s,FLAGS);return true;
    }
    static void check(ServerLevel l,BlockPos b,LabyrinthPlace place){
        var data=LabyrinthData.get(l.getServer());
        if(!PuzzleRooms.solved(data,place)){
            if(!reached(l,b,place))return;
            PuzzleRooms.solve(l,place,place==EARLY?"The door was behind the partition all along. Behind you the panels fold back on their hinges into the corners, and it is one room again."
                    :"Both partitions are behind you. Down the length of the room the panels fold back into their corners, and the room comes back the way its floor remembers it.");
            fold(l,b,place);
        }
        if(place==DEEP){var all=data.state(STATE);if(!all.getBoolean("Laid")&&lay(l,b)){all.putBoolean("Laid",true);data.setState(STATE,all);}}
    }

    // ------------------------------------------------------------------ hands on
    private static CompoundTag own(ServerLevel l,ServerPlayer p){return LabyrinthData.get(l.getServer()).stateEntry(STATE,p.getUUID().toString());}
    private static void save(ServerLevel l,ServerPlayer p,CompoundTag own){var data=LabyrinthData.get(l.getServer());var all=data.state(STATE);all.put(p.getUUID().toString(),own);data.setState(STATE,all);}
    public static boolean use(ServerPlayer p,BlockPos pos,BlockState s){
        if(p.isSpectator())return false;
        var origin=HouseSavedData.get(p.server).houseOrigin();if(origin==null)return false;
        var place=LabyrinthPlaces.placeAt(origin,pos);if(place!=EARLY&&place!=DEEP)return false;
        var b=LabyrinthPlaces.base(origin,place);var l=p.serverLevel();var rel=pos.subtract(b);
        switch(s.getValue(FoldBlock.KIND)){
            case PANEL,HANGER->{var q=partitionAt(place,rel);return q!=null&&push(p,l,b,place,q);}
            case SIDEBOARD->{return sideboard(p,l,pos,s);}
            case HATCH->{int stage=s.getValue(FoldBlock.STAGE);l.setBlock(pos,s.setValue(FoldBlock.STAGE,stage==0?1:0),FLAGS);l.playSound(null,pos,SoundEvents.WOODEN_TRAPDOOR_OPEN,SoundSource.BLOCKS,.7F,stage==0?1F:.8F);
                PuzzleRooms.say(p,stage==0?"You slide the hatch's shutter up. Through it, the pantry's shelves.":"You slide the hatch's shutter down.");}
            case FOLDED->PuzzleRooms.say(p,"A partition, folded back on its hinges into the corner.");
            case TABLE->PuzzleRooms.say(p,solved(l,place)?"Laid for dinner, as it used to be.":"A dining table.");
            case CHAIR->PuzzleRooms.say(p,"A dining chair.");
        }
        return true;
    }

    // ------------------------------------------------------------------ the clock
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();if(server.getTickCount()%10!=3)return;var origin=HouseSavedData.get(server).houseOrigin();var l=server.getLevel(HouseDimensions.INTERIOR);
        if(origin==null||l==null)return;
        for(var place:new LabyrinthPlace[]{EARLY,DEEP}){
            if(LabyrinthData.get(server).door(place.entryDoorId())==null)continue;
            for(var p:l.players())if(PuzzleRooms.at(p)==place){var b=LabyrinthPlaces.base(origin,place);if(b!=null&&l.isLoaded(b))check(l,b,place);break;}
        }
    }
    /** For tests: where each partition of a room stands. */
    public static int at(ServerLevel l,LabyrinthPlace place,int index){var b=PuzzleRooms.base(l.getServer(),place);return b==null?Integer.MIN_VALUE:at(l,b,partitions(place).get(index));}
}
