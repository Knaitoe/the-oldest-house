package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.network.TheListNightPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 0.4.733, The list, part 9b (docs/THE_LIST_0_4_733.md): the town afterwards. A reader who has finished the four nights (their account
 * read) can be dealt Lamesa again ({@link Aftermath}, twice at most), and finds it at noon, with nothing to do and nothing to earn, and
 * one line written from how their nights ended. Only when every reader in the town has finished it; otherwise the town is as it was.
 */
public final class TheListAfter{
    private TheListAfter(){}
    /** Noon on the town's clock, stopped. */
    public static final long NOON=6000;
    public static final String KILLED="Noon. The flyers are still up on the poles, gone pale in the sun. Nobody is out walking.",
        NONE="Noon. Everybody on the list is somewhere in town, doing nothing much. None of them looks at you twice.",
        GONE="Noon. Five houses in town have their curtains open on empty rooms.",
        BUS="Noon. The bench outside the feed store is empty. The schedule on the post says the bus comes once a day, at first light.",
        STOPPED="Noon. Somebody has leaned a shovel against the foot of the water tower and left it there.",
        OTHER="Noon in Lamesa. It looks like any town.";

    /** The line for this reader's ending, from the record only. */
    public static String line(CompoundTag own){
        return switch(own.getString("Outcome")){
            case "crossed_names_off","went_down_the_whole_list"->KILLED;
            case "crossed_no_names_off"->NONE;
            case "everyone_got_away","got_them_out"->GONE;
            case "took_the_bus"->BUS;
            case "was_stopped"->STOPPED;
            default->OTHER;};
    }
    /** A finished reader in through the back door of a town only finished readers are in: noon, the line, and nothing begins. */
    static boolean arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        if(!Aftermath.finished(own)||!Aftermath.alone(TheList.readers(p.serverLevel(),b),TheList.PLACE))return false;
        PacketDistributor.sendToPlayer(p,new TheListNightPayload(NOON,0F,0));
        if(Aftermath.count(own))TheList.cue(p,line(own));
        return true;
    }
}
