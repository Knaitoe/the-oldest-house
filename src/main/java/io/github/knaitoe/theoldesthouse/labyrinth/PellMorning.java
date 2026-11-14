package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.HouseFadePayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.CanContinueSleepingEvent;

/**
 * 0.4.531, the Pell house's first morning (docs/PELL_HOUSE_MORNING_0_4_531.md), from the proposal's "1. The placement": "The morning:
 * June has made breakfast, and Gail calls to see how it went." This is the night's passing and breakfast; Gail's call comes next.
 *
 * <ul>
 * <li><b>Sleep.</b> From the time June sends them out to the garage until she calls them to breakfast, a hand on the camp bed lies the
 * reader down on it, and the dark comes in. While every reader at the house has been lying down a moment, the night hurries on: the
 * hour goes on fast, and the house's people are where they were going at once. A reader can get up when they like. The house wakes
 * them twice: at eleven the back door's lock, as June lets herself out; at seven, June calling them to breakfast.</li>
 * <li><b>First light.</b> The stretch June painted in the night has dried. A smear made after she went in has dried as it was. Callum
 * comes in off the line the first moment nobody sees him or where he goes, and lies down on his mattress.</li>
 * <li><b>Six.</b> June gets up, shuts her door behind her, and unlocks the back door, the sliding door and the front door on her way to
 * the kitchen. There she lays breakfast, as she laid dinner: a place for Tilly, one for Callum with nothing on it, one for each reader
 * at the house, and any left over laid for nobody. Then she waits at the stove for seven.</li>
 * <li><b>Seven.</b> She calls them. Tilly gets up and comes to the table with her cane; Callum comes from his room, or, if he is still
 * out on the line, at the first moment nobody sees, once June has called him. Once Tilly is at the table and a reader has come (or
 * nobody has, after a while), she says that somebody was singing in the night, in the wall by her bed, and June says it will be the
 * pipes.</li>
 * <li><b>After.</b> When Tilly has eaten and each reader's breakfast is eaten (or it has gone on long enough), June says to leave the
 * plates. Tilly goes back to her room and Callum to the end of the hall; June clears and goes to the sink. 0.4.532: then Gail rings
 * (PellCall).</li>
 * </ul>
 *
 * <p>Nobody is hurt and nothing is forced.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class PellMorning {
    private PellMorning(){}
    /** First light (five), when the line is dry and Callum comes in; six, when June gets up; half past, where the hour holds while she
     *  goes round the doors to the kitchen; seven, breakfast; eight, where the morning waits. */
    public static final long DAWN=23000,UP=24000,HALF_SIX=24500,SEVEN=25000,EIGHT=26000;
    /** How fast the hour goes on while every reader at the house is asleep, and how long they must all have been lying down first. */
    public static final int HURRY=400,SETTLE=20;
    /** After the small hours: June up, unlocking her way to the kitchen; breakfast laid, waiting for seven; called; and after it. */
    public static final int RISING=PellNight.SMALL_HOURS+1,LAID=RISING+1,CALLED=RISING+2,AFTER=RISING+3;
    /** How long Tilly takes over her breakfast; how long she waits for a reader to come before she says what she heard anyway; how
     *  long breakfast goes on at most; how long June is at the sink after it. */
    public static final int TILLY_EATS=300,WAIT_FOR=600,LONGEST=1200,WASHING=200;
    /** The dark: coming in as a reader lies down, held while they sleep, and going as they wake. */
    static final int FADE_IN=30,FADE_HOLD=12000,FADE_OUT=30;

    static Vec3 v(double x,double z){return new Vec3(x,0,z);}
    /** June's morning: out of her room into the hall; through the laundry to the back door; back up the hall and through the living room
     *  to the sliding door; to the front door; and along the hall to the kitchen and the table's near end. */
    static final Vec3[] OUT_OF_ROOM={v(8.5,-63.4),v(8.5,-65.6)},
        TO_BACK={v(7.5,-65.6),v(7.5,-66.6),v(7.5,-68.4),v(7.5,-69.5),v(8.5,-70.5)},
        TO_SLIDER={v(7.5,-69.5),v(7.5,-68.4),v(7.5,-66.6),v(7.5,-65.6),v(0,-65.6),v(0,-68.5),v(.5,-70.6)},
        TO_FRONT={v(0,-68.5),v(0,-65.6),v(.5,-62),v(.5,-59.6)},
        TO_TABLE={v(.5,-62),v(.5,-65.6),v(-9.5,-65.6),PellVisit.BY_THE_BENCHES,v(PellVisit.TABLE_END.x(),PellVisit.TABLE_END.z())};
    /** Tilly's way from Nessa's bed to the kitchen door, and Callum's from his mattress. */
    static final Vec3[] TILLY_UP={v(-12.5,-59.6),v(-10.6,-59.7),v(-9.5,-62.8),v(-9.5,-64.6),v(-9.5,-65.6),v(-9.5,-66.6)},
        CALLUM_UP={v(-3.5,-61.9),v(-3.5,-63.2),v(-3.5,-64.6),v(-4.5,-65.6),v(-9.5,-65.6),v(-9.5,-66.6)};
    /** Which way somebody faces at the kitchen door, toward the table, if there is no place for them at it. */
    static final float AT_THE_DOOR=150;

    /** Whether it is the morning at the house: from June getting up (0.4.532: on through Gail's call, and after it). */
    public static boolean morning(BlockPos b){var h=PellPeople.household(b);return h.step>=RISING;}
    /** Whether first light has come at the house: the line dry, Callum in or coming in. */
    public static boolean dawned(BlockPos b){return PellPeople.household(b).dawned;}
    /** Whether the night is hurrying on, every reader at the house asleep. */
    public static boolean hurried(BlockPos b){return PellPeople.household(b).hurried;}

    /**
     * For tests: the night as June's rounds leave it, without walking them. Locked up, the thin stretch painted and wet, June in her
     * room with the door shut, Tilly asleep, Callum out on the line by the drive, and midnight.
     */
    public static void smallHours(ServerLevel l,BlockPos b){
        PellNight.lockedUp(l,b);var h=PellPeople.household(b);
        for(int x:PellNight.CELLS)PellNight.paint(l,b,x,PellLine.LAST);
        var c=PellPeople.find(l,b,PellPeople.CALLUM);
        if(c!=null){
            var spot=PellPeople.world(b,PellNight.CALLUM_LINE);float body=PellNight.CALLUM_LINE.look();
            c.moveTo(spot.x,spot.y,spot.z,body,0);c.setYHeadRot(body);c.setYBodyRot(body);c.gesture(0);
            h.asleep[PellPeople.CALLUM]=false;h.rest[PellPeople.CALLUM]=body;h.callumOut=true;h.copied=0;h.copyOf=null;
        }
        h.step=PellNight.SMALL_HOURS;h.waypoint=0;h.time=PellNight.TWELVE;h.next=0;
    }
    /** The morning undone, for the visit beginning again: anyone still lying on the camp bed up again. */
    static void restart(ServerLevel l,BlockPos b,PellPeople.Household h){
        for(var id:h.sleptAt.keySet()){var p=l.getServer().getPlayerList().getPlayer(id);if(p!=null)wake(p,null);}
        h.sleptAt.clear();h.hurried=false;h.dawned=false;h.callumCalled=false;h.morningSaid=0;h.tillySat=0;h.breakfastAt=0;
    }

    // ------------------------------------------------------------------------------------------------ sleep
    /**
     * A hand on the camp bed: from the time June sends them out until breakfast is called, the reader lies down on it, and the dark
     * comes in. True if they did. Their record keeps that they slept in the garage.
     */
    public static boolean lieDown(ServerPlayer p,BlockPos b,BlockPos at,BlockState s,CompoundTag own){
        var h=PellPeople.household(b);
        if(h.step<PellVisit.SENDING||h.step>=CALLED||p.isSleeping()||p.isPassenger()||!p.isAlive()||p.isSpectator())return false;
        p.startSleeping(PellThing.pillow(at,s));
        if(!p.isSleeping())return false;
        h.sleptAt.put(p.getUUID(),p.serverLevel().getGameTime());
        HousePackets.send(p,new HouseFadePayload(FADE_IN,FADE_HOLD,FADE_OUT));
        own.putBoolean("SleptInGarage",true);
        // 0.4.537: which visits' nights they slept out there, for that night's entry in their journal (PellEntries).
        own.putInt("GarageNights",own.getInt("GarageNights")|1<<h.visit);
        return true;
    }
    /** Up off the camp bed and the dark gone; told what woke them, if anything did. */
    static void wake(ServerPlayer p,@Nullable String why){
        if(p.isSleeping())p.stopSleepInBed(false,false);
        HousePackets.send(p,new HouseFadePayload(1,0,FADE_OUT));
        if(why!=null)PellHouse.cue(p,why);
    }
    /** Everyone asleep on the camp bed woken, told what woke them, and (with a key) keeping in their record that it did. */
    static void wakeAll(ServerLevel l,BlockPos b,PellPeople.Household h,List<ServerPlayer> here,@Nullable String why,@Nullable String record){
        for(var p:here){
            if(h.sleptAt.remove(p.getUUID())==null)continue;
            wake(p,why);
            if(record!=null){var own=PellHouse.own(p);if(!own.getBoolean(record)){own.putBoolean(record,true);PellHouse.save(p,own);}}
        }
        h.hurried=false;
    }
    /**
     * Every tick: anyone who was lying on the camp bed and has got up has the dark taken away; and whether every reader at the house
     * has been lying down long enough for the night to hurry on.
     */
    static void sleepers(ServerLevel l,BlockPos b,PellPeople.Household h,List<ServerPlayer> here,long now){
        for(var it=h.sleptAt.entrySet().iterator();it.hasNext();){
            var p=l.getServer().getPlayerList().getPlayer(it.next().getKey());
            if(p==null){it.remove();continue;}
            if(!p.isSleeping()){it.remove();HousePackets.send(p,new HouseFadePayload(1,0,FADE_OUT));}
        }
        int asleep=0,awake=0;
        for(var p:here){
            if(!p.isAlive()||p.isSpectator())continue;
            Long at=h.sleptAt.get(p.getUUID());
            if(at!=null&&p.isSleeping()&&now-at>=SETTLE)asleep++;else awake++;
        }
        h.hurried=asleep>0&&awake==0;
    }
    /** A reader lying on the camp bed stays there until the house wakes them or they get up, whatever the hour where the house stands. */
    @SubscribeEvent public static void keepSleeping(CanContinueSleepingEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p))return;
        var at=p.getSleepingPos().orElse(null);
        if(at!=null&&PellThing.is(p.level().getBlockState(at),PellThing.Kind.CAMP_BED))e.setContinueSleeping(true);
        // 0.4.545: and a reader as Tilly, in Nessa's bed.
        else if(at!=null&&PellThing.is(p.level().getBlockState(at),PellThing.Kind.BED)&&PellTilly.plays(p))e.setContinueSleeping(true);
    }

    // ------------------------------------------------------------------------------------------------ the morning
    private static void next(PellPeople.Household h,int step){h.step=step;h.waypoint=0;h.nudgedAt=0;}
    private static int placeOf(PellPeople.Household h,int who){for(int i=0;i<PellVisit.TABLE.length;i++)if(h.diner[i]==who)return i;return -1;}

    /** Every tick for the house from the small hours (PellNight.tick hands it on). */
    static void tick(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,List<ServerPlayer> here,long now){
        int J=PellPeople.JUNE;boolean still=h.way[J].isEmpty();
        // First light: the line dry; Callum in off it the first moment nobody sees.
        if(h.time>=DAWN&&!h.dawned){dry(l,b);PellScrub.repainted(l,b,h);h.dawned=true;}
        if(h.dawned&&h.callumOut)comeIn(l,b,h);
        switch(h.step-PellNight.SMALL_HOURS){
            case 0->{
                // 0.4.533: a week in, at first light June goes down to the pool and swims her lengths, and is back in her room before
                // she gets up for the day (PellSwim); then a while to dress.
                // 0.4.534: before that, at one, the knife (PellKnife); while it has the house, the night waits for it.
                if(PellKnife.tick(l,b,h,june,here,now))return;
                if(h.visit>=2&&!h.swum){if(h.time>=DAWN)PellSwim.tick(l,b,h,june,here,now);return;}
                if(h.visit>=2&&!h.hurried&&now-h.swumAt<PellSwim.DRESS)return;
                // June asleep in her room until six; then her door opened, and out into the hall, dressed for the day.
                if(h.time<UP||!still)return;
                if(!PellSite.open(l,b,PellSite.JUNE_DOOR))PellHouse.toggle(l,b,PellSite.JUNE_DOOR);
                PellVisit.dress(june,J);
                h.nightSaid=0;next(h,RISING);PellVisit.go(h,J,-1,180,OUT_OF_ROOM);
            }
            case 1->rising(l,b,h,june,here);
            case 2->{if(h.time>=SEVEN&&still&&PellVisit.ready(h,now,june))call(l,b,h,june,here,now);}
            case 3->breakfast(l,b,h,june,here,now);
            case 4->after(l,b,h,june,now);
            // 0.4.532: Gail's call, from the ring on.
            default->PellCall.tick(l,b,h,june,here,now);
        }
    }
    /** June's way to the kitchen: her door shut behind her once nobody is in it, and each outside door unlocked as she comes to it. At
     *  the table, breakfast laid; then to the stove. */
    private static void rising(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,List<ServerPlayer> here){
        int J=PellPeople.JUNE;if(!h.way[J].isEmpty())return;
        switch(h.waypoint){
            case 0->{
                if(PellSite.open(l,b,PellSite.JUNE_DOOR)){if(!PellVisit.clearOf(b,PellSite.JUNE_DOOR,here))return;PellHouse.toggle(l,b,PellSite.JUNE_DOOR);}
                PellVisit.go(h,J,-1,180,TO_BACK);h.waypoint=1;
            }
            case 1->{unlock(l,b,PellSite.BACK_DOOR);PellVisit.go(h,J,-1,180,TO_SLIDER);h.waypoint=2;}
            case 2->{unlock(l,b,PellSite.SLIDER);PellVisit.go(h,J,-1,0,TO_FRONT);h.waypoint=3;}
            case 3->{unlock(l,b,PellSite.FRONT_DOOR);PellVisit.go(h,J,-1,PellVisit.TABLE_END.look(),TO_TABLE);h.waypoint=4;}
            // 0.4.534: the tea towel picked up off the kitchen floor, and the light over the sink off, if they were left.
            default->{PellKnife.tidy(l,b);lay(l,b,h,here);PellVisit.go(h,J,-1,PellVisit.KITCHEN.look(),v(PellVisit.KITCHEN.x(),PellVisit.KITCHEN.z()));next(h,LAID);}
        }
    }
    /** An outside door's lock turned back from inside, if June locked it. */
    private static void unlock(ServerLevel l,BlockPos b,BlockPos door){
        if(!PellVisit.locked(b,door))return;
        PellVisit.unlock(b,door);
        var at=Vec3.atBottomCenterOf(b.offset(door)).add(0,1,0);l.playSound(null,at.x,at.y,at.z,PellRegistry.LOCK.get(),SoundSource.BLOCKS,.8F,1F);
    }
    /**
     * At the table's end: breakfast laid, as dinner was. A place for Tilly, one for Callum with nothing on it, one for each reader at the
     * house, and any left over laid for nobody.
     */
    private static void lay(ServerLevel l,BlockPos b,PellPeople.Household h,List<ServerPlayer> here){
        Arrays.fill(h.diner,PellVisit.NOBODY);
        int t=PellVisit.free(l,b,h,0);if(t>=0)h.diner[t]=PellPeople.TILLY;
        int c=PellVisit.free(l,b,h,1);if(c>=0)h.diner[c]=PellPeople.CALLUM;
        int readers=0;for(var p:here)if(p.isAlive()&&!p.isSpectator())readers++;
        for(int i=0;i<PellVisit.TABLE.length&&readers>0;i++)if(h.diner[i]==PellVisit.NOBODY){h.diner[i]=PellVisit.GUEST;readers--;}
        for(int i=0;i<PellVisit.TABLE.length;i++)PellVisit.plate(l,b,h,i,h.diner[i]==PellPeople.TILLY||h.diner[i]==PellVisit.GUEST?PellVisit.BREAKFAST:PellVisit.LAID_PLATE);
        var at=PellVisit.world(b,v(PellVisit.TABLE_END.x(),PellVisit.TABLE_END.z()));
        l.playSound(null,at.x,at.y+1,at.z,SoundEvents.DECORATED_POT_PLACE,SoundSource.BLOCKS,.5F,1.5F);
    }
    /**
     * Seven. June calls them, and anyone asleep in the garage wakes to it; whoever is too far off to hear her is told it. Tilly gets up
     * and comes to the table with her cane; Callum comes from his room, unless he is still out. Either, with no place left at the table,
     * comes to the kitchen door.
     */
    private static void call(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,List<ServerPlayer> here,long now){
        int J=PellPeople.JUNE,T=PellPeople.TILLY,C=PellPeople.CALLUM;
        PellVisit.say(h,june,J,PellTexts.JUNE_BREAKFAST,now);
        // 0.4.545: a reader as Tilly, asleep in Nessa's bed, wakes to it too.
        wakeAll(l,b,h,PellHouse.readers(l,b),null,null);
        // 0.4.537: a reader who slept in the garage last night, a week in or later, has that night's entry due in their journal.
        var night=PellEntries.night(h.visit);
        if(!night.isEmpty())for(var p:here){if(p.isSpectator())continue;var own=PellHouse.own(p);if((own.getInt("GarageNights")&1<<h.visit)==0)continue;PellEntries.due(own,night);PellHouse.save(p,own);}
        for(var p:here)if(p.isAlive()&&!p.isSpectator()&&p.distanceToSqr(june)>PellVisit.HEAR*PellVisit.HEAR)PellHouse.cue(p,PellTexts.BREAKFAST_FAR);
        var tilly=PellPeople.find(l,b,T);
        if(tilly!=null){h.asleep[T]=false;PellVisit.dress(tilly,T);toTable(h,T,placeOf(h,T),TILLY_UP);if(placeOf(h,T)>=0)PellTilly.cue(l,h,PellTexts.TILLY_R_PLACE);}
        var callum=PellPeople.find(l,b,C);
        if(callum!=null&&!h.callumOut){h.asleep[C]=false;callum.gesture(0);toTable(h,C,placeOf(h,C),CALLUM_UP);}
        h.breakfastAt=now;h.morningSaid=0;h.tillySat=0;h.callumCalled=false;next(h,CALLED);
    }
    private static void toTable(PellPeople.Household h,int who,int place,Vec3[] way){
        if(place>=0)PellVisit.go(h,who,place,PellVisit.TABLE[place].look(),PellVisit.to(way,PellVisit.TABLE[place]));
        else PellVisit.go(h,who,-1,AT_THE_DOOR,way);
    }
    /**
     * Breakfast. June calls Callum in once if he is still out. Tilly eats. Once she has come and a reader has come (or nobody has, after
     * a while), she says what she heard in the night and June answers. When Tilly has eaten, what she heard has been said and each
     * reader's breakfast is eaten (or it has gone on long enough), June says to leave the plates; Tilly goes back to her room and Callum
     * to the end of the hall, and June goes to the table's end to clear.
     */
    private static void breakfast(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,List<ServerPlayer> here,long now){
        int J=PellPeople.JUNE,T=PellPeople.TILLY,C=PellPeople.CALLUM;
        var tilly=PellPeople.find(l,b,T);if(tilly==null)return;
        if(h.callumOut&&!h.callumCalled&&PellVisit.ready(h,now,june)){PellVisit.say(h,june,J,PellTexts.JUNE_CALLUM_IN,now);h.callumCalled=true;return;}
        int t=placeOf(h,T);
        // Tilly has come once she is at the end of her way, at her place or at the door.
        if(h.tillySat==0&&h.way[T].isEmpty())h.tillySat=now;
        // 0.4.545: a reader as Tilly eats theirs, or doesn't (PellVisit.table).
        if(t>=0&&h.tillySat>0&&h.plate[t]==PellVisit.BREAKFAST&&now-h.tillySat>=TILLY_EATS&&h.tillyReader==null)PellVisit.plate(l,b,h,t,PellVisit.SCRAPED);
        // What Tilly heard in the night, and what June says to it. 0.4.533: a week in, June asks Tilly where she put her goggles, by
        // the name of the girl they were bought for, and Tilly says she has none.
        // 0.4.534: after the knife, June has a word for the table once the goggles have been asked after.
        int lines=h.visit>=2&&!h.found.isEmpty()?5:4;
        if(h.tillySat>0&&h.morningSaid<lines&&(PellVisit.come(l,b,here)||now-h.breakfastAt>=WAIT_FOR)){
            if(!PellVisit.ready(h,now,june)||!PellVisit.ready(h,now,tilly))return;
            if(h.morningSaid==4){PellVisit.say(h,june,J,PellTexts.JUNE_KNIVES,now);PellVisit.present(june,"HeardKnivesStay",here);h.morningSaid++;return;}
            boolean week=h.visit>=2,tillys=week?h.morningSaid%2==1:h.morningSaid%2==0;
            // 0.4.545: a week in, a reader as Tilly answers the name on the panel, or says nothing; June waits a while for it, and
            // answers what she is told.
            if(week&&h.morningSaid==1&&h.tillyReader!=null){
                if(!PellTilly.asked(l,b,h,now,PellTexts.JUNE_GOGGLES,PellTexts.GOGGLES_OWN,PellTexts.GOGGLES_YES))return;
                h.morningSaid++;return;
            }
            String line=week?switch(h.morningSaid){case 0->PellTexts.JUNE_GOGGLES;case 1->PellTexts.TILLY_NO_GOGGLES;case 2->h.tillyReader!=null&&h.tillySaidYes?PellTexts.JUNE_GOGGLES_ANSWERED:PellTexts.JUNE_ALL_OVER;default->PellTexts.TILLY_ALL_RIGHT;}
                :switch(h.morningSaid){case 0->PellTexts.TILLY_SINGING;case 1->PellTexts.JUNE_WAS_THERE;case 2->PellTexts.TILLY_IN_THE_WALL;default->PellTexts.JUNE_PIPES;};
            PellVisit.say(h,tillys?tilly:june,tillys?T:J,line,now);
            if(h.morningSaid==0)PellVisit.present(tillys?tilly:june,week?"HeardGoggles":"HeardAboutSinging",here);
            h.morningSaid++;return;
        }
        boolean done=t<0||h.plate[t]!=PellVisit.BREAKFAST;
        for(int i=0;i<PellVisit.TABLE.length;i++)if(h.diner[i]==PellVisit.GUEST&&h.plate[i]==PellVisit.BREAKFAST)done=false;
        if(!(done&&h.morningSaid>=lines||now-h.breakfastAt>=LONGEST)||!h.way[J].isEmpty()||!PellVisit.ready(h,now,june))return;
        PellVisit.say(h,june,J,PellTexts.JUNE_CLEARS,now);
        PellVisit.up(l,b,h,T,PellVisit.TILLY_ROOM.look(),PellVisit.TILLY_BACK);
        if(!h.callumOut)PellVisit.up(l,b,h,C,PellPeople.FIRST_VISIT[C].look(),PellVisit.CALLUM_BACK);
        PellVisit.go(h,J,-1,PellVisit.TABLE_END.look(),v(PellVisit.TABLE_END.x(),PellVisit.TABLE_END.z()));
        for(var p:here){if(!p.isAlive()||p.isSpectator())continue;var own=PellHouse.own(p);if(!own.getBoolean("FirstMorning")){own.putBoolean("FirstMorning",true);PellHouse.save(p,own);}}
        next(h,AFTER);h.clearedAt=now;
    }
    /** After breakfast: at the table's end she clears it and takes the plates to the sink; a while at the sink; and (0.4.532) a while
     *  after, the telephone rings. */
    private static void after(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,long now){
        int J=PellPeople.JUNE;if(!h.way[J].isEmpty())return;
        switch(h.waypoint){
            case 0->{
                for(int i=0;i<PellVisit.TABLE.length;i++)PellVisit.plate(l,b,h,i,PellVisit.NONE);
                var at=PellVisit.world(b,v(PellVisit.TABLE_END.x(),PellVisit.TABLE_END.z()));l.playSound(null,at.x,at.y+1,at.z,SoundEvents.DECORATED_POT_PLACE,SoundSource.BLOCKS,.5F,1.3F);
                june.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(PellRegistry.SCRAPED.get()));
                PellVisit.go(h,J,-1,PellVisit.SINK.look(),v(PellVisit.SINK.x(),PellVisit.SINK.z()));h.clearedAt=now;h.waypoint=1;
            }
            case 1->{if(now-h.clearedAt>=WASHING){june.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);h.waypoint=2;}}
            default->{if(now-h.clearedAt>=WASHING+PellCall.RINGS_AFTER)PellCall.ring(l,b,h,june,now);}
        }
    }
    /**
     * Callum, still out on the line after first light, comes in the first moment nobody sees him or where he goes: before breakfast is
     * called, to his mattress, asleep; at breakfast, to his place at the table; after it, to the end of the hall.
     */
    private static void comeIn(ServerLevel l,BlockPos b,PellPeople.Household h){
        int C=PellPeople.CALLUM;var c=PellPeople.find(l,b,C);if(c==null)return;
        int place=h.step==CALLED?placeOf(h,C):-1;
        Vec3 to=place>=0?Vec3.atBottomCenterOf(b.offset(PellVisit.TABLE[place].chair())):h.step>=CALLED?PellPeople.world(b,PellPeople.FIRST_VISIT[C])
            :PellVisit.world(b,PellVisit.CALLUM_SLEEPS);
        if(PellNight.seen(l,c.getEyePosition())||PellNight.seen(l,to.add(0,.8,0)))return;
        h.callumOut=false;h.copied=0;h.copyOf=null;c.setXRot(0);c.gesture(0);
        if(place>=0){PellVisit.sit(l,b,h,C,c,place);return;}
        if(h.step>=CALLED){PellVisit.stand(l,b,h,C,PellPeople.FIRST_VISIT[C]);return;}
        c.setYRot(0);c.setYBodyRot(0);c.setYHeadRot(0);PellVisit.lie(l,b,h,C,c,PellVisit.CALLUM_SLEEPS);
    }
    /** First light: the stretch June painted in the night has dried, gone over again; a smear made after she went in dries as it is. */
    private static void dry(ServerLevel l,BlockPos b){for(int x:PellNight.CELLS)if(PellNight.stage(l,b,x)==PellLine.LAST)PellNight.paint(l,b,x,PellLine.SECOND);}
}
