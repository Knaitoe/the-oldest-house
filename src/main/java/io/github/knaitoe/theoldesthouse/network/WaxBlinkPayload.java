package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** 0.4.750, the waxworks: a figure blinks, for this reader alone, for so many ticks (WaxNight.blink). */
public record WaxBlinkPayload(int entity,int ticks) implements CustomPacketPayload {
    public static final Type<WaxBlinkPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"wax_blink"));
    public static final StreamCodec<ByteBuf,WaxBlinkPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,WaxBlinkPayload::entity,ByteBufCodecs.VAR_INT,WaxBlinkPayload::ticks,WaxBlinkPayload::new);
    @Override public Type<WaxBlinkPayload> type(){return TYPE;}
}
