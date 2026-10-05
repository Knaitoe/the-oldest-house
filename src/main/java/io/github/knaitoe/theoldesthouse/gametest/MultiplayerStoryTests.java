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
    /** The binding the camp shelf actually gave, taken out of the real inventory. */
    private static ItemStack issued(ServerPlayer p) {
        if(!StaircaseFire.take(p))throw new IllegalStateException("binding was not issued");
        return held(p);
    }
    private static ItemStack held(ServerPlayer p) {
        for(int i=0;i<p.getInventory().getContainerSize();i++) {
            var book=p.getInventory().getItem(i);
            if(StaircaseStory.isCurrent(p,book)){p.getInventory().setItem(i,ItemStack.EMPTY);return book;}
        }
        throw new IllegalStateException("current original missing from actual inventory");
    }
    private static List<net.minecraft.server.network.Filterable<Component>> pages(ItemStack book) { return book.get(DataComponents.WRITTEN_BOOK_CONTENT).pages(); }
    private static String story(StaircaseAccessTests.Fixture f,ServerPlayer p) {
        var all=f.data().stateEntry(StaircaseStory.STATE,p.getUUID().toString()).getList("Pages",8);var text=new StringBuilder();
        for(int i=0;i<all.size();i++)text.append(all.getString(i)).append('\n');return text.toString();
    }
    private static void ready(ServerPlayer p,StaircaseAccessTests.Fixture f) {
        f.enter(p);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.FLINT_AND_STEEL));
    }
    private static boolean burn(ServerPlayer p,StaircaseAccessTests.Fixture f,int i) {
        var at=StaircaseFire.braziers(f.origin).get(i);p.teleportTo(f.level,at.getX()+1.5,at.getY(),at.getZ()+.5,0,0);
        return StaircaseFire.ignite(p,f.origin,at);
    }
    /** Hold the chunks of every flight's leaf before the fixture starts. */
    static void holdLeaves(StaircaseAccessTests.Fixture f) { for(var at:StaircaseLeaves.positions(f.origin))f.chunks.hold(f.level,new net.minecraft.world.phys.AABB(at).inflate(2)); }
    /** The sheets on their level treads, as the staircase lays them. */
    static void layLeaves(StaircaseAccessTests.Fixture f) {
        for(var at:StaircaseLeaves.positions(f.origin)){f.put(f.level,at.below(),Blocks.STONE.defaultBlockState());f.put(f.level,at,NoteSurfaceBlock.state(HouseMarginalia.Thread.HOUSEKEEPING,Direction.NORTH));}
    }
    /** Walk to this flight's sheet, open it through its native lectern page and press Take. */
    private static boolean find(ServerPlayer p,StaircaseAccessTests.Fixture f,int i) {
        var at=StaircaseLeaves.positions(f.origin).get(i);p.teleportTo(f.level,at.getX()+1.5,at.getY(),at.getZ()+.5,0,0);
        if(!StaircaseLeaves.open(p,at)||!(p.containerMenu instanceof net.minecraft.world.inventory.LecternMenu menu))return false;
        boolean taken=menu.clickMenuButton(p,3);p.closeContainer();return taken;
    }
    private static void serverTick(StaircaseAccessTests.Fixture f) { LabyrinthDoors.onServerTick(new ServerTickEvent.Post(()->true,f.level.getServer())); }
    private static void run(GameTestHelper h,int at,java.util.function.Consumer<StaircaseAccessTests.Fixture> setup,java.util.function.Consumer<StaircaseAccessTests.Fixture> test) { StaircaseAccessTests.run(h,at,setup,test); }

    @GameTest(template="empty",batch="multiplayer_story",timeoutTicks=1200)
    public static void twoNativeExplorersGetTheirOwnRecordedLivesAndImmutableSavedPages(GameTestHelper h) { run(h,520000,f->{
        var p=f.player(h,"story_road");var peer=f.player(h,"story_care");
        p.awardStat(Stats.WALK_ONE_CM,123400);p.awardStat(Stats.DEATHS,2);p.awardStat(Stats.ITEM_CRAFTED.get(Items.BREAD),7);
        var facts=new CompoundTag();facts.putInt("Care",1);facts.putString("CaredName","Juniper");facts.putBoolean("CaredNamed",true);HouseExperience.save(f.data(),peer.getUUID(),facts);
        peer.awardStat(Stats.DEATHS,1);peer.awardStat(Stats.SLEEP_IN_BED,1);
        var letter=new CompoundTag();letter.putString("SafeRetreat",LabyrinthPlace.TED_CAVER.id());f.data().setStateEntry(HouseCorrespondence.ID,p.getUUID().toString(),letter);
        var first=issued(p);var second=issued(peer);
        h.assertTrue(pages(first).size()==1&&pages(second).size()==1&&StaircaseFire.leaves(first,p.getUUID())==0&&pages(first).getFirst().raw().getString().contains("story_road"),"the shelf gives only the binding and this reader's title leaf; the story leaves are on the flights");
        String mine=story(f,p),theirs=story(f,peer);
        h.assertTrue(f.data().stateEntry(StaircaseStory.STATE,p.getUUID().toString()).getList("Pages",8).size()==5,"five story leaves are written for the five flights");
        h.assertTrue(mine.contains("1,234 metres")&&mine.contains("twice")&&(mine.contains("7 loaves")||mine.contains("7 times"))&&mine.contains("the narrow cave")&&!mine.contains("ted_caver")&&!mine.contains("ted caver"),"one player's native stats and confirmed retreat, by its prose name, form their story: "+mine);
        h.assertTrue(theirs.contains("Juniper")&&!mine.contains("Juniper")&&!theirs.contains("1,234"),"a shared shelf never borrows the peer's biography");
        h.assertTrue(theirs.contains("once")&&!theirs.contains("1 times")&&!theirs.contains("1 nights"),"one recorded death and bed rest use singular wording");
        var before=mine;p.awardStat(Stats.DEATHS,1);f.reload();
        h.assertTrue(before.equals(story(f,p)),"the written leaves remain unchanged as play continues");
    }); }

    @GameTest(template="empty",batch="multiplayer_story",timeoutTicks=1200)
    public static void eachExplorerFindsTheirOwnLeafOnEveryFlightAndBurnsItAtTheSharedHearths(GameTestHelper h) { run(h,520500,MultiplayerStoryTests::holdLeaves,f->{
        StaircaseFire.dress(f.level,f.origin);layLeaves(f);var p=f.player(h,"burn_first");var peer=f.player(h,"burn_peer");ready(p,f);ready(peer,f);
        var first=issued(p);var second=issued(peer);p.setItemInHand(InteractionHand.OFF_HAND,first);peer.setItemInHand(InteractionHand.OFF_HAND,second);
        first.set(DataComponents.CUSTOM_NAME,Component.literal("My kept account"));var extra=first.get(DataComponents.CUSTOM_DATA).copyTag();extra.putString("Kept","unchanged");first.set(DataComponents.CUSTOM_DATA,CustomData.of(extra));
        p.awardStat(Stats.DEATHS,3);int stats=p.getStats().getValue(Stats.CUSTOM.get(Stats.DEATHS));var own=f.data().stateEntry(StaircaseStory.STATE,p.getUUID().toString());
        for(int i=0;i<5;i++){
            h.assertTrue(!burn(p,f,i)&&StaircaseFire.flames(FinaleProgress.player(p.server,p.getUUID()))==i,"hearth "+i+" stays cold until this flight's leaf is bound");
            h.assertTrue(find(p,f,i),"the first reader finds and binds their own leaf on flight "+i);
            h.assertTrue(pages(first).size()==2&&pages(first).get(1).raw().getString().equals(StaircaseStory.leaf(own,i)),"the binding holds its title leaf and the leaf just found");
            h.assertTrue(burn(p,f,i),"the first reader burns leaf "+i);
            h.assertTrue(StaircaseFire.flames(FinaleProgress.player(peer.server,peer.getUUID()))==i,"shared firelight never advances a peer");
            h.assertTrue(find(peer,f,i)&&burn(peer,f,i),"the peer reads their own words on the same sheet and feeds the already lit hearth");
            if(i<4)h.assertTrue(pages(first).size()==1&&first.get(DataComponents.CUSTOM_NAME).getString().equals("My kept account")&&first.get(DataComponents.CUSTOM_DATA).copyTag().getString("Kept").equals("unchanged"),"burning takes only the bound leaf and keeps the original's other components");
        }
        h.assertTrue(first.isEmpty()&&second.isEmpty()&&p.getStats().getValue(Stats.CUSTOM.get(Stats.DEATHS))==stats,"the last leaf takes the binding with it; remembered facts remain in Minecraft stats");
        f.reload();h.assertTrue(StaircaseFire.open(FinaleProgress.player(p.server,p.getUUID()))&&StaircaseFire.open(FinaleProgress.player(peer.server,peer.getUUID()))&&!StaircaseFire.take(p),"both personal descents survive native saved-data reload without another binding");
    }); }

    @GameTest(template="empty",batch="multiplayer_story",timeoutTicks=1200)
    public static void OnlyTheExplorersOwnBoundLeafCanLightTheirFire(GameTestHelper h) { run(h,521000,MultiplayerStoryTests::holdLeaves,f->{
        StaircaseFire.dress(f.level,f.origin);layLeaves(f);var p=f.player(h,"leaf_owner");var peer=f.player(h,"leaf_borrower");ready(p,f);ready(peer,f);var book=issued(p);
        p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.PAPER,4));
        h.assertTrue(!burn(p,f,0)&&p.getOffhandItem().getCount()==4&&StaircaseFire.flames(FinaleProgress.player(p.server,p.getUUID()))==0,"ordinary paper no longer lights a hearth");
        p.setItemInHand(InteractionHand.OFF_HAND,book);h.assertTrue(!burn(p,f,0),"a binding with no found leaf gives the fire nothing");
        h.assertTrue(find(p,f,0)&&pages(book).size()==2,"the owner binds the first flight's leaf");
        var copied=book.copy();var c=copied.get(DataComponents.WRITTEN_BOOK_CONTENT);copied.set(DataComponents.WRITTEN_BOOK_CONTENT,new WrittenBookContent(c.title(),c.author(),1,c.pages(),c.resolved()));
        p.setItemInHand(InteractionHand.OFF_HAND,copied);h.assertTrue(!burn(p,f,0),"a native copy of the original cannot burn");
        peer.setItemInHand(InteractionHand.OFF_HAND,book);h.assertTrue(!burn(peer,f,0)&&pages(book).size()==2,"a borrowed original grants no credit and stays intact");
        peer.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);var replay=book.copy();p.setItemInHand(InteractionHand.OFF_HAND,book);h.assertTrue(burn(p,f,0),"the owner's original burns its bound leaf once");
        p.setItemInHand(InteractionHand.OFF_HAND,replay);h.assertTrue(!burn(p,f,1)&&pages(replay).size()==1,"a duplicate made before the burn cannot spend the leaf again");
        p.setGameMode(GameType.SPECTATOR);h.assertTrue(!find(p,f,1)&&!StaircaseFire.take(p)&&!burn(p,f,1),"a native spectator neither binds leaves, takes bindings nor burns");
        p.setGameMode(GameType.SURVIVAL);p.setItemInHand(InteractionHand.OFF_HAND,book);f.reload();
        h.assertTrue(find(p,f,1)&&burn(p,f,1)&&StaircaseFire.flames(FinaleProgress.player(p.server,p.getUUID()))==2,"the held original continues after native saved-data reload");
    }); }

    @GameTest(template="empty",batch="multiplayer_story",timeoutTicks=1200)
    public static void ALeafComesLooseOnlyInOrderIntoItsCarriedBindingAndTheDarkHoldsTheNext(GameTestHelper h) { run(h,521500,MultiplayerStoryTests::holdLeaves,f->{
        StaircaseFire.dress(f.level,f.origin);layLeaves(f);var p=f.player(h,"leaf_order");ready(p,f);
        h.assertTrue(StaircaseStory.leafState(p,0)==StaircaseStory.Take.NO_BINDING&&!find(p,f,0),"a leaf stays blank until the reader has their binding");
        p.getInventory().add(issued(p));
        h.assertTrue(StaircaseStory.leafState(p,1)==StaircaseStory.Take.EARLIER&&!find(p,f,1),"a later flight's leaf waits for the earlier one");
        var book=held(p);h.assertTrue(StaircaseStory.leafState(p,0)==StaircaseStory.Take.NOT_CARRIED&&!find(p,f,0),"a leaf does not come loose without the binding to hold it");
        p.setItemInHand(InteractionHand.OFF_HAND,book);
        h.assertTrue(find(p,f,0)&&StaircaseStory.leafState(p,0)==StaircaseStory.Take.TAKEN&&!find(p,f,0),"each reader binds a flight's leaf once");
        var leaf=StaircaseLeaves.positions(f.origin).get(1);var record=FinaleProgress.player(p.server,p.getUUID());
        p.teleportTo(f.level,leaf.getX()+.5,leaf.getY(),leaf.getZ()+.5,0,0);
        h.assertTrue(StaircaseFire.tick(p,f.origin,record),"the next flight's leaf lies beyond the reach of the unlit dark");
        h.assertTrue(burn(p,f,0),"the bound first leaf lights the first hearth");
        record=FinaleProgress.player(p.server,p.getUUID());p.teleportTo(f.level,leaf.getX()+.5,leaf.getY(),leaf.getZ()+.5,0,0);
        h.assertTrue(!StaircaseFire.tick(p,f.origin,record)&&find(p,f,1),"the lit hearth lets the reader down to the next flight's leaf");
    }); }

    @GameTest(template="empty",batch="multiplayer_story",timeoutTicks=1200)
    public static void ALostBindingIsBoundAgainWithoutRefillingAndTheOldCopyGoesCold(GameTestHelper h) { run(h,522000,MultiplayerStoryTests::holdLeaves,f->{
        StaircaseFire.dress(f.level,f.origin);layLeaves(f);var p=f.player(h,"leaf_lost");ready(p,f);var book=issued(p);p.setItemInHand(InteractionHand.OFF_HAND,book);
        h.assertTrue(find(p,f,0)&&burn(p,f,0)&&find(p,f,1),"the reader burns the first leaf and binds the second");
        var lost=book.copy();p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);var own=f.data().stateEntry(StaircaseStory.STATE,p.getUUID().toString());
        h.assertTrue(StaircaseStory.shelf(p)==StaircaseStory.Shelf.REBOUND,"the camp shelf binds a lost story again");
        var again=held(p);
        h.assertTrue(pages(again).size()==2&&pages(again).get(1).raw().getString().equals(StaircaseStory.leaf(own,1)),"the new binding holds exactly the found, unburned leaf; nothing burned is refilled");
        h.assertTrue(!StaircaseStory.isCurrent(p,lost)&&p.getInventory().countItem(Items.FLINT_AND_STEEL)==1,"the old binding goes cold and no extra striker is given while one is held");
        p.setItemInHand(InteractionHand.OFF_HAND,lost);h.assertTrue(!burn(p,f,1),"the cold copy cannot burn");
        p.setItemInHand(InteractionHand.OFF_HAND,again);h.assertTrue(burn(p,f,1)&&StaircaseStory.shelf(p)==StaircaseStory.Shelf.CARRIED,"the rebound original burns and the shelf gives nothing more while it is carried");
    }); }

    @GameTest(template="empty",batch="multiplayer_story",timeoutTicks=1200)
    public static void EarlierOriginalsKeepTheLeavesTheyAlreadyHeld(GameTestHelper h) { run(h,526500,f->{
        StaircaseFire.dress(f.level,f.origin);var p=f.player(h,"legacy_leaf");var peer=f.player(h,"legacy_story");ready(p,f);ready(peer,f);
        var old=StaircaseFire.book(p.getUUID(),3);old.set(DataComponents.CUSTOM_NAME,Component.literal("An old original"));
        var rec=FinaleProgress.player(p.server,p.getUUID());rec.putBoolean("StairBookTaken",true);rec.putInt("StairFires",2);FinaleProgress.save(p.server,p.getUUID(),rec);
        p.setItemInHand(InteractionHand.OFF_HAND,old);p.tickCount=20;StaircaseStory.tick(new ServerTickEvent.Post(()->true,p.server));
        h.assertTrue(pages(old).size()==4&&StaircaseFire.leaves(old,p.getUUID())==3&&old.get(DataComponents.CUSTOM_NAME).getString().equals("An old original"),"a 0.4.33-0.4.43 tutorial original becomes its reader's account still holding its three leaves");
        h.assertTrue(burn(p,f,2)&&pages(old).size()==3&&!StaircaseFire.take(p),"the held leaves burn without searching again, and no second binding is issued");
        f.reload();h.assertTrue(burn(p,f,3)&&burn(p,f,4)&&old.isEmpty(),"the upgraded original finishes after save/reload");
        var account=new CompoundTag();var id=UUID.randomUUID();account.putUUID("Original",id);var leaves=new net.minecraft.nbt.ListTag();
        for(int i=0;i<5;i++)leaves.add(net.minecraft.nbt.StringTag.valueOf("Chapter "+i));account.put("Pages",leaves);account.putInt("Burned",1);f.data().setStateEntry(StaircaseStory.STATE,peer.getUUID().toString(),account);
        var earlier=HouseWriting.book("House of Leaves",peer.getGameProfile().getName(),HouseWriting.WritingStyle.WILL,List.of("Chapter 1","Chapter 2","Chapter 3","Chapter 4"));
        var tag=new CompoundTag();tag.putUUID("StairReader",peer.getUUID());tag.putUUID("StairStory",id);tag.putInt("StairPage",1);tag.putInt(StaircaseFire.LEAVES,4);earlier.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
        var prec=FinaleProgress.player(peer.server,peer.getUUID());prec.putBoolean("StairBookTaken",true);prec.putInt("StairFires",1);FinaleProgress.save(peer.server,peer.getUUID(),prec);
        peer.setItemInHand(InteractionHand.OFF_HAND,earlier);StaircaseStory.sync(peer);
        h.assertTrue(pages(earlier).size()==5&&pages(earlier).get(1).raw().getString().equals("Chapter 1")&&burn(peer,f,1),"a 0.4.44 account keeps its four written chapters, gains a title leaf and burns on");
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
        java.util.function.Function<ServerPlayer,String> broke=who->StaircaseAccount.facts(who,StaircaseStory.record(who)).stream().filter(x->x.kind().id().equals("broke")).map(StaircaseProse.Fact::value).findFirst().orElse("");
        h.assertTrue(broke.apply(p).equals("stone")&&broke.apply(peer).isEmpty(),"the completed native mining stat gives only the miner a memory, named from the block itself");
        var other=f.player(h,"canceled_miner");other.teleportTo(world,at.getX()+.5,at.getY()+1,at.getZ()+.5,0,0);f.put(world,at,Blocks.GOLD_BLOCK.defaultBlockState());
        var canceled=new BlockEvent.BreakEvent(world,at,world.getBlockState(at),other);canceled.setCanceled(true);NeoForge.EVENT_BUS.post(canceled);StaircaseStory.tick(new ServerTickEvent.Post(()->true,other.server));
        h.assertTrue(broke.apply(other).isEmpty()&&world.getBlockState(at).is(Blocks.GOLD_BLOCK),"a canceled native break cannot become a recorded action");
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
