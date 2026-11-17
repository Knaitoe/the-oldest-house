package io.github.knaitoe.theoldesthouse.network;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
/** 0.4.664: how many seams the machine in Not Yet has left on a reader, so every viewer draws them while that reader is in the place. */
public record NotYetSeamsPayload(UUID player,int seams) implements CustomPacketPayload {
    public static final Type<NotYetSeamsPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"not_yet_seams"));
    public static final StreamCodec<ByteBuf,NotYetSeamsPayload> STREAM_CODEC=StreamCodec.composite(UUIDUtil.STREAM_CODEC,NotYetSeamsPayload::player,ByteBufCodecs.VAR_INT,NotYetSeamsPayload::seams,NotYetSeamsPayload::new);
    @Override public Type<NotYetSeamsPayload> type(){return TYPE;}
}
