package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.570, the afternoon and the evening (docs/PELL_HOUSE_NOTES_0_4_570.md). The owner, playing the house: "the NPCs should also have
 * more idle behavior in Pell house, as they do on ghosts set".
 *
 * <p>While the house waits on its hour (the afternoon before June calls them to dinner, and the evening while she watches her tape),
 * June, Tilly and Callum each go about small things of their own, one at a time, a while apart, through the house's own collision,
 * as the Ghosts set's family does between takes (GhostsErrands):
 *
 * <ul>
 *   <li><b>June</b>, in the afternoon: she stirs the pot at the stove, runs the tap at the sink, wipes down the end of the table. In the
 *       evening she gets up from her chair once in a while to put the kettle on, and goes back to it.</li>
 *   <li><b>Tilly</b>, in Nessa's room: she goes with her cane to the desk under the window and feels along it, sits on the edge of the
 *       bed, finds the height marks by the door with her fingers, puts her hand to the wardrobe.</li>
 *   <li><b>Callum</b>, at the end of the hall: he wrings the wet out of his jumper, goes along to the school photographs and looks up at
 *       them, sits down against the wall.</li>
 * </ul>
 *
 * Nothing here touches what the visit does. When the hour comes (six, or half past eight) whoever is out on something goes straight back
 * to where the day put them, and June's call and her bedtime wait for them (home). A week in, June's own come only after her hand.
 * Not on the last night. A hand on any of them still gets their lines; a reader who comes close is noticed. Each house's are its own.
 */
public final class PellIdle {
    private PellIdle(){}
    /** The windows: none, the afternoon (settled, before six), the evening (the tape, before half past eight). */
    static final int NONE=0,AFTERNOON=1,EVENING=2;
    /** Each one's first errand comes this long after the window opens; the next this long after their last ends. June, Tilly, Callum. */
    static final int[] FIRST={200,260,330},BETWEEN={260,300,360};
    /** In the evening June gets up for the kettle no more often than this. */
    static final int KETTLE_EVERY=900;
    /** Where an errand is: not out, on the way, at it, on the way back. */
    public static final int OFF=0,OUT=1,AT=2,BACK=3;
    /** A reader nearer than this to someone at an errand is noticed: June and Tilly turn their heads to them, Callum turns round. */
    static final double NOTICE=3.5;

    /**
     * One small thing: whose it is; the way there (none: where they stand); which way they face at it, what they do (a gesture of
     * PellPeople's, or a perch to sit on), for how long, with what sound how often (0: once as it starts); where their head looks (its
     * pitch; and whether it is on the work and not on whoever is near); a line said at it once a visit, if anyone is near enough to hear.
     */
    record Errand(int who,Vec3[] out,float face,int gesture,@Nullable PellVisit.Perch perch,int ticks,@Nullable Supplier<SoundEvent> sound,int every,
                  float pitch,boolean absorbed,@Nullable String line,double hear){}
    private static Vec3 v(double x,double z){return new Vec3(x,0,z);}
    private static final Vec3[] HERE={};
    static final Errand STIR=new Errand(PellPeople.JUNE,HERE,180,PellPeople.STIR,null,140,PellRegistry.STIR,40,30,true,PellTexts.JUNE_STIRS,PellVisit.HEAR),
        SINK=new Errand(PellPeople.JUNE,new Vec3[]{v(PellVisit.SINK.x(),PellVisit.SINK.z())},PellVisit.SINK.look(),PellPeople.WASH,null,100,PellRegistry.TAP,0,35,true,PellTexts.JUNE_TAP,PellVisit.HEAR),
        WIPE=new Errand(PellPeople.JUNE,new Vec3[]{v(PellVisit.TABLE_END.x(),PellVisit.TABLE_END.z())},PellVisit.TABLE_END.look(),PellPeople.WIPE,null,110,PellRegistry.WIPE,30,32,true,PellTexts.JUNE_WIPES,PellVisit.HEAR),
        // From her chair, through the hall, to the stove; back the same way to the chair's side, where the tape sits her down again.
        KETTLE=new Errand(PellPeople.JUNE,new Vec3[]{v(-2.6,-68.6),v(0,-68.5),v(0,-65.6),v(-9.5,-65.6),v(-9.5,-68.5),v(PellVisit.KITCHEN.x(),PellVisit.KITCHEN.z())},
            PellVisit.KITCHEN.look(),PellPeople.WASH,null,160,PellRegistry.KETTLE,0,25,true,PellTexts.JUNE_KETTLE,PellVisit.HEAR),
        DESK=new Errand(PellPeople.TILLY,new Vec3[]{v(-10.5,-59.7)},0,PellPeople.FEEL,null,110,null,0,18,true,PellTexts.TILLY_DESK,5),
        BED=new Errand(PellPeople.TILLY,new Vec3[]{v(-10.6,-59.7),PellVisit.TILLY_BED.beside()},PellVisit.TILLY_BED.look(),PellPeople.CANE,PellVisit.TILLY_BED,220,null,0,0,false,null,0),
        MARKS=new Errand(PellPeople.TILLY,new Vec3[]{v(-10.5,-61.75)},180,PellPeople.MEASURE,null,100,null,0,-12,true,PellTexts.TILLY_MARKS,5),
        WARDROBE=new Errand(PellPeople.TILLY,new Vec3[]{v(-9.5,-60.2),v(-8.6,-58.5)},-90,PellPeople.FEEL,null,90,null,0,10,true,PellTexts.TILLY_WARDROBE,5),
        WRING=new Errand(PellPeople.CALLUM,HERE,PellPeople.FIRST_VISIT[PellPeople.CALLUM].look(),PellPeople.WRING,null,70,PellRegistry.DRIP,14,40,true,null,0),
        PHOTOS=new Errand(PellPeople.CALLUM,new Vec3[]{v(-10.5,-65.5),v(-6.5,-65.6)},180,0,null,150,null,0,-28,true,null,0),
        FLOOR=new Errand(PellPeople.CALLUM,HERE,PellPeople.FIRST_VISIT[PellPeople.CALLUM].look(),PellPeople.SITTING,null,280,null,0,0,true,null,0);
    /** What each does, in turn, by day and in the evening. */
    static final List<Errand> JUNE_DAY=List.of(STIR,SINK,STIR,WIPE),JUNE_EVENING=List.of(KETTLE),
        TILLY=List.of(DESK,BED,MARKS,WARDROBE),CALLUM=List.of(WRING,PHOTOS,FLOOR);
    static final List<Errand> ALL=List.of(STIR,SINK,WIPE,KETTLE,DESK,BED,MARKS,WARDROBE,WRING,PHOTOS,FLOOR);

    /** Where each stands when not out on anything, in this window: June at the stove (or by her chair in the evening), Tilly at the foot
     *  of Nessa's bed, Callum at the far end of the hall. */
    static Vec3 home(int who,int window){
        return switch(who){
            case PellPeople.JUNE->window==EVENING?PellVisit.RECLINER.beside():v(PellVisit.KITCHEN.x(),PellVisit.KITCHEN.z());
            case PellPeople.TILLY->v(PellVisit.TILLY_ROOM.x(),PellVisit.TILLY_ROOM.z());
            default->v(PellPeople.FIRST_VISIT[PellPeople.CALLUM].x(),PellPeople.FIRST_VISIT[PellPeople.CALLUM].z());
        };
    }
    static float homeLook(int who,int window){
        return switch(who){
            case PellPeople.JUNE->window==EVENING?PellVisit.RECLINER.look():PellVisit.KITCHEN.look();
            case PellPeople.TILLY->PellVisit.TILLY_ROOM.look();
            default->PellPeople.FIRST_VISIT[PellPeople.CALLUM].look();
        };
    }

    /** Nobody is out on anything: what the visit does next may go ahead. */
    static boolean home(PellPeople.Household h){for(int who=0;who<3;who++)if(h.idleState[who]!=OFF)return false;return true;}
    /** The house's errands forgotten, as the visit begins again. */
    static void reset(PellPeople.Household h){
        for(int who=0;who<3;who++){h.idleState[who]=OFF;h.idleErrand[who]=null;h.idleNext[who]=0;h.idleTurn[who]=0;}
        h.idleWindow=NONE;h.idleSaid.clear();
    }

    /** Which window the house is in, and whether its hour has come (the window is over but somebody may still be out). */
    static int window(PellPeople.Household h){
        if(h.visit>=PellWeek.LAST)return NONE;
        if(h.step==PellVisit.SETTLED&&(h.visit<2||h.welcomed))return AFTERNOON;
        if(h.step==PellVisit.TELLY)return EVENING;
        return NONE;
    }
    static boolean due(PellPeople.Household h,int window){return window==AFTERNOON?h.time>=PellVisit.SIX:window==EVENING&&h.time>=PellVisit.EVENING;}

    /** Once a tick for the house, after the visit has walked its walkers. */
    static void tick(ServerLevel l,BlockPos b,PellPeople.Household h,List<ServerPlayer> here,long now){
        int window=window(h);
        if(window!=h.idleWindow){
            // Into a new window: each one's first errand a while off. Out of one by any other way (the visit moved on): anybody still out
            // is left to the visit, which sends them on from where they are.
            if(window==NONE)for(int who=0;who<3;who++){var a=PellPeople.find(l,b,who);if(h.idleState[who]!=OFF&&a!=null)settle(l,b,h,who,a);h.idleState[who]=OFF;h.idleErrand[who]=null;}
            for(int who=0;who<3;who++)h.idleNext[who]=now+FIRST[who];
            h.idleWindow=window;
        }
        if(window==NONE)return;
        boolean due=due(h,window);
        for(int who=0;who<3;who++){
            var a=PellPeople.find(l,b,who);if(a==null)continue;
            // A reader as Tilly does what they do; Tilly is theirs.
            if(who==PellPeople.TILLY&&h.tillyReader!=null){h.idleState[who]=OFF;continue;}
            switch(h.idleState[who]){
                case OFF->{if(!due&&now>=h.idleNext[who])begin(l,b,h,who,a,window,now);}
                case OUT->{if(due){recall(l,b,h,who,a,window);break;}if(h.way[who].isEmpty())arrive(l,b,h,who,a,now);else taps(l,h,who,a,now);}
                case AT->{if(due||now-h.idleAt[who]>=h.idleErrand[who].ticks())recall(l,b,h,who,a,window);else work(l,b,h,who,a,here,now);}
                case BACK->{
                    if(h.way[who].isEmpty()){
                        h.idleState[who]=OFF;h.idleErrand[who]=null;h.rest[who]=homeLook(who,window);
                        h.idleNext[who]=now+(who==PellPeople.JUNE&&window==EVENING?KETTLE_EVERY:BETWEEN[who]);
                    }else taps(l,h,who,a,now);
                }
                default->{}
            }
        }
        // Callum's hair and shoulders drip wherever he stands still.
        var callum=PellPeople.find(l,b,PellPeople.CALLUM);
        if(callum!=null&&h.way[PellPeople.CALLUM].isEmpty()&&(now+callum.getId())%37==0)drip(l,callum,1);
    }

    /** Off to the next of their errands, if they are where the day put them and free. */
    private static void begin(ServerLevel l,BlockPos b,PellPeople.Household h,int who,LiteraryActor a,int window,long now){
        if(!h.way[who].isEmpty()||h.seated[who]||h.asleep[who]||a.saying())return;
        List<Errand> list;
        switch(who){
            case PellPeople.JUNE->{
                if(window==EVENING){if(h.perch[who]!=PellVisit.RECLINER)return;list=JUNE_EVENING;}
                else{
                    // A week in, not until her hand (PellHand); and only from the stove.
                    if(h.visit>=2&&h.hand<PellHand.DONE)return;
                    if(!PellVisit.near(a,b,home(who,window),.6))return;list=JUNE_DAY;
                }
            }
            case PellPeople.TILLY->{if(h.perch[who]!=null||!PellVisit.near(a,b,home(who,window),.8))return;list=TILLY;}
            default->{if(!PellVisit.near(a,b,home(who,window),.8))return;list=CALLUM;}
        }
        var e=list.get(h.idleTurn[who]%list.size());h.idleTurn[who]++;
        h.idleErrand[who]=e;
        if(who==PellPeople.JUNE&&window==EVENING)PellVisit.unperch(b,h,who,a);
        if(e.out().length==0){arrive(l,b,h,who,a,now);return;}
        if(e.perch()!=null)PellVisit.goPerch(h,who,e.perch(),e.out());else PellVisit.go(h,who,-1,e.face(),e.out());
        h.idleState[who]=OUT;
    }
    /** At it: what they do, the sound of it, and the line said there once a visit. */
    private static void arrive(ServerLevel l,BlockPos b,PellPeople.Household h,int who,LiteraryActor a,long now){
        var e=h.idleErrand[who];h.idleState[who]=AT;h.idleAt[who]=now;
        h.rest[who]=e.face();
        if(e.perch()==null){a.setYRot(e.face());a.setYBodyRot(e.face());}
        a.gesture(e.gesture());
        if(e.sound()!=null&&e.every()==0)l.playSound(null,a.getX(),a.getY()+1,a.getZ(),e.sound().get(),SoundSource.NEUTRAL,.7F,1F);
    }
    /** At it, a tick: the head on the work unless a reader is close; the sound again; the line; Callum's drips. */
    private static void work(ServerLevel l,BlockPos b,PellPeople.Household h,int who,LiteraryActor a,List<ServerPlayer> here,long now){
        var e=h.idleErrand[who];long at=now-h.idleAt[who];
        ServerPlayer near=nearest(a,here,who==PellPeople.CALLUM&&e==FLOOR?PellPeople.WATCH:NOTICE);
        if(who==PellPeople.CALLUM&&e==FLOOR){
            // Sitting against the wall, he does not turn round; his head keeps whoever is nearest in sight.
            h.moving[who]=true;
            float toward=near==null?e.face():PellPeople.yawTo(a,near.getEyePosition());
            float want=a.getYRot()+Mth.clamp(Mth.wrapDegrees(toward-a.getYRot()),-PellPeople.HEAD_TURN,PellPeople.HEAD_TURN);
            a.setYHeadRot(Mth.approachDegrees(a.getYHeadRot(),want,8));a.setXRot(0);
        }else if(e.absorbed()&&near==null){
            h.moving[who]=true;a.setYHeadRot(Mth.approachDegrees(a.getYHeadRot(),a.getYRot(),10));a.setXRot(Mth.approach(a.getXRot(),e.pitch(),3));
        }else a.setXRot(Mth.approach(a.getXRot(),0,4));
        if(e.sound()!=null&&e.every()>0&&at%e.every()==5)l.playSound(null,a.getX(),a.getY()+1,a.getZ(),e.sound().get(),SoundSource.NEUTRAL,.55F,.95F+((now/e.every())%3)*.05F);
        if(e==WRING&&at%5==0)drip(l,a,3);
        if(e.line()!=null&&at==30&&!h.idleSaid.contains(e)&&PellVisit.ready(h,now,a)&&nearest(a,here,e.hear())!=null){
            h.idleSaid.add(e);PellVisit.say(h,a,who,e.line(),now);
        }
    }
    /** Back to where the day put them: what they were doing put down, off the bed, and the way back. */
    private static void recall(ServerLevel l,BlockPos b,PellPeople.Household h,int who,LiteraryActor a,int window){
        var e=h.idleErrand[who];
        settle(l,b,h,who,a);
        if(e==null||e.out().length==0&&PellVisit.near(a,b,home(who,window),.6)){h.idleState[who]=BACK;h.way[who].clear();return;}
        var back=new java.util.ArrayList<Vec3>();var out=e.out();
        for(int i=out.length-2;i>=0;i--)back.add(out[i]);
        back.add(home(who,window));
        PellVisit.go(h,who,-1,homeLook(who,window),back.toArray(Vec3[]::new));
        h.idleState[who]=BACK;
    }
    /** Whatever they had in hand or were doing put down: their day's gesture again, their head up, off the bed. */
    private static void settle(ServerLevel l,BlockPos b,PellPeople.Household h,int who,LiteraryActor a){
        if(who==PellPeople.TILLY&&h.perch[who]==PellVisit.TILLY_BED)PellVisit.unperch(b,h,who,a);
        PellVisit.dress(a,who);a.setXRot(0);
    }
    /** Tilly's cane on the floor as she goes. */
    private static void taps(ServerLevel l,PellPeople.Household h,int who,LiteraryActor a,long now){
        if(who==PellPeople.TILLY&&(now+a.getId())%14==0)l.playSound(null,a.getX(),a.getY(),a.getZ(),PellRegistry.CANE_TAP.get(),SoundSource.NEUTRAL,.3F,1.1F);
    }
    /** Water off him: a few drops from his jumper's hem, or (standing) one from his shoulder. */
    private static void drip(ServerLevel l,LiteraryActor a,int n){
        double y=a.getY()+(n>1?.42:.62);
        l.sendParticles(ParticleTypes.DRIPPING_WATER,a.getX(),y,a.getZ(),n,.12,.03,.12,0);
        if(n>1)l.sendParticles(ParticleTypes.FALLING_WATER,a.getX(),y-.1,a.getZ(),1,.08,0,.08,0);
    }
    private static @Nullable ServerPlayer nearest(LiteraryActor a,List<ServerPlayer> here,double reach){
        ServerPlayer near=null;double best=reach*reach;
        for(var p:here){if(p.isSpectator()||!p.isAlive()||p.isSleeping())continue;double d=a.distanceToSqr(p);if(d<best){best=d;near=p;}}
        return near;
    }

    // ------------------------------------------------------------------------------------------------ for the cases
    /** What one of them is out on, and where in it (OFF to BACK). */
    public static @Nullable String errand(BlockPos b,int who){var e=PellPeople.household(b).idleErrand[who];return e==null?null:name(e);}
    public static int state(BlockPos b,int who){return PellPeople.household(b).idleState[who];}
    public static String name(Errand e){
        return e==STIR?"stir":e==SINK?"sink":e==WIPE?"wipe":e==KETTLE?"kettle":e==DESK?"desk":e==BED?"bed":e==MARKS?"marks":e==WARDROBE?"wardrobe":e==WRING?"wring":e==PHOTOS?"photos":"floor";
    }
    /** The next errand of each comes now. */
    public static void soon(BlockPos b){var h=PellPeople.household(b);for(int who=0;who<3;who++)h.idleNext[who]=0;}
}
