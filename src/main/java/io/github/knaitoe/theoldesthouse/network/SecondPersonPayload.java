package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** 0.4.274, Second Person: the hour in the reader's own flat (a day time, or -1 to clear), from their record (labyrinth/SecondPerson). */
public record SecondPersonPayload(long time) implements CustomPacketPayload {
    public static final Type<SecondPersonPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"second_person"));
    public static final StreamCodec<FriendlyByteBuf,SecondPersonPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_LONG,SecondPersonPayload::time,SecondPersonPayload::new);
    @Override public Type<SecondPersonPayload> type(){return TYPE;}
}
