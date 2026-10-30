package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** The reader's hand in an open drawer: pull a leaf (its index) or, with -1, push the drawer shut. */
public record ZampanoDrawerActionPayload(int leaf) implements CustomPacketPayload {
    public static final int CLOSE=-1;
    public static final Type<ZampanoDrawerActionPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"zampano_drawer"));
    public static final StreamCodec<ByteBuf,ZampanoDrawerActionPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,ZampanoDrawerActionPayload::leaf,ZampanoDrawerActionPayload::new);
    @Override public Type<ZampanoDrawerActionPayload> type(){return TYPE;}
}
