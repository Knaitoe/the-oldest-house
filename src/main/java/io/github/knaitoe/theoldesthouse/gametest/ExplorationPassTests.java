package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import io.github.knaitoe.theoldesthouse.network.BurnEmbersPayload;
import io.netty.buffer.Unpooled;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID+"_exploration")
@PrefixGameTestTemplate(false)
public final class ExplorationPassTests {
    private static final List<LabyrinthPlace> HALLS=List.of(LabyrinthPlace.ALCOVE_HALL,LabyrinthPlace.OFFSET_HALL,LabyrinthPlace.SERVICE_LANDING,
            LabyrinthPlace.STONE_ARCADE,LabyrinthPlace.STONE_BEND,LabyrinthPlace.STONE_LANDING);
    private static Set<BlockPos> walk(ServerLevel l,BlockPos start) {
        var out=new HashSet<BlockPos>();var queue=new ArrayDeque<BlockPos>();out.add(start);queue.add(start);
        while(!queue.isEmpty()) {
          BlockPos from=queue.removeFirst();
          for(var side:Direction.Plane.HORIZONTAL) {
            BlockPos at=from.relative(side);
            var body=new AABB(at.getX()+.2,at.getY()+.01,at.getZ()+.2,at.getX()+.8,at.getY()+1.81,at.getZ()+.8);
            if(!out.contains(at)&&l.getBlockState(at.below()).isCollisionShapeFullBlock(l,at.below())&&l.noCollision(null,body)) {out.add(at);queue.add(at);}
          }
        }
        return out;
    }
    @GameTest(template="empty")
    public static void sixHallwaysHaveRealRoutesSupportedLampsAndRoomForTwoBodies(GameTestHelper h)throws Exception {
        var l=h.getLevel();int i=0;
        for(var hall:HALLS) {
            BlockPos b=h.absolutePos(BlockPos.ZERO).offset(2100+i++*60,8,280);
            if(HallVariations.domestic(hall))LabyrinthHalls.build(l,b,hall);else StoneHalls.build(l,b,hall);
            HouseFurnishings.decorate(l,b,hall);
            ScenePolish.apply(l,b,hall);
            var reached=walk(l,b.offset(0,0,-1));
            for(var door:hall.doors()) {
                var at=b.offset(door.rel());h.assertTrue(l.getBlockState(at).getBlock() instanceof DoorBlock,hall.id()+" retains its native "+door.name()+" door");
                if(!door.name().equals("entry"))h.assertTrue(reached.contains(at.relative(door.facing())),"a native body reaches "+hall.id()+"/"+door.name());
            }
            for(var relative:HallVariations.floor(hall)) {
                var at=b.offset(relative);h.assertTrue(l.getBlockState(at.below()).isCollisionShapeFullBlock(l,at.below()),"every bay rests on a full supported floor");
                if(l.getBlockState(at).isAir())h.assertTrue(reached.contains(at),"every empty alcove belongs to the reachable floor: "+hall.id()+"/"+relative);
            }
            for(var at:BlockPos.betweenClosed(b.offset(hall.room().minX(),0,hall.room().minZ()),b.offset(hall.room().maxX(),hall.room().maxY(),0)))
                if(l.getBlockState(at).is(Blocks.LANTERN))h.assertTrue(l.getBlockState(at).canSurvive(l,at),"every new lamp has real support");
            for(double side:new double[]{-.65,.65}) {
                var at=b.offset(0,0,-4);var body=new AABB(at.getX()+.5+side-.3,at.getY()+.01,at.getZ()+.2,at.getX()+.5+side+.3,at.getY()+1.81,at.getZ()+.8);
                h.assertTrue(l.noCollision(null,body),hall.id()+" leaves two actual bodies clear beside one another");
            }
            h.assertTrue(LabyrinthDealer.onward(hall)&&LabyrinthPacing.ordinary(hall),"variation is an ordinary route, not another hazard or Witness source");
            ArchitectureTests.export(l,b,hall);
        }
        h.succeed();
    }
    @GameTest(template="empty")
    public static void hallwayConstructionIsPacedAndMatchesItsDirectNativeFixture(GameTestHelper h) {
        var l=h.getLevel();int i=0;
        for(var hall:List.of(LabyrinthPlace.OFFSET_HALL,LabyrinthPlace.STONE_ARCADE)) {
            var direct=h.absolutePos(BlockPos.ZERO).offset(2900+i++*100,8,280);var paced=direct.east(55);
            Runnable first=()->{if(HallVariations.domestic(hall))LabyrinthHalls.build(l,direct,hall);else StoneHalls.build(l,direct,hall);};first.run();
            var plan=BuildBlocks.record(l,()->{if(HallVariations.domestic(hall))LabyrinthHalls.build(l,paced,hall);else StoneHalls.build(l,paced,hall);});
            h.assertTrue(l.getBlockState(paced.offset(0,-1,-5)).isAir(),"recording the fresh hall does not synchronously carve its native blocks");
            boolean done=false;int slices=0;while(!done&&slices++<5000){done=plan.tick();h.assertTrue(plan.lastVisits()<=BuildBlocks.MAX_VISITS,"every hall construction slice has a hard native block-visit cap");}
            h.assertTrue(done&&slices>1,"the complete hall requires multiple bounded slices");
            var box=hall.room();for(var relative:BlockPos.betweenClosed(new BlockPos(box.minX(),-1,box.minZ()),new BlockPos(box.maxX(),box.maxY(),17)))
                h.assertTrue(l.getBlockState(direct.offset(relative)).equals(l.getBlockState(paced.offset(relative))),"paced geometry preserves exact authored native blocks: "+hall.id()+"/"+relative);
        }
        h.succeed();
    }
    private static void depth(LabyrinthData d,UUID p,int count) {
        for(int i=0;i<count;i++)d.pushReturn(p,new LabyrinthData.Waypoint(HouseDimensions.INTERIOR,new Vec3(i,80,3),0,true));
    }
    private static Map<String,String> map(LabyrinthData d,UUID p,LabyrinthPlace room) {
        Map<String,String> out=new HashMap<>();for(var spec:room.doors())if(LabyrinthData.DEALT.equals(spec.destination())) {
            var door=d.door(room.doorId(spec));out.put(door.id,d.deal(p,door).place());
        }return out;
    }
    @GameTest(template="empty")
    public static void threeDifferentHistoriesShareFreshOrdinaryRoutesAndKeepThemAfterReload(GameTestHelper h) {
        var d=new LabyrinthData();var room=LabyrinthPlace.CROSS_HALL;LabyrinthBuilder.registerDoors(d,room,new BlockPos(400,80,0));
        var a=UUID.randomUUID();var b=UUID.randomUUID();var c=UUID.randomUUID();
        for(var hall:HALLS)d.visit(a,hall);d.visit(b,LabyrinthPlace.BENT_HALL);d.visit(b,LabyrinthPlace.STRAIGHT_HALL);
        for(var p:List.of(a,b,c)){depth(d,p,2);d.visit(p,room);LabyrinthDealer.arriveAt(d,p,room,17);}
        h.assertTrue(map(d,a,room).equals(map(d,b,room))&&map(d,a,room).equals(map(d,c,room)),"three explorers taking the same new route share its halls despite different recent visits");
        int dry=d.dryDeals(b);var before=map(d,b,room);var key=d.nodeKey(b,room);
        var loaded=LabyrinthData.load(d.save(new CompoundTag(),h.getLevel().registryAccess()),h.getLevel().registryAccess());
        loaded.visit(b,LabyrinthPlace.QUIET_ROOM);LabyrinthDealer.arriveAt(loaded,b,room,17);
        h.assertTrue(before.equals(map(loaded,b,room))&&loaded.dryDeals(b)==dry&&loaded.node(b,key)!=null,"reconnect and backtracking restore the saved route without banking stories");
        h.succeed();
    }
    @GameTest(template="empty")
    public static void legacyPersonalMapsWinOverNewSharedDiscoveries(GameTestHelper h) {
        var d=new LabyrinthData();var p=UUID.randomUUID();var room=LabyrinthPlace.STRAIGHT_HALL;
        LabyrinthBuilder.registerDoors(d,room,new BlockPos(0,80,0));depth(d,p,2);
        var door=d.door(room.doorId(room.doors().getLast()));d.deal(p,door,LabyrinthPlace.BENT_HALL.id(),false);
        long key=d.nodeKey(p,room);d.rememberNode(p,key,Map.of(door.id,d.deal(p,door)));
        var shared=new CompoundTag();shared.putString(door.id,LabyrinthPlace.OFFSET_HALL.id());d.setBoundedStateEntry("shared_halls_0448",Long.toUnsignedString(key^21),shared,2048);
        LabyrinthDealer.arriveAt(d,p,room,21);
        h.assertTrue(d.deal(p,door).place().equals(LabyrinthPlace.BENT_HALL.id()),"an explorer's existing drawn map is never replaced by another reader's new discovery");h.succeed();
    }
    @GameTest(template="empty")
    public static void sharedDiscoveryStorageIsBoundedWithoutErasingPersonalMaps(GameTestHelper h) {
        var d=new LabyrinthData();var p=UUID.randomUUID();d.rememberNode(p,9,Map.of("kept",new LabyrinthData.Deal("bent_hall",false,false)));
        for(int i=0;i<2050;i++){var tag=new CompoundTag();tag.putString("door","straight_hall");d.setBoundedStateEntry("shared_halls_0448",Integer.toString(i),tag,2048);}
        var loaded=LabyrinthData.load(d.save(new CompoundTag(),h.getLevel().registryAccess()),h.getLevel().registryAccess());
        h.assertTrue(loaded.state("shared_halls_0448").getAllKeys().size()==2049&&loaded.stateEntry("shared_halls_0448","0").isEmpty()&&!loaded.stateEntry("shared_halls_0448","2049").isEmpty(),"the 2048 shared discoveries evict the oldest and survive reload");
        h.assertTrue(loaded.node(p,9).get("kept").place().equals("bent_hall"),"shared cache eviction cannot erase personal routes");h.succeed();
    }
    @GameTest(template="empty")
    public static void spacingPausesDryDealsAndScentStillBypassesIt(GameTestHelper h) {
        var d=new LabyrinthData();var p=UUID.randomUUID();depth(d,p,12);LabyrinthBuilder.registerDoors(d,LabyrinthPlace.CROSS_HALL,new BlockPos(0,80,0));
        d.visit(p,LabyrinthPlace.HARRIGAN);d.setDryDeals(p,4);
        h.assertTrue(LabyrinthDealer.vignetteChance(d,p)==0,"a just-visited story leaves ordinary exploration ahead");
        LabyrinthDealer.dealPlace(d,p,LabyrinthPlace.CROSS_HALL,RandomSource.create(5));h.assertTrue(d.dryDeals(p)==4,"ineligible spacing cannot bank dry deals");
        d.setHillaryScent(p,true);LabyrinthDealer.dealPlace(d,p,LabyrinthPlace.CROSS_HALL,RandomSource.create(6));
        h.assertTrue(map(d,p,LabyrinthPlace.CROSS_HALL).values().stream().anyMatch(id->LabyrinthPlace.byId(id).isVignette())&&!d.hillaryScent(p),"a deliberate scent consumes one real personal story offer during spacing");
        d.visit(p,LabyrinthPlace.ALCOVE_HALL);d.visit(p,LabyrinthPlace.OFFSET_HALL);d.visit(p,LabyrinthPlace.STONE_ARCADE);
        d.setDryDeals(p,12);h.assertTrue(LabyrinthDealer.vignetteChance(d,p)==100,"a long eligible dry spell still guarantees a story");h.succeed();
    }
    @GameTest(template="empty")
    public static void aNewArrivalNeverStacksStoryAnomalyAndPhysicalHazard(GameTestHelper h) {
        var d=new LabyrinthData();var p=UUID.randomUUID();depth(d,p,16);var room=LabyrinthPlace.CROSS_HALL;
        LabyrinthBuilder.registerDoors(d,room,new BlockPos(0,80,0));int stories=0,hazards=0,quiet=0;
        for(int i=0;i<1000;i++) {
            d.setDryDeals(p,0);LabyrinthDealer.dealPlace(d,p,room,RandomSource.create(448L+i));int events=0;
            for(var id:map(d,p,room).values()) {
                var place=LabyrinthPlace.byId(id);if(place.isVignette()){events++;stories++;}
                else if(LabyrinthPacing.anomaly(place)||LabyrinthPacing.physicalTrial(place)){events++;hazards++;}else quiet++;
            }
            h.assertTrue(events<=1,"one new route cannot stack a story and a hazard across its different doors");
        }
        h.assertTrue(stories>80&&stories<240&&hazards>100&&hazards<280&&quiet>2400,"configured deep offers leave most doors ordinary: stories="+stories+", hazards="+hazards+", ordinary="+quiet);
        h.succeed();
    }
    @GameTest(template="empty")
    public static void emberExcerptUsesTheSavedHandAndNeverChangesTheOriginal(GameTestHelper h) {
        var own=new CompoundTag();own.putUUID("Original",UUID.randomUUID());own.putString("Hand","KAREN");
        var pages=new net.minecraft.nbt.ListTag();pages.add(net.minecraft.nbt.StringTag.valueOf("I walked 1,234 metres before the House. The second sentence is deliberately longer than the smoke should ever carry."));own.put("Pages",pages);
        var before=own.copy();var payload=BurnEmbers.excerpt(own,0,new BlockPos(3,80,9));var buf=Unpooled.buffer();
        try {BurnEmbersPayload.STREAM_CODEC.encode(buf,payload);var read=BurnEmbersPayload.STREAM_CODEC.decode(buf);
            h.assertTrue(read.equals(payload)&&read.text().equals("I walked 1,234 metres before the House.")&&read.hand().equals(HouseWriting.WritingStyle.KAREN.font()),"native payload preserves the original page excerpt and narrator's font");
            h.assertTrue(own.equals(before)&&payload.memory()==1,"presentation cannot rewrite saved words, found leaves or burn cursor");
        }finally{buf.release();}h.succeed();
    }
    @GameTest(template="empty")
    public static void roomSoundClockPausesEmptyAndDoesNotMultiplyForPeers(GameTestHelper h) {
        var own=new CompoundTag();for(int i=0;i<59;i++)h.assertTrue(!HallAtmosphere.occupiedSecond(own,60),"the shared room has no early cue");
        h.assertTrue(HallAtmosphere.occupiedSecond(own,60)&&own.getInt("Returns")==1&&own.getInt("Seconds")==0,"one native physical-room interval produces one cue for every listener");
        own.putBoolean("Occupied",false);var d=new LabyrinthData();d.setStateEntry(HallAtmosphere.STATE,"room",own);
        var loaded=LabyrinthData.load(d.save(new CompoundTag(),h.getLevel().registryAccess()),h.getLevel().registryAccess());own=loaded.stateEntry(HallAtmosphere.STATE,"room");
        h.assertTrue(!HallAtmosphere.occupiedSecond(own,60)&&own.getInt("Returns")==2&&own.getInt("Seconds")==1,"offline time and empty rooms produce no catch-up sound burst");h.succeed();
    }
    @GameTest(template="empty",batch="exploration_quiet",timeoutTicks=1200)
    public static void unseenChairWaitsForSpectatorsAndLivingStayPetsWithoutTouchingTheCache(GameTestHelper h) {
        var l=h.getLevel();var b=h.absolutePos(BlockPos.ZERO).offset(2700,6,350);var lease=new NativeTestChunks();
        lease.hold(l,new AABB(b.offset(-8,-1,-17),b.offset(8,8,18)));LabyrinthHalls.build(l,b,LabyrinthPlace.QUIET_ROOM);HouseFurnishings.decorate(l,b,LabyrinthPlace.QUIET_ROOM);
        var chair=b.offset(-4,0,-8);var original=l.getBlockState(chair);var cache=(net.minecraft.world.Container)l.getBlockEntity(b.offset(LabyrinthHalls.QUIET_CACHE));cache.setItem(0,ItemStack.EMPTY);cache.setItem(4,new ItemStack(Items.DIAMOND,3));
        var p=NativeTestPlayers.survival(h,"quiet_camera");p.gameMode.changeGameModeForPlayer(GameType.SPECTATOR);p.teleportTo(l,b.getX()+30,b.getY()+2,b.getZ()-8,0,0);
        var cat=EntityType.CAT.create(l);cat.moveTo(Vec3.atBottomCenterOf(b.offset(-3,0,-8)));cat.setTame(true,false);cat.setOwnerUUID(p.getUUID());cat.setOrderedToSit(true);cat.setHealth(5);l.addFreshEntity(cat);var uuid=cat.getUUID();final boolean[] done={false};
        h.onEachTick(()->{if(done[0]||!lease.ready())return;done[0]=true;
            try {
                h.assertTrue(!HallAtmosphere.turnQuietChair(l,b)&&l.getBlockState(chair).equals(original),"a real spectator camera blocks the unseen change");
                p.teleportTo(l,b.getX()+100,b.getY()+2,b.getZ(),0,0);
                h.assertTrue(!HallAtmosphere.turnQuietChair(l,b)&&cat.getUUID().equals(uuid)&&cat.getHealth()==5&&cat.isOrderedToSit(),"a living Stay pet prevents changes without moving or damaging it");
                cat.moveTo(b.getX()+100,b.getY(),b.getZ());h.assertTrue(HallAtmosphere.turnQuietChair(l,b),"the vacant, native-loaded room permits one small change");
                h.assertTrue(l.getBlockState(chair).getValue(HouseholdFurnitureBlock.FACING)==Direction.NORTH&&cache==l.getBlockEntity(b.offset(LabyrinthHalls.QUIET_CACHE))&&cache.getItem(0).isEmpty()&&cache.getItem(4).getCount()==3,"only the supported chair's angle changes; original property is retained without refill");
                h.succeed();
            }finally{cat.discard();NativeTestPlayers.remove(p);lease.close();}
        });
    }
    @GameTest(template="empty",batch="exploration_append",timeoutTicks=1200)
    public static void layoutThirtyThreeAppendsOnlyNewHallsAndKeepsExistingDoorsUsable(GameTestHelper h) {
        var server=h.getLevel().getServer();var l=HouseTestLevel.get(server);var store=server.overworld().getDataStorage();
        var oldHouse=HouseSavedData.get(server);var oldData=LabyrinthData.get(server);var origin=new BlockPos(590000,80,590000);
        var b=LabyrinthPlaces.base(origin,LabyrinthPlace.STRAIGHT_HALL);var lease=new NativeTestChunks();lease.hold(l,new AABB(b.offset(-8,-1,-38),b.offset(8,8,18)));
        LabyrinthHalls.build(l,b,LabyrinthPlace.STRAIGHT_HALL);var at=b.offset(-1,0,-7);l.setBlock(at,Blocks.BARREL.defaultBlockState(),3);
        var cache=(net.minecraft.world.Container)l.getBlockEntity(at);cache.setItem(0,new ItemStack(Items.DIAMOND,7));
        var cat=EntityType.CAT.create(l);cat.moveTo(Vec3.atBottomCenterOf(b.offset(1,0,-4)));cat.setTame(true,true);cat.setOwnerUUID(UUID.randomUUID());cat.setOrderedToSit(true);cat.setHealth(5);l.addFreshEntity(cat);var id=cat.getUUID();
        final boolean[] done={false};h.onEachTick(()->{if(done[0]||!lease.ready())return;done[0]=true;
            try {
                var house=new HouseSavedData();house.markSpawned(origin);store.set("the_oldest_house",house);
                var d=new LabyrinthData();d.setBuilt(33,origin);store.set("the_oldest_house_labyrinth",d);
                for(var p:LabyrinthPlace.values())if(p.slot()>=0&&p.slot()<72)LabyrinthBuilder.registerDoors(d,p,LabyrinthPlaces.base(origin,p));
                var reader=UUID.randomUUID();d.pushReturn(reader,new LabyrinthData.Waypoint(HouseDimensions.INTERIOR,Vec3.atBottomCenterOf(b),0,true));
                var keep=new CompoundTag();keep.putInt("Burned",3);keep.putString("Front","my saved words");d.setStateEntry(StaircaseStory.STATE,reader.toString(),keep);
                LabyrinthBuilder.clearAll();LabyrinthBuilder.gateForGameTest(true);
                h.assertTrue(LabyrinthBuilder.ensureReachable(server),"standing 0.4.47 doors stay usable immediately while new halls wait for carving");
                h.assertTrue(!LabyrinthBuilder.isPlaceReady(d,LabyrinthPlace.ALCOVE_HALL),"a new hall cannot be dealt before native construction finishes");
                LabyrinthBuilder.ensureBuilt(server);LabyrinthBuilder.finishGameTest(server);
                for(var hall:HALLS)h.assertTrue(d.door(hall.entryDoorId())!=null&&l.getBlockState(d.door(hall.entryDoorId()).lower).getBlock() instanceof DoorBlock,"all six appended rooms finish with real registered doors");
                h.assertTrue(d.builtVersion()==34&&cache==l.getBlockEntity(at)&&cache.getItem(0).getCount()==7&&cache.getItem(1).isEmpty(),"the native append keeps original container identity and finite contents");
                h.assertTrue(cat.isAlive()&&cat.getUUID().equals(id)&&cat.getHealth()==5&&cat.isOrderedToSit()&&d.returnDepth(reader)==1&&d.stateEntry(StaircaseStory.STATE,reader.toString()).equals(keep),"native pet identity, Stay, health, saved retreat and personal burned pages survive");h.succeed();
            }finally{cat.discard();lease.close();LabyrinthBuilder.gateForGameTest(null);LabyrinthBuilder.clearAll();store.set("the_oldest_house",oldHouse);store.set("the_oldest_house_labyrinth",oldData);}
        });
    }
}
