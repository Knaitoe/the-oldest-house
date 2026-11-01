package io.github.knaitoe.theoldesthouse.network;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
/**
 * 0.4.217, Polybius: a game over (or a screen closed mid-game). 0.4.225: it carries the session's token and the keys held on each
 * tick, as runs ({@code count<<5|keys}), and no score: the server plays the game again from its own seed and keeps the score it
 * gets ({@code PolybiusGames.replay}). A log is at most one run a tick of the longest game.
 */
public record PolybiusScorePayload(long token,List<Integer> runs) implements CustomPacketPayload {
    public static final int MAX_RUNS=12001;
    public static final Type<PolybiusScorePayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"polybius_score"));
    public static final StreamCodec<ByteBuf,PolybiusScorePayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_LONG,PolybiusScorePayload::token,ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list(MAX_RUNS)),PolybiusScorePayload::runs,PolybiusScorePayload::new);
    @Override public Type<PolybiusScorePayload> type(){return TYPE;}
}
