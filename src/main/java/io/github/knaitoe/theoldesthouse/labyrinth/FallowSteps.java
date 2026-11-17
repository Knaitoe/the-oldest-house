package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.684, Fallow 5b (docs/FALLOW_0_4_684.md): what the man asks at the cellar doors. Stood there (FallowRoad), he turns to each reader
 * who comes near and asks after the girl, and offers them all a ride in the big man's rig, whose engine turns over though nothing in it
 * should run. Each reader answers for themselves: a lie, the truth or nothing. Then he goes to the top of the steps. If anyone is standing
 * on them he smiles, says he'll find her anyway and walks back up the road. If nobody is, he goes down, and she is not there: she has
 * walked out past the yard's fence into the field, and the wheat comes up round her where she sits; he comes up and stands at the fence,
 * looking out at her, until he is gone. Every reader still at the station when he has gone is ready for the account in the keeper's
 * ledger, under the counter.
 */
public final class FallowSteps {
    private FallowSteps(){}
    public static final String ASK_TALK="fallow_road";
    /** How near a reader comes before he asks them; how long he waits on answers after he first asks before he goes to the steps. */
    public static final double ASK=6;
    public static final int WAIT=400;
    /** At the cellar: stood, asking, to the steps, walking off, gone down, coming up, at the fence, gone. */
    public static final int STOOD=0,ASKING=1,TO_STEPS=2,WALKS_OFF=3,DOWN=4,UP=5,AT_FENCE=6,GONE=7;
    /** Each reader's answer. */
    public static final int LIED=1,TOLD=2,SILENT=3;
    /** How long he stands at the fence looking out at her before he may go; how often the rig's engine is heard. */
    public static final int LOOKS=200,IDLE=40;
    /** Where he stops at the head of the steps; the steps' cells (a body there keeps her). */
    static final Vec3 HEAD=new Vec3(-13.5,0,-6.5);
    /** Where she sits out in the field, past the yard's fence; where he stands at the fence looking at her. */
    static final Vec3 OUT_IN_IT=new Vec3(-32.5,0,-9.5),AT_THE_FENCE=new Vec3(-25.5,0,-9.5);
    static final double WALK=.07;

    /** Whether a reader stands on the cellar's steps or at their head, between the doors. */
    public static boolean onSteps(Vec3 r){return r.x>FallowSite.WELL_W-.05&&r.x<FallowSite.WELL_E+1.05&&r.z>FallowSite.WELL_N-.05&&r.z<FallowSite.WELL_S+2&&r.y>FallowSite.CELLAR_FLOOR-.5&&r.y<.75;}

    static void tick(ServerLevel l,Fallow.Site s,List<ServerPlayer> here){
        var st=s.strike;
        if(st.road!=FallowRoad.STOOD||st.steps==GONE)return;
        var a=Fallow.find(l,s.base,Fallow.STRANGER);if(a==null)return;
        if(st.steps>=ASKING&&st.steps<GONE&&s.tick%IDLE==0)idle(l,s);
        switch(st.steps){
            case STOOD,ASKING->{
                for(var p:here){
                    if(p.isSpectator()||st.asked.contains(p.getUUID())||p.position().distanceToSqr(a.position())>ASK*ASK||!FallowRoad.inView(List.of(p),a.position()))continue;
                    ask(p,s,a);
                }
                if(st.steps==ASKING&&(s.tick-st.stepsAt>=WAIT||everyoneNear(s,a,here)))toSteps(s,a);
            }
            case TO_STEPS->{
                if(Fallow.busy(s,Fallow.STRANGER))return;
                var on=new ArrayList<ServerPlayer>();for(var p:here)if(!p.isSpectator()&&p.isAlive()&&onSteps(FallowStrike.rel(s,p)))on.add(p);
                if(!on.isEmpty()){
                    st.steps=WALKS_OFF;st.stepsAt=s.tick;st.kept=true;
                    for(var p:on){var own=Fallow.own(p);own.putBoolean("OnSteps",true);own.putInt("StepsAfternoon",s.id);Fallow.save(p,own);Fallow.cue(p,FallowTexts.HE_SMILES);}
                    a.gesture(0);FallowBuried.say(a,FallowTexts.STRANGER_LABEL,FallowTexts.FIND_HER_ANYWAY);
                    // Back round the doors and up the road the way he came, as far as the far pad's track.
                    var back=new ArrayList<Vec3>();back.add(new Vec3(-16.5,0,-6.5));for(int i=FallowRoad.DOWN_THE_ROAD.size()-1;i>=0;i--)back.add(FallowRoad.DOWN_THE_ROAD.get(i));
                    Fallow.send(s,Fallow.STRANGER,back);s.speed.put(Fallow.STRANGER,WALK);
                    for(var p:here)if(!on.contains(p))Fallow.cue(p,FallowTexts.WALKS_BACK);
                }else{
                    st.steps=DOWN;st.stepsAt=s.tick;
                    for(var p:here)Fallow.cue(p,FallowTexts.GOES_DOWN);
                    var down=new ArrayList<>(FallowStrike.STEPS);down.add(new Vec3(-13.5,-3,-13.5));
                    Fallow.send(s,Fallow.STRANGER,down);s.speed.put(Fallow.STRANGER,WALK);
                }
            }
            case DOWN->{
                // She is gone out of the cellar the first moment nobody, spectators counted, has her or where she goes in view.
                if(!st.inField)out(l,s);
                if(st.inField&&!Fallow.busy(s,Fallow.STRANGER)){
                    st.steps=UP;st.stepsAt=s.tick;
                    var up=new ArrayList<Vec3>();for(int i=FallowStrike.STEPS.size()-1;i>=0;i--)up.add(FallowStrike.STEPS.get(i));up.add(HEAD);
                    // Round the north end of the trailer to the yard's west fence.
                    up.add(new Vec3(-18.5,0,-6.5));up.add(new Vec3(-18.5,0,-16.5));up.add(new Vec3(-25.5,0,-16.5));up.add(AT_THE_FENCE);
                    Fallow.send(s,Fallow.STRANGER,up);s.speed.put(Fallow.STRANGER,WALK);
                    var taker=st.taker<0?null:Fallow.find(l,s.base,st.taker);
                    if(taker!=null)FallowBuried.say(taker,FallowTexts.NAMES[st.taker],st.taker==Fallow.WOMAN?FallowTexts.FAYE_GONE:FallowTexts.OTIS_GONE);
                }
            }
            case UP->{
                if(Fallow.busy(s,Fallow.STRANGER))return;
                st.steps=AT_FENCE;st.stepsAt=s.tick;
                Fallow.faceTo(a,s.world(OUT_IN_IT));a.gesture(0);
                for(var p:here)Fallow.cue(p,FallowTexts.AT_THE_FENCE);
            }
            case AT_FENCE->{
                if(s.tick-st.stepsAt>=LOOKS&&!FallowRoad.inView(FallowRoad.watchers(l,s),a.position()))gone(l,s,a,here);
            }
            case WALKS_OFF->{
                // Back up the road and into the ash: gone the first moment nobody sees him, once he is past the road.
                if(a.position().z<s.base.getZ()+FallowSite.ROAD_N-1&&!FallowRoad.inView(FallowRoad.watchers(l,s),a.position())||!Fallow.busy(s,Fallow.STRANGER)&&!FallowRoad.inView(FallowRoad.watchers(l,s),a.position()))gone(l,s,a,here);
            }
            default->{}
        }
    }
    /** He turns to a reader and asks after her, and offers the ride; each reader is asked once. */
    static void ask(ServerPlayer p,Fallow.Site s,LiteraryActor a){
        var st=s.strike;
        st.asked.add(p.getUUID());
        if(st.steps==STOOD){st.steps=ASKING;st.stepsAt=s.tick;}
        Fallow.faceTo(a,p.position());
        a.sayTo(p,FallowTexts.STRANGER_LABEL,FallowTexts.WHERE_IS_SHE);a.speaks(FallowTexts.WHERE_IS_SHE);
        if(!st.rig){st.rig=true;for(var q:Fallow.readers(p.serverLevel(),s.base))Fallow.cue(q,FallowTexts.RIG_TURNS);}
        Talk.begin(p,ASK_TALK,FallowTexts.STRANGER_LABEL);
        Talk.line(p,FallowTexts.STRANGER_LABEL,FallowTexts.WHERE_IS_SHE);Talk.line(p,FallowTexts.STRANGER_LABEL,FallowTexts.RIDE);
        Talk.ask(p,Talk.reply(FallowTexts.NOBODY_DOWN_THERE,q->answer(q,s,LIED)),Talk.reply(FallowTexts.DOWN_THE_STEPS,q->answer(q,s,TOLD)),
            Talk.act(FallowTexts.SAY_NOTHING,q->answer(q,s,SILENT)));
    }
    static void answer(ServerPlayer p,Fallow.Site s,int answer){
        var st=s.strike;if(st.steps>ASKING)return;
        var own=Fallow.own(p);if(own.getInt("RoadAnswer")==0||own.getInt("RoadAfternoon")!=s.id){own.putInt("RoadAnswer",answer);own.putInt("RoadAfternoon",s.id);}Fallow.save(p,own);
        st.answered.add(p.getUUID());
        var a=Fallow.find(p.serverLevel(),s.base,Fallow.STRANGER);
        String line=switch(answer){case LIED->FallowTexts.IS_THAT_RIGHT;case TOLD->FallowTexts.OBLIGED;default->null;};
        if(line!=null){Talk.line(p,FallowTexts.STRANGER_LABEL,line);if(a!=null){a.sayTo(p,FallowTexts.STRANGER_LABEL,line);a.speaks(line);}}
        else Fallow.cue(p,FallowTexts.LOOKS_PAST);
        Talk.end(p);
    }
    /** Everyone within reach of him has been asked and has answered. */
    static boolean everyoneNear(Fallow.Site s,LiteraryActor a,List<ServerPlayer> here){
        boolean any=false;
        for(var p:here){if(p.isSpectator()||p.position().distanceToSqr(a.position())>ASK*ASK*4)continue;any=true;if(!s.strike.answered.contains(p.getUUID()))return false;}
        return any;
    }
    static void toSteps(Fallow.Site s,LiteraryActor a){
        var st=s.strike;st.steps=TO_STEPS;st.stepsAt=s.tick;
        for(var p:((ServerLevel)a.level()).players())if(Talk.waiting(p,ASK_TALK))Talk.close(p);
        Fallow.send(s,Fallow.STRANGER,List.of(new Vec3(-16.5,0,-6.5),HEAD));s.speed.put(Fallow.STRANGER,WALK);
    }
    /** The girl out of the cellar, unseen, and sat down in the field past the fence; the wheat comes up round her there (FallowRoad.green). */
    static void out(ServerLevel l,Fallow.Site s){
        var st=s.strike;
        var girl=Fallow.find(l,s.base,Fallow.GIRL);if(girl==null){st.inField=true;return;}
        var watchers=FallowRoad.watchers(l,s);var to=s.world(OUT_IN_IT);
        if(FallowRoad.inView(watchers,girl.position())||FallowRoad.inView(watchers,to)||!l.isLoaded(BlockPos.containing(to)))return;
        s.routes.remove(Fallow.GIRL);s.away.add(Fallow.GIRL);
        girl.moveTo(to.x,to.y,to.z,90,0);girl.setYBodyRot(90);girl.setYHeadRot(90);girl.gesture(Fallow.SIT);
        st.inField=true;st.greenAt=BlockPos.containing(OUT_IN_IT);st.ring=0;st.plantedAt=s.tick;
    }
    /** The rig at the pumps idling, its stack smoking, while he is here. */
    static void idle(ServerLevel l,Fallow.Site s){
        var at=s.world(new Vec3(FallowSite.RIG_FRONT+2.5,1,FallowSite.RIG_Z+.5));
        l.playSound(null,at.x,at.y,at.z,FallowRegistry.RIG.get(),SoundSource.NEUTRAL,.7F,.95F+l.getRandom().nextFloat()*.1F);
        // Its two stacks stand up either side of the cab behind the doors (FallowSite.rig, slice 5).
        for(double z:new double[]{FallowSite.RIG_Z+.88,FallowSite.RIG_Z-.88}){
            var stack=s.world(new Vec3(FallowSite.RIG_FRONT+2.1,2.95,z));
            l.sendParticles(ParticleTypes.LARGE_SMOKE,stack.x,stack.y,stack.z,2,.03,.1,.03,.015);
        }
    }
    /** He is gone. Every reader still at the station is ready for the account. */
    static void gone(ServerLevel l,Fallow.Site s,LiteraryActor a,List<ServerPlayer> here){
        var st=s.strike;
        st.steps=GONE;st.stepsAt=s.tick;a.discard();s.routes.remove(Fallow.STRANGER);
        for(var p:here){
            var own=Fallow.own(p);
            // Only a reader this afternoon's light found here, out in it or below (Fallow.dealable keeps the rest away); a spectator earns
            // nothing.
            if(p.isSpectator()||own.getInt("Afternoon")!=s.id)continue;
            own.putBoolean(st.kept?"KeptHer":"SawHerOut",true);own.putInt("RoadEnd",st.kept?1:2);
            int answer=own.getInt("RoadAfternoon")==s.id?own.getInt("RoadAnswer"):0;
            boolean stood=own.getBoolean("OnSteps")&&own.getInt("StepsAfternoon")==s.id;if(!stood)own.remove("OnSteps");
            String outcome=stood&&st.kept?"kept_her_on_the_steps":answer==TOLD?"told_him_where_she_was":st.kept?"stayed_until_he_had_gone":"let_him_look";
            LiteraryVignettes.ready(p,Fallow.PLACE,own,outcome);Fallow.save(p,own);
            Fallow.cue(p,st.kept?FallowTexts.ROAD_EMPTY:FallowTexts.FENCE_EMPTY);
        }
    }
    /** Spoken to while he waits: he asks again if he has not asked them; otherwise he looks past them. */
    static boolean talk(ServerPlayer p,Fallow.Site s,LiteraryActor a){
        var st=s.strike;
        if(st.road!=FallowRoad.STOOD)return false;
        // Gone to the steps, or past them, he does not stop for anyone.
        if(st.steps>ASKING)return true;
        if(!st.asked.contains(p.getUUID())){ask(p,s,a);return true;}
        return Talk.resume(p,ASK_TALK);
    }
    /** A word with the girl out in the field. */
    static boolean opal(ServerPlayer p,Fallow.Site s,LiteraryActor girl){
        if(!s.strike.inField||s.strike.kept)return false;
        Fallow.faceTo(girl,p.position());FallowBuried.say(girl,"Opal",FallowTexts.CAME_UP_FOR_ME);
        return true;
    }
    // ------------------------------------------------------------------------------------------------ for the tests
    public static int steps(BlockPos b){var s=Fallow.afternoon(b);return s==null?-1:s.strike.steps;}
    public static boolean kept(BlockPos b){var s=Fallow.afternoon(b);return s!=null&&s.strike.kept;}
    public static boolean inField(BlockPos b){var s=Fallow.afternoon(b);return s!=null&&s.strike.inField;}
    public static boolean walking(BlockPos b,int who){var s=Fallow.afternoon(b);return s!=null&&Fallow.busy(s,who);}
}
