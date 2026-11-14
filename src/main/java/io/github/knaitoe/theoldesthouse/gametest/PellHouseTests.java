package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.journal.CoAuthor;
import io.github.knaitoe.theoldesthouse.journal.JournalPage;
import io.github.knaitoe.theoldesthouse.journal.Journals;
import io.github.knaitoe.theoldesthouse.journal.OwnHand;
import io.github.knaitoe.theoldesthouse.journal.TornPages;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.AfterBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import static io.github.knaitoe.theoldesthouse.labyrinth.PellThing.Kind.*;

/**
 * 0.4.518, the Pell house (docs/PELL_HOUSE_PROPOSAL.md). Each case builds the place itself, one case to a batch, as the literary
 * cases do.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_pell") @PrefixGameTestTemplate(false)
public final class PellHouseTests {
    private PellHouseTests(){}
    private static final LabyrinthPlace P=LabyrinthPlace.PELL_HOUSE;
    private static LiteraryTests.Fixture active;
    /**
     * A fixture with the whole lot held loaded, street to back fence. The yard and the pool lie further from the street than a test
     * host's view reaches from where most cases stand their readers; run 1577 (the long vignettes' half of the complete suite, on one
     * busy server) found the lane rope one length short a week in, a chunk's entities not yet loaded at the far end of the pool.
     */
    private static LiteraryTests.Fixture house(GameTestHelper h){done();active=new LiteraryTests.Fixture(h);active.chunks.hold(active.level(P),PellPeople.area(active.base(P)));return active;}
    private static void done(){if(active!=null){active.close();active=null;}PellHouse.clearAll();}
    private static boolean is(ServerLevel l,BlockPos b,int x,int y,int z,String material){return l.getBlockState(b.offset(x,y,z)).is(PellRegistry.material(material));}
    private static boolean fitting(ServerLevel l,BlockPos b,BlockPos at,PellBlock.Kind kind){return PellBlock.is(l.getBlockState(b.offset(at)),kind);}
    /** One of the house's things, of this kind and stage. */
    private static boolean thing(ServerLevel l,BlockPos b,int x,int y,int z,PellThing.Kind kind,int stage){var s=l.getBlockState(b.offset(x,y,z));return PellThing.is(s,kind)&&s.getValue(PellThing.STAGE)==stage;}
    private static Direction facing(ServerLevel l,BlockPos b,int x,int y,int z){return l.getBlockState(b.offset(x,y,z)).getValue(PellThing.FACING);}
    /** A cell of the house's outside wall, of this course, its brick turned this way. */
    private static boolean wall(ServerLevel l,BlockPos b,int x,int y,int z,int stage,Direction out){var s=l.getBlockState(b.offset(x,y,z));return PellBlock.is(s,PellBlock.Kind.WALL)&&s.getValue(PellBlock.STAGE)==stage&&s.getValue(PellBlock.FACING)==out;}
    /** 0.4.568: the hair length in the latest look sent to {@code to} of {@code who} as Tilly, kept in {@code seen} (-1 for none yet);
     *  reading it empties what was sent. */
    private static int lookHair(ServerPlayer to,ServerPlayer who,int[] seen){
        for(var packet:NativeTestPlayers.packets(to))if(packet instanceof net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket custom&&custom.payload() instanceof io.github.knaitoe.theoldesthouse.network.PellTillyPayload look&&look.player().equals(who.getUUID())&&look.gesture()>=0)seen[0]=look.hair();
        return seen[0];
    }
    /** Everything a reader has been told since this was last asked: reading it empties it. */
    private static List<String> told(ServerPlayer p){
        var out=new java.util.ArrayList<String>();
        for(var packet:NativeTestPlayers.packets(p))if(packet instanceof net.minecraft.network.protocol.game.ClientboundSystemChatPacket chat)out.add(chat.content().getString());
        return out;
    }
    /** Every cell of the ground a body can stand in and walk to from a start, without opening anything. */
    private static Set<BlockPos> walked(ServerLevel l,BlockPos b,BlockPos from){
        var seen=new HashSet<BlockPos>();var queue=new ArrayDeque<BlockPos>();seen.add(from);queue.add(from);
        while(!queue.isEmpty()){
            var at=queue.poll();
            for(var d:Direction.Plane.HORIZONTAL){
                var to=at.relative(d);
                if(to.getX()<PellSite.WEST||to.getX()>PellSite.EAST||to.getZ()<PellSite.NORTH||to.getZ()>0||seen.contains(to))continue;
                if(!step(l,b,at,to))continue;
                seen.add(to);queue.add(to);
            }
        }
        return seen;
    }
    /** Whether a body can step from one cell of the ground to the next: a body in the cell, and a body half way across the edge into it
     *  (a shut leaf or a fence stands on the cell's middle), with ground under it. */
    private static boolean step(ServerLevel l,BlockPos b,BlockPos at,BlockPos to){
        var here=b.offset(to);var edge=new AABB(b.getX()+(at.getX()+to.getX())/2.0+.2,b.getY()+.01,b.getZ()+(at.getZ()+to.getZ())/2.0+.2,b.getX()+(at.getX()+to.getX())/2.0+.8,b.getY()+1.79,b.getZ()+(at.getZ()+to.getZ())/2.0+.8);
        var body=new AABB(here.getX()+.2,here.getY()+.01,here.getZ()+.2,here.getX()+.8,here.getY()+1.79,here.getZ()+.8);
        return l.noCollision(body)&&l.noCollision(edge)&&!l.noCollision(body.move(0,-.5,0));
    }
    /** 0.4.548: the shortest way a body walks from one cell of the lot to another without opening anything, each cell in turn after
     *  the first (empty if there is none). */
    private static List<BlockPos> route(ServerLevel l,BlockPos b,BlockPos from,BlockPos to){
        var back=new java.util.HashMap<BlockPos,BlockPos>();var queue=new ArrayDeque<BlockPos>();back.put(from,from);queue.add(from);
        while(!queue.isEmpty()&&!back.containsKey(to)){
            var at=queue.poll();
            for(var d:Direction.Plane.HORIZONTAL){
                var next=at.relative(d);
                if(next.getX()<PellSite.WEST||next.getX()>PellSite.EAST||next.getZ()<PellSite.NORTH||next.getZ()>0||back.containsKey(next)||!step(l,b,at,next))continue;
                back.put(next,at);queue.add(next);
            }
        }
        var out=new java.util.ArrayList<BlockPos>();if(!back.containsKey(to))return out;
        for(var at=to;!at.equals(from);at=back.get(at))out.add(0,at);
        return out;
    }

    /**
     * The street and the lot. Built of the house's own materials, not the game's; June's the only watered lawn; every door answers a
     * hand as it should; from the House's door a body can walk to every room but June's, to the yard, the garage and the strip
     * behind the shed, and cannot get into the pool's yard or the shed; the pool is under its cover, shallow at one end and deep at
     * the other; the garage's window looks along the strip behind the shed; the first paper is in the letterbox; and the House
     * does not deal the place.
     */
    @GameTest(template="empty",batch="pell_built",timeoutTicks=1200)
    public static void theStreetAndTheLotAreBuiltOfTheirOwnThingsAndEveryDoorAnswersAsItShould(GameTestHelper h){
        var f=house(h);var p=f.player("pell_reader",P);var l=f.level(P);var b=f.base(P);
        h.startSequence().thenIdle(5).thenExecute(()->{
            // Its own materials.
            h.assertTrue(is(l,b,0,-1,-10,"asphalt")&&is(l,b,PellSite.CURB,-1,-10,"curb")&&is(l,b,PellSite.PATH,-1,-10,"concrete")&&is(l,b,9,-1,-10,"lawn_dry"),"the road, its curb, the footpath and a neighbour's dead lawn");
            h.assertTrue(is(l,b,-8,-1,-54,"lawn")&&is(l,b,0,-1,-54,"concrete")&&is(l,b,15,-1,-70,"concrete")&&is(l,b,-5,-1,-74,"paver"),"June's lawn is watered; the path, the drive, and the patio");
            h.assertTrue(wall(l,b,PellSite.HOUSE_W,0,-60,1,Direction.WEST)&&wall(l,b,PellSite.HOUSE_W,2,-60,0,Direction.WEST)&&wall(l,b,PellSite.HOUSE_W,1,-61,2,Direction.WEST)&&wall(l,b,5,2,PellSite.HOUSE_N,0,Direction.NORTH)&&wall(l,b,PellSite.HOUSE_E,1,-66,0,Direction.EAST)
                &&is(l,b,-3,PellSite.ROOF+1,-65,"roof"),"brick veneer turned to the weather on every side over a stained lowest course, a window bricked up in newer brick, and tiles over it");
            h.assertTrue(fitting(l,b,new BlockPos(PellSite.HOUSE_W,1,PellSite.HOUSE_N),PellBlock.Kind.WALL_CORNER)&&fitting(l,b,new BlockPos(PellSite.HOUSE_E,1,PellSite.HOUSE_S),PellBlock.Kind.WALL_CORNER)
                &&fitting(l,b,new BlockPos(PellSite.HOUSE_W-1,PellSite.ROOF,-65),PellBlock.Kind.EAVE)&&is(l,b,-3,PellSite.ROOF,-65,"ceiling"),"its corners, its eaves, and the ceiling inside");
            h.assertTrue(is(l,b,-3,-1,-66,"carpet")&&is(l,b,0,-1,-61,"carpet_worn")&&is(l,b,-13,-1,-68,"lino")&&is(l,b,15,-1,-90,"slab_oil")&&is(l,b,6,2,PellSite.SHED_S,"iron"),"carpet with its worn path, lino, the garage's stained slab, and the shed's iron");
            for(int x=PellSite.WEST;x<=PellSite.EAST;x++)for(int z=PellSite.NORTH;z<=0;z++){var s=l.getBlockState(b.offset(x,-1,z));
                h.assertFalse(s.is(Blocks.GRASS_BLOCK)||s.is(Blocks.DIRT)||s.isAir(),"the ground at "+x+","+z+" is one of the street's own surfaces: "+s);}
            // The doors, as built.
            h.assertTrue(fitting(l,b,PellSite.FRONT_DOOR,PellBlock.Kind.SCREEN)&&!PellSite.open(l,b,PellSite.FRONT_DOOR)&&fitting(l,b,PellSite.GARAGE_DOOR,PellBlock.Kind.GARAGE_DOOR)&&fitting(l,b,PellSite.SHED_DOOR,PellBlock.Kind.SHED_DOOR)
                &&fitting(l,b,PellSite.SLIDER,PellBlock.Kind.SLIDER)&&fitting(l,b,PellSite.SIDE_GATE,PellBlock.Kind.GATE)&&fitting(l,b,PellSite.POOL_GATE,PellBlock.Kind.POOL_GATE),"every door and gate is the house's own, and shut");
            h.assertTrue(l.getBlockState(b.offset(PellSite.POOL_GATE)).getValue(PellBlock.STAGE)==1,"the pool's gate is padlocked");
            h.assertTrue(l.getBlockState(b.offset(PellSite.GARAGE_DOOR)).getValue(PellBlock.FACING)==Direction.SOUTH,"the garage door's bolt is on the yard's side");
            // Shut, nobody gets in: from the House's door a body reaches the front step and the side gate, and nothing behind them.
            var out=walked(l,b,BlockPos.ZERO);
            h.assertTrue(out.contains(new BlockPos(0,0,-57))&&out.contains(new BlockPos(15,0,-80))&&out.contains(new BlockPos(5,0,-80)),"the front step, and down the drive to the yard: "+out.size());
            h.assertFalse(out.contains(new BlockPos(0,0,-60))||out.contains(new BlockPos(-5,0,-85))||out.contains(new BlockPos(6,0,-92))||out.contains(new BlockPos(13,0,-90)),"not into the house, the pool's yard, the shed or the garage");
            // A hand on each.
            told(p);
            for(var door:List.of(PellSite.JUNE_DOOR,PellSite.SHED_DOOR,PellSite.POOL_GATE,PellSite.SLIDER.west())){f.click(p,P,door);h.assertFalse(PellSite.open(l,b,door),"it does not open to a hand: "+door);}
        // What is said reaches a reader when the tick ends.
        }).thenIdle(2).thenExecute(()->{
            var why=told(p);
            h.assertTrue(why.contains(PellTexts.JUNES_DOOR)&&why.contains(PellTexts.PADLOCKED)&&why.contains(PellTexts.POOL_GATE)&&why.contains(PellTexts.FIXED_GLASS),"and the reader is told why of each: "+why);
            for(var door:List.of(PellSite.FRONT_DOOR,PellSite.NESSA_DOOR,PellSite.CALLUM_DOOR,PellSite.BATH_DOOR,PellSite.LAUNDRY_DOOR,PellSite.BACK_DOOR,PellSite.SLIDER,PellSite.GARAGE_DOOR,PellSite.SIDE_GATE)){
                f.click(p,P,door);h.assertTrue(PellSite.open(l,b,door),"it opens to a hand: "+door);
                if(l.getBlockState(b.offset(door)).getValue(PellBlock.KIND).leaf())h.assertTrue(PellSite.open(l,b,door.above()),"both cells of it: "+door);
            }
            f.click(p,P,PellSite.NESSA_DOOR.above());h.assertFalse(PellSite.open(l,b,PellSite.NESSA_DOOR),"a hand on its upper cell shuts it");f.click(p,P,PellSite.NESSA_DOOR);
            // Open, every room but June's, the yard by the house, the garage, and the strip behind the shed.
            var in=walked(l,b,BlockPos.ZERO);
            for(var room:List.of(new BlockPos(0,0,-60),new BlockPos(-10,0,-61),new BlockPos(-2,0,-61),new BlockPos(4,0,-61),new BlockPos(-5,0,-65),new BlockPos(-10,0,-70),new BlockPos(0,0,-70),new BlockPos(8,0,-70),
                    new BlockPos(0,0,-74),new BlockPos(-17,0,-65),new BlockPos(13,0,-90),new BlockPos(7,0,-96)))
                h.assertTrue(in.contains(room),"a body walks to "+room);
            h.assertFalse(in.contains(new BlockPos(8,0,-61))||in.contains(new BlockPos(-5,0,-85))||in.contains(new BlockPos(6,0,-92)),"and still not into June's room, the pool's yard or the shed");
            // The pool: under its cover; tiles, the band at the waterline; shallow at the south end and deep at the north.
            h.assertTrue(fitting(l,b,new BlockPos(PellSite.WATER_W,-1,PellSite.WATER_S),PellBlock.Kind.TARP)&&l.getBlockState(b.offset(-7,-1,-86)).getValue(PellBlock.STAGE)==1,"the cover lies on it, sagging in the middle");
            // 0.4.529: the sag holds one pond, its edges facing the dry cloth and its corners rounded, not a puddle in every cell.
            java.util.function.BiPredicate<BlockPos,Integer> cover=(at,stage)->l.getBlockState(b.offset(at)).getValue(PellBlock.STAGE)==stage;
            java.util.function.BiPredicate<BlockPos,Direction> facing=(at,d)->l.getBlockState(b.offset(at)).getValue(PellBlock.FACING)==d;
            h.assertTrue(cover.test(new BlockPos(-9,-1,-90),3)&&facing.test(new BlockPos(-9,-1,-90),Direction.NORTH)&&cover.test(new BlockPos(-5,-1,-83),3)&&facing.test(new BlockPos(-5,-1,-83),Direction.SOUTH)
                    &&cover.test(new BlockPos(-7,-1,-90),2)&&facing.test(new BlockPos(-7,-1,-90),Direction.NORTH)&&cover.test(new BlockPos(-9,-1,-86),2)&&facing.test(new BlockPos(-9,-1,-86),Direction.WEST)
                    &&cover.test(new BlockPos(-10,-1,-86),0)&&cover.test(new BlockPos(-7,-1,-91),0),"one pond in the sag: rounded corners, edges facing the dry cloth, dry beyond");
            h.assertTrue(l.getBlockState(b.offset(-7,-2,-82)).is(Blocks.WATER)&&is(l,b,-7,PellSite.SHALLOW,-82,"pool_tile")&&l.getBlockState(b.offset(-7,PellSite.DEEP+1,-90)).is(Blocks.WATER)&&is(l,b,-9,PellSite.DEEP,-90,"pool_tile"),"water over tile, shallow and deep");
            h.assertTrue(is(l,b,PellSite.WATER_W-1,-2,-85,"pool_band")&&is(l,b,PellSite.WATER_W-1,-1,-85,"coping"),"the band at the waterline under the coping");
            PellSite.pool(l,b,false);
            h.assertTrue(l.getBlockState(b.offset(-7,-1,-86)).is(Blocks.WATER)&&is(l,b,-9,PellSite.DEEP,-90,"pool_tile"),"uncovered, it is full to the brim");
            PellSite.pool(l,b,true);
            // The garage's window looks west along the strip behind the shed.
            h.assertTrue(fitting(l,b,PellSite.GARAGE_WINDOW,PellBlock.Kind.WINDOW),"the garage has its window");
            for(int x=PellSite.GARAGE_W-1;x>=PellSite.SHED_W;x--)h.assertTrue(l.getBlockState(b.offset(x,1,PellSite.GARAGE_WINDOW.getZ())).isAir()&&PellSite.behindShed(new net.minecraft.world.phys.Vec3(x+.5,0,PellSite.GARAGE_WINDOW.getZ()+.5)),"nothing stands between it and the back of the shed at "+x);
            // The first paper, and the House's dealing.
            h.assertTrue(fitting(l,b,PellSite.SOURCE,PellBlock.Kind.LETTERBOX)&&fitting(l,b,PellSite.ENDING,PellBlock.Kind.DOORMAT),"the letterbox by the path, and the mat at the step");
            var paper=LiteraryTexts.source(P).get(DataComponents.WRITTEN_BOOK_CONTENT);String text=String.join(" ",paper.pages().stream().map(pg->pg.raw().getString()).toList());
            h.assertTrue(text.contains("June Pell")&&text.contains("Gail Mercer"),"the placement notice");
            // June may be she and her; Tilly is a sister. The elder child, who is the reader, is neither.
            for(String word:List.of(" he "," his "," him ","brother"," boy"))h.assertFalse(text.toLowerCase(java.util.Locale.ROOT).contains(word),"the notice does not say what the elder child is: "+word);
            // 0.4.541: it can be finished, so it is dealt (once to each reader: PellHouse.dealable).
            h.assertTrue(PellHouse.dealable(f.data(),p.getUUID()),"a house that can be finished is dealt to a reader who has not finished it");
        }).thenSucceed();
    }
    @AfterBatch(batch="pell_built") public static void builtDone(ServerLevel l){done();}
    /**
     * 0.4.522, what is in the rooms (docs/PELL_HOUSE_THINGS_0_4_522.md). Every room has its things as on the first visit, each where
     * it belongs and turned the right way: things two cells wide read left to right as one faces them, things two cells long lie
     * head to foot. The circle is painted on Callum's floor with the mattress inside it; Nessa's ceiling has its stars and the floors
     * their marks. A hand on a thing gets its own line; the wardrobe opens and shuts, both cells; the garage's bulb goes off and on,
     * and its light with it; the clock says the time. Nothing stands across a doorway, and the flat things are walked over.
     */
    @GameTest(template="empty",batch="pell_things",timeoutTicks=1200)
    public static void everyRoomHasItsThingsAndEachAnswersAHand(GameTestHelper h){
        var f=house(h);var p=f.player("pell_things_reader",P);var l=f.level(P);var b=f.base(P);
        h.startSequence().thenIdle(5).thenExecute(()->{
            // The kitchen.
            h.assertTrue(thing(l,b,-13,0,-71,FRIDGE,0)&&thing(l,b,-13,1,-71,FRIDGE_TOP,0)&&thing(l,b,-10,0,-71,SINK,0)&&thing(l,b,-11,0,-71,KNIFE_BLOCK,0)&&thing(l,b,-8,0,-71,STOVE,0),
                "the fridge, the sink under the window, the knife block full, the stove");
            h.assertTrue(thing(l,b,-11,0,-69,TABLE,0)&&thing(l,b,-12,0,-69,TABLE,1)&&thing(l,b,-12,0,-70,CHAIR,0)&&facing(l,b,-12,0,-70)==Direction.SOUTH&&facing(l,b,-11,0,-68)==Direction.NORTH,
                "the table and its chairs, turned to it");
            h.assertTrue(thing(l,b,-13,1,-69,CHORE_CHART,0)&&thing(l,b,-13,1,-70,CHORE_CHART,1)&&PellThing.right(new BlockPos(-13,1,-69),Direction.EAST).equals(new BlockPos(-13,1,-70)),
                "the chore chart reads left to right from the room");
            h.assertTrue(is(l,b,-10,-1,-69,"lino_worn")&&is(l,b,-8,-1,-70,"lino_burn"),"the lino worn from the door to the sink, and burnt in front of the stove");
            // The living room and the hall.
            h.assertTrue(thing(l,b,-5,0,-70,TV_CABINET,0)&&thing(l,b,-2,0,-70,RECLINER,0)&&facing(l,b,-2,0,-70)==Direction.WEST&&thing(l,b,2,0,-71,WINDOW_SEAT,0),"the recliner turned to the television, the window seat");
            h.assertTrue(thing(l,b,-2,0,-71,SIDE_TABLE,0)&&thing(l,b,-3,0,-71,FLOOR_LAMP,0)&&PellThing.light(l.getBlockState(b.offset(-3,0,-71)))==0,"the side table at the chair's arm, the lamp off");
            for(int i=0;i<4;i++)h.assertTrue(thing(l,b,-8+i,1,-66,PHOTOS,i),"the school photographs in order: "+i);
            h.assertTrue(thing(l,b,-4,1,-66,HOOKS,0)&&thing(l,b,-2,1,-66,HOOKS,0)&&thing(l,b,1,1,-60,COAT_HOOK,0),"the empty hooks after them; the hook board by the door");
            // Nessa's room.
            h.assertTrue(thing(l,b,-13,0,-61,BED,0)&&thing(l,b,-12,0,-61,BED,1)&&facing(l,b,-13,0,-61)==Direction.EAST,"the bed, its head to the wall");
            h.assertTrue(thing(l,b,-8,0,-59,WARDROBE,0)&&thing(l,b,-8,1,-59,WARDROBE,1)&&thing(l,b,-11,0,-63,MARKS,0)&&thing(l,b,-11,1,-63,MARKS,3),"the wardrobe; the marks up the door frame, Tilly's among them");
            h.assertTrue(thing(l,b,-13,2,-61,BORDER,1)&&thing(l,b,-13,2,-63,BORDER,0)&&thing(l,b,-10,2,-63,BORDER,0),"the border round the top of the walls, crooked behind the bed");
            h.assertTrue(is(l,b,-10,PellSite.ROOF,-61,"ceiling_stars")&&is(l,b,-3,PellSite.ROOF,-65,"ceiling"),"stars on Nessa's ceiling, and nowhere else");
            // Callum's room: the circle on the floor, the mattress inside it.
            for(int i=0;i<12;i++){var s=l.getBlockState(b.offset(-5+i%3,-1,-62+i/3));h.assertTrue(PellThing.is(s,CIRCLE)&&PellThing.cell(s)==i,"cell "+i+" of the circle: "+s);}
            h.assertTrue(thing(l,b,-4,0,-60,MATTRESS,0)&&thing(l,b,-4,0,-61,MATTRESS,1)&&thing(l,b,-6,0,-61,CUTLERY,0),"the mattress in it; the cutlery, no knives");
            // The bathroom and the laundry.
            h.assertTrue(thing(l,b,5,0,-59,BATH,0)&&thing(l,b,4,0,-59,BATH,1)&&thing(l,b,3,1,-61,MIRROR,0),"the bath, its taps at the wall; the mirror");
            h.assertTrue(thing(l,b,9,0,-70,FREEZER,0)&&thing(l,b,9,0,-69,FREEZER,1)&&thing(l,b,6,0,-70,POWER,0)&&thing(l,b,7,0,-70,CORD,0)&&thing(l,b,8,0,-70,CORD,0),"the freezer, padlocked, and its cord from the power point");
            // The garage and the shed.
            h.assertTrue(thing(l,b,15,0,-89,CAMP_BED,0)&&thing(l,b,15,0,-90,CAMP_BED,1)&&is(l,b,15,-1,-90,"slab_oil")&&thing(l,b,15,2,-89,BULB,0),"the camp bed over the oil stain, the bulb over it");
            h.assertTrue(thing(l,b,17,0,-93,BOX_WINTER,0)&&thing(l,b,17,0,-92,BOX_WINTER,1)&&thing(l,b,17,0,-91,BOX_BOOKS,0)&&thing(l,b,17,0,-89,BOX_DONT,0)&&thing(l,b,17,0,-88,BOX_DONT,1)
                    &&thing(l,b,14,1,-95,PEGBOARD,0)&&thing(l,b,15,1,-95,PEGBOARD,1),
                "the boxes and the pegboard, read left to right from the room");
            h.assertTrue(thing(l,b,5,0,-93,TAPES,0)&&thing(l,b,5,1,-93,TAPES,1)&&thing(l,b,7,0,-93,WORKBENCH,1)&&thing(l,b,8,2,-92,TRANSCRIPT,2)&&thing(l,b,5,1,-92,SHED_CALENDAR,0),
                "the tapes, the bench and its empty tins, the transcript and the calendar");
            // Outside.
            h.assertTrue(thing(l,b,-12,0,-56,STAKE,0)&&thing(l,b,-2,0,-56,STAKE,1)&&thing(l,b,-14,0,-86,POOL_CHAIR,0)&&facing(l,b,-14,0,-86)==Direction.EAST,
                "the stakes, the newest by the step; the pool chair turned to the water");
            h.assertTrue(thing(l,b,19,0,-80,KENNEL,0)&&thing(l,b,19,0,-79,KENNEL,1)&&PellThing.right(new BlockPos(19,0,-80),Direction.WEST).equals(new BlockPos(19,0,-79))
                    &&thing(l,b,18,0,-79,BOWL,0),"the kennel, two cells, its name read left to right from the drive; the bowl at its door");
            h.assertTrue(is(l,b,1,-1,-54,"concrete_print")&&is(l,b,17,-1,-68,"concrete_oil"),"the handprint in the path and the stain on the drive");
            // Nothing stands across a doorway: the cell either side of every door is clear for a body.
            for(var door:List.of(PellSite.FRONT_DOOR,PellSite.NESSA_DOOR,PellSite.CALLUM_DOOR,PellSite.BATH_DOOR,PellSite.LAUNDRY_DOOR,PellSite.BACK_DOOR,PellSite.SLIDER,PellSite.GARAGE_DOOR,PellSite.SHED_DOOR)){
                var out=l.getBlockState(b.offset(door)).getValue(PellBlock.FACING);
                for(var side:List.of(door.relative(out),door.relative(out.getOpposite()))){var at=b.offset(side);
                    h.assertTrue(l.noCollision(new AABB(at.getX()+.2,at.getY()+.01,at.getZ()+.2,at.getX()+.8,at.getY()+1.79,at.getZ()+.8)),"the way through "+door+" is clear at "+side);}
            }
            // A hand on things.
            told(p);
            for(var at:List.of(new BlockPos(-10,0,-71),new BlockPos(-13,1,-71),new BlockPos(-5,0,-70),new BlockPos(-8,1,-66),new BlockPos(-6,0,-61),new BlockPos(9,0,-70),new BlockPos(17,0,-89),
                    new BlockPos(8,1,-93),new BlockPos(-2,0,-56),new BlockPos(19,0,-79),new BlockPos(-8,0,-63),new BlockPos(-2,0,-71)))
                f.click(p,P,at);
            f.click(p,P,new BlockPos(-8,1,-59));f.click(p,P,new BlockPos(15,2,-89));f.click(p,P,new BlockPos(-3,0,-71));
        }).thenIdle(2).thenExecute(()->{
            var said=told(p);
            for(var line:List.of(PellTexts.thing(SINK,0),PellTexts.thing(FRIDGE_TOP,0),PellTexts.thing(TV_CABINET,0),PellTexts.thing(PHOTOS,0),PellTexts.thing(CUTLERY,0),PellTexts.thing(FREEZER,0),
                    PellTexts.thing(BOX_DONT,0),PellTexts.thing(TIN,0),PellTexts.thing(STAKE,1),PellTexts.thing(KENNEL,1),PellTexts.thing(WARDROBE,2),PellTexts.thing(SIDE_TABLE,0),
                    PellTexts.thing(FLOOR_LAMP,1)))
                h.assertTrue(said.contains(line),"a hand gets: "+line+" (told "+said+")");
            h.assertTrue(said.stream().anyMatch(s->s.startsWith(PellTexts.thing(DRAWERS,0))&&s.contains("The time is")),"the clock on the drawers says the time: "+said);
            // The wardrobe opened, both cells, from a hand on the upper; the bulb went off.
            h.assertTrue(thing(l,b,-8,0,-59,WARDROBE,2)&&thing(l,b,-8,1,-59,WARDROBE,3),"the wardrobe is open, both cells");
            h.assertTrue(thing(l,b,15,2,-89,BULB,1)&&PellThing.light(l.getBlockState(b.offset(15,2,-89)))==0,"the bulb is off");
            h.assertTrue(thing(l,b,-3,0,-71,FLOOR_LAMP,1)&&PellThing.light(l.getBlockState(b.offset(-3,0,-71)))>0,"the lamp is on");
            f.click(p,P,new BlockPos(-8,0,-59));f.click(p,P,new BlockPos(15,2,-89));
            h.assertTrue(thing(l,b,-8,0,-59,WARDROBE,0)&&thing(l,b,-8,1,-59,WARDROBE,1),"and shut again, from a hand on the lower");
            h.assertTrue(thing(l,b,15,2,-89,BULB,0)&&PellThing.light(l.getBlockState(b.offset(15,2,-89)))>0,"and the bulb on again");
            // The flat things are walked over: with the doors open, a body crosses the cord, the circle and the hall under the photographs.
            for(var door:List.of(PellSite.FRONT_DOOR,PellSite.CALLUM_DOOR,PellSite.LAUNDRY_DOOR))f.click(p,P,door);
            var in=walked(l,b,BlockPos.ZERO);
            for(var cell:List.of(new BlockPos(8,0,-70),new BlockPos(-3,0,-60),new BlockPos(-5,0,-62),new BlockPos(-7,0,-66),new BlockPos(1,0,-60)))h.assertTrue(in.contains(cell),"a body walks to "+cell);
            h.assertFalse(in.contains(new BlockPos(-4,0,-61))||in.contains(new BlockPos(-11,0,-69)),"not through the mattress or the table");
        }).thenSucceed();
    }
    @AfterBatch(batch="pell_things") public static void thingsDone(ServerLevel l){done();}
    private static PellLine.Surface surface(ServerLevel l,BlockPos b,int x,int z){return l.getBlockState(b.offset(x,-1,z)).getValue(PellLine.SURFACE);}
    private static PellLine.Shape shape(ServerLevel l,BlockPos b,int x,int z){return l.getBlockState(b.offset(x,-1,z)).getValue(PellLine.SHAPE);}
    /**
     * 0.4.523, the line (docs/PELL_HOUSE_LINE_0_4_523.md). It goes round the whole lot unbroken: from the step, each cell runs out to
     * the next and the last back to the first. It is painted into the ground it lies on and takes as that ground does: the path's
     * concrete at the step, the drive where the wheels crossed it, the beds' mulch, the lawn, the patio's pavers, the pool fence's
     * footing twice, the deck, the pool yard's pavers; behind the shed it is at its thinnest, and only there. Nothing stands on it
     * but what was set there: the mat at the step, the side gate, the pool fence on its footing, the kennel. A body walks along it.
     * A hand on it gets what it is painted on; the mat lifts to show it running on under, and goes back down. Uncovering the pool
     * leaves it, and the ground round it, as they were.
     */
    @GameTest(template="empty",batch="pell_line",timeoutTicks=1200)
    public static void theLineGoesRoundTheWholeLotUnbrokenAndTakesAsItsGroundDoes(GameTestHelper h){
        var f=house(h);var p=f.player("pell_line_reader",P);var l=f.level(P);var b=f.base(P);
        h.startSequence().thenIdle(5).thenExecute(()->{
            // Round the whole lot, from the step: each cell runs out to the next, and the last back to the first.
            var start=new BlockPos(0,-1,PellSite.LINE_S);var at=start;Direction from=null;int cells=0;var seen=new HashSet<BlockPos>();
            do{
                var s=l.getBlockState(b.offset(at));
                h.assertTrue(PellLine.unbroken(s)&&s.getValue(PellLine.STAGE)==PellLine.FIRST,"the line is here, as on the first visit: "+at+" "+s);
                h.assertTrue(PellSite.onLine(at)&&seen.add(at),"on its route, and once: "+at);
                var ends=PellLine.ends(s);
                if(from!=null)h.assertTrue(ends.contains(from),"it runs in from where it came: "+at+" "+ends+" "+from);
                var out=from==null||ends.get(1)==from?ends.get(0):ends.get(1);
                at=at.relative(out);from=out.getOpposite();cells++;
            }while(!at.equals(start)&&cells<1000);
            h.assertTrue(at.equals(start)&&PellLine.ends(l.getBlockState(b.offset(start))).contains(from),"it comes back round to the step and joins up");
            h.assertTrue(cells==PellSite.route().size()&&cells==2*((PellSite.LINE_E-PellSite.LINE_W)+(PellSite.LINE_S-PellSite.LINE_N)),"the whole way round: "+cells+" cells");
            for(var corner:List.of(new BlockPos(PellSite.LINE_W,0,PellSite.LINE_S),new BlockPos(PellSite.LINE_E,0,PellSite.LINE_S),new BlockPos(PellSite.LINE_W,0,PellSite.LINE_N),new BlockPos(PellSite.LINE_E,0,PellSite.LINE_N)))
                h.assertTrue(shape(l,b,corner.getX(),corner.getZ())==PellLine.Shape.CORNER,"it turns at the corner "+corner);
            // What it is painted on.
            h.assertTrue(surface(l,b,0,PellSite.LINE_S)==PellLine.Surface.CONCRETE&&surface(l,b,1,PellSite.LINE_S)==PellLine.Surface.CONCRETE,"the path, just short of the step");
            for(int x:PellSite.WHEELS)h.assertTrue(surface(l,b,x,PellSite.LINE_S)==PellLine.Surface.TYRE,"across the drive where the wheels ran: "+x);
            h.assertTrue(surface(l,b,PellSite.DRIVE_W,PellSite.LINE_S)==PellLine.Surface.CONCRETE&&surface(l,b,15,PellSite.LINE_S)==PellLine.Surface.CONCRETE,"and the rest of the drive");
            h.assertTrue(surface(l,b,-5,PellSite.LINE_S)==PellLine.Surface.MULCH&&surface(l,b,5,PellSite.LINE_S)==PellLine.Surface.MULCH,"through the beds");
            h.assertTrue(surface(l,b,-16,PellSite.LINE_S)==PellLine.Surface.LAWN&&surface(l,b,PellSite.LINE_W,-65)==PellLine.Surface.LAWN&&surface(l,b,PellSite.LINE_E,-70)==PellLine.Surface.LAWN
                &&surface(l,b,15,PellSite.LINE_N)==PellLine.Surface.LAWN,"over the lawn: the front, the side passage, the east side, behind the garage");
            h.assertTrue(surface(l,b,PellSite.LINE_W,-74)==PellLine.Surface.PAVER&&surface(l,b,-5,PellSite.LINE_N)==PellLine.Surface.PAVER&&surface(l,b,PellSite.LINE_W,PellSite.LINE_N)==PellLine.Surface.PAVER,"the patio's pavers and the pool yard's");
            h.assertTrue(surface(l,b,PellSite.LINE_W,PellSite.POOL_FENCE_S)==PellLine.Surface.CONCRETE&&surface(l,b,PellSite.POOL_FENCE_E,PellSite.LINE_N)==PellLine.Surface.CONCRETE,"over the pool fence's footing, twice");
            h.assertTrue(surface(l,b,PellSite.LINE_W,-85)==PellLine.Surface.DECK&&is(l,b,-15,-1,-85,"deck"),"across the deck the chair stands on");
            h.assertTrue(is(l,b,-10,-1,PellSite.POOL_FENCE_S,"concrete")&&is(l,b,PellSite.POOL_FENCE_E,-1,-90,"concrete"),"the fence stands on its footing all along");
            for(int x=PellSite.SHED_W;x<=PellSite.SHED_E;x++)h.assertTrue(shape(l,b,x,PellSite.LINE_N)==PellLine.Shape.THIN,"behind the shed it is at its thinnest: "+x);
            h.assertTrue(shape(l,b,PellSite.SHED_W-1,PellSite.LINE_N)==PellLine.Shape.STRAIGHT&&shape(l,b,PellSite.SHED_E+1,PellSite.LINE_N)==PellLine.Shape.STRAIGHT,"and only there");
            // Nothing stands on it but what was set there.
            var kennel=List.of(new BlockPos(PellSite.LINE_E,0,-80),new BlockPos(PellSite.LINE_E,0,-79));
            for(var cell:PellSite.route()){
                var s=l.getBlockState(b.offset(cell));
                if(cell.equals(PellSite.ENDING))h.assertTrue(PellBlock.is(s,PellBlock.Kind.DOORMAT),"the mat lies over it at the step");
                else if(cell.equals(PellSite.SIDE_GATE))h.assertTrue(PellBlock.is(s,PellBlock.Kind.GATE),"it goes under the side gate");
                else if(cell.equals(new BlockPos(PellSite.LINE_W,0,PellSite.POOL_FENCE_S))||cell.equals(new BlockPos(PellSite.POOL_FENCE_E,0,PellSite.LINE_N)))h.assertTrue(PellBlock.is(s,PellBlock.Kind.POOL_FENCE),"the pool fence over it at "+cell);
                else if(kennel.contains(cell))h.assertTrue(PellThing.is(s,KENNEL),"it runs through the kennel at "+cell);
                else h.assertTrue(s.isAir(),"nothing else stands on it: "+cell+" "+s);
            }
            // A body walks along it, from the street.
            var out=walked(l,b,BlockPos.ZERO);
            for(var cell:List.of(new BlockPos(0,0,PellSite.LINE_S),new BlockPos(PellSite.LINE_E,0,PellSite.LINE_S),new BlockPos(PellSite.LINE_E,0,-70),new BlockPos(PellSite.LINE_E,0,-90),new BlockPos(6,0,PellSite.LINE_N)))
                h.assertTrue(out.contains(cell),"a body stands on the line at "+cell);
            // A hand on it, and on the mat.
            told(p);
            for(var cell:List.of(new BlockPos(PellSite.LINE_W,-1,-65),new BlockPos(6,-1,PellSite.LINE_N),new BlockPos(PellSite.WHEELS[0],-1,PellSite.LINE_S),new BlockPos(PellSite.LINE_W,-1,-85)))f.click(p,P,cell);
            f.click(p,P,PellSite.ENDING);
        }).thenIdle(2).thenExecute(()->{
            var said=told(p);
            for(var line:List.of(PellTexts.line(PellLine.Surface.LAWN,false,0),PellTexts.line(PellLine.Surface.LAWN,true,0),PellTexts.line(PellLine.Surface.TYRE,false,0),
                    PellTexts.line(PellLine.Surface.DECK,false,0),PellTexts.MAT_LIFTED))
                h.assertTrue(said.contains(line),"a hand gets: "+line+" (told "+said+")");
            h.assertTrue(l.getBlockState(b.offset(PellSite.ENDING)).getValue(PellBlock.STAGE)==1&&PellLine.unbroken(l.getBlockState(b.offset(PellSite.ENDING.below()))),"the mat is lifted, and the line runs on under it");
            f.click(p,P,PellSite.ENDING);
            h.assertTrue(l.getBlockState(b.offset(PellSite.ENDING)).getValue(PellBlock.STAGE)==0,"and it goes back down");
            // Uncovering the pool leaves the line, and the ground round it, as they were.
            PellSite.pool(l,b,false);
            h.assertTrue(PellLine.unbroken(l.getBlockState(b.offset(-5,-1,PellSite.LINE_N)))&&PellLine.unbroken(l.getBlockState(b.offset(PellSite.LINE_W,-1,-85))),"the pool uncovered, the line is as it was");
            h.assertTrue(is(l,b,-15,-1,-85,"deck")&&is(l,b,-5,-1,-95,"paver"),"and so is the ground round the pool");
            PellSite.pool(l,b,true);
        }).thenSucceed();
    }
    @AfterBatch(batch="pell_line") public static void lineDone(ServerLevel l){done();}
    /** How far, in degrees, a yaw is from looking straight at a point, from where this one stands. */
    private static double off(LiteraryActor a,float yaw,net.minecraft.world.phys.Vec3 at){
        var d=at.subtract(a.position());var look=net.minecraft.world.phys.Vec3.directionFromRotation(0,yaw);
        return Math.toDegrees(Math.acos(net.minecraft.util.Mth.clamp((look.x*d.x+look.z*d.z)/Math.sqrt(d.x*d.x+d.z*d.z),-1,1)));
    }
    private static LiteraryActor who(GameTestHelper h,ServerLevel l,BlockPos b,int who){var a=PellPeople.find(l,b,who);h.assertTrue(a!=null,PellPeople.name(who)+" is at the house");return a;}
    private static String said(LiteraryActor a){return a.getCustomName()==null?"":a.getCustomName().getString();}
    /**
     * 0.4.524, the people (docs/PELL_HOUSE_PEOPLE_0_4_524.md). The four are at the house when a reader comes up the street, each where
     * the first visit puts them and turned the way it says, in their own skins and sizes; Tilly holds her cane and Gail her folder,
     * and June and Callum hold nothing. Asked again, nobody is made twice, even with the house's memory of them gone (a reload); one
     * taken away is made again, and only that one. A hand on June, Gail and Tilly gets the first of their lines as a caption over
     * them, never in chat; a second hand while the line is up does not cut it short, and the next hand after it gets the next line.
     * A hand on Callum gets what he does, and no caption. A reader near June turns her head and not her body; Callum turns his whole
     * body to a reader he can watch from across the house.
     */
    @GameTest(template="empty",batch="pell_people",timeoutTicks=1600)
    public static void thePeopleStandWhereTheFirstVisitPutsThem(GameTestHelper h){
        var f=house(h);var p=f.player("pell_people_reader",P);var l=f.level(P);var b=f.base(P);
        // Up the street to just short of the letterbox: near enough for the ground the people stand on to have loaded, too far to be
        // looked at.
        f.at(p,P,.5,0,-44);
        var ids=new java.util.UUID[PellPeople.COUNT];
        h.startSequence().thenWaitUntil(()->{for(int i=0;i<PellPeople.COUNT;i++)h.assertTrue(PellPeople.find(l,b,i)!=null,"waiting for "+PellPeople.name(i));}).thenExecute(()->{
            for(int i=0;i<PellPeople.COUNT;i++){
                var a=who(h,l,b,i);var spot=PellPeople.FIRST_VISIT[i];
                h.assertTrue(Math.abs(a.getX()-(b.getX()+spot.x()))<.3&&Math.abs(a.getZ()-(b.getZ()+spot.z()))<.3&&Math.abs(a.getY()-b.getY())<.1,PellPeople.name(i)+" stands where the first visit puts them: "+a.position());
                h.assertTrue(Math.abs(net.minecraft.util.Mth.wrapDegrees(a.getYRot()-spot.look()))<1&&Math.abs(net.minecraft.util.Mth.wrapDegrees(a.getYHeadRot()-spot.look()))<1,PellPeople.name(i)+" faces the way the first visit says: "+a.getYRot()+" "+a.getYHeadRot());
                h.assertTrue(a.role()==LiteraryActor.STRANGER&&a.phase()==PellPeople.FIRST+i&&PellPeople.is(a)&&PellPeople.who(a)==i,PellPeople.name(i)+" in their own skin");
                ids[i]=a.getUUID();
            }
            var june=who(h,l,b,PellPeople.JUNE);var tilly=who(h,l,b,PellPeople.TILLY);var callum=who(h,l,b,PellPeople.CALLUM);var gail=who(h,l,b,PellPeople.GAIL);
            h.assertFalse(PellSite.onLine(BlockPos.containing(june.getX()-b.getX(),0,june.getZ()-b.getZ())),"June stands short of the line, not on it");
            h.assertTrue(Math.abs(june.getBbHeight()-gail.getBbHeight())<.01&&tilly.getBbHeight()<june.getBbHeight()*.7&&callum.getBbHeight()<tilly.getBbHeight(),
                "June and Gail are grown, Tilly and Callum children, Callum the smaller: "+june.getBbHeight()+" "+tilly.getBbHeight()+" "+callum.getBbHeight());
            h.assertTrue(tilly.getMainHandItem().is(PellRegistry.CANE.get())&&tilly.gesture()==PellPeople.CANE,"Tilly has her cane in her hand, held out ahead of her");
            // 0.4.568: and her hair as she came, to her jaw.
            h.assertTrue(PellPeople.hair(tilly.marks())==0,"on the first visit Tilly's hair is as she came: "+PellPeople.hair(tilly.marks()));
            h.assertTrue(gail.getMainHandItem().is(PellRegistry.FOLDER.get())&&gail.gesture()==PellPeople.FOLDER,"Gail has her folder, held to her chest");
            h.assertTrue(june.getMainHandItem().isEmpty()&&callum.getMainHandItem().isEmpty()&&june.getOffhandItem().isEmpty()&&callum.getOffhandItem().isEmpty(),"June and Callum hold nothing");
            // Asked again, nobody is made twice; nor with the house's memory of them gone, as after a reload. 0.4.540: the four, and
            // Nessa in the freezer.
            PellPeople.ensure(l,b);PellPeople.clearAll();PellPeople.ensure(l,b);
            var all=l.getEntitiesOfClass(LiteraryActor.class,PellPeople.area(b),PellPeople::is);
            h.assertTrue(all.size()==PellPeople.COUNT+1&&all.stream().map(PellPeople::who).distinct().count()==PellPeople.COUNT+1,"asked again, nobody is made twice: "+all.stream().map(a->PellPeople.name(PellPeople.who(a))).toList());
            for(int i=0;i<PellPeople.COUNT;i++)h.assertTrue(PellPeople.find(l,b,i).getUUID().equals(ids[i]),"the same "+PellPeople.name(i)+", taken up again");
            // Callum taken away is made again, where he stands on the first visit; nobody else is.
            callum.discard();PellPeople.ensure(l,b);
            var again=who(h,l,b,PellPeople.CALLUM);var spot=PellPeople.FIRST_VISIT[PellPeople.CALLUM];
            h.assertTrue(!again.getUUID().equals(ids[PellPeople.CALLUM])&&Math.abs(again.getX()-(b.getX()+spot.x()))<.3&&Math.abs(again.getZ()-(b.getZ()+spot.z()))<.3,"Callum, taken away, is made again where he stands");
            for(int i=0;i<PellPeople.COUNT;i++)if(i!=PellPeople.CALLUM)h.assertTrue(PellPeople.find(l,b,i).getUUID().equals(ids[i]),"and nobody else is: "+PellPeople.name(i));
            h.assertTrue(l.getEntitiesOfClass(LiteraryActor.class,PellPeople.area(b),PellPeople::is).size()==PellPeople.COUNT+1,"still four of them, and Nessa");
            // A hand on June, Gail and Tilly, from the path between them.
            f.at(p,P,3.5,0,-53.5);told(p);
            for(int i:new int[]{PellPeople.JUNE,PellPeople.GAIL,PellPeople.TILLY})LiteraryVignettes.talk(p,who(h,l,b,i));
            h.assertTrue(june.saying()&&said(june).equals(PellTexts.JUNE_TALK[0])&&june.speaker().equals("June"),"June says her first line, over her, with her name: "+said(june));
            h.assertTrue(gail.saying()&&said(gail).equals(PellTexts.GAIL_TALK[0])&&gail.speaker().equals("Gail"),"Gail says hers: "+said(gail));
            h.assertTrue(tilly.saying()&&said(tilly).equals(PellTexts.TILLY_TALK[0])&&tilly.speaker().equals("Tilly"),"Tilly says hers: "+said(tilly));
            LiteraryVignettes.talk(p,june);
            h.assertTrue(said(june).equals(PellTexts.JUNE_TALK[0]),"a second hand while June's line is up does not cut it short");
        }).thenIdle(2).thenExecute(()->{
            var chat=told(p);
            for(var line:List.of(PellTexts.JUNE_TALK[0],PellTexts.GAIL_TALK[0],PellTexts.TILLY_TALK[0]))h.assertFalse(chat.contains(line),"never in chat: "+line+" "+chat);
        }).thenWaitUntil(()->h.assertFalse(who(h,l,b,PellPeople.JUNE).saying(),"June's line stays up to be read")).thenExecute(()->{
            var june=who(h,l,b,PellPeople.JUNE);LiteraryVignettes.talk(p,june);
            h.assertTrue(said(june).equals(PellTexts.JUNE_TALK[1]),"the next hand gets June's next line: "+said(june));
            // A hand on Callum, from along the hall.
            f.at(p,P,-8.5,0,-65.5);told(p);
            LiteraryVignettes.talk(p,who(h,l,b,PellPeople.CALLUM));
        }).thenIdle(2).thenExecute(()->{
            var callum=who(h,l,b,PellPeople.CALLUM);
            h.assertTrue(told(p).contains(PellTexts.CALLUM_HAND[0]),"a hand on Callum gets what he does");
            h.assertFalse(callum.saying()||callum.getCustomName()!=null,"and he says nothing");
            // Near June, to one side of her: she turns her head, not her body.
            f.at(p,P,3.5,0,-53.5);
        }).thenIdle(10).thenExecute(()->{
            var june=who(h,l,b,PellPeople.JUNE);var rest=PellPeople.FIRST_VISIT[PellPeople.JUNE].look();
            h.assertTrue(off(june,june.getYHeadRot(),p.getEyePosition())<3&&off(june,rest,p.getEyePosition())>20,"June turns her head to a reader near her: "+off(june,june.getYHeadRot(),p.getEyePosition()));
            h.assertTrue(Math.abs(net.minecraft.util.Mth.wrapDegrees(june.getYRot()-rest))<1&&Math.abs(net.minecraft.util.Mth.wrapDegrees(june.yBodyRot-rest))<1,"and not her body: "+june.getYRot()+" "+june.yBodyRot);
            // At the step, across the house from Callum: he turns his whole body to keep them in sight.
            f.at(p,P,-.5,0,-56.5);
        }).thenIdle(15).thenExecute(()->{
            var callum=who(h,l,b,PellPeople.CALLUM);var rest=PellPeople.FIRST_VISIT[PellPeople.CALLUM].look();
            h.assertTrue(off(callum,rest,p.getEyePosition())>20&&off(callum,callum.getYRot(),p.getEyePosition())<3&&off(callum,callum.getYHeadRot(),p.getEyePosition())<3,
                "Callum turns his body and head to a reader across the house: "+callum.getYRot()+" "+off(callum,callum.getYRot(),p.getEyePosition()));
        }).thenSucceed();
    }
    @AfterBatch(batch="pell_people") public static void peopleDone(ServerLevel l){done();}
    private static int rules(LiteraryTests.Fixture f,ServerPlayer p){return f.own(p,P).getInt("Rules");}
    private static boolean at(LiteraryActor a,BlockPos b,double x,double z,double within){return a!=null&&Math.hypot(a.getX()-(b.getX()+x),a.getZ()-(b.getZ()+z))<within;}
    /**
     * 0.4.525, the first visit's day (docs/PELL_HOUSE_VISIT_0_4_525.md). Gail's car is at the curb. Nobody is greeted until a reader
     * comes up to Gail; then Gail greets them, June says hello, Gail hands over and says goodbye, and June gives the line at the step.
     * A second reader hanging back at the turning circle does not hear it. Gail goes, and her car with her, while they are inside
     * and cannot see. June shows them round, waiting at each stop for somebody: Callum from the hall; Nessa's room, opened; the pool,
     * through the glass; the garage across the yard, opened. Each rule is heard by whoever is within earshot, as their own: the reader
     * who went out to the garage has all four, the one who stayed in the living room has the two they were there for. Tilly follows
     * June in and goes into Nessa's room; June goes back to her kitchen. A hand on June gives a rule too. And when the house is left,
     * the visit begins again: everybody back where the first visit puts them, Gail and her car at the curb, June's doors shut.
     */
    @GameTest(template="empty",batch="pell_visit",timeoutTicks=6000)
    public static void theFirstVisitShowsThemRoundAndGivesJunesRules(GameTestHelper h){
        var f=house(h);var p=f.player("pell_visit_reader",P);var q=f.player("pell_visit_other",P);var l=f.level(P);var b=f.base(P);
        f.at(p,P,.5,0,-44);f.at(q,P,-6.5,0,-36);
        h.startSequence().thenWaitUntil(()->{for(int i=0;i<PellPeople.COUNT;i++)h.assertTrue(PellPeople.find(l,b,i)!=null,"waiting for "+PellPeople.name(i));h.assertTrue(PellVisit.car(l,b)!=null,"waiting for Gail's car");})
        .thenIdle(10).thenExecute(()->{
            var car=PellVisit.car(l,b);
            h.assertTrue(car.kind()==TownCar.BLUE&&car.parked()&&Math.abs(car.getX()-(b.getX()+PellVisit.CAR.x))<.1&&Math.abs(car.getZ()-(b.getZ()+PellVisit.CAR.z))<.1,"Gail's car is parked at the curb: "+car.position());
            h.assertTrue(PellVisit.step(b)==PellVisit.GREET&&!who(h,l,b,PellPeople.GAIL).saying(),"nobody is greeted before a reader comes up");
            // Up to Gail, from the street.
            f.at(p,P,.5,0,-49.5);
        }).thenWaitUntil(()->h.assertTrue(PellVisit.step(b)>=PellVisit.JUNE_HELLO,"Gail turns to the reader"))
        .thenExecute(()->h.assertTrue(said(who(h,l,b,PellPeople.GAIL)).equals(PellTexts.GAIL_GREETS),"Gail greets them first"))
        .thenWaitUntil(()->h.assertTrue(PellVisit.step(b)>=PellVisit.TOUR,"June gives the first rule at the step"))
        .thenExecute(()->{
            h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_LINE),"the line, at the step: "+said(who(h,l,b,PellPeople.JUNE)));
            h.assertTrue(rules(f,p)==PellTexts.RULE_LINE,"the reader at the step heard it: "+rules(f,p));
            h.assertTrue(rules(f,q)==0,"the reader back at the turning circle did not: "+rules(f,q));
            // Both go in ahead of June, to the passage from the front door, clear of her way.
            f.at(p,P,1.5,0,-63.5);f.at(q,P,1.5,0,-61.5);
        }).thenWaitUntil(()->h.assertTrue(PellVisit.step(b)>=PellVisit.TOUR+1,"June shows them Callum from the hall"))
        .thenExecute(()->{
            h.assertTrue(PellSite.open(l,b,PellSite.FRONT_DOOR),"she opened the front door on her way in");
            h.assertTrue(at(who(h,l,b,PellPeople.JUNE),b,.5,-65.5,.6),"June in the hall");
            // Along the hall to Nessa's door, the one reader nearer than the other.
            f.at(p,P,-5.5,0,-64.5);f.at(q,P,-1.5,0,-64.5);
        }).thenWaitUntil(()->h.assertTrue(PellVisit.gailGone(b)&&PellVisit.car(l,b)==null&&PellPeople.find(l,b,PellPeople.GAIL)==null,"Gail and her car go while they are inside"))
        .thenWaitUntil(()->h.assertTrue(PellVisit.step(b)>=PellVisit.TOUR+2,"June shows them Nessa's room"))
        .thenExecute(()->{
            h.assertTrue(PellSite.open(l,b,PellSite.NESSA_DOOR),"she opened Nessa's door");
            h.assertTrue((rules(f,p)&PellTexts.RULE_NESSA)!=0&&(rules(f,q)&PellTexts.RULE_NESSA)!=0,"both heard it");
            // Into the living room, either side of the way June comes.
            f.at(p,P,2.5,0,-69.5);f.at(q,P,-3.5,0,-68.5);
        }).thenWaitUntil(()->h.assertTrue(PellVisit.step(b)>=PellVisit.TOUR+3,"June shows them the pool through the glass"))
        .thenExecute(()->{
            h.assertTrue((rules(f,p)&PellTexts.RULE_POOL)!=0&&(rules(f,q)&PellTexts.RULE_POOL)!=0,"both heard the pool gate");
            // One goes out to the garage; the other stays in the living room, out of earshot of it.
            f.at(p,P,12.5,0,-82);
        }).thenWaitUntil(()->h.assertTrue(PellVisit.step(b)>=PellVisit.TOUR+4,"June shows the garage"))
        .thenExecute(()->{
            h.assertTrue(PellSite.open(l,b,PellSite.SLIDER)&&PellSite.open(l,b,PellSite.GARAGE_DOOR),"she went out by the sliding door and opened the garage");
            h.assertTrue(rules(f,p)==PellTexts.RULES,"the reader at the garage has all four of June's rules: "+rules(f,p));
            h.assertTrue(rules(f,q)==(PellTexts.RULE_NESSA|PellTexts.RULE_POOL),"the other has the two they were there for: "+rules(f,q));
        }).thenWaitUntil(()->h.assertTrue(PellVisit.step(b)==PellVisit.SETTLED,"June goes back to her kitchen"))
        .thenWaitUntil(()->h.assertTrue(at(who(h,l,b,PellPeople.TILLY),b,PellVisit.TILLY_ROOM.x(),PellVisit.TILLY_ROOM.z(),.7),"Tilly is in Nessa's room: "+who(h,l,b,PellPeople.TILLY).position()))
        .thenExecute(()->{
            var june=who(h,l,b,PellPeople.JUNE);
            h.assertTrue(at(june,b,PellVisit.KITCHEN.x(),PellVisit.KITCHEN.z(),.6),"June is in her kitchen: "+june.position());
            var s=PellPeople.FIRST_VISIT[PellPeople.CALLUM];
            h.assertTrue(at(who(h,l,b,PellPeople.CALLUM),b,s.x(),s.z(),.3),"Callum has not moved from the end of the hall");
            // A hand on June gives a rule too: the other reader, in the kitchen, gets the line from her.
            f.at(q,P,-8.5,0,-67.5);june.hush();
            LiteraryVignettes.talk(q,june);
            h.assertTrue(said(june).equals(PellTexts.JUNE_TALK[0]),"her first line to a hand: "+said(june));
            june.hush();LiteraryVignettes.talk(q,june);
            h.assertTrue(said(june).equals(PellTexts.JUNE_TALK[1])&&(rules(f,q)&PellTexts.RULE_LINE)!=0,"and the line, which the other reader now has: "+rules(f,q));
            // Everybody out in the street, and the house left: when somebody is next at it, the visit begins again.
            f.at(p,P,.5,0,-30);f.at(q,P,-6.5,0,-30);
            PellPeople.vacate(b);
        }).thenWaitUntil(()->h.assertTrue(PellPeople.find(l,b,PellPeople.GAIL)!=null&&!PellVisit.gailGone(b)&&PellVisit.car(l,b)!=null&&PellVisit.step(b)==PellVisit.GREET,"the visit begins again"))
        .thenExecute(()->{
            for(int i=0;i<PellPeople.COUNT;i++){var s=PellPeople.FIRST_VISIT[i];h.assertTrue(at(who(h,l,b,i),b,s.x(),s.z(),.3),PellPeople.name(i)+" is back where the first visit puts them");}
            for(var door:PellVisit.HER_DOORS)h.assertFalse(PellSite.open(l,b,door),"June's door is shut again: "+door);
            h.assertTrue(rules(f,p)==PellTexts.RULES,"and each reader keeps what they heard");
        }).thenSucceed();
    }
    @AfterBatch(batch="pell_visit") public static void visitDone(ServerLevel l){done();}

    /** A hand on a cell of the house, from wherever the reader is: the event a right-click sends, nothing moved. */
    private static void hand(ServerPlayer p,BlockPos at){
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock(p,net.minecraft.world.InteractionHand.MAIN_HAND,at,new net.minecraft.world.phys.BlockHitResult(at.getCenter(),Direction.UP,at,false)));
    }
    /** The plates lying on the table, as bodies. */
    private static List<net.minecraft.world.entity.Display.ItemDisplay> plates(ServerLevel l,BlockPos b){return l.getEntitiesOfClass(net.minecraft.world.entity.Display.ItemDisplay.class,PellPeople.area(b),d->d.getTags().contains(PellVisit.PLATE_TAG));}
    /** Somebody sitting in this place's chair. */
    private static boolean inChair(LiteraryActor a,BlockPos b,int place){var c=PellVisit.TABLE[place].chair();return a!=null&&a.blockPosition().equals(b.offset(c));}
    /**
     * 0.4.526, the first evening (docs/PELL_HOUSE_DINNER_0_4_526.md). The day settled, the house's hour goes on to six and holds there.
     * June calls; a reader out in the garage hears it from there, the one in the living room hears June herself. She lays four
     * places; Tilly sits by the hall door and Callum by the stove. With a reader at the table she serves: Tilly first, then a place for
     * each reader (where the one sitting is sitting, then a free one for the other), and Callum's plate she passes over. The reader at
     * the table saw Tilly served; the one in the garage did not. A hand on the table at a reader's place eats their dinner, once; at
     * Callum's, his plate is empty. With the plates done she clears, Tilly and Callum go back where they were, and the evening goes on
     * to half past eight and holds. Left, the house begins again: the afternoon, the table bare.
     */
    @GameTest(template="empty",batch="pell_dinner",timeoutTicks=6000)
    public static void dinnerAtSixServesTillyFirst(GameTestHelper h){
        var f=house(h);var p=f.player("pell_dinner_reader",P);var q=f.player("pell_dinner_other",P);var l=f.level(P);var b=f.base(P);
        f.at(p,P,.5,0,-44);f.at(q,P,-6.5,0,-36);
        long[] mark={0};int[] fed={0};
        h.startSequence().thenWaitUntil(()->{for(int i=0;i<PellPeople.COUNT;i++)h.assertTrue(PellPeople.find(l,b,i)!=null,"waiting for "+PellPeople.name(i));h.assertTrue(PellVisit.car(l,b)!=null,"waiting for the visit to begin");})
        .thenExecute(()->{
            h.assertTrue(PellVisit.hour(b)==PellVisit.AFTERNOON,"the visit begins in the late afternoon: "+PellVisit.hour(b));
            PellVisit.settle(l,b);mark[0]=PellVisit.hour(b);
            // One reader out in the garage, one in the living room, near enough to hear June in the kitchen.
            f.at(p,P,13.5,0,-92.5);f.at(q,P,-2.5,0,-68.5);
        }).thenIdle(20).thenExecute(()->{
            h.assertTrue(PellVisit.hour(b)>=mark[0]+15&&PellVisit.step(b)==PellVisit.SETTLED,"once the day is settled the hour goes on toward six: "+mark[0]+" to "+PellVisit.hour(b));
            told(p);told(q);PellVisit.hurry(b,PellVisit.SIX-5);
        }).thenWaitUntil(()->h.assertTrue(PellVisit.step(b)>=PellVisit.CALLED,"June calls them at six"))
        .thenExecute(()->{
            h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_CALLS),"June calls: "+said(who(h,l,b,PellPeople.JUNE)));
            h.assertTrue(told(p).contains(PellTexts.CALLED_FAR),"the reader in the garage hears the call from there");
            h.assertFalse(told(q).contains(PellTexts.CALLED_FAR),"the reader in the living room hears June herself");
        }).thenWaitUntil(()->h.assertTrue(PellVisit.step(b)>=PellVisit.LAID,"June lays the table"))
        .thenExecute(()->{
            for(int i=0;i<4;i++)h.assertTrue(PellVisit.plate(b,i)==PellVisit.LAID_PLATE,"a place laid at "+i);
            h.assertTrue(plates(l,b).size()==4,"four plates on the table: "+plates(l,b).size());
            h.assertTrue(PellVisit.diner(b,0)==PellPeople.TILLY&&PellVisit.diner(b,1)==PellPeople.CALLUM,"Tilly's place by the hall door and Callum's by the stove");
        }).thenWaitUntil(()->h.assertTrue(PellVisit.placeOf(b,PellPeople.TILLY)==0&&PellVisit.placeOf(b,PellPeople.CALLUM)==1,"Tilly and Callum sit down"))
        .thenExecute(()->{
            h.assertTrue(inChair(who(h,l,b,PellPeople.TILLY),b,0)&&inChair(who(h,l,b,PellPeople.CALLUM),b,1),"each in their chair");
            h.assertTrue(PellVisit.step(b)==PellVisit.LAID,"nobody is served before a reader comes");
            // The reader in the living room comes and sits at the far side of the table.
            f.at(q,P,-9.5,0,-67.5);h.assertTrue(io.github.knaitoe.theoldesthouse.house.HouseSitting.sit(q,b.offset(PellVisit.TABLE[3].chair())),"a reader sits at the table");
        }).thenWaitUntil(()->h.assertTrue(PellVisit.step(b)>=PellVisit.EATING,"June serves"))
        .thenExecute(()->{
            var order=PellVisit.served(b);
            h.assertTrue(order.equals(List.of(0,3,2)),"Tilly first, then the reader where they sit, then a place for the other: "+order);
            h.assertTrue(PellVisit.plate(b,0)==PellVisit.SERVED&&PellVisit.plate(b,3)==PellVisit.SERVED&&PellVisit.plate(b,2)==PellVisit.SERVED,"three dinners on the table");
            h.assertTrue(PellVisit.plate(b,1)==PellVisit.LAID_PLATE,"Callum's plate is passed over");
            h.assertTrue(f.own(q,P).getBoolean("SawTillyServed")&&!f.own(p,P).getBoolean("SawTillyServed"),"the reader at the table saw Tilly served first; the one in the garage did not");
            // A hand on the table at the reader's own place: their dinner, eaten. (What a reader is told reaches them at the end of the
            // tick it was said in, so it is read on the next.)
            q.getFoodData().setFoodLevel(8);told(q);hand(q,b.offset(-12,0,-69));
            h.assertTrue(q.getFoodData().getFoodLevel()==14&&PellVisit.plate(b,3)==PellVisit.SCRAPED&&f.own(q,P).getBoolean("Ate"),"the reader eats their dinner: "+q.getFoodData().getFoodLevel());
        }).thenIdle(1).thenExecute(()->{
            var heard=told(q);h.assertTrue(heard.contains(PellTexts.ATE),"and is told what it was: "+heard);
            hand(q,b.offset(-12,0,-69));
            h.assertTrue(q.getFoodData().getFoodLevel()==14,"a second hand eats nothing more");
        }).thenIdle(1).thenExecute(()->{
            h.assertTrue(told(q).contains(PellTexts.SCRAPED_PLATE),"a second hand finds the plate scraped");
            // The other comes in from the garage. Beside Callum's chair, the table's nearest place is his: nothing on it.
            f.at(p,P,-9.5,0,-69.5);told(p);hand(p,b.offset(-11,0,-69));
        }).thenIdle(1).thenExecute(()->{
            h.assertTrue(told(p).contains(PellTexts.CALLUMS_PLATE),"Callum's plate has nothing on it");
            LiteraryVignettes.talk(p,who(h,l,b,PellPeople.CALLUM));
        }).thenIdle(1).thenExecute(()->{
            h.assertTrue(told(p).contains(PellTexts.CALLUM_TABLE[0]),"a hand on Callum at the table");
            // Then sits at the place laid for them, and eats.
            h.assertTrue(io.github.knaitoe.theoldesthouse.house.HouseSitting.sit(p,b.offset(PellVisit.TABLE[2].chair())),"the other reader sits");
            hand(p,b.offset(-12,0,-69));
            h.assertTrue(PellVisit.plate(b,2)==PellVisit.SCRAPED&&f.own(p,P).getBoolean("Ate"),"and eats theirs");
        }).thenWaitUntil(()->h.assertTrue(PellVisit.plate(b,0)==PellVisit.SCRAPED,"Tilly eats hers"))
        .thenWaitUntil(()->h.assertTrue(PellVisit.step(b)>=PellVisit.CLEARING,"June clears"))
        .thenExecute(()->{
            h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_CLEARS),"\"Leave those\"");
            h.assertTrue(PellVisit.placeOf(b,PellPeople.TILLY)<0&&PellVisit.placeOf(b,PellPeople.CALLUM)<0,"Tilly and Callum get down");
        }).thenWaitUntil(()->h.assertTrue(PellVisit.step(b)>=PellVisit.CLEARED&&plates(l,b).isEmpty(),"the table cleared"))
        .thenExecute(()->{
            for(int i=0;i<4;i++)h.assertTrue(PellVisit.plate(b,i)==PellVisit.NONE,"nothing left at "+i);
            h.assertTrue(who(h,l,b,PellPeople.JUNE).getMainHandItem().is(PellRegistry.SCRAPED.get()),"June takes the plates to the sink");
            mark[0]=PellVisit.hour(b);
        }).thenWaitUntil(()->h.assertTrue(at(who(h,l,b,PellPeople.TILLY),b,PellVisit.TILLY_ROOM.x(),PellVisit.TILLY_ROOM.z(),.7),"Tilly goes back to Nessa's room"))
        .thenWaitUntil(()->{var s=PellPeople.FIRST_VISIT[PellPeople.CALLUM];h.assertTrue(at(who(h,l,b,PellPeople.CALLUM),b,s.x(),s.z(),.7),"Callum goes back to the end of the hall");})
        .thenExecute(()->{
            h.assertTrue(PellVisit.hour(b)>mark[0],"after dinner the evening goes on: "+mark[0]+" to "+PellVisit.hour(b));
            PellVisit.hurry(b,PellVisit.EVENING-3);
        }).thenIdle(10).thenExecute(()->h.assertTrue(PellVisit.hour(b)==PellVisit.EVENING,"and holds at half past eight: "+PellVisit.hour(b)))
        .thenIdle(10).thenExecute(()->{
            h.assertTrue(PellVisit.hour(b)==PellVisit.EVENING,"still half past eight");
            // Everybody out in the street, and the house left: the afternoon again, the table bare.
            f.at(p,P,.5,0,-30);f.at(q,P,-6.5,0,-30);
            PellPeople.vacate(b);
        }).thenWaitUntil(()->h.assertTrue(PellVisit.step(b)==PellVisit.GREET&&PellVisit.car(l,b)!=null,"the visit begins again"))
        .thenExecute(()->{
            h.assertTrue(PellVisit.hour(b)==PellVisit.AFTERNOON&&plates(l,b).isEmpty(),"the afternoon again, the table bare");
            h.assertTrue(PellVisit.placeOf(b,PellPeople.TILLY)<0&&PellVisit.placeOf(b,PellPeople.CALLUM)<0,"nobody sitting");
            h.assertTrue(f.own(p,P).getBoolean("Ate")&&f.own(q,P).getBoolean("Ate"),"what each reader did stays theirs");
        }).thenSucceed();
    }
    @AfterBatch(batch="pell_dinner") public static void dinnerDone(ServerLevel l){done();}

    /**
     * 0.4.527, bedtime (docs/PELL_HOUSE_BEDTIME_0_4_527.md). After the washing up June goes to her chair; a reader in it is asked for it
     * once, and she stands beside it until they get up, then sits. The television plays a birthday tape, and a hand on it finds what is
     * on it; a hand on June finds her talking about the girl on it. At half past eight she turns it off and calls Tilly to bed; on the
     * edge of Nessa's bed she brushes Tilly's hair, seen by the reader in the room and not by the one in the garage. Tilly lies down,
     * asleep, her cane put down, and Callum lies down in his circle. June sends the reader still in the house out to the garage and waits
     * until they have gone; then she locks the sliding door, the back door and the front door from inside, and goes to her room and
     * shuts its door. From outside a locked door does not open. The hour goes on into the night, faster while June is in her room
     * (0.4.530; the night itself is the next case). Left, the house begins again:
     * nothing locked, the television off, nobody asleep, Tilly with her cane, and each reader keeping what they saw.
     */
    @GameTest(template="empty",batch="pell_bedtime",timeoutTicks=9000)
    public static void bedtimeLocksTheDoors(GameTestHelper h){
        var f=house(h);var p=f.player("pell_bed_reader",P);var q=f.player("pell_bed_other",P);var l=f.level(P);var b=f.base(P);
        f.at(p,P,.5,0,-44);f.at(q,P,-6.5,0,-36);
        long[] mark={0};
        h.startSequence().thenWaitUntil(()->{for(int i=0;i<PellPeople.COUNT;i++)h.assertTrue(PellPeople.find(l,b,i)!=null,"waiting for "+PellPeople.name(i));h.assertTrue(PellVisit.car(l,b)!=null,"waiting for the visit to begin");})
        .thenExecute(()->{
            PellVisit.dined(l,b);
            // One reader sits in June's chair before she gets to it; the other waits in the hall.
            f.at(p,P,-2.5,0,-69.5);h.assertTrue(io.github.knaitoe.theoldesthouse.house.HouseSitting.sit(p,b.offset(PellVisit.RECLINER.cell())),"a reader sits in June's chair");
            f.at(q,P,-4.5,0,-65.5);
        }).thenWaitUntil(()->h.assertTrue(PellVisit.step(b)>=PellVisit.TELLY,"after the washing up June goes through to the living room"))
        .thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_MY_CHAIR),"June asks for her chair"))
        .thenExecute(()->{
            h.assertTrue(PellVisit.tvOn(l,b),"the television is on");
            h.assertTrue(PellVisit.perched(b,PellPeople.JUNE)==null,"June stands beside her chair while the reader is in it");
            p.stopRiding();
        }).thenWaitUntil(()->h.assertTrue(PellVisit.perched(b,PellPeople.JUNE)==PellVisit.RECLINER,"the reader gets up, and June sits"))
        .thenExecute(()->{
            h.assertTrue(PellVisit.watching(b),"June watches the tape");
            told(p);hand(p,b.offset(PellVisit.TV));
        }).thenIdle(1).thenExecute(()->{
            var heard=told(p);h.assertTrue(heard.contains(PellTexts.TV_TAPE[0]),"a hand on the television finds what is on the tape: "+heard);
            h.assertTrue(f.own(p,P).getBoolean("SawTape"),"and the reader has seen it");
        }).thenWaitUntil(()->h.assertFalse(who(h,l,b,PellPeople.JUNE).saying(),"June's line has had its time"))
        .thenExecute(()->{
            LiteraryVignettes.talk(p,who(h,l,b,PellPeople.JUNE));
            h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_WATCHING[0]),"a hand on June, watching: the girl on it: "+said(who(h,l,b,PellPeople.JUNE)));
            // One reader into Nessa's room, clear of the way through it; the other out to the garage.
            f.at(q,P,-8.5,0,-61.5);f.at(p,P,13.5,0,-92.5);
            PellVisit.hurry(b,PellVisit.EVENING-3);
        }).thenWaitUntil(()->h.assertTrue(PellVisit.step(b)>=PellVisit.BEDTIME,"half past eight: bedtime"))
        .thenExecute(()->{
            h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_BEDTIME),"\"Bedtime, Tilly\": "+said(who(h,l,b,PellPeople.JUNE)));
            h.assertFalse(PellVisit.tvOn(l,b),"the television off");
        }).thenWaitUntil(()->h.assertTrue(PellVisit.step(b)>=PellVisit.BRUSHING,"June brushes Tilly's hair"))
        .thenExecute(()->{
            var june=who(h,l,b,PellPeople.JUNE);var tilly=who(h,l,b,PellPeople.TILLY);
            h.assertTrue(PellVisit.perched(b,PellPeople.JUNE)==PellVisit.JUNE_BED&&PellVisit.perched(b,PellPeople.TILLY)==PellVisit.TILLY_BED,"both on the edge of Nessa's bed");
            h.assertTrue(june.getMainHandItem().is(PellRegistry.HAIRBRUSH.get())&&june.gesture()==PellPeople.BRUSH&&tilly.gesture()==PellPeople.BRUSHED,"June with the brush, brushing");
        }).thenWaitUntil(()->h.assertTrue(PellVisit.step(b)>=PellVisit.TUCKED,"Tilly is put to bed"))
        .thenExecute(()->{
            var tilly=who(h,l,b,PellPeople.TILLY);
            h.assertTrue(PellVisit.asleep(b,PellPeople.TILLY)&&tilly.gesture()==PellPeople.ASLEEP&&tilly.getMainHandItem().isEmpty(),"Tilly asleep, her cane put down");
            h.assertTrue(f.own(q,P).getBoolean("SawHairBrushed")&&!f.own(p,P).getBoolean("SawHairBrushed"),"the reader in the room saw her hair brushed; the one in the garage did not");
            told(q);LiteraryVignettes.talk(q,tilly);
        }).thenIdle(1).thenExecute(()->h.assertTrue(told(q).contains(PellTexts.TILLY_ASLEEP),"a hand on her: asleep"))
        .thenWaitUntil(()->h.assertTrue(PellVisit.asleep(b,PellPeople.CALLUM),"Callum lies down"))
        .thenExecute(()->{
            var callum=who(h,l,b,PellPeople.CALLUM);
            h.assertTrue(at(callum,b,PellVisit.CALLUM_SLEEPS.x,PellVisit.CALLUM_SLEEPS.z,.3)&&callum.gesture()==PellPeople.ASLEEP,"in his circle");
            // The reader in the house goes and stands in the living room.
            f.at(q,P,-1.5,0,-68.5);
        }).thenWaitUntil(()->h.assertTrue(PellVisit.step(b)>=PellVisit.SENDING,"June sends them out"))
        .thenExecute(()->{
            h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_SENDS_OUT),"\"Out to the garage\": "+said(who(h,l,b,PellPeople.JUNE)));
            h.assertTrue(f.own(q,P).getBoolean("SentToGarage")&&!f.own(p,P).getBoolean("SentToGarage"),"said to the reader in the house");
        }).thenIdle(40).thenExecute(()->{
            h.assertTrue(PellVisit.step(b)==PellVisit.SENDING&&!PellVisit.locked(b,PellSite.SLIDER),"she waits while a reader is in the house");
            // Out through the sliding door onto the patio.
            f.at(q,P,.5,0,-74.5);
        }).thenWaitUntil(()->h.assertTrue(PellVisit.locked(b,PellSite.SLIDER)&&!PellSite.open(l,b,PellSite.SLIDER),"the sliding door shut and locked"))
        .thenExecute(()->{told(q);hand(q,b.offset(PellSite.SLIDER));})
        .thenIdle(1).thenExecute(()->h.assertTrue(told(q).contains(PellTexts.locked(PellSite.SLIDER))&&!PellSite.open(l,b,PellSite.SLIDER),"from outside it does not open"))
        .thenWaitUntil(()->h.assertTrue(PellVisit.step(b)>=PellVisit.NIGHT,"locked up"))
        .thenExecute(()->{
            for(var door:PellVisit.LOCKS)h.assertTrue(PellVisit.locked(b,door)&&!PellSite.open(l,b,door),"locked: "+door);
            h.assertTrue(f.own(p,P).getBoolean("LockedOut")&&f.own(q,P).getBoolean("LockedOut"),"both readers were outside when she locked up");
        }).thenWaitUntil(()->h.assertTrue(at(who(h,l,b,PellPeople.JUNE),b,PellVisit.HER_ROOM.x(),PellVisit.HER_ROOM.z(),.7)&&!PellSite.open(l,b,PellSite.JUNE_DOOR),"June in her room, its door shut"))
        .thenExecute(()->mark[0]=PellVisit.hour(b))
        .thenIdle(10).thenExecute(()->{
            h.assertTrue(PellVisit.hour(b)>=mark[0]+8*PellNight.NIGHT_PACE,"the hour goes on into the night, faster while June is in her room: "+mark[0]+" to "+PellVisit.hour(b));
            h.assertTrue(PellVisit.step(b)==PellVisit.NIGHT,"and she stays there until eleven");
            // Everybody out in the street, and the house left.
            f.at(p,P,.5,0,-30);f.at(q,P,-6.5,0,-30);PellPeople.vacate(b);
        }).thenWaitUntil(()->h.assertTrue(PellVisit.step(b)==PellVisit.GREET&&PellVisit.car(l,b)!=null,"the visit begins again"))
        .thenExecute(()->{
            for(var door:PellVisit.LOCKS)h.assertFalse(PellVisit.locked(b,door),"nothing locked: "+door);
            h.assertFalse(PellVisit.tvOn(l,b),"the television off");
            var tilly=who(h,l,b,PellPeople.TILLY);
            h.assertTrue(!PellVisit.asleep(b,PellPeople.TILLY)&&!PellVisit.asleep(b,PellPeople.CALLUM),"nobody asleep");
            h.assertTrue(tilly.gesture()==PellPeople.CANE&&tilly.getMainHandItem().is(PellRegistry.CANE.get()),"Tilly with her cane again");
            h.assertTrue(f.own(q,P).getBoolean("SawHairBrushed")&&f.own(p,P).getBoolean("SawTape"),"what each reader saw stays theirs");
        }).thenSucceed();
    }
    @AfterBatch(batch="pell_bedtime") public static void bedtimeDone(ServerLevel l){done();}

    private static void face(ServerPlayer p,float yaw,float pitch){p.setYRot(yaw);p.setYHeadRot(yaw);p.setXRot(pitch);}
    private static void lookAt(ServerPlayer p,net.minecraft.world.phys.Vec3 at){p.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,at);}
    /**
     * 0.4.530, the first night (docs/PELL_HOUSE_NIGHT_0_4_530.md). Locked up, the hour goes on fast while June is in her room, and at
     * eleven she comes out with her lantern, unlocks the back door, goes out and locks it behind her; takes the tin from the shed and
     * locks the shed behind her; sets the lantern down at the end of the strip behind the shed, the tin open beside her; and kneels with
     * the brush and paints the thin stretch of the line cell by cell, leaving it wet. The reader at the garage window sees her painting
     * from the garage; the reader in the yard, looking the other way, does not. Callum is out on the line by the drive at the first
     * moment nobody sees, standing on it, facing in. When the reader in the yard looks at him and crouches, he crouches a beat late and
     * stands a beat after they do, and a hand on him finds him there. A foot in the wet paint smears it, June says so, and she paints it
     * over before she packs up. Then the tin goes back on the bench, the lantern goes with her, the shed and the back door are locked
     * behind her, and June is in her room with the door shut and her hands empty. The small hours go on (0.4.531: toward the morning,
     * the next cases) with Callum still on the line. Left, the house begins again: the line as it was, the tin on the bench, Callum back
     * at the end of the hall, and what each reader saw kept.
     */
    @GameTest(template="empty",batch="pell_night",timeoutTicks=9000)
    public static void theFirstNightJuneRepaintsTheLineAndCallumStandsOnIt(GameTestHelper h){
        var f=house(h);var p=f.player("pell_night_window",P);var q=f.player("pell_night_yard",P);var l=f.level(P);var b=f.base(P);
        f.at(p,P,.5,0,-44);f.at(q,P,-6.5,0,-36);
        long[] mark={0};
        h.startSequence().thenWaitUntil(()->{for(int i=0;i<PellPeople.COUNT;i++)h.assertTrue(PellPeople.find(l,b,i)!=null,"waiting for "+PellPeople.name(i));h.assertTrue(PellVisit.car(l,b)!=null,"waiting for the visit to begin");})
        .thenExecute(()->{
            PellNight.lockedUp(l,b);
            // One reader in the garage at its window, looking out along the strip behind the shed; the other in the yard below the
            // shed, looking toward the pool.
            f.at(p,P,13.5,0,-94.5);face(p,90,8);
            f.at(q,P,8.5,0,-82.5);face(q,90,0);
            mark[0]=PellVisit.hour(b);
        }).thenIdle(10).thenExecute(()->{
            h.assertTrue(PellVisit.hour(b)>=mark[0]+8*PellNight.NIGHT_PACE,"locked up, the hour goes on fast: "+mark[0]+" to "+PellVisit.hour(b));
            h.assertTrue(PellVisit.step(b)==PellVisit.NIGHT,"June stays in her room until eleven");
            PellVisit.hurry(b,PellNight.ELEVEN-6);
        }).thenWaitUntil(()->h.assertTrue(PellVisit.step(b)>=PellNight.ROUNDS,"eleven: June gets up"))
        .thenExecute(()->h.assertTrue(who(h,l,b,PellPeople.JUNE).getOffhandItem().is(PellRegistry.LANTERN.get()),"with her lantern"))
        .thenWaitUntil(()->h.assertTrue(PellVisit.step(b)>=PellNight.TO_SHED,"out by the back door"))
        .thenExecute(()->{
            h.assertTrue(PellVisit.locked(b,PellSite.BACK_DOOR)&&!PellSite.open(l,b,PellSite.BACK_DOOR),"the back door shut and locked behind her");
            var june=who(h,l,b,PellPeople.JUNE);
            h.assertFalse(PellSite.inHouse(june.position().subtract(b.getX(),b.getY(),b.getZ())),"June outside");
            // The reader in the yard tries the back door behind her.
            f.at(q,P,8.5,0,-74.5);told(q);hand(q,b.offset(PellSite.BACK_DOOR));
        }).thenIdle(1).thenExecute(()->{
            h.assertTrue(told(q).contains(PellTexts.BACK_LOCKED_OUT)&&!PellSite.open(l,b,PellSite.BACK_DOOR),"from outside it is locked, and June has the key");
            f.at(q,P,8.5,0,-82.5);face(q,90,0);
        })
        .thenWaitUntil(()->h.assertTrue(PellVisit.step(b)>=PellNight.TO_LINE,"the tin from the shed"))
        .thenExecute(()->{
            var june=who(h,l,b,PellPeople.JUNE);
            h.assertFalse(PellThing.is(l.getBlockState(b.offset(PellNight.BENCH_TIN)),TIN),"gone from the bench");
            h.assertTrue(june.getMainHandItem().is(PellRegistry.PAINT_TIN.get()),"in June's hand");
            h.assertFalse(PellSite.open(l,b,PellSite.SHED_DOOR),"the shed shut behind her");
        }).thenWaitUntil(()->h.assertTrue(PellVisit.step(b)>=PellNight.PAINTING,"at the line"))
        .thenExecute(()->{
            var june=who(h,l,b,PellPeople.JUNE);
            h.assertTrue(thing(l,b,11,0,-96,LANTERN,0),"the lantern set down by the passage");
            h.assertTrue(thing(l,b,10,0,-96,TIN,1),"the tin open beside her");
            h.assertTrue(june.getMainHandItem().is(PellRegistry.PAINTBRUSH.get()),"the brush in her hand");
        }).thenWaitUntil(()->h.assertTrue(who(h,l,b,PellPeople.JUNE).gesture()==PellPeople.PAINT,"kneeling to it"))
        .thenWaitUntil(()->h.assertTrue(PellNight.stage(l,b,9)==PellLine.LAST,"the first cell painted, and wet"))
        .thenExecute(()->{
            h.assertTrue(f.own(p,P).getBoolean("SawPainting")&&f.own(p,P).getBoolean("SawFromGarage"),"the reader at the garage window saw her painting, from the garage");
            h.assertFalse(f.own(q,P).getBoolean("SawPainting"),"the reader in the yard, looking the other way, did not");
            h.assertTrue(f.own(q,P).getBoolean("SawJuneOut"),"though June went by them on her way to the shed");
        }).thenWaitUntil(()->h.assertTrue(PellNight.callumOut(b),"Callum out"))
        .thenExecute(()->{
            var callum=who(h,l,b,PellPeople.CALLUM);
            h.assertTrue(at(callum,b,PellNight.CALLUM_LINE.x(),PellNight.CALLUM_LINE.z(),.3)&&!PellVisit.asleep(b,PellPeople.CALLUM)&&callum.gesture()!=PellPeople.ASLEEP,"standing by the drive");
            h.assertTrue(PellLine.is(l.getBlockState(b.offset(19,-1,-84))),"on the line");
            h.assertFalse(f.own(q,P).getBoolean("SawCallumOnLine"),"nobody saw him come");
            // The reader in the yard comes round below the garage, turns to him and crouches.
            f.at(q,P,14.5,0,-82.5);lookAt(q,callum.getEyePosition());q.setShiftKeyDown(true);q.setPose(net.minecraft.world.entity.Pose.CROUCHING);
        }).thenIdle(PellNight.BEAT/2).thenExecute(()->h.assertTrue(who(h,l,b,PellPeople.CALLUM).gesture()!=PellPeople.CROUCH,"not at once"))
        .thenWaitUntil(()->h.assertTrue(who(h,l,b,PellPeople.CALLUM).gesture()==PellPeople.CROUCH,"a beat late, he crouches as the reader does"))
        .thenExecute(()->{
            h.assertTrue(f.own(q,P).getBoolean("SawCallumOnLine"),"the reader saw him on the line");
            q.setShiftKeyDown(false);q.setPose(net.minecraft.world.entity.Pose.STANDING);
            told(q);LiteraryVignettes.talk(q,who(h,l,b,PellPeople.CALLUM));
        }).thenIdle(1).thenExecute(()->h.assertTrue(told(q).contains(PellTexts.CALLUM_NIGHT[0]),"a hand on him finds him standing on the line"))
        .thenWaitUntil(()->h.assertTrue(who(h,l,b,PellPeople.CALLUM).gesture()==0,"a beat after the reader stands, so does he"))
        // The reader goes up the passage into the strip and puts a foot in the wet paint.
        .thenWaitUntil(()->h.assertTrue(PellNight.stage(l,b,8)==PellLine.LAST,"the second cell painted"))
        .thenExecute(()->f.at(q,P,9.5,0,-96.5))
        .thenIdle(2).thenExecute(()->{
            h.assertTrue(PellNight.stage(l,b,9)==PellLine.FOOTPRINT&&f.own(q,P).getBoolean("Smeared"),"a foot in the wet paint smears it");
            f.at(q,P,10.5,0,-94.5);
        }).thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_MIND_PAINT),"June says so"))
        .thenWaitUntil(()->h.assertTrue(PellVisit.step(b)>=PellNight.PACKING,"painted, the smear painted over"))
        .thenExecute(()->{for(int x:PellNight.CELLS)h.assertTrue(PellNight.stage(l,b,x)==PellLine.LAST,"wet at "+x);})
        .thenWaitUntil(()->h.assertTrue(PellVisit.step(b)==PellNight.SMALL_HOURS,"home, and in her room"))
        .thenExecute(()->{
            var june=who(h,l,b,PellPeople.JUNE);
            h.assertTrue(thing(l,b,8,1,-93,TIN,0),"the tin back on the bench");
            h.assertFalse(PellThing.is(l.getBlockState(b.offset(PellNight.LANTERN_AT)),LANTERN),"the lantern taken in");
            h.assertFalse(PellSite.open(l,b,PellSite.SHED_DOOR),"the shed shut");
            h.assertTrue(PellVisit.locked(b,PellSite.BACK_DOOR)&&!PellSite.open(l,b,PellSite.BACK_DOOR),"the back door locked again");
            h.assertTrue(at(june,b,PellVisit.HER_ROOM.x(),PellVisit.HER_ROOM.z(),.7)&&!PellSite.open(l,b,PellSite.JUNE_DOOR),"June in her room, its door shut");
            h.assertTrue(june.getMainHandItem().isEmpty()&&june.getOffhandItem().isEmpty(),"her hands empty");
            h.assertTrue(PellNight.callumOut(b),"Callum still on the line");
            h.assertTrue(f.own(p,P).getBoolean("SawJuneOut"),"the reader in the garage saw June out");
            mark[0]=PellVisit.hour(b);
        }).thenIdle(10).thenExecute(()->{
            h.assertTrue(PellVisit.hour(b)>mark[0],"the hour goes on through the small hours: "+mark[0]+" to "+PellVisit.hour(b));
            // Everybody out in the street, and the house left.
            f.at(p,P,.5,0,-30);f.at(q,P,-6.5,0,-30);PellPeople.vacate(b);
        }).thenWaitUntil(()->h.assertTrue(PellVisit.step(b)==PellVisit.GREET&&PellVisit.car(l,b)!=null,"the visit begins again"))
        .thenExecute(()->{
            for(int x:PellNight.CELLS)h.assertTrue(PellNight.stage(l,b,x)==PellLine.FIRST,"the line as the first visit has it at "+x);
            h.assertTrue(thing(l,b,8,1,-93,TIN,0),"the tin on the bench");
            h.assertFalse(PellNight.callumOut(b),"Callum in");
            h.assertTrue(at(who(h,l,b,PellPeople.CALLUM),b,PellPeople.FIRST_VISIT[PellPeople.CALLUM].x(),PellPeople.FIRST_VISIT[PellPeople.CALLUM].z(),.3),"at the end of the hall");
            h.assertTrue(f.own(p,P).getBoolean("SawFromGarage")&&f.own(q,P).getBoolean("Smeared")&&f.own(q,P).getBoolean("SawCallumOnLine"),"what each reader saw and did stays theirs");
        }).thenSucceed();
    }
    @AfterBatch(batch="pell_night") public static void nightDone(ServerLevel l){done();}

    /** The camp bed in the garage: its head, where a hand on either cell lies the reader down. */
    private static final BlockPos CAMP_BED_AT=new BlockPos(15,0,-89);
    /**
     * 0.4.531, the first morning (docs/PELL_HOUSE_MORNING_0_4_531.md). Locked up, both readers out in the garage. One lies down on the
     * camp bed, and while the other is up the night does not hurry. The other lies down too, and it does: the hour goes on fast to
     * eleven, when the back door's lock, as June lets herself out, wakes them both, and each keeps that it did. Lying down again, they
     * sleep through June's rounds and the small hours. At first light the stretch she painted has dried and Callum is in; June gets up,
     * unlocks the three outside doors on her way to the kitchen and lays breakfast, and at seven her call wakes them, heard from the
     * garage. Tilly, with her cane, and Callum come to the table. The readers come in and sit at the places laid for them; Tilly says
     * somebody was singing in the night, in the wall by her bed, and June says it will be the pipes, and both readers heard it. Each
     * eats their breakfast. June says to leave the plates, Tilly and Callum get down, and the table is cleared. Left, the house begins
     * again, and what each reader did stays theirs.
     */
    @GameTest(template="empty",batch="pell_morning",timeoutTicks=12000)
    public static void theNightPassesWhileTheySleepAndJuneCallsThemToBreakfast(GameTestHelper h){
        var f=house(h);var p=f.player("pell_morning_reader",P);var q=f.player("pell_morning_other",P);var l=f.level(P);var b=f.base(P);
        f.at(p,P,.5,0,-44);f.at(q,P,-6.5,0,-36);
        var bed=b.offset(CAMP_BED_AT);
        h.startSequence().thenWaitUntil(()->{for(int i=0;i<PellPeople.COUNT;i++)h.assertTrue(PellPeople.find(l,b,i)!=null,"waiting for "+PellPeople.name(i));h.assertTrue(PellVisit.car(l,b)!=null,"waiting for the visit to begin");})
        .thenExecute(()->{
            PellNight.lockedUp(l,b);
            // Both readers out in the garage, beside the camp bed.
            f.at(p,P,14.5,0,-89.5);f.at(q,P,13.5,0,-91.5);
            hand(p,bed);
            h.assertTrue(p.isSleeping()&&f.own(p,P).getBoolean("SleptInGarage"),"a hand on the camp bed lies the reader down on it");
        }).thenIdle(PellMorning.SETTLE+5).thenExecute(()->{
            h.assertTrue(p.isSleeping(),"and they stay there");
            h.assertFalse(PellMorning.hurried(b),"with the other reader up, the night does not hurry");
            hand(q,b.offset(CAMP_BED_AT.north()));
            h.assertTrue(q.isSleeping(),"the other lies down too, from the bed's foot");
            told(p);told(q);
        }).thenWaitUntil(()->h.assertTrue(PellMorning.hurried(b),"with every reader at the house asleep, the night hurries on"))
        .thenWaitUntil(()->h.assertTrue(PellVisit.step(b)>=PellNight.ROUNDS&&!p.isSleeping()&&!q.isSleeping(),"eleven: June goes out, and the back door's lock wakes them"))
        .thenIdle(1).thenExecute(()->{
            h.assertTrue(told(p).contains(PellTexts.WOKEN_BY_LOCK)&&told(q).contains(PellTexts.WOKEN_BY_LOCK),"each is told what woke them");
            h.assertTrue(f.own(p,P).getBoolean("WokenByTheLock")&&f.own(q,P).getBoolean("WokenByTheLock"),"and keeps that it did");
            h.assertFalse(PellMorning.hurried(b),"awake, the night goes at its own pace");
            hand(p,bed);hand(q,bed);
            h.assertTrue(p.isSleeping()&&q.isSleeping(),"they lie down again");
            told(p);told(q);
        }).thenWaitUntil(()->h.assertTrue(PellVisit.step(b)>=PellMorning.CALLED,"they sleep through to breakfast"))
        .thenIdle(1).thenExecute(()->{
            h.assertFalse(p.isSleeping()||q.isSleeping(),"June's call wakes them");
            h.assertTrue(told(p).contains(PellTexts.BREAKFAST_FAR)&&told(q).contains(PellTexts.BREAKFAST_FAR),"heard from the garage");
            // 0.4.537: the night kept, and the first visit's night gives no entry in their journals.
            h.assertTrue(f.own(p,P).getInt("GarageNights")==1<<1&&f.own(q,P).getInt("GarageNights")==1<<1,"each keeps that they slept out there on the first visit");
            h.assertTrue(PellEntries.due(f.own(p,P)).isEmpty()&&PellEntries.due(f.own(q,P)).isEmpty(),"and no entry is due for the first visit's night");
            h.assertTrue(PellVisit.hour(b)>=PellMorning.SEVEN,"at seven: "+PellVisit.hour(b));
            for(int x:PellNight.CELLS)h.assertTrue(PellNight.stage(l,b,x)==PellLine.SECOND,"the stretch June painted in the night has dried at "+x);
            h.assertFalse(PellNight.callumOut(b),"Callum in off the line");
            for(var door:PellVisit.LOCKS)h.assertFalse(PellVisit.locked(b,door),"June has unlocked "+door);
            h.assertTrue(PellVisit.diner(b,0)==PellPeople.TILLY&&PellVisit.plate(b,0)==PellVisit.BREAKFAST,"Tilly's breakfast at her place");
            h.assertTrue(PellVisit.diner(b,1)==PellPeople.CALLUM&&PellVisit.plate(b,1)==PellVisit.LAID_PLATE,"Callum's plate with nothing on it");
            for(int i=2;i<4;i++)h.assertTrue(PellVisit.diner(b,i)==PellVisit.GUEST&&PellVisit.plate(b,i)==PellVisit.BREAKFAST,"a breakfast for a reader at "+i);
            h.assertTrue(plates(l,b).size()==4,"four plates on the table: "+plates(l,b).size());
            h.assertFalse(PellVisit.asleep(b,PellPeople.TILLY)||PellVisit.asleep(b,PellPeople.CALLUM),"Tilly and Callum up");
        }).thenWaitUntil(()->h.assertTrue(PellVisit.placeOf(b,PellPeople.TILLY)==0&&PellVisit.placeOf(b,PellPeople.CALLUM)==1,"Tilly and Callum come to the table"))
        .thenExecute(()->{
            h.assertTrue(who(h,l,b,PellPeople.TILLY).getMainHandItem().is(PellRegistry.CANE.get()),"Tilly with her cane");
            // The readers come in from the garage and sit at the places laid for them.
            f.at(q,P,-9.5,0,-67.5);h.assertTrue(io.github.knaitoe.theoldesthouse.house.HouseSitting.sit(q,b.offset(PellVisit.TABLE[3].chair())),"a reader sits at the table");
            f.at(p,P,-9.5,0,-69.5);h.assertTrue(io.github.knaitoe.theoldesthouse.house.HouseSitting.sit(p,b.offset(PellVisit.TABLE[2].chair())),"the other sits");
        }).thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.TILLY)).equals(PellTexts.TILLY_SINGING),"Tilly: somebody was singing last night"))
        .thenExecute(()->h.assertTrue(f.own(p,P).getBoolean("HeardAboutSinging")&&f.own(q,P).getBoolean("HeardAboutSinging"),"both readers heard her say so"))
        .thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_WAS_THERE),"June: was there?"))
        .thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.TILLY)).equals(PellTexts.TILLY_IN_THE_WALL),"Tilly: in the wall by her bed"))
        .thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_PIPES),"June: the pipes"))
        .thenExecute(()->{
            // A hand on the table at each reader's own place: their breakfast, eaten.
            p.getFoodData().setFoodLevel(8);told(p);
            hand(p,b.offset(-12,0,-69));hand(q,b.offset(-12,0,-69));
            h.assertTrue(PellVisit.plate(b,2)==PellVisit.SCRAPED&&PellVisit.plate(b,3)==PellVisit.SCRAPED,"each eats their breakfast");
            h.assertTrue(p.getFoodData().getFoodLevel()==13,"a breakfast's worth: "+p.getFoodData().getFoodLevel());
            h.assertTrue(f.own(p,P).getBoolean("AteBreakfast")&&f.own(q,P).getBoolean("AteBreakfast"),"each keeps that they ate");
        }).thenIdle(1).thenExecute(()->{var heard=told(p);h.assertTrue(heard.contains(PellTexts.ATE_BREAKFAST),"and is told what it was: "+heard);})
        .thenWaitUntil(()->h.assertTrue(PellVisit.step(b)>=PellMorning.AFTER,"breakfast done"))
        .thenExecute(()->{
            h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_CLEARS),"\"Leave those\"");
            h.assertTrue(PellVisit.plate(b,0)==PellVisit.SCRAPED,"Tilly has eaten hers");
            h.assertTrue(PellVisit.placeOf(b,PellPeople.TILLY)<0&&PellVisit.placeOf(b,PellPeople.CALLUM)<0,"Tilly and Callum get down");
            h.assertTrue(f.own(p,P).getBoolean("FirstMorning")&&f.own(q,P).getBoolean("FirstMorning"),"each keeps that they had the first morning");
        }).thenWaitUntil(()->h.assertTrue(plates(l,b).isEmpty(),"the table cleared"))
        .thenWaitUntil(()->h.assertTrue(at(who(h,l,b,PellPeople.TILLY),b,PellVisit.TILLY_ROOM.x(),PellVisit.TILLY_ROOM.z(),.7),"Tilly goes back to Nessa's room"))
        .thenExecute(()->{
            h.assertTrue(PellVisit.hour(b)<=PellMorning.EIGHT,"the morning goes on no further than eight: "+PellVisit.hour(b));
            // Everybody out in the street, and the house left.
            f.at(p,P,.5,0,-30);f.at(q,P,-6.5,0,-30);PellPeople.vacate(b);
        }).thenWaitUntil(()->h.assertTrue(PellVisit.step(b)==PellVisit.GREET&&PellVisit.car(l,b)!=null,"the visit begins again"))
        .thenExecute(()->{
            h.assertFalse(PellMorning.dawned(b),"night and morning undone");
            h.assertTrue(plates(l,b).isEmpty()&&PellVisit.hour(b)==PellVisit.AFTERNOON,"the afternoon again, the table bare");
            h.assertTrue(f.own(p,P).getBoolean("SleptInGarage")&&f.own(q,P).getBoolean("SleptInGarage")&&f.own(q,P).getBoolean("AteBreakfast"),"what each reader did stays theirs");
        }).thenSucceed();
    }
    @AfterBatch(batch="pell_morning") public static void morningDone(ServerLevel l){done();}

    /**
     * 0.4.531, the first morning with one reader awake. In the small hours, one reader asleep in the garage and the other out by the
     * drive, the night does not hurry. The reader awake steps in the wet paint behind the shed. At first light the stretch dries, and the
     * smear dries as it was. Callum, watched, stays out on the line through June's getting up and unlocking and her call to breakfast,
     * which wakes the reader asleep. Once breakfast is called the camp bed is only looked at. June calls Callum in, and the moment
     * nobody sees he is in his chair at the table. A hand on June in the morning gets her morning lines, and a hand on the smear finds it
     * dried as it was. Left, the house begins again, the line as it was and Callum at the end of the hall.
     */
    @GameTest(template="empty",batch="pell_morning_watched",timeoutTicks=12000)
    public static void watchedCallumStaysOutUntilNobodySeesAndJuneCallsHimIn(GameTestHelper h){
        var f=house(h);var p=f.player("pell_watch_sleeper",P);var q=f.player("pell_watch_waker",P);var l=f.level(P);var b=f.base(P);
        f.at(p,P,.5,0,-44);f.at(q,P,-6.5,0,-36);
        var bed=b.offset(CAMP_BED_AT);
        h.startSequence().thenWaitUntil(()->{for(int i=0;i<PellPeople.COUNT;i++)h.assertTrue(PellPeople.find(l,b,i)!=null,"waiting for "+PellPeople.name(i));h.assertTrue(PellVisit.car(l,b)!=null,"waiting for the visit to begin");})
        .thenExecute(()->{
            PellMorning.smallHours(l,b);
            f.at(p,P,14.5,0,-89.5);hand(p,bed);
            h.assertTrue(p.isSleeping(),"one reader asleep in the garage");
            // The other steps into the wet paint behind the shed.
            f.at(q,P,9.5,0,-96.5);
        }).thenIdle(2).thenExecute(()->{
            h.assertTrue(PellNight.stage(l,b,9)==PellLine.FOOTPRINT&&f.own(q,P).getBoolean("Smeared"),"a foot in the wet paint smears it");
            // Then out by the drive, looking at Callum on the line.
            f.at(q,P,14.5,0,-82.5);lookAt(q,who(h,l,b,PellPeople.CALLUM).getEyePosition());
        }).thenIdle(PellMorning.SETTLE+5).thenExecute(()->{
            h.assertFalse(PellMorning.hurried(b),"with a reader awake, the night does not hurry");
            h.assertTrue(PellNight.callumOut(b),"Callum on the line");
            PellVisit.hurry(b,PellMorning.DAWN-30);
        }).thenWaitUntil(()->h.assertTrue(PellMorning.dawned(b),"first light"))
        .thenIdle(5).thenExecute(()->{
            for(int x:PellNight.CELLS)if(x!=9)h.assertTrue(PellNight.stage(l,b,x)==PellLine.SECOND,"dried at "+x);
            h.assertTrue(PellNight.stage(l,b,9)==PellLine.FOOTPRINT,"the smear dries as it was");
            h.assertTrue(PellNight.callumOut(b),"Callum, watched, stays out");
            h.assertTrue(p.isSleeping(),"the reader asleep sleeps on");
            PellVisit.hurry(b,PellMorning.UP-3);
        }).thenWaitUntil(()->h.assertTrue(PellVisit.step(b)>=PellMorning.RISING,"six: June gets up"))
        .thenWaitUntil(()->h.assertTrue(PellVisit.step(b)>=PellMorning.LAID,"June unlocks her way to the kitchen and lays breakfast"))
        .thenExecute(()->{
            for(var door:PellVisit.LOCKS)h.assertFalse(PellVisit.locked(b,door),"June has unlocked "+door);
            h.assertFalse(PellSite.open(l,b,PellSite.JUNE_DOOR),"her door shut behind her");
            h.assertTrue(PellNight.callumOut(b),"Callum, still watched, still out");
            PellVisit.hurry(b,PellMorning.SEVEN-3);told(p);
        }).thenWaitUntil(()->h.assertTrue(PellVisit.step(b)>=PellMorning.CALLED,"seven: breakfast"))
        .thenIdle(1).thenExecute(()->{
            h.assertFalse(p.isSleeping(),"June's call wakes the reader asleep");
            h.assertTrue(told(p).contains(PellTexts.BREAKFAST_FAR),"who hears it from the garage");
            h.assertTrue(PellNight.callumOut(b),"Callum stays out");
            told(p);hand(p,bed);
        }).thenIdle(1).thenExecute(()->{
            h.assertFalse(p.isSleeping(),"once breakfast is called the camp bed is only looked at");
            h.assertTrue(told(p).contains(PellTexts.thing(CAMP_BED,0)),"and said what it is");
        }).thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_CALLUM_IN),"June calls him in"))
        .thenExecute(()->{
            h.assertTrue(PellNight.callumOut(b),"still out while watched");
            // The reader looks down at the ground.
            face(q,0,90);
        }).thenWaitUntil(()->h.assertTrue(!PellNight.callumOut(b)&&PellVisit.placeOf(b,PellPeople.CALLUM)==1,"the moment nobody sees, Callum is in his chair"))
        .thenExecute(()->h.assertTrue(inChair(who(h,l,b,PellPeople.CALLUM),b,1),"at his place"))
        .thenWaitUntil(()->h.assertFalse(who(h,l,b,PellPeople.JUNE).saying(),"June between lines"))
        .thenExecute(()->{f.at(q,P,-7.5,0,-68.5);LiteraryVignettes.talk(q,who(h,l,b,PellPeople.JUNE));})
        .thenIdle(1).thenExecute(()->{
            h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_MORNING[0]),"a hand on June in the morning: "+said(who(h,l,b,PellPeople.JUNE)));
            // The reader awake goes back behind the shed, to the smear.
            f.at(q,P,10.5,0,-94.5);told(q);hand(q,b.offset(9,-1,PellSite.LINE_N));
        }).thenIdle(1).thenExecute(()->{
            var heard=told(q);
            String dry=PellTexts.line(l.getBlockState(b.offset(9,-1,PellSite.LINE_N)).getValue(PellLine.SURFACE),true,PellLine.FOOTPRINT,true);
            h.assertTrue(heard.contains(dry),"a hand on the smear finds it dried as it was: "+heard);
            f.at(p,P,.5,0,-30);f.at(q,P,-6.5,0,-30);PellPeople.vacate(b);
        }).thenWaitUntil(()->h.assertTrue(PellVisit.step(b)==PellVisit.GREET&&PellVisit.car(l,b)!=null,"the visit begins again"))
        .thenExecute(()->{
            for(int x:PellNight.CELLS)h.assertTrue(PellNight.stage(l,b,x)==PellLine.FIRST,"the line as the first visit has it at "+x);
            h.assertFalse(PellNight.callumOut(b)||PellMorning.dawned(b),"night and morning undone");
            h.assertTrue(at(who(h,l,b,PellPeople.CALLUM),b,PellPeople.FIRST_VISIT[PellPeople.CALLUM].x(),PellPeople.FIRST_VISIT[PellPeople.CALLUM].z(),.3),"Callum at the end of the hall");
            h.assertTrue(f.own(q,P).getBoolean("Smeared")&&f.own(p,P).getBoolean("SleptInGarage"),"what each reader did stays theirs");
        }).thenSucceed();
    }
    @AfterBatch(batch="pell_morning_watched") public static void watchedDone(ServerLevel l){done();}

    /** What a reader has kept, written as the house would (the record read in a case is a copy). */
    /**
     * 0.4.566 (owner, playing the house: "the player should also be sort of prompted on when to leave"). Gail rings and nobody takes
     * the receiver; June hangs up, and the visit is the reader's. Nothing has been said to them before that. A while on, June sends
     * them out (she is near them in the kitchen); a while after that the House tells them once, in grey, that the day has nothing
     * more in it and the next visit is through the door they came in by; and later once more, in passing. (A reader's part of a scene is
     * ticked every fifth server tick: the case gives each step a dozen.)
     */
    @GameTest(template="empty",batch="pell_leaving",timeoutTicks=9000)
    public static void aReaderWhoHasHadTheirVisitIsSentOut(GameTestHelper h){
        var f=house(h);var p=f.player("pell_leaving",P);var l=f.level(P);var b=f.base(P);
        String hint=PellTexts.leaveHint(1,false);
        h.startSequence().thenWaitUntil(()->{for(int i=0;i<PellPeople.COUNT;i++)h.assertTrue(PellPeople.find(l,b,i)!=null,"waiting for "+PellPeople.name(i));h.assertTrue(PellVisit.car(l,b)!=null,"waiting for the visit to begin");})
        .thenExecute(()->{PellCall.afterBreakfast(l,b);f.at(p,P,-9.5,0,-67.5);told(p);})
        .thenWaitUntil(()->h.assertTrue(PellCall.stage(b)==PellCall.DONE,"Gail rings, nobody takes it, and June hangs up"))
        .thenIdle(12).thenExecute(()->{
            var own=f.own(p,P);
            h.assertTrue(own.getInt("Beat")>=own.getInt("Visit")&&own.getInt("Visit")==1,"the visit is the reader's: "+own);
            h.assertTrue(own.getInt("DoneVisit0566")==1&&own.getInt("ToldToGo0566")==0,"seen to be had, and nothing said yet: "+own);
            h.assertFalse(told(p).contains(hint),"the House has not told them to go before the visit was theirs");
            // On to June's moment, without waiting it out.
            own.putLong("DoneAt0566",l.getGameTime()-PellHouse.SEND_OUT);LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),P,own);
        }).thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_SEND_OUT),"June sends them out: "+said(who(h,l,b,PellPeople.JUNE))))
        .thenExecute(()->{
            h.assertFalse(told(p).contains(hint),"and the House says nothing over her");
            var own=f.own(p,P);own.putLong("DoneAt0566",l.getGameTime()-PellHouse.HINT);LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),P,own);
        }).thenIdle(12).thenExecute(()->{
            h.assertTrue(told(p).contains(hint),"the House tells them, once");
            h.assertTrue(f.own(p,P).getInt("ToldToGo0566")==2,"and keeps that it has");
        }).thenIdle(40).thenExecute(()->{
            h.assertFalse(told(p).contains(hint),"not again straight away");
            var own=f.own(p,P);own.putLong("DoneAt0566",l.getGameTime()-PellHouse.AGAIN);LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),P,own);
        }).thenIdle(12).thenExecute(()->{
            h.assertTrue(told(p).contains(hint),"once more, later, in passing");
            h.assertTrue(f.own(p,P).getInt("ToldToGo0566")==3,"and that is the last of it");
            done();
        }).thenSucceed();
    }
    private static void keep(LiteraryTests.Fixture f,ServerPlayer p,String key){var own=f.own(p,P);own.putBoolean(key,true);LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),P,own);}
    private static boolean telling(ServerPlayer p,String speaker,String line){return Talk.lines(p).contains(speaker+": "+line);}

    /**
     * 0.4.532, Gail's call, with two readers: one in the kitchen, who saw June out in the night and Callum on the line, and one out in
     * the garage. The morning as breakfast leaves it. The telephone rings and June says it will be Gail; the reader in the kitchen lifts
     * it first, Gail asks for June, and June takes it from them. A hand on June while she is on it gets her finger. She has her say to
     * Gail (the pair of them slept like logs, Tilly, Callum) and holds the receiver out; the reader in the garage is told she is calling.
     * The reader in the kitchen takes it: Gail has just been talking to June, asks how the first night was, and they are offered what
     * they saw. The other reader, come in, finds it busy. The first tells Gail June was out in the garden; Gail has June's version and
     * asks for June back. Given back, June holds it out to the other ("Your turn"), who takes it from her hand and says nothing. Back on,
     * June answers what was said in front of her, says goodbye and hangs up, and tells the room to keep inside the line. Each reader
     * keeps what they said, and that they have had the first visit (0.4.533); left, the house begins again a week in, without Gail or
     * her car, with the receiver on its hook.
     */
    @GameTest(template="empty",batch="pell_call",timeoutTicks=9000)
    public static void gailRingsAndEachReaderHasAWordWithHer(GameTestHelper h){
        var f=house(h);var p=f.player("pell_call_first",P);var q=f.player("pell_call_second",P);var l=f.level(P);var b=f.base(P);
        f.at(p,P,.5,0,-44);f.at(q,P,-6.5,0,-36);
        var phone=b.offset(PellCall.PHONE);
        h.startSequence().thenWaitUntil(()->{for(int i=0;i<PellPeople.COUNT;i++)h.assertTrue(PellPeople.find(l,b,i)!=null,"waiting for "+PellPeople.name(i));h.assertTrue(PellVisit.car(l,b)!=null,"waiting for the visit to begin");})
        .thenExecute(()->{
            h.assertTrue(thing(l,b,-7,1,-69,PHONE,0)&&facing(l,b,-7,1,-69)==Direction.WEST,"the telephone on the kitchen wall, its receiver on the hook");
            keep(f,p,"SawJuneOut");keep(f,p,"SawCallumOnLine");
            PellCall.afterBreakfast(l,b);
            f.at(p,P,-9.5,0,-67.5);f.at(q,P,14.5,0,-89.5);
        }).thenWaitUntil(()->h.assertTrue(PellCall.stage(b)==PellCall.RINGING,"the telephone rings"))
        .thenExecute(()->{
            h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_THATLL_BE_GAIL),"\"That'll be Gail.\"");
            hand(p,phone);
            h.assertTrue(PellCall.off(l,b),"the reader in the kitchen lifts it first");
            h.assertTrue(telling(p,"Gail",PellTexts.GAIL_FOR_JUNE),"Gail asks for June: "+Talk.lines(p));
            h.assertTrue(f.own(p,P).getBoolean("AnsweredThePhone"),"and they keep that they answered it");
        }).thenWaitUntil(()->h.assertTrue(PellCall.stage(b)==PellCall.ON_PHONE,"June takes it from them"))
        .thenExecute(()->{
            var june=who(h,l,b,PellPeople.JUNE);
            h.assertTrue(june.getMainHandItem().is(PellRegistry.RECEIVER.get())&&june.gesture()==PellPeople.PHONE,"the receiver at June's ear");
            h.assertTrue(Talk.key(p).isEmpty(),"out of the reader's hands");
            told(p);LiteraryVignettes.talk(p,june);
        }).thenIdle(1).thenExecute(()->h.assertTrue(told(p).contains(PellTexts.JUNE_ON_PHONE),"a hand on June while she is on it: a finger"))
        .thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_SORRY),"\"Gail? Sorry, love\""))
        .thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_SLEPT),"the pair of them slept like logs"))
        .thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_CALLUM_GOOD),"and Callum good as gold"))
        .thenExecute(()->told(q))
        .thenWaitUntil(()->h.assertTrue(PellCall.stage(b)==PellCall.OFFERED,"June holds it out"))
        .thenExecute(()->{
            var june=who(h,l,b,PellPeople.JUNE);
            h.assertTrue(said(june).equals(PellTexts.JUNE_HOLDS_OUT)&&june.gesture()==PellPeople.OFFER&&june.getMainHandItem().is(PellRegistry.RECEIVER.get()),"\"She wants a word\", the receiver held out");
            h.assertTrue(told(q).contains(PellTexts.PHONE_FAR),"the reader in the garage is told");
            h.assertTrue(f.own(p,P).getBoolean("HeardJuneOnPhone")&&!f.own(q,P).getBoolean("HeardJuneOnPhone"),"only the reader in the kitchen heard June's side");
            hand(p,phone);
            h.assertTrue(p.getUUID().equals(PellCall.caller(b)),"the reader in the kitchen takes it");
            h.assertTrue(telling(p,"Gail",PellTexts.GAIL_ASKS),"Gail has just been talking to June: "+Talk.lines(p));
            var r=Talk.replies(p);
            h.assertTrue(r.contains(PellTexts.TELL_JUNE_OUT)&&r.contains(PellTexts.TELL_CALLUM)&&r.contains(PellTexts.TELL_FINE)&&r.contains(PellTexts.TELL_LEAVE)&&r.contains(PellTexts.TELL_NOTHING),"what they saw is theirs to tell: "+r);
            h.assertTrue(june.getMainHandItem().isEmpty(),"out of June's hand");
            f.at(q,P,-8.5,0,-67.6);told(q);hand(q,phone);
        }).thenIdle(1).thenExecute(()->{
            h.assertTrue(told(q).contains(PellTexts.PHONE_BUSY),"the other reader finds it busy");
            h.assertTrue(Talk.choose(p,PellTexts.TELL_JUNE_OUT),"the first tells Gail June was out in the garden");
            h.assertTrue(telling(p,"Gail","In the garden? In the dark?")&&telling(p,"Gail",PellTexts.GAIL_BYE),"Gail has June's version, and asks for June back: "+Talk.lines(p));
            h.assertTrue(PellTexts.JUNE_OUT.equals(f.own(p,P).getString("ToldGail"))&&f.own(p,P).getBoolean("SpokeToGail"),"they keep what they said");
            h.assertTrue(Talk.choose(p,PellTexts.HAND_BACK),"and give it back");
        }).thenIdle(1).thenExecute(()->{
            var june=who(h,l,b,PellPeople.JUNE);
            h.assertTrue(PellCall.caller(b)==null&&said(june).equals(PellTexts.JUNE_NEXT)&&june.gesture()==PellPeople.OFFER,"\"Your turn\", held out to the other");
            told(p);hand(p,phone);
        }).thenIdle(1).thenExecute(()->{
            h.assertTrue(told(p).contains(PellTexts.HAD_A_TURN),"one turn each");
            LiteraryVignettes.talk(q,who(h,l,b,PellPeople.JUNE));
            h.assertTrue(q.getUUID().equals(PellCall.caller(b)),"the other takes it from June's hand");
            var r=Talk.replies(q);
            h.assertFalse(r.contains(PellTexts.TELL_JUNE_OUT)||r.contains(PellTexts.TELL_CALLUM),"nothing offered that they did not see: "+r);
            h.assertTrue(Talk.choose(q,PellTexts.TELL_NOTHING),"and says nothing");
            h.assertTrue(telling(q,"Gail","Hello? Are you there, love?"),"Gail on a silent line: "+Talk.lines(q));
            h.assertTrue(Talk.choose(q,PellTexts.HAND_BACK),"given back");
        }).thenWaitUntil(()->h.assertTrue(PellCall.stage(b)==PellCall.BACK,"June back on"))
        .thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_ABOUT_LINE),"June answers what was said in front of her"))
        .thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_BYE),"and says goodbye"))
        .thenWaitUntil(()->h.assertFalse(PellCall.off(l,b),"the receiver back on its hook"))
        .thenExecute(()->{
            var june=who(h,l,b,PellPeople.JUNE);
            h.assertTrue(june.getMainHandItem().isEmpty(),"out of her hand");
            h.assertTrue(said(june).equals(PellTexts.JUNE_INSIDE_LINE),"\"Inside the line after dark, love. I did say.\"");
            for(var r:new ServerPlayer[]{p,q})h.assertTrue(f.own(r,P).getBoolean("GailRang")&&f.own(r,P).getBoolean("SpokeToGail"),"each reader keeps that Gail rang, and that they spoke to her");
            h.assertTrue(PellTexts.NOTHING.equals(f.own(q,P).getString("ToldGail")),"and what they said");
        }).thenWaitUntil(()->h.assertTrue(PellCall.stage(b)==PellCall.DONE,"hung up, the morning waits"))
        .thenExecute(()->{told(p);hand(p,phone);})
        .thenIdle(1).thenExecute(()->{
            h.assertTrue(told(p).contains(PellTexts.thing(PHONE,0)),"a hand on the telephone afterwards: what it is");
            f.at(p,P,.5,0,-30);f.at(q,P,-6.5,0,-30);PellPeople.vacate(b);
        }).thenWaitUntil(()->h.assertTrue(PellWeek.visit(b)==2&&PellVisit.step(b)==PellVisit.SETTLED,"both have had the first visit: the house begins again a week in"))
        .thenExecute(()->{
            h.assertTrue(PellVisit.car(l,b)==null&&PellPeople.find(l,b,PellPeople.GAIL)==null,"without Gail or her car");
            h.assertTrue(thing(l,b,-7,1,-69,PHONE,0)&&PellCall.caller(b)==null,"the receiver on its hook, nobody on the line");
            for(var r:new ServerPlayer[]{p,q}){var own=f.own(r,P);h.assertTrue(own.getBoolean("FirstVisit")&&!own.getBoolean("SecondVisit")&&own.getInt("Beat")>=1,"each reader has had the first visit, and only that: "+own);}
            h.assertTrue(PellTexts.JUNE_OUT.equals(f.own(p,P).getString("ToldGail")),"what each reader said stays theirs");
        }).thenSucceed();
    }
    @AfterBatch(batch="pell_call") public static void callDone(ServerLevel l){done();}

    /**
     * 0.4.532, a reader who walks off with the receiver. The reader spoke to June in the night, so June tells Gail one of them was up and
     * she saw them back to bed. The reader takes the receiver and goes out into the yard with it, saying nothing: June has it back, the
     * panel is gone, and what they never said stays unsaid ("walked off"). With nothing said to her, June tells Gail they are shy, says
     * goodbye and hangs up, with nothing to say to the room.
     */
    @GameTest(template="empty",batch="pell_call_walked",timeoutTicks=6000)
    public static void aReaderWhoWalksOffWithTheReceiverLeavesItUnsaid(GameTestHelper h){
        var f=house(h);var p=f.player("pell_call_walker",P);var l=f.level(P);var b=f.base(P);
        f.at(p,P,.5,0,-44);
        var phone=b.offset(PellCall.PHONE);
        h.startSequence().thenWaitUntil(()->{for(int i=0;i<PellPeople.COUNT;i++)h.assertTrue(PellPeople.find(l,b,i)!=null,"waiting for "+PellPeople.name(i));h.assertTrue(PellVisit.car(l,b)!=null,"waiting for the visit to begin");})
        .thenExecute(()->{keep(f,p,"SpokeToJuneAtNight");PellCall.afterBreakfast(l,b);f.at(p,P,-9.5,0,-67.5);})
        .thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_UP),"June tells Gail one of them was up in the night"))
        .thenWaitUntil(()->h.assertTrue(PellCall.stage(b)==PellCall.OFFERED,"June holds it out"))
        .thenExecute(()->{
            hand(p,phone);
            h.assertTrue(p.getUUID().equals(PellCall.caller(b))&&Talk.key(p).equals("pell_gail"),"the reader takes it");
            f.at(p,P,14.5,0,-89.5);
        }).thenWaitUntil(()->h.assertTrue(PellCall.stage(b)==PellCall.BACK,"gone off with it: June has it back"))
        .thenExecute(()->{
            h.assertTrue(Talk.key(p).isEmpty(),"the line gone from the reader");
            h.assertTrue(PellTexts.WALKED_OFF.equals(f.own(p,P).getString("ToldGail")),"what they never said stays unsaid");
            h.assertTrue(who(h,l,b,PellPeople.JUNE).getMainHandItem().is(PellRegistry.RECEIVER.get()),"the receiver at June's ear");
        }).thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_SHY),"\"Shy. It'll come.\""))
        .thenWaitUntil(()->h.assertFalse(PellCall.off(l,b),"hung up"))
        .thenIdle(5).thenExecute(()->h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_BYE),"nothing to say to the room"))
        .thenSucceed();
    }
    @AfterBatch(batch="pell_call_walked") public static void walkedDone(ServerLevel l){done();}

    /**
     * 0.4.532, nobody comes to the telephone. The one reader stays out in the garage; June holds the receiver out and calls, and they are
     * told. Nobody comes for it in a while, so June speaks for them: they are a bit shy yet, she'll tell them Gail rang. She hangs up and
     * goes back to the sink. The reader keeps that Gail rang, and not that they spoke to her.
     */
    @GameTest(template="empty",batch="pell_call_nobody",timeoutTicks=6000)
    public static void nobodyComesToThePhoneAndJuneSpeaksForThem(GameTestHelper h){
        var f=house(h);var p=f.player("pell_call_away",P);var l=f.level(P);var b=f.base(P);
        f.at(p,P,.5,0,-44);
        h.startSequence().thenWaitUntil(()->{for(int i=0;i<PellPeople.COUNT;i++)h.assertTrue(PellPeople.find(l,b,i)!=null,"waiting for "+PellPeople.name(i));h.assertTrue(PellVisit.car(l,b)!=null,"waiting for the visit to begin");})
        .thenExecute(()->{PellCall.afterBreakfast(l,b);f.at(p,P,14.5,0,-89.5);told(p);})
        .thenWaitUntil(()->h.assertTrue(PellCall.stage(b)==PellCall.OFFERED,"June holds it out"))
        .thenIdle(1).thenExecute(()->h.assertTrue(told(p).contains(PellTexts.PHONE_FAR),"the reader in the garage is told"))
        .thenWaitUntil(()->h.assertTrue(PellCall.stage(b)==PellCall.BACK,"nobody comes: June has it back"))
        .thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_NOBODY),"\"They're a bit shy yet\""))
        .thenWaitUntil(()->h.assertTrue(PellCall.stage(b)==PellCall.DONE,"hung up"))
        .thenWaitUntil(()->h.assertTrue(at(who(h,l,b,PellPeople.JUNE),b,PellVisit.SINK.x(),PellVisit.SINK.z(),.7),"June back at the sink"))
        .thenExecute(()->{
            h.assertTrue(f.own(p,P).getBoolean("GailRang"),"the reader keeps that Gail rang");
            h.assertFalse(f.own(p,P).getBoolean("SpokeToGail")||f.own(p,P).contains("ToldGail"),"and not that they spoke to her");
        }).thenSucceed();
    }
    @AfterBatch(batch="pell_call_nobody") public static void nobodyDone(ServerLevel l){done();}

    /** Waiting for the visit to begin a week in: June, Tilly and Callum there, and the house settled. */
    private static void weekBegun(GameTestHelper h,ServerLevel l,BlockPos b){
        var unloaded=SceneReady.unloaded(l,PellPeople.area(b));h.assertTrue(unloaded==null,"waiting for the lot: "+unloaded);
        for(int i:new int[]{PellPeople.JUNE,PellPeople.TILLY,PellPeople.CALLUM})h.assertTrue(PellPeople.find(l,b,i)!=null,"waiting for "+PellPeople.name(i));
        h.assertTrue(PellWeek.visit(b)==2&&PellVisit.step(b)==PellVisit.SETTLED,"waiting for the visit to begin, a week in");
    }
    /**
     * 0.4.533, a week in (docs/PELL_HOUSE_WEEK_0_4_533.md), with two readers who have both had the first visit. The house begins a week
     * in: no Gail and no car; June at her stove, Tilly in Nessa's room with its door open, Callum at the end of the hall; the front door,
     * the sliding door and the garage's door shut, and a towel pushed under the garage's. The pool is uncovered and full; a tile of its
     * band is missing and another odd, the drain is in the deep end's floor, and a lane rope crosses it at the step with one float gone.
     * The line has been gone over again; a knife is gone from the block and there is one in Callum's drawer; two knives' outlines are
     * empty on the pegboard; the shed's calendar has moved on and the fridge has two new photographs. The late afternoon holds while
     * nobody comes in. One reader comes in, and June calls hello from the kitchen; the other, at the far end of the hall, is told, and
     * the hour goes on. A hand on June and on Tilly gets their week's lines, and June's pool gate is a rule. At six, with a reader at the
     * table, June serves Tilly as Nessa and corrects herself, heard at the table and not in the garage. At bedtime she brushes Tilly's
     * hair the way she has every night, says goodnight to Nessa, and Tilly, still sitting up, says who she is; June says goodnight to
     * Tilly, and Tilly lies down. That is heard in the room and not in the garage.
     */
    @GameTest(template="empty",batch="pell_week",timeoutTicks=12000)
    public static void aWeekInTheHouseIsAsJuneHasMadeItAndSheSlipsOnTheName(GameTestHelper h){
        var f=house(h);var p=f.player("pell_week_reader",P);var q=f.player("pell_week_other",P);var l=f.level(P);var b=f.base(P);
        // Both have had the first visit: Gail's call at its end gives it.
        keep(f,p,"FirstVisit");keep(f,q,"FirstVisit");
        f.at(p,P,.5,0,-44);f.at(q,P,-6.5,0,-36);
        long[] mark={0};
        h.startSequence().thenWaitUntil(()->weekBegun(h,l,b))
        .thenExecute(()->{
            // 0.4.535: this afternoon June does not go to the laundry (PellHand has its own cases).
            PellHand.skip(b);
            h.assertTrue(PellVisit.car(l,b)==null&&PellPeople.find(l,b,PellPeople.GAIL)==null,"no Gail and no car a week in");
            h.assertTrue(at(who(h,l,b,PellPeople.JUNE),b,PellVisit.KITCHEN.x(),PellVisit.KITCHEN.z(),.3),"June at her stove");
            h.assertTrue(at(who(h,l,b,PellPeople.TILLY),b,PellVisit.TILLY_ROOM.x(),PellVisit.TILLY_ROOM.z(),.3),"Tilly in Nessa's room");
            // 0.4.568: her hair grown past her shoulders.
            h.assertTrue(PellPeople.hair(who(h,l,b,PellPeople.TILLY).marks())==1,"a week in, Tilly's hair has grown past her shoulders: "+PellPeople.hair(who(h,l,b,PellPeople.TILLY).marks()));
            var s=PellPeople.FIRST_VISIT[PellPeople.CALLUM];h.assertTrue(at(who(h,l,b,PellPeople.CALLUM),b,s.x(),s.z(),.3),"Callum at the end of the hall");
            h.assertTrue(PellSite.open(l,b,PellSite.NESSA_DOOR),"Nessa's door open");
            for(var door:List.of(PellSite.FRONT_DOOR,PellSite.SLIDER,PellSite.GARAGE_DOOR))h.assertFalse(PellSite.open(l,b,door),"shut: "+door);
            var t=PellWeek.TOWEL;
            h.assertTrue(thing(l,b,t.getX(),t.getY(),t.getZ(),TOWEL,0)&&facing(l,b,t.getX(),t.getY(),t.getZ())==Direction.NORTH,"a towel pushed under the garage's door");
            // The pool, uncovered: water to the brim, the band's two tiles, the drain, the lane rope.
            for(int x=PellSite.WATER_W;x<=PellSite.WATER_E;x++)for(int z=PellSite.WATER_N;z<=PellSite.WATER_S;z++)h.assertTrue(l.getBlockState(b.offset(x,-1,z)).is(Blocks.WATER),"water to the brim at "+x+","+z);
            for(var d:List.of(PellSite.BAND_MISSING,PellSite.BAND_ODD,PellSite.DRAIN))h.assertTrue(PellWeek.detail(l.getBlockState(b.offset(d)))!=null,"the pool's detail at "+d);
            var rope=PellWeek.rope(l,b);int gaps=0;
            h.assertTrue(rope.size()==PellSite.WATER_E-PellSite.WATER_W+1,"the lane rope, a length to each block across: "+rope.size());
            for(var d:rope){
                h.assertTrue(Math.abs(d.getY()-(b.getY()+PellWeek.WATERLINE))<.01&&Math.abs(d.getZ()-(b.getZ()+PellWeek.ROPE_Z))<.01,"at the waterline, over the step: "+d.position());
                if(net.minecraft.world.item.ItemStack.parseOptional(l.registryAccess(),d.saveWithoutId(new net.minecraft.nbt.CompoundTag()).getCompound("item")).is(PellRegistry.LANE_GAP.get())){gaps++;h.assertTrue(Math.abs(d.getX()-(b.getX()+PellWeek.GAP_X+.5))<.01,"the float gone from one length: "+d.position());}
            }
            h.assertTrue(gaps==1,"one float gone: "+gaps);
            // The line gone over again, all the way round.
            for(var at:PellSite.route()){var st=l.getBlockState(b.offset(at.getX(),-1,at.getZ()));h.assertTrue(PellLine.unbroken(st)&&st.getValue(PellLine.STAGE)==PellLine.SECOND,"the line gone over at "+at+": "+st);}
            // The things a week changes.
            h.assertTrue(thing(l,b,-11,0,-71,KNIFE_BLOCK,1)&&thing(l,b,-6,0,-61,CUTLERY,1),"a knife gone from the block, and one in Callum's drawer");
            h.assertTrue(thing(l,b,14,1,-95,PEGBOARD,2)&&thing(l,b,15,1,-95,PEGBOARD,3),"two knives' outlines empty on the pegboard");
            h.assertTrue(thing(l,b,5,1,-92,SHED_CALENDAR,1)&&thing(l,b,-13,1,-71,FRIDGE_TOP,1),"the calendar moved on, two new photographs on the fridge");
            h.assertTrue(thing(l,b,PellSwim.CHAIR.getX(),0,PellSwim.CHAIR.getZ(),POOL_CHAIR,0)&&PellSwim.gateStage(l,b)==1,"nothing on the pool chair but its towel, and the gate padlocked");
            mark[0]=PellVisit.hour(b);
        }).thenIdle(20).thenExecute(()->{
            h.assertTrue(PellVisit.hour(b)==mark[0]&&!PellWeek.welcomed(b),"while nobody comes in, the late afternoon holds: "+PellVisit.hour(b));
            // One reader in at the front; the other to the far end of the hall, out of earshot of the kitchen.
            told(q);f.at(q,P,8.5,0,-65.5);f.at(p,P,1.5,0,-63.5);
        }).thenWaitUntil(()->h.assertTrue(PellWeek.welcomed(b),"June calls hello"))
        .thenIdle(1).thenExecute(()->{
            h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_WELCOME),"\"That you, love?\": "+said(who(h,l,b,PellPeople.JUNE)));
            h.assertTrue(told(q).contains(PellTexts.WELCOME_FAR),"heard from the end of the hall");
            mark[0]=PellVisit.hour(b);
        }).thenIdle(20).thenExecute(()->h.assertTrue(PellVisit.hour(b)>=mark[0]+15,"the hour goes on: "+mark[0]+" to "+PellVisit.hour(b)))
        .thenWaitUntil(()->h.assertFalse(who(h,l,b,PellPeople.JUNE).saying(),"June's hello has had its time"))
        .thenExecute(()->{f.at(p,P,-7.5,0,-67.5);told(p);hand(p,b.offset(-11,0,-71));})
        .thenIdle(1).thenExecute(()->{
            h.assertTrue(told(p).contains(PellTexts.thing(KNIFE_BLOCK,1)),"in the kitchen, a hand on the knife block: one slot empty");
            var june=who(h,l,b,PellPeople.JUNE);var tilly=who(h,l,b,PellPeople.TILLY);
            LiteraryVignettes.talk(p,june);
            h.assertTrue(said(june).equals(PellTexts.JUNE_WEEK_TALK[0]),"a hand on June a week in: "+said(june));
            june.hush();LiteraryVignettes.talk(p,june);june.hush();LiteraryVignettes.talk(p,june);
            h.assertTrue(said(june).equals(PellTexts.JUNE_WEEK_TALK[2])&&(rules(f,p)&PellTexts.RULE_POOL)!=0,"the pool gate, still a rule: "+rules(f,p));
            f.at(q,P,-9.5,0,-62.5);LiteraryVignettes.talk(q,tilly);
            h.assertTrue(said(tilly).equals(PellTexts.TILLY_WEEK_TALK[0]),"a hand on Tilly a week in: "+said(tilly));
            june.hush();tilly.hush();
            // To six: one reader in the living room, the other out in the garage.
            f.at(p,P,13.5,0,-92.5);f.at(q,P,-2.5,0,-68.5);PellVisit.hurry(b,PellVisit.SIX-5);
        }).thenWaitUntil(()->h.assertTrue(PellVisit.step(b)>=PellVisit.LAID,"June calls them at six and lays the table"))
        .thenWaitUntil(()->h.assertTrue(PellVisit.placeOf(b,PellPeople.TILLY)==0&&PellVisit.placeOf(b,PellPeople.CALLUM)==1,"Tilly and Callum sit down"))
        .thenExecute(()->{f.at(q,P,-9.5,0,-67.5);h.assertTrue(io.github.knaitoe.theoldesthouse.house.HouseSitting.sit(q,b.offset(PellVisit.TABLE[3].chair())),"a reader sits at the table");})
        .thenWaitUntil(()->h.assertTrue(PellVisit.plate(b,0)==PellVisit.SERVED,"June serves Tilly first"))
        .thenExecute(()->{
            h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_SERVES_NESSA),"by the other name, and then hers: "+said(who(h,l,b,PellPeople.JUNE)));
            h.assertTrue(f.own(q,P).getBoolean("HeardNessaAtDinner")&&!f.own(p,P).getBoolean("HeardNessaAtDinner"),"heard at the table, not in the garage");
        }).thenWaitUntil(()->h.assertTrue(PellVisit.plate(b,3)==PellVisit.SERVED,"the reader at the table served"))
        .thenExecute(()->hand(q,b.offset(-12,0,-69)))
        .thenWaitUntil(()->h.assertTrue(PellVisit.plate(b,2)==PellVisit.SERVED,"a place served for the other"))
        .thenExecute(()->{
            // The other comes in from the garage, sits at the place laid for them and eats.
            f.at(p,P,-9.5,0,-69.5);h.assertTrue(io.github.knaitoe.theoldesthouse.house.HouseSitting.sit(p,b.offset(PellVisit.TABLE[2].chair())),"the other sits");
            hand(p,b.offset(-12,0,-69));
            h.assertTrue(PellVisit.plate(b,2)==PellVisit.SCRAPED&&PellVisit.plate(b,3)==PellVisit.SCRAPED,"each eats their dinner");
        }).thenWaitUntil(()->h.assertTrue(PellVisit.step(b)>=PellVisit.CLEARED,"June clears"))
        .thenExecute(()->{
            // To bedtime: one reader in Nessa's room, clear of the way through it; the other out in the garage.
            p.stopRiding();q.stopRiding();f.at(q,P,-8.5,0,-61.5);f.at(p,P,13.5,0,-92.5);PellVisit.hurry(b,PellVisit.EVENING-3);
        }).thenWaitUntil(()->h.assertTrue(PellVisit.step(b)>=PellVisit.BRUSHING,"June brushes Tilly's hair"))
        .thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.TILLY)).equals(PellTexts.TILLY_SAME_WAY),"Tilly: the same way every night"))
        .thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_HOW_SHE_LIKED),"June: how she liked it"))
        .thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_COMING_ON),"June: it's coming on"))
        .thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_NIGHT_NESSA),"goodnight to Nessa"))
        .thenExecute(()->{
            h.assertTrue(f.own(q,P).getBoolean("HeardNessaAtBedtime")&&!f.own(p,P).getBoolean("HeardNessaAtBedtime"),"heard in the room, not in the garage");
            h.assertTrue(PellVisit.perched(b,PellPeople.TILLY)==PellVisit.TILLY_BED&&!PellVisit.asleep(b,PellPeople.TILLY),"Tilly still sitting up");
            h.assertTrue(who(h,l,b,PellPeople.JUNE).getMainHandItem().isEmpty(),"the brush put away");
        }).thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.TILLY)).equals(PellTexts.TILLY_ITS_TILLY),"Tilly: it's Tilly"))
        .thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_NIGHT_TILLY),"June: Tilly, I know"))
        .thenExecute(()->{
            h.assertTrue(PellVisit.step(b)==PellVisit.TUCKED&&PellVisit.asleep(b,PellPeople.TILLY),"and Tilly lies down");
            h.assertTrue(f.own(q,P).getBoolean("SawHairBrushed"),"the reader in the room saw her hair brushed");
        }).thenSucceed();
    }
    @AfterBatch(batch="pell_week") public static void weekDone(ServerLevel l){done();}

    /**
     * 0.4.533, June's lengths at first light, a week in, with two readers awake: one on the patio by the pool's fence, one in the garage.
     * The small hours as June's rounds leave them. At first light June comes out of her room in her robe, lets herself out by the sliding
     * door and locks it, takes the padlock off the pool's gate, goes in and shuts it, leaves the robe on the chair and gets in at the
     * shallow end. The hour holds at half past five. She swims her lengths down the middle, under the lane rope; the reader on the patio
     * sees her through the bars, the one in the garage does not, and a hand on her finds her swimming on. With the padlock off a hand
     * opens the gate; the reader goes in, finds the goggles under the drain's grate and the odd tile in the band, and gets out onto the
     * deck. Out of the water, her robe on, June waits outside the gate and asks the reader out; once they are, she padlocks it, comes in
     * by the sliding door and locks it behind her, and goes to her room. At six she gets up dressed for the day, and at breakfast she
     * asks Nessa where she put her goggles; Tilly says she has none, and June is all over the place this morning. Both readers heard it.
     */
    @GameTest(template="empty",batch="pell_swim",timeoutTicks=12000)
    public static void aWeekInJuneSwimsHerLengthsAtFirstLight(GameTestHelper h){
        var f=house(h);var p=f.player("pell_swim_patio",P);var q=f.player("pell_swim_garage",P);var l=f.level(P);var b=f.base(P);
        keep(f,p,"FirstVisit");keep(f,q,"FirstVisit");
        f.at(p,P,.5,0,-44);f.at(q,P,-6.5,0,-36);
        var chair=PellSwim.CHAIR;
        h.startSequence().thenWaitUntil(()->weekBegun(h,l,b))
        .thenExecute(()->{
            // 0.4.534: a night on which nobody came to the kitchen at one.
            PellMorning.smallHours(l,b);PellKnife.skip(b);
            // One reader on the patio by the pool's gate, clear of June's way to it; the other in the garage.
            f.at(p,P,-6.5,0,-74.5);face(p,180,15);f.at(q,P,14.5,0,-89.5);
            PellVisit.hurry(b,PellMorning.DAWN-10);
        }).thenWaitUntil(()->h.assertTrue(PellSwim.inWater(b),"June goes down to the pool and gets in"))
        .thenExecute(()->{
            var june=who(h,l,b,PellPeople.JUNE);
            h.assertTrue(june.gesture()==PellPeople.SWIM&&june.isNoGravity()&&"pell_june_swim".equals(PellPeople.dressed(june.phase(),june.gesture())),"in her swimming things");
            h.assertTrue(thing(l,b,chair.getX(),0,chair.getZ(),POOL_CHAIR,1),"her robe left over the chair");
            h.assertTrue(PellSwim.gateStage(l,b)==0,"the gate shut behind her, its padlock off");
            h.assertTrue(PellVisit.locked(b,PellSite.SLIDER)&&!PellSite.open(l,b,PellSite.SLIDER),"the sliding door locked behind her");
        }).thenWaitUntil(()->h.assertTrue(PellVisit.hour(b)==PellSwim.HALF_FIVE,"half past five: "+PellVisit.hour(b)))
        .thenExecute(()->lookAt(p,who(h,l,b,PellPeople.JUNE).position()))
        .thenIdle(20).thenExecute(()->{
            h.assertTrue(PellVisit.hour(b)==PellSwim.HALF_FIVE&&PellSwim.inWater(b),"the hour holds while she swims");
            h.assertTrue(f.own(p,P).getBoolean("SawJuneSwim")&&!f.own(q,P).getBoolean("SawJuneSwim"),"seen through the bars from the patio, not from the garage");
            told(p);hand(p,b.offset(PellSite.POOL_GATE));
            h.assertTrue(PellSwim.gateStage(l,b)==2,"with the padlock off, a hand opens the gate");
        }).thenIdle(1).thenExecute(()->{
            h.assertTrue(told(p).contains(PellTexts.POOL_GATE_OPEN),"the padlock hanging open");
            // In, and down to the drain at the deep end.
            f.at(p,P,-8.5,-4,-89.5);hand(p,b.offset(PellSite.DRAIN));hand(p,b.offset(PellSite.BAND_ODD));
        }).thenIdle(1).thenExecute(()->{
            var heard=told(p);
            h.assertTrue(heard.contains(PellTexts.DRAIN)&&f.own(p,P).getBoolean("SawGoggles"),"the goggles under the drain's grate: "+heard);
            h.assertTrue(heard.contains(PellTexts.BAND_ODD),"and a tile of the band that does not match");
        }).thenWaitUntil(()->h.assertTrue(who(h,l,b,PellPeople.JUNE).distanceToSqr(p)<16,"June swims past the reader in the water"))
        .thenExecute(()->{told(p);LiteraryVignettes.talk(p,who(h,l,b,PellPeople.JUNE));})
        .thenIdle(1).thenExecute(()->{
            h.assertTrue(told(p).contains(PellTexts.JUNE_SWIMMING),"a hand on her: she swims on");
            // Out onto the deck, inside the pool's fence.
            f.at(p,P,-15.5,0,-82.5);
        }).thenWaitUntil(()->h.assertTrue(PellSwim.lengths(b)>=PellSwim.LENGTHS,"her lengths"))
        .thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_OUT_OF_POOL),"out, her robe on, she asks the reader out of the pool's yard"))
        .thenExecute(()->{
            var june=who(h,l,b,PellPeople.JUNE);
            h.assertTrue(june.gesture()==PellPeople.ROBE&&!june.isNoGravity()&&thing(l,b,chair.getX(),0,chair.getZ(),POOL_CHAIR,0),"her robe on, off the chair");
            h.assertTrue(PellSwim.gateStage(l,b)!=1,"not padlocked while the reader is inside");
            f.at(p,P,-8.5,0,-74.5);
        }).thenWaitUntil(()->h.assertTrue(PellSwim.gateStage(l,b)==1,"once the reader is out, she padlocks it"))
        .thenWaitUntil(()->h.assertTrue(PellSwim.swum(b),"in by the sliding door and back to her room"))
        .thenExecute(()->{
            h.assertTrue(PellVisit.locked(b,PellSite.SLIDER)&&!PellSite.open(l,b,PellSite.SLIDER),"the sliding door locked behind her");
            h.assertTrue(at(who(h,l,b,PellPeople.JUNE),b,PellVisit.HER_ROOM.x(),PellVisit.HER_ROOM.z(),.7)&&!PellSite.open(l,b,PellSite.JUNE_DOOR),"in her room, its door shut");
        }).thenWaitUntil(()->h.assertTrue(PellVisit.step(b)>=PellMorning.RISING,"six: she gets up"))
        .thenExecute(()->h.assertTrue(who(h,l,b,PellPeople.JUNE).gesture()==0,"dressed for the day"))
        .thenWaitUntil(()->h.assertTrue(PellVisit.step(b)>=PellMorning.LAID,"breakfast laid"))
        .thenExecute(()->PellVisit.hurry(b,PellMorning.SEVEN-3))
        .thenWaitUntil(()->h.assertTrue(PellVisit.step(b)>=PellMorning.CALLED&&PellVisit.placeOf(b,PellPeople.TILLY)==0,"breakfast, and Tilly at the table"))
        .thenExecute(()->{
            f.at(q,P,-9.5,0,-67.5);h.assertTrue(io.github.knaitoe.theoldesthouse.house.HouseSitting.sit(q,b.offset(PellVisit.TABLE[3].chair())),"a reader sits");
            f.at(p,P,-9.5,0,-69.5);h.assertTrue(io.github.knaitoe.theoldesthouse.house.HouseSitting.sit(p,b.offset(PellVisit.TABLE[2].chair())),"the other sits");
        }).thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_GOGGLES),"June: where did you put your goggles"))
        .thenExecute(()->h.assertTrue(f.own(p,P).getBoolean("HeardGoggles")&&f.own(q,P).getBoolean("HeardGoggles"),"both readers heard"))
        .thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.TILLY)).equals(PellTexts.TILLY_NO_GOGGLES),"Tilly has none"))
        .thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_ALL_OVER),"June: all over the place"))
        .thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.TILLY)).equals(PellTexts.TILLY_ALL_RIGHT),"Tilly: that's all right"))
        .thenSucceed();
    }
    @AfterBatch(batch="pell_swim") public static void swimDone(ServerLevel l){done();}

    /**
     * 0.4.533, the week's call and the visits after it. Two readers who have had the first visit; one spoke to June in the night. The
     * morning as breakfast leaves it; June answers and tells Gail they are settling in, one still up at night and seen back to bed, and
     * Tilly a different girl. A reader takes the receiver: Gail says it is a week in now and asks how they are finding it; they say they
     * are not sleeping much, and Gail has June's version. Her goodbye is the week's. Each keeps that they have had the second visit too,
     * and what the first call's record said stays as it was. Left, the house plays the second visit again (there is no third yet); and
     * once a reader who has had neither is there, it begins at the first, Gail and her car at the curb and the pool under its cover.
     */
    @GameTest(template="empty",batch="pell_week_call",timeoutTicks=9000)
    public static void theWeeksCallAndTheVisitsAfterIt(GameTestHelper h){
        var f=house(h);var p=f.player("pell_week_caller",P);var q=f.player("pell_week_away",P);var l=f.level(P);var b=f.base(P);
        keep(f,p,"FirstVisit");keep(f,q,"FirstVisit");keep(f,p,"SpokeToJuneAtNight");
        {var own=f.own(p,P);own.putString("ToldGail",PellTexts.FINE);LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),P,own);}
        f.at(p,P,.5,0,-44);f.at(q,P,-6.5,0,-36);
        var phone=b.offset(PellCall.PHONE);
        h.startSequence().thenWaitUntil(()->weekBegun(h,l,b))
        .thenExecute(()->{PellCall.afterBreakfast(l,b);f.at(p,P,-9.5,0,-67.5);f.at(q,P,14.5,0,-89.5);})
        .thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_WEEK_UP),"June: settling in, one still up at night"))
        .thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_WEEK_TILLY),"and Tilly a different girl"))
        .thenWaitUntil(()->h.assertTrue(PellCall.stage(b)==PellCall.OFFERED,"June holds it out"))
        .thenExecute(()->{
            hand(p,phone);
            h.assertTrue(p.getUUID().equals(PellCall.caller(b)),"the reader takes it");
            h.assertTrue(telling(p,"Gail",PellTexts.GAIL_WEEK_ASKS),"a week in now: "+Talk.lines(p));
            var r=Talk.replies(p);
            h.assertTrue(r.contains(PellTexts.TELL_WEEK_FINE)&&r.contains(PellTexts.TELL_WEEK_NO_SLEEP)&&!r.contains(PellTexts.TELL_FINE),"the answers as they are a week in: "+r);
            h.assertTrue(Talk.choose(p,PellTexts.TELL_WEEK_NO_SLEEP),"not sleeping much");
            h.assertTrue(telling(p,"Gail","June said somebody's still up at night.")&&telling(p,"Gail",PellTexts.GAIL_WEEK_BYE),"Gail has June's version, and the week's goodbye: "+Talk.lines(p));
            var own=f.own(p,P);
            h.assertTrue(PellTexts.NO_SLEEP.equals(own.getString("ToldGailWeek"))&&PellTexts.FINE.equals(own.getString("ToldGail"))&&own.getBoolean("SpokeToGailWeek"),"the week's answer kept apart from the first's: "+own);
            h.assertTrue(Talk.choose(p,PellTexts.HAND_BACK),"given back");
        }).thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_WEEK_BYE),"June's goodbye: next week, then"))
        .thenWaitUntil(()->h.assertTrue(PellCall.stage(b)==PellCall.DONE,"hung up"))
        .thenExecute(()->{
            for(var r:new ServerPlayer[]{p,q}){var own=f.own(r,P);h.assertTrue(own.getBoolean("FirstVisit")&&own.getBoolean("SecondVisit"),"each has had the second visit too: "+own);}
            f.at(p,P,.5,0,-30);f.at(q,P,-6.5,0,-30);PellPeople.vacate(b);
        }).thenWaitUntil(()->h.assertTrue(PellWeek.visit(b)==PellWeek.LAST&&PellVisit.step(b)==PellVisit.TELLY,"with both visits had, the house plays the third, the last night, from the evening"))
        .thenExecute(()->{
            h.assertTrue(PellVisit.car(l,b)==null,"no car");
            h.assertTrue(PellLastNight.lit(l,b),"the pool lit for the last night");
            // A reader who has had neither, and the house left again.
            var r=f.player("pell_week_new",P);f.at(r,P,-3.5,0,-36);PellPeople.vacate(b);
        }).thenWaitUntil(()->h.assertTrue(PellWeek.visit(b)==1&&PellVisit.step(b)==PellVisit.GREET&&PellVisit.car(l,b)!=null&&PellPeople.find(l,b,PellPeople.GAIL)!=null,"a reader who has had neither: the first visit, Gail and her car at the curb"))
        .thenExecute(()->{
            h.assertTrue(PellWeek.rope(l,b).isEmpty()&&!l.getBlockState(b.offset(-7,-1,-86)).is(Blocks.WATER)&&!PellLastNight.lit(l,b),"the pool under its cover, no rope, no light");
            for(var at:PellSite.route()){var st=l.getBlockState(b.offset(at.getX(),-1,at.getZ()));h.assertTrue(st.getValue(PellLine.STAGE)==PellLine.FIRST,"the line as the first visit has it at "+at);}
            h.assertTrue(thing(l,b,-11,0,-71,KNIFE_BLOCK,0)&&thing(l,b,-6,0,-61,CUTLERY,0)&&thing(l,b,14,1,-95,PEGBOARD,0)&&thing(l,b,-11,2,-71,CUPBOARD_UPPER,0),"every knife where it was, the cupboard shut");
            var c=who(h,l,b,PellPeople.CALLUM);h.assertTrue(c.gesture()!=PellPeople.EATING&&c.gesture()!=PellPeople.SITTING&&c.getOffhandItem().isEmpty(),"Callum not sitting out on the grass");
            h.assertTrue(l.getBlockState(b.offset(PellWeek.TOWEL)).isAir(),"no towel under the garage's door");
        }).thenSucceed();
    }
    @AfterBatch(batch="pell_week_call") public static void weekCallDone(ServerLevel l){done();}

    /** Whether the cut to black came to this reader since their packets were last read (reading them empties them). */
    private static boolean cutToBlack(ServerPlayer p){
        boolean cut=false;
        for(var packet:NativeTestPlayers.packets(p))if(packet instanceof net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket custom&&custom.payload() instanceof io.github.knaitoe.theoldesthouse.network.PellCutPayload)cut=true;
        return cut;
    }
    /** The way one of them would face to look at a reader. */
    private static float yawTo(LiteraryActor a,ServerPlayer p){var d=p.position().subtract(a.position());return (float)(Math.atan2(-d.x,d.z)*180/Math.PI);}

    /**
     * 0.4.534, the knife (docs/PELL_HOUSE_KNIFE_0_4_534.md), a week in, with two readers: one asleep on the camp bed, one awake out by
     * the drive. Just before one. While the reader by the drive looks at Callum on the line, nothing happens; the first moment they look
     * down, he is at the kitchen sink with the knife from his drawer, the light over the sink on and the sliding door open and
     * unlocked, and the reader asleep wakes and is told a light is on in the kitchen. The hour holds. The woken reader comes into the
     * kitchen and sees him with the knife at the sink; the other goes round to the patio and watches through the kitchen's window. He
     * looks round at the reader in the kitchen. A hand on him takes his wrist; another, and he pulls against them. Then his fist comes,
     * and it goes black for the reader in the kitchen and for the one at the window. Only the one in the kitchen is who June found
     * there: they keep that they were in the black and go on Gail's file; the one at the window keeps that they saw it go black. In the
     * black Callum is back on his mattress with nothing in his hand, a tea towel lies on the kitchen floor, and June is in the kitchen's
     * arch with her door open behind her. She sends the reader back to bed; a hand on her, and it is bed, love. The light goes off; at
     * the sliding door she asks the reader out of the house, locks it behind them and goes back to her room; and the night goes on with
     * Callum asleep and the knife gone from his drawer.
     */
    @GameTest(template="empty",batch="pell_knife",timeoutTicks=9000)
    public static void atOneCallumIsAtTheSinkWithTheKnifeAndJuneFindsTheReaderWhoTookHold(GameTestHelper h){
        var f=house(h);var p=f.player("pell_knife_kitchen",P);var q=f.player("pell_knife_window",P);var l=f.level(P);var b=f.base(P);
        keep(f,p,"FirstVisit");keep(f,q,"FirstVisit");
        f.at(p,P,.5,0,-44);f.at(q,P,-6.5,0,-36);
        var bed=b.offset(CAMP_BED_AT);long[] mark={0};
        h.startSequence().thenWaitUntil(()->weekBegun(h,l,b))
        .thenExecute(()->{
            PellKnife.beforeOne(l,b);
            // One reader asleep on the camp bed; the other out by the drive, looking at Callum on the line.
            f.at(p,P,14.5,0,-89.5);hand(p,bed);
            h.assertTrue(p.isSleeping(),"one reader asleep in the garage");
            f.at(q,P,14.5,0,-82.5);lookAt(q,who(h,l,b,PellPeople.CALLUM).getEyePosition());
        }).thenIdle(20).thenExecute(()->{
            h.assertTrue(PellVisit.hour(b)>=PellKnife.ONE,"past one: "+PellVisit.hour(b));
            h.assertTrue(PellKnife.stage(b)==PellKnife.NOT_YET&&PellNight.callumOut(b),"while he is watched, Callum stays on the line");
            told(p);face(q,0,90);
        }).thenWaitUntil(()->h.assertTrue(PellKnife.stage(b)==PellKnife.AT_SINK,"the first moment nobody sees, he is at the sink"))
        .thenIdle(1).thenExecute(()->{
            var c=who(h,l,b,PellPeople.CALLUM);
            h.assertTrue(at(c,b,PellKnife.SINK.x(),PellKnife.SINK.z(),.3)&&c.getMainHandItem().is(PellRegistry.KNIFE.get()),"at the sink with a knife in his hand: "+c.position());
            h.assertTrue(thing(l,b,-6,0,-61,CUTLERY,2),"the space in his drawer where it lay");
            h.assertTrue(PellKnife.lit(l,b),"the light over the sink on");
            h.assertTrue(PellSite.open(l,b,PellSite.SLIDER)&&!PellVisit.locked(b,PellSite.SLIDER),"the sliding door standing open");
            h.assertFalse(p.isSleeping(),"the reader asleep wakes");
            h.assertTrue(told(p).contains(PellTexts.KNIFE_WOKEN)&&f.own(p,P).getBoolean("WokenByTheKitchen"),"told a light is on in the kitchen, and keeps that it woke them");
            h.assertFalse(f.own(q,P).getBoolean("WokenByTheKitchen"),"the reader awake was not woken");
            mark[0]=PellVisit.hour(b);
        }).thenIdle(20).thenExecute(()->{
            h.assertTrue(PellVisit.hour(b)==mark[0],"the hour holds while he is there: "+mark[0]+" to "+PellVisit.hour(b));
            // The woken reader into the kitchen, clear of him; the other round to the patio, looking in at the window.
            f.at(p,P,-7.5,0,-67.5);f.at(q,P,-9.5,0,-74.5);lookAt(q,who(h,l,b,PellPeople.CALLUM).getEyePosition());
        }).thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("SawKnifeAtSink"),"in the kitchen, the reader sees him"))
        .thenIdle(1).thenExecute(()->h.assertTrue(told(p).contains(PellTexts.KNIFE_SEEN),"at the sink with his back to the room, a knife in his hand"))
        .thenWaitUntil(()->h.assertTrue(f.own(q,P).getBoolean("SawKnife"),"seen through the window from the patio"))
        .thenExecute(()->{
            var c=who(h,l,b,PellPeople.CALLUM);
            h.assertTrue(Math.abs(net.minecraft.util.Mth.wrapDegrees(c.getYRot()-yawTo(c,p)))<5,"he looks round at the reader in the kitchen: "+c.getYRot());
            h.assertFalse(f.own(q,P).getBoolean("SawKnifeAtSink"),"the one at the window is not in the kitchen");
            told(p);LiteraryVignettes.talk(p,c);
        }).thenIdle(1).thenExecute(()->{
            var c=who(h,l,b,PellPeople.CALLUM);
            h.assertTrue(told(p).contains(PellTexts.KNIFE_HOLD),"a hand on him takes his wrist");
            h.assertTrue(PellKnife.stage(b)==PellKnife.STRUGGLE&&c.gesture()==PellPeople.STRUGGLE&&f.own(p,P).getBoolean("FoughtCallum"),"they struggle for it");
            // 0.4.567: whose hold he is in, for both of their hands to be drawn.
            h.assertTrue(c.holder().filter(p.getUUID()::equals).isPresent(),"the reader has hold of him: "+c.holder());
            LiteraryVignettes.talk(p,c);
        }).thenIdle(1).thenExecute(()->{
            h.assertTrue(told(p).contains(PellTexts.KNIFE_WRENCH),"he pulls against them");
            told(q);
        }).thenWaitUntil(()->h.assertTrue(PellKnife.stage(b)==PellKnife.LUNGE&&who(h,l,b,PellPeople.CALLUM).gesture()==PellPeople.LUNGE,"his fist comes"))
        .thenExecute(()->h.assertTrue(who(h,l,b,PellPeople.CALLUM).holder().isEmpty(),"and throws off their hold"))
        .thenWaitUntil(()->h.assertTrue(PellKnife.stage(b)==PellKnife.BLACK,"the cut"))
        .thenExecute(()->{
            h.assertTrue(cutToBlack(p)&&cutToBlack(q),"black for the reader in the kitchen and for the one looking in");
            h.assertTrue(PellKnife.found(b).equals(Set.of(p.getUUID())),"June found the one in the kitchen: "+PellKnife.found(b));
            var mine=f.own(p,P);var theirs=f.own(q,P);
            h.assertTrue(mine.getBoolean("InTheBlack")&&mine.getBoolean("MarkKnife")&&mine.getInt("GailMarks")==1,"in the black, and on Gail's file: "+mine);
            h.assertTrue(theirs.getBoolean("SawTheBlack")&&!theirs.getBoolean("InTheBlack")&&!theirs.getBoolean("MarkKnife")&&theirs.getInt("GailMarks")==0,"the one at the window saw it go black, and nothing more: "+theirs);
            var c=who(h,l,b,PellPeople.CALLUM);var june=who(h,l,b,PellPeople.JUNE);
            h.assertTrue(PellVisit.asleep(b,PellPeople.CALLUM)&&at(c,b,PellVisit.CALLUM_SLEEPS.x,PellVisit.CALLUM_SLEEPS.z,.3)&&c.getMainHandItem().isEmpty(),"in the black, Callum is back on his mattress with nothing in his hand");
            h.assertTrue(PellKnife.towelAt(l,b)!=null,"a tea towel on the kitchen floor");
            h.assertTrue(at(june,b,PellKnife.ARCH.x(),PellKnife.ARCH.z(),.3)&&PellSite.open(l,b,PellSite.JUNE_DOOR),"June in the kitchen's arch, her door open behind her");
            h.assertTrue(thing(l,b,-6,0,-61,CUTLERY,2)&&thing(l,b,-11,0,-71,KNIFE_BLOCK,1),"the knife gone");
        }).thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_KNIFE_BED),"June: back to bed"))
        .thenExecute(()->h.assertTrue(f.own(p,P).getBoolean("HeardJuneAfterKnife"),"heard in the kitchen"))
        .thenWaitUntil(()->h.assertFalse(who(h,l,b,PellPeople.JUNE).saying(),"June between lines"))
        .thenExecute(()->LiteraryVignettes.talk(p,who(h,l,b,PellPeople.JUNE)))
        .thenIdle(1).thenExecute(()->h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_KNIFE_HAND),"a hand on her: bed, love: "+said(who(h,l,b,PellPeople.JUNE))))
        .thenWaitUntil(()->h.assertTrue(PellKnife.stage(b)==PellKnife.LOCKING&&!PellKnife.lit(l,b),"the light off as she goes to lock up"))
        .thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_KNIFE_OUT),"at the sliding door she asks the reader out"))
        .thenExecute(()->{
            h.assertTrue(PellSite.open(l,b,PellSite.SLIDER),"not shut on a reader inside");
            f.at(p,P,-6.5,0,-74.5);
        }).thenWaitUntil(()->h.assertTrue(!PellSite.open(l,b,PellSite.SLIDER)&&PellVisit.locked(b,PellSite.SLIDER),"once they are out, she shuts it and locks it"))
        .thenWaitUntil(()->h.assertTrue(PellKnife.stage(b)==PellKnife.OVER,"and goes back to her room"))
        .thenExecute(()->{
            h.assertTrue(at(who(h,l,b,PellPeople.JUNE),b,PellVisit.HER_ROOM.x(),PellVisit.HER_ROOM.z(),.7)&&!PellSite.open(l,b,PellSite.JUNE_DOOR),"in her room, her door shut");
            mark[0]=PellVisit.hour(b);
        }).thenIdle(20).thenExecute(()->{
            h.assertTrue(PellVisit.hour(b)>mark[0],"the night goes on: "+mark[0]+" to "+PellVisit.hour(b));
            h.assertTrue(!PellNight.callumOut(b)&&PellVisit.asleep(b,PellPeople.CALLUM),"Callum asleep, not out on the line again");
            h.assertTrue(PellKnife.towelAt(l,b)!=null&&thing(l,b,-6,0,-61,CUTLERY,2),"the towel where it lay, and no knife in his drawer");
        }).thenSucceed();
    }
    @AfterBatch(batch="pell_knife") public static void knifeDone(ServerLevel l){done();}

    /**
     * 0.4.534, the knife when nobody comes. Two readers a week in, out in the garage: one asleep on the camp bed, one awake looking at
     * the floor. At one Callum is at the sink with the knife, and the reader asleep wakes; neither goes in. After a minute, with nobody
     * in the house, the knife is back in his drawer and he is out on the line again, the light off and the sliding door shut and locked
     * as June left it, no towel on the floor; nobody was found and nobody is on Gail's file, and the night goes on.
     */
    @GameTest(template="empty",batch="pell_knife_quiet",timeoutTicks=6000)
    public static void whenNobodyComesTheKnifeGoesBackAsIfItNeverWas(GameTestHelper h){
        var f=house(h);var p=f.player("pell_quiet_sleeper",P);var q=f.player("pell_quiet_garage",P);var l=f.level(P);var b=f.base(P);
        keep(f,p,"FirstVisit");keep(f,q,"FirstVisit");
        f.at(p,P,.5,0,-44);f.at(q,P,-6.5,0,-36);
        var bed=b.offset(CAMP_BED_AT);long[] mark={0};
        h.startSequence().thenWaitUntil(()->weekBegun(h,l,b))
        .thenExecute(()->{
            PellKnife.beforeOne(l,b);
            f.at(p,P,14.5,0,-89.5);hand(p,bed);
            h.assertTrue(p.isSleeping(),"one reader asleep in the garage");
            f.at(q,P,13.5,0,-91.5);face(q,0,90);
        }).thenWaitUntil(()->h.assertTrue(PellKnife.stage(b)==PellKnife.AT_SINK,"at one, Callum is at the sink"))
        .thenIdle(1).thenExecute(()->{
            h.assertFalse(p.isSleeping(),"the reader asleep wakes");
            h.assertTrue(told(p).contains(PellTexts.KNIFE_WOKEN),"a light on in the kitchen");
            face(p,0,90);mark[0]=PellVisit.hour(b);
        }).thenIdle(40).thenExecute(()->h.assertTrue(PellVisit.hour(b)==mark[0]&&PellKnife.stage(b)==PellKnife.AT_SINK,"he waits, and the hour holds"))
        .thenWaitUntil(()->h.assertTrue(PellKnife.stage(b)==PellKnife.QUIET,"nobody comes, and he goes back"))
        .thenExecute(()->{
            var c=who(h,l,b,PellPeople.CALLUM);
            h.assertTrue(PellNight.callumOut(b)&&at(c,b,PellNight.CALLUM_LINE.x(),PellNight.CALLUM_LINE.z(),.3)&&c.getMainHandItem().isEmpty(),"out on the line again, nothing in his hand: "+c.position());
            h.assertTrue(thing(l,b,-6,0,-61,CUTLERY,1),"the knife back in his drawer");
            h.assertFalse(PellKnife.lit(l,b),"the light off");
            h.assertTrue(!PellSite.open(l,b,PellSite.SLIDER)&&PellVisit.locked(b,PellSite.SLIDER),"the sliding door shut and locked as June left it");
            h.assertTrue(PellKnife.towelAt(l,b)==null&&PellKnife.found(b).isEmpty(),"no towel, and nobody found");
            for(var r:new ServerPlayer[]{p,q}){var own=f.own(r,P);h.assertTrue(own.getInt("GailMarks")==0&&!own.getBoolean("InTheBlack")&&!own.getBoolean("SawKnife"),"nothing on anybody's file: "+own);}
            mark[0]=PellVisit.hour(b);
        }).thenIdle(20).thenExecute(()->h.assertTrue(PellVisit.hour(b)>mark[0],"the night goes on: "+mark[0]+" to "+PellVisit.hour(b)))
        .thenSucceed();
    }
    @AfterBatch(batch="pell_knife_quiet") public static void knifeQuietDone(ServerLevel l){done();}

    /**
     * 0.4.534, the morning after the knife, with two readers: the one June found in the kitchen in the night, and one who saw him at the
     * sink with it. The night as the knife leaves it; both asleep in the garage, and it hurries to breakfast. On her way to lay it, June
     * has picked up the towel. At breakfast, once the goggles have been asked after, June tells the table that knives stay in the block
     * in this house, and both heard it. Gail rings; June has her version in first: there was trouble, one of them went for Callum in the
     * kitchen with a knife, nobody's hurt, they'd had it out of the block; then Tilly a different girl, and she puts them on. The reader
     * June found takes it, and Gail says what June has told her and asks what happened; they are offered what they could truly say
     * (that they were getting it off him, that they do not know, that June is lying, or nothing), and say she is lying; Gail won't have
     * it, and it goes on their file. Given back, June holds it out to the other, whom Gail asks whether it was them; offered that it
     * wasn't, that they saw him at the sink with it, that they want to leave, or nothing, they say they saw him; Gail will put down what
     * they saw. June back on says they are upset, that she'll keep an eye, and the week's goodbye. Each has had the second visit; the
     * call put nobody on the file who was not already there.
     */
    @GameTest(template="empty",batch="pell_knife_call",timeoutTicks=12000)
    public static void theMorningAfterTheKnifeGailHasJunesVersion(GameTestHelper h){
        var f=house(h);var p=f.player("pell_after_found",P);var q=f.player("pell_after_saw",P);var l=f.level(P);var b=f.base(P);
        keep(f,p,"FirstVisit");keep(f,q,"FirstVisit");keep(f,p,"FoughtCallum");keep(f,q,"SawKnife");
        f.at(p,P,.5,0,-44);f.at(q,P,-6.5,0,-36);
        var bed=b.offset(CAMP_BED_AT);var phone=b.offset(PellCall.PHONE);
        h.startSequence().thenWaitUntil(()->weekBegun(h,l,b))
        .thenExecute(()->{
            PellKnife.beforeOne(l,b);PellKnife.foundThere(l,b,p);
            h.assertTrue(PellKnife.towelAt(l,b)!=null&&thing(l,b,-6,0,-61,CUTLERY,2),"the night as the knife leaves it");
            f.at(p,P,14.5,0,-89.5);f.at(q,P,13.5,0,-91.5);
            hand(p,bed);hand(q,b.offset(CAMP_BED_AT.north()));
            h.assertTrue(p.isSleeping()&&q.isSleeping(),"both asleep in the garage");
            PellVisit.hurry(b,PellMorning.DAWN-10);
        }).thenWaitUntil(()->h.assertTrue(PellVisit.step(b)>=PellMorning.LAID,"breakfast laid"))
        .thenExecute(()->{
            h.assertTrue(PellKnife.towelAt(l,b)==null&&!PellKnife.lit(l,b),"the towel picked up off the kitchen floor");
            h.assertTrue(thing(l,b,-6,0,-61,CUTLERY,2),"and nothing put back in his drawer");
        }).thenWaitUntil(()->h.assertTrue(PellVisit.step(b)>=PellMorning.CALLED&&PellVisit.placeOf(b,PellPeople.TILLY)==0&&PellVisit.placeOf(b,PellPeople.CALLUM)==1,"breakfast: Tilly and Callum at the table"))
        .thenExecute(()->{
            // 0.4.537: a week in, the night in the garage has its entry due for each, after the knife's for the one June found; with no
            // journal to go in, they wait.
            var found=PellEntries.due(f.own(p,P));var saw=PellEntries.due(f.own(q,P));
            h.assertTrue(found.equals(List.of(PellEntries.KNIFE,PellEntries.NIGHT+2))&&saw.equals(List.of(PellEntries.NIGHT+2)),"the entries due: "+found+", "+saw);
            h.assertTrue(PellEntries.written(f.own(p,P)).isEmpty()&&PellEntries.written(f.own(q,P)).isEmpty(),"none written, with no journal");
            f.at(q,P,-9.5,0,-67.5);h.assertTrue(io.github.knaitoe.theoldesthouse.house.HouseSitting.sit(q,b.offset(PellVisit.TABLE[3].chair())),"a reader sits");
            f.at(p,P,-9.5,0,-69.5);h.assertTrue(io.github.knaitoe.theoldesthouse.house.HouseSitting.sit(p,b.offset(PellVisit.TABLE[2].chair())),"the other sits");
        }).thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_GOGGLES),"the goggles"))
        .thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.TILLY)).equals(PellTexts.TILLY_ALL_RIGHT),"Tilly: that's all right"))
        .thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_KNIVES),"June: knives stay in the block"))
        .thenExecute(()->h.assertTrue(f.own(p,P).getBoolean("HeardKnivesStay")&&f.own(q,P).getBoolean("HeardKnivesStay"),"both heard"))
        .thenExecute(()->{hand(p,b.offset(-12,0,-69));hand(q,b.offset(-12,0,-69));})
        .thenWaitUntil(()->h.assertTrue(PellVisit.step(b)>=PellMorning.AFTER,"breakfast done"))
        .thenExecute(()->{p.stopRiding();q.stopRiding();f.at(p,P,-9.5,0,-67.5);f.at(q,P,-8.5,0,-67.6);})
        .thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_KNIFE_TROUBLE),"June to Gail: there was trouble"))
        .thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_KNIFE_WHAT),"one of them went for Callum, with a knife"))
        .thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_KNIFE_HURT),"nobody's hurt"))
        .thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_KNIFE_BLOCK),"out of the block"))
        .thenWaitUntil(()->h.assertTrue(PellCall.stage(b)==PellCall.OFFERED,"June holds it out"))
        .thenExecute(()->{
            hand(p,phone);
            h.assertTrue(p.getUUID().equals(PellCall.caller(b)),"the reader June found takes it");
            h.assertTrue(PellTexts.GAIL_KNIFE_ASKS.stream().allMatch(line->telling(p,"Gail",line)),"Gail has June's version, and asks: "+Talk.lines(p));
            var r=Talk.replies(p);
            h.assertTrue(r.contains(PellTexts.TELL_KNIFE_TOOK)&&r.contains(PellTexts.TELL_KNIFE_DONT_KNOW)&&r.contains(PellTexts.TELL_KNIFE_LYING)&&r.contains(PellTexts.TELL_NOTHING),"what they could truly say: "+r);
            h.assertFalse(r.contains(PellTexts.TELL_KNIFE_NOT_ME)||r.contains(PellTexts.TELL_WEEK_FINE),"not that it wasn't them, and not the week's answers: "+r);
            h.assertTrue(Talk.choose(p,PellTexts.TELL_KNIFE_LYING),"she's lying");
            h.assertTrue(telling(p,"Gail","I won't have that, love. Not about June.")&&telling(p,"Gail",PellTexts.GAIL_KNIFE_BYE),"Gail won't have it: "+Talk.lines(p));
            h.assertTrue(PellTexts.KNIFE_LYING.equals(f.own(p,P).getString("ToldGailWeek")),"they keep what they said");
            h.assertTrue(Talk.choose(p,PellTexts.HAND_BACK),"given back");
        }).thenIdle(1).thenExecute(()->{
            var june=who(h,l,b,PellPeople.JUNE);
            h.assertTrue(PellCall.caller(b)==null&&said(june).equals(PellTexts.JUNE_NEXT),"\"Your turn\", held out to the other");
            LiteraryVignettes.talk(q,june);
            h.assertTrue(q.getUUID().equals(PellCall.caller(b)),"the other takes it from her hand");
            h.assertTrue(PellTexts.GAIL_KNIFE_OTHER.stream().allMatch(line->telling(q,"Gail",line)),"was it you: "+Talk.lines(q));
            var r=Talk.replies(q);
            h.assertTrue(r.contains(PellTexts.TELL_KNIFE_NOT_ME)&&r.contains(PellTexts.TELL_KNIFE_SAW)&&r.contains(PellTexts.TELL_LEAVE)&&r.contains(PellTexts.TELL_NOTHING),"what they could truly say: "+r);
            h.assertFalse(r.contains(PellTexts.TELL_KNIFE_TOOK)||r.contains(PellTexts.TELL_KNIFE_LYING),"nothing that was not theirs: "+r);
            h.assertTrue(Talk.choose(q,PellTexts.TELL_KNIFE_SAW),"saw him at the sink with it");
            h.assertTrue(telling(q,"Gail","On his own? With a knife?")&&telling(q,"Gail",PellTexts.GAIL_KNIFE_BYE),"Gail will put down what they saw: "+Talk.lines(q));
            h.assertTrue(Talk.choose(q,PellTexts.HAND_BACK),"given back");
        }).thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_KNIFE_UPSET),"June: they're upset"))
        .thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_KNIFE_EYE),"she'll keep an eye"))
        .thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_WEEK_BYE),"the week's goodbye"))
        .thenWaitUntil(()->h.assertTrue(PellCall.stage(b)==PellCall.DONE,"hung up"))
        .thenExecute(()->{
            var mine=f.own(p,P);var theirs=f.own(q,P);
            h.assertTrue(mine.getBoolean("SecondVisit")&&theirs.getBoolean("SecondVisit"),"each has had the second visit");
            h.assertTrue(mine.getInt("GailMarks")==1&&theirs.getInt("GailMarks")==0,"the call put nobody on the file: "+mine.getInt("GailMarks")+", "+theirs.getInt("GailMarks"));
            h.assertTrue(PellTexts.KNIFE_SAW.equals(theirs.getString("ToldGailWeek")),"what each said stays theirs");
        }).thenSucceed();
    }
    @AfterBatch(batch="pell_knife_call") public static void knifeCallDone(ServerLevel l){done();}

    /**
     * 0.4.535, June's hand (docs/PELL_HOUSE_HAND_0_4_535.md), a week in, with two readers: one in the kitchen, one out on the patio. A
     * while after her hello June goes to put a wash on, and the hour holds. The reader goes ahead of her into the laundry; she asks them
     * out, and does not shut the door on them. Once they are out in the hall, a step back from its door, she shuts both the laundry's
     * doors. A moment later a door slams and she cries out: the reader in the hall hears it at the back of the house, the one on the
     * patio hears it over at the house, and the reader nearest the laundry is the one she will say did it, on Gail's file for it once;
     * the other is not. She comes out with her hand wrapped in a tea towel: it's nothing, she caught it in the door; the reader at the
     * door sees the towel, the one on the patio does not. Back at her stove the hour goes on, and a hand on her gets a word about her
     * hand. When the visit begins again, her hand is as it was and nobody is to blame.
     */
    @GameTest(template="empty",batch="pell_hand",timeoutTicks=9000)
    public static void aWeekInJunesHandIsCaughtInTheLaundryDoorOffScreen(GameTestHelper h){
        var f=house(h);var p=f.player("pell_hand_hall",P);var q=f.player("pell_hand_patio",P);var l=f.level(P);var b=f.base(P);
        keep(f,p,"FirstVisit");keep(f,q,"FirstVisit");
        f.at(p,P,.5,0,-44);f.at(q,P,-6.5,0,-36);
        long[] mark={0};
        h.startSequence().thenWaitUntil(()->weekBegun(h,l,b))
        .thenExecute(()->{f.at(p,P,-7.5,0,-67.5);f.at(q,P,-6.5,0,-74.5);})
        .thenWaitUntil(()->h.assertTrue(PellWeek.welcomed(b),"June calls hello"))
        .thenExecute(()->PellHand.beforeHand(b))
        .thenWaitUntil(()->h.assertTrue(PellHand.stage(b)==PellHand.TO_LAUNDRY,"a while after her hello, June goes to the laundry"))
        .thenExecute(()->{
            h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_TO_LAUNDRY),"a wash to put on: "+said(who(h,l,b,PellPeople.JUNE)));
            mark[0]=PellVisit.hour(b);
            // The reader goes ahead of her into the laundry.
            f.at(p,P,8.5,0,-67.5);
        }).thenWaitUntil(()->h.assertTrue(PellHand.stage(b)==PellHand.SHUTTING,"at the laundry"))
        .thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_LAUNDRY_OUT),"she asks the reader out"))
        .thenExecute(()->{
            h.assertTrue(PellSite.open(l,b,PellSite.LAUNDRY_DOOR)&&PellHand.stage(b)==PellHand.SHUTTING,"not shut on a reader inside");
            h.assertTrue(PellVisit.hour(b)==mark[0],"the hour holds while she is away: "+mark[0]+" to "+PellVisit.hour(b));
            // Out into the hall, a step back from its door and looking at it; the other still on the patio.
            f.at(p,P,7.5,0,-64.6);lookAt(p,net.minecraft.world.phys.Vec3.atCenterOf(b.offset(7,1,-68)));told(p);told(q);
        }).thenWaitUntil(()->h.assertTrue(PellHand.stage(b)==PellHand.INSIDE,"once they are out, she shuts herself in"))
        .thenExecute(()->h.assertTrue(!PellSite.open(l,b,PellSite.LAUNDRY_DOOR)&&!PellSite.open(l,b,PellSite.BACK_DOOR),"both the laundry's doors shut"))
        .thenWaitUntil(()->h.assertTrue(PellHand.stage(b)==PellHand.CRIED,"a door slams, and the cry"))
        .thenExecute(()->{
            h.assertTrue(told(p).contains(PellTexts.SLAM_HEARD),"heard in the house, at the back");
            h.assertTrue(told(q).contains(PellTexts.SLAM_FAR),"heard from the patio, over at the house");
            h.assertTrue(p.getUUID().equals(PellHand.blamed(b)),"the reader nearest the laundry is the one she will say did it");
            var mine=f.own(p,P);var theirs=f.own(q,P);
            h.assertTrue(mine.getBoolean("BlamedForHand")&&mine.getBoolean("MarkHand")&&mine.getInt("GailMarks")==1&&!mine.getBoolean("AwayForHand"),"on Gail's file for it, once: "+mine);
            h.assertTrue(theirs.getBoolean("HeardJuneHand")&&!theirs.getBoolean("BlamedForHand")&&theirs.getInt("GailMarks")==0,"the other heard it, and nothing more: "+theirs);
            h.assertTrue((who(h,l,b,PellPeople.JUNE).marks()&PellPeople.WRAPPED)!=0,"her hand wrapped");
        }).thenWaitUntil(()->h.assertTrue(PellHand.stage(b)==PellHand.BACK,"she comes out"))
        .thenExecute(()->{
            h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_HAND_OUT),"it's nothing, she caught it in the door: "+said(who(h,l,b,PellPeople.JUNE)));
            h.assertTrue(told(p).contains(PellTexts.HAND_WRAPPED)&&f.own(p,P).getBoolean("SawJuneHand"),"the reader at the door sees the towel");
            h.assertFalse(f.own(q,P).getBoolean("SawJuneHand"),"not from the patio");
            h.assertTrue(PellSite.open(l,b,PellSite.LAUNDRY_DOOR),"the laundry door open");
        }).thenWaitUntil(()->h.assertTrue(PellHand.stage(b)==PellHand.DONE,"back at her stove"))
        .thenExecute(()->{h.assertTrue(at(who(h,l,b,PellPeople.JUNE),b,PellVisit.KITCHEN.x(),PellVisit.KITCHEN.z(),.3),"at her stove");mark[0]=PellVisit.hour(b);})
        .thenIdle(20).thenExecute(()->h.assertTrue(PellVisit.hour(b)>mark[0],"the hour goes on: "+mark[0]+" to "+PellVisit.hour(b)))
        .thenWaitUntil(()->h.assertFalse(who(h,l,b,PellPeople.JUNE).saying(),"June between lines"))
        .thenExecute(()->{f.at(p,P,-7.5,0,-67.5);LiteraryVignettes.talk(p,who(h,l,b,PellPeople.JUNE));})
        .thenIdle(1).thenExecute(()->{
            h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_HAND_TALK[0]),"a hand on her: don't fuss: "+said(who(h,l,b,PellPeople.JUNE)));
            h.assertTrue(f.own(p,P).getBoolean("AskedJuneAboutHand"),"and they keep that they asked");
            // The house left, and its visit begun again.
            f.at(p,P,.5,0,-30);f.at(q,P,-6.5,0,-30);PellPeople.vacate(b);
        }).thenWaitUntil(()->h.assertTrue(PellWeek.visit(b)==2&&PellVisit.step(b)==PellVisit.SETTLED&&PellHand.stage(b)==PellHand.NOT_YET,"the visit begun again"))
        .thenExecute(()->h.assertTrue(PellHand.blamed(b)==null&&(who(h,l,b,PellPeople.JUNE).marks()&PellPeople.WRAPPED)==0,"her hand as it was, and nobody to blame"))
        .thenSucceed();
    }
    @AfterBatch(batch="pell_hand") public static void handDone(ServerLevel l){done();}

    /**
     * 0.4.535, the week's call after the knife and June's hand, with two readers: the one June found in the kitchen in the night, who
     * heard the laundry door; and the one she says slammed it on her hand. June has her version of both in first: the knife, then her
     * hand, only bruised. The reader June found takes it; Gail asks about the knife and they say they were getting it off him; then, as
     * one of the rest, whether they saw what happened to June's hand: offered that they heard it, that it wasn't them, that they want to
     * leave, or nothing (and nothing that was not theirs), they say they heard it. Given back, the other takes it; Gail asks whether the
     * knife was them, and they say they want to leave; then that June says they slammed the door on her hand: offered that they never
     * touched it, that she did it herself, or nothing (not that they were away, nor leaving a second time), they say she did it herself,
     * and Gail won't hear it. June back on: they'll say anything, she'll keep an eye, the week's goodbye; hung up, give it time. What each
     * said about each thing stays theirs, and the call put nobody on the file.
     */
    @GameTest(template="empty",batch="pell_hand_call",timeoutTicks=9000)
    public static void theWeeksCallHasJunesVersionOfTheKnifeAndHerHand(GameTestHelper h){
        var f=house(h);var p=f.player("pell_handcall_found",P);var q=f.player("pell_handcall_named",P);var l=f.level(P);var b=f.base(P);
        keep(f,p,"FirstVisit");keep(f,q,"FirstVisit");keep(f,p,"FoughtCallum");keep(f,p,"HeardJuneHand");
        f.at(p,P,.5,0,-44);f.at(q,P,-6.5,0,-36);
        var phone=b.offset(PellCall.PHONE);
        h.startSequence().thenWaitUntil(()->weekBegun(h,l,b))
        .thenExecute(()->{
            PellKnife.foundThere(l,b,p);PellHand.blamed(l,b,q,false);PellCall.afterBreakfast(l,b);
            f.at(p,P,-9.5,0,-67.5);f.at(q,P,-8.5,0,-67.6);
            h.assertTrue((who(h,l,b,PellPeople.JUNE).marks()&PellPeople.WRAPPED)!=0,"June's hand wrapped");
        })
        .thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_KNIFE_BLOCK),"June to Gail: the knife first"))
        .thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_HAND_AND),"and her hand"))
        .thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_HAND_BRUISED),"only bruised"))
        .thenWaitUntil(()->h.assertTrue(PellCall.stage(b)==PellCall.OFFERED,"June holds it out"))
        .thenExecute(()->{
            hand(p,phone);
            h.assertTrue(p.getUUID().equals(PellCall.caller(b)),"the reader June found takes it");
            h.assertTrue(PellTexts.GAIL_KNIFE_ASKS.stream().allMatch(line->telling(p,"Gail",line)),"the knife first: "+Talk.lines(p));
            h.assertTrue(Talk.choose(p,PellTexts.TELL_KNIFE_TOOK),"getting it off him");
            h.assertTrue(PellTexts.gailHandAsks(false,true).stream().allMatch(line->telling(p,"Gail",line))&&!telling(p,"Gail",PellTexts.GAIL_KNIFE_BYE),"then June's hand, before her goodbye: "+Talk.lines(p));
            var r=Talk.replies(p);
            h.assertTrue(r.contains(PellTexts.TELL_HAND_HEARD)&&r.contains(PellTexts.TELL_HAND_WASNT_ME)&&r.contains(PellTexts.TELL_LEAVE)&&r.contains(PellTexts.TELL_NOTHING),"what they could truly say: "+r);
            h.assertFalse(r.contains(PellTexts.TELL_HAND_NOT_ME)||r.contains(PellTexts.TELL_HAND_HERSELF)||r.contains(PellTexts.TELL_HAND_AWAY),"nothing that was not theirs: "+r);
            h.assertTrue(Talk.choose(p,PellTexts.TELL_HAND_HEARD),"heard it");
            h.assertTrue(telling(p,"Gail","Heard what, love? A door?")&&telling(p,"Gail",PellTexts.GAIL_KNIFE_BYE),"Gail puts it down, and goodbye: "+Talk.lines(p));
            var own=f.own(p,P);
            h.assertTrue(PellTexts.KNIFE_TOOK.equals(own.getString("ToldGailWeek"))&&PellTexts.HAND_HEARD.equals(own.getString("ToldGailHand")),"each answer kept: "+own);
            h.assertTrue(Talk.choose(p,PellTexts.HAND_BACK),"given back");
        }).thenIdle(1).thenExecute(()->{
            LiteraryVignettes.talk(q,who(h,l,b,PellPeople.JUNE));
            h.assertTrue(q.getUUID().equals(PellCall.caller(b)),"the other takes it");
            h.assertTrue(PellTexts.GAIL_KNIFE_OTHER.stream().allMatch(line->telling(q,"Gail",line)),"was it you: "+Talk.lines(q));
            h.assertTrue(Talk.choose(q,PellTexts.TELL_LEAVE),"they want to leave");
            h.assertTrue(PellTexts.gailHandAsks(true,true).stream().allMatch(line->telling(q,"Gail",line)),"June says they slammed the door on her hand: "+Talk.lines(q));
            var r=Talk.replies(q);
            h.assertTrue(r.contains(PellTexts.TELL_HAND_NOT_ME)&&r.contains(PellTexts.TELL_HAND_HERSELF)&&r.contains(PellTexts.TELL_NOTHING),"what they could truly say: "+r);
            h.assertFalse(r.contains(PellTexts.TELL_HAND_AWAY)||r.contains(PellTexts.TELL_HAND_HEARD)||r.contains(PellTexts.TELL_LEAVE),"not away, and leaving said once: "+r);
            h.assertTrue(Talk.choose(q,PellTexts.TELL_HAND_HERSELF),"she did it herself");
            h.assertTrue(telling(q,"Gail","Slammed a door on her own hand? Love.")&&telling(q,"Gail",PellTexts.GAIL_KNIFE_BYE),"Gail won't hear it: "+Talk.lines(q));
            var own=f.own(q,P);
            h.assertTrue(PellTexts.LEAVE.equals(own.getString("ToldGailWeek"))&&PellTexts.HAND_HERSELF.equals(own.getString("ToldGailHand")),"each answer kept: "+own);
            h.assertTrue(Talk.choose(q,PellTexts.HAND_BACK),"given back");
        }).thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_HAND_UPSET),"June: they'll say anything"))
        .thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_KNIFE_EYE),"she'll keep an eye"))
        .thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_WEEK_BYE),"the week's goodbye"))
        .thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_GIVE_IT_TIME),"hung up: give it time"))
        .thenWaitUntil(()->h.assertTrue(PellCall.stage(b)==PellCall.DONE,"back to the sink"))
        .thenExecute(()->{
            var mine=f.own(p,P);var theirs=f.own(q,P);
            h.assertTrue(mine.getInt("GailMarks")==1&&theirs.getInt("GailMarks")==1,"the call put nobody on the file: "+mine.getInt("GailMarks")+", "+theirs.getInt("GailMarks"));
        }).thenSucceed();
    }
    @AfterBatch(batch="pell_hand_call") public static void handCallDone(ServerLevel l){done();}

    /**
     * 0.4.535, the week's call after June's hand alone, with one reader: the one she says slammed the door on it, who was out of the
     * house when it happened. June tells Gail it was not trouble, not really: one of them slammed the laundry door on her hand, only
     * bruised. Gail asks the reader about it straight away, not how the week has been: offered that they never touched the door, that
     * they were not even in the house, that she did it herself, or nothing, they say they were not in the house. Gail has only June's
     * word and theirs, and it goes on their file, once. June back on, she'll keep an eye, and hangs up.
     */
    @GameTest(template="empty",batch="pell_hand_away",timeoutTicks=6000)
    public static void aReaderAwayFromTheHouseIsTheOneJuneSaysSlammedTheDoor(GameTestHelper h){
        var f=house(h);var p=f.player("pell_hand_away",P);var l=f.level(P);var b=f.base(P);
        keep(f,p,"FirstVisit");
        f.at(p,P,.5,0,-44);
        var phone=b.offset(PellCall.PHONE);
        h.startSequence().thenWaitUntil(()->weekBegun(h,l,b))
        .thenExecute(()->{PellHand.blamed(l,b,p,true);PellCall.afterBreakfast(l,b);f.at(p,P,-9.5,0,-67.5);})
        .thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_HAND_TROUBLE),"June to Gail: not trouble, not really"))
        .thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_HAND_WHAT),"the laundry door on her hand"))
        .thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_HAND_BRUISED),"only bruised"))
        .thenWaitUntil(()->h.assertTrue(PellCall.stage(b)==PellCall.OFFERED,"June holds it out"))
        .thenExecute(()->{
            hand(p,phone);
            h.assertTrue(PellTexts.gailHandAsks(true,false).stream().allMatch(line->telling(p,"Gail",line))&&!telling(p,"Gail",PellTexts.GAIL_WEEK_ASKS),"Gail asks about her hand straight away: "+Talk.lines(p));
            var r=Talk.replies(p);
            h.assertTrue(r.contains(PellTexts.TELL_HAND_NOT_ME)&&r.contains(PellTexts.TELL_HAND_AWAY)&&r.contains(PellTexts.TELL_HAND_HERSELF)&&r.contains(PellTexts.TELL_NOTHING),"what they could truly say: "+r);
            h.assertFalse(r.contains(PellTexts.TELL_WEEK_FINE)||r.contains(PellTexts.TELL_HAND_HEARD),"not the week's answers: "+r);
            h.assertTrue(Talk.choose(p,PellTexts.TELL_HAND_AWAY),"not even in the house");
            h.assertTrue(telling(p,"Gail","I've only got her word and yours, love. It's going on your file.")&&telling(p,"Gail",PellTexts.GAIL_KNIFE_BYE),"her word and theirs: "+Talk.lines(p));
            var own=f.own(p,P);
            h.assertTrue(PellTexts.HAND_AWAY.equals(own.getString("ToldGailHand"))&&own.getString("ToldGailWeek").isEmpty()&&own.getInt("GailMarks")==1,"kept, and on the file once: "+own);
            h.assertTrue(Talk.choose(p,PellTexts.HAND_BACK),"given back");
        }).thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_KNIFE_EYE),"she'll keep an eye"))
        .thenWaitUntil(()->h.assertTrue(PellCall.stage(b)==PellCall.DONE,"hung up"))
        .thenSucceed();
    }
    @AfterBatch(batch="pell_hand_away") public static void handAwayDone(ServerLevel l){done();}

    /**
     * 0.4.537, the journal's false entries (docs/PELL_HOUSE_ENTRIES_0_4_537.md), with two readers a week in, each carrying their own
     * journal. One has written a page of theirs in the House. June says that one slammed the laundry door on her hand, and the entry
     * goes into their journal on that page, after their own words, in their own hand; the other's journal has none of it. Said again,
     * nothing more goes in. The other, found in the kitchen after the knife, has their journal open at its first spread: the entry
     * waits while it is open, and once it is shut it goes in on their blank first page, begun in the House. Written on below, the first
     * reader's page keeps it after their words. Nothing tells either reader it happened.
     */
    @GameTest(template="empty",batch="pell_entries",timeoutTicks=6000)
    public static void whatJuneSaysTheyDidIsInTheirJournalInTheirOwnHand(GameTestHelper h){
        var f=house(h);var p=f.player("pell_entries_named",P);var q=f.player("pell_entries_found",P);var l=f.level(P);var b=f.base(P);var s=l.getServer();
        keep(f,p,"FirstVisit");keep(f,q,"FirstVisit");
        f.at(p,P,.5,0,-44);f.at(q,P,-6.5,0,-36);
        var data=Journals.get(s);String mine="June made pancakes. Tilly didn't eat hers.",more=mine+"\nCallum stood at the end of the hall all evening.";
        String hand=PellTexts.falseEntry(PellEntries.HAND),knife=PellTexts.falseEntry(PellEntries.KNIFE);
        h.startSequence().thenWaitUntil(()->weekBegun(h,l,b))
        .thenExecute(()->{
            p.getInventory().add(data.issue(p));q.getInventory().add(data.issue(q));
            h.assertTrue(Journals.edit(p,1,JournalTests.page(mine))==Journals.Edit.ACCEPTED,"the reader writes a page in the House");
        }).thenWaitUntil(()->h.assertTrue(data.page(p.getUUID(),1).text().equals(mine)&&data.page(p.getUUID(),1).house(),"waiting for the page to be kept"))
        .thenExecute(()->{
            told(p);told(q);
            PellHand.blamed(l,b,p,false);
            h.assertTrue(PellEntries.due(f.own(p,P)).equals(List.of(PellEntries.HAND)),"the hand's entry due: "+PellEntries.due(f.own(p,P)));
        }).thenWaitUntil(()->h.assertTrue(OwnHand.lines(s,p.getUUID()).equals(List.of("1: "+hand)),"waiting for it in their journal: "+OwnHand.lines(s,p.getUUID())))
        .thenExecute(()->{
            var page=data.page(p.getUUID(),1);
            h.assertTrue(page.text().equals(mine),"their own words untouched: "+page.text());
            h.assertTrue(page.notes().stream().anyMatch(n->n.mark()==JournalPage.Mark.OWN&&n.text().equals(hand)),"on their page, in their own hand");
            h.assertTrue(PellEntries.due(f.own(p,P)).isEmpty()&&PellEntries.written(f.own(p,P)).equals(List.of(PellEntries.HAND)),"written, and no longer due");
            h.assertTrue(OwnHand.lines(s,q.getUUID()).isEmpty(),"nothing in the other's journal");
            PellHand.blamed(l,b,p,false);
            h.assertTrue(PellEntries.due(f.own(p,P)).isEmpty(),"said again, nothing more is due");
            // The other's journal lies open at its first spread when June finds them in the kitchen.
            CoAuthor.viewing(q,0);
            PellKnife.foundThere(l,b,q);
            h.assertTrue(PellEntries.due(f.own(q,P)).equals(List.of(PellEntries.KNIFE)),"the knife's entry due for the reader June found");
        }).thenIdle(PellEntries.TRY*3).thenExecute(()->{
            h.assertTrue(OwnHand.lines(s,q.getUUID()).isEmpty()&&PellEntries.due(f.own(q,P)).equals(List.of(PellEntries.KNIFE)),"never on a page they have open: it waits");
            h.assertTrue(OwnHand.lines(s,p.getUUID()).size()==1,"and nothing more in the first reader's");
            CoAuthor.viewing(q,-1);
        }).thenWaitUntil(()->h.assertTrue(OwnHand.lines(s,q.getUUID()).equals(List.of("1: "+knife)),"shut, it goes in on their first page: "+OwnHand.lines(s,q.getUUID())))
        .thenExecute(()->{
            var page=data.page(q.getUUID(),1);
            h.assertTrue(page.house()&&page.text().isEmpty(),"a page begun in the House, with nothing of theirs on it");
            h.assertTrue(PellEntries.due(f.own(q,P)).isEmpty()&&PellEntries.written(f.own(q,P)).equals(List.of(PellEntries.KNIFE)),"written");
            h.assertTrue(Journals.edit(p,1,JournalTests.page(more))==Journals.Edit.ACCEPTED,"the first reader writes on below");
        }).thenWaitUntil(()->h.assertTrue(data.page(p.getUUID(),1).text().equals(more),"waiting for it to be kept"))
        .thenExecute(()->{
            h.assertTrue(OwnHand.lines(s,p.getUUID()).equals(List.of("1: "+hand)),"written on, the page keeps it: "+OwnHand.lines(s,p.getUUID()));
            h.assertFalse(told(p).stream().anyMatch(t->t.contains(hand))||told(q).stream().anyMatch(t->t.contains(knife)),"nothing tells them");
        }).thenSucceed();
    }
    @AfterBatch(batch="pell_entries") public static void entriesDone(ServerLevel l){done();}

    /** Whether a reader's journal keeps a record under this heading whose first line is this, said by this voice. */
    private static boolean kept(ServerPlayer p,String heading,String who,String line){
        var all=TalkHistory.conversations(p.server,p.getUUID());
        for(int i=0;i<all.size();i++){
            var talk=all.getCompound(i);if(!talk.getString("Who").equals(heading))continue;
            var lines=talk.getList("Lines",net.minecraft.nbt.Tag.TAG_COMPOUND);
            if(!lines.isEmpty()&&lines.getCompound(0).getString("S").equals(who)&&lines.getCompound(0).getString("T").equals(line))return true;
        }
        return false;
    }
    /**
     * 0.4.536, the tapes (docs/PELL_HOUSE_TAPES_0_4_536.md), with two readers a week in. The shed's padlock hangs open, and a hand opens
     * its door. One reader in the shed puts on the tape in the deck, the eighth: the screen lights; they are watching it and the other,
     * out by the pool, is not. Her first line is kept in the watcher's journal under the tape's date, and not by the pool. Watched to its
     * end, the eighth is kept, before its turn, and the screen goes dark. From the shelf, the spines in order with the eighth's case
     * empty; the first taken, it plays from its start. The other reader, in after her first line, is not watching it; the first, walked
     * off past earshot, stops watching; at its end neither has it. Played again with both in the shed, both watch it to its end and are
     * on to the second, the other in turn. A hand on the set while it plays stops it. The other, through the rest in turn, watches the
     * ninth from the shelf and has read the tapes; the first has not.
     */
    @GameTest(template="empty",batch="pell_tapes",timeoutTicks=6000)
    public static void aWeekInTheShedIsOpenAndItsTapesPlayForWhoeverIsByTheDeck(GameTestHelper h){
        var f=house(h);var p=f.player("pell_tapes_watcher",P);var q=f.player("pell_tapes_other",P);var l=f.level(P);var b=f.base(P);
        keep(f,p,"FirstVisit");keep(f,q,"FirstVisit");
        f.at(p,P,6.5,0,-87.5);f.at(q,P,-6.5,0,-82.5);
        var tv=b.offset(PellTapes.TV);var shelf=b.offset(5,1,-93);var door=b.offset(PellSite.SHED_DOOR);
        h.startSequence().thenWaitUntil(()->weekBegun(h,l,b))
        .thenExecute(()->{
            PellHand.skip(b);
            h.assertFalse(PellSite.open(l,b,PellSite.SHED_DOOR),"the shed shut");
            told(p);hand(p,door);
            h.assertTrue(PellSite.open(l,b,PellSite.SHED_DOOR),"a week in its padlock hangs open, and it opens to a hand");
        }).thenIdle(1).thenExecute(()->{
            // What a reader is told reaches them at the end of the tick.
            h.assertTrue(told(p).contains(PellTexts.SHED_OPEN),"the padlock on its hasp");
            f.at(p,P,5.5,0,-91.5);lookAt(p,net.minecraft.world.phys.Vec3.atCenterOf(tv));
            hand(p,tv);
            h.assertTrue(PellTapes.playing(b)&&PellTapes.deck(b)==PellTapes.IN_DECK,"the tape in the deck plays: the eighth");
            h.assertTrue(thing(l,b,7,1,-93,SHED_TV,1),"the screen lit");
            h.assertTrue(PellTapes.watchers(b).contains(p.getUUID())&&!PellTapes.watchers(b).contains(q.getUUID()),"watching from the shed, not from the pool: "+PellTapes.watchers(b));
        })
        .thenWaitUntil(()->h.assertTrue((f.own(p,P).getLong("TapeLines")&1L<<(PellTapes.IN_DECK*4))!=0,"her first line, heard"))
        .thenExecute(()->{
            h.assertTrue(kept(p,PellTexts.tapeHeading(PellTapes.IN_DECK),PellTexts.TAPE_VOICE,PellTexts.TAPE_LINES[PellTapes.IN_DECK][0]),"kept in the journal under its date");
            h.assertTrue(f.own(q,P).getLong("TapeLines")==0,"not heard by the pool");
            PellTapes.nearEnd(l,b);
        })
        .thenWaitUntil(()->h.assertFalse(PellTapes.playing(b),"to its end"))
        .thenExecute(()->{
            var own=f.own(p,P);
            h.assertTrue(own.getInt("TapesWatched")==1<<PellTapes.IN_DECK&&own.getBoolean("WatchedOutOfOrder")&&own.getInt("TapeRun")==0,"the eighth watched, before its turn: "+own);
            h.assertTrue(f.own(q,P).getInt("TapesWatched")==0,"not from the pool");
            h.assertTrue(thing(l,b,7,1,-93,SHED_TV,0),"the screen dark");
            hand(p,shelf);
            var r=Talk.replies(p);
            for(int t=0;t<PellTapes.TAPES;t++)h.assertTrue(r.contains(PellTexts.spine(t,false))==(t!=PellTapes.IN_DECK),"the spines on the shelf, the eighth's in the deck: "+r);
            h.assertTrue(r.size()==PellTapes.TAPES&&r.get(r.size()-1).equals(PellTexts.TAPES_LEAVE),"and leave them: "+r);
            h.assertTrue(Talk.lines(p).contains("you: "+PellTexts.spine(PellTapes.IN_DECK,true)+"."),"the eighth's case empty: "+Talk.lines(p));
            told(p);
            h.assertTrue(Talk.choose(p,PellTexts.spine(0,false)),"the first taken");
            h.assertTrue(PellTapes.deck(b)==0&&PellTapes.playing(b),"in the deck, playing from its start");
        }).thenIdle(1).thenExecute(()->h.assertTrue(told(p).contains(PellTexts.TAPE_SWAP),"the deck's back in its case first"))
        .thenWaitUntil(()->h.assertTrue((f.own(p,P).getLong("TapeLines")&1L)!=0,"its first line"))
        .thenExecute(()->f.at(q,P,6.5,0,-91.5)).thenIdle(2)
        .thenExecute(()->{
            h.assertFalse(PellTapes.watchers(b).contains(q.getUUID()),"in after her first line: not watching it");
            f.at(p,P,-6.5,0,-82.5);
        }).thenIdle(2)
        .thenExecute(()->{
            h.assertFalse(PellTapes.watchers(b).contains(p.getUUID()),"walked off past earshot: no longer watching");
            PellTapes.nearEnd(l,b);
        })
        .thenWaitUntil(()->h.assertFalse(PellTapes.playing(b),"to its end"))
        .thenExecute(()->{
            h.assertTrue(f.own(p,P).getInt("TapesWatched")==1<<PellTapes.IN_DECK&&f.own(q,P).getInt("TapesWatched")==0,"neither has the first");
            f.at(p,P,5.5,0,-91.5);f.at(q,P,6.5,0,-91.5);
            hand(q,tv);
            h.assertTrue(PellTapes.deck(b)==0&&PellTapes.watchers(b).containsAll(List.of(p.getUUID(),q.getUUID())),"played again: both watching from its start");
            PellTapes.nearEnd(l,b);
        })
        .thenWaitUntil(()->h.assertFalse(PellTapes.playing(b),"to its end"))
        .thenExecute(()->{
            var mine=f.own(p,P);var theirs=f.own(q,P);
            h.assertTrue(mine.getInt("TapeRun")==1&&theirs.getInt("TapeRun")==1&&(theirs.getInt("TapesWatched")&1)==1,"both on to the second: "+mine+" / "+theirs);
            h.assertFalse(theirs.getBoolean("WatchedOutOfOrder"),"the other in turn");
            hand(q,tv);
            h.assertTrue(PellTapes.playing(b),"on again");
        }).thenIdle(1).thenExecute(()->{
            told(q);hand(q,tv);
            h.assertFalse(PellTapes.playing(b),"a hand on the set while it plays");
        }).thenIdle(1).thenExecute(()->{
            h.assertTrue(told(q).contains(PellTexts.TAPE_STOP),"the picture goes");
            PellTapes.asWatched(q,1,2,3,4,5,6,7);
            hand(q,shelf);
            h.assertTrue(Talk.choose(q,PellTexts.spine(8,false)),"the ninth from the shelf");
            PellTapes.nearEnd(l,b);
        })
        .thenWaitUntil(()->h.assertFalse(PellTapes.playing(b),"to its end"))
        .thenExecute(()->{
            var theirs=f.own(q,P);
            h.assertTrue(theirs.getInt("TapeRun")==PellTapes.TAPES&&theirs.getBoolean("ReadTheTapes"),"through them all in turn, the tapes read: "+theirs);
            h.assertFalse(f.own(p,P).getBoolean("ReadTheTapes"),"the first has not");
        }).thenSucceed();
    }
    @AfterBatch(batch="pell_tapes") public static void tapesDone(ServerLevel l){done();}

    /**
     * 0.4.536: on the first visit the shed is padlocked, and the set and the tapes are only what they are. A week in, the reader opens the
     * shed and waits in it; locked up, at eleven June goes out with her lantern, and with a tape playing she comes in for the tin and turns
     * the deck off. Nobody has watched it.
     */
    @GameTest(template="empty",batch="pell_tapes_rounds",timeoutTicks=9000)
    public static void theTapesWaitForAWeekInAndJuneTurnsTheDeckOffOnHerRounds(GameTestHelper h){
        var f=house(h);var p=f.player("pell_tapes_rounds",P);var l=f.level(P);var b=f.base(P);
        f.at(p,P,.5,0,-44);
        var tv=b.offset(PellTapes.TV);var shelf=b.offset(5,1,-93);var door=b.offset(PellSite.SHED_DOOR);
        h.startSequence().thenWaitUntil(()->{for(int i=0;i<PellPeople.COUNT;i++)h.assertTrue(PellPeople.find(l,b,i)!=null,"waiting for "+PellPeople.name(i));h.assertTrue(PellVisit.car(l,b)!=null,"waiting for the visit to begin");})
        .thenExecute(()->{
            h.assertTrue(PellWeek.visit(b)==1,"the first visit");
            f.at(p,P,6.5,0,-88.5);told(p);
            hand(p,door);
            h.assertFalse(PellSite.open(l,b,PellSite.SHED_DOOR),"the shed padlocked");
            hand(p,tv);
            h.assertFalse(PellTapes.playing(b),"the set only what it is");
            hand(p,shelf);
            h.assertTrue(Talk.replies(p).isEmpty(),"the tapes only what they are");
        }).thenIdle(1).thenExecute(()->{
            var said=told(p);
            h.assertTrue(said.contains(PellTexts.PADLOCKED)&&said.contains(PellTexts.thing(SHED_TV,0))&&said.contains(PellTexts.thing(TAPES,0)),"each says what it is: "+said);
            keep(f,p,"FirstVisit");PellPeople.vacate(b);
        })
        .thenWaitUntil(()->weekBegun(h,l,b))
        .thenExecute(()->{
            PellHand.skip(b);
            hand(p,door);
            h.assertTrue(PellSite.open(l,b,PellSite.SHED_DOOR),"a week in, the shed opens");
            f.at(p,P,5.5,0,-91.5);
            PellNight.lockedUp(l,b);PellVisit.hurry(b,PellNight.ELEVEN-6);
        })
        .thenWaitUntil(()->h.assertTrue(PellVisit.step(b)>=PellNight.TO_SHED,"June out with her lantern, on her way to the shed"))
        .thenExecute(()->{
            hand(p,tv);
            h.assertTrue(PellTapes.playing(b),"a tape on");
            told(p);
        })
        .thenWaitUntil(()->h.assertFalse(PellTapes.playing(b),"June comes in for the tin"))
        .thenIdle(1).thenExecute(()->{
            h.assertTrue(told(p).contains(PellTexts.TAPE_JUNE_OFF),"and turns the deck off");
            h.assertTrue(f.own(p,P).getInt("TapesWatched")==0,"nobody has watched it");
            h.assertTrue(PellVisit.step(b)==PellNight.TO_SHED,"on her rounds, at the bench");
        }).thenSucceed();
    }
    @AfterBatch(batch="pell_tapes_rounds") public static void tapesRoundsDone(ServerLevel l){done();}

    // ------------------------------------------------------------------------------------------------ 0.4.567, the shed kept June's
    /**
     * 0.4.567 (owner: "is the player supposed to gain access to the shed at a specific time? as is, they can walk in any time june has it
     * opened and aren't forced to leave despite june's prompts"). On her rounds June opens the shed for the tin. A reader who goes in
     * after her and stays is asked out; when they have had time to go, she sets the tin down by the door, comes in, takes them by the
     * arm, walks them out a step ahead of them and lets go of them at the shed's front, clear of its door. Then she padlocks it with
     * nobody in it, takes the tin up again and goes on to the line, and the shed does not open to a hand.
     */
    @GameTest(template="empty",batch="pell_shed_out",timeoutTicks=9000)
    public static void aReaderWhoStaysInTheShedIsWalkedOutBeforeItIsPadlocked(GameTestHelper h){
        var f=house(h);var p=f.player("pell_shed_out",P);var l=f.level(P);var b=f.base(P);
        f.at(p,P,.5,0,-44);
        long[] asked={0};
        h.startSequence().thenWaitUntil(()->{for(int i=0;i<PellPeople.COUNT;i++)h.assertTrue(PellPeople.find(l,b,i)!=null,"waiting for "+PellPeople.name(i));h.assertTrue(PellVisit.car(l,b)!=null,"waiting for the visit to begin");})
        .thenExecute(()->{PellNight.lockedUp(l,b);f.at(p,P,8.5,0,-82.5);PellVisit.hurry(b,PellNight.ELEVEN-6);})
        .thenWaitUntil(()->h.assertTrue(PellVisit.step(b)>=PellNight.TO_SHED&&PellSite.open(l,b,PellSite.SHED_DOOR),"June opens the shed for the tin"))
        // In after her, to the corner by the tapes, and there they stay.
        .thenExecute(()->{f.at(p,P,5.5,0,-91.5);told(p);})
        .thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_OUT_OF_SHED),"June asks them out"))
        .thenExecute(()->{
            asked[0]=l.getGameTime();
            h.assertFalse(PellNight.showingOut(b),"she asks first");
            h.assertTrue(who(h,l,b,PellPeople.JUNE).getMainHandItem().is(PellRegistry.PAINT_TIN.get()),"the tin in her hand");
        })
        .thenWaitUntil(()->h.assertTrue(PellNight.showingOut(b),"when they have had time to go, she comes for them"))
        .thenExecute(()->{
            h.assertTrue(l.getGameTime()-asked[0]>=PellNight.SHOW_OUT-5,"not before: "+(l.getGameTime()-asked[0]));
            h.assertTrue(thing(l,b,PellNight.TIN_DOWN.getX(),0,PellNight.TIN_DOWN.getZ(),TIN,0),"the tin set down by the door");
            h.assertTrue(who(h,l,b,PellPeople.JUNE).getMainHandItem().isEmpty(),"a hand free");
        })
        .thenWaitUntil(()->h.assertTrue(PellNight.heldOut(b),"she has hold of their arm"))
        .thenExecute(()->h.assertTrue(who(h,l,b,PellPeople.JUNE).gesture()==PellPeople.SHOWING,"her hand back to theirs"))
        .thenWaitUntil(()->h.assertFalse(PellNight.showingOut(b),"walked out and let go of"))
        .thenExecute(()->{
            var r=p.position().subtract(b.getX(),b.getY(),b.getZ());
            h.assertFalse(PellSite.inShed(r),"out of the shed: "+r);
            h.assertTrue(r.subtract(6.5,0,-89.5).horizontalDistance()>1.1,"and clear of its door: "+r);
            var said=told(p);
            h.assertTrue(said.contains(PellTexts.SHOWN_OUT)&&said.contains(PellTexts.LET_GO),"told so: "+said);
            h.assertTrue(who(h,l,b,PellPeople.JUNE).gesture()!=PellPeople.SHOWING,"her hand to herself again");
        })
        .thenWaitUntil(()->h.assertTrue(PellVisit.step(b)>=PellNight.TO_LINE,"and on to the line"))
        .thenExecute(()->{
            h.assertFalse(PellSite.open(l,b,PellSite.SHED_DOOR),"the shed shut and padlocked, with nobody in it");
            h.assertFalse(PellThing.is(l.getBlockState(b.offset(PellNight.TIN_DOWN)),TIN),"the tin taken up again");
            h.assertTrue(who(h,l,b,PellPeople.JUNE).getMainHandItem().is(PellRegistry.PAINT_TIN.get()),"in her hand");
            hand(p,b.offset(PellSite.SHED_DOOR));
        }).thenIdle(1).thenExecute(()->{
            h.assertFalse(PellSite.open(l,b,PellSite.SHED_DOOR),"and it does not open to a hand");
            h.assertTrue(told(p).contains(PellTexts.PADLOCKED),"padlocked");
        }).thenSucceed();
    }
    @AfterBatch(batch="pell_shed_out") public static void shedOutDone(ServerLevel l){done();}

    // ------------------------------------------------------------------------------------------------ 0.4.538, the last night begins
    /** Waiting for the last night to begin: June, Tilly and Callum there, and the house in its evening. */
    private static void lastBegun(GameTestHelper h,ServerLevel l,BlockPos b){
        for(int i:new int[]{PellPeople.JUNE,PellPeople.TILLY,PellPeople.CALLUM})h.assertTrue(PellPeople.find(l,b,i)!=null,"waiting for "+PellPeople.name(i));
        h.assertTrue(PellWeek.visit(b)==PellWeek.LAST&&PellVisit.step(b)==PellVisit.TELLY,"waiting for the last night to begin");
    }
    private static int stageAt(ServerLevel l,BlockPos b,BlockPos cell){return l.getBlockState(b.offset(cell)).getValue(PellLine.STAGE);}
    /** Where to look to hold the brush to the top of a cell of the line. */
    private static net.minecraft.world.phys.Vec3 top(BlockPos b,BlockPos cell){return new net.minecraft.world.phys.Vec3(b.getX()+cell.getX()+.5,b.getY()+cell.getY()+1,b.getZ()+cell.getZ()+.5);}
    private static final BlockPos FRIDGE_AT=new BlockPos(-13,0,-71);
    /**
     * 0.4.538, the last night begins (docs/PELL_HOUSE_LAST_NIGHT_0_4_538.md), with two readers who have both had the first two visits.
     * The house begins on its third visit in the evening, without Gail: June at her chair with the television on, Callum out on the
     * line in the middle of the thin stretch behind the shed, facing the house. The pool is lit from under the water and its lane rope
     * is gone; the line has been gone over again all the way round; the knife block is gone from the bench and put away in a cupboard,
     * every slot empty, and Callum's drawer has a space where the knife lay; the calendar's circle is tonight. The evening holds while
     * nobody comes in. One reader comes in and June says it will be an early night; the other, at the far end of the house, is told,
     * and the hour goes on. A hand on the fridge gives each reader a plate of dinner, once. A hand on the bucket in the laundry takes
     * it, and a bucket that nobody at the house has is back in its place within a second. Locked up, the night goes on to midnight
     * without June's rounds, and at midnight (0.4.539) she goes for Tilly; Callum stays out on the line.
     */
    @GameTest(template="empty",batch="pell_last",timeoutTicks=6000)
    public static void theLastNightBeginsWithThePoolLitTheKnivesPutAwayAndCallumOnTheLine(GameTestHelper h){
        var f=house(h);var p=f.player("pell_last_reader",P);var q=f.player("pell_last_other",P);var l=f.level(P);var b=f.base(P);
        for(var r:new ServerPlayer[]{p,q}){keep(f,r,"FirstVisit");keep(f,r,"SecondVisit");}
        f.at(p,P,.5,0,-44);f.at(q,P,-6.5,0,-36);
        long[] mark={0};
        h.startSequence().thenWaitUntil(()->lastBegun(h,l,b))
        .thenExecute(()->{
            h.assertTrue(PellVisit.car(l,b)==null&&PellPeople.find(l,b,PellPeople.GAIL)==null,"no Gail and no car");
            var c=who(h,l,b,PellPeople.CALLUM);
            h.assertTrue(c.blockPosition().subtract(b).equals(new BlockPos(PellLastNight.STRIP_X,0,PellSite.LINE_N))&&PellSite.thin(new BlockPos(PellLastNight.STRIP_X,0,PellSite.LINE_N)),"Callum out on the line, in the middle of the thin stretch behind the shed: "+c.position());
            h.assertTrue(Math.abs(net.minecraft.util.Mth.wrapDegrees(c.getYRot()))<1&&!PellVisit.asleep(b,PellPeople.CALLUM),"standing, facing the house: "+c.getYRot());
            // 0.4.568: Tilly's hair as long as Nessa's.
            var tilly=who(h,l,b,PellPeople.TILLY);
            h.assertTrue(PellPeople.hair(tilly.marks())==PellPeople.HAIR_LONGEST,"on the last night Tilly's hair is at its longest: "+PellPeople.hair(tilly.marks()));
            // The pool lit from under the water, and no rope across it.
            h.assertTrue(PellLastNight.lit(l,b)&&l.getBlockState(b.offset(-7,-1,-86)).is(Blocks.WATER)&&PellWeek.rope(l,b).isEmpty(),"the pool full and lit, its lane rope gone");
            // The line gone over again, all the way round.
            for(var at:PellSite.route()){var st=l.getBlockState(b.offset(at.getX(),-1,at.getZ()));h.assertTrue(PellLine.unbroken(st)&&st.getValue(PellLine.STAGE)==PellLine.LAST,"the line gone over again at "+at+": "+st);}
            // Every knife put away.
            h.assertTrue(thing(l,b,-11,0,-71,KNIFE_BLOCK,2)&&thing(l,b,-11,2,-71,CUPBOARD_UPPER,1),"the knife block gone from the bench, and put away in the cupboard above it");
            for(var at:PellWeek.CUPBOARD.subList(1,PellWeek.CUPBOARD.size()))h.assertTrue(thing(l,b,at.getX(),at.getY(),at.getZ(),CUPBOARD_UPPER,0),"the other cupboards shut: "+at);
            h.assertTrue(thing(l,b,-6,0,-61,CUTLERY,2),"a space in Callum's drawer where the knife lay");
            h.assertTrue(thing(l,b,5,1,-92,SHED_CALENDAR,2),"the calendar's circle is tonight");
            mark[0]=PellVisit.hour(b);
        }).thenIdle(20).thenExecute(()->{
            h.assertTrue(PellVisit.watching(b)&&PellVisit.tvOn(l,b),"June in her chair with the television on");
            h.assertTrue(PellVisit.hour(b)==mark[0]&&!PellWeek.welcomed(b),"while nobody comes in, the evening holds: "+PellVisit.hour(b));
            // One reader into the living room; the other to the far end of the house, out of earshot of her chair.
            told(q);f.at(q,P,8.5,0,-60.5);f.at(p,P,1.5,0,-63.5);
        }).thenWaitUntil(()->h.assertTrue(PellWeek.welcomed(b),"June says it will be an early night"))
        .thenIdle(1).thenExecute(()->{
            h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_LAST_WELCOME),"from her chair: "+said(who(h,l,b,PellPeople.JUNE)));
            h.assertTrue(told(q).contains(PellTexts.LAST_WELCOME_FAR),"heard from the far end of the house");
            mark[0]=PellVisit.hour(b);
        }).thenIdle(20).thenExecute(()->{
            h.assertTrue(PellVisit.hour(b)>=mark[0]+15,"the evening goes on: "+mark[0]+" to "+PellVisit.hour(b));
            // The fridge, twice for one reader and once for the other; the bucket, by both.
            told(p);told(q);
            f.click(p,P,FRIDGE_AT);f.click(p,P,FRIDGE_AT);f.click(q,P,FRIDGE_AT);
            f.click(p,P,PellScrub.BUCKET);f.click(q,P,PellScrub.BUCKET);
        }).thenIdle(1).thenExecute(()->{
            var mine=told(p);
            h.assertTrue(p.getInventory().countItem(PellRegistry.DINNER.get())==1&&mine.contains(PellTexts.FRIDGE_PLATE)&&f.own(p,P).getBoolean("TookPlate"),"a plate of dinner under cling film: "+mine);
            h.assertTrue(mine.contains(PellTexts.FRIDGE_EMPTY),"and nothing more for the same reader");
            h.assertTrue(q.getInventory().countItem(PellRegistry.DINNER.get())==1,"the other reader has one of their own");
            h.assertTrue(p.getInventory().countItem(PellRegistry.SCRUB.get())==1&&mine.contains(PellTexts.BUCKET_TAKEN)&&f.own(p,P).getBoolean("TookBucket"),"the bucket and its brush taken");
            h.assertTrue(l.getBlockState(b.offset(PellScrub.BUCKET)).isAir()&&q.getInventory().countItem(PellRegistry.SCRUB.get())==0,"gone from the laundry: there is one bucket");
        }).thenIdle(PellScrub.KEEP+5).thenExecute(()->{
            h.assertTrue(l.getBlockState(b.offset(PellScrub.BUCKET)).isAir()&&p.getInventory().countItem(PellRegistry.SCRUB.get())==1,"while a reader has it, it stays with them");
            p.getInventory().clearContent();
        }).thenWaitUntil(()->h.assertTrue(thing(l,b,PellScrub.BUCKET.getX(),PellScrub.BUCKET.getY(),PellScrub.BUCKET.getZ(),BUCKET,0),"a bucket nobody has is back in the laundry"))
        .thenExecute(()->{
            // Locked up, the night goes on to midnight without June's rounds; 0.4.539: at midnight she goes for Tilly (PellRite).
            PellLastNight.lockedUp(l,b);
        }).thenWaitUntil(()->h.assertTrue(PellVisit.hour(b)==PellNight.TWELVE,"midnight: "+PellVisit.hour(b)))
        .thenIdle(60).thenExecute(()->{
            h.assertTrue(PellVisit.hour(b)==PellNight.TWELVE&&PellVisit.step(b)==PellVisit.NIGHT,"the hour holds at midnight: "+PellVisit.hour(b));
            var june=who(h,l,b,PellPeople.JUNE);
            h.assertTrue(june.getOffhandItem().isEmpty()&&PellRite.step(b)>=PellRite.FETCH,"June does not go out on her rounds: at midnight she goes for Tilly");
            // 0.4.548: nobody on Gail's file twice, June does not get up in the night, and the last photograph stays on the wall.
            h.assertTrue(PellMark.stage(b)==PellMark.DONE&&PellMark.marked(b)==null&&thing(l,b,PellMark.PHOTO.getX(),PellMark.PHOTO.getY(),PellMark.PHOTO.getZ(),PHOTOS,3),"nobody framed for the photograph: "+PellMark.stage(b));
            var c=who(h,l,b,PellPeople.CALLUM);
            h.assertTrue(at(c,b,PellLastNight.ON_LINE.x,PellLastNight.ON_LINE.z,.3)&&!PellVisit.asleep(b,PellPeople.CALLUM),"Callum still out on the line");
        }).thenSucceed();
    }
    @AfterBatch(batch="pell_last") public static void lastDone(ServerLevel l){done();}

    /**
     * 0.4.538, breaking the line on the last night, with two readers. One takes a plate from the fridge and the bucket from the
     * laundry. A hand on Callum on the line gets that he does not move. Held to a cell of the thin stretch behind the shed, the brush
     * brings Callum onto that cell, once said, and nothing comes off however long it is held there. The plate held out to him: he
     * takes it, sits down on the grass a step inside the line and eats it; a hand on him then gets that he eats, and more food that
     * he has already what he was given. The stretch free, the brush takes the paint half away and then out in two seconds each; the
     * brush comes away from a cell scrubbed out. A second cell scrubbed out beside the first breaks the line: Callum looks up and says
     * his name, heard by both readers. He finishes eating and sits on.
     */
    @GameTest(template="empty",batch="pell_last_line",timeoutTicks=6000)
    public static void callumKeepsTheThinStretchUntilHeIsFedAndWhenTheLineBreaksHeSaysHisName(GameTestHelper h){
        var f=house(h);var p=f.player("pell_line_scrubber",P);var q=f.player("pell_line_watcher",P);var l=f.level(P);var b=f.base(P);
        for(var r:new ServerPlayer[]{p,q}){keep(f,r,"FirstVisit");keep(f,r,"SecondVisit");}
        f.at(p,P,.5,0,-44);f.at(q,P,-6.5,0,-36);
        var first=new BlockPos(7,-1,PellSite.LINE_N);var second=new BlockPos(8,-1,PellSite.LINE_N);
        int[] plate={-1},brush={-1};
        h.startSequence().thenWaitUntil(()->lastBegun(h,l,b))
        .thenExecute(()->{
            f.click(p,P,FRIDGE_AT);f.click(p,P,PellScrub.BUCKET);
            var inv=p.getInventory();
            for(int i=0;i<9;i++){if(inv.getItem(i).is(PellRegistry.DINNER.get()))plate[0]=i;if(inv.getItem(i).is(PellRegistry.SCRUB.get()))brush[0]=i;}
            h.assertTrue(plate[0]>=0&&brush[0]>=0,"a plate and the bucket in hand's reach: "+inv.items);
            inv.selected=brush[0];
            h.assertTrue(PellScrub.holding(p),"the bucket in hand");
            // Behind the shed, inside the line; the other reader by the shed's corner.
            f.at(p,P,7.5,0,-95.5);f.at(q,P,3.5,0,-95.5);told(p);
            LiteraryVignettes.talk(p,who(h,l,b,PellPeople.CALLUM));
        }).thenIdle(1).thenExecute(()->{
            h.assertTrue(told(p).contains(PellTexts.CALLUM_ON_LINE[0]),"a hand on him: he does not move");
            lookAt(p,top(b,first));
        }).thenWaitUntil(()->{
            PellScrub.scrubbing(p);
            h.assertTrue(f.own(p,P).getBoolean("CallumStood")&&at(who(h,l,b,PellPeople.CALLUM),b,first.getX()+.5,first.getZ()+.5,.3),"Callum steps onto the cell the brush is on");
        }).thenExecuteFor(60,()->PellScrub.scrubbing(p))
        .thenExecute(()->{
            h.assertTrue(stageAt(l,b,first)==PellLine.LAST,"while he stands on it, nothing comes off");
            h.assertTrue(told(p).stream().filter(PellTexts.CALLUM_STANDS::equals).count()==1,"told once");
            // The plate held out to him.
            p.getInventory().selected=plate[0];
            LiteraryVignettes.talk(p,who(h,l,b,PellPeople.CALLUM));
        }).thenIdle(1).thenExecute(()->{
            var c=who(h,l,b,PellPeople.CALLUM);
            h.assertTrue(told(p).contains(PellTexts.CALLUM_TAKES)&&PellLastNight.fed(b)&&f.own(p,P).getBoolean("FedCallum"),"he takes it");
            h.assertTrue(p.getInventory().countItem(PellRegistry.DINNER.get())==0&&c.getOffhandItem().is(PellRegistry.DINNER.get()),"from the reader's hand into his");
        }).thenWaitUntil(()->{
            var c=who(h,l,b,PellPeople.CALLUM);
            h.assertTrue(c.gesture()==PellPeople.EATING&&at(c,b,PellLastNight.SITS.x,PellLastNight.SITS.z,.3),"sat down on the grass a step inside the line, eating");
        }).thenExecute(()->LiteraryVignettes.talk(p,who(h,l,b,PellPeople.CALLUM)))
        .thenIdle(1).thenExecute(()->{
            h.assertTrue(told(p).contains(PellTexts.CALLUM_EATING[0]),"a hand on him: he eats");
            p.getInventory().setItem(p.getInventory().selected,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BREAD));
            LiteraryVignettes.talk(p,who(h,l,b,PellPeople.CALLUM));
        }).thenIdle(1).thenExecute(()->{
            h.assertTrue(told(p).contains(PellTexts.CALLUM_HAS_EATEN)&&p.getMainHandItem().getCount()==1,"he has what he was given");
            p.getInventory().selected=brush[0];lookAt(p,top(b,first));
        }).thenWaitUntil(()->{PellScrub.scrubbing(p);h.assertTrue(stageAt(l,b,first)==PellLine.QUARTER_SCRUBBED,"the paint a quarter away");})
        .thenIdle(1).thenExecute(()->h.assertTrue(told(p).contains(PellTexts.SCRUB_TAKES),"the first of it up, and the House says so"))
        .thenWaitUntil(()->{PellScrub.scrubbing(p);h.assertTrue(stageAt(l,b,first)==PellLine.HALF_SCRUBBED,"the paint half away");})
        .thenWaitUntil(()->{PellScrub.scrubbing(p);h.assertTrue(stageAt(l,b,first)==PellLine.GAP,"and out");})
        .thenExecute(()->{
            h.assertFalse(told(p).contains(PellTexts.SCRUB_TAKES),"said once");
            h.assertFalse(PellScrub.scrubbing(p),"the brush comes away from a cell scrubbed out");
            h.assertFalse(PellScrub.broken(l,b)||PellLastNight.said(b),"one cell out is not a gap");
            lookAt(p,top(b,second));
        }).thenWaitUntil(()->{PellScrub.scrubbing(p);h.assertTrue(stageAt(l,b,second)==PellLine.GAP,"the cell beside it scrubbed out");})
        .thenIdle(1).thenExecute(()->{
            var c=who(h,l,b,PellPeople.CALLUM);
            h.assertTrue(PellScrub.broken(l,b)&&PellLastNight.said(b),"the line broken");
            h.assertTrue(said(c).equals(PellTexts.CALLUM_NAME)&&c.getXRot()<-20,"he looks up and says his name: "+said(c)+" "+c.getXRot());
            for(var r:new ServerPlayer[]{p,q})h.assertTrue(f.own(r,P).getBoolean("HeardCallum"),"heard by both readers");
            var own=f.own(p,P);h.assertTrue(own.getInt("Scrubbed")==4&&own.getBoolean("ScrubbedOut"),"the scrubbing kept: "+own);
        }).thenWaitUntil(()->{
            var c=who(h,l,b,PellPeople.CALLUM);
            h.assertTrue(c.gesture()==PellPeople.SITTING&&c.getOffhandItem().isEmpty(),"he has eaten it, and sits on");
        }).thenExecute(()->h.assertTrue(PellLastNight.said(b)&&who(h,l,b,PellPeople.CALLUM).getXRot()<-20,"still looking up")).thenSucceed();
    }
    @AfterBatch(batch="pell_last_line") public static void lastLineDone(ServerLevel l){done();}

    /**
     * 0.4.538, the bucket and the brush on a visit before the last, with one reader a week in. Food held out to Callum at the end of the
     * hall: he looks at it and does not take it. A thick cell of the line scrubbed out, the one beside it half away, five seconds each
     * stage. The night goes on asleep in the garage; by first light June has painted over both, and the line is whole.
     */
    @GameTest(template="empty",batch="pell_scrub_week",timeoutTicks=12000)
    public static void beforeTheLastNightCallumWillNotEatAndJuneHasPaintedOverTheScrubbingByFirstLight(GameTestHelper h){
        var f=house(h);var p=f.player("pell_scrub_reader",P);var l=f.level(P);var b=f.base(P);
        keep(f,p,"FirstVisit");
        f.at(p,P,.5,0,-44);
        var out=new BlockPos(10,-1,PellSite.LINE_N);var half=new BlockPos(11,-1,PellSite.LINE_N);
        h.startSequence().thenWaitUntil(()->weekBegun(h,l,b))
        .thenExecute(()->{
            PellHand.skip(b);
            f.at(p,P,-8.5,0,-65.5);told(p);
            p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BREAD));
            LiteraryVignettes.talk(p,who(h,l,b,PellPeople.CALLUM));
        }).thenIdle(1).thenExecute(()->{
            var c=who(h,l,b,PellPeople.CALLUM);
            h.assertTrue(told(p).contains(PellTexts.CALLUM_WONT_TAKE)&&p.getMainHandItem().is(net.minecraft.world.item.Items.BREAD)&&c.getOffhandItem().isEmpty(),"he looks at it and does not take it");
            h.assertFalse(PellLastNight.fed(b),"and is not fed");
            p.getInventory().clearContent();
            f.click(p,P,PellScrub.BUCKET);
            h.assertTrue(PellScrub.holding(p),"the bucket in hand");
            f.at(p,P,10.5,0,-95.5);lookAt(p,top(b,out));
            h.assertTrue(!PellSite.thin(out)&&!PellSite.thin(half),"a hand's width of line, not the thin stretch");
        }).thenExecuteFor(PellScrub.quarter(false)-5,()->PellScrub.scrubbing(p))
        .thenExecute(()->h.assertTrue(stageAt(l,b,out)==PellLine.SECOND,"two and a half seconds to a quarter of it where the line is wide"))
        .thenWaitUntil(()->{PellScrub.scrubbing(p);h.assertTrue(stageAt(l,b,out)==PellLine.QUARTER_SCRUBBED,"a quarter of it up");})
        // 0.4.567: the brush taken off the cell a moment loses nothing done; the quarter stays in the paint.
        .thenExecute(()->lookAt(p,top(b,half)))
        .thenExecuteFor(PellScrub.quarter(false)-5,()->PellScrub.scrubbing(p))
        .thenExecute(()->{
            h.assertTrue(stageAt(l,b,out)==PellLine.QUARTER_SCRUBBED&&stageAt(l,b,half)==PellLine.SECOND,"the quarter done stays done");
            lookAt(p,top(b,out));
        })
        .thenWaitUntil(()->{PellScrub.scrubbing(p);h.assertTrue(stageAt(l,b,out)==PellLine.HALF_SCRUBBED,"half away");})
        .thenWaitUntil(()->{PellScrub.scrubbing(p);h.assertTrue(stageAt(l,b,out)==PellLine.MOSTLY_SCRUBBED,"three quarters away");})
        .thenWaitUntil(()->{PellScrub.scrubbing(p);h.assertTrue(stageAt(l,b,out)==PellLine.GAP,"and out");})
        .thenExecute(()->lookAt(p,top(b,half)))
        .thenWaitUntil(()->{PellScrub.scrubbing(p);h.assertTrue(stageAt(l,b,half)==PellLine.HALF_SCRUBBED,"the next half away");})
        .thenExecute(()->{
            h.assertFalse(PellScrub.broken(l,b),"one cell out and one half is not a gap");
            // The night, asleep in the garage, without the knife.
            PellKnife.skip(b);PellMorning.smallHours(l,b);
            f.at(p,P,14.5,0,-89.5);hand(p,b.offset(CAMP_BED_AT));
            h.assertTrue(p.isSleeping(),"asleep in the garage");
        }).thenWaitUntil(()->h.assertTrue(PellMorning.dawned(b),"first light"))
        .thenExecute(()->{
            for(var cell:List.of(out,half)){var st=l.getBlockState(b.offset(cell));h.assertTrue(PellLine.unbroken(st)&&st.getValue(PellLine.STAGE)==PellLine.SECOND,"June has painted over it: "+cell+" "+st);}
            h.assertFalse(PellScrub.broken(l,b),"the line whole");
        }).thenSucceed();
    }
    @AfterBatch(batch="pell_scrub_week") public static void scrubWeekDone(ServerLevel l){done();}

    // ------------------------------------------------------------------------------------------------ 0.4.539, midnight in the pool
    private static boolean said(BlockPos b,String who,String line){return PellRite.lines(b).contains(who+": "+line);}
    private static long times(BlockPos b,String who,String line){return PellRite.lines(b).stream().filter((who+": "+line)::equals).count();}
    /** The line's cell at (x, z) scrubbed out, as the brush leaves it. */
    private static void scrubbedOut(ServerLevel l,BlockPos b,int x,int z){var at=b.offset(x,-1,z);var s=l.getBlockState(at);if(PellLine.is(s))l.setBlock(at,s.setValue(PellLine.STAGE,PellLine.GAP),3);}
    /**
     * 0.4.539, midnight in the pool (docs/PELL_HOUSE_POOL_0_4_539.md). Two readers who have had the first two visits; the last night locked
     * up a little before midnight, one reader asleep on the camp bed in the garage and the other out in the yard at the pool's fence. At
     * midnight June comes out of her room in her robe, wakes Tilly in Nessa's bed by Nessa's name, and Tilly gives her own; June takes her
     * hand and leads her out by the sliding door and the pool's gate, both unlocked and left open, takes off her robe at the chair and goes
     * into the shallow end with her. The splashing wakes the reader asleep. June stands behind Tilly with her hands on her shoulders and
     * calls her Nessa, and Tilly answers with her own name; June presses her under, lets her up and asks again. Nobody takes her hand, and
     * in the end she answers to it, and both readers hear her. June takes her back to Nessa's bed, putting her robe on at the chair on the
     * way, and sits on the edge of the bed beside her.
     */
    @GameTest(template="empty",batch="pell_rite",timeoutTicks=9000)
    public static void atMidnightJuneTakesTillyIntoThePoolAndCallsHerNessaUntilSheAnswers(GameTestHelper h){
        var f=house(h);var p=f.player("pell_rite_watcher",P);var q=f.player("pell_rite_sleeper",P);var l=f.level(P);var b=f.base(P);
        for(var r:new ServerPlayer[]{p,q}){keep(f,r,"FirstVisit");keep(f,r,"SecondVisit");}
        f.at(p,P,.5,0,-44);f.at(q,P,-6.5,0,-36);
        var bed=b.offset(CAMP_BED_AT);var chair=PellSwim.CHAIR;
        h.startSequence().thenWaitUntil(()->lastBegun(h,l,b))
        .thenExecute(()->{
            PellLastNight.lockedUp(l,b);
            // One reader out at the pool's fence, the other asleep in the garage.
            f.at(p,P,-6.5,0,-74.6);f.at(q,P,14.5,0,-89.5);hand(q,bed);
            h.assertTrue(q.isSleeping(),"one reader asleep on the camp bed");
            told(p);told(q);
        }).thenWaitUntil(()->h.assertTrue(PellRite.step(b)==PellRite.FETCH,"midnight: June comes out of her room"))
        .thenExecute(()->h.assertTrue(who(h,l,b,PellPeople.JUNE).gesture()==PellPeople.ROBE,"in her robe"))
        .thenWaitUntil(()->h.assertTrue(PellRite.step(b)==PellRite.LEAD,"at Nessa's bed she wakes Tilly: "+PellRite.lines(b)))
        .thenExecute(()->{
            var tilly=who(h,l,b,PellPeople.TILLY);
            h.assertTrue(said(b,"June",PellTexts.JUNE_RITE_WAKE)&&said(b,"Tilly",PellTexts.TILLY_RITE_NAME)&&said(b,"June",PellTexts.JUNE_RITE_HAND),"by Nessa's name, and Tilly gives her own: "+PellRite.lines(b));
            h.assertTrue(!PellVisit.asleep(b,PellPeople.TILLY)&&tilly.gesture()==PellPeople.LED&&tilly.getMainHandItem().isEmpty(),"up, her hand out for June's, her cane left by the bed");
            h.assertTrue(who(h,l,b,PellPeople.JUNE).gesture()==PellPeople.LEADS,"June leads her by the hand");
        }).thenWaitUntil(()->h.assertTrue(PellRite.step(b)==PellRite.WATER,"down to the pool and in: June at "+who(h,l,b,PellPeople.JUNE).position().subtract(b.getX(),b.getY(),b.getZ())+", Tilly at "+who(h,l,b,PellPeople.TILLY).position().subtract(b.getX(),b.getY(),b.getZ())))
        .thenIdle(1).thenExecute(()->{
            var june=who(h,l,b,PellPeople.JUNE);var tilly=who(h,l,b,PellPeople.TILLY);
            h.assertTrue(PellSite.open(l,b,PellSite.SLIDER)&&!PellVisit.locked(b,PellSite.SLIDER),"the sliding door unlocked and left open");
            h.assertTrue(PellSwim.gateStage(l,b)==2,"the pool's gate unlocked and left open");
            h.assertTrue(thing(l,b,chair.getX(),chair.getY(),chair.getZ(),PellThing.Kind.POOL_CHAIR,1),"her robe over the chair");
            h.assertTrue(PellRite.held(b)&&june.gesture()==PellPeople.HOLD&&june.isNoGravity()&&june.getY()<b.getY()-.5,"June in the shallow end, holding her");
            h.assertTrue(tilly.isNoGravity()&&tilly.getY()<b.getY()-.5&&tilly.getZ()<june.getZ(),"Tilly in front of her in the water");
            h.assertFalse(q.isSleeping(),"the splashing wakes the reader asleep");
            h.assertTrue(told(q).contains(PellTexts.WOKEN_BY_POOL)&&f.own(q,P).getBoolean("WokenByThePool"),"told what woke them, and keeps that it did");
            lookAt(p,tilly.getEyePosition());
        }).thenIdle(10).thenExecute(()->h.assertTrue(f.own(p,P).getBoolean("SawRite"),"the reader at the fence sees it"))
        .thenWaitUntil(()->h.assertTrue(said(b,"June",PellTexts.RITE_CALLS[0])&&times(b,"Tilly",PellTexts.TILLY_RITE_NAME)>=2,"June calls her Nessa, and she answers with her own name: "+PellRite.lines(b)))
        .thenWaitUntil(()->h.assertTrue(PellRite.under(b),"June presses her under"))
        .thenIdle(12).thenExecute(()->{
            h.assertTrue(who(h,l,b,PellPeople.JUNE).gesture()==PellPeople.PRESS,"her arms down into it");
            h.assertTrue(who(h,l,b,PellPeople.TILLY).getEyeY()<b.getY()+PellWeek.WATERLINE,"Tilly under the water");
        }).thenWaitUntil(()->h.assertTrue(!PellRite.under(b)&&said(b,"June",PellTexts.JUNE_AFTER_UNDER[0]),"let up into the air, and asked again: "+PellRite.lines(b)))
        .thenExecute(()->{
            h.assertTrue(who(h,l,b,PellPeople.TILLY).getEyeY()>b.getY()+PellWeek.WATERLINE,"her head out of the water");
            // The woken reader out to the pool too, on the deck at the shallow end.
            f.at(q,P,-9.5,0,-78.5);
            PellRite.nearlyAnswers(b);
        }).thenWaitUntil(()->h.assertTrue(PellRite.step(b)>=PellRite.ANSWERED,"held out long enough, she answers"))
        .thenExecute(()->{
            var tilly=who(h,l,b,PellPeople.TILLY);
            h.assertTrue(said(b,"Tilly",PellTexts.TILLY_ANSWERS),"she answers to it: "+PellRite.lines(b));
            h.assertTrue(f.own(p,P).getBoolean("HeardTillyAnswer")&&f.own(q,P).getBoolean("HeardTillyAnswer"),"and both readers hear her: "+f.own(p,P).getBoolean("HeardTillyAnswer")+" at "+p.distanceTo(tilly)+", "+f.own(q,P).getBoolean("HeardTillyAnswer")+" at "+q.distanceTo(tilly)+(p.isSleeping()||q.isSleeping()?", one asleep":""));
        }).thenWaitUntil(()->h.assertTrue(PellRite.step(b)==PellRite.KEPT,"June takes her back to Nessa's bed: June at "+who(h,l,b,PellPeople.JUNE).position().subtract(b.getX(),b.getY(),b.getZ())))
        .thenExecute(()->{
            var june=who(h,l,b,PellPeople.JUNE);var tilly=who(h,l,b,PellPeople.TILLY);
            h.assertTrue(said(b,"June",PellTexts.JUNE_ANSWERED),"\"There's my girl.\"");
            h.assertTrue(PellVisit.asleep(b,PellPeople.TILLY)&&at(tilly,b,PellVisit.TILLY_SLEEPS.x,PellVisit.TILLY_SLEEPS.z,.3),"Tilly in Nessa's bed again");
            h.assertTrue(PellVisit.perched(b,PellPeople.JUNE)==PellVisit.JUNE_BED&&!june.isNoGravity()&&june.gesture()==PellPeople.ROBE,"June in her robe, sat on its edge beside her");
            h.assertTrue(thing(l,b,chair.getX(),chair.getY(),chair.getZ(),PellThing.Kind.POOL_CHAIR,0),"her robe off the chair");
        }).thenSucceed();
    }
    @AfterBatch(batch="pell_rite") public static void riteDone(ServerLevel l){done();}

    /**
     * 0.4.539, getting Tilly out. Two readers who have had the first two visits; the last night locked up, a gap scrubbed in the line on
     * its west side by the pool, and June already in the shallow end holding Tilly. One reader gets in beside her and takes her hand: she
     * holds on and asks if that is them, and June's hands come away; the other reader's hand finds she has somebody's already. The first
     * goes off across the yard: her hand slips out of theirs and she stands in the water, and in a few seconds June wades to her and takes
     * her back. The other takes her hand and leads her through the water to its edge, where she is lifted out, and across the deck to the
     * line. Where it is whole she stops short of it (her bare foot finds the paint); with the hand further along she goes along the paint to
     * the gap and steps through it. She is out, the reader who brought her keeps that they did, and June stops calling.
     */
    @GameTest(template="empty",batch="pell_rite_out",timeoutTicks=6000)
    public static void aHandInTheWaterTakesTillyAndSheFollowsItThroughTheGapButNeverOverThePaint(GameTestHelper h){
        var f=house(h);var p=f.player("pell_rite_first_hand",P);var q=f.player("pell_rite_second_hand",P);var l=f.level(P);var b=f.base(P);
        for(var r:new ServerPlayer[]{p,q}){keep(f,r,"FirstVisit");keep(f,r,"SecondVisit");}
        f.at(p,P,.5,0,-44);f.at(q,P,-6.5,0,-36);
        java.util.function.Supplier<String> where=()->"Tilly at "+who(h,l,b,PellPeople.TILLY).position().subtract(b.getX(),b.getY(),b.getZ());
        h.startSequence().thenWaitUntil(()->lastBegun(h,l,b))
        .thenExecute(()->{
            PellLastNight.lockedUp(l,b);PellRite.inWater(l,b);
            scrubbedOut(l,b,PellSite.LINE_W,-83);scrubbedOut(l,b,PellSite.LINE_W,-82);
            h.assertTrue(PellRite.held(b),"June in the shallow end, holding Tilly");
            // The first reader in the water beside her; the other on the deck at the shallow end.
            var tilly=who(h,l,b,PellPeople.TILLY);
            f.at(p,P,tilly.getX()-b.getX()+1.2,-1,tilly.getZ()-b.getZ());f.at(q,P,-8.5,0,-78.5);
            told(p);told(q);
            LiteraryVignettes.talk(p,tilly);
        }).thenIdle(1).thenExecute(()->{
            h.assertTrue(p.getUUID().equals(PellRite.leader(b))&&!PellRite.held(b),"a hand in the water takes hers");
            h.assertTrue(told(p).contains(PellTexts.TILLY_TAKES)&&f.own(p,P).getBoolean("TookTillysHand"),"she holds on");
            h.assertTrue(said(b,"Tilly",PellTexts.TILLY_WHO)&&who(h,l,b,PellPeople.TILLY).gesture()==PellPeople.LED,"and asks if that is them");
            h.assertTrue(who(h,l,b,PellPeople.JUNE).gesture()==PellPeople.WADE,"June's hands come away");
            LiteraryVignettes.talk(q,who(h,l,b,PellPeople.TILLY));
        }).thenIdle(1).thenExecute(()->{
            h.assertTrue(told(q).contains(PellTexts.TILLY_HAS_A_HAND)&&p.getUUID().equals(PellRite.leader(b)),"the other reader's hand: she has somebody's already");
            // The first reader goes off across the yard.
            f.at(p,P,-6.5,0,-70.5);
        }).thenIdle(2).thenExecute(()->h.assertTrue(PellRite.leader(b)==null&&told(p).contains(PellTexts.TILLY_LET_GO),"her hand slips out of theirs"))
        .thenWaitUntil(()->h.assertTrue(PellRite.held(b),"in a few seconds June wades to her and takes her back"))
        .thenWaitUntil(()->h.assertTrue(said(b,"June",PellTexts.JUNE_TAKES_BACK),"June takes her back, and says so as soon as she can: "+PellRite.lines(b)))
        .thenExecute(()->{
            h.assertTrue(PellRite.held(b)&&PellRite.leader(b)==null,"still in June's hands");
            var tilly=who(h,l,b,PellPeople.TILLY);
            f.at(q,P,tilly.getX()-b.getX()+1.2,-1,tilly.getZ()-b.getZ());told(q);
            LiteraryVignettes.talk(q,tilly);
        }).thenIdle(1).thenExecute(()->{
            var heard=told(q);
            h.assertTrue(q.getUUID().equals(PellRite.leader(b))&&heard.contains(PellTexts.TILLY_TAKES)&&f.own(q,P).getBoolean("TookTillysHand"),"the other reader takes her hand: "+heard);
            h.assertTrue(times(b,"Tilly",PellTexts.TILLY_WHO)==2,"a hand new to her, and she asks again: "+PellRite.lines(b));
            // Toward the pool's west side, through the water.
            f.at(q,P,-9.5,-1,-81.5);
        }).thenWaitUntil(()->h.assertTrue(who(h,l,b,PellPeople.TILLY).getX()<b.getX()-8,"she comes through the water after the hand: "+where.get()))
        .thenExecute(()->f.at(q,P,-12.5,0,-81.5))
        .thenWaitUntil(()->{var t=who(h,l,b,PellPeople.TILLY);h.assertTrue(!t.isNoGravity()&&t.getY()>b.getY()-.1&&!PellSite.inWater(t.position().subtract(b.getX(),b.getY(),b.getZ())),"lifted out where the reader stands at the edge: "+where.get());})
        .thenExecute(()->f.at(q,P,-15.5,0,-79.5))
        .thenWaitUntil(()->{var t=who(h,l,b,PellPeople.TILLY);h.assertTrue(Math.hypot(t.getX()-q.getX(),t.getZ()-q.getZ())<1.5,"she follows the hand across the deck: "+where.get());})
        .thenExecute(()->{told(q);f.at(q,P,-8.5,0,-70.5);})
        .thenIdle(2).thenExecute(()->{
            // Let go on dry land, she stands where she is, and June does not come for her.
            h.assertTrue(PellRite.leader(b)==null&&told(q).contains(PellTexts.TILLY_LET_GO),"let go on the deck");
        }).thenIdle(PellRite.TAKE_BACK+20).thenExecute(()->{
            var t=who(h,l,b,PellPeople.TILLY);
            h.assertTrue(!PellRite.held(b)&&!PellSite.inWater(t.position().subtract(b.getX(),b.getY(),b.getZ())),"June does not take her back from the deck: "+where.get());
            f.at(q,P,t.getX()-b.getX()+1,0,t.getZ()-b.getZ());
            LiteraryVignettes.talk(q,t);
        }).thenIdle(1).thenExecute(()->{
            var heard=told(q);
            h.assertTrue(q.getUUID().equals(PellRite.leader(b))&&heard.contains(PellTexts.TILLY_TAKES_AGAIN),"the same hand again: "+heard);
            h.assertTrue(times(b,"Tilly",PellTexts.TILLY_WHO)==2,"a hand she knows, and she does not ask: "+PellRite.lines(b));
            f.at(q,P,-18.5,0,-79.5);told(q);
        })
        .thenIdle(80).thenExecute(()->{
            var t=who(h,l,b,PellPeople.TILLY);
            h.assertTrue(t.getX()>=b.getX()+PellSite.LINE_W+1-.01,"with the hand over the line where it is whole, she stops short of the paint: "+where.get());
            h.assertTrue(told(q).contains(PellTexts.TILLY_AT_LINE),"her bare foot found it");
            h.assertTrue(PellRite.step(b)==PellRite.WATER,"she is not out");
            f.at(q,P,-18.5,0,-82.5);
        }).thenWaitUntil(()->h.assertTrue(PellRite.step(b)==PellRite.OUT,"along the paint to the gap and through it: "+where.get()))
        .thenIdle(1).thenExecute(()->{
            var t=who(h,l,b,PellPeople.TILLY);
            h.assertTrue(t.getZ()<b.getZ()-81&&t.getZ()>b.getZ()-83.9&&t.getX()<b.getX()+PellSite.LINE_W+1,"through the gap, never over the paint: "+where.get());
            h.assertTrue(told(q).contains(PellTexts.TILLY_OUT)&&f.own(q,P).getBoolean("GotTillyOut"),"out, and the reader who brought her keeps that they did");
            h.assertTrue(told(p).contains(PellTexts.JUNE_STOPPED),"the other reader, across the yard, hears June stop calling");
            h.assertTrue(who(h,l,b,PellPeople.JUNE).gesture()==PellPeople.WADE,"June stands in the water with her arms down");
        }).thenSucceed();
    }
    @AfterBatch(batch="pell_rite_out") public static void riteOutDone(ServerLevel l){done();}

    // ------------------------------------------------------------------------------------------------ 0.4.540, the freezer
    private static boolean freezerIs(ServerLevel l,BlockPos b,PellThing.Kind kind,int more){
        for(var c:new BlockPos[]{PellFreezer.FREEZER,PellFreezer.FREEZER_END}){
            var s=l.getBlockState(b.offset(c));if(!PellThing.is(s,kind)||(s.getValue(PellThing.STAGE)&2)!=more)return false;
        }
        return true;
    }
    /**
     * 0.4.540, the freezer (docs/PELL_HOUSE_FREEZER_0_4_540.md). Two readers who have had the first two visits; the last night locked up
     * and June in the shallow end holding Tilly, her robe over the pool chair. Nessa lies in the freezer under its shut lid, still. A hand
     * on the padlocked freezer with no key gets only what it is. The robe's pocket has June's lanyard in it: the first reader takes it,
     * the other finds the pocket empty. With it, a hand on the freezer opens the padlock, and the next lifts the lid: under the frosted
     * plastic, the girl from every photograph. The other reader, looking in from the doorway, keeps that they saw her; a hand on her is
     * cold. The lanyard, lost from the first reader, is back in the pocket within a second, and the other takes it there.
     */
    @GameTest(template="empty",batch="pell_freezer",timeoutTicks=3000)
    public static void junesLanyardInTheRobeOpensTheFreezerAndNessaIsUnderTheLid(GameTestHelper h){
        var f=house(h);var p=f.player("pell_freezer_key",P);var q=f.player("pell_freezer_door",P);var l=f.level(P);var b=f.base(P);
        for(var r:new ServerPlayer[]{p,q}){keep(f,r,"FirstVisit");keep(f,r,"SecondVisit");}
        f.at(p,P,.5,0,-44);f.at(q,P,-6.5,0,-36);
        h.startSequence().thenWaitUntil(()->lastBegun(h,l,b))
        .thenWaitUntil(()->h.assertTrue(PellFreezer.nessa(l,b)!=null,"Nessa in the freezer"))
        .thenExecute(()->{
            PellLastNight.lockedUp(l,b);PellRite.inWater(l,b);
            var nessa=PellFreezer.nessa(l,b);var at=nessa.position().subtract(b.getX(),b.getY(),b.getZ());
            h.assertTrue(nessa.phase()==PellPeople.FIRST+PellPeople.NESSA&&nessa.gesture()==PellPeople.STILL&&nessa.isNoGravity()&&at.distanceTo(PellFreezer.NESSA_AT)<.1,"lying still in it, along it: "+at);
            h.assertTrue(freezerIs(l,b,PellThing.Kind.FREEZER,0)&&!PellFreezer.open(l,b),"shut and padlocked");
            h.assertTrue(thing(l,b,PellFreezer.CHAIR.getX(),0,PellFreezer.CHAIR.getZ(),POOL_CHAIR,1),"June's robe over the chair while she is in the water");
            // No key: the freezer is only what it is.
            f.at(p,P,7.5,0,-68.5);told(p);
            hand(p,b.offset(PellFreezer.FREEZER));
        }).thenIdle(1).thenExecute(()->{
            h.assertTrue(told(p).contains(PellTexts.thing(FREEZER,0))&&freezerIs(l,b,PellThing.Kind.FREEZER,0),"no key, and it stays padlocked");
            f.at(p,P,-13.5,0,-84.5);told(p);
            hand(p,b.offset(PellFreezer.CHAIR));
        }).thenIdle(1).thenExecute(()->{
            h.assertTrue(told(p).contains(PellTexts.LANYARD_FOUND)&&PellFreezer.carries(p,b)&&f.own(p,P).getBoolean("TookLanyard")&&PellFreezer.lanyardOut(b),"the lanyard out of the robe's pocket");
            f.at(q,P,-12.5,0,-84.5);told(q);
            hand(q,b.offset(PellFreezer.CHAIR));
        }).thenIdle(1).thenExecute(()->{
            h.assertTrue(told(q).contains(PellTexts.LANYARD_GONE)&&!PellFreezer.carries(q,b),"the other finds the pocket empty");
            f.at(p,P,7.5,0,-68.5);told(p);
            hand(p,b.offset(PellFreezer.FREEZER));
        }).thenIdle(1).thenExecute(()->{
            h.assertTrue(told(p).contains(PellTexts.FREEZER_UNLOCKED)&&freezerIs(l,b,PellThing.Kind.FREEZER,2)&&f.own(p,P).getBoolean("UnlockedFreezer"),"the third key, and the padlock comes open");
            hand(p,b.offset(PellFreezer.FREEZER_END));
        }).thenIdle(1).thenExecute(()->{
            var heard=told(p);
            h.assertTrue(heard.contains(PellTexts.FREEZER_LID)&&heard.contains(PellTexts.FREEZER_OPENED),"the lid up, and under the plastic, her: "+heard);
            h.assertTrue(freezerIs(l,b,PellThing.Kind.FREEZER_OPEN,0)&&PellFreezer.open(l,b),"both cells open");
            h.assertTrue(f.own(p,P).getBoolean("OpenedFreezer")&&f.own(p,P).getBoolean("SawNessa"),"the reader who opened it saw her");
            h.assertTrue(!f.own(q,P).getBoolean("SawNessa"),"the other has not, yet");
            // The other reader at the laundry's door, looking in.
            f.at(q,P,7.5,0,-67.6);lookAt(q,new Vec3(b.getX()+PellFreezer.NESSA_AT.x,b.getY()+.7,b.getZ()+PellFreezer.NESSA_AT.z));
        }).thenWaitUntil(()->h.assertTrue(f.own(q,P).getBoolean("SawNessa"),"looking in from the door, they see her"))
        .thenExecute(()->{
            told(q);LiteraryVignettes.talk(q,PellFreezer.nessa(l,b));
        }).thenIdle(1).thenExecute(()->{
            h.assertTrue(told(q).contains(PellTexts.NESSA_HAND),"a hand on her: cold");
            h.assertTrue(PellFreezer.nessa(l,b).gesture()==PellPeople.STILL,"she does not move");
            // The lanyard lost: nobody at the house has it, and it is not lying in the lot.
            var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(inv.getItem(i).is(PellRegistry.LANYARD.get()))inv.setItem(i,net.minecraft.world.item.ItemStack.EMPTY);
        }).thenWaitUntil(()->h.assertTrue(!PellFreezer.lanyardOut(b),"back in the robe's pocket within a second"))
        .thenExecute(()->{
            f.at(q,P,-12.5,0,-84.5);told(q);
            hand(q,b.offset(PellFreezer.CHAIR));
        }).thenIdle(1).thenExecute(()->h.assertTrue(told(q).contains(PellTexts.LANYARD_FOUND)&&PellFreezer.carries(q,b),"and the other takes it there"))
        .thenSucceed();
    }
    @AfterBatch(batch="pell_freezer") public static void freezerDone(ServerLevel l){done();}

    /**
     * 0.4.540, bringing June to the freezer. Two readers who have had the first two visits; the last night locked up, June in the shallow
     * end holding Tilly, the freezer opened with her lanyard and the laundry's door open. The other reader takes Tilly's hand, and June's
     * hands come away. The first takes June's: she lets them, and follows the hand out of the water at its edge, across the deck, out of
     * the pool's gate, in at the sliding door, along the hall and into the laundry. There, a hand on her: she looks at the freezer and at
     * the reader, who says the name. June sits down on the stool by it, her hand on the plastic, and does not call again; the reader keeps
     * that they brought her, and the other, out at the pool, is told she has stopped. Let go in the water, Tilly is not taken back.
     */
    @GameTest(template="empty",batch="pell_freezer_june",timeoutTicks=6000)
    public static void broughtToTheOpenFreezerAndToldTheNameJuneSitsDownByItAndStops(GameTestHelper h){
        var f=house(h);var p=f.player("pell_freezer_leads",P);var q=f.player("pell_freezer_tilly",P);var l=f.level(P);var b=f.base(P);
        for(var r:new ServerPlayer[]{p,q}){keep(f,r,"FirstVisit");keep(f,r,"SecondVisit");}
        f.at(p,P,.5,0,-44);f.at(q,P,-6.5,0,-36);
        java.util.function.Supplier<String> where=()->"June at "+who(h,l,b,PellPeople.JUNE).position().subtract(b.getX(),b.getY(),b.getZ())+", reader at "+p.position().subtract(b.getX(),b.getY(),b.getZ());
        var route=new Vec3[]{new Vec3(-6.5,0,-78.5),new Vec3(-3.5,0,-77.4),new Vec3(-3.5,0,-74.6),new Vec3(-1.5,0,-74),new Vec3(.5,0,-73.6),new Vec3(.5,0,-71.5),
            new Vec3(.5,0,-70.6),new Vec3(0,0,-68.5),new Vec3(0,0,-65.6),new Vec3(3,0,-65.6),new Vec3(5.5,0,-65.6),new Vec3(7.5,0,-65.6),new Vec3(7.5,0,-66.6),
            new Vec3(7.5,0,-68.4),new Vec3(8.5,0,-70.3)};
        int[] said={0};
        var seq=h.startSequence().thenWaitUntil(()->lastBegun(h,l,b))
        .thenWaitUntil(()->h.assertTrue(PellFreezer.nessa(l,b)!=null,"Nessa in the freezer"))
        .thenExecute(()->{
            PellLastNight.lockedUp(l,b);PellRite.inWater(l,b);
            // The freezer opened with June's lanyard, and the laundry's door open.
            p.getInventory().add(PellFreezer.lanyardFor(b));f.at(p,P,7.5,0,-68.5);
            hand(p,b.offset(PellFreezer.FREEZER));
        }).thenIdle(1).thenExecute(()->hand(p,b.offset(PellFreezer.FREEZER)))
        .thenIdle(1).thenExecute(()->{
            h.assertTrue(PellFreezer.open(l,b),"the freezer open");
            if(!PellSite.open(l,b,PellSite.LAUNDRY_DOOR))PellHouse.toggle(l,b,PellSite.LAUNDRY_DOOR);
            // The other reader beside Tilly in the water takes her hand.
            var tilly=who(h,l,b,PellPeople.TILLY);
            f.at(q,P,tilly.getX()-b.getX()+1.2,-1,tilly.getZ()-b.getZ());told(q);
            LiteraryVignettes.talk(q,tilly);
        }).thenIdle(1).thenExecute(()->{
            h.assertTrue(q.getUUID().equals(PellRite.leader(b))&&!PellRite.held(b),"Tilly has the other reader's hand");
            // The first beside June in the water takes hers.
            var june=who(h,l,b,PellPeople.JUNE);
            f.at(p,P,june.getX()-b.getX()-1.2,-1,june.getZ()-b.getZ());told(p);
            LiteraryVignettes.talk(p,june);
        }).thenIdle(1).thenExecute(()->{
            var june=who(h,l,b,PellPeople.JUNE);
            h.assertTrue(p.getUUID().equals(PellRite.juneLeader(b))&&told(p).contains(PellTexts.JUNES_HAND)&&f.own(p,P).getBoolean("TookJunesHand"),"June lets the reader take her hand");
            h.assertTrue(june.gesture()==PellPeople.JUNE_LED,"her hand out in theirs");
            var at=route[0];f.at(p,P,at.x,at.y,at.z);
        }).thenWaitUntil(()->{var j=who(h,l,b,PellPeople.JUNE);h.assertTrue(!PellSite.inWater(j.position().subtract(b.getX(),b.getY(),b.getZ()))&&!j.isNoGravity()&&j.getY()>b.getY()-.1,"up out of the water at its edge after the hand: "+where.get());});
        // Across the deck, out of the gate, in at the slider, along the hall and into the laundry: a step behind the hand each time.
        for(int i=1;i<route.length;i++){
            final var at=route[i];final int n=i;
            seq.thenExecute(()->f.at(p,P,at.x,at.y,at.z))
            .thenWaitUntil(()->{var j=who(h,l,b,PellPeople.JUNE);h.assertTrue(p.getUUID().equals(PellRite.juneLeader(b))&&Math.hypot(j.getX()-p.getX(),j.getZ()-p.getZ())<1.8,"she follows the hand, leg "+n+": "+where.get());});
        }
        seq.thenExecute(()->{
            var june=who(h,l,b,PellPeople.JUNE);
            h.assertTrue(PellHand.inLaundry(june.position().subtract(b.getX(),b.getY(),b.getZ())),"in the laundry with the reader: "+where.get());
            h.assertTrue(!PellRite.juneSat(b)&&PellRite.step(b)==PellRite.WATER,"not sat down yet, and Tilly not out");
            told(p);told(q);
            LiteraryVignettes.talk(p,june);
        }).thenIdle(1).thenExecute(()->{
            h.assertTrue(Talk.key(p).equals("pell_freezer")&&Talk.replies(p).contains(PellTexts.TELL_NESSA)&&Talk.replies(p).contains(PellTexts.TELL_JUNE_NOTHING),"at the open freezer, the reader can say the name, or nothing: "+Talk.replies(p));
            h.assertTrue(Talk.choose(p,PellTexts.TELL_NESSA),"the reader says it");
        }).thenIdle(1).thenExecute(()->{
            var june=who(h,l,b,PellPeople.JUNE);var at=june.position().subtract(b.getX(),b.getY(),b.getZ());
            h.assertTrue(PellRite.juneSat(b)&&PellRite.juneLeader(b)==null,"June sits down by the freezer and stops");
            h.assertTrue(Math.abs(at.x-8.5)<.3&&Math.abs(at.z+68.5)<.3&&june.gesture()==PellPeople.BY_HER,"on the stool beside it, her hand on the plastic: "+at);
            h.assertTrue(told(p).contains(PellTexts.JUNE_SITS)&&f.own(p,P).getBoolean("BroughtJune"),"the reader who brought her keeps it");
            h.assertTrue(told(q).contains(PellTexts.JUNE_STOPPED_INSIDE),"out at the pool, the other is told she has stopped calling");
            said[0]=PellRite.lines(b).size();
        }).thenIdle(PellRite.CALL_EVERY*3).thenExecute(()->{
            h.assertTrue(PellRite.lines(b).size()==said[0],"she does not call again: "+PellRite.lines(b));
            // The other lets Tilly go in the water, and June does not come for her.
            f.at(q,P,-6.5,0,-70.5);
        }).thenIdle(PellRite.TAKE_BACK+40).thenExecute(()->{
            h.assertTrue(!PellRite.held(b)&&PellRite.juneSat(b),"Tilly is not taken back; June sits on");
            told(p);LiteraryVignettes.talk(p,who(h,l,b,PellPeople.JUNE));
        }).thenIdle(1).thenExecute(()->h.assertTrue(told(p).contains(PellTexts.JUNE_BY_HER),"a hand on her: she does not look up"))
        .thenSucceed();
    }
    @AfterBatch(batch="pell_freezer_june") public static void freezerJuneDone(ServerLevel l){done();}

    // ------------------------------------------------------------------------------------------------ 0.4.541, first light
    /** Where a reader stands, from the house's base. */
    private static Vec3 where(ServerPlayer p,BlockPos b){return p.position().subtract(b.getX(),b.getY(),b.getZ());}
    private static boolean standing(ServerPlayer p,BlockPos b,Vec3 at,double within){var r=where(p,b);return Math.hypot(r.x-at.x,r.z-at.z)<within;}
    /** Two readers facing the street, level. */
    private static void toTheStreet(ServerPlayer... readers){for(var r:readers){r.setYRot(0);r.setXRot(0);r.setYHeadRot(0);}}
    /** A book's words, its pages run together. */
    private static String words(net.minecraft.world.item.ItemStack book){
        var content=book.get(DataComponents.WRITTEN_BOOK_CONTENT);if(content==null)return "";
        var out=new StringBuilder();for(var page:content.pages())out.append(page.raw().getString()).append(' ');
        return out.toString().trim();
    }
    /**
     * 0.4.541, first light after Tilly came out (docs/PELL_HOUSE_DAWN_0_4_541.md). Two readers who have had the first two visits; the
     * last night locked up, and Tilly brought out through the line with her own name by the first of them, her hand in theirs, in the
     * front garden by the curb; the other reader on the path; both facing the street. The rest of the night goes on to first light, and
     * Gail's car comes up the street and pulls in at the curb; Gail comes to the foot of the path. Callum is on the front step with his
     * shoes on and Tilly at Gail's side, each the first moment nobody sees them; Gail asks if it is Tilly, and she says it is. Each
     * reader's account is ready under the mat. The first reads the notice, then the account: the house is resolved for them, Tilly's cane
     * tip is theirs, and they are never dealt the house again. The account says what that reader did, and ends in the street.
     */
    @GameTest(template="empty",batch="pell_dawn_out",timeoutTicks=6000)
    public static void atFirstLightAfterTillyCameOutGailsCarComesAndTheAccountEndsInTheStreet(GameTestHelper h){
        var f=house(h);var p=f.player("pell_dawn_brought",P);var q=f.player("pell_dawn_there",P);var l=f.level(P);var b=f.base(P);
        for(var r:new ServerPlayer[]{p,q}){keep(f,r,"FirstVisit");keep(f,r,"SecondVisit");}
        f.at(p,P,.5,0,-44);f.at(q,P,-6.5,0,-36);
        var story=WitnessAccount.Story.of("pell_house");
        h.startSequence().thenWaitUntil(()->lastBegun(h,l,b))
        .thenExecute(()->{
            PellLastNight.lockedUp(l,b);
            // The first reader at the curb with Tilly's hand in theirs, the other on the path; both facing the street.
            f.at(p,P,PellDawn.TILLY_BY_GAIL.x,0,PellDawn.TILLY_BY_GAIL.z);f.at(q,P,1.5,0,-50.5);toTheStreet(p,q);
            PellRite.outWith(l,b,p,List.of(p,q));
            h.assertTrue(PellRite.step(b)==PellRite.OUT&&f.own(p,P).getBoolean("GotTillyOut")&&p.getUUID().equals(PellRite.leader(b)),"Tilly out, brought by the first reader, her hand in theirs");
            h.assertTrue(PellDawn.dawn(b)==PellDawn.NOT_YET,"not light yet");
            PellDawn.nearlyLight(b);told(p);told(q);
        }).thenWaitUntil(()->h.assertTrue(PellDawn.dawn(b)>=PellDawn.COMING,"first light: a car comes up the street"))
        .thenExecute(()->{var c=PellDawn.car(l,b);h.assertTrue(c!=null&&c.position().z>b.getZ()-35,"the car from the end of the street: "+(c==null?"none":c.position().subtract(b.getX(),b.getY(),b.getZ())));})
        .thenWaitUntil(()->h.assertTrue(PellDawn.dawn(b)==PellDawn.COME,"the car pulls in at the curb: "+(PellDawn.car(l,b)==null?"none":PellDawn.car(l,b).position().subtract(b.getX(),b.getY(),b.getZ()))))
        .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Dawned")&&f.own(q,P).getBoolean("Dawned"),"the morning reaches each reader it is for"))
        .thenExecute(()->{
            var c=PellDawn.car(l,b);
            h.assertTrue(c!=null&&Math.hypot(c.getX()-(b.getX()+PellVisit.CAR.x),c.getZ()-(b.getZ()+PellVisit.CAR.z))<.3,"Gail's car at the curb");
            who(h,l,b,PellPeople.GAIL);
            h.assertTrue(told(p).contains(PellTexts.DAWN_OUT),"the reader is told the car is at the curb");
            for(var r:new ServerPlayer[]{p,q}){var own=f.own(r,P);h.assertTrue(own.getBoolean("Ready")&&own.getBoolean("Dawned")&&own.getBoolean("SawCarCome"),"the account is ready for each reader");}
            h.assertTrue(f.own(p,P).getString("Outcome").equals("brought_tilly_out_with_her_name"),"the first brought her out: "+f.own(p,P).getString("Outcome"));
            h.assertTrue(f.own(q,P).getString("Outcome").equals("saw_tilly_come_out_with_her_name"),"the other saw her come out: "+f.own(q,P).getString("Outcome"));
        }).thenWaitUntil(()->h.assertTrue(PellDawn.callumStepped(b)&&PellDawn.tillyWithGail(b),"Callum on the step and Tilly with Gail, the first moment nobody sees them"))
        .thenExecute(()->{
            var c=who(h,l,b,PellPeople.CALLUM);
            h.assertTrue(c.gesture()==PellPeople.STEP&&"pell_callum_shoes".equals(PellPeople.dressed(c.phase(),c.gesture()))&&at(c,b,PellDawn.CALLUM_STEP.x,PellDawn.CALLUM_STEP.z,.3),"Callum sitting on the front step, his shoes on");
            h.assertTrue(at(who(h,l,b,PellPeople.TILLY),b,PellDawn.TILLY_BY_GAIL.x,PellDawn.TILLY_BY_GAIL.z,2.6)&&PellRite.leader(b)==null,"Tilly at Gail's side, her hand let go");
        }).thenWaitUntil(()->h.assertTrue(PellDawn.gailSaid(b)>=3,"Gail asks if it is Tilly, and she says it is"))
        .thenExecute(()->{
            // The first reader up the path to the step.
            f.at(p,P,.5,0,-54.5);told(p);LiteraryVignettes.talk(p,who(h,l,b,PellPeople.CALLUM));
        }).thenIdle(1).thenExecute(()->{
            var heard=told(p);
            h.assertTrue(heard.stream().anyMatch(List.of(PellTexts.CALLUM_STEP_HAND)::contains),"a hand on Callum on the step: "+heard);
            // The notice in the letterbox, then the account under the mat.
            f.read(p,P,PellSite.SOURCE);f.read(p,P,PellSite.ENDING);
        }).thenIdle(1).thenExecute(()->{
            var own=f.own(p,P);
            h.assertTrue(own.getBoolean("Completed")&&WitnessAccount.has(f.data(),p.getUUID(),story),"the house resolved for the reader who read it");
            h.assertTrue(Keepsakes.carries(p,P,"CaneTip"),"Tilly's cane tip is theirs");
            h.assertTrue(!PellHouse.dealable(f.data(),p.getUUID()),"never dealt the house again");
            // 0.4.542: the other's ending has landed too, unread: never dealt the house again as it was, the account waiting for them.
            h.assertTrue(!PellHouse.dealable(f.data(),q.getUUID())&&f.own(q,P).getBoolean("Ended")&&f.own(q,P).getBoolean("Ready")&&!WitnessAccount.has(f.data(),q.getUUID(),story),
                "the other, whose account is unread, has resolved nothing yet, and is not dealt the house again");
            var text=words(PellTexts.account(p.getGameProfile().getName(),own));
            h.assertTrue(text.contains("I led her across the grass to the gap in the line")&&text.contains("Callum was sitting on the front step with his shoes on")
                &&text.contains("the spare rubber tip from her cane")&&text.endsWith("Behind me nobody was calling any name at all."),"the account: what the reader did, and the street: "+text);
            h.assertTrue(!text.contains("she was still sitting there")&&!text.contains("still sitting by the freezer"),"nothing about the freezer that did not happen");
        }).thenSucceed();
    }
    @AfterBatch(batch="pell_dawn_out") public static void dawnOutDone(ServerLevel l){done();}

    /**
     * 0.4.541, first light after Tilly answered. Two readers who have had the first two visits; the last night locked up, and Tilly
     * answered, back in Nessa's bed with June beside her. Both readers on the front path, facing the street. At first light Gail's car
     * comes; each reader has their ending, with nothing to read and nothing resolved. Tilly is at Nessa's window waving, June behind her,
     * the first moment nobody sees them. Gail says her piece. The first reader goes to her: into the car, a beat to look back at the
     * window, then the black, and set down at the House's door. The other does not go; when Gail has waited, the horn, and they are set
     * down too. Neither is dealt the house again, and Gail and her car go.
     */
    @GameTest(template="empty",batch="pell_dawn_kept",timeoutTicks=6000)
    public static void atFirstLightAfterTillyAnsweredTheCarTakesTheReadersAndTillyWavesFromTheWindow(GameTestHelper h){
        var f=house(h);var p=f.player("pell_dawn_goes",P);var q=f.player("pell_dawn_stays",P);var l=f.level(P);var b=f.base(P);
        for(var r:new ServerPlayer[]{p,q}){keep(f,r,"FirstVisit");keep(f,r,"SecondVisit");}
        f.at(p,P,.5,0,-44);f.at(q,P,-6.5,0,-36);
        var story=WitnessAccount.Story.of("pell_house");
        h.startSequence().thenWaitUntil(()->lastBegun(h,l,b))
        .thenExecute(()->{
            PellLastNight.lockedUp(l,b);PellRite.kept(l,b);
            h.assertTrue(PellRite.step(b)==PellRite.KEPT&&PellVisit.asleep(b,PellPeople.TILLY),"Tilly answered, and in Nessa's bed again");
            f.at(p,P,.5,0,-50.5);f.at(q,P,1.5,0,-52.5);toTheStreet(p,q);
            // 0.4.543: the first lay down in the garage this last night.
            var slept=f.own(p,P);slept.putInt("GarageNights",slept.getInt("GarageNights")|1<<PellWeek.visit(b));LiteraryVignettes.save(f.data(),p.getUUID(),P,slept);
            PellDawn.nearlyLight(b);told(p);told(q);
        }).thenWaitUntil(()->h.assertTrue(PellDawn.dawn(b)==PellDawn.COME,"first light: Gail's car at the curb"))
        .thenWaitUntil(()->h.assertTrue(PellDawn.atWindow(b),"Tilly at Nessa's window, the first moment nobody sees it"))
        .thenExecute(()->{
            // 0.4.543: with their ending, the last night's entry is due for the one who slept in the garage, and only for them.
            h.assertTrue(PellEntries.due(f.own(p,P)).equals(List.of(PellEntries.NIGHT+PellWeek.visit(b)))&&PellEntries.due(f.own(q,P)).isEmpty(),
                "the last night's entry due for the one who slept out there: "+PellEntries.due(f.own(p,P))+" / "+PellEntries.due(f.own(q,P)));
            var t=who(h,l,b,PellPeople.TILLY);var j=who(h,l,b,PellPeople.JUNE);
            h.assertTrue(t.gesture()==PellPeople.WAVE&&at(t,b,PellDawn.TILLY_WINDOW.x,PellDawn.TILLY_WINDOW.z,.3)&&!PellVisit.asleep(b,PellPeople.TILLY),"Tilly up at the window, waving");
            h.assertTrue(at(j,b,PellDawn.JUNE_WINDOW.x,PellDawn.JUNE_WINDOW.z,.3)&&j.gesture()==PellPeople.ROBE,"June behind her in her robe");
            for(var r:new ServerPlayer[]{p,q}){var own=f.own(r,P);h.assertTrue(own.getBoolean("Ended")&&own.getBoolean("TillyStayed")&&!own.getBoolean("Ready")&&own.getString("Outcome").equals("tilly_answered"),"each reader has their ending, with nothing to read: "+own.getString("Outcome"));}
            h.assertTrue(told(p).contains(PellTexts.DAWN_ANSWERED)&&!PellHouse.dealable(f.data(),p.getUUID()),"told Gail is waiting; never dealt the house again");
        }).thenWaitUntil(()->h.assertTrue(PellDawn.gailSaid(b)>=1,"Gail at the foot of the path says her piece"))
        .thenExecute(()->{var g=who(h,l,b,PellPeople.GAIL);f.at(p,P,g.getX()-b.getX()+1,0,g.getZ()-b.getZ());told(p);})
        .thenIdle(1).thenExecute(()->{
            h.assertTrue(PellDawn.inCar(b,p.getUUID())&&!PellDawn.inCar(b,q.getUUID()),"the reader who went to Gail is in the car, the other not");
            h.assertTrue(told(p).contains(PellTexts.TILLY_WAVES)&&f.own(p,P).getBoolean("SawTillyWave"),"and sees Tilly wave from the window");
        }).thenIdle(PellDawn.LOOK_BACK+30).thenExecute(()->{
            h.assertTrue(standing(p,b,PellDawn.SET_DOWN,.4)&&f.own(p,P).getBoolean("SetDown")&&told(p).contains(PellTexts.SET_DOWN),"set down at the House's door: "+where(p,b));
            h.assertTrue(PellDawn.dawn(b)==PellDawn.COME,"the car waits for the other");
            told(q);PellDawn.waitedOut(b);
        }).thenIdle(2).thenExecute(()->h.assertTrue(PellDawn.inCar(b,q.getUUID())&&told(q).contains(PellTexts.CAR_HORN),"Gail has waited: the horn, for the one who did not come"))
        .thenIdle(PellDawn.LOOK_BACK+30).thenExecute(()->{
            h.assertTrue(standing(q,b,PellDawn.SET_DOWN,.4)&&f.own(q,P).getBoolean("SetDown"),"set down at the House's door too: "+where(q,b));
            h.assertTrue(PellDawn.dawn(b)==PellDawn.GONE,"everybody gone down the street");
            for(var r:new ServerPlayer[]{p,q})h.assertTrue(!PellHouse.dealable(f.data(),r.getUUID())&&!WitnessAccount.has(f.data(),r.getUUID(),story)&&!f.own(r,P).getBoolean("Completed")&&!Keepsakes.carries(r,P,"CaneTip"),"never dealt again, and nothing resolved");
            toTheStreet(p,q);
        }).thenWaitUntil(()->h.assertTrue(PellDawn.car(l,b)==null&&PellPeople.find(l,b,PellPeople.GAIL)==null,"Gail and her car go, the first moment nobody sees them"))
        .thenSucceed();
    }
    @AfterBatch(batch="pell_dawn_kept") public static void dawnKeptDone(ServerLevel l){done();}

    // ------------------------------------------------------------------------------------------------ 0.4.542, after
    /** A reader whose ending has landed at first light, without the night itself: Tilly came out (or answered), the morning theirs. */
    private static void ended(LiteraryTests.Fixture f,ServerPlayer p,boolean answered){
        var own=f.own(p,P);
        for(var key:List.of("FirstVisit","SecondVisit","Dawned","SawCarCome","Ended"))own.putBoolean(key,true);
        if(answered){own.putBoolean("TillyStayed",true);own.putString("Outcome","tilly_answered");}
        else{own.putBoolean("GotTillyOut",true);own.putBoolean("Ready",true);own.putString("Outcome","brought_tilly_out_with_her_name");}
        own.putInt("Beat",Math.max(own.getInt("Beat"),own.getInt("Visit")));
        LiteraryVignettes.save(f.data(),p.getUUID(),P,own);
    }
    /** Eight crossings of ordinary halls since, so the house is not among the reader's latest places. */
    private static void later(LiteraryTests.Fixture f,ServerPlayer p){
        var hall=java.util.Arrays.stream(LabyrinthPlace.values()).filter(LabyrinthPlace::isGray).findFirst().orElseThrow();
        for(int i=0;i<8;i++)f.data().visit(p.getUUID(),hall);
    }
    private static boolean nobody(ServerLevel l,BlockPos b){return l.getEntitiesOfClass(LiteraryActor.class,PellPeople.area(b),PellPeople::is).isEmpty();}
    /**
     * 0.4.542, the house after Tilly came out (docs/PELL_HOUSE_AFTER_0_4_542.md). A reader whose ending has landed, their account
     * still under the mat. Through the door they came by, the house is only a passage now; eight halls later it can come back. When the
     * house would begin its visit again it stands empty: a FOR SALE sign on the front lawn, the line scrubbed off everywhere (dead grass
     * where it lay on the lawn), the pool drained to its tiles and its gate open, everything the family had gone and only what is built
     * in left, Nessa's room down to the height marks with the N rubbed out, and nobody in it. The reader is told once, and that the
     * account is waiting; a hand gets what each thing is now; the account, read here, resolves the house as at first light. Counted
     * once, it never comes back. A reader who has not had their ending brings it back as it always was, and the visit begins again.
     */
    @GameTest(template="empty",batch="pell_after_out",timeoutTicks=2400)
    public static void afterTillyCameOutTheHouseComesBackOnceEmptyAndForSale(GameTestHelper h){
        var f=house(h);var p=f.player("pell_after_out",P);var l=f.level(P);var b=f.base(P);var d=f.data();
        f.chunks.hold(l,PellPeople.area(b));f.at(p,P,.5,0,-44);
        var story=WitnessAccount.Story.of("pell_house");
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready()&&PellPeople.find(l,b,PellPeople.JUNE)!=null,"the house and its people"))
        .thenExecute(()->{
            h.assertTrue(PellHouse.dealable(d,p.getUUID())&&!Aftermath.canDeal(d,p.getUUID(),P)&&!LabyrinthDealer.finishedFor(d,p.getUUID(),P),"before an ending, the house is the reader's story");
            ended(f,p,false);d.visit(p.getUUID(),P);
            h.assertTrue(!PellHouse.dealable(d,p.getUUID())&&!Aftermath.canDeal(d,p.getUUID(),P)&&LabyrinthDealer.finishedFor(d,p.getUUID(),P),"the door they came by is only a passage now");
            later(f,p);
            h.assertTrue(Aftermath.canDeal(d,p.getUUID(),P)&&!LabyrinthDealer.finishedFor(d,p.getUUID(),P),"eight halls later the house can come back to them");
            told(p);PellPeople.vacate(b);
        }).thenWaitUntil(()->h.assertTrue(PellAftermath.dressed(l,b)==PellAftermath.Kind.EMPTY,"the house as it stands after Tilly came out"))
        .thenIdle(2).thenExecute(()->{
            h.assertTrue(thing(l,b,PellAftermath.SIGN.getX(),0,PellAftermath.SIGN.getZ(),FOR_SALE,0)&&facing(l,b,PellAftermath.SIGN.getX(),0,PellAftermath.SIGN.getZ())==Direction.SOUTH,"a FOR SALE sign on the front lawn, to the street");
            h.assertTrue(thing(l,b,-11,1,-63,MARKS_AFTER,0)&&thing(l,b,-11,0,-63,MARKS,0),"the height marks by Nessa's door, the N rubbed out");
            h.assertTrue(l.getBlockState(b.offset(-13,0,-61)).isAir()&&l.getBlockState(b.offset(-11,0,-69)).isAir()&&l.getBlockState(b.offset(-2,0,-70)).isAir()&&l.getBlockState(b.offset(9,0,-70)).isAir()
                &&l.getBlockState(b.offset(-13,2,-61)).isAir()&&l.getBlockState(b.offset(-9,1,-63)).isAir(),"the bed, the table, the recliner, the freezer, the border and the taped switch all gone");
            h.assertTrue(thing(l,b,-9,0,-71,COUNTER,0)&&thing(l,b,-10,0,-71,SINK,0)&&PellThing.is(l.getBlockState(b.offset(19,0,-80)),KENNEL)&&PellThing.is(l.getBlockState(b.offset(-5,-1,-62)),CIRCLE),"what is built in is left: the benches and sink, the kennel, the circle in the floor");
            h.assertTrue(is(l,b,-10,PellSite.ROOF,-61,"ceiling"),"no stars on Nessa's ceiling");
            for(var at:PellSite.route())h.assertFalse(PellLine.is(l.getBlockState(b.offset(at.getX(),-1,at.getZ()))),"the line scrubbed off at "+at);
            h.assertTrue(is(l,b,-17,-1,-62,"lawn_dry")&&is(l,b,-8,-1,-54,"lawn")&&is(l,b,15,-1,-57,"concrete"),"dead grass where it lay on the lawn, the lawn either side of it, and the drive under it");
            h.assertTrue(l.getBlockState(b.offset(-7,-2,-82)).isAir()&&l.getBlockState(b.offset(-7,-1,-86)).isAir()&&l.getBlockState(b.offset(-7,PellSite.DEEP+1,-90)).isAir()&&is(l,b,-7,PellSite.SHALLOW,-82,"pool_tile")&&PellWeek.rope(l,b).isEmpty(),"the pool drained to its tiles");
            h.assertTrue(l.getBlockState(b.offset(PellSite.POOL_GATE)).getValue(PellBlock.STAGE)==2,"its gate open, the padlock gone");
            h.assertTrue(nobody(l,b)&&PellVisit.car(l,b)==null,"nobody in it");
            var heard=told(p);
            h.assertTrue(heard.contains(PellTexts.AFTER_EMPTY)&&heard.stream().anyMatch(t->t.startsWith("The last account is here.")),"told once what it is now, and that the account waits: "+heard);
            h.assertTrue(Aftermath.returns(d,p.getUUID(),P)==1&&!Aftermath.canDeal(d,p.getUUID(),P)&&LabyrinthDealer.finishedFor(d,p.getUUID(),P),"it comes back once");
            f.click(p,P,PellAftermath.SIGN);f.click(p,P,PellAftermath.MARKS);f.click(p,P,new BlockPos(-17,-1,-62));f.click(p,P,new BlockPos(15,-1,-57));f.click(p,P,new BlockPos(19,0,-80));
        }).thenIdle(2).thenExecute(()->{
            var heard=told(p);
            h.assertTrue(heard.containsAll(List.of(PellTexts.SIGN,PellTexts.MARKS_EMPTY,PellTexts.LINE_GONE,PellTexts.LINE_GONE_HARD,PellTexts.LEFT)),"a hand on each gets what it is now: "+heard);
            f.click(p,P,PellSite.NESSA_DOOR);f.click(p,P,PellSite.POOL_GATE);
            h.assertTrue(PellSite.open(l,b,PellSite.NESSA_DOOR)&&l.getBlockState(b.offset(PellSite.POOL_GATE)).getValue(PellBlock.STAGE)==0,"its doors open and shut to a hand");
            // The account, read here as at first light.
            f.read(p,P,PellSite.SOURCE);f.read(p,P,PellSite.ENDING);
        }).thenIdle(1).thenExecute(()->{
            h.assertTrue(f.own(p,P).getBoolean("Completed")&&WitnessAccount.has(d,p.getUUID(),story)&&Keepsakes.carries(p,P,"CaneTip"),"the account under the mat resolves the house for them, here");
            // A reader who has not had their ending.
            var q=f.player("pell_after_new",P);f.at(q,P,.5,0,-44);
        }).thenWaitUntil(()->h.assertTrue(PellAftermath.dressed(l,b)==PellAftermath.Kind.NONE&&PellPeople.find(l,b,PellPeople.JUNE)!=null&&PellVisit.step(b)>=PellVisit.GREET&&PellWeek.visit(b)==1,"brought back as it always was, for a reader on their first visit"))
        .thenExecute(()->{
            h.assertTrue(l.getBlockState(b.offset(PellAftermath.SIGN)).isAir()&&thing(l,b,-11,1,-63,MARKS,3)&&PellThing.is(l.getBlockState(b.offset(-13,0,-61)),BED)&&is(l,b,-10,PellSite.ROOF,-61,"ceiling_stars"),"the sign gone, the marks and the N, the bed and the stars back");
            h.assertTrue(PellLine.is(l.getBlockState(b.offset(-17,-1,-62)))&&fitting(l,b,new BlockPos(PellSite.WATER_W,-1,PellSite.WATER_S),PellBlock.Kind.TARP),"the line round the lot, and the pool under its cover");
        }).thenSucceed();
    }
    @AfterBatch(batch="pell_after_out") public static void afterOutDone(ServerLevel l){done();}

    /**
     * 0.4.542, the house after Tilly answered. A reader whose ending has landed with nothing to read. Eight halls later the house comes
     * back lived in, with nobody home: everything where the family keeps it, the line gone over again and still wet, the pool open with
     * its rope across it and its gate padlocked, June's door shut to a hand, and on the door frame one more pencil mark, higher, dated
     * later, with an N. Told once; counted once; nothing resolved.
     */
    @GameTest(template="empty",batch="pell_after_kept",timeoutTicks=2400)
    public static void afterTillyAnsweredTheHouseComesBackOnceLivedInWithOneMoreMark(GameTestHelper h){
        var f=house(h);var p=f.player("pell_after_kept",P);var l=f.level(P);var b=f.base(P);var d=f.data();
        f.chunks.hold(l,PellPeople.area(b));f.at(p,P,.5,0,-44);
        var story=WitnessAccount.Story.of("pell_house");
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready()&&PellPeople.find(l,b,PellPeople.JUNE)!=null,"the house and its people"))
        .thenExecute(()->{
            ended(f,p,true);d.visit(p.getUUID(),P);
            h.assertTrue(LabyrinthDealer.finishedFor(d,p.getUUID(),P),"the door they came by is only a passage now");
            later(f,p);told(p);PellPeople.vacate(b);
        }).thenWaitUntil(()->h.assertTrue(PellAftermath.dressed(l,b)==PellAftermath.Kind.KEPT,"the house as it stands after Tilly answered"))
        .thenIdle(2).thenExecute(()->{
            h.assertTrue(thing(l,b,-11,1,-63,MARKS_AFTER,1),"one more pencil mark on the door frame, higher, with an N");
            h.assertTrue(PellThing.is(l.getBlockState(b.offset(-13,0,-61)),BED)&&PellThing.is(l.getBlockState(b.offset(-11,0,-69)),TABLE)&&PellThing.is(l.getBlockState(b.offset(9,0,-70)),FREEZER)&&is(l,b,-10,PellSite.ROOF,-61,"ceiling_stars"),"lived in: everything where it is kept");
            var line=l.getBlockState(b.offset(-17,-1,-62));
            h.assertTrue(PellLine.is(line)&&line.getValue(PellLine.STAGE)==PellLine.LAST,"the line gone over again, still wet");
            h.assertTrue(l.getBlockState(b.offset(-7,-1,-86)).is(Blocks.WATER)&&!PellWeek.rope(l,b).isEmpty()&&l.getBlockState(b.offset(PellSite.POOL_GATE)).getValue(PellBlock.STAGE)==1,"the pool open with its rope across it, the gate padlocked");
            h.assertTrue(l.getBlockState(b.offset(PellAftermath.SIGN)).isAir()&&nobody(l,b),"no sign, and nobody home");
            var heard=told(p);
            h.assertTrue(heard.contains(PellTexts.AFTER_KEPT)&&heard.stream().noneMatch(t->t.startsWith("The last account is here.")),"told once what it is now, with nothing to read: "+heard);
            h.assertTrue(Aftermath.returns(d,p.getUUID(),P)==1&&!Aftermath.canDeal(d,p.getUUID(),P)&&LabyrinthDealer.finishedFor(d,p.getUUID(),P),"it comes back once");
            f.click(p,P,PellAftermath.MARKS);f.click(p,P,PellSite.JUNE_DOOR);f.click(p,P,PellSite.POOL_GATE);f.click(p,P,new BlockPos(-17,-1,-62));
        }).thenIdle(2).thenExecute(()->{
            var heard=told(p);
            h.assertTrue(heard.containsAll(List.of(PellTexts.MARKS_KEPT,PellTexts.JUNES_DOOR,PellTexts.POOL_GATE)),"a hand on the marks, June's door and the gate: "+heard);
            h.assertTrue(heard.stream().anyMatch(t->t.endsWith("It is wet in places. It comes away on a finger.")),"and on the line, wet: "+heard);
            h.assertTrue(!PellSite.open(l,b,PellSite.JUNE_DOOR)&&l.getBlockState(b.offset(PellSite.POOL_GATE)).getValue(PellBlock.STAGE)==1,"June's door and the gate stay shut");
            f.click(p,P,PellSite.NESSA_DOOR);
            h.assertTrue(PellSite.open(l,b,PellSite.NESSA_DOOR),"Nessa's door opens to a hand");
            h.assertTrue(!WitnessAccount.has(d,p.getUUID(),story)&&!f.own(p,P).getBoolean("Completed")&&!PellHouse.dealable(d,p.getUUID()),"nothing resolved, and never dealt as it was");
        }).thenSucceed();
    }
    @AfterBatch(batch="pell_after_kept") public static void afterKeptDone(ServerLevel l){done();}

    // ------------------------------------------------------------------------------------------------ 0.4.543, after a failure
    /**
     * 0.4.543 (docs/PELL_HOUSE_FAILURE_0_4_543.md). Two readers whose endings have landed, each with a journal: Tilly answered for one,
     * and came out with the other. Each resolves a later story. Into the first one's journal, in their own hand, goes a twisted line
     * about what they did there; the other's journal stays as it was. Another story resolved the same House day waits for the next one,
     * and goes in then. Every Witness story but the house's own has its line.
     */
    @GameTest(template="empty",batch="pell_after_hand",timeoutTicks=1800)
    public static void afterTillyAnsweredWhatTheyDoLaterGoesIntoTheirJournalTwisted(GameTestHelper h){
        var f=house(h);var p=f.player("pell_forged_kept",P);var q=f.player("pell_forged_out",P);var l=f.level(P);var b=f.base(P);var s=l.getServer();var d=f.data();
        f.at(p,P,.5,0,-44);f.at(q,P,-6.5,0,-36);
        var data=Journals.get(s);String goat=PellTexts.forged("goatman"),lake=PellTexts.forged("shallows");
        for(var story:WitnessAccount.Story.values())h.assertTrue(story.id.equals("pell_house")!=!PellTexts.forged(story.id).isEmpty(),"a line for every story but the house's own: "+story.id);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready()&&PellPeople.find(l,b,PellPeople.JUNE)!=null,"the house and its people"))
        .thenExecute(()->{
            ended(f,p,true);ended(f,q,false);
            p.getInventory().add(data.issue(p));q.getInventory().add(data.issue(q));
            WitnessAccount.resolve(d,p.getUUID(),WitnessAccount.Story.of("goatman"),"kept_the_door");
            WitnessAccount.resolve(d,q.getUUID(),WitnessAccount.Story.of("goatman"),"kept_the_door");
            h.assertTrue(f.own(p,P).getString("ForgeNext").equals("goatman")&&!f.own(q,P).contains("ForgeNext"),"a later story leaves a line due only for the reader the house failed");
        }).thenWaitUntil(()->h.assertTrue(OwnHand.lines(s,p.getUUID()).equals(List.of("1: "+goat)),"waiting for it in their journal: "+OwnHand.lines(s,p.getUUID())))
        .thenExecute(()->{
            h.assertTrue(PellEntries.forged(f.own(p,P)).equals(List.of("goatman"))&&!f.own(p,P).contains("ForgeNext"),"written, and kept as written");
            h.assertTrue(OwnHand.lines(s,q.getUUID()).isEmpty(),"after a success, nothing new in their hand");
            WitnessAccount.resolve(d,p.getUUID(),WitnessAccount.Story.of("shallows"),"threw_her");
            h.assertTrue(f.own(p,P).getString("ForgeNext").equals("shallows"),"the next story's line due");
        }).thenIdle(PellEntries.TRY*3).thenExecute(()->{
            h.assertTrue(OwnHand.lines(s,p.getUUID()).size()==1,"one a House day at most: "+OwnHand.lines(s,p.getUUID()));
            var world=s.overworld();world.setDayTime(world.getDayTime()+24000L);
        }).thenWaitUntil(()->h.assertTrue(OwnHand.lines(s,p.getUUID()).equals(List.of("1: "+goat,"1: "+lake)),"the next House day, the next: "+OwnHand.lines(s,p.getUUID())))
        .thenExecute(()->h.assertTrue(PellEntries.forged(f.own(p,P)).equals(List.of("goatman","shallows"))&&OwnHand.lines(s,q.getUUID()).isEmpty(),"two lines, both theirs alone"))
        .thenSucceed();
    }
    @AfterBatch(batch="pell_after_hand") public static void afterHandDone(ServerLevel l){done();}

    /** A resident's door (locked: "Someone lives here.") at this place on the house's level, on stone, facing south, as the House keeps them. */
    private static LabyrinthData.Door residentDoor(LiteraryTests.Fixture f,BlockPos rel){
        var l=f.level(P);var at=f.base(P).offset(rel);
        l.setBlock(at.below(),Blocks.STONE.defaultBlockState(),3);
        var s=Blocks.OAK_DOOR.defaultBlockState().setValue(net.minecraft.world.level.block.DoorBlock.FACING,Direction.SOUTH);
        l.setBlock(at,s.setValue(net.minecraft.world.level.block.DoorBlock.HALF,net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER),3);
        l.setBlock(at.above(),s.setValue(net.minecraft.world.level.block.DoorBlock.HALF,net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER),3);
        var door=new LabyrinthData.Door("test/pell_resident",l.dimension(),at,Direction.SOUTH,LabyrinthData.LOCKED,true);f.data().putDoor(door);return door;
    }
    /** What this reader has been sent since last asked: the cane's taps, where they were heard, and every line. Reading empties it. */
    private static void listen(ServerPlayer p,List<Vec3> taps,List<String> lines){
        for(var packet:NativeTestPlayers.packets(p)){
            if(packet instanceof net.minecraft.network.protocol.game.ClientboundSoundPacket sound&&sound.getSound().value()==PellRegistry.CANE_TAP.get())taps.add(new Vec3(sound.getX(),sound.getY(),sound.getZ()));
            else if(packet instanceof net.minecraft.network.protocol.game.ClientboundSystemChatPacket chat)lines.add(chat.content().getString());
        }
    }
    /**
     * 0.4.543, the cane behind a door that will not open (docs/PELL_HOUSE_FAILURE_0_4_543.md). Two readers a few steps from a resident's
     * locked door. It is due for the reader Tilly answered for, not for the one she came out with. Behind the door, for that reader alone,
     * a cane taps along; at the door, June calls "Nessa?" and the answer is the one Tilly gave in the pool. Then it is over, and it is not
     * due again that House day. Another day: trying the door stops it, and the door stays locked; knocking stops it, and nobody knocks back.
     */
    @GameTest(template="empty",batch="pell_after_cane",timeoutTicks=1800)
    public static void afterTillyAnsweredACaneTapsBehindADoorThatWillNotOpen(GameTestHelper h){
        var f=house(h);var p=f.player("pell_cane_kept",P);var q=f.player("pell_cane_out",P);var l=f.level(P);var b=f.base(P);var s=l.getServer();
        var rel=new BlockPos(PellSite.EAST+12,0,-30);f.chunks.hold(l,new AABB(b.offset(rel)).inflate(16));
        var tapsP=new java.util.ArrayList<Vec3>();var linesP=new java.util.ArrayList<String>();var tapsQ=new java.util.ArrayList<Vec3>();var linesQ=new java.util.ArrayList<String>();
        LabyrinthData.Door[] door={null};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready()&&PellPeople.find(l,b,PellPeople.JUNE)!=null,"the house, its people and the ground by the door"))
        .thenExecute(()->{
            ended(f,p,true);ended(f,q,false);door[0]=residentDoor(f,rel);
            f.at(p,P,rel.getX()+.5,0,rel.getZ()+6.5);f.at(q,P,rel.getX()-1.5,0,rel.getZ()+6.5);
            h.assertTrue(PellAfterlife.due(p)&&!PellAfterlife.due(q),"due for the reader Tilly answered for, not for the one she came out with");
            var found=PellAfterlife.door(p);h.assertTrue(found!=null&&found.id.equals(door[0].id),"the resident's door near them is the one: "+(found==null?null:found.id));
            listen(p,tapsP,linesP);listen(q,tapsQ,linesQ);tapsP.clear();linesP.clear();tapsQ.clear();linesQ.clear();
            PellAfterlife.begin(p,door[0],s.getTickCount());
            h.assertTrue(!PellAfterlife.due(p),"once a House day");
        }).thenWaitUntil(()->{listen(p,tapsP,linesP);h.assertTrue(tapsP.size()>=3,"waiting for the cane: "+tapsP.size());})
        .thenExecute(()->{
            h.assertTrue(linesP.contains(PellTexts.CANE_BEGINS),"told a cane is tapping behind one of the doors: "+linesP);
            double doorZ=b.getZ()+rel.getZ();for(var t:tapsP)h.assertTrue(t.z<doorZ+.6,"behind the door, on the side away from them: "+t);
            listen(q,tapsQ,linesQ);h.assertTrue(tapsQ.isEmpty()&&!linesQ.contains(PellTexts.CANE_BEGINS),"the other reader hears none of it");
            f.at(p,P,rel.getX()+.5,0,rel.getZ()+1.5);
        }).thenWaitUntil(()->{listen(p,tapsP,linesP);h.assertTrue(linesP.contains(PellTexts.CANE_ANSWER),"at the door, the call and the answer: "+linesP);})
        .thenExecute(()->{
            h.assertTrue(linesP.indexOf(PellTexts.CANE_CALL)>=0&&linesP.indexOf(PellTexts.CANE_CALL)<linesP.indexOf(PellTexts.CANE_ANSWER),"June calls Nessa, and the answer comes after: "+linesP);
            h.assertTrue(PellTexts.CANE_ANSWER.contains(PellTexts.TILLY_ANSWERS),"the answer Tilly gave in the pool");
            listen(q,tapsQ,linesQ);h.assertTrue(!linesQ.contains(PellTexts.CANE_CALL)&&!linesQ.contains(PellTexts.CANE_ANSWER),"the other reader is told nothing");
        }).thenWaitUntil(()->h.assertTrue(PellAfterlife.tapping(p.getUUID())==null,"then the cane goes, and it is over"))
        .thenExecute(()->{
            h.assertTrue(!PellAfterlife.due(p),"not again that House day");
            var world=s.overworld();world.setDayTime(world.getDayTime()+24000L);
            h.assertTrue(PellAfterlife.due(p),"due again the next House day");
            // Another day: a hand on the door.
            PellAfterlife.begin(p,door[0],s.getTickCount());listen(p,tapsP,linesP);linesP.clear();
            LabyrinthDoors.use(p,door[0]);
            h.assertTrue(PellAfterlife.tapping(p.getUUID())==null,"trying the door stops it");
        }).thenIdle(2).thenExecute(()->{
            listen(p,tapsP,linesP);
            h.assertTrue(linesP.contains("The door is locked. Someone lives here.")&&!linesP.contains(PellTexts.CANE_CALL),"the door stays locked, and nobody calls: "+linesP);
            h.assertTrue(!l.getBlockState(b.offset(rel)).getValue(net.minecraft.world.level.block.DoorBlock.OPEN),"it does not open");
            // And a knock.
            PellAfterlife.begin(p,door[0],s.getTickCount());linesP.clear();
            DoorSounds.knock(p,f.data(),door[0]);
            h.assertTrue(PellAfterlife.tapping(p.getUUID())==null,"knocking stops it");
        }).thenIdle(DoorSounds.REPLY_AFTER+10).thenExecute(()->{
            listen(p,tapsP,linesP);
            h.assertTrue(linesP.stream().noneMatch(t->t.contains("knocks back"))&&!linesP.contains(PellTexts.CANE_CALL),"nobody knocks back: "+linesP);
        }).thenSucceed();
    }
    @AfterBatch(batch="pell_after_cane") public static void afterCaneDone(ServerLevel l){done();DoorSounds.clearAll();}

    // ------------------------------------------------------------------------------------------------ 0.4.544, the torn page
    /** What has been sent to this reader since last asked: the lines, the torn pages opened for them to read (by writer), and the pins
     *  they heard go in, where. Reading empties it. */
    private record Sent(List<String> lines,List<String> pages,List<Vec3> pins,List<String> payloads){}
    private static Sent sent(ServerPlayer p){
        var lines=new java.util.ArrayList<String>();var pages=new java.util.ArrayList<String>();var pins=new java.util.ArrayList<Vec3>();var payloads=new java.util.ArrayList<String>();
        for(var packet:NativeTestPlayers.packets(p)){
            if(packet instanceof net.minecraft.network.protocol.game.ClientboundSystemChatPacket chat)lines.add(chat.content().getString());
            else if(packet instanceof net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket custom){
                payloads.add(custom.payload().type().id().toString());
                if(custom.payload() instanceof io.github.knaitoe.theoldesthouse.network.TornPagePayload torn)pages.add(torn.writer());
            }
            else if(packet instanceof net.minecraft.network.protocol.game.ClientboundSoundPacket sound&&sound.getSound().value()==net.minecraft.sounds.SoundEvents.ITEM_FRAME_ADD_ITEM)pins.add(new Vec3(sound.getX(),sound.getY(),sound.getZ()));
        }
        return new Sent(lines,pages,pins,payloads);
    }
    /** Whether this reader has this copy of a torn page about them, and it is still the page. */
    private static boolean holds(ServerPlayer p,TornPages.Ref ref){
        var inv=p.getInventory();
        for(int i=0;i<inv.getContainerSize();i++){var r=TornPages.ref(inv.getItem(i));if(ref.equals(r)&&TornPages.valid(p.server,r))return true;}
        return false;
    }
    /**
     * 0.4.544, the torn page in Gail's folder (docs/PELL_HOUSE_PAGE_0_4_544.md). Two readers on the last night, Tilly answered. The
     * first has one of the house's lines in their own hand on a page with nothing else on it, and tears it out; the other tears out a
     * page they wrote themselves. At first light the false page is in Gail's folder, and the copy in the first reader's pocket is blank;
     * the other's own page stays with them. When Gail has said her piece and the first reader comes near, she holds the page out to them
     * from her folder, and they are told what it is; the car waits for them while she holds it out; a hand on her is a hand on the page,
     * and they read it. Then she puts it back in her folder, keeps it, and the car takes them. Nothing is held out to the other reader.
     */
    @GameTest(template="empty",batch="pell_page_folder",timeoutTicks=6000)
    public static void atFirstLightGailHasTheTornFalsePageInHerFolderAndHoldsItOut(GameTestHelper h){
        var f=house(h);var p=f.player("pell_page_torn",P);var q=f.player("pell_page_plain",P);var l=f.level(P);var b=f.base(P);var s=l.getServer();
        for(var r:new ServerPlayer[]{p,q}){keep(f,r,"FirstVisit");keep(f,r,"SecondVisit");}
        f.at(p,P,.5,0,-44);f.at(q,P,-6.5,0,-36);
        var data=Journals.get(s);String line=PellTexts.falseEntry(PellEntries.KNIFE);int[] page={0};TornPages.Ref[] held={null};
        h.startSequence().thenWaitUntil(()->lastBegun(h,l,b))
        .thenExecute(()->{
            PellLastNight.lockedUp(l,b);PellRite.kept(l,b);
            f.at(p,P,.5,0,-50.5);f.at(q,P,1.5,0,-52.5);toTheStreet(p,q);
            p.getInventory().add(data.issue(p));q.getInventory().add(data.issue(q));
            page[0]=OwnHand.write(p,line);
            h.assertTrue(page[0]>0&&data.page(p.getUUID(),page[0]).text().isBlank()&&data.page(p.getUUID(),page[0]).ownHand(),"the line in their own hand, on a page with nothing else on it");
            h.assertTrue(TornPages.tear(p,page[0])==TornPages.Tear.TEARING,"a page with only that line on it tears out");
            h.assertTrue(Journals.edit(q,1,JournalTests.page("June made pancakes."))==Journals.Edit.ACCEPTED&&TornPages.tear(q,1)==TornPages.Tear.TEARING,"the other tears out a page of their own");
        }).thenWaitUntil(()->h.assertTrue(TornPages.where(s,p.getUUID(),page[0])==TornPages.Where.CARRIED&&TornPages.where(s,q.getUUID(),1)==TornPages.Where.CARRIED,"both pages out, in their pockets"))
        .thenExecute(()->{
            h.assertTrue(PellPages.torn(s,p.getUUID()).equals(List.of(page[0]))&&PellPages.torn(s,q.getUUID()).isEmpty(),"one false page out; the other reader's own page is not one");
            held[0]=TornPages.current(s,p.getUUID(),page[0]);
            h.assertTrue(holds(p,held[0])&&TornPages.page(s,held[0]).ownLines().equals(List.of(line)),"the torn page in their pocket, the line in their hand on it");
            PellDawn.nearlyLight(b);sent(p);sent(q);
        }).thenWaitUntil(()->h.assertTrue(TornPages.where(s,p.getUUID(),page[0])==TornPages.Where.KEPT,"from first light the page is in Gail's folder"))
        .thenExecute(()->{
            h.assertTrue(!TornPages.valid(s,held[0])&&!holds(p,held[0]),"the copy in the reader's pocket is blank now");
            h.assertTrue(TornPages.where(s,q.getUUID(),1)==TornPages.Where.CARRIED,"the other reader's own page stays with them");
        }).thenWaitUntil(()->h.assertTrue(PellDawn.dawn(b)==PellDawn.COME&&PellDawn.gailSaid(b)>=1,"Gail at the foot of the path says her piece"))
        .thenExecute(()->{
            // Four steps from her: near enough to be shown it, not near enough to be put in the car.
            var g=who(h,l,b,PellPeople.GAIL);f.at(p,P,g.getX()-b.getX(),0,g.getZ()-b.getZ()-4);sent(p);sent(q);
        }).thenWaitUntil(()->h.assertTrue(p.getUUID().equals(PellPages.showing(b)),"Gail holds the page out to them"))
        .thenExecute(()->{
            var g=who(h,l,b,PellPeople.GAIL);var ref=TornPages.ref(g.getMainHandItem());
            h.assertTrue(ref!=null&&ref.owner().equals(p.getUUID())&&ref.page()==page[0]&&TornPages.valid(s,ref),"the page in her hand is theirs");
            h.assertTrue(g.getOffhandItem().is(PellRegistry.FOLDER.get())&&g.gesture()==PellPeople.SHOW,"held out, her folder in her other hand");
            h.assertTrue(sent(p).lines().contains(PellTexts.PAGE_HELD_OUT)&&f.own(p,P).getBoolean("PageShown"),"the reader is told what she holds out");
            h.assertTrue(!sent(q).lines().contains(PellTexts.PAGE_HELD_OUT),"the other reader is not");
            // Right up to her: the car waits while she holds it out.
            f.at(p,P,g.getX()-b.getX(),0,g.getZ()-b.getZ()-1.5);
        }).thenIdle(3).thenExecute(()->{
            h.assertTrue(!PellDawn.inCar(b,p.getUUID()),"the car waits while she holds it out");
            // A hand on her; what the server sends a reader in a tick reaches them at the tick's end.
            sent(p);LiteraryVignettes.talk(p,who(h,l,b,PellPeople.GAIL));
        }).thenIdle(1).thenExecute(()->{
            var after=sent(p);
            h.assertTrue(after.pages().size()==1,"a hand on her is a hand on the page: it opens for them to read: "+after.payloads()+" "+after.lines());
        }).thenWaitUntil(()->h.assertTrue(PellPages.showing(b)==null,"then she puts it back"))
        .thenExecute(()->{
            var g=who(h,l,b,PellPeople.GAIL);
            h.assertTrue(g.getMainHandItem().is(PellRegistry.FOLDER.get())&&g.getOffhandItem().isEmpty()&&g.gesture()==PellPeople.FOLDER,"her folder held to her chest again");
            h.assertTrue(TornPages.where(s,p.getUUID(),page[0])==TornPages.Where.KEPT,"and the page kept in it");
        }).thenWaitUntil(()->h.assertTrue(PellDawn.inCar(b,p.getUUID()),"then the car takes the reader at her side"))
        .thenExecute(()->h.assertTrue(PellPages.showing(b)==null&&!f.own(q,P).getBoolean("PageShown"),"nothing held out to the other reader, who tore out no false page"))
        .thenSucceed();
    }
    @AfterBatch(batch="pell_page_folder") public static void pageFolderDone(ServerLevel l){done();}

    /**
     * 0.4.544, the torn page pinned in a later hall. Two readers in a closed stone hall, each with a torn page with one of the house's
     * lines in their own hand on it; the first has had their ending at the house, the other has not. The first's page turns up pinned to
     * the hall's wall, behind them and out of their sight, at their head height: the House's doing, not counted among the pages they may
     * leave, and the pin heard by them alone; the copy in their pocket is blank. The other's does not turn up. Anybody can read it, nobody
     * but its writer can take it down. Taken back into the journal and torn out again, it turns up again; knocked down, it is due to.
     */
    @GameTest(template="empty",batch="pell_page_hall",timeoutTicks=1800)
    public static void afterTheHouseTheTornFalsePageTurnsUpPinnedInALaterHall(GameTestHelper h){
        var f=house(h);var p=f.player("pell_page_after",P);var q=f.player("pell_page_beside",P);var l=f.level(P);var b=f.base(P);var s=l.getServer();
        // A closed hall away from the house: seven wide and nine long inside, three high, stone all round.
        var lo=b.offset(PellSite.EAST+14,0,-34);var hi=b.offset(PellSite.EAST+20,2,-26);
        f.chunks.hold(l,new AABB(Vec3.atLowerCornerOf(lo),Vec3.atLowerCornerOf(hi)).inflate(10));
        java.util.function.Predicate<BlockPos> inside=c->c.getX()>=lo.getX()&&c.getX()<=hi.getX()&&c.getY()>=lo.getY()&&c.getY()<=hi.getY()&&c.getZ()>=lo.getZ()&&c.getZ()<=hi.getZ();
        var data=Journals.get(s);String line=PellTexts.falseEntry(PellEntries.KNIFE);int[] page={0,0};TornPages.Ref[] held={null};
        net.minecraft.world.entity.Entity[] up={null};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready()&&PellPeople.find(l,b,PellPeople.JUNE)!=null,"the house, its people and the ground for the hall"))
        .thenExecute(()->{
            for(var at:BlockPos.betweenClosed(lo.offset(-1,-1,-1),hi.offset(1,1,1)))l.setBlock(at,inside.test(at)?Blocks.AIR.defaultBlockState():Blocks.STONE.defaultBlockState(),3);
            ended(f,p,true);
            p.getInventory().add(data.issue(p));q.getInventory().add(data.issue(q));
            page[0]=OwnHand.write(p,line);page[1]=OwnHand.write(q,line);
            h.assertTrue(page[0]>0&&page[1]>0&&TornPages.tear(p,page[0])==TornPages.Tear.TEARING&&TornPages.tear(q,page[1])==TornPages.Tear.TEARING,"each tears out a page with the line in their own hand");
        }).thenWaitUntil(()->h.assertTrue(TornPages.where(s,p.getUUID(),page[0])==TornPages.Where.CARRIED&&TornPages.where(s,q.getUUID(),page[1])==TornPages.Where.CARRIED,"both pages out, in their pockets"))
        .thenExecute(()->{
            held[0]=TornPages.current(s,p.getUUID(),page[0]);
            h.assertTrue(PellPages.next(p)==page[0]&&PellPages.next(q)==0,"due to turn up for the reader whose ending has landed, not the other");
            // Both in the middle of the hall, looking north along it.
            var mid=lo.offset(3,0,4);
            p.teleportTo(l,mid.getX()+.5,mid.getY(),mid.getZ()+.5,180,0);q.teleportTo(l,mid.getX()+1.5,mid.getY(),mid.getZ()+.5,180,0);
            p.setYHeadRot(180);q.setYHeadRot(180);sent(p);sent(q);
            h.assertTrue(PellPages.turnUp(q,inside)==null,"nothing turns up for the other reader");
            var pinned=PellPages.turnUp(p,inside);up[0]=pinned;
            h.assertTrue(pinned!=null&&pinned.ref().owner().equals(p.getUUID())&&pinned.ref().page()==page[0],"the page turns up pinned to a wall");
            var support=pinned.support();var face=pinned.face();
            h.assertTrue(l.getBlockState(support).is(Blocks.STONE)&&inside.test(support.relative(face))&&support.getY()==p.blockPosition().getY()+1,"on the hall's wall, at their head height: "+support+" "+face);
            var at=pinned.getBoundingBox().getCenter();double d=at.distanceTo(p.getEyePosition());
            h.assertTrue(d>=PellPages.FROM&&d<=PellPages.REACH,"a few blocks from them: "+d);
            h.assertTrue(p.getLookAngle().dot(at.subtract(p.getEyePosition()).normalize())<.26,"behind them, out of their sight, as it went up");
            h.assertTrue(TornPages.where(s,p.getUUID(),page[0])==TornPages.Where.PINNED&&TornPages.byHouse(s,p.getUUID(),page[0])&&TornPages.out(s,p.getUUID())==0,"pinned by the House, not counted among the pages they may leave");
            h.assertTrue(!TornPages.valid(s,held[0])&&!holds(p,held[0]),"the copy in their pocket is blank");
            h.assertTrue(pinned.glance().replace('\n',' ').equals(line),"its first lines show at a glance: "+pinned.glance());
        // Run 1579: what the server sends a reader in a tick reaches them at the tick's end, so what they heard is read a tick on.
        }).thenIdle(1).thenExecute(()->{
            var pinned=(io.github.knaitoe.theoldesthouse.journal.PinnedPage)up[0];var at=pinned.getBoundingBox().getCenter();
            var heard=sent(p);h.assertTrue(heard.pins().size()==1&&heard.pins().getFirst().distanceTo(at)<1.5,"the pin heard by them, where it went in: "+heard.pins());
            h.assertTrue(sent(q).pins().isEmpty(),"and not by the other reader");
            h.assertTrue(PellPages.next(p)==0,"put up by the House, it is not due again");
            // Anybody can read it; nobody but its writer can take it down.
            pinned.interact(q,net.minecraft.world.InteractionHand.MAIN_HAND);
        }).thenIdle(1).thenExecute(()->{
            var pinned=(io.github.knaitoe.theoldesthouse.journal.PinnedPage)up[0];
            h.assertTrue(sent(q).pages().size()==1,"the other reader reads it");
            pinned.skipAttackInteraction(q);
            h.assertTrue(!pinned.isRemoved()&&TornPages.where(s,p.getUUID(),page[0])==TornPages.Where.PINNED,"and cannot take it down");
            // Its writer takes it back into the journal, and tears it out again.
            h.assertTrue(TornPages.reclaim(p,page[0])&&TornPages.where(s,p.getUUID(),page[0])==null&&pinned.isRemoved(),"its writer takes it back into the journal");
            h.assertTrue(TornPages.tear(p,page[0])==TornPages.Tear.TEARING,"and tears it out again");
        }).thenWaitUntil(()->h.assertTrue(TornPages.where(s,p.getUUID(),page[0])==TornPages.Where.CARRIED,"out again, in their pocket"))
        .thenExecute(()->{
            h.assertTrue(PellPages.next(p)==page[0],"due to turn up again");
            var again=PellPages.turnUp(p,inside);up[0]=again;
            h.assertTrue(again!=null&&TornPages.byHouse(s,p.getUUID(),page[0])&&TornPages.where(s,p.getUUID(),page[0])==TornPages.Where.PINNED,"it turns up again");
            // Knocked down: its wall gone, it falls.
            l.setBlock(again.support(),Blocks.AIR.defaultBlockState(),3);
        }).thenWaitUntil(()->h.assertTrue(TornPages.where(s,p.getUUID(),page[0])==TornPages.Where.DROPPED,"its wall gone, it falls"))
        .thenExecute(()->h.assertTrue(!TornPages.byHouse(s,p.getUUID(),page[0])&&PellPages.next(p)==page[0],"fallen, it is due to turn up again"))
        .thenSucceed();
    }
    @AfterBatch(batch="pell_page_hall") public static void pageHallDone(ServerLevel l){done();PellPages.clearAll();}

    // ------------------------------------------------------------------------------------------------ 0.4.545, a reader as Tilly
    /** For the last night: one reader's record says they are Tilly at the house, the other's who was, as the cane leaves them. */
    private static void playsTilly(LiteraryTests.Fixture f,ServerPlayer tilly,ServerPlayer sibling){
        var own=f.own(tilly,P);own.putBoolean(PellTilly.PLAYS,true);LiteraryVignettes.save(f.data(),tilly.getUUID(),P,own);
        var o=f.own(sibling,P);o.putUUID(PellTilly.TILLY_ID,tilly.getUUID());o.putString(PellTilly.TILLY_NAME,tilly.getGameProfile().getName());LiteraryVignettes.save(f.data(),sibling.getUUID(),P,o);
    }
    private static final String TILLY_LOOK="the_oldest_house:pell_tilly";
    /** A hand on another reader, as the right-click sends it. */
    private static void handOn(ServerPlayer p,ServerPlayer other){
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.EntityInteract(p,net.minecraft.world.InteractionHand.MAIN_HAND,other));
    }
    /**
     * 0.4.545, a reader as Tilly (docs/PELL_HOUSE_TILLY_0_4_545.md). Two readers on their first visit. Before June has the front door open
     * the cane on the hook is only a cane. Once she has, each reader is told once that whoever takes it is Tilly. One reader's hand on the
     * hook takes it: they are Tilly from then on, the other is told who took it, and each record says so. The hook is bare of the cane, and
     * the other's hand on it takes nothing. The house's own Tilly is hidden where the reader is, and cannot be touched; the reader is a
     * child's size, and drawn as Tilly to both readers. A line of Tilly's comes to the reader in grey, and the house's Tilly says nothing.
     */
    @GameTest(template="empty",batch="pell_tilly_cane",timeoutTicks=4000)
    public static void whoeverTakesTheCaneOnTheFirstVisitIsTillyAndHasHerLinesInGrey(GameTestHelper h){
        var f=house(h);var p=f.player("pell_tilly_cane",P);var q=f.player("pell_tilly_older",P);var l=f.level(P);var b=f.base(P);
        var at=PellTilly.HOOK;var hook=b.offset(at);
        f.at(p,P,.5,0,-44);f.at(q,P,-6.5,0,-36);
        h.startSequence().thenWaitUntil(()->{for(int i=0;i<PellPeople.COUNT;i++)h.assertTrue(PellPeople.find(l,b,i)!=null,"waiting for "+PellPeople.name(i));})
        .thenIdle(10).thenExecute(()->{
            // In the hall, in reach of the hook.
            f.at(p,P,1.5,0,-61.5);told(p);hand(p,hook);
            h.assertTrue(!f.own(p,P).getBoolean(PellTilly.PLAYS)&&thing(l,b,at.getX(),at.getY(),at.getZ(),COAT_HOOK,0),"before June has the door open, the cane stays on its hook");
        }).thenIdle(1).thenExecute(()->{
            h.assertTrue(told(p).contains(PellTexts.thing(COAT_HOOK,0)),"a hand on it says what hangs there");
            // Both up to Gail, from the street.
            f.at(p,P,.5,0,-49.5);f.at(q,P,1.5,0,-49.5);
        }).thenWaitUntil(()->h.assertTrue(PellVisit.step(b)>=PellVisit.TOUR,"June gives the line at the step"))
        .thenIdle(21).thenExecute(()->{
            h.assertTrue(told(p).contains(PellTexts.CANE_HINT)&&told(q).contains(PellTexts.CANE_HINT),"each reader is told that whoever takes the cane is Tilly");
            // Both in at the front door, in reach of the hook.
            f.at(p,P,1.5,0,-61.5);f.at(q,P,1.5,0,-62.5);
            hand(p,hook);
            h.assertTrue(f.own(p,P).getBoolean(PellTilly.PLAYS)&&p.getUUID().equals(PellTilly.readerAt(b)),"the reader who took the cane is Tilly");
            var o=f.own(q,P);
            h.assertTrue(o.hasUUID(PellTilly.TILLY_ID)&&o.getUUID(PellTilly.TILLY_ID).equals(p.getUUID())&&o.getString(PellTilly.TILLY_NAME).equals(p.getGameProfile().getName()),"the other's record says who");
            h.assertTrue(thing(l,b,at.getX(),at.getY(),at.getZ(),COAT_HOOK,1),"the hook is bare of the cane");
        }).thenIdle(1).thenExecute(()->{
            h.assertTrue(told(p).contains(PellTexts.caneTaken(q.getGameProfile().getName())),"the reader is told they are Tilly to the other");
            h.assertTrue(told(q).contains(PellTexts.caneTakenBy(p.getGameProfile().getName())),"the other is told who took it");
            hand(q,hook);
            h.assertTrue(!f.own(q,P).getBoolean(PellTilly.PLAYS)&&p.getUUID().equals(PellTilly.readerAt(b)),"the other's hand on the hook takes nothing");
        }).thenIdle(12).thenExecute(()->{
            var t=who(h,l,b,PellPeople.TILLY);
            h.assertTrue(PellTilly.small(p)&&p.getBbHeight()<1.5,"the reader is a child's size: "+p.getBbHeight());
            h.assertTrue(t.isInvisible()&&!t.isPickable()&&t.distanceTo(p)<.3,"the house's Tilly hidden where the reader is, not to be touched: "+t.distanceTo(p));
            h.assertTrue(sent(p).payloads().contains(TILLY_LOOK)&&sent(q).payloads().contains(TILLY_LOOK),"drawn as Tilly to both readers");
            PellTilly.says(l,b,PellTexts.TILLY_WHO);
        }).thenIdle(1).thenExecute(()->{
            h.assertTrue(told(p).contains(PellTexts.mightSay(PellTexts.TILLY_WHO)),"a line of Tilly's comes to the reader in grey");
            h.assertTrue(!told(q).contains(PellTexts.mightSay(PellTexts.TILLY_WHO))&&!said(who(h,l,b,PellPeople.TILLY)).equals(PellTexts.TILLY_WHO),"and nobody hears it from the house's Tilly");
            // Both out of the house at once, nobody left at it.
            p.teleportTo(l,b.getX()+.5,b.getY()+60,b.getZ()-60,0,0);q.teleportTo(l,b.getX()+1.5,b.getY()+60,b.getZ()-60,0,0);
        }).thenIdle(12).thenExecute(()->{
            h.assertTrue(!PellTilly.small(p),"out of the house, the reader's size is their own again");
            // The other back at the house: Tilly is the house's again, and the cane back on its hook.
            f.at(q,P,1.5,0,-62.5);
        }).thenIdle(2).thenExecute(()->{
            var t=who(h,l,b,PellPeople.TILLY);
            h.assertTrue(PellTilly.readerAt(b)==null&&!t.isInvisible()&&thing(l,b,at.getX(),at.getY(),at.getZ(),COAT_HOOK,0),"Tilly the house's again, her cane back on its hook");
            h.assertTrue(f.own(p,P).getBoolean(PellTilly.PLAYS),"and the reader who took it is still Tilly for when they come back");
        }).thenSucceed();
    }
    @AfterBatch(batch="pell_tilly_cane") public static void tillyCaneDone(ServerLevel l){done();}

    /**
     * 0.4.545, the name on the panel. Two readers who have had the first two visits, one of them Tilly. The last night locked up and June
     * in the shallow end holding Tilly: the reader as Tilly is in her hands, where Tilly is. Each time June says the name, the reader as
     * Tilly is asked on the panel, and only they are: the name June wants, their own, or nothing. They give their own; June goes on, and
     * presses them under with her. Let up and asked again, they answer to the name: it is heard as when Tilly answers, June says the line
     * she keeps for a reader as Tilly, takes them home and puts them to bed in Nessa's bed. At first light June stands them at Nessa's window;
     * their ending lands with nothing to read, and it is not the older sibling's.
     */
    @GameTest(template="empty",batch="pell_tilly_name",timeoutTicks=9000)
    public static void aReaderAsTillyInJunesHandsIsAskedOnThePanelAndAnswersOrNot(GameTestHelper h){
        var f=house(h);var p=f.player("pell_tilly_held",P);var q=f.player("pell_tilly_deck",P);var l=f.level(P);var b=f.base(P);
        for(var r:new ServerPlayer[]{p,q}){keep(f,r,"FirstVisit");keep(f,r,"SecondVisit");}
        playsTilly(f,p,q);
        // A week in, they once answered to the name at bedtime.
        var before=f.own(p,P);before.putInt("AnsweredNessa",1);LiteraryVignettes.save(f.data(),p.getUUID(),P,before);
        f.at(p,P,.5,0,-44);f.at(q,P,-6.5,0,-36);
        int[] looked={-1};
        h.startSequence().thenWaitUntil(()->lastBegun(h,l,b))
        .thenExecute(()->{
            PellLastNight.lockedUp(l,b);PellRite.inWater(l,b);
            f.at(q,P,-9.5,0,-78.5);told(p);told(q);
        }).thenIdle(3).thenExecute(()->{
            var t=who(h,l,b,PellPeople.TILLY);
            h.assertTrue(p.getVehicle()==t&&PellTilly.small(p)&&t.isInvisible(),"the reader as Tilly in June's hands, where Tilly is: "+where(p,b));
            h.assertTrue(p.getY()<b.getY()-.5,"in the water: "+where(p,b));
        }).thenWaitUntil(()->h.assertTrue(Talk.key(p).equals(PellTilly.RITE_TALK),"June says the name, and the reader is asked: "+PellRite.lines(b)))
        // 0.4.568: the reader as Tilly is drawn with her hair as long as the last night has grown it.
        .thenWaitUntil(()->h.assertTrue(lookHair(p,p,looked)==PellPeople.HAIR_LONGEST,"the reader as Tilly's look has her hair at its longest: "+looked[0]))
        .thenExecute(()->{
            h.assertTrue(said(b,"June",PellTexts.JUNE_YOU_SAID),"answered to the name a week in, June reminds them of it first: "+PellRite.lines(b));
            h.assertTrue(Talk.replies(p).equals(List.of(PellTexts.ANSWER_YES,PellTexts.ANSWER_OWN,PellTexts.ANSWER_NOTHING)),"the name June wants, their own, or nothing: "+Talk.replies(p));
            h.assertTrue(!Talk.key(q).equals(PellTilly.RITE_TALK),"the other reader is asked nothing");
            h.assertTrue(times(b,"Tilly",PellTexts.TILLY_RITE_NAME)==0,"the house's Tilly answers nothing for them: "+PellRite.lines(b));
            Talk.choose(p,PellTexts.ANSWER_OWN);
            h.assertTrue(f.own(p,P).getInt("SaidOwnName")==1&&times(b,"Tilly",PellTexts.TILLY_RITE_NAME)==1,"they give their own name");
            h.assertTrue(PellRite.step(b)==PellRite.WATER&&PellRite.held(b),"and June goes on");
        }).thenWaitUntil(()->h.assertTrue(PellRite.under(b),"June presses them under"))
        .thenIdle(12).thenExecute(()->h.assertTrue(p.getEyeY()<b.getY()+PellWeek.WATERLINE&&p.getVehicle()!=null,"under the water with her: "+where(p,b)))
        .thenWaitUntil(()->h.assertTrue(!PellRite.under(b)&&said(b,"June",PellTexts.JUNE_AFTER_UNDER[0])&&Talk.replies(p).contains(PellTexts.ANSWER_YES),"let up, and asked again: "+PellRite.lines(b)))
        .thenExecute(()->{
            Talk.choose(p,PellTexts.ANSWER_YES);
            h.assertTrue(PellRite.step(b)>=PellRite.ANSWERED&&said(b,"Tilly",PellTexts.TILLY_ANSWERS),"they answer to it: "+PellRite.lines(b));
            h.assertTrue(f.own(p,P).getBoolean(PellTilly.ANSWERED)&&f.own(q,P).getBoolean("HeardTillyAnswer"),"and the other reader hears them");
        }).thenWaitUntil(()->h.assertTrue(said(b,"June",PellTexts.JUNE_ANSWERED_R),"June says the line she keeps for them: "+PellRite.lines(b)))
        .thenWaitUntil(()->h.assertTrue(PellRite.step(b)==PellRite.KEPT,"June takes them home: June at "+who(h,l,b,PellPeople.JUNE).position().subtract(b.getX(),b.getY(),b.getZ())))
        .thenIdle(2).thenExecute(()->{
            h.assertTrue(p.isSleeping()&&p.getVehicle()==null&&told(p).contains(PellTexts.TILLY_R_KEPT),"put to bed in Nessa's bed: "+where(p,b));
            f.at(q,P,.5,0,-50.5);toTheStreet(q);
            PellDawn.nearlyLight(b);
        }).thenWaitUntil(()->h.assertTrue(PellDawn.atWindow(b),"first light: June stands them at Nessa's window"))
        .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Ended")&&f.own(q,P).getBoolean("Ended"),"each reader's ending lands"))
        .thenExecute(()->{
            h.assertTrue(standing(p,b,PellDawn.TILLY_WINDOW,.4)&&!p.isSleeping(),"at the window: "+where(p,b));
            var own=f.own(p,P);
            h.assertTrue(own.getString("Outcome").equals("answered_to_nessa")&&!own.getBoolean("TillyStayed")&&!own.getBoolean("Ready"),"answered to the name: nothing to read, and not the older sibling's ending: "+own.getString("Outcome"));
            var o=f.own(q,P);
            h.assertTrue(o.getString("Outcome").equals("tilly_answered")&&o.getBoolean("TillyStayed")&&!o.getBoolean("Ready"),"Tilly stayed, for the other: "+o.getString("Outcome"));
        }).thenSucceed();
    }
    @AfterBatch(batch="pell_tilly_name") public static void tillyNameDone(ServerLevel l){done();}

    /**
     * 0.4.545, a hand for a reader as Tilly. Two readers who have had the first two visits, one of them Tilly; the last night locked up, a
     * gap scrubbed in the line on its west side by the pool, and June in the shallow end holding the reader as Tilly. The other reader's hand
     * on them is a hand on Tilly: June's hands come away, the reader as Tilly is let down into the water and told whose hand it is, and
     * what Tilly would ask comes to them in grey. At the unbroken line the reader as Tilly is put back off the paint; through the gap together
     * they are out, and each keeps it. At first light the account is ready for both: the reader as Tilly came out with their own name, and
     * the other brought them out. Each Witness line is their own.
     */
    @GameTest(template="empty",batch="pell_tilly_out",timeoutTicks=6000)
    public static void anotherReadersHandTakesTheReaderAsTillyOutThroughTheGap(GameTestHelper h){
        var f=house(h);var p=f.player("pell_tilly_led",P);var q=f.player("pell_tilly_hand",P);var l=f.level(P);var b=f.base(P);
        for(var r:new ServerPlayer[]{p,q}){keep(f,r,"FirstVisit");keep(f,r,"SecondVisit");}
        playsTilly(f,p,q);
        f.at(p,P,.5,0,-44);f.at(q,P,-6.5,0,-36);
        h.startSequence().thenWaitUntil(()->lastBegun(h,l,b))
        .thenExecute(()->{
            PellLastNight.lockedUp(l,b);PellRite.inWater(l,b);
            scrubbedOut(l,b,PellSite.LINE_W,-83);scrubbedOut(l,b,PellSite.LINE_W,-82);
        }).thenIdle(3).thenExecute(()->{
            var t=who(h,l,b,PellPeople.TILLY);
            h.assertTrue(p.getVehicle()==t&&PellRite.held(b),"the reader as Tilly in June's hands");
            f.at(q,P,t.getX()-b.getX()+1.2,-1,t.getZ()-b.getZ());told(p);told(q);
            handOn(q,p);
        }).thenIdle(1).thenExecute(()->{
            h.assertTrue(q.getUUID().equals(PellRite.leader(b))&&!PellRite.held(b),"the other reader's hand on them is a hand on Tilly");
            h.assertTrue(told(q).contains(PellTexts.TILLY_TAKES_R)&&f.own(q,P).getBoolean("TookTillysHand"),"and they are told so");
            var heard=told(p);
            h.assertTrue(heard.contains(PellTexts.TILLY_R_HAND)&&heard.contains(PellTexts.mightSay(PellTexts.TILLY_WHO))&&f.own(p,P).getString(PellTilly.HAND).equals(q.getGameProfile().getName()),
                "the reader as Tilly is told of the hand, keeps whose it was, and has Tilly's question in grey: "+heard);
            h.assertTrue(p.getVehicle()==null,"let down into the water");
            h.assertTrue(times(b,"Tilly",PellTexts.TILLY_WHO)==0,"the house's Tilly asks nothing for them: "+PellRite.lines(b));
            // Out of the water and toward the line where it is whole.
            f.at(q,P,-18.5,0,-79.5);f.at(p,P,-15.5,0,-79.5);
        }).thenIdle(2).thenExecute(()->{told(p);f.at(p,P,PellSite.LINE_W+.5,0,-79.5);})
        .thenIdle(2).thenExecute(()->{
            h.assertTrue(where(p,b).x>PellSite.LINE_W+1-.01&&told(p).contains(PellTexts.TILLY_R_PAINT),"a bare foot on the unbroken paint is put back off it: "+where(p,b));
            h.assertTrue(PellRite.step(b)==PellRite.WATER&&q.getUUID().equals(PellRite.leader(b)),"not out, the hand still theirs");
            // Along to the gap, and through it together.
            told(q);f.at(q,P,-18.5,0,-82.5);f.at(p,P,PellSite.LINE_W+.5,0,-82.5);
        }).thenIdle(2).thenExecute(()->{
            h.assertTrue(PellRite.step(b)==PellRite.OUT,"through the gap together, out: "+where(p,b));
            h.assertTrue(f.own(p,P).getBoolean("CameOut")&&f.own(p,P).getString("OutWith").equals(q.getGameProfile().getName())&&told(p).contains(PellTexts.TILLY_R_OUT),"the reader as Tilly is out, and keeps who with");
            h.assertTrue(f.own(q,P).getBoolean("GotTillyOut")&&told(q).contains(PellTexts.TILLY_OUT_R),"the other brought them out");
            f.at(p,P,PellDawn.TILLY_BY_GAIL.x,0,PellDawn.TILLY_BY_GAIL.z);f.at(q,P,1.5,0,-50.5);toTheStreet(p,q);
            PellDawn.nearlyLight(b);
        }).thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Dawned")&&f.own(q,P).getBoolean("Dawned"),"the morning reaches each reader"))
        .thenExecute(()->{
            var own=f.own(p,P);var o=f.own(q,P);
            h.assertTrue(own.getBoolean("Ready")&&own.getString("Outcome").equals("came_out_with_own_name"),"the reader as Tilly came out with their own name: "+own.getString("Outcome"));
            h.assertTrue(o.getBoolean("Ready")&&o.getString("Outcome").equals("brought_tilly_out_with_her_name"),"the other brought them out: "+o.getString("Outcome"));
            var text=words(PellTexts.account(p.getGameProfile().getName(),own));
            h.assertTrue(text.contains("I took my cane off the hook")&&text.contains(q.getGameProfile().getName()+" took my hand under the water")&&text.contains("We went across the grass to a gap in the paint")&&text.endsWith("Behind us nobody was calling any name at all."),"Tilly's own account: "+text);
            h.assertTrue(PellTexts.witness(own,"").startsWith("I stayed three times in a house where a woman called me by her daughter's name")&&PellTexts.witness(own,"").endsWith("came out through a gap in the line."),"Tilly's own Witness line");
            h.assertTrue(PellTexts.witness(o,"").contains("Tilly would not answer to any name but Tilly"),"the other's line says Tilly, not she");
        }).thenSucceed();
    }
    @AfterBatch(batch="pell_tilly_out") public static void tillyOutDone(ServerLevel l){done();}

    /**
     * 0.4.545, holding out. Two readers who have had the first two visits, one of them Tilly; the last night locked up and June in the shallow
     * end holding the reader as Tilly, and nobody takes their hand. They never answer to the name, and the night goes on to first light:
     * June's hands come away and she stops calling. At first light the reader as Tilly kept their own name until it was light, and the
     * account is ready for them; the other, who did not get them out, has nothing resolved.
     */
    @GameTest(template="empty",batch="pell_tilly_held",timeoutTicks=4000)
    public static void aReaderAsTillyWhoNeverAnswersHoldsOutUntilLight(GameTestHelper h){
        var f=house(h);var p=f.player("pell_tilly_kept",P);var q=f.player("pell_tilly_yard",P);var l=f.level(P);var b=f.base(P);
        for(var r:new ServerPlayer[]{p,q}){keep(f,r,"FirstVisit");keep(f,r,"SecondVisit");}
        playsTilly(f,p,q);
        f.at(p,P,.5,0,-44);f.at(q,P,-6.5,0,-36);
        h.startSequence().thenWaitUntil(()->lastBegun(h,l,b))
        .thenExecute(()->{PellLastNight.lockedUp(l,b);PellRite.inWater(l,b);f.at(q,P,-6.5,0,-70.5);})
        .thenWaitUntil(()->h.assertTrue(Talk.key(p).equals(PellTilly.RITE_TALK),"June says the name"))
        .thenExecute(()->{
            h.assertTrue(PellDawn.dawn(b)==PellDawn.NOT_YET&&PellRite.step(b)==PellRite.WATER,"still night, still in the water");
            told(p);told(q);PellDawn.nearlyLight(b);
        }).thenWaitUntil(()->h.assertTrue(PellRite.step(b)==PellRite.HELD_OUT,"never answered, they hold out until it is light"))
        .thenIdle(1).thenExecute(()->{
            h.assertTrue(f.own(p,P).getBoolean("HeldOut")&&told(p).contains(PellTexts.TILLY_R_HELD_OUT)&&!Talk.key(p).equals(PellTilly.RITE_TALK),"June's hands come away from them");
            h.assertTrue(told(q).contains(PellTexts.JUNE_STOPPED),"the other, out on the lot, hears her stop");
            h.assertTrue(p.getVehicle()==null,"let go in the water");
        }).thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Dawned")&&f.own(q,P).getBoolean("Dawned"),"the morning reaches each reader"))
        .thenExecute(()->{
            var own=f.own(p,P);var o=f.own(q,P);
            h.assertTrue(own.getBoolean("Ready")&&own.getString("Outcome").equals("kept_own_name_until_light"),"the reader as Tilly kept their own name until light: "+own.getString("Outcome"));
            h.assertTrue(!o.getBoolean("Ready")&&o.getString("Outcome").equals("tilly_held_out_alone"),"the other has nothing resolved: "+o.getString("Outcome"));
            h.assertTrue(PellTexts.witness(own,"").endsWith("I kept my own until it was light."),"Tilly's own Witness line");
            h.assertTrue(words(PellTexts.account(p.getGameProfile().getName(),own)).contains("I kept my own name until it got light."),"and account");
        }).thenSucceed();
    }
    @AfterBatch(batch="pell_tilly_held") public static void tillyHeldDone(ServerLevel l){done();}

    // ------------------------------------------------------------------------------------------------ 0.4.546, Tilly's senses
    /** What Tilly's senses sent this reader since last asked, and the sounds they were sent. Reading empties it. */
    private record Senses(List<io.github.knaitoe.theoldesthouse.network.PellSensePayload> sent,List<net.minecraft.sounds.SoundEvent> sounds){
        List<io.github.knaitoe.theoldesthouse.network.PellSensePayload> of(int kind){var out=new java.util.ArrayList<io.github.knaitoe.theoldesthouse.network.PellSensePayload>();for(var s:sent)if(s.kind()==kind)out.add(s);return out;}
    }
    private static Senses senses(ServerPlayer p){
        var sent=new java.util.ArrayList<io.github.knaitoe.theoldesthouse.network.PellSensePayload>();var sounds=new java.util.ArrayList<net.minecraft.sounds.SoundEvent>();
        for(var packet:NativeTestPlayers.packets(p)){
            if(packet instanceof net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket custom&&custom.payload() instanceof io.github.knaitoe.theoldesthouse.network.PellSensePayload s)sent.add(s);
            else if(packet instanceof net.minecraft.network.protocol.game.ClientboundSoundPacket sound)sounds.add(sound.getSound().value());
        }
        return new Senses(sent,sounds);
    }
    /** Whether an outline holds a line matching this. */
    private static boolean outlined(List<Float> lines,java.util.function.Predicate<float[]> match){
        for(int i=0;i+5<lines.size();i+=6){var seg=new float[6];for(int k=0;k<6;k++)seg[k]=lines.get(i+k);if(match.test(seg))return true;}
        return false;
    }
    /** The reader's cane, used: as the use key sends it. */
    private static void tap(ServerLevel l,ServerPlayer p){p.getMainHandItem().getItem().use(l,p,net.minecraft.world.InteractionHand.MAIN_HAND);}
    /**
     * 0.4.546, the cane (docs/PELL_HOUSE_SENSES_0_4_546.md). Two readers on their first visit, one of them Tilly. The long cane is in the
     * reader as Tilly's hand and not the other's. Tapped just inside the front door: everybody near hears a cane, and only the reader as
     * Tilly is shown what is in reach, the front door's frame among it; tapped again at once, nothing more. On the curb, its edges; on the
     * front step, the line under the mat. It cannot be dropped; put away, it comes back to them; gone from the house, they have it no
     * longer.
     */
    @GameTest(template="empty",batch="pell_senses_cane",timeoutTicks=4000)
    public static void aReaderAsTillyHasTheCaneAndItsTapShowsThemWhatIsInReach(GameTestHelper h){
        var f=house(h);var p=f.player("pell_senses_tilly",P);var q=f.player("pell_senses_older",P);var l=f.level(P);var b=f.base(P);
        playsTilly(f,p,q);
        p.getInventory().setItem(p.getInventory().selected,net.minecraft.world.item.ItemStack.EMPTY);
        f.at(p,P,.5,0,-44);f.at(q,P,-6.5,0,-36);
        h.startSequence().thenWaitUntil(()->{for(int i=0;i<PellPeople.COUNT;i++)h.assertTrue(PellPeople.find(l,b,i)!=null,"waiting for "+PellPeople.name(i));})
        .thenIdle(10).thenExecute(()->{
            h.assertTrue(p.getUUID().equals(PellTilly.readerAt(b)),"the reader is Tilly here");
            h.assertTrue(PellSenses.hasCane(p)&&PellSenses.marked(p.getMainHandItem()),"the cane in their hand");
            h.assertTrue(!PellSenses.hasCane(q),"and not in the other's");
            // Just inside the front door, the other beside them.
            f.at(p,P,.5,0,-59.5);f.at(q,P,1.5,0,-60.5);senses(p);senses(q);
            tap(l,p);tap(l,p);
        }).thenIdle(1).thenExecute(()->{
            var mine=senses(p);var theirs=senses(q);var shown=mine.of(io.github.knaitoe.theoldesthouse.network.PellSensePayload.OUTLINE);
            h.assertTrue(shown.size()==1,"one tap shown, the second too soon after it: "+shown.size());
            h.assertTrue(theirs.sounds().contains(PellRegistry.CANE_TAP.get())&&mine.sounds().contains(PellRegistry.CANE_TAP.get()),"everybody near hears the cane");
            h.assertTrue(theirs.sent().isEmpty(),"and only the reader as Tilly is shown anything");
            double z=b.getZ()+PellSite.FRONT_DOOR.getZ()+.5;
            h.assertTrue(outlined(shown.get(0).lines(),s->Math.abs(s[2]-z)<.01&&Math.abs(s[5]-z)<.01&&Math.abs(s[4]-s[1])>1.9),"the front door's frame is outlined: "+shown.get(0).lines().size()/6+" lines");
            // On the curb.
            f.at(p,P,4.5,0,-20.5);
        }).thenIdle(PellSenses.TAP_EVERY).thenExecute(()->{senses(p);tap(l,p);})
        .thenIdle(1).thenExecute(()->{
            var shown=senses(p).of(io.github.knaitoe.theoldesthouse.network.PellSensePayload.OUTLINE);
            double x=b.getX()+PellSite.CURB;
            h.assertTrue(shown.size()==1&&outlined(shown.get(0).lines(),s->Math.abs(s[0]-x)<.01&&Math.abs(s[3]-x)<.01&&Math.abs(s[1]-(b.getY()+.02))<.01),"the curb's edge is outlined");
            // On the front step, by the mat.
            f.at(p,P,.5,0,-55.5);
        }).thenIdle(PellSenses.TAP_EVERY).thenExecute(()->{senses(p);tap(l,p);})
        .thenIdle(1).thenExecute(()->{
            var shown=senses(p).of(io.github.knaitoe.theoldesthouse.network.PellSensePayload.OUTLINE);
            double y=b.getY()+.03,z0=b.getZ()+PellSite.LINE_S,z1=z0+1;
            h.assertTrue(shown.size()==1&&outlined(shown.get(0).lines(),s->Math.abs(s[1]-y)<.01&&s[2]>=z0-.01&&s[2]<=z1+.01),"the line underfoot is outlined");
            // It cannot be dropped.
            p.drop(true);
            h.assertTrue(PellSenses.hasCane(p)&&PellSenses.marked(p.getMainHandItem()),"dropped, it stays in their hand");
            // Put away out of their hands, it comes back.
            p.getInventory().setItem(p.getInventory().selected,net.minecraft.world.item.ItemStack.EMPTY);
            h.assertTrue(!PellSenses.hasCane(p),"put away");
        }).thenIdle(6).thenExecute(()->{
            h.assertTrue(PellSenses.hasCane(p)&&PellSenses.marked(p.getMainHandItem()),"it comes back to their hand");
            // Gone from the house.
            p.teleportTo(l,b.getX()+.5,b.getY()+60,b.getZ()-60,0,0);
        }).thenIdle(12).thenExecute(()->h.assertTrue(!PellSenses.hasCane(p),"gone from the house, the cane is back on its hook"))
        .thenSucceed();
    }
    @AfterBatch(batch="pell_senses_cane") public static void sensesCaneDone(ServerLevel l){done();}

    /**
     * 0.4.546, what only Tilly hears and feels, by day a week in. Two readers, one of them Tilly. In the hall near the laundry the freezer's
     * hum is sent to the reader as Tilly alone; at the freezer they have followed it. In the yard, the tape hiss from the shed, to them
     * alone. Each time they step onto the line, its cold falls over them, counted; the other, on it too, feels nothing.
     */
    @GameTest(template="empty",batch="pell_senses_hear",timeoutTicks=4000)
    public static void onlyTheReaderAsTillyHearsTheFreezerAndTheTapesAndFeelsTheLine(GameTestHelper h){
        var f=house(h);var p=f.player("pell_senses_hears",P);var q=f.player("pell_senses_deaf",P);var l=f.level(P);var b=f.base(P);
        for(var r:new ServerPlayer[]{p,q})keep(f,r,"FirstVisit");
        playsTilly(f,p,q);
        f.at(p,P,.5,0,-44);f.at(q,P,-6.5,0,-36);
        var heard=new java.util.ArrayList<net.minecraft.sounds.SoundEvent>();var other=new java.util.ArrayList<net.minecraft.sounds.SoundEvent>();
        h.startSequence().thenWaitUntil(()->weekBegun(h,l,b))
        .thenExecute(()->{
            f.at(p,P,4.5,0,-65.5);f.at(q,P,3.5,0,-65.5);senses(p);senses(q);
        }).thenWaitUntil(()->{heard.addAll(senses(p).sounds());other.addAll(senses(q).sounds());h.assertTrue(heard.contains(PellRegistry.HUM.get()),"the freezer's hum, to the reader as Tilly");})
        .thenExecute(()->{
            h.assertTrue(!other.contains(PellRegistry.HUM.get()),"and not to the other beside them");
            h.assertTrue(f.own(p,P).getBoolean(PellSenses.HEARD_HUM)&&!f.own(p,P).getBoolean(PellSenses.FOLLOWED_HUM),"heard, not yet followed");
            f.at(p,P,8.5,0,-67.9);
        }).thenIdle(2).thenExecute(()->{
            h.assertTrue(f.own(p,P).getBoolean(PellSenses.FOLLOWED_HUM),"at the freezer, they have followed it");
            // Out in the yard, by the shed.
            heard.clear();other.clear();f.at(p,P,7.5,0,-87.5);f.at(q,P,6.5,0,-87.5);senses(p);senses(q);
        }).thenWaitUntil(()->{heard.addAll(senses(p).sounds());other.addAll(senses(q).sounds());h.assertTrue(heard.contains(PellRegistry.HISS.get()),"tape hiss from the shed, to the reader as Tilly");})
        .thenExecute(()->{
            h.assertTrue(!other.contains(PellRegistry.HISS.get()),"and not to the other");
            // The line, at the front step.
            f.at(p,P,.5,0,-56.5);p.setOnGround(true);f.at(q,P,1.5,0,-56.5);q.setOnGround(true);senses(p);senses(q);
        }).thenIdle(2).thenExecute(()->{
            h.assertTrue(senses(p).of(io.github.knaitoe.theoldesthouse.network.PellSensePayload.COLD).size()==1&&f.own(p,P).getInt(PellSenses.LINE_COLD)==1,"onto the line, its cold falls over them");
            h.assertTrue(senses(q).sent().isEmpty(),"the other, on it too, feels nothing");
            f.at(p,P,.5,0,-54.5);p.setOnGround(true);
        }).thenIdle(2).thenExecute(()->{
            h.assertTrue(senses(p).sent().isEmpty(),"off it, nothing");
            f.at(p,P,-1.5,0,-56.5);p.setOnGround(true);
        }).thenIdle(2).thenExecute(()->h.assertTrue(senses(p).of(io.github.knaitoe.theoldesthouse.network.PellSensePayload.COLD).size()==1&&f.own(p,P).getInt(PellSenses.LINE_COLD)==2,"onto it again, again"))
        .thenSucceed();
    }
    @AfterBatch(batch="pell_senses_hear") public static void sensesHearDone(ServerLevel l){done();}

    /**
     * 0.4.546, at night and on the telephone. Two readers on their first visit, one of them Tilly. In the small hours, in Nessa's room,
     * singing through the wall at the head of the bed comes to the reader as Tilly, and not to the other in the hall outside. In the
     * morning, Gail rings; while June is on the telephone, between her lines, she says four things low that only the reader as Tilly hears,
     * in the house, in order, kept in their journal's record of what was said; the other, in the kitchen, hears none of them, and the call
     * goes on.
     */
    @GameTest(template="empty",batch="pell_senses_night",timeoutTicks=9000)
    public static void atNightSingingThroughTheWallAndOnTheTelephoneJuneQuietly(GameTestHelper h){
        var f=house(h);var p=f.player("pell_senses_bed",P);var q=f.player("pell_senses_hall",P);var l=f.level(P);var b=f.base(P);
        playsTilly(f,p,q);
        f.at(p,P,.5,0,-44);f.at(q,P,-6.5,0,-36);
        var heard=new java.util.ArrayList<net.minecraft.sounds.SoundEvent>();var other=new java.util.ArrayList<net.minecraft.sounds.SoundEvent>();
        var hushed=new java.util.ArrayList<String>();
        h.startSequence().thenWaitUntil(()->{for(int i=0;i<PellPeople.COUNT;i++)h.assertTrue(PellPeople.find(l,b,i)!=null,"waiting for "+PellPeople.name(i));h.assertTrue(PellVisit.car(l,b)!=null,"waiting for the visit to begin");})
        .thenExecute(()->{
            PellMorning.smallHours(l,b);PellKnife.skip(b);
            f.at(p,P,-10.5,0,-60.5);f.at(q,P,-9.5,0,-65.5);senses(p);senses(q);
        }).thenWaitUntil(()->{heard.addAll(senses(p).sounds());other.addAll(senses(q).sounds());h.assertTrue(heard.contains(PellRegistry.SINGING.get()),"singing through the wall, to the reader as Tilly: next at "+PellSenses.singNext(b));})
        .thenExecute(()->{
            h.assertTrue(!other.contains(PellRegistry.SINGING.get()),"and not to the other in the hall outside");
            PellCall.afterBreakfast(l,b);
            f.at(p,P,-3.5,0,-65.5);f.at(q,P,-9.5,0,-67.5);senses(p);senses(q);
        }).thenWaitUntil(()->h.assertTrue(PellCall.stage(b)==PellCall.ON_PHONE,"June on the telephone"))
        .thenWaitUntil(()->{
            for(var s:senses(p).of(io.github.knaitoe.theoldesthouse.network.PellSensePayload.HUSH))hushed.add(s.words());
            h.assertTrue(senses(q).sent().isEmpty(),"the other hears none of it");
            h.assertTrue(hushed.size()>=PellSenses.HUSHED,"June's quiet lines: "+hushed);
        })
        .thenExecute(()->{
            h.assertTrue(hushed.equals(List.of(PellTexts.JUNE_QUIETLY_FIRST)),"in order: "+hushed);
            h.assertTrue(f.own(p,P).getInt(PellSenses.HEARD_HUSH)==PellSenses.HUSHED,"heard, all four");
            var talks=TalkHistory.conversations(p.server,p.getUUID()).toString();
            h.assertTrue(talks.contains(PellTexts.JUNE_QUIETLY_FIRST[0])&&!TalkHistory.conversations(q.server,q.getUUID()).toString().contains(PellTexts.JUNE_QUIETLY_FIRST[0]),"kept in their record of what was said, and not the other's");
        }).thenWaitUntil(()->h.assertTrue(PellCall.stage(b)==PellCall.OFFERED,"and the call goes on"))
        .thenSucceed();
    }
    @AfterBatch(batch="pell_senses_night") public static void sensesNightDone(ServerLevel l){done();}

    // ------------------------------------------------------------------------------------------------ 0.4.547, a house to each pair
    /** A reader not yet at the house: in the House's own halls, far from any Pell house. */
    private static ServerPlayer elsewhere(LiteraryTests.Fixture f,String name){
        var p=NativeTestPlayers.survival(f.h,name);f.players.add(p);p.stopRiding();p.teleportTo(f.in,0,90,0,0,0);p.setDeltaMovement(Vec3.ZERO);p.hasChangedDimension();return p;
    }
    /** A reader come through the door into the house standing at this base: on its lot, there for the house and for its story. */
    private static void into(LiteraryTests.Fixture f,ServerPlayer p,BlockPos base,double x,double z){
        p.stopRiding();p.teleportTo(f.level(P),base.getX()+x,base.getY(),base.getZ()+z,180,0);p.setDeltaMovement(Vec3.ZERO);p.hasChangedDimension();p.setNoGravity(true);
        LiteraryVignettes.onArrive(p,P);VignetteThreads.arrived(p,P);
    }
    /** A reader's Witness book, its pages run together. */
    private static String witnessBook(LiteraryTests.Fixture f,ServerPlayer p){return words(WitnessAccount.book(f.data(),p.getUUID(),p.getGameProfile().getName(),false));}

    /**
     * 0.4.547, a house to each pair (docs/PELL_HOUSE_PAIRS_0_4_547.md; the owner: "the house should generate a second instance for a third
     * and fourth player"). Five readers come to the house's door one after another. The first takes the house; the second, through the
     * door behind them before they have arrived, joins them, and each is the other's pair; the third's door waits while a second house is
     * made ready, then opens onto it; the fourth joins the third; the fifth waits for a house of their own. Each house is where its readers
     * are, built as the first, with its own people; a story is shared only within one house; and every account and Witness line names the
     * reader's own pair, by their part in the house, and nobody from the other house.
     */
    @GameTest(template="empty",batch="pell_pairs",timeoutTicks=12000)
    public static void aThirdAndFourthReaderHaveAHouseOfTheirOwnAndEachAccountNamesItsOwnPair(GameTestHelper h){
        var f=house(h);var l=f.level(P);var b=f.base(P);var s=l.getServer();f.build(P);
        var a=elsewhere(f,"pell_pair_a");var o=elsewhere(f,"pell_pair_b");var c=elsewhere(f,"pell_pair_c");var e=elsewhere(f,"pell_pair_d");var fifth=elsewhere(f,"pell_pair_e");
        var second=new BlockPos[1];var story=WitnessAccount.Story.of("pell_house");
        h.startSequence().thenExecute(()->{
            h.assertTrue(SceneInstances.copyable(P),"the Pell house has copies");
            var first=SceneInstances.enter(a,P);
            h.assertTrue(first.door()!=null&&first.copy()==0&&SceneInstances.kept(f.data(),a.getUUID(),P)==0,"the first reader takes the house");
            // Through the door behind them before they have arrived: the second joins them.
            var join=SceneInstances.enter(o,P);
            h.assertTrue(join.door()!=null&&join.copy()==0,"the second reader, through the same door a moment later, comes into the same house");
            h.assertTrue(o.getUUID().equals(SceneInstances.siblingOf(f.data(),a.getUUID()))&&a.getUUID().equals(SceneInstances.siblingOf(f.data(),o.getUUID())),"each is the other's pair");
            into(f,a,b,.5,-44);into(f,o,b,-1.5,-44);
            var wait=SceneInstances.enter(c,P);
            h.assertTrue(wait.door()==null&&wait.waiting()&&SceneInstances.copies(f.data(),P)==1,"a third reader finds both places in the house taken, and waits while a house of their own is made ready");
        })
        .thenWaitUntil(()->h.assertTrue(SceneInstances.built(f.data(),P,1),"a second house stands"))
        .thenExecute(()->{second[0]=SceneInstances.base(s,P,1);f.chunks.hold(l,PellPeople.area(second[0]));})
        .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"its street is resident"))
        .thenExecute(()->{
            var house=SceneInstances.enter(c,P);
            h.assertTrue(house.door()!=null&&house.copy()==1&&house.door().id.equals(SceneInstances.doorId(P,1,"entry")),"the third reader's door opens onto a house of their own");
            into(f,c,second[0],.5,-44);
            var four=SceneInstances.enter(e,P);
            h.assertTrue(four.door()!=null&&four.copy()==1&&c.getUUID().equals(SceneInstances.siblingOf(f.data(),e.getUUID())),"the fourth joins the third");
            into(f,e,second[0],-1.5,-44);
            var five=SceneInstances.enter(fifth,P);
            h.assertTrue(five.door()==null&&five.waiting()&&SceneInstances.copies(f.data(),P)==2,"a fifth has a house of their own again, still being made ready");
            h.assertTrue(b.equals(LiteraryVignettes.base(a,P))&&b.equals(LiteraryVignettes.base(o,P))&&second[0].equals(LiteraryVignettes.base(c,P))&&second[0].equals(LiteraryVignettes.base(e,P)),"each reader's house is the one they are at");
            h.assertTrue(LiteraryVignettes.current(c)==P&&SceneInstances.at(s,P,c.level().dimension(),c.position())==1,"the House knows the second house for the Pell house");
            h.assertTrue(PellBlock.is(l.getBlockState(second[0].offset(PellSite.FRONT_DOOR)),PellBlock.Kind.SCREEN)&&PellBlock.is(l.getBlockState(second[0].offset(PellSite.SOURCE)),PellBlock.Kind.LETTERBOX),"built as the first, its screen door and its letterbox");
            var t0=VignetteThreads.threadId(f.data(),P,a.getUUID());var t1=VignetteThreads.threadId(f.data(),P,c.getUUID());
            h.assertTrue(t0!=null&&t0.equals(VignetteThreads.threadId(f.data(),P,o.getUUID()))&&t1!=null&&t1.equals(VignetteThreads.threadId(f.data(),P,e.getUUID()))&&!t0.equals(t1),"each pair shares one story, and the two houses do not share one: "+t0+" "+t1);
        })
        .thenWaitUntil(()->{for(var base:List.of(b,second[0]))for(int i=0;i<PellPeople.COUNT;i++)h.assertTrue(PellPeople.find(l,base,i)!=null,"waiting for "+PellPeople.name(i)+" at "+base.toShortString());})
        .thenIdle(10).thenExecute(()->{
            h.assertTrue(!PellPeople.find(l,b,PellPeople.JUNE).getUUID().equals(PellPeople.find(l,second[0],PellPeople.JUNE).getUUID()),"each house has its own June");
            // Who each was at the house with: their own pair, and nobody from the other house.
            h.assertTrue(PellTexts.others(f.own(c,P)).equals(List.of("pell_pair_d"))&&PellTexts.others(f.own(e,P)).equals(List.of("pell_pair_c")),"the second house's readers were there with each other: "+PellTexts.others(f.own(c,P))+" "+PellTexts.others(f.own(e,P)));
            h.assertTrue(PellTexts.others(f.own(a,P)).equals(List.of("pell_pair_b")),"and the first house's with each other: "+PellTexts.others(f.own(a,P)));
            // In the first house the first reader took the cane and was Tilly; in the second, nobody took it.
            playsTilly(f,a,o);
            h.assertTrue(PellTexts.others(f.own(o,P)).isEmpty(),"the reader who was Tilly is Tilly to the other, not another older one");
            var mine=f.own(c,P);mine.putInt("Rules",PellTexts.RULES);LiteraryVignettes.save(f.data(),c.getUUID(),P,mine);
            String tilly=words(PellTexts.account("pell_pair_a",f.own(a,P))),older=words(PellTexts.account("pell_pair_b",f.own(o,P))),third=words(PellTexts.account("pell_pair_c",f.own(c,P)));
            h.assertTrue(tilly.contains("June gave pell_pair_b the garage across the yard."),"Tilly's account names who had the garage: "+tilly);
            h.assertTrue(older.contains("June Pell took us in, Tilly and me."),"the older one's names nobody else as taken in: "+older);
            h.assertTrue(third.contains("June Pell took us in: Tilly, pell_pair_d and me.")&&third.contains("pell_pair_d and I were in the garage, for our privacy."),"two older ones were taken in together: "+third);
            for(var text:List.of(tilly,older))h.assertTrue(!text.contains("pell_pair_c")&&!text.contains("pell_pair_d")&&!text.contains("pell_pair_e"),"the first house's accounts name nobody from the second: "+text);
            h.assertTrue(!third.contains("pell_pair_a")&&!third.contains("pell_pair_b"),"nor the second's anybody from the first");
            // A reader whose ending has landed, away and coming back for the house's aftermath, is never sent into a house a pair is
            // still playing: not their own, which their pair keeps, nor the other.
            var ended=f.own(a,P);ended.putBoolean("Ended",true);LiteraryVignettes.save(f.data(),a.getUUID(),P,ended);
            a.teleportTo(f.in,0,90,0,0,0);a.hasChangedDimension();
            var back=SceneInstances.enter(a,P);
            h.assertTrue(back.copy()!=0&&back.copy()!=1,"after their ending, the aftermath only in a house nobody keeps for a story under way: "+back);
            h.assertTrue(SceneInstances.kept(f.data(),a.getUUID(),P)==0,"and the house they keep is still the one they were at");
            // Their pair goes back into their own house, the other there or not.
            var home=SceneInstances.enter(o,P);
            h.assertTrue(home.door()!=null&&home.copy()==0,"the other half of a pair goes back into their house: "+home);
            // Each reads their account: the Witness line closes on their own pair, by their part in the house.
            WitnessAccount.resolve(a,story,"came_out_with_own_name");WitnessAccount.resolve(o,story,"brought_tilly_out_with_her_name");
            WitnessAccount.resolve(c,story,"there_when_tilly_came_out");WitnessAccount.resolve(e,story,"there_when_tilly_came_out");
            String wa=witnessBook(f,a),wo=witnessBook(f,o),wc=witnessBook(f,c),we=witnessBook(f,e);
            h.assertTrue(wa.contains("pell_pair_b was the older one, and had the garage across the yard."),"Tilly's Witness line names the older one: "+wa);
            h.assertTrue(wo.contains("pell_pair_a took the cane off the hook, and was Tilly."),"the older one's names who was Tilly: "+wo);
            h.assertTrue(wc.contains("pell_pair_d had the garage with me.")&&we.contains("pell_pair_c had the garage with me."),"two older ones name each other: "+wc+" | "+we);
            for(var text:List.of(wa,wo))h.assertTrue(!text.contains("pell_pair_c")&&!text.contains("pell_pair_d")&&!text.contains("in that street"),"nobody from the other house: "+text);
            for(var text:List.of(wc,we))h.assertTrue(!text.contains("pell_pair_a")&&!text.contains("pell_pair_b"),"nobody from the first house: "+text);
            // Once the other half has had their ending too and gone, nobody keeps the house for a story under way, and the first comes
            // back to the aftermath in their own house.
            var done=f.own(o,P);done.putBoolean("Ended",true);LiteraryVignettes.save(f.data(),o.getUUID(),P,done);
            o.teleportTo(f.in,0,90,0,0,0);o.hasChangedDimension();
            var after=SceneInstances.enter(a,P);
            h.assertTrue(after.door()!=null&&after.copy()==0,"the aftermath in their own house: "+after);
        }).thenSucceed();
    }
    @AfterBatch(batch="pell_pairs") public static void pairsDone(ServerLevel l){done();}

    // ------------------------------------------------------------------------------------------------ 0.4.548, Gail's third mark
    /** Two readers in place for the last night's framing: one in Nessa's room facing its front wall, one out in the back yard facing the
     *  back fence. Neither can see the hall; both are near enough to hear it. */
    private static void outOfSight(LiteraryTests.Fixture f,ServerPlayer inside,ServerPlayer yard){
        f.at(inside,P,-11.5,0,-60.5);inside.setYRot(0);inside.setXRot(0);inside.setYHeadRot(0);
        f.at(yard,P,5.5,0,-80.5);yard.setYRot(180);yard.setXRot(0);yard.setYHeadRot(180);
    }
    /** Two readers beside Gail at the foot of the path, a step either side of her. */
    private static void besideGail(GameTestHelper h,LiteraryTests.Fixture f,ServerLevel l,BlockPos b,ServerPlayer one,ServerPlayer two){
        var g=who(h,l,b,PellPeople.GAIL);
        f.at(one,P,g.getX()-b.getX()+1,0,g.getZ()-b.getZ());f.at(two,P,g.getX()-b.getX()-1,0,g.getZ()-b.getZ());
    }
    /**
     * 0.4.548, Gail's third mark (docs/PELL_HOUSE_MARK_0_4_548.md), taken away. Two readers who have had the first two visits: one on
     * Gail's file twice (the knife and June's hand), the other on it never. The last night locked up, at eleven, June gets up and goes
     * along the hall to Nessa's school photographs; the first moment nobody sees her or them, glass breaks. The last photograph hangs
     * crooked, its glass starred. The one on her file twice is on it a third time, for this, and a line they never wrote is due in their
     * journal; both hear the glass, in the house and from the yard. June rings Gail from the kitchen, and the hour holds at eleven. Gail's
     * car comes up the street and she comes to the foot of the path. A hand on her: to the one June named, what June told her; to the
     * other, whom she has come for; neither has anything to show her, and both say nothing. When she has waited, the horn: the one June
     * named goes in her car, has their ending with nothing resolved, and is set down at the House's door. The other keeps who was taken.
     * Gail and her car go the first moment nobody sees them, and the night goes on. The other's account and Witness close keep it.
     */
    @GameTest(template="empty",batch="pell_mark_taken",timeoutTicks=6000)
    public static void onGailsFileTwiceAReaderIsFramedForTheLastPhotographAndGailTakesThemInTheNight(GameTestHelper h){
        var f=house(h);var p=f.player("pell_mark_framed",P);var q=f.player("pell_mark_other",P);var l=f.level(P);var b=f.base(P);
        for(var r:new ServerPlayer[]{p,q}){keep(f,r,"FirstVisit");keep(f,r,"SecondVisit");}
        f.at(p,P,.5,0,-44);f.at(q,P,-6.5,0,-36);
        String name=p.getGameProfile().getName();long[] hour={0};
        h.startSequence().thenWaitUntil(()->lastBegun(h,l,b))
        .thenExecute(()->{
            var own=f.own(p,P);PellMark.twoMarks(own);LiteraryVignettes.save(f.data(),p.getUUID(),P,own);
            outOfSight(f,p,q);told(p);told(q);
            PellLastNight.lockedUp(l,b);
        }).thenWaitUntil(()->h.assertTrue(PellMark.stage(b)>=PellMark.TO_PHONE,"June along the hall to the photographs, and the glass: "+PellMark.stage(b)))
        .thenExecute(()->{
            h.assertTrue(thing(l,b,PellMark.PHOTO.getX(),PellMark.PHOTO.getY(),PellMark.PHOTO.getZ(),PHOTO_BROKEN,0)&&thing(l,b,-6,1,-66,PHOTOS,2),"the last photograph hangs crooked, its glass starred; the one beside it whole");
            h.assertTrue(p.getUUID().equals(PellMark.marked(b)),"June has somebody to say broke it: the one on Gail's file twice");
            var own=f.own(p,P);
            h.assertTrue(own.getBoolean("MarkPhoto")&&own.getBoolean("BlamedForPhoto")&&own.getInt("GailMarks")==3,"on Gail's file a third time, for this: "+own.getInt("GailMarks"));
            h.assertTrue(PellEntries.due(own).contains(PellEntries.PHOTO),"a line they never wrote is due in their journal: "+PellEntries.due(own));
            h.assertTrue(told(p).contains(PellTexts.GLASS_HEARD)&&told(q).contains(PellTexts.GLASS_FAR),"heard in the house, and from the yard");
            h.assertTrue(f.own(q,P).getBoolean("HeardGlass")&&f.own(q,P).getInt("GailMarks")==0&&!f.own(q,P).getBoolean("BlamedForPhoto"),"the other heard it, and is on nobody's file");
            hour[0]=PellVisit.hour(b);
            h.assertTrue(hour[0]<PellNight.TWELVE,"before midnight: "+hour[0]);
        }).thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.JUNE)).equals(PellTexts.JUNE_RINGS_GAIL[PellTexts.JUNE_RINGS_GAIL.length-1]),"June on the kitchen telephone to Gail: "+said(who(h,l,b,PellPeople.JUNE))))
        .thenWaitUntil(()->h.assertTrue(PellMark.waiting(b),"Gail's car up the street, and Gail at the foot of the path: "+PellMark.stage(b)))
        .thenExecute(()->{
            h.assertTrue(PellVisit.hour(b)==hour[0],"the hour holds at eleven while June waits for her and Gail is here: "+PellVisit.hour(b)+", "+hour[0]);
            besideGail(h,f,l,b,p,q);
            var g=who(h,l,b,PellPeople.GAIL);LiteraryVignettes.talk(p,g);LiteraryVignettes.talk(q,g);
        }).thenIdle(1).thenExecute(()->{
            h.assertTrue(java.util.Arrays.stream(PellTexts.GAIL_NIGHT_MARKED).allMatch(line->telling(p,"Gail",line)),"to the one June named, what June told her: "+Talk.lines(p));
            h.assertTrue(java.util.Arrays.stream(PellTexts.gailNightOther(name)).allMatch(line->telling(q,"Gail",line)),"to the other, whom she has come for: "+Talk.lines(q));
            h.assertTrue(Talk.replies(p).equals(List.of(PellTexts.TELL_THINGS,PellTexts.TELL_SAY_NOTHING)),"nothing about them to show her: the one June named may go, or say nothing: "+Talk.replies(p));
            h.assertTrue(Talk.replies(q).equals(List.of(PellTexts.TELL_SAY_NOTHING)),"the other has nothing to show her: "+Talk.replies(q));
            h.assertTrue(Talk.choose(p,PellTexts.TELL_SAY_NOTHING)&&Talk.choose(q,PellTexts.TELL_SAY_NOTHING),"both say nothing");
            h.assertTrue(PellMark.waiting(b)&&!PellMark.inCar(b,p.getUUID()),"she waits");
            told(p);told(q);PellMark.waitedOut(b);
        }).thenIdle(2).thenExecute(()->{
            h.assertTrue(PellMark.inCar(b,p.getUUID())&&!PellMark.inCar(b,q.getUUID()),"when she has waited, the one June named goes in her car, and only them");
            var mine=told(p);h.assertTrue(mine.contains(PellTexts.CAR_HORN)&&mine.contains(PellTexts.NIGHT_IN_CAR),"the horn, and the car door held open: "+mine);
            h.assertTrue(told(q).contains(PellTexts.CAR_HORN),"heard by the other too");
            var own=f.own(p,P);
            h.assertTrue(own.getBoolean("Ended")&&own.getBoolean("TakenAway")&&own.getString("Outcome").equals("taken_away_in_the_night")&&!own.getBoolean("Ready"),"their ending, with nothing to read: "+own.getString("Outcome"));
            h.assertTrue(PellEntries.failed(own)&&!PellHouse.dealable(f.data(),p.getUUID()),"a failure: the house is not dealt to them again as it was");
            var theirs=f.own(q,P);
            h.assertTrue(PellTexts.taken(theirs).equals(List.of(name))&&theirs.getBoolean("GailCame")&&!theirs.getBoolean("Ended"),"the other keeps who was taken, and their own night goes on");
        }).thenIdle(PellDawn.LOOK_BACK+30).thenExecute(()->{
            h.assertTrue(standing(p,b,PellDawn.SET_DOWN,.4)&&f.own(p,P).getBoolean("SetDown"),"set down at the House's door: "+where(p,b));
            // The one set down faces the House's door; the one left looks up at the house, away from the street.
            toTheStreet(p);q.setYRot(180);q.setYHeadRot(180);
        }).thenWaitUntil(()->h.assertTrue(PellMark.stage(b)==PellMark.DONE&&PellPeople.find(l,b,PellPeople.GAIL)==null&&PellDawn.car(l,b)==null,"Gail and her car go, the first moment nobody sees them: "+PellMark.gailState(l,b)))
        .thenWaitUntil(()->h.assertTrue(PellVisit.hour(b)>hour[0],"and the night goes on: "+PellVisit.hour(b)))
        .thenExecute(()->{
            var theirs=f.own(q,P);
            var text=words(PellTexts.account(q.getGameProfile().getName(),theirs));
            h.assertTrue(text.contains("glass broke in the hall")&&text.contains("Gail's car came up the street in the night. June had rung her.")&&text.contains("She took "+name+" away in her car."),"the other's account keeps the night: "+text);
            h.assertTrue(!text.contains("it was me who had broken it"),"and blames nobody else's glass on them");
            String close=PellTexts.company(theirs,List.of(name));
            h.assertTrue(close.equals(name+" had the garage with me, until Gail came for them in the night."),"the Witness close: "+close);
        }).thenSucceed();
    }
    @AfterBatch(batch="pell_mark_taken") public static void markTakenDone(ServerLevel l){done();}

    /**
     * 0.4.548, Gail's third mark, answered. The same two readers and the same night, but before it the other reader takes the last page
     * off the shed's wall, in June's writing, and the note off the freezer's switch: torn tape is left where each was. At Gail, the other
     * has both to show her, and puts the page into her hands: she reads it twice, and asks where it was found. A mark comes off the named
     * reader's file. With fewer than three, she says she is taking nobody anywhere tonight, and goes without anybody; the hour goes on.
     * Gail keeps the page: its place on the wall stays torn. The note, still carried, stays off its switch until nobody has it, then is
     * back. Each account keeps the night: the one blamed for the glass, Gail gone again without them, and what was on their file and
     * taken off it; the other, the page given her.
     */
    @GameTest(template="empty",batch="pell_mark_cleared",timeoutTicks=6000)
    public static void thePageFromTheShedWallTakesAMarkOffAndGailGoesWithoutAnybody(GameTestHelper h){
        var f=house(h);var p=f.player("pell_mark_blamed",P);var q=f.player("pell_mark_shows",P);var l=f.level(P);var b=f.base(P);
        for(var r:new ServerPlayer[]{p,q}){keep(f,r,"FirstVisit");keep(f,r,"SecondVisit");}
        f.at(p,P,.5,0,-44);f.at(q,P,-6.5,0,-36);
        String name=p.getGameProfile().getName();long[] hour={0};
        h.startSequence().thenWaitUntil(()->lastBegun(h,l,b))
        .thenExecute(()->{
            // The last page off the shed's wall; then the note off the switch in the laundry.
            told(q);f.click(q,P,PellMark.LAST_PAGE);
        }).thenIdle(1).thenExecute(()->{
            h.assertTrue(told(q).contains(PellTexts.PAGE_TAKEN)&&q.getInventory().countItem(PellRegistry.PAGE.get())==1&&f.own(q,P).getBoolean("TookPage"),"the last page peeled off the wall");
            h.assertTrue(thing(l,b,PellMark.LAST_PAGE.getX(),PellMark.LAST_PAGE.getY(),PellMark.LAST_PAGE.getZ(),TRANSCRIPT,3),"a strip of tape and a torn edge where it was");
            f.click(q,P,PellMark.POWER);
        }).thenIdle(1).thenExecute(()->{
            h.assertTrue(told(q).contains(PellTexts.NOTE_TAKEN)&&q.getInventory().countItem(PellRegistry.NOTE.get())==1&&f.own(q,P).getBoolean("TookNote"),"the note peeled off the switch");
            h.assertTrue(thing(l,b,PellMark.POWER.getX(),PellMark.POWER.getY(),PellMark.POWER.getZ(),POWER,1),"the switch taped down, the note's corners left");
        }).thenIdle(PellScrub.KEEP+5).thenExecute(()->{
            h.assertTrue(thing(l,b,PellMark.LAST_PAGE.getX(),PellMark.LAST_PAGE.getY(),PellMark.LAST_PAGE.getZ(),TRANSCRIPT,3)&&thing(l,b,PellMark.POWER.getX(),PellMark.POWER.getY(),PellMark.POWER.getZ(),POWER,1),"carried at the house, they stay off the walls");
            var own=f.own(p,P);PellMark.twoMarks(own);LiteraryVignettes.save(f.data(),p.getUUID(),P,own);
            outOfSight(f,p,q);told(p);told(q);
            PellLastNight.lockedUp(l,b);
        }).thenWaitUntil(()->h.assertTrue(PellMark.stage(b)>=PellMark.TO_PHONE,"the glass: "+PellMark.stage(b)))
        .thenExecute(()->{
            h.assertTrue(p.getUUID().equals(PellMark.marked(b))&&f.own(p,P).getInt("GailMarks")==3,"the one on Gail's file twice, blamed for it: "+f.own(p,P).getInt("GailMarks"));
            hour[0]=PellVisit.hour(b);
        }).thenWaitUntil(()->h.assertTrue(PellMark.waiting(b),"Gail at the foot of the path: "+PellMark.stage(b)))
        .thenExecute(()->{
            h.assertTrue(PellVisit.hour(b)==hour[0],"the hour holds: "+PellVisit.hour(b)+", "+hour[0]);
            besideGail(h,f,l,b,p,q);told(q);LiteraryVignettes.talk(q,who(h,l,b,PellPeople.GAIL));
        }).thenIdle(1).thenExecute(()->{
            h.assertTrue(java.util.Arrays.stream(PellTexts.gailNightOther(name)).allMatch(line->telling(q,"Gail",line)),"whom she has come for: "+Talk.lines(q));
            h.assertTrue(Talk.replies(q).equals(List.of(PellTexts.TELL_PAGE,PellTexts.TELL_NOTE,PellTexts.TELL_SAY_NOTHING)),"the page and the note to show her: "+Talk.replies(q));
            h.assertTrue(Talk.choose(q,PellTexts.TELL_PAGE),"the page into her hands");
        }).thenIdle(1).thenExecute(()->{
            // What the reader sees comes with the next tick's packets.
            var heard=told(q);
            h.assertTrue(java.util.Arrays.stream(PellTexts.GAIL_ON_PAGE).allMatch(line->telling(q,"Gail",line))&&heard.contains(PellTexts.GAIL_READS_PAGE),"she reads it twice, and asks where it was: "+Talk.lines(q)+" / "+heard);
            h.assertTrue(q.getInventory().countItem(PellRegistry.PAGE.get())==0&&q.getInventory().countItem(PellRegistry.NOTE.get())==1,"out of the reader's hands; the note still with them");
            var own=f.own(p,P);
            h.assertTrue(own.getInt("GailMarks")==2&&own.getInt("MarksCleared")==1,"a mark off the named reader's file: "+own.getInt("GailMarks"));
            h.assertTrue(f.own(q,P).getString("ShowedGail").equals(PellMark.PAGE),"kept in the record of the one who showed her");
        }).thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.GAIL)).equals(PellTexts.GAIL_STAYS[PellTexts.GAIL_STAYS.length-1]),"she is taking nobody anywhere tonight: "+said(who(h,l,b,PellPeople.GAIL))))
        .thenExecute(()->{
            h.assertTrue(!PellMark.inCar(b,p.getUUID())&&!PellMark.inCar(b,q.getUUID()),"nobody in her car");
            for(var r:new ServerPlayer[]{p,q}){var own=f.own(r,P);h.assertTrue(own.getBoolean("GailCame")&&own.getBoolean("GailLeft")&&!own.getBoolean("Ended")&&!own.getBoolean("TakenAway"),"each keeps that she came and went without anybody");}
            for(var r:new ServerPlayer[]{p,q}){r.setYRot(180);r.setYHeadRot(180);}
        }).thenWaitUntil(()->h.assertTrue(PellMark.stage(b)==PellMark.DONE&&PellPeople.find(l,b,PellPeople.GAIL)==null&&PellDawn.car(l,b)==null,"Gail and her car go: "+PellMark.gailState(l,b)))
        .thenWaitUntil(()->h.assertTrue(PellVisit.hour(b)>hour[0],"the night goes on: "+PellVisit.hour(b)))
        .thenIdle(PellScrub.KEEP+5).thenExecute(()->{
            h.assertTrue(thing(l,b,PellMark.LAST_PAGE.getX(),PellMark.LAST_PAGE.getY(),PellMark.LAST_PAGE.getZ(),TRANSCRIPT,3),"Gail kept the page: its place on the wall stays torn");
            h.assertTrue(thing(l,b,PellMark.POWER.getX(),PellMark.POWER.getY(),PellMark.POWER.getZ(),POWER,1),"the note still carried, still off its switch");
            q.getInventory().clearContent();
        }).thenWaitUntil(()->h.assertTrue(thing(l,b,PellMark.POWER.getX(),PellMark.POWER.getY(),PellMark.POWER.getZ(),POWER,0),"nobody has the note: it is back over the switch"))
        .thenExecute(()->{
            var blamed=words(PellTexts.account(name,f.own(p,P)));
            h.assertTrue(blamed.contains("It was the last of Nessa's school photographs. June rang Gail, and by then it was me who had broken it.")
                &&blamed.contains("June had rung her about me.")&&blamed.contains("She went away again without me.")
                &&blamed.contains("Gail put three things on my file, and took one off again."),"the blamed one's account: "+blamed);
            var shower=words(PellTexts.account(q.getGameProfile().getName(),f.own(q,P)));
            h.assertTrue(shower.contains("I gave her the last page from the shed wall, in June's writing. She read it twice.")&&shower.contains("She went away again on her own."),"the other's account: "+shower);
            h.assertTrue(PellTexts.company(f.own(q,P),List.of(name)).equals(name+" had the garage with me."),"nobody was taken: the Witness close as before");
        }).thenSucceed();
    }
    @AfterBatch(batch="pell_mark_cleared") public static void markClearedDone(ServerLevel l){done();}

    /**
     * 0.4.548, Gail's third mark, answered with a tape. The other reader has watched a tape in the shed. At Gail, they are offered to
     * take her to see what is in the shed; she says "Show me, then." and comes behind them. Run on too far ahead of her and she stops,
     * and goes back to the foot of the path. Asked again, she follows the reader's own way up the lot to the shed, a step behind, and
     * in through its door. With a tape playing she watches it, and when she has seen enough: "Turn it off. Turn it off, love." A mark
     * comes off the named reader's file; with fewer than three, she goes without anybody, back the way she came. The one who showed her
     * keeps it in their account.
     */
    @GameTest(template="empty",batch="pell_mark_tape",timeoutTicks=8000)
    public static void aReaderWhoHasWatchedATapeTakesGailToTheShedAndSheAsksForItOff(GameTestHelper h){
        var f=house(h);var p=f.player("pell_mark_named",P);var q=f.player("pell_mark_leads",P);var l=f.level(P);var b=f.base(P);
        for(var r:new ServerPlayer[]{p,q}){keep(f,r,"FirstVisit");keep(f,r,"SecondVisit");}
        f.at(p,P,.5,0,-44);f.at(q,P,-6.5,0,-36);
        String name=p.getGameProfile().getName();long[] hour={0};
        var tv=b.offset(PellTapes.TV);var door=PellSite.SHED_DOOR;var outside=door.south();
        @SuppressWarnings("unchecked") List<BlockPos>[] way=new List[]{List.of()};int[] next={0};
        h.startSequence().thenWaitUntil(()->lastBegun(h,l,b))
        .thenExecute(()->{
            PellTapes.asWatched(q,0);
            var own=f.own(p,P);PellMark.twoMarks(own);LiteraryVignettes.save(f.data(),p.getUUID(),P,own);
            outOfSight(f,p,q);
            PellLastNight.lockedUp(l,b);
        }).thenWaitUntil(()->h.assertTrue(PellMark.waiting(b),"Gail at the foot of the path: "+PellMark.stage(b)))
        .thenExecute(()->{
            hour[0]=PellVisit.hour(b);
            besideGail(h,f,l,b,p,q);told(q);LiteraryVignettes.talk(q,who(h,l,b,PellPeople.GAIL));
        }).thenIdle(1).thenExecute(()->{
            h.assertTrue(Talk.replies(q).equals(List.of(PellTexts.TELL_TAPES,PellTexts.TELL_SAY_NOTHING)),"one who has watched a tape can take her to see: "+Talk.replies(q));
            h.assertTrue(Talk.choose(q,PellTexts.TELL_TAPES)&&telling(q,"Gail",PellTexts.GAIL_SHOW_ME),"Show me, then: "+Talk.lines(q));
            h.assertTrue(PellMark.stage(b)==PellMark.LEADING,"she comes behind them");
            // Too far ahead of her, down the street.
            var g=who(h,l,b,PellPeople.GAIL);f.at(q,P,g.getX()-b.getX(),0,g.getZ()-b.getZ()+12);
        }).thenIdle(2).thenExecute(()->{
            h.assertTrue(told(q).contains(PellTexts.GAIL_LET_GO)&&PellMark.stage(b)!=PellMark.LEADING,"she stops, and will not go further without them");
        }).thenWaitUntil(()->h.assertTrue(PellMark.waiting(b),"back at the foot of the path: "+PellMark.stage(b)))
        .thenExecute(()->{
            besideGail(h,f,l,b,p,q);LiteraryVignettes.talk(q,who(h,l,b,PellPeople.GAIL));
        }).thenIdle(1).thenExecute(()->{
            h.assertTrue(Talk.choose(q,PellTexts.TELL_TAPES),"asked again");
            var from=BlockPos.containing(q.position().subtract(Vec3.atLowerCornerOf(b)));
            way[0]=route(l,b,from,outside);next[0]=0;
            h.assertTrue(!way[0].isEmpty(),"a way on foot from the foot of the path to the shed door: "+from+" to "+outside);
        }).thenWaitUntil(()->{
            // The reader walks up the lot a cell at a time, never more than a few steps ahead of her.
            var g=who(h,l,b,PellPeople.GAIL);
            if(next[0]<way[0].size()&&g.distanceTo(q)<3){var c=way[0].get(next[0]++);f.at(q,P,c.getX()+.5,0,c.getZ()+.5);}
            h.assertTrue(next[0]>=way[0].size()&&g.distanceTo(q)<PellMark.BESIDE+.6,"Gail a step behind the reader to the shed door: "+next[0]+"/"+way[0].size()+", "+g.position().subtract(Vec3.atLowerCornerOf(b))+", stage "+PellMark.stage(b));
        }).thenExecute(()->{
            if(!PellSite.open(l,b,door))hand(q,b.offset(door));
        }).thenIdle(1).thenExecute(()->{
            h.assertTrue(PellSite.open(l,b,door),"the shed's padlock hangs open on the last night, and its door opens to a hand");
            way[0]=List.of(door,door.north(),door.north().north().west());next[0]=0;
        }).thenWaitUntil(()->{
            var g=who(h,l,b,PellPeople.GAIL);
            if(next[0]<way[0].size()&&g.distanceTo(q)<2.5){var c=way[0].get(next[0]++);f.at(q,P,c.getX()+.5,0,c.getZ()+.5);}
            h.assertTrue(PellMark.stage(b)==PellMark.IN_SHED,"in through its door after them: "+g.position().subtract(Vec3.atLowerCornerOf(b))+", stage "+PellMark.stage(b));
        }).thenExecute(()->{
            lookAt(q,Vec3.atCenterOf(tv));hand(q,tv);
            h.assertTrue(PellTapes.playing(b),"a tape playing in the shed");
        }).thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.GAIL)).equals(PellTexts.GAIL_TURN_IT_OFF),"she has seen enough: "+said(who(h,l,b,PellPeople.GAIL))))
        .thenExecute(()->{
            var own=f.own(p,P);
            h.assertTrue(own.getInt("GailMarks")==2&&own.getInt("MarksCleared")==1,"a mark off the named reader's file: "+own.getInt("GailMarks"));
            h.assertTrue(f.own(q,P).getString("ShowedGail").equals(PellMark.TAPE),"kept by the one who showed her");
        }).thenWaitUntil(()->h.assertTrue(said(who(h,l,b,PellPeople.GAIL)).equals(PellTexts.GAIL_STAYS[PellTexts.GAIL_STAYS.length-1]),"she is taking nobody anywhere tonight: "+said(who(h,l,b,PellPeople.GAIL))))
        .thenExecute(()->{
            for(var r:new ServerPlayer[]{p,q})h.assertTrue(!f.own(r,P).getBoolean("Ended")&&f.own(r,P).getBoolean("GailLeft"),"nobody taken; each keeps that she went without anybody");
            // The reader stays watching the screen; the other turns from the window to the hall. She goes back the way she came.
            lookAt(q,Vec3.atCenterOf(tv));p.setYRot(180);p.setYHeadRot(180);
        }).thenWaitUntil(()->h.assertTrue(PellMark.stage(b)==PellMark.DONE&&PellPeople.find(l,b,PellPeople.GAIL)==null,"Gail back to her car and away, the first moment nobody sees: "+PellMark.gailState(l,b)))
        .thenWaitUntil(()->h.assertTrue(PellVisit.hour(b)>hour[0],"the night goes on: "+PellVisit.hour(b)))
        .thenExecute(()->{
            var text=words(PellTexts.account(q.getGameProfile().getName(),f.own(q,P)));
            h.assertTrue(text.contains("I took her out to the shed and played her one of the tapes. She asked me to turn it off."),"the account keeps it: "+text);
            h.assertTrue(words(PellTexts.account(name,f.own(p,P))).contains("Gail put three things on my file, and took one off again."),"and the named reader's file");
        }).thenSucceed();
    }
    @AfterBatch(batch="pell_mark_tape") public static void markTapeDone(ServerLevel l){done();}
}
