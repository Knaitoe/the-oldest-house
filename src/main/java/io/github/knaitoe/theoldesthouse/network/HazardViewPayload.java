package io.github.knaitoe.theoldesthouse.network;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
/** A private, short-lived lease for the reader's delayed mirror or local listing tilt. */
public record HazardViewPayload(int mode,BlockPos base,CompoundTag view) implements CustomPacketPayload {
    public static final Type<HazardViewPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"hazard_view"));
    public static final StreamCodec<ByteBuf,HazardViewPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,HazardViewPayload::mode,BlockPos.STREAM_CODEC,HazardViewPayload::base,ByteBufCodecs.COMPOUND_TAG,HazardViewPayload::view,HazardViewPayload::new);
    public static HazardViewPayload clear(){return new HazardViewPayload(0,BlockPos.ZERO,new CompoundTag());}
    public static HazardViewPayload list(BlockPos b,float tilt,float push){var t=new CompoundTag();t.putFloat("Tilt",tilt);t.putFloat("Push",push);return new HazardViewPayload(2,b,t);}
    public static HazardViewPayload mirror(BlockPos b,double x,double y,double z,float yaw,float pitch,boolean crouch,float stride){var t=new CompoundTag();t.putDouble("X",x);t.putDouble("Y",y);t.putDouble("Z",z);t.putFloat("Yaw",yaw);t.putFloat("Pitch",pitch);t.putBoolean("Crouch",crouch);t.putFloat("Stride",stride);return new HazardViewPayload(1,b,t);}
    @Override public Type<HazardViewPayload> type(){return TYPE;}
}
