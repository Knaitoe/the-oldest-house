package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.HouseFadePayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.github.knaitoe.theoldesthouse.network.NotYetSeamsPayload;
import io.github.knaitoe.theoldesthouse.network.NotYetStatePayload;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.664, Not Yet, the second part (docs/NOT_YET_0_4_664.md): refused. The machine will not let a reader die in its ward. The hatch in
 * the passage opens under a reader who has not yet fallen, the whole width of the passage, and they fall to the grating ten blocks down.
 * Any death in the place, that one first, is refused the same way: a moment's dark where they fell; the mending table, where they lie under
 * the ring of arms while the arms work and the others in the ward can see them there; their own cot, where they wake with every screen in
 * the ward saying NOT YET to them alone; and then they get up, at the cot's side, whole.
 *
 * <p>Each mending is that reader's: their count of seams (drawn on them for everyone while they are in the place), their cot (the first the
 * machine has not given to another, bed one first), the year the machine began counting them (the counter over the arch, drawn by their
 * own game). Nobody who is mending can be hurt, and nobody gets up off the table or the cot before the machine is done. A reader who goes
 * while being mended is finished at once and left standing at their cot. The House's own deaths and drops never happen here: nothing is
 * lost, because nobody dies. An operator's kill is not refused.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class NotYetMending {
    private NotYetMending(){}
    static final LabyrinthPlace PLACE=LabyrinthPlace.NOT_YET;
    /** The machine's year, in ticks: the counter turns over once in this long. */
    public static final int YEAR=60;
    /** Ticks: dark where the reader fell; on the table; lying on their own cot with the screens' words before they get up. */
    public static final int DARK=14,TABLE=100,COT=60;
    /** How long the hatch stays open with nobody over it before it shuts. */
    public static final int HATCH_HELD=40;
    /** The shared record of the cots the machine has given, per ward. */
    public static final String COTS="not_yet_cots_0664";

    static final class Mend {
        final UUID id;final BlockPos base;final String cause;final Vec3 hold;final float yaw;
        final long tableAt,cotAt,upAt;boolean onTable,onCot;
        Mend(UUID id,BlockPos base,String cause,Vec3 hold,float yaw,long tableAt){this.id=id;this.base=base;this.cause=cause;this.hold=hold;this.yaw=yaw;this.tableAt=tableAt;cotAt=tableAt+TABLE;upAt=cotAt+COT;}
    }
    /** Everyone being mended, in the order they came to the table. */
    private static final Map<UUID,Mend> MENDS=new LinkedHashMap<>();
    /** Readers in the ward who have not yet fallen, watched each tick for the hatch. */
    private static final Set<UUID> WATCH=new HashSet<>();
    /** The seams each reader is drawn with, as last told to everyone. */
    private static final Map<UUID,Integer> SHOWN=new HashMap<>();
    /** Each open hatch, by ward, and how long it has stood open with nobody over it. */
    private static final Map<Long,Integer> OPEN=new HashMap<>();
    /** Screens woken for a reader as they came in, until this tick. */
    private static final Map<UUID,Long> WOKEN=new HashMap<>();
    public static void clearAll(){MENDS.clear();WATCH.clear();SHOWN.clear();OPEN.clear();WOKEN.clear();}
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){clearAll();}

    /** Whether this reader is being mended: on their way to the table, on it, or on their cot after it. */
    public static boolean held(UUID id){return MENDS.containsKey(id);}
    public static boolean held(ServerPlayer p){return held(p.getUUID());}
    public static boolean onTable(UUID id){var m=MENDS.get(id);return m!=null&&m.onTable&&!m.onCot;}
    public static int seams(UUID id){return SHOWN.getOrDefault(id,0);}

    static CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);}
    static void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);}
    static Vec3 rel(Vec3 at,BlockPos b){return at.subtract(b.getX(),b.getY(),b.getZ());}

    /**
     * The ward this reader is in, or null. Asked when they are dying too, so it does not ask whether they are alive: only that they are no
     * spectator, not past the end of their story, and inside the place.
     */
    static @Nullable BlockPos inside(ServerPlayer p){
        if(p.isSpectator()||FinaleProgress.terminal(FinaleProgress.phase(p.server,p.getUUID())))return null;
        if(!p.level().dimension().equals(NovelRooms.dimension(PLACE)))return null;
        var b=LiteraryVignettes.base(p,PLACE);if(b==null)return null;
        return IndianLakeRooms.bounds(b,PLACE).contains(p.position())?b:null;
    }

    // ------------------------------------------------------------------------------------------------ refused
    /** A death in the place is refused. The House's own business with a death (custody, portraits, keepsakes) never starts. */
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void died(LivingDeathEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p)||e.getSource().is(DamageTypes.GENERIC_KILL))return;
        var b=inside(p);if(b==null)return;
        e.setCanceled(true);p.setHealth(p.getMaxHealth());
        refuse(p,b,cause(e.getSource()));
    }
    /** The fall down the shaft is the first death; the landing is refused without the fall's harm. */
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void fell(LivingFallEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p))return;
        var b=inside(p);if(b==null||!NotYetSite.inShaft(rel(p.position(),b)))return;
        e.setCanceled(true);refuse(p,b,"fall");
    }
    /** Nobody is hurt while being mended. */
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void hurt(LivingIncomingDamageEvent e){
        if(e.getEntity() instanceof ServerPlayer p&&held(p)&&!e.getSource().is(DamageTypes.GENERIC_KILL))e.setCanceled(true);
    }
    static String cause(DamageSource s){
        if(s.is(DamageTypes.FALL))return "fall";
        if(s.getEntity() instanceof ServerPlayer)return "reader";
        return s.getMsgId();
    }
    /**
     * The machine takes the reader: whole again at once, held where they fell in the dark until the table is theirs, then on it, then on
     * their cot. The table is had in turn; a second reader waits in the dark until the first is off it.
     */
    public static void refuse(ServerPlayer p,BlockPos b,String cause){
        if(held(p))return;
        p.setHealth(p.getMaxHealth());p.clearFire();p.setAirSupply(p.getMaxAirSupply());p.fallDistance=0;
        // 0.4.665: lying down on their cot, a reader is mended where they lie, and stays down (NotYetTrial).
        if(NotYetTrial.lying(p.getUUID())){NotYetTrial.mended(p,b,cause);return;}
        p.setTicksFrozen(0);
        if(p.isPassenger())p.stopRiding();
        if(p.isSleeping())p.stopSleeping();
        p.setDeltaMovement(Vec3.ZERO);p.hurtMarked=true;
        long now=p.server.getTickCount(),table=now+DARK;
        for(var m:MENDS.values())if(m.base.equals(b))table=Math.max(table,m.cotAt);
        var m=new Mend(p.getUUID(),b,cause,p.position(),p.getYRot(),table);MENDS.put(p.getUUID(),m);
        // Black from the moment they fall to the moment they are on their cot, then it clears as they lie there.
        HousePackets.send(p,new HouseFadePayload(4,(int)(m.cotAt-now)-4,30));
        var own=own(p);own.putBoolean("Fallen",true);own.putString("LastCause",cause);save(p,own);
        WATCH.remove(p.getUUID());
        state(p,own);
    }

    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();
        if(!WATCH.isEmpty())for(var id:List.copyOf(WATCH)){var p=server.getPlayerList().getPlayer(id);if(p==null)WATCH.remove(id);else watched(p);}
        if(!MENDS.isEmpty())for(var m:List.copyOf(MENDS.values())){var p=server.getPlayerList().getPlayer(m.id);if(p==null){MENDS.remove(m.id);continue;}step(p,m,server.getTickCount());}
        if(!OPEN.isEmpty())hatches(server);
        if(server.getTickCount()%20==0&&!SHOWN.isEmpty())sweep(server);
    }
    static void step(ServerPlayer p,Mend m,long now){
        var l=p.serverLevel();var b=m.base;
        if(now<m.tableAt){
            if(p.position().distanceToSqr(m.hold)>.04)p.teleportTo(l,m.hold.x,m.hold.y,m.hold.z,m.yaw,p.getXRot());
            p.setDeltaMovement(Vec3.ZERO);return;
        }
        if(now<m.cotAt){
            var head=b.offset(NotYetSite.TABLE);
            if(!m.onTable){
                m.onTable=true;
                p.teleportTo(l,head.getX()+.5,head.getY()+.6875,head.getZ()+.5,p.getYRot(),0);p.startSleeping(head);
                NotYetSite.arms(l,b,2);
            }else if(!p.isSleeping())p.startSleeping(head);
            long t=now-m.tableAt;
            if(t%20==0)l.playSound(null,head.above(2),NotYetRegistry.MEND.get(),SoundSource.BLOCKS,.9F,.9F+(t%60)/300F);
            if(t==TABLE-10&&MENDS.values().stream().noneMatch(o->o!=m&&o.base.equals(b)&&o.tableAt<=now+10&&o.cotAt>now+10))NotYetSite.arms(l,b,1);
            return;
        }
        var own=own(p);
        if(now<m.upAt){
            if(!m.onCot){
                m.onCot=true;
                if(MENDS.values().stream().noneMatch(o->o!=m&&o.base.equals(b)&&o.tableAt<=now&&o.cotAt>now))NotYetSite.arms(l,b,0);
                wake(p,b,own);save(p,own);
            }else{var head=cotHead(p,b,own);if(!p.isSleeping())p.startSleeping(b.offset(head));}
            return;
        }
        // Let go first: while held, the game's own way of getting a sleeper up is refused (GoatmanWakeMixin), and that includes this one.
        MENDS.remove(m.id);up(p,b,own);save(p,own);state(p,own);
    }
    /** On their own cot, with a new seam and the counter running: every screen in the ward says the two words to them. */
    static void wake(ServerPlayer p,BlockPos b,CompoundTag own){
        var l=p.serverLevel();var head=b.offset(cotHead(p,b,own));
        // Straight from the table to the cot: lying down again moves where they lie (getting up is refused while held, and not wanted).
        p.teleportTo(l,head.getX()+.5,head.getY()+.5625,head.getZ()+.5,p.getYRot(),0);p.startSleeping(head);
        int times=own.getInt("Mended")+1;own.putInt("Mended",times);
        if(!own.contains("CountFrom"))own.putLong("CountFrom",l.getGameTime());
        own.putInt("WordsVisit",own.getInt("Visits"));
        words(p,b,true);show(p,times);
        Kept.machine(p,NotYetTexts.mended(times));NotYet.told(own,NotYetTexts.mended(times));
        NotYet.cue(p,NotYetTexts.WAKE);
    }
    /** Up off the cot, at its side, facing down the aisle. */
    static void up(ServerPlayer p,BlockPos b,CompoundTag own){
        var head=cotHead(p,b,own);
        if(p.isSleeping())p.stopSleeping();
        var at=NotYetSite.bedside(head);var f=NotYetSite.cotFacing(head);
        p.teleportTo(p.serverLevel(),b.getX()+at.x,b.getY(),b.getZ()+at.z,f.toYRot(),0);
        p.setDeltaMovement(Vec3.ZERO);
    }
    /** A reader who goes while being mended is finished at once: the seam, the cot, the counter, and standing at the cot's side. */
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void logout(PlayerEvent.PlayerLoggedOutEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p))return;
        WATCH.remove(p.getUUID());WOKEN.remove(p.getUUID());
        var m=MENDS.remove(p.getUUID());
        if(m!=null){var own=own(p);if(!m.onCot){int times=own.getInt("Mended")+1;own.putInt("Mended",times);if(!own.contains("CountFrom"))own.putLong("CountFrom",p.serverLevel().getGameTime());}
            up(p,m.base,own);save(p,own);
            if(MENDS.values().stream().noneMatch(o->o.base.equals(m.base)&&o.onTable&&!o.onCot))NotYetSite.arms(p.serverLevel(),m.base,0);}
        if(SHOWN.remove(p.getUUID())!=null)HousePackets.sendToAll(p.server,new NotYetSeamsPayload(p.getUUID(),0));
    }
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p))return;
        for(var s:SHOWN.entrySet())if(s.getValue()>0)HousePackets.send(p,new NotYetSeamsPayload(s.getKey(),s.getValue()));
    }

    // ------------------------------------------------------------------------------------------------ cots
    /** The reader's cot: the one the machine gave them, or the first it has not given to anyone else, nearest the door first. */
    static BlockPos cotHead(ServerPlayer p,BlockPos b,CompoundTag own){
        int at=own.getInt("Cot")-1;
        if(at<0||at>=NotYetSite.WARD_COTS.size()){
            var d=LabyrinthData.get(p.server);var key=Long.toString(b.asLong());var given=d.stateEntry(COTS,key);
            at=-1;
            for(int i=0;i<NotYetSite.WARD_COTS.size()&&at<0;i++){String k="Cot"+i;if(!given.hasUUID(k)||given.getUUID(k).equals(p.getUUID()))at=i;}
            if(at<0)at=Math.floorMod(p.getUUID().hashCode(),NotYetSite.WARD_COTS.size());
            given.putUUID("Cot"+at,p.getUUID());d.setStateEntry(COTS,key,given);
            own.putInt("Cot",at+1);
        }
        return NotYetSite.WARD_COTS.get(at);
    }
    /** The cot this reader has been given, if any (for tests and the account). */
    public static @Nullable BlockPos cot(CompoundTag own){int at=own.getInt("Cot")-1;return at>=0&&at<NotYetSite.WARD_COTS.size()?NotYetSite.WARD_COTS.get(at):null;}

    // ------------------------------------------------------------------------------------------------ the hatch
    /** Every five ticks for each reader in the place: whether to watch them for the hatch, their seams for everyone, their screens. */
    public static void reader(ServerPlayer p,BlockPos b,CompoundTag own){
        if(!own.getBoolean("Fallen")&&!p.isCreative()&&!held(p))WATCH.add(p.getUUID());else WATCH.remove(p.getUUID());
        show(p,own.getInt("Mended"));
        long now=p.server.getTickCount();var woken=WOKEN.get(p.getUUID());
        // 0.4.665: while a reader lies down their screens are the trial's; once it has looked away, dark for the rest of that visit.
        if(NotYetTrial.lying(p.getUUID()))return;
        if(own.getInt("QuietVisit")==own.getInt("Visits")&&own.getBoolean("StayedDown")){if(now%100==0)screens(p,b,NotYetBlock.DARK);return;}
        if(woken!=null&&now>=woken){WOKEN.remove(p.getUUID());words(p,b,own.getInt("WordsVisit")==own.getInt("Visits")&&own.getInt("Mended")>0);}
        else if(woken==null&&own.getInt("Mended")>0&&own.getInt("WordsVisit")==own.getInt("Visits")&&now%100==0)words(p,b,true);
    }
    /**
     * Each tick, for the readers who have not fallen: over the plates, the hatch opens and takes them, however they cross it. Asked from the
     * server's own tick, not the player's, so it holds for every connection.
     */
    static void watched(ServerPlayer p){
        var b=inside(p);if(b==null){WATCH.remove(p.getUUID());return;}
        var r=rel(p.position(),b);var l=p.serverLevel();
        if(NotYetSite.aboveHatch(r)){
            if(!NotYetSite.hatchOpen(l,b)){
                NotYetSite.hatches(l,b,true);OPEN.put(b.asLong(),0);
                l.playSound(null,b.offset(0,0,(NotYetSite.HATCH_S+NotYetSite.HATCH_N)/2),NotYetRegistry.HATCH.get(),SoundSource.BLOCKS,1F,1F);
                NotYet.cue(p,NotYetTexts.HATCH_OPEN);
            }
            // Over the shaft with the plates gone: nothing carries them on across it, a jump included.
            var v=p.getDeltaMovement();p.setDeltaMovement(0,Math.min(0,v.y),0);p.hurtMarked=true;
        }
        if(NotYetSite.atShaftFoot(r)&&p.onGround())refuse(p,b,"fall");
    }
    /** An open hatch shuts once nobody has been over it for two seconds. */
    static void hatches(MinecraftServer server){
        for(var key:List.copyOf(OPEN.keySet())){
            var b=BlockPos.of(key);ServerLevel l=server.getLevel(NovelRooms.dimension(PLACE));if(l==null){OPEN.remove(key);continue;}
            boolean over=false;
            for(var p:l.players())if(!p.isSpectator()&&NotYetSite.aboveHatch(rel(p.position(),b)))over=true;
            int t=over?0:OPEN.get(key)+1;
            if(t>=HATCH_HELD){NotYetSite.hatches(l,b,false);OPEN.remove(key);}else OPEN.put(key,t);
        }
    }

    // ------------------------------------------------------------------------------------------------ what the reader sees and is seen with
    /** The ward's screens wake for a reader as they come in: noise, to them alone, for two seconds. */
    public static void wakeScreens(ServerPlayer p,BlockPos b){WOKEN.put(p.getUUID(),p.server.getTickCount()+40L);screens(p,b,NotYetBlock.NOISE);}
    /** Every screen in the ward with the two words on it, to this reader alone; or as they really are. */
    static void words(ServerPlayer p,BlockPos b,boolean on){if(on)screens(p,b,NotYetBlock.WORDS);else screens(p,b,-1);}
    static void screens(ServerPlayer p,BlockPos b,int stage){
        var l=p.serverLevel();
        for(var head:NotYetSite.WARD_COTS){var at=b.offset(NotYetSite.screen(head));var st=l.getBlockState(at);if(!NotYetBlock.is(st,NotYetBlock.Kind.SCREEN))continue;
            p.connection.send(new ClientboundBlockUpdatePacket(at,stage<0?st:st.setValue(NotYetBlock.STAGE,stage)));}
    }
    /** A reader's seams, drawn on them by every viewer while they are in the place. */
    static void show(ServerPlayer p,int seams){
        int was=SHOWN.getOrDefault(p.getUUID(),0);if(was==seams)return;
        if(seams>0)SHOWN.put(p.getUUID(),seams);else SHOWN.remove(p.getUUID());
        HousePackets.sendToAll(p.server,new NotYetSeamsPayload(p.getUUID(),seams));
    }
    /** Out of the place, the seams fade. */
    static void sweep(MinecraftServer server){
        for(var id:List.copyOf(SHOWN.keySet())){var p=server.getPlayerList().getPlayer(id);if(p==null||inside(p)==null&&!held(id)){SHOWN.remove(id);HousePackets.sendToAll(server,new NotYetSeamsPayload(id,0));}}
    }
    /** What this reader's game needs: whether the machine is holding them, when it began counting them, and the kind of counter. */
    public static void state(ServerPlayer p,CompoundTag own){
        var b=LiteraryVignettes.base(p,PLACE);
        HousePackets.send(p,new NotYetStatePayload(held(p),own.contains("CountFrom")?own.getLong("CountFrom"):-1L,NotYet.counterKind(LabyrinthData.get(p.server),p.getUUID()),b==null?BlockPos.ZERO:b.offset(NotYetSite.COUNTER_FIRST)));
    }
    /** The years the machine has counted this reader, at this game time. */
    public static long years(CompoundTag own,long gameTime){return own.contains("CountFrom")?Math.max(0,Math.min(9_999_999L,(gameTime-own.getLong("CountFrom"))/YEAR)):0;}
}
