package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;

/** Loads the saved original before moving him between dimensions; a missing loaded body is never replaced by a second man. */
final class TruantBody {
    private TruantBody(){}
    private static final TicketType<ChunkPos> TICKET=TicketType.create("truant_original",Comparator.comparingLong(ChunkPos::toLong));
    private record Held(ServerLevel level,ChunkPos chunk){}
    private static final Set<Held> HELD=new HashSet<>();
    static void clear(){for(var h:HELD)h.level().getChunkSource().removeRegionTicket(TICKET,h.chunk(),2,h.chunk());HELD.clear();}
    static LiteraryActor loaded(MinecraftServer s,UUID id){for(var l:s.getAllLevels())if(l.getEntity(id) instanceof LiteraryActor a&&Truant.isTruant(a))return a;return null;}
    static boolean move(MinecraftServer server,BlockPos origin,LabyrinthPlace from,LabyrinthPlace to,CompoundTag state){
        if(!state.hasUUID("Body")){clear();return true;}
        var a=loaded(server,state.getUUID("Body"));
        if(a==null){
            if(from==null)return false;var source=server.getLevel(NovelRooms.dimension(from));if(source==null)return false;
            var b=LabyrinthPlaces.base(origin,from);var area=RoomActors.box(b,from);
            for(int x=(int)Math.floor(area.minX)>>4;x<=((int)Math.floor(area.maxX))>>4;x++)for(int z=(int)Math.floor(area.minZ)>>4;z<=((int)Math.floor(area.maxZ))>>4;z++)hold(source,new ChunkPos(x,z));
            if(state.contains("BodyPos"))hold(source,new ChunkPos(BlockPos.of(state.getLong("BodyPos"))));
            if(HELD.stream().anyMatch(h->!h.level().hasChunk(h.chunk().x,h.chunk().z)||!h.level().areEntitiesLoaded(h.chunk().toLong())))return false;
            a=loaded(server,state.getUUID("Body"));if(a==null)return false;
        }
        var target=server.getLevel(NovelRooms.dimension(to));if(target==null)return false;var b=LabyrinthPlaces.base(origin,to);
        if(!RoomActors.ready(target,RoomActors.box(b,to)))return false;
        if(a.level()!=target){var spot=Vec3.atLowerCornerOf(b).add(Truant.home(to));var moved=a.changeDimension(new DimensionTransition(target,spot,Vec3.ZERO,0,0,DimensionTransition.DO_NOTHING));if(!(moved instanceof LiteraryActor original)||!original.getUUID().equals(state.getUUID("Body")))return false;a=original;}
        a.bind(to,null);a.setNoGravity(true);a.setDeltaMovement(Vec3.ZERO);Truant.place(a,b,Truant.home(to),0);state.putLong("BodyPos",a.blockPosition().asLong());clear();return true;
    }
    private static void hold(ServerLevel l,ChunkPos c){if(HELD.add(new Held(l,c)))l.getChunkSource().addRegionTicket(TICKET,c,2,c);}
}
