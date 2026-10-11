package io.github.knaitoe.theoldesthouse.labyrinth;
import java.util.*;
import io.github.knaitoe.theoldesthouse.labyrinth.CampDecorBlock.Kind;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
/** Six weathered summer-camp cabins, with genuine crawlable beds and their own small history. */
public final class CampBloodDecor {
    private static Map<BlockPos,BlockState> plan;
    private CampBloodDecor(){}
    public static Map<BlockPos,BlockState> cells(){if(plan!=null)return plan;var cells=new LinkedHashMap<BlockPos,BlockState>();int cabin=0;
        for(int x:new int[]{-20,20})for(int z:new int[]{-26,-57,-83}){int variant=cabin++;
            for(int xx=x-8;xx<=x+8;xx++)for(int zz=z-7;zz<=z+7;zz++)cells.put(new BlockPos(xx,-1,zz),CampDecorBlock.of(Kind.FLOOR,Direction.NORTH,variant));
            for(int y=0;y<5;y++){
                for(int xx=x-8;xx<=x+8;xx++)for(int zz:new int[]{z-7,z+7})cells.put(new BlockPos(xx,y,zz),CampDecorBlock.of(Kind.WALL,Direction.NORTH,variant));
                for(int zz=z-6;zz<=z+6;zz++)for(int xx:new int[]{x-8,x+8})cells.put(new BlockPos(xx,y,zz),CampDecorBlock.of(Kind.WALL,Direction.NORTH,variant));
            }
            for(int dx:new int[]{-5,5}){
                cells.put(new BlockPos(x+dx,0,z-5),CampDecorBlock.of(Kind.BED_HEAD,Direction.NORTH,variant));
                cells.put(new BlockPos(x+dx,0,z-4),CampDecorBlock.of(Kind.BED_FOOT,Direction.NORTH,variant));
                cells.put(new BlockPos(x+dx-1,0,z-5),CampDecorBlock.of(Kind.TRUNK,Direction.NORTH,variant));
                cells.put(new BlockPos(x+dx,0,z-2),CampDecorBlock.of(Kind.BOOTS,Direction.NORTH,variant));
            }
            cells.put(new BlockPos(x+7,1,z+3),CampDecorBlock.of(Kind.NOTICE,Direction.WEST,variant));
            cells.put(new BlockPos(x-7,1,z+3),CampDecorBlock.of(Kind.FIRST_AID,Direction.EAST,variant));
            cells.put(new BlockPos(x+6,1,z-6),CampDecorBlock.of(Kind.OAR,Direction.SOUTH,variant));
            cells.put(new BlockPos(x-6,1,z-6),CampDecorBlock.of(Kind.RING,Direction.SOUTH,variant));
            cells.put(new BlockPos(x+(x<0?7:-7),2,z),CampDecorBlock.of(Kind.SIGN,x<0?Direction.WEST:Direction.EAST,variant));
            cells.put(new BlockPos(x,3,z),CampDecorBlock.of(Kind.LANTERN,Direction.NORTH,variant));
        }plan=Collections.unmodifiableMap(cells);return plan;
    }
    private static boolean replaceable(BlockState old,BlockState next){
        var kind=next.getValue(CampDecorBlock.KIND);return kind==Kind.WALL||kind==Kind.FLOOR?old.is(Blocks.SPRUCE_PLANKS):kind.bed()?old.is(Blocks.RED_BED):kind==Kind.LANTERN?old.is(Blocks.LANTERN):old.isAir();
    }
    /** Queued after the original structure: keep doors, windows, papers and finite supplies exactly where they are. */
    public static void build(ServerLevel l,BlockPos b){for(var entry:cells().entrySet()){var at=b.offset(entry.getKey());if(replaceable(BuildBlocks.state(l,at),entry.getValue()))
        BuildBlocks.set(l,at,entry.getValue(),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE|Block.UPDATE_SUPPRESS_DROPS);}}
    /** A saved room is dressed in at most 64 considered cells per pass. */
    public static int applySlice(ServerLevel l,BlockPos b,int start){var plan=new ArrayList<>(cells().entrySet());int end=Math.min(plan.size(),Math.max(0,start)+64);
        for(int i=Math.max(0,start);i<end;i++){var entry=plan.get(i);var at=b.offset(entry.getKey());if(l.getBlockEntity(at)==null&&replaceable(l.getBlockState(at),entry.getValue()))
            l.setBlock(at,entry.getValue(),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE|Block.UPDATE_SUPPRESS_DROPS);}return end;
    }
}
