package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 0.4.536, the Pell house's tapes (labyrinth/PellTapes): the tape the shed's deck is playing, where its screen is and which way it
 * faces, the game tick it began on and the reading pace its lines keep (Dialogue), so a reader's client draws its picture and its
 * captions in time with the house. A tape of -1: nothing is playing.
 */
public record PellTapePayload(BlockPos screen,int facing,int tape,long start,int pace) implements CustomPacketPayload {
    public static final Type<PellTapePayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"pell_tape"));
    public static final StreamCodec<FriendlyByteBuf,PellTapePayload> STREAM_CODEC=StreamCodec.composite(
        BlockPos.STREAM_CODEC,PellTapePayload::screen,ByteBufCodecs.VAR_INT,PellTapePayload::facing,ByteBufCodecs.VAR_INT,PellTapePayload::tape,
        ByteBufCodecs.VAR_LONG,PellTapePayload::start,ByteBufCodecs.VAR_INT,PellTapePayload::pace,PellTapePayload::new);
    /** Nothing playing. */
    public static final PellTapePayload STOP=new PellTapePayload(BlockPos.ZERO,0,-1,0,100);
    @Override public Type<PellTapePayload> type(){return TYPE;}
}
