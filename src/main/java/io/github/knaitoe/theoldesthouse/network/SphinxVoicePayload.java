package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 0.4.341, the Sphinx speaking to a reader who hears her (docs/SPHINX_FACE_0_4_337.md, "0.4.341: her mouth"): for {@code ticks} ticks her
 * mouth moves wherever that reader can see her face. The owner: "Ensure the mouth moves when she speaks too."
 */
public record SphinxVoicePayload(int ticks) implements CustomPacketPayload {
    public static final Type<SphinxVoicePayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"sphinx_voice"));
    public static final StreamCodec<ByteBuf,SphinxVoicePayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,SphinxVoicePayload::ticks,SphinxVoicePayload::new);
    @Override public Type<SphinxVoicePayload> type(){return TYPE;}
}
