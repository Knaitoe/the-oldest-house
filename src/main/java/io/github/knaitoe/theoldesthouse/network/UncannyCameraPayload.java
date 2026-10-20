package io.github.knaitoe.theoldesthouse.network;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
/** Actual first/third-person camera samples. The server bounds position, direction, FOV and monitor reach. */
public record UncannyCameraPayload(double x,double y,double z,double dx,double dy,double dz,float fov,float aspect,int monitor) implements CustomPacketPayload {
    public static final Type<UncannyCameraPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"uncanny_camera"));
    public static final StreamCodec<ByteBuf,UncannyCameraPayload> STREAM_CODEC=new StreamCodec<>(){
        @Override public UncannyCameraPayload decode(ByteBuf b){return new UncannyCameraPayload(b.readDouble(),b.readDouble(),b.readDouble(),b.readDouble(),b.readDouble(),b.readDouble(),b.readFloat(),b.readFloat(),b.readInt());}
        @Override public void encode(ByteBuf b,UncannyCameraPayload p){b.writeDouble(p.x).writeDouble(p.y).writeDouble(p.z).writeDouble(p.dx).writeDouble(p.dy).writeDouble(p.dz).writeFloat(p.fov).writeFloat(p.aspect).writeInt(p.monitor);}
    };
    @Override public Type<UncannyCameraPayload> type(){return TYPE;}
}
