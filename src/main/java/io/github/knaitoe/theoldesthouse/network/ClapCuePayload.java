package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** A personal pulse at an actual audible clap, rather than a continuously visible target. */
public record ClapCuePayload(BlockPos source,boolean wardrobe) implements CustomPacketPayload {
    public static final Type<ClapCuePayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"clap_cue"));
    public static final StreamCodec<FriendlyByteBuf,ClapCuePayload> STREAM_CODEC=StreamCodec.composite(
            BlockPos.STREAM_CODEC,ClapCuePayload::source,ByteBufCodecs.BOOL,ClapCuePayload::wardrobe,ClapCuePayload::new);
    @Override public Type<ClapCuePayload> type(){return TYPE;}
}
