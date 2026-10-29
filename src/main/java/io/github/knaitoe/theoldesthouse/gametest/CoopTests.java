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
    @AfterBatch(batch="coop_child149") public static void childDone(ServerLevel l){close();}
    @AfterBatch(batch="coop_private151") public static void privateDone(ServerLevel l){SceneInstances.fixtureCap(null);close();}
    @AfterBatch(batch="coop_copies155") public static void harriganCopiesDone(ServerLevel l){SceneInstances.clear();close();}
    @AfterBatch(batch="coop_copies153") public static void copiesDone(ServerLevel l){SceneInstances.clear();close();}
    @AfterBatch(batch="coop_copies154") public static void novelCopiesDone(ServerLevel l){SceneInstances.clear();close();}
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
                // 0.4.150: the Goatman's readers take cousins' places, so a group at its door goes in together.
                data.deal(lead.getUUID(),far,LabyrinthPlace.GOATMAN.id(),false);GroupCrossings.forget();GroupCrossings.before(lead,far);
                h.assertTrue(LabyrinthPlace.GOATMAN.id().equals(data.deal(lead.getUUID(),far).place()),"a group at the Goatman's door keeps it");
                // A scene not ready for a second reader is swapped for one the group can share.
                data.deal(lead.getUUID(),far,LabyrinthPlace.HIDE_AND_CLAP.id(),false);GroupCrossings.forget();GroupCrossings.before(lead,far);
                var swapped=LabyrinthPlace.byId(data.deal(lead.getUUID(),far).place());
                h.assertTrue(swapped!=LabyrinthPlace.HIDE_AND_CLAP&&SharedVignettes.followable(swapped),"the clapping game has one blindfold, so a group is dealt a shared scene instead: "+swapped);
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

    /** 0.4.149: shut in the child's room together, each reader looks and crawls for themselves, and each account says who was there. */
    @GameTest(template="empty",batch="coop_child149",timeoutTicks=700)
    public static void readersShutInTheChildsRoomTogetherCrawlForThemselvesAndEachAccountSaysWhoWasThere(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var place=LabyrinthPlace.CHILD_ROOM;var story=WitnessAccount.Story.CHILD_ROOM;var b=f.base(place);var door=b.offset(0,1,1);
        var first=f.player("child_first",place);var second=f.player("child_second",place);var spectator=f.player("child_watching",place);spectator.setGameMode(GameType.SPECTATOR);
        var late=new ServerPlayer[1];
        h.runAfterDelay(10,()->{
            h.assertTrue(SharedVignettes.followable(place)&&SharedVignettes.drafted(place)==null,"friends go into the child's room together, and nobody is credited for another's crawl");
            f.source(first,place);first.closeContainer();
            for(var p:List.of(first,second,spectator)){f.at(p,place,p==second?-.5:.5,0,-4.5);f.look(p,door);}
        });
        // The room closes around the two of them, unseen, while they face the door they came in by.
        h.runAfterDelay(25,()->{
            var world=LiteraryVignettes.shared(f.data(),place);
            h.assertTrue(world.getBoolean("ClockStarted0121")&&first.getUUID().equals(world.getUUID("CardBy0149")),"the room began to close when the first reader opened the card");
            for(int i=0;i<ChildRoom.EXITS.size();i++)h.assertTrue(ChildRoom.seal(f.in,b,i),"an exit goes while nobody looks at it: "+i);
            world=LiteraryVignettes.shared(f.data(),place);world.putInt("Exits0474",ChildRoom.ALL);LiteraryVignettes.shared(f.data(),place,world);
            // A third reader comes in after every exit has gone.
            late[0]=f.player("child_late",place);f.at(late[0],place,1.5,0,-4.5);f.look(late[0],door);
        });
        h.runAfterDelay(35,()->{
            h.assertTrue(f.own(first,place).getInt("ExitsAtArrival0474")==0&&f.own(second,place).getInt("ExitsAtArrival0474")==0&&f.own(late[0],place).getInt("ExitsAtArrival0474")==ChildRoom.ALL,"two were there from the start; the third came into a shut room");
            for(var p:List.of(second,late[0])){f.source(p,place);p.closeContainer();}
        });
        // Each looks at every sealed exit for themselves, from where a child would stand.
        double[][] stands={{-6.5,0,-18.5},{6.5,0,-18.5},{-6.5,0,-11.5},{7.5,0,-7.5}};
        for(int i=0;i<ChildRoom.EXITS.size();i++){final int exit=i;h.runAfterDelay(45+i*30,()->{var s=stands[exit];var cells=ChildRoom.EXITS.get(exit);
            for(var p:List.of(first,second,late[0])){f.at(p,place,s[0],s[1],s[2]);f.look(p,b.offset(cells.get(cells.size()-1)));}});}
        // Each crawls in turn while the others wait upstairs.
        int[] start={180,230,280};
        for(int k=0;k<3;k++){final int who=k;
            h.runAfterDelay(start[k]-5,()->{var all=List.of(first,second,late[0]);
                for(var p:all)if(!f.own(p,place).getBoolean("Ready")){f.at(p,place,.5,0,-6.5);f.look(p,door);}
                h.assertTrue(f.own(all.get(who),place).getInt("ExitSeen0474")==ChildRoom.ALL,"each reader has looked at every sealed exit themselves: "+all.get(who).getGameProfile().getName());
                f.at(all.get(who),place,.5,-2,-22.5);});
            h.runAfterDelay(start[k]+7,()->f.at(List.of(first,second,late[0]).get(who),place,6.5,-2,-18.5));
            h.runAfterDelay(start[k]+19,()->f.at(List.of(first,second,late[0]).get(who),place,6.5,-4,-12.5));
            h.runAfterDelay(start[k]+35,()->{var all=List.of(first,second,late[0]);var p=all.get(who);
                h.assertTrue(f.own(p,place).getBoolean("Ready"),"their own crawl readies their own account: "+p.getGameProfile().getName());
                f.read(p,place,LiteraryRooms.ending(place));
                h.assertTrue(WitnessAccount.has(f.data(),p.getUUID(),story)&&WitnessAccount.role(f.data(),p.getUUID(),story)==VignetteRole.ACTOR,"each resolves the room as the one who did it");
                for(int j=who+1;j<3;j++)h.assertTrue(!WitnessAccount.has(f.data(),all.get(j).getUUID(),story),"a reader still upstairs is not credited for another's crawl");
                f.at(p,place,6.5,-4,-8.5);});
        }
        h.runAfterDelay(330,()->{var data=f.data();
            h.assertTrue(ChildRoom.account(f.own(first,place)).equals("It began when I opened the bedtime card. The windows went first. Then the doors. child_second and child_late were in the room with me, and none of us saw one go. I crawled beneath the bed and found a strip of floor that still led somewhere. I stood up only after I had left it. Above me, they were still in the room."),
                    "the first opened the card, went under first and left the others above: "+ChildRoom.account(f.own(first,place)));
            h.assertTrue(ChildRoom.account(f.own(second,place)).equals("It began when child_first opened the bedtime card. The windows went first. Then the doors. child_first and child_late were in the room with me, and none of us saw one go. child_first had gone beneath the bed before me. I crawled along the clean line their body had left in the dust, on a strip of floor that still led somewhere. I stood up only after I had left it. Above me, child_late was still in the room."),
                    "the second followed the first down: "+ChildRoom.account(f.own(second,place)));
            var lateWords=ChildRoom.account(f.own(late[0],place));
            h.assertTrue(lateWords.startsWith("When I came in, every way out of the room was already wall.")&&lateWords.contains("child_first")&&lateWords.contains("child_second")&&lateWords.contains("They had gone beneath the bed before me. I crawled along the clean lines their bodies had left")&&!lateWords.contains("Above me"),"the late reader came into a shut room and followed both down: "+lateWords);
            h.assertTrue(pages(data,first).contains("I was in the child's room with child_second and child_late when it closed. I crawled below the bed after the windows and doors had gone."),"the first reader's Witness line names who was shut in with them");
            h.assertTrue(pages(data,second).contains("I was in the child's room with child_first and child_late when it closed. I followed child_first below the bed."),"the second's says whom they followed");
            h.assertTrue(pages(data,late[0]).contains("I came into the child's room after its windows and doors had gone. I followed "),"the late reader's says they came in after");
            h.assertTrue(!WitnessAccount.has(data,spectator.getUUID(),story)&&!f.own(spectator,place).getBoolean("Ready"),"a spectator is in nobody's account and earns nothing");
            for(var p:List.of(first,second,late[0]))h.assertTrue(!ChildRoom.account(f.own(p,place)).contains("child_watching"),"nor is the spectator named");
            // A reader who finished alone before 0.4.149 keeps the account and line they had.
            h.assertTrue(ChildRoom.account(new CompoundTag()).equals("The windows went first. Then the doors. I crawled beneath the bed and found a strip of floor that still led somewhere. I stood up only after I had left it.")&&ChildRoom.witness(new CompoundTag(),story.text).equals(story.text),"older accounts keep their words");
            h.succeed();
        });
    }

    @GameTest(template="empty",batch="coop_private151",timeoutTicks=4000)
    public static void aPrivateSceneIsOneReadersAtATimeAndWaitsForTheNext(GameTestHelper h){
        // 0.4.153: private scenes have copies; with none allowed (sceneCopies 0) they are one reader's at a time, as in 0.4.151.
        SceneInstances.fixtureCap(0);
        var f=new LiteraryTests.Fixture(h);active=f;var inside=player(h,f,"private_inside");var next=player(h,f,"private_next");var watcher=player(h,f,"private_watcher");
        var halls=List.of(LabyrinthPlace.GRAY_CORRIDOR,LabyrinthPlace.STRAIGHT_HALL,LabyrinthPlace.BENT_HALL,LabyrinthPlace.CROSS_HALL,LabyrinthPlace.ALCOVE_HALL,LabyrinthPlace.OFFSET_HALL,LabyrinthPlace.SERVICE_LANDING,LabyrinthPlace.QUIET_ROOM);
        var junction=f.base(LabyrinthPlace.JUNCTION);f.chunks.hold(f.in,new AABB(junction).inflate(8,4,16));
        for(var hall:halls)hold(f,hall);hold(f,LabyrinthPlace.HARRIGAN);
        var scenes=List.of(LabyrinthPlace.HARRIGAN,LabyrinthPlace.CONFESSION,LabyrinthPlace.HOLY_RABBIT,LabyrinthPlace.WHEEL,LabyrinthPlace.WHALE,LabyrinthPlace.PHONE_CANOE,LabyrinthPlace.SECOND_WAITRESS,LabyrinthPlace.MISSING_SLEEP);
        boolean[] ticking={false};LabyrinthData.Door[] west={null};
        h.onEachTick(()->{if(ticking[0])HouseTransitionEvents.onPlayerTick(new PlayerTickEvent.Post(next));});
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the junction, its halls and Harrigan's study are resident"))
            .thenExecute(()->{
                for(var v:scenes)h.assertTrue(SharedVignettes.privateScene(v)&&!SharedVignettes.followable(v),"friends do not follow each other into "+v.id());
                h.assertTrue(SharedVignettes.followable(LabyrinthPlace.LAKE_HOUSE)&&SharedVignettes.followable(LabyrinthPlace.GHOSTS_SET),"the lake house and the film set stay shared");
                LabyrinthBuilder.buildJunction(f.in,junction);LabyrinthBuilder.registerDoors(f.data(),LabyrinthPlace.JUNCTION,junction);f.built.add(LabyrinthPlace.JUNCTION);
                for(var hall:halls){var b=f.base(hall);if(hall==LabyrinthPlace.GRAY_CORRIDOR)LabyrinthBuilder.buildCorridor(f.in,b);else LabyrinthHalls.build(f.in,b,hall);LabyrinthBuilder.registerDoors(f.data(),hall,b);f.built.add(hall);}
                var study=f.base(LabyrinthPlace.HARRIGAN);
                at(inside,f.in,study.offset(0,0,-10));at(watcher,f.in,study.offset(2,0,-10));watcher.setGameMode(GameType.SPECTATOR);
                h.assertTrue(SharedVignettes.occupant(f.in.getServer(),LabyrinthPlace.HARRIGAN,next.getUUID())==inside,"a reader in Harrigan's study has it");
                h.assertTrue(SharedVignettes.occupant(f.in.getServer(),LabyrinthPlace.HARRIGAN,inside.getUUID())==null,"a spectator watching does not take it from them");
                h.assertTrue(SharedVignettes.occupant(f.in.getServer(),LabyrinthPlace.HILL_NURSERY,next.getUUID())==null,"a shared scene is nobody's alone");
                west[0]=f.data().door(LabyrinthPlace.JUNCTION.id()+"/west");f.data().deal(next.getUUID(),west[0],LabyrinthPlace.HARRIGAN.id(),false);
                at(next,f.in,west[0].lower.relative(west[0].facing));ticking[0]=true;click(next,west[0].lower);
            })
            .thenWaitUntil(()->h.assertTrue(!LabyrinthDoors.isBusy(next)&&halls.contains(where(f,next)),"the next reader's door into the study opens onto an ordinary hall while someone else is in it"))
            .thenExecute(()->{
                var deal=f.data().deal(next.getUUID(),west[0]);
                h.assertTrue(deal!=null&&halls.contains(LabyrinthPlace.byId(deal.place())),"the door is re-dealt for them to the hall they reached");
                h.assertTrue(!f.data().visited(next.getUUID()).contains(LabyrinthPlace.HARRIGAN.id())&&WitnessAccount.Story.of(LabyrinthPlace.HARRIGAN.id())!=null&&!WitnessAccount.begun(f.data(),next.getUUID(),WitnessAccount.Story.of(LabyrinthPlace.HARRIGAN.id())),"the study waits for them: they have not been in it");
                h.assertTrue(where(f,inside)==LabyrinthPlace.HARRIGAN&&inside.isAlive()&&inside.getHealth()==20,"the reader inside is left alone");
                away(f,inside);h.assertTrue(SharedVignettes.occupant(f.in.getServer(),LabyrinthPlace.HARRIGAN,next.getUUID())==null,"once they leave, the study is free");
            }).thenSucceed();
    }

    @GameTest(template="empty",batch="coop_copies153",timeoutTicks=6000)
    public static void aPrivateSceneTakenByAnotherOpensACopyThatItsReaderKeeps(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var first=player(h,f,"copy_first");var second=player(h,f,"copy_second");var watcher=player(h,f,"copy_watcher");
        var scene=LabyrinthPlace.CONFESSION;var server=f.in.getServer();
        var halls=List.of(LabyrinthPlace.GRAY_CORRIDOR,LabyrinthPlace.STRAIGHT_HALL,LabyrinthPlace.BENT_HALL,LabyrinthPlace.CROSS_HALL,LabyrinthPlace.ALCOVE_HALL,LabyrinthPlace.OFFSET_HALL,LabyrinthPlace.SERVICE_LANDING,LabyrinthPlace.QUIET_ROOM);
        var junction=f.base(LabyrinthPlace.JUNCTION);f.chunks.hold(f.in,new AABB(junction).inflate(8,4,16));
        for(var hall:halls)hold(f,hall);hold(f,scene);
        var readers=new ArrayList<>(List.of(first,second));boolean[] ticking={false};LabyrinthData.Door[] west={null};ServerPlayer[] third={null};
        h.onEachTick(()->{if(ticking[0])for(var p:readers)HouseTransitionEvents.onPlayerTick(new PlayerTickEvent.Post(p));});
        java.util.function.Consumer<ServerPlayer> knock=p->{var front=west[0].lower.relative(west[0].facing);f.data().deal(p.getUUID(),west[0],scene.id(),false);VignetteGate.redealt(f.data(),p.getUUID(),west[0]);at(p,f.in,front);click(p,west[0].lower);};
        java.util.function.ToIntFunction<ServerPlayer> copy=p->SceneInstances.at(server,scene,p.level().dimension(),p.position());
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the junction, its halls and the confession room are resident"))
            .thenExecute(()->{
                LabyrinthBuilder.buildJunction(f.in,junction);LabyrinthBuilder.registerDoors(f.data(),LabyrinthPlace.JUNCTION,junction);f.built.add(LabyrinthPlace.JUNCTION);
                for(var hall:halls){var b=f.base(hall);if(hall==LabyrinthPlace.GRAY_CORRIDOR)LabyrinthBuilder.buildCorridor(f.in,b);else LabyrinthHalls.build(f.in,b,hall);LabyrinthBuilder.registerDoors(f.data(),hall,b);f.built.add(hall);}
                f.build(scene);at(watcher,f.in,f.base(scene).offset(2,0,-10));watcher.setGameMode(GameType.SPECTATOR);
                h.assertTrue(SceneInstances.copyable(scene)&&SceneInstances.cap()==8,"the confession has copies, eight at most by default");
                west[0]=f.data().door(LabyrinthPlace.JUNCTION.id()+"/west");ticking[0]=true;knock.accept(first);
            })
            .thenWaitUntil(()->h.assertTrue(!LabyrinthDoors.isBusy(first)&&copy.applyAsInt(first)==0,"the first reader comes into the original, though a spectator stands in it"))
            .thenExecute(()->{
                h.assertTrue(SceneInstances.kept(f.data(),first.getUUID(),scene)==0,"they keep the original");
                h.assertTrue(SceneInstances.copies(f.data(),scene)==1,"with another reader in the House, the next copy is made ready ahead");
                knock.accept(second);
            })
            .thenExecute(()->h.assertTrue(where(f,second)==LabyrinthPlace.JUNCTION&&SceneInstances.kept(f.data(),second.getUUID(),scene)<0,"while the copy is still being built, the second reader's door stays shut"))
            .thenWaitUntil(()->h.assertTrue(SceneInstances.built(f.data(),scene,1)&&!SceneInstances.building(),"the copy stands ready"))
            .thenExecute(()->knock.accept(second))
            .thenWaitUntil(()->h.assertTrue(!LabyrinthDoors.isBusy(second)&&copy.applyAsInt(second)==1,"the second reader comes into a copy of their own"))
            .thenExecute(()->{
                var original=f.base(scene);var mine=SceneInstances.base(server,scene,1);
                h.assertTrue(mine.getX()<=original.getX()-SceneInstances.WEST&&mine.getY()==original.getY()&&mine.getZ()==original.getZ(),"the copy stands a million blocks west of the original");
                h.assertTrue(where(f,second)==scene&&LiteraryVignettes.current(second)==scene,"the House knows the copy for the scene it is");
                h.assertTrue(mine.equals(LiteraryVignettes.base(second,scene))&&original.equals(LiteraryVignettes.base(first,scene)),"each reader's scene is where they are");
                h.assertTrue(SceneInstances.kept(f.data(),second.getUUID(),scene)==1,"they keep their copy");
                var onward=f.data().door(SceneInstances.doorId(scene,1,OnwardDoors.name(scene)));
                h.assertTrue(f.data().door(SceneInstances.doorId(scene,1,"entry"))!=null&&onward!=null&&OnwardDoors.placeOf(onward)==scene&&!onward.lower.equals(f.data().door(OnwardDoors.id(scene)).lower),"the copy has its own entry and its own way on");
                h.assertTrue(f.in.getBlockState(original.offset(0,0,1)).getBlock()==f.in.getBlockState(mine.offset(0,0,1)).getBlock()&&f.in.getBlockState(onward.lower).getBlock() instanceof net.minecraft.world.level.block.DoorBlock,"it is built like the original, doors and all");
            })
            .thenIdle(20)
            .thenExecute(()->{
                h.assertTrue(second.serverLevel()==f.in&&copy.applyAsInt(second)==1,"a reader in a copy is inside the House, not thrown out through a wall");
                // From inside, the entry only opens; walking back out through it is what takes a reader back.
                var entry=f.data().door(SceneInstances.doorId(scene,1,"entry"));at(second,f.in,entry.lower.relative(entry.facing.getOpposite(),2));click(second,entry.lower);
            })
            .thenIdle(2)
            .thenExecute(()->{var entry=f.data().door(SceneInstances.doorId(scene,1,"entry"));h.assertTrue(f.in.getBlockState(entry.lower).getValue(net.minecraft.world.level.block.DoorBlock.OPEN),"the copy's entry opens from inside");at(second,f.in,entry.lower.relative(entry.facing,2));})
            .thenWaitUntil(()->h.assertTrue(!LabyrinthDoors.isBusy(second)&&where(f,second)==LabyrinthPlace.JUNCTION,"the copy's own entry takes them back to the hall they came from"))
            .thenExecute(()->{away(f,first);knock.accept(second);})
            .thenWaitUntil(()->h.assertTrue(!LabyrinthDoors.isBusy(second)&&copy.applyAsInt(second)==1,"they come back to the copy they keep, though the original stands empty"))
            .thenExecute(()->{
                // Once the first reader has finished the story, the original is anyone's.
                away(f,second);WitnessAccount.resolve(f.data(),first.getUUID(),WitnessAccount.Story.of(scene.id()),"confessed");
                third[0]=player(h,f,"copy_third");readers.add(third[0]);knock.accept(third[0]);
            })
            .thenWaitUntil(()->h.assertTrue(!LabyrinthDoors.isBusy(third[0])&&copy.applyAsInt(third[0])==0,"a new reader takes the original its finished reader no longer keeps"))
            .thenExecute(()->{
                h.assertTrue(SceneInstances.kept(f.data(),third[0].getUUID(),scene)==0&&SceneInstances.kept(f.data(),watcher.getUUID(),scene)<0,"the new reader keeps it; the spectator keeps nothing");
                for(var p:readers)h.assertTrue(p.isAlive()&&p.getHealth()==20,"nobody is hurt");
            }).thenSucceed();
    }

    @GameTest(template="empty",batch="coop_copies154",timeoutTicks=6000)
    public static void theWhaleInstituteAndThePhoneCanoeHaveCopiesOfTheirOwn(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var server=f.in.getServer();
        var writer=player(h,f,"copy_writer");var nextWriter=player(h,f,"copy_next_writer");var filmer=player(h,f,"copy_filmer");var nextFilmer=player(h,f,"copy_next_filmer");
        var whale=LabyrinthPlace.WHALE;var lake=LabyrinthPlace.PHONE_CANOE;hold(f,whale);hold(f,lake);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the institute and the lake are resident"))
            .thenExecute(()->{
                NovelRooms.build(server,f.level(whale),f.base(whale),whale);LabyrinthBuilder.registerDoors(f.data(),whale,f.base(whale));f.built.add(whale);
                PhoneCanoe.build(server,f.level(lake),f.base(lake));LabyrinthBuilder.registerDoors(f.data(),lake,f.base(lake));f.built.add(lake);
                h.assertTrue(SceneInstances.copyable(whale)&&SceneInstances.copyable(lake),"both scenes have copies now");
                at(writer,f.level(whale),f.base(whale).offset(0,0,-6));at(filmer,f.level(lake),f.base(lake).offset(0,0,-5));
                var w=SceneInstances.enter(nextWriter,whale);var c=SceneInstances.enter(nextFilmer,lake);
                h.assertTrue(w.door()==null&&w.waiting()&&c.door()==null&&c.waiting(),"with each original taken, a copy of each is made ready");
            })
            .thenWaitUntil(()->h.assertTrue(SceneInstances.built(f.data(),whale,1)&&SceneInstances.built(f.data(),lake,1),"a copy of each stands ready"))
            .thenExecute(()->{
                var w=SceneInstances.enter(nextWriter,whale);var institute=SceneInstances.base(server,whale,1);
                h.assertTrue(w.door()!=null&&w.copy()==1&&w.door().id.equals(SceneInstances.doorId(whale,1,"entry")),"the next writer's door opens onto their copy of the institute");
                at(nextWriter,f.level(whale),institute.offset(0,0,-6));
                h.assertTrue(NovelVignettes.current(nextWriter)==whale&&institute.equals(NovelVignettes.base(nextWriter,whale))&&f.base(whale).equals(NovelVignettes.base(writer,whale)),"each writer's institute is where they are");
                NovelVignettes.onArrive(nextWriter,whale);
                h.assertTrue(f.level(whale).getBlockState(institute.offset(0,0,1)).getBlock()==f.level(whale).getBlockState(f.base(whale).offset(0,0,1)).getBlock(),"the copy is built like the original");
                var c=SceneInstances.enter(nextFilmer,lake);var shore=SceneInstances.base(server,lake,1);
                h.assertTrue(c.door()!=null&&c.copy()==1,"the next filmer's door opens onto their copy of the lake");
                at(nextFilmer,f.level(lake),shore.offset(0,0,-5));
                h.assertTrue(PhoneCanoe.inside(nextFilmer)&&shore.equals(PhoneCanoe.base(nextFilmer))&&PhoneCanoe.inside(filmer)&&f.base(lake).equals(PhoneCanoe.base(filmer)),"each filmer's lake is where they are");
                h.assertTrue(SceneInstances.copyAt(server,lake,shore)==1&&SceneInstances.copyAt(server,lake,f.base(lake))==0,"a copy is known by where it stands");
                var theirs=PhoneCanoe.stage(f.level(lake),shore);var original=PhoneCanoe.stage(f.level(lake),f.base(lake));
                h.assertTrue(theirs!=null&&original!=null&&!theirs.getUUID().equals(original.getUUID())&&IndianLakeRooms.bounds(shore,lake).contains(theirs.position()),"each lake has its own canoe");
                h.assertTrue(PhoneCanoe.stage(f.level(lake),shore)==theirs,"and a copy's canoe is the same canoe the next time it is looked for");
            }).thenSucceed();
    }

    @GameTest(template="empty",batch="coop_copies155",timeoutTicks=6000)
    public static void harrigansRoomKeepsItsOwnStoryInEachCopy(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var server=f.in.getServer();var place=LabyrinthPlace.HARRIGAN;
        var first=player(h,f,"harrigan_first");var second=player(h,f,"harrigan_second");var third=player(h,f,"harrigan_third");hold(f,place);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"his study is resident"))
            .thenExecute(()->{
                HarriganVignette.build(server,f.in,f.base(place));LabyrinthBuilder.registerDoors(f.data(),place,f.base(place));f.built.add(place);
                at(first,f.in,f.base(place).offset(0,0,-4));h.assertTrue(SceneInstances.enter(first,place).copy()==0,"the first reader takes his study");
                HarriganVignette.onArrive(first,place);h.assertTrue(f.data().state(HarriganVignette.ID).getInt("Visit")==1,"their first visit is the world's, as before");
                var waiting=SceneInstances.enter(second,place);h.assertTrue(waiting.door()==null&&waiting.waiting(),"a second reader waits for a copy");
            })
            .thenWaitUntil(()->h.assertTrue(SceneInstances.built(f.data(),place,1),"a copy of his study stands ready"))
            // A reader arriving keeps the copy loaded; these fixture readers do not load chunks themselves.
            .thenExecute(()->f.chunks.hold(f.in,IndianLakeRooms.bounds(SceneInstances.base(server,place,1),place)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the copy is resident"))
            .thenExecute(()->{
                var chosen=SceneInstances.enter(second,place);var room=SceneInstances.base(server,place,1);
                h.assertTrue(chosen.door()!=null&&chosen.copy()==1,"the second reader's door opens onto the copy");
                at(second,f.in,room.offset(0,0,-4));HarriganVignette.onArrive(second,place);
                var copy=f.data().state(HarriganVignette.ID+"@1");
                h.assertTrue(copy.getInt("Visit")==1&&copy.hasUUID("Harrigan0505"),"the copy has its own first visit and its own Mr. Harrigan");
                h.assertTrue(!copy.getUUID("Harrigan0505").equals(f.data().state(HarriganVignette.ID).getUUID("Harrigan0505")),"he is not the original's body");
                var him=HarriganVignette.harrigan(f.in,room,-1);h.assertTrue(him!=null&&room.distSqr(him.blockPosition())<400,"and he sits in the copy");
                // The first reader's story goes on in the original; the copy's is untouched.
                var world=f.data().state(HarriganVignette.ID);world.putBoolean("BeatDone",true);f.data().setState(HarriganVignette.ID,world);
                HarriganVignette.onArrive(first,place);
                h.assertTrue(f.data().state(HarriganVignette.ID).getInt("Visit")==2&&f.data().state(HarriganVignette.ID+"@1").getInt("Visit")==1,"each room counts its own visits");
                // The first reader's story ends: the original keeps it, and a new reader is never given that room.
                f.data().setCompleted(HarriganVignette.ID,true);WitnessAccount.resolve(f.data(),first.getUUID(),WitnessAccount.Story.HARRIGAN,"buried_phone");
                var third0=SceneInstances.enter(third,place);h.assertTrue(third0.copy()!=0,"a finished original stays the world's story: a new reader is given a copy instead");
                // The second reader finishes; the next reader to take their copy starts its story again.
                var mine=f.data().state(HarriganVignette.ID+"@1");mine.putInt("Visit",3);mine.putBoolean("TicketVisible",true);mine.putBoolean("PropTaken0490_harrigan_phone",true);f.data().setState(HarriganVignette.ID+"@1",mine);
                // Their part is over; they look on as a spectator, who holds no room.
                WitnessAccount.resolve(f.data(),second.getUUID(),WitnessAccount.Story.HARRIGAN,"kept_phone");second.setGameMode(GameType.SPECTATOR);
                SceneInstances.fixtureCap(1);
            })
            .thenExecute(()->{
                var reuse=SceneInstances.enter(third,place);
                h.assertTrue(reuse.copy()==1&&reuse.door()!=null,"the copy whose reader has finished is the new reader's: "+reuse);
                var fresh=f.data().state(HarriganVignette.ID+"@1");
                h.assertTrue(fresh.getInt("Visit")==0&&!fresh.getBoolean("TicketVisible")&&!fresh.getBoolean("PropTaken0490_harrigan_phone")&&fresh.hasUUID("Harrigan0505"),"its story starts again, with the same Mr. Harrigan");
                h.assertTrue(f.data().isCompleted(HarriganVignette.ID)&&f.data().state(HarriganVignette.ID).getInt("Visit")==2,"the original's story is untouched by the copy's");
                SceneInstances.fixtureCap(null);
            }).thenSucceed();
    }
}
