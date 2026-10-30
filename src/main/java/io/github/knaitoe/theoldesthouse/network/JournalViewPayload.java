package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Where the reader's journal lies open, as a spread, or -1 once it is shut: the other hand never writes on a page that is open. */
public record JournalViewPayload(int spread) implements CustomPacketPayload {
    public static final int SHUT=-1;
    public static final Type<JournalViewPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"journal_view"));
    public static final StreamCodec<ByteBuf,JournalViewPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,JournalViewPayload::spread,JournalViewPayload::new);
    @Override public Type<JournalViewPayload> type(){return TYPE;}
}
