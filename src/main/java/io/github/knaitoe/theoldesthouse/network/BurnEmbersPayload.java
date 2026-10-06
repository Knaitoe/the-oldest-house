package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** A brief excerpt for the actual burner only; it carries no progression authority. */
public record BurnEmbersPayload(BlockPos fire, String text, ResourceLocation hand, int memory) implements CustomPacketPayload {
    public static final Type<BurnEmbersPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"burn_embers"));
    public static final StreamCodec<ByteBuf,BurnEmbersPayload> STREAM_CODEC=StreamCodec.composite(
            BlockPos.STREAM_CODEC,BurnEmbersPayload::fire,ByteBufCodecs.STRING_UTF8,BurnEmbersPayload::text,
            ResourceLocation.STREAM_CODEC,BurnEmbersPayload::hand,ByteBufCodecs.VAR_INT,BurnEmbersPayload::memory,BurnEmbersPayload::new);
    @Override public Type<BurnEmbersPayload> type(){return TYPE;}
}
