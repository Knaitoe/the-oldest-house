package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.625, A Little Longer, the fifth slice (docs/A_LITTLE_LONGER_0_4_625.md): chapter 3, "The radio isn't a clue". The proposal: "The
 * radio works. A short original tune comes through clearly enough to recognize. Rowan wants it left on." A disagreement is there to be
 * had, word for word from the proposal, and Rowan does the work of its repair; then the initiative changes sides, and Rowan asks the
 * reader outside.
 *
 * <ul>
 * <li><b>The tune</b>: the flat's radio plays an original eight-bar tune (tools/generate_longer_audio.py) whenever it is on, every
 * thirteen seconds with a breath between; while Rowan is talking the loop is the turned-down take, and once it has been agreed quieter,
 * always. Sparing: only while someone is in the place.</li>
 * <li><b>Put it on</b>: twenty seconds after the tea, or a hand on Rowan, they ask for it; a reader who does not, Rowan gets up and does
 * themselves after forty. Then "Leave it on?": leave it, say it suits them (a flirt, remembered), turn it down, or worry that it could
 * be heard.</li>
 * <li><b>The disagreement</b>: turned down or worried, the exchange as the proposal has it; Rowan's answer is sharper than it needs to be
 * and they take their cup to the sink. The reader pushes, asks why, lets it pass (eight quiet seconds) or apologises; whichever, Rowan
 * comes back with the repair. The reader wanted the same, holds that the danger is real, or offers quieter: Rowan keeps the tune and,
 * for the last two, turns it down.</li>
 * <li><b>The invitation</b>: flirting, direct or quiet, by what the reader has done this evening; either answer, Rowan goes up the roof
 * stair on their own feet, opens the stair house door and sits on the bench under the awning. The roof is the next slice.</li>
 * </ul>
 */
public final class LongerRadio {
    private LongerRadio(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.LITTLE_LONGER;
    public static final String STAGE="Radio0625",MOOD="Mood0625",FLIRT="Flirt0625",DISAGREED="Disagreed0625",REPAIR="Repair0625",INVITE="Invite0625";
    public static final int NONE=0,ASK_ON=1,LEAVE_ASK=2,PUSHED=3,PASSING=4,REPAIR_ASK=5,TO_INVITE=6,INVITE_ASK=7,UP=8,ROOF=9;
    public static final String ENJOYED="ENJOYED",TURN_DOWN="TURN_DOWN",WORRIED="WORRIED",SAME="SAME",DANGER="DANGER",QUIETER="QUIETER",
        INVITE_FLIRT="FLIRT",INVITE_DIRECT="DIRECT",INVITE_QUIET="QUIET";
    /** Rowan washing their cup at the sink, busy with it. */
    public static final int BUSY=12;
    /** Ticks: after the tea before the ask; before Rowan puts it on themselves; the tune's loop; quiet before the repair; before the invitation. */
    public static final int ASK_AFTER=400,SELF_AFTER=800,LOOP=260,QUIET_PASS=160,INVITE_AFTER=100;
    public static final LongerRowan.Spot SINK_SPOT=new LongerRowan.Spot(5.5,6,-14.5,-90,BUSY),BENCH_SPOT=new LongerRowan.Spot(4.5,12,-8.5,0,LongerBench.REST),
        RADIO_SPOT=new LongerRowan.Spot(5.5,6,-11.5,-90,LongerKettle.KETTLE);
    /** From the chair round the table to the foot of the roof stair, up its six treads, through the stair house door, along the roof to the bench. */
    public static final List<Vec3> TO_ROOF=List.of(new Vec3(-1.5,6,-15.5),new Vec3(-2.5,6,-17.5),new Vec3(-5,6,-17.5),
        new Vec3(-5,7,-16.5),new Vec3(-5,8,-15.5),new Vec3(-5,9,-14.5),new Vec3(-5,10,-13.5),new Vec3(-5,11,-12.5),new Vec3(-5,12,-11.5),
        new Vec3(-5,12,-10.5),new Vec3(-3.5,12,-10.5),new Vec3(-2,12,-10.5),new Vec3(1,12,-9.5),new Vec3(4.5,12,-9.5));

    static final class Work {UUID reader;long at=-1,nextLoop=-1;boolean low;int leg=-1,via;}
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
    static boolean radioOn(ServerLevel l,BlockPos b){var s=l.getBlockState(b.offset(LongerSite.FLAT_RADIO));return LongerBlock.is(s,LongerBlock.Kind.RADIO)&&s.getValue(LongerBlock.STAGE)==1;}
    private static void radio(ServerLevel l,BlockPos b,boolean on){var at=b.offset(LongerSite.FLAT_RADIO);var s=l.getBlockState(at);if(LongerBlock.is(s,LongerBlock.Kind.RADIO)&&(s.getValue(LongerBlock.STAGE)==1)!=on)l.setBlock(at,s.setValue(LongerBlock.STAGE,on?1:0),3);}
    /** For cases and the client proof: the tune is turned down here. */
    public static boolean low(BlockPos b){return work(b).low;}
    private static LongerRowan.Spot chair(CompoundTag own){return own.getBoolean("Kitchen0624")?LongerKettle.KITCHEN_SPOT:LongerKettle.ARMCHAIR_SPOT;}

    /** A reader arrives: a chapter left part way goes back to the tea finished (what was said is kept); Rowan up on the roof if it is over. */
    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        var l=p.serverLevel();var w=work(b);int st=stage(own);
        if(st>NONE&&st<UP){own.putInt(STAGE,NONE);st=NONE;}
        if(st==UP){own.putInt(STAGE,ROOF);st=ROOF;}
        w.reader=p.getUUID();w.at=-1;w.leg=-1;w.via=0;w.low=own.contains(REPAIR)&&!own.getString(REPAIR).equals(SAME);
        var a=LongerRowan.find(l,b);
        if(a!=null&&st==ROOF){LongerKettle.place(a,b,BENCH_SPOT);a.gesture(LongerKettle.CUP_HELD);}
    }

    /** The tune, while the flat's radio is on: every thirteen seconds, the turned-down take while Rowan talks or once agreed quieter. */
    static void tune(ServerLevel l,BlockPos b,@Nullable LiteraryActor a){
        var w=work(b);long now=l.getGameTime();
        // 0.4.630: after the cut the radio only ever finds the space between stations (LongerAfter).
        if(!radioOn(l,b)||LongerAfter.is(b)){w.nextLoop=-1;return;}
        if(w.nextLoop>now)return;
        var r=Vec3.atCenterOf(b.offset(LongerSite.FLAT_RADIO));
        boolean quiet=w.low||a!=null&&a.saying();
        l.playSound(null,r.x,r.y,r.z,quiet?LongerRegistry.TUNE_LOW.get():LongerRegistry.TUNE.get(),SoundSource.RECORDS,quiet?.35F:.7F,1);
        w.nextLoop=now+LOOP;
    }
    /** The radio switched by a hand: the tune starts at once (or stops). */
    public static void switched(ServerLevel l,BlockPos b){work(b).nextLoop=-1;}

    /**
     * Once a tick, from the kettle chapter's end (LongerKettle): true while this chapter has Rowan. Before the ask Rowan is in their chair;
     * from the invitation on, on their way up or on the roof bench.
     */
    static boolean tick(ServerLevel l,BlockPos b,LiteraryActor a,List<ServerPlayer> here,ServerPlayer p,CompoundTag own,long doneAt){
        var w=work(b);long now=l.getGameTime();int st=stage(own);
        switch(st){
            case NONE->{
                LongerKettle.keep(a,b,chair(own),here);
                if(doneAt>=0&&now-doneAt>=ASK_AFTER&&!Talk.waiting(p)&&LongerSite.inFlat(LongerKettle.rel(p,b)))askOn(p,a,own);
                return true;
            }
            case ASK_ON->{
                if(radioOn(l,b)){leaveAsk(p,a,own);return true;}
                if(w.leg>=0){putOnTick(l,b,p,a,own,w,here);return true;}
                LongerKettle.keep(a,b,chair(own),here);
                if(w.at>=0&&now-w.at>=SELF_AFTER-ASK_AFTER){w.leg=0;w.via=0;a.gesture(LongerRowan.WALK);}
                return true;
            }
            case LEAVE_ASK,REPAIR_ASK,INVITE_ASK->{
                if(st==REPAIR_ASK&&own.getBoolean(DISAGREED)&&w.leg==1){LongerKettle.keep(a,b,SINK_SPOT,here);return true;}
                if(w.leg==2){if(LongerRowan.step(a,chair(own).at(b)))w.leg=-1;return true;}
                LongerKettle.keep(a,b,chair(own),here);return true;
            }
            case PUSHED,PASSING->{
                // Rowan to the sink with their cup, and busy there.
                if(w.leg==0){if(LongerRowan.step(a,SINK_SPOT.at(b)))w.leg=1;return true;}
                LongerKettle.keep(a,b,SINK_SPOT,here);
                if(st==PASSING&&now-w.at>=QUIET_PASS)repair(p,a,own);
                return true;
            }
            case TO_INVITE->{
                if(w.leg==2){if(LongerRowan.step(a,chair(own).at(b)))w.leg=-1;return true;}
                LongerKettle.keep(a,b,chair(own),here);
                if(now-w.at>=INVITE_AFTER&&!Talk.waiting(p))invite(p,a,own);
                return true;
            }
            case UP->{
                if(w.via<TO_ROOF.size()){
                    var to=TO_ROOF.get(w.via).add(b.getX(),b.getY(),b.getZ());
                    if(w.via==TO_ROOF.size()-4)door(l,b,true);
                    if(LongerRowan.step(a,to))w.via++;
                    return true;
                }
                if(LongerRowan.step(a,BENCH_SPOT.at(b))){LongerKettle.place(a,b,BENCH_SPOT);a.gesture(LongerKettle.CUP_HELD);stage(p,own,ROOF);}
                return true;
            }
            // 0.4.626: on the roof the next chapter has them (LongerRoof).
            case ROOF->{if(LongerRoof.tick(l,b,a,here,p,own))return true;LongerKettle.keep(a,b,new LongerRowan.Spot(BENCH_SPOT.x(),BENCH_SPOT.y(),BENCH_SPOT.z(),BENCH_SPOT.look(),LongerKettle.CUP_HELD),here);return true;}
            default->{return false;}
        }
    }
    private static void door(ServerLevel l,BlockPos b,boolean open){
        var at=b.offset(LongerSite.ROOF_DOOR);var s=l.getBlockState(at);
        if(s.getBlock() instanceof DoorBlock d&&s.getValue(DoorBlock.OPEN)!=open)d.setOpen(null,l,s,at,open);
    }

    // ------------------------------------------------------------------------------------------------ put it on
    private static void askOn(ServerPlayer p,LiteraryActor a,CompoundTag own){
        var b=base(p);if(b==null)return;var w=work(b);w.reader=p.getUUID();w.at=p.serverLevel().getGameTime();
        stage(p,own,ASK_ON);
        if(radioOn(p.serverLevel(),b)){leaveAsk(p,a,own);return;}
        Talk.begin(p,LongerRowan.TALK,LongerTexts.ROWAN);line(p,a,LongerTexts.PUT_IT_ON);Talk.end(p);
    }
    /** Nobody put it on: Rowan to the worktop, the set on, back to the chair. */
    private static void putOnTick(ServerLevel l,BlockPos b,ServerPlayer p,LiteraryActor a,CompoundTag own,Work w,List<ServerPlayer> here){
        switch(w.leg){
            case 0->{if(LongerRowan.step(a,RADIO_SPOT.at(b))){w.leg=1;w.at=l.getGameTime();}}
            case 1->{LongerKettle.keep(a,b,RADIO_SPOT,here);if(l.getGameTime()-w.at==10){radio(l,b,true);switched(l,b);l.playSound(null,b.offset(LongerSite.FLAT_RADIO),SoundEvents.LEVER_CLICK,SoundSource.BLOCKS,.4F,.8F);}
                if(l.getGameTime()-w.at>=20){w.leg=2;a.gesture(LongerRowan.WALK);}}
            default->{}
        }
    }
    private static void leaveAsk(ServerPlayer p,LiteraryActor a,CompoundTag own){
        var b=base(p);if(b==null)return;var w=work(b);if(w.leg==1||w.leg==0)w.leg=2;
        stage(p,own,LEAVE_ASK);
        Talk.begin(p,LongerRowan.TALK,LongerTexts.ROWAN);line(p,a,LongerTexts.LEAVE_IT_ON);
        Talk.ask(p,Talk.reply(LongerTexts.LEAVE_IT,r->mood(r,ENJOYED,false)),Talk.reply(LongerTexts.SUITS,r->mood(r,ENJOYED,true)),
            Talk.reply(LongerTexts.TURN_IT_DOWN,r->mood(r,TURN_DOWN,false)),Talk.reply(LongerTexts.HEARD,r->mood(r,WORRIED,false)));
    }
    private static void mood(ServerPlayer p,String how,boolean flirt){
        var b=base(p);var a=rowan(p);if(b==null||a==null)return;var own=own(p);if(stage(own)!=LEAVE_ASK)return;
        own.putString(MOOD,how);if(flirt)own.putBoolean(FLIRT,true);
        var w=work(b);w.at=p.serverLevel().getGameTime();
        if(how.equals(ENJOYED)){line(p,a,flirt?LongerTexts.IT_DOES:LongerTexts.GOOD);stage(p,own,TO_INVITE);Talk.end(p);return;}
        // The disagreement, word for word.
        own.putBoolean(DISAGREED,true);
        Talk.line(p,"",LongerTexts.LISTENING);line(p,a,LongerTexts.HAVE_BEEN);Talk.line(p,"",LongerTexts.CHANGING);line(p,a,LongerTexts.I_KNOW);
        line(p,a,LongerTexts.SHARP);
        stage(p,own,PUSHED);w.leg=0;w.via=0;a.gesture(LongerRowan.WALK);
        Talk.ask(p,Talk.reply(LongerTexts.NOT_MUSIC,r->pushed(r)),Talk.reply(LongerTexts.WHY_MATTER,r->pushed(r)),
            Talk.act(LongerTexts.LET_PASS,LongerRadio::letPass),Talk.reply(LongerTexts.SORRY,r->pushed(r)));
    }
    private static void pushed(ServerPlayer p){var a=rowan(p);var own=own(p);if(a==null||stage(own)!=PUSHED)return;repair(p,a,own);}
    private static void letPass(ServerPlayer p){
        var b=base(p);var own=own(p);if(b==null||stage(own)!=PUSHED)return;
        work(b).at=p.serverLevel().getGameTime();stage(p,own,PASSING);Talk.end(p);
    }
    /** Rowan's half of it, every way: "That came out badly." */
    private static void repair(ServerPlayer p,LiteraryActor a,CompoundTag own){
        if(!Talk.key(p).equals(LongerRowan.TALK))Talk.begin(p,LongerRowan.TALK,LongerTexts.ROWAN);
        line(p,a,LongerTexts.BADLY);Talk.line(p,"",LongerTexts.WHAT_DID);line(p,a,LongerTexts.BROKEN_RADIO);
        stage(p,own,REPAIR_ASK);
        Talk.ask(p,Talk.reply(LongerTexts.WANTED_TOO,r->repaired(r,SAME)),Talk.reply(LongerTexts.STILL_REAL,r->repaired(r,DANGER)),
            Talk.reply(LongerTexts.JUST_QUIETER,r->repaired(r,QUIETER)));
    }
    private static void repaired(ServerPlayer p,String how){
        var b=base(p);var a=rowan(p);if(b==null||a==null)return;var own=own(p);if(stage(own)!=REPAIR_ASK)return;
        own.putString(REPAIR,how);var w=work(b);
        line(p,a,switch(how){case SAME->LongerTexts.BOTH_DID;case DANGER->LongerTexts.QUIETER_THEN;default->LongerTexts.LIVE_WITH;});
        if(!how.equals(SAME)){w.low=true;w.nextLoop=-1;}
        w.at=p.serverLevel().getGameTime();w.leg=2;a.gesture(LongerRowan.WALK);
        stage(p,own,TO_INVITE);Talk.end(p);
    }

    // ------------------------------------------------------------------------------------------------ the invitation
    /** How Rowan asks: flirting back if the reader flirted; quietly if they came for one cup or said nothing to the kettle; plainly otherwise. */
    public static String inviteKind(CompoundTag own){
        if(own.getBoolean(FLIRT))return INVITE_FLIRT;
        if(own.getString(LongerKettle.STAY).equals("ONE_CUP")||own.getBoolean(LongerKettle.SAID_NOTHING))return INVITE_QUIET;
        return INVITE_DIRECT;
    }
    private static void invite(ServerPlayer p,LiteraryActor a,CompoundTag own){
        String kind=inviteKind(own);own.putString(INVITE,kind);stage(p,own,INVITE_ASK);
        Talk.begin(p,LongerRowan.TALK,LongerTexts.ROWAN);
        if(kind.equals(INVITE_QUIET))line(p,a,LongerTexts.BRING_CUP);
        else{line(p,a,LongerTexts.COME_OUTSIDE);Talk.line(p,"",LongerTexts.WHY);line(p,a,LongerTexts.SHOW_YOU);if(kind.equals(INVITE_FLIRT))line(p,a,LongerTexts.STAND_UP);}
        Talk.ask(p,Talk.act(LongerTexts.GO_WITH,r->go(r,true)),Talk.reply(LongerTexts.IN_A_MINUTE,r->go(r,false)));
    }
    private static void go(ServerPlayer p,boolean with){
        var b=base(p);var a=rowan(p);if(b==null||a==null)return;var own=own(p);if(stage(own)!=INVITE_ASK)return;
        if(!with)line(p,a,LongerTexts.UP_THERE);
        stage(p,own,UP);Talk.end(p);var w=work(b);w.via=0;w.leg=-1;a.gesture(LongerRowan.WALK);
    }

    // ------------------------------------------------------------------------------------------------ a hand on Rowan
    static boolean talk(ServerPlayer p,LiteraryActor a,CompoundTag own){
        var b=base(p);if(b==null)return false;int st=stage(own);
        switch(st){
            case NONE->{askOn(p,a,own);return true;}
            case ASK_ON->{Talk.begin(p,LongerRowan.TALK,LongerTexts.ROWAN);line(p,a,LongerTexts.PUT_IT_ON);Talk.end(p);return true;}
            case LEAVE_ASK,PUSHED,REPAIR_ASK,INVITE_ASK->{if(!Talk.resume(p,LongerRowan.TALK)){if(st==LEAVE_ASK)leaveAsk(p,a,own);else if(st==REPAIR_ASK)repair(p,a,own);else if(st==INVITE_ASK)invite(p,a,own);}return true;}
            case PASSING->{repair(p,a,own);return true;}
            case TO_INVITE->{invite(p,a,own);return true;}
            case UP->{return true;}
            case ROOF->{if(LongerRoof.talk(p,a,own))return true;Talk.begin(p,LongerRowan.TALK,LongerTexts.ROWAN);line(p,a,LongerTexts.ROOF_WAIT);Talk.end(p);return true;}
            default->{return false;}
        }
    }

    // ------------------------------------------------------------------------------------------------ for cases
    public static void waited(BlockPos b,long ticks){work(b).at-=ticks;}
    public static int leg(BlockPos b){return work(b).leg;}
}
