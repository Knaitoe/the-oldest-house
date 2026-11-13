package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.network.HouseFadePayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.548, Gail's third mark (docs/PELL_HOUSE_MARK_0_4_548.md), from the proposal's "Being framed": "Her file. It gains a mark for each
 * incident the reader can't answer. At three marks, Gail takes the older sibling away mid-visit. Clearing a mark. Evidence found and
 * shown to her clears one: the transcript page, the 'DO NOT SWITCH OFF' note, a tape played while she's in the room." And from
 * "Failure: the older sibling is taken away": "Gail's third mark. She comes in the night and takes the older sibling. Their part ends in
 * Gail's car. They are driven to the end of the cul-de-sac and set down outside the House's door, and the house is not dealt to them
 * again. Tilly's night goes ahead without them." Built as proposed, for the owner's open question 2 to be played and judged.
 *
 * <ul>
 * <li><b>The broken thing.</b> On the last night, locked up, at eleven: if an older sibling at the house has two marks on Gail's file
 * (the knife and June's hand), June gets up and goes along the hall to Nessa's school photographs. The first moment nobody awake can see
 * her or them, glass breaks: the last photograph hangs crooked, its glass starred across the girl's face. That reader is the one she will
 * say broke it, and goes on Gail's file for it ("MarkPhoto"), once. If somebody keeps her in sight for two minutes, it keeps.</li>
 * <li><b>June rings Gail</b> from the kitchen telephone, her side heard by anybody near, and goes back to her room. The hour holds at
 * eleven from when she gets up until Gail sets off home.</li>
 * <li><b>Gail in the night.</b> A while after, Gail's car comes up the street (waking anybody asleep) and she comes to the foot of the
 * path. A hand on her opens the conversation panel: to the reader June named, what June has told her and that she cannot leave them
 * there; to anybody else, whom she has come for. Either may show her what they have: the last page off the shed's wall (in June's
 * writing) or the note off the freezer's switch, put into her hands; or, for a reader who has watched a tape, she comes with them,
 * following their way, and watches whatever tape plays in the shed. Each takes a mark off the named reader's file; under three, she says
 * she is taking nobody anywhere tonight and goes. The named reader may instead go and get their things.</li>
 * <li><b>Taken away.</b> Going with her, or not coming for a minute and a half (the horn): a beat to look back, the black, and the car
 * sets them down at the House's door. Their ending lands, nothing resolved, and the house is not dealt to them again as it was: Tilly,
 * with nobody left to bring her out, answers (as after Tilly answered, until the night ends otherwise), and the false entries go on
 * after it. The others at the house keep who was taken; the night goes on without them.</li>
 * </ul>
 *
 * <p>The page and the note are this house's: carried away from it, they go back on the wall. Gail keeps what is given to her. Nothing
 * here is shown that is not already in the house; nobody is hurt.
 */
public final class PellMark {
    private PellMark(){}
    static final int J=PellPeople.JUNE,G=PellPeople.GAIL;
    /** Where the night has got: not yet; June on her way to the photographs; at them, waiting for nobody to see; to the telephone; on it;
     *  back to her room; waiting for Gail; Gail's car coming; Gail at the foot of the path; Gail following a reader to the shed; in it;
     *  walking back to the curb; Gail going; June back to her room with nothing done; done (or never). */
    public static final int NOT_YET=0,TO_PHOTO=1,AT_PHOTO=2,TO_PHONE=3,CALLING=4,TO_ROOM=5,WAITING=6,COMING=7,AT_CURB=8,LEADING=9,
        IN_SHED=10,BACK=11,GOING=12,KEPT=13,DONE=14;
    /** How many marks on Gail's file it takes for her to take a reader away. */
    public static final int TAKEN_AT=3;
    /** How long June waits at the photographs for nobody to see before it keeps; how long after the receiver is down Gail's car comes;
     *  how long Gail waits at the curb before the horn; how long she watches a tape before she has seen enough; how long she waits in
     *  the shed for one; how long she stands unable to get on, unseen, before she is put further along. */
    public static final int KEEPS=2400,COMES_AFTER=300,WAIT=1800,SEEN_ENOUGH=100,SHED_WAIT=600,STUCK=80;
    /** How far the glass is heard; how near Gail a reader comes for her to speak to them; how far off a reader leading her goes before she
     *  stops; how near behind them she walks; how fast; how far apart the points of their way are kept, and how many. */
    public static final double HEARD=24,NEAR=5,LET_GO=8,BESIDE=1.6;
    static final double LED_PACE=.17,TRAIL=.8;static final int TRAIL_MOST=96;
    /** Gail's lines of going without anybody begin at this count of what she has said. */
    static final int STAYING=100;
    /** The last school photograph, from the house's base, and where June stands at it; the last page of the transcript on the shed's
     *  wall; the freezer's power point. */
    public static final BlockPos PHOTO=new BlockPos(-5,1,-66),LAST_PAGE=new BlockPos(8,2,-92),POWER=new BlockPos(6,0,-70);
    static final PellPeople.Spot PHOTO_SPOT=new PellPeople.Spot(-4.5,-65.6,180);
    static Vec3 v(double x,double z){return new Vec3(x,0,z);}
    /** June's ways: her room to the photographs; the photographs to the telephone; the telephone back to her room; the photographs back. */
    static final Vec3[] TO_PHOTO_WAY={v(8.5,-63.4),v(8.5,-65.6),v(PHOTO_SPOT.x(),PHOTO_SPOT.z())},
        PHOTO_TO_PHONE={v(-9.5,-65.6),v(-9.5,-66.6),v(-9.5,-68.4),v(-8.3,-68.6),v(PellCall.AT_PHONE.x(),PellCall.AT_PHONE.z())},
        PHONE_TO_ROOM={v(-9.5,-68.4),v(-9.5,-66.6),v(-9.5,-65.6),v(8.5,-65.6),v(8.5,-63.4),v(PellVisit.HER_ROOM.x(),PellVisit.HER_ROOM.z())},
        PHOTO_TO_ROOM={v(8.5,-65.6),v(8.5,-63.4),v(PellVisit.HER_ROOM.x(),PellVisit.HER_ROOM.z())};
    /** On the page and the note: which house they are from. */
    static final String HOME="PellHome";
    /** What a reader showed Gail ("ShowedGail"). */
    public static final String PAGE="page",NOTE="note",TAPE="tape";
    /** Whom Gail took away from this reader's house, by name. */
    public static final String TOOK="GailTook";
    /** Which of the page and the note Gail has kept. */
    static final int HAS_PAGE=1,HAS_NOTE=2;
    static final String KEY="pell_gail_night",WHO="Gail";

    // ------------------------------------------------------------------------------------------------ for tests and the house
    /** For tests: where the night has got. */
    public static int stage(BlockPos b){return PellPeople.household(b).mark;}
    /** For tests: whom June has said broke the photograph. */
    public static @Nullable UUID marked(BlockPos b){return PellPeople.household(b).marked;}
    /** For tests: whether Gail has come up the path and is waiting at the foot of it. */
    public static boolean waiting(BlockPos b){var h=PellPeople.household(b);return h.mark==AT_CURB&&h.way[G].isEmpty()&&h.gailUpAt>0;}
    /** For tests: Gail has waited as long as she waits. */
    public static void waitedOut(BlockPos b){var h=PellPeople.household(b);h.gailUpAt-=WAIT;}
    /** For tests: whether a reader has gone to Gail's car in the night. */
    public static boolean inCar(BlockPos b,UUID id){return PellPeople.household(b).nightInCar.contains(id);}
    /** For tests: where Gail is in the night, how much of her way is left, and whether she or the curb is seen. */
    public static String gailState(ServerLevel l,BlockPos b){
        var h=PellPeople.household(b);var g=PellPeople.find(l,b,G);
        if(g==null)return "stage "+h.mark+", no Gail";
        var at=rel(b,g.position());var curb=PellVisit.world(b,PellVisit.CAR);
        return String.format(java.util.Locale.ROOT,"stage %d, Gail at (%.1f, %.1f, %.1f), %d points of her way left, she is %s, the curb is %s",h.mark,at.x,at.y,at.z,
            h.way[G].size(),LiteraryVignettes.watched(l,g.getEyePosition())?"seen":"unseen",LiteraryVignettes.watched(l,curb.add(0,.8,0))?"seen":"unseen");
    }
    /** For tests: the house as the knife and the hand left this reader: on Gail's file twice. */
    public static void twoMarks(CompoundTag own){own.putBoolean("MarkKnife",true);own.putBoolean("MarkHand",true);own.putInt("GailMarks",2);}
    /** Whether the hour holds for the night's framing: from June getting up until Gail sets off home. */
    static boolean holds(PellPeople.Household h){return h.visit>=PellWeek.LAST&&h.mark>=TO_PHOTO&&h.mark<GOING;}
    /** Whether Gail is at the house in the night, the night's to move (not the first visit's goodbye). */
    static boolean night(PellPeople.Household h){return h.visit>=PellWeek.LAST&&h.mark>=COMING&&h.mark<KEPT;}

    /** The night undone, for the visit beginning again: the photograph whole, the last page and the note back, Gail and her car gone. */
    static void restart(ServerLevel l,BlockPos b,PellPeople.Household h){
        var photo=b.offset(PHOTO);var ps=l.getBlockState(photo);
        if(PellThing.is(ps,PellThing.Kind.PHOTO_BROKEN))l.setBlock(photo,PellThing.of(PellThing.Kind.PHOTOS,ps.getValue(PellThing.FACING),3),3);
        whole(l,b,LAST_PAGE,PellThing.Kind.TRANSCRIPT,3,2);whole(l,b,POWER,PellThing.Kind.POWER,1,0);
        for(var p:l.getServer().getPlayerList().getPlayers()){takeFrom(p,b,PellRegistry.PAGE.get());takeFrom(p,b,PellRegistry.NOTE.get());}
        for(var e:l.getEntitiesOfClass(ItemEntity.class,PellPeople.area(b).inflate(16),e->evidence(e.getItem(),b)))e.discard();
        if(h.mark>=COMING&&h.mark<KEPT){
            var g=PellPeople.find(l,b,G);if(g!=null)g.discard();
            var c=PellVisit.car(l,b);if(c!=null){c.discard();h.car=null;}
            h.gailGone=true;
        }
        h.mark=NOT_YET;h.marked=null;h.markedName="";h.markSaid=0;h.gailNightSaid=0;h.nightLeg=0;h.gailHas=0;h.gailLeader=null;
        h.markAt=0;h.hungUpAt=0;h.gailUpAt=0;h.gailWatched=0;h.gailStillAt=0;h.gailOff=false;
        h.gailTrail.clear();h.gailWalked.clear();h.nightInCar.clear();h.nightFaded.clear();h.nightLeave.clear();h.took.clear();
    }
    private static void whole(ServerLevel l,BlockPos b,BlockPos rel,PellThing.Kind kind,int gone,int whole){
        var at=b.offset(rel);var st=l.getBlockState(at);
        if(PellThing.is(st,kind)&&st.getValue(PellThing.STAGE)==gone)l.setBlock(at,st.setValue(PellThing.STAGE,whole),3);
    }

    // ------------------------------------------------------------------------------------------------ the night
    /** Every tick of the last night, locked up, before midnight's rite (PellNight). */
    static void tick(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,List<ServerPlayer> here,long now){
        if(h.visit<PellWeek.LAST)return;
        if(h.mark<DONE){
            boolean still=h.way[J].isEmpty();
            switch(h.mark){
                case NOT_YET->begin(h,june,here,now);
                case TO_PHOTO->{if(still){h.mark=AT_PHOTO;h.markAt=now;h.rest[J]=PHOTO_SPOT.look();}}
                case AT_PHOTO->atPhoto(l,b,h,june,here,now);
                case TO_PHONE->{if(still)lift(l,b,h,june,now);}
                case CALLING->calling(l,b,h,june,now);
                case TO_ROOM->{if(still){h.rest[J]=PellVisit.HER_ROOM.look();h.mark=WAITING;}}
                case WAITING->{if(now-h.hungUpAt>=COMES_AFTER)come(l,b,h,here);}
                case COMING->drive(l,b,h,now);
                case KEPT->{if(still){h.rest[J]=PellVisit.HER_ROOM.look();h.mark=DONE;}}
                default->gail(l,b,h,here,now);
            }
        }
        leaving(l,b,h,now);
    }
    /** At eleven: whether June has somebody to say broke something, and if so, up and along the hall to the photographs. */
    private static void begin(PellPeople.Household h,LiteraryActor june,List<ServerPlayer> here,long now){
        if(h.time<PellNight.ELEVEN||h.rite!=PellRite.NOT_YET)return;
        var who=framed(here);
        if(who==null){h.mark=DONE;return;}
        h.marked=who.getUUID();h.markedName=who.getGameProfile().getName();h.markAt=now;h.mark=TO_PHOTO;
        june.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        PellVisit.go(h,J,-1,PHOTO_SPOT.look(),TO_PHOTO_WAY);
    }
    /**
     * Of the readers at the house, the older sibling June will say broke it: one on Gail's file twice or more, the most first, who has not
     * had their ending. A reader whose third mark came off before (the photograph pinned on them once already) is not framed again.
     */
    static @Nullable ServerPlayer framed(List<ServerPlayer> here){
        ServerPlayer best=null;int most=TAKEN_AT-2;
        for(var p:here){
            if(!p.isAlive()||p.isSpectator())continue;
            var own=PellHouse.own(p);
            if(own.getBoolean(PellTilly.PLAYS)||own.getBoolean("Ended")||own.getBoolean("TakenAway"))continue;
            int marks=own.getInt("GailMarks");
            if(own.getBoolean("MarkPhoto")&&marks<TAKEN_AT)continue;
            if(marks>most){most=marks;best=p;}
        }
        return best;
    }
    /** At the photographs: the first moment nobody sees her or them, the glass. Kept in sight long enough, it keeps. */
    private static void atPhoto(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,List<ServerPlayer> here,long now){
        h.rest[J]=PHOTO_SPOT.look();
        if(PellNight.seen(l,june.getEyePosition())||PellNight.seen(l,Vec3.atCenterOf(b.offset(PHOTO)))){
            if(now-h.markAt>=KEEPS){PellVisit.go(h,J,-1,PellVisit.HER_ROOM.look(),PHOTO_TO_ROOM);h.mark=KEPT;}
            return;
        }
        var at=b.offset(PHOTO);var st=l.getBlockState(at);
        Direction facing=PellThing.is(st)?st.getValue(PellThing.FACING):Direction.SOUTH;
        l.setBlock(at,PellThing.of(PellThing.Kind.PHOTO_BROKEN,facing,0),3);
        l.playSound(null,at,SoundEvents.GLASS_BREAK,SoundSource.NEUTRAL,1F,1.1F);
        var c=Vec3.atCenterOf(at);
        for(var p:here){
            if(!p.isAlive()||p.isSpectator()||p.position().distanceToSqr(c)>HEARD*HEARD)continue;
            PellHouse.cue(p,PellSite.inHouse(rel(b,p.position()))?PellTexts.GLASS_HEARD:PellTexts.GLASS_FAR);
            var own=PellHouse.own(p);if(!own.getBoolean("HeardGlass")){own.putBoolean("HeardGlass",true);PellHouse.save(p,own);}
        }
        // The one she will say did it, on Gail's file for it (whether they are at the house now or not).
        var s=l.getServer();var own=record(s,h.marked);
        own.putBoolean("BlamedForPhoto",true);
        if(!own.getBoolean("MarkPhoto")){own.putBoolean("MarkPhoto",true);own.putInt("GailMarks",own.getInt("GailMarks")+1);PellEntries.due(own,PellEntries.PHOTO);}
        save(s,h.marked,own);
        PellVisit.say(h,june,J,PellTexts.JUNE_PHOTO,now);
        PellVisit.go(h,J,-1,PellCall.AT_PHONE.look(),PHOTO_TO_PHONE);h.mark=TO_PHONE;h.markAt=now;
    }
    /** At the telephone: the receiver up. */
    private static void lift(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,long now){
        PellCall.phone(l,b,1);sound(l,b,PellCall.PHONE,PhoneSounds.PICKUP.get(),.8F,1F);
        june.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(PellRegistry.RECEIVER.get()));june.gesture(PellPeople.PHONE);h.rest[J]=PellCall.AT_PHONE.look();
        h.mark=CALLING;h.markSaid=0;h.next=Math.max(h.next,now+20);
    }
    /** June's side of the call, a pause after each line while Gail talks; then the receiver down, and back to her room. */
    private static void calling(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,long now){
        if(!PellVisit.ready(h,now,june))return;
        if(h.markSaid<PellTexts.JUNE_RINGS_GAIL.length){PellVisit.say(h,june,J,PellTexts.JUNE_RINGS_GAIL[h.markSaid++],now);h.next+=PellCall.LISTEN;return;}
        PellCall.phone(l,b,0);sound(l,b,PellCall.PHONE,PhoneSounds.PICKUP.get(),.6F,.8F);
        june.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);june.gesture(0);
        PellVisit.go(h,J,-1,PellVisit.HER_ROOM.look(),PHONE_TO_ROOM);h.mark=TO_ROOM;h.hungUpAt=now;
    }
    /** Gail's car up the street from the end where the House's door is; anybody asleep wakes to it. */
    private static void come(ServerLevel l,BlockPos b,PellPeople.Household h,List<ServerPlayer> here){
        var start=PellVisit.world(b,PellDawn.CAR_IN[0]);var cell=BlockPos.containing(start);
        if(!l.isLoaded(cell)||!l.areEntitiesLoaded(ChunkPos.asLong(cell)))return;
        var c=PellVisit.car(l,b);
        if(c==null){
            c=BikeNightRegistry.TOWN_CAR.get().create(l);if(c==null)return;
            c.place(start,PellDawn.CAR_IN_YAW[0]);c.kind(TownCar.BLUE);
            if(!l.addFreshEntity(c))return;
        }
        c.place(start,PellDawn.CAR_IN_YAW[0]);h.car=c.getUUID();h.nightLeg=1;
        var curb=PellVisit.world(b,PellVisit.CAR);
        l.playSound(null,curb.x,curb.y+.8,curb.z,PellRegistry.CAR_COME.get(),SoundSource.NEUTRAL,1F,1F);
        PellMorning.wakeAll(l,b,h,here,PellTexts.WOKEN_BY_CAR,"WokenByCar");
        h.mark=COMING;
    }
    /** The car a step along its way; at the curb, Gail gets out and comes up onto the footpath. */
    private static void drive(ServerLevel l,BlockPos b,PellPeople.Household h,long now){
        var c=PellVisit.car(l,b);
        if(c==null){h.mark=WAITING;h.hungUpAt=now-COMES_AFTER;return;}
        if(h.nightLeg>=PellDawn.CAR_IN.length){
            c.place(PellVisit.world(b,PellVisit.CAR),PellVisit.CAR_YAW);
            var gail=PellPeople.find(l,b,G);
            if(gail==null)gail=PellPeople.make(l,b,G,PellDawn.GAIL_UP[0],PellVisit.CAR_YAW);
            if(gail==null)return;
            var door=PellVisit.world(b,PellDawn.GAIL_UP[0]);gail.moveTo(door.x,door.y,door.z,0,0);
            h.gailGone=false;h.rest[G]=PellDawn.GAIL_LOOK;h.said[G]=0;h.gailOff=false;
            PellVisit.go(h,G,-1,PellDawn.GAIL_LOOK,PellDawn.GAIL_UP[1],PellDawn.GAIL_UP[2]);
            h.mark=AT_CURB;h.gailUpAt=0;h.gailNightSaid=0;
            return;
        }
        var to=PellVisit.world(b,PellDawn.CAR_IN[h.nightLeg]);var at=c.position();
        double dx=to.x-at.x,dz=to.z-at.z,d=Math.sqrt(dx*dx+dz*dz);
        float yaw=Mth.approachDegrees(c.getYRot(),PellDawn.CAR_IN_YAW[h.nightLeg],6);
        if(d<=PellDawn.CAR_PACE){c.place(to,yaw);h.nightLeg++;}
        else c.place(at.add(dx/d*PellDawn.CAR_PACE,0,dz/d*PellDawn.CAR_PACE),yaw);
    }

    // ------------------------------------------------------------------------------------------------ Gail
    private static void gail(ServerLevel l,BlockPos b,PellPeople.Household h,List<ServerPlayer> here,long now){
        var gail=PellPeople.find(l,b,G);
        if(gail==null){gone(l,b,h,here);return;}
        switch(h.mark){
            case AT_CURB->atCurb(l,b,h,gail,here,now);
            case LEADING->led(l,b,h,gail,now);
            case IN_SHED->inShed(l,b,h,gail,here,now);
            case BACK->{if(h.way[G].isEmpty()){h.mark=AT_CURB;h.gailUpAt=now;h.rest[G]=PellDawn.GAIL_LOOK;}}
            case GOING->going(l,b,h,gail,here,now);
            default->{}
        }
    }
    /** At the foot of the path: a word for whoever comes near, once; the reader June named under three now, she goes without anybody;
     *  waited long enough, the horn, and the named reader is put in the car wherever they are at the house. */
    private static void atCurb(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor gail,List<ServerPlayer> here,long now){
        if(!h.way[G].isEmpty())return;
        if(h.gailUpAt==0)h.gailUpAt=now;
        if(!h.nightInCar.isEmpty())return;
        if(marks(l.getServer(),h.marked)<TAKEN_AT){stays(l,b,h,here,now);return;}
        if(h.gailNightSaid==0&&PellVisit.ready(h,now,gail)&&near(gail,here)){PellVisit.say(h,gail,G,PellTexts.GAIL_NIGHT_HELLO,now);h.gailNightSaid=1;}
        if(now-h.gailUpAt<WAIT)return;
        var p=player(l,h.marked);
        if(p!=null&&here.contains(p)&&p.isAlive()&&!p.isSpectator()){
            for(var q:here)if(q.isAlive()&&!q.isSpectator())PellHouse.cue(q,PellTexts.CAR_HORN);
            take(l,b,h,p,here,now);
            return;
        }
        // Whoever she came for is not here: she goes.
        off(h,b,false);
    }
    /** A mark has come off: under three, the readers here keep that she went without anybody, and she says so and goes. */
    private static void stays(ServerLevel l,BlockPos b,PellPeople.Household h,List<ServerPlayer> here,long now){
        var s=l.getServer();
        for(var q:here){if(!q.isAlive()||q.isSpectator())continue;var own=PellHouse.own(q);own.putBoolean("GailCame",true);own.putBoolean("GailLeft",true);PellHouse.save(q,own);}
        if(h.marked!=null){var own=record(s,h.marked);own.putBoolean("GailCame",true);own.putBoolean("GailLeft",true);save(s,h.marked,own);}
        h.gailNightSaid=STAYING;off(h,b,h.mark==IN_SHED);
    }
    /** Gail sets off for her car: from the curb, round its front to the door; from the shed, back the way she came first. */
    private static void off(PellPeople.Household h,BlockPos b,boolean fromShed){
        h.mark=GOING;h.gailOff=false;
        if(h.gailNightSaid<STAYING)h.gailNightSaid=STAYING+PellTexts.GAIL_STAYS.length;
        h.gailWalkedBack=fromShed;
    }
    /** Her lines, then the walk to the car door; once she is in and whoever she took is out of it, the car goes, the first moment nobody
     *  sees it. */
    private static void going(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor gail,List<ServerPlayer> here,long now){
        if(h.gailNightSaid>=STAYING&&h.gailNightSaid<STAYING+PellTexts.GAIL_STAYS.length){
            if(PellVisit.ready(h,now,gail))PellVisit.say(h,gail,G,PellTexts.GAIL_STAYS[h.gailNightSaid++-STAYING],now);
            return;
        }
        if(!h.gailOff){
            h.gailOff=true;var way=new ArrayList<Vec3>();
            if(h.gailWalkedBack)for(var it=h.gailWalked.descendingIterator();it.hasNext();)way.add(rel(b,it.next()));
            way.add(PellDawn.GAIL_UP[1]);way.add(PellDawn.GAIL_UP[0]);
            PellVisit.go(h,G,-1,PellVisit.CAR_YAW,way.toArray(Vec3[]::new));
            return;
        }
        if(!h.way[G].isEmpty()||!h.nightLeave.isEmpty())return;
        var curb=PellVisit.world(b,PellVisit.CAR);
        if(LiteraryVignettes.watched(l,gail.getEyePosition())||LiteraryVignettes.watched(l,curb.add(0,.8,0)))return;
        gone(l,b,h,here);
    }
    /** Gail in and the car gone: the night goes on. */
    private static void gone(ServerLevel l,BlockPos b,PellPeople.Household h,List<ServerPlayer> here){
        var gail=PellPeople.find(l,b,G);if(gail!=null)gail.discard();h.gailGone=true;
        var c=PellVisit.car(l,b);
        if(c!=null){
            var curb=PellVisit.world(b,PellVisit.CAR);
            l.playSound(null,curb.x,curb.y+.8,curb.z,PellRegistry.CAR_AWAY.get(),SoundSource.NEUTRAL,1F,1F);
            c.discard();h.car=null;
            for(var q:here)if(q.isAlive()&&!q.isSpectator()&&q.position().distanceToSqr(curb)<=HEARD*HEARD*4)PellHouse.cue(q,PellTexts.GAIL_GONE);
        }
        h.mark=DONE;
    }

    // ------------------------------------------------------------------------------------------------ a hand on her
    /** A hand on Gail in the night: true if the night answers it. */
    static boolean talk(ServerPlayer p,BlockPos b,PellPeople.Household h,LiteraryActor a,int who){
        if(who!=G||h.mark<AT_CURB||h.mark>=KEPT)return false;
        if(h.mark==AT_CURB&&h.way[G].isEmpty()&&h.nightInCar.isEmpty()){ask(p,b,h);return true;}
        if(a.saying())return true;
        String line=h.mark==LEADING||h.mark==IN_SHED?PellTexts.GAIL_SHOW_ME:PellTexts.GAIL_NIGHT_TALK[h.said[G]++%PellTexts.GAIL_NIGHT_TALK.length];
        a.say(PellPeople.name(G),line);a.speaks(line);
        return true;
    }
    /** The conversation panel: what June has told her, and what the reader has to show her. */
    private static void ask(ServerPlayer p,BlockPos b,PellPeople.Household h){
        var own=PellHouse.own(p);boolean named=p.getUUID().equals(h.marked);
        own.putBoolean("GailCame",true);PellHouse.save(p,own);
        Talk.begin(p,KEY,WHO);
        for(var line:named?PellTexts.GAIL_NIGHT_MARKED:PellTexts.gailNightOther(h.markedName))Talk.line(p,WHO,line);
        var replies=new ArrayList<Talk.Reply>();
        if(carries(p,b,PellRegistry.PAGE.get()))replies.add(Talk.reply(PellTexts.TELL_PAGE,q->show(q,PAGE)));
        if(carries(p,b,PellRegistry.NOTE.get()))replies.add(Talk.reply(PellTexts.TELL_NOTE,q->show(q,NOTE)));
        if(own.getInt("TapesWatched")!=0)replies.add(Talk.reply(PellTexts.TELL_TAPES,q->show(q,TAPE)));
        if(named)replies.add(Talk.reply(PellTexts.TELL_THINGS,PellMark::things));
        replies.add(Talk.act(PellTexts.TELL_SAY_NOTHING,Talk::close));
        Talk.ask(p,replies);
    }
    /** The reader June named goes with her. */
    private static void things(ServerPlayer p){
        var b=LiteraryVignettes.base(p,PellHouse.PLACE);if(b==null){Talk.close(p);return;}
        var h=PellPeople.household(b);if(h.mark!=AT_CURB||!p.getUUID().equals(h.marked)){Talk.close(p);return;}
        Talk.line(p,WHO,PellTexts.GAIL_IN_YOU_GET);Talk.end(p);
        var l=p.serverLevel();take(l,b,h,p,PellHouse.readers(l,b),l.getGameTime());
    }
    /** What a reader shows her: the page or the note into her hands, or, for the tapes, she comes with them to the shed. */
    private static void show(ServerPlayer p,String what){
        var b=LiteraryVignettes.base(p,PellHouse.PLACE);if(b==null){Talk.close(p);return;}
        var h=PellPeople.household(b);var l=p.serverLevel();
        if(h.mark!=AT_CURB||PellPeople.find(l,b,G)==null){Talk.close(p);return;}
        if(what.equals(TAPE)){
            Talk.line(p,WHO,PellTexts.GAIL_SHOW_ME);Talk.end(p);
            h.gailLeader=p.getUUID();h.gailTrail.clear();h.gailWalked.clear();h.gailWatched=0;h.gailStillAt=0;h.mark=LEADING;h.markAt=l.getGameTime();
            return;
        }
        var item=what.equals(PAGE)?PellRegistry.PAGE.get():PellRegistry.NOTE.get();
        if(!takeOne(p,b,item)){Talk.close(p);return;}
        h.gailHas|=what.equals(PAGE)?HAS_PAGE:HAS_NOTE;
        PellHouse.cue(p,what.equals(PAGE)?PellTexts.GAIL_READS_PAGE:PellTexts.GAIL_HOLDS_NOTE);
        for(var line:what.equals(PAGE)?PellTexts.GAIL_ON_PAGE:PellTexts.GAIL_ON_NOTE)Talk.line(p,WHO,line);
        Talk.end(p);
        cleared(l.getServer(),h,p,what);
    }
    /** A mark off the named reader's file, for what this reader showed her; kept in the shower's record. */
    private static void cleared(MinecraftServer s,PellPeople.Household h,@Nullable ServerPlayer shower,String what){
        if(shower!=null){var own=PellHouse.own(shower);own.putString("ShowedGail",what);own.putBoolean("GailCame",true);PellHouse.save(shower,own);}
        if(h.marked==null)return;
        var own=record(s,h.marked);int marks=own.getInt("GailMarks");
        if(marks>0){own.putInt("GailMarks",marks-1);own.putInt("MarksCleared",own.getInt("MarksCleared")+1);save(s,h.marked,own);}
    }

    // ------------------------------------------------------------------------------------------------ to the shed and back
    /** Gail with a reader to the shed: along the way they walked, a step behind; in the shed, she stops to watch. */
    private static void led(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor gail,long now){
        h.moving[G]=true;
        var p=player(l,h.gailLeader);
        if(p==null||p.level()!=l||!p.isAlive()||p.isSpectator()||p.distanceTo(gail)>LET_GO){
            if(p!=null)PellHouse.cue(p,PellTexts.GAIL_LET_GO);
            back(h,b);return;
        }
        var to=p.position();var last=h.gailTrail.peekLast();
        if(last==null||last.distanceToSqr(to)>=TRAIL*TRAIL){h.gailTrail.addLast(to);while(h.gailTrail.size()>TRAIL_MOST)h.gailTrail.pollFirst();}
        var at=gail.position();
        var walked=h.gailWalked.peekLast();if(walked==null||walked.distanceToSqr(at)>=TRAIL*TRAIL){h.gailWalked.addLast(at);while(h.gailWalked.size()>TRAIL_MOST)h.gailWalked.pollFirst();}
        if(PellSite.inShed(rel(b,at))){h.mark=IN_SHED;h.markAt=now;h.gailWatched=0;return;}
        if(horizontal(at,to)<=BESIDE){face(gail,to);h.gailStillAt=0;return;}
        while(h.gailTrail.size()>1&&horizontal(h.gailTrail.peekFirst(),at)<.4)h.gailTrail.pollFirst();
        var next=h.gailTrail.isEmpty()?to:h.gailTrail.peekFirst();
        if(gail.isNoGravity()){gail.setNoGravity(false);gail.noPhysics=false;}
        SceneFootpaths.walk(gail,next,LED_PACE);
        if(gail.position().distanceToSqr(at)<1e-4){
            if(h.gailStillAt==0)h.gailStillAt=now;
            else if(now-h.gailStillAt>=STUCK&&!LiteraryVignettes.watched(l,gail.getEyePosition())){gail.moveTo(next.x,next.y,next.z,gail.getYRot(),0);h.gailStillAt=0;}
        }else h.gailStillAt=0;
    }
    /** In the shed, facing the screen: a tape playing long enough, she has seen enough; none for a while, there is nothing here. */
    private static void inShed(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor gail,List<ServerPlayer> here,long now){
        h.moving[G]=true;
        var tv=Vec3.atCenterOf(b.offset(PellTapes.TV));face(gail,tv);
        if(PellTapes.playing(b)&&gail.position().distanceTo(tv)<=PellTapes.WATCH+1){
            if(++h.gailWatched<SEEN_ENOUGH)return;
            PellVisit.say(h,gail,G,PellTexts.GAIL_TURN_IT_OFF,now);
            cleared(l.getServer(),h,player(l,h.gailLeader),TAPE);
            if(marks(l.getServer(),h.marked)<TAKEN_AT)stays(l,b,h,here,now);else back(h,b);
            return;
        }
        if(now-h.markAt>=SHED_WAIT&&PellVisit.ready(h,now,gail)){PellVisit.say(h,gail,G,PellTexts.GAIL_NOTHING_HERE,now);back(h,b);}
    }
    /** Back to the foot of the path the way she came. */
    private static void back(PellPeople.Household h,BlockPos b){
        var way=new ArrayList<Vec3>();
        for(var it=h.gailWalked.descendingIterator();it.hasNext();)way.add(rel(b,it.next()));
        way.add(PellDawn.GAIL_UP[2]);
        PellVisit.go(h,G,-1,PellDawn.GAIL_LOOK,way.toArray(Vec3[]::new));
        h.gailLeader=null;h.gailTrail.clear();h.mark=BACK;
    }

    // ------------------------------------------------------------------------------------------------ taken away
    /**
     * Into Gail's car: a beat to look back, then the black, and the street. The reader's ending lands, nothing resolved; Tilly, until the
     * night ends otherwise, answers; the others at the house keep that Gail took them.
     */
    static void take(ServerLevel l,BlockPos b,PellPeople.Household h,ServerPlayer p,List<ServerPlayer> here,long now){
        if(!h.nightInCar.add(p.getUUID()))return;
        h.nightLeave.put(p.getUUID(),now+PellDawn.LOOK_BACK);
        if(Talk.key(p).equals(KEY))Talk.close(p);
        PellHouse.cue(p,PellTexts.NIGHT_IN_CAR);
        var own=PellHouse.own(p);
        own.putBoolean("Ended",true);own.putBoolean("TakenAway",true);own.putBoolean("GailCame",true);own.putString("Outcome","taken_away_in_the_night");
        own.putInt("Beat",Math.max(own.getInt("Beat"),own.getInt("Visit")));
        if((own.getInt("GarageNights")&1<<h.visit)!=0)PellEntries.due(own,PellEntries.night(h.visit));
        own.putBoolean("TillyStayed",true);
        PellHouse.save(p,own);
        h.took.add(p.getUUID());
        String name=p.getGameProfile().getName();
        for(var q:here){
            if(q==p||!q.isAlive()||q.isSpectator())continue;
            var theirs=PellHouse.own(q);theirs.putBoolean("GailCame",true);
            var list=theirs.getList(TOOK,Tag.TAG_STRING);boolean known=false;
            for(int i=0;i<list.size();i++)known|=list.getString(i).equals(name);
            if(!known){list.add(StringTag.valueOf(name));theirs.put(TOOK,list);}
            PellHouse.save(q,theirs);
        }
        off(h,b,false);
    }
    /** Each reader in the car: the black after their beat, and in it the car down the street and the reader out at the House's door. */
    private static void leaving(ServerLevel l,BlockPos b,PellPeople.Household h,long now){
        for(var it=h.nightLeave.entrySet().iterator();it.hasNext();){
            var e=it.next();var id=e.getKey();var p=l.getServer().getPlayerList().getPlayer(id);
            if(p==null||p.level()!=l||!p.isAlive()){it.remove();h.nightFaded.remove(id);continue;}
            long at=e.getValue();
            if(now>=at&&h.nightFaded.add(id))HousePackets.send(p,new HouseFadePayload(PellDawn.FADE_IN,PellDawn.FADE_HOLD,PellDawn.FADE_OUT));
            if(now<at+PellDawn.MOVE_AT)continue;
            var to=PellVisit.world(b,PellDawn.SET_DOWN);
            p.teleportTo(l,to.x,to.y,to.z,PellDawn.SET_LOOK,0);p.setDeltaMovement(Vec3.ZERO);
            PellHouse.cue(p,PellTexts.SET_DOWN);
            var own=PellHouse.own(p);own.putBoolean("SetDown",true);PellHouse.save(p,own);
            it.remove();h.nightFaded.remove(id);
        }
    }
    /** The night's end (PellDawn): whoever Gail took learns, in their record, how it ended; with Tilly out, she did not answer. */
    static void nightEnded(MinecraftServer s,PellPeople.Household h){
        if(h.took.isEmpty()||!PellDawn.out(h))return;
        for(var id:h.took){var own=record(s,id);own.remove("TillyStayed");own.putBoolean("TillyCameOut",true);save(s,id,own);}
    }

    // ------------------------------------------------------------------------------------------------ the page and the note
    /** A hand on the last page on the shed's wall: peeled off, to carry. True if it answered. */
    static boolean page(ServerPlayer p,BlockPos b,BlockPos at,BlockState st,CompoundTag own){
        if(st.getValue(PellThing.STAGE)!=2)return false;
        if(!p.getInventory().add(evidence(PellRegistry.PAGE.get(),b))){PellHouse.cue(p,PellTexts.HANDS_FULL);return true;}
        p.serverLevel().setBlock(at,st.setValue(PellThing.STAGE,3),3);
        p.serverLevel().playSound(null,at,SoundEvents.BOOK_PAGE_TURN,SoundSource.BLOCKS,.6F,1.2F);
        PellHouse.cue(p,PellTexts.PAGE_TAKEN);own.putBoolean("TookPage",true);PellHouse.save(p,own);
        return true;
    }
    /** A hand on the note over the freezer's switch: peeled off, to carry. True if it answered. */
    static boolean note(ServerPlayer p,BlockPos b,BlockPos at,BlockState st,CompoundTag own){
        if(st.getValue(PellThing.STAGE)!=0)return false;
        if(!p.getInventory().add(evidence(PellRegistry.NOTE.get(),b))){PellHouse.cue(p,PellTexts.HANDS_FULL);return true;}
        p.serverLevel().setBlock(at,st.setValue(PellThing.STAGE,1),3);
        p.serverLevel().playSound(null,at,SoundEvents.BOOK_PAGE_TURN,SoundSource.BLOCKS,.6F,1.4F);
        PellHouse.cue(p,PellTexts.NOTE_TAKEN);own.putBoolean("TookNote",true);PellHouse.save(p,own);
        return true;
    }
    /** Once a second for a house with readers at it: the page or the note that nobody at the house has, that is not lying in the lot and
     *  that Gail has not kept, goes back where it was. */
    static void keep(ServerLevel l,BlockPos b,List<ServerPlayer> here){
        var h=PellPeople.household(b);
        keep(l,b,here,PellRegistry.PAGE.get(),LAST_PAGE,PellThing.Kind.TRANSCRIPT,3,2,(h.gailHas&HAS_PAGE)!=0);
        keep(l,b,here,PellRegistry.NOTE.get(),POWER,PellThing.Kind.POWER,1,0,(h.gailHas&HAS_NOTE)!=0);
    }
    private static void keep(ServerLevel l,BlockPos b,List<ServerPlayer> here,Item item,BlockPos rel,PellThing.Kind kind,int gone,int whole,boolean kept){
        var at=b.offset(rel);if(!l.isLoaded(at))return;var st=l.getBlockState(at);
        if(kept||!PellThing.is(st,kind)||st.getValue(PellThing.STAGE)!=gone)return;
        for(var p:here)if(p.isAlive()&&!p.isSpectator()&&carries(p,b,item))return;
        if(!l.getEntitiesOfClass(ItemEntity.class,PellPeople.area(b),e->is(e.getItem(),b,item)).isEmpty())return;
        for(var p:l.getServer().getPlayerList().getPlayers())takeFrom(p,b,item);
        l.setBlock(at,st.setValue(PellThing.STAGE,whole),3);
    }
    static ItemStack evidence(Item item,BlockPos b){var s=new ItemStack(item);var t=new CompoundTag();t.putLong(HOME,b.asLong());s.set(DataComponents.CUSTOM_DATA,CustomData.of(t));return s;}
    private static boolean evidence(ItemStack s,BlockPos b){return is(s,b,PellRegistry.PAGE.get())||is(s,b,PellRegistry.NOTE.get());}
    private static boolean is(ItemStack s,BlockPos b,Item item){
        if(!s.is(item))return false;var d=s.get(DataComponents.CUSTOM_DATA);return d!=null&&d.copyTag().getLong(HOME)==b.asLong();
    }
    static boolean carries(ServerPlayer p,BlockPos b,Item item){
        if(is(p.containerMenu.getCarried(),b,item))return true;
        var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(is(inv.getItem(i),b,item))return true;
        return false;
    }
    private static boolean takeOne(ServerPlayer p,BlockPos b,Item item){
        var inv=p.getInventory();
        for(int i=0;i<inv.getContainerSize();i++)if(is(inv.getItem(i),b,item)){inv.setItem(i,ItemStack.EMPTY);return true;}
        if(is(p.containerMenu.getCarried(),b,item)){p.containerMenu.setCarried(ItemStack.EMPTY);return true;}
        return false;
    }
    private static void takeFrom(ServerPlayer p,BlockPos b,Item item){
        if(is(p.containerMenu.getCarried(),b,item))p.containerMenu.setCarried(ItemStack.EMPTY);
        var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(is(inv.getItem(i),b,item))inv.setItem(i,ItemStack.EMPTY);
    }

    // ------------------------------------------------------------------------------------------------ helpers
    private static CompoundTag record(MinecraftServer s,@Nullable UUID id){return id==null?new CompoundTag():LiteraryVignettes.personal(LabyrinthData.get(s),id,PellHouse.PLACE);}
    private static void save(MinecraftServer s,@Nullable UUID id,CompoundTag own){if(id!=null)LiteraryVignettes.save(LabyrinthData.get(s),id,PellHouse.PLACE,own);}
    private static int marks(MinecraftServer s,@Nullable UUID id){return record(s,id).getInt("GailMarks");}
    private static @Nullable ServerPlayer player(ServerLevel l,@Nullable UUID id){return id==null?null:l.getServer().getPlayerList().getPlayer(id);}
    private static boolean near(LiteraryActor a,List<ServerPlayer> here){for(var p:here)if(p.isAlive()&&!p.isSpectator()&&p.distanceToSqr(a)<=NEAR*NEAR)return true;return false;}
    private static void face(LiteraryActor a,Vec3 to){float y=PellPeople.yawTo(a,to);a.setYRot(y);a.setYBodyRot(y);a.setYHeadRot(y);}
    private static double horizontal(Vec3 a,Vec3 c){double dx=a.x-c.x,dz=a.z-c.z;return Math.sqrt(dx*dx+dz*dz);}
    private static Vec3 rel(BlockPos b,Vec3 at){return at.subtract(b.getX(),b.getY(),b.getZ());}
    private static void sound(ServerLevel l,BlockPos b,BlockPos rel,SoundEvent s,float volume,float pitch){var at=Vec3.atCenterOf(b.offset(rel));l.playSound(null,at.x,at.y,at.z,s,SoundSource.BLOCKS,volume,pitch);}
}
