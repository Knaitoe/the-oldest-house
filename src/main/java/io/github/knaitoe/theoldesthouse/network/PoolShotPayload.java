package io.github.knaitoe.theoldesthouse.network;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
/**
 * 0.4.414: a shot the server has played: the table before it, the shot itself (angle and power, and where the cue ball was placed if
 * it was in hand), and the table after. A watching client plays the same shot from the same table to animate it, and then takes the
 * server's table as it is.
 */
public record PoolShotPayload(BlockPos table,CompoundTag before,double angle,double power,double placeX,double placeY,CompoundTag after,CompoundTag info) implements CustomPacketPayload {
    public static final Type<PoolShotPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"pool_shot"));
    public static final StreamCodec<ByteBuf,PoolShotPayload> STREAM_CODEC=new StreamCodec<>(){
        @Override public PoolShotPayload decode(ByteBuf b){return new PoolShotPayload(BlockPos.STREAM_CODEC.decode(b),ByteBufCodecs.COMPOUND_TAG.decode(b),b.readDouble(),b.readDouble(),b.readDouble(),b.readDouble(),ByteBufCodecs.COMPOUND_TAG.decode(b),ByteBufCodecs.COMPOUND_TAG.decode(b));}
        @Override public void encode(ByteBuf b,PoolShotPayload p){BlockPos.STREAM_CODEC.encode(b,p.table());ByteBufCodecs.COMPOUND_TAG.encode(b,p.before());b.writeDouble(p.angle());b.writeDouble(p.power());b.writeDouble(p.placeX());b.writeDouble(p.placeY());ByteBufCodecs.COMPOUND_TAG.encode(b,p.after());ByteBufCodecs.COMPOUND_TAG.encode(b,p.info());}
    };
    @Override public Type<PoolShotPayload> type(){return TYPE;}
}
