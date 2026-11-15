package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.604, The Ones Already Inside, its fourth slice (docs/THE_ONES_ALREADY_INSIDE_PLAN.md: "N1 and N2: Rae's request and the rooms,
 * the doubles in the beds, the first eyes opening"). The house's own chapter, kept with the house and the same for everybody in it:
 *
 * <ol>
 * <li><b>ASKING</b>: Rae asks each reader who comes in (LodgingPeople). When every reader in the house has answered her, or a minute
 * of the house's time after she first asked, she says she will find Vera and goes.</li>
 * <li><b>RAE_UP</b>: she walks up the stair and along the corridor to the workroom (she knows the way: the script's contradiction a
 * reader can notice), opening the workroom's door and shutting it behind her.</li>
 * <li><b>FETCHING</b>: a moment out of sight.</li>
 * <li><b>COMING_DOWN</b>: Vera comes down to the hall with Rae behind her, the workroom's door opened and shut again.</li>
 * <li><b>ADDRESS</b>: Vera's four lines (N2), over her head for anybody near. Every reader in the house is given a guest room, their
 * name on the card at its door, and a body asleep in its bed (LodgingDoubles). Her last line is to Dan.</li>
 * <li><b>UPSTAIRS</b>: Vera takes Dan up to the workroom; Rae goes with them. Dan, passing a guest room whose door stands open on its
 * sleeper, says his line once.</li>
 * <li><b>SETTLED</b>: the three of them in the workroom.</li>
 * <li><b>DOWN</b> (0.4.606): Vera steps out of the workroom into the corridor, between rooms; Dan and Rae come back down to the hall.</li>
 * <li><b>INJURY</b>: N4 (LodgingInjury): the failed body comes through the jib door.</li>
 * <li><b>AFTER</b>: it is over. If nothing was cut, the pin is still to come.</li>
 * <li><b>PIN</b>: Vera comes down and takes Dan to Daniel's frame, drawing his bolt on the way.</li>
 * <li><b>PINNED</b>: the pin done, the two of them by the frame.</li>
 * </ol>
 *
 * <p>The house's clock is its own and counts only while somebody is reading in it, so everybody leaving holds the chapter where it
 * is. The walking is the body's own (SceneFootpaths): over the floor and up the treads, never a step through a wall; a door on the
 * way is opened as it is reached and shut once everybody is through. A reader who comes in after the rooms are given out has a room
 * of their own given them when they arrive, while there is one.
 */
public final class LodgingStory {
    private LodgingStory(){}
    public static final int ASKING=0,RAE_UP=1,FETCHING=2,COMING_DOWN=3,ADDRESS=4,UPSTAIRS=5,SETTLED=6,DOWN=7,INJURY=8,AFTER=9,PIN=10,PINNED=11;
    /** 0.4.606: how long the three are settled in the workroom before Vera steps out and the others come down. */
    public static final int SETTLE=400;
    /** How long after Rae first asks anybody she waits before she goes, whoever has answered: the script's minute. */
    public static final int ASK_WAIT=1200;
    /** How long she is out of sight in the workroom before Vera comes down. */
    public static final int FETCH=60;
    /** Between Vera's lines in the hall. */
    public static final int LINE_GAP=70;
    /** How fast they walk: a little under a reader's walk. */
    public static final double PACE=.12;

    // ------------------------------------------------------------------------------------------------ the house's record
    private static CompoundTag all(net.minecraft.server.MinecraftServer s){return LiteraryVignettes.shared(LabyrinthData.get(s),LodgingPeople.PLACE);}
    private static String key(BlockPos b){return "House"+b.asLong();}
    /** This house's chapter record (one per copy of the house, by where it stands). */
    public static CompoundTag house(net.minecraft.server.MinecraftServer s,BlockPos b){var a=all(s);var k=key(b);if(!a.contains(k))a.put(k,new CompoundTag());return a.getCompound(k);}
    public static void save(net.minecraft.server.MinecraftServer s,BlockPos b,CompoundTag h){var a=all(s);a.put(key(b),h);LiteraryVignettes.shared(LabyrinthData.get(s),LodgingPeople.PLACE,a);}
    public static int stage(net.minecraft.server.MinecraftServer s,BlockPos b){return house(s,b).getInt("Stage");}
    /** Forget this house's chapter: for a house built again from nothing, and for tests. */
    public static void reset(net.minecraft.server.MinecraftServer s,BlockPos b){var a=all(s);a.remove(key(b));LiteraryVignettes.shared(LabyrinthData.get(s),LodgingPeople.PLACE,a);LodgingDoubles.forget(b);}

    /** The guest room given to this reader at this house, or -1. */
    public static int room(net.minecraft.server.MinecraftServer s,BlockPos b,UUID reader){var r=house(s,b).getCompound("Rooms");return r.contains(reader.toString())?r.getInt(reader.toString()):-1;}
    /** Every reader given a room here, and which. */
    public static List<UUID> guests(net.minecraft.server.MinecraftServer s,BlockPos b){var out=new ArrayList<UUID>();for(var k:house(s,b).getCompound("Rooms").getAllKeys())out.add(UUID.fromString(k));return out;}
    public static String guestName(net.minecraft.server.MinecraftServer s,BlockPos b,UUID reader){return house(s,b).getCompound("Names").getString(reader.toString());}

    /** Rae has asked somebody for the first time: the minute starts. */
    static void asked(ServerPlayer p){
        var b=LiteraryVignettes.base(p,LodgingPeople.PLACE);if(b==null)return;var h=house(p.server,b);
        if(!h.contains("AskedAt")){h.putInt("AskedAt",h.getInt("Clock"));save(p.server,b,h);}
    }

    // ------------------------------------------------------------------------------------------------ where everybody is now
    /** Where each of the four stands and what they are doing, for the chapter the house is in (LodgingPeople.SPOTS before Rae goes). */
    public static LodgingPeople.Spot spot(int stage,int who){
        if(who==LodgingPeople.RAE){
            if(stage>=RAE_UP&&stage<=FETCHING||stage>=UPSTAIRS&&stage<DOWN)return new LodgingPeople.Spot(-5.5,LodgingSite.UPPER,-19.5,-90,LodgingPeople.WAIT);
            if(stage==COMING_DOWN||stage==ADDRESS)return new LodgingPeople.Spot(2.6,LodgingSite.GROUND,-5.4,0,LodgingPeople.WAIT);
        }
        if(who==LodgingPeople.VERA&&(stage==COMING_DOWN||stage==ADDRESS))return new LodgingPeople.Spot(1.5,LodgingSite.GROUND,-4.4,0,LodgingPeople.WAIT);
        if(who==LodgingPeople.DAN&&stage>=UPSTAIRS&&stage<DOWN)return new LodgingPeople.Spot(-6.5,LodgingSite.UPPER,-23.5,180,LodgingPeople.POCKET);
        // 0.4.606: Vera between rooms in the corridor; then, for the pin, Vera and Dan at Daniel's frame.
        if(who==LodgingPeople.VERA&&stage>=DOWN&&stage<PIN)return VERA_BETWEEN;
        if(who==LodgingPeople.DAN&&stage>=DOWN&&stage<PIN)return DAN_HALL;
        if(who==LodgingPeople.VERA&&stage>=PIN)return VERA_FRAME;
        if(who==LodgingPeople.DAN&&stage>=PIN)return DAN_FRAME;
        return LodgingPeople.SPOTS[who];
    }
    /** 0.4.606: Vera in the corridor between the workroom and her own room; Vera and Dan either side of Daniel's frame. */
    public static final LodgingPeople.Spot VERA_BETWEEN=new LodgingPeople.Spot(.5,LodgingSite.UPPER,-21.6,0,LodgingPeople.WAIT),
        VERA_FRAME=new LodgingPeople.Spot(-6.0,LodgingSite.GROUND,-26.9,140,LodgingPeople.WAIT),
        /** Dan back down in the hall, standing in front of the bench, facing into it. */
        DAN_HALL=new LodgingPeople.Spot(2.4,LodgingSite.GROUND,-4.6,90,LodgingPeople.POCKET),DAN_FRAME=new LodgingPeople.Spot(-9.4,LodgingSite.GROUND,-26.6,-137,LodgingPeople.POCKET);
    /** Vera out of the workroom into the corridor (the door crossed on the second leg). */
    private static final List<Vec3> VERA_OUT=List.of(new Vec3(-3.5,5,-19.5),new Vec3(.5,5,-19.5));
    /** Vera from the corridor down to the hall, and on through the back passage and Daniel's door to his frame (the door on leg 7). */
    private static final List<Vec3> VERA_TO_FRAME=List.of(new Vec3(.5,5,-19.5),new Vec3(.5,5,-15),new Vec3(3.5,5,-12.5),new Vec3(3.5,0,-5.5),new Vec3(0,0,-5.5),
        new Vec3(0,0,-14),new Vec3(-7.5,0,-14),new Vec3(-7.5,0,-18.5),new Vec3(-5.6,0,-20),new Vec3(-5.6,0,-26.9));
    /** Dan from the hall to the frame (the door on leg 4). Both keep west of the stair's newel post. */
    private static final List<Vec3> DAN_TO_FRAME=List.of(new Vec3(0,0,-5),new Vec3(0,0,-14),new Vec3(-7.5,0,-14),new Vec3(-7.5,0,-18.5),new Vec3(-9.4,0,-20),new Vec3(-9.4,0,-26.6));
    /** The way each walks from the hall to the workroom: the stair's open side, the gallery, the corridor, the workroom's door. */
    private static final Vec3[] UP={new Vec3(3.5,0,-5.5),new Vec3(3.5,5,-12.5),new Vec3(.5,5,-15),new Vec3(.5,5,-19.5),new Vec3(-3.5,5,-19.5)};

    // ------------------------------------------------------------------------------------------------ every tick
    /** Houses whose chapter a test holds where it is, to look at one scene without the next beginning (fixtures only). */
    private static final java.util.Set<BlockPos> HELD=new java.util.HashSet<>();
    public static void clearAll(){HELD.clear();}
    public static void hold(BlockPos b,boolean held){if(held)HELD.add(b.immutable());else HELD.remove(b.immutable());}
    /** Once a tick for the house, with its readers (LodgingPeople.tick). */
    static void tick(ServerLevel l,BlockPos b,List<ServerPlayer> here){
        if(HELD.contains(b))return;
        var s=l.getServer();var h=house(s,b);int clock=h.getInt("Clock")+1;h.putInt("Clock",clock);int stage=h.getInt("Stage");
        switch(stage){
            case ASKING->{
                if(h.contains("AskedAt")&&(clock-h.getInt("AskedAt")>=ASK_WAIT||everyoneAnswered(here))){
                    var rae=LodgingPeople.find(l,b,LodgingPeople.RAE);if(rae!=null){rae.say("Rae",LodgingTexts.RAE_GOES);rae.speaks(LodgingTexts.RAE_GOES);}
                    stage=next(h,RAE_UP,clock);
                }
            }
            case RAE_UP->{if(walk(l,b,h,LodgingPeople.RAE,true,spot(FETCHING,LodgingPeople.RAE)))stage=next(h,FETCHING,clock);}
            // 0.4.608: out of sight in the workroom, she takes the key off its hook.
            case FETCHING->{if(clock-h.getInt("StageAt")>=FETCH){LodgingKey.taken(l,b,h);stage=next(h,COMING_DOWN,clock);}}
            case COMING_DOWN->{
                boolean vera=walk(l,b,h,LodgingPeople.VERA,false,spot(COMING_DOWN,LodgingPeople.VERA));
                // Rae a few paces behind her.
                boolean rae=clock-h.getInt("StageAt")<30||walk(l,b,h,LodgingPeople.RAE,false,spot(COMING_DOWN,LodgingPeople.RAE));
                if(vera&&rae&&clock-h.getInt("StageAt")>=30)stage=next(h,ADDRESS,clock);
            }
            case ADDRESS->{
                int line=h.getInt("Line");var vera=LodgingPeople.find(l,b,LodgingPeople.VERA);
                if(line==0)give(l,b,h,here);
                if(vera!=null&&clock-h.getInt("StageAt")>=line*LINE_GAP){
                    if(line<LodgingTexts.VERA_ROOMS.length){
                        var text=LodgingTexts.VERA_ROOMS[line];vera.say("Vera",text);vera.speaks(text);
                        // Her last line is to Dan.
                        if(line==LodgingTexts.VERA_ROOMS.length-1){var dan=LodgingPeople.find(l,b,LodgingPeople.DAN);if(dan!=null){dan.gesture(LodgingPeople.WAIT);h.putBoolean("DanUp",true);}}
                        h.putInt("Line",line+1);
                    }else stage=next(h,UPSTAIRS,clock);
                }
            }
            case UPSTAIRS->{
                boolean vera=walk(l,b,h,LodgingPeople.VERA,true,spot(SETTLED,LodgingPeople.VERA));
                boolean dan=clock-h.getInt("StageAt")<20||walk(l,b,h,LodgingPeople.DAN,true,spot(SETTLED,LodgingPeople.DAN));
                boolean rae=clock-h.getInt("StageAt")<40||walk(l,b,h,LodgingPeople.RAE,true,spot(SETTLED,LodgingPeople.RAE));
                danSees(l,b,h);
                if(vera&&dan&&rae){var v=LodgingPeople.find(l,b,LodgingPeople.VERA);if(v!=null)v.gesture(LodgingPeople.WORK);var d=LodgingPeople.find(l,b,LodgingPeople.DAN);if(d!=null)d.gesture(LodgingPeople.POCKET);stage=next(h,SETTLED,clock);}
            }
            case SETTLED->{if(clock-h.getInt("StageAt")>=SETTLE)stage=next(h,DOWN,clock);}
            case DOWN->{
                // 0.4.606: Vera out into the corridor, then Dan and Rae down the stair to the hall.
                boolean vera=walkVia(l,b,h,LodgingPeople.VERA,VERA_OUT,VERA_BETWEEN,LodgingSite.WORK_DOOR,1);
                boolean dan=clock-h.getInt("StageAt")<40||walk(l,b,h,LodgingPeople.DAN,false,spot(DOWN,LodgingPeople.DAN));
                boolean rae=clock-h.getInt("StageAt")<70||walk(l,b,h,LodgingPeople.RAE,false,spot(DOWN,LodgingPeople.RAE));
                if(vera&&dan&&rae){LodgingInjury.begin(h,clock);stage=next(h,INJURY,clock);}
            }
            case INJURY->{if(LodgingInjury.tick(l,b,h,here,clock))stage=next(h,AFTER,clock);}
            // 0.4.608: nobody brings Dan down to an empty frame.
            case AFTER->{LodgingInjury.tick(l,b,h,here,clock);if(LodgingInjury.pinDue(h,clock)&&!h.getBoolean(LodgingKey.FREE)&&h.getInt(LodgingMeeting.STAGE)==LodgingMeeting.NONE)stage=next(h,PIN,clock);}
            case PIN->{
                // Vera down and through the back passage; Dan after her once she is in the hall.
                boolean vera=walkVia(l,b,h,LodgingPeople.VERA,VERA_TO_FRAME,VERA_FRAME,LodgingSite.DANIEL_DOOR,7);
                boolean dan=h.getInt("Leg"+LodgingPeople.VERA)<5||walkVia(l,b,h,LodgingPeople.DAN,DAN_TO_FRAME,DAN_FRAME,LodgingSite.DANIEL_DOOR,3);
                if(vera&&dan){
                    var v=LodgingPeople.find(l,b,LodgingPeople.VERA);var d=LodgingPeople.find(l,b,LodgingPeople.DAN);
                    if(!h.contains("PinAt")){h.putInt("PinAt",clock);if(v!=null)v.gesture(LodgingPeople.PIN);if(d!=null){d.gesture(LodgingPeople.SHOW);LodgingPeople.showHand(b,l.getGameTime()+LodgingPeople.SHOWN+30);}}
                    else if(clock-h.getInt("PinAt")>=30){LodgingInjury.pin(l,b);if(v!=null)v.gesture(LodgingPeople.WAIT);stage=next(h,PINNED,clock);}
                }
            }
            case PINNED->{LodgingInjury.tick(l,b,h,here,clock);var d=LodgingPeople.find(l,b,LodgingPeople.DAN);if(d!=null&&d.gesture()==LodgingPeople.SHOW&&clock-h.getInt("StageAt")>=LodgingPeople.SHOWN)d.gesture(LodgingPeople.POCKET);}
            default->{}
        }
        // A reader who has come in since the rooms were given out is given one.
        if(stage>=ADDRESS&&h.getInt("Line")>0)for(var p:here)if(!p.isSpectator()&&!h.getCompound("Rooms").contains(p.getUUID().toString()))assign(l,b,h,p);
        save(s,b,h);
    }
    private static int next(CompoundTag h,int stage,int clock){h.putInt("Stage",stage);h.putInt("StageAt",clock);for(int who=0;who<4;who++)h.remove("Leg"+who);return stage;}
    /** Whether every reader in the house has given Rae their answer, and there is at least one. */
    private static boolean everyoneAnswered(List<ServerPlayer> here){
        int counted=0;
        for(var p:here){if(p.isSpectator())continue;counted++;if(LodgingHouse.own(p).getInt(LodgingPeople.RAE_ANSWER)==LodgingPeople.UNDECIDED)return false;}
        return counted>0;
    }
    /** 0.4.608: one of them says this aloud, if a reader is near enough to hear it (the cast checks); otherwise they say nothing. */
    static void speak(ServerLevel l,BlockPos b,LiteraryActor a,String name,String line){
        if(LodgingHouse.readers(l,b).stream().noneMatch(p->!p.isSpectator()&&p.distanceToSqr(a)<=LodgingDaniel.HEARD*LodgingDaniel.HEARD))return;
        a.say(name,line);a.speaks(line);
    }
    /** One of them a step along their way; true once there. Each leg is walked in turn; the workroom door opened on the way through. */
    private static boolean walk(ServerLevel l,BlockPos b,CompoundTag h,int who,boolean up,LodgingPeople.Spot to){
        var legs=new ArrayList<Vec3>();if(up){for(var v:UP)legs.add(v);}else{for(int i=UP.length-1;i>=0;i--)legs.add(UP[i]);}
        return walkVia(l,b,h,who,legs,to,LodgingSite.WORK_DOOR,up?UP.length-1:1);
    }
    /**
     * 0.4.606: one of them a step along these legs and then to their spot; the door crossed on the given leg opened as they reach it
     * (its bolt drawn first, if it is Daniel's) and shut behind them, unless it is Daniel's, which is left standing open.
     */
    private static boolean walkVia(ServerLevel l,BlockPos b,CompoundTag h,int who,List<Vec3> legs,LodgingPeople.Spot to,BlockPos door,int doorLeg){return walkVia(l,b,h,"Leg"+who,who,legs,to,door,doorLeg);}
    /** As above, counting the legs under this key (0.4.608: the key's errands, kept apart from the chapter's walks). A door leg of -1 crosses no door. */
    static boolean walkVia(ServerLevel l,BlockPos b,CompoundTag h,String legKey,int who,List<Vec3> legs,LodgingPeople.Spot to,BlockPos door,int doorLeg){
        var a=LodgingPeople.find(l,b,who);if(a==null)return false;
        int leg=h.getInt(legKey);var route=new ArrayList<>(legs);route.add(new Vec3(to.x(),to.y(),to.z()));
        if(leg>=route.size()){LodgingPeople.settle(a,to);return true;}
        var target=route.get(leg).add(b.getX(),b.getY(),b.getZ());
        // The door stands open while anybody is on the legs either side of it.
        boolean atDoor=leg==doorLeg;boolean daniels=door.equals(LodgingSite.DANIEL_DOOR);
        var lower=l.getBlockState(b.offset(door));boolean open=LodgingBlock.open(lower);
        if(atDoor&&!open&&LodgingBlock.bolted(lower))LodgingSite.leaf(l,b,door,LodgingBlock.Kind.BOLT_DOOR,lower.getValue(LodgingBlock.FACING),false,0);
        if(atDoor&&!open)LodgingHouse.toggle(l,b,door);
        if(a.gesture()!=LodgingPeople.WAIT&&a.gesture()!=LodgingPeople.POCKET)a.gesture(LodgingPeople.WAIT);
        // Every leg of the way is a straight line laid down here, the flight included, walked on whatever is underfoot.
        if(climb(l,a,target)){
            h.putInt(legKey,leg+1);
            // Through the door and clear of it: shut behind them unless somebody else is still to come through.
            if(atDoor&&!daniels&&LodgingBlock.open(l.getBlockState(b.offset(door)))&&!someoneIn(l,b.offset(door),1.6))LodgingHouse.toggle(l,b,door);
        }
        return false;
    }
    /** A step up or down the straight flight toward the far end of it, the feet on whatever is under them there. True once there. */
    private static boolean climb(ServerLevel l,LiteraryActor a,Vec3 target){return climb(l,a,target,PACE);}
    /** As above, at this pace (0.4.606: the failed body's slower walk, Dan's going to it). */
    static boolean climb(ServerLevel l,LiteraryActor a,Vec3 target,double pace){
        var at=a.position();var d=new Vec3(target.x-at.x,0,target.z-at.z);double len=d.length();
        if(len<.05){a.setPos(target.x,support(l,target.x,at.y,target.z),target.z);return true;}
        double step=Math.min(pace,len);double nx=at.x+d.x/len*step,nz=at.z+d.z/len*step;
        // On whatever is highest under the body from heel to toe: up onto a tread as the toes reach it, down only once the heels are off.
        double fx=d.x/len*.21,fz=d.z/len*.21;
        double ny=Math.max(support(l,nx,at.y,nz),Math.max(support(l,nx+fx,at.y,nz+fz),support(l,nx-fx,at.y,nz-fz)));
        // Never a step into anything solid: a body that would be is held where it is (and the case watching it says so).
        var moved=a.getBoundingBox().move(nx-at.x,ny-at.y+.02,nz-at.z).deflate(.1,.02,.1);
        if(l.getBlockCollisions(a,moved).iterator().hasNext()){a.setDeltaMovement(Vec3.ZERO);return false;}
        a.setPos(nx,ny,nz);
        float yaw=(float)(net.minecraft.util.Mth.atan2(d.z,d.x)*net.minecraft.util.Mth.RAD_TO_DEG)-90;a.setYRot(yaw);a.setYBodyRot(yaw);a.setYHeadRot(yaw);
        a.walkAnimation.update((float)step*4,.4F);a.setDeltaMovement(Vec3.ZERO);
        return false;
    }
    /** The top of whatever a body standing here near this height would stand on: a tread, the landing, the floor. */
    static double support(ServerLevel l,double x,double y,double z){
        double best=Double.NEGATIVE_INFINITY;
        for(int by=(int)Math.floor(y+.6);by>=(int)Math.floor(y-1.25);by--){
            var cell=BlockPos.containing(x,by,z);var shape=l.getBlockState(cell).getCollisionShape(l,cell);
            for(var box:shape.toAabbs())if(x-cell.getX()>=box.minX&&x-cell.getX()<=box.maxX&&z-cell.getZ()>=box.minZ&&z-cell.getZ()<=box.maxZ){double top=cell.getY()+box.maxY;if(top<=y+.61&&top>best)best=top;}
            if(best>Double.NEGATIVE_INFINITY)return best;
        }
        return y;
    }
    /** Whether anybody living stands within this of a door's cell (so it is not shut on them). */
    static boolean someoneIn(ServerLevel l,BlockPos cell,double r){return someoneIn(l,cell,r,null);}
    /** As above, but for the one shutting it. */
    static boolean someoneIn(ServerLevel l,BlockPos cell,double r,@javax.annotation.Nullable net.minecraft.world.entity.Entity except){
        var at=net.minecraft.world.phys.Vec3.atCenterOf(cell);
        for(var e:l.getEntities(except,new net.minecraft.world.phys.AABB(at,at).inflate(r,1.5,r),e->e instanceof net.minecraft.world.entity.LivingEntity))return true;
        return false;
    }

    // ------------------------------------------------------------------------------------------------ the rooms
    /** The rooms given out: every reader in the house, in the order they came in. */
    private static void give(ServerLevel l,BlockPos b,CompoundTag h,List<ServerPlayer> here){for(var p:here)if(!p.isSpectator())assign(l,b,h,p);}
    /** A room of their own for this reader, while there is one free: their name on its card. */
    static void assign(ServerLevel l,BlockPos b,CompoundTag h,ServerPlayer p){
        var rooms=h.getCompound("Rooms");String id=p.getUUID().toString();if(rooms.contains(id))return;
        boolean[] taken=new boolean[6];for(var k:rooms.getAllKeys())taken[rooms.getInt(k)]=true;
        int free=-1;for(int i:ORDER)if(!taken[i]){free=i;break;}
        if(free<0)return;
        rooms.putInt(id,free);h.put("Rooms",rooms);
        var names=h.getCompound("Names");names.putString(id,p.getGameProfile().getName());h.put("Names",names);
        LodgingDoubles.card(l,b,free,p.getGameProfile().getName());
    }
    /** The rooms in the order they are given: the front pair first, nearest the stair's head, then back along the gallery. */
    public static final int[] ORDER={3,0,4,1,5,2};

    /** How far along the gallery Dan sees into a room through its open door. */
    public static final double DAN_SEES=11;
    /** Dan at a guest room's open door with its sleeper in the bed: his line, once a house. */
    private static void danSees(ServerLevel l,BlockPos b,CompoundTag h){
        if(h.getBoolean("DanSaw"))return;var dan=LodgingPeople.find(l,b,LodgingPeople.DAN);if(dan==null)return;
        // On the gallery, with a guest room's door standing open along it and its sleeper in the bed.
        if(dan.getY()<b.getY()+LodgingSite.UPPER-.5)return;
        for(int i=0;i<6;i++){
            var door=b.offset(LodgingSite.ROOM_DOORS[i]);if(!LodgingBlock.open(l.getBlockState(door)))continue;
            if(dan.position().distanceToSqr(Vec3.atBottomCenterOf(door))>DAN_SEES*DAN_SEES||LodgingDoubles.sleeper(l,b,i)==null)continue;
            h.putBoolean("DanSaw",true);dan.say("Dan",LodgingTexts.DAN_NOT_HOW);dan.speaks(LodgingTexts.DAN_NOT_HOW);return;
        }
    }
}
