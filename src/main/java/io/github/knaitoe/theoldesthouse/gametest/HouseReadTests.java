package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.LecternMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.level.block.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID+"_house_read") @PrefixGameTestTemplate(false)
public final class HouseReadTests {
    private static LiteraryTests.Fixture active;
    @AfterBatch(batch="read_wardrobe") public static void wardrobeDone(ServerLevel ignored){close();}
    @AfterBatch(batch="read_route") public static void routeDone(ServerLevel ignored){close();}
    @AfterBatch(batch="read_porch") public static void porchDone(ServerLevel ignored){close();}
    private static void close(){HouseReadSites.clear();if(active!=null){active.close();active=null;}}
    private static LiteraryTests.Fixture fixture(GameTestHelper h){active=new LiteraryTests.Fixture(h);return active;}
    private static ServerPlayer player(GameTestHelper h,LiteraryTests.Fixture f,String name){var p=NativeTestPlayers.survival(h,name);f.players.add(p);io.github.knaitoe.theoldesthouse.opening.OpeningSequence.state(p).markEntered();p.setNoGravity(true);return p;}
    private static void transition(ServerPlayer p){if(LabyrinthDoors.isBusy(p))HouseTransitionEvents.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(p));}
    private static void click(ServerPlayer p,BlockPos at){p.gameMode.useItemOn(p,p.serverLevel(),p.getMainHandItem(),InteractionHand.MAIN_HAND,new BlockHitResult(at.getCenter(),Direction.SOUTH,at,false));}
    private static LabyrinthData.Door source(LiteraryTests.Fixture f){var at=f.origin.offset(0,0,-5);for(var pos:BlockPos.betweenClosed(at.offset(-4,-1,-3),at.offset(4,-1,5)))f.in.setBlock(pos,Blocks.OAK_PLANKS.defaultBlockState(),18);NovelRooms.door(f.in,at,Direction.SOUTH,Blocks.OAK_DOOR,false);var door=new LabyrinthData.Door("native/read/source",f.in.dimension(),at,Direction.SOUTH,LabyrinthData.DEALT,false);f.data().putDoor(door);return door;}
    private static void stand(ServerPlayer p,ServerLevel l,BlockPos at,double x,double y,double z){p.stopRiding();p.teleportTo(l,at.getX()+x,at.getY()+y,at.getZ()+z,180,0);p.setDeltaMovement(Vec3.ZERO);p.connection.resetPosition();p.hasChangedDimension();}
    private static void seed(ServerPlayer p,HouseReadSites.Room room,LabyrinthData.Door from){var d=LabyrinthData.get(p.server);var own=HouseReadSites.state(p.server,p.getUUID());var visit=own.getCompound(room.key());visit.putBoolean("Entered",true);visit.putBoolean("Here",true);visit.putString("SourceDoor",from.id);visit.putString("NextPlace",LabyrinthPlace.GRAY_CORRIDOR.id());own.put(room.key(),visit);own.putString("Active",room.key());d.setStateEntry(HouseReadSites.STATE,p.getUUID().toString(),own);}
    private static boolean ready(ServerPlayer p,HouseReadSites.Room room){boolean ready=HouseReadSites.prepare(p,room)!=null;if(!ready)java.util.concurrent.locks.LockSupport.parkNanos(2_000_000L);return ready;}
    private static CompoundTag visit(ServerPlayer p,HouseReadSites.Room room){return HouseReadSites.state(p.server,p.getUUID()).getCompound(room.key());}

    @GameTest(template="empty",batch="read_wardrobe",timeoutTicks=4000)
    public static void actualBorrowingRetreatRestoresFullInventoryAndTheOriginalGearAfterReload(GameTestHelper h){
        var f=fixture(h);var p=player(h,f,"coat_reader");var peer=player(h,f,"coat_observer");var sourceAt=f.origin.offset(0,0,-5);f.chunks.hold(f.in,new AABB(sourceAt).inflate(8));BlockPos[] b={null};LabyrinthData.Door[] from={null};ItemStack[] gear={null,null};h.onEachTick(()->transition(p));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native source is resident")).thenExecute(()->{from[0]=source(f);stand(p,f.in,sourceAt,.5,0,2.5);HouseReadSites.prepare(p,HouseReadSites.Room.COATROOM);})
          .thenWaitUntil(()->h.assertTrue(ready(p,HouseReadSites.Room.COATROOM),"the bounded production coatroom finishes"))
          .thenExecute(()->{
              b[0]=HouseReadSites.base(p.server,p.getUUID(),HouseReadSites.Room.COATROOM);seed(p,HouseReadSites.Room.COATROOM,from[0]);stand(p,f.in,b[0],.5,0,.5);stand(peer,f.in,b[0],2.5,0,.5);
              for(int i=0;i<36;i++)p.getInventory().setItem(i,new ItemStack(Items.COBBLESTONE,64));gear[0]=new ItemStack(Items.DIAMOND_CHESTPLATE);gear[0].set(DataComponents.CUSTOM_NAME,Component.literal("Original coat armour"));gear[0].setDamageValue(37);gear[1]=new ItemStack(Items.DIAMOND_BOOTS);gear[1].setDamageValue(19);p.setItemSlot(EquipmentSlot.CHEST,gear[0].copy());p.setItemSlot(EquipmentSlot.FEET,gear[1].copy());
              p.lookAt(EntityAnchorArgument.Anchor.EYES,b[0].offset(HouseReadRooms.UMBRELLA_AT).getCenter());
              var peg=b[0].offset(HouseReadRooms.PEGS.get(0));var part=f.in.getBlockState(peg).getValue(HouseReadPropBlock.PART);HouseReadSites.use(peer,peg,part);h.assertTrue(peer.getItemBySlot(EquipmentSlot.CHEST).isEmpty(),"an observer cannot borrow another reader's clothes");
              click(p,peg);var boots=peg.east();click(p,boots);h.assertTrue(HouseReadSites.loan(p.getItemBySlot(EquipmentSlot.CHEST),p.getUUID())&&HouseReadSites.loan(p.getItemBySlot(EquipmentSlot.FEET),p.getUUID()),"actual block use equips both original room loans even with a full pack");
          }).thenIdle(6).thenExecute(()->{
              h.assertTrue(p.getTicksFrozen()==0&&!visit(p,HouseReadSites.Room.COATROOM).getBoolean("Written"),"clothing lifts the harmless frost and a watched slip stays blank");p.setYRot(180);p.setXRot(0);
          }).thenWaitUntil(()->h.assertTrue(visit(p,HouseReadSites.Room.COATROOM).getBoolean("Written"),"the slip acquires writing only after looking away"))
          .thenExecute(()->{
              stand(p,f.in,b[0],3.5,0,1.5);click(p,b[0].offset(HouseReadRooms.UMBRELLA_AT));h.assertTrue(p.containerMenu instanceof LecternMenu&&!p.containerMenu.clickMenuButton(p,3),"the note opens actual native pages and cannot be printed or taken");p.closeContainer();
              var glass=b[0].offset(HouseReadRooms.MIRROR);stand(p,f.in,b[0],-3.5,0,-7.5);click(p,glass);h.assertTrue(f.in.getBlockState(glass).getValue(HouseReadPropBlock.TURNED),"native glass use turns the actual mirror to its wooden back");click(p,b[0].offset(-3,0,-9));h.assertTrue(f.in.getBlockState(glass).getValue(HouseReadPropBlock.PART)==HouseReadPropBlock.Part.COVERED_MIRROR,"the nearby towel physically covers the same glass");click(p,glass);h.assertTrue(f.in.getBlockState(glass).getValue(HouseReadPropBlock.PART)==HouseReadPropBlock.Part.MIRROR,"the same mirror can be uncovered again");
              f.reload();h.assertTrue(visit(p,HouseReadSites.Room.COATROOM).getBoolean("CapturedChest")&&visit(p,HouseReadSites.Room.COATROOM).getBoolean("Written"),"native saved-data reload preserves gear custody and the unseen writing");
              HouseReadExport.save(f.in,b[0],HouseReadSites.Room.COATROOM,"coatroom");var entry=LabyrinthData.get(p.server).door("house_read/"+p.getUUID()+"/coatroom/entry");stand(p,f.in,b[0],.5,0,1);click(p,entry.lower);
          }).thenWaitUntil(()->h.assertTrue(HouseReadSites.inside(p)==null&&!LabyrinthDoors.isBusy(p),"the actual original door permits immediate retreat"))
          .thenExecute(()->{
              h.assertTrue(ItemStack.matches(gear[0],p.getItemBySlot(EquipmentSlot.CHEST))&&ItemStack.matches(gear[1],p.getItemBySlot(EquipmentSlot.FEET)),"the exact original gear, names and damage return to their original slots");for(int i=0;i<36;i++)h.assertTrue(p.getInventory().getItem(i).is(Items.COBBLESTONE)&&p.getInventory().getItem(i).getCount()==64,"a full original pack is unchanged");
              h.assertTrue(f.data().returnDepth(p.getUUID())==0&&p.getHealth()==20,"a quiet retreat consumes no story return, health or reward");
          }).thenSucceed();
    }

    @GameTest(template="empty",batch="read_route",timeoutTicks=6000)
    public static void twoReadersCrossOneDoorIntoPrivateCopiesAndRejoinTheSameOrdinaryHallWithoutWaiting(GameTestHelper h){
        var f=fixture(h);var a=player(h,f,"quiet_a");var b=player(h,f,"quiet_b");var at=f.origin.offset(0,0,-5);var target=f.origin.offset(0,0,-45);f.chunks.hold(f.in,new AABB(at).inflate(6));f.chunks.hold(f.in,new AABB(target).inflate(7));LabyrinthData.Door[] from={null};BlockPos[] ab={null},bb={null};h.onEachTick(()->{transition(a);transition(b);});
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"both native shared doorways are resident")).thenExecute(()->{
            from[0]=source(f);for(var pos:BlockPos.betweenClosed(target.offset(-4,-1,-5),target.offset(4,-1,5)))f.in.setBlock(pos,Blocks.OAK_PLANKS.defaultBlockState(),18);LabyrinthBuilder.buildCorridor(f.in,target);LabyrinthBuilder.registerDoors(f.data(),LabyrinthPlace.GRAY_CORRIDOR,target);
            for(var p:List.of(a,b)){stand(p,f.in,at,.5,0,2);f.data().deal(p.getUUID(),from[0],LabyrinthPlace.GRAY_CORRIDOR.id(),false);click(p,at);}
          }).thenWaitUntil(()->{boolean present=HouseReadSites.inside(a)==HouseReadSites.Room.COATROOM&&HouseReadSites.inside(b)==HouseReadSites.Room.COATROOM&&!LabyrinthDoors.isBusy(a)&&!LabyrinthDoors.isBusy(b);if(!present)java.util.concurrent.locks.LockSupport.parkNanos(2_000_000L);h.assertTrue(present,"the same original door gives both actual native players a complete private copy");})
          .thenExecute(()->{
              ab[0]=HouseReadSites.base(a.server,a.getUUID(),HouseReadSites.Room.COATROOM);bb[0]=HouseReadSites.base(b.server,b.getUUID(),HouseReadSites.Room.COATROOM);h.assertTrue(!ab[0].equals(bb[0]),"the private copies are distinct physical sites");stand(a,f.in,ab[0],.5,0,-9);click(a,ab[0].offset(0,0,-10));
          }).thenWaitUntil(()->h.assertTrue(HouseReadSites.inside(a)==null&&!LabyrinthDoors.isBusy(a)&&a.distanceToSqr(target.getCenter())<25,"the first reader proceeds immediately into the original ordinary hall"))
          .thenExecute(()->{h.assertTrue(HouseReadSites.inside(b)==HouseReadSites.Room.COATROOM,"their companion is still free to linger in the other copy");stand(b,f.in,bb[0],.5,0,-9);click(b,bb[0].offset(0,0,-10));})
          .thenWaitUntil(()->h.assertTrue(HouseReadSites.inside(b)==null&&!LabyrinthDoors.isBusy(b)&&b.position().distanceToSqr(a.position())<.01,"the second reader independently rejoins that same shared hall"))
          .thenExecute(()->{
              h.assertTrue(f.data().returnDepth(a.getUUID())==1&&f.data().returnDepth(b.getUUID())==1,"each route contains exactly the genuine original forward crossing");var back=new LabyrinthData.Waypoint(from[0].dimension,Vec3.atBottomCenterOf(from[0].lower),from[0].facing.toYRot(),true);var originalKey=f.data().nodeKey(a.getUUID(),LabyrinthPlace.GRAY_CORRIDOR);h.assertTrue(f.data().node(a.getUUID(),originalKey)!=null,"the original destination has the ordinary native remembered map");
              h.assertTrue(HouseReadSites.next(a.server,a.getUUID())==HouseReadSites.Room.PORCH&&WitnessAccount.Story.of(LabyrinthPlace.READ_COATROOM.id())==null&&WitnessAccount.Story.of(LabyrinthPlace.READ_PORCH.id())==null,"the next quiet moment is the porch, and neither room is a Witness source");
          }).thenSucceed();
    }

    @GameTest(template="empty",batch="read_porch",timeoutTicks=5000)
    public static void porchWhistleVoiceAndAllQuietWaysOnRemainPersonalAndTheRailWritesUnseen(GameTestHelper h){
        var f=fixture(h);var p=player(h,f,"porch_reader");var peer=player(h,f,"porch_peer");var at=f.origin.offset(0,0,-5);f.chunks.hold(f.in,new AABB(at).inflate(6));BlockPos[] b={null};LabyrinthData.Door[] from={null};h.onEachTick(()->transition(p));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the original House door is loaded")).thenExecute(()->{from[0]=source(f);stand(p,f.in,at,.5,0,2);stand(peer,f.in,at,2.5,0,2);HouseReadSites.prepare(p,HouseReadSites.Room.PORCH);})
          .thenWaitUntil(()->h.assertTrue(ready(p,HouseReadSites.Room.PORCH),"the whole bounded private yard is complete"))
          .thenExecute(()->{b[0]=HouseReadSites.base(p.server,p.getUUID(),HouseReadSites.Room.PORCH);seed(p,HouseReadSites.Room.PORCH,from[0]);stand(p,f.out,b[0],.5,0,8.5);p.lookAt(EntityAnchorArgument.Anchor.EYES,b[0].offset(HouseReadRooms.RAIL_AT).getCenter());HouseReadExport.save(f.out,b[0],HouseReadSites.Room.PORCH,"porch");})
          .thenWaitUntil(()->h.assertTrue(visit(p,HouseReadSites.Room.PORCH).getInt("Age")>=435,"the actual ten-second whistle and quiet observation window finish"))
          .thenExecute(()->{
              var packets=NativeTestPlayers.packets(p);h.assertTrue(packets.stream().anyMatch(x->x instanceof net.minecraft.network.protocol.game.ClientboundSoundPacket s&&s.getSound().value()==HouseReadRegistry.WHISTLE.get())&&packets.stream().anyMatch(x->x instanceof net.minecraft.network.protocol.game.ClientboundSoundPacket s&&s.getSound().value()==HouseReadRegistry.SUPPER.get()),"the actual reader receives both original positional supper cues");
              h.assertTrue(NativeTestPlayers.packets(peer).stream().noneMatch(x->x instanceof net.minecraft.network.protocol.game.ClientboundSoundPacket s&&s.getSound().value()==HouseReadRegistry.SUPPER.get()),"the other reader receives no private kitchen voice");h.assertTrue(!visit(p,HouseReadSites.Room.PORCH).getBoolean("Written"),"watching the rail through the entire window keeps it plain");p.setYRot(0);p.setXRot(0);
          }).thenWaitUntil(()->h.assertTrue(visit(p,HouseReadSites.Room.PORCH).getBoolean("Written"),"the supplied carving appears only after the actual reader looks away"))
          .thenExecute(()->{
              for(var rel:List.of(new BlockPos(0,3,-24),new BlockPos(-15,0,-8),new BlockPos(14,0,-42)))h.assertTrue(f.data().doorAt(f.out.dimension(),b[0].offset(rel))!=null,"kitchen, side and road each have their own real way on");
              h.assertTrue(!f.out.getBlockState(b[0].offset(HouseReadRooms.SCREEN)).getValue(DoorBlock.OPEN),"the ordinary screen door is a genuine native door");
              var text=String.join(" ",HouseReadTexts.book(true).get(DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().map(page->page.raw().getString()).toList());h.assertTrue(text.equals(HouseReadTexts.PORCH.replace("\n\n"," ")),"all supplied rail prose survives native pagination verbatim");
              f.reload();h.assertTrue(visit(p,HouseReadSites.Room.PORCH).getInt("Age")>=435&&visit(p,HouseReadSites.Room.PORCH).getBoolean("Written"),"the quiet timer and same original rail state survive saved-data reload");
              var entry=f.data().door("house_read/"+p.getUUID()+"/porch/entry");stand(p,f.out,b[0],.5,0,11.5);click(p,entry.lower);
          }).thenWaitUntil(()->h.assertTrue(HouseReadSites.inside(p)==null&&!LabyrinthDoors.isBusy(p),"doing nothing still permits immediate ordinary retreat through the actual entry"))
          .thenExecute(()->h.assertTrue(p.getHealth()==20&&f.data().returnDepth(p.getUUID())==0,"there is no punishment or story credit for this quiet moment")).thenSucceed();
    }
}
