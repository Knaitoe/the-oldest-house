package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;

/** Real doors, walking bodies, half steps, occupied clocks and a second reader in shelter. */
@GameTestHolder(TheOldestHouse.MOD_ID+"_exploration") @PrefixGameTestTemplate(false)
public final class HallwayVariantsTests {
    private static Fixture active;
    private static final class Fixture implements AutoCloseable {
        final GameTestHelper h;final LabyrinthPlace place;final ServerLevel level;
        final BlockPos origin=new BlockPos(128000,80,128000),base;
        final HouseSavedData oldHouse;final LabyrinthData oldData;final NativeTestChunks chunks=new NativeTestChunks();
        final List<ServerPlayer> readers=new ArrayList<>();
        Fixture(GameTestHelper h,LabyrinthPlace place) {
            this.h=h;this.place=place;var server=h.getLevel().getServer();level=HouseTestLevel.get(server,NovelRooms.dimension(place));
            oldHouse=HouseSavedData.get(server);oldData=LabyrinthData.get(server);
            var house=new HouseSavedData();house.markSpawned(origin);house.markInteriorInitialized();
            var store=server.overworld().getDataStorage();store.set("the_oldest_house",house);store.set("the_oldest_house_labyrinth",new LabyrinthData());
            base=LabyrinthPlaces.base(origin,place);h.assertTrue(base!=null,"the new room has a slot: "+place);
            var r=place.room();chunks.hold(level,new AABB(base.getX()+r.minX()-2,base.getY()+r.minY()-2,base.getZ()+r.minZ()-2,
                    base.getX()+r.maxX()+3,base.getY()+r.maxY()+3,base.getZ()+18));
            HallwayVariants.build(level,base,place);OnwardDoors.install(level,base,place);
            d().setBuilt(LabyrinthBuilder.VERSION,origin);d().setReady(place.id(),true);
            for(var door:place.doors())open(base.offset(door.rel()));
            if(place==LabyrinthPlace.PARALLEL_PASSAGE)for(int z:new int[]{-11,-27})open(base.offset(0,0,z));
            active=this;
        }
        LabyrinthData d(){return LabyrinthData.get(level.getServer());}
        void open(BlockPos at){var s=level.getBlockState(at);if(s.getBlock() instanceof DoorBlock door)door.setOpen(null,level,s,at,true);}
        ServerPlayer player(String name){var p=NativeTestPlayers.survival(h,name);readers.add(p);at(p,.5,0,3.5);p.setNoGravity(true);p.getFoodData().setFoodLevel(6);return p;}
        void at(ServerPlayer p,double x,double y,double z){p.teleportTo(level,base.getX()+x,base.getY()+y,base.getZ()+z,180,0);p.hasChangedDimension();p.setDeltaMovement(Vec3.ZERO);p.setOnGround(true);}
        void clock(long n){var t=HallwayHazards.state(level,base);t.putLong("Clock",n);d().setStateEntry(HallwayHazards.STATE,Long.toString(base.asLong()),t);}
        void reset(){d().setStateEntry(HallwayHazards.STATE,Long.toString(base.asLong()),new CompoundTag());}
        void ready(Runnable r){h.startSequence().thenWaitUntil(()->h.assertTrue(chunks.ready(),"room chunks and entity sections are resident")).thenExecute(r);}
        @Override public void close(){for(var p:readers)NativeTestPlayers.remove(p);chunks.close();GroupCrossings.forget();var store=level.getServer().overworld().getDataStorage();store.set("the_oldest_house",oldHouse);store.set("the_oldest_house_labyrinth",oldData);}
    }
    private static void close(){if(active!=null){active.close();active=null;}}
    private static Vec3 p(double x,double y,double z){return new Vec3(x,y,z);}
    private static List<Vec3> route(Vec3...points){return List.of(points);}
    private static Vec3 rel(Fixture f,ServerPlayer p){return p.position().subtract(f.base.getX(),f.base.getY(),f.base.getZ());}
    private static void step(ServerPlayer reader,Vec3 here,Vec3 target,boolean go) {
        double dx=target.x-here.x,dz=target.z-here.z;
        if(go){reader.setYRot((float)Math.toDegrees(Math.atan2(-dx,dz)));reader.setYHeadRot(reader.getYRot());}
        reader.xxa=0;reader.zza=go?1:0;reader.setJumping(false);reader.setXRot(0);reader.doTick();
    }
    private static final class Walk {
        final Fixture f;final ServerPlayer reader;final List<Vec3> route;final boolean timedSeams;int leg,guarded=-1,ticks;
        Walk(Fixture f,ServerPlayer p,List<Vec3> route){this(f,p,route,true);}
        Walk(Fixture f,ServerPlayer p,List<Vec3> route,boolean timedSeams){this.f=f;reader=p;this.route=route;this.timedSeams=timedSeams;}
        void tick(){
            var here=rel(f,reader);if(done()){step(reader,here,here,false);return;}
            var target=route.get(leg);
            if(Math.hypot(target.x-here.x,target.z-here.z)<.3&&Math.abs(target.y-here.y)<.6){leg++;return;}
            boolean go=true;
            if(timedSeams&&f.place==LabyrinthPlace.OPENING_SEAMS&&leg<6&&leg%2==1&&guarded!=leg) {
                var state=HallwayHazards.state(f.level,f.base);int bay=leg/2;
                go=HallwayHazards.phase(state.getLong("Clock"),bay)<70&&state.getInt("Seam"+bay)!=2;
                if(go)guarded=leg;
            }
            step(reader,here,target,go);ticks++;
            f.h.assertTrue(reader.isAlive(),"the walking route leaves a reader alive: "+f.place);
        }
        boolean done(){return leg>=route.size();}
        String where(){return f.place+" leg "+leg+" at "+rel(f,reader)+" after "+ticks+" ticks";}
    }
    @SafeVarargs private static void walk(GameTestHelper h,LabyrinthPlace place,List<Vec3>...routes) {
        var f=new Fixture(h,place);var reader=f.player("hall_walker");reader.setNoGravity(false);
        var seq=h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"walking chunks and entity sections are resident"));
        for(var route:routes) {
            var body=new Walk(f,reader,route);
            seq.thenExecute(()->{f.at(reader,.5,0,3.5);reader.setHealth(20);})
                .thenWaitUntil(()->{body.tick();h.assertTrue(body.done(),"a body walks through the real doors: "+body.where());});
        }
        seq.thenSucceed();
    }
    @GameTest(template="empty",batch="hall_parallel",timeoutTicks=3000) public static void parallelPassageHasTwoWalkableWaysOn(GameTestHelper h){walk(h,LabyrinthPlace.PARALLEL_PASSAGE,
        route(p(.5,0,-1.5),p(-1.5,0,-3.5),p(-1.5,0,-17.5),p(-4.5,0,-17.5)),
        route(p(.5,0,-1.5),p(-1.5,0,-3.5),p(-1.5,0,-26.5),p(2.5,0,-26.5),p(2.5,0,-7.5),p(5.5,0,-7.5)));}
    @AfterBatch(batch="hall_parallel") public static void parallelDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="hall_back_stair",timeoutTicks=3000) public static void backStairClimbsAndPassesBeneathTheNextFlight(GameTestHelper h){walk(h,LabyrinthPlace.BACK_STAIR,
        route(p(.5,0,-3.5),p(4.5,0,-3.5),p(4.5,4,-13.5),p(-6.5,4,-13.5)),
        route(p(.5,0,-3.5),p(4.5,0,-3.5),p(4.5,4,-13.5),p(.5,4,-13.5),p(.5,4,-21.5),p(7.5,4,-21.5)));}
    @AfterBatch(batch="hall_back_stair") public static void backDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="hall_plaster_walk",timeoutTicks=1500) public static void plasterCanStillBeWalkedAfterItFalls(GameTestHelper h){walk(h,LabyrinthPlace.FALLING_PLASTER,route(p(.5,0,-33.5)));}
    @AfterBatch(batch="hall_plaster_walk") public static void plasterWalkDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="hall_crossed",timeoutTicks=3000) public static void crossedGalleriesPassAboveAndBelow(GameTestHelper h){walk(h,LabyrinthPlace.CROSSED_GALLERIES,
        route(p(.5,0,-29.5)),route(p(.5,0,-3.5),p(-6.5,0,-3.5),p(-6.5,-4,-14.5),p(10.5,-4,-14.5)));}
    @AfterBatch(batch="hall_crossed") public static void crossedDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="hall_vaults",timeoutTicks=3000) public static void threeVaultsAlternateTheirOpenings(GameTestHelper h){walk(h,LabyrinthPlace.THREE_VAULTS,
        route(p(.5,0,-3.5),p(-3.5,0,-10.5),p(-3.5,0,-16.5),p(-3.5,0,-20.5),p(-7.5,0,-20.5)),
        route(p(.5,0,-3.5),p(-3.5,0,-10.5),p(-3.5,0,-16.5),p(4.5,0,-24.5),p(4.5,0,-30.5),p(.5,0,-40.5),p(.5,0,-43.5)));}
    @AfterBatch(batch="hall_vaults") public static void vaultsDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="hall_steam_walk",timeoutTicks=1500) public static void steamWalkHasAContinuousFloor(GameTestHelper h){walk(h,LabyrinthPlace.STEAM_WALK,route(p(.5,0,-35.5)));}
    @AfterBatch(batch="hall_steam_walk") public static void steamWalkDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="hall_garages",timeoutTicks=3000) public static void garageLaneReachesBothGates(GameTestHelper h){walk(h,LabyrinthPlace.BEHIND_GARAGES,
        route(p(.5,0,-27.5),p(-13.5,0,-27.5)),route(p(.5,0,-39.5)));}
    @AfterBatch(batch="hall_garages") public static void garagesDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="hall_orchard",timeoutTicks=4400) public static void orchardRowsMeetAndTheSideWayOpens(GameTestHelper h){walk(h,LabyrinthPlace.ORCHARD_ROWS,
        route(p(.5,0,-4.5),p(-2.5,0,-5.5),p(-2.5,0,-35.5),p(.5,0,-38.5),p(.5,0,-43.5)),
        route(p(.5,0,-4.5),p(3.5,0,-5.5),p(3.5,0,-35.5),p(.5,0,-38.5),p(.5,0,-43.5)),
        route(p(.5,0,-4.5),p(3.5,0,-5.5),p(3.5,0,-21.5),p(17.5,0,-21.5)));}
    @AfterBatch(batch="hall_orchard") public static void orchardDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="hall_line_walk",timeoutTicks=3000) public static void fallenWireHasAShortCrossingAndARaisedBank(GameTestHelper h){walk(h,LabyrinthPlace.DOWNED_LINE,
        route(p(.5,0,-35.5)),route(p(.5,0,-6.5),p(-5.5,0,-7.5),p(-5.5,1,-11.5),p(-5.5,1,-27.5),p(-5.5,0,-30.5),p(.5,0,-30.5),p(.5,0,-35.5)));}
    @AfterBatch(batch="hall_line_walk") public static void lineWalkDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="hall_dividing",timeoutTicks=3000) public static void dividingWallCanBePassedOnEitherSide(GameTestHelper h){walk(h,LabyrinthPlace.DIVIDING_WALL,
        route(p(.5,0,-3.5),p(-8.5,0,-3.5),p(-8.5,0,-35.5),p(.5,0,-36.5),p(.5,0,-41.5)),
        route(p(.5,0,-3.5),p(9.5,0,-3.5),p(9.5,0,-35.5),p(.5,0,-36.5),p(.5,0,-41.5)));}
    @AfterBatch(batch="hall_dividing") public static void dividingDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="hall_lintels",timeoutTicks=1500) public static void lowLintelsFitAStandingBody(GameTestHelper h){walk(h,LabyrinthPlace.LOW_LINTELS,
        route(p(.5,0,-3.5),p(5.5,0,-9.5),p(5.5,0,-14.5),p(-4.5,0,-21.5),p(-4.5,0,-26.5),p(5.5,0,-33.5),p(5.5,0,-38.5),p(.5,0,-44.5),p(.5,0,-47.5)));}
    @AfterBatch(batch="hall_lintels") public static void lintelsDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="hall_seams_walk",timeoutTicks=1500) public static void openingSeamsCanBeCrossedByWaitingOnTheBanks(GameTestHelper h){walk(h,LabyrinthPlace.OPENING_SEAMS,
        route(p(.5,0,-5.5),p(.5,0,-12.5),p(.5,0,-16.5),p(.5,0,-23.5),p(.5,0,-27.5),p(.5,0,-34.5),p(.5,0,-37.5)));}
    @AfterBatch(batch="hall_seams_walk") public static void seamsWalkDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="hall_plaster_state",timeoutTicks=260) public static void plasterWarnsFallsOnceAndLeavesShelterUnhurt(GameTestHelper h){
        var f=new Fixture(h,LabyrinthPlace.FALLING_PLASTER);var hit=f.player("plaster_hit");var safe=f.player("plaster_shelter");
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"plaster chunks ready"))
            .thenExecute(()->{f.reset();f.at(hit,.5,0,-7.5);hit.setHealth(1.5F);f.at(safe,2.5,0,-7.5);})
            .thenExecuteFor(18,()->{})
            .thenExecute(()->{h.assertTrue(!HallwayHazards.state(f.level,f.base).getBoolean("Plaster0Fallen")&&hit.getHealth()==1.5F,"dust and cracks precede the fall without hurting anyone");
                h.assertTrue(f.level.getBlockState(f.base.offset(0,3,-8)).getValue(HallwayFittingBlock.STAGE)==1,"the ceiling visibly cracks during the warning");})
            .thenExecuteFor(28,()->{})
            .thenExecute(()->h.assertTrue(HallwayHazards.state(f.level,f.base).getBoolean("Plaster0Fallen")&&hit.getHealth()>=1&&hit.getHealth()<1.5F&&safe.getHealth()==20,"the fallen span gives a nonfatal hit and the recess protects the other reader"))
            .thenExecuteFor(90,()->{})
            .thenExecute(()->h.assertTrue(hit.getHealth()>=1&&f.level.getBlockState(f.base.offset(0,0,-8)).getCollisionShape(f.level,f.base.offset(0,0,-8)).isEmpty(),"it falls once and the remaining rubble does not obstruct a body"))
            .thenSucceed();
    }
    @AfterBatch(batch="hall_plaster_state") public static void plasterStateDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="hall_steam_state",timeoutTicks=200) public static void steamWarnsAndSharesItsCycleWithProtectedReaders(GameTestHelper h){
        var f=new Fixture(h,LabyrinthPlace.STEAM_WALK);var hit=f.player("steam_hit");var safe=f.player("steam_shelter");
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"steam chunks ready"))
            .thenExecute(()->{f.reset();f.clock(119);f.at(hit,.5,0,-8.5);f.at(safe,2.5,0,-8.5);})
            .thenExecuteFor(25,()->{})
            .thenExecute(()->h.assertTrue(hit.getHealth()==20&&safe.getHealth()==20,"the rattle and thin plume give time to step into shelter"))
            .thenExecuteFor(20,()->{})
            .thenExecute(()->h.assertTrue(hit.getHealth()<20&&hit.getHealth()>=18&&safe.getHealth()==20,"one shared jet hurts the exposed reader and leaves the recess safe"))
            .thenExecuteFor(12,()->{})
            .thenExecute(()->h.assertTrue(hit.getHealth()>=18,"the jet has a damage cooldown"))
            .thenExecute(()->f.clock(200)).thenExecuteFor(10,()->{})
            .thenExecute(()->h.assertTrue(hit.getHealth()>=18&&safe.getHealth()==20,"the clear phase is safe for both readers"))
            .thenSucceed();
    }
    @AfterBatch(batch="hall_steam_state") public static void steamStateDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="hall_wire_state",timeoutTicks=200) public static void wireDimsBeforeArcingAndCannotReachTheBank(GameTestHelper h){
        var f=new Fixture(h,LabyrinthPlace.DOWNED_LINE);var hit=f.player("wire_hit");var safe=f.player("wire_bank");
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"wire chunks ready"))
            .thenExecute(()->{f.reset();f.clock(119);f.at(hit,.5,0,-18.5);f.at(safe,-5.5,1,-18.5);})
            .thenExecuteFor(25,()->{})
            .thenExecute(()->h.assertTrue(hit.getHealth()==20&&PowerBlock.stage(f.level.getBlockState(f.base.offset(HallwayVariants.LINE_LAMP)))==0,"the work light dims before the wire becomes dangerous"))
            .thenExecuteFor(20,()->{})
            .thenExecute(()->h.assertTrue(hit.getHealth()<20&&hit.getHealth()>=18&&safe.getHealth()==20,"the wet crossing hurts, while the raised bank stays safe"))
            .thenExecute(()->f.clock(200)).thenExecuteFor(3,()->{})
            .thenExecute(()->h.assertTrue(PowerBlock.stage(f.level.getBlockState(f.base.offset(HallwayVariants.LINE_LAMP)))==1,"the light returns with the safe crossing"))
            .thenSucceed();
    }
    @AfterBatch(batch="hall_wire_state") public static void wireStateDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="hall_seam_state",timeoutTicks=160) public static void openingSeamsWaitForBodiesBeforeClosing(GameTestHelper h){
        var f=new Fixture(h,LabyrinthPlace.OPENING_SEAMS);var bank=f.player("seam_bank");var body=f.player("seam_body");
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"seam chunks ready"))
            .thenExecute(()->{f.reset();f.clock(119);f.at(bank,.5,0,-5.5);f.at(body,-1.5,0,-9.5);})
            .thenExecuteFor(20,()->{})
            .thenExecute(()->h.assertTrue(f.level.getBlockState(f.base.offset(0,-1,-10)).getValue(HallwayFittingBlock.STAGE)==1,"a wider joint and grinding precede the opening"))
            .thenExecuteFor(22,()->{})
            .thenExecute(()->{h.assertTrue(f.level.getBlockState(f.base.offset(0,-1,-10)).isAir(),"the warned floor opens for every reader");f.at(body,-1.5,-.5,-9.5);f.clock(199);})
            .thenExecuteFor(5,()->{})
            .thenExecute(()->{h.assertTrue(f.level.getBlockState(f.base.offset(0,-1,-10)).isAir(),"the floor cannot close through a second reader");f.at(body,.5,0,3.5);})
            .thenExecuteFor(3,()->{})
            .thenExecute(()->h.assertTrue(!f.level.getBlockState(f.base.offset(0,-1,-10)).isAir(),"when clear, the same shared floor closes"))
            .thenSucceed();
    }
    @AfterBatch(batch="hall_seam_state") public static void seamStateDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="hall_seam_recovery",timeoutTicks=1800) public static void everySeamHasStepsBackToItsPrecedingBank(GameTestHelper h){
        var f=new Fixture(h,LabyrinthPlace.OPENING_SEAMS);var reader=f.player("seam_climber");reader.setNoGravity(false);
        var seq=h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"recovery chunks ready"));
        for(int i=0;i<3;i++) {
            int bay=i,z=HallwayVariants.SEAMS[i];var walk=new Walk(f,reader,route(p(3.5,-4,z+.5),p(3.5,-4,z+1.5),p(3.5,0,z+9.5),p(.5,0,z+9.5)),false);
            seq.thenExecute(()->{f.at(reader,.5,0,z+.5);reader.setHealth(20);f.clock(HallwayHazards.ACTIVE+60L*bay);})
                .thenExecuteFor(28,()->step(reader,rel(f,reader),rel(f,reader),false))
                .thenExecute(()->h.assertTrue(rel(f,reader).y<-3.7,"a real fall lands on the catch ledge at seam "+bay))
                .thenWaitUntil(()->{walk.tick();h.assertTrue(walk.done()&&rel(f,reader).y>-.2,"a body climbs back onto the preceding bank: "+walk.where());});
        }
        seq.thenSucceed();
    }
    @AfterBatch(batch="hall_seam_recovery") public static void recoveryDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="hall_saved_cycle",timeoutTicks=200) public static void emptyRoomsPauseAndSavedCyclesResume(GameTestHelper h){
        var f=new Fixture(h,LabyrinthPlace.STEAM_WALK);var reader=f.player("saved_cycle");long[] stopped={0};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"saved-cycle chunks ready"))
            .thenExecute(()->{f.reset();f.clock(100);f.at(reader,.5,0,-4.5);})
            .thenExecuteFor(5,()->{})
            .thenExecute(()->{stopped[0]=HallwayHazards.state(f.level,f.base).getLong("Clock");f.at(reader,50,0,50);})
            .thenExecuteFor(10,()->{})
            .thenExecute(()->{
                h.assertTrue(HallwayHazards.state(f.level,f.base).getLong("Clock")==stopped[0],"an empty room does not consume the safe part of its cycle");
                var registries=f.level.registryAccess();var saved=f.d().save(new CompoundTag(),registries);
                f.level.getServer().overworld().getDataStorage().set("the_oldest_house_labyrinth",LabyrinthData.load(saved,registries));
                h.assertTrue(HallwayHazards.state(f.level,f.base).getLong("Clock")==stopped[0],"the occupied clock survives a SavedData round trip");
                f.at(reader,.5,0,-4.5);
            })
            .thenExecuteFor(5,()->{})
            .thenExecute(()->h.assertTrue(HallwayHazards.state(f.level,f.base).getLong("Clock")>stopped[0],"returning readers resume the same shared cycle"))
            .thenSucceed();
    }
    @AfterBatch(batch="hall_saved_cycle") public static void savedDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="hall_rules") public static void variantsKeepDepthBandsYardRoutesAndSlotBoundaries(GameTestHelper h){
        var data=new LabyrinthData();var id=UUID.randomUUID();
        for(var place:HallwayVariants.PLACES) {
            h.assertTrue(place.authored()&&!place.isVignette(),"a hallway is built whole and supplies no story credit: "+place);
            h.assertTrue(LabyrinthPacing.physicalTrial(place)==HallwayVariants.hazard(place),"only the hazards take the existing hazard pacing: "+place);
            h.assertTrue(NovelRooms.outside(place)==HallwayVariants.yard(place),"yard rooms use the outside dimension: "+place);
            int first=HallwayVariants.firstDepth(place);
            h.assertTrue(LabyrinthBuilder.requiredDepth(place)==first&&HallwayVariants.weight(place,first)>0,"the builder prepares the room before it can be dealt: "+place);
            if(first>0)h.assertTrue(HallwayVariants.weight(place,first-1)==0,"the room cannot be dealt early: "+place);
            if(HallwayVariants.hazard(place))h.assertTrue(!TrialMarks.name(place).equals("a room the House has not named"),"the trial's earned mark has an account line");
            for(int y:new int[]{64,80,100,160,220}) {
                var origin=new BlockPos(0,y,0);var b=LabyrinthPlaces.base(origin,place);var r=place.room();var slot=LabyrinthPlaces.slotBounds(origin,place);
                h.assertTrue(b!=null&&slot!=null,"the appended slot fits at manor height "+y);
                if(!HallwayVariants.yard(place))h.assertTrue(b.getY()+r.minY()>=slot.minY()&&b.getY()+r.maxY()<=slot.maxY(),"the entire room remains in its own slot at height "+y+": "+place);
            }
        }
        for(var place:List.of(LabyrinthPlace.PARALLEL_PASSAGE,LabyrinthPlace.BACK_STAIR,LabyrinthPlace.FALLING_PLASTER))h.assertTrue(HallwayVariants.weight(place,9)==0,"domestic additions stop when the halls turn to stone");
        for(int clock=0;clock<HallwayHazards.WARNING;clock++)for(int bay=0;bay<3;bay++)
            h.assertTrue(!HallwayHazards.active(clock,bay),"every bay starts with a safe approach and a full warning");
        var pool=new ArrayList<>(HallwayVariants.PLACES);YardStretch.fit(pool,data,id,LabyrinthPlace.JUNCTION);
        h.assertTrue(pool.stream().noneMatch(HallwayVariants::yard),"yard additions are never dealt behind an inside door");
        h.succeed();
    }
}
