package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 0.4.336, State Route 9: a reader struck on the road is about to die there. Their client holds its camera on where the body lies for
 * {@code ticks}, in place of the death screen, and then respawns them.
 */
public record StateRouteWreckPayload(double x,double y,double z,float yaw,int ticks) implements CustomPacketPayload {
    public static final Type<StateRouteWreckPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"state_route_wreck"));
    public static final StreamCodec<ByteBuf,StateRouteWreckPayload> STREAM_CODEC=StreamCodec.composite(
        ByteBufCodecs.DOUBLE,StateRouteWreckPayload::x,ByteBufCodecs.DOUBLE,StateRouteWreckPayload::y,ByteBufCodecs.DOUBLE,StateRouteWreckPayload::z,
        ByteBufCodecs.FLOAT,StateRouteWreckPayload::yaw,ByteBufCodecs.VAR_INT,StateRouteWreckPayload::ticks,StateRouteWreckPayload::new);
    @Override public Type<StateRouteWreckPayload> type(){return TYPE;}
}
