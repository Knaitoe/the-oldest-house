package io.github.knaitoe.theoldesthouse.network;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
/** One bounded map-sized exposure. The server separately validates the sender's real aim and pending nonce. */
public record PlainFramePayload(UUID nonce,byte[] pixels) implements CustomPacketPayload {
    public static final Type<PlainFramePayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"plain_frame"));
    public static final StreamCodec<RegistryFriendlyByteBuf,PlainFramePayload> STREAM_CODEC=new StreamCodec<>(){
        public PlainFramePayload decode(RegistryFriendlyByteBuf b){return new PlainFramePayload(b.readUUID(),b.readByteArray(16384));}
        public void encode(RegistryFriendlyByteBuf b,PlainFramePayload p){b.writeUUID(p.nonce());b.writeByteArray(p.pixels());}
    };
    @Override public Type<PlainFramePayload> type(){return TYPE;}
}
