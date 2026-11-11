package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** 0.4.455: from a reader's corkboard: a piece they have had in the hand for a moment (labyrinth.BorrascaCards.seen). */
public record BorrascaCardSeenPayload(int index) implements CustomPacketPayload {
    public static final Type<BorrascaCardSeenPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"borrasca_card_seen"));
    public static final StreamCodec<ByteBuf,BorrascaCardSeenPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,BorrascaCardSeenPayload::index,BorrascaCardSeenPayload::new);
    @Override public Type<BorrascaCardSeenPayload> type(){return TYPE;}
}
