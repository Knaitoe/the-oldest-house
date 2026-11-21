package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.724, The list part 6b (docs/THE_LIST_0_4_724.md): the deputy out on the streets. On night three and night four, while the hunt is
 * on, the deputy leaves his place by the substation and walks the town with a flashlight lit in his hand: east and west along Main
 * Street's south sidewalk, down Avenue K to Seventh and along Seventh, and back. The flashlight's beam is a real light a few blocks ahead
 * of him, and a reader in it is standing in light (the pull is held off).
 * <p>
 * If he sees the one who went in (near enough, facing them, nothing in the way) he stops and calls out, and keeps the light on them. Seen
 * for a second and a half more, he fires: the body goes down where it stands and gets up as it always does. Then he does not fire again
 * for twenty seconds. Out of his sight for three seconds, he goes back to walking. Other readers in town out on the street are told to go
 * home, once a night; nobody is made to.
 * <p>
 * Where he is on his way is kept per town and never saved; picked up again, he starts from the nearest corner of his round.
 */
public final class TheListDeputy{
    private TheListDeputy(){}
    /** His round, there and back: Main Street's south sidewalk, the avenue's west shoulder, Seventh's north edge. */
    static final Vec3[] ROUND={new Vec3(36.5,0,-110.5),new Vec3(20.5,0,-110.5),new Vec3(-40.5,0,-110.5),new Vec3(20.5,0,-110.5),
        new Vec3(17.5,0,-108.5),new Vec3(17.5,0,-75.5),new Vec3(-40.5,0,-75.5),new Vec3(17.5,0,-75.5),new Vec3(17.5,0,-108.5),new Vec3(20.5,0,-110.5)};
    static final double WALK=.55,SEES=20,FACING=60,BEAM=4;
    /** Ticks seen before he fires; ticks before he fires again; ticks out of sight before he walks on. */
    public static final int AIM=30,AGAIN=400,LOST=60,STUCK=100;
    /** Never fired: far enough back that the wait before firing again is long over. */
    static final long NEVER=-1_000_000L;

    static final class Patrol{int step;int still;@Nullable Vec3 last;@Nullable BlockPos beam;int seen;int unseen;long firedAt=NEVER;boolean called;}
    static final Map<Long,Patrol> PATROLS=new HashMap<>();
    public static void clearAll(){PATROLS.clear();}

    /** Whether the deputy is out on his round now: night three's or night four's hunt. */
    public static boolean on(MinecraftServer s,BlockPos b){
        var n=TheListNight.night(s,b);return n!=null&&(n.phase==TheListNights.HUNT3||n.phase==TheListNights.HUNT4);
    }
    /** Whether the town's deputy is the round's: wherever he has got to. */
    static boolean holds(MinecraftServer s,BlockPos b,int who){return who==TheListPeople.DEPUTY&&on(s,b);}

    /** One tick of his round in a town whose night three or four is on. */
    static void tick(ServerLevel l,TheListNight.Night n,List<ServerPlayer> here){
        var b=n.base;
        var found=l.getEntitiesOfClass(LiteraryActor.class,TheListBody.bounds(b),x->TheListPeople.is(x)&&TheListPeople.who(x)==TheListPeople.DEPUTY&&x.isAlive());
        if(found.isEmpty())return;
        var a=found.get(0);
        var p=PATROLS.computeIfAbsent(b.asLong(),k->{var q=new Patrol();q.step=nearest(a,b);return q;});
        beam(l,a,p);
        var slasher=TheListNight.slasher(l,n);
        long now=l.getGameTime();
        boolean sees=slasher!=null&&sees(a,slasher);
        if(sees){
            p.unseen=0;
            if(p.seen==0){
                BorrascaCast.stop(a);a.setNoGravity(true);
                say(a,TheListTexts.DEPUTY_HEY);TheList.cue(slasher,TheListTexts.DEPUTY_SEEN);
            }
            p.seen++;face(a,slasher.getEyePosition());
            if(p.seen>=AIM&&now-p.firedAt>=AGAIN&&TheListBody.down(slasher)==0){
                p.firedAt=now;
                l.playSound(null,a.getX(),a.getY()+1.4,a.getZ(),SoundEvents.GENERIC_EXPLODE.value(),SoundSource.HOSTILE,1.6F,1.9F);
                TheListBody.felled(slasher,TheListTexts.DEPUTY_SHOT,a.position());
                say(a,TheListTexts.DEPUTY_FIRED);
            }
            return;
        }
        if(p.seen>0){
            if(++p.unseen<LOST)return;
            p.seen=0;p.unseen=0;p.still=0;p.last=null;
        }
        // Readers on Amber's side out in the street: told to go home, once a night.
        if(!p.called)for(var q:here){
            if(slasher!=null&&q==slasher||!LiteraryVignettes.participant(q))continue;
            if(q.distanceTo(a)<8&&a.hasLineOfSight(q)){p.called=true;a.sayTo(q,TheListTexts.NAMES[TheListPeople.DEPUTY],TheListTexts.DEPUTY_CURFEW);a.speaks(TheListTexts.DEPUTY_CURFEW);break;}
        }
        walk(l,a,b,p);
    }
    static void walk(ServerLevel l,LiteraryActor a,BlockPos b,Patrol p){
        var goal=Vec3.atLowerCornerOf(b).add(ROUND[p.step]);
        if(a.position().distanceToSqr(goal)>.5){
            if(p.last!=null&&a.position().distanceToSqr(p.last)<.0004){if(++p.still>=STUCK){a.moveTo(goal.x,goal.y,goal.z,a.getYRot(),0);p.still=0;}}else p.still=0;
            p.last=a.position();
            if(!a.getPersistentData().contains("BorrascaSpeed"))BorrascaCast.walk(a,goal,WALK);
            return;
        }
        p.step=(p.step+1)%ROUND.length;p.still=0;
        BorrascaCast.walk(a,Vec3.atLowerCornerOf(b).add(ROUND[p.step]),WALK);
    }
    static int nearest(LiteraryActor a,BlockPos b){
        int best=0;double d=Double.MAX_VALUE;
        for(int i=0;i<ROUND.length;i++){double e=a.position().distanceToSqr(Vec3.atLowerCornerOf(b).add(ROUND[i]));if(e<d){d=e;best=i;}}
        return best;
    }
    /** Whether he has the one who went in in sight: near enough, turned toward them, nothing in the way. */
    static boolean sees(LiteraryActor a,ServerPlayer p){
        if(!p.isAlive()||p.isSpectator()||a.distanceTo(p)>SEES)return false;
        var d=p.getEyePosition().subtract(a.getEyePosition());
        double yaw=Math.toDegrees(Math.atan2(-d.x,d.z));
        return Math.abs(Mth.wrapDegrees(yaw-a.getYHeadRot()))<=FACING&&a.hasLineOfSight(p);
    }
    static void face(LiteraryActor a,Vec3 at){var d=at.subtract(a.getEyePosition());float yaw=(float)Math.toDegrees(Math.atan2(-d.x,d.z));a.setYRot(yaw);a.setYBodyRot(yaw);a.setYHeadRot(yaw);}
    static void say(LiteraryActor a,String line){a.say(TheListTexts.NAMES[TheListPeople.DEPUTY],line);a.speaks(line);}
    /** The flashlight's beam: a light where it falls, a few blocks ahead of him at chest height, moved as he moves. */
    static void beam(ServerLevel l,LiteraryActor a,Patrol p){
        var look=Vec3.directionFromRotation(0,a.getYHeadRot());
        var cell=BlockPos.containing(a.getX()+look.x*BEAM,a.getY()+1,a.getZ()+look.z*BEAM);
        if(cell.equals(p.beam))return;
        clear(l,p);
        if(l.isLoaded(cell)&&l.getBlockState(cell).isAir()){l.setBlock(cell,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,12),Block.UPDATE_CLIENTS);p.beam=cell;}
    }
    static void clear(ServerLevel l,Patrol p){
        if(p.beam!=null&&l.isLoaded(p.beam)&&l.getBlockState(p.beam).is(Blocks.LIGHT))l.setBlock(p.beam,Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);
        p.beam=null;
    }
    /** His round put away: the beam out, his way forgotten. */
    static void reset(ServerLevel l,BlockPos b){var p=PATROLS.remove(b.asLong());if(p!=null)clear(l,p);}
    /** Whether he is aiming at the one who went in now, and when he last fired (for a check). */
    public static boolean aiming(BlockPos b){var p=PATROLS.get(b.asLong());return p!=null&&p.seen>0;}
    public static boolean fired(BlockPos b){var p=PATROLS.get(b.asLong());return p!=null&&p.firedAt!=NEVER;}
    public static @Nullable BlockPos beamAt(BlockPos b){var p=PATROLS.get(b.asLong());return p==null?null:p.beam;}
    /** Which point of his round he is walking to (for a check). */
    public static int step(BlockPos b){var p=PATROLS.get(b.asLong());return p==null?-1:p.step;}
}
