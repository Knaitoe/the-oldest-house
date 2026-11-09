package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** 0.4.426: the Ridge Service payphone. Opens its dial, or brings an open one up to date with what is left and the call just placed. */
public record BorrascaPayphonePayload(boolean open,CompoundTag state) implements CustomPacketPayload {
    public static final Type<BorrascaPayphonePayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"borrasca_payphone"));
    public static final StreamCodec<ByteBuf,BorrascaPayphonePayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.BOOL,BorrascaPayphonePayload::open,ByteBufCodecs.COMPOUND_TAG,BorrascaPayphonePayload::state,BorrascaPayphonePayload::new);
    @Override public Type<BorrascaPayphonePayload> type(){return TYPE;}
}
