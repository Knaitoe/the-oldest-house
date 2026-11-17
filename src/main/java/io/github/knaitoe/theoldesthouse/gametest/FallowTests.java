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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.GameType;
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
            for(int i=0;i<4;i++)h.assertTrue(fitting(l,b,new BlockPos(-2+i,FallowSite.CEILING+2,FallowSite.STORE_N),FallowBlock.Kind.SIGNBAND)&&stage(l,b,new BlockPos(-2+i,FallowSite.CEILING+2,FallowSite.STORE_N))==i,"the name along the front of the roof, piece "+i);
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
}
