package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** 0.4.459: the Garritys' letter held up before a reader (labyrinth.BorrascaGarritys): as it was handed over, or years on. */
public record BorrascaLetterPayload(boolean aged) implements CustomPacketPayload {
    public static final Type<BorrascaLetterPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"borrasca_letter"));
    public static final StreamCodec<ByteBuf,BorrascaLetterPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.BOOL,BorrascaLetterPayload::aged,BorrascaLetterPayload::new);
    @Override public Type<BorrascaLetterPayload> type(){return TYPE;}
}
