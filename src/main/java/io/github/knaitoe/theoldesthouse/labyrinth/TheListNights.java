package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.network.HouseFadePayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * 0.4.721, The list's skeleton to its end (docs/THE_LIST_0_4_721.md): the rest of night two, night three and night four, and the end.
 * Each night is the hunt (TheListHunt) begun on that night and ended by the one who went in lying down on their own bed or by first
 * light. The town keeps who of the five is dead from one night to the next (TheListNight.Night.dead); the dead are not made again.
 * <ol>
 * <li><b>The first name</b>: five seconds after Amber writes the last of her tests' rules down (0.4.722; the first in 0.4.721) she says the first one is out, and night two's hunt
 * begins ({@link #HUNT2}).</li>
 * <li><b>Night two's end</b> ({@link #DONE2}): the one who went in has had two nights.</li>
 * <li><b>Night three</b> ({@link #HUNT3}): back in the town, ten o'clock in their kitchen with the hay hook back; the hunt begins at once.</li>
 * <li><b>Night four</b> ({@link #HUNT4}): the same, and the gin runs. It ends at bed, at first light, or once all five are dead.</li>
 * <li><b>The end</b> ({@link #OVER}): the body is the reader's own again, and every reader in town (the one who went in wherever they are)
 * is ready: the account is on the tape deck in the reader's room.</li>
 * </ol>
 */
public final class TheListNights{
    private TheListNights(){}
    public static final int HUNT2=12,DONE2=13,HUNT3=14,DONE3=15,HUNT4=16,OVER=17;
    /** How long after the first rule is written the first name goes out. */
    public static final int NAME_AFTER=100;
    /** 0.4.729: all five on the list, a bit for each. */
    static final int ALL=java.util.Arrays.stream(TheListBody.LISTED).map(w->1<<w).sum();

    /** 0.4.723: whether a thrown hook comes back in this town rather than straight to the hand: from night three's hunt on. */
    static boolean comesBack(net.minecraft.server.MinecraftServer s,net.minecraft.core.BlockPos b){
        var n=TheListNight.night(s,b);return n!=null&&n.phase>=HUNT3&&n.phase<OVER;
    }
    public static boolean hunting(int phase){return phase==HUNT2||phase==HUNT3||phase==HUNT4;}
    /** Which of the four nights a phase from night two on belongs to. */
    public static int nightOf(int phase){return phase<=DONE2?2:phase<=DONE3?3:4;}

    /** At RAN: once the wait is up, Amber says the first name is out, and night two's hunt begins. True if it did. */
    static boolean ran(ServerLevel l,TheListNight.Night n){
        // 0.4.722: after the last of Amber's tests.
        if(n.since<NAME_AFTER||!TheListDen.tested(l,n))return false;
        var amber=TheListPeople.find(l,n.base,TheListPeople.AMBER);
        if(amber!=null){amber.say(TheListTexts.NAMES[TheListPeople.AMBER],TheListTexts.FIRST_NAME);amber.speaks(TheListTexts.FIRST_NAME);}
        for(var q:TheList.readers(l,n.base))if(LiteraryVignettes.participant(q))TheList.cue(q,q.getUUID().equals(n.slasher)?TheListTexts.FIRST_NAME_CUE:TheListTexts.HUNT_OTHER);
        hunt(l,n,2);
        return true;
    }
    static void hunt(ServerLevel l,TheListNight.Night n,int night){
        n.phase=night==2?HUNT2:night==3?HUNT3:HUNT4;n.since=0;
        TheListHunt.start(l,n.base,night);
    }

    /** One tick of a night from night two's hunt on. True if the night changed. */
    static boolean tick(ServerLevel l,TheListNight.Night n,List<ServerPlayer> here){
        n.time=Math.min(TheListNight.LIGHT,n.time+TheListNight.RATE);
        if(!hunting(n.phase))return false;
        // 0.4.724: the deputy's round, on nights three and four.
        if(n.phase==HUNT3||n.phase==HUNT4){TheListDeputy.tick(l,n,here);TheListCruiser.tick(l,n,here);TheListSearch.tick(l,n,here);}
        // 0.4.726: Stace's plan, on night four; she may end the night.
        if(n.phase==HUNT4){TheListStace.tick(l,n,here);if(n.phase!=HUNT4)return true;TheListBus.tick(l,n,here);}
        // 0.4.732: the rules from the tapes, tried by readers on Amber's side.
        TheListRules.tick(l,n,here);
        if(n.time>=TheListNight.LIGHT||n.phase==HUNT4&&alive(n)==0){light(l,n,false);return true;}
        return false;
    }
    /** How many of the five are still alive: neither dead on an earlier night nor dead in this one's hunt. */
    static int alive(TheListNight.Night n){
        int alive=0;for(int who:TheListBody.LISTED)if(((n.dead|n.left)&1<<who)==0&&!TheListHunt.dead(n.base,who)&&!TheListHunt.left(n.base,who))alive++;return alive;
    }

    /** The one who went in lying down on their own bed during a hunt: the night ends behind a black. The record in hand keeps it too. */
    static boolean bed(ServerPlayer p,TheListNight.Night n,CompoundTag own){
        if(!p.getUUID().equals(n.slasher)||!LiteraryVignettes.participant(p))return false;
        light(p.serverLevel(),n,true);
        own.merge(LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),TheList.PLACE));
        return true;
    }

    /**
     * First light on night two, three or four. The dead of this night are kept, the hunt is put away and the night is over. Each reader
     * in town, and the one who went in wherever they are, keeps that they have had it; after the fourth, each is ready and the body
     * is the one who went in's own again.
     */
    static void light(ServerLevel l,TheListNight.Night n,boolean bed){
        int night=nightOf(n.phase);
        for(int who:TheListBody.LISTED){if(TheListHunt.dead(n.base,who))n.dead|=1<<who;if(TheListHunt.left(n.base,who))n.left|=1<<who;}
        // 0.4.729: every name out of town alive, none dead.
        boolean allGone=night==4&&n.dead==0&&n.left==ALL;
        // 0.4.732: first light, seen from town, is the one rule from the tapes that holds.
        if(!bed)TheListRules.dawn(l,n);
        TheListHunt.end(l,n.base);TheListDeputy.reset(l,n.base);TheListCruiser.reset(l,n.base);TheListSearch.reset(l,n.base);TheListStace.reset(l,n.base);TheListBus.reset(n.base);
        // 0.4.735: the bus, if it came, pulls out.
        if(night==4)TheListBus.leave(l,n.base);
        n.time=TheListNight.LIGHT;n.since=0;n.bed=bed;
        n.phase=night==2?DONE2:night==3?DONE3:OVER;
        var d=LabyrinthData.get(l.getServer());
        if(bed)for(var q:TheListNight.inTown(l,n.base))HousePackets.send(q,new HouseFadePayload(20,60,40));
        boolean slasherHere=false;
        for(var q:TheList.readers(l,n.base)){
            if(!LiteraryVignettes.participant(q))continue;
            boolean him=q.getUUID().equals(n.slasher);slasherHere|=him;
            var own=LiteraryVignettes.personal(d,q.getUUID(),TheList.PLACE);
            if(him)own.putInt("Night",Math.max(night,own.getInt("Night")));
            if(night==4){
                // 0.4.728: the dawn bus, for the one who went in read as running, at the bench.
                boolean bus=him&&TheListBus.boards(q,n,own);
                if(him){own.putBoolean("WentIn",true);TheListBody.end(q);own=LiteraryVignettes.personal(d,q.getUUID(),TheList.PLACE);own.putInt("Night",4);own.putBoolean("WentIn",true);if(bus)own.putBoolean("Bus",true);if(allGone)own.putBoolean("AllGone",true);}
                if(allGone)own.putBoolean("SawAllGone",true);
                LiteraryVignettes.ready(q,TheList.PLACE,own,outcome(own,him));
                TheList.cue(q,bus?TheListTexts.BUS_TAKEN:him?(allGone?TheListTexts.END_ALL_GONE:TheListTexts.END_WENT_IN):TheListTexts.END_OTHER);
                if(bus)TheListBus.board(q,n.base);
            }else TheList.cue(q,him?(bed?TheListTexts.LIGHT_BED:TheListTexts.LIGHT_OUT):TheListTexts.LIGHT_OTHER);
            LiteraryVignettes.save(d,q.getUUID(),TheList.PLACE,own);
        }
        // The one who went in, not in town at first light, keeps the night all the same; after the fourth, they are ready.
        if(!slasherHere&&n.slasher!=null){
            var own=LiteraryVignettes.personal(d,n.slasher,TheList.PLACE);
            own.putInt("Night",Math.max(night,own.getInt("Night")));
            if(night==4&&!own.getBoolean("Ready")){own.putBoolean("WentIn",true);if(allGone)own.putBoolean("AllGone",true);own.putBoolean("Ready",true);own.putString("Outcome",outcome(own,true));}
            LiteraryVignettes.save(d,n.slasher,TheList.PLACE,own);
        }
        TheListNight.save(l.getServer(),n);TheListPeople.ensure(l,n.base);TheListNight.send(l,n,true);
    }
    /** How the four nights came out for a reader: for the one who went in, by how many of the five they finished. */
    static String outcome(CompoundTag own,boolean wentIn){
        // 0.4.729: one on Amber's side who got any of the five out of town.
        if(!wentIn)return own.getInt("GotOut")!=0?"got_them_out":"was_in_town_at_the_end";
        // 0.4.726: Stace finished it.
        if(own.getBoolean("Stopped"))return "was_stopped";
        // 0.4.728: on the dawn bus.
        if(own.getBoolean("Bus"))return "took_the_bus";
        if(own.getBoolean("AllGone"))return "everyone_got_away";
        int kills=Integer.bitCount(own.getInt("Kills"));
        return kills>=TheListBody.LISTED.length?"went_down_the_whole_list":kills>0?"crossed_names_off":"crossed_no_names_off";
    }

    /** Whether this reader is owed the next night: the one who went in, back in a town whose last night is over. */
    static boolean owed(ServerPlayer p,TheListNight.Night n,CompoundTag own){
        if(!p.getUUID().equals(n.slasher)||!LiteraryVignettes.participant(p))return false;
        return n.phase==DONE2&&own.getInt("Night")==2||n.phase==DONE3&&own.getInt("Night")==3;
    }
    /** The next night begun round the one who went in: ten o'clock in their kitchen, the body theirs, the hook back, the hunt on. */
    static void begin(ServerPlayer p,TheListNight.Night n){
        var l=p.serverLevel();int night=n.phase==DONE2?3:4;
        n.time=TheListNight.TEN;
        TheListBody.give(p);TheListBody.giveHook(p);
        hunt(l,n,night);
        TheListNight.save(p.server,n);TheListPeople.ensure(l,n.base);
        TheList.cue(p,night==3?TheListTexts.NIGHT3:TheListTexts.NIGHT4);
        // 0.4.725: the five are at the field house tonight, and its lights are on.
        if(night==3)TheList.cue(p,TheListTexts.FIELD_LIT);
        // 0.4.723: the town knows by now.
        TheListFlyers.post(l,n.base);
        if(night==3)for(var q:TheList.readers(l,n.base))if(LiteraryVignettes.participant(q))TheList.cue(q,TheListTexts.FLYERS);
        TheListNight.send(l,n,true);
    }
    /** For an operator or a check: night three or four begun round this reader, the nights before put behind them. False if they cannot be. */
    public static boolean begin(ServerPlayer p,net.minecraft.core.BlockPos b,int night){
        if(!LiteraryVignettes.participant(p)||night<3||night>4)return false;
        if(!TheListDen.begin(p,b))return false;
        var n=TheListNight.night(p.server,b);if(n==null)return false;
        n.phase=night==3?DONE2:DONE3;
        var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),TheList.PLACE);
        own.putInt("Night",night-1);LiteraryVignettes.save(d,p.getUUID(),TheList.PLACE,own);
        begin(p,n);
        return true;
    }
    /** For a check: the night's clock set to this hour, the hunt going on. */
    public static void clockTo(ServerLevel l,net.minecraft.core.BlockPos b,long time){
        var n=TheListNight.night(l.getServer(),b);if(n==null)return;n.time=time;TheListNight.save(l.getServer(),n);
    }
    /** For a check: the night's clock brought round to first light, as if it had come. */
    public static void dawn(ServerLevel l,net.minecraft.core.BlockPos b){
        var n=TheListNight.night(l.getServer(),b);if(n==null||!hunting(n.phase))return;
        light(l,n,false);
    }
    /** For a check: night two brought to the first rule written, as if the field had been run. */
    public static void toRan(ServerLevel l,net.minecraft.core.BlockPos b){
        var n=TheListNight.night(l.getServer(),b);if(n==null)return;
        n.phase=TheListNight.RAN;n.since=0;TheListNight.save(l.getServer(),n);
        // 0.4.722: with every test had.
        if(n.slasher!=null){var d=LabyrinthData.get(l.getServer());var own=LiteraryVignettes.personal(d,n.slasher,TheList.PLACE);
            own.putInt("Rules",own.getInt("Rules")|1|1<<TheListDen.RULE_DOOR|1<<TheListDen.RULE_HEART|1<<TheListDen.RULE_TOWER);LiteraryVignettes.save(d,n.slasher,TheList.PLACE,own);}
    }
    /** For a check: one of the five dead on an earlier night by this reader's hand, as the hook would leave them. */
    public static void slain(ServerLevel l,net.minecraft.core.BlockPos b,int who,java.util.UUID killer){
        var n=TheListNight.night(l.getServer(),b);if(n==null)return;
        n.dead|=1<<who;TheListNight.save(l.getServer(),n);TheListBody.killed(l.getServer(),killer,who);TheListPeople.ensure(l,b);
    }
    /** Whether one of the five died on an earlier night: they are not made again. */
    public static boolean dead(TheListNight.Night n,int who){return (n.dead&1<<who)!=0;}
}
