package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.594: a light has been let fall into the great staircase's well. The server has followed the whole fall; a client steps the
 * same motion from {@code start} and {@code speed} for {@code ticks} ticks and sets it down at {@code end}. {@code hit}: it came
 * down on something and burns there a while; otherwise it was lost from view long before. {@code tint} is its flame's.
 */
public record StairLightFallPayload(Vec3 start,Vec3 speed,int ticks,Vec3 end,boolean hit,int tint) implements CustomPacketPayload {
    public static final Type<StairLightFallPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"stair_light_fall"));
    public static final StreamCodec<FriendlyByteBuf,StairLightFallPayload> STREAM_CODEC=StreamCodec.ofMember(StairLightFallPayload::write,StairLightFallPayload::read);
    private void write(FriendlyByteBuf b){
        b.writeVec3(start);b.writeVec3(speed);b.writeVarInt(ticks);b.writeVec3(end);b.writeBoolean(hit);b.writeInt(tint);
    }
    private static StairLightFallPayload read(FriendlyByteBuf b){
        return new StairLightFallPayload(b.readVec3(),b.readVec3(),b.readVarInt(),b.readVec3(),b.readBoolean(),b.readInt());
    }
    @Override public Type<StairLightFallPayload> type(){return TYPE;}
}
