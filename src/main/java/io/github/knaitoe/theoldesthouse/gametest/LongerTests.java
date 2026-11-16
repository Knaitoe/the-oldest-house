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
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * 0.4.621, A Little Longer (docs/A_LITTLE_LONGER_PLAN.md). Each case builds the place itself, one case to a batch, as the literary
 * cases do.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_longer") @PrefixGameTestTemplate(false)
public final class LongerTests {
    private LongerTests(){}
    private static final LabyrinthPlace P=LabyrinthPlace.LITTLE_LONGER;
    private static LiteraryTests.Fixture active;
    private static LiteraryTests.Fixture shop(GameTestHelper h){done();active=new LiteraryTests.Fixture(h);return active;}
    private static void done(){if(active!=null){active.close();active=null;}}
    private static boolean is(ServerLevel l,BlockPos b,int x,int y,int z,String material){return l.getBlockState(b.offset(x,y,z)).is(LongerRegistry.material(material));}
    private static boolean fitting(ServerLevel l,BlockPos b,BlockPos at,LongerBlock.Kind kind){return LongerBlock.is(l.getBlockState(b.offset(at)),kind);}
    private static List<String> told(ServerPlayer p){
        NativeTestPlayers.flush(p);
        var out=new java.util.ArrayList<String>();
        for(var packet:NativeTestPlayers.packets(p))if(packet instanceof net.minecraft.network.protocol.game.ClientboundSystemChatPacket chat)out.add(chat.content().getString());
        return out;
    }
    /** How high a body stands in this cell: on top of what is in it (a tread), or on what is under it; NaN if it cannot stand there. */
    private static double feet(ServerLevel l,BlockPos b,BlockPos cell){
        var at=b.offset(cell);var here=l.getBlockState(at).getCollisionShape(l,at);
        double f;
        if(here.isEmpty()){var below=l.getBlockState(at.below()).getCollisionShape(l,at.below());if(below.isEmpty())return Double.NaN;f=at.getY()-1+below.max(Direction.Axis.Y);if(f<at.getY()-.51)return Double.NaN;}
        else{double top=here.max(Direction.Axis.Y);if(top>1.01)return Double.NaN;f=at.getY()+top;}
        var body=new AABB(at.getX()+.2,f+.01,at.getZ()+.2,at.getX()+.8,f+1.79,at.getZ()+.8);
        return l.noCollision(body)?f:Double.NaN;
    }
    /**
     * Every cell a body can stand in and walk to from a start, without opening anything: across level ground, over a half-cell sill, up
     * a stair the way it rises (a whole cell, a half at a time) and down it again, through any gap a body fits across the edge.
     */
    private static Set<BlockPos> walked(ServerLevel l,BlockPos b,BlockPos from){
        var seen=new HashSet<BlockPos>();var queue=new ArrayDeque<BlockPos>();seen.add(from);queue.add(from);
        while(!queue.isEmpty()){
            var at=queue.poll();double f=feet(l,b,at);if(Double.isNaN(f))continue;
            for(var d:Direction.Plane.HORIZONTAL)for(int dy=-1;dy<=1;dy++){
                var to=at.relative(d).above(dy);if(seen.contains(to)||!LongerSite.inside(net.minecraft.world.phys.Vec3.atBottomCenterOf(to)))continue;
                double g=feet(l,b,to);if(Double.isNaN(g))continue;
                var toState=l.getBlockState(b.offset(to));var atState=l.getBlockState(b.offset(at));
                boolean stairUp=toState.getBlock() instanceof StairBlock&&toState.getValue(StairBlock.FACING)==d;
                boolean stairDown=atState.getBlock() instanceof StairBlock&&atState.getValue(StairBlock.FACING)==d.getOpposite();
                double rise=g-f;
                if(rise>.6&&!(stairUp&&rise<=1.01))continue;
                if(rise<-.6&&!(stairDown&&rise>=-1.01)&&rise<-1.3)continue;
                double y=Math.max(f,g);
                var a=b.offset(at);var c=b.offset(to);
                var edge=new AABB((a.getX()+c.getX())/2.0+.2,y+.01,(a.getZ()+c.getZ())/2.0+.2,(a.getX()+c.getX())/2.0+.8,y+1.79,(a.getZ()+c.getZ())/2.0+.8);
                if(!l.noCollision(edge))continue;
                seen.add(to);queue.add(to);
            }
        }
        seen.removeIf(c->Double.isNaN(feet(l,b,c)));
        return seen;
    }
    private static void open(ServerLevel l,BlockPos b,BlockPos door,boolean open){var at=b.offset(door);var s=l.getBlockState(at);((DoorBlock)s.getBlock()).setOpen(null,l,s,at,open);}

    /**
     * Rowan's shop. Built of its own things: brick and plaster, the shop's boards and the flat's, the bathroom's tiles, felt on the roof,
     * the House's paper and runner down its corridor. The stair up the back wall and the stair to the roof are clear to climb. Shut, a
     * body walks only the House's corridor; with the shop's back door open, the shop, the stair and the flat, and with the bathroom and
     * the roof doors open, those too, out to the bench under the awning. The shop door onto the street stays shut and says so. Every
     * fitting answers a hand; the lamp, the radio and the toaster change. The roof's parapet stands too high to step over. The work order
     * is on its spike; nobody is here, and the House does not deal the place.
     */
    @GameTest(template="empty",batch="longer_built",timeoutTicks=1200)
    public static void theRadioShopIsBuiltOfItsOwnThingsAndEveryDoorAndFittingAnswers(GameTestHelper h){
        var f=shop(h);var p=f.player("longer_reader",P);var l=f.level(P);var b=f.base(P);
        h.startSequence().thenIdle(5).thenExecute(()->{
            h.assertTrue(is(l,b,-7,1,-12,"plaster")&&is(l,b,0,-1,-12,"boards")&&is(l,b,0,LongerSite.UP-1,-14,"flatboards")&&is(l,b,-4,LongerSite.UP-1,-9,"tile")&&is(l,b,0,LongerSite.ROOF-1,-14,"felt"),
                "plaster in the shop, its boards, the flat's boards, the bathroom's tiles, felt on the roof");
            h.assertTrue(is(l,b,-2,1,-3,"corridor")&&is(l,b,0,-1,-3,"runner")&&is(l,b,-7,LongerSite.ROOF+1,-12,"brick")&&is(l,b,0,LongerSite.ROOF+2,-19,"coping"),"the House's corridor, the parapet's brick and coping");
            h.assertTrue(is(l,b,0,4,-12,"ceiling")&&is(l,b,0,LongerSite.UP+4,-14,"ceiling"),"a ceiling over the shop and over the flat, under the roof's felt");
            // The two stairs, every tread with room to stand on it.
            for(int i=0;i<6;i++){var s=l.getBlockState(b.offset(1+i,i,-8));h.assertTrue(s.getBlock() instanceof StairBlock&&s.getValue(StairBlock.FACING)==Direction.EAST,"a tread of the shop's stair at "+i);h.assertFalse(Double.isNaN(feet(l,b,new BlockPos(1+i,i,-8))),"room to stand on the shop's stair, tread "+i);}
            for(int i=0;i<6;i++){var s=l.getBlockState(b.offset(-6,LongerSite.UP+i,-17+i));h.assertTrue(s.getBlock() instanceof StairBlock&&s.getValue(StairBlock.FACING)==Direction.SOUTH,"a tread of the roof stair at "+i);h.assertFalse(Double.isNaN(feet(l,b,new BlockPos(-6,LongerSite.UP+i,-17+i))),"room to stand on the roof stair, tread "+i);}
            // The fittings where they belong.
            for(int i=0;i<3;i++)h.assertTrue(fitting(l,b,LongerSite.BENCH_PARTS.get(i),LongerBlock.Kind.WORKBENCH)&&LongerBlock.stage(l.getBlockState(b.offset(LongerSite.BENCH_PARTS.get(i))))==i,"the workbench, part "+i);
            h.assertTrue(fitting(l,b,LongerSite.LAMP,LongerBlock.Kind.LAMP)&&fitting(l,b,LongerSite.RADIO,LongerBlock.Kind.RADIO)&&fitting(l,b,LongerSite.TRAYS,LongerBlock.Kind.TRAYS)&&fitting(l,b,LongerSite.ORDER,LongerBlock.Kind.ORDER)
                &&fitting(l,b,LongerSite.CHAIR,LongerBlock.Kind.CHAIR),"on the bench the lamp, the radio, the trays; the order over it; the chair in front");
            h.assertTrue(fitting(l,b,LongerSite.HOOK,LongerBlock.Kind.HOOK)&&fitting(l,b,LongerSite.KETTLE,LongerBlock.Kind.KETTLE)&&fitting(l,b,LongerSite.TOASTER,LongerBlock.Kind.TOASTER)&&fitting(l,b,LongerSite.SINK,LongerBlock.Kind.WORKTOP)
                &&fitting(l,b,LongerSite.TABLE,LongerBlock.Kind.TABLE)&&fitting(l,b,LongerSite.CUPS,LongerBlock.Kind.CUPS)&&fitting(l,b,LongerSite.ARMCHAIR,LongerBlock.Kind.ARMCHAIR)&&fitting(l,b,LongerSite.KITCHEN_CHAIR,LongerBlock.Kind.KITCHEN_CHAIR),
                "in the flat the hook, the kettle and toaster, the sink, the table with the cups, the armchair and the kitchen chair");
            h.assertTrue(fitting(l,b,LongerSite.BASIN,LongerBlock.Kind.BASIN)&&fitting(l,b,LongerSite.LAVATORY,LongerBlock.Kind.LAVATORY)&&fitting(l,b,LongerSite.BENCH,LongerBlock.Kind.BENCH)&&fitting(l,b,LongerSite.TANK,LongerBlock.Kind.TANK),"the bathroom's basin and lavatory; the roof's bench and tank");
            for(var w:LongerSite.windows())h.assertTrue(fitting(l,b,w,LongerBlock.Kind.WINDOW)&&fitting(l,b,w.above(),LongerBlock.Kind.WINDOW)&&LongerBlock.stage(l.getBlockState(b.offset(w.above())))==1,"a rain window, both halves, at "+w);
            // Seats are sat in; the chairs face what they belong to.
            h.assertTrue(LongerBlock.seat(l.getBlockState(b.offset(LongerSite.ARMCHAIR)))&&l.getBlockState(b.offset(LongerSite.ARMCHAIR)).getValue(LongerBlock.FACING)==Direction.EAST
                &&l.getBlockState(b.offset(LongerSite.KITCHEN_CHAIR)).getValue(LongerBlock.FACING)==Direction.WEST&&l.getBlockState(b.offset(LongerSite.CHAIR)).getValue(LongerBlock.FACING)==Direction.WEST,"the two chairs face each other across the table; the bench chair faces the bench");
            // Shut, a body walks the corridor and no further.
            var shut=walked(l,b,new BlockPos(0,0,-1));
            h.assertTrue(shut.contains(new BlockPos(0,0,-6))&&shut.contains(new BlockPos(1,0,-2)),"the corridor, end to end: "+shut.size());
            h.assertFalse(shut.contains(new BlockPos(0,0,-12)),"not into the shop while its back door is shut");
            // The back door opened: the shop, up the stair, the flat; not the bathroom or the roof while their doors are shut.
            open(l,b,LongerSite.BACK_DOOR,true);
            var in=walked(l,b,new BlockPos(0,0,-1));
            for(var cell:List.of(new BlockPos(-4,0,-11),new BlockPos(0,0,-17),new BlockPos(5,0,-12),new BlockPos(6,LongerSite.UP,-9),new BlockPos(1,LongerSite.UP,-15),new BlockPos(-5,LongerSite.UP,-18)))
                h.assertTrue(in.contains(cell),"a body walks to "+cell+" of "+in.size());
            for(var cell:List.of(new BlockPos(-4,LongerSite.UP,-9),new BlockPos(3,LongerSite.ROOF,-14),new BlockPos(0,0,-21)))
                h.assertFalse(in.contains(cell),"not into the bathroom, onto the roof or out into the street yet: "+cell);
            h.assertFalse(in.contains(new BlockPos(2,LongerSite.UP,-9)),"not off the stairhead into its well");
            open(l,b,LongerSite.BATH_DOOR,true);open(l,b,LongerSite.ROOF_DOOR,true);
            var all=walked(l,b,new BlockPos(0,0,-1));
            for(var cell:List.of(new BlockPos(-4,LongerSite.UP,-9),new BlockPos(-5,LongerSite.ROOF,-11),new BlockPos(3,LongerSite.ROOF,-14),new BlockPos(4,LongerSite.ROOF,-10)))
                h.assertTrue(all.contains(cell),"the bathroom, up through the stair house, out on the roof to the bench: "+cell+" of "+all.size());
            // The parapet is too high to step over, all round.
            for(int x=-6;x<=6;x++){h.assertFalse(all.contains(new BlockPos(x,LongerSite.ROOF+2,-19))||all.contains(new BlockPos(x,LongerSite.ROOF+3,-19)),"nobody steps up onto the parapet at "+x);}
            h.assertFalse(all.stream().anyMatch(c->c.getZ()<=-20||c.getY()>=LongerSite.ROOF+3),"nobody reaches the street or the tops of the walls: "+all.stream().filter(c->c.getZ()<=-20||c.getY()>=LongerSite.ROOF+3).limit(4).toList());
            // A hand on what answers.
            told(p);
            f.click(p,P,LongerSite.FRONT_DOOR);f.click(p,P,LongerSite.CHAIR);f.click(p,P,LongerSite.KETTLE);f.click(p,P,LongerSite.CUPS);f.click(p,P,LongerSite.HOOK);f.click(p,P,LongerSite.RADIO);
        }).thenIdle(2).thenExecute(()->{
            var why=told(p);
            h.assertTrue(why.contains(LongerTexts.FRONT_DOOR)&&why.contains(LongerTexts.CHAIR)&&why.contains(LongerTexts.KETTLE)&&why.contains(LongerTexts.CUPS)&&why.contains(LongerTexts.HOOK)&&why.contains(LongerTexts.BENCH_RADIO),"each says what it is: "+why);
            h.assertFalse(l.getBlockState(b.offset(LongerSite.FRONT_DOOR)).getValue(DoorBlock.OPEN),"the shop door onto the street stays shut");
            // The lamp goes off and on, the flat's radio on and off, the toaster down and up.
            int lamp=LongerBlock.stage(l.getBlockState(b.offset(LongerSite.LAMP)));
            f.click(p,P,LongerSite.LAMP);h.assertTrue(LongerBlock.stage(l.getBlockState(b.offset(LongerSite.LAMP)))!=lamp,"the lamp switches");
            f.click(p,P,LongerSite.LAMP);h.assertTrue(LongerBlock.stage(l.getBlockState(b.offset(LongerSite.LAMP)))==lamp,"and back");
            f.click(p,P,LongerSite.FLAT_RADIO);h.assertTrue(LongerBlock.stage(l.getBlockState(b.offset(LongerSite.FLAT_RADIO)))==1,"the kitchen radio comes on");
            f.click(p,P,LongerSite.FLAT_RADIO);h.assertTrue(LongerBlock.stage(l.getBlockState(b.offset(LongerSite.FLAT_RADIO)))==0,"and off");
            f.click(p,P,LongerSite.TOASTER);h.assertTrue(LongerBlock.stage(l.getBlockState(b.offset(LongerSite.TOASTER)))==1,"the toaster's lever goes down");
            f.click(p,P,LongerSite.TOASTER);h.assertTrue(LongerBlock.stage(l.getBlockState(b.offset(LongerSite.TOASTER)))==0,"and comes back up");
            h.assertTrue(LongerBlock.light(l.getBlockState(b.offset(LongerSite.LAMP)))>0,"the lamp, lit, gives light");
            // The first paper, and the House's dealing.
            h.assertTrue(fitting(l,b,LongerSite.SOURCE,LongerBlock.Kind.ORDER)&&fitting(l,b,LongerSite.ENDING,LongerBlock.Kind.TABLE),"the work order on its spike, and the table where the account will wait");
            var paper=LiteraryTexts.source(P).get(DataComponents.WRITTEN_BOOK_CONTENT);String text=String.join(" ",paper.pages().stream().map(pg->pg.raw().getString()).toList());
            h.assertTrue(text.contains("WORK ORDER")&&text.contains("One screw short")&&paper.author().equals("R."),"the work order, in Rowan's pencil");
            for(String word:List.of(" he "," she "," his "," her "," him "))h.assertFalse(text.toLowerCase(java.util.Locale.ROOT).contains(word),"the order says nothing of what anybody is: "+word);
            h.assertFalse(LiteraryVignettes.canDeal(f.data(),p.getUUID(),P),"the House does not deal it yet");
            done();h.succeed();
        });
    }

    /** Rowan at this reader's copy, once made. */
    private static LiteraryActor rowan(GameTestHelper h,ServerPlayer p){var b=LiteraryVignettes.base(p,P);return b==null?null:LongerRowan.find(h.getLevel().getServer().getLevel(NovelRooms.dimension(P)),b);}
    private static void settleRead(ServerPlayer p,String value){HouseRead.signal(p,HouseRead.Trait.DRAWN_TO,value,false);HouseRead.signal(p,HouseRead.Trait.DRAWN_TO,value,true);}

    /**
     * 0.4.622: Rowan in the shop. A reader the House has settled as drawn to men meets Rowan as the man; the cast is kept in their own
     * record and does not change when the read does. Rowan is made once, sat in the mended chair facing the bench, hands at the radio,
     * holding their own body and gestures; humming carries to a reader still in the House's corridor. A hand on Rowan opens the panel
     * with where they have got to, the first time and after, and their name over it; nothing goes to chat.
     */
    @GameTest(template="empty",batch="longer_rowan",timeoutTicks=1200)
    public static void rowanSitsAtTheBenchHummingAndKeepsTheCastTheyWereMetIn(GameTestHelper h){
        var f=shop(h);var p=NativeTestPlayers.survival(h,"longer_rowan_reader");f.players.add(p);settleRead(p,"MEN");f.arrive(p,P);var l=f.level(P);
        h.startSequence().thenWaitUntil(()->h.assertTrue(rowan(h,p)!=null,"Rowan is made in the shop"))
        .thenExecute(()->{
            var b=LiteraryVignettes.base(p,P);var a=rowan(h,p);
            h.assertTrue(a.phase()==LongerRowan.FIRST+LongerRowan.MAN&&f.own(p,P).getInt(LongerRowan.CAST)==LongerRowan.FIRST+LongerRowan.MAN,"a reader drawn to men meets Rowan as the man, and keeps it: "+a.phase());
            h.assertTrue(a.blockPosition().equals(b.offset(LongerSite.CHAIR))&&a.gesture()==LongerRowan.WORK&&a.holdsBody()&&a.ownGestures(),"in the mended chair, at work: "+a.blockPosition().subtract(b));
            var seat=ActorSeat.of(a);h.assertTrue(seat!=null&&seat.facing()==Direction.WEST&&seat.seatHeight()==LongerBlock.Kind.CHAIR.seatHeight(),"sat, facing the bench");
            h.assertTrue(l.getEntitiesOfClass(LiteraryActor.class,LongerRowan.area(b),LongerRowan::is).size()==1,"one Rowan, and only one");
            // The read changes: the Rowan they met does not.
            settleRead(p,"WOMEN");f.arrive(p,P);
            h.assertTrue(f.own(p,P).getInt(LongerRowan.CAST)==LongerRowan.FIRST+LongerRowan.MAN,"the cast is fixed at the first meeting");
            NativeTestPlayers.flush(p);NativeTestPlayers.packets(p);
        }).thenIdle(340).thenExecute(()->{
            var b=LiteraryVignettes.base(p,P);
            h.assertTrue(l.getEntitiesOfClass(LiteraryActor.class,LongerRowan.area(b),LongerRowan::is).size()==1,"arriving again makes nobody twice");
            NativeTestPlayers.flush(p);boolean hum=false,tune=false;
            for(var packet:NativeTestPlayers.packets(p))if(packet instanceof net.minecraft.network.protocol.game.ClientboundSoundPacket s){hum|=s.getSound().value()==LongerRegistry.HUM.get();tune|=s.getSound().value()==LongerRegistry.TUNING.get();}
            h.assertTrue(hum&&tune,"from the corridor a reader hears somebody humming and a radio being tuned: hum "+hum+", tuning "+tune);
            // A hand on Rowan: where they have got to, in the panel, over their head; nothing in chat.
            var a=rowan(h,p);f.at(p,P,-3.5,0,-11.5);told(p);
            LiteraryVignettes.talk(p,a);
            h.assertTrue(Talk.lines(p).contains(LongerTexts.ROWAN+": "+LongerTexts.ROWAN_BENCH_FIRST),"the first time: "+Talk.lines(p));
            h.assertTrue(f.own(p,P).getBoolean(LongerRowan.MET),"the reader has met Rowan");
            Talk.close(p);
        }).thenIdle(60).thenExecute(()->{
            var a=rowan(h,p);LiteraryVignettes.talk(p,a);
            h.assertTrue(Talk.lines(p).contains(LongerTexts.ROWAN+": "+LongerTexts.ROWAN_BENCH),"after: "+Talk.lines(p));
            h.assertTrue(told(p).stream().noneMatch(t->t.contains(LongerTexts.ROWAN_BENCH)||t.contains(LongerTexts.ROWAN_BENCH_FIRST)),"nothing Rowan says goes to chat");
            Talk.close(p);done();h.succeed();
        });
    }

    /**
     * 0.4.622: Rowan's errand, walked on their own feet: out of the chair, along the aisle to the shelf of waiting sets (a hand up at a
     * tag), on to the counter (leaning on it, looking at the door), and back to the chair. Every way is clear of everything in the shop;
     * no step is longer than a walking pace; they face the way they walk. A hand on them at the shelf gets the shelf's line and keeps
     * them there a while.
     */
    @GameTest(template="empty",batch="longer_rowan_walk",timeoutTicks=2400)
    public static void rowanWalksTheirErrandOnTheirOwnFeetAndComesBackToTheChair(GameTestHelper h){
        var f=shop(h);var p=f.player("longer_rowan_walker",P);var l=f.level(P);
        var last=new net.minecraft.world.phys.Vec3[1];var worst=new double[1];var stops=new java.util.ArrayList<Integer>();
        h.startSequence().thenWaitUntil(()->h.assertTrue(rowan(h,p)!=null,"Rowan is made"))
        .thenExecute(()->{
            var b=LiteraryVignettes.base(p,P);
            // Every way is clear for a body, but for the chair they stand up out of.
            var legs=new java.util.ArrayList<net.minecraft.world.phys.Vec3>();legs.add(LongerRowan.BENCH.at(b));
            for(var v:LongerRowan.TO_SHELF)legs.add(v.add(b.getX(),b.getY(),b.getZ()));legs.add(LongerRowan.SHELF_SPOT.at(b));
            for(var v:LongerRowan.TO_COUNTER)legs.add(v.add(b.getX(),b.getY(),b.getZ()));legs.add(LongerRowan.COUNTER_SPOT.at(b));
            for(var v:LongerRowan.TO_BENCH)legs.add(v.add(b.getX(),b.getY(),b.getZ()));legs.add(LongerRowan.BENCH.at(b));
            var chair=b.offset(LongerSite.CHAIR);
            for(int i=0;i+1<legs.size();i++){var from=legs.get(i);var to=legs.get(i+1);int n=(int)Math.ceil(from.distanceTo(to)/.1);
                for(int k=0;k<=n;k++){var at=from.lerp(to,k/(double)Math.max(1,n));if(Math.hypot(at.x-chair.getX()-.5,at.z-chair.getZ()-.5)<.75)continue;
                    var body=new AABB(at.x-.3,at.y+.01,at.z-.3,at.x+.3,at.y+1.8,at.z+.3);h.assertTrue(l.noCollision(body),"Rowan's way is clear at "+at.subtract(b.getX(),b.getY(),b.getZ()));}}
            last[0]=rowan(h,p).position();LongerRowan.sendOnErrand(b);
        })
        .thenWaitUntil(()->{
            var b=LiteraryVignettes.base(p,P);var a=rowan(h,p);var at=a.position();if(last[0]==null)last[0]=at;worst[0]=Math.max(worst[0],at.distanceTo(last[0]));last[0]=at;
            if(LongerRowan.holding(b)>0&&(stops.isEmpty()||stops.get(stops.size()-1)!=LongerRowan.leg(b)))stops.add(LongerRowan.leg(b));
            h.assertTrue(stops.contains(0),"at the shelf yet: "+at.subtract(b.getX(),b.getY(),b.getZ()));
        }).thenExecute(()->{
            var b=LiteraryVignettes.base(p,P);var a=rowan(h,p);
            h.assertTrue(a.position().distanceTo(LongerRowan.SHELF_SPOT.at(b))<.05&&a.gesture()==LongerRowan.SHELF,"at the shelf, a hand up at a tag: "+a.gesture());
            h.assertTrue(ActorSeat.of(a)==null,"standing, out of the chair");
            f.at(p,P,3.5,0,-12.5);LiteraryVignettes.talk(p,a);
            h.assertTrue(Talk.lines(p).contains(LongerTexts.ROWAN+": "+LongerTexts.ROWAN_SHELF),"what they say at the shelf: "+Talk.lines(p));
            Talk.close(p);
        }).thenIdle(160).thenExecute(()->{
            var b=LiteraryVignettes.base(p,P);
            h.assertTrue(LongerRowan.leg(b)==0&&rowan(h,p).position().distanceTo(LongerRowan.SHELF_SPOT.at(b))<.05,"spoken to, they stay at the shelf a while longer");
            last[0]=rowan(h,p).position();
        })
        .thenWaitUntil(()->{
            var b=LiteraryVignettes.base(p,P);var a=rowan(h,p);var at=a.position();if(last[0]==null)last[0]=at;worst[0]=Math.max(worst[0],at.distanceTo(last[0]));last[0]=at;
            if(LongerRowan.holding(b)>0&&(stops.isEmpty()||stops.get(stops.size()-1)!=LongerRowan.leg(b)))stops.add(LongerRowan.leg(b));
            // Walking, they face the way they go (within their turning).
            h.assertTrue(stops.contains(1),"at the counter yet: "+at.subtract(b.getX(),b.getY(),b.getZ()));
        }).thenExecute(()->{
            var b=LiteraryVignettes.base(p,P);var a=rowan(h,p);
            h.assertTrue(a.position().distanceTo(LongerRowan.COUNTER_SPOT.at(b))<.05&&a.gesture()==LongerRowan.COUNTER,"behind the counter, leaning on it");
        })
        .thenWaitUntil(()->{
            var b=LiteraryVignettes.base(p,P);var a=rowan(h,p);var at=a.position();if(last[0]==null)last[0]=at;worst[0]=Math.max(worst[0],at.distanceTo(last[0]));last[0]=at;
            h.assertTrue(LongerRowan.leg(b)<0,"back in the chair yet: "+at.subtract(b.getX(),b.getY(),b.getZ()));
        }).thenIdle(20).thenExecute(()->{
            var b=LiteraryVignettes.base(p,P);var a=rowan(h,p);
            h.assertTrue(a.blockPosition().equals(b.offset(LongerSite.CHAIR))&&a.gesture()==LongerRowan.WORK&&ActorSeat.of(a)!=null,"back in the chair at work");
            h.assertTrue(Math.abs(net.minecraft.util.Mth.wrapDegrees(a.getYRot()-LongerRowan.BENCH.look()))<1,"turned back to the bench: "+a.getYRot());
            h.assertTrue(worst[0]<=LongerRowan.PACE+.01,"no step longer than a walking pace: "+worst[0]);
            done();h.succeed();
        });
    }
}
