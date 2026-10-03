package io.github.knaitoe.theoldesthouse.network;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
/** Bounded, server-only presentation: blind bell direction, blizzard, and a short party flash. */
public record HotelAtmospherePayload(int mode,BlockPos source,boolean lie,int flash) implements CustomPacketPayload {
    public static final Type<HotelAtmospherePayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"hotel_atmosphere"));
    public static final StreamCodec<ByteBuf,HotelAtmospherePayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,HotelAtmospherePayload::mode,BlockPos.STREAM_CODEC,HotelAtmospherePayload::source,ByteBufCodecs.BOOL,HotelAtmospherePayload::lie,ByteBufCodecs.VAR_INT,HotelAtmospherePayload::flash,HotelAtmospherePayload::new);
    @Override public Type<HotelAtmospherePayload> type(){return TYPE;}
}
