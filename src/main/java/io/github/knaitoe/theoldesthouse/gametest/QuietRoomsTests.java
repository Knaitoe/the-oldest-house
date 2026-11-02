package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseSitting;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.LecternMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.261, the rest rooms (docs/REST_ROOMS_0_4_261.md). The quiet room's four siblings are built whole, each with seats, a little food,
 * light, a note and its two doors; a reader's note is their own and moves on one each time they come into a rest room, so two readers
 * at one stand read different notes and the quiet room's old note is no longer the only one; and a rest takes the reader to the rest
 * room they saw longest ago, none coming round again among the halls within six rooms.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_exploration") @PrefixGameTestTemplate(false)
public final class QuietRoomsTests {
    private QuietRoomsTests(){}
    private static LiteraryTests.Fixture active;
    private static final List<LabyrinthPlace> NEW=List.of(LabyrinthPlace.QUIET_READING,LabyrinthPlace.QUIET_BOOTROOM,LabyrinthPlace.QUIET_SUNROOM,LabyrinthPlace.QUIET_SPARE);
    @AfterBatch(batch="rest_rooms") public static void done(ServerLevel l){if(active!=null){active.close();active=null;}QuietRooms.clearAll();}
    private static void hold(LiteraryTests.Fixture f,LabyrinthPlace place){var slot=LabyrinthPlaces.slotBounds(f.origin,place);f.chunks.hold(f.in,new AABB(slot.minX(),slot.minY(),slot.minZ(),slot.maxX()+1,slot.maxY()+1,slot.maxZ()+1));}
    private static void build(LiteraryTests.Fixture f,LabyrinthPlace place){
        var l=f.in;var b=f.base(place);
        switch(place){
            case QUIET_READING->QuietRooms.buildReading(l,b);case QUIET_BOOTROOM->QuietRooms.buildBootRoom(l,b);
            case QUIET_SUNROOM->QuietRooms.buildSunRoom(l,b);case QUIET_SPARE->QuietRooms.buildSpare(l,b);
            default->LabyrinthHalls.build(l,b,place);
        }
        LabyrinthBuilder.registerDoors(f.data(),place,b);
    }
    private static void stand(ServerPlayer p,LiteraryTests.Fixture f,LabyrinthPlace place,double x,double z){var b=f.base(place);p.teleportTo(f.in,b.getX()+x,b.getY(),b.getZ()+z,180,0);}
    /** The reader's hand on whatever holds the note, through the ordinary use of a block. */
    private static void touch(ServerPlayer p,ServerLevel l,BlockPos at){p.gameMode.useItemOn(p,l,ItemStack.EMPTY,InteractionHand.MAIN_HAND,new BlockHitResult(at.getCenter(),Direction.UP,at,false));}
    private static String open(ServerPlayer p){if(!(p.containerMenu instanceof LecternMenu menu))return "";var c=menu.getBook().get(DataComponents.WRITTEN_BOOK_CONTENT);return c==null?"":c.title().raw();}

    @GameTest(template="empty",batch="rest_rooms",timeoutTicks=900)
    public static void theRestRoomsAreBuiltWholeAndEachReaderReadsTheirOwnNote(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var l=f.in;QuietRooms.clearAll();
        for(var place:QuietRooms.FAMILY)hold(f,place);
        var p=NativeTestPlayers.survival(h,"resting_reader");var q=NativeTestPlayers.survival(h,"second_rester");f.players.add(p);f.players.add(q);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the rest rooms are resident"))
            .thenExecute(()->{
                for(var place:QuietRooms.FAMILY)build(f,place);
                for(var place:NEW){
                    var b=f.base(place);
                    h.assertTrue(place.isGray()&&!place.isVignette()&&place.authored()&&LabyrinthPacing.quiet(place)&&LabyrinthPacing.calm(place)&&WitnessAccount.Story.of(place.id())==null,place.id()+" is a rest room, built whole, with no story");
                    for(var spec:place.doors())h.assertTrue(l.getBlockState(b.offset(spec.rel())).getBlock() instanceof DoorBlock,place.id()+" has its "+spec.name()+" door");
                    var far=place.doors().stream().filter(s->s.name().equals("far")).findFirst().orElseThrow();var front=far.rel().relative(far.facing());
                    h.assertTrue(l.getBlockState(b.offset(front)).getCollisionShape(l,b.offset(front)).isEmpty()&&l.getBlockState(b.offset(front).above()).getCollisionShape(l,b.offset(front).above()).isEmpty(),place.id()+" leaves room to stand at its way on");
                    var store=QuietRooms.store(l,b,place);h.assertTrue(store!=null&&!store.isEmpty(),place.id()+" keeps a little food");
                    for(var seat:QuietRooms.seats(place))h.assertTrue(HouseSitting.isSeat(l.getBlockState(b.offset(seat))),place.id()+" has somewhere to sit at "+seat);
                    h.assertTrue(QuietRooms.noteAt(l,b.offset(QuietRooms.note(place)))==place&&!l.getBlockState(b.offset(QuietRooms.note(place))).isAir(),place.id()+" has its note where a reader can find it");
                }
                h.assertTrue(QuietNotes.COUNT>=16&&QuietNotes.title(0).equals("A place to stop"),"sixteen notes, the first the quiet room's own");})
            .thenWaitUntil(()->{for(var place:NEW){var b=f.base(place);int lit=l.getBrightness(LightLayer.BLOCK,b.offset(0,1,-3));
                h.assertTrue(lit>=8,place.id()+" is lit as you come in: "+lit+" (light at 2,4,-3: "+l.getBlockState(b.offset(2,4,-3))+" "+l.getBrightness(LightLayer.BLOCK,b.offset(2,4,-3))+")");}})
            .thenExecute(()->stand(p,f,LabyrinthPlace.QUIET_ROOM,.5,-7.5))
            .thenWaitUntil(()->h.assertTrue(QuietRooms.rests(f.data(),p.getUUID())==1,"coming into a rest room counts once"))
            .thenExecute(()->{
                var lectern=f.base(LabyrinthPlace.QUIET_ROOM).offset(QuietRooms.ROOM_NOTE);
                touch(p,l,lectern);
                h.assertTrue(p.containerMenu instanceof LecternMenu&&open(p).equals(QuietNotes.title(0)),"the stand opens on the reader's own first note: "+open(p));
                h.assertTrue(!p.containerMenu.clickMenuButton(p,3),"nobody takes the note away");p.closeContainer();
                h.assertTrue(l.getBlockEntity(lectern) instanceof LecternBlockEntity stand&&stand.hasBook(),"the stand still holds its book");
                stand(p,f,LabyrinthPlace.QUIET_READING,.5,-7.5);})
            .thenWaitUntil(()->h.assertTrue(QuietRooms.rests(f.data(),p.getUUID())==2,"a second rest room, a second rest"))
            .thenExecute(()->{
                var lectern=f.base(LabyrinthPlace.QUIET_READING).offset(QuietRooms.READING_NOTE);
                touch(p,l,lectern);h.assertTrue(open(p).equals(QuietNotes.title(1)),"the next note on the next rest: "+open(p));p.closeContainer();
                stand(q,f,LabyrinthPlace.QUIET_READING,1.5,-7.5);})
            .thenWaitUntil(()->h.assertTrue(QuietRooms.rests(f.data(),q.getUUID())==1,"the second reader's first rest"))
            .thenExecute(()->{
                var lectern=f.base(LabyrinthPlace.QUIET_READING).offset(QuietRooms.READING_NOTE);
                touch(q,l,lectern);h.assertTrue(open(q).equals(QuietNotes.title(0)),"at the same stand the second reader reads their own first note: "+open(q));
                touch(p,l,lectern);h.assertTrue(open(p).equals(QuietNotes.title(1)),"and the first still reads theirs");p.closeContainer();q.closeContainer();
                stand(p,f,LabyrinthPlace.QUIET_SPARE,.5,-4.5);})
            .thenWaitUntil(()->h.assertTrue(QuietRooms.rests(f.data(),p.getUUID())==3,"the spare room is a rest too"))
            .thenExecute(()->{
                touch(p,l,f.base(LabyrinthPlace.QUIET_SPARE).offset(QuietRooms.SPARE_NOTE));
                h.assertTrue(open(p).equals(QuietNotes.title(2)),"the note on the pillow: "+open(p));p.closeContainer();
                stand(p,f,LabyrinthPlace.QUIET_BOOTROOM,.5,-8.5);})
            .thenWaitUntil(()->h.assertTrue(QuietRooms.rests(f.data(),p.getUUID())==4,"the boot room is a rest too"))
            .thenExecute(()->{
                touch(p,l,f.base(LabyrinthPlace.QUIET_BOOTROOM).offset(QuietRooms.BOOT_NOTE));
                h.assertTrue(open(p).equals(QuietNotes.title(3)),"the card by the hooks: "+open(p));p.closeContainer();
                stand(p,f,LabyrinthPlace.QUIET_SUNROOM,.5,-3.5);})
            .thenWaitUntil(()->h.assertTrue(QuietRooms.rests(f.data(),p.getUUID())==5,"the garden room is a rest too"))
            .thenExecute(()->{
                touch(p,l,f.base(LabyrinthPlace.QUIET_SUNROOM).offset(QuietRooms.SUN_NOTE));
                h.assertTrue(open(p).equals(QuietNotes.title(4)),"the note on the table: "+open(p));p.closeContainer();})
            .thenSucceed();
    }

    @GameTest(template="empty",batch="rest_rooms",timeoutTicks=100)
    public static void aRestGoesToTheRestRoomSeenLongestAgo(GameTestHelper h){
        var data=new LabyrinthData();var player=UUID.randomUUID();var random=RandomSource.create(261);
        var seen=new java.util.ArrayList<LabyrinthPlace>();
        for(int i=0;i<QuietRooms.FAMILY.size();i++){
            var next=QuietRooms.rest(data,player,random);
            h.assertTrue(!seen.contains(next),"a rest goes to a rest room not yet seen: "+next+" after "+seen);
            seen.add(next);data.visit(player,next);
            h.assertTrue(QuietRooms.weight(data,player,next)==0&&LabyrinthDealer.grayWeight(data,player,next,0)==0,"just seen, it does not come round again among the halls");
        }
        h.assertTrue(QuietRooms.rest(data,player,random)==seen.getFirst(),"every one seen: the one seen longest ago");
        for(int i=0;i<QuietRooms.REPEAT;i++)data.visit(player,LabyrinthPlace.STRAIGHT_HALL);
        h.assertTrue(QuietRooms.FAMILY.stream().allMatch(place->LabyrinthDealer.grayWeight(data,player,place,0)==1),"six rooms later, each can come round again, one weight each");
        h.succeed();
    }
}
