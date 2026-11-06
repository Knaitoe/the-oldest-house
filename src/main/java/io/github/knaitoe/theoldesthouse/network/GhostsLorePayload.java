package io.github.knaitoe.theoldesthouse.network;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
/** Read-only family papers or a saved laptop page, opened by a nearby server-checked interaction. */
public record GhostsLorePayload(int kind,int day) implements CustomPacketPayload {
    public static final Type<GhostsLorePayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"ghosts_lore"));
    public static final StreamCodec<ByteBuf,GhostsLorePayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,GhostsLorePayload::kind,ByteBufCodecs.VAR_INT,GhostsLorePayload::day,GhostsLorePayload::new);
    @Override public Type<GhostsLorePayload> type(){return TYPE;}
}
