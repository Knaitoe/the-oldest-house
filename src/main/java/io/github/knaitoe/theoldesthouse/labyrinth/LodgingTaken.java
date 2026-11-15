package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.612, The Ones Already Inside, its twelfth slice (docs/THE_ONES_ALREADY_INSIDE_PLAN.md, N12: "taken readers in the passages; Vera
 * using the doubles").
 *
 * <ul>
 * <li><b>The disturbance.</b> A little after the first attempt is over, the failed body at the jib door comes through again, this time
 * into the room where the two men were put together (the parlor, or Daniel's room), and takes whoever there is nearest: a reader, or Rae,
 * or Dan, or Daniel if he is out of the frame. It walks them back through the house and out through the jib door and leaves them in the
 * passage. It goes back for a second if there is anyone left in that room, then the jib door swings to and its latch drops, on the
 * house's side. Blows break its grip and drive it off as before.</li>
 * <li><b>In the passages.</b> A taken reader is awake there. A hand on the lath knocks, and the knocking carries into the room on the
 * other side; while it is in a room with nobody in its hand, it lets the room be and goes back toward the knocking. A hatch slid back
 * from inside the wall is seen from the room. The jib door's latch is worked from inside, a finger through the gap at the jamb, with a
 * few crouched tries; from the house's side a hand lifts it at once. Rae, Dan or Daniel left in the wall comes out once it is lifted,
 * or works it themselves after a while.</li>
 * <li><b>Vera's one activity.</b> When it is over, Vera reads the house: each reader who answered her offer is for her (help) or against
 * (send Dan back, or no). If more are for her, she goes up to one of their rooms, raises their unfinished body from the bed, brings it
 * down by the wrist to Daniel's frame, and asks its original to put its hands where they would put their own. If more are against her,
 * she goes up to the workroom and calls down for one of them to come up, and asks whoever comes to hold Daniel when she calls. Either
 * way nothing is done to anybody's body but by that reader's own reply.</li>
 * </ul>
 *
 * <p>The disturbance, the latch and Vera's activity are the house's ({@code Taken0612} and {@code VeraParty0612} in the chapter's
 * record); who was taken, who knocked, who lifted the latch and what each reader answered are their own.
 */
public final class LodgingTaken {
    private LodgingTaken(){}
    /** The disturbance: not yet, waiting a little after the attempt, coming through, in the room, holding somebody, going back, over. */
    public static final int NONE=0,WAITING=1,COMING=2,LOOSE=3,DRAGGING=4,BACK=5,OVER=6;
    /** Vera's activity: not yet, about to, up for a body, raising it, bringing it down, asking its original; up to the workroom, calling. */
    public static final int V_NONE=0,V_WAIT=1,V_UP=2,V_RAISE=3,V_DOWN=4,V_ASK=5,V_SEND=6,V_CALLED=7;
    /** How long after the attempt is over before it comes through; after it is over before Vera acts; how long she draws back a bed. */
    public static final int AFTER=160,VERA_AFTER=80,RAISING=30;
    /** Its pace through the house, and with somebody in its hand; how near is a reach; how long it looks for somebody in the room. */
    public static final double PACE=.075,DRAG_PACE=.06,REACH=1.1,ACTOR_PACE=.1,VERA_PACE=.1;
    public static final int CHASE=360,EMPTY=60,STUN=15,TAKES=2;
    /** Blows that break a grip (from others, from the one held), and that drive it off. */
    public static final int FREE_OTHERS=2,FREE_SELF=3,KEEP_OFF=5;
    /** Crouched tries at the latch from inside; how long one of the four left in the wall waits before working it themselves. */
    public static final int LATCH_TRIES=4,WALL_WAIT=900;
    /** How near the knocking carries to a reader on the other side, and to Rae or Dan; between knocks heard as one. */
    public static final double KNOCK_CARRIES=9;
    public static final int KNOCK_GAP=30;
    /** The double's gestures standing (LodgingDoubles: ASLEEP 1, AWAKE 2): still, led by the wrist, its hands on the frame. */
    public static final int STAND=3,LED=4,STEADY=5;
    /** Each reader's own. */
    public static final String TAKEN="Taken0612",KNOCKED="Knocked0612",HEARD_KNOCK="HeardKnock0612",DREW_OFF="DrewOff0612",
        WORKED_LATCH="WorkedLatch0612",LIFTED_LATCH="LiftedLatch0612",HATCH_FROM_WALL="HatchFromWall0612",SAW_TAKEN="SawTaken0612",
        STEADIED="SteadiedDouble0612",REFUSED_DOUBLE="RefusedDouble0612",AGREED="AgreedVera0612",REFUSED_VERA="RefusedVera0612",
        SAW_DOUBLE_LED="SawDoubleLed0612";
    /** The house's records. */
    public static final String STATE="Taken0612",VERA_STATE="VeraParty0612";

    // ------------------------------------------------------------------------------------------------ where
    /** Through the jib door: its passage side (the failed body's own place), its house side. Where the taken are left. */
    static final Vec3 P0=new Vec3(-14.5,0,-13.5),P1=new Vec3(-12.5,0,-13.5),LEFT=new Vec3(-14.5,0,-15.5);
    /** From the jib door into the parlor (the jib on leg 1, the parlor's door on leg 5), and into Daniel's room (his door on leg 3). */
    static final List<Vec3> TO_PARLOR=List.of(P0,P1,new Vec3(0,0,-13.5),new Vec3(0,0,-8.5),new Vec3(-2.6,0,-8.5),new Vec3(-5.6,0,-8.5));
    static final List<Vec3> TO_DANIELS=List.of(P0,P1,new Vec3(-7.5,0,-14),new Vec3(-7.5,0,-18.5),new Vec3(-5.6,0,-20),new Vec3(-5.6,0,-24));
    static List<Vec3> route(int where){return where==LodgingMeeting.PARLOR?TO_PARLOR:TO_DANIELS;}
    static Map<Integer,BlockPos> doors(int where){return where==LodgingMeeting.PARLOR?Map.of(1,LodgingSite.JIB,5,LodgingSite.PARLOR_DOOR):Map.of(1,LodgingSite.JIB,3,LodgingSite.DANIEL_DOOR);}
    /** The same way walked back: the legs reversed, each door on the matching leg. */
    static List<Vec3> back(List<Vec3> legs){var out=new ArrayList<>(legs);java.util.Collections.reverse(out);return out;}
    /** For legs of n points: a door crossed going from point k-1 to k is crossed coming back from point n-k-1 to n-k. */
    static Map<Integer,BlockPos> backDoors(int n,Map<Integer,BlockPos> doors){var out=new HashMap<Integer,BlockPos>();doors.forEach((k,v)->out.put(n-k,v));return out;}
    static boolean inRoom(int where,Vec3 rel){return where==LodgingMeeting.PARLOR?LodgingSite.inParlor(rel):LodgingSite.inDaniels(rel);}

    /** Vera from Daniel's room out through his door (leg 3), up the stair, along the gallery, and in at a guest room's door. */
    static List<Vec3> up(int room){
        int s=room<3?-1:1;double dz=LodgingSite.ROOM_DOORS[room].getZ()+.5;
        return List.of(new Vec3(-5.6,0,-24),new Vec3(-5.6,0,-20),new Vec3(-7.5,0,-18.5),new Vec3(-7.5,0,-14),new Vec3(0,0,-14),new Vec3(0,0,-5),
            new Vec3(3.5,0,-5.5),new Vec3(3.5,5,-12.5),new Vec3(.5,5,-12.5),new Vec3(.5,5,dz),new Vec3(s*2.5,5,dz),new Vec3(s*6.0,5,dz),bedside(room));
    }
    static Map<Integer,BlockPos> upDoors(int room){return Map.of(3,LodgingSite.DANIEL_DOOR,11,LodgingSite.ROOM_DOORS[room]);}
    /** Vera from the upstairs corridor, where she stands between rooms after the first intrusion, along the gallery to a guest room. */
    static List<Vec3> along(int room){
        int s=room<3?-1:1;double dz=LodgingSite.ROOM_DOORS[room].getZ()+.5;
        return List.of(new Vec3(.5,5,-19.5),new Vec3(.5,5,-15),new Vec3(.5,5,dz),new Vec3(s*2.5,5,dz),new Vec3(s*6.0,5,dz),bedside(room));
    }
    static Map<Integer,BlockPos> alongDoors(int room){return Map.of(4,LodgingSite.ROOM_DOORS[room]);}
    /** From the upstairs corridor into the workroom (its door on leg 1). */
    static final List<Vec3> CORRIDOR_TO_WORKROOM=List.of(new Vec3(.5,5,-19.5),new Vec3(-3.5,5,-19.5),new Vec3(-6.5,5,-20.8));
    static final Map<Integer,BlockPos> CORRIDOR_DOORS=Map.of(1,LodgingSite.WORK_DOOR);
    /** Whether Vera stands in the upstairs corridor between the workroom and her own room. */
    static boolean inCorridor(Vec3 rel){return LodgingSite.upstairs(rel)&&rel.x>=-1&&rel.x<2&&rel.z<=-16&&rel.z>=-28;}
    /** Beside a guest room's bed at the pillow end, on the door's side. */
    static Vec3 bedside(int room){var bed=LodgingSite.bed(room);int s=room<3?-1:1;return new Vec3(s<0?-7.5:8.5,5,bed.getZ()+.5);}
    /** Vera's place at the frame, and the double's: in front of the frame, facing it. */
    static final LodgingPeople.Spot VERA_AT=new LodgingPeople.Spot(-6.0,LodgingSite.GROUND,-26.9,140,LodgingPeople.WAIT),
        DOUBLE_AT=new LodgingPeople.Spot(-7.5,LodgingSite.GROUND,-27.45,180,STAND);
    /** Vera from Daniel's room up to the workroom (his door on leg 3, the workroom's on leg 10), to stand by her table. */
    static final List<Vec3> TO_WORKROOM=List.of(new Vec3(-5.6,0,-24),new Vec3(-5.6,0,-20),new Vec3(-7.5,0,-18.5),new Vec3(-7.5,0,-14),new Vec3(0,0,-14),
        new Vec3(0,0,-5),new Vec3(3.5,0,-5.5),new Vec3(3.5,5,-12.5),new Vec3(.5,5,-15),new Vec3(.5,5,-19.5),new Vec3(-3.5,5,-19.5),new Vec3(-6.5,5,-20.8));
    static final Map<Integer,BlockPos> WORKROOM_DOORS=Map.of(3,LodgingSite.DANIEL_DOOR,10,LodgingSite.WORK_DOOR);
    static final LodgingPeople.Spot VERA_WORKROOM=new LodgingPeople.Spot(-6.5,LodgingSite.UPPER,-20.8,90,LodgingPeople.WAIT);

    // ------------------------------------------------------------------------------------------------ state
    static CompoundTag state(CompoundTag house){if(!house.contains(STATE))house.put(STATE,new CompoundTag());return house.getCompound(STATE);}
    static CompoundTag vera(CompoundTag house){if(!house.contains(VERA_STATE))house.put(VERA_STATE,new CompoundTag());return house.getCompound(VERA_STATE);}
    public static int phase(MinecraftServer s,BlockPos b){return state(LodgingStory.house(s,b)).getInt("Phase");}
    public static int veraPhase(MinecraftServer s,BlockPos b){return vera(LodgingStory.house(s,b)).getInt("Phase");}
    public static boolean latched(MinecraftServer s,BlockPos b){return state(LodgingStory.house(s,b)).getBoolean("Latched");}
    /** Whether the failed body at the jib door is this slice's to move just now. */
    public static boolean moving(MinecraftServer s,BlockPos b){int ph=phase(s,b);return ph>=COMING&&ph<OVER;}
    /** Whether one of the four is in the wall just now. */
    public static boolean inWall(MinecraftServer s,BlockPos b,int who){return state(LodgingStory.house(s,b)).getCompound("InWall").contains(""+who);}
    /** The one in its hand, for a case: a reader's UUID, or null. */
    public static @Nullable UUID held(MinecraftServer s,BlockPos b){var in=state(LodgingStory.house(s,b));return in.hasUUID("Held")?in.getUUID("Held"):null;}
    public static int heldActor(MinecraftServer s,BlockPos b){var in=state(LodgingStory.house(s,b));return in.contains("HeldActor")?in.getInt("HeldActor"):-1;}
    private static void go(CompoundTag in,int phase,int clock){in.putInt("Phase",phase);in.putInt("At",clock);}
    /** Vera's trail on her way down, so the body she leads walks where she walked. Not kept over a reload: it starts again from her. */
    private static final Map<BlockPos,ArrayDeque<Vec3>> TRAIL=new HashMap<>();
    static final int TRAIL_BEHIND=12;
    public static void clearAll(){TRAIL.clear();}

    /** Where one of the four is kept by this slice, or null for everybody else's. */
    static @Nullable LodgingPeople.Spot spot(MinecraftServer s,BlockPos b,int who){
        var h=LodgingStory.house(s,b);var in=state(h);
        if(in.getCompound("InWall").contains(""+who))return new LodgingPeople.Spot(LEFT.x,LEFT.y,LEFT.z,0,LodgingPeople.WAIT);
        if(who==LodgingPeople.VERA){int v=vera(h).getInt("Phase");if(v==V_ASK)return VERA_AT;if(v==V_CALLED)return VERA_WORKROOM;}
        return null;
    }

    // ------------------------------------------------------------------------------------------------ every tick
    /** Once a tick for the house (LodgingPeople.tick). */
    static void tick(ServerLevel l,BlockPos b,List<ServerPlayer> here){
        var s=l.getServer();var h=LodgingStory.house(s,b);var in=state(h);int clock=h.getInt("Clock");int ph=in.getInt("Phase");
        if(ph==NONE){
            if(h.getInt(LodgingMeeting.STAGE)!=LodgingMeeting.DONE||LodgingInjury.phase(s,b)!=LodgingInjury.OVER)return;
            go(in,WAITING,clock);in.putInt("Where",h.getInt(LodgingMeeting.WHERE));LodgingStory.save(s,b,h);return;
        }
        boolean changed=disturbance(l,b,h,in,here,clock);
        changed|=wall(l,b,h,in,clock);
        changed|=veraTick(l,b,h,here,clock);
        if(changed||clock%20==0)LodgingStory.save(s,b,h);
    }

    /** The body's part. True if anything of the house's record changed. */
    private static boolean disturbance(ServerLevel l,BlockPos b,CompoundTag h,CompoundTag in,List<ServerPlayer> here,int clock){
        int ph=in.getInt("Phase");int into=clock-in.getInt("At");int where=in.getInt("Where");
        var body=LodgingPeople.find(l,b,LodgingPeople.FAILED_SHIRT);
        var legs=route(where);var dr=doors(where);
        switch(ph){
            case WAITING->{
                if(into<AFTER)return false;
                in.putInt("Leg",1);go(in,COMING,clock);
                var jib=b.offset(LodgingSite.JIB);
                l.playSound(null,jib,SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR,SoundSource.NEUTRAL,.6F,.85F);
                for(var p:here)if(!p.isSpectator()&&!LodgingSite.upstairs(rel(b,p))&&!LodgingSite.inCellar(rel(b,p)))LodgingHouse.cue(p,LodgingTexts.TAKEN_COMES);
                return true;
            }
            case COMING->{
                if(body==null){go(in,OVER,clock);return true;}
                body.gesture(LodgingPeople.REACH);
                if(clock<in.getInt("StunUntil"))return false;
                if(walk(l,b,in,"Leg",body,legs,dr,PACE)){in.putInt("ChaseAt",clock);in.remove("EmptySince");go(in,LOOSE,clock);}
                return true;
            }
            case LOOSE->{
                if(body==null){go(in,OVER,clock);return true;}
                body.gesture(LodgingPeople.REACH);
                if(clock<in.getInt("StunUntil"))return false;
                var target=nearest(l,b,where,body,here);
                if(target==null){
                    if(!in.contains("EmptySince"))in.putInt("EmptySince",clock);
                    if(clock-in.getInt("EmptySince")>=EMPTY)turnBack(in,legs.size(),clock);
                    return true;
                }
                in.remove("EmptySince");
                LodgingInjury.step(l,body,target.position(),PACE);
                if(body.distanceTo(target)<=REACH)grip(l,b,in,target,clock);
                else if(clock-in.getInt("ChaseAt")>=CHASE)turnBack(in,legs.size(),clock);
                return true;
            }
            case DRAGGING->{
                if(body==null){release(l,b,in,false);go(in,OVER,clock);return true;}
                var held=heldEntity(l,b,in);
                if(held==null||!held.isAlive()||held instanceof ServerPlayer sp&&(sp.isSpectator()||!here.contains(sp))){release(l,b,in,false);turnBack(in,legs.size(),clock);return true;}
                body.gesture(LodgingPeople.DRAG);
                boolean there=clock>=in.getInt("StunUntil")&&walk(l,b,in,"Leg",body,back(legs),backDoors(legs.size(),dr),DRAG_PACE);
                // The one it holds a step behind it, walked backwards by the arm.
                var behind=body.position().subtract(body.getLookAngle().multiply(1,0,1).normalize().scale(-.9));
                if(held instanceof ServerPlayer sp)sp.teleportTo(l,behind.x,body.getY(),behind.z,sp.getYRot(),sp.getXRot());
                else if(held instanceof LiteraryActor a){a.setPos(behind.x,body.getY(),behind.z);float yaw=LodgingPeople.yawTo(a,body.position());a.setYRot(yaw);a.setYBodyRot(yaw);a.setYHeadRot(yaw);if(a.gesture()!=LodgingPeople.STRAIN)a.gesture(LodgingPeople.STRAIN);}
                if(there)leave(l,b,in,held,clock);
                return true;
            }
            case BACK->{
                if(body==null){go(in,OVER,clock);return true;}
                body.gesture(LodgingPeople.REACH);
                if(walk(l,b,in,"Leg",body,back(legs),backDoors(legs.size(),dr),PACE)){
                    home(body);go(in,OVER,clock);
                }
                return true;
            }
            case OVER->{
                // The jib door swings to behind it once nobody stands in it, and its latch drops on the house's side.
                if(in.getBoolean("Shut"))return false;
                var jib=b.offset(LodgingSite.JIB);
                if(LodgingSite.open(l,b,LodgingSite.JIB)){if(LodgingStory.someoneIn(l,jib,1.2,body))return false;LodgingHouse.toggle(l,b,LodgingSite.JIB);}
                in.putBoolean("Shut",true);
                if(in.getInt("Takes")>0){
                    in.putBoolean("Latched",true);in.putInt("LatchTries",0);
                    l.playSound(null,jib,SoundEvents.IRON_TRAPDOOR_CLOSE,SoundSource.BLOCKS,.5F,1.5F);
                    for(var p:here)if(!p.isSpectator()&&p.distanceToSqr(Vec3.atCenterOf(jib))<=8*8)LodgingHouse.cue(p,LodgingTexts.LATCH_DROPS);
                }
                return true;
            }
            default->{return false;}
        }
    }
    /**
     * Turned round, back the way it came to its place at the jib door: from the room's end of the way if it was in the room; from the
     * point it had last passed if it was on its way in; and from where it is if it was already walking somebody out.
     */
    private static void turnBack(CompoundTag in,int n,int clock){
        int ph=in.getInt("Phase");int leg=in.getInt("Leg");
        if(ph==LOOSE)in.putInt("Leg",0);else if(ph==COMING)in.putInt("Leg",Math.max(0,n-leg));
        go(in,BACK,clock);
    }
    private static void home(LiteraryActor body){var s=LodgingPeople.SPOTS[LodgingPeople.FAILED_SHIRT];body.gesture(LodgingPeople.FRAME);body.setYRot(s.look());body.setYBodyRot(s.look());body.setYHeadRot(s.look());}

    /** Whoever living there is nearest the body in that room: a reader, or one of the four who can be taken (Vera is never). */
    private static @Nullable net.minecraft.world.entity.LivingEntity nearest(ServerLevel l,BlockPos b,int where,LiteraryActor body,List<ServerPlayer> here){
        net.minecraft.world.entity.LivingEntity best=null;double d=Double.MAX_VALUE;
        for(var p:here){if(p.isSpectator()||!p.isAlive()||!inRoom(where,rel(b,p)))continue;double e=p.distanceToSqr(body);if(e<d){d=e;best=p;}}
        var in=state(LodgingStory.house(l.getServer(),b));
        for(int who:new int[]{LodgingPeople.RAE,LodgingPeople.DAN,LodgingPeople.DANIEL}){
            if(who==LodgingPeople.DANIEL&&!LodgingKey.free(l.getServer(),b))continue;
            if(in.getCompound("InWall").contains(""+who))continue;
            var a=LodgingPeople.find(l,b,who);if(a==null||!inRoom(where,rel(b,a)))continue;double e=a.distanceToSqr(body);if(e<d){d=e;best=a;}
        }
        return best;
    }
    private static @Nullable net.minecraft.world.entity.LivingEntity heldEntity(ServerLevel l,BlockPos b,CompoundTag in){
        if(in.hasUUID("Held"))return l.getServer().getPlayerList().getPlayer(in.getUUID("Held"));
        if(in.contains("HeldActor"))return LodgingPeople.find(l,b,in.getInt("HeldActor"));
        return null;
    }
    /** Its hand closes on them: walked back toward the jib door. */
    private static void grip(ServerLevel l,BlockPos b,CompoundTag in,net.minecraft.world.entity.LivingEntity who,int clock){
        in.putInt("Others",0);in.putInt("Own",0);in.putInt("Leg",0);go(in,DRAGGING,clock);
        if(who instanceof ServerPlayer p){in.putUUID("Held",p.getUUID());LodgingHouse.cue(p,LodgingTexts.GRIPPED);}
        else if(who instanceof LiteraryActor a){
            int w=LodgingPeople.who(a);in.putInt("HeldActor",w);a.gesture(LodgingPeople.STRAIN);
            LodgingStory.speak(l,b,a,name(w),LodgingTexts.TAKEN_CRY[w==LodgingPeople.RAE?0:w==LodgingPeople.DAN?1:2]);
            for(var p:LodgingHouse.readers(l,b))if(!p.isSpectator()&&p.distanceToSqr(a)<=16*16&&p.hasLineOfSight(a)){var own=LodgingHouse.own(p);own.putBoolean(SAW_TAKEN,true);LodgingHouse.save(p,own);LodgingHouse.cue(p,LodgingTexts.takenSeen(name(w)));}
        }
    }
    private static void release(ServerLevel l,BlockPos b,CompoundTag in,boolean say){
        if(in.hasUUID("Held")){var p=l.getServer().getPlayerList().getPlayer(in.getUUID("Held"));if(p!=null&&say)LodgingHouse.cue(p,LodgingTexts.GRIP_BROKEN);in.remove("Held");}
        if(in.contains("HeldActor")){var a=LodgingPeople.find(l,b,in.getInt("HeldActor"));if(a!=null)a.gesture(LodgingPeople.WAIT);in.remove("HeldActor");}
    }
    /** At the jib door's passage side: it lets them go there, and goes on, or back for another. */
    private static void leave(ServerLevel l,BlockPos b,CompoundTag in,net.minecraft.world.entity.LivingEntity held,int clock){
        var at=LodgingInjury.abs(b,LEFT);
        if(held instanceof ServerPlayer p){
            p.teleportTo(l,at.x,at.y,at.z,p.getYRot(),p.getXRot());
            var own=LodgingHouse.own(p);own.putBoolean(TAKEN,true);LodgingHouse.save(p,own);LodgingHouse.cue(p,LodgingTexts.TAKEN_AWAKE);
        }else if(held instanceof LiteraryActor a){
            a.setPos(at.x,at.y,at.z);a.gesture(LodgingPeople.WAIT);
            var wall=in.getCompound("InWall");wall.putInt(""+LodgingPeople.who(a),clock);in.put("InWall",wall);
        }
        in.remove("Held");in.remove("HeldActor");
        int takes=in.getInt("Takes")+1;in.putInt("Takes",takes);
        var body=LodgingPeople.find(l,b,LodgingPeople.FAILED_SHIRT);
        // Another, if there is one left in that room, and it has not been driven off; otherwise it is over.
        if(takes<TAKES&&!in.getBoolean("Kept")&&body!=null&&nearest(l,b,in.getInt("Where"),body,LodgingHouse.readers(l,b))!=null){in.putInt("Leg",1);go(in,COMING,clock);}
        else{if(body!=null)home(body);go(in,OVER,clock);}
    }

    /** A blow on the failed body from a reader. True if this slice took it. */
    public static boolean struck(ServerPlayer p,LiteraryActor body){
        var b=LiteraryVignettes.base(p,LodgingPeople.PLACE);if(b==null)return false;var l=p.serverLevel();var s=l.getServer();
        var house=LodgingStory.house(s,b);var in=state(house);int ph=in.getInt("Phase");
        if(ph<COMING||ph>DRAGGING)return false;
        int clock=house.getInt("Clock");
        var own=LodgingHouse.own(p);own.putInt(LodgingInjury.STRUCK,own.getInt(LodgingInjury.STRUCK)+1);LodgingHouse.save(p,own);
        int blows=in.getInt("Blows")+1;in.putInt("Blows",blows);in.putInt("StunUntil",clock+STUN);
        var away=body.position().subtract(p.position()).multiply(1,0,1);
        if(away.lengthSqr()>1e-4){var to=body.position().add(away.normalize().scale(.5));if(!l.getBlockCollisions(body,body.getBoundingBox().move(to.subtract(body.position())).inflate(.02,-.02,.02)).iterator().hasNext())body.setPos(to.x,body.getY(),to.z);}
        l.playSound(null,body.blockPosition(),SoundEvents.PLAYER_ATTACK_WEAK,SoundSource.PLAYERS,.8F,.8F);
        if(ph==DRAGGING){
            boolean self=in.hasUUID("Held")&&in.getUUID("Held").equals(p.getUUID());
            int n=in.getInt(self?"Own":"Others")+1;in.putInt(self?"Own":"Others",n);
            // Its hand off them, it goes on back the way it was going, empty-handed.
            if(n>=(self?FREE_SELF:FREE_OTHERS)){release(l,b,in,true);in.putInt("StunUntil",clock+STUN*2);turnBack(in,0,clock);}
        }
        int n=route(in.getInt("Where")).size();
        if(blows>=KEEP_OFF){
            in.putBoolean("Kept",true);
            if(in.getInt("Phase")==DRAGGING)release(l,b,in,true);
            if(in.getInt("Phase")!=BACK)turnBack(in,n,clock);
        }else LodgingHouse.cue(p,LodgingTexts.STRUCK_FAILED);
        LodgingStory.save(s,b,house);
        return true;
    }

    // ------------------------------------------------------------------------------------------------ in the passages
    /** The four left in the wall: out once the latch is lifted (or by their own hands after a while), back to where they were. */
    private static boolean wall(ServerLevel l,BlockPos b,CompoundTag h,CompoundTag in,int clock){
        var wall=in.getCompound("InWall");var going=in.getCompound("Going");if(wall.isEmpty()&&going.isEmpty())return false;boolean changed=false;
        // While it is still about, they stay where it left them; once it is over, out as soon as the latch is up, or by their own hands.
        boolean over=in.getInt("Phase")==OVER&&in.getBoolean("Shut");
        for(var key:List.copyOf(wall.getAllKeys())){
            int who=Integer.parseInt(key);var a=LodgingPeople.find(l,b,who);if(a==null||!over)continue;
            boolean waited=clock-wall.getInt(key)>=WALL_WAIT;
            if(in.getBoolean("Latched")&&!waited)continue;
            if(in.getBoolean("Latched")){lift(l,b,in);LodgingStory.speak(l,b,a,name(who),LodgingTexts.WORKED_IT);}
            wall.remove(key);going.putBoolean(key,true);in.putInt("Out"+who,1);
            LodgingStory.speak(l,b,a,name(who),LodgingTexts.OUT_OF_WALL[who==LodgingPeople.RAE?0:who==LodgingPeople.DAN?1:2]);changed=true;
        }
        for(var key:List.copyOf(going.getAllKeys())){
            int who=Integer.parseInt(key);var a=LodgingPeople.find(l,b,who);if(a==null)continue;
            // Out through the jib door and back the way it brought them, to where they belong now.
            var legs=new ArrayList<Vec3>();legs.add(LEFT);legs.addAll(route(in.getInt("Where")));
            var to=LodgingKey.spot(l.getServer(),b,LodgingStory.stage(l.getServer(),b),who);legs.add(new Vec3(to.x(),to.y(),to.z()));
            var dr=new HashMap<Integer,BlockPos>();doors(in.getInt("Where")).forEach((k,v)->dr.put(k+1,v));
            if(walk(l,b,in,"Out"+who,a,legs,dr,ACTOR_PACE)){LodgingPeople.settle(a,to);a.setYRot(to.look());a.setYBodyRot(to.look());going.remove(key);in.remove("Out"+who);}
            else if(a.gesture()!=LodgingPeople.WAIT)a.gesture(LodgingPeople.WAIT);
            changed=true;
        }
        in.put("InWall",wall);in.put("Going",going);
        return changed;
    }
    private static void lift(ServerLevel l,BlockPos b,CompoundTag in){
        in.putBoolean("Latched",false);
        if(!LodgingSite.open(l,b,LodgingSite.JIB))LodgingHouse.toggle(l,b,LodgingSite.JIB);
        l.playSound(null,b.offset(LodgingSite.JIB),SoundEvents.IRON_TRAPDOOR_OPEN,SoundSource.BLOCKS,.5F,1.5F);
    }
    /** A hand on the jib door while its latch is down. True if this slice answered it. */
    static boolean jib(ServerPlayer p,BlockPos b){
        var l=p.serverLevel();var s=p.server;var h=LodgingStory.house(s,b);var in=state(h);if(!in.getBoolean("Latched"))return false;
        var own=LodgingHouse.own(p);
        if(!LodgingSite.inPassages(rel(b,p))){
            lift(l,b,in);own.putBoolean(LIFTED_LATCH,true);LodgingHouse.save(p,own);LodgingHouse.cue(p,LodgingTexts.LATCH_FROM_HOUSE);LodgingStory.save(s,b,h);return true;
        }
        if(!p.isShiftKeyDown()){LodgingHouse.cue(p,LodgingTexts.LATCH_INSIDE);return true;}
        int tries=in.getInt("LatchTries")+1;in.putInt("LatchTries",tries);
        l.playSound(null,b.offset(LodgingSite.JIB),SoundEvents.CHAIN_HIT,SoundSource.BLOCKS,.4F,1.6F+tries*.05F);
        if(tries>=LATCH_TRIES){lift(l,b,in);own.putBoolean(WORKED_LATCH,true);LodgingHouse.cue(p,LodgingTexts.LATCH_WORKED);}
        else LodgingHouse.cue(p,LodgingTexts.LATCH_TRY);
        LodgingHouse.save(p,own);LodgingStory.save(s,b,h);return true;
    }
    /** A hand on the lath from inside the wall: a knock, carried into the room on the other side. True if it was a knock. */
    static boolean knock(ServerPlayer p,BlockPos b,BlockPos rel){
        var r=rel(b,p);if(!LodgingSite.inPassages(r))return false;
        var l=p.serverLevel();var s=p.server;var h=LodgingStory.house(s,b);var in=state(h);int clock=h.getInt("Clock");
        var at=b.offset(rel);l.playSound(null,at,SoundEvents.WOOD_HIT,SoundSource.BLOCKS,.9F,.75F);
        var own=LodgingHouse.own(p);own.putBoolean(KNOCKED,true);LodgingHouse.cue(p,LodgingTexts.KNOCK);
        boolean fresh=clock-in.getInt("KnockAt")>=KNOCK_GAP||!in.contains("KnockAt");in.putInt("KnockAt",clock);
        if(fresh){
            var c=Vec3.atCenterOf(at);
            for(var q:LodgingHouse.readers(l,b)){
                if(q==p||q.isSpectator()||LodgingSite.inPassages(rel(b,q))||q.position().distanceToSqr(c)>KNOCK_CARRIES*KNOCK_CARRIES)continue;
                var qo=LodgingHouse.own(q);qo.putBoolean(HEARD_KNOCK,true);LodgingHouse.save(q,qo);LodgingHouse.cue(q,LodgingTexts.HEARD_KNOCK);
            }
            for(int who:new int[]{LodgingPeople.RAE,LodgingPeople.DAN}){
                var a=LodgingPeople.find(l,b,who);if(a==null||in.getCompound("InWall").contains(""+who)||a.position().distanceToSqr(c)>KNOCK_CARRIES*KNOCK_CARRIES)continue;
                String k="Said"+who;if(in.getBoolean(k))continue;in.putBoolean(k,true);LodgingStory.speak(l,b,a,name(who),who==LodgingPeople.RAE?LodgingTexts.RAE_WALL:LodgingTexts.DAN_WALL);
            }
        }
        // While it is in a room with nobody in its hand, it lets the room be and goes back toward the knocking.
        int ph=in.getInt("Phase");
        if(ph==LOOSE){
            own.putBoolean(DREW_OFF,true);turnBack(in,0,clock);LodgingHouse.cue(p,LodgingTexts.DREW_OFF);
            var body=LodgingPeople.find(l,b,LodgingPeople.FAILED_SHIRT);
            if(body!=null)for(var q:LodgingHouse.readers(l,b))if(q!=p&&!q.isSpectator()&&q.distanceToSqr(body)<=12*12&&q.hasLineOfSight(body))LodgingHouse.cue(q,LodgingTexts.DREW_OFF_SEEN);
        }else if(ph==DRAGGING)in.putInt("StunUntil",clock+STUN*3);
        LodgingHouse.save(p,own);LodgingStory.save(s,b,h);
        return true;
    }
    /** A hatch slid back from inside the wall: seen from the room. */
    static void hatchFromWall(ServerPlayer p,BlockPos b,BlockPos rel){
        if(!LodgingSite.inPassages(rel(b,p)))return;
        var own=LodgingHouse.own(p);own.putBoolean(HATCH_FROM_WALL,true);LodgingHouse.save(p,own);LodgingHouse.cue(p,LodgingTexts.HATCH_SEE);
        var c=Vec3.atCenterOf(b.offset(rel));
        for(var q:LodgingHouse.readers(p.serverLevel(),b))if(q!=p&&!q.isSpectator()&&!LodgingSite.inPassages(rel(b,q))&&q.position().distanceToSqr(c)<=8*8)LodgingHouse.cue(q,LodgingTexts.HATCH_SEEN);
    }

    // ------------------------------------------------------------------------------------------------ Vera
    /** Vera's preparation and her repair, one activity, from what the house told her. True if anything of the house's record changed. */
    private static boolean veraTick(ServerLevel l,BlockPos b,CompoundTag h,List<ServerPlayer> here,int clock){
        var in=state(h);var v=vera(h);int ph=v.getInt("Phase");var vera=LodgingPeople.find(l,b,LodgingPeople.VERA);if(vera==null)return false;
        switch(ph){
            case V_NONE->{
                if(in.getInt("Phase")!=OVER||!in.getBoolean("Shut"))return false;
                v.putInt("Phase",V_WAIT);v.putInt("At",clock);return true;
            }
            case V_WAIT->{
                if(clock-v.getInt("At")<VERA_AFTER||clock%20!=0)return false;
                // From Daniel's room (the pin, or her own attempt) or the corridor upstairs (between rooms); anywhere else she is still on her way.
                var vr=rel(b,vera);boolean upper=inCorridor(vr);if(!upper&&!LodgingSite.inDaniels(vr))return false;v.putBoolean("Upper",upper);
                int help=0,against=0;ServerPlayer pick=null;
                for(var p:here){
                    if(p.isSpectator())continue;int a=LodgingHouse.own(p).getInt(LodgingVera.ANSWER);
                    if(a==LodgingVera.HELP){help++;if(pick==null&&LodgingStory.room(l.getServer(),b,p.getUUID())>=0&&LodgingDoubles.sleeper(l,b,LodgingStory.room(l.getServer(),b,p.getUUID()))!=null)pick=p;}
                    else if(a==LodgingVera.SEND_BACK||a==LodgingVera.REFUSE)against++;
                }
                if(help>against&&pick!=null){
                    v.putInt("Phase",V_UP);v.putUUID("For",pick.getUUID());v.putInt("Room",LodgingStory.room(l.getServer(),b,pick.getUUID()));v.putInt("Leg",0);
                    LodgingStory.speak(l,b,vera,"Vera",LodgingTexts.VERA_FETCH);return true;
                }
                if(against>help){v.putInt("Phase",V_SEND);v.putInt("Leg",0);return true;}
                return false;
            }
            case V_UP->{
                int room=v.getInt("Room");
                boolean upper=v.getBoolean("Upper");
                if(walk(l,b,v,"Leg",vera,upper?along(room):up(room),upper?alongDoors(room):upDoors(room),VERA_PACE)){v.putInt("Phase",V_RAISE);v.putInt("At",clock);vera.gesture(LodgingPeople.REACH);}
                return true;
            }
            case V_RAISE->{
                var body=LodgingDoubles.sleeper(l,b,v.getInt("Room"));
                if(body==null){v.putInt("Phase",V_WAIT);v.putInt("At",clock);return true;}
                var bed=LodgingDoubles.lying(b,v.getInt("Room"));vera.setYRot(LodgingPeople.yawTo(vera,bed));vera.setYBodyRot(vera.getYRot());vera.setYHeadRot(vera.getYRot());
                if(clock-v.getInt("At")<RAISING)return false;
                // Up out of the bed onto its feet, beside her.
                var at=LodgingInjury.abs(b,bedside(v.getInt("Room")));body.gesture(STAND);body.setPos(at.x,at.y,at.z);
                vera.gesture(LodgingPeople.DRAG);
                var legs=back(up(v.getInt("Room")));v.putInt("Phase",V_DOWN);v.putInt("Leg",1);TRAIL.remove(b.immutable());
                l.playSound(null,body.blockPosition(),SoundEvents.WOOL_BREAK,SoundSource.NEUTRAL,.6F,.8F);
                return true;
            }
            case V_DOWN->{
                var body=LodgingDoubles.sleeper(l,b,v.getInt("Room"));
                int room=v.getInt("Room");var legs=back(up(room));var dr=backDoors(legs.size(),upDoors(room));
                legs=new ArrayList<>(legs);legs.add(new Vec3(VERA_AT.x(),VERA_AT.y(),VERA_AT.z()));
                boolean there=walk(l,b,v,"Leg",vera,legs,dr,VERA_PACE);
                vera.gesture(LodgingPeople.DRAG);
                var trail=TRAIL.computeIfAbsent(b.immutable(),k->new ArrayDeque<>());trail.addLast(vera.position());
                if(body!=null){
                    if(body.gesture()!=LED)body.gesture(LED);
                    if(trail.size()>TRAIL_BEHIND){var to=trail.removeFirst();var d=to.subtract(body.position());body.setPos(to.x,to.y,to.z);
                        if(d.horizontalDistanceSqr()>1e-5){float yaw=(float)(Math.atan2(d.z,d.x)*180/Math.PI)-90;body.setYRot(yaw);body.setYBodyRot(yaw);body.setYHeadRot(yaw);}
                        body.walkAnimation.update((float)d.horizontalDistance()*4,.4F);}
                    if(!v.getBoolean("Seen"))for(var p:here)if(!p.isSpectator()&&p.distanceToSqr(body)<=12*12&&p.hasLineOfSight(body)){var own=LodgingHouse.own(p);if(!own.getBoolean(SAW_DOUBLE_LED)){own.putBoolean(SAW_DOUBLE_LED,true);LodgingHouse.save(p,own);LodgingHouse.cue(p,LodgingTexts.DOUBLE_LED);}}
                }
                if(there){
                    TRAIL.remove(b.immutable());vera.gesture(LodgingPeople.WAIT);vera.setYRot(VERA_AT.look());vera.setYBodyRot(VERA_AT.look());
                    if(body!=null){var at=LodgingPeople.world(b,DOUBLE_AT);body.setPos(at.x,at.y,at.z);body.gesture(STAND);body.setYRot(DOUBLE_AT.look());body.setYBodyRot(DOUBLE_AT.look());body.setYHeadRot(DOUBLE_AT.look());}
                    v.putInt("Phase",V_ASK);v.putInt("At",clock);
                }
                return true;
            }
            case V_ASK->{
                var body=LodgingDoubles.sleeper(l,b,v.getInt("Room"));
                if(body!=null){var at=LodgingPeople.world(b,DOUBLE_AT);if(body.position().distanceToSqr(at)>.0004)body.setPos(at.x,at.y,at.z);body.setYRot(DOUBLE_AT.look());body.setYBodyRot(DOUBLE_AT.look());}
                if(v.getBoolean("Asked")||!v.hasUUID("For"))return false;
                var p=l.getServer().getPlayerList().getPlayer(v.getUUID("For"));
                if(p==null||p.isSpectator()||!here.contains(p)||p.distanceToSqr(vera)>8*8||!p.hasLineOfSight(vera))return false;
                v.putBoolean("Asked",true);LodgingHouse.cue(p,LodgingTexts.DOUBLE_YOURS);LodgingStory.speak(l,b,vera,"Vera",LodgingTexts.VERA_ASK_ORIGINAL);
                return true;
            }
            case V_SEND->{
                boolean upper=v.getBoolean("Upper");
                if(walk(l,b,v,"Leg",vera,upper?CORRIDOR_TO_WORKROOM:TO_WORKROOM,upper?CORRIDOR_DOORS:WORKROOM_DOORS,VERA_PACE)){
                    v.putInt("Phase",V_CALLED);v.putInt("At",clock);vera.setYRot(VERA_WORKROOM.look());vera.setYBodyRot(VERA_WORKROOM.look());
                    // From the workroom door she calls down the stair: everybody in the house hears it.
                    vera.say("Vera",LodgingTexts.VERA_CALLS);vera.speaks(LodgingTexts.VERA_CALLS);
                    for(var p:here)if(!p.isSpectator())Talk.line(p,"Vera",LodgingTexts.VERA_CALLS);
                }
                return true;
            }
            default->{return false;}
        }
    }

    /** A hand on Vera while she is at the frame with a body, or in the workroom having called. True if answered here. */
    static boolean talk(ServerPlayer p,BlockPos b,LiteraryActor a,int who,CompoundTag own){
        if(who!=LodgingPeople.VERA)return false;
        var h=LodgingStory.house(p.server,b);var v=vera(h);int ph=v.getInt("Phase");
        if(ph==V_ASK){
            if(!v.hasUUID("For")||!v.getUUID("For").equals(p.getUUID())){LodgingHouse.cue(p,LodgingTexts.VERA_NOT_YOURS);return true;}
            Talk.begin(p,LodgingPeople.TALK+"double","Vera");
            if(v.getBoolean("Answered")){LodgingPeople.says(p,a,"Vera",own.getBoolean(STEADIED)?LodgingTexts.VERA_HOLD_IT:LodgingTexts.VERA_THEN);Talk.end(p);return true;}
            LodgingPeople.says(p,a,"Vera",LodgingTexts.VERA_ASK_ORIGINAL);
            Talk.ask(p,List.of(Talk.reply(LodgingTexts.R_STEADY,r->double_(r,true)),Talk.reply(LodgingTexts.R_NOT_MINE,r->double_(r,false)),Talk.act(LodgingTexts.R_NOTHING,Talk::close)));
            return true;
        }
        if(ph==V_CALLED&&LodgingSite.inWorkroom(rel(b,p))){
            Talk.begin(p,LodgingPeople.TALK+"called","Vera");
            int ans=own.getInt(LodgingVera.ANSWER);
            if(ans!=LodgingVera.SEND_BACK&&ans!=LodgingVera.REFUSE||own.getBoolean(AGREED)||own.getBoolean(REFUSED_VERA)){LodgingPeople.says(p,a,"Vera",LodgingTexts.VERA_NOT_YOU);Talk.end(p);return true;}
            for(var line:LodgingTexts.VERA_SENT_ASK)LodgingPeople.says(p,a,"Vera",line);
            Talk.ask(p,List.of(Talk.reply(LodgingTexts.R_WHEN_YOU_CALL,r->called(r,true)),Talk.reply(LodgingTexts.R_REFUSE_VERA,r->called(r,false)),Talk.act(LodgingTexts.R_NOTHING,Talk::close)));
            return true;
        }
        return false;
    }
    /** Its original's answer: put its hands on the frame, or not. Only theirs. */
    private static void double_(ServerPlayer p,boolean yes){
        var b=LiteraryVignettes.base(p,LodgingPeople.PLACE);if(b==null){Talk.close(p);return;}
        var l=p.serverLevel();var s=p.server;var h=LodgingStory.house(s,b);var v=vera(h);
        if(v.getInt("Phase")!=V_ASK||!v.hasUUID("For")||!v.getUUID("For").equals(p.getUUID())||v.getBoolean("Answered")){Talk.close(p);return;}
        var vera=LodgingPeople.find(l,b,LodgingPeople.VERA);var body=LodgingDoubles.sleeper(l,b,v.getInt("Room"));
        v.putBoolean("Answered",true);var own=LodgingHouse.own(p);
        if(yes){own.putBoolean(STEADIED,true);v.putBoolean("Steadied",true);if(body!=null)body.gesture(STEADY);LodgingHouse.cue(p,LodgingTexts.STEADIED);if(vera!=null)LodgingPeople.says(p,vera,"Vera",LodgingTexts.VERA_HOLD_IT);}
        else{own.putBoolean(REFUSED_DOUBLE,true);if(vera!=null)LodgingPeople.says(p,vera,"Vera",LodgingTexts.VERA_THEN);}
        LodgingHouse.save(p,own);LodgingStory.save(s,b,h);Talk.end(p);
    }
    /** A reader against her, come up when she called: hold Daniel when she calls, or not. */
    private static void called(ServerPlayer p,boolean yes){
        var b=LiteraryVignettes.base(p,LodgingPeople.PLACE);if(b==null){Talk.close(p);return;}
        var l=p.serverLevel();var s=p.server;var h=LodgingStory.house(s,b);var v=vera(h);if(v.getInt("Phase")!=V_CALLED){Talk.close(p);return;}
        var vera=LodgingPeople.find(l,b,LodgingPeople.VERA);var own=LodgingHouse.own(p);
        if(yes){own.putBoolean(AGREED,true);v.putInt("Agreed",v.getInt("Agreed")+1);if(vera!=null)LodgingPeople.says(p,vera,"Vera",LodgingTexts.VERA_SENT_YES);}
        else{own.putBoolean(REFUSED_VERA,true);if(vera!=null)LodgingPeople.says(p,vera,"Vera",LodgingTexts.VERA_SENT_NO);}
        LodgingHouse.save(p,own);LodgingStory.save(s,b,h);Talk.end(p);
    }

    // ------------------------------------------------------------------------------------------------ walking
    /**
     * One of them a step along these legs at this pace; true once at the end. A door is opened as they set out on the leg that crosses
     * it (Daniel's bolt drawn first) and shut behind them once they are through, unless somebody else is in it, or it is Daniel's.
     */
    static boolean walk(ServerLevel l,BlockPos b,CompoundTag st,String key,LiteraryActor a,List<Vec3> legs,Map<Integer,BlockPos> doors,double pace){
        int leg=st.getInt(key);if(leg>=legs.size())return true;
        var door=doors.get(leg);
        if(door!=null){
            var lower=l.getBlockState(b.offset(door));
            if(!LodgingBlock.open(lower)){
                if(LodgingBlock.bolted(lower))LodgingSite.leaf(l,b,door,LodgingBlock.Kind.BOLT_DOOR,lower.getValue(LodgingBlock.FACING),false,0);
                LodgingHouse.toggle(l,b,door);
                // The jib's latch, if it is down, lifted from the house's side by whoever comes through.
                if(door.equals(LodgingSite.JIB))state(LodgingStory.house(l.getServer(),b)).putBoolean("Latched",false);
            }
        }
        if(LodgingInjury.step(l,a,legs.get(leg).add(b.getX(),b.getY(),b.getZ()),pace)){
            st.putInt(key,leg+1);
            if(door!=null&&!door.equals(LodgingSite.DANIEL_DOOR)&&!door.equals(LodgingSite.JIB)&&LodgingBlock.open(l.getBlockState(b.offset(door)))&&!LodgingStory.someoneIn(l,b.offset(door),1.6))LodgingHouse.toggle(l,b,door);
            return leg+1>=legs.size();
        }
        return false;
    }

    static Vec3 rel(BlockPos b,net.minecraft.world.entity.Entity e){return LodgingInjury.rel(b,e);}
    static String name(int who){return switch(who){case LodgingPeople.RAE->"Rae";case LodgingPeople.DAN->"Dan";case LodgingPeople.VERA->"Vera";default->"Daniel";};}
    /** For a house built again: nothing of this slice's. */
    public static void clear(MinecraftServer s,BlockPos b){var h=LodgingStory.house(s,b);h.remove(STATE);h.remove(VERA_STATE);TRAIL.remove(b.immutable());}
    /** Whether a reader's body is up out of its bed (Vera has it): LodgingDoubles leaves it alone. */
    public static boolean up(LiteraryActor a){return a.gesture()>=STAND;}
}
