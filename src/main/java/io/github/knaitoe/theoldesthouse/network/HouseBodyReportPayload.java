package io.github.knaitoe.theoldesthouse.network;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
/** 0.4.163: a reader's own client says only whether the face of their own skin looks human. Nothing else of the skin is sent. */
public record HouseBodyReportPayload(boolean human) implements CustomPacketPayload {
    public static final Type<HouseBodyReportPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"house_body_report"));
    public static final StreamCodec<ByteBuf,HouseBodyReportPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.BOOL,HouseBodyReportPayload::human,HouseBodyReportPayload::new);
    @Override public Type<HouseBodyReportPayload> type(){return TYPE;}
}
