package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.*;

/** 0.4.146: friends go into a vignette together; one story per thread, with an account for each way of being there. */
@GameTestHolder(TheOldestHouse.MOD_ID+"_coop") @PrefixGameTestTemplate(false)
public final class CoopTests {
    private static LiteraryTests.Fixture active;
    private static void close(){GroupCrossings.forget();SharedVignettes.undraft(LabyrinthPlace.HILL_NURSERY);DrownedTown.clearAll();HouseTransitionEvents.clearAll();if(active!=null){active.close();active=null;}}
    @AfterBatch(batch="coop_follow146") public static void followDone(ServerLevel l){close();}
    @AfterBatch(batch="coop_mixed146") public static void mixedDone(ServerLevel l){close();}
    @AfterBatch(batch="coop_thread146") public static void threadDone(ServerLevel l){close();}
    @AfterBatch(batch="coop_town148") public static void townDone(ServerLevel l){close();}
    private static final LabyrinthPlace SCENE=LabyrinthPlace.HILL_NURSERY;
    private static ServerPlayer player(GameTestHelper h,LiteraryTests.Fixture f,String name){var p=NativeTestPlayers.survival(h,name);f.players.add(p);return p;}
    private static void at(ServerPlayer p,ServerLevel l,BlockPos at){p.stopRiding();p.teleportTo(l,at.getX()+.5,at.getY(),at.getZ()+.5,180,0);p.setDeltaMovement(Vec3.ZERO);p.connection.resetPosition();p.hasChangedDimension();p.resetFallDistance();}
    private static void click(ServerPlayer p,BlockPos at){p.gameMode.useItemOn(p,p.serverLevel(),p.getMainHandItem(),InteractionHand.MAIN_HAND,new BlockHitResult(at.getCenter(),Direction.SOUTH,at,false));}
    private static void hold(LiteraryTests.Fixture f,LabyrinthPlace place){var slot=LabyrinthPlaces.slotBounds(f.origin,place);f.chunks.hold(f.level(place),new AABB(slot.minX(),slot.minY(),slot.minZ(),slot.maxX()+1,slot.maxY()+1,slot.maxZ()+1));}
    private static void hall(LiteraryTests.Fixture f,LabyrinthPlace hall){var b=f.base(hall);LabyrinthHalls.build(f.in,b,hall);LabyrinthBuilder.registerDoors(f.data(),hall,b);f.built.add(hall);}
    private static LabyrinthPlace where(LiteraryTests.Fixture f,ServerPlayer p){return LabyrinthPlaces.placeAt(f.origin,p.blockPosition());}
    private static void away(LiteraryTests.Fixture f,ServerPlayer p){at(p,f.in,f.base(LabyrinthPlace.STRAIGHT_HALL).offset(0,0,-4));}

    @GameTest(template="empty",batch="coop_follow146",timeoutTicks=2400)
    public static void friendsThroughOneDoorShareTheSceneAndAFriendWhoFinishedItGoesOnAlone(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var lead=player(h,f,"coop_lead");var friend=player(h,f,"coop_friend");var done=player(h,f,"coop_done");
        hold(f,LabyrinthPlace.STRAIGHT_HALL);hold(f,LabyrinthPlace.BENT_HALL);hold(f,SCENE);
        var everyone=List.of(lead,friend,done);boolean[] ticking={false};LabyrinthData.Door[] far={null};
        h.onEachTick(()->{if(ticking[0])for(var p:everyone)HouseTransitionEvents.onPlayerTick(new PlayerTickEvent.Post(p));});
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the halls and the nursery are resident"))
            .thenExecute(()->{
                hall(f,LabyrinthPlace.STRAIGHT_HALL);hall(f,LabyrinthPlace.BENT_HALL);f.build(SCENE);
                var story=WitnessAccount.Story.of(SCENE.id());WitnessAccount.resolve(f.data(),done.getUUID(),story,"heard_the_knocks_answered");
                far[0]=f.data().door(LabyrinthPlace.STRAIGHT_HALL.id()+"/far");var front=far[0].lower.relative(far[0].facing);
                f.data().deal(lead.getUUID(),far[0],SCENE.id(),false);
                f.data().deal(friend.getUUID(),far[0],LabyrinthPlace.BENT_HALL.id(),false);
                f.data().deal(done.getUUID(),far[0],LabyrinthPlace.BENT_HALL.id(),false);
                // The friends hang back while the first goes through alone.
                at(lead,f.in,front);at(friend,f.in,front.south(9));at(done,f.in,front.south(9).east(2));ticking[0]=true;click(lead,far[0].lower);
            })
            .thenWaitUntil(()->h.assertTrue(!LabyrinthDoors.isBusy(lead)&&where(f,lead)==SCENE,"the first through comes into the nursery"))
            .thenExecute(()->{var front=far[0].lower.relative(far[0].facing);at(friend,f.in,front);click(friend,far[0].lower);})
            .thenWaitUntil(()->h.assertTrue(!LabyrinthDoors.isBusy(friend)&&where(f,friend)==SCENE,"a friend through the same door soon after comes into the same nursery, not the hall they had been dealt"))
            .thenExecute(()->{
                var thread=VignetteThreads.threadId(f.data(),SCENE,lead.getUUID());
                h.assertTrue(thread!=null&&thread.equals(VignetteThreads.threadId(f.data(),SCENE,friend.getUUID())),"arriving together, they share one thread");
                var t=VignetteThreads.thread(f.data(),SCENE,lead.getUUID());
                h.assertTrue(!VignetteThreads.partway(t,lead.getUUID())&&!VignetteThreads.partway(t,friend.getUUID())&&t.getInt("Visit")==1,"both are there from its first visit");
                h.assertTrue(f.data().returnDepth(friend.getUUID())==1,"following is an ordinary crossing for the friend");
                var front=far[0].lower.relative(far[0].facing);at(done,f.in,front);click(done,far[0].lower);
            })
            .thenWaitUntil(()->h.assertTrue(!LabyrinthDoors.isBusy(done)&&where(f,done)==LabyrinthPlace.BENT_HALL,"a friend who has finished the nursery goes on through their own door"))
            .thenExecute(()->{
                h.assertTrue(VignetteThreads.threadId(f.data(),SCENE,done.getUUID())==null,"and joins no thread of it");
                for(var p:everyone)h.assertTrue(p.isAlive()&&p.getHealth()==20,"nobody is hurt");
                f.reload();h.assertTrue(VignetteThreads.threadId(f.data(),SCENE,friend.getUUID())!=null,"the thread survives a reload");
            }).thenSucceed();
    }

    @GameTest(template="empty",batch="coop_mixed146",timeoutTicks=1200)
    public static void aMixedGroupAtOneDoorIsDealtAVignetteNoneHasStartedOrSplits(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var lead=player(h,f,"mixed_lead");var done=player(h,f,"mixed_done");var watcher=player(h,f,"mixed_watcher");
        hold(f,LabyrinthPlace.STRAIGHT_HALL);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the hall is resident"))
            .thenExecute(()->{
                hall(f,LabyrinthPlace.STRAIGHT_HALL);var data=f.data();var far=data.door(LabyrinthPlace.STRAIGHT_HALL.id()+"/far");var front=far.lower.relative(far.facing);
                WitnessAccount.resolve(data,done.getUUID(),WitnessAccount.Story.of(SCENE.id()),"heard_the_knocks_answered");
                at(lead,f.in,front);at(done,f.in,front.east(2));at(watcher,f.in,front.west(2));watcher.setGameMode(GameType.SPECTATOR); // a spectator at the door is not part of the group
                data.deal(lead.getUUID(),far,SCENE.id(),false);
                GroupCrossings.forget();GroupCrossings.before(lead,far);
                var dealt=LabyrinthPlace.byId(data.deal(lead.getUUID(),far).place());
                h.assertTrue(dealt!=SCENE&&SharedVignettes.followable(dealt),"a friend at the door has finished the nursery, so the first through is dealt a scene they can share: "+dealt);
                h.assertTrue(LabyrinthDealer.vignettesAvailable(data,lead.getUUID()).contains(dealt)&&LabyrinthDealer.vignettesAvailable(data,done.getUUID()).contains(dealt),"one either could be dealt");
                var story=WitnessAccount.Story.of(dealt.id());
                h.assertTrue(story==null||!WitnessAccount.begun(data,lead.getUUID(),story)&&!WitnessAccount.begun(data,done.getUUID(),story),"and one neither has started");
                // With nothing left that neither has started, they split: the first keeps their own door.
                for(var v:LabyrinthDealer.vignettesAvailable(data,lead.getUUID()))data.visit(done.getUUID(),v);
                data.deal(lead.getUUID(),far,SCENE.id(),false);GroupCrossings.forget();GroupCrossings.before(lead,far);
                h.assertTrue(SCENE.id().equals(data.deal(lead.getUUID(),far).place()),"with no scene neither has started, the group splits and the first keeps the nursery");
                // Friends who can all share it keep what was dealt.
                done.teleportTo(f.in,front.getX()+.5,front.getY(),front.getZ()+20.5,180,0);var fresh=player(h,f,"mixed_fresh");at(fresh,f.in,front.east(1));
                data.deal(lead.getUUID(),far,SCENE.id(),false);GroupCrossings.forget();GroupCrossings.before(lead,far);
                h.assertTrue(SCENE.id().equals(data.deal(lead.getUUID(),far).place()),"a friend who can share the nursery changes nothing");
                // A scene not ready for a second reader is swapped for one the group can share.
                data.deal(lead.getUUID(),far,LabyrinthPlace.GOATMAN.id(),false);GroupCrossings.forget();GroupCrossings.before(lead,far);
                var swapped=LabyrinthPlace.byId(data.deal(lead.getUUID(),far).place());
                h.assertTrue(swapped!=LabyrinthPlace.GOATMAN&&SharedVignettes.followable(swapped),"the Goatman's headcount is one reader's for now, so a group is dealt a shared scene instead: "+swapped);
                h.assertTrue(!SharedVignettes.followable(LabyrinthPlace.BACKSEAT)&&!SharedVignettes.followable(LabyrinthPlace.BORRASCA)&&SharedVignettes.followable(LabyrinthPlace.THREE_FINGERS),"the backseat and the private sites stay one reader's");
            }).thenSucceed();
    }

    /** A drafted scene for this fixture: every way of being there has its own line, and the holder's beat waits for them. */
    private static final SharedVignettes.Scene DRAFT=new SharedVignettes.Scene(){
        @Override public @Nullable String line(VignetteRole role,String outcome,String actor){return switch(role){
            case WITNESS->"I stood in the cold room while "+actor+" knocked. The wall knocked back.";
            case PARTWAY->"I came in late. "+actor+" was already at the wall.";
            case AFTERMATH->"The chalk was still on the wall when I came back.";
            default->null;};}
        @Override public @Nullable UUID waitsFor(LabyrinthData data,CompoundTag thread){return thread.getBoolean("NeedsHolder")?VignetteThreads.holder(thread):null;}
        @Override public void release(MinecraftServer server,CompoundTag thread){thread.putBoolean("Released",true);}
    };
    private static String pages(LabyrinthData data,ServerPlayer p){
        var book=WitnessAccount.book(data,p.getUUID(),p.getGameProfile().getName(),false);var text=new StringBuilder();
        for(var page:book.get(DataComponents.WRITTEN_BOOK_CONTENT).pages())text.append(page.raw().getString()).append('\n');return text.toString();
    }

    @GameTest(template="empty",batch="coop_thread146",timeoutTicks=1200)
    public static void aSharedEndingIsWatchedJoinedPartwayAndReadAfterwardsEachInTheirOwnWords(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;hold(f,SCENE);hold(f,LabyrinthPlace.STRAIGHT_HALL);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the nursery is resident"))
            .thenExecute(()->{
                SharedVignettes.draft(SCENE,DRAFT);var data=f.data();var story=WitnessAccount.Story.of(SCENE.id());
                var actor=player(h,f,"coop_actor");var witness=player(h,f,"coop_witness");var absent=player(h,f,"coop_absent");var late=player(h,f,"coop_late");var spectator=player(h,f,"coop_spectator");
                // The first visit: three readers arrive together.
                for(var p:List.of(actor,witness,absent)){f.arrive(p,SCENE);VignetteThreads.arrived(p,SCENE);}
                var id=VignetteThreads.threadId(data,SCENE,actor.getUUID());
                h.assertTrue(id!=null&&id.equals(VignetteThreads.threadId(data,SCENE,witness.getUUID()))&&id.equals(VignetteThreads.threadId(data,SCENE,absent.getUUID())),"the readers on the first visit share one thread");
                for(var p:List.of(actor,witness,absent))away(f,p);
                // A later visit: two of them come back, and a reader new to the nursery comes in with them.
                f.arrive(actor,SCENE);VignetteThreads.arrived(actor,SCENE);f.arrive(late,SCENE);VignetteThreads.arrived(late,SCENE);f.arrive(witness,SCENE);VignetteThreads.arrived(witness,SCENE);
                var t=VignetteThreads.thread(data,SCENE,actor.getUUID());
                h.assertTrue(t.getInt("Visit")==2&&id.equals(VignetteThreads.threadId(data,SCENE,late.getUUID()))&&VignetteThreads.partway(t,late.getUUID())&&!VignetteThreads.partway(t,witness.getUUID()),"the newcomer joins the thread partway; the others are from its start");
                f.arrive(spectator,SCENE);spectator.setGameMode(GameType.SPECTATOR);VignetteThreads.arrived(spectator,SCENE);
                h.assertTrue(VignetteThreads.threadId(data,SCENE,spectator.getUUID())==null,"a spectator joins no thread");
                // A beat that needs the holder waits for them: the others are not dealt it alone.
                VignetteThreads.hold(data,SCENE,actor);t=VignetteThreads.thread(data,SCENE,actor.getUUID());t.putBoolean("NeedsHolder",true);VignetteThreads.saveThread(data,SCENE,actor.getUUID(),t);
                h.assertTrue(VignetteThreads.waiting(data,witness.getUUID(),SCENE)&&!VignetteThreads.waiting(data,actor.getUUID(),SCENE),"the thread waits for its holder");
                h.assertTrue(!LabyrinthDealer.vignettesAvailable(data,witness.getUUID()).contains(SCENE)&&GroupCrossings.canJoin(data,witness.getUUID(),SCENE,id),"alone it is not dealt to the others, though they can follow the holder in");
                // A holder long gone: what they hold goes back, so the thread is never stuck.
                t=VignetteThreads.thread(data,SCENE,actor.getUUID());t.putLong("HolderSince",f.in.getServer().overworld().getGameTime()-VignetteThreads.RELEASE-1);VignetteThreads.saveThread(data,SCENE,actor.getUUID(),t);
                VignetteThreads.releaseOverdue(f.in.getServer());
                h.assertTrue(VignetteThreads.holder(VignetteThreads.thread(data,SCENE,actor.getUUID()))!=null,"never while the holder is there");
                away(f,actor);VignetteThreads.releaseOverdue(f.in.getServer());t=VignetteThreads.thread(data,SCENE,actor.getUUID());
                h.assertTrue(VignetteThreads.holder(t)==null&&t.getBoolean("Released"),"the scene takes it back once the holder has been gone too long");
                t.remove("NeedsHolder");VignetteThreads.saveThread(data,SCENE,actor.getUUID(),t);f.arrive(actor,SCENE);VignetteThreads.arrived(actor,SCENE);
                // The actor ends it. Those there watched; the newcomer watched from where they came in.
                WitnessAccount.resolve(actor,story,"heard_the_knocks_answered");
                h.assertTrue(WitnessAccount.role(data,actor.getUUID(),story)==VignetteRole.ACTOR,"the one who acted holds an actor's account");
                h.assertTrue(WitnessAccount.has(data,witness.getUUID(),story)&&WitnessAccount.role(data,witness.getUUID(),story)==VignetteRole.WITNESS,"a member there throughout gets their own witness account and the credit");
                h.assertTrue(WitnessAccount.has(data,late.getUUID(),story)&&WitnessAccount.role(data,late.getUUID(),story)==VignetteRole.PARTWAY,"the newcomer's account is a late joiner's");
                h.assertTrue(!WitnessAccount.has(data,absent.getUUID(),story)&&!WitnessAccount.has(data,spectator.getUUID(),story),"neither the absent member nor a spectator is credited for what they did not see");
                var words=pages(data,witness);
                h.assertTrue(words.contains("while coop_actor knocked")&&!words.contains(story.text),"the witness reads their own drafted words, naming who acted, not the actor's account");
                h.assertTrue(pages(data,late).contains("I came in late. coop_actor"),"the late joiner reads theirs");
                h.assertTrue(pages(data,actor).contains(story.text),"the actor's account is unchanged");
                h.assertTrue(!LiteraryVignettes.personal(data,witness.getUUID(),SCENE).getBoolean("Completed")&&!f.own(witness,SCENE).getBoolean("Ready"),"another reader's act is never this reader's own act in the scene");
                h.assertTrue(LabyrinthDealer.finishedFor(data,witness.getUUID(),SCENE),"for the witness the nursery's door is a passage now");
                // The member who was elsewhere comes back to its aftermath.
                f.arrive(absent,SCENE);VignetteThreads.arrived(absent,SCENE);
                h.assertTrue(WitnessAccount.role(data,absent.getUUID(),story)==VignetteRole.AFTERMATH&&pages(data,absent).contains("The chalk was still on the wall"),"the absent member reads the aftermath");
                f.reload();h.assertTrue(WitnessAccount.role(f.data(),witness.getUUID(),story)==VignetteRole.WITNESS&&VignetteThreads.thread(f.data(),SCENE,absent.getUUID()).getBoolean("Ended"),"roles and the ended thread survive a reload");
            }).thenSucceed();
    }

    /** 0.4.148: the drowned town's story is the world's, so everyone in Proofrock when the roof opens watched it. */
    @GameTest(template="empty",batch="coop_town148",timeoutTicks=600)
    public static void everyoneInProofrockWhenTheRoofOpensWatchedItInTheirOwnWords(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var town=LabyrinthPlace.DROWNED_TOWN;var b=f.base(town);var hatch=b.offset(DrownedTown.ROOF_HATCH);
        f.chunks.hold(f.out,new AABB(hatch).inflate(8));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the drowned church is resident")).thenExecute(()->{
            var data=f.data();var story=WitnessAccount.Story.DROWNED_TOWN;
            var actor=player(h,f,"town_actor");var early=player(h,f,"town_early");var late=player(h,f,"town_late");var away=player(h,f,"town_away");var spectator=player(h,f,"town_spectator");
            // The first visit, for the essays: two readers are in Proofrock for it.
            var state=new CompoundTag();state.putInt("Visit",1);data.setState(DrownedTown.ID,state);
            for(var p:List.of(actor,early)){at(p,f.out,hatch.south(2));DrownedTown.noteFirstVisit(data,p);}
            // The second: the essays dried and the church unlocked. A newcomer comes too; one reader is elsewhere.
            state=data.state(DrownedTown.ID);state.putInt("Visit",2);state.putInt("DryMask",7);state.putBoolean("ChurchUnlocked",true);data.setState(DrownedTown.ID,state);
            at(late,f.out,hatch.south(3));DrownedTown.noteFirstVisit(data,late);
            at(spectator,f.out,hatch.east(2));spectator.setGameMode(GameType.SPECTATOR);DrownedTown.noteFirstVisit(data,spectator);
            at(away,f.out,b.offset(0,0,240));
            h.assertTrue(SharedVignettes.drafted(town)!=null&&SharedVignettes.drafted(town).whole(),"the drowned town is drafted, and its story is the world's");
            h.assertTrue(DrownedTown.openRoof(actor,hatch),"the reader beneath the hatch lets the breath go and the roof opens");
            h.assertTrue(WitnessAccount.has(data,actor.getUUID(),story)&&WitnessAccount.role(data,actor.getUUID(),story)==VignetteRole.ACTOR,"the one who opened it holds an actor's account");
            h.assertTrue(WitnessAccount.role(data,early.getUUID(),story)==VignetteRole.WITNESS&&pages(data,early).contains("town_actor let a breath go beneath the roof hatch"),"a reader there for the essays watched it, in their own words, naming who opened it");
            h.assertTrue(WitnessAccount.role(data,late.getUUID(),story)==VignetteRole.PARTWAY&&pages(data,late).contains("I came to Proofrock after the essays had dried"),"the newcomer's account is a late joiner's");
            h.assertTrue(!WitnessAccount.has(data,away.getUUID(),story)&&!WitnessAccount.has(data,spectator.getUUID(),story),"nobody elsewhere, and no spectator, is credited");
            h.assertTrue(pages(data,actor).contains(story.text),"the actor's account is unchanged");
            // Later, the one who was elsewhere examines what the ending left, as before.
            h.assertTrue(WitnessAccount.aftermathTarget(data,town,b,hatch),"the open roof hatch is still the aftermath for anyone who missed it");
        }).thenSucceed();
    }
}
