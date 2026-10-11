package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Native local-player pose must agree with server collision at every crawl mouth. {@code pushX}/{@code pushZ} are the cave's
 * draught on the crawling body, per tick along the crawl (toward the camp on the out-breath, back in on the in-breath); the
 * client applies it to its own motion. (0.4.73: two axes, because the crawls wind.) 0.4.75: {@code air} is the reader's own
 * air, 0 to 1, drawn by their own gauge instead of Minecraft's bubbles; {@code choke} counts the ticks since it ran out, past
 * {@code CaverVignette.CHOKE} into the blackout, or 0 while they can breathe.
 */
public record CaverCrawlPayload(boolean active,int x,int y,int z,float pushX,float pushZ,float air,int choke) implements CustomPacketPayload {
    public static final Type<CaverCrawlPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"caver_crawl"));
    public static final StreamCodec<ByteBuf,CaverCrawlPayload> STREAM_CODEC=new StreamCodec<>(){
        public CaverCrawlPayload decode(ByteBuf b){return new CaverCrawlPayload(ByteBufCodecs.BOOL.decode(b),ByteBufCodecs.VAR_INT.decode(b),ByteBufCodecs.VAR_INT.decode(b),
                ByteBufCodecs.VAR_INT.decode(b),ByteBufCodecs.FLOAT.decode(b),ByteBufCodecs.FLOAT.decode(b),ByteBufCodecs.FLOAT.decode(b),ByteBufCodecs.VAR_INT.decode(b));}
        public void encode(ByteBuf b,CaverCrawlPayload p){ByteBufCodecs.BOOL.encode(b,p.active());ByteBufCodecs.VAR_INT.encode(b,p.x());ByteBufCodecs.VAR_INT.encode(b,p.y());
            ByteBufCodecs.VAR_INT.encode(b,p.z());ByteBufCodecs.FLOAT.encode(b,p.pushX());ByteBufCodecs.FLOAT.encode(b,p.pushZ());ByteBufCodecs.FLOAT.encode(b,p.air());ByteBufCodecs.VAR_INT.encode(b,p.choke());}
    };
    @Override public Type<CaverCrawlPayload> type(){return TYPE;}
}
