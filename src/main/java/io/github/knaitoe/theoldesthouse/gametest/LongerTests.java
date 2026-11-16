package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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
            h.assertTrue(Talk.lines(p).contains(LongerTexts.ROWAN+": "+LongerTexts.ASK_HOLD)&&Talk.replies(p).contains(LongerTexts.HOLD_IT),"the first hand at the bench: the workbench chapter's ask: "+Talk.lines(p)+" "+Talk.replies(p));
            h.assertTrue(f.own(p,P).getBoolean(LongerRowan.MET),"the reader has met Rowan");
            Talk.close(p);
        }).thenIdle(60).thenExecute(()->{
            var a=rowan(h,p);LiteraryVignettes.talk(p,a);
            h.assertTrue(Talk.lines(p).contains(LongerTexts.ROWAN+": "+LongerTexts.ASK_HOLD)&&Talk.replies(p).contains(LongerTexts.HOLD_IT),"a hand again: the same question, still waiting: "+Talk.lines(p));
            h.assertTrue(told(p).stream().noneMatch(t->t.contains(LongerTexts.ASK_HOLD)),"nothing Rowan says goes to chat");
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

    // ------------------------------------------------------------------------------------------------ 0.4.623, chapter 1, the workbench
    private static CompoundTag bench(LiteraryTests.Fixture f,ServerPlayer p){return f.own(p,P);}
    private static int stage(LiteraryTests.Fixture f,ServerPlayer p){return LongerBench.stage(f.own(p,P));}
    private static boolean heard(ServerPlayer p,net.minecraft.sounds.SoundEvent sound){NativeTestPlayers.flush(p);for(var packet:NativeTestPlayers.packets(p))if(packet instanceof net.minecraft.network.protocol.game.ClientboundSoundPacket s&&s.getSound().value()==sound)return true;return false;}

    /**
     * A reader near the chair is asked to hold the panel, holds it (within reach, looking at the radio), and Rowan thanks them. The set
     * is switched on, bursts, and the panel opens on its own: "That sounded expensive." One question; the route on its paper in Rowan's
     * hand; the stairs, word for word; the crossed-out turn; the tool put down. The one act kept is the panel held. A hand on Rowan after that is the next chapter's.
     */
    @GameTest(template="empty",batch="longer_bench_hold",timeoutTicks=2400)
    public static void theReaderHoldsThePanelTheSetBurstsAndRowanShowsTheirRoute(GameTestHelper h){
        var f=shop(h);var p=f.player("longer_bench_holder",P);var l=f.level(P);
        h.startSequence().thenWaitUntil(()->h.assertTrue(rowan(h,p)!=null,"Rowan is made"))
        .thenExecute(()->{f.at(p,P,-3.6,0,-12.6);told(p);NativeTestPlayers.flush(p);NativeTestPlayers.packets(p);})
        .thenWaitUntil(()->h.assertTrue(stage(f,p)==LongerBench.ASK,"near the chair a while, the reader is asked: "+stage(f,p)))
        .thenExecute(()->{
            h.assertTrue(Talk.lines(p).contains(LongerTexts.ROWAN+": "+LongerTexts.ASK_HOLD),"\"Could you hold that side a second?\" "+Talk.lines(p));
            h.assertTrue(Talk.replies(p).equals(List.of(LongerTexts.HOLD_IT,LongerTexts.WATCH,LongerTexts.NOT_NOW)),"hold it, watch, or not now: "+Talk.replies(p));
            h.assertTrue(Talk.choose(p,LongerTexts.HOLD_IT),"hold it");
            h.assertTrue(stage(f,p)==LongerBench.HOLDING&&Talk.lines(p).contains(LongerTexts.ROWAN+": "+LongerTexts.HOLD_WHERE),"where to hold it: "+Talk.lines(p));
            Talk.close(p);
            // Within reach of the radio, looking at it.
            var b=LiteraryVignettes.base(p,P);f.at(p,P,-4.1,0,-11.8);
            p.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,net.minecraft.world.phys.Vec3.atCenterOf(b.offset(LongerSite.RADIO)));
        })
        .thenWaitUntil(()->h.assertTrue(stage(f,p)>=LongerBench.FINISHING,"held long enough: "+stage(f,p)))
        .thenExecute(()->{
            var a=rowan(h,p);
            h.assertTrue(LongerBench.act(bench(f,p)).equals(LongerBench.HELD_PANEL),"the one act kept is the panel held: "+LongerBench.act(bench(f,p)));
            h.assertTrue(a.getCustomName()!=null&&a.getCustomName().getString().equals(LongerTexts.THANKS),"\"There. Thank you.\"");
            h.assertTrue(LongerBench.screwOnFloor(l,LiteraryVignettes.base(p,P))==null,"held, no screw rolls");
        })
        .thenWaitUntil(()->h.assertTrue(stage(f,p)==LongerBench.TALK,"the burst, then the panel: "+stage(f,p)))
        .thenExecute(()->{
            var b=LiteraryVignettes.base(p,P);var a=rowan(h,p);
            h.assertTrue(heard(p,LongerRegistry.BURST.get()),"the set bursts");
            h.assertTrue(LongerBlock.stage(l.getBlockState(b.offset(LongerSite.RADIO)))==0,"the set is off, its back on");
            h.assertTrue(Talk.lines(p).contains(LongerTexts.ROWAN+": "+LongerTexts.EXPENSIVE)&&Talk.lines(p).contains(LongerTexts.ROWAN+": "+LongerTexts.STANDING),"\"That sounded expensive.\" "+Talk.lines(p));
            h.assertTrue(Talk.replies(p).equals(LongerTexts.QUESTIONS),"the four ways to answer: "+Talk.replies(p));
            h.assertTrue(Talk.choose(p,LongerTexts.QUESTIONS.get(1)),"what's wrong with it");
            h.assertTrue(Talk.lines(p).contains(LongerTexts.ROWAN+": "+LongerTexts.ANSWERS.get(1)),"the answer: "+Talk.lines(p));
            h.assertTrue(Talk.lines(p).contains(LongerTexts.ON_PAPER+": "+LongerTexts.ROUTE)&&a.getMainHandItem().is(net.minecraft.world.item.Items.PAPER)&&a.gesture()==LongerBench.SHOW,"the route on its paper, held out: "+a.gesture());
            h.assertTrue(!Talk.replies(p).contains(LongerTexts.QUESTIONS.get(1))&&Talk.replies(p).contains(LongerTexts.DOUBT)&&Talk.replies(p).contains(LongerTexts.LEAVE),"each question once; the stairs; leave it: "+Talk.replies(p));
            h.assertTrue(Talk.choose(p,LongerTexts.DOUBT)&&Talk.lines(p).contains(LongerTexts.ROWAN+": "+LongerTexts.STAIRS),"\"I said I knew where the stairs were.\"");
            h.assertTrue(Talk.choose(p,LongerTexts.THREE)&&Talk.lines(p).contains("you: "+LongerTexts.THREE)&&Talk.lines(p).contains(LongerTexts.ROWAN+": "+LongerTexts.RIGHT),"\"You drew them in three places.\" \"And one of those was right.\" "+Talk.lines(p));
            h.assertTrue(Talk.choose(p,LongerTexts.WHICH)&&Talk.lines(p).contains(LongerTexts.ROWAN+": "+LongerTexts.WHICH_ANSWER),"the crossed-out turn");
            h.assertTrue(Talk.choose(p,LongerTexts.LEAVE),"leave it there");
            h.assertTrue(stage(f,p)==LongerBench.DONE&&a.getMainHandItem().isEmpty()&&a.gesture()==LongerBench.REST,"the paper put away, the tool down: "+a.gesture());
            h.assertTrue(told(p).stream().noneMatch(t->t.contains(LongerTexts.EXPENSIVE)||t.contains(LongerTexts.ROUTE)),"nothing in chat");
            Talk.close(p);
        }).thenIdle(30).thenExecute(()->{
            var b=LiteraryVignettes.base(p,P);var a=rowan(h,p);
            h.assertTrue(a.gesture()==LongerBench.REST&&LongerBench.rested(l,b),"resting in the chair");
            LiteraryVignettes.talk(p,a);
            // 0.4.624: after the chapter, the next one: the kettle (LongerKettle).
            h.assertTrue(Talk.lines(p).contains(LongerTexts.ROWAN+": "+LongerTexts.MAKING_TEA),"after the chapter, the tea: "+Talk.lines(p));
            Talk.close(p);done();h.succeed();
        });
    }

    /**
     * Watched, not held: the last screw goes off the bench and rolls across the floor, a real screw. A hand on the lamp while Rowan works
     * turns it onto the radio, and that is the act kept: the first thing done, not the last. The reader picks the screw up and hands it
     * back; Rowan says so and finishes. A screw taken out of the shop does not leave with the reader.
     */
    @GameTest(template="empty",batch="longer_bench_screw",timeoutTicks=1600)
    public static void theScrewRollsTheLampIsTurnedAndTheScrewIsHandedBack(GameTestHelper h){
        var f=shop(h);var p=f.player("longer_bench_screw",P);var l=f.level(P);
        h.startSequence().thenWaitUntil(()->h.assertTrue(rowan(h,p)!=null,"Rowan is made"))
        .thenExecute(()->{
            f.at(p,P,-3.6,0,-12.6);LiteraryVignettes.talk(p,rowan(h,p));
            h.assertTrue(stage(f,p)==LongerBench.ASK,"a hand on Rowan at the bench asks");
            h.assertTrue(Talk.choose(p,LongerTexts.WATCH),"watch");Talk.close(p);
            var b=LiteraryVignettes.base(p,P);
            h.assertTrue(stage(f,p)==LongerBench.ALONE&&LongerBench.screwOnFloor(l,b)!=null,"the screw goes off the bench");
            int lamp=LongerBlock.stage(l.getBlockState(b.offset(LongerSite.LAMP)));
            f.click(p,P,LongerSite.LAMP);
            h.assertTrue(LongerBlock.stage(l.getBlockState(b.offset(LongerSite.LAMP)))==2&&lamp!=2,"the lamp turned onto the radio");
            h.assertTrue(LongerBench.act(bench(f,p)).equals(LongerBench.MOVED_LAMP),"the lamp moved is kept: "+LongerBench.act(bench(f,p)));
            h.assertTrue(l.getBrightness(net.minecraft.world.level.LightLayer.BLOCK,b.offset(LongerSite.LAMP))>=11,"still lit");
        })
        .thenWaitUntil(()->{var e=LongerBench.screwOnFloor(l,LiteraryVignettes.base(p,P));h.assertTrue(e!=null&&e.onGround()&&e.getDeltaMovement().horizontalDistanceSqr()<1e-4,"the screw comes to rest on the floor");})
        .thenExecute(()->{
            var b=LiteraryVignettes.base(p,P);var e=LongerBench.screwOnFloor(l,b);var at=e.position().subtract(b.getX(),b.getY(),b.getZ());
            h.assertTrue(at.y<.2&&at.x>-5&&LongerSite.inShop(at),"on the shop floor, off the bench: "+at);
            h.assertTrue(LongerBench.isScrew(e.getItem())&&e.getItem().getHoverName().getString().equals(LongerTexts.SCREW),"a small screw");
            // Picked up, as a hand does.
            p.getInventory().add(e.getItem().copy());e.discard();
            LiteraryVignettes.talk(p,rowan(h,p));
            h.assertTrue(Talk.lines(p).contains(LongerTexts.ROWAN+": "+LongerTexts.SCREW_BACK),"handed back: "+Talk.lines(p));
            boolean kept=false;for(var s:p.getInventory().items)kept|=LongerBench.isScrew(s);
            h.assertTrue(!kept,"the screw goes back to Rowan");
            h.assertTrue(LongerBench.act(bench(f,p)).equals(LongerBench.MOVED_LAMP),"the first act stays the one kept, not the last");
            h.assertTrue(stage(f,p)==LongerBench.FINISHING,"Rowan finishes");
            Talk.close(p);
            // Another screw in a pocket outside the shop goes.
            p.getInventory().add(LongerBench.screw());
        })
        .thenWaitUntil(()->h.assertTrue(stage(f,p)==LongerBench.TALK,"the burst and the panel"))
        .thenExecute(()->{f.at(p,P,0,0,-40);})
        .thenWaitUntil(()->{boolean kept=false;for(var s:p.getInventory().items)kept|=LongerBench.isScrew(s);h.assertTrue(!kept,"out of the shop, the shop's screw is not in the reader's pocket");})
        .thenExecute(()->{Talk.close(p);done();h.succeed();});
    }

    /**
     * Not now: Rowan does it one-handed, the screw rolls, and nobody picks it up. In time Rowan gets out of the chair, walks to it, stoops
     * for it and walks back; nothing they walk through is solid. No act is kept that the reader did not do.
     */
    @GameTest(template="empty",batch="longer_bench_fetch",timeoutTicks=2000)
    public static void leftOnTheFloorTheScrewIsFetchedByRowanOnTheirOwnFeet(GameTestHelper h){
        var f=shop(h);var p=f.player("longer_bench_fetch",P);var l=f.level(P);var stooped=new boolean[1];
        h.startSequence().thenWaitUntil(()->h.assertTrue(rowan(h,p)!=null,"Rowan is made"))
        .thenExecute(()->{
            f.at(p,P,-0.5,0,-13.5);LiteraryVignettes.talk(p,rowan(h,p));
            h.assertTrue(Talk.choose(p,LongerTexts.NOT_NOW),"not right now");
            h.assertTrue(Talk.lines(p).contains(LongerTexts.ROWAN+": "+LongerTexts.ONE_HANDED),"\"I've done it one-handed before.\"");
            Talk.close(p);
        })
        .thenWaitUntil(()->{var e=LongerBench.screwOnFloor(l,LiteraryVignettes.base(p,P));h.assertTrue(e!=null&&e.onGround()&&e.getDeltaMovement().horizontalDistanceSqr()<1e-4,"at rest on the floor");})
        .thenExecute(()->LongerBench.screwWaited(LiteraryVignettes.base(p,P),LongerBench.FETCH_AFTER))
        .thenWaitUntil(()->{
            var b=LiteraryVignettes.base(p,P);var a=rowan(h,p);
            if(LongerBench.fetching(b)>=0){var body=a.getBoundingBox().deflate(.05);if(LongerBench.fetching(b)!=1&&a.position().distanceTo(LongerRowan.BENCH.at(b))>.8)h.assertTrue(l.noCollision(body),"Rowan walks through nothing solid: "+a.position().subtract(b.getX(),b.getY(),b.getZ()));}
            if(LongerBench.fetching(b)==1&&a.gesture()==LongerBench.STOOP)stooped[0]=true;
            h.assertTrue(stage(f,p)==LongerBench.FINISHING,"fetched and back: fetch "+LongerBench.fetching(b)+" at "+a.position().subtract(b.getX(),b.getY(),b.getZ()));
        })
        .thenExecute(()->{
            var b=LiteraryVignettes.base(p,P);var a=rowan(h,p);
            h.assertTrue(stooped[0],"they stooped for it");
            h.assertTrue(LongerBench.screwOnFloor(l,b)==null,"the screw is off the floor");
            h.assertTrue(a.position().distanceTo(LongerRowan.BENCH.at(b))<.05,"back in the chair");
            h.assertTrue(LongerBench.act(bench(f,p)).isEmpty(),"nothing the reader did not do: "+LongerBench.act(bench(f,p)));
            h.assertTrue(a.getCustomName()!=null&&a.getCustomName().getString().equals(LongerTexts.THERE),"\"There.\" and no thanks for nothing");
            done();h.succeed();
        });
    }

    // ------------------------------------------------------------------------------------------------ 0.4.624, chapter 2, the kettle
    private static int kettle(LiteraryTests.Fixture f,ServerPlayer p){return LongerKettle.stage(f.own(p,P));}
    /** The bench chapter as finished, for a case that starts at the kettle. */
    private static void benchDone(LiteraryTests.Fixture f,ServerPlayer p){var own=f.own(p,P);own.putInt(LongerBench.STAGE,LongerBench.DONE);LiteraryVignettes.save(f.data(),p.getUUID(),P,own);}
    private static boolean holds(ServerPlayer p,net.minecraft.world.item.Item item){for(var s:p.getInventory().items)if(s.is(item))return true;return false;}
    /** Rowan, walking the chapter's ways: never inside anything solid, except where they sit. */
    private static void clearWalk(GameTestHelper h,ServerLevel l,BlockPos b,LiteraryActor a){
        var r=a.position().subtract(b.getX(),b.getY(),b.getZ());
        if(LongerRowan.cast(a.phase())&&io.github.knaitoe.theoldesthouse.labyrinth.ActorSeat.of(a)==null&&a.position().distanceTo(LongerRowan.BENCH.at(b))>.8)
            h.assertTrue(l.noCollision(a.getBoundingBox().deflate(.12)),"Rowan walks through nothing solid: "+r);
    }
    /** Through the offer and up the stair to the kettle, and the kettle boiled: the cups are poured. */
    private static void toPoured(GameTestHelper h,LiteraryTests.Fixture f,ServerPlayer p,ServerLevel l,java.util.function.Consumer<net.minecraft.gametest.framework.GameTestSequence> then){
        var seq=h.startSequence().thenWaitUntil(()->h.assertTrue(rowan(h,p)!=null,"Rowan is made"))
        .thenExecute(()->{benchDone(f,p);f.at(p,P,-3.6,0,-12.6);LiteraryVignettes.talk(p,rowan(h,p));
            h.assertTrue(Talk.choose(p,LongerTexts.SAY_NOTHING),"say nothing");LongerKettle.waited(LiteraryVignettes.base(p,P),LongerKettle.QUIET);})
        .thenWaitUntil(()->h.assertTrue(kettle(f,p)==LongerKettle.STAY_ASK,"the pause, then the offer: "+kettle(f,p)))
        .thenExecute(()->{h.assertTrue(Talk.choose(p,LongerTexts.YES),"I'd like that");Talk.close(p);})
        .thenWaitUntil(()->{var b=LiteraryVignettes.base(p,P);clearWalk(h,l,b,rowan(h,p));h.assertTrue(kettle(f,p)>=LongerKettle.BOIL,"up the stair to the kettle: "+rowan(h,p).position().subtract(b.getX(),b.getY(),b.getZ()));})
        .thenExecute(()->LongerKettle.boiled(LiteraryVignettes.base(p,P),60))
        .thenWaitUntil(()->{var b=LiteraryVignettes.base(p,P);clearWalk(h,l,b,rowan(h,p));h.assertTrue(kettle(f,p)>=LongerKettle.POURED,"boiled and poured: leg "+LongerKettle.leg(b));});
        then.accept(seq);
    }

    /**
     * The tool down, a hand on Rowan: "I'm making tea." The reader asks whether that is part of the repair: "No." A pause with nothing to
     * say into it; then "You could stay for it." Going on is let be; the offer comes once more on a later hand ("Kettle's still there.").
     * One cup: Rowan walks up the back stair on their own feet, through nothing solid, the kettle boils (heard) and clicks off, the cups
     * are fetched from the table and poured (heard), and the whole one is left there. Rowan sits in the good chair, seated. A hand on the
     * cups is a real cup of tea; the first hand on Rowan in the good chair: "This is the good chair. I know. I'm not getting up."
     */
    @GameTest(template="empty",batch="longer_kettle_offer",timeoutTicks=3600)
    public static void rowanMakesTeaTheReaderStaysForItAndRowanKeepsTheGoodChair(GameTestHelper h){
        var f=shop(h);var p=f.player("longer_kettle_offer",P);var l=f.level(P);
        h.startSequence().thenWaitUntil(()->h.assertTrue(rowan(h,p)!=null,"Rowan is made"))
        .thenExecute(()->{
            benchDone(f,p);f.at(p,P,-3.6,0,-12.6);LiteraryVignettes.talk(p,rowan(h,p));
            h.assertTrue(kettle(f,p)==LongerKettle.OFFER&&Talk.lines(p).contains(LongerTexts.ROWAN+": "+LongerTexts.MAKING_TEA),"\"I'm making tea.\" "+Talk.lines(p));
            h.assertTrue(Talk.replies(p).equals(List.of(LongerTexts.PART,LongerTexts.SAY_NOTHING)),"ask, or say nothing: "+Talk.replies(p));
            h.assertTrue(Talk.choose(p,LongerTexts.PART)&&Talk.lines(p).contains("you: "+LongerTexts.PART)&&Talk.lines(p).contains(LongerTexts.ROWAN+": "+LongerTexts.NO),"\"No.\"");
            h.assertTrue(kettle(f,p)==LongerKettle.PAUSE&&Talk.replies(p).isEmpty(),"a pause, nothing to say into it");
            LongerKettle.waited(LiteraryVignettes.base(p,P),LongerKettle.QUIET);
        })
        .thenWaitUntil(()->h.assertTrue(kettle(f,p)==LongerKettle.STAY_ASK,"then the offer: "+kettle(f,p)))
        .thenExecute(()->{
            h.assertTrue(Talk.lines(p).contains(LongerTexts.ROWAN+": "+LongerTexts.STAY_FOR),"\"You could stay for it.\" "+Talk.lines(p));
            h.assertTrue(Talk.replies(p).equals(List.of(LongerTexts.YES,LongerTexts.ONE_CUP,LongerTexts.KEEP_GOING)),"stay, one cup, or go on: "+Talk.replies(p));
            h.assertTrue(Talk.choose(p,LongerTexts.KEEP_GOING)&&Talk.lines(p).contains(LongerTexts.ROWAN+": "+LongerTexts.GO_ON),"let go");
            h.assertTrue(kettle(f,p)==LongerKettle.NONE&&f.own(p,P).getInt(LongerKettle.DECLINED)==1,"nothing more asked");
            Talk.close(p);
            LiteraryVignettes.talk(p,rowan(h,p));
            h.assertTrue(Talk.lines(p).contains(LongerTexts.ROWAN+": "+LongerTexts.STILL_THERE)&&Talk.lines(p).contains(LongerTexts.ROWAN+": "+LongerTexts.STAY_FOR),"once more: "+Talk.lines(p));
            h.assertTrue(Talk.choose(p,LongerTexts.ONE_CUP)&&kettle(f,p)==LongerKettle.UP&&f.own(p,P).getString(LongerKettle.STAY).equals("ONE_CUP"),"one cup");
            Talk.close(p);NativeTestPlayers.flush(p);NativeTestPlayers.packets(p);
            f.at(p,P,3.5,6,-14.5);
        })
        .thenWaitUntil(()->{var b=LiteraryVignettes.base(p,P);clearWalk(h,l,b,rowan(h,p));h.assertTrue(kettle(f,p)>=LongerKettle.BOIL,"up the stair: "+rowan(h,p).position().subtract(b.getX(),b.getY(),b.getZ()));})
        .thenExecute(()->{
            var b=LiteraryVignettes.base(p,P);var a=rowan(h,p);
            h.assertTrue(a.position().distanceTo(LongerKettle.KETTLE_SPOT.at(b))<.1,"at the kettle, upstairs");
            h.assertTrue(LongerBlock.stage(l.getBlockState(b.offset(LongerSite.KETTLE)))==1&&heard(p,LongerRegistry.KETTLE.get()),"the kettle on, heard");
        })
        .thenWaitUntil(()->{var b=LiteraryVignettes.base(p,P);clearWalk(h,l,b,rowan(h,p));h.assertTrue(LongerKettle.leg(b)>=3,"to the table for the cups: "+LongerKettle.leg(b));})
        .thenExecute(()->{var b=LiteraryVignettes.base(p,P);
            h.assertTrue(l.getBlockState(b.offset(LongerSite.CUPS)).isAir()&&rowan(h,p).getMainHandItem().is(LongerRegistry.TEA.get()),"the cups taken up from the table");
            LongerKettle.boiled(b,LongerKettle.BOILED);})
        .thenWaitUntil(()->{var b=LiteraryVignettes.base(p,P);clearWalk(h,l,b,rowan(h,p));h.assertTrue(kettle(f,p)>=LongerKettle.POURED,"poured and set down: leg "+LongerKettle.leg(b));})
        .thenExecute(()->{var b=LiteraryVignettes.base(p,P);
            h.assertTrue(heard(p,LongerRegistry.POUR.get()),"poured, heard");
            h.assertTrue(LongerBlock.stage(l.getBlockState(b.offset(LongerSite.CUPS)))==2,"the whole cup left at the table; the cracked one Rowan's");
            h.assertTrue(LongerBlock.stage(l.getBlockState(b.offset(LongerSite.KETTLE)))==0,"the kettle off");})
        .thenWaitUntil(()->{var b=LiteraryVignettes.base(p,P);clearWalk(h,l,b,rowan(h,p));h.assertTrue(kettle(f,p)>=LongerKettle.SEATED,"to the good chair: "+rowan(h,p).position().subtract(b.getX(),b.getY(),b.getZ()));})
        .thenExecute(()->{
            var b=LiteraryVignettes.base(p,P);var a=rowan(h,p);
            h.assertTrue(a.position().distanceTo(LongerKettle.ARMCHAIR_SPOT.at(b))<.05&&io.github.knaitoe.theoldesthouse.labyrinth.ActorSeat.of(a)!=null,"sat in the good chair");
            h.assertTrue(a.gesture()==LongerKettle.CUP_HELD&&a.getMainHandItem().is(LongerRegistry.TEA.get()),"with their cup");
            f.click(p,P,LongerSite.CUPS);
            h.assertTrue(holds(p,LongerRegistry.TEA.get())&&l.getBlockState(b.offset(LongerSite.CUPS)).isAir()&&f.own(p,P).getBoolean(LongerKettle.CUP),"a real cup of tea");
            f.at(p,P,2.5,6,-14.5);LiteraryVignettes.talk(p,a);
            h.assertTrue(Talk.lines(p).contains(LongerTexts.ROWAN+": "+LongerTexts.NOTICED_LINE),"\"This is the good chair.\" "+Talk.lines(p));
            h.assertTrue(told(p).stream().noneMatch(t->t.contains(LongerTexts.MAKING_TEA)||t.contains(LongerTexts.NOTICED_LINE)),"nothing in chat");
            Talk.close(p);done();h.succeed();
        });
    }

    /**
     * The toast, standing: Rowan says the chair doesn't bite and asks for the toaster. Down; Rowan's word on the heat; it scorches,
     * pops (heard), and one slice is burnt. "I'll have the burnt one." "You don't have to be polite." "I'm being brave." "That's worse."
     * The burnt slice is the reader's, the good one Rowan's. Then three questions about now, each answered by Rowan too; then done, and
     * Rowan stays in the chair.
     */
    @GameTest(template="empty",batch="longer_kettle_toast",timeoutTicks=3600)
    public static void theToastBurnsTheReaderIsBraveAndRowanAsksAboutNow(GameTestHelper h){
        var f=shop(h);var p=f.player("longer_kettle_toast",P);var l=f.level(P);
        toPoured(h,f,p,l,seq->seq.thenExecute(()->f.at(p,P,3.5,6,-14.5))
        .thenWaitUntil(()->h.assertTrue(kettle(f,p)==LongerKettle.SEATED,"seated"))
        .thenWaitUntil(()->{var a=rowan(h,p);h.assertTrue(a.getCustomName()!=null&&a.getCustomName().getString().equals(LongerTexts.TOAST_ASK),"the remark, then the toaster: "+(a.getCustomName()==null?"":a.getCustomName().getString()));})
        .thenExecute(()->{
            f.click(p,P,LongerSite.TOASTER);var b=LiteraryVignettes.base(p,P);
            h.assertTrue(kettle(f,p)==LongerKettle.TOASTING&&LongerBlock.stage(l.getBlockState(b.offset(LongerSite.TOASTER)))==1,"the toaster down");
            NativeTestPlayers.flush(p);NativeTestPlayers.packets(p);
            LongerKettle.toasted(b,LongerKettle.HEAT_AT-1);
        })
        .thenWaitUntil(()->{var a=rowan(h,p);h.assertTrue(a.getCustomName()!=null&&a.getCustomName().getString().equals(LongerTexts.HEAT),"\"The heat's perfectly manageable.\"");})
        .thenExecute(()->LongerKettle.toasted(LiteraryVignettes.base(p,P),LongerKettle.POP-LongerKettle.HEAT_AT-45))
        .thenWaitUntil(()->h.assertTrue(kettle(f,p)==LongerKettle.TOAST_ASK,"up: "+kettle(f,p)))
        .thenExecute(()->{
            var b=LiteraryVignettes.base(p,P);var a=rowan(h,p);
            h.assertTrue(heard(p,LongerRegistry.TOASTER.get())&&LongerBlock.stage(l.getBlockState(b.offset(LongerSite.TOASTER)))==0,"the spring, heard; the lever up");
            h.assertTrue(Talk.replies(p).equals(List.of(LongerTexts.TEASE,LongerTexts.OFFER,LongerTexts.BRAVE_ASK)),"tease, give, or be brave: "+Talk.replies(p));
            h.assertTrue(Talk.choose(p,LongerTexts.BRAVE_ASK),"I'll have the burnt one");
            var lines=Talk.lines(p);
            int polite=lines.indexOf(LongerTexts.ROWAN+": "+LongerTexts.POLITE),brave=lines.indexOf("you: "+LongerTexts.BRAVE_LINE),worse=lines.indexOf(LongerTexts.ROWAN+": "+LongerTexts.WORSE);
            h.assertTrue(polite>=0&&brave>polite&&worse>brave,"polite, brave, worse, in order: "+lines);
            h.assertTrue(holds(p,LongerRegistry.BURNT_TOAST.get())&&!holds(p,LongerRegistry.TOAST.get())&&a.getOffhandItem().is(LongerRegistry.TOAST.get()),"the burnt slice the reader's, the good one Rowan's");
            h.assertTrue(f.own(p,P).getString(LongerKettle.TOAST).equals(LongerKettle.BRAVE),"kept: brave");
            for(int i=0;i<3;i++){
                h.assertTrue(Talk.replies(p).equals(LongerTexts.ANSWERS_NOW.get(i)),"question "+i+": "+Talk.replies(p));
                h.assertTrue(Talk.choose(p,LongerTexts.ANSWERS_NOW.get(i).get(1)),"answered");
            }
            lines=Talk.lines(p);
            for(var said:List.of(LongerTexts.Q_MUSIC,LongerTexts.A_MUSIC,LongerTexts.Q_PLAN,LongerTexts.A_PLAN,LongerTexts.Q_FIRST,LongerTexts.A_FIRST))h.assertTrue(lines.contains(LongerTexts.ROWAN+": "+said),"Rowan asks and answers: "+said);
            var own=f.own(p,P);
            h.assertTrue(own.getInt(LongerKettle.MUSIC)==1&&own.getInt(LongerKettle.PLAN)==1&&own.getInt(LongerKettle.FIRST)==1&&kettle(f,p)==LongerKettle.DONE,"the answers kept; done");
            Talk.close(p);
        })
        .thenIdle(20).thenExecute(()->{
            var b=LiteraryVignettes.base(p,P);var a=rowan(h,p);
            h.assertTrue(a.position().distanceTo(LongerKettle.ARMCHAIR_SPOT.at(b))<.05,"Rowan stays in the good chair");
            f.at(p,P,1.5,6,-15.2);LiteraryVignettes.talk(p,a);
            h.assertTrue(Talk.lines(p).stream().anyMatch(x->x.endsWith(LongerTexts.ROWAN_KETTLE_AFTER)||x.endsWith(LongerTexts.NOTICED_LINE)),"after the tea: "+Talk.lines(p));
            Talk.close(p);done();h.succeed();
        }));
    }

    /**
     * The good chair taken first: a reader sat in the armchair keeps it, and Rowan takes the kitchen chair and says that is fine. The
     * toaster popped by hand before it scorches: two good slices, "Show-off."
     */
    @GameTest(template="empty",batch="longer_kettle_chair",timeoutTicks=3600)
    public static void aReaderInTheGoodChairKeepsItAndRescuesTheToast(GameTestHelper h){
        var f=shop(h);var p=f.player("longer_kettle_chair",P);var l=f.level(P);
        toPoured(h,f,p,l,seq->seq.thenExecute(()->{
            var b=LiteraryVignettes.base(p,P);
            f.at(p,P,.5,6,-15.2);
            h.assertTrue(io.github.knaitoe.theoldesthouse.house.HouseSitting.sit(p,b.offset(LongerSite.ARMCHAIR))&&p.isPassenger(),"the reader sits in the good chair");
        })
        .thenWaitUntil(()->h.assertTrue(kettle(f,p)==LongerKettle.SEATED,"seated"))
        .thenExecute(()->{
            var b=LiteraryVignettes.base(p,P);var a=rowan(h,p);
            h.assertTrue(a.position().distanceTo(LongerKettle.KITCHEN_SPOT.at(b))<.05&&io.github.knaitoe.theoldesthouse.labyrinth.ActorSeat.of(a)!=null,"Rowan in the kitchen chair");
            h.assertTrue(f.own(p,P).getBoolean("Kitchen0624"),"kept");
        })
        .thenWaitUntil(()->{var a=rowan(h,p);h.assertTrue(a.getCustomName()!=null&&a.getCustomName().getString().equals(LongerTexts.TOAST_ASK),"fine; then the toaster");})
        .thenExecute(()->{
            f.click(p,P,LongerSite.TOASTER);
            h.assertTrue(kettle(f,p)==LongerKettle.TOASTING,"down");
            f.click(p,P,LongerSite.TOASTER);
            h.assertTrue(kettle(f,p)==LongerKettle.NOW&&f.own(p,P).getString(LongerKettle.TOAST).equals(LongerKettle.RESCUED),"popped in time: "+kettle(f,p));
            h.assertTrue(Talk.lines(p).contains(LongerTexts.ROWAN+": "+LongerTexts.RESCUED_LINE),"\"Show-off.\"");
            h.assertTrue(holds(p,LongerRegistry.TOAST.get())&&!holds(p,LongerRegistry.BURNT_TOAST.get()),"a good slice");
            h.assertTrue(Talk.replies(p).equals(LongerTexts.ANSWERS_NOW.get(0)),"then now: "+Talk.replies(p));
            Talk.close(p);p.stopRiding();done();h.succeed();
        }));
    }

    // ------------------------------------------------------------------------------------------------ 0.4.625, chapter 3, the radio
    private static int radio(LiteraryTests.Fixture f,ServerPlayer p){return LongerRadio.stage(f.own(p,P));}
    /** The tea as drunk: the bench and the kettle finished, the good chair noticed, and how the reader stayed. */
    private static void teaDone(LiteraryTests.Fixture f,ServerPlayer p,String stay){
        var own=f.own(p,P);own.putInt(LongerBench.STAGE,LongerBench.DONE);own.putInt(LongerKettle.STAGE,LongerKettle.DONE);own.putBoolean(LongerKettle.NOTICED,true);own.putString(LongerKettle.STAY,stay);
        LiteraryVignettes.save(f.data(),p.getUUID(),P,own);
    }
    private static boolean heardAny(ServerPlayer p,net.minecraft.sounds.SoundEvent... sounds){NativeTestPlayers.flush(p);for(var packet:NativeTestPlayers.packets(p))if(packet instanceof net.minecraft.network.protocol.game.ClientboundSoundPacket s)for(var x:sounds)if(s.getSound().value()==x)return true;return false;}
    private static boolean on(ServerLevel l,BlockPos b){return LongerBlock.stage(l.getBlockState(b.offset(LongerSite.FLAT_RADIO)))==1;}

    /**
     * After the tea Rowan asks for the radio; the reader puts it on and the tune plays (heard). Worried that something could hear it: the
     * disagreement word for word, Rowan's sharper answer, and Rowan away to the sink, through nothing solid. The reader lets it pass; Rowan
     * repairs it ("That came out badly." "What did?" "I wanted a minute where it was just a broken radio."). Quieter: the tune goes on,
     * turned down. Back in the chair, Rowan asks the reader outside plainly, and walks up the roof stair, through the stair house door,
     * to the bench under the awning, on their own feet.
     */
    @GameTest(template="empty",batch="longer_radio_quarrel",timeoutTicks=3600)
    public static void theRadioPlaysTheyDisagreeRowanRepairsItAndAsksTheReaderOutside(GameTestHelper h){
        var f=shop(h);var p=f.player("longer_radio_quarrel",P);var l=f.level(P);
        h.startSequence().thenWaitUntil(()->h.assertTrue(rowan(h,p)!=null,"Rowan is made"))
        .thenExecute(()->{teaDone(f,p,"YES");f.at(p,P,2.5,6,-14.5);})
        .thenWaitUntil(()->{var b=LiteraryVignettes.base(p,P);h.assertTrue(rowan(h,p).position().distanceTo(LongerKettle.ARMCHAIR_SPOT.at(b))<.05,"Rowan in the good chair");})
        .thenExecute(()->LongerKettle.doneWaited(LiteraryVignettes.base(p,P),LongerRadio.ASK_AFTER))
        .thenWaitUntil(()->h.assertTrue(radio(f,p)==LongerRadio.ASK_ON,"the ask: "+radio(f,p)))
        .thenExecute(()->{
            var b=LiteraryVignettes.base(p,P);
            h.assertTrue(Talk.lines(p).contains(LongerTexts.ROWAN+": "+LongerTexts.PUT_IT_ON),"\"Put the radio on, would you?\" "+Talk.lines(p));
            Talk.close(p);NativeTestPlayers.flush(p);NativeTestPlayers.packets(p);
            f.click(p,P,LongerSite.FLAT_RADIO);
            h.assertTrue(on(l,b),"the radio on");
        })
        .thenWaitUntil(()->h.assertTrue(radio(f,p)==LongerRadio.LEAVE_ASK,"\"Leave it on?\" "+radio(f,p)))
        .thenExecute(()->{
            var b=LiteraryVignettes.base(p,P);
            h.assertTrue(heardAny(p,LongerRegistry.TUNE.get(),LongerRegistry.TUNE_LOW.get()),"the tune, heard (the quiet take while Rowan is still talking)");
            h.assertTrue(Talk.replies(p).equals(List.of(LongerTexts.LEAVE_IT,LongerTexts.SUITS,LongerTexts.TURN_IT_DOWN,LongerTexts.HEARD)),"four ways: "+Talk.replies(p));
            h.assertTrue(Talk.choose(p,LongerTexts.HEARD),"something could hear that");
            var lines=Talk.lines(p);
            int a=lines.indexOf("you: "+LongerTexts.LISTENING),c=lines.indexOf(LongerTexts.ROWAN+": "+LongerTexts.HAVE_BEEN),d=lines.indexOf("you: "+LongerTexts.CHANGING),e=lines.indexOf(LongerTexts.ROWAN+": "+LongerTexts.I_KNOW),s=lines.indexOf(LongerTexts.ROWAN+": "+LongerTexts.SHARP);
            h.assertTrue(a>=0&&c>a&&d>c&&e>d&&s>e,"the disagreement word for word, in order: "+lines);
            h.assertTrue(radio(f,p)==LongerRadio.PUSHED&&f.own(p,P).getBoolean(LongerRadio.DISAGREED),"kept");
        })
        .thenWaitUntil(()->{var b=LiteraryVignettes.base(p,P);clearWalk(h,l,b,rowan(h,p));h.assertTrue(LongerRadio.leg(b)==1&&rowan(h,p).gesture()==LongerRadio.BUSY,"to the sink: "+rowan(h,p).position().subtract(b.getX(),b.getY(),b.getZ()));})
        .thenExecute(()->{
            var b=LiteraryVignettes.base(p,P);var a=rowan(h,p);
            h.assertTrue(a.position().distanceTo(LongerRadio.SINK_SPOT.at(b))<.05&&a.gesture()==LongerRadio.BUSY,"at the sink, busy with the cup");
            h.assertTrue(Talk.choose(p,LongerTexts.LET_PASS)&&radio(f,p)==LongerRadio.PASSING,"let it pass");
            LongerRadio.waited(b,LongerRadio.QUIET_PASS);
        })
        .thenWaitUntil(()->h.assertTrue(radio(f,p)==LongerRadio.REPAIR_ASK,"Rowan's repair: "+radio(f,p)))
        .thenExecute(()->{
            var lines=Talk.lines(p);
            int a=lines.indexOf(LongerTexts.ROWAN+": "+LongerTexts.BADLY),c=lines.indexOf("you: "+LongerTexts.WHAT_DID),d=lines.indexOf(LongerTexts.ROWAN+": "+LongerTexts.BROKEN_RADIO);
            h.assertTrue(a>=0&&c>a&&d>c,"the repair word for word: "+lines);
            h.assertTrue(Talk.choose(p,LongerTexts.JUST_QUIETER)&&Talk.lines(p).contains(LongerTexts.ROWAN+": "+LongerTexts.LIVE_WITH),"quieter");
            h.assertTrue(LongerRadio.low(LiteraryVignettes.base(p,P))&&on(l,LiteraryVignettes.base(p,P)),"the tune kept, turned down");
            h.assertTrue(f.own(p,P).getString(LongerRadio.REPAIR).equals(LongerRadio.QUIETER),"kept");
            Talk.close(p);NativeTestPlayers.flush(p);NativeTestPlayers.packets(p);
        })
        .thenWaitUntil(()->{var b=LiteraryVignettes.base(p,P);clearWalk(h,l,b,rowan(h,p));h.assertTrue(rowan(h,p).position().distanceTo(LongerKettle.ARMCHAIR_SPOT.at(b))<.05,"back in the chair");})
        .thenWaitUntil(()->h.assertTrue(heard(p,LongerRegistry.TUNE_LOW.get()),"the quiet take"))
        .thenExecute(()->LongerRadio.waited(LiteraryVignettes.base(p,P),LongerRadio.INVITE_AFTER))
        .thenWaitUntil(()->h.assertTrue(radio(f,p)==LongerRadio.INVITE_ASK,"the invitation: "+radio(f,p)))
        .thenExecute(()->{
            var lines=Talk.lines(p);
            h.assertTrue(lines.contains(LongerTexts.ROWAN+": "+LongerTexts.COME_OUTSIDE)&&lines.contains("you: "+LongerTexts.WHY)&&lines.contains(LongerTexts.ROWAN+": "+LongerTexts.SHOW_YOU),"\"When you finish that, come outside with me.\" "+lines);
            h.assertTrue(!lines.contains(LongerTexts.ROWAN+": "+LongerTexts.STAND_UP)&&f.own(p,P).getString(LongerRadio.INVITE).equals(LongerRadio.INVITE_DIRECT),"plainly, not flirting");
            h.assertTrue(Talk.choose(p,LongerTexts.GO_WITH)&&radio(f,p)==LongerRadio.UP,"go with them");
            Talk.close(p);
        })
        .thenWaitUntil(()->{var b=LiteraryVignettes.base(p,P);clearWalk(h,l,b,rowan(h,p));h.assertTrue(radio(f,p)==LongerRadio.ROOF,"up to the roof: "+rowan(h,p).position().subtract(b.getX(),b.getY(),b.getZ()));})
        .thenExecute(()->{
            var b=LiteraryVignettes.base(p,P);var a=rowan(h,p);
            h.assertTrue(a.position().distanceTo(LongerRadio.BENCH_SPOT.at(b))<.05&&io.github.knaitoe.theoldesthouse.labyrinth.ActorSeat.of(a)!=null,"sat on the bench under the awning");
            var door=l.getBlockState(b.offset(LongerSite.ROOF_DOOR));
            h.assertTrue(door.getValue(DoorBlock.OPEN),"the stair house door left open behind them");
            h.assertTrue(told(p).stream().noneMatch(t->t.contains(LongerTexts.BROKEN_RADIO)),"nothing in chat");
            done();h.succeed();
        });
    }

    /**
     * Nobody puts it on: Rowan gets up, walks to the worktop, switches it on and sits again. The reader says it suits them (a flirt, kept);
     * no disagreement. Rowan asks them outside and adds "And because you keep finding reasons to stand up." In a minute: Rowan goes anyway.
     * A reader who came for one cup is asked quietly instead.
     */
    @GameTest(template="empty",batch="longer_radio_flirt",timeoutTicks=3000)
    public static void rowanPutsTheRadioOnAndAsksAFlirtingReaderOutsideInKind(GameTestHelper h){
        var f=shop(h);var p=f.player("longer_radio_flirt",P);var l=f.level(P);
        h.startSequence().thenWaitUntil(()->h.assertTrue(rowan(h,p)!=null,"Rowan is made"))
        .thenExecute(()->{teaDone(f,p,"YES");f.at(p,P,2.5,6,-14.5);})
        .thenWaitUntil(()->{var b=LiteraryVignettes.base(p,P);h.assertTrue(rowan(h,p).position().distanceTo(LongerKettle.ARMCHAIR_SPOT.at(b))<.05,"Rowan in the good chair");})
        .thenExecute(()->LongerKettle.doneWaited(LiteraryVignettes.base(p,P),LongerRadio.ASK_AFTER))
        .thenWaitUntil(()->h.assertTrue(radio(f,p)==LongerRadio.ASK_ON,"the ask"))
        .thenExecute(()->{Talk.close(p);LongerRadio.waited(LiteraryVignettes.base(p,P),LongerRadio.SELF_AFTER);})
        .thenWaitUntil(()->{var b=LiteraryVignettes.base(p,P);clearWalk(h,l,b,rowan(h,p));h.assertTrue(radio(f,p)==LongerRadio.LEAVE_ASK&&on(l,b),"Rowan puts it on: leg "+LongerRadio.leg(b));})
        .thenExecute(()->{
            h.assertTrue(Talk.choose(p,LongerTexts.SUITS)&&Talk.lines(p).contains(LongerTexts.ROWAN+": "+LongerTexts.IT_DOES),"\"It does, doesn't it.\"");
            h.assertTrue(f.own(p,P).getBoolean(LongerRadio.FLIRT)&&!f.own(p,P).getBoolean(LongerRadio.DISAGREED)&&radio(f,p)==LongerRadio.TO_INVITE,"a flirt kept, no disagreement");
            Talk.close(p);
        })
        .thenWaitUntil(()->{var b=LiteraryVignettes.base(p,P);clearWalk(h,l,b,rowan(h,p));h.assertTrue(rowan(h,p).position().distanceTo(LongerKettle.ARMCHAIR_SPOT.at(b))<.05,"back in the chair");})
        .thenExecute(()->LongerRadio.waited(LiteraryVignettes.base(p,P),LongerRadio.INVITE_AFTER))
        .thenWaitUntil(()->h.assertTrue(radio(f,p)==LongerRadio.INVITE_ASK,"the invitation"))
        .thenExecute(()->{
            h.assertTrue(Talk.lines(p).contains(LongerTexts.ROWAN+": "+LongerTexts.STAND_UP),"\"And because you keep finding reasons to stand up.\" "+Talk.lines(p));
            h.assertTrue(Talk.choose(p,LongerTexts.IN_A_MINUTE)&&Talk.lines(p).contains(LongerTexts.ROWAN+": "+LongerTexts.UP_THERE)&&radio(f,p)==LongerRadio.UP,"in a minute: Rowan goes anyway");
            Talk.close(p);
            // One cup instead: asked quietly.
            var own=f.own(p,P);own.remove(LongerRadio.FLIRT);own.putString(LongerKettle.STAY,"ONE_CUP");
            h.assertTrue(LongerRadio.inviteKind(own).equals(LongerRadio.INVITE_QUIET),"one cup: the quiet invitation");
            own.putString(LongerKettle.STAY,"YES");own.putBoolean(LongerKettle.SAID_NOTHING,true);
            h.assertTrue(LongerRadio.inviteKind(own).equals(LongerRadio.INVITE_QUIET),"said nothing to the kettle: the quiet invitation");
            done();h.succeed();
        });
    }
}
