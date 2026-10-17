package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** A line and a reaction shown over one Shallows speaker, to the one reader whose memory it is. */
public record ShallowsCuePayload(int entity,String line,int mood,int ticks) implements CustomPacketPayload {
    public static final Type<ShallowsCuePayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"shallows_cue"));
    public static final StreamCodec<ByteBuf,ShallowsCuePayload> STREAM_CODEC=StreamCodec.composite(
            ByteBufCodecs.VAR_INT,ShallowsCuePayload::entity,ByteBufCodecs.stringUtf8(256),ShallowsCuePayload::line,
            ByteBufCodecs.VAR_INT,ShallowsCuePayload::mood,ByteBufCodecs.VAR_INT,ShallowsCuePayload::ticks,ShallowsCuePayload::new);
    @Override public Type<ShallowsCuePayload> type(){return TYPE;}
}
