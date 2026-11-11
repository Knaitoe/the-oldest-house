package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.486, Bike night: what changes in the town while the night goes on. The proposal:
 * <ul>
 * <li>of the laundromat: "Lit, with dryers tumbling. A woman is folding alone at the counter, and a television on a bracket plays a
 * late horror film." Its front is a picture hung on its wall, and the picture moves now (tools/draw_calder_signs.py: eight frames,
 * which the game plays by itself). Here is only what is heard of it.</li>
 * <li>of the drive-in: "in neon that buzzes and flickers one letter". The G of BURGERS goes out and comes back, a moment at a time
 * and now and then for longer, on both its signs at once, and is heard doing it. (The cabinet in its window has its screen going:
 * the same kind of picture.)</li>
 * <li>of the grocery: "Closing up: the lights go off aisle by aisle". Half a minute after the town wakes, its three windows go dark
 * one after another from the west, eight seconds apart, each with the clack of a switch at the box.</li>
 * <li>of the school: "Riders leave tyre tracks in the dew on the field". A bike going over the field's turf, on the ground and
 * moving, leaves the tracked turf under it: a reader's, one of the three's, Gus's.</li>
 * <li>of the town's sound: "distant music from a car radio, the dryers, the neon's buzz, the theater's laughter". The radio is in
 * the car two of the teenagers sit on.</li>
 * </ul>
 *
 * <p>All of it begins when the town wakes (the gate opened), as the cars and the people do. A night's end puts the windows and the
 * sign back as they were and takes every track off the field.
 */
public final class CalderEvening {
    private CalderEvening(){}
    /** In ticks: how long after the town wakes the grocery's first aisle goes dark, and how long between one aisle and the next. */
    public static final int CLOSING=600,AISLE_GAP=160;
    /**
     * In ticks: how long the dryers' sound is and the radio's tune (each is played again as it ends); the least and the most between
     * two laughs from the theater; and the least between two of the neon's buzzes.
     */
    public static final int DRYERS_EVERY=58,RADIO_EVERY=158,LAUGH_LEAST=300,LAUGH_MOST=700,BUZZ_GAP=12;
    /** In blocks: how near a reader is for each to be played at all. */
    public static final double DRYERS_HEARD=21,RADIO_HEARD=20,LAUGH_HEARD=24;
    /** Where each comes from: the laundromat's glass; the hood of the first parked car; the theater's doors. */
    public static final Vec3 DRYERS_AT=new Vec3(13,1.5,-98.5),RADIO_AT=new Vec3(CalderTraffic.PARKED[0][1],1,CalderTraffic.PARKED[0][2]+.5),LAUGH_AT=new Vec3(-14,1.5,-84.5);
    /** A bike going this fast or faster leaves tracks. */
    public static final float TRACKING=.06F;

    /**
     * A night's evening: when the town woke; whether the windows and the sign have been looked at since (a night that ended with
     * that part of the town asleep left them as they were); how many aisles are dark; whether the G is out, when it next changes,
     * since when it has been out and when it last buzzed; when each sound is next due; the field's tracked cells; and how long
     * after waking the grocery starts to close (for tests).
     */
    static final class Evening {
        long since=-1;boolean tidy;int dark;
        boolean dim;long neonNext=-1,dimSince,buzzAt;
        long dryersAt,radioAt,laughAt=-1;
        final Set<BlockPos> tracks=new HashSet<>();
        int closing=CLOSING;
    }
    private static boolean awake(ServerLevel l,BlockPos b){
        return l.isLoaded(b.offset(CalderTown.FOOD[0]))&&l.isLoaded(b.offset(CalderTown.FOOD[2]))&&l.isLoaded(b.offset(CalderTown.NEON_SOUTH))&&l.isLoaded(b.offset(CalderTown.NEON_NORTH))
            &&l.isLoaded(b.offset(CalderTown.FIELD_W,-1,CalderTown.FIELD_N))&&l.isLoaded(b.offset(CalderTown.FIELD_E,-1,CalderTown.FIELD_S));
    }
    /** The windows lit, the sign whole, and nothing on the field. */
    private static boolean tidy(ServerLevel l,BlockPos b){
        if(!awake(l,b))return false;
        for(int k=0;k<CalderTown.FOOD.length;k++)CalderTown.aisle(l,b,k,true);
        CalderTown.neon(l,b,false);
        var turf=BikeNightSite.mat("turf");var tracked=BikeNightSite.mat("turf_track");
        for(int x=CalderTown.FIELD_W;x<=CalderTown.FIELD_E;x++)for(int z=CalderTown.FIELD_N;z<=CalderTown.FIELD_S;z++){var at=b.offset(x,-1,z);if(l.getBlockState(at)==tracked)l.setBlock(at,turf,Block.UPDATE_CLIENTS);}
        return true;
    }
    /** The night over, or beginning. */
    static void reset(ServerLevel l,BikeNight.Night n){n.evening=new Evening();n.evening.tidy=tidy(l,n.base);}

    static void tick(ServerLevel l,BikeNight.Night n,List<ServerPlayer> here){
        var e=n.evening;
        if(!e.tidy&&!(e.tidy=tidy(l,n.base)))return;
        if(!n.traffic.going)return;
        if(e.since<0){e.since=n.tick;e.neonNext=n.tick+40;e.laughAt=n.tick+LAUGH_LEAST/2;}
        neon(l,n,e);aisles(l,n,e);tracks(l,n,e);sounds(l,n,e,here);
    }
    /** The bad letter: out for a moment, now and then for longer; back for a few seconds, now and then only for a moment. */
    private static void neon(ServerLevel l,BikeNight.Night n,Evening e){
        var b=n.base;
        if(n.tick<e.neonNext||!l.isLoaded(b.offset(CalderTown.NEON_SOUTH))||!l.isLoaded(b.offset(CalderTown.NEON_NORTH)))return;
        var rnd=l.getRandom();e.dim=!e.dim;
        if(e.dim){e.dimSince=n.tick;e.neonNext=n.tick+(rnd.nextInt(4)==0?14+rnd.nextInt(26):2+rnd.nextInt(4));}
        else e.neonNext=n.tick+(rnd.nextInt(3)==0?3+rnd.nextInt(6):40+rnd.nextInt(120));
        CalderTown.neon(l,b,e.dim);
        if(n.tick<e.buzzAt)return;e.buzzAt=n.tick+BUZZ_GAP;
        for(var at:new BlockPos[]{CalderTown.NEON_SOUTH,CalderTown.NEON_NORTH})play(l,Vec3.atCenterOf(b.offset(at).offset(3,0,0)),BikeNightRegistry.NEON,1F,.95F+rnd.nextFloat()*.1F);
    }
    /** Closing up. An aisle whose time came while that part of the town was asleep is dark when it wakes, and was not heard. */
    private static void aisles(ServerLevel l,BikeNight.Night n,Evening e){
        if(e.dark>=CalderTown.FOOD.length)return;
        long due=e.since+e.closing+(long)e.dark*AISLE_GAP;var at=n.base.offset(CalderTown.FOOD[e.dark]);
        if(n.tick<due||!l.isLoaded(at))return;
        CalderTown.aisle(l,n.base,e.dark,false);
        if(n.tick<due+AISLE_GAP)play(l,Vec3.atCenterOf(at.offset(1,1,0)),BikeNightRegistry.BREAKER,1.75F,.96F+l.getRandom().nextFloat()*.08F);
        e.dark++;
    }
    static AABB field(BlockPos b){return new AABB(b.getX()+CalderTown.FIELD_W,b.getY()-.5,b.getZ()+CalderTown.FIELD_N,b.getX()+CalderTown.FIELD_E+1,b.getY()+2,b.getZ()+CalderTown.FIELD_S+1);}
    /** Tracks in the dew: under any bike on the field's turf that is on the ground and moving. */
    private static void tracks(ServerLevel l,BikeNight.Night n,Evening e){
        if(!l.isLoaded(n.base.offset(CalderTown.FIELD_W,-1,CalderTown.FIELD_N)))return;
        var turf=BikeNightSite.mat("turf");var tracked=BikeNightSite.mat("turf_track");
        for(var bike:l.getEntitiesOfClass(RiddenBike.class,field(n.base),x->x.onGround()&&x.speedNow()>=TRACKING)){
            var under=BlockPos.containing(bike.getX(),bike.getY()-.5,bike.getZ());
            if(l.getBlockState(under)==turf){l.setBlock(under,tracked,Block.UPDATE_CLIENTS);e.tracks.add(under.immutable());}
        }
    }
    private static boolean near(List<ServerPlayer> here,BlockPos b,Vec3 at,double within){
        for(var p:here){double x=p.getX()-b.getX()-at.x,z=p.getZ()-b.getZ()-at.z;if(x*x+z*z<within*within)return true;}
        return false;
    }
    private static void play(ServerLevel l,Vec3 at,Holder<SoundEvent> sound,float volume,float pitch){l.playSound(null,at.x,at.y,at.z,sound.value(),SoundSource.AMBIENT,volume,pitch);}
    private static Vec3 world(BlockPos b,Vec3 r){return new Vec3(b.getX()+r.x,b.getY()+r.y,b.getZ()+r.z);}
    /** The dryers and the radio go round for as long as somebody is near enough to hear them; the theater laughs now and then. */
    private static void sounds(ServerLevel l,BikeNight.Night n,Evening e,List<ServerPlayer> here){
        var b=n.base;
        if(n.tick>=e.dryersAt&&near(here,b,DRYERS_AT,DRYERS_HEARD)){e.dryersAt=n.tick+DRYERS_EVERY;play(l,world(b,DRYERS_AT),BikeNightRegistry.DRYERS,1.3F,1F);}
        if(n.tick>=e.radioAt&&near(here,b,RADIO_AT,RADIO_HEARD)){e.radioAt=n.tick+RADIO_EVERY;play(l,world(b,RADIO_AT),BikeNightRegistry.RADIO,1.25F,1F);}
        if(n.tick>=e.laughAt){
            var rnd=l.getRandom();e.laughAt=n.tick+LAUGH_LEAST+rnd.nextInt(LAUGH_MOST-LAUGH_LEAST);
            if(near(here,b,LAUGH_AT,LAUGH_HEARD))play(l,world(b,LAUGH_AT),BikeNightRegistry.LAUGHTER,1.5F,.95F+rnd.nextFloat()*.1F);
        }
    }

    // ------------------------------------------------------------------------------------------------ for tests
    public static int dark(BlockPos b){var n=BikeNight.night(b);return n==null?-1:n.evening.dark;}
    public static boolean dim(BlockPos b){var n=BikeNight.night(b);return n!=null&&n.evening.dim;}
    public static int tracks(BlockPos b){var n=BikeNight.night(b);return n==null?-1:n.evening.tracks.size();}
    /** The grocery starts to close this long after the town wakes. */
    public static void closing(BlockPos b,int ticks){var n=BikeNight.night(b);if(n!=null)n.evening.closing=ticks;}
    /** The G changes now; the theater laughs now. */
    public static void neonNow(BlockPos b){var n=BikeNight.night(b);if(n!=null)n.evening.neonNext=n.tick;}
    public static void laughNow(BlockPos b){var n=BikeNight.night(b);if(n!=null)n.evening.laughAt=n.tick;}
}
