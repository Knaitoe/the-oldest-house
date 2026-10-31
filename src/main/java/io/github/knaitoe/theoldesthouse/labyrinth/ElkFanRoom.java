package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import java.util.LinkedHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** One bounded saved-room repair; no actor, paper, supply or personal progress is rebuilt. */
public final class ElkFanRoom {
    public static final int VERSION=2;
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE|Block.UPDATE_SUPPRESS_DROPS;
    private ElkFanRoom(){}
    public static java.util.Map<BlockPos,BlockState> frame(BlockPos b){
        var cells=new LinkedHashMap<BlockPos,BlockState>();
        for(int part=0;part<30;part++)if(FanLadderGeometry.BOXES[part].length>0)
            cells.put(b.offset(0,part/5,-17+part%5-2),FanLadderBlock.part(part));
        return cells;
    }
    public static void build(ServerLevel l,BlockPos b){frame(b).forEach((at,s)->BuildBlocks.set(l,at,s,F));}
    public static boolean standing(ServerLevel l,BlockPos b){return frame(b).entrySet().stream().allMatch(e->l.getBlockState(e.getKey()).equals(e.getValue()));}
    public static boolean onLadder(net.minecraft.server.level.ServerPlayer p,BlockPos b){
        return p.getY()>=b.getY()+4.5&&p.getY()<b.getY()+6&&Math.abs(p.getX()-b.getX()-.5)<1.2
                &&Math.abs(p.getZ()-b.getZ()+16.5)<1.2&&standing(p.serverLevel(),b);
    }
}
