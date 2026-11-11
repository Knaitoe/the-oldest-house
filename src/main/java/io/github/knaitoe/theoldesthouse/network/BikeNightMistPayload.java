package io.github.knaitoe.theoldesthouse.network;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
/**
 * 0.4.491, Bike night: how thick one reader's own mist is in the graveyard, 0 to 100 (BikeNightGraveyard.mist). Sent while
 * they are in it, and once as nothing when they leave; the client lets it go by itself if it stops hearing.
 */
public record BikeNightMistPayload(int mist) implements CustomPacketPayload {
    public static final Type<BikeNightMistPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"bike_night_mist"));
    public static final StreamCodec<ByteBuf,BikeNightMistPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,BikeNightMistPayload::mist,BikeNightMistPayload::new);
    @Override public Type<BikeNightMistPayload> type(){return TYPE;}
}
