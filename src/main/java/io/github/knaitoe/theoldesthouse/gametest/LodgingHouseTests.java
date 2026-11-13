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
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * 0.4.549, The Ones Already Inside (docs/THE_ONES_ALREADY_INSIDE_PLAN.md). Each case builds the place itself, one case to a batch, as
 * the literary cases do.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_lodging") @PrefixGameTestTemplate(false)
public final class LodgingHouseTests {
    private LodgingHouseTests(){}
    private static final LabyrinthPlace P=LabyrinthPlace.ONES_INSIDE;
    private static LiteraryTests.Fixture active;
    private static LiteraryTests.Fixture house(GameTestHelper h){done();active=new LiteraryTests.Fixture(h);return active;}
    private static void done(){if(active!=null){active.close();active=null;}LodgingHouse.clearAll();}
    private static boolean is(ServerLevel l,BlockPos b,int x,int y,int z,String material){return l.getBlockState(b.offset(x,y,z)).is(LodgingRegistry.material(material));}
    private static boolean fitting(ServerLevel l,BlockPos b,BlockPos at,LodgingBlock.Kind kind){return LodgingBlock.is(l.getBlockState(b.offset(at)),kind);}
    private static boolean floor(ServerLevel l,BlockPos b,int x,int y,int z,LodgingFloor.Pair pair){var s=l.getBlockState(b.offset(x,y,z));return s.is(LodgingRegistry.FLOOR.get())&&s.getValue(LodgingFloor.PAIR)==pair;}
    private static boolean wall(ServerLevel l,BlockPos b,int x,int y,int z,LodgingWall.Pair pair,Direction front){var s=l.getBlockState(b.offset(x,y,z));return s.is(LodgingRegistry.WALL.get())&&s.getValue(LodgingWall.PAIR)==pair&&s.getValue(LodgingWall.FACING)==front;}
    private static void wallAt(GameTestHelper h,ServerLevel l,BlockPos b,int x,int y,int z,LodgingWall.Pair pair,Direction front){
        h.assertTrue(wall(l,b,x,y,z,pair,front),"the wall at "+x+","+y+","+z+" papered as "+pair+", its first face "+front+": "+l.getBlockState(b.offset(x,y,z)));
    }
    private static LodgingWall.Part part(ServerLevel l,BlockPos b,BlockPos at){var s=l.getBlockState(b.offset(at));return s.is(LodgingRegistry.WALL.get())?s.getValue(LodgingWall.PART):null;}
    /** Everything a reader has been told since this was last asked: reading it empties it. */
    private static List<String> told(ServerPlayer p){
        var out=new java.util.ArrayList<String>();
        for(var packet:NativeTestPlayers.packets(p))if(packet instanceof net.minecraft.network.protocol.game.ClientboundSystemChatPacket chat)out.add(chat.content().getString());
        return out;
    }
    /** Every cell of one floor a body can stand in and walk to from a start, without opening anything. */
    private static Set<BlockPos> walked(ServerLevel l,BlockPos b,BlockPos from){
        var seen=new HashSet<BlockPos>();var queue=new ArrayDeque<BlockPos>();seen.add(from);queue.add(from);
        while(!queue.isEmpty()){
            var at=queue.poll();
            for(var d:Direction.Plane.HORIZONTAL){
                var to=at.relative(d);
                if(to.getX()<=LodgingSite.WEST||to.getX()>=LodgingSite.EAST||to.getZ()<=LodgingSite.BACK||to.getZ()>0||seen.contains(to))continue;
                // A body in the cell, and a body half way across the edge into it: a shut leaf stands on the cell's middle.
                var here=b.offset(to);var y=b.getY()+to.getY();
                var edge=new AABB(b.getX()+(at.getX()+to.getX())/2.0+.2,y+.01,b.getZ()+(at.getZ()+to.getZ())/2.0+.2,b.getX()+(at.getX()+to.getX())/2.0+.8,y+1.79,b.getZ()+(at.getZ()+to.getZ())/2.0+.8);
                var body=new AABB(here.getX()+.2,here.getY()+.01,here.getZ()+.2,here.getX()+.8,here.getY()+1.79,here.getZ()+.8);
                // Over a sill or a step half a cell high, a body steps up.
                boolean free=l.noCollision(body)&&l.noCollision(edge)||l.noCollision(body.move(0,.5,0))&&l.noCollision(edge.move(0,.5,0));
                if(!free||l.noCollision(body.move(0,-.5,0)))continue;
                seen.add(to);queue.add(to);
            }
        }
        return seen;
    }
    /** A body standing on top of a stair's tread here has the room above it to stand up. */
    private static boolean headroom(ServerLevel l,BlockPos b,BlockPos tread){var at=b.offset(tread);return l.noCollision(new AABB(at.getX()+.3,at.getY()+1.01,at.getZ()+.2,at.getX()+.9,at.getY()+2.8,at.getZ()+.8));}

    /**
     * The lodging house. Built of its own materials, papered room by room on both faces of every wall; the stair up and the stair down
     * clear to climb; the gallery railed round its well; ladders down the passages' back corners; every frame in its wall, Daniel's in
     * his; the sill, the posts, the ratchet and the dead post in the cellar. Every door answers a hand as it should: Daniel's bolt is drawn
     * before it opens, the jib door lies flush until it is opened, the hatches slide. Shut, a body walks from the House's door through the
     * hall and the back passage and nowhere else; open, to every room, upstairs and down, behind the walls and under the house. The first
     * paper is on the hall table, and the House does not deal the place.
     */
    @GameTest(template="empty",batch="lodging_built",timeoutTicks=1200)
    public static void theLodgingHouseIsBuiltOfItsOwnThingsAndEveryDoorAnswersAsItShould(GameTestHelper h){
        var f=house(h);var p=f.player("lodging_reader",P);var l=f.level(P);var b=f.base(P);
        h.startSequence().thenIdle(5).thenExecute(()->{
            // Its shell: brick, the cellar's damp course and flags; every floor its room's, every ceiling under it.
            h.assertTrue(is(l,b,LodgingSite.WEST,3,-10,"brick")&&is(l,b,LodgingSite.WEST,LodgingSite.CELLAR,-10,"brick_damp")&&is(l,b,0,LodgingSite.CELLAR_SLAB,-10,"flags"),"brick outside walls, damp at the cellar's foot, a flagged cellar floor");
            h.assertTrue(floor(l,b,0,-1,-5,LodgingFloor.Pair.TILE)&&floor(l,b,-8,-1,-6,LodgingFloor.Pair.BOARDS)&&floor(l,b,0,-1,-14,LodgingFloor.Pair.WORN)&&floor(l,b,-8,-1,-22,LodgingFloor.Pair.SCUFFED)
                &&floor(l,b,8,-1,-22,LodgingFloor.Pair.FLAGS)&&floor(l,b,LodgingSite.PASS_W,-1,-10,LodgingFloor.Pair.PASSAGE_LOW),"the hall's tiles, boards, the worn back passage, Daniel's boards, the scullery's flags, the passage's bare boards");
            h.assertTrue(floor(l,b,-1,4,-5,LodgingFloor.Pair.RUNNER_W)&&floor(l,b,0,4,-5,LodgingFloor.Pair.RUNNER)&&floor(l,b,1,4,-20,LodgingFloor.Pair.RUNNER_E)&&floor(l,b,-9,4,-8,LodgingFloor.Pair.ROOM)
                &&floor(l,b,-8,4,-22,LodgingFloor.Pair.WORK)&&floor(l,b,8,4,-22,LodgingFloor.Pair.CARPET)&&floor(l,b,0,LodgingSite.ROOF,-8,LodgingFloor.Pair.TOP),"upstairs the runner down the gallery and the corridor, the rooms' boards, the workroom's, Vera's carpet, and the roof");
            // Every wall papered on each face as its own room is, and lath on the passages' side.
            wallAt(h,l,b,-4,1,-3,LodgingWall.Pair.HALL_PARLOR,Direction.EAST);wallAt(h,l,b,4,1,-3,LodgingWall.Pair.HALL_DINING,Direction.WEST);wallAt(h,l,b,-10,1,LodgingSite.BACK_WALL,LodgingWall.Pair.HALL_DANIEL,Direction.SOUTH);
            wallAt(h,l,b,LodgingSite.ROOM_W,2,-20,LodgingWall.Pair.DANIEL_LATH,Direction.EAST);wallAt(h,l,b,-4,6,-2,LodgingWall.Pair.HALL_ROOM,Direction.EAST);wallAt(h,l,b,-2,6,-24,LodgingWall.Pair.HALL_WORK,Direction.EAST);
            h.assertTrue(part(l,b,new BlockPos(-4,0,-3))==LodgingWall.Part.LOW&&part(l,b,new BlockPos(-4,2,-3))==LodgingWall.Part.MID&&part(l,b,new BlockPos(-4,5,-2))==LodgingWall.Part.LOW,"a skirting along the foot of every wall, on both floors");
            // The stairs: the main stair rising north from the hall to the gallery, the back stair down to the cellar, each clear to climb.
            for(int i=0;i<5;i++)for(int x=2;x<=3;x++){var s=l.getBlockState(b.offset(x,i,LodgingSite.STAIR_FOOT-i));h.assertTrue(s.is(LodgingRegistry.STAIR.get())&&s.getValue(StairBlock.FACING)==Direction.NORTH,"a tread of the main stair at "+x+","+i);}
            for(int i=0;i<5;i++)h.assertTrue(headroom(l,b,new BlockPos(3,i,LodgingSite.STAIR_FOOT-i)),"room to stand on the main stair's tread "+i);
            h.assertTrue(LodgingSite.CELLAR_TREADS==4,"four treads down to the cellar, a cell lower each");
            for(int i=0;i<LodgingSite.CELLAR_TREADS;i++){var s=l.getBlockState(b.offset(0,LodgingSite.GROUND_SLAB-i,LodgingSite.CELLAR_STAIR_TOP-i));h.assertTrue(s.is(LodgingRegistry.CELLAR_STAIR.get())&&s.getValue(StairBlock.FACING)==Direction.SOUTH&&headroom(l,b,new BlockPos(0,LodgingSite.GROUND_SLAB-i,LodgingSite.CELLAR_STAIR_TOP-i)),"the back stair's tread "+i+", clear");}
            h.assertTrue(fitting(l,b,new BlockPos(2,LodgingSite.UPPER,LodgingSite.STAIR_FOOT-2),LodgingBlock.Kind.RAIL)&&fitting(l,b,new BlockPos(3,LodgingSite.UPPER,LodgingSite.STAIR_FOOT-2),LodgingBlock.Kind.RAIL)
                &&fitting(l,b,new BlockPos(2,LodgingSite.UPPER,LodgingSite.STAIR_FOOT-3),LodgingBlock.Kind.RAIL)&&fitting(l,b,new BlockPos(1,0,LodgingSite.STAIR_FOOT),LodgingBlock.Kind.BANISTER),"the gallery's rail round the well, and the newel at the stair's foot");
            for(int y=LodgingSite.CELLAR;y<=LodgingSite.UPPER+3;y++)h.assertTrue(fitting(l,b,new BlockPos(LodgingSite.PASS_W,y,LodgingSite.LADDER_Z),LodgingBlock.Kind.LADDER)&&fitting(l,b,new BlockPos(LodgingSite.PASS_E,y,LodgingSite.LADDER_Z),LodgingBlock.Kind.LADDER),"the ladders at the side passages' ends, at "+y);
            h.assertTrue(LodgingRegistry.FITTING.get().isLadder(l.getBlockState(b.offset(LodgingSite.PASS_W,0,LodgingSite.LADDER_Z)),l,b.offset(LodgingSite.PASS_W,0,LodgingSite.LADDER_Z),p),"they are climbed");
            // The frames: one in every guest room's outside wall, Daniel's in his back wall.
            for(int i=0;i<6;i++){var o=LodgingSite.frame(i);var right=LodgingSite.frameFacing(i).getClockWise();
                h.assertTrue(fitting(l,b,o,LodgingBlock.Kind.FRAME_BACK)&&fitting(l,b,o.above(),LodgingBlock.Kind.FRAME_BACK)&&fitting(l,b,o.relative(right),LodgingBlock.Kind.FRAME_SIDE)&&fitting(l,b,o.relative(right,-1),LodgingBlock.Kind.FRAME_SIDE)
                    &&fitting(l,b,o.above(2),LodgingBlock.Kind.FRAME_HEAD)&&l.getBlockState(b.offset(o)).getValue(LodgingBlock.FACING)==LodgingSite.frameFacing(i),"room "+(i+1)+"'s frame, facing into it");
                h.assertTrue(l.isEmptyBlock(b.offset(o).relative(LodgingSite.frameFacing(i))),"room "+(i+1)+"'s frame has the room in front of it");}
            h.assertTrue(fitting(l,b,LodgingSite.DANIEL_FRAME,LodgingBlock.Kind.FRAME_BACK)&&(l.getBlockState(b.offset(LodgingSite.DANIEL_FRAME)).getValue(LodgingBlock.STAGE)&2)==2,"Daniel's frame in his back wall");
            // The support: the sill under the frames' walls, a post on it under every frame, the ratchet, and the dead post.
            for(int i=0;i<6;i++){var o=LodgingSite.frame(i);h.assertTrue(fitting(l,b,new BlockPos(o.getX(),LodgingSite.CELLAR,o.getZ()+1),LodgingBlock.Kind.SILL)&&fitting(l,b,new BlockPos(o.getX(),LodgingSite.CELLAR,o.getZ()),LodgingBlock.Kind.SHOE)
                &&fitting(l,b,new BlockPos(o.getX(),LodgingSite.CELLAR+1,o.getZ()),LodgingBlock.Kind.POST)&&fitting(l,b,new BlockPos(o.getX(),LodgingSite.CELLAR+2,o.getZ()),LodgingBlock.Kind.POST),"under room "+(i+1)+"'s frame, its post in its shoe on the sill");}
            h.assertTrue(fitting(l,b,new BlockPos(LodgingSite.DANIEL_FRAME.getX(),LodgingSite.CELLAR,LodgingSite.ROOM_N),LodgingBlock.Kind.SHOE)&&fitting(l,b,LodgingSite.RATCHET,LodgingBlock.Kind.RATCHET)&&fitting(l,b,LodgingSite.RATCHET.above(),LodgingBlock.Kind.RATCHET)
                &&fitting(l,b,LodgingSite.DEAD_POST,LodgingBlock.Kind.SHOE)&&l.getBlockState(b.offset(LodgingSite.DEAD_POST)).getValue(LodgingBlock.STAGE)==1,"Daniel's post, the ratchet with its wheel, and the dead post standing on the bare floor");
            // Nothing in the house is the game's own: every cell is air or the house's.
            var own=Set.of(LodgingRegistry.FITTING.get(),LodgingRegistry.THING.get(),LodgingRegistry.WALL.get(),LodgingRegistry.FLOOR.get(),LodgingRegistry.STAIR.get(),LodgingRegistry.CELLAR_STAIR.get());
            for(int x=LodgingSite.WEST;x<=LodgingSite.EAST;x++)for(int y=LodgingSite.CELLAR_SLAB;y<=LodgingSite.ROOF;y++)for(int z=LodgingSite.BACK;z<=LodgingSite.FRONT;z++){
                if(Math.abs(x-LodgingSite.ONWARD.getX())<=0&&z==LodgingSite.ONWARD.getZ()&&(y==0||y==1))continue;
                var s=l.getBlockState(b.offset(x,y,z));
                h.assertTrue(s.isAir()||own.contains(s.getBlock())||LodgingRegistry.MATERIAL_NAMES.stream().anyMatch(n->s.is(LodgingRegistry.material(n))),"the house's own at "+x+","+y+","+z+": "+s);
            }
            // The doors, as built: the house's own, all shut, Daniel's bolted.
            for(var door:LodgingSite.doors())h.assertTrue(l.getBlockState(b.offset(door)).getValue(LodgingBlock.KIND).leaf()&&!LodgingSite.open(l,b,door),"the house's own door, shut: "+door);
            h.assertTrue(LodgingBlock.bolted(l.getBlockState(b.offset(LodgingSite.DANIEL_DOOR)))&&l.getBlockState(b.offset(LodgingSite.DANIEL_DOOR)).getValue(LodgingBlock.FACING)==Direction.SOUTH,"Daniel's door, bolted on the passage side");
            // Shut, a body walks the hall and the back passage to the back stair's head, and into nothing else.
            var out=walked(l,b,new BlockPos(0,0,-1));
            for(var cell:List.of(new BlockPos(0,0,-11),new BlockPos(-12,0,-14),new BlockPos(12,0,-14),new BlockPos(0,0,-17),LodgingSite.SOURCE.east()))
                h.assertTrue(out.contains(cell),"the hall, both ends of the back passage, the back stair's head and the hall table: "+cell+" of "+out.size());
            for(var cell:List.of(new BlockPos(-9,0,-6),new BlockPos(12,0,-2),new BlockPos(-8,0,-22),new BlockPos(11,0,-20),new BlockPos(LodgingSite.PASS_W,0,-10),new BlockPos(3,0,-10)))
                h.assertFalse(out.contains(cell),"not into the parlor, the dining room, Daniel's room, the scullery, the passages or the cupboard: "+cell);
            var up=walked(l,b,new BlockPos(2,LodgingSite.UPPER,-12));
            for(var cell:List.of(new BlockPos(0,LodgingSite.UPPER,-1),new BlockPos(3,LodgingSite.UPPER,-7),new BlockPos(0,LodgingSite.UPPER,-28)))h.assertTrue(up.contains(cell),"upstairs, the gallery round the well and the corridor to its end: "+cell+" of "+up.size());
            for(var cell:List.of(new BlockPos(2,LodgingSite.UPPER,-9),new BlockPos(3,LodgingSite.UPPER,-10)))h.assertFalse(up.contains(cell),"not off the gallery into its well: "+cell);
            for(int i=0;i<6;i++){var o=LodgingSite.frame(i);h.assertFalse(up.contains(o.relative(LodgingSite.frameFacing(i),3)),"not into room "+(i+1)+" while its door is shut");}
            // A hand on what does not open.
            told(p);
            f.click(p,P,LodgingSite.frame(0));f.click(p,P,LodgingSite.DANIEL_FRAME.above());f.click(p,P,new BlockPos(-11,1,LodgingSite.FRONT));f.click(p,P,LodgingSite.HALL_STAND.above());
            f.click(p,P,LodgingSite.RATCHET);f.click(p,P,LodgingSite.DEAD_POST.above());
        // What is said reaches a reader when the tick ends.
        }).thenIdle(2).thenExecute(()->{
            var why=told(p);
            h.assertTrue(why.contains(LodgingTexts.FRAME)&&why.contains(LodgingTexts.DANIELS_FRAME)&&why.contains(LodgingTexts.SHUTTERS)&&why.contains(LodgingTexts.GLASS)&&why.contains(LodgingTexts.RATCHET)&&why.contains(LodgingTexts.DEAD_POST),"each says what it is: "+why);
            // Daniel's bolt is drawn by the first hand, and the second opens the door.
            f.click(p,P,LodgingSite.DANIEL_DOOR);
            h.assertTrue(!LodgingSite.open(l,b,LodgingSite.DANIEL_DOOR)&&!LodgingBlock.bolted(l.getBlockState(b.offset(LodgingSite.DANIEL_DOOR)))&&!LodgingBlock.bolted(l.getBlockState(b.offset(LodgingSite.DANIEL_DOOR.above()))),"the first hand draws the bolt, both cells");
        }).thenIdle(2).thenExecute(()->{
            h.assertTrue(told(p).contains(LodgingTexts.BOLT_DRAWN),"and says the bolt is on this side");
            for(var door:LodgingSite.doors()){f.click(p,P,door);h.assertTrue(LodgingSite.open(l,b,door)&&LodgingSite.open(l,b,door.above()),"it opens to a hand, both cells: "+door);}
            f.click(p,P,LodgingSite.PARLOR_DOOR.above());h.assertFalse(LodgingSite.open(l,b,LodgingSite.PARLOR_DOOR),"a hand on its upper cell shuts it");f.click(p,P,LodgingSite.PARLOR_DOOR);
            f.click(p,P,LodgingSite.DANIEL_DOOR);h.assertTrue(LodgingHouse.bolt(l,b)&&LodgingBlock.bolted(l.getBlockState(b.offset(LodgingSite.DANIEL_DOOR))),"shut, its bolt can be shot again");
            f.click(p,P,LodgingSite.DANIEL_DOOR);f.click(p,P,LodgingSite.DANIEL_DOOR);h.assertTrue(LodgingSite.open(l,b,LodgingSite.DANIEL_DOOR),"and drawn and opened again");
            // The hatches slide open and shut.
            for(var hatch:List.of(LodgingSite.PARLOR_HATCH,LodgingSite.DINING_HATCH,LodgingSite.FOOD_HATCH,new BlockPos(LodgingSite.ROOM_W,LodgingSite.UPPER+1,LodgingSite.HATCH_Z[1]))){
                h.assertTrue(part(l,b,hatch)==LodgingWall.Part.HATCH,"a hatch, shut: "+hatch);
                f.click(p,P,hatch);h.assertTrue(LodgingHouse.hatchOpen(l,b,hatch),"slid open: "+hatch);
                f.click(p,P,hatch);h.assertTrue(part(l,b,hatch)==LodgingWall.Part.HATCH,"and shut again: "+hatch);
            }
            // Open, every room downstairs, the cupboard under the stair, the passages behind the walls.
            var in=walked(l,b,new BlockPos(0,0,-1));
            for(var room:List.of(new BlockPos(-9,0,-6),new BlockPos(12,0,-2),new BlockPos(-8,0,-22),new BlockPos(11,0,-20),new BlockPos(0,0,-25),new BlockPos(3,0,-10),new BlockPos(LodgingSite.PASS_W,0,-2),
                    new BlockPos(LodgingSite.PASS_W,0,-29),new BlockPos(0,0,LodgingSite.PASS_N),new BlockPos(LodgingSite.PASS_E,0,-2)))
                h.assertTrue(in.contains(room),"a body walks to "+room);
            var up=walked(l,b,new BlockPos(2,LodgingSite.UPPER,-12));
            for(int i=0;i<6;i++){var o=LodgingSite.frame(i);h.assertTrue(up.contains(o.relative(LodgingSite.frameFacing(i))),"upstairs, into room "+(i+1)+", up to its frame");}
            for(var cell:List.of(new BlockPos(-8,LodgingSite.UPPER,-22),new BlockPos(8,LodgingSite.UPPER,-22),new BlockPos(LodgingSite.PASS_E,LodgingSite.UPPER,-5),new BlockPos(LodgingSite.PASS_W,LodgingSite.UPPER,-5)))
                h.assertTrue(up.contains(cell),"the workroom, Vera's room, and the passages upstairs, round the back: "+cell);
            // The cellar, from the back stair's foot: the sill's ratchet, the dead post, and the ladders up.
            var under=walked(l,b,new BlockPos(0,LodgingSite.CELLAR,LodgingSite.CELLAR_STAIR_TOP-LodgingSite.CELLAR_TREADS));
            for(var cell:List.of(LodgingSite.RATCHET.south(),LodgingSite.DEAD_POST.east(),new BlockPos(LodgingSite.PASS_W,LodgingSite.CELLAR,LodgingSite.LADDER_Z-1),new BlockPos(LodgingSite.PASS_E,LodgingSite.CELLAR,LodgingSite.LADDER_Z-1)))
                h.assertTrue(under.contains(cell),"under the house, over the sill, to the ratchet, the dead post and the foot of both ladders: "+cell+" of "+under.size());
            // The first paper on the hall table, and the House's dealing.
            h.assertTrue(fitting(l,b,LodgingSite.SOURCE,LodgingBlock.Kind.HALL_TABLE)&&fitting(l,b,LodgingSite.ENDING,LodgingBlock.Kind.BENCH),"the hall table, and the bench across from it");
            var paper=LiteraryTexts.source(P).get(DataComponents.WRITTEN_BOOK_CONTENT);String text=String.join(" ",paper.pages().stream().map(pg->pg.raw().getString()).toList());
            h.assertTrue(text.contains("TERMS")&&text.contains("sleeper in your room")&&paper.author().equals("V."),"the terms of the house, signed V.");
            for(String word:List.of(" he "," she "," his "," her "," him "))h.assertFalse(text.toLowerCase(java.util.Locale.ROOT).contains(word),"the terms say nothing of what the guest is: "+word);
            h.assertFalse(LiteraryVignettes.canDeal(f.data(),p.getUUID(),P),"a place still being built is never dealt");
        }).thenSucceed();
    }
    @AfterBatch(batch="lodging_built") public static void builtDone(ServerLevel l){done();}

    private static boolean thing(ServerLevel l,BlockPos b,BlockPos at,LodgingThing.Kind kind){return LodgingThing.is(l.getBlockState(b.offset(at)),kind);}
    /**
     * 0.4.550: what is in the rooms. In each guest room a bed made and turned down, its head to a wall, a washstand and a chair by the
     * bed's head, and by its door the holder for a card, empty; the parlor's settee facing the grate with an armchair either side; the
     * long table with ten chairs and the sideboard; Daniel's cot, the shelf under his hatch and his chair turned to the frame; the range
     * in and lit, its flue, the sink, the dresser and the tray laid for one; the workroom's preparations, the record of guests and the key
     * on its hook; Vera's bed made tight; the rope by the dead post. Everything stands on the floor or hangs on a wall, and nothing in a
     * doorway or before a frame or a hatch. A body sits in a chair, an armchair and the settee and faces as they face, and each thing says
     * what it is, none of it what the reader is.
     */
    @GameTest(template="empty",batch="lodging_rooms",timeoutTicks=1200)
    public static void whatIsInTheRoomsStandsInItsPlaceAndAnswersAHand(GameTestHelper h){
        var f=house(h);var p=f.player("lodging_guest",P);var l=f.level(P);var b=f.base(P);
        h.startSequence().thenIdle(5).thenExecute(()->{
            for(int i=0;i<6;i++){
                var head=LodgingSite.bed(i);var face=LodgingSite.bedFacing(i);var hs=l.getBlockState(b.offset(head));var fs=l.getBlockState(b.offset(LodgingThing.ahead(head,face)));
                h.assertTrue(LodgingThing.is(hs,LodgingThing.Kind.BED)&&hs.getValue(LodgingThing.STAGE)==0&&hs.getValue(LodgingThing.FACING)==face&&LodgingThing.is(fs,LodgingThing.Kind.BED)&&fs.getValue(LodgingThing.STAGE)==1,"room "+(i+1)+"'s bed, its head and its foot");
                h.assertFalse(l.isEmptyBlock(b.offset(head.relative(face.getOpposite()))),"room "+(i+1)+"'s bed has its head to a wall");
                h.assertTrue(thing(l,b,LodgingSite.washstand(i),LodgingThing.Kind.WASHSTAND)&&thing(l,b,LodgingSite.guestChair(i),LodgingThing.Kind.CHAIR)&&thing(l,b,LodgingSite.cardHolder(i),LodgingThing.Kind.CARD_HOLDER)
                    &&l.getBlockState(b.offset(LodgingSite.cardHolder(i))).getValue(LodgingThing.STAGE)==0,"room "+(i+1)+"'s washstand and chair, and the empty holder by its door");
                var before=LodgingSite.frame(i).relative(LodgingSite.frameFacing(i));
                var hatch=new BlockPos(i<3?LodgingSite.ROOM_W:LodgingSite.ROOM_E,LodgingSite.UPPER,LodgingSite.HATCH_Z[i%3]).relative(LodgingSite.frameFacing(i));
                h.assertTrue(l.isEmptyBlock(b.offset(before))&&l.isEmptyBlock(b.offset(before.above()))&&l.isEmptyBlock(b.offset(hatch))&&l.isEmptyBlock(b.offset(hatch.above())),"nothing before room "+(i+1)+"'s frame or its hatch");
            }
            for(var door:LodgingSite.doors()){var facing=l.getBlockState(b.offset(door)).getValue(LodgingBlock.FACING);
                for(var side:List.of(door.relative(facing),door.relative(facing.getOpposite())))h.assertTrue(l.isEmptyBlock(b.offset(side))&&l.isEmptyBlock(b.offset(side.above())),"nothing in the way of the door at "+door+": "+side);}
            h.assertTrue(l.isEmptyBlock(b.offset(LodgingSite.ONWARD.south())),"nor before the way on");
            h.assertTrue(thing(l,b,LodgingSite.SETTEE,LodgingThing.Kind.SETTEE)&&thing(l,b,LodgingThing.right(LodgingSite.SETTEE,Direction.NORTH),LodgingThing.Kind.SETTEE)&&thing(l,b,LodgingSite.SIDE_TABLE,LodgingThing.Kind.SIDE_TABLE)
                &&LodgingSite.ARMCHAIRS.stream().allMatch(a->thing(l,b,a,LodgingThing.Kind.ARMCHAIR))&&LodgingSite.PARLOR_CHAIRS.stream().allMatch(a->thing(l,b,a,LodgingThing.Kind.CHAIR)),"the parlor's settee facing the grate, the armchairs either side, the table and the chairs");
            for(int x=LodgingSite.TABLE_W;x<=LodgingSite.TABLE_E;x++)for(int z=LodgingSite.TABLE_N;z<=LodgingSite.TABLE_S;z++)h.assertTrue(thing(l,b,new BlockPos(x,0,z),LodgingThing.Kind.LONG_TABLE),"the long table at "+x+","+z);
            int chairs=0;for(int x=5;x<=13;x++)for(int z=-11;z<=-1;z++)if(thing(l,b,new BlockPos(x,0,z),LodgingThing.Kind.CHAIR))chairs++;
            h.assertTrue(chairs==10&&thing(l,b,LodgingSite.SIDEBOARD,LodgingThing.Kind.SIDEBOARD),"ten chairs round it, and the sideboard: "+chairs);
            h.assertTrue(thing(l,b,LodgingSite.COT,LodgingThing.Kind.COT)&&thing(l,b,LodgingSite.COT.south(),LodgingThing.Kind.COT)&&thing(l,b,LodgingSite.SHELF,LodgingThing.Kind.SHELF)&&LodgingSite.SHELF.above().equals(LodgingSite.FOOD_HATCH.east())
                &&thing(l,b,LodgingSite.DANIEL_CHAIR,LodgingThing.Kind.CHAIR)&&l.getBlockState(b.offset(LodgingSite.DANIEL_CHAIR)).getValue(LodgingThing.FACING)==Direction.NORTH,"Daniel's cot, the shelf under his hatch, his chair turned to the frame");
            var tray=l.getBlockState(b.offset(LodgingSite.TRAY));var range=l.getBlockState(b.offset(LodgingSite.RANGE));
            h.assertTrue(LodgingThing.is(tray,LodgingThing.Kind.SCULLERY_TABLE)&&(tray.getValue(LodgingThing.STAGE)&2)==2&&LodgingThing.is(range,LodgingThing.Kind.RANGE)&&range.getLightEmission(l,b.offset(LodgingSite.RANGE))>0
                &&thing(l,b,LodgingSite.RANGE.above(3),LodgingThing.Kind.RANGE)&&thing(l,b,LodgingSite.SINK,LodgingThing.Kind.SINK)&&thing(l,b,LodgingSite.DRESSER,LodgingThing.Kind.DRESSER)&&thing(l,b,LodgingSite.DRESSER.above(),LodgingThing.Kind.DRESSER),
                "the scullery: the tray laid, the range in with its flue to the ceiling, the sink, the dresser");
            h.assertTrue(thing(l,b,LodgingSite.WORK_TABLE,LodgingThing.Kind.WORK_TABLE)&&thing(l,b,LodgingThing.right(LodgingSite.WORK_TABLE,Direction.NORTH),LodgingThing.Kind.WORK_TABLE)&&thing(l,b,LodgingSite.RECORD,LodgingThing.Kind.CARD_INDEX)
                &&thing(l,b,LodgingSite.KEY_HOOK,LodgingThing.Kind.KEY_HOOK)&&l.getBlockState(b.offset(LodgingSite.KEY_HOOK)).getValue(LodgingThing.STAGE)==0&&LodgingSite.inWorkroom(Vec3.atCenterOf(LodgingSite.KEY_HOOK)),"the workroom's preparations, the record of guests, and the key on its hook");
            var vera=l.getBlockState(b.offset(LodgingSite.VERA_BED));
            h.assertTrue(LodgingThing.is(vera,LodgingThing.Kind.BED)&&vera.getValue(LodgingThing.STAGE)==2&&thing(l,b,LodgingSite.DRESSING_TABLE,LodgingThing.Kind.DRESSING_TABLE)&&thing(l,b,LodgingSite.VERA_WARDROBE.above(),LodgingThing.Kind.WARDROBE),"Vera's bed made tight, her dressing table, her wardrobe");
            h.assertTrue(thing(l,b,LodgingSite.ROPE,LodgingThing.Kind.ROPE)&&LodgingSite.ROPE.distManhattan(LodgingSite.DEAD_POST)==1&&LodgingSite.CRATES.stream().allMatch(c->thing(l,b,c,LodgingThing.Kind.CRATE))&&thing(l,b,LodgingSite.COAL,LodgingThing.Kind.COAL),"the rope beside the dead post, the crates, the coal");
            // Everything stands on the floor or hangs on a wall.
            int things=0;
            for(int x=LodgingSite.WEST;x<=LodgingSite.EAST;x++)for(int y=LodgingSite.CELLAR;y<=LodgingSite.ROOF;y++)for(int z=LodgingSite.BACK;z<=LodgingSite.FRONT;z++){
                var at=new BlockPos(x,y,z);var s=l.getBlockState(b.offset(at));if(!LodgingThing.is(s))continue;things++;var kind=s.getValue(LodgingThing.KIND);
                if(kind==LodgingThing.Kind.KEY_HOOK||kind==LodgingThing.Kind.CARD_HOLDER||kind==LodgingThing.Kind.SHELF)h.assertFalse(l.isEmptyBlock(b.offset(at.relative(s.getValue(LodgingThing.FACING).getOpposite()))),"hung on a wall: "+kind+" at "+at);
                else h.assertFalse(l.isEmptyBlock(b.offset(at.below())),"standing on something: "+kind+" at "+at);
            }
            h.assertTrue(things==90,"ninety things in the rooms: "+things);
            // A body sits in a chair, an armchair, the settee and at the table, facing as each faces.
            for(var seat:List.of(LodgingSite.guestChair(0),LodgingSite.ARMCHAIRS.get(0),LodgingSite.SETTEE,new BlockPos(LodgingSite.TABLE_W-1,0,-5))){
                var at=b.offset(seat);p.moveTo(at.getX()+.5,at.getY(),at.getZ()+1.5);
                h.assertTrue(io.github.knaitoe.theoldesthouse.house.HouseSitting.sit(p,at)&&p.getVehicle() instanceof io.github.knaitoe.theoldesthouse.house.SeatEntity,"a body sits in "+seat);
                h.assertTrue(Math.abs(net.minecraft.util.Mth.wrapDegrees(p.getYRot()-l.getBlockState(at).getValue(LodgingThing.FACING).toYRot()))<1,"facing as it faces: "+seat+", "+p.getYRot());
                p.stopRiding();
            }
            told(p);p.setShiftKeyDown(true);
            for(var at:List.of(LodgingSite.bed(0),LodgingSite.washstand(0),LodgingSite.guestChair(0),LodgingSite.cardHolder(0),LodgingSite.ARMCHAIRS.get(0),LodgingSite.COT,LodgingSite.SHELF,LodgingSite.DANIEL_CHAIR,
                    LodgingSite.TRAY,LodgingSite.RANGE,LodgingSite.WORK_TABLE,LodgingSite.RECORD,LodgingSite.KEY_HOOK,LodgingSite.VERA_BED,LodgingSite.ROPE))f.click(p,P,at);
        }).thenIdle(2).thenExecute(()->{
            var why=told(p);p.setShiftKeyDown(false);
            var lines=List.of(LodgingTexts.BED,LodgingTexts.WASHSTAND,LodgingTexts.GUEST_CHAIR,LodgingTexts.CARD_HOLDER,LodgingTexts.ARMCHAIR,LodgingTexts.COT,LodgingTexts.SHELF,LodgingTexts.DANIEL_CHAIR,
                LodgingTexts.TRAY,LodgingTexts.RANGE,LodgingTexts.PREPARATIONS,LodgingTexts.RECORD,LodgingTexts.KEY,LodgingTexts.BED_VERA,LodgingTexts.ROPE);
            for(var line:lines)h.assertTrue(why.contains(line),"each says what it is: "+line+" in "+why);
            for(var line:lines)for(String word:List.of(" he "," she "," his "," her "," him "," you "))h.assertFalse((" "+line.toLowerCase(java.util.Locale.ROOT)+" ").contains(word),"nothing of what the reader is: "+line);
        }).thenSucceed();
    }
    @AfterBatch(batch="lodging_rooms") public static void roomsDone(ServerLevel l){done();}

    /**
     * 0.4.551 (the owner, 13 November: "All multiplayer vignettes should be hooked through that system. If full"): six readers to a
     * house. A seventh waits at the House while a house of their own is made ready, built as the first is, and goes into it by its own
     * door; one of the six goes back into theirs.
     */
    @GameTest(template="empty",batch="lodging_copy",timeoutTicks=24000)
    public static void aSeventhReaderIsGivenAHouseOfTheirOwn(GameTestHelper h){
        var f=house(h);var l=f.level(P);var b=f.base(P);var server=l.getServer();final ServerPlayer[] p=new ServerPlayer[LodgingHouse.PARTY+1];final BlockPos[] theirs={null};
        var whole=new AABB(b.getX()+LodgingSite.WEST,b.getY()+LodgingSite.CELLAR_SLAB,b.getZ()+LodgingSite.BACK,b.getX()+LodgingSite.EAST+1,b.getY()+LodgingSite.ROOF+1,b.getZ()+LodgingSite.FRONT+1);
        SceneInstances.fixtureCap(1);
        h.startSequence().thenExecute(()->{
                h.assertTrue(SceneInstances.copyable(P)&&SceneInstances.party(P)==LodgingHouse.PARTY&&LodgingHouse.PARTY==6,"the lodging house has copies, six readers to a house");
                for(int i=0;i<LodgingHouse.PARTY;i++)p[i]=f.player("lodging_copy"+i,P);
                var late=NativeTestPlayers.survival(h,"lodging_copy_late");p[LodgingHouse.PARTY]=late;f.players.add(late);
                late.teleportTo(f.in,f.origin.getX()+.5,f.origin.getY()+2,f.origin.getZ()+.5,0,0);late.setNoGravity(true);
                h.assertTrue(SceneInstances.held(server,P,0,late.getUUID())==6&&SceneInstances.held(server,P,0,p[0].getUUID())==5,"six in the house: full to a seventh, never to one of the six");
                var e=SceneInstances.enter(late,P);
                h.assertTrue(e.door()==null&&e.waiting()&&SceneInstances.copies(f.data(),P)==1,"the seventh's door waits while a house of their own is made ready: "+e);
                var mine=SceneInstances.enter(p[0],P);
                h.assertTrue(mine.door()!=null&&mine.copy()==0,"one of the six goes back into theirs: "+mine);
            })
            .thenWaitUntil(()->h.assertTrue(SceneInstances.built(f.data(),P,1)&&!SceneInstances.building(),"waiting for the second house to stand"))
            .thenExecute(()->{
                theirs[0]=SceneInstances.base(server,P,1);var late=p[LodgingHouse.PARTY];
                h.assertTrue(theirs[0].getX()<=b.getX()-SceneInstances.WEST&&theirs[0].getY()==b.getY()&&theirs[0].getZ()==b.getZ(),"it stands a million blocks west of the first: "+theirs[0]);
                var e=SceneInstances.enter(late,P);
                h.assertTrue(e.door()!=null&&e.copy()==1&&e.door().id.equals(SceneInstances.doorId(P,1,"entry"))&&SceneInstances.kept(f.data(),late.getUUID(),P)==1,"the seventh goes into it by its own door: "+e);
                h.assertTrue(SceneInstances.held(server,P,1,late.getUUID())==0,"and nobody else is in it");
                var by=theirs[0].subtract(b);f.chunks.hold(l,whole);f.chunks.hold(l,whole.move(by.getX(),by.getY(),by.getZ()));
            })
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"both houses are resident"))
            .thenExecute(()->{
                // Built as the first is: the terms on the hall table, the frames, the furniture, the ratchet under the house.
                var by=theirs[0].subtract(b);
                for(var at:List.of(LodgingSite.SOURCE,LodgingSite.HALL_STAND,LodgingSite.DANIEL_FRAME,LodgingSite.DANIEL_DOOR,LodgingSite.bed(0),LodgingSite.bed(5),LodgingSite.SETTEE,LodgingSite.RANGE,LodgingSite.KEY_HOOK,LodgingSite.RATCHET,LodgingSite.DEAD_POST,LodgingSite.ONWARD)){
                    var first=l.getBlockState(b.offset(at));var second=l.getBlockState(b.offset(at).offset(by));
                    h.assertTrue(!first.isAir()&&second.equals(first),"the second house has what the first has at "+at+": "+first+" and "+second);
                }
            }).thenSucceed();
    }
    @AfterBatch(batch="lodging_copy") public static void copyDone(ServerLevel l){SceneInstances.fixtureCap(null);SceneInstances.clear();done();}
}
