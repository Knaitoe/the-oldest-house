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
    public static void oneLeafLiesOnALevelTreadOfEachFlightBeyondThePreviousFiresReach(GameTestHelper h){run(h,515000,f->{
        var route=FinaleArchitecture.staircaseRoute(f.origin);var leaves=StaircaseLeaves.positions(f.origin);var fires=StaircaseFire.braziers(f.origin);
        h.assertTrue(leaves.size()==StaircaseFire.REQUIRED&&new HashSet<>(leaves).size()==leaves.size(),"five distinct leaves, one per hearth");
        for(int k=0;k<leaves.size();k++){
            var leaf=leaves.get(k);int step=-1;
            for(int i=0;i+1<route.size();i++)if(route.get(i).getY()==leaf.getY()&&route.get(i).distManhattan(leaf)==3){step=i;break;}
            h.assertTrue(step>=0&&route.get(step+1).getY()==route.get(step).getY(),"leaf "+k+" lies three blocks aside on a level, full tread");
            h.assertTrue(!fires.contains(leaf)&&!StaircaseFire.landings(f.origin).contains(leaf),"leaf "+k+" lies on the flight, not at a hearth");
            var lit=new CompoundTag();lit.putInt("StairFires",k);
            h.assertTrue(leaf.getY()>=StaircaseFire.edge(f.origin,lit).getY()-.75,"leaf "+k+" is reachable once "+k+" fires burn");
            if(k>0){var before=new CompoundTag();before.putInt("StairFires",k-1);
                h.assertTrue(leaf.getY()<StaircaseFire.edge(f.origin,before).getY()-.75,"leaf "+k+" lies beyond the reach of the fire before it");}
            int landing=route.indexOf(StaircaseFire.landings(f.origin).get(k));
            h.assertTrue(step<landing,"leaf "+k+" lies above its own hearth's landing");
        }
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

    @GameTest(template="empty",batch="staircase_access",timeoutTicks=1200)
    public static void alreadyComposedRoofsAndBarnReceiveOnlyTheirMissedRepairsOnce(GameTestHelper h){run(h,516000,f->{
        for(var place:List.of(LabyrinthPlace.ELK_LOT,LabyrinthPlace.MAPPING_INTERIOR,LabyrinthPlace.CAMP_BLOOD,LabyrinthPlace.END_WORLD_CABIN,LabyrinthPlace.BARN_WELL))holdScene(f,place);
    },f->{
        var places=List.of(LabyrinthPlace.ELK_LOT,LabyrinthPlace.MAPPING_INTERIOR,LabyrinthPlace.CAMP_BLOOD,LabyrinthPlace.END_WORLD_CABIN,LabyrinthPlace.BARN_WELL);
        for(var place:places)composed(f,place);
        oldRoof(f,LabyrinthPlaces.base(f.origin,LabyrinthPlace.ELK_LOT),-23,-7,-24,-5,6);
        oldRoof(f,LabyrinthPlaces.base(f.origin,LabyrinthPlace.MAPPING_INTERIOR),-13,13,-36,-8,5);
        var camp=LabyrinthPlaces.base(f.origin,LabyrinthPlace.CAMP_BLOOD);
        for(int x:new int[]{-20,20})for(int z:new int[]{-26,-57,-83})oldRoof(f,camp,x-8,x+8,z-7,z+7,5);
        var cabin=LabyrinthPlaces.base(f.origin,LabyrinthPlace.END_WORLD_CABIN);oldRoof(f,cabin,-12,12,-35,-13,6);
        var barn=LabyrinthPlaces.base(f.origin,LabyrinthPlace.BARN_WELL);track(f,f.outside,barn.offset(4,3,-35),barn.offset(16,10,-15));
        for(int x=4;x<=16;x++)for(int z=-35;z<=-16;z++)f.put(f.outside,barn.offset(x,4+Math.min(x-4,16-x)/2,z),Blocks.SPRUCE_STAIRS.defaultBlockState());
        for(int x=5;x<=15;x++)for(int z=-34;z<=-17;z++)f.put(f.outside,barn.offset(x,7,z),Blocks.BIRCH_PLANKS.defaultBlockState());
        var hanger=barn.offset(6,6,-20);f.put(f.outside,hanger,Blocks.CHAIN.defaultBlockState());
        var custom=cabin.offset(-13,6,-25);f.put(f.outside,custom,Blocks.GOLD_BLOCK.defaultBlockState());
        var cache=cabin.offset(-8,0,-25);f.put(f.outside,cache,Blocks.BARREL.defaultBlockState());var inventory=(BarrelBlockEntity)f.outside.getBlockEntity(cache);
        var notes=new ItemStack(Items.PAPER,3);notes.set(DataComponents.CUSTOM_NAME,Component.literal("Kept personal leaves"));inventory.setItem(7,notes.copy());
        var absent=cabin.offset(6,0,-29);f.put(f.outside,absent,Blocks.AIR.defaultBlockState());
        var p=f.player(h,"roof_visitor");p.teleportTo(f.outside,cabin.getX()+.5,cabin.getY()+1,cabin.getZ()-24.5,0,0);
        SceneCraft.craftOnce(f.outside,f.origin,LabyrinthPlace.END_WORLD_CABIN);
        String cabinKey=f.origin.asLong()+":"+LabyrinthPlace.END_WORLD_CABIN.id();
        h.assertTrue(!f.data().state(SceneCraft.REPAIR_STATE).getBoolean(cabinKey)&&f.outside.getBlockState(cabin.offset(-13,6,-30)).is(Blocks.SPRUCE_STAIRS),"an occupied saved composition waits for its structural repair");
        var pet=EntityType.WOLF.create(f.outside);h.assertTrue(pet!=null,"a native roof companion exists");pet.tame(p);pet.setNoAi(true);pet.setNoGravity(true);
        pet.moveTo(Vec3.atBottomCenterOf(cabin.offset(-13,7,-30)));CompanionOrders.issue(pet,p,CompanionOrders.Order.STAY);f.outside.addFreshEntity(pet);
        try{
            p.teleportTo(f.level,f.source.getX()+.5,f.source.getY(),f.source.getZ()+.5,0,0);SceneCraft.craftOnce(f.outside,f.origin,LabyrinthPlace.END_WORLD_CABIN);
            h.assertTrue(!f.data().state(SceneCraft.REPAIR_STATE).getBoolean(cabinKey)&&f.outside.getBlockState(cabin.offset(-13,6,-30)).is(Blocks.SPRUCE_STAIRS),"a Stay companion on the old roof keeps its supporting slope until it leaves");
            pet.moveTo(Vec3.atBottomCenterOf(cabin.offset(0,0,-25)));
            for(var place:places)SceneCraft.craftOnce(f.outside,f.origin,place);
            for(var place:places)h.assertTrue(SceneCraft.done(f.level.getServer(),f.origin,place)&&f.data().state(SceneCraft.REPAIR_STATE).getBoolean(f.origin.asLong()+":"+place.id()),"the existing "+place.id()+" completion receives a separate repair checkpoint");
            h.assertTrue(f.outside.getBlockState(cabin.offset(-13,6,-30)).is(Blocks.DARK_OAK_SLAB)&&f.outside.getBlockState(custom).is(Blocks.GOLD_BLOCK),"the old cabin slope changes while customized roof cells remain");
            h.assertTrue(f.outside.getBlockState(LabyrinthPlaces.base(f.origin,LabyrinthPlace.ELK_LOT).offset(-24,6,-20)).is(Blocks.DARK_OAK_SLAB)
                    &&f.outside.getBlockState(LabyrinthPlaces.base(f.origin,LabyrinthPlace.MAPPING_INTERIOR).offset(-14,5,-20)).is(Blocks.SPRUCE_SLAB),"the bar and lodge receive their missed half-pitch roofs");
            for(int x:new int[]{-20,20})for(int z:new int[]{-26,-57,-83})h.assertTrue(f.outside.getBlockState(camp.offset(x-9,5,z)).getBlock() instanceof SlabBlock,"each of the six cabins receives its missed roof");
            h.assertTrue(f.outside.getBlockState(barn.offset(10,8,-25)).is(Blocks.DARK_OAK_LOG)&&!f.outside.getBlockState(barn.offset(8,7,-25)).is(Blocks.BIRCH_PLANKS)
                    &&f.outside.getBlockState(hanger.above()).is(Blocks.DARK_OAK_PLANKS)&&f.outside.getBlockState(hanger).is(Blocks.CHAIN),"the barn loses its lid, gains the gambrel and retains hanging supports");
            h.assertTrue(f.outside.getBlockEntity(cache)==inventory&&ItemStack.isSameItemSameComponents(notes,inventory.getItem(7))&&inventory.getItem(0).isEmpty()&&f.outside.getBlockState(absent).isAir(),"the targeted repair retains exact finite property and never repeats the scene's furnishings");
            var removed=cabin.offset(-13,6,-30);f.outside.setBlock(removed,Blocks.AIR.defaultBlockState(),F);f.reload();SceneCraft.craftOnce(f.outside,f.origin,LabyrinthPlace.END_WORLD_CABIN);
            h.assertTrue(f.outside.getBlockState(removed).isAir(),"the saved structural repair never replenishes a roof the player removed later");
            var repaired=f.data().state(SceneCraft.REPAIR_STATE);repaired.remove(cabinKey);f.data().setState(SceneCraft.REPAIR_STATE,repaired);SceneCraft.craftOnce(f.outside,f.origin,LabyrinthPlace.END_WORLD_CABIN);
            h.assertTrue(f.outside.getBlockState(removed).isAir(),"a latest 0.4.46 roof without the new checkpoint is recognized and left alone");
            // The intermediate build already had its gambrel, but still kept the birch lid.
            String barnKey=f.origin.asLong()+":"+LabyrinthPlace.BARN_WELL.id();repaired=f.data().state(SceneCraft.REPAIR_STATE);repaired.remove(barnKey);f.data().setState(SceneCraft.REPAIR_STATE,repaired);
            for(int x=5;x<=15;x++)for(int z=-34;z<=-17;z++)f.outside.setBlock(barn.offset(x,7,z),Blocks.BIRCH_PLANKS.defaultBlockState(),F);
            var lid=barn.offset(7,7,-25);SceneCraft.craftOnce(f.outside,f.origin,LabyrinthPlace.BARN_WELL);
            h.assertTrue(f.outside.getBlockState(lid).isAir()&&f.outside.getBlockState(barn.offset(8,7,-25)).is(Blocks.DARK_OAK_SLAB)
                    &&f.outside.getBlockState(barn.offset(10,8,-25)).is(Blocks.DARK_OAK_LOG),"the intermediate gambrel loses its old lid and fills only the roof cells that lid had blocked");
            SceneCraft.forget(f.level.getServer(),f.origin,LabyrinthPlace.BARN_WELL);
            h.assertTrue(!SceneCraft.done(f.level.getServer(),f.origin,LabyrinthPlace.BARN_WELL)&&!f.data().state(SceneCraft.REPAIR_STATE).getBoolean(barnKey),"an explicit rebuild clears both checkpoints");
        }finally{pet.discard();}
    });}

    @GameTest(template="empty",batch="staircase_access",timeoutTicks=1200)
    public static void alreadyComposedGoatmanThicketRepairsOnceAndKeepsNativeCompanionsAndTheTrail(GameTestHelper h){run(h,516500,f->holdScene(f,LabyrinthPlace.GOATMAN),f->{
        var place=LabyrinthPlace.GOATMAN;var b=LabyrinthPlaces.base(f.origin,place);composed(f,place);track(f,f.level,b.offset(-22,-1,-81),b.offset(22,12,-1));
        var leaves=Blocks.DARK_OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true);BlockPos root=null;
        for(int x=-22;x<=22;x++)for(int z=-81;z<=-1;z++){
            var at=new Vec3(x+.5,0,z+.5);boolean trail=GoatmanWoods.clearing(at)||GoatmanWoods.project(at).distance()<1.6;
            f.put(f.level,b.offset(x,-1,z),(trail?Blocks.DIRT_PATH:Blocks.GRASS_BLOCK).defaultBlockState());
            if(trail)continue;
            for(int y=0;y<=3;y++)f.put(f.level,b.offset(x,y,z),leaves);
            if(Math.floorMod(x*17+z*13,7)==0){for(int y=0;y<=8;y++)f.put(f.level,b.offset(x,y,z),Blocks.DARK_OAK_LOG.defaultBlockState());if(root==null)root=b.offset(x,9,z);}
        }
        h.assertTrue(root!=null,"the legacy forest has its original lattice trunks");var p=f.player(h,"thicket_owner");p.teleportTo(f.level,f.source.getX()+.5,f.source.getY(),f.source.getZ()+.5,0,0);
        var pet=EntityType.WOLF.create(f.level);h.assertTrue(pet!=null,"a native forest companion exists");pet.tame(p);pet.setNoAi(true);pet.setNoGravity(true);pet.setHealth(9);pet.moveTo(Vec3.atBottomCenterOf(root));
        CompanionOrders.issue(pet,p,CompanionOrders.Order.STAY);f.level.addFreshEntity(pet);var id=pet.getUUID();String key=f.origin.asLong()+":"+place.id();
        try{
            SceneCraft.craftOnce(f.level,f.origin,place);
            h.assertTrue(!f.data().state(SceneCraft.REPAIR_STATE).getBoolean(key)&&f.level.getBlockState(root.below()).is(Blocks.DARK_OAK_LOG),"a native companion above the thicket defers its repair");
            pet.moveTo(Vec3.atBottomCenterOf(b.offset(0,0,-20)));var position=pet.position();SceneCraft.craftOnce(f.level,f.origin,place);
            h.assertTrue(f.data().state(SceneCraft.REPAIR_STATE).getBoolean(key),"the old completed composition receives its missed thicket");
            int mixed=0;BlockPos removable=null;
            for(var at:BlockPos.betweenClosed(b.offset(-22,0,-81),b.offset(22,0,-1))){var s=f.level.getBlockState(at);if(s.is(net.minecraft.tags.BlockTags.LEAVES)&&!s.is(Blocks.DARK_OAK_LEAVES))mixed++;}
            for(var at:BlockPos.betweenClosed(b.offset(-22,4,-81),b.offset(22,12,-1)))if(f.level.getBlockState(at).is(net.minecraft.tags.BlockTags.LOGS))removable=at.immutable();
            h.assertTrue(mixed>=16&&removable!=null,"the flat leaf wall and lattice become mixed woods");
            for(double distance=0;distance<83;distance+=.5){var feet=GoatmanWoods.path(distance).add(b.getX(),b.getY(),b.getZ());
                h.assertTrue(f.level.noCollision(null,new AABB(feet.x-.3,feet.y+.01,feet.z-.3,feet.x+.3,feet.y+1.8,feet.z+.3)),"the repaired trail clears a native player body at "+distance);}
            h.assertTrue(f.level.getEntity(id)==pet&&pet.getHealth()==9&&pet.position().equals(position)&&CompanionOrders.order(pet)==CompanionOrders.Order.STAY,"a parked companion on the safe trail keeps its identity, health, position and order");
            f.level.setBlock(removable,Blocks.AIR.defaultBlockState(),F);f.reload();SceneCraft.craftOnce(f.level,f.origin,place);h.assertTrue(f.level.getBlockState(removable).isAir(),"the saved thicket repair cannot grow a removed tree again");
            var repaired=f.data().state(SceneCraft.REPAIR_STATE);repaired.remove(key);f.data().setState(SceneCraft.REPAIR_STATE,repaired);
            var snapshot=new HashMap<BlockPos,BlockState>();for(var at:BlockPos.betweenClosed(b.offset(-22,0,-81),b.offset(22,12,-1)))snapshot.put(at.immutable(),f.level.getBlockState(at));
            SceneCraft.craftOnce(f.level,f.origin,place);
            h.assertTrue(snapshot.entrySet().stream().allMatch(e->f.level.getBlockState(e.getKey()).equals(e.getValue())),"a latest 0.4.46 thicket without the new checkpoint is recognized without restaging");
        }finally{pet.discard();}
    });}
}
