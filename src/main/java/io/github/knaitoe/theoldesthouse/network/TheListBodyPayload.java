package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 0.4.705, The list, part 2 (docs/THE_LIST_0_4_705.md): the slasher's body as its own reader's client needs it. {@code body} while the
 * body is theirs and they are in the town; {@code down} the ticks left lying where they fell, 0 when up; {@code pullYaw} the way, in the
 * world's own yaw, to the nearest name; {@code pull} how hard that name draws them, 0 to 1, after light and looking away have had their
 * say. Sent to that reader alone.
 */
public record TheListBodyPayload(boolean body,int down,float pullYaw,float pull) implements CustomPacketPayload {
    public static final Type<TheListBodyPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"list_body"));
    public static final StreamCodec<ByteBuf,TheListBodyPayload> STREAM_CODEC=new StreamCodec<>(){
        public TheListBodyPayload decode(ByteBuf b){return new TheListBodyPayload(ByteBufCodecs.BOOL.decode(b),ByteBufCodecs.VAR_INT.decode(b),ByteBufCodecs.FLOAT.decode(b),ByteBufCodecs.FLOAT.decode(b));}
        public void encode(ByteBuf b,TheListBodyPayload p){ByteBufCodecs.BOOL.encode(b,p.body());ByteBufCodecs.VAR_INT.encode(b,p.down());ByteBufCodecs.FLOAT.encode(b,p.pullYaw());ByteBufCodecs.FLOAT.encode(b,p.pull());}
    };
    @Override public Type<TheListBodyPayload> type(){return TYPE;}
}
