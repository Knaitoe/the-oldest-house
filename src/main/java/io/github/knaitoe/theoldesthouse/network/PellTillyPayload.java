package io.github.knaitoe.theoldesthouse.network;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
/**
 * 0.4.545: a reader playing Tilly at the Pell house (labyrinth.PellTilly), and what her body is doing: the pose of the house's own Tilly
 * (PellPeople's gestures), or below zero for no longer. Sent to everyone at the house, the reader among them, twice a second while they
 * play her: each draws that reader as Tilly at a child's size, cane and all, and their own name and skin are not drawn. The look lapses
 * when it stops coming. 0.4.568: and how long her hair has grown (labyrinth.PellPeople.hairFor; protocol 190).
 */
public record PellTillyPayload(UUID player,int gesture,int hair) implements CustomPacketPayload {
    public static final Type<PellTillyPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"pell_tilly"));
    public static final StreamCodec<ByteBuf,PellTillyPayload> STREAM_CODEC=StreamCodec.composite(UUIDUtil.STREAM_CODEC,PellTillyPayload::player,ByteBufCodecs.VAR_INT,PellTillyPayload::gesture,ByteBufCodecs.VAR_INT,PellTillyPayload::hair,PellTillyPayload::new);
    @Override public Type<PellTillyPayload> type(){return TYPE;}
}
