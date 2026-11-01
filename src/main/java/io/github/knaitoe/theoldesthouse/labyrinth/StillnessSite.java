package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.211, the stillness (docs/STILLNESS_0_4_211.md): a wide, low dark hall in the dark halls' ash with a door at each end, and
 * nothing else in it but square pillars down both sides.
 *
 * <p>Relative to the base: the hall x -5 to 5 (eleven wide), y 0 to 2 (three high), z 0 to -48; pillars at x ±3 every six blocks
 * from z -6 to -42; the way on, the far door, at z -49. Halfway is z -24. A surveyor's page is nailed beside the way in (the
 * source); the account is scratched into the dust before the far door.
 */
public final class StillnessSite {
    private StillnessSite(){}
    public static final int SIDE=5,HIGH=3,NEAR_Z=0,FAR_Z=-48,DOOR_Z=-49,HALFWAY=-24;
    /** The surveyor's page, nailed to the west wall just inside (the source). */
    public static final BlockPos LOG=new BlockPos(-5,1,-3);
    /** A count scratched into the dust before the far door (the account). */
    public static final BlockPos TALLY=new BlockPos(0,0,-46);
    /** Where it comes in and goes out: just inside the far door. */
    public static final Vec3 LAIR=new Vec3(.5,0,-47.5);
    public static final BlockPos ONWARD=new BlockPos(0,0,DOOR_Z);
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;

    static net.minecraft.world.level.block.state.BlockState ash(){return DarkHallRegistry.material("ash").defaultBlockState();}
    static net.minecraft.world.level.block.state.BlockState floor(){return DarkHallRegistry.material("floor").defaultBlockState();}
    static net.minecraft.world.level.block.state.BlockState ceiling(){return DarkHallRegistry.material("ceiling").defaultBlockState();}
    public static boolean pillar(int x,int z){return Math.abs(x)==3&&z<=-6&&z>=-42&&(-z)%6==0;}
    public static boolean inHall(Vec3 r){return r.x>=-SIDE&&r.x<SIDE+1&&r.z>=FAR_Z&&r.z<NEAR_Z+1&&r.y>=-.5&&r.y<HIGH+1;}
    public static boolean pastHalfway(Vec3 r){return r.z<HALFWAY;}
    public static boolean atFarDoor(Vec3 r){return r.z<FAR_Z+4;}
    /** Keeps a point inside the hall's walls. */
    public static double clampX(double x){return Math.max(-SIDE+.5,Math.min(SIDE+.5,x));}
    public static double clampZ(double z){return Math.max(FAR_Z+.5,Math.min(NEAR_Z-.5,z));}

    public static void build(ServerLevel l,BlockPos b){
        BuildBlocks.box(l,b.offset(-SIDE-2,-1,DOOR_Z-2),b.offset(SIDE+2,HIGH+1,0),ash(),F);
        for(int z=FAR_Z;z<=NEAR_Z;z++)for(int x=-SIDE;x<=SIDE;x++){
            BuildBlocks.set(l,b.offset(x,-1,z),floor(),F);BuildBlocks.set(l,b.offset(x,HIGH,z),ceiling(),F);
            for(int y=0;y<HIGH;y++)BuildBlocks.set(l,b.offset(x,y,z),pillar(x,z)?ash():Blocks.AIR.defaultBlockState(),F);
        }
        BuildBlocks.set(l,b.offset(LOG),HorrorBlock.of(HorrorBlock.Kind.LOG,Direction.EAST),F);
        BuildBlocks.set(l,b.offset(TALLY),HorrorBlock.of(HorrorBlock.Kind.TALLY,Direction.SOUTH),F);
    }
    /** A cloven print pressed into the dust, where it stood or walked; only ever in an empty cell on the hall floor. */
    public static boolean print(ServerLevel l,BlockPos b,double x,double z,float yaw){
        var at=b.offset((int)Math.floor(x),0,(int)Math.floor(z));
        if(!l.isLoaded(at)||!l.getBlockState(at).isAir()||!l.getBlockState(at.below()).is(DarkHallRegistry.material("floor")))return false;
        l.setBlock(at,HorrorBlock.of(HorrorBlock.Kind.HOOF,Direction.fromYRot(yaw)),F);return true;
    }
}
