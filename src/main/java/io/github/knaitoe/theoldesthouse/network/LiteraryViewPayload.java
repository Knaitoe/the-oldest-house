package io.github.knaitoe.theoldesthouse.network;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
/** Bounded camera/overlay lease and the current reader's small native model samples. */
public record LiteraryViewPayload(int actor,int ticks,int drowse,CompoundTag models) implements CustomPacketPayload {
    public static final Type<LiteraryViewPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"literary_view"));
    public static final StreamCodec<ByteBuf,LiteraryViewPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,LiteraryViewPayload::actor,ByteBufCodecs.VAR_INT,LiteraryViewPayload::ticks,ByteBufCodecs.VAR_INT,LiteraryViewPayload::drowse,ByteBufCodecs.COMPOUND_TAG,LiteraryViewPayload::models,LiteraryViewPayload::new);
    @Override public Type<LiteraryViewPayload> type(){return TYPE;}
}
