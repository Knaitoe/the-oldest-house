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
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.726, The list part 7a (docs/THE_LIST_0_4_726.md): night four, Stace's plan. Half a minute into the last hunt Stace has worked
 * out where it ends, by who is left of the five: at the gin while three or more are alive, at the foot of the water tower while one or
 * two are, out on the playa once none is. From then the House's door is shut to the one who went in until first light.
 * <p>
 * She waits there. When the one who went in comes within her reach and is on their feet, she puts them down; a few seconds after they
 * get up she does it again. The third time, she finishes it: the night is over, and for the one who went in the four nights end
 * stopped.
 * <p>
 * Where she is and how many times she has put them down is kept per town and never saved.
 */
public final class TheListStace{
    private TheListStace(){}
    /** The three places the night can end. */
    public static final int GIN=0,TOWER=1,PLAYA=2;
    /** Ticks into night four's hunt before her plan begins; how near she reaches; ticks between blows; blows before she finishes it. */
    public static final int PLAN_AFTER=600,GAP=40,FINISH=3;
    static final double REACH=2.5,COMES=12;

    static final class Plan{int where=-1;int blows;long last=-1_000_000L;}
    static final Map<Long,Plan> PLANS=new HashMap<>();
    public static void clearAll(){PLANS.clear();}

    /** Whether her plan has begun in this town. */
    public static boolean on(BlockPos b){var p=PLANS.get(b.asLong());return p!=null&&p.where>=0;}
    /** Where it ends, or -1 before her plan. */
    public static int where(BlockPos b){var p=PLANS.get(b.asLong());return p==null?-1:p.where;}
    /** How many times she has put the one who went in down. */
    public static int blows(BlockPos b){var p=PLANS.get(b.asLong());return p==null?0:p.blows;}
    /** Whether Stace is the plan's: wherever she has got to. */
    static boolean holds(MinecraftServer s,BlockPos b,int who){return who==TheListPeople.STACE&&on(b);}

    /** Where she waits for each ending, in the town's coordinates. */
    public static Vec3 at(BlockPos b,int where){
        return switch(where){
            case GIN->TheListGin.front(b).subtract(Vec3.atLowerCornerOf(b)).add(0,0,-2);
            case TOWER->new Vec3(TheListSite.TOWER_X+.5,0,TheListSite.TOWER_Z+7.5);
            default->new Vec3(TheListSite.PLAYA_W+14.5,0,TheListSite.PLAYA_S-14.5);
        };
    }
    /** Which place the night ends at, by how many of the five are alive. */
    static int choose(int alive){return alive<=0?PLAYA:alive<=2?TOWER:GIN;}

    /** One tick of night four's hunt. */
    static void tick(ServerLevel l,TheListNight.Night n,List<ServerPlayer> here){
        var b=n.base;var p=PLANS.computeIfAbsent(b.asLong(),k->new Plan());
        if(p.where<0){if(n.since>=PLAN_AFTER)begin(l,n,p);return;}
        var stace=find(l,b);if(stace==null)return;
        var spot=ground(l,b,at(b,p.where));
        var slasher=TheListNight.slasher(l,n);
        long now=l.getGameTime();
        if(slasher!=null&&TheListBody.down(slasher)==0&&slasher.position().distanceTo(spot)<=COMES){
            face(stace,slasher.position());
            if(stace.distanceTo(slasher)>REACH-.5)BorrascaCast.walk(stace,slasher.position(),.9);
            if(stace.distanceTo(slasher)<=REACH&&now-p.last>=GAP){
                p.last=now;p.blows++;
                stace.swing(net.minecraft.world.InteractionHand.MAIN_HAND,true);
                l.playSound(null,slasher.getX(),slasher.getEyeY(),slasher.getZ(),SoundEvents.PLAYER_ATTACK_STRONG,SoundSource.NEUTRAL,1.2F,.8F);
                if(p.blows>=FINISH){finish(l,n,p,stace,slasher);return;}
                say(stace,TheListTexts.STACE_DOWN);
                TheListBody.felled(slasher,TheListTexts.STACE_HIT,stace.position());
            }
            return;
        }
        // Back to her place when nobody is in reach.
        if(stace.position().distanceTo(spot)>1.2){if(!stace.getPersistentData().contains("BorrascaSpeed"))BorrascaCast.walk(stace,spot,.9);}
        else{BorrascaCast.stop(stace);stace.setNoGravity(true);}
    }
    /** Her plan begun: where it ends chosen, her there, the House's door shut to the one who went in, and they told. */
    static void begin(ServerLevel l,TheListNight.Night n,Plan p){
        p.where=choose(TheListNights.alive(n));
        var stace=find(l,n.base);
        if(stace!=null){var spot=ground(l,n.base,at(n.base,p.where));BorrascaCast.stop(stace);stace.moveTo(spot.x,spot.y,spot.z,stace.getYRot(),0);stace.setNoGravity(true);}
        var slasher=TheListNight.slasher(l,n);
        if(slasher!=null)TheList.cue(slasher,TheListTexts.STACE_PLAN[p.where]);
    }
    /** The third time: she finishes it. The night is over, and the one who went in is stopped. */
    static void finish(ServerLevel l,TheListNight.Night n,Plan p,LiteraryActor stace,ServerPlayer slasher){
        say(stace,TheListTexts.STACE_FINISH);
        TheList.cue(slasher,TheListTexts.STACE_FINISHED);
        var d=LabyrinthData.get(l.getServer());var own=LiteraryVignettes.personal(d,slasher.getUUID(),TheList.PLACE);
        own.putBoolean("Stopped",true);LiteraryVignettes.save(d,slasher.getUUID(),TheList.PLACE,own);
        TheListNights.light(l,n,false);
    }
    static @Nullable LiteraryActor find(ServerLevel l,BlockPos b){
        var found=l.getEntitiesOfClass(LiteraryActor.class,TheListBody.bounds(b),x->TheListPeople.is(x)&&TheListPeople.who(x)==TheListPeople.STACE&&x.isAlive());
        return found.isEmpty()?null:found.get(0);
    }
    /** Where to stand at a place: the first open cell on solid ground near the place's height. */
    static Vec3 ground(ServerLevel l,BlockPos b,Vec3 rel){
        var at=BlockPos.containing(Vec3.atLowerCornerOf(b).add(rel));
        for(int dy=6;dy>=-6;dy--){
            var c=at.offset(0,dy,0);
            if(l.isLoaded(c)&&l.getBlockState(c).isAir()&&l.getBlockState(c.above()).isAir()&&!l.getBlockState(c.below()).isAir())return Vec3.atBottomCenterOf(c);
        }
        return Vec3.atBottomCenterOf(at);
    }
    static void face(LiteraryActor a,Vec3 at){var d=at.subtract(a.position());float yaw=(float)Math.toDegrees(Math.atan2(-d.x,d.z));a.setYRot(yaw);a.setYBodyRot(yaw);a.setYHeadRot(yaw);}
    static void say(LiteraryActor a,String line){a.say(TheListTexts.NAMES[TheListPeople.STACE],line);a.speaks(line);}
    /** First light, or the night put away: her plan forgotten. */
    static void reset(BlockPos b){PLANS.remove(b.asLong());}
    /** For a check: her plan begun now. */
    public static void planNow(ServerLevel l,BlockPos b){
        var n=TheListNight.night(l.getServer(),b);if(n==null||n.phase!=TheListNights.HUNT4)return;
        var p=PLANS.computeIfAbsent(b.asLong(),k->new Plan());if(p.where<0)begin(l,n,p);
    }
}
