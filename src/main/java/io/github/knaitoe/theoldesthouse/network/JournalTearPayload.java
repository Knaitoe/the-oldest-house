package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** From the reader's open journal: tear this page out, or (back) take the torn page back into its stub (journal/TornPages). */
public record JournalTearPayload(int page,boolean back) implements CustomPacketPayload {
    public static final Type<JournalTearPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"journal_tear"));
    public static final StreamCodec<ByteBuf,JournalTearPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,JournalTearPayload::page,ByteBufCodecs.BOOL,JournalTearPayload::back,JournalTearPayload::new);
    @Override public Type<JournalTearPayload> type(){return TYPE;}
}
