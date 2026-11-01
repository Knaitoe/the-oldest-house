package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 0.4.203, Work print: something of the film over one reader's own sight, for {@code ticks}. {@code kind} 0 is the grain and
 * tracking lines, 1 a frame of their own footage for a moment ({@code frame}, 64 by 48), 2 the wet handprints on the inside of
 * the CRT's glass after the seventh call.
 */
public record WorkPrintHauntPayload(int kind,int ticks,byte[] frame) implements CustomPacketPayload {
    public static final Type<WorkPrintHauntPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"work_print_haunt"));
    public static final StreamCodec<ByteBuf,WorkPrintHauntPayload> STREAM_CODEC=StreamCodec.composite(
        ByteBufCodecs.VAR_INT,WorkPrintHauntPayload::kind,ByteBufCodecs.VAR_INT,WorkPrintHauntPayload::ticks,ByteBufCodecs.BYTE_ARRAY,WorkPrintHauntPayload::frame,WorkPrintHauntPayload::new);
    @Override public Type<WorkPrintHauntPayload> type(){return TYPE;}
}
