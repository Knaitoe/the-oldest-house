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
        var shape=p.serverLevel().getBlockState(at).getShape(p.serverLevel(),at).bounds();var target=new Vec3(at.getX()+(shape.minX+shape.maxX)/2,at.getY()+shape.maxY-.01,at.getZ()+(shape.minZ+shape.maxZ)/2);
        p.moveTo(at.getX()+.5,at.getY(),at.getZ()+2);p.setDeltaMovement(Vec3.ZERO);
        var event=new PlayerInteractEvent.RightClickBlock(p,hand,at,new BlockHitResult(target,Direction.SOUTH,at,false));NeoForge.EVENT_BUS.post(event);return event;
    }
    @GameTest(template="empty",batch="review492_nursery",timeoutTicks=300)
    public static void chestAndShelfToysTouchTheirOriginalSupportsAndTheChairSeatsANativePlayer(GameTestHelper h){
        var f=fixture(h);var place=ChildRoom.PLACE;var p=f.player("nursery_support",place);var b=f.base(place);var l=f.in;
        var chest=b.offset(ChildRoom.TOY_HOMES.get(1));var shelf=b.offset(-10,1,-16);var music=b.offset(-10,1,-15);
        h.assertTrue(l.getBlockState(chest).getValue(NurseryBlock.STAGE)==2&&l.getBlockState(shelf).getValue(NurseryBlock.STAGE)==1,"fresh toys use fitted variants without replacing any original prop");
        var doll=l.getBlockState(chest).getShape(l,chest).bounds();var lid=l.getBlockState(chest.below()).getShape(l,chest.below()).bounds();
        h.assertTrue(Math.abs(1+doll.minY-lid.maxY)<.001,"the doll's feet meet the chest lid");
        for(var at:List.of(shelf,music)){var top=l.getBlockState(at.below()).getShape(l,at.below()).bounds();var toy=l.getBlockState(at).getShape(l,at).bounds();h.assertTrue(toy.minX>=top.minX&&toy.maxX<=top.maxX&&toy.minZ>=top.minZ&&toy.maxZ<=top.maxZ&&toy.minY==0&&top.maxY==1,"shelf footprint supports the entire toy: "+at);}
        var mobile=b.offset(0,5,-22);h.assertTrue(NurseryBlock.is(l.getBlockState(mobile),NurseryBlock.Kind.MOBILE)&&!l.getBlockState(mobile.above()).isAir(),"the original mobile already reaches its ceiling");
        var chair=b.offset(7,0,-18);use(p,chair,InteractionHand.MAIN_HAND);h.assertTrue(p.getVehicle() instanceof SeatEntity,"the actual child's chair creates a native seat");p.stopRiding();h.assertTrue(!p.isPassenger(),"ordinary dismount releases the player");
        h.assertTrue(!f.own(p,place).getBoolean("Ready")&&!WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.of(place.id())),"handling furniture never grants the story");h.succeed();
    }
    @AfterBatch(batch="review492_nursery") public static void nurseryDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="review492_nursery_saved",timeoutTicks=400)
    public static void aSavedToyRepairWaitsForNativeSectionsCamerasAndBodiesAndNeverRestocksAnEdit(GameTestHelper h){
        var f=fixture(h);var place=ChildRoom.PLACE;var p=f.player("nursery_camera",place);var b=f.base(place);var l=f.in;lease(f,place);
        var chest=b.offset(ChildRoom.TOY_HOMES.get(1));var shelf=b.offset(-10,1,-16);var t=f.data().state(NurserySupport0492.STATE);t.remove(Long.toString(b.asLong()));f.data().setState(NurserySupport0492.STATE,t);
        l.setBlock(chest,l.getBlockState(chest).setValue(NurseryBlock.STAGE,0),18);l.setBlock(shelf,l.getBlockState(shelf).setValue(NurseryBlock.STAGE,3),18);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native entity sections load")).thenExecute(()->{
            p.setGameMode(GameType.SPECTATOR);f.at(p,place,-9,2,-20);h.assertTrue(!NurserySupport0492.repair(l,b),"a spectator's actual camera protects the props");
            var pig=EntityType.PIG.create(l);f.extras.add(pig);pig.setNoGravity(true);pig.moveTo(chest.getX()+.5,chest.getY(),chest.getZ()+.5);l.addFreshEntity(pig);f.at(p,place,0,0,100);
            h.assertTrue(!NurserySupport0492.repair(l,b),"an actual living body blocks the changed cell");pig.discard();
            h.assertTrue(NurserySupport0492.repair(l,b)&&l.getBlockState(chest).getValue(NurseryBlock.STAGE)==2&&l.getBlockState(shelf).getValue(NurseryBlock.STAGE)==3,"only the recognised original mount changes; an advanced hanging toy stays intact");
            l.setBlock(chest,Blocks.AIR.defaultBlockState(),18);f.reload();h.assertTrue(NurserySupport0492.repair(l,b)&&l.getBlockState(chest).isAir(),"the saved checkpoint never restores a later removal");
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

    @GameTest(template="empty",batch="review492_herd_custody",timeoutTicks=400)
    public static void loopCountersCannotCaptureTheReaderAndPrivateHerdsPreserveSavedOriginals(GameTestHelper h){
        var f=fixture(h);var place=ElkLot.PLACE;var p=f.player("herd_custody",place);var peer=f.player("herd_legacy",place);var b=f.base(place);lease(f,place);f.at(p,place,28,0,-85);f.at(peer,place,-22,0,-12);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready()&&LiteraryVignettes.shared(f.data(),place).hasUUID("Elk"),"the original cow and native field are ready")).thenExecute(()->{
            var legacy=f.own(peer,place);legacy.putBoolean("Ready",true);legacy.putBoolean("Completed",true);legacy.putString("Outcome","escaped_the_looping_field_and_read_own_clipping");LiteraryVignettes.save(f.data(),peer.getUUID(),place,legacy);var original=f.out.getEntity(LiteraryVignettes.shared(f.data(),place).getUUID("Elk"));var originalId=original.getUUID();
            var own=f.own(p,place);own.putBoolean("ElkSeen",true);own.putInt("FieldTicks",400);own.putInt("FieldLoops",200);ElkLotEnding.reader(p,b,own,LiteraryVignettes.shared(f.data(),place));LiteraryVignettes.save(f.data(),p.getUUID(),place,own);
            h.assertTrue(!own.getBoolean("Ready")&&own.getInt("CaptureTicks0492")==0&&!LiteraryVignettes.retreatLocked(p),"any number of loops still requires actual physical capture and permits retreat");
            var world=LiteraryVignettes.shared(f.data(),place);var elk=(LiteraryElk)f.out.getEntity(world.getUUID("EndingHerd0492_0_"+p.getUUID()));h.assertTrue(elk!=null&&elk.owner().orElseThrow().equals(p.getUUID())&&!elk.getUUID().equals(originalId),"the approaching herd has an explicit private reader");var elkId=elk.getUUID();
            elk.hurt(f.out.damageSources().playerAttack(peer),4);h.assertTrue(!f.own(peer,place).getBoolean("ElkWounded"),"hitting a private apparition cannot wound the original cow for another story");
            var saved=new CompoundTag();elk.saveWithoutId(saved);h.assertTrue(saved.getUUID("HerdOwner0492").equals(p.getUUID()),"native entity persistence retains the private rendering owner");
            elk.discard();f.reload();own=f.own(p,place);ElkLotEnding.reader(p,b,own,LiteraryVignettes.shared(f.data(),place));h.assertTrue(f.out.getEntity(elkId)==null&&LiteraryVignettes.shared(f.data(),place).getUUID("EndingHerd0492_0_"+p.getUUID()).equals(elkId),"a saved missing herd body is never refilled");
            var keep=f.own(peer,place);ElkLotEnding.reader(peer,b,keep,LiteraryVignettes.shared(f.data(),place));h.assertTrue(keep.getBoolean("Completed")&&keep.getString("Outcome").equals(legacy.getString("Outcome"))&&f.out.getEntity(originalId)==original,"legacy endings and the original shared cow survive");
        }).thenSucceed();
    }
    @AfterBatch(batch="review492_herd_custody") public static void herdDone(ServerLevel l){close();}
}
