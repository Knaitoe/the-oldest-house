package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** 0.4.751, the waxworks: how far each figure's head has turned for this reader alone (WaxNight.look), by entity id, in degrees. */
public record WaxLookPayload(int[] ids,float[] turns) implements CustomPacketPayload {
    public static final Type<WaxLookPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"wax_look"));
    public static final StreamCodec<ByteBuf,WaxLookPayload> STREAM_CODEC=StreamCodec.of(
        (buf,p)->{var b=new FriendlyByteBuf(buf);int n=Math.min(p.ids().length,64);b.writeVarInt(n);for(int i=0;i<n;i++){b.writeVarInt(p.ids()[i]);b.writeFloat(p.turns()[i]);}},
        buf->{var b=new FriendlyByteBuf(buf);int n=Math.min(64,b.readVarInt());int[] ids=new int[n];float[] t=new float[n];for(int i=0;i<n;i++){ids[i]=b.readVarInt();t[i]=b.readFloat();}return new WaxLookPayload(ids,t);});
    @Override public Type<WaxLookPayload> type(){return TYPE;}
}
