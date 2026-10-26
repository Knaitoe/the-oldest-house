package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import java.util.function.BiConsumer;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;

/** Actual controller use, collision, original custody and independent outcomes for the photographed defects. */
@GameTestHolder(TheOldestHouse.MOD_ID+"_uncanny") @PrefixGameTestTemplate(false)
public final class Playtest0127Tests {
    private static LiteraryTests.Fixture fixture;
    private static void close(){if(fixture!=null){fixture.close();fixture=null;}}
    private static LiteraryTests.Fixture make(GameTestHelper h){var f=new LiteraryTests.Fixture(h);fixture=f;HouseSavedData.get(h.getLevel().getServer()).markInteriorInitialized();return f;}
    private static void run(GameTestHelper h,LabyrinthPlace scene,BiConsumer<LiteraryTests.Fixture,ServerPlayer> action){
        var f=make(h);var p=f.player("review127",scene);f.chunks.hold(f.level(scene),IndianLakeRooms.bounds(f.base(scene),scene));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"actual room and entity sections loaded")).thenExecute(()->action.accept(f,p)).thenSucceed();
    }
    private static void use(ServerPlayer p,BlockPos at){p.gameMode.useItemOn(p,p.serverLevel(),p.getMainHandItem(),InteractionHand.MAIN_HAND,new BlockHitResult(at.getCenter(),Direction.SOUTH,at,false));}
    private static CompoundTag step(LiteraryTests.Fixture f,ServerPlayer p,LabyrinthPlace scene){var own=f.own(p,scene);UncannyStories.reader(p,f.base(scene),scene,own);LiteraryVignettes.save(f.data(),p.getUUID(),scene,own);return own;}

    @GameTest(template="empty",batch="review127_mail",timeoutTicks=500)
    public static void everyMailboxFacingCanBePickedOpenedAndClosedThroughNativeUse(GameTestHelper h){run(h,LabyrinthPlace.MOTION_LIGHT,(f,p)->{
        var l=f.level(LabyrinthPlace.MOTION_LIGHT);var b=f.base(LabyrinthPlace.MOTION_LIGHT);var at=b.offset(0,1,-9);p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        for(var face:Direction.Plane.HORIZONTAL){
            l.setBlock(at,UncannyFurnitureBlock.of(UncannyFurnitureBlock.Kind.MAILBOX,face),3);
            var n=Vec3.atLowerCornerOf(face.getNormal());p.moveTo(at.getX()+.5+n.x*2,b.getY(),at.getZ()+.5+n.z*2);
            var hit=l.clip(new ClipContext(p.getEyePosition(),at.getCenter(),ClipContext.Block.OUTLINE,ClipContext.Fluid.NONE,p));
            h.assertTrue(hit.getType()==HitResult.Type.BLOCK&&hit.getBlockPos().equals(at),"the actual mailbox mesh is reachable from "+face);
            p.gameMode.useItemOn(p,l,ItemStack.EMPTY,InteractionHand.MAIN_HAND,hit);
            h.assertTrue(l.getBlockState(at).getValue(UncannyFurnitureBlock.OPEN),"native use opens the mailbox: "+face);
            p.gameMode.useItemOn(p,l,ItemStack.EMPTY,InteractionHand.MAIN_HAND,hit);
            h.assertTrue(!l.getBlockState(at).getValue(UncannyFurnitureBlock.OPEN),"native use closes the same original: "+face);
        }
    });}
    @AfterBatch(batch="review127_mail") public static void mailDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="review127_frames",timeoutTicks=500)
    public static void touchingEitherFalseDoorHalfExaminesItAndItsAutomaticOpeningStillStopsANativeBody(GameTestHelper h){run(h,LabyrinthPlace.MOTION_LIGHT,(f,p)->{
        var scene=LabyrinthPlace.MOTION_LIGHT;var peer=f.player("frame_peer127",scene);var l=f.level(scene);var b=f.base(scene);var rel=UncannyRooms.FALSE_DOORS.get(2);var at=b.offset(rel);
        for(int y=0;y<2;y++)l.setBlock(at.above(y),UncannyPropBlock.of(UncannyPropBlock.Kind.FALSE_DOOR,Direction.SOUTH).setValue(UncannyPropBlock.STAGE,y),3);
        p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);f.at(p,scene,.5,0,-.5);use(p,at);use(p,at.above());
        h.assertTrue(f.own(p,scene).getBoolean("FalseExamined")&&!f.own(peer,scene).getBoolean("FalseExamined"),"examination belongs to the actual reader");
        h.assertTrue(l.getBlockState(at).getValue(UncannyPropBlock.STAGE)==0&&l.getBlockState(at.above()).getValue(UncannyPropBlock.STAGE)==1,"neither half is opened by a hand");
        for(int y=0;y<2;y++)l.setBlock(at.above(y),l.getBlockState(at.above(y)).setValue(UncannyPropBlock.STAGE,y+2),3);
        p.move(MoverType.SELF,new Vec3(0,0,-4));h.assertTrue(p.getZ()>b.getZ()-1.4&&p.getY()>=b.getY(),"the visible black backing stops actual movement into the unbuilt space");
        for(var facing:Direction.Plane.HORIZONTAL){var state=UncannyPropBlock.of(UncannyPropBlock.Kind.FALSE_DOOR,facing).setValue(UncannyPropBlock.STAGE,2);h.assertTrue(!state.getCollisionShape(l,at).isEmpty(),"every automatic opening retains real backing: "+facing);}
    });}
    @AfterBatch(batch="review127_frames") public static void framesDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="review127_cuts",timeoutTicks=500)
    public static void nativeMiningCutsEveryTissueLayerAndNeverWearsTheCleaver(GameTestHelper h){run(h,LabyrinthPlace.LAYERED_MAN,(f,p)->{
        var scene=LabyrinthPlace.LAYERED_MAN;var peer=f.player("cut_peer127",scene);var l=f.level(scene);var b=f.base(scene);var at=b.offset(3,-4,-15);f.at(p,scene,3.5,-5,-13.5);
        var cleaver=new ItemStack(UncannyRegistry.CLEAVER.get());p.setItemInHand(InteractionHand.MAIN_HAND,cleaver);int before=f.own(p,scene).getInt("Peeled");
        for(int i=0;i<8;i++){
            l.setBlock(at,UncannyRegistry.FLESH.get().defaultBlockState().setValue(UncannyFleshBlock.LAYER,i),3);
            h.assertTrue(LiteraryVignettes.mayBreak(p,at)&&p.gameMode.destroyBlock(at)&&l.isEmptyBlock(at),"the native controller mines tissue layer "+i);
        }
        h.assertTrue(f.own(p,scene).getInt("Peeled")==before+8&&f.own(peer,scene).getInt("Peeled")==0,"ordinary completed mining records only its actual cutter");
        cleaver.hurtAndBreak(200,p,EquipmentSlot.MAINHAND);h.assertTrue(!cleaver.isEmpty()&&cleaver.getDamageValue()==0&&cleaver.has(DataComponents.UNBREAKABLE),"the actual held cleaver has hidden native unbreakability");
        h.assertTrue(l.getBlockState(b.offset(-1,-2,-21)).getValue(UncannyFleshBlock.LAYER)==4&&l.getBlockState(b.offset(1,-2,-21)).getValue(UncannyFleshBlock.LAYER)==5&&l.getBlockState(b.offset(0,-3,-21)).getValue(UncannyFleshBlock.LAYER)==6,"the actual authored man has two eyes and a mouth above his abdomen");
        h.assertTrue(l.getBlockState(b.offset(-1,-2,-20)).isAir()&&l.getBlockState(b.offset(1,-2,-20)).isAir()&&l.getBlockState(b.offset(0,-3,-20)).isAir(),"neither the shoulders nor the abdomen bury the visible facial surfaces");
        h.assertTrue(l.isEmptyBlock(b.offset(UncannyRooms.KEY_CAVITY))&&UncannyFleshBlock.is(l.getBlockState(b.offset(0,-4,-21))),"the original key cavity remains inside actual tissue");f.reload();h.assertTrue(cleaver.getDamageValue()==0&&f.own(p,scene).getInt("Peeled")==before+8,"native reload retains cutting history and the same tool");
    });}
    @AfterBatch(batch="review127_cuts") public static void cutsDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="review127_overrun",timeoutTicks=600)
    public static void theWorkerCallsBeforeReadingAndAnActualBoundaryBreachRecordsFailure(GameTestHelper h){
        var f=make(h);var scene=LabyrinthPlace.LAYERED_MAN;var p=f.player("overrun127",scene);f.chunks.hold(f.level(scene),IndianLakeRooms.bounds(f.base(scene),scene));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the cellar is loaded")).thenExecute(()->{
            var own=f.own(p,scene);UncannyStories.arrive(p,scene,own);LiteraryVignettes.save(f.data(),p.getUUID(),scene,own);
            h.assertTrue(own.getBoolean("HelpCalled127")&&!own.getBoolean("Read_Source"),"the original worker asks for help on physical entry");
            f.level(scene).setBlock(f.base(scene).offset(6,-4,-16),UncannyFleshBlock.at(new BlockPos(6,-4,-16)),3);
        }).thenWaitUntil(()->h.assertTrue(f.own(p,scene).getBoolean("UncannyFailed"),"the actual boundary growth is registered"))
        .thenExecute(()->{var own=f.own(p,scene);h.assertTrue(own.getString("Failure").equals("basement_overrun")&&!own.getBoolean("Ready")&&!WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.LAYERED_MAN),"overrun is a saved failed attempt, never a survival account");f.reload();h.assertTrue(f.own(p,scene).getBoolean("UncannyFailed"),"the failure survives SavedData reload");}).thenSucceed();
    }
    @AfterBatch(batch="review127_overrun") public static void overrunDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="review127_family",timeoutTicks=500)
    public static void familyContactEndsOnlyTheTouchedReaderWithoutRequiringACornerOrBook(GameTestHelper h){run(h,LabyrinthPlace.PAINTED_INTERIOR,(f,p)->{
        var scene=LabyrinthPlace.PAINTED_INTERIOR;var peer=f.player("family_peer127",scene);var own=f.own(p,scene);UncannyStories.arrive(p,scene,own);LiteraryVignettes.save(f.data(),p.getUUID(),scene,own);
        var a=UncannyStories.actor(p,own,"CastP0");h.assertTrue(a!=null,"the actual saved family exists");p.moveTo(a.position());own=step(f,p,scene);
        h.assertTrue(own.getBoolean("UncannyFailed")&&own.getString("Failure").equals("touched_by_the_family")&&!own.getBoolean("Read_Source"),"actual contact triggers an ending independently of peripheral gaze and corner checks");
        h.assertTrue(!f.own(peer,scene).getBoolean("UncannyFailed")&&!WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.PAINTED_INTERIOR),"a peer receives no ending and contact grants no survival shortcut");
    });}
    @AfterBatch(batch="review127_family") public static void familyDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="review127_courtesy",timeoutTicks=700)
    public static void threeDeliberateFriendlyHitsWarnBeforeTheFourthInterrupts(GameTestHelper h){
        var f=make(h);var scene=LabyrinthPlace.SECOND_WAITRESS;var p=f.player("courtesy127",scene);f.chunks.hold(f.level(scene),IndianLakeRooms.bounds(f.base(scene),scene));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the original cast is loaded")).thenExecute(()->{UncannyStories.prepare(f.level(scene),f.base(scene),scene);var a=UncannyDiner.waitress(p,f.base(scene));h.assertTrue(a!=null,"the actual waitress exists");p.moveTo(a.position().add(0,0,1));p.attack(a);})
        .thenIdle(12).thenExecute(()->p.attack(UncannyDiner.waitress(p,f.base(scene))))
        .thenIdle(12).thenExecute(()->{p.attack(UncannyDiner.waitress(p,f.base(scene)));var own=f.own(p,scene);h.assertTrue(own.getInt("FriendlyStrikes127")==3&&!own.getBoolean("Interrupted")&&LiteraryVignettes.inside(p,scene),"three separate real hits warn without ejection");})
        .thenIdle(12).thenExecute(()->{p.attack(UncannyDiner.waitress(p,f.base(scene)));h.assertTrue(f.own(p,scene).getBoolean("Interrupted"),"the fourth deliberate hit ends the friendly visit");}).thenSucceed();
    }
    @AfterBatch(batch="review127_courtesy") public static void courtesyDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="review127_hostile",timeoutTicks=500)
    public static void strikingTheActualTunnelWomanNeverUsesFriendlyDiscipline(GameTestHelper h){run(h,LabyrinthPlace.TUNNEL_PERIPHERY,(f,p)->{
        var scene=LabyrinthPlace.TUNNEL_PERIPHERY;var own=f.own(p,scene);UncannyStories.arrive(p,scene,own);LiteraryVignettes.save(f.data(),p.getUUID(),scene,own);var a=UncannyStories.actor(p,own,"CastGiant");h.assertTrue(a!=null&&a.phase()==0,"the original begins dormant beyond the entrance");p.moveTo(a.position().add(0,0,1));
        for(int i=0;i<5;i++)p.attack(a);own=f.own(p,scene);h.assertTrue(!own.getBoolean("Interrupted")&&own.getInt("FriendlyStrikes127")==0&&LiteraryVignettes.inside(p,scene),"a hostile creature may be struck without any friendly warning or ejection");
    });}
    @AfterBatch(batch="review127_hostile") public static void hostileDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="review127_hosting",timeoutTicks=1800)
    public static void theOriginalWaitressWalksTheReaderToTheBoothAndReturnsBeforeTheSecondAppears(GameTestHelper h){
        var f=make(h);var scene=LabyrinthPlace.SECOND_WAITRESS;var p=f.player("seated127",scene);f.chunks.hold(f.level(scene),IndianLakeRooms.bounds(f.base(scene),scene));boolean[] begun={false};
        h.onEachTick(()->{if(!begun[0])return;var own=f.own(p,scene);if(own.getBoolean("DinerHosted127")||own.getBoolean("DinerSeated127"))return;var a=UncannyDiner.waitress(p,f.base(scene));if(a==null)return;
            if(own.getInt("DinerHostLeg127")>=4){var seat=f.base(scene).offset(UncannyDiner.SEAT);if(p.distanceToSqr(seat.getCenter())>9)SceneFootpaths.step(p,new Vec3(seat.getX()-1,seat.getY(),seat.getZ()+.5),.18);else use(p,seat);}
            else SceneFootpaths.step(p,a.position().add(.8,0,.8),.18);
        });
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the native diner is loaded")).thenExecute(()->{UncannyStories.prepare(f.level(scene),f.base(scene),scene);var a=UncannyDiner.waitress(p,f.base(scene));h.assertTrue(a!=null,"the actual waitress exists");p.moveTo(a.position().add(0,0,1));p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);p.interactOn(a,InteractionHand.MAIN_HAND);begun[0]=true;})
        .thenWaitUntil(()->h.assertTrue(f.own(p,scene).getBoolean("DinerHosted127"),"the same waitress completes her collision-bound seating and return route: "+f.own(p,scene)))
        .thenExecute(()->{var own=f.own(p,scene);var a=UncannyDiner.waitress(p,f.base(scene));var fake=UncannyStories.actor(p,own,"CastFake");h.assertTrue(own.getBoolean("DinerSeated127")&&p.getVehicle() instanceof SeatEntity&&a.getZ()>f.base(scene).getZ()-10&&fake!=null&&fake.phase()==0&&!own.getBoolean("Read_Source"),"actual seating and the original waitress's return precede the second's appearance, without a book gate");p.stopRiding();}).thenSucceed();
    }
    @AfterBatch(batch="review127_hosting") public static void hostingDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="review127_lot",timeoutTicks=700)
    public static void oneRealPatronContactConcludesWithoutAnElkSightingAndHidesOnlyForThatReader(GameTestHelper h){run(h,LabyrinthPlace.ELK_LOT,(f,p)->{
        var scene=LabyrinthPlace.ELK_LOT;var peer=f.player("lot_peer127",scene);var b=f.base(scene);f.at(p,scene,.5,0,-8.5);ElkLotNight.reader(p,b,f.own(p,scene));
        var d=f.data();var night=LiteraryVignettes.shared(d,scene);night.putInt("CrashAt",0);night.putInt("Night",ElkLotNight.MEN_OUT+ElkLotNight.SEARCH+2*ElkLotNight.STAGE);LiteraryVignettes.shared(d,scene,night);
        var own=f.own(p,scene);own.putBoolean("ElkSeen",false);var men=new ArrayList<LiteraryActor>();
        for(var person:ElkLotNight.MEN){var a=LiteraryVignettes.actor(p,scene,person.key(),LiteraryActor.STRANGER,BlockPos.containing(person.home(BlockPos.ZERO)),false);h.assertTrue(a!=null,"the original patron exists");a.appearance(LiteraryActor.STRANGER,person.phase());a.moveTo(p.position().add(.7,0,0));men.add(a);}
        for(int i=0;i<18&&!own.getBoolean("Ready");i++){LiteraryVignettes.save(d,p.getUUID(),scene,own);var before=men.getFirst().position();ElkLotEnding.reader(p,b,own,night);h.assertTrue(men.getFirst().position().subtract(before).horizontalDistance()<=.1,"capture approaches are walking steps");}
        LiteraryVignettes.save(d,p.getUUID(),scene,own);h.assertTrue(own.getBoolean("Ready")&&own.getString("Outcome").equals(ElkLotEnding.MEN)&&!own.getBoolean("ElkSeen"),"physical contact itself starts and completes the personal conclusion");
        h.assertTrue(!f.own(peer,scene).getBoolean("Ready")&&!men.getFirst().broadcastToPlayer(p)&&men.getFirst().broadcastToPlayer(peer),"the same patrons disappear only for the concluded reader, retaining their identity for peers");
    });}
    @AfterBatch(batch="review127_lot") public static void lotDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="review127_bed",timeoutTicks=700)
    public static void pushingTheBedMovesBothOriginalHalvesAndLeavesTheHatchClear(GameTestHelper h){run(h,LabyrinthPlace.CHILD_ROOM,(f,p)->{
        var scene=LabyrinthPlace.CHILD_ROOM;var l=f.level(scene);var b=f.base(scene);var head=l.getBlockState(b.offset(ChildRoom.BED_HEAD));var foot=l.getBlockState(b.offset(ChildRoom.BED_FOOT));f.at(p,scene,-1.5,0,-21.5);p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);use(p,b.offset(ChildRoom.BED_FOOT));
        h.assertTrue(l.isEmptyBlock(b.offset(ChildRoom.BED_HEAD))&&l.isEmptyBlock(b.offset(ChildRoom.BED_FOOT))&&l.getBlockState(b.offset(ChildRoom.BED_HEAD.west())).equals(head)&&l.getBlockState(b.offset(ChildRoom.BED_FOOT.west())).equals(foot),"the actual paired bed slides aside, preserving its native states");
        h.assertTrue(l.getBlockState(b.offset(ChildRoom.GAP)).getBlock() instanceof TrapDoorBlock,"the discovered hatch is no longer obscured by the bed");f.reload();use(p,b.offset(ChildRoom.BED_FOOT.west()));h.assertTrue(l.isEmptyBlock(b.offset(ChildRoom.BED_FOOT))&&l.getBlockState(b.offset(ChildRoom.BED_FOOT.west())).equals(foot),"a saved interaction never moves or duplicates the bed again");
    });}
    @AfterBatch(batch="review127_bed") public static void bedDone(ServerLevel l){close();}
}
