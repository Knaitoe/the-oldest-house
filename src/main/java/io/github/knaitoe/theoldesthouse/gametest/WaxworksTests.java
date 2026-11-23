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
 * 0.4.749 and 0.4.750, the waxworks (docs/WAXWORKS_0_4_749.md, docs/WAXWORKS_0_4_750.md). Each case builds the place itself, one case
 * to a batch, as Not Yet's do.
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

    /** Stand a reader at a place relative to the museum, looking at a point, turned off it by so many degrees. */
    private static void aim(ServerPlayer p,ServerLevel l,BlockPos b,double x,double z,net.minecraft.world.phys.Vec3 at,float off){
        double ex=b.getX()+x,ey=b.getY()+p.getEyeHeight(),ez=b.getZ()+z;var to=at.subtract(ex,ey,ez);
        float yaw=(float)(Math.toDegrees(Math.atan2(to.z,to.x))-90)+off;float pitch=(float)-Math.toDegrees(Math.atan2(to.y,Math.sqrt(to.x*to.x+to.z*to.z)));
        p.teleportTo(l,b.getX()+x,b.getY(),b.getZ()+z,yaw,off==0?pitch:0);p.setYHeadRot(yaw);p.setXRot(off==0?pitch:0);p.setYRot(yaw);
    }
    private static void pocket(ServerPlayer p){for(int i=0;i<9;i++)if(!WaxNight.mine(p.getInventory().getItem(i),p)&&!WaxNight.mine(p.getOffhandItem(),p)){p.getInventory().selected=i;return;}}
    private static void hold(ServerPlayer p){for(int i=0;i<9;i++)if(WaxNight.mine(p.getInventory().getItem(i),p)){p.getInventory().selected=i;return;}}
    private static float headTurn(LiteraryActor a,int who){return net.minecraft.util.Mth.wrapDegrees(a.getYHeadRot()-WaxSite.FIGURES.get(who).yaw());}
    private static boolean looked(ServerPlayer p){
        NativeTestPlayers.flush(p);
        for(var packet:NativeTestPlayers.packets(p))if(packet instanceof net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket c&&c.payload() instanceof io.github.knaitoe.theoldesthouse.network.WaxLookPayload look&&look.ids().length>0)return true;
        return false;
    }
    private static boolean blinkSent(ServerPlayer p){
        NativeTestPlayers.flush(p);
        for(var packet:NativeTestPlayers.packets(p))if(packet instanceof net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket c&&c.payload() instanceof io.github.knaitoe.theoldesthouse.network.WaxBlinkPayload)return true;
        return false;
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
            for(String line:List.of(WaxTexts.punched("11:52"),WaxTexts.LOCKER_OPEN,WaxTexts.DESK,WaxTexts.log(0,0,false),WaxTexts.CALLED,WaxTexts.CHAIR,WaxTexts.CLOCK,
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
            h.assertTrue(Waxworks.dealable(f.data(),p.getUUID()),"0.4.753: with its account, the House deals it");
            done();h.succeed();
        });
    }

    /**
     * 0.4.750, the flashlight and the figures. The NIGHTS locker lends one flashlight to a reader, and no second; it lights where it is
     * pointed while it is held, and goes out when it is not. A statesman looked straight at keeps his head where it was set; one at
     * the edge of the reader's sight turns his head toward them, and keeps the turn when the figures are put on their marks again. A
     * face held in the beam, close, blinks for that reader after four seconds, and not without the light. The statesman across from
     * the door counts as copying a reader who stands in front of him facing him. The second reader has none of it, nor does the
     * spectator, who is lent nothing. Out of the museum the flashlight goes back.
     */
    @GameTest(template="empty",batch="waxworks_night",timeoutTicks=1600)
    public static void theFlashlightTheLookTheBlinkAndTheMirror(GameTestHelper h){
        var f=place(h);var p=f.player("wax_guard",P);var q=f.player("wax_second",P);var l=f.level(P);var b=f.base(P);
        final int LAPEL=3,SCROLL=4;
        h.startSequence().thenIdle(5).thenExecute(()->Waxworks.ensure(l,b)).thenWaitUntil(()->{
            Waxworks.ensure(l,b);
            for(int who=0;who<Waxworks.FIGURES;who++)h.assertTrue(Waxworks.find(l,b,who)!=null,"figure "+who+" is on its mark");
        }).thenExecute(()->{
            Waxworks.arrive(p,b,f.own(p,P));Waxworks.arrive(q,b,f.own(q,P));
            p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-6.5,-90,0);q.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-3.5,180,0);
            f.click(p,P,WaxSite.LOCKERS[0]);f.click(p,P,WaxSite.LOCKERS[1]);
            h.assertTrue(WaxNight.has(p)&&p.getInventory().countItem(LastSeenRegistry.FLASHLIGHT.get())==1&&f.own(p,P).getBoolean("Light"),"the locker lends one flashlight, and no second");
            h.assertTrue(told(p).contains(WaxTexts.LOCKER_EMPTY),"a second hand on the locker finds the hook empty");
            h.assertFalse(WaxNight.has(q),"the second reader has not been to the locker");
            var s=NativeTestPlayers.survival(h,"wax_spectator");f.players.add(s);s.setGameMode(GameType.SPECTATOR);s.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-6.5,-90,0);
            WaxNight.locker(s,f.own(s,P));
            h.assertFalse(WaxNight.has(s),"a spectator is lent nothing");
            // Not yet held: no light.
            pocket(p);aim(p,l,b,-6.5,-26.5,net.minecraft.world.phys.Vec3.atCenterOf(b.offset(-12,1,-27)),0);
        }).thenIdle(4).thenExecute(()->{
            h.assertTrue(WaxNight.beamOf(p.getUUID())==null,"a flashlight in the pocket lights nothing");
            hold(p);
        }).thenIdle(4).thenExecute(()->{
            var at=WaxNight.beamOf(p.getUUID());
            h.assertTrue(at!=null&&l.getBlockState(at).is(net.minecraft.world.level.block.Blocks.LIGHT)&&at.getX()-b.getX()>=-12&&at.getX()-b.getX()<=-10,"held, it lights where it points: "+(at==null?null:at.subtract(b))+" from "+p.position().subtract(net.minecraft.world.phys.Vec3.atLowerCornerOf(b))+" yaw "+p.getYRot()+" pitch "+p.getXRot());
            pocket(p);
        }).thenIdle(4).thenExecute(()->{
            h.assertTrue(WaxNight.beamOf(p.getUUID())==null&&l.getBrightness(LightLayer.BLOCK,b.offset(-12,1,-27))<5,"put away, the light goes out");
            // Looked at straight, a statesman keeps his head where it was set.
            var lapel=Waxworks.find(l,b,LAPEL);aim(p,l,b,-6.5,-27.5,lapel.getEyePosition(),0);
        }).thenIdle(40).thenExecute(()->{
            h.assertTrue(Math.abs(WaxNight.turned(p.getUUID(),LAPEL))<1,"watched, his head stays where it was set: "+WaxNight.turned(p.getUUID(),LAPEL));
            aim(p,l,b,-6.5,-27.5,Waxworks.find(l,b,LAPEL).getEyePosition(),-50);looked(p);
        }).thenIdle(40).thenExecute(()->{
            float turn=WaxNight.turned(p.getUUID(),LAPEL);
            h.assertTrue(turn>20&&turn<=WaxNight.TURN_MAX,"at the edge of sight, his head has turned toward the reader: "+turn);
            h.assertTrue(looked(p),"and the turn is sent to that reader's own client");
            h.assertTrue(WaxNight.turned(q.getUUID(),LAPEL)==0&&!looked(q),"the other reader's statesman has not moved");
            var lapel=Waxworks.find(l,b,LAPEL);
            h.assertTrue(Math.abs(net.minecraft.util.Mth.wrapDegrees(lapel.getYHeadRot()-WaxSite.FIGURES.get(LAPEL).yaw()))<1,"the body in the world keeps its head where it was set");
            // The blink: without the light, nothing.
            var scroll=Waxworks.find(l,b,SCROLL);pocket(p);aim(p,l,b,-6.5,-28.5,scroll.getEyePosition(),0);told(p);
        }).thenIdle(WaxNight.HOLD+20).thenExecute(()->{
            h.assertTrue(f.own(p,P).getInt("Blinked")==0&&!blinkSent(p),"without the light on the face, nothing blinks");
            hold(p);
        }).thenIdle(WaxNight.HOLD+10).thenExecute(()->{
            h.assertTrue((f.own(p,P).getInt("Blinked")&1<<SCROLL)!=0&&blinkSent(p),"the face held in the beam for four seconds blinks, for this reader: "+f.own(p,P).getInt("Blinked"));
            h.assertFalse(blinkSent(q)||f.own(q,P).getInt("Blinked")!=0,"and nobody else sees it");
            // The mirror: standing in front of him, facing him.
            var mirror=Waxworks.find(l,b,WaxSite.MIRROR);aim(p,l,b,-6.5,-31.5,mirror.getEyePosition(),0);
        }).thenIdle(WaxNight.MIRROR_SEEN+10).thenExecute(()->{
            h.assertTrue(f.own(p,P).getBoolean("Mirrored")&&!f.own(q,P).getBoolean("Mirrored"),"the statesman across from the door copies the reader in front of him, and that reader alone");
            var feet=WaxSite.feet(WaxSite.FIGURES.get(WaxSite.MIRROR));
            h.assertFalse(WaxNight.before(new net.minecraft.world.phys.Vec3(feet.x+3,0,feet.z),new net.minecraft.world.phys.Vec3(-1,0,0),feet,WaxSite.FIGURES.get(WaxSite.MIRROR).yaw()),"behind him, there is nothing to copy");
            // Out of the museum, the flashlight goes back.
            p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()+12.5,0,0);
        }).thenIdle(25).thenExecute(()->{
            h.assertFalse(WaxNight.has(p),"out of the museum, the flashlight goes back on its hook");
            h.assertTrue(WaxNight.beamOf(p.getUUID())==null,"and its light is out");
            done();h.succeed();
        });
    }

    /** A free spot in front of a figure, the way it faces, a few blocks off, relative to the museum. */
    private static net.minecraft.world.phys.Vec3 before(ServerLevel l,BlockPos b,int who){
        var f=WaxSite.FIGURES.get(who);var feet=WaxSite.feet(f);double yaw=Math.toRadians(f.yaw());
        for(double d:new double[]{2.5,3.5,1.6,4.5}){
            double x=feet.x-Math.sin(yaw)*d,z=feet.z+Math.cos(yaw)*d;var cell=BlockPos.containing(x,0,z);
            if(l.getBlockState(b.offset(cell)).getCollisionShape(l,b.offset(cell)).isEmpty()&&l.getBlockState(b.offset(cell).above()).getCollisionShape(l,b.offset(cell).above()).isEmpty()&&WaxSite.gallery(new net.minecraft.world.phys.Vec3(x,.5,z))==f.gallery())
                return new net.minecraft.world.phys.Vec3(cell.getX()+.5,0,cell.getZ()+.5);
        }
        return new net.minecraft.world.phys.Vec3(feet.x-Math.sin(yaw)*2.5,0,feet.z+Math.cos(yaw)*2.5);
    }

    /**
     * 0.4.751, the shift. The time clock puts a reader on the shift and starts the night's one clock; a second card is refused and a
     * spectator punches nothing. With the flashlight on each figure of the foyer and the Hall, those galleries are counted, the Hall's
     * count taking the twenty-fourth when the beam finds him; with every gallery counted the hour's round is in the log, which says so.
     * An irregularity is made only where nobody is, comes into the ring when it has gone unnoticed, is noticed by someone on the shift
     * who looks at it with the light, and goes back to its mark when nobody sees it. A reader not on the shift notices nothing. Three
     * in the office end the night: the screens go dark, the light is taken, the shift is unfinished, and the reader not on it is not
     * failed. In a second night, six o'clock with every round in the log keeps the shift for whoever punched in, and for nobody else.
     */
    @GameTest(template="empty",batch="waxworks_shift",timeoutTicks=3000)
    public static void theShiftItsRoundsAndWhatMoves(GameTestHelper h){
        var f=place(h);var p=f.player("wax_night",P);var q=f.player("wax_visitor",P);var l=f.level(P);var b=f.base(P);
        final int[] irr={-1};WaxShift.forget(l,b);
        h.startSequence().thenIdle(5).thenExecute(()->Waxworks.ensure(l,b)).thenWaitUntil(()->{
            Waxworks.ensure(l,b);
            for(int who=0;who<Waxworks.FIGURES;who++)h.assertTrue(Waxworks.find(l,b,who)!=null,"figure "+who+" is on its mark");
        }).thenExecute(()->{
            Waxworks.arrive(p,b,f.own(p,P));Waxworks.arrive(q,b,f.own(q,P));
            p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-3.5,90,0);q.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-2.5,90,0);
            h.assertTrue(WaxShift.night(b)==null,"no night until a card is punched");
            f.click(p,P,WaxSite.TIMECLOCK);f.click(p,P,WaxSite.TIMECLOCK);
            var n=WaxShift.night(b);
            h.assertTrue(n!=null&&n.punched.contains(p.getUUID())&&n.punched.size()==1&&f.own(p,P).getBoolean("Punched"),"the time clock puts the reader on the shift");
            var why=told(p);h.assertTrue(why.contains(WaxTexts.punched("11:52"))&&why.contains(WaxTexts.PUNCH_AGAIN),"stamped once, and a second card refused: "+why);
            var s=NativeTestPlayers.survival(h,"wax_onlooker");f.players.add(s);s.setGameMode(GameType.SPECTATOR);s.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-3.5,90,0);
            WaxShift.punch(s,b,f.own(s,P));
            h.assertFalse(n.punched.contains(s.getUUID()),"a spectator punches nothing");
            f.click(p,P,WaxSite.LOCKERS[0]);hold(p);
            h.assertTrue(n.t<=2,"the clock has only just started: "+n.t);
        }).thenIdle(20).thenExecute(()->{
            var n=WaxShift.night(b);
            h.assertTrue(n.t>=19&&n.t<=22,"one clock, running while someone is on the shift: "+n.t);
            // The foyer and the Hall, figure by figure.
            var missed=new java.util.ArrayList<String>();
            for(int who=0;who<Waxworks.FIGURES;who++){
                var fig=WaxSite.FIGURES.get(who);if(fig.gallery()>1)continue;
                var a=Waxworks.find(l,b,who);var at=before(l,b,who);
                aim(p,l,b,at.x,at.z,a.position().add(0,a.getBbHeight()*.6,0),0);
                WaxShift.step(l,b);
                if((n.lit&1<<who)==0)missed.add(fig.key()+" from "+at);
            }
            h.assertTrue(missed.isEmpty(),"the beam on each figure counts it: missed "+missed);
            h.assertTrue((n.counted&3)==3,"the foyer and the Hall are counted: "+n.counted);
            h.assertTrue(n.counts[0][0]==3&&n.counts[0][1]==8&&f.own(p,P).getBoolean("FoundTwentyFourth"),"three in the foyer, and eight in the Hall with the one who has no plaque: "+n.counts[0][0]+", "+n.counts[0][1]);
            var why=told(p);h.assertTrue(why.contains(WaxTexts.counted(1,8)),"the count goes in the log: "+why);
            // The other galleries, counted the same way (their figures lit).
            for(int who=0;who<Waxworks.FIGURES;who++)if(WaxSite.FIGURES.get(who).gallery()>1)n.lit|=1<<who;
            for(int g=2;g<5;g++){var c=WaxSite.GALLERIES.get(g);p.teleportTo(l,b.getX()+(c.x0()+c.x1())/2.0+.5,b.getY(),b.getZ()+(c.z0()+c.z1())/2.0+.5,0,0);WaxShift.step(l,b);}
            h.assertTrue(n.rounds==1,"every gallery counted: the twelve o'clock round is in the log: "+n.rounds+" counted "+n.counted);
            h.assertTrue(told(p).contains(WaxTexts.round(0)),"and the reader is told");
            h.assertTrue(WaxShift.log(b).startsWith("The shift log. One of six rounds"),"the log on the desk says so: "+WaxShift.log(b));
            h.assertTrue(f.own(q,P).getInt("Lit")==0,"the reader not on the shift has counted nothing");
            // An irregularity, made while everyone is in the office.
            p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-13.5,0,0);q.teleportTo(l,b.getX()+1.5,b.getY(),b.getZ()-13.5,0,0);
            n.t=WaxShift.FIRST-30;
        }).thenWaitUntil(()->h.assertTrue(!WaxShift.night(b).irregular.isEmpty(),"an irregularity, once the half hour comes: t "+WaxShift.night(b).t)).thenExecute(()->{
            var n=WaxShift.night(b);var i=n.irregular.get(0);irr[0]=i.who;
            var fig=WaxSite.FIGURES.get(i.who);var a=Waxworks.find(l,b,i.who);
            h.assertTrue(i.who!=WaxSite.STRANGER&&i.who!=WaxSite.MIRROR&&fig.pose()!=WaxSite.COVERED,"never the twenty-fourth, the mirror or the one under the sheet: "+fig.key());
            if(i.kind==1)h.assertTrue(a.isInvisible(),"gone from its plinth: "+fig.key());
            else h.assertTrue(Math.abs(net.minecraft.util.Mth.wrapDegrees(a.getYRot()-fig.yaw()))>5,"turned on its mark: "+fig.key()+" "+a.getYRot());
            i.since=WaxShift.NEARER;
        }).thenWaitUntil(()->h.assertTrue(WaxShift.night(b).irregular.get(0).stage==1,"unnoticed, it comes out into the ring")).thenExecute(()->{
            var n=WaxShift.night(b);var i=n.irregular.get(0);var a=Waxworks.find(l,b,i.who);
            var ring=WaxShift.place(b,i.who).at().add(b.getX(),b.getY(),b.getZ());
            h.assertTrue(a!=null&&a.position().distanceTo(ring)<.5&&!a.isInvisible(),"and stands in the ring outside its gallery: "+(a==null?null:a.position())+" for "+ring);
            // The visitor looks at it: nothing. The night guard with the light: noticed.
            aim(q,l,b,ring.x-b.getX()+(ring.z-b.getZ()>-40?0:0),ring.z-b.getZ()+3,a.getEyePosition(),0);
        }).thenIdle(45).thenExecute(()->{
            var n=WaxShift.night(b);var i=n.irregular.get(0);var a=Waxworks.find(l,b,i.who);
            h.assertFalse(i.noticed,"somebody not on the shift notices nothing");
            q.teleportTo(l,b.getX()+1.5,b.getY(),b.getZ()-13.5,0,0);
            var ring=a.position();aim(p,l,b,ring.x-b.getX(),ring.z-b.getZ()+3,a.getEyePosition(),0);told(p);
        }).thenIdle(45).thenExecute(()->{
            var n=WaxShift.night(b);
            h.assertTrue(n.irregular.isEmpty()||n.irregular.get(0).noticed,"the night guard, looking at it with the light, notices");
            h.assertTrue(n.noticed==1&&f.own(p,P).getInt("Noticed")==1,"it goes in the log, and on that reader's record");
            h.assertTrue(told(p).stream().anyMatch(t->t.endsWith("It goes in the log.")&&t.startsWith("That one")),"and they are told what they saw");
            p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-13.5,0,0);
        }).thenWaitUntil(()->h.assertTrue(WaxShift.night(b).irregular.isEmpty(),"unseen, it goes back")).thenExecute(()->{
            var a=Waxworks.find(l,b,irr[0]);var feet=WaxSite.feet(WaxSite.FIGURES.get(irr[0])).add(b.getX(),b.getY(),b.getZ());
            h.assertTrue(a!=null&&a.position().distanceTo(feet)<.3&&!a.isInvisible(),"to its own mark");
            // Three in the office end the night.
            var n=WaxShift.night(b);
            for(int who:new int[]{0,1,6}){var i=new WaxShift.Irr(who,0);i.stage=WaxShift.ARRIVED;n.irregular.add(i);}
        }).thenIdle(3).thenExecute(()->{
            var n=WaxShift.night(b);
            h.assertTrue(n!=null&&n.failed&&n.over,"three in the office: the night is over");
            for(var m:WaxSite.MONITORS)h.assertTrue(stage(l,b,m)==0,"the screens go dark");
            h.assertTrue(!WaxNight.has(p)&&WaxNight.beamOf(p.getUUID())==null,"the flashlight is gone and its light out");
            h.assertTrue(f.own(p,P).getBoolean("Failed")&&!f.own(p,P).getBoolean("Kept"),"the shift is unfinished");
            h.assertFalse(f.own(q,P).getBoolean("Failed"),"the reader not on the shift is not failed");
            h.assertTrue(told(p).contains(WaxTexts.FAILED),"and they are told");
            // The museum empty, the night is forgotten.
            p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()+30.5,0,0);q.teleportTo(l,b.getX()+1.5,b.getY(),b.getZ()+30.5,0,0);
        }).thenWaitUntil(()->h.assertTrue(WaxShift.night(b)==null,"emptied, the night is forgotten")).thenExecute(()->{
            for(var m:WaxSite.MONITORS)h.assertTrue(stage(l,b,m)==1,"and the screens are back");
            h.assertTrue(stage(l,b,WaxSite.CLOCK)==0,"and the clock at eight minutes to");
            p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-3.5,90,0);q.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-2.5,90,0);
            f.click(p,P,WaxSite.TIMECLOCK);
            var n=WaxShift.night(b);n.rounds=(1<<WaxShift.HOURS)-1;n.t=WaxShift.SIX-3;
        }).thenIdle(6).thenExecute(()->{
            var n=WaxShift.night(b);
            h.assertTrue(n.over&&n.kept&&f.own(p,P).getBoolean("Kept"),"six, every round in the log: the shift is kept");
            h.assertTrue(stage(l,b,WaxSite.CLOCK)==7,"and the clock says six");
            h.assertFalse(f.own(q,P).getBoolean("Kept"),"and kept for whoever punched in, nobody else");
            h.assertTrue(told(p).contains(WaxTexts.KEPT),"the key turns in the front door");
            done();h.succeed();
        });
    }

    private static String account(ServerPlayer p,LiteraryTests.Fixture f){
        return String.join(" ",LiteraryTexts.ending(p.getGameProfile().getName(),P,f.own(p,P)).get(net.minecraft.core.component.DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().map(x->x.raw().getString()).toList()).replaceAll("\\s+"," ");
    }
    /**
     * 0.4.753, the account: the telephone on the desk, the police at five, six o'clock with every round in the log, and the night guard's
     * log in each reader's own hand. One reader calls, one does not, and a spectator earns nothing.
     */
    @GameTest(template="empty",batch="waxworks_account",timeoutTicks=1600)
    public static void thePhoneThePoliceAndTheLog(GameTestHelper h){
        var f=place(h);var p=f.player("wax_caller",P);var q=f.player("wax_partner",P);var l=f.level(P);var b=f.base(P);
        final ServerPlayer[] s={null};WaxShift.forget(l,b);
        h.startSequence().thenIdle(5).thenExecute(()->Waxworks.ensure(l,b)).thenWaitUntil(()->{
            Waxworks.ensure(l,b);
            for(int who=0;who<Waxworks.FIGURES;who++)h.assertTrue(Waxworks.find(l,b,who)!=null,"figure "+who+" is on its mark");
        }).thenExecute(()->{
            Waxworks.arrive(p,b,f.own(p,P));Waxworks.arrive(q,b,f.own(q,P));
            p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-3.5,90,0);q.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-2.5,90,0);
            told(p);f.click(p,P,WaxSite.PHONE);
            h.assertTrue(told(p).contains(WaxTexts.DESK_PHONE)&&!f.own(p,P).getBoolean("Called"),"before a card is punched the telephone is only a telephone");
            f.click(p,P,WaxSite.TIMECLOCK);f.click(q,P,WaxSite.TIMECLOCK);
            var sp=NativeTestPlayers.survival(h,"wax_watching");f.players.add(sp);sp.setGameMode(GameType.SPECTATOR);sp.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-14.5,0,0);s[0]=sp;
            p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-14.5,180,0);q.teleportTo(l,b.getX()+1.5,b.getY(),b.getZ()-14.5,180,0);
            WaxShift.phone(sp,b,f.own(sp,P));
            var n=WaxShift.night(b);h.assertFalse(n.called,"a spectator calls nobody");
            told(p);f.click(p,P,WaxSite.PHONE);
            h.assertTrue(n.called&&f.own(p,P).getBoolean("CalledTonight")&&told(p).contains(WaxTexts.CALLED),"the night guard calls the police: "+f.own(p,P));
            told(q);f.click(q,P,WaxSite.PHONE);
            h.assertTrue(told(q).contains(WaxTexts.CALLED_AGAIN)&&!f.own(q,P).getBoolean("Called"),"once a night: the other reader only hears it ring");
            // Something out of place, unnoticed, and five o'clock.
            var i=new WaxShift.Irr(0,0);i.stage=1;n.irregular.add(i);
            n.t=WaxShift.POLICE-2;
        }).thenWaitUntil(()->h.assertTrue(WaxShift.night(b).police,"five o'clock: the police come")).thenExecute(()->{
            var n=WaxShift.night(b);
            h.assertTrue(n.irregular.stream().allMatch(x->x.noticed),"what was out of place is put down to them, and goes home");
            h.assertTrue(f.own(p,P).getBoolean("PoliceTonight")&&f.own(q,P).getBoolean("PoliceTonight")&&!f.own(s[0],P).getBoolean("PoliceTonight"),"everyone on the shift sees them; the spectator is not on it");
            h.assertTrue(told(p).contains(WaxTexts.POLICE)&&told(q).contains(WaxTexts.POLICE),"and is told");
            told(p);f.click(p,P,WaxSite.PHONE);h.assertTrue(told(p).contains(WaxTexts.POLICE_GONE),"afterward the phone says they have been");
            // Every round in the log, and six.
            n.rounds=(1<<WaxShift.HOURS)-1;n.t=WaxShift.SIX-2;
        }).thenWaitUntil(()->h.assertTrue(WaxShift.night(b).over,"six o'clock")).thenExecute(()->{
            var n=WaxShift.night(b);h.assertTrue(n.kept&&!n.failed,"the shift is kept");
            var one=f.own(p,P);var two=f.own(q,P);
            h.assertTrue(one.getBoolean("Ready")&&one.getString("Outcome").equals("called_the_police"),"the caller's account is ready: "+one);
            h.assertTrue(two.getBoolean("Ready")&&two.getString("Outcome").equals("kept_the_shift"),"and the other's: "+two);
            h.assertFalse(f.own(s[0],P).getBoolean("Ready"),"the spectator earns nothing");
            var text=account(p,f);
            h.assertTrue(text.contains("I called the police from the desk phone.")&&text.contains("They counted twenty-three."),"the caller's log: "+text);
            h.assertTrue(text.contains("Night log. Merriman's.")&&text.contains("Seen."),"opens on the night and ends at six: "+text);
            h.assertFalse(text.contains("blinked")||text.contains("stood the way I stood"),"and says nothing the reader did not see: "+text);
            var other=account(q,f);
            h.assertTrue(other.contains("Somebody else on the shift called the police.")&&!other.contains("I called the police"),"the other's: "+other);
            // A reader who saw a blink, the mirror and the twenty-fourth has them in their log, and never what the twenty-fourth was.
            var seen=f.own(q,P);seen.putInt("Blinked",1<<WaxSite.STRANGER|1<<2);seen.putBoolean("Mirrored",true);seen.putBoolean("FoundTwentyFourth",true);seen.putInt("Noticed",2);seen.putInt("NoticedOnScreen",1);
            io.github.knaitoe.theoldesthouse.labyrinth.LiteraryVignettes.save(f.data(),q.getUUID(),P,seen);
            var full=account(q,f);
            h.assertTrue(full.contains("I counted eight where the plaques say seven")&&full.contains("the ticket seller and the one by the wall with no plaque")&&full.contains("he stood the way I stood")&&full.contains("Twice something was not where it had been"),"what that reader saw: "+full);
            h.assertTrue(full.contains("I did not ask him about the one with no plaque."),"and it ends without saying what it was: "+full);
            f.source(p,P);f.read(p,P,WaxSite.LOG);
            h.assertTrue(f.own(p,P).getBoolean("Completed")&&WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.WAXWORKS),"read after the notice, it is theirs");
            h.assertFalse(WitnessAccount.has(f.data(),q.getUUID(),WitnessAccount.Story.WAXWORKS),"the other reader's is still theirs to read");
            done();h.succeed();
        });
    }
    private static boolean sat(ServerPlayer p){
        NativeTestPlayers.flush(p);
        for(var packet:NativeTestPlayers.packets(p))if(packet instanceof net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket c&&c.payload() instanceof io.github.knaitoe.theoldesthouse.network.WaxMonitorPayload)return true;
        return false;
    }
    /**
     * 0.4.752, the monitors. A hand on a screen from the office sits the reader down at the bank (their client is told where the
     * cameras are); from the corridor it is only a screen. The feed they have up is watched, and standing up or walking out leaves it.
     * "Something's moved" on a feed that shows nothing out of place notes nothing, and a second mark straight after is not taken; on the
     * feed that shows an irregular figure, it is noted, in the log and on the reader's record as seen on a screen. Someone not on the
     * shift writes nothing in the log. A spectator does not sit down.
     */
    @GameTest(template="empty",batch="waxworks_monitors",timeoutTicks=1200)
    public static void theMonitorsAndWhatTheyShow(GameTestHelper h){
        var f=place(h);var p=f.player("wax_desk",P);var q=f.player("wax_caller",P);var l=f.level(P);var b=f.base(P);WaxShift.forget(l,b);
        h.startSequence().thenIdle(5).thenExecute(()->Waxworks.ensure(l,b)).thenWaitUntil(()->{
            Waxworks.ensure(l,b);
            for(int who=0;who<Waxworks.FIGURES;who++)h.assertTrue(Waxworks.find(l,b,who)!=null,"figure "+who+" is on its mark");
        }).thenExecute(()->{
            Waxworks.arrive(p,b,f.own(p,P));Waxworks.arrive(q,b,f.own(q,P));
            p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-3.5,90,0);f.click(p,P,WaxSite.TIMECLOCK);
            p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-7.5,180,0);sat(p);
            WaxMonitors.sit(p,b,f.own(p,P));
            h.assertFalse(sat(p),"from the corridor it is only a screen");
            p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-15.5,180,0);q.teleportTo(l,b.getX()+2.5,b.getY(),b.getZ()-15.5,180,0);
            var mine=f.own(p,P);WaxMonitors.sit(p,b,mine);LiteraryVignettes.save(f.data(),p.getUUID(),P,mine);
            h.assertTrue(sat(p)&&f.own(p,P).getBoolean("Monitors"),"from the office, a hand on a screen sits the reader down at the bank");
            var s=NativeTestPlayers.survival(h,"wax_watcher");f.players.add(s);s.setGameMode(GameType.SPECTATOR);s.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-14.5,180,0);sat(s);
            WaxMonitors.sit(s,b,f.own(s,P));
            h.assertFalse(sat(s),"a spectator does not sit down");
            WaxMonitors.feed(p,2,false);
            h.assertTrue(WaxMonitors.watched(2)&&!WaxMonitors.watched(1),"the feed the reader has up is watched");
            // An irregularity in the Hall of Statesmen.
            var n=WaxShift.night(b);var i=new WaxShift.Irr(3,0);n.irregular.add(i);told(p);
            WaxMonitors.feed(p,2,true);
            h.assertTrue(told(p).contains(WaxTexts.NOTHING_MOVED)&&!i.noticed,"on a feed that shows nothing out of place, nothing is noted");
            WaxMonitors.feed(p,1,true);
            h.assertFalse(i.noticed,"and a second mark straight after is not taken");
        }).thenIdle(WaxMonitors.AGAIN+2).thenExecute(()->{
            var n=WaxShift.night(b);var i=n.irregular.get(0);
            WaxMonitors.feed(p,1,true);
            h.assertTrue(i.noticed&&n.noticed==1,"on the feed that shows it, it is noted");
            h.assertTrue(f.own(p,P).getInt("Noticed")==1&&f.own(p,P).getInt("NoticedOnScreen")==1,"and kept on the reader's record as seen on a screen");
            h.assertTrue(told(p).contains(WaxTexts.noticed(0,0)),"they are told");
            WaxMonitors.feed(q,1,true);
            h.assertTrue(told(q).contains(WaxTexts.NOT_ON_SHIFT)&&n.noticed==1,"someone not on the shift writes nothing in the log");
            WaxMonitors.feed(p,-1,false);
            h.assertTrue(WaxMonitors.watching(p.getUUID())==-1,"standing up leaves the feed");
            WaxMonitors.feed(p,4,false);p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-5.5,0,0);
        }).thenIdle(12).thenExecute(()->{
            h.assertTrue(WaxMonitors.watching(p.getUUID())==-1&&!WaxMonitors.watched(4),"and so does walking out of the office");
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
