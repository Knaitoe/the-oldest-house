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
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * 0.4.667, Last Seen (docs/LAST_SEEN_0_4_667.md). Each case builds the place itself, one case to a batch, as Not Yet's cases do.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_last_seen") @PrefixGameTestTemplate(false)
public final class LastSeenTests {
    private LastSeenTests(){}
    private static final LabyrinthPlace P=LabyrinthPlace.LAST_SEEN;
    private static LiteraryTests.Fixture active;
    private static LiteraryTests.Fixture place(GameTestHelper h){done();active=new LiteraryTests.Fixture(h);return active;}
    private static void done(){if(active!=null){active.close();active=null;}}
    private static boolean is(ServerLevel l,BlockPos b,BlockPos at,net.minecraft.world.level.block.Block block){return l.getBlockState(b.offset(at)).is(block);}
    private static boolean fitting(ServerLevel l,BlockPos b,BlockPos at,LastSeenBlock.Kind kind){return LastSeenBlock.is(l.getBlockState(b.offset(at)),kind);}
    private static int stage(ServerLevel l,BlockPos b,BlockPos at){return l.getBlockState(b.offset(at)).getValue(LastSeenBlock.STAGE);}
    private static List<String> told(ServerPlayer p){
        NativeTestPlayers.flush(p);
        var out=new ArrayList<String>();
        for(var packet:NativeTestPlayers.packets(p))if(packet instanceof net.minecraft.network.protocol.game.ClientboundSystemChatPacket chat)out.add(chat.content().getString());
        return out;
    }
    /** A hand on a thing, the reader stood at a spot of their own within reach of it, as a client would send it. */
    private static void use(ServerPlayer p,BlockPos b,BlockPos rel,BlockPos stand){
        var at=b.offset(rel);
        p.moveTo(b.getX()+stand.getX()+.5,b.getY()+stand.getY(),b.getZ()+stand.getZ()+.5);p.setDeltaMovement(Vec3.ZERO);
        NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(p,InteractionHand.MAIN_HAND,at,new BlockHitResult(at.getCenter(),Direction.UP,at,false)));
    }
    private static double feet(ServerLevel l,BlockPos b,BlockPos cell){
        var at=b.offset(cell);var here=l.getBlockState(at).getCollisionShape(l,at);
        double f;
        if(here.isEmpty()){var below=l.getBlockState(at.below()).getCollisionShape(l,at.below());if(below.isEmpty())return Double.NaN;f=at.getY()-1+below.max(Direction.Axis.Y);if(f<at.getY()-.51)return Double.NaN;}
        else{double top=here.max(Direction.Axis.Y);if(top>1.01)return Double.NaN;f=at.getY()+top;}
        var body=new AABB(at.getX()+.2,f+.01,at.getZ()+.2,at.getX()+.8,f+1.79,at.getZ()+.8);
        return l.noCollision(body)?f:Double.NaN;
    }
    /** Every cell a body can walk to from a start without opening anything, up or down a block at a time, within a box. */
    private static Set<BlockPos> walked(ServerLevel l,BlockPos b,BlockPos from,int x0,int z0,int x1,int z1){
        var seen=new HashSet<BlockPos>();var queue=new ArrayDeque<BlockPos>();seen.add(from);queue.add(from);
        while(!queue.isEmpty()){
            var at=queue.poll();double f=feet(l,b,at);if(Double.isNaN(f))continue;
            for(var d:Direction.Plane.HORIZONTAL)for(int dy=-1;dy<=1;dy++){
                var to=at.relative(d).above(dy);
                if(seen.contains(to)||to.getX()<x0||to.getX()>x1||to.getZ()<z0||to.getZ()>z1||to.getY()<-2||to.getY()>3)continue;
                double g=feet(l,b,to);if(Double.isNaN(g)||g-f>1.01)continue;
                var a=b.offset(at);var c=b.offset(to);double y=Math.max(f,g);
                var edge=new AABB((a.getX()+c.getX())/2.0+.2,y+.01,(a.getZ()+c.getZ())/2.0+.2,(a.getX()+c.getX())/2.0+.8,y+1.79,(a.getZ()+c.getZ())/2.0+.8);
                if(!l.noCollision(edge))continue;
                seen.add(to);queue.add(to);
            }
        }
        seen.removeIf(c->Double.isNaN(feet(l,b,c)));
        return seen;
    }
    private static String said(LiteraryActor a){return a.getCustomName()==null?"":a.getCustomName().getString();}
    private static void open(ServerLevel l,BlockPos b,BlockPos door){var at=b.offset(door);l.setBlock(at,l.getBlockState(at).setValue(DoorBlock.OPEN,true),2);l.setBlock(at.above(),l.getBlockState(at.above()).setValue(DoorBlock.OPEN,true),2);}

    /**
     * The office, the archive and the park, built of their own things. The office's floor, render and roof; the counter across the front
     * with its flap and the card on it; sixteen posters on the partition, the first ten the ten children by name; shelf bays to the
     * ceiling on the archive's three walls, archive boxes on top; the long table and Nora's desk, its drawer locked. The park: paving down
     * the cross of paths with curbs where it meets the grass, the dry fountain, the railing all round and its north gate shut. A body
     * walks from the House's door past the counter's end to the archive and the office's park door, and from the park door to the
     * fountain, the playground and up onto the bandstand, but not out of the gate until a hand opens it; then on across the avenue to
     * the way on. The three sit in their own chairs, one each however often they are looked for, and stay sat with a reader by them. Every
     * thing answers a hand, the three included, each reader hearing the three's first lines for themselves. The card is the office's own
     * paper and fits a book. It is nine in the morning there. A bay of folders gives its letter; the day's pile gives a folder.
     * The House deals it from depth eight.
     */
    @GameTest(template="empty",batch="last_seen_place",timeoutTicks=1800)
    public static void theOfficeTheArchiveAndThePark(GameTestHelper h){
        var f=place(h);var p=f.player("last_seen_reader",P);var peer=f.player("last_seen_peer",P);var l=f.level(P);var b=f.base(P);
        h.startSequence().thenIdle(5).thenExecute(()->{
            h.assertTrue(is(l,b,new BlockPos(0,-1,-3),LastSeenRegistry.material("floor"))&&is(l,b,new BlockPos(LastSeenSite.OFFICE_E,2,-2),LastSeenRegistry.material("render"))
                &&is(l,b,new BlockPos(0,LastSeenSite.CEILING,-3),LastSeenRegistry.material("ceiling"))&&is(l,b,new BlockPos(LastSeenSite.OFFICE_E,0,-2),LastSeenRegistry.material("plinth")),"the office: its own floor, render over a plinth, its roof");
            for(int x=-7;x<=5;x++)h.assertTrue(fitting(l,b,new BlockPos(x,0,LastSeenSite.COUNTER_Z),LastSeenBlock.Kind.COUNTER),"the counter across the front at "+x);
            h.assertTrue(fitting(l,b,new BlockPos(6,0,LastSeenSite.COUNTER_Z),LastSeenBlock.Kind.COUNTER)&&stage(l,b,new BlockPos(6,0,LastSeenSite.COUNTER_Z))==1,"its flap at the east end");
            h.assertTrue(fitting(l,b,LastSeenSite.CARD,LastSeenBlock.Kind.CARD)&&LiteraryRooms.source(P).equals(LastSeenSite.CARD),"the card on the counter is the first paper");
            h.assertTrue(LastSeenSite.POSTERS.size()==16,"sixteen posters");
            for(int i=0;i<LastSeenSite.POSTERS.size();i++){var at=LastSeenSite.POSTERS.get(i);h.assertTrue(fitting(l,b,at,i<8?LastSeenBlock.Kind.POSTER:LastSeenBlock.Kind.POSTER_B)&&stage(l,b,at)==i%8,"poster "+i+" on the partition");}
            for(var bay:LastSeenSite.SHELVES){
                for(int y=0;y<LastSeenSite.CEILING-1;y++)h.assertTrue(fitting(l,b,bay.above(y),LastSeenBlock.Kind.SHELF),"a shelf bay at "+bay.above(y).toShortString());
                h.assertTrue(stage(l,b,bay.above(LastSeenSite.CEILING-1))==LastSeenBlock.BOXES,"archive boxes on top of "+bay.toShortString());
            }
            h.assertTrue(fitting(l,b,LastSeenSite.DESK,LastSeenBlock.Kind.DESK)&&stage(l,b,LastSeenSite.DESK)==LastSeenBlock.LOCKED&&LiteraryRooms.ending(P).equals(LastSeenSite.DESK),"Nora's desk, its drawer locked, where the account will be");
            h.assertTrue(fitting(l,b,new BlockPos(-19,0,-7),LastSeenBlock.Kind.TABLE)&&stage(l,b,new BlockPos(-19,0,-7))==2&&stage(l,b,new BlockPos(-17,0,-7))==1,"the long table, its lamp and the day's pile");
            for(var at:new BlockPos[]{LastSeenSite.NORA_AT,LastSeenSite.PATRICIA_AT,LastSeenSite.LUZ_AT})h.assertTrue(fitting(l,b,at,LastSeenBlock.Kind.CHAIR),"a chair at "+at.toShortString());
            h.assertTrue(is(l,b,new BlockPos(LastSeenSite.PATH_X,-1,-30),LastSeenRegistry.material("paving"))&&is(l,b,new BlockPos(LastSeenSite.PATH_X-2,-1,-30),LastSeenRegistry.material("curb")),"paving down the path, a curb where it meets the grass");
            h.assertTrue(fitting(l,b,LastSeenSite.FOUNTAIN,LastSeenBlock.Kind.FOUNTAIN)&&stage(l,b,LastSeenSite.FOUNTAIN.above())==3&&l.getBlockState(b.offset(LastSeenSite.FOUNTAIN.offset(2,0,0))).isAir(),"the fountain's plinth and bowl, and the basin dry");
            for(var leaf:List.of(LastSeenSite.GATE_W,LastSeenSite.GATE_E))h.assertTrue(fitting(l,b,leaf,LastSeenBlock.Kind.GATE)&&stage(l,b,leaf)==LastSeenBlock.SHUT,"the north gate shut");
            h.assertTrue(is(l,b,new BlockPos(0,-1,LastSeenSite.ROAD_S),LastSeenRegistry.material("asphalt"))&&is(l,b,new BlockPos(0,-1,LastSeenSite.NEAR_CURB),LastSeenRegistry.material("curb")),"the avenue past the railing, curbed");
            h.assertTrue(is(l,b,new BlockPos(0,-1,(LastSeenSite.ROAD_S+LastSeenSite.ROAD_N)/2),LastSeenRegistry.material("road_line"))&&is(l,b,new BlockPos(LastSeenSite.GATE_E.getX(),-1,LastSeenSite.ROAD_N+1),LastSeenRegistry.material("crossing")),"a dashed line down the road, and a crossing from the gate");
            // The houses across the avenue: each its own render, stone at its corners, a plinth along its foot; tall windows set in the wall.
            int front=LastSeenSite.HOUSE_FRONT;
            h.assertTrue(is(l,b,new BlockPos(-24,3,front),LastSeenRegistry.material("stone"))&&is(l,b,new BlockPos(-23,0,front),LastSeenRegistry.material("plinth"))
                &&is(l,b,new BlockPos(-30,3,front),LastSeenRegistry.material("house_b"))&&is(l,b,new BlockPos(-20,3,front),LastSeenRegistry.material("house")),"two houses side by side, each its own render, stone between them");
            var low=new BlockPos(-22,1,front);
            h.assertTrue(fitting(l,b,low,LastSeenBlock.Kind.SHUTTER)&&stage(l,b,low)==2&&stage(l,b,low.above())==3&&fitting(l,b,low.above(4),LastSeenBlock.Kind.SHUTTER),"a tall window on each storey, its blind down");
            h.assertFalse(l.getBlockState(b.offset(low)).getCollisionShape(l,b.offset(low)).isEmpty(),"a window is part of its wall: nothing walks into it");
            var side=new BlockPos(LastSeenSite.WEST,5,LastSeenSite.HOUSE_FRONT+4);
            h.assertTrue(fitting(l,b,side,LastSeenBlock.Kind.SHUTTER)&&l.getBlockState(b.offset(side)).getValue(LastSeenBlock.FACING)==net.minecraft.core.Direction.EAST,"the houses' sides at the avenue's end have windows, looking in");
            h.assertTrue(f.data().door(OnwardDoors.id(P))!=null||OnwardDoors.install(l,b,P),"the way on goes in where it was placed, a house door across the avenue");
            // Walking. Inside the office: past the counter's end, through the partition, to both park doors.
            var office=walked(l,b,new BlockPos(0,0,-1),LastSeenSite.OFFICE_W,LastSeenSite.OFFICE_N,LastSeenSite.OFFICE_E,0);
            h.assertTrue(office.contains(new BlockPos(7,0,LastSeenSite.COUNTER_Z))&&office.contains(new BlockPos(0,0,-12)),"behind the counter by its end: "+office.size());
            h.assertTrue(office.contains(new BlockPos(-16,0,-10))&&office.contains(new BlockPos(-12,0,-6)),"into the archive through the partition");
            h.assertFalse(office.stream().anyMatch(c->c.getZ()<=LastSeenSite.OFFICE_N),"not through a shut door");
            // The park from its front door: the paths, the plaza, the playground, the bandstand; not past the shut gate.
            open(l,b,LastSeenSite.FRONT_DOOR);
            var park=walked(l,b,new BlockPos(0,0,-12),LastSeenSite.WEST,LastSeenSite.NORTH,LastSeenSite.EAST,0);
            h.assertTrue(park.contains(new BlockPos(0,0,-16))&&park.contains(new BlockPos(-8,0,-45))&&park.contains(new BlockPos(10,0,-30)),"out of the door, down the path to the fountain, into the playground: "+park.size());
            h.assertTrue(park.contains(new BlockPos(-28,1,-62)),"up onto the bandstand by its steps");
            h.assertFalse(park.stream().anyMatch(c->c.getZ()<=LastSeenSite.PARK_N),"not out of the shut gate");
            LastSeen.ensure(l,b);
        }).thenWaitUntil(()->{
            for(int who=0;who<LastSeen.PEOPLE;who++)h.assertTrue(LastSeen.find(l,b,who)!=null,"the "+who+"th is at their place");
        }).thenExecute(()->{
            LastSeen.ensure(l,b);
            var seats=new BlockPos[]{LastSeenSite.NORA_AT,LastSeenSite.PATRICIA_AT,LastSeenSite.LUZ_AT};
            for(int who=0;who<LastSeen.PEOPLE;who++){
                int phase=LastSeen.FIRST+who;var at=b.offset(seats[who]);
                var them=l.getEntitiesOfClass(LiteraryActor.class,new AABB(at).inflate(6),a->LastSeen.is(a)&&a.phase()==phase);
                h.assertTrue(them.size()==1,"one of each of the three, however often they are looked for: "+who+" "+them.size());
                var a=them.get(0);var seat=ActorSeat.of(a);
                h.assertTrue(a.blockPosition().equals(at)&&seat!=null&&seat.seatHeight()==10&&seat.facing()==l.getBlockState(at).getValue(LastSeenBlock.FACING),"sat in their own chair, turned the way it faces: "+a.position()+" "+seat);
            }
            // Readers in the office, close enough for the three's own ticks to run.
            f.at(p,P,.5,0,-2.5);f.at(peer,P,1.5,0,-2.5);told(p);told(peer);
        }).thenIdle(40).thenExecute(()->{
            var seats=new BlockPos[]{LastSeenSite.NORA_AT,LastSeenSite.PATRICIA_AT,LastSeenSite.LUZ_AT};
            for(int who=0;who<LastSeen.PEOPLE;who++){var a=LastSeen.find(l,b,who);h.assertTrue(a!=null&&a.blockPosition().equals(b.offset(seats[who])),"with readers by them they stay sat: "+a);}
            // Everything answers a hand.
            var office=new BlockPos(0,0,-3);var behind=new BlockPos(2,0,-9);var archive=new BlockPos(-16,0,-10);
            use(p,b,new BlockPos(0,0,LastSeenSite.COUNTER_Z),office);use(p,b,new BlockPos(6,0,LastSeenSite.COUNTER_Z),new BlockPos(5,0,-3));
            use(p,b,LastSeenSite.PATRICIA_AT,behind);use(p,b,LastSeenSite.POSTERS.get(0),new BlockPos(-6,0,-3));use(p,b,LastSeenSite.POSTERS.get(9),new BlockPos(-6,0,-9));
            var corner=new BlockPos(-21,0,-2);use(p,b,LastSeenSite.SHELVES.get(1),corner);use(p,b,LastSeenSite.SHELVES.get(0).above(LastSeenSite.CEILING-1),corner);
            use(p,b,new BlockPos(-16,0,-7),archive);use(p,b,new BlockPos(-17,0,-7),archive);use(p,b,new BlockPos(-19,0,-7),archive);
            use(p,b,LastSeenSite.DESK,new BlockPos(-10,0,-6));use(p,b,new BlockPos(3,1,LastSeenSite.OFFICE_N),behind);use(p,b,new BlockPos(-2,LastSeenSite.CEILING-1,-3),office);
            use(p,b,new BlockPos(-5,0,-1),office);
            var front=new BlockPos(-8,0,-18);var plaza=new BlockPos(-8,0,-45);
            use(p,b,new BlockPos(LastSeenSite.PATH_X-2,3,-22),front);use(p,b,new BlockPos(-5,0,-50),plaza);use(p,b,LastSeenSite.FOUNTAIN.above(),plaza);
            use(p,b,new BlockPos(-24,0,-30),new BlockPos(-22,0,-30));use(p,b,new BlockPos(-4,0,-58),new BlockPos(-6,0,-56));
            use(p,b,new BlockPos(11,1,-27),new BlockPos(11,0,-30));use(p,b,new BlockPos(17,0,-34),new BlockPos(15,0,-33));use(p,b,new BlockPos(17,0,-31),new BlockPos(15,0,-31));
            use(p,b,new BlockPos(8,0,-34),new BlockPos(10,0,-32));use(p,b,new BlockPos(6,0,-30),new BlockPos(8,0,-30));
            use(p,b,new BlockPos(-24,1,-60),new BlockPos(-21,0,-60));
            use(p,b,new BlockPos(-39,0,LastSeenSite.PARK_N),new BlockPos(-37,0,-82));use(p,b,new BlockPos(-40,0,LastSeenSite.PARK_N+4),new BlockPos(-37,0,-80));
            // The gate: a hand opens it, and then the avenue, the stop and the way on are there to walk to.
            use(p,b,LastSeenSite.GATE_W,new BlockPos(-9,0,LastSeenSite.PARK_N+2));
            // The three: each reader hears each one's first line first, then the second, counted for themselves.
            // Each reader stands across from them (Nora at her desk, the other two over the counter), as a hand on them would come.
            var across=new BlockPos[]{new BlockPos(-10,0,-6),new BlockPos(-3,0,-4),new BlockPos(3,0,-4)};
            for(int who=0;who<LastSeen.PEOPLE;who++){
                var a=LastSeen.find(l,b,who);var at=across[who];
                f.at(p,P,at.getX()+.5,0,at.getZ()+.5);LiteraryVignettes.talk(p,a);h.assertTrue(said(a).equals(LastSeenTexts.line(who,0)),"the first reader hears "+LastSeenTexts.NAMES[who]+"'s first line: "+said(a));
                f.at(peer,P,at.getX()+.5,0,at.getZ()+1.5);LiteraryVignettes.talk(peer,a);h.assertTrue(said(a).equals(LastSeenTexts.line(who,0)),"and so does the second, for themselves: "+said(a));
                f.at(p,P,at.getX()+.5,0,at.getZ()+.5);LiteraryVignettes.talk(p,a);h.assertTrue(said(a).equals(LastSeenTexts.line(who,1))&&!LastSeenTexts.line(who,1).equals(LastSeenTexts.line(who,0)),"the first, asking again, hears the second: "+said(a));
                h.assertTrue(a.speaker().equals(LastSeenTexts.NAMES[who]),"spoken under their own name: "+a.speaker());
            }
        }).thenIdle(2).thenExecute(()->{
            for(var leaf:List.of(LastSeenSite.GATE_W,LastSeenSite.GATE_E))h.assertTrue(stage(l,b,leaf)==LastSeenBlock.OPEN,"both leaves swing open");
            var avenue=walked(l,b,new BlockPos(-8,0,LastSeenSite.PARK_N+2),LastSeenSite.WEST,LastSeenSite.NORTH,LastSeenSite.EAST,0);
            h.assertTrue(avenue.contains(new BlockPos(-8,0,LastSeenSite.ROAD_S))&&avenue.contains(new BlockPos(14,0,LastSeenSite.NEAR_PAVE-1))
                &&avenue.contains(new BlockPos(LastSeenSite.ONWARD.getX(),0,LastSeenSite.HOUSE_FRONT+1)),"through the gate, across the avenue to the stop and the way on: "+avenue.size());
            var why=told(p);
            for(String line:List.of(LastSeenTexts.COUNTER,LastSeenTexts.FLAP,LastSeenTexts.CHAIR,LastSeenTexts.poster(0),LastSeenTexts.poster(9),LastSeenTexts.spines(LastSeen.letter(1)),LastSeenTexts.BOXES,
                LastSeenTexts.TABLE,LastSeenTexts.fromPile(LastSeen.PILE[0]),LastSeenTexts.LAMP,LastSeenTexts.DESK,LastSeenTexts.LOUVRE,LastSeenTexts.TUBE,LastSeenTexts.BENCH,LastSeenTexts.LAMP_POST,
                LastSeenTexts.FOUNTAIN,LastSeenTexts.BOWL,LastSeenTexts.TRUNK,LastSeenTexts.KIOSK,LastSeenTexts.play(1),LastSeenTexts.play(2),LastSeenTexts.play(3),LastSeenTexts.play(4),
                LastSeenTexts.play(5),LastSeenTexts.BANDSTAND,LastSeenTexts.RAILING,LastSeenTexts.POST,LastSeenTexts.GATE))
                h.assertTrue(why.contains(line),"each says what it is: missing \""+line+"\" in "+why);
            h.assertTrue(LastSeenTexts.poster(0).contains("Rocío")&&LastSeenTexts.poster(9).contains("Santino")&&!LastSeenTexts.poster(12).contains("Last seen"),"the ten by name; the older posters' names gone into the paper");
            for(int who=0;who<LastSeen.PEOPLE;who++)h.assertTrue(f.own(p,P).getInt("Heard"+who)==2&&f.own(peer,P).getInt("Heard"+who)==1,"each reader's own count of what they heard from "+LastSeenTexts.NAMES[who]);
            // The card: the office's own words, and it fits a book.
            var paper=LiteraryTexts.source(P).get(DataComponents.WRITTEN_BOOK_CONTENT);String text=String.join(" ",paper.pages().stream().map(pg->pg.raw().getString()).toList());
            h.assertTrue(text.contains("MISSING PERSONS")&&text.contains("last seen")&&paper.pages().size()>=4,"the card says what the office is for: "+text);
            for(String word:List.of(" he "," she "," his "," her "," him "))h.assertFalse(text.toLowerCase(java.util.Locale.ROOT).contains(word),"the card says nothing of what anybody is: "+word);
            h.assertTrue(LiteraryTexts.accountTitle(P).length()<=32,"the account's title fits a book: "+LiteraryTexts.accountTitle(P));
            h.assertTrue(SceneClock.at(f.origin,b.offset(-8,0,-40),HouseDimensions.OUTSIDE)==12,"nine in the morning in the park");
            // 0.4.668: dealt from depth eight; never at the surface.
            h.assertFalse(LiteraryVignettes.canDeal(f.data(),p.getUUID(),P)||LastSeen.dealable(f.data(),java.util.UUID.randomUUID()),"the House deals it only from depth eight");
            done();h.succeed();
        });
    }

    /**
     * The houses across the avenue keep their doors shut: a hand on one is told it is locked and it does not open. A spectator's hand
     * opens nothing: the gate stays shut for them.
     */
    @GameTest(template="empty",batch="last_seen_doors",timeoutTicks=600)
    public static void theHousesDoorsStayLockedAndASpectatorOpensNothing(GameTestHelper h){
        var f=place(h);var p=f.player("last_seen_door_reader",P);var l=f.level(P);var b=f.base(P);
        h.startSequence().thenIdle(5).thenExecute(()->{
            BlockPos house=null;
            for(int x=LastSeenSite.WEST;x<=LastSeenSite.EAST&&house==null;x++){var at=new BlockPos(x,0,LastSeenSite.HOUSE_FRONT);if(x!=LastSeenSite.ONWARD.getX()&&l.getBlockState(b.offset(at)).getBlock() instanceof DoorBlock)house=at;}
            h.assertTrue(house!=null,"a house door across the avenue");
            h.assertTrue(f.data().door(OnwardDoors.id(P))!=null||OnwardDoors.install(l,b,P),"the way on goes in");
            LastSeenSite.gate(l,b,LastSeenBlock.OPEN);told(p);
            use(p,b,house,house.offset(0,0,2));
            var why=told(p);
            h.assertTrue(why.contains(LastSeenTexts.HOUSE_DOOR)&&!l.getBlockState(b.offset(house)).getValue(DoorBlock.OPEN),"the door is locked and stays shut: "+why);
            h.assertTrue(l.getBlockState(b.offset(LastSeenSite.ONWARD)).getBlock() instanceof DoorBlock,"the way on stands among them, a door of its own: "+l.getBlockState(b.offset(LastSeenSite.ONWARD)));
            LastSeenSite.gate(l,b,LastSeenBlock.SHUT);
            p.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);
            use(p,b,LastSeenSite.GATE_W,new BlockPos(-9,0,LastSeenSite.PARK_N+2));
            h.assertTrue(stage(l,b,LastSeenSite.GATE_W)==LastSeenBlock.SHUT,"a spectator's hand opens nothing");
            done();h.succeed();
        });
    }

    // ------------------------------------------------------------------------------------------------ 0.4.668: the archive and the first one
    /** Where a reader stands to reach a bay: two cells into the archive from its wall. */
    private static BlockPos before(int bay){
        var at=LastSeenSite.SHELVES.get(bay);
        if(at.getX()==LastSeenSite.OFFICE_W+1)return at.offset(2,0,0);
        if(at.getZ()==LastSeenSite.OFFICE_S-1)return at.offset(0,0,-2);
        return at.offset(0,0,2);
    }
    private static void bay(ServerPlayer p,BlockPos b,int bay){use(p,b,LastSeenSite.SHELVES.get(bay),before(bay));}
    private static void bayOf(ServerPlayer p,BlockPos b,int child){bay(p,b,LastSeen.bayFor(child));}
    private static int held(ServerPlayer p){return LastSeen.child(p.getMainHandItem());}
    private static int carried(ServerPlayer p,int child){int n=0;var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(LastSeen.child(inv.getItem(i))==child)n++;return n;}
    private static int gap(ServerLevel l,BlockPos b,int child){return stage(l,b,LastSeen.gapCell(child));}
    private static boolean opened(ServerPlayer p,int child){NativeTestPlayers.flush(p);return opened(new ArrayList<Object>(NativeTestPlayers.packets(p)),child);}
    /** What a reader was sent, read once: whether a folder was held open on their screen, and the lines they were told. */
    private static List<Object> sent(ServerPlayer p){NativeTestPlayers.flush(p);return new ArrayList<Object>(NativeTestPlayers.packets(p));}
    private static boolean opened(List<Object> sent,int child){
        for(var packet:sent)if(packet instanceof net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket custom&&custom.payload() instanceof io.github.knaitoe.theoldesthouse.network.LastSeenFolderPayload folder&&folder.child()==child)return true;
        return false;
    }
    private static List<String> lines(List<Object> sent){var out=new ArrayList<String>();for(var packet:sent)if(packet instanceof net.minecraft.network.protocol.game.ClientboundSystemChatPacket chat)out.add(chat.content().getString());return out;}
    /** A reader in the archive, turned away from the park, as a reader at the shelves would be. */
    private static void archive(LiteraryTests.Fixture f,ServerPlayer p){f.at(p,P,-15.5,0,-9.5);p.setYRot(0);p.setYHeadRot(0);p.setXRot(0);}
    private static void talk(LiteraryTests.Fixture f,ServerPlayer p,LiteraryActor a){f.at(p,P,a.getX()-f.base(P).getX()+1.2,0,a.getZ()-f.base(P).getZ()+.5);LiteraryVignettes.talk(p,a);}

    /**
     * One reader through the morning. Nora gives her rule and then the chore. The day's pile is four folders, handed out one at a time
     * into an empty hand and held open; each leaves its gap on its own shelf. A folder offered to the wrong bay is told whose names are
     * there; at its own bay it goes back and the gap fills. With the pile shelved the table is bare, and the first of them is at the
     * fountain: Luz opens the front door and says so, then calls Nora, who walks out as far as the head of the path and stops. The girl
     * is child-sized and stands at the rim facing the office; coming near her the reader is told so. Her hand is cold; another child's
     * folder she looks at and gives no more; a bay that holds one of the ten gives that folder, held open, and only one is off the
     * shelves at a time. Given her own folder she takes it, looks at it, says the one thing, and gives it back. Nora's lines follow.
     */
    @GameTest(template="empty",batch="last_seen_first",timeoutTicks=4000)
    public static void theArchiveAndTheFirstOne(GameTestHelper h){
        var f=place(h);var p=f.player("last_seen_first_reader",P);var l=f.level(P);var b=f.base(P);
        h.startSequence().thenIdle(5).thenExecute(()->archive(f,p))
        .thenWaitUntil(()->h.assertTrue(LastSeen.stage(b)==LastSeen.MORNING&&LastSeen.inPile(b,LastSeen.PILE[0])&&LastSeen.find(l,b,LastSeen.NORA)!=null,"the morning starts with the day's pile on the table"))
        .thenExecute(()->{
            for(int c:LastSeen.PILE)h.assertTrue(gap(l,b,c)==LastSeenBlock.ONE_OUT,"each of the pile's folders is missing from its own shelf: "+LastSeenTexts.CHILDREN[c]);
            h.assertTrue(gap(l,b,LastSeen.ROCIO)!=LastSeenBlock.ONE_OUT,"hers is on its shelf");
            var nora=LastSeen.find(l,b,LastSeen.NORA);
            talk(f,p,nora);h.assertTrue(said(nora).equals(LastSeenTexts.RULE)&&f.own(p,P).getBoolean("Rule"),"Nora's rule first: "+said(nora));
            talk(f,p,nora);h.assertTrue(said(nora).equals(LastSeenTexts.CHORE),"then the chore: "+said(nora));
            archive(f,p);told(p);
            var archive=new BlockPos(-16,0,-10);
            for(int i=0;i<LastSeen.PILE.length;i++){
                int c=LastSeen.PILE[i];
                use(p,b,LastSeen.PILE_AT,archive);
                h.assertTrue(held(p)==c&&p.getUUID().equals(LastSeen.out(b,c))&&!LastSeen.inPile(b,c),"the pile's next folder, into an empty hand: "+held(p));
                h.assertTrue(opened(p,c),"held open as it is handed over");
                if(i==0){
                    use(p,b,LastSeen.PILE_AT,archive);h.assertTrue(carried(p,LastSeen.PILE[1])==0,"one folder off the shelves at a time");
                    bay(p,b,1);h.assertTrue(held(p)==c,"the wrong bay keeps nothing");
                    var why=told(p);h.assertTrue(why.contains(LastSeenTexts.ONE_AT_A_TIME)&&why.contains(LastSeenTexts.wrongBay(LastSeen.letter(1))),"told why: "+why);
                }
                bayOf(p,b,c);
                h.assertTrue(p.getMainHandItem().isEmpty()&&LastSeen.out(b,c)==null&&gap(l,b,c)!=LastSeenBlock.ONE_OUT,"back where its name says, and the gap filled: "+LastSeenTexts.CHILDREN[c]);
                h.assertTrue(told(p).contains(LastSeenTexts.shelved(LastSeen.letter(LastSeen.bayFor(c)))),"told it goes back among the same letter");
            }
            int shelved=f.own(p,P).getInt("Shelved");for(int c:LastSeen.PILE)h.assertTrue((shelved&1<<c)!=0,"the reader's own count of what they shelved");
            h.assertTrue(stage(l,b,LastSeen.PILE_AT)==0,"the table is bare");
            use(p,b,LastSeen.PILE_AT,archive);h.assertTrue(told(p).contains(LastSeenTexts.PILE_DONE),"and says so");
            archive(f,p);
        }).thenWaitUntil(()->h.assertTrue(LastSeen.stage(b)>=LastSeen.CALLED,"with the pile shelved, the first of them comes"))
        .thenExecute(()->{
            var girl=LastSeen.rocio(l,b);
            h.assertTrue(girl!=null&&girl.phase()==LastSeen.CHILDREN_FIRST+LastSeen.ROCIO&&Math.abs(girl.getScale()-.68)<.01,"she is there, at a child's height: "+girl);
            h.assertTrue(girl.position().distanceToSqr(new Vec3(b.getX()+LastSeen.RIM.x,b.getY(),b.getZ()+LastSeen.RIM.z))<.25&&Math.abs(net.minecraft.util.Mth.wrapDegrees(girl.getYRot()))<5,"at the fountain's rim, facing the office: "+girl.position());
        }).thenWaitUntil(()->h.assertTrue(LastSeen.stage(b)==LastSeen.FOUND,"Luz calls, and Nora goes out"))
        .thenExecute(()->{
            var door=l.getBlockState(b.offset(LastSeenSite.FRONT_DOOR));
            h.assertTrue(door.getBlock() instanceof DoorBlock&&door.getValue(DoorBlock.OPEN),"the front door stands open");
            var luz=LastSeen.find(l,b,LastSeen.LUZ);var nora=LastSeen.find(l,b,LastSeen.NORA);
            h.assertTrue(said(luz).equals(LastSeenTexts.LUZ_NORA)&&luz.position().distanceToSqr(Vec3.atLowerCornerOf(b).add(1.5,0,-12.6))<.5,"Luz at the door, calling Nora: "+said(luz)+" "+luz.position());
            h.assertTrue(nora.position().distanceToSqr(Vec3.atLowerCornerOf(b).add(-7.5,0,-18.5))<.5&&nora.position().z-b.getZ()>LastSeenSite.FRONT_N-1.5,"Nora at the head of the path and no further: "+nora.position());
            talk(f,p,nora);h.assertTrue(said(nora).equals(LastSeenTexts.NORA_WAITS),"she stays: "+said(nora));
            talk(f,p,nora);h.assertTrue(said(nora).equals(LastSeenTexts.NORA_FIND),"and says to find hers: "+said(nora));
            told(p);f.at(p,P,-7.5,0,-41.5);
        }).thenIdle(10).thenExecute(()->{
            var girl=LastSeen.rocio(l,b);
            h.assertTrue(told(p).contains(LastSeenTexts.AT_RIM)&&f.own(p,P).getBoolean("SawFirst"),"near her, the reader is told how she stands");
            talk(f,p,girl);h.assertTrue(told(p).contains(LastSeenTexts.COLD),"her hand is cold");
            // Another child's folder: from its bay, held open; she looks at the face, and away.
            archive(f,p);bayOf(p,b,5);var got=sent(p);
            h.assertTrue(held(p)==5&&opened(got,5)&&lines(got).contains(LastSeenTexts.taken(5,LastSeen.letter(LastSeen.bayFor(5)))),"a bay that holds one of the ten gives that folder: "+held(p)+" "+lines(got));
            bayOf(p,b,LastSeen.ROCIO);var why=told(p);
            h.assertTrue(held(p)==5&&LastSeen.out(b,LastSeen.ROCIO)==null&&why.contains(LastSeenTexts.wrongBay(LastSeen.letter(LastSeen.bayFor(LastSeen.ROCIO)))),"one at a time: a bay of another letter takes nothing and gives nothing: "+why);
            talk(f,p,girl);h.assertTrue(held(p)==5&&told(p).contains(LastSeenTexts.NOT_HERS),"not hers: she looks, and gives no more");
            archive(f,p);bayOf(p,b,5);bayOf(p,b,LastSeen.ROCIO);
            h.assertTrue(held(p)==LastSeen.ROCIO&&opened(p,LastSeen.ROCIO)&&(f.own(p,P).getInt("Read")&1<<LastSeen.ROCIO)!=0,"hers, from its own bay, held open");
            talk(f,p,girl);
            h.assertTrue(p.getMainHandItem().isEmpty()&&LastSeen.holding(b)==LastSeen.ROCIO&&LastSeen.child(girl.getMainHandItem())==LastSeen.ROCIO,"she takes it into her own hand");
            h.assertTrue((f.own(p,P).getInt("Matched")&1<<LastSeen.ROCIO)!=0&&f.own(p,P).getInt("Days")>=1,"matched, for this reader");
        }).thenIdle(LastSeen.LOOK+2).thenExecute(()->{
            var girl=LastSeen.rocio(l,b);
            h.assertTrue(said(girl).equals(LastSeenTexts.WAITING),"she looks up and says the one thing: "+said(girl));
        }).thenWaitUntil(()->h.assertTrue(LastSeen.holding(b)<0,"and gives it back"))
        .thenExecute(()->{
            h.assertTrue(carried(p,LastSeen.ROCIO)==1&&p.getUUID().equals(LastSeen.out(b,LastSeen.ROCIO))&&told(p).contains(LastSeenTexts.GIVES_BACK),"back to the reader who brought it");
            h.assertTrue(LastSeen.rocio(l,b).getMainHandItem().isEmpty(),"her hands empty again");
            var nora=LastSeen.find(l,b,LastSeen.NORA);talk(f,p,nora);h.assertTrue(said(nora).equals(LastSeenTexts.NORA_AFTER),"Nora: put it back where it goes: "+said(nora));
            // The reader leaves the place with it: the folder goes back on its shelf.
            p.moveTo(b.getX()+.5,b.getY(),b.getZ()+30);
        }).thenWaitUntil(()->h.assertTrue(carried(p,LastSeen.ROCIO)==0&&LastSeen.out(b,LastSeen.ROCIO)==null&&gap(l,b,LastSeen.ROCIO)!=LastSeenBlock.ONE_OUT,"a reader who leaves leaves the folder on its shelf"))
        .thenExecute(()->{done();h.succeed();});
    }

    /**
     * Two readers. A spectator's hand takes nothing from the pile. Each reader can hold one folder at once, and each counts their own
     * shelving. A folder out with one reader leaves a gap the other is told about. A reader who leaves has theirs put back, and is told.
     * A copy of a folder is nobody's once the folder goes back. Each reader brings her folder for themselves and has it matched; the one
     * watching is counted as having seen it. The House's read: Patricia asks a reader it reads as young whether they are here with
     * someone, and Nora tells a reader it reads as having left a home that most of the ones in here went.
     */
    @GameTest(template="empty",batch="last_seen_two",timeoutTicks=4000)
    public static void twoReadersEachMatchHer(GameTestHelper h){
        var f=place(h);var p=f.player("last_seen_two_a",P);var q=f.player("last_seen_two_b",P);var l=f.level(P);var b=f.base(P);
        HouseRead.signal(q,HouseRead.Trait.AGE,"DIGITAL",false);HouseRead.signal(q,HouseRead.Trait.AGE,"DIGITAL",true);
        HouseRead.signal(p,HouseRead.Trait.HOME,"RUNAWAY",false);HouseRead.signal(p,HouseRead.Trait.HOME,"RUNAWAY",true);
        h.startSequence().thenIdle(5).thenExecute(()->{archive(f,p);archive(f,q);})
        .thenWaitUntil(()->h.assertTrue(LastSeen.stage(b)==LastSeen.MORNING&&LastSeen.inPile(b,LastSeen.PILE[0]),"the morning starts"))
        .thenExecute(()->{
            var archive=new BlockPos(-16,0,-10);
            q.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);use(q,b,LastSeen.PILE_AT,archive);
            h.assertTrue(LastSeen.inPile(b,LastSeen.PILE[0])&&q.getMainHandItem().isEmpty(),"a spectator's hand takes nothing");
            q.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
            // Both hold one at once.
            use(p,b,LastSeen.PILE_AT,archive);use(q,b,LastSeen.PILE_AT,new BlockPos(-15,0,-10));
            h.assertTrue(held(p)==LastSeen.PILE[0]&&held(q)==LastSeen.PILE[1],"each reader holds one of their own: "+held(p)+" "+held(q));
            // A copy made of p's folder is nobody's once it goes back.
            p.getInventory().setItem(20,p.getMainHandItem().copy());
            bayOf(p,b,LastSeen.PILE[0]);bayOf(q,b,LastSeen.PILE[1]);
            h.assertTrue((f.own(p,P).getInt("Shelved")&1<<LastSeen.PILE[0])!=0&&(f.own(p,P).getInt("Shelved")&1<<LastSeen.PILE[1])==0&&(f.own(q,P).getInt("Shelved")&1<<LastSeen.PILE[1])!=0,"each counts their own shelving");
            use(p,b,LastSeen.PILE_AT,archive);bayOf(p,b,LastSeen.PILE[2]);use(q,b,LastSeen.PILE_AT,new BlockPos(-15,0,-10));bayOf(q,b,LastSeen.PILE[3]);
            // q takes hers; p is told there is a gap.
            bayOf(q,b,LastSeen.ROCIO);h.assertTrue(held(q)==LastSeen.ROCIO,"one reader takes hers down");
            told(p);bayOf(p,b,LastSeen.ROCIO);h.assertTrue(p.getMainHandItem().isEmpty()&&told(p).contains(LastSeenTexts.SHELF_GAP),"the other finds the gap");
            told(q);q.moveTo(b.getX()+.5,b.getY(),b.getZ()+30);
        }).thenWaitUntil(()->h.assertTrue(carried(q,LastSeen.ROCIO)==0&&LastSeen.out(b,LastSeen.ROCIO)==null,"the one who left has hers put back"))
        .thenExecute(()->h.assertTrue(told(q).contains(LastSeenTexts.BACK_ON_SHELF),"and is told"))
        .thenIdle(41).thenExecute(()->{
            h.assertTrue(p.getInventory().getItem(20).isEmpty(),"the copy is nobody's, and gone");
            archive(f,q);
            // The House's read, in the office's own words.
            var patricia=LastSeen.find(l,b,LastSeen.PATRICIA);var nora=LastSeen.find(l,b,LastSeen.NORA);
            talk(f,q,patricia);h.assertTrue(said(patricia).equals(LastSeenTexts.PATRICIA_YOUNG),"to a reader read as young: "+said(patricia));
            talk(f,p,patricia);h.assertTrue(said(patricia).equals(LastSeenTexts.line(LastSeen.PATRICIA,0)),"and her usual first line to the other: "+said(patricia));
            for(int i=0;i<2;i++)talk(f,p,nora);
            talk(f,p,nora);h.assertTrue(said(nora).equals(LastSeenTexts.LEAVING)&&f.own(p,P).getBoolean("Leaving"),"to a reader read as having left a home: "+said(nora));
            archive(f,p);archive(f,q);
        }).thenWaitUntil(()->h.assertTrue(LastSeen.stage(b)==LastSeen.FOUND,"the first of them is at the fountain"))
        .thenExecute(()->{
            var girl=LastSeen.rocio(l,b);
            bayOf(p,b,LastSeen.ROCIO);f.at(q,P,-6.5,0,-42.5);talk(f,p,girl);
            h.assertTrue((f.own(p,P).getInt("Matched")&1)!=0&&(f.own(q,P).getInt("Matched")&1)==0&&(f.own(q,P).getInt("SawMatched")&1)!=0,"matched for the one who brought it; seen by the other");
        }).thenWaitUntil(()->h.assertTrue(LastSeen.holding(b)<0&&carried(p,LastSeen.ROCIO)==1,"she gives it back"))
        .thenExecute(()->{
            archive(f,p);bayOf(p,b,LastSeen.ROCIO);archive(f,q);bayOf(q,b,LastSeen.ROCIO);
            h.assertTrue(held(q)==LastSeen.ROCIO,"the other takes it down in turn");
            talk(f,q,LastSeen.rocio(l,b));
            h.assertTrue((f.own(q,P).getInt("Matched")&1)!=0&&LastSeen.holding(b)==LastSeen.ROCIO,"and brings it to her for themselves");
        }).thenWaitUntil(()->h.assertTrue(LastSeen.holding(b)<0&&carried(q,LastSeen.ROCIO)==1,"she gives it back to them too"))
        .thenExecute(()->{done();h.succeed();});
    }

    // ------------------------------------------------------------------------------------------------ 0.4.669: the second day
    /** A reader who matched the first of them comes back to the second day: their own count says so before the morning starts. */
    private static void secondDay(LiteraryTests.Fixture f,ServerPlayer p){var o=f.own(p,P);o.putInt("Days",1);LiteraryVignettes.save(f.data(),p.getUUID(),P,o);}
    /** The park's hour each time it was sent. */
    private static List<Long> hours(List<Object> sent){
        var out=new ArrayList<Long>();
        for(var packet:sent)if(packet instanceof net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket custom&&custom.payload() instanceof io.github.knaitoe.theoldesthouse.network.LastSeenTimePayload time)out.add(time.time());
        return out;
    }
    private static Vec3 spot(BlockPos b,int c){return Vec3.atLowerCornerOf(b).add(LastSeen.SPOT[c]);}
    /** A reader on the path, south of a spot and turned south, away from where the visitors come in. */
    private static void south(LiteraryTests.Fixture f,ServerPlayer p,Vec3 rel){f.at(p,P,rel.x,0,rel.z+4);p.setYRot(0);p.setYHeadRot(0);p.setXRot(0);}
    private static float yaw(LiteraryActor a){return net.minecraft.util.Mth.wrapDegrees(a.getYRot());}
    private static boolean facing(LiteraryActor a,float yaw){return Math.abs(net.minecraft.util.Mth.wrapDegrees(a.getYRot()-yaw))<6;}

    /**
     * One reader through the second day, from the first of them to dusk. Eight of the ten are in the park at child scale, each at their
     * own spot facing their own way; the last two never come. The table is bare and the front door open. Four are matched by their own
     * folders: each takes it, looks at it, says the one thing that is theirs, and gives it back; the fourth makes it the reader's second
     * day. A reader who comes near is told how they stand. Something to eat is held and not eaten; an empty hand is cold. Nora's day-two
     * lines, Patricia's phone, Luz's count. Tomás's grandmother brings his coat and puts it round him; Luz has seen her come. Daiana's
     * father takes her out of the park by the wrist and brings her back to the gate. The city's fence goes up round the playground, a
     * panel at a time, with corners turned to fit; its notices go onto every lamp post, facing the path; Lautaro puts his hands to the
     * mesh. Mr. Ocampo tells Nora to close the files, and Nora's answer is that they stay open. At dusk every one of them faces the avenue,
     * and the park's hour has gone from morning to dusk.
     */
    @GameTest(template="empty",batch="last_seen_day_two",timeoutTicks=9000)
    public static void theSecondDayAndItsFamilies(GameTestHelper h){
        var f=place(h);var p=f.player("last_seen_day_two_reader",P);secondDay(f,p);var l=f.level(P);var b=f.base(P);
        int[] four={LastSeen.TOMAS,LastSeen.MICAELA,LastSeen.BRIAN,LastSeen.ABRIL};
        var seq=h.startSequence().thenIdle(2).thenExecute(()->archive(f,p))
        .thenWaitUntil(()->h.assertTrue(LastSeen.dayNumber(b)==2&&LastSeen.stage(b)==LastSeen.PARK&&java.util.Arrays.stream(LastSeen.EIGHT).allMatch(c->LastSeen.kid(l,b,c)!=null),"the second day: the eight are in the park"))
        .thenExecute(()->{
            for(int c:LastSeen.EIGHT){
                var k=LastSeen.kid(l,b,c);
                h.assertTrue(Math.abs(k.getScale()-.68)<.01&&k.position().distanceToSqr(spot(b,c))<.3&&facing(k,LastSeen.FACES[c]),"each at a child's height, at their own spot, facing their own way: "+LastSeenTexts.CHILDREN[c]+" "+k.position()+" "+yaw(k));
            }
            h.assertTrue(LastSeen.kid(l,b,8)==null&&LastSeen.kid(l,b,9)==null,"the last two never come");
            h.assertTrue(stage(l,b,LastSeen.PILE_AT)==0&&l.getBlockState(b.offset(LastSeenSite.FRONT_DOOR)).getValue(DoorBlock.OPEN),"the table is bare and the front door open");
            var hours=hours(sent(p));h.assertTrue(!hours.isEmpty()&&hours.get(hours.size()-1)>=LastSeen.MORNING_TIME,"the park's hour is sent: "+hours);
            // Nora, Patricia and Luz on the second day.
            var nora=LastSeen.find(l,b,LastSeen.NORA);var patricia=LastSeen.find(l,b,LastSeen.PATRICIA);var luz=LastSeen.find(l,b,LastSeen.LUZ);
            talk(f,p,nora);h.assertTrue(said(nora).equals(LastSeenTexts.RULE),"her rule first: "+said(nora));
            talk(f,p,nora);h.assertTrue(said(nora).equals(LastSeenTexts.NORA_EIGHT),"then that there are eight of them: "+said(nora));
            talk(f,p,nora);h.assertTrue(said(nora).equals(LastSeenTexts.NORA_EACH),"and to take each one their own: "+said(nora));
            talk(f,p,patricia);h.assertTrue(said(patricia).equals(LastSeenTexts.PATRICIA_PHONE),"Patricia and the phone: "+said(patricia));
            talk(f,p,luz);h.assertTrue(said(luz).equals(LastSeenTexts.LUZ_COUNTED),"Luz counted them from the door: "+said(luz));
            // One of the two who never come: Nora says so.
            archive(f,p);bayOf(p,b,8);h.assertTrue(held(p)==8,"Yanina's folder, from its bay");
            talk(f,p,nora);h.assertTrue(said(nora).equals(LastSeenTexts.NORA_NOT_BACK),"that one hasn't come back: "+said(nora));
            archive(f,p);bayOf(p,b,8);h.assertTrue(p.getMainHandItem().isEmpty(),"back on its shelf");
            told(p);
        });
        for(int i=0;i<four.length;i++){
            final int c=four[i],n=i+1;
            seq=seq.thenExecute(()->{
                archive(f,p);bayOf(p,b,c);h.assertTrue(held(p)==c,"their own folder: "+LastSeenTexts.CHILDREN[c]);
                var k=LastSeen.kid(l,b,c);talk(f,p,k);
                h.assertTrue(LastSeen.holding(b,c)&&LastSeen.child(k.getMainHandItem())==c,"they take it into their own hand: "+LastSeenTexts.CHILDREN[c]);
                h.assertTrue(Integer.bitCount(f.own(p,P).getInt("Second"))==n,"counted on the second day");
            }).thenIdle(LastSeen.LOOK+2).thenExecute(()->{
                var k=LastSeen.kid(l,b,c);
                h.assertTrue(said(k).equals(LastSeenTexts.oneThing(c,false)),"the one thing that is theirs: "+said(k));
                h.assertTrue(told(p).contains(LastSeenTexts.near(c)),"a reader near is told how they stand: "+LastSeenTexts.CHILDREN[c]);
            }).thenWaitUntil(()->h.assertTrue(!LastSeen.holding(b,c)&&carried(p,c)==1,"given back"))
            .thenExecute(()->{archive(f,p);bayOf(p,b,c);h.assertTrue(LastSeen.out(b,c)==null,"and put back where it goes");});
        }
        seq.thenExecute(()->{
            h.assertTrue(f.own(p,P).getInt("Days")>=2,"four matched on the second day make it the reader's own");
            // Something to eat, held and not eaten; and a second offered is not taken.
            var joel=LastSeen.kid(l,b,LastSeen.JOEL);
            p.setItemInHand(InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BREAD,2));talk(f,p,joel);
            h.assertTrue(joel.getOffhandItem().is(net.minecraft.world.item.Items.BREAD)&&p.getMainHandItem().getCount()==1&&told(p).contains(LastSeenTexts.fed(LastSeen.JOEL)),"Joel holds it and does not eat");
            talk(f,p,joel);h.assertTrue(p.getMainHandItem().getCount()==1&&told(p).contains(LastSeenTexts.stillHolding(LastSeen.JOEL)),"and is still holding the first");
            p.setItemInHand(InteractionHand.MAIN_HAND,net.minecraft.world.item.ItemStack.EMPTY);
            talk(f,p,LastSeen.kid(l,b,LastSeen.BRIAN));h.assertTrue(told(p).contains(LastSeenTexts.cold(LastSeen.BRIAN,false)),"an empty hand: cold");
            // The grandmother: the reader stands south of Tomás, turned away from where she comes in.
            south(f,p,LastSeen.SPOT[LastSeen.TOMAS]);LastSeen.at(b,LastSeen.GRAN_AT);
        }).thenWaitUntil(()->h.assertTrue((LastSeen.kid(l,b,LastSeen.TOMAS).marks()&LastSeen.COAT_MARK)!=0,"Tomás's grandmother puts his coat round him"))
        .thenExecute(()->{
            var gran=LastSeen.family(l,b,LastSeen.GRAN);var tomas=LastSeen.kid(l,b,LastSeen.TOMAS);
            h.assertTrue(gran!=null&&gran.distanceTo(tomas)<2.5&&gran.getMainHandItem().isEmpty(),"she is beside him, her hands empty now");
            h.assertTrue(told(p).contains(LastSeenTexts.COAT_ON),"the reader near sees it");
            h.assertTrue("lastseen_tomas_coat".equals(LastSeen.dressed(tomas.phase(),tomas.marks())),"and he is drawn in it");
            talk(f,p,gran);h.assertTrue(said(gran).equals(LastSeenTexts.family(LastSeen.GRAN,0)),"she says why she brought it: "+said(gran));
            talk(f,p,tomas);h.assertTrue(told(p).contains(LastSeenTexts.cold(LastSeen.TOMAS,true)),"under the coat, his hand just as cold");
            var luz=LastSeen.find(l,b,LastSeen.LUZ);talk(f,p,luz);h.assertTrue(said(luz).equals(LastSeenTexts.LUZ_COAT),"Luz saw her come in: "+said(luz));
            // Mr. Ocampo, the city's fence and Daiana's father all come in this stretch of the afternoon; the reader is in the archive.
            archive(f,p);told(p);LastSeen.at(b,LastSeen.OCAMPO_AT);
        }).thenWaitUntil(()->h.assertTrue(LastSeen.ocampoDone(b),"Mr. Ocampo comes to Nora's desk"))
        .thenExecute(()->{
            var nora=LastSeen.find(l,b,LastSeen.NORA);var ocampo=LastSeen.family(l,b,LastSeen.OCAMPO);
            h.assertTrue(ocampo!=null&&said(ocampo).equals(LastSeenTexts.OCAMPO_CLOSE)&&said(nora).equals(LastSeenTexts.NORA_OPEN),"close them, all of them; they stay open: "+(ocampo==null?null:said(ocampo))+" / "+said(nora));
            h.assertTrue(ocampo.position().distanceToSqr(Vec3.atLowerCornerOf(b).add(-9.4,0,-4.6))<.5,"at her desk: "+ocampo.position());
            talk(f,p,nora);h.assertTrue(said(nora).equals(LastSeenTexts.NORA_CLOSED),"and to the reader after: "+said(nora));
            archive(f,p);
        }).thenWaitUntil(()->h.assertTrue(LastSeen.fenced(b),"the city's fence goes up round the playground: "+LastSeen.fenceLeft(b)))
        .thenWaitUntil(()->h.assertTrue(LastSeenSite.LAMPS.stream().allMatch(lamp->fitting(l,b,lamp,LastSeenBlock.Kind.LAMP_POST)&&stage(l,b,lamp)==LastSeenBlock.NOTICE),"a notice on every lamp post"))
        .thenExecute(()->{
            for(var lamp:LastSeenSite.LAMPS)h.assertTrue(l.getBlockState(b.offset(lamp)).getValue(LastSeenBlock.FACING)==LastSeenSite.noticeFacing(lamp),"each notice faces its path: "+lamp);
            var nw=l.getBlockState(b.offset(5,0,-39));var row=l.getBlockState(b.offset(7,1,-39));var mid=l.getBlockState(b.offset(6,0,-39));var se=l.getBlockState(b.offset(21,0,-21));var col=l.getBlockState(b.offset(5,0,-37));
            h.assertTrue(nw.getValue(LastSeenBlock.STAGE)==LastSeenBlock.CORNER&&nw.getValue(LastSeenBlock.FACING)==Direction.NORTH,"a corner post turned to its two sides");
            h.assertTrue(se.getValue(LastSeenBlock.STAGE)==LastSeenBlock.CORNER&&se.getValue(LastSeenBlock.FACING)==Direction.SOUTH,"and the opposite corner turned the other way");
            h.assertTrue(row.getValue(LastSeenBlock.STAGE)==LastSeenBlock.PANEL_TOP&&mid.getValue(LastSeenBlock.STAGE)==LastSeenBlock.PANEL_MID,"panels two blocks wide, each joint with its frame and foot");
            h.assertTrue(col.getValue(LastSeenBlock.FACING)==Direction.EAST&&col.getValue(LastSeenBlock.STAGE)==LastSeenBlock.PANEL,"the sides run the other way");
            h.assertTrue(fitting(l,b,new BlockPos(13,0,-21),LastSeenBlock.Kind.FENCE)&&fitting(l,b,new BlockPos(13,1,-21),LastSeenBlock.Kind.FENCE),"the old gap in it closed too");
            h.assertTrue(LastSeen.kid(l,b,LastSeen.LAUTARO).gesture()==LastSeen.MESH,"Lautaro's hands on the mesh");
        }).thenWaitUntil(()->h.assertTrue(LastSeen.daiana(b)>=2,"Daiana's father takes her out of the park"))
        .thenExecute(()->{h.assertTrue(LastSeen.kid(l,b,LastSeen.DAIANA)==null,"and she is gone with him");LastSeen.at(b,LastSeen.since(b)+LastSeen.AWAY);})
        .thenWaitUntil(()->h.assertTrue(LastSeen.daiana(b)==4,"he brings her back to the gate"))
        .thenWaitUntil(()->{var k=LastSeen.kid(l,b,LastSeen.DAIANA);h.assertTrue(k!=null&&k.position().distanceToSqr(Vec3.atLowerCornerOf(b).add(LastSeen.AT_GATE))<.3&&facing(k,180),"and leaves her there, facing the avenue: "+(k==null?null:k.position()));})
        .thenExecute(()->{
            LastSeen.at(b,LastSeen.DUSK_AT);
        }).thenWaitUntil(()->h.assertTrue(LastSeen.stage(b)==LastSeen.DUSK,"dusk"))
        .thenIdle(5).thenExecute(()->{
            for(int c:LastSeen.EIGHT){var k=LastSeen.kid(l,b,c);if(k!=null&&!LastSeen.holding(b,c))h.assertTrue(facing(k,180),"every one of them faces the avenue: "+LastSeenTexts.CHILDREN[c]+" "+yaw(k));}
            var said=told(p);h.assertTrue(said.contains(LastSeenTexts.DUSK),"the reader is told");
        }).thenIdle(41).thenExecute(()->{
            var hours=hours(sent(p));h.assertTrue(!hours.isEmpty()&&hours.get(hours.size()-1)>=LastSeen.DUSK_TIME-1,"the park's hour is dusk: "+hours);
            done();h.succeed();
        });
    }

    /**
     * Two readers on the second day, one who has been before and one who has not: the day is the second for both of them because one of
     * them reached it. Each matches Brian for themselves, in turn, and each is counted for themselves; the one watching is counted as
     * having seen it. What Brian says to a reader the House reads as having run from a home is his own line for them. Nora's day-two
     * lines are each reader's own. Something given to one of them is given once. Both readers near Tomás are told when the coat goes on.
     */
    @GameTest(template="empty",batch="last_seen_day_two_together",timeoutTicks=5000)
    public static void twoReadersOnTheSecondDay(GameTestHelper h){
        var f=place(h);var p=f.player("last_seen_day_two_a",P);var q=f.player("last_seen_day_two_b",P);secondDay(f,p);var l=f.level(P);var b=f.base(P);
        HouseRead.signal(q,HouseRead.Trait.HOME,"RUNAWAY",false);HouseRead.signal(q,HouseRead.Trait.HOME,"RUNAWAY",true);
        int brian=LastSeen.BRIAN;
        h.startSequence().thenIdle(2).thenExecute(()->{archive(f,p);archive(f,q);})
        .thenWaitUntil(()->h.assertTrue(LastSeen.dayNumber(b)==2&&LastSeen.kid(l,b,brian)!=null,"the second day, for both, because one of them reached it"))
        .thenExecute(()->{
            var nora=LastSeen.find(l,b,LastSeen.NORA);
            for(var r:List.of(p,q)){talk(f,r,nora);talk(f,r,nora);h.assertTrue(said(nora).equals(LastSeenTexts.NORA_EIGHT),"each reader hears Nora's day for themselves: "+said(nora));}
            archive(f,p);bayOf(p,b,brian);talk(f,p,LastSeen.kid(l,b,brian));f.at(q,P,LastSeen.SPOT[brian].x+2,0,LastSeen.SPOT[brian].z+2);
            h.assertTrue(LastSeen.holding(b,brian)&&(f.own(p,P).getInt("Second")&1<<brian)!=0&&(f.own(q,P).getInt("Second")&1<<brian)==0,"matched for the one who brought it");
            h.assertTrue((f.own(q,P).getInt("SawMatched")&1<<brian)!=0,"seen by the other");
        }).thenIdle(LastSeen.LOOK+2).thenExecute(()->h.assertTrue(said(LastSeen.kid(l,b,brian)).equals(LastSeenTexts.oneThing(brian,false)),"his line to the first: "+said(LastSeen.kid(l,b,brian))))
        .thenWaitUntil(()->h.assertTrue(!LastSeen.holding(b,brian)&&carried(p,brian)==1,"given back to the first"))
        .thenExecute(()->{
            archive(f,p);bayOf(p,b,brian);archive(f,q);bayOf(q,b,brian);h.assertTrue(held(q)==brian,"the other takes it down in turn");
            talk(f,q,LastSeen.kid(l,b,brian));h.assertTrue((f.own(q,P).getInt("Second")&1<<brian)!=0,"and is counted for themselves");
        }).thenIdle(LastSeen.LOOK+2).thenExecute(()->h.assertTrue(said(LastSeen.kid(l,b,brian)).equals(LastSeenTexts.oneThing(brian,true)),"his line to one read as having run: "+said(LastSeen.kid(l,b,brian))))
        .thenWaitUntil(()->h.assertTrue(!LastSeen.holding(b,brian)&&carried(q,brian)==1,"given back to them too"))
        .thenExecute(()->{
            archive(f,q);bayOf(q,b,brian);
            h.assertTrue(f.own(q,P).getInt("Days")==0&&f.own(p,P).getInt("Days")==1,"one match is not four: neither reader's day moves on");
            // Bread for Abril from one; the other's is not taken.
            var abril=LastSeen.kid(l,b,LastSeen.ABRIL);
            p.setItemInHand(InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BREAD));q.setItemInHand(InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BREAD));
            talk(f,p,abril);talk(f,q,abril);
            h.assertTrue(p.getMainHandItem().isEmpty()&&q.getMainHandItem().getCount()==1&&told(q).contains(LastSeenTexts.stillHolding(LastSeen.ABRIL)),"held once, from the first");
            q.setItemInHand(InteractionHand.MAIN_HAND,net.minecraft.world.item.ItemStack.EMPTY);
            south(f,p,LastSeen.SPOT[LastSeen.TOMAS]);south(f,q,LastSeen.SPOT[LastSeen.TOMAS].add(1,0,0));told(p);told(q);LastSeen.at(b,LastSeen.GRAN_AT);
        }).thenWaitUntil(()->h.assertTrue((LastSeen.kid(l,b,LastSeen.TOMAS).marks()&LastSeen.COAT_MARK)!=0,"the coat goes on"))
        .thenExecute(()->{
            h.assertTrue(told(p).contains(LastSeenTexts.COAT_ON)&&told(q).contains(LastSeenTexts.COAT_ON),"both readers near see it");
            done();h.succeed();
        });
    }

    // ------------------------------------------------------------------------------------------------ 0.4.670: the night
    /** A reader who has had both days comes back to the night. */
    private static void nightOf(LiteraryTests.Fixture f,ServerPlayer p,int days){var o=f.own(p,P);o.putInt("Days",days);LiteraryVignettes.save(f.data(),p.getUUID(),P,o);}
    /** A route walked at a walking pace, a little each tick, the reader turned the way they go. */
    private static List<Vec3> pace(Vec3... points){
        var out=new ArrayList<Vec3>();
        for(int i=1;i<points.length;i++){var a=points[i-1];var c=points[i];int n=Math.max(1,(int)Math.ceil(a.distanceTo(c)/.24));for(int k=1;k<=n;k++)out.add(a.lerp(c,k/(double)n));}
        return out;
    }
    private static void step(LiteraryTests.Fixture f,ServerPlayer p,List<Vec3> route,int[] i){
        if(i[0]>=route.size())return;
        var at=route.get(i[0]);var next=route.get(Math.min(route.size()-1,i[0]+1));i[0]++;
        f.at(p,P,at.x,0,at.z);
        if(next.distanceToSqr(at)>1e-4){float yaw=(float)(net.minecraft.util.Mth.atan2(next.z-at.z,next.x-at.x)*net.minecraft.util.Mth.RAD_TO_DEG)-90F;p.setYRot(yaw);p.setYHeadRot(yaw);}
        p.setXRot(0);
    }
    private static int lights(ServerLevel l,BlockPos b){
        int n=0;for(var at:BlockPos.betweenClosed(b.offset(-40,0,-99),b.offset(24,4,-14)))if(l.getBlockState(at).is(net.minecraft.world.level.block.Blocks.LIGHT))n++;return n;
    }
    private static void shut(ServerLevel l,BlockPos b,BlockPos door){var at=b.offset(door);l.setBlock(at,l.getBlockState(at).setValue(DoorBlock.OPEN,false),2);l.setBlock(at.above(),l.getBlockState(at.above()).setValue(DoorBlock.OPEN,false),2);}
    private static boolean doorOpen(ServerLevel l,BlockPos b,BlockPos door){return l.getBlockState(b.offset(door)).getValue(DoorBlock.OPEN);}

    /**
     * One reader through the night. It is the third day for a reader who has had both. Only Nora is in the office; the front door is
     * locked and says so; the city's fence and notices are up from the start; the eight stand facing the avenue. Nora's night lines. The
     * reader takes Lautaro's hand with an empty one: from then until morning they do not go back out the way they came. They walk him
     * from the playground to the archive by the back door, he following a step behind; inside he lets go and goes to stand at his own
     * folder, facing it. Three men come in from the avenue's ends, flashlights lit and a length of wood in two of their hands, and chain
     * the gates; the gate says it is chained; their lights fall where they point. The reader keeps the back door. The men come over the
     * railing: the lamps go out, every child still in the park is gone, and the reader was there. One tries the back door; it holds. At
     * dawn Lautaro is gone too, his folder lies open on the long table at its first page, a gap on its shelf; the gate is open; the hour
     * is first light; the night is the reader's own, with what they brought and that they kept the door; and they may leave again.
     */
    @GameTest(template="empty",batch="last_seen_night",timeoutTicks=7000)
    public static void theNightAndTheDoor(GameTestHelper h){
        var f=place(h);var p=f.player("last_seen_night_reader",P);nightOf(f,p,2);var l=f.level(P);var b=f.base(P);
        int lau=LastSeen.LAUTARO;int[] i={0};var heard=new ArrayList<String>();
        var spotL=LastSeen.SPOT[lau];
        var route=pace(new Vec3(spotL.x-1.0,0,spotL.z+.7),new Vec3(-11.5,0,-18.6),new Vec3(-15.5,0,-15.8),new Vec3(-15.5,0,-13.4),new Vec3(-15.5,0,-11.2));
        h.startSequence().thenIdle(2).thenExecute(()->archive(f,p))
        .thenWaitUntil(()->h.assertTrue(LastSeen.dayNumber(b)==LastSeen.NIGHT&&LastSeen.kid(l,b,lau)!=null,"the night, for a reader who has had both days"))
        .thenExecute(()->{
            h.assertTrue(LastSeen.find(l,b,LastSeen.PATRICIA)==null&&LastSeen.find(l,b,LastSeen.LUZ)==null&&LastSeen.find(l,b,LastSeen.NORA)!=null,"only Nora stays");
            use(p,b,LastSeenSite.FRONT_DOOR,LastSeenSite.FRONT_DOOR.south());h.assertTrue(!doorOpen(l,b,LastSeenSite.FRONT_DOOR)&&told(p).contains(LastSeenTexts.FRONT_LOCKED),"the front door is locked");
            h.assertTrue(fitting(l,b,new BlockPos(13,0,-21),LastSeenBlock.Kind.FENCE)&&LastSeenSite.LAMPS.stream().allMatch(lamp->stage(l,b,lamp)==LastSeenBlock.NOTICE),"the fence and the notices are up already");
            var nora=LastSeen.find(l,b,LastSeen.NORA);talk(f,p,nora);h.assertTrue(said(nora).equals(LastSeenTexts.NORA_STAY),"Nora is staying: "+said(nora));
            var k=LastSeen.kid(l,b,lau);h.assertTrue(facing(k,180),"Lautaro faces the avenue: "+yaw(k));
            h.assertTrue(!LiteraryVignettes.retreatLocked(p),"nothing taken yet: the way back is open");
            LastSeen.at(b,LastSeen.MEN_AT);
            f.at(p,P,spotL.x-1.0,0,spotL.z+.7);talk(f,p,k);
        }).thenIdle(1).thenExecute(()->{
            h.assertTrue(p.getUUID().equals(LastSeen.led(b,lau))&&told(p).contains(LastSeenTexts.takeHand(lau))&&LastSeen.kid(l,b,lau).gesture()==LastSeen.LED,"he takes the reader's hand");
            h.assertTrue(LiteraryVignettes.retreatLocked(p)&&f.own(p,P).getLong("Committed3")!=0,"and the reader does not go back out the way they came until morning");
            open(l,b,LastSeenSite.BACK_DOOR);
        }).thenExecuteFor(route.size()+10,()->step(f,p,route,i))
        .thenWaitUntil(()->h.assertTrue(LastSeen.inside(b,lau),"he comes into the archive: "+LastSeen.kid(l,b,lau).position().subtract(Vec3.atLowerCornerOf(b))+" led "+LastSeen.led(b,lau)))
        .thenExecute(()->h.assertTrue(told(p).contains(LastSeenTexts.atShelf(lau))&&(f.own(p,P).getInt("LedIn")&1<<lau)!=0&&LastSeen.led(b,lau)==null,"lets go, and the reader brought him in"))
        .thenWaitUntil(()->{var k=LastSeen.kid(l,b,lau);var front=LastSeen.shelfFront(b,lau);h.assertTrue(k.position().distanceToSqr(front)<.1&&facing(k,LastSeen.shelfYaw(lau)),"he stands at his own folder, facing it: "+k.position()+" "+front);})
        .thenExecute(()->{talk(f,p,LastSeen.kid(l,b,lau));h.assertTrue(told(p).contains(LastSeenTexts.atFolder(lau)),"and does not turn round");})
        .thenWaitUntil(()->h.assertTrue(LastSeen.chained(b),"the men come and chain the gates: beat "+LastSeen.menBeat(b)))
        .thenExecute(()->{
            for(int m=0;m<LastSeen.MEN;m++){var a=LastSeen.man(l,b,m);h.assertTrue(a!=null&&a.getMainHandItem().is(LastSeenRegistry.FLASHLIGHT.get())&&a.gesture()==LastSeen.FLASH&&a.getOffhandItem().is(LastSeenRegistry.STICK.get())==(m!=1),"each man with his flashlight out, two with a length of wood: "+m);}
            h.assertTrue(stage(l,b,LastSeenSite.GATE_W)==LastSeenBlock.CHAINED,"the gate is chained");
            h.assertTrue(lights(l,b)>0,"their lights fall where they point");
            var nora=LastSeen.find(l,b,LastSeen.NORA);h.assertTrue(said(nora).equals(LastSeenTexts.NORA_CHAINED),"Nora tells the archive: "+said(nora));
            shut(l,b,LastSeenSite.BACK_DOOR);f.at(p,P,LastSeen.KEEP.x,0,LastSeen.KEEP.z);p.setYRot(180);p.setYHeadRot(180);told(p);
            LastSeen.at(b,LastSeen.OVER_AT);
        }).thenWaitUntil(()->h.assertTrue(LastSeen.over(b),"the men come over the railing"))
        .thenWaitUntil(()->h.assertTrue(java.util.Arrays.stream(LastSeen.EIGHT).filter(c->c!=lau).allMatch(c->LastSeen.kid(l,b,c)==null),"every one of them still in the park is gone"))
        .thenExecute(()->{
            h.assertTrue(LastSeen.kid(l,b,lau)!=null,"the one at his shelf is not");
            heard.addAll(told(p));
            h.assertTrue(f.own(p,P).getLong("NightOver")!=0&&heard.contains(LastSeenTexts.OVER_OFFICE),"the reader was there when they came");
            h.assertTrue(LastSeenSite.LAMPS.stream().allMatch(lamp->stage(l,b,lamp.above(2))==LastSeenBlock.DARK),"the lamps are out");
        }).thenWaitUntil(()->h.assertTrue(f.own(p,P).getLong("KeepCue")!=0,"the reader keeping the door is told"))
        .thenExecute(()->{heard.addAll(told(p));h.assertTrue(heard.contains(LastSeenTexts.KEEP)&&LiteraryVignettes.retreatLocked(p),"keeping the door, and held to it");})
        .thenWaitUntil(()->h.assertTrue(LastSeen.tried(b),"one of them tries the back door: beat "+LastSeen.menBeat(b)+" leg "+LastSeen.leg(b,0)+" "+(LastSeen.man(l,b,0)==null?null:LastSeen.man(l,b,0).position().subtract(Vec3.atLowerCornerOf(b)))))
        .thenExecute(()->{
            h.assertTrue(LastSeen.held(b)&&!doorOpen(l,b,LastSeenSite.BACK_DOOR)&&told(p).contains(LastSeenTexts.KEPT)&&f.own(p,P).getLong("KeptNight")!=0,"it holds");
            LastSeen.at(b,LastSeen.DAWN_AT+400);
        }).thenWaitUntil(()->h.assertTrue(LastSeen.dawn(b),"dawn"))
        .thenExecute(()->{
            var own=f.own(p,P);
            h.assertTrue(LastSeen.kid(l,b,lau)==null&&LastSeen.openOn(b,LastSeen.TABLE_E).contains(lau)&&stage(l,b,new BlockPos(LastSeen.TABLE_E,0,-7))==3,"he is gone too; his folder lies open on the long table");
            h.assertTrue(stage(l,b,LastSeen.gapCell(lau))==LastSeenBlock.ONE_OUT,"a gap on his shelf");
            h.assertTrue(own.getInt("Days")==3&&(own.getInt("Brought")&1<<lau)!=0&&own.getBoolean("KeptDoor"),"the night is the reader's own: "+own);
            h.assertTrue(!LiteraryVignettes.retreatLocked(p),"and they may go");
            h.assertTrue(stage(l,b,LastSeenSite.GATE_W)==LastSeenBlock.OPEN&&lights(l,b)==0,"the gate open, the lights gone");
            var got=sent(p);
            h.assertTrue(lines(got).contains(LastSeenTexts.DAWN_TABLE),"the reader sees the table");
            var hours=hours(got);h.assertTrue(!hours.isEmpty()&&hours.get(hours.size()-1)>=LastSeen.DAWN_TIME,"first light: "+hours);
            use(p,b,new BlockPos(LastSeen.TABLE_E,0,-7),new BlockPos(LastSeen.TABLE_E,0,-6));var shown=sent(p);h.assertTrue(opened(shown,lau)&&lines(shown).contains(LastSeenTexts.tableOpen(lau)),"his folder, open at the first page: "+lines(shown));
            var nora=LastSeen.find(l,b,LastSeen.NORA);talk(f,p,nora);h.assertTrue(said(nora).equals(LastSeenTexts.NORA_DAWN),"Nora was here all night: "+said(nora));
            done();h.succeed();
        });
    }

    /**
     * Three readers and a spectator through one night. One takes Lautaro's hand; another cannot take a hand already held; the first cannot
     * take a second; crouching lets go, and he goes back to his place. The second leaves before taking a hand, and the way back was open
     * for them. The third has had only one day, and the night is the furthest any of them has reached. The first is out in the park with
     * him by the hand when the men come: the dark comes down, the hand is empty, and he is gone. Nobody keeps the door: it opens and the
     * light goes along the shelves, and it is shut again. At dawn the night is the first reader's only: not the one who left, not the one
     * who had not had the second day, and not the spectator. Nobody stood at the shelves, and the table is bare.
     */
    @GameTest(template="empty",batch="last_seen_night_three",timeoutTicks=7000)
    public static void threeReadersOneNight(GameTestHelper h){
        var f=place(h);var p=f.player("last_seen_night_a",P);var q=f.player("last_seen_night_b",P);var r=f.player("last_seen_night_c",P);var w=f.player("last_seen_night_w",P);
        nightOf(f,p,2);nightOf(f,q,2);nightOf(f,r,1);nightOf(f,w,2);var l=f.level(P);var b=f.base(P);
        int lau=LastSeen.LAUTARO;var spotL=LastSeen.SPOT[lau];
        h.startSequence().thenIdle(2).thenExecute(()->{archive(f,p);archive(f,q);archive(f,r);w.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);archive(f,w);})
        .thenWaitUntil(()->h.assertTrue(LastSeen.dayNumber(b)==LastSeen.NIGHT&&LastSeen.kid(l,b,lau)!=null&&LastSeen.kid(l,b,LastSeen.BRIAN)!=null,"the night: the furthest any of them has reached"))
        .thenExecute(()->{
            LastSeen.at(b,LastSeen.MEN_AT);
            var k=LastSeen.kid(l,b,lau);
            f.at(p,P,spotL.x-1.0,0,spotL.z+.7);talk(f,p,k);h.assertTrue(p.getUUID().equals(LastSeen.led(b,lau)),"the first takes his hand");
            f.at(q,P,spotL.x+1.0,0,spotL.z+.7);talk(f,q,k);h.assertTrue(told(q).contains(LastSeenTexts.heldOther(lau))&&p.getUUID().equals(LastSeen.led(b,lau)),"the second cannot take a hand already held");
            var brian=LastSeen.kid(l,b,LastSeen.BRIAN);f.at(p,P,LastSeen.SPOT[LastSeen.BRIAN].x+1,0,LastSeen.SPOT[LastSeen.BRIAN].z+1);LiteraryVignettes.talk(p,brian);
            h.assertTrue(told(p).contains(LastSeenTexts.HAND_FULL)&&LastSeen.led(b,LastSeen.BRIAN)==null,"the first cannot take a second");
            f.at(p,P,spotL.x-1.0,0,spotL.z+.7);p.setShiftKeyDown(true);
        }).thenIdle(2).thenExecute(()->{
            h.assertTrue(LastSeen.led(b,lau)==null&&told(p).contains(LastSeenTexts.letGo(lau))&&LastSeen.kid(l,b,lau).gesture()==0,"crouching lets go");
            p.setShiftKeyDown(false);
            h.assertTrue(!LiteraryVignettes.retreatLocked(q),"the second took no hand: the way back is open");
            f.leave(q);
            talk(f,p,LastSeen.kid(l,b,lau));h.assertTrue(p.getUUID().equals(LastSeen.led(b,lau)),"and takes it again");
            f.at(r,P,-12.5,0,-6.5);
        }).thenWaitUntil(()->h.assertTrue(LastSeen.chained(b),"the gates are chained"))
        .thenExecute(()->{told(p);told(r);LastSeen.at(b,LastSeen.OVER_AT);})
        .thenWaitUntil(()->h.assertTrue(LastSeen.over(b),"the men come over"))
        .thenWaitUntil(()->h.assertTrue(LastSeen.kid(l,b,lau)==null,"he is gone"))
        .thenExecute(()->{
            var got=sent(p);var said=lines(got);
            h.assertTrue(said.contains(LastSeenTexts.EMPTY_HAND)&&said.contains(LastSeenTexts.OVER_PARK)&&LastSeen.led(b,lau)==null,"the hand is empty: "+said);
            h.assertTrue(got.stream().anyMatch(x->x instanceof net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket c&&c.payload() instanceof io.github.knaitoe.theoldesthouse.network.HouseFadePayload),"the dark comes down out there");
            h.assertTrue(f.own(q,P).getLong("NightOver")==0&&f.own(w,P).getLong("NightOver")==0&&f.own(r,P).getLong("NightOver")!=0,"the one who left and the spectator were not there");
            told(r);
        }).thenWaitUntil(()->h.assertTrue(LastSeen.tried(b),"the back door is tried: beat "+LastSeen.menBeat(b)+" leg "+LastSeen.leg(b,0)+" "+(LastSeen.man(l,b,0)==null?null:LastSeen.man(l,b,0).position().subtract(Vec3.atLowerCornerOf(b)))))
        .thenExecute(()->{
            h.assertTrue(!LastSeen.held(b)&&doorOpen(l,b,LastSeenSite.BACK_DOOR)&&told(r).contains(LastSeenTexts.TRY_OPEN),"nobody keeps it: it opens");
        }).thenIdle(20).thenExecute(()->{int n=0;for(int x=-22;x<=-10;x++)if(l.getBlockState(b.offset(x,1,-10)).is(net.minecraft.world.level.block.Blocks.LIGHT))n++;h.assertTrue(n==1,"a light goes along the shelves: "+n);})
        .thenWaitUntil(()->h.assertTrue(!doorOpen(l,b,LastSeenSite.BACK_DOOR),"and it is shut again"))
        .thenExecute(()->LastSeen.at(b,LastSeen.DAWN_AT+400))
        .thenWaitUntil(()->h.assertTrue(LastSeen.dawn(b),"dawn"))
        .thenExecute(()->{
            var a=f.own(p,P);
            h.assertTrue(a.getInt("Days")==3&&a.getInt("Brought")==0&&!a.getBoolean("KeptDoor")&&(a.getInt("TookHand")&1<<lau)!=0,"the night is the first reader's: "+a);
            h.assertTrue(f.own(q,P).getInt("Days")==2&&f.own(r,P).getInt("Days")==1&&f.own(w,P).getInt("Days")==2,"and nobody else's");
            h.assertTrue(told(r).contains(LastSeenTexts.DAWN_EMPTY)&&told(p).contains(LastSeenTexts.DAWN)&&stage(l,b,new BlockPos(LastSeen.TABLE_E,0,-7))==0,"nobody stood at the shelves, and the table is bare");
            done();h.succeed();
        });
    }

    // ------------------------------------------------------------------------------------------------ 0.4.671, after
    private static void afterOf(LiteraryTests.Fixture f,ServerPlayer p){var o=f.own(p,P);o.putInt("Days",3);o.putString("FolderName",p.getGameProfile().getName());LiteraryVignettes.save(f.data(),p.getUUID(),P,o);}
    private static @javax.annotation.Nullable io.github.knaitoe.theoldesthouse.network.LastSeenDrawerPayload drawer(List<Object> sent){
        for(var x:sent)if(x instanceof net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket c&&c.payload() instanceof io.github.knaitoe.theoldesthouse.network.LastSeenDrawerPayload d)return d;
        return null;
    }
    private static String pages(ServerPlayer p,LiteraryTests.Fixture f){return LiteraryTexts.ending(p.getGameProfile().getName(),P,f.own(p,P)).get(DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().map(x->x.raw().getString()).reduce("",(x,y)->x+" "+y).replaceAll("\\s+"," ");}

    /**
     * 0.4.671: the day after, for one reader who has had the night (they brought Lautaro in by the back door and kept it; he was gone at
     * dawn), whom the House took in on the evening of its fourth day. Mid-morning and nobody in the park; the city's fence lies flat; the
     * notices are torn; the gate is open; Lautaro's folder lies open on the long table, a gap on his shelf; Nora's drawer is unlocked and
     * she says so. A hand on her desk: the drawer comes out, and in it is the reader's own folder, with their own face, their name and the
     * day the House took them in; Nora says whose it is; the reader is ready. The account under it is what they did, and not what they did
     * not. Read after the card, it is theirs. Nora: it stays open.
     */
    @GameTest(template="empty",batch="last_seen_after",timeoutTicks=3000)
    public static void theDayAfterAndTheDrawer(GameTestHelper h){
        var f=place(h);var p=f.player("last_seen_after_reader",P);var l=f.level(P);var b=f.base(P);
        int lau=LastSeen.LAUTARO;
        var o=f.own(p,P);o.putInt("Days",3);o.putBoolean("Rule",true);o.putInt("Matched",1<<LastSeen.ROCIO);o.putInt("Second",1<<LastSeen.TOMAS|1<<LastSeen.ABRIL);
        o.putInt("TookHand",1<<lau);o.putInt("Brought",1<<lau);o.putBoolean("KeptDoor",true);o.putInt("Gone",1<<lau);LiteraryVignettes.save(f.data(),p.getUUID(),P,o);
        io.github.knaitoe.theoldesthouse.opening.OpeningSequence.state(p).markEntered(3,13000);
        h.startSequence().thenIdle(2).thenExecute(()->archive(f,p))
        .thenWaitUntil(()->h.assertTrue(LastSeen.dayNumber(b)==LastSeen.AFTER,"the day after, for a reader who has had the night: "+LastSeen.dayNumber(b)))
        .thenIdle(25).thenExecute(()->{
            h.assertTrue(told(p).contains(LastSeenTexts.AFTER_OFFICE),"told what the morning is");
            h.assertTrue(java.util.Arrays.stream(LastSeen.EIGHT).allMatch(c->LastSeen.kid(l,b,c)==null)&&LastSeen.cornerActor(l,b,true)==null,"nobody in the park");
            h.assertTrue(stage(l,b,new BlockPos(13,0,-21))>=LastSeenBlock.FALLEN&&!fitting(l,b,new BlockPos(13,1,-21),LastSeenBlock.Kind.FENCE),"the city's fence lies flat: "+stage(l,b,new BlockPos(13,0,-21)));
            h.assertTrue(LastSeenSite.LAMPS.stream().allMatch(lamp->stage(l,b,lamp)==LastSeenBlock.TORN),"the notices are torn");
            h.assertTrue(stage(l,b,LastSeenSite.GATE_W)==LastSeenBlock.OPEN,"the gate is open");
            h.assertTrue(stage(l,b,new BlockPos(LastSeen.TABLE_E,0,-7))==3&&gap(l,b,lau)==LastSeenBlock.ONE_OUT,"his folder lies open on the long table, a gap on his shelf");
            h.assertTrue(stage(l,b,LastSeenSite.DESK)==LastSeenBlock.UNLOCKED,"Nora's drawer is unlocked");
            var nora=LastSeen.find(l,b,LastSeen.NORA);talk(f,p,nora);h.assertTrue(said(nora).equals(LastSeenTexts.NORA_DRAWER),"Nora says so: "+said(nora));
            h.assertTrue(!f.own(p,P).getBoolean("Ready"),"nothing counts yet");
            f.click(p,P,LastSeenSite.DESK);
            var got=sent(p);var d=drawer(got);
            h.assertTrue(d!=null&&d.folders().size()==1&&d.folders().get(0).reader().equals(p.getUUID()),"the drawer holds their own folder: "+d);
            var mine=d.folders().get(0);
            h.assertTrue(mine.name().equals(p.getGameProfile().getName())&&mine.on().equals("the evening of day four")&&mine.file().equals(LastSeenTexts.file(p.getUUID())),"their name, and the day the House took them in: "+mine);
            h.assertTrue(lines(got).contains(LastSeenTexts.DRAWER)&&stage(l,b,LastSeenSite.DESK)==LastSeenBlock.DRAWN,"the drawer is out, theirs on top: "+lines(got));
            h.assertTrue(said(LastSeen.find(l,b,LastSeen.NORA)).equals(LastSeenTexts.NORA_YOURS),"Nora says whose it is");
            var own=f.own(p,P);h.assertTrue(own.getBoolean("Ready")&&own.getString("Outcome").equals("found_their_own_folder"),"they are ready: "+own);
            var text=pages(p,f);
            h.assertTrue(text.contains("Nora kept them")&&text.contains("I took Lautaro by the hand.")&&text.contains("and it held")&&text.contains("Lautaro was gone from the shelves")
                &&text.contains("the evening of day four")&&text.contains("Folder stays open.")&&text.contains("She keeps it with the others. A folder stays open."),"the account is what they did: "+text);
            h.assertFalse(text.contains("girl with one arm")||text.contains("Under mine"),"and not what they did not: "+text);
            f.source(p,P);f.read(p,P,LastSeenSite.DESK);
            h.assertTrue(f.own(p,P).getBoolean("Completed")&&WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.LAST_SEEN),"read after the card, it is theirs: "+f.own(p,P));
            var again=LastSeen.find(l,b,LastSeen.NORA);talk(f,p,again);h.assertTrue(said(again).equals(LastSeenTexts.NORA_OPEN_AFTER),"it stays open: "+said(again));
            done();h.succeed();
        });
    }

    /**
     * Three readers and a spectator on the day after. Two have had the night; the third has had only two days; the spectator was there
     * through the night, but a spectator is never counted at dawn and has no folder. Each of the two finds their own folder first and the other's under it; each account names the other's.
     * The third is shown the two others' folders and told there is none of theirs, and nothing counts for them. The spectator opens nothing.
     */
    @GameTest(template="empty",batch="last_seen_after_two",timeoutTicks=3000)
    public static void readersFindEachOthersFolders(GameTestHelper h){
        var f=place(h);var p=f.player("last_seen_after_a",P);var q=f.player("last_seen_after_b",P);var r=f.player("last_seen_after_c",P);var w=f.player("last_seen_after_w",P);
        afterOf(f,p);afterOf(f,q);nightOf(f,r,2);nightOf(f,w,3);var l=f.level(P);var b=f.base(P);
        String a=p.getGameProfile().getName(),bn=q.getGameProfile().getName();
        h.startSequence().thenIdle(2).thenExecute(()->{archive(f,p);archive(f,q);archive(f,r);w.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);archive(f,w);})
        .thenWaitUntil(()->h.assertTrue(LastSeen.dayNumber(b)==LastSeen.AFTER,"the day after: the furthest any of them has reached"))
        .thenIdle(2).thenExecute(()->{
            sent(p);sent(q);sent(r);sent(w);
            f.click(p,P,LastSeenSite.DESK);var dp=drawer(sent(p));
            h.assertTrue(dp!=null&&dp.folders().size()==2&&dp.folders().get(0).reader().equals(p.getUUID())&&dp.folders().get(1).reader().equals(q.getUUID()),"the first finds their own folder, and the other's under it: "+dp);
            f.click(q,P,LastSeenSite.DESK);var dq=drawer(sent(q));
            h.assertTrue(dq!=null&&dq.folders().size()==2&&dq.folders().get(0).reader().equals(q.getUUID())&&dq.folders().get(1).name().equals(a),"and the second theirs first: "+dq);
            f.click(r,P,LastSeenSite.DESK);var got=sent(r);var dr=drawer(got);
            h.assertTrue(dr!=null&&dr.folders().size()==2&&dr.folders().stream().noneMatch(x->x.reader().equals(r.getUUID()))&&lines(got).contains(LastSeenTexts.NO_FOLDER),"the third is shown theirs, and none of their own: "+lines(got));
            h.assertTrue(!f.own(r,P).getBoolean("Ready"),"nothing counts for them");
            f.click(w,P,LastSeenSite.DESK);
            h.assertTrue(drawer(sent(w))==null&&!f.own(w,P).getBoolean("Ready"),"the spectator opens nothing");
            h.assertTrue(f.own(p,P).getBoolean("Ready")&&f.own(q,P).getBoolean("Ready"),"the two are ready");
            h.assertTrue(pages(p,f).contains("Under mine were "+bn+"'s.")&&pages(q,f).contains("Under mine were "+a+"'s."),"each account names the other's folder");
            done();h.succeed();
        });
    }

    /**
     * The Corner's girl, once. One reader has finished The House on the Corner and another has not. While both are in the archive and
     * nobody can see the fountain, she comes: the girl with one arm at the rim, facing the avenue, and her brother, grown, across it, facing
     * the park. The one who has not finished the Corner sees her and is not marked; the one who has is. A hand on her: she turns her head,
     * and says nothing. On him: he does not cross. Once they are both indoors again and nobody sees, the two are gone, and they do not come
     * again that day. Only the first reader's account has her in it.
     */
    @GameTest(template="empty",batch="last_seen_after_corner",timeoutTicks=3000)
    public static void theCornersGirlOnce(GameTestHelper h){
        var f=place(h);var p=f.player("last_seen_corner_a",P);var q=f.player("last_seen_corner_b",P);
        afterOf(f,p);afterOf(f,q);var l=f.level(P);var b=f.base(P);
        WitnessAccount.resolve(f.data(),p.getUUID(),WitnessAccount.Story.CORNER_HOUSE,"took_the_photograph");
        var rim=LastSeen.RIM;
        h.startSequence().thenIdle(2).thenExecute(()->{archive(f,p);archive(f,q);})
        .thenWaitUntil(()->h.assertTrue(LastSeen.dayNumber(b)==LastSeen.AFTER,"the day after"))
        .thenExecute(()->{h.assertTrue(!LastSeen.romina(b),"not yet");LastSeen.at(b,LastSeen.GIRL_AT);})
        .thenWaitUntil(()->h.assertTrue(LastSeen.romina(b),"while nobody sees the fountain, she comes"))
        .thenExecute(()->{
            var girl=LastSeen.cornerActor(l,b,true);var man=LastSeen.cornerActor(l,b,false);
            h.assertTrue(girl!=null&&girl.phase()==Corner.ROMINA&&girl.position().distanceToSqr(Vec3.atLowerCornerOf(b).add(rim))<.1&&facing(girl,180),"the girl with one arm at the rim, facing the avenue: "+(girl==null?null:girl.position()));
            h.assertTrue(man!=null&&man.phase()==Corner.NACHO_GROWN&&man.position().distanceToSqr(Vec3.atLowerCornerOf(b).add(LastSeen.NACHO_AT))<.1&&facing(man,0),"her brother across the avenue, facing the park");
            f.at(q,P,rim.x,0,rim.z+5);q.setYRot(180);q.setYHeadRot(180);q.setXRot(0);
        }).thenIdle(12).thenExecute(()->{
            h.assertTrue(!f.own(q,P).getBoolean("SawRomina")&&LastSeen.romina(b),"one who has not finished the Corner is not marked, and she stays");
            f.at(p,P,rim.x+1,0,rim.z+5);p.setYRot(180);p.setYHeadRot(180);p.setXRot(0);
        }).thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("SawRomina"),"the one who has, seeing her, is"))
        .thenExecute(()->{
            var girl=LastSeen.cornerActor(l,b,true);talk(f,p,girl);
            h.assertTrue(told(p).contains(LastSeenTexts.ROMINA_TURNS)&&f.own(p,P).getBoolean("RominaTurned"),"a hand on her: she turns her head");
            talk(f,q,girl);h.assertTrue(told(q).contains(LastSeenTexts.ROMINA_TURNS)&&!f.own(q,P).getBoolean("RominaTurned"),"to the other too, but it is not theirs");
            var man=LastSeen.cornerActor(l,b,false);talk(f,p,man);h.assertTrue(told(p).contains(LastSeenTexts.NACHO_ACROSS),"he does not cross");
            h.assertTrue(said(girl).isEmpty()&&said(man).isEmpty(),"neither says a word");
            archive(f,p);archive(f,q);
        }).thenWaitUntil(()->h.assertTrue(!LastSeen.romina(b)&&LastSeen.cornerActor(l,b,true)==null&&LastSeen.cornerActor(l,b,false)==null,"once nobody sees, they are gone"))
        .thenIdle(30).thenExecute(()->{
            h.assertTrue(LastSeen.cornerActor(l,b,true)==null,"and she does not come again");
            h.assertTrue(pages(p,f).contains("a girl with one arm")&&pages(p,f).contains("turned her head to me"),"the first reader's account has her: "+pages(p,f));
            h.assertFalse(pages(q,f).contains("girl with one arm"),"the second's does not");
            done();h.succeed();
        });
    }
}
