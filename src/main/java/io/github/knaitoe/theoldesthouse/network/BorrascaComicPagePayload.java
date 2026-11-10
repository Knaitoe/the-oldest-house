package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** 0.4.445: a page of the comic its reader has had open for a moment (labyrinth.BorrascaComic.page). */
public record BorrascaComicPagePayload(int page) implements CustomPacketPayload {
    public static final Type<BorrascaComicPagePayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"borrasca_comic_page"));
    public static final StreamCodec<ByteBuf,BorrascaComicPagePayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,BorrascaComicPagePayload::page,BorrascaComicPagePayload::new);
    @Override public Type<BorrascaComicPagePayload> type(){return TYPE;}
}
