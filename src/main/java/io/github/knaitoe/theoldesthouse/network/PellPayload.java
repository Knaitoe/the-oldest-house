package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 0.4.526, the Pell house (labyrinth/PellVisit): the house's own hour (a day time) and whether it is going on (1, a tick of the day to
 * a game tick) or held where it is (0), so the client keeps the sun moving between packets without stepping.
 */
public record PellPayload(long time,int rate) implements CustomPacketPayload {
    public static final Type<PellPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"pell_house"));
    public static final StreamCodec<FriendlyByteBuf,PellPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_LONG,PellPayload::time,ByteBufCodecs.VAR_INT,PellPayload::rate,PellPayload::new);
    @Override public Type<PellPayload> type(){return TYPE;}
}
