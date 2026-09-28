package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * The lower halves of every door that leads somewhere else. They never open
 * where they stand, so the client must not swing them open when clicked.
 */
public record HouseSealedDoorsPayload(List<GlobalPos> doors) implements CustomPacketPayload {
    public static final Type<HouseSealedDoorsPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, "sealed_doors")
    );

    public static final StreamCodec<ByteBuf, HouseSealedDoorsPayload> STREAM_CODEC = StreamCodec.composite(
            GlobalPos.STREAM_CODEC.apply(ByteBufCodecs.list()), HouseSealedDoorsPayload::doors,
            HouseSealedDoorsPayload::new
    );

    @Override
    public Type<HouseSealedDoorsPayload> type() {
        return TYPE;
    }
}
