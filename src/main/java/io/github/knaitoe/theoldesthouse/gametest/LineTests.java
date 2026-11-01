package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.212, the line, played with real readers: dealt only in the dark band; holding the line, losing it and finding it again;
 * the tugs from behind and the thing coming along the line; reeled all the way (depth lost, the line round the arm, a thing gone);
 * and reaching the pack first, with the tape in the reader's own hand.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_literary") @PrefixGameTestTemplate(false)
public final class LineTests {
    private LineTests(){}
    private static final LabyrinthPlace P=LabyrinthPlace.LINE;
    private static LiteraryTests.Fixture active;
    private static LiteraryTests.Fixture fixture(GameTestHelper h){if(active!=null)active.close();active=new LiteraryTests.Fixture(h);Line.clearAll();return active;}
    private static void close(){if(active!=null){active.close();active=null;}Line.clearAll();}
    private static net.minecraft.nbt.CompoundTag set(LiteraryTests.Fixture f){return Line.set(f.in.getServer());}
    private static void hold(LiteraryTests.Fixture f){var b=f.base(P);f.chunks.hold(f.in,new AABB(b.getX()-26,b.getY()-2,b.getZ()-91,b.getX()+27,b.getY()+8,b.getZ()+3));}
    /** Stands the reader on the line, this far along it. */
    private static void along(LiteraryTests.Fixture f,ServerPlayer p,double s){var b=f.base(P);var at=LineSite.at(s);p.teleportTo(f.in,b.getX()+at.x,b.getY(),b.getZ()+at.z,180,0);}
    private static void clickFrom(LiteraryTests.Fixture f,ServerPlayer p,BlockPos rel){var at=f.base(P).offset(rel);NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(p,InteractionHand.MAIN_HAND,at,new BlockHitResult(at.getCenter(),Direction.UP,at,false)));}
    private static void take(LiteraryTests.Fixture f,ServerPlayer p){f.source(p,P);var b=f.base(P);p.teleportTo(f.in,b.getX()+.5,b.getY(),b.getZ()-6.5,180,0);clickFrom(f,p,LineSite.TAG);}

    @GameTest(template="empty",batch="line_deal",timeoutTicks=400)
    public static void dealtOnlyInTheDarkBandWithTheTagTheLineAndThePack(GameTestHelper h){
        var f=fixture(h);var p=f.player("line_deal",P);var b=f.base(P);
        h.startSequence().thenExecute(()->hold(f)).thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the space is resident"))
            .thenExecute(()->{
                h.assertTrue(LiteraryRooms.isLiterary(P)&&!LiteraryRooms.outside(P)&&DarkHalls.horror(P)&&P.kind()==LabyrinthPlace.Kind.ONE_SHOT,"an interior horror of the dark halls, in one visit");
                var story=WitnessAccount.Story.of(P.id());h.assertTrue(story!=null&&story.kind.equals("survival")&&WitnessAccount.Story.values().length==WitnessPool.SOURCES&&WitnessAccount.REQUIRED==WitnessPool.REQUIRED,"a survival story, and the Witness pool grows by one");
                h.assertTrue(LiteraryRooms.source(P).equals(LineSite.TAG)&&LiteraryRooms.ending(P).equals(LineSite.RECORDER),"the tag on the knot is the source and the tape the account");
                var d=new LabyrinthData();var id=UUID.randomUUID();
                for(int depth=1;depth<=17;depth++){d.pushReturn(id,new LabyrinthData.Waypoint(HouseDimensions.INTERIOR,new Vec3(depth,64,0),0F,true));h.assertTrue(LiteraryVignettes.canDeal(d,id,P)==(depth>=DarkHalls.DARK_DEPTH),"dealt only from depth sixteen (depth "+depth+")");}
                h.assertTrue(LineSite.length()>80,"a long line: "+LineSite.length());
                for(double s=0;s<LineSite.length()-1.5;s+=.5){var at=LineSite.at(s);var cell=b.offset((int)Math.floor(at.x),1,(int)Math.floor(at.z));h.assertTrue(f.in.getBlockState(cell).isAir()&&f.in.getBlockState(cell.below()).isAir(),"the line runs clear of every column, at "+s+": "+cell);}
                int columns=0;for(int x=-LineSite.SIDE;x<=LineSite.SIDE;x++)for(int z=LineSite.FAR_Z;z<=LineSite.NEAR_Z;z++)if(!f.in.getBlockState(b.offset(x,1,z)).isAir())columns++;
                h.assertTrue(columns>40,"columns stand about in the dark: "+columns);
                h.assertTrue(f.in.getBlockState(b.offset(LineSite.SIDE,1,-40)).isAir()&&!f.in.getBlockState(b.offset(LineSite.SIDE+1,1,-40)).isAir(),"the space is forty-nine wide");
                h.assertTrue(HorrorBlock.is(f.in.getBlockState(b.offset(LineSite.TAG)),HorrorBlock.Kind.TAG)&&HorrorBlock.is(f.in.getBlockState(b.offset(LineSite.PACK)),HorrorBlock.Kind.PACK)&&HorrorBlock.is(f.in.getBlockState(b.offset(LineSite.RECORDER)),HorrorBlock.Kind.RECORDER),"the tag on the frame, and the pack and recorder at the end");
                var text=LineTexts.source().get(DataComponents.WRITTEN_BOOK_CONTENT);var all=text==null?"":String.join(" ",text.pages().stream().map(page->page.raw().getString()).toList());
                h.assertTrue(all.contains("If it pulls, it's me")&&all.contains("If it pulls twice, it isn't"),"the tag's rules: "+all);
            }).thenSucceed();
    }
    @AfterBatch(batch="line_deal") public static void dealDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="line_hold",timeoutTicks=500)
    public static void holdingTheLineLeadsAndLettingGoIsTotalDark(GameTestHelper h){
        var f=fixture(h);var p=f.player("line_hold",P);var b=f.base(P);
        h.startSequence().thenExecute(()->hold(f)).thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the space is resident"))
            .thenExecute(()->{take(f,p);h.assertTrue(Line.holding(f.own(p,P)),"taking the tag, the reader holds the line");along(f,p,6);})
            .thenIdle(4)
            .thenExecute(()->{h.assertTrue(Line.holding(f.own(p,P))&&!p.hasEffect(MobEffects.BLINDNESS),"walking along it, they keep it");
                var at=LineSite.at(10);p.teleportTo(f.in,b.getX()+at.x+3,b.getY(),b.getZ()+at.z,180,0);})
            .thenIdle(3)
            .thenExecute(()->h.assertTrue(!Line.holding(f.own(p,P))&&p.hasEffect(MobEffects.BLINDNESS)&&f.own(p,P).getInt("LetGo212")==1,"off it, the hand loses it and the dark is total"))
            .thenExecute(()->along(f,p,10))
            .thenIdle(3)
            .thenExecute(()->h.assertTrue(Line.holding(f.own(p,P)),"walking back to it, the hand finds it again"))
            .thenExecute(()->{p.setSprinting(true);along(f,p,11);})
            .thenIdle(2)
            .thenExecute(()->{h.assertTrue(!Line.holding(f.own(p,P)),"nobody can run and keep hold of it");p.setSprinting(false);})
            .thenSucceed();
    }
    @AfterBatch(batch="line_hold") public static void holdDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="line_tug",timeoutTicks=900)
    public static void aThirdOfTheWayItTugsThenComesAlongTheLineFasterForStandingStill(GameTestHelper h){
        var f=fixture(h);var p=f.player("line_tug",P);double[] mark={0};
        h.startSequence().thenExecute(()->hold(f)).thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the space is resident"))
            .thenExecute(()->{take(f,p);along(f,p,LineSite.length()*.2);})
            .thenIdle(3)
            .thenExecute(()->h.assertTrue(Line.phase(set(f))==0,"short of a third, nothing pulls"))
            .thenExecute(()->along(f,p,LineSite.length()*.35))
            .thenWaitUntil(()->h.assertTrue(Line.phase(set(f))==1,"a third of the way along, it tugs"))
            .thenWaitUntil(()->h.assertTrue(Line.phase(set(f))==2,"once, again, then steadily"))
            .thenExecute(()->{double s=LineSite.length()*.35;h.assertTrue(Line.thing(set(f))<s-8,"something has hold of the line behind them: "+Line.thing(set(f)));mark[0]=Line.thing(set(f));})
            .thenIdle(20)
            .thenExecute(()->{double came=Line.thing(set(f))-mark[0];h.assertTrue(came>Line.COMES*20,"standing still, it comes along the line fast: "+came);
                p.setHealth(20);})
            // held a pace and a half off them along the line, so it drags without reaching
            .thenWaitUntil(()->{Line.edit(p.server,t->t.putDouble("T",LineSite.length()*.35-2.5));var own=f.own(p,P);
                h.assertTrue(own.getInt("Dragged212")>=1&&p.getHealth()<20,"close, it drags them back across the floor: "+own.getInt("Dragged212")+", "+p.getHealth());})
            .thenExecute(()->{Line.edit(p.server,t->t.putInt("Phase",0));h.assertTrue(p.getHealth()>=4F&&f.own(p,P).getInt("Reeled212")==0,"and the scraping never takes the last two hearts");})
            .thenSucceed();
    }
    @AfterBatch(batch="line_tug") public static void tugDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="line_reeled",timeoutTicks=600)
    public static void reeledAllTheWayTheReaderLosesDepthAndTheLineIsWoundRoundTheirArm(GameTestHelper h){
        var f=fixture(h);var p=f.player("line_reeled",P);int[] depth={0};
        h.startSequence().thenExecute(()->hold(f)).thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the space is resident"))
            .thenExecute(()->{for(int i=0;i<6;i++)f.data().pushReturn(p.getUUID(),new LabyrinthData.Waypoint(HouseDimensions.INTERIOR,new Vec3(i,64,i),0F,false));depth[0]=f.data().returnDepth(p.getUUID());
                p.getInventory().setItem(3,new ItemStack(Items.COMPASS));take(f,p);along(f,p,40);
                Line.edit(p.server,t->{t.putInt("Phase",2);t.putDouble("T",39.5);});})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getInt("Reeled212")==1,"it reaches them along the line"))
            .thenExecute(()->{var own=f.own(p,P);
                h.assertTrue(f.data().returnDepth(p.getUUID())<=depth[0]-Line.DEPTH_LOST,"they come to several doors shallower: "+f.data().returnDepth(p.getUUID())+" from "+depth[0]);
                h.assertTrue(own.contains("Tied212")&&own.getString("Gone212").equals("Compass")&&p.getInventory().getItem(3).isEmpty(),"something of theirs is gone: "+own.getString("Gone212"));
                boolean wound=false;int at=-1;var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(Line.wound(inv.getItem(i))){wound=true;at=i;}
                h.assertTrue(wound,"and the line is wound round their arm");
                var item=new ItemEntity(f.in,p.getX(),p.getY(),p.getZ(),inv.getItem(at).copy());var toss=new ItemTossEvent(item,p);inv.setItem(at,ItemStack.EMPTY);NeoForge.EVENT_BUS.post(toss);
                h.assertTrue(toss.isCanceled()&&Line.wound(p.getOffhandItem())||hasWound(p),"it will not come off");
                NeoForge.EVENT_BUS.post(new PlayerWakeUpEvent(p,false,false));
                h.assertTrue(!hasWound(p),"until they have slept");})
            .thenSucceed();
    }
    private static boolean hasWound(ServerPlayer p){var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(Line.wound(inv.getItem(i)))return true;return false;}
    @AfterBatch(batch="line_reeled") public static void reeledDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="line_end",timeoutTicks=600)
    public static void reachingThePackFirstTheTapeIsInTheReadersOwnHand(GameTestHelper h){
        var f=fixture(h);var p=f.player("line_end",P);
        h.startSequence().thenExecute(()->hold(f)).thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the space is resident"))
            .thenExecute(()->{take(f,p);var own=f.own(p,P);own.put("Tied212",new ItemStack(Items.CLOCK).save(p.registryAccess()));own.putString("Gone212","Clock");own.putInt("Reeled212",1);LiteraryVignettes.save(f.data(),p.getUUID(),P,own);
                along(f,p,LineSite.length()-.5);})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Ready"),"at the pack before it reaches them"))
            .thenExecute(()->{var own=f.own(p,P);h.assertTrue(own.getString("Outcome").equals("held_on")&&Line.phase(set(f))==3,"the line goes slack behind them: "+own);
                boolean clock=false;var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(inv.getItem(i).is(Items.CLOCK))clock=true;
                h.assertTrue(clock&&!own.contains("Tied212"),"what it took before is tied to the pack's strap");
                var text=LineTexts.accountText(p.getGameProfile().getName(),own);
                h.assertTrue(text.contains("Yours")&&text.contains("DAY "+(own.getLong("Found212")+4))&&text.contains("a pack")&&text.contains("clock"),"the tape is in their own hand, dated days from now, and it ends at the pack: "+text);
                h.assertTrue(!text.matches("(?s).*\\b(he|she|his|her|him|hers)\\b.*"),"and never gives the reader a gender");})
            .thenSucceed();
    }
    @AfterBatch(batch="line_end") public static void endDone(ServerLevel l){close();}
}
