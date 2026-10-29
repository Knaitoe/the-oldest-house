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
    @AfterBatch(batch="read_waiting") public static void waitingDone(ServerLevel ignored){close();}
    @AfterBatch(batch="read_routine") public static void routineDone(ServerLevel ignored){close();}
    @AfterBatch(batch="read_light") public static void lightDone(ServerLevel ignored){close();}
    @AfterBatch(batch="read_shelter") public static void shelterDone(ServerLevel ignored){close();}
    private static void close(){HouseReadSites.clear();if(active!=null){active.close();active=null;}}
    private static LiteraryTests.Fixture fixture(GameTestHelper h){active=new LiteraryTests.Fixture(h);return active;}
    private static ServerPlayer player(GameTestHelper h,LiteraryTests.Fixture f,String name){var p=NativeTestPlayers.survival(h,name);f.players.add(p);io.github.knaitoe.theoldesthouse.opening.OpeningSequence.state(p).markEntered();p.setNoGravity(true);return p;}
    private static java.util.List<net.minecraft.network.protocol.Packet<?>> packets(ServerPlayer p){var out=new java.util.ArrayList<net.minecraft.network.protocol.Packet<?>>();var queue=new java.util.ArrayDeque<net.minecraft.network.protocol.Packet<?>>(NativeTestPlayers.packets(p));while(!queue.isEmpty()){var packet=queue.removeFirst();if(packet instanceof net.minecraft.network.protocol.BundlePacket<?> bundle){for(var child:bundle.subPackets())queue.add(child);}else out.add(packet);}return out;}
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
              b[0]=HouseReadSites.base(p.server,p.getUUID(),HouseReadSites.Room.COATROOM);HouseReadAudit.check(h,f.in,b[0],HouseReadSites.Room.COATROOM);seed(p,HouseReadSites.Room.COATROOM,from[0]);stand(p,f.in,b[0],.5,0,.5);stand(peer,f.in,b[0],2.5,0,.5);
              for(int i=0;i<36;i++)p.getInventory().setItem(i,new ItemStack(Items.COBBLESTONE,64));gear[0]=new ItemStack(Items.DIAMOND_CHESTPLATE);gear[0].set(DataComponents.CUSTOM_NAME,Component.literal("Original coat armour"));gear[0].setDamageValue(37);gear[1]=new ItemStack(Items.DIAMOND_BOOTS);gear[1].setDamageValue(19);p.setItemSlot(EquipmentSlot.CHEST,gear[0].copy());p.setItemSlot(EquipmentSlot.FEET,gear[1].copy());
              p.lookAt(EntityAnchorArgument.Anchor.EYES,b[0].offset(HouseReadRooms.UMBRELLA_AT).getCenter());
              var peg=b[0].offset(HouseReadRooms.PEGS.get(0));var part=f.in.getBlockState(peg).getValue(HouseReadPropBlock.PART);HouseReadSites.use(peer,peg,part);h.assertTrue(peer.getItemBySlot(EquipmentSlot.CHEST).isEmpty(),"an observer cannot borrow another reader's clothes");
              click(p,peg);var boots=peg.east();click(p,boots);h.assertTrue(HouseReadSites.loan(p.getItemBySlot(EquipmentSlot.CHEST),p.getUUID())&&HouseReadSites.loan(p.getItemBySlot(EquipmentSlot.FEET),p.getUUID()),"actual block use equips both original room loans even with a full pack");
          }).thenIdle(6).thenExecute(()->{
              h.assertTrue(p.getTicksFrozen()==0&&!visit(p,HouseReadSites.Room.COATROOM).getBoolean("Written"),"clothing lifts the harmless frost and a watched slip stays blank");p.setYRot(180);p.setXRot(0);
          }).thenWaitUntil(()->h.assertTrue(visit(p,HouseReadSites.Room.COATROOM).getBoolean("Written"),"the slip acquires writing only after looking away"))
          .thenExecute(()->{
              stand(p,f.in,b[0],2.5,0,.5);click(p,b[0].offset(HouseReadRooms.UMBRELLA_AT));h.assertTrue(p.containerMenu instanceof LecternMenu&&!p.containerMenu.clickMenuButton(p,3),"the note opens actual native pages and cannot be printed or taken");p.closeContainer();
              // 0.4.150: two seconds at the glass in the borrowed coat, looking into it, is kept.
              stand(p,f.in,b[0],2.5,0,-5.5);p.lookAt(EntityAnchorArgument.Anchor.EYES,b[0].offset(HouseReadRooms.MIRROR).above().getCenter());
          }).thenIdle(45).thenExecute(()->{
              var read=HouseRead.raw(f.data(),p.getUUID(),HouseRead.Trait.SELF);var cloth=((HouseReadRegistry.Garment)p.getItemBySlot(EquipmentSlot.CHEST).getItem()).cloth;h.assertTrue(read.strength()==HouseRead.Strength.SETTLED&&read.value().equals(switch(cloth){case "wool"->"MASC";case "trench"->"FEM";default->"ANDROGYNOUS";}),"the first coat sets the presentation and two seconds at the glass in it settle it: "+read);
              h.assertTrue(HouseReadLook.of(p).coat().equals(cloth)&&!HouseReadLook.of(p).unseen()&&HouseReadLook.of(peer).coat().isEmpty(),"the House's version of the reader wears the coat they took, and nobody else's");
              var seen=visit(p,HouseReadSites.Room.COATROOM);h.assertTrue(seen.getBoolean("AtMirror")&&seen.getList("Actions",net.minecraft.nbt.Tag.TAG_STRING).stream().anyMatch(t->t.getAsString().equals("stood at the hall glass in the borrowed coat")),"standing at the glass in the borrowed coat is kept");
              var glass=b[0].offset(HouseReadRooms.MIRROR);stand(p,f.in,b[0],2.5,0,-6.5);click(p,glass);h.assertTrue(f.in.getBlockState(glass).getValue(HouseReadPropBlock.TURNED),"native glass use turns the actual mirror to its wooden back");click(p,b[0].offset(HouseReadRooms.TOWEL_AT));h.assertTrue(f.in.getBlockState(glass).getValue(HouseReadPropBlock.PART)==HouseReadPropBlock.Part.COVERED_MIRROR&&f.in.getBlockState(b[0].offset(HouseReadRooms.TOWEL_AT)).getValue(HouseReadPropBlock.TURNED),"the towel from the bar beside it physically covers the same glass");click(p,glass);h.assertTrue(f.in.getBlockState(glass).getValue(HouseReadPropBlock.PART)==HouseReadPropBlock.Part.MIRROR,"the same mirror can be uncovered again");
              f.reload();h.assertTrue(visit(p,HouseReadSites.Room.COATROOM).getBoolean("CapturedChest")&&visit(p,HouseReadSites.Room.COATROOM).getBoolean("Written"),"native saved-data reload preserves gear custody and the unseen writing");
              HouseReadExport.save(f.in,b[0],HouseReadSites.Room.COATROOM,"coatroom");var entry=LabyrinthData.get(p.server).door("house_read/"+p.getUUID()+"/coatroom/entry");stand(p,f.in,b[0],.5,0,1);click(p,entry.lower);
          }).thenWaitUntil(()->h.assertTrue(HouseReadSites.inside(p)==null&&!LabyrinthDoors.isBusy(p),"the actual original door permits immediate retreat"))
          .thenExecute(()->{
              h.assertTrue(ItemStack.matches(gear[0],p.getItemBySlot(EquipmentSlot.CHEST))&&ItemStack.matches(gear[1],p.getItemBySlot(EquipmentSlot.FEET)),"the exact original gear, names and damage return to their original slots");for(int i=0;i<36;i++)h.assertTrue(p.getInventory().getItem(i).is(Items.COBBLESTONE)&&p.getInventory().getItem(i).getCount()==64,"a full original pack is unchanged");
              h.assertTrue(f.data().returnDepth(p.getUUID())==0&&p.getHealth()==20,"a quiet retreat consumes no story return, health or reward");
          }).thenSucceed();
    }

    @GameTest(template="empty",batch="read_route",timeoutTicks=6000)
    public static void savedInterleavedVisitsStillRejoinTheSameOrdinaryHallIndependently(GameTestHelper h){
        var f=fixture(h);var a=player(h,f,"quiet_a");var b=player(h,f,"quiet_b");var at=f.origin.offset(0,0,-5);var target=f.origin.offset(0,0,-45);f.chunks.hold(f.in,new AABB(at).inflate(6));f.chunks.hold(f.in,new AABB(target).inflate(7));LabyrinthData.Door[] from={null};BlockPos[] ab={null},bb={null};h.onEachTick(()->{transition(a);transition(b);});
        var notEntered=NativeTestPlayers.survival(h,"quiet_before_opening");f.players.add(notEntered);HouseReadSites.prepareNext(notEntered);h.assertTrue(!HouseReadSites.state(notEntered.server,notEntered.getUUID()).contains("Index"),"preparing an ordinary arrival before the opening cannot allocate or queue a quiet room");
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"both native shared doorways are resident")).thenExecute(()->{
            from[0]=source(f);for(var pos:BlockPos.betweenClosed(target.offset(-4,-1,-5),target.offset(4,-1,5)))f.in.setBlock(pos,Blocks.OAK_PLANKS.defaultBlockState(),18);LabyrinthBuilder.buildCorridor(f.in,target);LabyrinthBuilder.registerDoors(f.data(),LabyrinthPlace.GRAY_CORRIDOR,target);
            for(var p:List.of(a,b)){stand(p,f.in,at,.5,0,2);f.data().deal(p.getUUID(),from[0],LabyrinthPlace.GRAY_CORRIDOR.id(),false);HouseReadSites.prepare(p,HouseReadSites.Room.WAITING);}
          }).thenWaitUntil(()->h.assertTrue(ready(a,HouseReadSites.Room.WAITING),"the first saved private site is resident")).thenExecute(()->{seed(a,HouseReadSites.Room.WAITING,from[0]);stand(a,f.in,HouseReadSites.base(a.server,a.getUUID(),HouseReadSites.Room.WAITING),.5,0,.5);}).thenWaitUntil(()->h.assertTrue(ready(b,HouseReadSites.Room.WAITING),"the second saved private site is resident")).thenExecute(()->{seed(b,HouseReadSites.Room.WAITING,from[0]);stand(b,f.in,HouseReadSites.base(b.server,b.getUUID(),HouseReadSites.Room.WAITING),.5,0,.5);}).thenWaitUntil(()->{boolean present=HouseReadSites.inside(a)==HouseReadSites.Room.WAITING&&HouseReadSites.inside(b)==HouseReadSites.Room.WAITING&&!LabyrinthDoors.isBusy(a)&&!LabyrinthDoors.isBusy(b);if(!present)java.util.concurrent.locks.LockSupport.parkNanos(2_000_000L);h.assertTrue(present,"the same original door gives both actual native players a complete private copy");})
          .thenExecute(()->{ab[0]=HouseReadSites.base(a.server,a.getUUID(),HouseReadSites.Room.WAITING);bb[0]=HouseReadSites.base(b.server,b.getUUID(),HouseReadSites.Room.WAITING);h.assertTrue(!ab[0].equals(bb[0]),"the private copies are distinct physical sites");})
          .thenWaitUntil(()->h.assertTrue(HouseReadMoments.canProceed(a,HouseReadSites.Room.WAITING),"the first reader's fifteen-second occupied wait finishes without interacting"))
          .thenExecute(()->{h.assertTrue(packets(a).stream().anyMatch(x->x instanceof net.minecraft.network.protocol.game.ClientboundSystemChatPacket chat&&chat.content().getString().contains("Sorry for keeping you waiting")),"the receptionist calls the reader through");stand(a,f.in,ab[0],.5,0,-10.5);click(a,ab[0].offset(HouseReadMoments.exit(HouseReadSites.Room.WAITING)));})
          .thenWaitUntil(()->h.assertTrue(HouseReadSites.inside(a)==null&&!LabyrinthDoors.isBusy(a)&&a.distanceToSqr(target.getCenter())<25,"the first reader goes through the office door into the original ordinary hall"))
          .thenWaitUntil(()->h.assertTrue(HouseReadSites.inside(b)==null&&!LabyrinthDoors.isBusy(b)&&b.position().distanceToSqr(a.position())<.01,"the second reader, doing nothing, is taken on by the House into that same shared hall"))
          .thenExecute(()->{
              h.assertTrue(f.data().returnDepth(a.getUUID())==1&&f.data().returnDepth(b.getUUID())==1,"each route contains exactly the genuine original forward crossing");var back=new LabyrinthData.Waypoint(from[0].dimension,Vec3.atBottomCenterOf(from[0].lower),from[0].facing.toYRot(),true);var originalKey=f.data().nodeKey(a.getUUID(),LabyrinthPlace.GRAY_CORRIDOR);h.assertTrue(f.data().node(a.getUUID(),originalKey)!=null,"the original destination has the ordinary native remembered map");
              h.assertTrue(HouseReadSites.next(a.server,a.getUUID())==HouseReadSites.Room.BATHROOM&&WitnessAccount.Story.of(LabyrinthPlace.READ_COATROOM.id())==null&&WitnessAccount.Story.of(LabyrinthPlace.READ_PORCH.id())==null,"the next quiet moment is the bathroom, and the quiet rooms are not Witness sources");
              h.assertTrue(HouseReadSites.state(b.server,b.getUUID()).getCompound("waiting").getInt("Age")<HouseReadMoments.WAIT+60,"the House does not keep a reader waiting long past the call");
          }).thenSucceed();
    }

    @GameTest(template="empty",batch="read_porch",timeoutTicks=5000)
    public static void porchWhistleVoiceAndAllQuietWaysOnRemainPersonalAndTheRailWritesUnseen(GameTestHelper h){
        var f=fixture(h);var p=player(h,f,"porch_reader");var peer=player(h,f,"porch_peer");var at=f.origin.offset(0,0,-5);f.chunks.hold(f.in,new AABB(at).inflate(6));BlockPos[] b={null};LabyrinthData.Door[] from={null};h.onEachTick(()->transition(p));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the original House door is loaded")).thenExecute(()->{from[0]=source(f);stand(p,f.in,at,.5,0,2);stand(peer,f.in,at,2.5,0,2);HouseReadSites.prepare(p,HouseReadSites.Room.PORCH);})
          .thenWaitUntil(()->h.assertTrue(ready(p,HouseReadSites.Room.PORCH),"the whole bounded private yard is complete"))
          .thenExecute(()->{b[0]=HouseReadSites.base(p.server,p.getUUID(),HouseReadSites.Room.PORCH);HouseReadAudit.check(h,f.out,b[0],HouseReadSites.Room.PORCH);seed(p,HouseReadSites.Room.PORCH,from[0]);stand(p,f.out,b[0],.5,0,8.5);p.lookAt(EntityAnchorArgument.Anchor.EYES,b[0].offset(HouseReadRooms.RAIL_AT).getCenter());HouseReadExport.save(f.out,b[0],HouseReadSites.Room.PORCH,"porch");})
          .thenWaitUntil(()->h.assertTrue(visit(p,HouseReadSites.Room.PORCH).getInt("Age")>=435,"the actual ten-second whistle and quiet observation window finish"))
          .thenExecute(()->{
              var packets=packets(p);h.assertTrue(packets.stream().anyMatch(x->x instanceof net.minecraft.network.protocol.game.ClientboundSoundPacket s&&s.getSound().value()==HouseReadRegistry.WHISTLE.get())&&packets.stream().anyMatch(x->x instanceof net.minecraft.network.protocol.game.ClientboundSoundPacket s&&s.getSound().value()==HouseReadRegistry.SUPPER.get()),"the actual reader receives both original positional supper cues");
              h.assertTrue(packets(peer).stream().noneMatch(x->x instanceof net.minecraft.network.protocol.game.ClientboundSoundPacket s&&s.getSound().value()==HouseReadRegistry.SUPPER.get()),"the other reader receives no private kitchen voice");h.assertTrue(!visit(p,HouseReadSites.Room.PORCH).getBoolean("Written"),"watching the rail through the entire window keeps it plain");p.setYRot(0);p.setXRot(0);
          }).thenWaitUntil(()->h.assertTrue(visit(p,HouseReadSites.Room.PORCH).getBoolean("Written"),"the supplied carving appears only after the actual reader looks away"))
          .thenExecute(()->{
              for(var rel:List.of(new BlockPos(0,3,-24),new BlockPos(-15,0,-8),new BlockPos(14,0,-42)))h.assertTrue(f.data().doorAt(f.out.dimension(),b[0].offset(rel))!=null,"kitchen, side and road each have their own real way on");
              h.assertTrue(!f.out.getBlockState(b[0].offset(HouseReadRooms.SCREEN)).getValue(DoorBlock.OPEN),"the ordinary screen door is a genuine native door");
              var text=String.join(" ",HouseReadTexts.book(true).get(DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().map(page->page.raw().getString()).toList());h.assertTrue(text.equals(HouseReadTexts.PORCH.replace("\n\n"," ")),"all supplied rail prose survives native pagination verbatim");
              // The rail sits in front of the lit front window: looking through its gap at the window from the lawn, and never going in, is the design's DANGER.
              h.assertTrue(HouseRead.raw(f.data(),p.getUUID(),HouseRead.Trait.HOME).equals(new HouseRead.Category("DANGER",HouseRead.Strength.TENTATIVE))&&HouseRead.seen(f.data(),p.getUUID()).getCompound("porch").getString("First").equals("window"),"looking in at the lit window from the lawn and staying out reads danger, tentatively: "+HouseRead.raw(f.data(),p.getUUID(),HouseRead.Trait.HOME));
              f.reload();h.assertTrue(visit(p,HouseReadSites.Room.PORCH).getInt("Age")>=435&&visit(p,HouseReadSites.Room.PORCH).getBoolean("Written"),"the quiet timer and same original rail state survive saved-data reload");
              var entry=f.data().door("house_read/"+p.getUUID()+"/porch/entry");stand(p,f.out,b[0],.5,0,11.5);click(p,entry.lower);
          }).thenWaitUntil(()->h.assertTrue(HouseReadSites.inside(p)==null&&!LabyrinthDoors.isBusy(p),"doing nothing still permits immediate ordinary retreat through the actual entry"))
          .thenExecute(()->h.assertTrue(p.getHealth()==20&&f.data().returnDepth(p.getUUID())==0,"there is no punishment or story credit for this quiet moment")).thenSucceed();
    }
    @GameTest(template="empty",batch="read_waiting",timeoutTicks=5000)
    public static void waitingObjectsArePrivateTheBellNeverShortensTheWaitAndAllBorrowedOriginalsReturn(GameTestHelper h){
        var f=fixture(h);var p=player(h,f,"waiting_reader");var peer=player(h,f,"waiting_peer");var at=f.origin.offset(0,0,-5);f.chunks.hold(f.in,new AABB(at).inflate(8));BlockPos[] b={null},coldCoat={null};LabyrinthData.Door[] from={null};int[] paused={0};ItemStack[] head={null},extra={null};h.onEachTick(()->transition(p));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"source loaded")).thenExecute(()->{from[0]=source(f);var privateState=HouseReadSites.state(p.server,p.getUUID());privateState.putInt("Index",1390);f.data().setStateEntry(HouseReadSites.STATE,p.getUUID().toString(),privateState);coldCoat[0]=HouseReadSites.base(p.server,p.getUUID(),HouseReadSites.Room.COATROOM);h.assertTrue(f.in.getChunkSource().getChunkNow(coldCoat[0].getX()>>4,coldCoat[0].getZ()>>4)==null,"the unrelated private coatroom begins genuinely unloaded");stand(p,f.in,at,.5,0,2);stand(peer,f.in,at,2.5,0,2);HouseReadSites.prepare(p,HouseReadSites.Room.WAITING);})
          .thenWaitUntil(()->h.assertTrue(ready(p,HouseReadSites.Room.WAITING),"the actual waiting-room copy finishes"))
          .thenExecute(()->{
              b[0]=HouseReadSites.base(p.server,p.getUUID(),HouseReadSites.Room.WAITING);HouseReadAudit.check(h,f.in,b[0],HouseReadSites.Room.WAITING);seed(p,HouseReadSites.Room.WAITING,from[0]);stand(p,f.in,b[0],-2.5,0,-9.5);click(p,b[0].offset(HouseReadRemainingRooms.BELL_AT));h.assertTrue(!HouseReadMoments.canProceed(p,HouseReadSites.Room.WAITING)&&!f.in.getBlockState(b[0].offset(HouseReadMoments.exit(HouseReadSites.Room.WAITING))).getValue(DoorBlock.OPEN),"a real bell click neither unlocks nor shortens the wait");
              for(int i=0;i<36;i++)p.getInventory().setItem(i,new ItemStack(Items.COBBLESTONE,64));head[0]=new ItemStack(Items.DIAMOND_HELMET);head[0].setDamageValue(41);head[0].set(DataComponents.CUSTOM_NAME,Component.literal("Original waiting hat"));p.setItemSlot(EquipmentSlot.HEAD,head[0].copy());
              stand(p,f.in,b[0],-3.5,0,-3);p.lookAt(EntityAnchorArgument.Anchor.EYES,b[0].offset(HouseReadRemainingRooms.CALENDAR_AT).getCenter());click(p,b[0].offset(HouseReadRemainingRooms.GLASSES_AT));h.assertTrue(HouseReadSites.loan(p.getItemBySlot(EquipmentSlot.HEAD),p.getUUID()),"reading glasses really occupy the head slot with the original safely held");
              h.assertTrue(HouseRead.raw(f.data(),p.getUUID(),HouseRead.Trait.AGE).equals(new HouseRead.Category("ANALOG",HouseRead.Strength.TENTATIVE)),"putting on the reading glasses first reads the reader as older, tentatively");
              click(p,b[0].offset(HouseReadRemainingRooms.PAPER_AT));p.closeContainer();stand(p,f.in,b[0],-3.5,0,-1.5);click(p,b[0].offset(-6,0,-2));h.assertTrue(p.isPassenger()&&p.getMainHandItem().is(HouseReadRegistry.PAPER.get()),"a reader can actually sit with the borrowed newspaper still in hand");
              stand(p,f.in,b[0],4.5,0,-2);click(p,b[0].offset(HouseReadRemainingRooms.PHONE_AT));h.assertTrue(p.getMainHandItem().is(HouseReadRegistry.PHONE.get()),"the actual phone is borrowed into the original hand slot with a full pack");
          }).thenIdle(2).thenExecute(()->{
              var phonePackets=packets(p);h.assertTrue(phonePackets.stream().anyMatch(x->x instanceof net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket packet&&packet.payload() instanceof io.github.knaitoe.theoldesthouse.network.HarriganPhonePayload phone&&phone.state().getBoolean("WaitingRoom")),"the existing native phone screen receives only the lock-screen state");
              h.assertTrue(packets(peer).stream().noneMatch(x->x instanceof net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket packet&&packet.payload() instanceof io.github.knaitoe.theoldesthouse.network.HarriganPhonePayload),"a peer receives no private phone data");
              p.drop(false);extra[0]=new ItemStack(Items.APPLE);extra[0].set(DataComponents.CUSTOM_NAME,Component.literal("Original pocket apple"));p.setItemInHand(InteractionHand.MAIN_HAND,extra[0].copy());
              p.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.SPECTATOR);paused[0]=visit(p,HouseReadSites.Room.WAITING).getInt("Age");
          }).thenIdle(25).thenExecute(()->{h.assertTrue(visit(p,HouseReadSites.Room.WAITING).getInt("Age")==paused[0],"an observer cannot advance occupied time");p.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.SURVIVAL);p.setNoGravity(true);f.reload();})
          .thenWaitUntil(()->h.assertTrue(visit(p,HouseReadSites.Room.WAITING).getInt("Age")>=HouseReadMoments.WAIT,"the fifteen-second occupied wait completes after reload"))
          .thenExecute(()->{
              var seen=HouseRead.seen(f.data(),p.getUUID()).getCompound("waiting");h.assertTrue(seen.getString("First").equals("glasses")&&seen.getString("Kept").equals("glasses")&&HouseRead.raw(f.data(),p.getUUID(),HouseRead.Trait.AGE).equals(new HouseRead.Category("ANALOG",HouseRead.Strength.SETTLED)),"still wearing the glasses when the door clicks settles it; the paper, chair and phone after them are only observed");
              for(var rel:List.of(HouseReadRemainingRooms.PAPER_AT,HouseReadRemainingRooms.CRT_AT,HouseReadRemainingRooms.PHONE_AT)){double d=Math.sqrt(rel.getX()*rel.getX()+rel.getZ()*rel.getZ());h.assertTrue(d>5.5&&d<7.5,"the three ways to pass the time stand about seven blocks from the door: "+rel);}
              h.assertTrue(f.in.getBlockState(b[0].offset(HouseReadRemainingRooms.OUTLET_AT)).getValue(HouseReadPropBlock.PART)==HouseReadPropBlock.Part.OUTLET&&f.in.getBlockState(b[0].offset(7,1,-2)).is(HouseReadRegistry.WINDOW.get()),"the phone charges at its outlet and the phone chair has its clear window onto the lot");
              h.assertTrue(f.in.getBlockState(b[0].offset(HouseReadMoments.exit(HouseReadSites.Room.WAITING))).getValue(DoorBlock.OPEN),"the actual exit opens regardless of what was handled");HouseReadExport.save(f.in,b[0],HouseReadSites.Room.WAITING,"waiting_room");stand(p,f.in,b[0],.5,0,1);click(p,b[0].offset(HouseReadRooms.COAT_ENTRY));
          }).thenWaitUntil(()->h.assertTrue(HouseReadSites.inside(p)==null&&!LabyrinthDoors.isBusy(p),"native original entry still permits retreat"))
          .thenExecute(()->{h.assertTrue(f.in.getChunkSource().getChunkNow(coldCoat[0].getX()>>4,coldCoat[0].getZ()>>4)==null,"returning borrowed waiting-room objects must not load the unrelated coatroom");h.assertTrue(ItemStack.matches(head[0],p.getItemBySlot(EquipmentSlot.HEAD)),"the original named/damaged helmet returns exactly");for(int i=0;i<36;i++)h.assertTrue(p.getInventory().getItem(i).is(Items.COBBLESTONE)&&p.getInventory().getItem(i).getCount()==64,"the full original pack returns intact");h.assertTrue(f.in.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,p.getBoundingBox().inflate(5),e->ItemStack.matches(extra[0],e.getItem())).size()==1,"an item put into the borrowed hand is preserved exactly once even when the original pack is full");h.assertTrue(f.data().returnDepth(p.getUUID())==0,"quiet waiting gives no story return or reward");}).thenSucceed();
    }

    @GameTest(template="empty",batch="read_routine",timeoutTicks=5000)
    public static void oneRoutineItemAtATimeIsPickedUpAndUsedAtTheMirrorWhichEndsTheBathroom(GameTestHelper h){
        var f=fixture(h);var p=player(h,f,"routine_reader");var at=f.origin.offset(0,0,-5);var target=f.origin.offset(0,0,-45);f.chunks.hold(f.in,new AABB(at).inflate(8));f.chunks.hold(f.in,new AABB(target).inflate(7));BlockPos[] b={null};LabyrinthData.Door[] from={null};int[] first={-1},second={-1};ItemStack[] own={null};h.onEachTick(()->transition(p));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"source loaded")).thenExecute(()->{from[0]=source(f);for(var pos:BlockPos.betweenClosed(target.offset(-4,-1,-5),target.offset(4,-1,5)))f.in.setBlock(pos,Blocks.OAK_PLANKS.defaultBlockState(),18);LabyrinthBuilder.buildCorridor(f.in,target);LabyrinthBuilder.registerDoors(f.data(),LabyrinthPlace.GRAY_CORRIDOR,target);stand(p,f.in,at,.5,0,2);HouseReadSites.prepare(p,HouseReadSites.Room.BATHROOM);})
          .thenWaitUntil(()->h.assertTrue(ready(p,HouseReadSites.Room.BATHROOM),"the actual bathroom finishes"))
          .thenExecute(()->{
              b[0]=HouseReadSites.base(p.server,p.getUUID(),HouseReadSites.Room.BATHROOM);HouseReadAudit.check(h,f.in,b[0],HouseReadSites.Room.BATHROOM);seed(p,HouseReadSites.Room.BATHROOM,from[0]);stand(p,f.in,b[0],.5,0,-7.5);
              var kinds=new HashSet<HouseReadPropBlock.Part>();double near=99,far=0;for(var rel:HouseReadRemainingRooms.ROUTINE_PLACES){double d=Math.sqrt(rel.getX()*rel.getX()+(rel.getZ()-2)*(rel.getZ()-2));near=Math.min(near,d);far=Math.max(far,d);h.assertTrue(rel.getY()==1,"every routine sits on the vanity counter");kinds.add(f.in.getBlockState(b[0].offset(rel)).getValue(HouseReadPropBlock.PART));}
              h.assertTrue(kinds.size()==4&&far-near<.6,"four distinct shuffled routines, all about the same distance from the door");
              own[0]=new ItemStack(Items.COMPASS);own[0].set(DataComponents.CUSTOM_NAME,Component.literal("Original pocket compass"));p.setItemInHand(InteractionHand.MAIN_HAND,own[0].copy());
              // Pick up one item, then change your mind to an item from another set: the first goes back.
              var a=b[0].offset(HouseReadRemainingRooms.ROUTINE_PLACES.get(0));int ga=f.in.getBlockState(a).getValue(HouseReadPropBlock.PART).ordinal()-HouseReadPropBlock.Part.ROUTINE_A.ordinal();
              p.gameMode.useItemOn(p,p.serverLevel(),p.getMainHandItem(),InteractionHand.MAIN_HAND,new BlockHitResult(new Vec3(a.getX()+3/16.0,a.getY()+.15,a.getZ()+.5),Direction.UP,a,false));first[0]=ga*3;
              h.assertTrue(p.getMainHandItem().is(HouseReadRegistry.ROUTINES.get(first[0]).get())&&HouseReadSites.loan(p.getMainHandItem(),p.getUUID())&&f.in.getBlockState(a).getValue(HouseReadPropBlock.LIFTED)==1,"the chosen item comes into the hand and leaves its place on the counter");
              h.assertTrue(HouseRead.raw(f.data(),p.getUUID(),HouseRead.Trait.HAIR).equals(new HouseRead.Category(HouseReadMoments.HAIR[ga],HouseRead.Strength.TENTATIVE)),"the first item sets the routine, tentatively");
              var c=b[0].offset(HouseReadRemainingRooms.ROUTINE_PLACES.get(2));int gc=f.in.getBlockState(c).getValue(HouseReadPropBlock.PART).ordinal()-HouseReadPropBlock.Part.ROUTINE_A.ordinal();
              p.gameMode.useItemOn(p,p.serverLevel(),p.getMainHandItem(),InteractionHand.MAIN_HAND,new BlockHitResult(new Vec3(c.getX()+8/16.0,c.getY()+.15,c.getZ()+.5),Direction.UP,c,false));second[0]=gc*3+1;
              h.assertTrue(p.getMainHandItem().is(HouseReadRegistry.ROUTINES.get(second[0]).get())&&f.in.getBlockState(a).getValue(HouseReadPropBlock.LIFTED)==0&&f.in.getBlockState(c).getValue(HouseReadPropBlock.LIFTED)==2,"picking another puts the first back: only one is ever in the hand");
              h.assertTrue(p.getInventory().items.stream().filter(i->i.getItem()!=Items.AIR&&HouseReadSites.loan(i,p.getUUID())).count()==1,"never more than one routine item is lent");
              h.assertTrue(HouseRead.raw(f.data(),p.getUUID(),HouseRead.Trait.HAIR).strength()==HouseRead.Strength.UNCERTAIN,"changing one's mind to another set makes the read uncertain");
              // Use it at the mirror: the routine is done, and it ends the bathroom.
              click(p,b[0].offset(HouseReadRemainingRooms.BATH_MIRROR));
              h.assertTrue(HouseRead.raw(f.data(),p.getUUID(),HouseRead.Trait.HAIR).equals(new HouseRead.Category(HouseReadMoments.HAIR[gc],HouseRead.Strength.SETTLED))&&HouseRead.seen(f.data(),p.getUUID()).getCompound("bathroom").getInt("Used")==second[0],"using the item in hand at the mirror decides the routine");
              var d=b[0].offset(HouseReadRemainingRooms.ROUTINE_PLACES.get(1));p.gameMode.useItemOn(p,p.serverLevel(),p.getMainHandItem(),InteractionHand.MAIN_HAND,new BlockHitResult(new Vec3(d.getX()+.5,d.getY()+.15,d.getZ()+.5),Direction.UP,d,false));
              h.assertTrue(p.getMainHandItem().is(HouseReadRegistry.ROUTINES.get(second[0]).get())&&f.in.getBlockState(d).getValue(HouseReadPropBlock.LIFTED)==0,"after the routine nothing else on the counter can be played with");
              HouseReadExport.save(f.in,b[0],HouseReadSites.Room.BATHROOM,"bathroom_before_bed");
          })
          .thenWaitUntil(()->h.assertTrue(visit(p,HouseReadSites.Room.BATHROOM).getBoolean("LightOff")&&HouseReadMoments.canProceed(p,HouseReadSites.Room.BATHROOM),"the light goes out by itself and the bedroom door opens"))
          .thenExecute(()->{for(var rel:HouseReadRemainingRooms.BATH_LIGHTS)h.assertTrue(f.in.getBlockState(b[0].offset(rel)).getValue(HouseReadPropBlock.TURNED),"both actual lights are off: "+rel);
              h.assertTrue(HouseReadProse.bathroom(HouseRead.seen(f.data(),p.getUUID()).getCompound("bathroom")).startsWith("reached for the "+HouseReadMoments.ROUTINES[first[0]]+", then "),"the account says where the hand went first and what was done: "+HouseReadProse.bathroom(HouseRead.seen(f.data(),p.getUUID()).getCompound("bathroom")));})
          .thenWaitUntil(()->h.assertTrue(HouseReadSites.inside(p)==null&&!LabyrinthDoors.isBusy(p),"in the dark the House takes the reader on"))
          .thenExecute(()->{h.assertTrue(ItemStack.matches(own[0],p.getMainHandItem())&&p.getInventory().items.stream().noneMatch(i->HouseReadSites.loan(i,p.getUUID())),"the reader's own item is back in their hand and no routine item leaves the bathroom");
              for(var rel:HouseReadRemainingRooms.ROUTINE_PLACES)h.assertTrue(f.in.getBlockState(b[0].offset(rel)).getValue(HouseReadPropBlock.LIFTED)==0,"every routine is back in its place: "+rel);})
          .thenSucceed();
    }

    @GameTest(template="empty",batch="read_light",timeoutTicks=4000)
    public static void theBedroomOpensWithoutTouchingAnyRoutineAndThePostcardNeverChangesInView(GameTestHelper h){
        var f=fixture(h);var p=player(h,f,"night_reader");var at=f.origin.offset(0,0,-5);f.chunks.hold(f.in,new AABB(at).inflate(8));BlockPos[] b={null};LabyrinthData.Door[] from={null};h.onEachTick(()->transition(p));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"source loaded")).thenExecute(()->{from[0]=source(f);stand(p,f.in,at,.5,0,2);HouseReadSites.prepare(p,HouseReadSites.Room.BATHROOM);})
          .thenWaitUntil(()->h.assertTrue(ready(p,HouseReadSites.Room.BATHROOM),"bathroom ready"))
          .thenExecute(()->{b[0]=HouseReadSites.base(p.server,p.getUUID(),HouseReadSites.Room.BATHROOM);seed(p,HouseReadSites.Room.BATHROOM,from[0]);stand(p,f.in,b[0],.5,0,-.5);p.lookAt(EntityAnchorArgument.Anchor.EYES,b[0].offset(HouseReadRemainingRooms.POSTCARD_AT).getCenter());click(p,b[0].offset(HouseReadRemainingRooms.SWITCH_AT));h.assertTrue(visit(p,HouseReadSites.Room.BATHROOM).getInt("Touches")==0&&HouseReadMoments.canProceed(p,HouseReadSites.Room.BATHROOM),"the very first switch use opens the way with no grooming requirement");})
          .thenIdle(8).thenExecute(()->{h.assertTrue(HouseRead.seen(f.data(),p.getUUID()).getCompound("bathroom").getBoolean("Untouched")&&HouseRead.raw(f.data(),p.getUUID(),HouseRead.Trait.HAIR).strength()==HouseRead.Strength.UNKNOWN,"the light turned off without touching anything is kept, and reads no routine");h.assertTrue(!visit(p,HouseReadSites.Room.BATHROOM).getBoolean("Written"),"the watched lake postcard remains unchanged");p.setYRot(0);p.setXRot(0);})
          .thenWaitUntil(()->h.assertTrue(visit(p,HouseReadSites.Room.BATHROOM).getBoolean("Written"),"the original postcard gains its writing only unseen"))
          .thenExecute(()->{stand(p,f.in,b[0],.5,0,-6.5);click(p,b[0].offset(HouseReadRemainingRooms.POSTCARD_AT));h.assertTrue(p.containerMenu instanceof LecternMenu&&!p.containerMenu.clickMenuButton(p,3),"the original postcard has actual read-only pages");p.closeContainer();for(var room:HouseReadSites.Room.values()){var words=String.join(" ",HouseReadTexts.book(room).get(DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().map(page->page.raw().getString()).toList());h.assertTrue(words.equals(HouseReadTexts.text(room).replace("\n\n"," ")),"all five owner's passages remain verbatim after native pagination");h.assertTrue(WitnessAccount.Story.of(room.place().id())==null,"a quiet first room is never an extra Witness source");}stand(p,f.in,b[0],.5,0,1);click(p,b[0].offset(HouseReadRooms.COAT_ENTRY));})
          .thenWaitUntil(()->h.assertTrue(HouseReadSites.inside(p)==null&&!LabyrinthDoors.isBusy(p),"untouched bathroom still permits native retreat"))
          .thenExecute(()->h.assertTrue(p.getHealth()==20&&f.data().returnDepth(p.getUUID())==0,"no wrong answer, health charge or story credit")).thenSucceed();
    }

    @GameTest(template="empty",batch="read_shelter",timeoutTicks=6000)
    public static void theTwoOriginalStrangersKeepTheirPrivacyAndDistanceThroughTheWholeSlowSong(GameTestHelper h){
        var f=fixture(h);var p=player(h,f,"shelter_reader");var peer=player(h,f,"shelter_peer");var at=f.origin.offset(0,0,-5);f.chunks.hold(f.in,new AABB(at).inflate(8));BlockPos[] b={null};LabyrinthData.Door[] from={null};UUID[] cast={null,null};double[] start={0,0};h.onEachTick(()->transition(p));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"source loaded")).thenExecute(()->{from[0]=source(f);stand(p,f.in,at,.5,0,2);stand(peer,f.in,at,2.5,0,2);HouseReadSites.prepare(p,HouseReadSites.Room.SHELTER);})
          .thenWaitUntil(()->h.assertTrue(ready(p,HouseReadSites.Room.SHELTER),"the complete dry pavilion and lake set finish"))
          .thenExecute(()->{b[0]=HouseReadSites.base(p.server,p.getUUID(),HouseReadSites.Room.SHELTER);f.chunks.hold(f.in,HouseReadRooms.bounds(b[0],HouseReadSites.Room.SHELTER));HouseReadAudit.check(h,f.in,b[0],HouseReadSites.Room.SHELTER);seed(p,HouseReadSites.Room.SHELTER,from[0]);stand(p,f.in,b[0],.5,0,-3);h.assertTrue(HouseReadMoments.canProceed(p,HouseReadSites.Room.SHELTER),"the way on is already open before the music");})
          .thenWaitUntil(()->h.assertTrue(visit(p,HouseReadSites.Room.SHELTER).hasUUID("Man")&&visit(p,HouseReadSites.Room.SHELTER).hasUUID("Woman")&&f.in.getEntity(visit(p,HouseReadSites.Room.SHELTER).getUUID("Man")) instanceof LiteraryActor&&f.in.getEntity(visit(p,HouseReadSites.Room.SHELTER).getUUID("Woman")) instanceof LiteraryActor,"both owner-bound originals exist"))
          .thenExecute(()->{
              var t=visit(p,HouseReadSites.Room.SHELTER);cast[0]=t.getUUID("Man");cast[1]=t.getUUID("Woman");for(var id:cast){var a=(LiteraryActor)f.in.getEntity(id);f.extras.add(a);h.assertTrue(a.owner().filter(p.getUUID()::equals).isPresent()&&!a.broadcastToPlayer(peer),"a peer cannot track or pick the reader's private stranger");}
              var man=f.in.getEntity(cast[0]);var woman=f.in.getEntity(cast[1]);double mid=b[0].getX()+.5;h.assertTrue(Math.abs((mid-man.getX())-(woman.getX()-mid))<.01&&man.getZ()==woman.getZ()&&mid-man.getX()>9&&woman.getX()-mid>9,"the two strangers are mirror images on the west and east rails");stand(p,f.in,b[0],-7,0,-8.5);
          }).thenIdle(70).thenExecute(()->{
              h.assertTrue(((LiteraryActor)f.in.getEntity(cast[0])).phase()==1,"three quiet seconds beside the boy produce the small half-smile");f.reload();for(var id:cast)h.assertTrue(f.in.getEntity(id)!=null,"the same original survives saved-data reload");for(int i=0;i<2;i++)start[i]=f.in.getEntity(cast[i]).getX();stand(p,f.in,b[0],.5,0,-9.5);
          }).thenIdle(100).thenExecute(()->{
              var left=(LiteraryActor)f.in.getEntity(cast[0]);var right=(LiteraryActor)f.in.getEntity(cast[1]);h.assertTrue(left.getX()>start[0]&&right.getX()<start[1]&&left.distanceTo(p)>=3&&right.distanceTo(p)>=3,"both originals walk inward on the actual floor and stop at least three blocks short");stand(p,f.in,b[0],.5,-1,-24.5);
          }).thenWaitUntil(()->h.assertTrue(visit(p,HouseReadSites.Room.SHELTER).getInt("SongAge")>=900,"the complete 45-second instrumental finishes in occupied time"))
          .thenExecute(()->{
              h.assertTrue(visit(p,HouseReadSites.Room.SHELTER).getBoolean("Lantern")&&HouseRead.seen(f.data(),p.getUUID()).getCompound("shelter").getString("Stood").equals("lantern"),"the dock ahead, past both rails, with its lantern is a quiet real destination, and staying there through the song is kept");h.assertTrue(packets(p).stream().anyMatch(x->x instanceof net.minecraft.network.protocol.game.ClientboundSoundPacket sound&&HouseReadRegistry.MUSIC.stream().anyMatch(cue->cue.get()==sound.getSound().value())),"the real reader receives the native instrumental");h.assertTrue(packets(peer).stream().noneMatch(x->x instanceof net.minecraft.network.protocol.game.ClientboundSoundPacket sound&&HouseReadRegistry.MUSIC.stream().anyMatch(cue->cue.get()==sound.getSound().value())),"the other reader hears no private song");
              HouseReadExport.save(f.in,b[0],HouseReadSites.Room.SHELTER,"lakeside_shelter");stand(p,f.in,b[0],-2.5,0,.5);click(p,b[0].offset(HouseReadRooms.entry(HouseReadSites.Room.SHELTER)));
          }).thenWaitUntil(()->h.assertTrue(HouseReadSites.inside(p)==null&&!LabyrinthDoors.isBusy(p),"the original door always permits a quiet departure"))
          .thenExecute(()->{h.assertTrue(p.getHealth()==20&&f.data().returnDepth(p.getUUID())==0,"neither solitude nor company punishes or rewards the reader");h.assertTrue(HouseReadSites.state(p.server,p.getUUID()).getCompound(HouseReadSites.Room.SHELTER.key()).getUUID("Man").equals(cast[0]),"leaving never restages or replaces the original cast");}).thenSucceed();
    }
}
