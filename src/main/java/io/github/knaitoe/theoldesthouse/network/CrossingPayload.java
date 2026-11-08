package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** 0.4.351, The Last Crossing: the station's hour on its own day's clock (a day time, or -1 to clear), from the crew's shared run (labyrinth/LastCrossing). */
public record CrossingPayload(long time) implements CustomPacketPayload {
    public static final Type<CrossingPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"last_crossing"));
    public static final StreamCodec<FriendlyByteBuf,CrossingPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_LONG,CrossingPayload::time,CrossingPayload::new);
    @Override public Type<CrossingPayload> type(){return TYPE;}
}
