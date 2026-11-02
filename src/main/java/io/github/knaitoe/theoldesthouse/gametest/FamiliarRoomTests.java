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
}
