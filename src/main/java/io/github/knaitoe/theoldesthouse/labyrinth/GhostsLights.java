package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 0.4.242 (owner: "if they see it through to the end - merry running out the door, the lights in the house should cut off. the
 * family remains."). The Barretts' house is one house for every reader, but each reader films their own family, so the lights go out
 * for that reader alone: their client is sent unlit copies of the house's lights (ceiling lights, lamps, fairy lights, the set) and
 * relights nothing until it is sent the house again. The light of the windows stays. Sent again every five seconds, so a chunk the
 * client loads afresh goes dark too.
 */
public final class GhostsLights {
    private static final Map<Long,List<BlockPos>> FIXTURES=new HashMap<>();
    private GhostsLights(){}
    public static void clearAll(){FIXTURES.clear();}

    /** An unlit copy of a light the house has, or null where the cell is not one of its lights. */
    static BlockState dark(BlockState s){
        if(s.getBlock() instanceof BarrettHomeBlock){
            var kind=s.getValue(BarrettHomeBlock.KIND);
            if(kind==BarrettHomeBlock.Kind.WINDOW||kind==BarrettHomeBlock.Kind.WINDOW_HEAD||!s.getValue(BarrettHomeBlock.LIT)||BarrettHomeBlock.light(s)==0)return null;
            return s.setValue(BarrettHomeBlock.LIT,false);
        }
        if(s.getBlock() instanceof GhostsSetBlock&&s.getValue(GhostsSetBlock.KIND)==GhostsSetBlock.Kind.LAMP)return s.setValue(GhostsSetBlock.KIND,GhostsSetBlock.Kind.LAMP_OFF);
        return null;
    }
    private static List<BlockPos> fixtures(ServerLevel l,BlockPos b){
        return FIXTURES.computeIfAbsent(b.asLong(),k->{
            var r=LabyrinthPlace.GHOSTS_SET.room();var found=new ArrayList<BlockPos>();
            for(var at:BlockPos.betweenClosed(b.offset(r.minX(),r.minY(),r.minZ()),b.offset(r.maxX(),r.maxY(),r.maxZ())))if(dark(l.getBlockState(at))!=null)found.add(at.immutable());
            return List.copyOf(found);
        });
    }
    /** The lights out for this reader: at once, then every five seconds while they are in the house. */
    public static void out(ServerPlayer p,BlockPos b,CompoundTag own){
        long now=p.level().getGameTime();if(own.contains("LightsOut0242")&&now-own.getLong("LightsOut0242")<100)return;
        var l=p.serverLevel();for(var at:fixtures(l,b)){
            if(!l.hasChunkAt(at)||!l.areEntitiesLoaded(ChunkPos.asLong(at.getX()>>4,at.getZ()>>4)))continue;
            var dark=dark(l.getBlockState(at));if(dark!=null)p.connection.send(new ClientboundBlockUpdatePacket(at,dark));
        }
        own.putLong("LightsOut0242",now);
    }
    /** Whether this reader has the house's lights out. */
    public static boolean out(CompoundTag own){return own.contains("LightsOut0242");}
}
