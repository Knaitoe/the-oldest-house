package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** The way out (labyrinth/WayOut) to its reader's client: arriving, a blip of static and how long, the end, the line, leaving. */
public record WayOutPayload(int kind,int value) implements CustomPacketPayload {
    public static final int ENTER=0,BLIP=1,END=2,CARD=3,LEAVE=4;
    public static final Type<WayOutPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"way_out"));
    public static final StreamCodec<ByteBuf,WayOutPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,WayOutPayload::kind,ByteBufCodecs.VAR_INT,WayOutPayload::value,WayOutPayload::new);
    @Override public Type<WayOutPayload> type(){return TYPE;}
}
