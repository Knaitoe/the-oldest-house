package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID+"_multiplayer") @PrefixGameTestTemplate(false)
public final class ReviewIntegrationTests {
    private static final LabyrinthPlace HALL=LabyrinthPlace.LONG_HALLWAY;
    private static void holdHall(StaircaseAccessTests.Fixture f){var b=LabyrinthPlaces.base(f.origin,HALL);f.chunks.hold(f.level,new AABB(b.offset(-5,-2,-52),b.offset(18,6,5)));}
    private static void buildHall(StaircaseAccessTests.Fixture f){var b=LabyrinthPlaces.base(f.origin,HALL);LabyrinthLoops.buildHallway(f.level,b,HALL);LabyrinthBuilder.registerDoors(f.data(),HALL,b);}
    private static void at(StaircaseAccessTests.Fixture f,ServerPlayer p,Vec3 at){p.teleportTo(f.level,at.x,at.y,at.z,0,0);p.hasChangedDimension();p.setDeltaMovement(Vec3.ZERO);p.connection.resetPosition();}
    private static void period(StaircaseAccessTests.Fixture f,ServerPlayer p,int k){at(f,p,Vec3.atBottomCenterOf(LabyrinthPlaces.base(f.origin,HALL).offset(LabyrinthLoops.periodOrigin(k))));LabyrinthDoors.tickPlayer(p,f.origin);}
    private static void click(ServerPlayer p,BlockPos at){NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(p,InteractionHand.MAIN_HAND,at,new BlockHitResult(at.getCenter(),Direction.WEST,at,false)));}
    private static ItemStack readTake(GameTestHelper h,ServerPlayer p){
        h.assertTrue(p.containerMenu instanceof ExpeditionInquiry.PaperMenu,"the real surface opens its own native investigation paper");var menu=(LecternMenu)p.containerMenu;var book=menu.getSlot(0).getItem().copy();
        int count=book.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size();if(count>1)h.assertTrue(menu.clickMenuButton(p,100+count-1),"the original's last native page is read");
        h.assertTrue(menu.clickMenuButton(p,3)&&!menu.clickMenuButton(p,3),"the native take yields exactly one original");return book;
    }
    private static void travel(ServerPlayer p){HouseExperience.arrived(p,LabyrinthPlace.JUNCTION);HouseExperience.returned(p);}
    @GameTest(template="empty",timeoutTicks=800)
    public static void originalPapersMakeOnePersonalInvestigationAcrossReturnsAndSavedDeskCustody(GameTestHelper h){
        StaircaseAccessTests.run(h,1310000,ReviewIntegrationTests::holdHall,f->{
            buildHall(f);var p=f.player(h,"inquiry_reader");var peer=f.player(h,"inquiry_peer");var b=LabyrinthPlaces.base(f.origin,HALL);
            HouseExperience.arrived(p,HALL);period(f,p,1);period(f,p,2);p.setShiftKeyDown(true);p.getInventory().selected=7;
            var wall=b.offset(LabyrinthLoops.periodOrigin(1)).east(2);click(p,wall);var field=readTake(h,p);
            h.assertTrue(!ExpeditionInquiry.record(peer).getBoolean("Read_field"),"recording a physical crossing confers nothing on a peer");
            var paper=f.source.south(4);var desk=f.source.south(7);
            f.put(f.level,paper,NoteSurfaceBlock.state(HouseMarginalia.Thread.ROOM,Direction.WEST));f.put(f.level,desk,HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.NORTH));
            travel(p);at(f,p,Vec3.atBottomCenterOf(paper.east()));p.setShiftKeyDown(false);click(p,paper);var plan=readTake(h,p);
            at(f,p,Vec3.atBottomCenterOf(desk.east()));click(p,desk);h.assertTrue(p.containerMenu instanceof ExpeditionInquiry.DeskMenu,"the physical desk opens native personal custody");var menu=(ChestMenu)p.containerMenu;
            for(int i=0;i<36;i++)if(!p.getInventory().getItem(i).isEmpty())menu.quickMoveStack(p,i<9?36+i:i);
            h.assertTrue(ExpeditionInquiry.record(p).getBoolean("Compared")&&menu.getContainer().countItem(Items.WRITTEN_BOOK)==2,"laying the two read originals together records comparison");
            h.assertTrue(ItemStack.isSameItemSameComponents(field,menu.getSlot(0).getItem())&&ItemStack.isSameItemSameComponents(plan,menu.getSlot(1).getItem()),"the desk holds the exact original components");p.closeContainer();f.reload();click(p,desk);
            menu=(ChestMenu)p.containerMenu;h.assertTrue(ItemStack.isSameItemSameComponents(field,menu.getSlot(0).getItem())&&ItemStack.isSameItemSameComponents(plan,menu.getSlot(1).getItem()),"reload restores custody without refilling inventory");
            menu.quickMoveStack(p,0);int loanSlot=-1;for(int i=0;i<36;i++)if(ItemStack.isSameItemSameComponents(field,p.getInventory().getItem(i)))loanSlot=i;h.assertTrue(loanSlot>=0,"the original is physically recovered from its desk slot");var loan=p.getInventory().removeItemNoUpdate(loanSlot);p.closeContainer();peer.setItemInHand(InteractionHand.MAIN_HAND,loan);
            var borrowed=new PlayerInteractEvent.RightClickItem(peer,InteractionHand.MAIN_HAND);NeoForge.EVENT_BUS.post(borrowed);
            h.assertTrue(!borrowed.isCanceled()&&!ExpeditionInquiry.record(peer).getBoolean("Read_field")&&!ExpeditionInquiry.record(peer).getBoolean("Compared"),"a borrowed original is readable but never becomes the peer's observation");
            peer.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);p.getInventory().add(loan);travel(p);at(f,p,Vec3.atBottomCenterOf(paper.east()));click(p,paper);var margin=readTake(h,p);
            h.assertTrue(!ExpeditionInquiry.record(p).getBoolean("Revisited")&&margin.get(DataComponents.WRITTEN_BOOK_CONTENT).author().equals("A different hand"),"the later conflicting account invites an actual return, not an automatic resolution");
            HouseExperience.arrived(p,HALL);period(f,p,1);period(f,p,2);period(f,p,0);p.getInventory().selected=7;p.setShiftKeyDown(true);click(p,wall);readTake(h,p);
            h.assertTrue(ExpeditionInquiry.record(p).getBoolean("Revisited")&&!ExpeditionInquiry.record(peer).getBoolean("Revisited")&&WitnessAccount.count(f.data(),p.getUUID())==0,"a personally reversed revisit ends the optional inquiry without awarding Witness or imposing a correct interpretation");
        });
    }
    @GameTest(template="empty",timeoutTicks=800)
    public static void testingHallwayRepetitionOpensItWhilePeersAndOfflineTimeCannotAdvanceIt(GameTestHelper h){
        StaircaseAccessTests.run(h,1311000,ReviewIntegrationTests::holdHall,f->{
            buildHall(f);var p=f.player(h,"hall_tester");var peer=f.player(h,"hall_observer");HouseExperience.arrived(p,HALL);HouseExperience.arrived(peer,HALL);
            period(f,p,1);period(f,p,2);period(f,p,2);var before=LabyrinthLoops.observations(p);f.reload();LabyrinthLoops.forget(p.getUUID());period(f,p,1);
            h.assertTrue(LabyrinthLoops.observations(p).getLong("Occupied")==before.getLong("Occupied")&&LabyrinthLoops.observations(p).getInt("Laps")==2,"reconnecting retains occupied progress and actual reverse distance without adding offline time");
            peer.setGameMode(GameType.SPECTATOR);period(f,peer,1);period(f,peer,2);h.assertTrue(LabyrinthLoops.observations(peer).getInt("Forward")==0,"spectators cannot test the hall for another reader");
            period(f,p,0);period(f,p,2);var b=LabyrinthPlaces.base(f.origin,HALL);
            h.assertTrue(LabyrinthLoops.periodOf(b,p.getZ())==2&&LabyrinthLoops.observations(p).getInt("Forward")==2&&LabyrinthLoops.observations(p).getInt("Backward")==1,"two forward repeats and one reversed repeat physically reveal the way on without enduring the fallback");
            h.assertTrue(LabyrinthLoops.LONG_HALLWAY_TICKS==1800,"the fallback is bounded at ninety occupied seconds");
        });
    }
    private static void door(StaircaseAccessTests.Fixture f,LabyrinthData.Door d,boolean open){
        f.put(f.level,d.lower.below(),Blocks.STONE.defaultBlockState());var s=Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING,d.facing).setValue(DoorBlock.OPEN,open);
        f.put(f.level,d.lower,s);f.put(f.level,d.lower.above(),s.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));
    }
    @GameTest(template="empty",timeoutTicks=800)
    public static void rootsWaitsForAnActualReturnAndKeepsEveryEarlierOriginal(GameTestHelper h){
        StaircaseAccessTests.run(h,1311500,f->{
            var p=f.player(h,"roots_reader");var peer=f.player(h,"roots_peer");var at=f.source.south(4);
            f.put(f.level,at,NoteSurfaceBlock.state(HouseMarginalia.Thread.ROOM,Direction.WEST));at(f,p,Vec3.atBottomCenterOf(at.east()));
            var old=HouseCorrespondence.bind(p,at,HouseMarginalia.Thread.ROOM);var kept=old.book().copy();
            h.assertTrue(!HouseCorrespondence.available(p,NovelCorrespondence.ROOTS),"depth and arrival alone cannot turn the return poem into a routine pool find");
            travel(p);click(p,at);h.assertTrue(p.containerMenu instanceof HouseMarginalia.NotebookMenu,"the return poem uses the existing native paper menu");
            var menu=(HouseMarginalia.NotebookMenu)p.containerMenu;var book=menu.book();h.assertTrue(book.get(DataComponents.WRITTEN_BOOK_CONTENT).title().raw().equals("You Shall Be My Roots"),"an actual safe return gives the poem its setting");
            int pages=book.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size();if(pages>1)menu.clickMenuButton(p,100+pages-1);h.assertTrue(menu.clickMenuButton(p,3),"the source remains a finite collectible original");p.closeContainer();f.reload();click(p,at);
            var again=(HouseMarginalia.NotebookMenu)p.containerMenu;h.assertTrue(ItemStack.isSameItemSameComponents(book,again.book())&&!again.clickMenuButton(p,3),"the saved returned poem cannot refill");
            var saved=HouseCorrespondence.record(f.data(),p.getUUID()).getCompound("Books");h.assertTrue(ItemStack.isSameItemSameComponents(kept,ItemStack.parseOptional(p.registryAccess(),saved.getCompound(old.note())))&&!HouseCorrespondence.available(peer,NovelCorrespondence.ROOTS)&&WitnessAccount.count(f.data(),p.getUUID())==0,"placement preserves earlier words, personal returns and the optional non-Witness status");p.closeContainer();
        });
    }
    @GameTest(template="empty",timeoutTicks=800)
    public static void serviceReturnsRequireAnActualDoorwayCrossingAndKeepEachReadersLockAndRoute(GameTestHelper h){
        StaircaseAccessTests.run(h,1312000,f->{var b=LabyrinthPlaces.base(f.origin,LabyrinthPlace.HOLLOWAY_CAMP);f.chunks.hold(f.level,new AABB(b.offset(-13,-2,-74),b.offset(13,5,4)));},f->{
            var place=LabyrinthPlace.HOLLOWAY_CAMP;var b=LabyrinthPlaces.base(f.origin,place);LabyrinthBuilder.registerDoors(f.data(),place,b);
            var gate=f.data().door(place.id()+"/service");var entry=f.data().door(place.entryDoorId());door(f,gate,true);door(f,entry,true);
            var p=f.player(h,"service_locked");var peer=f.player(h,"service_free");
            for(var reader:java.util.List.of(p,peer)){VignetteGate.begin(reader,place);HouseExperience.arrived(reader,place);at(f,reader,Vec3.atBottomCenterOf(entry.lower.north(3)));LabyrinthDoors.tickPlayer(reader,f.origin);
                f.data().pushReturn(reader.getUUID(),new LabyrinthData.Waypoint(f.level.dimension(),Vec3.atBottomCenterOf(f.source.east(reader==p?0:5)),0,true));}
            var world=f.data().state(HollowayVignette.ID);var people=world.getCompound("Players");var running=new CompoundTag();running.putBoolean("Run",true);people.put(p.getUUID().toString(),running);world.put("Players",people);f.data().setState(HollowayVignette.ID,world);
            at(f,p,Vec3.atBottomCenterOf(gate.lower.south(2)));LabyrinthDoors.tickPlayer(p,f.origin);at(f,p,Vec3.atBottomCenterOf(gate.lower.north(2)));LabyrinthDoors.tickPlayer(p,f.origin);
            h.assertTrue(f.data().returnDepth(p.getUUID())==1&&p.getZ()>gate.lower.getZ()&&!f.level.getBlockState(gate.lower).getValue(DoorBlock.OPEN),"a committed reader cannot escape through the peer's open service gate");
            door(f,gate,true);at(f,peer,Vec3.atBottomCenterOf(gate.lower.south(2).east(4)));LabyrinthDoors.tickPlayer(peer,f.origin);at(f,peer,Vec3.atBottomCenterOf(gate.lower.north(2).east(4)));LabyrinthDoors.tickPlayer(peer,f.origin);
            h.assertTrue(f.data().returnDepth(peer.getUUID())==1,"crossing the infinite plane beside the gate is not a return");
            at(f,peer,Vec3.atBottomCenterOf(gate.lower.south(2).above(4)));LabyrinthDoors.tickPlayer(peer,f.origin);at(f,peer,Vec3.atBottomCenterOf(gate.lower.north(2).above(4)));LabyrinthDoors.tickPlayer(peer,f.origin);
            h.assertTrue(f.data().returnDepth(peer.getUUID())==1,"passing above the doorway cannot trigger a return");
            at(f,peer,Vec3.atBottomCenterOf(gate.lower.south(2)));LabyrinthDoors.tickPlayer(peer,f.origin);door(f,gate,false);at(f,peer,Vec3.atBottomCenterOf(gate.lower.north(2)));LabyrinthDoors.tickPlayer(peer,f.origin);
            h.assertTrue(f.data().returnDepth(peer.getUUID())==1,"a closed gate does not act as a return plane");
            at(f,peer,Vec3.atBottomCenterOf(gate.lower.south(2)));LabyrinthDoors.tickPlayer(peer,f.origin);LabyrinthDoors.use(peer,gate);at(f,peer,Vec3.atBottomCenterOf(gate.lower.north(2)));LabyrinthDoors.tickPlayer(peer,f.origin);
            h.assertTrue(f.data().returnDepth(peer.getUUID())==0&&peer.position().distanceToSqr(f.source.east(5).getCenter())<16&&f.data().returnDepth(p.getUUID())==1,"the actual unlocked crossing returns only its own reader and consumes only their route");
        });
    }
}
