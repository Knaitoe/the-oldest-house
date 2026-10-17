package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Only which strafe key was pressed (-1 left, 1 right); the server derives the reader, the carry and the stroke. */
public record ShallowsSwingPayload(int side) implements CustomPacketPayload {
    public static final Type<ShallowsSwingPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"shallows_swing"));
    public static final StreamCodec<ByteBuf,ShallowsSwingPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,ShallowsSwingPayload::side,ShallowsSwingPayload::new);
    @Override public Type<ShallowsSwingPayload> type(){return TYPE;}
}
