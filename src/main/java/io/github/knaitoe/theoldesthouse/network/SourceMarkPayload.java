package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** 0.4.273: where the paper a vignette is waiting on this reader to read lies, or that it has been read (labyrinth/SourceMark). */
public record SourceMarkPayload(boolean on,BlockPos at) implements CustomPacketPayload {
    public static final Type<SourceMarkPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"source_mark"));
    public static final StreamCodec<FriendlyByteBuf,SourceMarkPayload> STREAM_CODEC=StreamCodec.composite(
            ByteBufCodecs.BOOL,SourceMarkPayload::on,BlockPos.STREAM_CODEC,SourceMarkPayload::at,SourceMarkPayload::new);
    @Override public Type<SourceMarkPayload> type(){return TYPE;}
}
