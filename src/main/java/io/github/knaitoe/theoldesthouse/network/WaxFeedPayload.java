package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** 0.4.752, the waxworks: the feed a reader at the monitors is looking at (-1 when they stand up), and whether they marked something moved on it. */
public record WaxFeedPayload(int feed,boolean note) implements CustomPacketPayload {
    public static final Type<WaxFeedPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"wax_feed"));
    public static final StreamCodec<ByteBuf,WaxFeedPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,WaxFeedPayload::feed,ByteBufCodecs.BOOL,WaxFeedPayload::note,WaxFeedPayload::new);
    @Override public Type<WaxFeedPayload> type(){return TYPE;}
}
