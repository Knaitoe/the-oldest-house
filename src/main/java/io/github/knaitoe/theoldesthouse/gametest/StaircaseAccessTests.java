package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.Entity;
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
    private static final class Fixture implements AutoCloseable {
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
            for(var p:players)NativeTestPlayers.remove(p);players.clear();
            var remove=new ArrayList<Entity>();for(var e:level.getAllEntities())if(FinaleArchitecture.contains(origin,e.blockPosition())&&(e instanceof NovelActor||e instanceof FinaleWitness))remove.add(e);
            for(var e:remove)e.discard();
            for(var entry:touched.entrySet())for(var at:entry.getValue())entry.getKey().setBlock(at,Blocks.AIR.defaultBlockState(),F);
            chunks.close();if(started){var storage=level.getServer().overworld().getDataStorage();storage.set("the_oldest_house",oldHouse);storage.set("the_oldest_house_labyrinth",oldData);started=false;}
        }
    }
    private static void run(GameTestHelper h,int coordinate,Consumer<Fixture> setup,Consumer<Fixture> test){
        var f=new Fixture(h,coordinate);ACTIVE.add(f);setup.accept(f);
        h.onEachTick(()->{if(f.finished||!f.chunks.ready())return;f.finished=true;
            try{f.start();test.accept(f);h.succeed();}finally{f.close();ACTIVE.remove(f);}});
    }
    private static void run(GameTestHelper h,int coordinate,Consumer<Fixture> test){run(h,coordinate,f->{},test);}
    @AfterBatch(batch="staircase_access") public static void cleanup(ServerLevel level){for(var f:ACTIVE)f.close();ACTIVE.clear();}

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
    public static void alreadyPolishedWorldReceivesTargetedRepairsOnceWithoutRestaging(GameTestHelper h){run(h,514000,f->{
        var b=LabyrinthPlaces.base(f.origin,LabyrinthPlace.END_WORLD_CABIN);var r=LabyrinthPlace.END_WORLD_CABIN.room();
        f.chunks.hold(f.outside,new AABB(b.getX()+r.minX()-14,b.getY()+r.minY()-2,b.getZ()+r.minZ()-14,b.getX()+r.maxX()+15,b.getY()+r.maxY()+3,b.getZ()+r.maxZ()+15));
    },f->{
        var place=LabyrinthPlace.END_WORLD_CABIN;var b=LabyrinthPlaces.base(f.origin,place);var cabinet=b.offset(7,0,-24);var tv=cabinet.above();var barrel=b.offset(-8,0,-25);var removed=b.offset(6,0,-29);
        f.put(f.outside,tv,LiteraryRegistry.PROP.get().defaultBlockState().setValue(LiteraryPropBlock.KIND,LiteraryPropBlock.Kind.TELEVISION));
        f.put(f.outside,cabinet,Blocks.AIR.defaultBlockState());f.put(f.outside,barrel,Blocks.BARREL.defaultBlockState());f.put(f.outside,removed,Blocks.AIR.defaultBlockState());
        var original=new ItemStack(Items.PAPER,3);original.set(DataComponents.CUSTOM_NAME,Component.literal("Player's kept notes"));((BarrelBlockEntity)f.outside.getBlockEntity(barrel)).setItem(0,original.copy());
        var originalTv=f.outside.getBlockEntity(tv);var originalBarrel=f.outside.getBlockEntity(barrel);String key=f.origin.asLong()+":"+place.id();var done=new CompoundTag();done.putBoolean(key,true);f.data().setState(ScenePolish.STATE,done);
        ScenePolish.polishOnce(f.outside,f.origin,place);
        h.assertTrue(f.outside.getBlockState(cabinet).is(HouseBlocks.HOUSEHOLD_FURNITURE.get())&&f.data().state(ScenePolish.REPAIR_STATE).getBoolean(key),"a legacy polished scene receives the previously skipped cabinet correction");
        h.assertTrue(f.outside.getBlockEntity(tv)==originalTv&&f.outside.getBlockEntity(barrel)==originalBarrel&&ItemStack.isSameItemSameComponents(original,((BarrelBlockEntity)originalBarrel).getItem(0))&&f.outside.getBlockState(removed).isAir(),"the upgrade retains original entities and player property without rebuilding removed scenery");
        f.reload();f.outside.setBlock(cabinet,Blocks.AIR.defaultBlockState(),F);ScenePolish.polishOnce(f.outside,f.origin,place);
        h.assertTrue(f.outside.getBlockState(cabinet).isAir()&&f.data().state(ScenePolish.REPAIR_STATE).getBoolean(key),"the saved repair checkpoint prevents later restaging");
    });}
}
