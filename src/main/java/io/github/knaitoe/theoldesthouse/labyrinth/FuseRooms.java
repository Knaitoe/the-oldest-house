package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import static io.github.knaitoe.theoldesthouse.labyrinth.PowerBlock.Kind.*;

/**
 * 0.4.254, the Fuse Board (docs/FUSE_BOARD_0_4_254.md), the owner's fourth puzzle proposal of 2 November: "One working fuse, two circuits:
 * a work light and a powered shutter. You need the light to find and secure a manual catch, then transfer the fuse to operate the
 * shutter. Deeper versions combine limited power with doors you can wedge and equipment whose operation is visible through adjoining
 * windows. Wires physically lead to their appliances. Every change is reversible, so experimentation is useful."
 * <p>
 * The board: a holder on the wall for each circuit, labelled in an old hand, and a spare clip for each fuse. A hand on a holder with a
 * fuse in it moves the fuse to an empty clip; a hand on an empty holder moves a clip's fuse into it. Nothing is carried, so nothing can
 * leave the room. Cables run in clips along the walls from each holder to what it feeds, and each appliance has a lamp that shows it
 * has power.
 * <p>
 * <b>The fuse room</b> (early): a basement workshop, dark but for the board's pilot lamp and the line of light under a steel roller
 * shutter. One fuse; holders for the WORK LIGHT and the SHUTTER. Powered, the shutter's motor strains against a manual catch on its
 * guide rail and stops; in the dark the catch cannot be found. With the light on it can be drawn and pinned back, and then, with the
 * fuse moved to the shutter, the shutter rolls up onto the lit room beyond and the pin holds it there.
 * <p>
 * <b>The fuse rooms</b> (deep): three rooms in a row with windows between, two fuses and four circuits. The door between the first two
 * rooms opens only while its latch has power, but it can be wedged; the hoist in the second room, seen working through the window
 * from the board, lifts a crate off a floor hatch, and holds it up only while powered; down the hatch a passage leads under the wall
 * into the third room, dark but for its work light, where the shutter has a catch of its own. Nobody can be shut in by a fuse moved
 * at the board: the latched door has a push bar on the second room's side, and an iron door with a handle only on the third room's
 * side lets a reader back into the second, shutting itself behind them.
 * <p>
 * Like every puzzle room nothing counts down, hurts or is used up; every move can be undone; solved stays solved, for everyone; no Witness.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class FuseRooms {
    private FuseRooms(){}
    public static final LabyrinthPlace EARLY=LabyrinthPlace.FUSE_ROOM,DEEP=LabyrinthPlace.FUSE_ROOMS;
    public static final String STATE="fuse_rooms_0254";
    private static final int FLAGS=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;

    /** The circuits, their holders' kinds and labels, and what their cables feed. */
    public enum Circuit {
        LIGHT(HOLDER_LIGHT,CABLE_LIGHT,"WORK LIGHT","the work light"),
        SHUTTER(HOLDER_SHUTTER,CABLE_SHUTTER,"SHUTTER","the shutter's motor"),
        LATCH(HOLDER_LATCH,CABLE_LATCH,"DOOR LATCH","the latch box beside the door"),
        HOIST(HOLDER_HOIST,CABLE_HOIST,"HOIST","the hoist's motor");
        public final PowerBlock.Kind holder,cable;public final String label,feeds;
        Circuit(PowerBlock.Kind holder,PowerBlock.Kind cable,String label,String feeds){this.holder=holder;this.cable=cable;this.label=label;this.feeds=feeds;}
        static @Nullable Circuit of(PowerBlock.Kind kind){for(var c:values())if(c.holder==kind||c.cable==kind)return c;return null;}
    }

    // ------------------------------------------------------------------ the fuse room (early)
    /** The workshop's shutter wall, and where its board's holders and clip are. */
    public static final int EARLY_WALL=-10;
    public static final BlockPos EARLY_SHUTTER_HOLDER=new BlockPos(-4,1,-4),EARLY_LIGHT_HOLDER=new BlockPos(-4,1,-5),EARLY_CLIP=new BlockPos(-4,1,-6),EARLY_LAMP=new BlockPos(-4,2,-8);

    // ------------------------------------------------------------------ the fuse rooms (deep)
    /** The three rooms' walls, the shutter wall, the board's clips and holders. */
    public static final int DEEP_WALL=-24;
    public static final BlockPos DEEP_CLIP_A=new BlockPos(-4,1,-2),DEEP_CLIP_B=new BlockPos(-4,1,-3),DEEP_LATCH_HOLDER=new BlockPos(-4,1,-4),DEEP_HOIST_HOLDER=new BlockPos(-4,1,-5),
        DEEP_LIGHT_HOLDER=new BlockPos(-4,1,-6),DEEP_SHUTTER_HOLDER=new BlockPos(-4,1,-7),DEEP_LAMP=new BlockPos(-4,2,-21);
    /** The latched door between the first two rooms, its latch box and its wedge; the third room's way back into the second, with a handle
     * only on the third room's side. */
    public static final BlockPos DOOR=new BlockPos(2,0,-8),LATCH_BOX=new BlockPos(3,1,-7),WEDGE_AT=new BlockPos(3,0,-7),BACK_DOOR=new BlockPos(0,0,-16);
    /** How long a door opened by hand stands before its closer may take it, in clock steps of five ticks. */
    static final int CLOSER=8;
    /** The hoist on the second room's west wall, its jib, the crate's column, the hatch under it; the passage's far hatch in the third room. */
    public static final BlockPos HOIST_AT=new BlockPos(-4,3,-12),HATCH_AT=new BlockPos(-2,-1,-12),FAR_HATCH=new BlockPos(-2,-1,-18);
    public static final int CRATE_X=-2,CRATE_Z=-12,CRATE_TOP=2;

    static BlockState mat(String name){return PuzzleRegistry.powerMaterial(name).defaultBlockState();}
    static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){l.setBlock(b.offset(at),s,FLAGS);}
    static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){l.setBlock(b.offset(x,y,z),s,FLAGS);}
    static boolean solved(ServerLevel l,LabyrinthPlace place){return PuzzleRooms.solved(l.getServer(),place);}
    static int wall(LabyrinthPlace place){return place==EARLY?EARLY_WALL:DEEP_WALL;}
    static BlockPos lamp(LabyrinthPlace place){return place==EARLY?EARLY_LAMP:DEEP_LAMP;}
    public static BlockPos catchAt(LabyrinthPlace place){return new BlockPos(-2,1,wall(place));}
    public static BlockPos holder(LabyrinthPlace place,Circuit c){
        if(place==EARLY)return c==Circuit.LIGHT?EARLY_LIGHT_HOLDER:EARLY_SHUTTER_HOLDER;
        return switch(c){case LIGHT->DEEP_LIGHT_HOLDER;case SHUTTER->DEEP_SHUTTER_HOLDER;case LATCH->DEEP_LATCH_HOLDER;case HOIST->DEEP_HOIST_HOLDER;};
    }
    public static List<Circuit> circuits(LabyrinthPlace place){return place==EARLY?List.of(Circuit.LIGHT,Circuit.SHUTTER):List.of(Circuit.values());}
    public static List<BlockPos> clips(LabyrinthPlace place){return place==EARLY?List.of(EARLY_CLIP):List.of(DEEP_CLIP_A,DEEP_CLIP_B);}

    // ------------------------------------------------------------------ building
    /** Whitewashed brick walls, a painted concrete floor and a concrete soffit, everything inside them air. */
    static void shell(ServerLevel l,BlockPos b,int x0,int z0,int x1,int z1){
        var brick=mat("brick");var floor=mat("floor");var ceiling=mat("ceiling");
        for(int x=x0;x<=x1;x++)for(int z=z1;z<=z0;z++)for(int y=-1;y<=5;y++){
            boolean wall=x==x0||x==x1||z==z0||z==z1;
            set(l,b,x,y,z,y==-1?floor:y==5?ceiling:wall?brick:Blocks.AIR.defaultBlockState());
        }
    }
    static void across(ServerLevel l,BlockPos b,int z,int x0,int x1){for(int x=x0;x<=x1;x++)for(int y=0;y<=4;y++)set(l,b,x,y,z,mat("brick"));}
    static void lantern(ServerLevel l,BlockPos b,int x,int z){set(l,b,x,4,z,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));}
    /** A cable laid along a run of cells, each with its stage (run, rise or junction), facing away from its wall. */
    static void cable(ServerLevel l,BlockPos b,Circuit c,Direction facing,int[][] cells){for(var at:cells)set(l,b,at[0],at[1],at[2],PowerBlock.of(c.cable,facing,at[3]));}
    /** The shutter wall: the opening's slats, the housing over them, the guide rails either side and the catch on the west rail. */
    static void shutterWall(ServerLevel l,BlockPos b,int z,boolean up,boolean pinned){
        var plate=mat("plate");
        for(int x=-2;x<=2;x++)set(l,b,x,4,z,plate);
        for(int x=-1;x<=1;x++){set(l,b,x,3,z,PowerBlock.of(SHUTTER_BOX,Direction.SOUTH));for(int y=0;y<=2;y++)set(l,b,x,y,z,up?Blocks.AIR.defaultBlockState():PowerBlock.of(SHUTTER,Direction.SOUTH,y==0?1:0));}
        for(int x:new int[]{-2,2})for(int y=0;y<=3;y++)set(l,b,x,y,z,PowerBlock.of(GUIDE,Direction.SOUTH));
        set(l,b,-2,1,z,PowerBlock.of(CATCH,Direction.SOUTH,pinned?1:0));
    }
    static void fuses(ServerLevel l,BlockPos b,LabyrinthPlace place,Set<Circuit> powered){
        int spare=clips(place).size()-powered.size();
        for(var c:circuits(place))set(l,b,holder(place,c),PowerBlock.of(c.holder,Direction.EAST,powered.contains(c)?1:0));
        var cl=clips(place);for(int i=0;i<cl.size();i++)set(l,b,cl.get(i),PowerBlock.of(CLIP,Direction.EAST,i<spare?1:0));
    }

    public static void buildEarly(ServerLevel l,BlockPos b){
        boolean solved=solved(l,EARLY);
        shell(l,b,-5,0,5,EARLY_WALL);shell(l,b,-4,EARLY_WALL,4,-15);
        shutterWall(l,b,EARLY_WALL,solved,solved);
        // The board on the west wall: the SHUTTER and WORK LIGHT holders and the spare clip, and the cables up the wall to what they feed.
        fuses(l,b,EARLY,solved?EnumSet.of(Circuit.LIGHT):EnumSet.noneOf(Circuit.class));
        cable(l,b,Circuit.SHUTTER,Direction.EAST,new int[][]{{-4,2,-4,1},{-4,3,-4,1},{-4,4,-4,2},{-4,4,-5,0},{-4,4,-6,0},{-4,4,-7,0},{-4,4,-8,0},{-4,4,-9,2}});
        cable(l,b,Circuit.SHUTTER,Direction.SOUTH,new int[][]{{-3,4,-9,0},{-2,4,-9,2}});
        cable(l,b,Circuit.LIGHT,Direction.EAST,new int[][]{{-4,2,-5,1},{-4,3,-5,2},{-4,3,-6,0},{-4,3,-7,0},{-4,3,-8,2}});
        set(l,b,EARLY_LAMP,PowerBlock.of(WORK_LAMP,Direction.EAST,solved?1:0));
        // The workshop's bench and its things; the lit loading bay beyond the shutter.
        set(l,b,4,0,-3,Blocks.SMITHING_TABLE.defaultBlockState());set(l,b,4,0,-4,Blocks.GRINDSTONE.defaultBlockState().setValue(GrindstoneBlock.FACE,net.minecraft.world.level.block.state.properties.AttachFace.FLOOR).setValue(GrindstoneBlock.FACING,Direction.WEST));
        set(l,b,4,0,-6,Blocks.BARREL.defaultBlockState());set(l,b,4,0,-7,Blocks.BARREL.defaultBlockState());set(l,b,3,0,-8,Blocks.CHAIN.defaultBlockState());
        lantern(l,b,0,-12);lantern(l,b,-3,-13);lantern(l,b,3,-13);
        set(l,b,-3,0,-14,Blocks.BARREL.defaultBlockState());set(l,b,3,0,-11,Blocks.BARREL.defaultBlockState());
        LabyrinthBuilder.entrance(l,b,mat("brick"),mat("floor"),mat("ceiling"));
        LabyrinthBuilder.doors(l,b,EARLY);
    }

    public static void buildDeep(ServerLevel l,BlockPos b){
        boolean solved=solved(l,DEEP);
        // Under the floors, solid ground: the passage from the hoist's hatch runs through it under the wall into the third room.
        for(int x=-5;x<=5;x++)for(int y=-6;y<=-2;y++)for(int z=-25;z<=-7;z++)set(l,b,x,y,z,mat("brick"));
        shell(l,b,-5,0,5,-29);
        across(l,b,-8,-4,4);across(l,b,-16,-4,4);across(l,b,DEEP_WALL,-4,4);
        // The latched door and the window between the first two rooms; the window between the second and third.
        door(l,b,DOOR,DoorHingeSide.LEFT,solved);door(l,b,BACK_DOOR,DoorHingeSide.RIGHT,false);
        for(int x=-3;x<=-1;x++)for(int y=1;y<=2;y++)set(l,b,x,y,-8,Blocks.GLASS_PANE.defaultBlockState());
        for(int x=1;x<=3;x++)for(int y=1;y<=2;y++)set(l,b,x,y,-16,Blocks.GLASS_PANE.defaultBlockState());
        set(l,b,LATCH_BOX,PowerBlock.of(LATCH,Direction.SOUTH,solved?0:0));set(l,b,WEDGE_AT,PowerBlock.of(WEDGE,Direction.SOUTH,solved?1:0));
        // The board on the first room's west wall: two clips, four holders.
        fuses(l,b,DEEP,solved?EnumSet.of(Circuit.HOIST,Circuit.LIGHT):EnumSet.noneOf(Circuit.class));
        // The latch's cable up the wall, along the head of the dividing wall and down to the latch box.
        cable(l,b,Circuit.LATCH,Direction.EAST,new int[][]{{-4,2,-4,1},{-4,3,-4,1},{-4,4,-4,2},{-4,4,-5,0},{-4,4,-6,0},{-4,4,-7,2}});
        cable(l,b,Circuit.LATCH,Direction.SOUTH,new int[][]{{-3,4,-7,0},{-2,4,-7,0},{-1,4,-7,0},{0,4,-7,0},{1,4,-7,0},{2,4,-7,0},{3,4,-7,2},{3,3,-7,1},{3,2,-7,1}});
        // The hoist's, the light's and the shutter's cables through the dividing walls to the rooms they serve.
        cable(l,b,Circuit.HOIST,Direction.EAST,new int[][]{{-4,2,-5,1},{-4,3,-5,2},{-4,3,-6,0},{-4,3,-7,2},{-4,3,-9,2},{-4,3,-10,0},{-4,3,-11,0}});
        cable(l,b,Circuit.LIGHT,Direction.EAST,new int[][]{{-4,2,-6,2},{-4,2,-7,2},{-4,2,-9,2},{-4,2,-10,0},{-4,2,-11,0},{-4,2,-12,0},{-4,2,-13,0},{-4,2,-14,0},{-4,2,-15,2},
            {-4,2,-17,2},{-4,2,-18,0},{-4,2,-19,0},{-4,2,-20,0}});
        cable(l,b,Circuit.SHUTTER,Direction.EAST,new int[][]{{-4,1,-9,2},{-4,1,-10,0},{-4,1,-11,0},{-4,1,-12,0},{-4,1,-13,0},{-4,1,-14,0},{-4,1,-15,2},
            {-4,1,-17,2},{-4,1,-18,0},{-4,1,-19,0},{-4,1,-20,0},{-4,1,-21,0},{-4,1,-22,0},{-4,1,-23,2},{-4,2,-23,1},{-4,3,-23,2}});
        cable(l,b,Circuit.SHUTTER,Direction.SOUTH,new int[][]{{-3,3,-23,0},{-2,3,-23,2}});
        // The hoist, its jib out over the hatch, the chain and the crate; the hatch, the ladder down, the passage under the wall, and up into the third room.
        set(l,b,HOIST_AT,PowerBlock.of(HOIST,Direction.EAST,solved?1:0));
        set(l,b,-3,4,CRATE_Z,PowerBlock.of(JIB,Direction.EAST,0));set(l,b,CRATE_X,4,CRATE_Z,PowerBlock.of(JIB,Direction.EAST,1));
        crate(l,b,solved?CRATE_TOP:0);
        for(int z=-12;z>=-18;z--)for(int y=-4;y<=-3;y++)set(l,b,CRATE_X,y,z,Blocks.AIR.defaultBlockState());
        for(int y=-4;y<=-2;y++){set(l,b,CRATE_X,y,-12,Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.NORTH));set(l,b,CRATE_X,y,-18,Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.SOUTH));}
        set(l,b,HATCH_AT,PowerBlock.of(HATCH,Direction.NORTH,solved?1:0));set(l,b,FAR_HATCH,PowerBlock.of(HATCH,Direction.NORTH,1));
        // The third room: dark but for its work light; its shutter wall; the lit bay beyond with the far door.
        set(l,b,DEEP_LAMP,PowerBlock.of(WORK_LAMP,Direction.EAST,solved?1:0));
        shutterWall(l,b,DEEP_WALL,solved,solved);
        set(l,b,3,0,-22,Blocks.BARREL.defaultBlockState());set(l,b,3,0,-21,Blocks.BARREL.defaultBlockState());set(l,b,4,0,-18,Blocks.CHAIN.defaultBlockState());
        // The first two rooms' lamps, and the bay's.
        lantern(l,b,0,-4);lantern(l,b,2,-10);lantern(l,b,0,-26);lantern(l,b,-3,-27);lantern(l,b,3,-27);
        set(l,b,4,0,-2,Blocks.SMITHING_TABLE.defaultBlockState());set(l,b,4,0,-14,Blocks.BARREL.defaultBlockState());set(l,b,3,0,-14,Blocks.BARREL.defaultBlockState());
        LabyrinthBuilder.entrance(l,b,mat("brick"),mat("floor"),mat("ceiling"));
        LabyrinthBuilder.doors(l,b,DEEP);
    }
    static void door(ServerLevel l,BlockPos b,BlockPos at,DoorHingeSide hinge,boolean open){
        var s=Blocks.IRON_DOOR.defaultBlockState().setValue(DoorBlock.FACING,Direction.SOUTH).setValue(DoorBlock.HINGE,hinge).setValue(DoorBlock.OPEN,open);
        set(l,b,at,s.setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER));set(l,b,at.above(),s.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));
    }
    /** Swings a door already standing at {@code at}, keeping its hinge. */
    static void swing(ServerLevel l,BlockPos b,BlockPos at,boolean open){
        for(var cell:new BlockPos[]{at,at.above()}){var s=l.getBlockState(b.offset(cell));if(s.getBlock() instanceof DoorBlock)set(l,b,cell,s.setValue(DoorBlock.OPEN,open));}
    }
    /** The crate at a height above its hatch, the chain from it up to the jib. */
    static void crate(ServerLevel l,BlockPos b,int h){
        for(int y=0;y<=3;y++)set(l,b,CRATE_X,y,CRATE_Z,y==h?PowerBlock.of(CRATE,Direction.NORTH):y>h?Blocks.CHAIN.defaultBlockState():Blocks.AIR.defaultBlockState());
    }

    // ------------------------------------------------------------------ reading the room
    public static boolean powered(ServerLevel l,BlockPos b,LabyrinthPlace place,Circuit c){return circuits(place).contains(c)&&PowerBlock.stage(l.getBlockState(b.offset(holder(place,c))))==1;}
    public static boolean lit(ServerLevel l,BlockPos b,LabyrinthPlace place){return PowerBlock.stage(l.getBlockState(b.offset(lamp(place))))==1;}
    public static boolean pinned(ServerLevel l,BlockPos b,LabyrinthPlace place){return PowerBlock.stage(l.getBlockState(b.offset(catchAt(place))))==1;}
    /** How many rows of the shutter are still down (3 shut, 0 rolled up). */
    public static int shutterRows(ServerLevel l,BlockPos b,LabyrinthPlace place){int n=0;for(int y=0;y<=2;y++)if(PowerBlock.is(l.getBlockState(b.offset(0,y,wall(place))),SHUTTER))n++;return n;}
    public static int crateHeight(ServerLevel l,BlockPos b){for(int y=0;y<=CRATE_TOP;y++)if(PowerBlock.is(l.getBlockState(b.offset(CRATE_X,y,CRATE_Z)),CRATE))return y;return -1;}
    public static boolean doorOpen(ServerLevel l,BlockPos b){return open(l,b,DOOR);}
    public static boolean backDoorOpen(ServerLevel l,BlockPos b){return open(l,b,BACK_DOOR);}
    static boolean open(ServerLevel l,BlockPos b,BlockPos at){var s=l.getBlockState(b.offset(at));return s.getBlock() instanceof DoorBlock&&s.getValue(DoorBlock.OPEN);}
    public static boolean wedged(ServerLevel l,BlockPos b){return PowerBlock.stage(l.getBlockState(b.offset(WEDGE_AT)))==1;}
    public static boolean hatchOpen(ServerLevel l,BlockPos b){return PowerBlock.stage(l.getBlockState(b.offset(HATCH_AT)))==1;}
    static boolean occupied(ServerLevel l,BlockPos at){return !l.getEntitiesOfClass(LivingEntity.class,new AABB(at).deflate(.02),e->e.isAlive()&&!e.isSpectator()).isEmpty();}
    static void tell(ServerLevel l,LabyrinthPlace place,String line){for(var p:l.players())if(PuzzleRooms.at(p)==place)PuzzleRooms.say(p,line);}

    // ------------------------------------------------------------------ the board
    /** A hand on a holder: its fuse to a spare clip, or a spare clip's fuse into it. */
    static void fuse(ServerPlayer p,ServerLevel l,BlockPos b,LabyrinthPlace place,BlockPos at,BlockState s,Circuit c){
        var clips=clips(place);
        if(s.getValue(PowerBlock.STAGE)==1){
            for(var clip:clips)if(PowerBlock.stage(l.getBlockState(b.offset(clip)))==0){
                l.setBlock(at,s.setValue(PowerBlock.STAGE,0),FLAGS);set(l,b,clip,PowerBlock.of(CLIP,Direction.EAST,1));
                l.playSound(null,at,SoundEvents.STONE_BUTTON_CLICK_OFF,SoundSource.BLOCKS,.8F,.8F);
                PuzzleRooms.say(p,"You draw the fuse from "+c.label+" and push it into the spare clip.");return;}
            PuzzleRooms.say(p,"There's no empty clip to put it in.");return;
        }
        for(var clip:clips)if(PowerBlock.stage(l.getBlockState(b.offset(clip)))==1){
            set(l,b,clip,PowerBlock.of(CLIP,Direction.EAST,0));l.setBlock(at,s.setValue(PowerBlock.STAGE,1),FLAGS);
            l.playSound(null,at,SoundEvents.STONE_BUTTON_CLICK_ON,SoundSource.BLOCKS,.8F,1.1F);l.playSound(null,at,PuzzleRegistry.HUM.get(),SoundSource.BLOCKS,.4F,1F);
            PuzzleRooms.say(p,"You push the fuse home under "+c.label+". Somewhere along its cable, something wakes.");return;}
        PuzzleRooms.say(p,"The holder marked "+c.label+" is empty, and so "+(clips.size()>1?"are the spare clips":"is the spare clip")+": the fuse"+(clips.size()>1?"s are":" is")+" in another holder.");
    }

    // ------------------------------------------------------------------ hands on
    private static CompoundTag own(ServerLevel l,ServerPlayer p){return LabyrinthData.get(l.getServer()).stateEntry(STATE,p.getUUID().toString());}
    private static void save(ServerLevel l,ServerPlayer p,CompoundTag own){var data=LabyrinthData.get(l.getServer());var all=data.state(STATE);all.put(p.getUUID().toString(),own);data.setState(STATE,all);}
    static @Nullable LabyrinthPlace place(ServerPlayer p,BlockPos pos){
        var origin=HouseSavedData.get(p.server).houseOrigin();if(origin==null)return null;
        var place=LabyrinthPlaces.placeAt(origin,pos);return place==EARLY||place==DEEP?place:null;
    }
    public static boolean use(ServerPlayer p,BlockPos pos,BlockState s){
        if(p.isSpectator())return false;
        var place=place(p,pos);if(place==null)return false;
        var b=PuzzleRooms.base(p.server,place);var l=p.serverLevel();var kind=s.getValue(PowerBlock.KIND);
        if(kind.holder()||kind==CLIP){var own=own(l,p);if(!own.getBoolean("Told")){own.putBoolean("Told",true);save(l,p,own);
            PuzzleRooms.say(p,"A fuse board: a holder for each circuit, each labelled in an old hand, and a spare clip"+(place==EARLY?"":" for each fuse")+". A fuse moves between them. It never leaves the board.");return true;}}
        var c=Circuit.of(kind);
        if(kind.holder()&&c!=null){fuse(p,l,b,place,pos,s,c);return true;}
        if(kind.cable()&&c!=null){PuzzleRooms.say(p,"A cable in clips along the wall, from the board's "+c.label+" holder to "+c.feeds+".");return true;}
        switch(kind){
            case CLIP->PuzzleRooms.say(p,s.getValue(PowerBlock.STAGE)==1?"The spare clip, with a fuse in it. Over it the board's pilot lamp glows.":"The spare clip, empty. Over it the board's pilot lamp glows.");
            case WORK_LAMP->PuzzleRooms.say(p,s.getValue(PowerBlock.STAGE)==1?"A work light in a cage on its bracket, burning.":"A work light in a cage on its bracket, cold. Its cable runs back along the wall.");
            case SHUTTER->PuzzleRooms.say(p,powered(l,b,place,Circuit.SHUTTER)&&!pinned(l,b,place)?"A steel roller shutter. It shivers in its rails and doesn't lift.":"A steel roller shutter, down. There's light under it.");
            case SHUTTER_BOX->PuzzleRooms.say(p,"The shutter's motor in its housing. Its lamp is "+(s.getValue(PowerBlock.STAGE)==1?"lit.":"dark."));
            case GUIDE->PuzzleRooms.say(p,lit(l,b,place)?"The shutter's guide rail. Lower down on the west rail, something is fixed to it.":"Cold iron, in the dark.");
            case CATCH->catchHand(p,l,b,place,pos,s);
            case LATCH->PuzzleRooms.say(p,"An electric latch box beside the door. Its lamp is "+(s.getValue(PowerBlock.STAGE)==1?"lit: the door will open.":"dark: the door won't."));
            case WEDGE->wedgeHand(p,l,b,pos,s);
            case HOIST->PuzzleRooms.say(p,"A hoist's motor bolted to the wall, its jib out over the floor. Its lamp is "+(s.getValue(PowerBlock.STAGE)==1?"lit.":"dark."));
            case JIB->PuzzleRooms.say(p,"The hoist's jib, the chain over its pulley.");
            case CRATE->PuzzleRooms.say(p,crateHeight(l,b)==0?"A crate of something heavy on the hoist's hook. It sits square on a hatch in the floor.":"A crate of something heavy, hanging from the hoist's hook.");
            case HATCH->hatchHand(p,l,b,pos,s);
            default->{return false;}
        }
        return true;
    }
    static void catchHand(ServerPlayer p,ServerLevel l,BlockPos b,LabyrinthPlace place,BlockPos pos,BlockState s){
        if(!lit(l,b,place)){PuzzleRooms.say(p,"Your hand finds only cold iron in the dark: the rail, and the wall.");return;}
        boolean up=shutterRows(l,b,place)<3;
        if(s.getValue(PowerBlock.STAGE)==0){
            l.setBlock(pos,s.setValue(PowerBlock.STAGE,1),FLAGS);l.playSound(null,pos,SoundEvents.IRON_TRAPDOOR_OPEN,SoundSource.BLOCKS,.8F,1.2F);
            PuzzleRooms.say(p,"There, in the light: a catch on the rail, holding the shutter down. You draw it and pin it back.");
        }else if(up)PuzzleRooms.say(p,"The catch, pinned back. The pin holds the shutter up.");
        else{l.setBlock(pos,s.setValue(PowerBlock.STAGE,0),FLAGS);l.playSound(null,pos,SoundEvents.IRON_TRAPDOOR_CLOSE,SoundSource.BLOCKS,.8F,1F);PuzzleRooms.say(p,"You pull the pin and let the catch back onto the rail.");}
    }
    static void wedgeHand(ServerPlayer p,ServerLevel l,BlockPos b,BlockPos pos,BlockState s){
        if(s.getValue(PowerBlock.STAGE)==1){l.setBlock(pos,s.setValue(PowerBlock.STAGE,0),FLAGS);l.playSound(null,pos,SoundEvents.WOOD_HIT,SoundSource.BLOCKS,.7F,1F);PuzzleRooms.say(p,"You work the wedge out from under the door.");return;}
        if(!doorOpen(l,b)){PuzzleRooms.say(p,"A wooden wedge, by the wall. The door's shut: it would hold it open.");return;}
        l.setBlock(pos,s.setValue(PowerBlock.STAGE,1),FLAGS);l.playSound(null,pos,SoundEvents.WOOD_PLACE,SoundSource.BLOCKS,.8F,.8F);
        PuzzleRooms.say(p,"You kick the wedge under the open door. It'll stay open now, latch or no latch.");
    }
    static void hatchHand(ServerPlayer p,ServerLevel l,BlockPos b,BlockPos pos,BlockState s){
        if(pos.equals(b.offset(FAR_HATCH))){PuzzleRooms.say(p,"An open hatch. A ladder goes down into a passage under the wall.");return;}
        if(crateHeight(l,b)==0){PuzzleRooms.say(p,"Something heavy sits on the hatch: the crate.");return;}
        boolean open=s.getValue(PowerBlock.STAGE)==0;
        if(!open&&occupied(l,pos)){PuzzleRooms.say(p,"Someone is on the ladder.");return;}
        l.setBlock(pos,s.setValue(PowerBlock.STAGE,open?1:0),FLAGS);l.playSound(null,pos,open?SoundEvents.WOODEN_TRAPDOOR_OPEN:SoundEvents.WOODEN_TRAPDOOR_CLOSE,SoundSource.BLOCKS,.9F,.8F);
        PuzzleRooms.say(p,open?"You haul the hatch up. A ladder goes down into the dark, and a passage runs north under the wall.":"You let the hatch down.");
    }
    /**
     * A hand on the latched door: from the first room it opens and shuts only while the latch has power. From the second room a push bar
     * lets the latch go whatever its power, as a real electric lock lets anyone out, so nobody is ever shut in there by a fuse moved at
     * the board. A hand on the third room's way back: it opens from the third room's side only, and shuts itself.
     */
    @SubscribeEvent public static void door(PlayerInteractEvent.RightClickBlock e){
        if(e.getLevel().isClientSide()||!(e.getEntity() instanceof ServerPlayer p)||p.isSpectator())return;
        var place=place(p,e.getPos());if(place!=DEEP)return;var b=PuzzleRooms.base(p.server,DEEP);var rel=e.getPos().subtract(b);
        boolean main=rel.equals(DOOR)||rel.equals(DOOR.above()),back=rel.equals(BACK_DOOR)||rel.equals(BACK_DOOR.above());
        if(!main&&!back)return;
        var l=p.serverLevel();e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);
        var at=main?DOOR:BACK_DOOR;boolean beyond=p.getZ()<b.getZ()+at.getZ()+.5;boolean open=!open(l,b,at);
        if(back){
            if(!beyond){PuzzleRooms.say(p,"An iron door with no handle on this side.");return;}
            if(!open&&(occupied(l,b.offset(at))||occupied(l,b.offset(at.above())))){PuzzleRooms.say(p,"Someone is in the doorway.");return;}
            swing(l,b,at,open);opened(l,b,at,open);l.playSound(null,b.offset(at),open?SoundEvents.IRON_DOOR_OPEN:SoundEvents.IRON_DOOR_CLOSE,SoundSource.BLOCKS,.9F,1F);
            PuzzleRooms.say(p,open?"The handle turns on this side. The door opens onto the room with the hoist.":"You pull the door to.");return;
        }
        boolean power=powered(l,b,DEEP,Circuit.LATCH);
        if(!power&&!beyond){PuzzleRooms.say(p,doorOpen(l,b)?"The wedge holds it open. Without the latch it won't move.":"It won't open. The lamp on the latch box beside it is dark.");return;}
        if(!open&&(occupied(l,b.offset(at))||occupied(l,b.offset(at.above())))){PuzzleRooms.say(p,"Someone is in the doorway.");return;}
        if(!open&&wedged(l,b)){PuzzleRooms.say(p,"The wedge is under it.");return;}
        swing(l,b,at,open);opened(l,b,at,open);l.playSound(null,b.offset(at),open?SoundEvents.IRON_DOOR_OPEN:SoundEvents.IRON_DOOR_CLOSE,SoundSource.BLOCKS,.9F,1F);
        PuzzleRooms.say(p,!open?"You pull the door to. The latch takes it.":power?"The latch buzzes and lets go. The door swings open.":"On this side the door has a push bar. The latch lets go, and the door swings open.");
    }

    // ------------------------------------------------------------------ the clock
    private static final Map<Long,Integer> STRAIN=new HashMap<>(),OPENED=new HashMap<>();
    public static void clearAll(){STRAIN.clear();OPENED.clear();}
    /** Notes the clock step a door was opened by hand, so its closer waits; forgets it when shut. */
    static void opened(ServerLevel l,BlockPos b,BlockPos at,boolean open){long key=b.offset(at).asLong();if(open)OPENED.put(key,l.getServer().getTickCount()/5);else OPENED.remove(key);}
    /** Whether a door's closer may take it: nobody in the doorway, and not opened by hand in the last few seconds. */
    static boolean closes(ServerLevel l,BlockPos b,BlockPos at,int step){
        if(occupied(l,b.offset(at))||occupied(l,b.offset(at.above())))return false;
        var since=OPENED.get(b.offset(at).asLong());return since==null||step-since>=CLOSER;
    }
    /** One step of the room's clock, every five ticks ({@code step} counts them): the slow things move every other step. */
    static void update(ServerLevel l,BlockPos b,LabyrinthPlace place,int step){
        // The lamps on what has power.
        sync(l,b,lamp(place),powered(l,b,place,Circuit.LIGHT));
        boolean shutter=powered(l,b,place,Circuit.SHUTTER);
        for(int x=-1;x<=1;x++)sync(l,b,new BlockPos(x,3,wall(place)),shutter);
        if(place==DEEP){sync(l,b,LATCH_BOX,powered(l,b,place,Circuit.LATCH));sync(l,b,HOIST_AT,powered(l,b,place,Circuit.HOIST));latch(l,b,step);back(l,b,step);if(step%2==0)hoist(l,b);}
        // The shutter: the motor strains against an engaged catch, once each time it is given power; with the catch pinned, it rolls up.
        long key=b.asLong();int rows=shutterRows(l,b,place);
        if(!shutter||rows==0)STRAIN.remove(key);
        else if(!pinned(l,b,place)){
            if(!STRAIN.containsKey(key)){STRAIN.put(key,step);var at=b.offset(0,3,wall(place));l.playSound(null,at,PuzzleRegistry.STRAIN.get(),SoundSource.BLOCKS,1F,1F);
                tell(l,place,"The motor in the housing strains; the shutter lifts a finger's width in its rails and drops back. Something on the guide rail is holding it.");}
        }else if(step%2==0){
            if(rows==3)l.playSound(null,b.offset(0,3,wall(place)),PuzzleRegistry.MOTOR.get(),SoundSource.BLOCKS,1F,1F);
            roll(l,b,place,rows);
            if(rows==1)PuzzleRooms.solve(l,place,place==EARLY?"The shutter rolls up into its housing, and the pin holds it there. Beyond, a lit bay, and a door."
                :"Through the window, the shutter rolls up into its housing; the pin holds it there. Beyond it, light, and a door.");
        }
    }
    static void sync(ServerLevel l,BlockPos b,BlockPos at,boolean on){var s=l.getBlockState(b.offset(at));if(s.is(PuzzleRegistry.POWER.get())&&s.getValue(PowerBlock.STAGE)!=(on?1:0))set(l,b,at,s.setValue(PowerBlock.STAGE,on?1:0));}
    /** The shutter's lowest row rolls up; the row above it becomes the bottom slat. */
    static void roll(ServerLevel l,BlockPos b,LabyrinthPlace place,int rows){
        int z=wall(place),low=3-rows;
        for(int x=-1;x<=1;x++){set(l,b,x,low,z,Blocks.AIR.defaultBlockState());if(low+1<=2)set(l,b,x,low+1,z,PowerBlock.of(SHUTTER,Direction.SOUTH,1));}
    }
    /** Without power the latched door swings shut on its closer and locks, unless it is wedged, someone is in the doorway or a hand has only
     * just opened it. */
    static void latch(ServerLevel l,BlockPos b,int step){
        if(!doorOpen(l,b)||powered(l,b,DEEP,Circuit.LATCH)||wedged(l,b)||!closes(l,b,DOOR,step))return;
        swing(l,b,DOOR,false);opened(l,b,DOOR,false);l.playSound(null,b.offset(DOOR),SoundEvents.IRON_DOOR_CLOSE,SoundSource.BLOCKS,1F,.9F);
        tell(l,DEEP,"The door swings shut on its closer, and the latch takes it.");
    }
    /** The third room's way back shuts itself once nobody is in it. */
    static void back(ServerLevel l,BlockPos b,int step){
        if(!backDoorOpen(l,b)||!closes(l,b,BACK_DOOR,step))return;
        swing(l,b,BACK_DOOR,false);opened(l,b,BACK_DOOR,false);l.playSound(null,b.offset(BACK_DOOR),SoundEvents.IRON_DOOR_CLOSE,SoundSource.BLOCKS,.8F,1F);
    }
    /** The hoist holds the crate up only while it has power; let down, it settles back onto the hatch, shutting it, never onto anyone. */
    static void hoist(ServerLevel l,BlockPos b){
        int h=crateHeight(l,b);if(h<0)return;
        int target=powered(l,b,DEEP,Circuit.HOIST)?CRATE_TOP:0;if(h==target)return;
        if(target>h){crate(l,b,h+1);l.playSound(null,b.offset(CRATE_X,h+1,CRATE_Z),PuzzleRegistry.MOTOR.get(),SoundSource.BLOCKS,.6F,.7F);l.playSound(null,b.offset(CRATE_X,h+1,CRATE_Z),SoundEvents.CHAIN_PLACE,SoundSource.BLOCKS,.7F,.8F);return;}
        var below=new BlockPos(CRATE_X,h-1,CRATE_Z);if(occupied(l,b.offset(below)))return;
        if(h-1==0){if(occupied(l,b.offset(HATCH_AT)))return;if(hatchOpen(l,b)){set(l,b,HATCH_AT,PowerBlock.of(HATCH,Direction.NORTH,0));l.playSound(null,b.offset(HATCH_AT),SoundEvents.WOODEN_TRAPDOOR_CLOSE,SoundSource.BLOCKS,.9F,.7F);}}
        crate(l,b,h-1);l.playSound(null,b.offset(below),SoundEvents.CHAIN_PLACE,SoundSource.BLOCKS,.7F,.6F);
        if(h-1==0)l.playSound(null,b.offset(below),SoundEvents.WOOD_FALL,SoundSource.BLOCKS,1F,.6F);
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();int t=server.getTickCount();if(t%5!=2)return;var origin=HouseSavedData.get(server).houseOrigin();var l=server.getLevel(HouseDimensions.INTERIOR);
        if(origin==null||l==null)return;
        for(var place:new LabyrinthPlace[]{EARLY,DEEP}){
            if(LabyrinthData.get(server).door(place.entryDoorId())==null)continue;
            for(var p:l.players())if(PuzzleRooms.at(p)==place){var b=LabyrinthPlaces.base(origin,place);if(b!=null&&l.isLoaded(b))update(l,b,place,t/5);break;}
        }
    }
}
