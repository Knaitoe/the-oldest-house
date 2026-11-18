package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestSequence;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * 0.4.672, Fallow (docs/FALLOW_0_4_672.md), part 1: the station, its people and its things. Each case builds the place itself, one case
 * to a batch, as Last Seen's cases do.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_fallow") @PrefixGameTestTemplate(false)
public final class FallowTests {
    private FallowTests(){}
    private static final LabyrinthPlace P=LabyrinthPlace.FALLOW;
    private static LiteraryTests.Fixture active;
    private static LiteraryTests.Fixture place(GameTestHelper h){done();active=new LiteraryTests.Fixture(h);return active;}
    private static void done(){if(active!=null){active.close();active=null;}}
    private static boolean is(ServerLevel l,BlockPos b,BlockPos at,net.minecraft.world.level.block.Block block){return l.getBlockState(b.offset(at)).is(block);}
    private static boolean fitting(ServerLevel l,BlockPos b,BlockPos at,FallowBlock.Kind kind){return FallowBlock.is(l.getBlockState(b.offset(at)),kind);}
    private static int stage(ServerLevel l,BlockPos b,BlockPos at){return l.getBlockState(b.offset(at)).getValue(FallowBlock.STAGE);}
    private static boolean rubble(ServerLevel l,BlockPos b,BlockPos at,int stage){var s=l.getBlockState(b.offset(at));return FallowBlock.is(s,FallowBlock.Kind.RUBBLE)&&s.getValue(FallowBlock.STAGE)==stage;}
    private static List<String> told(ServerPlayer p){
        NativeTestPlayers.flush(p);
        var out=new ArrayList<String>();
        for(var packet:NativeTestPlayers.packets(p))if(packet instanceof net.minecraft.network.protocol.game.ClientboundSystemChatPacket chat)out.add(chat.content().getString());
        return out;
    }
    /** A hand on a thing, the reader stood at a spot of their own near it, as a client would send it. */
    private static void use(ServerPlayer p,BlockPos b,BlockPos rel,BlockPos stand){
        var at=b.offset(rel);
        p.moveTo(b.getX()+stand.getX()+.5,b.getY()+stand.getY(),b.getZ()+stand.getZ()+.5);p.setDeltaMovement(Vec3.ZERO);
        NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(p,InteractionHand.MAIN_HAND,at,new BlockHitResult(at.getCenter(),Direction.UP,at,false)));
    }
    private static final double[][] OFFSETS={{0,0},{.2,0},{-.2,0},{0,.2},{0,-.2}};
    /**
     * Where a body's feet rest in a cell, or NaN: on the floor under it, when there is room for a body somewhere in the cell beside what
     * stands in it (the counter's lifted flap, a curb along an edge); otherwise on top of what is in it, if that is no taller than a block.
     */
    private static double feet(ServerLevel l,BlockPos b,BlockPos cell){
        var at=b.offset(cell);var here=l.getBlockState(at).getCollisionShape(l,at);var below=l.getBlockState(at.below()).getCollisionShape(l,at.below());
        if(!below.isEmpty()){
            double f=at.getY()-1+below.max(Direction.Axis.Y);
            if(f>=at.getY()-.51&&f<=at.getY()+.01)for(var o:OFFSETS){
                var body=new AABB(at.getX()+.2+o[0],f+.01,at.getZ()+.2+o[1],at.getX()+.8+o[0],f+1.79,at.getZ()+.8+o[1]);
                if(l.noCollision(body))return f;
            }
        }
        if(here.isEmpty())return Double.NaN;
        double top=here.max(Direction.Axis.Y);if(top>1.01)return Double.NaN;
        double f=at.getY()+top;
        var body=new AABB(at.getX()+.2,f+.01,at.getZ()+.2,at.getX()+.8,f+1.79,at.getZ()+.8);
        return l.noCollision(body)?f:Double.NaN;
    }
    /**
     * Every cell a body can walk to from a start without opening anything, up or down a block at a time, within a box. Between two cells the
     * body's upper part must pass; anything below half a block it steps over, as a walking body does.
     */
    private static Set<BlockPos> walked(ServerLevel l,BlockPos b,BlockPos from,int x0,int z0,int x1,int z1){
        var seen=new HashSet<BlockPos>();var queue=new ArrayDeque<BlockPos>();seen.add(from);queue.add(from);
        while(!queue.isEmpty()){
            var at=queue.poll();double f=feet(l,b,at);if(Double.isNaN(f))continue;
            for(var d:Direction.Plane.HORIZONTAL)for(int dy=-1;dy<=1;dy++){
                var to=at.relative(d).above(dy);
                if(seen.contains(to)||to.getX()<x0||to.getX()>x1||to.getZ()<z0||to.getZ()>z1||to.getY()<-4||to.getY()>4)continue;
                double g=feet(l,b,to);if(Double.isNaN(g)||g-f>1.01)continue;
                var a=b.offset(at);var c=b.offset(to);double y=Math.max(f,g);
                var edge=new AABB((a.getX()+c.getX())/2.0+.2,y+.5,(a.getZ()+c.getZ())/2.0+.2,(a.getX()+c.getX())/2.0+.8,y+1.79,(a.getZ()+c.getZ())/2.0+.8);
                if(!l.noCollision(edge))continue;
                seen.add(to);queue.add(to);
            }
        }
        seen.removeIf(c->Double.isNaN(feet(l,b,c)));
        return seen;
    }
    private static String said(LiteraryActor a){return a.getCustomName()==null?"":a.getCustomName().getString();}
    private static void open(ServerLevel l,BlockPos b,BlockPos door,boolean open){var at=b.offset(door);l.setBlock(at,l.getBlockState(at).setValue(DoorBlock.OPEN,open),2);l.setBlock(at.above(),l.getBlockState(at.above()).setValue(DoorBlock.OPEN,open),2);}

    /**
     * The station, built of its own things. The store's boards on their block plinth, its floor, ceiling and roof, its name along the front;
     * the counter across the room with its register, its rack and its lifted flap, the note by the register, Harlan's stool; the shelves,
     * the coolers, the maps, the coffee and the strip lights. The forecourt's concrete, the island and its pumps, the canopy on its four
     * columns, the diesel pump, the sign. The road's two lanes, its line and its shoulders, the poles and their wires; the farmhouse across it
     * with its porch and the way on in its front wall; the two pads in the wheat, fenced, gated and lit; the garage and its lift; the yard,
     * the trailer and the clothesline; the cellar's doors over its steps; the rig. Wheat everywhere else, to the edge.
     *
     * A body walks from the House's door along behind the counter and out by its flap to the store's front, but not out of the shut front
     * door; then through it across the forecourt and the road, up the drive to the farmhouse porch, and round to the yard by the gap in its
     * fence; not into the locked trailer. It is late afternoon there; the House does not deal it in this part; the account's title fits.
     */
    @GameTest(template="empty",batch="fallow_place",timeoutTicks=1800)
    public static void theStationTheRoadAndTheWheat(GameTestHelper h){
        var f=place(h);var p=f.player("fallow_reader",P);var l=f.level(P);var b=f.base(P);
        h.startSequence().thenIdle(5).thenExecute(()->{
            // The store.
            h.assertTrue(is(l,b,new BlockPos(0,-1,-5),FallowRegistry.material("lino"))&&is(l,b,new BlockPos(FallowSite.STORE_W,0,-6),FallowRegistry.material("plinth"))
                &&is(l,b,new BlockPos(FallowSite.STORE_W,2,-6),FallowRegistry.material("siding"))&&is(l,b,new BlockPos(0,FallowSite.CEILING,-6),FallowRegistry.material("ceiling"))
                &&is(l,b,new BlockPos(0,FallowSite.CEILING+1,-6),FallowRegistry.material("roof")),"the store: its own floor, boards on a block plinth, its ceiling and roof");
            // Read from the forecourt, facing south, east is on the left: the first piece is the east end.
            for(int i=0;i<4;i++)h.assertTrue(fitting(l,b,new BlockPos(1-i,FallowSite.CEILING+2,FallowSite.STORE_N),FallowBlock.Kind.SIGNBAND)&&stage(l,b,new BlockPos(1-i,FallowSite.CEILING+2,FallowSite.STORE_N))==i,"the name along the front of the roof, read left to right from the forecourt, piece "+i);
            for(int x=FallowSite.STORE_W+1;x<=FallowSite.STORE_E-2;x++)h.assertTrue(fitting(l,b,new BlockPos(x,0,FallowSite.COUNTER_Z),FallowBlock.Kind.COUNTER),"the counter across the room at "+x);
            h.assertTrue(stage(l,b,FallowSite.ENDING)==1&&stage(l,b,new BlockPos(-5,0,FallowSite.COUNTER_Z))==3,"the register and the candy rack on it");
            h.assertTrue(fitting(l,b,new BlockPos(FallowSite.STORE_E-1,0,FallowSite.COUNTER_Z),FallowBlock.Kind.COUNTER)&&stage(l,b,new BlockPos(FallowSite.STORE_E-1,0,FallowSite.COUNTER_Z))==2,"its flap at the east end, lifted");
            h.assertTrue(fitting(l,b,FallowSite.NOTE,FallowBlock.Kind.NOTE)&&LiteraryRooms.source(P).equals(FallowSite.NOTE),"the note by the register is the first paper");
            h.assertTrue(fitting(l,b,FallowSite.STOOL,FallowBlock.Kind.STOOL)&&LiteraryRooms.ending(P).equals(FallowSite.ENDING),"Harlan's stool, and the register where the account will be");
            h.assertTrue(fitting(l,b,FallowSite.RADIO,FallowBlock.Kind.BACKSHELF)&&stage(l,b,FallowSite.RADIO)==2&&fitting(l,b,FallowSite.TV,FallowBlock.Kind.TV),"the radio on the back shelf, the television on its bracket");
            for(int z=-5;z>=-8;z--)h.assertTrue(fitting(l,b,new BlockPos(FallowSite.STORE_W+1,0,z),FallowBlock.Kind.COOLER),"a cooler at "+z);
            for(int z=FallowSite.GONDOLA_S;z>=FallowSite.GONDOLA_N;z--)h.assertTrue(fitting(l,b,new BlockPos(FallowSite.GONDOLA_X,0,z),FallowBlock.Kind.GONDOLA)&&stage(l,b,new BlockPos(FallowSite.GONDOLA_X,1,z))==3,"the shelving front to back, its low top, at "+z);
            h.assertTrue(stage(l,b,new BlockPos(FallowSite.STORE_W+1,1,-6))==1&&stage(l,b,new BlockPos(FallowSite.STORE_W+1,1,-7))==2&&stage(l,b,new BlockPos(FallowSite.STORE_W+1,1,-5))==3,"the cooler's panel: ICE, COLD, and plain over the other doors");
            // From just inside the front door the register is in plain sight down the aisle: nothing stands between.
            var eye=net.minecraft.world.phys.Vec3.atLowerCornerOf(b).add(.5,1.62,FallowSite.STORE_N+1.3);var till=net.minecraft.world.phys.Vec3.atCenterOf(b.offset(FallowSite.ENDING));
            var sight=l.clip(new net.minecraft.world.level.ClipContext(eye,till,net.minecraft.world.level.ClipContext.Block.VISUAL,net.minecraft.world.level.ClipContext.Fluid.NONE,net.minecraft.world.phys.shapes.CollisionContext.empty()));
            h.assertTrue(sight.getType()==net.minecraft.world.phys.HitResult.Type.MISS||sight.getBlockPos().getZ()-b.getZ()==FallowSite.COUNTER_Z,"the register seen from the front door: "+sight.getBlockPos().subtract(b).toShortString());
            h.assertTrue(fitting(l,b,new BlockPos(-3,0,FallowSite.STORE_N+1),FallowBlock.Kind.MAPS)
                &&fitting(l,b,new BlockPos(FallowSite.STORE_E-1,0,-9),FallowBlock.Kind.COFFEE)&&fitting(l,b,new BlockPos(-4,FallowSite.CEILING-1,-5),FallowBlock.Kind.TUBE),"the shelving, the maps, the coffee and the lights");
            h.assertTrue(fitting(l,b,new BlockPos(3,1,FallowSite.STORE_N),FallowBlock.Kind.WINDOW)&&l.getBlockState(b.offset(FallowSite.FRONT_DOOR)).getBlock() instanceof DoorBlock,"plate windows either side of the front door");
            // The forecourt.
            h.assertTrue(is(l,b,new BlockPos(0,-1,-17),FallowRegistry.material("apron"))&&fitting(l,b,new BlockPos(-2,0,FallowSite.ISLAND_Z),FallowBlock.Kind.PUMP)&&fitting(l,b,new BlockPos(1,1,FallowSite.ISLAND_Z),FallowBlock.Kind.PUMP),"the forecourt's concrete and the island's two pumps");
            h.assertTrue(fitting(l,b,new BlockPos(FallowSite.CANOPY_W+1,0,FallowSite.CANOPY_S-1),FallowBlock.Kind.CANOPY)&&stage(l,b,new BlockPos(FallowSite.CANOPY_W+1,FallowSite.CANOPY_Y-1,FallowSite.CANOPY_S-1))==6
                &&fitting(l,b,new BlockPos(0,FallowSite.CANOPY_Y,-17),FallowBlock.Kind.CANOPY),"the canopy on its columns, each column's top under the deck");
            h.assertTrue(fitting(l,b,new BlockPos(FallowSite.DRIVER_AT.getX()+2,0,FallowSite.ISLAND_Z),FallowBlock.Kind.PUMP)&&stage(l,b,new BlockPos(FallowSite.DRIVER_AT.getX()+2,0,FallowSite.ISLAND_Z))==2,"the diesel pump on its own island");
            h.assertTrue(stage(l,b,new BlockPos(-12,5,FallowSite.APRON_N))==1&&stage(l,b,new BlockPos(-12,6,FallowSite.APRON_N))==2,"the sign by the road: the prices, and the name over them");
            // The road, its poles and the field.
            h.assertTrue(is(l,b,new BlockPos(0,-1,FallowSite.ROAD_S),FallowRegistry.material("asphalt"))&&is(l,b,new BlockPos(0,-1,FallowSite.LINE_Z),FallowRegistry.material("road_line"))
                &&is(l,b,new BlockPos(3,-1,FallowSite.LINE_Z),FallowRegistry.material("asphalt"))&&is(l,b,new BlockPos(0,-1,FallowSite.SHOULDER_N),FallowRegistry.material("shoulder")),"two lanes of asphalt, a dashed line, gravel shoulders");
            h.assertTrue(fitting(l,b,new BlockPos(-48,FallowSite.POLE_TOP,FallowSite.POLES_Z),FallowBlock.Kind.POLE)&&stage(l,b,new BlockPos(-48,FallowSite.POLE_TOP,FallowSite.POLES_Z))==1
                &&stage(l,b,new BlockPos(-40,FallowSite.POLE_TOP,FallowSite.POLES_Z))==4,"telephone poles along the road, the wires sagging between them");
            for(var at:List.of(new BlockPos(FallowSite.WEST,0,-60),new BlockPos(FallowSite.EAST,0,-100),new BlockPos(40,0,-10),new BlockPos(0,0,FallowSite.NORTH)))
                h.assertTrue(fitting(l,b,at,FallowBlock.Kind.WHEAT)&&is(l,b,at.below(),FallowRegistry.material("soil")),"wheat to the edge, at "+at.toShortString());
            // The farmhouse and the way on.
            h.assertTrue(is(l,b,new BlockPos(FallowSite.FARM_W,2,FallowSite.FARM_S),FallowRegistry.material("farm_siding"))&&fitting(l,b,new BlockPos(FallowSite.FARM_W+2,1,FallowSite.FARM_S),FallowBlock.Kind.FARM)
                &&fitting(l,b,new BlockPos(FallowSite.FARM_DRIVE_X+2,0,FallowSite.SHOULDER_N-1),FallowBlock.Kind.FARM),"the farmhouse, its windows, the mailbox at the end of its drive");
            h.assertTrue(f.data().door(OnwardDoors.id(P))!=null||OnwardDoors.install(l,b,P),"the way on goes in where it was placed");
            h.assertTrue(l.getBlockState(b.offset(FallowSite.ONWARD)).getBlock() instanceof DoorBlock,"the farmhouse's front door is the way on: "+l.getBlockState(b.offset(FallowSite.ONWARD)));
            // The pads.
            for(var pad:List.of(FallowSite.PAD_A,FallowSite.PAD_B)){
                h.assertTrue(stage(l,b,pad.offset(0,0,FallowSite.PAD_HALF))==2&&fitting(l,b,pad.offset(-FallowSite.PAD_HALF,0,1),FallowBlock.Kind.PAD),"a pad's fence and its gate at "+pad.toShortString());
                h.assertTrue(stage(l,b,pad.below())==6&&stage(l,b,pad.offset(-FallowSite.LID_HALF,-1,0))==5,"its lid, seamed at the edge");
                h.assertTrue(stage(l,b,pad.offset(-FallowSite.PAD_HALF+1,4,-FallowSite.PAD_HALF+1))==4,"its light on a pole");
            }
            // The garage, the yard, the cellar, the rig.
            h.assertTrue(fitting(l,b,new BlockPos(FallowSite.BAY_W+1,0,-6),FallowBlock.Kind.LIFT)&&is(l,b,new BlockPos(FallowSite.GARAGE_E,2,-6),FallowRegistry.material("metal")),"the garage: the lift's posts in corrugated walls");
            h.assertTrue(is(l,b,new BlockPos(FallowSite.TRAILER_E,2,-7),FallowRegistry.material("trailer"))&&l.getBlockState(b.offset(FallowSite.TRAILER_DOOR)).getBlock() instanceof DoorBlock,"the trailer, its door");
            for(int z=FallowSite.WELL_S;z>=FallowSite.WELL_N;z--)for(int x=FallowSite.WELL_W;x<=FallowSite.WELL_E;x++)h.assertTrue(fitting(l,b,new BlockPos(x,0,z),FallowBlock.Kind.CELLAR),"the cellar doors shut at "+x+","+z);
            h.assertTrue(fitting(l,b,FallowSite.COT_HEAD,FallowBlock.Kind.COT)&&fitting(l,b,FallowSite.CROCK,FallowBlock.Kind.CROCK)&&fitting(l,b,FallowSite.LANTERN,FallowBlock.Kind.CROCK),"in the cellar, a cot, a crock and a lantern");
            h.assertTrue(fitting(l,b,new BlockPos(FallowSite.RIG_FRONT,0,FallowSite.RIG_Z),FallowBlock.Kind.RIG)&&fitting(l,b,new BlockPos(FallowSite.TRAILER_BACK,1,FallowSite.RIG_Z),FallowBlock.Kind.BOX),"the rig and its trailer by the diesel pump");
            // Walking. Inside: along behind the counter and out by its flap, but not out of the shut front door.
            var inside=walked(l,b,new BlockPos(0,0,-1),FallowSite.STORE_W,FallowSite.STORE_N-3,FallowSite.STORE_E,0);
            h.assertTrue(inside.contains(new BlockPos(6,0,-2))&&inside.contains(new BlockPos(FallowSite.STORE_E-1,0,FallowSite.COUNTER_Z))&&inside.contains(new BlockPos(0,0,-10)),"round the counter by its flap into the store: "+inside.size());
            h.assertFalse(inside.stream().anyMatch(c->c.getZ()<=FallowSite.STORE_N),"not through a shut door");
            // Out: across the forecourt and the road, up the drive to the porch, and round into the yard by the gap in its fence.
            open(l,b,FallowSite.FRONT_DOOR,true);
            var out=walked(l,b,new BlockPos(0,0,-10),FallowSite.WEST,FallowSite.FARM_N-4,FallowSite.EAST,0);
            h.assertTrue(out.contains(new BlockPos(0,0,-17))&&out.contains(new BlockPos(0,0,FallowSite.LINE_Z))&&out.contains(new BlockPos(0,0,FallowSite.SHOULDER_N)),"out of the door, across the forecourt and the road: "+out.size());
            h.assertTrue(out.contains(new BlockPos(FallowSite.FARM_DRIVE_X,0,FallowSite.FARM_S+1)),"up the drive to the farmhouse porch, in front of the way on");
            h.assertTrue(out.contains(new BlockPos(-16,0,-12))&&out.contains(new BlockPos(-19,0,-6)),"through the gap in the yard's fence, past the trailer");
            h.assertFalse(out.stream().anyMatch(c->c.getX()>FallowSite.TRAILER_W&&c.getX()<FallowSite.TRAILER_E&&c.getZ()<FallowSite.TRAILER_S&&c.getZ()>FallowSite.TRAILER_N),"not into the locked trailer");
            h.assertFalse(out.stream().anyMatch(c->c.getY()<-1),"not down into the cellar while its doors are shut");
            h.assertFalse(out.stream().anyMatch(c->c.getX()>FallowSite.PAD_A.getX()-FallowSite.PAD_HALF&&c.getX()<FallowSite.PAD_A.getX()+FallowSite.PAD_HALF&&c.getZ()>FallowSite.PAD_A.getZ()-FallowSite.PAD_HALF&&c.getZ()<FallowSite.PAD_A.getZ()+FallowSite.PAD_HALF),"not inside a pad's fence by its chained gate");
            open(l,b,FallowSite.FRONT_DOOR,false);
            // The hour, the dealing and the title.
            h.assertTrue(SceneClock.at(f.origin,b.offset(0,0,-20),HouseDimensions.OUTSIDE)==16,"late afternoon over the wheat");
            h.assertFalse(LiteraryVignettes.canDeal(f.data(),p.getUUID(),P)||Fallow.dealable(f.data(),java.util.UUID.randomUUID()),"the House deals it only from ten crossings down");
            h.assertTrue(LiteraryTexts.accountTitle(P).length()<=32,"the account's title fits a book: "+LiteraryTexts.accountTitle(P));
            var paper=LiteraryTexts.source(P).get(DataComponents.WRITTEN_BOOK_CONTENT);String text=String.join(" ",paper.pages().stream().map(pg->pg.raw().getString()).toList());
            h.assertTrue(text.contains("Ring the bell on the pump")&&text.contains("cellar")&&paper.pages().size()==4,"the note: where he is, what costs what, the cellar, the phone: "+text);
            done();h.succeed();
        });
    }

    /**
     * The five at their places, one each however often they are looked for: Harlan on his stool behind the register, Faye at the coffee, Opal
     * on the step out front at a child's height, Boyd in the garage by the car, Otis at his rig. The car is up on the lift. Each reader hears
     * each of them's first line for themselves, then the second, under their own names; a third time is the second again.
     */
    @GameTest(template="empty",batch="fallow_people",timeoutTicks=1800)
    public static void theFiveAtTheirPlacesEachWithTheirOwnLines(GameTestHelper h){
        var f=place(h);var l=f.level(P);var b=f.base(P);Fallow.forget(b);var p=f.player("fallow_people_reader",P);var peer=f.player("fallow_people_peer",P);
        // 0.4.673: the afternoon's clock is held back, so that everyone stays at their places and says their first lines.
        h.startSequence().thenIdle(5).thenExecute(()->{Fallow.forget(b);Fallow.ensure(l,b);}).thenWaitUntil(()->{
            // Run 1706: with the full suite loading round it, the five's entity section could come in after the one ensure above, and
            // nothing put them there afterwards. Each look puts anyone missing at their place, as the station's own clock does.
            Fallow.ensure(l,b);
            for(int who=0;who<Fallow.PEOPLE;who++)h.assertTrue(Fallow.find(l,b,who)!=null,"the "+who+"th is at their place");
            // 0.4.673 (the 0.4.672 release chain): the car is put up on the lift once its chunk's entities are in; with other cases
            // loading beside this one that was after the five were found, so it is waited for too.
            h.assertTrue(Fallow.car(l,b)!=null,"the car is up on the lift");
        }).thenExecute(()->{
            Fallow.forget(b);Fallow.ensure(l,b);
            var at=new BlockPos[]{FallowSite.KEEPER_AT,FallowSite.WOMAN_AT,FallowSite.GIRL_AT,FallowSite.MAN_AT,FallowSite.DRIVER_AT};
            for(int who=0;who<Fallow.PEOPLE;who++){
                int phase=Fallow.FIRST+who;
                var them=l.getEntitiesOfClass(LiteraryActor.class,new AABB(b.offset(at[who])).inflate(6),a->Fallow.is(a)&&a.phase()==phase);
                h.assertTrue(them.size()==1,"one of each, however often they are looked for: "+who+" "+them.size());
                h.assertTrue(them.get(0).blockPosition().equals(b.offset(at[who])),"at their own place: "+FallowTexts.NAMES[who]+" "+them.get(0).position());
            }
            var keeper=Fallow.find(l,b,Fallow.KEEPER);var seat=ActorSeat.of(keeper);
            h.assertTrue(seat!=null&&seat.seatHeight()==11,"Harlan sits on his stool: "+seat);
            var girl=Fallow.find(l,b,Fallow.GIRL);
            h.assertTrue(Math.abs(girl.getScale()-.68)<.01&&Math.abs(Fallow.find(l,b,Fallow.MAN).getScale()-1)<.01,"Opal at a child's height, the rest grown: "+girl.getScale());
            var cars=l.getEntitiesOfClass(TownCar.class,new AABB(BlockPos.containing(Vec3.atLowerCornerOf(b).add(FallowSite.CAR_AT))).inflate(3));
            h.assertTrue(cars.size()==1&&cars.get(0).getY()>b.getY()+1.5,"the car up on the lift, once: "+cars);
            // Each reader across from them, as a hand on them would come.
            var across=new BlockPos[]{new BlockPos(-1,0,-4),new BlockPos(5,0,-10),new BlockPos(3,0,-15),new BlockPos(13,0,-8),new BlockPos(12,0,-18)};
            for(int who=0;who<Fallow.PEOPLE;who++){
                var a=Fallow.find(l,b,who);var stand=across[who];
                f.at(p,P,stand.getX()+.5,0,stand.getZ()+.5);LiteraryVignettes.talk(p,a);h.assertTrue(said(a).equals(FallowTexts.line(who,0)),"the first reader hears "+FallowTexts.NAMES[who]+"'s first line: "+said(a));
                f.at(peer,P,stand.getX()+1.5,0,stand.getZ()+.5);LiteraryVignettes.talk(peer,a);h.assertTrue(said(a).equals(FallowTexts.line(who,0)),"and so does the second, for themselves: "+said(a));
                f.at(p,P,stand.getX()+.5,0,stand.getZ()+.5);LiteraryVignettes.talk(p,a);h.assertTrue(said(a).equals(FallowTexts.line(who,1))&&!FallowTexts.line(who,1).equals(FallowTexts.line(who,0)),"the first, asking again, hears the second: "+said(a));
                LiteraryVignettes.talk(p,a);h.assertTrue(said(a).equals(FallowTexts.line(who,1)),"and the second again after that");
                h.assertTrue(a.speaker().equals(FallowTexts.NAMES[who]),"spoken under their own name: "+a.speaker());
            }
            for(int who=0;who<Fallow.PEOPLE;who++)h.assertTrue(f.own(p,P).getInt("Heard"+who)==3&&f.own(peer,P).getInt("Heard"+who)==1,"each reader's own count of what they heard from "+FallowTexts.NAMES[who]);
            // Nothing they say names a colour or an age, or says how anybody feels.
            for(int who=0;who<Fallow.PEOPLE;who++)for(int n=0;n<2;n++){
                String line=FallowTexts.line(who,n).toLowerCase(java.util.Locale.ROOT);
                for(String word:List.of(" red "," blue "," green "," yellow "," years old"," afraid"," scared"," happy"," sad "))h.assertFalse(line.contains(word),"a line says nothing of colour, age or feeling: "+line);
            }
            done();h.succeed();
        });
    }

    /**
     * A hand on anything answers: the store's counter, register, flap and rack, the stool, the shelves and the radio, the television, the
     * coolers, the shelving, the maps, the coffee, the windows and the lights; the pumps, the island, the canopy and its column, the sign;
     * the road's pole (its crossarm and wires, and the sign's name, are over any hand's reach), the wheat; a pad's fence, its gate and its light; the garage's lift, bench and pegboard; the trailer,
     * its tank, its steps, the clothesline and the barrel; the farmhouse's window, porch and mailbox; the rig. The pump's bell rings for it.
     */
    @GameTest(template="empty",batch="fallow_things",timeoutTicks=1200)
    public static void aHandOnAnythingAnswers(GameTestHelper h){
        var f=place(h);var p=f.player("fallow_hand",P);var l=f.level(P);var b=f.base(P);
        h.startSequence().thenIdle(5).thenExecute(()->{
            var front=new BlockPos(0,0,-5);var behind=new BlockPos(1,0,-2);var court=new BlockPos(0,0,-16);
            record Touch(BlockPos at,BlockPos stand){}
            var touches=List.of(
                new Touch(new BlockPos(0,0,FallowSite.COUNTER_Z),front),new Touch(FallowSite.ENDING,front),new Touch(new BlockPos(FallowSite.STORE_E-1,0,FallowSite.COUNTER_Z),new BlockPos(6,0,-5)),
                new Touch(new BlockPos(-5,0,FallowSite.COUNTER_Z),new BlockPos(-5,0,-5)),new Touch(FallowSite.STOOL,behind),
                new Touch(new BlockPos(-5,0,FallowSite.STORE_S-1),new BlockPos(-5,0,-2)),new Touch(new BlockPos(-6,1,FallowSite.STORE_S-1),new BlockPos(-6,0,-2)),new Touch(FallowSite.RADIO,new BlockPos(-4,0,-2)),
                new Touch(FallowSite.TV,new BlockPos(5,0,-2)),new Touch(new BlockPos(FallowSite.STORE_W+1,0,-6),new BlockPos(-5,0,-6)),new Touch(new BlockPos(FallowSite.STORE_W+1,1,-6),new BlockPos(-5,0,-6)),
                new Touch(new BlockPos(-3,0,-6),new BlockPos(-1,0,-6)),new Touch(new BlockPos(-3,0,-7),new BlockPos(-1,0,-7)),new Touch(new BlockPos(-3,0,-8),new BlockPos(-1,0,-8)),new Touch(new BlockPos(-3,1,-8),new BlockPos(-1,0,-8)),
                new Touch(new BlockPos(FallowSite.STORE_W+1,1,-7),new BlockPos(-5,0,-7)),new Touch(new BlockPos(FallowSite.STORE_W+1,1,-5),new BlockPos(-5,0,-5)),
                new Touch(new BlockPos(-3,0,FallowSite.STORE_N+1),new BlockPos(-1,0,-10)),new Touch(new BlockPos(-3,1,FallowSite.STORE_N+1),new BlockPos(-1,0,-10)),
                new Touch(new BlockPos(FallowSite.STORE_E-1,0,-9),new BlockPos(6,0,-10)),new Touch(new BlockPos(3,1,FallowSite.STORE_N),new BlockPos(3,0,-10)),new Touch(new BlockPos(3,2,FallowSite.STORE_N),new BlockPos(3,0,-10)),
                new Touch(new BlockPos(-4,FallowSite.CEILING-1,-5),new BlockPos(-4,0,-6)),
                new Touch(new BlockPos(-2,0,FallowSite.ISLAND_Z),court),new Touch(new BlockPos(-2,1,FallowSite.ISLAND_Z),court),new Touch(new BlockPos(0,0,FallowSite.ISLAND_Z),court),new Touch(new BlockPos(-3,0,FallowSite.ISLAND_Z),court),
                new Touch(new BlockPos(FallowSite.DRIVER_AT.getX()+2,0,FallowSite.ISLAND_Z),new BlockPos(14,0,-17)),new Touch(new BlockPos(FallowSite.DRIVER_AT.getX()+2,1,FallowSite.ISLAND_Z),new BlockPos(14,0,-17)),
                new Touch(new BlockPos(-1,FallowSite.CANOPY_Y,-16),new BlockPos(-1,0,-16)),new Touch(new BlockPos(0,FallowSite.CANOPY_Y,FallowSite.CANOPY_S),new BlockPos(0,0,-14)),new Touch(new BlockPos(0,FallowSite.CANOPY_Y,-17),new BlockPos(0,0,-17)),
                new Touch(new BlockPos(FallowSite.CANOPY_W+1,1,FallowSite.CANOPY_S-1),new BlockPos(-4,0,-16)),new Touch(new BlockPos(FallowSite.CANOPY_W+1,0,FallowSite.CANOPY_S-1),new BlockPos(-4,0,-16)),
                new Touch(new BlockPos(-12,0,FallowSite.APRON_N),new BlockPos(-12,0,-25)),new Touch(new BlockPos(-12,5,FallowSite.APRON_N),new BlockPos(-12,0,-25)),
                new Touch(new BlockPos(-48,0,FallowSite.POLES_Z),new BlockPos(-48,0,FallowSite.SHOULDER_N)),
                new Touch(FallowSite.PAD_A.offset(-FallowSite.PAD_HALF,0,1),FallowSite.PAD_A.offset(-FallowSite.PAD_HALF-2,0,1)),new Touch(FallowSite.PAD_A.offset(-FallowSite.PAD_HALF,0,0),FallowSite.PAD_A.offset(-FallowSite.PAD_HALF-2,0,0)),
                new Touch(FallowSite.PAD_A.offset(0,0,FallowSite.PAD_HALF),FallowSite.PAD_A.offset(0,0,FallowSite.PAD_HALF+2)),
                new Touch(FallowSite.PAD_A.offset(-FallowSite.PAD_HALF+1,0,-FallowSite.PAD_HALF+1),FallowSite.PAD_A.offset(-FallowSite.PAD_HALF-1,0,-FallowSite.PAD_HALF+1)),
                new Touch(FallowSite.PAD_A.offset(-FallowSite.PAD_HALF+1,4,-FallowSite.PAD_HALF+1),FallowSite.PAD_A.offset(-FallowSite.PAD_HALF-1,0,-FallowSite.PAD_HALF+1)),
                new Touch(new BlockPos(FallowSite.BAY_W+1,0,-6),new BlockPos(13,0,-4)),new Touch(new BlockPos(FallowSite.BAY_W+1,2,-6),new BlockPos(13,0,-4)),new Touch(new BlockPos(FallowSite.BAY_W+2,1,-6),new BlockPos(13,0,-4)),
                new Touch(new BlockPos(FallowSite.GARAGE_E-1,0,-3),new BlockPos(15,0,-3)),new Touch(new BlockPos(FallowSite.GARAGE_E-1,1,-3),new BlockPos(15,0,-3)),
                new Touch(new BlockPos(FallowSite.GARAGE_E-1,0,-9),new BlockPos(15,0,-9)),new Touch(new BlockPos(FallowSite.GARAGE_E-1,0,-10),new BlockPos(15,0,-10)),
                new Touch(new BlockPos(FallowSite.TRAILER_E,2,-5),new BlockPos(-19,0,-5)),new Touch(new BlockPos(-22,0,FallowSite.TRAILER_N-1),new BlockPos(-22,0,-17)),
                new Touch(new BlockPos(FallowSite.TRAILER_E+1,0,-4),new BlockPos(-18,0,-4)),new Touch(new BlockPos(FallowSite.TRAILER_E+1,0,FallowSite.TRAILER_DOOR.getZ()),new BlockPos(-18,0,-8)),
                new Touch(new BlockPos(-18,0,-3),new BlockPos(-18,0,-5)),new Touch(new BlockPos(-15,1,-3),new BlockPos(-15,0,-5)),new Touch(new BlockPos(FallowSite.STORE_W-1,0,-2),new BlockPos(-11,0,-2)),
                new Touch(new BlockPos(-20,0,FallowSite.YARD_N),new BlockPos(-20,0,-17)),
                new Touch(new BlockPos(FallowSite.FARM_W+2,1,FallowSite.FARM_S),new BlockPos(FallowSite.FARM_W+2,0,FallowSite.FARM_S+1)),new Touch(new BlockPos(FallowSite.FARM_W,0,FallowSite.FARM_S+2),new BlockPos(FallowSite.FARM_W+1,0,FallowSite.FARM_S+1)),
                new Touch(new BlockPos(FallowSite.FARM_W+1,0,FallowSite.FARM_S+2),new BlockPos(FallowSite.FARM_W+1,0,FallowSite.FARM_S+1)),new Touch(new BlockPos(FallowSite.FARM_DRIVE_X+2,0,FallowSite.SHOULDER_N-1),new BlockPos(FallowSite.FARM_DRIVE_X,0,FallowSite.SHOULDER_N-1)),
                new Touch(new BlockPos(FallowSite.RIG_FRONT,0,FallowSite.RIG_Z),new BlockPos(12,0,-21)),new Touch(new BlockPos(FallowSite.RIG_FRONT+1,1,FallowSite.RIG_Z),new BlockPos(15,0,-19)),
                new Touch(new BlockPos(FallowSite.TRAILER_BACK,0,FallowSite.RIG_Z),new BlockPos(27,0,-21)),new Touch(new BlockPos(FallowSite.TRAILER_BACK,1,FallowSite.RIG_Z),new BlockPos(27,0,-21)));
            var wheat=new BlockPos(30,0,-60);touches=new ArrayList<>(touches);touches.add(new Touch(wheat,wheat.offset(1,0,0)));
            told(p);
            var expected=new ArrayList<String>();
            for(var t:touches){
                var st=l.getBlockState(b.offset(t.at()));
                h.assertTrue(FallowBlock.is(st),"a station fitting at "+t.at().toShortString()+": "+st);
                String line=FallowTexts.thing(st.getValue(FallowBlock.KIND),st.getValue(FallowBlock.STAGE));
                h.assertTrue(line!=null,"it has words for a hand: "+t.at().toShortString());
                expected.add(line);use(p,b,t.at(),t.stand());
            }
            var why=told(p);
            for(int i=0;i<expected.size();i++)h.assertTrue(why.contains(expected.get(i)),"each says what it is: missing \""+expected.get(i)+"\" at "+touches.get(i).at().toShortString()+" in "+why);
            // The trailer's door is locked and says so; the note is the House's own paper, not a line.
            use(p,b,FallowSite.TRAILER_DOOR,FallowSite.TRAILER_DOOR.offset(2,-1,0));
            h.assertTrue(told(p).contains(FallowTexts.TRAILER_DOOR)&&!l.getBlockState(b.offset(FallowSite.TRAILER_DOOR)).getValue(DoorBlock.OPEN),"the trailer's door is locked and stays shut");
            h.assertTrue(FallowTexts.thing(FallowBlock.Kind.NOTE,0)==null,"the note answers as a paper");
            // Nothing a thing says names an age or how anybody feels.
            for(var kind:FallowBlock.Kind.values())for(int s=0;s<8;s++){
                String line=FallowTexts.thing(kind,s);if(line==null)continue;line=" "+line.toLowerCase(java.util.Locale.ROOT)+" ";
                for(String word:List.of(" years old"," afraid"," scared"," happy"," lonely"," uneasy"," eerie"," feels "))h.assertFalse(line.contains(word),"a thing's words say nothing of age or feeling: "+line);
            }
            done();h.succeed();
        });
    }

    /**
     * The cellar's doors: a hand throws both leaves back, all six lengths together, with a sound and a line; then a body walks down the three
     * steps into the cellar and stands in it with room over its head; a hand shuts them again and the way down is covered. A spectator's hand
     * opens nothing.
     */
    @GameTest(template="empty",batch="fallow_cellar",timeoutTicks=1200)
    public static void theCellarDoorsOpenOntoStepsAReaderCanWalkDown(GameTestHelper h){
        var f=place(h);var p=f.player("fallow_cellar_reader",P);var watcher=f.player("fallow_cellar_watcher",P);var l=f.level(P);var b=f.base(P);
        h.startSequence().thenIdle(5).thenExecute(()->{
            var yard=new BlockPos(FallowSite.WELL_W,0,FallowSite.WELL_S+1);var door=new BlockPos(FallowSite.WELL_W,0,FallowSite.WELL_S);
            // A spectator first: nothing moves.
            watcher.setGameMode(GameType.SPECTATOR);told(watcher);
            use(watcher,b,door,yard);
            h.assertFalse(Fallow.cellarOpen(l,b),"a spectator's hand opens nothing");
            // A reader's hand throws them back.
            told(p);use(p,b,door,yard);
            h.assertTrue(Fallow.cellarOpen(l,b)&&told(p).contains(FallowTexts.CELLAR_OPENED),"the doors swing back, and the reader is told so");
            for(int z=FallowSite.WELL_S;z>=FallowSite.WELL_N;z--)for(int x=FallowSite.WELL_W;x<=FallowSite.WELL_E;x++)h.assertTrue(fitting(l,b,new BlockPos(x,0,z),FallowBlock.Kind.CELLAR_OPEN),"every length thrown back, at "+x+","+z);
            // Down the steps into the cellar.
            var down=walked(l,b,yard,FallowSite.CELLAR_W,FallowSite.CELLAR_N,FallowSite.CELLAR_E,0);
            h.assertTrue(down.contains(new BlockPos(FallowSite.WELL_W,FallowSite.CELLAR_FLOOR+1,FallowSite.CELLAR_S-1))&&down.contains(new BlockPos(FallowSite.CELLAR_E-2,FallowSite.CELLAR_FLOOR+1,-15)),"down the steps and across the cellar's floor: "+down.size());
            for(int y=FallowSite.CELLAR_FLOOR+1;y<=-2;y++)h.assertTrue(l.getBlockState(b.offset(FallowSite.WELL_W,y,FallowSite.CELLAR_S)).isAir(),"the gap from the steps into the room at "+y);
            var floor=b.offset(FallowSite.WELL_W,FallowSite.CELLAR_FLOOR+1,FallowSite.WELL_N);
            h.assertTrue(l.noCollision(new AABB(floor.getX()+.2,floor.getY()+.01,floor.getZ()-.6,floor.getX()+.8,floor.getY()+1.79,floor.getZ()+.8)),"off the last step into the room with room over the head");
            // And shut again.
            use(p,b,door,yard);
            h.assertFalse(Fallow.cellarOpen(l,b),"a second hand brings them down");
            h.assertTrue(told(p).contains(FallowTexts.CELLAR_SHUT),"and says so");
            var shut=walked(l,b,yard,FallowSite.CELLAR_W,FallowSite.CELLAR_N,FallowSite.CELLAR_E,0);
            h.assertFalse(shut.stream().anyMatch(c->c.getY()<-1),"the way down is covered while they are shut");
            done();h.succeed();
        });
    }

    // ------------------------------------------------------------------------------------------------ 0.4.673, the afternoon
    /**
     * The chores and the register. Three readers in the store: one the House has read as young, two it has not read. Harlan gives each their
     * own chore, said to them: the young one keeps Opal company, the first unread one minds the till, and the second unread one, with both
     * chores given, minds the till too. Then he gets down off his stool and walks round to the car, opening the garage door, and works under
     * it with his wrench. Faye comes for milk; the reader behind the counter rings her up, and she thanks them. Otis comes for coffee and
     * diesel; nobody rings him up, and after his wait he leaves the money by the register. Each reader's chore and what they rang up is their
     * own; spectators are given nothing.
     */
    @GameTest(template="empty",batch="fallow_afternoon",timeoutTicks=6000)
    public static void theChoresAndTheRegister(GameTestHelper h){
        var f=place(h);var l=f.level(P);var b=f.base(P);Fallow.forget(b);
        var p=f.player("fallow_till",P);var q=f.player("fallow_young",P);var r=f.player("fallow_third",P);
        HouseRead.signal(q,HouseRead.Trait.AGE,"DIGITAL",false);HouseRead.signal(q,HouseRead.Trait.AGE,"DIGITAL",true);
        int[] keeperWas={0};
        h.startSequence().thenIdle(5).thenExecute(()->{
            h.assertTrue(Fallow.clock(b)>=0,"an afternoon begins when a reader comes in");
            // The third out of his hearing at first, so that the other two have theirs before them.
            f.at(p,P,2.5,0,-2.5);f.at(q,P,2.5,0,-6.5);f.at(r,P,4.5,0,-11.5);
            Fallow.skipTo(b,Fallow.CHORES);
        }).thenWaitUntil(()->{
            for(var x:List.of(p,q))h.assertTrue(Fallow.chore(b,x.getUUID())!=null,"Harlan has given "+x.getGameProfile().getName()+" a chore: "+Fallow.clock(b));
        }).thenExecute(()->{
            h.assertTrue(Fallow.chore(b,r.getUUID())==null&&Fallow.keeper(b)==Fallow.STOOL,"nobody out of his hearing is given one, and he waits for them");
            f.at(r,P,4.5,0,-6.5);
        }).thenWaitUntil(()->h.assertTrue(Fallow.chore(b,r.getUUID())!=null,"come nearer, the third is given theirs: "+Fallow.clock(b)))
        .thenExecute(()->{
            h.assertTrue(Fallow.TILL.equals(Fallow.chore(b,p.getUUID()))&&Fallow.TILL.equals(f.own(p,P).getString("Chore")),"one the House has not read minds the till");
            h.assertTrue(Fallow.GIRL_CHORE.equals(Fallow.chore(b,q.getUUID()))&&Fallow.GIRL_CHORE.equals(f.own(q,P).getString("Chore")),"one it reads as young keeps Opal company");
            h.assertTrue(Fallow.TILL.equals(Fallow.chore(b,r.getUUID())),"a third, with both chores given, gets the one the House's read gives them");
            var keeper=Fallow.find(l,b,Fallow.KEEPER);
            h.assertTrue(List.of(FallowTexts.TILL).contains(said(keeper))||List.of(FallowTexts.COMPANY).contains(said(keeper)),"a chore is said aloud, in his own words: "+said(keeper));
            h.assertTrue(Fallow.keeper(b)==Fallow.STOOL,"he gives the chores from his stool");
        }).thenWaitUntil(()->h.assertTrue(Fallow.keeper(b)==Fallow.UNDER,"Harlan gets down and goes round to the car: "+Fallow.keeper(b)+" at "+Fallow.find(l,b,Fallow.KEEPER).position().subtract(Vec3.atLowerCornerOf(b))))
        .thenExecute(()->{
            var keeper=Fallow.find(l,b,Fallow.KEEPER);var at=keeper.position().subtract(Vec3.atLowerCornerOf(b));
            h.assertTrue(at.distanceTo(Fallow.UNDER_CAR)<1.2,"he is under the car: "+at);
            h.assertTrue(keeper.gesture()==Fallow.WORK&&!keeper.getMainHandItem().isEmpty(),"working up into it, a wrench in his hand: "+keeper.gesture());
            h.assertTrue(l.getBlockState(b.offset(FallowSite.GARAGE_DOOR)).getValue(DoorBlock.OPEN),"he opened the garage door on his way");
            h.assertTrue(ActorSeat.of(keeper)==null,"off his stool");
            keeperWas[0]=1;
            Fallow.skipTo(b,Fallow.FAYE_TIME);
        }).thenWaitUntil(()->h.assertTrue(Fallow.customer(b)==Fallow.WOMAN,"Faye comes to the register with her milk: "+Fallow.customer(b)))
        .thenExecute(()->{
            var faye=Fallow.find(l,b,Fallow.WOMAN);
            h.assertTrue(faye.getMainHandItem().is(io.github.knaitoe.theoldesthouse.labyrinth.FallowRegistry.MILK.get()),"she carries a quart of milk");
            h.assertTrue(said(faye).equals(FallowTexts.CUSTOMER[0][0]),"and asks for it: "+said(faye));
            // A reader in front of the counter only looks at the register; the reader behind it rings her up.
            told(q);use(q,b,FallowSite.ENDING,new BlockPos(1,0,-5));
            h.assertTrue(Fallow.customer(b)==Fallow.WOMAN&&!told(q).contains(FallowTexts.RING),"from in front of the counter, nothing is rung up");
            told(p);f.click(p,P,FallowSite.ENDING);
            h.assertTrue(told(p).contains(FallowTexts.RING),"the reader behind the counter rings her up");
            h.assertTrue(said(faye).equals(FallowTexts.CUSTOMER[0][1]),"and she thanks them: "+said(faye));
            h.assertTrue(f.own(p,P).getInt("Rang")==1&&f.own(q,P).getInt("Rang")==0&&f.own(r,P).getInt("Rang")==0,"what each reader rang up is their own");
            h.assertTrue((Fallow.served(b)&1)!=0,"Faye has paid");
            told(p);f.click(p,P,FallowSite.ENDING);
            h.assertTrue(told(p).contains(FallowTexts.RING_NOTHING)&&f.own(p,P).getInt("Rang")==1,"rung again with nobody waiting, the drawer opens on nothing");
            Fallow.skipTo(b,Fallow.OTIS_TIME);
        }).thenWaitUntil(()->h.assertTrue(Fallow.customer(b)==Fallow.DRIVER,"Otis comes in with his coffee: "+Fallow.customer(b)))
        .thenExecute(()->{
            var otis=Fallow.find(l,b,Fallow.DRIVER);
            h.assertTrue(otis.getMainHandItem().is(io.github.knaitoe.theoldesthouse.labyrinth.FallowRegistry.COFFEE.get()),"a cup from the machine in his hand");
            told(p);
        }).thenWaitUntil(()->h.assertTrue((Fallow.served(b)&2)!=0,"with nobody ringing him up, Otis waits, then pays anyway"))
        .thenExecute(()->{
            var otis=Fallow.find(l,b,Fallow.DRIVER);
            h.assertTrue(said(otis).equals(FallowTexts.CUSTOMER[1][2]),"and leaves the money by the register: "+said(otis));
            h.assertTrue(told(p).contains(FallowTexts.COINS_LEFT),"the coins are heard on the counter in the store");
            h.assertTrue(f.own(p,P).getInt("Rang")==1,"nothing rung up that was not");
            // Nothing they say names a colour or an age, or says how anybody feels.
            var lines=new ArrayList<String>(List.of(FallowTexts.TILL));lines.addAll(List.of(FallowTexts.COMPANY));lines.addAll(List.of(FallowTexts.BEAN_TALK));
            for(var c:FallowTexts.CUSTOMER)lines.addAll(List.of(c));for(var c:FallowTexts.AT_THE_CAR)lines.add(c[1]);
            lines.addAll(List.of(FallowTexts.ASK_BEAN,FallowTexts.HAND_BEAN,FallowTexts.ASKS_NAME,FallowTexts.NAMES_KEEP,FallowTexts.ASKS_TIME,FallowTexts.ANSWERED,FallowTexts.UNANSWERED,FallowTexts.RING,FallowTexts.RING_NOTHING,FallowTexts.COINS_LEFT));
            for(int stage=0;stage<4;stage++)lines.add(FallowTexts.thing(FallowBlock.Kind.BEAN,stage));
            for(String line:lines){String low=" "+line.toLowerCase(java.util.Locale.ROOT)+" ";
                for(String word:List.of(" red "," blue "," green "," yellow "," years old"," afraid"," scared"," happy"," sad "," angry"))h.assertFalse(low.contains(word),"a line says nothing of colour, age or feeling: "+line);}
            done();h.succeed();
        });
    }

    /**
     * Opal's bean and the man on the road. Harlan goes under the car, and Opal goes to him for her bean, which is in the car; he hands it
     * down and she carries it out front in both hands and sets it down in the sun, and kneels by it. A reader who stays by her keeps her
     * company and has the time kept. Then a man nobody knows comes walking along the road out of the haze, made where nobody sees him come
     * to be: he takes a bottle of pop from the cooler and pays for it, goes out to Opal and asks her name, and she does not tell him. He asks
     * the readers near him the time; one tells him, and he walks off up the track into the wheat and is gone the first moment nobody sees him.
     */
    /**
     * 0.4.693 (run 1718): Otis, spoken to as he comes to the coffee machine, finishes answering the reader before he goes on to pour. While
     * he is turned to them or his answer is still up, his head never swings away from them in a single tick (to the machine, the counter or
     * his place); he still gets his coffee and comes to the counter afterwards.
     */
    @GameTest(template="empty",batch="fallow_answer",timeoutTicks=3000)
    public static void aCustomerFinishesAnsweringFirst(GameTestHelper h){
        var f=place(h);var l=f.level(P);var b=f.base(P);Fallow.forget(b);
        var p=f.player("fallow_answer",P);
        int[] jumps={0};float[] last={Float.NaN};String[] seen={""};
        h.onEachTick(()->{
            var otis=Fallow.find(l,b,Fallow.DRIVER);
            if(otis==null||otis.addressee()==null&&!otis.saying()){last[0]=Float.NaN;return;}
            float head=otis.getYHeadRot();var d=p.position().subtract(otis.position());float toward=(float)Math.toDegrees(Math.atan2(-d.x,d.z));
            if(!Float.isNaN(last[0])&&Math.abs(net.minecraft.util.Mth.wrapDegrees(head-toward))-Math.abs(net.minecraft.util.Mth.wrapDegrees(last[0]-toward))>40){
                jumps[0]++;seen[0]="head "+head+" from "+last[0]+", the reader at "+toward;}
            last[0]=head;
        });
        h.startSequence().thenIdle(5).thenExecute(()->f.at(p,P,3.5,0,-9.5))
        // Run 1722: the skip was dropped when the afternoon had not begun yet, and with Faye unserved she went first; Otis was still at
        // his truck when the case timed out. The skip waits for the afternoon and for Otis at his rig: skipped before the cast is placed,
        // he is sent for his coffee before he is there to go.
        .thenWaitUntil(()->h.assertTrue(Fallow.afternoon(b)!=null&&Fallow.find(l,b,Fallow.DRIVER)!=null,"the afternoon begins, Otis at his rig"))
        .thenExecute(()->{Fallow.fayeServed(b);Fallow.skipTo(b,Fallow.OTIS_TIME);})
        .thenWaitUntil(()->{var otis=Fallow.find(l,b,Fallow.DRIVER);
            h.assertTrue(otis!=null&&otis.position().subtract(Vec3.atLowerCornerOf(b)).distanceTo(new Vec3(6.5,0,-10.5))<2.5,"Otis nearly at the machine: "+(otis==null?"none":otis.position().subtract(Vec3.atLowerCornerOf(b))));})
        .thenExecute(()->{var otis=Fallow.find(l,b,Fallow.DRIVER);p.interactOn(otis,InteractionHand.MAIN_HAND);
            h.assertTrue(said(otis).startsWith("Hauling seed corn north."),"he answers: "+said(otis));})
        .thenWaitUntil(()->h.assertTrue(Fallow.customer(b)==Fallow.DRIVER,"then gets his coffee and comes to the counter: "+Fallow.customer(b)))
        .thenExecute(()->h.assertTrue(jumps[0]==0,"while he answered, his head never swung away from the reader: "+seen[0]))
        .thenSucceed();
    }

    @GameTest(template="empty",batch="fallow_road",timeoutTicks=9000)
    public static void theBeanAndTheManOnTheRoad(GameTestHelper h){
        var f=place(h);var l=f.level(P);var b=f.base(P);Fallow.forget(b);
        var p=f.player("fallow_road_till",P);var q=f.player("fallow_road_girl",P);
        h.startSequence().thenIdle(5).thenExecute(()->{
            // One behind the counter, one out front by Opal's step, too far for Harlan to give a chore to: he waits for them a while,
            // then goes all the same.
            f.at(p,P,3.5,0,-2.5);f.at(q,P,5.5,0,-14.5);
            f.look(p,b.offset(0,1,20));f.look(q,b.offset(0,1,20));
            Fallow.skipTo(b,Fallow.BEAN_TIME);
        }).thenWaitUntil(()->h.assertTrue(Fallow.bean(b)>=Fallow.FETCHING,"Opal goes for her bean once Harlan is under the car: "+Fallow.keeper(b)+" "+Fallow.bean(b)))
        .thenWaitUntil(()->h.assertTrue(Fallow.bean(b)>=Fallow.CARRYING,"he hands it down to her: "+Fallow.bean(b)))
        .thenExecute(()->{
            var girl=Fallow.find(l,b,Fallow.GIRL);
            h.assertTrue(girl.getMainHandItem().is(io.github.knaitoe.theoldesthouse.labyrinth.FallowRegistry.BEAN_CUP.get())&&girl.gesture()==Fallow.CUP,"she carries the cup in both hands");
            h.assertTrue(Fallow.find(l,b,Fallow.KEEPER).getMainHandItem().isEmpty()||!Fallow.find(l,b,Fallow.KEEPER).getMainHandItem().is(io.github.knaitoe.theoldesthouse.labyrinth.FallowRegistry.BEAN_CUP.get()),"and he no longer has it");
        }).thenWaitUntil(()->h.assertTrue(Fallow.bean(b)==Fallow.SET,"she sets it down out front: "+Fallow.bean(b)))
        .thenExecute(()->{
            h.assertTrue(fitting(l,b,Fallow.BEAN_AT,FallowBlock.Kind.BEAN)&&stage(l,b,Fallow.BEAN_AT)==0,"the bean on the forecourt, two leaves up");
            var girl=Fallow.find(l,b,Fallow.GIRL);
            h.assertTrue(girl.gesture()==Fallow.KNEEL&&girl.getMainHandItem().isEmpty(),"kneeling by it, her hands empty");
            h.assertTrue(girl.blockPosition().equals(b.offset(FallowSite.GIRL_AT)),"at her own step: "+girl.position().subtract(Vec3.atLowerCornerOf(b)));
            told(q);use(q,b,Fallow.BEAN_AT,new BlockPos(5,0,-14));
            h.assertTrue(told(q).contains(FallowTexts.thing(FallowBlock.Kind.BEAN,0)),"a hand on the bean answers");
        }).thenWaitUntil(()->h.assertTrue(f.own(q,P).getInt("Kept")>=40&&f.own(p,P).getInt("Kept")==0,"the reader by her keeps her company, the one inside does not: "+f.own(q,P).getInt("Kept")))
        .thenExecute(()->{Fallow.servedAlready(b);Fallow.skipTo(b,Fallow.STRANGER_TIME);})
        .thenWaitUntil(()->h.assertTrue(Fallow.stranger(b)>=Fallow.COMING,"the man comes along the road: "+Fallow.stranger(b)))
        .thenExecute(()->{
            var man=Fallow.find(l,b,Fallow.STRANGER);
            h.assertTrue(man!=null&&man.position().subtract(Vec3.atLowerCornerOf(b)).x<-40,"out of the haze to the west: "+(man==null?"none":man.position()));
        }).thenWaitUntil(()->h.assertTrue(Fallow.customer(b)==Fallow.STRANGER,"he comes to the register with a bottle of pop: "+Fallow.stranger(b)+" "+Fallow.customer(b)))
        .thenExecute(()->{
            var man=Fallow.find(l,b,Fallow.STRANGER);
            h.assertTrue(man.getMainHandItem().is(io.github.knaitoe.theoldesthouse.labyrinth.FallowRegistry.SODA.get()),"the pop from the cooler");
            told(p);f.click(p,P,FallowSite.ENDING);
            h.assertTrue(said(man).equals(FallowTexts.CUSTOMER[2][1]),"rung up, he says to keep the change: "+said(man));
            f.look(p,b.offset(0,1,20));
        }).thenWaitUntil(()->h.assertTrue(Fallow.stranger(b)==Fallow.ASKING,"he goes out to Opal, asks her name, and asks the time: "+Fallow.stranger(b)))
        .thenExecute(()->{
            h.assertTrue(Talk.replies(q).contains(FallowTexts.TIME_ANSWER)&&Talk.replies(q).contains(FallowTexts.SAY_NOTHING),"the reader by Opal is asked the time: "+Talk.replies(q));
            h.assertTrue(f.own(q,P).getBoolean("Asked"),"and it is kept that they were asked");
            h.assertTrue(Talk.choose(q,FallowTexts.TIME_ANSWER),"they tell him");
            h.assertTrue(f.own(q,P).getBoolean("Answered")&&!f.own(p,P).getBoolean("Answered"),"what each told him is their own");
            h.assertTrue(Fallow.stranger(b)==Fallow.GOING,"and he goes");
            h.assertTrue(said(Fallow.find(l,b,Fallow.STRANGER)).equals(FallowTexts.ANSWERED),"saying what he says: "+said(Fallow.find(l,b,Fallow.STRANGER)));
            h.assertTrue(Fallow.find(l,b,Fallow.GIRL).gesture()==Fallow.KNEEL,"Opal never got up for him");
            f.look(q,b.offset(0,1,20));
        }).thenWaitUntil(()->h.assertTrue(Fallow.stranger(b)==Fallow.GONE&&Fallow.find(l,b,Fallow.STRANGER)==null,"up the track into the wheat, and gone where nobody saw: "+Fallow.stranger(b)))
        .thenExecute(()->{done();h.succeed();});
    }

    /**
     * 0.4.674, the four minutes: the tone and the card, the lids sliding off both shafts, the readers committed; Harlan holding the
     * cellar doors until one reader takes them from him; Faye at the lift's lever until the other takes her by the arm and leads her,
     * then down to Opal; Otis carrying Opal and her bean down; Boyd at the rig; the doors pulled shut, Opal counting, and the light, kept
     * by each reader as where it found them, and both taken out by the House.
     */
    @GameTest(template="empty",batch="fallow_strike",timeoutTicks=7500)
    public static void theFourMinutes(GameTestHelper h){
        var f=place(h);var l=f.level(P);var b=f.base(P);Fallow.forget(b);
        f.chunks.hold(l,new AABB(b.getX()-30,b.getY()-8,b.getZ()-118,b.getX()+30,b.getY()+8,b.getZ()-80));
        // The station itself, so that it is all there to be built again once the readers have gone.
        f.chunks.hold(l,new AABB(b.getX()+FallowSite.YARD_W-1,b.getY()-8,b.getZ()+FallowSite.APRON_N-1,b.getX()+FallowSite.APRON_E+1,b.getY()+8,b.getZ()+1));
        var p=f.player("fallow_strike_doors",P);var q=f.player("fallow_strike_faye",P);
        var cx=FallowSite.PAD_A.getX();var cz=FallowSite.PAD_A.getZ();
        h.startSequence().thenIdle(5).thenExecute(()->{
            f.at(p,P,3.5,0,-2.5);f.at(q,P,2.5,0,-2.5);
            f.look(p,b.offset(0,1,20));f.look(q,b.offset(0,1,20));
            // The way each came in, for the House to take them back by after the light: south of the station, past its scene.
            for(var r:new ServerPlayer[]{p,q})f.data().pushReturn(r.getUUID(),new LabyrinthData.Waypoint(l.dimension(),Vec3.atBottomCenterOf(b.offset(0,0,24)),0F,false));
        }).thenWaitUntil(()->h.assertTrue(Fallow.afternoon(b)!=null&&Fallow.find(l,b,Fallow.GIRL)!=null&&Fallow.find(l,b,Fallow.WOMAN)!=null,"the afternoon is on"))
        .thenExecute(()->{
            l.setBlock(b.offset(Fallow.BEAN_AT),FallowBlock.of(FallowBlock.Kind.BEAN,Direction.WEST,0),3);
            told(p);FallowStrike.begin(b);
        }).thenWaitUntil(()->h.assertTrue(FallowStrike.since(b)>=1,"the tone comes on: "+FallowStrike.since(b)))
        .thenExecute(()->{
            h.assertTrue(stage(l,b,FallowSite.TV)==FallowBlock.CARD,"the television shows its card");
            h.assertTrue(told(p).contains(FallowTexts.TONE),"the ball game stops for the tone");
            h.assertTrue(Fallow.keeper(b)==Fallow.STRIKE,"Harlan is off his afternoon");
            h.assertFalse(Fallow.locked(p),"nobody is held yet");
            FallowStrike.skip(b,FallowStrike.LIDS-1);
        }).thenWaitUntil(()->h.assertTrue(FallowStrike.slid(b)==FallowSite.LID_SLIDE,"both lids slide all the way: "+FallowStrike.slid(b)))
        .thenExecute(()->{
            h.assertTrue(fitting(l,b,new BlockPos(cx+FallowSite.LID_HALF+FallowSite.LID_SLIDE,-1,cz),FallowBlock.Kind.PAD),"the lid lies over to the east of its seat");
            h.assertTrue(l.getBlockState(b.offset(cx-1,-1,cz)).isAir()&&l.getBlockState(b.offset(cx-1,-3,cz)).isAir(),"its seat is a mouth over an empty shaft");
            h.assertTrue(l.getBlockState(b.offset(cx-1,-1,cz+FallowSite.LID_HALF)).is(FallowRegistry.material("apron")),"with a rim at its end");
            h.assertTrue(Fallow.locked(p)&&LiteraryVignettes.retreatLocked(q),"from the lids, nobody leaves by the House's door");
        }).thenWaitUntil(()->h.assertTrue(FallowStrike.harlan(b)==FallowStrike.HOLDING,"Harlan runs to the cellar doors and holds them: "+FallowStrike.harlan(b)))
        .thenExecute(()->{
            h.assertTrue(Fallow.cellarOpen(l,b),"he has them open");
            told(p);use(p,b,new BlockPos(FallowSite.WELL_W,0,FallowSite.WELL_S),new BlockPos(-12,0,-9));
            h.assertTrue(FallowStrike.harlan(b)==FallowStrike.GOING_DOWN&&Fallow.cellarOpen(l,b),"a hand on the doors near him takes them, and they stay open");
            h.assertTrue(told(p).contains(FallowTexts.TAKEN_DOORS)&&f.own(p,P).getBoolean("HeldDoors"),"the reader holding them is kept");
        }).thenWaitUntil(()->h.assertTrue(FallowStrike.faye(b)==FallowStrike.AT_CAR,"Faye runs to the car's lever: "+FallowStrike.faye(b)))
        .thenExecute(()->{
            var faye=Fallow.find(l,b,Fallow.WOMAN);f.at(q,P,14.5,0,-9.5);told(q);LiteraryVignettes.talk(q,faye);
            h.assertTrue(FallowStrike.faye(b)==FallowStrike.LED&&told(q).contains(FallowTexts.FAYE_TAKEN),"the other reader takes her by the arm");
            h.assertTrue(said(faye).equals(FallowTexts.FAYE_WHERE),"and she asks after Opal: "+said(faye));
            f.at(q,P,12.5,0,-14.5);
        }).thenWaitUntil(()->h.assertTrue(Fallow.find(l,b,Fallow.WOMAN).distanceTo(q)<3,"she comes after the reader: "+Fallow.find(l,b,Fallow.WOMAN).position().subtract(Vec3.atLowerCornerOf(b))))
        .thenExecute(()->f.at(q,P,-12.5,-3,-13.5))
        .thenWaitUntil(()->h.assertTrue(FallowStrike.faye(b)==FallowStrike.FAYE_BELOW,"and down the steps to Opal once the reader is below: "+FallowStrike.faye(b)+" "+Fallow.find(l,b,Fallow.WOMAN).position().subtract(Vec3.atLowerCornerOf(b))))
        .thenExecute(()->h.assertTrue(f.own(q,P).getBoolean("BroughtFaye")&&!f.own(p,P).getBoolean("BroughtFaye"),"the one who brought her is kept, the other not"))
        .thenWaitUntil(()->h.assertTrue(FallowStrike.otis(b)==FallowStrike.OTIS_BELOW,"Otis carries Opal down: "+FallowStrike.otis(b)+" "+Fallow.find(l,b,Fallow.DRIVER).position().subtract(Vec3.atLowerCornerOf(b))))
        .thenExecute(()->{
            var girl=Fallow.find(l,b,Fallow.GIRL);var r=girl.position().subtract(Vec3.atLowerCornerOf(b));
            h.assertTrue(!girl.isPassenger()&&FallowSite.inCellar(r),"set down below, by the cot: "+r);
            h.assertTrue(girl.getMainHandItem().is(FallowRegistry.BEAN_CUP.get())&&l.getBlockState(b.offset(Fallow.BEAN_AT)).isAir(),"with her bean, taken up off the forecourt");
        }).thenWaitUntil(()->h.assertTrue(FallowStrike.harlan(b)==FallowStrike.BELOW&&FallowStrike.boyd(b)==FallowStrike.AT_RIG,"Harlan below, Boyd at the rig's door: "+FallowStrike.harlan(b)+" "+FallowStrike.boyd(b)))
        // The one who held the doors goes out front, before the store's west window, for the light to find them there.
        .thenExecute(()->{f.at(p,P,-6.6,0,-13.5);FallowStrike.skip(b,FallowStrike.LIGHT-205);})
        .thenWaitUntil(()->h.assertTrue(FallowStrike.counted(b)==FallowTexts.COUNTING.length,"at the last, Opal counts: "+FallowStrike.counted(b)))
        .thenExecute(()->h.assertFalse(Fallow.cellarOpen(l,b),"the doors are pulled shut before the light"))
        .thenWaitUntil(()->h.assertTrue(FallowStrike.since(b)>=FallowStrike.LIGHT+2,"the light"))
        .thenExecute(()->{
            h.assertTrue(f.own(p,P).getString("AtLight").equals("outside")&&f.own(q,P).getString("AtLight").equals("cellar"),"each reader kept where it found them: "+f.own(p,P).getString("AtLight")+" "+f.own(q,P).getString("AtLight"));
            h.assertTrue(f.own(p,P).getInt("Strikes")==1&&f.own(q,P).getInt("Strikes")==1,"once each");
            h.assertTrue(stage(l,b,FallowSite.TV)==FallowBlock.DARK,"the television is dark");
            // 0.4.680: the light throws the one out front's shadow back onto the store's front, on the solid column nearest them.
            var cast=l.getBlockEntity(b.offset(-7,0,FallowSite.STORE_N-1));
            h.assertTrue(cast instanceof FallowShadowBlock.Shadow sh&&p.getUUID().equals(sh.reader),"the reader out front leaves their shadow beside where they stood");
            h.assertTrue(f.own(p,P).getInt("ShadowX")==-7&&!f.own(q,P).contains("ShadowX"),"kept as theirs; the one below throws none");
            // 0.4.681: Boyd, out at the rig's door, leaves his own (one of the five's, not a reader's); no other reader's is there.
            for(int x:FallowRuin.WALL)if(l.getBlockEntity(b.offset(x,0,FallowSite.STORE_N-1)) instanceof FallowShadowBlock.Shadow sh)
                h.assertTrue(p.getUUID().equals(sh.reader)||sh.person&&"Boyd".equals(sh.name),"no other shadow on the front: "+x);
            told(q);
        }).thenWaitUntil(()->h.assertTrue(FallowStrike.since(b)>=FallowStrike.LIGHT+FallowRuin.DOORS+2,"the station comes down"))
        .thenExecute(()->{
            var heard=told(q);
            h.assertTrue(heard.contains(FallowTexts.RUIN_CANOPY)&&heard.contains(FallowTexts.RUIN_STORE)&&heard.contains(FallowTexts.RUIN_DOORS),"heard from below, a part at a time: "+heard);
            var pane=l.getBlockState(b.offset(-6,1,FallowSite.STORE_N));
            h.assertTrue(FallowBlock.is(pane,FallowBlock.Kind.WINDOW)&&pane.getValue(FallowBlock.STAGE)>=FallowBlock.BLOWN_LOW,"the plate glass is blown out of its frame");
            h.assertFalse(fitting(l,b,new BlockPos(0,FallowSite.CANOPY_Y,FallowSite.ISLAND_Z),FallowBlock.Kind.CANOPY),"the canopy is off its columns");
            h.assertTrue(rubble(l,b,new BlockPos(FallowSite.CANOPY_W,0,FallowSite.CANOPY_N+1),FallowBlock.COLUMN),"a column lies beside its foot");
            h.assertTrue(l.getBlockState(b.offset(0,FallowSite.CEILING,-6)).isAir(),"the store's roof has gone into the store");
            h.assertTrue(rubble(l,b,new BlockPos(FallowSite.WELL_W,0,FallowSite.WELL_S),FallowBlock.ON_DOORS),"and the fall of it lies across the cellar doors");
            h.assertTrue(is(l,b,new BlockPos(-7,2,FallowSite.STORE_N),FallowRegistry.material("siding")),"the front stands");
            h.assertTrue(l.getBlockState(b).isAir()&&l.getBlockState(b.above()).isAir(),"and so does the House's door in the back wall");
            h.assertTrue(FallowShadowBlock.is(l.getBlockState(b.offset(-7,0,FallowSite.STORE_N-1))),"with the shadow still on the front");
        })
        .thenWaitUntil(()->h.assertTrue(!FallowSite.inScene(p.position().subtract(Vec3.atLowerCornerOf(b)))||p.level()!=l,"the reader out in it is taken out by the House"))
        // 0.4.681: the one below stays below, for the buried days (theBuriedDays has them); here they step out of it before the ground is
        // still, as a spectator, so that the afternoon is let go as before.
        .thenExecute(()->{h.assertTrue(FallowSite.inCellar(q.position().subtract(Vec3.atLowerCornerOf(b))),"the one below is still below");q.setGameMode(GameType.SPECTATOR);})
        // 0.4.683: the station is held for the one the light found out in it (FallowRoad.RETURN_HOLD); they come back, and then go too.
        .thenIdle(Fallow.EMPTY_FORGET+20)
        .thenExecute(()->{h.assertTrue(Fallow.afternoon(b)!=null,"with nobody at it, the afternoon is kept");h.assertTrue(FallowRoad.held(b),"with nobody at it, the station is held for them");f.at(p,P,2.5,0,-6.5);})
        .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("CameBack"),"they come back"))
        .thenExecute(()->p.setGameMode(GameType.SPECTATOR))
        // Empty, the afternoon is over; the next begins on the station as it was built, with nobody's shadow on it.
        .thenWaitUntil(()->h.assertTrue(Fallow.afternoon(b)==null,"the afternoon is over"))
        .thenExecute(()->{
            h.assertTrue(fitting(l,b,new BlockPos(0,FallowSite.CANOPY_Y,FallowSite.ISLAND_Z),FallowBlock.Kind.CANOPY),"the canopy is back on its columns");
            h.assertTrue(stage(l,b,new BlockPos(-6,1,FallowSite.STORE_N))<FallowBlock.BLOWN_LOW,"the glass is in its frames");
            h.assertFalse(l.getBlockState(b.offset(0,FallowSite.CEILING,-6)).isAir(),"the store has its ceiling");
            h.assertTrue(fitting(l,b,new BlockPos(FallowSite.WELL_W,0,FallowSite.WELL_S),FallowBlock.Kind.CELLAR),"the cellar doors are shut and clear");
            h.assertTrue(l.getBlockState(b.offset(-7,0,FallowSite.STORE_N-1)).isAir(),"and the front has no shadow on it");
            h.assertFalse(FallowRuin.ruined(l,b),"the station is whole");
        })
        .thenExecute(()->{done();h.succeed();});
    }

    /**
     * 0.4.681, buried (docs/FALLOW_0_4_681.md). Two readers below at the light and one out in it. The people out in it are gone, Boyd's
     * shadow on the garage's front with his arm up; the two below stay below. When the ground is still Opal sets her bean down by the
     * cot; the lantern burns; no daylight comes past the fall over the doors. The roof is hot, then warm, then cool; the doors are too hot
     * to dig until it is. A hand turns the lantern down, out and lights it again. Otis puts it out at night and the bean grows in the dark,
     * leaning toward the steps, and is seen grown in the morning. On the second day Otis asks each reader below about home in turn, and
     * what he says follows the House's read of the one who tells him, or that they said nothing. On the third the doors are dug: one
     * reader from below, Otis beside them, the one who was out in it from the yard; a length clear lets the daylight in; the doors give.
     * The one who dug and came up is kept as having dug out and come up, the other as neither; nobody is held from the House's door
     * after; Otis and Opal come up, she with her bean. Once all have gone, the station is built again with the cellar as it was.
     */
    @GameTest(template="empty",batch="fallow_buried",timeoutTicks=9000)
    public static void theBuriedDays(GameTestHelper h){
        var f=place(h);var l=f.level(P);var b=f.base(P);Fallow.forget(b);
        f.chunks.hold(l,new AABB(b.getX()-30,b.getY()-8,b.getZ()-118,b.getX()+30,b.getY()+8,b.getZ()-80));
        f.chunks.hold(l,new AABB(b.getX()+FallowSite.YARD_W-1,b.getY()-8,b.getZ()+FallowSite.APRON_N-1,b.getX()+FallowSite.APRON_E+1,b.getY()+8,b.getZ()+1));
        var p=f.player("fallow_buried_out",P);var q=f.player("fallow_buried_a",P);var r=f.player("fallow_buried_b",P);
        var door=new BlockPos(FallowSite.WELL_E,0,FallowSite.WELL_S);var step=new BlockPos(-14,-2,-10);var room=new BlockPos(-12,-3,-14);
        h.startSequence().thenIdle(5).thenExecute(()->{
            f.at(p,P,3.5,0,-2.5);f.at(q,P,2.5,0,-2.5);f.at(r,P,1.5,0,-2.5);
            for(var x:new ServerPlayer[]{p,q,r})f.data().pushReturn(x.getUUID(),new LabyrinthData.Waypoint(l.dimension(),Vec3.atBottomCenterOf(b.offset(0,0,24)),0F,false));
            // The House has read one of the two below as having somebody to get back to; the other it has not read.
            HouseRead.signal(q,HouseRead.Trait.HOME,"HAVEN",false);HouseRead.signal(q,HouseRead.Trait.HOME,"HAVEN",true);
        }).thenWaitUntil(()->h.assertTrue(Fallow.afternoon(b)!=null&&Fallow.find(l,b,Fallow.GIRL)!=null&&Fallow.find(l,b,Fallow.DRIVER)!=null&&Fallow.find(l,b,Fallow.MAN)!=null,"the afternoon is on"))
        .thenExecute(()->{l.setBlock(b.offset(Fallow.BEAN_AT),FallowBlock.of(FallowBlock.Kind.BEAN,Direction.WEST,0),3);FallowStrike.begin(b);})
        .thenWaitUntil(()->h.assertTrue(FallowStrike.since(b)>=1,"the tone comes on"))
        .thenExecute(()->FallowStrike.skip(b,FallowStrike.GO-1))
        .thenWaitUntil(()->h.assertTrue(FallowStrike.otis(b)==FallowStrike.OTIS_BELOW,"Otis carries Opal down: "+FallowStrike.otis(b)))
        .thenWaitUntil(()->h.assertTrue(FallowStrike.boyd(b)==FallowStrike.AT_RIG,"Boyd is at the rig's door: "+FallowStrike.boyd(b)))
        .thenExecute(()->{f.at(q,P,-12.5,-3,-13.5);f.at(r,P,-14.5,-3,-15.5);f.at(p,P,-6.6,0,-13.5);FallowStrike.skip(b,FallowStrike.LIGHT-2);})
        .thenWaitUntil(()->h.assertTrue(FallowStrike.since(b)>=FallowStrike.LIGHT+2,"the light"))
        .thenExecute(()->{
            h.assertTrue(Fallow.find(l,b,Fallow.MAN)==null,"Boyd, who would not go down, is gone from the station");
            FallowShadowBlock.Shadow cast=null;
            for(int x:FallowRuin.WALL)if(l.getBlockEntity(b.offset(x,0,FallowSite.STORE_N-1)) instanceof FallowShadowBlock.Shadow sh)cast=sh;
            h.assertTrue(cast!=null&&cast.person&&cast.reach&&"Boyd".equals(cast.name)&&cast.getBlockPos().getX()-b.getX()>=FallowSite.STORE_E,"his shadow is on the garage's front beside where he stood, an arm up");
            h.assertTrue(Fallow.find(l,b,Fallow.KEEPER)==null&&Fallow.find(l,b,Fallow.WOMAN)==null,"Harlan, who stayed at the doors, and Faye at the car are gone too");
            h.assertTrue(FallowBuried.down(b,Fallow.DRIVER)&&FallowBuried.down(b,Fallow.GIRL)&&!FallowBuried.down(b,Fallow.KEEPER),"Otis and Opal are below, and nobody else of theirs");
        })
        .thenWaitUntil(()->h.assertTrue(FallowBuried.day(b)==0,"the ground is still: "+FallowStrike.since(b)))
        .thenWaitUntil(()->h.assertTrue(l.getBrightness(LightLayer.SKY,b.offset(-13,-3,-11))==0,"no daylight comes past the fall over the doors: "+l.getBrightness(LightLayer.SKY,b.offset(-13,-3,-11))))
        .thenExecute(()->{
            for(var x:new ServerPlayer[]{q,r})h.assertTrue(FallowSite.inCellar(x.position().subtract(Vec3.atLowerCornerOf(b)))&&x.level()==l,"the readers below stay below");
            h.assertTrue(told(q).contains(FallowTexts.STILL),"they are told the ground is still");
            var bean=l.getBlockState(b.offset(FallowBuried.BEAN_BELOW));
            h.assertTrue(FallowBlock.is(bean,FallowBlock.Kind.BEAN)&&bean.getValue(FallowBlock.STAGE)==0&&Fallow.find(l,b,Fallow.GIRL).getMainHandItem().isEmpty(),"Opal sets her bean down by the cot");
            h.assertTrue(stage(l,b,FallowSite.LANTERN)==FallowBlock.LANTERN_LIT&&FallowBuried.oil(b)>0,"the lantern burns");
            use(q,b,new BlockPos(-12,-1,-14),room);
            h.assertTrue(told(q).contains(FallowTexts.CEILING[0]),"the roof is hot the first day");
            use(q,b,door,step);
            h.assertTrue(told(q).contains(FallowTexts.HOT_DOORS)&&FallowBuried.dug(b)==0,"and the doors too hot to dig at");
            use(q,b,FallowSite.LANTERN,room);
            h.assertTrue(stage(l,b,FallowSite.LANTERN)==FallowBlock.LANTERN_LOW&&told(q).contains(FallowTexts.LANTERN_LOW),"a hand turns the lantern down");
            use(q,b,FallowSite.LANTERN,room);
            h.assertTrue(stage(l,b,FallowSite.LANTERN)==FallowBlock.LANTERN_DARK,"and out");
            use(q,b,FallowSite.LANTERN,room);
            h.assertTrue(stage(l,b,FallowSite.LANTERN)==FallowBlock.LANTERN_LIT&&told(q).contains(FallowTexts.LANTERN_LIT),"and lights it again with a match");
            h.assertTrue(f.own(q,P).getInt("Lantern")==3&&f.own(r,P).getInt("Lantern")==0,"each reader's own hand on it is kept");
            FallowBuried.skip(b,FallowBuried.NIGHT_AT-1);
        })
        .thenWaitUntil(()->h.assertTrue(stage(l,b,FallowSite.LANTERN)==FallowBlock.LANTERN_DARK,"Otis puts the lantern out for the night"))
        .thenWaitUntil(()->h.assertTrue(Fallow.find(l,b,Fallow.GIRL).gesture()==Fallow.DOZE,"Opal dozes by her bean"))
        .thenExecute(()->{FallowBuried.dark(b,FallowBuried.GROWS*3);told(q);FallowBuried.skip(b,FallowBuried.DAY-1);})
        .thenWaitUntil(()->h.assertTrue(FallowBuried.day(b)==1&&stage(l,b,FallowSite.LANTERN)==FallowBlock.LANTERN_LIT,"in the morning Otis lights it"))
        .thenIdle(3)
        .thenExecute(()->{
            var heard=told(q);
            h.assertTrue(heard.contains(FallowTexts.MORNING[0])&&heard.contains(FallowTexts.BEAN_GREW),"a second morning, and the bean grown in the dark: "+heard);
            var bean=l.getBlockState(b.offset(FallowBuried.BEAN_BELOW));
            h.assertTrue(bean.getValue(FallowBlock.STAGE)==3&&bean.getValue(FallowBlock.FACING)==Direction.SOUTH,"long and thin, leaning toward the steps");
            use(r,b,new BlockPos(-15,-1,-15),new BlockPos(-15,-3,-15));
            h.assertTrue(told(r).contains(FallowTexts.CEILING[1]),"the roof is warm the second day");
        })
        // Otis asks each of them, one at a time; to the one the House reads as having somebody to get back to, and who tells him, he says
        // so; to the one who says nothing, that they need not.
        .thenWaitUntil(()->h.assertTrue(Talk.waiting(q,"fallow_home")||Talk.waiting(r,"fallow_home"),"Otis asks about home"))
        .thenExecute(()->{var first=Talk.waiting(q,"fallow_home")?q:r;h.assertTrue(Talk.choose(first,first==q?"Tell him":"Say nothing"),"an answer");})
        .thenWaitUntil(()->h.assertTrue(Talk.waiting(q,"fallow_home")||Talk.waiting(r,"fallow_home"),"and asks the other"))
        .thenExecute(()->{
            var second=Talk.waiting(q,"fallow_home")?q:r;h.assertTrue(Talk.choose(second,second==q?"Tell him":"Say nothing"),"an answer");
            h.assertTrue(Talk.lines(q).contains("Otis: "+FallowTexts.homeAnswer("HAVEN"))&&f.own(q,P).getBoolean("ToldHome")&&"HAVEN".equals(f.own(q,P).getString("HomeRead")),"what he says follows the read: "+Talk.lines(q));
            h.assertTrue(Talk.lines(r).contains("Otis: "+FallowTexts.HOME_QUIET)&&f.own(r,P).getBoolean("QuietHome")&&!f.own(r,P).getBoolean("ToldHome"),"to silence, that it is all right: "+Talk.lines(r));
            FallowBuried.skip(b,FallowBuried.DAY*FallowBuried.COOL_DAY+20);
        })
        .thenWaitUntil(()->h.assertTrue(FallowBuried.day(b)==FallowBuried.COOL_DAY,"the third day"))
        .thenWaitUntil(()->h.assertTrue(f.own(q,P).getInt("Days")==3&&f.own(r,P).getInt("Days")==3,"each keeps the days below: "+f.own(q,P).getInt("Days")))
        .thenExecute(()->{
            use(q,b,new BlockPos(-12,-1,-14),room);
            h.assertTrue(told(q).contains(FallowTexts.CEILING[2]),"the roof is cool");
            use(q,b,door,step);
            h.assertTrue(FallowBuried.dug(b)==1&&f.own(q,P).getInt("Dug")==1&&told(q).contains(FallowTexts.DIG_FIRST),"a reader digs at the doors from the steps");
            use(q,b,door,step);
            h.assertTrue(FallowBuried.dug(b)==1,"a stroke at a time");
        })
        .thenWaitUntil(()->h.assertTrue(FallowBuried.digging(b),"Otis comes to dig beside them"))
        .thenExecute(()->{
            // The one who was out in it has come back, and digs from the yard.
            f.at(p,P,-11.5,1,-9.5);told(p);
            int before=FallowBuried.dug(b);use(p,b,new BlockPos(FallowSite.WELL_W,0,FallowSite.WELL_S-1),new BlockPos(-12,1,-10));
            h.assertTrue(FallowBuried.dug(b)==before+1&&f.own(p,P).getInt("DugAbove")==1&&told(p).contains(FallowTexts.DIG_FIRST_ABOVE),"from the yard too");
            FallowBuried.dugTo(b,FallowBuried.PER_CELL-1);told(q);
        })
        .thenIdle(FallowBuried.STROKE+1)
        .thenExecute(()->use(q,b,door,step))
        .thenWaitUntil(()->h.assertTrue(fitting(l,b,door,FallowBlock.Kind.CELLAR),"a length of the doors comes clear: "+FallowBuried.dug(b)))
        .thenWaitUntil(()->h.assertTrue(l.getBrightness(LightLayer.SKY,b.offset(FallowSite.WELL_E,-1,FallowSite.WELL_S-1))>0,"and daylight comes down the steps: "+l.getBrightness(LightLayer.SKY,b.offset(FallowSite.WELL_E,-1,FallowSite.WELL_S-1))))
        .thenExecute(()->{
            h.assertTrue(told(q).contains(FallowTexts.CRACK),"a crack of daylight");
            h.assertTrue(Fallow.locked(q)==false,"nobody is held from the House's door after the light");
            FallowBuried.dugTo(b,FallowBuried.DIG-1);
        })
        .thenIdle(FallowBuried.STROKE+1)
        .thenExecute(()->use(q,b,door,step))
        .thenWaitUntil(()->h.assertTrue(FallowBuried.open(b)&&Fallow.cellarOpen(l,b),"the doors give"))
        .thenExecute(()->{
            for(int z=FallowSite.WELL_S;z>=FallowSite.WELL_N;z--)for(int x=FallowSite.WELL_W;x<=FallowSite.WELL_E;x++)
                h.assertTrue(fitting(l,b,new BlockPos(x,0,z),FallowBlock.Kind.CELLAR_OPEN),"every leaf thrown back: "+x+" "+z);
            h.assertTrue(f.own(q,P).getBoolean("DugOut")&&f.own(p,P).getBoolean("DugOut")&&!f.own(r,P).getBoolean("DugOut"),"those who dug are kept as having dug out");
            f.at(q,P,-13.5,0,-5.5);
        })
        .thenWaitUntil(()->h.assertTrue(f.own(q,P).getBoolean("CameUp")&&!f.own(r,P).getBoolean("CameUp"),"the one who climbed out is kept as having come up"))
        .thenWaitUntil(()->{var otis=Fallow.find(l,b,Fallow.DRIVER);h.assertTrue(otis!=null&&!FallowSite.inCellar(otis.position().subtract(Vec3.atLowerCornerOf(b))),"Otis comes up");})
        .thenWaitUntil(()->{var girl=Fallow.find(l,b,Fallow.GIRL);h.assertTrue(girl!=null&&girl.gesture()==Fallow.SIT&&girl.getMainHandItem().is(FallowRegistry.BEAN_CUP.get())&&!FallowSite.inCellar(girl.position().subtract(Vec3.atLowerCornerOf(b))),"and Opal, with her bean, and sits by the steps");})
        .thenExecute(()->{for(var x:new ServerPlayer[]{p,q,r})x.setGameMode(GameType.SPECTATOR);})
        .thenWaitUntil(()->h.assertTrue(Fallow.afternoon(b)==null,"once all have gone, the afternoon is over"))
        .thenExecute(()->{
            h.assertTrue(fitting(l,b,new BlockPos(FallowSite.WELL_W,0,FallowSite.WELL_S),FallowBlock.Kind.CELLAR),"the cellar doors are shut and clear");
            h.assertTrue(l.getBlockState(b.offset(FallowBuried.BEAN_BELOW)).isAir()&&stage(l,b,FallowSite.LANTERN)==FallowBlock.LANTERN_LIT,"the cellar is as it was");
            for(int x:FallowRuin.WALL)h.assertTrue(l.getBlockState(b.offset(x,0,FallowSite.STORE_N-1)).isAir(),"and nobody's shadow is on the front: "+x);
        })
        .thenExecute(()->{done();h.succeed();});
    }

    /**
     * 0.4.682, the ash outside (docs/FALLOW_0_4_682.md). With one reader below at the light, once the light has gone over the field burns
     * tile by tile to bare ash and burnt stubble on ash-grey ground; the ground is opened where things came down, a bowl burnt black at the
     * bottom; the poles lie across the road with their crossarms stood on end, their wires gone, some broken off at their feet; the
     * farmhouse is burnt to its sill, its chimney standing broken and its front door still in a charred scrap of its frame, the way on;
     * the windbreak is burnt standing and the yard black. When the station has stood empty, the fresh afternoon puts every bit of it back as
     * it was sown and built: the same wheat in the same places, the ground filled, the poles stood up and strung, the farmhouse whole, its
     * windbreak in leaf and its yard in grass, and the front door kept.
     */
    @GameTest(template="empty",batch="fallow_ash",timeoutTicks=3000)
    public static void theAshOutside(GameTestHelper h){
        var f=place(h);var l=f.level(P);var b=f.base(P);Fallow.forget(b);
        // The whole field, from the station to its far edge.
        f.chunks.hold(l,new AABB(b.getX()+FallowSite.WEST-1,b.getY()-8,b.getZ()+FallowSite.NORTH-1,b.getX()+FallowSite.EAST+1,b.getY()+16,b.getZ()+1));
        var q=f.player("fallow_ash_below",P);
        var field=new BlockPos(-50,0,-70);var crater=FallowAsh.CRATERS[0];int mid=(FallowSite.FARM_S+FallowSite.FARM_N)/2;
        var chimney=new BlockPos(FallowSite.FARM_E-3,0,mid);var wall=new BlockPos(FallowSite.FARM_W,3,FallowSite.FARM_S);
        var yard=new BlockPos(-40,-1,-48);var trunk=new BlockPos(-40,0,FallowSite.WINDBREAK_Z);
        boolean[] door={false};
        h.startSequence().thenIdle(5).thenExecute(()->{
            f.at(q,P,2.5,0,-2.5);
            f.data().pushReturn(q.getUUID(),new LabyrinthData.Waypoint(l.dimension(),Vec3.atBottomCenterOf(b.offset(0,0,24)),0F,false));
        }).thenWaitUntil(()->h.assertTrue(Fallow.afternoon(b)!=null&&Fallow.find(l,b,Fallow.DRIVER)!=null,"the afternoon is on"))
        .thenExecute(()->{
            // The way on is put in by OnwardDoors' own tick once nobody can see the doorway, which may be before or after the light
            // (run 1711's local check raced it); it is put in here, so that the burn is shown to leave a door that stands.
            if(!(l.getBlockState(b.offset(FallowSite.ONWARD)).getBlock() instanceof DoorBlock))OnwardDoors.install(l,b,LabyrinthPlace.FALLOW);
            door[0]=l.getBlockState(b.offset(FallowSite.ONWARD)).getBlock() instanceof DoorBlock;
            h.assertTrue(door[0],"the farmhouse's front door, the way on, is in its wall");
            h.assertTrue(l.getBlockState(b.offset(field)).equals(FallowSite.wheatAt(field.getX(),field.getZ())),"the wheat stands as it was sown");
            FallowStrike.begin(b);
        })
        .thenWaitUntil(()->h.assertTrue(FallowStrike.since(b)>=1,"the tone comes on"))
        .thenExecute(()->{f.at(q,P,-12.5,-3,-13.5);FallowStrike.skip(b,FallowStrike.LIGHT-2);})
        .thenWaitUntil(()->h.assertTrue(FallowStrike.since(b)>=FallowStrike.LIGHT+2,"the light"))
        .thenWaitUntil(()->h.assertTrue(FallowAsh.done(b),"the outside burns: "+FallowStrike.since(b)))
        .thenExecute(()->{
            h.assertTrue(FallowSite.inCellar(q.position().subtract(Vec3.atLowerCornerOf(b))),"the reader below stays below");
            h.assertTrue(is(l,b,field.below(),FallowRegistry.material("ash_ground")),"the field's ground is grey with ash");
            var top=l.getBlockState(b.offset(field));
            h.assertTrue(top.isAir()||FallowBlock.is(top,FallowBlock.Kind.WHEAT)&&top.getValue(FallowBlock.STAGE)==FallowBlock.ASH,"and its wheat is burnt to stubble or to nothing: "+top);
            for(int x=FallowSite.WEST;x<=FallowSite.EAST;x+=7)for(int z=-40;z>=FallowSite.NORTH;z-=9){
                var st=l.getBlockState(b.offset(x,0,z));
                h.assertTrue(!FallowBlock.is(st,FallowBlock.Kind.WHEAT)||st.getValue(FallowBlock.STAGE)==FallowBlock.ASH,"no wheat stands anywhere in the field: "+x+" "+z);
            }
            h.assertTrue(l.getBlockState(b.offset(crater[0],-1,crater[1])).isAir()&&l.getBlockState(b.offset(crater[0],-3,crater[1])).isAir()
                &&is(l,b,new BlockPos(crater[0],-4,crater[1]),FallowRegistry.material("scorched")),"the ground is opened, burnt black at the bottom");
            for(int x:FallowAsh.POLE_X){
                h.assertTrue(fitting(l,b,new BlockPos(x,0,FallowSite.POLES_Z+6),FallowBlock.Kind.POLE)&&stage(l,b,new BlockPos(x,0,FallowSite.POLES_Z+6))==FallowBlock.POLE_DOWN_TOP,"each pole lies across the road with its crossarm on end: "+x);
                h.assertTrue(stage(l,b,new BlockPos(x,0,FallowSite.POLES_Z+3))==FallowBlock.POLE_DOWN&&l.getBlockState(b.offset(x,FallowSite.POLE_TOP,FallowSite.POLES_Z)).isAir(),"and nothing of it stands: "+x);
                var foot=l.getBlockState(b.offset(x,0,FallowSite.POLES_Z));
                h.assertTrue(FallowAsh.snapped(x)==(FallowBlock.is(foot,FallowBlock.Kind.POLE)&&foot.getValue(FallowBlock.STAGE)<FallowBlock.POLE_DOWN),"the broken ones leave a stump: "+x);
            }
            for(int x=FallowSite.WEST;x<=FallowSite.EAST;x++){
                var st=l.getBlockState(b.offset(x,FallowSite.POLE_TOP,FallowSite.POLES_Z));
                h.assertTrue(!FallowBlock.is(st,FallowBlock.Kind.POLE),"the wires are gone: "+x);
            }
            h.assertTrue(is(l,b,chimney,FallowRegistry.material("brick"))&&is(l,b,chimney.above(FallowAsh.CHIMNEY_LEFT),FallowRegistry.material("brick"))
                &&!is(l,b,chimney.above(FallowAsh.CHIMNEY_LEFT+1),FallowRegistry.material("brick")),"the farmhouse's chimney stands, its top broken off");
            h.assertTrue(l.getBlockState(b.offset(wall)).isAir(),"and its walls are gone");
            h.assertTrue(stage(l,b,new BlockPos(FallowSite.FARM_DRIVE_X-1,1,FallowSite.FARM_S))==FallowBlock.JAMB&&stage(l,b,new BlockPos(FallowSite.FARM_DRIVE_X+1,1,FallowSite.FARM_S))==FallowBlock.JAMB&&stage(l,b,new BlockPos(FallowSite.FARM_DRIVE_X,2,FallowSite.FARM_S))==FallowBlock.LINTEL,"but for a charred scrap of the door's frame");
            h.assertTrue(!door[0]||l.getBlockState(b.offset(FallowSite.ONWARD)).getBlock() instanceof DoorBlock,"and the door in it, the way on");
            h.assertTrue(is(l,b,trunk,FallowRegistry.material("charred_log"))&&!l.getBlockState(b.offset(trunk.above(7))).is(Blocks.OAK_LEAVES),"the windbreak burnt standing");
            h.assertTrue(is(l,b,yard,FallowRegistry.material("scorched")),"the yard burnt black");
            q.setGameMode(GameType.SPECTATOR);
        })
        .thenWaitUntil(()->h.assertTrue(Fallow.afternoon(b)==null,"once the reader has gone, the afternoon is over"))
        .thenExecute(()->{
            h.assertTrue(is(l,b,field.below(),FallowRegistry.material("soil"))&&l.getBlockState(b.offset(field)).equals(FallowSite.wheatAt(field.getX(),field.getZ())),"the same wheat stands where it was sown");
            for(int x=FallowSite.WEST;x<=FallowSite.EAST;x+=7)for(int z=-40;z>=FallowSite.NORTH;z-=9){
                var st=l.getBlockState(b.offset(x,0,z));
                h.assertTrue(!FallowBlock.is(st,FallowBlock.Kind.WHEAT)||st.getValue(FallowBlock.STAGE)<FallowBlock.STUBBLE,"no burnt stubble is left: "+x+" "+z);
                h.assertTrue(!is(l,b,new BlockPos(x,-1,z),FallowRegistry.material("ash_ground")),"and no ash on the ground: "+x+" "+z);
            }
            h.assertTrue(is(l,b,new BlockPos(crater[0],-1,crater[1]),FallowRegistry.material("soil"))&&is(l,b,new BlockPos(crater[0],-4,crater[1]),Blocks.DIRT),"the opened ground is filled");
            for(int x:FallowAsh.POLE_X){
                h.assertTrue(fitting(l,b,new BlockPos(x,1,FallowSite.POLES_Z),FallowBlock.Kind.POLE)&&l.getBlockState(b.offset(x,0,FallowSite.POLES_Z+6)).isAir(),"the poles stand again and the road is clear: "+x);
            }
            h.assertTrue(l.getBlockState(b.offset(chimney)).isAir()&&is(l,b,wall,FallowRegistry.material("farm_siding")),"the farmhouse stands whole");
            h.assertTrue(!door[0]||l.getBlockState(b.offset(FallowSite.ONWARD)).getBlock() instanceof DoorBlock,"with its front door kept");
            h.assertTrue(is(l,b,trunk,Blocks.OAK_LOG)&&is(l,b,yard,Blocks.GRASS_BLOCK),"its windbreak grown and its yard in grass");
            h.assertTrue(!FallowAsh.burnt(l,b),"nothing of the light is left outside");
        })
        .thenExecute(()->{done();h.succeed();});
    }
    /** Turns a reader to look at a spot (relative to the station) with their eyes, as a client would. */
    private static void look(ServerPlayer p,BlockPos b,Vec3 rel){p.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,Vec3.atLowerCornerOf(b).add(rel));}
    /** Wheat at a stage within a ring of the bean: cells further than {@code from} and no further than {@code to} blocks from it. */
    private static boolean wheatRing(ServerLevel l,BlockPos b,BlockPos bean,double from,double to,int stage){
        int r=(int)Math.ceil(to);
        for(int x=-r;x<=r;x++)for(int z=-r;z<=r;z++){
            double d=Math.hypot(x,z);if(d<=from||d>to)continue;
            var st=l.getBlockState(b.offset(bean.offset(x,0,z)));
            if(FallowBlock.is(st,FallowBlock.Kind.WHEAT)&&st.getValue(FallowBlock.STAGE)==stage)return true;
        }
        return false;
    }

    /**
     * 0.4.683, the road (docs/FALLOW_0_4_683.md). One reader out in front at the light and one below; both keep the afternoon's mark. The
     * one out in it is taken out by the House, and the station is held for them; back, they are told the store has no roof, find their own
     * shadow on its front by hand (and, a second time, that it does not come off) and Boyd's, an arm up. Dug out on the third day, Otis and
     * Opal come up and she sits by the steps; a while after, she kneels and sets her bean down in its cup in the yard and sits by it, and
     * the ground comes up round her a ring at a time: shoots through the yard's dirt and through the ash (the ash going back to soil under
     * them), young wheat behind them. Some while after, the man is made out on the pads' track where nobody sees him and walks down to the
     * yard and stands looking at the cellar doors. Seen, somebody is coming down the road, and Otis (Faye is gone) takes Opal down the
     * steps; the green stops where it reached. Each time he is looked at again, out of everyone's sight between, he has another face:
     * Harlan's, Boyd's, Otis's, and last the reader's own; the reader who never looked at him keeps none of them. Spoken to, he looks past
     * the reader at the doors. When all have gone, a fresh afternoon takes the bean up, leaves the yard bare and puts the field back.
     */
    @GameTest(template="empty",batch="fallow_road_after",timeoutTicks=14000)
    public static void theRoad(GameTestHelper h){
        var f=place(h);var l=f.level(P);var b=f.base(P);Fallow.forget(b);
        // The station and the yard, and the field north to past where the man is made on the pads' track.
        f.chunks.hold(l,new AABB(b.getX()-40,b.getY()-8,b.getZ()-118,b.getX()+30,b.getY()+8,b.getZ()+1));
        var p=f.player("fallow_road_out",P);var q=f.player("fallow_road_below",P);
        var door=new BlockPos(FallowSite.WELL_E,0,FallowSite.WELL_S);var step=new BlockPos(-14,-2,-10);
        var heard=new ArrayList<String>();int[] boydX={Integer.MIN_VALUE},ringAt={0};BlockPos[] bean={null};
        var seq=h.startSequence().thenIdle(5).thenExecute(()->{
            f.at(p,P,3.5,0,-2.5);f.at(q,P,2.5,0,-2.5);
            for(var x:new ServerPlayer[]{p,q})f.data().pushReturn(x.getUUID(),new LabyrinthData.Waypoint(l.dimension(),Vec3.atBottomCenterOf(b.offset(0,0,24)),0F,false));
        }).thenWaitUntil(()->h.assertTrue(Fallow.afternoon(b)!=null&&Fallow.find(l,b,Fallow.GIRL)!=null&&Fallow.find(l,b,Fallow.DRIVER)!=null,"the afternoon is on"))
        .thenExecute(()->{l.setBlock(b.offset(Fallow.BEAN_AT),FallowBlock.of(FallowBlock.Kind.BEAN,Direction.WEST,0),3);FallowStrike.begin(b);})
        .thenWaitUntil(()->h.assertTrue(FallowStrike.since(b)>=1,"the tone comes on"))
        .thenExecute(()->FallowStrike.skip(b,FallowStrike.GO-1))
        .thenWaitUntil(()->h.assertTrue(FallowStrike.otis(b)==FallowStrike.OTIS_BELOW,"Otis carries Opal down: "+FallowStrike.otis(b)))
        .thenWaitUntil(()->h.assertTrue(FallowStrike.boyd(b)==FallowStrike.AT_RIG,"Boyd is at the rig's door: "+FallowStrike.boyd(b)))
        .thenExecute(()->{f.at(q,P,-12.5,-3,-13.5);f.at(p,P,-6.6,0,-13.5);FallowStrike.skip(b,FallowStrike.LIGHT-2);})
        .thenWaitUntil(()->h.assertTrue(FallowStrike.since(b)>=FallowStrike.LIGHT+2,"the light"))
        .thenExecute(()->{
            int mark=Fallow.mark(b);
            h.assertTrue(mark!=0&&f.own(p,P).getInt("Afternoon")==mark&&f.own(q,P).getInt("Afternoon")==mark,"both keep this afternoon's mark: "+mark+" "+f.own(p,P).getInt("Afternoon"));
            for(int x:FallowRuin.WALL)if(l.getBlockEntity(b.offset(x,0,FallowSite.STORE_N-1)) instanceof FallowShadowBlock.Shadow sh&&sh.person)boydX[0]=x;
            h.assertTrue(boydX[0]!=Integer.MIN_VALUE,"Boyd's shadow is on the front");
        })
        .thenWaitUntil(()->h.assertTrue(!FallowSite.inScene(p.position().subtract(Vec3.atLowerCornerOf(b)))||p.level()!=l,"the reader out in it is taken out by the House"))
        .thenExecute(()->h.assertTrue(FallowRoad.held(b),"the station is held for them while they have not come back"))
        // Back through the House's door, into the store under no roof.
        .thenExecute(()->{told(p);f.at(p,P,2.5,0,-6.5);})
        .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("CameBack"),"they are back"))
        .thenExecute(()->{
            heard.addAll(told(p));
            h.assertTrue(heard.contains(FallowTexts.ARRIVE_AFTER),"and told the store has no roof: "+heard);
            h.assertFalse(FallowRoad.held(b),"and the station is no longer held for anyone");
            // A hand on the wall where their shadow lies, twice; then on Boyd's.
            use(p,b,new BlockPos(-7,1,FallowSite.STORE_N),new BlockPos(-7,0,-15));
            heard.addAll(told(p));
            h.assertTrue(f.own(p,P).getBoolean("FoundShadow")&&heard.contains(FallowTexts.FOUND_OWN),"they find their own shadow: "+heard);
            use(p,b,new BlockPos(-7,1,FallowSite.STORE_N),new BlockPos(-7,0,-15));
            heard.addAll(told(p));
            h.assertTrue(heard.contains(FallowTexts.OWN_ON_HAND),"and it does not come off on the hand");
            use(p,b,new BlockPos(boydX[0],1,FallowSite.STORE_N),new BlockPos(boydX[0],0,-15));
            heard.addAll(told(p));
            h.assertTrue(heard.contains(FallowTexts.SHADOW_REACHING.formatted("Boyd")),"and Boyd's, an arm up: "+heard);
            h.assertTrue(f.own(p,P).getInt("OthersShadows")==0,"his is one of the five's, not another reader's");
            FallowBuried.skip(b,FallowBuried.DAY*FallowBuried.COOL_DAY+20);
        })
        .thenWaitUntil(()->h.assertTrue(FallowBuried.day(b)==FallowBuried.COOL_DAY,"the third day"))
        .thenExecute(()->{FallowBuried.dugTo(b,FallowBuried.DIG-1);use(q,b,door,step);})
        .thenWaitUntil(()->h.assertTrue(FallowBuried.open(b),"the doors give"))
        .thenExecute(()->{
            // Out of the way of the yard, both looking south, away from the road and the yard's gate.
            f.at(q,P,-18.5,0,-2.5);look(q,b,new Vec3(-18.5,1.6,20));
            f.at(p,P,5.5,0,-6.5);look(p,b,new Vec3(20,1.6,10));
        })
        .thenWaitUntil(()->{var girl=Fallow.find(l,b,Fallow.GIRL);h.assertTrue(girl!=null&&girl.gesture()==Fallow.SIT&&!FallowSite.inCellar(girl.position().subtract(Vec3.atLowerCornerOf(b))),"Opal comes up and sits by the steps");})
        .thenWaitUntil(()->h.assertTrue(FallowRoad.bean(b)!=null,"she sets her bean down"))
        .thenExecute(()->{
            bean[0]=FallowRoad.bean(b);var girl=Fallow.find(l,b,Fallow.GIRL);
            h.assertTrue(fitting(l,b,bean[0],FallowBlock.Kind.BEAN)&&girl.getMainHandItem().isEmpty(),"in its cup in the yard, out of her hands: "+bean[0]);
            h.assertTrue(girl.gesture()==Fallow.SIT&&said(girl).equals(FallowTexts.OPAL_SITS_WITH_IT),"and sits by it: "+said(girl));
        })
        .thenWaitUntil(()->h.assertTrue(FallowRoad.ring(b)>=5,"the ground comes up round her: "+FallowRoad.ring(b)))
        .thenExecute(()->{
            h.assertTrue(wheatRing(l,b,bean[0],0,1,FallowBlock.GREEN),"young wheat by the bean");
            h.assertTrue(wheatRing(l,b,bean[0],4,5,FallowBlock.SHOOT),"shoots at the edge");
        })
        .thenWaitUntil(()->h.assertTrue(FallowRoad.ring(b)>=18,"out past the yard: "+FallowRoad.ring(b)))
        .thenExecute(()->{
            // Seventeen blocks west of the bean is the field, burnt to ash; the shoots come up there through the ash, on soil again.
            var field=bean[0].offset(-17,0,0);var top=l.getBlockState(b.offset(field));
            h.assertTrue(field.getX()<FallowSite.YARD_W&&FallowBlock.is(top,FallowBlock.Kind.WHEAT)&&top.getValue(FallowBlock.STAGE)>=FallowBlock.SHOOT,"shoots in the field: "+top);
            h.assertTrue(is(l,b,field.below(),FallowRegistry.material("soil")),"the ash gone back to soil under them");
            h.assertTrue(FallowRoad.road(b)==FallowRoad.WALKING,"and somebody is on the road, where nobody saw him made: "+FallowRoad.road(b));
            var man=Fallow.find(l,b,Fallow.STRANGER);
            h.assertTrue(man!=null&&man.marks()==FallowRoad.OWN_FACE&&!FallowRoad.hid(b),"the man from the afternoon, with his own face, unseen");
            FallowRoad.hurry(b,.35);
        })
        .thenWaitUntil(()->h.assertTrue(FallowRoad.road(b)==FallowRoad.STOOD,"he comes down to the yard and stands: "+Fallow.find(l,b,Fallow.STRANGER).position().subtract(Vec3.atLowerCornerOf(b))))
        .thenExecute(()->{
            var man=Fallow.find(l,b,Fallow.STRANGER);var r=man.position().subtract(Vec3.atLowerCornerOf(b));
            h.assertTrue(r.distanceTo(new Vec3(-16.5,0,-9.5))<1.5,"west of the cellar doors: "+r);
            h.assertTrue(!FallowRoad.hid(b)&&f.own(p,P).getInt("SawFaces")==0,"and nobody has seen him yet");
            // The one who was out in it goes into the yard and looks at him.
            told(p);f.at(p,P,-10.5,0,-12.5);look(p,b,r.add(0,1.6,0));
        })
        .thenWaitUntil(()->h.assertTrue(FallowRoad.hid(b),"seen"))
        .thenExecute(()->{
            heard.clear();heard.addAll(told(p));
            h.assertTrue(heard.contains(FallowTexts.STRANGER_SEEN),"somebody is walking down the road: "+heard);
            var otis=Fallow.find(l,b,Fallow.DRIVER);
            h.assertTrue(said(otis).equals(FallowTexts.OTIS_TAKES_HER),"Otis, Faye being gone, takes Opal down: "+said(otis));
            ringAt[0]=FallowRoad.ring(b);
        })
        .thenWaitUntil(()->{var girl=Fallow.find(l,b,Fallow.GIRL);h.assertTrue(FallowSite.inCellar(girl.position().subtract(Vec3.atLowerCornerOf(b))),"down the steps");})
        .thenIdle(FallowRoad.GROW_RING*2)
        .thenExecute(()->{
            h.assertTrue(FallowRoad.ring(b)<=ringAt[0]+1,"the green stops where it reached: "+ringAt[0]+" "+FallowRoad.ring(b));
            h.assertTrue(fitting(l,b,bean[0],FallowBlock.Kind.BEAN),"her bean stays in the ground");
        });
        // Each time he is out of everyone's sight and looked at again, he has another face: Harlan's, Boyd's, Otis's, then the reader's own.
        String[] faces={FallowTexts.HIS_FACE.formatted("Harlan"),FallowTexts.HIS_FACE.formatted("Boyd"),FallowTexts.HIS_FACE.formatted("Otis"),FallowTexts.YOUR_FACE};
        for(int i=0;i<faces.length;i++){
            int face=i+1;String line=faces[i];
            seq=seq.thenExecute(()->look(p,b,new Vec3(30,1.6,-12.5)))
                .thenWaitUntil(()->h.assertTrue(FallowRoad.face(b)==face,"out of sight, his face changes: "+FallowRoad.face(b)))
                .thenExecute(()->{told(p);var man=Fallow.find(l,b,Fallow.STRANGER);
                    // The client draws the borrowed faces from the cast's own skins, and the last on the viewer's own body.
                    String worn=Fallow.dressed(man.phase(),man.marks());
                    h.assertTrue(face<FallowRoad.VIEWER?Fallow.skin(Fallow.FIRST+FallowRoad.faceOf(face)).equals(worn)&&!Fallow.viewersFace(man):worn==null&&Fallow.viewersFace(man),"drawn with that face: "+worn);
                    look(p,b,man.position().subtract(Vec3.atLowerCornerOf(b)).add(0,1.6,0));})
                .thenWaitUntil(()->h.assertTrue((f.own(p,P).getInt("SawFaces")&1<<face)!=0,"looked at again: "+f.own(p,P).getInt("SawFaces")))
                .thenExecute(()->{var got=told(p);h.assertTrue(got.contains(line),"he has another face: "+got);});
        }
        seq.thenExecute(()->{
            var man=Fallow.find(l,b,Fallow.STRANGER);
            h.assertTrue(man.marks()==FallowRoad.VIEWER&&f.own(p,P).getInt("SawFaces")==31,"the last is the face of whoever looks at him, and the reader kept each one");
            h.assertTrue(f.own(q,P).getInt("SawFaces")==0&&!f.own(q,P).getBoolean("CameBack"),"the one who never looked keeps none of it, and never went out to come back");
            // Beside him (a word reaches six blocks), and spoken to.
            f.at(p,P,-16.5,0,-6.5);told(p);LiteraryVignettes.talk(p,man);
            // 0.4.684: spoken to, he asks after the girl; said nothing, he looks past them at the doors.
            h.assertTrue(Talk.waiting(p,FallowSteps.ASK_TALK)&&said(man).equals(FallowTexts.WHERE_IS_SHE),"spoken to, he asks after the girl: "+said(man));
            Talk.choose(p,FallowTexts.SAY_NOTHING);
            h.assertTrue(told(p).contains(FallowTexts.LOOKS_PAST)&&f.own(p,P).getInt("RoadAnswer")==FallowSteps.SILENT,"said nothing, he looks past them at the doors");
            for(var x:new ServerPlayer[]{p,q})x.setGameMode(GameType.SPECTATOR);
        })
        .thenWaitUntil(()->h.assertTrue(Fallow.afternoon(b)==null,"once both have gone, the afternoon is over"))
        .thenExecute(()->{
            h.assertTrue(l.getBlockState(b.offset(bean[0])).isAir(),"the bean is taken up");
            h.assertFalse(wheatRing(l,b,bean[0],0,5,FallowBlock.GREEN)||wheatRing(l,b,bean[0],0,5,FallowBlock.SHOOT),"nothing of it is left in the yard");
            var field=bean[0].offset(-17,0,0);
            h.assertTrue(l.getBlockState(b.offset(field)).equals(FallowSite.wheatAt(field.getX(),field.getZ())),"and the field stands as it was sown");
            h.assertTrue(Fallow.find(l,b,Fallow.STRANGER)==null,"and the man is gone off the road");
        })
        .thenExecute(()->{done();h.succeed();});
    }

    // ------------------------------------------------------------------------------------------------ 0.4.684: what he asks
    /** Readers below at the light, dug out, the bean in the ground, and the man brought down the road unseen to stand at the doors. */
    private static GameTestSequence toTheMan(GameTestHelper h,LiteraryTests.Fixture f,ServerPlayer... readers){
        var l=f.level(P);var b=f.base(P);
        var door=new BlockPos(FallowSite.WELL_E,0,FallowSite.WELL_S);var step=new BlockPos(-14,-2,-10);
        return h.startSequence().thenIdle(5).thenExecute(()->{
            for(int i=0;i<readers.length;i++){f.at(readers[i],P,2.5+i,0,-2.5);f.data().pushReturn(readers[i].getUUID(),new LabyrinthData.Waypoint(l.dimension(),Vec3.atBottomCenterOf(b.offset(0,0,24)),0F,false));}
        }).thenWaitUntil(()->h.assertTrue(Fallow.afternoon(b)!=null&&Fallow.find(l,b,Fallow.GIRL)!=null&&Fallow.find(l,b,Fallow.DRIVER)!=null,"the afternoon is on"))
        .thenExecute(()->{l.setBlock(b.offset(Fallow.BEAN_AT),FallowBlock.of(FallowBlock.Kind.BEAN,Direction.WEST,0),3);FallowStrike.begin(b);})
        .thenWaitUntil(()->h.assertTrue(FallowStrike.since(b)>=1,"the tone comes on"))
        .thenExecute(()->FallowStrike.skip(b,FallowStrike.GO-1))
        .thenWaitUntil(()->h.assertTrue(FallowStrike.otis(b)==FallowStrike.OTIS_BELOW,"Otis carries Opal down: "+FallowStrike.otis(b)))
        .thenExecute(()->{for(int i=0;i<readers.length;i++)f.at(readers[i],P,-12.5+i,-3,-13.5);FallowStrike.skip(b,FallowStrike.LIGHT-2);})
        .thenWaitUntil(()->h.assertTrue(FallowStrike.since(b)>=FallowStrike.LIGHT+2,"the light"))
        .thenExecute(()->FallowBuried.skip(b,FallowBuried.DAY*FallowBuried.COOL_DAY+20))
        .thenWaitUntil(()->h.assertTrue(FallowBuried.day(b)==FallowBuried.COOL_DAY,"the third day"))
        .thenExecute(()->{FallowBuried.dugTo(b,FallowBuried.DIG-1);use(readers[0],b,door,step);})
        .thenWaitUntil(()->h.assertTrue(FallowBuried.open(b),"the doors give"))
        .thenExecute(()->{for(int i=0;i<readers.length;i++){f.at(readers[i],P,-18.5+i,0,-2.5);look(readers[i],b,new Vec3(-18.5+i,1.6,20));}})
        .thenWaitUntil(()->h.assertTrue(FallowRoad.bean(b)!=null,"Opal sets her bean down"))
        .thenExecute(()->{FallowRoad.comeNow(b);})
        .thenWaitUntil(()->h.assertTrue(FallowRoad.road(b)==FallowRoad.WALKING,"he is on the road"))
        .thenExecute(()->FallowRoad.hurry(b,.35))
        .thenWaitUntil(()->h.assertTrue(FallowRoad.road(b)==FallowRoad.STOOD,"he stands at the doors"))
        // The first reader goes into the yard and looks at him: Otis takes Opal down the steps.
        .thenExecute(()->{var man=Fallow.find(l,b,Fallow.STRANGER);f.at(readers[0],P,-10.5,0,-12.5);look(readers[0],b,man.position().subtract(Vec3.atLowerCornerOf(b)).add(0,1.6,0));})
        .thenWaitUntil(()->h.assertTrue(FallowRoad.hid(b),"seen"))
        .thenWaitUntil(()->{var girl=Fallow.find(l,b,Fallow.GIRL);h.assertTrue(FallowSite.inCellar(girl.position().subtract(Vec3.atLowerCornerOf(b)))&&!FallowSteps.walking(b,Fallow.GIRL),"Opal down the steps");});
    }
    private static String pages(ServerPlayer p,LiteraryTests.Fixture f){return LiteraryTexts.ending(p.getGameProfile().getName(),P,f.own(p,P)).get(DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().map(x->x.raw().getString()).reduce("",(x,y)->x+" "+y).replaceAll("\\s+"," ");}
    /** A reader near him, looking at him. */
    private static void faceHim(LiteraryTests.Fixture f,ServerPlayer p,double x,double z){
        var l=f.level(P);var b=f.base(P);var man=Fallow.find(l,b,Fallow.STRANGER);
        f.at(p,P,x,0,z);look(p,b,man.position().subtract(Vec3.atLowerCornerOf(b)).add(0,1.6,0));
    }

    /** 0.4.684: one reader lies to him from the steps, the other tells him where she is; he smiles at the one on the steps, says he'll find
     * her anyway and walks back up the road; she stays below; both stayed until he had gone, so both are ready, each with their own
     * account; a spectator watching the road keeps him there, and earns nothing. */
    @GameTest(template="empty",batch="fallow_steps",timeoutTicks=7000)
    public static void theStepsKeepHer(GameTestHelper h){
        var f=place(h);var l=f.level(P);var b=f.base(P);Fallow.forget(b);
        f.chunks.hold(l,new AABB(b.getX()-40,b.getY()-8,b.getZ()-118,b.getX()+30,b.getY()+8,b.getZ()+1));
        var p=f.player("fallow_steps_liar",P);var q=f.player("fallow_steps_teller",P);var r=f.player("fallow_steps_watcher",P);
        // A spectator from the start, out on the field's edge and looking away west, so the light and the road pass it by.
        r.setGameMode(GameType.SPECTATOR);f.at(r,P,-30.5,0,-30.5);look(r,b,new Vec3(-90,1.6,-30.5));
        toTheMan(h,f,p,q).thenExecute(()->{
            told(p);told(q);
            faceHim(f,p,-14.5,-5.5);
        })
        .thenWaitUntil(()->h.assertTrue(Talk.waiting(p,FallowSteps.ASK_TALK),"he asks the one near him"))
        .thenExecute(()->{
            var man=Fallow.find(l,b,Fallow.STRANGER);
            h.assertTrue(said(man).equals(FallowTexts.WHERE_IS_SHE),"where's the little one: "+said(man));
            h.assertTrue(Talk.lines(p).stream().anyMatch(x->x.contains(FallowTexts.RIDE)),"and offers a ride: "+Talk.lines(p));
            h.assertTrue(Talk.replies(p).equals(List.of(FallowTexts.NOBODY_DOWN_THERE,FallowTexts.DOWN_THE_STEPS,FallowTexts.SAY_NOTHING)),"three answers: "+Talk.replies(p));
            h.assertTrue(told(p).contains(FallowTexts.RIG_TURNS),"the rig turns over");
            h.assertFalse(Talk.waiting(r),"the spectator is asked nothing");
            Talk.choose(p,FallowTexts.NOBODY_DOWN_THERE);
            h.assertTrue(said(man).equals(FallowTexts.IS_THAT_RIGHT)&&f.own(p,P).getInt("RoadAnswer")==FallowSteps.LIED,"the lie: "+said(man));
            // The one who lied stands on the steps; the other comes and tells him the truth.
            f.at(p,P,-13.5,-1,-8.5);
            faceHim(f,q,-18.5,-6.5);
        })
        .thenWaitUntil(()->h.assertTrue(Talk.waiting(q,FallowSteps.ASK_TALK),"he asks the second"))
        .thenExecute(()->{
            Talk.choose(q,FallowTexts.DOWN_THE_STEPS);
            h.assertTrue(said(Fallow.find(l,b,Fallow.STRANGER)).equals(FallowTexts.OBLIGED)&&f.own(q,P).getInt("RoadAnswer")==FallowSteps.TOLD,"the truth");
        })
        .thenWaitUntil(()->h.assertTrue(FallowSteps.steps(b)==FallowSteps.WALKS_OFF,"he comes to the steps, and somebody is on them: "+FallowSteps.steps(b)))
        .thenExecute(()->{
            var man=Fallow.find(l,b,Fallow.STRANGER);
            h.assertTrue(FallowSteps.kept(b)&&said(man).equals(FallowTexts.FIND_HER_ANYWAY),"he'll find her anyway: "+said(man));
            h.assertTrue(told(p).contains(FallowTexts.HE_SMILES)&&f.own(p,P).getBoolean("OnSteps")&&!f.own(q,P).getBoolean("OnSteps"),"he smiles at the one on the steps");
            h.assertTrue(told(q).contains(FallowTexts.WALKS_BACK),"the other sees him go");
            FallowRoad.hurry(b,.35);
            for(var x:new ServerPlayer[]{p,q})look(x,b,x.position().subtract(Vec3.atLowerCornerOf(b)).add(0,1.6,30));
            // The spectator stands on the far pad's track and watches up it, the way he goes.
            f.at(r,P,20.5,0,-25.5);look(r,b,new Vec3(20.5,1.6,-90));
        })
        .thenIdle(400)
        .thenExecute(()->h.assertTrue(FallowSteps.steps(b)==FallowSteps.WALKS_OFF&&Fallow.find(l,b,Fallow.STRANGER)!=null,"a spectator watching keeps him there: "+FallowSteps.steps(b)))
        .thenExecute(()->look(r,b,new Vec3(-30.5,1.6,60)))
        .thenWaitUntil(()->h.assertTrue(FallowSteps.steps(b)==FallowSteps.GONE&&Fallow.find(l,b,Fallow.STRANGER)==null,"gone up the road"))
        .thenExecute(()->{
            var girl=Fallow.find(l,b,Fallow.GIRL);
            h.assertTrue(FallowSite.inCellar(girl.position().subtract(Vec3.atLowerCornerOf(b)))&&!FallowSteps.inField(b),"she is still below");
            var own=f.own(p,P);h.assertTrue(own.getBoolean("Ready")&&own.getString("Outcome").equals("kept_her_on_the_steps"),"the one on the steps: "+own);
            var two=f.own(q,P);h.assertTrue(two.getBoolean("Ready")&&two.getString("Outcome").equals("told_him_where_she_was"),"the one who told: "+two);
            h.assertTrue(!f.own(r,P).getBoolean("Ready")&&f.own(r,P).getInt("RoadAnswer")==0,"the spectator earns nothing");
            h.assertTrue(told(q).contains(FallowTexts.ROAD_EMPTY),"the road is empty");
            var text=pages(p,f);
            h.assertTrue(text.contains("I told him there was nobody down there.")&&text.contains("I stood on the steps.")&&text.contains("She was still down the steps when I left."),"the liar's account: "+text);
            h.assertFalse(text.contains("I woke at the House's door"),"and not what they did not: "+text);
            var other=pages(q,f);
            h.assertTrue(other.contains("I told him she was down the steps.")&&other.contains("Somebody was standing on the steps."),"the other's: "+other);
            f.source(p,P);f.read(p,P,FallowSite.ENDING);
            h.assertTrue(f.own(p,P).getBoolean("Completed")&&WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.FALLOW),"read after the note, it is theirs");
            for(var x:new ServerPlayer[]{p,q})x.setGameMode(GameType.SPECTATOR);
        })
        .thenWaitUntil(()->h.assertTrue(Fallow.afternoon(b)==null,"the afternoon ends"))
        .thenExecute(()->{done();h.succeed();});
    }

    /** 0.4.684: alone, the reader says nothing and nobody stands on the steps. He goes down, and she is not there: out past the fence in
     * the field, sitting, the wheat coming up round her. He stands at the fence looking at her, and is gone when nobody sees him. */
    @GameTest(template="empty",batch="fallow_field",timeoutTicks=7000)
    public static void theFieldTakesHer(GameTestHelper h){
        var f=place(h);var l=f.level(P);var b=f.base(P);Fallow.forget(b);
        f.chunks.hold(l,new AABB(b.getX()-45,b.getY()-8,b.getZ()-118,b.getX()+30,b.getY()+8,b.getZ()+1));
        var p=f.player("fallow_field_alone",P);
        toTheMan(h,f,p).thenExecute(()->{told(p);faceHim(f,p,-16.5,-4.5);})
        .thenWaitUntil(()->h.assertTrue(Talk.waiting(p,FallowSteps.ASK_TALK),"he asks"))
        .thenExecute(()->{
            Talk.choose(p,FallowTexts.SAY_NOTHING);
            h.assertTrue(f.own(p,P).getInt("RoadAnswer")==FallowSteps.SILENT&&told(p).contains(FallowTexts.LOOKS_PAST),"nothing; he looks past them");
            f.at(p,P,-19.5,0,-2.5);
        })
        .thenWaitUntil(()->h.assertTrue(FallowSteps.steps(b)==FallowSteps.DOWN,"nobody on the steps: he goes down: "+FallowSteps.steps(b)))
        .thenExecute(()->{h.assertTrue(told(p).contains(FallowTexts.GOES_DOWN),"told");look(p,b,new Vec3(-19.5,1.6,30));})
        .thenWaitUntil(()->h.assertTrue(FallowSteps.inField(b),"she is gone out of the cellar"))
        .thenExecute(()->{
            var girl=Fallow.find(l,b,Fallow.GIRL);var r=girl.position().subtract(Vec3.atLowerCornerOf(b));
            h.assertTrue(r.x<FallowSite.YARD_W&&girl.gesture()==Fallow.SIT,"out past the fence, sitting: "+r);
        })
        // At the foot of the steps he finds nobody, and Otis, who took her down, never saw her go.
        .thenWaitUntil(()->h.assertTrue(FallowSteps.steps(b)>=FallowSteps.UP,"he comes back up: "+FallowSteps.steps(b)))
        .thenExecute(()->h.assertTrue(said(Fallow.find(l,b,Fallow.DRIVER)).equals(FallowTexts.OTIS_GONE),"Otis never saw her go: "+said(Fallow.find(l,b,Fallow.DRIVER))))
        .thenWaitUntil(()->h.assertTrue(FallowSteps.steps(b)==FallowSteps.AT_FENCE,"he comes up and stands at the fence: "+FallowSteps.steps(b)))
        .thenExecute(()->{
            h.assertTrue(told(p).contains(FallowTexts.AT_THE_FENCE),"told");
            var man=Fallow.find(l,b,Fallow.STRANGER);
            h.assertTrue(man.position().subtract(Vec3.atLowerCornerOf(b)).distanceTo(new Vec3(-25.5,0,-9.5))<1.5,"at the fence: "+man.position().subtract(Vec3.atLowerCornerOf(b)));
            look(p,b,man.position().subtract(Vec3.atLowerCornerOf(b)).add(0,1.6,0));
        })
        .thenWaitUntil(()->h.assertTrue(FallowRoad.ring(b)>=3,"the ground comes up round her: "+FallowRoad.ring(b)))
        .thenExecute(()->{
            var girl=Fallow.find(l,b,Fallow.GIRL);var at=BlockPos.containing(girl.position().subtract(Vec3.atLowerCornerOf(b)));
            h.assertTrue(wheatRing(l,b,at,0,1,FallowBlock.GREEN)||wheatRing(l,b,at,2,3,FallowBlock.SHOOT),"round her in the field");
            // Over the fence and beside her (a word reaches six blocks), and spoken to.
            f.at(p,P,-30.5,0,-12.5);LiteraryVignettes.talk(p,girl);
            h.assertTrue(said(girl).equals(FallowTexts.CAME_UP_FOR_ME),"it came up for her: "+said(girl));
            var man=Fallow.find(l,b,Fallow.STRANGER);look(p,b,man.position().subtract(Vec3.atLowerCornerOf(b)).add(0,1.6,0));
        })
        .thenIdle(FallowSteps.LOOKS)
        .thenExecute(()->h.assertTrue(FallowSteps.steps(b)==FallowSteps.AT_FENCE,"while he is looked at, he stays"))
        .thenExecute(()->look(p,b,new Vec3(-60,1.6,-12.5)))
        .thenWaitUntil(()->h.assertTrue(FallowSteps.steps(b)==FallowSteps.GONE,"gone from the fence"))
        .thenExecute(()->{
            var own=f.own(p,P);h.assertTrue(own.getBoolean("Ready")&&own.getString("Outcome").equals("let_him_look"),"ready: "+own);
            h.assertTrue(told(p).contains(FallowTexts.FENCE_EMPTY),"nobody at the fence");
            var text=pages(p,f);
            h.assertTrue(text.contains("I said nothing.")&&text.contains("She was out past the fence")&&text.contains("it was still coming up."),"the account: "+text);
            p.setGameMode(GameType.SPECTATOR);
        })
        .thenWaitUntil(()->h.assertTrue(Fallow.afternoon(b)==null,"the afternoon ends"))
        .thenExecute(()->{done();h.succeed();});
    }
}
