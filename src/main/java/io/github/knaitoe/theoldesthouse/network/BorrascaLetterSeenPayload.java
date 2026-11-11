package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** 0.4.459: from a reader looking at the Garritys' letter: a side they have had before them for a moment (labyrinth.BorrascaGarritys.seen). */
public record BorrascaLetterSeenPayload(boolean back) implements CustomPacketPayload {
    public static final Type<BorrascaLetterSeenPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"borrasca_letter_seen"));
    public static final StreamCodec<ByteBuf,BorrascaLetterSeenPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.BOOL,BorrascaLetterSeenPayload::back,BorrascaLetterSeenPayload::new);
    @Override public Type<BorrascaLetterSeenPayload> type(){return TYPE;}
}
