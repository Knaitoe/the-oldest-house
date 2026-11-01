package io.github.knaitoe.theoldesthouse.network;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
/** 0.4.221, Block Party: which keys a player at the four-player cabinet is holding (see PolybiusBrawl's key bits). */
public record PolybiusBrawlKeysPayload(int keys) implements CustomPacketPayload {
    public static final Type<PolybiusBrawlKeysPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"polybius_brawl_keys"));
    public static final StreamCodec<ByteBuf,PolybiusBrawlKeysPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,PolybiusBrawlKeysPayload::keys,PolybiusBrawlKeysPayload::new);
    @Override public Type<PolybiusBrawlKeysPayload> type(){return TYPE;}
}
