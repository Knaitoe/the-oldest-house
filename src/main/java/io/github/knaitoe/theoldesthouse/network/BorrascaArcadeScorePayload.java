package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 0.4.436: a drugstore game over, or its screen closed early: the session's token and the keys held on each tick, as runs
 * ({@code count<<5|keys}), and no score. The server plays the game again from its seed (labyrinth.BorrascaArcade.replay).
 */
public record BorrascaArcadeScorePayload(long token,List<Integer> runs) implements CustomPacketPayload {
    public static final int MAX_RUNS=6001;
    public static final Type<BorrascaArcadeScorePayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"borrasca_arcade_score"));
    public static final StreamCodec<ByteBuf,BorrascaArcadeScorePayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_LONG,BorrascaArcadeScorePayload::token,ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list(MAX_RUNS)),BorrascaArcadeScorePayload::runs,BorrascaArcadeScorePayload::new);
    @Override public Type<BorrascaArcadeScorePayload> type(){return TYPE;}
}
