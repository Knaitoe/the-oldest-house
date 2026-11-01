package io.github.knaitoe.theoldesthouse.network;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
/** 0.4.221, Block Party: the four-player game as the server sees it, sent to each seat every step; seat -1 closes the screen. */
public record PolybiusBrawlPayload(int seat,CompoundTag state) implements CustomPacketPayload {
    public static final Type<PolybiusBrawlPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"polybius_brawl"));
    public static final StreamCodec<ByteBuf,PolybiusBrawlPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,PolybiusBrawlPayload::seat,ByteBufCodecs.COMPOUND_TAG,PolybiusBrawlPayload::state,PolybiusBrawlPayload::new);
    public static PolybiusBrawlPayload close(){return new PolybiusBrawlPayload(-1,new CompoundTag());}
    @Override public Type<PolybiusBrawlPayload> type(){return TYPE;}
}
