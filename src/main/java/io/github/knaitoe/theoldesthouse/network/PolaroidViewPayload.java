package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** 0.4.204, the Polaroid Trail: a developed photograph held up to look at: its picture (64 by 48, one byte of brightness a pixel) and its caption. */
public record PolaroidViewPayload(byte[] frame,String caption) implements CustomPacketPayload {
    public static final Type<PolaroidViewPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"polaroid_view"));
    public static final StreamCodec<ByteBuf,PolaroidViewPayload> STREAM_CODEC=StreamCodec.composite(
        ByteBufCodecs.BYTE_ARRAY,PolaroidViewPayload::frame,ByteBufCodecs.STRING_UTF8,PolaroidViewPayload::caption,PolaroidViewPayload::new);
    @Override public Type<PolaroidViewPayload> type(){return TYPE;}
}
