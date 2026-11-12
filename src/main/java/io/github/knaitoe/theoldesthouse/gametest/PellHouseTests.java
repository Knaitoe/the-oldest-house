package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
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
    private static LiteraryTests.Fixture house(GameTestHelper h){done();active=new LiteraryTests.Fixture(h);return active;}
    private static void done(){if(active!=null){active.close();active=null;}PellHouse.clearAll();}
    private static boolean is(ServerLevel l,BlockPos b,int x,int y,int z,String material){return l.getBlockState(b.offset(x,y,z)).is(PellRegistry.material(material));}
    private static boolean fitting(ServerLevel l,BlockPos b,BlockPos at,PellBlock.Kind kind){return PellBlock.is(l.getBlockState(b.offset(at)),kind);}
    /** One of the house's things, of this kind and stage. */
    private static boolean thing(ServerLevel l,BlockPos b,int x,int y,int z,PellThing.Kind kind,int stage){var s=l.getBlockState(b.offset(x,y,z));return PellThing.is(s,kind)&&s.getValue(PellThing.STAGE)==stage;}
    private static Direction facing(ServerLevel l,BlockPos b,int x,int y,int z){return l.getBlockState(b.offset(x,y,z)).getValue(PellThing.FACING);}
    /** A cell of the house's outside wall, of this course, its brick turned this way. */
    private static boolean wall(ServerLevel l,BlockPos b,int x,int y,int z,int stage,Direction out){var s=l.getBlockState(b.offset(x,y,z));return PellBlock.is(s,PellBlock.Kind.WALL)&&s.getValue(PellBlock.STAGE)==stage&&s.getValue(PellBlock.FACING)==out;}
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
                // A body in the cell, and a body half way across the edge into it: a shut leaf or a fence stands on the cell's middle.
                var here=b.offset(to);var edge=new AABB(b.getX()+(at.getX()+to.getX())/2.0+.2,b.getY()+.01,b.getZ()+(at.getZ()+to.getZ())/2.0+.2,b.getX()+(at.getX()+to.getX())/2.0+.8,b.getY()+1.79,b.getZ()+(at.getZ()+to.getZ())/2.0+.8);
                var body=new AABB(here.getX()+.2,here.getY()+.01,here.getZ()+.2,here.getX()+.8,here.getY()+1.79,here.getZ()+.8);
                if(!l.noCollision(body)||!l.noCollision(edge)||l.noCollision(body.move(0,-.5,0)))continue;
                seen.add(to);queue.add(to);
            }
        }
        return seen;
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
            h.assertTrue(l.getBlockState(b.offset(-7,-2,-82)).is(Blocks.WATER)&&is(l,b,-7,PellSite.SHALLOW,-82,"pool_tile")&&l.getBlockState(b.offset(-7,PellSite.DEEP+1,-90)).is(Blocks.WATER)&&is(l,b,-7,PellSite.DEEP,-90,"pool_tile"),"water over tile, shallow and deep");
            h.assertTrue(is(l,b,PellSite.WATER_W-1,-2,-85,"pool_band")&&is(l,b,PellSite.WATER_W-1,-1,-85,"coping"),"the band at the waterline under the coping");
            PellSite.pool(l,b,false);
            h.assertTrue(l.getBlockState(b.offset(-7,-1,-86)).is(Blocks.WATER)&&is(l,b,-7,PellSite.DEEP,-90,"pool_tile"),"uncovered, it is full to the brim");
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
            h.assertFalse(LiteraryVignettes.canDeal(f.data(),p.getUUID(),P),"a place still being built is never dealt");
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
            h.assertTrue(gail.getMainHandItem().is(PellRegistry.FOLDER.get())&&gail.gesture()==PellPeople.FOLDER,"Gail has her folder, held to her chest");
            h.assertTrue(june.getMainHandItem().isEmpty()&&callum.getMainHandItem().isEmpty()&&june.getOffhandItem().isEmpty()&&callum.getOffhandItem().isEmpty(),"June and Callum hold nothing");
            // Asked again, nobody is made twice; nor with the house's memory of them gone, as after a reload.
            PellPeople.ensure(l,b);PellPeople.clearAll();PellPeople.ensure(l,b);
            h.assertTrue(l.getEntitiesOfClass(LiteraryActor.class,PellPeople.area(b),PellPeople::is).size()==PellPeople.COUNT,"asked again, nobody is made twice");
            for(int i=0;i<PellPeople.COUNT;i++)h.assertTrue(PellPeople.find(l,b,i).getUUID().equals(ids[i]),"the same "+PellPeople.name(i)+", taken up again");
            // Callum taken away is made again, where he stands on the first visit; nobody else is.
            callum.discard();PellPeople.ensure(l,b);
            var again=who(h,l,b,PellPeople.CALLUM);var spot=PellPeople.FIRST_VISIT[PellPeople.CALLUM];
            h.assertTrue(!again.getUUID().equals(ids[PellPeople.CALLUM])&&Math.abs(again.getX()-(b.getX()+spot.x()))<.3&&Math.abs(again.getZ()-(b.getZ()+spot.z()))<.3,"Callum, taken away, is made again where he stands");
            for(int i=0;i<PellPeople.COUNT;i++)if(i!=PellPeople.CALLUM)h.assertTrue(PellPeople.find(l,b,i).getUUID().equals(ids[i]),"and nobody else is: "+PellPeople.name(i));
            h.assertTrue(l.getEntitiesOfClass(LiteraryActor.class,PellPeople.area(b),PellPeople::is).size()==PellPeople.COUNT,"still four of them");
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
     * behind her, and June is in her room with the door shut and her hands empty. The night waits with Callum still on the line. Left,
     * the house begins again: the line as it was, the tin on the bench, Callum back at the end of the hall, and what each reader saw kept.
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
            h.assertTrue(PellVisit.hour(b)>mark[0]||PellVisit.hour(b)==PellNight.TWELVE,"the hour goes on to midnight: "+mark[0]+" to "+PellVisit.hour(b));
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
}
