package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.565, the multiplayer audit (the owner: "confirm intended and functional multiplayer compatibility for each"). Every story in
 * the Witness pool that {@link TogetherConductTests} does not already take two readers into, with two readers at once.
 *
 * <p>Each scene is built as the House builds it ({@link LabyrinthBuilder#buildOne}). Both readers come through the House's door as
 * players do: the door decides where the second goes ({@link SceneInstances#enter}: into the first's scene where the story is written
 * for a party or is one room for everyone, into a copy of their own where it is private), and every scene's own welcome is run for
 * each of them exactly as a door runs it ({@link LabyrinthDoors#scenesArrived}). Then:
 * <ul>
 * <li>the second reader is where the story says they should be;</li>
 * <li>where the story has a paper to read, each reader reads their own, and one reader's reading is never the other's;</li>
 * <li>both go round whoever they find there for a quarter of a minute, and the people are held to the cast checks
 * ({@link CastConduct}) for each reader, as in {@link TogetherConductTests}; a scene with nobody in it has nobody to check.</li>
 * </ul>
 * The scenes the House keeps one reader's own in a place of their own (the backseat's road, the silo, Drisking, the family copy) are
 * not here: their own cases build each reader's place. See docs/MULTIPLAYER_AUDIT.md.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_together") @PrefixGameTestTemplate(false)
public final class TogetherSceneTests {
    private TogetherSceneTests(){}
    /** How long both readers stay and go round the scene's people. */
    static final int ROUND=300;
    /** The round, plus time to build a big scene and a copy of it for the second reader. */
    private static final int TIMEOUT=ROUND+9600;
    private static LiteraryTests.Fixture open;

    /** Brings a reader in at a scene's base as its door does: onto the floor just inside, then every scene's own welcome. */
    private static void arrive(LiteraryTests.Fixture f,LabyrinthPlace place,ServerPlayer p,BlockPos base,LabyrinthData.Door entry){
        var l=f.level(place);p.stopRiding();p.teleportTo(l,base.getX()+.5,base.getY(),base.getZ()-3,180,0);p.hasChangedDimension();
        p.setNoGravity(true);p.setDeltaMovement(Vec3.ZERO);
        VignetteGate.begin(p,place);f.data().visit(p.getUUID(),place);LabyrinthDoors.scenesArrived(p,place,entry);
    }

    /** Two readers in one story: see the class comment. */
    static void together(GameTestHelper h,LabyrinthPlace place){
        var f=new LiteraryTests.Fixture(h);open=f;var server=f.in.getServer();
        SceneInstances.fixtureCap(1);
        h.assertTrue(LabyrinthBuilder.buildOne(server,place),place+": the House builds the story");f.built.add(place);
        var l=f.level(place);var b0=f.base(place);f.chunks.hold(l,IndianLakeRooms.bounds(b0,place));
        var first=NativeTestPlayers.survival(h,"scene_a"+place.ordinal());f.players.add(first);
        var second=NativeTestPlayers.survival(h,"scene_b"+place.ordinal());f.players.add(second);
        // The second waits in the hall at the House's origin until their door opens.
        second.teleportTo(f.in,f.origin.getX()+.5,f.origin.getY()+2,f.origin.getZ()+.5,0,0);second.setNoGravity(true);second.setDeltaMovement(Vec3.ZERO);
        final BlockPos[] bases={b0,null};final LabyrinthData.Door[] doors={null,null};final String[] how={""};
        h.startSequence()
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),place+": the first reader's scene is resident"))
            .thenExecute(()->{
                // The first comes through the door too, where the story has copies: the door is what keeps a scene for its reader.
                if(SceneInstances.copyable(place)){var e=SceneInstances.enter(first,place);h.assertTrue(e.door()!=null&&e.copy()==0,place+": the first reader's door opens onto the original: "+e);doors[0]=e.door();}
                else doors[0]=SceneInstances.entry(f.data(),place,0);
                h.assertTrue(doors[0]!=null,place+": the story has an entry door");
                arrive(f,place,first,b0,doors[0]);
            })
            .thenWaitUntil(()->{
                if(!SceneInstances.copyable(place)){bases[1]=b0;doors[1]=doors[0];how[0]="the House's one room for everyone";return;}
                var e=SceneInstances.enter(second,place);
                h.assertTrue(e.door()!=null&&!SceneInstances.building(),place+": waiting for the second reader's door: "+e+", copies "+SceneInstances.copies(f.data(),place));
                bases[1]=SceneInstances.base(server,place,e.copy());doors[1]=e.door();
                how[0]=e.copy()==0?"the same scene, written for more than one":"a copy of their own, "+(b0.getX()-bases[1].getX())+" blocks west";
            })
            .thenExecute(()->{
                h.assertTrue(bases[1]!=null,place+": the second reader's door opens onto a scene");
                // A private story gives the second reader a scene of their own; a party's or a shared room's takes them in with the first.
                boolean party=SceneInstances.party(place)>0||place==LabyrinthPlace.LAKE_HOUSE||place==LabyrinthPlace.PELL_HOUSE;
                if(SceneInstances.copyable(place)&&place!=LabyrinthPlace.LAKE_HOUSE)
                    h.assertTrue(party==bases[1].equals(b0),place+": the second reader goes "+(party?"in with the first":"into a scene of their own")+", not "+how[0]);
                if(!bases[1].equals(b0))f.chunks.hold(l,IndianLakeRooms.bounds(bases[1],place));
            })
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),place+": the second reader's scene is resident"))
            .thenExecute(()->{
                arrive(f,place,second,bases[1],doors[1]);
                if(LiteraryRooms.isLiterary(place)){
                    var seen=LiteraryVignettes.base(second,place);
                    h.assertTrue(bases[1].equals(seen),place+": the House has the second reader in the scene they went into, at "+bases[1].toShortString()+", not "+seen);
                }
                round(h,f,place,first,second,bases[0],bases[1],how[0]);
            });
    }

    /** Each reads their own paper, where there is one; then both go round the scene's people at once. */
    private static void round(GameTestHelper h,LiteraryTests.Fixture f,LabyrinthPlace place,ServerPlayer first,ServerPlayer second,BlockPos b0,BlockPos b1,String how){
        var l=f.level(place);boolean apart=!b0.equals(b1);
        var areaA=IndianLakeRooms.bounds(b0,place);var areaB=IndianLakeRooms.bounds(b1,place);
        SceneReady.whenTicking(h,l,areaA,2400,()->SceneReady.whenTicking(h,l,areaB,2400,()->{
            if(paper(place)){
                f.source(first,place);
                h.assertTrue(f.own(first,place).getBoolean("Read_Source"),place+": the first reader read their paper to its end");
                h.assertTrue(!f.own(second,place).getBoolean("Read_Source"),place+": one reader's reading is not the other's, in "+how);
                f.source(second,place);
                h.assertTrue(f.own(second,place).getBoolean("Read_Source"),place+": the second reader read their own paper, in "+how);
            }
            var conductA=allow(place,new CastConduct(h,l,areaA,()->apart?List.of(first):List.of(first,second)));
            var conductB=apart?allow(place,new CastConduct(h,l,areaB,()->List.of(second))):conductA;
            var botA=new CastConductTests.TalkBot(first,l,areaA,conductA);var botB=new CastConductTests.TalkBot(second,l,areaB,conductB);
            final int[] done={0};
            Runnable finish=()->{
                if(++done[0]<2)return;
                try{
                    conductA.assertClean(place.name()+" with two readers, "+how+" (first: "+botA.summary()+"; second: "+botB.summary()+")");
                    if(apart)conductB.assertClean(place.name()+", the second reader's copy ("+botB.summary()+")");
                    if(!DEADLY.contains(place))h.assertTrue(first.isAlive()&&second.isAlive(),place+": both readers are still alive after the round");
                    h.succeed();
                }finally{conductA.close();if(apart)conductB.close();close();}
            };
            botA.run(h,ROUND,finish);botB.run(h,ROUND,finish);
        }));
    }
    /**
     * What a story's own people do on purpose, which the cast checks would otherwise call a fault. Each is the scene as written: the
     * Masque's fourteen dancers to a reader, the two parents asleep in the split feed, the four of the painted family propped about
     * the boarded house, the swimmer in her lane, the woman who climbs out of the doe on the deer road, and a child carried on a
     * reader's back in the closing hall.
     */
    static CastConduct allow(LabyrinthPlace place,CastConduct c){
        return switch(place){
            case MASQUE->c.several(a->a.role()==LiteraryActor.DANCER,14);
            // The Lady glides her lane to the pond and out over its water before it takes her (LiteraryMotion.lady), as a ghost does.
            case BLY_ROUTE->c.floating(a->a.role()==LiteraryActor.LADY);
            case MISSING_SLEEP->c.several(a->a.role()==LiteraryActor.UNCANNY_PARENT,2);
            case PAINTED_INTERIOR->c.several(a->a.role()==LiteraryActor.UNCANNY_PROPPED,4);
            case LAST_SWIMMER->c.swimming(a->a.role()==LiteraryActor.STRANGER&&LastSwimmer.swimmer(a.phase()));
            case DEER_ROAD->c.emerges(a->a.role()==LiteraryActor.ROAD_WOMAN&&a.phase()==DeerRoad.EMERGE);
            case CLOSING_HALL->c.floating(a->a.role()==LiteraryActor.STRANGER&&ClosingHall.child(a.phase())&&ClosingHall.onBack(a));
            default->c;
        };
    }
    /** Stories whose people kill a reader who walks up to them, as these readers walk up to everyone: Camp Blood's man. */
    private static final Set<LabyrinthPlace> DEADLY=EnumSet.of(LabyrinthPlace.CAMP_BLOOD);
    /** A story whose paper is read on its own native page surface, at {@link LiteraryRooms#source}. */
    private static boolean paper(LabyrinthPlace place){return LiteraryRooms.isLiterary(place)&&!NO_PAPER.contains(place);}
    /** Literary stories whose source is not read on a page surface where the reader stands: each case says why. */
    private static final Set<LabyrinthPlace> NO_PAPER=EnumSet.noneOf(LabyrinthPlace.class);
    private static void close(){SceneInstances.fixtureCap(null);SceneInstances.clear();if(open!=null){var f=open;open=null;f.close();}}

    // ------------------------------------------------------------------------------------------------ the stories
    @GameTest(template="empty",batch="scene_floorboards",timeoutTicks=TIMEOUT) public static void floorboards(GameTestHelper h){together(h,LabyrinthPlace.FLOORBOARDS);}
    @AfterBatch(batch="scene_floorboards") public static void floorboardsDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_hide_and_clap",timeoutTicks=TIMEOUT) public static void hideAndClap(GameTestHelper h){together(h,LabyrinthPlace.HIDE_AND_CLAP);}
    @AfterBatch(batch="scene_hide_and_clap") public static void hideAndClapDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_model_home",timeoutTicks=TIMEOUT) public static void modelHome(GameTestHelper h){together(h,LabyrinthPlace.MODEL_HOME);}
    @AfterBatch(batch="scene_model_home") public static void modelHomeDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_drowned_town",timeoutTicks=TIMEOUT) public static void drownedTown(GameTestHelper h){together(h,LabyrinthPlace.DROWNED_TOWN);}
    @AfterBatch(batch="scene_drowned_town") public static void drownedTownDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_preserved_cave",timeoutTicks=TIMEOUT) public static void preservedCave(GameTestHelper h){together(h,LabyrinthPlace.PRESERVED_CAVE);}
    @AfterBatch(batch="scene_preserved_cave") public static void preservedCaveDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_shallows",timeoutTicks=TIMEOUT) public static void shallows(GameTestHelper h){together(h,LabyrinthPlace.SHALLOWS);}
    @AfterBatch(batch="scene_shallows") public static void shallowsDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_phone_canoe",timeoutTicks=TIMEOUT) public static void phoneCanoe(GameTestHelper h){together(h,LabyrinthPlace.PHONE_CANOE);}
    @AfterBatch(batch="scene_phone_canoe") public static void phoneCanoeDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_goatman",timeoutTicks=TIMEOUT) public static void goatman(GameTestHelper h){together(h,LabyrinthPlace.GOATMAN);}
    @AfterBatch(batch="scene_goatman") public static void goatmanDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_ted_caver",timeoutTicks=TIMEOUT) public static void tedCaver(GameTestHelper h){together(h,LabyrinthPlace.TED_CAVER);}
    @AfterBatch(batch="scene_ted_caver") public static void tedCaverDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_zampano",timeoutTicks=TIMEOUT) public static void zampano(GameTestHelper h){together(h,LabyrinthPlace.ZAMPANO_COURTYARD);}
    @AfterBatch(batch="scene_zampano") public static void zampanoDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_whale",timeoutTicks=TIMEOUT) public static void whale(GameTestHelper h){together(h,LabyrinthPlace.WHALE);}
    @AfterBatch(batch="scene_whale") public static void whaleDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_barn_well",timeoutTicks=TIMEOUT) public static void barnWell(GameTestHelper h){together(h,LabyrinthPlace.BARN_WELL);}
    @AfterBatch(batch="scene_barn_well") public static void barnWellDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_plain",timeoutTicks=TIMEOUT) public static void plain(GameTestHelper h){together(h,LabyrinthPlace.PLAIN);}
    @AfterBatch(batch="scene_plain") public static void plainDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_hospital",timeoutTicks=TIMEOUT) public static void hospital(GameTestHelper h){together(h,LabyrinthPlace.HOSPITAL);}
    @AfterBatch(batch="scene_hospital") public static void hospitalDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_holloway",timeoutTicks=TIMEOUT) public static void holloway(GameTestHelper h){together(h,LabyrinthPlace.HOLLOWAY_CAMP);}
    @AfterBatch(batch="scene_holloway") public static void hollowayDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_mother",timeoutTicks=TIMEOUT) public static void mother(GameTestHelper h){together(h,LabyrinthPlace.MOTHER_DEN);}
    @AfterBatch(batch="scene_mother") public static void motherDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_seance",timeoutTicks=TIMEOUT) public static void seance(GameTestHelper h){together(h,LabyrinthPlace.SEANCE);}
    @AfterBatch(batch="scene_seance") public static void seanceDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_wallpaper",timeoutTicks=TIMEOUT) public static void wallpaper(GameTestHelper h){together(h,LabyrinthPlace.WALLPAPER_NURSERY);}
    @AfterBatch(batch="scene_wallpaper") public static void wallpaperDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_hotel",timeoutTicks=TIMEOUT) public static void hotel(GameTestHelper h){together(h,LabyrinthPlace.HOTEL);}
    @AfterBatch(batch="scene_hotel") public static void hotelDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_hill_nursery",timeoutTicks=TIMEOUT) public static void hillNursery(GameTestHelper h){together(h,LabyrinthPlace.HILL_NURSERY);}
    @AfterBatch(batch="scene_hill_nursery") public static void hillNurseryDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_miniatures",timeoutTicks=TIMEOUT) public static void miniatures(GameTestHelper h){together(h,LabyrinthPlace.MINIATURES);}
    @AfterBatch(batch="scene_miniatures") public static void miniaturesDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_masque",timeoutTicks=TIMEOUT) public static void masque(GameTestHelper h){together(h,LabyrinthPlace.MASQUE);}
    @AfterBatch(batch="scene_masque") public static void masqueDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_usher",timeoutTicks=TIMEOUT) public static void usher(GameTestHelper h){together(h,LabyrinthPlace.USHER);}
    @AfterBatch(batch="scene_usher") public static void usherDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_winchester",timeoutTicks=TIMEOUT) public static void winchester(GameTestHelper h){together(h,LabyrinthPlace.WINCHESTER);}
    @AfterBatch(batch="scene_winchester") public static void winchesterDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_child_room",timeoutTicks=TIMEOUT) public static void childRoom(GameTestHelper h){together(h,LabyrinthPlace.CHILD_ROOM);}
    @AfterBatch(batch="scene_child_room") public static void childRoomDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_crimson_hall",timeoutTicks=TIMEOUT) public static void crimsonHall(GameTestHelper h){together(h,LabyrinthPlace.CRIMSON_HALL);}
    @AfterBatch(batch="scene_crimson_hall") public static void crimsonHallDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_bly",timeoutTicks=TIMEOUT) public static void bly(GameTestHelper h){together(h,LabyrinthPlace.BLY_ROUTE);}
    @AfterBatch(batch="scene_bly") public static void blyDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_elk_lot",timeoutTicks=TIMEOUT) public static void elkLot(GameTestHelper h){together(h,LabyrinthPlace.ELK_LOT);}
    @AfterBatch(batch="scene_elk_lot") public static void elkLotDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_elk_fan",timeoutTicks=TIMEOUT) public static void elkFan(GameTestHelper h){together(h,LabyrinthPlace.ELK_FAN);}
    @AfterBatch(batch="scene_elk_fan") public static void elkFanDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_mapping",timeoutTicks=TIMEOUT) public static void mapping(GameTestHelper h){together(h,LabyrinthPlace.MAPPING_INTERIOR);}
    @AfterBatch(batch="scene_mapping") public static void mappingDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_holy_rabbit",timeoutTicks=TIMEOUT) public static void holyRabbit(GameTestHelper h){together(h,LabyrinthPlace.HOLY_RABBIT);}
    @AfterBatch(batch="scene_holy_rabbit") public static void holyRabbitDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_elk_carcasses",timeoutTicks=TIMEOUT) public static void elkCarcasses(GameTestHelper h){together(h,LabyrinthPlace.ELK_CARCASSES);}
    @AfterBatch(batch="scene_elk_carcasses") public static void elkCarcassesDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_costume_night",timeoutTicks=TIMEOUT) public static void costumeNight(GameTestHelper h){together(h,LabyrinthPlace.COSTUME_NIGHT);}
    @AfterBatch(batch="scene_costume_night") public static void costumeNightDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_movie_night",timeoutTicks=TIMEOUT) public static void movieNight(GameTestHelper h){together(h,LabyrinthPlace.MOVIE_NIGHT);}
    @AfterBatch(batch="scene_movie_night") public static void movieNightDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_winter_lake",timeoutTicks=TIMEOUT) public static void winterLake(GameTestHelper h){together(h,LabyrinthPlace.WINTER_LAKE);}
    @AfterBatch(batch="scene_winter_lake") public static void winterLakeDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_camp_blood",timeoutTicks=TIMEOUT) public static void campBlood(GameTestHelper h){together(h,LabyrinthPlace.CAMP_BLOOD);}
    @AfterBatch(batch="scene_camp_blood") public static void campBloodDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_devils_rock",timeoutTicks=TIMEOUT) public static void devilsRock(GameTestHelper h){together(h,LabyrinthPlace.DEVILS_ROCK);}
    @AfterBatch(batch="scene_devils_rock") public static void devilsRockDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_wheel",timeoutTicks=TIMEOUT) public static void wheel(GameTestHelper h){together(h,LabyrinthPlace.WHEEL);}
    @AfterBatch(batch="scene_wheel") public static void wheelDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_end_world_cabin",timeoutTicks=TIMEOUT) public static void endWorldCabin(GameTestHelper h){together(h,LabyrinthPlace.END_WORLD_CABIN);}
    @AfterBatch(batch="scene_end_world_cabin") public static void endWorldCabinDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_lake_house",timeoutTicks=TIMEOUT) public static void lakeHouse(GameTestHelper h){together(h,LabyrinthPlace.LAKE_HOUSE);}
    @AfterBatch(batch="scene_lake_house") public static void lakeHouseDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_painted_interior",timeoutTicks=TIMEOUT) public static void paintedInterior(GameTestHelper h){together(h,LabyrinthPlace.PAINTED_INTERIOR);}
    @AfterBatch(batch="scene_painted_interior") public static void paintedInteriorDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_second_waitress",timeoutTicks=TIMEOUT) public static void secondWaitress(GameTestHelper h){together(h,LabyrinthPlace.SECOND_WAITRESS);}
    @AfterBatch(batch="scene_second_waitress") public static void secondWaitressDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_tunnel_periphery",timeoutTicks=TIMEOUT) public static void tunnelPeriphery(GameTestHelper h){together(h,LabyrinthPlace.TUNNEL_PERIPHERY);}
    @AfterBatch(batch="scene_tunnel_periphery") public static void tunnelPeripheryDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_missing_sleep",timeoutTicks=TIMEOUT) public static void missingSleep(GameTestHelper h){together(h,LabyrinthPlace.MISSING_SLEEP);}
    @AfterBatch(batch="scene_missing_sleep") public static void missingSleepDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_layered_man",timeoutTicks=TIMEOUT) public static void layeredMan(GameTestHelper h){together(h,LabyrinthPlace.LAYERED_MAN);}
    @AfterBatch(batch="scene_layered_man") public static void layeredManDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_motion_light",timeoutTicks=TIMEOUT) public static void motionLight(GameTestHelper h){together(h,LabyrinthPlace.MOTION_LIGHT);}
    @AfterBatch(batch="scene_motion_light") public static void motionLightDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_dare",timeoutTicks=TIMEOUT) public static void dare(GameTestHelper h){together(h,LabyrinthPlace.DARE_IN_THE_DARK);}
    @AfterBatch(batch="scene_dare") public static void dareDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_feet_up",timeoutTicks=TIMEOUT) public static void feetUp(GameTestHelper h){together(h,LabyrinthPlace.FEET_UP);}
    @AfterBatch(batch="scene_feet_up") public static void feetUpDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_whistlers",timeoutTicks=TIMEOUT) public static void whistlers(GameTestHelper h){together(h,LabyrinthPlace.WHISTLERS);}
    @AfterBatch(batch="scene_whistlers") public static void whistlersDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_work_print",timeoutTicks=TIMEOUT) public static void workPrint(GameTestHelper h){together(h,LabyrinthPlace.WORK_PRINT);}
    @AfterBatch(batch="scene_work_print") public static void workPrintDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_polaroid_trail",timeoutTicks=TIMEOUT) public static void polaroidTrail(GameTestHelper h){together(h,LabyrinthPlace.POLAROID_TRAIL);}
    @AfterBatch(batch="scene_polaroid_trail") public static void polaroidTrailDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_left_right",timeoutTicks=TIMEOUT) public static void leftRight(GameTestHelper h){together(h,LabyrinthPlace.LEFT_RIGHT);}
    @AfterBatch(batch="scene_left_right") public static void leftRightDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_dead_air",timeoutTicks=TIMEOUT) public static void deadAir(GameTestHelper h){together(h,LabyrinthPlace.DEAD_AIR);}
    @AfterBatch(batch="scene_dead_air") public static void deadAirDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_closing_hall",timeoutTicks=TIMEOUT) public static void closingHall(GameTestHelper h){together(h,LabyrinthPlace.CLOSING_HALL);}
    @AfterBatch(batch="scene_closing_hall") public static void closingHallDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_storeroom",timeoutTicks=TIMEOUT) public static void storeroom(GameTestHelper h){together(h,LabyrinthPlace.STOREROOM);}
    @AfterBatch(batch="scene_storeroom") public static void storeroomDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_stillness",timeoutTicks=TIMEOUT) public static void stillness(GameTestHelper h){together(h,LabyrinthPlace.STILLNESS);}
    @AfterBatch(batch="scene_stillness") public static void stillnessDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_line",timeoutTicks=TIMEOUT) public static void line(GameTestHelper h){together(h,LabyrinthPlace.LINE);}
    @AfterBatch(batch="scene_line") public static void lineDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_own_light",timeoutTicks=TIMEOUT) public static void ownLight(GameTestHelper h){together(h,LabyrinthPlace.OWN_LIGHT);}
    @AfterBatch(batch="scene_own_light") public static void ownLightDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_polybius",timeoutTicks=TIMEOUT) public static void polybius(GameTestHelper h){together(h,LabyrinthPlace.POLYBIUS);}
    @AfterBatch(batch="scene_polybius") public static void polybiusDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_replacement",timeoutTicks=TIMEOUT) public static void replacement(GameTestHelper h){together(h,LabyrinthPlace.REPLACEMENT);}
    @AfterBatch(batch="scene_replacement") public static void replacementDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_vacant_possession",timeoutTicks=TIMEOUT) public static void vacantPossession(GameTestHelper h){together(h,LabyrinthPlace.VACANT_POSSESSION);}
    @AfterBatch(batch="scene_vacant_possession") public static void vacantPossessionDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_last_swimmer",timeoutTicks=TIMEOUT) public static void lastSwimmer(GameTestHelper h){together(h,LabyrinthPlace.LAST_SWIMMER);}
    @AfterBatch(batch="scene_last_swimmer") public static void lastSwimmerDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_deer_road",timeoutTicks=TIMEOUT) public static void deerRoad(GameTestHelper h){together(h,LabyrinthPlace.DEER_ROAD);}
    @AfterBatch(batch="scene_deer_road") public static void deerRoadDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_state_route",timeoutTicks=TIMEOUT) public static void stateRoute(GameTestHelper h){together(h,LabyrinthPlace.STATE_ROUTE);}
    @AfterBatch(batch="scene_state_route") public static void stateRouteDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_tall_grass",timeoutTicks=TIMEOUT) public static void tallGrass(GameTestHelper h){together(h,LabyrinthPlace.TALL_GRASS);}
    @AfterBatch(batch="scene_tall_grass") public static void tallGrassDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="scene_black_city",timeoutTicks=TIMEOUT) public static void blackCity(GameTestHelper h){together(h,LabyrinthPlace.BLACK_CITY);}
    @AfterBatch(batch="scene_black_city") public static void blackCityDone(ServerLevel l){close();}
}
