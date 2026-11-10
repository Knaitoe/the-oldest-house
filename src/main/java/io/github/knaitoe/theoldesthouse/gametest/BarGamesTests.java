package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.414 (docs/BAR_GAMES_PROPOSAL.md): the bar's games played for real. Pool: the same shot always ends the same way; pots, fouls,
 * groups and the eight follow the rules; the table's own player finds a pot; real readers take turns at a real table, someone who
 * comes late watches, a shot out of turn is ignored, and a reader who walks away gives up their seat. Darts (0.4.415): where a dart
 * lands gives its ring and number; 301 and round the clock keep their rules; real darts thrown from the oche stick, score and come
 * out after three; the board's own player throws on its turn.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_literary") @PrefixGameTestTemplate(false)
public final class BarGamesTests {
    private BarGamesTests(){}
    /** A table with only the balls given, at their places, everything else off. */
    static PoolGame lay(double[]... balls){
        var g=PoolGame.rack(1);for(int b=0;b<PoolGame.BALLS;b++)g.on[b]=false;
        for(var b:balls){int i=(int)b[0];g.on[i]=true;g.x[i]=b[1];g.y[i]=b[2];}
        g.phase=PoolGame.Phase.OPEN;return g;
    }
    static double toward(PoolGame g,int from,double tx,double ty){return Math.atan2(ty-g.y[from],tx-g.x[from]);}

    @GameTest(template="empty")
    public static void poolPlaysTheSameShotTheSameWayAndKeepsTheRules(GameTestHelper h){
        var a=PoolGame.rack(77);var b=PoolGame.rack(77);
        h.assertTrue(a.save().equals(b.save()),"the same seed racks the same frame");
        a.strike(.02,.9);b.strike(.02,.9);
        h.assertTrue(a.save().equals(b.save()),"and the same shot ends in the same places, ball for ball");
        var brk=PoolGame.rack(5);var r=brk.shoot(toward(brk,0,brk.x[1],brk.y[1]),1,0,0);
        h.assertTrue(r!=null&&r.first==1&&brk.phase!=PoolGame.Phase.BREAK,"the break meets the head ball and opens the table: "+(r==null?"none":r.first));
        int moved=0;var fresh=PoolGame.rack(5);for(int i=1;i<PoolGame.BALLS;i++)if(Math.hypot(brk.x[i]-fresh.x[i],brk.y[i]-fresh.y[i])>1)moved++;
        h.assertTrue(moved>=6,"and spreads the rack: "+moved+" balls moved");
        // A spot straight into the corner: it drops, the side takes spots, and shoots again.
        // Aimed along the line from the ball to the pocket, through the ball.
        var pot=lay(new double[]{0,180,70},new double[]{3,220,100},new double[]{8,60,60},new double[]{12,60,30});
        double px=PoolGame.W,py=PoolGame.H,tl=Math.hypot(px-220,py-100),gx=220-(px-220)/tl*2*PoolGame.R,gy=100-(py-100)/tl*2*PoolGame.R;
        r=pot.shoot(Math.atan2(gy-70,gx-180),.55,0,0);
        h.assertTrue(r!=null&&r.potted.contains(3)&&pot.group[0]==1&&pot.group[1]==2&&pot.side==0&&!pot.ballInHand,"a spot dropped: spots for that side, and the shot again: "+(r==null?"none":r.potted)+" groups "+pot.group[0]+"/"+pot.group[1]+" side "+pot.side);
        // The cue ball straight into a pocket with nothing in the way: a foul, and the cue ball in the other side's hand.
        var scratch=lay(new double[]{0,30,30},new double[]{3,200,100},new double[]{8,150,60});scratch.group[0]=1;scratch.group[1]=2;scratch.phase=PoolGame.Phase.PLAY;
        r=scratch.shoot(toward(scratch,0,0,0),.5,0,0);
        h.assertTrue(r!=null&&r.potted.contains(0)&&scratch.side==1&&scratch.ballInHand&&scratch.on[0],"a scratch is a foul: ball in hand for the other side");
        h.assertTrue(!scratch.canPlace(150,60)&&scratch.canPlace(100,30),"the cue ball in hand goes down only where it touches nothing");
        // The other group's ball first: a foul.
        var wrong=lay(new double[]{0,100,60},new double[]{12,140,60},new double[]{3,200,30},new double[]{8,60,100});wrong.group[0]=1;wrong.group[1]=2;wrong.phase=PoolGame.Phase.PLAY;
        r=wrong.shoot(0,.3,0,0);
        h.assertTrue(r!=null&&r.first==12&&wrong.side==1&&wrong.ballInHand,"hitting the other group's ball first is a foul: "+(r==null?"none":r.first));
        // The eight down while the side still has balls: the frame to the other side. With the group cleared: the frame.
        tl=Math.hypot(PoolGame.W-220,PoolGame.H-100);gx=220-(PoolGame.W-220)/tl*2*PoolGame.R;gy=100-(PoolGame.H-100)/tl*2*PoolGame.R;
        var early=lay(new double[]{0,180,70},new double[]{8,220,100},new double[]{2,40,40});early.group[0]=1;early.group[1]=2;early.phase=PoolGame.Phase.PLAY;
        r=early.shoot(Math.atan2(gy-70,gx-180),.55,0,0);
        h.assertTrue(r!=null&&r.potted.contains(8)&&early.phase==PoolGame.Phase.OVER&&early.winner==1,"the eight too soon loses the frame");
        var clean=lay(new double[]{0,180,70},new double[]{8,220,100},new double[]{12,40,40});clean.group[0]=1;clean.group[1]=2;clean.phase=PoolGame.Phase.PLAY;
        r=clean.shoot(Math.atan2(gy-70,gx-180),.55,0,0);
        h.assertTrue(r!=null&&clean.phase==PoolGame.Phase.OVER&&clean.winner==0,"the eight after the group wins it");
        h.assertTrue(clean.shoot(0,1,0,0)==null,"and nothing more is played on a finished frame");
        // The table's own player, steady, finds the pot that is there.
        var easy=lay(new double[]{0,180,70},new double[]{5,220,100},new double[]{8,60,60},new double[]{11,60,30});easy.group[0]=1;easy.group[1]=2;easy.phase=PoolGame.Phase.PLAY;
        var aim=easy.aim(3,1);r=easy.shoot(aim[0],aim[1],aim[2],aim[3]);
        h.assertTrue(r!=null&&r.potted.contains(5),"the table's own player pots the easy one: "+(r==null?"none":r.potted));
        h.succeed();
    }

    static List<ServerPlayer> made=new ArrayList<>();
    static ServerPlayer reader(GameTestHelper h,String name,BlockPos at){var p=NativeTestPlayers.survival(h,name);made.add(p);p.teleportTo(h.getLevel(),at.getX()+.5,at.getY(),at.getZ()+.5,0,0);return p;}
    static void clear(GameTestHelper h,BlockPos foot){var l=h.getLevel();for(int x=-2;x<=2;x++)for(int z=-2;z<=3;z++){l.setBlock(foot.offset(x,-1,z),Blocks.STONE.defaultBlockState(),3);for(int y=0;y<3;y++)l.setBlock(foot.offset(x,y,z),Blocks.AIR.defaultBlockState(),3);}}

    @GameTest(template="empty",batch="bar_pool",timeoutTicks=400)
    public static void readersTakeTurnsAtATableAndALateComerWatches(GameTestHelper h){
        var l=h.getLevel();var foot=h.absolutePos(new BlockPos(1,1,1));clear(h,foot);
        var p=reader(h,"pool_p",foot.east(2));var q=reader(h,"pool_q",foot.west(2));
        // Put down from the item, the way a reader does: the foot here, the head one block on.
        p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(BarRegistry.POOL_TABLE_ITEM.get()));
        p.setYRot(180);p.setXRot(60);
        var placed=p.getMainHandItem().useOn(new net.minecraft.world.item.context.UseOnContext(p,net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(foot.below()).add(0,.5,0),Direction.UP,foot.below(),false)));
        var footState=l.getBlockState(foot);var dir=PoolTableBlock.is(footState)?footState.getValue(PoolTableBlock.FACING):Direction.NORTH;
        h.assertTrue(placed.consumesAction()&&PoolTableBlock.is(footState)&&footState.getValue(PoolTableBlock.PART)==BedPart.FOOT&&PoolTableBlock.is(l.getBlockState(foot.relative(dir)))&&l.getBlockState(foot.relative(dir)).getValue(PoolTableBlock.PART)==BedPart.HEAD,"the table goes down two blocks long, foot and head: "+footState);
        h.assertTrue(l.getBlockEntity(foot) instanceof PoolTableBlockEntity,"its foot keeps the table");
        var head=foot.relative(dir);PoolTables.use(p,foot);PoolTables.use(q,PoolTableBlock.foot(l.getBlockState(head),head));
        var t=PoolTables.at(l,foot);
        h.assertTrue(t!=null&&t.seats.size()==2&&t.seated(p.getUUID())&&t.seated(q.getUUID())&&t.game.phase==PoolGame.Phase.BREAK,"two readers sit at it, from either end, before the break");
        h.assertTrue(p.getUUID().equals(t.shooter()),"the first to sit breaks");
        h.assertTrue(!PoolTables.shoot(q,foot,0,1,0,0),"a shot out of turn is ignored");
        var g=t.game;double brk=Math.atan2(g.y[1]-g.y[0],g.x[1]-g.x[0]);
        h.assertTrue(PoolTables.shoot(p,foot,brk,1,0,0)&&t.game.phase!=PoolGame.Phase.BREAK,"the break is played");
        var shooter=t.game.side==0?p:q;h.assertTrue(shooter.getUUID().equals(t.shooter()),"the turn follows the side whose shot it is");
        var w=reader(h,"pool_w",foot.east(3));PoolTables.use(w,foot);
        h.assertTrue(!t.seated(w.getUUID())&&t.seats.size()==2,"someone who comes after the break watches");
        // A reader who walks off gives up their seat.
        q.teleportTo(l,foot.getX()+30.5,foot.getY(),foot.getZ()+.5,0,0);
        h.runAfterDelay(45,()->{
            h.assertTrue(!t.seated(q.getUUID())&&t.seated(p.getUUID()),"a reader who walks off gives up their seat");
            h.assertTrue(t.sideOf(t.seatOf(p.getUUID()))==0&&p.getUUID().equals(t.shooter()),"the one left keeps their side and shoots for both");
            // A fresh frame with the table's own player: when the turn comes to it, it shoots.
            PoolTables.clearAll();PoolTables.use(p,foot);PoolTables.seatRegular(l,foot,"the regular",.9);
            var u=PoolTables.at(l,foot);
            h.assertTrue(u!=null&&u.seats.size()==2&&u.seated(PoolTables.REGULAR),"the table can seat its own player");
            var gg=u.game;
            gg.x[9]=PoolGame.HEAD_X;gg.y[9]=PoolGame.H/2;
            gg.x[0]=PoolGame.HEAD_X-20;
            h.assertTrue(PoolTables.shoot(p,foot,Math.PI,.1,0,0)&&PoolTables.REGULAR.equals(u.shooter())&&gg.ballInHand&&!gg.canPlace(PoolGame.HEAD_X,PoolGame.H/2),"a foul gives the regular ball in hand with the head spot occupied");
            var before=u.game.save();
            h.runAfterDelay(PoolTables.REGULAR_PAUSE+10,()->{
                h.assertTrue(!u.game.save().equals(before),"the regular plays its scheduled shot from a legal spot");
                // Broken from its head, the whole table goes and comes back as one item.
                l.destroyBlock(head,true);
                var drops=l.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(foot).inflate(3),e->e.getItem().is(BarRegistry.POOL_TABLE_ITEM.get()));
                h.assertTrue(l.getBlockState(foot).isAir()&&drops.size()==1&&drops.get(0).getItem().getCount()==1,"broken, the table goes whole and drops once: "+drops.size());
                drops.forEach(e->e.discard());
                for(var m:made)NativeTestPlayers.remove(m);made.clear();PoolTables.clearAll();h.succeed();
            });
        });
    }
    @AfterBatch(batch="bar_pool") public static void poolDone(ServerLevel l){for(var m:made)NativeTestPlayers.remove(m);made.clear();PoolTables.clearAll();}

    @GameTest(template="empty")
    public static void dartsScoreWhereTheyLandAndKeepTheRules(GameTestHelper h){
        // Where a dart lands: the bull, the outer bull, trebles, doubles and singles, by the standard number order.
        h.assertTrue(DartsGame.score(0,0).value()==50&&DartsGame.score(0,.06).value()==25,"the bull is 50 and the ring round it 25");
        double tr=(DartsGame.TREBLE_IN+DartsGame.TREBLE_OUT)/2,dr=(DartsGame.DOUBLE_IN+1)/2;
        h.assertTrue(DartsGame.score(0,tr).equals(new DartsGame.Hit(DartsGame.Ring.TREBLE,20))&&DartsGame.score(0,tr).value()==60,"straight up through the treble is treble 20");
        h.assertTrue(DartsGame.score(dr,0).equals(new DartsGame.Hit(DartsGame.Ring.DOUBLE,6)),"to the right through the double is double 6: "+DartsGame.score(dr,0));
        h.assertTrue(DartsGame.score(-.5,0).equals(new DartsGame.Hit(DartsGame.Ring.SINGLE,11))&&DartsGame.score(0,-.5).equals(new DartsGame.Hit(DartsGame.Ring.SINGLE,3)),"left is 11 and down is 3");
        double in=Math.toRadians(8.5),out=Math.toRadians(9.5);
        h.assertTrue(DartsGame.score(Math.sin(in)*.5,Math.cos(in)*.5).number()==20&&DartsGame.score(Math.sin(out)*.5,Math.cos(out)*.5).number()==1,"the wire between 20 and 1 sits nine degrees round");
        h.assertTrue(DartsGame.score(0,1.02).ring()==DartsGame.Ring.MISS,"past the double's outer wire scores nothing");
        for(int n:DartsGame.ORDER){var a=DartsGame.aimAt(DartsGame.Ring.TREBLE,n);h.assertTrue(DartsGame.score(a[0],a[1]).equals(new DartsGame.Hit(DartsGame.Ring.TREBLE,n)),"aiming at treble "+n+" lands in it");}
        // 301: down to exactly nothing, with a bust over.
        var g=new DartsGame(DartsGame.Mode.X01,2);var t20=new DartsGame.Hit(DartsGame.Ring.TREBLE,20);
        g.dart(t20);g.dart(t20);h.assertTrue(!g.turnOver()&&g.score[0]==181,"two darts in, the turn goes on: "+g.score[0]);
        g.dart(t20);h.assertTrue(g.turnOver()&&g.score[0]==121,"three darts end the turn: "+g.score[0]);
        g.dart(t20);h.assertTrue(g.score[0]==121&&g.darts==3,"a fourth dart counts for nothing");
        g.nextTurn();h.assertTrue(g.turn==1&&g.darts==0&&g.lastTurn.size()==3,"then it is the next player's turn");
        g.score[1]=40;g.turnStart=40;g.dart(new DartsGame.Hit(DartsGame.Ring.SINGLE,20));g.dart(t20);
        h.assertTrue(g.turnOver()&&g.score[1]==40&&!g.over(),"going over is a bust: back to where the turn began");
        g.nextTurn();g.nextTurn();g.dart(new DartsGame.Hit(DartsGame.Ring.DOUBLE,20));
        h.assertTrue(g.over()&&g.winner==1&&g.score[1]==0,"exactly nothing wins");
        // Round the clock: the number in order, any ring of it; after 20, the bull.
        var c=new DartsGame(DartsGame.Mode.CLOCK,2);
        c.dart(new DartsGame.Hit(DartsGame.Ring.SINGLE,1));c.dart(new DartsGame.Hit(DartsGame.Ring.TREBLE,2));c.dart(new DartsGame.Hit(DartsGame.Ring.SINGLE,20));
        h.assertTrue(c.score[0]==3&&c.turnOver(),"one, then two, and twenty out of turn counts for nothing: "+c.score[0]);
        c.nextTurn();h.assertTrue(c.target()==1,"the other player starts at one");
        c.nextTurn();c.score[0]=20;c.dart(new DartsGame.Hit(DartsGame.Ring.DOUBLE,20));h.assertTrue(c.target()==DartsGame.BULL_TARGET,"after twenty comes the bull");
        c.dart(new DartsGame.Hit(DartsGame.Ring.OUTER,25));h.assertTrue(c.over()&&c.winner==0,"and the bull goes round the clock");
        // Someone leaving: the others keep their scores and a turn that was theirs passes on.
        var three=new DartsGame(DartsGame.Mode.X01,3);three.score[2]=99;three.nextTurn();
        var two=three.without(1);h.assertTrue(two.players()==2&&two.score[1]==99&&two.turn==1&&two.darts==0,"a player leaving takes their place with them");
        h.assertTrue(DartsGame.load(g.save()).save().equals(g.save()),"a game saves and loads as it was");
        h.succeed();
    }

    /** A stone wall and floor with a board hung on it, facing north, at eye height for a reader on the floor. Returns the board. */
    static BlockPos hangBoard(GameTestHelper h,ServerPlayer placer){
        var l=h.getLevel();var foot=h.absolutePos(new BlockPos(1,1,1));
        for(int x=-3;x<=3;x++)for(int z=-4;z<=1;z++){l.setBlock(foot.offset(x,-1,z),Blocks.STONE.defaultBlockState(),3);for(int y=0;y<4;y++)l.setBlock(foot.offset(x,y,z),Blocks.AIR.defaultBlockState(),3);}
        for(int x=-3;x<=3;x++)for(int y=0;y<4;y++)l.setBlock(foot.offset(x,y,2),Blocks.STONE.defaultBlockState(),3);
        var wall=foot.offset(0,1,2);
        placer.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(BarRegistry.DARTBOARD_ITEM.get()));
        placer.setYRot(0);placer.setXRot(0);
        placer.getMainHandItem().useOn(new net.minecraft.world.item.context.UseOnContext(placer,net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(wall).add(0,0,-.5),Direction.NORTH,wall,false)));
        return wall.north();
    }
    /** Turns a reader to look at a point. */
    static void lookAt(ServerPlayer p,net.minecraft.world.phys.Vec3 at){
        var d=at.subtract(p.getEyePosition());
        p.setYRot((float)Math.toDegrees(Math.atan2(-d.x,d.z)));p.setXRot((float)-Math.toDegrees(Math.atan2(d.y,Math.hypot(d.x,d.z))));
    }
    static int houseDarts(ServerPlayer p){int n=0;for(var s:p.getInventory().items)if(DartBoards.isHouseDart(s))n+=s.getCount();return n;}
    static List<DartEntity> stuck(GameTestHelper h,BlockPos board){return h.getLevel().getEntitiesOfClass(DartEntity.class,new net.minecraft.world.phys.AABB(board).inflate(4));}

    @GameTest(template="empty",batch="bar_darts",timeoutTicks=600)
    public static void readersThrowRealDartsInTurn(GameTestHelper h){
        // Run 1499: the template keeps only its own corner's chunks ticking, and the board can hang in the next one, where a dart
        // thrown before that chunk ticks entities hangs in the air. Hold the chunks round the oche and wait for them, as the
        // regular's case does.
        var l=h.getLevel();var foot=h.absolutePos(new BlockPos(1,1,1));
        held=new NativeTestChunks();held.hold(l,new net.minecraft.world.phys.AABB(foot).inflate(16));
        h.startSequence()
            .thenWaitUntil(()->h.assertTrue(held.ticking()&&around(l,foot),"the chunks round the oche tick entities"))
            .thenExecute(()->throwInTurn(h,l,foot));
    }
    /** Whether the oche and everything within six blocks of it ticks entities. */
    private static boolean around(net.minecraft.server.level.ServerLevel l,BlockPos foot){
        for(int dx=-6;dx<=6;dx+=6)for(int dz=-6;dz<=6;dz+=6)if(!l.isPositionEntityTicking(foot.offset(dx,0,dz)))return false;
        return true;
    }
    private static void throwInTurn(GameTestHelper h,net.minecraft.server.level.ServerLevel l,BlockPos foot){
        var p=reader(h,"darts_p",foot.offset(0,0,-2));
        var board=hangBoard(h,p);var state=l.getBlockState(board);
        h.assertTrue(DartboardBlock.is(state)&&state.getValue(DartboardBlock.FACING)==Direction.NORTH,"the board hangs on the wall, face out: "+state);
        var face=DartboardBlock.centre(board,Direction.NORTH);
        // The oche: the board's distance out from the face, on the floor.
        p.teleportTo(l,face.x,foot.getY(),face.z-DartBoards.OCHE,0,0);
        var q=reader(h,"darts_q",foot.offset(2,0,-3));
        DartBoards.use(q,board,true);
        var b=DartBoards.at(l,board);
        h.assertTrue(b!=null&&b.mode==DartsGame.Mode.CLOCK,"crouching at the board before the first dart changes the game");
        DartBoards.use(q,board,true);
        h.assertTrue(b.mode==DartsGame.Mode.X01,"and changes it back");
        DartBoards.use(p,board,false);DartBoards.use(q,board,false);
        h.assertTrue(b.seats.size()==2&&p.getUUID().equals(b.shooter()),"two readers chalk up, and the first throws first");
        h.assertTrue(houseDarts(p)==3&&houseDarts(q)==0,"the thrower holds three of the house's darts and nobody else any: "+houseDarts(p)+"/"+houseDarts(q));
        lookAt(p,face);
        var hand=p.getMainHandItem();
        h.assertTrue(DartBoards.isHouseDart(hand),"they are in the thrower's hand");
        DartBoards.thrown(p,hand,20);
        h.runAfterDelay(15,()->{
            h.assertTrue(b.game!=null&&b.game.darts==1&&b.game.thisTurn.size()==1,"the first dart lands and counts");
            var hit=b.game.thisTurn.get(0);
            h.assertTrue(hit.ring()!=DartsGame.Ring.MISS&&b.game.score[0]==DartsGame.START-hit.value(),"aimed at the middle from the oche, it scores where it lands: "+hit.say());
            var darts=stuck(h,board);
            h.assertTrue(darts.size()==1&&darts.get(0).stuck()&&darts.get(0).position().distanceTo(face)<.6,"and stays stuck in the board: "+darts.size());
            var w=reader(h,"darts_w",foot.offset(-2,0,-3));DartBoards.use(w,board,false);
            h.assertTrue(!b.seated(w.getUUID())&&houseDarts(w)==0,"someone who comes after the first dart watches");
            DartBoards.thrown(q,new net.minecraft.world.item.ItemStack(BarRegistry.DART.get()),20);
            h.assertTrue(b.game.darts==1&&stuck(h,board).size()==1,"a dart that is not the house's, or not their throw, flies nowhere");
            lookAt(p,face);DartBoards.thrown(p,p.getMainHandItem(),20);
            h.runAfterDelay(6,()->{
                lookAt(p,face);DartBoards.thrown(p,p.getMainHandItem(),20);
                h.runAfterDelay(15,()->{
                    h.assertTrue(b.game.darts==3&&b.game.turnOver()&&houseDarts(p)==0,"three darts end the turn, and the thrower's hand is empty");
                    h.assertTrue(stuck(h,board).size()==3,"all three are still in the board for everyone to see");
                    h.runAfterDelay(DartBoards.PAUSE+5,()->{
                        h.assertTrue(stuck(h,board).isEmpty(),"then the darts are pulled out");
                        h.assertTrue(q.getUUID().equals(b.shooter())&&houseDarts(q)==3&&houseDarts(p)==0,"and the next player is handed three: "+houseDarts(q));
                        // A reader who walks away gives up their place; the game goes on without them.
                        q.teleportTo(l,foot.getX()+40.5,foot.getY(),foot.getZ()+.5,0,0);
                        h.runAfterDelay(30,()->{
                            h.assertTrue(!b.seated(q.getUUID())&&b.seats.size()==1&&p.getUUID().equals(b.shooter())&&houseDarts(p)==3&&houseDarts(q)==0,"a reader who walks off takes their darts and place with them; the turn comes back round");
                            h.assertTrue(b.game.players()==1&&b.game.score[0]<DartsGame.START,"and the one left keeps their score: "+b.game.score[0]);
                            // Broken, the board comes down whole and drops once.
                            l.destroyBlock(board,true);
                            var drops=l.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(board).inflate(3),e->e.getItem().is(BarRegistry.DARTBOARD_ITEM.get()));
                            h.assertTrue(drops.size()==1,"broken, the board drops itself once: "+drops.size());
                            drops.forEach(e->e.discard());
                            h.runAfterDelay(2,()->{
                                h.assertTrue(DartBoards.at(l,board)==null&&houseDarts(p)==0,"and its game and darts go with it");
                                for(var m:made)NativeTestPlayers.remove(m);made.clear();DartBoards.clearAll();h.succeed();
                            });
                        });
                    });
                });
            });
        });
    }

    @GameTest(template="empty",batch="bar_darts_regular",timeoutTicks=400)
    public static void theBoardsOwnPlayerThrowsOnItsTurn(GameTestHelper h){
        var l=h.getLevel();var foot=h.absolutePos(new BlockPos(1,1,1));
        var p=reader(h,"darts_r",foot.offset(0,0,-2));
        var board=hangBoard(h,p);var face=DartboardBlock.centre(board,Direction.NORTH);
        p.teleportTo(l,face.x+1,foot.getY(),face.z-DartBoards.OCHE,0,0);
        // Run 1498: the empty template keeps only its own corner's chunks ticking, and the board can hang in the next chunk, where the
        // regular's first dart hung in the air for the rest of the case. Hold the corner's chunks, and seat the regular only once the
        // board and the oche both tick entities.
        var oche=BlockPos.containing(face.x+1,foot.getY(),face.z-DartBoards.OCHE);var b=new DartBoards.Board[1];
        held=new NativeTestChunks();held.hold(l,new net.minecraft.world.phys.AABB(foot).inflate(16));
        h.startSequence()
            .thenWaitUntil(()->h.assertTrue(held.ticking()&&l.isPositionEntityTicking(board)&&l.isPositionEntityTicking(oche),"the board and the oche tick entities: "+l.isPositionEntityTicking(board)+"/"+l.isPositionEntityTicking(oche)))
            .thenExecute(()->{
                DartBoards.seatRegular(l,board,"the regular",.95);DartBoards.use(p,board,false);
                b[0]=DartBoards.at(l,board);
                h.assertTrue(b[0]!=null&&b[0].seats.size()==2&&DartBoards.regular(b[0].shooter())&&houseDarts(p)==0,"the board's own player throws first, and the reader waits");
            })
        // Run 1495: three throws a REGULAR_GAP apart, each landing after its flight, left about fifteen ticks of a fixed 120-tick budget,
        // and the full suite's busy server once ran past it. Wait for the three darts (within the case's limit) and report the board's state.
            .thenWaitUntil(()->h.assertTrue(b[0].game!=null&&b[0].game.lastTurn.size()+b[0].game.thisTurn.size()>=3,"on its turn it throws its three darts: "+DartBoards.describe(b[0])))
            .thenExecute(()->{
                int hits=0;for(var x:b[0].game.lastTurn.isEmpty()?b[0].game.thisTurn:b[0].game.lastTurn)if(x.ring()!=DartsGame.Ring.MISS)hits++;
                h.assertTrue(hits>=2,"and a steady hand finds the board: "+hits);
            })
            .thenWaitUntil(()->h.assertTrue(p.getUUID().equals(b[0].shooter())&&houseDarts(p)==3,"then the reader is handed three: "+DartBoards.describe(b[0])))
            .thenExecute(()->{for(var m:made)NativeTestPlayers.remove(m);made.clear();DartBoards.clearAll();release();})
            .thenSucceed();
    }
    private static NativeTestChunks held;
    private static void release(){if(held!=null){held.close();held=null;}}
    @AfterBatch(batch="bar_darts") public static void dartsDone(ServerLevel l){for(var m:made)NativeTestPlayers.remove(m);made.clear();DartBoards.clearAll();release();}
    @AfterBatch(batch="bar_darts_regular") public static void regularDone(ServerLevel l){for(var m:made)NativeTestPlayers.remove(m);made.clear();DartBoards.clearAll();release();}
}
