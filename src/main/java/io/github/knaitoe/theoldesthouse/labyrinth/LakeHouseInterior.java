package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import static io.github.knaitoe.theoldesthouse.labyrinth.LakeHouseFittingBlock.Kind.*;

/** Measured rooms, real stair openings and thirty half-height basement treads; everything except chairs is wet. */
public final class LakeHouseInterior {
    public static final BlockPos TABLE_PLACE=new BlockPos(-3,-25,-166),ASH=new BlockPos(-6,-24,-175),PAINTING_AT=new BlockPos(8,-23,-167),
        CLOSET=new BlockPos(-6,-25,-176),POOL=new BlockPos(5,-41,-175),SAUNA=new BlockPos(5,-40,-165),SAUNA_EXIT=new BlockPos(5,-40,-170),
        FIGURE=new BlockPos(-5,-19,-166),DRESSING=new BlockPos(-5,-19,-167),KITCHEN=new BlockPos(5,-25,-174);
    public static final List<BlockPos> LAMPS=List.of(new BlockPos(0,-22,-163),new BlockPos(-5,-24,-169),new BlockPos(5,-24,-169),new BlockPos(-8,-24,-174),new BlockPos(6,-24,-178),new BlockPos(0,-16,-175),new BlockPos(-5,-18,-169),new BlockPos(8,-18,-164),new BlockPos(6,-18,-178),new BlockPos(4,-39,-169),new BlockPos(7,-40,-175),new BlockPos(-8,-18,-176));
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE|Block.UPDATE_SUPPRESS_DROPS;
    private LakeHouseInterior(){}
    private static void at(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void fill(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){for(int x=Math.min(x0,x1);x<=Math.max(x0,x1);x++)for(int y=Math.min(y0,y1);y<=Math.max(y0,y1);y++)for(int z=Math.min(z0,z1);z<=Math.max(z0,z1);z++)at(l,b,x,y,z,s);}
    private static BlockState wet(BlockState s){return s.hasProperty(BlockStateProperties.WATERLOGGED)?s.setValue(BlockStateProperties.WATERLOGGED,true):s;}
    private static void prop(ServerLevel l,BlockPos b,int x,int y,int z,LakeHouseFittingBlock.Kind k,Direction d){at(l,b,x,y,z,LakeHouseFittingBlock.of(k,d));}
    public static void build(ServerLevel l,BlockPos b){
        var water=Blocks.WATER.defaultBlockState();var floor=LakeHouseRegistry.FLOOR.get().defaultBlockState();var wall=LakeHouseRegistry.WALL.get().defaultBlockState();
        fill(l,b,-8,-26,-162,8,-26,-178,floor);fill(l,b,-8,-20,-162,8,-20,-178,floor);
        // A three-wide central route, with two real door openings into each side. No furniture occupies it.
        for(int storey:new int[]{-25,-19}){
            for(int x:new int[]{-2,2})fill(l,b,x,storey,-162,x,storey+4,-178,wall);
            for(int z:new int[]{-164,-174})for(int x:new int[]{-2,2})fill(l,b,x,storey,z,x,storey+1,z-1,water);
            for(int x0:new int[]{-8,3})fill(l,b,x0,storey,-170,x0+5,storey+4,-170,wall);
        }
        // Foyer. The half door is open and sways; its native collision never blocks a diver.
        prop(l,b,0,-25,-161,HALF_DOOR,Direction.SOUTH);prop(l,b,-1,-25,-162,COATRACK,Direction.EAST);
        prop(l,b,1,-25,-163,TABLE,Direction.WEST);prop(l,b,1,-24,-163,KEY_BOWL,Direction.WEST);
        // Dining room, set for the arrivals plus one. Three permanent places support the solo/co-op story.
        for(int x=-7;x<=-4;x++)for(int z=-165;z>=-167;z--)prop(l,b,x,-25,z,TABLE,Direction.NORTH);
        prop(l,b,-5,-25,-168,CHAIR,Direction.SOUTH);prop(l,b,-7,-25,-164,CHAIR,Direction.NORTH);
        prop(l,b,-3,-25,-166,CHAIR,Direction.WEST);prop(l,b,-8,-25,-166,CHAIR,Direction.EAST);
        // Study: an ashtray over a real desk, fitted shelves and the drowned hearth.
        for(int x=-8;x<=-5;x++)prop(l,b,x,-25,-175,TABLE,Direction.SOUTH);
        prop(l,b,ASH.getX(),ASH.getY(),ASH.getZ(),ASH_TRAY,Direction.SOUTH);
        for(int z=-172;z>=-174;z--)prop(l,b,-8,-25,z,BOOKCASE,Direction.EAST);
        prop(l,b,-4,-25,-177,HEARTH,Direction.SOUTH);
        // Lounge: a racked three-by-two pool table, seating facing the still life and an underwater candle.
        for(int x=3;x<=5;x++)for(int z=-164;z>=-165;z--)prop(l,b,x,-25,z,POOL_TABLE,Direction.NORTH);
        for(int z=-166;z>=-168;z--)prop(l,b,3,-25,z,SOFA,Direction.EAST);
        prop(l,b,PAINTING_AT.getX(),PAINTING_AT.getY(),PAINTING_AT.getZ(),PAINTING,Direction.WEST);
        prop(l,b,7,-25,-163,CANDLE,Direction.NORTH);
        // Kitchen, two separate marble islands, drawers and the porcelain beaver.
        for(int z=-173;z>=-176;z--)for(int x:new int[]{4,7})prop(l,b,x,-25,z,ISLAND,Direction.NORTH);
        prop(l,b,7,-24,-174,BEAVER,Direction.WEST);for(int x=3;x<=7;x++)prop(l,b,x,-25,-178,BOOKCASE,Direction.NORTH);
        // Stair with a runner: six treads, a headroom opening in the actual upper floor, and a clear landing.
        fill(l,b,-1,-20,-170,1,-20,-177,water);
        for(int i=0;i<6;i++)for(int x=-1;x<=1;x++){
            int y=-25+i,z=-171-i;at(l,b,x,y,z,wet((x==0?LakeHouseRegistry.RED_STAIR.get():Blocks.DARK_OAK_STAIRS).defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH)));
        }
        fill(l,b,-1,-20,-177,1,-20,-178,floor);
        // Upstairs dressing room, bedrooms and an enclosed swim-crawl below the eaves.
        for(int x=-8;x<=-4;x++)prop(l,b,x,-19,-169,WARDROBE,Direction.NORTH);
        for(int x:new int[]{4,7}){prop(l,b,x,-19,-165,BED,Direction.NORTH);prop(l,b,x,-19,-166,BED,Direction.NORTH);prop(l,b,x,-19,-175,BED,Direction.NORTH);prop(l,b,x,-19,-176,BED,Direction.NORTH);}
        fill(l,b,-1,-14,-172,1,-14,-174,water); // the eaves are reached through this upper opening
        // 0.4.105: upstairs is lived in. A vanity in the dressing room; nightstands and a wardrobe in each bedroom; the
        // back room on the left is a child's, with its own lamp. Nothing stands in the central route or a doorway.
        prop(l,b,-8,-19,-163,TABLE,Direction.EAST);prop(l,b,-8,-18,-163,KEY_BOWL,Direction.EAST);
        for(int z:new int[]{-166,-176})prop(l,b,5,-19,z,TABLE,Direction.NORTH);
        prop(l,b,8,-19,-168,WARDROBE,Direction.WEST);prop(l,b,8,-19,-172,WARDROBE,Direction.WEST);
        prop(l,b,-7,-19,-176,BED,Direction.NORTH);prop(l,b,-7,-19,-177,BED,Direction.NORTH);
        prop(l,b,-5,-19,-178,TABLE,Direction.NORTH);prop(l,b,-5,-18,-178,CANDLE,Direction.NORTH);
        prop(l,b,-8,-19,-172,WARDROBE,Direction.EAST);prop(l,b,-8,-19,-173,BOOKCASE,Direction.EAST);
        // The eaves keep what the house no longer uses: a wardrobe and shelves, a table, a dress on its form.
        prop(l,b,-6,-13,-169,WARDROBE,Direction.EAST);prop(l,b,-6,-13,-171,BOOKCASE,Direction.EAST);
        prop(l,b,4,-13,-167,TABLE,Direction.NORTH);prop(l,b,-3,-13,-176,DRESS,Direction.SOUTH);
        // The closet's thirty marked half-height treads spiral down round a stone core.
        fill(l,b,-9,-44,-179,9,-27,-161,LakeHouseRegistry.POOL_STONE.get().defaultBlockState());
        fill(l,b,-8,-42,-178,-3,-27,-171,water);
        int[][] ring={{-7,-172},{-6,-172},{-5,-172},{-4,-172},{-4,-173},{-4,-174},{-4,-175},{-4,-176},{-5,-176},{-6,-176},{-7,-176},{-7,-175},{-7,-174},{-7,-173}};
        for(int i=0;i<30;i++){
            int[] t=ring[i%ring.length];int y=-27-i/2;
            var step=wet(Blocks.STONE_BRICK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,i%2==0?net.minecraft.world.level.block.state.properties.SlabType.TOP:net.minecraft.world.level.block.state.properties.SlabType.BOTTOM));
            at(l,b,t[0],y,t[1],step);
        }
        fill(l,b,-7,-27,-175,-5,-26,-177,water);prop(l,b,CLOSET.getX(),CLOSET.getY(),CLOSET.getZ(),WARDROBE,Direction.SOUTH);
        // A lower chamber, a coloured downward current inside its pool, and a cedar sauna alongside it.
        fill(l,b,-2,-42,-178,7,-38,-162,water);fill(l,b,-2,-43,-178,7,-43,-162,LakeHouseRegistry.POOL_STONE.get().defaultBlockState());
        fill(l,b,2,-40,-170,7,-37,-170,LakeHouseRegistry.CEDAR.get().defaultBlockState());
        fill(l,b,5,-40,-170,5,-39,-170,water);prop(l,b,SAUNA_EXIT.getX(),SAUNA_EXIT.getY(),SAUNA_EXIT.getZ(),SAUNA_DOOR,Direction.NORTH);
        fill(l,b,3,-41,-164,7,-41,-169,LakeHouseRegistry.CEDAR.get().defaultBlockState());
        for(int x=3;x<=7;x++)prop(l,b,x,-40,-162,SAUNA_BENCH,Direction.SOUTH);
        fill(l,b,3,-42,-173,7,-42,-177,water);at(l,b,5,-43,-175,LakeHouseRegistry.POOL_STONE.get().defaultBlockState());
        for(var pos:LAMPS){if(pos.getX()==0){prop(l,b,pos.getX(),pos.getY(),pos.getZ(),BULB,Direction.NORTH);at(l,b,pos.getX(),pos.getY()+1,pos.getZ(),wet(Blocks.CHAIN.defaultBlockState()));}else prop(l,b,pos.getX(),pos.getY(),pos.getZ(),LAMP,pos.getX()==-8?Direction.EAST:pos.getX()>=7?Direction.WEST:Direction.SOUTH);}
    }
}
