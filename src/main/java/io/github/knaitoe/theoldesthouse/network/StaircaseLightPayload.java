package io.github.knaitoe.theoldesthouse.network;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
public record StaircaseLightPayload(boolean active,int fires,float sight) implements CustomPacketPayload {
    public static final Type<StaircaseLightPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"staircase_light"));
    public static final StreamCodec<ByteBuf,StaircaseLightPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.BOOL,StaircaseLightPayload::active,ByteBufCodecs.VAR_INT,StaircaseLightPayload::fires,ByteBufCodecs.FLOAT,StaircaseLightPayload::sight,StaircaseLightPayload::new);
    @Override public Type<StaircaseLightPayload> type(){return TYPE;}
}
