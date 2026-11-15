package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import io.github.knaitoe.theoldesthouse.opening.CompanionOrders;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID + "_staircase")
@PrefixGameTestTemplate(false)
public final class StaircaseAccessTests {
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE|Block.UPDATE_SUPPRESS_DROPS;
    private static final List<Fixture> ACTIVE=new ArrayList<>();
    static final class Fixture implements AutoCloseable {
        final ServerLevel level,outside;
        final BlockPos origin,base,source;
        final NativeTestChunks chunks=new NativeTestChunks();
        final List<ServerPlayer> players=new ArrayList<>();
        final Map<ServerLevel,Set<BlockPos>> touched=new HashMap<>();
        HouseSavedData oldHouse; LabyrinthData oldData; boolean started,finished;
        Fixture(GameTestHelper h,int coordinate){
            level=HouseTestLevel.get(h.getLevel().getServer());outside=HouseTestLevel.get(level.getServer(),HouseDimensions.OUTSIDE);
            origin=new BlockPos(coordinate,0,coordinate);base=FinaleArchitecture.base(origin);source=FinaleArchitecture.entry(origin).east(100);
            chunks.hold(level,new AABB(base.getX()-17,FinaleArchitecture.TOP-2,base.getZ()+18,base.getX()+8,FinaleArchitecture.TOP+8,base.getZ()+44));
            chunks.hold(level,new AABB(source).inflate(18));
            for(var at:StaircaseFire.braziers(origin))chunks.hold(level,new AABB(at).inflate(2));
            for(var p:FinaleArchitecture.entrancePlan(origin))put(level,p.pos(),p.block());
            for(var at:StaircaseFire.braziers(origin)){put(level,at.below(),Blocks.STONE.defaultBlockState());put(level,at,Blocks.AIR.defaultBlockState());}
            for(int x=-7;x<=7;x++)for(int z=0;z<=16;z++)put(level,source.offset(x,-1,z),Blocks.DARK_OAK_PLANKS.defaultBlockState());
            var door=Blocks.SPRUCE_DOOR.defaultBlockState().setValue(DoorBlock.FACING,Direction.SOUTH).setValue(DoorBlock.OPEN,true);
            put(level,source,door);put(level,source.above(),door.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));
        }
        void start(){
            var server=level.getServer();oldHouse=HouseSavedData.get(server);oldData=LabyrinthData.get(server);started=true;
            var house=new HouseSavedData();house.markSpawned(origin);house.markInteriorInitialized();
            server.overworld().getDataStorage().set("the_oldest_house",house);
            var data=new LabyrinthData();server.overworld().getDataStorage().set("the_oldest_house_labyrinth",data);
            var architecture=new CompoundTag();architecture.putBoolean("Ready",true);architecture.putInt("CarveVersion",FinaleArchitecture.CARVE_VERSION);
            data.setState("finale_architecture_049",architecture);
            data.putDoor(new LabyrinthData.Door(FinaleArchitecture.ENTRY,HouseDimensions.INTERIOR,FinaleArchitecture.entry(origin),Direction.SOUTH,LabyrinthData.RETURN,false));
        }
        LabyrinthData data(){return LabyrinthData.get(level.getServer());}
        void put(ServerLevel target,BlockPos at,BlockState state){touched.computeIfAbsent(target,k->new HashSet<>()).add(at.immutable());target.setBlock(at,state,F);}
        ServerPlayer player(GameTestHelper h,String name){var p=NativeTestPlayers.survival(h,name);p.setNoGravity(true);players.add(p);return p;}
        void enter(ServerPlayer p){
            p.teleportTo(level,source.getX()+.5,source.getY(),source.getZ()-1.5,0,0);
            FinaleController.enter(p,new LabyrinthData.Door("fixture.source",HouseDimensions.INTERIOR,source,Direction.SOUTH,LabyrinthData.RETURN,false));
        }
        LecternBlockEntity lectern(){return (LecternBlockEntity)level.getBlockEntity(StaircaseFire.shelf(origin));}
        void reload(){var loaded=LabyrinthData.FACTORY.deserializer().apply(data().save(new CompoundTag(),level.registryAccess()),level.registryAccess());level.getServer().overworld().getDataStorage().set("the_oldest_house_labyrinth",loaded);}
        @Override public void close(){
            for(var p:players){HouseTransitionEvents.cancelPending(p,"fixture closed");LabyrinthDoors.onPlayerLoggedOut(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(p));NativeTestPlayers.remove(p);}players.clear();
            var remove=new ArrayList<Entity>();for(var e:level.getAllEntities())if(FinaleArchitecture.contains(origin,e.blockPosition())&&(e instanceof NovelActor||e instanceof FinaleWitness))remove.add(e);
            for(var e:remove)e.discard();
            for(var entry:touched.entrySet())for(var at:entry.getValue())entry.getKey().setBlock(at,Blocks.AIR.defaultBlockState(),F);
            chunks.close();if(started){var storage=level.getServer().overworld().getDataStorage();storage.set("the_oldest_house",oldHouse);storage.set("the_oldest_house_labyrinth",oldData);started=false;}
        }
    }
    static void run(GameTestHelper h,int coordinate,Consumer<Fixture> setup,Consumer<Fixture> test){
        var f=new Fixture(h,coordinate);ACTIVE.add(f);setup.accept(f);
        h.onEachTick(()->{if(f.finished||!f.chunks.ready())return;f.finished=true;
            try{f.start();test.accept(f);}finally{f.close();ACTIVE.remove(f);}h.succeed();});
    }
    static void run(GameTestHelper h,int coordinate,Consumer<Fixture> test){run(h,coordinate,f->{},test);}
    @AfterBatch(batch="staircase_access") public static void cleanup(ServerLevel level){for(var f:ACTIVE)f.close();ACTIVE.clear();}
    @AfterBatch(batch="staircase_ascent") public static void ascentCleanup(ServerLevel level){for(var f:ACTIVE)f.close();ACTIVE.clear();}
    /** The corner landings at one corner of the spiral, from the bottom of the descent up, as feet positions. */
    static List<BlockPos> cornerLandings(BlockPos origin){
        var b=FinaleArchitecture.base(origin);var out=new ArrayList<BlockPos>();int last=Integer.MIN_VALUE;
        for(var at:FinaleArchitecture.staircaseRoute(origin))if(at.getX()-b.getX()==FinaleArchitecture.STAIR_RADIUS&&at.getZ()-b.getZ()==FinaleArchitecture.STAIR_RADIUS&&at.getY()!=last){out.add(at);last=at.getY();}
        Collections.reverse(out);return out;
    }
    /** 0.4.199, the owner: the climb up the great staircase is shorter than the way down. */
    @GameTest(template="empty",batch="staircase_ascent",timeoutTicks=1200)
    public static void theClimbBackUpIsShorterThanTheWayDown(GameTestHelper h){
        var landings=cornerLandings(new BlockPos(517000,0,517000));
        run(h,517000,f->{
            var b=f.base;var col=landings.get(0);
            f.chunks.hold(f.level,new AABB(col.getX()-3,FinaleArchitecture.ARENA,col.getZ()-3,col.getX()+4,FinaleArchitecture.TOP+4,col.getZ()+4));
            for(var at:landings)for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++){f.put(f.level,at.offset(dx,-1,dz),Blocks.STONE.defaultBlockState());for(int y=0;y<3;y++)f.put(f.level,at.offset(dx,y,dz),Blocks.AIR.defaultBlockState());}
        },f->{
            var server=f.level.getServer();var p=f.player(h,"climber");var peer=f.player(h,"beside");var watcher=f.player(h,"watcher");
            for(var q:List.of(p,peer))FinaleProgress.phase(server,q.getUUID(),FinaleProgress.Phase.HOMEWARD);
            h.assertTrue(landings.get(1).getY()-landings.get(0).getY()==StaircaseAscent.TURN,"the spiral's corner landings are one turn of seventy blocks apart");
            var at=landings.get(2);
            java.util.function.BiConsumer<ServerPlayer,BlockPos> stand=(q,to)->{q.teleportTo(f.level,to.getX()+.5,to.getY(),to.getZ()+.5,0,0);q.setOnGround(true);};
            // Going down never carries anyone.
            stand.accept(p,landings.get(4));StaircaseAscent.step(p,f.origin);stand.accept(p,landings.get(3));
            h.assertTrue(StaircaseAscent.step(p,f.origin)==0&&p.blockPosition().equals(landings.get(3)),"a turn walked down is walked");
            // A whole turn climbed, from the corner to the same corner a turn up, carries the reader up two more.
            stand.accept(p,at);StaircaseAscent.step(p,f.origin);stand.accept(p,landings.get(3));
            h.assertTrue(StaircaseAscent.step(p,f.origin)==2*StaircaseAscent.TURN&&p.blockPosition().equals(landings.get(5)),"a turn climbed rises three, to the same landing: at "+p.blockPosition()+", expected "+landings.get(5));
            h.assertTrue(FinaleProgress.player(server,p.getUUID()).getInt("AscentLifts")==1,"the climb remembers it was carried");
            // Climbing on from there counts from the landing they were carried to.
            stand.accept(p,landings.get(6));h.assertTrue(StaircaseAscent.step(p,f.origin)==2*StaircaseAscent.TURN&&p.blockPosition().equals(landings.get(8)),"the next turn climbed carries again");
            // Two readers climbing side by side are carried together.
            p.getPersistentData().remove("StairAscent199");
            stand.accept(p,landings.get(2));stand.accept(peer,landings.get(2).east());StaircaseAscent.step(p,f.origin);StaircaseAscent.step(peer,f.origin);
            stand.accept(p,landings.get(3));stand.accept(peer,landings.get(3).east());
            h.assertTrue(StaircaseAscent.step(p,f.origin)==2*StaircaseAscent.TURN&&p.blockPosition().equals(landings.get(5))&&peer.blockPosition().equals(landings.get(5).east()),"a party climbing together is carried together");
            // Anyone else who could see, a spectator included, keeps the reader walking.
            p.getPersistentData().remove("StairAscent199");watcher.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);
            stand.accept(peer,landings.get(0));stand.accept(p,landings.get(2));StaircaseAscent.step(p,f.origin);
            stand.accept(watcher,landings.get(3).north(3));stand.accept(p,landings.get(3));
            h.assertTrue(StaircaseAscent.step(p,f.origin)==0&&p.blockPosition().equals(landings.get(3)),"a watched climb is walked");
            stand.accept(watcher,landings.get(0).below(40));
            // The top turns, where the leaves and the hearths are, are always walked.
            p.getPersistentData().remove("StairAscent199");int top=landings.size()-1;
            stand.accept(p,landings.get(top-2));StaircaseAscent.step(p,f.origin);stand.accept(p,landings.get(top-1));
            h.assertTrue(StaircaseAscent.step(p,f.origin)==0&&p.blockPosition().equals(landings.get(top-1)),"the last turns below the top are walked");
            // Will, who measures, writes it down.
            var book=WitnessAccount.book(f.data(),p.getUUID(),"climber",true);
            var text=book.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().map(page->page.raw().getString()).reduce("",String::concat);
            h.assertTrue(text.contains("There were fewer coming up."),"the completed account says the way up was shorter");
            var plain=WitnessAccount.book(f.data(),watcher.getUUID(),"watcher",true).get(DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().map(page->page.raw().getString()).reduce("",String::concat);
            h.assertTrue(!plain.contains("fewer coming up"),"a reader never carried is not told so");
        });
    }

    @GameTest(template="empty",batch="staircase_access",timeoutTicks=1200)
    public static void aDoorLandsOnTheFirstSupportedTread(GameTestHelper h){run(h,513250,f->{
        var reader=f.player(h,"supported_arrival");
        f.enter(reader);
        var tread=FinaleArchitecture.staircaseRoute(f.origin).getFirst();
        h.assertTrue(reader.position().distanceToSqr(Vec3.atBottomCenterOf(tread))<.01,
                "the real crossing puts the reader on the first tread");
        h.assertTrue(f.level.getBlockState(tread.below()).isFaceSturdy(f.level,tread.below(),Direction.UP),
                "the native arrival has a full support block");
    });}

    // The complete suite loads several native dimensions before this batch. Allow its
    // asynchronous entity-section preparation to finish; every gameplay assertion remains required.
    @GameTest(template="empty",batch="staircase_access",timeoutTicks=1200)
    public static void upgradeMovesOriginalShelfFireAndTomBeyondTheRealArrivalCopy(GameTestHelper h){run(h,512000,f->{
        var p=f.player(h,"entrance_original");var old=f.base.offset(2,FinaleArchitecture.TOP,32);
        f.put(f.level,old,Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.HAS_BOOK,true));
        var original=StaircaseFire.book(p.getUUID(),3);original.set(DataComponents.CUSTOM_NAME,Component.literal("Kept original"));
        ((LecternBlockEntity)f.level.getBlockEntity(old)).setBook(original.copy());
        var fire=f.base.offset(-7,FinaleArchitecture.TOP,32);f.put(f.level,fire,Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT,true));
        var actor=NovelRegistry.ACTOR.get().create(f.level);h.assertTrue(actor!=null,"native Tom exists");actor.appearance(p.getUUID(),0);actor.moveTo(Vec3.atBottomCenterOf(f.base.offset(-6,FinaleArchitecture.LOOP_BOTTOM-20,30)));f.level.addFreshEntity(actor);var uuid=actor.getUUID();
        var personal=new CompoundTag();personal.putUUID("Tom",uuid);NovelVignettes.save(f.data(),p.getUUID(),personal);
        FinaleRepairs.repairEntrance(f.level,f.origin);
        h.assertTrue(f.level.getBlockState(old).isAir()&&ItemStack.isSameItemSameComponents(original,f.lectern().getBook()),"migration moves the actual written original without changing its components");
        h.assertTrue(actor.getUUID().equals(uuid)&&actor.blockPosition().equals(FinaleRepairs.tom(f.origin))&&NovelVignettes.personal(f.data(),p.getUUID()).getUUID("Tom").equals(uuid),"a saved Tom below the damaged platform is recovered with the same owner and actual actor UUID");
        h.assertTrue(f.level.getBlockState(f.base.offset(-13,FinaleArchitecture.TOP,24)).getValue(CampfireBlock.LIT),"the camp's actual lit state survives");
        var lectern=f.lectern();f.enter(p);f.enter(p);
        h.assertTrue(f.lectern()==lectern&&ItemStack.isSameItemSameComponents(original,lectern.getBook()),"two production arrival copies retain the same lectern and original");
    });}

    @GameTest(template="empty",batch="staircase_access",timeoutTicks=1200)
    public static void twoRealArrivalsKeepTheCampWalkableAndGiveEachExplorerTheirOwnTom(GameTestHelper h){run(h,512500,f->{
        StaircaseFire.dress(f.level,f.origin);var shelf=f.lectern();var original=shelf.getBook().copy();
        var p=f.player(h,"tom_first");var peer=f.player(h,"tom_second");f.enter(p);p.moveTo(Vec3.atBottomCenterOf(FinaleRepairs.tom(f.origin).south(2)));f.enter(peer);
        var actors=f.level.getEntitiesOfClass(NovelActor.class,new AABB(FinaleRepairs.tom(f.origin)).inflate(6),a->a.role()==0);
        h.assertTrue(actors.size()==2&&actors.stream().anyMatch(a->a.owner().filter(p.getUUID()::equals).isPresent())&&actors.stream().anyMatch(a->a.owner().filter(peer.getUUID()::equals).isPresent()),"both real native players receive separate owner-filtered Toms on confirmed arrival");
        NovelVignettes.staircaseArrival(p);NovelVignettes.staircaseArrival(peer);
        h.assertTrue(f.level.getEntitiesOfClass(NovelActor.class,new AABB(FinaleRepairs.tom(f.origin)).inflate(6),a->a.role()==0).size()==2,"repeated arrival checks cannot duplicate either actor");
        for(int x=0;x>=-13;x--){var at=f.base.offset(x,FinaleArchitecture.TOP,24);var body=new AABB(at.getX()+.2,at.getY(),at.getZ()+.2,at.getX()+.8,at.getY()+1.8,at.getZ()+.8);
            h.assertTrue(f.level.getBlockState(at.below()).isFaceSturdy(f.level,at.below(),Direction.UP)&&!f.level.getBlockCollisions(p,body).iterator().hasNext(),"a native survival body has supported clearance from the actual first tread to the camp at "+x);}
        h.assertTrue(f.lectern()==shelf&&ItemStack.isSameItemSameComponents(original,shelf.getBook()),"the second player cannot erase the shelf");
        h.assertTrue(StaircaseFire.take(p)&&StaircaseFire.take(peer)&&!StaircaseFire.take(p)&&!StaircaseFire.take(peer),"the five-leaf supply remains finite for each explorer");
        f.reload();h.assertTrue(NovelVignettes.personal(f.data(),p.getUUID()).hasUUID("Tom")&&NovelVignettes.personal(f.data(),peer.getUUID()).hasUUID("Tom"),"both native actor identities survive saved-data reload");
    });}

    @GameTest(template="empty",batch="staircase_access",timeoutTicks=1200)
    public static void deepVisitorsDoNotStarveHearthPlacementAndRetriesDoNotRefillOriginals(GameTestHelper h){run(h,513000,f->{
        var deep=f.player(h,"deep_stair_peer");deep.teleportTo(f.level,f.base.getX()+.5,FinaleArchitecture.TOP-200,f.base.getZ()+.5,0,0);
        var local=f.player(h,"occupied_hearth");var first=StaircaseFire.braziers(f.origin).getFirst();local.teleportTo(f.level,first.getX()+.5,first.getY(),first.getZ()+.5,0,0);
        StaircaseFire.dress(f.level,f.origin);
        h.assertTrue(f.level.getBlockState(first).isAir()&&StaircaseFire.braziers(f.origin).stream().skip(1).allMatch(at->f.level.getBlockState(at).is(Blocks.CAMPFIRE))&&f.lectern().hasBook(),"only the locally occupied hearth waits; an explorer deep below cannot block entrance supplies");
        f.lectern().setBook(ItemStack.EMPTY);f.level.setBlock(StaircaseFire.shelf(f.origin),f.level.getBlockState(StaircaseFire.shelf(f.origin)).setValue(LecternBlock.HAS_BOOK,false),F);
        local.moveTo(Vec3.atBottomCenterOf(f.base.offset(0,FinaleArchitecture.TOP-200,0)));StaircaseFire.dress(f.level,f.origin);
        h.assertTrue(StaircaseFire.braziers(f.origin).stream().allMatch(at->f.level.getBlockState(at).is(Blocks.CAMPFIRE))&&!f.lectern().hasBook(),"the retry completes the five native hearths without replenishing the removed original");
    });}

    @GameTest(template="empty",batch="staircase_access",timeoutTicks=1200)
    public static void campVisitKeepsTheRetreatStackButCrossingTheActualDoorReturns(GameTestHelper h){run(h,513500,f->{
        var p=f.player(h,"camp_retreat");f.enter(p);var record=FinaleProgress.player(p.server,p.getUUID());record.putBoolean("Inside",true);record.putInt("StairFires",5);FinaleProgress.save(p.server,p.getUUID(),record);p.tickCount=1;
        h.assertTrue(f.level.getBlockState(FinaleArchitecture.entry(f.origin)).is(Blocks.SPRUCE_DOOR)&&f.level.getBlockState(FinaleArchitecture.entry(f.origin)).getValue(DoorBlock.OPEN),"the production arrival retains a real open native door");
        p.moveTo(Vec3.atBottomCenterOf(FinaleRepairs.tom(f.origin)));FinaleController.tickPlayer(p,f.origin);
        h.assertTrue(f.data().returnDepth(p.getUUID())==1,"the connected camp remains part of the staircase visit");
        p.moveTo(Vec3.atBottomCenterOf(f.base.offset(-6,FinaleArchitecture.TOP,30)));FinaleController.tickPlayer(p,f.origin);
        h.assertTrue(f.data().returnDepth(p.getUUID())==1,"the old camp side of the entrance cannot consume a saved retreat");
        p.moveTo(Vec3.atBottomCenterOf(FinaleArchitecture.entry(f.origin).south(2)));FinaleController.tickPlayer(p,f.origin);
        h.assertTrue(f.data().returnDepth(p.getUUID())==0&&p.position().distanceToSqr(Vec3.atBottomCenterOf(f.source))<4,"crossing the real entry corridor returns to the exact saved source door");
    });}


    @GameTest(template="empty",batch="staircase_access",timeoutTicks=1200)
    public static void theCampIsCarriedByBeamsAndChainsAndArrivalsCopyOnlyTheirHall(GameTestHelper h){run(h,514500,f->{
        int top=FinaleArchitecture.TOP,wall=FinaleArchitecture.SHAFT_RADIUS;var supports=new LinkedHashMap<BlockPos,BlockState>();FinaleRepairs.supportPlan(supports,f.base);
        var entrance=new HashMap<BlockPos,BlockState>();for(var placement:FinaleArchitecture.entrancePlan(f.origin))entrance.put(placement.pos(),placement.block());
        for(int x:new int[]{-14,-9}){
            for(int z=19;z<wall;z++)h.assertTrue(supports.get(f.base.offset(x,top-2,z))!=null&&supports.get(f.base.offset(x,top-2,z)).isSolid(),"a beam runs under the camp to the shaft wall at "+x+","+z);
            h.assertTrue(entrance.get(f.base.offset(x,top-1,22)).is(Blocks.DARK_OAK_PLANKS)&&supports.get(f.base.offset(x,top-3,wall-1)).is(Blocks.DEEPSLATE_TILE_STAIRS),"the beam carries the camp floor and rests on a corbel");
        }
        for(int x:new int[]{-15,-8})for(int z:new int[]{19,26}){
            h.assertTrue(entrance.get(f.base.offset(x,top+4,z)).is(Blocks.DEEPSLATE_TILES),"the camp roof corner exists");
            for(int y=top+5;y<top+15;y++)h.assertTrue(supports.get(f.base.offset(x,y,z)).is(Blocks.CHAIN),"a chain runs from the roof corner to the shaft cap");
        }
        for(int x=-7;x<=-4;x++)h.assertTrue(entrance.get(f.base.offset(x,top,26)).is(Blocks.IRON_BARS),"the walkway's south rail stands outside the hall at "+x);
        var sentinel=f.base.offset(5,top+1,27);f.put(f.level,sentinel,Blocks.GLOWSTONE.defaultBlockState());
        var p=f.player(h,"hall_copy");f.enter(p);
        for(int x=-7;x<=-4;x++)h.assertTrue(f.level.getBlockState(f.base.offset(x,top,26)).is(Blocks.IRON_BARS),"a production arrival copy leaves the walkway rail at "+x);
        h.assertTrue(f.level.getBlockState(sentinel).is(Blocks.GLOWSTONE)&&f.level.getBlockState(FinaleArchitecture.entry(f.origin)).is(Blocks.SPRUCE_DOOR),"the copy fills the entry hall's door and stops at its walls");
        for(int z=27;z<=FinaleArchitecture.SHAFT_RADIUS;z++)for(int x:new int[]{-3,3}){var side=f.base.offset(x,top+1,z);
            h.assertTrue(!f.level.getBlockState(side).getCollisionShape(f.level,side).isEmpty(),"a wide, wall-less source room cannot open the hall onto the shaft at "+x+","+z);}
    });}

    @GameTest(template="empty",batch="staircase_access",timeoutTicks=1200)
    public static void oneLeafLiesOnALandingInTheLastTurnAboveEachCamp(GameTestHelper h){run(h,515000,f->{
        // 0.4.595: the five fires are the nights of the descent. The camps are spread down the whole stair, and each camp's leaf
        // lies on a landing no more than three above it, where every reader walks.
        var route=FinaleArchitecture.staircaseRoute(f.origin);var leaves=StaircaseLeaves.positions(f.origin);var fires=StaircaseFire.braziers(f.origin);
        var corners=StaircaseFire.corners(f.origin);var camps=StaircaseFire.landings(f.origin);
        h.assertTrue(leaves.size()==StaircaseFire.REQUIRED&&new HashSet<>(leaves).size()==leaves.size(),"five distinct leaves, one per hearth");
        h.assertTrue(camps.size()==StaircaseFire.REQUIRED&&fires.size()==StaircaseFire.REQUIRED,"five camps, a hearth at each");
        h.assertTrue(corners.getFirst().equals(StaircaseFire.threshold(f.origin))&&corners.getFirst().getY()>FinaleArchitecture.TOP-12,"the first landing is the one just below the door");
        var plan=new HashMap<BlockPos,BlockState>();for(var p:FinaleArchitecture.plan(f.origin))plan.put(p.pos(),p.block());
        for(int k=0;k<StaircaseFire.REQUIRED;k++){
            var camp=camps.get(k);int depth=FinaleArchitecture.TOP-camp.getY();
            h.assertTrue(camp.equals(corners.get(StaircaseFire.NIGHTS[k])),"camp "+k+" stands on its own landing");
            h.assertTrue(k==0?depth>120:camp.getY()<camps.get(k-1).getY()-200,"the camps are a long way apart: camp "+k+" is "+depth+" blocks down");
            h.assertTrue(camp.getY()>FinaleArchitecture.ARENA,"camp "+k+" is above the cell");
            var hearth=fires.get(k);int sx=Integer.signum(camp.getX()-f.base.getX()),sz=Integer.signum(camp.getZ()-f.base.getZ());
            h.assertTrue(plan.get(hearth)!=null&&plan.get(hearth).is(Blocks.CAMPFIRE)&&!plan.get(hearth).getValue(CampfireBlock.LIT),"camp "+k+" is built with its hearth cold");
            h.assertTrue(plan.get(hearth.below()).is(Blocks.POLISHED_BASALT),"the hearth stands on a hearthstone");
            for(int w=6;w<=7;w++)for(var bed:List.of(camp.offset(sx*w,0,sz*3),camp.offset(sx*3,0,sz*w))){
                h.assertTrue(plan.get(bed)!=null&&plan.get(bed).is(net.minecraft.tags.BlockTags.WOOL_CARPETS),"a bedroll lies out from camp "+k+"'s fire at "+bed);
                h.assertTrue(plan.get(bed.below())!=null&&!plan.get(bed.below()).isAir(),"the bedroll lies on the landing's own floor");
                h.assertTrue(!route.contains(bed)&&bed.distManhattan(camp)>=6,"the bedroll is clear of the walk from one flight to the next");}
            var leaf=leaves.get(k);var landing=corners.get(StaircaseLeaves.LANDINGS[k]);
            h.assertTrue(leaf.getY()==landing.getY()&&Math.abs(leaf.getX()-landing.getX())<=4&&Math.abs(leaf.getZ()-landing.getZ())<=4&&!leaf.equals(landing),
                    "leaf "+k+" lies on its landing, where the flights meet: "+leaf+" on "+landing);
            h.assertTrue(plan.get(leaf.below())!=null&&!plan.get(leaf.below()).isAir()&&(plan.get(leaf)==null||plan.get(leaf).isAir()),"leaf "+k+" has floor under it and nothing in its place");
            int above=StaircaseFire.NIGHTS[k]-StaircaseLeaves.LANDINGS[k];
            h.assertTrue(above>=1&&above<=3,"leaf "+k+" lies within the last turn above its camp: "+above+" landings up");
            h.assertTrue(StaircaseLeaves.LANDINGS[k]%3!=1,"no loose sheet of the household's lies on leaf "+k+"'s landing");
            h.assertTrue(!fires.contains(leaf)&&!camps.contains(landing),"leaf "+k+" lies above the camp, not at its hearth");
            var lit=new CompoundTag();lit.putInt("StairFires",k);
            h.assertTrue(leaf.getY()>=StaircaseFire.edge(f.origin,lit).getY()-.75,"leaf "+k+" is reachable once "+k+" fires burn");
            if(k>0){var before=new CompoundTag();before.putInt("StairFires",k-1);
                h.assertTrue(leaf.getY()<StaircaseFire.edge(f.origin,before).getY()-.75,"leaf "+k+" lies beyond the reach of the fire before it");}
            h.assertTrue(leaf.getY()<StaircaseFire.reach(f.origin,new CompoundTag(),false).getY()-.75,"a reader who has not taken their book from the shelf reaches no leaf: "+k);
            h.assertTrue(camp.getY()<StaircaseFire.reach(f.origin,new CompoundTag(),false).getY()-.75,"nor any camp: "+k);
        }
        h.assertTrue(StaircaseFire.reach(f.origin,new CompoundTag(),true).equals(StaircaseFire.edge(f.origin,new CompoundTag())),"with their book, the dark stops them past the first camp");
        // The dark is the book's twelve blocks at every depth; only the chamber at the foot of the stair and the side mazes are out of it.
        var foot=Vec3.atBottomCenterOf(f.base.offset(0,FinaleArchitecture.ARENA,40));
        h.assertTrue(StaircaseFire.off(f.origin,foot)&&StaircaseFire.off(f.origin,Vec3.atBottomCenterOf(f.base.offset(FinaleArchitecture.SHAFT_RADIUS+6,FinaleArchitecture.TOP-400,0))),
                "the chamber and the side mazes keep their own dark");
        for(var camp:camps)h.assertTrue(!StaircaseFire.off(f.origin,Vec3.atBottomCenterOf(camp)),"every camp is on the stair");
        h.assertTrue(!StaircaseFire.off(f.origin,Vec3.atBottomCenterOf(route.getLast()))&&StaircaseFire.SIGHT==12,"the stair's dark holds to its last tread");
    });}

    @GameTest(template="empty",batch="staircase_access",timeoutTicks=1200)
    public static void anExistingCampGainsItsSupportsOnceAndLosesOnlyTheOldFloatingPlatform(GameTestHelper h){run(h,515500,
            f->f.chunks.hold(f.level,new AABB(f.base.getX()-17,FinaleArchitecture.TOP-4,f.base.getZ()+13,f.base.getX()+1,FinaleArchitecture.TOP+16,f.base.getZ()+FinaleArchitecture.SHAFT_RADIUS+1)),f->{
        int top=FinaleArchitecture.TOP;var done=new CompoundTag();done.putBoolean(Long.toString(f.origin.asLong()),true);f.data().setState("staircase_entrance_0443",done);
        var plank=f.base.offset(-6,top-1,17);var wool=f.base.offset(-8,top,16);var walkway=f.base.offset(-6,top-1,23);
        f.put(f.level,plank,Blocks.DARK_OAK_PLANKS.defaultBlockState());f.put(f.level,wool,Blocks.WHITE_WOOL.defaultBlockState());
        var supports=new LinkedHashMap<BlockPos,BlockState>();FinaleRepairs.supportPlan(supports,f.base);for(var at:supports.keySet())f.put(f.level,at,Blocks.AIR.defaultBlockState());
        var p=f.player(h,"camp_sitter");p.teleportTo(f.level,f.base.getX()-11.5,top,f.base.getZ()+22.5,0,0);
        FinaleRepairs.repairSupports(f.level,f.origin);
        h.assertTrue(f.level.getBlockState(f.base.offset(-14,top-2,30)).isAir()&&f.level.getBlockState(plank).is(Blocks.DARK_OAK_PLANKS),"nothing changes while an explorer is in the camp");
        p.teleportTo(f.level,f.source.getX()+.5,f.source.getY(),f.source.getZ()+.5,0,0);
        var pet=EntityType.WOLF.create(f.level);h.assertTrue(pet!=null,"a real native companion exists");
        pet.tame(p);pet.setNoAi(true);pet.setNoGravity(true);pet.setHealth(7);pet.moveTo(Vec3.atBottomCenterOf(plank.above()));
        CompanionOrders.issue(pet,p,CompanionOrders.Order.STAY);pet.setOrderedToSit(false);f.level.addFreshEntity(pet);var petId=pet.getUUID();var left=pet.position();
        var tom=NovelRegistry.ACTOR.get().create(f.level);h.assertTrue(tom!=null,"native Tom exists");
        tom.appearance(p.getUUID(),0);tom.moveTo(Vec3.atBottomCenterOf(FinaleRepairs.tom(f.origin)));f.level.addFreshEntity(tom);var tomId=tom.getUUID();
        try{
        FinaleRepairs.repairSupports(f.level,f.origin);
        h.assertTrue(f.level.getBlockState(plank).is(Blocks.DARK_OAK_PLANKS)&&f.level.getBlockState(f.base.offset(-14,top-2,30)).isAir()
                &&!f.data().state("staircase_supports_0445").getBoolean(Long.toString(f.origin.asLong())),"a parked Stay companion defers the entire repair, including removal of its floor");
        h.assertTrue(pet.position().equals(left)&&pet.getUUID().equals(petId)&&pet.getHealth()==7&&CompanionOrders.order(pet)==CompanionOrders.Order.STAY,"the repair never moves, replaces, heals or changes the parked companion");
        pet.moveTo(Vec3.atBottomCenterOf(f.base.offset(-11,top,22)));FinaleRepairs.repairSupports(f.level,f.origin);
        h.assertTrue(supports.entrySet().stream().allMatch(e->f.level.getBlockState(e.getKey()).equals(e.getValue())),"the beams, corbels and chains are added");
        h.assertTrue(f.level.getBlockState(plank).isAir()&&f.level.getBlockState(wool).isAir()&&f.level.getBlockState(walkway).is(Blocks.DARK_OAK_PLANKS),"only the old floating platform is lifted; the walkway stays");
        h.assertTrue(f.level.getEntity(tomId)==tom&&f.level.getEntity(petId)==pet&&pet.getHealth()==7&&CompanionOrders.order(pet)==CompanionOrders.Order.STAY,"Tom and a companion on retained camp flooring do not block safe support repairs or lose their identities");
        f.reload();
        f.level.setBlock(f.base.offset(-14,top-2,30),Blocks.AIR.defaultBlockState(),F);FinaleRepairs.repairSupports(f.level,f.origin);
        h.assertTrue(f.level.getBlockState(f.base.offset(-14,top-2,30)).isAir(),"the saved checkpoint keeps the pass from running again");
        }finally{pet.discard();tom.discard();}
    });}

    private static void holdScene(Fixture f,LabyrinthPlace place){
        var b=LabyrinthPlaces.base(f.origin,place);var r=place.room();var level=NovelRooms.outside(place)?f.outside:f.level;
        f.chunks.hold(level,new AABB(b.getX()+r.minX()-9,b.getY()+r.minY()-2,b.getZ()+r.minZ()-9,b.getX()+r.maxX()+10,b.getY()+r.maxY()+4,b.getZ()+r.maxZ()+3));
    }
    private static void track(Fixture f,ServerLevel level,BlockPos from,BlockPos to){
        for(var at:BlockPos.betweenClosed(from,to))f.touched.computeIfAbsent(level,k->new HashSet<>()).add(at.immutable());
    }
    private static void oldRoof(Fixture f,BlockPos b,int x0,int x1,int z0,int z1,int eaves){
        track(f,f.outside,b.offset(x0-1,eaves-1,z0-1),b.offset(x1+1,eaves+(x1-x0)/4+3,z1+1));
        for(int x=x0-1;x<=x1+1;x++){
            int y=eaves+Math.min(x-x0+1,x1+1-x)/2;
            for(int z=z0-1;z<=z1+1;z++)f.put(f.outside,b.offset(x,y,z),Blocks.SPRUCE_STAIRS.defaultBlockState());
        }
    }
    private static void composed(Fixture f,LabyrinthPlace place){
        String key=f.origin.asLong()+":"+place.id();var done=f.data().state(SceneCraft.STATE);done.putBoolean(key,true);f.data().setState(SceneCraft.STATE,done);
    }


}
