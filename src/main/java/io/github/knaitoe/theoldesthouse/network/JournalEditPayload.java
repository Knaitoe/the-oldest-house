package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** One page as the reader left it: their writing and the lines they placed. The server checks it and stamps where it was begun. */
public record JournalEditPayload(int page,CompoundTag content) implements CustomPacketPayload {
    public static final Type<JournalEditPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"journal_edit"));
    public static final StreamCodec<ByteBuf,JournalEditPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,JournalEditPayload::page,ByteBufCodecs.compoundTagCodec(()->NbtAccounter.create(32768)),JournalEditPayload::content,JournalEditPayload::new);
    @Override public Type<JournalEditPayload> type(){return TYPE;}
}
