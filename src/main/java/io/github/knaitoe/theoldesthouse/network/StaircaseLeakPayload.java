package io.github.knaitoe.theoldesthouse.network;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
public record StaircaseLeakPayload(boolean active,int kind,int returnGlow) implements CustomPacketPayload {
    public static final Type<StaircaseLeakPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"staircase_leak"));
    public static final StreamCodec<ByteBuf,StaircaseLeakPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.BOOL,StaircaseLeakPayload::active,ByteBufCodecs.VAR_INT,StaircaseLeakPayload::kind,ByteBufCodecs.VAR_INT,StaircaseLeakPayload::returnGlow,StaircaseLeakPayload::new);
    @Override public Type<StaircaseLeakPayload> type(){return TYPE;}
}
