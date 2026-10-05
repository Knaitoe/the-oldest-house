package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import io.github.knaitoe.theoldesthouse.opening.CompanionOrders;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID+"_multiplayer")
@PrefixGameTestTemplate(false)
public final class MultiplayerStoryTests {
    @GameTest(template="empty",batch="multiplayer_cave_return",timeoutTicks=1200)
    public static void NativeCaveCreditWaitsForTheActualReturnAndSurvivesACanceledAttempt(GameTestHelper h){IndianLakeLinkedTests.nativeQuietSoundsOccupiedVisitsRoofAndCanoePreserveTheSequence(h);}
    @AfterBatch(batch="multiplayer_cave_return") public static void caveCleanup(ServerLevel level){IndianLakeLinkedTests.cleanCave(level);}
    private static void run(GameTestHelper h,int at,java.util.function.Consumer<StaircaseAccessTests.Fixture> test) { StaircaseAccessTests.run(h,at,test); }
    private static ItemStack issued(ServerPlayer p) {
        if(!StaircaseFire.take(p))throw new IllegalStateException("original was not issued");
        for(int i=0;i<p.getInventory().getContainerSize();i++) {
            var book=p.getInventory().getItem(i);
            if(StaircaseFire.leaves(book,p.getUUID())>0){p.getInventory().setItem(i,ItemStack.EMPTY);return book;}
        }
        throw new IllegalStateException("issued original missing from actual inventory");
    }
    private static List<net.minecraft.server.network.Filterable<Component>> pages(ItemStack book) { return book.get(DataComponents.WRITTEN_BOOK_CONTENT).pages(); }
    private static String text(ItemStack book) { return pages(book).stream().map(p->p.raw().getString()).collect(java.util.stream.Collectors.joining("\n")); }
    private static void ready(ServerPlayer p,StaircaseAccessTests.Fixture f) {
        f.enter(p);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.FLINT_AND_STEEL));
    }
    private static boolean burn(ServerPlayer p,StaircaseAccessTests.Fixture f,int i) {
        var at=StaircaseFire.braziers(f.origin).get(i);p.teleportTo(f.level,at.getX()+1.5,at.getY(),at.getZ()+.5,0,0);
        return StaircaseFire.ignite(p,f.origin,at);
    }
    private static void serverTick(StaircaseAccessTests.Fixture f) { LabyrinthDoors.onServerTick(new ServerTickEvent.Post(()->true,f.level.getServer())); }

    @GameTest(template="empty",batch="multiplayer_story",timeoutTicks=1200)
    public static void twoNativeExplorersGetTheirOwnRecordedLivesAndImmutableSavedPages(GameTestHelper h) { run(h,520000,f->{
        var p=f.player(h,"story_road");var peer=f.player(h,"story_care");
        p.awardStat(Stats.WALK_ONE_CM,123400);p.awardStat(Stats.DEATHS,2);p.awardStat(Stats.ITEM_CRAFTED.get(Items.BREAD),7);
        var facts=new CompoundTag();facts.putInt("Care",1);facts.putString("CaredName","Juniper");HouseExperience.save(f.data(),peer.getUUID(),facts);
        peer.awardStat(Stats.DEATHS,1);peer.awardStat(Stats.SLEEP_IN_BED,1);
        var letter=new CompoundTag();letter.putString("SafeRetreat",LabyrinthPlace.TED_CAVER.id());f.data().setStateEntry(HouseCorrespondence.ID,p.getUUID().toString(),letter);
        var first=issued(p);var second=issued(peer);
        h.assertTrue(pages(first).size()==5&&pages(second).size()==5,"the native originals have five actual written pages");
        h.assertTrue(text(first).contains("1234 metres")&&text(first).contains("2 times")&&text(first).contains("7 loaves")&&text(first).contains("caver"),"one player's native stats and confirmed personal retreat form their story");
        h.assertTrue(text(second).contains("Juniper")&&!text(first).contains("Juniper")&&!text(second).contains("1234 metres"),"a shared shelf never borrows the peer's biography");
        h.assertTrue(text(second).contains("1 time;")&&text(second).contains("1 time."),"one recorded death and bed rest use singular narrative wording");
        var before=first.copy();p.awardStat(Stats.DEATHS,1);f.reload();
        h.assertTrue(ItemStack.isSameItemSameComponents(before,first)&&f.data().stateEntry(StaircaseStory.STATE,p.getUUID().toString()).getList("Pages",8).size()==5,"issued pages and their saved snapshot remain unchanged as play continues");
    }); }

    @GameTest(template="empty",batch="multiplayer_story",timeoutTicks=1200)
    public static void eachOfTwoPlayersBurnsFiveOriginalPagesAtTheSameActualHearths(GameTestHelper h) { run(h,520500,f->{
        StaircaseFire.dress(f.level,f.origin);var p=f.player(h,"burn_first");var peer=f.player(h,"burn_peer");ready(p,f);ready(peer,f);
        var first=issued(p);var second=issued(peer);p.setItemInHand(InteractionHand.OFF_HAND,first);peer.setItemInHand(InteractionHand.OFF_HAND,second);
        first.set(DataComponents.CUSTOM_NAME,Component.literal("My kept account"));var extra=first.get(DataComponents.CUSTOM_DATA).copyTag();extra.putString("Kept","unchanged");first.set(DataComponents.CUSTOM_DATA,CustomData.of(extra));
        p.awardStat(Stats.DEATHS,3);int stats=p.getStats().getValue(Stats.CUSTOM.get(Stats.DEATHS));var originalPages=List.copyOf(pages(first));
        for(int i=0;i<5;i++){
            h.assertTrue(burn(p,f,i),"first actual reader burns chapter "+i);
            h.assertTrue(StaircaseFire.flames(FinaleProgress.player(peer.server,peer.getUUID()))==i,"shared firelight never advances a peer");
            h.assertTrue(burn(peer,f,i),"the peer feeds their own next leaf even though the native hearth is already lit");
            if(i<4)h.assertTrue(pages(first).equals(originalPages.subList(i+1,5))&&first.get(DataComponents.CUSTOM_NAME).getString().equals("My kept account")&&first.get(DataComponents.CUSTOM_DATA).copyTag().getString("Kept").equals("unchanged"),"burning removes only the next actual page and preserves surviving pages and original components");
        }
        h.assertTrue(first.isEmpty()&&second.isEmpty()&&p.getStats().getValue(Stats.CUSTOM.get(Stats.DEATHS))==stats,"the last page consumes each finite book; remembered facts remain in Minecraft stats");
        f.reload();h.assertTrue(StaircaseFire.open(FinaleProgress.player(p.server,p.getUUID()))&&StaircaseFire.open(FinaleProgress.player(peer.server,peer.getUUID()))&&!StaircaseFire.take(p),"both personal descents survive native saved-data reload without another supply");
    }); }

    @GameTest(template="empty",batch="multiplayer_story",timeoutTicks=1200)
    public static void BorrowedCopiedAndReplayedLeavesCannotBuyPersonalFireCredit(GameTestHelper h) { run(h,521000,f->{
        StaircaseFire.dress(f.level,f.origin);var p=f.player(h,"leaf_owner");var peer=f.player(h,"leaf_borrower");ready(p,f);ready(peer,f);var book=issued(p);var replay=book.copy();
        peer.setItemInHand(InteractionHand.OFF_HAND,book);h.assertTrue(!burn(peer,f,0)&&pages(book).size()==5,"a borrowed original remains intact and grants no credit");
        p.setItemInHand(InteractionHand.OFF_HAND,book);h.assertTrue(burn(p,f,0),"the owner consumes the first page once");
        p.setItemInHand(InteractionHand.OFF_HAND,replay);h.assertTrue(!burn(p,f,1)&&pages(replay).size()==5,"a duplicate pre-burn original cannot replay an already spent page");
        p.setItemInHand(InteractionHand.OFF_HAND,book);var copied=book.copy();var c=copied.get(DataComponents.WRITTEN_BOOK_CONTENT);copied.set(DataComponents.WRITTEN_BOOK_CONTENT,new WrittenBookContent(c.title(),c.author(),1,c.pages(),c.resolved()));
        p.setItemInHand(InteractionHand.OFF_HAND,copied);h.assertTrue(!burn(p,f,1),"native copied-book generation cannot spend the original's next chapter");
        f.reload();p.setItemInHand(InteractionHand.OFF_HAND,book);h.assertTrue(burn(p,f,1)&&pages(book).size()==3,"the held surviving original continues after native saved-data reload");
    }); }

    @GameTest(template="empty",batch="multiplayer_story",timeoutTicks=1200)
    public static void OrdinaryPaperDoesNotSkipTheReadersNextChapter(GameTestHelper h) { run(h,521500,f->{
        StaircaseFire.dress(f.level,f.origin);var p=f.player(h,"paper_reader");ready(p,f);var book=issued(p);var before=book.copy();
        p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.PAPER,2));h.assertTrue(burn(p,f,0)&&p.getOffhandItem().getCount()==1,"ordinary native paper feeds a fire once");
        h.assertTrue(ItemStack.isSameItemSameComponents(book,before)&&f.data().stateEntry(StaircaseStory.STATE,p.getUUID().toString()).getInt("Burned")==0,"paper does not consume an unread chapter");
        p.setItemInHand(InteractionHand.OFF_HAND,book);h.assertTrue(burn(p,f,1)&&pages(book).size()==4,"the next hearth takes chapter one, rather than deleting a chapter the player never burned");
        p.setGameMode(GameType.SPECTATOR);var held=book.copy();h.assertTrue(!burn(p,f,2)&&ItemStack.isSameItemSameComponents(held,book)&&!StaircaseFire.take(p),"a native spectator neither burns pages nor obtains another original");
    }); }

    @GameTest(template="empty",batch="multiplayer_story",timeoutTicks=1200)
    public static void ExistingTutorialLeavesBecomeTheirRemainingChaptersWithoutRefilling(GameTestHelper h) { run(h,522000,f->{
        StaircaseFire.dress(f.level,f.origin);var p=f.player(h,"legacy_leaf");ready(p,f);var old=StaircaseFire.book(p.getUUID(),3);old.set(DataComponents.CUSTOM_NAME,Component.literal("An old original"));
        var rec=FinaleProgress.player(p.server,p.getUUID());rec.putBoolean("StairBookTaken",true);rec.putInt("StairFires",2);FinaleProgress.save(p.server,p.getUUID(),rec);
        p.setItemInHand(InteractionHand.OFF_HAND,old);h.assertTrue(burn(p,f,2)&&pages(old).size()==2&&text(old).startsWith("IV."),"three old leaves migrate to chapters three through five, then the next actual page burns");
        h.assertTrue(old.get(DataComponents.CUSTOM_NAME).getString().equals("An old original")&&StaircaseFire.leaves(old,p.getUUID())==2&&!StaircaseFire.take(p),"migration preserves naming and finite custody without issuing another book");
        f.reload();h.assertTrue(burn(p,f,3)&&burn(p,f,4)&&old.isEmpty(),"the remaining legacy original can finish after save/reload");
    }); }

    @GameTest(template="empty",batch="multiplayer_story",timeoutTicks=1200)
    public static void CancelingOneNativeStaircaseCrossingDoesNotCommitItOrCancelThePeer(GameTestHelper h) { run(h,522500,f->{
        var p=f.player(h,"cancel_cross");var peer=f.player(h,"live_cross");
        var from=new LabyrinthData.Door("fixture.outside",HouseDimensions.OUTSIDE,f.source,Direction.SOUTH,LabyrinthData.RETURN,false);
        for(var who:List.of(p,peer)){who.teleportTo(f.outside,f.source.getX()+.5,f.source.getY(),f.source.getZ()-1.5,0,0);FinaleController.enter(who,from);}
        h.assertTrue(HouseTransitionEvents.isPending(p)&&HouseTransitionEvents.isPending(peer)&&f.data().returnDepth(p.getUUID())==0&&FinaleProgress.phase(p.server,p.getUUID())==FinaleProgress.Phase.UNSEEN,"pending native crossings have not pushed routes or committed personal arrival");
        HouseTransitionEvents.cancelPending(p,"native multiplayer regression");HouseTransitionEvents.onPlayerTick(new PlayerTickEvent.Post(peer));
        h.assertTrue(p.serverLevel()==f.outside&&f.data().returnDepth(p.getUUID())==0&&!FinaleProgress.player(p.server,p.getUUID()).getBoolean("Discovered"),"canceling one explorer leaves no phantom staircase visit");
        h.assertTrue(peer.serverLevel()==f.level&&f.data().returnDepth(peer.getUUID())==1&&FinaleProgress.phase(peer.server,peer.getUUID())==FinaleProgress.Phase.STAIRCASE,"the peer's native crossing succeeds and commits exactly once");
        HouseTransitionEvents.onPlayerTick(new PlayerTickEvent.Post(peer));h.assertTrue(f.data().returnDepth(peer.getUUID())==1,"a completed handoff cannot push another return");
    }); }

    @GameTest(template="empty",batch="multiplayer_story",timeoutTicks=1200)
    public static void DyingDuringAFadeKeepsTheReturnRouteWhileTheLivePeerCompletes(GameTestHelper h) { run(h,523000,f->{
        var p=f.player(h,"dead_fade");var peer=f.player(h,"live_fade");for(var who:List.of(p,peer))who.teleportTo(f.level,f.source.getX()+.5,f.source.getY(),f.source.getZ()+.5,0,0);
        var target=new LabyrinthData.Waypoint(HouseDimensions.INTERIOR,Vec3.atBottomCenterOf(f.source.north(8)),180,false);
        f.data().pushReturn(p.getUUID(),target);f.data().pushReturn(peer.getUUID(),target);
        LabyrinthDoors.sendBack(p,1,1,1);LabyrinthDoors.sendBack(peer,1,1,1);p.setHealth(0);var before=p.position();serverTick(f);
        h.assertTrue(p.position().equals(before)&&f.data().returnDepth(p.getUUID())==1,"a dead native player is not moved and their route is not spent");
        h.assertTrue(peer.position().distanceToSqr(target.pos())<.01&&f.data().returnDepth(peer.getUUID())==0,"the living peer's independent fade arrives and consumes one route");
    }); }

    @GameTest(template="empty",batch="multiplayer_story",timeoutTicks=1200)
    public static void AReconnectedNativePlayerCannotInheritTheOldConnectionsQueuedFade(GameTestHelper h) { run(h,523500,f->{
        var p=f.player(h,"reconnect_fade");p.teleportTo(f.level,f.source.getX()+.5,f.source.getY(),f.source.getZ()+.5,0,0);
        var back=new LabyrinthData.Waypoint(HouseDimensions.INTERIOR,Vec3.atBottomCenterOf(f.source.north(8)),180,false);f.data().pushReturn(p.getUUID(),back);LabyrinthDoors.sendBack(p,1,1,1);
        var id=p.getUUID();NativeTestPlayers.remove(p);f.players.remove(p);
        var replacement=NativeTestPlayers.survival(h,"reconnect_fade",id);replacement.setNoGravity(true);f.players.add(replacement);replacement.teleportTo(f.level,f.source.getX()+.5,f.source.getY(),f.source.getZ()+.5,0,0);var before=replacement.position();serverTick(f);
        h.assertTrue(replacement.position().equals(before)&&f.data().returnDepth(id)==1,"the same UUID on a new native connection keeps its route and cannot inherit the old entity's pending move");
    }); }

    @GameTest(template="empty",batch="multiplayer_story",timeoutTicks=1200)
    public static void ACompletedFadeCarriesOnlyTheReadersLivingFollowingCompanion(GameTestHelper h) { run(h,524000,f->{
        var p=f.player(h,"fade_pet_owner");var peer=f.player(h,"fade_pet_peer");p.teleportTo(f.level,f.source.getX()+.5,f.source.getY(),f.source.getZ()+.5,0,0);
        var dog=EntityType.WOLF.create(f.level);var stay=EntityType.CAT.create(f.level);var foreign=EntityType.WOLF.create(f.level);
        try{dog.tame(p);stay.tame(p);foreign.tame(peer);dog.moveTo(p.position().add(1,0,0));stay.moveTo(p.position().add(1,0,1));foreign.moveTo(p.position().add(-1,0,0));dog.setHealth(4);
            for(var pet:List.of(dog,stay,foreign)){pet.setNoGravity(true);f.level.addFreshEntity(pet);}CompanionOrders.issue(stay,p,CompanionOrders.Order.STAY);var dogId=dog.getUUID();var catPos=stay.position();var foreignPos=foreign.position();
            f.data().pushReturn(p.getUUID(),new LabyrinthData.Waypoint(HouseDimensions.INTERIOR,Vec3.atBottomCenterOf(f.source.north(12)),180,false));LabyrinthDoors.sendBack(p,1,1,1);serverTick(f);
            h.assertTrue(dog.distanceToSqr(p)<4&&dog.getUUID().equals(dogId)&&dog.getHealth()==4,"the confirmed native fade preserves and carries the owner's actual living dog");
            h.assertTrue(stay.position().equals(catPos)&&foreign.position().equals(foreignPos),"Stay and a peer's companion remain where they were");
        }finally{dog.discard();stay.discard();foreign.discard();}
    }); }

    @GameTest(template="empty",batch="multiplayer_story",timeoutTicks=1200)
    public static void AnActualOverworldBreakBelongsToItsMinerAndCanceledWorkAddsNoMemory(GameTestHelper h) { run(h,524500,f->{
        var p=f.player(h,"world_miner");var peer=f.player(h,"world_peer");var world=p.server.overworld();var at=new BlockPos(524500,80,524500);
        f.chunks.hold(world,new net.minecraft.world.phys.AABB(at).inflate(2));f.put(world,at,Blocks.STONE.defaultBlockState());p.teleportTo(world,at.getX()+.5,at.getY()+1,at.getZ()+.5,0,0);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.IRON_PICKAXE));
        h.assertTrue(p.gameMode.destroyBlock(at),"native survival mining actually removes the Overworld block");StaircaseStory.tick(new ServerTickEvent.Post(()->true,p.server));
        h.assertTrue(text(issued(p)).contains("broke Stone in the Overworld")&&!text(issued(peer)).contains("broke Stone in the Overworld"),"the completed native mining stat gives only the miner a dimension-specific memory");
        var other=f.player(h,"canceled_miner");other.teleportTo(world,at.getX()+.5,at.getY()+1,at.getZ()+.5,0,0);f.put(world,at,Blocks.GOLD_BLOCK.defaultBlockState());
        var canceled=new BlockEvent.BreakEvent(world,at,world.getBlockState(at),other);canceled.setCanceled(true);NeoForge.EVENT_BUS.post(canceled);StaircaseStory.tick(new ServerTickEvent.Post(()->true,other.server));
        h.assertTrue(!text(issued(other)).contains("Gold")&&world.getBlockState(at).is(Blocks.GOLD_BLOCK),"a canceled native break cannot become a recorded action");
    }); }

    @GameTest(template="empty",batch="multiplayer_story",timeoutTicks=1200)
    public static void AFailedStaircaseRetreatKeepsItsPhaseAndWaypointUntilNativeArrival(GameTestHelper h) { run(h,525000,f->{
        var p=f.player(h,"return_retry");ready(p,f);f.data().clearReturns(p.getUUID());
        var back=new LabyrinthData.Waypoint(HouseDimensions.OUTSIDE,Vec3.atBottomCenterOf(f.source),180,false);f.data().pushReturn(p.getUUID(),back);
        var rec=FinaleProgress.player(p.server,p.getUUID());rec.putBoolean("Inside",true);rec.putInt("StairFires",5);FinaleProgress.save(p.server,p.getUUID(),rec);
        p.moveTo(Vec3.atBottomCenterOf(FinaleArchitecture.entry(f.origin).south(2)));p.tickCount=1;FinaleController.tickPlayer(p,f.origin);
        h.assertTrue(HouseTransitionEvents.isPending(p)&&f.data().returnDepth(p.getUUID())==1&&FinaleProgress.phase(p.server,p.getUUID())==FinaleProgress.Phase.STAIRCASE,"preparing the outward native crossing preserves the route and staircase phase");
        HouseTransitionEvents.cancelPending(p,"retry return fixture");h.assertTrue(f.data().peekReturn(p.getUUID()).equals(back),"canceling the first attempt retains the exact waypoint");
        FinaleController.tickPlayer(p,f.origin);HouseTransitionEvents.onPlayerTick(new PlayerTickEvent.Post(p));
        h.assertTrue(p.serverLevel()==f.outside&&f.data().returnDepth(p.getUUID())==0&&FinaleProgress.phase(p.server,p.getUUID())==FinaleProgress.Phase.UNSEEN,"the confirmed native retreat consumes once and resets only this explorer's phase");
    }); }

    @GameTest(template="empty",batch="multiplayer_story",timeoutTicks=1200)
    public static void AForeignArrivalCannotRerollTheSavedExplorersHallwayMap(GameTestHelper h) { run(h,525500,f->{
        var p=f.player(h,"map_owner");var peer=f.player(h,"map_peer");var place=LabyrinthPlace.JUNCTION;var base=LabyrinthPlaces.base(f.origin,place);
        var doors=new ArrayList<LabyrinthData.Door>();
        for(var spec:place.doors()) {
            var door=new LabyrinthData.Door(place.doorId(spec),HouseDimensions.INTERIOR,base.offset(spec.rel()),spec.facing(),spec.destination(),false);f.data().putDoor(door);
            if(LabyrinthData.DEALT.equals(spec.destination()))doors.add(door);
        }
        f.data().pushReturn(p.getUUID(),new LabyrinthData.Waypoint(HouseDimensions.INTERIOR,Vec3.atBottomCenterOf(f.source),180,true));
        LabyrinthDealer.arriveAt(f.data(),p.getUUID(),place,f.origin.asLong());var before=new ArrayList<LabyrinthData.Deal>();for(var door:doors)before.add(f.data().deal(p.getUUID(),door));
        f.data().pushReturn(peer.getUUID(),new LabyrinthData.Waypoint(HouseDimensions.INTERIOR,Vec3.atBottomCenterOf(f.source.east(50)),180,true));LabyrinthDealer.arriveAt(f.data(),peer.getUUID(),place,f.origin.asLong());f.reload();
        LabyrinthDealer.arriveAt(f.data(),p.getUUID(),place,f.origin.asLong());var after=new ArrayList<LabyrinthData.Deal>();for(var door:doors)after.add(f.data().deal(p.getUUID(),f.data().door(door.id)));
        h.assertTrue(before.equals(after)&&before.stream().allMatch(Objects::nonNull),"a peer taking another route and a native reload do not reroll the original explorer's remembered door destinations");
    }); }

    @GameTest(template="empty",batch="multiplayer_story",timeoutTicks=1200)
    public static void ANativeRespawnCannotCompleteTheDeadPlayersPreparedDoorCrossing(GameTestHelper h) { run(h,526000,f->{
        var p=f.player(h,"respawn_cross");p.teleportTo(f.outside,f.source.getX()+.5,f.source.getY(),f.source.getZ()-1.5,0,0);
        var from=new LabyrinthData.Door("fixture.respawn",HouseDimensions.OUTSIDE,f.source,Direction.SOUTH,LabyrinthData.RETURN,false);FinaleController.enter(p,from);
        h.assertTrue(HouseTransitionEvents.isPending(p),"the native original player has a prepared crossing");p.setHealth(0);
        var fresh=p.server.getPlayerList().respawn(p,false,net.minecraft.world.entity.Entity.RemovalReason.KILLED);f.players.remove(p);f.players.add(fresh);fresh.hasChangedDimension();fresh.setNoGravity(true);
        fresh.teleportTo(f.outside,f.source.getX()+.5,f.source.getY(),f.source.getZ()-1.5,0,0);HouseTransitionEvents.onPlayerTick(new PlayerTickEvent.Post(fresh));
        h.assertTrue(fresh!=p&&fresh.isAlive()&&fresh.serverLevel()==f.outside&&!HouseTransitionEvents.isPending(fresh)&&f.data().returnDepth(fresh.getUUID())==0&&FinaleProgress.phase(fresh.server,fresh.getUUID())==FinaleProgress.Phase.UNSEEN,"a same-UUID native respawn in the source dimension cannot commit the original dead entity's arrival");
    }); }
}
