package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.232, the familiar room: one kitchen, the same for everyone, dealt like a hall and far more readily to a reader while someone else
 * is in it; its pantry keeps what is left in it, its chairs stay where they are pulled, its clock is heard through a door that leads to
 * it; and its window looks down on a yard laid out, for each reader, like the last halls they walked, one more each visit.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_exploration") @PrefixGameTestTemplate(false)
public final class FamiliarRoomTests {
    private FamiliarRoomTests(){}
    private static LiteraryTests.Fixture active;
    private static void close(){if(active!=null){active.close();active=null;}}
    @AfterBatch(batch="familiar_kitchen") public static void doneKitchen(ServerLevel l){close();}
    @AfterBatch(batch="familiar_window") public static void doneWindow(ServerLevel l){close();}
    @AfterBatch(batch="familiar_return") public static void doneReturn(ServerLevel l){close();}
    @AfterBatch(batch="familiar_wedge") public static void doneWedge(ServerLevel l){close();}
    @AfterBatch(batch="familiar_use") public static void doneUse(ServerLevel l){close();KitchenFittings.clearAll();}
    private static void hold(LiteraryTests.Fixture f,LabyrinthPlace place){var slot=LabyrinthPlaces.slotBounds(f.origin,place);f.chunks.hold(f.in,new AABB(slot.minX(),slot.minY(),slot.minZ(),slot.maxX()+1,slot.maxY()+1,slot.maxZ()+1));}

    @GameTest(template="empty",batch="familiar_kitchen",timeoutTicks=600)
    public static void theKitchenIsOneRoomForEveryoneAndKeepsWhatIsLeftInIt(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var place=LabyrinthPlace.FAMILIAR_KITCHEN;var b=f.base(place);var l=f.in;hold(f,place);
        var p=NativeTestPlayers.survival(h,"kitchen_reader");f.players.add(p);var q=NativeTestPlayers.survival(h,"kitchen_other");f.players.add(q);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the kitchen is resident"))
            .thenExecute(()->{
                h.assertTrue(place.authored()&&!place.isVignette()&&WitnessAccount.Story.of(place.id())==null&&!SceneInstances.copyable(place),"one kitchen, built whole, the same for everyone, and no story");
                FamiliarRoom.build(l,b);LabyrinthBuilder.registerDoors(f.data(),place,b);
                h.assertTrue(f.data().door(place.id()+"/far")!=null&&f.data().door(place.id()+"/side")!=null,"two ways on besides the way in");
                h.assertTrue(DoorSounds.carries(place)==DoorSounds.Carry.CLOCK,"its clock is heard through a door that leads to it");
                h.assertTrue(!l.getEntitiesOfClass(ItemFrame.class,new AABB(b.offset(FamiliarRoom.CLOCK))).isEmpty(),"the clock hangs on the wall");
                // The pantry: stocked once; what is taken stays taken, and what is left stays.
                var pantry=(BarrelBlockEntity)l.getBlockEntity(b.offset(FamiliarRoom.PANTRY));
                h.assertTrue(pantry!=null&&pantry.getItem(0).is(Items.BREAD),"a little in the pantry");
                pantry.removeItem(0,3);pantry.setItem(8,new net.minecraft.world.item.ItemStack(Items.STRING,4));pantry.setChanged();
                FamiliarRoom.build(l,b);pantry=(BarrelBlockEntity)l.getBlockEntity(b.offset(FamiliarRoom.PANTRY));
                h.assertTrue(pantry.getItem(0).isEmpty()&&pantry.getItem(8).is(Items.STRING),"what was taken stays taken, and what was left stays: "+pantry.getItem(0)+" "+pantry.getItem(8));
                // The chairs at the table are the loose chairs: pulled, they stay where they are left.
                var chair=b.offset(FamiliarRoom.TABLE.south());h.assertTrue(TraceBlock.is(l.getBlockState(chair),TraceBlock.Kind.CHAIR),"a chair at the table");
                p.teleportTo(l,chair.getX()+.5,chair.getY(),chair.getZ()+2.5,0,0);
                h.assertTrue(TraceRooms.chair(p,chair,l.getBlockState(chair))&&TraceBlock.is(l.getBlockState(chair.south()),TraceBlock.Kind.CHAIR),"pulled back from the table, it stays there");
                // Dealt from the third crossing; far more readily while somebody else is in it.
                h.assertTrue(FamiliarRoom.weight(p.getUUID(),2)==0&&FamiliarRoom.weight(p.getUUID(),5)==FamiliarRoom.WEIGHT,"from the third crossing, now and then");
                q.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-4.5,0,0);p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()+40.5,0,0);
            })
            .thenIdle(12)
            .thenExecute(()->{
                h.assertTrue(FamiliarRoom.weight(p.getUUID(),5)==FamiliarRoom.MEET,"with someone already in it, far more readily");
                h.assertTrue(FamiliarRoom.weight(q.getUUID(),5)==FamiliarRoom.WEIGHT,"but not more readily for the one who is there");
                // 0.4.239: through its door, someone in there for the reader outside; the clock for the one inside.
                h.assertTrue(FamiliarRoom.heard(p.getUUID()).carry()==DoorSounds.Carry.SOMEONE&&FamiliarRoom.heard(q.getUUID()).carry()==DoorSounds.Carry.CLOCK,"someone in there, heard from outside; for them, the clock");
            })
            .thenSucceed();
    }

    @GameTest(template="empty",batch="familiar_window",timeoutTicks=600)
    public static void theWindowShowsTheHallsTheReaderWalkedFromAbove(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var place=LabyrinthPlace.FAMILIAR_KITCHEN;var b=f.base(place);var l=f.in;hold(f,place);hold(f,LabyrinthPlace.DUST_HALL);
        var p=NativeTestPlayers.survival(h,"window_reader");f.players.add(p);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the kitchen and a hall are resident"))
            .thenExecute(()->{
                FamiliarRoom.build(l,b);LabyrinthBuilder.registerDoors(f.data(),place,b);
                TraceRooms.buildDust(l,f.base(LabyrinthPlace.DUST_HALL));
                var data=f.data();
                // An ordinary hall walked is remembered; a puzzle room is not a passage the window shows.
                FamiliarRoom.walked(p,LabyrinthPlace.DUST_HALL);FamiliarRoom.walked(p,LabyrinthPlace.PROPPED_DOOR);
                // The first visit: only the yard.
                FamiliarRoom.arrived(p);h.assertTrue(FamiliarRoom.shown(data,p.getUUID()).isEmpty(),"the first time, there is only the yard");
                // The next: the last hall walked, laid out below from above.
                FamiliarRoom.arrived(p);var shown=FamiliarRoom.shown(data,p.getUUID());
                h.assertTrue(shown.size()==1&&shown.get(0)==LabyrinthPlace.DUST_HALL,"the next time, the hall the reader last walked: "+shown);
                var plan=FamiliarRoom.plan(l,f.origin,LabyrinthPlace.DUST_HALL);h.assertTrue(plan!=null&&plan.length==12,"its floor seen from above at a third of its size");
                var cells=FamiliarRoom.yard(l,f.origin,shown);
                h.assertTrue(cells.size()>8,"the yard laid out like it: "+cells.size());
                h.assertTrue(cells.stream().allMatch(c->c.getY()==FamiliarRoom.GROUND_Y&&c.getX()<=FamiliarRoom.YARD_EAST&&c.getX()>=FamiliarRoom.YARD_WEST&&c.getZ()<=-1&&c.getZ()>=-4),"on the ground below the window, in the nearest lane");
                h.assertTrue(l.getBlockState(b.offset(cells.iterator().next())).is(net.minecraft.world.level.block.Blocks.DEEPSLATE_TILES),"for that reader alone: the yard itself is unchanged");
            })
            .thenSucceed();
    }

    /**
     * 0.4.238: the window's yard is only on the reader's client, so a reader who reconnects in the kitchen, or whose client is sent the
     * yard's chunk afresh, is shown it again; reconnecting is not a visit, and logging out forgets what was waiting to be shown.
     */
    @GameTest(template="empty",batch="familiar_return",timeoutTicks=600)
    public static void theWindowIsShownAgainAfterAReconnect(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var place=LabyrinthPlace.FAMILIAR_KITCHEN;var b=f.base(place);var l=f.in;hold(f,place);hold(f,LabyrinthPlace.DUST_HALL);
        var p=NativeTestPlayers.survival(h,"return_reader");f.players.add(p);var id=p.getUUID();
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the kitchen and a hall are resident"))
            .thenExecute(()->{
                FamiliarRoom.build(l,b);LabyrinthBuilder.registerDoors(f.data(),place,b);TraceRooms.buildDust(l,f.base(LabyrinthPlace.DUST_HALL));
                // A first visit (only the yard) already made; walking in again is the second, counted by the House's own room tracking.
                FamiliarRoom.walked(p,LabyrinthPlace.DUST_HALL);FamiliarRoom.arrived(p);
                p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-4.5,0,0);
            })
            .thenWaitUntil(()->h.assertTrue(FamiliarRoom.visits(f.data(),id)==2&&FamiliarRoom.viewed(id)&&!FamiliarRoom.pending(id),"walking in is the second visit, and the yard is laid out at the window: visits "+FamiliarRoom.visits(f.data(),id)))
            .thenExecute(()->{
                // Logging out forgets the view; logging back in, still in the kitchen, lays it out again.
                FamiliarRoom.loggedOut(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(p));
                h.assertTrue(!FamiliarRoom.viewed(id)&&!FamiliarRoom.pending(id),"logging out forgets it");
                FamiliarRoom.loggedIn(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent(p));
                h.assertTrue(FamiliarRoom.pending(id),"logging back in at the window: shown again");
            })
            // Long enough for the room tracking to look again: the reconnect is not another visit.
            .thenIdle(25)
            .thenExecute(()->h.assertTrue(FamiliarRoom.viewed(id)&&!FamiliarRoom.pending(id)&&FamiliarRoom.visits(f.data(),id)==2,"laid out again after the reconnect, and not another visit: visits "+FamiliarRoom.visits(f.data(),id)))
            .thenExecute(()->{
                // The yard's chunk sent to the client afresh (it came back into view): the view goes with it, so it is laid out again.
                FamiliarRoom.chunkSent(new net.neoforged.neoforge.event.level.ChunkWatchEvent.Sent(p,l.getChunkAt(b.offset(FamiliarRoom.YARD_EAST-4,FamiliarRoom.GROUND_Y,-3)),l));
                h.assertTrue(FamiliarRoom.pending(id),"the yard's chunk sent again: laid out again");
            })
            .thenWaitUntil(()->h.assertTrue(FamiliarRoom.viewed(id)&&!FamiliarRoom.pending(id),"laid out after the chunk"))
            .thenExecute(()->{
                // Away from the kitchen, nothing is laid out.
                p.teleportTo(l,f.base(LabyrinthPlace.DUST_HALL).getX()+.5,f.base(LabyrinthPlace.DUST_HALL).getY(),f.base(LabyrinthPlace.DUST_HALL).getZ()-3.5,0,0);
                FamiliarRoom.loggedOut(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(p));
                FamiliarRoom.loggedIn(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent(p));
                h.assertTrue(!FamiliarRoom.pending(id)&&FamiliarRoom.visits(f.data(),id)==2,"reconnecting elsewhere lays out nothing");
            })
            .thenSucceed();
    }

    /**
     * 0.4.239: a wedge in the kitchen's entry still holds it on the hall it was set from, for its owner; a reader coming from anywhere
     * else is not kept out of the room people meet in, but comes in by its side door, hears their knock come out there, and goes back
     * out the way they came in.
     */
    @GameTest(template="empty",batch="familiar_wedge",timeoutTicks=600)
    public static void aWedgedKitchenStillLetsOthersInByAnotherDoor(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var place=LabyrinthPlace.FAMILIAR_KITCHEN;var b=f.base(place);var l=f.in;hold(f,place);
        var p=NativeTestPlayers.survival(h,"kitchen_wedger");f.players.add(p);var q=NativeTestPlayers.survival(h,"kitchen_comer");f.players.add(q);
        // Two halls the kitchen is dealt behind, a little south of it in its own ground: the owner's and the other reader's.
        var xAt=b.offset(-3,0,12);var yAt=b.offset(3,0,12);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the kitchen is resident"))
            .thenExecute(()->{
                FamiliarRoom.build(l,b);var data=f.data();LabyrinthBuilder.registerDoors(data,place,b);
                for(var at:new BlockPos[]{xAt,yAt}){for(int x=-3;x<=3;x++)for(int z=-1;z<=3;z++)l.setBlock(at.offset(x,-1,z),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),3);LabyrinthBuilder.placeDoor(l,at,net.minecraft.core.Direction.SOUTH);}
                var hallX=new LabyrinthData.Door("test/kitchen_x",l.dimension(),xAt,net.minecraft.core.Direction.SOUTH,"place:"+place.id(),false);
                var hallY=new LabyrinthData.Door("test/kitchen_y",l.dimension(),yAt,net.minecraft.core.Direction.SOUTH,"place:"+place.id(),false);
                data.putDoor(hallX);data.putDoor(hallY);
                var entry=data.door(place.entryDoorId());var side=data.door(place.id()+"/side");h.assertTrue(entry!=null&&side!=null,"the kitchen's doors are known");
                // The owner came in from hall X and wedges the entry behind them.
                data.pushReturn(p.getUUID(),new LabyrinthData.Waypoint(l.dimension(),net.minecraft.world.phys.Vec3.atBottomCenterOf(xAt),0,true,false));
                p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-3.5,0,0);p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(TraceRegistry.WEDGE.get()));
                h.assertTrue(Wedges.wedge(p,l,entry.lower)&&Wedges.at(l,entry.lower),"the wedge holds the kitchen's door on hall X");
                h.assertTrue(Wedges.way(p,hallX,entry,place)==entry,"from hall X, in by the door as ever");
                // From hall Y the knock comes out at the side door, where the reader would come in.
                DoorSounds.clearAll();q.teleportTo(l,yAt.getX()+.5,yAt.getY(),yAt.getZ()+2.5,180,0);
                h.assertTrue(DoorSounds.knock(q,data,hallY)&&DoorSounds.heard().stream().anyMatch(s->s.what().equals("knock_far")&&s.at().pos().equals(side.lower)),"knocking from hall Y, it is heard at the side door: "+DoorSounds.heard());
                // Through hall Y's door: not kept out, but in by the side door, the wedge where it was.
                LabyrinthDoors.use(q,hallY);
                h.assertTrue(Wedges.cameInBy(q,side)&&Wedges.cameInOtherWay(data,q.getUUID(),place),"in by the side door");
                h.assertTrue(q.getX()>b.getX()+6.5&&Math.abs(q.getZ()-(b.getZ()-5.5))<3.5&&LabyrinthPlaces.placeAt(f.origin,q.blockPosition())==place,"standing outside the side door, in the kitchen's own ground: "+q.position());
                h.assertTrue(Wedges.at(l,entry.lower)&&l.getBlockState(entry.lower).getValue(net.minecraft.world.level.block.DoorBlock.OPEN),"the wedge still holds the entry open on hall X");
                h.assertTrue(l.getBlockState(side.lower).getValue(net.minecraft.world.level.block.DoorBlock.OPEN),"the side door stands open for them");
                // In, and back out the way they came: to hall Y.
                q.teleportTo(l,b.getX()+3.5,b.getY(),b.getZ()-5.5,90,0);LabyrinthDoors.tickPlayer(q,f.origin);
                q.teleportTo(l,b.getX()+8.5,b.getY(),b.getZ()-5.5,-90,0);LabyrinthDoors.tickPlayer(q,f.origin);
            })
            .thenWaitUntil(()->h.assertTrue(q.position().distanceTo(net.minecraft.world.phys.Vec3.atBottomCenterOf(yAt))<4&&!Wedges.cameInOtherWay(f.data(),q.getUUID(),place),"back out by the side door, they are at hall Y's door: "+q.position()))
            .thenSucceed();
    }

    /**
     * 0.4.239: the kitchen in ordinary use, and calm. A chair sat in from its seat and pulled by its back, and not pulled out from under
     * someone; the tap filling a bottle and running over the hands; the stove cooking a little at a time and burning nothing; the fridge
     * keeping what is left in it; the table keeping what is set on it, for anyone to take.
     */
    @GameTest(template="empty",batch="familiar_use",timeoutTicks=900)
    public static void theKitchenIsForUseAndNothingFrightensAnyoneInIt(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var place=LabyrinthPlace.FAMILIAR_KITCHEN;var b=f.base(place);var l=f.in;hold(f,place);KitchenFittings.clearAll();
        var p=NativeTestPlayers.survival(h,"kitchen_cook");f.players.add(p);var q=NativeTestPlayers.survival(h,"kitchen_guest");f.players.add(q);
        var hand=net.minecraft.world.InteractionHand.MAIN_HAND;
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the kitchen is resident"))
            .thenExecute(()->{
                FamiliarRoom.build(l,b);LabyrinthBuilder.registerDoors(f.data(),place,b);
                h.assertTrue(LabyrinthPacing.calm(place)&&!LabyrinthPacing.quiet(place),"the kitchen is calm: no growls, no hesitating pets");
                p.teleportTo(l,b.getX()+1.5,b.getY(),b.getZ()-5.5,0,0);q.teleportTo(l,b.getX()+2.5,b.getY(),b.getZ()-9.5,0,0);
                // A chair: sat in from the seat, facing the table; not pulled out from under anyone; pulled by its back once empty.
                var chair=b.offset(FamiliarRoom.TABLE.south());
                var seat=new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atBottomCenterOf(chair).add(0,TraceBlock.SEAT,0),net.minecraft.core.Direction.UP,chair,false);
                var back=new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atBottomCenterOf(chair).add(0,1.1,.4),net.minecraft.core.Direction.SOUTH,chair,false);
                h.assertTrue(TraceBlock.onSeat(chair,seat)&&!TraceBlock.onSeat(chair,back),"a hand on the seat sits; a hand on the back pulls");
                h.assertTrue(TraceRooms.sitOn(p,chair)&&p.getVehicle() instanceof io.github.knaitoe.theoldesthouse.house.SeatEntity,"sitting at the table");
                h.assertTrue(Math.abs(net.minecraft.util.Mth.wrapDegrees(p.getYRot()-net.minecraft.core.Direction.NORTH.toYRot()))<1,"facing the table: "+p.getYRot());
                h.assertTrue(TraceRooms.chair(q,chair,l.getBlockState(chair))&&TraceBlock.is(l.getBlockState(chair),TraceBlock.Kind.CHAIR),"nobody pulls it out from under them");
                p.stopRiding();
                p.teleportTo(l,b.getX()+1.5,b.getY(),b.getZ()-5.5,0,0);
                // The tap: a bottle filled; and run over the hands, it puts out anything burning on them.
                p.setItemInHand(hand,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.GLASS_BOTTLE));
                h.assertTrue(KitchenFittings.use(p,l,b,KitchenFittings.Fitting.SINK,b.offset(FamiliarRoom.SINK),net.minecraft.world.phys.Vec3.atCenterOf(b.offset(FamiliarRoom.SINK))),"the tap");
                h.assertTrue(p.getInventory().countItem(net.minecraft.world.item.Items.POTION)==1&&p.getInventory().countItem(net.minecraft.world.item.Items.GLASS_BOTTLE)==0,"a bottle of water");
                p.setItemInHand(hand,net.minecraft.world.item.ItemStack.EMPTY);
                p.igniteForSeconds(5);KitchenFittings.use(p,l,b,KitchenFittings.Fitting.SINK,b.offset(FamiliarRoom.SINK),net.minecraft.world.phys.Vec3.atCenterOf(b.offset(FamiliarRoom.SINK)));
                h.assertTrue(!p.isOnFire(),"the water puts it out");
                // The stove: four at a time.
                p.setItemInHand(hand,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BEEF,6));
                h.assertTrue(KitchenFittings.use(p,l,b,KitchenFittings.Fitting.STOVE,b.offset(KitchenFittings.STOVE),net.minecraft.world.phys.Vec3.ZERO)&&KitchenFittings.cooking(l.getServer())&&p.getMainHandItem().getCount()==2,"four on the stove, two left in hand");
                h.assertTrue(!KitchenFittings.done(l),"not done at once");
                p.setItemInHand(hand,net.minecraft.world.item.ItemStack.EMPTY);KitchenFittings.use(p,l,b,KitchenFittings.Fitting.STOVE,b.offset(KitchenFittings.STOVE),net.minecraft.world.phys.Vec3.ZERO);
                h.assertTrue(p.getInventory().countItem(net.minecraft.world.item.Items.COOKED_BEEF)==0&&KitchenFittings.cooking(l.getServer()),"and not taken off before it is");
                // The fridge keeps what is left in it, for whoever comes next, and as the House remembers it.
                var fridge=KitchenFittings.store(l,"fridge",27,b.offset(FamiliarRoom.FRIDGE));fridge.setItem(4,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.MILK_BUCKET));
                KitchenFittings.clearAll();
                h.assertTrue(KitchenFittings.store(l,"fridge",27,b.offset(FamiliarRoom.FRIDGE)).getItem(4).is(net.minecraft.world.item.Items.MILK_BUCKET),"left in the fridge, it is there again");
                KitchenFittings.use(q,l,b,KitchenFittings.Fitting.FRIDGE,b.offset(FamiliarRoom.FRIDGE),net.minecraft.world.phys.Vec3.ZERO);
                h.assertTrue(q.containerMenu instanceof net.minecraft.world.inventory.ChestMenu menu&&menu.getContainer().getItem(4).is(net.minecraft.world.item.Items.MILK_BUCKET),"and for the other reader too");q.closeContainer();
                // The table keeps what is set on it, for anyone to take.
                var table=b.offset(FamiliarRoom.TABLE);var top=net.minecraft.world.phys.Vec3.atBottomCenterOf(table).add(0,1,0);
                p.setItemInHand(hand,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BREAD,2));
                KitchenFittings.use(p,l,b,KitchenFittings.Fitting.TABLE,table,top);
                h.assertTrue(p.getMainHandItem().isEmpty()&&KitchenFittings.lying(l,table).size()==1&&KitchenFittings.item(l,KitchenFittings.lying(l,table).get(0)).getCount()==2,"two loaves left on the table");
                for(int i=0;i<4;i++){p.setItemInHand(hand,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.APPLE));KitchenFittings.use(p,l,b,KitchenFittings.Fitting.TABLE,table,top);}
                h.assertTrue(KitchenFittings.lying(l,table).size()==KitchenFittings.TABLE_SPOTS.length&&p.getMainHandItem().is(net.minecraft.world.item.Items.APPLE),"no room left on the table, and the last apple still in hand");
                q.setItemInHand(hand,net.minecraft.world.item.ItemStack.EMPTY);
                var bread=KitchenFittings.lying(l,table).stream().filter(d->KitchenFittings.item(l,d).is(net.minecraft.world.item.Items.BREAD)).findFirst().orElseThrow();
                KitchenFittings.use(q,l,b,KitchenFittings.Fitting.TABLE,table,bread.position());
                h.assertTrue(q.getInventory().countItem(net.minecraft.world.item.Items.BREAD)==2&&KitchenFittings.lying(l,table).size()==KitchenFittings.TABLE_SPOTS.length-1,"the other reader takes the bread");
            })
            .thenIdle(KitchenFittings.COOK+20)
            .thenExecute(()->{
                h.assertTrue(KitchenFittings.done(l),"done");
                p.setItemInHand(hand,net.minecraft.world.item.ItemStack.EMPTY);KitchenFittings.use(p,l,b,KitchenFittings.Fitting.STOVE,b.offset(KitchenFittings.STOVE),net.minecraft.world.phys.Vec3.ZERO);
                h.assertTrue(p.getInventory().countItem(net.minecraft.world.item.Items.COOKED_BEEF)==4&&!KitchenFittings.cooking(l.getServer()),"four cooked, off the stove, and nothing burnt");
            })
            .thenSucceed();
    }
}
