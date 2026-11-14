package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.571, being with Tilly (docs/PELL_HOUSE_NOTES_0_4_571.md). The owner, playing the house alone: "ways to interact with tilly, for
 * singleplayer characters". With Tilly one of the house's people, in Nessa's room, while the house waits on its hour (the afternoon and
 * the evening of the first two visits):
 *
 * <ul>
 * <li><b>Telling her what things look like.</b> Tilly asks ("Is it nice? Tell me what it looks like."; a week in, "Tell me what's
 * different."), and the conversation panel offers what this reader has themselves put a hand on in the house and not yet told her:
 * the stars on the ceiling, the border, the desk, the height marks, the cane in the wardrobe, the photographs, the kennel, the garage,
 * the pool, the chart; a week in, the pool with its cover off, the photographs on the fridge, the knife gone from the block, her hair,
 * the name June called her. She answers each, once. Nothing is offered that the reader has not seen.</li>
 * <li><b>Reading to her.</b> One of Nessa's large-print books is out on top of the shelf, a library book long overdue, a bus ticket in
 * it where Nessa got to. A hand on the shelf takes it up: Tilly goes and sits on the edge of the bed (PellIdle's LISTEN), and the panel
 * offers the book a passage at a time, said aloud by the reader, with Tilly's word between some of them. The first visit reads to the
 * end of the chapter; a week in June has been reading it to her from the ticket every night and never past it, and the reader can read
 * her the rest. How far a reader has read is their own ("StoryRead").</li>
 * </ul>
 *
 * With a reader as Tilly there is nobody to ask and nobody to answer: the book is read aloud, passage by passage, for them to hear.
 * Nothing here changes the visit: when the hour comes the book is put down, and Tilly goes where the day sends her.
 */
public final class PellCompany {
    private PellCompany(){}
    /** The keys of the two exchanges with her (the book, and what things look like), and her name on the panel. */
    public static final String STORY="pell_story",TELL="pell_tell",WHO="Tilly";
    /** How many passages the first visit's sitting reads: to the end of the chapter, where the fog comes down. */
    public static final int CHAPTER=5;
    /** How far from Nessa's room a reader can go before the book is put down; how long it lies open with nothing read. */
    public static final double GONE=9;
    public static final int IDLE=2400;
    /** In a reader's record: how many passages they have read; what they have told Tilly; what of the house they have put a hand on. */
    public static final String READ="StoryRead",TOLD="ToldTilly0571";
    static final String SEEN_A="SeenA0571",SEEN_B="SeenB0571";
    /** The middle of Nessa's room. */
    private static final Vec3 ROOM=new Vec3(-10.5,0,-61);

    // ------------------------------------------------------------------------------------------------ what a reader has looked at
    /** A hand on one of the house's things, as it was when the hand came: it is kept that this reader has seen it. */
    static void saw(CompoundTag own,PellThing.Kind kind,int stage){
        int o=kind.ordinal();String key=o<64?SEEN_A:SEEN_B;own.putLong(key,own.getLong(key)|1L<<(o&63));
        // The wardrobe shut is about to be opened, and what leans in its corner read.
        if(kind==PellThing.Kind.WARDROBE&&(stage&2)==0)own.putBoolean("SawSecondCane",true);
        if(kind==PellThing.Kind.FRIDGE_TOP&&stage==1)own.putBoolean("SawDoorstep",true);
        if(kind==PellThing.Kind.KNIFE_BLOCK&&stage==1)own.putBoolean("SawKnifeGone",true);
        // 0.4.574: Tilly's seedling a week in, tied to its stake. (The last night's second label is only seen: she is asked nothing
        // that night, the house waiting on no hour, so there is nothing kept of it to tell her.)
        if(kind==PellThing.Kind.SEEDLING&&stage==1)own.putBoolean("SawSeedlingGrown",true);
    }
    public static boolean seen(CompoundTag own,PellThing.Kind kind){int o=kind.ordinal();return (own.getLong(o<64?SEEN_A:SEEN_B)&1L<<(o&63))!=0;}
    static boolean inRoom(Vec3 at,BlockPos b){double x=at.x-b.getX(),z=at.z-b.getZ();return x>=-13&&x<-7&&z>=-63&&z<-58;}
    /** Whether the house is waiting on its hour, with the hour not yet come. */
    private static boolean waiting(PellPeople.Household h){int w=PellIdle.window(h);return w!=PellIdle.NONE&&!PellIdle.due(h,w);}
    /** Tilly as one of the house's people, awake and in Nessa's room, while the house waits on its hour. */
    static @Nullable LiteraryActor free(ServerLevel l,BlockPos b,PellPeople.Household h){
        if(h.tillyReader!=null||h.asleep[PellPeople.TILLY]||h.seated[PellPeople.TILLY]||!waiting(h))return null;
        var t=PellPeople.find(l,b,PellPeople.TILLY);return t!=null&&inRoom(t.position(),b)?t:null;
    }
    /** Tilly's line: on the reader's panel, and said by her. */
    private static void says(ServerPlayer p,PellPeople.Household h,LiteraryActor tilly,String line,long now){
        Talk.line(p,WHO,line);PellVisit.say(h,tilly,PellPeople.TILLY,line,now);
    }

    // ------------------------------------------------------------------------------------------------ telling her
    /** Something to tell her: its bit in what a reader has told, when it is theirs to tell, what they say, and what she says to it. */
    record Tell(int bit,Predicate<CompoundTag> when,String say,String answer){}
    private static final int NICE=15,SAME=30;
    static final List<Tell> FIRST=List.of(
        new Tell(0,o->true,PellTexts.TELL_STARS,PellTexts.TILLY_STARS),
        new Tell(1,o->seen(o,PellThing.Kind.BORDER),PellTexts.TELL_BORDER,PellTexts.TILLY_BORDER),
        new Tell(2,o->seen(o,PellThing.Kind.DESK),PellTexts.TELL_DESK,PellTexts.TILLY_MAGNIFIER),
        new Tell(3,o->seen(o,PellThing.Kind.MARKS),PellTexts.TELL_MARKS,PellTexts.TILLY_NOT_AN_N),
        new Tell(4,o->o.getBoolean("SawSecondCane"),PellTexts.TELL_CANE,PellTexts.TILLY_CANE),
        new Tell(5,o->seen(o,PellThing.Kind.PHOTOS),PellTexts.TELL_PHOTOS,PellTexts.TILLY_HER_HAIR),
        new Tell(6,o->seen(o,PellThing.Kind.KENNEL)||seen(o,PellThing.Kind.BOWL),PellTexts.TELL_KENNEL,PellTexts.TILLY_NO_DOG),
        new Tell(7,o->(o.getInt("Rules")&PellTexts.RULE_GARAGE)!=0||seen(o,PellThing.Kind.CAMP_BED),PellTexts.TELL_GARAGE,PellTexts.TILLY_TOO_FAR),
        new Tell(8,o->(o.getInt("Rules")&PellTexts.RULE_POOL)!=0,PellTexts.TELL_POOL,PellTexts.TILLY_SMELL_IT),
        new Tell(9,o->seen(o,PellThing.Kind.CHORE_CHART),PellTexts.TELL_CHART,PellTexts.TILLY_FELT_EDGE),
        new Tell(10,o->seen(o,PellThing.Kind.SEEDLING),PellTexts.TELL_STAKE,PellTexts.TILLY_GOT_HERE_TODAY)),
        WEEK=List.of(
        new Tell(16,o->true,PellTexts.TELL_POOL_OPEN,PellTexts.TILLY_UP_AND_BACK),
        new Tell(19,o->true,PellTexts.TELL_HAIR,PellTexts.TILLY_A_WEEK),
        new Tell(17,o->o.getBoolean("SawDoorstep"),PellTexts.TELL_DOORSTEP,PellTexts.TILLY_NEXT_TO_HER),
        new Tell(18,o->o.getBoolean("SawKnifeGone"),PellTexts.TELL_KNIFE,PellTexts.TILLY_NOT_ME),
        new Tell(20,o->o.getBoolean("HeardNessaAtDinner")||o.getBoolean("HeardNessaAtBedtime"),PellTexts.TELL_NAME,PellTexts.TILLY_SAYS_SORRY),
        new Tell(21,o->o.getBoolean("SawSeedlingGrown"),PellTexts.TELL_GROWN,PellTexts.TILLY_HER_HAND_OVER));
    private static List<Tell> tells(PellPeople.Household h){return h.visit>=2?WEEK:FIRST;}
    /** The line of hers that asks: her first on the first visit, her last a week in. */
    static String asks(int visit){return visit>=2?PellTexts.TILLY_WEEK_TALK[PellTexts.TILLY_WEEK_TALK.length-1]:PellTexts.TILLY_TALK[0];}
    /** Whether this reader has anything left to tell her this visit: something seen and not told, or the kind word. */
    private static boolean anything(PellPeople.Household h,CompoundTag own){
        int told=own.getInt(TOLD);boolean any=false;
        for(var t:tells(h)){if((told&1<<t.bit())!=0)any=true;else if(t.when().test(own))return true;}
        return !any&&(told&1<<(h.visit>=2?SAME:NICE))==0;
    }
    /**
     * A hand on Tilly (PellPeople.talk): whether she asks now, whatever her next line would have been. The first hand of each reader on
     * her in Nessa's room in each of the house's waits (the afternoon, the evening) is asked, if that reader has anything to tell.
     */
    static boolean asksFirst(ServerPlayer p,BlockPos b,PellPeople.Household h,CompoundTag own){
        if(h.readingTo!=null||free(p.serverLevel(),b,h)==null||!anything(h,own))return false;
        int window=PellIdle.window(h);Integer before=h.askedIn.put(p.getUUID(),window);
        return before==null||before!=window;
    }
    /**
     * Tilly is about to say this to a reader's hand (PellPeople.talk). If it is her asking, and they have something to tell, the
     * exchange is begun with her line in it; true then, and the replies are offered once she has said it (offer).
     */
    static boolean opens(ServerPlayer p,BlockPos b,PellPeople.Household h,CompoundTag own,String line){
        if(!line.equals(asks(h.visit))||h.readingTo!=null||free(p.serverLevel(),b,h)==null||!anything(h,own))return false;
        h.askedIn.put(p.getUUID(),PellIdle.window(h));
        Talk.begin(p,TELL,WHO);Talk.line(p,WHO,line);
        return true;
    }
    /** A hand on Tilly with her question, or the book, still waiting on this reader: the panel comes up again where it was. */
    static boolean resume(ServerPlayer p,BlockPos b,PellPeople.Household h){
        if(free(p.serverLevel(),b,h)==null)return false;
        if(Talk.waiting(p,TELL)&&h.readingTo==null)return Talk.resume(p,TELL);
        return p.getUUID().equals(h.readingTo)&&Talk.waiting(p,STORY)&&Talk.resume(p,STORY);
    }
    /** What is offered: up to four things this reader has seen and not told; until something is told, a kind word; and leaving it. */
    static void offer(ServerPlayer p,PellPeople.Household h,CompoundTag own){
        int told=own.getInt(TOLD);var replies=new ArrayList<Talk.Reply>();boolean any=false;int kind=h.visit>=2?SAME:NICE;
        for(var t:tells(h))if((told&1<<t.bit())!=0)any=true;
        for(var t:tells(h)){if(replies.size()>=4)break;if((told&1<<t.bit())==0&&t.when().test(own))replies.add(Talk.reply(t.say(),q->told(q,t.bit(),t.answer())));}
        if(!any&&(told&1<<kind)==0)replies.add(Talk.reply(h.visit>=2?PellTexts.TELL_SAME:PellTexts.TELL_NICE,q->told(q,kind,h.visit>=2?PellTexts.TILLY_BEING_KIND:PellTexts.TILLY_ALWAYS_SAY)));
        if(replies.isEmpty()){Talk.end(p);return;}
        replies.add(Talk.act(PellTexts.TELL_ENOUGH,Talk::close));
        Talk.ask(p,replies);
    }
    private static void told(ServerPlayer p,int bit,String answer){
        var b=LiteraryVignettes.base(p,PellHouse.PLACE);if(b==null){Talk.close(p);return;}
        var h=PellPeople.household(b);var l=p.serverLevel();
        var own=PellHouse.own(p);own.putInt(TOLD,own.getInt(TOLD)|1<<bit);PellHouse.save(p,own);
        // If the hour has taken her off meanwhile, what was said is kept and she does not answer it.
        var tilly=free(l,b,h);if(tilly==null){Talk.end(p);return;}
        says(p,h,tilly,answer,l.getGameTime());
        offer(p,h,own);
    }
    /** For tests: how many things a reader has told her (the kind word not among them). */
    public static int told(CompoundTag own){return Integer.bitCount(own.getInt(TOLD)&~(1<<NICE|1<<SAME));}

    // ------------------------------------------------------------------------------------------------ reading to her
    /**
     * A hand on Nessa's bookshelf, from inside her room, while the house waits on its hour: the book that is out, read to Tilly from
     * where this reader has got to. True if the book answered.
     */
    static boolean book(ServerPlayer p,BlockPos b,CompoundTag own){
        var h=PellPeople.household(b);var l=p.serverLevel();long now=l.getGameTime();
        if(!inRoom(p.position(),b)||!waiting(h)||h.visit>=PellWeek.LAST)return false;
        boolean aloud=h.tillyReader!=null;var tilly=aloud?null:free(l,b,h);
        if(!aloud&&tilly==null)return false;
        if(h.readingTo!=null&&!h.readingTo.equals(p.getUUID())){PellHouse.cue(p,PellTexts.SOMEBODY_READING);return true;}
        // The book already open in this reader's hands: the panel again, where they were.
        if(h.readingTo!=null&&Talk.waiting(p,STORY)&&Talk.resume(p,STORY))return true;
        int read=own.getInt(READ),end=PellTexts.STORY.length,cap=h.visit>=2?end:CHAPTER;
        if(read>=cap&&aloud){PellHouse.cue(p,read>=end?PellTexts.BOOK_FINISHED:PellTexts.BOOK_CHAPTER);return true;}
        Talk.begin(p,STORY,WHO);
        if(read>=cap){says(p,h,tilly,read>=end?PellTexts.TILLY_FINISHED:PellTexts.TILLY_SAID_TOMORROW,now);Talk.end(p);return true;}
        if(tilly!=null)says(p,h,tilly,h.visit>=2&&read<=CHAPTER?PellTexts.TILLY_JUNE_READS:read==0?PellTexts.TILLY_TICKET:PellTexts.TILLY_GO_ON,now);
        h.readingTo=p.getUUID();h.readAt=now;
        next(p,read);
        return true;
    }
    private static void next(ServerPlayer p,int read){Talk.ask(p,Talk.reply(PellTexts.STORY[read],PellCompany::readOn),Talk.act(PellTexts.STORY_STOP,PellCompany::stop));}
    /** The offered passage read aloud: the reader's place moves on, Tilly has her word on it, and the next is offered or the sitting ends. */
    private static void readOn(ServerPlayer p){
        var b=LiteraryVignettes.base(p,PellHouse.PLACE);if(b==null){Talk.close(p);return;}
        var h=PellPeople.household(b);var l=p.serverLevel();long now=l.getGameTime();
        if(!p.getUUID().equals(h.readingTo)){Talk.close(p);return;}
        int end=PellTexts.STORY.length,cap=h.visit>=2?end:CHAPTER;
        var own=PellHouse.own(p);int read=Math.min(end,own.getInt(READ)+1);
        own.putInt(READ,read);if(read>=end)own.putBoolean("ReadEnding",true);
        PellHouse.save(p,own);h.readAt=now;
        var tilly=h.tillyReader!=null?null:PellPeople.find(l,b,PellPeople.TILLY);
        if(read>=end){if(tilly!=null)says(p,h,tilly,PellTexts.TILLY_END,now);h.readingTo=null;Talk.end(p);return;}
        if(read>=cap){
            // The chapter read to its end: June ticks it on the chart. (A week in it is no longer on the chart to tick.)
            if(h.visit<2)h.ticked|=PellJobs.STORY;
            if(tilly!=null)says(p,h,tilly,PellTexts.TILLY_DONT_STOP,now);
            Talk.ask(p,Talk.reply(PellTexts.CHAPTER_ENDS,PellCompany::chapter));
            return;
        }
        String after=PellTexts.STORY_AFTER[read-1];
        if(after!=null&&tilly!=null)says(p,h,tilly,after,now);
        next(p,read);
    }
    /** The end of the chapter said: tomorrow, then. */
    private static void chapter(ServerPlayer p){close(p,PellTexts.TILLY_TOMORROW);}
    /** The book put down before the end of the sitting. */
    private static void stop(ServerPlayer p){close(p,PellTexts.TILLY_KEEP_PLACE);}
    private static void close(ServerPlayer p,String line){
        var b=LiteraryVignettes.base(p,PellHouse.PLACE);if(b==null){Talk.close(p);return;}
        var h=PellPeople.household(b);var l=p.serverLevel();
        var tilly=h.tillyReader!=null?null:PellPeople.find(l,b,PellPeople.TILLY);
        if(tilly!=null)says(p,h,tilly,line,l.getGameTime());
        if(p.getUUID().equals(h.readingTo))h.readingTo=null;
        Talk.end(p);
    }
    /** Once a tick for the house: the book is put down when its reader goes, when nothing has been read for a long while, and when the
     *  hour comes (and then Tilly, if she is one of the house's people, asks for the place to be kept). */
    static void tick(ServerLevel l,BlockPos b,PellPeople.Household h,List<ServerPlayer> here,long now){
        if(h.readingTo==null)return;
        var p=l.getServer().getPlayerList().getPlayer(h.readingTo);
        boolean over=!waiting(h),gone=p==null||!here.contains(p)||p.position().subtract(b.getX()+ROOM.x,p.getY(),b.getZ()+ROOM.z).horizontalDistanceSqr()>GONE*GONE;
        if(!over&&!gone&&now-h.readAt<IDLE)return;
        h.readingTo=null;
        if(p==null)return;
        boolean open=Talk.key(p).equals(STORY);
        if(open&&over&&!gone&&h.tillyReader==null){
            var tilly=PellPeople.find(l,b,PellPeople.TILLY);
            if(tilly!=null){says(p,h,tilly,PellTexts.TILLY_KEEP_PLACE,now);Talk.end(p);return;}
        }
        if(open)Talk.close(p);
    }
    /** Whether Tilly is being read to (PellIdle sits her on the edge of the bed for it). */
    static boolean listening(PellPeople.Household h){return h.readingTo!=null&&h.tillyReader==null;}
    /** The visit begun again: no book open. */
    static void restart(PellPeople.Household h){h.readingTo=null;h.readAt=0;h.askedIn.clear();}
    /** For tests: who is reading at this house, if anybody. */
    public static @Nullable java.util.UUID reader(BlockPos b){return PellPeople.household(b).readingTo;}
}
