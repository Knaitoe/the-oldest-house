package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Only this reader's earned, untaken account is shown on the shared table. */
public record EndingBookPayload(BlockPos at, boolean visible) implements CustomPacketPayload {
    public static final Type<EndingBookPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"ending_book"));
    public static final StreamCodec<RegistryFriendlyByteBuf,EndingBookPayload> STREAM_CODEC = new StreamCodec<>() {
        public EndingBookPayload decode(RegistryFriendlyByteBuf b) { return new EndingBookPayload(b.readBlockPos(),b.readBoolean()); }
        public void encode(RegistryFriendlyByteBuf b,EndingBookPayload p) { b.writeBlockPos(p.at());b.writeBoolean(p.visible()); }
    };
    @Override public Type<EndingBookPayload> type() { return TYPE; }
}
