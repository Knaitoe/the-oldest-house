package io.github.knaitoe.theoldesthouse.network;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
/** 0.4.411: which prizes a player wears (bracelets and rings, PrizeWear's bits), so every viewer draws them. */
public record PrizeWearPayload(UUID player,int worn) implements CustomPacketPayload {
    public static final Type<PrizeWearPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"prize_wear"));
    public static final StreamCodec<ByteBuf,PrizeWearPayload> STREAM_CODEC=StreamCodec.composite(UUIDUtil.STREAM_CODEC,PrizeWearPayload::player,ByteBufCodecs.VAR_INT,PrizeWearPayload::worn,PrizeWearPayload::new);
    @Override public Type<PrizeWearPayload> type(){return TYPE;}
}
