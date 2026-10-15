package io.github.knaitoe.theoldesthouse.network;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
/** Owner-private state bounded by the actual source's site and native world. No client progress authority. */
public record BorrascaViewPayload(BlockPos source,int visit,boolean active,boolean running) implements CustomPacketPayload {
    public static final Type<BorrascaViewPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"borrasca_view"));
    public static final StreamCodec<ByteBuf,BorrascaViewPayload> STREAM_CODEC=StreamCodec.composite(BlockPos.STREAM_CODEC,BorrascaViewPayload::source,ByteBufCodecs.VAR_INT,BorrascaViewPayload::visit,ByteBufCodecs.BOOL,BorrascaViewPayload::active,ByteBufCodecs.BOOL,BorrascaViewPayload::running,BorrascaViewPayload::new);
    @Override public Type<BorrascaViewPayload> type(){return TYPE;}
}
