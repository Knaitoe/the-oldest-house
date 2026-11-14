package io.github.knaitoe.theoldesthouse.labyrinth;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

/**
 * 0.4.583, the yard's bulbs held by something (docs/YARD_NOTES_0_4_583.md). The owner, of the clothesline's back door: the wall
 * light is "a dark empty tube on a cream base, reads broken"; and of the flooded yard: a floating lamp.
 *
 * <p>The yard's bulb was one fitting, a bulb on a cord, set wherever a room wanted light: in a recess beside a back door, where
 * it read as a broken wall light, and at the far ends of the lanes, where nothing was over it and its cord ended in the air.
 * Once a yard room is built (LabyrinthBuilder), every bulb in it is mounted on what is really there:
 *
 * <ul>
 * <li>in a recess with one open side and a wall behind it, on that wall, facing out (WALL);</li>
 * <li>under a block, hung from it on its cord (HUNG; under a half block, on a longer cord, HUNG_LOW);</li>
 * <li>beside a wall, on that wall (WALL);</li>
 * <li>anywhere else, on a post, which is stood under it down to the ground (POST).</li>
 * </ul>
 *
 * A hedge is not a wall and holds nothing up.
 */
public final class YardLights {
    private YardLights(){}
    /** How a bulb is mounted (YardBlock.STAGE). */
    public static final int HUNG=0,WALL=1,POST=2,HUNG_LOW=3;
    /** The tallest post stood under a bulb. */
    static final int POST_MAX=6;

    /** Every bulb in a yard room, once the room is built. */
    public static void mount(ServerLevel l,BlockPos b,LabyrinthPlace place){
        var r=place.room();if(r==null)return;
        var at=new BlockPos.MutableBlockPos();
        for(int x=r.minX();x<=r.maxX();x++)for(int y=r.minY();y<=r.maxY();y++)for(int z=r.minZ();z<=r.maxZ();z++){
            at.set(b.getX()+x,b.getY()+y,b.getZ()+z);
            if(YardBlock.is(l.getBlockState(at),YardBlock.Kind.BULB))mount(l,at.immutable());
        }
    }
    /** One bulb, mounted on what is round it. */
    public static void mount(ServerLevel l,BlockPos at){
        var s=l.getBlockState(at);if(!YardBlock.is(s,YardBlock.Kind.BULB))return;
        Direction hint=s.getValue(YardBlock.FACING),open=null;int ways=0;
        for(var d:Direction.Plane.HORIZONTAL)if(passable(l,at.relative(d))){ways++;open=d;}
        BlockState want;
        if(ways==1&&wall(l,at,open.getOpposite()))want=YardBlock.of(YardBlock.Kind.BULB,open,WALL);
        else if(over(l,at))want=YardBlock.of(YardBlock.Kind.BULB,hint,HUNG);
        else if(halfOver(l,at))want=YardBlock.of(YardBlock.Kind.BULB,hint,HUNG_LOW);
        else{
            Direction out=null;
            if(wall(l,at,hint.getOpposite())&&passable(l,at.relative(hint)))out=hint;
            else for(var d:Direction.Plane.HORIZONTAL)if(wall(l,at,d.getOpposite())&&passable(l,at.relative(d))){out=d;break;}
            if(out!=null)want=YardBlock.of(YardBlock.Kind.BULB,out,WALL);
            else if(post(l,at))want=YardBlock.of(YardBlock.Kind.BULB,hint,POST);
            else want=s;
        }
        if(!want.equals(s))l.setBlock(at,want,Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);
    }
    /** For the checks: what is wrong with how a bulb is held, or null if it is held. */
    public static @Nullable String held(ServerLevel l,BlockPos at){
        var s=l.getBlockState(at);if(!YardBlock.is(s,YardBlock.Kind.BULB))return "no bulb";
        return switch(s.getValue(YardBlock.STAGE)){
            case HUNG->over(l,at)?null:"hung under nothing: "+l.getBlockState(at.above());
            case HUNG_LOW->halfOver(l,at)?null:"hung under nothing: "+l.getBlockState(at.above());
            case WALL->wall(l,at,s.getValue(YardBlock.FACING).getOpposite())?null:"on no wall: "+l.getBlockState(at.relative(s.getValue(YardBlock.FACING).getOpposite()));
            default->stands(l,at.below())?null:"on no post: "+l.getBlockState(at.below());
        };
    }

    private static boolean passable(ServerLevel l,BlockPos p){return l.getBlockState(p).getCollisionShape(l,p).isEmpty();}
    /** A wall on this side of the bulb's cell: a face that something can be fixed to. */
    private static boolean wall(ServerLevel l,BlockPos at,Direction side){var p=at.relative(side);return l.getBlockState(p).isFaceSturdy(l,p,side.getOpposite());}
    /** Something over the bulb's cell that a cord can hang from, at the cell's top. */
    private static boolean over(ServerLevel l,BlockPos at){var p=at.above();return l.getBlockState(p).isFaceSturdy(l,p,Direction.DOWN,SupportType.CENTER);}
    /** The upper half of a block over the bulb's cell: the cord goes up to it. */
    private static boolean halfOver(ServerLevel l,BlockPos at){
        var p=at.above();var shape=l.getBlockState(p).getCollisionShape(l,p);
        return !shape.isEmpty()&&Math.abs(shape.min(Direction.Axis.Y)-.5)<1e-6&&shape.min(Direction.Axis.X)<=.4&&shape.max(Direction.Axis.X)>=.6&&shape.min(Direction.Axis.Z)<=.4&&shape.max(Direction.Axis.Z)>=.6;
    }
    private static boolean stands(ServerLevel l,BlockPos p){var u=l.getBlockState(p);return u.getBlock() instanceof FenceBlock||u.isFaceSturdy(l,p,Direction.UP,SupportType.CENTER);}
    /**
     * A post stood under the bulb, down to the ground: the game's fence post with no arms, set so that it never joins what stands
     * beside it (a post, not a piece of fence). False if there is no ground within reach or something that holds nothing up (a
     * hedge) is in the way, and then nothing is placed: the bulb is left as it was built, and the architecture check names it.
     */
    private static boolean post(ServerLevel l,BlockPos at){
        int n=0;var p=at.below();
        while(n<POST_MAX&&!stands(l,p)){var u=l.getBlockState(p);if(!u.isAir()&&!u.canBeReplaced())return false;n++;p=p.below();}
        if(!stands(l,p))return false;
        for(var q=at.below();q.getY()>p.getY();q=q.below())
            l.setBlock(q,Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.WATERLOGGED,l.getFluidState(q).is(Fluids.WATER)),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);
        return true;
    }
}
