package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * 0.4.618, The Ones Already Inside, its eighteenth slice (docs/THE_ONES_ALREADY_INSIDE_PLAN.md, "The accounts and the Witness: each
 * reader's account from their own record; the source dealt; the co-op lines; a second house for a seventh reader").
 *
 * <p>The house has ended once one of the four ends has played out to its last image: Daniel returned and the dog named (E1), Dan
 * completed and his hand over his mouth (E2), both on the bench (E3), or Rae at the dining table (E4). Every reader who had come into
 * the house before then is given their account on the bench, each written from their own record; one who left before it and comes
 * back finds it there. Reading it, having read the terms, resolves the story once. A house that has ended takes no new readers: the
 * next reader is given another house.
 */
public final class LodgingEnd {
    private LodgingEnd(){}
    public static final String ENDED="Ended0618",PARTY="EndedParty0618";
    public static final int RETURNED=1,COMPLETED=2,SEVERED=3,NEITHER=4,EVERY=20;
    static final String[] OUTCOMES={"","returned_daniel","completed_dan","severed_the_house","neither_daniel"};

    /** Which end the house came to, or 0 while none has played out. */
    public static int ended(MinecraftServer s,BlockPos b){return LodgingStory.house(s,b).getInt(ENDED);}
    /** The end the house has reached by now, from each end's own state. */
    private static int reached(MinecraftServer s,BlockPos b){
        if(LodgingReturn.phase(s,b)==LodgingReturn.DONE)return RETURNED;
        if(LodgingComplete.phase(s,b)==LodgingComplete.DONE)return COMPLETED;
        if(LodgingSever.phase(s,b)==LodgingSever.DONE)return SEVERED;
        if(LodgingNeither.phase(s,b)>=LodgingNeither.RAE)return NEITHER;
        return 0;
    }
    static void tick(ServerLevel l,BlockPos b,List<ServerPlayer> here){
        if(l.getGameTime()%EVERY!=0)return;
        var s=l.getServer();var h=LodgingStory.house(s,b);
        if(h.getInt(ENDED)==0){
            int end=reached(s,b);if(end==0)return;
            h.putInt(ENDED,end);h.put(PARTY,h.getList(LodgingParty.READERS,Tag.TAG_STRING).copy());LodgingStory.save(s,b,h);
        }
        for(var p:here)ready(p,b,h);
    }
    /** A reader of the ended house's party, in it: their account on the bench. */
    private static void ready(ServerPlayer p,BlockPos b,CompoundTag h){
        if(!inParty(h,p))return;
        var own=LodgingHouse.own(p);if(own.getBoolean("Ready")||own.getBoolean("Completed"))return;
        LiteraryVignettes.ready(p,LodgingHouse.PLACE,own,OUTCOMES[h.getInt(ENDED)]);LodgingHouse.save(p,own);
    }
    private static boolean inParty(CompoundTag h,ServerPlayer p){
        var list=h.getList(PARTY,Tag.TAG_STRING);var id=p.getUUID().toString();
        for(int i=0;i<list.size();i++)if(list.getString(i).equals(id))return true;
        return false;
    }
    /** 0.4.620: whether a reader was of copy {@code k}'s party when it ended: they may come back to it (LodgingAfter). */
    public static boolean ofParty(MinecraftServer s,int k,java.util.UUID reader){
        var b=SceneInstances.base(s,LodgingHouse.PLACE,k);if(b==null)return false;var list=LodgingStory.house(s,b).getList(PARTY,Tag.TAG_STRING);
        for(int i=0;i<list.size();i++)if(list.getString(i).equals(reader.toString()))return true;
        return false;
    }
    /** Copy {@code k} has ended and takes nobody new (SceneInstances). */
    public static boolean closed(MinecraftServer s,int k){var b=SceneInstances.base(s,LodgingHouse.PLACE,k);return b!=null&&ended(s,b)!=0;}
}
