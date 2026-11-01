package io.github.knaitoe.theoldesthouse.network;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
/** 0.4.217, Polybius: a game over (or a screen closed mid-game): the session's token, the score and how many ticks were played. The server decides whether it stands. */
public record PolybiusScorePayload(long token,int score,int ticks) implements CustomPacketPayload {
    public static final Type<PolybiusScorePayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"polybius_score"));
    public static final StreamCodec<ByteBuf,PolybiusScorePayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_LONG,PolybiusScorePayload::token,ByteBufCodecs.VAR_INT,PolybiusScorePayload::score,ByteBufCodecs.VAR_INT,PolybiusScorePayload::ticks,PolybiusScorePayload::new);
    @Override public Type<PolybiusScorePayload> type(){return TYPE;}
}
