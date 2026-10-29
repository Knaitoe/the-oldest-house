package io.github.knaitoe.theoldesthouse.network;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
/**
 * 0.4.166: one of the House's lines between a quiet room and the rest of the House, shown on black to that reader alone.
 * Pages are separated by a form feed; {@code sound} (a sound id, or empty) is heard as the room cuts off; {@code title} draws
 * the last page large, as the House's own warning.
 */
public record HouseReadLinePayload(String text,String sound,boolean title) implements CustomPacketPayload {
    public static final Type<HouseReadLinePayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"house_read_line"));
    public static final StreamCodec<ByteBuf,HouseReadLinePayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.STRING_UTF8,HouseReadLinePayload::text,ByteBufCodecs.STRING_UTF8,HouseReadLinePayload::sound,ByteBufCodecs.BOOL,HouseReadLinePayload::title,HouseReadLinePayload::new);
    @Override public Type<HouseReadLinePayload> type(){return TYPE;}
}
