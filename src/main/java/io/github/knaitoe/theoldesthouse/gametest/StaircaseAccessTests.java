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
            StairCompany.clearAll(level);
            var remove=new ArrayList<Entity>();for(var e:level.getAllEntities())if(FinaleArchitecture.contains(origin,e.blockPosition())&&(e instanceof NovelActor||e instanceof FinaleWitness||e instanceof LiteraryActor a&&a.company()))remove.add(e);
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
            java.util.function.BiConsumer<ServerPlayer,BlockPos> stand=(q,to)->{q.teleportTo(f.level,to.getX()+.5,to.getY(),to.getZ()+.5,0,0);q.setOnGround(true);};
            // 0.4.596: this corner's landings, counted up from the lowest, are the stair's 72nd, 68th, 64th and so on from the door.
            // The fourth night's camp is on the 56th (the fifth of these) and the third's on the 40th (the ninth).
            var camps=StaircaseFire.landings(f.origin);
            h.assertTrue(landings.get(4).equals(camps.get(3))&&landings.get(8).equals(camps.get(2)),"the camps this climb passes are where the case takes them to be: "+camps);
            // Going down never carries anyone.
            stand.accept(p,landings.get(4));StaircaseAscent.step(p,f.origin);stand.accept(p,landings.get(3));
            h.assertTrue(StaircaseAscent.step(p,f.origin)==0&&p.blockPosition().equals(landings.get(3)),"a turn walked down is walked");
            // A whole turn climbed, from the corner to the same corner a turn up, carries the reader up two more.
            p.getPersistentData().remove("StairAscent199");
            stand.accept(p,landings.get(0));StaircaseAscent.step(p,f.origin);stand.accept(p,landings.get(1));
            h.assertTrue(StaircaseAscent.step(p,f.origin)==2*StaircaseAscent.TURN&&p.blockPosition().equals(landings.get(3)),"a turn climbed rises three, to the same landing: at "+p.blockPosition()+", expected "+landings.get(3));
            h.assertTrue(FinaleProgress.player(server,p.getUUID()).getInt("AscentLifts")==1,"the climb remembers it was carried");
            // 0.4.596: on the way home nobody is carried from a camp's landing or past one. The camp a turn above is walked up to,
            // and walked out of; the turn after it carries again, counted from the camp.
            stand.accept(p,landings.get(4));
            h.assertTrue(StaircaseAscent.step(p,f.origin)==0&&p.blockPosition().equals(landings.get(4)),"a reader who climbs a turn onto a camp's landing stands at the camp");
            stand.accept(p,landings.get(5));
            h.assertTrue(StaircaseAscent.step(p,f.origin)==2*StaircaseAscent.TURN&&p.blockPosition().equals(landings.get(7)),"the turn climbed out of a camp carries again");
            stand.accept(p,landings.get(8));
            h.assertTrue(StaircaseAscent.step(p,f.origin)==0&&p.blockPosition().equals(landings.get(8)),"and the next camp is walked up to in its turn");
            p.getPersistentData().remove("StairAscent199");
            stand.accept(p,landings.get(2));StaircaseAscent.step(p,f.origin);stand.accept(p,landings.get(3));
            h.assertTrue(StaircaseAscent.step(p,f.origin)==0&&p.blockPosition().equals(landings.get(3)),"a climb that would carry a reader past a camp is walked");
            // A reader still on their way down, who has turned back, is carried as before: the camps are theirs to pass.
            FinaleProgress.phase(server,p.getUUID(),FinaleProgress.Phase.STAIRCASE);p.getPersistentData().remove("StairAscent199");
            stand.accept(p,landings.get(2));StaircaseAscent.step(p,f.origin);stand.accept(p,landings.get(3));
            h.assertTrue(StaircaseAscent.step(p,f.origin)==2*StaircaseAscent.TURN&&p.blockPosition().equals(landings.get(5)),"the camps hold only a reader on their way home");
            FinaleProgress.phase(server,p.getUUID(),FinaleProgress.Phase.HOMEWARD);
            // Two readers climbing side by side are carried together.
            p.getPersistentData().remove("StairAscent199");
            stand.accept(p,landings.get(0));stand.accept(peer,landings.get(0).east());StaircaseAscent.step(p,f.origin);StaircaseAscent.step(peer,f.origin);
            stand.accept(p,landings.get(1));stand.accept(peer,landings.get(1).east());
            h.assertTrue(StaircaseAscent.step(p,f.origin)==2*StaircaseAscent.TURN&&p.blockPosition().equals(landings.get(3))&&peer.blockPosition().equals(landings.get(3).east()),"a party climbing together is carried together");
            // Anyone else who could see, a spectator included, keeps the reader walking.
            p.getPersistentData().remove("StairAscent199");watcher.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);
            stand.accept(peer,landings.get(10));stand.accept(p,landings.get(0));StaircaseAscent.step(p,f.origin);
            stand.accept(watcher,landings.get(1).north(3));stand.accept(p,landings.get(1));
            h.assertTrue(StaircaseAscent.step(p,f.origin)==0&&p.blockPosition().equals(landings.get(1)),"a watched climb is walked");
            stand.accept(watcher,landings.get(0).below(40));
            // The top turns are always walked.
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

    // ------------------------------------------------------------------------------------------------ 0.4.596: the reader's own stories' people
    private static AABB well(Fixture f){return new AABB(f.base.getX()-34,FinaleArchitecture.TOP,f.base.getZ()-34,f.base.getX()+35,FinaleArchitecture.TOP+1,f.base.getZ()+35);}
    private static void stand(Fixture f,ServerPlayer p,Vec3 at,Vec3 toward){
        var d=toward.subtract(at.add(0,p.getEyeHeight(),0));
        float yaw=(float)(Math.toDegrees(Math.atan2(d.z,d.x))-90),pitch=(float)-Math.toDegrees(Math.atan2(d.y,Math.hypot(d.x,d.z)));
        p.teleportTo(f.level,at.x,at.y,at.z,yaw,pitch);p.setOnGround(true);
    }
    private static void hear(Fixture f,ServerPlayer p,WitnessAccount.Story... stories){for(var s:stories)WitnessAccount.resolve(f.data(),p.getUUID(),s,"heard");}
    /** The people on the stair come from one list, and every story is somewhere on it. */
    @GameTest(template="empty",batch="staircase_access",timeoutTicks=1200)
    public static void everyStoryIsAccountedForOnTheStair(GameTestHelper h){
        var names=new HashSet<String>();var withFigure=EnumSet.noneOf(WitnessAccount.Story.class);
        for(var f:StairCompany.FIGURES){
            h.assertTrue(names.add(f.name())&&f.stories().length>0,"each figure has a name of its own and a story it is from: "+f.name());
            withFigure.addAll(List.of(f.stories()));
        }
        for(var story:WitnessAccount.Story.values()){
            int places=(withFigure.contains(story)?1:0)+(StairCompany.NOT_YET.contains(story)?1:0)+(StairCompany.NOBODY.contains(story)?1:0)+(StairCompany.LEFT_OUT.contains(story)?1:0);
            h.assertTrue(places==1,"each story has a figure, or one still to be given a body, or nobody, or is left out, and only one of the four: "+story+" is in "+places);
        }
        h.assertTrue(StairCompany.camp(0,7)==0&&StairCompany.camp(6,7)==4&&StairCompany.camp(0,1)==0&&StairCompany.camp(3,0)==0,"the first of a reader's stories stands by the door and the most recent deepest");
        int last=0;int[] filled=new int[StaircaseFire.REQUIRED];
        for(int i=0;i<StairCompany.FIGURES.size();i++){int c=StairCompany.camp(i,StairCompany.FIGURES.size());h.assertTrue(c>=last&&c<StaircaseFire.REQUIRED,"the camps are filled in the order the stories ended");last=c;filled[c]++;}
        var spots=new HashSet<Long>();
        for(var s:StairCompany.SPOTS){
            h.assertTrue(spots.add(BlockPos.asLong(s[0],0,s[1]))&&s[0]>=0&&s[1]>=0&&s[0]<=9&&s[1]<=9&&s[0]+s[1]>0,"each place round a fire is its own, on the landing's floor, off the corner the walk turns on: "+s[0]+", "+s[1]);
            h.assertTrue(!(s[0]==3&&s[1]==3)&&!(s[1]==3&&(s[0]==6||s[0]==7))&&!(s[0]==3&&(s[1]==6||s[1]==7)),"nobody stands in the fire or on a bedroll: "+s[0]+", "+s[1]);
        }
        for(int n:filled)h.assertTrue(n<=StairCompany.SPOTS.length,"there is a place at each fire for everyone who could stand there: "+n);
        h.succeed();
    }
    /** On the way down a reader's people stand ahead in the dark, one at a time, and are gone when reached. */
    @GameTest(template="empty",batch="staircase_access",timeoutTicks=1200)
    public static void aReadersPeopleStandAheadInTheDarkAndAreGoneWhenReached(GameTestHelper h){
        run(h,596000,f->f.chunks.hold(f.level,well(f)),f->{
            var server=f.level.getServer();var p=f.player(h,"descender");var peer=f.player(h,"another");
            FinaleProgress.phase(server,p.getUUID(),FinaleProgress.Phase.STAIRCASE);var record=FinaleProgress.player(server,p.getUUID());
            var route=FinaleArchitecture.staircaseRoute(f.origin);int[] corners=StaircaseFire.cornerSteps(f.origin),camps=StaircaseFire.campSteps(f.origin);
            java.util.function.IntConsumer at=step->{var c=route.get(step);stand(f,p,Vec3.atBottomCenterOf(c),Vec3.atBottomCenterOf(route.get(step+6)).add(0,1.6,0));p.tickCount=100;};
            h.assertTrue(StairCompany.stepAt(f.origin,Vec3.atBottomCenterOf(route.get(700)))==700&&StairCompany.stepAt(f.origin,Vec3.atBottomCenterOf(route.get(0)).add(0,40,0))==0,"a place on the stair is found by its height");
            // Nobody stands for a reader who has heard no story to the end.
            at.accept(corners[2]+6);StairCompany.down(p,f.origin,record);
            h.assertTrue(StairCompany.people(p).isEmpty()&&StairCompany.shape(p)==null&&StairCompany.sightings(f.origin,0).isEmpty(),"a reader with no story heard has nobody on the stair");
            // Five stories heard: three have someone to stand for them, in the order they ended.
            hear(f,p,WitnessAccount.Story.PLAIN,WitnessAccount.Story.FLOORBOARDS,WitnessAccount.Story.MASQUE,WitnessAccount.Story.MOTHER,WitnessAccount.Story.PELL_HOUSE);
            hear(f,peer,WitnessAccount.Story.USHER);
            var people=StairCompany.people(p);
            h.assertTrue(people.size()==3&&people.get(0).name().equals("the boy, crouched")&&people.get(1).name().equals("the red figure")&&people.get(2).name().equals("June"),"a reader's people are those of the stories they heard, in order: "+people.stream().map(StairCompany.Figure::name).toList());
            var sightings=StairCompany.sightings(f.origin,people.size());
            h.assertTrue(sightings.size()==3,"each of three people stands once on the way down: "+sightings.size());
            for(int i=0;i<sightings.size();i++){
                int step=sightings.get(i)[0],camp=StairCompany.camp(sightings.get(i)[1],people.size());int flight=0;while(corners[flight+1]<=step)flight++;
                h.assertTrue(step==(corners[flight]+corners[flight+1])/2&&step<camps[camp]-StairCompany.CLEAR&&(camp==0?step>corners[1]:step>camps[camp-1]+StairCompany.CLEAR),"each stands in the middle of a flight, on the stretch above the camp they will stand at, clear of the camps: step "+step+", camp "+camp);
                h.assertTrue(i==0||step>sightings.get(i-1)[0],"and they are met in the order the stories ended");
            }
            h.assertTrue(StairCompany.sightings(f.origin,48).size()<=StairCompany.MOST*StaircaseFire.REQUIRED&&StairCompany.sightings(f.origin,48).size()>=20,"a reader with every story heard meets a few on each stretch, not all of them");
            // Coming down toward the first, it is set down ahead, beyond what the book lights.
            int first=sightings.get(0)[0];
            at.accept(first-StairCompany.AHEAD_FAR-4);StairCompany.down(p,f.origin,record);
            h.assertTrue(StairCompany.shape(p)==null,"it is not there while the reader is a long way above");
            at.accept(first-StairCompany.AHEAD_NEAR+1);StairCompany.down(p,f.origin,record);
            h.assertTrue(StairCompany.shape(p)==null,"nor is it set down in front of a reader who is already near enough to make it out");
            at.accept(first-22);StairCompany.down(p,f.origin,record);var boy=StairCompany.shape(p);
            h.assertTrue(boy!=null&&boy.role()==LiteraryActor.SILHOUETTE&&boy.phase()==4&&boy.company()&&boy.shape(),"the first of their people stands ahead on the stair, as a shape");
            double off=p.getEyePosition().distanceTo(boy.position().add(0,1,0));
            h.assertTrue(boy.position().distanceTo(Vec3.atBottomCenterOf(route.get(first)))<.01&&off>=StairCompany.SHOWN_FROM&&StairCompany.shown(off,0)==0,"in the middle of the walk, too far off for anything of it to be made out yet: "+off);
            h.assertTrue(StairCompany.shown(StairCompany.SHOWN_FULL,0)==1&&StairCompany.shown(8,0)==1&&StairCompany.shown(16.5,0)>0&&StairCompany.shown(16.5,0)<1
                    &&Math.abs(StairCompany.shown(10,50)-.5F)<1e-6&&StairCompany.shown(4,100)==0,"it comes out of the dark as the reader comes down, and goes as it is reached");
            h.assertTrue(boy.owner().filter(p.getUUID()::equals).isPresent()&&boy.broadcastToPlayer(p)&&!boy.broadcastToPlayer(peer)&&!boy.shouldBeSaved(),"it is that reader's alone to see, and nothing is saved of it");
            var toReader=p.position().subtract(boy.position());float facing=(float)(Math.toDegrees(Math.atan2(toReader.z,toReader.x))-90);
            h.assertTrue(Math.abs(net.minecraft.util.Mth.wrapDegrees(boy.getYRot()-facing))<1,"it stands facing the reader coming down");
            h.assertTrue(!boy.hurt(p.damageSources().playerAttack(p),8)&&boy.isAlive()&&!boy.isPushable()&&!boy.addresses(),"a blow does nothing to it, and it is nobody to push or talk to");
            // Another reader's people are their own.
            FinaleProgress.phase(server,peer.getUUID(),FinaleProgress.Phase.STAIRCASE);
            h.assertTrue(StairCompany.shape(peer)==null&&StairCompany.people(peer).size()==1,"another reader has their own");
            // It stands as it is until the reader is nearly on it.
            at.accept(first-10);for(int i=0;i<10;i++)StairCompany.down(p,f.origin,record);
            h.assertTrue(StairCompany.shape(p)==boy&&boy.fade()==0&&boy.position().distanceTo(Vec3.atBottomCenterOf(route.get(first)))<.01,"it does not move or thin while the reader comes on");
            StairCompany.sweep(server);
            h.assertTrue(StairCompany.shape(p)==boy&&!boy.isRemoved(),"the stair's look for readers who have left leaves the shape of one who is on it");
            at.accept(first-4);StairCompany.down(p,f.origin,record);
            h.assertTrue(boy.fade()==StairCompany.FADE&&!boy.isRemoved(),"reached, it begins to go");
            for(int i=0;i<100/StairCompany.FADE+1;i++)StairCompany.down(p,f.origin,record);
            h.assertTrue(StairCompany.shape(p)==null&&boy.isRemoved()&&StairCompany.shown(record,people.get(0))&&StairCompany.shown(FinaleProgress.player(server,p.getUUID()),people.get(0)),"and is gone, and that is remembered");
            at.accept(first-22);StairCompany.down(p,f.origin,record);
            h.assertTrue(StairCompany.shape(p)==null,"it does not stand there a second time");
            // The next of them further down. A reader who turns back leaves it, and finds it there again.
            int second=sightings.get(1)[0];
            at.accept(second-22);StairCompany.down(p,f.origin,record);var red=StairCompany.shape(p);
            h.assertTrue(red!=null&&red.role()==LiteraryActor.RED_FIGURE,"the next of their people stands on the stretch below");
            at.accept(second-80);StairCompany.down(p,f.origin,record);
            h.assertTrue(StairCompany.shape(p)==null&&red.isRemoved()&&!StairCompany.shown(record,people.get(1)),"a reader who climbs away from it leaves nobody standing, and has not met it");
            at.accept(second-22);StairCompany.down(p,f.origin,record);var again=StairCompany.shape(p);
            h.assertTrue(again!=null&&again.role()==LiteraryActor.RED_FIGURE,"coming back down, it is there again");
            // A reader who leaves the stair leaves nobody on it.
            p.teleportTo(f.level,f.source.getX()+.5,f.source.getY(),f.source.getZ()+2.5,0,0);StairCompany.sweep(server);
            h.assertTrue(StairCompany.shape(p)==null&&again.isRemoved(),"a reader who has left the stair leaves nobody standing on it");
        });
    }
    /** On the climb home a reader's people stand at the fires they lit, and come after them only while unseen. */
    @GameTest(template="empty",batch="staircase_access",timeoutTicks=1200)
    public static void aReadersPeopleStandAtTheirFiresAndComeAfterThemUnseen(GameTestHelper h){
        run(h,597000,f->f.chunks.hold(f.level,well(f)),f->{
            var server=f.level.getServer();var p=f.player(h,"homeward");var peer=f.player(h,"beside");
            hear(f,p,WitnessAccount.Story.PLAIN,WitnessAccount.Story.MASQUE,WitnessAccount.Story.USHER,WitnessAccount.Story.BLY_ROUTE,WitnessAccount.Story.HOLY_RABBIT,
                    WitnessAccount.Story.CONFESSION,WitnessAccount.Story.COSTUME_NIGHT,WitnessAccount.Story.MOVIE_NIGHT,WitnessAccount.Story.CAMP_BLOOD,WitnessAccount.Story.ELK_CARCASSES,WitnessAccount.Story.PELL_HOUSE);
            FinaleProgress.phase(server,p.getUUID(),FinaleProgress.Phase.HOMEWARD);
            var route=FinaleArchitecture.staircaseRoute(f.origin);var camps=StaircaseFire.landings(f.origin);int[] steps=StaircaseFire.campSteps(f.origin);
            var people=StairCompany.people(p);
            h.assertTrue(people.size()==10&&people.get(8).name().equals("the man with the blade")&&people.get(9).name().equals("June"),"two stories that share a figure have it once: "+people.stream().map(StairCompany.Figure::name).toList());
            java.util.function.Function<Integer,Vec3> on=step->Vec3.atBottomCenterOf(route.get(step));
            // At the foot of the stair, looking up it: the people of the deepest camp are at its fire, and no others yet.
            int foot=route.size()-1;stand(f,p,on.apply(foot),on.apply(foot-8).add(0,1.6,0));StairCompany.home(p,f.origin);
            var there=StairCompany.bodies(p);var fire=StaircaseFire.braziers(f.origin).get(4).getBottomCenter();
            h.assertTrue(there.size()==2&&there.get(0).role()==LiteraryActor.KILLER&&there.get(1).phase()==PellPeople.FIRST+PellPeople.JUNE,"the two most recent of ten stand at the deepest camp: "+there.size());
            var stood=new ArrayList<Vec3>();
            for(var a:there){
                var toFire=fire.subtract(a.position());float facing=(float)(Math.toDegrees(Math.atan2(toFire.z,toFire.x))-90);
                h.assertTrue(a.position().distanceTo(fire)<4&&a.position().distanceTo(fire)>1.5&&Math.abs(a.getY()-camps.get(4).getY())<.01&&Math.abs(net.minecraft.util.Mth.wrapDegrees(a.getYRot()-facing))<1,"each stands by the fire on the camp's landing, turned to it: "+a.position());
                h.assertTrue(a.company()&&!a.shape()&&a.broadcastToPlayer(p)&&!a.broadcastToPlayer(peer)&&!a.shouldBeSaved(),"and is that reader's alone, and drawn as it is in its story");
                stood.add(a.position());
            }
            // Walked up to and stood among, they do not stir.
            stand(f,p,Vec3.atBottomCenterOf(camps.get(4)),fire.add(0,1,0));for(int i=0;i<20;i++)StairCompany.home(p,f.origin);
            h.assertTrue(StairCompany.following(p)==0&&there.get(0).position().equals(stood.get(0))&&there.get(1).position().equals(stood.get(1)),"they stand as they are while the reader is at their fire");
            StairCompany.sweep(server);
            h.assertTrue(StairCompany.bodies(p).size()==2&&!there.get(0).isRemoved()&&!there.get(1).isRemoved(),"the stair's look for readers who have left leaves the people of one who is on it");
            // Past the camp and looking on up the stair, they come after: on the stair behind the reader, turned to them.
            int r=steps[4]-20;stand(f,p,on.apply(r),on.apply(r-8).add(0,1.6,0));for(int i=0;i<20;i++)StairCompany.home(p,f.origin);
            h.assertTrue(StairCompany.following(p)==2&&StairCompany.looks(p)==0,"once the reader has climbed past, the camp's people are coming after them");
            for(var a:there){
                int behind=StairCompany.stepAt(f.origin,a.position())-r;var toReader=p.position().subtract(a.position());float facing=(float)(Math.toDegrees(Math.atan2(toReader.z,toReader.x))-90);
                h.assertTrue(behind==StairCompany.GAP&&a.getY()<p.getY()&&Math.abs(net.minecraft.util.Mth.wrapDegrees(a.getYRot()-facing))<1,"each stands on the stair below the reader, as far behind as the first look allows, turned to them: "+behind+" steps");
            }
            h.assertTrue(there.get(0).position().distanceTo(there.get(1).position())>2,"abreast, not one inside another");
            // Looked back at, they are standing still, however long the look.
            var before=List.of(there.get(0).position(),there.get(1).position());
            stand(f,p,on.apply(r),there.get(1).position().add(0,1,0));for(int i=0;i<40;i++)StairCompany.home(p,f.origin);
            h.assertTrue(StairCompany.looks(p)==1&&there.get(0).position().equals(before.get(0))&&there.get(1).position().equals(before.get(1)),"a reader who looks back sees them standing still, and one long look is one look: "+StairCompany.looks(p));
            // Climbing on with eyes on them, they stay where they were seen.
            stand(f,p,on.apply(r-12),there.get(1).position().add(0,1,0));for(int i=0;i<20;i++)StairCompany.home(p,f.origin);
            h.assertTrue(there.get(0).position().equals(before.get(0))&&there.get(1).position().equals(before.get(1)),"watched, they do not follow");
            // Eyes forward again: they come on, and a step nearer than before.
            stand(f,p,on.apply(r-12),on.apply(r-20).add(0,1.6,0));for(int i=0;i<30;i++)StairCompany.home(p,f.origin);
            for(var a:there)h.assertTrue(StairCompany.stepAt(f.origin,a.position())-(r-12)==StairCompany.GAP-1,"each look back brings them a step nearer: "+(StairCompany.stepAt(f.origin,a.position())-(r-12)));
            // They never come down again: a reader who walks back down past them finds them where they stood.
            var up=List.of(there.get(0).position(),there.get(1).position());
            stand(f,p,on.apply(r+30),on.apply(r+38).add(0,1.6,0));for(int i=0;i<20;i++)StairCompany.home(p,f.origin);
            h.assertTrue(there.get(0).position().equals(up.get(0))&&there.get(1).position().equals(up.get(1)),"they do not go back down the stair after a reader");
            // The shorter climb carries them as it carries the reader, and no further than the stair goes.
            stand(f,p,on.apply(r-12),on.apply(r-20).add(0,1.6,0));for(int i=0;i<30;i++)StairCompany.home(p,f.origin);
            var low=List.of(there.get(0).position(),there.get(1).position());int lift=2*StaircaseAscent.TURN;
            StairCompany.lifted(p,f.origin,lift);
            h.assertTrue(there.get(0).position().equals(low.get(0).add(0,lift,0))&&there.get(1).position().equals(low.get(1).add(0,lift,0)),"carried up with the reader, each stands where it stood on the turn above");
            p.teleportTo(f.level,p.getX(),p.getY()+lift,p.getZ(),p.getYRot(),p.getXRot());for(int i=0;i<10;i++)StairCompany.home(p,f.origin);
            for(var a:there)h.assertTrue(StairCompany.stepAt(f.origin,a.position())-StairCompany.stepAt(f.origin,p.position())==StairCompany.GAP-1&&a.position().y-low.get(there.indexOf(a)).y==lift,"and is as far behind them as it was");
            // Each camp's own people wait at it until the reader comes near.
            h.assertTrue(StairCompany.bodies(p).size()==2,"the camps above have nobody standing yet");
            stand(f,p,Vec3.atBottomCenterOf(camps.get(3)).add(0,-60,0),Vec3.atBottomCenterOf(camps.get(3)));StairCompany.home(p,f.origin);
            h.assertTrue(StairCompany.bodies(p).size()==4,"coming up under the next camp, its people are at its fire: "+StairCompany.bodies(p).size());
            // However many come after a reader, and wherever on the stair, no two of them stand in one place.
            for(var figure:StairCompany.FIGURES)hear(f,peer,figure.stories());
            FinaleProgress.phase(server,peer.getUUID(),FinaleProgress.Phase.HOMEWARD);
            h.assertTrue(StairCompany.people(peer).size()==StairCompany.FIGURES.size(),"a reader who has heard every story has everyone");
            // The reader climbs on: those whose place in the column is still below their own fire wait at it, and fall in as it passes.
            for(int past:new int[]{12,20,34,60}){
                int at=steps[0]-past;stand(f,peer,on.apply(at),on.apply(at-8).add(0,1.6,0));for(int i=0;i<160;i++)StairCompany.home(peer,f.origin);
                var crowd=StairCompany.bodies(peer);
                h.assertTrue(crowd.size()==StairCompany.FIGURES.size()&&StairCompany.following(peer)==crowd.size(),past+" steps past the first camp, everyone has come after them: "+crowd.size()+" standing, "+StairCompany.following(peer)+" following");
                for(int i=0;i<crowd.size();i++){
                    var one=crowd.get(i);int behind=StairCompany.stepAt(f.origin,one.position())-at;
                    h.assertTrue(behind>=StairCompany.LEAST&&behind<=StairCompany.GAP+StairCompany.ROW*(crowd.size()/3)+StairCompany.TRIES+2&&one.getY()<=peer.getY(),"each of them is on the stair behind the reader, and not far behind: "+behind+" steps, "+past+" past the camp, at "+one.position()+" (the reader at "+peer.position()+")");
                    for(int j=i+1;j<crowd.size();j++){
                        var other=crowd.get(j);double dx=one.getX()-other.getX(),dz=one.getZ()-other.getZ();
                        h.assertTrue(Math.abs(one.getY()-other.getY())>=3||dx*dx+dz*dz>=StairCompany.APART*StairCompany.APART-1e-6,"no two stand in one place, "+past+" steps past the camp's corner: "+one.position()+" and "+other.position());
                    }
                }
            }
            h.assertTrue(StairCompany.bodies(peer).stream().allMatch(a->StairCompany.stepAt(f.origin,a.position())<steps[0]),"sixty steps on, none of them is still at the fire");
            // The camps hold the shorter climb for a reader going home, and nobody else.
            int y=camps.get(3).getY();
            h.assertTrue(StairCompany.holds(p,f.origin,y,lift)&&StairCompany.holds(p,f.origin,y-lift,lift)&&!StairCompany.holds(p,f.origin,y+1,lift)&&!StairCompany.holds(p,f.origin,y-lift-1,lift),"a camp's own landing and the two turns below it are walked");
            FinaleProgress.phase(server,p.getUUID(),FinaleProgress.Phase.STAIRCASE);
            h.assertTrue(!StairCompany.holds(p,f.origin,y,lift),"a reader not on their way home is not held");
            // Their walk home over, nobody is left on the stair.
            StairCompany.sweep(server);
            h.assertTrue(StairCompany.bodies(p).isEmpty()&&there.get(0).isRemoved()&&there.get(1).isRemoved(),"a reader whose walk home has ended leaves nobody standing");
        });
    }
    /**
     * 0.4.654: a figure whose story could end more than one way, or gave its reader one of two people, stands for each reader as that
     * reader's own story had it, and asking does not change their record.
     */
    @GameTest(template="empty",batch="staircase_access",timeoutTicks=600)
    public static void aFigureIsDressedAsTheReadersOwnStoryLeftIt(GameTestHelper h){
        var f=new Fixture(h,599000);ACTIVE.add(f);int y=FinaleArchitecture.TOP-30;
        f.chunks.hold(f.level,new AABB(f.base.getX()-8,y-2,f.base.getZ()-8,f.base.getX()+9,y+6,f.base.getZ()+9));
        h.onEachTick(()->{
            if(f.finished||!f.chunks.ready())return;
            var bodies=new ArrayList<LiteraryActor>();
            try{
                var back=f.player(h,"went_back");var held=f.player(h,"held_in_the_wall");var nobody=f.player(h,"never_there");
                StairCompany.Figure lodger=null,rowan=null;
                for(var figure:StairCompany.FIGURES){if(figure.from(WitnessAccount.Story.ONES_INSIDE))lodger=figure;if(figure.from(WitnessAccount.Story.LITTLE_LONGER))rowan=figure;}
                h.assertTrue(lodger!=null&&rowan!=null&&lodger.dressed()!=null&&rowan.dressed()!=null,"the lodging house's and the radio shop's figures are dressed for each reader");
                WitnessAccount.resolve(f.data(),back.getUUID(),WitnessAccount.Story.ONES_INSIDE,"returned_daniel");
                WitnessAccount.resolve(f.data(),held.getUUID(),WitnessAccount.Story.ONES_INSIDE,"completed_dan");
                h.assertTrue(lodger.phaseFor(back)==LodgingPeople.FIRST+LodgingPeople.DAN,"where Dan went back into the frame, it is Dan on the stair");
                h.assertTrue(lodger.phaseFor(held)==LodgingPeople.FIRST+LodgingPeople.DANIEL&&lodger.phaseFor(nobody)==LodgingPeople.FIRST+LodgingPeople.DANIEL,"where Daniel was held in the wall, or for a reader with no end of it, it is Daniel");
                var place=LongerRowan.PLACE;var keptMan=new net.minecraft.nbt.CompoundTag();keptMan.putInt(LongerRowan.CAST,LongerRowan.FIRST+LongerRowan.MAN);
                LiteraryVignettes.save(f.data(),back.getUUID(),place,keptMan);
                var keptWoman=new net.minecraft.nbt.CompoundTag();keptWoman.putInt(LongerRowan.CAST,LongerRowan.FIRST+LongerRowan.WOMAN);
                LiteraryVignettes.save(f.data(),held.getUUID(),place,keptWoman);
                h.assertTrue(rowan.phaseFor(back)==LongerRowan.FIRST+LongerRowan.MAN&&rowan.phaseFor(held)==LongerRowan.FIRST+LongerRowan.WOMAN,"each reader's Rowan is the one they were given");
                int guess=rowan.phaseFor(nobody);
                h.assertTrue((guess==LongerRowan.FIRST+LongerRowan.MAN||guess==LongerRowan.FIRST+LongerRowan.WOMAN)&&!LiteraryVignettes.personal(f.data(),nobody.getUUID(),place).contains(LongerRowan.CAST),
                        "a reader who was never given one is shown one, and nothing is written into their record by asking");
                int n=0;
                for(var reader:new ServerPlayer[]{back,held})for(var figure:new StairCompany.Figure[]{lodger,rowan}){
                    var at=new Vec3(f.base.getX()-3.5+n%2*3,y,f.base.getZ()-3.5+n/2*3);n++;
                    var a=StairCompany.spawn(f.level,reader,figure,at,0);
                    h.assertTrue(a!=null&&a.phase()==figure.phaseFor(reader)&&a.role()==figure.role(),"set down for a reader, it stands in their own story's dress: "+figure.name());
                    bodies.add(a);
                }
                h.assertTrue(bodies.get(0).phase()!=bodies.get(2).phase()&&bodies.get(1).phase()!=bodies.get(3).phase(),"two readers whose stories went differently see different people");
                var standing=EnumSet.of(WitnessAccount.Story.HARRIGAN,WitnessAccount.Story.LAST_SWIMMER,WitnessAccount.Story.SLEEPING_KING,WitnessAccount.Story.ONES_INSIDE,WitnessAccount.Story.LITTLE_LONGER);
                int k=0;
                for(var figure:StairCompany.FIGURES){
                    boolean ours=false;for(var story:standing)ours|=figure.from(story);
                    if(!ours)continue;
                    var a=StairCompany.spawn(f.level,back,figure,new Vec3(f.base.getX()-4.5+k*2.2,y,f.base.getZ()+4.5),0);k++;
                    h.assertTrue(a!=null,"set down: "+figure.name());bodies.add(a);
                    h.assertTrue(a.getBbHeight()>=1.75F&&a.getEyeHeight()>=1.5F,
                            "on their feet with a standing body's height, so a look at the head is a look at them: "+figure.name()+" ("+a.getBbHeight()+" high, eyes at "+a.getEyeHeight()+")");
                }
                h.assertTrue(k==5,"the five stories given someone in 0.4.654 each have a figure on their feet: "+k);
                f.finished=true;h.succeed();
            }finally{if(f.finished){for(var a:bodies)a.discard();f.close();ACTIVE.remove(f);}}
        });
    }
    /**
     * 0.4.655: eight more stories' people are their own stories' creatures. Each stands on the stair as one of the cast's bodies in the
     * stand-in's role, at its creature's own size; the cousin wears the face the reader's own night gave it.
     */
    @GameTest(template="empty",batch="staircase_access",timeoutTicks=600)
    public static void theStoriesOwnCreaturesStandOnTheStairAtTheirOwnSize(GameTestHelper h){
        var f=new Fixture(h,655000);ACTIVE.add(f);int y=FinaleArchitecture.TOP-30;
        f.chunks.hold(f.level,new AABB(f.base.getX()-14,y-2,f.base.getZ()-8,f.base.getX()+15,y+6,f.base.getZ()+9));
        h.onEachTick(()->{
            if(f.finished||!f.chunks.ready())return;
            var bodies=new ArrayList<LiteraryActor>();
            try{
                var counted=f.player(h,"counted_right");var other=f.player(h,"none_kept");
                var creatures=new ArrayList<StairCompany.Figure>();int[] kinds=new int[StairStandIn.KINDS];var theirs=EnumSet.noneOf(WitnessAccount.Story.class);
                for(var figure:StairCompany.FIGURES)if(figure.role()==LiteraryActor.STAND_IN){creatures.add(figure);kinds[StairStandIn.kind(figure.phase())]++;theirs.addAll(List.of(figure.stories()));}
                for(int k=0;k<StairStandIn.KINDS;k++)h.assertTrue(kinds[k]==1,"each creature stands for one row of the list: kind "+k+" stands for "+kinds[k]);
                h.assertTrue(theirs.equals(EnumSet.of(WitnessAccount.Story.CLAP,WitnessAccount.Story.DROWNED_TOWN,WitnessAccount.Story.PRESERVED_CAVE,WitnessAccount.Story.SHALLOWS,
                        WitnessAccount.Story.GOATMAN,WitnessAccount.Story.SEANCE,WitnessAccount.Story.HOTEL,WitnessAccount.Story.ELK_LOT,WitnessAccount.Story.ELK_FAN)),
                        "the creatures stand for the stories whose people were some other kind of body than the cast's: "+theirs);
                h.assertTrue(StairCompany.NOT_YET.equals(EnumSet.of(WitnessAccount.Story.BACKSEAT,WitnessAccount.Story.DARE_IN_THE_DARK,WitnessAccount.Story.WORK_PRINT,
                        WitnessAccount.Story.LEFT_RIGHT,WitnessAccount.Story.DEAD_AIR)),"only the five drawn as blocks or in a mirror have nobody yet: "+StairCompany.NOT_YET);
                // The cousin: the face the one too many wore the night the reader counted right; for a reader with none kept, one of the
                // faces the cousins are dealt, the same one each time, and nothing written by asking.
                StairCompany.Figure cousin=null;for(var figure:creatures)if(figure.from(WitnessAccount.Story.GOATMAN))cousin=figure;
                h.assertTrue(cousin!=null&&cousin.dressed()!=null,"the cousin is dressed for each reader");
                var all=f.data().state(GoatmanVignette.ID);var players=all.getCompound("Players");var own=new CompoundTag();own.putInt(GoatmanVignette.EXTRA_FACE,6);
                players.put(counted.getUUID().toString(),own);all.put("Players",players);f.data().setState(GoatmanVignette.ID,all);
                h.assertTrue(cousin.phaseFor(counted)==StairStandIn.phase(StairStandIn.COUSIN,6),"a reader who counted right is shown the face the one too many wore that night");
                int guess=cousin.phaseFor(other);
                h.assertTrue(StairStandIn.kind(guess)==StairStandIn.COUSIN&&java.util.stream.IntStream.of(StairStandIn.COUSIN_FACES).anyMatch(face->face==StairStandIn.dress(guess))&&cousin.phaseFor(other)==guess,
                        "a reader with none kept is shown one of the faces the cousins are dealt, the same one each time: "+StairStandIn.dress(guess));
                h.assertTrue(!GoatmanVignette.personal(f.data(),other.getUUID()).contains(GoatmanVignette.EXTRA_FACE),"and nothing is written into their record by asking");
                // Each body is its creature's own size: the hunting witch low, the cousin a child's, the elk broad, the girl from the
                // wardrobe small, and the preacher, the girl before the lake, the mother and the bartender a person's.
                h.assertTrue(StairStandIn.size(StairStandIn.phase(StairStandIn.STACEY,0)).height()<1&&Math.abs(StairStandIn.size(StairStandIn.phase(StairStandIn.COUSIN,6)).height()-1.8F*.7F)<1e-3
                        &&StairStandIn.size(StairStandIn.phase(StairStandIn.ELK,0)).width()>1.2F&&StairStandIn.size(StairStandIn.phase(StairStandIn.GIRL,0)).height()<1.5F,"the creatures' bodies are their own stories'");
                for(int kind:new int[]{StairStandIn.PREACHER,StairStandIn.BEFORE_THE_LAKE,StairStandIn.MOTHER,StairStandIn.BARTENDER})
                    h.assertTrue(StairStandIn.size(StairStandIn.phase(kind,0)).height()>=1.75F,"a person's height: kind "+kind);
                // Set down, each is that size, its reader's alone, and nobody to push, hurt or talk to.
                int n=0;
                for(var figure:creatures){
                    var at=new Vec3(f.base.getX()-10.5+n*3,y,f.base.getZ()+.5);n++;
                    var a=StairCompany.spawn(f.level,counted,figure,at,0);
                    h.assertTrue(a!=null,"set down: "+figure.name());bodies.add(a);
                    var size=StairStandIn.size(figure.phaseFor(counted));
                    h.assertTrue(a.role()==LiteraryActor.STAND_IN&&a.phase()==figure.phaseFor(counted),"it stands as its row says, in its reader's dress: "+figure.name());
                    h.assertTrue(Math.abs(a.getBbHeight()-size.height())<1e-4&&Math.abs(a.getBbWidth()-size.width())<1e-4&&Math.abs(a.getEyeHeight()-size.eyeHeight())<1e-4&&Math.abs(figure.height()-size.height())<1e-4,
                            "its body is its creature's own size, set down or not yet: "+figure.name()+" ("+a.getBbWidth()+" by "+a.getBbHeight()+", eyes at "+a.getEyeHeight()+")");
                    h.assertTrue(a.owner().filter(counted.getUUID()::equals).isPresent()&&a.broadcastToPlayer(counted)&&!a.broadcastToPlayer(other)&&!a.shouldBeSaved()&&!a.isPushable()
                            &&!a.hurt(counted.damageSources().playerAttack(counted),8)&&a.isAlive()&&!a.addresses()&&!a.crawling()&&!a.reaching(),"its reader's alone, nobody to push, hurt or talk to, and drawn as its creature only: "+figure.name());
                }
                h.assertTrue(n==8,"eight creatures stand on the stair: "+n);
                f.finished=true;h.succeed();
            }finally{if(f.finished){for(var a:bodies)a.discard();f.close();ACTIVE.remove(f);}}
        });
    }
    /** Every figure on the list can be set down, and stays as it was set down while the server runs: nothing of its story moves it. */
    @GameTest(template="empty",batch="staircase_access",timeoutTicks=1200)
    public static void everyFigureKeepsItsPlaceWhileTheServerRuns(GameTestHelper h){
        var f=new Fixture(h,598000);ACTIVE.add(f);int y=FinaleArchitecture.TOP-30;
        f.chunks.hold(f.level,new AABB(f.base.getX()-16,y-2,f.base.getZ()-16,f.base.getX()+17,y+6,f.base.getZ()+17));
        var bodies=new ArrayList<LiteraryActor>();var places=new ArrayList<Vec3>();var dress=new ArrayList<Integer>();int[] waited={-1};
        h.onEachTick(()->{
            if(f.finished||!f.chunks.ready())return;boolean last=false;
            try{
                if(waited[0]<0){
                    var p=f.player(h,"keeper");
                    for(int i=0;i<StairCompany.FIGURES.size();i++){
                        var at=new Vec3(f.base.getX()-12.5+i%10*2.7,y,f.base.getZ()-12.5+i/10*2.7);var figure=StairCompany.FIGURES.get(i);
                        var a=StairCompany.spawn(f.level,p,figure,at,i*37%360-180);int phase=figure.phaseFor(p);
                        h.assertTrue(a!=null&&a.role()==figure.role()&&a.phase()==phase&&a.gesture()==figure.gesture(),"each figure on the list can be set down as its row says: "+figure.name());
                        h.assertTrue(ItemStack.matches(a.getMainHandItem(),figure.held())&&a.getBbHeight()>.3,"holding what its row gives it, with a body to stand in: "+figure.name());
                        h.assertTrue(figure.scale()==0||a.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.SCALE)==figure.scale(),"and the size its row gives it: "+figure.name());
                        bodies.add(a);places.add(at);dress.add(phase);
                    }
                    waited[0]=0;return;
                }
                if(++waited[0]<40)return;
                last=true;
                for(int i=0;i<bodies.size();i++){
                    var a=bodies.get(i);var figure=StairCompany.FIGURES.get(i);
                    h.assertTrue(a.isAlive()&&!a.isRemoved()&&a.position().distanceTo(places.get(i))<.001,"forty ticks on, each stands where it was set down: "+figure.name()+" at "+a.position()+", set at "+places.get(i));
                    h.assertTrue(Math.abs(net.minecraft.util.Mth.wrapDegrees(a.getYRot()-(i*37%360-180)))<1&&a.role()==figure.role()&&a.phase()==dress.get(i)&&a.gesture()==figure.gesture()&&a.fade()==0,
                            "turned as it was turned and dressed as it was dressed, whatever its story does with it elsewhere: "+figure.name()+" (phase "+a.phase()+", gesture "+a.gesture()+", fade "+a.fade()+", yaw "+a.getYRot()+")");
                    h.assertTrue(a.company()&&!a.shouldBeSaved()&&a.getDeltaMovement().lengthSqr()<1e-6,"still one of the stair's people, with nothing saved of it, and not on its way anywhere: "+figure.name());
                    h.assertTrue(figure.scale()==0||a.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.SCALE)==figure.scale(),"and still the size its row gives it: "+figure.name());
                }
            }catch(RuntimeException|Error e){last=true;throw e;}
            finally{if(last){f.finished=true;for(var a:bodies)a.discard();f.close();ACTIVE.remove(f);}}
            h.succeed();
        });
    }
}
