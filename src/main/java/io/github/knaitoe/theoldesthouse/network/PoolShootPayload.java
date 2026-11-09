package io.github.knaitoe.theoldesthouse.network;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
/** 0.4.414: a player's shot at a pool table: the angle and power, and where they placed the cue ball if they had it in hand. */
public record PoolShootPayload(BlockPos table,double angle,double power,double placeX,double placeY) implements CustomPacketPayload {
    public static final Type<PoolShootPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"pool_shoot"));
    public static final StreamCodec<ByteBuf,PoolShootPayload> STREAM_CODEC=StreamCodec.composite(BlockPos.STREAM_CODEC,PoolShootPayload::table,ByteBufCodecs.DOUBLE,PoolShootPayload::angle,ByteBufCodecs.DOUBLE,PoolShootPayload::power,ByteBufCodecs.DOUBLE,PoolShootPayload::placeX,ByteBufCodecs.DOUBLE,PoolShootPayload::placeY,PoolShootPayload::new);
    @Override public Type<PoolShootPayload> type(){return TYPE;}
}
