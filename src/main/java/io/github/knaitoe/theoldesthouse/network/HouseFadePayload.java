package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Fade the screen to black and back, in ticks: covers a door that moves the
 * player within one dimension, and the lights going out in a vignette.
 */
public record HouseFadePayload(int fadeIn, int hold, int fadeOut) implements CustomPacketPayload {
    public static final Type<HouseFadePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, "fade")
    );

    public static final StreamCodec<ByteBuf, HouseFadePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, HouseFadePayload::fadeIn,
            ByteBufCodecs.VAR_INT, HouseFadePayload::hold,
            ByteBufCodecs.VAR_INT, HouseFadePayload::fadeOut,
            HouseFadePayload::new
    );

    @Override
    public Type<HouseFadePayload> type() {
        return TYPE;
    }
}
