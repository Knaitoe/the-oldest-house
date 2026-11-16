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
            h.assertTrue(is(l,b,-17,-1,third,KingSite.light(3,1)?"turf_light":"turf_dark")&&is(l,b,-1,-1,third,"path")&&is(l,b,16,-1,third-8,KingSite.light(3,3)?"turf_light":"turf_dark"),"the third rank filled: its squares and the road (the third file's beyond the carriage)");
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
            // Both in the middle file, clear of the Queen's hill (0.4.644), which would stand between them in the first.
            p.teleportTo(l,b.getX()-4.5,b.getY(),b.getZ()+ruledZ+.5,0,0);KingBreath.reader(p,b,own);
            h.assertTrue(own.getInt("KingState")==KingBreath.LINE,"still a line in the middle file, got "+own.getInt("KingState"));
            q.teleportTo(l,b.getX()-4.5,b.getY(),b.getZ()+KingSite.rankNear(3)-4.5,180,0);
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

    /**
     * 0.4.644, the hill and the glass table. The Queen takes the hand of the first reader to come up to her and runs on the spot with
     * them, rooted and a thing, then lets go and walks to the far side of the field, where she looks back at the peg; standing on it,
     * the reader is looked at and counted. Only a thing lifts the bottle and the cake, one of each to a reader; the bottle makes them a
     * step less and the cake a step more, too much of a thing and rooted on a thing; leaving the country takes both away.
     */
    @GameTest(template="empty",batch="king_hill",timeoutTicks=1600)
    public static void theQueenThePegAndTheGlassTable(GameTestHelper h){
        var f=place(h);var p=f.player("king_pawn",P);var q=f.player("king_pencil",P);var l=f.level(P);var b=f.base(P);KingSleep.forget(b);
        var queen=b.offset(KingSite.QUEEN);
        h.startSequence().thenExecute(()->{q.teleportTo(l,b.getX()+60.5,b.getY(),b.getZ()-4.5,0,0);p.teleportTo(l,queen.getX()+.5,b.getY(),queen.getZ()+2.5,180,0);})
        .thenWaitUntil(()->h.assertTrue(KingQueen.find(l,b)!=null&&KingQueen.state(b)==KingQueen.RUNNING,"she takes the hand of the reader who comes up to her: "+KingQueen.state(b)))
        .thenExecute(()->{
            h.assertTrue(KingQueen.holding(p,b),"she has hold of that reader");
            var own=f.own(p,P);KingBreath.reader(p,b,own);
            h.assertTrue(own.getInt("KingState")>=KingBreath.THING&&p.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED).getValue()<1e-6,"held, they are a thing and get nowhere: "+own.getInt("KingState"));
        })
        .thenWaitUntil(()->h.assertTrue(KingQueen.state(b)==KingQueen.LOOKING&&f.own(p,P).getBoolean("KingQueenRan"),"she lets go and walks to the far side, and the reader has run with her: "+KingQueen.state(b)))
        .thenExecute(()->{
            var peg=b.offset(KingSite.peg(2));p.teleportTo(l,peg.getX()+.5,b.getY(),peg.getZ()+.5,0,0);
            h.assertTrue(KingQueen.onPeg(p,b,2),"standing on the peg");
        })
        .thenWaitUntil(()->h.assertTrue((f.own(p,P).getInt(KingSleep.COUNTED)&1<<2)!=0,"looked at from the far side, they are counted on the square"))
        .thenExecute(()->{
            // The glass table: a line's hand goes through; a thing lifts the bottle and the cake, once.
            KingBreath.STATES.put(q.getUUID(),KingBreath.LINE);f.click(q,P,KingSite.GLASS_TABLE);
            h.assertFalse(q.getInventory().items.stream().anyMatch(KingDraughtItem::is),"a line lifts nothing from the table");
            KingBreath.STATES.put(p.getUUID(),KingBreath.THING);f.click(p,P,KingSite.GLASS_TABLE);f.click(p,P,KingSite.GLASS_TABLE);
            long n=p.getInventory().items.stream().filter(KingDraughtItem::is).count();
            h.assertTrue(n==2,"a thing takes a bottle and a cake, and only once: "+n);
            // Drinking and eating, on filled land far from him.
            p.teleportTo(l,b.getX()-16.5,b.getY(),b.getZ()+KingSite.rankNear(4)-4.5,0,0);
            var own=f.own(p,P);KingBreath.set(l,b,KingBreath.IN+10);KingBreath.reader(p,b,own);
            h.assertTrue(own.getInt("KingState")==KingBreath.THING,"on filled land, a thing: "+own.getInt("KingState"));
            KingBreath.swallow(p,false,l.getGameTime()+KingDraughtItem.LASTS);KingBreath.reader(p,b,own);
            h.assertTrue(own.getInt("KingState")==KingBreath.SORT,"a swallow from the bottle, a step less: "+own.getInt("KingState"));
            KingBreath.swallow(p,true,l.getGameTime()+KingDraughtItem.LASTS);KingBreath.reader(p,b,own);
            var speed=p.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
            h.assertTrue(own.getInt("KingState")==KingBreath.TOO_MUCH&&speed.getValue()<1e-6,"a bite of the cake on a thing, too much of one and rooted: "+own.getInt("KingState"));
            KingBreath.leave(p);KingBreath.leave(q);
            h.assertFalse(p.getInventory().items.stream().anyMatch(KingDraughtItem::is),"leaving the country takes the bottle and the cake");
            done();h.succeed();
        });
    }

    /**
     * 0.4.644, the railway. A reader who sits down in the carriage is looked at by the Guard through three glasses; under his glass a
     * thing keeps its seat (getting up puts them back in it); after the third the train takes the brook at a jump and puts them on the
     * road at the near edge of the fourth square.
     */
    @GameTest(template="empty",batch="king_train",timeoutTicks=1600)
    public static void theGuardAndTheTrain(GameTestHelper h){
        var f=place(h);var p=f.player("king_passenger",P);var l=f.level(P);var b=f.base(P);KingSleep.forget(b);
        var seat=b.offset(KingSite.SEATS[0]);
        h.startSequence().thenExecute(()->p.teleportTo(l,seat.getX()+1.5,b.getY(),seat.getZ()+.5,90,0))
        .thenWaitUntil(()->h.assertTrue(KingGuard.find(l,b)!=null,"the Guard is in the carriage"))
        .thenExecute(()->{h.assertTrue(io.github.knaitoe.theoldesthouse.house.HouseSitting.sit(p,seat),"a carriage seat is sat in");})
        .thenWaitUntil(()->h.assertTrue(p.getUUID().equals(KingGuard.rider(b))&&KingGuard.holding(p,b),"he looks at the one who sat down"))
        .thenExecute(()->p.stopRiding())
        .thenWaitUntil(()->h.assertTrue(p.isPassenger()||!p.getUUID().equals(KingGuard.rider(b)),"under his glass, they are back in the seat"))
        .thenExecute(()->h.assertTrue(p.isPassenger(),"under his glass, a thing keeps its seat"))
        .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("KingRode"),"the ride runs its course"))
        .thenExecute(()->{
            var r=p.position().subtract(b.getX(),b.getY(),b.getZ());
            h.assertTrue(r.distanceTo(KingSite.RIDE_OUT)<1.5&&!p.isPassenger(),"put down on the road beyond the brook: "+r);
            KingBreath.leave(p);done();h.succeed();
        });
    }

    /** A hand on the mushroom's cap from where the reader stands (the fixture's click would move them in front of it). */
    private static void capClick(ServerPlayer p,BlockPos at){
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock(p,net.minecraft.world.InteractionHand.MAIN_HAND,at,new net.minecraft.world.phys.BlockHitResult(at.getCenter(),net.minecraft.core.Direction.UP,at,false)));
    }

    /**
     * 0.4.645, "Who are you?". The Caterpillar sits on the mushroom's cap and asks; the answers are what the reader is, with words gone
     * from them on the out-breath. A piece from the west side of the cap fills (or gives) a cake, from the east side a bottle. The
     * bridge past the fifth square is only drawn: a thing goes through it and is set back on the bank; less than a thing walks over.
     */
    @GameTest(template="empty",batch="king_mushroom",timeoutTicks=1600)
    public static void theCaterpillarTheMushroomAndTheDrawnBridge(GameTestHelper h){
        var f=place(h);var p=f.player("king_who",P);var l=f.level(P);var b=f.base(P);KingSleep.forget(b);
        var cap=KingSite.MUSHROOM.above();var m=b.offset(KingSite.MUSHROOM);
        h.startSequence().thenExecute(()->p.teleportTo(l,m.getX()-1.5,b.getY(),m.getZ()+.5,-90,0))
        .thenWaitUntil(()->h.assertTrue(KingCaterpillar.find(l,b)!=null,"the Caterpillar is on the mushroom"))
        .thenExecute(()->{
            var c=KingCaterpillar.find(l,b);
            h.assertTrue(c.getY()>m.getY()+1.8&&io.github.knaitoe.theoldesthouse.labyrinth.ActorSeat.of(c)!=null,"it sits on the cap: "+c.position());
            // "Who are you?": what the reader is, whole on the in-breath and with words gone on the out-breath.
            h.assertTrue(KingCaterpillar.answer(KingBreath.THING,false).equals(KingTexts.R_A_THING),"a thing says it is a thing");
            var dropped=KingCaterpillar.answer(KingBreath.THING,true);
            h.assertTrue(dropped.contains("...")&&dropped.startsWith("A")&&!dropped.equals(KingTexts.R_A_THING),"on the out-breath words drop out: "+dropped);
            KingCaterpillar.talk(p,c,f.own(p,P));KingCaterpillar.you(p);
            h.assertTrue(f.own(p,P).getBoolean("KingAsked"),"asked, and answered");
            // The mushroom: the west side more (a cake), the east side less (a bottle); going back fills them again.
            capClick(p,b.offset(cap));
            var cake=p.getInventory().items.stream().filter(KingDraughtItem::is).toList();
            h.assertTrue(cake.size()==1&&((KingDraughtItem)cake.get(0).getItem()).more(),"from the west side, a cake");
            cake.get(0).setDamageValue(2);capClick(p,b.offset(cap));
            var again=p.getInventory().items.stream().filter(KingDraughtItem::is).toList();
            h.assertTrue(again.size()==1&&again.get(0).getDamageValue()==0,"going back fills the cake again, and gives no second");
            p.teleportTo(l,m.getX()+2.5,b.getY(),m.getZ()+.5,90,0);capClick(p,b.offset(cap));
            h.assertTrue(p.getInventory().items.stream().anyMatch(i->KingDraughtItem.is(i)&&!((KingDraughtItem)i.getItem()).more()),"from the east side, a bottle");
            // The drawn bridge: no floor to a thing, a floor to anything less.
            var drawn=b.offset(-1,-1,KingSite.rankFar(5)-1);
            h.assertTrue(l.getBlockState(drawn).is(KingRegistry.DRAWN_BRIDGE.get()),"the road crosses the brook on a drawn bridge");
            KingBreath.STATES.put(p.getUUID(),KingBreath.THING);
            h.assertTrue(l.getBlockState(drawn).getCollisionShape(l,drawn,net.minecraft.world.phys.shapes.CollisionContext.of(p)).isEmpty(),"a drawing will not bear a thing");
            KingBreath.STATES.put(p.getUUID(),KingBreath.SORT);
            h.assertFalse(l.getBlockState(drawn).getCollisionShape(l,drawn,net.minecraft.world.phys.shapes.CollisionContext.of(p)).isEmpty(),"it bears a sort of thing");
            // A thing that went through is in the brook, and is set back on the near bank.
            var own=f.own(p,P);
            p.teleportTo(l,b.getX()-.5,b.getY()-1,b.getZ()+KingSite.rankFar(5)-1.5,180,0);KingBreath.reader(p,b,own);
            var at=p.position().subtract(b.getX(),b.getY(),b.getZ());
            h.assertTrue(own.getBoolean("KingFellThrough")&&at.distanceTo(KingSite.BANK)<1,"set back on the bank: "+at);
            // Less than a thing (a swallow from the bottle), standing on it: over as a drawing.
            KingBreath.swallow(p,false,l.getGameTime()+KingDraughtItem.LASTS);
            p.teleportTo(l,b.getX()-.5,b.getY(),b.getZ()+KingSite.rankFar(5)-1.5,180,0);p.setOnGround(true);KingBreath.reader(p,b,own);
            h.assertTrue(own.getInt("KingState")<KingBreath.THING&&own.getBoolean("KingDrawnCrossed"),"over the drawn bridge as less than a thing: "+own.getInt("KingState"));
            KingBreath.leave(p);done();h.succeed();
        });
    }

    /**
     * 0.4.646, the court of cards. The herald names a reader as they come on to the sixth square, and the court holds them a thing; the
     * pack's guard bears a thing and lets a sort of thing through edge-on; a held reader who gets past anyway is set back in the dock.
     * The three ways out, one reader each: the bottle (a step under what the court holds them to), the Cat (the court looks at it
     * instead), and the cards (too much of a thing says what the court is, and the pack comes down, and the King stirs). A reader who
     * comes after finds the court a pack of cards on the grass. A fresh dream stands the pack up again.
     */
    @GameTest(template="empty",batch="king_court",timeoutTicks=1800)
    public static void theCourtOfCardsAndTheCat(GameTestHelper h){
        var f=place(h);var p=f.player("king_court_bottle",P);var q=f.player("king_court_cat",P);var r=f.player("king_court_cards",P);var t=f.player("king_court_late",P);
        var l=f.level(P);var b=f.base(P);KingSleep.forget(b);
        // One record a reader, carried from step to step as a tick carries it (f.own hands out a fresh copy each time).
        var owns=new java.util.HashMap<java.util.UUID,net.minecraft.nbt.CompoundTag>();
        java.util.function.Function<ServerPlayer,net.minecraft.nbt.CompoundTag> mine=w->owns.computeIfAbsent(w.getUUID(),u->f.own(w,P));
        java.util.function.BiConsumer<ServerPlayer,double[]> at=(who,xz)->{who.teleportTo(l,b.getX()+xz[0],b.getY(),b.getZ()+xz[1],180,0);who.setOnGround(true);
            // Twice, as two ticks: what the court does on a step (the summons) counts in the state from the next.
            KingBreath.reader(who,b,mine.apply(who));KingBreath.reader(who,b,mine.apply(who));};
        double[] onSquare={.5,-121.5},past={.5,-134.6},far={-30.5,-60.5};
        h.startSequence().thenExecute(()->{for(var who:new ServerPlayer[]{p,q,r,t})at.accept(who,far);})
        .thenWaitUntil(()->{for(var who:new ServerPlayer[]{p,q,r,t})KingBreath.reader(who,b,mine.apply(who));
            h.assertTrue(KingCourt.find(l,b,KingCourt.KING)!=null&&KingCourt.find(l,b,KingCourt.QUEEN)!=null&&KingCourt.find(l,b,KingCourt.HERALD)!=null&&KingCourt.find(l,b,KingCourt.CAT)!=null,"the court, the herald and the Cat are in their places");})
        .thenExecute(()->{
            // The summons: on to the square, named, and a thing while the court attends.
            at.accept(p,onSquare);var own=mine.apply(p);
            h.assertTrue(own.getBoolean("KingSummoned")&&KingCourt.holding(p,b,own)&&own.getInt("KingState")>=KingBreath.THING,"named by the herald and held a thing: "+own.getInt("KingState")+" summoned "+own.getBoolean("KingSummoned")+" held "+KingCourt.holding(p,b,own)+" at "+p.position().subtract(b.getX(),b.getY(),b.getZ())+" rank "+KingSite.rank((int)Math.floor(p.getZ()-b.getZ())));
            // The guard: a wall to a thing, nothing to a sort of thing.
            var guard=b.offset(0,0,KingSite.GUARD_ROW);
            h.assertTrue(KingCardBlock.standing(l.getBlockState(guard)),"the pack's guard stands across the road");
            KingBreath.STATES.put(p.getUUID(),KingBreath.THING);
            h.assertFalse(l.getBlockState(guard).getCollisionShape(l,guard,net.minecraft.world.phys.shapes.CollisionContext.of(p)).isEmpty(),"a thing cannot get between the guard");
            KingBreath.STATES.put(p.getUUID(),KingBreath.SORT);
            h.assertTrue(l.getBlockState(guard).getCollisionShape(l,guard,net.minecraft.world.phys.shapes.CollisionContext.of(p)).isEmpty(),"a sort of thing goes between them edge-on");
            // Past the guard by no way at all: set back in the dock.
            at.accept(p,past);
            var rel=p.position().subtract(b.getX(),b.getY(),b.getZ());
            h.assertTrue(rel.distanceTo(KingSite.DOCK)<1&&own.getInt("KingCourtWay")==0,"the court has not finished with them: "+rel);
            // The bottle: a step under what the court holds them to, and out.
            KingBreath.swallow(p,false,l.getGameTime()+KingDraughtItem.LASTS);
            at.accept(p,new double[]{KingSite.DOCK.x,KingSite.DOCK.z});
            h.assertTrue(own.getInt("KingState")==KingBreath.SORT,"held a thing, a swallow makes them a sort of thing: "+own.getInt("KingState"));
            at.accept(p,past);
            h.assertTrue(own.getInt("KingCourtWay")==KingCourt.BY_BOTTLE&&!KingCourt.holding(p,b,own),"out by the bottle: "+own.getInt("KingCourtWay"));
            KingBreath.leave(p);
            // The Cat: called, it goes over the court, and nobody is held while they look at it.
            at.accept(q,onSquare);
            h.assertTrue(KingCourt.holding(q,b,mine.apply(q)),"the second reader is named and held");
            KingCourt.call(q);
            h.assertTrue(KingCourt.attendingToCat(l,b)&&!KingCourt.holding(q,b,mine.apply(q)),"the court looks at the Cat and holds nobody");
        })
        .thenWaitUntil(()->{KingBreath.reader(q,b,mine.apply(q));var cat=KingCourt.find(l,b,KingCourt.CAT);
            h.assertTrue(cat!=null&&cat.position().subtract(b.getX(),b.getY(),b.getZ()).distanceTo(KingSite.CAT_OVER)<.5,"the Cat's head is over the court");
            var queen=KingCourt.find(l,b,KingCourt.QUEEN);h.assertTrue(queen!=null&&queen.gesture()==KingCourt.LOOKING_UP,"the court looks up at it");})
        .thenExecute(()->{
            at.accept(q,past);
            h.assertTrue(mine.apply(q).getInt("KingCourtWay")==KingCourt.BY_CAT,"out while they argued over the Cat: "+mine.apply(q).getInt("KingCourtWay"));
            KingBreath.leave(q);KingCourt.forgetCat(b);
            // The cards: too much of a thing (the cake) says what the court is, the pack comes down, and the King stirs once.
            at.accept(r,onSquare);var own=mine.apply(r);
            KingBreath.swallow(r,true,l.getGameTime()+KingDraughtItem.LASTS);at.accept(r,new double[]{KingSite.DOCK.x,KingSite.DOCK.z});
            h.assertTrue(own.getInt("KingState")==KingBreath.TOO_MUCH,"held a thing, a bite makes them too much of one: "+own.getInt("KingState"));
            int stirs=KingSleep.stirs(b);
            KingCourt.pack(r);
            var guard=b.offset(0,0,KingSite.GUARD_ROW);
            h.assertTrue(KingCardBlock.is(l.getBlockState(guard))&&!KingCardBlock.standing(l.getBlockState(guard)),"the guard lies flat in the grass");
            h.assertTrue(KingCourt.find(l,b,KingCourt.KING)==null&&KingCourt.find(l,b,KingCourt.QUEEN)==null&&KingCourt.find(l,b,KingCourt.HERALD)==null,"the court has come down as cards");
            var king=l.getBlockState(b.offset(KingSite.COURT_KING));
            h.assertTrue(KingCardBlock.is(king)&&king.getValue(KingCardBlock.FACE)==KingCardBlock.KING&&king.getValue(KingCardBlock.FALLEN),"the King of Hearts is a card where he stood");
            h.assertTrue(KingSleep.stirs(b)==stirs+1,"the King stirred once, a long way off: "+KingSleep.stirs(b));
            KingBreath.swallow(r,false,l.getGameTime());at.accept(r,past);
            h.assertTrue(own.getInt("KingCourtWay")==KingCourt.BY_CARDS,"out by saying what the court was: "+own.getInt("KingCourtWay"));
            // A reader who comes after is named by nobody, and finds a pack of cards on the grass.
            at.accept(t,onSquare);
            h.assertFalse(mine.apply(t).getBoolean("KingSummoned"),"no herald is left to name the late reader");
            at.accept(t,past);
            h.assertTrue(mine.apply(t).getInt("KingCourtWay")==KingCourt.ALREADY_CARDS,"the late reader found the cards on the grass");
            // The accounts say which way each went.
            h.assertTrue(pages(KingTexts.account("p",mine.apply(p))).contains("edge-on")&&pages(KingTexts.account("r",own)).contains("pack of cards"),"the accounts say which way each left the court");
            // A fresh dream stands the pack up again (on its first tick).
            KingSleep.forget(b);
        })
        .thenWaitUntil(()->{KingBreath.reader(r,b,mine.apply(r));
            h.assertTrue(KingCardBlock.standing(l.getBlockState(b.offset(0,0,KingSite.GUARD_ROW))),"a fresh dream stands the pack up again");})
        .thenExecute(()->{for(var who:new ServerPlayer[]{r,t})KingBreath.leave(who);done();h.succeed();});
    }
    /**
     * 0.4.647, the forest on the seventh square. The Knight waits on his horse by the peg, a drawing until he is looked at; looked at,
     * he fills in and looks back, and that look counts. On the peg a reader he looks at is a sort of thing, and a bite of cake makes
     * them a thing there, and counted. Let alone, he thins and falls off his horse into the path, and lies there until somebody looks
     * at him long enough to get him up. He sings if asked, and a reader who has heard him sings it afterwards, crouching still; he keeps
     * a cake for a reader who has none. At the last brook he asks to be watched to the turn: watched the whole way he goes round it
     * filled in; let go, he is a drawing before he gets there. The accounts say which.
     */
    @GameTest(template="empty",batch="king_forest",timeoutTicks=2400)
    public static void theKnightTheForestAndTheTurn(GameTestHelper h){
        var f=place(h);var p=f.player("king_forest_rider",P);var q=f.player("king_forest_walker",P);
        var l=f.level(P);var b=f.base(P);KingSleep.forget(b);
        var owns=new java.util.HashMap<java.util.UUID,net.minecraft.nbt.CompoundTag>();
        java.util.function.Function<ServerPlayer,net.minecraft.nbt.CompoundTag> mine=w->owns.computeIfAbsent(w.getUUID(),u->f.own(w,P));
        java.util.function.BiConsumer<ServerPlayer,double[]> at=(who,xz)->{who.teleportTo(l,b.getX()+xz[0],b.getY(),b.getZ()+xz[1],180,0);who.setOnGround(true);};
        Runnable tick=()->{for(var who:new ServerPlayer[]{p,q})KingBreath.reader(who,b,mine.apply(who));};
        java.util.function.BiConsumer<ServerPlayer,LiteraryActor> look=(who,k)->who.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,k.getBoundingBox().getCenter());
        java.util.function.Consumer<ServerPlayer> away=who->who.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,new net.minecraft.world.phys.Vec3(b.getX()-60,who.getEyeY(),who.getZ()));
        var peg=KingSite.peg(7);double[] onPeg={peg.getX()+.5,peg.getZ()+.5},far={-30.5,-60.5},brook={.5,KingSite.TURN_Z+.2};
        h.startSequence().thenExecute(()->{at.accept(q,far);at.accept(p,onPeg);away.accept(p);})
        .thenWaitUntil(()->{tick.run();h.assertTrue(KingForest.find(l,b)!=null,"the Knight is waiting on the seventh square");})
        .thenExecute(()->{
            var k=KingForest.find(l,b);
            h.assertTrue(!KingForest.filled(k)&&!KingForest.fallen(k),"nobody looking: a drawing, on his horse");
            // Looked at, he fills in and looks back; on the peg that makes a line a sort of thing, which the peg does not count here.
            look.accept(p,k);tick.run();
        })
        .thenWaitUntil(()->{var k=KingForest.find(l,b);look.accept(p,k);tick.run();
            h.assertTrue(KingForest.filled(k)&&KingBreath.lookedAtByTheDream(p,b),"looked at, he fills in and looks back: filled "+KingForest.filled(k));})
        .thenExecute(()->{
            var own=mine.apply(p);var k=KingForest.find(l,b);look.accept(p,k);tick.run();
            h.assertTrue(own.getInt("KingState")==KingBreath.SORT&&(own.getInt(KingSleep.COUNTED)&1<<7)==0,"a line he looks at is a sort of thing, and not counted: "+own.getInt("KingState"));
            // A bite of cake does the rest.
            KingBreath.swallow(p,true,l.getGameTime()+KingDraughtItem.LASTS);look.accept(p,k);tick.run();
            h.assertTrue(own.getInt("KingState")==KingBreath.THING&&(own.getInt(KingSleep.COUNTED)&1<<7)!=0,"looked at and fed, a thing, and counted: "+own.getInt("KingState"));
            KingBreath.swallow(p,true,l.getGameTime());away.accept(p);
        })
        // Let alone, he thins and comes off his horse.
        .thenWaitUntil(()->{away.accept(p);tick.run();var k=KingForest.find(l,b);h.assertTrue(k!=null&&KingForest.fallen(k)&&!KingForest.filled(k),"left unwatched, he falls off as a drawing");})
        .thenExecute(()->{tick.run();h.assertTrue(mine.apply(p).getBoolean(KingForest.FELL)&&KingForest.falls(b)==1,"the reader saw him fall: "+KingForest.falls(b));})
        // Looked at long enough, he gets up.
        .thenWaitUntil(()->{var k=KingForest.find(l,b);look.accept(p,k);tick.run();h.assertTrue(!KingForest.fallen(k),"looked at long enough, he gets up");})
        .thenExecute(()->{
            tick.run();h.assertTrue(mine.apply(p).getBoolean(KingForest.RAISED),"the reader got him up");
            // His song, and a cake from his saddle.
            KingForest.sing(p);
            h.assertTrue(f.own(p,P).getBoolean(KingForest.SONG),"asked, he sings, and the reader has it");
            p.getInventory().items.removeIf(KingDraughtItem::is);
            KingForest.cake(p);
            h.assertTrue(p.getInventory().items.stream().anyMatch(st->st.getItem() instanceof KingDraughtItem d&&d.more())&&f.own(p,P).getBoolean(KingForest.KNIGHT_CAKE),"he gives a cake to a reader who has none");
            // Crouching still, a reader who has heard it sings it; beside the King, it settles him by four stirs.
            KingSleep.stir(l,b,null,null,2);
            h.assertTrue(KingSleep.stirs(b)==2&&KingSleep.level(b)==1,"stirred twice, he sleeps a depth lighter");
            at.accept(p,new double[]{KingSite.KING.getX()+1.5,KingSite.KING.getZ()+.5});
            var own=mine.apply(p);own.putBoolean(KingForest.SONG,true);
            p.setShiftKeyDown(true);p.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
            for(int i=0;i<12;i++)KingBreath.reader(p,b,own);
            p.setShiftKeyDown(false);
            h.assertTrue(own.getBoolean("KingSang")&&own.getBoolean(KingForest.SANG_TO_HIM),"crouching still beside him, the reader sings his song");
            h.assertTrue(KingSleep.stirs(b)==0&&KingSleep.level(b)==0,"the song settles him: "+KingSleep.stirs(b));
            // On to the last brook, keeping an eye on him.
            at.accept(p,brook);
        })
        .thenWaitUntil(()->{var k=KingForest.find(l,b);look.accept(p,k);tick.run();h.assertTrue(KingForest.asked(b),"at the last brook he asks to be watched to the turn");})
        .thenWaitUntil(()->{var k=KingForest.find(l,b);if(k!=null)look.accept(p,k);tick.run();h.assertTrue(KingForest.gone(b),"watched, he rides out round the turn");})
        .thenExecute(()->{
            tick.run();var own=mine.apply(p);
            h.assertTrue(own.getInt(KingForest.TURN)==KingForest.WATCHED&&KingForest.find(l,b)==null,"the reader watched him round the turn: "+own.getInt(KingForest.TURN));
            h.assertTrue(pages(KingTexts.account("p",own)).contains("went round it filled in"),"the account says so");
            // A fresh dream: the second reader goes on without watching him.
            KingBreath.leave(p);at.accept(p,far);KingSleep.forget(b);at.accept(q,onPeg);
        })
        .thenWaitUntil(()->{tick.run();var k=KingForest.find(l,b);h.assertTrue(k!=null,"a fresh dream has him back at the peg");})
        .thenExecute(()->{var k=KingForest.find(l,b);k.moveTo(b.getX()+KingSite.KNIGHT_LANE,b.getY(),b.getZ()+KingSite.TURN_Z+3,180,0);at.accept(q,brook);})
        .thenWaitUntil(()->{var k=KingForest.find(l,b);look.accept(q,k);tick.run();h.assertTrue(KingForest.asked(b),"he asks the second reader");})
        .thenWaitUntil(()->{away.accept(q);tick.run();h.assertTrue(KingForest.gone(b),"let go, he rides out anyway");})
        .thenExecute(()->{
            tick.run();var own=mine.apply(q);
            h.assertTrue(own.getInt(KingForest.TURN)==KingForest.WENT_ON,"the second reader went on: "+own.getInt(KingForest.TURN));
            h.assertTrue(pages(KingTexts.account("q",own)).contains("a drawing before he got there"),"the account says he was a drawing before the turn");
            for(var who:new ServerPlayer[]{p,q})KingBreath.leave(who);done();h.succeed();
        });
    }
    private static String pages(net.minecraft.world.item.ItemStack book){
        var c=book.get(net.minecraft.core.component.DataComponents.WRITTEN_BOOK_CONTENT);if(c==null)return "";
        var out=new StringBuilder();for(var page:c.pages())out.append(page.raw().getString()).append(' ');return out.toString();
    }
}
