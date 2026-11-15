package io.github.knaitoe.theoldesthouse.network;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
/** A private, short-lived lease for the reader's delayed mirror or local listing tilt. */
public record HazardViewPayload(int mode,BlockPos base,CompoundTag view) implements CustomPacketPayload {
    public static final Type<HazardViewPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"hazard_view"));
    public static final StreamCodec<ByteBuf,HazardViewPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,HazardViewPayload::mode,BlockPos.STREAM_CODEC,HazardViewPayload::base,ByteBufCodecs.COMPOUND_TAG,HazardViewPayload::view,HazardViewPayload::new);
    public static HazardViewPayload clear(){return new HazardViewPayload(0,BlockPos.ZERO,new CompoundTag());}
    public static HazardViewPayload list(BlockPos b,float tilt,float push){var t=new CompoundTag();t.putFloat("Tilt",tilt);t.putFloat("Push",push);return new HazardViewPayload(2,b,t);}
    /**
     * 0.4.592: {@code delay} is how many ticks behind its reader the hall's glass shows them, and {@code answer} whether it shows them
     * standing at {@code door} instead, in front of the door that is really theirs. The glass is a true mirror on the reader's own
     * client, which keeps what its reader did: nothing of where they were is sent.
     */
    public static HazardViewPayload mirror(BlockPos b,int delay,boolean answer,net.minecraft.world.phys.Vec3 door){var t=new CompoundTag();t.putInt("Delay",delay);t.putBoolean("Answer",answer);t.putDouble("X",door.x);t.putDouble("Y",door.y);t.putDouble("Z",door.z);return new HazardViewPayload(1,b,t);}
    /** 0.4.599, the rising stair: its reader is on the stair, and in the shaft's water their own client lets them rise no faster than {@code pull} falling (HazardClient). */
    public static HazardViewPayload undertow(BlockPos b,float pull){var t=new CompoundTag();t.putFloat("Pull",pull);return new HazardViewPayload(3,b,t);}
    @Override public Type<HazardViewPayload> type(){return TYPE;}
}
