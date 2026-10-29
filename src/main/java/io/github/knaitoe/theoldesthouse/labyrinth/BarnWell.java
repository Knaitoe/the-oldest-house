package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;

/**
 * The farm round the barn well, built whole (0.4.164). The owner's notes: "Improve clutter in barn_well shed", "Fix fencing
 * in barn_well barn", "Give barn custom texture? Fix the mess it is right now." Until now the farm was the old room and
 * seven later passes laid over it (BarnFarm, Farmstead, VignetteArchitecture, SceneCraft, SceneShells, SceneSupportRepairs,
 * ScenePolish), each fixing the one before: pen rails stood in the barn's wall line under walls left hanging above them, a
 * beam floated by the well, trees grew at the barn's eaves. It is now one build, which none of those passes touch.
 * <p>
 * Every address the well's story and the farm's stock use is where it was: the shaft, its ladder, the cover and the
 * initials ({@link NovelRooms#WELL}, {@link NovelRooms#CARVING}), the paper on its lectern, the ribbon barrel
 * ({@link NovelRooms#RIBBON}), the feed barrel by the barn door ({@link Farmstead#FOOD}), the two pens and the animals'
 * places in them, the dog's and the cat's places, and the shed's back wall, where the onward door is set.
 */
public final class BarnWell {
    private static final int F=LabyrinthBuilder.flags();
    private BarnWell(){}
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,Block s){set(l,b,x,y,z,s.defaultBlockState());}
    private static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){
        for(int x=Math.min(x0,x1);x<=Math.max(x0,x1);x++)for(int y=Math.min(y0,y1);y<=Math.max(y0,y1);y++)for(int z=Math.min(z0,z1);z<=Math.max(z0,z1);z++)set(l,b,x,y,z,s);
    }
    private static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,Block s){box(l,b,x0,y0,z0,x1,y1,z1,s.defaultBlockState());}
    private static BlockState log(Block log,Direction.Axis axis){return log.defaultBlockState().setValue(RotatedPillarBlock.AXIS,axis);}
    private static BlockState stairs(Block stairs,Direction facing){return stairs.defaultBlockState().setValue(StairBlock.FACING,facing);}
    private static BlockState slab(Block slab,SlabType type){return slab.defaultBlockState().setValue(SlabBlock.TYPE,type);}
    private static BlockState prop(FarmPropBlock.Kind k,Direction facing){return FarmPropBlock.of(k,facing);}
    private static BlockState siding(FarmMaterialBlock.Kind k){return FarmMaterialBlock.of(k);}
    private static boolean mossy(int x,int y,int z){return Math.floorMod(x*31+y*17+z*13,5)==0;}

    public static void build(ServerLevel l,BlockPos b){
        ground(l,b);paths(l,b);barn(l,b);well(l,b);shed(l,b);field(l,b);yard(l,b);
        Landscapes.barnForest(l,b,false);yardTree(l,b,-5,-4);
        NovelRooms.safeApproach(l,b);
        reshape(l,b);
    }

    /** Flat farm ground, and the bank rising beyond it into the wood, as the old dressing had it, with no wall of barriers. */
    private static void ground(ServerLevel l,BlockPos b){
        for(int x=-17;x<=17;x++)for(int z=-39;z<=0;z++){set(l,b,x,-2,z,Blocks.DIRT);set(l,b,x,-1,z,Blocks.GRASS_BLOCK);}
        for(int x=-23;x<=23;x++)for(int z=-45;z<=4;z++){
            if(x>=-15&&x<=15&&z>=-37&&z<=-1)continue;
            if(Math.abs(x)<4&&z>=-1)continue;
            int edge=Math.max(Math.abs(x)-15,Math.max(-z-37,z+1));
            int height=Math.min(5,Math.max(0,edge/2))+(Landscapes.noise(x,z,7)>0?1:0);
            for(int y=-2;y<=height;y++)set(l,b,x,y,z,y==height?Blocks.PODZOL:Blocks.DIRT);
        }
    }
    private static void paths(ServerLevel l,BlockPos b){
        // From the gate up the yard; the barn's way off to the right, the shed's to the left, and the well straight on.
        for(int z=-1;z>=-14;z--)for(int x=-1;x<=1;x++)set(l,b,x,-1,z,Blocks.DIRT_PATH);
        for(int x=2;x<=12;x++)for(int z=-14;z>=-15;z--)set(l,b,x,-1,z,Blocks.DIRT_PATH);
        for(int x=7;x<=12;x++)set(l,b,x,-1,-16,Blocks.COARSE_DIRT);
        for(int x=-7;x<=-2;x++)for(int z=-11;z>=-12;z--)set(l,b,x,-1,z,Blocks.DIRT_PATH);
        for(int z=-15;z>=-21;z--)for(int x=-1;x<=1;x++)set(l,b,x,-1,z,Math.floorMod(x*3+z,4)==0?Blocks.COARSE_DIRT:Blocks.GRAVEL);
        for(int x=-6;x<=-2;x++)set(l,b,x,-1,-24,Blocks.DIRT_PATH);
        // The paper the farm left out for whoever comes, on its lectern by the well path.
        NovelRooms.lectern(l,b.offset(-4,0,-17),NovelTexts.well());
    }

    // ---------------------------------------------------------------------------------------------------- the barn
    /** The gambrel's height over each column, x 4 to 16: three steep courses of stairs, then slabs, a dark oak ridge at x 10. */
    private static int roofY(int x){int d=Math.min(x-4,16-x);return x==10?8:d<=2?4+d:7+(d-3)/2;}
    private static BlockState roof(int x){
        int d=Math.min(x-4,16-x);
        if(x==10)return log(Blocks.DARK_OAK_LOG,Direction.Axis.Z);
        if(d<=2)return stairs(Blocks.DARK_OAK_STAIRS,x<10?Direction.EAST:Direction.WEST);
        return slab(Blocks.DARK_OAK_SLAB,(d-3)%2==0?SlabType.BOTTOM:SlabType.TOP);
    }
    private static boolean topSlab(int x){int d=Math.min(x-4,16-x);return x!=10&&d>2&&(d-3)%2==1;}
    private static void barn(ServerLevel l,BlockPos b){
        // Floor: the pens bedded in coarse earth and straw, packed earth in the aisle with one trodden track down its middle
        // (a path block is a pixel low, so nothing stands on it).
        for(int x=6;x<=14;x++)for(int z=-33;z<=-18;z++){
            boolean pen=(x<=7||x>=13)&&z<=-22;
            set(l,b,x,-1,z,x==10&&z>=-32?Blocks.DIRT_PATH:pen&&Math.floorMod(x*7+z*3,6)==0?Blocks.HAY_BLOCK:Blocks.COARSE_DIRT);
        }
        // Stone footing, red board-and-batten above it, white boards at the corners.
        for(int x=5;x<=15;x++)for(int z=-34;z<=-17;z++){
            if(x!=5&&x!=15&&z!=-34&&z!=-17)continue;
            set(l,b,x,-1,z,Blocks.COBBLESTONE);
            boolean corner=(x==5||x==15)&&(z==-34||z==-17);
            set(l,b,x,0,z,mossy(x,0,z)?Blocks.MOSSY_COBBLESTONE:Blocks.COBBLESTONE);
            for(int y=1;y<=4;y++)set(l,b,x,y,z,siding(corner?FarmMaterialBlock.Kind.TRIM:FarmMaterialBlock.Kind.SIDING));
        }
        // Windows high in the long walls and the back, above anything an animal could reach.
        for(int z:new int[]{-31,-27,-23}){set(l,b,5,2,z,siding(FarmMaterialBlock.Kind.WINDOW));set(l,b,15,2,z,siding(FarmMaterialBlock.Kind.WINDOW));}
        set(l,b,10,2,-34,siding(FarmMaterialBlock.Kind.WINDOW));
        // The big doorway, four wide, its two doors slid back along the wall either side of it, a white header over it.
        box(l,b,8,0,-17,11,3,-17,Blocks.AIR);
        for(int x:new int[]{6,7,12,13})for(int y=0;y<=3;y++)set(l,b,x,y,-17,siding(FarmMaterialBlock.Kind.DOOR));
        for(int x=6;x<=13;x++)set(l,b,x,4,-17,siding(FarmMaterialBlock.Kind.TRIM));
        // The gables, filled up under the roof, and the roof: a gambrel from eave to ridge, a board's overhang at each end.
        for(int z:new int[]{-34,-17})for(int x=6;x<=14;x++){
            int top=roofY(x);for(int y=5;y<top;y++)set(l,b,x,y,z,siding(FarmMaterialBlock.Kind.SIDING));
            // Where the roof is a top slab the gable rises to its top: a dark board there, so it reads as roof from above.
            if(topSlab(x))set(l,b,x,top,z,Blocks.DARK_OAK_PLANKS);
        }
        for(int x=4;x<=16;x++)for(int z=-35;z<=-16;z++){
            if((z==-34||z==-17)&&topSlab(x)&&x>=6&&x<=14)continue;
            set(l,b,x,roofY(x),z,roof(x));
        }
        // The hay door high in the south gable, its jambs, and the hoist beam run out from the ridge with its chain.
        box(l,b,9,5,-17,11,6,-17,Blocks.AIR);
        for(int y=5;y<=6;y++){set(l,b,8,y,-17,siding(FarmMaterialBlock.Kind.TRIM));set(l,b,12,y,-17,siding(FarmMaterialBlock.Kind.TRIM));}
        set(l,b,10,8,-15,log(Blocks.DARK_OAK_LOG,Direction.Axis.Z));set(l,b,10,7,-15,Blocks.CHAIN);
        // The loft over the front half, on a beam and two posts; hay stacked under the eaves, sacks, the loft's low rail.
        box(l,b,6,4,-25,14,4,-18,Blocks.SPRUCE_PLANKS);
        for(int x=6;x<=14;x++)set(l,b,x,3,-25,log(Blocks.STRIPPED_SPRUCE_LOG,Direction.Axis.X));
        for(int x:new int[]{8,12})box(l,b,x,0,-25,x,2,-25,log(Blocks.STRIPPED_SPRUCE_LOG,Direction.Axis.Y));
        for(int z=-24;z<=-19;z++){set(l,b,6,5,z,Blocks.HAY_BLOCK);if(z<=-21){set(l,b,7,5,z,Blocks.HAY_BLOCK);set(l,b,7,6,z,Blocks.HAY_BLOCK);}}
        for(int z=-24;z<=-22;z++)set(l,b,14,5,z,Blocks.HAY_BLOCK);
        set(l,b,13,5,-19,prop(FarmPropBlock.Kind.SEED_SACKS,Direction.WEST));
        for(int x=6;x<=14;x++)set(l,b,x,5,-25,Blocks.SPRUCE_FENCE);
        // Two pens inside the long walls, never on them: a rail and a gate on the aisle side of each, a rail across the front.
        for(int z=-33;z<=-22;z++)if(z!=-25){set(l,b,8,0,z,Blocks.SPRUCE_FENCE);set(l,b,12,0,z,Blocks.SPRUCE_FENCE);}
        for(int x=6;x<=8;x++)set(l,b,x,0,-21,Blocks.SPRUCE_FENCE);
        for(int x=12;x<=14;x++)set(l,b,x,0,-21,Blocks.SPRUCE_FENCE);
        set(l,b,8,0,-29,Blocks.SPRUCE_FENCE_GATE.defaultBlockState().setValue(FenceGateBlock.FACING,Direction.EAST));
        set(l,b,12,0,-29,Blocks.SPRUCE_FENCE_GATE.defaultBlockState().setValue(FenceGateBlock.FACING,Direction.WEST));
        // In the pens: a trough at the back of each, the east pen's water.
        set(l,b,7,0,-33,prop(FarmPropBlock.Kind.TROUGH,Direction.SOUTH));set(l,b,13,0,-33,prop(FarmPropBlock.Kind.TROUGH,Direction.SOUTH));
        set(l,b,14,0,-31,Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL,3));
        // The aisle: the harness on the back wall, a pitchfork leant beside it; lanterns from the ridge and the loft.
        set(l,b,10,1,-33,prop(FarmPropBlock.Kind.HARNESS,Direction.SOUTH));set(l,b,9,0,-33,prop(FarmPropBlock.Kind.PITCHFORK,Direction.SOUTH));
        set(l,b,10,7,-29,Blocks.CHAIN);set(l,b,10,6,-29,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
        set(l,b,10,3,-21,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
        // Inside the door: the feed barrel (Farmstead.FOOD), hay bales, milk cans, sacks.
        set(l,b,7,0,-18,Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING,Direction.UP));
        set(l,b,6,0,-18,Blocks.HAY_BLOCK);set(l,b,6,1,-18,Blocks.HAY_BLOCK);set(l,b,6,0,-19,Blocks.HAY_BLOCK);
        set(l,b,13,0,-18,prop(FarmPropBlock.Kind.MILK_CANS,Direction.NORTH));set(l,b,14,0,-19,prop(FarmPropBlock.Kind.SEED_SACKS,Direction.WEST));
        // Outside the door, a barrow left on the apron.
        set(l,b,13,0,-15,prop(FarmPropBlock.Kind.WHEELBARROW,Direction.WEST));
    }

    // ---------------------------------------------------------------------------------------------------- the well
    private static void well(ServerLevel l,BlockPos b){
        // The shaft, its ladder hung from the north side, the bottom stone, the initials and the cover (as before).
        for(int y=-12;y<=0;y++)for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)
            if(dx!=0||dz!=0)set(l,b,dx,y,-23+dz,Blocks.MOSSY_COBBLESTONE);
        for(int y=-12;y<=1;y++)set(l,b,0,y,-23,Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.SOUTH));
        set(l,b,0,-13,-23,Blocks.MOSSY_COBBLESTONE);set(l,b,0,1,-24,Blocks.MOSSY_COBBLESTONE);
        BuildBlocks.set(l,b.offset(NovelRooms.CARVING),NovelRegistry.CARVINGS.get().defaultBlockState(),F);
        NovelRooms.cover(l,b,false);
        for(int x=-2;x<=2;x++)for(int z=-25;z<=-21;z++)if(Math.abs(x)==2||Math.abs(z+23)==2)set(l,b,x,-1,z,Blocks.GRAVEL);
        // Two posts carry the roof; the windlass spans between them above the mouth, three blocks up, so a reader climbs
        // clear of the shaft under it; its rope runs down to the bucket resting on the curb.
        for(int x:new int[]{-2,2})box(l,b,x,0,-23,x,5,-23,log(Blocks.STRIPPED_SPRUCE_LOG,Direction.Axis.Y));
        for(int x=-1;x<=1;x++)set(l,b,x,4,-23,log(Blocks.SPRUCE_LOG,Direction.Axis.X));
        set(l,b,1,3,-23,Blocks.CHAIN);set(l,b,1,2,-23,Blocks.CHAIN);set(l,b,1,1,-23,Blocks.CAULDRON);
        // A small gabled roof, its ridge along the windlass.
        for(int x=-3;x<=3;x++){
            set(l,b,x,4,-25,stairs(Blocks.SPRUCE_STAIRS,Direction.SOUTH));set(l,b,x,5,-24,stairs(Blocks.SPRUCE_STAIRS,Direction.SOUTH));
            set(l,b,x,6,-23,slab(Blocks.SPRUCE_SLAB,SlabType.BOTTOM));
            set(l,b,x,5,-22,stairs(Blocks.SPRUCE_STAIRS,Direction.NORTH));set(l,b,x,4,-21,stairs(Blocks.SPRUCE_STAIRS,Direction.NORTH));
        }
        // The ribbon barrel beside it (NovelRooms.RIBBON).
        set(l,b,NovelRooms.RIBBON.getX(),NovelRooms.RIBBON.getY(),NovelRooms.RIBBON.getZ(),Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING,Direction.UP));
    }

    // ---------------------------------------------------------------------------------------------------- the tool shed
    private static int shedRoofY(int x){int d=Math.min(x+16,-7-x);return 4+d/2;}
    private static boolean shedTop(int x){int d=Math.min(x+16,-7-x);return d%2==1;}
    private static void shed(ServerLevel l,BlockPos b){
        var boards=siding(FarmMaterialBlock.Kind.SHED_BOARDS);
        for(int x=-15;x<=-8;x++)for(int z=-15;z<=-7;z++){
            boolean wall=x==-15||x==-8||z==-15||z==-7,corner=(x==-15||x==-8)&&(z==-15||z==-7);
            set(l,b,x,-1,z,wall?Blocks.COBBLESTONE:Blocks.SPRUCE_PLANKS);
            if(wall)for(int y=0;y<=3;y++)set(l,b,x,y,z,corner?log(Blocks.STRIPPED_SPRUCE_LOG,Direction.Axis.Y):boards);
        }
        // The side walls' top course is the roof's dark plate under its first slab; the gables are boarded up under the roof,
        // a dark board where the roof is a top slab, so nothing grey shows from above.
        for(int z=-15;z<=-7;z++){set(l,b,-15,4,z,Blocks.DARK_OAK_PLANKS);set(l,b,-8,4,z,Blocks.DARK_OAK_PLANKS);}
        for(int z:new int[]{-15,-7})for(int x=-14;x<=-9;x++){
            int top=shedRoofY(x);for(int y=4;y<top;y++)set(l,b,x,y,z,boards);if(shedTop(x))set(l,b,x,top,z,Blocks.DARK_OAK_PLANKS);
        }
        // A half-pitch roof of dark slabs, ridge running front to back, overhanging all round.
        for(int x=-16;x<=-7;x++)for(int z=-16;z<=-6;z++){
            if(x==-15||x==-8)continue;
            if((z==-15||z==-7)&&shedTop(x)&&x>=-14&&x<=-9)continue;
            set(l,b,x,shedRoofY(x),z,slab(Blocks.DARK_OAK_SLAB,shedTop(x)?SlabType.TOP:SlabType.BOTTOM));
        }
        for(int z=-16;z<=-6;z++)for(int x:new int[]{-15,-8})if(z<-15||z>-7)set(l,b,x,4,z,slab(Blocks.DARK_OAK_SLAB,SlabType.TOP));
        // The doorway in the east wall, windows in the south; the onward door is set in the back (west) wall at z -11.
        box(l,b,-8,0,-11,-8,1,-11,Blocks.AIR);
        for(int x:new int[]{-13,-10})box(l,b,x,1,-7,x,2,-7,Blocks.GLASS_PANE);
        // A collar tie across the middle, a lantern from it.
        for(int x=-14;x<=-9;x++)set(l,b,x,4,-11,log(Blocks.STRIPPED_SPRUCE_LOG,Direction.Axis.X));
        set(l,b,-12,3,-11,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
        // What a working shed keeps, the way through from door to back door left clear: the workbench and its tools on the
        // back wall, the potting bench and its pegboard, seed sacks, a harness by the back door, a pitchfork, milk cans, the
        // barrow under the window.
        set(l,b,-13,0,-14,prop(FarmPropBlock.Kind.WORKBENCH,Direction.SOUTH));
        for(int x=-13;x<=-12;x++)set(l,b,x,1,-14,prop(FarmPropBlock.Kind.TOOL_RACK,Direction.SOUTH));
        set(l,b,-11,0,-14,prop(FarmPropBlock.Kind.POTTING_BENCH,Direction.SOUTH));set(l,b,-10,0,-14,prop(FarmPropBlock.Kind.MILK_CANS,Direction.SOUTH));
        set(l,b,-14,0,-14,prop(FarmPropBlock.Kind.SEED_SACKS,Direction.EAST));set(l,b,-9,0,-14,prop(FarmPropBlock.Kind.SEED_SACKS,Direction.WEST));
        set(l,b,-14,1,-12,prop(FarmPropBlock.Kind.HARNESS,Direction.EAST));set(l,b,-9,0,-13,prop(FarmPropBlock.Kind.PITCHFORK,Direction.WEST));
        set(l,b,-12,0,-8,prop(FarmPropBlock.Kind.WHEELBARROW,Direction.EAST));set(l,b,-14,0,-8,Blocks.COMPOSTER);set(l,b,-9,0,-8,Blocks.HAY_BLOCK);
    }

    // ---------------------------------------------------------------------------------------------------- field and yard
    private static void field(ServerLevel l,BlockPos b){
        for(int x=-15;x<=-7;x++)for(int z=-37;z<=-20;z++){
            boolean edge=x==-15||x==-7||z==-37||z==-20;
            if(edge){set(l,b,x,0,z,Blocks.SPRUCE_FENCE);continue;}
            if(x==-11){set(l,b,x,-2,z,Blocks.COBBLESTONE);set(l,b,x,-1,z,Blocks.WATER);continue;}
            set(l,b,x,-1,z,Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE,7));
            set(l,b,x,0,z,(x<-11?Blocks.WHEAT:Blocks.CARROTS).defaultBlockState().setValue(CropBlock.AGE,5));
        }
        set(l,b,-7,0,-24,Blocks.SPRUCE_FENCE_GATE.defaultBlockState().setValue(FenceGateBlock.FACING,Direction.EAST));
        // A scarecrow in the carrots.
        set(l,b,-9,-1,-29,Blocks.COARSE_DIRT);set(l,b,-9,0,-29,Blocks.SPRUCE_FENCE);set(l,b,-9,1,-29,Blocks.HAY_BLOCK);
        set(l,b,-9,2,-29,Blocks.CARVED_PUMPKIN.defaultBlockState().setValue(CarvedPumpkinBlock.FACING,Direction.SOUTH));
    }
    private static void yard(ServerLevel l,BlockPos b){
        for(int x=11;x<=15;x++)for(int z=-11;z<=-3;z++)if(x==11||x==15||z==-11||z==-3)set(l,b,x,0,z,Blocks.SPRUCE_FENCE);
        set(l,b,11,0,-7,Blocks.SPRUCE_FENCE_GATE.defaultBlockState().setValue(FenceGateBlock.FACING,Direction.EAST));
        set(l,b,14,0,-10,Blocks.HAY_BLOCK);set(l,b,13,0,-4,prop(FarmPropBlock.Kind.TROUGH,Direction.NORTH));
        for(int x=12;x<=14;x++)for(int z=-10;z<=-4;z++)if(Math.floorMod(x+z,3)==0)set(l,b,x,-1,z,Blocks.COARSE_DIRT);
    }
    /** One old oak in the yard, for shade by the gate. */
    private static void yardTree(ServerLevel l,BlockPos b,int x,int z){
        for(int y=0;y<=4;y++)set(l,b,x,y,z,Blocks.OAK_LOG);
        var leaves=Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true);
        for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++)for(int y=3;y<=6;y++){
            int r=y==6?1:y==3?1:2;if(Math.abs(dx)>r||Math.abs(dz)>r||dx==0&&dz==0&&y<=4)continue;
            if(Math.abs(dx)==2&&Math.abs(dz)==2)continue;
            set(l,b,x+dx,y,z+dz,leaves);
        }
    }
    /** Built without neighbour updates, every fence, gate, pane and stair takes its joined shape once all of it stands. */
    private static void reshape(ServerLevel l,BlockPos b){
        for(int y=-1;y<=9;y++){final int layer=y;BuildBlocks.after(l,()->{
            for(int x=-17;x<=17;x++)for(int z=-39;z<=0;z++){
                var at=b.offset(x,layer,z);var state=l.getBlockState(at);
                if(!(state.getBlock() instanceof FenceBlock||state.getBlock() instanceof FenceGateBlock||state.getBlock() instanceof IronBarsBlock||state.getBlock() instanceof StairBlock))continue;
                var shaped=Block.updateFromNeighbourShapes(state,l,at);if(!shaped.equals(state)&&!shaped.isAir())l.setBlock(at,shaped,F);
            }
        });}
    }
}
