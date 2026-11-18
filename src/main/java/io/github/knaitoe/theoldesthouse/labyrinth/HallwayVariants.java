package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.SlabType;

/** Two ordinary routes and one physical hazard for each part of the walk. Built whole from existing materials. */
public final class HallwayVariants {
    private HallwayVariants() {}
    private static final int FLAGS = LabyrinthBuilder.flags();
    public static final List<LabyrinthPlace> PLACES = List.of(
            LabyrinthPlace.PARALLEL_PASSAGE, LabyrinthPlace.BACK_STAIR, LabyrinthPlace.FALLING_PLASTER,
            LabyrinthPlace.CROSSED_GALLERIES, LabyrinthPlace.THREE_VAULTS, LabyrinthPlace.STEAM_WALK,
            LabyrinthPlace.BEHIND_GARAGES, LabyrinthPlace.ORCHARD_ROWS, LabyrinthPlace.DOWNED_LINE,
            LabyrinthPlace.DIVIDING_WALL, LabyrinthPlace.LOW_LINTELS, LabyrinthPlace.OPENING_SEAMS);
    public static final int[] SPANS = {-8, -17, -26}, STEAM_BAYS = {-9, -19, -29}, SEAMS = {-10, -21, -32};
    public static boolean is(LabyrinthPlace place) { return place!=null&&PLACES.contains(place); }
    public static boolean yard(LabyrinthPlace place) {
        return place == LabyrinthPlace.BEHIND_GARAGES || place == LabyrinthPlace.ORCHARD_ROWS || place == LabyrinthPlace.DOWNED_LINE;
    }
    public static boolean hazard(LabyrinthPlace place) {
        return place == LabyrinthPlace.FALLING_PLASTER || place == LabyrinthPlace.STEAM_WALK
                || place == LabyrinthPlace.DOWNED_LINE || place == LabyrinthPlace.OPENING_SEAMS;
    }
    public static boolean deep(LabyrinthPlace place) {
        return place == LabyrinthPlace.DIVIDING_WALL || place == LabyrinthPlace.LOW_LINTELS || place == LabyrinthPlace.OPENING_SEAMS;
    }
    public static int firstDepth(LabyrinthPlace place) {
        if (deep(place)) return 16;
        if (place == LabyrinthPlace.STEAM_WALK) return 10;
        if (place == LabyrinthPlace.CROSSED_GALLERIES || place == LabyrinthPlace.THREE_VAULTS) return 9;
        if (yard(place) || place == LabyrinthPlace.FALLING_PLASTER) return 6;
        return 0;
    }
    public static int weight(LabyrinthPlace place, int depth) {
        if (depth < firstDepth(place)) return 0;
        if (place == LabyrinthPlace.PARALLEL_PASSAGE || place == LabyrinthPlace.BACK_STAIR || place == LabyrinthPlace.FALLING_PLASTER)
            return depth >= 9 ? 0 : hazard(place) ? 6 : 12;
        if (place == LabyrinthPlace.CROSSED_GALLERIES || place == LabyrinthPlace.THREE_VAULTS || place == LabyrinthPlace.STEAM_WALK)
            return depth >= DarkHalls.ONLY_DEPTH ? 0 : hazard(place) ? 7 : 14;
        return hazard(place) ? 7 : 14;
    }

    static BlockState plaster() { return HallRegistry.surface("plaster").defaultBlockState(); }
    static BlockState boards() { return HallRegistry.surface("floor_worn").defaultBlockState(); }
    static BlockState stone() { return HallRegistry.stone("ashlar").defaultBlockState(); }
    static BlockState flags() { return HallRegistry.stone("flags_worn").defaultBlockState(); }
    static BlockState dark() { return DarkHallRegistry.material("ash").defaultBlockState(); }
    static BlockState darkFloor() { return DarkHallRegistry.material("floor").defaultBlockState(); }
    static BlockState air() { return Blocks.AIR.defaultBlockState(); }
    static void set(ServerLevel l, BlockPos b, int x, int y, int z, BlockState s) { BuildBlocks.set(l, b.offset(x,y,z), s, FLAGS); }
    static void box(ServerLevel l, BlockPos b, int x0, int y0, int z0, int x1, int y1, int z1, BlockState s) {
        BuildBlocks.box(l, b.offset(x0,y0,z0), b.offset(x1,y1,z1), s, FLAGS);
    }
    private static void lamp(ServerLevel l, BlockPos b, int x, int y, int z) { set(l,b,x,y,z,TrialBlock.of(TrialBlock.Kind.STILL_LAMP,1)); }
    private static void shell(ServerLevel l, BlockPos b, LabyrinthPlace p, BlockState wall) {
        var r = p.room(); box(l,b,r.minX(),r.minY(),r.minZ(),r.maxX(),r.maxY(),0,wall);
    }
    /** A lined passage, inclusive, with feet at y and the roof at y+height. */
    private static void passage(ServerLevel l, BlockPos b, int x0, int x1, int z0, int z1, int y, int height,
                                BlockState floor, BlockState roof) {
        box(l,b,x0,y,z0,x1,y+height-1,z1,air());
        box(l,b,x0,y-1,z0,x1,y-1,z1,floor); box(l,b,x0,y+height,z0,x1,y+height,z1,roof);
    }
    private static void rail(ServerLevel l, BlockPos b, int x, int y, int z, Direction side) {
        set(l,b,x,y,z,OrdinaryHallBlock.of(OrdinaryHallBlock.Kind.BALUSTRADE,side));
    }
    /** Half-block treads, solid underneath, ascending north. Half count is the feet above the supplied floor. */
    private static void tread(ServerLevel l, BlockPos b, int x0, int x1, int z, int floorY, int halves, BlockState full, BlockState half) {
        int top = floorY + halves / 2;
        if (top > floorY) box(l,b,x0,floorY,z,x1,top-1,z,full);
        if (halves % 2 != 0) box(l,b,x0,top,z,x1,top,z,half);
    }

    public static void build(ServerLevel level, BlockPos base, LabyrinthPlace place) {
        switch (place) {
            case PARALLEL_PASSAGE -> parallel(level,base,place);
            case BACK_STAIR -> backStair(level,base,place);
            case FALLING_PLASTER -> plasterHall(level,base,place);
            case CROSSED_GALLERIES -> crossed(level,base,place);
            case THREE_VAULTS -> vaults(level,base,place);
            case STEAM_WALK -> steam(level,base,place);
            case BEHIND_GARAGES -> garages(level,base,place);
            case ORCHARD_ROWS -> orchard(level,base,place);
            case DOWNED_LINE -> line(level,base,place);
            case DIVIDING_WALL -> dividing(level,base,place);
            case LOW_LINTELS -> lintels(level,base,place);
            case OPENING_SEAMS -> seams(level,base,place);
            default -> throw new IllegalArgumentException(place.id());
        }
        BlockState wall = deep(place) ? dark() : yard(place) ? PuzzleRegistry.yardMaterial("clapboard").defaultBlockState()
                : firstDepth(place) >= 9 ? stone() : plaster();
        BlockState floor = deep(place) ? darkFloor() : yard(place) ? PuzzleRegistry.yardMaterial("wet_gravel").defaultBlockState()
                : firstDepth(place) >= 9 ? flags() : boards();
        LabyrinthBuilder.entrance(level,base,wall,floor,wall);
        LabyrinthBuilder.doors(level,base,place);
        if (yard(place)) BuildBlocks.after(level,() -> YardLights.mount(level,base,place));
        if (hazard(place)) BuildBlocks.after(level,() -> HallwayHazards.fresh(level,base,place));
    }

    private static void parallel(ServerLevel l, BlockPos b, LabyrinthPlace p) {
        shell(l,b,p,plaster());
        passage(l,b,-3,-1,-33,-1,0,4,boards(),HallRegistry.surface("ceiling").defaultBlockState());
        passage(l,b,1,3,-33,-5,0,4,boards(),HallRegistry.surface("ceiling").defaultBlockState());
        passage(l,b,-1,0,-4,-1,0,4,boards(),plaster());
        var glass = Blocks.GLASS_PANE.defaultBlockState().setValue(IronBarsBlock.NORTH,true).setValue(IronBarsBlock.SOUTH,true);
        for (int z=-6;z>=-32;z--) {
            set(l,b,0,0,z,HallRegistry.surface("skirting").defaultBlockState());
            for (int y=1;y<=2;y++) set(l,b,0,y,z,z%6==0?plaster():glass);
        }
        for (int z : new int[]{-11,-27}) {
            var door = Blocks.SPRUCE_DOOR.defaultBlockState().setValue(DoorBlock.FACING,Direction.EAST);
            set(l,b,0,0,z,door.setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER));
            set(l,b,0,1,z,door.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));
        }
        // One way on halfway down the first lane; the other doubles back in the lane beyond the glass.
        passage(l,b,-4,-3,-19,-17,0,3,boards(),plaster());
        passage(l,b,3,4,-9,-7,0,3,boards(),plaster());
        for (int x:new int[]{-2,2}) for(int z:new int[]{-5,-17,-30}) lamp(l,b,x,3,z);
    }

    private static void backStair(ServerLevel l, BlockPos b, LabyrinthPlace p) {
        shell(l,b,p,plaster());
        passage(l,b,-2,5,-23,-1,0,9,boards(),HallRegistry.surface("ceiling").defaultBlockState());
        // The first stair folds back above the passage through which the reader entered.
        var half=Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.BOTTOM);
        for(int n=1;n<=8;n++) tread(l,b,3,5,-4-n,0,n,boards(),half);
        box(l,b,-5,3,-15,5,3,-13,boards()); box(l,b,-5,4,-15,5,8,-13,air());
        box(l,b,-2,3,-12,0,3,-6,boards());
        for(int z=-6;z>=-12;z--) rail(l,b,0,4,z,Direction.EAST);
        // A second flight climbs beside the upper passage; its far end can be walked under.
        for(int n=1;n<=8;n++) tread(l,b,3,5,-15-n,4,n,boards(),half);
        passage(l,b,-6,-3,-15,-13,4,4,boards(),plaster());
        passage(l,b,0,2,-23,-16,4,4,boards(),plaster());
        passage(l,b,2,6,-23,-21,4,3,boards(),plaster());
        for(int z=-6;z>=-12;z--) rail(l,b,2,Math.max(0,(-z-4)/2),z,Direction.WEST);
        for(var at:List.of(new BlockPos(0,8,-3),new BlockPos(-3,7,-14),new BlockPos(0,7,-22)))lamp(l,b,at.getX(),at.getY(),at.getZ());
    }

    private static void plasterHall(ServerLevel l, BlockPos b, LabyrinthPlace p) {
        shell(l,b,p,plaster()); passage(l,b,-1,1,-33,-1,0,4,boards(),plaster());
        for(int i=0;i<SPANS.length;i++) {
            int z=SPANS[i],side=i%2==0?1:-1;
            passage(l,b,side>0?2:-3,side>0?3:-2,z-1,z+1,0,3,boards(),plaster());
            for(int x=-1;x<=1;x++)for(int dz=-1;dz<=1;dz++)set(l,b,x,3,z+dz,HallwayFittingBlock.of(HallwayFittingBlock.Kind.PLASTER,0));
            box(l,b,-1,3,z-2,1,3,z-2,HallRegistry.surface("rail").defaultBlockState());
            box(l,b,-1,3,z+2,1,3,z+2,HallRegistry.surface("rail").defaultBlockState());
            lamp(l,b,side*3,2,z);
        }
        lamp(l,b,0,3,-3); lamp(l,b,0,3,-32);
    }

    private static void crossed(ServerLevel l, BlockPos b, LabyrinthPlace p) {
        shell(l,b,p,stone()); passage(l,b,-2,2,-29,-1,0,6,flags(),stone());
        passage(l,b,-9,9,-16,-14,-4,7,flags(),stone());
        // The bridge retains its floor across the lower gallery, with open sightlines at either edge.
        box(l,b,-2,-1,-17,2,-1,-13,flags());
        for(int x:new int[]{-3,3})box(l,b,x,0,-17,x,5,-13,air());
        for(int z=-13;z>=-17;z--)for(int x:new int[]{-2,2})rail(l,b,x,0,z,x<0?Direction.WEST:Direction.EAST);
        passage(l,b,-8,-2,-5,-3,0,5,flags(),stone());
        box(l,b,-8,-4,-13,-6,6,-6,air()); box(l,b,-8,-5,-13,-6,-5,-6,flags());
        var half=OrdinaryHallBlock.of(OrdinaryHallBlock.Kind.STEP,Direction.SOUTH);
        for(int n=0;n<8;n++)tread(l,b,-8,-6,-6-n,-4,7-n,stone(),half);
        // The lower walk continues out east, under the bridge.
        passage(l,b,9,9,-16,-14,-4,4,flags(),stone());
        lamp(l,b,0,5,-5);lamp(l,b,0,5,-25);lamp(l,b,8,1,-15);lamp(l,b,-7,4,-8);
    }

    private static void vaults(ServerLevel l, BlockPos b, LabyrinthPlace p) {
        shell(l,b,p,stone());
        for(int i=0;i<3;i++) {
            int centre=-7-i*14;
            for(int x=-6;x<=6;x++)for(int z=centre-6;z<=centre+6;z++) {
                int top=8-(i%2==0?Math.abs(x):Math.abs(z-centre))/2;
                passage(l,b,x,x,z,z,0,top,flags(),stone());
            }
            lamp(l,b,i%2==0?-6:6,3,centre);
        }
        for(int i=0;i<2;i++) {
            int z=-14-i*14,side=i==0?-1:1;
            passage(l,b,side<0?-5:3,side<0?-3:5,z,z,0,3,flags(),stone());
        }
        passage(l,b,-1,1,-43,-41,0,3,flags(),stone());
        passage(l,b,-7,-6,-22,-20,0,3,flags(),stone());
    }

    private static void steam(ServerLevel l, BlockPos b, LabyrinthPlace p) {
        shell(l,b,p,stone()); passage(l,b,-1,1,-35,-1,0,5,flags(),stone());
        for(int z=-3;z>=-33;z--)set(l,b,-2,2,z,TankBlock.of(TankBlock.Kind.PIPE,Direction.EAST,0));
        for(int z:STEAM_BAYS) {
            passage(l,b,2,3,z-2,z+2,0,3,flags(),stone());
            set(l,b,-2,1,z,TankBlock.of(TankBlock.Kind.OUTLET,Direction.EAST,0));
            set(l,b,-2,2,z,TankBlock.of(TankBlock.Kind.PIPE,Direction.EAST,2));
            lamp(l,b,3,2,z);
        }
        lamp(l,b,0,4,-3);lamp(l,b,0,4,-34);
    }

    private static void yardGround(ServerLevel l, BlockPos b, LabyrinthPlace p) {
        var r=p.room(); box(l,b,r.minX(),-3,r.minZ(),r.maxX(),-2,0,Blocks.DIRT.defaultBlockState());
        box(l,b,r.minX(),-1,r.minZ(),r.maxX(),-1,0,Blocks.GRASS_BLOCK.defaultBlockState());
        box(l,b,r.minX(),0,r.minZ(),r.maxX(),r.maxY(),0,air());
        // Dense tree banks end the view inside the authored bounds; the routes are cut through them below.
        var leaves=Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true);
        for(int x=r.minX();x<=r.maxX();x++)for(int z=r.minZ();z<=0;z++)
            if(x<r.minX()+2||x>r.maxX()-2||z<r.minZ()+2||z>-2)box(l,b,x,0,z,x,9,z,leaves);
    }
    private static void path(ServerLevel l,BlockPos b,int x0,int x1,int z0,int z1) {
        box(l,b,x0,0,z0,x1,3,z1,air());box(l,b,x0,-1,z0,x1,-1,z1,PuzzleRegistry.yardMaterial("wet_gravel").defaultBlockState());
    }
    private static void bulb(ServerLevel l,BlockPos b,int x,int y,int z) { set(l,b,x,y,z,YardBlock.of(YardBlock.Kind.BULB,Direction.SOUTH,0)); }
    private static void garages(ServerLevel l,BlockPos b,LabyrinthPlace p) {
        yardGround(l,b,p);path(l,b,-2,2,-27,-1);path(l,b,-10,10,-38,-26);
        var siding=PuzzleRegistry.yardMaterial("clapboard").defaultBlockState();
        for(int side:new int[]{-1,1}) {
            int x0=side<0?-10:4,x1=side<0?-4:10;
            box(l,b,x0,0,-24,x1,4,-8,siding); box(l,b,x0,5,-25,x1,5,-7,Blocks.DARK_OAK_SLAB.defaultBlockState());
            for(int z=-9;z>=-23;z--)set(l,b,side*3,3,z,TankBlock.of(TankBlock.Kind.PIPE,side<0?Direction.EAST:Direction.WEST,0));
            for(int y=0;y<=3;y++)set(l,b,side*3,y,-24,TankBlock.of(TankBlock.Kind.PIPE,side<0?Direction.EAST:Direction.WEST,1));
            bulb(l,b,side*3,2,-10);
        }
        box(l,b,-3,4,-19,3,4,-12,Blocks.DARK_OAK_SLAB.defaultBlockState());
        box(l,b,-1,4,-17,1,4,-15,air());
        path(l,b,-13,-9,-29,-27);path(l,b,-1,1,-39,-37);
        bulb(l,b,-7,3,-29);bulb(l,b,2,3,-37);
    }
    private static void orchard(ServerLevel l,BlockPos b,LabyrinthPlace p) {
        yardGround(l,b,p);path(l,b,-1,1,-5,-1);path(l,b,-4,4,-6,-4);
        path(l,b,-4,-2,-37,-6);path(l,b,2,4,-37,-6);path(l,b,-4,4,-40,-36);path(l,b,-1,1,-43,-39);
        var leaves=Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true);
        for(int x:new int[]{-12,-6,6,12})for(int z:new int[]{-8,-16,-24,-32}) {
            box(l,b,x,0,z,x,3,z,Blocks.OAK_LOG.defaultBlockState());
            box(l,b,x-2,2,z-2,x+2,3,z+2,leaves);box(l,b,x-1,4,z-1,x+1,4,z+1,leaves);
        }
        path(l,b,3,16,-23,-21);
        // A small open equipment shelter beside the gate, with storage that can be opened.
        box(l,b,-11,0,-41,-11,3,-37,Blocks.SPRUCE_PLANKS.defaultBlockState());
        for(int x:new int[]{-11,-7})for(int z:new int[]{-41,-37})box(l,b,x,0,z,x,3,z,Blocks.SPRUCE_LOG.defaultBlockState());
        box(l,b,-12,4,-42,-6,4,-36,Blocks.DARK_OAK_SLAB.defaultBlockState());
        set(l,b,-10,0,-40,Blocks.BARREL.defaultBlockState());path(l,b,-9,-4,-39,-38);
        // Mown rows keep their low soil surface; the foliage above and beside them does not become a hedge maze.
        var gravel=PuzzleRegistry.yardMaterial("wet_gravel").defaultBlockState();
        for(int x=-16;x<=16;x++)for(int z=-43;z<=-1;z++)
            if(BuildBlocks.state(l,b.offset(x,-1,z)).equals(gravel))set(l,b,x,-1,z,Blocks.DIRT_PATH.defaultBlockState());
        for(int x=-14;x<=14;x++)if(Math.abs(x)>1)set(l,b,x,0,-42,Blocks.SPRUCE_FENCE.defaultBlockState());
        // 0.4.713 (run 1735): the gate's bulb was over the open gateway with nothing to hang from; it stands on the gatepost now,
        // raised to a post by YardLights.
        bulb(l,b,-7,2,-38);bulb(l,b,2,3,-42);
    }
    public static final BlockPos LINE_LAMP = new BlockPos(3,4,-18);
    private static void line(ServerLevel l,BlockPos b,LabyrinthPlace p) {
        yardGround(l,b,p);path(l,b,-2,2,-35,-1);path(l,b,-7,2,-8,-6);path(l,b,-7,2,-31,-29);
        // The dry bank is raised one block, with half steps at both ends, reachable from either approach.
        box(l,b,-7,0,-28,-5,0,-10,Blocks.DIRT.defaultBlockState());
        box(l,b,-7,1,-28,-5,4,-10,air());
        var half=Blocks.SPRUCE_SLAB.defaultBlockState();
        for(int z:new int[]{-9,-29})box(l,b,-7,0,z,-5,0,z,half);
        for(int x:new int[]{-3,3})box(l,b,x,0,-19,x,6,-19,Blocks.SPRUCE_LOG.defaultBlockState());
        for(int x=-2;x<=2;x++)set(l,b,x,0,-19,HallwayFittingBlock.of(HallwayFittingBlock.Kind.CABLE,0));
        for(int y=0;y<=5;y++)set(l,b,2,y,-19,PowerBlock.of(PowerBlock.Kind.CABLE_LIGHT,Direction.WEST,1));
        set(l,b,LINE_LAMP.getX(),LINE_LAMP.getY(),LINE_LAMP.getZ(),PowerBlock.of(PowerBlock.Kind.WORK_LAMP,Direction.SOUTH,1));
        // The shallow puddle is beside the crossing. The whole wet strip is dangerous when the wire arcs.
        box(l,b,-2,-1,-20,1,-1,-18,PuzzleRegistry.yardMaterial("wet_gravel").defaultBlockState());
        set(l,b,-2,-1,-20,Blocks.WATER.defaultBlockState());set(l,b,1,-1,-18,Blocks.WATER.defaultBlockState());
        bulb(l,b,1,3,-4);bulb(l,b,-1,3,-33);
    }

    private static void dividing(ServerLevel l,BlockPos b,LabyrinthPlace p) {
        shell(l,b,p,dark());passage(l,b,-10,10,-41,-1,0,15,darkFloor(),DarkHallRegistry.material("ceiling").defaultBlockState());
        box(l,b,-7,0,-33,7,14,-6,dark());
        passage(l,b,-1,1,-10,-6,0,3,darkFloor(),dark());
    }
    private static void lintels(ServerLevel l,BlockPos b,LabyrinthPlace p) {
        shell(l,b,p,dark());
        for(int centre:new int[]{-6,-18,-30,-42})passage(l,b,-7,7,centre-5,centre+5,0,14,darkFloor(),dark());
        for(int i=0;i<3;i++) {
            int z=-12-i*12;passage(l,b,i%2==0?4:-6,i%2==0?6:-4,z,z,0,2,darkFloor(),dark());
        }
    }
    private static void seams(ServerLevel l,BlockPos b,LabyrinthPlace p) {
        shell(l,b,p,dark());passage(l,b,-2,2,-37,-1,0,10,darkFloor(),dark());
        for(int z:SEAMS) {
            box(l,b,-2,-4,z,4,-2,z+1,air());box(l,b,-2,-5,z,4,-5,z+1,darkFloor());
            box(l,b,3,-4,z,4,2,z+9,air());box(l,b,3,-5,z,4,-5,z+9,darkFloor());
            for(int n=1;n<=8;n++)tread(l,b,3,4,z+1+n,-4,n,darkFloor(),HallwayFittingBlock.of(HallwayFittingBlock.Kind.DEEP_STEP,0));
            // From the last tread a reader steps west onto the preceding waiting bank.
            box(l,b,2,0,z+8,4,2,z+9,air());
            seamFloor(l,b,z,0);
        }
    }
    static void seamFloor(ServerLevel l,BlockPos b,int z,int stage) {
        box(l,b,-2,-1,z,2,-1,z+1,stage==2?air():HallwayFittingBlock.of(HallwayFittingBlock.Kind.SEAM,stage));
    }
}
