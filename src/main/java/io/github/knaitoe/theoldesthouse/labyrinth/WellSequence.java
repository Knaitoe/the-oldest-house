package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.AABB;

/** One lid for the physical shaft; occupied time, darkness and completion belong to each reader. */
public final class WellSequence {
    public static final int DESCENDING_MODE=18,DARK_START=60,DARK_END=220;
    private WellSequence(){}
    public static boolean shaft(ServerPlayer p,BlockPos b){return p.level().dimension().equals(HouseDimensions.OUTSIDE)
            &&p.getY()<b.getY()+1&&Math.abs(p.getX()-b.getX()-.5)<.6&&Math.abs(p.getZ()-b.getZ()+22.5)<.6;}
    public static float depth(double relativeY){return net.minecraft.util.Mth.clamp((float)(-relativeY/12),0,1);}
    public static float darkness(float occupiedTicks){float t=net.minecraft.util.Mth.clamp((occupiedTicks-DARK_START)/(DARK_END-DARK_START),0,1);return t*t*(3-2*t);}
    public static void tick(MinecraftServer server,BlockPos b){
        var level=server.getLevel(HouseDimensions.OUTSIDE);if(level==null||!level.hasChunkAt(b.offset(NovelRooms.WELL)))return;
        boolean occupied=false,waiting=false,finishedAtMouth=false;
        var data=LabyrinthData.get(server);
        for(var p:level.players()){
            if(!p.isAlive()||p.isSpectator()||!NovelVignettes.inside(p,LabyrinthPlace.BARN_WELL))continue;
            occupied=true;if(!shaft(p,b))continue;CompoundTag own=NovelVignettes.personal(data,p.getUUID());
            waiting|=own.getBoolean("WellEntered")&&own.getInt("WellTicks")<NovelVignettes.WELL_WAIT;
            finishedAtMouth|=own.getInt("WellTicks")>=NovelVignettes.WELL_WAIT&&p.getY()>=b.getY()-2.5;
        }
        var at=b.offset(NovelRooms.WELL);
        if(level.getBlockEntity(at) instanceof WellCoverBlockEntity lid){
            // No occupants: preserve the physical position, with no offline catch-up.
            lid.advance(occupied,waiting&&!finishedAtMouth);
        }else if(level.getBlockState(at).is(Blocks.SPRUCE_TRAPDOOR)){
            // A saved farm keeps its original cover until the vacant in-place farm pass runs.
            level.setBlock(at,level.getBlockState(at).setValue(TrapDoorBlock.OPEN,!waiting||finishedAtMouth),3);
        }
        var shadow=b.offset(0,2,-24);var old=level.getBlockState(shadow);
        if(old.is(io.github.knaitoe.theoldesthouse.house.HouseBlocks.VIGNETTE_DETAIL.get())
                &&old.getValue(io.github.knaitoe.theoldesthouse.house.VignetteDetailBlock.KIND)==io.github.knaitoe.theoldesthouse.house.VignetteDetailBlock.Kind.SHADOW)
            level.setBlock(shadow,Blocks.AIR.defaultBlockState(),3);
    }
}
