package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** 0.4.408, The House on the Corner: the hour on the street for this reader (a day time, or -1 for the street's own siesta). */
public record CornerPayload(long time) implements CustomPacketPayload {
    public static final Type<CornerPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"corner_house"));
    public static final StreamCodec<FriendlyByteBuf,CornerPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_LONG,CornerPayload::time,CornerPayload::new);
    @Override public Type<CornerPayload> type(){return TYPE;}
}
