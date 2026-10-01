package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record LakePhonePayload(int phase,int cameraId,int elapsed) implements CustomPacketPayload {
    public static final Type<LakePhonePayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"lake_phone"));
    public static final StreamCodec<ByteBuf,LakePhonePayload> STREAM_CODEC=StreamCodec.composite(
            ByteBufCodecs.VAR_INT,LakePhonePayload::phase,ByteBufCodecs.VAR_INT,LakePhonePayload::cameraId,
            ByteBufCodecs.VAR_INT,LakePhonePayload::elapsed,LakePhonePayload::new);
    @Override public Type<LakePhonePayload> type(){return TYPE;}
}
