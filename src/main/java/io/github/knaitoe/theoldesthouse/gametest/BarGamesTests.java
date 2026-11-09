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
 * comes late watches, a shot out of turn is ignored, and a reader who walks away gives up their seat.
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
            var gg=u.game;PoolTables.shoot(p,foot,Math.atan2(gg.y[1]-gg.y[0],gg.x[1]-gg.x[0]),.15,0,0);
            boolean regularsTurn=PoolTables.REGULAR.equals(u.shooter());
            var before=u.game.save();
            h.runAfterDelay(PoolTables.REGULAR_PAUSE+10,()->{
                h.assertTrue(!regularsTurn||!u.game.save().equals(before),"on its turn the table's own player takes its shot");
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
}
