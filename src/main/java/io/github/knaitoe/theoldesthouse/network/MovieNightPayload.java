package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Movie Night (0.4.102): a firework lighting the whole lake, or the reader's own canoe lifted from below. */
public record MovieNightPayload(int kind,int ticks) implements CustomPacketPayload {
    public static final int FLASH=0,ROCK=1;
    public static final Type<MovieNightPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"movie_night"));
    public static final StreamCodec<ByteBuf,MovieNightPayload> STREAM_CODEC=StreamCodec.composite(
            ByteBufCodecs.VAR_INT,MovieNightPayload::kind,ByteBufCodecs.VAR_INT,MovieNightPayload::ticks,MovieNightPayload::new);
    @Override public Type<MovieNightPayload> type(){return TYPE;}
}
