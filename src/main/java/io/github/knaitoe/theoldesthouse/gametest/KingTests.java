package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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
 * 0.4.641, A sort of thing (docs/SLEEPING_KING_PLAN.md). Each case builds the place itself, one case to a batch, as the literary
 * cases do.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_king") @PrefixGameTestTemplate(false)
public final class KingTests {
    private KingTests(){}
    private static final LabyrinthPlace P=LabyrinthPlace.SLEEPING_KING;
    private static LiteraryTests.Fixture active;
    private static LiteraryTests.Fixture place(GameTestHelper h){done();active=new LiteraryTests.Fixture(h);return active;}
    private static void done(){if(active!=null){active.close();active=null;}}
    private static boolean is(ServerLevel l,BlockPos b,int x,int y,int z,String material){return l.getBlockState(b.offset(x,y,z)).is(KingRegistry.material(material));}
    private static boolean fitting(ServerLevel l,BlockPos b,BlockPos at,KingBlock.Kind kind){return KingBlock.is(l.getBlockState(b.offset(at)),kind);}
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
     * The drawing-room and its glass, the room the other way round behind it, and the ruled country. Built of their own things: the
     * room's paper and panel, boards, ceiling and hearth, the chimney stone; the board's turf in light and dark squares, filled where
     * the King sleeps deepest and ruled where he does not, with the band between drawn a block at a time; hedges with gaps, brooks
     * sunk between the ranks with a footbridge over each, and the wood on the fourth rank. A body walks the drawing-room and no
     * further: the glass is the only way on, and a hand on it takes a reader through, either way. From the room behind the glass the
     * whole board is walkable, out to the King's tree, where he lies asleep. Every thing answers a hand; the chess problem is the
     * first paper, printed backwards, and reads the right way round behind the glass. The House does not deal the place.
     */
    @GameTest(template="empty",batch="king_built",timeoutTicks=1600)
    public static void theDrawingRoomTheGlassAndTheRuledCountry(GameTestHelper h){
        var f=place(h);var p=f.player("king_reader",P);var l=f.level(P);var b=f.base(P);
        h.startSequence().thenIdle(5).thenExecute(()->{
            h.assertTrue(is(l,b,-7,3,-8,"wallpaper")&&is(l,b,-7,0,-8,"panel")&&is(l,b,0,-1,-5,"floorboards")&&is(l,b,0,KingSite.CEIL,-5,"ceiling")&&is(l,b,0,-1,-11,"hearth")&&is(l,b,-2,2,-12,"chimney"),
                "the drawing-room's paper over its panel, its boards, ceiling, hearth and chimney-breast");
            h.assertTrue(is(l,b,-7,3,-19,"wallpaper")&&is(l,b,0,-1,-20,"floorboards")&&is(l,b,0,-1,-16,"hearth")&&is(l,b,8,2,-10,"chimney"),"the room behind the glass the same, and the house's outer wall of stone");
            // The chimney-piece and the glass, in both rooms, the second the first reflected.
            for(int i=-1;i<=1;i++){
                for(boolean behind:new boolean[]{false,true}){
                    int z=behind?-15:-12;var into=behind?Direction.NORTH:Direction.SOUTH;
                    var mantel=l.getBlockState(b.offset(i,0,z));var low=l.getBlockState(b.offset(i,1,z));var high=l.getBlockState(b.offset(i,2,z));
                    h.assertTrue(KingBlock.is(mantel,KingBlock.Kind.MANTEL)&&KingBlock.stage(mantel)==i+1&&mantel.getValue(KingBlock.FACING)==into,"the chimney-piece, part "+(i+1)+(behind?" behind the glass":""));
                    h.assertTrue(KingBlock.is(low,KingBlock.Kind.GLASS)&&KingBlock.stage(low)==i+1&&KingBlock.is(high,KingBlock.Kind.GLASS)&&KingBlock.stage(high)==i+4,"the glass over it, both rows, at "+i+(behind?" behind":""));
                }
            }
            h.assertTrue(KingBlock.light(l.getBlockState(b.offset(KingSite.FIRE)))>0,"the fire gives the room its light");
            h.assertTrue(fitting(l,b,KingSite.TABLE,KingBlock.Kind.TABLE)&&fitting(l,b,KingSite.PROBLEM,KingBlock.Kind.PROBLEM)&&KingBlock.stage(l.getBlockState(b.offset(KingSite.PROBLEM)))==0
                &&fitting(l,b,KingSite.reflect(KingSite.PROBLEM),KingBlock.Kind.PROBLEM)&&KingBlock.stage(l.getBlockState(b.offset(KingSite.reflect(KingSite.PROBLEM))))==1,"the problem on its table, backwards here and the right way behind the glass");
            h.assertTrue(fitting(l,b,KingSite.CHESSMEN,KingBlock.Kind.CHESSMEN)&&fitting(l,b,KingSite.ARMCHAIR,KingBlock.Kind.ARMCHAIR)&&fitting(l,b,KingSite.RUG,KingBlock.Kind.RUG),"the chessmen, the armchair and the hearthrug");
            for(int x=-7;x<=7;x+=14)for(int z:new int[]{-4,-6,-21,-23})h.assertTrue(fitting(l,b,new BlockPos(x,1,z),KingBlock.Kind.WINDOW)&&KingBlock.stage(l.getBlockState(b.offset(x,2,z)))==1,"a tall window, both halves, at "+x+","+z);
            // The country: light and dark squares, the brooks and their bridges, the hedges.
            int third=KingSite.rankNear(3)-4,seventh=KingSite.rankNear(7)-4;
            h.assertTrue(is(l,b,-17,-1,third,KingSite.light(3,1)?"turf_light":"turf_dark")&&is(l,b,-1,-1,third,"path")&&is(l,b,16,-1,third,KingSite.light(3,3)?"turf_light":"turf_dark"),"the third rank filled: its squares and the road");
            h.assertTrue(is(l,b,-17,-1,seventh,KingSite.light(7,1)?"turf_light_ruled":"turf_dark_ruled")&&(is(l,b,40,-1,third,"turf_light_ruled")||is(l,b,40,-1,third,"turf_dark_ruled")),"the seventh rank ruled, and the country beyond the strip");
            int ruled=0,filled=0;for(int x=-25;x<=24;x++)for(int z=KingSite.rankFar(2);z<=KingSite.rankNear(2);z++){var s=l.getBlockState(b.offset(x,-1,z));if(KingRegistry.ruled(s))ruled++;else if(KingRegistry.material(s))filled++;}
            h.assertTrue(ruled>80&&filled>80,"the second rank is the band: some of it ruled, some filled ("+ruled+" ruled, "+filled+" filled)");
            int brook=KingSite.rankFar(3)-1;
            h.assertTrue(is(l,b,-17,-2,brook,"brook")&&l.getBlockState(b.offset(-17,-1,brook)).isAir()&&is(l,b,0,-1,brook,"bridge"),"the brook beyond the third rank, sunk a block, and its footbridge");
            h.assertTrue(is(l,b,-9,0,third,"hedge")&&!l.getBlockState(b.offset(-9,0,KingSite.rankNear(3)-7)).blocksMotion(),"a hedge between the files, with a gap in the rank");
            h.assertTrue(l.getBlockState(b.offset(KingSite.TREE)).is(KingRegistry.material("bark"))&&java.util.stream.IntStream.rangeClosed(6,9).anyMatch(y->l.getBlockState(b.offset(KingSite.TREE).above(y)).is(KingRegistry.material("bough"))),"his tree, its trunk and its crown");
            // Walking: the drawing-room alone, its glass the only way on.
            var room=walked(l,b,new BlockPos(0,0,-1),-8,-30,8,0);
            h.assertTrue(room.contains(new BlockPos(-5,0,-5))&&room.contains(new BlockPos(5,0,-11)),"the drawing-room, wall to wall: "+room.size());
            h.assertFalse(room.stream().anyMatch(c->c.getZ()<=-13),"nothing past the glass on foot");
            var behind=walked(l,b,new BlockPos(0,0,-16),-30,KingSite.rankFar(4)-1,30,-14);
            h.assertTrue(behind.contains(new BlockPos(0,0,-29))&&behind.contains(new BlockPos(0,0,KingSite.rankNear(2)-1))&&behind.contains(new BlockPos(-17,0,KingSite.rankNear(3)-7))&&behind.contains(new BlockPos(16,0,KingSite.KING.getZ()+2)),
                "from behind the glass, out of its door and over the ranks to the King's square: "+behind.size());
            h.assertFalse(behind.stream().anyMatch(c->c.getZ()>-14),"and not back through the glass on foot");
            King.ensure(l,b);
        }).thenWaitUntil(()->{var k=King.find(l,b);h.assertTrue(k!=null&&k.hasPose(Pose.SLEEPING),"the King lies asleep under his tree: "+k);})
        .thenExecute(()->{
            var k=King.find(l,b);h.assertTrue(k.position().distanceTo(Vec3.atBottomCenterOf(b.offset(KingSite.KING)))<.75,"at the foot of his tree: "+k.position());
            King.ensure(l,b);h.assertTrue(l.getEntitiesOfClass(LiteraryActor.class,new AABB(b.offset(KingSite.KING)).inflate(10),King::is).size()==1,"one King, however often he is looked for");
            // Through the glass and back.
            p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-10.5,180,0);told(p);
            f.click(p,P,new BlockPos(0,1,-12));
            h.assertTrue(KingSite.behindGlass(p.position().subtract(b.getX(),b.getY(),b.getZ())),"a hand on the glass and the reader is on its other side: "+p.position());
            f.click(p,P,new BlockPos(0,1,-15));
            h.assertTrue(KingSite.inRoom(p.position().subtract(b.getX(),b.getY(),b.getZ())),"and back again: "+p.position());
            f.click(p,P,KingSite.CHESSMEN);f.click(p,P,KingSite.ARMCHAIR);f.click(p,P,KingSite.FIRE);f.click(p,P,KingSite.reflect(KingSite.PROBLEM));f.click(p,P,new BlockPos(-17,-1,KingSite.rankNear(7)-4));
        }).thenIdle(2).thenExecute(()->{
            var why=told(p);
            h.assertTrue(why.contains(KingTexts.GLASS)&&why.contains(KingTexts.GLASS_BACK)&&why.contains(KingTexts.CHESSMEN)&&why.contains(KingTexts.ARMCHAIR)&&why.contains(KingTexts.MANTEL)&&why.contains(KingTexts.PROBLEM)&&why.contains(KingTexts.RULED),"each says what it is: "+why);
            var paper=LiteraryTexts.source(P).get(DataComponents.WRITTEN_BOOK_CONTENT);String text=String.join(" ",paper.pages().stream().map(pg->pg.raw().getString()).toList());
            h.assertTrue(text.contains(".sevom nevele ni niw dna ,yalp ot etihW"),"the problem, printed backwards");
            for(String word:List.of(" he "," she "," his "," her "," him "))h.assertFalse(text.toLowerCase(java.util.Locale.ROOT).contains(word),"the paper says nothing of what anybody is: "+word);
            h.assertFalse(LiteraryVignettes.canDeal(f.data(),p.getUUID(),P),"the House does not deal it yet");
            done();h.succeed();
        });
    }

    /**
     * 0.4.642, his breath and how much of a thing a reader is. Filled land makes a thing, held by the hedges and stopped at the brooks'
     * verge; ruled land makes a line, which goes through both; another reader who is a thing looking at a line makes it a sort of
     * thing; beside him on his in-breath a reader is too much of a thing and rooted, and free again on the out-breath; off the country
     * altogether a reader is nowhere and is put back. The in-breath moves the fill's edge a block out, and every block whose drawing
     * changes goes over to its twin and back.
     */
    @GameTest(template="empty",batch="king_breath",timeoutTicks=1600)
    public static void hisBreathAndHowMuchOfAThingAReaderIs(GameTestHelper h){
        var f=place(h);var p=f.player("king_breather",P);var q=f.player("king_watcher",P);var l=f.level(P);var b=f.base(P);
        h.startSequence().thenIdle(5).thenExecute(()->{
            var own=new net.minecraft.nbt.CompoundTag();var theirs=new net.minecraft.nbt.CompoundTag();
            // Out-breath: the land shows its resting drawing.
            KingBreath.set(l,b,KingBreath.IN+10);
            h.assertTrue(KingBreath.shown(b)==0,"on the out-breath the land shows its resting drawing");
            // A thing on filled land: the hedge is a wall, the brook's verge a wall.
            int filledZ=KingSite.rankNear(4)-4;
            p.teleportTo(l,b.getX()-15.5,b.getY(),b.getZ()+filledZ+.5,0,0);q.teleportTo(l,b.getX()+60.5,b.getY(),b.getZ()-4.5,0,0);
            KingBreath.reader(p,b,own);
            h.assertTrue(own.getInt("KingState")==KingBreath.THING,"filled land makes a thing, got "+own.getInt("KingState"));
            var hedge=b.offset(-9,0,filledZ);var hs=l.getBlockState(hedge);
            h.assertTrue(hs.getBlock() instanceof KingHedgeBlock,"the file's hedge is a hedge that knows who meets it");
            h.assertFalse(hs.getCollisionShape(l,hedge,net.minecraft.world.phys.shapes.CollisionContext.of(p)).isEmpty(),"a hedge is a wall to a thing");
            var verge=b.offset(-16,0,KingSite.rankFar(4)-1);
            h.assertTrue(l.getBlockState(verge).is(KingRegistry.VERGE.get()),"the brook beyond the rank has its verge");
            h.assertFalse(l.getBlockState(b.offset(0,0,KingSite.rankFar(4)-1)).is(KingRegistry.VERGE.get()),"but not over the bridge");
            h.assertTrue(l.getBlockState(verge).getCollisionShape(l,verge,net.minecraft.world.phys.shapes.CollisionContext.of(p)).max(net.minecraft.core.Direction.Axis.Y)>1.25,"a thing cannot jump the verge");
            // A line on ruled land: the hedge and the verge are nothing to it.
            int ruledZ=KingSite.rankNear(1)-4;
            p.teleportTo(l,b.getX()-15.5,b.getY(),b.getZ()+ruledZ+.5,0,0);
            KingBreath.reader(p,b,own);
            h.assertTrue(own.getInt("KingState")==KingBreath.LINE,"ruled land makes a line, got "+own.getInt("KingState"));
            var ruledHedge=b.offset(-9,0,ruledZ);
            h.assertTrue(l.getBlockState(ruledHedge).getCollisionShape(l,ruledHedge,net.minecraft.world.phys.shapes.CollisionContext.of(p)).isEmpty(),"a line goes through a hedge");
            h.assertTrue(l.getBlockState(verge).getCollisionShape(l,verge,net.minecraft.world.phys.shapes.CollisionContext.of(p)).isEmpty(),"and through the verge");
            // Looked at by a reader who is a thing, the line is a sort of thing.
            q.teleportTo(l,b.getX()-15.5,b.getY(),b.getZ()+KingSite.rankNear(3)-4.5,180,0);
            KingBreath.reader(q,b,theirs);
            h.assertTrue(theirs.getInt("KingState")>=KingBreath.SORT,"the watcher stands on filled land");
            q.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,p.getBoundingBox().getCenter());
            KingBreath.reader(p,b,own);
            h.assertTrue(own.getInt("KingState")==KingBreath.SORT,"being looked at makes the line a sort of thing, got "+own.getInt("KingState"));
            // Beside him: too much of a thing on the in-breath, rooted; a thing again on the out-breath.
            q.teleportTo(l,b.getX()+60.5,b.getY(),b.getZ()-4.5,0,0);
            var king=b.offset(KingSite.KING);
            p.teleportTo(l,king.getX()-2.5,b.getY(),king.getZ()+.5,0,0);
            KingBreath.set(l,b,10);
            KingBreath.reader(p,b,own);
            h.assertTrue(own.getInt("KingState")==KingBreath.TOO_MUCH,"beside him on his in-breath a reader is too much of a thing, got "+own.getInt("KingState"));
            var speed=p.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
            h.assertTrue(speed.getValue()<1e-6,"and rooted where they stand");
            KingBreath.set(l,b,KingBreath.IN+10);
            KingBreath.reader(p,b,own);
            h.assertTrue(own.getInt("KingState")==KingBreath.THING&&speed.getValue()>.01,"and free on the out-breath");
            // The in-breath moves the fill's edge: every breathing column shows its in-breath drawing, and back.
            var cells=KingSite.breathing();h.assertTrue(cells.length>40,"the fill's edge has columns to move, "+cells.length);
            KingBreath.set(l,b,10);
            int match=0,moved=0;
            for(long cell:cells){int x=net.minecraft.core.BlockPos.getX(cell),z=net.minecraft.core.BlockPos.getZ(cell);var st=l.getBlockState(b.offset(x,-2,z));
                if(!KingRegistry.isCountry(st))continue;moved++;if(KingRegistry.ruled(st)==KingSite.ruled(x,z,1))match++;}
            h.assertTrue(moved>0&&match==moved,"on the in-breath every moved column is drawn as the in-breath draws it ("+match+"/"+moved+")");
            KingBreath.set(l,b,KingBreath.IN+1);
            match=0;
            for(long cell:cells){int x=net.minecraft.core.BlockPos.getX(cell),z=net.minecraft.core.BlockPos.getZ(cell);var st=l.getBlockState(b.offset(x,-2,z));
                if(KingRegistry.isCountry(st)&&KingRegistry.ruled(st)==KingSite.ruled(x,z,0))match++;}
            h.assertTrue(match==moved,"and back on the out-breath ("+match+"/"+moved+")");
            // Off the country altogether: nowhere, and put back.
            p.teleportTo(l,b.getX()+KingSite.COUNTRY+6.5,b.getY(),b.getZ()-40.5,0,0);
            KingBreath.reader(p,b,own);
            h.assertTrue(KingSite.inside(p.position().subtract(b.getX(),b.getY(),b.getZ())),"a reader who is nowhere is put back in the country");
            KingBreath.leave(p);KingBreath.leave(q);
            done();h.succeed();
        });
    }

    private static void beside(ServerPlayer p,ServerLevel l,BlockPos b){var k=b.offset(KingSite.KING);p.teleportTo(l,k.getX()-2.5,b.getY(),k.getZ()+.5,0,0);p.setDeltaMovement(Vec3.ZERO);}

    /**
     * 0.4.643, the brothers, the rattle and his name. The brothers say what a reader is in his dream and what his name is, and that
     * readies their battle; his stirs draw the country in, a depth to every two; a line's hand goes through the rattle and a thing
     * lifts it, which settles the battle; a reader who has heard the brothers says his whole name beside him, the breathing stops,
     * the country goes to lines, and the reader comes to in the drawing-room with the account ready on the armchair.
     */
    @GameTest(template="empty",batch="king_name",timeoutTicks=1600)
    public static void theBrothersTheRattleAndHisName(GameTestHelper h){
        var f=place(h);var p=f.player("king_namer",P);var q=f.player("king_line",P);var l=f.level(P);var b=f.base(P);KingSleep.forget(b);
        var brothers=b.offset(KingSite.DUM);
        h.startSequence().thenExecute(()->{p.teleportTo(l,brothers.getX()-2.5,b.getY(),brothers.getZ()+.5,90,0);q.teleportTo(l,brothers.getX()-3.5,b.getY(),brothers.getZ()+2.5,90,0);King.ensure(l,b);})
        .thenWaitUntil(()->h.assertTrue(KingBrothers.find(l,b,KingBrothers.DUM)!=null&&KingBrothers.find(l,b,KingBrothers.DEE)!=null&&King.find(l,b)!=null,"both brothers stand in the wood, and the King lies under his tree: "+l.getEntitiesOfClass(LiteraryActor.class,new AABB(brothers).inflate(60)).stream().map(a->a.phase()+"@"+a.blockPosition().subtract(b)).toList()+" king "+King.find(l,b)))
        .thenExecute(()->{
            var dum=KingBrothers.find(l,b,KingBrothers.DUM);var dee=KingBrothers.find(l,b,KingBrothers.DEE);
            h.assertTrue(dum.distanceTo(dee)<1.5,"arm in arm, side by side: "+dum.distanceTo(dee));
            KingBrothers.talk(p,dum,f.own(p,P));KingBrothers.sortOfThing(p);KingBrothers.name(p);
            h.assertTrue(f.own(p,P).getBoolean(KingSleep.HEARD)&&KingBrothers.battle(b)==KingBrothers.ARMED,"once they have said his name, the reader has heard them and there is to be a battle");
            h.assertFalse(f.own(q,P).getBoolean(KingSleep.HEARD),"what one reader hears is their own");
            // His stirs: two to each depth, the country drawn in at once.
            int before=KingSleep.stirs(b);
            KingSleep.stir(l,b,p,f.own(p,P),2-before%2);
            h.assertTrue(KingSleep.level(b)>=KingSleep.LIGHTLY&&KingBreath.shownLevel(b)==KingSleep.level(b),"two stirs and he sleeps more lightly; the land shows it at once ("+KingSleep.stirs(b)+" stirs, level "+KingSleep.level(b)+")");
            // The rattle: a line's hand goes through it, a thing lifts it.
            beside(q,l,b);KingBreath.STATES.put(q.getUUID(),KingBreath.LINE);
            f.click(q,P,KingSite.RATTLE);KingBreath.STATES.put(q.getUUID(),KingBreath.LINE);
            h.assertTrue(KingBlock.is(l.getBlockState(b.offset(KingSite.RATTLE)),KingBlock.Kind.RATTLE)&&!KingBrothers.rattleTaken(b),"a line's hand goes through the rattle");
            KingBreath.STATES.put(p.getUUID(),KingBreath.THING);
            f.click(p,P,KingSite.RATTLE);
            h.assertTrue(!KingBlock.is(l.getBlockState(b.offset(KingSite.RATTLE)),KingBlock.Kind.RATTLE)&&KingBrothers.rattleTaken(b)&&f.own(p,P).getBoolean("KingRattle"),"a thing lifts it");
        }).thenWaitUntil(()->h.assertTrue(KingBrothers.battle(b)==KingBrothers.SETTLED,"with the rattle gone there is nothing to fight about"))
        .thenExecute(()->{
            beside(p,l,b);KingBreath.STATES.put(p.getUUID(),KingBreath.THING);
            King.sayName(p);
            h.assertTrue(KingSleep.waking(b)&&f.own(p,P).getBoolean("KingNamed"),"his whole name, said beside him, and the breathing stops");
        }).thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Ready"),"the waking runs its course"))
        .thenExecute(()->{
            h.assertTrue("said_his_whole_name".equals(f.own(p,P).getString(KingSleep.ENDING)),"the reader who said it said his whole name: "+f.own(p,P).getString(KingSleep.ENDING));
            h.assertTrue(KingSite.inRoom(p.position().subtract(b.getX(),b.getY(),b.getZ())),"and comes to in the drawing-room: "+p.position());
            h.assertTrue(KingBlock.is(l.getBlockState(b.offset(KingSite.RATTLE)),KingBlock.Kind.RATTLE)&&KingSleep.level(b)==KingSleep.DEEP,"a fresh dream: the rattle back, and him deep asleep");
            f.source(p,P);f.read(p,P,LiteraryRooms.ending(P));
            h.assertTrue(WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.SLEEPING_KING),"reading the account on the armchair resolves the story");
            KingBreath.leave(p);KingBreath.leave(q);done();h.succeed();
        });
    }

    /**
     * 0.4.643, the battle and waking him too soon. Left alone, the brothers dress and fight beside him, which stirs him twice and is
     * remembered by the reader who saw it; a great shadow goes over and they run. Six stirs in all wake him too soon: the country
     * goes out, and a reader still in it is sent back through the door with nothing.
     */
    @GameTest(template="empty",batch="king_battle",timeoutTicks=1600)
    public static void theBattleStirsHimAndSixStirsWakeHimTooSoon(GameTestHelper h){
        var f=place(h);var p=f.player("king_battler",P);var l=f.level(P);var b=f.base(P);KingSleep.forget(b);
        var brothers=b.offset(KingSite.DUM);
        h.startSequence().thenExecute(()->{p.teleportTo(l,brothers.getX()-2.5,b.getY(),brothers.getZ()+.5,90,0);King.ensure(l,b);})
        .thenWaitUntil(()->h.assertTrue(KingBrothers.find(l,b,KingBrothers.DUM)!=null&&King.find(l,b)!=null,"the brothers and the King are there"))
        .thenExecute(()->{KingBrothers.name(p);KingBrothers.soon(b,l.getGameTime());})
        .thenWaitUntil(()->h.assertTrue(KingBrothers.battle(b)>=KingBrothers.FIGHT,"they dress, then fight: "+KingBrothers.battle(b)))
        .thenExecute(()->h.assertTrue(KingSleep.stirs(b)>=2&&f.own(p,P).getBoolean("KingSawBattle"),"a battle beside him stirs him twice, and the reader saw it: "+KingSleep.stirs(b)))
        .thenWaitUntil(()->h.assertTrue(KingBrothers.battle(b)==KingBrothers.GONE&&KingBrothers.find(l,b,KingBrothers.DUM)==null,"then the shadow, and they run"))
        .thenExecute(()->{
            beside(p,l,b);KingBreath.STATES.put(p.getUUID(),KingBreath.THING);
            KingSleep.stir(l,b,p,f.own(p,P),KingSleep.WAKE_AT);
            h.assertTrue(KingSleep.waking(b),"six stirs wake him");
        }).thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("KingWokeTooSoon"),"he woke too soon, and the country went out"))
        .thenExecute(()->{
            h.assertFalse(f.own(p,P).getBoolean("Ready"),"nothing is ready for a reader who woke him too soon");
            KingBreath.leave(p);done();h.succeed();
        });
    }

    /**
     * 0.4.643, leaving him his sleep. A reader who has been beside him and heard the brothers, and then walks off the board as a line,
     * leaves him asleep: they come to in the drawing-room with the account ready. Anybody else off the board is only put back.
     */
    @GameTest(template="empty",batch="king_walk",timeoutTicks=800)
    public static void walkingOffLeavesHimHisSleep(GameTestHelper h){
        var f=place(h);var p=f.player("king_leaver",P);var l=f.level(P);var b=f.base(P);KingSleep.forget(b);
        h.startSequence().thenIdle(5).thenExecute(()->{
            var own=f.own(p,P);
            p.teleportTo(l,b.getX()+KingSite.COUNTRY+6.5,b.getY(),b.getZ()-40.5,0,0);
            KingBreath.reader(p,b,own);LiteraryVignettes.save(f.data(),p.getUUID(),P,own);
            h.assertTrue(KingSite.inside(p.position().subtract(b.getX(),b.getY(),b.getZ()))&&!f.own(p,P).getBoolean("Ready"),"a reader who has not met him is only put back");
            own=f.own(p,P);own.putBoolean(KingSleep.MET,true);own.putBoolean(KingSleep.HEARD,true);LiteraryVignettes.save(f.data(),p.getUUID(),P,own);
            p.teleportTo(l,b.getX()+KingSite.COUNTRY+6.5,b.getY(),b.getZ()-40.5,0,0);
            KingBreath.reader(p,b,f.own(p,P));
            h.assertTrue(f.own(p,P).getBoolean("Ready")&&"left_him_his_sleep".equals(f.own(p,P).getString(KingSleep.ENDING)),"one who met him and heard the brothers leaves him his sleep");
            h.assertTrue(KingSite.inRoom(p.position().subtract(b.getX(),b.getY(),b.getZ()))&&!KingSleep.waking(b),"and comes to in the drawing-room while he sleeps on");
            h.assertFalse(King.dealable(f.data(),UUID.randomUUID()),"the House deals the country only from depth twelve");
            KingBreath.leave(p);done();h.succeed();
        });
    }
}
