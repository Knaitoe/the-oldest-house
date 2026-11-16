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
import net.minecraft.nbt.CompoundTag;
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
        // The server holds a player's packets until the end of its tick; what was said in this one is read too.
        NativeTestPlayers.flush(p);
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
            h.assertTrue(fitting(l,b,new BlockPos(2,LodgingSite.UPPER,LodgingSite.STAIR_FOOT-1),LodgingBlock.Kind.RAIL)&&fitting(l,b,new BlockPos(3,LodgingSite.UPPER,LodgingSite.STAIR_FOOT-1),LodgingBlock.Kind.RAIL)
                &&fitting(l,b,new BlockPos(2,LodgingSite.UPPER,LodgingSite.STAIR_FOOT-2),LodgingBlock.Kind.RAIL)&&fitting(l,b,new BlockPos(2,LodgingSite.UPPER,LodgingSite.STAIR_FOOT-3),LodgingBlock.Kind.RAIL)&&fitting(l,b,new BlockPos(1,0,LodgingSite.STAIR_FOOT),LodgingBlock.Kind.BANISTER),"the gallery's rail round the well, and the newel at the stair's foot");
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
            h.assertTrue(why.contains(LodgingTexts.FRAME)&&why.contains(LodgingTexts.DANIELS_FRAME)&&why.contains(LodgingTexts.SHUTTERS)&&why.contains(LodgingTexts.GLASS)&&why.contains(LodgingTexts.RATCHET)&&why.stream().anyMatch(w->w.startsWith(LodgingTexts.DEAD_POST)),"each says what it is: "+why);
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
            // 0.4.618: it can be finished, so the House deals it to a reader who has not read their account of it.
            h.assertTrue(LodgingHouse.dealable(f.data(),p.getUUID()),"dealt to a reader who has not had it");
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

    private static CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),P);}
    private static Vec3 rel(BlockPos b,net.minecraft.world.entity.Entity e){return e.position().subtract(b.getX(),b.getY(),b.getZ());}
    private static void has(GameTestHelper h,ServerPlayer p,String line){h.assertTrue(Talk.lines(p).contains(line),"the panel has \""+line+"\": "+Talk.lines(p));}
    /**
     * 0.4.602, the people (docs/ONES_INSIDE_PEOPLE_0_4_602.md). Each of the seven is where the script's first scene has them, doing what
     * they do there, and is made once. The four turn their heads, and not their bodies, to a reader they can see; nobody turns to one they
     * cannot. A hand on Rae, Dan, Vera or Daniel opens the panel with the script's lines; what a reader answers is in that reader's own
     * record and nobody else's. A hand on a failed body gets what it is doing and no panel.
     */
    @GameTest(template="empty",batch="lodging_people",timeoutTicks=3000)
    public static void theFourAreWhereTheScriptHasThemAndAnswerAHandInThePanel(GameTestHelper h){
        var f=house(h);var p=f.player("lodging_talker",P);var q=f.player("lodging_other",P);var l=f.level(P);var b=f.base(P);
        f.chunks.hold(l,LodgingPeople.area(b));final LiteraryActor[] who=new LiteraryActor[LodgingPeople.COUNT];
        // 0.4.604: both readers answer Rae here, which sets the house's chapter going; this case looks at the first scene only.
        LodgingStory.hold(b,true);
        h.startSequence()
            .thenWaitUntil(()->{h.assertTrue(f.chunks.ready(),"the house is resident");LodgingPeople.ensure(l,b);
                for(int i=0;i<LodgingPeople.COUNT;i++){who[i]=LodgingPeople.find(l,b,i);h.assertTrue(who[i]!=null,"waiting for number "+i+" of the house's people");}})
            .thenExecute(()->{
                for(int i=0;i<LodgingPeople.COUNT;i++){
                    var a=who[i];var s=LodgingPeople.SPOTS[i];
                    h.assertTrue(a.position().distanceTo(LodgingPeople.world(b,s))<.05&&a.gesture()==s.gesture()&&net.minecraft.util.Mth.degreesDifferenceAbs(a.getYRot(),s.look())<.5F,"number "+i+" is where they belong, turned as they are and doing what they do: "+rel(b,a)+" gesture "+a.gesture()+" yaw "+a.getYRot());
                    h.assertTrue(a.role()==LiteraryActor.STRANGER&&a.phase()==LodgingPeople.FIRST+i&&a.holdsBody()&&a.ownGestures(),"number "+i+" is one of the house's own");
                }
                LodgingPeople.ensure(l,b);
                h.assertTrue(l.getEntitiesOfClass(LiteraryActor.class,LodgingPeople.area(b),LodgingPeople::is).size()==LodgingPeople.COUNT,"seven of them, and asking again makes no eighth");
                // Rae in the hall; Dan sat on its bench; Vera at her table upstairs; Daniel in his frame behind his bolted door.
                h.assertTrue(LodgingSite.inHall(rel(b,who[LodgingPeople.RAE]))&&ActorSeat.of(who[LodgingPeople.RAE])==null,"Rae stands in the front hall");
                var seat=ActorSeat.of(who[LodgingPeople.DAN]);
                h.assertTrue(seat!=null&&seat.seatHeight()==8&&seat.facing()==Direction.WEST&&who[LodgingPeople.DAN].blockPosition().equals(b.offset(LodgingSite.ENDING.north())),"Dan sits on the hall bench's far end, facing into the hall: "+seat);
                h.assertTrue(LodgingSite.inWorkroom(rel(b,who[LodgingPeople.VERA]))&&l.getBlockState(who[LodgingPeople.VERA].blockPosition()).isAir(),"Vera stands in the workroom, on clear floor");
                h.assertTrue(who[LodgingPeople.DANIEL].blockPosition().equals(b.offset(LodgingSite.DANIEL_FRAME))&&fitting(l,b,LodgingSite.DANIEL_FRAME,LodgingBlock.Kind.FRAME_BACK)&&LodgingBlock.bolted(l.getBlockState(b.offset(LodgingSite.DANIEL_DOOR))),
                    "Daniel stands in his frame's opening, and his door is bolted");
                var back=who[LodgingPeople.DANIEL].getBoundingBox();
                h.assertTrue(back.minZ>b.getZ()+LodgingSite.DANIEL_FRAME.getZ()+6/16.0-1e-6&&back.minX>=b.getX()+LodgingSite.DANIEL_FRAME.getX()&&back.maxX<=b.getX()+LodgingSite.DANIEL_FRAME.getX()+1,"he is in front of its plaster and between its jambs: "+back);
                // The failed bodies: one sat tied at the dead post's foot, one in each of two passages.
                var tied=who[LodgingPeople.FAILED_POST];var floor=ActorSeat.of(tied);
                h.assertTrue(LodgingSite.inCellar(rel(b,tied))&&floor!=null&&floor.seatHeight()==1&&tied.blockPosition().equals(b.offset(LodgingSite.DEAD_POST.east())),"a failed body sits on the cellar floor against the dead post: "+rel(b,tied));
                h.assertTrue(LodgingSite.inPassages(rel(b,who[LodgingPeople.FAILED_DRESS]))&&LodgingSite.upstairs(rel(b,who[LodgingPeople.FAILED_DRESS]))&&part(l,b,new BlockPos(LodgingSite.ROOM_E,LodgingSite.UPPER+1,LodgingSite.HATCH_Z[1]))==LodgingWall.Part.HATCH,
                    "one stands in the upper passage at a guest room's hatch");
                h.assertTrue(LodgingSite.inPassages(rel(b,who[LodgingPeople.FAILED_SHIRT]))&&!LodgingSite.upstairs(rel(b,who[LodgingPeople.FAILED_SHIRT]))&&fitting(l,b,LodgingSite.JIB,LodgingBlock.Kind.JIB),"and one in the lower passage at the jib door");
                h.assertTrue(who[LodgingPeople.FAILED_DRESS].getScale()<1&&who[LodgingPeople.FAILED_SHIRT].getScale()>1&&who[LodgingPeople.RAE].getScale()==1,"two of the failed bodies are a little the wrong size");
                // A reader beside Rae, to her west: where she can see them, and Daniel cannot. The other is further off, up the hall.
                f.at(p,P,-1.5,0,-3.5);f.at(q,P,.5,0,-10.5);
            })
            .thenIdle(40)
            .thenExecute(()->{
                var rae=who[LodgingPeople.RAE];
                h.assertTrue(Math.abs(net.minecraft.util.Mth.wrapDegrees(rae.getYHeadRot()-LodgingPeople.HEAD_TURN))<1.5F&&net.minecraft.util.Mth.degreesDifferenceAbs(rae.getYRot(),0)<.01F&&net.minecraft.util.Mth.degreesDifferenceAbs(rae.yBodyRot,0)<.01F,
                    "Rae has turned her head to the reader beside her as far as a head turns, and not her body: head "+rae.getYHeadRot()+" body "+rae.getYRot());
                h.assertTrue(net.minecraft.util.Mth.degreesDifferenceAbs(who[LodgingPeople.DANIEL].getYHeadRot(),0)<.01F&&net.minecraft.util.Mth.degreesDifferenceAbs(who[LodgingPeople.VERA].getYHeadRot(),180)<.01F,"Daniel and Vera, who cannot see them, have not");
                // Rae asks the first reader.
                told(p);LiteraryVignettes.talk(p,rae);
                for(var line:LodgingTexts.RAE_REQUEST)has(h,p,"Rae: "+line);
                for(var line:LodgingTexts.DAN_FINE)has(h,p,"Dan: "+line);
                h.assertTrue(Talk.replies(p).equals(List.of(LodgingTexts.R_STAY,LodgingTexts.R_WRONG,LodgingTexts.R_PLACE,LodgingTexts.R_VERA,LodgingTexts.R_NOT)),"her request has the script's five answers: "+Talk.replies(p));
                h.assertTrue(rae.saying()&&who[LodgingPeople.DAN].saying()&&own(p).getBoolean(LodgingPeople.RAE_ASKED)&&!own(q).getBoolean(LodgingPeople.RAE_ASKED),"both have spoken, to this reader and not the other");
                h.assertTrue(Talk.choose(p,LodgingTexts.R_WRONG),"asked what is wrong with him");
                has(h,p,"Rae: "+LodgingTexts.RAE_HAND);has(h,p,"Dan: "+LodgingTexts.DAN_SPRING);has(h,p,"Rae: "+LodgingTexts.RAE_ARRANGEMENT);
                var shown=told(p);
                h.assertTrue(shown.contains(LodgingTexts.DAN_SHOWS),"the reader is told Dan takes his hand out of his pocket: "+shown);
                h.assertTrue(who[LodgingPeople.DAN].gesture()==LodgingPeople.SHOW,"and he holds it out to be looked at: gesture "+who[LodgingPeople.DAN].gesture());
                h.assertTrue(Talk.replies(p).equals(List.of(LodgingTexts.R_STAY,LodgingTexts.R_PLACE,LodgingTexts.R_VERA,LodgingTexts.R_NOT)),"a question asked is not offered again: "+Talk.replies(p));
            })
            .thenIdle(LodgingPeople.SHOWN+5)
            .thenExecute(()->{
                h.assertTrue(who[LodgingPeople.DAN].gesture()==LodgingPeople.POCKET,"and puts it back");
                h.assertTrue(Talk.choose(p,LodgingTexts.R_PLACE),"asked what the place is");has(h,p,"Dan: "+LodgingTexts.DAN_PLACE);
                h.assertTrue(Talk.choose(p,LodgingTexts.R_STAY),"said they would stay with him");has(h,p,"Rae: "+LodgingTexts.RAE_THANKS);
                h.assertTrue(Talk.replies(p).isEmpty()&&LodgingPeople.promisedDan(own(p))&&!LodgingPeople.promisedDan(own(q)),"the promise is this reader's and binds nobody else");
            })
            .thenWaitUntil(()->h.assertTrue(!who[LodgingPeople.RAE].saying()&&!who[LodgingPeople.DAN].saying(),"waiting for them to finish"))
            .thenExecute(()->{
                var rae=who[LodgingPeople.RAE];var dan=who[LodgingPeople.DAN];
                // The second reader goes to Dan before Rae has asked them anything.
                f.at(q,P,2.5,0,-4.5);LiteraryVignettes.talk(q,dan);has(h,q,"Dan: "+LodgingTexts.DAN_FINE[0]);has(h,q,"Dan: "+LodgingTexts.DAN_FINE[1]);
                h.assertTrue(Talk.replies(q).equals(List.of(LodgingTexts.R_WRONG_DAN,LodgingTexts.R_PLACE,LodgingTexts.R_LEAVE_DAN)),"he can be asked what is wrong, and what the place is, or left be: "+Talk.replies(q));
                h.assertTrue(Talk.choose(q,LodgingTexts.R_WRONG_DAN),"asked what is wrong");has(h,q,"Dan: "+LodgingTexts.DAN_SLEEPING);
                h.assertTrue(Talk.choose(q,LodgingTexts.R_PLACE),"asked what the place is");has(h,q,"Dan: "+LodgingTexts.DAN_PLACE);
                h.assertTrue(Talk.replies(q).isEmpty(),"and he has nothing more to be asked");
                // The first reader's later hand on Rae: she has had her answer. And on Dan, who has had his say to them in her request: he
                // says nothing again, and can still be asked what is wrong.
                f.at(p,P,-1.5,0,-3.5);Talk.close(p);told(p);LiteraryVignettes.talk(p,rae);
                h.assertTrue(told(p).contains(LodgingTexts.RAE_WATCHES)&&Talk.key(p).isEmpty(),"a reader who has answered her is not asked again");
                h.assertTrue(own(p).getBoolean(LodgingPeople.DAN_MET),"Dan has had his say to the first reader");
                LiteraryVignettes.talk(p,dan);
                h.assertTrue(told(p).contains(LodgingTexts.DAN_LOOKS_UP)&&Talk.lines(p).isEmpty()&&Talk.replies(p).equals(List.of(LodgingTexts.R_WRONG_DAN,LodgingTexts.R_LEAVE_DAN)),"he does not say it again; what is left to ask him is offered: "+Talk.lines(p)+" "+Talk.replies(p));
                h.assertTrue(Talk.choose(p,LodgingTexts.R_LEAVE_DAN)&&Talk.key(p).isEmpty(),"and he can be left be");
            })
            .thenWaitUntil(()->h.assertTrue(!who[LodgingPeople.RAE].saying()&&!who[LodgingPeople.DAN].saying(),"waiting for them to finish"))
            .thenExecute(()->{
                var rae=who[LodgingPeople.RAE];
                // Then Rae asks the second reader for themselves. Dan has had his say to them: he adds only that they need not. They say
                // it is not their business.
                f.at(q,P,2.5,0,-4.5);Talk.close(q);told(q);LiteraryVignettes.talk(q,rae);for(var line:LodgingTexts.RAE_REQUEST)has(h,q,"Rae: "+line);has(h,q,"Dan: "+LodgingTexts.DAN_FINE[2]);
                h.assertTrue(!Talk.lines(q).contains("Dan: "+LodgingTexts.DAN_FINE[0]),"he does not tell them again that he is fine: "+Talk.lines(q));
                h.assertTrue(Talk.replies(q).equals(List.of(LodgingTexts.R_STAY,LodgingTexts.R_WRONG,LodgingTexts.R_VERA,LodgingTexts.R_NOT)),"what they asked Dan already is not offered again: "+Talk.replies(q));
                h.assertTrue(Talk.choose(q,LodgingTexts.R_NOT),"said it was not their business");has(h,q,"Rae: "+LodgingTexts.RAE_NO);has(h,q,"Dan: "+LodgingTexts.DAN_GOOD);
                h.assertTrue(own(q).getInt(LodgingPeople.RAE_ANSWER)==LodgingPeople.NOT_MINE&&Talk.replies(q).isEmpty()&&!LodgingPeople.promisedDan(own(q)),"and that is their answer");
            })
            .thenWaitUntil(()->h.assertTrue(!who[LodgingPeople.DAN].saying(),"waiting for Dan to finish"))
            .thenExecute(()->{
                f.at(q,P,2.5,0,-4.5);Talk.close(q);told(q);LiteraryVignettes.talk(q,who[LodgingPeople.DAN]);
                h.assertTrue(told(q).contains(LodgingTexts.DAN_SITS)&&Talk.key(q).isEmpty(),"a later hand gets what he is doing");
                // Vera, found upstairs at her work.
                var vera=who[LodgingPeople.VERA];f.at(p,P,-7.5,LodgingSite.UPPER,-19.5);Talk.close(p);LiteraryVignettes.talk(p,vera);
                for(var line:LodgingTexts.VERA_GUESTS)has(h,p,"Vera: "+line);
                h.assertTrue(Talk.replies(p).isEmpty()&&vera.saying(),"she says it and has nothing to be answered");
            })
            .thenWaitUntil(()->h.assertTrue(!who[LodgingPeople.VERA].saying(),"waiting for Vera to finish"))
            .thenExecute(()->{
                f.at(p,P,-7.5,LodgingSite.UPPER,-19.5);Talk.close(p);told(p);LiteraryVignettes.talk(p,who[LodgingPeople.VERA]);
                h.assertTrue(told(p).contains(LodgingTexts.VERA_WORKS),"a later hand gets what she is doing");
                // Daniel, in his room.
                var daniel=who[LodgingPeople.DANIEL];f.at(p,P,-7.5,0,-25.5);told(p);LiteraryVignettes.talk(p,daniel);
                for(var line:LodgingTexts.DANIEL_LISTEN)has(h,p,LodgingTexts.MAN_IN_FRAME+": "+line);
                h.assertTrue(Talk.replies(p).equals(List.of(LodgingTexts.R_DAN_HERE,LodgingTexts.R_WHO,LodgingTexts.R_FREE,LodgingTexts.R_BACK,LodgingTexts.R_NOTHING)),"he has the script's five answers: "+Talk.replies(p));
                h.assertTrue(Talk.choose(p,LodgingTexts.R_WHO),"asked who he is");
                has(h,p,LodgingTexts.MAN_IN_FRAME+": "+LodgingTexts.DANIEL_NAME);has(h,p,"Daniel: "+LodgingTexts.DANIEL_COUNTING);
                h.assertTrue(told(p).contains(LodgingTexts.DANIEL_LOOKS)&&own(p).getBoolean(LodgingPeople.KNOWS_DANIEL)&&!own(q).getBoolean(LodgingPeople.KNOWS_DANIEL),"he has a name for this reader from when he gives it");
                h.assertTrue(Talk.choose(p,LodgingTexts.R_DAN_HERE),"told him Dan is here");has(h,p,"Daniel: "+LodgingTexts.DANIEL_VOICE);
                h.assertTrue(Talk.choose(p,LodgingTexts.R_FREE),"said they would get him out");has(h,p,"Daniel: "+LodgingTexts.DANIEL_KEY);
                h.assertTrue(Talk.replies(p).isEmpty()&&LodgingPeople.promisedDaniel(own(p))&&!LodgingPeople.promisedDaniel(own(q)),"that promise is this reader's too");
            })
            .thenWaitUntil(()->h.assertTrue(!who[LodgingPeople.DANIEL].saying(),"waiting for Daniel to finish"))
            .thenExecute(()->{
                f.at(p,P,-7.5,0,-25.5);Talk.close(p);LiteraryVignettes.talk(p,who[LodgingPeople.DANIEL]);has(h,p,"Daniel: "+LodgingTexts.DANIEL_LISTEN[2]);
                h.assertTrue(Talk.replies(p).equals(List.of(LodgingTexts.R_BACK,LodgingTexts.R_NOTHING))&&Talk.choose(p,LodgingTexts.R_BACK)&&LodgingPeople.promisedDaniel(own(p)),"asked again he has less to be told, and a promise made stays made: "+Talk.replies(p));
                // The failed bodies: a hand gets what each is doing, and no panel; none turns to a reader beside it.
                for(int i=LodgingPeople.FAILED_POST;i<LodgingPeople.COUNT;i++){
                    var a=who[i];var s=LodgingPeople.SPOTS[i];var at=rel(b,a);f.at(p,P,at.x+(i==LodgingPeople.FAILED_POST?1.5:0),at.y,at.z+(i==LodgingPeople.FAILED_POST?0:1.5));
                    Talk.close(p);told(p);LiteraryVignettes.talk(p,a);
                    h.assertTrue(told(p).contains(LodgingTexts.failed(i))&&Talk.key(p).isEmpty()&&!a.saying(),"a hand on failed body "+i+" gets what it is doing, in the house's grey, and nothing said");
                }
            })
            .thenIdle(30)
            .thenExecute(()->{
                var a=who[LodgingPeople.FAILED_SHIRT];
                h.assertTrue(net.minecraft.util.Mth.degreesDifferenceAbs(a.getYHeadRot(),LodgingPeople.SPOTS[LodgingPeople.FAILED_SHIRT].look())<.01F,"a failed body does not turn to a reader beside it: "+a.getYHeadRot());
            }).thenSucceed();
    }
    @AfterBatch(batch="lodging_people") public static void peopleDone(ServerLevel l){done();}

    /** A reader turned to look at a point, as their own camera would be. */
    private static void look(ServerPlayer p,Vec3 at){var d=at.subtract(p.getEyePosition());float yaw=(float)(net.minecraft.util.Mth.atan2(d.z,d.x)*net.minecraft.util.Mth.RAD_TO_DEG)-90;float pitch=(float)-(net.minecraft.util.Mth.atan2(d.y,d.horizontalDistance())*net.minecraft.util.Mth.RAD_TO_DEG);p.setYRot(yaw);p.setXRot(pitch);p.setYHeadRot(yaw);}
    private static String cardText(ServerLevel l,BlockPos b,int room){
        for(var d:l.getEntitiesOfClass(net.minecraft.world.entity.Display.TextDisplay.class,new AABB(b.offset(LodgingSite.cardHolder(room))).inflate(.6),d->d.getTags().contains(LodgingDoubles.CARD_TAG+room))){var tag=new CompoundTag();d.saveWithoutId(tag);return tag.getString("text");}
        return "";
    }
    /**
     * 0.4.604, N1's end and N2 (docs/ONES_INSIDE_ROOMS_0_4_604.md). When every reader has answered Rae she says she will find Vera and walks
     * up the stair and along the corridor into the workroom, the door opened for her and shut behind her. Vera comes down with Rae behind
     * her and says her four lines in the hall. Each reader is given a room of their own, their name on its card; a body is made asleep in
     * its bed only once nobody has the bed in view, and it is that reader's. Standing by it three seconds, its own reader sees it open its
     * eyes and turn its head on the pillow, away from them. A blow does nothing to it; Vera, asked about it, answers in the panel. Vera
     * takes Dan up with Rae, and Dan, passing a guest room open on its sleeper, says his line.
     */
    @GameTest(template="empty",batch="lodging_rooms_given",timeoutTicks=8000)
    public static void theRoomsAreGivenOutAndEachReaderFindsTheirOwnBodyAsleepInTheBed(GameTestHelper h){
        var f=house(h);var p=f.player("lodging_first",P);var q=f.player("lodging_second",P);var l=f.level(P);var b=f.base(P);
        f.chunks.hold(l,LodgingPeople.area(b));final LiteraryActor[] who=new LiteraryActor[LodgingPeople.COUNT];final LiteraryActor[] mine=new LiteraryActor[2];final int[] rooms=new int[2];
        h.startSequence()
            .thenWaitUntil(()->{h.assertTrue(f.chunks.ready(),"the house is resident");LodgingPeople.ensure(l,b);
                for(int i=0;i<LodgingPeople.COUNT;i++){who[i]=LodgingPeople.find(l,b,i);h.assertTrue(who[i]!=null,"waiting for number "+i+" of the house's people");}})
            .thenExecute(()->{
                h.assertTrue(LodgingStory.stage(l.getServer(),b)==LodgingStory.ASKING,"the house begins with Rae asking");
                f.at(p,P,-1.5,0,-3.5);f.at(q,P,.5,0,-4.5);
                LiteraryVignettes.talk(p,who[LodgingPeople.RAE]);h.assertTrue(Talk.choose(p,LodgingTexts.R_STAY),"the first reader will stay with Dan");
            })
            .thenIdle(20)
            .thenExecute(()->{
                h.assertTrue(LodgingStory.stage(l.getServer(),b)==LodgingStory.ASKING,"one reader's answer is not the party's: she waits for the other");
                Talk.close(q);LiteraryVignettes.talk(q,who[LodgingPeople.RAE]);h.assertTrue(Talk.choose(q,LodgingTexts.R_VERA),"the second would see Vera first");
            })
            .thenWaitUntil(()->h.assertTrue(LodgingStory.stage(l.getServer(),b)==LodgingStory.FETCHING,"waiting for Rae to reach the workroom: stage "+LodgingStory.stage(l.getServer(),b)+", Rae at "+rel(b,who[LodgingPeople.RAE])+" yaw "+who[LodgingPeople.RAE].getYRot()+" "+LodgingStory.house(l.getServer(),b)))
            .thenExecute(()->{
                h.assertTrue(LodgingSite.inWorkroom(rel(b,who[LodgingPeople.RAE])),"Rae walked up to the workroom: "+rel(b,who[LodgingPeople.RAE]));
                h.assertTrue(!LodgingBlock.open(l.getBlockState(b.offset(LodgingSite.WORK_DOOR))),"and shut its door behind her");
            })
            .thenWaitUntil(()->h.assertTrue(LodgingStory.stage(l.getServer(),b)>=LodgingStory.COMING_DOWN,"waiting for Vera to come down"))
            .thenExecute(()->{
                // Both readers go up into the two rooms first given out, each looking at its bed: nobody is to be made in front of them.
                f.at(p,P,11.5,LodgingSite.UPPER,-3.5);look(p,LodgingDoubles.lying(b,3).add(0,.7,0));
                f.at(q,P,-11.5,LodgingSite.UPPER,-3.5);look(q,LodgingDoubles.lying(b,0).add(0,.7,0));
            })
            .thenWaitUntil(()->h.assertTrue(LodgingStory.stage(l.getServer(),b)>=LodgingStory.ADDRESS&&LodgingStory.room(l.getServer(),b,p.getUUID())>=0&&LodgingStory.room(l.getServer(),b,q.getUUID())>=0,
                "waiting for the rooms: stage "+LodgingStory.stage(l.getServer(),b)+", Vera at "+rel(b,who[LodgingPeople.VERA])+", Rae at "+rel(b,who[LodgingPeople.RAE])+" "+LodgingStory.house(l.getServer(),b)))
            .thenExecute(()->{
                h.assertTrue(LodgingSite.inHall(rel(b,who[LodgingPeople.VERA]))&&LodgingSite.inHall(rel(b,who[LodgingPeople.RAE])),"Vera came down to the hall, and Rae with her: "+rel(b,who[LodgingPeople.VERA])+" "+rel(b,who[LodgingPeople.RAE]));
                rooms[0]=LodgingStory.room(l.getServer(),b,p.getUUID());rooms[1]=LodgingStory.room(l.getServer(),b,q.getUUID());
                h.assertTrue(rooms[0]!=rooms[1]&&java.util.Set.of(rooms[0],rooms[1]).equals(java.util.Set.of(LodgingStory.ORDER[0],LodgingStory.ORDER[1])),"two readers, two rooms, the first two given: "+rooms[0]+" "+rooms[1]);
                for(int i=0;i<2;i++){var reader=i==0?p:q;int room=rooms[i];
                    h.assertTrue(LodgingThing.is(l.getBlockState(b.offset(LodgingSite.cardHolder(room))),LodgingThing.Kind.CARD_HOLDER)&&l.getBlockState(b.offset(LodgingSite.cardHolder(room))).getValue(LodgingThing.STAGE)==1,"room "+room+"'s holder has its card");
                    h.assertTrue(cardText(l,b,room).contains(reader.getGameProfile().getName()),"and the card has the reader's name on it: "+cardText(l,b,room));}
            })
            .thenIdle(40)
            .thenExecute(()->{
                h.assertTrue(LodgingDoubles.sleeper(l,b,rooms[0])==null&&LodgingDoubles.sleeper(l,b,rooms[1])==null,"no body is made in a bed somebody is looking at");
                // They turn to the door, away from the beds.
                look(p,Vec3.atCenterOf(b.offset(LodgingSite.ROOM_DOORS[3])).add(0,.5,0));p.setXRot(-60);look(q,Vec3.atCenterOf(b.offset(LodgingSite.ROOM_DOORS[0])).add(0,.5,0));q.setXRot(-60);
                f.at(p,P,5.5,LodgingSite.UPPER,-3.5);f.at(q,P,-5.5,LodgingSite.UPPER,-3.5);
            })
            .thenWaitUntil(()->{for(int i=0;i<2;i++){mine[i]=LodgingDoubles.sleeper(l,b,rooms[i]);h.assertTrue(mine[i]!=null,"waiting for the body in room "+rooms[i]);}})
            .thenExecute(()->{
                for(int i=0;i<2;i++){var a=mine[i];var reader=i==0?p:q;
                    h.assertTrue(a.role()==LiteraryActor.LODGING_DOUBLE&&a.owner().isPresent()&&a.owner().get().equals(reader.getUUID()),"the body in room "+rooms[i]+" is that reader's");
                    h.assertTrue(a.position().distanceTo(LodgingDoubles.lying(b,rooms[i]))<.03&&a.gesture()==LodgingDoubles.ASLEEP,"asleep in its bed: "+rel(b,a));
                    h.assertTrue(LodgingThing.is(l.getBlockState(a.blockPosition()),LodgingThing.Kind.BED)&&l.getBlockState(a.blockPosition()).getValue(LodgingThing.STAGE)==0,"its head in the bed's head cell");}
                h.assertTrue(l.getEntitiesOfClass(LiteraryActor.class,LodgingPeople.area(b),LodgingDoubles::is).size()==2,"one body to a reader, and no more");
                // A blow lands on the second reader's body and does nothing.
                float health=mine[1].getHealth();Talk.close(q);told(q);q.attack(mine[1]);
                h.assertTrue(mine[1].isAlive()&&mine[1].getHealth()==health&&own(q).getBoolean(LodgingDoubles.STRUCK),"a blow does nothing to it");
                // The first reader stands beside their own.
                f.at(p,P,LodgingDoubles.lying(b,rooms[0]).x-b.getX()+(rooms[0]<3?-1.5:1.5),LodgingSite.UPPER,LodgingDoubles.lying(b,rooms[0]).z-b.getZ()-1.2);told(p);
            })
            .thenIdle(LodgingDoubles.WAKE+10)
            .thenExecute(()->{
                h.assertTrue(mine[0].gesture()==LodgingDoubles.AWAKE&&own(p).getBoolean(LodgingDoubles.SAW_EYES)&&told(p).contains(LodgingTexts.DOUBLE_WAKES),"standing by it, its reader sees it open its eyes");
                h.assertTrue(Math.abs(net.minecraft.util.Mth.wrapDegrees(mine[0].getYHeadRot()-mine[0].yBodyRot))>LodgingDoubles.TURN-1,"and turn its head on the pillow: "+mine[0].getYHeadRot()+" body "+mine[0].yBodyRot);
                h.assertTrue(mine[1].gesture()==LodgingDoubles.ASLEEP,"the other reader's, whose reader is not by it, sleeps on");
                LodgingDoubles.touched(p,mine[0]);h.assertTrue(told(p).contains(LodgingTexts.DOUBLE_OWN_AWAKE),"a hand on it says what it is");
                // Open the first room's door onto its bed, for Dan to see when he is taken up.
                if(!LodgingBlock.open(l.getBlockState(b.offset(LodgingSite.ROOM_DOORS[rooms[0]]))))LodgingHouse.toggle(l,b,LodgingSite.ROOM_DOORS[rooms[0]]);
                f.at(q,P,.5,LodgingSite.UPPER,-5.5);
            })
            .thenWaitUntil(()->h.assertTrue(LodgingStory.stage(l.getServer(),b)==LodgingStory.SETTLED,"waiting for Vera to take Dan up: stage "+LodgingStory.stage(l.getServer(),b)+", Dan at "+rel(b,who[LodgingPeople.DAN])+", Vera at "+rel(b,who[LodgingPeople.VERA])))
            .thenExecute(()->{
                for(int i:new int[]{LodgingPeople.VERA,LodgingPeople.DAN,LodgingPeople.RAE})h.assertTrue(LodgingSite.inWorkroom(rel(b,who[i])),"number "+i+" went up to the workroom: "+rel(b,who[i]));
                h.assertTrue(LodgingStory.house(l.getServer(),b).getBoolean("DanSaw"),"Dan saw a body through a door on his way up");
                h.assertTrue(who[LodgingPeople.VERA].gesture()==LodgingPeople.WORK,"Vera back at her work");
                // Asked about it in the workroom, Vera answers.
                f.at(p,P,-5.5,LodgingSite.UPPER,-21.5);Talk.close(p);LiteraryVignettes.talk(p,who[LodgingPeople.VERA]);
                h.assertTrue(Talk.replies(p).equals(List.of(LodgingTexts.R_WHO_THAT,LodgingTexts.R_WHAT_THIS,LodgingTexts.R_NOT_STAYING,LodgingTexts.R_LEAVE_IT)),"she can be asked about it: "+Talk.replies(p));
                h.assertTrue(Talk.choose(p,LodgingTexts.R_WHO_THAT),"asked who that is");
                for(var line:LodgingTexts.VERA_YOURS)has(h,p,"Vera: "+line);
                h.assertTrue(Talk.replies(p).equals(List.of(LodgingTexts.R_NOT_STAYING,LodgingTexts.R_LEAVE_IT))&&Talk.choose(p,LodgingTexts.R_NOT_STAYING),"what is left to ask: "+Talk.replies(p));
                has(h,p,"Vera: "+LodgingTexts.VERA_EITHER_WAY);
                h.assertTrue(own(p).getBoolean(LodgingDoubles.ASKED)&&own(p).getBoolean(LodgingDoubles.NOT_STAYING)&&!own(q).getBoolean(LodgingDoubles.ASKED),"what was asked is this reader's own");
            }).thenSucceed();
    }
    @AfterBatch(batch="lodging_rooms_given") public static void roomsGivenDone(ServerLevel l){done();}

    /**
     * 0.4.605, Daniel through the wall (docs/ONES_INSIDE_DANIEL_0_4_605.md). From the back passage he is not spoken to through his shut
     * door with a hand on him, but is crouched at it; through the food hatch, once it is open, from the passage; and in his room. Dan's
     * voice in the hall brings him against the frame, heel to its back and shouting, once a minute, and a reader near enough hears it.
     * The straps are locked.
     */
    @GameTest(template="empty",batch="lodging_daniel",timeoutTicks=3000)
    public static void danielIsSpokenToThroughTheHatchAndTheDoorAndHammersWhenDanSpeaks(GameTestHelper h){
        var f=house(h);var p=f.player("lodging_daniel_a",P);var q=f.player("lodging_daniel_b",P);var l=f.level(P);var b=f.base(P);
        f.chunks.hold(l,LodgingPeople.area(b));LodgingStory.hold(b,true);final LiteraryActor[] who=new LiteraryActor[LodgingPeople.COUNT];
        final long[] at={0};
        h.startSequence()
            .thenWaitUntil(()->{h.assertTrue(f.chunks.ready(),"the house is resident");LodgingPeople.ensure(l,b);
                for(int i=0;i<LodgingPeople.COUNT;i++){who[i]=LodgingPeople.find(l,b,i);h.assertTrue(who[i]!=null,"waiting for number "+i);}})
            .thenExecute(()->{
                var daniel=who[LodgingPeople.DANIEL];
                // At his shut door, standing: a hand on him through it says nothing.
                p.teleportTo(l,b.getX()-7.5,b.getY(),b.getZ()-14.5,180,0);p.setShiftKeyDown(false);
                LodgingPeople.talk(p,daniel,own(p));
                h.assertFalse(Talk.lines(p).contains(LodgingTexts.MAN_IN_FRAME+": "+LodgingTexts.DANIEL_LISTEN[0]),"nobody speaks to him through a shut door with a hand on him: "+Talk.lines(p));
                // Crouched at it, a hand on the door speaks through it, and the bolt stays shot.
                p.setShiftKeyDown(true);
                h.assertTrue(LodgingHouse.click(p,b,LodgingSite.DANIEL_DOOR,own(p)),"the door answers a crouched hand");
                has(h,p,LodgingTexts.MAN_IN_FRAME+": "+LodgingTexts.DANIEL_LISTEN[0]);
                h.assertTrue(LodgingBlock.bolted(l.getBlockState(b.offset(LodgingSite.DANIEL_DOOR))),"speaking through the door leaves its bolt shot");
                h.assertTrue(own(p).getBoolean(LodgingDaniel.THROUGH_DOOR),"the reader's record has the door");
                Talk.close(p);p.setShiftKeyDown(false);
                // The food hatch, from the west passage: a hand opens it, the next speaks to him through it, a crouched one shuts it.
                p.teleportTo(l,b.getX()-14.5,b.getY(),b.getZ()-21.5,-90,0);
                h.assertFalse(LodgingHouse.hatchOpen(l,b,LodgingSite.FOOD_HATCH),"the hatch starts shut");
                LodgingHouse.click(p,b,LodgingSite.FOOD_HATCH,own(p));
                h.assertTrue(LodgingHouse.hatchOpen(l,b,LodgingSite.FOOD_HATCH),"a hand opens the hatch");
                LodgingHouse.click(p,b,LodgingSite.FOOD_HATCH,own(p));
                h.assertTrue(LodgingHouse.hatchOpen(l,b,LodgingSite.FOOD_HATCH)&&!Talk.lines(p).isEmpty()&&own(p).getBoolean(LodgingDaniel.THROUGH_HATCH),"the next speaks to him through it: "+Talk.lines(p));
                Talk.close(p);p.setShiftKeyDown(true);
                LodgingHouse.click(p,b,LodgingSite.FOOD_HATCH,own(p));
                h.assertFalse(LodgingHouse.hatchOpen(l,b,LodgingSite.FOOD_HATCH),"a crouched hand shuts it");
                p.setShiftKeyDown(false);
                // Dan speaks in the hall: Daniel strains in the frame and shouts; a reader in the back passage hears it.
                q.teleportTo(l,b.getX()-8.5,b.getY(),b.getZ()-14.5,180,0);
                var dan=who[LodgingPeople.DAN];p.teleportTo(l,dan.getX()+1.2,b.getY(),dan.getZ(),90,0);
                h.assertFalse(LodgingDaniel.straining(b),"he hangs in the frame before Dan has spoken");
                LodgingPeople.talk(p,dan,own(p));
                h.assertTrue(LodgingDaniel.straining(b)&&daniel.gesture()==LodgingPeople.STRAIN,"Dan's voice brings Daniel against the frame: gesture "+daniel.gesture());
                h.assertTrue(own(q).getBoolean(LodgingDaniel.HEARD_HAMMER),"the reader in the back passage hears it");
                at[0]=l.getGameTime();Talk.close(p);
            })
            .thenWaitUntil(()->h.assertTrue(l.getGameTime()>=at[0]+LodgingDaniel.HAMMER+2,"waiting for him to stop"))
            .thenExecute(()->{
                var daniel=who[LodgingPeople.DANIEL];
                h.assertTrue(!LodgingDaniel.straining(b)&&daniel.gesture()==LodgingPeople.HELD,"he hangs in the frame again: "+daniel.gesture());
                // Dan again, straight after: not again for a minute.
                var dan=who[LodgingPeople.DAN];
                LodgingPeople.talk(p,dan,own(p));if(!Talk.lines(p).isEmpty())Talk.close(p);
                h.assertFalse(LodgingDaniel.straining(b),"not again inside the minute");
                // In his room: the bolt drawn, a hand on him speaks; the straps are locked.
                p.setShiftKeyDown(false);p.teleportTo(l,b.getX()-7.5,b.getY(),b.getZ()-14.5,180,0);
                LodgingHouse.click(p,b,LodgingSite.DANIEL_DOOR,own(p));
                h.assertFalse(LodgingBlock.bolted(l.getBlockState(b.offset(LodgingSite.DANIEL_DOOR))),"a standing hand draws the bolt");
                p.teleportTo(l,b.getX()-7.5,b.getY(),b.getZ()-26.5,180,0);
                LodgingPeople.talk(p,daniel,own(p));
                h.assertFalse(Talk.lines(p).isEmpty(),"in his room a hand on him speaks to him");Talk.close(p);
                BlockPos strap=null;
                for(int dx=-1;dx<=1;dx+=2)for(int dy=0;dy<=2;dy++){var c=LodgingSite.DANIEL_FRAME.offset(dx,dy,0);var st=l.getBlockState(b.offset(c));
                    if(LodgingBlock.is(st,LodgingBlock.Kind.FRAME_SIDE)&&(st.getValue(LodgingBlock.STAGE)&4)!=0)strap=c;}
                h.assertTrue(strap!=null,"his frame has its straps");
                LodgingHouse.click(p,b,strap,own(p));
                h.assertTrue(own(p).getBoolean(LodgingDaniel.FELT_STRAPS),"a hand on the straps is told they are locked");
                h.assertTrue(daniel.position().distanceTo(LodgingPeople.world(b,LodgingPeople.SPOTS[LodgingPeople.DANIEL]))<.05,"and he is still held in the frame");
            })
            .thenSucceed();
    }
    @AfterBatch(batch="lodging_daniel") public static void danielDone(ServerLevel l){done();}

    // ------------------------------------------------------------------------------------------------ 0.4.606: N4, the injury
    /** The chapter set to this stage now, with the four placed where it has them (a fixture, to start a scene without the ones before). */
    private static void at(ServerLevel l,BlockPos b,int stage,LiteraryActor[] who){
        var s=l.getServer();var h=LodgingStory.house(s,b);int clock=h.getInt("Clock");
        h.putInt("Stage",stage);h.putInt("StageAt",clock-(stage==LodgingStory.SETTLED?LodgingStory.SETTLE:0));h.putInt("Line",4);
        for(int i=0;i<4;i++){h.remove("Leg"+i);var spot=LodgingStory.spot(stage,i);var w=LodgingPeople.world(b,spot);who[i].moveTo(w.x,w.y,w.z,spot.look(),0);who[i].gesture(spot.gesture());}
        LodgingStory.save(s,b,h);
    }
    @GameTest(template="empty",batch="lodging_injury",timeoutTicks=4000)
    public static void theFailedBodyComesThroughTheGlassBreaksAndBothMenAreCut(GameTestHelper h){
        var f=house(h);var p=f.player("lodging_injury_a",P);var q=f.player("lodging_injury_b",P);var l=f.level(P);var b=f.base(P);var s=l.getServer();
        f.chunks.hold(l,LodgingPeople.area(b));LodgingStory.hold(b,true);final LiteraryActor[] who=new LiteraryActor[LodgingPeople.COUNT];
        h.startSequence()
            .thenWaitUntil(()->{h.assertTrue(f.chunks.ready(),"the house is resident");LodgingPeople.ensure(l,b);
                for(int i=0;i<LodgingPeople.COUNT;i++){who[i]=LodgingPeople.find(l,b,i);h.assertTrue(who[i]!=null,"waiting for number "+i);}})
            .thenExecute(()->{
                // The three settled in the workroom, their minute up; one reader in the hall, the other in Daniel's room.
                at(l,b,LodgingStory.SETTLED,who);
                f.at(p,P,.5,0,-4.5);f.at(q,P,-7.5,0,-23.5);
                LodgingStory.hold(b,false);
            })
            .thenWaitUntil(()->h.assertTrue(LodgingStory.stage(s,b)==LodgingStory.INJURY,"waiting for Vera out and Dan and Rae down: stage "+LodgingStory.stage(s,b)+", Dan at "+rel(b,who[LodgingPeople.DAN])+", Vera at "+rel(b,who[LodgingPeople.VERA])))
            .thenExecute(()->{
                h.assertTrue(LodgingSite.onGallery(rel(b,who[LodgingPeople.VERA])),"Vera is in the corridor between the rooms: "+rel(b,who[LodgingPeople.VERA]));
                h.assertTrue(LodgingSite.inHall(rel(b,who[LodgingPeople.DAN]))&&LodgingSite.inHall(rel(b,who[LodgingPeople.RAE])),"Dan and Rae are down in the hall");
                h.assertFalse(LodgingSite.open(l,b,LodgingSite.WORK_DOOR),"the workroom door is shut behind them");
                h.assertFalse(LodgingSite.open(l,b,LodgingSite.JIB),"the jib door is shut");
            })
            .thenWaitUntil(()->h.assertTrue(LodgingInjury.phase(s,b)==LodgingInjury.BATTER,"waiting for it to batter the latch: "+LodgingInjury.state(s,b)))
            .thenExecute(()->{
                h.assertTrue(who[LodgingPeople.FAILED_SHIRT].gesture()==LodgingPeople.BATTER,"it batters with both fists");
                h.assertTrue(own(p).getBoolean(LodgingInjury.HEARD_JIB),"the reader in the hall heard it pull at the door");
                h.assertFalse(LodgingSite.open(l,b,LodgingSite.JIB),"the door holds while it batters");
            })
            .thenWaitUntil(()->h.assertTrue(LodgingInjury.phase(s,b)==LodgingInjury.LOOSE,"waiting for the latch to go"))
            .thenExecute(()->h.assertTrue(LodgingSite.open(l,b,LodgingSite.JIB),"the jib door is open"))
            .thenWaitUntil(()->h.assertTrue(LodgingInjury.cut(s,b),"waiting for it to reach Dan: "+LodgingInjury.state(s,b)+", it at "+rel(b,who[LodgingPeople.FAILED_SHIRT])+", Dan at "+rel(b,who[LodgingPeople.DAN])))
            .thenExecute(()->{
                var stand=l.getBlockState(b.offset(LodgingSite.HALL_STAND).above());
                h.assertTrue(LodgingBlock.is(stand,LodgingBlock.Kind.HALL_STAND)&&stand.getValue(LodgingBlock.STAGE)==2,"the hall stand's glass is broken");
                var dan=who[LodgingPeople.DAN];var daniel=who[LodgingPeople.DANIEL];
                h.assertTrue((dan.marks()&LodgingInjury.CUT)!=0&&dan.gesture()==LodgingPeople.HURT,"Dan's hand is cut and he holds it");
                h.assertTrue((daniel.marks()&LodgingInjury.CUT)!=0,"Daniel's hand is cut in the same place");
                h.assertTrue("lodging_dan_cut".equals(LodgingPeople.dressed(dan.phase(),dan.marks()))&&"lodging_daniel_cut".equals(LodgingPeople.dressed(daniel.phase(),daniel.marks())),"both are drawn cut");
                h.assertTrue(own(p).getBoolean(LodgingInjury.SAW_DAN_CUT)&&!own(p).getBoolean(LodgingInjury.SAW_DANIEL_CUT),"the reader in the hall saw Dan's, not Daniel's");
                h.assertTrue(own(q).getBoolean(LodgingInjury.SAW_DANIEL_CUT)&&!own(q).getBoolean(LodgingInjury.SAW_DAN_CUT),"the reader with Daniel saw his, not Dan's");
                has(h,q,LodgingTexts.MAN_IN_FRAME+": "+LodgingTexts.DANIEL_CUT);
                h.assertTrue(LodgingInjury.held(s,b)==null&&!LodgingInjury.kept(s,b),"nobody held, not kept off");
            })
            .thenWaitUntil(()->h.assertTrue(LodgingStory.stage(s,b)==LodgingStory.AFTER,"waiting for it to go back: "+LodgingInjury.state(s,b)+", it at "+rel(b,who[LodgingPeople.FAILED_SHIRT])))
            .thenExecute(()->{
                h.assertTrue(LodgingInjury.state(s,b).getBoolean("DanSaid"),"Dan has said it's nothing");
                h.assertFalse(LodgingSite.open(l,b,LodgingSite.JIB),"it pulled the jib door to behind it");
                h.assertTrue(rel(b,who[LodgingPeople.FAILED_SHIRT]).distanceTo(new net.minecraft.world.phys.Vec3(LodgingPeople.SPOTS[LodgingPeople.FAILED_SHIRT].x(),0,LodgingPeople.SPOTS[LodgingPeople.FAILED_SHIRT].z()))<.3,"it is back at the door in the passage");
                h.assertTrue(rel(b,who[LodgingPeople.DAN]).distanceTo(new net.minecraft.world.phys.Vec3(LodgingStory.DAN_HALL.x(),0,LodgingStory.DAN_HALL.z()))<.2,"Dan is back in front of the bench");
                // Cut, the rule has been shown: the pin never comes.
                var in=LodgingInjury.state(s,b);in.putInt("At",in.getInt("At")-LodgingInjury.PIN_WAIT-10);
            })
            .thenIdle(10)
            .thenExecute(()->h.assertTrue(LodgingStory.stage(s,b)==LodgingStory.AFTER,"with the cut shown there is no pin"))
            .thenSucceed();
    }
    @AfterBatch(batch="lodging_injury") public static void injuryDone(ServerLevel l){done();}

    @GameTest(template="empty",batch="lodging_kept",timeoutTicks=4000)
    public static void aFailedBodyTakesAReaderIsDrivenOffAndThePinShowsTheRule(GameTestHelper h){
        var f=house(h);var p=f.player("lodging_kept_a",P);var q=f.player("lodging_kept_b",P);var l=f.level(P);var b=f.base(P);var s=l.getServer();
        f.chunks.hold(l,LodgingPeople.area(b));LodgingStory.hold(b,true);final LiteraryActor[] who=new LiteraryActor[LodgingPeople.COUNT];
        final net.minecraft.world.phys.Vec3[] took={null};
        h.startSequence()
            .thenWaitUntil(()->{h.assertTrue(f.chunks.ready(),"the house is resident");LodgingPeople.ensure(l,b);
                for(int i=0;i<LodgingPeople.COUNT;i++){who[i]=LodgingPeople.find(l,b,i);h.assertTrue(who[i]!=null,"waiting for number "+i);}})
            .thenExecute(()->{
                // Straight to N4, Vera between rooms, Dan and Rae in the hall; one reader standing just inside the back passage from the jib.
                at(l,b,LodgingStory.INJURY,who);var house=LodgingStory.house(s,b);LodgingInjury.begin(house,house.getInt("Clock"));LodgingStory.save(s,b,house);
                f.at(p,P,-11.0,0,-13.5);f.at(q,P,-9.5,0,-14.5);
                LodgingStory.hold(b,false);
            })
            .thenWaitUntil(()->h.assertTrue(LodgingInjury.held(s,b)!=null,"waiting for it to take hold of the reader by the door: "+LodgingInjury.state(s,b)+", it at "+rel(b,who[LodgingPeople.FAILED_SHIRT])))
            .thenExecute(()->{
                h.assertTrue(p.getUUID().equals(LodgingInjury.held(s,b))&&own(p).getBoolean(LodgingInjury.HELD),"it has hold of the nearer reader");
                took[0]=p.position();
            })
            .thenIdle(20)
            .thenExecute(()->{
                var body=who[LodgingPeople.FAILED_SHIRT];
                h.assertTrue(body.gesture()==LodgingPeople.DRAG,"it walks them back by the arm: gesture "+body.gesture());
                h.assertTrue(p.getX()<took[0].x,"the reader is walked back toward the door: "+took[0]+" to "+p.position());
                // The other reader strikes it: one blow does not break its grip, the second does.
                body.hurt(l.damageSources().playerAttack(q),1);
                h.assertTrue(LodgingInjury.held(s,b)!=null,"one blow does not break its grip");
                body.hurt(l.damageSources().playerAttack(q),1);
                h.assertTrue(LodgingInjury.held(s,b)==null,"the second does");
                h.assertTrue(own(q).getInt(LodgingInjury.STRUCK)==2,"the blows are in the striker's record");
                // Three more: it is driven back through the door, and nothing has been cut.
                for(int i=0;i<3;i++)body.hurt(l.damageSources().playerAttack(q),1);
                h.assertTrue(LodgingInjury.kept(s,b)&&!LodgingInjury.cut(s,b)&&LodgingInjury.phase(s,b)==LodgingInjury.BACK,"five blows drive it off and nothing is cut");
                // The one it held, let go in the doorway, stays there a moment: the door is not pulled to on them.
            })
            .thenWaitUntil(()->h.assertTrue(LodgingInjury.phase(s,b)==LodgingInjury.OVER,"waiting for it to be back in the passage: "+LodgingInjury.state(s,b)+", it at "+rel(b,who[LodgingPeople.FAILED_SHIRT])))
            .thenIdle(5)
            .thenExecute(()->{
                h.assertTrue(p.position().distanceTo(net.minecraft.world.phys.Vec3.atCenterOf(b.offset(LodgingSite.JIB)))>1.2||LodgingSite.open(l,b,LodgingSite.JIB),"the door is not pulled to on somebody standing in it");
                f.at(p,P,-8.5,0,-14.5);
            })
            .thenWaitUntil(()->h.assertTrue(LodgingStory.stage(s,b)==LodgingStory.AFTER,"waiting for it to be back through the door: "+LodgingInjury.state(s,b)))
            .thenExecute(()->{
                h.assertFalse(LodgingSite.open(l,b,LodgingSite.JIB),"it pulled the door to");
                h.assertTrue(own(q).getBoolean(LodgingInjury.KEPT_OFF)&&!own(p).getBoolean(LodgingInjury.TAKEN),"the striker kept it off; the one held was not taken");
                h.assertTrue((who[LodgingPeople.DAN].marks()|who[LodgingPeople.DANIEL].marks())==0,"neither man is marked");
                h.assertTrue(l.getBlockState(b.offset(LodgingSite.HALL_STAND).above()).getValue(LodgingBlock.STAGE)==1,"the glass is whole");
                // A minute on: Vera takes Dan down to the frame. The second reader waits in Daniel's room.
                var in=LodgingInjury.state(s,b);in.putInt("At",in.getInt("At")-LodgingInjury.PIN_WAIT);
                f.at(q,P,-7.5,0,-23.5);f.at(p,P,.5,0,-4.5);
            })
            .thenWaitUntil(()->h.assertTrue(LodgingStory.stage(s,b)==LodgingStory.PINNED,"waiting for the pin: stage "+LodgingStory.stage(s,b)+", Vera at "+rel(b,who[LodgingPeople.VERA])+" leg "+LodgingStory.house(s,b).getInt("Leg"+LodgingPeople.VERA)+", Dan at "+rel(b,who[LodgingPeople.DAN])))
            .thenExecute(()->{
                h.assertTrue(LodgingSite.open(l,b,LodgingSite.DANIEL_DOOR)&&!LodgingBlock.bolted(l.getBlockState(b.offset(LodgingSite.DANIEL_DOOR))),"Vera drew Daniel's bolt and left his door open");
                h.assertTrue(LodgingSite.inDaniels(rel(b,who[LodgingPeople.VERA]))&&LodgingSite.inDaniels(rel(b,who[LodgingPeople.DAN])),"Vera and Dan are at his frame");
                h.assertTrue((who[LodgingPeople.DAN].marks()&LodgingInjury.PRICK)!=0&&(who[LodgingPeople.DANIEL].marks()&LodgingInjury.PRICK)!=0,"the pin in Dan's hand shows in Daniel's");
                h.assertTrue("lodging_daniel_prick".equals(LodgingPeople.dressed(who[LodgingPeople.DANIEL].phase(),who[LodgingPeople.DANIEL].marks())),"Daniel is drawn pricked");
                h.assertTrue(own(q).getBoolean(LodgingInjury.SAW_PIN)&&!own(p).getBoolean(LodgingInjury.SAW_PIN),"the reader in the room saw it; the one in the hall did not");
                has(h,q,"Vera: "+LodgingTexts.VERA_PIN);
            })
            .thenSucceed();
    }
    @AfterBatch(batch="lodging_kept") public static void keptDone(ServerLevel l){done();}

    // ------------------------------------------------------------------------------------------------ 0.4.607: N5 and N6, the two accounts
    @GameTest(template="empty",batch="lodging_accounts",timeoutTicks=2000)
    public static void theTwoMenGiveTheirAccountsAndDanielCanBeArmed(GameTestHelper h){
        var f=house(h);var p=f.player("lodging_accounts_a",P);var q=f.player("lodging_accounts_b",P);var l=f.level(P);var b=f.base(P);var s=l.getServer();
        f.chunks.hold(l,LodgingPeople.area(b));LodgingStory.hold(b,true);final LiteraryActor[] who=new LiteraryActor[LodgingPeople.COUNT];
        h.startSequence()
            .thenWaitUntil(()->{h.assertTrue(f.chunks.ready(),"the house is resident");LodgingPeople.ensure(l,b);
                for(int i=0;i<LodgingPeople.COUNT;i++){who[i]=LodgingPeople.find(l,b,i);h.assertTrue(who[i]!=null,"waiting for number "+i);}})
            .thenExecute(()->{
                var daniel=who[LodgingPeople.DANIEL];var dan=who[LodgingPeople.DAN];
                // Before Act II, Dan has no account, and Daniel only the first exchange.
                f.at(p,P,-7.5,0,-24.5);f.at(q,P,dan.getX()-b.getX()+1.2,0,dan.getZ()-b.getZ());
                LodgingPeople.talk(p,daniel,own(p));Talk.close(p);
                LodgingPeople.talk(p,daniel,own(p));
                h.assertFalse(own(p).getBoolean(LodgingPeople.DANIEL_TOLD),"no account before the glass");
                Talk.close(p);
                // Act II: the glass is over, nothing cut.
                at(l,b,LodgingStory.AFTER,who);
                LodgingPeople.talk(p,daniel,own(p));
                h.assertTrue(own(p).getBoolean(LodgingPeople.DANIEL_TOLD),"a reader he has spoken to has his account in Act II");
                has(h,p,LodgingPeople.name(LodgingPeople.DANIEL,own(p))+": "+LodgingTexts.DANIEL_ACCOUNT[3]);
                h.assertFalse(Talk.choose(p,LodgingTexts.R_DIDNT_KNOW),"\"He didn't know\" is not offered to a reader who has not heard Dan's side");
                h.assertFalse(Talk.choose(p,LodgingTexts.R_GIVE),"nothing to give with an empty hand");
                Talk.close(p);
                // Dan's account; told Daniel is still here, he says Vera told him he had gone home.
                LodgingPeople.talk(q,dan,own(q));
                has(h,q,"Dan: "+LodgingTexts.DAN_DOG[0]);
                h.assertFalse(Talk.choose(q,LodgingTexts.R_STILL),"a reader who does not know Daniel cannot tell Dan about him");
                Talk.close(q);
                var oq=own(q);oq.putBoolean(LodgingInjury.SAW_DANIEL_CUT,true);LiteraryVignettes.save(LabyrinthData.get(s),q.getUUID(),P,oq);
                LodgingPeople.talk(q,dan,own(q));
                h.assertTrue(Talk.choose(q,LodgingTexts.R_STILL),"a reader who saw Daniel's hand can tell Dan he is still here");
                has(h,q,"Dan: "+LodgingTexts.DAN_NO[0]);has(h,q,"Dan: "+LodgingTexts.DAN_NO[1]);
                h.assertTrue(own(q).getBoolean(LodgingPeople.HEARD_DAN_SIDE)&&LodgingStory.house(s,b).getBoolean(LodgingPeople.DAN_KNOWS),"the reader has heard Dan's side; the house has Dan knowing");
                Talk.close(q);
                // q goes to Daniel with an axe: he has met him now, has his account, may say Dan didn't know, and gives it.
                f.at(q,P,-6.5,0,-24.5);q.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_AXE));
                LodgingPeople.talk(q,daniel,own(q));Talk.close(q);LodgingPeople.talk(q,daniel,own(q));
                h.assertTrue(own(q).getBoolean(LodgingPeople.DANIEL_TOLD),"the second reader has his account too");
                h.assertTrue(Talk.choose(q,LodgingTexts.R_DIDNT_KNOW),"a reader who heard Dan's side can say he didn't know");
                h.assertTrue(Talk.choose(q,LodgingTexts.R_GIVE),"the axe is offered");
                h.assertTrue(LodgingPeople.armed(daniel)&&daniel.getMainHandItem().is(net.minecraft.world.item.Items.IRON_AXE)&&q.getMainHandItem().isEmpty(),"Daniel holds the reader's axe; the reader no longer does");
                h.assertTrue(LodgingStory.house(s,b).getBoolean(LodgingPeople.ARMED)&&own(q).getBoolean(LodgingPeople.GAVE_WEAPON),"the house and the reader have it");
                h.assertTrue(own(q).getBoolean(LodgingInjury.SAW_DANIEL_CUT)?Talk.lines(q).stream().noneMatch(x->x.endsWith(": "+LodgingTexts.DANIEL_GOOD)):true,"a reader who saw the rule is not told \"Good.\"");
                h.assertTrue(Talk.choose(q,LodgingTexts.R_HURT_SELF),"a reader who saw the cut can warn him");
                has(h,q,LodgingPeople.name(LodgingPeople.DANIEL,own(q))+": "+LodgingTexts.DANIEL_MAKE);
                h.assertFalse(Talk.choose(q,LodgingTexts.R_GIVE),"he is not given a second");
                Talk.close(q);
            })
            .thenSucceed();
    }
    @AfterBatch(batch="lodging_accounts") public static void accountsDone(ServerLevel l){done();}

    // ------------------------------------------------------------------------------------------------ 0.4.608: N7, the key
    private static void keyAt(ServerLevel l,BlockPos b,String where,long seenAt){
        var s=l.getServer();var h=LodgingStory.house(s,b);h.putString(LodgingKey.WHERE,where);if(seenAt>=0)h.putLong(LodgingKey.SEEN_AT,seenAt);LodgingStory.save(s,b,h);
    }
    private static BlockPos strap(GameTestHelper h,ServerLevel l,BlockPos b){
        BlockPos strap=null;
        for(int dx=-1;dx<=1;dx+=2){var c=LodgingSite.DANIEL_FRAME.offset(dx,0,0);var st=l.getBlockState(b.offset(c));
            if(LodgingBlock.is(st,LodgingBlock.Kind.FRAME_SIDE)&&(st.getValue(LodgingBlock.STAGE)&4)!=0)strap=c;}
        h.assertTrue(strap!=null,"his frame has its straps");return strap;
    }
    private static int frameStage(ServerLevel l,BlockPos b){return l.getBlockState(b.offset(LodgingSite.DANIEL_FRAME)).getValue(LodgingBlock.STAGE);}
    @GameTest(template="empty",batch="lodging_key",timeoutTicks=4000)
    public static void raeTakesTheKeyIsSeenWithItAndAReaderLetsDanielOut(GameTestHelper h){
        var f=house(h);var p=f.player("lodging_key_a",P);var q=f.player("lodging_key_b",P);var l=f.level(P);var b=f.base(P);var s=l.getServer();
        f.chunks.hold(l,LodgingPeople.area(b));final LiteraryActor[] who=new LiteraryActor[LodgingPeople.COUNT];
        h.startSequence()
            .thenWaitUntil(()->{h.assertTrue(f.chunks.ready(),"the house is resident");LodgingPeople.ensure(l,b);
                for(int i=0;i<LodgingPeople.COUNT;i++){who[i]=LodgingPeople.find(l,b,i);h.assertTrue(who[i]!=null,"waiting for number "+i);}})
            .thenExecute(()->{
                h.assertTrue(LodgingKey.where(s,b).equals(LodgingKey.HOOK),"the key starts on its hook");
                f.at(p,P,1.5,0,-6.5);f.at(q,P,2.5,0,-6.5);
                at(l,b,LodgingStory.FETCHING,who);
            })
            // Rae, out of sight in the workroom, takes it.
            .thenWaitUntil(()->h.assertTrue(LodgingStory.stage(s,b)>=LodgingStory.COMING_DOWN,"waiting for Rae to come down: stage "+LodgingStory.stage(s,b)))
            .thenExecute(()->{
                h.assertTrue(LodgingKey.where(s,b).equals(LodgingKey.RAE),"Rae has the key once she has been to the workroom: "+LodgingKey.where(s,b));
                var hook=l.getBlockState(b.offset(LodgingSite.KEY_HOOK));
                h.assertTrue(LodgingThing.is(hook,LodgingThing.Kind.KEY_HOOK)&&hook.getValue(LodgingThing.STAGE)==1,"its hook is empty");
                LodgingStory.hold(b,true);at(l,b,LodgingStory.AFTER,who);
                // Both readers in the hall behind her, turned away from her up the stair.
                look(p,p.getEyePosition().add(0,0,-6));look(q,q.getEyePosition().add(0,0,-6));
            })
            // Unwatched in Act II, in her place, she takes it out and looks at it.
            .thenWaitUntil(()->h.assertTrue(LodgingKey.is(who[LodgingPeople.RAE].getMainHandItem(),b),"waiting for Rae to look at the key in her hand"))
            .thenExecute(()->{h.assertTrue(who[LodgingPeople.RAE].gesture()==LodgingPeople.KEY,"she holds it low to look at it");look(p,who[LodgingPeople.RAE].position().add(0,1.2,0));})
            .thenWaitUntil(()->h.assertTrue(own(p).getBoolean(LodgingKey.SAW_KEY),"the reader who looks sees it"))
            .thenExecute(()->{
                var rae=who[LodgingPeople.RAE];var dan=who[LodgingPeople.DAN];
                h.assertTrue(rae.getMainHandItem().isEmpty()&&LodgingStory.house(s,b).contains(LodgingKey.SEEN_AT),"seen, she puts it away");
                h.assertFalse(own(q).getBoolean(LodgingKey.SAW_KEY),"the reader looking away did not see it");
                // Asked about it, she confesses, and gives it to the reader who asks for it.
                LodgingPeople.talk(p,rae,own(p));
                h.assertTrue(Talk.choose(p,LodgingTexts.R_KEY),"\"You have the key.\" is offered to the reader who saw it");
                for(var line:LodgingTexts.RAE_CONFESSION)has(h,p,"Rae: "+line);
                h.assertTrue(Talk.choose(p,LodgingTexts.R_GIVE_ME),"the reader can ask for it");
                h.assertTrue(p.getInventory().items.stream().anyMatch(i->LodgingKey.is(i,b))&&p.getUUID().equals(LodgingKey.holder(s,b)),"the key is in the reader's hands");
                Talk.close(p);
                // Told, Dan says it to her, and she answers him.
                LodgingPeople.talk(p,dan,own(p));
                h.assertTrue(Talk.choose(p,LodgingTexts.R_RAE_KNEW),"a reader who heard her can tell Dan");
                has(h,p,"Dan: "+LodgingTexts.DAN_SHE_KNEW);has(h,p,"Dan: "+LodgingTexts.DAN_YOU_KNEW);has(h,p,"Rae: "+LodgingTexts.RAE_SO_YOU_CAME);
                Talk.close(p);
                // In his room with it in hand: the straps open.
                f.at(p,P,-7.5,0,-24.5);
                for(int i=0;i<9;i++)if(LodgingKey.is(p.getInventory().items.get(i),b))p.getInventory().selected=i;
                h.assertTrue(LodgingKey.is(p.getMainHandItem(),b),"the reader holds the key");
                h.assertTrue(LodgingHouse.click(p,b,strap(h,l,b),own(p)),"the straps answer");
                h.assertTrue((frameStage(l,b)&4)!=0,"the frame shows them hanging open");
                h.assertTrue(LodgingKey.free(s,b)&&LodgingKey.where(s,b).equals(LodgingKey.USED),"he is free; the key is spent");
                h.assertTrue(p.getInventory().items.stream().noneMatch(i->LodgingKey.is(i,b))&&!LodgingKey.is(p.getMainHandItem(),b),"and gone from the reader");
            })
            // He walks out through his door and the back passage to the parlor; Rae stands in its door.
            .thenWaitUntil(()->{var d=who[LodgingPeople.DANIEL];h.assertTrue(LodgingStory.house(s,b).getBoolean(LodgingKey.IN),"waiting for Daniel to reach the parlor: at "+rel(b,d)+", leg "+LodgingStory.house(s,b).getInt("KeyLeg"+LodgingPeople.DANIEL));})
            .thenExecute(()->h.assertTrue(LodgingSite.inParlor(rel(b,who[LodgingPeople.DANIEL])),"Daniel is in the parlor: "+rel(b,who[LodgingPeople.DANIEL])))
            .thenWaitUntil(()->h.assertTrue(who[LodgingPeople.RAE].position().distanceTo(LodgingPeople.world(b,LodgingKey.RAE_DOORWAY))<.3,"waiting for Rae to stand in the parlor's door: at "+rel(b,who[LodgingPeople.RAE])))
            .thenSucceed();
    }
    @AfterBatch(batch="lodging_key") public static void keyDone(ServerLevel l){done();}
    @GameTest(template="empty",batch="lodging_key_hidden",timeoutTicks=3000)
    public static void seenAndNotAskedRaeHidesTheKeyInTheCupboard(GameTestHelper h){
        var f=house(h);var p=f.player("lodging_hide_a",P);var l=f.level(P);var b=f.base(P);var s=l.getServer();
        f.chunks.hold(l,LodgingPeople.area(b));LodgingStory.hold(b,true);final LiteraryActor[] who=new LiteraryActor[LodgingPeople.COUNT];
        h.startSequence()
            .thenWaitUntil(()->{h.assertTrue(f.chunks.ready(),"the house is resident");LodgingPeople.ensure(l,b);
                for(int i=0;i<LodgingPeople.COUNT;i++){who[i]=LodgingPeople.find(l,b,i);h.assertTrue(who[i]!=null,"waiting for number "+i);}})
            .thenExecute(()->{f.at(p,P,-1.5,0,-3.5);at(l,b,LodgingStory.AFTER,who);keyAt(l,b,LodgingKey.RAE,l.getGameTime()-LodgingKey.HIDE_AFTER);})
            .thenWaitUntil(()->h.assertTrue(LodgingKey.where(s,b).equals(LodgingKey.CUPBOARD),"waiting for Rae to put it in the cupboard: at "+rel(b,who[LodgingPeople.RAE])+", task "+LodgingStory.house(s,b).getInt(LodgingKey.TASK)))
            .thenExecute(()->h.assertFalse(LodgingSite.open(l,b,LodgingSite.CUPBOARD),"she shuts the cupboard"))
            .thenWaitUntil(()->h.assertTrue(LodgingStory.house(s,b).getInt(LodgingKey.TASK)==LodgingKey.NONE,"waiting for her to go back to Dan: at "+rel(b,who[LodgingPeople.RAE])))
            .thenExecute(()->{
                h.assertTrue(who[LodgingPeople.RAE].position().distanceTo(LodgingPeople.world(b,LodgingPeople.SPOTS[LodgingPeople.RAE]))<.3,"back in her place");
                f.at(p,P,.5,0,-9.6);LodgingHouse.click(p,b,LodgingSite.CUPBOARD,own(p));
                h.assertTrue(own(p).getBoolean(LodgingKey.FOUND_KEY)&&p.getUUID().equals(LodgingKey.holder(s,b))&&p.getInventory().items.stream().anyMatch(i->LodgingKey.is(i,b)),"the reader who opens it finds the key");
            })
            .thenSucceed();
    }
    @AfterBatch(batch="lodging_key_hidden") public static void hiddenDone(ServerLevel l){done();}
    @GameTest(template="empty",batch="lodging_key_rae",timeoutTicks=3000)
    public static void raeGivesDanielTheKeyHerself(GameTestHelper h){
        var f=house(h);var p=f.player("lodging_rae_a",P);var l=f.level(P);var b=f.base(P);var s=l.getServer();
        f.chunks.hold(l,LodgingPeople.area(b));LodgingStory.hold(b,true);final LiteraryActor[] who=new LiteraryActor[LodgingPeople.COUNT];
        h.startSequence()
            .thenWaitUntil(()->{h.assertTrue(f.chunks.ready(),"the house is resident");LodgingPeople.ensure(l,b);
                for(int i=0;i<LodgingPeople.COUNT;i++){who[i]=LodgingPeople.find(l,b,i);h.assertTrue(who[i]!=null,"waiting for number "+i);}})
            .thenExecute(()->{
                f.at(p,P,1.5,0,-5.5);at(l,b,LodgingStory.AFTER,who);keyAt(l,b,LodgingKey.RAE,-1);
                var o=own(p);o.putBoolean(LodgingPeople.RAE_ASKED,true);LiteraryVignettes.save(LabyrinthData.get(s),p.getUUID(),P,o);
                LodgingPeople.talk(p,who[LodgingPeople.RAE],own(p));
                h.assertFalse(Talk.choose(p,LodgingTexts.R_KEY),"a reader who did not see the key cannot say so");
                h.assertTrue(Talk.choose(p,LodgingTexts.R_WAY_UP),"a reader who heard her ask in the hall can say she knew the way up");
                has(h,p,"Rae: "+LodgingTexts.RAE_CONFESSION[2]);
                h.assertTrue(Talk.choose(p,LodgingTexts.R_YOURSELF),"and ask her to give it to him herself");
                Talk.close(p);
                // The reader follows her into his room, near enough to hear.
                f.at(p,P,-8.5,0,-21.5);
            })
            .thenWaitUntil(()->h.assertTrue(LodgingKey.free(s,b),"waiting for Rae to unlock him: at "+rel(b,who[LodgingPeople.RAE])+", task "+LodgingStory.house(s,b).getInt(LodgingKey.TASK)))
            .thenExecute(()->{
                var hh=LodgingStory.house(s,b);
                h.assertTrue(hh.getBoolean(LodgingKey.BY_RAE)&&hh.getBoolean(LodgingKey.SAID_DONT),"she unlocked him, after he told her not to be sorry");
                h.assertTrue((frameStage(l,b)&4)!=0,"the straps hang open");
            })
            .thenWaitUntil(()->h.assertTrue(LodgingStory.house(s,b).getBoolean(LodgingKey.IN),"waiting for Daniel to reach the parlor: at "+rel(b,who[LodgingPeople.DANIEL])))
            .thenExecute(()->{
                h.assertTrue(who[LodgingPeople.RAE].position().distanceTo(LodgingPeople.world(b,LodgingKey.RAE_AT_FRAME))<.3,"she stays by the frame; nobody stands in the parlor's door");
                h.assertFalse(LodgingStory.house(s,b).getInt(LodgingKey.TASK)==LodgingKey.DOORWAY,"no doorway");
            })
            .thenSucceed();
    }
    @AfterBatch(batch="lodging_key_rae") public static void raeKeyDone(ServerLevel l){done();}

    // ------------------------------------------------------------------------------------------------ 0.4.609: N8, Vera's offer
    @GameTest(template="empty",batch="lodging_vera",timeoutTicks=3000)
    public static void veraOffersToMendDanAndTheCostIsKnownBeforeAnybodyHelps(GameTestHelper h){
        var f=house(h);var p=f.player("lodging_vera_a",P);var q=f.player("lodging_vera_b",P);var l=f.level(P);var b=f.base(P);var s=l.getServer();
        f.chunks.hold(l,LodgingPeople.area(b));LodgingStory.hold(b,true);final LiteraryActor[] who=new LiteraryActor[LodgingPeople.COUNT];
        h.startSequence()
            .thenWaitUntil(()->{h.assertTrue(f.chunks.ready(),"the house is resident");LodgingPeople.ensure(l,b);
                for(int i=0;i<LodgingPeople.COUNT;i++){who[i]=LodgingPeople.find(l,b,i);h.assertTrue(who[i]!=null,"waiting for number "+i);}})
            .thenExecute(()->{
                at(l,b,LodgingStory.AFTER,who);var vera=who[LodgingPeople.VERA];
                var v=rel(b,vera);f.at(p,P,v.x+1.2,v.y,v.z);f.at(q,P,v.x-1.2,v.y,v.z);
                // Her offer; nobody can back her yet.
                LodgingPeople.talk(p,vera,own(p));
                for(var line:LodgingTexts.VERA_OFFER)has(h,p,"Vera: "+line);
                h.assertFalse(Talk.replies(p).contains(LodgingTexts.R_HELP_VERA),"nobody helps before the cost is known: "+Talk.replies(p));
                h.assertFalse(Talk.replies(p).contains(LodgingTexts.R_IT_CUT_HIM),"a reader who has not seen the rule cannot put it to her");
                h.assertTrue(Talk.choose(p,LodgingTexts.R_OTHER_ONE),"anybody can ask after the other one");
                for(var line:LodgingTexts.VERA_TRUTH)has(h,p,"Vera: "+line);
                h.assertTrue(Talk.choose(p,LodgingTexts.R_HELP_VERA),"told the cost, the reader can back her");
                has(h,p,"Vera: "+LodgingTexts.VERA_GOOD);
                h.assertTrue(own(p).getInt(LodgingVera.ANSWER)==LodgingVera.HELP,"the reader's answer is kept");
                Talk.close(p);
                // A second reader who saw the pin challenges her with it, and refuses.
                var o=own(q);o.putBoolean(LodgingInjury.SAW_PIN,true);LiteraryVignettes.save(LabyrinthData.get(s),q.getUUID(),P,o);
                LodgingPeople.talk(q,vera,own(q));
                h.assertTrue(Talk.choose(q,LodgingTexts.R_IT_CUT_HIM),"a reader who saw the pin can put it to her");
                has(h,q,"Vera: "+LodgingTexts.VERA_TRUTH[0]);
                h.assertTrue(Talk.choose(q,LodgingTexts.R_REFUSE_VERA),"and refuse");
                h.assertTrue(own(q).getInt(LodgingVera.ANSWER)==LodgingVera.REFUSE&&own(p).getInt(LodgingVera.ANSWER)==LodgingVera.HELP,"each reader's answer is their own");
                Talk.close(q);
                // Rae, asked by the second reader, takes the key up to Vera, who keeps it in her hand.
                keyAt(l,b,LodgingKey.RAE,-1);var oq=own(q);oq.putBoolean(LodgingPeople.RAE_ASKED,true);LiteraryVignettes.save(LabyrinthData.get(s),q.getUUID(),P,oq);
                var r=rel(b,who[LodgingPeople.RAE]);f.at(q,P,r.x+1.2,r.y,r.z);
                LodgingPeople.talk(q,who[LodgingPeople.RAE],own(q));
                h.assertTrue(Talk.choose(q,LodgingTexts.R_WAY_UP),"asked how she knew the way");
                h.assertTrue(Talk.choose(q,LodgingTexts.R_TO_VERA),"told to keep it and take it to Vera");
                Talk.close(q);
            })
            .thenWaitUntil(()->h.assertTrue(LodgingKey.VERA.equals(LodgingKey.where(s,b)),"waiting for Rae to reach Vera: at "+rel(b,who[LodgingPeople.RAE])))
            .thenExecute(()->h.assertTrue(LodgingKey.is(who[LodgingPeople.VERA].getMainHandItem(),b),"Vera holds the key"))
            .thenSucceed();
    }
    @AfterBatch(batch="lodging_vera") public static void veraDone(ServerLevel l){done();}

    // ------------------------------------------------------------------------------------------------ 0.4.610: N9, the cellar
    @GameTest(template="empty",batch="lodging_cellar",timeoutTicks=3000)
    public static void theDeadPostsWedgeComesOutAndTheBodyComesApart(GameTestHelper h){
        var f=house(h);var p=f.player("lodging_cellar_a",P);var q=f.player("lodging_cellar_b",P);var l=f.level(P);var b=f.base(P);var s=l.getServer();
        f.chunks.hold(l,LodgingPeople.area(b));LodgingStory.hold(b,true);final LiteraryActor[] who=new LiteraryActor[LodgingPeople.COUNT];
        var post=LodgingSite.DEAD_POST;var shoe=b.offset(post);
        h.startSequence()
            .thenWaitUntil(()->{h.assertTrue(f.chunks.ready(),"the house is resident");LodgingPeople.ensure(l,b);
                for(int i=0;i<LodgingPeople.COUNT;i++){who[i]=LodgingPeople.find(l,b,i);h.assertTrue(who[i]!=null,"waiting for number "+i);}})
            .thenExecute(()->{
                at(l,b,LodgingStory.AFTER,who);
                // One reader in the cellar facing the post, the other upstairs in the hall.
                f.at(p,P,post.getX()+3.5,LodgingSite.CELLAR,post.getZ()+.5);p.setYRot(90);p.setXRot(10);
                f.at(q,P,1.5,LodgingSite.GROUND,-3.5);
                h.assertTrue(LodgingPeople.onFloor(who[LodgingPeople.FAILED_POST]),"the body sits tied at the post's foot");
                // A hand on it says so; a crouched hand knocks the wedge out.
                LodgingHouse.click(p,b,post,own(p));
                h.assertTrue(l.getBlockState(shoe).getValue(LodgingBlock.STAGE)==1,"a plain hand leaves the wedge in");
                p.setShiftKeyDown(true);LodgingHouse.click(p,b,post.above(),own(p));p.setShiftKeyDown(false);
                h.assertTrue(l.getBlockState(shoe).getValue(LodgingBlock.STAGE)==2,"a crouched hand on the post kicks the wedge out");
                h.assertTrue(own(p).getBoolean(LodgingCellar.KICKED)&&!own(q).getBoolean(LodgingCellar.KICKED),"who kicked it is the reader's own");
                h.assertTrue(LodgingStory.house(s,b).contains(LodgingCellar.OUT_AT),"the house keeps when it came out");
            })
            .thenWaitUntil(()->h.assertTrue(who[LodgingPeople.FAILED_POST].gesture()==LodgingPeople.LOOSE,"the wrist slips first: "+who[LodgingPeople.FAILED_POST].gesture()))
            .thenExecute(()->{
                h.assertTrue(LodgingPeople.onFloor(who[LodgingPeople.FAILED_POST]),"it still sits on the floor coming loose");
                h.assertFalse(own(p).getBoolean(LodgingCellar.KNOWS_THIRD),"nothing learned from a slipped wrist");
                var hs=LodgingStory.house(s,b);hs.putInt("Clock",hs.getInt("Clock")+LodgingCellar.LOOSEN);LodgingStory.save(s,b,hs);
            })
            .thenWaitUntil(()->{h.assertTrue(who[LodgingPeople.FAILED_POST].gesture()==LodgingPeople.LOOSE+1,"the arm drops away: "+who[LodgingPeople.FAILED_POST].gesture());
                h.assertTrue(own(p).getBoolean(LodgingCellar.KNOWS_THIRD),"the reader watching learns the third way");})
            .thenExecute(()->{
                h.assertFalse(own(q).getBoolean(LodgingCellar.KNOWS_THIRD),"the reader upstairs saw nothing");
                var hs=LodgingStory.house(s,b);hs.putInt("Clock",hs.getInt("Clock")+LodgingCellar.LOOSEN*5);LodgingStory.save(s,b,hs);
            })
            .thenWaitUntil(()->h.assertTrue(who[LodgingPeople.FAILED_POST].gesture()==LodgingPeople.LOOSE+3,"four stages and no more: "+who[LodgingPeople.FAILED_POST].gesture()))
            .thenExecute(()->{
                // Driven back, it is tied and still.
                p.setShiftKeyDown(true);LodgingHouse.click(p,b,post,own(p));p.setShiftKeyDown(false);
                h.assertTrue(l.getBlockState(shoe).getValue(LodgingBlock.STAGE)==1,"the wedge goes back in");
                h.assertFalse(LodgingStory.house(s,b).contains(LodgingCellar.OUT_AT),"the house forgets when it came out");
                h.assertTrue(own(p).getBoolean(LodgingCellar.KNOWS_THIRD),"what the reader learned stays learned");
            })
            .thenWaitUntil(()->h.assertTrue(who[LodgingPeople.FAILED_POST].gesture()==LodgingPeople.TIED,"tied again: "+who[LodgingPeople.FAILED_POST].gesture()))
            .thenExecute(()->{
                // The one who knows can put it to Vera; the other cannot.
                var vera=who[LodgingPeople.VERA];var v=rel(b,vera);f.at(p,P,v.x+1.2,v.y,v.z);f.at(q,P,v.x-1.2,v.y,v.z);
                LodgingPeople.talk(p,vera,own(p));
                h.assertTrue(Talk.choose(p,LodgingTexts.R_CELLAR),"the reader who saw it can say so: "+Talk.replies(p));
                has(h,p,"Vera: "+LodgingTexts.VERA_CELLAR);
                h.assertFalse(Talk.replies(p).contains(LodgingTexts.R_CELLAR),"once");
                h.assertTrue(own(p).getInt(LodgingVera.ANSWER)==LodgingVera.NONE,"saying so is not an answer");
                Talk.close(p);
                LodgingPeople.talk(q,vera,own(q));
                h.assertFalse(Talk.replies(q).contains(LodgingTexts.R_CELLAR),"the reader who did not see it cannot");
                Talk.close(q);
            })
            .thenSucceed();
    }
    @AfterBatch(batch="lodging_cellar") public static void cellarDone(ServerLevel l){done();}

    // ------------------------------------------------------------------------------------------------ 0.4.611: N10 and N11, the two men
    /** Daniel out of the frame and standing in the parlor (as the key's walk leaves him), the chapter in Act II. */
    private static void danielInParlor(ServerLevel l,BlockPos b,LiteraryActor[] who){
        var s=l.getServer();at(l,b,LodgingStory.AFTER,who);var h=LodgingStory.house(s,b);h.putBoolean(LodgingKey.FREE,true);h.putBoolean(LodgingKey.IN,true);LodgingStory.save(s,b,h);
        var w=LodgingPeople.world(b,LodgingKey.DANIEL_PARLOR);who[LodgingPeople.DANIEL].moveTo(w.x,w.y,w.z,LodgingKey.DANIEL_PARLOR.look(),0);who[LodgingPeople.DANIEL].gesture(LodgingPeople.WAIT);
    }
    private static void mark(ServerPlayer p,String key){var o=own(p);o.putBoolean(key,true);LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),P,o);}
    @GameTest(template="empty",batch="lodging_meeting",timeoutTicks=4000)
    public static void theTwoMenMeetInTheParlorAndDanAsksForAMinute(GameTestHelper h){
        var f=house(h);var p=f.player("lodging_meet_a",P);var q=f.player("lodging_meet_b",P);var l=f.level(P);var b=f.base(P);var s=l.getServer();
        f.chunks.hold(l,LodgingPeople.area(b));LodgingStory.hold(b,true);final LiteraryActor[] who=new LiteraryActor[LodgingPeople.COUNT];
        h.startSequence()
            .thenWaitUntil(()->{h.assertTrue(f.chunks.ready(),"the house is resident");LodgingPeople.ensure(l,b);
                for(int i=0;i<LodgingPeople.COUNT;i++){who[i]=LodgingPeople.find(l,b,i);h.assertTrue(who[i]!=null,"waiting for number "+i);}})
            .thenExecute(()->{
                // Who acts first follows what was said aloud (N11's order).
                var t=new CompoundTag();h.assertTrue(LodgingMeeting.choose(t)==LodgingMeeting.RAE_TAKES,"nobody has said anything: Rae, still trusted, takes him");
                t.putBoolean(LodgingMeeting.TOLD_RAE,true);h.assertTrue(LodgingMeeting.choose(t)==LodgingMeeting.DAN_HOLDS,"Rae exposed and Dan alone: he takes hold of Daniel");
                t.putBoolean(LodgingMeeting.SUPPORTED,true);h.assertTrue(LodgingMeeting.choose(t)==LodgingMeeting.DAN_MINUTE,"told and supported: Dan asks for a minute");
                t.putBoolean(LodgingPeople.ARMED,true);t.putBoolean(LodgingKey.FREE,true);h.assertTrue(LodgingMeeting.choose(t)==LodgingMeeting.DANIEL_GOES,"Daniel armed, free and not backed goes for Dan");
                t.putBoolean(LodgingMeeting.BACKED,true);h.assertTrue(LodgingMeeting.choose(t)==LodgingMeeting.DAN_MINUTE,"backed, he holds off");
                t.putBoolean(LodgingMeeting.TOLD_VERA,true);h.assertTrue(LodgingMeeting.choose(t)==LodgingMeeting.VERA_GOES,"Vera exposed goes first of all");
                // Daniel out in the parlor; one reader in the parlor who knows the cellar and promised Rae to stay with Dan, one in the hall.
                danielInParlor(l,b,who);
                mark(p,LodgingCellar.KNOWS_THIRD);var o=own(p);o.putInt(LodgingPeople.RAE_ANSWER,LodgingPeople.STAY);LiteraryVignettes.save(LabyrinthData.get(s),p.getUUID(),P,o);
                f.at(p,P,-9.5,0,-2.5);f.at(q,P,2.5,0,-7.5);
                LodgingStory.hold(b,false);
            })
            .thenWaitUntil(()->h.assertTrue(LodgingMeeting.stage(l,b)==LodgingMeeting.ASKED,"waiting for the lines to Rae: meeting "+LodgingMeeting.stage(l,b)+", line "+LodgingStory.house(s,b).getInt(LodgingMeeting.LINE)+", Rae at "+rel(b,who[LodgingPeople.RAE])+", Dan at "+rel(b,who[LodgingPeople.DAN])))
            .thenExecute(()->{
                h.assertTrue(LodgingSite.inParlor(rel(b,who[LodgingPeople.RAE]))&&LodgingSite.inParlor(rel(b,who[LodgingPeople.DAN])),"Rae and Dan came into the parlor: "+rel(b,who[LodgingPeople.RAE])+" "+rel(b,who[LodgingPeople.DAN]));
                h.assertFalse(LodgingSite.open(l,b,LodgingSite.PARLOR_DOOR),"the parlor door is shut behind them");
                LodgingPeople.talk(p,who[LodgingPeople.DAN],own(p));
                h.assertFalse(Talk.replies(p).contains(LodgingTexts.R_SAY_RAE),"what Rae said is only for a reader who heard her: "+Talk.replies(p));
                h.assertFalse(Talk.replies(p).contains(LodgingTexts.R_SAY_VERA),"and what Vera said for one who heard her");
                h.assertTrue(Talk.choose(p,LodgingTexts.R_SAY_CELLAR),"the reader who saw the cellar can tell them");
                has(h,p,"Dan: "+LodgingTexts.DAN_ME_AS_WELL);
                h.assertFalse(Talk.replies(p).contains(LodgingTexts.R_SAY_CELLAR),"once");
                h.assertTrue(Talk.choose(p,LodgingTexts.R_KEEP_DAN),"and keep the promise to stay with Dan out loud");
                has(h,p,"Dan: "+LodgingTexts.DAN_ALL_RIGHT);
                Talk.close(p);
                LodgingPeople.talk(q,who[LodgingPeople.RAE],own(q));
                h.assertFalse(Talk.replies(q).contains(LodgingTexts.R_SAY_CELLAR),"the cellar was told already: "+Talk.replies(q));
                h.assertFalse(Talk.replies(q).contains(LodgingTexts.R_KEEP_DAN),"a promise is only its own reader's");
                h.assertTrue(Talk.choose(q,LodgingTexts.R_LET_HER),"anybody can let her answer");
                for(var line:LodgingTexts.RAE_WHY)has(h,q,"Rae: "+line);
                Talk.close(q);
            })
            .thenWaitUntil(()->h.assertTrue(LodgingMeeting.stage(l,b)==LodgingMeeting.DONE,"waiting for somebody to act: "+LodgingMeeting.stage(l,b)))
            .thenExecute(()->{
                var hs=LodgingStory.house(s,b);
                h.assertTrue(hs.getInt(LodgingMeeting.ATTEMPT)==LodgingMeeting.DAN_MINUTE&&hs.getBoolean(LodgingMeeting.LANDED),"told and stood by, Dan asks for his minute: "+hs.getInt(LodgingMeeting.ATTEMPT));
                h.assertTrue(who[LodgingPeople.DAN].gesture()==LodgingPeople.PALMS,"his hands up between himself and the room");
            })
            .thenSucceed();
    }
    @GameTest(template="empty",batch="lodging_meeting_frame",timeoutTicks=4000)
    public static void aReaderTakesDanToTheFrameAndStopsRaeTakingHimUp(GameTestHelper h){
        var f=house(h);var p=f.player("lodging_frame_a",P);var l=f.level(P);var b=f.base(P);var s=l.getServer();
        f.chunks.hold(l,LodgingPeople.area(b));LodgingStory.hold(b,true);final LiteraryActor[] who=new LiteraryActor[LodgingPeople.COUNT];
        h.startSequence()
            .thenWaitUntil(()->{h.assertTrue(f.chunks.ready(),"the house is resident");LodgingPeople.ensure(l,b);
                for(int i=0;i<LodgingPeople.COUNT;i++){who[i]=LodgingPeople.find(l,b,i);h.assertTrue(who[i]!=null,"waiting for number "+i);}})
            .thenExecute(()->{
                at(l,b,LodgingStory.AFTER,who);var hs=LodgingStory.house(s,b);hs.putBoolean(LodgingPeople.DAN_KNOWS,true);LodgingStory.save(s,b,hs);
                var d=rel(b,who[LodgingPeople.DAN]);f.at(p,P,d.x-1.2,d.y,d.z-1);
                LodgingPeople.talk(p,who[LodgingPeople.DAN],own(p));
                h.assertTrue(Talk.choose(p,LodgingTexts.R_COME_SEE),"Dan asked to be shown; the reader can take him: "+Talk.replies(p));
                has(h,p,"Dan: "+LodgingTexts.DAN_SHOW_ME);
                h.assertTrue(own(p).getBoolean(LodgingMeeting.BROUGHT),"the reader's own record");
                f.at(p,P,-7.5,0,-22.5);LodgingStory.hold(b,false);
            })
            .thenWaitUntil(()->h.assertTrue(LodgingMeeting.stage(l,b)==LodgingMeeting.ASKED,"waiting for them at the frame: meeting "+LodgingMeeting.stage(l,b)+", Dan at "+rel(b,who[LodgingPeople.DAN])+", Rae at "+rel(b,who[LodgingPeople.RAE])))
            .thenExecute(()->{
                h.assertTrue(LodgingSite.inDaniels(rel(b,who[LodgingPeople.DAN]))&&LodgingSite.inDaniels(rel(b,who[LodgingPeople.RAE])),"Dan and Rae are in Daniel's room: "+rel(b,who[LodgingPeople.DAN])+" "+rel(b,who[LodgingPeople.RAE]));
                h.assertTrue(LodgingStory.stage(s,b)==LodgingStory.AFTER,"Vera's pin waits while they are there");
                // Nobody says anything; the room's wait runs out.
                var hs=LodgingStory.house(s,b);hs.putInt(LodgingMeeting.AT,hs.getInt("Clock")-LodgingMeeting.WAIT);LodgingStory.save(s,b,hs);
            })
            .thenWaitUntil(()->{var hs=LodgingStory.house(s,b);h.assertTrue(hs.getInt(LodgingMeeting.ATTEMPT)==LodgingMeeting.RAE_TAKES&&hs.getInt("ActLeg")>=1,"waiting for Rae to take him: attempt "+hs.getInt(LodgingMeeting.ATTEMPT)+", leg "+hs.getInt("ActLeg"));})
            .thenExecute(()->{
                h.assertTrue(who[LodgingPeople.RAE].gesture()==LodgingPeople.DRAG,"Rae has him by the arm");
                LodgingPeople.talk(p,who[LodgingPeople.RAE],own(p));
                h.assertTrue(Talk.choose(p,LodgingTexts.R_STOP),"a hand on her stops it");
                has(h,p,"Rae: "+LodgingTexts.ATTEMPT_STOPPED[LodgingMeeting.RAE_TAKES-1]);
                var hs=LodgingStory.house(s,b);
                h.assertTrue(hs.getBoolean(LodgingMeeting.STOPPED)&&!hs.getBoolean(LodgingMeeting.LANDED)&&hs.getInt(LodgingMeeting.STAGE)==LodgingMeeting.DONE,"stopped, not done");
                h.assertTrue(own(p).getBoolean(LodgingMeeting.STOPPED_IT),"by this reader");
            })
            .thenIdle(40)
            .thenExecute(()->h.assertTrue(LodgingSite.inDaniels(rel(b,who[LodgingPeople.RAE]))||LodgingSite.inDaniels(rel(b,who[LodgingPeople.DAN])),"they stop where they were stopped, not at the stair: "+rel(b,who[LodgingPeople.RAE])))
            .thenSucceed();
    }
    @GameTest(template="empty",batch="lodging_meeting_vera",timeoutTicks=5000)
    public static void toldWhatVeraSaidSheGoesForTheFrame(GameTestHelper h){
        var f=house(h);var p=f.player("lodging_mvera_a",P);var l=f.level(P);var b=f.base(P);var s=l.getServer();
        f.chunks.hold(l,LodgingPeople.area(b));LodgingStory.hold(b,true);final LiteraryActor[] who=new LiteraryActor[LodgingPeople.COUNT];
        h.startSequence()
            .thenWaitUntil(()->{h.assertTrue(f.chunks.ready(),"the house is resident");LodgingPeople.ensure(l,b);
                for(int i=0;i<LodgingPeople.COUNT;i++){who[i]=LodgingPeople.find(l,b,i);h.assertTrue(who[i]!=null,"waiting for number "+i);}})
            .thenExecute(()->{danielInParlor(l,b,who);mark(p,LodgingVera.TRUTH);f.at(p,P,-9.5,0,-2.5);LodgingStory.hold(b,false);})
            .thenWaitUntil(()->h.assertTrue(LodgingMeeting.stage(l,b)==LodgingMeeting.ASKED,"waiting for the lines: "+LodgingMeeting.stage(l,b)))
            .thenExecute(()->{
                LodgingPeople.talk(p,who[LodgingPeople.DANIEL],own(p));
                h.assertTrue(Talk.choose(p,LodgingTexts.R_SAY_VERA),"the reader who heard her can say so: "+Talk.replies(p));
                has(h,p,"Dan: "+LodgingTexts.DAN_WHICH);has(h,p,"Daniel: "+LodgingTexts.DANIEL_YOU_KNOW);
                Talk.close(p);
                var hs=LodgingStory.house(s,b);hs.putInt(LodgingMeeting.AT,hs.getInt("Clock")-LodgingMeeting.WAIT);LodgingStory.save(s,b,hs);
                f.at(p,P,-7.5,0,-22.5);
            })
            .thenWaitUntil(()->{var hs=LodgingStory.house(s,b);h.assertTrue(hs.getBoolean(LodgingMeeting.LANDED),"waiting for Vera at the frame: attempt "+hs.getInt(LodgingMeeting.ATTEMPT)+", Vera at "+rel(b,who[LodgingPeople.VERA]));})
            .thenExecute(()->{
                h.assertTrue(LodgingStory.house(s,b).getInt(LodgingMeeting.ATTEMPT)==LodgingMeeting.VERA_GOES,"exposed, Vera went first");
                h.assertTrue(LodgingSite.inDaniels(rel(b,who[LodgingPeople.VERA])),"she is in Daniel's room: "+rel(b,who[LodgingPeople.VERA]));
            })
            .thenSucceed();
    }
    @AfterBatch(batch="lodging_meeting") public static void meetingDone(ServerLevel l){done();}
    @AfterBatch(batch="lodging_meeting_frame") public static void meetingFrameDone(ServerLevel l){done();}
    @AfterBatch(batch="lodging_meeting_vera") public static void meetingVeraDone(ServerLevel l){done();}

    // ------------------------------------------------------------------------------------------------ 0.4.612: N12, taken; Vera's activity
    /** The meeting over in this room, the first intrusion long done, and the second about to come through. */
    private static void disturbed(ServerLevel l,BlockPos b,int where,boolean coming){
        var s=l.getServer();var h=LodgingStory.house(s,b);int clock=h.getInt("Clock");
        h.putInt(LodgingMeeting.STAGE,LodgingMeeting.DONE);h.putInt(LodgingMeeting.WHERE,where);h.putInt(LodgingMeeting.ATTEMPT,LodgingMeeting.DAN_MINUTE);h.putBoolean(LodgingMeeting.LANDED,true);
        var in=LodgingInjury.state(s,b);in.putInt("Phase",LodgingInjury.OVER);in.putBoolean("Shut",true);
        var t=new CompoundTag();t.putInt("Phase",coming?LodgingTaken.WAITING:LodgingTaken.OVER);t.putInt("At",clock-LodgingTaken.AFTER);t.putInt("Where",where);
        if(!coming)t.putBoolean("Shut",true);
        h.put(LodgingTaken.STATE,t);LodgingStory.save(s,b,h);
    }
    private static void answer(ServerPlayer p,int answer){var o=own(p);o.putInt(LodgingVera.ANSWER,answer);LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),P,o);}
    private static void place(LiteraryActor a,BlockPos b,double x,double z,float yaw){a.moveTo(b.getX()+x,b.getY(),b.getZ()+z,yaw,0);a.setYHeadRot(yaw);a.setYBodyRot(yaw);}
    @GameTest(template="empty",batch="lodging_taken",timeoutTicks=7000)
    public static void aReaderTakenIntoTheWallKnocksItAwayAndWorksTheLatch(GameTestHelper h){
        var f=house(h);var p=f.player("lodging_taken_a",P);var q=f.player("lodging_taken_b",P);var l=f.level(P);var b=f.base(P);var s=l.getServer();
        f.chunks.hold(l,LodgingPeople.area(b));LodgingStory.hold(b,true);final LiteraryActor[] who=new LiteraryActor[LodgingPeople.COUNT];
        h.startSequence()
            .thenWaitUntil(()->{h.assertTrue(f.chunks.ready(),"the house is resident");LodgingPeople.ensure(l,b);
                for(int i=0;i<LodgingPeople.COUNT;i++){who[i]=LodgingPeople.find(l,b,i);h.assertTrue(who[i]!=null,"waiting for number "+i);}})
            .thenExecute(()->{
                // The two men have been put together in the parlor; Rae and Dan back toward the windows, one reader just inside the door.
                danielInParlor(l,b,who);disturbed(l,b,LodgingMeeting.PARLOR,true);
                place(who[LodgingPeople.DAN],b,-10.5,-4.5,0);place(who[LodgingPeople.RAE],b,-11.5,-3.0,0);
                f.at(p,P,-5.8,0,-10.0);f.at(q,P,-12.5,0,-2.5);
                LodgingStory.hold(b,false);
            })
            .thenWaitUntil(()->h.assertTrue(p.getUUID().equals(LodgingTaken.held(s,b)),"waiting for it to take the reader by the door: "+LodgingTaken.phase(s,b)+", it at "+rel(b,who[LodgingPeople.FAILED_SHIRT])+" "+LodgingStory.house(s,b).getCompound(LodgingTaken.STATE)))
            .thenIdle(20)
            .thenExecute(()->{
                h.assertTrue(who[LodgingPeople.FAILED_SHIRT].gesture()==LodgingPeople.DRAG,"it walks the reader out by the arm");
                h.assertTrue(LodgingSite.open(l,b,LodgingSite.PARLOR_DOOR),"the parlor door stands open while they go through it");
            })
            .thenWaitUntil(()->h.assertTrue(own(p).getBoolean(LodgingTaken.TAKEN),"waiting for it to leave the reader in the wall: "+LodgingTaken.phase(s,b)+", it at "+rel(b,who[LodgingPeople.FAILED_SHIRT])+", reader at "+rel(b,p)))
            .thenExecute(()->{
                h.assertTrue(LodgingSite.inPassages(rel(b,p)),"the reader is in the passage behind the paper: "+rel(b,p));
                
                // A second: somebody is still in the parlor.
                h.assertTrue(LodgingTaken.phase(s,b)==LodgingTaken.COMING,"it goes back for another: "+LodgingTaken.phase(s,b));
                // The reader goes along the passage to the parlor's wall.
                f.at(p,P,-14.5,0,-8.5);
            })
            .thenWaitUntil(()->h.assertTrue(LodgingTaken.phase(s,b)==LodgingTaken.LOOSE,"waiting for it to be in the parlor again: "+LodgingTaken.phase(s,b)+", it at "+rel(b,who[LodgingPeople.FAILED_SHIRT])))
            .thenExecute(()->{
                // A hand on the lath: it lets the room be.
                h.assertTrue(LodgingHouse.click(p,b,new BlockPos(LodgingSite.ROOM_W,1,-8),own(p)),"a hand on the lath from inside the wall is a knock");
                
                h.assertTrue(own(p).getBoolean(LodgingTaken.KNOCKED)&&own(p).getBoolean(LodgingTaken.DREW_OFF),"the reader's own record");
                h.assertTrue(LodgingTaken.phase(s,b)==LodgingTaken.BACK,"it turns back toward the knocking");
                h.assertTrue(own(q).getBoolean(LodgingTaken.HEARD_KNOCK),"the reader in the parlor heard it");
                
            })
            .thenWaitUntil(()->h.assertTrue(LodgingTaken.latched(s,b),"waiting for the jib door to swing to and latch: "+LodgingTaken.phase(s,b)+", it at "+rel(b,who[LodgingPeople.FAILED_SHIRT])))
            .thenExecute(()->{
                h.assertFalse(LodgingSite.open(l,b,LodgingSite.JIB),"the jib door is shut");
                h.assertTrue(LodgingStory.house(s,b).getCompound(LodgingTaken.STATE).getInt("Takes")==1,"it took one: the knock drew it off before a second");
                f.at(p,P,-14.5,0,-12.5);
                LodgingHouse.click(p,b,LodgingSite.JIB,own(p));
                h.assertTrue(LodgingTaken.latched(s,b),"standing, a hand on it does not lift the latch");
                p.setShiftKeyDown(true);
                for(int i=0;i<LodgingTaken.LATCH_TRIES-1;i++){LodgingHouse.click(p,b,LodgingSite.JIB,own(p));h.assertTrue(LodgingTaken.latched(s,b),"one try at a time: "+i);}
                LodgingHouse.click(p,b,LodgingSite.JIB,own(p));p.setShiftKeyDown(false);
                h.assertFalse(LodgingTaken.latched(s,b),"crouched, a finger through the gap works it up");
                h.assertTrue(LodgingSite.open(l,b,LodgingSite.JIB)&&own(p).getBoolean(LodgingTaken.WORKED_LATCH),"the door gives, and it is the reader's own doing");
            })
            .thenSucceed();
    }
    @GameTest(template="empty",batch="lodging_taken_dan",timeoutTicks=8000)
    public static void danIsTakenFromTheFrameLetOutAndVeraCallsForAReaderAgainstHer(GameTestHelper h){
        var f=house(h);var q=f.player("lodging_tdan_a",P);var l=f.level(P);var b=f.base(P);var s=l.getServer();
        f.chunks.hold(l,LodgingPeople.area(b));LodgingStory.hold(b,true);final LiteraryActor[] who=new LiteraryActor[LodgingPeople.COUNT];
        h.startSequence()
            .thenWaitUntil(()->{h.assertTrue(f.chunks.ready(),"the house is resident");LodgingPeople.ensure(l,b);
                for(int i=0;i<LodgingPeople.COUNT;i++){who[i]=LodgingPeople.find(l,b,i);h.assertTrue(who[i]!=null,"waiting for number "+i);}})
            .thenExecute(()->{
                // Dan was brought to the frame; Daniel is still held in it; Rae is out in the hall. The reader is against Vera.
                at(l,b,LodgingStory.AFTER,who);disturbed(l,b,LodgingMeeting.FRAME,true);
                var w=LodgingPeople.world(b,LodgingMeeting.DAN_FRAME);who[LodgingPeople.DAN].moveTo(w.x,w.y,w.z,LodgingMeeting.DAN_FRAME.look(),0);
                place(who[LodgingPeople.RAE],b,1.5,-3.5,0);
                answer(q,LodgingVera.SEND_BACK);f.at(q,P,2.5,0,-13.5);
                LodgingStory.hold(b,false);
            })
            .thenWaitUntil(()->h.assertTrue(LodgingTaken.heldActor(s,b)==LodgingPeople.DAN,"waiting for it to take Dan: "+LodgingTaken.phase(s,b)+", it at "+rel(b,who[LodgingPeople.FAILED_SHIRT])))
            .thenExecute(()->h.assertTrue(who[LodgingPeople.DAN].gesture()==LodgingPeople.STRAIN,"Dan pulls against it"))
            .thenWaitUntil(()->h.assertTrue(LodgingTaken.latched(s,b),"waiting for it to leave Dan in the wall and latch the door: "+LodgingTaken.phase(s,b)+", Dan at "+rel(b,who[LodgingPeople.DAN])))
            .thenExecute(()->{
                h.assertTrue(LodgingTaken.inWall(s,b,LodgingPeople.DAN)&&LodgingSite.inPassages(rel(b,who[LodgingPeople.DAN])),"Dan is in the passage: "+rel(b,who[LodgingPeople.DAN]));
                h.assertTrue(LodgingStory.house(s,b).getCompound(LodgingTaken.STATE).getInt("Takes")==1,"nobody else in that room it can take: Vera is never, Daniel is held");
                // From the house's side, a hand lifts it.
                LodgingHouse.click(q,b,LodgingSite.JIB,own(q));
                h.assertFalse(LodgingTaken.latched(s,b),"lifted from the house's side at once");
                h.assertTrue(own(q).getBoolean(LodgingTaken.LIFTED_LATCH),"by this reader");
                f.at(q,P,2.5,0,-5.5);
            })
            .thenWaitUntil(()->h.assertTrue(!LodgingTaken.inWall(s,b,LodgingPeople.DAN)&&LodgingSite.inDaniels(rel(b,who[LodgingPeople.DAN]))&&LodgingStory.house(s,b).getCompound(LodgingTaken.STATE).getCompound("Going").isEmpty(),
                "waiting for Dan to come back out to the frame: Dan at "+rel(b,who[LodgingPeople.DAN])+" "+LodgingStory.house(s,b).getCompound(LodgingTaken.STATE)))
            .thenWaitUntil(()->h.assertTrue(LodgingTaken.veraPhase(s,b)==LodgingTaken.V_CALLED,"waiting for Vera to go up and call: "+LodgingTaken.veraPhase(s,b)+", Vera at "+rel(b,who[LodgingPeople.VERA])+" "+LodgingStory.house(s,b).getCompound(LodgingTaken.VERA_STATE)))
            .thenExecute(()->{
                h.assertTrue(LodgingSite.inWorkroom(rel(b,who[LodgingPeople.VERA])),"she is in the workroom: "+rel(b,who[LodgingPeople.VERA]));
                has(h,q,"Vera: "+LodgingTexts.VERA_CALLS);
                f.at(q,P,-9.5,LodgingSite.UPPER,-22.5);
                LodgingPeople.talk(q,who[LodgingPeople.VERA],own(q));
                for(var line:LodgingTexts.VERA_SENT_ASK)has(h,q,"Vera: "+line);
                h.assertTrue(Talk.choose(q,LodgingTexts.R_WHEN_YOU_CALL),"the reader who came up can promise: "+Talk.replies(q));
                has(h,q,"Vera: "+LodgingTexts.VERA_SENT_YES);
                h.assertTrue(own(q).getBoolean(LodgingTaken.AGREED),"the reader's own promise");
                h.assertTrue(LodgingDoubles.sleeper(l,b,0)==null||LodgingDoubles.sleeper(l,b,0).gesture()<LodgingTaken.STAND,"nobody's body was raised");
            })
            .thenSucceed();
    }
    @GameTest(template="empty",batch="lodging_taken_double",timeoutTicks=8000)
    public static void veraBringsAReadersBodyDownAndOnlyItsOriginalCanPutItsHandsThere(GameTestHelper h){
        var f=house(h);var p=f.player("lodging_double_a",P);var q=f.player("lodging_double_b",P);var l=f.level(P);var b=f.base(P);var s=l.getServer();
        f.chunks.hold(l,LodgingPeople.area(b));LodgingStory.hold(b,true);final LiteraryActor[] who=new LiteraryActor[LodgingPeople.COUNT];final LiteraryActor[] mine={null};
        h.startSequence()
            .thenWaitUntil(()->{h.assertTrue(f.chunks.ready(),"the house is resident");LodgingPeople.ensure(l,b);
                for(int i=0;i<LodgingPeople.COUNT;i++){who[i]=LodgingPeople.find(l,b,i);h.assertTrue(who[i]!=null,"waiting for number "+i);}})
            .thenExecute(()->{
                // The first reader has room 2 (west, the gallery's far end) and is for Vera; the second said nothing to her.
                at(l,b,LodgingStory.AFTER,who);disturbed(l,b,LodgingMeeting.FRAME,false);
                var hs=LodgingStory.house(s,b);var rooms=hs.getCompound("Rooms");rooms.putInt(p.getUUID().toString(),2);hs.put("Rooms",rooms);LodgingStory.save(s,b,hs);
                answer(p,LodgingVera.HELP);
                f.at(p,P,-9.5,0,-21.5);f.at(q,P,-7.5,0,-22.5);
            })
            .thenWaitUntil(()->{mine[0]=LodgingDoubles.sleeper(l,b,2);h.assertTrue(mine[0]!=null,"waiting for the reader's body to be made in its bed");})
            .thenExecute(()->LodgingStory.hold(b,false))
            .thenWaitUntil(()->h.assertTrue(LodgingTaken.veraPhase(s,b)>=LodgingTaken.V_RAISE,"waiting for Vera to reach the bed: "+LodgingTaken.veraPhase(s,b)+", Vera at "+rel(b,who[LodgingPeople.VERA])+" "+LodgingStory.house(s,b).getCompound(LodgingTaken.VERA_STATE)))
            .thenWaitUntil(()->h.assertTrue(LodgingTaken.veraPhase(s,b)>=LodgingTaken.V_DOWN,"waiting for it to be raised"))
            .thenExecute(()->h.assertTrue(mine[0].gesture()>=LodgingTaken.STAND,"it is up out of the bed: gesture "+mine[0].gesture()))
            .thenIdle(60)
            .thenExecute(()->{
                h.assertTrue(mine[0].gesture()==LodgingTaken.LED,"led by the wrist: gesture "+mine[0].gesture());
                h.assertTrue(mine[0].distanceTo(who[LodgingPeople.VERA])<3.5,"a step or two behind her: "+mine[0].distanceTo(who[LodgingPeople.VERA]));
            })
            .thenWaitUntil(()->h.assertTrue(LodgingTaken.veraPhase(s,b)==LodgingTaken.V_ASK,"waiting for them at the frame: "+LodgingTaken.veraPhase(s,b)+", Vera at "+rel(b,who[LodgingPeople.VERA])+", it at "+rel(b,mine[0])))
            .thenIdle(10)
            .thenExecute(()->{
                h.assertTrue(LodgingSite.inDaniels(rel(b,mine[0]))&&mine[0].gesture()==LodgingTaken.STAND,"it stands at the frame, its hands at its sides: "+rel(b,mine[0])+" gesture "+mine[0].gesture());
                h.assertTrue(own(p).getBoolean(LodgingTaken.SAW_DOUBLE_LED)&&own(q).getBoolean(LodgingTaken.SAW_DOUBLE_LED),"both readers in the room saw it brought in");
                
                // The other reader cannot answer for it.
                LodgingPeople.talk(q,who[LodgingPeople.VERA],own(q));h.assertTrue(Talk.lines(q).isEmpty(),"Vera says nothing to it: "+Talk.lines(q));
                h.assertTrue(Talk.replies(q).isEmpty()||!Talk.replies(q).contains(LodgingTexts.R_STEADY),"nobody but its original is asked: "+Talk.replies(q));
                Talk.close(q);
                LodgingPeople.talk(p,who[LodgingPeople.VERA],own(p));
                has(h,p,"Vera: "+LodgingTexts.VERA_ASK_ORIGINAL);
                h.assertTrue(Talk.choose(p,LodgingTexts.R_STEADY),"its original can: "+Talk.replies(p));
                has(h,p,"Vera: "+LodgingTexts.VERA_HOLD_IT);
                h.assertTrue(mine[0].gesture()==LodgingTaken.STEADY&&own(p).getBoolean(LodgingTaken.STEADIED),"its hands where its original put them");
            })
            .thenSucceed();
    }
    @AfterBatch(batch="lodging_taken") public static void takenDone(ServerLevel l){done();}
    @AfterBatch(batch="lodging_taken_dan") public static void takenDanDone(ServerLevel l){done();}
    @AfterBatch(batch="lodging_taken_double") public static void takenDoubleDone(ServerLevel l){done();}

    // ------------------------------------------------------------------------------------------------ 0.4.613: E1, return Daniel
    private static String ret(ServerLevel l,BlockPos b){return LodgingStory.house(l.getServer(),b).getCompound(LodgingReturn.STATE).toString();}
    @GameTest(template="empty",batch="lodging_return",timeoutTicks=5000)
    public static void danAgreesGoesBackThroughTheFrameAndDanielWalksOut(GameTestHelper h){
        var f=house(h);var p=f.player("lodging_ret_a",P);var l=f.level(P);var b=f.base(P);var s=l.getServer();
        f.chunks.hold(l,LodgingPeople.area(b));LodgingStory.hold(b,true);final LiteraryActor[] who=new LiteraryActor[LodgingPeople.COUNT];
        h.startSequence()
            .thenWaitUntil(()->{h.assertTrue(f.chunks.ready(),"the house is resident");LodgingPeople.ensure(l,b);
                for(int i=0;i<LodgingPeople.COUNT;i++){who[i]=LodgingPeople.find(l,b,i);h.assertTrue(who[i]!=null,"waiting for number "+i);}})
            .thenExecute(()->{
                // Dan was brought to the frame and stood by out loud; Daniel is still held in it.
                at(l,b,LodgingStory.AFTER,who);disturbed(l,b,LodgingMeeting.FRAME,false);
                var hs=LodgingStory.house(s,b);hs.putBoolean(LodgingMeeting.SUPPORTED,true);LodgingStory.save(s,b,hs);
                var w=LodgingPeople.world(b,LodgingMeeting.DAN_FRAME);who[LodgingPeople.DAN].moveTo(w.x,w.y,w.z,LodgingMeeting.DAN_FRAME.look(),0);
                f.at(p,P,-9.5,0,-22.5);
                // A reader who does not know how the frame works cannot put it to him.
                LodgingPeople.talk(p,who[LodgingPeople.DAN],own(p));
                h.assertFalse(Talk.replies(p).contains(LodgingTexts.R_GO_BACK),"only a reader who knows: "+Talk.replies(p));Talk.close(p);
                mark(p,LodgingVera.TRUTH);
                LodgingPeople.talk(p,who[LodgingPeople.DAN],own(p));
                h.assertTrue(Talk.choose(p,LodgingTexts.R_GO_BACK),"a reader who heard Vera's truth can: "+Talk.replies(p));
                for(var line:LodgingTexts.DAN_AGREES)has(h,p,"Dan: "+line);
                h.assertTrue(LodgingReturn.way(s,b)==LodgingReturn.AGREED,"stood by, he agrees: "+ret(l,b));
                LodgingStory.hold(b,false);
            })
            .thenWaitUntil(()->h.assertTrue(LodgingReturn.phase(s,b)>=LodgingReturn.ENTERING,"waiting for Daniel to step out: "+ret(l,b)+", Dan at "+rel(b,who[LodgingPeople.DAN])+", Daniel at "+rel(b,who[LodgingPeople.DANIEL])))
            .thenExecute(()->{
                h.assertTrue(LodgingKey.free(s,b),"the straps let go of Daniel as Dan comes");
                h.assertTrue(who[LodgingPeople.DANIEL].position().distanceTo(LodgingPeople.world(b,LodgingReturn.DANIEL_OUT))<.3,"he stepped out and aside: "+rel(b,who[LodgingPeople.DANIEL]));
            })
            .thenWaitUntil(()->h.assertTrue(LodgingReturn.gone(s,b),"waiting for Dan to go back into the plaster: "+ret(l,b)+", Dan at "+rel(b,who[LodgingPeople.DAN])))
            .thenExecute(()->{
                h.assertTrue(LodgingPeople.find(l,b,LodgingPeople.DAN)==null,"Dan is not anywhere");
                h.assertTrue(own(p).getBoolean(LodgingReturn.SAW)&&own(p).getInt("ReturnWay0613")==LodgingReturn.AGREED,"the reader in the room saw it");
                LodgingPeople.ensure(l,b);h.assertTrue(LodgingPeople.find(l,b,LodgingPeople.DAN)==null,"and he is not made again");
            })
            .thenWaitUntil(()->h.assertTrue(LodgingReturn.phase(s,b)==LodgingReturn.DONE,"waiting for the old dog: "+ret(l,b)))
            .thenExecute(()->{
                has(h,p,"Rae: "+LodgingTexts.RAE_DOG);
                h.assertTrue(own(p).getBoolean(LodgingReturn.HEARD_DOG),"the reader heard its name");
            })
            .thenSucceed();
    }
    @GameTest(template="empty",batch="lodging_return_force",timeoutTicks=6000)
    public static void danRefusesAndTwoReadersWalkHimToTheFrame(GameTestHelper h){
        var f=house(h);var p=f.player("lodging_force_a",P);var q=f.player("lodging_force_b",P);var l=f.level(P);var b=f.base(P);var s=l.getServer();
        f.chunks.hold(l,LodgingPeople.area(b));LodgingStory.hold(b,true);final LiteraryActor[] who=new LiteraryActor[LodgingPeople.COUNT];
        h.startSequence()
            .thenWaitUntil(()->{h.assertTrue(f.chunks.ready(),"the house is resident");LodgingPeople.ensure(l,b);
                for(int i=0;i<LodgingPeople.COUNT;i++){who[i]=LodgingPeople.find(l,b,i);h.assertTrue(who[i]!=null,"waiting for number "+i);}})
            .thenExecute(()->{
                // The two men in the parlor; nobody stood by Dan. Both readers have seen the cellar.
                danielInParlor(l,b,who);disturbed(l,b,LodgingMeeting.PARLOR,false);
                var w=LodgingPeople.world(b,LodgingMeeting.DAN_PARLOR);who[LodgingPeople.DAN].moveTo(w.x,w.y,w.z,0,0);
                mark(p,LodgingCellar.KNOWS_THIRD);mark(q,LodgingCellar.KNOWS_THIRD);
                f.at(p,P,-7.5,0,-6.5);f.at(q,P,-8.0,0,-8.0);
                LodgingPeople.talk(p,who[LodgingPeople.DAN],own(p));
                h.assertTrue(Talk.choose(p,LodgingTexts.R_GO_BACK),"put to him: "+Talk.replies(p));has(h,p,"Dan: "+LodgingTexts.DAN_REFUSES);
                h.assertTrue(LodgingReturn.phase(s,b)==LodgingReturn.NONE,"he says no");
                LodgingPeople.talk(p,who[LodgingPeople.DAN],own(p));h.assertTrue(Talk.choose(p,LodgingTexts.R_TAKE_HIM),"then a reader can take hold of him: "+Talk.replies(p));
                LodgingPeople.talk(q,who[LodgingPeople.DAN],own(q));h.assertTrue(Talk.choose(q,LodgingTexts.R_TAKE_HIM),"and the other: "+Talk.replies(q));
                h.assertTrue(LodgingReturn.way(s,b)==LodgingReturn.FORCE&&own(p).getBoolean(LodgingReturn.HELD)&&own(q).getBoolean(LodgingReturn.HELD),"both have hold of him: "+ret(l,b));
                LodgingStory.hold(b,false);
            })
            .thenIdle(10)
            .thenExecute(()->h.assertTrue(who[LodgingPeople.DAN].gesture()==LodgingPeople.STRAIN,"he pulls against it"))
            .thenWaitUntil(()->{
                // The readers keep at his arms all the way.
                var d=who[LodgingPeople.DAN];if(d.isAlive()&&LodgingReturn.phase(s,b)==LodgingReturn.GOING){p.teleportTo(l,d.getX()+.6,d.getY(),d.getZ(),0,0);q.teleportTo(l,d.getX()-.6,d.getY(),d.getZ(),0,0);}
                else if(LodgingReturn.phase(s,b)>=LodgingReturn.ENTERING){f.at(p,P,-6.0,0,-24.5);f.at(q,P,-9.0,0,-24.0);}
                h.assertTrue(LodgingReturn.gone(s,b),"waiting for him to be walked there and go: "+ret(l,b)+", Dan at "+rel(b,d));
            })
            .thenExecute(()->{
                h.assertTrue(LodgingPeople.find(l,b,LodgingPeople.DAN)==null,"Dan is not anywhere");
                h.assertTrue(own(p).getInt("ReturnWay0613")==LodgingReturn.FORCE,"the account knows how");
                h.assertFalse(LodgingStory.house(s,b).getCompound(LodgingReturn.STATE).getBoolean("SteppedOut"),"Daniel was already out of the frame");
            })
            .thenSucceed();
    }
    @GameTest(template="empty",batch="lodging_return_panic",timeoutTicks=5000)
    public static void veraUsesDansPanicAndHeRunsForTheFrame(GameTestHelper h){
        var f=house(h);var p=f.player("lodging_panic_a",P);var l=f.level(P);var b=f.base(P);var s=l.getServer();
        f.chunks.hold(l,LodgingPeople.area(b));LodgingStory.hold(b,true);final LiteraryActor[] who=new LiteraryActor[LodgingPeople.COUNT];
        h.startSequence()
            .thenWaitUntil(()->{h.assertTrue(f.chunks.ready(),"the house is resident");LodgingPeople.ensure(l,b);
                for(int i=0;i<LodgingPeople.COUNT;i++){who[i]=LodgingPeople.find(l,b,i);h.assertTrue(who[i]!=null,"waiting for number "+i);}})
            .thenExecute(()->{
                // The party set against Vera; nobody came up to her; Dan was dragged into the walls and is back in the hall.
                at(l,b,LodgingStory.AFTER,who);disturbed(l,b,LodgingMeeting.FRAME,false);
                var hs=LodgingStory.house(s,b);hs.getCompound(LodgingTaken.STATE).putBoolean("DanWalled",true);
                var v=new CompoundTag();v.putInt("Phase",LodgingTaken.V_CALLED);v.putInt("At",hs.getInt("Clock")-LodgingReturn.PANIC_AFTER);hs.put(LodgingTaken.VERA_STATE,v);LodgingStory.save(s,b,hs);
                place(who[LodgingPeople.DAN],b,1.5,-3.5,0);f.at(p,P,-1.5,0,-3.5);
                LodgingStory.hold(b,false);
            })
            .thenWaitUntil(()->h.assertTrue(LodgingReturn.way(s,b)==LodgingReturn.PANIC,"waiting for Vera to call down: "+ret(l,b)))
            .thenExecute(()->{
                for(var line:LodgingTexts.VERA_PANIC)has(h,p,"Vera: "+line);
                LodgingPeople.talk(p,who[LodgingPeople.DAN],own(p));
                h.assertTrue(Talk.replies(p).contains(LodgingTexts.R_STOP_DAN),"a hand on him could stop him: "+Talk.replies(p));Talk.close(p);
                f.at(p,P,-9.5,0,-22.5);
            })
            .thenWaitUntil(()->h.assertTrue(LodgingReturn.gone(s,b),"waiting for him to run for it: "+ret(l,b)+", Dan at "+rel(b,who[LodgingPeople.DAN])))
            .thenExecute(()->h.assertTrue(own(p).getInt("ReturnWay0613")==LodgingReturn.PANIC&&LodgingKey.free(s,b),"Vera's way, and Daniel out of the frame"))
            .thenSucceed();
    }

    // ------------------------------------------------------------------------------------------------ 0.4.614: E2, complete Dan
    private static String comp(ServerLevel l,BlockPos b){return LodgingStory.house(l.getServer(),b).getCompound(LodgingComplete.STATE).toString();}
    @GameTest(template="empty",batch="lodging_complete",timeoutTicks=6000)
    public static void aSteadiedBodyAndTheStrapsHoldDanielUntilTheRoomClosesOnHim(GameTestHelper h){
        var f=house(h);var p=f.player("lodging_comp_a",P);var l=f.level(P);var b=f.base(P);var s=l.getServer();
        f.chunks.hold(l,LodgingPeople.area(b));LodgingStory.hold(b,true);final LiteraryActor[] who=new LiteraryActor[LodgingPeople.COUNT];final LiteraryActor[] mine={null};
        h.startSequence()
            .thenWaitUntil(()->{h.assertTrue(f.chunks.ready(),"the house is resident");LodgingPeople.ensure(l,b);
                for(int i=0;i<LodgingPeople.COUNT;i++){who[i]=LodgingPeople.find(l,b,i);h.assertTrue(who[i]!=null,"waiting for number "+i);}})
            .thenExecute(()->{
                // Daniel still strapped in his frame; the reader has room 2 and is for Vera.
                at(l,b,LodgingStory.AFTER,who);disturbed(l,b,LodgingMeeting.FRAME,false);
                var hs=LodgingStory.house(s,b);var rooms=hs.getCompound("Rooms");rooms.putInt(p.getUUID().toString(),2);hs.put("Rooms",rooms);LodgingStory.save(s,b,hs);
                answer(p,LodgingVera.HELP);f.at(p,P,-9.5,0,-22.5);
            })
            .thenWaitUntil(()->{mine[0]=LodgingDoubles.sleeper(l,b,2);h.assertTrue(mine[0]!=null,"waiting for the reader's body to be made in its bed");})
            .thenExecute(()->{
                // Vera has brought it down, and its original has put its hands on the frame.
                var hs=LodgingStory.house(s,b);var v=new CompoundTag();v.putInt("Phase",LodgingTaken.V_ASK);v.putInt("Room",2);v.putUUID("For",p.getUUID());
                v.putBoolean("Asked",true);v.putBoolean("Answered",true);v.putBoolean("Steadied",true);hs.put(LodgingTaken.VERA_STATE,v);LodgingStory.save(s,b,hs);
                var at=LodgingPeople.world(b,LodgingTaken.DOUBLE_AT);mine[0].setPos(at.x,at.y,at.z);mine[0].gesture(LodgingTaken.STEADY);
                var va=LodgingPeople.world(b,LodgingTaken.VERA_AT);who[LodgingPeople.VERA].moveTo(va.x,va.y,va.z,LodgingTaken.VERA_AT.look(),0);
                place(who[LodgingPeople.RAE],b,1.5,-3.5,0);
                LodgingStory.hold(b,false);
            })
            .thenWaitUntil(()->h.assertTrue(LodgingComplete.phase(s,b)>=LodgingComplete.HOLDING,"waiting for Vera to begin: "+comp(l,b)))
            .thenExecute(()->{
                has(h,p,"Vera: "+LodgingTexts.VERA_NOW[0]);
                // Nobody may put Dan back once it has begun.
                mark(p,LodgingVera.TRUTH);LodgingPeople.talk(p,who[LodgingPeople.DAN],own(p));
                h.assertFalse(Talk.replies(p).contains(LodgingTexts.R_GO_BACK),"E1 is not offered while Daniel is being held: "+Talk.replies(p));Talk.close(p);
            })
            .thenWaitUntil(()->h.assertTrue(LodgingStory.house(s,b).getCompound(LodgingComplete.STATE).getInt("ChairSteps")>=1,"waiting for the chair to be pushed back: "+comp(l,b)))
            .thenWaitUntil(()->h.assertTrue(LodgingComplete.gone(s,b),"waiting for the room to close on him: "+comp(l,b)))
            .thenExecute(()->{
                h.assertTrue(LodgingPeople.find(l,b,LodgingPeople.DANIEL)==null,"Daniel is not anywhere");
                LodgingPeople.ensure(l,b);h.assertTrue(LodgingPeople.find(l,b,LodgingPeople.DANIEL)==null,"and is not made again");
                for(int z=0;z<5;z++)h.assertFalse(LodgingThing.is(l.getBlockState(b.offset(LodgingSite.DANIEL_CHAIR).north(z)),LodgingThing.Kind.CHAIR),"his chair is gone into the wall ("+z+")");
                h.assertTrue(own(p).getBoolean(LodgingComplete.SAW),"the reader in the room saw it");
            })
            .thenWaitUntil(()->h.assertTrue(LodgingComplete.phase(s,b)==LodgingComplete.DONE,"waiting for Dan and Rae: "+comp(l,b)+", Rae at "+rel(b,who[LodgingPeople.RAE])+", Dan at "+rel(b,who[LodgingPeople.DAN])))
            .thenExecute(()->{
                has(h,p,"Daniel: "+LodgingTexts.DANIEL_WALL[0]);has(h,p,"Dan: "+LodgingTexts.DAN_RAE);has(h,p,"Daniel: "+LodgingTexts.DANIEL_UNDER);
                h.assertTrue(own(p).getBoolean(LodgingComplete.HEARD_UNDER),"the reader heard the voice under his");
                h.assertTrue(who[LodgingPeople.DAN].gesture()==LodgingComplete.MOUTH&&who[LodgingPeople.RAE].gesture()==LodgingComplete.TAKE,"his hand at his mouth, hers at his wrist");
                h.assertTrue(who[LodgingPeople.RAE].distanceTo(who[LodgingPeople.DAN])<1.2,"close enough to take it: "+who[LodgingPeople.RAE].distanceTo(who[LodgingPeople.DAN]));
                h.assertTrue(LodgingTexts.account("x",own(p)).toString().length()>0,"the account is written");
            })
            .thenSucceed();
    }
    @GameTest(template="empty",batch="lodging_complete_held",timeoutTicks=6000)
    public static void readersWalkAFreeDanielToHisFrameAndLettingGoLosesIt(GameTestHelper h){
        var f=house(h);var p=f.player("lodging_held_a",P);var q=f.player("lodging_held_b",P);var l=f.level(P);var b=f.base(P);var s=l.getServer();
        f.chunks.hold(l,LodgingPeople.area(b));LodgingStory.hold(b,true);final LiteraryActor[] who=new LiteraryActor[LodgingPeople.COUNT];
        h.startSequence()
            .thenWaitUntil(()->{h.assertTrue(f.chunks.ready(),"the house is resident");LodgingPeople.ensure(l,b);
                for(int i=0;i<LodgingPeople.COUNT;i++){who[i]=LodgingPeople.find(l,b,i);h.assertTrue(who[i]!=null,"waiting for number "+i);}})
            .thenExecute(()->{
                // Daniel free in the parlor; the party went against Vera, but the first reader promised her their hands.
                danielInParlor(l,b,who);disturbed(l,b,LodgingMeeting.PARLOR,false);
                var hs=LodgingStory.house(s,b);var v=new CompoundTag();v.putInt("Phase",LodgingTaken.V_CALLED);v.putInt("Agreed",1);v.putInt("At",hs.getInt("Clock"));hs.put(LodgingTaken.VERA_STATE,v);LodgingStory.save(s,b,hs);
                var vw=LodgingPeople.world(b,LodgingTaken.VERA_WORKROOM);who[LodgingPeople.VERA].moveTo(vw.x,vw.y,vw.z,LodgingTaken.VERA_WORKROOM.look(),0);
                mark(p,LodgingTaken.AGREED);f.at(p,P,-7.5,0,-6.5);f.at(q,P,-8.0,0,-8.0);
                LodgingStory.hold(b,false);
            })
            .thenWaitUntil(()->h.assertTrue(LodgingComplete.phase(s,b)==LodgingComplete.GATHER,"waiting for Vera to call down: "+comp(l,b)))
            .thenExecute(()->{
                has(h,p,"Vera: "+LodgingTexts.VERA_COME_DOWN[0]);has(h,q,"Vera: "+LodgingTexts.VERA_COME_DOWN[0]);
                h.assertTrue(own(p).getBoolean(LodgingComplete.CALLED)&&!own(q).getBoolean(LodgingComplete.CALLED),"only the one who promised is called");
                LodgingPeople.talk(p,who[LodgingPeople.DANIEL],own(p));h.assertTrue(Talk.choose(p,LodgingTexts.R_HOLD_DANIEL),"a reader can take his shoulders: "+Talk.replies(p));
                LodgingPeople.talk(q,who[LodgingPeople.DANIEL],own(q));h.assertTrue(Talk.choose(q,LodgingTexts.R_HOLD_DANIEL),"and the other: "+Talk.replies(q));
                h.assertTrue(own(p).getBoolean(LodgingComplete.HELD)&&own(q).getBoolean(LodgingComplete.HELD),"each by their own reply");
            })
            .thenWaitUntil(()->{
                // The readers keep their hands on him all the way.
                var d=who[LodgingPeople.DANIEL];
                if(LodgingComplete.phase(s,b)==LodgingComplete.GATHER){p.teleportTo(l,d.getX()+.6,d.getY(),d.getZ(),0,0);q.teleportTo(l,d.getX()-.6,d.getY(),d.getZ(),0,0);}
                else{f.at(p,P,-6.5,0,-26.5);f.at(q,P,-8.5,0,-26.0);}
                h.assertTrue(LodgingComplete.phase(s,b)==LodgingComplete.HOLDING&&LodgingStory.house(s,b).getCompound(LodgingComplete.STATE).getInt("Progress")>20,"waiting for him to be walked to his frame and held: "+comp(l,b)+", Daniel at "+rel(b,d));
            })
            .thenExecute(()->{
                h.assertTrue(who[LodgingPeople.DANIEL].gesture()==LodgingPeople.STRAIN,"he pulls against it");
                // Both let go and step back.
                LodgingPeople.talk(p,who[LodgingPeople.DANIEL],own(p));h.assertTrue(Talk.choose(p,LodgingTexts.R_LET_DANIEL_GO),"let go: "+Talk.replies(p));
                LodgingPeople.talk(q,who[LodgingPeople.DANIEL],own(q));h.assertTrue(Talk.choose(q,LodgingTexts.R_LET_DANIEL_GO),"and the other: "+Talk.replies(q));
                f.at(p,P,-5.5,0,-20.5);f.at(q,P,-6.5,0,-20.5);
            })
            .thenWaitUntil(()->h.assertTrue(LodgingComplete.phase(s,b)==LodgingComplete.BROKEN&&LodgingStory.house(s,b).getCompound(LodgingComplete.STATE).getBoolean("Out"),"waiting for him to pull out of it: "+comp(l,b)))
            .thenExecute(()->{
                h.assertTrue(who[LodgingPeople.DANIEL].isAlive()&&who[LodgingPeople.DANIEL].position().distanceTo(LodgingPeople.world(b,LodgingReturn.DANIEL_OUT))<.4,"he stands out of it: "+rel(b,who[LodgingPeople.DANIEL]));
                h.assertFalse(LodgingComplete.gone(s,b),"the room did not close");
                h.assertTrue(own(p).getBoolean(LodgingComplete.LET_GO),"the reader's own letting go");
            })
            .thenSucceed();
    }

    // ------------------------------------------------------------------------------------------------ 0.4.615: E3, sever the house
    private static String sev(ServerLevel l,BlockPos b){return LodgingStory.house(l.getServer(),b).getCompound(LodgingSever.STATE).toString();}
    private static String ahead(ServerLevel l,net.minecraft.world.entity.Entity e){var sb=new StringBuilder();var c=e.blockPosition();for(int dz=-1;dz<=1;dz++)for(int dx=-1;dx<=1;dx++)for(int y=0;y<=2;y++){var q=c.offset(dx,y,dz);var st=l.getBlockState(q);if(!st.getCollisionShape(l,q).isEmpty())sb.append(dx).append(",").append(y).append(",").append(dz).append("=").append(st.getBlock().getDescriptionId()).append(" ");}return sb.toString();}
    private static final BlockPos CATCH_SIDE=LodgingSite.DANIEL_FRAME.east();
    @GameTest(template="empty",batch="lodging_sever",timeoutTicks=6000)
    public static void twoReadersSeverTheHouseAndTheTwoMenSitOnTheBench(GameTestHelper h){
        var f=house(h);var p=f.player("lodging_sev_a",P);var q=f.player("lodging_sev_b",P);var r=f.player("lodging_sev_c",P);var l=f.level(P);var b=f.base(P);var s=l.getServer();
        f.chunks.hold(l,LodgingPeople.area(b));LodgingStory.hold(b,true);final LiteraryActor[] who=new LiteraryActor[LodgingPeople.COUNT];
        h.startSequence()
            .thenWaitUntil(()->{h.assertTrue(f.chunks.ready(),"the house is resident");LodgingPeople.ensure(l,b);
                for(int i=0;i<LodgingPeople.COUNT;i++){who[i]=LodgingPeople.find(l,b,i);h.assertTrue(who[i]!=null,"waiting for number "+i);}})
            .thenExecute(()->{
                // Daniel strapped in his frame, Dan in the hall. Two readers know the third way; the third does not.
                at(l,b,LodgingStory.AFTER,who);disturbed(l,b,LodgingMeeting.FRAME,false);
                place(who[LodgingPeople.DAN],b,1.5,-4.5,0);
                mark(p,LodgingCellar.KNOWS_THIRD);mark(q,LodgingCellar.KNOWS_THIRD);
                f.at(p,P,.5,LodgingSite.CELLAR,-26.5);f.at(q,P,-6.5,0,-27.0);f.at(r,P,-.5,LodgingSite.CELLAR,-26.5);
                LodgingStory.hold(b,false);
                // A reader who has not seen the dead post finds only a wheel.
                LodgingHouse.click(r,b,LodgingSite.RATCHET,own(r));
                h.assertTrue(LodgingSever.ratchet(s,b)==0&&!own(r).getBoolean(LodgingSever.RATCHET),"nothing happens for a reader who does not know: "+sev(l,b));
            })
            .thenWaitUntil(()->{
                // The two take turns, one at each: every lift after the first counts twice.
                LodgingHouse.click(p,b,LodgingSite.RATCHET,own(p));LodgingHouse.click(q,b,CATCH_SIDE,own(q));
                h.assertTrue(LodgingSever.severed(s,b),"waiting for both to come free: "+sev(l,b));
            })
            .thenExecute(()->{
                h.assertTrue(own(p).getBoolean(LodgingSever.RATCHET)&&own(q).getBoolean(LodgingSever.CATCH),"each worked their own");
                h.assertTrue(own(p).getBoolean(LodgingSever.SAW)&&own(r).getBoolean(LodgingSever.SAW),"every reader in the house felt it");
                h.assertTrue(LodgingKey.free(s,b),"Daniel's straps open");
                h.assertTrue(who[LodgingPeople.FAILED_DRESS].gesture()==LodgingSever.SAT,"the one at the hatch sits down");
                // Nobody can put Dan back now.
                mark(q,LodgingVera.TRUTH);LodgingPeople.talk(q,who[LodgingPeople.DAN],own(q));
                h.assertFalse(Talk.replies(q).contains(LodgingTexts.R_GO_BACK),"E1 is not offered: "+Talk.replies(q));Talk.close(q);
                f.at(p,P,.5,0,-3.5);f.at(r,P,-.5,0,-3.5);
            })
            .thenWaitUntil(()->{
                var shirt=who[LodgingPeople.FAILED_SHIRT];
                if(shirt.gesture()!=LodgingPeople.GRAB){h.assertTrue(false,"waiting for it to come for Daniel: "+sev(l,b)+", it at "+rel(b,shirt)+" ahead "+ahead(l,shirt)+", Daniel at "+rel(b,who[LodgingPeople.DANIEL]));}
            })
            .thenExecute(()->{
                var shirt=who[LodgingPeople.FAILED_SHIRT];f.at(q,P,1.5,0,-1.5);
                LodgingPeople.struck(q,shirt);h.assertTrue(shirt.gesture()==LodgingPeople.GRAB,"one blow is not enough");
                LodgingPeople.struck(q,shirt);h.assertTrue(shirt.gesture()==LodgingSever.SAT&&own(q).getBoolean(LodgingSever.STRUCK),"two, and it sits down");
            })
            .thenWaitUntil(()->h.assertTrue(LodgingSever.phase(s,b)==LodgingSever.DONE,"waiting for the two men on the bench: "+sev(l,b)+", Dan at "+rel(b,who[LodgingPeople.DAN])+", Daniel at "+rel(b,who[LodgingPeople.DANIEL])))
            .thenExecute(()->{
                h.assertTrue(ActorSeat.of(who[LodgingPeople.DAN])!=null&&ActorSeat.of(who[LodgingPeople.DANIEL])!=null,"both sat on the bench");
                h.assertTrue(who[LodgingPeople.DAN].gesture()==LodgingPeople.HURT&&who[LodgingPeople.DANIEL].gesture()==LodgingSever.RISE,"his hand held together; the other leaning to stand");
                h.assertTrue(own(p).getBoolean(LodgingSever.SAT_WITH),"the reader in the hall saw them there");
                has(h,p,"Daniel: "+LodgingTexts.DANIEL_SITS);
                h.assertTrue(LodgingTexts.account("x",own(q)).toString().length()>0,"the account is written");
            })
            .thenSucceed();
    }
    @GameTest(template="empty",batch="lodging_sever_alone",timeoutTicks=7000)
    public static void aReaderAloneSeversItSlowlyAndTheBodyGivesUp(GameTestHelper h){
        var f=house(h);var p=f.player("lodging_alone_a",P);var l=f.level(P);var b=f.base(P);var s=l.getServer();
        f.chunks.hold(l,LodgingPeople.area(b));LodgingStory.hold(b,true);final LiteraryActor[] who=new LiteraryActor[LodgingPeople.COUNT];
        h.startSequence()
            .thenWaitUntil(()->{h.assertTrue(f.chunks.ready(),"the house is resident");LodgingPeople.ensure(l,b);
                for(int i=0;i<LodgingPeople.COUNT;i++){who[i]=LodgingPeople.find(l,b,i);h.assertTrue(who[i]!=null,"waiting for number "+i);}})
            .thenExecute(()->{
                at(l,b,LodgingStory.AFTER,who);disturbed(l,b,LodgingMeeting.FRAME,false);
                mark(p,LodgingCellar.KNOWS_THIRD);f.at(p,P,.5,LodgingSite.CELLAR,-26.5);
                LodgingStory.hold(b,false);
                LodgingHouse.click(p,b,LodgingSite.RATCHET,own(p));
                h.assertTrue(LodgingSever.ratchet(s,b)==1,"alone, a tooth at a time: "+sev(l,b));
                LodgingHouse.click(p,b,LodgingSite.RATCHET,own(p));
                h.assertTrue(LodgingSever.ratchet(s,b)==1,"and not faster than a hand can: "+sev(l,b));
            })
            .thenWaitUntil(()->{LodgingHouse.click(p,b,LodgingSite.RATCHET,own(p));h.assertTrue(LodgingSever.ratchet(s,b)>=LodgingSever.STEPS,"waiting for the pawl to come clear: "+sev(l,b));})
            .thenExecute(()->{h.assertFalse(LodgingSever.severed(s,b),"one is not enough");f.at(p,P,-6.5,0,-27.0);})
            .thenWaitUntil(()->{LodgingHouse.click(p,b,CATCH_SIDE,own(p));h.assertTrue(LodgingSever.severed(s,b),"waiting for the catch: "+sev(l,b));})
            .thenExecute(()->f.at(p,P,.5,0,-3.5))
            .thenWaitUntil(()->h.assertTrue(LodgingSever.phase(s,b)==LodgingSever.DONE,"waiting for it to give up and the two to sit: "+sev(l,b)+", it at "+rel(b,who[LodgingPeople.FAILED_SHIRT])))
            .thenExecute(()->h.assertTrue(who[LodgingPeople.FAILED_SHIRT].gesture()==LodgingSever.SAT&&!own(p).getBoolean(LodgingSever.STRUCK),"nobody struck it; it sat down by itself"))
            .thenSucceed();
    }
    @AfterBatch(batch="lodging_sever") public static void severDone(ServerLevel l){done();}
    @AfterBatch(batch="lodging_sever_alone") public static void severAloneDone(ServerLevel l){done();}

    // ------------------------------------------------------------------------------------------------ 0.4.616: E4, neither Daniel
    private static String nei(ServerLevel l,BlockPos b){return LodgingStory.house(l.getServer(),b).getCompound(LodgingNeither.STATE).toString();}
    private static String bookText(net.minecraft.world.item.ItemStack book){var c=book.get(net.minecraft.core.component.DataComponents.WRITTEN_BOOK_CONTENT);return c==null?"":String.join(" ",c.pages().stream().map(pg->pg.raw().getString()).toList()).replace('\n',' ');}
    private static void knowsLink(ServerPlayer p){mark(p,LodgingInjury.SAW_DAN_CUT);mark(p,LodgingInjury.SAW_DANIEL_CUT);}
    private static int stage(ServerLevel l,BlockPos b,BlockPos cell){var st=l.getBlockState(b.offset(cell));return st.getBlock() instanceof LodgingThing?st.getValue(LodgingThing.STAGE):-1;}
    @GameTest(template="empty",batch="lodging_neither",timeoutTicks=6000)
    public static void readersWhoKnowTheLinkKillBothAndVeraLaysForTheNext(GameTestHelper h){
        var f=house(h);var p=f.player("lodging_nei_a",P);var q=f.player("lodging_nei_b",P);var r=f.player("lodging_nei_c",P);var l=f.level(P);var b=f.base(P);var s=l.getServer();
        f.chunks.hold(l,LodgingPeople.area(b));LodgingStory.hold(b,true);final LiteraryActor[] who=new LiteraryActor[LodgingPeople.COUNT];
        h.startSequence()
            .thenWaitUntil(()->{h.assertTrue(f.chunks.ready(),"the house is resident");LodgingPeople.ensure(l,b);
                for(int i=0;i<LodgingPeople.COUNT;i++){who[i]=LodgingPeople.find(l,b,i);h.assertTrue(who[i]!=null,"waiting for number "+i);}})
            .thenExecute(()->{
                at(l,b,LodgingStory.AFTER,who);disturbed(l,b,LodgingMeeting.FRAME,false);
                place(who[LodgingPeople.DAN],b,1.5,-4.5,0);
                knowsLink(p);knowsLink(q);
                f.at(p,P,.5,0,-3.5);f.at(q,P,-6.5,0,-26.0);f.at(r,P,6.5,0,-8.5);
                LodgingStory.hold(b,false);
                // A reader who does not know: nothing.
                LodgingPeople.struck(r,who[LodgingPeople.DAN]);
                h.assertTrue(LodgingNeither.blows(s,b)==0&&!own(r).getBoolean(LodgingNeither.STRUCK),"a blow from somebody who does not know does nothing: "+nei(l,b));
            })
            .thenWaitUntil(()->{
                // One at Dan in the hall, one at Daniel in his room: every blow lands on both.
                LodgingPeople.struck(p,who[LodgingPeople.DAN]);LodgingPeople.struck(q,who[LodgingPeople.DANIEL]);
                h.assertTrue(LodgingNeither.dead(s,b),"waiting for both to die: "+nei(l,b));
            })
            .thenExecute(()->{
                h.assertTrue(own(p).getBoolean(LodgingNeither.STRUCK)&&own(q).getBoolean(LodgingNeither.STRUCK),"each struck");
                h.assertTrue(own(r).getBoolean(LodgingNeither.SAW_DIE),"the one who did nothing heard it too");
                h.assertTrue(who[LodgingPeople.DAN].gesture()==LodgingNeither.DOWN,"Dan lies where he fell");
                h.assertTrue(who[LodgingPeople.DANIEL].gesture()==LodgingNeither.SLUMP,"Daniel hangs in the straps");
                has(h,p,"Vera: "+LodgingTexts.VERA_BACK);
                // Nobody can put Dan back now, or sever the house for the two of them.
                mark(q,LodgingVera.TRUTH);LodgingPeople.talk(q,who[LodgingPeople.DAN],own(q));
                h.assertFalse(Talk.replies(q).contains(LodgingTexts.R_GO_BACK),"E1 is not offered: "+Talk.replies(q));Talk.close(q);
                f.at(q,P,6.5,0,-3.5);
            })
            .thenWaitUntil(()->h.assertTrue(LodgingNeither.phase(s,b)==LodgingNeither.DONE,"waiting for the places and Rae: "+nei(l,b)+", Vera at "+rel(b,who[LodgingPeople.VERA])+", Rae at "+rel(b,who[LodgingPeople.RAE])))
            .thenExecute(()->{
                h.assertTrue((stage(l,b,LodgingNeither.PLACE_WEST)&2)!=0&&(stage(l,b,LodgingNeither.PLACE_EAST)&2)!=0,"two places laid");
                h.assertTrue(stage(l,b,LodgingNeither.RAE_CHAIR)==1,"one chair knocked over");
                h.assertTrue(own(r).getBoolean(LodgingNeither.SAW_PLACES),"the reader in the dining room saw them");
                has(h,r,"Rae: "+LodgingTexts.RAE_WHICH);
                // Tell Rae.
                LodgingPeople.talk(q,who[LodgingPeople.RAE],own(q));
                h.assertTrue(Talk.replies(q).contains(LodgingTexts.R_TELL_RAE),"Rae can be told: "+Talk.replies(q));
                Talk.choose(q,LodgingTexts.R_TELL_RAE);
                h.assertTrue(own(q).getBoolean(LodgingNeither.TOLD),"told");
                h.assertTrue(bookText(LodgingTexts.account("x",own(r))).contains("left the house to Vera"),"the one who did nothing left it to her");
            })
            .thenSucceed();
    }
    @GameTest(template="empty",batch="lodging_neither_stop",timeoutTicks=6000)
    public static void aBlowStopsVeraLayingAndTheSupportCanStillBeBroken(GameTestHelper h){
        var f=house(h);var p=f.player("lodging_neis_a",P);var l=f.level(P);var b=f.base(P);var s=l.getServer();
        f.chunks.hold(l,LodgingPeople.area(b));LodgingStory.hold(b,true);final LiteraryActor[] who=new LiteraryActor[LodgingPeople.COUNT];
        h.startSequence()
            .thenWaitUntil(()->{h.assertTrue(f.chunks.ready(),"the house is resident");LodgingPeople.ensure(l,b);
                for(int i=0;i<LodgingPeople.COUNT;i++){who[i]=LodgingPeople.find(l,b,i);h.assertTrue(who[i]!=null,"waiting for number "+i);}})
            .thenExecute(()->{
                at(l,b,LodgingStory.AFTER,who);disturbed(l,b,LodgingMeeting.FRAME,false);
                place(who[LodgingPeople.DAN],b,1.5,-4.5,0);
                mark(p,LodgingVera.TRUTH);mark(p,LodgingCellar.KNOWS_THIRD);f.at(p,P,.5,0,-3.5);
                LodgingStory.hold(b,false);
            })
            .thenWaitUntil(()->{LodgingPeople.struck(p,who[LodgingPeople.DAN]);h.assertTrue(LodgingNeither.dead(s,b),"waiting for both to die: "+nei(l,b));})
            .thenExecute(()->f.at(p,P,5.5,0,-6.5))
            .thenWaitUntil(()->h.assertTrue(LodgingNeither.phase(s,b)==LodgingNeither.LAYING&&LodgingNeither.laid(s,b)>=1,"waiting for the first place: "+nei(l,b)+", Vera at "+rel(b,who[LodgingPeople.VERA])))
            .thenExecute(()->{
                LodgingPeople.struck(p,who[LodgingPeople.VERA]);
                h.assertTrue(LodgingNeither.phase(s,b)==LodgingNeither.STOPPED&&own(p).getBoolean(LodgingNeither.STOPPED_HER),"she stops: "+nei(l,b));
                f.at(p,P,.5,LodgingSite.CELLAR,-26.5);
            })
            .thenIdle(80)
            .thenExecute(()->{
                h.assertTrue(LodgingNeither.laid(s,b)==1&&(stage(l,b,LodgingNeither.PLACE_EAST)&2)==0,"no second place: "+nei(l,b));
                h.assertTrue(stage(l,b,LodgingNeither.RAE_CHAIR)==0,"Rae never came to the table");
            })
            .thenWaitUntil(()->{LodgingHouse.click(p,b,LodgingSite.RATCHET,own(p));h.assertTrue(LodgingSever.ratchet(s,b)>=LodgingSever.STEPS,"waiting for the pawl: "+nei(l,b));})
            .thenExecute(()->f.at(p,P,-6.5,0,-27.0))
            .thenWaitUntil(()->{LodgingHouse.click(p,b,LodgingSite.DANIEL_FRAME.east(),own(p));h.assertTrue(LodgingNeither.phase(s,b)==LodgingNeither.BROKEN,"waiting for the support: "+nei(l,b));})
            .thenExecute(()->{
                h.assertTrue(who[LodgingPeople.VERA].gesture()==LodgingSever.SAT,"Vera sits down");
                h.assertTrue(own(p).getBoolean(LodgingNeither.BROKE),"the one who broke it");
                h.assertTrue(who[LodgingPeople.DAN].gesture()==LodgingNeither.DOWN,"Dan stays where he fell");
            })
            .thenSucceed();
    }
    @AfterBatch(batch="lodging_neither") public static void neitherDone(ServerLevel l){done();}
    @AfterBatch(batch="lodging_neither_stop") public static void neitherStopDone(ServerLevel l){done();}

    // ------------------------------------------------------------------------------------------------ 0.4.617: party changes
    private static String where(ServerLevel l,BlockPos b){return LodgingStory.house(l.getServer(),b).getString(LodgingKey.WHERE);}
    @GameTest(template="empty",batch="lodging_party",timeoutTicks=3000)
    public static void theKeyGoesBackWhereItLivesWhenItsHolderIsGone(GameTestHelper h){
        var f=house(h);var p=f.player("lodging_party_a",P);var q=f.player("lodging_party_b",P);var l=f.level(P);var b=f.base(P);var s=l.getServer();
        f.chunks.hold(l,LodgingPeople.area(b));LodgingStory.hold(b,true);final LiteraryActor[] who=new LiteraryActor[LodgingPeople.COUNT];
        h.startSequence()
            .thenWaitUntil(()->{h.assertTrue(f.chunks.ready(),"the house is resident");LodgingPeople.ensure(l,b);
                for(int i=0;i<LodgingPeople.COUNT;i++){who[i]=LodgingPeople.find(l,b,i);h.assertTrue(who[i]!=null,"waiting for number "+i);}})
            .thenExecute(()->{
                at(l,b,LodgingStory.AFTER,who);
                f.at(p,P,.5,0,-3.5);f.at(q,P,-.5,0,-3.5);
                // Rae gave it to the first reader.
                var house=LodgingStory.house(s,b);house.putString(LodgingKey.WHERE,p.getUUID().toString());LodgingParty.lives(house,LodgingKey.RAE);LodgingStory.save(s,b,house);
                p.getInventory().add(LodgingKey.stack(b));
                LodgingStory.hold(b,false);
            })
            .thenIdle(40)
            .thenExecute(()->{
                h.assertTrue(p.getUUID().toString().equals(where(l,b))&&LodgingParty.carries(p,b),"kept while its holder is in the house with it: "+where(l,b));
                // The holder walks out of the house with it.
                p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()+60.5,0,0);
            })
            .thenWaitUntil(()->h.assertTrue(LodgingKey.RAE.equals(where(l,b))&&!LodgingParty.carries(p,b),"waiting for it to go back to Rae's coat: "+where(l,b)))
            .thenExecute(()->{
                // Found in the cupboard by the second, who drops it on the hall floor.
                var house=LodgingStory.house(s,b);house.putString(LodgingKey.WHERE,q.getUUID().toString());LodgingParty.lives(house,LodgingKey.CUPBOARD);LodgingStory.save(s,b,house);
                q.drop(LodgingKey.stack(b),false);
            })
            .thenWaitUntil(()->h.assertTrue(LodgingKey.CUPBOARD.equals(where(l,b))&&l.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,LodgingPeople.area(b),e->LodgingKey.is(e.getItem(),b)).isEmpty(),
                "waiting for it to go back to the cupboard, and none left on the floor: "+where(l,b)))
            .thenExecute(()->{
                // A reader who logged off with it comes back carrying it: it is no longer theirs.
                p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-3.5,0,0);p.getInventory().add(LodgingKey.stack(b));
            })
            .thenWaitUntil(()->h.assertTrue(!LodgingParty.carries(p,b)&&LodgingKey.CUPBOARD.equals(where(l,b)),"waiting for the stale key to be taken: "+where(l,b)))
            .thenSucceed();
    }
    @GameTest(template="empty",batch="lodging_party_late",timeoutTicks=3000)
    public static void aLateReaderComesInByTheHallOrByTheScullery(GameTestHelper h){
        var f=house(h);var p=f.player("lodging_late_a",P);var q=f.player("lodging_late_b",P);var r=f.player("lodging_late_c",P);var l=f.level(P);var b=f.base(P);var s=l.getServer();
        f.chunks.hold(l,LodgingPeople.area(b));LodgingStory.hold(b,true);final LiteraryActor[] who=new LiteraryActor[LodgingPeople.COUNT];
        h.startSequence()
            .thenWaitUntil(()->{h.assertTrue(f.chunks.ready(),"the house is resident");LodgingPeople.ensure(l,b);
                for(int i=0;i<LodgingPeople.COUNT;i++){who[i]=LodgingPeople.find(l,b,i);h.assertTrue(who[i]!=null,"waiting for number "+i);}})
            .thenExecute(()->{
                at(l,b,LodgingStory.AFTER,who);var house=LodgingStory.house(s,b);house.remove(LodgingParty.READERS);LodgingStory.save(s,b,house);
                f.at(p,P,.5,0,-3.5);f.at(q,P,.5,0,-2.5);
                var a=own(p);a.remove(LodgingParty.ARRIVED);LodgingHouse.arrive(p,b,a);
                h.assertTrue(a.getInt(LodgingParty.LATE)==0,"the first reader is not late");
                var c=own(q);c.remove(LodgingParty.ARRIVED);LodgingHouse.arrive(q,b,c);
                h.assertTrue(c.getInt(LodgingParty.LATE)==LodgingParty.HALL,"the second, after Rae has begun, comes in at the hall");
                has(h,q,"Dan: "+LodgingTexts.DAN_LATE[0]);has(h,q,"Dan: "+LodgingTexts.DAN_LATE[1]);
                // Once the first attempt is under way, a third comes in by the scullery door.
                house=LodgingStory.house(s,b);house.putInt(LodgingMeeting.STAGE,LodgingMeeting.ATTEMPTING);LodgingStory.save(s,b,house);
                f.at(r,P,.5,0,-2.5);var e=own(r);e.remove(LodgingParty.ARRIVED);LodgingHouse.arrive(r,b,e);
                h.assertTrue(e.getInt(LodgingParty.LATE)==LodgingParty.SCULLERY,"in by the scullery door");
                var at=r.position().subtract(b.getX(),b.getY(),b.getZ());
                h.assertTrue(Math.abs(at.x-12.5)<.6&&Math.abs(at.z+23.5)<.6,"standing inside it: "+at);
                h.assertTrue(LodgingParty.readers(s,b).size()==3,"three have come in");
                // Coming in again is not arriving late again.
                LodgingHouse.arrive(q,b,c);h.assertTrue(LodgingParty.readers(s,b).size()==3,"nobody counted twice");
                house=LodgingStory.house(s,b);house.putInt(LodgingMeeting.STAGE,0);LodgingStory.save(s,b,house);
            })
            .thenSucceed();
    }
    @AfterBatch(batch="lodging_party") public static void partyDone(ServerLevel l){done();}
    @AfterBatch(batch="lodging_party_late") public static void partyLateDone(ServerLevel l){done();}

    // ------------------------------------------------------------------------------------------------ 0.4.618: the accounts and the Witness
    @GameTest(template="empty",batch="lodging_end",timeoutTicks=3000)
    public static void everyReaderInTheHouseBeforeItsEndHasAnAccountAndAnEndedHouseTakesNobody(GameTestHelper h){
        var f=house(h);var p=f.player("lodging_end_a",P);var q=f.player("lodging_end_b",P);var r=f.player("lodging_end_c",P);var l=f.level(P);var b=f.base(P);var s=l.getServer();
        f.chunks.hold(l,LodgingPeople.area(b));LodgingStory.hold(b,true);final LiteraryActor[] who=new LiteraryActor[LodgingPeople.COUNT];
        h.startSequence()
            .thenWaitUntil(()->{h.assertTrue(f.chunks.ready(),"the house is resident");LodgingPeople.ensure(l,b);
                for(int i=0;i<LodgingPeople.COUNT;i++){who[i]=LodgingPeople.find(l,b,i);h.assertTrue(who[i]!=null,"waiting for number "+i);}})
            .thenExecute(()->{
                at(l,b,LodgingStory.AFTER,who);var data=LabyrinthData.get(s);
                h.assertTrue(LodgingHouse.dealable(data,p.getUUID()),"dealt to a reader who has not had it");
                // Two readers come in; the house comes to E3's end; a third arrives after it.
                var house=LodgingStory.house(s,b);house.remove(LodgingParty.READERS);house.remove(LodgingEnd.ENDED);house.remove(LodgingEnd.PARTY);LodgingStory.save(s,b,house);
                f.at(p,P,.5,0,-3.5);f.at(q,P,-.5,0,-3.5);
                for(var x:new ServerPlayer[]{p,q,r}){var o=own(x);o.remove(LodgingParty.ARRIVED);o.remove("Ready");o.remove("Completed");LiteraryVignettes.save(data,x.getUUID(),P,o);}
                var a=own(p);LodgingHouse.arrive(p,b,a);LiteraryVignettes.save(data,p.getUUID(),P,a);
                var c=own(q);c.putBoolean(LodgingSever.RATCHET,true);c.putBoolean(LodgingSever.SAT_WITH,true);LodgingHouse.arrive(q,b,c);LiteraryVignettes.save(data,q.getUUID(),P,c);
                h.assertTrue(LodgingEnd.ended(s,b)==0&&!own(p).getBoolean("Ready"),"no account before an end");
                house=LodgingStory.house(s,b);var st=house.getCompound(LodgingSever.STATE);st.putInt("Phase",LodgingSever.DONE);house.put(LodgingSever.STATE,st);LodgingStory.save(s,b,house);
                LodgingStory.hold(b,false);
            })
            .thenWaitUntil(()->h.assertTrue(own(p).getBoolean("Ready")&&own(q).getBoolean("Ready"),"waiting for both readers' accounts: "+LodgingEnd.ended(s,b)))
            .thenExecute(()->{
                h.assertTrue(LodgingEnd.ended(s,b)==LodgingEnd.SEVERED&&own(p).getString("Outcome").equals("severed_the_house"),"the end and its outcome: "+own(p).getString("Outcome"));
                var book=bookText(LodgingTexts.account("q",own(q)));
                h.assertTrue(book.contains("You worked the wheel in the cellar loose")&&book.contains("neither could finish helping the other")&&!book.contains("Daniel came out of it"),"the account is the reader's own: "+book);
                h.assertTrue(book.indexOf("lodging house")<book.indexOf("wheel")&&book.endsWith("when you went out."),"in order, ending at the door");
                // One who comes in afterwards is not of the party: no account, and the ended house is closed to new readers.
                f.at(r,P,.5,0,-2.5);var e=own(r);LodgingHouse.arrive(r,b,e);LiteraryVignettes.save(LabyrinthData.get(s),r.getUUID(),P,e);
            })
            .thenIdle(40)
            .thenExecute(()->{
                h.assertTrue(!own(r).getBoolean("Ready"),"a reader who came after the end has no account");
                h.assertTrue(LodgingEnd.closed(s,SceneInstances.copyAt(s,P,b)),"an ended house takes nobody new");
                h.assertTrue(LodgingTexts.witness("severed_the_house").contains("let go of both")&&!LodgingTexts.witness("returned_daniel").equals(LodgingTexts.witness("neither_daniel")),"a Witness line for each end");
                var house=LodgingStory.house(s,b);house.remove(LodgingSever.STATE);house.remove(LodgingEnd.ENDED);house.remove(LodgingEnd.PARTY);LodgingStory.save(s,b,house);
            })
            .thenSucceed();
    }
    @AfterBatch(batch="lodging_end") public static void endDone(ServerLevel l){done();}

    // ------------------------------------------------------------------------------------------------ 0.4.619: personhood fragments
    @GameTest(template="empty",batch="lodging_self",timeoutTicks=3000)
    public static void whatTheHouseReadIsMadeReadyForThatReaderAloneAndTheUnreadGetTheNeutralHouse(GameTestHelper h){
        var f=house(h);var p=f.player("lodging_self_a",P);var q=f.player("lodging_self_b",P);var l=f.level(P);var b=f.base(P);var s=l.getServer();
        f.chunks.hold(l,LodgingPeople.area(b));LodgingStory.hold(b,true);final LiteraryActor[] who=new LiteraryActor[LodgingPeople.COUNT];
        h.startSequence()
            .thenWaitUntil(()->{h.assertTrue(f.chunks.ready(),"the house is resident");LodgingPeople.ensure(l,b);
                for(int i=0;i<LodgingPeople.COUNT;i++){who[i]=LodgingPeople.find(l,b,i);h.assertTrue(who[i]!=null,"waiting for number "+i);}})
            .thenExecute(()->{
                at(l,b,LodgingStory.AFTER,who);var data=LabyrinthData.get(s);
                // Every read value has its line; nothing read, nothing said.
                for(var v:List.of("HAVEN","DANGER","RUNAWAY"))h.assertTrue(!LodgingSelf.homeUpstairs(v).isEmpty(),"a landing for "+v);
                for(var v:List.of("ANALOG","TRANSITIONAL","DIGITAL"))h.assertTrue(!LodgingSelf.ageNightstand(v).isEmpty(),"a nightstand for "+v);
                for(var v:List.of("WRAPPED","BRAIDED_AND_OILED","BRUSHED_DRY","WASHED_AND_BARBERED"))h.assertTrue(!LodgingSelf.hairRoutine(v).isEmpty(),"a routine for "+v);
                h.assertTrue(LodgingSelf.homeUpstairs("").isEmpty()&&LodgingSelf.ageNightstand("").isEmpty()&&LodgingSelf.hairRoutine("").isEmpty()&&LodgingSelf.glass("").isEmpty()&&LodgingSelf.veraOffer("","").isEmpty(),"the unread get nothing");
                // One reader read, one not.
                f.at(p,P,.5,0,-3.5);f.at(q,P,-.5,0,-3.5);
                var a=own(p);var snap=new CompoundTag();snap.putString("Home","HAVEN");snap.putString("Age","ANALOG");snap.putString("Hair","WRAPPED");snap.putString("Drawn","NO_ONE");snap.putString("Glass","covered");
                a.put(LodgingSelf.SNAP,snap);for(var k:List.of(LodgingSelf.HOME_SHOWN,LodgingSelf.HAIR_SHOWN,LodgingSelf.GLASS_SHOWN,LodgingSelf.VERA_SHOWN,LodgingSelf.AGE_SHOWN,"WentUp"))a.remove(k);LiteraryVignettes.save(data,p.getUUID(),P,a);
                var c=own(q);c.put(LodgingSelf.SNAP,new CompoundTag());for(var k:List.of(LodgingSelf.HOME_SHOWN,LodgingSelf.GLASS_SHOWN,LodgingSelf.VERA_SHOWN,"WentUp"))c.remove(k);LiteraryVignettes.save(data,q.getUUID(),P,c);
                // Up the stair: the landing, for the read reader only.
                LodgingSelf.reader(p,b,own(p),true);LodgingSelf.reader(q,b,own(q),true);
                h.assertTrue(own(p).getBoolean(LodgingSelf.HOME_SHOWN)&&!own(q).getBoolean(LodgingSelf.HOME_SHOWN),"the landing is made ready for the read reader alone");
                // The hall glass: the read reader's own way; the other's as it always was.
                h.assertTrue(LodgingSelf.glass("covered").equals(LodgingSelf.hallGlass(p,own(p)))&&LodgingSelf.hallGlass(q,own(q))==null,"the hall glass as each left the coatroom's");
                // Vera's offer: the read reader hears rest and a room alone; the other only the script.
                LodgingSelf.offer(p,who[LodgingPeople.VERA],own(p));LodgingSelf.offer(q,who[LodgingPeople.VERA],own(q));
                has(h,p,"Vera: You could rest. That's all it would be. Somebody else to get up in the mornings.");
                has(h,p,"Vera: And nobody would ask anything of you. You'd have the room to yourself.");
                h.assertTrue(Talk.lines(q).stream().noneMatch(x->x.contains("You could rest")),"never said to the other reader");
                h.assertTrue(!own(q).getBoolean(LodgingSelf.VERA_SHOWN),"nothing recorded for the unread reader");
                // The routine, a hand on the body once awake.
                h.assertTrue(LodgingSelf.hairRoutine("WRAPPED").equals(LodgingSelf.touchedAwake(p,own(p)))&&LodgingSelf.touchedAwake(q,own(q))==null,"its hands try the read reader's routine");
                // The account says what each was shown, and nothing for the other.
                var mine=bookText(LodgingTexts.account("p",own(p)));var theirs=bookText(LodgingTexts.account("q",own(q)));
                h.assertTrue(mine.contains("getting it wrong")&&mine.contains("Vera made it sound like rest."),"the read reader's account: "+mine);
                h.assertTrue(!theirs.contains("getting it wrong")&&!theirs.contains("Vera made it sound"),"the unread reader's account has none of it: "+theirs);
                LodgingStory.hold(b,false);
            })
            .thenSucceed();
    }
    @AfterBatch(batch="lodging_self") public static void selfDone(ServerLevel l){done();}
    @AfterBatch(batch="lodging_complete") public static void completeDone(ServerLevel l){done();}
    @AfterBatch(batch="lodging_complete_held") public static void completeHeldDone(ServerLevel l){done();}
    @AfterBatch(batch="lodging_return") public static void returnDone(ServerLevel l){done();}
    @AfterBatch(batch="lodging_return_force") public static void returnForceDone(ServerLevel l){done();}
    @AfterBatch(batch="lodging_return_panic") public static void returnPanicDone(ServerLevel l){done();}
}
