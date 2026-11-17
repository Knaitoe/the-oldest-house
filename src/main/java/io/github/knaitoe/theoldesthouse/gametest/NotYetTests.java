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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
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
            // 0.4.665: dealt from depth twelve, deep in the machine; never near the surface.
            h.assertFalse(LiteraryVignettes.canDeal(f.data(),p.getUUID(),P)||NotYet.dealable(f.data(),java.util.UUID.randomUUID()),"the House deals it only deep in: not at the surface");
            done();h.succeed();
        });
    }

    // ------------------------------------------------------------------------------------------------ 0.4.664: refused
    /** Gravity for a test player, which the game never ticks; a body lying on a bed does not move, as in the game. */
    private static void body(GameTestHelper h,ServerPlayer p){
        p.setNoGravity(false);
        h.onEachTick(()->{if(p.isRemoved()||p.isSpectator()||p.isSleeping())return;var v=p.getDeltaMovement();double y=p.onGround()&&v.y<=0?-.08:(v.y-.08)*.98;
            p.setDeltaMovement(v.x,y,v.z);p.move(net.minecraft.world.entity.MoverType.SELF,p.getDeltaMovement());
            var after=p.getDeltaMovement();double friction=p.onGround()?.546:.91;p.setDeltaMovement(after.x*friction,after.y,after.z*friction);});
    }
    /** Who is held, on the table, on a cot, and where, for a failure's message. */
    private static String where(ServerPlayer p,BlockPos b){return "held "+NotYetMending.held(p)+", table "+NotYetMending.onTable(p.getUUID())+", sleeping "+p.getSleepingPos().map(s->s.subtract(b).toShortString()).orElse("no")+", at "+p.position().subtract(b.getX(),b.getY(),b.getZ());}

    /**
     * 0.4.664: the first fall. A spectator standing over the plates opens nothing. A reader who has not fallen steps onto them at the
     * passage's edge, where a narrower hatch would have left a plate's width of grating to go round by: the plates drop under them and they
     * fall to the grating at the foot of the shaft, and the fall is refused without its harm. A moment's dark where they fell; the mending
     * table, lying, under the arms at work; bed one, with the machine's NOT YET and a seam on them, the counter running; then up at the
     * cot's side, whole, nothing they carry lost, the arms folded. The plates shut behind them, and do not open for them twice.
     */
    @GameTest(template="empty",batch="not_yet_fall",timeoutTicks=1800)
    public static void theFloorOpensTheTableMendsTheCotWakes(GameTestHelper h){
        var f=place(h);var p=f.player("not_yet_faller",P);var w=f.player("not_yet_onlooker",P);var l=f.level(P);var b=f.base(P);
        h.startSequence().thenIdle(5).thenExecute(()->{
            w.setGameMode(GameType.SPECTATOR);w.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-4.5,180,0);
            body(h,p);
            p.teleportTo(l,b.getX()-1.5,b.getY(),b.getZ()-1.5,180,0);p.connection.resetPosition();
            p.getInventory().add(new ItemStack(Items.BREAD,3));
        }).thenIdle(20).thenExecute(()->{
            h.assertFalse(NotYetSite.hatchOpen(l,b),"a spectator over the plates opens nothing");
            h.assertFalse(f.own(p,P).getBoolean("Fallen"),"nobody has fallen yet");
            w.teleportTo(l,b.getX()+.5,b.getY()+2,b.getZ()-30.5,180,0);
            p.teleportTo(l,b.getX()-1.5,b.getY(),b.getZ()-4.5,180,0);p.connection.resetPosition();
        }).thenWaitUntil(()->h.assertTrue(NotYetMending.held(p),"the floor opens under them and takes them: open "+NotYetSite.hatchOpen(l,b)+", "+where(p,b)))
        .thenExecute(()->{
            h.assertTrue(p.isAlive()&&p.getHealth()==p.getMaxHealth(),"whole: "+p.getHealth());
            h.assertTrue(p.getY()-b.getY()<NotYetSite.SHAFT_FLOOR+2,"at the foot of the shaft: "+where(p,b));
            h.assertTrue(f.own(p,P).getBoolean("Fallen"),"fallen");
        }).thenWaitUntil(()->h.assertTrue(NotYetMending.onTable(p.getUUID())&&p.getSleepingPos().filter(b.offset(NotYetSite.TABLE)::equals).isPresent(),"then on the mending table, lying: "+where(p,b)))
        .thenExecute(()->{
            for(var arm:NotYetSite.ARMS)h.assertTrue(stage(l,b,arm)==2,"the arms at work over the table");
            h.assertTrue(NotYetMending.seams(p.getUUID())==0,"no seam until the arms are done");
        }).thenWaitUntil(()->h.assertTrue(!NotYetMending.onTable(p.getUUID())&&p.getSleepingPos().filter(b.offset(NotYetSite.WARD_COTS.get(0))::equals).isPresent(),"then on bed one: "+where(p,b)))
        .thenExecute(()->{
            var own=f.own(p,P);
            h.assertTrue(own.getInt("Mended")==1&&own.contains("CountFrom"),"mended once, the counter running: "+own);
            h.assertTrue(NotYetMending.seams(p.getUUID())==1,"one seam, shown to everyone");
            h.assertTrue(NotYet.told(own).contains(NotYetTexts.NOT_YET),"the machine says it: "+NotYet.told(own));
            h.assertTrue(NotYetSite.WARD_COTS.get(0).equals(NotYetMending.cot(own)),"bed one is theirs: "+NotYetMending.cot(own));
        }).thenWaitUntil(()->h.assertFalse(NotYetMending.held(p),"and then up: "+where(p,b)))
        .thenExecute(()->{
            h.assertFalse(p.isSleeping(),"standing");
            var side=NotYetSite.bedside(NotYetSite.WARD_COTS.get(0));
            h.assertTrue(p.position().distanceTo(new Vec3(b.getX()+side.x,b.getY(),b.getZ()+side.z))<1.5,"at the cot's side: "+where(p,b));
            h.assertTrue(p.getHealth()==p.getMaxHealth()&&p.getInventory().countItem(Items.BREAD)==3,"whole, and nothing lost");
            for(var arm:NotYetSite.ARMS)h.assertTrue(stage(l,b,arm)==0,"the arms folded again");
        }).thenWaitUntil(()->h.assertFalse(NotYetSite.hatchOpen(l,b),"the plates shut again behind them"))
        .thenExecute(()->{p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-5.5,180,0);p.connection.resetPosition();})
        .thenIdle(30).thenExecute(()->{
            h.assertFalse(NotYetSite.hatchOpen(l,b),"they cross the plates again and the plates hold");
            h.assertTrue(p.getY()-b.getY()>-.5&&!NotYetMending.held(p),"standing on them: "+where(p,b));
            done();h.succeed();
        });
    }

    /**
     * 0.4.664: every death after the first is refused the same way. A reader who has fallen before is struck down in the ward: they are
     * whole at once and the machine has them; table, cot, up. A second seam, its cause kept, the machine counting. Nothing they carry is
     * dropped and nothing lies on the floor. Out of the place, the seams are no longer drawn.
     */
    @GameTest(template="empty",batch="not_yet_death",timeoutTicks=1400)
    public static void everyDeathAfterIsRefusedTheSameWay(GameTestHelper h){
        var f=place(h);var p=f.player("not_yet_struck",P);var l=f.level(P);var b=f.base(P);
        h.startSequence().thenIdle(5).thenExecute(()->{
            var own=f.own(p,P);own.putBoolean("Fallen",true);own.putInt("Mended",1);own.putInt("Cot",1);own.putLong("CountFrom",l.getGameTime()-600);LiteraryVignettes.save(f.data(),p.getUUID(),P,own);
            body(h,p);
            p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-20.5,180,0);p.connection.resetPosition();
            p.getInventory().add(new ItemStack(Items.IRON_INGOT,5));
        }).thenIdle(10).thenExecute(()->{
            h.assertTrue(NotYetMending.seams(p.getUUID())==1,"the seam they came in with is drawn on them");
            p.invulnerableTime=0;p.hurt(p.damageSources().fellOutOfWorld(),1000);
            h.assertTrue(p.isAlive()&&p.getHealth()==p.getMaxHealth(),"the death is refused: alive and whole, "+p.getHealth());
            h.assertTrue(NotYetMending.held(p),"and the machine has them");
        }).thenWaitUntil(()->h.assertFalse(NotYetMending.held(p),"mended: "+where(p,b)))
        .thenExecute(()->{
            var own=f.own(p,P);
            h.assertTrue(own.getInt("Mended")==2&&NotYetMending.seams(p.getUUID())==2,"a second seam: "+own.getInt("Mended")+" "+NotYetMending.seams(p.getUUID()));
            h.assertTrue(own.getString("LastCause").equals("outOfWorld"),"its cause kept: "+own.getString("LastCause"));
            h.assertTrue(p.getInventory().countItem(Items.IRON_INGOT)==5,"nothing dropped");
            h.assertTrue(l.getEntitiesOfClass(ItemEntity.class,new AABB(b).inflate(40)).isEmpty(),"nothing on the floor");
            h.assertTrue(NotYet.told(own).contains(NotYetTexts.mended(2)),"the machine counts: "+NotYet.told(own));
            h.assertTrue(NotYetSite.WARD_COTS.get(0).equals(NotYetMending.cot(own)),"the same cot: "+NotYetMending.cot(own));
            f.leave(p);
        }).thenWaitUntil(()->h.assertTrue(NotYetMending.seams(p.getUUID())==0,"out of the ward, the seams are no longer drawn"))
        .thenExecute(()->{done();h.succeed();});
    }

    /**
     * 0.4.664, two readers: both over the plates, both fall. The table is had in turn, never by both: one is mended while the other waits
     * in the dark where they fell. Each gets their own cot, bed one and bed two, and their own seam.
     */
    @GameTest(template="empty",batch="not_yet_two",timeoutTicks=2000)
    public static void twoFallTogetherAndAreMendedInTurn(GameTestHelper h){
        var f=place(h);var a=f.player("not_yet_first",P);var c=f.player("not_yet_second",P);var l=f.level(P);var b=f.base(P);
        h.onEachTick(()->{if(NotYetMending.onTable(a.getUUID())&&NotYetMending.onTable(c.getUUID()))h.fail("both on the table at once");});
        h.startSequence().thenIdle(5).thenExecute(()->{
            for(var p:List.of(a,c)){body(h,p);}
            a.teleportTo(l,b.getX()-.5,b.getY(),b.getZ()-4.5,180,0);c.teleportTo(l,b.getX()+1.5,b.getY(),b.getZ()-5.5,180,0);
            a.connection.resetPosition();c.connection.resetPosition();
        }).thenWaitUntil(()->h.assertTrue(NotYetMending.held(a)&&NotYetMending.held(c),"both fall: "+where(a,b)+" / "+where(c,b)))
        .thenWaitUntil(()->h.assertTrue(NotYetMending.onTable(a.getUUID())||NotYetMending.onTable(c.getUUID()),"one on the table"))
        .thenExecute(()->{var waiting=NotYetMending.onTable(a.getUUID())?c:a;h.assertTrue(NotYetMending.held(waiting)&&!waiting.isSleeping(),"the other waits in the dark: "+where(waiting,b));})
        .thenWaitUntil(()->h.assertFalse(NotYetMending.held(a)||NotYetMending.held(c),"both mended: "+where(a,b)+" / "+where(c,b)))
        .thenExecute(()->{
            var ca=NotYetMending.cot(f.own(a,P));var cc=NotYetMending.cot(f.own(c,P));
            h.assertTrue(ca!=null&&cc!=null&&!ca.equals(cc),"each their own cot: "+ca+" "+cc);
            h.assertTrue(java.util.Set.of(ca,cc).equals(java.util.Set.of(NotYetSite.WARD_COTS.get(0),NotYetSite.WARD_COTS.get(1))),"bed one and bed two");
            h.assertTrue(NotYetMending.seams(a.getUUID())==1&&NotYetMending.seams(c.getUUID())==1,"a seam each");
            done();h.succeed();
        });
    }

    /**
     * 0.4.664: what the machine says, and the rule. A reader who let the Kept's four go is told so as they come in; at the ward, the machine
     * says what it says by what the House has read of their home. A newcomer is told the machine has been waiting. The three say nothing to
     * a reader the machine has not mended; to one it has, whichever is spoken to first tells the rule, and the others then say a little
     * more. The counter's kind follows the House's read of age.
     */
    @GameTest(template="empty",batch="not_yet_rule",timeoutTicks=1200)
    public static void theMachineSpeaksAndTheLongRoomTellsTheRule(GameTestHelper h){
        var f=place(h);var l=f.level(P);var b=f.base(P);
        // Who they are to the House is settled before they come in: one let the Kept's four go and has been read; the other is new.
        var p=NativeTestPlayers.survival(h,"not_yet_listener");var q=NativeTestPlayers.survival(h,"not_yet_newcomer");f.players.add(p);f.players.add(q);
        var kept=LiteraryVignettes.personal(f.data(),p.getUUID(),LabyrinthPlace.KEPT);kept.putInt("Released",15);LiteraryVignettes.save(f.data(),p.getUUID(),LabyrinthPlace.KEPT,kept);
        HouseRead.signal(p,HouseRead.Trait.HOME,"RUNAWAY",false);HouseRead.signal(p,HouseRead.Trait.HOME,"RUNAWAY",true);
        HouseRead.signal(p,HouseRead.Trait.AGE,"DIGITAL",false);HouseRead.signal(p,HouseRead.Trait.AGE,"DIGITAL",true);
        // Both have fallen before, so the plates hold them here.
        for(var r:List.of(p,q)){var own=f.own(r,P);own.putBoolean("Fallen",true);LiteraryVignettes.save(f.data(),r.getUUID(),P,own);}
        f.arrive(p,P);f.arrive(q,P);
        h.startSequence().thenIdle(5).thenExecute(()->{
            p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-3.5,180,0);q.teleportTo(l,b.getX()-.5,b.getY(),b.getZ()-3.5,180,0);
        }).thenIdle(15).thenExecute(()->{
            h.assertTrue(NotYet.told(f.own(p,P)).contains(NotYetTexts.TOOK_FOUR),"the Kept's four remembered: "+NotYet.told(f.own(p,P)));
            h.assertTrue(NotYet.told(f.own(q,P)).contains(NotYetTexts.WAITING),"and a newcomer waited for: "+NotYet.told(f.own(q,P)));
            p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-14.5,180,0);q.teleportTo(l,b.getX()-.5,b.getY(),b.getZ()-14.5,180,0);
        }).thenIdle(15).thenExecute(()->{
            h.assertTrue(NotYet.told(f.own(p,P)).contains(NotYetTexts.home("RUNAWAY")),"at the ward, by the read of home: "+NotYet.told(f.own(p,P)));
            h.assertTrue(NotYet.told(f.own(q,P)).size()==1,"and nothing more to one with no read: "+NotYet.told(f.own(q,P)));
            h.assertTrue(NotYet.counterKind(f.data(),p.getUUID())==NotYet.SEGMENT&&NotYet.counterKind(f.data(),q.getUUID())==NotYet.DRUM,"the counter's kind by the read of age");
            NotYet.ensure(l,b);
        }).thenWaitUntil(()->{for(int who=0;who<NotYet.PEOPLE;who++)h.assertTrue(NotYet.find(l,b,who)!=null,"the three in their cots");})
        .thenExecute(()->{
            var ines=NotYet.find(l,b,NotYet.INES);var bram=NotYet.find(l,b,NotYet.BRAM);
            // Not yet mended: they say nothing; the reader sees how they lie.
            NotYet.talk(q,ines,f.own(q,P));
            h.assertTrue(ines.getCustomName()==null,"nothing said to one not yet mended: "+ines.getCustomName());
            h.assertFalse(f.own(q,P).getBoolean("Rule"),"and no rule told");
            var own=f.own(p,P);own.putInt("Mended",1);
            NotYet.talk(p,bram,own);
            h.assertTrue(own.getBoolean("Rule")&&own.getInt("RuleFrom")==NotYet.BRAM,"the first spoken to tells the rule");
            h.assertTrue(bram.getCustomName()!=null&&bram.getCustomName().getString().equals(NotYetTexts.rule(NotYet.BRAM,p)),"in their own words: "+bram.getCustomName());
            NotYet.talk(p,ines,own);
            h.assertTrue(ines.getCustomName()!=null&&ines.getCustomName().getString().equals(NotYetTexts.after(NotYet.INES,0)),"then a little more: "+ines.getCustomName());
            h.assertTrue(own.getInt("Heard"+NotYet.INES)==1&&own.getInt("Heard"+NotYet.BRAM)==0,"counted, so the next is the next");
            LiteraryVignettes.save(f.data(),p.getUUID(),P,own);
            done();h.succeed();
        });
    }

    // ------------------------------------------------------------------------------------------------ 0.4.665: lie down
    /** A reader the machine has mended once, on bed one, who has heard the rule; whole, and open to harm as a player in the world is. */
    private static void ready(LiteraryTests.Fixture f,ServerPlayer p,int cot){
        var own=f.own(p,P);own.putBoolean("Fallen",true);own.putInt("Mended",1);own.putInt("Cot",cot);own.putBoolean("Rule",true);own.putLong("CountFrom",f.level(P).getGameTime()-600);
        LiteraryVignettes.save(f.data(),p.getUUID(),P,own);
        vulnerable(p);
    }
    /** A mock player is never ticked, so the game's grace after joining never runs out: clear it, as the game would have by now. */
    private static void vulnerable(ServerPlayer p){
        try{var f=ServerPlayer.class.getDeclaredField("spawnInvulnerableTime");f.setAccessible(true);f.setInt(p,0);}
        catch(ReflectiveOperationException e){throw new IllegalStateException("the joining grace could not be cleared",e);}
    }
    /** Up off the cot the way a reader gets up: the bed screen's Leave Bed. */
    private static void leaveBed(ServerPlayer p){p.connection.handlePlayerCommand(new ServerboundPlayerCommandPacket(p,ServerboundPlayerCommandPacket.Action.STOP_SLEEPING));}
    /** Moves a trial on to a point, in thousandths, and lets the machine act on it. */
    private static void to(ServerPlayer p,ServerLevel l,int thousandths){
        int now=NotYetTrial.progress(p.getUUID(),p.server.getTickCount());if(now<0)return;
        int len=NotYetTrial.length(LiteraryVignettes.personal(io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthData.get(p.server),p.getUUID(),P));
        NotYetTrial.advance(p.getUUID(),Math.max(0,(thousandths-now)*len/1000+1));
    }
    private static int cotStage(ServerLevel l,BlockPos b,BlockPos head){return stage(l,b,head);}
    /** The reader's own account, as words. */
    private static String pages(ServerPlayer p,LiteraryTests.Fixture f){return LiteraryTexts.ending(p.getGameProfile().getName(),P,f.own(p,P)).get(net.minecraft.core.component.DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().map(x->x.raw().getString()).reduce("",(x,y)->x+" "+y).replaceAll("\\s+"," ");}

    /**
     * 0.4.665: lying down, and getting up. A reader the machine has not mended lies on nothing: a cot is only a cot to them. One it has
     * mended and who has heard the rule is asked to lie down, and their lamp comes on; another's cot is not theirs; their own takes them,
     * lying on it under the lamp. The cot heats and burns them, and nothing gets them up, not the burns: only the bed screen's Leave
     * Bed. Then it is over, the cot cools, the lamp goes off, the machine says what it says, and the next lie-down is longer.
     */
    @GameTest(template="empty",batch="not_yet_lie",timeoutTicks=1200)
    public static void lyingDownStartsTheTrialAndOnlyTheReaderEndsIt(GameTestHelper h){
        var f=place(h);var l=f.level(P);var b=f.base(P);
        var p=NativeTestPlayers.survival(h,"not_yet_lier");var q=NativeTestPlayers.survival(h,"not_yet_unmended");f.players.add(p);f.players.add(q);
        ready(f,p,1);var qo=f.own(q,P);qo.putBoolean("Fallen",true);LiteraryVignettes.save(f.data(),q.getUUID(),P,qo);
        f.arrive(p,P);f.arrive(q,P);
        var mine=NotYetSite.WARD_COTS.get(0);var theirs=NotYetSite.WARD_COTS.get(1);
        h.startSequence().thenIdle(5).thenExecute(()->{
            p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-16.5,180,0);q.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-18.5,180,0);
        }).thenIdle(25).thenExecute(()->{
            h.assertTrue(NotYet.told(f.own(p,P)).contains(NotYetTexts.INVITE),"the machine asks them to lie down: "+NotYet.told(f.own(p,P)));
            h.assertTrue(fitting(l,b,NotYetSite.lamp(mine),NotYetBlock.Kind.LAMP)&&stage(l,b,NotYetSite.lamp(mine))==1,"and their lamp comes on over their cot");
            h.assertFalse(NotYet.told(f.own(q,P)).contains(NotYetTexts.INVITE),"nobody unmended is asked");
            f.click(q,P,mine);
            h.assertFalse(NotYetTrial.lying(q.getUUID())||q.isSleeping(),"a cot is only a cot to one it has not mended");
            f.click(p,P,theirs);
            h.assertFalse(NotYetTrial.lying(p.getUUID()),"another's cot is not theirs");
            f.click(p,P,NotYetSite.cotFoot(mine));
        }).thenIdle(2).thenExecute(()->{
            h.assertTrue(NotYetTrial.lying(p.getUUID())&&p.isSleeping()&&p.getSleepingPos().filter(b.offset(mine)::equals).isPresent(),"lying on their own cot: "+where(p,b));
            h.assertTrue(f.own(p,P).getInt("LayDown")==1,"counted");
            to(p,l,NotYetTrial.HEAT_FROM+10);
        }).thenIdle(90).thenExecute(()->{
            h.assertTrue(cotStage(l,b,mine)==NotYetBlock.HEATING,"the cot heats");
            h.assertTrue(p.getHealth()<p.getMaxHealth(),"and it burns: "+p.getHealth());
            h.assertTrue(p.isSleeping()&&NotYetTrial.lying(p.getUUID()),"and nothing gets them up, not the burns");
            leaveBed(p);
        }).thenIdle(2).thenExecute(()->{
            var own=f.own(p,P);
            h.assertFalse(p.isSleeping()||NotYetTrial.lying(p.getUUID()),"the reader got up, and it is over");
            h.assertTrue(own.getInt("GotUp")==1&&NotYet.told(own).contains(NotYetTexts.THERE),"the machine says so: "+NotYet.told(own));
            h.assertTrue(cotStage(l,b,mine)==NotYetBlock.PLAIN&&stage(l,b,NotYetSite.lamp(mine))==0,"the cot cools and the lamp goes off");
            h.assertTrue(NotYetTrial.length(own)==NotYetTrial.FIRST+NotYetTrial.LONGER,"the next lie-down is longer: "+NotYetTrial.length(own));
            f.click(p,P,mine);
        }).thenIdle(2).thenExecute(()->{
            h.assertTrue(NotYetTrial.lying(p.getUUID())&&NotYet.told(f.own(p,P)).contains(NotYetTexts.AGAIN),"lying down again: \"again, then\"");
            leaveBed(p);
            done();h.succeed();
        });
    }

    /**
     * 0.4.665, alone, to the end. A reader whose companion the House saw die hears it called by name; the screens show the far door
     * open, which it is not, and the House keeps both; the man in the long room is hurt on the screen, and says to stay down; the cold
     * comes in, and the machine kills them, and the death is refused where they lie: a second seam, and they stay down. Then everything
     * goes quiet, the far door opens, and it stays open while they are near it and shuts once they have gone. Lying down again, the
     * machine does not bother.
     */
    @GameTest(template="empty",batch="not_yet_stay",timeoutTicks=2400)
    public static void stayingDownToTheEndOpensTheFarDoor(GameTestHelper h){
        var f=place(h);var l=f.level(P);var b=f.base(P);
        var p=NativeTestPlayers.survival(h,"not_yet_stayer");f.players.add(p);ready(f,p,1);
        var hotel=HotelVignette.personal(f.data(),p.getUUID());var lost=new net.minecraft.nbt.ListTag();var moss=new net.minecraft.nbt.CompoundTag();moss.putString("Name","Moss");lost.add(moss);hotel.put("LostPets",lost);HotelVignette.save(f.data(),p.getUUID(),hotel);
        f.arrive(p,P);
        var mine=NotYetSite.WARD_COTS.get(0);
        h.startSequence().thenIdle(5).thenExecute(()->{
            p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-16.5,180,0);
            f.click(p,P,mine);
        }).thenIdle(2).thenExecute(()->{
            h.assertTrue(NotYetTrial.lying(p.getUUID()),"lying down");
            to(p,l,NotYetTrial.CALL+5);
        }).thenIdle(3).thenExecute(()->{
            var heard=f.own(p,P).getList("TrialHeard",net.minecraft.nbt.Tag.TAG_STRING).toString();
            h.assertTrue(heard.contains("Moss"),"the companion the House saw them lose, called by name: "+heard);
            to(p,l,NotYetTrial.LIE+5);
        }).thenIdle(3).thenExecute(()->{
            var own=f.own(p,P);
            h.assertTrue(NotYet.told(own).contains(NotYetTexts.DOOR_IS_OPEN),"the screens say the door is open");
            var lies=NotYetTrial.lies(own);
            h.assertTrue(!lies.isEmpty()&&lies.get(lies.size()-1).getString("Line").equals(NotYetTexts.DOOR_IS_OPEN)&&!lies.get(lies.size()-1).getBoolean("True"),"and it is not, and the House keeps both: "+lies);
            h.assertFalse(NotYetSite.doorOpen(l,b),"the far door is shut");
            to(p,l,NotYetTrial.HURT_TO);
        }).thenIdle(3).thenExecute(()->{
            var own=f.own(p,P);
            h.assertTrue(NotYet.told(own).contains(NotYetTexts.I_WILL_STOP),"get up and I will stop");
            h.assertTrue(own.getList("TrialHeard",net.minecraft.nbt.Tag.TAG_STRING).toString().contains("Stay down"),"and the man on the screen says to stay down");
            to(p,l,NotYetTrial.COLD_FROM+20);
        }).thenIdle(3).thenExecute(()->{
            h.assertTrue(p.getTicksFrozen()>0&&NotYet.told(f.own(p,P)).contains(NotYetTexts.WARM),"the cold comes in: "+p.getTicksFrozen());
            to(p,l,NotYetTrial.BLOW+5);
        }).thenIdle(3).thenExecute(()->{
            var own=f.own(p,P);
            h.assertTrue(p.isAlive()&&p.getHealth()==p.getMaxHealth(),"killed, and the death refused: "+p.getHealth());
            h.assertTrue(own.getInt("Mended")==2&&own.getInt("MendedLying")==1&&own.getString("LastCause").equals("machine")&&NotYetMending.seams(p.getUUID())==2,"mended where they lie, a second seam: "+own);
            h.assertTrue(NotYetTrial.lying(p.getUUID())&&p.isSleeping()&&!NotYetMending.held(p),"and they stay down, on their cot, not taken to the table");
            to(p,l,1000);
        }).thenIdle(3).thenExecute(()->{
            var own=f.own(p,P);
            h.assertTrue(own.getBoolean("StayedDown")&&!NotYetTrial.lying(p.getUUID()),"it looked away: they stayed down");
            h.assertTrue(NotYetSite.doorOpen(l,b),"the far door opens");
            h.assertTrue(p.getTicksFrozen()==0&&cotStage(l,b,mine)==NotYetBlock.MADE,"the cold gone, the cot cool and made up tight: "+cotStage(l,b,mine));
            h.assertTrue(own.getBoolean("Ready")&&own.getString("Outcome").equals("stayed_down"),"its account is ready: "+own);
            leaveBed(p);
        }).thenIdle(2).thenExecute(()->{
            var own=f.own(p,P);
            h.assertTrue(own.getInt("GotUp")==0,"getting up after does not count as getting up");
            h.assertTrue(cotStage(l,b,mine)==NotYetBlock.MADE&&cotStage(l,b,NotYetSite.WARD_COTS.get(1))==NotYetBlock.PLAIN,"their cot stays made once they are up, and only theirs");
            f.click(p,P,mine);
            h.assertTrue(p.containerMenu instanceof LiteraryVignettes.Pages,"the drawer under their own cot holds the account");
            p.closeContainer();
            h.assertFalse(NotYetTrial.lying(p.getUUID()),"lying down again, it does not bother");
            var why=NotYet.told(f.own(p,P));var seen=told(p);
            h.assertTrue(why.contains(NotYetTexts.NOT_YOU)&&seen.contains(NotYetTexts.DRAWER),"not you, not now; and the drawer slides out: "+why+" / "+seen);
            var text=pages(p,f);
            h.assertTrue(text.contains("Moss")&&text.contains("It was not.")&&text.contains("the man from the long room")&&text.contains("STAYED DOWN")&&text.contains("MENDED: TWO"),"the account is what happened: "+text);
            h.assertFalse(text.contains("got up once")||text.contains("lying in the ward with me"),"and not what did not: "+text);
            f.source(p,P);f.read(p,P,mine);
            h.assertTrue(f.own(p,P).getBoolean("Completed")&&WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.NOT_YET),"read through after the chart, it is theirs: "+f.own(p,P));
            // The long room's three: one thing more to a reader who stayed down.
            NotYet.ensure(l,b);
        }).thenWaitUntil(()->{for(int who=0;who<NotYet.PEOPLE;who++)h.assertTrue(NotYet.find(l,b,who)!=null,"the three in their cots");})
        .thenExecute(()->{
            for(int who=0;who<NotYet.PEOPLE;who++){var a=NotYet.find(l,b,who);NotYet.talk(p,a,f.own(p,P));
                h.assertTrue(a.getCustomName()!=null&&a.getCustomName().getString().equals(NotYetTexts.stayed(who,p)),"each says one thing more to one who stayed: "+a.getCustomName());}
            p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-60.5,180,0);
        }).thenIdle(NotYetTrial.DOOR_HELD+20).thenExecute(()->{
            h.assertTrue(NotYetSite.doorOpen(l,b),"open while one who stayed down is near it");
            f.leave(p);
        }).thenWaitUntil(()->h.assertFalse(NotYetSite.doorOpen(l,b),"and shut once they have gone"))
        .thenExecute(()->{done();h.succeed();});
    }

    /**
     * 0.4.665, two lying down: the machine uses each on the other. It calls the other's name; it tells each the other got up, and the
     * House knows it was a lie; it hurts the other, really, and tells each who could stop it. When one really gets up the other is told,
     * and that one was true.
     */
    @GameTest(template="empty",batch="not_yet_pair",timeoutTicks=1600)
    public static void twoLieDownAndTheMachineUsesEachOnTheOther(GameTestHelper h){
        var f=place(h);var l=f.level(P);var b=f.base(P);
        var a=NativeTestPlayers.survival(h,"not_yet_alpha");var c=NativeTestPlayers.survival(h,"not_yet_gamma");f.players.add(a);f.players.add(c);
        ready(f,a,1);ready(f,c,2);f.arrive(a,P);f.arrive(c,P);
        h.startSequence().thenIdle(5).thenExecute(()->{
            a.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-16.5,180,0);c.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-18.5,180,0);
            f.click(a,P,NotYetSite.WARD_COTS.get(0));f.click(c,P,NotYetSite.WARD_COTS.get(1));
        }).thenIdle(2).thenExecute(()->{
            h.assertTrue(NotYetTrial.lying(a.getUUID())&&NotYetTrial.lying(c.getUUID()),"both lie down");
            to(a,l,NotYetTrial.LIE+5);
        }).thenIdle(3).thenExecute(()->{
            var own=f.own(a,P);
            h.assertTrue(own.getList("TrialHeard",net.minecraft.nbt.Tag.TAG_STRING).toString().contains(c.getGameProfile().getName()),"the other's name, called");
            var lies=NotYetTrial.lies(own);
            h.assertTrue(!lies.isEmpty()&&lies.get(lies.size()-1).getString("Line").equals(NotYetTexts.gotUpLie(c.getGameProfile().getName()))&&!lies.get(lies.size()-1).getBoolean("True"),"told the other got up, and the House knows it was a lie: "+lies);
            to(a,l,NotYetTrial.HURT_FROM+5);
        }).thenIdle(3).thenExecute(()->{
            h.assertTrue(c.getHealth()<c.getMaxHealth()&&NotYetTrial.lying(c.getUUID())&&c.isSleeping(),"the other really hurt, and still lying down: "+c.getHealth());
            h.assertTrue(NotYet.told(f.own(a,P)).contains(NotYetTexts.thatIs(c.getGameProfile().getName())),"that is them; get up and I will stop");
            h.assertTrue(NotYet.told(f.own(c,P)).contains(NotYetTexts.couldStop(a.getGameProfile().getName())),"and the other told who could stop it");
            leaveBed(c);
        }).thenIdle(2).thenExecute(()->{
            h.assertFalse(NotYetTrial.lying(c.getUUID()),"the other got up");
            var own=f.own(a,P);var line=NotYetTexts.gotUpTrue(c.getGameProfile().getName());
            h.assertTrue(NotYet.told(own).contains(line),"and the one still lying down is told: "+NotYet.told(own));
            var lies=NotYetTrial.lies(own);
            h.assertTrue(lies.get(lies.size()-1).getString("Line").equals(line)&&lies.get(lies.size()-1).getBoolean("True"),"and that one was so");
            h.assertTrue(NotYetTrial.lying(a.getUUID()),"they are still lying down");
            // What each one's account would say: the other by name, where the machine used them, and which line was true.
            String an=a.getGameProfile().getName(),cn=c.getGameProfile().getName();var mine=pages(a,f);var theirs=pages(c,f);
            h.assertTrue(mine.contains("It hurt "+cn+" where they lay")&&mine.contains("When "+cn+" got up, it told me so. That was true.")&&mine.contains("The screens said "+cn+" had got up and was standing at the door. "+cn+" had not.")&&mine.contains("it called "+cn+" from the far end of the ward"),"the one still lying: "+mine);
            h.assertTrue(theirs.contains("It hurt me where I lay, and told "+an+" that they could stop it."),"the one hurt: "+theirs);
            leaveBed(a);
            done();h.succeed();
        });
    }
}
