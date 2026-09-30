package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Nearby hints for this viewer's deals. No shared block state is changed to paint a hint. */
public record DoorLeaksPayload(ResourceLocation dimension, boolean labyrinth, List<Leak> leaks) implements CustomPacketPayload {
    public record Leak(BlockPos door, int facing, int kind) {
        public static final StreamCodec<ByteBuf, Leak> CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, Leak::door, ByteBufCodecs.VAR_INT, Leak::facing,
                ByteBufCodecs.VAR_INT, Leak::kind, Leak::new);
    }
    public static final Type<DoorLeaksPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, "door_leaks"));
    public static final StreamCodec<ByteBuf, DoorLeaksPayload> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, DoorLeaksPayload::dimension, ByteBufCodecs.BOOL, DoorLeaksPayload::labyrinth,
            Leak.CODEC.apply(ByteBufCodecs.list()), DoorLeaksPayload::leaks, DoorLeaksPayload::new);
    @Override public Type<DoorLeaksPayload> type() { return TYPE; }
}
