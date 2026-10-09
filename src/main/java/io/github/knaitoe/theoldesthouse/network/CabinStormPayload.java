package io.github.knaitoe.theoldesthouse.network;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
/**
 * One reader's own weather and scene at the cabin: storm strength 0-100, the scene being played (0 none, 1 a heart
 * given, 2 the arm, 3 the storm breaking) and how many ticks into it the server is. A short lease; leaving ends it.
 */
public record CabinStormPayload(int storm,int scene,int sceneTicks,int flash) implements CustomPacketPayload {
    public static final Type<CabinStormPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"cabin_storm"));
    public static final StreamCodec<ByteBuf,CabinStormPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,CabinStormPayload::storm,ByteBufCodecs.VAR_INT,CabinStormPayload::scene,ByteBufCodecs.VAR_INT,CabinStormPayload::sceneTicks,ByteBufCodecs.VAR_INT,CabinStormPayload::flash,CabinStormPayload::new);
    @Override public Type<CabinStormPayload> type(){return TYPE;}
}
