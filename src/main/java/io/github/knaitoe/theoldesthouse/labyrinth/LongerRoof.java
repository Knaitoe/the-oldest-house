package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.626, A Little Longer, the sixth slice (docs/A_LITTLE_LONGER_0_4_626.md): chapter 4, "Nothing useful for a while". The proposal: "On
 * the sheltered roof landing, Rowan shows the reader a tiny patch of sky between the wrong buildings... This section has no objective to
 * complete. Offer conversation, shared silence, an invitation to move closer, or a return indoors."
 *
 * <ul>
 * <li><b>The sky</b>: a reader who comes out onto the roof finds Rowan on the bench under the awning; Rowan points up between the two
 * leaning buildings. The panel stays open as free time: ask whether it is real, sit beside them, say nothing for a while, or go back in.
 * Nothing waits on a clock to move on.</li>
 * <li><b>The admission</b>: once the reader has sat or been quiet with them, word for word from the proposal, and four ways to meet it.
 * Then the one act the reader actually did in the workshop, if there was one, is remembered aloud.</li>
 * <li><b>Interest, at a believable pace</b>: glad they stopped (Rowan glad they stayed); the proposal's "Was there really something you
 * wanted to show me?" for a reader Rowan asked out directly or flirting; sitting closer; Rowan's hand, held out with time to take it or
 * not; the reader asking to kiss them, or, once the hand is taken, Rowan asking, interrupted once by a nervous joke. "Not yet" and "Not
 * tonight" are answered kindly and not pressed. Friendship is a real answer and closes the romantic offers with warmth.</li>
 * <li><b>Back in</b>: always on offer. Rowan comes down the roof stair with the reader to the flat; the return is the next slice.</li>
 * </ul>
 */
public final class LongerRoof {
    private LongerRoof(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.LITTLE_LONGER;
    public static final String STAGE="Roof0626",ASKED_REAL="Real0626",SAT="Sat0626",SILENCE="Silence0626",ADMITTED="Admit0626",MEET="Meet0626",
        RECALLED="Recalled0626",GLAD="Glad0626",SHOW="Show0626",CLOSER="Closer0626",HAND="Hand0626",KISS="Kiss0626",FRIEND="Friend0626";
    public static final int NONE=0,SKY=1,QUIET=2,FREE=3,HAND_HELD=4,ASKING=5,DOWN=6,DONE=7;
    public static final String TOOK="TOOK",DECLINED="DECLINED",ROWAN="ROWAN",READER="READER",NOT_TONIGHT="NOT_TONIGHT";
    /** Rowan's gestures here: pointing up at the sky; a hand held out; leaning in. */
    public static final int POINT=13,REACH=14,LEAN=15;
    /** The reader's place on the bench beside Rowan's. */
    public static final BlockPos SEAT=new BlockPos(5,LongerSite.ROOF,-9);
    /** Ticks: near before the sky; the shared silence; before the hand; before Rowan's ask; the joke's beat. */
    public static final int NEAR=5,SILENT=200,HAND_AFTER=120,ASK_AFTER=160,JOKE=60,POINTING=60;
    /** Down from the bench, through the stair house door, down the roof stair, along the flat to the worktop. */
    public static final List<Vec3> DOWNSTAIRS=List.of(new Vec3(4.5,12,-9.5),new Vec3(1,12,-9.5),new Vec3(-2,12,-10.5),new Vec3(-3.5,12,-10.5),
        new Vec3(-5,12,-10.5),new Vec3(-5,12,-11.5),new Vec3(-5,11,-12.5),new Vec3(-5,10,-13.5),new Vec3(-5,9,-14.5),new Vec3(-5,8,-15.5),
        new Vec3(-5,7,-16.5),new Vec3(-5,6,-17.5),new Vec3(-2.5,6,-17.5),new Vec3(-1.5,6,-15.5),new Vec3(3.5,6,-13.5));
    public static final LongerRowan.Spot WORKTOP_SPOT=new LongerRowan.Spot(4.6,6,-12.5,-90,LongerBench.REST);

    static final class Work {UUID reader;long at=-1,handAt=-1,askAt=-1,pointAt=-1;int via;}
    private static final Map<BlockPos,Work> WORK=new HashMap<>();
    static Work work(BlockPos b){return WORK.computeIfAbsent(b.immutable(),k->new Work());}
    public static void clearAll(){WORK.clear();}
    static void forget(BlockPos b){WORK.remove(b);}
    static CompoundTag own(ServerPlayer p){return LongerKettle.own(p);}
    static void save(ServerPlayer p,CompoundTag own){LongerKettle.save(p,own);}
    public static int stage(CompoundTag own){return own.getInt(STAGE);}
    private static void stage(ServerPlayer p,CompoundTag own,int st){own.putInt(STAGE,st);save(p,own);}
    private static void say(LiteraryActor a,String line){a.say(LongerTexts.ROWAN,line);a.speaks(line);}
    private static void line(ServerPlayer p,LiteraryActor a,String text){Talk.line(p,LongerTexts.ROWAN,text);say(a,text);}
    private static @Nullable BlockPos base(ServerPlayer p){return LiteraryVignettes.base(p,PLACE);}
    private static @Nullable LiteraryActor rowan(ServerPlayer p){var b=base(p);return b==null?null:LongerRowan.find(p.serverLevel(),b);}
    private static boolean sitting(ServerPlayer p,BlockPos b){return LongerKettle.sittingIn(p,b,SEAT);}
    private static void begin(ServerPlayer p){if(!Talk.key(p).equals(LongerRowan.TALK))Talk.begin(p,LongerRowan.TALK,LongerTexts.ROWAN);}
    private static LongerRowan.Spot bench(int gesture){var s=LongerRadio.BENCH_SPOT;return new LongerRowan.Spot(s.x(),s.y(),s.z(),s.look(),gesture);}

    /** A reader arrives: a roof left part way is the roof again from the sky (what was said and done is kept); back in, Rowan by the worktop. */
    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        var w=work(b);int st=stage(own);
        if(st>NONE&&st<DOWN){own.putInt(STAGE,NONE);st=NONE;}
        if(st==DOWN){own.putInt(STAGE,DONE);st=DONE;}
        w.reader=p.getUUID();w.at=-1;w.handAt=-1;w.askAt=-1;w.via=0;w.pointAt=-1;
        var a=LongerRowan.find(p.serverLevel(),b);
        if(a!=null&&st==DONE){LongerKettle.place(a,b,WORKTOP_SPOT);a.gesture(LongerBench.REST);}
    }

    /** Once a tick, from the radio chapter's end (LongerRadio, on the roof bench): true while this chapter has Rowan, which is from here on. */
    static boolean tick(ServerLevel l,BlockPos b,LiteraryActor a,List<ServerPlayer> here,ServerPlayer p,CompoundTag own){
        var w=work(b);long now=l.getGameTime();int st=stage(own);
        var at=LongerKettle.rel(p,b);boolean near=LongerSite.onRoof(at)&&p.position().distanceTo(a.position())<NEAR+.5;
        switch(st){
            case NONE->{LongerKettle.keep(a,b,bench(LongerKettle.CUP_HELD),here);if(near&&!Talk.waiting(p))sky(p,a,own);return true;}
            case SKY,FREE->{
                // Sat down beside them by a hand on the bench rather than the panel: the same as sitting from it.
                if(sitting(p,b)&&!own.getBoolean(SAT)){own.putBoolean(SAT,true);save(p,own);if(!Talk.waiting(p)||Talk.key(p).equals(LongerRowan.TALK)){begin(p);admitOrOffer(p,a,own);}}
                boolean pointing=w.pointAt>=0&&now-w.pointAt<POINTING;
                boolean leaning=own.contains(KISS)&&!own.getString(KISS).equals(NOT_TONIGHT)&&now-w.at<80&&w.at>=0;
                LongerKettle.keep(a,b,bench(pointing?POINT:leaning?LEAN:LongerKettle.CUP_HELD),here);
                // The hand: a while after interest, while the reader sits beside them.
                if(st==FREE&&w.handAt>=0&&now-w.handAt>=HAND_AFTER&&!own.contains(HAND)&&!own.getBoolean(FRIEND)&&sitting(p,b)&&!Talk.waiting(p)){w.handAt=-1;hand(p,a,own);}
                if(st==FREE&&w.askAt>=0&&now-w.askAt>=ASK_AFTER&&!own.contains(KISS)&&!own.getBoolean(FRIEND)&&!Talk.waiting(p)){w.askAt=-1;ask(p,a,own,w,now);}
                return true;
            }
            case QUIET->{
                LongerKettle.keep(a,b,bench(LongerKettle.CUP_HELD),here);
                if(now-w.at>=SILENT){own.putBoolean(SILENCE,true);stage(p,own,FREE);begin(p);line(p,a,LongerTexts.NICE_QUIET);admitOrOffer(p,a,own);}
                return true;
            }
            case HAND_HELD->{LongerKettle.keep(a,b,bench(REACH),here);return true;}
            case ASKING->{
                // The nervous joke, then looking away and back: Rowan's ask comes after the beat.
                LongerKettle.keep(a,b,bench(LongerKettle.CUP_HELD),here);
                if(w.at>=0&&now-w.at==JOKE/2){a.setYHeadRot(a.getYRot()-60);}
                if(w.at>=0&&now-w.at>=JOKE){w.at=-1;begin(p);line(p,a,LongerTexts.CAN_I_KISS);
                    Talk.ask(p,Talk.reply(LongerTexts.KISS_YES,r->kissed(r,ROWAN)),Talk.reply(LongerTexts.NOT_TONIGHT_R,LongerRoof::notTonight));}
                return true;
            }
            case DOWN->{
                if(w.via<DOWNSTAIRS.size()){if(LongerRowan.step(a,DOWNSTAIRS.get(w.via).add(b.getX(),b.getY(),b.getZ())))w.via++;return true;}
                LongerKettle.place(a,b,WORKTOP_SPOT);a.gesture(LongerBench.REST);stage(p,own,DONE);return true;
            }
            // 0.4.627: indoors again the next chapter has them (LongerReturn).
            case DONE->{if(LongerReturn.tick(l,b,a,here,p,own))return true;LongerKettle.keep(a,b,WORKTOP_SPOT,here);return true;}
            default->{return false;}
        }
    }

    // ------------------------------------------------------------------------------------------------ the sky and the free time
    private static void sky(ServerPlayer p,LiteraryActor a,CompoundTag own){
        var b=base(p);if(b==null)return;var w=work(b);w.pointAt=p.serverLevel().getGameTime();w.reader=p.getUUID();
        stage(p,own,SKY);Talk.begin(p,LongerRowan.TALK,LongerTexts.ROWAN);line(p,a,LongerTexts.THERE_SKY);line(p,a,LongerTexts.NOT_SURE_SKY);offer(p);
    }
    /** What the reader can do now: always go back in; the rest each once, as they become possible. */
    private static void offer(ServerPlayer p){
        var b=base(p);if(b==null)return;var own=own(p);var r=new ArrayList<Talk.Reply>();
        boolean sat=sitting(p,b);
        if(!own.getBoolean(ASKED_REAL))r.add(Talk.reply(LongerTexts.IS_IT_REAL,x->real(x)));
        if(!sat)r.add(Talk.act(LongerTexts.SIT_BESIDE,LongerRoof::sit));
        if(!own.getBoolean(SILENCE))r.add(Talk.act(LongerTexts.SAY_NOTHING_WHILE,LongerRoof::quiet));
        if(own.getBoolean(ADMITTED)&&!own.getBoolean(FRIEND)){
            if(!own.getBoolean(GLAD))r.add(Talk.reply(LongerTexts.GLAD_STOPPED,LongerRoof::glad));
            String invite=own.getString(LongerRadio.INVITE);
            if(!own.getBoolean(SHOW)&&(invite.equals(LongerRadio.INVITE_FLIRT)||invite.equals(LongerRadio.INVITE_DIRECT)))r.add(Talk.reply(LongerTexts.REALLY_SHOW,LongerRoof::show));
            if(sat&&!own.getBoolean(CLOSER))r.add(Talk.reply(LongerTexts.SIT_CLOSER,LongerRoof::closer));
            if(sat&&!own.contains(KISS)&&(own.getBoolean(GLAD)||own.getBoolean(CLOSER)||TOOK.equals(own.getString(HAND))))r.add(Talk.reply(LongerTexts.CAN_I_KISS_YOU,x->kissed(x,READER)));
            r.add(Talk.reply(LongerTexts.FRIENDS,LongerRoof::friends));
        }
        r.add(Talk.act(LongerTexts.GO_BACK_IN,LongerRoof::goIn));
        Talk.ask(p,r);
    }
    private static boolean live(ServerPlayer p,CompoundTag own){int st=stage(own);return st==SKY||st==FREE;}
    private static void real(ServerPlayer p){var a=rowan(p);var own=own(p);if(a==null||!live(p,own))return;own.putBoolean(ASKED_REAL,true);save(p,own);line(p,a,LongerTexts.REAL_RAIN);offer(p);}
    private static void sit(ServerPlayer p){
        var b=base(p);var a=rowan(p);var own=own(p);if(b==null||a==null||!live(p,own))return;
        if(!sitting(p,b)&&!io.github.knaitoe.theoldesthouse.house.HouseSitting.sit(p,b.offset(SEAT))){Longer.cue(p,LongerTexts.NEARER_BENCH);offer(p);return;}
        own.putBoolean(SAT,true);save(p,own);Longer.cue(p,LongerTexts.MAKE_ROOM);
        admitOrOffer(p,a,own);
    }
    private static void quiet(ServerPlayer p){
        var b=base(p);var own=own(p);if(b==null||!live(p,own))return;
        work(b).at=p.serverLevel().getGameTime();stage(p,own,QUIET);Talk.end(p);
    }
    /** The admission comes once, after sitting or silence; otherwise the offers again. */
    private static void admitOrOffer(ServerPlayer p,LiteraryActor a,CompoundTag own){
        if(!own.getBoolean(ADMITTED)&&(own.getBoolean(SAT)||own.getBoolean(SILENCE))){
            own.putBoolean(ADMITTED,true);stage(p,own,FREE);begin(p);
            line(p,a,LongerTexts.BRAVER);Talk.line(p,"",LongerTexts.LIGHTS_ON);line(p,a,LongerTexts.WIRING);
            Talk.ask(p,Talk.reply(LongerTexts.IT_MATTERS,r->meet(r,"MATTERS")),Talk.reply(LongerTexts.FRIGHTENED_TOO,r->meet(r,"FRIGHTENED")),
                Talk.reply(LongerTexts.GOOD_AT_WIRING,r->meet(r,"TEASE")),Talk.act(LongerTexts.SIT_WITH_IT,r->meet(r,"SAT_WITH")));
            return;
        }
        offer(p);
    }
    private static void meet(ServerPlayer p,String how){
        var a=rowan(p);var own=own(p);if(a==null||!live(p,own)||own.contains(MEET))return;
        own.putString(MEET,how);save(p,own);
        switch(how){
            case "MATTERS"->line(p,a,LongerTexts.MAYBE);
            case "FRIGHTENED"->line(p,a,LongerTexts.NOT_JUST_ME);
            case "TEASE"->line(p,a,LongerTexts.TWO_THINGS);
            default->Longer.cue(p,LongerTexts.BREATHES_OUT);
        }
        // The one thing the reader did in the workshop, if anything, remembered.
        String act=LongerBench.act(own);String said=switch(act){case LongerBench.HELD_PANEL->LongerTexts.REMEMBER_HELD;case LongerBench.RETURNED_SCREW->LongerTexts.REMEMBER_SCREW;
            case LongerBench.MOVED_LAMP->LongerTexts.REMEMBER_LAMP;case LongerBench.WAITED_AT_DOOR->LongerTexts.REMEMBER_DOOR;default->null;};
        if(said!=null&&!own.getBoolean(RECALLED)){own.putBoolean(RECALLED,true);save(p,own);line(p,a,said);}
        offer(p);
    }
    private static void glad(ServerPlayer p){
        var b=base(p);var a=rowan(p);var own=own(p);if(b==null||a==null||!live(p,own))return;
        own.putBoolean(GLAD,true);save(p,own);line(p,a,LongerTexts.GLAD_STAYED);
        if(!own.contains(HAND))work(b).handAt=p.serverLevel().getGameTime();
        offer(p);
    }
    private static void show(ServerPlayer p){
        var a=rowan(p);var own=own(p);if(a==null||!live(p,own))return;
        own.putBoolean(SHOW,true);save(p,own);line(p,a,LongerTexts.YES_SHOW);Talk.line(p,"",LongerTexts.THE_SKY_Q);line(p,a,LongerTexts.AT_FIRST);offer(p);
    }
    private static void closer(ServerPlayer p){
        var b=base(p);var a=rowan(p);var own=own(p);if(b==null||a==null||!live(p,own))return;
        own.putBoolean(CLOSER,true);save(p,own);line(p,a,LongerTexts.YOU_CAN);
        if(!own.contains(HAND))work(b).handAt=p.serverLevel().getGameTime();
        offer(p);
    }
    private static void hand(ServerPlayer p,LiteraryActor a,CompoundTag own){
        stage(p,own,HAND_HELD);begin(p);line(p,a,LongerTexts.HERE_HAND);
        Talk.ask(p,Talk.act(LongerTexts.TAKE_IT,r->handed(r,true)),Talk.reply(LongerTexts.NOT_YET,r->handed(r,false)));
    }
    private static void handed(ServerPlayer p,boolean took){
        var b=base(p);var a=rowan(p);var own=own(p);if(b==null||a==null||stage(own)!=HAND_HELD)return;
        own.putString(HAND,took?TOOK:DECLINED);stage(p,own,FREE);
        if(took){Longer.cue(p,LongerTexts.HAND_TAKEN);if(!own.contains(KISS))work(b).askAt=p.serverLevel().getGameTime();}
        else line(p,a,LongerTexts.ALL_RIGHT);
        offer(p);
    }
    private static void ask(ServerPlayer p,LiteraryActor a,CompoundTag own,Work w,long now){
        stage(p,own,ASKING);w.at=now;begin(p);line(p,a,LongerTexts.CAN_I_START);line(p,a,LongerTexts.PIGEON);Talk.end(p);
    }
    private static void kissed(ServerPlayer p,String who){
        var b=base(p);var a=rowan(p);var own=own(p);if(b==null||a==null)return;int st=stage(own);
        if(!(st==ASKING&&who.equals(ROWAN)||live(p,own)&&who.equals(READER))||own.contains(KISS))return;
        if(!sitting(p,b)){line(p,a,LongerTexts.COME_HERE);stage(p,own,FREE);offer(p);return;}
        if(who.equals(READER))line(p,a,LongerTexts.KISS_YES_R);
        own.putString(KISS,who);stage(p,own,FREE);work(b).at=p.serverLevel().getGameTime();
        Longer.cue(p,LongerTexts.LEANS_IN);offer(p);
    }
    private static void notTonight(ServerPlayer p){
        var a=rowan(p);var own=own(p);if(a==null||stage(own)!=ASKING)return;
        own.putString(KISS,NOT_TONIGHT);stage(p,own,FREE);line(p,a,LongerTexts.STILL_HERE);offer(p);
    }
    private static void friends(ServerPlayer p){
        var a=rowan(p);var own=own(p);if(a==null||!live(p,own))return;
        own.putBoolean(FRIEND,true);save(p,own);line(p,a,LongerTexts.FRIEND_ON_ROOF);offer(p);
    }
    private static void goIn(ServerPlayer p){
        var b=base(p);var a=rowan(p);var own=own(p);if(b==null||a==null)return;int st=stage(own);if(st==DOWN||st==DONE||st==NONE)return;
        line(p,a,LongerTexts.COLLAR);Talk.end(p);
        if(p.isPassenger())p.stopRiding();
        stage(p,own,DOWN);var w=work(b);w.via=0;a.gesture(LongerRowan.WALK);
    }

    // ------------------------------------------------------------------------------------------------ a hand on Rowan
    static boolean talk(ServerPlayer p,LiteraryActor a,CompoundTag own){
        int st=stage(own);
        switch(st){
            case NONE->{sky(p,a,own);return true;}
            case SKY,FREE->{if(!Talk.resume(p,LongerRowan.TALK)){begin(p);offer(p);}return true;}
            case QUIET->{return true;}
            case HAND_HELD,ASKING->{Talk.resume(p,LongerRowan.TALK);return true;}
            case DOWN->{return true;}
            case DONE->{if(LongerReturn.talk(p,a,own))return true;begin(p);line(p,a,LongerTexts.INDOORS_AGAIN);Talk.end(p);return true;}
            default->{return false;}
        }
    }

    // ------------------------------------------------------------------------------------------------ for cases
    public static void waited(BlockPos b,long ticks){var w=work(b);w.at-=ticks;w.handAt-=ticks;w.askAt-=ticks;w.pointAt-=ticks;}
}
