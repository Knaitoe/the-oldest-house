package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

/**
 * 0.4.620, The Ones Already Inside, its twentieth and last slice (docs/THE_ONES_ALREADY_INSIDE_PLAN.md, "Aftermath"). Once, eight halls
 * or more after reading their account, the lodging house comes back to a reader who finished it (Aftermath): the house they were in,
 * as it ended, since an ended house keeps its chapter and takes nobody new. Nothing to do there, nothing to earn, and nobody in it asks
 * them anything. A reader of that party who left before the end and never read an account comes back to the same house, and their
 * account is waiting on the bench.
 */
public final class LodgingAfter {
    private LodgingAfter(){}
    public static final String SEEN="AfterSeen0620";

    /** A finished reader's arrival in the house they ended in: a line by the end it came to, and the return counted once a visit. */
    static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        if(!Aftermath.finished(own))return;
        int end=LodgingEnd.ended(p.server,b);if(end==0)return;
        if(Aftermath.count(own)){own.putBoolean(SEEN,true);LodgingHouse.cue(p,line(end));}
    }
    /** What a finished reader finds, by the end the house came to. Every line here is new. */
    public static String line(int end){return switch(end){
        case LodgingEnd.RETURNED->"The frame upstairs is empty and its straps are hanging. Somebody has left a dog's lead on the hall stand.";
        case LodgingEnd.COMPLETED->"The house is quiet. Upstairs one room is shallower than the rest, and nobody goes up there.";
        case LodgingEnd.SEVERED->"The bench in the hall is empty. The front door has been left on the latch.";
        case LodgingEnd.NEITHER->"The places are still laid at the dining table, and nobody has set the chair back up.";
        default->"";};}
    /** Whether this reader has finished the house and finds only its aftermath: the people then have nothing to ask of them. */
    static boolean quiet(CompoundTag own){return Aftermath.finished(own);}
    public static final String NOTHING="Nobody here is waiting for you to say anything.";
}
