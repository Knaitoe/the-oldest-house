package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Requests manual reading or finishes the reader's current private story card. */
public record HouseReadCardActionPayload(int token,boolean finished) implements CustomPacketPayload {
    public static final Type<HouseReadCardActionPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"house_read_card_action"));
    public static final StreamCodec<ByteBuf,HouseReadCardActionPayload> STREAM_CODEC=StreamCodec.composite(
            ByteBufCodecs.VAR_INT,HouseReadCardActionPayload::token,ByteBufCodecs.BOOL,HouseReadCardActionPayload::finished,HouseReadCardActionPayload::new);
    @Override public Type<HouseReadCardActionPayload> type(){return TYPE;}
}
