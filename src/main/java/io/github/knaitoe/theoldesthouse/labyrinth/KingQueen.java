package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.644, A sort of thing, part 4 (docs/SLEEPING_KING_0_4_644.md): the Queen on the hill. She waits on the road at the foot of the
 * hill; the first time a reader comes up to her she takes their hand and runs, and while she has hold of them they are a thing and the
 * country goes past without their getting anywhere (they are rooted; she runs on the spot beside them). Then she lets go, says her
 * rule, and walks to the far side of the field, where she stands looking back at the peg. A reader standing on it is looked at, so a
 * thing, and is counted on the square (KingBreath).
 */
public final class KingQueen {
    private KingQueen(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.SLEEPING_KING;
    public static final int PHASE=220;
    /** Waiting at the foot of the hill (0), running with a reader's hand (1), walking away (2), looking back at the peg (3). */
    public static final int WAITING=0,RUNNING=1,WALKING=2,LOOKING=3;
    /** How long she runs, how near a reader must come for her to take their hand, and how fast she walks. */
    public static final int RUN_TICKS=100;
    static final double REACH=3,PACE=.16;
    static final String RAN="KingQueenRan";

    public static boolean is(LiteraryActor a){return a.role()==LiteraryActor.STRANGER&&a.phase()==PHASE;}
    public static @Nullable LiteraryActor find(ServerLevel l,BlockPos b){
        for(var a:l.getEntitiesOfClass(LiteraryActor.class,new AABB(b.offset(KingSite.QUEEN)).inflate(32,8,32),KingQueen::is))if(a.isAlive())return a;
        return null;
    }
    static void ensure(ServerLevel l,BlockPos b){
        var at=b.offset(KingSite.QUEEN);
        if(!l.isLoaded(at)||!l.areEntitiesLoaded(ChunkPos.asLong(at))||find(l,b)!=null)return;
        var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)return;
        a.bind(PLACE,null);a.appearance(LiteraryActor.STRANGER,PHASE);
        a.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5,180,0);a.setYBodyRot(180);a.setYHeadRot(180);
        l.addFreshEntity(a);
    }
    /** A fresh dream: back at the foot of the hill, waiting. */
    static void reset(ServerLevel l,BlockPos b,KingSleep.Sleep s){
        s.queen=WAITING;s.queenWith=null;
        var a=find(l,b);if(a==null)return;var at=b.offset(KingSite.QUEEN);a.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5,180,0);a.gesture(WAITING);
    }
    /** Whether she has hold of this reader now. */
    public static boolean holding(ServerPlayer p,BlockPos b){var s=KingSleep.get(b);return s.queen==RUNNING&&p.getUUID().equals(s.queenWith);}
    /** Whether she is standing at the far side looking back at the peg. */
    public static boolean looking(BlockPos b){return KingSleep.get(b).queen==LOOKING;}
    public static int state(BlockPos b){return KingSleep.get(b).queen;}

    // ------------------------------------------------------------------------------------------------ while anybody is there
    static void tick(ServerLevel l,BlockPos b,KingSleep.Sleep s){
        ensure(l,b);var a=find(l,b);if(a==null)return;long now=l.getGameTime();
        switch(s.queen){
            case WAITING->{
                a.gesture(WAITING);
                var near=KingSleep.nearest(l,b,a.position());
                if(near!=null&&near.distanceTo(a)<16)KingSleep.face(a,near.position());
                // The first reader to come up to her who has not run with her yet, and is at least a sort of thing.
                if(near!=null&&near.distanceTo(a)<REACH&&KingBreath.of(near)>=KingBreath.SORT&&!ran(near)){
                    s.queen=RUNNING;s.queenWith=near.getUUID();s.queenAt=now+RUN_TICKS;
                    say(near,a,KingTexts.QUEEN_FASTER);
                }
            }
            case RUNNING->{
                var p=s.queenWith==null?null:l.getServer().getPlayerList().getPlayer(s.queenWith);
                if(p==null||!KingSleep.readers(l,b).contains(p)){s.queen=WAITING;s.queenWith=null;return;}
                // She runs on the spot beside them, facing up the road; they go nowhere.
                a.gesture(RUNNING);a.setYRot(180);a.setYBodyRot(180);a.setYHeadRot(180);
                if(now==s.queenAt-RUN_TICKS/2)say(p,a,KingTexts.QUEEN_DONT_TALK);
                if(now>=s.queenAt){
                    s.ran.add(p.getUUID());
                    say(p,a,KingTexts.QUEEN_LETS_GO);King.cue(p,KingTexts.QUEEN_LET_GO);
                    s.queen=WALKING;s.queenWith=null;
                }
            }
            case WALKING->{
                a.gesture(WALKING);
                var to=Vec3.atBottomCenterOf(b.offset(KingSite.QUEEN_FAR));var d=to.subtract(a.position());double len=d.horizontalDistance();
                if(len<.2){s.queen=LOOKING;a.moveTo(to.x,to.y,to.z,0,0);return;}
                var step=d.scale(Math.min(1,PACE/len));
                a.moveTo(a.getX()+step.x,a.getY(),a.getZ()+step.z,a.getYRot(),0);
                KingSleep.face(a,to);
            }
            case LOOKING->{
                // At the far side of the field, looking back at the peg, or at whoever stands on it.
                a.gesture(LOOKING);
                var peg=Vec3.atBottomCenterOf(b.offset(KingSite.peg(2)));
                ServerPlayer on=null;for(var p:KingSleep.readers(l,b))if(onPeg(p,b,2)){on=p;break;}
                KingSleep.face(a,on!=null?on.getEyePosition():peg.add(0,.3,0));
            }
            default->{}
        }
    }
    /** Whether this reader has run with her: their record, or this dream's memory of it (written through their own tick). */
    static boolean ran(ServerPlayer p){
        var b=LiteraryVignettes.base(p,PLACE);
        if(b!=null&&KingSleep.get(b).ran.contains(p.getUUID()))return true;
        var d=LabyrinthData.get(p.server);return LiteraryVignettes.personal(d,p.getUUID(),PLACE).getBoolean(RAN);
    }
    /** Whether a reader is standing on a square's peg. */
    public static boolean onPeg(ServerPlayer p,BlockPos b,int rank){
        var r=p.position().subtract(b.getX(),b.getY(),b.getZ());var peg=Vec3.atBottomCenterOf(KingSite.peg(rank));
        return Math.hypot(r.x-peg.x,r.z-peg.z)<.8&&Math.abs(r.y-peg.y)<1.2;
    }
    /** Her words over her head, to whoever is near (no conversation is open while she runs). */
    static void say(ServerPlayer p,LiteraryActor a,String line){a.say(KingTexts.QUEEN_NAME,line);a.speaks(line);}
}
