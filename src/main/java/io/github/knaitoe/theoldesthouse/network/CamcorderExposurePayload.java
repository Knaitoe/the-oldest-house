package io.github.knaitoe.theoldesthouse.network;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
public record CamcorderExposurePayload(UUID nonce) implements CustomPacketPayload {
    public static final Type<CamcorderExposurePayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"camcorder_exposure"));
    public static final StreamCodec<RegistryFriendlyByteBuf,CamcorderExposurePayload> STREAM_CODEC=new StreamCodec<>(){
        public CamcorderExposurePayload decode(RegistryFriendlyByteBuf b){return new CamcorderExposurePayload(b.readUUID());}
        public void encode(RegistryFriendlyByteBuf b,CamcorderExposurePayload p){b.writeUUID(p.nonce());}
    };
    @Override public Type<CamcorderExposurePayload> type(){return TYPE;}
}
