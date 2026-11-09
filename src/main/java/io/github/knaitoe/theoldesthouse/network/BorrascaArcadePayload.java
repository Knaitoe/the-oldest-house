package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 0.4.436: a drugstore cabinet's screen opened for one reader in Borrasca (labyrinth.BorrascaArcade). The game, the session's token
 * (its seed too), the reader's three letters and that reader's own table for the cabinet ("Rows" of N and S).
 */
public record BorrascaArcadePayload(int game,long token,String name,CompoundTag table) implements CustomPacketPayload {
    public static final Type<BorrascaArcadePayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"borrasca_arcade"));
    public static final StreamCodec<ByteBuf,BorrascaArcadePayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,BorrascaArcadePayload::game,ByteBufCodecs.VAR_LONG,BorrascaArcadePayload::token,ByteBufCodecs.stringUtf8(8),BorrascaArcadePayload::name,ByteBufCodecs.COMPOUND_TAG,BorrascaArcadePayload::table,BorrascaArcadePayload::new);
    @Override public Type<BorrascaArcadePayload> type(){return TYPE;}
}
