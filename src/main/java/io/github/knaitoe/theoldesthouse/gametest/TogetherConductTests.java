package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.564, the multiplayer audit (the owner: "remember to continue other vignettes and then confirm intended and functional
 * multiplayer compatibility for each"; and, for the cast checks, "You should standardize your tests with Tess and run them against
 * the dialogue heavy, branching vignettes too"). The talking scenes of {@link CastConductTests}, each with two readers at once.
 *
 * <p>The first reader comes in as the cast checks' reader does. The second comes through the House's own door, as a second player
 * would ({@link SceneInstances#enter}): into the same scene where the story is written for a party or is the House's one room for
 * everyone, and into a copy of their own where the story is private (built a million blocks west while their door waits). Both go
 * round everyone they find, each with their own {@link CastConductTests.TalkBot}, for the same half a minute, and the cast is held
 * to the same checks as with one reader: one body to a part, an answer turned to whoever asked it, nobody appearing, vanishing,
 * jumping or standing in a block where either reader can see. A scene where both are together is watched as one; a reader in a copy
 * has their own copy's people watched for them.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_together") @PrefixGameTestTemplate(false)
public final class TogetherConductTests {
    private TogetherConductTests(){}
    /** The round, plus time for a copy to be built for the second reader. */
    private static final int TIMEOUT=CastConductTests.TICKS+9000;
    private static LiteraryTests.Fixture open;

    /** How a reader is brought into a scene that stands at a base (the original's or a copy's), as its door brings them. */
    interface Arrival{void at(LiteraryTests.Fixture f,ServerPlayer p,BlockPos base);}
    /** The literary rooms' own way in: onto the mat inside the door, and the scene told they have come. */
    static Arrival literary(LabyrinthPlace place){
        return (f,p,b)->{
            var l=f.level(place);p.stopRiding();p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-3,180,0);p.hasChangedDimension();
            p.setNoGravity(true);p.setDeltaMovement(Vec3.ZERO);LiteraryVignettes.onArrive(p,place);
        };
    }

    /** Two readers in one scene: see the class comment. {@code build} lays the original down where its builder is not the literary rooms'. */
    private static void together(GameTestHelper h,LabyrinthPlace place,Arrival arrive,@Nullable Consumer<LiteraryTests.Fixture> build,Consumer<CastConduct> allow){
        var f=new LiteraryTests.Fixture(h);open=f;var l=f.level(place);var server=f.in.getServer();
        SceneInstances.fixtureCap(1);
        if(build==null)f.build(place);else{f.built.add(place);build.accept(f);}
        var b0=f.base(place);f.chunks.hold(l,IndianLakeRooms.bounds(b0,place));
        var first=NativeTestPlayers.survival(h,"together_a"+place.ordinal());f.players.add(first);
        // The first comes through the door too, where the scene has copies: the door is what keeps a scene for its reader, and how a
        // partner finds them (the Pell house's second reader joins the one who keeps the house).
        if(SceneInstances.copyable(place)){var e=SceneInstances.enter(first,place);h.assertTrue(e.door()!=null&&e.copy()==0,place+": the first reader's door opens onto the original: "+e);}
        arrive.at(f,first,b0);
        // The second waits in the hall at the House's origin, and goes in by the door the House gives them.
        var second=NativeTestPlayers.survival(h,"together_b"+place.ordinal());f.players.add(second);
        second.teleportTo(f.in,f.origin.getX()+.5,f.origin.getY()+2,f.origin.getZ()+.5,0,0);second.setNoGravity(true);second.setDeltaMovement(Vec3.ZERO);
        final BlockPos[] bases={b0,null};final String[] how={""};
        h.startSequence()
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the first reader's scene is resident"))
            .thenWaitUntil(()->{
                if(!SceneInstances.copyable(place)){bases[1]=b0;how[0]="the House's one room for everyone";return;}
                var e=SceneInstances.enter(second,place);
                h.assertTrue(e.door()!=null&&!SceneInstances.building(),"waiting for the second reader's door: "+e+", copies "+SceneInstances.copies(f.data(),place));
                bases[1]=SceneInstances.base(server,place,e.copy());
                how[0]=e.copy()==0?"the same scene, written for more than one":"a copy of their own, "+(b0.getX()-bases[1].getX())+" blocks west";
            })
            .thenExecute(()->{
                h.assertTrue(bases[1]!=null,"the second reader's door opens onto a scene");
                // A private story gives the second reader a scene of their own; a party's or a shared room's takes them in with the first.
                boolean party=SceneInstances.party(place)>0||place==LabyrinthPlace.PELL_HOUSE;
                if(SceneInstances.copyable(place))h.assertTrue(party==bases[1].equals(b0),place+": the second reader goes "+(party?"in with the first":"into a scene of their own")+", not "+how[0]);
                if(!bases[1].equals(b0))f.chunks.hold(l,IndianLakeRooms.bounds(bases[1],place));
            })
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the second reader's scene is resident"))
            .thenExecute(()->{
                arrive.at(f,second,bases[1]);
                h.assertTrue(bases[1].equals(LiteraryVignettes.base(second,place))||!LiteraryRooms.isLiterary(place),place+": the House has the second reader in the scene they went into, at "+bases[1].toShortString()+", not "+LiteraryVignettes.base(second,place));
                round(h,f,place,first,second,bases[0],bases[1],how[0],allow);
            });
    }

    /** Both readers go round their scene's people at once; the case fails with what the checks found. */
    private static void round(GameTestHelper h,LiteraryTests.Fixture f,LabyrinthPlace place,ServerPlayer first,ServerPlayer second,BlockPos b0,BlockPos b1,String how,Consumer<CastConduct> allow){
        var l=f.level(place);boolean apart=!b0.equals(b1);
        var areaA=IndianLakeRooms.bounds(b0,place);var areaB=IndianLakeRooms.bounds(b1,place);
        SceneReady.whenTicking(h,l,areaA,2400,()->SceneReady.whenTicking(h,l,areaB,2400,()->{
            var conductA=new CastConduct(h,l,areaA,()->apart?List.of(first):List.of(first,second));allow.accept(conductA);
            var conductB=apart?new CastConduct(h,l,areaB,()->List.of(second)):conductA;if(apart)allow.accept(conductB);
            var botA=new CastConductTests.TalkBot(first,l,areaA,conductA);var botB=new CastConductTests.TalkBot(second,l,areaB,conductB);
            final int[] done={0};
            Runnable finish=()->{
                if(++done[0]<2)return;
                try{
                    conductA.assertClean(place.name()+" with two readers, "+how+" (first: "+botA.summary()+"; second: "+botB.summary()+")");
                    if(apart)conductB.assertClean(place.name()+", the second reader's copy ("+botB.summary()+")");
                    h.assertTrue(botA.met()>0,place+": nobody was there for the first reader to meet");
                    h.assertTrue(botB.met()>0,place+": nobody was there for the second reader to meet, in "+how);
                    h.succeed();
                }finally{conductA.close();if(apart)conductB.close();close();}
            };
            botA.run(h,CastConductTests.TICKS,finish);botB.run(h,CastConductTests.TICKS,finish);
        }));
    }
    private static void together(GameTestHelper h,LabyrinthPlace place){together(h,place,literary(place),null,c->{});}
    private static void close(){SceneInstances.fixtureCap(null);SceneInstances.clear();if(open!=null){var f=open;open=null;f.close();}}

    // ------------------------------------------------------------------------------------------------ the scenes
    @GameTest(template="empty",batch="together_place_for_you",timeoutTicks=TIMEOUT) public static void aPlaceForYou(GameTestHelper h){together(h,LabyrinthPlace.PLACE_FOR_YOU);}
    @AfterBatch(batch="together_place_for_you") public static void placeForYouDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="together_second_person",timeoutTicks=TIMEOUT) public static void secondPerson(GameTestHelper h){together(h,LabyrinthPlace.SECOND_PERSON);}
    @AfterBatch(batch="together_second_person") public static void secondPersonDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="together_speak_up",timeoutTicks=TIMEOUT) public static void speakUp(GameTestHelper h){
        var place=SpeakUp.PLACE;
        together(h,place,(f,p,b)->{
            SpeakUpTests.enterEveryQuietRoom(f.data(),p.getUUID());
            p.teleportTo(f.in,b.getX()-2.5,b.getY(),b.getZ()-8.5,-90,0);p.hasChangedDimension();p.setNoGravity(true);p.setDeltaMovement(Vec3.ZERO);SpeakUp.arrived(p,place);
        },f->{
            var b=f.base(place);var slot=LabyrinthPlaces.slotBounds(f.origin,place);
            f.chunks.hold(f.in,new AABB(slot.minX(),slot.minY(),slot.minZ(),slot.maxX()+1,slot.maxY()+1,slot.maxZ()+1));
            SpeakUp.build(f.in,b);LabyrinthBuilder.registerDoors(f.data(),place,b);
        },c->{});
    }
    @AfterBatch(batch="together_speak_up") public static void speakUpDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="together_pell",timeoutTicks=TIMEOUT) public static void pellHouse(GameTestHelper h){together(h,LabyrinthPlace.PELL_HOUSE);}
    @AfterBatch(batch="together_pell") public static void pellDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="together_pinehook",timeoutTicks=TIMEOUT) public static void pinehook(GameTestHelper h){together(h,LabyrinthPlace.PINEHOOK);}
    @AfterBatch(batch="together_pinehook") public static void pinehookDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="together_last_crossing",timeoutTicks=TIMEOUT) public static void lastCrossing(GameTestHelper h){together(h,LabyrinthPlace.LAST_CROSSING);}
    @AfterBatch(batch="together_last_crossing") public static void lastCrossingDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="together_supper",timeoutTicks=TIMEOUT) public static void theSupper(GameTestHelper h){together(h,LabyrinthPlace.MARSH_SUPPER);}
    @AfterBatch(batch="together_supper") public static void supperDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="together_growing",timeoutTicks=TIMEOUT) public static void growingThings(GameTestHelper h){together(h,LabyrinthPlace.GROWING_THINGS);}
    @AfterBatch(batch="together_growing") public static void growingDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="together_last_orders",timeoutTicks=TIMEOUT) public static void lastOrders(GameTestHelper h){together(h,LabyrinthPlace.LAST_ORDERS);}
    @AfterBatch(batch="together_last_orders") public static void lastOrdersDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="together_corner",timeoutTicks=TIMEOUT) public static void theCorner(GameTestHelper h){together(h,LabyrinthPlace.CORNER_HOUSE);}
    @AfterBatch(batch="together_corner") public static void cornerDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="together_kept",timeoutTicks=TIMEOUT) public static void theKept(GameTestHelper h){together(h,LabyrinthPlace.KEPT);}
    @AfterBatch(batch="together_kept") public static void keptDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="together_drawing",timeoutTicks=TIMEOUT) public static void theDrawing(GameTestHelper h){together(h,LabyrinthPlace.THE_DRAWING);}
    @AfterBatch(batch="together_drawing") public static void drawingDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="together_people_lights",timeoutTicks=TIMEOUT) public static void peopleLights(GameTestHelper h){together(h,LabyrinthPlace.PEOPLE_LIGHTS);}
    @AfterBatch(batch="together_people_lights") public static void peopleLightsDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="together_bike_night",timeoutTicks=TIMEOUT) public static void bikeNight(GameTestHelper h){together(h,LabyrinthPlace.BIKE_NIGHT);}
    @AfterBatch(batch="together_bike_night") public static void bikeNightDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="together_three_fingers",timeoutTicks=TIMEOUT) public static void threeFingers(GameTestHelper h){together(h,LabyrinthPlace.THREE_FINGERS);}
    @AfterBatch(batch="together_three_fingers") public static void threeFingersDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="together_harrigan",timeoutTicks=TIMEOUT) public static void harrigan(GameTestHelper h){
        var place=LabyrinthPlace.HARRIGAN;var walk=literary(place);
        together(h,place,(f,p,b)->{walk.at(f,p,b);HarriganVignette.onArrive(p,place);},f->{
            var b=f.base(place);f.chunks.hold(f.in,HarriganWake.bounds(b).inflate(2));
            HarriganVignette.build(f.in.getServer(),f.in,b);LabyrinthBuilder.registerDoors(f.data(),place,b);
        },c->{});
    }
    @AfterBatch(batch="together_harrigan") public static void harriganDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="together_ghosts_set",timeoutTicks=TIMEOUT) public static void ghostsSet(GameTestHelper h){together(h,LabyrinthPlace.GHOSTS_SET);}
    @AfterBatch(batch="together_ghosts_set") public static void ghostsSetDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="together_confession",timeoutTicks=TIMEOUT) public static void confession(GameTestHelper h){together(h,LabyrinthPlace.CONFESSION);}
    @AfterBatch(batch="together_confession") public static void confessionDone(ServerLevel l){close();}
}
