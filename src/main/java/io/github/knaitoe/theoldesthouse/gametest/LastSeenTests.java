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
}
