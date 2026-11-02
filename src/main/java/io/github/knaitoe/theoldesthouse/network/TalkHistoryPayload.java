package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** 0.4.266: a reader's own kept conversations (labyrinth/TalkHistory), for the journal's "What was said" section. */
public record TalkHistoryPayload(CompoundTag history) implements CustomPacketPayload {
    public static final Type<TalkHistoryPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"talk_history"));
    public static final StreamCodec<ByteBuf,TalkHistoryPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.COMPOUND_TAG,TalkHistoryPayload::history,TalkHistoryPayload::new);
    @Override public Type<TalkHistoryPayload> type(){return TYPE;}
}
