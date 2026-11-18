package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 0.4.710, The list, part 4a-3 (docs/THE_LIST_0_4_710.md): how much blood is down the front and the right sleeve of a reader wearing
 * the slasher's body, 0 to 3, so that every viewer draws it. Sent to everyone when it changes, now and then while it lasts, and as 0 when
 * the body comes off.
 */
public record TheListMarksPayload(UUID player,int blood) implements CustomPacketPayload {
    public static final Type<TheListMarksPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"list_marks"));
    public static final StreamCodec<ByteBuf,TheListMarksPayload> STREAM_CODEC=StreamCodec.composite(UUIDUtil.STREAM_CODEC,TheListMarksPayload::player,ByteBufCodecs.VAR_INT,TheListMarksPayload::blood,TheListMarksPayload::new);
    @Override public Type<TheListMarksPayload> type(){return TYPE;}
}
