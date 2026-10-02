package io.github.knaitoe.theoldesthouse.network;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
public record PersonalProjectionPayload(BlockPos wall,int map) implements CustomPacketPayload {
    public static final Type<PersonalProjectionPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"personal_projection"));
    public static final StreamCodec<RegistryFriendlyByteBuf,PersonalProjectionPayload> STREAM_CODEC=StreamCodec.composite(BlockPos.STREAM_CODEC,PersonalProjectionPayload::wall,ByteBufCodecs.VAR_INT,PersonalProjectionPayload::map,PersonalProjectionPayload::new);
    @Override public Type<PersonalProjectionPayload> type(){return TYPE;}
}
