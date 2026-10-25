package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;

/**
 * Borrasca's buildings (0.4.121): the trailhead station the reader arrives in, Drisking's store and the Sheriff's office
 * round the lot, the children's fort in the Triple Tree, and the ridge depot's office, sorting room, back room, machine
 * shed and gate hut. Every story position, walk and sightline that BorrascaRooms and BorrascaStory use is kept: the
 * station's aisle, the fort's ladder landing, doorway and Kimber's steps, the gaps either side of the Triple Tree, the
 * depot's aisle from its front door to the service exit, and the line from the taking to the ridge lamp.
 */
final class BorrascaTown {
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private static final BlockState AIR=Blocks.AIR.defaultBlockState();
    private BorrascaTown(){}

    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,Block block){set(l,b,x,y,z,block.defaultBlockState());}
    private static void box(ServerLevel l,BlockPos b,int x,int y,int z,int X,int Y,int Z,BlockState s){NovelRooms.box(l,b,Math.min(x,X),Math.min(y,Y),Math.min(z,Z),Math.max(x,X),Math.max(y,Y),Math.max(z,Z),s);}
    private static void box(ServerLevel l,BlockPos b,int x,int y,int z,int X,int Y,int Z,Block block){box(l,b,x,y,z,X,Y,Z,block.defaultBlockState());}
    private static BlockState fit(BorrascaFittingBlock.Kind k,Direction d){return BorrascaFittingBlock.of(k,d);}
    private static BlockState fit(BorrascaFittingBlock.Kind k){return fit(k,Direction.NORTH);}
    private static BlockState slab(Block block,SlabType type){return block.defaultBlockState().setValue(SlabBlock.TYPE,type);}
    private static BlockState stair(Block block,Direction facing,Half half){return block.defaultBlockState().setValue(StairBlock.FACING,facing).setValue(StairBlock.HALF,half);}
    private static BlockState log(Block block,Direction.Axis axis){return block.defaultBlockState().setValue(RotatedPillarBlock.AXIS,axis);}
    private static BlockState hanging(){return Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true);}
    private static BlockState leaves(){return Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true);}
    private static void furniture(ServerLevel l,BlockPos b,int x,int y,int z,HouseholdFurnitureBlock.Kind k,Direction d){NovelRooms.furniture(l,b.offset(x,y,z),k,d);}
    private static void sign(ServerLevel l,BlockPos b,int x,int y,int z,Direction d,String... lines){NovelRooms.sign(l,b.offset(x,y,z),d,lines);}
    private static void door(ServerLevel l,BlockPos b,int x,int y,int z,Direction d,Block block){NovelRooms.door(l,b.offset(x,y,z),d,block,false);}
    private static void prop(ServerLevel l,BlockPos b,int x,int y,int z,LiteraryPropBlock.Kind k,Direction d,int stage){set(l,b,x,y,z,LiteraryRegistry.PROP.get().defaultBlockState().setValue(LiteraryPropBlock.KIND,k).setValue(LiteraryPropBlock.FACING,d).setValue(LiteraryPropBlock.STAGE,stage));}
    private static void road(ServerLevel l,BlockPos b,int x,int y,int z,RoadhouseBlock.Kind k,Direction d){set(l,b,x,y,z,RoadhouseBlock.of(k,d));}
    private static int hash(int x,int y,int z){int h=x*73856093^y*19349663^z*83492791;return (h^h>>>13)&0x7fffffff;}
    /** Corrugated steel, every few sheets rusted through. */
    private static BlockState sheet(int x,int y,int z){return fit(hash(x,y,z)%6==0?BorrascaFittingBlock.Kind.RUST:BorrascaFittingBlock.Kind.STEEL);}
    /** A slab roof in half-block steps; on a wall line an odd step is a double slab, so no slit opens over the wall. */
    private static void roofCell(ServerLevel l,BlockPos b,int x,int z,int half,Block slab,boolean wall){
        set(l,b,x,half/2,z,slab(slab,half%2==0?SlabType.BOTTOM:wall?SlabType.DOUBLE:SlabType.TOP));
    }
    private static void pickup(ServerLevel l,BlockPos b,int x,int y,int z,int paint){for(var e:ElkLot.pickupStates(b.offset(0,y,0),x,z,Direction.NORTH,paint,false).entrySet())BuildBlocks.set(l,e.getKey(),e.getValue(),F);}

    // ---- The town edge -------------------------------------------------------------------------------------------

    static void town(ServerLevel l,BlockPos b){station(l,b);store(l,b);office(l,b);lot(l,b);edge(l,b);}

    /** The station roof falls from the vestibule's wall top at z 0 to the eave at z -13, in half-block steps. */
    static int stationRoof(int z){return z<=-13?9:10+Math.floorDiv((z+12)*5,12);}

    /** County Trails' station: the reader steps out of the House into it, with the trail notice and the account inside. */
    private static void station(ServerLevel l,BlockPos b){
        var timber=fit(BorrascaFittingBlock.Kind.TIMBER);
        box(l,b,-8,0,-13,8,8,0,AIR);
        box(l,b,-7,-1,-12,7,-1,0,Blocks.STONE_BRICKS);box(l,b,-6,-1,-11,6,-1,0,Blocks.SPRUCE_PLANKS);
        for(int z=-12;z<=0;z++){int top=stationRoof(z)/2;for(int x:new int[]{-7,7}){box(l,b,x,0,z,x,top-1,z,timber);if(z==0||z==-12)box(l,b,x,0,z,x,top-1,z,log(Blocks.STRIPPED_SPRUCE_LOG,Direction.Axis.Y));}}
        box(l,b,-6,0,-12,6,4,-12,timber);
        box(l,b,-1,0,-12,1,2,-12,AIR);for(int x:new int[]{-2,2})box(l,b,x,0,-12,x,3,-12,log(Blocks.STRIPPED_SPRUCE_LOG,Direction.Axis.Y));box(l,b,-1,3,-12,1,3,-12,log(Blocks.STRIPPED_SPRUCE_LOG,Direction.Axis.X));
        for(int x:new int[]{-7,7})for(int z:new int[]{-10,-9,-3,-2})box(l,b,x,1,z,x,2,z,Blocks.GLASS_PANE);
        for(int x:new int[]{-5,-4,4,5})box(l,b,x,1,-12,x,2,-12,Blocks.GLASS_PANE);
        for(int z=-13;z<=0;z++)for(int x=-8;x<=8;x++)roofCell(l,b,x,z,stationRoof(z),Blocks.DARK_OAK_SLAB,Math.abs(x)==7||z==-12);
        for(int z:new int[]{-4,-8})box(l,b,-6,stationRoof(z)/2-1,z,6,stationRoof(z)/2-1,z,log(Blocks.STRIPPED_SPRUCE_LOG,Direction.Axis.X));
        set(l,b,0,4,-4,hanging());set(l,b,0,3,-8,hanging());set(l,b,-4,4,-4,hanging());
        // Inside: a runner down the aisle, a stove and its woodpile, a bench under the window, pamphlets, the trail map.
        box(l,b,-1,0,-10,1,0,-1,Blocks.BROWN_CARPET);
        set(l,b,-6,0,-1,Blocks.SMOKER.defaultBlockState().setValue(AbstractFurnaceBlock.FACING,Direction.EAST));box(l,b,-6,1,-1,-6,stationRoof(-1)/2-1,-1,Blocks.IRON_BARS);
        box(l,b,-6,0,0,-4,0,0,log(Blocks.SPRUCE_LOG,Direction.Axis.X));set(l,b,-6,1,0,log(Blocks.SPRUCE_LOG,Direction.Axis.X));
        for(int z=-8;z<=-6;z++)set(l,b,-6,0,z,stair(Blocks.SPRUCE_STAIRS,Direction.WEST,Half.BOTTOM));
        box(l,b,-6,0,-4,-6,1,-4,Blocks.BOOKSHELF);
        fitAt(l,b,BorrascaRooms.TRAIL_MAP,BorrascaFittingBlock.Kind.NOTICE,Direction.WEST);furniture(l,b,6,0,-7,HouseholdFurnitureBlock.Kind.FORMICA_TABLE,Direction.WEST);
        set(l,b,6,0,-1,Blocks.BARREL);set(l,b,6,0,-11,Blocks.POTTED_FERN);set(l,b,-6,0,-11,Blocks.POTTED_FERN);
        sign(l,b,6,1,-1,Direction.WEST,"LOST & FOUND","Ask at the","Sheriff's office","");
        // Out front: the trailhead board.
        for(int x:new int[]{-6,-3})box(l,b,x,0,-16,x,2,-16,log(Blocks.SPRUCE_LOG,Direction.Axis.Y));box(l,b,-5,1,-16,-4,2,-16,Blocks.DARK_OAK_PLANKS);box(l,b,-6,3,-16,-3,3,-16,slab(Blocks.DARK_OAK_SLAB,SlabType.BOTTOM));
        sign(l,b,-5,2,-15,Direction.SOUTH,"OZARK PINE TRAIL","Triple Tree  1","Marker Two  2","");
        sign(l,b,-4,2,-15,Direction.SOUTH,"Public walk","ends at","Marker Two","");
        sign(l,b,-5,1,-15,Direction.SOUTH,"Stay on the","marked trail","","");
        sign(l,b,-4,1,-15,Direction.SOUTH,"Return","before dark","","County Trails");
        BorrascaRooms.join(l,b,-7,1,-12,7,2,0);
    }

    /** Drisking General: a false front on the lot, the noticeboard on its porch, counter, shelves and the cooler inside. */
    private static void store(ServerLevel l,BlockPos b){
        box(l,b,-29,0,-31,-12,8,-15,AIR);
        box(l,b,-28,-1,-30,-16,-1,-16,Blocks.STONE_BRICKS);box(l,b,-27,-1,-29,-17,-1,-17,Blocks.DARK_OAK_PLANKS);
        for(int x=-28;x<=-16;x++)for(int z=-30;z<=-16;z++){if(x!=-28&&x!=-16&&z!=-30&&z!=-16)continue;box(l,b,x,0,z,x,5,z,Blocks.BIRCH_PLANKS);}
        for(int x:new int[]{-28,-16})for(int z:new int[]{-30,-16})box(l,b,x,0,z,x,5,z,log(Blocks.STRIPPED_DARK_OAK_LOG,Direction.Axis.Y));
        box(l,b,-27,5,-29,-17,5,-17,Blocks.SPRUCE_PLANKS);
        // The false front stands two above the parapet, stepped down at its ends.
        box(l,b,-16,6,-28,-16,7,-18,Blocks.BIRCH_PLANKS);box(l,b,-16,6,-30,-16,6,-29,Blocks.BIRCH_PLANKS);box(l,b,-16,6,-17,-16,6,-16,Blocks.BIRCH_PLANKS);
        box(l,b,-16,8,-28,-16,8,-18,slab(Blocks.DARK_OAK_SLAB,SlabType.BOTTOM));box(l,b,-16,7,-30,-16,7,-29,slab(Blocks.DARK_OAK_SLAB,SlabType.BOTTOM));box(l,b,-16,7,-17,-16,7,-16,slab(Blocks.DARK_OAK_SLAB,SlabType.BOTTOM));
        sign(l,b,-15,6,-23,Direction.EAST,"DRISKING","GENERAL","STORE","Est. 1961");
        sign(l,b,-15,6,-26,Direction.EAST,"FEED","HARDWARE","","");sign(l,b,-15,6,-20,Direction.EAST,"COLD","DRINKS","","");
        door(l,b,-16,0,-23,Direction.EAST,Blocks.SPRUCE_DOOR);
        for(int z:new int[]{-28,-27,-26,-19,-18})box(l,b,-16,1,z,-16,2,z,Blocks.GLASS_PANE);
        for(int z:new int[]{-26,-23,-20})box(l,b,-28,1,z,-28,2,z,Blocks.GLASS_PANE);
        for(int x:new int[]{-25,-19})box(l,b,x,1,-16,x,2,-16,Blocks.GLASS_PANE);
        // Porch: a raised deck, posts and a lean-to roof; the board hangs between the door and the north window.
        box(l,b,-15,0,-29,-13,0,-17,slab(Blocks.SPRUCE_SLAB,SlabType.BOTTOM));
        for(int z:new int[]{-29,-17})box(l,b,-13,0,z,-13,3,z,Blocks.SPRUCE_FENCE);
        box(l,b,-15,4,-29,-13,4,-17,slab(Blocks.SPRUCE_SLAB,SlabType.BOTTOM));
        for(var at:BorrascaRooms.BOARD)fitAt(l,b,at,BorrascaFittingBlock.Kind.NOTICE,Direction.EAST);
        set(l,b,-14,0,-29,Blocks.BARREL);set(l,b,-15,0,-27,stair(Blocks.SPRUCE_STAIRS,Direction.WEST,Half.BOTTOM));set(l,b,-15,0,-26,stair(Blocks.SPRUCE_STAIRS,Direction.WEST,Half.BOTTOM));
        // Inside: counter and till by the door, stock behind it, two aisles, the cooler along the back.
        for(int z=-27;z<=-25;z++)road(l,b,-19,0,z,RoadhouseBlock.Kind.COUNTER,Direction.EAST);road(l,b,-19,1,-26,RoadhouseBlock.Kind.REGISTER,Direction.EAST);
        for(int z=-28;z<=-25;z++)road(l,b,-17,0,z,RoadhouseBlock.Kind.SHELF,Direction.WEST);
        set(l,b,-18,0,-28,stair(Blocks.SPRUCE_STAIRS,Direction.SOUTH,Half.BOTTOM));
        for(int z=-27;z<=-19;z++){if(z==-23)continue;set(l,b,-24,0,z,Blocks.BARREL);set(l,b,-24,1,z,z%2==0?Blocks.BARREL.defaultBlockState():Blocks.HAY_BLOCK.defaultBlockState());set(l,b,-21,0,z,z%3==0?Blocks.DRIED_KELP_BLOCK:Blocks.HAY_BLOCK);set(l,b,-21,1,z,z%2==0?Blocks.COMPOSTER:Blocks.BARREL);}
        for(int x=-27;x<=-23;x++){set(l,b,x,0,-29,Blocks.WHITE_CONCRETE);set(l,b,x,1,-29,Blocks.LIGHT_BLUE_STAINED_GLASS);set(l,b,x,2,-29,Blocks.WHITE_CONCRETE);}
        road(l,b,-17,1,-20,RoadhouseBlock.Kind.CORKBOARD,Direction.WEST);
        for(int z:new int[]{-26,-20})set(l,b,-22,4,z,hanging());set(l,b,-18,4,-23,hanging());
        set(l,b,-27,0,-17,Blocks.POTTED_DEAD_BUSH);set(l,b,-27,0,-18,Blocks.CAULDRON);
        BorrascaRooms.join(l,b,-29,1,-31,-12,3,-15);
    }

    /** The Sheriff's office: brick, a counter and gate, two desks, the files, a holding cell and the county map. */
    private static void office(ServerLevel l,BlockPos b){
        box(l,b,17,0,-31,31,7,-15,AIR);
        box(l,b,18,-1,-30,30,-1,-16,Blocks.STONE_BRICKS);box(l,b,19,-1,-29,29,-1,-17,Blocks.POLISHED_ANDESITE);
        for(int x=18;x<=30;x++)for(int z=-30;z<=-16;z++){if(x!=18&&x!=30&&z!=-30&&z!=-16)continue;box(l,b,x,0,z,x,4,z,Blocks.BRICKS);set(l,b,x,5,z,slab(Blocks.STONE_BRICK_SLAB,SlabType.BOTTOM));}
        box(l,b,19,5,-29,29,5,-17,Blocks.STONE_BRICKS);box(l,b,18,4,-30,30,4,-30,Blocks.STONE_BRICKS);box(l,b,18,4,-16,30,4,-16,Blocks.STONE_BRICKS);box(l,b,18,4,-29,18,4,-17,Blocks.STONE_BRICKS);box(l,b,30,4,-29,30,4,-17,Blocks.STONE_BRICKS);
        door(l,b,18,0,-23,Direction.WEST,Blocks.SPRUCE_DOOR);set(l,b,17,-1,-23,Blocks.STONE_BRICKS);set(l,b,17,0,-23,slab(Blocks.STONE_BRICK_SLAB,SlabType.BOTTOM));
        for(int z:new int[]{-27,-26,-20,-19})box(l,b,18,1,z,18,2,z,Blocks.GLASS_PANE);
        for(int z:new int[]{-26,-20})box(l,b,30,1,z,30,2,z,Blocks.GLASS_PANE);
        sign(l,b,17,3,-23,Direction.WEST,"COUNTY","SHERIFF","Drisking","Office");
        // A flagpole by the step.
        box(l,b,16,0,-26,16,6,-26,Blocks.IRON_BARS);set(l,b,16,7,-26,Blocks.LIGHTNING_ROD);
        // The public side: a bench and a counter with a swing gate.
        for(int z=-29;z<=-17;z++){if(z==-23)continue;set(l,b,22,0,z,z==-22||z==-24?Blocks.SPRUCE_PLANKS.defaultBlockState():Blocks.SPRUCE_FENCE.defaultBlockState());}
        set(l,b,22,0,-23,Blocks.SPRUCE_FENCE_GATE.defaultBlockState().setValue(FenceGateBlock.FACING,Direction.EAST));
        set(l,b,22,1,-22,slab(Blocks.SPRUCE_SLAB,SlabType.BOTTOM));set(l,b,22,1,-24,slab(Blocks.SPRUCE_SLAB,SlabType.BOTTOM));
        for(int z:new int[]{-28,-27,-18})set(l,b,19,0,z,stair(Blocks.SPRUCE_STAIRS,Direction.WEST,Half.BOTTOM));
        road(l,b,19,1,-21,RoadhouseBlock.Kind.CORKBOARD,Direction.EAST);
        // The working side: two desks, the radio, the files, the map and the cell.
        furniture(l,b,25,0,-20,HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.WEST);furniture(l,b,24,0,-20,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.EAST);
        furniture(l,b,25,0,-25,HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.WEST);furniture(l,b,24,0,-25,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.EAST);
        fitAt(l,b,new BlockPos(25,1,-20),BorrascaFittingBlock.Kind.PAPER,Direction.WEST);prop(l,b,25,1,-25,LiteraryPropBlock.Kind.RECEIVER,Direction.WEST,0);
        for(int z=-24;z<=-21;z++)box(l,b,29,0,z,29,1,z,fit(BorrascaFittingBlock.Kind.DRAWERS,Direction.WEST));
        fitAt(l,b,new BlockPos(29,2,-22),BorrascaFittingBlock.Kind.NOTICE,Direction.WEST);fitAt(l,b,new BlockPos(29,2,-23),BorrascaFittingBlock.Kind.NOTICE,Direction.WEST);
        set(l,b,29,0,-20,Blocks.CAULDRON);set(l,b,29,1,-20,Blocks.BREWING_STAND);
        box(l,b,26,0,-29,26,3,-26,Blocks.IRON_BARS);box(l,b,27,0,-26,29,3,-26,Blocks.IRON_BARS);NovelRooms.door(l,b.offset(26,0,-28),Direction.WEST,Blocks.IRON_DOOR,true);
        for(int x=27;x<=29;x++)set(l,b,x,0,-29,slab(Blocks.SPRUCE_SLAB,SlabType.BOTTOM));set(l,b,29,0,-27,Blocks.CAULDRON);
        set(l,b,20,0,-17,Blocks.POTTED_SPRUCE_SAPLING);
        for(int z:new int[]{-26,-20})set(l,b,24,4,z,hanging());set(l,b,20,4,-23,hanging());set(l,b,28,4,-28,hanging());
        BorrascaRooms.join(l,b,16,0,-31,31,3,-15);
    }

    /** The gravel lot between them, with the county pickup and Prescott's truck nosed in. */
    private static void lot(ServerLevel l,BlockPos b){
        box(l,b,5,-1,-27,16,-1,-14,Blocks.GRAVEL);for(int z:new int[]{-28,-19,-15})set(l,b,16,0,z,log(Blocks.SPRUCE_LOG,Direction.Axis.Y));
        pickup(l,b,8,0,-19,2);sign(l,b,9,0,-13,Direction.SOUTH,"COUNTY 17","Sheriff's","office","");
        pickup(l,b,12,0,-26,3);sign(l,b,13,0,-20,Direction.SOUTH,"PRESCOTT ORE","SERVICES","D-417","");
    }

    /** The town's back edge: a rail fence and hedge either side of the station, and pines behind the buildings. */
    private static void edge(ServerLevel l,BlockPos b){
        for(int x=-44;x<=44;x++){if(Math.abs(x)<=8)continue;set(l,b,x,0,1,Blocks.SPRUCE_FENCE);if(hash(x,0,0)%3!=0)set(l,b,x,0,0,leaves());}
        BorrascaRooms.join(l,b,-44,0,1,44,0,1);
        for(int[] at:new int[][]{{-36,-22},{-34,-34},{-10,-32},{36,-24},{34,-36},{22,-38}})pine(l,b,at[0],at[1],0,9+hash(at[0],0,at[1])%4);
    }
    private static void pine(ServerLevel l,BlockPos b,int x,int z,int y,int h){box(l,b,x,y,z,x,y+h,z,log(Blocks.SPRUCE_LOG,Direction.Axis.Y));for(int k=0;k<6;k++){int w=k<2?3:k<4?2:1;for(int dx=-w;dx<=w;dx++)for(int dz=-w;dz<=w;dz++)if(Math.abs(dx)+Math.abs(dz)<=w+1&&!(dx==0&&dz==0&&k<5))set(l,b,x+dx,y+h-4+k,z+dz,leaves());}set(l,b,x,y+h+2,z,leaves());}

    // ---- The fort in the Triple Tree ------------------------------------------------------------------------------

    /** The three trunks grown into one root; the fort is built in their crotch, above the first marker's walk. */
    static void fort(ServerLevel l,BlockPos b){
        box(l,b,-8,-1,-104,7,-1,-82,Blocks.PODZOL);box(l,b,-8,0,-104,7,18,-82,AIR);
        for(int[] at:new int[][]{{-4,-101},{-2,-102},{1,-101},{-5,-97},{3,-101},{-1,-97}})set(l,b,at[0],-1,at[1],Blocks.ROOTED_DIRT);
        // The burl: one mass of bark from which the trunks part. The trail forks round it, at x -4 and x 3.
        box(l,b,-3,0,-100,2,2,-98,log(Blocks.SPRUCE_WOOD,Direction.Axis.Y));box(l,b,-3,3,-100,1,4,-99,log(Blocks.SPRUCE_WOOD,Direction.Axis.Y));
        set(l,b,3,3,-99,log(Blocks.SPRUCE_WOOD,Direction.Axis.X));
        set(l,b,-4,-1,-98,Blocks.MOSS_BLOCK);set(l,b,-5,0,-98,Blocks.FERN);set(l,b,4,0,-97,Blocks.FERN);set(l,b,-1,0,-102,Blocks.FERN);
        // Platform, posts and braces.
        box(l,b,-5,5,-100,5,5,-88,Blocks.SPRUCE_PLANKS);for(int[] at:new int[][]{{-2,-95},{1,-97},{3,-92},{-4,-91},{2,-99}})set(l,b,at[0],5,at[1],hash(at[0],5,at[1])%2==0?Blocks.BIRCH_PLANKS:Blocks.OAK_PLANKS);
        for(int x:new int[]{-5,5})for(int z:new int[]{-100,-89})box(l,b,x,0,z,x,5,z,log(Blocks.SPRUCE_LOG,Direction.Axis.Y));
        for(int z:new int[]{-100,-89}){set(l,b,-4,4,z,stair(Blocks.SPRUCE_STAIRS,Direction.WEST,Half.TOP));set(l,b,4,4,z,stair(Blocks.SPRUCE_STAIRS,Direction.EAST,Half.TOP));}
        // Walls: a doorway beside the hatch, a window each side; Kimber's notes go up on the back wall.
        box(l,b,-5,6,-100,5,8,-100,Blocks.SPRUCE_PLANKS);box(l,b,-5,6,-100,-5,8,-89,Blocks.SPRUCE_PLANKS);box(l,b,5,6,-100,5,8,-89,Blocks.SPRUCE_PLANKS);
        box(l,b,-5,6,-89,0,8,-89,Blocks.SPRUCE_PLANKS);box(l,b,4,6,-89,5,8,-89,Blocks.SPRUCE_PLANKS);set(l,b,3,8,-89,Blocks.SPRUCE_PLANKS);box(l,b,1,8,-89,2,8,-89,Blocks.SPRUCE_PLANKS);
        for(int[] at:new int[][]{{-5,7,-97},{5,6,-92},{-2,8,-100},{4,7,-89},{-5,8,-91}})set(l,b,at[0],at[1],at[2],hash(at[0],at[1],at[2])%2==0?Blocks.BIRCH_PLANKS:Blocks.OAK_PLANKS);
        for(int x:new int[]{-5,5})box(l,b,x,7,-96,x,7,-94,Blocks.GLASS_PANE);
        for(int x=-5;x<=0;x++)set(l,b,x,6,-88,Blocks.SPRUCE_FENCE);for(int x=4;x<=5;x++)set(l,b,x,6,-88,Blocks.SPRUCE_FENCE);
        // Roof: a gable along the platform, oversailing the landing so the hatch is under cover.
        for(int z=-101;z<=-87;z++)for(int x=-6;x<=6;x++){int half=18+6-Math.abs(x);roofCell(l,b,x,z,half,hash(x,0,z)%7==0?Blocks.BIRCH_SLAB:Blocks.SPRUCE_SLAB,Math.abs(x)==5);
            if((z==-100||z==-89)&&Math.abs(x)<=4){box(l,b,x,9,z,x,half/2-1,z,Blocks.SPRUCE_PLANKS);if(half%2==1)set(l,b,x,half/2,z,slab(Blocks.SPRUCE_SLAB,SlabType.DOUBLE));}}
        // The ladder and its hatch, unchanged in place.
        box(l,b,3,-1,-88,3,-1,-86,Blocks.SPRUCE_PLANKS);box(l,b,3,0,-89,3,7,-89,log(Blocks.SPRUCE_LOG,Direction.Axis.Y));
        for(int y=0;y<=6;y++)set(l,b,3,y,-88,Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.SOUTH));
        set(l,b,3,6,-88,Blocks.SPRUCE_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.FACING,Direction.SOUTH).setValue(TrapDoorBlock.OPEN,true));box(l,b,3,7,-88,3,8,-88,AIR);
        sign(l,b,0,7,-88,Direction.SOUTH,"TRIPLE TREE","FORT","NO GROWN-UPS","password: shiny");
        // Inside.
        furniture(l,b,-3,6,-95,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.EAST);set(l,b,-3,6,-99,Blocks.BARREL);set(l,b,4,6,-91,Blocks.BARREL);set(l,b,-4,6,-90,Blocks.BOOKSHELF);
        box(l,b,-2,6,-97,0,6,-93,Blocks.RED_CARPET);set(l,b,-4,6,-99,Blocks.CANDLE.defaultBlockState());
        fitAt(l,b,BorrascaRooms.NOTES,BorrascaFittingBlock.Kind.NOTICE,Direction.SOUTH);
        set(l,b,0,11,-94,hanging());
        // The three trunks run up through the floor, wall and roof, and crown together above it.
        for(int[] t:new int[][]{{-3,-100,3},{0,-100,5},{4,-99,0}}){box(l,b,t[0],t[2],t[1],t[0],17,t[1],log(Blocks.SPRUCE_LOG,Direction.Axis.Y));crown(l,b,t[0],t[1],17);}
        for(int[] limb:new int[][]{{-5,14,-100},{-4,14,-100},{5,15,-99},{6,15,-99}})set(l,b,limb[0],limb[1],limb[2],log(Blocks.SPRUCE_LOG,Direction.Axis.X));
        BuildBlocks.set(l,b.offset(BorrascaRooms.CARVING),BorrascaRegistry.CARVING.get().defaultBlockState(),F);
        BorrascaRooms.join(l,b,-6,6,-101,6,8,-87);
    }
    private static void crown(ServerLevel l,BlockPos b,int x,int z,int top){for(int k=0;k<6;k++){int y=top-4+k,w=k<2?3:k<4?2:1;for(int dx=-w;dx<=w;dx++)for(int dz=-w;dz<=w;dz++)if(dx*dx+dz*dz<=w*w+1&&(dx!=0||dz!=0||y>top)&&BuildBlocks.state(l,b.offset(x+dx,y,z+dz)).isAir())set(l,b,x+dx,y,z+dz,leaves());}}

    // ---- The ridge depot ------------------------------------------------------------------------------------------

    /** The depot roof: a low copper gable over x -30..-13, one half-block for every two across. */
    private static int depotRoof(int x){return 36+Math.min(x+30,-13-x)/2;}

    /** Office, sorting room and back room under one roof, along an aisle from the front door to the service exit. */
    static void depot(ServerLevel l,BlockPos b){
        box(l,b,-29,11,-204,-14,11,-169,Blocks.POLISHED_ANDESITE);box(l,b,-28,12,-203,-15,16,-170,AIR);
        for(int x=-29;x<=-14;x++)for(int z=-204;z<=-169;z++){if(x!=-29&&x!=-14&&z!=-204&&z!=-169)continue;set(l,b,x,12,z,Blocks.GRAY_CONCRETE);for(int y=13;y<=16;y++)set(l,b,x,y,z,sheet(x,y,z));}
        for(int x:new int[]{-29,-14})for(int z:new int[]{-204,-169})box(l,b,x,12,z,x,16,z,Blocks.IRON_BLOCK);
        box(l,b,-29,17,-204,-14,17,-169,fit(BorrascaFittingBlock.Kind.STEEL));
        for(int x=-30;x<=-13;x++)for(int z=-205;z<=-168;z++){int half=depotRoof(x);roofCell(l,b,x,z,half,Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB,false);
            if((z==-204||z==-169)&&x>-30&&x<-13){for(int y=18;y<half/2;y++)set(l,b,x,y,z,sheet(x,y,z));if(half%2==1)set(l,b,x,half/2,z,slab(Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB,SlabType.DOUBLE));}}
        box(l,b,-28,11,-180,-15,11,-170,Blocks.DARK_OAK_PLANKS);box(l,b,-28,11,-197,-15,11,-182,Blocks.SMOOTH_STONE);
        // Doors, windows, the front lever and the office sign.
        NovelRooms.door(l,b.offset(BorrascaRooms.FACTORY_DOOR),Direction.SOUTH,Blocks.IRON_DOOR,false);door(l,b,-14,12,-179,Direction.EAST,Blocks.SPRUCE_DOOR);box(l,b,-17,12,-204,-17,13,-204,AIR);
        set(l,b,-17,11,-168,Blocks.SMOOTH_STONE);set(l,b,-13,11,-179,Blocks.SMOOTH_STONE);
        set(l,b,-15,13,-168,Blocks.LEVER.defaultBlockState().setValue(LeverBlock.FACE,AttachFace.WALL).setValue(LeverBlock.FACING,Direction.SOUTH));
        sign(l,b,-16,14,-168,Direction.SOUTH,"RIDGE DEPOT","Office","Ring for","service");
        for(int x=-24;x<=-22;x++)box(l,b,x,13,-169,x,14,-169,Blocks.LIGHT_GRAY_STAINED_GLASS_PANE);
        for(int z:new int[]{-173,-174,-175,-186,-187,-188})box(l,b,-14,13,z,-14,14,z,Blocks.LIGHT_GRAY_STAINED_GLASS_PANE);
        for(int z:new int[]{-185,-186,-187,-191,-192,-193})box(l,b,-29,13,z,-29,14,z,Blocks.LIGHT_GRAY_STAINED_GLASS_PANE);
        // Partitions: the office is panelled with a glazed band; the back room is block.
        for(int x=-28;x<=-15;x++){box(l,b,x,12,-181,x,13,-181,Blocks.SPRUCE_PLANKS);set(l,b,x,14,-181,x==-17?Blocks.SPRUCE_PLANKS:Blocks.GLASS_PANE);box(l,b,x,15,-181,x,16,-181,Blocks.SPRUCE_PLANKS);box(l,b,x,12,-198,x,16,-198,Blocks.LIGHT_GRAY_CONCRETE);}
        door(l,b,-17,12,-181,Direction.SOUTH,Blocks.SPRUCE_DOOR);door(l,b,-17,12,-198,Direction.SOUTH,Blocks.SPRUCE_DOOR);
        depotOffice(l,b);sorting(l,b);backRoom(l,b);
        BorrascaRooms.join(l,b,-29,12,-204,-14,14,-169);
        shed(l,b);hut(l,b);yard(l,b);
    }
    private static void depotOffice(ServerLevel l,BlockPos b){
        for(int x=-28;x<=-26;x++)box(l,b,x,12,-170,x,13,-170,fit(BorrascaFittingBlock.Kind.DRAWERS,Direction.NORTH));
        furniture(l,b,-23,12,-170,HouseholdFurnitureBlock.Kind.RADIATOR,Direction.NORTH);
        for(int z:new int[]{-174,-177}){furniture(l,b,-25,12,z,HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.WEST);furniture(l,b,-26,12,z,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.EAST);fitAt(l,b,new BlockPos(-25,13,z),BorrascaFittingBlock.Kind.PAPER,Direction.WEST);}
        fitAt(l,b,new BlockPos(-28,14,-176),BorrascaFittingBlock.Kind.NOTICE,Direction.EAST);fitAt(l,b,new BlockPos(-28,14,-175),BorrascaFittingBlock.Kind.NOTICE,Direction.EAST);
        for(int z=-174;z<=-171;z++)set(l,b,-20,12,z,Blocks.SPRUCE_PLANKS);fitAt(l,b,new BlockPos(-20,13,-172),BorrascaFittingBlock.Kind.PAPER,Direction.EAST);set(l,b,-21,12,-172,Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.WEST));
        set(l,b,-15,12,-171,Blocks.WHITE_CONCRETE);set(l,b,-15,13,-171,Blocks.LIGHT_BLUE_STAINED_GLASS);
        for(int z=-177;z<=-175;z++)set(l,b,-15,12,z,stair(Blocks.SPRUCE_STAIRS,Direction.EAST,Half.BOTTOM));
        set(l,b,-22,16,-175,hanging());set(l,b,-17,16,-172,hanging());
    }
    private static void sorting(ServerLevel l,BlockPos b){
        for(int z=-192;z<=-183;z++)box(l,b,-28,12,z,-28,14,z,fit(BorrascaFittingBlock.Kind.DRAWERS,Direction.EAST));
        for(int x=-23;x<=-20;x++)for(int z=-189;z<=-188;z++)furniture(l,b,x,12,z,HouseholdFurnitureBlock.Kind.FORMICA_TABLE,Direction.SOUTH);
        fitAt(l,b,new BlockPos(-23,13,-188),BorrascaFittingBlock.Kind.PAPER,Direction.SOUTH);fitAt(l,b,new BlockPos(-21,13,-189),BorrascaFittingBlock.Kind.PAPER,Direction.NORTH);
        prop(l,b,-22,13,-188,LiteraryPropBlock.Kind.BELONGINGS,Direction.SOUTH,1);prop(l,b,-20,13,-189,LiteraryPropBlock.Kind.BELONGINGS,Direction.WEST,2);prop(l,b,-23,13,-189,LiteraryPropBlock.Kind.BELONGINGS,Direction.NORTH,0);
        for(var at:new BlockPos[]{BorrascaRooms.EVIDENCE,BorrascaRooms.PACK})NovelRooms.furniture(l,b.offset(at.below()),HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.SOUTH);
        fitAt(l,b,BorrascaRooms.EVIDENCE,BorrascaFittingBlock.Kind.PAPER,Direction.SOUTH);fitAt(l,b,BorrascaRooms.PACK,BorrascaFittingBlock.Kind.PACK,Direction.SOUTH);
        fitAt(l,b,new BlockPos(-28,14,-195),BorrascaFittingBlock.Kind.NOTICE,Direction.EAST);
        // Shelved effects behind a wire cage on the east wall, its gate open; the aisle stays clear at x -19..-17.
        for(int z=-196;z<=-183;z++){if(z==-190)continue;set(l,b,-15,12,z,Blocks.BARREL);if(z%2==0)set(l,b,-15,13,z,Blocks.BARREL);}
        for(int z=-195;z<=-185;z++)if(z!=-190)box(l,b,-16,12,z,-16,14,z,Blocks.IRON_BARS);
        for(int z:new int[]{-186,-193})set(l,b,-22,16,z,hanging());
    }
    private static void backRoom(ServerLevel l,BlockPos b){
        box(l,b,-28,11,-203,-15,11,-199,Blocks.POLISHED_ANDESITE);
        for(int z=-202;z<=-199;z++)box(l,b,-28,12,z,-28,13,z,Blocks.IRON_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.FACING,Direction.EAST).setValue(TrapDoorBlock.OPEN,true));
        for(int z=-202;z<=-199;z++)set(l,b,-26,12,z,slab(Blocks.SPRUCE_SLAB,SlabType.BOTTOM));
        furniture(l,b,-22,12,-201,HouseholdFurnitureBlock.Kind.FORMICA_TABLE,Direction.SOUTH);furniture(l,b,-23,12,-201,HouseholdFurnitureBlock.Kind.KITCHEN_STOOL,Direction.EAST);furniture(l,b,-21,12,-201,HouseholdFurnitureBlock.Kind.KITCHEN_STOOL,Direction.WEST);
        for(int z=-203;z<=-199;z++)set(l,b,-15,12,z,z==-202?Blocks.CAULDRON.defaultBlockState():Blocks.SMOOTH_STONE.defaultBlockState());set(l,b,-15,13,-200,Blocks.BREWING_STAND);
        fitAt(l,b,new BlockPos(-20,14,-203),BorrascaFittingBlock.Kind.NOTICE,Direction.SOUTH);
        set(l,b,-22,16,-201,hanging());
    }
    /** The machine shed: steel on three sides, an open bay facing the yard and the lamp, the belt through both ends. */
    private static void shed(ServerLevel l,BlockPos b){
        box(l,b,-4,11,-196,4,11,-185,Blocks.GRAY_CONCRETE);box(l,b,-3,12,-195,3,19,-186,AIR);
        for(int x=-4;x<=4;x++)for(int z=-196;z<=-185;z++){if(Math.abs(x)!=4&&z!=-196&&z!=-185)continue;for(int y=12;y<=17;y++)set(l,b,x,y,z,sheet(x,y,z));}
        box(l,b,-2,12,-185,2,15,-185,AIR);
        for(int x=-5;x<=5;x++)for(int z=-197;z<=-185;z++){int half=36+5-Math.abs(x);roofCell(l,b,x,z,half,Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB,Math.abs(x)==4);
            if((z==-196||z==-185)&&Math.abs(x)<=3){for(int y=18;y<half/2;y++)set(l,b,x,y,z,sheet(x,y,z));if(half%2==1)set(l,b,x,half/2,z,slab(Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB,SlabType.DOUBLE));}}
        box(l,b,0,13,-196,0,13,-196,AIR);
        door(l,b,4,12,-190,Direction.EAST,Blocks.IRON_DOOR);set(l,b,5,11,-190,Blocks.SMOOTH_STONE);
        box(l,b,0,17,-193,0,19,-193,Blocks.CHAIN);set(l,b,0,16,-193,Blocks.HEAVY_CORE);
        fitAt(l,b,new BlockPos(3,12,-194),BorrascaFittingBlock.Kind.BREAKER,Direction.WEST);
        for(int z:new int[]{-188,-194}){box(l,b,-3,17,z,-3,19,z,Blocks.CHAIN);set(l,b,-3,16,z,hanging());}
        box(l,b,-1,11,-203,1,11,-201,Blocks.GRAY_CONCRETE);box(l,b,-1,12,-203,1,12,-201,Blocks.BLACK_CONCRETE);set(l,b,0,12,-201,Blocks.COMPOSTER);
    }
    /** The gate hut, inside the wire east of the gate, its window on whoever comes up the trail. */
    private static void hut(ServerLevel l,BlockPos b){
        var timber=fit(BorrascaFittingBlock.Kind.TIMBER);
        box(l,b,4,11,-172,8,11,-168,Blocks.SPRUCE_PLANKS);box(l,b,5,12,-171,7,14,-169,AIR);
        for(int x=4;x<=8;x++)for(int z=-172;z<=-168;z++){if(x!=4&&x!=8&&z!=-172&&z!=-168)continue;box(l,b,x,12,z,x,14,z,timber);}
        for(int z=-171;z<=-169;z++)set(l,b,4,13,z,Blocks.GLASS_PANE);set(l,b,6,13,-168,Blocks.GLASS_PANE);
        door(l,b,6,12,-172,Direction.NORTH,Blocks.SPRUCE_DOOR);
        box(l,b,3,15,-173,9,15,-168,slab(Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB,SlabType.BOTTOM));
        furniture(l,b,5,12,-170,HouseholdFurnitureBlock.Kind.READING_DESK,Direction.EAST);furniture(l,b,6,12,-170,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.WEST);
        fitAt(l,b,new BlockPos(5,13,-170),BorrascaFittingBlock.Kind.PAPER,Direction.EAST);set(l,b,7,14,-170,hanging());
        BorrascaRooms.join(l,b,4,13,-172,8,13,-168);
    }
    /** Yard clutter, kept off the trail-to-lamp line, the workers' and Prescott's marks, the county pickup and the breaker. */
    private static void yard(ServerLevel l,BlockPos b){
        for(int[] at:new int[][]{{-9,-186},{-8,-186},{-9,-187},{20,-199},{21,-199},{20,-200}})set(l,b,at[0],12,at[1],Blocks.BARREL);set(l,b,-8,13,-186,Blocks.BARREL);set(l,b,-7,12,-187,Blocks.CAULDRON);
        for(int x=8;x<=10;x++)for(int z=-189;z<=-188;z++)set(l,b,x,12,z,slab(Blocks.SPRUCE_SLAB,SlabType.BOTTOM));set(l,b,9,12,-190,slab(Blocks.SPRUCE_SLAB,SlabType.TOP));
        box(l,b,18,12,-208,22,13,-205,Blocks.IRON_BLOCK);box(l,b,18,14,-208,22,14,-205,slab(Blocks.SMOOTH_STONE_SLAB,SlabType.BOTTOM));set(l,b,17,12,-206,Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.WEST));set(l,b,17,13,-206,Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.WEST));
        for(int[] at:new int[][]{{-12,-170},{24,-170},{24,-210},{-27,-210}}){box(l,b,at[0],12,at[1],at[0],15,at[1],Blocks.IRON_BARS);set(l,b,at[0],16,at[1],Blocks.LANTERN);}
    }

    private static void fitAt(ServerLevel l,BlockPos b,BlockPos at,BorrascaFittingBlock.Kind k,Direction d){BuildBlocks.set(l,b.offset(at),fit(k,d),F);}
}
