package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** 0.4.268: the reader lays their journal's ribbon in a page, or takes it out (0). Kept only for a reader carrying their journal. */
public record JournalRibbonPayload(int page) implements CustomPacketPayload {
    public static final Type<JournalRibbonPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"journal_ribbon"));
    public static final StreamCodec<ByteBuf,JournalRibbonPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,JournalRibbonPayload::page,JournalRibbonPayload::new);
    @Override public Type<JournalRibbonPayload> type(){return TYPE;}
}
