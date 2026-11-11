package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 0.4.446: a drugstore cabinet with somebody else at it, watched over their shoulder (labyrinth.BorrascaArcadeFriends). The game,
 * its seed, how its player plays (labyrinth.BorrascaArcade.Hand), how many ticks in it is, their three letters and the cabinet's
 * table. The screen plays the same game out of the same seed; nothing comes back.
 */
public record BorrascaArcadeWatchPayload(int game,long seed,int way,int elapsed,String name,CompoundTag table) implements CustomPacketPayload {
    public static final Type<BorrascaArcadeWatchPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"borrasca_arcade_watch"));
    public static final StreamCodec<ByteBuf,BorrascaArcadeWatchPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,BorrascaArcadeWatchPayload::game,ByteBufCodecs.VAR_LONG,BorrascaArcadeWatchPayload::seed,ByteBufCodecs.VAR_INT,BorrascaArcadeWatchPayload::way,ByteBufCodecs.VAR_INT,BorrascaArcadeWatchPayload::elapsed,ByteBufCodecs.stringUtf8(8),BorrascaArcadeWatchPayload::name,ByteBufCodecs.COMPOUND_TAG,BorrascaArcadeWatchPayload::table,BorrascaArcadeWatchPayload::new);
    @Override public Type<BorrascaArcadeWatchPayload> type(){return TYPE;}
}
