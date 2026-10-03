package io.github.knaitoe.theoldesthouse.network;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
/** A bounded presentation lease. It never changes the explorer's position or native game mode. */
public record SeanceViewPayload(int actorId,int ticks,boolean empty) implements CustomPacketPayload {
    public static final Type<SeanceViewPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"seance_view"));
    public static final StreamCodec<ByteBuf,SeanceViewPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,SeanceViewPayload::actorId,ByteBufCodecs.VAR_INT,SeanceViewPayload::ticks,ByteBufCodecs.BOOL,SeanceViewPayload::empty,SeanceViewPayload::new);
    @Override public Type<SeanceViewPayload> type(){return TYPE;}
}
