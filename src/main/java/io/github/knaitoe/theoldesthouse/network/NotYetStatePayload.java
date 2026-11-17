package io.github.knaitoe.theoldesthouse.network;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
/**
 * 0.4.664: what one reader's game needs of the machine's ward: whether the machine is holding them (on the table or their cot, so the bed
 * screen offers no way up), the game time it began counting them (or -1), the kind of counter they see, and the counter's first window.
 */
public record NotYetStatePayload(boolean held,long countFrom,int kind,BlockPos counter) implements CustomPacketPayload {
    public static final Type<NotYetStatePayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"not_yet_state"));
    public static final StreamCodec<ByteBuf,NotYetStatePayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.BOOL,NotYetStatePayload::held,ByteBufCodecs.VAR_LONG,NotYetStatePayload::countFrom,ByteBufCodecs.VAR_INT,NotYetStatePayload::kind,BlockPos.STREAM_CODEC,NotYetStatePayload::counter,NotYetStatePayload::new);
    @Override public Type<NotYetStatePayload> type(){return TYPE;}
}
