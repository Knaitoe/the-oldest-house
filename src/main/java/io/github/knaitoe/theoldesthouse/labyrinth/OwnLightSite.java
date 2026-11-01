package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.213, your own light (docs/OWN_LIGHT_0_4_213.md): a lobby, then twin dark branches running side by side, three wide each, with
 * a wall between them broken by low gaps; they meet again in an end room with the way on in its far wall.
 *
 * <p>Relative to the base: the lobby x -4 to 4, z 0 to -4; the west branch x -4 to -2 and the east x 2 to 4, z -5 to -44, the wall
 * between them x -1 to 1 with gaps a block wide and two high every six blocks; the end room z -45 to -49; the far door at z -50.
 * Halfway is z -25. A line is scratched into the lobby wall (the source); a burnt-down stub lies in the end room (the account).
 */
public final class OwnLightSite {
    private OwnLightSite(){}
    public static final int SIDE=4,HIGH=3,LOBBY_Z=-4,BRANCH_NEAR=-5,BRANCH_FAR=-44,END_NEAR=-45,END_FAR=-49,DOOR_Z=-50,HALFWAY=-25;
    public static final int[] GAPS={-9,-15,-21,-27,-33,-39};
    /** Words scratched into the lobby's west wall (the source). */
    public static final BlockPos SCRATCH=new BlockPos(-4,1,-2);
    /** The stub of a torch burnt down to nothing, in the end room (the account). */
    public static final BlockPos STUB=new BlockPos(2,0,-47);
    public static final BlockPos ONWARD=new BlockPos(0,0,DOOR_Z);
    /** Where the double goes out: the way in. */
    public static final Vec3 ENTRY=new Vec3(.5,0,-.5);
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;

    static net.minecraft.world.level.block.state.BlockState ash(){return DarkHallRegistry.material("ash").defaultBlockState();}
    static net.minecraft.world.level.block.state.BlockState floor(){return DarkHallRegistry.material("floor").defaultBlockState();}
    static net.minecraft.world.level.block.state.BlockState ceiling(){return DarkHallRegistry.material("ceiling").defaultBlockState();}
    public static boolean gap(int z){for(int g:GAPS)if(g==z)return true;return false;}
    /** -1 in the west branch, 1 in the east, 0 anywhere else. */
    public static int branch(Vec3 r){if(r.z>=BRANCH_NEAR+1||r.z<BRANCH_FAR)return 0;return r.x<-1?-1:r.x>=2?1:0;}
    public static boolean inLobby(Vec3 r){return r.z>=LOBBY_Z&&r.z<1;}
    public static boolean inEnd(Vec3 r){return r.z<END_NEAR+1&&r.z>=END_FAR;}
    public static boolean inside(Vec3 r){return r.x>=-SIDE&&r.x<SIDE+1&&r.z>=END_FAR&&r.z<1&&r.y>=-.5&&r.y<HIGH+1;}
    /** The far wall's sixteen torch places, in the order a reader's torches are set back in it. */
    public static List<BlockPos> wall(){var out=new ArrayList<BlockPos>();for(int y=1;y<=2;y++)for(int x:new int[]{-4,-3,-2,-1,1,2,3,4})out.add(new BlockPos(x,y,END_FAR));return out;}
    /** A place across the wall: the same distance down the hall, in the other branch. */
    public static BlockPos across(BlockPos rel){return new BlockPos(-rel.getX(),rel.getY(),rel.getZ());}

    public static void build(ServerLevel l,BlockPos b){
        BuildBlocks.box(l,b.offset(-SIDE-1,-1,DOOR_Z),b.offset(SIDE+1,HIGH,0),ash(),F);
        for(int z=END_FAR;z<=0;z++)for(int x=-SIDE;x<=SIDE;x++){
            boolean wall=z<=BRANCH_NEAR&&z>=BRANCH_FAR&&Math.abs(x)<=1;
            BuildBlocks.set(l,b.offset(x,-1,z),floor(),F);BuildBlocks.set(l,b.offset(x,HIGH,z),ceiling(),F);
            for(int y=0;y<HIGH;y++)BuildBlocks.set(l,b.offset(x,y,z),wall&&!(gap(z)&&y<2)?ash():Blocks.AIR.defaultBlockState(),F);
        }
        BuildBlocks.set(l,b.offset(SCRATCH),HorrorBlock.of(HorrorBlock.Kind.SCRATCH,Direction.EAST),F);
        BuildBlocks.set(l,b.offset(STUB),HorrorBlock.of(HorrorBlock.Kind.STUB,Direction.SOUTH),F);
    }
    /** Sets a reader's torches back into the far wall, in the order they were placed. */
    public static int setBack(ServerLevel l,BlockPos b,int count){
        int n=0;for(var at:wall()){if(n>=count)break;var cell=b.offset(at);if(!l.getBlockState(cell).isAir())continue;
            l.setBlock(cell,Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING,Direction.SOUTH),F);n++;}
        return n;
    }
}
