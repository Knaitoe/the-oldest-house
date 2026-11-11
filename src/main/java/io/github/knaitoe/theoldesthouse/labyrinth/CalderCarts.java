package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.488, Bike night: the grocery's stock boy and his carts. The proposal: "A stock boy is wheeling a long chain of carts across
 * the lot. They are a moving obstacle, and riding through the gap in the chain is a shortcut."
 *
 * <p><b>The chain.</b> Eight carts in a row along the aisle between the Food Fair's two rows of bays, with a gap after the fourth
 * that a bike goes through. He wheels them from the west end of the lot to the east and back, slowly, all night: at each end he
 * walks round to the other end of them and sets off again.
 *
 * <p><b>Where it matters.</b> The lot's east end is the mouth of the store's yard, and the yard is the way to Elm Street and the
 * cut. With the chain at the east end of its run the mouth is barred from side to side, but for the gap. Round its west end is eight
 * blocks further. The three's way into the yard crosses the aisle there too.
 *
 * <p><b>What a cart is.</b> A thing in the way, as a parked car is (TownCar.CART): nobody goes through one, and a rider who meets
 * one going fast comes off. The chain never runs into anybody: with somebody in its way, or between its carts, it waits, and he
 * says so. One of the three held up by it is told to go round.
 */
public final class CalderCarts {
    private CalderCarts(){}
    /** How many carts, and after which of them the gap is. */
    public static final int CARTS=8,GAP_AFTER=4;
    /**
     * In blocks: the aisle's line; from one cart's middle to the next, and how much further across the gap; how far west the first
     * cart goes and how far east the last; the chain's pace a tick; how far ahead of it somebody in its lane stops it, and how far
     * to either side of its line counts as in its lane; how far behind the last cart he walks; and the line he walks round it on.
     */
    public static final double AISLE_Z=-104.5,STEP=.62,GAP=1.9,WEST=-35,EAST=-4.6,PACE=.05,AHEAD=1.3,BESIDE=.85,BEHIND=.95,ROUND_Z=-103.5;
    /** In ticks: how long it stands at each end at least; how long it has waited for somebody before he speaks; between two things he says; and between two rattles. */
    public static final int PAUSE_WEST=100,PAUSE_EAST=200,MIND_AFTER=30,SAY_GAP=200,RATTLE_EVERY=20;
    /** His pace walking round, and how near a reader is for the rattle to be played. */
    public static final double WALK=.11,HEARD=20;
    /** From the first cart's middle to the last's. */
    public static double span(){return (CARTS-1)*STEP+GAP;}
    /** How far along the chain cart {@code i} is, from the first cart's middle. */
    public static double along(int i){return i*STEP+(i>=GAP_AFTER?GAP:0);}
    /** The gap, from the back of the fourth cart to the front of the fifth, along the chain from the first cart's middle. */
    public static double gapFrom(){return along(GAP_AFTER-1)+TownCar.CART_LONG;}
    public static double gapTo(){return along(GAP_AFTER)-TownCar.CART_LONG;}
    /** Where he stands before any of it: behind the chain at the west end of its run. */
    public static Vec3 boyStart(){return new Vec3(WEST-BEHIND,0,AISLE_Z);}

    /**
     * A night's chain: where its first cart's middle is; which way it is going; until when it stands, and whether he is still walking
     * round it; its carts' bodies; since when it has waited for somebody, and whether that is a reader; when he last spoke and
     * what is next; which side of the aisle each reader was on; when it next rattles; how many readers have ridden through the
     * gap; and its pace (for tests).
     */
    static final class Chain {
        double west=WEST;int dir=1;long pauseUntil=-1;boolean rounding;
        final UUID[] ids=new UUID[CARTS];
        long waitSince=-1;boolean waiting;long saidAt=-SAY_GAP;int said;
        final Map<UUID,Integer> side=new HashMap<>();long rattleAt;int through;
        double pace=PACE;
    }
    private static boolean awake(ServerLevel l,BlockPos b){
        for(double x:new double[]{WEST-1,-20,EAST}){var cell=BlockPos.containing(b.getX()+x,b.getY(),b.getZ()+AISLE_Z);if(!l.isLoaded(cell)||!l.isPositionEntityTicking(cell))return false;}
        return true;
    }
    static void reset(ServerLevel l,BikeNight.Night n){
        for(var id:n.carts.ids)if(id!=null&&l.getEntity(id) instanceof TownCar body)body.discard();
        n.carts=new Chain();
    }
    private static Vec3 cartAt(BlockPos b,Chain c,int i){return new Vec3(b.getX()+c.west+along(i),b.getY(),b.getZ()+AISLE_Z);}

    static void tick(ServerLevel l,BikeNight.Night n,List<ServerPlayer> here){
        if(!n.traffic.going||!awake(l,n.base))return;
        var c=n.carts;var b=n.base;var boy=CalderPeople.find(l,b,CalderPeople.STOCK);
        boolean moved=false;
        if(n.tick>=c.pauseUntil&&!c.rounding&&boy!=null){
            int in=inWay(l,n,c,here,boy);
            if(in==0){
                c.waitSince=-1;c.waiting=false;c.west+=c.dir*c.pace;moved=true;
                if(c.dir>0&&c.west+span()>=EAST){c.west=EAST-span();c.dir=-1;c.pauseUntil=n.tick+PAUSE_EAST;c.rounding=true;}
                else if(c.dir<0&&c.west<=WEST){c.west=WEST;c.dir=1;c.pauseUntil=n.tick+PAUSE_WEST;c.rounding=true;}
            }else{
                if(c.waitSince<0)c.waitSince=n.tick;c.waiting=true;
                // A reader in its way for a while: he says so. One of the three: he is told to go round, at once.
                if(in>1)say(n,c,boy,BikeNightTexts.CARTS_ROUND[in-2]);
                else if(n.tick-c.waitSince>=MIND_AFTER)say(n,c,boy,BikeNightTexts.CARTS_MIND[c.said%BikeNightTexts.CARTS_MIND.length]);
            }
        }
        for(int i=0;i<CARTS;i++){
            var at=cartAt(b,c,i);
            if(c.ids[i]!=null&&l.getEntity(c.ids[i]) instanceof TownCar body&&body.isAlive()){body.place(at,-90);continue;}
            var body=CalderTraffic.make(l,TownCar.CART,at,-90);c.ids[i]=body==null?null:body.getUUID();
        }
        if(boy!=null)boy(n,c,boy,moved);
        through(l,n,c,here,boy);
        if(moved&&n.tick>=c.rattleAt){
            c.rattleAt=n.tick+RATTLE_EVERY;var mid=new Vec3(b.getX()+c.west+span()/2,b.getY()+.6,b.getZ()+AISLE_Z);
            for(var p:here)if(p.distanceToSqr(mid)<HEARD*HEARD){l.playSound(null,mid.x,mid.y,mid.z,BikeNightRegistry.CARTS.get(),SoundSource.AMBIENT,1.25F,.95F+l.getRandom().nextFloat()*.1F);break;}
        }
    }
    /**
     * What is in the chain's way: nothing (0), a reader (1), or one of the three (2 and up, by which). In its lane, between the
     * back of its last cart and a little ahead of its first; the gap is in its lane.
     */
    private static int inWay(ServerLevel l,BikeNight.Night n,Chain c,List<ServerPlayer> here,LiteraryActor boy){
        var b=n.base;double lo=c.west-TownCar.CART_LONG-(c.dir<0?AHEAD:.2),hi=c.west+span()+TownCar.CART_LONG+(c.dir>0?AHEAD:.2);
        for(var p:here){
            if(p.isSpectator())continue;var at=p.getVehicle()!=null?p.getVehicle().position():p.position();
            double x=at.x-b.getX(),z=at.z-b.getZ()-AISLE_Z;
            if(Math.abs(z)<BESIDE&&x>lo&&x<hi&&Math.abs(at.y-b.getY())<2)return 1;
        }
        for(var u:n.pack.b){
            var his=BikeNightBullies.bike(l,u);if(his==null||!u.riding||u.gone)continue;
            double x=his.getX()-b.getX(),z=his.getZ()-b.getZ()-AISLE_Z;
            if(Math.abs(z)<BESIDE&&x>lo&&x<hi)return 2+u.who;
        }
        if(n.gus.bike!=null&&l.getEntity(n.gus.bike) instanceof PackBike gus&&gus.isAlive()){double x=gus.getX()-b.getX(),z=gus.getZ()-b.getZ()-AISLE_Z;if(Math.abs(z)<BESIDE&&x>lo&&x<hi)return 1;}
        return 0;
    }
    private static void say(BikeNight.Night n,Chain c,LiteraryActor boy,String line){
        if(n.tick-c.saidAt<SAY_GAP||boy.saying())return;
        c.saidAt=n.tick;c.said++;CalderPeople.say(boy,line);
    }
    /** Behind the chain, pushing, for the way it is going; or walking round it to its other end. */
    private static void boy(BikeNight.Night n,Chain c,LiteraryActor boy,boolean moved){
        var b=n.base;double end=c.dir>0?c.west-BEHIND:c.west+span()+BEHIND;
        var to=new Vec3(b.getX()+end,b.getY(),b.getZ()+AISLE_Z);float look=c.dir>0?-90:90;
        if(c.rounding){
            boy.gesture(0);
            boolean beside=Math.abs(boy.getZ()-(b.getZ()+ROUND_Z))<.25,level=Math.abs(boy.getX()-to.x)<.3;
            var next=level?to:beside?new Vec3(to.x,to.y,b.getZ()+ROUND_Z):new Vec3(boy.getX(),to.y,b.getZ()+ROUND_Z);
            SceneFootpaths.step(boy,next,WALK);
            if(BikeNightGus.flat(boy.position(),to)<.2*.2)c.rounding=false;
            return;
        }
        if(BikeNightGus.flat(boy.position(),to)>.05*.05)SceneFootpaths.step(boy,to,c.pace+.03);
        boy.setYRot(look);boy.setYBodyRot(look);boy.setYHeadRot(look);
        boy.gesture(moved?CalderPeople.PUSH:0);
    }
    /** A reader on a bike who crosses the aisle's line inside the gap has ridden through it. */
    private static void through(ServerLevel l,BikeNight.Night n,Chain c,List<ServerPlayer> here,@Nullable LiteraryActor boy){
        var b=n.base;
        for(var p:here){
            if(p.isSpectator()||!(p.getVehicle() instanceof RiddenBike bike)){c.side.remove(p.getUUID());continue;}
            double x=bike.getX()-b.getX()-c.west,z=bike.getZ()-b.getZ()-AISLE_Z;int now=z>0?1:-1;
            var before=c.side.put(p.getUUID(),now);
            if(before!=null&&before!=now&&x>gapFrom()&&x<gapTo()){
                c.through++;
                if(boy!=null)say(n,c,boy,BikeNightTexts.CARTS_THROUGH[c.said%BikeNightTexts.CARTS_THROUGH.length]);
            }
        }
    }

    // ------------------------------------------------------------------------------------------------ for tests
    public static double west(BlockPos b){var n=BikeNight.night(b);return n==null?Double.NaN:n.carts.west;}
    public static int dir(BlockPos b){var n=BikeNight.night(b);return n==null?0:n.carts.dir;}
    public static boolean rounding(BlockPos b){var n=BikeNight.night(b);return n!=null&&n.carts.rounding;}
    public static boolean waiting(BlockPos b){var n=BikeNight.night(b);return n!=null&&n.carts.waiting;}
    public static int through(BlockPos b){var n=BikeNight.night(b);return n==null?-1:n.carts.through;}
    public static @Nullable TownCar cart(ServerLevel l,BlockPos b,int i){var n=BikeNight.night(b);return n!=null&&n.carts.ids[i]!=null&&l.getEntity(n.carts.ids[i]) instanceof TownCar body&&body.isAlive()?body:null;}
    /** The chain is here, going this way, at this pace. */
    public static void put(BlockPos b,double west,int dir,double pace){var n=BikeNight.night(b);if(n!=null){n.carts.west=west;n.carts.dir=dir;n.carts.pace=pace;n.carts.pauseUntil=-1;}}
}
