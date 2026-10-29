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
    @AfterBatch(batch="read_kitchen") public static void kitchenDone(ServerLevel ignored){close();}
    @AfterBatch(batch="read_closet") public static void closetDone(ServerLevel ignored){close();}
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
    private static boolean said(java.util.List<net.minecraft.network.protocol.Packet<?>> packets,String words){return packets.stream().anyMatch(x->x instanceof net.minecraft.network.protocol.game.ClientboundSystemChatPacket chat&&chat.content().getString().contains(words));}

    /** The tin's crayons, as the reader's own copy of the kitchen holds them: the left half's ten, then the right half's. */
    private static int[] tin(ServerLevel l,BlockPos b,BlockPos half){return l.getBlockEntity(b.offset(half)) instanceof HouseReadPropBlock.Fitting t?t.read().getIntArray("Colours"):new int[0];}
    /** Takes the crayon of one colour from wherever it lies in the tin, by its place. */
    private static void pick(ServerPlayer p,ServerLevel l,BlockPos b,int colour){
        for(var half:List.of(HouseReadKitchen.TIN_LEFT_AT,HouseReadKitchen.TIN_RIGHT_AT)){int[] in=tin(l,b,half);for(int slot=0;slot<in.length;slot++)if(in[slot]==colour){
            var at=b.offset(half);double[] box=HouseReadKitchen.slot(slot);var hit=new Vec3(at.getX()+(box[0]+box[1])/32,at.getY()+1.6/16,at.getZ()+(box[2]+box[3])/32);
            p.gameMode.useItemOn(p,l,p.getMainHandItem(),InteractionHand.MAIN_HAND,new BlockHitResult(hit,Direction.UP,at,false));return;}}
    }
    /** Colours the pixel at a column and row of the drawing with the crayon in hand. */
    private static void draw(ServerPlayer p,ServerLevel l,BlockPos b,int col,int row){var at=b.offset(HouseReadKitchen.SHEET_AT);p.gameMode.useItemOn(p,l,p.getMainHandItem(),InteractionHand.MAIN_HAND,new BlockHitResult(new Vec3(at.getX()+(col+.5)/16,at.getY()+.25/16,at.getZ()+(row+.5)/16),Direction.UP,at,false));}

    /**
     * 0.4.168 (owner: "The skin-color selection should be the first vignette the player encounters"): the kitchen table. The
     * tin holds every skin crayon once, in a place shuffled for each reader; the face sets the read, the hands settle it, and
     * nothing else does; the doors open once the picture is up on the fridge.
     */
    @GameTest(template="empty",batch="read_kitchen",timeoutTicks=6000)
    public static void theKitchenTableReadsOnlyTheFaceTheReaderDrawsAndOpensOnceThePictureIsUp(GameTestHelper h){
        var f=fixture(h);var p=player(h,f,"kitchen_reader");var at=f.origin.offset(0,0,-5);var target=f.origin.offset(0,0,-45);f.chunks.hold(f.in,new AABB(at).inflate(8));f.chunks.hold(f.in,new AABB(target).inflate(7));BlockPos[] b={null};LabyrinthData.Door[] from={null};ItemStack[] own={null};h.onEachTick(()->transition(p));
        var K=HouseReadSites.Room.KITCHEN;
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"source loaded")).thenExecute(()->{from[0]=source(f);for(var pos:BlockPos.betweenClosed(target.offset(-4,-1,-5),target.offset(4,-1,5)))f.in.setBlock(pos,Blocks.OAK_PLANKS.defaultBlockState(),18);LabyrinthBuilder.buildCorridor(f.in,target);LabyrinthBuilder.registerDoors(f.data(),LabyrinthPlace.GRAY_CORRIDOR,target);stand(p,f.in,at,.5,0,2);HouseReadSites.prepare(p,K);})
          .thenWaitUntil(()->h.assertTrue(ready(p,K),"the actual kitchen finishes"))
          .thenExecute(()->{
              b[0]=HouseReadSites.base(p.server,p.getUUID(),K);HouseReadAudit.check(h,f.in,b[0],K);seed(p,K,from[0]);stand(p,f.in,b[0],.5,0,-3.5);
              h.assertTrue(HouseReadSites.next(p.server,new UUID(168,0))==K,"the kitchen table is the first quiet room a reader meets");
              var parts=Map.of(HouseReadKitchen.SHEET_AT,HouseReadPropBlock.Part.SHEET,HouseReadKitchen.TIN_LEFT_AT,HouseReadPropBlock.Part.TIN_LEFT,HouseReadKitchen.TIN_RIGHT_AT,HouseReadPropBlock.Part.TIN_RIGHT,HouseReadKitchen.FRIDGE_AT,HouseReadPropBlock.Part.FRIDGE,HouseReadKitchen.FRIDGE_AT.above(),HouseReadPropBlock.Part.FRIDGE_TOP,HouseReadKitchen.RADIO_AT,HouseReadPropBlock.Part.RADIO);
              for(var e:parts.entrySet()){var s=f.in.getBlockState(b[0].offset(e.getKey()));h.assertTrue(s.is(HouseReadRegistry.FITTING.get())&&s.getValue(HouseReadPropBlock.PART)==e.getValue(),"the kitchen has its "+e.getValue());}
              // Every skin crayon once along the backs of the two halves, the eight others along the fronts, in this reader's own order.
              int[] left=tin(f.in,b[0],HouseReadKitchen.TIN_LEFT_AT),right=tin(f.in,b[0],HouseReadKitchen.TIN_RIGHT_AT);var backs=new TreeSet<Integer>();var fronts=new TreeSet<Integer>();
              for(int[] half:List.of(left,right)){h.assertTrue(half.length==10,"each half holds ten crayons");for(int k=0;k<10;k++)(k<6?backs:fronts).add(half[k]);}
              h.assertTrue(backs.size()==12&&backs.first()==0&&backs.last()==11&&fronts.size()==8&&fronts.first()==12&&fronts.last()==19,"twelve skin crayons and eight others, each once: "+backs+" "+fronts);
              h.assertTrue(!Arrays.equals(HouseReadKitchen.order(p.getUUID()),HouseReadKitchen.order(new UUID(168,1)))&&left[0]==HouseReadKitchen.order(p.getUUID())[0],"no crayon has the same place for every reader");
              h.assertTrue(HouseReadKitchen.SKIN.size()==14&&HouseReadKitchen.person(13)&&HouseReadKitchen.person(14)&&!HouseReadKitchen.person(18)&&!HouseReadKitchen.person(19)&&!HouseReadKitchen.person(12)&&HouseReadKitchen.hairColour(12)&&!HouseReadKitchen.hairColour(19),"a face can be any skin crayon or a brown; blue, green or black is play");
              // Both doors stay shut until the picture is up.
              var entry=f.data().door("house_read/"+p.getUUID()+"/kitchen/entry");h.assertTrue(entry!=null&&!HouseReadStory.ready(p,K),"the kitchen's way back is registered and its doors are locked");HouseReadSites.door(p,entry);h.assertTrue(HouseReadSites.inside(p)==K&&!HouseReadStory.closing(p),"the locked door keeps the reader in the kitchen");
              own[0]=new ItemStack(Items.COMPASS);own[0].set(DataComponents.CUSTOM_NAME,Component.literal("Original pocket compass"));p.setItemInHand(InteractionHand.MAIN_HAND,own[0].copy());
              // A crayon comes into the hand and leaves its place; the same one again puts it back.
              pick(p,f.in,b[0],4);h.assertTrue(HouseReadKitchen.held(p)==4&&HouseReadSites.loan(p.getMainHandItem(),p.getUUID())&&p.getMainHandItem().get(DataComponents.DYED_COLOR).rgb()==HouseReadKitchen.rgb(4),"the crayon comes into the hand in its own colour");
              boolean taken=false;for(var half:List.of(HouseReadKitchen.TIN_LEFT_AT,HouseReadKitchen.TIN_RIGHT_AT))if(f.in.getBlockEntity(b[0].offset(half)) instanceof HouseReadPropBlock.Fitting t&&t.read().getInt("Taken")>=0)taken=true;h.assertTrue(taken,"and leaves its place in the tin");
              pick(p,f.in,b[0],4);h.assertTrue(ItemStack.matches(own[0],p.getMainHandItem())&&HouseReadKitchen.held(p)<0,"picking the same crayon again puts it back, and the reader's own item returns");
              // The face sets the read; a different face disagrees; hands in that crayon settle it.
              pick(p,f.in,b[0],4);draw(p,f.in,b[0],7,4);
              h.assertTrue(HouseRead.raw(f.data(),p.getUUID(),HouseRead.Trait.TONE).equals(new HouseRead.Category(HouseReadKitchen.hex(4),HouseRead.Strength.TENTATIVE)),"the first face sets the tone, tentatively: "+HouseRead.raw(f.data(),p.getUUID(),HouseRead.Trait.TONE));
              pick(p,f.in,b[0],9);draw(p,f.in,b[0],8,5);h.assertTrue(HouseRead.raw(f.data(),p.getUUID(),HouseRead.Trait.TONE).strength()==HouseRead.Strength.UNCERTAIN,"a face coloured again in another crayon disagrees");
              draw(p,f.in,b[0],1,10);h.assertTrue(HouseRead.raw(f.data(),p.getUUID(),HouseRead.Trait.TONE).equals(new HouseRead.Category(HouseReadKitchen.hex(9),HouseRead.Strength.SETTLED)),"hands in the face's crayon settle it");
              pick(p,f.in,b[0],19);draw(p,f.in,b[0],7,1);h.assertTrue(HouseReadKitchen.hair(f.data(),p.getUUID())==-1,"green hair is play: the House's body keeps the account's own hair colour");
              pick(p,f.in,b[0],12);draw(p,f.in,b[0],6,2);h.assertTrue(HouseReadKitchen.hair(f.data(),p.getUUID())==HouseReadKitchen.rgb(12),"hair drawn in a hair colour is kept");
              var body=HouseBodies.body(f.data(),p.getUUID());h.assertTrue(body.tone()==HouseReadKitchen.rgb(9)&&body.hair()==HouseReadKitchen.rgb(12),"the House's body wears the face and the hair as the reader drew them");
              var sheet=(HouseReadPropBlock.Fitting)f.in.getBlockEntity(b[0].offset(HouseReadKitchen.SHEET_AT));h.assertTrue(sheet.read().getInt("Face")==9&&sheet.read().getInt("Hands")==9&&sheet.read().getInt("Hair")==12&&sheet.read().getInt("Clothes")<0,"the drawing keeps each part's colour for the client to draw: "+sheet.read());
              // Up on the fridge: the picture leaves the table, the crayon goes back, the doors open.
              stand(p,f.in,b[0],3.5,0,-8.5);click(p,b[0].offset(HouseReadKitchen.FRIDGE_AT));
              var top=(HouseReadPropBlock.Fitting)f.in.getBlockEntity(b[0].offset(HouseReadKitchen.FRIDGE_AT.above()));
              h.assertTrue(visit(p,K).getBoolean("Hung")&&f.in.getBlockState(b[0].offset(HouseReadKitchen.SHEET_AT)).getValue(HouseReadPropBlock.TURNED)&&f.in.getBlockState(b[0].offset(HouseReadKitchen.FRIDGE_AT.above())).getValue(HouseReadPropBlock.TURNED)&&top.read().getInt("Face")==9,"the picture goes up on the fridge door and leaves the table");
              h.assertTrue(HouseReadStory.ready(p,K)&&f.in.getBlockState(b[0].offset(HouseReadKitchen.EXIT)).getValue(DoorBlock.OPEN),"with the picture up, the hall door past the fridge opens");
              h.assertTrue(ItemStack.matches(own[0],p.getMainHandItem()),"the crayon goes back into the tin");
              var seen=HouseRead.seen(f.data(),p.getUUID()).getCompound("kitchen");h.assertTrue(HouseReadProse.kitchen(seen).equals("drew yourself at the kitchen table, face and hands alike, and put the picture up on the fridge")&&StaircaseAccount.read(f.data(),p.getUUID()).drewHands(),"the account and the book recite the act, never a colour: "+HouseReadProse.kitchen(seen));
              HouseReadExport.save(f.in,b[0],K,"kitchen_picture_up");
          })
          .thenIdle(120).thenExecute(()->{h.assertTrue(HouseReadSites.inside(p)==K&&!HouseReadStory.closing(p),"with the picture up the kitchen waits for the reader to walk through the open door; no timer ends it first");stand(p,f.in,b[0],5.5,0,-13);})
          .thenWaitUntil(()->h.assertTrue(HouseReadSites.inside(p)==null&&!LabyrinthDoors.isBusy(p),"walking through the open hall door, the House takes the reader on"))
          .thenExecute(()->{var shown=HouseReadStory.last(p.getUUID());h.assertTrue(shown!=null&&shown.text().equals(HouseReadStory.closing(K))&&shown.sound().equals("minecraft:block.iron_trapdoor.close"),"the kitchen is cut off with the fridge door and the House's closing line: "+shown);
              h.assertTrue(ItemStack.matches(own[0],p.getMainHandItem())&&p.getInventory().items.stream().noneMatch(i->HouseReadSites.loan(i,p.getUUID())),"no crayon leaves the kitchen");})
          .thenSucceed();
    }

    /** 0.4.166: the House's story about the reader, a line as each quiet room begins and ends. */
    @GameTest(template="empty")
    public static void theHouseStoryWelcomesTheReaderAndEndsOnItsWarningWithoutNamingThem(GameTestHelper h){
        var words=new ArrayList<String>(List.of(HouseReadStory.WELCOME,HouseReadStory.HAUNTED,HouseReadStory.WARNING_LEAD,HouseReadStory.WARNING));
        for(var room:HouseReadSites.Room.values()){words.add(HouseReadStory.opening(room));words.add(HouseReadStory.closing(room));h.assertTrue(!HouseReadStory.opening(room).isBlank()&&!HouseReadStory.closing(room).isBlank(),"every quiet room has its opening and closing line: "+room);}
        h.assertTrue(HouseReadStory.WELCOME.startsWith("Come in")&&HouseReadStory.WARNING.equals("this house has ghosts in it."),"the story welcomes the reader in and ends on the warning that this house has ghosts in it");
        var forbidden=java.util.regex.Pattern.compile("\\b(he|she|him|her|his|hers|man|woman|boy|girl|son|daughter|brother|sister|black|white|brown|young|old|older|younger)\\b",java.util.regex.Pattern.CASE_INSENSITIVE);
        for(String line:words){h.assertTrue(!forbidden.matcher(line).find(),"the House's story never genders the reader, names a colour or an age: "+line);h.assertTrue(!line.contains(HouseReadStory.PAGE),"each line is one page");}
        for(String line:words){int t=HouseReadStory.ticks(line);h.assertTrue(t>=40&&t<=200,"each page holds long enough to read and no longer: "+t+" for "+line);}
        h.assertTrue(HouseReadStory.ticks(HouseReadStory.WELCOME+HouseReadStory.PAGE+HouseReadStory.opening(HouseReadSites.Room.WAITING))==HouseReadStory.page(HouseReadStory.WELCOME)+HouseReadStory.page(HouseReadStory.opening(HouseReadSites.Room.WAITING)),"a card's length is the sum of its pages, as the client counts it");
        h.succeed();
    }

    @GameTest(template="empty",batch="read_wardrobe",timeoutTicks=5000)
    public static void actualBorrowingRetreatRestoresFullInventoryAndTheOriginalGearAfterReload(GameTestHelper h){
        var f=fixture(h);var p=player(h,f,"coat_reader");var peer=player(h,f,"coat_observer");var sourceAt=f.origin.offset(0,0,-5);f.chunks.hold(f.in,new AABB(sourceAt).inflate(8));BlockPos[] b={null};LabyrinthData.Door[] from={null};ItemStack[] gear={null,null};h.onEachTick(()->transition(p));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native source is resident")).thenExecute(()->{from[0]=source(f);stand(p,f.in,sourceAt,.5,0,2.5);HouseReadSites.prepare(p,HouseReadSites.Room.COATROOM);})
          .thenWaitUntil(()->h.assertTrue(ready(p,HouseReadSites.Room.COATROOM),"the bounded production coatroom finishes"))
          .thenExecute(()->{
              b[0]=HouseReadSites.base(p.server,p.getUUID(),HouseReadSites.Room.COATROOM);HouseReadAudit.check(h,f.in,b[0],HouseReadSites.Room.COATROOM);seed(p,HouseReadSites.Room.COATROOM,from[0]);stand(p,f.in,b[0],.5,0,.5);stand(peer,f.in,b[0],2.5,0,.5);
              for(int i=0;i<36;i++)p.getInventory().setItem(i,new ItemStack(Items.COBBLESTONE,64));gear[0]=new ItemStack(Items.DIAMOND_CHESTPLATE);gear[0].set(DataComponents.CUSTOM_NAME,Component.literal("Original coat armour"));gear[0].setDamageValue(37);gear[1]=new ItemStack(Items.DIAMOND_BOOTS);gear[1].setDamageValue(19);p.setItemSlot(EquipmentSlot.CHEST,gear[0].copy());p.setItemSlot(EquipmentSlot.FEET,gear[1].copy());
              // 0.4.166: until the House has seen the reader dress (or turn the glass away), the door back and the curtain stay shut.
              var locked=LabyrinthData.get(p.server).door("house_read/"+p.getUUID()+"/coatroom/entry");click(p,locked.lower);var curtain=b[0].offset(HouseReadRooms.CURTAIN_AT);
              h.assertTrue(HouseReadSites.inside(p)==HouseReadSites.Room.COATROOM&&!LabyrinthDoors.isBusy(p)&&!HouseReadStory.ready(p,HouseReadSites.Room.COATROOM)&&!f.in.getBlockState(curtain).getCollisionShape(f.in,curtain).isEmpty(),"the door back is locked and the curtain stops a body until the room has its answer");
              p.lookAt(EntityAnchorArgument.Anchor.EYES,b[0].offset(HouseReadRooms.UMBRELLA_AT).getCenter());
              var peg=b[0].offset(HouseReadRooms.PEGS.get(0));var part=f.in.getBlockState(peg).getValue(HouseReadPropBlock.PART);HouseReadSites.use(peer,peg,part);h.assertTrue(peer.getItemBySlot(EquipmentSlot.CHEST).isEmpty(),"an observer cannot borrow another reader's clothes");
              click(p,peg);var set=HouseReadWardrobe.of(part).set();BlockPos boots=null;for(var rel:HouseReadWardrobe.BOOT_PLACES)if(HouseReadWardrobe.of(f.in.getBlockState(b[0].offset(rel)).getValue(HouseReadPropBlock.PART)).set()==set)boots=b[0].offset(rel);click(p,boots);h.assertTrue(HouseReadSites.loan(p.getItemBySlot(EquipmentSlot.CHEST),p.getUUID())&&HouseReadSites.loan(p.getItemBySlot(EquipmentSlot.FEET),p.getUUID()),"actual block use equips both original room loans even with a full pack");
          }).thenIdle(6).thenExecute(()->{
              h.assertTrue(p.getTicksFrozen()==0&&!visit(p,HouseReadSites.Room.COATROOM).getBoolean("Written"),"clothing lifts the harmless frost and a watched slip stays blank");p.setYRot(180);p.setXRot(0);
              var curtain=b[0].offset(HouseReadRooms.CURTAIN_AT);h.assertTrue(HouseReadStory.ready(p,HouseReadSites.Room.COATROOM)&&f.in.getBlockState(curtain).getCollisionShape(f.in,curtain).isEmpty(),"once dressed, the room has its answer and the curtain parts");
          }).thenWaitUntil(()->h.assertTrue(visit(p,HouseReadSites.Room.COATROOM).getBoolean("Written"),"the slip acquires writing only after looking away"))
          .thenExecute(()->{
              stand(p,f.in,b[0],2.5,0,.5);click(p,b[0].offset(HouseReadRooms.UMBRELLA_AT));h.assertTrue(p.containerMenu instanceof LecternMenu&&!p.containerMenu.clickMenuButton(p,3),"the note opens actual native pages and cannot be printed or taken");p.closeContainer();
              // 0.4.150: two seconds at the glass in the borrowed coat, looking into it, is kept.
              stand(p,f.in,b[0],2.5,0,-5.5);p.lookAt(EntityAnchorArgument.Anchor.EYES,b[0].offset(HouseReadRooms.MIRROR).above().getCenter());
          }).thenIdle(45).thenExecute(()->{
              var read=HouseRead.raw(f.data(),p.getUUID(),HouseRead.Trait.SELF);var cloth=((HouseReadRegistry.Garment)p.getItemBySlot(EquipmentSlot.CHEST).getItem()).cloth;h.assertTrue(read.strength()==HouseRead.Strength.SETTLED&&read.value().equals(new String[]{"MASC","FEM","ANDROGYNOUS"}[HouseReadWardrobe.set(cloth)]),"the first coat sets the presentation and two seconds at the glass in it settle it: "+read);
              h.assertTrue(HouseReadLook.of(p).coat().equals(cloth)&&!HouseReadLook.of(p).unseen()&&HouseReadLook.of(peer).coat().isEmpty(),"the House's version of the reader wears the coat they took, and nobody else's");
              var seen=visit(p,HouseReadSites.Room.COATROOM);h.assertTrue(seen.getBoolean("AtMirror")&&seen.getList("Actions",net.minecraft.nbt.Tag.TAG_STRING).stream().anyMatch(t->t.getAsString().equals("stood at the hall glass in the borrowed coat")),"standing at the glass in the borrowed coat is kept");
              var glass=b[0].offset(HouseReadRooms.MIRROR);stand(p,f.in,b[0],2.5,0,-6.5);click(p,glass);h.assertTrue(f.in.getBlockState(glass).getValue(HouseReadPropBlock.TURNED),"native glass use turns the actual mirror to its wooden back");click(p,b[0].offset(HouseReadRooms.TOWEL_AT));h.assertTrue(f.in.getBlockState(glass).getValue(HouseReadPropBlock.PART)==HouseReadPropBlock.Part.COVERED_MIRROR&&f.in.getBlockState(b[0].offset(HouseReadRooms.TOWEL_AT)).getValue(HouseReadPropBlock.TURNED),"the towel from the bar beside it physically covers the same glass");click(p,glass);h.assertTrue(f.in.getBlockState(glass).getValue(HouseReadPropBlock.PART)==HouseReadPropBlock.Part.MIRROR,"the same mirror can be uncovered again");
              f.reload();h.assertTrue(visit(p,HouseReadSites.Room.COATROOM).getBoolean("CapturedChest")&&visit(p,HouseReadSites.Room.COATROOM).getBoolean("Written"),"native saved-data reload preserves gear custody and the unseen writing");
              HouseReadExport.save(f.in,b[0],HouseReadSites.Room.COATROOM,"coatroom");var entry=LabyrinthData.get(p.server).door("house_read/"+p.getUUID()+"/coatroom/entry");stand(p,f.in,b[0],.5,0,1);click(p,entry.lower);
              var shown=HouseReadStory.last(p.getUUID());h.assertTrue(shown!=null&&shown.text().equals(HouseReadStory.closing(HouseReadSites.Room.COATROOM))&&!shown.sound().isEmpty()&&!shown.title()&&HouseReadSites.inside(p)==HouseReadSites.Room.COATROOM&&HouseReadStory.closing(p),"the complete room's door opens onto the House's closing line on black, heard cut off, before the reader is taken: "+shown);
          }).thenWaitUntil(()->h.assertTrue(HouseReadSites.inside(p)==null&&!LabyrinthDoors.isBusy(p),"after the closing line the original door takes the reader back"))
          .thenExecute(()->{
              h.assertTrue(ItemStack.matches(gear[0],p.getItemBySlot(EquipmentSlot.CHEST))&&ItemStack.matches(gear[1],p.getItemBySlot(EquipmentSlot.FEET)),"the exact original gear, names and damage return to their original slots");for(int i=0;i<36;i++)h.assertTrue(p.getInventory().getItem(i).is(Items.COBBLESTONE)&&p.getInventory().getItem(i).getCount()==64,"a full original pack is unchanged");
              h.assertTrue(f.data().returnCount(p.getUUID())==0&&p.getHealth()==20,"a quiet retreat consumes no story return, health or reward");
          }).thenSucceed();
    }

    /** The garment hanging or standing at one place in a reader's coatroom, if any. */
    private static HouseReadWardrobe.Garment at(ServerLevel l,BlockPos at){var state=l.getBlockState(at);return state.is(HouseReadRegistry.FITTING.get())?HouseReadWardrobe.of(state.getValue(HouseReadPropBlock.PART)):null;}
    private static BlockPos find(ServerLevel l,BlockPos b,String id){for(var rel:HouseReadWardrobe.coatHooks())if(at(l,b.offset(rel)) instanceof HouseReadWardrobe.Garment g&&g.id().equals(id))return b.offset(rel);for(var rel:HouseReadWardrobe.BOOT_PLACES)if(at(l,b.offset(rel)) instanceof HouseReadWardrobe.Garment g&&g.id().equals(id))return b.offset(rel);for(var rel:HouseReadWardrobe.RAIL)if(at(l,b.offset(rel)) instanceof HouseReadWardrobe.Garment g&&g.id().equals(id))return b.offset(rel);return null;}

    /**
     * 0.4.169 (owner: "a drastically wider variety of clothes, each guessing something about the player's personality. maybe add
     * a brief history to each piece of cloth"): twelve coats and six pairs of boots, each once, in an order of the reader's own;
     * each carries its past in its tooltip and, put on for the first time, has the House say what it guesses. The first coat
     * still sets the read by its set, and the account names the garment itself; an older read keeps its words.
     */
    @GameTest(template="empty",batch="read_closet",timeoutTicks=5000)
    public static void theWardrobeHangsEveryGarmentOnceWithItsPastAndWhatItLooksLike(GameTestHelper h){
        var f=fixture(h);var p=player(h,f,"wardrobe_reader");var other=player(h,f,"wardrobe_other");var sourceAt=f.origin.offset(0,0,-5);f.chunks.hold(f.in,new AABB(sourceAt).inflate(8));BlockPos[] b={null,null};LabyrinthData.Door[] from={null};h.onEachTick(()->{transition(p);transition(other);});
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native source is resident")).thenExecute(()->{from[0]=source(f);for(var r:List.of(p,other)){stand(r,f.in,sourceAt,.5,0,2.5);HouseReadSites.prepare(r,HouseReadSites.Room.COATROOM);}})
          .thenWaitUntil(()->h.assertTrue(ready(p,HouseReadSites.Room.COATROOM),"the first coatroom finishes")).thenWaitUntil(()->h.assertTrue(ready(other,HouseReadSites.Room.COATROOM),"the second coatroom finishes"))
          .thenExecute(()->{
              b[0]=HouseReadSites.base(p.server,p.getUUID(),HouseReadSites.Room.COATROOM);b[1]=HouseReadSites.base(p.server,other.getUUID(),HouseReadSites.Room.COATROOM);HouseReadAudit.check(h,f.in,b[0],HouseReadSites.Room.COATROOM);
              h.assertTrue(HouseReadWardrobe.coats().size()==12&&HouseReadWardrobe.footwear().size()==6&&HouseReadWardrobe.coatHooks().size()==12,"twelve coats, six pairs of boots, a hook for every coat");
              for(var g:HouseReadWardrobe.ALL){h.assertTrue(find(f.in,b[0],g.id())!=null&&find(f.in,b[1],g.id())!=null,"every garment is in each reader's wardrobe: "+g.id());h.assertTrue(!g.history().isBlank()&&!g.look().isBlank()&&g.set()>=0&&g.set()<3,"every garment has a past, a look and a set: "+g.id());
                  for(String text:List.of(g.history(),g.look()))for(String word:List.of("boy","girl","young"," old ","white","black","brown")){String lower=" "+text.toLowerCase(Locale.ROOT)+" ";h.assertTrue(!lower.contains(word),"no garment's words read the reader's gender, colour or age: "+g.id()+" / "+word);}}
              h.assertTrue(f.in.getBlockState(b[0].offset(HouseReadWardrobe.EMPTY_HOOK)).getValue(HouseReadPropBlock.PART)==HouseReadPropBlock.Part.EMPTY_HANGER,"the hanger by the door stays empty");
              boolean differ=false;for(var rel:HouseReadWardrobe.coatHooks())differ|=at(f.in,b[0].offset(rel))!=at(f.in,b[1].offset(rel));h.assertTrue(differ,"two readers' wardrobes hang in different orders");
              seed(p,HouseReadSites.Room.COATROOM,from[0]);stand(p,f.in,b[0],.5,0,-2);
              var tail=find(f.in,b[0],"ringmaster");click(p,tail);var worn=p.getItemBySlot(EquipmentSlot.CHEST);
              h.assertTrue(worn.getItem() instanceof HouseReadRegistry.Garment g&&g.cloth.equals("ringmaster")&&HouseReadSites.loan(worn,p.getUUID())&&f.in.getBlockState(tail).getValue(HouseReadPropBlock.TURNED),"the tailcoat comes off its hook onto the reader, lent");
              var lines=new ArrayList<Component>();worn.getItem().appendHoverText(worn,Item.TooltipContext.EMPTY,lines,TooltipFlag.NORMAL);h.assertTrue(lines.stream().anyMatch(c->c.getString().equals("Belonged to a circus performer. The lining is darned in a dozen threads.")),"its past is in its tooltip: "+lines);
              var seen=HouseRead.seen(f.data(),p.getUUID()).getCompound("coatroom");h.assertTrue(seen.getString("CoatId").equals("ringmaster")&&seen.getString("Coat").equals("wool")&&HouseRead.raw(f.data(),p.getUUID(),HouseRead.Trait.SELF).equals(new HouseRead.Category("MASC",HouseRead.Strength.TENTATIVE)),"the first coat reads by its set and is kept by itself");
              var swing=find(f.in,b[0],"swing_coat");click(p,swing);h.assertTrue(!f.in.getBlockState(tail).getValue(HouseReadPropBlock.TURNED)&&f.in.getBlockState(swing).getValue(HouseReadPropBlock.TURNED)&&((HouseReadRegistry.Garment)p.getItemBySlot(EquipmentSlot.CHEST).getItem()).cloth.equals("swing_coat"),"a second coat goes on and the first goes back on its hook");
              // 0.4.172: an everyday outfit comes off the rail in place of a coat, and the coat goes back on its hook.
              var tee=find(f.in,b[0],"tee_jeans");click(p,tee);h.assertTrue(f.in.getBlockState(tee).getValue(HouseReadPropBlock.TURNED)&&!f.in.getBlockState(swing).getValue(HouseReadPropBlock.TURNED)&&((HouseReadRegistry.Garment)p.getItemBySlot(EquipmentSlot.CHEST).getItem()).cloth.equals("tee_jeans"),"the t-shirt and jeans come off the rail in place of the coat, which goes back on its hook");
              h.assertTrue(visit(p,HouseReadSites.Room.COATROOM).getList("Guessed",net.minecraft.nbt.Tag.TAG_STRING).size()==3&&HouseRead.seen(f.data(),p.getUUID()).getCompound("coatroom").getString("CoatId").equals("ringmaster"),"each garment is guessed once; the first coat is still the one the House keeps");
              click(p,find(f.in,b[0],"galoshes"));h.assertTrue(HouseRead.seen(f.data(),p.getUUID()).getCompound("coatroom").getString("BootsId").equals("galoshes"),"the boots are kept by themselves");
              seen=HouseRead.seen(f.data(),p.getUUID()).getCompound("coatroom");String said=HouseReadProse.coatroom(seen);
              h.assertTrue(said.startsWith("took the ringmaster's tailcoat off the peg in the hall and the galoshes from the mat"),"the account names the garments themselves: "+said);
              h.assertTrue(StaircaseProse.hands(StaircaseAccount.read(f.data(),p.getUUID())).contains("the ringmaster's tailcoat"),"and so does the book");
              var old=new CompoundTag();old.putString("Coat","trench");old.putString("Boots","wool");h.assertTrue(HouseReadProse.coatroom(old).startsWith("took the trench coat off the peg in the hall and the heavy leather boots from the mat"),"an older read keeps its words: "+HouseReadProse.coatroom(old));
              HouseReadSites.left(p);boolean back=true;for(var g:HouseReadWardrobe.ALL)back&=!f.in.getBlockState(find(f.in,b[0],g.id())).getValue(HouseReadPropBlock.TURNED);
              h.assertTrue(back&&p.getItemBySlot(EquipmentSlot.CHEST).isEmpty()&&p.getItemBySlot(EquipmentSlot.FEET).isEmpty(),"leaving puts every garment back and takes none away");
          }).thenIdle(2).thenExecute(()->{
              // The lines reach the reader's connection once it has flushed, a tick later.
              var lines=packets(p).stream().filter(x->x instanceof net.minecraft.network.protocol.game.ClientboundSystemChatPacket).map(x->((net.minecraft.network.protocol.game.ClientboundSystemChatPacket)x).content().getString()).toList();
              String tail=HouseReadWardrobe.byId("ringmaster").look();h.assertTrue(lines.stream().filter(l->l.equals(tail)).count()==1&&lines.contains(HouseReadWardrobe.byId("swing_coat").look())&&lines.contains(HouseReadWardrobe.byId("galoshes").look())&&lines.contains(HouseReadWardrobe.byId("tee_jeans").look()),"the House says what each garment looks like the first time it goes on, once: "+lines);
              for(var g:HouseReadWardrobe.ALL)h.assertTrue(!g.look().startsWith("You ")&&!g.look().contains(" you "),"0.4.172: the line describes the garment, not the reader: "+g.look());
              var outfit=new net.minecraft.nbt.CompoundTag();outfit.putString("Coat","barn");outfit.putString("CoatId","tee_jeans");var told=HouseReadProse.coatroom(outfit);
              h.assertTrue(told.equals("took the t-shirt and jeans off the rail in the hall without looking in the glass"),"an outfit taken first comes off the rail in the account: "+told);
              h.assertTrue(HouseReadWardrobe.outfits().size()==3&&HouseReadWardrobe.coats().size()==12&&HouseReadWardrobe.ALL.size()==21,"twelve coats, six pairs of boots and three outfits");
          }).thenSucceed();
    }

    @GameTest(template="empty",batch="read_route",timeoutTicks=7000)
    public static void savedInterleavedVisitsStillRejoinTheSameOrdinaryHallIndependently(GameTestHelper h){
        var f=fixture(h);var a=player(h,f,"quiet_a");var b=player(h,f,"quiet_b");var at=f.origin.offset(0,0,-5);var target=f.origin.offset(0,0,-45);f.chunks.hold(f.in,new AABB(at).inflate(6));f.chunks.hold(f.in,new AABB(target).inflate(7));LabyrinthData.Door[] from={null};BlockPos[] ab={null},bb={null};h.onEachTick(()->{transition(a);transition(b);});
        var notEntered=NativeTestPlayers.survival(h,"quiet_before_opening");f.players.add(notEntered);HouseReadSites.prepareNext(notEntered);h.assertTrue(!HouseReadSites.state(notEntered.server,notEntered.getUUID()).contains("Index"),"preparing an ordinary arrival before the opening cannot allocate or queue a quiet room");
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"both native shared doorways are resident")).thenExecute(()->{
            from[0]=source(f);for(var pos:BlockPos.betweenClosed(target.offset(-4,-1,-5),target.offset(4,-1,5)))f.in.setBlock(pos,Blocks.OAK_PLANKS.defaultBlockState(),18);LabyrinthBuilder.buildCorridor(f.in,target);LabyrinthBuilder.registerDoors(f.data(),LabyrinthPlace.GRAY_CORRIDOR,target);
            for(var p:List.of(a,b)){stand(p,f.in,at,.5,0,2);f.data().deal(p.getUUID(),from[0],LabyrinthPlace.GRAY_CORRIDOR.id(),false);HouseReadSites.prepare(p,HouseReadSites.Room.WAITING);}
          }).thenWaitUntil(()->h.assertTrue(ready(a,HouseReadSites.Room.WAITING),"the first saved private site is resident")).thenExecute(()->{seed(a,HouseReadSites.Room.WAITING,from[0]);stand(a,f.in,HouseReadSites.base(a.server,a.getUUID(),HouseReadSites.Room.WAITING),.5,0,.5);}).thenWaitUntil(()->h.assertTrue(ready(b,HouseReadSites.Room.WAITING),"the second saved private site is resident")).thenExecute(()->{seed(b,HouseReadSites.Room.WAITING,from[0]);stand(b,f.in,HouseReadSites.base(b.server,b.getUUID(),HouseReadSites.Room.WAITING),.5,0,.5);}).thenWaitUntil(()->{boolean present=HouseReadSites.inside(a)==HouseReadSites.Room.WAITING&&HouseReadSites.inside(b)==HouseReadSites.Room.WAITING&&!LabyrinthDoors.isBusy(a)&&!LabyrinthDoors.isBusy(b);if(!present)java.util.concurrent.locks.LockSupport.parkNanos(2_000_000L);h.assertTrue(present,"the same original door gives both actual native players a complete private copy");})
          .thenExecute(()->{ab[0]=HouseReadSites.base(a.server,a.getUUID(),HouseReadSites.Room.WAITING);bb[0]=HouseReadSites.base(b.server,b.getUUID(),HouseReadSites.Room.WAITING);h.assertTrue(!ab[0].equals(bb[0]),"the private copies are distinct physical sites");
              // The first reader picks up the phone at once; the second does nothing at all.
              stand(a,f.in,ab[0],4.5,0,-2);click(a,ab[0].offset(HouseReadRemainingRooms.PHONE_AT));h.assertTrue(a.getMainHandItem().is(HouseReadRegistry.PHONE.get()),"the first reader passes the time with the phone");})
          .thenWaitUntil(()->h.assertTrue(HouseReadMoments.canProceed(a,HouseReadSites.Room.WAITING),"the first reader is called once the forty-second occupied wait finishes"))
          .thenExecute(()->{h.assertTrue(said(packets(a),"Sorry for keeping you waiting"),"the receptionist calls the reader through");stand(a,f.in,ab[0],.5,0,-10.5);click(a,ab[0].offset(HouseReadMoments.exit(HouseReadSites.Room.WAITING)));})
          .thenWaitUntil(()->h.assertTrue(HouseReadSites.inside(a)==null&&!LabyrinthDoors.isBusy(a)&&a.distanceToSqr(target.getCenter())<25,"the first reader goes through the office door into the original ordinary hall"))
          // 0.4.166: the House needs to see how the reader passes the time. Doing nothing keeps the door shut, with the
          // receptionist's word through the glass, until they pick something up; then the call comes and the House takes them.
          .thenWaitUntil(()->h.assertTrue(visit(b,HouseReadSites.Room.WAITING).getInt("Age")>=HouseReadMoments.WAIT+5,"the second reader's forty seconds pass"))
          .thenExecute(()->{h.assertTrue(!HouseReadMoments.canProceed(b,HouseReadSites.Room.WAITING)&&!f.in.getBlockState(bb[0].offset(HouseReadMoments.exit(HouseReadSites.Room.WAITING))).getValue(DoorBlock.OPEN)&&said(packets(b),"It won't be much longer"),"a reader who has done nothing is not called: the door stays shut and the receptionist asks them to wait");
              stand(b,f.in,bb[0],.5,0,1);click(b,bb[0].offset(HouseReadRooms.COAT_ENTRY));h.assertTrue(HouseReadSites.inside(b)==HouseReadSites.Room.WAITING&&!LabyrinthDoors.isBusy(b),"and the door back is locked too");
              stand(b,f.in,bb[0],-3.5,0,-3);click(b,bb[0].offset(HouseReadRemainingRooms.PAPER_AT));b.closeContainer();})
          // 0.4.172: the call opens the office door, and the room ends only as the reader walks through it.
          .thenWaitUntil(()->h.assertTrue(HouseReadMoments.canProceed(b,HouseReadSites.Room.WAITING)&&f.in.getBlockState(bb[0].offset(HouseReadMoments.exit(HouseReadSites.Room.WAITING))).getValue(DoorBlock.OPEN),"once they pick up the paper the receptionist calls the second reader and the office door opens"))
          .thenIdle(80).thenExecute(()->{h.assertTrue(HouseReadSites.inside(b)==HouseReadSites.Room.WAITING&&!HouseReadStory.closing(b),"the room waits for the reader to walk through the open door; no timer ends it first: inside "+HouseReadSites.inside(b)+", closing "+HouseReadStory.closing(b)+", at "+b.position().subtract(Vec3.atLowerCornerOf(bb[0])));stand(b,f.in,bb[0],.5,0,-13);})
          .thenWaitUntil(()->h.assertTrue(HouseReadSites.inside(b)==null&&!LabyrinthDoors.isBusy(b)&&b.position().distanceToSqr(a.position())<.01,"walking through it, the second reader is taken into that same shared hall"))
          .thenExecute(()->{
              h.assertTrue(f.data().returnCount(a.getUUID())==1&&f.data().returnCount(b.getUUID())==1,"each route contains exactly the genuine original forward crossing");var back=new LabyrinthData.Waypoint(from[0].dimension,Vec3.atBottomCenterOf(from[0].lower),from[0].facing.toYRot(),true);var originalKey=f.data().nodeKey(a.getUUID(),LabyrinthPlace.GRAY_CORRIDOR);h.assertTrue(f.data().node(a.getUUID(),originalKey)!=null,"the original destination has the ordinary native remembered map");
              h.assertTrue(HouseReadSites.next(a.server,a.getUUID())==HouseReadSites.Room.KITCHEN&&WitnessAccount.Story.of(LabyrinthPlace.READ_COATROOM.id())==null&&WitnessAccount.Story.of(LabyrinthPlace.READ_PORCH.id())==null&&WitnessAccount.Story.of(LabyrinthPlace.READ_KITCHEN.id())==null,"the next quiet moment is the kitchen table, still unentered and first in the order, and the quiet rooms are not Witness sources");
              var kept=HouseReadSites.state(b.server,b.getUUID()).getCompound("waiting");h.assertTrue(kept.getInt("CalledAt")>=kept.getInt("FirstAge")+60,"the call waits three seconds after a late first pick-up");
          }).thenSucceed();
    }

    @GameTest(template="empty",batch="read_porch",timeoutTicks=6000)
    public static void porchWhistleVoiceAndAllQuietWaysOnRemainPersonalAndTheRailWritesUnseen(GameTestHelper h){
        var f=fixture(h);var p=player(h,f,"porch_reader");var peer=player(h,f,"porch_peer");var at=f.origin.offset(0,0,-5);f.chunks.hold(f.in,new AABB(at).inflate(6));BlockPos[] b={null};LabyrinthData.Door[] from={null};h.onEachTick(()->transition(p));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the original House door is loaded")).thenExecute(()->{from[0]=source(f);stand(p,f.in,at,.5,0,2);stand(peer,f.in,at,2.5,0,2);HouseReadSites.prepare(p,HouseReadSites.Room.PORCH);})
          .thenWaitUntil(()->h.assertTrue(ready(p,HouseReadSites.Room.PORCH),"the whole bounded private yard is complete"))
          .thenExecute(()->{b[0]=HouseReadSites.base(p.server,p.getUUID(),HouseReadSites.Room.PORCH);HouseReadAudit.check(h,f.out,b[0],HouseReadSites.Room.PORCH);seed(p,HouseReadSites.Room.PORCH,from[0]);
              // 0.4.166: before the whistle has been answered the gate back to the House is locked.
              var entry=f.data().door("house_read/"+p.getUUID()+"/porch/entry");stand(p,f.out,b[0],.5,0,11.5);click(p,entry.lower);h.assertTrue(HouseReadSites.inside(p)==HouseReadSites.Room.PORCH&&!LabyrinthDoors.isBusy(p)&&!HouseReadStory.ready(p,HouseReadSites.Room.PORCH),"the porch's way back is locked until the House has its answer");
              stand(p,f.out,b[0],.5,0,8.5);p.lookAt(EntityAnchorArgument.Anchor.EYES,b[0].offset(HouseReadRooms.RAIL_AT).getCenter());HouseReadExport.save(f.out,b[0],HouseReadSites.Room.PORCH,"porch");})
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
          }).thenWaitUntil(()->h.assertTrue(HouseReadSites.inside(p)==null&&!LabyrinthDoors.isBusy(p),"once the House has its answer the actual entry opens, after the closing line"))
          .thenExecute(()->h.assertTrue(p.getHealth()==20&&f.data().returnCount(p.getUUID())==0,"there is no punishment or story credit for this quiet moment")).thenSucceed();
    }
    @GameTest(template="empty",batch="read_waiting",timeoutTicks=6000)
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
          .thenWaitUntil(()->h.assertTrue(visit(p,HouseReadSites.Room.WAITING).getInt("Age")>=HouseReadMoments.WAIT,"the forty-second occupied wait completes after reload"))
          .thenExecute(()->{
              var seen=HouseRead.seen(f.data(),p.getUUID()).getCompound("waiting");h.assertTrue(seen.getString("First").equals("glasses")&&seen.getString("Kept").equals("glasses")&&HouseRead.raw(f.data(),p.getUUID(),HouseRead.Trait.AGE).equals(new HouseRead.Category("ANALOG",HouseRead.Strength.SETTLED)),"still wearing the glasses when the door clicks settles it; the paper, chair and phone after them are only observed");
              for(var rel:List.of(HouseReadRemainingRooms.PAPER_AT,HouseReadRemainingRooms.CRT_AT,HouseReadRemainingRooms.PHONE_AT)){double d=Math.sqrt(rel.getX()*rel.getX()+rel.getZ()*rel.getZ());h.assertTrue(d>5.5&&d<7.5,"the three ways to pass the time stand about seven blocks from the door: "+rel);}
              h.assertTrue(f.in.getBlockState(b[0].offset(HouseReadRemainingRooms.OUTLET_AT)).getValue(HouseReadPropBlock.PART)==HouseReadPropBlock.Part.OUTLET&&f.in.getBlockState(b[0].offset(7,1,-2)).is(HouseReadRegistry.WINDOW.get()),"the phone charges at its outlet and the phone chair has its clear window onto the lot");
              h.assertTrue(f.in.getBlockState(b[0].offset(HouseReadMoments.exit(HouseReadSites.Room.WAITING))).getValue(DoorBlock.OPEN),"the actual exit opens regardless of what was handled");HouseReadExport.save(f.in,b[0],HouseReadSites.Room.WAITING,"waiting_room");stand(p,f.in,b[0],.5,0,1);click(p,b[0].offset(HouseReadRooms.COAT_ENTRY));
          }).thenWaitUntil(()->h.assertTrue(HouseReadSites.inside(p)==null&&!LabyrinthDoors.isBusy(p),"after the call the native original entry opens too, after the closing line"))
          .thenExecute(()->{h.assertTrue(f.in.getChunkSource().getChunkNow(coldCoat[0].getX()>>4,coldCoat[0].getZ()>>4)==null,"returning borrowed waiting-room objects must not load the unrelated coatroom");h.assertTrue(ItemStack.matches(head[0],p.getItemBySlot(EquipmentSlot.HEAD)),"the original named/damaged helmet returns exactly");for(int i=0;i<36;i++)h.assertTrue(p.getInventory().getItem(i).is(Items.COBBLESTONE)&&p.getInventory().getItem(i).getCount()==64,"the full original pack returns intact");h.assertTrue(f.in.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,p.getBoundingBox().inflate(5),e->ItemStack.matches(extra[0],e.getItem())).size()==1,"an item put into the borrowed hand is preserved exactly once even when the original pack is full");h.assertTrue(f.data().returnCount(p.getUUID())==0,"quiet waiting gives no story return or reward");}).thenSucceed();
    }

    @GameTest(template="empty",batch="read_routine",timeoutTicks=6000)
    public static void oneRoutineItemAtATimeIsPickedUpAndUsedAtTheMirrorWhichEndsTheBathroom(GameTestHelper h){
        var f=fixture(h);var p=player(h,f,"routine_reader");var at=f.origin.offset(0,0,-5);var target=f.origin.offset(0,0,-45);f.chunks.hold(f.in,new AABB(at).inflate(8));f.chunks.hold(f.in,new AABB(target).inflate(7));BlockPos[] b={null};LabyrinthData.Door[] from={null};int[] first={-1},second={-1};ItemStack[] own={null};h.onEachTick(()->transition(p));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"source loaded")).thenExecute(()->{from[0]=source(f);for(var pos:BlockPos.betweenClosed(target.offset(-4,-1,-5),target.offset(4,-1,5)))f.in.setBlock(pos,Blocks.OAK_PLANKS.defaultBlockState(),18);LabyrinthBuilder.buildCorridor(f.in,target);LabyrinthBuilder.registerDoors(f.data(),LabyrinthPlace.GRAY_CORRIDOR,target);stand(p,f.in,at,.5,0,2);HouseReadSites.prepare(p,HouseReadSites.Room.BATHROOM);})
          .thenWaitUntil(()->h.assertTrue(ready(p,HouseReadSites.Room.BATHROOM),"the actual bathroom finishes"))
          .thenExecute(()->{
              b[0]=HouseReadSites.base(p.server,p.getUUID(),HouseReadSites.Room.BATHROOM);HouseReadAudit.check(h,f.in,b[0],HouseReadSites.Room.BATHROOM);seed(p,HouseReadSites.Room.BATHROOM,from[0]);stand(p,f.in,b[0],.5,0,-7.5);
              // 0.4.167: sixteen tools, two to a block, on eight blocks; a set's two blocks side by side.
              var kinds=new HashSet<HouseReadPropBlock.Part>();int[] groups=new int[8];for(int i=0;i<8;i++){var rel=HouseReadRemainingRooms.ROUTINE_PLACES.get(i);h.assertTrue(rel.getY()==1,"every routine sits on the vanity counter");var part=f.in.getBlockState(b[0].offset(rel)).getValue(HouseReadPropBlock.PART);kinds.add(part);groups[i]=HouseReadPropBlock.routine(part);}
              h.assertTrue(kinds.size()==8&&java.util.stream.IntStream.range(0,4).allMatch(k->groups[2*k]/2==groups[2*k+1]/2),"eight distinct routine blocks, each set's pair side by side: "+java.util.Arrays.toString(groups));
              h.assertTrue(f.in.getBlockState(b[0].offset(HouseReadRemainingRooms.POSTCARD_AT)).getValue(HouseReadPropBlock.PART)==HouseReadPropBlock.Part.POSTCARD,"the postcard moved up beside the mirror's top");
              own[0]=new ItemStack(Items.COMPASS);own[0].set(DataComponents.CUSTOM_NAME,Component.literal("Original pocket compass"));p.setItemInHand(InteractionHand.MAIN_HAND,own[0].copy());
              // Pick up one item, then change your mind to an item from another set: the first goes back.
              var a=b[0].offset(HouseReadRemainingRooms.ROUTINE_PLACES.get(0));int ga=HouseReadPropBlock.routine(f.in.getBlockState(a).getValue(HouseReadPropBlock.PART));
              p.gameMode.useItemOn(p,p.serverLevel(),p.getMainHandItem(),InteractionHand.MAIN_HAND,new BlockHitResult(new Vec3(a.getX()+3/16.0,a.getY()+.15,a.getZ()+.5),Direction.UP,a,false));first[0]=HouseReadMoments.PAIRS[ga][0];
              h.assertTrue(p.getMainHandItem().is(HouseReadRegistry.ROUTINES.get(first[0]).get())&&HouseReadSites.loan(p.getMainHandItem(),p.getUUID())&&f.in.getBlockState(a).getValue(HouseReadPropBlock.LIFTED)==1,"the chosen item comes into the hand and leaves its place on the counter");
              h.assertTrue(HouseRead.raw(f.data(),p.getUUID(),HouseRead.Trait.HAIR).equals(new HouseRead.Category(HouseReadMoments.HAIR[HouseReadMoments.set(first[0])],HouseRead.Strength.TENTATIVE)),"the first item sets the routine, tentatively");
              var c=b[0].offset(HouseReadRemainingRooms.ROUTINE_PLACES.get(2));int gc=HouseReadPropBlock.routine(f.in.getBlockState(c).getValue(HouseReadPropBlock.PART));
              p.gameMode.useItemOn(p,p.serverLevel(),p.getMainHandItem(),InteractionHand.MAIN_HAND,new BlockHitResult(new Vec3(c.getX()+12/16.0,c.getY()+.15,c.getZ()+.5),Direction.UP,c,false));second[0]=HouseReadMoments.PAIRS[gc][1];
              h.assertTrue(p.getMainHandItem().is(HouseReadRegistry.ROUTINES.get(second[0]).get())&&f.in.getBlockState(a).getValue(HouseReadPropBlock.LIFTED)==0&&f.in.getBlockState(c).getValue(HouseReadPropBlock.LIFTED)==2,"picking another puts the first back: only one is ever in the hand");
              // 0.4.167: the first tool can be taken up again, and put back by its own place, before anything is used at the glass.
              p.gameMode.useItemOn(p,p.serverLevel(),p.getMainHandItem(),InteractionHand.MAIN_HAND,new BlockHitResult(new Vec3(a.getX()+3/16.0,a.getY()+.15,a.getZ()+.5),Direction.UP,a,false));
              h.assertTrue(p.getMainHandItem().is(HouseReadRegistry.ROUTINES.get(first[0]).get())&&f.in.getBlockState(a).getValue(HouseReadPropBlock.LIFTED)==1&&f.in.getBlockState(c).getValue(HouseReadPropBlock.LIFTED)==0,"a tool put back can be picked up again");
              p.gameMode.useItemOn(p,p.serverLevel(),p.getMainHandItem(),InteractionHand.MAIN_HAND,new BlockHitResult(new Vec3(c.getX()+12/16.0,c.getY()+.15,c.getZ()+.5),Direction.UP,c,false));
              h.assertTrue(p.getInventory().items.stream().filter(i->i.getItem()!=Items.AIR&&HouseReadSites.loan(i,p.getUUID())).count()==1,"never more than one routine item is lent");
              h.assertTrue(HouseRead.raw(f.data(),p.getUUID(),HouseRead.Trait.HAIR).strength()==HouseRead.Strength.UNCERTAIN,"changing one's mind to another set makes the read uncertain");
              // Use it at the mirror: the routine is done, and it ends the bathroom.
              click(p,b[0].offset(HouseReadRemainingRooms.BATH_MIRROR));
              h.assertTrue(HouseRead.raw(f.data(),p.getUUID(),HouseRead.Trait.HAIR).equals(new HouseRead.Category(HouseReadMoments.HAIR[HouseReadMoments.set(second[0])],HouseRead.Strength.SETTLED))&&HouseRead.seen(f.data(),p.getUUID()).getCompound("bathroom").getInt("Used")==second[0],"using the item in hand at the mirror decides the routine");
              var d=b[0].offset(HouseReadRemainingRooms.ROUTINE_PLACES.get(1));p.gameMode.useItemOn(p,p.serverLevel(),p.getMainHandItem(),InteractionHand.MAIN_HAND,new BlockHitResult(new Vec3(d.getX()+.5,d.getY()+.15,d.getZ()+.5),Direction.UP,d,false));
              h.assertTrue(p.getMainHandItem().is(HouseReadRegistry.ROUTINES.get(second[0]).get())&&f.in.getBlockState(d).getValue(HouseReadPropBlock.LIFTED)==0,"after the routine nothing else on the counter can be played with");
              HouseReadExport.save(f.in,b[0],HouseReadSites.Room.BATHROOM,"bathroom_before_bed");
          })
          .thenWaitUntil(()->h.assertTrue(visit(p,HouseReadSites.Room.BATHROOM).getBoolean("LightOff")&&HouseReadMoments.canProceed(p,HouseReadSites.Room.BATHROOM),"the light goes out by itself and the bedroom door opens"))
          .thenExecute(()->{for(var rel:HouseReadRemainingRooms.BATH_LIGHTS)h.assertTrue(f.in.getBlockState(b[0].offset(rel)).getValue(HouseReadPropBlock.TURNED),"both actual lights are off: "+rel);
              h.assertTrue(HouseReadProse.bathroom(HouseRead.seen(f.data(),p.getUUID()).getCompound("bathroom")).startsWith("reached for the "+HouseReadMoments.ROUTINES[first[0]]+", then "),"the account says where the hand went first and what was done: "+HouseReadProse.bathroom(HouseRead.seen(f.data(),p.getUUID()).getCompound("bathroom")));})
          .thenIdle(100).thenExecute(()->{h.assertTrue(HouseReadSites.inside(p)==HouseReadSites.Room.BATHROOM&&!HouseReadStory.closing(p),"in the dark the bathroom waits for the reader to walk through to the bedroom; no timer ends it first");stand(p,f.in,b[0],4.5,0,-11.5);})
          .thenWaitUntil(()->h.assertTrue(HouseReadSites.inside(p)==null&&!LabyrinthDoors.isBusy(p),"walking through to the bedroom in the dark, the House takes the reader on"))
          .thenExecute(()->{var shown=HouseReadStory.last(p.getUUID());h.assertTrue(shown!=null&&shown.text().equals(HouseReadStory.closing(HouseReadSites.Room.BATHROOM))&&shown.sound().equals("minecraft:block.lever.click"),"the bathroom is cut off with its own switch and the House's closing line: "+shown);
              h.assertTrue(ItemStack.matches(own[0],p.getMainHandItem())&&p.getInventory().items.stream().noneMatch(i->HouseReadSites.loan(i,p.getUUID())),"the reader's own item is back in their hand and no routine item leaves the bathroom");
              for(var rel:HouseReadRemainingRooms.ROUTINE_PLACES)h.assertTrue(f.in.getBlockState(b[0].offset(rel)).getValue(HouseReadPropBlock.LIFTED)==0,"every routine is back in its place: "+rel);})
          .thenSucceed();
    }

    @GameTest(template="empty",batch="read_light",timeoutTicks=5000)
    public static void theBedroomOpensWithoutTouchingAnyRoutineAndThePostcardNeverChangesInView(GameTestHelper h){
        var f=fixture(h);var p=player(h,f,"night_reader");var at=f.origin.offset(0,0,-5);f.chunks.hold(f.in,new AABB(at).inflate(8));BlockPos[] b={null};LabyrinthData.Door[] from={null};h.onEachTick(()->transition(p));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"source loaded")).thenExecute(()->{from[0]=source(f);stand(p,f.in,at,.5,0,2);HouseReadSites.prepare(p,HouseReadSites.Room.BATHROOM);})
          .thenWaitUntil(()->h.assertTrue(ready(p,HouseReadSites.Room.BATHROOM),"bathroom ready"))
          .thenExecute(()->{b[0]=HouseReadSites.base(p.server,p.getUUID(),HouseReadSites.Room.BATHROOM);seed(p,HouseReadSites.Room.BATHROOM,from[0]);stand(p,f.in,b[0],.5,0,-.5);p.lookAt(EntityAnchorArgument.Anchor.EYES,b[0].offset(HouseReadRemainingRooms.POSTCARD_AT).getCenter());click(p,b[0].offset(HouseReadRemainingRooms.SWITCH_AT));h.assertTrue(visit(p,HouseReadSites.Room.BATHROOM).getInt("Touches")==0&&!HouseReadMoments.canProceed(p,HouseReadSites.Room.BATHROOM)&&!f.in.getBlockState(b[0].offset(HouseReadMoments.exit(HouseReadSites.Room.BATHROOM))).getValue(DoorBlock.OPEN),"0.4.166: the switch alone no longer opens the bedroom; the House waits for the routine");})
          .thenIdle(8).thenExecute(()->{h.assertTrue(HouseRead.seen(f.data(),p.getUUID()).getCompound("bathroom").getBoolean("Untouched")&&HouseRead.raw(f.data(),p.getUUID(),HouseRead.Trait.HAIR).strength()==HouseRead.Strength.UNKNOWN,"the light turned off without touching anything is kept, and reads no routine yet");h.assertTrue(!visit(p,HouseReadSites.Room.BATHROOM).getBoolean("Written"),"the watched lake postcard remains unchanged");p.setYRot(0);p.setXRot(0);})
          .thenWaitUntil(()->h.assertTrue(visit(p,HouseReadSites.Room.BATHROOM).getBoolean("Written"),"the original postcard gains its writing only unseen"))
          .thenExecute(()->{stand(p,f.in,b[0],.5,0,-6.5);click(p,b[0].offset(HouseReadRemainingRooms.POSTCARD_AT));h.assertTrue(p.containerMenu instanceof LecternMenu&&!p.containerMenu.clickMenuButton(p,3),"the original postcard has actual read-only pages");p.closeContainer();for(var room:HouseReadSites.Room.values()){var words=String.join(" ",HouseReadTexts.book(room).get(DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().map(page->page.raw().getString()).toList());h.assertTrue(words.equals(HouseReadTexts.text(room).replace("\n\n"," ")),"all five owner's passages remain verbatim after native pagination");h.assertTrue(WitnessAccount.Story.of(room.place().id())==null,"a quiet first room is never an extra Witness source");}
              stand(p,f.in,b[0],.5,0,1);click(p,b[0].offset(HouseReadRooms.COAT_ENTRY));h.assertTrue(HouseReadSites.inside(p)==HouseReadSites.Room.BATHROOM&&!LabyrinthDoors.isBusy(p),"the door back is locked in the dark as well");
              // In the dark the routine is still the routine: the first item used at the glass is read, and it opens the bedroom.
              stand(p,f.in,b[0],.5,0,-7.5);var a=b[0].offset(HouseReadRemainingRooms.ROUTINE_PLACES.get(0));int ga=HouseReadPropBlock.routine(f.in.getBlockState(a).getValue(HouseReadPropBlock.PART));
              p.gameMode.useItemOn(p,p.serverLevel(),p.getMainHandItem(),InteractionHand.MAIN_HAND,new BlockHitResult(new Vec3(a.getX()+3/16.0,a.getY()+.15,a.getZ()+.5),Direction.UP,a,false));click(p,b[0].offset(HouseReadRemainingRooms.BATH_MIRROR));
              h.assertTrue(HouseReadMoments.canProceed(p,HouseReadSites.Room.BATHROOM)&&HouseRead.raw(f.data(),p.getUUID(),HouseRead.Trait.HAIR).value().equals(HouseReadMoments.HAIR[HouseReadMoments.set(HouseReadMoments.PAIRS[ga][0])])&&HouseReadLook.of(p).hair()==HouseReadMoments.PAIRS[ga][0],"a routine used at the glass with the light off is read, the room has its answer, and the House keeps the style for its version of the reader");
              stand(p,f.in,b[0],.5,0,1);click(p,b[0].offset(HouseReadRooms.COAT_ENTRY));})
          .thenWaitUntil(()->h.assertTrue(HouseReadSites.inside(p)==null&&!LabyrinthDoors.isBusy(p),"then the door back opens, after the closing line"))
          .thenExecute(()->h.assertTrue(p.getHealth()==20&&f.data().returnCount(p.getUUID())==0,"no wrong answer, health charge or story credit")).thenSucceed();
    }

    @GameTest(template="empty",batch="read_shelter",timeoutTicks=7000)
    public static void theTwoOriginalStrangersKeepTheirPrivacyAndDistanceThroughTheWholeSlowSong(GameTestHelper h){
        var f=fixture(h);var p=player(h,f,"shelter_reader");var peer=player(h,f,"shelter_peer");var at=f.origin.offset(0,0,-5);f.chunks.hold(f.in,new AABB(at).inflate(8));BlockPos[] b={null};LabyrinthData.Door[] from={null};UUID[] cast={null,null};double[] start={0,0};h.onEachTick(()->transition(p));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"source loaded")).thenExecute(()->{from[0]=source(f);stand(p,f.in,at,.5,0,2);stand(peer,f.in,at,2.5,0,2);HouseReadSites.prepare(p,HouseReadSites.Room.SHELTER);})
          .thenWaitUntil(()->h.assertTrue(ready(p,HouseReadSites.Room.SHELTER),"the complete dry pavilion and lake set finish"))
          .thenExecute(()->{b[0]=HouseReadSites.base(p.server,p.getUUID(),HouseReadSites.Room.SHELTER);f.chunks.hold(f.in,HouseReadRooms.bounds(b[0],HouseReadSites.Room.SHELTER));HouseReadAudit.check(h,f.in,b[0],HouseReadSites.Room.SHELTER);seed(p,HouseReadSites.Room.SHELTER,from[0]);stand(p,f.in,b[0],.5,0,-3);h.assertTrue(!HouseReadMoments.canProceed(p,HouseReadSites.Room.SHELTER),"0.4.166: the shelter's doors are locked until the song has played and the House has its answer");})
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
              h.assertTrue(HouseReadMoments.canProceed(p,HouseReadSites.Room.SHELTER),"staying out by the lantern through the song is an answer, and the doors open");
              // As the last quiet room it ends on the House's warning (the other four marked entered).
              var own=HouseReadSites.state(p.server,p.getUUID());for(var room:HouseReadSites.Room.values()){var visit=own.getCompound(room.key());visit.putBoolean("Entered",true);own.put(room.key(),visit);}f.data().setStateEntry(HouseReadSites.STATE,p.getUUID().toString(),own);
              HouseReadExport.save(f.in,b[0],HouseReadSites.Room.SHELTER,"lakeside_shelter");stand(p,f.in,b[0],-2.5,0,.5);click(p,b[0].offset(HouseReadRooms.entry(HouseReadSites.Room.SHELTER)));
              var shown=HouseReadStory.last(p.getUUID());h.assertTrue(shown!=null&&shown.title()&&shown.text().equals(HouseReadStory.closing(HouseReadSites.Room.SHELTER)+HouseReadStory.PAGE+HouseReadStory.HAUNTED+HouseReadStory.PAGE+HouseReadStory.WARNING_LEAD+HouseReadStory.PAGE+HouseReadStory.WARNING),"the last quiet room ends on the warning that this house has ghosts in it: "+shown);
          }).thenWaitUntil(()->h.assertTrue(HouseReadSites.inside(p)==null&&!LabyrinthDoors.isBusy(p),"and then the House takes the reader on"))
          .thenExecute(()->{h.assertTrue(p.getHealth()==20&&f.data().returnCount(p.getUUID())==0,"neither solitude nor company punishes or rewards the reader");h.assertTrue(HouseReadSites.state(p.server,p.getUUID()).getCompound(HouseReadSites.Room.SHELTER.key()).getUUID("Man").equals(cast[0]),"leaving never restages or replaces the original cast");}).thenSucceed();
    }
}
