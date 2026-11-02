package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** 0.4.269: the companion key was pressed; the server opens the wheel for the reader's own nearby cat or dog, if one is close enough. */
public record CompanionNearbyPayload() implements CustomPacketPayload {
    public static final Type<CompanionNearbyPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"companion_nearby"));
    public static final StreamCodec<ByteBuf,CompanionNearbyPayload> STREAM_CODEC=StreamCodec.unit(new CompanionNearbyPayload());
    @Override public Type<CompanionNearbyPayload> type(){return TYPE;}
}
