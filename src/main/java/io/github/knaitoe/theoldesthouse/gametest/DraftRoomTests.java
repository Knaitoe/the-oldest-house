package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.230, drafts and pressure, played with a real reader's hands: in the drying room the air goes where it can (nowhere with the vent
 * shut, against the shut inner door, out of the open window) and opens the far door only when it has nothing else; in the vent rooms
 * the seam in plain plaster is plaster until the air is drawn to it, and then it gives.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_exploration") @PrefixGameTestTemplate(false)
public final class DraftRoomTests {
    private DraftRoomTests(){}
    private static LiteraryTests.Fixture active;
    @AfterBatch(batch="draft_rooms") public static void done(ServerLevel l){if(active!=null){active.close();active=null;}}
    @AfterBatch(batch="airing_rooms") public static void airingDone(ServerLevel l){if(active!=null){active.close();active=null;}}
    private static void hold(LiteraryTests.Fixture f,LabyrinthPlace place){var slot=LabyrinthPlaces.slotBounds(f.origin,place);f.chunks.hold(f.in,new AABB(slot.minX(),slot.minY(),slot.minZ(),slot.maxX()+1,slot.maxY()+1,slot.maxZ()+1));}
    private static boolean press(ServerLevel l,ServerPlayer p,BlockPos at){return l.getBlockState(at).useWithoutItem(l,p,new BlockHitResult(at.getCenter(),Direction.UP,at,false)).consumesAction();}
    private static void door(ServerLevel l,BlockPos at,boolean open){for(var half:new BlockPos[]{at,at.above()}){var s=l.getBlockState(half);if(s.getBlock() instanceof DoorBlock)l.setBlock(half,s.setValue(DoorBlock.OPEN,open),3);}}
    private static int stage(ServerLevel l,BlockPos at){var s=l.getBlockState(at);return s.is(PuzzleRegistry.DRAFT.get())?s.getValue(DraftBlock.STAGE):-1;}

    @GameTest(template="empty",batch="draft_rooms",timeoutTicks=600)
    public static void theDryingRoomsAirOpensTheFarDoorOnlyWhenItHasNowhereElseToGo(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var place=LabyrinthPlace.DRAFT_ROOM;var b=f.base(place);var l=f.in;hold(f,place);
        var p=NativeTestPlayers.survival(h,"drying_reader");f.players.add(p);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the room is resident"))
            .thenExecute(()->{
                h.assertTrue(PuzzleRooms.is(place)&&!PuzzleRooms.Puzzle.of(place).deep&&PuzzleRooms.Puzzle.of(place).family==PuzzleRooms.Family.AIR&&!place.isVignette()&&place.authored()&&WitnessAccount.Story.of(place.id())==null,"an early puzzle room of the air, built whole, and no story");
                DraftRooms.buildDrying(l,b);LabyrinthBuilder.registerDoors(f.data(),place,b);
                p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-4.5,180,0);
                var far=f.data().door(place.id()+"/far");var farAt=b.offset(PuzzleRooms.farDoor(place));
                h.assertTrue(far!=null&&l.getBlockState(farAt).is(Blocks.IRON_DOOR)&&!l.getBlockState(farAt).getValue(DoorBlock.OPEN)&&PuzzleRooms.holds(p,far),"the far door is shut and has no handle on this side");
                h.assertTrue(stage(l,b.offset(DraftRooms.VENT))==0&&stage(l,b.offset(DraftRooms.SASH))==0&&!l.getBlockState(b.offset(DraftRooms.INNER_DOOR)).getValue(DoorBlock.OPEN),"the grille shut, the window open, the inner door shut");
                h.assertTrue(DraftRooms.drying(l,b)==DraftRooms.Flow.STILL&&stage(l,b.offset(DraftRooms.SHEETS[0]))==0,"the air is still; the sheets hang straight");
                // The grille open: the air comes in and leans on the shut inner door.
                h.assertTrue(press(l,p,b.offset(DraftRooms.VENT))&&stage(l,b.offset(DraftRooms.VENT))==1,"a hand slides the grille open");
                h.assertTrue(DraftRooms.drying(l,b)==DraftRooms.Flow.BLOCKED&&stage(l,b.offset(DraftRooms.SHEETS[0]))==1,"the air comes in; the sheets belly toward the inner door, and it is shut");
                // The inner door open: the air goes straight out of the open window.
                door(l,b.offset(DraftRooms.INNER_DOOR),true);DraftRooms.airDrying(l,b,true);
                h.assertTrue(DraftRooms.drying(l,b)==DraftRooms.Flow.ESCAPES&&stage(l,b.offset(DraftRooms.WINDOW_CURTAINS[0]))==1,"the inner door open, the air goes out of the window and pulls its curtains");
                h.assertTrue(!PuzzleRooms.solved(f.data(),place)&&!l.getBlockState(farAt).getValue(DoorBlock.OPEN),"and the far door stays shut");
                // The window shut: nowhere for the air but the far door, and it gives.
                h.assertTrue(press(l,p,b.offset(DraftRooms.SASH))&&stage(l,b.offset(DraftRooms.SASH))==1,"a hand pulls the sash down");
                h.assertTrue(DraftRooms.drying(l,b)==DraftRooms.Flow.THROUGH&&PuzzleRooms.solved(f.data(),place)&&l.getBlockState(farAt).getValue(DoorBlock.OPEN),"the air leans on the far door and opens it; the room is solved for good");
                h.assertTrue(!PuzzleRooms.holds(p,far)&&stage(l,b.offset(DraftRooms.WINDOW_CURTAINS[0]))==0,"the door gives; the window's curtains hang still");
                // Solved, the room is built as it was left.
                DraftRooms.buildDrying(l,b);h.assertTrue(l.getBlockState(farAt).getValue(DoorBlock.OPEN)&&stage(l,b.offset(DraftRooms.VENT))==1,"built again, the door stands open and the grille with it");
            })
            .thenSucceed();
    }

    @GameTest(template="empty",batch="draft_rooms",timeoutTicks=600)
    public static void theVentRoomsSeamIsPlasterUntilTheAirIsDrawnToIt(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var place=LabyrinthPlace.VENT_ROOMS;var b=f.base(place);var l=f.in;hold(f,place);
        var p=NativeTestPlayers.survival(h,"vent_reader");f.players.add(p);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the rooms are resident"))
            .thenExecute(()->{
                h.assertTrue(PuzzleRooms.is(place)&&PuzzleRooms.Puzzle.of(place).deep&&PuzzleRooms.Puzzle.of(place).early().place==LabyrinthPlace.DRAFT_ROOM&&place.authored(),"the drying room's deep room, built whole");
                DraftRooms.buildVents(l,b);LabyrinthBuilder.registerDoors(f.data(),place,b);
                p.teleportTo(l,b.getX()+5.5,b.getY(),b.getZ()-23.5,90,0);
                var seam=b.offset(DraftRooms.SEAM);var farAt=b.offset(PuzzleRooms.farDoor(place));
                h.assertTrue(stage(l,seam)==0&&l.getBlockState(seam).isCollisionShapeFullBlock(l,seam),"the seam is a wall like the plaster round it");
                h.assertTrue(l.getBlockState(farAt).is(Blocks.IRON_DOOR)&&!l.getBlockState(farAt).getValue(DoorBlock.OPEN),"the far door is shut behind it");
                h.assertTrue(press(l,p,seam)&&stage(l,seam)==0&&!PuzzleRooms.solved(f.data(),place),"pressed with the air still, it is only plaster");
                // The first door open: the air goes out of the louvre in the second room.
                door(l,b.offset(DraftRooms.AB),true);DraftRooms.airVents(l,b,true);
                h.assertTrue(DraftRooms.vents(l,b)==DraftRooms.Flow.ESCAPES&&stage(l,b.offset(DraftRooms.LOUVRE_CURTAINS[0]))==1&&stage(l,seam)==0,"the air goes out of the louvre, pulling its curtains; nothing reaches the third room");
                // The louvre shut: the air leans on the second door.
                h.assertTrue(press(l,p,b.offset(DraftRooms.LOUVRE))&&DraftRooms.vents(l,b)==DraftRooms.Flow.BLOCKED_ON,"the louvre shut, the air is held at the second door");
                // The second door open: the air reaches the third room and is drawn into the seam.
                door(l,b.offset(DraftRooms.BC),true);DraftRooms.airVents(l,b,true);
                h.assertTrue(DraftRooms.vents(l,b)==DraftRooms.Flow.THROUGH&&stage(l,seam)==1&&stage(l,b.offset(DraftRooms.SEAM_CURTAIN))==1,"the dust lifts off the seam and the curtain lies flat toward it");
                h.assertTrue(press(l,p,seam)&&stage(l,seam)==2&&stage(l,seam.above())==2&&PuzzleRooms.solved(f.data(),place),"pressed while the air is at it, the plaster gives: a panel");
                h.assertTrue(l.getBlockState(farAt).getValue(DoorBlock.OPEN),"the far door is open: "+l.getBlockState(farAt));
                h.assertTrue(!l.getBlockState(seam).isCollisionShapeFullBlock(l,seam)&&!l.getBlockState(seam.above()).isCollisionShapeFullBlock(l,seam.above()),"the panel stands open on the passage: "+l.getBlockState(seam).getCollisionShape(l,seam).bounds());
                for(int z=-27;z<=-24;z++)h.assertTrue(l.getBlockState(b.offset(11,0,z)).getCollisionShape(l,b.offset(11,0,z)).isEmpty()&&l.getBlockState(b.offset(11,1,z)).getCollisionShape(l,b.offset(11,1,z)).isEmpty(),"the passage behind is clear to the far door: "+z);
                // Shut a door again and the panel stays open: solved stays solved.
                door(l,b.offset(DraftRooms.BC),false);DraftRooms.airVents(l,b,false);h.assertTrue(stage(l,seam)==2,"the panel stays open");
            })
            .thenSucceed();
    }

    /**
     * 0.4.245, the airing rooms (docs/AIRING_ROOMS_0_4_245.md): the air and the held doors together. Offered only once the vent rooms and
     * the sprung rooms are solved. The wedge in the first door changes the air in the second room at once: its sheets belly and the
     * louvre's curtains pull. With the louvre shut the air leans on the second door, which swings shut by itself; the loose chair,
     * dragged block by block into that doorway, holds it, and leaves a scrape across the dust all the way from where it stood. Then the
     * air reaches the plaster, and the plaster gives.
     */
    @GameTest(template="empty",batch="airing_rooms",timeoutTicks=600)
    public static void theWedgeAndADraggedChairLetTheAirThroughToThePlaster(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var place=LabyrinthPlace.AIRING_ROOMS;var b=f.base(place);var l=f.in;hold(f,place);
        var p=NativeTestPlayers.survival(h,"airing_reader");f.players.add(p);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the rooms are resident"))
            .thenExecute(()->{
                var q=PuzzleRooms.Puzzle.of(place);var data=f.data();
                h.assertTrue(q!=null&&q.deep&&q.builtOn().equals(java.util.List.of(LabyrinthPlace.VENT_ROOMS,LabyrinthPlace.SPRUNG_ROOMS))&&place.authored()&&WitnessAccount.Story.of(place.id())==null&&LabyrinthDealer.grayWeight(place,12)==0,"a deep puzzle room built on the vent rooms and the sprung rooms, no story, never dealt as a plain hall");
                // Offered only once both rooms it builds on are solved: a story since the last puzzle and the strange depths are not enough.
                WitnessAccount.resolve(data,p.getUUID(),WitnessAccount.Story.values()[0],"test");
                h.assertTrue(PuzzleRooms.weight(data,p.getUUID(),place,LabyrinthPacing.STRANGE_DEPTH+2)==0,"not offered before the vent rooms and the sprung rooms are solved");
                PuzzleRooms.markSolved(data,LabyrinthPlace.DRAFT_ROOM);PuzzleRooms.markSolved(data,LabyrinthPlace.VENT_ROOMS);
                h.assertTrue(PuzzleRooms.weight(data,p.getUUID(),place,LabyrinthPacing.STRANGE_DEPTH+2)==0,"nor with only the vent rooms solved");
                PuzzleRooms.markSolved(data,LabyrinthPlace.PROPPED_DOOR);PuzzleRooms.markSolved(data,LabyrinthPlace.SPRUNG_ROOMS);
                h.assertTrue(PuzzleRooms.weight(data,p.getUUID(),place,LabyrinthPacing.STRANGE_DEPTH+2)>0,"offered once both are");

                DraftRooms.buildAiring(l,b);LabyrinthBuilder.registerDoors(data,place,b);
                p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-4.5,180,0);
                var a=b.offset(DraftRooms.AB);var c=b.offset(DraftRooms.BC);var seam=b.offset(DraftRooms.SEAM);var farAt=b.offset(PuzzleRooms.farDoor(place));long now=l.getGameTime();
                h.assertTrue(TraceRooms.spring(l,a)&&TraceRooms.spring(l,c),"both doors are on closers");
                h.assertTrue(TraceBlock.is(l.getBlockState(b.offset(0,0,-14)),TraceBlock.Kind.DUST)&&TraceBlock.is(l.getBlockState(b.offset(DraftRooms.AIRING_CHAIR)),TraceBlock.Kind.CHAIR),"dust over the floors, and the loose chair in the second room");
                h.assertTrue(l.getBlockState(farAt).is(Blocks.IRON_DOOR)&&!l.getBlockState(farAt).getValue(DoorBlock.OPEN)&&stage(l,seam)==0,"the far door shut, the seam plaster");
                DraftRooms.airAiring(l,b,true);
                h.assertTrue(DraftRooms.flowAt(l,b,place)==DraftRooms.Flow.BLOCKED&&stage(l,b.offset(DraftRooms.B_SHEETS[0]))==0,"the first door shut: the air is held in the first room, the second room's sheets hang still");

                // The wedge in the first door: at once the air in the second room changes.
                TraceRooms.hook(p,b.offset(DraftRooms.AIRING_HOOK));p.getInventory().clearContent();p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(TraceRegistry.WEDGE.get()));
                h.assertTrue(Wedges.wedge(p,l,a)&&open(l,a),"the wedge holds the first door open");
                DraftRooms.airAiring(l,b,true);
                h.assertTrue(DraftRooms.flowAt(l,b,place)==DraftRooms.Flow.ESCAPES&&stage(l,b.offset(DraftRooms.B_SHEETS[0]))==1&&stage(l,b.offset(DraftRooms.B_SHEETS[1]))==1&&stage(l,b.offset(DraftRooms.LOUVRE_CURTAINS[0]))==1,"the second room's sheets belly, and the louvre's curtains pull: the air goes out of the louvre");
                TraceRooms.springs(l,b,place,now);TraceRooms.springs(l,b,place,now+TraceRooms.SPRING_TICKS);
                h.assertTrue(open(l,a),"the wedged door does not swing shut");

                // The louvre shut: the air leans on the second door, which will not stay open by itself.
                p.teleportTo(l,b.getX()-9.5,b.getY(),b.getZ()-13.5,-90,0);
                h.assertTrue(press(l,p,b.offset(DraftRooms.LOUVRE))&&DraftRooms.flowAt(l,b,place)==DraftRooms.Flow.BLOCKED_ON,"the louvre shut, the air is held at the second door");
                door(l,c,true);TraceRooms.springs(l,b,place,now+40);TraceRooms.springs(l,b,place,now+40+TraceRooms.SPRING_TICKS);
                h.assertTrue(!open(l,c)&&DraftRooms.flowAt(l,b,place)==DraftRooms.Flow.BLOCKED_ON,"opened, the second door swings shut again");

                // The chair, dragged a block at a time from where it stood into the second doorway.
                var chair=b.offset(DraftRooms.AIRING_CHAIR);
                for(int i=0;i<10;i++){p.teleportTo(l,chair.getX()+3,chair.getY(),chair.getZ()+.5,-90,0);h.assertTrue(TraceRooms.chair(p,chair,l.getBlockState(chair))&&TraceBlock.is(l.getBlockState(chair.east()),TraceBlock.Kind.CHAIR),"pulled east: "+i);chair=chair.east();}
                for(int i=0;i<2;i++){p.teleportTo(l,chair.getX()+.5,chair.getY(),chair.getZ()-2,180,0);h.assertTrue(TraceRooms.chair(p,chair,l.getBlockState(chair))&&TraceBlock.is(l.getBlockState(chair.north()),TraceBlock.Kind.CHAIR),"pulled north: "+i);chair=chair.north();}
                h.assertTrue(chair.equals(c.south()),"the chair stands in the second doorway: "+chair);
                var from=b.offset(DraftRooms.AIRING_CHAIR);
                for(int x=0;x<10;x++){var at=from.east(x);h.assertTrue(TraceBlock.dusty(l.getBlockState(at))&&l.getBlockState(at).getValue(TraceBlock.STAGE)==TraceBlock.DRAGGED,"a scrape across the dust where it was dragged: "+x);}
                for(int z=0;z<2;z++){var at=from.east(10).north(z);h.assertTrue(TraceBlock.dusty(l.getBlockState(at))&&l.getBlockState(at).getValue(TraceBlock.STAGE)==TraceBlock.DRAGGED,"and on to the doorway: "+z);}

                // The second door opened again: the chair holds it, and the air goes through to the plaster.
                door(l,c,true);TraceRooms.springs(l,b,place,now+100);TraceRooms.springs(l,b,place,now+100+TraceRooms.SPRING_TICKS+10);
                h.assertTrue(open(l,c)&&open(l,a)&&TraceRooms.held(l,c),"the chair in its way, the second door stays open, and the first is still wedged");
                DraftRooms.airAiring(l,b,true);
                h.assertTrue(DraftRooms.flowAt(l,b,place)==DraftRooms.Flow.THROUGH&&stage(l,seam)==1&&stage(l,b.offset(DraftRooms.SEAM_CURTAIN))==1,"the dust lifts off the seam and the curtain lies flat toward it");
                p.teleportTo(l,b.getX()+8.5,b.getY(),b.getZ()-23.5,90,0);
                h.assertTrue(press(l,p,seam)&&stage(l,seam)==2&&PuzzleRooms.solved(data,place)&&l.getBlockState(farAt).getValue(DoorBlock.OPEN),"pressed while the air is at it, the plaster gives and the far door opens");
                h.assertTrue(TraceBlock.is(l.getBlockState(c.south()),TraceBlock.Kind.CHAIR)&&Wedges.at(l,a)&&l.getBlockState(from).getValue(TraceBlock.STAGE)==TraceBlock.DRAGGED,"and what solved it is still there to see: the wedge, the chair, the scrape");
            })
            .thenSucceed();
    }
    private static boolean open(ServerLevel l,BlockPos at){var s=l.getBlockState(at);return s.getBlock() instanceof DoorBlock&&s.getValue(DoorBlock.OPEN);}
}
