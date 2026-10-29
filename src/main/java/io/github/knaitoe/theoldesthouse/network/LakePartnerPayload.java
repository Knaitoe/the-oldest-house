package io.github.knaitoe.theoldesthouse.network;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
/**
 * 0.4.156: a lake house partner and their diver, sent to both every second they are in the lake together. The partner is
 * drawn as Tess (or Theo, {@code role}) on both clients, and sees the diver's own figure; the look lapses when it stops coming.
 */
public record LakePartnerPayload(UUID partner,UUID diver,int role) implements CustomPacketPayload {
    public static final Type<LakePartnerPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"lake_partner"));
    public static final StreamCodec<ByteBuf,LakePartnerPayload> STREAM_CODEC=StreamCodec.composite(UUIDUtil.STREAM_CODEC,LakePartnerPayload::partner,UUIDUtil.STREAM_CODEC,LakePartnerPayload::diver,ByteBufCodecs.VAR_INT,LakePartnerPayload::role,LakePartnerPayload::new);
    @Override public Type<LakePartnerPayload> type(){return TYPE;}
}
