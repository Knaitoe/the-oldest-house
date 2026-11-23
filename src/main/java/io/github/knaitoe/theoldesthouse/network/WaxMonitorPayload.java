package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** 0.4.752, the waxworks: sit down at the bank of monitors (the museum's base, so the client knows where its six cameras are), and whether the screens are dark. */
public record WaxMonitorPayload(BlockPos base,boolean dark,String time) implements CustomPacketPayload {
    public static final Type<WaxMonitorPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"wax_monitor"));
    public static final StreamCodec<ByteBuf,WaxMonitorPayload> STREAM_CODEC=StreamCodec.composite(BlockPos.STREAM_CODEC,WaxMonitorPayload::base,ByteBufCodecs.BOOL,WaxMonitorPayload::dark,ByteBufCodecs.STRING_UTF8,WaxMonitorPayload::time,WaxMonitorPayload::new);
    @Override public Type<WaxMonitorPayload> type(){return TYPE;}
}
