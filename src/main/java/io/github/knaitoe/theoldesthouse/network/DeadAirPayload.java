package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** 0.4.206, the Dead Air Broadcast: what the set is tuned to (-1 off the set, 0 static, 1 the stage, 2 the marsh, 3 dead air for one reader) and how many ticks of dead air are left. */
public record DeadAirPayload(int channel,int dead) implements CustomPacketPayload {
    public static final Type<DeadAirPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"dead_air"));
    public static final StreamCodec<ByteBuf,DeadAirPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,DeadAirPayload::channel,ByteBufCodecs.VAR_INT,DeadAirPayload::dead,DeadAirPayload::new);
    @Override public Type<DeadAirPayload> type(){return TYPE;}
}
