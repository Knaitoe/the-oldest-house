package io.github.knaitoe.theoldesthouse.network;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
/** A short personal visual lease; no shared world mutation or client-earned Witness credit. */
public record UncannyViewPayload(CompoundTag view) implements CustomPacketPayload {
    public static final Type<UncannyViewPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"uncanny_view"));
    public static final StreamCodec<ByteBuf,UncannyViewPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.COMPOUND_TAG,UncannyViewPayload::view,UncannyViewPayload::new);
    @Override public Type<UncannyViewPayload> type(){return TYPE;}
}
