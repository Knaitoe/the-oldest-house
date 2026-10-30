package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** A torn page opened in full for whoever holds or looks at it: its writer's name and the page as it was torn (journal/TornPages). */
public record TornPagePayload(String writer,CompoundTag page) implements CustomPacketPayload {
    public static final Type<TornPagePayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"torn_page"));
    public static final StreamCodec<ByteBuf,TornPagePayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.stringUtf8(64),TornPagePayload::writer,ByteBufCodecs.COMPOUND_TAG,TornPagePayload::page,TornPagePayload::new);
    @Override public Type<TornPagePayload> type(){return TYPE;}
}
