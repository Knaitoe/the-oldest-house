package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Native local-player pose must agree with server collision at every crawl mouth. {@code pushX}/{@code pushZ} are the cave's
 * draught on the crawling body, per tick along the crawl (toward the camp on the out-breath, back in on the in-breath); the
 * client applies it to its own motion. (0.4.73: two axes, because the crawls wind.)
 */
public record CaverCrawlPayload(boolean active,int x,int y,int z,float pushX,float pushZ) implements CustomPacketPayload {
    public static final Type<CaverCrawlPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"caver_crawl"));
    public static final StreamCodec<ByteBuf,CaverCrawlPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.BOOL,CaverCrawlPayload::active,ByteBufCodecs.VAR_INT,CaverCrawlPayload::x,ByteBufCodecs.VAR_INT,CaverCrawlPayload::y,ByteBufCodecs.VAR_INT,CaverCrawlPayload::z,ByteBufCodecs.FLOAT,CaverCrawlPayload::pushX,ByteBufCodecs.FLOAT,CaverCrawlPayload::pushZ,CaverCrawlPayload::new);
    @Override public Type<CaverCrawlPayload> type(){return TYPE;}
}
