package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** What the reader did on the phone: 0 texted a name, 1 called, 2 left a name on his voicemail. The server decides the rest. */
public record HarriganPhoneActionPayload(int action,String text) implements CustomPacketPayload {
    public static final int TEXT=0,CALL=1,VOICEMAIL=2;
    public static final Type<HarriganPhoneActionPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"harrigan_phone_action"));
    public static final StreamCodec<ByteBuf,HarriganPhoneActionPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,HarriganPhoneActionPayload::action,ByteBufCodecs.stringUtf8(64),HarriganPhoneActionPayload::text,HarriganPhoneActionPayload::new);
    @Override public Type<HarriganPhoneActionPayload> type(){return TYPE;}
}
