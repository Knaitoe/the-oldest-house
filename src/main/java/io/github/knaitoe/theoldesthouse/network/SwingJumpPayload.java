package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 0.4.423: the reader pressed jump on a swing; the server works out where the seat is and how fast it is going (HouseReadSwing.jump).
 * 0.4.468: or on an end of the park's seesaw, where it is a push off the sand (SeesawSeat.jump). Which it is, is the server's to say,
 * from what the reader is sitting on.
 */
public record SwingJumpPayload() implements CustomPacketPayload {
    public static final SwingJumpPayload INSTANCE=new SwingJumpPayload();
    public static final Type<SwingJumpPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"swing_jump"));
    public static final StreamCodec<ByteBuf,SwingJumpPayload> STREAM_CODEC=StreamCodec.unit(INSTANCE);
    @Override public Type<SwingJumpPayload> type(){return TYPE;}
}
