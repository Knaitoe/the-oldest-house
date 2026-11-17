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
            h.assertFalse(LiteraryVignettes.canDeal(f.data(),p.getUUID(),P)||Fallow.dealable(f.data(),java.util.UUID.randomUUID()),"the House does not deal it yet");
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
        var f=place(h);var p=f.player("fallow_people_reader",P);var peer=f.player("fallow_people_peer",P);var l=f.level(P);var b=f.base(P);
        h.startSequence().thenIdle(5).thenExecute(()->Fallow.ensure(l,b)).thenWaitUntil(()->{
            for(int who=0;who<Fallow.PEOPLE;who++)h.assertTrue(Fallow.find(l,b,who)!=null,"the "+who+"th is at their place");
        }).thenExecute(()->{
            Fallow.ensure(l,b);
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
}
