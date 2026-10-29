package io.github.knaitoe.theoldesthouse.network;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
/**
 * 0.4.159: the House's version of one reader, sent only to that reader's own client: the coat they chose (empty until their
 * presentation is settled), reading glasses, a silk wrap, and whether the face is obscured. Mirrors and doubles dress the
 * reader's own skin with it; the live model never wears it, and no other client is ever sent it.
 */
public record HouseReadLookPayload(String coat,boolean glasses,boolean wrap,boolean unseen) implements CustomPacketPayload {
    public static final Type<HouseReadLookPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"house_read_look"));
    public static final StreamCodec<ByteBuf,HouseReadLookPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.STRING_UTF8,HouseReadLookPayload::coat,ByteBufCodecs.BOOL,HouseReadLookPayload::glasses,ByteBufCodecs.BOOL,HouseReadLookPayload::wrap,ByteBufCodecs.BOOL,HouseReadLookPayload::unseen,HouseReadLookPayload::new);
    @Override public Type<HouseReadLookPayload> type(){return TYPE;}
}
