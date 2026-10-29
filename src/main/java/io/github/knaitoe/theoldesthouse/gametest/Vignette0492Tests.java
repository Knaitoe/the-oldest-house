package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.*;

/** Native interaction, collision, saved-world and individual-ending boundaries for the October 13 pass. */
@GameTestHolder(TheOldestHouse.MOD_ID+"_playtest492") @PrefixGameTestTemplate(false)
public final class Vignette0492Tests {
    private static LiteraryTests.Fixture active;
    private static LiteraryTests.Fixture fixture(GameTestHelper h){return active=new LiteraryTests.Fixture(h);}
    private static void close(){if(active!=null){active.close();active=null;}}
    private static void lease(LiteraryTests.Fixture f,LabyrinthPlace place){var b=f.base(place);var r=place.room();f.chunks.hold(f.level(place),new AABB(b.getX()+r.minX()-2,b.getY()+r.minY()-2,b.getZ()+r.minZ()-2,b.getX()+r.maxX()+3,b.getY()+r.maxY()+3,b.getZ()+r.maxZ()+3));}
    private static PlayerInteractEvent.RightClickBlock use(ServerPlayer p,BlockPos at,InteractionHand hand){
        p.moveTo(at.getX()+.5,at.getY(),at.getZ()+2);p.setDeltaMovement(Vec3.ZERO);
        BlockHitResult hit=null;for(var box:p.serverLevel().getBlockState(at).getShape(p.serverLevel(),at).toAabbs()){
            var target=box.getCenter().add(at.getX(),at.getY(),at.getZ());var ray=p.serverLevel().clip(new net.minecraft.world.level.ClipContext(p.getEyePosition(),target,net.minecraft.world.level.ClipContext.Block.OUTLINE,net.minecraft.world.level.ClipContext.Fluid.NONE,p));
            if(ray.getType()==HitResult.Type.BLOCK&&ray.getBlockPos().equals(at)){hit=ray;break;}
        }
        if(hit==null)hit=new BlockHitResult(at.getCenter(),Direction.SOUTH,at,false);
        var event=new PlayerInteractEvent.RightClickBlock(p,hand,at,hit);NeoForge.EVENT_BUS.post(event);return event;
    }
    @GameTest(template="empty",batch="review492_nursery",timeoutTicks=300)
    public static void chestAndShelfToysTouchTheirOriginalSupportsAndTheChairSeatsANativePlayer(GameTestHelper h){
        var f=fixture(h);var place=ChildRoom.PLACE;var p=f.player("nursery_support",place);var b=f.base(place);var l=f.in;
        var chest=b.offset(-9,1,-20);var shelf=b.offset(ChildRoom.TOY_HOMES.get(1));var music=b.offset(-10,1,-15);
        h.assertTrue(l.getBlockState(chest).isAir()&&NurseryBlock.is(l.getBlockState(shelf),NurseryBlock.Kind.DOLL)&&l.getBlockState(shelf).getValue(NurseryBlock.STAGE)==1,"0.4.121: the one doll sits fitted on the shelf; the toy chest's lid is bare");
        for(var at:List.of(shelf,music)){var top=l.getBlockState(at.below()).getShape(l,at.below()).bounds();var toy=l.getBlockState(at).getShape(l,at).bounds();h.assertTrue(toy.minX>=top.minX&&toy.maxX<=top.maxX&&toy.minZ>=top.minZ&&toy.maxZ<=top.maxZ&&toy.minY==0&&top.maxY==1,"shelf footprint supports the entire toy: "+at);}
        var mobile=b.offset(0,5,-22);h.assertTrue(NurseryBlock.is(l.getBlockState(mobile),NurseryBlock.Kind.MOBILE)&&!l.getBlockState(mobile.above()).isAir(),"the original mobile already reaches its ceiling");
        var chair=b.offset(7,0,-18);use(p,chair,InteractionHand.MAIN_HAND);h.assertTrue(p.getVehicle() instanceof SeatEntity,"the actual child's chair creates a native seat");p.stopRiding();h.assertTrue(!p.isPassenger(),"ordinary dismount releases the player");
        h.assertTrue(!f.own(p,place).getBoolean("Ready")&&!WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.of(place.id())),"handling furniture never grants the story");h.succeed();
    }
    @AfterBatch(batch="review492_nursery") public static void nurseryDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="review492_nursery_saved",timeoutTicks=400)
    public static void aSavedToyRepairWaitsForNativeSectionsCamerasAndBodiesAndNeverRestocksAnEdit(GameTestHelper h){
        var f=fixture(h);var place=ChildRoom.PLACE;var p=f.player("nursery_camera",place);var b=f.base(place);var l=f.in;lease(f,place);
        var shelf=b.offset(ChildRoom.TOY_HOMES.get(1));var t=f.data().state(NurserySupport0492.STATE);t.remove(Long.toString(b.asLong()));f.data().setState(NurserySupport0492.STATE,t);
        l.setBlock(shelf,l.getBlockState(shelf).setValue(NurseryBlock.STAGE,0),18);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native entity sections load")).thenExecute(()->{
            p.setGameMode(GameType.SPECTATOR);f.at(p,place,-9,2,-20);h.assertTrue(!NurserySupport0492.repair(l,b),"a spectator's actual camera protects the props");
            var body=LiteraryRegistry.ACTOR.get().create(l);f.extras.add(body);body.bind(place,null);body.setNoGravity(true);body.moveTo(shelf.getX()+.5,shelf.getY(),shelf.getZ()+.5);h.assertTrue(l.addFreshEntity(body),"the fixture body joins the native level");f.at(p,place,0,0,100);
            h.assertTrue(!NurserySupport0492.repair(l,b),"an actual living body blocks the changed cell");body.discard();
            h.assertTrue(NurserySupport0492.repair(l,b)&&l.getBlockState(shelf).getValue(NurseryBlock.STAGE)==1,"the recognised original mount is fitted");
            l.setBlock(shelf,Blocks.AIR.defaultBlockState(),18);f.reload();h.assertTrue(NurserySupport0492.repair(l,b)&&l.getBlockState(shelf).isAir(),"the saved checkpoint never restores a later removal");
        }).thenSucceed();
    }
    @AfterBatch(batch="review492_nursery_saved") public static void savedNurseryDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="review492_interactivity",timeoutTicks=300)
    public static void lampsClutterAndChessRespondWithoutLootOrStoryCreditAndRespectNativeReach(GameTestHelper h){
        var f=fixture(h);var place=LabyrinthPlace.THREE_FINGERS;var p=f.player("clutter_reader",place);var b=f.base(place);var l=f.in;var lamp=b.offset(0,1,-8);var papers=b.offset(2,1,-8);var chess=b.offset(4,1,-8);
        for(var at:List.of(lamp,papers,chess))l.setBlock(at.below(),Blocks.OAK_PLANKS.defaultBlockState(),18);
        l.setBlock(lamp,SceneDetailBlock.state(SceneDetailBlock.Kind.TABLE_LAMP,Direction.NORTH),18);l.setBlock(papers,SceneDetailBlock.state(SceneDetailBlock.Kind.INK_PAPERS,Direction.NORTH),18);l.setBlock(chess,CottageBlock.of(CottageBlock.Kind.CHESSBOARD,Direction.NORTH),18);
        var before=p.getInventory().save(new net.minecraft.nbt.ListTag());
        h.assertTrue(l.getBlockState(lamp).getLightEmission(l,lamp)==9,"a lit table lamp emits actual block light");
        use(p,lamp,InteractionHand.MAIN_HAND);h.assertTrue(!l.getBlockState(lamp).getValue(SceneDetailBlock.LIT)&&l.getBlockState(lamp).getLightEmission(l,lamp)==0,"the switch extinguishes native light");
        use(p,lamp,InteractionHand.OFF_HAND);h.assertTrue(!l.getBlockState(lamp).getValue(SceneDetailBlock.LIT),"offhand packets do not double-toggle");
        use(p,lamp,InteractionHand.MAIN_HAND);h.assertTrue(l.getBlockState(lamp).getValue(SceneDetailBlock.LIT),"the lamp can be switched back on");
        use(p,papers,InteractionHand.MAIN_HAND);h.assertTrue(l.getBlockState(papers).getValue(SceneDetailBlock.FACING)==Direction.EAST,"handling rotates the supported papers");
        use(p,chess,InteractionHand.MAIN_HAND);h.assertTrue(l.getBlockState(chess).getValue(CottageBlock.STAGE)==1,"the chess move changes the native mesh state");
        p.setGameMode(GameType.SPECTATOR);use(p,chess,InteractionHand.MAIN_HAND);h.assertTrue(l.getBlockState(chess).getValue(CottageBlock.STAGE)==1,"observers cannot move pieces");p.setGameMode(GameType.SURVIVAL);
        p.moveTo(lamp.getX()+.5,lamp.getY(),lamp.getZ()+8);var far=new PlayerInteractEvent.RightClickBlock(p,InteractionHand.MAIN_HAND,lamp,new BlockHitResult(lamp.getCenter(),Direction.SOUTH,lamp,false));NeoForge.EVENT_BUS.post(far);h.assertTrue(l.getBlockState(lamp).getValue(SceneDetailBlock.LIT),"unreachable props ignore remote clicks");
        l.setBlock(papers.south(),Blocks.STONE.defaultBlockState(),18);use(p,papers,InteractionHand.MAIN_HAND);h.assertTrue(l.getBlockState(papers).getValue(SceneDetailBlock.FACING)==Direction.EAST,"a native wall blocks the handling ray");
        h.assertTrue(before.equals(p.getInventory().save(new net.minecraft.nbt.ListTag()))&&!f.own(p,place).getBoolean("Ready")&&!WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.of(place.id())),"ordinary handling leaves finite supplies and personal resolution untouched");h.succeed();
    }
    @AfterBatch(batch="review492_interactivity") public static void interactivityDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="review492_bar_capture",timeoutTicks=600)
    public static void theOriginalMenPhysicallyTakeOnlyTheirChosenReaderAndTheClippingRequiresTheSource(GameTestHelper h){
        var f=fixture(h);var place=ElkLot.PLACE;var p=f.player("men_captive",place);var peer=f.player("men_onlooker",place);var b=f.base(place);lease(f,place);f.at(p,place,-17.5,0,-22.5);f.at(peer,place,-28,0,-16);
        var ids=new HashMap<String,UUID>();
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready()&&LiteraryVignettes.shared(f.data(),place).hasUUID("Wade"),"the original native cast and sections are ready")).thenExecute(()->{
            var night=LiteraryVignettes.shared(f.data(),place);night.putInt("CrashAt",0);night.putInt("Night",ElkLotNight.MEN_OUT+ElkLotNight.SEARCH+ElkLotNight.STAGE*3);LiteraryVignettes.shared(f.data(),place,night);
            for(var who:ElkLotNight.MEN)ids.put(who.key(),night.getUUID(who.key()));var own=f.own(p,place);own.putBoolean("ElkSeen",true);LiteraryVignettes.save(f.data(),p.getUUID(),place,own);
        }).thenIdle(10).thenExecute(()->h.assertTrue(!f.own(p,place).getBoolean("Ready")&&f.own(p,place).getInt("CaptureTicks0492")==0,"choosing the bar does not substitute for reaching native bodies"))
        .thenWaitUntil(()->h.assertTrue(f.own(p,place).getBoolean("Ready"),"the original men walk into contact and finish their capture")).thenExecute(()->{
            var own=f.own(p,place);h.assertTrue(ElkLotEnding.MEN.equals(own.getString("Outcome"))&&own.getBoolean("CaptureWitness0492")&&!ElkLotEnding.held(p),"the physical capture produces its own released aftermath");
            var night=LiteraryVignettes.shared(f.data(),place);for(var entry:ids.entrySet())h.assertTrue(night.getUUID(entry.getKey()).equals(entry.getValue())&&f.out.getEntity(entry.getValue()) instanceof LiteraryActor,"the men retain their original native UUIDs");
            h.assertTrue(!f.own(peer,place).getBoolean("Ready")&&!WitnessAccount.has(f.data(),peer.getUUID(),WitnessAccount.Story.ELK_LOT),"the onlooker inherits neither capture nor account");
            f.read(p,place,ElkLot.BOARD);h.assertTrue(!WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.ELK_LOT),"the ending alone cannot replace the personally read source");f.source(p,place);f.read(p,place,ElkLot.BOARD);h.assertTrue(WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.ELK_LOT),"both personally read papers resolve this reader's capture");
            var text=LiteraryTexts.ending(p,place,own).get(DataComponents.WRITTEN_BOOK_CONTENT);h.assertTrue(text.pages().stream().anyMatch(page->page.raw().getString().contains("LAST SEEN")),"the men have a distinct account");
        }).thenSucceed();
    }
    @AfterBatch(batch="review492_bar_capture") public static void barDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="review492_bar_furniture04112",timeoutTicks=900)
    public static void theOriginalMenFindAReaderAcrossTheActualPoolTableAndMarlaIsWithinReach(GameTestHelper h){
        var f=fixture(h);var place=ElkLot.PLACE;var p=f.player("bar_furniture",place);var b=f.base(place);lease(f,place);var ids=new HashMap<String,UUID>();
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready()&&LiteraryVignettes.shared(f.data(),place).hasUUID("Wade"),"the actual furnished bar and native sections are ready")).thenExecute(()->{
            var night=LiteraryVignettes.shared(f.data(),place);var marla=(LiteraryActor)f.out.getEntity(night.getUUID("Marla"));f.at(p,place,-24.5,0,-23.5);h.assertTrue(p.getEyePosition().distanceToSqr(marla.getBoundingBox().getCenter())<9&&f.out.noCollision(marla,marla.getBoundingBox()),"Marla serves from behind the actual counter at natural native reach");
            f.at(p,place,-25.5,0,-16.5);for(var who:ElkLotNight.MEN)ids.put(who.key(),night.getUUID(who.key()));night.putInt("CrashAt",0);night.putInt("Night",ElkLotNight.MEN_OUT+ElkLotNight.SEARCH+ElkLotNight.STAGE*3);LiteraryVignettes.shared(f.data(),place,night);var own=f.own(p,place);own.putBoolean("ElkSeen",true);LiteraryVignettes.save(f.data(),p.getUUID(),place,own);
        }).thenWaitUntil(()->h.assertTrue(f.own(p,place).getBoolean("Ready"),"two original men must walk around the pool table and reach the reader: "+ids.values().stream().map(id->f.out.getEntity(id)).filter(Objects::nonNull).map(a->a.position().subtract(b.getX(),b.getY(),b.getZ()).toString()).toList())).thenExecute(()->{
            h.assertTrue(f.own(p,place).getBoolean("CaptureWitness0492")&&ElkLotEnding.MEN.equals(f.own(p,place).getString("Outcome")),"only actual physical contact finishes the men's ending across furniture");var night=LiteraryVignettes.shared(f.data(),place);for(var who:ElkLotNight.MEN)h.assertTrue(ids.get(who.key()).equals(night.getUUID(who.key())),"routing never substitutes a new man");
        }).thenSucceed();
    }
    @AfterBatch(batch="review492_bar_furniture04112") public static void barFurnitureDone(ServerLevel level){close();}

    @GameTest(template="empty",batch="review492_herd_custody",timeoutTicks=400)
    public static void loopCountersCannotCaptureTheReaderAndPrivateHerdsPreserveSavedOriginals(GameTestHelper h){
        var f=fixture(h);var place=ElkLot.PLACE;var p=f.player("herd_custody",place);var peer=f.player("herd_legacy",place);var b=f.base(place);lease(f,place);f.at(p,place,28,0,-85);f.at(peer,place,-22,0,-12);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready()&&LiteraryVignettes.shared(f.data(),place).hasUUID("Elk"),"the original cow and native field are ready")).thenExecute(()->{
            var legacy=f.own(peer,place);legacy.putBoolean("Ready",true);legacy.putBoolean("Completed",true);legacy.putString("Outcome","escaped_the_looping_field_and_read_own_clipping");LiteraryVignettes.save(f.data(),peer.getUUID(),place,legacy);var original=f.out.getEntity(LiteraryVignettes.shared(f.data(),place).getUUID("Elk"));var originalId=original.getUUID();
            var own=f.own(p,place);own.putBoolean("ElkSeen",true);own.putInt("FieldTicks",400);own.putInt("FieldLoops",200);ElkLotEnding.reader(p,b,own,LiteraryVignettes.shared(f.data(),place));LiteraryVignettes.save(f.data(),p.getUUID(),place,own);
            h.assertTrue(!own.getBoolean("Ready")&&own.getInt("CaptureTicks0492")==0&&!LiteraryVignettes.retreatLocked(p),"any number of loops still requires actual physical capture and permits retreat");
            var world=LiteraryVignettes.shared(f.data(),place);var elk=(LiteraryElk)f.out.getEntity(world.getUUID("EndingHerd0492_0_"+p.getUUID()));h.assertTrue(elk!=null&&elk.owner().orElseThrow().equals(p.getUUID())&&!elk.getUUID().equals(originalId),"the approaching herd has an explicit private reader");var elkId=elk.getUUID();
            h.assertTrue(elk.broadcastToPlayer(p)&&!elk.broadcastToPlayer(peer)&&original.broadcastToPlayer(p)&&original.broadcastToPlayer(peer),"native tracking keeps private herd colliders out of the peer client while the original cow stays shared");
            elk.hurt(f.out.damageSources().playerAttack(peer),4);h.assertTrue(!f.own(peer,place).getBoolean("ElkWounded"),"hitting a private apparition cannot wound the original cow for another story");
            var saved=new CompoundTag();elk.saveWithoutId(saved);h.assertTrue(saved.getUUID("HerdOwner0492").equals(p.getUUID()),"native entity persistence retains the private rendering owner");
            elk.discard();f.reload();own=f.own(p,place);ElkLotEnding.reader(p,b,own,LiteraryVignettes.shared(f.data(),place));h.assertTrue(f.out.getEntity(elkId)==null&&LiteraryVignettes.shared(f.data(),place).getUUID("EndingHerd0492_0_"+p.getUUID()).equals(elkId),"a saved missing herd body is never refilled");
            var keep=f.own(peer,place);ElkLotEnding.reader(peer,b,keep,LiteraryVignettes.shared(f.data(),place));h.assertTrue(keep.getBoolean("Completed")&&keep.getString("Outcome").equals(legacy.getString("Outcome"))&&f.out.getEntity(originalId)==original,"legacy endings and the original shared cow survive");
        }).thenSucceed();
    }
    @AfterBatch(batch="review492_herd_custody") public static void herdDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="review492_bar_handling",timeoutTicks=300)
    public static void barStoolsRespectTheOriginalOccupantAndNativeDartsAndPoolRemainUsable(GameTestHelper h){
        var f=fixture(h);var place=ElkLot.PLACE;var p=f.player("bar_handling",place);var b=f.base(place);lease(f,place);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready()&&LiteraryVignettes.shared(f.data(),place).hasUUID("Russ"),"the actual cast is loaded")).thenExecute(()->{
            var russ=(LiteraryActor)f.out.getEntity(LiteraryVignettes.shared(f.data(),place).getUUID("Russ"));var occupied=russ.blockPosition();f.at(p,place,-23.5,0,-22);
            h.assertTrue(!HouseSitting.sit(p,occupied)&&!p.isPassenger(),"an original bar attendant keeps his occupied stool");
            var free=b.offset(-20,0,-24);use(p,free,InteractionHand.MAIN_HAND);h.assertTrue(p.getVehicle() instanceof SeatEntity,"a free native bar stool seats the player");p.stopRiding();
            f.click(p,place,ElkLot.POOL_L);h.assertTrue(f.out.getBlockState(b.offset(ElkLot.POOL_L)).getValue(RoadhouseBlock.STAGE)==1&&f.out.getBlockState(b.offset(ElkLot.POOL_R)).getValue(RoadhouseBlock.STAGE)==1,"both original pool halves show the break");
            f.click(p,place,ElkLot.DARTS);h.assertTrue(f.out.getBlockState(b.offset(ElkLot.DARTS)).getValue(RoadhouseBlock.STAGE)==1&&!f.own(p,place).getBoolean("Ready"),"throwing darts changes the native mesh without finishing the story");
        }).thenSucceed();
    }
    @AfterBatch(batch="review492_bar_handling") public static void barHandlingDone(ServerLevel l){close();}

    private static ClassicsTests.Fixture seance;
    @AfterBatch(batch="review492_seance_saved") public static void seanceDone(ServerLevel l){if(seance!=null){seance.close();seance=null;}}
    @GameTest(template="empty",batch="review492_seance_saved",timeoutTicks=650)
    public static void savedFamilyStepsOffTheActualCaneChairsAndRunsThroughTheWestDoor(GameTestHelper h){
        seance=new ClassicsTests.Fixture(h,492400,LabyrinthPlace.SEANCE);var f=seance;var p=f.player("saved_family_witness");var ids=new HashSet<UUID>();
        h.startSequence().thenWaitUntil(()->h.assertTrue(ClassicsVignettes.cast(f.l).size()==4,"the original family and native chair layout are loaded")).thenExecute(()->{
            // The old westbound route leaves the mother trapped behind her chair; the others are still on their seats.
            for(var a:ClassicsVignettes.cast(f.l)){ids.add(a.getUUID());if(a.role()==1)a.stopRiding();a.moveTo(a.role()==1?f.b.getX()-3.3:a.getX(),f.b.getY()+.48,a.getZ());a.setNoGravity(true);a.appearance(a.role(),false,true);a.getPersistentData().putInt("Flee0477",0);a.getPersistentData().remove("FlightRoute0492");var saved=a.saveWithoutId(new CompoundTag());a.load(saved);}
            var all=f.data().state(ClassicsVignettes.STATE);all.putBoolean("SittingEnded",true);all.putString("SharedOutcome","drove_the_family_out");all.putBoolean("Fleeing0477",true);f.data().setState(ClassicsVignettes.STATE,all);f.reload();
            NovelRooms.door(f.l,f.b.offset(-5,0,-22),Direction.SOUTH,Blocks.DARK_OAK_DOOR,true);f.at(p,.5,0,-11.5);
        }).thenWaitUntil(()->h.assertTrue(f.data().state(ClassicsVignettes.STATE).getBoolean("Fled0477"),"the saved native bodies must actually leave: "+ClassicsVignettes.cast(f.l).stream().map(a->a.role()+":"+a.position().subtract(f.b.getX(),f.b.getY(),f.b.getZ())+" leg="+a.getPersistentData().getInt("Flee0477")).toList())).thenExecute(()->{
            h.assertTrue(ids.equals(new HashSet<>(ClassicsVignettes.cast(f.l).stream().map(Entity::getUUID).toList())),"no original is replaced to manufacture an escape");
            for(var a:ClassicsVignettes.cast(f.l))h.assertTrue(a.getZ()<f.b.getZ()-23&&!a.seated()&&f.l.noCollision(a,a.getBoundingBox()),"each actor finishes clear of the native room collision: "+a.role());
            h.assertTrue(!f.l.getBlockState(f.b.offset(-5,0,-22)).getValue(DoorBlock.OPEN),"the last original shuts the physical door");
        }).thenSucceed();
    }
    @AfterBatch(batch="review492_courtyard_saved") public static void courtyardDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="review492_courtyard_saved",timeoutTicks=350)
    public static void checkpointedCourtyardDecorBecomesChessWithoutRestockingLaterEdits(GameTestHelper h){
        var f=fixture(h);var place=LabyrinthPlace.ZAMPANO_COURTYARD;var b=f.base(place);var l=f.out;f.built.add(place);var box=new AABB(Vec3.atLowerCornerOf(b.offset(-16,-2,-42)),Vec3.atLowerCornerOf(b.offset(17,18,8)));f.chunks.hold(l,box);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the native courtyard sections are loaded")).thenExecute(()->{
            NovelRooms.build(l.getServer(),l,b,place);VignetteArchitecture.decorateOnce(l,f.origin,place);
            for(int x:new int[]{-8,8}){var table=b.offset(x,0,-12);h.assertTrue(l.getBlockState(table).getValue(HouseholdFurnitureBlock.KIND)==HouseholdFurnitureBlock.Kind.CHESS_TABLE&&l.getBlockState(table.above()).getValue(VignetteDetailBlock.KIND)==VignetteDetailBlock.Kind.CHESS,"the actual builder authors chess before any review can run");l.setBlock(table,HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE,Direction.SOUTH),18);l.setBlock(table.above(),SceneDetailBlock.state(SceneDetailBlock.Kind.VASE,Direction.SOUTH),18);}
            var old=f.data().state(PlaytestSceneReview.STATE);old.putBoolean(f.origin.asLong()+":"+place.id(),true);f.data().setState(PlaytestSceneReview.STATE,old);f.reload();
            var p=NativeTestPlayers.survival(h,"courtyard_camera");f.players.add(p);p.teleportTo(l,b.getX(),b.getY(),b.getZ()-11,0,0);p.setGameMode(GameType.SPECTATOR);h.assertTrue(!CourtyardChess0492.repair(l,b),"an actual spectator camera postpones the saved geometry change");p.teleportTo(l,b.getX(),b.getY(),b.getZ()+100,0,0);
            h.assertTrue(CourtyardChess0492.repair(l,b),"the old completed checkpoint cannot suppress the recognised correction");
            for(int x:new int[]{-8,8})h.assertTrue(l.getBlockState(b.offset(x,0,-12)).getValue(HouseholdFurnitureBlock.KIND)==HouseholdFurnitureBlock.Kind.CHESS_TABLE&&l.getBlockState(b.offset(x,1,-12)).getValue(VignetteDetailBlock.KIND)==VignetteDetailBlock.Kind.CHESS,"both actual settings retain boards and chessmen");
            l.setBlock(b.offset(-8,1,-12),Blocks.AIR.defaultBlockState(),18);f.reload();h.assertTrue(CourtyardChess0492.repair(l,b)&&l.getBlockState(b.offset(-8,1,-12)).isAir(),"later removal stays removed across native saved-data reload");
        }).thenSucceed();
    }
    @AfterBatch(batch="review492_men_exit") public static void menExitDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="review492_men_exit",timeoutTicks=2400)
    public static void allThreeOriginalMenLeaveTheirActualBarSeatsThroughTheSideDoor(GameTestHelper h){
        var f=fixture(h);var place=ElkLot.PLACE;var p=f.player("bar_exit_reader",place);var b=f.base(place);lease(f,place);f.at(p,place,-3.5,0,-8.5);var ids=new HashMap<String,UUID>();
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready()&&LiteraryVignettes.shared(f.data(),place).hasUUID("Wade"),"the native bar layout and original men are loaded")).thenExecute(()->{
            var w=LiteraryVignettes.shared(f.data(),place);for(var who:ElkLotNight.MEN)ids.put(who.key(),w.getUUID(who.key()));w.putInt("CrashAt",0);w.putInt("Night",ElkLotNight.MEN_OUT-1);LiteraryVignettes.shared(f.data(),place,w);
        }).thenWaitUntil(()->{for(var who:ElkLotNight.MEN){var a=(LiteraryActor)f.out.getEntity(ids.get(who.key()));h.assertTrue(a!=null&&!ElkLotNight.inBar(a.position(),b),who.name()+" exits the actual furnishings: "+(a==null?"missing":a.position().subtract(b.getX(),b.getY(),b.getZ())+" leg="+a.getPersistentData().getInt("Leg0478")));}}).thenExecute(()->{
            for(var id:ids.values())h.assertTrue(f.out.getEntity(id) instanceof LiteraryActor,"the same original men walked out");h.assertTrue(f.out.getBlockState(b.offset(ElkLot.SIDE_DOOR)).getValue(DoorBlock.OPEN),"they leave the real side door open");
            var doorway=b.offset(ElkLot.SIDE_DOOR);var state=f.out.getBlockState(doorway);((DoorBlock)state.getBlock()).setOpen(p,f.out,state,doorway,false);
            f.at(p,place,-25.5,0,-16.5);var own=f.own(p,place);own.putBoolean("ElkSeen",true);LiteraryVignettes.save(f.data(),p.getUUID(),place,own);var night=LiteraryVignettes.shared(f.data(),place);night.putInt("Night",ElkLotNight.MEN_OUT+ElkLotNight.SEARCH+ElkLotNight.STAGE*3);LiteraryVignettes.shared(f.data(),place,night);
        }).thenWaitUntil(()->{var where=new StringBuilder();for(var who:ElkLotNight.MEN){var a=f.out.getEntity(ids.get(who.key()));where.append(" ").append(who.name()).append(a==null?" missing":" at "+a.position().subtract(b.getX(),b.getY(),b.getZ())+" leg="+a.getPersistentData().getInt("Leg0478")+" back="+a.getPersistentData().getInt("ReturnToBar0492")+(a instanceof LiteraryActor actor?" gesture="+actor.gesture():"")+" floating="+a.isNoGravity()+" sight="+f.out.clip(new net.minecraft.world.level.ClipContext(p.getEyePosition(),a.getEyePosition(),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,p)).getType());}
            var mine=f.own(p,place);var target=ElkLotEnding.menTarget(p.server);where.append("; side=").append(mine.getString("EndingSide0492")).append(" capture=").append(mine.getInt("CaptureTicks0492")).append(" target=").append(target==null?"none":target.getGameProfile().getName()).append(" here=").append(p.server.getPlayerList().getPlayers().stream().map(q->q.getGameProfile().getName()).toList());var nightNow=LiteraryVignettes.shared(f.data(),place);where.append(" stage=").append(ElkLotNight.stage(nightNow)).append(" night=").append(nightNow.getInt("Night")).append(" crash=").append(nightNow.getInt("CrashAt")).append(" current=").append(LiteraryVignettes.current(p)).append(" held=").append(ElkLotEnding.held(p)).append(" own=").append(mine);
            h.assertTrue(f.own(p,place).getBoolean("Ready"),"after the reader returns to the furnished bar, those same men open the closed side door and physically take them; reader at "+p.position().subtract(b.getX(),b.getY(),b.getZ())+", side door open="+f.out.getBlockState(b.offset(ElkLot.SIDE_DOOR)).getValue(DoorBlock.OPEN)+where);}).thenExecute(()->{
            h.assertTrue(ElkLotEnding.MEN.equals(f.own(p,place).getString("Outcome"))&&f.own(p,place).getBoolean("CaptureWitness0492"),"the complete outward and return route ends only with actual capture");
            var night=LiteraryVignettes.shared(f.data(),place);for(var who:ElkLotNight.MEN)h.assertTrue(ids.get(who.key()).equals(night.getUUID(who.key()))&&f.out.getEntity(ids.get(who.key())) instanceof LiteraryActor,"the complete ending retains every original man");
        }).thenSucceed();
    }
    private static GoatmanTests.Fixture goatman;
    @AfterBatch(batch="review492_cousin_talk") public static void cousinDone(ServerLevel l){if(goatman!=null){goatman.close();goatman=null;}}
    @GameTest(template="empty",batch="review492_cousin_talk",timeoutTicks=300)
    public static void nativeCousinClicksGiveDifferentRepliesDuringCookingVigilAndDawn(GameTestHelper h){
        goatman=new GoatmanTests.Fixture(h,492600);var f=goatman;var p=f.nativePlayer("cousin_talker");f.indoors(p);
        h.startSequence().thenWaitUntil(()->h.assertTrue(!f.cousins().isEmpty(),"a real native cousin is loaded")).thenExecute(()->{
            var c=f.cousins().getFirst();c.moveTo(f.b.getX()-2.5,f.b.getY()+1,f.b.getZ()-72.5);p.moveTo(f.b.getX()-.5,f.b.getY()+1,f.b.getZ()-72.5);p.setDeltaMovement(Vec3.ZERO);var r=f.run();r.putInt("Phase",GoatmanVignette.GATHERING);f.run(r);
            var click=new PlayerInteractEvent.EntityInteract(p,InteractionHand.MAIN_HAND,c);NeoForge.EVENT_BUS.post(click);h.assertTrue(click.isCanceled()&&!GoatmanVignette.personal(LabyrinthData.get(f.server),p.getUUID()).getString("LastCousinReply0492").isEmpty(),"the native entity click produces a visible reply");
            String first=GoatmanVignette.personal(LabyrinthData.get(f.server),p.getUUID()).getString("LastCousinReply0492");var own=GoatmanVignette.personal(LabyrinthData.get(f.server),p.getUUID());own.remove("CousinTalkAt0492");var data=LabyrinthData.get(f.server);var all=data.state(GoatmanVignette.ID);var players=all.getCompound("Players");players.put(p.getUUID().toString(),own);all.put("Players",players);data.setState(GoatmanVignette.ID,all);r=f.run();r.putInt("Phase",GoatmanVignette.VIGIL);f.run(r);
            h.assertTrue(GoatmanConversations.talk(p,c,InteractionHand.MAIN_HAND)&&!GoatmanVignette.personal(data,p.getUUID()).getString("LastCousinReply0492").equals(first),"the vigil has its own replies");
            String vigil=GoatmanVignette.personal(data,p.getUUID()).getString("LastCousinReply0492");own=GoatmanVignette.personal(data,p.getUUID());own.remove("CousinTalkAt0492");all=data.state(GoatmanVignette.ID);players=all.getCompound("Players");players.put(p.getUUID().toString(),own);all.put("Players",players);data.setState(GoatmanVignette.ID,all);r=f.run();r.putInt("Phase",GoatmanVignette.DAWN);f.run(r);
            h.assertTrue(GoatmanConversations.talk(p,c,InteractionHand.MAIN_HAND)&&!GoatmanVignette.personal(data,p.getUUID()).getString("LastCousinReply0492").equals(vigil),"concluded nights retain distinct dawn replies");
            var far=new PlayerInteractEvent.EntityInteract(p,InteractionHand.OFF_HAND,c);NeoForge.EVENT_BUS.post(far);h.assertTrue(!far.isCanceled(),"an offhand packet cannot double-deliver dialogue");p.moveTo(f.b.getX()+5,f.b.getY()+1,f.b.getZ()-72.5);h.assertTrue(!GoatmanConversations.talk(p,c,InteractionHand.MAIN_HAND),"unreachable cousins cannot be talked to remotely");
        }).thenSucceed();
    }
    @AfterBatch(batch="review492_ghosts_seats") public static void ghostsSeatsDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="review492_ghosts_seats",timeoutTicks=350)
    public static void savedGhostsDiningPosesLiftTheOriginalThighsOntoTheirActualSeats(GameTestHelper h){
        var f=fixture(h);var place=GhostsSet.PLACE;var p=f.player("ghost_seat_reader",place);var b=f.base(place);lease(f,place);var ids=new HashMap<Integer,UUID>();
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready()&&LiteraryVignettes.shared(f.data(),place).hasUUID("FilmSarah_"+p.getUUID()),"the original private family and actual chairs are loaded")).thenExecute(()->{
            var own=f.own(p,place);own.putInt("FilmDay",2);own.putBoolean("CastPlaced",false);own.putBoolean("Rolling",false);GhostsSet.reader(p,b,own);var world=LiteraryVignettes.shared(f.data(),place);
            for(int i=0;i<4;i++){var a=(LiteraryActor)f.in.getEntity(world.getUUID("Film"+GhostsSet.PEOPLE[i]+"_"+p.getUUID()));ids.put(i,a.getUUID());h.assertTrue(Math.abs(a.getY()-b.getY()-GhostsSet.seatedHeight(a.getScale()))<.001,"fresh placement fits each original cast scale");a.moveTo(a.getX(),b.getY()+.625,a.getZ());a.getPersistentData().remove("GhostSeat0492");a.load(a.saveWithoutId(new CompoundTag()));}
            int clock=own.getInt("ShotClock");GhostsSet.reader(p,b,own);for(var entry:ids.entrySet()){var a=(LiteraryActor)f.in.getEntity(entry.getValue());h.assertTrue(a!=null&&a.phase()==GhostsSet.SIT&&Math.abs(a.getY()-b.getY()-GhostsSet.seatedHeight(a.getScale()))<.001&&f.in.noCollision(a,a.getBoundingBox()),"saved originals fit the native seat without a replacement or collision: "+entry.getKey());}
            h.assertTrue(own.getInt("ShotClock")==clock&&!own.getBoolean("DayDone")&&!own.getBoolean("Ready"),"fitting the actors does not advance or complete the filmed day");
            var merry=(LiteraryActor)f.in.getEntity(ids.get(1));merry.appearance(merry.role(),GhostsSet.PRAY);merry.moveTo(merry.getX(),b.getY()+.625,merry.getZ());merry.getPersistentData().remove("GhostSeat0492");
            GhostsSet.reader(p,b,own);h.assertTrue(merry.phase()==GhostsSet.PRAY&&Math.abs(merry.getY()-b.getY()-GhostsSet.seatedHeight(merry.getScale()))<.001&&Math.abs(merry.getBbHeight()-1.25*merry.getScale())<.001&&f.in.noCollision(merry,merry.getBoundingBox()),"the praying child uses the fitted seated origin and collider while retaining the original pose and UUID");
            own.putBoolean("Completed",true);own.putBoolean("Ready",true);own.putString("Outcome","kept_the_recorded_day");
            for(var id:ids.values()){var a=(LiteraryActor)f.in.getEntity(id);a.moveTo(a.getX(),b.getY()+.625,a.getZ());a.getPersistentData().remove("GhostSeat0492");}
            var missing=ids.get(3);f.in.getEntity(missing).discard();GhostsSet.reader(p,b,own);
            for(int i=0;i<3;i++){var a=(LiteraryActor)f.in.getEntity(ids.get(i));h.assertTrue(a!=null&&Math.abs(a.getY()-b.getY()-GhostsSet.seatedHeight(a.getScale()))<.001,"concluded takes fit only their existing originals");}
            h.assertTrue(f.in.getEntity(missing)==null&&own.getBoolean("Completed")&&own.getString("Outcome").equals("kept_the_recorded_day")&&own.getInt("ShotClock")==clock,"missing originals, the exact outcome and the concluded take remain untouched");
        }).thenSucceed();
    }
}
