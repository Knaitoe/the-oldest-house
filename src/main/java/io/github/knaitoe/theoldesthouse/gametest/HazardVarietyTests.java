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
    /**
     * 0.4.375 (docs/TEXTURE_PASS_PAINT_0_4_375.md): the five quiet and threshold halls wear their own linoleum, paint and panelling,
     * with no vanilla shell left on any face a room shows, and the watched threshold's door, moving on, leaves the painted wall behind.
     */
    @GameTest(template="empty",batch="hazard_paint",timeoutTicks=400) public static void quietAndThresholdHallsWearTheirOwnPaint(GameTestHelper h){var f=new Fixture(h);active=f;
        var halls=List.of(LabyrinthPlace.LAST_SCONCE,LabyrinthPlace.WATCHED_THRESHOLD,LabyrinthPlace.MIRROR_HALL,LabyrinthPlace.QUIET_HALL,LabyrinthPlace.BLIND_STRETCH);
        for(var p:halls){if(p!=LabyrinthPlace.BLIND_STRETCH){f.build(p);continue;}var bounds=LabyrinthPlaces.placeBounds(f.origin,p);f.chunks.hold(f.level,new AABB(bounds.minX(),bounds.minY(),bounds.minZ(),bounds.maxX()+1,bounds.maxY()+1,bounds.maxZ()+1));
            // The blind stretch's turns are carved out of the slot's own fill, which the House lays before building it.
            var bb=f.base(p);var r=p.room();for(var at:BlockPos.betweenClosed(bb.offset(r.minX()-1,r.minY()-1,r.minZ()-1),bb.offset(r.maxX()+1,r.maxY()+1,r.maxZ())))f.level.setBlock(at,Blocks.WHITE_TERRACOTTA.defaultBlockState(),2);
            BlindStretch.build(f.level,bb);}
        f.ready(()->{
            for(var p:halls){var b=f.base(p);var box=p.room();var shell=PaintFinish.shellOf(p);int lino=0,worn=0,foot=0,wall=0,marked=0,ceiling=0,bare=0;BlockPos firstBare=null;
                for(var at:BlockPos.betweenClosed(b.offset(box.minX(),box.minY(),box.minZ()),b.offset(box.maxX(),box.maxY(),box.maxZ()))){var s=f.level.getBlockState(at);
                    if(s.is(HallRegistry.paint("lino_worn"))){lino++;worn++;}else if(s.is(HallRegistry.paint("lino")))lino++;
                    else if(s.is(HallRegistry.paint("dado"))||s.is(HallRegistry.paint("panel_low")))foot++;
                    else if(s.is(HallRegistry.paint("wall_marked"))){wall++;marked++;}else if(s.is(HallRegistry.paint("wall"))||s.is(HallRegistry.paint("panel")))wall++;
                    else if(s.is(HallRegistry.paint("ceiling")))ceiling++;
                    if(!s.isAir())continue;
                    for(var side:Direction.values()){var n=at.relative(side);if(n.getX()<b.getX()+box.minX()||n.getX()>b.getX()+box.maxX()||n.getZ()<b.getZ()+box.minZ()||n.getZ()>b.getZ()+box.maxZ())continue;if(shell.contains(f.level.getBlockState(n).getBlock())||f.level.getBlockState(n).is(Blocks.SMOOTH_STONE)){bare++;if(firstBare==null)firstBare=n.subtract(b);}}}
                boolean quiet=p==LabyrinthPlace.QUIET_HALL;
                h.assertTrue((lino>8||quiet&&lino>0)&&(worn>0||quiet)&&foot>10&&wall>30&&ceiling>20&&bare==0,p.id()+" wears its own paint: lino "+lino+" (worn "+worn+"), foot "+foot+", wall "+wall+" (marked "+marked+"), ceiling "+ceiling+", vanilla faces showing "+bare+(firstBare==null?"":" (first at "+firstBare.toShortString()+")"));}
            // The watched threshold's door moves on, and the wall it leaves behind is the painted wall, not terracotta.
            var place=LabyrinthPlace.WATCHED_THRESHOLD;var b=f.base(place);var w=f.room(place);int from=w.getInt("DoorIndex"),to=(from+1)%3;var old=b.offset(place.doors().get(from+1).rel());
            h.assertTrue(HazardVariety.moveThreshold(f.level,b,w,to),"the unwatched door moves through the painted wall");
            h.assertTrue(f.level.getBlockState(old).is(HallRegistry.paint("dado"))&&PaintFinish.is(f.level.getBlockState(old.above()))&&f.level.getBlockState(b.offset(place.doors().get(to+1).rel())).getBlock() instanceof DoorBlock,"it leaves the dado and the plaster behind it: "+f.level.getBlockState(old)+" / "+f.level.getBlockState(old.above()));
            h.succeed();});
    }
    @AfterBatch(batch="hazard_paint") public static void paintDone(ServerLevel level){close();}
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

    /**
     * 0.4.385, the still light (docs/STILL_LIGHT_0_4_385.md): the room is built as its plan says; its lamps come up only while a
     * reader holds still and go out a breath after they move; in the dark a dusty board gives way under a moving reader and the
     * worn way never does; a friend holding still lights the room for someone walking; the board is back once nobody is there; and
     * a torch will not stay alight, though anything else may be set down.
     */
    @GameTest(template="empty",batch="still_light",timeoutTicks=900)
    public static void theStillLightLightsForStillnessAndItsDustyBoardsGiveInTheDark(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.STILL_LIGHT;var walker=f.player("still_walker",place);var b=f.base(place);
        final ServerPlayer[] friend={null};final int[] step={0};
        java.util.function.BiConsumer<ServerPlayer,double[]> hold=(p,at)->{f.at(p,place,at[0],0,at[1]);p.setOnGround(true);};
        java.util.function.BiConsumer<Integer,Integer> walk=(x,z)->{f.at(walker,place,x+.5+(step[0]++%2==0?.2:-.2),0,z+.5);walker.setOnGround(true);};
        java.util.function.IntPredicate lamps=stage->TrialRooms.LAMP_Z.stream().allMatch(z->TrialBlock.is(f.level.getBlockState(b.offset(0,3,z)),TrialBlock.Kind.STILL_LAMP,stage));
        java.util.function.IntSupplier gone=()->{int n=0;for(int z=TrialRooms.FIELD_FAR;z<=TrialRooms.FIELD_NEAR;z++)for(int x=-TrialRooms.HALF;x<=TrialRooms.HALF;x++)if(TrialBlock.is(f.level.getBlockState(b.offset(x,-1,z)),TrialBlock.Kind.STILL_BOARD,1))n++;return n;};
        var board=b.offset(-4,-1,-10);var held=b.offset(-4,-1,-20);var landing=new double[]{.5,-1.5};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the still light is loaded"))
                .thenExecute(()->{
                    for(int z=TrialRooms.FIELD_FAR;z<=TrialRooms.FIELD_NEAR;z++)for(int x=-TrialRooms.HALF;x<=TrialRooms.HALF;x++){
                        var s=f.level.getBlockState(b.offset(x,-1,z));
                        if(TrialRooms.stairWell(x,z))h.assertTrue(s.isAir(),"the well over the steps is open at "+x+","+z);
                        else if(TrialRooms.onWay(x,z))h.assertTrue(s.is(TrialRegistry.material("still_path")),"the worn way at "+x+","+z);
                        else h.assertTrue(TrialBlock.is(s,TrialBlock.Kind.STILL_BOARD,0),"a dusty board at "+x+","+z);
                        if(!TrialRooms.stairWell(x,z))f.body(b,x+.5,-3,z+.5,1,"standing room in the trough under "+x+","+z);
                    }
                    var way=TrialRooms.wayInOrder();
                    h.assertTrue(way.getFirst().getZ()==TrialRooms.FIELD_NEAR&&way.getLast().getZ()==TrialRooms.FIELD_FAR,"the way runs from the entry landing to the far one");
                    for(int i=1;i<way.size();i++)h.assertTrue(way.get(i).distManhattan(way.get(i-1))==1,"the way is one unbroken walk at "+way.get(i));
                    f.body(b,3.5,-2,-3.5,1,"the first step up out of the trough");f.body(b,3.5,-1,-2.5,1,"the second step up");
                    f.body(b,3.5,0,-1.5,1,"the entry landing above the steps");f.body(b,.5,0,-42.5,1,"the far landing");
                    // 0.4.403: and a way up against each side wall partway down, so a reader who falls can climb back out where they are.
                    f.body(b,4.5,-2,-16.5,1,"the east way up's first step");f.body(b,4.5,-1,-15.5,1,"its second step, under its hole");
                    f.body(b,-3.5,-2,-28.5,1,"the west way up's first step");f.body(b,-3.5,-1,-27.5,1,"its second step, under its hole");
                    h.assertTrue(TrialRooms.stairWell(4,-16)&&TrialRooms.stairWell(-4,-28)&&f.level.getBlockState(b.offset(4,-3,-18)).is(Blocks.LIGHT),"each way up shows a faint glow at its foot");
                    h.assertTrue(f.level.getBlockState(b.offset(0,0,TrialRooms.FAR_WALL)).getBlock() instanceof DoorBlock,"the far door stands in the end wall");
                    for(int z:TrialRooms.LAMP_Z)h.assertTrue(TrialBlock.is(f.level.getBlockState(b.offset(0,3,z)),TrialBlock.Kind.STILL_LAMP,0)
                            &&TrialRegistry.isMaterial(f.level.getBlockState(b.offset(0,4,z))),"a dark lamp hangs from the ceiling at "+z);
                    h.assertTrue(TrialMarks.kind(place)==place&&HazardVariety.tier(place)==1&&LabyrinthPacing.physicalTrial(place),"a first-tier physical trial, and a mark of its own");
                    hold.accept(walker,landing);
                })
                .thenExecuteFor(TrialRooms.STILL+4,()->hold.accept(walker,landing))
                .thenWaitUntil(()->{h.assertTrue(lamps.test(1),"holding still brings every lamp up");
                    h.assertTrue(f.level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK,b.offset(-4,0,-20))>=8,"and the middle of the room can be seen");})
                .thenExecuteFor(TrialRooms.FADE+TrialRooms.GIVE+4,()->walk.accept(-4,-10))
                .thenExecute(()->{
                    h.assertTrue(lamps.test(0),"moving puts the lamps out a breath later");
                    h.assertTrue(TrialBlock.is(f.level.getBlockState(board),TrialBlock.Kind.STILL_BOARD,1)&&gone.getAsInt()==1,"the dusty board under a reader in the dark gives way, and only that one");
                    f.body(b,-3.5,-3,-9.5,1,"the trough the reader falls into");
                })
                .thenExecuteFor(12,()->walk.accept(2,-10))
                .thenExecute(()->{
                    h.assertTrue(gone.getAsInt()==1&&f.level.getBlockState(b.offset(2,-1,-10)).is(TrialRegistry.material("still_path")),"the worn way holds in the dark");
                    friend[0]=f.player("still_friend",place);hold.accept(friend[0],landing);hold.accept(walker,new double[]{2.5,-9.5});
                })
                .thenExecuteFor(TrialRooms.STILL+2,()->{hold.accept(friend[0],landing);hold.accept(walker,new double[]{2.5,-9.5});})
                .thenExecute(()->h.assertTrue(lamps.test(1),"the lamps are up"))
                .thenExecuteFor(20,()->{hold.accept(friend[0],landing);walk.accept(-4,-20);})
                .thenExecute(()->{
                    h.assertTrue(lamps.test(1)&&TrialBlock.is(f.level.getBlockState(held),TrialBlock.Kind.STILL_BOARD,0),"while a friend holds still, a dusty board holds for someone walking on it");
                    hold.accept(walker,landing);
                })
                .thenWaitUntil(()->h.assertTrue(gone.getAsInt()==0,"the board that gave is back once nobody is where it was"))
                .thenExecute(()->{
                    var at=b.offset(1,0,-1);
                    // The House lets a reader set a torch down almost anywhere (LabyrinthLighting); here it goes out.
                    h.assertTrue(LabyrinthLighting.allowsPlacing(f.level,at,Blocks.TORCH.defaultBlockState()),"the House's own rule would let a torch be set down here");
                    f.level.setBlock(at,Blocks.TORCH.defaultBlockState(),3);
                    var torch=new net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent(net.neoforged.neoforge.common.util.BlockSnapshot.create(f.level.dimension(),f.level,at),Blocks.AIR.defaultBlockState(),walker);
                    net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(torch);
                    f.level.setBlock(at,Blocks.AIR.defaultBlockState(),3);
                    h.assertTrue(torch.isCanceled(),"but a torch will not stay alight in the still light");
                }).thenSucceed();
    }
    @AfterBatch(batch="still_light") public static void stillLightDone(ServerLevel level){close();}

    /**
     * 0.4.386, the soft floor (docs/SOFT_FLOOR_0_4_386.md): the room is built as its plan says, with beams to crouch under and
     * half-walls to wind past; a square under a reader who stands still sinks a little, then more, and gives way at two seconds; a
     * reader who keeps walking never sinks one past its first stage; two on one square sink it twice as fast; and every square is
     * back once nobody is on it.
     */
    @GameTest(template="empty",batch="soft_floor",timeoutTicks=900)
    public static void theSoftFloorGivesUnderAPauseAndHoldsAReaderWhoKeepsMoving(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.SOFT_FLOOR;var b=f.base(place);
        java.util.function.BiConsumer<ServerPlayer,double[]> hold=(p,at)->{f.at(p,place,at[0],0,at[1]);p.setOnGround(true);};
        var landing=new double[]{.5,-1.5};var still=b.offset(-2,-1,-6);var shared=b.offset(1,-1,-20);
        final ServerPlayer[] who=new ServerPlayer[4];final long[] at={0};final double[] walked={-5.5};
        for(int i=0;i<4;i++)who[i]=f.player("soft_reader_"+i,place);
        java.util.function.Function<BlockPos,Integer> stageAt=pos->{var s=f.level.getBlockState(pos);return TrialBlock.is(s,TrialBlock.Kind.SOFT_PLATE)?s.getValue(TrialBlock.STAGE):-1;};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the soft floor is loaded"))
                .thenExecute(()->{
                    for(var p:who)hold.accept(p,landing);
                    for(int z=TrialRooms.SOFT_FAR;z<=TrialRooms.SOFT_NEAR;z++)for(int x=-TrialRooms.SOFT_HALF;x<=TrialRooms.SOFT_HALF;x++){
                        var s=f.level.getBlockState(b.offset(x,-1,z));
                        if(TrialRooms.softWell(x,z))h.assertTrue(s.isAir(),"the well over the steps is open at "+x+","+z);
                        else if(TrialRooms.halfWall(x,z))h.assertTrue(s.is(TrialRegistry.material("soft_wall"))&&f.level.getBlockState(b.offset(x,3,z)).is(TrialRegistry.material("soft_wall")),"a half-wall stands at "+x+","+z);
                        else h.assertTrue(TrialBlock.is(s,TrialBlock.Kind.SOFT_PLATE,0),"a firm square at "+x+","+z);
                        if(!TrialRooms.softWell(x,z))f.body(b,x+.5,-3,z+.5,1,"standing room in the passage under "+x+","+z);
                    }
                    // 0.4.403: a way up against each side wall partway down the passage.
                    f.body(b,3.5,-2,-19.5,1,"the east way up's first step");f.body(b,3.5,-1,-18.5,1,"its second step, under its hole");
                    f.body(b,-2.5,-2,-31.5,1,"the west way up's first step");f.body(b,-2.5,-1,-30.5,1,"its second step, under its hole");
                    for(int z:TrialRooms.BEAM_Z)for(int x=-TrialRooms.SOFT_HALF;x<=TrialRooms.SOFT_HALF;x++){
                        h.assertTrue(TrialBlock.is(f.level.getBlockState(b.offset(x,1,z)),TrialBlock.Kind.SOFT_BEAM),"a beam across the room at "+x+","+z);
                        f.body(b,x+.5,0,z+.5,1.5/1.8,"room to crouch under the beam at "+x+","+z);
                        h.assertTrue(!f.level.noCollision(null,new AABB(b.getX()+x+.2,b.getY()+.005,b.getZ()+z+.2,b.getX()+x+.8,b.getY()+1.8,b.getZ()+z+.8)),"but not to walk upright under it at "+x+","+z);
                    }
                    f.body(b,2.5,0,-15.5,1,"the way past the first half-wall");f.body(b,-1.5,0,-27.5,1,"the way past the second, on the other side");
                    f.body(b,2.5,-2,-3.5,1,"the first step up out of the passage");f.body(b,2.5,-1,-2.5,1,"the second step up");f.body(b,.5,0,-40.5,1,"the far landing");
                    h.assertTrue(f.level.getBlockState(b.offset(0,0,TrialRooms.SOFT_FAR_WALL)).getBlock() instanceof DoorBlock,"the far door stands in the end wall");
                    for(int z:TrialRooms.SOFT_LAMP_Z)h.assertTrue(TrialBlock.is(f.level.getBlockState(b.offset(0,3,z)),TrialBlock.Kind.STILL_LAMP,1)&&TrialRegistry.isMaterial(f.level.getBlockState(b.offset(0,4,z))),"a lit lamp hangs from the ceiling at "+z);
                    h.assertTrue(TrialMarks.kind(place)==place&&HazardVariety.tier(place)==1&&LabyrinthPacing.physicalTrial(place),"a first-tier physical trial, and a mark of its own");
                    hold.accept(who[0],new double[]{-1.5,-5.5});at[0]=h.getTick();
                })
                .thenExecuteFor(TrialRooms.SINK-4,()->{})
                .thenExecute(()->h.assertTrue(stageAt.apply(still)==0,"a moment's standing does nothing yet"))
                .thenWaitUntil(()->h.assertTrue(stageAt.apply(still)==1,"the square sinks a little"))
                .thenWaitUntil(()->h.assertTrue(stageAt.apply(still)==2,"then more"))
                .thenWaitUntil(()->h.assertTrue(stageAt.apply(still)==3,"then gives way"))
                .thenExecute(()->{long took=h.getTick()-at[0];
                    h.assertTrue(took>=TrialRooms.DROP-2&&took<=TrialRooms.DROP+6,"at two seconds' standing ("+took+" ticks)");
                    f.body(b,-1.5,-3,-5.5,1,"the passage the reader drops into");hold.accept(who[0],landing);})
                .thenExecuteFor(32,()->{walked[0]-=.22;hold.accept(who[1],new double[]{2.5,walked[0]});})
                .thenExecute(()->{
                    for(int z=-5;z>=-12;z--){int st=stageAt.apply(b.offset(2,-1,z));h.assertTrue(st>=0&&st<=1,"a reader who keeps walking never sinks a square past its first stage (z "+z+": "+st+")");}
                    hold.accept(who[1],landing);hold.accept(who[2],new double[]{1.5,-19.5});hold.accept(who[3],new double[]{1.4,-19.6});at[0]=h.getTick();
                })
                .thenWaitUntil(()->h.assertTrue(stageAt.apply(shared)==3,"two on one square sink it through"))
                .thenExecute(()->{long took=h.getTick()-at[0];
                    h.assertTrue(took<=TrialRooms.DROP/2+6,"twice as fast ("+took+" ticks)");
                    hold.accept(who[2],landing);hold.accept(who[3],landing);})
                .thenWaitUntil(()->{for(int z=TrialRooms.SOFT_FAR;z<=TrialRooms.SOFT_NEAR;z++)for(int x=-TrialRooms.SOFT_HALF;x<=TrialRooms.SOFT_HALF;x++){
                    int st=stageAt.apply(b.offset(x,-1,z));h.assertTrue(st<=0,"every square is back once nobody is on it ("+x+","+z+": "+st+")");}})
                .thenSucceed();
    }
    @AfterBatch(batch="soft_floor") public static void softFloorDone(ServerLevel level){close();}

    /**
     * 0.4.387, the rising stair (docs/RISING_STAIR_0_4_387.md): the shaft is built as its plan says, every tread a walkable half
     * step above the last and each gap a single stride; the water rises a layer every RISE ticks while anyone is in the shaft,
     * closing over the treads; its undertow pulls a reader down; a reader out of air under it is back on the landing; the pockets
     * keep their air when the shaft is full; and the water goes down once nobody is in the shaft.
     */
    @GameTest(template="empty",batch="rising_stair",timeoutTicks=1700)
    public static void theRisingStairFloodsBehindAClimberAndPutsTheDrownedBackOnTheLanding(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.RISING_STAIR;var climber=f.player("rising_climber",place);var b=f.base(place);
        java.util.function.BiConsumer<ServerPlayer,double[]> hold=(p,at)->{f.at(p,place,at[0],0,at[1]);p.setOnGround(true);};
        var landing=new double[]{.5,-1.5};var floor=new double[]{-.5,-6.5};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the rising stair is loaded"))
                .thenExecute(()->{
                    int last=-1;
                    for(int i=0;i<TrialRooms.TREADS;i++){var at=TrialRooms.tread(i);var s=f.level.getBlockState(b.offset(at));
                        if(TrialRooms.GAPS.contains(i)){h.assertTrue(s.isAir()&&!TrialRooms.GAPS.contains(i+1),"a single stride's gap at tread "+i);continue;}
                        h.assertTrue(TrialBlock.is(s,TrialBlock.Kind.RISING_TREAD,TrialRooms.halfTread(i)?1:0),"tread "+i+" stands at "+at);
                        f.body(b,at.getX()+.5,TrialRooms.treadTop2(i)/2.0,at.getZ()+.5,1,"headroom over tread "+i);
                        if(last>=0)h.assertTrue(TrialRooms.treadTop2(i)-TrialRooms.treadTop2(last)<=2&&TrialRooms.tread(i).distManhattan(TrialRooms.tread(last))<=4,"tread "+i+" is a step or a stride on from tread "+last);
                        last=i;
                    }
                    h.assertTrue(TrialRooms.treadTop2(TrialRooms.TREADS-1)==44&&!TrialRooms.GAPS.contains(TrialRooms.TREADS-1),"the last tread is level with the landing at the head");
                    for(int x=0;x<=3;x++)for(int z=TrialRooms.SHAFT_FAR;z<=TrialRooms.SHAFT_FAR+1;z++)h.assertTrue(f.level.getBlockState(b.offset(x,21,z)).is(TrialRegistry.material("rising_landing")),"the landing at the head at "+x+","+z);
                    f.body(b,1.5,22,-11.5,1,"standing room on the landing at the head");
                    h.assertTrue(f.level.getBlockState(b.offset(1,22,-13)).getBlock() instanceof DoorBlock&&f.level.getBlockState(b.offset(0,0,-3)).getBlock() instanceof DoorBlock,"the far door at the head, and a door between the landing at the foot and the shaft");
                    f.body(b,.5,0,-1.5,1,"the landing at the foot");f.body(b,-.5,0,-6.5,1,"the shaft floor");
                    for(var door:List.of(TrialRooms.POCKET_LOW,TrialRooms.POCKET_HIGH)){
                        for(var at:TrialRooms.pocketWater(door))h.assertTrue(f.level.getBlockState(b.offset(at)).isAir(),"a pocket's way in at "+at);
                        var top=TrialRooms.pocketAir(door);
                        for(var at:top){h.assertTrue(f.level.getBlockState(b.offset(at)).isAir(),"a pocket's air at "+at);
                            for(var d:List.of(net.minecraft.core.Direction.NORTH,net.minecraft.core.Direction.SOUTH,net.minecraft.core.Direction.EAST,net.minecraft.core.Direction.WEST))
                                h.assertTrue(!f.level.getBlockState(b.offset(at).relative(d)).isAir(),"the pocket is closed round its air at "+at+" "+d);}
                        h.assertTrue(!f.level.getBlockState(b.offset(top.getLast()).above()).isAir(),"and over it");
                    }
                    h.assertTrue(f.level.getBlockState(b.offset(TrialRooms.tread(7))).getValue(TrialBlock.KIND)==TrialBlock.Kind.RISING_TREAD&&TrialRooms.tread(7).above().east().equals(TrialRooms.POCKET_LOW)
                            &&TrialRooms.tread(23).above().west().equals(TrialRooms.POCKET_HIGH),"each pocket opens off a tread, a step to the side");
                    for(int y:List.of(2,8,14,20))h.assertTrue(f.level.getBlockState(b.offset(5,y,-11)).getLightEmission()>0,"a lit window at "+y);
                    h.assertTrue(TrialRooms.water(f.level,b)==0&&TrialMarks.kind(place)==place&&HazardVariety.tier(place)==2&&LabyrinthPacing.physicalTrial(place),"dry to begin with; a second-tier physical trial, and a mark of its own");
                    hold.accept(climber,floor);
                })
                .thenExecuteFor(TrialRooms.RISE*3+3,()->{})
                .thenExecute(()->{int w=TrialRooms.water(f.level,b);
                    h.assertTrue(w>=2&&w<=4,"with a reader in the shaft the water rises a layer every "+TrialRooms.RISE+" ticks ("+w+")");
                    h.assertTrue(f.level.getBlockState(b.offset(TrialRooms.tread(0))).getValue(TrialBlock.WATERLOGGED)&&f.level.getBlockState(b.offset(TrialRooms.tread(1))).getValue(TrialBlock.WATERLOGGED),"and closes over the treads");
                    h.assertTrue(climber.getDeltaMovement().y<0,"its undertow pulls a reader down");
                    climber.setAirSupply(0);
                })
                .thenWaitUntil(()->h.assertTrue(climber.getZ()-b.getZ()>-2.5&&climber.getAirSupply()==climber.getMaxAirSupply(),"out of air under the water, the reader is back on the landing at the foot"))
                .thenWaitUntil(()->h.assertTrue(TrialRooms.water(f.level,b)==0&&!f.level.getBlockState(b.offset(TrialRooms.tread(0))).getValue(TrialBlock.WATERLOGGED),"once nobody is in the shaft the water goes down"))
                .thenExecute(()->{
                    TrialRooms.flood(f.level,b,TrialRooms.LAYERS);
                    h.assertTrue(TrialRooms.water(f.level,b)==TrialRooms.LAYERS&&!f.level.getFluidState(b.offset(1,22,-11)).is(net.minecraft.tags.FluidTags.WATER),"full, the water stops short of the landing at the head");
                })
                .thenExecuteFor(6,()->{})
                .thenExecute(()->{for(var door:List.of(TrialRooms.POCKET_LOW,TrialRooms.POCKET_HIGH)){
                        for(var at:TrialRooms.pocketWater(door))h.assertTrue(f.level.getBlockState(b.offset(at)).is(Blocks.WATER),"the water fills a pocket's way in at "+at);
                        for(var at:TrialRooms.pocketAir(door))h.assertTrue(f.level.getBlockState(b.offset(at)).isAir(),"but not the air above it at "+at);}})
                .thenWaitUntil(()->h.assertTrue(TrialRooms.water(f.level,b)==0,"and drains again with nobody in the shaft"))
                .thenExecute(()->{var at=Vec3.atLowerCornerOf(b);var t=TrialRooms.tread(20);
                    h.assertTrue(TrialRooms.onStair(b,at.add(-.5,0,-6.5))&&TrialRooms.onStair(b,at.add(t.getX()+.5,TrialRooms.treadTop2(20)/2.0,t.getZ()+.5)),"the shaft floor and the treads are the stair");
                    h.assertTrue(!TrialRooms.onStair(b,at.add(6.5,4,-7.5))&&!TrialRooms.onStair(b,at.add(1.5,22,-11.5))&&!TrialRooms.onStair(b,at.add(.5,0,-1.5)),"a pocket, the landing at the head and the landing at the foot are not");
                    f.at(climber,LabyrinthPlace.SOFT_FLOOR,.5,0,-1.5);
                    TrialRooms.flood(f.level,b,TrialRooms.LAYERS);
                })
                .thenExecuteFor(10,()->{})
                .thenExecute(()->{h.assertTrue(TrialRooms.water(f.level,b)==TrialRooms.LAYERS,"with nobody in the room, nothing in it changes");hold.accept(climber,landing);})
                .thenExecuteFor(2,()->{})
                .thenExecute(()->{h.assertTrue(TrialRooms.water(f.level,b)==0,"coming back to it, the shaft is as dry as before anyone came");
                    f.at(climber,place,6.5,4,-7.5);climber.setOnGround(true);TrialRooms.flood(f.level,b,10);})
                .thenExecuteFor(3,()->{})
                .thenExecute(()->h.assertTrue(TrialRooms.water(f.level,b)==10&&f.level.getBlockState(b.offset(6,4,-8)).is(Blocks.WATER)&&f.level.getBlockState(b.offset(6,6,-8)).isAir(),"the water stands in the pocket's way in, under its air"))
                .thenWaitUntil(()->h.assertTrue(TrialRooms.water(f.level,b)==0,"with its only reader in a pocket, the water goes down"))
                .thenSucceed();
    }
    @AfterBatch(batch="rising_stair") public static void risingStairDone(ServerLevel level){close();}

    /**
     * 0.4.388, the hall of many lengths (docs/MANY_LENGTHS_0_4_388.md): five lengths, each the same block for block as the next,
     * lit and walkable to the far door; a reader who walks the whole way is never moved back; one who sprints or jumps through a
     * middle length walks it again, one length back and facing the same way; nobody is moved back while someone else can see it;
     * the first and last lengths are free.
     */
    @GameTest(template="empty",batch="many_lengths",timeoutTicks=900)
    public static void theHallOfManyLengthsSendsBackAReaderWhoHurries(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.MANY_LENGTHS;var walker=f.player("lengths_walker",place);var runner=f.player("lengths_runner",place);var b=f.base(place);
        var route=new ArrayList<Vec3>();
        for(int k=0;k<TrialRooms.LENGTHS;k++){var o=TrialRooms.lengthOrigin(k);
            for(double z=o.getZ()+.5;z>=o.getZ()-9.5;z-=.5)route.add(new Vec3(o.getX()+.5,0,z));
            for(double x=o.getX()+1;x<=o.getX()+3.5;x+=.5)route.add(new Vec3(x,0,o.getZ()-9.5));
            for(double z=o.getZ()-10;z>=o.getZ()-(k==TrialRooms.LENGTHS-1?10.5:11.5);z-=.5)route.add(new Vec3(o.getX()+3.5,0,z));}
        var step=new int[]{0};
        java.util.function.BiConsumer<ServerPlayer,BlockPos> at=(p,rel)->{f.at(p,place,rel.getX()+.5,0,rel.getZ()+.5);p.setOnGround(true);};
        java.util.function.Function<Integer,BlockPos> start=k->TrialRooms.lengthOrigin(k);
        java.util.function.Function<Integer,BlockPos> mid=k->TrialRooms.lengthOrigin(k).offset(0,0,-4);
        java.util.function.Function<Integer,BlockPos> bend=k->TrialRooms.lengthOrigin(k).offset(3,0,-10);
        var mark=new double[3];
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the hall of many lengths is loaded"))
                .thenExecute(()->{
                    // The lengths a reader can be moved between (1 and 2, 2 and 3, 3 and 4) are the same; the first has the entry behind it and is never landed in.
                    for(int k=TrialRooms.FIRST_FOLD-1;k<TrialRooms.LAST_FOLD;k++){var o=TrialRooms.lengthOrigin(k);var n=TrialRooms.lengthOrigin(k+1);
                        for(int x=-2;x<=5;x++)for(int y=-1;y<=3;y++)for(int z=0;z>=-11;z--){var a=f.level.getBlockState(b.offset(o).offset(x,y,z));var c=f.level.getBlockState(b.offset(n).offset(x,y,z));
                            h.assertTrue(a==c,"length "+k+" and length "+(k+1)+" are the same at "+x+","+y+","+z+": "+a+" / "+c);}}
                    for(int k=0;k<TrialRooms.LENGTHS;k++){var o=TrialRooms.lengthOrigin(k);
                        f.body(b,o.getX()+.5,0,o.getZ()-4.5,1,"the straight of length "+k);f.body(b,o.getX()+3.5,0,o.getZ()-9.5,1,"the bend of length "+k);
                        h.assertTrue(f.level.getBlockState(b.offset(o).offset(0,2,-4)).getLightEmission()>0,"a lit lamp over length "+k);}
                    var door=TrialRooms.lengthsDoor();
                    h.assertTrue(f.level.getBlockState(b.offset(door)).getBlock() instanceof DoorBlock,"the far door round the last bend");f.body(b,door.getX()+.5,0,door.getZ()+1.5,1,"the way to the far door");
                    h.assertTrue(TrialMarks.kind(place)==place&&HazardVariety.tier(place)==2&&LabyrinthPacing.physicalTrial(place),"a second-tier physical trial, and a mark of its own");
                    f.at(runner,place,.5,0,400);
                })
                .thenExecuteFor(route.size(),()->{var v=route.get(Math.min(step[0],route.size()-1));step[0]++;f.at(walker,place,v.x,0,v.z);walker.setOnGround(true);})
                .thenIdle(3)
                .thenExecute(()->{h.assertTrue(TrialRooms.folds(walker)==0&&walker.getZ()-b.getZ()<TrialRooms.lengthOrigin(TrialRooms.LENGTHS-1).getZ()-9,"walking, a reader crosses every length once ("+TrialRooms.folds(walker)+" moved back)");
                    f.at(walker,place,.5,0,400);at.accept(runner,mid.apply(1));})
                .thenIdle(2).thenExecute(()->runner.setSprinting(true))
                .thenIdle(2).thenExecute(()->at.accept(runner,bend.apply(1)))
                .thenIdle(2).thenExecute(()->{mark[0]=runner.getYRot();at.accept(runner,start.apply(2));})
                .thenIdle(2).thenExecute(()->{var back=start.apply(1);
                    h.assertTrue(TrialRooms.folds(runner)==1&&Math.abs(runner.getX()-b.getX()-(back.getX()+.5))<.01&&Math.abs(runner.getZ()-b.getZ()-(back.getZ()+.5))<.01&&runner.getYRot()==(float)mark[0],
                            "sprinting through the second length, the reader is back at its start, one length back and facing the same way ("+runner.position().subtract(Vec3.atLowerCornerOf(b))+")");
                    runner.setSprinting(false);at.accept(runner,mid.apply(0));})
                .thenIdle(2).thenExecute(()->at.accept(runner,mid.apply(1)))
                .thenIdle(2).thenExecute(()->at.accept(runner,bend.apply(1)))
                .thenIdle(2).thenExecute(()->at.accept(runner,start.apply(2)))
                .thenIdle(2).thenExecute(()->{h.assertTrue(TrialRooms.folds(runner)==1&&TrialRooms.lengthOf(b,runner.getZ())==2,"walking it again, the reader goes on");runner.awardStat(net.minecraft.stats.Stats.JUMP);})
                .thenIdle(2).thenExecute(()->at.accept(runner,bend.apply(2)))
                .thenIdle(2).thenExecute(()->at.accept(runner,start.apply(3)))
                .thenIdle(2).thenExecute(()->{h.assertTrue(TrialRooms.folds(runner)==2&&TrialRooms.lengthOf(b,runner.getZ())==2,"a jump in a length counts as hurrying it");
                    at.accept(runner,mid.apply(2));runner.setSprinting(true);})
                .thenIdle(2).thenExecute(()->{runner.setSprinting(false);at.accept(runner,bend.apply(2));
                    f.at(walker,place,bend.apply(2).getX()+.5,0,bend.apply(2).getZ()+.5);walker.setYRot(180);walker.setYHeadRot(180);walker.setXRot(0);})
                .thenIdle(2).thenExecute(()->at.accept(runner,start.apply(3).offset(0,0,-1)))
                .thenExecuteFor(6,()->{})
                .thenExecute(()->{h.assertTrue(TrialRooms.folds(runner)==2&&TrialRooms.lengthOf(b,runner.getZ())==3,"while someone else watches, nobody is moved back");
                    walker.setYRot(-90);walker.setYHeadRot(-90);})
                .thenIdle(3)
                .thenExecute(()->{h.assertTrue(TrialRooms.folds(runner)==3&&TrialRooms.lengthOf(b,runner.getZ())==2,"once nobody sees it, the move back owed is made");
                    f.at(walker,place,.5,0,400);at.accept(runner,bend.apply(-1));runner.setSprinting(true);})
                .thenIdle(2).thenExecute(()->at.accept(runner,mid.apply(0)))
                .thenIdle(2).thenExecute(()->at.accept(runner,bend.apply(0)))
                .thenIdle(2).thenExecute(()->at.accept(runner,start.apply(1)))
                .thenIdle(2).thenExecute(()->{h.assertTrue(TrialRooms.folds(runner)==3&&TrialRooms.lengthOf(b,runner.getZ())==1,"the first length is free");
                    runner.setSprinting(false);at.accept(runner,mid.apply(3));})
                .thenIdle(2).thenExecute(()->at.accept(runner,bend.apply(3)))
                .thenIdle(2).thenExecute(()->at.accept(runner,start.apply(4)))
                .thenIdle(2).thenExecute(()->{runner.setSprinting(true);at.accept(runner,mid.apply(4));})
                .thenIdle(2).thenExecute(()->at.accept(runner,bend.apply(4)))
                .thenIdle(2).thenExecute(()->{var door=TrialRooms.lengthsDoor();at.accept(runner,door.offset(0,0,1));})
                .thenIdle(2).thenExecute(()->h.assertTrue(TrialRooms.folds(runner)==3&&runner.getZ()-b.getZ()<TrialRooms.lengthsDoor().getZ()+2.1,"and so is the last: the reader reaches the far door"))
                .thenSucceed();
    }
    @AfterBatch(batch="many_lengths") public static void manyLengthsDone(ServerLevel level){close();}

    /**
     * 0.4.389, the door that follows (docs/FOLLOWING_DOOR_0_4_389.md): nine framed doors in three walls with a passage behind
     * every one to the far door, two tall mirrors facing into the room; exactly one door stands open and never one a reader in
     * the room can see; turning to it shuts it and opens another out of sight; a reader standing in the doorway keeps it open
     * against anyone's look; a reader through into the passage no longer counts.
     */
    @GameTest(template="empty",batch="following_door",timeoutTicks=600)
    public static void theDoorThatFollowsIsNeverWhereAnyoneIsLooking(GameTestHelper h){
        var f=new Fixture(h);var place=LabyrinthPlace.FOLLOWING_DOOR;var reader=f.player("following_reader",place);var friend=f.player("following_friend",place);var b=f.base(place);
        var doors=TrialRooms.DOORWAYS;
        java.util.function.Supplier<Integer> open=()->{int found=-1,count=0;for(int i=0;i<doors.size();i++)if(f.level.getBlockState(b.offset(doors.get(i))).isAir()){found=i;count++;}return count>1?-2:found;};
        java.util.function.BiConsumer<ServerPlayer,Vec3> look=(p,target)->{var to=target.subtract(p.getEyePosition());float yaw=(float)Math.toDegrees(Math.atan2(-to.x,to.z));p.setYRot(yaw);p.setYHeadRot(yaw);p.setXRot(0);};
        java.util.function.BiConsumer<ServerPlayer,Float> face=(p,yaw)->{p.setYRot(yaw);p.setYHeadRot(yaw);p.setXRot(0);};
        var was=new int[2];
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the door that follows is loaded"))
                .thenExecute(()->{
                    for(var d:doors){var side=TrialRooms.into(d).getClockWise();var trim=TrialRegistry.material("follow_trim");
                        h.assertTrue(f.level.getBlockState(b.offset(d).above(2)).is(trim)&&f.level.getBlockState(b.offset(d).relative(side)).is(trim)&&f.level.getBlockState(b.offset(d).relative(side.getOpposite()).above()).is(trim),"a frame round the door at "+d);
                        var behind=d.relative(TrialRooms.into(d).getOpposite());f.body(b,behind.getX()+.5,0,behind.getZ()+.5,1,"the passage behind the door at "+d);
                        var front=d.relative(TrialRooms.into(d));f.body(b,front.getX()+.5,0,front.getZ()+.5,1,"room in front of the door at "+d);}
                    for(int z=-1;z>=-13;z--){f.body(b,-6.5,0,z+.5,1,"the west passage at "+z);f.body(b,7.5,0,z+.5,1,"the east passage at "+z);}
                    for(int x=-7;x<=7;x++)f.body(b,x+.5,0,-12.5,1,"the north passage at "+x);
                    h.assertTrue(f.level.getBlockState(b.offset(0,0,-14)).getBlock() instanceof DoorBlock,"the far door, off the passage");
                    for(int x:new int[]{-3,3}){var low=f.level.getBlockState(b.offset(x,1,-1));var high=f.level.getBlockState(b.offset(x,2,-1));
                        h.assertTrue(low.is(TrialRegistry.MIRROR.get())&&low.getValue(TrialMirrorBlock.FACING)==Direction.NORTH&&!low.getValue(TrialMirrorBlock.UPPER)&&high.is(TrialRegistry.MIRROR.get())&&high.getValue(TrialMirrorBlock.UPPER)
                            &&f.level.getBlockEntity(b.offset(x,1,-1)) instanceof TrialMirrorBlock.Glass,"a tall mirror on the entry wall at "+x+", facing into the room");}
                    h.assertTrue(f.level.getBlockState(b.offset(0,3,-6)).getLightEmission()>0,"the room is lit");
                    h.assertTrue(TrialMarks.kind(place)==place&&HazardVariety.tier(place)==2&&LabyrinthPacing.physicalTrial(place),"a second-tier physical trial, and a mark of its own");
                    f.at(friend,place,.5,0,400);
                    f.at(reader,place,.5,0,-6.5);reader.setOnGround(true);face.accept(reader,180f);
                })
                .thenIdle(3)
                .thenExecute(()->{int o=open.get();h.assertTrue(o>=0,"exactly one door stands open ("+o+")");
                    h.assertTrue(!TrialRooms.doorwaySeen(f.level,List.of(reader),b,doors.get(o)),"and not one the reader can see");was[0]=o;
                    look.accept(reader,TrialRooms.doorwayFront(b,doors.get(o),1));})
                .thenIdle(2)
                .thenExecute(()->{int o=open.get();
                    h.assertTrue(o>=0&&o!=was[0]&&TrialBlock.is(f.level.getBlockState(b.offset(doors.get(was[0]))),TrialBlock.Kind.FOLLOW_PANEL),"turning to it, it is shut and another stands open ("+was[0]+" to "+o+")");
                    h.assertTrue(!TrialRooms.doorwaySeen(f.level,List.of(reader),b,doors.get(o)),"one the reader can't see");was[0]=o;
                    look.accept(reader,TrialRooms.doorwayFront(b,doors.get(o),1));})
                .thenIdle(2)
                .thenExecute(()->{int o=open.get();h.assertTrue(o>=0&&o!=was[0]&&!TrialRooms.doorwaySeen(f.level,List.of(reader),b,doors.get(o)),"and again: it is always behind");
                    // Backing into it: the reader stands in the doorway, the friend in the room looks straight at it.
                    var d=doors.get(o);was[0]=o;f.at(reader,place,d.getX()+.5,0,d.getZ()+.5);reader.setOnGround(true);
                    var front=d.relative(TrialRooms.into(d),3);f.at(friend,place,front.getX()+.5,0,front.getZ()+.5);friend.setOnGround(true);look.accept(friend,TrialRooms.doorwayFront(b,d,1));})
                .thenIdle(3)
                .thenExecute(()->{h.assertTrue(open.get()==was[0],"nobody's look shuts a door with someone standing in it");
                    var behind=doors.get(was[0]).relative(TrialRooms.into(doors.get(was[0])).getOpposite());
                    h.assertTrue(!TrialRooms.inFollowRoom(b,Vec3.atLowerCornerOf(b.offset(behind)).add(.5,1.6,.5)),"the passage behind the doors is not the room");
                    f.at(reader,place,behind.getX()+.5,0,behind.getZ()+.5);reader.setOnGround(true);})
                .thenIdle(3)
                .thenExecute(()->{int o=open.get();h.assertTrue(o>=0&&o!=was[0]&&!TrialRooms.doorwaySeen(f.level,List.of(friend),b,doors.get(o)),"once it is empty, the friend's look shuts it, and another opens out of the friend's sight");})
                .thenSucceed();
    }
    @AfterBatch(batch="following_door") public static void followingDoorDone(ServerLevel level){close();}

}
