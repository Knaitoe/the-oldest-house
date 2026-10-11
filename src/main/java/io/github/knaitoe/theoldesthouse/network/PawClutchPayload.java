package io.github.knaitoe.theoldesthouse.network;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
/** The authoritative wish has already consumed one finger; only its owner receives the close-up. */
public record PawClutchPayload(int fingers) implements CustomPacketPayload {
    public static final Type<PawClutchPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"paw_clutch"));
    public static final StreamCodec<ByteBuf,PawClutchPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,PawClutchPayload::fingers,PawClutchPayload::new);
    @Override public Type<PawClutchPayload> type(){return TYPE;}
}
