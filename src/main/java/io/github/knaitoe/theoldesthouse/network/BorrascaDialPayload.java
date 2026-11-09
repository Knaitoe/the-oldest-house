package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** 0.4.426: the number the reader dialled on the payphone. Who answers, and what it costs, is the server's to decide. */
public record BorrascaDialPayload(String number) implements CustomPacketPayload {
    public static final Type<BorrascaDialPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"borrasca_dial"));
    public static final StreamCodec<ByteBuf,BorrascaDialPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.stringUtf8(16),BorrascaDialPayload::number,BorrascaDialPayload::new);
    @Override public Type<BorrascaDialPayload> type(){return TYPE;}
}
