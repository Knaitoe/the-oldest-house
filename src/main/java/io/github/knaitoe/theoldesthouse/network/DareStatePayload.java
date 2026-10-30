package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 0.4.173, "Third Floor Girls'": what the reader's own glass shows. {@code count} is how many times it has been said
 * (0 to 3), 4 once the reader is out; {@code inside} silences the music; {@code held} keeps the door shut under the
 * reader's hand while the handle is held from the hall.
 */
public record DareStatePayload(int count,boolean inside,boolean held) implements CustomPacketPayload {
    public static final Type<DareStatePayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"dare_state"));
    public static final StreamCodec<RegistryFriendlyByteBuf,DareStatePayload> STREAM_CODEC=new StreamCodec<>(){
        public DareStatePayload decode(RegistryFriendlyByteBuf b){return new DareStatePayload(b.readVarInt(),b.readBoolean(),b.readBoolean());}
        public void encode(RegistryFriendlyByteBuf b,DareStatePayload p){b.writeVarInt(p.count());b.writeBoolean(p.inside());b.writeBoolean(p.held());}
    };
    @Override public Type<DareStatePayload> type(){return TYPE;}
}
