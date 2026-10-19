package io.github.knaitoe.theoldesthouse.network;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
/** Owner-private state bounded by the actual source's site and native world. No client progress authority. */
public record BorrascaViewPayload(BlockPos source,int visit,boolean active,boolean running,int takingTicks,int clock,BlockPos sheriff,float flashlightYaw) implements CustomPacketPayload {
    public BorrascaViewPayload(BlockPos source,int visit,boolean active,boolean running){this(source,visit,active,running,0,0,source.subtract(io.github.knaitoe.theoldesthouse.labyrinth.BorrascaRooms.MACHINE).offset(io.github.knaitoe.theoldesthouse.labyrinth.BorrascaRooms.SHERIFF),180);}
    public static final Type<BorrascaViewPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"borrasca_view"));
    public static final StreamCodec<ByteBuf,BorrascaViewPayload> STREAM_CODEC=new StreamCodec<>(){
        public BorrascaViewPayload decode(ByteBuf b){return new BorrascaViewPayload(BlockPos.STREAM_CODEC.decode(b),ByteBufCodecs.VAR_INT.decode(b),b.readBoolean(),b.readBoolean(),ByteBufCodecs.VAR_INT.decode(b),ByteBufCodecs.VAR_INT.decode(b),BlockPos.STREAM_CODEC.decode(b),b.readFloat());}
        public void encode(ByteBuf b,BorrascaViewPayload p){BlockPos.STREAM_CODEC.encode(b,p.source);ByteBufCodecs.VAR_INT.encode(b,p.visit);b.writeBoolean(p.active);b.writeBoolean(p.running);ByteBufCodecs.VAR_INT.encode(b,p.takingTicks);ByteBufCodecs.VAR_INT.encode(b,p.clock);BlockPos.STREAM_CODEC.encode(b,p.sheriff);b.writeFloat(p.flashlightYaw);}
    };
    @Override public Type<BorrascaViewPayload> type(){return TYPE;}
}
