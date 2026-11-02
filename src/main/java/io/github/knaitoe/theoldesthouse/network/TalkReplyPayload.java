package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** 0.4.257: the reply a reader chose in the conversation panel, by the exchange's token and the reply's place. The server decides the rest. */
public record TalkReplyPayload(int token,int reply) implements CustomPacketPayload {
    public static final Type<TalkReplyPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"talk_reply"));
    public static final StreamCodec<ByteBuf,TalkReplyPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,TalkReplyPayload::token,ByteBufCodecs.VAR_INT,TalkReplyPayload::reply,TalkReplyPayload::new);
    @Override public Type<TalkReplyPayload> type(){return TYPE;}
}
