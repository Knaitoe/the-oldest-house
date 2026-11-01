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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.213, your own light, played with real readers: dealt only in the dark band; the double level with them across the wall,
 * setting a torch across from theirs; halfway, turning back through a gap and taking their torches down on the way out; following it
 * back, every light gone and the fork again; and the visit home, with the note on the pillow.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_literary") @PrefixGameTestTemplate(false)
public final class OwnLightTests {
    private OwnLightTests(){}
    private static final LabyrinthPlace P=LabyrinthPlace.OWN_LIGHT;
    private static LiteraryTests.Fixture active;
    private static LiteraryTests.Fixture fixture(GameTestHelper h){if(active!=null)active.close();active=new LiteraryTests.Fixture(h);OwnLight.clearAll();return active;}
    private static void close(){if(active!=null){active.close();active=null;}OwnLight.clearAll();}
    private static void hold(LiteraryTests.Fixture f){var b=f.base(P);f.chunks.hold(f.in,new AABB(b.getX()-7,b.getY()-2,b.getZ()-52,b.getX()+8,b.getY()+6,b.getZ()+3));}
    private static void at(LiteraryTests.Fixture f,ServerPlayer p,double x,double z){var b=f.base(P);p.teleportTo(f.in,b.getX()+x,b.getY(),b.getZ()+z,180,0);}
    private static Vec3 rel(LiteraryTests.Fixture f,ServerPlayer p){return p.position().subtract(Vec3.atLowerCornerOf(f.base(P)));}
    private static LiteraryActor dbl(LiteraryTests.Fixture f,ServerPlayer p){var s=LiteraryVignettes.shared(f.data(),P);String id="Double_"+p.getUUID();return s.hasUUID(id)&&f.in.getEntity(s.getUUID(id)) instanceof LiteraryActor a?a:null;}
    private static void torch(LiteraryTests.Fixture f,ServerPlayer p,BlockPos rel){var at=f.base(P).offset(rel);f.in.setBlock(at,Blocks.TORCH.defaultBlockState(),3);
        var e=new BlockEvent.EntityPlaceEvent(BlockSnapshot.create(f.in.dimension(),f.in,at),Blocks.AIR.defaultBlockState(),p);NeoForge.EVENT_BUS.post(e);}

    @GameTest(template="empty",batch="own_deal",timeoutTicks=400)
    public static void dealtOnlyInTheDarkBandWithTwinBranchesAndTheWordsOnTheWall(GameTestHelper h){
        var f=fixture(h);var p=f.player("own_deal",P);var b=f.base(P);
        h.startSequence().thenExecute(()->hold(f)).thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the fork is resident"))
            .thenExecute(()->{
                h.assertTrue(LiteraryRooms.isLiterary(P)&&!LiteraryRooms.outside(P)&&DarkHalls.horror(P)&&P.kind()==LabyrinthPlace.Kind.ONE_SHOT,"an interior horror of the dark halls, in one visit");
                var story=WitnessAccount.Story.of(P.id());h.assertTrue(story!=null&&story.kind.equals("understanding")&&WitnessAccount.Story.values().length==WitnessPool.SOURCES&&WitnessAccount.REQUIRED==WitnessPool.REQUIRED,"an understanding story, and the Witness pool grows by one");
                h.assertTrue(LiteraryRooms.source(P).equals(OwnLightSite.SCRATCH)&&LiteraryRooms.ending(P).equals(OwnLightSite.STUB),"the words on the wall are the source and the stub the account");
                var d=new LabyrinthData();var id=UUID.randomUUID();
                for(int depth=1;depth<=17;depth++){d.pushReturn(id,new LabyrinthData.Waypoint(HouseDimensions.INTERIOR,new Vec3(depth,64,0),0F,true));h.assertTrue(LiteraryVignettes.canDeal(d,id,P)==(depth>=DarkHalls.DARK_DEPTH),"dealt only from depth sixteen (depth "+depth+")");}
                h.assertTrue(!f.in.getBlockState(b.offset(0,1,-20)).isAir()&&f.in.getBlockState(b.offset(0,0,-21)).isAir()&&f.in.getBlockState(b.offset(0,1,-21)).isAir()&&!f.in.getBlockState(b.offset(0,2,-21)).isAir(),"a wall between the branches, broken by low gaps");
                h.assertTrue(f.in.getBlockState(b.offset(-3,1,-30)).isAir()&&f.in.getBlockState(b.offset(3,1,-30)).isAir()&&f.in.getBlockState(b.offset(0,1,-47)).isAir(),"two branches side by side, meeting in the far room");
                h.assertTrue(HorrorBlock.is(f.in.getBlockState(b.offset(OwnLightSite.SCRATCH)),HorrorBlock.Kind.SCRATCH)&&HorrorBlock.is(f.in.getBlockState(b.offset(OwnLightSite.STUB)),HorrorBlock.Kind.STUB),"the words on the wall and the stub at the end");
                var text=OwnLightTexts.source().get(DataComponents.WRITTEN_BOOK_CONTENT);var all=text==null?"":String.join(" ",text.pages().stream().map(page->page.raw().getString()).toList());
                h.assertTrue(all.contains("BRING YOUR OWN LIGHT")&&all.contains("it will bring its own"),"scratched into the wall: "+all);
            }).thenSucceed();
    }
    @AfterBatch(batch="own_deal") public static void dealDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="own_level",timeoutTicks=600)
    public static void theDoubleWalksLevelABeatLateThenEarlyAndSetsATorchAcross(GameTestHelper h){
        var f=fixture(h);var p=f.player("own_level",P);double[] z={-6.5};
        h.startSequence().thenExecute(()->hold(f)).thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the fork is resident"))
            .thenExecute(()->at(f,p,-2.5,-6.5))
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Run213")&&f.own(p,P).getInt("Branch213")==-1,"into the west branch"))
            .thenWaitUntil(()->{if(z[0]>-10.5){z[0]-=.2;at(f,p,-2.5,z[0]);}var a=dbl(f,p);
                h.assertTrue(a!=null&&a.position().x-f.base(P).getX()>2&&z[0]<=-10.5,"someone walks the other branch");})
            .thenExecute(()->{var a=dbl(f,p);double dz=a.position().z-f.base(P).getZ();h.assertTrue(dz>z[0]+.5,"a beat late: "+dz+" behind "+z[0]);
                torch(f,p,new BlockPos(-3,0,-9));})
            .thenIdle(2)
            .thenExecute(()->{h.assertTrue(OwnLight.torches(f.own(p,P)).contains(new BlockPos(-3,0,-9)),"the reader's torch is remembered");
                h.assertTrue(f.in.getBlockState(f.base(P).offset(3,0,-9)).is(Blocks.TORCH),"and across the wall, a torch goes down too");})
            .thenWaitUntil(()->{if(z[0]>-18.5){z[0]-=.2;at(f,p,-2.5,z[0]);}
                h.assertTrue(z[0]<=-18.5,"walking on");})
            .thenExecute(()->{var a=dbl(f,p);double dz=a.position().z-f.base(P).getZ();h.assertTrue(f.own(p,P).getBoolean("Early213"),"then it is a beat early");h.assertTrue(dz<-18.5+.01,"ahead of them now: "+dz);})
            .thenSucceed();
    }
    @AfterBatch(batch="own_level") public static void levelDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="own_turn",timeoutTicks=1200)
    public static void halfwayItTurnsBackTakesTheirTorchesAndTheFarWallHasThem(GameTestHelper h){
        var f=fixture(h);var p=f.player("own_turn",P);var b=f.base(P);
        h.startSequence().thenExecute(()->hold(f)).thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the fork is resident"))
            .thenExecute(()->at(f,p,-2.5,-6.5))
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Run213"),"into the west branch"))
            .thenExecute(()->{torch(f,p,new BlockPos(-4,0,-8));at(f,p,-2.5,-16.5);})
            .thenIdle(2)
            .thenExecute(()->{torch(f,p,new BlockPos(-3,0,-17));at(f,p,-2.5,-26.5);})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Turned213"),"halfway it stops matching"))
            .thenExecute(()->{var a=dbl(f,p);h.assertTrue(a!=null&&Math.abs(a.position().x-b.getX()-.5)<.6,"it comes through a gap behind them");at(f,p,-2.5,-30.5);})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Gone213"),"it walks back the way they came and goes out through the way in"))
            .thenExecute(()->{h.assertTrue(f.in.getBlockState(b.offset(-4,0,-8)).isAir()&&f.in.getBlockState(b.offset(-3,0,-17)).isAir()&&f.own(p,P).getInt("Taken213")==2,"taking their torches down on the way: "+f.own(p,P).getInt("Taken213"));
                h.assertTrue(f.in.getBlockState(b.offset(3,0,-17)).isAir(),"its own go out with it");
                at(f,p,.5,-46.5);})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Ready"),"going on without looking back"))
            .thenExecute(()->{var own=f.own(p,P);h.assertTrue(own.getString("Outcome").equals("let_it_take_them")&&own.getInt("SetBack213")==2,"the outcome: "+own);
                var wall=OwnLightSite.wall();h.assertTrue(f.in.getBlockState(b.offset(wall.get(0))).is(Blocks.WALL_TORCH)&&f.in.getBlockState(b.offset(wall.get(1))).is(Blocks.WALL_TORCH)&&f.in.getBlockState(b.offset(wall.get(2))).isAir(),"in the far wall, their torches, in the order they were placed");
                h.assertTrue(own.getString("Note213").equals("Not yet.")&&own.getBoolean("Home213"),"and it goes home first, to leave a note");})
            .thenSucceed();
    }
    @AfterBatch(batch="own_turn") public static void turnDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="own_follow",timeoutTicks=800)
    public static void followingItBackEveryLightGoesAndTheyComeToAtTheFork(GameTestHelper h){
        var f=fixture(h);var p=f.player("own_follow",P);var b=f.base(P);
        h.startSequence().thenExecute(()->hold(f)).thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the fork is resident"))
            .thenExecute(()->at(f,p,-2.5,-6.5))
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Run213"),"into the west branch"))
            .thenExecute(()->{torch(f,p,new BlockPos(-4,0,-10));at(f,p,-2.5,-26.5);})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Turned213"),"halfway it turns back"))
            .thenExecute(()->at(f,p,-2.5,-18.5))
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Followed213"),"following it back"))
            .thenExecute(()->{var r=rel(f,p);h.assertTrue(OwnLightSite.inLobby(r),"they come to at the fork: "+r);
                h.assertTrue(f.in.getBlockState(b.offset(-4,0,-10)).isAir()&&dbl(f,p)==null,"every torch gone, and it with them");
                h.assertTrue(f.own(p,P).getString("Note213").equals("Thank you for the light."),"it still goes home first");
                at(f,p,.5,-46.5);})
            .thenIdle(10)
            .thenExecute(()->h.assertTrue(!f.own(p,P).getBoolean("Ready"),"and the far room holds nothing for a reader who followed"))
            .thenSucceed();
    }
    @AfterBatch(batch="own_follow") public static void followDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="own_home",timeoutTicks=600)
    public static void atHomeTheBedIsSleptInAChestTurnedRoundTheLightsOutAndANote(GameTestHelper h){
        var f=fixture(h);var p=f.player("own_home",P);var b=f.base(P);
        h.startSequence().thenExecute(()->hold(f)).thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the fork is resident"))
            .thenExecute(()->{var bed=b.offset(-2,0,-2);var chest=b.offset(2,0,-2);var candle=b.offset(-3,0,-3);
                f.in.setBlock(chest,Blocks.CHEST.defaultBlockState(),3);var c=(ChestBlockEntity)f.in.getBlockEntity(chest);c.setItem(0,new ItemStack(Items.APPLE));c.setItem(1,new ItemStack(Items.BREAD));
                f.in.setBlock(candle,Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.LIT,true),3);
                var own=f.own(p,P);own.putBoolean("Home213",true);own.putString("Note213","Not yet.");LiteraryVignettes.save(f.data(),p.getUUID(),P,own);
                p.setRespawnPosition(f.in.dimension(),bed,0,true,false);at(f,p,-1.5,-1.5);})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getString("Left213").equals("Not yet."),"at their bed, it has been"))
            .thenExecute(()->{var c=(ChestBlockEntity)f.in.getBlockEntity(b.offset(2,0,-2));int n=c.getContainerSize();
                h.assertTrue(c.getItem(n-1).is(Items.APPLE)&&c.getItem(n-2).is(Items.BREAD)&&c.getItem(0).isEmpty(),"a chest turned round, nothing taken");
                h.assertTrue(!f.in.getBlockState(b.offset(-3,0,-3)).getValue(CandleBlock.LIT),"the lights left burning, out");
                var notes=f.in.getEntitiesOfClass(ItemEntity.class,new AABB(b.offset(-2,0,-2)).inflate(2),e->e.getItem().has(DataComponents.WRITTEN_BOOK_CONTENT));
                h.assertTrue(!notes.isEmpty(),"and a note on the pillow");
                var page=notes.get(0).getItem().get(DataComponents.WRITTEN_BOOK_CONTENT).pages().get(0).raw().getString();
                h.assertTrue(page.contains("Not yet.")&&page.contains("your own handwriting"),"in their own hand: "+page);
                var own=f.own(p,P);var text=OwnLightTexts.accountText(p.getGameProfile().getName(),own);
                h.assertTrue(text.contains("Not yet.")&&!text.matches("(?s).*\\b(he|she|his|her|him|hers)\\b.*"),"the account pastes the note in, and never gives the reader a gender");})
            .thenSucceed();
    }
    @AfterBatch(batch="own_home") public static void homeDone(ServerLevel l){close();}
}
