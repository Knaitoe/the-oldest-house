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
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * 0.4.749, the waxworks (docs/WAXWORKS_0_4_749.md). Each case builds the place itself, one case to a batch, as Not Yet's do.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_waxworks") @PrefixGameTestTemplate(false)
public final class WaxworksTests {
    private WaxworksTests(){}
    private static final LabyrinthPlace P=LabyrinthPlace.WAXWORKS;
    private static LiteraryTests.Fixture active;
    private static LiteraryTests.Fixture place(GameTestHelper h){done();active=new LiteraryTests.Fixture(h);return active;}
    private static void done(){if(active!=null){active.close();active=null;}}
    private static boolean is(ServerLevel l,BlockPos b,int x,int y,int z,net.minecraft.world.level.block.Block block){return l.getBlockState(b.offset(x,y,z)).is(block);}
    private static boolean fitting(ServerLevel l,BlockPos b,BlockPos at,WaxBlock.Kind kind){return WaxBlock.is(l.getBlockState(b.offset(at)),kind);}
    private static int stage(ServerLevel l,BlockPos b,BlockPos at){return l.getBlockState(b.offset(at)).getValue(WaxBlock.STAGE);}
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
    /** Every cell a body can walk to from a start without opening anything, up or down a block at a time, within the shell. */
    private static Set<BlockPos> walked(ServerLevel l,BlockPos b,BlockPos from){
        var seen=new HashSet<BlockPos>();var queue=new ArrayDeque<BlockPos>();seen.add(from);queue.add(from);
        while(!queue.isEmpty()){
            var at=queue.poll();double f=feet(l,b,at);if(Double.isNaN(f))continue;
            for(var d:Direction.Plane.HORIZONTAL)for(int dy=-1;dy<=1;dy++){
                var to=at.relative(d).above(dy);
                if(seen.contains(to)||to.getX()<WaxSite.WEST||to.getX()>WaxSite.EAST||to.getZ()<WaxSite.NORTH||to.getZ()>1||to.getY()<-1||to.getY()>3)continue;
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
     * The staff corridor, the office, the ring and the five galleries, built of their own things: the staff's painted block over
     * linoleum, the public side's panelling and paper over parquet and the runner, brick behind it all. The time clock, the notice
     * and the two lockers; the desk of eight under six lit monitors, the log and the telephone on it; the clock at eight to midnight;
     * a camera in every gallery and one in the ring; the locked front doors under their board; the booth. Each of the twenty-three
     * figures on the plaques stands on its mark over its plaque, the twenty-fourth in its corner with neither, one of each however
     * often they are looked for. A body walks from the House's door into every room, and not past the ropes into the kitchen or the
     * crypt. There is no light in the galleries. Everything answers a hand, every figure included. The notice's count is the
     * museum's own. The House does not deal it.
     */
    @GameTest(template="empty",batch="waxworks_museum",timeoutTicks=1600)
    public static void theMuseumItsGalleriesAndItsFigures(GameTestHelper h){
        var f=place(h);var p=f.player("wax_reader",P);var l=f.level(P);var b=f.base(P);
        h.startSequence().thenIdle(5).thenExecute(()->{
            h.assertTrue(is(l,b,0,-1,-5,WaxRegistry.material("lino"))&&is(l,b,-2,1,-5,WaxRegistry.material("block"))&&is(l,b,0,3,-5,WaxRegistry.material("tile")),"the staff corridor: linoleum, painted block, ceiling tile");
            h.assertTrue(is(l,b,-6,-1,-30,WaxRegistry.material("parquet"))&&is(l,b,-13,1,-26,WaxRegistry.material("panel"))&&is(l,b,-13,3,-26,WaxRegistry.material("paper"))&&is(l,b,-6,4,-30,WaxRegistry.material("plaster")),"a gallery: parquet, panelling, paper over it, plaster ceiling");
            h.assertTrue(is(l,b,0,-1,-20,WaxRegistry.material("runner"))&&is(l,b,WaxSite.WEST,0,-5,WaxRegistry.material("brick")),"the ring's runner, and brick behind everything");
            h.assertTrue(fitting(l,b,WaxSite.NOTICE,WaxBlock.Kind.NOTICE)&&fitting(l,b,WaxSite.TIMECLOCK,WaxBlock.Kind.TIMECLOCK),"the notice by the time clock");
            for(var at:WaxSite.LOCKERS)h.assertTrue(fitting(l,b,at,WaxBlock.Kind.LOCKER)&&fitting(l,b,at.above(),WaxBlock.Kind.LOCKER_TOP)&&stage(l,b,at)==0,"a locker, shut, at "+at.toShortString());
            for(var at:WaxSite.DESK)h.assertTrue(fitting(l,b,at,WaxBlock.Kind.DESK),"the desk at "+at.toShortString());
            h.assertTrue(stage(l,b,WaxSite.LOG)==1&&stage(l,b,WaxSite.PHONE)==2,"the log open on the desk, and the telephone");
            for(var at:WaxSite.MONITORS)h.assertTrue(fitting(l,b,at,WaxBlock.Kind.MONITOR)&&stage(l,b,at)==1&&fitting(l,b,at.below(),WaxBlock.Kind.DESK),"a monitor on the desk, on, at "+at.toShortString());
            h.assertTrue(WaxSite.MONITORS.length==WaxSite.CAMERAS.size(),"a monitor for every camera");
            h.assertTrue(fitting(l,b,WaxSite.CLOCK,WaxBlock.Kind.CLOCK)&&stage(l,b,WaxSite.CLOCK)==0,"the clock at eight minutes to midnight");
            for(var c:WaxSite.CAMERAS)h.assertTrue(fitting(l,b,c.at(),WaxBlock.Kind.CAMERA),"a camera at "+c.at().toShortString());
            for(int i=0;i<WaxSite.GALLERIES.size();i++){var g=WaxSite.GALLERIES.get(i);var c=WaxSite.CAMERAS.get(i).at();h.assertTrue(c.getX()>=g.x0()&&c.getX()<=g.x1()&&c.getZ()>=g.z0()&&c.getZ()<=g.z1(),"gallery "+g.name()+"'s camera is in it");}
            h.assertTrue(fitting(l,b,WaxSite.FRONT_LEFT,WaxBlock.Kind.FRONT)&&fitting(l,b,WaxSite.FRONT_RIGHT.above(),WaxBlock.Kind.FRONT)&&fitting(l,b,WaxSite.FRONT_LEFT.above(2),WaxBlock.Kind.SIGN),"the front doors under their board");
            for(var at:WaxSite.BOOTH)h.assertTrue(fitting(l,b,at,WaxBlock.Kind.BOOTH)&&fitting(l,b,at.above(),WaxBlock.Kind.BOOTH_TOP),"the booth at "+at.toShortString());
            int plaques=0;
            for(var fig:WaxSite.FIGURES){
                if(fig.plinth())h.assertTrue(fitting(l,b,fig.at(),WaxBlock.Kind.PLINTH),"a plinth under "+fig.key());
                if(fig.plaque()!=null){h.assertTrue(fitting(l,b,fig.plaque(),WaxBlock.Kind.PLAQUE),"a plaque for "+fig.key());plaques++;}
            }
            h.assertTrue(plaques==WaxSite.NAMED&&WaxSite.FIGURES.size()==Waxworks.FIGURES&&WaxSite.FIGURES.get(WaxSite.STRANGER).plaque()==null,"twenty-three plaques for twenty-four figures: "+plaques);
            // Walking: every room from the House's door, and not behind the ropes.
            var walk=walked(l,b,new BlockPos(0,0,-1));
            h.assertTrue(walk.contains(new BlockPos(0,0,-14)),"the office, through its door: "+walk.size());
            h.assertTrue(walk.contains(new BlockPos(-15,0,-40))&&walk.contains(new BlockPos(15,0,-40))&&walk.contains(new BlockPos(0,0,-60)),"the ring, all round");
            h.assertTrue(walk.contains(new BlockPos(0,0,-68)),"the foyer");
            h.assertTrue(walk.contains(new BlockPos(-8,0,-30))&&walk.contains(new BlockPos(10,0,-30))&&walk.contains(new BlockPos(-11,0,-49))&&walk.contains(new BlockPos(6,0,-48)),"every gallery");
            h.assertFalse(walk.contains(new BlockPos(6,0,-34))||walk.contains(new BlockPos(-5,0,-46)),"not behind the ropes, into the kitchen's tableau or the crypt: "+walk.stream().filter(c->(c.getX()>=1&&c.getX()<=8&&c.getZ()<=-22&&c.getZ()>=-40)||(c.getX()<=-1&&c.getX()>=-8&&c.getZ()<=-40&&c.getZ()>=-58)).map(BlockPos::toShortString).sorted().limit(6).toList());
            h.assertTrue(l.getBrightness(LightLayer.BLOCK,b.offset(-6,1,-30))==0&&l.getBrightness(LightLayer.BLOCK,b.offset(0,1,-68))==0,"no light in the galleries");
            Waxworks.ensure(l,b);
        }).thenWaitUntil(()->{
            Waxworks.ensure(l,b);
            for(int who=0;who<Waxworks.FIGURES;who++)h.assertTrue(Waxworks.find(l,b,who)!=null,"figure "+who+" is on its mark");
        }).thenExecute(()->{
            Waxworks.ensure(l,b);
            for(int who=0;who<Waxworks.FIGURES;who++){
                var fig=WaxSite.FIGURES.get(who);int phase=Waxworks.FIRST+who;
                var them=l.getEntitiesOfClass(LiteraryActor.class,new AABB(b.offset(fig.at())).inflate(4),a->Waxworks.is(a)&&a.phase()==phase);
                h.assertTrue(them.size()==1,"one of each figure, however often they are looked for: "+fig.key()+" "+them.size());
                var a=them.get(0);var feet=WaxSite.feet(fig).add(b.getX(),b.getY(),b.getZ());
                h.assertTrue(a.position().distanceTo(feet)<.3,fig.key()+" stands on its mark: "+a.position()+" for "+feet);
                h.assertTrue(a.gesture()==fig.pose()&&!a.addresses(),fig.key()+" holds its pose and turns to nobody");
            }
            p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-30.5,90,0);told(p);
        }).thenIdle(40).thenExecute(()->{
            for(int who=0;who<Waxworks.FIGURES;who++){
                var fig=WaxSite.FIGURES.get(who);var a=Waxworks.find(l,b,who);var feet=WaxSite.feet(fig).add(b.getX(),b.getY(),b.getZ());
                h.assertTrue(a!=null&&a.position().distanceTo(feet)<.3,"with a reader among them they stand where they were set: "+fig.key());
            }
            // Everything answers a hand.
            f.click(p,P,WaxSite.TIMECLOCK);f.click(p,P,WaxSite.LOCKERS[0]);f.click(p,P,WaxSite.DESK[0]);f.click(p,P,WaxSite.LOG);f.click(p,P,WaxSite.PHONE);
            f.click(p,P,WaxSite.MONITORS[0]);f.click(p,P,WaxSite.CHAIRS[0]);f.click(p,P,WaxSite.CLOCK);f.click(p,P,WaxSite.CAMERAS.get(1).at());
            f.click(p,P,WaxSite.FRONT_LEFT);f.click(p,P,WaxSite.FRONT_LEFT.above(2));f.click(p,P,WaxSite.BOOTH[0]);
            f.click(p,P,new BlockPos(4,0,-30));f.click(p,P,new BlockPos(1,0,-28));f.click(p,P,new BlockPos(1,0,-31));f.click(p,P,new BlockPos(8,0,-30));
            f.click(p,P,new BlockPos(-3,0,-49));f.click(p,P,new BlockPos(-2,0,-47));f.click(p,P,new BlockPos(2,0,-43));
            f.click(p,P,new BlockPos(2,0,-50));f.click(p,P,new BlockPos(2,0,-49));f.click(p,P,new BlockPos(2,0,-51));f.click(p,P,new BlockPos(2,0,-54));
            f.click(p,P,WaxSite.FIGURES.get(0).at());f.click(p,P,WaxSite.FIGURES.get(0).plaque());
            for(int who=0;who<Waxworks.FIGURES;who++)Waxworks.talk(p,Waxworks.find(l,b,who),f.own(p,P));
        }).thenIdle(2).thenExecute(()->{
            var why=told(p);
            for(String line:List.of(WaxTexts.TIMECLOCK,WaxTexts.LOCKER,WaxTexts.DESK,WaxTexts.DESK_LOG,WaxTexts.DESK_PHONE,WaxTexts.MONITOR,WaxTexts.CHAIR,WaxTexts.CLOCK,
                WaxTexts.CAMERA,WaxTexts.FRONT,WaxTexts.SIGN,WaxTexts.BOOTH,WaxTexts.TABLE,WaxTexts.DRESSER,WaxTexts.STOVE,WaxTexts.ROPE,WaxTexts.COFFIN,WaxTexts.GRAVESTONE,
                WaxTexts.VAT,WaxTexts.BENCH,WaxTexts.BENCH_HEAD,WaxTexts.BENCH_HANDS,WaxTexts.HEADS,WaxTexts.PLINTH,WaxTexts.plaque(0)))
                h.assertTrue(why.contains(line),"each says what it is: missing \""+line+"\" in "+why);
            for(int who=0;who<Waxworks.FIGURES;who++)h.assertTrue(why.contains(WaxTexts.touch(who)),"figure "+who+" answers a hand");
            // The notice: the museum's own count.
            var paper=LiteraryTexts.source(P).get(DataComponents.WRITTEN_BOOK_CONTENT);String text=String.join(" ",paper.pages().stream().map(pg->pg.raw().getString()).toList());
            h.assertTrue(text.contains("There are twenty-three")&&WaxSite.NAMED==23,"the notice counts the figures on the plaques");
            h.assertTrue(text.contains("all five rooms")&&WaxSite.GALLERIES.size()==5,"and the five rooms of the round");
            for(String word:List.of(" he "," she "," his "," her "," him "))h.assertFalse(text.toLowerCase(java.util.Locale.ROOT).contains(word),"the notice says nothing of who has the night: "+word);
            h.assertTrue(LiteraryTexts.accountTitle(P).length()<=32,"the account's title fits a book: "+LiteraryTexts.accountTitle(P));
            h.assertFalse(LiteraryVignettes.canDeal(f.data(),p.getUUID(),P),"the House does not deal it yet");
            done();h.succeed();
        });
    }

    /**
     * Two readers on the shift and a spectator: each reader's hand is their own (what one touches the other has not), the figures are
     * made once for both, and a spectator in the office reads nothing and holds nothing.
     */
    @GameTest(template="empty",batch="waxworks_two",timeoutTicks=1200)
    public static void twoReadersAndASpectator(GameTestHelper h){
        var f=place(h);var p=f.player("wax_one",P);var q=f.player("wax_two",P);var l=f.level(P);var b=f.base(P);
        h.startSequence().thenIdle(5).thenExecute(()->{
            Waxworks.ensure(l,b);
        }).thenWaitUntil(()->{
            Waxworks.ensure(l,b);
            for(int who=0;who<Waxworks.FIGURES;who++)h.assertTrue(Waxworks.find(l,b,who)!=null,"figure "+who+" is on its mark");
        }).thenExecute(()->{
            Waxworks.arrive(p,b,f.own(p,P));Waxworks.arrive(q,b,f.own(q,P));
            int all=l.getEntitiesOfClass(LiteraryActor.class,new AABB(b).inflate(96),Waxworks::is).size();
            h.assertTrue(all==Waxworks.FIGURES,"two readers in, and still one of each figure: "+all);
            var mine=f.own(p,P);Waxworks.talk(p,Waxworks.find(l,b,WaxSite.MIRROR),mine);LiteraryVignettes.save(f.data(),p.getUUID(),P,mine);
            h.assertTrue((f.own(p,P).getInt("Touched")&1<<WaxSite.MIRROR)!=0&&f.own(q,P).getInt("Touched")==0,"what one reader touched is theirs alone");
            var s=NativeTestPlayers.survival(h,"wax_watch");f.players.add(s);s.setGameMode(GameType.SPECTATOR);
            s.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-14.5,180,0);
            try{f.click(s,P,LiteraryRooms.source(P));}catch(RuntimeException ignored){/* a spectator's click may find nothing */}
            h.assertFalse(f.own(s,P).getBoolean("Read_Source"),"a spectator at the notice reads nothing");
            h.assertFalse(LiteraryVignettes.inside(s,P),"a spectator is not on the shift");
            done();h.succeed();
        });
    }
}
