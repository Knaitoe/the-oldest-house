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
    @GameTest(template="empty")
    public static void outdoorArrivalHasGroundAndKeepsItsNativeReturnCollision(GameTestHelper h){
        var l=h.getLevel();var b=h.absolutePos(BlockPos.ZERO).offset(3700,8,500);
        for(var rel:List.of(new BlockPos(-7,0,10),new BlockPos(7,7,16))){
            var old=Blocks.WHITE_TERRACOTTA.defaultBlockState();var next=ScenePlaytestRepairs.arrivalSkin(rel,old);
            h.assertTrue(next.is(LiteraryRegistry.SIDING.get())&&old.getCollisionShape(l,b.offset(rel)).equals(next.getCollisionShape(l,b.offset(rel))),"the exposed white shell becomes siding with the same native full-block collision");
        }
        var protectedWall=new BlockPos(2,2,10);h.assertTrue(ScenePlaytestRepairs.arrivalSkin(protectedWall,Blocks.WHITE_TERRACOTTA.defaultBlockState()).is(Blocks.WHITE_TERRACOTTA),"the central copied return passage remains exact");
        var edit=b.offset(9,-1,9);l.setBlock(edit,Blocks.DIAMOND_BLOCK.defaultBlockState(),2);ScenePlaytestRepairs.arrivalGround(l,b);
        h.assertTrue(l.getBlockState(b.offset(8,-1,9)).isCollisionShapeFullBlock(l,b.offset(8,-1,9))&&l.getBlockState(b.offset(8,-2,9)).is(Blocks.DIRT)&&l.getBlockState(edit).is(Blocks.DIAMOND_BLOCK),"the gap beside the arrival has rooted ground and retains player edits");h.succeed();
    }
    @GameTest(template="empty")
    public static void sevenForwardPassesOfferOnlyStoneOrdinaryHallsAndRetainDrawnMaps(GameTestHelper h){
        var d=new LabyrinthData();var room=LabyrinthPlace.CROSS_HALL;LabyrinthBuilder.registerDoors(d,room,new BlockPos(0,80,0));
        for(int at:new int[]{7,12,20}){
            var p=UUID.randomUUID();depth(d,p,at);
            for(int seed=0;seed<300;seed++){
                d.setDryDeals(p,0);LabyrinthDealer.dealPlace(d,p,room,RandomSource.create(seed));
                for(var id:map(d,p,room).values()){var destination=LabyrinthPlace.byId(id);
                    if(LabyrinthPacing.ordinary(destination))h.assertTrue(StoneHalls.isStone(destination),"new ordinary halls are stone at depth "+at+": "+id);
                }
            }
        }
        var early=UUID.randomUUID();depth(d,early,6);int wooden=0;
        for(int seed=0;seed<100;seed++){LabyrinthDealer.dealPlace(d,early,room,RandomSource.create(seed));for(var id:map(d,early,room).values())if(LabyrinthPacing.ordinary(LabyrinthPlace.byId(id)))wooden++;}
        h.assertTrue(wooden>0&&!LabyrinthDealer.grayAvailable(d,early).stream().anyMatch(StoneHalls::isStone),"the approach still has domestic halls before seven forward passes");
        for(var p:LabyrinthPlace.values())if(StoneHalls.isStone(p))h.assertTrue(LabyrinthBuilder.requiredDepth(p)<=7,"stone construction happens ahead of its availability");
        h.succeed();
    }
    @GameTest(template="empty")
    public static void legacyCabinRepairOpensTheLadderAndPreservesItsNativeContents(GameTestHelper h){
        var l=h.getLevel();var b=h.absolutePos(BlockPos.ZERO).offset(3400,8,500);
        for(int y=-5;y<=-1;y++){l.setBlock(b.offset(0,y,-18),Blocks.DIRT.defaultBlockState(),2);l.setBlock(b.offset(0,y,-17),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.SOUTH),2);}
        l.setBlock(b.offset(0,-2,-17),Blocks.STONE_BRICKS.defaultBlockState(),2);l.setBlock(b.offset(0,-1,-17),Blocks.STONE_BRICKS.defaultBlockState(),2);
        var chest=b.offset(11,0,-10);l.setBlock(chest,Blocks.BARREL.defaultBlockState(),2);
        var inventory=(net.minecraft.world.level.block.entity.BarrelBlockEntity)l.getBlockEntity(chest);inventory.setItem(0,new ItemStack(Items.PAPER,3));
        var edit=b.offset(-6,-1,-18);l.setBlock(edit,Blocks.DIAMOND_BLOCK.defaultBlockState(),2);
        ScenePlaytestRepairs.cabin(l,b);ScenePlaytestRepairs.cabin(l,b);
        for(int y=-5;y<=-1;y++)h.assertTrue(l.getBlockState(b.offset(0,y,-17)).is(Blocks.LADDER)&&l.getBlockState(b.offset(0,y,-17)).canSurvive(l,b.offset(0,y,-17)),"the full cellar ladder has native support at "+y);
        h.assertTrue(l.getBlockState(b.offset(0,4,-24)).is(Blocks.CALCITE)&&!l.getBlockState(b.offset(0,2,-24)).isAir(),"the cabin partition meets its ceiling and each door has a header");
        h.assertTrue(l.getBlockEntity(chest)==inventory&&inventory.getItem(0).getCount()==3&&l.getBlockState(edit).is(Blocks.DIAMOND_BLOCK),"targeted retries keep native containers, finite contents and player edits");h.succeed();
    }
    @GameTest(template="empty")
    public static void nativePanesJoinTheirFrameWithoutRebuildingIt(GameTestHelper h){
        var l=h.getLevel();var b=h.absolutePos(BlockPos.ZERO).offset(3450,8,500);
        l.setBlock(b.west(),Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(),2);l.setBlock(b.east(2),Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(),2);
        BuildBlocks.box(l,b,b.east(),Blocks.GLASS_PANE.defaultBlockState(),18);
        for(var at:List.of(b,b.east())){var joined=l.getBlockState(at);h.assertTrue(joined.getValue(IronBarsBlock.WEST)&&joined.getValue(IronBarsBlock.EAST)&&joined.equals(ScenePlaytestRepairs.connected(l,at)),"each placed native pane already joins both its neighbour and frame");}
        h.assertTrue(l.getBlockState(b.west()).is(Blocks.STRIPPED_SPRUCE_LOG),"joining panes preserves the existing window frame");h.succeed();
    }
    @GameTest(template="empty")
    public static void swimmingChannelHasTwoBlockHeadroomAndDryArrival(GameTestHelper h){
        var l=h.getLevel();var b=h.absolutePos(BlockPos.ZERO).offset(3550,8,500);LabyrinthHazards.buildFloodedPassage(l,b);
        for(var feet:LabyrinthHazards.floodRoute(b)){
            var body=new AABB(feet.getX()+.2,feet.getY()+.01,feet.getZ()+.2,feet.getX()+.8,feet.getY()+1.81,feet.getZ()+.8);
            h.assertTrue(l.noCollision(null,body),"the submerged route has native body clearance at "+feet);
        }
        h.runAfterDelay(40,()->{h.assertTrue(l.getBlockState(b.offset(0,0,0)).getFluidState().isEmpty(),"the source-water channel cannot flood the arrival doorway");h.succeed();});
    }
    @GameTest(template="empty")
    public static void childDistanceMakesTheSavedOccupiedClockGrowlAndShakeMoreOften(GameTestHelper h){
        h.assertTrue(StaircaseAtmosphere.growlPeriod(0)==30&&StaircaseAtmosphere.growlPeriod(1280)==60&&StaircaseAtmosphere.shakePeriod(0)==30&&StaircaseAtmosphere.shakePeriod(1280)==60,"growls are slower and the shake uses the same cue");
        var clock=new CompoundTag();int pulses=0;for(int i=0;i<90;i++)if(StaircaseAtmosphere.occupiedSecond(clock,30))pulses++;
        h.assertTrue(pulses==3&&StaircaseAtmosphere.growlVolume(0)>StaircaseAtmosphere.growlVolume(1280),"occupied seconds, rather than the number of peers, govern a louder nearby growl");h.succeed();
    }
    @GameTest(template="empty",batch="staircase_debris",timeoutTicks=400)
    public static void neighboringReadersShareOneNativeFallingBlockWithoutOpeningTheShaft(GameTestHelper h){
        var l=HouseTestLevel.get(h.getLevel().getServer());var origin=new BlockPos(610000,0,610000);var base=FinaleArchitecture.base(origin);var at=base.offset(24,FinaleArchitecture.TOP-128,0);
        var lease=new NativeTestChunks();lease.hold(l,new AABB(at).inflate(15,12,6));
        var a=NativeTestPlayers.survival(h,"debris_a");var b=NativeTestPlayers.survival(h,"debris_b");var observer=NativeTestPlayers.survival(h,"debris_observer");observer.setGameMode(GameType.SPECTATOR);
        var walls=new ArrayList<BlockPos>();var touched=new HashSet<BlockPos>();
        Runnable cleanup=()->{l.getEntitiesOfClass(net.minecraft.world.entity.item.FallingBlockEntity.class,new AABB(at).inflate(20),e->e.getTags().contains(StaircaseDebris.TAG)).forEach(net.minecraft.world.entity.item.FallingBlockEntity::discard);for(var cell:touched)l.setBlock(cell,Blocks.AIR.defaultBlockState(),2);NativeTestPlayers.remove(a);NativeTestPlayers.remove(b);NativeTestPlayers.remove(observer);lease.close();};
        net.minecraft.world.entity.item.FallingBlockEntity[] piece={null};long[] started={-1};boolean[] motion={false},done={false};double[] initialY={0};
        h.runAfterDelay(399,()->{if(!done[0]){done[0]=true;cleanup.run();}});
        h.onEachTick(()->{if(done[0]||!lease.ready())return;
            try{
                if(started[0]<0){
                    for(int y=3;y<=8;y++)for(int z=-2;z<=2;z++){var wall=base.offset(34,at.getY()+y,z);walls.add(wall);touched.add(wall);touched.add(wall.east());l.setBlock(wall,Blocks.DEEPSLATE_TILES.defaultBlockState(),2);}
                    for(int x=22;x<=35;x++)for(int z=-3;z<=3;z++){var floor=base.offset(x,at.getY()-2,z);touched.add(floor);l.setBlock(floor,Blocks.STONE.defaultBlockState(),2);}
                    for(var p:List.of(a,b,observer)){p.teleportTo(l,at.getX()+.5,at.getY()+(p==b?1:0),at.getZ()+.5,0,0);p.connection.resetPosition();}
                    h.assertTrue(StaircaseDebris.fallForPlayers(l,origin,List.of(a,b,observer))==1,"two living neighbors across the clock boundary share one block; observers add none");
                    var falling=l.getEntitiesOfClass(net.minecraft.world.entity.item.FallingBlockEntity.class,new AABB(at).inflate(20),e->e.getTags().contains(StaircaseDebris.TAG));h.assertTrue(falling.size()==1,"one authoritative native falling entity is visible to both readers");piece[0]=falling.getFirst();initialY[0]=piece[0].getY();
                    var broken=walls.stream().filter(cell->l.getBlockState(cell).isAir()).toList();h.assertTrue(broken.size()==1&&l.getBlockState(broken.getFirst().east()).isCollisionShapeFullBlock(l,broken.getFirst().east()),"one real wall cell breaks and a solid backing retains the enclosure");started[0]=l.getGameTime();return;
                }
                if(l.getGameTime()-started[0]>=6&&!motion[0]){h.assertTrue(!piece[0].isRemoved()&&piece[0].getY()<initialY[0]&&piece[0].getDeltaMovement().y<0,"the native block actually falls clear of the wall face");motion[0]=true;}
                if(l.getGameTime()-started[0]>=40){
                    h.assertTrue(piece[0].isRemoved()&&l.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new AABB(at).inflate(20)).isEmpty(),"landing does not place an obstruction or create free drops");h.assertTrue(a.getHealth()==20&&b.getHealth()==20,"debris does not hurt either reader");
                    for(var wall:walls)l.setBlock(wall,Blocks.DIAMOND_BLOCK.defaultBlockState(),2);h.assertTrue(StaircaseDebris.fallForPlayers(l,origin,List.of(a,b,observer))==0&&walls.stream().allMatch(cell->l.getBlockState(cell).is(Blocks.DIAMOND_BLOCK)),"player-edited walls cannot become debris");done[0]=true;cleanup.run();h.succeed();
                }
            }catch(RuntimeException|Error failure){done[0]=true;cleanup.run();throw failure;}
        });
    }
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
        // A spectator taking the route first keeps a personal map but writes nothing the explorers will share.
        var watcher=UUID.randomUUID();depth(d,watcher,2);d.visit(watcher,room);LabyrinthDealer.arriveAt(d,watcher,room,17,false);
        h.assertTrue(d.stateEntry("shared_halls_0448",Long.toUnsignedString(d.nodeKey(watcher,room)^17)).isEmpty(),"a spectator's arrival does not decide the shared halls");
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
        h.assertTrue(stories>300&&stories<480&&hazards>200&&hazards<440&&quiet>1800,"configured deep offers leave most doors ordinary: stories="+stories+", hazards="+hazards+", ordinary="+quiet);
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
        lease.hold(l,new AABB(Vec3.atLowerCornerOf(b.offset(-8,-1,-17)),Vec3.atLowerCornerOf(b.offset(8,8,18))));LabyrinthHalls.build(l,b,LabyrinthPlace.QUIET_ROOM);HouseFurnishings.decorate(l,b,LabyrinthPlace.QUIET_ROOM);
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
        var b=LabyrinthPlaces.base(origin,LabyrinthPlace.STRAIGHT_HALL);var lease=new NativeTestChunks();lease.hold(l,new AABB(Vec3.atLowerCornerOf(b.offset(-8,-1,-38)),Vec3.atLowerCornerOf(b.offset(8,8,18))));
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
                h.assertTrue(d.builtVersion()==LabyrinthBuilder.VERSION&&cache==l.getBlockEntity(at)&&cache.getItem(0).getCount()==7&&cache.getItem(1).isEmpty(),"the native append keeps original container identity and finite contents");
                h.assertTrue(cat.isAlive()&&cat.getUUID().equals(id)&&cat.getHealth()==5&&cat.isOrderedToSit()&&d.returnDepth(reader)==1&&d.stateEntry(StaircaseStory.STATE,reader.toString()).equals(keep),"native pet identity, Stay, health, saved retreat and personal burned pages survive");h.succeed();
            }finally{cat.discard();lease.close();LabyrinthBuilder.gateForGameTest(null);LabyrinthBuilder.clearAll();store.set("the_oldest_house",oldHouse);store.set("the_oldest_house_labyrinth",oldData);}
        });
    }
    @GameTest(template="empty",batch="exploration_notes")
    public static void actualLooseNotesAreUniquePrivateFiniteAndKeepLegacyOriginalsAfterReload(GameTestHelper h){
        var server=h.getLevel().getServer();var l=HouseTestLevel.get(server);var store=server.overworld().getDataStorage();
        var oldHouse=HouseSavedData.get(server);var oldData=LabyrinthData.get(server);var origin=new BlockPos(640000,80,640000);
        var a=NativeTestPlayers.survival(h,"stair_notes_a");var b=NativeTestPlayers.survival(h,"stair_notes_b");
        try{
            var house=new HouseSavedData();house.markSpawned(origin);store.set("the_oldest_house",house);var d=new LabyrinthData();store.set("the_oldest_house_labyrinth",d);
            var positions=StaircaseWriting.positions(origin);h.assertTrue(!positions.isEmpty()&&positions.size()<=StaircaseNotes.TEXTS.size(),"the complete two-part physical descent has a distinct authored sheet for every landing");
            Set<String> texts=new HashSet<>();ItemStack first=null;
            for(var at:positions){
                l.setBlock(at.below(),Blocks.STONE.defaultBlockState(),2);l.setBlock(at,NoteSurfaceBlock.state(HouseMarginalia.Thread.POEMS,Direction.WEST),2);
                a.teleportTo(l,at.getX()+1.5,at.getY(),at.getZ()+.5,0,0);a.hasChangedDimension();
                h.assertTrue(StaircaseWriting.open(a,at),"a real stair sheet opens the native reader");var menu=(net.minecraft.world.inventory.LecternMenu)a.containerMenu;
                var book=menu.getSlot(0).getItem().copy();String text=book.get(net.minecraft.core.component.DataComponents.WRITTEN_BOOK_CONTENT).pages().getFirst().raw().getString();
                h.assertTrue(texts.add(text),"no two authored landings repeat their sheet");
                if(first==null){first=book;h.assertTrue(menu.clickMenuButton(a,3)&&!menu.clickMenuButton(a,3),"the shared surface yields one finite original to its own reader");}
                a.closeContainer();
            }
            var at=positions.getFirst();b.teleportTo(l,at.getX()+1.5,at.getY(),at.getZ()+.5,0,0);b.hasChangedDimension();StaircaseWriting.open(b,at);
            h.assertTrue(((net.minecraft.world.inventory.LecternMenu)b.containerMenu).clickMenuButton(b,3),"a peer has an independent finite collection, not the first reader's taken bit");b.closeContainer();
            var own=d.stateEntry(StaircaseWriting.ID,a.getUUID().toString());var books=own.getCompound("Books");var legacy=HouseWriting.book("A saved sheet","Ruth",HouseWriting.WritingStyle.KAREN,List.of("These exact words were already here."));
            books.put(Long.toString(at.asLong()),legacy.save(a.registryAccess()));own.put("Books",books);var editions=own.getCompound("Editions");editions.putInt(Long.toString(at.asLong()),427);own.put("Editions",editions);d.setStateEntry(StaircaseWriting.ID,a.getUUID().toString(),own);
            var loaded=LabyrinthData.load(d.save(new CompoundTag(),a.registryAccess()),a.registryAccess());store.set("the_oldest_house_labyrinth",loaded);
            a.teleportTo(l,at.getX()+1.5,at.getY(),at.getZ()+.5,0,0);StaircaseWriting.open(a,at);var menu=(net.minecraft.world.inventory.LecternMenu)a.containerMenu;
            h.assertTrue(ItemStack.isSameItemSameComponents(legacy,menu.getSlot(0).getItem())&&!menu.clickMenuButton(a,3),"an older read/taken original keeps its exact components and cannot refill after reload");a.closeContainer();
            h.succeed();
        }finally{a.closeContainer();b.closeContainer();NativeTestPlayers.remove(a);NativeTestPlayers.remove(b);store.set("the_oldest_house",oldHouse);store.set("the_oldest_house_labyrinth",oldData);}
    }
    @GameTest(template="empty")
    public static void staircasePulsesPauseOfflineAndRemainOneOccupiedSectionClock(GameTestHelper h){
        var clock=new CompoundTag();for(int i=0;i<54;i++)h.assertTrue(!StaircaseAtmosphere.occupiedSecond(clock,55),"a section's cue cannot arrive early");
        var d=new LabyrinthData();d.setStateEntry(StaircaseAtmosphere.STATE,"section",clock);var loaded=LabyrinthData.load(d.save(new CompoundTag(),h.getLevel().registryAccess()),h.getLevel().registryAccess());
        clock=loaded.stateEntry(StaircaseAtmosphere.STATE,"section");h.assertTrue(StaircaseAtmosphere.occupiedSecond(clock,55)&&clock.getInt("Seconds")==0,"one occupied second after reload yields one cue, without offline catch-up");h.succeed();
    }
    @GameTest(template="empty")
    public static void stairDamageStaysAwayFromLandingsSourcesAndTheCentralRoute(GameTestHelper h){
        var origin=new BlockPos(0,80,0);var marks=StaircaseWear.marks(origin);var base=FinaleArchitecture.base(origin);
        h.assertTrue(!marks.isEmpty()&&marks.size()<20,"the long two-part descent has sparse, recognizable edge damage");
        for(var mark:marks){
            h.assertTrue(Math.abs(mark.hole().getX()-mark.route().getX())+Math.abs(mark.hole().getZ()-mark.route().getZ())==4,"a hole is on the edge, outside the seven central columns");
            h.assertTrue(Math.min(Math.abs(mark.route().getX()-base.getX()),Math.abs(mark.route().getZ()-base.getZ()))<=15,"turn platforms remain whole");
            for(var p:StaircaseFire.braziers(origin))h.assertTrue(p.distSqr(mark.hole())>=100,"canonical hearth approaches remain clear");
            for(var p:StaircaseLeaves.positions(origin))h.assertTrue(p.distSqr(mark.hole())>=100,"canonical original leaves stay on supported accessible treads");
        }h.succeed();
    }
    @GameTest(template="empty",batch="exploration_wear",timeoutTicks=1200)
    public static void oldStairWearWaitsForRealCamerasAndStayPetsAndHonorsPlayerEdits(GameTestHelper h){
        var l=HouseTestLevel.get(h.getLevel().getServer());var at=h.absolutePos(BlockPos.ZERO).offset(3400,10,3400);
        var mark=new StaircaseWear.Mark(at,at.east(4).below(),at.east(5));var lease=new NativeTestChunks();lease.hold(l,new AABB(mark.hole()).inflate(7,9,7));
        for(var cell:mark.cells())l.setBlock(cell,Blocks.DEEPSLATE_BRICKS.defaultBlockState(),2);l.setBlock(mark.rail(),Blocks.IRON_BARS.defaultBlockState(),2);
        var p=NativeTestPlayers.survival(h,"stair_wear_camera");p.setGameMode(GameType.SPECTATOR);p.teleportTo(l,at.getX()+10,at.getY(),at.getZ(),0,0);
        var cat=EntityType.CAT.create(l);cat.moveTo(Vec3.atBottomCenterOf(mark.hole().above()));cat.setTame(true,true);cat.setOwnerUUID(p.getUUID());cat.setOrderedToSit(true);cat.setHealth(5);l.addFreshEntity(cat);var id=cat.getUUID();boolean[] done={false};
        h.onEachTick(()->{if(done[0]||!lease.ready())return;done[0]=true;
            try{
                h.assertTrue(!StaircaseWear.apply(l,mark),"an actual spectator camera prevents visible structural change");p.teleportTo(l,at.getX()+100,at.getY(),at.getZ(),0,0);
                h.assertTrue(!StaircaseWear.apply(l,mark)&&cat.getUUID().equals(id)&&cat.getHealth()==5&&cat.isOrderedToSit(),"a living Stay pet blocks the planned edge change without losing identity or orders");cat.moveTo(at.getX()+100,at.getY(),at.getZ());
                l.setBlock(mark.hole(),Blocks.DIAMOND_BLOCK.defaultBlockState(),2);h.assertTrue(StaircaseWear.apply(l,mark)&&l.getBlockState(mark.hole()).is(Blocks.DIAMOND_BLOCK)&&l.getBlockState(mark.rail()).is(Blocks.IRON_BARS),"player-edited flooring is retained together with its rail");
                l.setBlock(mark.hole(),Blocks.DEEPSLATE_BRICKS.defaultBlockState(),2);
                var beside=mark.rail().north();l.setBlock(beside,Blocks.IRON_BARS.defaultBlockState().setValue(net.minecraft.world.level.block.IronBarsBlock.SOUTH,true).setValue(net.minecraft.world.level.block.IronBarsBlock.NORTH,true),2);
                h.assertTrue(StaircaseWear.apply(l,mark)&&mark.cells().stream().allMatch(cell->l.getBlockState(cell).isAir()),"a vacant, native-loaded authored edge receives only the three planned changes");
                h.assertTrue(!l.getBlockState(beside).getValue(net.minecraft.world.level.block.IronBarsBlock.SOUTH)&&l.getBlockState(beside).getValue(net.minecraft.world.level.block.IronBarsBlock.NORTH),"the rail beside the break stops at it instead of reaching into the gap");h.succeed();
            }finally{cat.discard();NativeTestPlayers.remove(p);lease.close();}
        });
    }
}
