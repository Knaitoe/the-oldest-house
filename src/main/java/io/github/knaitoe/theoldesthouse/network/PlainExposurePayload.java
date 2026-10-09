package io.github.knaitoe.theoldesthouse.network;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
public record PlainExposurePayload(UUID nonce) implements CustomPacketPayload {
    public static final Type<PlainExposurePayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"plain_exposure"));
    public static final StreamCodec<RegistryFriendlyByteBuf,PlainExposurePayload> STREAM_CODEC=new StreamCodec<>(){
        public PlainExposurePayload decode(RegistryFriendlyByteBuf b){return new PlainExposurePayload(b.readUUID());}
        public void encode(RegistryFriendlyByteBuf b,PlainExposurePayload p){b.writeUUID(p.nonce());}
    };
    @Override public Type<PlainExposurePayload> type(){return TYPE;}
}
