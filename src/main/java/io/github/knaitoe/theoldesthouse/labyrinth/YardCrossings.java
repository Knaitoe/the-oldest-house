package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.322, the yard's exceptional crossings (docs/YARD_CROSSINGS_0_4_322.md; the owner's "The House Has a Yard", step 4: "Author the
 * exceptional crossings. Test the perimeter loop, deep ceiling, and false porch bearing as discrete events."). Each is dealt to a reader
 * once, and only after the ordinary yard has taught them what it breaks.
 * <ul>
 * <li><b>The long drive</b> (the perimeter loop): the turn-around exactly as the reader learned it, except that the road east no longer
 * ends at a creek and the gate in the back fence is gone. Either way out bends long and shallow through the cedars, past the property's
 * old boundary fence, a dead tree, a streetlight at the end of a straight and a set of tire tracks that stop in clear mud, and comes back
 * in by the other way, to the same wagon with its door ajar and the reader's own prints in the mud beside it. While they were on the far
 * side of the loop the garage door went up; the way on is through the garage.</li>
 * <li><b>The porch turn</b> (the false porch bearing): a back porch with its bulb, and a footpath walking away from it between hedges, the
 * bulb behind you down the straight. The path bends into a long half-turn under knitted branches with no sky to steer by, and comes out
 * onto a straight with the porch and its bulb ahead. The back door ahead is the way on. Somebody shuts it as you come into sight.</li>
 * <li><b>The joists</b> (the deep ceiling): a muddy ditch between the cedars and a bank of roots, a retaining face of dark timbers, and
 * the state route just above you on top of it, close enough to hear, out of reach. Over all of it, where the sky should be, are floor
 * joists and copper pipes. A passing car's headlights run along the route and catch them, high up. The stand of cedars ends at a face
 * of the same dark joists; the door in it is the way on.</li>
 * </ul>
 * Nothing here hurts, counts toward an ending, or needs solving: the way on is always visible once the crossing has done its one thing.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class YardCrossings {
    private YardCrossings(){}
    public static final String STATE="yard_crossings_0322";
    public static final LabyrinthPlace LONG_DRIVE=LabyrinthPlace.YARD_LONG_DRIVE,PORCH_TURN=LabyrinthPlace.YARD_PORCH_TURN,JOISTS=LabyrinthPlace.YARD_JOISTS;
    public static final List<LabyrinthPlace> PLACES=List.of(LONG_DRIVE,PORCH_TURN,JOISTS);
    /** The ordinary yard: crossings with the porch, the bulb and the drive, which teach the bearings the exceptional ones break. */
    public static final List<LabyrinthPlace> ORDINARY=List.of(LabyrinthPlace.YARD_TURNAROUND,LabyrinthPlace.YARD_REUNION,LabyrinthPlace.YARD_DITCH,
            LabyrinthPlace.YARD_CLOTHESLINE,LabyrinthPlace.YARD_SURVEY,LabyrinthPlace.YARD_FLOOD);
    public static final int LONG_WEIGHT=2,PORCH_WEIGHT=2,JOIST_WEIGHT=3,RESET=200;
    private static final int FLAGS=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    public static boolean is(LabyrinthPlace p){return PLACES.contains(p);}
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){YardRooms.set(l,b,x,y,z,s);}
    private static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){YardRooms.set(l,b,at,s);}
    private static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){YardRooms.box(l,b,x0,y0,z0,x1,y1,z1,s);}
    private static BlockState air(){return YardRooms.air();}
    private static BlockState block(Block b){return b.defaultBlockState();}
    private static BlockState mud(){return YardBlock.of(YardBlock.Kind.PRINT,Direction.NORTH,0);}

    // ------------------------------------------------------------------ dealing
    /**
     * Once each, and only to a reader who has learned what the crossing breaks: the long drive after the turn-around (or the apron of a
     * reunion), the porch turn after two of the ordinary yard's crossings, the joists in the deep halls after any of them. Never in the
     * shared cache: these are a reader's own discoveries.
     */
    public static int weight(LabyrinthData data,UUID player,LabyrinthPlace place,int depth){
        var seen=data.visited(player);if(seen.contains(place.id()))return 0;
        int yard=0;for(var p:ORDINARY)if(seen.contains(p.id()))yard++;
        boolean apron=seen.contains(LabyrinthPlace.YARD_TURNAROUND.id())||seen.contains(LabyrinthPlace.YARD_REUNION.id());
        return switch(place){
            case YARD_LONG_DRIVE->depth<LabyrinthPacing.STORY_DEPTH||!apron?0:LONG_WEIGHT;
            case YARD_PORCH_TURN->depth<LabyrinthPacing.STORY_DEPTH||yard<2?0:PORCH_WEIGHT;
            case YARD_JOISTS->depth<LabyrinthPacing.DEEP_DEPTH||depth>=DarkHalls.ONLY_DEPTH||yard<1?0:JOIST_WEIGHT;
            default->0;
        };
    }

    // ------------------------------------------------------------------ state
    public static CompoundTag state(ServerLevel l,LabyrinthPlace p){return LabyrinthData.get(l.getServer()).stateEntry(STATE,p.id());}
    static void save(ServerLevel l,LabyrinthPlace p,CompoundTag s){LabyrinthData.get(l.getServer()).setStateEntry(STATE,p.id(),s);}
    public static CompoundTag reader(ServerLevel l,ServerPlayer p,LabyrinthPlace place){return LabyrinthData.get(l.getServer()).stateEntry(STATE,"reader/"+place.id()+"/"+p.getUUID());}
    static void saveReader(ServerLevel l,ServerPlayer p,LabyrinthPlace place,CompoundTag own){own.putBoolean("Here",true);LabyrinthData.get(l.getServer()).setStateEntry(STATE,"reader/"+place.id()+"/"+p.getUUID(),own);}
    /** A fresh arrival starts that reader's crossing over: the loop walked again, the turn taken again. */
    public static void arrived(ServerPlayer p,LabyrinthPlace place){
        if(!is(place)||p.isSpectator())return;
        LabyrinthData.get(p.server).setStateEntry(STATE,"reader/"+place.id()+"/"+p.getUUID(),new CompoundTag());
    }

    // ------------------------------------------------------------------ building
    public static void build(ServerLevel l,BlockPos b,LabyrinthPlace place){
        switch(place){
            case YARD_LONG_DRIVE->longDrive(l,b);
            case YARD_PORCH_TURN->porchTurn(l,b);
            case YARD_JOISTS->joists(l,b);
            default->{return;}
        }
        var s=new CompoundTag();s.putBoolean("Built",true);save(l,place,s);
    }
    /** A back porch on the House's clapboard, as the turn-around has it: two dark windows, the eaves, a deck, a step and the bulb. */
    static void porch(ServerLevel l,BlockPos b,int cx){
        YardRooms.clear(l,b,cx-9,-6,cx+9,0,6);
        box(l,b,cx-9,0,1,cx+9,8,2,YardRooms.mat("clapboard"));
        for(int x:new int[]{cx-5,cx+5})box(l,b,x-1,1,1,x,2,1,block(Blocks.BLACK_STAINED_GLASS));
        for(int x=cx-10;x<=cx+10;x++)set(l,b,x,9,1,block(Blocks.DARK_OAK_SLAB));
        box(l,b,cx-3,-1,-2,cx+3,-1,0,block(Blocks.SPRUCE_PLANKS));
        for(int x:new int[]{cx-3,cx+3})box(l,b,x,0,-2,x,2,-2,block(Blocks.SPRUCE_FENCE));
        box(l,b,cx-3,3,-2,cx+3,3,0,block(Blocks.SPRUCE_SLAB));
        for(int x=cx-1;x<=cx+1;x++)set(l,b,x,-1,-3,block(Blocks.SPRUCE_STAIRS).setValue(StairBlock.FACING,Direction.SOUTH).setValue(StairBlock.HALF,Half.TOP));
        set(l,b,cx+1,2,0,YardBlock.of(YardBlock.Kind.BULB,Direction.NORTH,0));
    }

    // ------------------------------------------------------------------ the long drive
    /** The loop's road, centre line: from the end of the path behind the garage, round the property, to where the creek was. */
    public static final int[][] LOOP={{-7,-34},{-7,-50},{0,-62},{16,-67},{36,-66},{52,-58},{59,-44},{58,-28},{50,-18},{40,-14}};
    /** The garage door's middle; the way on, in the garage's back wall; the landmarks along the loop. */
    public static final BlockPos GARAGE_DOOR=new BlockPos(-8,0,-19),LONG_EXIT=new BlockPos(-11,0,-22),STREETLIGHT=new BlockPos(37,0,-68),
            DEAD_TREE=new BlockPos(25,0,-69),STUMP=new BlockPos(-5,0,-58),TURNOUT=new BlockPos(-4,0,-57);
    /** Past this from the middle of the apron a reader is round the far side of the loop. */
    public static final int FAR=55,FENCE_Z=-52,TRACKS_END=-39;
    /** The soft mud, x0 z0 x1 z1: in the squeeze beside the wagon's door, across the mouth of the road, and where the tire tracks stop. */
    public static final int[][] MUD={{-7,-21,-7,-16},{8,-15,10,-13},{57,-42,60,TRACKS_END-1}};
    /** How far a cell is from the loop's centre line, and the segment it is nearest. */
    static double[] loopDistance(int x,int z){
        double best=99,seg=0;
        for(int i=0;i+1<LOOP.length;i++){
            double ax=LOOP[i][0]+.5,az=LOOP[i][1]+.5,dx=LOOP[i+1][0]-LOOP[i][0],dz=LOOP[i+1][1]-LOOP[i][1],len=dx*dx+dz*dz;
            double t=Math.max(0,Math.min(1,((x+.5-ax)*dx+(z+.5-az)*dz)/len)),d=Math.hypot(x+.5-ax-t*dx,z+.5-az-t*dz);
            if(d<best){best=d;seg=i;}
        }
        return new double[]{best,seg};
    }
    public static boolean onLoop(int x,int z){return loopDistance(x,z)[0]<=1.6;}
    static void longDrive(ServerLevel l,BlockPos b){
        var rnd=new Random(b.asLong()^0x5EED_0322L);
        // The ground and the cedars past what the turn-around's plan reaches; then the turn-around itself, built by its own builder.
        box(l,b,-20,-3,-72,64,-2,4,block(Blocks.DIRT));box(l,b,-20,-1,-72,64,-1,4,block(Blocks.GRASS_BLOCK));
        box(l,b,-20,0,-72,64,14,4,air());YardRooms.cedars(l,b,rnd,-20,-72,64,4);
        YardRooms.build(l,b,LONG_DRIVE);
        // No swollen creek: the bed is filled and grown over, and the road goes on.
        for(int x=YardRooms.CREEK_X0;x<=YardRooms.CREEK_X1;x++)for(int z=YardRooms.ROAD_Z-3;z<=YardRooms.ROAD_Z+3;z++){
            box(l,b,x,-4,z,x,-2,z,block(Blocks.DIRT));set(l,b,x,-1,z,block(Blocks.GRASS_BLOCK));
        }
        YardRooms.cedars(l,b,rnd,YardRooms.CREEK_X0,YardRooms.ROAD_Z-3,YardRooms.CREEK_X1,YardRooms.ROAD_Z+3);
        // No gate in the back fence: the path goes on through the tree line.
        box(l,b,YardRooms.SQUEEZE_X-2,0,YardRooms.FAR_GATE.getZ(),YardRooms.SQUEEZE_X+2,2,YardRooms.FAR_GATE.getZ(),YardRooms.leaves());
        // The loop: tire ruts of wet gravel with grass between, until the tracks stop in the clear mud; after it, a grassed track.
        for(int x=-20;x<=64;x++)for(int z=-72;z<=-12;z++){
            var d=loopDistance(x,z);if(d[0]>1.6)continue;
            boolean rutted=d[1]<6||d[1]==6&&z<TRACKS_END;
            box(l,b,x,0,z,x,YardRooms.SKY_CLEAR,z,air());
            set(l,b,x,-1,z,d[0]<=.7?(rutted?block(Blocks.GRASS_BLOCK):block(Blocks.COARSE_DIRT)):rutted?YardRooms.mat("wet_gravel"):block(Blocks.GRASS_BLOCK));
            // Branches knit across the road where it leaves the property and again round the far bend.
            if((d[1]==2||d[1]==5)&&rnd.nextInt(10)<8)box(l,b,x,4,z,x,4+rnd.nextInt(2),z,YardRooms.leaves());
            else if(d[0]>1&&rnd.nextInt(10)<4)for(int y=3+rnd.nextInt(2);y<=5+rnd.nextInt(3);y++)set(l,b,x,y,z,YardRooms.leaves());
        }
        // The turnout, where somebody once pulled over: a wider patch of gravel and a sawn stump.
        for(int x=TURNOUT.getX()-3;x<=TURNOUT.getX()+3;x++)for(int z=TURNOUT.getZ()-3;z<=TURNOUT.getZ()+3;z++){
            if(Math.hypot(x-TURNOUT.getX(),z-TURNOUT.getZ())>3.2)continue;
            box(l,b,x,0,z,x,YardRooms.SKY_CLEAR,z,air());set(l,b,x,-1,z,block(Blocks.GRAVEL));
        }
        set(l,b,STUMP,block(Blocks.STRIPPED_SPRUCE_LOG));
        // The property's old boundary fence, running off into the trees on both sides each time the road crosses its line.
        for(int x=-20;x<=64;x++){
            double d=loopDistance(x,FENCE_Z)[0];if(d<=1.6||d>4.5)continue;
            box(l,b,x,0,FENCE_Z,x,2,FENCE_Z,air());
            set(l,b,x,0,FENCE_Z,block(Blocks.OAK_FENCE).setValue(FenceBlock.EAST,loopDistance(x+1,FENCE_Z)[0]>1.6&&loopDistance(x+1,FENCE_Z)[0]<=4.5).setValue(FenceBlock.WEST,loopDistance(x-1,FENCE_Z)[0]>1.6&&loopDistance(x-1,FENCE_Z)[0]<=4.5));
        }
        // The dead tree on the long straight, where the crow is.
        clearAround(l,b,DEAD_TREE,1);
        box(l,b,DEAD_TREE.getX(),0,DEAD_TREE.getZ(),DEAD_TREE.getX(),6,DEAD_TREE.getZ(),block(Blocks.STRIPPED_SPRUCE_LOG));
        set(l,b,DEAD_TREE.getX()+1,4,DEAD_TREE.getZ(),block(Blocks.STRIPPED_SPRUCE_LOG).setValue(RotatedPillarBlock.AXIS,Direction.Axis.X));
        set(l,b,DEAD_TREE.getX(),5,DEAD_TREE.getZ()+1,block(Blocks.STRIPPED_SPRUCE_LOG).setValue(RotatedPillarBlock.AXIS,Direction.Axis.Z));
        // The streetlight at the end of the straight, its lamp out over the road.
        clearAround(l,b,STREETLIGHT,1);
        box(l,b,STREETLIGHT.getX(),0,STREETLIGHT.getZ(),STREETLIGHT.getX(),5,STREETLIGHT.getZ(),block(Blocks.DARK_OAK_FENCE));
        set(l,b,STREETLIGHT.getX(),5,STREETLIGHT.getZ(),block(Blocks.DARK_OAK_FENCE).setValue(FenceBlock.SOUTH,true));
        set(l,b,STREETLIGHT.getX(),5,STREETLIGHT.getZ()+1,block(Blocks.DARK_OAK_FENCE).setValue(FenceBlock.NORTH,true));
        set(l,b,STREETLIGHT.getX(),4,STREETLIGHT.getZ()+1,block(Blocks.LANTERN).setValue(LanternBlock.HANGING,true));
        // A fallen run of fence on the inside of the far bend.
        for(int x=41;x<=44;x++){box(l,b,x,0,-61,x,2,-61,air());set(l,b,x,0,-61,block(Blocks.STRIPPED_OAK_LOG).setValue(RotatedPillarBlock.AXIS,Direction.Axis.X));}
        box(l,b,45,0,-60,45,2,-60,air());set(l,b,45,0,-60,block(Blocks.OAK_FENCE));
        // The mud, clear until someone walks through it.
        for(var m:MUD)for(int x=m[0];x<=m[2];x++)for(int z=m[1];z<=m[3];z++)if(m!=MUD[2]||onLoop(x,z))set(l,b,x,-1,z,mud());
        // The garage: shut, with a bench and a bulb inside and the back door in its north wall.
        garage(l,b,false);
        box(l,b,-13,0,-17,-13,0,-16,block(Blocks.SPRUCE_SLAB).setValue(SlabBlock.TYPE,SlabType.TOP));
        set(l,b,-11,3,-19,YardBlock.of(YardBlock.Kind.BULB,Direction.NORTH,0));
        YardRooms.doors(l,b,LONG_DRIVE);
    }
    private static void clearAround(ServerLevel l,BlockPos b,BlockPos at,int r){box(l,b,at.getX()-r,0,at.getZ()-r,at.getX()+r,7,at.getZ()+r,air());}
    /** The garage door down (planks above it) or up (the door rolled into the header and the opening clear). */
    static void garage(ServerLevel l,BlockPos b,boolean open){
        for(int z=GARAGE_DOOR.getZ()-1;z<=GARAGE_DOOR.getZ()+1;z++){
            for(int y=0;y<=2;y++)set(l,b,GARAGE_DOOR.getX(),y,z,open?air():YardRooms.mat("garage_door"));
            set(l,b,GARAGE_DOOR.getX(),3,z,open?YardRooms.mat("garage_door"):block(Blocks.DARK_OAK_PLANKS));
        }
        l.playSound(null,b.offset(GARAGE_DOOR).above(),open?SoundEvents.WOODEN_TRAPDOOR_OPEN:SoundEvents.WOODEN_TRAPDOOR_CLOSE,SoundSource.BLOCKS,.4F,.6F);
    }
    public static boolean garageOpen(ServerLevel l,BlockPos b){return l.getBlockState(b.offset(GARAGE_DOOR)).isAir();}
    private static void clearPrints(ServerLevel l,BlockPos b){
        for(var m:MUD)for(int x=m[0];x<=m[2];x++)for(int z=m[1];z<=m[3];z++){var at=b.offset(x,-1,z);if(YardBlock.is(l.getBlockState(at),YardBlock.Kind.PRINT))l.setBlock(at,mud(),FLAGS);}
        for(int x=-2;x<=2;x++)for(int z=-44;z<=-2;z++){var at=b.offset(x,-1,z);if(YardBlock.is(l.getBlockState(at),YardBlock.Kind.PRINT))l.setBlock(at,mud(),FLAGS);}
    }
    static void longTick(ServerLevel l,BlockPos b,CompoundTag s,List<ServerPlayer> here,long now){
        boolean dirty=false;double ax=b.getX()+YardRooms.APRON.getX()+.5,az=b.getZ()+YardRooms.APRON.getZ()+.5;
        for(var p:here){
            double d=Math.hypot(p.getX()-ax,p.getZ()-az);var own=reader(l,p,LONG_DRIVE);boolean changed=false;
            if(d>FAR){
                if(!own.getBoolean("Far")){own.putBoolean("Far",true);changed=true;}
                if(!s.getBoolean("Due")){s.putBoolean("Due",true);dirty=true;}
            }else if(own.getBoolean("Far")&&!own.getBoolean("Looped")&&d<YardRooms.APRON_R+1.5){
                own.putBoolean("Looped",true);changed=true;PuzzleRooms.say(p,"The wagon again. Its door still ajar.");
            }
            if(changed)saveReader(l,p,LONG_DRIVE,own);
        }
        // The garage door goes up while the reader is round the far side, and only while nobody is looking at it.
        var door=b.offset(GARAGE_DOOR).above();
        if(s.getBoolean("Due")&&!s.getBoolean("Open")&&!watched(l,door,48)){garage(l,b,true);s.putBoolean("Open",true);dirty=true;}
        // Empty a while, it comes down again and the mud smooths over, unseen, for the next reader's discovery.
        if(here.isEmpty()&&(s.getBoolean("Due")||s.getBoolean("Open")||s.getBoolean("Trodden"))){
            if(!s.contains("Empty")){s.putLong("Empty",now);dirty=true;}
            else if(now-s.getLong("Empty")>=RESET&&!watched(l,door,64)){if(s.getBoolean("Open"))garage(l,b,false);clearPrints(l,b);s.remove("Due");s.remove("Open");s.remove("Trodden");s.remove("Empty");dirty=true;}
        }else if(!here.isEmpty()&&s.contains("Empty")){s.remove("Empty");dirty=true;}
        if(dirty)save(l,LONG_DRIVE,s);
        if(here.isEmpty())return;
        if(now%40==0)l.playSound(null,b.offset(YardRooms.NOSE_AT),PuzzleRegistry.YARD_IDLE.get(),SoundSource.AMBIENT,1F,1F);
        if(now%420==60)l.playSound(null,b.offset(56,6,-66),PuzzleRegistry.YARD_TRAFFIC.get(),SoundSource.AMBIENT,1.2F,.95F);
        if(now%640==200)l.playSound(null,b.offset(DEAD_TREE).above(6),PuzzleRegistry.YARD_CROW.get(),SoundSource.AMBIENT,.9F,1F);
        if(now%260==130)l.playSound(null,b.offset(0,4,-62),PuzzleRegistry.YARD_WIND.get(),SoundSource.AMBIENT,.6F,1F);
    }

    // ------------------------------------------------------------------ the porch turn
    public static final int PORCH_B_X=24,TURN_Z=-16,TURN_R=12;
    /** How far a cell is from the path: two straights north from the porches, joined by a half-turn of radius twelve. */
    public static double pathDistance(int x,int z){
        double d=99;
        if(z>=TURN_Z&&z<=-4)d=Math.min(Math.abs(x),Math.abs(x-PORCH_B_X));
        if(z<=TURN_Z)d=Math.min(d,Math.abs(Math.hypot(x-PORCH_B_X/2.0,z-TURN_Z)-TURN_R));
        return d;
    }
    static void porchTurn(ServerLevel l,BlockPos b){
        var rnd=new Random(b.asLong()^0x7E44_0322L);
        box(l,b,-12,-3,-36,36,-2,4,block(Blocks.DIRT));box(l,b,-12,-1,-36,36,-1,4,block(Blocks.GRASS_BLOCK));
        box(l,b,-12,0,-36,36,14,4,air());YardRooms.cedars(l,b,rnd,-12,-36,36,4);
        porch(l,b,0);porch(l,b,PORCH_B_X);
        // The footpath: worn down the middle; open to the fog on the straights, the branches knit over the turn so nothing steers by the sky.
        for(int x=-12;x<=36;x++)for(int z=-36;z<=-4;z++){
            double d=pathDistance(x,z);if(d>1.6)continue;
            box(l,b,x,0,z,x,YardRooms.SKY_CLEAR,z,air());
            set(l,b,x,-1,z,d<=.8?block(Blocks.COARSE_DIRT):block(Blocks.GRASS_BLOCK));
            if(z<TURN_Z){if(rnd.nextInt(10)<9)box(l,b,x,4,z,x,5+rnd.nextInt(2),z,YardRooms.leaves());}
            else if(d>1&&rnd.nextInt(10)<3)set(l,b,x,4+rnd.nextInt(2),z,YardRooms.leaves());
        }
        // The same things on the same hand on both straights: a trough on the left, a stack of split logs on the right.
        for(int[] at:new int[][]{{-2,-10},{PORCH_B_X+2,-10}}){box(l,b,at[0],0,at[1],at[0],3,at[1],air());set(l,b,at[0],0,at[1],block(Blocks.CAULDRON));}
        for(int[] at:new int[][]{{2,-7},{PORCH_B_X-2,-7}}){box(l,b,at[0],0,at[1],at[0],3,at[1],air());box(l,b,at[0],0,at[1],at[0],1,at[1],block(Blocks.SPRUCE_LOG).setValue(RotatedPillarBlock.AXIS,Direction.Axis.Z));}
        YardRooms.doors(l,b,PORCH_TURN);
    }
    static void porchTick(ServerLevel l,BlockPos b,CompoundTag s,List<ServerPlayer> here,long now){
        for(var p:here){
            double rx=p.getX()-b.getX()-.5,rz=p.getZ()-b.getZ()-.5;var own=reader(l,p,PORCH_TURN);boolean changed=false;
            if(rz<TURN_Z-TURN_R+1.5&&!own.getBoolean("Apex")){own.putBoolean("Apex",true);changed=true;}
            if(own.getBoolean("Apex")&&!own.getBoolean("Turned")&&Math.abs(rx-PORCH_B_X)<2.5&&rz>TURN_Z+2&&rz<-3){
                // Out of the turn, the porch ahead, and somebody going in by its back door.
                own.putBoolean("Turned",true);changed=true;
                l.playSound(null,b.offset(PORCH_B_X,1,1),SoundEvents.WOODEN_DOOR_CLOSE,SoundSource.BLOCKS,.8F,.9F);
            }
            if(changed)saveReader(l,p,PORCH_TURN,own);
        }
        if(!here.isEmpty()&&now%220==0)l.playSound(null,b.offset(PORCH_B_X/2,4,TURN_Z-TURN_R),PuzzleRegistry.YARD_WIND.get(),SoundSource.AMBIENT,.8F,1F);
    }

    // ------------------------------------------------------------------ the joists
    /** The route's surface, its middle where the headlights run, and the height they light the joists from. */
    public static final int ROUTE_Y=4,ROUTE_X=9,JOIST_Y=16,FLOOR_Y=17,BEAM_Y=14,SWEEP_SOUTH=-2,SWEEP_NORTH=-44,FACE_Z=-45;
    public static final BlockPos JOIST_EXIT=new BlockPos(0,0,-45);
    public static int joistSweep=-1000;
    public static long nextJoistCar=-1;
    public static void clearAll(){joistSweep=-1000;nextJoistCar=-1;}
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){clearAll();}
    static void joists(ServerLevel l,BlockPos b){
        var rnd=new Random(b.asLong()^0x1015_0322L);
        box(l,b,-16,-3,-48,16,-2,0,block(Blocks.DIRT));box(l,b,-16,-1,-48,16,-1,0,block(Blocks.GRASS_BLOCK));
        box(l,b,-16,0,-48,16,18,0,air());
        // The cedars on the west bank, and past the route on the east.
        YardRooms.cedars(l,b,rnd,-16,-44,-3,0);YardRooms.cedars(l,b,rnd,13,-44,16,0);
        for(int x=13;x<=16;x++)for(int z=-44;z<=0;z++)box(l,b,x,0,z,x,ROUTE_Y,z,block(Blocks.DIRT));
        // The ditch: water over the boots in two stretches, soft mud in two, a verge either side.
        for(int z=-1;z>=-44;z--){
            boolean wet=z>=-12||z<=-21&&z>=-30;
            for(int x=-1;x<=1;x++)set(l,b,x,-1,z,z>=-2?block(Blocks.COARSE_DIRT):wet?block(Blocks.STONE_SLAB).setValue(SlabBlock.TYPE,SlabType.BOTTOM).setValue(SlabBlock.WATERLOGGED,true):mud());
            set(l,b,-2,-1,z,block(Blocks.COARSE_DIRT));set(l,b,2,-1,z,block(Blocks.ROOTED_DIRT));
            // The bank: mud two high at its foot, roots above, then the retaining face, then the route on top of it.
            box(l,b,3,0,z,3,1,z,block(Blocks.MUD));box(l,b,4,0,z,4,2,z,block(Blocks.ROOTED_DIRT));
            set(l,b,3,2,z,block(Blocks.MUDDY_MANGROVE_ROOTS));set(l,b,4,3,z,rnd.nextInt(3)==0?block(Blocks.MANGROVE_ROOTS):block(Blocks.MUDDY_MANGROVE_ROOTS));
            box(l,b,5,0,z,5,ROUTE_Y,z,block(Blocks.DARK_OAK_LOG).setValue(RotatedPillarBlock.AXIS,Direction.Axis.Z));
            set(l,b,6,ROUTE_Y,z,block(Blocks.GRAVEL));set(l,b,12,ROUTE_Y,z,block(Blocks.GRAVEL));
            for(int x=7;x<=11;x++)set(l,b,x,ROUTE_Y,z,x==ROUTE_X&&Math.floorMod(z,4)<2?block(Blocks.WHITE_CONCRETE):block(Blocks.BLACK_CONCRETE));
            box(l,b,6,0,z,12,ROUTE_Y-1,z,block(Blocks.DIRT));
            set(l,b,6,ROUTE_Y+1,z,block(Blocks.DARK_OAK_FENCE).setValue(FenceBlock.NORTH,z>-44).setValue(FenceBlock.SOUTH,z<-1));
        }
        // Streetlights on the far shoulder, their lamps out over the route.
        for(int z:new int[]{-8,-23,-38}){
            box(l,b,12,ROUTE_Y+1,z,12,ROUTE_Y+5,z,block(Blocks.DARK_OAK_FENCE));
            set(l,b,12,ROUTE_Y+5,z,block(Blocks.DARK_OAK_FENCE).setValue(FenceBlock.WEST,true));set(l,b,11,ROUTE_Y+5,z,block(Blocks.DARK_OAK_FENCE).setValue(FenceBlock.EAST,true));
            set(l,b,11,ROUTE_Y+4,z,block(Blocks.LANTERN).setValue(LanternBlock.HANGING,true));
        }
        // Dim light down the ditch, so the way is never black.
        for(int z=-6;z>=-38;z-=8)set(l,b,0,3,z,block(Blocks.LIGHT).setValue(LightBlock.LEVEL,7));
        // Where the sky should be: the subfloor, the joists every third block, and the copper runs.
        box(l,b,-16,FLOOR_Y,-48,16,FLOOR_Y,0,block(Blocks.SPRUCE_PLANKS));
        for(int z=0;z>=-48;z-=3)box(l,b,-16,JOIST_Y,z,16,JOIST_Y,z,block(Blocks.DARK_OAK_LOG).setValue(RotatedPillarBlock.AXIS,Direction.Axis.X));
        for(int z=-1;z>=-47;z--)set(l,b,-4,JOIST_Y-1,z,block(Blocks.LIGHTNING_ROD).setValue(LightningRodBlock.FACING,Direction.NORTH));
        for(int z:new int[]{-11,-26,-41})for(int x=-16;x<=16;x++)if(x!=-4)set(l,b,x,JOIST_Y-1,z,block(Blocks.LIGHTNING_ROD).setValue(LightningRodBlock.FACING,Direction.EAST));
        // The stand of cedars ends at a face of the same dark joists, floor to ceiling; the door in it is the way on.
        box(l,b,-16,0,-48,16,JOIST_Y,FACE_Z,block(Blocks.DARK_OAK_LOG).setValue(RotatedPillarBlock.AXIS,Direction.Axis.X));
        set(l,b,1,3,FACE_Z+1,block(Blocks.DARK_OAK_SLAB).setValue(SlabBlock.TYPE,SlabType.TOP));
        set(l,b,1,2,FACE_Z+1,YardBlock.of(YardBlock.Kind.BULB,Direction.NORTH,0));
        // The back of the House at the south end, the lane in from its door, the bulb.
        box(l,b,-3,0,-2,3,4,0,YardRooms.mat("clapboard"));box(l,b,-1,0,-2,1,3,-1,air());
        set(l,b,2,3,-3,block(Blocks.DARK_OAK_SLAB).setValue(SlabBlock.TYPE,SlabType.TOP));set(l,b,2,2,-3,YardBlock.of(YardBlock.Kind.BULB,Direction.NORTH,0));
        LabyrinthBuilder.entrance(l,b,YardRooms.mat("clapboard"),block(Blocks.GRASS_BLOCK),block(Blocks.DARK_OAK_SLAB));
        LabyrinthBuilder.doors(l,b,JOISTS);
    }
    private static void clearSweep(ServerLevel l,BlockPos b){
        for(int z=SWEEP_SOUTH+2;z>=SWEEP_NORTH-2;z--)for(int y:new int[]{ROUTE_Y+1,BEAM_Y}){var at=b.offset(ROUTE_X,y,z);if(l.getBlockState(at).is(Blocks.LIGHT))l.setBlock(at,air(),FLAGS);}
    }
    static void joistTick(ServerLevel l,BlockPos b,CompoundTag s,List<ServerPlayer> here,long now){
        boolean any=!here.isEmpty();
        if(!any&&joistSweep==-1000)return;
        if(nextJoistCar<0)nextJoistCar=now+120;
        if(any&&now>=nextJoistCar&&joistSweep==-1000){
            clearSweep(l,b);joistSweep=SWEEP_SOUTH;nextJoistCar=now+260+l.getRandom().nextInt(200);
            l.playSound(null,b.offset(ROUTE_X,ROUTE_Y+1,-12),PuzzleRegistry.YARD_TRAFFIC.get(),SoundSource.AMBIENT,1.6F,1F);
            for(var p:here){var own=reader(l,p,JOISTS);if(!own.getBoolean("Lit")){own.putBoolean("Lit",true);saveReader(l,p,JOISTS,own);}}
        }
        if(any&&now%300==150)l.playSound(null,b.offset(4,2,-20),SoundEvents.WEATHER_RAIN,SoundSource.AMBIENT,.2F,1.4F);
        if(any&&now%340==20)l.playSound(null,b.offset(-10,6,-24),PuzzleRegistry.YARD_WIND.get(),SoundSource.AMBIENT,.6F,1F);
        if(joistSweep==-1000)return;
        // The car runs north along the route, its lights on the asphalt and thrown up onto the joists, and leaves the dark behind it.
        for(int z=joistSweep;z>=joistSweep-1;z--)for(int y:new int[]{ROUTE_Y+1,BEAM_Y}){var at=b.offset(ROUTE_X,y,z);if(l.getBlockState(at).is(Blocks.LIGHT))l.setBlock(at,air(),FLAGS);}
        joistSweep--;
        if(joistSweep-1<SWEEP_NORTH){joistSweep=-1000;return;}
        for(int z=joistSweep;z>=joistSweep-1;z--)for(int y:new int[]{ROUTE_Y+1,BEAM_Y}){var at=b.offset(ROUTE_X,y,z);if(l.getBlockState(at).isAir())l.setBlock(at,block(Blocks.LIGHT).setValue(LightBlock.LEVEL,15),FLAGS);}
        if(joistSweep==-24)l.playSound(null,b.offset(-4,JOIST_Y-1,-24),SoundEvents.COPPER_BULB_TURN_OFF,SoundSource.AMBIENT,.35F,.6F);
    }

    // ------------------------------------------------------------------ the shared tick
    /** Whether any player's camera, spectators' included, has a point within sixty degrees of their gaze. */
    static boolean watched(ServerLevel l,BlockPos at,double range){
        var c=Vec3.atCenterOf(at);
        for(var p:l.players()){var to=c.subtract(p.getEyePosition());double d=to.length();if(d>range||d<.01)continue;if(p.getViewVector(1F).dot(to.scale(1/d))>.5)return true;}
        return false;
    }
    /** A reader walking on soft mud leaves their prints in it, pointed the way they were going. */
    static boolean print(ServerLevel l,ServerPlayer p){
        if(!p.onGround()||p.isSpectator())return false;
        for(var at:new BlockPos[]{p.blockPosition(),p.blockPosition().below()}){
            var s=l.getBlockState(at);
            if(YardBlock.is(s,YardBlock.Kind.PRINT)){
                if(s.getValue(YardBlock.STAGE)!=0)return false;
                l.setBlock(at,YardBlock.of(YardBlock.Kind.PRINT,p.getDirection(),1),FLAGS);return true;
            }
        }
        return false;
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();var origin=HouseSavedData.get(server).houseOrigin();var l=server.getLevel(HouseDimensions.OUTSIDE);if(origin==null||l==null)return;
        long now=l.getGameTime();
        for(var place:PLACES){
            var b=LabyrinthPlaces.base(origin,place);var reach=LabyrinthPlaces.reach(origin,place);if(b==null||reach==null||!l.isLoaded(b))continue;
            var s=state(l,place);if(!s.getBoolean("Built"))continue;
            var here=new ArrayList<ServerPlayer>();
            for(var p:l.players())if(p.isAlive()&&!p.isSpectator()&&reach.isInside(p.blockPosition()))here.add(p);
            boolean trodden=false;for(var p:here)trodden|=print(l,p);
            if(trodden&&!s.getBoolean("Trodden")){s.putBoolean("Trodden",true);save(l,place,s);}
            switch(place){
                case YARD_LONG_DRIVE->longTick(l,b,s,here,now);
                case YARD_PORCH_TURN->porchTick(l,b,s,here,now);
                case YARD_JOISTS->joistTick(l,b,s,here,now);
                default->{}
            }
        }
    }
}
