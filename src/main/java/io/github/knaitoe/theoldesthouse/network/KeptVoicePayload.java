package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 0.4.675 (owner, of the Kept: "the unique dialogue formatting for AM is good, however it doesn't fit into the screen"). The machine's
 * line, to one reader, for so many ticks: drawn large across the middle of the view as before, but wrapped to fit it
 * (client.KeptVoiceClient). It had gone as the vanilla subtitle, which is one line however long the words.
 */
public record KeptVoicePayload(String text,int ticks) implements CustomPacketPayload {
    public static final Type<KeptVoicePayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"kept_voice"));
    public static final StreamCodec<ByteBuf,KeptVoicePayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.stringUtf8(1024),KeptVoicePayload::text,ByteBufCodecs.VAR_INT,KeptVoicePayload::ticks,KeptVoicePayload::new);
    @Override public Type<KeptVoicePayload> type(){return TYPE;}
}
