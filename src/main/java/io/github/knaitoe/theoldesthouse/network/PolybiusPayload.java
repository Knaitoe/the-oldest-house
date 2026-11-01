package io.github.knaitoe.theoldesthouse.network;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
/**
 * 0.4.217, Polybius: a cabinet's screen opened for one reader (the game, the session's token, the reader's name, how many times they
 * have played the black cabinet, whether this play is the one it keeps, and the game's high-score table), or closed (game -1).
 */
public record PolybiusPayload(int game,long token,String name,int plays,boolean taken,CompoundTag table) implements CustomPacketPayload {
    public static final Type<PolybiusPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"polybius"));
    public static final StreamCodec<ByteBuf,PolybiusPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,PolybiusPayload::game,ByteBufCodecs.VAR_LONG,PolybiusPayload::token,ByteBufCodecs.stringUtf8(32),PolybiusPayload::name,ByteBufCodecs.VAR_INT,PolybiusPayload::plays,ByteBufCodecs.BOOL,PolybiusPayload::taken,ByteBufCodecs.COMPOUND_TAG,PolybiusPayload::table,PolybiusPayload::new);
    public static PolybiusPayload close(){return new PolybiusPayload(-1,0,"",0,false,new CompoundTag());}
    @Override public Type<PolybiusPayload> type(){return TYPE;}
}
