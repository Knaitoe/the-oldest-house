package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.231, traces and the door wedge, played with a real reader's hands: dust keeps the reader's prints, pointing the way they went;
 * the loose chair is pulled a block at a time and scores the dust it leaves; after many dependable returns, and with nobody there, a
 * second chair is drawn up across the reader's table. The cloakroom's far door gives only while the door behind is held, and the
 * wedge on the hook holds it; the sprung rooms need both their doors held at once, by the wedge and the chair. In the House's own
 * doors a wedge holds the door a reader came in by: the hall behind it stays what it was, and nobody comes in by it from elsewhere.
 * 0.4.234: below the turn to stone, the grit gallery keeps prints and the dragged chair's scoring the same way.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_exploration") @PrefixGameTestTemplate(false)
public final class TraceRoomTests {
    private TraceRoomTests(){}
    private static LiteraryTests.Fixture active;
    private static void close(){TraceRooms.always=false;if(active!=null){active.close();active=null;}}
    // Each case its own batch: the shut-up rooms and the wedged door share one hall, and the House adds nothing while anyone else is in it.
    @AfterBatch(batch="trace_dust") public static void doneDust(ServerLevel l){close();}
    @AfterBatch(batch="trace_cloak") public static void doneCloak(ServerLevel l){close();}
    @AfterBatch(batch="trace_sprung") public static void doneSprung(ServerLevel l){close();}
    @AfterBatch(batch="trace_wedge") public static void doneWedge(ServerLevel l){close();}
    @AfterBatch(batch="trace_stone") public static void doneStone(ServerLevel l){close();}
    private static void hold(LiteraryTests.Fixture f,LabyrinthPlace place){var slot=LabyrinthPlaces.slotBounds(f.origin,place);f.chunks.hold(f.in,new AABB(slot.minX(),slot.minY(),slot.minZ(),slot.maxX()+1,slot.maxY()+1,slot.maxZ()+1));}
    private static int stage(ServerLevel l,BlockPos at){var s=l.getBlockState(at);return s.is(TraceRegistry.FITTING.get())?s.getValue(TraceBlock.STAGE):-1;}
    private static boolean is(ServerLevel l,BlockPos at,TraceBlock.Kind kind){return TraceBlock.is(l.getBlockState(at),kind);}
    /** A step from the cell south of {@code at} into it, as the reader's foot meets the dust. */
    private static void walkNorth(ServerLevel l,ServerPlayer p,BlockPos at){p.teleportTo(l,at.getX()+.5,at.getY(),at.getZ()+.5,p.getYRot(),p.getXRot());p.xo=p.getX();p.zo=p.getZ()+.4;TraceRooms.step(p,at,l.getBlockState(at));}
    private static void door(ServerLevel l,BlockPos at,boolean open){for(var half:new BlockPos[]{at,at.above()}){var s=l.getBlockState(half);if(s.getBlock() instanceof DoorBlock)l.setBlock(half,s.setValue(DoorBlock.OPEN,open),3);}}
    private static boolean open(ServerLevel l,BlockPos at){var s=l.getBlockState(at);return s.getBlock() instanceof DoorBlock&&s.getValue(DoorBlock.OPEN);}

    @GameTest(template="empty",batch="trace_dust",timeoutTicks=600)
    public static void theShutUpRoomsKeepPrintsAndTheChairWhereItWasDragged(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var place=LabyrinthPlace.DUST_HALL;var b=f.base(place);var l=f.in;hold(f,place);
        var p=NativeTestPlayers.survival(h,"dust_reader");f.players.add(p);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the hall is resident"))
            .thenExecute(()->{
                h.assertTrue(LabyrinthPacing.ordinary(place)&&place.authored()&&!place.isVignette()&&WitnessAccount.Story.of(place.id())==null,"an ordinary hall, built whole, and no story");
                TraceRooms.buildDust(l,b);LabyrinthBuilder.registerDoors(f.data(),place,b);
                h.assertTrue(is(l,b.offset(0,0,-6),TraceBlock.Kind.DUST)&&stage(l,b.offset(0,0,-6))==TraceBlock.CLEAN,"dust over the floor, clean");
                h.assertTrue(LabyrinthDealer.grayWeight(place,2)==0&&LabyrinthDealer.grayWeight(place,6)>0&&LabyrinthDealer.grayWeight(place,LabyrinthPacing.STONE_DEPTH)==0,"not in the first crossings, nor once the halls are stone; an ordinary hall between");
                // A walk up the hall: prints in every cell, pointing north, the way the reader went; kept on their path.
                for(int z=-6;z>=-11;z--)walkNorth(l,p,b.offset(0,0,z));
                var printed=l.getBlockState(b.offset(0,0,-8));
                h.assertTrue(stage(l,b.offset(0,0,-8))==TraceBlock.PRINTS&&printed.getValue(TraceBlock.FACING)==Direction.NORTH,"prints pointing the way the reader went: "+printed);
                h.assertTrue(stage(l,b.offset(1,0,-8))==TraceBlock.CLEAN,"and nowhere they did not");
                h.assertTrue(TraceRooms.pathLength(f.data(),p.getUUID(),place)==6,"the path is the reader's own");
                // The loose chair, pulled north a block: the dust it stood in is scored where it was dragged.
                var chair=b.offset(TraceRooms.DUST_CHAIR);h.assertTrue(is(l,chair,TraceBlock.Kind.CHAIR),"the loose chair stands in the dust");
                p.teleportTo(l,chair.getX()+.5,chair.getY(),chair.getZ()-2.5,p.getYRot(),p.getXRot());
                h.assertTrue(TraceRooms.chair(p,chair,l.getBlockState(chair))&&is(l,chair.north(),TraceBlock.Kind.CHAIR),"a hand pulls it a block toward the reader");
                h.assertTrue(stage(l,chair)==TraceBlock.DRAGGED,"the dust where it stood is scored");
                p.setShiftKeyDown(true);h.assertTrue(TraceRooms.chair(p,chair.north(),l.getBlockState(chair.north()))&&l.getBlockState(chair.north()).getValue(TraceBlock.FACING)==Direction.SOUTH,"crouching, the reader turns it");p.setShiftKeyDown(false);
                // Drawn up to the table: set beside it facing it, and pulled the last block.
                var table=b.offset(TraceRooms.DUST_TABLE);var start=table.east(2);
                l.setBlock(start,TraceBlock.of(TraceBlock.Kind.CHAIR,Direction.WEST,1),3);
                p.teleportTo(l,table.getX()-1.5,table.getY(),table.getZ()+.5,p.getYRot(),p.getXRot());
                h.assertTrue(TraceRooms.chair(p,start,l.getBlockState(start))&&is(l,table.east(),TraceBlock.Kind.CHAIR),"pulled up to the table");
                h.assertTrue(TraceRooms.drawnUp(f.data(),p.getUUID(),place),"drawn up to it, facing it");
                // Many dependable returns; then, leaving with nobody there and nobody looking, a second chair across the table.
                for(int i=0;i<TraceRooms.MANY;i++)TraceRooms.arrived(p,place);
                h.assertTrue(TraceRooms.returns(f.data(),p.getUUID())>=TraceRooms.MANY,"each return found the traces as left");
                p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()+3.5,0,0);
                TraceRooms.always=true;TraceRooms.departed(l,p,place);TraceRooms.always=false;
                h.assertTrue(is(l,table.west(),TraceBlock.Kind.CHAIR)&&l.getBlockState(table.west()).getValue(TraceBlock.FACING)==Direction.EAST,"a second chair drawn up across the table");
                h.assertTrue(TraceRooms.added(f.data(),p.getUUID(),place),"once, and remembered");
                TraceRooms.always=true;l.setBlock(table.west(),TraceBlock.dust(),3);TraceRooms.departed(l,p,place);TraceRooms.always=false;
                h.assertTrue(!is(l,table.west(),TraceBlock.Kind.CHAIR),"and never again in the same place");
            })
            .thenSucceed();
    }

    @GameTest(template="empty",batch="trace_cloak",timeoutTicks=600)
    public static void theCloakroomsFarDoorGivesOnlyWhileTheDoorBehindIsHeld(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var place=LabyrinthPlace.PROPPED_DOOR;var b=f.base(place);var l=f.in;hold(f,place);
        var p=NativeTestPlayers.survival(h,"cloak_reader");f.players.add(p);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the room is resident"))
            .thenExecute(()->{
                var q=PuzzleRooms.Puzzle.of(place);
                h.assertTrue(q!=null&&!q.deep&&q.family==PuzzleRooms.Family.HOLD&&place.authored()&&WitnessAccount.Story.of(place.id())==null,"an early puzzle room of doors held open, built whole, and no story");
                TraceRooms.buildCloak(l,b);LabyrinthBuilder.registerDoors(f.data(),place,b);
                var farAt=b.offset(PuzzleRooms.farDoor(place));var far=f.data().door(place.id()+"/far");
                h.assertTrue(l.getBlockState(farAt).is(Blocks.IRON_DOOR)&&!open(l,farAt)&&PuzzleRooms.holds(p,far),"the far door is shut and has no handle on this side");
                // The wedge on the hook: one to a reader.
                var hook=b.offset(TraceRooms.CLOAK_HOOK);
                h.assertTrue(TraceRooms.hook(p,hook)&&p.getInventory().countItem(TraceRegistry.WEDGE.get())==1,"a wedge from the hook");
                h.assertTrue(TraceRooms.hook(p,hook)&&p.getInventory().countItem(TraceRegistry.WEDGE.get())==1,"only the one");
                // The inner door on its closer: left open, it swings shut by itself.
                var inner=b.offset(TraceRooms.CLOAK_DOOR);long now=l.getGameTime();
                h.assertTrue(TraceRooms.spring(l,inner),"the inner door is on a closer");
                door(l,inner,true);TraceRooms.springs(l,b,place,now);h.assertTrue(open(l,inner),"opened, it stands a moment");
                TraceRooms.springs(l,b,place,now+TraceRooms.SPRING_TICKS);h.assertTrue(!open(l,inner),"then swings shut");
                // Standing at the far door with the way behind shut: nothing.
                var front=b.offset(TraceRooms.CLOAK_FRONT);p.teleportTo(l,front.getX()+.5,front.getY(),front.getZ()+.5,p.getYRot(),p.getXRot());
                TraceRooms.springs(l,b,place,now+40);h.assertTrue(!open(l,farAt)&&!PuzzleRooms.solved(f.data(),place),"the far door stays shut while the way behind is shut");
                // The wedge in the inner door: it is opened and stays open, and the far door gives.
                p.getInventory().clearContent();p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(TraceRegistry.WEDGE.get()));
                h.assertTrue(Wedges.wedge(p,l,inner)&&open(l,inner)&&Wedges.at(l,inner)&&p.getMainHandItem().isEmpty(),"the wedge goes in under the inner door and holds it open");
                h.assertTrue(!l.getEntitiesOfClass(Display.ItemDisplay.class,new AABB(inner).inflate(1),d->d.getTags().contains(Wedges.TAG)).isEmpty(),"the wedge lies at the door's foot, there to be seen");
                TraceRooms.springs(l,b,place,now+200);
                h.assertTrue(open(l,inner)&&open(l,farAt)&&PuzzleRooms.solved(f.data(),place),"held, the door stays open and the far door gives; solved for good");
                // The wedge comes out into the hand that pulls it.
                h.assertTrue(Wedges.lift(p,l,inner)&&!Wedges.at(l,inner)&&p.getInventory().countItem(TraceRegistry.WEDGE.get())==1,"pulled out, it is the reader's again");
                TraceRooms.buildCloak(l,b);h.assertTrue(open(l,farAt),"built again, the far door stands open");
            })
            .thenSucceed();
    }

    @GameTest(template="empty",batch="trace_sprung",timeoutTicks=600)
    public static void theSprungRoomsNeedBothDoorsHeldTheWedgeAndTheChair(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var place=LabyrinthPlace.SPRUNG_ROOMS;var b=f.base(place);var l=f.in;hold(f,place);
        var p=NativeTestPlayers.survival(h,"sprung_reader");f.players.add(p);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the rooms are resident"))
            .thenExecute(()->{
                h.assertTrue(PuzzleRooms.Puzzle.of(place).deep&&PuzzleRooms.Puzzle.of(place).early().place==LabyrinthPlace.PROPPED_DOOR,"the cloakroom's deep room");
                TraceRooms.buildSprung(l,b);LabyrinthBuilder.registerDoors(f.data(),place,b);
                var a=b.offset(TraceRooms.SPRUNG_A);var c=b.offset(TraceRooms.SPRUNG_B);var farAt=b.offset(PuzzleRooms.farDoor(place));long now=l.getGameTime();
                h.assertTrue(is(l,b.offset(2,0,-14),TraceBlock.Kind.DUST),"dust over the rooms");
                // The wedge in the first door; the second opened but nothing holding it.
                TraceRooms.hook(p,b.offset(TraceRooms.SPRUNG_HOOK));p.getInventory().clearContent();p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(TraceRegistry.WEDGE.get()));
                h.assertTrue(Wedges.wedge(p,l,a),"the wedge in the first door");
                door(l,c,true);TraceRooms.springs(l,b,place,now);TraceRooms.springs(l,b,place,now+TraceRooms.SPRING_TICKS);
                h.assertTrue(open(l,a)&&!open(l,c),"the wedged door stays; the other swings shut");
                var front=b.offset(TraceRooms.SPRUNG_FRONT);p.teleportTo(l,front.getX()+.5,front.getY(),front.getZ()+.5,p.getYRot(),p.getXRot());
                TraceRooms.springs(l,b,place,now+40);h.assertTrue(!open(l,farAt),"one door held is not the way behind held");
                // The chair, pulled a couple of blocks, then set in the second doorway as if dragged there: it holds that door.
                var chair=b.offset(TraceRooms.SPRUNG_CHAIR);p.teleportTo(l,chair.getX()-2.5,chair.getY(),chair.getZ()+.5,p.getYRot(),p.getXRot());
                h.assertTrue(TraceRooms.chair(p,chair,l.getBlockState(chair))&&is(l,chair.west(),TraceBlock.Kind.CHAIR)&&stage(l,chair)==TraceBlock.DRAGGED,"the chair comes a block, scoring the dust");
                l.setBlock(chair.west(),TraceBlock.dust(),3);l.setBlock(c.south(),TraceBlock.of(TraceBlock.Kind.CHAIR,Direction.NORTH,1),3);
                door(l,c,true);TraceRooms.springs(l,b,place,now+50);
                p.teleportTo(l,front.getX()+.5,front.getY(),front.getZ()+.5,p.getYRot(),p.getXRot());
                TraceRooms.springs(l,b,place,now+50+TraceRooms.SPRING_TICKS+20);
                h.assertTrue(open(l,c)&&open(l,a),"the chair in its way, the second door stays open too");
                h.assertTrue(open(l,farAt)&&PuzzleRooms.solved(f.data(),place),"both held, the far door gives");
            })
            .thenSucceed();
    }

    @GameTest(template="empty",batch="trace_wedge",timeoutTicks=600)
    public static void aWedgeHoldsTheDoorAReaderCameInByAndTheHallBehindIt(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var place=LabyrinthPlace.DUST_HALL;var b=f.base(place);var l=f.in;hold(f,place);
        var p=NativeTestPlayers.survival(h,"wedge_owner");f.players.add(p);var q=NativeTestPlayers.survival(h,"wedge_other");f.players.add(q);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the hall is resident"))
            .thenExecute(()->{
                TraceRooms.buildDust(l,b);var data=f.data();LabyrinthBuilder.registerDoors(data,place,b);
                // The hall the reader came from: the junction's west door, dealt to the shut-up rooms.
                LabyrinthBuilder.registerDoors(data,LabyrinthPlace.JUNCTION,f.base(LabyrinthPlace.JUNCTION));
                var west=data.door("junction/west");var east=data.door("junction/east");var entry=data.door(place.entryDoorId());
                h.assertTrue(west!=null&&east!=null&&entry!=null,"the doors are known");
                data.pushReturn(p.getUUID(),new LabyrinthData.Waypoint(west.dimension,Vec3.atBottomCenterOf(west.lower),0,true,false));
                p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-3.5,0,0);q.teleportTo(l,b.getX()+1.5,b.getY(),b.getZ()-3.5,0,0);
                // Not any door: only the one the reader came in by.
                q.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(TraceRegistry.WEDGE.get()));
                h.assertTrue(!Wedges.wedge(q,l,b.offset(4,0,-21)),"a door further in will not take it");
                p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(TraceRegistry.WEDGE.get()));
                h.assertTrue(Wedges.wedge(p,l,entry.lower)&&open(l,entry.lower)&&Wedges.at(l,entry.lower),"the wedge goes in under the door the reader came in by, and holds it open");
                // The hall behind it stays what it was: the junction's west door still leads here, however the reader comes back to it.
                h.assertTrue(place.id().equals(Wedges.pin(data,p.getUUID(),west.id))&&Wedges.pin(data,q.getUUID(),west.id)==null,"held for the one who set it");
                data.popReturn(p.getUUID());data.pushReturn(p.getUUID(),new LabyrinthData.Waypoint(l.dimension(),Vec3.atBottomCenterOf(b.offset(0,0,40)),0,true,false));
                data.deal(p.getUUID(),west,LabyrinthPlace.GRAY_CORRIDOR.id(),false);
                LabyrinthDealer.arriveAt(data,p.getUUID(),LabyrinthPlace.JUNCTION,77L);
                h.assertTrue(place.id().equals(data.deal(p.getUUID(),west).place()),"come back by another way, the west door still leads to the shut-up rooms: "+data.deal(p.getUUID(),west));
                // Nobody comes in by that door from anywhere else while its wedge's owner is here; from the west door, as ever.
                h.assertTrue(Wedges.blocks(q,east,entry),"from the east door it gives an inch and stops");
                h.assertTrue(!Wedges.blocks(q,west,entry),"from the hall it shows, it opens");
                // With the one who set it gone from the game, it is knocked loose instead, and lies by the door.
                f.players.remove(p);NativeTestPlayers.remove(p);
                h.assertTrue(!Wedges.blocks(q,east,entry)&&!Wedges.at(l,entry.lower),"knocked loose");
                h.assertTrue(!l.getEntitiesOfClass(ItemEntity.class,new AABB(entry.lower).inflate(2),e->e.getItem().is(TraceRegistry.WEDGE.get())).isEmpty(),"the wedge lies by the door");
                h.assertTrue(l.getEntitiesOfClass(Display.ItemDisplay.class,new AABB(entry.lower).inflate(1),d->d.getTags().contains(Wedges.TAG)).isEmpty(),"and is no longer in it");
            })
            .thenSucceed();
    }

    @GameTest(template="empty",batch="trace_stone",timeoutTicks=600)
    public static void theGritGalleryKeepsPrintsBelowTheTurnToStone(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var place=LabyrinthPlace.STONE_DUST;var b=f.base(place);var l=f.in;hold(f,place);
        var p=NativeTestPlayers.survival(h,"grit_reader");f.players.add(p);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the gallery is resident"))
            .thenExecute(()->{
                h.assertTrue(LabyrinthPacing.ordinary(place)&&StoneHalls.isStone(place)&&place.authored()&&!place.isVignette()&&WitnessAccount.Story.of(place.id())==null,"an ordinary stone hall, built whole, and no story");
                h.assertTrue(LabyrinthBuilder.requiredDepth(place)==LabyrinthPacing.STONE_DEPTH,"built ahead of the stone depths");
                int stone=LabyrinthPacing.STONE_DEPTH;
                h.assertTrue(LabyrinthDealer.grayWeight(place,stone-1)==0&&LabyrinthDealer.grayWeight(place,stone)>0&&LabyrinthDealer.grayWeight(place,DarkHalls.ONLY_DEPTH-1)>0&&LabyrinthDealer.grayWeight(place,DarkHalls.ONLY_DEPTH)==0,"dealt from the turn to stone until only the dark halls remain");
                h.assertTrue(LabyrinthDealer.grayWeight(LabyrinthPlace.DUST_HALL,stone)==0,"where the shut-up rooms leave off");
                TraceRooms.buildStoneDust(l,b);LabyrinthBuilder.registerDoors(f.data(),place,b);
                h.assertTrue(is(l,b.offset(0,0,-6),TraceBlock.Kind.GRIT)&&stage(l,b.offset(0,0,-6))==TraceBlock.CLEAN,"grit over the floor, clean");
                h.assertTrue(l.getBlockState(b.offset(2,-1,-6)).is(Blocks.POLISHED_ANDESITE)&&l.getBlockState(b.offset(-4,2,-6)).is(net.minecraft.tags.BlockTags.STONE_BRICKS),"stone underfoot and masonry walls");
                h.assertTrue(is(l,b.offset(TraceRooms.STONE_TABLE),TraceBlock.Kind.TABLE)&&l.getBlockState(b.offset(TraceRooms.STONE_LAMP)).is(Blocks.LANTERN),"someone's table, and a lantern left on the floor by it");
                h.assertTrue(l.getBlockState(b.offset(-2,5,TraceRooms.STONE_FALLEN_RIB)).is(Blocks.STONE_BRICK_SLAB)&&l.getBlockState(b.offset(0,5,TraceRooms.STONE_FALLEN_RIB)).isAir()&&l.getBlockState(b.offset(-1,0,TraceRooms.STONE_FALLEN_RIB)).is(Blocks.STONE_BRICK_SLAB),"a rib come down, its stones on the floor under the gap");
                h.assertTrue(f.data().door(place.id()+"/side")!=null&&f.data().door(place.id()+"/far")!=null,"a side door and a far door lead on");
                // A walk up the gallery: prints in the grit, pointing the way the reader went, and nowhere else; the path their own.
                for(int z=-6;z>=-11;z--)walkNorth(l,p,b.offset(0,0,z));
                var printed=l.getBlockState(b.offset(0,0,-8));
                h.assertTrue(is(l,b.offset(0,0,-8),TraceBlock.Kind.GRIT)&&stage(l,b.offset(0,0,-8))==TraceBlock.PRINTS&&printed.getValue(TraceBlock.FACING)==Direction.NORTH,"prints in the grit pointing the way the reader went: "+printed);
                h.assertTrue(stage(l,b.offset(1,0,-8))==TraceBlock.CLEAN,"and nowhere they did not");
                h.assertTrue(TraceRooms.pathLength(f.data(),p.getUUID(),place)==6,"the path is the reader's own");
                // The loose chair, pulled north a block: the grit it stood in is scored, and it stands in grit.
                var chair=b.offset(TraceRooms.STONE_CHAIR);h.assertTrue(is(l,chair,TraceBlock.Kind.CHAIR)&&stage(l,chair)==2,"the loose chair stands in the grit");
                p.teleportTo(l,chair.getX()+.5,chair.getY(),chair.getZ()-2.5,p.getYRot(),p.getXRot());
                h.assertTrue(TraceRooms.chair(p,chair,l.getBlockState(chair))&&is(l,chair.north(),TraceBlock.Kind.CHAIR)&&stage(l,chair.north())==2,"a hand pulls it a block toward the reader, into the grit");
                h.assertTrue(is(l,chair,TraceBlock.Kind.GRIT)&&stage(l,chair)==TraceBlock.DRAGGED,"the grit where it stood is scored");
                // Many dependable returns; then, leaving with nobody there and nobody looking, a second set of prints beside the reader's.
                for(int i=0;i<TraceRooms.MANY;i++)TraceRooms.arrived(p,place);
                h.assertTrue(TraceRooms.returns(f.data(),p.getUUID())>=TraceRooms.MANY,"each return found the prints as left");
                p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()+3.5,0,0);
                TraceRooms.always=true;TraceRooms.departed(l,p,place);TraceRooms.always=false;
                h.assertTrue(is(l,b.offset(1,0,-8),TraceBlock.Kind.GRIT)&&stage(l,b.offset(1,0,-8))==TraceBlock.OTHER,"a second set of prints beside the reader's own");
                h.assertTrue(TraceRooms.added(f.data(),p.getUUID(),place)&&stage(l,b.offset(0,0,-8))==TraceBlock.PRINTS,"once, and the reader's own prints as they were");
            })
            .thenSucceed();
    }
}
