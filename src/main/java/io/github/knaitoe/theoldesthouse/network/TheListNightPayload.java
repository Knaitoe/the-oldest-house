package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 0.4.706, The list, part 3a (docs/THE_LIST_0_4_706.md): a town's night as a reader in it needs it. {@code time} is the night's hour in
 * the game's own day time (sixteen thousand is ten at night), {@code rate} how much of it passes in a tick while the clock runs (0 while
 * it holds), and {@code shake} the ticks the ground still shakes under the reader (the auger), 0 when it does not; {@code time} below 0
 * means there is no night yet. Sent to everyone in the town, spectators too, whenever it changes and once a second.
 */
public record TheListNightPayload(long time,float rate,int shake) implements CustomPacketPayload {
    public static final Type<TheListNightPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"list_night"));
    public static final StreamCodec<ByteBuf,TheListNightPayload> STREAM_CODEC=new StreamCodec<>(){
        public TheListNightPayload decode(ByteBuf b){return new TheListNightPayload(ByteBufCodecs.VAR_LONG.decode(b),ByteBufCodecs.FLOAT.decode(b),ByteBufCodecs.VAR_INT.decode(b));}
        public void encode(ByteBuf b,TheListNightPayload p){ByteBufCodecs.VAR_LONG.encode(b,p.time());ByteBufCodecs.FLOAT.encode(b,p.rate());ByteBufCodecs.VAR_INT.encode(b,p.shake());}
    };
    @Override public Type<TheListNightPayload> type(){return TYPE;}
}
