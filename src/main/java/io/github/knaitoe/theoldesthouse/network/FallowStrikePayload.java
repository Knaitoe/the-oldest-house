package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 0.4.674, Fallow's four minutes (docs/FALLOW_0_4_674.md): how far along the strike is at the station whose base this is, in ticks since
 * the tone, or -1 when there is none. The client draws the launches, their smoke, the shaking and the light from it (FallowStrikeClient).
 */
public record FallowStrikePayload(BlockPos base,int since) implements CustomPacketPayload {
    public static final Type<FallowStrikePayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"fallow_strike"));
    public static final StreamCodec<FriendlyByteBuf,FallowStrikePayload> STREAM_CODEC=StreamCodec.composite(
            BlockPos.STREAM_CODEC,FallowStrikePayload::base,ByteBufCodecs.VAR_INT,FallowStrikePayload::since,FallowStrikePayload::new);
    @Override public Type<FallowStrikePayload> type(){return TYPE;}
}
