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
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;

/**
 * Night four, Stace's plan (0.4.726, docs/THE_LIST_0_4_726.md; walking, the shovel and her death 0.4.727, docs/THE_LIST_0_4_727.md).
 * Half a minute into the last hunt Stace has worked out where it ends, by who is left of the five: at the gin while three or more are
 * alive, at the foot of the water tower while one or two are, out on the playa once none is. From then the House's door is shut to the
 * one who went in until first light.
 * <p>
 * She goes there herself: along the catwalk to the ladder, down it rung by rung, and across town on foot, a shovel in her hands. There
 * she waits. When the one who went in comes within her reach and is on their feet, she puts them down; a few seconds after they get up
 * she does it again. The third time, she finishes it: the night is over, and for the one who went in the four nights end stopped.
 * <p>
 * She can be fought. A blow from the one who went in staggers her and she cannot swing for three seconds; the third puts her on the
 * ground, and a blow there with the hay hook in hand finishes her. She stays where she fell until first light, and the account keeps it.
 * <p>
 * Where she is, how many times she has put the one who went in down and how many blows she has taken are kept per town and never saved;
 * that she is dead is kept in the one who went in's own record.
 */
public final class TheListStace{
    private TheListStace(){}
    /** The three places the night can end. */
    public static final int GIN=0,TOWER=1,PLAYA=2;
    /** Her way there: along the catwalk to the ladder, down it, across town, there; down; dead. */
    public static final int CATWALK=0,LADDER=1,WALKING=2,THERE=3,DOWN=4,DEAD=5;
    /** Ticks into night four's hunt before her plan begins; ticks between her blows; her blows before she finishes it; ticks she is
     * staggered after a blow; blows she takes before she goes down; ticks without progress before she is set on at her goal. */
    public static final int PLAN_AFTER=600,GAP=40,FINISH=3,STAGGER=60,TAKES=3,STUCK=100;
    static final double REACH=2.5,COMES=12,CLIMB=.25,PACE=.9;

    static final class Plan{int where=-1;int stage;int blows;int taken;long last=-1_000_000L;long staggered=-1_000_000L;int still;@Nullable Vec3 lastAt;}
    static final Map<Long,Plan> PLANS=new HashMap<>();
    public static void clearAll(){PLANS.clear();}

    /** Whether her plan has begun in this town. */
    public static boolean on(BlockPos b){var p=PLANS.get(b.asLong());return p!=null&&p.where>=0;}
    /** Where it ends, or -1 before her plan. */
    public static int where(BlockPos b){var p=PLANS.get(b.asLong());return p==null?-1:p.where;}
    /** How far along she is (CATWALK to DEAD), or -1 before her plan. */
    public static int stage(BlockPos b){var p=PLANS.get(b.asLong());return p==null||p.where<0?-1:p.stage;}
    /** How many times she has put the one who went in down. */
    public static int blows(BlockPos b){var p=PLANS.get(b.asLong());return p==null?0:p.blows;}
    /** How many blows she has taken. */
    public static int taken(BlockPos b){var p=PLANS.get(b.asLong());return p==null?0:p.taken;}
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
    /** The ladder: its top, in the catwalk's gap, and its foot, in the town's coordinates. */
    static final Vec3 LADDER_TOP=new Vec3(TheListSite.TOWER_X+2.5,TheListSite.CATWALK_Y,TheListSite.TOWER_Z+3.5),
        LADDER_FOOT=new Vec3(TheListSite.TOWER_X+2.5,0,TheListSite.TOWER_Z+4.5);
    /** Which place the night ends at, by how many of the five are alive. */
    static int choose(int alive){return alive<=0?PLAYA:alive<=2?TOWER:GIN;}

    /** One tick of night four's hunt. */
    static void tick(ServerLevel l,TheListNight.Night n,List<ServerPlayer> here){
        var b=n.base;var p=PLANS.computeIfAbsent(b.asLong(),k->new Plan());
        if(p.where<0){if(n.since>=PLAN_AFTER)begin(l,n,p);return;}
        if(p.stage>=DOWN)return;
        var stace=find(l,b);if(stace==null)return;
        var base=Vec3.atLowerCornerOf(b);
        switch(p.stage){
            case CATWALK->{
                var top=base.add(LADDER_TOP);
                if(stace.position().distanceTo(top)>.6){walkTo(stace,top,p);return;}
                BorrascaCast.stop(stace);stace.setNoGravity(true);p.stage=LADDER;p.still=0;
            }
            case LADDER->{
                // Down the ladder rung by rung, facing it.
                float yaw=net.minecraft.core.Direction.NORTH.toYRot();
                double y=Math.max(base.y,stace.getY()-CLIMB);
                stace.moveTo(base.x+LADDER_TOP.x,y,base.z+LADDER_TOP.z,yaw,0);stace.setYBodyRot(yaw);stace.setYHeadRot(yaw);
                if(y<=base.y+1e-3){stace.moveTo(base.x+LADDER_FOOT.x,base.y,base.z+LADDER_FOOT.z,yaw,0);p.stage=WALKING;p.still=0;p.lastAt=null;}
            }
            case WALKING->{
                var spot=ground(l,b,at(b,p.where));
                if(stace.position().distanceTo(spot)>1.2){walkTo(stace,spot,p);return;}
                BorrascaCast.stop(stace);stace.setNoGravity(true);p.stage=THERE;
            }
            case THERE->there(l,n,p,stace);
            default->{}
        }
    }
    /** Toward a point on foot; set on there if something has stopped her for five seconds. */
    static void walkTo(LiteraryActor a,Vec3 to,Plan p){
        if(p.lastAt!=null&&a.position().distanceToSqr(p.lastAt)<.0004){if(++p.still>=STUCK){a.moveTo(to.x,to.y,to.z,a.getYRot(),0);p.still=0;}}else p.still=0;
        p.lastAt=a.position();
        if(!a.getPersistentData().contains("BorrascaSpeed"))BorrascaCast.walk(a,to,PACE);
    }
    /** At her place: the one who went in, near and on their feet, closed on and put down. */
    static void there(ServerLevel l,TheListNight.Night n,Plan p,LiteraryActor stace){
        var b=n.base;var spot=ground(l,b,at(b,p.where));
        var slasher=TheListNight.slasher(l,n);
        long now=l.getGameTime();
        boolean staggered=now-p.staggered<STAGGER;
        if(slasher!=null&&TheListBody.down(slasher)==0&&slasher.position().distanceTo(spot)<=COMES){
            face(stace,slasher.position());
            if(staggered)return;
            if(stace.distanceTo(slasher)>REACH-.5){if(!stace.getPersistentData().contains("BorrascaSpeed"))BorrascaCast.walk(stace,slasher.position(),PACE);}
            if(stace.distanceTo(slasher)<=REACH&&now-p.last>=GAP){
                p.last=now;p.blows++;
                stace.swing(net.minecraft.world.InteractionHand.MAIN_HAND,true);
                l.playSound(null,slasher.getX(),slasher.getEyeY(),slasher.getZ(),SoundEvents.PLAYER_ATTACK_STRONG,SoundSource.NEUTRAL,1.2F,.8F);
                if(p.blows>=FINISH){finish(l,n,stace,slasher);return;}
                say(stace,TheListTexts.STACE_DOWN);
                TheListBody.felled(slasher,TheListTexts.STACE_HIT,stace.position());
            }
            return;
        }
        // Back to her place when nobody is in reach.
        if(stace.position().distanceTo(spot)>1.2){if(!stace.getPersistentData().contains("BorrascaSpeed"))BorrascaCast.walk(stace,spot,PACE);}
        else{BorrascaCast.stop(stace);stace.setNoGravity(true);}
    }
    /** Her plan begun: where it ends chosen, her on her way with the shovel, the House's door shut to the one who went in, and they told. */
    static void begin(ServerLevel l,TheListNight.Night n,Plan p){
        p.where=choose(TheListNights.alive(n));p.stage=CATWALK;
        var stace=find(l,n.base);
        // Not up on the catwalk: she starts on foot from wherever she is.
        if(stace!=null&&stace.getY()<n.base.getY()+TheListSite.CATWALK_Y-1)p.stage=WALKING;
        if(stace!=null)stace.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_SHOVEL));
        var slasher=TheListNight.slasher(l,n);
        if(slasher!=null)TheList.cue(slasher,TheListTexts.STACE_PLAN[p.where]);
    }
    /** The third time: she finishes it. The night is over, and the one who went in is stopped. */
    static void finish(ServerLevel l,TheListNight.Night n,LiteraryActor stace,ServerPlayer slasher){
        say(stace,TheListTexts.STACE_FINISH);
        TheList.cue(slasher,TheListTexts.STACE_FINISHED);
        var d=LabyrinthData.get(l.getServer());var own=LiteraryVignettes.personal(d,slasher.getUUID(),TheList.PLACE);
        own.putBoolean("Stopped",true);LiteraryVignettes.save(d,slasher.getUUID(),TheList.PLACE,own);
        TheListNights.light(l,n,false);
    }
    /**
     * A blow on her from the one who went in, once her plan has begun (from TheListHunt.struck). On her way or at her place: staggered,
     * and the third puts her down. On the ground, with the hay hook in hand: finished.
     */
    static void struck(ServerPlayer p,LiteraryActor stace,BlockPos b){
        var plan=PLANS.get(b.asLong());if(plan==null||plan.where<0||plan.stage==DEAD)return;
        var n=TheListNight.night(p.server,b);if(n==null||!p.getUUID().equals(n.slasher)||TheListBody.down(p)>0)return;
        var l=p.serverLevel();long now=l.getGameTime();
        if(plan.stage==DOWN){
            if(!TheListBody.hookInHand(p))return;
            plan.stage=DEAD;
            stace.gesture(TheListHunt.FALLEN);stace.marks(TheListHunt.WOUND);
            l.playSound(null,stace.getX(),stace.getY()+.5,stace.getZ(),SoundEvents.PLAYER_ATTACK_CRIT,SoundSource.NEUTRAL,1.1F,.7F);
            TheList.cue(p,TheListTexts.STACE_KILLED);
            var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),TheList.PLACE);
            own.putBoolean("StaceDead",true);LiteraryVignettes.save(d,p.getUUID(),TheList.PLACE,own);
            return;
        }
        if(now-plan.staggered<10)return;
        plan.staggered=now;plan.taken++;
        stace.knockback(.6,p.getX()-stace.getX(),p.getZ()-stace.getZ());
        l.playSound(null,stace.getX(),stace.getEyeY(),stace.getZ(),SoundEvents.PLAYER_ATTACK_KNOCKBACK,SoundSource.NEUTRAL,1F,1.1F);
        if(plan.taken>=TAKES){
            plan.stage=DOWN;BorrascaCast.stop(stace);stace.setPose(Pose.STANDING);stace.gesture(TheListHunt.FALLEN);
            TheList.cue(p,TheListTexts.STACE_FALLS);
        }else TheList.cue(p,TheListTexts.STACE_STAGGERS);
    }
    /** Whether the one who went in has finished her, from their own record. */
    public static boolean dead(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),TheList.PLACE).getBoolean("StaceDead");}

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
    /** First light, or the night put away: her plan forgotten, and her stood up again without the wound for the town to put back. */
    static void reset(ServerLevel l,BlockPos b){
        if(PLANS.remove(b.asLong())==null)return;
        var stace=find(l,b);if(stace!=null){BorrascaCast.stop(stace);stace.marks(0);stace.gesture(0);stace.setPose(Pose.STANDING);stace.setNoGravity(true);}
    }
    /** For a check: her plan begun now. */
    public static void planNow(ServerLevel l,BlockPos b){
        var n=TheListNight.night(l.getServer(),b);if(n==null||n.phase!=TheListNights.HUNT4)return;
        var p=PLANS.computeIfAbsent(b.asLong(),k->new Plan());if(p.where<0)begin(l,n,p);
    }
}
