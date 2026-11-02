package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseBlocks;
import io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.Vec3;
import static io.github.knaitoe.theoldesthouse.labyrinth.PeopleLightsBlock.Kind.*;

/**
 * 0.4.253, After the People Lights Have Gone Off (docs/PEOPLE_LIGHTS_0_4_253.md): the house Stan built, narrow and tall on its
 * craggy lot, built whole. Every position is relative to the base; north is into the house.
 * <p>
 * The reader comes in on the front walk (z -1 to -3, standing at 0, the yard a step down either side) to the front door at (0,0,-4),
 * with coloured glass either side of it. Inside, the ground floor stands at 0: the foyer's large tiles (x -2 to 1, z -5 to -11) open
 * on the living room's boards (x 2 to 6), both under one tall well that reaches the roof. The stair climbs the west side (x -4 to -3,
 * treads z -6 to -10) from the newel post at (-2,0,-6) to the landing at z -11. North of the well: the hall (x -2 to 0, z -12 to -19,
 * its runner down x -1), the kitchen (x 1 to 6, z -12 to -18, small tiles, the table and the tall counters, the side door east to the
 * garage), the bedroom (x -6 to 0, z -20 to -25) and Kelly's bathroom (x 1 to 6, z -20 to -25). The study stands at 5 over the north
 * half (x -2 to 6, z -12 to -18), Mark's desk facing its rail over the well at z -12; the spare chair waits by the stair head. The
 * loft stands at 10 under the roof, its ledge at z -12 over the foyer now behind a rail Stan built, a ladder up from the study's
 * storeroom. The garage (x 9 to 14, z -10 to -20) stands a step lower, at -1, its ramp up to the side door; its door opens south
 * onto the driveway (x 9 to 14, z -1 to -9), where Stan parks.
 */
public final class PeopleLightsSite {
    private PeopleLightsSite(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.PEOPLE_LIGHTS;
    /** The standing levels: the ground floor, the study, the loft; the yard, the garage and the driveway a step down. */
    public static final int GROUND=0,STUDY=5,LOFT=10,YARD=-1;
    /** Stan's offer on the kitchen table (the source); the house's book of the night on the hall table is the account (the ending). */
    public static final BlockPos OFFER_AT=new BlockPos(3,1,-14),ENDING=new BlockPos(-2,1,-13);
    /** The table (three long, z -13 to -15) and its places: Kelly's at the north end (her chair), Mark's and the guest's either side; the cards are dealt on its north end, the case set at its south end. */
    public static final BlockPos TABLE=new BlockPos(3,0,-15),CASE_DOWN=new BlockPos(3,1,-13),BED_CHAIR=new BlockPos(-3,0,-21),KELLY_PLACE=new BlockPos(3,0,-16),MARK_SEAT=new BlockPos(2,0,-15),GUEST_SEAT=new BlockPos(4,0,-15);
    /** Mark's desk and chair, facing the rail; his work case on the desk. */
    public static final BlockPos DESK=new BlockPos(2,STUDY,-13),DESK_CHAIR=new BlockPos(2,STUDY,-14),CASE_AT=new BlockPos(3,STUDY+1,-13);
    /** The sofa, and where Kelly's chair waits beside it when she is on it. */
    public static final BlockPos SOFA=new BlockPos(6,0,-8),SOFA_B=new BlockPos(6,0,-9),CHAIR_BY_SOFA=new BlockPos(5,0,-10);
    /** Where Kelly sits on the floor beside the sofa, with the cushion she asks for. */
    public static final BlockPos FLOOR_SPOT=new BlockPos(5,0,-7);
    /** The newel post, and the spot on the foyer floor where she fell. */
    public static final BlockPos NEWEL=new BlockPos(-2,0,-6),FELL=new BlockPos(-1,0,-7);
    /** The bathroom mirror, hung where Stan put it; the bathroom's door from the kitchen. */
    public static final BlockPos MIRROR=new BlockPos(4,1,-25),BATH_DOOR=new BlockPos(3,0,-19);
    /** The bedroom door at the hall's end, and the bed. */
    public static final BlockPos BEDROOM_DOOR=new BlockPos(-1,0,-20),BED=new BlockPos(-4,0,-23);
    /** The spare chair, at the stair head upstairs. */
    public static final BlockPos SPARE_CHAIR=new BlockPos(-3,STUDY,-13);
    /** The kitchen phone on the wall by the table. */
    public static final BlockPos PHONE_AT=new BlockPos(6,1,-14);
    /** The lamp by the sofa, which can be turned on in the night; the lamp by the hall's end. */
    public static final BlockPos LAMP=new BlockPos(6,0,-11),HALL_LAMP=new BlockPos(0,0,-19);
    /** Where the likeness stands, a step back from the loft rail above the foyer. */
    public static final BlockPos LIKENESS_AT=new BlockPos(-1,LOFT,-13);
    /** The side door to the garage, the foot of the ramp, the garage door and the driveway where Stan waits. */
    public static final BlockPos SIDE_DOOR=new BlockPos(7,0,-16),RAMP_FOOT=new BlockPos(10,YARD,-16),GARAGE_OUT=new BlockPos(11,YARD,-8),APRON=new BlockPos(11,YARD,-4);
    /** The front door. */
    public static final BlockPos FRONT_DOOR=new BlockPos(0,0,-4);
    /** The way on into the halls: a door in the garage's back wall. */
    public static final int WAY_X=12,WAY_Z=-21;
    /** The hall runner's cells, down x -1. */
    public static final List<BlockPos> RUNNER_CELLS;
    static{var cells=new java.util.ArrayList<BlockPos>();for(int z=-12;z>=-19;z--)cells.add(new BlockPos(-1,0,z));RUNNER_CELLS=List.copyOf(cells);}
    /**
     * Kelly's round in her sleep: from the bedroom door down the hall, into the foyer to the newel post, round it and back the other
     * side of the hall, and again.
     */
    public static final List<Vec3> ROUND=List.of(new Vec3(-.5,0,-19.5),new Vec3(-.5,0,-12.5),new Vec3(-.8,0,-8.2),new Vec3(-1.4,0,-6.6),
        new Vec3(.2,0,-6.8),new Vec3(.5,0,-10.5),new Vec3(-.2,0,-12.5),new Vec3(-.2,0,-19.2));
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;

    /** The floor under a ground-floor position, by what it is made of: the foyer's tile, the living room's boards, the kitchen's tile, the runner. */
    public enum Floor {TILE,WOOD,KITCHEN,RUNNER,OTHER}
    public static Floor floor(ServerLevel l,BlockPos b,Vec3 rel){
        var at=b.offset((int)Math.floor(rel.x),0,(int)Math.floor(rel.z));
        if(PeopleLightsBlock.is(l.getBlockState(at),RUNNER)&&l.getBlockState(at).getValue(PeopleLightsBlock.STAGE)==0)return Floor.RUNNER;
        var under=l.getBlockState(at.below());
        if(under.is(mat("tile_large").getBlock()))return Floor.TILE;
        if(under.is(mat("boards").getBlock()))return Floor.WOOD;
        if(under.is(mat("tile_small").getBlock()))return Floor.KITCHEN;
        return Floor.OTHER;
    }
    public static boolean inside(Vec3 r){return r.x>=-16&&r.x<18&&r.z>=-31&&r.z<.5&&r.y>=-3&&r.y<16;}
    /** Inside the house itself (not the yard, the garage or the driveway). */
    public static boolean inHouse(Vec3 r){return r.x>=-6.5&&r.x<7&&r.z>=-26&&r.z<-4&&r.y>=-.5&&r.y<14;}
    public static boolean groundFloor(Vec3 r){return inHouse(r)&&r.y<STUDY-1;}
    public static boolean upstairs(Vec3 r){return inHouse(r)&&r.y>=STUDY-1;}
    /** The driveway apron, out past the garage door. */
    public static boolean onApron(Vec3 r){return r.x>=8.5&&r.x<15&&r.z>=-9&&r.z<0&&r.y<GROUND;}
    /** Somewhere on the ground floor that looks up the well at the loft rail. */
    public static boolean underWell(Vec3 r){return groundFloor(r)&&r.x>=-2.5&&r.x<7&&r.z>=-11.5&&r.z<-4.5;}

    static BlockState mat(String name){return PeopleLightsRegistry.material(name).defaultBlockState();}
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){BuildBlocks.set(l,b.offset(at),s,F);}
    private static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){for(int x=Math.min(x0,x1);x<=Math.max(x0,x1);x++)for(int y=Math.min(y0,y1);y<=Math.max(y0,y1);y++)for(int z=Math.min(z0,z1);z<=Math.max(z0,z1);z++)set(l,b,x,y,z,s);}
    static void door(ServerLevel l,BlockPos b,int x,int y,int z,Block door,Direction facing,boolean open){
        var s=door.defaultBlockState().setValue(DoorBlock.FACING,facing).setValue(DoorBlock.OPEN,open).setValue(DoorBlock.HINGE,DoorHingeSide.LEFT);
        set(l,b,x,y,z,s.setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER));set(l,b,x,y+1,z,s.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));
    }
    static BlockState furniture(HouseholdFurnitureBlock.Kind kind,Direction facing){return HouseBlocks.HOUSEHOLD_FURNITURE.get().defaultBlockState().setValue(HouseholdFurnitureBlock.KIND,kind).setValue(HouseholdFurnitureBlock.FACING,facing);}
    static BlockState barrett(BarrettHomeBlock.Kind kind,Direction facing,boolean lit){return BarrettHomeBlock.of(kind,facing).setValue(BarrettHomeBlock.LIT,lit);}
    static BlockState pane(){return Blocks.GLASS_PANE.defaultBlockState();}

    public static void build(ServerLevel l,BlockPos b){
        var air=Blocks.AIR.defaultBlockState();var night=Blocks.BLACK_CONCRETE.defaultBlockState();var siding=mat("siding");var plaster=mat("plaster");
        // The night round the lot: dark earth under a dark sky, the lot's edge in spruce.
        box(l,b,-16,-3,-31,17,15,0,night);box(l,b,-15,-2,-30,16,14,-1,air);
        box(l,b,-15,-3,-30,16,-3,-1,Blocks.DIRT.defaultBlockState());box(l,b,-15,-2,-30,16,-2,-1,Blocks.GRASS_BLOCK.defaultBlockState());
        for(int x=-15;x<=16;x+=3)for(int z:new int[]{-30,-29})box(l,b,x,-1,z,x,3,z,Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true));
        for(int z=-27;z>=-1;z-=3)for(int x:new int[]{-15,-14,16})box(l,b,x,-1,z,x,2+(z&1),z,Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true));
        // The front walk, a step up from the yard, to the front door.
        box(l,b,-2,-2,-2,2,-1,-1,mat("drive"));box(l,b,-1,-1,0,1,-1,0,mat("drive"));
        for(int x:new int[]{-3,3})for(int z=-2;z<=-1;z++)set(l,b,x,-1,z,Blocks.SPRUCE_SLAB.defaultBlockState());
        // The driveway and the garage's apron, a step down, from the yard's edge to the garage door.
        box(l,b,9,-2,-9,14,-2,-1,mat("drive"));
        // The house's shell: siding outside, plaster inside, the ground floor's slab, the roof.
        box(l,b,-8,-1,-27,8,14,-3,siding);box(l,b,-7,-1,-26,7,13,-4,plaster);box(l,b,-6,0,-25,6,12,-5,air);
        box(l,b,-8,15,-28,8,15,-2,mat("shingle"));box(l,b,-9,14,-28,-9,14,-2,mat("shingle"));box(l,b,9,14,-28,9,14,-2,mat("shingle"));
        // Floors: the foyer's large tiles, the living room's boards, the hall's boards, the kitchen's and bathroom's small tiles, the bedroom's carpet.
        box(l,b,-2,-1,-11,1,-1,-5,mat("tile_large"));box(l,b,2,-1,-11,6,-1,-5,mat("boards"));box(l,b,-6,-1,-11,-3,-1,-5,mat("boards"));
        box(l,b,-2,-1,-19,0,-1,-12,mat("boards"));box(l,b,1,-1,-18,6,-1,-12,mat("tile_small"));
        box(l,b,-6,-1,-25,0,-1,-20,mat("carpet"));box(l,b,1,-1,-25,6,-1,-20,mat("tile_small"));box(l,b,-6,-1,-19,-3,-1,-12,mat("boards"));
        // Ground-floor walls: the hall's sides, the kitchen's back to the bathroom, the bedroom's front, the stair's closet under the landing.
        box(l,b,-3,0,-19,-3,3,-12,plaster);box(l,b,-6,0,-19,-4,3,-19,plaster);
        box(l,b,1,0,-19,6,3,-19,plaster);box(l,b,-6,0,-20,0,3,-20,plaster);box(l,b,1,0,-25,1,3,-20,plaster);
        // The ground floor's ceiling (the study's floor) over the north half, and the strip west of the well under the stair.
        box(l,b,-6,4,-25,6,4,-12,mat("carpet"));box(l,b,-6,4,-11,-5,4,-5,plaster);
        // Doors: the front door (closed), the bedroom's, the bathroom's, the side door to the garage (all open but the front).
        door(l,b,0,0,-4,Blocks.SPRUCE_DOOR,Direction.SOUTH,false);set(l,b,0,0,-3,air);set(l,b,0,1,-3,air);for(int x:new int[]{-1,1})for(int y=0;y<=2;y++)set(l,b,x,y,-4,Blocks.ORANGE_STAINED_GLASS_PANE.defaultBlockState());
        set(l,b,0,2,-4,Blocks.YELLOW_STAINED_GLASS_PANE.defaultBlockState());
        door(l,b,-1,0,-20,Blocks.OAK_DOOR,Direction.NORTH,true);door(l,b,3,0,-19,Blocks.OAK_DOOR,Direction.NORTH,true);
        door(l,b,7,0,-16,Blocks.OAK_DOOR,Direction.EAST,true);set(l,b,8,0,-16,air);set(l,b,8,1,-16,air);
        // Windows: the living room onto the walk (lit, seen from outside), the kitchen east over the driveway, the study and the loft at the front.
        for(int x=3;x<=5;x++)for(int y=1;y<=2;y++){set(l,b,x,y,-4,pane());set(l,b,x,y,-3,pane());}
        for(int z=-14;z>=-17;z--)if(z!=-16)for(int y=1;y<=2;y++){set(l,b,7,y,z,pane());set(l,b,8,y,z,pane());}
        for(int x:new int[]{-4,-1,2,5})for(int y=6;y<=7;y++){set(l,b,x,y,-4,pane());set(l,b,x,y,-3,pane());}
        for(int x:new int[]{-1,0,1})for(int y=11;y<=12;y++){set(l,b,x,y,-4,pane());set(l,b,x,y,-3,pane());}
        // The stair up the west side: five treads from the newel to the landing, a banister stepping up with them.
        for(int i=0;i<5;i++){int z=-6-i;for(int x=-4;x<=-3;x++){set(l,b,x,i,z,Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH));for(int under=0;under<i;under++)set(l,b,x,under,z,plaster);}
            if(i>0)set(l,b,-2,i,z,Blocks.DARK_OAK_FENCE.defaultBlockState());}
        set(l,b,NEWEL,PeopleLightsBlock.of(PeopleLightsBlock.Kind.NEWEL,Direction.SOUTH));
        box(l,b,-4,4,-11,-3,4,-11,mat("carpet"));box(l,b,-4,0,-11,-3,3,-11,plaster);
        // The study: a rail along its edge over the well, Mark's desk facing it, his chair, the case, the phone's extension, a futon.
        for(int x=-2;x<=6;x++)set(l,b,x,STUDY,-12,Blocks.DARK_OAK_FENCE.defaultBlockState());
        set(l,b,-2,STUDY,-11,Blocks.DARK_OAK_FENCE.defaultBlockState());
        set(l,b,DESK,furniture(HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.NORTH));set(l,b,DESK_CHAIR,furniture(HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.SOUTH));
        set(l,b,CASE_AT.below(),furniture(HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.NORTH));set(l,b,CASE_AT,PeopleLightsBlock.of(CASE,Direction.NORTH));
        box(l,b,-1,STUDY,-17,0,STUDY,-17,furniture(HouseholdFurnitureBlock.Kind.BLUE_SOFA,Direction.SOUTH));
        set(l,b,SPARE_CHAIR,PeopleLightsBlock.of(CHAIR,Direction.SOUTH));
        set(l,b,6,STUDY,-16,barrett(BarrettHomeBlock.Kind.BOXES,Direction.WEST,false));set(l,b,5,STUDY+3,-15,barrett(BarrettHomeBlock.Kind.CEILING_LIGHT,Direction.NORTH,true));
        // Upstairs north of the study: Mark's bathroom and the storeroom with the ladder to the loft.
        box(l,b,-6,STUDY,-18,6,STUDY+3,-18,plaster);door(l,b,-2,STUDY,-18,Blocks.OAK_DOOR,Direction.NORTH,true);door(l,b,4,STUDY,-18,Blocks.OAK_DOOR,Direction.NORTH,true);
        box(l,b,0,STUDY,-25,0,STUDY+3,-19,plaster);
        set(l,b,-4,STUDY,-24,barrett(BarrettHomeBlock.Kind.TOILET,Direction.EAST,false));set(l,b,-2,STUDY,-24,barrett(BarrettHomeBlock.Kind.VANITY,Direction.SOUTH,false));
        set(l,b,3,STUDY,-23,barrett(BarrettHomeBlock.Kind.BOXES,Direction.SOUTH,false));set(l,b,2,STUDY,-21,barrett(BarrettHomeBlock.Kind.BOXES,Direction.EAST,false));
        for(int y=STUDY;y<=LOFT-1;y++)set(l,b,6,y,-21,Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.WEST));set(l,b,6,LOFT-1,-21,Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.WEST));
        // The loft: boards, the ledge at z -12 behind Stan's rail, the boxes, the old air mattress where it was left.
        box(l,b,-6,LOFT-1,-25,6,LOFT-1,-12,mat("boards"));set(l,b,6,LOFT-1,-21,Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.WEST));
        for(int x=-6;x<=6;x++)set(l,b,x,LOFT,-12,PeopleLightsBlock.of(RAILING,Direction.NORTH));
        set(l,b,0,LOFT,-14,PeopleLightsBlock.of(MATTRESS,Direction.SOUTH));set(l,b,1,LOFT,-14,PeopleLightsBlock.of(MATTRESS,Direction.NORTH));
        for(var at:new int[][]{{-5,-24},{-4,-24},{-5,-23},{3,-24},{4,-24},{4,-20},{-5,-17}})set(l,b,at[0],LOFT,at[1],barrett(BarrettHomeBlock.Kind.BOXES,Direction.SOUTH,false));
        // The foyer: coats and shoes by the door, the photograph on the wall, a light.
        set(l,b,-2,1,-5,barrett(BarrettHomeBlock.Kind.COAT_HOOKS,Direction.EAST,false));set(l,b,-2,0,-5,barrett(BarrettHomeBlock.Kind.SHOE_RACK,Direction.EAST,false));
        set(l,b,-2,2,-9,PeopleLightsBlock.of(PHOTO,Direction.EAST));
        // The living room: the sofa against the east wall, the lamp at its end, the television opposite, the ceiling light.
        set(l,b,SOFA,furniture(HouseholdFurnitureBlock.Kind.BLUE_SOFA,Direction.WEST));set(l,b,SOFA_B,furniture(HouseholdFurnitureBlock.Kind.BLUE_SOFA,Direction.WEST));
        set(l,b,LAMP,barrett(BarrettHomeBlock.Kind.FLOOR_LAMP,Direction.WEST,true));set(l,b,2,0,-8,barrett(BarrettHomeBlock.Kind.TV_STAND,Direction.EAST,true));
        set(l,b,4,0,-6,barrett(BarrettHomeBlock.Kind.COFFEE_TABLE,Direction.WEST,false));
        set(l,b,0,3,-8,barrett(BarrettHomeBlock.Kind.CEILING_LIGHT,Direction.NORTH,true));
        // The hall: the runner down its middle, the lamp at its end, the little table with the house's book.
        for(var at:RUNNER_CELLS)set(l,b,at,PeopleLightsBlock.of(RUNNER,Direction.NORTH));
        set(l,b,HALL_LAMP,barrett(BarrettHomeBlock.Kind.FLOOR_LAMP,Direction.NORTH,true));
        set(l,b,ENDING.below(),furniture(HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE,Direction.EAST));
        // The kitchen: the tall counters along the east wall, the stove, the fridge, the table with its two chairs and Kelly's place, the phone.
        for(int z=-12;z>=-13;z--)set(l,b,6,0,z,barrett(BarrettHomeBlock.Kind.WALL_CABINET,Direction.WEST,false));
        set(l,b,6,0,-17,barrett(BarrettHomeBlock.Kind.RANGE,Direction.WEST,false));set(l,b,6,0,-18,barrett(BarrettHomeBlock.Kind.FRIDGE,Direction.WEST,false));set(l,b,6,1,-18,barrett(BarrettHomeBlock.Kind.FRIDGE_TOP,Direction.WEST,false));
        set(l,b,1,0,-12,barrett(BarrettHomeBlock.Kind.DISHWASHER,Direction.EAST,false));set(l,b,1,0,-13,barrett(BarrettHomeBlock.Kind.COFFEE_MAKER,Direction.EAST,false));
        for(int z=-13;z>=-15;z--)set(l,b,3,0,z,furniture(HouseholdFurnitureBlock.Kind.FORMICA_TABLE,Direction.NORTH));set(l,b,MARK_SEAT,furniture(HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.EAST));set(l,b,GUEST_SEAT,furniture(HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.WEST));
        set(l,b,OFFER_AT,air);set(l,b,PHONE_AT,PeopleLightsBlock.of(PHONE,Direction.WEST));set(l,b,3,3,-15,barrett(BarrettHomeBlock.Kind.CEILING_LIGHT,Direction.NORTH,true));
        // The bedroom: the bed, the bedside tables, a light. Kelly's bathroom: her rails, the tub, the toilet, the mirror where Stan hung it.
        set(l,b,BED,Blocks.WHITE_BED.defaultBlockState().setValue(BedBlock.FACING,Direction.NORTH).setValue(BedBlock.PART,BedPart.HEAD));
        set(l,b,BED.south(),Blocks.WHITE_BED.defaultBlockState().setValue(BedBlock.FACING,Direction.NORTH).setValue(BedBlock.PART,BedPart.FOOT));
        set(l,b,-5,0,-23,furniture(HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE,Direction.EAST));set(l,b,-3,0,-23,furniture(HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE,Direction.WEST));
        set(l,b,-3,3,-22,barrett(BarrettHomeBlock.Kind.CEILING_LIGHT,Direction.NORTH,true));
        set(l,b,6,0,-23,barrett(BarrettHomeBlock.Kind.TUB,Direction.WEST,false));set(l,b,2,0,-24,barrett(BarrettHomeBlock.Kind.TOILET,Direction.EAST,false));
        set(l,b,2,1,-23,PeopleLightsBlock.of(GRAB_RAIL,Direction.EAST));set(l,b,4,0,-25,barrett(BarrettHomeBlock.Kind.VANITY,Direction.SOUTH,false));
        set(l,b,MIRROR,PeopleLightsBlock.of(PeopleLightsBlock.Kind.MIRROR,Direction.SOUTH,0));set(l,b,4,3,-22,barrett(BarrettHomeBlock.Kind.CEILING_LIGHT,Direction.NORTH,true));
        // The garage: concrete a step down, the ramp to the side door, the open door south onto the driveway, the way on in the back wall.
        box(l,b,9,-2,-21,15,4,-9,siding);box(l,b,9,-1,-20,14,3,-10,air);box(l,b,9,-2,-20,14,-2,-10,mat("concrete"));box(l,b,9,5,-21,15,5,-9,mat("shingle"));
        set(l,b,9,-1,-16,Blocks.SMOOTH_STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP));set(l,b,9,-1,-17,Blocks.SMOOTH_STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP));
        set(l,b,10,-1,-16,Blocks.SMOOTH_STONE_SLAB.defaultBlockState());set(l,b,10,-1,-17,Blocks.SMOOTH_STONE_SLAB.defaultBlockState());
        box(l,b,10,-1,-9,13,2,-9,air);set(l,b,12,2,-13,barrett(BarrettHomeBlock.Kind.CEILING_LIGHT,Direction.NORTH,true));
        set(l,b,14,-1,-12,barrett(BarrettHomeBlock.Kind.SHELVES,Direction.WEST,false));set(l,b,14,-1,-14,barrett(BarrettHomeBlock.Kind.BOXES,Direction.WEST,false));
        box(l,b,WAY_X-1,-1,WAY_Z,WAY_X+1,2,WAY_Z,siding);
    }

    /** Puts the house back as it was before the evening: the mirror up, the runner down, the case on the desk, the spare chair upstairs, no offer, no cards. */
    public static void reset(ServerLevel l,BlockPos b){
        set(l,b,MIRROR,PeopleLightsBlock.of(PeopleLightsBlock.Kind.MIRROR,Direction.SOUTH,0));
        for(var at:RUNNER_CELLS)set(l,b,at,PeopleLightsBlock.of(RUNNER,Direction.NORTH));
        caseUpstairs(l,b,true);set(l,b,SPARE_CHAIR,PeopleLightsBlock.of(CHAIR,Direction.SOUTH));
        set(l,b,OFFER_AT,Blocks.AIR.defaultBlockState());set(l,b,TABLE.above(),Blocks.AIR.defaultBlockState());
        for(var at:List.of(CHAIR_BY_SOFA,BED_CHAIR,new BlockPos(9,-1,-13)))if(PeopleLightsBlock.is(l.getBlockState(b.offset(at)),CHAIR))set(l,b,at,Blocks.AIR.defaultBlockState());
        lights(l,b,true);
    }
    /** The house's lights: on through the evening, off for the night but the hall lamp (which the reader may turn on). */
    public static void lights(ServerLevel l,BlockPos b,boolean evening){
        for(var at:List.of(new BlockPos(0,3,-8),new BlockPos(3,3,-15),new BlockPos(-3,3,-22),new BlockPos(4,3,-22),new BlockPos(5,STUDY+3,-15),new BlockPos(12,2,-13))){
            var s=l.getBlockState(b.offset(at));if(s.is(LiteraryRegistry.BARRETT_HOME.get()))set(l,b,at,s.setValue(BarrettHomeBlock.LIT,evening));}
        var lamp=l.getBlockState(b.offset(LAMP));if(lamp.is(LiteraryRegistry.BARRETT_HOME.get()))set(l,b,LAMP,lamp.setValue(BarrettHomeBlock.LIT,evening));
        var tv=l.getBlockState(b.offset(2,0,-8));if(tv.is(LiteraryRegistry.BARRETT_HOME.get()))set(l,b,new BlockPos(2,0,-8),tv.setValue(BarrettHomeBlock.LIT,evening));
    }
    public static boolean lampOn(ServerLevel l,BlockPos b){var s=l.getBlockState(b.offset(LAMP));return s.is(LiteraryRegistry.BARRETT_HOME.get())&&s.getValue(BarrettHomeBlock.LIT);}
    public static void lamp(ServerLevel l,BlockPos b,boolean on){var s=l.getBlockState(b.offset(LAMP));if(s.is(LiteraryRegistry.BARRETT_HOME.get()))set(l,b,LAMP,s.setValue(BarrettHomeBlock.LIT,on));}
    /** An empty wheelchair standing at a spot, facing a way; or the spot cleared. */
    public static void chair(ServerLevel l,BlockPos b,BlockPos at,Direction facing,boolean there){set(l,b,at,there?PeopleLightsBlock.of(CHAIR,facing):Blocks.AIR.defaultBlockState());}
    public static void mirror(ServerLevel l,BlockPos b,boolean lowered){set(l,b,MIRROR,PeopleLightsBlock.of(PeopleLightsBlock.Kind.MIRROR,Direction.SOUTH,lowered?1:0));}
    public static boolean mirrorLowered(ServerLevel l,BlockPos b){var s=l.getBlockState(b.offset(MIRROR));return PeopleLightsBlock.is(s,PeopleLightsBlock.Kind.MIRROR)&&s.getValue(PeopleLightsBlock.STAGE)==1;}
    public static void runner(ServerLevel l,BlockPos b,boolean laid){
        for(int i=0;i<RUNNER_CELLS.size();i++){var at=RUNNER_CELLS.get(i);set(l,b,at,laid?PeopleLightsBlock.of(RUNNER,Direction.NORTH):i==RUNNER_CELLS.size()-1?PeopleLightsBlock.of(RUNNER,Direction.EAST,1):Blocks.AIR.defaultBlockState());}
    }
    public static boolean runnerLaid(ServerLevel l,BlockPos b){var s=l.getBlockState(b.offset(RUNNER_CELLS.get(0)));return PeopleLightsBlock.is(s,RUNNER)&&s.getValue(PeopleLightsBlock.STAGE)==0;}
    public static void offer(ServerLevel l,BlockPos b,boolean there){set(l,b,OFFER_AT,there?PeopleLightsBlock.of(OFFER,Direction.NORTH):Blocks.AIR.defaultBlockState());}
    public static void cards(ServerLevel l,BlockPos b,boolean dealt){set(l,b,TABLE.above(),dealt?PeopleLightsBlock.of(CARDS,Direction.NORTH,1):Blocks.AIR.defaultBlockState());}
    /** The case on Mark's desk upstairs, or on the kitchen table where he can work. */
    public static void caseUpstairs(ServerLevel l,BlockPos b,boolean upstairs){
        set(l,b,CASE_AT,upstairs?PeopleLightsBlock.of(CASE,Direction.NORTH):Blocks.AIR.defaultBlockState());
        set(l,b,CASE_DOWN,upstairs?Blocks.AIR.defaultBlockState():PeopleLightsBlock.of(CASE,Direction.WEST));
    }
    /** Mark takes the case with him when they leave: off the desk and off the table both. */
    public static void caseCarriedOut(ServerLevel l,BlockPos b){set(l,b,CASE_AT,Blocks.AIR.defaultBlockState());set(l,b,CASE_DOWN,Blocks.AIR.defaultBlockState());}
    public static boolean caseDownstairs(ServerLevel l,BlockPos b){return PeopleLightsBlock.is(l.getBlockState(b.offset(CASE_DOWN)),CASE);}
}
