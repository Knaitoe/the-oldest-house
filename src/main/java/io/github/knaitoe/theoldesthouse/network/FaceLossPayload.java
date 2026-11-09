package io.github.knaitoe.theoldesthouse.network;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
/** 0.4.410: whether the machine in the Kept took a player's mouth, so every viewer draws them as they are. */
public record FaceLossPayload(UUID player,boolean gone) implements CustomPacketPayload {
    public static final Type<FaceLossPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"face_loss"));
    public static final StreamCodec<ByteBuf,FaceLossPayload> STREAM_CODEC=StreamCodec.composite(UUIDUtil.STREAM_CODEC,FaceLossPayload::player,ByteBufCodecs.BOOL,FaceLossPayload::gone,FaceLossPayload::new);
    @Override public Type<FaceLossPayload> type(){return TYPE;}
}
