package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.601, taking Tilly round the house by the arm (docs/PELL_HOUSE_NOTES_0_4_601.md; put by in 0.4.571). The owner, playing the
 * house alone: "ways to interact with tilly, for singleplayer characters".
 *
 * <p>With Tilly one of the house's people, in Nessa's room, while the house waits on its hour (the afternoon and the evening of the
 * first two visits): to her asking what things look like, a reader can answer "Come on. Take my arm and I'll show you." She takes it,
 * her cane in her other hand, and goes where the reader goes, a step behind: through the house and anywhere on June's lot. She stops
 * at the lot's edge, and she does not go into the pool. Whatever of the house's things the reader puts a hand on with her beside them,
 * she puts her own hand on; of some she has a word, each once a visit, by touch and smell and what she hears (PellTexts.arm).
 *
 * <p>It ends with a hand on her again, or when the reader goes off without her, or when the hour comes: she lets go and goes back to
 * her room the way she came, by herself, her cane ahead of her. June's call and her bedtime wait until she is there (PellIdle.home),
 * and a walk begun late has a little while before the hour ends it (SHORTEST). One reader has her arm at a time; each house's Tilly is
 * its own; with a reader as Tilly there is nobody to lead. Nothing here changes what the visit does.
 */
public final class PellArm {
    private PellArm(){}
    /** Where the walk is: not on; on a reader's arm; on her way back to her room. */
    public static final int OFF=0,LED=1,BACK=2;
    /** How near she keeps; how fast she comes after the arm; how far off its reader can go, and for how long, before she lets go; how
     *  near she must be for her hand to follow theirs; how long her hand stays on a thing. */
    public static final double BESIDE=1.5,PACE=.14,LET_GO=7,WITH=3.5;
    public static final int FAR_FOR=60,TOUCH=50;
    /** A walk lasts at least this long, though the hour has come. */
    public static final int SHORTEST=800;
    /** How far apart the points of the way she has come are kept, and how many at most. */
    static final double STEP=.7;static final int TRAIL=400;
    /** In a reader's record: that they took her round, and how many things she put her hand on with them. */
    public static final String WALKED="WalkedTilly0601",SHOWN="ShownTilly0601";
    private static final int T=PellPeople.TILLY;

    /** Whether a reader could take her arm now: Tilly in Nessa's room with the house waiting, nobody reading to her, nobody with her arm. */
    static boolean available(ServerLevel l,BlockPos b,PellPeople.Household h){
        return h.arm==OFF&&h.readingTo==null&&h.visit<PellWeek.LAST&&PellCompany.free(l,b,h)!=null;
    }
    private static Vec3 rel(BlockPos b,Vec3 at){return at.subtract(b.getX(),b.getY(),b.getZ());}
    private static @Nullable ServerPlayer leader(ServerLevel l,PellPeople.Household h){
        if(h.armWith==null)return null;var p=l.getServer().getPlayerList().getPlayer(h.armWith);
        return p!=null&&p.level()==l&&p.isAlive()&&!p.isSpectator()?p:null;
    }
    /** Where she will not go: off June's lot, or into the pool's water. */
    private static boolean barred(BlockPos b,Vec3 at){var r=rel(b,at);return !PellSite.onLot(r)||PellSite.inWater(new Vec3(r.x,-.5,r.z));}

    // ------------------------------------------------------------------------------------------------ taking it, and letting go
    /** The reader's answer on the panel (PellCompany.offer): she takes their arm. */
    static void take(ServerPlayer p){
        var b=LiteraryVignettes.base(p,PellHouse.PLACE);if(b==null){Talk.close(p);return;}
        var h=PellPeople.household(b);var l=p.serverLevel();long now=l.getGameTime();
        if(!available(l,b,h)){Talk.end(p);return;}
        var tilly=PellPeople.find(l,b,T);if(tilly==null){Talk.end(p);return;}
        // Whatever she was at in her room is put down.
        PellIdle.drop(l,b,h,T,tilly);
        h.arm=LED;h.armWith=p.getUUID();h.armAt=now;h.armFar=0;h.armTouch=0;h.armEdge=0;h.armTrail.clear();h.armTrail.add(rel(b,tilly.position()));
        tilly.gesture(PellPeople.ARM);h.rest[T]=tilly.getYRot();
        // What she says on taking it, once in each of the house's waits; after that she only takes it.
        int window=PellIdle.window(h);
        var own=PellHouse.own(p);own.putBoolean(WALKED,true);PellHouse.save(p,own);
        // With her word, the exchange ends on it; with none, there is nothing left to read and it closes.
        if(h.armWindow!=window){h.armWindow=window;String line=h.visit>=2?PellTexts.TILLY_ARM_WEEK:PellTexts.TILLY_ARM;Talk.line(p,PellCompany.WHO,line);PellVisit.say(h,tilly,T,line,now);Talk.end(p);}
        else{PellHouse.cue(p,PellTexts.ARM_TAKEN);Talk.close(p);}
    }
    /**
     * A hand on Tilly while she is on somebody's arm (PellPeople.talk): its own reader's lets her go, and she goes back; another's is
     * told whose she has. True if it answered.
     */
    static boolean hand(ServerPlayer p,BlockPos b,PellPeople.Household h,LiteraryActor tilly,CompoundTag own){
        if(h.arm!=LED)return false;
        if(!p.getUUID().equals(h.armWith)){PellHouse.cue(p,PellTexts.ARM_SOMEBODYS);return true;}
        PellHouse.cue(p,PellTexts.ARM_LET_GO);
        home(p.serverLevel(),b,h,tilly);
        return true;
    }
    /** She lets go and goes back to her room the way she came, her cane ahead of her. */
    private static void home(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor tilly){
        var way=new ArrayList<Vec3>();
        for(int i=h.armTrail.size()-1;i>=0;i--)way.add(new Vec3(h.armTrail.get(i).x,0,h.armTrail.get(i).z));
        way.add(new Vec3(PellVisit.TILLY_ROOM.x(),0,PellVisit.TILLY_ROOM.z()));
        // Whoever had her arm is asked again by their next hand on her, this wait: the question, and the arm, come up again.
        if(h.armWith!=null)h.askedIn.remove(h.armWith);
        h.arm=BACK;h.armWith=null;h.armTrail.clear();
        PellVisit.dress(tilly,T);tilly.setXRot(0);
        PellVisit.go(h,T,-1,PellVisit.TILLY_ROOM.look(),way.toArray(Vec3[]::new));
    }

    // ------------------------------------------------------------------------------------------------ every tick
    /** Once a tick for the house, after the visit has walked its walkers. */
    static void tick(ServerLevel l,BlockPos b,PellPeople.Household h,List<ServerPlayer> here,long now){
        if(h.arm==OFF)return;
        var tilly=PellPeople.find(l,b,T);
        if(tilly==null||h.tillyReader!=null||h.asleep[T]||h.seated[T]){h.arm=OFF;h.armWith=null;h.armTrail.clear();return;}
        if(h.arm==BACK){
            // On her way back: she opens what is shut in front of her, and is home when her way is walked.
            for(var door:PellVisit.LOCKS)opened(l,b,tilly,door);
            opened(l,b,tilly,PellSite.GARAGE_DOOR);
            if(h.way[T].isEmpty()){h.arm=OFF;h.rest[T]=PellVisit.TILLY_ROOM.look();h.idleNext[T]=now+PellIdle.BETWEEN[T];}
            else if((now+tilly.getId())%14==0)tap(l,tilly);
            return;
        }
        var p=leader(l,h);
        int window=PellIdle.window(h);
        boolean over=window==PellIdle.NONE||PellIdle.due(h,window)&&now-h.armAt>=SHORTEST;
        // Her reader gone, or gone off without her for a while: she lets go.
        boolean far=p==null||!here.contains(p)||p.distanceTo(tilly)>LET_GO;
        if(!far)h.armFar=0;else if(h.armFar==0)h.armFar=now;
        if(over||p==null||!here.contains(p)||far&&now-h.armFar>=FAR_FOR){
            if(p!=null&&here.contains(p))PellHouse.cue(p,over?PellTexts.ARM_HOUR:PellTexts.ARM_LEFT);
            if(over&&p!=null&&here.contains(p)&&p.distanceTo(tilly)<=LET_GO&&PellVisit.ready(h,now,tilly))PellVisit.say(h,tilly,T,PellTexts.TILLY_ARM_HOUR,now);
            home(l,b,h,tilly);return;
        }
        h.moving[T]=true;
        boolean touching=now<h.armTouch;
        if(tilly.gesture()!=(touching?PellPeople.FEEL:PellPeople.ARM))tilly.gesture(touching?PellPeople.FEEL:PellPeople.ARM);
        var to=p.position();double dx=to.x-tilly.getX(),dz=to.z-tilly.getZ(),d=Math.sqrt(dx*dx+dz*dz);
        if(touching||d<=BESIDE){
            // Beside them: she stands, turned the way she was going, her head to whoever speaks.
            if(!touching){float y=Mth.approachDegrees(tilly.getYRot(),PellPeople.yawTo(tilly,to),6);tilly.setYRot(y);tilly.setYBodyRot(y);tilly.setYHeadRot(y);}
            h.rest[T]=tilly.getYRot();return;
        }
        var before=tilly.position();
        SceneFootpaths.walk(tilly,to,PACE);
        if(barred(b,tilly.position())){
            // The lot's edge, or the water: she stops where she was, and keeps hold.
            tilly.moveTo(before.x,before.y,before.z,tilly.getYRot(),0);tilly.setDeltaMovement(Vec3.ZERO);
            if(now-h.armEdge>=200){h.armEdge=now;PellHouse.cue(p,PellTexts.ARM_EDGE);}
        }
        h.rest[T]=tilly.getYRot();
        if(tilly.position().distanceToSqr(before)>1e-5){
            if((now+tilly.getId())%14==0)tap(l,tilly);
            trail(h,rel(b,tilly.position()));
        }
    }
    private static void tap(ServerLevel l,LiteraryActor tilly){l.playSound(null,tilly.getX(),tilly.getY(),tilly.getZ(),PellRegistry.CANE_TAP.get(),SoundSource.NEUTRAL,.3F,1.1F);}
    /** An outside door shut in front of her on her way back is opened, unless June has locked it. */
    private static void opened(ServerLevel l,BlockPos b,LiteraryActor tilly,BlockPos door){
        if(tilly.position().subtract(Vec3.atBottomCenterOf(b.offset(door))).horizontalDistanceSqr()<1.7*1.7&&!PellSite.open(l,b,door)&&!PellVisit.locked(b,door))PellHouse.toggle(l,b,door);
    }
    /** The way she has come, a point every STEP: a point she comes back to cuts off the loop she walked since, so the way back is the
     *  short one. */
    private static void trail(PellPeople.Household h,Vec3 at){
        var t=h.armTrail;
        if(!t.isEmpty()&&t.get(t.size()-1).distanceToSqr(at)<STEP*STEP)return;
        for(int i=0;i<t.size()-3;i++)if(t.get(i).distanceToSqr(at)<STEP*STEP){while(t.size()>i+1)t.remove(t.size()-1);return;}
        if(t.size()<TRAIL)t.add(at);
    }

    // ------------------------------------------------------------------------------------------------ her hand on what theirs is on
    /**
     * A hand on one of the house's things by the reader whose arm she has, with her beside them (PellHouse.thing): she turns to it and
     * puts her own hand on it; of some things she has a word, once a visit.
     */
    static void touched(ServerPlayer p,ServerLevel l,BlockPos b,BlockPos at,PellThing.Kind kind,int stage,CompoundTag own){
        var h=PellPeople.household(b);
        if(h.arm!=LED||!p.getUUID().equals(h.armWith))return;
        var tilly=PellPeople.find(l,b,T);if(tilly==null||tilly.distanceTo(p)>WITH)return;
        long now=l.getGameTime();
        float yaw=PellPeople.yawTo(tilly,Vec3.atCenterOf(at));tilly.setYRot(yaw);tilly.setYBodyRot(yaw);tilly.setYHeadRot(yaw);h.rest[T]=yaw;
        h.armTouch=now+TOUCH;tilly.gesture(PellPeople.FEEL);
        own.putInt(SHOWN,own.getInt(SHOWN)+1);
        String line=PellTexts.arm(kind,stage,h.visit);
        if(line==null||h.armSaid.contains(kind)||!PellVisit.ready(h,now,tilly))return;
        h.armSaid.add(kind);PellVisit.say(h,tilly,T,line,now);
        own.putBoolean("ArmWord0601",true);
    }
    /** The visit begun again: nobody has her arm, and she has had her word on nothing. */
    static void restart(PellPeople.Household h){h.arm=OFF;h.armWith=null;h.armAt=0;h.armTouch=0;h.armEdge=0;h.armFar=0;h.armWindow=PellIdle.NONE;h.armTrail.clear();h.armSaid.clear();}

    // ------------------------------------------------------------------------------------------------ for the cases
    /** Where the walk is at this house (OFF, LED, BACK), and whose arm she has. */
    public static int state(BlockPos b){return PellPeople.household(b).arm;}
    public static @Nullable java.util.UUID with(BlockPos b){return PellPeople.household(b).armWith;}
}
