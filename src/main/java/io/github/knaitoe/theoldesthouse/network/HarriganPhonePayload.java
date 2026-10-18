package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** The reader's phone as the server keeps it (clock, line, thread with Harrigan, the last call); open shows the screen. */
public record HarriganPhonePayload(boolean open,CompoundTag state) implements CustomPacketPayload {
    public static final Type<HarriganPhonePayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"harrigan_phone"));
    public static final StreamCodec<ByteBuf,HarriganPhonePayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.BOOL,HarriganPhonePayload::open,ByteBufCodecs.COMPOUND_TAG,HarriganPhonePayload::state,HarriganPhonePayload::new);
    @Override public Type<HarriganPhonePayload> type(){return TYPE;}
}
