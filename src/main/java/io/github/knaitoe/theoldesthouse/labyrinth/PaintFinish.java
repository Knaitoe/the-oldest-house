package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 0.4.375, the texture pass's release 2b (docs/TEXTURE_PASS_PAINT_0_4_375.md): the last thing done to the five halls that test a
 * reader (the last sconce, the watched threshold, the mirror hall, the blind stretch and the quiet hall) as each is built. Like
 * {@link HallFinish} and {@link StoneFinish}, it changes only the hall's own shell, one block for one, from the vanilla block it was
 * laid in, so no shape, door, lamp, mirror, sensor or hazard block moves:
 * <ul>
 * <li>the smooth stone floors, and the thresholds under doorways, become linoleum, worn pale down the middle of the hall;</li>
 * <li>a wall's foot, beside a floor someone stands on, becomes a dado (darker paint between a rubber cove and a rail), and the wall
 * above it painted plaster, now and then marked; the quiet hall's dark oak becomes panelling, with a skirting at its foot;</li>
 * <li>the shell over a room becomes a ceiling of fibre tiles.</li>
 * </ul>
 * A shell block counts as a wall when a room of the hall stands beside it, and as a ceiling only when none does and a room is under
 * it, so the block over a doorway is part of the wall. Every read and write goes through {@link BuildBlocks}, and every choice comes
 * from coordinates relative to the hall, so a paced build equals a direct one. The watched threshold's door moves through these walls
 * ({@link HazardVariety#moveThreshold}), and the wall it leaves behind is laid back by {@link #wallAt}.
 */
public final class PaintFinish {
    private PaintFinish(){}

    /** Any of these halls' own surfaces. */
    public static boolean is(BlockState s){return HallRegistry.isPaint(s);}

    /** The vanilla blocks each hall's shell was laid in; empty for every other place. */
    public static Set<Block> shellOf(LabyrinthPlace p){
        return switch(p){
            case LAST_SCONCE->Set.of(Blocks.GRAY_TERRACOTTA);
            case WATCHED_THRESHOLD->Set.of(Blocks.LIGHT_GRAY_TERRACOTTA);
            case MIRROR_HALL->Set.of(Blocks.GRAY_CONCRETE);
            // The blind stretch's turns are carved out of the slot's own fill.
            case BLIND_STRETCH->Set.of(Blocks.GRAY_CONCRETE,Blocks.WHITE_TERRACOTTA);
            case QUIET_HALL->Set.of(Blocks.DARK_OAK_PLANKS);
            default->Set.of();
        };
    }
    public static boolean finishes(LabyrinthPlace p){return !shellOf(p).isEmpty();}

    public static void finish(ServerLevel l,BlockPos b,LabyrinthPlace p){
        var shell=shellOf(p);if(shell.isEmpty())return;
        boolean panelled=p==LabyrinthPlace.QUIET_HALL;
        var box=p.room();int top=b.getY()+box.maxY(),bottom=b.getY()+box.minY();
        // Floors first, so a wall's foot is measured from the finished floor beside it. The threshold under a doorway was laid in
        // the shell itself; with room to stand over it, it is floor too.
        for(int x=box.minX();x<=box.maxX();x++)for(int z=box.minZ();z<=box.maxZ();z++)for(int y=box.minY();y<=box.maxY();y++){
            var at=b.offset(x,y,z);var s=BuildBlocks.state(l,at);
            boolean laid=s.is(Blocks.SMOOTH_STONE)||shell.contains(s.getBlock())&&open(l,at.above());
            if(!laid||!floor(l,at,top))continue;
            set(l,at,surface(x==0?"lino_worn":"lino"));
        }
        for(int x=box.minX();x<=box.maxX();x++)for(int z=box.minZ();z<=box.maxZ();z++)for(int y=box.minY();y<=box.maxY();y++){
            var at=b.offset(x,y,z);
            if(!shell.contains(BuildBlocks.state(l,at).getBlock()))continue;
            int course=course(l,at,bottom,top);
            if(course>=0){set(l,at,surface(name(panelled,course,x,y,z)));continue;}
            if(open(l,at.below())&&floorUnder(l,at.below(),bottom)>Integer.MIN_VALUE&&roofed(l,at.below(),top))set(l,at,surface("ceiling"));
        }
    }

    /** What a wall wears at a given height above its floor. */
    static String name(boolean panelled,int course,int x,int y,int z){
        if(panelled)return course==0?"panel_low":"panel";
        return course==0?"dado":marked(x,y,z)?"wall_marked":"wall";
    }
    /** The wall the watched threshold leaves behind when its door moves on: the same surface the finish would have laid there. */
    public static BlockState wallAt(BlockPos b,BlockPos at){
        int course=at.getY()-b.getY();
        return surface(name(false,Math.max(0,course),at.getX()-b.getX(),course,at.getZ()-b.getZ()));
    }

    /** About one stretch of plaster in nine is marked, scattered by position. */
    static boolean marked(int x,int y,int z){return Math.floorMod(x*348191+y*719573+z*256117,89)%9==4;}

    /** Room to stand: air, a doorway, a lantern, or anything else without a body. */
    private static boolean open(ServerLevel l,BlockPos at){
        var s=BuildBlocks.state(l,at);
        return s.isAir()||s.getBlock() instanceof DoorBlock||s.getBlock() instanceof LanternBlock||s.getCollisionShape(l,at).isEmpty();
    }
    /** The height of the first open cell above the floor under an open cell, or MIN_VALUE when no floor of the hall is under it. */
    private static int floorUnder(ServerLevel l,BlockPos cell,int bottom){
        int y=cell.getY();
        while(y>bottom&&open(l,new BlockPos(cell.getX(),y-1,cell.getZ())))y--;
        return y>bottom?y:Integer.MIN_VALUE;
    }
    /** Something of the hall over a cell, looked for no higher than the top of the hall's box. */
    private static boolean roofed(ServerLevel l,BlockPos cell,int top){
        for(int y=cell.getY()+1;y<=top;y++){var up=new BlockPos(cell.getX(),y,cell.getZ());var s=BuildBlocks.state(l,up);if(!s.isAir()&&!s.getCollisionShape(l,up).isEmpty())return true;}
        return false;
    }
    /** A floor: no wall stands on it, under the hall's own roof. */
    private static boolean floor(ServerLevel l,BlockPos at,int top){
        var over=at.above();
        return !BuildBlocks.state(l,over).isCollisionShapeFullBlock(l,over)&&roofed(l,over,top);
    }
    /** How high a shell block stands above the floor of the room beside it (0 at the foot), or -1 when no room of the hall is beside it. */
    private static int course(ServerLevel l,BlockPos at,int bottom,int top){
        int best=-1;
        for(Direction side:Direction.Plane.HORIZONTAL){
            var beside=at.relative(side);
            if(!open(l,beside))continue;
            int floor=floorUnder(l,beside,bottom);
            if(floor==Integer.MIN_VALUE||!roofed(l,beside,top))continue;
            int course=at.getY()-floor;
            if(course>=0&&(best<0||course<best))best=course;
        }
        return best;
    }
    private static BlockState surface(String name){return HallRegistry.paint(name).defaultBlockState();}
    private static void set(ServerLevel l,BlockPos at,BlockState state){BuildBlocks.set(l,at,state,LabyrinthBuilder.flags());}
}
