package io.github.knaitoe.theoldesthouse.network;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.HouseBodies;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
/**
 * 0.4.163: every House body being worn, sent to every client: which reader, and the body the House chose from their read.
 * 0.4.201: every reader appears here while they are inside the House, and nobody outside it.
 */
public record HouseBodiesPayload(List<Entry> bodies) implements CustomPacketPayload {
    public record Entry(UUID reader,HouseBodies.Body body){
        static final StreamCodec<ByteBuf,Entry> CODEC=StreamCodec.composite(UUIDUtil.STREAM_CODEC,Entry::reader,HouseBodies.Body.STREAM_CODEC,Entry::body,Entry::new);
    }
    public static final Type<HouseBodiesPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"house_bodies"));
    public static final StreamCodec<ByteBuf,HouseBodiesPayload> STREAM_CODEC=StreamCodec.composite(Entry.CODEC.apply(ByteBufCodecs.list(64)),HouseBodiesPayload::bodies,HouseBodiesPayload::new);
    @Override public Type<HouseBodiesPayload> type(){return TYPE;}
}
