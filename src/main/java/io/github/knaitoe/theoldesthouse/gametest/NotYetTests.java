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
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * 0.4.663, Not Yet (docs/NOT_YET_0_4_663.md). Each case builds the place itself, one case to a batch, as the King's cases do.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_not_yet") @PrefixGameTestTemplate(false)
public final class NotYetTests {
    private NotYetTests(){}
    private static final LabyrinthPlace P=LabyrinthPlace.NOT_YET;
    private static LiteraryTests.Fixture active;
    private static LiteraryTests.Fixture place(GameTestHelper h){done();active=new LiteraryTests.Fixture(h);return active;}
    private static void done(){if(active!=null){active.close();active=null;}}
    private static boolean is(ServerLevel l,BlockPos b,int x,int y,int z,net.minecraft.world.level.block.Block block){return l.getBlockState(b.offset(x,y,z)).is(block);}
    private static boolean fitting(ServerLevel l,BlockPos b,BlockPos at,NotYetBlock.Kind kind){return NotYetBlock.is(l.getBlockState(b.offset(at)),kind);}
    private static int stage(ServerLevel l,BlockPos b,BlockPos at){return l.getBlockState(b.offset(at)).getValue(NotYetBlock.STAGE);}
    private static List<String> told(ServerPlayer p){
        NativeTestPlayers.flush(p);
        var out=new java.util.ArrayList<String>();
        for(var packet:NativeTestPlayers.packets(p))if(packet instanceof net.minecraft.network.protocol.game.ClientboundSystemChatPacket chat)out.add(chat.content().getString());
        return out;
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
                if(seen.contains(to)||to.getX()<x0||to.getX()>x1||to.getZ()<z0||to.getZ()>z1||to.getY()<-3||to.getY()>3)continue;
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

    /**
     * The passage over the hatch, the ward, the mending room and the long room, built of their own things. The machine's casing and
     * grating in the passage, nine plates of its floor shut over a ten-block shaft; the ward's tile, dado and painted wall under the
     * machine's cable ceiling; fifteen cots in two rows, each with its screen and lamp, curtains between them; the painted window down
     * the east wall with its one sun; the counter of seven digits over the arch, every one at nought; the table under its six arms,
     * folded; six cots in the long room, three taken, their lamps on, their screens showing noise and their curtains drawn; the far door
     * shut. A body walks from the House's door to every room and no further: not past the far door, not down the shaft. The three lie
     * in their cots and stay there, one each however often they are looked for. Everything answers a hand, the three included. The
     * chart's count is the ward's own. The House does not deal it.
     */
    @GameTest(template="empty",batch="not_yet_ward",timeoutTicks=1600)
    public static void theWardTheMendingRoomAndTheLongRoom(GameTestHelper h){
        var f=place(h);var p=f.player("not_yet_reader",P);var l=f.level(P);var b=f.base(P);
        h.startSequence().thenIdle(5).thenExecute(()->{
            var casing=KeptRegistry.material("casing");var grating=KeptRegistry.material("grating");var ceiling=KeptRegistry.material("ceiling");
            var tile=NotYetRegistry.material("tile");var wall=NotYetRegistry.material("wall");var dado=NotYetRegistry.material("dado");
            h.assertTrue(is(l,b,0,-1,-2,grating)&&is(l,b,0,NotYetSite.PASS_HIGH+1,-2,ceiling)&&is(l,b,NotYetSite.PASS_HALF+1,1,-2,casing),"the passage: grating underfoot, cable overhead, the casing either side");
            for(int x=NotYetSite.HATCH_W;x<=NotYetSite.HATCH_E;x++)for(int z=NotYetSite.HATCH_N;z<=NotYetSite.HATCH_S;z++){
                var at=new BlockPos(x,-1,z);
                h.assertTrue(fitting(l,b,at,NotYetBlock.Kind.HATCH)&&stage(l,b,at)==0,"a plate shut over the shaft at "+x+","+z);
            }
            h.assertFalse(NotYetSite.hatchOpen(l,b),"the hatch is shut");
            h.assertTrue(l.getBlockState(b.offset(0,-6,-5)).isAir()&&is(l,b,0,NotYetSite.SHAFT_FLOOR,-5,grating),"under it the shaft, ten blocks, grating at its foot");
            h.assertTrue(is(l,b,0,-1,-20,tile)&&is(l,b,-NotYetSite.WARD_HALF-1,0,-20,dado)&&is(l,b,-NotYetSite.WARD_HALF-1,3,-20,wall)&&is(l,b,0,NotYetSite.WARD_HIGH+1,-20,ceiling),"the ward: tile, dado, painted wall, cable ceiling");
            int beds=0;
            for(var head:NotYetSite.COTS){
                var facing=NotYetSite.cotFacing(head);
                h.assertTrue(fitting(l,b,head,NotYetBlock.Kind.COT)&&l.getBlockState(b.offset(head)).getValue(NotYetBlock.FACING)==facing,"a cot's head at "+head.toShortString());
                h.assertTrue(fitting(l,b,NotYetSite.cotFoot(head),NotYetBlock.Kind.COT_FOOT),"and its foot");
                h.assertTrue(fitting(l,b,NotYetSite.screen(head),NotYetBlock.Kind.SCREEN)&&fitting(l,b,NotYetSite.lamp(head),NotYetBlock.Kind.LAMP),"its screen and its lamp over it");
                beds++;
            }
            h.assertTrue(NotYetSite.WARD_COTS.size()==15&&beds==21,"fifteen cots in the ward and six in the long room: "+NotYetSite.WARD_COTS.size()+" and "+beds);
            for(int i=0;i<NotYetSite.COUNTER_DIGITS;i++){var at=NotYetSite.COUNTER_FIRST.offset(i,0,0);h.assertTrue(fitting(l,b,at,NotYetBlock.Kind.COUNTER)&&stage(l,b,at)==0,"a digit of the counter at nought, "+i);}
            int panes=0;for(int z=NotYetSite.WINDOW_N;z<=NotYetSite.WINDOW_S;z++)for(int y=NotYetSite.WINDOW_LOW;y<=NotYetSite.WINDOW_HIGH;y++)if(fitting(l,b,new BlockPos(NotYetSite.WINDOW_X,y,z),NotYetBlock.Kind.WINDOW))panes++;
            h.assertTrue(panes==(NotYetSite.WINDOW_S-NotYetSite.WINDOW_N+1)*3,"the painted window, whole: "+panes);
            h.assertTrue(stage(l,b,new BlockPos(NotYetSite.WINDOW_X,5,-21))==4&&stage(l,b,new BlockPos(NotYetSite.WINDOW_X,4,-20))==7,"its sun, all four quarters in their places");
            h.assertTrue(fitting(l,b,NotYetSite.TABLE,NotYetBlock.Kind.TABLE)&&fitting(l,b,NotYetSite.TABLE_FOOT,NotYetBlock.Kind.TABLE_FOOT),"the mending table");
            for(var arm:NotYetSite.ARMS)h.assertTrue(fitting(l,b,arm,NotYetBlock.Kind.ARM)&&stage(l,b,arm)==0,"an arm folded up over it at "+arm.toShortString());
            for(int z:NotYetSite.LONG_COTS)for(int side:new int[]{-1,1}){
                var head=new BlockPos(side*NotYetSite.WARD_HALF,0,z);boolean taken=java.util.Arrays.asList(NotYetSite.PEOPLE_COTS).contains(head);
                h.assertTrue(stage(l,b,NotYetSite.screen(head))==(taken?NotYetBlock.NOISE:NotYetBlock.DARK),"the screen over "+head.toShortString()+(taken?" shows noise":" is dark"));
                h.assertTrue(stage(l,b,NotYetSite.lamp(head))==(taken?1:0),"its lamp "+(taken?"on":"off"));
                if(taken)h.assertTrue(fitting(l,b,new BlockPos(side*NotYetSite.WARD_HALF,0,z-2),NotYetBlock.Kind.CURTAIN)&&stage(l,b,new BlockPos(side*NotYetSite.WARD_HALF,1,z-2))==3,"and a curtain drawn beside it");
            }
            h.assertTrue(fitting(l,b,NotYetSite.FAR_DOOR,NotYetBlock.Kind.DOOR)&&fitting(l,b,NotYetSite.FAR_DOOR.above(),NotYetBlock.Kind.DOOR)&&!NotYetSite.doorOpen(l,b),"the far door, both halves, shut");
            h.assertTrue(fitting(l,b,NotYetSite.CHART,NotYetBlock.Kind.CHART),"the chart on the wall by the door");
            // Walking: every room from the House's door, and nothing past the far door or down the shaft.
            var walk=walked(l,b,new BlockPos(0,0,-1),NotYetSite.WEST,NotYetSite.NORTH,NotYetSite.EAST,1);
            h.assertTrue(walk.contains(new BlockPos(0,0,-5)),"over the shut hatch: "+walk.size());
            h.assertTrue(walk.contains(new BlockPos(0,0,-20))&&walk.contains(new BlockPos(6,0,-40)),"the ward, end to end");
            h.assertTrue(walk.contains(new BlockPos(-17,0,-31)),"the mending room, through its doorway");
            h.assertTrue(walk.contains(new BlockPos(0,0,-55))&&walk.contains(new BlockPos(-6,0,-60)),"the long room, through the arch");
            h.assertFalse(walk.stream().anyMatch(c->c.getZ()<=NotYetSite.FAR_DOOR.getZ()),"not past the far door");
            h.assertFalse(walk.stream().anyMatch(c->c.getY()<-1),"not down the shaft");
            NotYet.ensure(l,b);
        }).thenWaitUntil(()->{
            for(int who=0;who<NotYet.PEOPLE;who++){var a=NotYet.find(l,b,who);h.assertTrue(a!=null&&a.hasPose(Pose.SLEEPING),"the "+who+"th lies in the cot: "+a);}
        }).thenExecute(()->{
            NotYet.ensure(l,b);
            for(int who=0;who<NotYet.PEOPLE;who++){
                var head=b.offset(NotYetSite.PEOPLE_COTS[who]);int phase=NotYet.FIRST+who;
                var them=l.getEntitiesOfClass(LiteraryActor.class,new AABB(head).inflate(4),a->NotYet.is(a)&&a.phase()==phase);
                h.assertTrue(them.size()==1,"one of each of the three, however often they are looked for: "+who+" "+them.size());
                var a=them.get(0);
                h.assertTrue(a.getSleepingPos().filter(head::equals).isPresent()&&a.position().distanceTo(Vec3.atBottomCenterOf(head))<.75,"on the cot's head, the pillow: "+a.position());
                // Drawn laid along the cot by the cot's own facing (LiteraryRenderers.lieOnNotYet), whatever way the body turns.
                h.assertTrue(NotYetBlock.is(l.getBlockState(a.getSleepingPos().get()),NotYetBlock.Kind.COT),"over a cot's head, which lays them out");
            }
            // A reader in the long room, close enough for their own ticks to run.
            p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-54.5,180,0);told(p);
        }).thenIdle(40).thenExecute(()->{
            for(int who=0;who<NotYet.PEOPLE;who++){
                var a=NotYet.find(l,b,who);var head=b.offset(NotYetSite.PEOPLE_COTS[who]);
                h.assertTrue(a!=null&&a.hasPose(Pose.SLEEPING)&&a.isSleeping()&&a.position().distanceTo(Vec3.atBottomCenterOf(head))<.75,"with a reader beside them they lie on, unmoved: "+a);
            }
            // Everything answers a hand.
            f.click(p,P,NotYetSite.WARD_COTS.get(0));f.click(p,P,NotYetSite.cotFoot(NotYetSite.WARD_COTS.get(0)));
            f.click(p,P,NotYetSite.lamp(NotYetSite.WARD_COTS.get(0)));f.click(p,P,NotYetSite.lamp(NotYetSite.PEOPLE_COTS[0]));
            f.click(p,P,NotYetSite.screen(NotYetSite.WARD_COTS.get(0)));f.click(p,P,NotYetSite.COUNTER_FIRST);
            f.click(p,P,NotYetSite.TABLE);f.click(p,P,NotYetSite.ARMS[0]);f.click(p,P,new BlockPos(0,-1,-5));
            f.click(p,P,new BlockPos(-NotYetSite.WARD_HALF,0,-14));f.click(p,P,new BlockPos(NotYetSite.WINDOW_X,4,-30));
            f.click(p,P,NotYetSite.FAR_DOOR);f.click(p,P,new BlockPos(NotYetSite.WARD_HALF,1,(NotYetSite.EAST_COTS[0]+NotYetSite.EAST_COTS[1])/2));
            for(int who=0;who<NotYet.PEOPLE;who++)NotYet.talk(p,NotYet.find(l,b,who),f.own(p,P));
        }).thenIdle(2).thenExecute(()->{
            var why=told(p);
            for(String line:List.of(NotYetTexts.COT,NotYetTexts.COT_FOOT,NotYetTexts.LAMP,NotYetTexts.LAMP_ON,NotYetTexts.SCREEN,NotYetTexts.COUNTER,NotYetTexts.TABLE,NotYetTexts.ARM,
                NotYetTexts.HATCH,NotYetTexts.VENT,NotYetTexts.WINDOW,NotYetTexts.DOOR,NotYetTexts.CURTAIN,NotYetTexts.INES,NotYetTexts.BRAM,NotYetTexts.ODILE))
                h.assertTrue(why.contains(line),"each says what it is: missing \""+line+"\" in "+why);
            // The chart: the ward's own count.
            var paper=LiteraryTexts.source(P).get(DataComponents.WRITTEN_BOOK_CONTENT);String text=String.join(" ",paper.pages().stream().map(pg->pg.raw().getString()).toList());
            h.assertTrue(text.contains("BEDS: TWENTY-ONE")&&NotYetSite.COTS.size()==21,"the chart counts the beds that are there");
            h.assertTrue(text.contains("KEPT: THREE")&&NotYetSite.PEOPLE_COTS.length==NotYet.PEOPLE&&NotYet.PEOPLE==3,"and the three it keeps");
            for(String word:List.of(" he "," she "," his "," her "," him "))h.assertFalse(text.toLowerCase(java.util.Locale.ROOT).contains(word),"the chart says nothing of what anybody is: "+word);
            h.assertTrue(LiteraryTexts.accountTitle(P).length()<=32,"the account's title fits a book: "+LiteraryTexts.accountTitle(P));
            h.assertFalse(LiteraryVignettes.canDeal(f.data(),p.getUUID(),P),"the House does not deal it yet");
            done();h.succeed();
        });
    }
}
