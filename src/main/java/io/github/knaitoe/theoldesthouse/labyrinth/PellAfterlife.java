package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.543, the Pell house after a failure (docs/PELL_HOUSE_FAILURE_0_4_543.md), from the proposal's "Failure: Tilly answers": "later in
 * the House, now and then, there's a cane tapping behind a door that will not open, and a voice that answers to a name that isn't hers."
 *
 * <ul>
 * <li><b>Whose.</b> A reader whose morning came after Tilly answered (PellEntries.failed), and nobody else: nobody else hears it.</li>
 * <li><b>Where.</b> In an ordinary hall of the House, behind a resident's door: a door the House keeps locked ("The door is locked.
 * Someone lives here."), so it is a door that will not open, and never one the reader needs.</li>
 * <li><b>When.</b> Now and then: one chance in CHANCE each second the reader is in a hall with such a door nearby, and once a House day
 * at most.</li>
 * <li><b>What.</b> A cane tapping its way along behind the door, heard from where it is. When the reader comes to the door, or after a
 * while if they do not, a woman calls "Nessa?", and a child's voice answers her with the words Tilly answered her with in the pool.
 * Then the cane goes away from the door, and there is nothing. Trying the door, or knocking on it, stops it at once: the door stays
 * locked, nobody knocks back, and the tapping stops.</li>
 * </ul>
 * This class also drives the false entries' tick away from the house (PellEntries.anywhere).
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class PellAfterlife {
    private PellAfterlife(){}
    /** One chance in this many, each second a failed reader is in an ordinary hall, that the cane is behind a door near them. */
    public static final int CHANCE=40;
    /** How far a resident's door may be; nearer than NEAR it does not begin (it begins out of reach, and the reader goes to it). */
    public static final int REACH=10,NEAR=3;
    /** Ticks between taps; how long the cane taps before June calls if the reader does not come; from her call to the answer; the cane
     *  going away after it; how near counts as at the door; how far off the reader may go before it stops. */
    public static final int TAP=14,TAPPING=200,ANSWER=30,AWAY=40;
    public static final double AT_DOOR=2.5,LEAVE=24;

    /** One reader's episode: the door, the side the cane is on, and when each part of it came. */
    static final class Cane {
        final ResourceKey<Level> dim;final BlockPos door;final Direction behind;final long start;long called=-1,answered=-1;
        Cane(ResourceKey<Level> dim,BlockPos door,Direction behind,long start){this.dim=dim;this.door=door;this.behind=behind;this.start=start;}
    }
    private static final Map<UUID,Cane> CANES=new HashMap<>();

    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();long now=server.getTickCount();
        if(!CANES.isEmpty())CANES.entrySet().removeIf(en->!step(server,en.getKey(),en.getValue(),now));
        if(now%PellEntries.TRY!=0)return;
        for(var p:server.getPlayerList().getPlayers()){
            if(p.isSpectator()||!p.isAlive()||!HouseDimensions.isHouseDimension(p.level().dimension()))continue;
            PellEntries.anywhere(p);
            maybe(p,now);
        }
    }

    // ------------------------------------------------------------------------------------------------ when it comes
    /** Whether this reader can hear the cane today: the house failed them, and it has not come yet this House day. */
    public static boolean due(ServerPlayer p){
        var own=PellHouse.own(p);return PellEntries.failed(own)&&own.getLong("CaneDay")!=PellEntries.day(p);
    }
    /** In an ordinary hall of the House, and not in the middle of anything: not in a story, not going through a door. */
    static boolean inHall(ServerPlayer p){
        if(!p.level().dimension().equals(HouseDimensions.INTERIOR)||LabyrinthDoors.isBusy(p)||LiteraryVignettes.current(p)!=null)return false;
        var origin=HouseSavedData.get(p.server).houseOrigin();if(origin==null)return false;
        var place=LabyrinthPlaces.placeAt(origin,p.blockPosition());return place!=null&&place.isGray();
    }
    private static void maybe(ServerPlayer p,long now){
        if(CANES.containsKey(p.getUUID())||p.getRandom().nextInt(CHANCE)!=0||!due(p)||!inHall(p))return;
        var door=door(p);if(door!=null)begin(p,door,now);
    }
    /** The nearest resident's door to this reader, shut, within REACH and no nearer than NEAR. */
    public static @Nullable LabyrinthData.Door door(ServerPlayer p){
        var l=p.serverLevel();var data=LabyrinthData.get(p.server);var feet=p.blockPosition();
        LabyrinthData.Door best=null;double near=Double.MAX_VALUE;
        for(var at:BlockPos.betweenClosed(feet.offset(-REACH,-2,-REACH),feet.offset(REACH,2,REACH))){
            var s=l.getBlockState(at);
            if(!(s.getBlock() instanceof DoorBlock)||s.getValue(DoorBlock.HALF)!=DoubleBlockHalf.LOWER||s.getValue(DoorBlock.OPEN))continue;
            var d=data.doorAt(l.dimension(),at);if(d==null||!LabyrinthData.LOCKED.equals(d.destination))continue;
            double dist=Vec3.atBottomCenterOf(d.lower).distanceToSqr(p.position());
            if(dist<NEAR*NEAR||dist>REACH*REACH||dist>=near)continue;
            near=dist;best=d;
        }
        return best;
    }
    /** The cane behind this door, for this reader alone, now; counted as today's. */
    public static void begin(ServerPlayer p,LabyrinthData.Door door,long now){
        var to=p.position().subtract(Vec3.atBottomCenterOf(door.lower));
        boolean front=to.x*door.facing.getStepX()+to.z*door.facing.getStepZ()>=0;
        CANES.put(p.getUUID(),new Cane(p.level().dimension(),door.lower.immutable(),front?door.facing.getOpposite():door.facing,now));
        var own=PellHouse.own(p);own.putLong("CaneDay",PellEntries.day(p));PellHouse.save(p,own);
        PellHouse.cue(p,PellTexts.CANE_BEGINS);
        io.github.knaitoe.theoldesthouse.house.PlaytestLog.event(p,"pell_cane","door",door.id);
    }

    // ------------------------------------------------------------------------------------------------ the episode
    /** One tick of a reader's episode; false when it is over. */
    private static boolean step(MinecraftServer server,UUID id,Cane c,long now){
        var p=server.getPlayerList().getPlayer(id);
        if(p==null||!p.isAlive()||!p.level().dimension().equals(c.dim))return false;
        var at=Vec3.atBottomCenterOf(c.door);
        if(p.position().distanceToSqr(at)>LEAVE*LEAVE)return false;
        var s=p.serverLevel().getBlockState(c.door);if(!(s.getBlock() instanceof DoorBlock)||s.getValue(DoorBlock.OPEN))return false;
        long age=now-c.start;
        if(c.called<0){
            if(age%TAP==0)tap(p,c,(int)(age/TAP),.7F);
            if(age>=TAPPING||p.position().distanceToSqr(at)<=AT_DOOR*AT_DOOR&&age>=TAP){c.called=now;PellHouse.cue(p,PellTexts.CANE_CALL);}
            return true;
        }
        if(c.answered<0){if(now-c.called>=ANSWER){c.answered=now;PellHouse.cue(p,PellTexts.CANE_ANSWER);}return true;}
        long after=now-c.answered;
        // The cane goes away from the door, further in, fainter.
        if(after==TAP||after==2*TAP)tap(p,c,-(int)(after/TAP),.4F/(after/TAP));
        return after<AWAY;
    }
    /**
     * One tap of the cane, heard by this reader alone, behind the door. Along the wall while she comes (step 0, 1, 2...: from one side of
     * the door to the other); further in as she goes away (step -1, -2).
     */
    private static void tap(ServerPlayer p,Cane c,int step,float volume){
        BlockPos where;
        if(step>=0){int along=Math.max(-2,Math.min(2,step/2-2));where=c.door.relative(c.behind).relative(c.behind.getClockWise(),along);}
        else where=c.door.relative(c.behind,1-step);
        LiteraryVignettes.heard(p,PellRegistry.CANE_TAP.get(),where,volume,.95F+p.getRandom().nextFloat()*.1F);
    }

    // ------------------------------------------------------------------------------------------------ the door
    /**
     * A hand on a resident's door, or a knock on it (LabyrinthDoors.use, DoorSounds.knock): if the cane is behind it for this reader, it
     * stops, and nothing answers. True if it was.
     */
    public static boolean tried(ServerPlayer p,LabyrinthData.Door door){
        var c=CANES.get(p.getUUID());
        if(c==null||!c.door.equals(door.lower)||!c.dim.equals(p.level().dimension()))return false;
        CANES.remove(p.getUUID());return true;
    }
    /** For tests: whether the cane is behind a door for this reader now, and which. */
    public static @Nullable BlockPos tapping(UUID reader){var c=CANES.get(reader);return c==null?null:c.door;}
    public static void clearAll(){CANES.clear();}
}
