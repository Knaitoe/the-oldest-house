package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * The demand is a subtitle at the door, never an instruction or a suspect label: positive lines are the thing at the
 * door, negative ones the cousin who went for gas (0.4.53). The flags say whether the woods have gone quiet.
 */
public record GoatmanScenePayload(int phase,int demand,int remaining,int flags,int cousinLine,int cousinRemaining) implements CustomPacketPayload {
    public GoatmanScenePayload(int phase,int demand,int remaining,int flags){this(phase,demand,remaining,flags,0,0);}
    public static final int QUIET=1;
    public static final Type<GoatmanScenePayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"trailer_scene"));
    public static final StreamCodec<ByteBuf,GoatmanScenePayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,GoatmanScenePayload::phase,ByteBufCodecs.VAR_INT,GoatmanScenePayload::demand,ByteBufCodecs.VAR_INT,GoatmanScenePayload::remaining,ByteBufCodecs.VAR_INT,GoatmanScenePayload::flags,ByteBufCodecs.VAR_INT,GoatmanScenePayload::cousinLine,ByteBufCodecs.VAR_INT,GoatmanScenePayload::cousinRemaining,GoatmanScenePayload::new);
    @Override public Type<GoatmanScenePayload> type(){return TYPE;}
}
