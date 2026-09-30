package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CompanionOrderPayload(int entityId, int order) implements CustomPacketPayload {
    public static final Type<CompanionOrderPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"companion_order"));
    public static final StreamCodec<ByteBuf,CompanionOrderPayload> STREAM_CODEC=StreamCodec.composite(
            ByteBufCodecs.VAR_INT,CompanionOrderPayload::entityId,ByteBufCodecs.VAR_INT,CompanionOrderPayload::order,CompanionOrderPayload::new);
    @Override public Type<CompanionOrderPayload> type(){return TYPE;}
}
