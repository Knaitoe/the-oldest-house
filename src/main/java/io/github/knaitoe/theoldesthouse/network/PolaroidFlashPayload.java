package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** 0.4.204, the Polaroid Trail: a camera flash over one reader's sight, for {@code ticks}, as bright as {@code strength} (0 to 1). */
public record PolaroidFlashPayload(int ticks,float strength) implements CustomPacketPayload {
    public static final Type<PolaroidFlashPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"polaroid_flash"));
    public static final StreamCodec<ByteBuf,PolaroidFlashPayload> STREAM_CODEC=StreamCodec.composite(
        ByteBufCodecs.VAR_INT,PolaroidFlashPayload::ticks,ByteBufCodecs.FLOAT,PolaroidFlashPayload::strength,PolaroidFlashPayload::new);
    @Override public Type<PolaroidFlashPayload> type(){return TYPE;}
}
