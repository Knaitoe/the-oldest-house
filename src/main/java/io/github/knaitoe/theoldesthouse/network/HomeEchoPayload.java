package io.github.knaitoe.theoldesthouse.network;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
public record HomeEchoPayload(BlockPos at,int chair,int floor,int hush) implements CustomPacketPayload {
    public static final Type<HomeEchoPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"home_echo"));
    public static final StreamCodec<RegistryFriendlyByteBuf,HomeEchoPayload> STREAM_CODEC=StreamCodec.composite(BlockPos.STREAM_CODEC,HomeEchoPayload::at,ByteBufCodecs.VAR_INT,HomeEchoPayload::chair,ByteBufCodecs.VAR_INT,HomeEchoPayload::floor,ByteBufCodecs.VAR_INT,HomeEchoPayload::hush,HomeEchoPayload::new);
    @Override public Type<HomeEchoPayload> type(){return TYPE;}
}
