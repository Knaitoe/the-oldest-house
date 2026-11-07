package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 0.4.329: words another reader has just said aloud from their conversation panel (labyrinth/Spoken), to be shown over that
 * reader's head for {@code ticks} ticks. Never chat.
 */
public record SpokenPayload(int entity,String text,int ticks) implements CustomPacketPayload {
    public static final Type<SpokenPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"spoken"));
    public static final StreamCodec<ByteBuf,SpokenPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,SpokenPayload::entity,ByteBufCodecs.STRING_UTF8,SpokenPayload::text,ByteBufCodecs.VAR_INT,SpokenPayload::ticks,SpokenPayload::new);
    @Override public Type<SpokenPayload> type(){return TYPE;}
}
