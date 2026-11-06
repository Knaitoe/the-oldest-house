package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** The culvert's local collision pose, without moving the reader or changing their air. */
public record YardCrawlPayload(boolean active) implements CustomPacketPayload {
    public static final Type<YardCrawlPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"yard_crawl"));
    public static final StreamCodec<ByteBuf,YardCrawlPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.BOOL,YardCrawlPayload::active,YardCrawlPayload::new);
    @Override public Type<YardCrawlPayload> type(){return TYPE;}
}
