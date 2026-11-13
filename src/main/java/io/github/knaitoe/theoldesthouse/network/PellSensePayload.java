package io.github.knaitoe.theoldesthouse.network;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
/**
 * 0.4.546, Tilly's senses at the Pell house (labyrinth.PellSenses, docs/PELL_HOUSE_SENSES_0_4_546.md), sent to the reader playing Tilly
 * and nobody else:
 * <ul>
 * <li>{@link #OUTLINE}: what the cane's tap found in reach, as lines in the world, six numbers to each (from x, y, z; to x, y, z), drawn
 * through the haze for a moment (client.PellTillySight).</li>
 * <li>{@link #COLD}: the line underfoot, crossed: a cold at the edges of the screen.</li>
 * <li>{@link #HUSH}: a line of June's, said on the telephone where she thinks nobody can hear it; where she is, the words, and how many
 * ticks they stay up (the one number in its lines).</li>
 * </ul>
 */
public record PellSensePayload(int kind,List<Float> lines,String words,double x,double y,double z) implements CustomPacketPayload {
    public static final int OUTLINE=0,COLD=1,HUSH=2;
    public static final Type<PellSensePayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"pell_sense"));
    public static final StreamCodec<ByteBuf,PellSensePayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,PellSensePayload::kind,
        ByteBufCodecs.FLOAT.apply(ByteBufCodecs.list()),PellSensePayload::lines,ByteBufCodecs.STRING_UTF8,PellSensePayload::words,
        ByteBufCodecs.DOUBLE,PellSensePayload::x,ByteBufCodecs.DOUBLE,PellSensePayload::y,ByteBufCodecs.DOUBLE,PellSensePayload::z,PellSensePayload::new);
    public static PellSensePayload outline(List<Float> lines){return new PellSensePayload(OUTLINE,lines,"",0,0,0);}
    public static PellSensePayload cold(){return new PellSensePayload(COLD,List.of(),"",0,0,0);}
    public static PellSensePayload hush(String words,double x,double y,double z,int ticks){return new PellSensePayload(HUSH,List.of((float)ticks),words,x,y,z);}
    @Override public Type<PellSensePayload> type(){return TYPE;}
}
