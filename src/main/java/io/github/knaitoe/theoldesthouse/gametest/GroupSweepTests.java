package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import java.util.function.BooleanSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.740, the multiplayer sweep (the owner: "running additional redundancy tests for multiplayer capabilities for each vignette";
 * "Two or more. Looking for exclusively two may cause you to overlook things"). {@link TogetherSceneTests} takes two readers into each
 * story. This takes as many as each story's door is written for, and one more, then has one of them leave and come back, and a
 * spectator walk in. See docs/MULTIPLAYER_AUDIT.md.
 *
 * <p>How many readers go in, one at a time, each through the House's door ({@link SceneInstances#enter}) and each scene's own welcome:
 * <ul>
 * <li>a story written for a party ({@link SceneInstances#party}): a full party, then one more. The full party is in one copy, and at
 * two, at three and at the full party the copy holds exactly the readers in it; the one more goes into a second copy;</li>
 * <li>the lake house and the Pell house, kept in pairs: three, the pair together and the third in a second house;</li>
 * <li>a private story (a copy per reader): three, each in a copy of their own, and a fourth who finds nothing free while the server
 * allows no more copies;</li>
 * <li>a story that is one room for everyone: six, all in the one room.</li>
 * </ul>
 * Then:
 * <ul>
 * <li>each reads their own paper (where the story has one), and nobody's reading is anybody else's. At Camp Blood the man kills
 * whoever he reaches while the party gathers, as the story is written; those he has reached are not asked to read;</li>
 * <li>a spectator walks into the first copy: it is not in the story, holds no place in it, and earns nothing, even at the paper;</li>
 * <li>the first reader drops out of the game: the others keep what they have earned, every copy still stands, and nobody else is put
 * out of the story;</li>
 * <li>the first reader comes back as the same player: their door opens onto the copy they were in, and what they had read is still
 * theirs.</li>
 * </ul>
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_group") @PrefixGameTestTemplate(false)
public final class GroupSweepTests {
    private GroupSweepTests(){}
    /** Time to build a big scene and two copies of it, and take everyone in. */
    private static final int TIMEOUT=30000;
    /** How long the others are watched after the first drops out, and after they come back. */
    static final int AFTER=60;
    private static LiteraryTests.Fixture open;

    /** How a story's door takes a group in. */
    enum Kind{PARTY,PAIR,PRIVATE,SHARED}
    static Kind kind(LabyrinthPlace place){
        if(place==LabyrinthPlace.LAKE_HOUSE||place==LabyrinthPlace.PELL_HOUSE)return Kind.PAIR;
        if(SceneInstances.party(place)>0)return Kind.PARTY;
        return SceneInstances.copyable(place)?Kind.PRIVATE:Kind.SHARED;
    }
    /** How many readers go in. */
    static int readers(LabyrinthPlace place){
        return switch(kind(place)){case PARTY->SceneInstances.party(place)+1;case PAIR,PRIVATE->3;case SHARED->6;};
    }
    /** The copy reader {@code i} should be taken into. */
    static int expected(LabyrinthPlace place,int i){
        return switch(kind(place)){
            case PARTY->i<SceneInstances.party(place)?0:1;
            case PAIR->i<2?0:1;
            case PRIVATE->i;
            case SHARED->0;
        };
    }

    /** Asks every tick until {@code ok}, then runs {@code then}; fails after {@code max} ticks with {@code what}. */
    static void until(GameTestHelper h,BooleanSupplier ok,int max,java.util.function.Supplier<String> what,Runnable then){step(h,ok,max,what,then,0);}
    private static void step(GameTestHelper h,BooleanSupplier ok,int max,java.util.function.Supplier<String> what,Runnable then,int t){
        if(ok.getAsBoolean()){then.run();return;}
        if(t>=max)throw new GameTestAssertException(what.get());
        h.runAfterDelay(1,()->step(h,ok,max,what,then,t+1));
    }

    /** One story's sweep: see the class comment. */
    static void sweep(GameTestHelper h,LabyrinthPlace place){new Sweep(h,place).start();}

    static final class Sweep{
        final GameTestHelper h;final LabyrinthPlace place;final LiteraryTests.Fixture f;final ServerLevel l;final Kind kind;final int n;
        final List<ServerPlayer> group=new ArrayList<>();final List<Integer> copy=new ArrayList<>();final Map<Integer,BlockPos> bases=new HashMap<>();
        final List<String> names=new ArrayList<>();final List<UUID> ids=new ArrayList<>();
        /** The readers who read their paper: all of them, but at Camp Blood, whose man kills a reader he reaches, the living. */
        final Set<UUID> read=new HashSet<>();
        Sweep(GameTestHelper h,LabyrinthPlace place){
            this.h=h;this.place=place;f=new LiteraryTests.Fixture(h);open=f;l=f.level(place);kind=kind(place);n=readers(place);
            SceneInstances.fixtureCap(kind==Kind.PRIVATE?2:1);
        }
        String tag(){return place+" ("+kind+", "+n+" readers)";}
        void start(){
            h.assertTrue(LabyrinthBuilder.buildOne(f.in.getServer(),place),tag()+": the House builds the story");f.built.add(place);
            var b0=f.base(place);bases.put(0,b0);f.chunks.hold(l,IndianLakeRooms.bounds(b0,place));
            until(h,f.chunks::ready,2400,()->tag()+": the story is resident",()->admit(0));
        }
        /** Takes reader {@code i} in through the door, then the next. */
        void admit(int i){
            if(i==n){overfull();return;}
            var name="group"+i+"_"+place.ordinal();var p=NativeTestPlayers.survival(h,name);f.players.add(p);group.add(p);names.add(name);ids.add(p.getUUID());
            hall(p);
            final LabyrinthData.Door[] door={null};final int[] k={-1};
            until(h,()->{
                if(kind==Kind.SHARED){door[0]=SceneInstances.entry(f.data(),place,0);k[0]=0;return door[0]!=null;}
                var e=SceneInstances.enter(p,place);
                if(e.door()==null||SceneInstances.building())return false;
                door[0]=e.door();k[0]=e.copy();return true;
            },12000,()->tag()+": reader "+(i+1)+"'s door never opened; copies "+SceneInstances.copies(f.data(),place),()->{
                h.assertTrue(k[0]==expected(place,i),tag()+": reader "+(i+1)+" goes into copy "+expected(place,i)+", not "+k[0]);
                copy.add(k[0]);
                var b=bases.computeIfAbsent(k[0],c->SceneInstances.base(f.in.getServer(),place,c));
                h.assertTrue(b!=null,tag()+": copy "+k[0]+" stands");
                f.chunks.hold(l,IndianLakeRooms.bounds(b,place));
                until(h,f.chunks::ready,2400,()->tag()+": copy "+k[0]+" is resident",()->{
                    arrive(p,b,door[0]);
                    if(kind==Kind.PARTY){
                        int in=i+1;
                        if(in==2||in==3||in==SceneInstances.party(place)){
                            int held=SceneInstances.held(f.in.getServer(),place,0,new UUID(0,0));
                            h.assertTrue(held==in,tag()+": with "+in+" readers in, the first copy holds "+in+", not "+held);
                        }
                    }
                    if(LiteraryRooms.isLiterary(place)){
                        var seen=LiteraryVignettes.base(p,place);
                        h.assertTrue(b.equals(seen),tag()+": the House has reader "+(i+1)+" in the copy they went into, "+b.toShortString()+", not "+seen);
                    }
                    admit(i+1);
                });
            });
        }
        /** A private story: one more reader than copies finds nothing free and nothing being made. */
        void overfull(){
            if(kind==Kind.PRIVATE){
                var extra=NativeTestPlayers.survival(h,"group_over_"+place.ordinal());f.players.add(extra);hall(extra);
                var e=SceneInstances.enter(extra,place);
                h.assertTrue(e.door()==null&&!e.waiting(),tag()+": a fourth reader, with every copy the server allows in use, is turned away: "+e);
                f.players.remove(extra);NativeTestPlayers.remove(extra);
            }
            ticking(new ArrayList<>(new TreeSet<>(bases.keySet())),0,this::read);
        }
        void ticking(List<Integer> ks,int j,Runnable then){
            if(j==ks.size()){then.run();return;}
            SceneReady.whenTicking(h,l,IndianLakeRooms.bounds(bases.get(ks.get(j)),place),2400,()->ticking(ks,j+1,then));
        }
        /** Each reads their own paper; nobody else's reading moves. */
        void read(){
            if(paper(place))for(int i=0;i<group.size();i++){
                var p=group.get(i);
                if(TogetherSceneTests.deadly(place)&&!p.isAlive())continue;
                h.assertTrue(!f.own(p,place).getBoolean("Read_Source"),tag()+": reader "+(i+1)+" has not read before they read");
                try{f.source(p,place);}catch(GameTestAssertException e){throw new GameTestAssertException(tag()+": reader "+(i+1)+" of copy "+copy.get(i)+" (alive "+p.isAlive()+", health "+p.getHealth()+", at "+p.blockPosition().toShortString()+"): "+e.getMessage());}
                h.assertTrue(f.own(p,place).getBoolean("Read_Source"),tag()+": reader "+(i+1)+" read their own paper");read.add(p.getUUID());
                for(int j=i+1;j<group.size();j++)h.assertTrue(!f.own(group.get(j),place).getBoolean("Read_Source"),tag()+": reader "+(i+1)+"'s reading is not reader "+(j+1)+"'s");
            }
            spectator();
        }
        /** A spectator in the first copy is nobody in it. */
        void spectator(){
            var s=NativeTestPlayers.survival(h,"group_watch_"+place.ordinal());f.players.add(s);
            s.setGameMode(GameType.SPECTATOR);
            var b=bases.get(0);s.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-3,180,0);s.hasChangedDimension();s.setDeltaMovement(Vec3.ZERO);
            int before=SceneInstances.copyable(place)?SceneInstances.held(f.in.getServer(),place,0,new UUID(0,0)):0;
            if(paper(place)){
                try{f.click(s,place,LiteraryRooms.source(place));}catch(RuntimeException ignored){/* a spectator's click may find no copy of theirs */}
                s.closeContainer();
            }
            h.runAfterDelay(20,()->{
                h.assertTrue(!LiteraryVignettes.inside(s,place),tag()+": a spectator is not in the story");
                h.assertTrue(!f.own(s,place).getBoolean("Read_Source"),tag()+": a spectator at the paper reads nothing");
                h.assertTrue(WitnessAccount.record(f.data(),s.getUUID()).getCompound("Stories").isEmpty(),tag()+": a spectator earns no Witness story");
                // 0.4.746: what a spectator is sent is what they see. A reader's own cast (bound to them, or to their thread) is never sent
                // to a spectator; the shared cast is, as it is to anyone there. The counts go to the log for the audit.
                int shared=0,own=0;
                for(var a:l.getEntitiesOfClass(LiteraryActor.class,new net.minecraft.world.phys.AABB(b).inflate(96))){
                    boolean bound=a.owner().isPresent()||a.getPersistentData().contains(VignetteThreads.ACTOR_TAG);
                    boolean copy=a.scene().equals(LabyrinthPlace.FAMILY_COPY.id())||a.scene().equals(LabyrinthPlace.OLD_CABIN.id());
                    if(bound&&!copy){own++;h.assertTrue(!a.broadcastToPlayer(s),tag()+": a reader's own "+a.getName().getString()+" (role "+a.role()+") is not sent to a spectator");}
                    else if(a.broadcastToPlayer(s))shared++;
                }
                TheOldestHouse.LOGGER.info("GROUP SWEEP spectator {}: {} shared cast sent, {} readers' own cast withheld",place.id(),shared,own);
                if(SceneInstances.copyable(place)){
                    int now=SceneInstances.held(f.in.getServer(),place,0,new UUID(0,0));
                    h.assertTrue(now==before,tag()+": a spectator holds no place: "+before+" held before, "+now+" with them there");
                }
                f.players.remove(s);NativeTestPlayers.remove(s);
                drop();
            });
        }
        /** The first reader drops out of the game; nothing of the others' is lost. */
        void drop(){
            var first=group.get(0);int copies=SceneInstances.copies(f.data(),place);
            f.players.remove(first);NativeTestPlayers.remove(first);
            h.runAfterDelay(AFTER,()->{
                h.assertTrue(SceneInstances.copies(f.data(),place)==copies,tag()+": no copy is given up when one reader drops out");
                for(var k:bases.keySet())h.assertTrue(k==0||SceneInstances.base(f.in.getServer(),place,k)!=null,tag()+": copy "+k+" still stands");
                others(1,"after the first reader dropped out");
                reconnect();
            });
        }
        /** The others: still alive (where the story's people do not kill), still in the story, still with what they read. */
        void others(int from,String when){
            for(int i=from;i<group.size();i++){
                var p=group.get(i);
                if(!TogetherSceneTests.deadly(place))h.assertTrue(p.isAlive(),tag()+": reader "+(i+1)+" is alive "+when);
                if(read.contains(p.getUUID()))h.assertTrue(f.own(p,place).getBoolean("Read_Source"),tag()+": reader "+(i+1)+" keeps their reading "+when);
                if(LiteraryRooms.isLiterary(place)&&p.isAlive()){
                    var seen=LiteraryVignettes.base(p,place);var b=bases.get(copy.get(i));
                    h.assertTrue(b.equals(seen),tag()+": reader "+(i+1)+" is still in copy "+copy.get(i)+" "+when+", not "+seen);
                }
            }
        }
        /** The first reader comes back as the same player. */
        void reconnect(){
            var back=NativeTestPlayers.survival(h,names.get(0),ids.get(0));f.players.add(back);group.set(0,back);hall(back);
            final LabyrinthData.Door[] door={null};final int[] k={-1};
            until(h,()->{
                if(kind==Kind.SHARED){door[0]=SceneInstances.entry(f.data(),place,0);k[0]=0;return door[0]!=null;}
                var e=SceneInstances.enter(back,place);
                if(e.door()==null||SceneInstances.building())return false;
                door[0]=e.door();k[0]=e.copy();return true;
            },2400,()->tag()+": the first reader's door never opened again",()->{
                h.assertTrue(k[0]==copy.get(0),tag()+": the first reader comes back into copy "+copy.get(0)+", not "+k[0]);
                arrive(back,bases.get(k[0]),door[0]);
                if(read.contains(back.getUUID()))h.assertTrue(f.own(back,place).getBoolean("Read_Source"),tag()+": the first reader's reading is still theirs when they come back");
                h.runAfterDelay(AFTER,()->{
                    others(0,"after the first reader came back");
                    h.succeed();
                    close();
                });
            });
        }
        /** Waits in the hall at the House's origin. */
        void hall(ServerPlayer p){p.teleportTo(f.in,f.origin.getX()+.5,f.origin.getY()+2,f.origin.getZ()+.5,0,0);p.setNoGravity(true);p.setDeltaMovement(Vec3.ZERO);}
        /** In at a copy's base as its door brings a reader: onto the floor just inside, then every scene's own welcome. */
        void arrive(ServerPlayer p,BlockPos base,LabyrinthData.Door entry){
            p.stopRiding();p.teleportTo(l,base.getX()+.5,base.getY(),base.getZ()-3,180,0);p.hasChangedDimension();
            p.setNoGravity(true);p.setDeltaMovement(Vec3.ZERO);
            VignetteGate.begin(p,place);f.data().visit(p.getUUID(),place);LabyrinthDoors.scenesArrived(p,place,entry);
        }
    }
    private static boolean paper(LabyrinthPlace place){return LiteraryRooms.isLiterary(place);}
    private static void close(){SceneInstances.fixtureCap(null);SceneInstances.clear();if(open!=null){var f=open;open=null;f.close();}}

    // ---------------------------------------------------------------------------------------------- every story
    @GameTest(template="empty",batch="group_floorboards",timeoutTicks=TIMEOUT) public static void floorboards(GameTestHelper h){sweep(h,LabyrinthPlace.FLOORBOARDS);}
    @AfterBatch(batch="group_floorboards") public static void floorboardsDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_hide_and_clap",timeoutTicks=TIMEOUT) public static void hideAndClap(GameTestHelper h){sweep(h,LabyrinthPlace.HIDE_AND_CLAP);}
    @AfterBatch(batch="group_hide_and_clap") public static void hideAndClapDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_model_home",timeoutTicks=TIMEOUT) public static void modelHome(GameTestHelper h){sweep(h,LabyrinthPlace.MODEL_HOME);}
    @AfterBatch(batch="group_model_home") public static void modelHomeDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_drowned_town",timeoutTicks=TIMEOUT) public static void drownedTown(GameTestHelper h){sweep(h,LabyrinthPlace.DROWNED_TOWN);}
    @AfterBatch(batch="group_drowned_town") public static void drownedTownDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_preserved_cave",timeoutTicks=TIMEOUT) public static void preservedCave(GameTestHelper h){sweep(h,LabyrinthPlace.PRESERVED_CAVE);}
    @AfterBatch(batch="group_preserved_cave") public static void preservedCaveDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_shallows",timeoutTicks=TIMEOUT) public static void shallows(GameTestHelper h){sweep(h,LabyrinthPlace.SHALLOWS);}
    @AfterBatch(batch="group_shallows") public static void shallowsDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_phone_canoe",timeoutTicks=TIMEOUT) public static void phoneCanoe(GameTestHelper h){sweep(h,LabyrinthPlace.PHONE_CANOE);}
    @AfterBatch(batch="group_phone_canoe") public static void phoneCanoeDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_goatman",timeoutTicks=TIMEOUT) public static void goatman(GameTestHelper h){sweep(h,LabyrinthPlace.GOATMAN);}
    @AfterBatch(batch="group_goatman") public static void goatmanDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_ted_caver",timeoutTicks=TIMEOUT) public static void tedCaver(GameTestHelper h){sweep(h,LabyrinthPlace.TED_CAVER);}
    @AfterBatch(batch="group_ted_caver") public static void tedCaverDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_zampano_courtyard",timeoutTicks=TIMEOUT) public static void zampanoCourtyard(GameTestHelper h){sweep(h,LabyrinthPlace.ZAMPANO_COURTYARD);}
    @AfterBatch(batch="group_zampano_courtyard") public static void zampanoCourtyardDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_whale",timeoutTicks=TIMEOUT) public static void whale(GameTestHelper h){sweep(h,LabyrinthPlace.WHALE);}
    @AfterBatch(batch="group_whale") public static void whaleDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_barn_well",timeoutTicks=TIMEOUT) public static void barnWell(GameTestHelper h){sweep(h,LabyrinthPlace.BARN_WELL);}
    @AfterBatch(batch="group_barn_well") public static void barnWellDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_plain",timeoutTicks=TIMEOUT) public static void plain(GameTestHelper h){sweep(h,LabyrinthPlace.PLAIN);}
    @AfterBatch(batch="group_plain") public static void plainDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_hospital",timeoutTicks=TIMEOUT) public static void hospital(GameTestHelper h){sweep(h,LabyrinthPlace.HOSPITAL);}
    @AfterBatch(batch="group_hospital") public static void hospitalDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_holloway_camp",timeoutTicks=TIMEOUT) public static void hollowayCamp(GameTestHelper h){sweep(h,LabyrinthPlace.HOLLOWAY_CAMP);}
    @AfterBatch(batch="group_holloway_camp") public static void hollowayCampDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_mother_den",timeoutTicks=TIMEOUT) public static void motherDen(GameTestHelper h){sweep(h,LabyrinthPlace.MOTHER_DEN);}
    @AfterBatch(batch="group_mother_den") public static void motherDenDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_seance",timeoutTicks=TIMEOUT) public static void seance(GameTestHelper h){sweep(h,LabyrinthPlace.SEANCE);}
    @AfterBatch(batch="group_seance") public static void seanceDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_wallpaper_nursery",timeoutTicks=TIMEOUT) public static void wallpaperNursery(GameTestHelper h){sweep(h,LabyrinthPlace.WALLPAPER_NURSERY);}
    @AfterBatch(batch="group_wallpaper_nursery") public static void wallpaperNurseryDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_hotel",timeoutTicks=TIMEOUT) public static void hotel(GameTestHelper h){sweep(h,LabyrinthPlace.HOTEL);}
    @AfterBatch(batch="group_hotel") public static void hotelDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_hill_nursery",timeoutTicks=TIMEOUT) public static void hillNursery(GameTestHelper h){sweep(h,LabyrinthPlace.HILL_NURSERY);}
    @AfterBatch(batch="group_hill_nursery") public static void hillNurseryDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_miniatures",timeoutTicks=TIMEOUT) public static void miniatures(GameTestHelper h){sweep(h,LabyrinthPlace.MINIATURES);}
    @AfterBatch(batch="group_miniatures") public static void miniaturesDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_masque",timeoutTicks=TIMEOUT) public static void masque(GameTestHelper h){sweep(h,LabyrinthPlace.MASQUE);}
    @AfterBatch(batch="group_masque") public static void masqueDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_usher",timeoutTicks=TIMEOUT) public static void usher(GameTestHelper h){sweep(h,LabyrinthPlace.USHER);}
    @AfterBatch(batch="group_usher") public static void usherDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_winchester",timeoutTicks=TIMEOUT) public static void winchester(GameTestHelper h){sweep(h,LabyrinthPlace.WINCHESTER);}
    @AfterBatch(batch="group_winchester") public static void winchesterDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_child_room",timeoutTicks=TIMEOUT) public static void childRoom(GameTestHelper h){sweep(h,LabyrinthPlace.CHILD_ROOM);}
    @AfterBatch(batch="group_child_room") public static void childRoomDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_crimson_hall",timeoutTicks=TIMEOUT) public static void crimsonHall(GameTestHelper h){sweep(h,LabyrinthPlace.CRIMSON_HALL);}
    @AfterBatch(batch="group_crimson_hall") public static void crimsonHallDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_bly_route",timeoutTicks=TIMEOUT) public static void blyRoute(GameTestHelper h){sweep(h,LabyrinthPlace.BLY_ROUTE);}
    @AfterBatch(batch="group_bly_route") public static void blyRouteDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_elk_lot",timeoutTicks=TIMEOUT) public static void elkLot(GameTestHelper h){sweep(h,LabyrinthPlace.ELK_LOT);}
    @AfterBatch(batch="group_elk_lot") public static void elkLotDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_elk_fan",timeoutTicks=TIMEOUT) public static void elkFan(GameTestHelper h){sweep(h,LabyrinthPlace.ELK_FAN);}
    @AfterBatch(batch="group_elk_fan") public static void elkFanDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_mapping_interior",timeoutTicks=TIMEOUT) public static void mappingInterior(GameTestHelper h){sweep(h,LabyrinthPlace.MAPPING_INTERIOR);}
    @AfterBatch(batch="group_mapping_interior") public static void mappingInteriorDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_holy_rabbit",timeoutTicks=TIMEOUT) public static void holyRabbit(GameTestHelper h){sweep(h,LabyrinthPlace.HOLY_RABBIT);}
    @AfterBatch(batch="group_holy_rabbit") public static void holyRabbitDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_elk_carcasses",timeoutTicks=TIMEOUT) public static void elkCarcasses(GameTestHelper h){sweep(h,LabyrinthPlace.ELK_CARCASSES);}
    @AfterBatch(batch="group_elk_carcasses") public static void elkCarcassesDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_costume_night",timeoutTicks=TIMEOUT) public static void costumeNight(GameTestHelper h){sweep(h,LabyrinthPlace.COSTUME_NIGHT);}
    @AfterBatch(batch="group_costume_night") public static void costumeNightDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_movie_night",timeoutTicks=TIMEOUT) public static void movieNight(GameTestHelper h){sweep(h,LabyrinthPlace.MOVIE_NIGHT);}
    @AfterBatch(batch="group_movie_night") public static void movieNightDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_winter_lake",timeoutTicks=TIMEOUT) public static void winterLake(GameTestHelper h){sweep(h,LabyrinthPlace.WINTER_LAKE);}
    @AfterBatch(batch="group_winter_lake") public static void winterLakeDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_camp_blood",timeoutTicks=TIMEOUT) public static void campBlood(GameTestHelper h){sweep(h,LabyrinthPlace.CAMP_BLOOD);}
    @AfterBatch(batch="group_camp_blood") public static void campBloodDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_devils_rock",timeoutTicks=TIMEOUT) public static void devilsRock(GameTestHelper h){sweep(h,LabyrinthPlace.DEVILS_ROCK);}
    @AfterBatch(batch="group_devils_rock") public static void devilsRockDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_wheel",timeoutTicks=TIMEOUT) public static void wheel(GameTestHelper h){sweep(h,LabyrinthPlace.WHEEL);}
    @AfterBatch(batch="group_wheel") public static void wheelDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_end_world_cabin",timeoutTicks=TIMEOUT) public static void endWorldCabin(GameTestHelper h){sweep(h,LabyrinthPlace.END_WORLD_CABIN);}
    @AfterBatch(batch="group_end_world_cabin") public static void endWorldCabinDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_lake_house",timeoutTicks=TIMEOUT) public static void lakeHouse(GameTestHelper h){sweep(h,LabyrinthPlace.LAKE_HOUSE);}
    @AfterBatch(batch="group_lake_house") public static void lakeHouseDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_painted_interior",timeoutTicks=TIMEOUT) public static void paintedInterior(GameTestHelper h){sweep(h,LabyrinthPlace.PAINTED_INTERIOR);}
    @AfterBatch(batch="group_painted_interior") public static void paintedInteriorDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_second_waitress",timeoutTicks=TIMEOUT) public static void secondWaitress(GameTestHelper h){sweep(h,LabyrinthPlace.SECOND_WAITRESS);}
    @AfterBatch(batch="group_second_waitress") public static void secondWaitressDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_tunnel_periphery",timeoutTicks=TIMEOUT) public static void tunnelPeriphery(GameTestHelper h){sweep(h,LabyrinthPlace.TUNNEL_PERIPHERY);}
    @AfterBatch(batch="group_tunnel_periphery") public static void tunnelPeripheryDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_missing_sleep",timeoutTicks=TIMEOUT) public static void missingSleep(GameTestHelper h){sweep(h,LabyrinthPlace.MISSING_SLEEP);}
    @AfterBatch(batch="group_missing_sleep") public static void missingSleepDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_layered_man",timeoutTicks=TIMEOUT) public static void layeredMan(GameTestHelper h){sweep(h,LabyrinthPlace.LAYERED_MAN);}
    @AfterBatch(batch="group_layered_man") public static void layeredManDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_motion_light",timeoutTicks=TIMEOUT) public static void motionLight(GameTestHelper h){sweep(h,LabyrinthPlace.MOTION_LIGHT);}
    @AfterBatch(batch="group_motion_light") public static void motionLightDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_dare_in_the_dark",timeoutTicks=TIMEOUT) public static void dareInTheDark(GameTestHelper h){sweep(h,LabyrinthPlace.DARE_IN_THE_DARK);}
    @AfterBatch(batch="group_dare_in_the_dark") public static void dareInTheDarkDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_feet_up",timeoutTicks=TIMEOUT) public static void feetUp(GameTestHelper h){sweep(h,LabyrinthPlace.FEET_UP);}
    @AfterBatch(batch="group_feet_up") public static void feetUpDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_whistlers",timeoutTicks=TIMEOUT) public static void whistlers(GameTestHelper h){sweep(h,LabyrinthPlace.WHISTLERS);}
    @AfterBatch(batch="group_whistlers") public static void whistlersDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_work_print",timeoutTicks=TIMEOUT) public static void workPrint(GameTestHelper h){sweep(h,LabyrinthPlace.WORK_PRINT);}
    @AfterBatch(batch="group_work_print") public static void workPrintDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_polaroid_trail",timeoutTicks=TIMEOUT) public static void polaroidTrail(GameTestHelper h){sweep(h,LabyrinthPlace.POLAROID_TRAIL);}
    @AfterBatch(batch="group_polaroid_trail") public static void polaroidTrailDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_left_right",timeoutTicks=TIMEOUT) public static void leftRight(GameTestHelper h){sweep(h,LabyrinthPlace.LEFT_RIGHT);}
    @AfterBatch(batch="group_left_right") public static void leftRightDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_dead_air",timeoutTicks=TIMEOUT) public static void deadAir(GameTestHelper h){sweep(h,LabyrinthPlace.DEAD_AIR);}
    @AfterBatch(batch="group_dead_air") public static void deadAirDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_closing_hall",timeoutTicks=TIMEOUT) public static void closingHall(GameTestHelper h){sweep(h,LabyrinthPlace.CLOSING_HALL);}
    @AfterBatch(batch="group_closing_hall") public static void closingHallDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_storeroom",timeoutTicks=TIMEOUT) public static void storeroom(GameTestHelper h){sweep(h,LabyrinthPlace.STOREROOM);}
    @AfterBatch(batch="group_storeroom") public static void storeroomDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_stillness",timeoutTicks=TIMEOUT) public static void stillness(GameTestHelper h){sweep(h,LabyrinthPlace.STILLNESS);}
    @AfterBatch(batch="group_stillness") public static void stillnessDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_line",timeoutTicks=TIMEOUT) public static void line(GameTestHelper h){sweep(h,LabyrinthPlace.LINE);}
    @AfterBatch(batch="group_line") public static void lineDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_own_light",timeoutTicks=TIMEOUT) public static void ownLight(GameTestHelper h){sweep(h,LabyrinthPlace.OWN_LIGHT);}
    @AfterBatch(batch="group_own_light") public static void ownLightDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_polybius",timeoutTicks=TIMEOUT) public static void polybius(GameTestHelper h){sweep(h,LabyrinthPlace.POLYBIUS);}
    @AfterBatch(batch="group_polybius") public static void polybiusDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_replacement",timeoutTicks=TIMEOUT) public static void replacement(GameTestHelper h){sweep(h,LabyrinthPlace.REPLACEMENT);}
    @AfterBatch(batch="group_replacement") public static void replacementDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_vacant_possession",timeoutTicks=TIMEOUT) public static void vacantPossession(GameTestHelper h){sweep(h,LabyrinthPlace.VACANT_POSSESSION);}
    @AfterBatch(batch="group_vacant_possession") public static void vacantPossessionDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_last_swimmer",timeoutTicks=TIMEOUT) public static void lastSwimmer(GameTestHelper h){sweep(h,LabyrinthPlace.LAST_SWIMMER);}
    @AfterBatch(batch="group_last_swimmer") public static void lastSwimmerDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_deer_road",timeoutTicks=TIMEOUT) public static void deerRoad(GameTestHelper h){sweep(h,LabyrinthPlace.DEER_ROAD);}
    @AfterBatch(batch="group_deer_road") public static void deerRoadDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_state_route",timeoutTicks=TIMEOUT) public static void stateRoute(GameTestHelper h){sweep(h,LabyrinthPlace.STATE_ROUTE);}
    @AfterBatch(batch="group_state_route") public static void stateRouteDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_tall_grass",timeoutTicks=TIMEOUT) public static void tallGrass(GameTestHelper h){sweep(h,LabyrinthPlace.TALL_GRASS);}
    @AfterBatch(batch="group_tall_grass") public static void tallGrassDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_black_city",timeoutTicks=TIMEOUT) public static void blackCity(GameTestHelper h){sweep(h,LabyrinthPlace.BLACK_CITY);}
    @AfterBatch(batch="group_black_city") public static void blackCityDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_place_for_you",timeoutTicks=TIMEOUT) public static void placeForYou(GameTestHelper h){sweep(h,LabyrinthPlace.PLACE_FOR_YOU);}
    @AfterBatch(batch="group_place_for_you") public static void placeForYouDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_second_person",timeoutTicks=TIMEOUT) public static void secondPerson(GameTestHelper h){sweep(h,LabyrinthPlace.SECOND_PERSON);}
    @AfterBatch(batch="group_second_person") public static void secondPersonDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_pell_house",timeoutTicks=TIMEOUT) public static void pellHouse(GameTestHelper h){sweep(h,LabyrinthPlace.PELL_HOUSE);}
    @AfterBatch(batch="group_pell_house") public static void pellHouseDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_pinehook",timeoutTicks=TIMEOUT) public static void pinehook(GameTestHelper h){sweep(h,LabyrinthPlace.PINEHOOK);}
    @AfterBatch(batch="group_pinehook") public static void pinehookDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_last_crossing",timeoutTicks=TIMEOUT) public static void lastCrossing(GameTestHelper h){sweep(h,LabyrinthPlace.LAST_CROSSING);}
    @AfterBatch(batch="group_last_crossing") public static void lastCrossingDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_marsh_supper",timeoutTicks=TIMEOUT) public static void marshSupper(GameTestHelper h){sweep(h,LabyrinthPlace.MARSH_SUPPER);}
    @AfterBatch(batch="group_marsh_supper") public static void marshSupperDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_growing_things",timeoutTicks=TIMEOUT) public static void growingThings(GameTestHelper h){sweep(h,LabyrinthPlace.GROWING_THINGS);}
    @AfterBatch(batch="group_growing_things") public static void growingThingsDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_last_orders",timeoutTicks=TIMEOUT) public static void lastOrders(GameTestHelper h){sweep(h,LabyrinthPlace.LAST_ORDERS);}
    @AfterBatch(batch="group_last_orders") public static void lastOrdersDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_corner_house",timeoutTicks=TIMEOUT) public static void cornerHouse(GameTestHelper h){sweep(h,LabyrinthPlace.CORNER_HOUSE);}
    @AfterBatch(batch="group_corner_house") public static void cornerHouseDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_kept",timeoutTicks=TIMEOUT) public static void kept(GameTestHelper h){sweep(h,LabyrinthPlace.KEPT);}
    @AfterBatch(batch="group_kept") public static void keptDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_the_drawing",timeoutTicks=TIMEOUT) public static void theDrawing(GameTestHelper h){sweep(h,LabyrinthPlace.THE_DRAWING);}
    @AfterBatch(batch="group_the_drawing") public static void theDrawingDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_little_longer",timeoutTicks=TIMEOUT) public static void littleLonger(GameTestHelper h){sweep(h,LabyrinthPlace.LITTLE_LONGER);}
    @AfterBatch(batch="group_little_longer") public static void littleLongerDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_not_yet",timeoutTicks=TIMEOUT) public static void notYet(GameTestHelper h){sweep(h,LabyrinthPlace.NOT_YET);}
    @AfterBatch(batch="group_not_yet") public static void notYetDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_last_seen",timeoutTicks=TIMEOUT) public static void lastSeen(GameTestHelper h){sweep(h,LabyrinthPlace.LAST_SEEN);}
    @AfterBatch(batch="group_last_seen") public static void lastSeenDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_fallow",timeoutTicks=TIMEOUT) public static void fallow(GameTestHelper h){sweep(h,LabyrinthPlace.FALLOW);}
    @AfterBatch(batch="group_fallow") public static void fallowDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_people_lights",timeoutTicks=TIMEOUT) public static void peopleLights(GameTestHelper h){sweep(h,LabyrinthPlace.PEOPLE_LIGHTS);}
    @AfterBatch(batch="group_people_lights") public static void peopleLightsDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_bike_night",timeoutTicks=TIMEOUT) public static void bikeNight(GameTestHelper h){sweep(h,LabyrinthPlace.BIKE_NIGHT);}
    @AfterBatch(batch="group_bike_night") public static void bikeNightDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_three_fingers",timeoutTicks=TIMEOUT) public static void threeFingers(GameTestHelper h){sweep(h,LabyrinthPlace.THREE_FINGERS);}
    @AfterBatch(batch="group_three_fingers") public static void threeFingersDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_ghosts_set",timeoutTicks=TIMEOUT) public static void ghostsSet(GameTestHelper h){sweep(h,LabyrinthPlace.GHOSTS_SET);}
    @AfterBatch(batch="group_ghosts_set") public static void ghostsSetDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_confession",timeoutTicks=TIMEOUT) public static void confession(GameTestHelper h){sweep(h,LabyrinthPlace.CONFESSION);}
    @AfterBatch(batch="group_confession") public static void confessionDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_sleeping_king",timeoutTicks=TIMEOUT) public static void sleepingKing(GameTestHelper h){sweep(h,LabyrinthPlace.SLEEPING_KING);}
    @AfterBatch(batch="group_sleeping_king") public static void sleepingKingDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_harrigan",timeoutTicks=TIMEOUT) public static void harrigan(GameTestHelper h){sweep(h,LabyrinthPlace.HARRIGAN);}
    @AfterBatch(batch="group_harrigan") public static void harriganDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_ones_inside",timeoutTicks=TIMEOUT) public static void onesInside(GameTestHelper h){sweep(h,LabyrinthPlace.ONES_INSIDE);}
    @AfterBatch(batch="group_ones_inside") public static void onesInsideDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="group_the_list",timeoutTicks=TIMEOUT) public static void theList(GameTestHelper h){sweep(h,LabyrinthPlace.THE_LIST);}
    @AfterBatch(batch="group_the_list") public static void theListDone(ServerLevel l){close();}
}
