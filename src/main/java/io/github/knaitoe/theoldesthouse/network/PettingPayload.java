package io.github.knaitoe.theoldesthouse.network;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
public record PettingPayload(int playerId,int petId,int ticks) implements CustomPacketPayload {
    public static final Type<PettingPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"petting"));
    public static final StreamCodec<ByteBuf,PettingPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,PettingPayload::playerId,ByteBufCodecs.VAR_INT,PettingPayload::petId,ByteBufCodecs.VAR_INT,PettingPayload::ticks,PettingPayload::new);
    @Override public Type<PettingPayload> type(){return TYPE;}
}
