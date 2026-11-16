package io.github.knaitoe.theoldesthouse.network;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
/**
 * 0.4.642, A sort of thing (labyrinth.KingBreath, docs/SLEEPING_KING_0_4_642.md): how much of a thing each reader in the country is,
 * from nowhere (0) to too much of one (4), sent every quarter second to every reader there. A client keeps each for two seconds, so a
 * reader who has left the country is soon their whole self again (client.KingClient).
 */
public record KingStatePayload(List<Entry> readers) implements CustomPacketPayload {
    public record Entry(UUID reader,int state){
        static final StreamCodec<ByteBuf,Entry> CODEC=StreamCodec.composite(UUIDUtil.STREAM_CODEC,Entry::reader,ByteBufCodecs.VAR_INT,Entry::state,Entry::new);
    }
    public static final Type<KingStatePayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"king_state"));
    public static final StreamCodec<ByteBuf,KingStatePayload> STREAM_CODEC=StreamCodec.composite(Entry.CODEC.apply(ByteBufCodecs.list(64)),KingStatePayload::readers,KingStatePayload::new);
    @Override public Type<KingStatePayload> type(){return TYPE;}
}
