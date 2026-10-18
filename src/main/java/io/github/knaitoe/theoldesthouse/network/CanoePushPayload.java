package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Only a hand side is requested (-1 left, 1 right; doubled for a back stroke); the server derives the actual rider, canoe, wall and cooldown. */
public record CanoePushPayload(int side) implements CustomPacketPayload {
    public static final Type<CanoePushPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"canoe_push"));
    public static final StreamCodec<ByteBuf,CanoePushPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,CanoePushPayload::side,CanoePushPayload::new);
    @Override public Type<CanoePushPayload> type(){return TYPE;}
}
