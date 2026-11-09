package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** 0.4.409, the city on the ice: the hour on the ice for this reader (dawn after they outran what fills the tunnel, or -1 for the low sun). */
public record CityPayload(long time) implements CustomPacketPayload {
    public static final Type<CityPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"black_city"));
    public static final StreamCodec<FriendlyByteBuf,CityPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_LONG,CityPayload::time,CityPayload::new);
    @Override public Type<CityPayload> type(){return TYPE;}
}
