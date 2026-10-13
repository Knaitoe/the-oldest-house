package io.github.knaitoe.theoldesthouse.network;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
/** Authoritative personal light/dawn state, refreshed while the reader is actually in the lake scene. */
public record LakeHouseViewPayload(int state) implements CustomPacketPayload {
    public static final Type<LakeHouseViewPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"lake_house_view"));
    public static final StreamCodec<ByteBuf,LakeHouseViewPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,LakeHouseViewPayload::state,LakeHouseViewPayload::new);
    @Override public Type<LakeHouseViewPayload> type(){return TYPE;}
}
