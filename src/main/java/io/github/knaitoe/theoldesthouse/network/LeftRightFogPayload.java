package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** 0.4.205, the Left-Right Game: the reader's fog. 0 none, 1 the road's tight brown dark, 2 the salt's bleached white, 3 the sinkhole's snow. */
public record LeftRightFogPayload(int mode) implements CustomPacketPayload {
    public static final Type<LeftRightFogPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"left_right_fog"));
    public static final StreamCodec<ByteBuf,LeftRightFogPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,LeftRightFogPayload::mode,LeftRightFogPayload::new);
    @Override public Type<LeftRightFogPayload> type(){return TYPE;}
}
