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
 * A scene names what its own people do on purpose ({@link #swimming}, {@link #floating}, {@link #turnsAway}, {@link #exempt},
 * {@link #emerges}), and the parts it has several of ({@link #several}).
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
    private Predicate<LiteraryActor> swimming=a->false,floating=a->false,turnsAway=a->false,exempt=a->false,emerges=a->false;
    /** Parts a scene has several of, and how many: dancers, a pair of parents, a family propped about a room. */
    private final List<Map.Entry<Predicate<LiteraryActor>,Integer>> several=new ArrayList<>();
    private static final class Track {Vec3 last;boolean first=true,wasRider,wasSeen;final ArrayDeque<double[]> steps=new ArrayDeque<>();int inside,unsupported,dryswim;long reported;List<ServerPlayer> shown=List.of();}
    /** Ticks after the checks begin during which people a scene sets out as the reader arrives are not said to appear. */
    public static final int ARRIVAL=40;
    private final Map<UUID,Track> tracks=new HashMap<>();
    /**
     * A line to be checked for the turn: who said it, when the check is due, how many ticks the speaker had lived when it was said,
     * and whether the check has already been put off once because the reader had just been moved.
     */
    private record Turn(LiteraryActor who,String text,long due,int lived,boolean again,@javax.annotation.Nullable UUID to){
        Turn(LiteraryActor who,String text,long due,@javax.annotation.Nullable UUID to){this(who,text,due,who.tickCount,false,to);}
    }
    /** Where each reader stood on each of the last ten ticks. */
    private final Map<UUID,ArrayDeque<Vec3>> trails=new HashMap<>();
    /** Which way each reader has faced, tick by tick, over the last ten ticks. */
    private final Map<UUID,ArrayDeque<Float>> facings=new HashMap<>();
    private final List<Turn> turns=new ArrayList<>(),later=new ArrayList<>();
    private final Map<String,Long> said=new HashMap<>();
    /** When a reader last spoke to each person: their answer to it may be the same answer as before. */
    private final Map<UUID,Long> asked=new HashMap<>();
    /** 0.4.564: and which reader it was, so that the answer is checked for turning to them rather than to whoever stands nearest. */
    private final Map<UUID,UUID> askedBy=new HashMap<>();
    /** Every reader who spoke to each person, and when: two readers may speak to the same person at once, and be answered either. */
    private final Map<UUID,Map<UUID,Long>> askers=new HashMap<>();
    /**
     * 0.4.568: everyone about when a reader answered on the conversation panel, and when: any of them may be the one who answers back,
     * and what they say in the next two ticks is not the same line said again. Who turns to the reader is still {@link #asked}'s.
     */
    private final Map<UUID,Long> mayAnswerAt=new HashMap<>();
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
     * 0.4.565: people this scene brings out where a reader can see, on purpose, and draws coming out (the woman on the deer road
     * climbs out of the doe). Everyone else who is suddenly there where a reader is looking has appeared.
     */
    public CastConduct emerges(Predicate<LiteraryActor> p){emerges=p;return this;}
    /**
     * 0.4.565: a part this scene has this many of for each reader at once (fourteen dancers, two parents, four propped figures).
     * One more than that is two bodies for one part.
     */
    public CastConduct several(Predicate<LiteraryActor> p,int howMany){several.add(Map.entry(p,howMany));return this;}
    private int bodiesFor(LiteraryActor a){for(var e:several)if(e.getKey().test(a))return e.getValue();return 1;}
    /**
     * A reader is speaking to this person now (a click on them, or an answer on the conversation panel): whatever they say back
     * within the next two ticks is an answer, which may be the same answer as last time. Called before the words are said.
     */
    public void asking(LiteraryActor a){if(!closed)asked.put(a.getUUID(),level.getGameTime());}
    /** As above, by this reader: the person's answer turns to them (with two readers about, not always the nearer one). */
    public void asking(LiteraryActor a,ServerPlayer by){asking(a);if(!closed){askedBy.put(a.getUUID(),by.getUUID());askers.computeIfAbsent(a.getUUID(),k->new HashMap<>()).put(by.getUUID(),level.getGameTime());}}
    /**
     * 0.4.568 (run 1599): a reader answers on the panel with this person about. The panel's answer may come from them rather than from
     * whoever stands nearest (at A Place for You's supper table Kit sat nearer than Alex, and Alex's answer, the same each time the
     * reader chose the same words, was taken for a line said again). It does not make them the one who turns to the reader.
     */
    public void mayAnswer(LiteraryActor a){if(!closed)mayAnswerAt.put(a.getUUID(),level.getGameTime());}
    /** The readers who spoke to this person within a few ticks of {@code when}. */
    private Set<UUID> askedAround(LiteraryActor a,long when){
        var out=new HashSet<UUID>();for(var e:askers.getOrDefault(a.getUUID(),Map.of()).entrySet())if(Math.abs(e.getValue()-when)<=5)out.add(e.getKey());return out;
    }
    /** The reader who spoke to this person last, if it was within the last two ticks. */
    private @javax.annotation.Nullable UUID askedJustNow(LiteraryActor a){return level.getGameTime()-asked.getOrDefault(a.getUUID(),Long.MIN_VALUE/2)<=2?askedBy.get(a.getUUID()):null;}
    /** The reader was answered: the person turns to them as for a line of their own. */
    public void answered(LiteraryActor a,String what){
        if(closed||exempt.test(a))return;supersede(a);turns.add(new Turn(a,what,level.getGameTime()+TURN_TICKS,askedBy.get(a.getUUID())));
    }
    /**
     * 0.4.565 (two readers at Costume Night): somebody who says something new is looking where that line goes, not where their last
     * one went. Hank answered one reader and, a tick later, called the next number to the other; he was held to the first.
     */
    private void supersede(LiteraryActor a){turns.removeIf(t->t.who()==a);later.removeIf(t->t.who()==a);}
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
    private boolean seen(LiteraryActor a,List<ServerPlayer> rs){return seenAt(a.position(),a.getEyeHeight(),shown(a,rs));}
    /**
     * 0.4.564: the readers this person is there for at all, as the game sends them (LiteraryActor.broadcastToPlayer): one reader's
     * own people, kept from the others, neither appear nor vanish nor jump for those others, and do not speak to them.
     */
    private static List<ServerPlayer> shown(LiteraryActor a,List<ServerPlayer> rs){return rs.stream().filter(a::broadcastToPlayer).toList();}
    /** The way a person would face to look at a reader. */
    private static float want(LiteraryActor a,ServerPlayer to){var d=to.getEyePosition().subtract(a.getEyePosition());return (float)Math.toDegrees(Math.atan2(-d.x,d.z));}
    /** How far a person's head is from looking at a reader: seated, as far as a head turns, and to someone right behind them over either shoulder. */
    private static float off(LiteraryActor a,ServerPlayer to){
        float want=want(a,to);var front=a.seatFront();
        if(front==null)return Math.abs(Mth.wrapDegrees(want-a.getYHeadRot()));
        float rel=Mth.wrapDegrees(want-front);
        return Math.abs(rel)>150?Math.min(Math.abs(Mth.wrapDegrees(front+LiteraryActor.HEAD_TURN-a.getYHeadRot())),Math.abs(Mth.wrapDegrees(front-LiteraryActor.HEAD_TURN-a.getYHeadRot())))
                :Math.abs(Mth.wrapDegrees(front+Mth.clamp(rel,-LiteraryActor.HEAD_TURN,LiteraryActor.HEAD_TURN)-a.getYHeadRot()));
    }
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
    private boolean justMoved(ServerPlayer r){return movedWithin(r,10);}
    /** Whether a reader was moved more than a block in one tick within the last {@code ticks} ticks. */
    private boolean movedWithin(ServerPlayer r,int ticks){
        var trail=trails.get(r.getUUID());if(trail==null)return false;Vec3 before=null;int skip=Math.max(0,trail.size()-ticks-1),i=0;
        for(var at:trail){if(i++<skip)continue;if(before!=null&&at.distanceToSqr(before)>1)return true;before=at;}
        return false;
    }
    /**
     * 0.4.565 (two readers in the closing hall): whether a reader has turned sharply (a quarter turn or more in a tick) within this many
     * ticks. A child held by the hand is kept at the reader's side, so a reader who turns round swings them round too, in a hurry.
     */
    private boolean turnedWithin(ServerPlayer r,int ticks){
        var faced=facings.get(r.getUUID());if(faced==null)return false;Float before=null;int skip=Math.max(0,faced.size()-ticks-1),i=0;
        for(var yaw:faced){if(i++<skip)continue;if(before!=null&&Math.abs(net.minecraft.util.Mth.wrapDegrees(yaw-before))>=45)return true;before=yaw;}
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
        for(var r:rs){var trail=trails.computeIfAbsent(r.getUUID(),k->new ArrayDeque<>());trail.addLast(r.position());if(trail.size()>10)trail.removeFirst();
            var faced=facings.computeIfAbsent(r.getUUID(),k->new ArrayDeque<>());faced.addLast(r.getYRot());if(faced.size()>10)faced.removeFirst();}
        var present=new HashSet<UUID>();var bodies=new HashMap<String,Integer>();
        for(var a:level.getEntitiesOfClass(LiteraryActor.class,area,e->e.isAlive()&&!e.isRemoved())){
            if(exempt.test(a))continue;present.add(a.getUUID());
            var t=tracks.computeIfAbsent(a.getUUID(),k->new Track());var to=shown(a,rs);boolean seen=seenAt(a.position(),a.getEyeHeight(),to);
            if(t.first){
                t.first=false;t.last=a.position();t.wasRider=a.isPassenger();t.wasSeen=seen;
                // 0.4.565: somebody already there when a reader is put somewhere (a scene's cut: the Masque puts a reader who moved at the
                // chime back a room, where the red figure stands) did not appear to them. Only readers who stood where they were count.
                if(ticks>ARRIVAL&&a.tickCount<3&&!emerges.test(a)&&nearest(a,to)<24&&seenAt(a.position(),a.getEyeHeight(),to.stream().filter(r->!movedWithin(r,3)).toList()))fault(a,"appeared","where a reader could see, at "+at(a.position()));
                t.shown=to;
                continue;
            }
            double moved=a.position().distanceTo(t.last);
            if(moved>JUMP&&!a.isPassenger()&&!t.wasRider&&(seen||seenAt(t.last,a.getEyeHeight(),to)||nearest(a,to)<4))fault(a,"jumped",String.format(Locale.ROOT,"%.1f blocks in a tick, from %s to %s",moved,at(t.last),at(a.position())));
            // Moving in steps: over the last second, strides of a third of a block or more where a reader could see, on few ticks with
            // nothing between them. A walk or a slide moves a little every tick; a move nobody sees is how a scene puts people away.
            // 0.4.565: a stride counts only after a tick standing still. A step goes, stops, and goes again; somebody who hurries to
            // catch up with a reader put down a few blocks off (a test's reader) goes on every tick until they are there, and stops once.
            // A test's reader is put down beside each person in turn, which no reader on foot does: whoever keeps with a reader (a
            // child by the hand, a follower at heel) hurries after them in a burst each time. Strides near a reader who has just been
            // put somewhere are not steps.
            if(!a.isPassenger()){
                boolean hurried=rs.stream().anyMatch(r->r.distanceTo(a)<6&&(movedWithin(r,10)||turnedWithin(r,10)));
                t.steps.addLast(new double[]{moved,seen||t.wasSeen?1:0,hurried?1:0});if(t.steps.size()>20)t.steps.removeFirst();
                double total=0,before=1;int on=0,strides=0;for(var d:t.steps){total+=d[0];if(d[0]>.004)on++;if(d[0]>.3&&d[0]<=JUMP&&d[1]>0&&d[2]==0&&before<=.004)strides++;before=d[0];}
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
            int allowed=bodiesFor(a);
            if(bodies.merge(key,1,Integer::sum)==allowed+1)fault(a,"is there twice",allowed==1?"(two bodies for one part)":"("+(allowed+1)+" bodies for a part this scene has "+allowed+" of)");
            t.last=a.position();t.wasRider=a.isPassenger();t.wasSeen=seen;t.shown=to;
        }
        // Gone where a reader could see.
        for(var it=tracks.entrySet().iterator();it.hasNext();){
            var e=it.next();if(present.contains(e.getKey()))continue;
            var gone=level.getEntity(e.getKey());
            var watching=e.getValue().shown.stream().filter(r->r.isAlive()&&!r.isSpectator()&&r.level()==level).toList();
            if(gone==null&&seenAt(e.getValue().last,1.6,watching))faults.add("tick "+ticks+": somebody vanished where a reader could see, at "+at(e.getValue().last));
            it.remove();
        }
        // Turned to the reader.
        for(var it=turns.iterator();it.hasNext();){
            var turn=it.next();if(level.getGameTime()<turn.due())continue;it.remove();
            var a=turn.who();if(!a.isAlive()||a.isRemoved()||turnsAway.test(a)||!a.addresses())continue;
            // Somebody whose body has not been ticking (a test world's chunk not yet promoted) has had no chance to turn.
            if(a.tickCount-turn.lived()<TURN_TICKS-4)continue;
            // The reader they were answering, while that reader is there; otherwise whoever stands nearest.
            ServerPlayer to=null;double best=Double.MAX_VALUE;
            var those=shown(a,rs);
            if(turn.to()!=null)for(var r:those)if(r.getUUID().equals(turn.to())){to=r;best=r.distanceTo(a);}
            if(to==null)for(var r:those){double d=r.distanceTo(a);if(d<best){best=d;to=r;}}
            if(to==null||best>12||!a.hasLineOfSight(to))continue;
            if(justMoved(to)){if(!turn.again())later.add(new Turn(a,turn.text(),level.getGameTime()+10,turn.lived(),true,turn.to()));continue;}
            float off=off(a,to),want=want(a,to);
            // 0.4.564: a line nobody asked for, with more than one reader there, may be said to any of them in reach and in sight; an
            // answer is turned to the reader who asked.
            if(off>TURN_DEGREES&&turn.to()==null)for(var r:those)if(r!=to&&r.distanceTo(a)<=12&&a.hasLineOfSight(r)&&off(a,r)<=TURN_DEGREES){off=off(a,r);break;}
            // Two readers who spoke to them at once may be answered either.
            if(off>TURN_DEGREES&&turn.to()!=null){var also=askedAround(a,turn.due()-TURN_TICKS);for(var r:those)if(r!=to&&also.contains(r.getUUID())&&off(a,r)<=TURN_DEGREES){off=off(a,r);break;}}
            if(off>TURN_DEGREES)fault(a,"does not turn to the reader",String.format(Locale.ROOT,"(head %.0f degrees away from reader %s at %s, %.1f blocks off, toward %.0f) after \"%s\" %s",off,to.getUUID().toString().substring(0,8),at(to.position()),best,want,abbreviate(turn.text()),a.addressing()));
        }
        turns.addAll(later);later.clear();
    }
    private void heard(LiteraryActor a,String text){
        if(closed||a.level()!=level||!area.contains(a.position())||exempt.test(a))return;
        var rs=shown(a,readers.get().stream().filter(r->r.isAlive()&&!r.isSpectator()&&r.level()==level).toList());
        if(a.isRemoved()||!a.isAlive())fault(a,"speaks while gone","\""+abbreviate(text)+"\"");
        else if(!rs.isEmpty()&&nearest(a,rs)>EARSHOT)fault(a,"speaks out of earshot",String.format(Locale.ROOT,"(%.0f blocks from the nearest reader) \"%s\"",nearest(a,rs),abbreviate(text)));
        supersede(a);
        String key=a.getUUID()+"|"+text;long now=level.getGameTime();Long before=said.put(key,now);
        boolean answer=now-asked.getOrDefault(a.getUUID(),Long.MIN_VALUE/2)<=2;
        boolean answered=answer||now-mayAnswerAt.getOrDefault(a.getUUID(),Long.MIN_VALUE/2)<=2;
        if(before!=null&&now-before<REPEAT&&!answered)fault(a,"says the same line again",String.format(Locale.ROOT,"after %d ticks: \"%s\"",now-before,abbreviate(text)));
        // A line said with no reader in reach and in sight (LiteraryActor.ADDRESS_RANGE, as the person's own turning goes) is said to
        // nobody: there is nobody to turn to. A reader put near them afterwards is not who it was said to.
        if(!answer&&rs.stream().noneMatch(r->r.distanceTo(a)<=LiteraryActor.ADDRESS_RANGE&&a.hasLineOfSight(r)))return;
        turns.add(new Turn(a,text,now+TURN_TICKS,answer?askedJustNow(a):null));
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
