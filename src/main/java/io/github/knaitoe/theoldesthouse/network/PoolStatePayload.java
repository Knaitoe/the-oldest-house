package io.github.knaitoe.theoldesthouse.network;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
/**
 * 0.4.414: a pool table's frame as it stands (PoolGame's own save), who sits where and whose shot it is ({@code info}), sent to
 * everyone near enough to watch. {@code open} asks the receiver's screen to open on it (they used the table).
 */
public record PoolStatePayload(BlockPos table,CompoundTag game,CompoundTag info,boolean open) implements CustomPacketPayload {
    public static final Type<PoolStatePayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"pool_state"));
    public static final StreamCodec<ByteBuf,PoolStatePayload> STREAM_CODEC=StreamCodec.composite(BlockPos.STREAM_CODEC,PoolStatePayload::table,ByteBufCodecs.COMPOUND_TAG,PoolStatePayload::game,ByteBufCodecs.COMPOUND_TAG,PoolStatePayload::info,ByteBufCodecs.BOOL,PoolStatePayload::open,PoolStatePayload::new);
    @Override public Type<PoolStatePayload> type(){return TYPE;}
}
