package io.github.knaitoe.theoldesthouse.network;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
/** 0.4.411: a crouching player used an empty hand; the server takes off a worn prize if there is one (PrizeWear.takeOff). */
public record PrizeTakeOffPayload() implements CustomPacketPayload {
    public static final PrizeTakeOffPayload INSTANCE=new PrizeTakeOffPayload();
    public static final Type<PrizeTakeOffPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"prize_take_off"));
    public static final StreamCodec<ByteBuf,PrizeTakeOffPayload> STREAM_CODEC=StreamCodec.unit(INSTANCE);
    @Override public Type<PrizeTakeOffPayload> type(){return TYPE;}
}
