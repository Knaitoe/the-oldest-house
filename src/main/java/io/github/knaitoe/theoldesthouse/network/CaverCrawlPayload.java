package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Native local-player pose must agree with server collision at both open mouths. {@code breath} is the cave's draught on
 * the crawling body, per tick along +z (toward the camp) or -z (back into the cave); the client applies it to its own motion.
 */
public record CaverCrawlPayload(boolean active,int x,int y,int z,float breath) implements CustomPacketPayload {
    public static final Type<CaverCrawlPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"caver_crawl"));
    public static final StreamCodec<ByteBuf,CaverCrawlPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.BOOL,CaverCrawlPayload::active,ByteBufCodecs.VAR_INT,CaverCrawlPayload::x,ByteBufCodecs.VAR_INT,CaverCrawlPayload::y,ByteBufCodecs.VAR_INT,CaverCrawlPayload::z,ByteBufCodecs.FLOAT,CaverCrawlPayload::breath,CaverCrawlPayload::new);
    @Override public Type<CaverCrawlPayload> type(){return TYPE;}
}
