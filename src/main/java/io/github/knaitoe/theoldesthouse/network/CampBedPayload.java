package io.github.knaitoe.theoldesthouse.network;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
public record CampBedPayload(boolean active) implements CustomPacketPayload {
    public static final Type<CampBedPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"camp_bed"));
    public static final StreamCodec<ByteBuf,CampBedPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.BOOL,CampBedPayload::active,CampBedPayload::new);
    @Override public Type<CampBedPayload> type(){return TYPE;}
}
