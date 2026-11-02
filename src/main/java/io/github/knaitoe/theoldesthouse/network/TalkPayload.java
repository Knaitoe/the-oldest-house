package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 0.4.257: one reader's open exchange as the server keeps it (labyrinth/Talk): its token, who is speaking, the lines so far (each
 * {@code S} speaker, empty for the reader's own replies, and {@code T} text) and the replies on offer. {@code open} shows the
 * panel; otherwise an open panel is only brought up to date. A state with no token closes it.
 */
public record TalkPayload(boolean open,CompoundTag state) implements CustomPacketPayload {
    public static final Type<TalkPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"talk"));
    public static final StreamCodec<ByteBuf,TalkPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.BOOL,TalkPayload::open,ByteBufCodecs.COMPOUND_TAG,TalkPayload::state,TalkPayload::new);
    @Override public Type<TalkPayload> type(){return TYPE;}
}
