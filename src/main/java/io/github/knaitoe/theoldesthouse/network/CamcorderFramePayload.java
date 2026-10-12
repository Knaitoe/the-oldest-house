package io.github.knaitoe.theoldesthouse.network;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
public record CamcorderFramePayload(UUID nonce,int[] pixels) implements CustomPacketPayload {
    public static final Type<CamcorderFramePayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"camcorder_frame"));
    public static final StreamCodec<RegistryFriendlyByteBuf,CamcorderFramePayload> STREAM_CODEC=new StreamCodec<>(){
        public CamcorderFramePayload decode(RegistryFriendlyByteBuf b){return new CamcorderFramePayload(b.readUUID(),b.readVarIntArray(384));}
        public void encode(RegistryFriendlyByteBuf b,CamcorderFramePayload p){b.writeUUID(p.nonce());b.writeVarIntArray(p.pixels());}
    };
    @Override public Type<CamcorderFramePayload> type(){return TYPE;}
}
