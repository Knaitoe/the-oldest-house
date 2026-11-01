package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 0.4.203, Work print: one reader's private viewing begins or ends. {@code kind} is 0 to stop, 1 the tape, 2 a dub (the
 * tape's own film), 3 the finished work print on the flatbed, 4 the water-stained tape. The film is composed on the reader's
 * client from {@code start} (game time) and their {@code number}th viewing, on the screen at {@code screen} facing
 * {@code facing} (a horizontal 2D value); {@code footage} is what the House has filmed of them (64 by 48, one byte a pixel),
 * {@code scene} the last scene as the camera saw it, {@code credits} the names in the credits.
 */
public record WorkPrintFilmPayload(int kind,long start,int number,BlockPos screen,int facing,List<byte[]> footage,byte[] scene,String credits) implements CustomPacketPayload {
    public static final Type<WorkPrintFilmPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"work_print_film"));
    public static final StreamCodec<FriendlyByteBuf,WorkPrintFilmPayload> STREAM_CODEC=StreamCodec.ofMember(WorkPrintFilmPayload::write,WorkPrintFilmPayload::read);
    public static WorkPrintFilmPayload stop(){return new WorkPrintFilmPayload(0,0,0,BlockPos.ZERO,0,List.of(),new byte[0],"");}
    private void write(FriendlyByteBuf b){
        b.writeVarInt(kind);b.writeLong(start);b.writeVarInt(number);b.writeBlockPos(screen);b.writeVarInt(facing);
        b.writeVarInt(footage.size());for(var f:footage)b.writeByteArray(f);b.writeByteArray(scene);b.writeUtf(credits,512);
    }
    private static WorkPrintFilmPayload read(FriendlyByteBuf b){
        int kind=b.readVarInt();long start=b.readLong();int number=b.readVarInt();var screen=b.readBlockPos();int facing=b.readVarInt();
        int n=Math.min(8,b.readVarInt());var footage=new ArrayList<byte[]>();for(int i=0;i<n;i++)footage.add(b.readByteArray(8192));
        return new WorkPrintFilmPayload(kind,start,number,screen,facing,List.copyOf(footage),b.readByteArray(8192),b.readUtf(512));
    }
    @Override public Type<WorkPrintFilmPayload> type(){return TYPE;}
}
