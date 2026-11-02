package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 0.4.267, the hall drawer open on a reader's screen (house/HouseKeeping): which menu it is, the open page (from 0), how many pages
 * there are and how many stacks the drawer holds, so the screen can show its buttons and say how much is kept.
 */
public record KeptStatePayload(int container,int page,int pages,int stacks) implements CustomPacketPayload {
    public static final Type<KeptStatePayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"kept_state"));
    public static final StreamCodec<ByteBuf,KeptStatePayload> STREAM_CODEC=StreamCodec.composite(
        ByteBufCodecs.VAR_INT,KeptStatePayload::container,ByteBufCodecs.VAR_INT,KeptStatePayload::page,
        ByteBufCodecs.VAR_INT,KeptStatePayload::pages,ByteBufCodecs.VAR_INT,KeptStatePayload::stacks,KeptStatePayload::new);
    @Override public Type<KeptStatePayload> type(){return TYPE;}
}
