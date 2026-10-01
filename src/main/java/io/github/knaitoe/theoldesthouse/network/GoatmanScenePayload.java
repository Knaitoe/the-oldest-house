package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** The demand is a subtitle at the door, never an instruction or a suspect label. */
public record GoatmanScenePayload(int phase,int demand,int remaining) implements CustomPacketPayload {
    public static final Type<GoatmanScenePayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"trailer_scene"));
    public static final StreamCodec<ByteBuf,GoatmanScenePayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,GoatmanScenePayload::phase,ByteBufCodecs.VAR_INT,GoatmanScenePayload::demand,ByteBufCodecs.VAR_INT,GoatmanScenePayload::remaining,GoatmanScenePayload::new);
    @Override public Type<GoatmanScenePayload> type(){return TYPE;}
}
