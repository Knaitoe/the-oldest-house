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
            h.assertTrue(thing(l,b,-12,0,-57,STAKE,0)&&thing(l,b,-2,0,-57,STAKE,1)&&thing(l,b,-14,0,-86,POOL_CHAIR,0)&&facing(l,b,-14,0,-86)==Direction.EAST,
                "the stakes, the newest by the step; the pool chair turned to the water");
            h.assertTrue(thing(l,b,19,0,-80,KENNEL,0)&&thing(l,b,19,0,-79,KENNEL,1)&&PellThing.right(new BlockPos(19,0,-80),Direction.WEST).equals(new BlockPos(19,0,-79))
                    &&thing(l,b,19,0,-78,BOWL,0),"the kennel, two cells, its name read left to right from the drive; the bowl beside it");
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
                    new BlockPos(8,1,-93),new BlockPos(-2,0,-57),new BlockPos(19,0,-79),new BlockPos(-8,0,-63),new BlockPos(-2,0,-71)))
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
}
