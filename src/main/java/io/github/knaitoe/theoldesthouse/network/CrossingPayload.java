package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 0.4.351, The Last Crossing: the station's hour on its own day's clock (a day time, or -1 to clear), from the crew's shared run
 * (labyrinth/LastCrossing). 0.4.354, Night Two: also who of the crew has turned tonight, drawn in their drowned shape, and whose clothes a
 * claw has torn, drawn with the tear.
 */
public record CrossingPayload(long time,List<UUID> turned,List<UUID> torn) implements CustomPacketPayload {
    public static final Type<CrossingPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"last_crossing"));
    public static final StreamCodec<FriendlyByteBuf,CrossingPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_LONG,CrossingPayload::time,
        UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs.list(16)),CrossingPayload::turned,UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs.list(16)),CrossingPayload::torn,CrossingPayload::new);
    public CrossingPayload(long time){this(time,List.of(),List.of());}
    @Override public Type<CrossingPayload> type(){return TYPE;}
}
