package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * 0.4.715, The list, part 4c-2 (docs/THE_LIST_0_4_715.md): a car parked in one of the Caprock drive-in's stalls, nose in under the
 * canopy, its lights off and nobody in it: the blue sedan, whose driver's door opens. It is the town's while anyone is in it, told where
 * it stands every tick (a TownCar is gone two seconds after it is not), and never saved.
 * <p>
 * A hand on its driver's door opens it, for anyone who is not a spectator, with or without a hunt; another hand shuts it, slammed. The
 * door's swing is the town's own, sent to everyone with the car (TownCar.door). The door's own part in the hunt, the head of one of the
 * five slammed in it, is TheListHunt's (State.CAR): this keeps the door and swings it when told.
 */
public final class TheListCar {
    private TheListCar(){}
    /** Where it stands, in the town's coordinates: in the stall between the canopy's second and third posts from the north, its nose in
     * (east) and its driver's door to the north, clear of the posts and the menus. */
    public static final Vec3 AT=new Vec3(55.05,0,-139.0);
    public static final float YAW=-90F;
    /** The door's angles, in degrees out from shut: wide open; and where it stops on one whose head is in the way (a head between its
     * back edge and the pillar holds it a third open). */
    public static final float OPEN=70F,HIT=34F;
    /** Where the door hinges and its back edge when shut, in the car's own space, in blocks (x along the car toward its nose, z across it
     * toward its right; the left is the driver's side). The model's own numbers (tools/draw_calder_cars.py: HINGE, DOOR_X). */
    public static final double HINGE_X=13/16D,HINGE_Z=-15/16D,BACK_X=-1/16D;
    /** How near the door one held must be for the slasher to force their head in at it; and how near a hand must be to be on it. */
    public static final double REACH=2.6,HAND=1.4;
    /** Where one forced down at the door kneels, in the car's own space: just inside the opening's back edge, far enough out from the side
     * that the head, leaned in, sits in the doorway against the pillar. */
    static final double KNEEL_X=.19,KNEEL_Z=-1.51;

    /** One leg of the door's swing: to an angle, over so many ticks; then, at the end of the last, what it hit. */
    record Leg(float to,int ticks){}
    static final class Door{
        @Nullable UUID car;float angle,from;int t;final ArrayDeque<Leg> legs=new ArrayDeque<>();@Nullable Runnable landed;
        boolean swinging(){return !legs.isEmpty();}
    }
    static final Map<Long,Door> DOORS=new HashMap<>();
    static Door door(BlockPos b){return DOORS.computeIfAbsent(b.asLong(),k->new Door());}

    /** The car's place in the level, and how it points. */
    public static Vec3 at(BlockPos b){return Vec3.atLowerCornerOf(b).add(AT);}
    /** A point in the car's own space, in the level. Facing east, the car's x is the level's x and its z the level's z. */
    public static Vec3 local(BlockPos b,double x,double y,double z){var c=at(b);return new Vec3(c.x+x,c.y+y,c.z+z);}
    /** Where one forced down at the door kneels, and the way they face (into the car: south). */
    public static Vec3 kneel(BlockPos b){return local(b,KNEEL_X,0,KNEEL_Z);}
    public static final float KNEEL_YAW=0F;
    /** The middle of the door's opening at the height of a head, in the level. */
    public static Vec3 doorway(BlockPos b){return local(b,(HINGE_X+BACK_X)/2,.95,HINGE_Z);}
    /** Where the head is that the door comes down on: at the back of the opening, in it to the ears. */
    public static Vec3 head(BlockPos b){return local(b,KNEEL_X,1.15,HINGE_Z-.1);}
    /** Where blood off the head lands: on the lot under the door, just out from the car's side. */
    public static Vec3 under(BlockPos b){var h=head(b);return new Vec3(h.x,at(b).y,h.z-.35);}
    /** The door's back edge now, in the level: where it swings to. */
    public static Vec3 edge(BlockPos b){
        double a=Math.toRadians(angle(b)),dx=BACK_X-HINGE_X;
        return local(b,HINGE_X+dx*Math.cos(a),.7,HINGE_Z+dx*Math.sin(a));
    }

    /** The door's angle now, out from shut. */
    public static float angle(BlockPos b){var d=DOORS.get(b.asLong());return d==null?0:d.angle;}
    public static boolean swinging(BlockPos b){var d=DOORS.get(b.asLong());return d!=null&&d.swinging();}
    /** Whether the door stands open, wide and still. */
    public static boolean open(BlockPos b){var d=DOORS.get(b.asLong());return d!=null&&!d.swinging()&&d.angle>=OPEN-1;}
    /** The car, if it stands there now. */
    public static @Nullable TownCar car(ServerLevel l,BlockPos b){var d=DOORS.get(b.asLong());return d!=null&&d.car!=null&&l.getEntity(d.car) instanceof TownCar c&&c.isAlive()?c:null;}

    // ------------------------------------------------------------------------------------------------ a hand on it
    /**
     * A hand or a blow on the car (TownCar.interact, TownCar.skipAttackInteraction). Spectators touch nothing. Near its driver's door:
     * the hunt's first (TheListHunt.carDoor), otherwise the door, opened or slammed shut; anywhere else on it, a look at it.
     */
    public static void hand(ServerPlayer p,TownCar car){
        if(p.isSpectator())return;
        var b=TheListBody.town(p);if(b==null){b=townOf(car);if(b==null)return;}
        var d=DOORS.get(b.asLong());if(d==null||!car.getUUID().equals(d.car))return;
        if(!atDoor(b,p)){TheList.cue(p,TheListTexts.CAR_LOOK);return;}
        if(TheListHunt.carDoor(p,b))return;
        if(d.swinging())return;
        if(d.angle<1)swingOpen(p.serverLevel(),b,p);else swingShut(p.serverLevel(),b,p);
    }
    /** Whether a reader's hand is on the driver's door: near the door, open or shut, on its side of the car. */
    static boolean atDoor(BlockPos b,ServerPlayer p){
        var at=p.position();var mid=doorway(b);var edge=edge(b);
        double side=at.z-local(b,0,0,HINGE_Z).z;
        return side<.2&&(Math.hypot(at.x-mid.x,at.z-mid.z)<=HAND+.6||Math.hypot(at.x-edge.x,at.z-edge.z)<=HAND);
    }
    static @Nullable BlockPos townOf(TownCar car){for(var e:DOORS.entrySet())if(car.getUUID().equals(e.getValue().car))return BlockPos.of(e.getKey());return null;}
    /** Opened: the latch, and the door swings wide. */
    static void swingOpen(ServerLevel l,BlockPos b,@Nullable ServerPlayer p){
        swing(b,null,new Leg(OPEN,8));
        var at=doorway(b);l.playSound(null,at.x,at.y,at.z,TheListRegistry.CAR_OPEN.get(),SoundSource.BLOCKS,.9F,.95F+l.getRandom().nextFloat()*.1F);
        if(p!=null)TheList.cue(p,TheListTexts.CAR_OPENED);
    }
    /** Shut: swung to and slammed. */
    static void swingShut(ServerLevel l,BlockPos b,@Nullable ServerPlayer p){
        swing(b,()->{var at=doorway(b);l.playSound(null,at.x,at.y,at.z,TheListRegistry.CAR_DOOR.get(),SoundSource.BLOCKS,1.1F,.95F+l.getRandom().nextFloat()*.1F);},new Leg(0,3));
        if(p!=null)TheList.cue(p,TheListTexts.CAR_SHUT);
    }
    /** The door swung through these legs in turn, and then what it hit, if anything. */
    static void swing(BlockPos b,@Nullable Runnable landed,Leg... legs){
        var d=door(b);d.legs.clear();for(var leg:legs)d.legs.add(leg);d.from=d.angle;d.t=0;d.landed=landed;
    }
    /** Pulled back wide and slammed down to where it stops on what is in it; then it bounces off it and comes to rest there. */
    static void slam(BlockPos b,Runnable landed){
        var d=door(b);
        if(d.angle<OPEN-6)swing(b,null,new Leg(OPEN,4),new Leg(HIT,2));else swing(b,null,new Leg(HIT,2));
        d.landed=()->{landed.run();var e=door(b);e.legs.add(new Leg(HIT+11,2));e.legs.add(new Leg(HIT,3));e.from=e.angle;e.t=0;};
    }
    /** Held where it is, resting against what is in it. */
    static void rest(BlockPos b,float angle){var d=door(b);d.legs.clear();d.landed=null;d.angle=angle;d.from=angle;}

    // ------------------------------------------------------------------------------------------------ each tick
    /** The town's tick: the car told where it stands, and its door swung. */
    static void tick(ServerLevel l,BlockPos b,List<ServerPlayer> here){
        var d=door(b);
        var at=at(b);
        if(!l.isLoaded(BlockPos.containing(at)))return;
        TownCar car=d.car!=null&&l.getEntity(d.car) instanceof TownCar c&&c.isAlive()?c:null;
        if(car==null){car=CalderTraffic.make(l,TownCar.BLUE,at,YAW);if(car==null)return;d.car=car.getUUID();}
        car.place(at,YAW);car.lit(false);car.working(true);
        if(d.swinging()){
            var leg=d.legs.peek();d.t++;
            float f=Mth.clamp(d.t/(float)leg.ticks(),0,1);
            d.angle=Mth.lerp(f*f*(3-2*f),d.from,leg.to());
            if(d.t>=leg.ticks()){
                d.angle=leg.to();d.legs.poll();d.from=d.angle;d.t=0;
                if(d.legs.isEmpty()&&d.landed!=null){var then=d.landed;d.landed=null;then.run();}
            }
        }
        car.door(d.angle);
    }
    /** Laid again as it was: the door shut and still. */
    static void reset(BlockPos b){rest(b,0);}
    public static void clearAll(){DOORS.clear();}
}
