package io.github.knaitoe.theoldesthouse.network;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
/**
 * 0.4.415: a dartboard's game as it stands (DartsGame's own save), the chosen game, who plays and whose throw it is, sent to everyone
 * near enough to read the chalk.
 */
public record DartsStatePayload(BlockPos board,CompoundTag info) implements CustomPacketPayload {
    public static final Type<DartsStatePayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"darts_state"));
    public static final StreamCodec<ByteBuf,DartsStatePayload> STREAM_CODEC=StreamCodec.composite(BlockPos.STREAM_CODEC,DartsStatePayload::board,ByteBufCodecs.COMPOUND_TAG,DartsStatePayload::info,DartsStatePayload::new);
    @Override public Type<DartsStatePayload> type(){return TYPE;}
}
