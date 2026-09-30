package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CompanionMenuPayload(int entityId) implements CustomPacketPayload {
    public static final Type<CompanionMenuPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"companion_menu"));
    public static final StreamCodec<ByteBuf,CompanionMenuPayload> STREAM_CODEC=StreamCodec.composite(
            ByteBufCodecs.VAR_INT,CompanionMenuPayload::entityId,CompanionMenuPayload::new);
    @Override public Type<CompanionMenuPayload> type(){return TYPE;}
}
