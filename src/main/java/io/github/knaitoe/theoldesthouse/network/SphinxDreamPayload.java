package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 0.4.342, the Sphinx's dream on the dreamer's screen: her face filling it before the first waking ({@link #FACE}), the face still
 * there as the first waking begins and fading off it ({@link #WAKE}), the first waking going dark ({@link #FADE}), the second waking
 * coming up out of the dark ({@link #CLEAR}), or nothing ({@link #NONE}). {@code conduct} is the dream's manner (patient, dismissive,
 * hostile), which sets how close her breath comes.
 */
public record SphinxDreamPayload(int stage,int conduct) implements CustomPacketPayload {
    public static final int NONE=0,FACE=1,WAKE=2,FADE=3,CLEAR=4;
    public static final Type<SphinxDreamPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"sphinx_dream"));
    public static final StreamCodec<ByteBuf,SphinxDreamPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,SphinxDreamPayload::stage,ByteBufCodecs.VAR_INT,SphinxDreamPayload::conduct,SphinxDreamPayload::new);
    @Override public Type<SphinxDreamPayload> type(){return TYPE;}
}
