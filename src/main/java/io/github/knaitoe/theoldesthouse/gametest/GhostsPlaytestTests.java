package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

/** Actual input, original bodies and the routes reported in the owner's Ghosts playtest. */
@GameTestHolder(TheOldestHouse.MOD_ID+"_ghosts") @PrefixGameTestTemplate(false)
public final class GhostsPlaytestTests {
    private static LiteraryTests.Fixture active;
    private static final LabyrinthPlace P=GhostsSet.PLACE;
    private static void close(){Talk.clearAll();if(active!=null){active.close();active=null;}}
    @AfterBatch(batch="ghosts_camera_words318") public static void wordsDone(ServerLevel l){close();}
    @AfterBatch(batch="ghosts_sister_spacing318") public static void sistersDone(ServerLevel l){close();}
    @AfterBatch(batch="ghosts_family_blocking318") public static void familyDone(ServerLevel l){close();}
    private static LiteraryTests.Fixture fixture(GameTestHelper h){active=new LiteraryTests.Fixture(h);return active;}
    private static LiteraryActor actor(LiteraryTests.Fixture f,ServerPlayer p,int role){return f.in.getEntitiesOfClass(LiteraryActor.class,IndianLakeRooms.bounds(f.base(P),P),a->a.role()==role&&a.owner().filter(p.getUUID()::equals).isPresent()).stream().findFirst().orElse(null);}
    private static void save(LiteraryTests.Fixture f,ServerPlayer p,CompoundTag own){LiteraryVignettes.save(f.data(),p.getUUID(),P,own);}
    private static void scene(LiteraryTests.Fixture f,ServerPlayer p,int day,int clock){var own=f.own(p,P);own.putInt("FilmDay",day);own.putInt("ShotClock",clock);own.putBoolean("Rolling",true);own.putBoolean("DayDone",false);own.putBoolean("CastPlaced",false);save(f,p,own);}
    private static boolean resident(LiteraryTests.Fixture f,ServerPlayer p){return f.chunks.ready()&&Arrays.stream(GhostsSet.ROLES).allMatch(r->actor(f,p,r)!=null);}

    @GameTest(template="empty",batch="ghosts_camera_words318",timeoutTicks=1800)
    public static void actualCameraUseLeavesFamilySpeechReadableAndItsHistoryPrivate(GameTestHelper h){
        var f=fixture(h);var p=f.player("camera_words",P);var peer=f.player("camera_peer",P);f.source(p,P);var b=f.base(P);f.chunks.hold(f.in,IndianLakeRooms.bounds(b,P));ItemStack[] camera={ItemStack.EMPTY};
        h.startSequence().thenWaitUntil(()->h.assertTrue(resident(f,p),"the original family is resident"))
            .thenExecute(()->{
                var merry=actor(f,p,LiteraryActor.GHOST_MERRY);int slot=-1;for(int i=0;i<p.getInventory().items.size();i++)if(p.getInventory().items.get(i).is(LiteraryRegistry.CAMCORDER.get()))slot=i;
                h.assertTrue(slot>=0,"the actual owned camera was issued");camera[0]=p.getInventory().removeItemNoUpdate(slot);p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);p.moveTo(merry.position().add(0,0,1.5));NativeTestPlayers.messages(p);
                merry.interact(p,InteractionHand.MAIN_HAND);p.setItemInHand(InteractionHand.MAIN_HAND,camera[0]);
                camera[0].use(p.level(),p,InteractionHand.MAIN_HAND);p.releaseUsingItem();h.assertTrue(GhostsSet.recording(p),"the actual first input starts recording");camera[0].use(p.level(),p,InteractionHand.MAIN_HAND);p.releaseUsingItem();h.assertTrue(!GhostsSet.recording(p),"the actual next input stops it");
            }).thenIdle(3).thenExecute(()->{
                var messages=NativeTestPlayers.messages(p);h.assertTrue(messages.stream().anyMatch(m->m.startsWith("Merry:"))&&messages.stream().noneMatch(m->m.equals("Recording.")||m.equals("Stopped recording.")),"camera status never replaces the actual family line: "+messages);
                h.assertTrue(TalkHistory.conversations(p.server,p.getUUID()).stream().map(CompoundTag.class::cast).flatMap(t->t.getList("Lines",10).stream()).map(CompoundTag.class::cast).anyMatch(t->t.getString("S").equals("Merry")&&t.getString("T").equals(messages.stream().filter(m->m.startsWith("Merry:")).findFirst().orElseThrow().substring(7))),"the same delivered words enter the journal");
                String heard=messages.stream().filter(m->m.startsWith("Merry:")).findFirst().orElseThrow().substring(7);h.assertTrue(TalkHistory.conversations(peer.server,peer.getUUID()).stream().map(CompoundTag.class::cast).flatMap(t->t.getList("Lines",10).stream()).map(CompoundTag.class::cast).noneMatch(l->l.getString("S").equals("Merry")&&l.getString("T").equals(heard)),"the nearby friend's history contains none of this private family conversation");
                scene(f,p,4,40);f.at(p,P,7,-4,-18);var own=f.own(p,P);p.setXRot(-20);h.assertTrue(!GhostsSet.privacyPull(p,own),"a stopped camera does not pull the view");camera[0].use(p.level(),p,InteractionHand.MAIN_HAND);p.releaseUsingItem();h.assertTrue(GhostsSet.privacyPull(p,own),"only the live held owned camera receives the privacy cue");
                GhostsSet.reader(p,b,own);h.assertTrue(p.getXRot()==-20,"the server never imposes a replacement pitch on the reader or recording");p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);h.assertTrue(!GhostsSet.privacyPull(p,own),"stowing the camera stops the cue immediately");
                f.reload();h.assertTrue(!TalkHistory.conversations(p.server,p.getUUID()).isEmpty(),"the heard family survives a real SavedData reload");
            }).thenSucceed();
    }

    @GameTest(template="empty",batch="ghosts_sister_spacing318",timeoutTicks=2600)
    public static void theOriginalSistersUseThePickupInSingleFileAndStopApart(GameTestHelper h){
        var f=fixture(h);var p=f.player("sisters_space",P);f.source(p,P);var b=f.base(P);f.chunks.hold(f.in,IndianLakeRooms.bounds(b,P));UUID[] ids=new UUID[2];
        h.startSequence().thenWaitUntil(()->h.assertTrue(resident(f,p),"the original sisters are resident"))
            .thenExecute(()->{ids[0]=actor(f,p,LiteraryActor.GHOST_MARJORIE).getUUID();ids[1]=actor(f,p,LiteraryActor.GHOST_MERRY).getUUID();scene(f,p,4,575);var own=f.own(p,P);own.putBoolean("BedroomPickup",true);own.putInt("PickupStep0",0);own.putInt("PickupStep1",0);own.putBoolean("Rolling",false);save(f,p,own);f.at(p,P,5,5,-16);})
            .thenWaitUntil(()->{
                var own=f.own(p,P);var a=actor(f,p,LiteraryActor.GHOST_MARJORIE);var c=actor(f,p,LiteraryActor.GHOST_MERRY);
                h.assertFalse(own.getBoolean("CastPlaced")&&a.getBoundingBox().deflate(.02).intersects(c.getBoundingBox().deflate(.02)),"their original bodies overlap during the real pickup: "+a.position()+" / "+c.position());
                h.assertTrue(GhostsSet.pickupReady(own),"both sisters walk the actual stairs to their distinct marks: "+own.getInt("PickupStep0")+" / "+own.getInt("PickupStep1"));
            }).thenExecute(()->{
                var a=actor(f,p,LiteraryActor.GHOST_MARJORIE);var c=actor(f,p,LiteraryActor.GHOST_MERRY);h.assertTrue(a.getUUID().equals(ids[0])&&c.getUUID().equals(ids[1])&&a.position().distanceToSqr(c.position())>2,"distinct stopping positions preserve the original sisters before the box scene");
            }).thenSucceed();
    }

    @GameTest(template="empty",batch="ghosts_family_blocking318",timeoutTicks=2400)
    public static void johnReleasesHisHelpingPoseAndTheFamilyReactsToTheSameDaughtersExit(GameTestHelper h){
        var f=fixture(h);var p=f.player("family_exit",P);f.source(p,P);var b=f.base(P);f.chunks.hold(f.in,IndianLakeRooms.bounds(b,P));UUID[] original=new UUID[4];
        h.startSequence().thenWaitUntil(()->h.assertTrue(resident(f,p),"the original family and bedroom are resident"))
            .thenExecute(()->{original[0]=actor(f,p,LiteraryActor.GHOST_MARJORIE).getUUID();original[1]=actor(f,p,LiteraryActor.GHOST_JOHN).getUUID();original[2]=actor(f,p,LiteraryActor.GHOST_SARAH).getUUID();original[3]=actor(f,p,LiteraryActor.GHOST_PRIEST).getUUID();scene(f,p,3,95);f.at(p,P,-4.5,5,-19.5);})
            .thenWaitUntil(()->h.assertTrue(actor(f,p,LiteraryActor.GHOST_JOHN).phase()==GhostsSet.HELP,"John reaches the wall around the actual bed before helping"))
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getInt("ShotClock")>=440,"the wall incident ends"))
            .thenExecute(()->{
                var john=actor(f,p,LiteraryActor.GHOST_JOHN);h.assertTrue(john.phase()==GhostsSet.STAND&&Math.abs(john.getY()-b.getY()-5)<.1,"John lowers his arms and remains on the real floor: "+john.position()+" / "+john.phase());
                var facing=GhostsSetRooms.EXORCISM_FACING;var target=b.offset(GhostsSetRooms.EXORCISM_DESK).getCenter().add(facing.getStepX()*.7,.1,facing.getStepZ()*.7);f.at(p,P,-5.5,5,-15.5);p.lookAt(EntityAnchorArgument.Anchor.EYES,target);var ray=p.serverLevel().clip(new net.minecraft.world.level.ClipContext(p.getEyePosition(),target,net.minecraft.world.level.ClipContext.Block.VISUAL,net.minecraft.world.level.ClipContext.Fluid.NONE,p));h.assertTrue(GhostsSet.framed(p,target),"the relocated drawer is visible from the bedroom doorway: "+ray.getBlockPos()+" / "+p.serverLevel().getBlockState(ray.getBlockPos()));
                scene(f,p,6,590);f.at(p,P,-4.5,5,-19.5);
            }).thenWaitUntil(()->{var own=f.own(p,P);h.assertTrue(own.getBoolean("FamilyShoved")&&actor(f,p,LiteraryActor.GHOST_JOHN).position().distanceToSqr(Vec3.atLowerCornerOf(b).add(-3.5,5,-12.5))<.08&&actor(f,p,LiteraryActor.GHOST_SARAH).position().distanceToSqr(Vec3.atLowerCornerOf(b).add(-5.5,5,-13.3))<.08&&actor(f,p,LiteraryActor.GHOST_PRIEST).position().distanceToSqr(Vec3.atLowerCornerOf(b).add(-5.5,5,-14.6))<.08,"the contact displaces John and the family follows through actual native ticks: "+own.getBoolean("FamilyShoved")+" / "+own.getInt("FamilyFollow2")+" / "+own.getInt("FamilyFollow3")+" / "+own.getInt("FamilyFollow5"));})
            .thenExecute(()->{
                int[] roles={LiteraryActor.GHOST_MARJORIE,LiteraryActor.GHOST_JOHN,LiteraryActor.GHOST_SARAH,LiteraryActor.GHOST_PRIEST};for(int i=0;i<roles.length;i++)h.assertTrue(actor(f,p,roles[i]).getUUID().equals(original[i]),"reactions and following never replace a native original");h.assertTrue(actor(f,p,LiteraryActor.GHOST_JOHN).phase()==GhostsSet.STAND,"the recoil is finite");
            }).thenSucceed();
    }
    @AfterBatch(batch="ghosts_scene_history318") public static void sceneHistoryDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="ghosts_scene_history318",timeoutTicks=1800)
    public static void heardSceneDialogueIsKeptButAnAbsentPeerGetsNoTranscript(GameTestHelper h){
        var f=fixture(h);var p=f.player("scene_hearer",P);var peer=f.player("absent_hearer",P);f.source(p,P);var b=f.base(P);f.chunks.hold(f.in,IndianLakeRooms.bounds(b,P));
        h.startSequence().thenWaitUntil(()->h.assertTrue(resident(f,p),"the scene's original speakers are resident")).thenExecute(()->{
            f.at(peer,P,0,0,40);scene(f,p,2,95);f.at(p,P,-4,0,-15);NativeTestPlayers.messages(p);
            var own=f.own(p,P);GhostsSet.reader(p,b,own);save(f,p,own);
        }).thenIdle(3).thenExecute(()->{
            var delivered=NativeTestPlayers.messages(p);h.assertTrue(delivered.stream().anyMatch(line->line.startsWith("Merry:")),"the actual scene delivers Merry's speech while the camera is stopped: "+delivered);
            String said=delivered.stream().filter(line->line.startsWith("Merry:")).findFirst().orElseThrow().substring(7);
            h.assertTrue(TalkHistory.conversations(p.server,p.getUUID()).stream().map(CompoundTag.class::cast).filter(t->t.getString("Scene").equals("ghosts/day/2")).flatMap(t->t.getList("Lines",10).stream()).map(CompoundTag.class::cast).anyMatch(line->line.getString("S").equals("Merry")&&line.getString("T").equals(said)),"the journal keeps the heard line under the actual filming day");
            h.assertTrue(TalkHistory.conversations(peer.server,peer.getUUID()).stream().map(CompoundTag.class::cast).noneMatch(t->t.getString("Scene").equals("ghosts/day/2")),"leaving the set gives the absent peer no transcript of a scene they did not hear");
        }).thenSucceed();
    }

}
