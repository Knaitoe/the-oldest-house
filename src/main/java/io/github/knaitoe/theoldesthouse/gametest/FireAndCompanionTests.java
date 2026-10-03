package io.github.knaitoe.theoldesthouse.gametest;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import io.github.knaitoe.theoldesthouse.opening.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FireAndCompanionTests {
    private static final List<Entity> ROUTE_ENTITIES=new ArrayList<>();
    private static ServerLevel routeLevel;private static BlockPos routeTicket;
    private static void remove(ServerPlayer p){NativeTestPlayers.remove(p);}
    @AfterBatch(batch="companion_route") public static void cleanRoute(ServerLevel level){for(var e:ROUTE_ENTITIES){if(e instanceof ServerPlayer p)remove(p);else e.discard();}ROUTE_ENTITIES.clear();if(routeLevel!=null){routeLevel.getChunkSource().removeRegionTicket(TicketType.PORTAL,new net.minecraft.world.level.ChunkPos(routeTicket),3,routeTicket);routeLevel=null;}}
    @GameTest(template="empty",batch="companion_route",timeoutTicks=300)
    public static void nativeGuideActuallyWalksPastStoppingRadiusAndAroundBend(GameTestHelper h){
        var owner=NativeTestPlayers.survival(h,"walking_guide_owner");var level=HouseTestLevel.get(owner.server,HouseDimensions.INTERIOR);
        BlockPos base=new BlockPos(h.absolutePos(BlockPos.ZERO).getX()+240000,180,240000);var floor=LabyrinthHalls.floor(LabyrinthPlace.BENT_HALL);
        // getChunkAt registers geometry; a portal ticket makes this remote native entity area tick.
        routeLevel=level;routeTicket=base.offset(-8,0,-15);level.getChunkSource().addRegionTicket(TicketType.PORTAL,new net.minecraft.world.level.ChunkPos(routeTicket),3,routeTicket);
        for(var local:floor){var at=base.offset(local);level.getChunkAt(at);level.setBlock(at.below(),Blocks.STONE.defaultBlockState(),3);for(int y=0;y<3;y++)level.setBlock(at.above(y),Blocks.AIR.defaultBlockState(),3);}
        // Real walls prevent native navigation from cutting across the empty corner.
        for(var local:floor)for(var d:Direction.Plane.HORIZONTAL)if(!floor.contains(local.relative(d)))for(int y=0;y<3;y++)level.setBlock(base.offset(local.relative(d)).above(y),Blocks.STONE.defaultBlockState(),3);
        owner.teleportTo(level,base.getX()+.5,base.getY(),base.getZ()+.5,0,0);
        var dog=EntityType.WOLF.create(level);dog.tame(owner);dog.moveTo(Vec3.atBottomCenterOf(base));level.addFreshEntity(dog);ROUTE_ENTITIES.add(owner);ROUTE_ENTITIES.add(dog);
        BlockPos goal=base.offset(-15,0,-30);
        h.runAtTickTime(280,()->h.assertTrue(dog.getX()<base.getX()-8&&dog.getZ()<base.getZ()-17,
                "native route did not advance: ticks="+dog.tickCount+", position="+dog.position()+", base="+base+", noAi="+dog.isNoAi()+", navigationDone="+dog.getNavigation().isDone()));
        h.onEachTick(()->{
            if(level.getEntity(dog.getUUID())!=dog)return;
            owner.moveTo(dog.position().add(0,0,2));HillaryPaths.lead(dog,owner,goal,LabyrinthPlace.BENT_HALL,base);
            if(dog.getX()<base.getX()-8&&dog.getZ()<base.getZ()-17){
                h.assertTrue(dog.getY()>base.getY()-1&&dog.getHealth()>0,"the native guide walked on the connected floor around the bend");
                dog.discard();remove(owner);h.succeed();
            }
        });
    }
    @GameTest(template="empty",batch="stair_fire",timeoutTicks=160)
    public static void fiveNativeFiresConsumeBookAndUnlockOnlyTheirReader(GameTestHelper h){
        var p=NativeTestPlayers.survival(h,"stair_fire_reader");var peer=h.makeMockServerPlayerInLevel();var level=HouseTestLevel.get(p.server,HouseDimensions.INTERIOR);
        BlockPos origin=new BlockPos(h.absolutePos(BlockPos.ZERO).getX()+220000,0,220000);
        var old=FinaleProgress.world(p.server);var fires=StaircaseFire.braziers(origin);
        try{
            p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
            p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.FLINT_AND_STEEL));
            p.setItemInHand(InteractionHand.OFF_HAND,StaircaseFire.book(p.getUUID(),5));
            CompoundTag record=new CompoundTag();record.putString("Phase",FinaleProgress.Phase.STAIRCASE.name());record.putBoolean("StairFireVersion",true);FinaleProgress.save(p.server,p.getUUID(),record);
            for(int i=0;i<5;i++){
                var at=fires.get(i);level.getChunkAt(at);level.setBlock(at.below(),Blocks.STONE.defaultBlockState(),3);level.setBlock(at,Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT,false),3);
                p.teleportTo(level,at.getX()+1.5,at.getY(),at.getZ()+.5,0,0);
                h.assertTrue(StaircaseFire.ignite(p,origin,at),"the actual hearth accepts its ordered leaf "+i);
                h.assertTrue(level.getBlockState(at).getValue(CampfireBlock.LIT),"a real native fire lights");
                h.assertTrue(StaircaseFire.flames(FinaleProgress.player(p.server,p.getUUID()))==i+1,"the reader advances once");
                h.assertTrue(!StaircaseFire.ignite(p,origin,at),"the same fire cannot consume another leaf or count twice");
            }
            h.assertTrue(p.getOffhandItem().isEmpty()&&p.getMainHandItem().getDamageValue()==5,"the fifth leaf consumes the book and five uses of the striker");
            var data=LabyrinthData.get(p.server);var loaded=LabyrinthData.FACTORY.deserializer().apply(data.save(new CompoundTag(),level.registryAccess()),level.registryAccess());
            h.assertTrue(StaircaseFire.open(loaded.state(FinaleProgress.STATE).getCompound(p.getUUID().toString()))
                    &&!StaircaseFire.open(loaded.state(FinaleProgress.STATE).getCompound(peer.getUUID().toString())),"reload preserves personal light; a peer cannot borrow it");
            h.assertTrue(StaircaseFire.take(p)&&!StaircaseFire.take(p),"the native personal supply is finite");
        }finally{LabyrinthData.get(p.server).setState(FinaleProgress.STATE,old);for(var at:fires){level.setBlock(at,Blocks.AIR.defaultBlockState(),3);level.setBlock(at.below(),Blocks.AIR.defaultBlockState(),3);}remove(p);remove(peer);}
        h.succeed();
    }
    @GameTest(template="empty",batch="stair_fire",timeoutTicks=160)
    public static void unlitDepthStopsAdvanceAndOldDeepVisitsRemainOpen(GameTestHelper h){
        var p=NativeTestPlayers.survival(h,"stair_dark_walker");var level=HouseTestLevel.get(p.server,HouseDimensions.INTERIOR);
        BlockPos origin=new BlockPos(h.absolutePos(BlockPos.ZERO).getX()+230000,0,230000);
        var record=new CompoundTag();StaircaseFire.initialize(record,FinaleArchitecture.TOP);
        var safe=StaircaseFire.landings(origin).getFirst();var edge=StaircaseFire.edge(origin,record);
        level.getChunkAt(safe);level.setBlock(safe.below(),Blocks.STONE.defaultBlockState(),3);
        try{
            p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.teleportTo(level,edge.getX()+.5,edge.getY()-2,edge.getZ()+.5,0,0);
            boolean blocked=StaircaseFire.tick(p,origin,record);
            h.assertTrue(blocked,"the native survival boundary blocks: creative="+p.isCreative()+", fires="+StaircaseFire.flames(record)+", edge="+edge+", position="+p.position());
            h.assertTrue(p.position().distanceToSqr(Vec3.atBottomCenterOf(safe))<1,"native movement is returned safely: position="+p.position()+", safe="+safe);
            p.moveTo(safe.getX()+.5,FinaleArchitecture.TOP,safe.getZ()+.5);
            h.assertTrue(!StaircaseFire.tick(p,origin,record),"movement back toward the entry remains available");
            record.putInt("StairFires",5);p.moveTo(safe.getX()+.5,FinaleArchitecture.ARENA,safe.getZ()+.5);
            h.assertTrue(!StaircaseFire.tick(p,origin,record),"five fires allow the full physical descent");
            var old=new CompoundTag();StaircaseFire.initialize(old,FinaleArchitecture.ARENA);
            h.assertTrue(StaircaseFire.open(old),"an existing deep visit is not trapped by the upgrade");
            h.assertTrue(!WitnessAccount.ready(LabyrinthData.get(p.server),p.getUUID()),"lighting is no additional Witness source");
        }finally{level.setBlock(safe.below(),Blocks.AIR.defaultBlockState(),3);remove(p);}
        h.succeed();
    }
    @GameTest(template="empty",batch="companion_doors",timeoutTicks=160)
    public static void nativeThresholdCarriesAheadGuideButLeavesStayAndForeignPets(GameTestHelper h){
        var p=h.makeMockServerPlayerInLevel();var stranger=h.makeMockServerPlayerInLevel();var level=h.getLevel();
        BlockPos at=h.absolutePos(new BlockPos(2,2,2));p.moveTo(Vec3.atBottomCenterOf(at));
        var guide=EntityType.WOLF.create(level);var stay=EntityType.CAT.create(level);var foreign=EntityType.WOLF.create(level);
        guide.tame(p);stay.tame(p);foreign.tame(stranger);guide.moveTo(p.position().add(20,0,0));stay.moveTo(p.position().add(1,0,0));foreign.moveTo(p.position().add(1,0,1));
        // Persisted tracking is allowed in a source world; issuing it still requires the interior.
        guide.getPersistentData().putInt("HouseCompanionOrder",CompanionOrders.Order.DEEPER.ordinal());guide.setHealth(4);
        CompanionOrders.issue(stay,p,CompanionOrders.Order.STAY);stay.setOrderedToSit(false);
        level.addFreshEntity(guide);level.addFreshEntity(stay);level.addFreshEntity(foreign);UUID id=guide.getUUID();
        h.runAfterDelay(2,()->{
            try{
                var followers=CompanionOrders.followingAll(p);
                h.assertTrue(followers.contains(guide)&&!followers.contains(stay)&&!followers.contains(foreign),"the guide ahead crosses; saved Stay and someone else's pet do not");
                Vec3 left=stay.position();HouseInternalTeleport.shift(p,p.position().add(60,0,0),90);
                h.assertTrue(guide.distanceToSqr(p)<4&&guide.getUUID().equals(id)&&guide.getHealth()==4
                        &&CompanionOrders.order(guide)==CompanionOrders.Order.DEEPER,"the actual seamless door move retains native identity, health and order");
                h.assertTrue(stay.position().distanceToSqr(left)<1,"Stay remains on the original side");
            }finally{guide.discard();stay.discard();foreign.discard();remove(p);remove(stranger);}
            h.succeed();
        });
    }
    @GameTest(template="empty",batch="companion_doors")
    public static void nativePatKeepsDogAndCatOrdersAndThrottlesResponse(GameTestHelper h){
        var p=h.makeMockServerPlayerInLevel();p.moveTo(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(2,2,2))));
        try{for(boolean feline:List.of(false,true)){
            TamableAnimal pet=feline?EntityType.CAT.create(h.getLevel()):EntityType.WOLF.create(h.getLevel());pet.tame(p);pet.moveTo(p.position().add(1,0,0));h.getLevel().addFreshEntity(pet);
            CompanionOrders.issue(pet,p,CompanionOrders.Order.STAY);
            h.assertTrue(CompanionOrders.pet(pet,p)&&!CompanionOrders.pet(pet,p),"a native pat starts exactly one bounded response");
            h.assertTrue(pet.getPersistentData().getLong("CompanionPatUntil")>h.getLevel().getGameTime()
                    &&CompanionOrders.order(pet)==CompanionOrders.Order.STAY&&pet.isOrderedToSit(),"dog and cat responses retain their movement command");pet.discard();
        }}finally{remove(p);}h.succeed();
    }
}
