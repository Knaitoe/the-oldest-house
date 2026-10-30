package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** The reader's whole journal, sent only to its owner when they open it: the name in the cover, its wear and every written page. */
public record JournalOpenPayload(CompoundTag journal) implements CustomPacketPayload {
    public static final Type<JournalOpenPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"journal_open"));
    public static final StreamCodec<ByteBuf,JournalOpenPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.COMPOUND_TAG,JournalOpenPayload::journal,JournalOpenPayload::new);
    @Override public Type<JournalOpenPayload> type(){return TYPE;}
}
