package io.github.knaitoe.theoldesthouse.labyrinth;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.643, A sort of thing, part 3 (docs/SLEEPING_KING_0_4_643.md): the two brothers in the wood, grown men alike as two buttons, arm
 * in arm either side of the hedge's gap into his square. This is where it is said: they say what the reader is, and what would happen
 * if he woke. They point the way with one arm each, in opposite directions.
 *
 * <p>Once they have spoken they dress for a battle over the rattle lying spoiled under his tree, and after a while they fight it,
 * beside him, which stirs him twice; then a great shadow goes over the wood and they run. A reader who lifts the rattle first stops the
 * battle: there is nothing left to fight about. Either way the road goes on.
 */
public final class KingBrothers {
    private KingBrothers(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.SLEEPING_KING;
    /** Their stranger phases (literary_king_dum, literary_king_dee). */
    public static final int DUM=218,DEE=219;
    /**
     * Arm in arm (0), pointing the way (1), dressing for battle (2), fighting (3), running (4); and one of them on his own, pointing
     * the way with nobody's shoulders under his other arm (5: the great staircase, StairCompany, 0.4.654).
     */
    public static final int LINKED=0,POINTING=1,DRESSING=2,FIGHTING=3,RUNNING=4,ALONE=5;
    /** The battle: not yet spoken of, waiting for them to be dressed, dressing, fought, the crow, gone; or settled by the rattle. */
    public static final int NONE=0,ARMED=1,DRESS=2,FIGHT=3,CROW=4,GONE=5,SETTLED=6;
    /** How long after they have spoken before they start to dress, how long they dress for, and how long they fight. */
    public static final int WAIT=900,DRESS_TICKS=100,FIGHT_TICKS=80,CROW_TICKS=40;
    /** How far they run in a tick, and how far past the wood's area anybody of theirs is looked for when they are gone. */
    static final double RUN=.9,GONE_REACH=48;
    /**
     * Run 1731: the way they face arm in arm (into the hedge's gap); how far a reader may be from that before the pair shuffles round
     * together, and how far round they shuffle in a tick.
     */
    static final float ROAD=90,LEEWAY=50,SHUFFLE=8;
    static final String TALK="king_brothers";

    public static boolean cast(int phase){return phase==DUM||phase==DEE;}
    public static String skin(int phase){return phase==DUM?"king_dum":"king_dee";}
    public static boolean is(LiteraryActor a){return a.role()==LiteraryActor.STRANGER&&cast(a.phase());}
    static String name(int phase){return phase==DUM?KingTexts.DUM_NAME:KingTexts.DEE_NAME;}
    static BlockPos spot(int phase){return phase==DUM?KingSite.DUM:KingSite.DEE;}
    static AABB area(BlockPos b){return new AABB(b.offset(KingSite.DUM)).inflate(40,6,40);}

    public static @Nullable LiteraryActor find(ServerLevel l,BlockPos b,int phase){
        for(var a:l.getEntitiesOfClass(LiteraryActor.class,area(b),x->x.role()==LiteraryActor.STRANGER&&x.phase()==phase))if(a.isAlive())return a;
        return null;
    }
    /** Both brothers are in the wood unless they are running or have run; nothing is made while the wood's entities are unloaded.
     * Run 1711: while they ran past the edge of {@link #area}, a fresh pair was made at their places and only that pair was taken
     * when they were gone, so one stayed in the wood. */
    static void ensure(ServerLevel l,BlockPos b,KingSleep.Sleep s){
        if(s.battle==CROW||s.battle==GONE)return;
        for(int phase:new int[]{DUM,DEE}){
            var at=b.offset(spot(phase));
            if(!l.isLoaded(at)||!l.areEntitiesLoaded(ChunkPos.asLong(at)))continue;
            if(find(l,b,phase)!=null)continue;
            var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)continue;
            a.bind(PLACE,null);a.appearance(LiteraryActor.STRANGER,phase);
            a.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5,90,0);a.setYBodyRot(90);a.setYHeadRot(90);
            l.addFreshEntity(a);
        }
    }
    /** A fresh dream: both of them back, arm in arm, in their places. */
    static void reset(ServerLevel l,BlockPos b){
        for(int phase:new int[]{DUM,DEE}){var a=find(l,b,phase);if(a==null)continue;var at=b.offset(spot(phase));a.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5,90,0);a.gesture(LINKED);}
    }

    // ------------------------------------------------------------------------------------------------ while anybody is there
    static void tick(ServerLevel l,BlockPos b,KingSleep.Sleep s){
        ensure(l,b,s);
        var dum=find(l,b,DUM);var dee=find(l,b,DEE);long now=l.getGameTime();
        if(s.battle==ARMED&&s.rattleTaken||s.battle==DRESS&&s.rattleTaken){s.battle=SETTLED;for(var p:KingSleep.readers(l,b))King.cue(p,KingTexts.NOTHING_TO_FIGHT);}
        switch(s.battle){
            case ARMED->{if(now>=s.battleAt){s.battle=DRESS;s.battleAt=now+DRESS_TICKS;for(var p:KingSleep.readers(l,b))King.cue(p,KingTexts.DRESSING);}}
            case DRESS->{if(now>=s.battleAt){
                s.battle=FIGHT;s.battleAt=now+FIGHT_TICKS;
                // A battle beside him: he stirs twice.
                KingSleep.stir(l,b,null,null,2);
                // Each reader there remembers it, in their own record as their own tick next reads it (KingSleep.reader).
                for(var p:KingSleep.readers(l,b))s.sawBattle.add(p.getUUID());
            }}
            case FIGHT->{
                if(dum!=null&&dee!=null){dum.swing(InteractionHand.MAIN_HAND);dee.swing(InteractionHand.MAIN_HAND);
                    var mid=dum.position().add(dee.position()).scale(.5);
                    l.playSound(null,mid.x,mid.y+1,mid.z,l.random.nextBoolean()?SoundEvents.SHIELD_BLOCK:SoundEvents.WOOD_HIT,SoundSource.NEUTRAL,.9F,.8F+l.random.nextFloat()*.3F);}
                if(now>=s.battleAt){
                    s.battle=CROW;s.battleAt=now+CROW_TICKS;
                    var over=Vec3.atCenterOf(b.offset(KingSite.TREE)).add(0,12,0);
                    l.playSound(null,over.x,over.y,over.z,KingRegistry.CROW.get(),SoundSource.AMBIENT,2F,1F);
                    for(var p:KingSleep.readers(l,b))King.cue(p,KingTexts.CROW);
                }
            }
            case CROW->{
                // They run for it, east into the trees, and are not seen again in this dream.
                // Short of the edge of the wood's area (36 blocks in CROW_TICKS), so the pair that ran is the pair that goes.
                for(var a:new LiteraryActor[]{dum,dee})if(a!=null){a.moveTo(a.getX()+RUN,a.getY(),a.getZ()+(a==dum?.2:-.2),-90,0);a.setYBodyRot(-90);a.setYHeadRot(-90);}
                if(now>=s.battleAt){s.battle=GONE;for(var a:l.getEntitiesOfClass(LiteraryActor.class,area(b).inflate(GONE_REACH),x->x.role()==LiteraryActor.STRANGER&&cast(x.phase())))a.discard();}
            }
            default->{}
        }
        if(dum==null||dee==null||s.battle==CROW||s.battle==GONE)return;
        int pose=switch(s.battle){case DRESS->DRESSING;case FIGHT->FIGHTING;default->KingSleep.nearest(l,b,dum.position())!=null&&KingSleep.nearest(l,b,dum.position()).distanceTo(dum)<12?POINTING:LINKED;};
        dum.gesture(pose);dee.gesture(pose);
        if(s.battle==FIGHT){KingSleep.face(dum,dee.position());KingSleep.face(dee,dum.position());return;}
        // Arm in arm they face the same way, so the arms stay round each other, and their heads turn to whoever has come. Run 1731: a
        // reader who went round behind them to the King was answered by a head turned as far as a head goes, the other way. Now a reader
        // past LEEWAY of the way they face has the pair shuffle round together, about the middle between their places, until the reader
        // is LEEWAY off; with nobody near, they shuffle back to facing the gap.
        var near=KingSleep.nearest(l,b,dum.position());
        var mid=Vec3.atBottomCenterOf(b.offset(spot(DUM))).add(Vec3.atBottomCenterOf(b.offset(spot(DEE)))).scale(.5);
        float face=dum.getYRot(),turn;
        if(near!=null&&near.distanceTo(dum)<16){
            float want=(float)Math.toDegrees(Math.atan2(near.getZ()-mid.z,near.getX()-mid.x))-90,d=net.minecraft.util.Mth.wrapDegrees(want-face);
            turn=Math.abs(d)<=LEEWAY?0:d-Math.signum(d)*LEEWAY;
        }else turn=net.minecraft.util.Mth.wrapDegrees(ROAD-face);
        face+=net.minecraft.util.Mth.clamp(turn,-SHUFFLE,SHUFFLE);
        for(var a:new LiteraryActor[]{dum,dee}){
            // Tweedledum on the left of the pair as they face.
            double side=a==dum?.5:-.5,rad=Math.toRadians(face);
            if(turn!=0)a.setPos(mid.x+Math.cos(rad)*side,a.getY(),mid.z+Math.sin(rad)*side);
            a.setYRot(face);a.setYBodyRot(face);
            float head=face;
            if(near!=null&&near.distanceTo(a)<16){float to=(float)(Math.toDegrees(Math.atan2(near.getZ()-a.getZ(),near.getX()-a.getX()))-90);head=face+net.minecraft.util.Mth.clamp(net.minecraft.util.Mth.wrapDegrees(to-face),-70,70);}
            a.setYHeadRot(head);
        }
    }

    // ------------------------------------------------------------------------------------------------ a hand on either of them
    public static void talk(ServerPlayer p,LiteraryActor a,CompoundTag own){
        var b=LiteraryVignettes.base(p,PLACE);if(b==null)return;var s=KingSleep.get(b);
        if(s.battle==FIGHT||s.battle==CROW){King.cue(p,KingTexts.TOO_BUSY);return;}
        Talk.begin(p,TALK,KingTexts.BROTHERS);
        if(!own.getBoolean(KingSleep.HEARD)){
            say(p,b,DUM,KingTexts.DUM_DREAMING);
            Talk.ask(p,Talk.reply(KingTexts.R_NOBODY,KingBrothers::sortOfThing),Talk.reply(KingTexts.R_WHAT,KingBrothers::sortOfThing));
            return;
        }
        if(s.rattleTaken){say(p,b,DUM,KingTexts.DUM_GOT_IT);say(p,b,DEE,KingTexts.DEE_GOT_IT);Talk.end(p);return;}
        if(s.battle==DRESS){say(p,b,DUM,KingTexts.DUM_DRESSING);say(p,b,DEE,KingTexts.DEE_DRESSING);Talk.end(p);return;}
        say(p,b,DUM,KingTexts.DUM_AGAIN);say(p,b,DEE,KingTexts.DEE_AGAIN);Talk.end(p);
    }
    /** What the reader is in his dream, and what they would be if he woke. */
    public static void sortOfThing(ServerPlayer p){
        var b=LiteraryVignettes.base(p,PLACE);if(b==null)return;
        say(p,b,DEE,KingTexts.DEE_SORT_OF_THING);say(p,b,DUM,KingTexts.DUM_TREAD);say(p,b,DEE,KingTexts.DEE_NOT_EVEN);
        Talk.ask(p,Talk.reply(KingTexts.R_WHO,KingBrothers::name),Talk.reply(KingTexts.R_QUIET,KingBrothers::name));
    }
    /** His longer name; then the rattle, and the battle there will be over it. */
    public static void name(ServerPlayer p){
        var b=LiteraryVignettes.base(p,PLACE);if(b==null)return;
        say(p,b,DUM,KingTexts.DUM_LONGER_NAME);say(p,b,DEE,KingTexts.DEE_HIS_NAME);
        say(p,b,DUM,KingTexts.DUM_RATTLE);say(p,b,DEE,KingTexts.DEE_RATTLE);say(p,b,DUM,KingTexts.DUM_BATTLE);
        var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),PLACE);own.putBoolean(KingSleep.HEARD,true);LiteraryVignettes.save(d,p.getUUID(),PLACE,own);
        var s=KingSleep.get(b);
        if(s.battle==NONE&&!s.rattleTaken){s.battle=ARMED;s.battleAt=p.serverLevel().getGameTime()+WAIT;}
        Talk.end(p);
    }
    /** Where the battle has got to (NONE to SETTLED). */
    public static int battle(BlockPos b){return KingSleep.get(b).battle;}
    public static boolean rattleTaken(BlockPos b){return KingSleep.get(b).rattleTaken;}
    /** For tests: an armed battle starts dressing now rather than after WAIT. */
    public static void soon(BlockPos b,long now){var s=KingSleep.get(b);if(s.battle==ARMED)s.battleAt=now;}
    static void say(ServerPlayer p,BlockPos b,int phase,String line){
        String who=name(phase);Talk.line(p,who,line);
        var a=find(p.serverLevel(),b,phase);if(a!=null)King.aloud(a,who,line);
    }
}
