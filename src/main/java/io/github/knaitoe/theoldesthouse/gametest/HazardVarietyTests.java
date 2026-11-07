package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import io.github.knaitoe.theoldesthouse.network.HazardViewPayload;
import java.util.*;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.*;

/** Real blocks, cameras, vibration sensors, living attributes, collision and SavedData; no simulated replacement engine. */
@GameTestHolder(TheOldestHouse.MOD_ID+"_exploration") @PrefixGameTestTemplate(false)
public final class HazardVarietyTests {
    private static Fixture active;
    private static final class Fixture implements AutoCloseable {
        final GameTestHelper h;final ServerLevel level;final BlockPos origin=new BlockPos(92000,80,92000);final HouseSavedData house;final LabyrinthData data;
        final NativeTestChunks chunks=new NativeTestChunks();final List<ServerPlayer> players=new ArrayList<>();final Set<LabyrinthPlace> built=new HashSet<>();
        Fixture(GameTestHelper h){this.h=h;level=HouseTestLevel.get(h.getLevel().getServer(),HouseDimensions.INTERIOR);house=HouseSavedData.get(level.getServer());data=LabyrinthData.get(level.getServer());var ownHouse=new HouseSavedData();ownHouse.markSpawned(origin);ownHouse.markInteriorInitialized();var store=level.getServer().overworld().getDataStorage();store.set("the_oldest_house",ownHouse);store.set("the_oldest_house_labyrinth",new LabyrinthData());d().setBuilt(LabyrinthBuilder.VERSION,origin);active=this;}
        LabyrinthData d(){return LabyrinthData.get(level.getServer());}BlockPos base(LabyrinthPlace p){return LabyrinthPlaces.base(origin,p);}
        void build(LabyrinthPlace p){if(built.contains(p))return;var b=base(p);h.assertTrue(b!=null,"appended native slot fits: "+p);built.add(p);var bounds=LabyrinthPlaces.placeBounds(origin,p);chunks.hold(level,new AABB(bounds.minX(),bounds.minY(),bounds.minZ(),bounds.maxX()+1,bounds.maxY()+1,bounds.maxZ()+1));HazardMaps.build(level,b,p);LabyrinthBuilder.registerDoors(d(),p,b);d().setReady(p.id(),true);}
        ServerPlayer player(String name,LabyrinthPlace p){build(p);var reader=NativeTestPlayers.survival(h,name);players.add(reader);at(reader,p,.5,0,-2.5);reader.setNoGravity(true);HazardVariety.onArrive(reader,p);return reader;}
        void at(ServerPlayer p,LabyrinthPlace place,double x,double y,double z){var b=base(place);p.teleportTo(level,b.getX()+x,b.getY()+y,b.getZ()+z,0,0);p.hasChangedDimension();p.setDeltaMovement(Vec3.ZERO);}
        LabyrinthData.Door door(LabyrinthPlace p,int index){return d().door(p.doorId(p.doors().get(index+1)));}
        CompoundTag room(LabyrinthPlace p){return d().stateEntry(HazardVariety.ROOMS,Long.toString(base(p).asLong()));}
        CompoundTag own(ServerPlayer p,LabyrinthPlace place){return d().stateEntry(HazardVariety.READERS,p.getUUID()+":"+place.id());}
        void ready(Runnable then){h.startSequence().thenWaitUntil(()->h.assertTrue(chunks.ready(),"hazard chunks and native entity sections are resident")).thenExecute(then);}
        void body(BlockPos b,double x,double y,double z,double scale,String why){var at=Vec3.atLowerCornerOf(b).add(x,y,z);h.assertTrue(level.noCollision(null,new AABB(at.x-.3*scale,at.y+.005,at.z-.3*scale,at.x+.3*scale,at.y+1.8*scale,at.z+.3*scale)),why+" clears a native body");}
        void walkCountingStair(){var b=base(LabyrinthPlace.COUNTING_STAIR);double previous=0;for(int n=0;n<=172;n++){double z=b.getZ()-2.5-n*.25,feet=Double.NaN;for(double y=previous;y<=Math.min(14,previous+.5);y+=.5){double at=b.getY()+y,x=b.getX()+.5;var body=new AABB(x-.3,at+.005,z-.3,x+.3,at+1.8,z+.3);var support=new AABB(x-.29,at-.1,z-.29,x+.29,at,z+.29);if(level.noCollision(null,body)&&!level.noCollision(null,support)){feet=y;break;}}h.assertTrue(Double.isFinite(feet),"each counting-stair tread supports a native reader within normal step height at "+n);previous=feet;}h.assertTrue(previous==14,"the native walking route reaches the seventh landing");}
        @Override public void close(){for(var p:players){HazardVariety.clear(p);NativeTestPlayers.remove(p);}for(var place:built)if(base(place)!=null)for(var e:level.getEntitiesOfClass(Entity.class,new AABB(base(place)).inflate(70)))if(!(e instanceof ServerPlayer))e.discard();chunks.close();var store=level.getServer().overworld().getDataStorage();store.set("the_oldest_house",house);store.set("the_oldest_house_labyrinth",data);}
    }
    private static void close(){if(active!=null){active.close();active=null;}}
    private static void depth(LabyrinthData d,UUID id,int n){for(int i=0;i<n;i++)d.pushReturn(id,new LabyrinthData.Waypoint(HouseDimensions.INTERIOR,new Vec3(0,80,i*4),0,true));}
    @GameTest(template="empty") public static void siblingSelectionIsRouteStableAndNeverRewritesAnExistingMap(GameTestHelper h){var d=new LabyrinthData();var seen=new HashSet<LabyrinthPlace>();for(int route=0;route<128;route++){var a=HazardVariety.routeVariant(LabyrinthPlace.FLOODED_PASSAGE,route,d);seen.add(a);h.assertTrue(a==HazardVariety.routeVariant(LabyrinthPlace.FLOODED_PASSAGE,route,d),"a shared route chooses the same sibling");}h.assertTrue(seen.equals(Set.of(LabyrinthPlace.FLOODED_PASSAGE,LabyrinthPlace.BENT_CISTERN)),"both layouts are dealt without adding a second family weight");
        var id=UUID.randomUUID();LabyrinthBuilder.registerDoors(d,LabyrinthPlace.CROSS_HALL,new BlockPos(0,80,0));var door=d.door(LabyrinthPlace.CROSS_HALL.doorId(LabyrinthPlace.CROSS_HALL.doors().get(1)));d.deal(id,door,LabyrinthPlace.FLOODED_PASSAGE.id(),false);var before=d.deal(id,door);HazardVariety.routeVariant(LabyrinthPlace.FLOODED_PASSAGE,3,d);h.assertTrue(d.deal(id,door).equals(before),"layout choices leave already discovered links exact");h.succeed();}
    @GameTest(template="empty",batch="hazard_layout",timeoutTicks=400) public static void nativeLayoutsHaveVaultsSensorsSupportedRecoveryAndSmallDoors(GameTestHelper h){var f=new Fixture(h);for(var p:LabyrinthPlace.values())if(HazardVariety.added(p))f.build(p);
        for(var p:LabyrinthPlace.values())if(HazardVariety.added(p)){var b=f.base(p);f.body(b,.5,0,-2.5,1,p.id()+" entrance");h.assertTrue(f.level.getBlockState(b.offset(0,0,1)).getBlock() instanceof DoorBlock,"native retreat door stands in "+p);}
        for(var pocket:HazardMaps.airPockets()){var b=f.base(LabyrinthPlace.BENT_CISTERN);h.assertTrue(f.level.getBlockState(b.offset(pocket).above(2)).isAir()&&f.level.getBlockState(b.offset(pocket).above()).is(Blocks.WATER),"each cistern vault has breathable air above its swim channel");}
        for(var sensor:HazardMaps.sensors(f.base(LabyrinthPlace.QUIET_HALL)))h.assertTrue(f.level.getBlockState(sensor).is(Blocks.SCULK_SENSOR)&&HazardBlock.is(f.level.getBlockState(sensor.above()),HazardBlock.Kind.SCULK_BOARDS),"real native sensors sit under the boards");
        for(var p:List.of(LabyrinthPlace.HEAVY_AND_LIGHT,LabyrinthPlace.TURNING_GRAVITY,LabyrinthPlace.SPIRAL_WALKWAY)){var b=f.base(p);int ladders=0;var r=p.room();for(var at:BlockPos.betweenClosed(b.offset(r.minX(),r.minY(),r.minZ()),b.offset(r.maxX(),r.maxY(),0)))if(f.level.getBlockState(at).is(Blocks.LADDER)){h.assertTrue(f.level.getBlockState(at).canSurvive(f.level,at),"every recovery ladder has real support: "+p+" "+at.subtract(b));ladders++;}h.assertTrue(ladders>=6,"recovery ladders exist in "+p);}
        // 0.4.308: the spiral walkway's two recovery ladders, at z -24: each climbs from the pit floor to its own walk's height (3 east, 6 west) and no further, and steps off onto that walk.
        {var b=f.base(LabyrinthPlace.SPIRAL_WALKWAY);h.assertTrue(HazardMaps.spiralLadderTop(11)==3&&HazardMaps.spiralLadderTop(-11)==6,"the walks pass the ladders at heights 3 and 6");
            for(int x:new int[]{-11,11}){int top=HazardMaps.spiralLadderTop(x),z=HazardMaps.SPIRAL_LADDER_Z;
                for(int y=-3;y<top;y++)h.assertTrue(f.level.getBlockState(b.offset(x,y,z)).is(Blocks.LADDER),"an unbroken ladder from the pit floor at x "+x+" y "+y);
                // A climber's feet rise one block past the top rung: the last rung a block below the walk brings them off level with it, not above.
                h.assertTrue(!f.level.getBlockState(b.offset(x,top,z)).is(Blocks.LADDER)&&!f.level.getBlockState(b.offset(x,top+1,z)).is(Blocks.LADDER),"the ladder at x "+x+" stops at its walk's height");
                int walk=x<0?-10:10;h.assertTrue(!f.level.getBlockState(b.offset(walk,top-1,z)).isAir()&&f.level.getBlockState(b.offset(walk,top,z)).isAir()&&f.level.getBlockState(b.offset(walk,top+1,z)).isAir(),"its top steps off onto the walk at x "+walk+" height "+top);
                f.body(b,walk+.5,top,z+.5,1,"a reader stepping off the ladder onto the walk at x "+walk);
                f.body(b,x+.5,-3,z+.5,1,"a reader at the foot of the ladder at x "+x);}
            for(int z:new int[]{-9,-19,-27})for(int x:new int[]{-11,11})h.assertTrue(!f.level.getBlockState(b.offset(x,0,z)).is(Blocks.LADDER),"the old ladders that ran past their walks are gone: "+x+","+z);}
        f.walkCountingStair();
        var b=f.base(LabyrinthPlace.NESTED_ROOMS);f.body(b,.5,0,-17.5,.5,"second parlour arch");f.body(b,.5,0,-26.5,.34,"smaller third parlour arch");h.assertTrue(!f.level.noCollision(null,new AABB(b.getX()+.35,b.getY(),b.getZ()-26.65,b.getX()+.65,b.getY()+.9,b.getZ()-26.35)),"the third arch is too small for the previous half-size body");f.body(b,.5,0,-38.5,.34,"skirting exit");h.assertTrue(!f.level.noCollision(null,new AABB(b.getX()+.2,b.getY(),b.getZ()-38.8,b.getX()+.8,b.getY()+1.8,b.getZ()-38.2)),"a full-sized reader cannot bypass the skirting gap");h.succeed();}
    @AfterBatch(batch="hazard_layout") public static void layoutDone(ServerLevel l){close();}
    /**
     * 0.4.323 (owner, November 5: "this room is unpassable, if a player falls. the manipulated gravity is great, but it binds a player
     * to the ladders"): a reader on a recovery ladder is not pulled, and the walk itself still pulls outward.
     */
    @GameTest(template="empty",batch="hazard_spiral",timeoutTicks=300) public static void spiralClimbersAreNotPulledButTheWalkStillPulls(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.SPIRAL_WALKWAY;var p=f.player("spiral_climber",place);var b=f.base(place);int z=HazardMaps.SPIRAL_LADDER_Z;
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready()&&f.room(place).getLong("Clock")>20,"the walkway's own clock runs"))
            .thenExecute(()->{f.at(p,place,11.5,1,z+.5);h.assertTrue(p.onClimbable(),"the reader is on the east recovery ladder, inside the pull's band");})
            .thenExecuteFor(30,()->{p.moveTo(b.getX()+11.5,b.getY()+1,b.getZ()+z+.5);h.assertTrue(Math.abs(p.getDeltaMovement().x)<1e-6&&Math.abs(p.getDeltaMovement().z)<1e-6,"a climber is not pressed into the wall or along it: "+p.getDeltaMovement());})
            .thenExecute(()->f.at(p,place,9.5,2,-14.5))
            .thenWaitUntil(()->{p.moveTo(b.getX()+9.5,b.getY()+2,b.getZ()-14.5);h.assertTrue(p.getDeltaMovement().x>0,"off the ladder, the walk still pulls outward: "+p.getDeltaMovement());})
            .thenSucceed();
    }
    @AfterBatch(batch="hazard_spiral") public static void spiralDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="hazard_camera",timeoutTicks=200) public static void spectatorAndBorrowedCameraPinTheDoorAndBodiesGuardItsDestination(GameTestHelper h){var f=new Fixture(h);var p=f.player("hazard_camera",LabyrinthPlace.WATCHED_THRESHOLD);var b=f.base(LabyrinthPlace.WATCHED_THRESHOLD);p.setGameMode(GameType.SPECTATOR);final boolean[] checked={false};h.onEachTick(()->{if(checked[0]||!f.chunks.ready())return;checked[0]=true;f.at(p,LabyrinthPlace.WATCHED_THRESHOLD,.5,0,-18);p.lookAt(EntityAnchorArgument.Anchor.EYES,b.offset(0,1,-23).getCenter());var w=f.room(LabyrinthPlace.WATCHED_THRESHOLD);h.assertTrue(HazardVariety.watched(f.level,b.offset(0,1,-23))&&!HazardVariety.moveThreshold(f.level,b,w,1),"a spectator watching the actual door pins it");
        p.setYRot(0);p.setXRot(-90);h.assertTrue(HazardVariety.moveThreshold(f.level,b,w,1),"an unwatched empty native door can move");
        f.at(p,LabyrinthPlace.WATCHED_THRESHOLD,-7,0,-12);p.lookAt(EntityAnchorArgument.Anchor.EYES,b.offset(-11,1,-12).getCenter());h.assertTrue(!HazardVariety.moveThreshold(f.level,b,w,2),"a second wall can be pinned too");p.setYRot(0);p.setXRot(0);var camera=EntityType.ARMOR_STAND.create(f.level);camera.moveTo(b.getX()-7,b.getY(),b.getZ()-12,90,0);h.assertTrue(f.level.addFreshEntity(camera),"the borrowed camera has a native tracked body");f.at(p,LabyrinthPlace.WATCHED_THRESHOLD,0,0,-10);p.setCamera(camera);h.assertTrue(HazardVariety.watched(f.level,b.offset(-11,1,-12)),"the borrowed native camera pins the wall even while its owner looks away");p.setCamera(p);p.setYRot(0);p.setXRot(-90);camera.discard();var item=new ItemEntity(f.level,b.getX()+11.3,b.getY()+.2,b.getZ()-11.7,new ItemStack(Items.PAPER));h.assertTrue(f.level.addFreshEntity(item)&&f.level.getEntitiesOfClass(ItemEntity.class,new AABB(b.offset(11,0,-12)).inflate(.12)).contains(item),"the native dropped item is visible in a fully loaded entity chunk");h.assertTrue(!HazardVariety.moveThreshold(f.level,b,w,2),"native dropped objects also prevent a wall closing over them");item.discard();h.succeed();});}
    @AfterBatch(batch="hazard_camera") public static void cameraDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="hazard_maze",timeoutTicks=200) public static void nativePanelsCarryChalkAndAllThirtySixCellsKeepAnExitPath(GameTestHelper h){var f=new Fixture(h);f.build(LabyrinthPlace.UNWATCHED_MAZE);var b=f.base(LabyrinthPlace.UNWATCHED_MAZE);long mask=HazardMaze.initialMask(b.asLong());boolean moved=false;
        for(int source=0;source<60&&!moved;source++){if((mask&(1L<<source))!=0)continue;var a=HazardMaze.EDGES.get(source);if(a.centre().getZ()>-10)continue;var face=a.east()?Direction.EAST:Direction.SOUTH;var mark=b.offset(a.centre()).relative(face);if(!f.level.getBlockState(mark).isAir())continue;var chalk=HouseBlocks.CHALK_MARK.get().defaultBlockState().setValue(ChalkMarkBlock.FACE,face).setValue(ChalkMarkBlock.ARROW,Direction.NORTH);f.level.setBlock(mark,chalk,3);
            for(int target:HazardMaze.path(mask,a.a(),a.b())){if(HazardMaze.EDGES.get(target).east()!=a.east())continue;long next=HazardMaze.exchange(f.level,b,mask,source,target);if(next==mask)continue;var dest=b.offset(HazardMaze.EDGES.get(target).centre()).relative(face);h.assertTrue(f.level.getBlockState(dest).equals(chalk)&&f.level.getBlockState(mark).isAir(),"native chalk moves with its panel and keeps its arrow");h.assertTrue(HazardMaze.connected(next),"every reader's cell still connects to the exit after the exchange");mask=next;moved=true;break;}if(!moved)f.level.setBlock(mark,Blocks.AIR.defaultBlockState(),3);}
        h.assertTrue(moved,"a complete native panel exchange occurred");for(int n=0;n<36;n++)h.assertTrue(n==33||!HazardMaze.path(mask,n,33).isEmpty(),"the exit is reachable from cell "+n);h.succeed();}
    @AfterBatch(batch="hazard_maze") public static void mazeDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="hazard_cold",timeoutTicks=600) public static void coldUsesNativeFrostPeersSlowItHeatThawsAndDamageStopsAtThreeHearts(GameTestHelper h){var f=new Fixture(h);var solo=f.player("cold_solo",LabyrinthPlace.COLD_STORE);var a=f.player("cold_pair_a",LabyrinthPlace.COLD_STORE);var peer=f.player("cold_pair_b",LabyrinthPlace.COLD_STORE);
        // 0.4.165: the frost is measured from the moment the cold store's native sections are loaded (run 1330 timed it from a still-loading room).
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the cold store's native sections are loaded")).thenExecute(()->{f.at(solo,LabyrinthPlace.COLD_STORE,0,0,-10);f.at(a,LabyrinthPlace.COLD_STORE,0,0,-26);f.at(peer,LabyrinthPlace.COLD_STORE,1,0,-26);for(var r:List.of(solo,a,peer))r.setTicksFrozen(0);})
            .thenIdle(110).thenExecute(()->{h.assertTrue(solo.getTicksFrozen()>a.getTicksFrozen()*1.6&&a.getTicksFrozen()>25,"native freezing is halved with another reader nearby: "+solo.getTicksFrozen()+" alone, "+a.getTicksFrozen()+" together");solo.setHealth(7);solo.hurt(solo.damageSources().freeze(),20);h.assertTrue(solo.getHealth()>=6,"even a large native freezing hit stops at three hearts");f.at(solo,LabyrinthPlace.COLD_STORE,0,0,-13);})
            .thenIdle(35).thenExecute(()->{h.assertTrue(solo.getTicksFrozen()==0,"the authored heat lamp thaws native frost");h.assertTrue(!HazardVariety.compressorOpen(399)&&HazardVariety.compressorOpen(400)&&HazardVariety.compressorOpen(599)&&!HazardVariety.compressorOpen(600),"the compressor latch is open for exactly ten occupied seconds");}).thenSucceed();}
    @AfterBatch(batch="hazard_cold") public static void coldDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="hazard_attributes",timeoutTicks=600) public static void nativeGravityJumpAndFallModifiersCleanUpWithoutRemovingAnotherModsEffects(GameTestHelper h){var f=new Fixture(h);var p=f.player("heavy_reader",LabyrinthPlace.HEAVY_AND_LIGHT);var foreign=net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("fixture","foreign_scale");p.getAttribute(Attributes.SCALE).addTransientModifier(new AttributeModifier(foreign,.1,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        f.ready(()->{
        h.runAfterDelay(25,()->{h.assertTrue(p.getAttribute(Attributes.GRAVITY).getModifier(HazardVariety.GRAVITY)!=null&&p.getAttributeValue(Attributes.GRAVITY)>.15&&p.getAttributeValue(Attributes.JUMP_STRENGTH)<.3,"the heavy room changes actual native attributes");f.at(p,LabyrinthPlace.HEAVY_AND_LIGHT,0,0,-38);});h.runAfterDelay(40,()->{h.assertTrue(p.getAttributeValue(Attributes.GRAVITY)<.04&&p.getAttributeValue(Attributes.FALL_DAMAGE_MULTIPLIER)==0,"the light room has actual low gravity and safe fall damage");HazardVariety.logout(new PlayerEvent.PlayerLoggedOutEvent(p));for(var pair:List.of(Map.entry(Attributes.GRAVITY,HazardVariety.GRAVITY),Map.entry(Attributes.JUMP_STRENGTH,HazardVariety.JUMP),Map.entry(Attributes.FALL_DAMAGE_MULTIPLIER,HazardVariety.FALL)))h.assertTrue(p.getAttribute(pair.getKey()).getModifier(pair.getValue())==null,"logout removes every owned movement modifier");h.assertTrue(p.getAttribute(Attributes.SCALE).getModifier(foreign)!=null,"unrelated modifiers retain their identity");h.succeed();});});}
    @AfterBatch(batch="hazard_attributes") public static void attributesDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="hazard_nested",timeoutTicks=620) public static void shrinkingFitsTheNativeSkirtingGapThenRestoresOverThreeSeconds(GameTestHelper h){var f=new Fixture(h);var p=f.player("nested_reader",LabyrinthPlace.NESTED_ROOMS);f.at(p,LabyrinthPlace.NESTED_ROOMS,.5,0,-36);
        f.ready(()->{
        h.runAfterDelay(25,()->{h.assertTrue(Math.abs(p.getAttributeValue(Attributes.SCALE)-.34)<.001,"fourth parlour has native one-third scale");f.body(f.base(LabyrinthPlace.NESTED_ROOMS),.5,0,-38.5,.34,"actual shrunken reader exit");p.teleportTo(p.server.overworld(),0,90,0,0,0);p.hasChangedDimension();HazardVariety.onArrive(p,LabyrinthPlace.JUNCTION);});h.runAfterDelay(45,()->h.assertTrue(p.getAttributeValue(Attributes.SCALE)>.34&&p.getAttributeValue(Attributes.SCALE)<1,"leaving restores scale gradually"));h.runAfterDelay(95,()->{h.assertTrue(p.getAttribute(Attributes.SCALE).getModifier(HazardVariety.SCALE)==null&&p.getAttributeValue(Attributes.SCALE)==1,"the scale lease is completely removed after three seconds");h.succeed();});});}
    @AfterBatch(batch="hazard_nested") public static void nestedDone(ServerLevel l){close();}
    /** 0.4.259 (owner: "even shrunk down, a player is still too small to enter this place"): a reader walks the whole way, door, two arches and the skirting gap, with ordinary steps, gravity and collision, getting smaller as they go. */
    @GameTest(template="empty",batch="hazard_nested_walk",timeoutTicks=1600) public static void aReaderWalksThroughEveryParlourAndOutUnderTheSkirting(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("nested_walker",LabyrinthPlace.NESTED_ROOMS);var b=f.base(LabyrinthPlace.NESTED_ROOMS);int[] stuck={0};double[] best={0};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the parlours are resident")).thenExecute(()->{f.at(p,LabyrinthPlace.NESTED_ROOMS,.5,0,-2.5);p.setNoGravity(false);})
            .thenWaitUntil(()->{
                var aim=new Vec3(b.getX()+.5,p.getY(),b.getZ()-41.5).subtract(p.position());var step=aim.multiply(1,0,1).normalize().scale(.12);
                double vertical=p.onGround()?0:(p.getDeltaMovement().y-.08)*.98;var v=new Vec3(step.x,vertical,step.z);p.setDeltaMovement(v);p.move(MoverType.SELF,v);if(p.verticalCollision)p.setDeltaMovement(step.x,0,step.z);
                double z=p.getZ()-b.getZ();if(z<best[0]-.01){best[0]=z;stuck[0]=0;}else stuck[0]++;
                h.assertTrue(stuck[0]<100,"stuck at depth "+String.format("%.2f",z)+" at scale "+String.format("%.2f",p.getAttributeValue(Attributes.SCALE)));
                h.assertTrue(z<-40,"walking in: depth "+String.format("%.2f",z)+" at scale "+String.format("%.2f",p.getAttributeValue(Attributes.SCALE)));})
            .thenSucceed();}
    @AfterBatch(batch="hazard_nested_walk") public static void nestedWalkDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="hazard_count",timeoutTicks=660) public static void onlyTheSeventhActualOrderedLandingOpensAndPeersKeepTheirOwnCount(GameTestHelper h){var f=new Fixture(h);var p=f.player("stairs_counter",LabyrinthPlace.COUNTING_STAIR);var peer=f.player("stairs_peer",LabyrinthPlace.COUNTING_STAIR);var place=LabyrinthPlace.COUNTING_STAIR;int stack=f.d().returnDepth(p.getUUID());
        f.ready(()->{
        for(int i=1;i<=7;i++){final int landing=i;h.runAfterDelay(15+i*20,()->{var at=HazardMaps.landing(landing);f.at(p,place,.5,at.getY(),at.getZ());});}
        h.runAfterDelay(190,()->{h.assertTrue(f.own(p,place).getInt("Landings")==7&&f.own(peer,place).getInt("Landings")==0,"only actual personal landings are counted");h.assertTrue(!HazardVariety.blocksDoor(p,f.door(place,6)),"the personally counted seventh door is real");h.assertTrue(HazardVariety.blocksDoor(peer,f.door(place,6)),"another reader cannot borrow the count");h.assertTrue(f.d().returnDepth(p.getUUID())==stack,"loop gates never change the route stack");for(int i=1;i<=7;i++)h.assertTrue(HazardBlock.is(f.level.getBlockState(f.base(place).offset(5,i*2+1,-4-i*6)),HazardBlock.Kind.NUMBER_FOUR),"all seven physical plaques read four");h.succeed();});});}
    @AfterBatch(batch="hazard_count") public static void countDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="hazard_duplicates",timeoutTicks=100) public static void theRoomMissingItsClockAloneHasARealDoorAndWrongDoorsKeepTheMap(GameTestHelper h){var f=new Fixture(h);var p=f.player("duplicate_reader",LabyrinthPlace.DUPLICATE_ROOMS);var place=LabyrinthPlace.DUPLICATE_ROOMS;var b=f.base(place);int missing=HazardMaps.missingRoom(b),stack=f.d().returnDepth(p.getUUID());for(int i=0;i<3;i++){h.assertTrue(f.level.getBlockState(b.offset(5,0,-13-i*14)).isAir()==(i==missing),"only one otherwise identical room omits its clock");h.assertTrue(HazardVariety.blocksDoor(p,f.door(place,i))==(i!=missing),"the missing detail selects the physical room's true door");}h.assertTrue(f.d().returnDepth(p.getUUID())==stack,"wrong doors keep every saved return exact");h.succeed();}
    @AfterBatch(batch="hazard_duplicates") public static void duplicatesDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="hazard_mirror",timeoutTicks=100) public static void mirrorIsRecipientPrivateAndTheWrongDoorOnlyLoopsThatReader(GameTestHelper h){var f=new Fixture(h);var p=f.player("mirror_a",LabyrinthPlace.MIRROR_HALL);var peer=f.player("mirror_b",LabyrinthPlace.MIRROR_HALL);f.at(p,LabyrinthPlace.MIRROR_HALL,0,0,-36);f.at(peer,LabyrinthPlace.MIRROR_HALL,2,0,-35);h.runAfterDelay(30,()->{int real=f.own(p,LabyrinthPlace.MIRROR_HALL).getInt("MirrorDoor");var before=peer.position();h.assertTrue(HazardVariety.blocksDoor(p,f.door(LabyrinthPlace.MIRROR_HALL,(real+1)%3))&&peer.position().equals(before),"a false mirror door moves only its reader");h.assertTrue(!HazardVariety.blocksDoor(p,f.door(LabyrinthPlace.MIRROR_HALL,real)),"the reflection's selected door is real");h.assertTrue(f.level.getEntitiesOfClass(NovelActor.class,new AABB(f.base(LabyrinthPlace.MIRROR_HALL)).inflate(60)).isEmpty(),"no server-side reflection leaks to other clients");h.succeed();});}
    @AfterBatch(batch="hazard_mirror") public static void mirrorDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="hazard_quiet",timeoutTicks=650) public static void realSculkVibrationsFillASharedMeterAndTheHarmlessSweepReturnsEveryPassedReader(GameTestHelper h){var f=new Fixture(h);var p=f.player("quiet_noisy",LabyrinthPlace.QUIET_HALL);var peer=f.player("quiet_peer",LabyrinthPlace.QUIET_HALL);f.at(p,LabyrinthPlace.QUIET_HALL,.5,0,-33);f.at(peer,LabyrinthPlace.QUIET_HALL,2,0,-30);var before=p.getHealth();
        for(int n=0;n<6;n++)h.runAfterDelay(25+n*50,()->f.level.gameEvent(net.minecraft.world.level.gameevent.GameEvent.STEP,p.position(),net.minecraft.world.level.gameevent.GameEvent.Context.of(p)));
        h.runAfterDelay(370,()->{h.assertTrue(f.room(LabyrinthPlace.QUIET_HALL).getInt("Generation")>0,"real sensor activations start a shared sweep");h.assertTrue(p.getZ()-f.base(LabyrinthPlace.QUIET_HALL).getZ()>-4&&peer.getZ()-f.base(LabyrinthPlace.QUIET_HALL).getZ()>-4,"the harmless native walker has passed and returned both readers");h.assertTrue(p.getHealth()==before,"the walker never attacks or damages readers");h.succeed();});}
    @AfterBatch(batch="hazard_quiet") public static void quietDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="hazard_listing",timeoutTicks=1000) public static void nativeListingSlidesDroppedItemsAndCrouchingOrHoldingTheRailStopsTheReader(GameTestHelper h){var f=new Fixture(h);var p=f.player("ship_reader",LabyrinthPlace.LISTING_HALL);f.at(p,LabyrinthPlace.LISTING_HALL,0,0,-18);p.setShiftKeyDown(true);var b=f.base(LabyrinthPlace.LISTING_HALL);final ItemEntity[] dropped={null};
        // The list is a sine on the hall's own occupied clock (a 600-tick swing), which starts once its chunks and entity sections are loaded
        // and may already be partway through a swing; drop the item early in a rising half, so it is still rising when the drift is measured.
        h.startSequence().thenWaitUntil(()->{long swing=Math.floorMod(f.room(LabyrinthPlace.LISTING_HALL).getLong("Clock"),600);h.assertTrue(swing>=20&&swing<=150,"wait for the listing hall's clock to start a rising swing: "+swing);}).thenExecute(()->{dropped[0]=new ItemEntity(f.level,b.getX()+.5,b.getY()+1,b.getZ()-18.5,new ItemStack(Items.PAPER));dropped[0].setNoGravity(true);dropped[0].setDeltaMovement(Vec3.ZERO);dropped[0].setPickUpDelay(100);h.assertTrue(f.level.addFreshEntity(dropped[0]),"a real dropped item registers in the occupied corridor");var item=dropped[0];
        h.runAfterDelay(55,()->{h.assertTrue(item.getX()>b.getX()+.6,"native dropped items drift with the listing corridor");h.assertTrue(Math.abs(p.getDeltaMovement().x)<.01,"crouching holds the reader against the list");p.setShiftKeyDown(false);var at=b.offset(2,0,-18);f.at(p,LabyrinthPlace.LISTING_HALL,1.5,0,-18);HazardVariety.rail(new PlayerInteractEvent.RightClickBlock(p,InteractionHand.MAIN_HAND,at,new BlockHitResult(at.getCenter(),Direction.WEST,at,false)));});
        h.runAfterDelay(70,()->{h.assertTrue(Math.abs(p.getDeltaMovement().x)<.01,"a native rail interaction holds footing");h.succeed();});});}
    @AfterBatch(batch="hazard_listing") public static void listingDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="hazard_listing_grip",timeoutTicks=1000)
    public static void aListingHallRailHoldsOnlyItsReaderWithinReachWhileItStillStands(GameTestHelper h){railAnchor(h,LabyrinthPlace.LISTING_HALL);}
    @AfterBatch(batch="hazard_listing_grip") public static void listingGripDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="hazard_gravity_grip",timeoutTicks=1000)
    public static void aTurningGravityRailAlsoLetsGoWhenItsReaderWalksAway(GameTestHelper h){railAnchor(h,LabyrinthPlace.TURNING_GRAVITY);}
    @AfterBatch(batch="hazard_gravity_grip") public static void gravityGripDone(ServerLevel l){close();}
    private static void railAnchor(GameTestHelper h,LabyrinthPlace place){
        var f=new Fixture(h);var p=f.player("rail_reader",place);var peer=f.player("rail_peer",place);var b=f.base(place);var l=f.level;
        boolean listing=place==LabyrinthPlace.LISTING_HALL;
        var at=b.offset(listing?2:11,0,listing?-18:-6);double nearX=listing?1.5:10.5,farX=nearX-3,z=listing?-18:-5.5;
        f.at(p,place,nearX,0,z);f.at(peer,place,farX,0,z);
        final long[] heldAt={0};
        Runnable grip=()->{var event=new PlayerInteractEvent.RightClickBlock(p,InteractionHand.MAIN_HAND,at,new BlockHitResult(at.getCenter(),Direction.WEST,at,false));HazardVariety.rail(event);h.assertTrue(event.isCanceled(),"the actual nearby rail accepts the reader's hand");heldAt[0]=h.getTick();};
        h.startSequence().thenWaitUntil(()->{long clock=f.room(place).getLong("Clock");h.assertTrue(clock>=40&&clock<=140,"a loaded room's first sustained pull: "+clock);})
            .thenExecute(()->{p.setDeltaMovement(Vec3.ZERO);peer.setDeltaMovement(Vec3.ZERO);grip.run();})
            .thenIdle(10).thenExecute(()->{
                h.assertTrue(Math.abs(p.getDeltaMovement().x)<.01&&peer.getDeltaMovement().x>.01,"holding the rail secures only that reader, not their peer");
                f.at(p,place,farX,0,z);
            }).thenWaitUntil(()->h.assertTrue(p.getDeltaMovement().x>.01,"walking beyond reach releases the grip before its forty-tick lease expires"))
            .thenExecute(()->{h.assertTrue(h.getTick()-heldAt[0]<40,"walking away must release the grip before ordinary expiry");f.at(p,place,nearX,0,z);})
            .thenWaitUntil(()->h.assertTrue(p.getDeltaMovement().x>.01,"returning within reach still requires taking hold again"))
            .thenExecute(()->{h.assertTrue(h.getTick()-heldAt[0]<40,"a released grip cannot silently return within the old lease");f.at(p,place,nearX,0,z);grip.run();})
            .thenIdle(5).thenExecute(()->{
                h.assertTrue(Math.abs(p.getDeltaMovement().x)<.01,"the same real rail can be gripped again");
                l.setBlock(at,Blocks.AIR.defaultBlockState(),2);
            }).thenWaitUntil(()->h.assertTrue(p.getDeltaMovement().x>.01,"a removed rail no longer holds the reader"))
            .thenExecute(()->{h.assertTrue(h.getTick()-heldAt[0]<40,"removing the rail must release the grip before ordinary expiry");p.setShiftKeyDown(true);p.setDeltaMovement(Vec3.ZERO);})
            .thenIdle(5).thenExecute(()->h.assertTrue(Math.abs(p.getDeltaMovement().x)<.01,"crouching remains an independent way to secure footing"))
            .thenSucceed();
    }
    @GameTest(template="empty",batch="hazard_receding",timeoutTicks=700) public static void backingAwayBringsTheActualDoorIntoReachWithoutChangingItsSavedDeal(GameTestHelper h){var f=new Fixture(h);var p=f.player("receding_reader",LabyrinthPlace.RECEDING_HALL);var b=f.base(LabyrinthPlace.RECEDING_HALL);f.at(p,LabyrinthPlace.RECEDING_HALL,.5,0,-10);var door=f.door(LabyrinthPlace.RECEDING_HALL,0);f.d().deal(p.getUUID(),door,LabyrinthPlace.JUNCTION.id(),false);var saved=f.d().deal(p.getUUID(),door);final int[] phase={1},away={-34};
        f.ready(()->{
        p.setYRot(180);h.onEachTick(()->{if(phase[0]==2&&p.position().distanceTo(f.door(LabyrinthPlace.RECEDING_HALL,0).lower.getCenter())<3.1)phase[0]=0;if(phase[0]>0)p.moveTo(p.getX(),p.getY(),p.getZ()+(phase[0]==1?-.08:.08));});
        h.runAfterDelay(65,()->{away[0]=f.room(LabyrinthPlace.RECEDING_HALL).getInt("DoorZ");h.assertTrue(away[0]<-34,"walking at the far door physically recedes it");phase[0]=2;p.setYRot(180);});
        h.runAfterDelay(195,()->{phase[0]=0;var current=f.door(LabyrinthPlace.RECEDING_HALL,0);h.assertTrue(current.lower.getZ()-b.getZ()>away[0]&&p.position().distanceTo(current.lower.getCenter())<4.5,"walking backwards brings the real door close enough to use");h.assertTrue(f.level.getBlockState(current.lower).getBlock() instanceof DoorBlock&&saved.equals(f.d().deal(p.getUUID(),current)),"the physical moving door keeps its stable saved destination");h.succeed();});});}
    @AfterBatch(batch="hazard_receding") public static void recedingDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="hazard_sconce",timeoutTicks=1000) public static void theOnlySconceFollowsTheWarmTorchWhileColdPortableLightsDie(GameTestHelper h){var f=new Fixture(h);var p=f.player("sconce_reader",LabyrinthPlace.LAST_SCONCE);var b=f.base(LabyrinthPlace.LAST_SCONCE);var warm=b.offset(8,0,-34);var cold=b.offset(-8,0,-34);var ceiling=b.offset(-8,4,-1);f.level.setBlock(warm,Blocks.TORCH.defaultBlockState(),3);f.level.setBlock(cold,Blocks.SOUL_TORCH.defaultBlockState(),3);
        f.ready(()->{
        h.runAfterDelay(180,()->{h.assertTrue(f.level.getBlockState(warm).is(Blocks.TORCH)&&f.level.getBlockState(cold).isAir(),"the far wall's warm anchor survives while the cold portable flame expires");h.assertTrue(HazardBlock.is(f.level.getBlockState(b.offset(8,2,-34)),HazardBlock.Kind.SCONCE)&&f.level.getBlockState(b.offset(-8,2,-7)).isAir(),"the one native sconce follows a warm flame at the room's far edge");f.level.setBlock(ceiling,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true),3);});
        h.runAfterDelay(350,()->{h.assertTrue(f.level.getBlockState(ceiling).is(Blocks.LANTERN)&&f.level.getBlockState(warm).isAir(),"the stronger ceiling lantern becomes the anchor and the weaker torch expires");h.assertTrue(HazardBlock.is(f.level.getBlockState(b.offset(-8,2,-1)),HazardBlock.Kind.SCONCE)&&f.level.getBlockState(b.offset(8,2,-34)).isAir(),"the sconce reaches the ceiling anchor at the entrance edge");f.level.setBlock(ceiling,Blocks.AIR.defaultBlockState(),3);f.level.setBlock(cold,Blocks.SOUL_TORCH.defaultBlockState(),3);});
        h.runAfterDelay(520,()->{h.assertTrue(f.level.getBlockState(cold).isAir()&&HazardBlock.is(f.level.getBlockState(b.offset(-8,2,-1)),HazardBlock.Kind.SCONCE),"cold flames also expire when there is no warm anchor, leaving the one sconce");h.succeed();});});}
    @AfterBatch(batch="hazard_sconce") public static void sconceDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="hazard_pressure",timeoutTicks=260) public static void cornerPressureCostsHealthButNeverClosesOnAReaderOrTakesTheirLastThreeHearts(GameTestHelper h){var f=new Fixture(h);var p=f.player("pressure_reader",LabyrinthPlace.CORNER_PRESSURE);var b=f.base(LabyrinthPlace.CORNER_PRESSURE);f.at(p,LabyrinthPlace.CORNER_PRESSURE,1.15,0,-11.5);p.setHealth(10);h.onEachTick(()->p.moveTo(b.getX()+1.15,b.getY(),b.getZ()-11.5));
        h.runAfterDelay(200,()->{h.assertTrue(p.getHealth()<10&&p.getHealth()>=6,"native compression retains its health cost and three-heart floor");h.assertTrue(f.level.getBlockState(b.offset(1,0,-12)).isAir()&&f.level.noCollision(p,p.getBoundingBox()),"the closing native cells remain clear around an occupied reader");for(var at:HazardMaps.pressureRoute())f.body(b,at.getX()+.5,0,at.getZ()+.5,1,"the retreat lane around the corner after compression at "+at);h.succeed();});}
    @AfterBatch(batch="hazard_pressure") public static void pressureDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="return_air",timeoutTicks=500)
    public static void returnAirPullsAnExposedReaderWhileASecondBraces(GameTestHelper h){
        var f=new Fixture(h);
        var place=LabyrinthPlace.RETURN_AIR;
        var reader=f.player("air_exposed",place);
        var peer=f.player("air_braced",place);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"return air is loaded"))
                .thenExecute(()->{
                    f.at(reader,place,.5,0,-8.5);
                    f.at(peer,place,1.5,0,-8.5);
                    peer.setShiftKeyDown(true);
                })
                .thenWaitUntil(()->h.assertTrue(reader.getDeltaMovement().x>.1,"the open floor pulls toward the first grille"))
                .thenExecute(()->h.assertTrue(Math.abs(peer.getDeltaMovement().x)<.01&&reader.getHealth()==20,
                        "crouching anchors the peer and the draft costs ground"))
                .thenSucceed();
    }
    @AfterBatch(batch="return_air") public static void returnAirDone(ServerLevel level){close();}

    @GameTest(template="empty",batch="settling_cellar",timeoutTicks=500)
    public static void sharedWearOpensOnlyTheLoadedFloorPanel(GameTestHelper h){
        var f=new Fixture(h);
        var place=LabyrinthPlace.SETTLING_CELLAR;
        var reader=f.player("cellar_runner",place);
        var peer=f.player("cellar_bank",place);
        var base=f.base(place);
        var seam=base.offset(1,-1,-8);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the settling cellar is loaded"))
                .thenExecute(()->{
                    f.at(reader,place,1.5,0,-8.5);
                    reader.setSprinting(true);
                    f.at(peer,place,4.5,0,-8.5);
                })
                .thenWaitUntil(()->h.assertTrue(f.level.getBlockState(seam).isAir(),"a loaded panel gives way"))
                .thenExecute(()->{
                    h.assertTrue(f.level.getBlockState(base.offset(-1,-1,-8)).is(Blocks.CRACKED_STONE_BRICKS)
                            &&f.level.getBlockState(base.offset(0,-1,-8)).is(Blocks.STRIPPED_DARK_OAK_WOOD)
                            &&f.level.getBlockState(base.offset(4,-1,-8)).is(Blocks.DARK_OAK_PLANKS),
                            "the facing panel, joist and bank stay in place for everyone");
                    f.body(base,1.5,-4,-8.5,1,"the bottom of the catch bay");
                }).thenSucceed();
    }
    @AfterBatch(batch="settling_cellar") public static void settlingCellarDone(ServerLevel level){close();}

}
