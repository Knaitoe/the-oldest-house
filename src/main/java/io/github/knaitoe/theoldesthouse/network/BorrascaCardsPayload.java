package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 0.4.455: the doctor's corkboard opened to a reader as a board (labyrinth.BorrascaCards): which of their visits it is, and the
 * pieces they have already looked at.
 */
public record BorrascaCardsPayload(int visit,int seen) implements CustomPacketPayload {
    public static final Type<BorrascaCardsPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"borrasca_cards"));
    public static final StreamCodec<ByteBuf,BorrascaCardsPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,BorrascaCardsPayload::visit,ByteBufCodecs.VAR_INT,BorrascaCardsPayload::seen,BorrascaCardsPayload::new);
    @Override public Type<BorrascaCardsPayload> type(){return TYPE;}
}
