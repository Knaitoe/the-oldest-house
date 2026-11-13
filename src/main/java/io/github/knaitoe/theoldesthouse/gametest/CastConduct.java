package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.house.HouseWatchers;
import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryActor;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.560, the owner: "You should standardize your tests with Tess and run them against the dialogue heavy, branching vignettes
 * too. Many of those NPCs also did not behave properly." The one set of checks every person in a scene is held to, watched on
 * every tick while a case plays the scene through:
 * <ul>
 * <li><b>No jumps.</b> Nobody moves more than {@link #JUMP} blocks in a tick where a reader can see it, or by such a jump lands
 * beside a reader. (A move nobody sees, off by itself, is how a scene puts people away; that is allowed.)</li>
 * <li><b>No steps.</b> Somebody going somewhere moves on most ticks, not one tick in five.</li>
 * <li><b>Nothing through a block.</b> Nobody stands inside a whole block (a chair, a bench or a bed is furniture to sit in, and
 * does not count), and nobody sitting in something puts their head through what is over it.</li>
 * <li><b>Ground, water or a seat.</b> Nobody stands on nothing or on water: they are on the ground, swimming in water, or seated.
 * Nobody swims through the air.</li>
 * <li><b>Present and in earshot.</b> A line is said by somebody there, near enough to a reader to be heard ({@link #EARSHOT}),
 * and not said again straight after ({@link #REPEAT}).</li>
 * <li><b>Turned to the reader.</b> Whoever is saying something turns their head to the reader they are saying it to; one sitting
 * down turns it as far as a head turns ({@link LiteraryActor#HEAD_TURN}).</li>
 * <li><b>One body.</b> Two of the same person are never in a scene at once.</li>
 * </ul>
 * A scene names what its own people do on purpose ({@link #swimming}, {@link #floating}, {@link #turnsAway}, {@link #exempt}).
 */
public final class CastConduct implements AutoCloseable {
    /** The most a person moves in one tick where it can be seen; a reader running is under half a block. */
    public static final double JUMP=2.5;
    /** How near a reader a line must be said to be heard. */
    public static final double EARSHOT=24;
    /** Ticks within which the same line from the same person counts as said again. */
    public static final int REPEAT=200;
    /** Ticks a speaker has to turn their head to the reader, and how far off it may be. */
    public static final int TURN_TICKS=20;
    public static final float TURN_DEGREES=75;

    private final GameTestHelper h;private final ServerLevel level;private final AABB area;private final Supplier<List<ServerPlayer>> readers;
    private Predicate<LiteraryActor> swimming=a->false,floating=a->false,turnsAway=a->false,exempt=a->false;
    private static final class Track {Vec3 last;boolean first=true,wasRider,wasSeen;final ArrayDeque<double[]> steps=new ArrayDeque<>();int inside,unsupported,dryswim;long reported;}
    /** Ticks after the checks begin during which people a scene sets out as the reader arrives are not said to appear. */
    public static final int ARRIVAL=40;
    private final Map<UUID,Track> tracks=new HashMap<>();
    /**
     * A line to be checked for the turn: who said it, when the check is due, how many ticks the speaker had lived when it was said,
     * and whether the check has already been put off once because the reader had just been moved.
     */
    private record Turn(LiteraryActor who,String text,long due,int lived,boolean again){
        Turn(LiteraryActor who,String text,long due){this(who,text,due,who.tickCount,false);}
    }
    /** Where each reader stood on each of the last ten ticks. */
    private final Map<UUID,ArrayDeque<Vec3>> trails=new HashMap<>();
    private final List<Turn> turns=new ArrayList<>(),later=new ArrayList<>();
    private final Map<String,Long> said=new HashMap<>();
    /** When a reader last spoke to each person: their answer to it may be the same answer as before. */
    private final Map<UUID,Long> asked=new HashMap<>();
    private final Map<String,Integer> counts=new LinkedHashMap<>();
    private final List<String> faults=new ArrayList<>();
    private final BiConsumer<LiteraryActor,String> hearing=this::heard;
    private boolean closed;
    private int ticks;

    public CastConduct(GameTestHelper h,ServerLevel level,AABB area,Supplier<List<ServerPlayer>> readers){
        this.h=h;this.level=level;this.area=area;this.readers=readers;
        LiteraryActor.SAID.add(hearing);
        // Watched at the very end of each tick, after every scene has moved and turned its people: as the readers' games see it.
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,ServerTickEvent.Post.class,watcher);
    }
    private final java.util.function.Consumer<ServerTickEvent.Post> watcher=e->{if(!closed)tick();};
    /** People this scene has swim, rather than stand, wherever they are in water. */
    public CastConduct swimming(Predicate<LiteraryActor> p){swimming=p;return this;}
    /** People this scene hangs in the air on purpose. */
    public CastConduct floating(Predicate<LiteraryActor> p){floating=p;return this;}
    /** People this scene has speak without turning to the reader (with their back to them, or face down, on purpose). */
    public CastConduct turnsAway(Predicate<LiteraryActor> p){turnsAway=p;return this;}
    /** People these checks leave alone altogether. */
    public CastConduct exempt(Predicate<LiteraryActor> p){exempt=p;return this;}
    /**
     * A reader is speaking to this person now (a click on them, or an answer on the conversation panel): whatever they say back
     * within the next two ticks is an answer, which may be the same answer as last time. Called before the words are said.
     */
    public void asking(LiteraryActor a){if(!closed)asked.put(a.getUUID(),level.getGameTime());}
    /** The reader was answered: the person turns to them as for a line of their own. */
    public void answered(LiteraryActor a,String what){
        if(closed||exempt.test(a))return;turns.add(new Turn(a,what,level.getGameTime()+TURN_TICKS));
    }
    /** How many ticks it has watched. */
    public int ticks(){return ticks;}
    public List<String> faults(){return List.copyOf(faults);}

    private static String name(LiteraryActor a){
        var custom=a.getCustomName();String said=a.saying()&&custom!=null?" saying \""+abbreviate(custom.getString())+"\"":"";
        return "role "+a.role()+" phase "+a.phase()+said;
    }
    private static String abbreviate(String text){return text.length()>40?text.substring(0,37)+"...":text;}
    private static String at(Vec3 v){return String.format(Locale.ROOT,"(%.1f, %.1f, %.1f)",v.x,v.y,v.z);}
    private void fault(LiteraryActor a,String kind,String detail){
        int n=counts.merge(kind,1,Integer::sum);
        if(n<=4)faults.add("tick "+ticks+": "+name(a)+" "+kind+" "+detail);
    }
    private boolean seen(LiteraryActor a,List<ServerPlayer> rs){return seenAt(a.position(),a.getEyeHeight(),rs);}
    /**
     * Whether a reader, as they are now, can see a body standing at {@code at}. A move is judged by what the readers can see when
     * it happens: from where they stand and the way they look at that moment, at the place left and at the place come to.
     */
    private static boolean seenAt(Vec3 at,double eye,List<ServerPlayer> rs){
        for(var r:rs)if(HouseWatchers.sees(r,at.add(0,eye,0))||HouseWatchers.sees(r,at.add(0,.3,0)))return true;
        return false;
    }
    /**
     * Whether a reader was moved more than a block in one tick within the last ten: a test's reader is put beside the next person
     * at once, which no reader on foot does, and nobody turns their head to a place the reader has only just been put.
     */
    private boolean justMoved(ServerPlayer r){
        var trail=trails.get(r.getUUID());if(trail==null)return false;Vec3 before=null;
        for(var at:trail){if(before!=null&&at.distanceToSqr(before)>1)return true;before=at;}
        return false;
    }
    private static double nearest(LiteraryActor a,List<ServerPlayer> rs){double d=Double.MAX_VALUE;for(var r:rs)d=Math.min(d,r.distanceTo(a));return d;}
    /**
     * The name of a whole block this person's body is in, or null. Furniture (anything less than a whole block) does not count, and
     * neither do the feet: a person sat in a chair is drawn from a little below its seat.
     */
    private String wholeBlockIn(LiteraryActor a){
        var whole=a.getBoundingBox();var box=new AABB(whole.minX+.12,whole.minY+.35,whole.minZ+.12,whole.maxX-.12,whole.maxY-.12,whole.maxZ-.12);
        if(box.minY>=box.maxY)return null;
        for(var pos:BlockPos.betweenClosed(BlockPos.containing(box.minX,box.minY,box.minZ),BlockPos.containing(box.maxX,box.maxY,box.maxZ))){
            var state=level.getBlockState(pos);var shape=state.getCollisionShape(level,pos);
            if(!shape.isEmpty()&&net.minecraft.world.level.block.Block.isShapeFullBlock(shape))return net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath()+" at "+pos.toShortString();
        }
        return null;
    }
    private boolean supported(LiteraryActor a){
        var box=a.getBoundingBox();var under=new AABB(box.minX+.08,box.minY-.3,box.minZ+.08,box.maxX-.08,box.minY+.02,box.maxZ-.08);
        return level.getBlockCollisions(a,under).iterator().hasNext();
    }

    private void tick(){
        ticks++;var rs=readers.get().stream().filter(r->r.isAlive()&&!r.isSpectator()&&r.level()==level).toList();
        for(var r:rs){var trail=trails.computeIfAbsent(r.getUUID(),k->new ArrayDeque<>());trail.addLast(r.position());if(trail.size()>10)trail.removeFirst();}
        var present=new HashSet<UUID>();var bodies=new HashMap<String,Integer>();
        for(var a:level.getEntitiesOfClass(LiteraryActor.class,area,e->e.isAlive()&&!e.isRemoved())){
            if(exempt.test(a))continue;present.add(a.getUUID());
            var t=tracks.computeIfAbsent(a.getUUID(),k->new Track());boolean seen=seen(a,rs);
            if(t.first){
                t.first=false;t.last=a.position();t.wasRider=a.isPassenger();t.wasSeen=seen;
                if(ticks>ARRIVAL&&a.tickCount<3&&seen&&nearest(a,rs)<24)fault(a,"appeared","where a reader could see, at "+at(a.position()));
                continue;
            }
            double moved=a.position().distanceTo(t.last);
            if(moved>JUMP&&!a.isPassenger()&&!t.wasRider&&(seen||seenAt(t.last,a.getEyeHeight(),rs)||nearest(a,rs)<4))fault(a,"jumped",String.format(Locale.ROOT,"%.1f blocks in a tick, from %s to %s",moved,at(t.last),at(a.position())));
            // Moving in steps: over the last second, strides of a third of a block or more where a reader could see, on few ticks with
            // nothing between them. A walk or a slide moves a little every tick; a move nobody sees is how a scene puts people away.
            if(!a.isPassenger()){
                t.steps.addLast(new double[]{moved,seen||t.wasSeen?1:0});if(t.steps.size()>20)t.steps.removeFirst();
                double total=0;int on=0,strides=0;for(var d:t.steps){total+=d[0];if(d[0]>.004)on++;if(d[0]>.3&&d[0]<=JUMP&&d[1]>0)strides++;}
                if(t.steps.size()==20&&strides>=2&&on<8&&ticks-t.reported>60){t.reported=ticks;fault(a,"moves in steps",String.format(Locale.ROOT,"(%.1f blocks over a second in %d strides, on %d ticks of 20) near %s",total,strides,on,at(a.position())));}
            }else t.steps.clear();
            // Through a block.
            var solid=a.isPassenger()?null:wholeBlockIn(a);
            if(solid!=null){if(++t.inside==3)fault(a,"stands inside a block","("+solid+") at "+at(a.position()));}else t.inside=0;
            if(a.isPassenger()){var head=BlockPos.containing(a.getX(),a.getEyeY()+.2,a.getZ());if(!level.getBlockState(head).getCollisionShape(level,head).isEmpty())fault(a,"puts their head through a block","at "+head.toShortString()+" while riding");}
            // Ground, water or a seat.
            if(!a.isPassenger()&&!floating.test(a)){
                if(swimming.test(a)){if(!a.isInWater()){if(++t.dryswim==8)fault(a,"swims in the air","at "+at(a.position()));}else t.dryswim=0;}
                else if(a.isNoGravity()&&!supported(a)){if(++t.unsupported==8)fault(a,a.isInWater()?"stands in deep water":"stands on nothing","at "+at(a.position()));}
                else t.unsupported=0;
            }
            // One body each: the same part, for the same reader.
            String key=a.role()+"/"+(a.role()==LiteraryActor.STRANGER?a.phase():-1)+"/"+a.owner().map(UUID::toString).orElse("all");
            if(bodies.merge(key,1,Integer::sum)==2)fault(a,"is there twice","(two bodies for one part)");
            t.last=a.position();t.wasRider=a.isPassenger();t.wasSeen=seen;
        }
        // Gone where a reader could see.
        for(var it=tracks.entrySet().iterator();it.hasNext();){
            var e=it.next();if(present.contains(e.getKey()))continue;
            var gone=level.getEntity(e.getKey());
            if(gone==null&&seenAt(e.getValue().last,1.6,rs))faults.add("tick "+ticks+": somebody vanished where a reader could see, at "+at(e.getValue().last));
            it.remove();
        }
        // Turned to the reader.
        for(var it=turns.iterator();it.hasNext();){
            var turn=it.next();if(level.getGameTime()<turn.due())continue;it.remove();
            var a=turn.who();if(!a.isAlive()||a.isRemoved()||turnsAway.test(a)||!a.addresses())continue;
            // Somebody whose body has not been ticking (a test world's chunk not yet promoted) has had no chance to turn.
            if(a.tickCount-turn.lived()<TURN_TICKS-4)continue;
            ServerPlayer to=null;double best=Double.MAX_VALUE;for(var r:rs){double d=r.distanceTo(a);if(d<best){best=d;to=r;}}
            if(to==null||best>12||!a.hasLineOfSight(to))continue;
            if(justMoved(to)){if(!turn.again())later.add(new Turn(a,turn.text(),level.getGameTime()+10,turn.lived(),true));continue;}
            var d=to.getEyePosition().subtract(a.getEyePosition());float want=(float)Math.toDegrees(Math.atan2(-d.x,d.z));
            var front=a.seatFront();float off;
            if(front!=null){
                // A seated person turns their head as far as a head turns; to someone right behind them, over either shoulder.
                float rel=Mth.wrapDegrees(want-front);
                off=Math.abs(rel)>150?Math.min(Math.abs(Mth.wrapDegrees(front+LiteraryActor.HEAD_TURN-a.getYHeadRot())),Math.abs(Mth.wrapDegrees(front-LiteraryActor.HEAD_TURN-a.getYHeadRot())))
                        :Math.abs(Mth.wrapDegrees(front+Mth.clamp(rel,-LiteraryActor.HEAD_TURN,LiteraryActor.HEAD_TURN)-a.getYHeadRot()));
            }else off=Math.abs(Mth.wrapDegrees(want-a.getYHeadRot()));
            if(off>TURN_DEGREES)fault(a,"does not turn to the reader",String.format(Locale.ROOT,"(head %.0f degrees away from reader %s at %s, %.1f blocks off, toward %.0f) after \"%s\" %s",off,to.getUUID().toString().substring(0,8),at(to.position()),best,want,abbreviate(turn.text()),a.addressing()));
        }
        turns.addAll(later);later.clear();
    }
    private void heard(LiteraryActor a,String text){
        if(closed||a.level()!=level||!area.contains(a.position())||exempt.test(a))return;
        var rs=readers.get().stream().filter(r->r.isAlive()&&!r.isSpectator()&&r.level()==level).toList();
        if(a.isRemoved()||!a.isAlive())fault(a,"speaks while gone","\""+abbreviate(text)+"\"");
        else if(!rs.isEmpty()&&nearest(a,rs)>EARSHOT)fault(a,"speaks out of earshot",String.format(Locale.ROOT,"(%.0f blocks from the nearest reader) \"%s\"",nearest(a,rs),abbreviate(text)));
        String key=a.getUUID()+"|"+text;long now=level.getGameTime();Long before=said.put(key,now);
        boolean answer=now-asked.getOrDefault(a.getUUID(),Long.MIN_VALUE/2)<=2;
        if(before!=null&&now-before<REPEAT&&!answer)fault(a,"says the same line again",String.format(Locale.ROOT,"after %d ticks: \"%s\"",now-before,abbreviate(text)));
        turns.add(new Turn(a,text,now+TURN_TICKS));
    }
    /**
     * Fails the case with what was found, or says nothing. Every fault goes to the log; the failure itself names the kinds and the
     * first of them only, since a test's message is written into a book on its lectern and a long one cannot be saved there.
     */
    public void assertClean(String scene){
        if(faults.isEmpty())return;
        var b=new StringBuilder(scene).append(": the cast checks found ").append(counts.values().stream().mapToInt(Integer::intValue).sum()).append(" faults ").append(counts).append(" over ").append(ticks).append(" ticks");
        var log=new StringBuilder(b);for(var f:faults)log.append("\n  ").append(f);
        io.github.knaitoe.theoldesthouse.TheOldestHouse.LOGGER.error(log.toString());
        var first=faults.get(0);h.fail(b.append("; first: ").append(first.length()>300?first.substring(0,297)+"...":first).toString());
    }
    @Override public void close(){closed=true;LiteraryActor.SAID.remove(hearing);NeoForge.EVENT_BUS.unregister(watcher);}
}
