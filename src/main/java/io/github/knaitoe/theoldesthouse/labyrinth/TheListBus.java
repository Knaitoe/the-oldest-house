package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.network.HouseFadePayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jetbrains.annotations.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.728, The list, part 7c (docs/THE_LIST_0_4_728.md): the dawn bus. For the one who went in whom the House has read as running
 * (HOME RUNAWAY), the last night has another way to end: the first bus out stops at the feed store's bench at first light.
 * <ul>
 * <li>Late in night four's hunt ({@link #DUE} on the town's clock), that reader alone is told it is due, once.</li>
 * <li>At first light, if they are standing at the bench ({@link #REACH}), on their feet and not stopped by Stace, they are on it: their
 * outcome is {@code took_the_bus}, the screen goes black, and they are set down in their own room by the tape deck.</li>
 * </ul>
 * Kept per town (who has been told), never saved; a reader told before a restart is told again.
 * <p>
 * 0.4.735 (docs/THE_LIST_0_4_735.md): the bus itself, where the fade was. On a night four whose one who went in is read as running, a
 * coach (TownCar.BUS, tools/draw_list_bus.py) comes in along Main Street from the west end of town from {@link #COME} on the town's
 * clock, slows, and stands in the eastbound lane with its door at the bench from {@link #STANDS}, its lamps on. Everyone in town sees
 * it. At first light, with whoever got on or nobody, it pulls out east and is gone past the tracks ({@link #LEAVE} ticks). Kept per town,
 * never saved: a bus left alone is gone in two seconds, as any of the town's cars is.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class TheListBus{
    private TheListBus(){}
    /** When on the town's clock the bus is due: a little over two minutes of the hunt before first light. */
    public static final long DUE=TheListNight.LIGHT-900;
    /** How near the bench's middle a reader must stand to be on the bus. */
    public static final double REACH=3.5;
    /** The bench's middle, at the curb in front of the feed store, in the town's coordinates. */
    public static final Vec3 BENCH=new Vec3(-35,0,TheListSite.WALK_S_N+.5);
    /** Where one who took it is set down: in their room, between the bed and the desk, facing the tape deck. */
    public static final Vec3 ROOM=new Vec3(10.5,0,-3.5);
    private static final Map<Long,Set<UUID>> TOLD=new HashMap<>();
    /** 0.4.735: on the town's clock, when the bus turns onto Main Street at the west end of town, and when it stands at the bench. */
    public static final long COME=TheListNight.LIGHT-270,STANDS=TheListNight.LIGHT-120;
    /** Where it comes from, where it stands (its door, at the front of its right side, at the bench) and where it goes, in the town's
     * coordinates: the eastbound lane of Main Street, nose east. */
    public static final double FROM_X=TheListSite.WEST+8,STOP_X=BENCH.x-4.375,GONE_X=TheListSite.EAST-6,LANE_Z=-114.4;
    public static final float YAW=-90F;
    /** How long it takes to pull out and be gone, in ticks. */
    public static final int LEAVE=300;
    /** Each town's bus: its body, its level, and when it pulled out (or -1 while it is coming in or standing). */
    static final class Ride{@Nullable UUID car;ResourceKey<Level> level;BlockPos base;long left=-1;Ride(ResourceKey<Level> l,BlockPos b){level=l;base=b;}}
    static final Map<Long,Ride> RIDES=new HashMap<>();

    /** Whether the House has read this reader as running: the only ones the bus is for. */
    public static boolean runaway(ServerPlayer p){
        return "RUNAWAY".equals(HouseRead.value(LabyrinthData.get(p.server),p.getUUID(),HouseRead.Trait.HOME,false));
    }
    /** Whether this reader stands at the bench. */
    public static boolean atBench(ServerPlayer p,BlockPos b){
        var at=Vec3.atLowerCornerOf(b).add(BENCH);double dx=p.getX()-at.x,dz=p.getZ()-at.z;
        return dx*dx+dz*dz<=REACH*REACH&&Math.abs(p.getY()-at.y)<2;
    }
    /** Whether, at first light on night four, this reader gets on: the one who went in, read as running, at the bench, up, not stopped. */
    public static boolean boards(ServerPlayer p,TheListNight.Night n,net.minecraft.nbt.CompoundTag own){
        return p.getUUID().equals(n.slasher)&&LiteraryVignettes.participant(p)&&!own.getBoolean("Stopped")
            &&TheListBody.down(p)==0&&runaway(p)&&atBench(p,n.base);
    }

    /** One tick of night four's hunt: tells the one who went in that the bus is due, once, if it is for them; and the bus comes in. */
    static void tick(ServerLevel l,TheListNight.Night n,List<ServerPlayer> here){
        drive(l,n);
        if(n.time<DUE||n.slasher==null)return;
        for(var q:here){
            if(!q.getUUID().equals(n.slasher)||!LiteraryVignettes.participant(q)||!runaway(q))continue;
            if(TOLD.computeIfAbsent(n.base.asLong(),k->new HashSet<>()).add(q.getUUID()))TheList.cue(q,TheListTexts.BUS_DUE);
        }
    }
    /** Whether a bus comes to this town tonight: its one who went in, here, is read as running. */
    static boolean coming(ServerLevel l,TheListNight.Night n){
        return n.slasher!=null&&l.getServer().getPlayerList().getPlayer(n.slasher) instanceof ServerPlayer q&&runaway(q);
    }
    /** Where the bus stands along the lane at this point on the town's clock: in from the west, slowing to the bench. */
    public static double inX(double time){
        double t=Math.max(0,Math.min(1,(time-COME)/(double)(STANDS-COME))),ease=1-(1-t)*(1-t);
        return FROM_X+(STOP_X-FROM_X)*ease;
    }
    /** And ticks after it pulled out: from the bench, gathering speed, to the east end. */
    public static double outX(long ticks){
        double t=Math.max(0,Math.min(1,ticks/(double)LEAVE));
        return STOP_X+(GONE_X-STOP_X)*t*t;
    }
    /** The bus coming in and standing, told where it is, while it is due tonight. */
    static void drive(ServerLevel l,TheListNight.Night n){
        if(n.time<COME||!coming(l,n))return;
        var r=RIDES.computeIfAbsent(n.base.asLong(),k->new Ride(l.dimension(),n.base));
        if(r.left>=0)return;
        r.car=place(l,r,inX(n.time));
    }
    private static @Nullable UUID place(ServerLevel l,Ride r,double x){
        var at=Vec3.atLowerCornerOf(r.base).add(x,0,LANE_Z);
        if(r.car!=null&&l.getEntity(r.car) instanceof TownCar c&&c.isAlive()){c.place(at,YAW);return r.car;}
        var body=CalderTraffic.make(l,TownCar.BUS,at,YAW);return body==null?null:body.getUUID();
    }
    /** First light, night four: if the bus is here, it pulls out, with whoever got on. */
    static void leave(ServerLevel l,BlockPos b){var r=RIDES.get(b.asLong());if(r!=null&&r.left<0)r.left=l.getGameTime();}
    /** The bus pulling out, wherever its night has got to: told where it is until it is past the tracks, then gone. */
    @SubscribeEvent public static void pulling(ServerTickEvent.Post e){
        if(RIDES.isEmpty())return;
        var it=RIDES.values().iterator();
        while(it.hasNext()){
            var r=it.next();if(r.left<0)continue;
            var l=e.getServer().getLevel(r.level);long t=l==null?LEAVE:l.getGameTime()-r.left;
            if(l==null||t>=LEAVE){if(l!=null&&r.car!=null&&l.getEntity(r.car) instanceof TownCar c)c.discard();it.remove();continue;}
            r.car=place(l,r,outX(t));
        }
    }
    /** The bus in this town now, if there is one (for a check). */
    public static @Nullable TownCar bus(ServerLevel l,BlockPos b){var r=RIDES.get(b.asLong());return r!=null&&r.car!=null&&l.getEntity(r.car) instanceof TownCar c&&c.isAlive()?c:null;}
    /** The town put away: its bus with it. */
    static void drop(ServerLevel l,BlockPos b){var r=RIDES.remove(b.asLong());if(r!=null&&r.car!=null&&l.getEntity(r.car) instanceof TownCar c)c.discard();}

    /** The one who went in on the bus: black, and set down in their room. */
    static void board(ServerPlayer p,BlockPos b){
        HousePackets.send(p,new HouseFadePayload(20,60,40));
        var at=Vec3.atLowerCornerOf(b).add(ROOM);
        p.teleportTo(p.serverLevel(),at.x,at.y,at.z,180F,0F);
    }
    /** Whether this reader has been told the bus is due tonight (for a check). */
    public static boolean told(BlockPos b,UUID id){var t=TOLD.get(b.asLong());return t!=null&&t.contains(id);}
    /** The night put away: who has been told is forgotten. */
    static void reset(BlockPos b){TOLD.remove(b.asLong());}
    static void clearAll(){TOLD.clear();RIDES.clear();}
}
