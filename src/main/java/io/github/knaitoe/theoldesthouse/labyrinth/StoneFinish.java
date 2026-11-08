package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 0.4.374, the texture pass's second release (docs/TEXTURE_PASS_STONE_0_4_374.md): the last thing done to a stone hall as it is
 * built. Like {@link HallFinish} for the domestic halls, it changes only the hall's own masonry, and only from the vanilla block each
 * was laid in, one for one, so no shape, collision, door, lamp, rib, stair or story moves:
 * <ul>
 * <li>stone brick walls and vaults become dressed ashlar, now and then weathered; the cracked courses, and about one plain stone in
 * twenty, become cracked ashlar (six different cracks, picked by where the block stands) and the mossy ones damp ashlar;</li>
 * <li>the foot of every wall, where it meets a floor someone stands on, becomes a damp course (where the builder laid a chiselled
 * plinth there, it becomes the carved band instead);</li>
 * <li>the polished floors become flagstones, worn smooth down the middle of the hall, and the brick path down the middle of the
 * older halls becomes worn flags too;</li>
 * <li>the chiselled plinths and courses become a carved band.</li>
 * </ul>
 * The dry fountain's diorite rings, the stair and slab ribs and the parapets stay as they are. Every read and write goes through
 * {@link BuildBlocks}, and every choice made here comes from coordinates relative to the hall (which courses are cracked or mossy
 * the builder had already chosen), so a paced build equals a direct one.
 */
public final class StoneFinish {
    private StoneFinish(){}

    /** Any of the stone halls' own surfaces. */
    public static boolean is(BlockState s){return HallRegistry.isStone(s);}

    public static void finish(ServerLevel l,BlockPos b,LabyrinthPlace p){
        var box=p.room();
        // Floors first, so a wall's foot is judged against the finished floor beside it.
        for(int x=box.minX();x<=box.maxX();x++)for(int z=box.minZ();z<=box.maxZ();z++)for(int y=box.minY();y<=box.maxY();y++){
            var at=b.offset(x,y,z);var s=BuildBlocks.state(l,at);
            boolean andesite=s.is(Blocks.POLISHED_ANDESITE),brick=s.is(Blocks.STONE_BRICKS);
            if(!andesite&&!brick)continue;
            if(!floor(l,at,b.getY()+box.maxY()))continue;
            set(l,at,surface(x==0?"flags_worn":"flags"));
        }
        for(int x=box.minX();x<=box.maxX();x++)for(int z=box.minZ();z<=box.maxZ();z++)for(int y=box.minY();y<=box.maxY();y++){
            var at=b.offset(x,y,z);var s=BuildBlocks.state(l,at);
            if(s.is(Blocks.CHISELED_STONE_BRICKS)){set(l,at,surface("carved"));continue;}
            boolean plain=s.is(Blocks.STONE_BRICKS),cracked=s.is(Blocks.CRACKED_STONE_BRICKS),mossy=s.is(Blocks.MOSSY_STONE_BRICKS);
            if(!plain&&!cracked&&!mossy)continue;
            if(foot(l,at)){set(l,at,surface("damp_course"));continue;}
            set(l,at,surface(cracked||cracks(x,y,z)&&!mossy?"ashlar_cracked":mossy?"ashlar_damp":weathered(x,y,z)?"ashlar_weathered":"ashlar"));
        }
    }

    /** About one stone in four is weathered, scattered by position. */
    static boolean weathered(int x,int y,int z){return Math.floorMod(x*734287+y*912271+z*438289,97)%4==1;}
    /** And about one in twenty has cracked of its own, so even a hall laid without cracked courses shows some. */
    static boolean cracks(int x,int y,int z){return Math.floorMod(x*517619+y*283331+z*699493,101)%20==7;}

    /** Room to stand: air, a doorway, a lantern on the floor, a torch, dust or anything else without a body. */
    private static boolean open(ServerLevel l,BlockPos at){
        var s=BuildBlocks.state(l,at);
        return s.isAir()||s.getBlock() instanceof DoorBlock||s.getBlock() instanceof LanternBlock||s.getCollisionShape(l,at).isEmpty();
    }
    /**
     * A floor: no wall stands on it (a table, a chair or a fallen rib may), under the hall's own roof (looked for no higher than the
     * top of the hall's box, so the top of an outer wall or a ceiling, with nothing of the hall over it, is never a floor).
     */
    private static boolean floor(ServerLevel l,BlockPos at,int top){
        var over=at.above();if(BuildBlocks.state(l,over).isCollisionShapeFullBlock(l,over))return false;
        for(int y=at.getY()+2;y<=top;y++){var up=new BlockPos(at.getX(),y,at.getZ());var s=BuildBlocks.state(l,up);if(!s.isAir()&&!s.getCollisionShape(l,up).isEmpty())return true;}
        return false;
    }
    /** The foot of a wall: beside it, at the same height, room to stand over a finished floor. */
    private static boolean foot(ServerLevel l,BlockPos at){
        for(Direction side:Direction.Plane.HORIZONTAL){
            var beside=at.relative(side);
            if(!open(l,beside))continue;
            var under=BuildBlocks.state(l,beside.below());
            if(is(under)||under.is(Blocks.POLISHED_DIORITE)||under.is(Blocks.POLISHED_ANDESITE)||under.is(Blocks.STONE_BRICKS))return true;
        }
        return false;
    }
    private static BlockState surface(String name){return HallRegistry.stone(name).defaultBlockState();}
    private static void set(ServerLevel l,BlockPos at,BlockState state){BuildBlocks.set(l,at,state,LabyrinthBuilder.flags());}
}
