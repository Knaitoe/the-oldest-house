package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 0.4.463: the keys a rider on a bike holds that the game does not send for a rider: jump (the hop: held to crouch, let go to pop) and
 * sprint (the Rattler's card). Sent when either changes; the server gives them to the bike only from the one pedalling it
 * (RiddenBike.keys).
 */
public record BikeKeysPayload(boolean jump,boolean turbo) implements CustomPacketPayload {
    public static final Type<BikeKeysPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"bike_keys"));
    public static final StreamCodec<ByteBuf,BikeKeysPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.BOOL,BikeKeysPayload::jump,ByteBufCodecs.BOOL,BikeKeysPayload::turbo,BikeKeysPayload::new);
    @Override public Type<BikeKeysPayload> type(){return TYPE;}
}
