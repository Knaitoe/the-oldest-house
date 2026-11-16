package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * 0.4.627, A Little Longer, the seventh slice (docs/A_LITTLE_LONGER_0_4_627.md): the return indoors. The proposal: "On returning indoors,
 * Rowan starts to offer another small job, then abandons the excuse... Let that admission stand. Rowan does not immediately hide it under
 * another joke. For a reader who has already asked to remain, use a reciprocal acknowledgment instead of pretending the invitation is
 * new." Then ordinary time: "They can return indoors, disagree about the toast again, or wash the cups."
 */
public final class LongerReturn {
    private LongerReturn(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.LITTLE_LONGER;
    public static final String STAGE="Return0627",HOW="ReturnHow0627",ORDINARY="Ordinary0627";
    public static final int NONE=0,ASKED=1,ORDINARY_TIME=2,WASHING=3,TO_CHAIR=4,DONE=5;
    public static final String DIRECT="DIRECT",TEASE="TEASE",HOPED="HOPED",QUIET="QUIET",CUPS="CUPS",TOAST="TOAST",SAT="SAT";
    /** Ticks: near enough to be spoken to; the cups washed; letting it stand. */
    public static final int NEAR=4,WASH=120;

    static final class Work {long at=-1;int leg;}
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
    private static void begin(ServerPlayer p){if(!Talk.key(p).equals(LongerRowan.TALK))Talk.begin(p,LongerRowan.TALK,LongerTexts.ROWAN);}
    public static LongerRowan.Spot chair(CompoundTag own){return own.getBoolean("Kitchen0624")?LongerKettle.KITCHEN_SPOT:LongerKettle.ARMCHAIR_SPOT;}
    /** A reader who has already said they wanted to stay: glad they stopped, asked to sit closer, or asked for the kiss. */
    static boolean alreadyAsked(CompoundTag own){
        return own.getBoolean(LongerRoof.GLAD)||own.getBoolean(LongerRoof.CLOSER)||LongerRoof.READER.equals(own.getString(LongerRoof.KISS));
    }

    /** A reader arrives: an unfinished return is the return again; washing finished; Rowan in the chair once sat. */
    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        var w=work(b);w.at=-1;w.leg=0;int st=stage(own);
        if(st==ASKED){own.putInt(STAGE,NONE);st=NONE;}
        if(st==WASHING||st==TO_CHAIR){own.putInt(STAGE,DONE);st=DONE;}
        var a=LongerRowan.find(p.serverLevel(),b);
        if(a!=null&&st==DONE&&SAT.equals(own.getString(ORDINARY))){LongerKettle.place(a,b,chair(own));a.gesture(LongerKettle.CUP_HELD);}
    }

    /** Once a tick, after the roof (LongerRoof, DONE): Rowan at the worktop until the return is spoken. */
    static boolean tick(ServerLevel l,BlockPos b,LiteraryActor a,List<ServerPlayer> here,ServerPlayer p,CompoundTag own){
        var w=work(b);long now=l.getGameTime();int st=stage(own);
        boolean near=p.position().distanceTo(a.position())<NEAR+.5&&!LongerSite.onRoof(LongerKettle.rel(p,b));
        switch(st){
            case NONE->{LongerKettle.keep(a,b,LongerRoof.WORKTOP_SPOT,here);if(near&&!Talk.waiting(p))start(p,a,own);return true;}
            case ASKED,ORDINARY_TIME->{LongerKettle.keep(a,b,LongerRoof.WORKTOP_SPOT,here);return true;}
            case WASHING->{
                if(w.leg==0){if(LongerRowan.step(a,LongerRadio.SINK_SPOT.at(b))){w.leg=1;w.at=now;}return true;}
                LongerKettle.keep(a,b,LongerRadio.SINK_SPOT,here);
                if(w.at>=0&&now-w.at>=WASH){w.at=-1;w.leg=0;stage(p,own,DONE);}
                return true;
            }
            case TO_CHAIR->{if(LongerRowan.step(a,chair(own).at(b))){LongerKettle.place(a,b,chair(own));a.gesture(LongerKettle.CUP_HELD);stage(p,own,DONE);}return true;}
            case DONE->{
                if(SAT.equals(own.getString(ORDINARY)))LongerKettle.keep(a,b,chair(own),here);
                else if(CUPS.equals(own.getString(ORDINARY)))LongerKettle.keep(a,b,new LongerRowan.Spot(LongerRadio.SINK_SPOT.x(),LongerRadio.SINK_SPOT.y(),LongerRadio.SINK_SPOT.z(),LongerRadio.SINK_SPOT.look(),LongerBench.REST),here);
                else LongerKettle.keep(a,b,LongerRoof.WORKTOP_SPOT,here);
                return true;
            }
            default->{return false;}
        }
    }

    private static void start(ServerPlayer p,LiteraryActor a,CompoundTag own){
        stage(p,own,ASKED);Talk.begin(p,LongerRowan.TALK,LongerTexts.ROWAN);
        if(alreadyAsked(own)){line(p,a,LongerTexts.MAKE_SOMETHING_UP);Talk.line(p,"",LongerTexts.NO_R);line(p,a,LongerTexts.GOOD_DONT_GO);}
        else{line(p,a,LongerTexts.RADIO_AGAIN);Talk.line(p,"",LongerTexts.IS_THERE);line(p,a,LongerTexts.DONT_GO_YET);}
        Talk.ask(p,Talk.reply(LongerTexts.DONT_WANT_EITHER,r->answer(r,DIRECT)),Talk.reply(LongerTexts.JUST_SAID,r->answer(r,TEASE)),
            Talk.reply(LongerTexts.HOPING_TOO,r->answer(r,HOPED)),Talk.act(LongerTexts.LET_IT_STAND,r->answer(r,QUIET)));
    }
    private static void answer(ServerPlayer p,String how){
        var a=rowan(p);var own=own(p);if(a==null||stage(own)!=ASKED)return;
        own.putString(HOW,how);stage(p,own,ORDINARY_TIME);
        switch(how){
            case DIRECT->Longer.cue(p,LongerTexts.DOESNT_LOOK_AWAY);
            case TEASE->line(p,a,LongerTexts.TRYING);
            case HOPED->line(p,a,LongerTexts.BOTH_HOPING);
            default->Longer.cue(p,LongerTexts.NEITHER_FILLS);
        }
        ordinary(p);
    }
    private static void ordinary(ServerPlayer p){
        var r=new ArrayList<Talk.Reply>();
        r.add(Talk.act(LongerTexts.WASH_CUPS,LongerReturn::wash));
        String t=own(p).getString(LongerKettle.TOAST);
        if(t.equals(LongerKettle.TEASED)||t.equals(LongerKettle.OFFERED)||t.equals(LongerKettle.BRAVE))r.add(Talk.reply(LongerTexts.ABOUT_TOAST,LongerReturn::toast));
        r.add(Talk.act(LongerTexts.SIT_DOWN,LongerReturn::sit));
        Talk.ask(p,r);
    }
    private static void wash(ServerPlayer p){
        var b=base(p);var a=rowan(p);var own=own(p);if(b==null||a==null||stage(own)!=ORDINARY_TIME)return;
        own.putString(ORDINARY,CUPS);stage(p,own,WASHING);var w=work(b);w.leg=0;w.at=-1;a.gesture(LongerRowan.WALK);
        Longer.cue(p,LongerTexts.WASH_DRY);Talk.end(p);
    }
    private static void toast(ServerPlayer p){
        var a=rowan(p);var own=own(p);if(a==null||stage(own)!=ORDINARY_TIME)return;
        own.putString(ORDINARY,TOAST);stage(p,own,DONE);line(p,a,LongerTexts.CHARCOAL);Talk.end(p);
    }
    private static void sit(ServerPlayer p){
        var b=base(p);var a=rowan(p);var own=own(p);if(b==null||a==null||stage(own)!=ORDINARY_TIME)return;
        own.putString(ORDINARY,SAT);stage(p,own,TO_CHAIR);a.gesture(LongerRowan.WALK);
        Longer.cue(p,LongerTexts.SITS_TOO);Talk.end(p);
    }

    /** A hand on Rowan. */
    static boolean talk(ServerPlayer p,LiteraryActor a,CompoundTag own){
        switch(stage(own)){
            case NONE->{start(p,a,own);return true;}
            case ASKED->{Talk.resume(p,LongerRowan.TALK);return true;}
            case ORDINARY_TIME->{if(!Talk.resume(p,LongerRowan.TALK)){begin(p);ordinary(p);}return true;}
            case WASHING,TO_CHAIR->{return true;}
            case DONE->{begin(p);line(p,a,LongerTexts.STAY_LONG);Talk.end(p);return true;}
            default->{return false;}
        }
    }

    // ------------------------------------------------------------------------------------------------ for cases
    public static void waited(BlockPos b,long ticks){var w=work(b);if(w.at>=0)w.at-=ticks;}
}
