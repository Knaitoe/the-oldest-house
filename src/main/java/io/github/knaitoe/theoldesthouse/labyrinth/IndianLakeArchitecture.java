package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.List;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Walkable, sealed lake scenes. Natural rock contours hide the boundaries of each slot. */
public final class IndianLakeArchitecture {
    private static final int F = LabyrinthBuilder.flags();
    private IndianLakeArchitecture() {}
    public static void cave(ServerLevel level, BlockPos base) {
        for (int z=-47;z<=0;z++) for (int x=-15;x<=15;x++) for (int y=-3;y<=10;y++) {
            int width = z < -38 ? 8 : z < -10 ? 11 + Math.floorMod(z,5)/2 : 14;
            int ceiling = z < -10 ? 6 + Math.floorMod(x*7+z,3) : 10;
            boolean rock = Math.abs(x)>=width || z==-47 || z==0 || y>=ceiling || y==-3;
            BlockState state = rock ? stone(x,y,z) : y==-1 ? stone(x,-1,z) : y < -1 ? Blocks.DEEPSLATE.defaultBlockState() : Blocks.AIR.defaultBlockState();
            if (z>=-10 && z<=-2 && Math.abs(x)>=2 && Math.abs(x)<=8 && y>=-2 && y<=-1) state=Blocks.WATER.defaultBlockState();
            level.setBlock(base.offset(x,y,z),state,F);
        }
        // The entrance is a narrow causeway at the waterline. Rows face the lake behind it.
        for (int z=-10;z<=-2;z++) for (int x=-1;x<=1;x++)
            level.setBlock(base.offset(x,-1,z),Blocks.DARK_OAK_PLANKS.defaultBlockState(),F);
        for (int row=0;row<6;row++) for (int x=-7;x<=7;x++) {
            if (Math.abs(x)<2) continue;
            int z=-15-row*4;
            level.setBlock(base.offset(x,0,z),LabyrinthBuilder.stairs(Blocks.DARK_OAK_STAIRS,Direction.NORTH),F);
            if (Math.abs(x)==7) level.setBlock(base.offset(x,1,z),Blocks.DARK_OAK_FENCE.defaultBlockState(),F);
        }
        for (int z=-12;z>=-38;z-=6) for (int side:new int[]{-1,1}) {
            level.setBlock(base.offset(side*10,0,z),Blocks.MOSSY_COBBLESTONE_WALL.defaultBlockState(),F);
            light(level,base.offset(side*9,4,z),4);
        }
        // A dry recess beyond the last row holds the old canoe, not a replenishing loot container.
        for(int x=-3;x<=3;x++)for(int z=-44;z<=-39;z++)
            level.setBlock(base.offset(x,-1,z),Blocks.GRAVEL.defaultBlockState(),F);
        light(level,base.offset(0,3,-42),7);
        lectern(level,base.offset(11,0,-5),List.of(
                "THE WATERLINE\n\nThey are not bones. The oldest coat has brass buttons. The youngest still has a red mark from its collar.\n\nThe lake keeps them as they were.",
                "I crossed between the pews with my knees bent. When I stood and hurried, one more voice joined the song.\n\nAt the back, something wooden scraped against the stone.",
                "Doors, food, a fall, a shot: they hear those too. Crouching quiets your feet. It does not quiet everything you do.\n\nThe way out is still behind you."));
        LabyrinthBuilder.doors(level,base,LabyrinthPlace.PRESERVED_CAVE);
    }
    public static void shallows(ServerLevel level,BlockPos base) {
        for(int z=-36;z<=0;z++)for(int x=-18;x<=18;x++)for(int y=-4;y<=10;y++){
            boolean edge=x==-18||x==18||z==-36||z==0||y==10;
            BlockState state=edge?Blocks.BLACK_CONCRETE.defaultBlockState()
                    :y==-4?Blocks.DEEPSLATE.defaultBlockState()
                    :z<=-14&&y<=-1?Blocks.WATER.defaultBlockState()
                    :y<=-1?(z<=-14?Blocks.GRAVEL:Blocks.COARSE_DIRT).defaultBlockState():Blocks.AIR.defaultBlockState();
            level.setBlock(base.offset(x,y,z),state,F);
        }
        for(int x=-15;x<=15;x+=5) {
            if(Math.abs(x)<3)continue;
            for(int y=0;y<5;y++) level.setBlock(base.offset(x,y,-2),Blocks.DARK_OAK_LOG.defaultBlockState(),F);
            for(int dx=-2;dx<=2;dx++) level.setBlock(base.offset(x+dx,5,-2),Blocks.DARK_OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true),F);
        }
        for(int side:new int[]{-1,1})for(int z=-16;z>=-30;z-=3)
            level.setBlock(base.offset(side*15,0,z),Blocks.SUGAR_CANE.defaultBlockState(),F);
        // The same drowned steeple fixes this memory to the lake already visited.
        for(int y=-3;y<=3;y++)for(int x=9;x<=11;x++)for(int z=-31;z<=-29;z++)
            if(x==9||x==11||z==-31||z==-29)level.setBlock(base.offset(x,y,z),Blocks.DEEPSLATE_BRICKS.defaultBlockState(),F);
        level.setBlock(base.offset(10,4,-30),Blocks.DEEPSLATE_BRICK_WALL.defaultBlockState(),F);
        light(level,base.offset(2,3,-8),8);light(level,base.offset(0,2,-13),6);
        LabyrinthBuilder.doors(level,base,LabyrinthPlace.SHALLOWS);
    }
    private static BlockState stone(int x,int y,int z){
        return (Math.floorMod(x*17+y*3+z*7,11)<3?Blocks.MOSSY_COBBLESTONE:Math.floorMod(x+z,5)==0?Blocks.TUFF:Blocks.DEEPSLATE).defaultBlockState();
    }
    public static void light(ServerLevel level,BlockPos pos,int value){level.setBlock(pos,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,value),F);}
    private static void lectern(ServerLevel level,BlockPos at,List<String> pages){
        level.setBlock(at,Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING,Direction.SOUTH).setValue(LecternBlock.HAS_BOOK,true),F);
        if(level.getBlockEntity(at) instanceof LecternBlockEntity desk){desk.setBook(HouseWriting.book("At the waterline","An explorer",pages.stream().map(p->HouseWriting.page(HouseWriting.WritingStyle.PLAIN,p)).toList()));desk.setChanged();}
    }
}
