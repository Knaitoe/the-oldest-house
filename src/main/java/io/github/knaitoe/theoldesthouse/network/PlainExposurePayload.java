package io.github.knaitoe.theoldesthouse.network;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
/** Asks the composing client for one exposure, and says where its distant figure stands. */
public record PlainExposurePayload(UUID nonce,double x,double y,double z) implements CustomPacketPayload {
    public static final Type<PlainExposurePayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"plain_exposure"));
    public static final StreamCodec<RegistryFriendlyByteBuf,PlainExposurePayload> STREAM_CODEC=new StreamCodec<>(){
        public PlainExposurePayload decode(RegistryFriendlyByteBuf b){return new PlainExposurePayload(b.readUUID(),b.readDouble(),b.readDouble(),b.readDouble());}
        public void encode(RegistryFriendlyByteBuf b,PlainExposurePayload p){b.writeUUID(p.nonce());b.writeDouble(p.x());b.writeDouble(p.y());b.writeDouble(p.z());}
    };
    @Override public Type<PlainExposurePayload> type(){return TYPE;}
}
