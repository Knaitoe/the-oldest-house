package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 0.4.662, the feed (docs/HALL_FEED_0_4_662.md). To one reader's game alone. Mode {@link #ARM}: watch the hall the reader stands in
 * from a camera at x, y, z. {@link #SHOW}: the hall just left is on a monitor on the wall of this one, its cell at x, y, z, its screen
 * turned to {@code facing} (a horizontal direction's 2D index), the reader having left at game time {@code time}. {@link #HIDE}: the
 * monitor is gone. {@link #DROP}: forget all of it.
 */
public record HallFeedPayload(int mode,double x,double y,double z,int facing,long time) implements CustomPacketPayload {
    public static final int DROP=0,ARM=1,SHOW=2,HIDE=3;
    public static final Type<HallFeedPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"hall_feed"));
    public static final StreamCodec<ByteBuf,HallFeedPayload> STREAM_CODEC=StreamCodec.composite(
            ByteBufCodecs.VAR_INT,HallFeedPayload::mode,ByteBufCodecs.DOUBLE,HallFeedPayload::x,ByteBufCodecs.DOUBLE,HallFeedPayload::y,ByteBufCodecs.DOUBLE,HallFeedPayload::z,
            ByteBufCodecs.VAR_INT,HallFeedPayload::facing,ByteBufCodecs.VAR_LONG,HallFeedPayload::time,HallFeedPayload::new);
    @Override public Type<HallFeedPayload> type(){return TYPE;}
}
