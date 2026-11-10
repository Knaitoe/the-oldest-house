package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 0.4.445: the comic from the fort's window opened in its reader's hands (labyrinth.BorrascaComic): whether it is the one the
 * window bleached, and the page they last had open.
 */
public record BorrascaComicPayload(boolean aged,int page) implements CustomPacketPayload {
    public static final Type<BorrascaComicPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"borrasca_comic"));
    public static final StreamCodec<ByteBuf,BorrascaComicPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.BOOL,BorrascaComicPayload::aged,ByteBufCodecs.VAR_INT,BorrascaComicPayload::page,BorrascaComicPayload::new);
    @Override public Type<BorrascaComicPayload> type(){return TYPE;}
}
