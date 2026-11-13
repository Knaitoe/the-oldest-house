package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.560, the owner: "You should standardize your tests with Tess and run them against the dialogue heavy, branching vignettes
 * too. Many of those NPCs also did not behave properly." Each case builds one of those scenes, brings a reader in at its door, and
 * has them go round everyone there for half a minute ({@link TalkBot}): up to each person in turn, looking at them, speaking to
 * them, and answering whatever the conversation panel asks, a different answer each time. {@link CastConduct} holds the whole cast
 * to the same checks as Tess on every tick.
 *
 * <p>Three talking scenes are left to their own cases: the Tall Grass's people are voices until the car at its end, the familiar
 * kitchen's visitors come with time, and Johnny (the Truant) keeps his own hours between his places.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_cast") @PrefixGameTestTemplate(false)
public final class CastConductTests {
    private CastConductTests(){}
    /** How long a reader goes round a scene, and how often they do something. */
    static final int TICKS=600,STEP=10;
    /** Ticks before the reader speaks to the same person again. */
    static final int SPACING=100;
    /** Ticks a reader who has been answered stays to listen, rather than going straight on to the next person. */
    static final int LISTEN=30;
    private static final int TIMEOUT=TICKS+3600;
    private static LiteraryTests.Fixture open;

    /**
     * A reader going round a scene's people. Every {@link #STEP} ticks: if the conversation panel is asking something, they answer
     * it (the next answer along each time, so the branches are walked); if somebody has just answered them, they stay and listen
     * ({@link #LISTEN}); otherwise they go up to the next person in turn, to a place beside them in plain sight of them, look at them
     * and speak to them.
     */
    static final class TalkBot {
        private final ServerPlayer p;private final ServerLevel l;private final AABB area;private final CastConduct conduct;
        private final Set<UUID> met=new HashSet<>();
        /** When each person was last spoken to, so nobody is spoken to over and over (a reader would not). */
        private final Map<UUID,Integer> lastSpoken=new HashMap<>();
        private int turn,answer,spoken,answered,replies,now,listenUntil;
        /** Who the reader is listening to, until {@link #listenUntil}. */
        private @Nullable LiteraryActor listening;
        TalkBot(ServerPlayer p,ServerLevel l,AABB area,CastConduct conduct){this.p=p;this.l=l;this.area=area;this.conduct=conduct;}
        /** People met, words to them that were answered, and answers chosen on the panel. */
        int met(){return met.size();}
        int answered(){return answered;}
        int replies(){return replies;}
        String summary(){return met.size()+" met, "+spoken+" spoken to, "+answered+" answered, "+replies+" panel answers";}
        void run(GameTestHelper h,int ticks,Runnable done){step(h,0,ticks,done);}
        private void step(GameTestHelper h,int t,int ticks,Runnable done){
            if(t>=ticks){done.run();return;}
            now=t;act();
            h.runAfterDelay(STEP,()->step(h,t+STEP,ticks,done));
        }
        private List<LiteraryActor> people(){
            // The people the game shows this reader (0.4.564: LiteraryActor.broadcastToPlayer, so a second camera on the Ghosts set
            // meets the lead's family, and nobody meets another reader's own people).
            var all=new ArrayList<>(l.getEntitiesOfClass(LiteraryActor.class,area,a->a.isAlive()&&!a.isRemoved()&&!a.isInvisible()&&a.broadcastToPlayer(p)));
            all.sort(Comparator.comparing(LiteraryActor::getUUID));return all;
        }
        private void act(){
            if(!p.isAlive()||p.level()!=l)return;
            if(Talk.waiting(p)){
                var offered=Talk.replies(p);var speaker=nearest();boolean near=speaker!=null&&speaker.distanceTo(p)<6;
                boolean wasSaying=near&&speaker.saying();var name=near?speaker.getCustomName():null;
                if(near)conduct.asking(speaker,p);
                Talk.reply(p,Talk.token(p),answer++%offered.size());replies++;
                if(near&&speaker.saying()&&(!wasSaying||!Objects.equals(name,speaker.getCustomName()))){conduct.answered(speaker,"(an answer on the panel)");listen(speaker);}
                return;
            }
            // Somebody answering is listened to, as a reader would, before going on to the next person.
            if(now<listenUntil&&listening!=null&&listening.isAlive()&&!listening.isRemoved()){p.lookAt(EntityAnchorArgument.Anchor.EYES,listening.getEyePosition());return;}
            var people=people();if(people.isEmpty())return;
            LiteraryActor a=null;
            for(int i=0;i<people.size()&&a==null;i++){var next=people.get(turn++%people.size());if(now-lastSpoken.getOrDefault(next.getUUID(),-SPACING)>=SPACING)a=next;}
            if(a==null)return;
            met.add(a.getUUID());lastSpoken.put(a.getUUID(),now);
            approach(a);p.lookAt(EntityAnchorArgument.Anchor.EYES,a.getEyePosition());
            var before=Talk.lines(p);boolean wasSaying=a.saying();var name=a.getCustomName();
            spoken++;conduct.asking(a,p);p.interactOn(a,InteractionHand.MAIN_HAND);
            boolean heard=!Talk.lines(p).equals(before)||a.saying()&&(!wasSaying||!Objects.equals(name,a.getCustomName()));
            if(heard){answered++;conduct.answered(a,"(spoken to)");listen(a);}
        }
        private void listen(LiteraryActor a){listening=a;listenUntil=now+LISTEN;}
        private LiteraryActor nearest(){LiteraryActor best=null;double d=Double.MAX_VALUE;for(var a:people()){double e=a.distanceToSqr(p);if(e<d){d=e;best=a;}}return best;}
        /** To a place a step or two from them, nearest where the reader stands, with room to stand and nothing between them. */
        private void approach(LiteraryActor a){
            if(a.distanceTo(p)<2.6&&clear(p.getEyePosition(),a.getEyePosition()))return;
            Vec3 best=null;double nearest=Double.MAX_VALUE;var size=p.getDimensions(Pose.STANDING);
            for(int i=0;i<16;i++){
                double angle=Math.PI*2*i/16,reach=i%2==0?1.8:2.4;
                for(double dy:new double[]{0,1,-1}){
                    var spot=new Vec3(a.getX()+Math.cos(angle)*reach,Math.floor(a.getY())+dy,a.getZ()+Math.sin(angle)*reach);
                    if(!area.contains(spot)||!l.noCollision(p,size.makeBoundingBox(spot)))continue;
                    if(!clear(spot.add(0,size.eyeHeight(),0),a.getEyePosition()))continue;
                    double d=spot.distanceToSqr(p.position());if(d<nearest){nearest=d;best=spot;}
                    break;
                }
            }
            if(best!=null){p.stopRiding();p.moveTo(best.x,best.y,best.z,p.getYRot(),p.getXRot());p.setDeltaMovement(Vec3.ZERO);}
        }
        private boolean clear(Vec3 from,Vec3 to){return l.clip(new ClipContext(from,to,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p)).getType()==HitResult.Type.MISS;}
    }

    /** One scene, gone round for {@link #TICKS} ticks; the case fails with what the checks found, or if there was nobody to meet. */
    private static void sweep(GameTestHelper h,LabyrinthPlace place,Consumer<CastConduct> allow){sweep(h,place,null,allow);}
    /**
     * As above, for a scene that is not one of the literary rooms: {@code arrive} builds it with its own builder and brings the
     * reader in as its door would.
     */
    private static void sweep(GameTestHelper h,LabyrinthPlace place,@Nullable BiConsumer<LiteraryTests.Fixture,ServerPlayer> arrive,Consumer<CastConduct> allow){
        var f=new LiteraryTests.Fixture(h);open=f;
        ServerPlayer p;
        if(arrive==null)p=f.player("cast"+place.ordinal(),place);
        else{p=NativeTestPlayers.survival(h,"cast"+place.ordinal());f.players.add(p);f.built.add(place);arrive.accept(f,p);}
        var l=f.level(place);var b=LiteraryVignettes.base(p,place);if(b==null)b=f.base(place);
        var area=IndianLakeRooms.bounds(b,place);f.chunks.hold(l,area);
        SceneReady.whenTicking(h,l,area,2400,()->{
            var conduct=new CastConduct(h,l,area,()->List.of(p));allow.accept(conduct);
            var bot=new TalkBot(p,l,area,conduct);
            bot.run(h,TICKS,()->{
                try{
                    conduct.assertClean(place.name()+" ("+bot.summary()+")");
                    h.assertTrue(bot.met()>0,place+": nobody was there to meet in "+TICKS+" ticks");
                    h.succeed();
                }finally{conduct.close();close();}
            });
        });
    }
    private static void close(){if(open!=null){var f=open;open=null;f.close();}}

    // ------------------------------------------------------------------------------------------------ the scenes
    @GameTest(template="empty",batch="cast_place_for_you",timeoutTicks=TIMEOUT) public static void aPlaceForYou(GameTestHelper h){sweep(h,LabyrinthPlace.PLACE_FOR_YOU,c->{});}
    @AfterBatch(batch="cast_place_for_you") public static void placeForYouDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="cast_second_person",timeoutTicks=TIMEOUT) public static void secondPerson(GameTestHelper h){sweep(h,LabyrinthPlace.SECOND_PERSON,c->{});}
    @AfterBatch(batch="cast_second_person") public static void secondPersonDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="cast_speak_up",timeoutTicks=TIMEOUT) public static void speakUp(GameTestHelper h){
        sweep(h,SpeakUp.PLACE,(f,p)->{
            var place=SpeakUp.PLACE;var b=f.base(place);var slot=LabyrinthPlaces.slotBounds(f.origin,place);
            f.chunks.hold(f.in,new AABB(slot.minX(),slot.minY(),slot.minZ(),slot.maxX()+1,slot.maxY()+1,slot.maxZ()+1));
            SpeakUp.build(f.in,b);LabyrinthBuilder.registerDoors(f.data(),place,b);SpeakUpTests.enterEveryQuietRoom(f.data(),p.getUUID());
            p.teleportTo(f.in,b.getX()-2.5,b.getY(),b.getZ()-8.5,-90,0);p.hasChangedDimension();p.setDeltaMovement(Vec3.ZERO);SpeakUp.arrived(p,place);
        },c->{});
    }
    @AfterBatch(batch="cast_speak_up") public static void speakUpDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="cast_pell",timeoutTicks=TIMEOUT) public static void pellHouse(GameTestHelper h){sweep(h,LabyrinthPlace.PELL_HOUSE,c->{});}
    @AfterBatch(batch="cast_pell") public static void pellDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="cast_pinehook",timeoutTicks=TIMEOUT) public static void pinehook(GameTestHelper h){sweep(h,LabyrinthPlace.PINEHOOK,c->{});}
    @AfterBatch(batch="cast_pinehook") public static void pinehookDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="cast_last_crossing",timeoutTicks=TIMEOUT) public static void lastCrossing(GameTestHelper h){sweep(h,LabyrinthPlace.LAST_CROSSING,c->{});}
    @AfterBatch(batch="cast_last_crossing") public static void lastCrossingDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="cast_supper",timeoutTicks=TIMEOUT) public static void theSupper(GameTestHelper h){sweep(h,LabyrinthPlace.MARSH_SUPPER,c->{});}
    @AfterBatch(batch="cast_supper") public static void supperDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="cast_growing",timeoutTicks=TIMEOUT) public static void growingThings(GameTestHelper h){sweep(h,LabyrinthPlace.GROWING_THINGS,c->{});}
    @AfterBatch(batch="cast_growing") public static void growingDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="cast_last_orders",timeoutTicks=TIMEOUT) public static void lastOrders(GameTestHelper h){sweep(h,LabyrinthPlace.LAST_ORDERS,c->{});}
    @AfterBatch(batch="cast_last_orders") public static void lastOrdersDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="cast_corner",timeoutTicks=TIMEOUT) public static void theCorner(GameTestHelper h){sweep(h,LabyrinthPlace.CORNER_HOUSE,c->{});}
    @AfterBatch(batch="cast_corner") public static void cornerDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="cast_kept",timeoutTicks=TIMEOUT) public static void theKept(GameTestHelper h){sweep(h,LabyrinthPlace.KEPT,c->{});}
    @AfterBatch(batch="cast_kept") public static void keptDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="cast_drawing",timeoutTicks=TIMEOUT) public static void theDrawing(GameTestHelper h){sweep(h,LabyrinthPlace.THE_DRAWING,c->{});}
    @AfterBatch(batch="cast_drawing") public static void drawingDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="cast_people_lights",timeoutTicks=TIMEOUT) public static void peopleLights(GameTestHelper h){sweep(h,LabyrinthPlace.PEOPLE_LIGHTS,c->{});}
    @AfterBatch(batch="cast_people_lights") public static void peopleLightsDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="cast_bike_night",timeoutTicks=TIMEOUT) public static void bikeNight(GameTestHelper h){sweep(h,LabyrinthPlace.BIKE_NIGHT,c->{});}
    @AfterBatch(batch="cast_bike_night") public static void bikeNightDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="cast_three_fingers",timeoutTicks=TIMEOUT) public static void threeFingers(GameTestHelper h){sweep(h,LabyrinthPlace.THREE_FINGERS,c->{});}
    @AfterBatch(batch="cast_three_fingers") public static void threeFingersDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="cast_harrigan",timeoutTicks=TIMEOUT) public static void harrigan(GameTestHelper h){
        sweep(h,LabyrinthPlace.HARRIGAN,(f,p)->{
            var place=LabyrinthPlace.HARRIGAN;var b=f.base(place);f.chunks.hold(f.in,HarriganWake.bounds(b).inflate(2));
            HarriganVignette.build(f.in.getServer(),f.in,b);LabyrinthBuilder.registerDoors(f.data(),place,b);
            f.arrive(p,place);HarriganVignette.onArrive(p,place);
        },c->{});
    }
    @AfterBatch(batch="cast_harrigan") public static void harriganDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="cast_ghosts_set",timeoutTicks=TIMEOUT) public static void ghostsSet(GameTestHelper h){sweep(h,LabyrinthPlace.GHOSTS_SET,c->{});}
    @AfterBatch(batch="cast_ghosts_set") public static void ghostsSetDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="cast_confession",timeoutTicks=TIMEOUT) public static void confession(GameTestHelper h){sweep(h,LabyrinthPlace.CONFESSION,c->{});}
    @AfterBatch(batch="cast_confession") public static void confessionDone(ServerLevel l){close();}
}
