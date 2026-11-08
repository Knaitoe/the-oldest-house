package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.349, In the Tall Grass, with real readers. The road built whole and dealt only after the yard; the wagon on the shoulder with its
 * hazards blinking; the grass thin in the first rows and close past the old fence; the county's notice. One reader: the radio by the
 * wagon, the road seen from the first rows, gone in past the fence, moved farther from the road, the track trampled and springing back,
 * the hazards heard true by a reader standing still, set down by the heart of the field, the cap taken; then let out at the edge, the
 * pickup pulling over, the driver calling, the shout to stay by the car, the driver driving on, and an account that says only what this
 * reader did. Then together: a caller on the shoulder heard first from ahead and later from the true side, readers in the grass hearing
 * each other from the wrong side, two found together and moved as a huddle, a reader who comes in partway, and the first shout for help
 * deciding for everyone, each with their own account. The road set back after.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_literary") @PrefixGameTestTemplate(false)
public final class TallGrassTests {
    private TallGrassTests(){}
    private static final LabyrinthPlace P=LabyrinthPlace.TALL_GRASS;
    private static LiteraryTests.Fixture active;
    private static LiteraryTests.Fixture fixture(GameTestHelper h){if(active!=null)active.close();active=new LiteraryTests.Fixture(h);TallGrass.clearAll();return active;}
    private static AABB box(LiteraryTests.Fixture f){var b=f.base(P);return new AABB(b.getX()-57,b.getY()-5,b.getZ()-113,b.getX()+57,b.getY()+16,b.getZ()+3);}
    private static String account(ServerPlayer p){for(var st:p.getInventory().items)if(st.is(Items.WRITTEN_BOOK)){var c=st.get(DataComponents.WRITTEN_BOOK_CONTENT);if(c!=null&&c.title().raw().equals(TallGrassTexts.TITLE))return String.join(" ",c.pages().stream().map(pg->pg.raw().getString()).toList());}return "";}
    private static boolean holds(ServerPlayer p,net.minecraft.world.item.Item item){for(var st:p.getInventory().items)if(st.is(item))return true;return false;}
    private static Vec3 rel(LiteraryTests.Fixture f,ServerPlayer p){var b=f.base(P);return p.position().subtract(b.getX(),b.getY(),b.getZ());}
    /** A hand on one of the yard's fittings here. */
    private static boolean use(LiteraryTests.Fixture f,ServerPlayer p,BlockPos rel){var at=f.base(P).offset(rel);return TallGrass.use(p,at,f.level(P).getBlockState(at));}
    private static void close(){if(active!=null){active.close();active=null;}TallGrass.clearAll();}
    @AfterBatch(batch="tall_grass") public static void done(net.minecraft.server.level.ServerLevel l){close();}
    @AfterBatch(batch="tall_grass_together") public static void togetherDone(net.minecraft.server.level.ServerLevel l){close();}

    @GameTest(template="empty",batch="tall_grass",timeoutTicks=4000)
    public static void aReaderGoesInAndTheRoadRecedes(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);
        ServerPlayer[] me=new ServerPlayer[1];double[] depth=new double[1];boolean[] blink=new boolean[2];
        h.startSequence().thenExecute(()->f.chunks.hold(l,box(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the road is resident"))
            .thenExecute(()->{
                h.assertTrue(LiteraryRooms.isLiterary(P)&&LiteraryRooms.outside(P)&&P.isOneShot()&&P.authored()&&SceneClock.at(f.origin,b.offset(0,0,-40),io.github.knaitoe.theoldesthouse.house.HouseDimensions.OUTSIDE)==16,"a literary road outside, one visit, built whole, late in the afternoon");
                var story=WitnessAccount.Story.of(P.id());h.assertTrue(story!=null&&story.kind.equals("survival")&&WitnessAccount.Story.values().length==WitnessPool.SOURCES&&WitnessAccount.REQUIRED==WitnessPool.REQUIRED,"a survival story, the quota derived");
                for(int y=0;y<3;y++)h.assertTrue(YardBlock.is(l.getBlockState(b.offset(10,y,-40)),YardBlock.Kind.GRASS),"grass three high past the fence");
                int thin=0,close=0;for(int x=-40;x<=40;x++){if(!YardBlock.is(l.getBlockState(b.offset(x,0,TallGrassSite.GRASS_S)),YardBlock.Kind.GRASS))thin++;if(!YardBlock.is(l.getBlockState(b.offset(x,0,-40)),YardBlock.Kind.GRASS))close++;}
                h.assertTrue(thin>40&&close==0,"the first row is thin ("+thin+" gaps), the field close ("+close+")");
                h.assertTrue(YardBlock.is(l.getBlockState(b.offset(TallGrassSite.WAGON_NOSE_X,0,TallGrassSite.WAGON_Z1)),YardBlock.Kind.NOSE)&&YardBlock.is(l.getBlockState(b.offset(TallGrassSite.WAGON_TAIL_X,0,TallGrassSite.WAGON_Z1)),YardBlock.Kind.TAIL),"the House's wagon on the shoulder");
                h.assertTrue(YardBlock.is(l.getBlockState(b.offset(TallGrassSite.HEART)),YardBlock.Kind.CAP),"the cap at the heart of the field");
                h.assertTrue(DeerRoadBlock.is(l.getBlockState(b.offset(TallGrassSite.NOTICE)),DeerRoadBlock.Kind.NOTICE),"the county's notice on its post");
                var text=LiteraryTexts.source(P).get(DataComponents.WRITTEN_BOOK_CONTENT);var all=String.join(" ",text.pages().stream().map(pg->pg.raw().getString()).toList());
                h.assertTrue(all.contains("stay with your vehicle")&&all.contains("torn off"),"the notice: "+all);
                var probe=UUID.randomUUID();h.assertTrue(!TallGrass.dealable(f.data(),probe),"not before the yard");
                f.data().visit(probe,LabyrinthPlace.YARD_TURNAROUND);h.assertTrue(TallGrass.dealable(f.data(),probe),"after it");
                me[0]=f.player("grass_one",P);
            })
            .thenWaitUntil(()->{var st=l.getBlockState(b.offset(TallGrassSite.WAGON_TAIL_X,0,TallGrassSite.WAGON_Z1));blink[st.getValue(YardBlock.STAGE)]=true;h.assertTrue(blink[0]&&blink[1],"the hazards blink");})
            .thenExecute(()->f.at(me[0],P,TallGrassSite.WAGON_TAIL_X+1.5,0,TallGrassSite.NORTH_SHOULDER+.5))
            .thenWaitUntil(()->h.assertTrue(f.own(me[0],P).getBoolean("HeardRadio"),"the radio, by the wagon"))
            .thenExecute(()->f.at(me[0],P,.5,0,-15.5))
            .thenWaitUntil(()->h.assertTrue(f.own(me[0],P).getBoolean("SawRoad"),"the road seen back through the first rows"))
            .thenExecute(()->{h.assertTrue(!f.own(me[0],P).getBoolean("Committed"),"not gone in yet");f.at(me[0],P,.5,0,-30.5);})
            .thenWaitUntil(()->h.assertTrue(f.own(me[0],P).getBoolean("Committed"),"gone in past the fence posts"))
            .thenExecute(()->{depth[0]=rel(f,me[0]).z;TallGrass.moveNow(b,me[0]);})
            .thenWaitUntil(()->h.assertTrue(f.own(me[0],P).getInt("Moved")==1&&rel(f,me[0]).z<depth[0]-3,"moved farther from the road: from "+depth[0]+" to "+rel(f,me[0]).z))
            .thenExecute(()->{
                var at=me[0].blockPosition();TallGrass.trample(l,at,me[0]);
                h.assertTrue(YardBlock.is(l.getBlockState(at),YardBlock.Kind.TRAMPLED)&&l.getBlockState(at.above()).isAir()&&l.getBlockState(at.above(2)).isAir(),"the grass goes down under the reader");
                TallGrass.ageTrack(b,TallGrass.FLAT+5);})
            .thenWaitUntil(()->{var at=me[0].blockPosition();h.assertTrue(YardBlock.is(l.getBlockState(at),YardBlock.Kind.TRAMPLED)&&l.getBlockState(at).getValue(YardBlock.STAGE)>=1,"the track springing back");})
            .thenWaitUntil(()->h.assertTrue(f.own(me[0],P).getBoolean("HeardTrue"),"standing still, the hazards heard where the road is"))
            .thenExecute(()->TallGrass.moveNow(b,me[0]))
            .thenWaitUntil(()->h.assertTrue(f.own(me[0],P).getInt("Moved")==2,"moved again"))
            .thenExecute(()->TallGrass.moveNow(b,me[0]))
            .thenWaitUntil(()->h.assertTrue(f.own(me[0],P).getBoolean("SawHeart"),"the third time, set down by the heart of the field: at "+rel(f,me[0])))
            .thenExecute(()->{f.at(me[0],P,TallGrassSite.HEART.getX()+.5,0,TallGrassSite.HEART.getZ()+1.5);h.assertTrue(use(f,me[0],TallGrassSite.HEART)&&holds(me[0],TallGrassRegistry.CAP.get())&&f.own(me[0],P).getBoolean("TookCap"),"the boy's cap taken");
                TallGrass.beginNow(l,b);})
            .thenWaitUntil(()->h.assertTrue(f.own(me[0],P).getBoolean("LetOut")&&rel(f,me[0]).z>TallGrassSite.FIRST_ROWS,"let out at the edge of the grass: "+rel(f,me[0])))
            .thenWaitUntil(()->h.assertTrue(TallGrass.carParked(b),"the pickup pulls over"))
            .thenWaitUntil(()->h.assertTrue(TallGrass.end(b)==TallGrass.CALLING&&Talk.waiting(me[0],TallGrass.TALK)&&TallGrass.driver(l,b)!=null,"the driver gets out and calls, and the reader may shout"))
            .thenExecute(()->h.assertTrue(Talk.choose(me[0],"Stay by your car"),"the shouts: "+Talk.replies(me[0])))
            .thenWaitUntil(()->h.assertTrue(f.own(me[0],P).getBoolean("Completed"),"the shout decides"))
            .thenExecute(()->{
                var o=f.own(me[0],P);h.assertTrue(o.getString("Outcome").equals(TallGrass.WARNED)&&o.getString("GrassRole").equals("actor")&&WitnessAccount.has(f.data(),me[0].getUUID(),WitnessAccount.Story.TALL_GRASS),"resolved: warned them off the grass: "+o);
                var text=account(me[0]);
                h.assertTrue(text.contains("From the first rows I could still see the road")&&text.contains("The grass moved me")&&text.contains("When I stood still I could hear the hazards")&&text.contains("small cap")&&text.contains("I took it")&&text.contains("I shouted to them to stay by their car"),"the account follows the record: "+text);
                h.assertTrue(text.contains("near the end of its side")||text.contains("losing its signal"),"the radio the reader heard: "+text);
            })
            .thenWaitUntil(()->h.assertTrue(!TallGrass.carParked(b)&&TallGrass.driver(l,b)==null,"the driver gets back in and drives on"))
            .thenExecute(()->{var at=me[0].blockPosition();TallGrass.resetNow(l,b);
                h.assertTrue(!YardBlock.is(l.getBlockState(b.offset(TallGrassSite.STOP_X,0,TallGrassSite.NORTH_SHOULDER)),YardBlock.Kind.PICKUP_NOSE),"the road set back: no pickup on the shoulder");})
            .thenExecute(()->{if(active!=null){active.close();active=null;}TallGrass.clearAll();})
            .thenSucceed();
    }

    @GameTest(template="empty",batch="tall_grass_together",timeoutTicks=4000)
    public static void togetherTheFirstShoutDecides(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);
        ServerPlayer[] r=new ServerPlayer[4];
        h.startSequence().thenExecute(()->f.chunks.hold(l,box(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the road is resident"))
            .thenExecute(()->{r[0]=f.player("grass_caller",P);r[1]=f.player("grass_left",P);r[2]=f.player("grass_right",P);
                f.at(r[1],P,-20.5,0,-30.5);f.at(r[2],P,20.5,0,-30.5);})
            .thenWaitUntil(()->h.assertTrue(f.own(r[1],P).getBoolean("Committed")&&f.own(r[2],P).getBoolean("Committed"),"two readers gone in, apart"))
            // The first call from the shoulder comes from ahead.
            .thenExecute(()->{f.at(r[0],P,TallGrassSite.WAGON_TAIL_X+1.5,0,TallGrassSite.NORTH_SHOULDER+.5);h.assertTrue(use(f,r[0],new BlockPos(TallGrassSite.WAGON_TAIL_X,0,TallGrassSite.WAGON_Z1)),"the wagon answers a hand");
                h.assertTrue(Talk.choose(r[0],"Over here"),"a call into the grass: "+Talk.replies(r[0]));
                for(int i=1;i<=2;i++){var o=f.own(r[i],P);h.assertTrue(o.getBoolean("FirstAhead")&&o.getString("HeardCaller").equals("grass_caller"),"the first call heard ahead: "+o);}})
            // Later, from the true side, while the caller stands still by the wagon.
            .thenIdle(60)
            .thenExecute(()->{use(f,r[0],new BlockPos(TallGrassSite.WAGON_TAIL_X,0,TallGrassSite.WAGON_Z1));h.assertTrue(Talk.choose(r[0],"Follow my voice"),"another call");
                h.assertTrue(f.own(r[1],P).getInt("TrueCalls")==1&&f.own(r[0],P).getInt("Called")==2,"the second heard from the true side: "+f.own(r[1],P));})
            // In the grass, from the wrong side.
            .thenExecute(()->{var at=r[1].blockPosition().north(2);h.assertTrue(use(f,r[1],at.subtract(b)),"the grass answers a hand: "+l.getBlockState(at));h.assertTrue(Talk.choose(r[1],"Hello?"),"a call from the grass");
                h.assertTrue(f.own(r[2],P).getString("HeardOther").equals("grass_left"),"heard from the wrong side by the other");})
            // Found together, and moved as one.
            .thenExecute(()->{var at=rel(f,r[1]);f.at(r[2],P,at.x+1.5,0,at.z);})
            .thenWaitUntil(()->h.assertTrue(f.own(r[1],P).getString("FoundOther").equals("grass_right")&&f.own(r[2],P).getString("FoundOther").equals("grass_left"),"found each other a few stems apart"))
            .thenExecute(()->{TallGrass.moveNow(b,r[1]);TallGrass.moveNow(b,r[2]);})
            .thenWaitUntil(()->h.assertTrue(f.own(r[1],P).getString("MovedWith").equals("grass_right")&&f.own(r[2],P).getString("MovedWith").equals("grass_left")&&r[1].distanceTo(r[2])<3,"moved as a huddle: "+r[1].distanceTo(r[2])))
            .thenExecute(()->TallGrass.beginNow(l,b))
            .thenWaitUntil(()->h.assertTrue(f.own(r[1],P).getBoolean("LetOut")&&f.own(r[2],P).getBoolean("LetOut"),"both let out at the edge"))
            .thenExecute(()->{r[3]=f.player("grass_late",P);h.assertTrue(f.own(r[3],P).getBoolean("Partway"),"a reader who comes in partway");})
            .thenWaitUntil(()->h.assertTrue(TallGrass.end(b)==TallGrass.CALLING&&Talk.waiting(r[1],TallGrass.TALK)&&Talk.waiting(r[2],TallGrass.TALK)&&!Talk.waiting(r[0],TallGrass.TALK),"those at the edge may shout; the caller on the road is not asked"))
            .thenExecute(()->h.assertTrue(Talk.choose(r[2],"Help! In here"),"the first shout"))
            .thenWaitUntil(()->{for(var p:r)h.assertTrue(f.own(p,P).getBoolean("Completed"),p.getGameProfile().getName()+" resolved");})
            .thenExecute(()->{
                for(var p:r)h.assertTrue(f.own(p,P).getString("Outcome").equals(TallGrass.CALLED)&&WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.TALL_GRASS),"every reader on the road has the same ending: "+p.getGameProfile().getName());
                h.assertTrue(f.own(r[2],P).getString("GrassRole").equals("actor")&&f.own(r[1],P).getString("GrassRole").equals("witness")&&f.own(r[0],P).getString("GrassRole").equals("witness")&&f.own(r[3],P).getString("GrassRole").equals("partway"),"actor, witnesses and partway");
                var shouter=account(r[2]);var other=account(r[1]);var caller=account(r[0]);var late=account(r[3]);
                h.assertTrue(shouter.contains("I shouted for help")&&shouter.contains("moved grass_left with me"),"the shouter's account: "+shouter);
                h.assertTrue(other.contains("grass_right shouted for help")&&other.contains("grass_caller called to me from the road, and the first time I heard it from ahead of me")&&other.contains("Later I heard grass_caller from the right side"),"the other's account: "+other);
                h.assertTrue(caller.contains("I stood by the wagon and called into the grass")&&caller.contains("Follow my voice")&&!caller.contains("I went in"),"the caller's account: "+caller);
                h.assertTrue(late.contains("I came to the road late"),"the late reader's account: "+late);
            })
            .thenWaitUntil(()->h.assertTrue(TallGrass.driver(l,b)==null,"the driver goes into the grass and does not come out"))
            .thenExecute(()->{if(active!=null){active.close();active=null;}TallGrass.clearAll();})
            .thenSucceed();
    }
}
