package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.labyrinth.TheListBlock.Kind;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;

/**
 * 0.4.723, The list part 6a (docs/THE_LIST_0_4_723.md): the flyers. From night three on the town knows: a flyer is taped up along
 * both sides of Main Street, the curfew printed on it over a drawing of the face in the sack. They are a stage of the town's poster
 * fitting ({@link #STAGE}), put up when night three begins and taken down only when the town's nights are put away; they stay up
 * through night four. A flyer goes only where there is a wall to tape it to and nothing in the way.
 */
public final class TheListFlyers{
    private TheListFlyers(){}
    /** The poster fitting's stage that is the flyer. */
    public static final int STAGE=6;
    /** Along Main Street, at eye height: on the south fronts facing the street, and on the north fronts facing back. */
    static final int Y=1,STEP=12,FROM=-48,TO=60;

    /** Where a flyer may go, with the way it faces: in front of each front, against it. */
    static List<Object[]> spots(){
        var out=new ArrayList<Object[]>();
        for(int x=FROM;x<=TO;x+=STEP){
            out.add(new Object[]{new BlockPos(x,Y,TheListSite.MAIN_FRONT_S-1),Direction.NORTH});
            out.add(new Object[]{new BlockPos(x+STEP/2,Y,TheListSite.MAIN_FRONT_N+1),Direction.SOUTH});
        }
        return out;
    }
    /** The flyers put up: each where the cell is clear and the front behind it is a wall. How many went up. */
    public static int post(ServerLevel l,BlockPos b){
        int up=0;
        for(var s:spots()){
            var rel=(BlockPos)s[0];var facing=(Direction)s[1];var at=b.offset(rel);
            if(!l.isLoaded(at))continue;
            var here=l.getBlockState(at);
            if(TheListBlock.is(here,Kind.POSTER)&&here.getValue(TheListBlock.STAGE)==STAGE){up++;continue;}
            if(!here.isAir())continue;
            var wall=at.relative(facing.getOpposite());
            if(!l.getBlockState(wall).isFaceSturdy(l,wall,facing))continue;
            l.setBlock(at,TheListBlock.of(Kind.POSTER,facing,STAGE),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);up++;
        }
        return up;
    }
    /** The flyers taken down when the town's nights are put away. */
    public static void down(ServerLevel l,BlockPos b){
        for(var s:spots()){
            var at=b.offset((BlockPos)s[0]);if(!l.isLoaded(at))continue;
            var here=l.getBlockState(at);
            if(TheListBlock.is(here,Kind.POSTER)&&here.getValue(TheListBlock.STAGE)==STAGE)l.setBlock(at,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);
        }
    }
    /** How many flyers are up in a town (for a check). */
    public static int up(ServerLevel l,BlockPos b){
        int n=0;for(var s:spots()){var here=l.getBlockState(b.offset((BlockPos)s[0]));if(TheListBlock.is(here,Kind.POSTER)&&here.getValue(TheListBlock.STAGE)==STAGE)n++;}return n;
    }
}
