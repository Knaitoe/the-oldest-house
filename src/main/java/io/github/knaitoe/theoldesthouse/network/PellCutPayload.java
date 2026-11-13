package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 0.4.534, the Pell house's knife (labyrinth/PellKnife): a sharp cut to black on this reader's screen, held so many ticks, then static
 * over their view that clears over so many more.
 */
public record PellCutPayload(int black,int clear) implements CustomPacketPayload {
    public static final Type<PellCutPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"pell_cut"));
    public static final StreamCodec<FriendlyByteBuf,PellCutPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,PellCutPayload::black,ByteBufCodecs.VAR_INT,PellCutPayload::clear,PellCutPayload::new);
    @Override public Type<PellCutPayload> type(){return TYPE;}
}
