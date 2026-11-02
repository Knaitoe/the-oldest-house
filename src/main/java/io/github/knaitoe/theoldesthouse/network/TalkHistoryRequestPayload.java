package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** 0.4.266: the journal's ribbon turned to "What was said"; the server answers with the reader's own conversations, if they carry their journal. */
public record TalkHistoryRequestPayload() implements CustomPacketPayload {
    public static final Type<TalkHistoryRequestPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"talk_history_request"));
    public static final StreamCodec<ByteBuf,TalkHistoryRequestPayload> STREAM_CODEC=StreamCodec.unit(new TalkHistoryRequestPayload());
    @Override public Type<TalkHistoryRequestPayload> type(){return TYPE;}
}
