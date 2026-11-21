package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.network.HouseFadePayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
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
 */
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

    /** One tick of night four's hunt: tells the one who went in that the bus is due, once, if it is for them. */
    static void tick(ServerLevel l,TheListNight.Night n,List<ServerPlayer> here){
        if(n.time<DUE||n.slasher==null)return;
        for(var q:here){
            if(!q.getUUID().equals(n.slasher)||!LiteraryVignettes.participant(q)||!runaway(q))continue;
            if(TOLD.computeIfAbsent(n.base.asLong(),k->new HashSet<>()).add(q.getUUID()))TheList.cue(q,TheListTexts.BUS_DUE);
        }
    }
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
    static void clearAll(){TOLD.clear();}
}
