package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseExperience;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
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
 * <p>0.4.600: Johnny (the Truant) at his lamp, as the stranger a reader first meets and as the claimant a reader far down meets
 * later, and the familiar kitchen's visitors, in by a door, round the conversation panel and out again. The Tall Grass's people are
 * voices until the car at its end; its two-reader case watches them (TogetherSceneTests).
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
    /** What a scene that is not a literary room leaves behind it, cleared with the fixture. */
    private static @Nullable Runnable after;
    /** What a case asks of its scene besides the cast checks, asked once the round is over and before it passes. */
    private static @Nullable Runnable verdict;
    /** Who the reader can go up to: everyone, unless a scene keeps somebody where a reader does not go (behind a shut door). */
    private static java.util.function.Predicate<LiteraryActor> reachable=a->true;

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
            var all=new ArrayList<>(l.getEntitiesOfClass(LiteraryActor.class,area,a->a.isAlive()&&!a.isRemoved()&&!a.isInvisible()&&a.broadcastToPlayer(p)&&reachable.test(a)));
            all.sort(Comparator.comparing(LiteraryActor::getUUID));return all;
        }
        private void act(){
            if(!p.isAlive()||p.level()!=l)return;
            if(Talk.waiting(p)){
                var offered=Talk.replies(p);var speaker=nearest();boolean near=speaker!=null&&speaker.distanceTo(p)<6;
                boolean wasSaying=near&&speaker.saying();var name=near?speaker.getCustomName():null;
                if(near)conduct.asking(speaker,p);
                // 0.4.568 (run 1599): the answer may come from anyone about, not only the nearest.
                for(var a:people())if(a.distanceTo(p)<6)conduct.mayAnswer(a);
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
                    if(verdict!=null){var v=verdict;verdict=null;v.run();}
                    h.succeed();
                }finally{conduct.close();close();}
            });
        });
    }
    private static void close(){verdict=null;reachable=a->true;if(open!=null){var f=open;open=null;if(after!=null){var r=after;after=null;r.run();}f.close();}}

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
    /** 0.4.602: the lodging house's seven, though the House does not deal it yet. */
    @GameTest(template="empty",batch="cast_lodging",timeoutTicks=TIMEOUT) public static void lodgingHouse(GameTestHelper h){sweep(h,LabyrinthPlace.ONES_INSIDE,c->{});}
    @AfterBatch(batch="cast_lodging") public static void lodgingDone(ServerLevel l){close();}
    /** 0.4.685: the five newest, each a round of its people: Rowan's radio shop, the sleeping King's country, the machine's ward, the
     * park's office and the station in the wheat. */
    @GameTest(template="empty",batch="cast_longer",timeoutTicks=TIMEOUT) public static void aLittleLonger(GameTestHelper h){sweep(h,LabyrinthPlace.LITTLE_LONGER,c->{});}
    @AfterBatch(batch="cast_longer") public static void longerDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="cast_king",timeoutTicks=TIMEOUT) public static void theSleepingKing(GameTestHelper h){sweep(h,LabyrinthPlace.SLEEPING_KING,c->c.floating(io.github.knaitoe.theoldesthouse.labyrinth.KingCourt::cat));}
    @AfterBatch(batch="cast_king") public static void kingDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="cast_not_yet",timeoutTicks=TIMEOUT) public static void notYet(GameTestHelper h){sweep(h,LabyrinthPlace.NOT_YET,c->{});}
    @AfterBatch(batch="cast_not_yet") public static void notYetDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="cast_waxworks",timeoutTicks=TIMEOUT) public static void waxworks(GameTestHelper h){sweep(h,LabyrinthPlace.WAXWORKS,c->{});}
    @GameTest(template="empty",batch="cast_cold_room",timeoutTicks=TIMEOUT) public static void coldRoom(GameTestHelper h){sweep(h,LabyrinthPlace.COLD_ROOM,c->{});}
    @GameTest(template="empty",batch="cast_last_seen",timeoutTicks=TIMEOUT) public static void lastSeen(GameTestHelper h){sweep(h,LabyrinthPlace.LAST_SEEN,c->{});}
    @AfterBatch(batch="cast_last_seen") public static void lastSeenDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="cast_fallow",timeoutTicks=TIMEOUT) public static void fallow(GameTestHelper h){sweep(h,LabyrinthPlace.FALLOW,c->{});}
    @AfterBatch(batch="cast_fallow") public static void fallowDone(ServerLevel l){close();}
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

    // ------------------------------------------------------------------------------------------------ 0.4.600: the other talkers
    private static AABB slot(LiteraryTests.Fixture f,LabyrinthPlace place){var slot=LabyrinthPlaces.slotBounds(f.origin,place);return new AABB(slot.minX(),slot.minY(),slot.minZ(),slot.maxX()+1,slot.maxY()+1,slot.maxZ()+1);}
    /** Where a hall's own door leaves a reader: three blocks in from it, facing down the hall. */
    private static void throughTheDoor(LiteraryTests.Fixture f,ServerPlayer p,BlockPos b){p.teleportTo(f.in,b.getX()+.5,b.getY(),b.getZ()-3,180,0);p.hasChangedDimension();p.setDeltaMovement(Vec3.ZERO);}
    /**
     * One of Johnny's places, built by its own builder, and a reader come in by its door. {@code veteran}: a reader past twenty
     * passes who has met him three times and been helped, told him a place and kept a promise (the history his own cases use), so
     * that he can be the claimant; otherwise this is a first meeting.
     */
    private static void johnny(LiteraryTests.Fixture f,ServerPlayer p,LabyrinthPlace place,@Nullable Truant.Turn turn,boolean veteran){
        var b=f.base(place);var l=f.level(place);f.chunks.hold(l,slot(f,place));
        TruantPlaces.build(l,b,place);LabyrinthBuilder.registerDoors(f.data(),place,b);
        Truant.fresh();Truant.apart();Truant.force(turn);
        if(veteran){
            var exp=HouseExperience.record(f.data(),p.getUUID());exp.putInt("Deepest",25);HouseExperience.save(f.data(),p.getUUID(),exp);
            Truant.remember(f.in.getServer(),p.getUUID(),TruantTests.history());
        }
        after=()->{
            Talk.clearAll();
            for(var a:l.getEntitiesOfClass(LiteraryActor.class,IndianLakeRooms.bounds(b,place),Truant::isTruant))a.discard();
            Truant.fresh();Truant.apart();Truant.force(null);
        };
        p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-3,180,0);p.hasChangedDimension();p.setDeltaMovement(Vec3.ZERO);
    }
    @GameTest(template="empty",batch="cast_truant_lamp",timeoutTicks=TIMEOUT) public static void johnnyAtHisLamp(GameTestHelper h){
        sweep(h,TruantPlaces.LAMP,(f,p)->johnny(f,p,TruantPlaces.LAMP,Truant.Turn.HELP,false),c->{});
    }
    @AfterBatch(batch="cast_truant_lamp") public static void truantLampDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="cast_truant_claim",timeoutTicks=TIMEOUT) public static void johnnyTheClaimant(GameTestHelper h){
        sweep(h,TruantPlaces.LAMP,(f,p)->johnny(f,p,TruantPlaces.LAMP,Truant.Turn.CLAIM,true),c->{});
    }
    @AfterBatch(batch="cast_truant_claim") public static void truantClaimDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="cast_truant_door",timeoutTicks=TIMEOUT) public static void johnnyAtTheHeavyDoor(GameTestHelper h){
        sweep(h,TruantPlaces.DOOR,(f,p)->johnny(f,p,TruantPlaces.DOOR,Truant.Turn.HELP,false),c->{});
    }
    @AfterBatch(batch="cast_truant_door") public static void truantDoorDone(ServerLevel l){close();}
    // 0.4.744: his other turns, each at its own place, with a reader he can turn on (the claimant's history).
    @GameTest(template="empty",batch="cast_truant_doorway",timeoutTicks=TIMEOUT) public static void johnnyTheHandInTheDoorway(GameTestHelper h){
        sweep(h,TruantPlaces.DOOR,(f,p)->johnny(f,p,TruantPlaces.DOOR,Truant.Turn.DOORWAY,true),c->{});
    }
    @AfterBatch(batch="cast_truant_doorway") public static void truantDoorwayDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="cast_truant_cover",timeoutTicks=TIMEOUT) public static void johnnyTheCoveredLight(GameTestHelper h){
        sweep(h,TruantPlaces.LAMP,(f,p)->johnny(f,p,TruantPlaces.LAMP,Truant.Turn.COVER,true),c->{});
    }
    @AfterBatch(batch="cast_truant_cover") public static void truantCoverDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="cast_truant_shove",timeoutTicks=TIMEOUT) public static void johnnyTheShove(GameTestHelper h){
        sweep(h,TruantPlaces.LEDGE,(f,p)->johnny(f,p,TruantPlaces.LEDGE,Truant.Turn.SHOVE,true),c->{});
    }
    @AfterBatch(batch="cast_truant_shove") public static void truantShoveDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="cast_truant_arrow",timeoutTicks=TIMEOUT) public static void johnnyTheChalkArrow(GameTestHelper h){
        sweep(h,LabyrinthPlace.TRUANT_CHALK,(f,p)->johnny(f,p,LabyrinthPlace.TRUANT_CHALK,Truant.Turn.ARROW,true),c->{});
    }
    @AfterBatch(batch="cast_truant_arrow") public static void truantArrowDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="cast_truant_knock",timeoutTicks=TIMEOUT) public static void johnnyTheKnock(GameTestHelper h){
        sweep(h,LabyrinthPlace.TRUANT_KNOCK,(f,p)->johnny(f,p,LabyrinthPlace.TRUANT_KNOCK,Truant.Turn.KNOCK,true),c->{});
    }
    @AfterBatch(batch="cast_truant_knock") public static void truantKnockDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="cast_truant_brake",timeoutTicks=TIMEOUT) public static void johnnyTheLiftBrake(GameTestHelper h){
        sweep(h,LabyrinthPlace.TRUANT_LIFT,(f,p)->johnny(f,p,LabyrinthPlace.TRUANT_LIFT,Truant.Turn.BRAKE,true),c->{});
    }
    @AfterBatch(batch="cast_truant_brake") public static void truantBrakeDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="cast_truant_shed",timeoutTicks=TIMEOUT) public static void johnnyTheShed(GameTestHelper h){
        sweep(h,LabyrinthPlace.TRUANT_SHED,(f,p)->johnny(f,p,LabyrinthPlace.TRUANT_SHED,Truant.Turn.SHED,true),c->{});
    }
    @AfterBatch(batch="cast_truant_shed") public static void truantShedDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="cast_truant_signal",timeoutTicks=TIMEOUT) public static void johnnyTheFalseSignal(GameTestHelper h){
        sweep(h,LabyrinthPlace.TRUANT_LANTERN,(f,p)->johnny(f,p,LabyrinthPlace.TRUANT_LANTERN,Truant.Turn.SIGNAL,true),c->{});
    }
    @AfterBatch(batch="cast_truant_signal") public static void truantSignalDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="cast_truant_herd",timeoutTicks=TIMEOUT) public static void johnnyTheHerd(GameTestHelper h){
        sweep(h,LabyrinthPlace.TRUANT_STOCK_GATE,(f,p)->johnny(f,p,LabyrinthPlace.TRUANT_STOCK_GATE,Truant.Turn.HERD,true),c->{});
    }
    @AfterBatch(batch="cast_truant_herd") public static void truantHerdDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="cast_truant_sentence",timeoutTicks=TIMEOUT) public static void johnnyTheSentence(GameTestHelper h){
        sweep(h,LabyrinthPlace.TRUANT_SENTENCE,(f,p)->johnny(f,p,LabyrinthPlace.TRUANT_SENTENCE,Truant.Turn.SENTENCE,true),c->{});
    }
    @AfterBatch(batch="cast_truant_sentence") public static void truantSentenceDone(ServerLevel l){close();}
    /** 0.4.744: the tall grass with one reader; the together cases hold it to the checks with two. Its one person is the driver, who comes only at the end, so the end is begun at once and his stop, walk and going are watched. */
    @GameTest(template="empty",batch="cast_tall_grass",timeoutTicks=TIMEOUT) public static void tallGrassWithOneReader(GameTestHelper h){
        sweep(h,LabyrinthPlace.TALL_GRASS,c->{var f=open;var p=f.players.get(0);TallGrass.beginNow(f.level(LabyrinthPlace.TALL_GRASS),LiteraryVignettes.base(p,LabyrinthPlace.TALL_GRASS));});
    }
    @AfterBatch(batch="cast_tall_grass") public static void tallGrassDone(ServerLevel l){close();}

    /** Seconds a visitor has sat at the kitchen table before the case has them finish their visit, so that their going is watched too. */
    static final int KITCHEN_STAY=7;
    /** Where the kitchen case's reader stands with nobody to talk to, in line with the doorway, and the door they keep their eyes on. */
    private static final Vec3 KITCHEN_STAND=new Vec3(.5,0,-5.5),KITCHEN_DOOR=new Vec3(.5,1,1.2);
    /**
     * The familiar kitchen, built by its own builder, a reader in by its door, and its clock run on: the first visitor due at once
     * rather than after forty seconds, and each one's visit ended after {@link #KITCHEN_STAY} seconds at the table rather than a
     * hundred and fifty, so that in half a minute somebody comes in, is talked to and goes, and the next is on the way.
     *
     * <p>The reader here watches. With nobody in the kitchen they stand in line with the doorway and look at the door; while
     * somebody is coming in or going out they look at them, every tick. A visitor made where they can see, or gone where they can
     * see, is a fault of the cast checks (0.4.600: both were, a step inside the far doors). The reader goes up to nobody who is
     * still in the passage.
     */
    @GameTest(template="empty",batch="cast_kitchen",timeoutTicks=TIMEOUT) public static void theKitchensVisitors(GameTestHelper h){
        sweep(h,FamiliarRoom.KITCHEN,(f,p)->{
            var place=FamiliarRoom.KITCHEN;var b=f.base(place);f.chunks.hold(f.in,slot(f,place));KitchenFittings.clearAll();
            // The kitchen is built once and kept, for every case that comes to it after this one: its clock and its book are not
            // the fixture's to clear away (the kitchen's own cases leave them too).
            f.built.remove(place);
            FamiliarRoom.build(f.in,b);LabyrinthBuilder.registerDoors(f.data(),place,b);
            var data=f.data();var hollo=data.state(HollowayVignette.ID);hollo.putBoolean("ActorDead",false);data.setState(HollowayVignette.ID,hollo);
            boolean[] over={false};int[] came={0},went={0},opened={0},stage={0},seated={-1};boolean[] wasOpen={false};
            var stand=new Vec3(b.getX()+KITCHEN_STAND.x,b.getY()+KITCHEN_STAND.y,b.getZ()+KITCHEN_STAND.z);
            var door=new Vec3(b.getX()+KITCHEN_DOOR.x,b.getY()+KITCHEN_DOOR.y,b.getZ()+KITCHEN_DOOR.z);
            java.util.function.Consumer<net.neoforged.neoforge.event.tick.ServerTickEvent.Post> watch=e->{
                if(over[0]||!p.isAlive()||p.level()!=f.in)return;
                var a=KitchenVisitors.current(f.in);int now=a==null?0:f.data().stateEntry(KitchenVisitors.STATE,KitchenVisitors.SHARED).getInt("Stage");
                if(now==2&&stage[0]==1)came[0]++;
                if(now==0&&stage[0]==3)went[0]++;
                stage[0]=now;
                boolean open=KitchenVisitors.doorOpen(f.in,b);if(open&&!wasOpen[0])opened[0]++;wasOpen[0]=open;
                if(a==null){
                    if(p.position().distanceToSqr(stand)>.01){p.moveTo(stand.x,stand.y,stand.z,0,0);p.setDeltaMovement(Vec3.ZERO);}
                    p.lookAt(EntityAnchorArgument.Anchor.EYES,door);
                }else if(now!=2)p.lookAt(EntityAnchorArgument.Anchor.EYES,a.getEyePosition());
            };
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(watch);
            reachable=a->a.getZ()<b.getZ()-.6;
            after=()->{
                over[0]=true;net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(watch);Talk.clearAll();var a=KitchenVisitors.current(f.in);if(a!=null)a.discard();KitchenFittings.clearAll();
                // Whoever was at the door when the round ended has left it open: shut, as the next case will expect to find it.
                for(var half:new BlockPos[]{b.offset(0,0,1),b.offset(0,1,1)}){var state=f.in.getBlockState(half);if(state.getBlock() instanceof net.minecraft.world.level.block.DoorBlock&&state.getValue(net.minecraft.world.level.block.DoorBlock.OPEN))f.in.setBlock(half,state.setValue(net.minecraft.world.level.block.DoorBlock.OPEN,false),3);}
            };
            verdict=()->{
                h.assertTrue(came[0]>=1&&went[0]>=1,"somebody came in and sat down, and somebody finished and went, in "+TICKS+" ticks: came "+came[0]+", went "+went[0]);
                h.assertTrue(opened[0]>=2,"the kitchen's door opened for them coming and going: "+opened[0]+" times");
                h.assertTrue(went[0]<came[0]||!KitchenVisitors.doorOpen(f.in,b)||KitchenVisitors.current(f.in)!=null,"and is shut behind whoever has gone");
            };
            throughTheDoor(f,p,b);
            hurryTheKitchen(h,f,over,seated);
        },c->{});
    }
    @AfterBatch(batch="cast_kitchen") public static void kitchenDone(ServerLevel l){close();}
    /**
     * Once a second: nobody at the table means the next visitor is due now; somebody sat there {@link #KITCHEN_STAY} seconds has
     * finished, by the case's own count of their sitting (a reader's "Stay as long as you like." puts the kitchen's count back).
     */
    private static void hurryTheKitchen(GameTestHelper h,LiteraryTests.Fixture f,boolean[] over,int[] seated){
        if(over[0])return;
        var data=f.data();var t=data.stateEntry(KitchenVisitors.STATE,KitchenVisitors.SHARED);int occ=t.getInt("Occ");
        if(!t.contains("Who")){seated[0]=-1;if(t.getInt("Next")>occ+1){t.putInt("Next",occ+1);data.setStateEntry(KitchenVisitors.STATE,KitchenVisitors.SHARED,t);}}
        else if(t.getInt("Stage")==2){
            if(seated[0]<0)seated[0]=occ;
            if(occ-seated[0]>=KITCHEN_STAY&&occ-t.getInt("StageAt")<KitchenVisitors.STAY){t.putInt("StageAt",occ-KitchenVisitors.STAY);data.setStateEntry(KitchenVisitors.STATE,KitchenVisitors.SHARED,t);}
        }
        h.runAfterDelay(20,()->hurryTheKitchen(h,f,over,seated));
    }
}
