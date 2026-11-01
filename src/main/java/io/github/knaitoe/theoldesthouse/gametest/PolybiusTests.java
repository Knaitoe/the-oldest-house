package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.217, Polybius, played with real readers: the arcade, its three working cabinets and the black one; scores that go on the
 * whole server's tables only when the server believes them, one place each, best kept; three plays of the black cabinet and a walk to
 * the front, which leaves the men's clipboard; and a fourth play, which takes the evening and every place the reader held.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_literary") @PrefixGameTestTemplate(false)
public final class PolybiusTests {
    private PolybiusTests(){}
    private static final LabyrinthPlace P=LabyrinthPlace.POLYBIUS;
    private static LiteraryTests.Fixture active;
    private static LiteraryTests.Fixture fixture(GameTestHelper h){if(active!=null)active.close();active=new LiteraryTests.Fixture(h);Polybius.clearAll();return active;}
    private static void close(){if(active!=null){active.close();active=null;}Polybius.clearAll();}
    private static void hold(LiteraryTests.Fixture f){var b=f.base(P);f.chunks.hold(f.in,new AABB(b.getX()-10,b.getY()-2,b.getZ()-25,b.getX()+11,b.getY()+7,b.getZ()+3));}
    private static void at(LiteraryTests.Fixture f,ServerPlayer p,double x,double z){var b=f.base(P);p.teleportTo(f.in,b.getX()+x,b.getY(),b.getZ()+z,180,0);}
    private static void cabinet(LiteraryTests.Fixture f,ServerPlayer p,int game){f.click(p,P,PolybiusSite.CABINETS.get(game));}
    private static long token(ServerPlayer p){var s=Polybius.session(p.getUUID());return s==null?0:s.token();}
    private static int men(LiteraryTests.Fixture f,ServerPlayer p){var b=f.base(P);return f.in.getEntitiesOfClass(LiteraryActor.class,new AABB(b).inflate(30),a->a.owner().isPresent()&&a.owner().get().equals(p.getUUID())&&(a.phase()==Polybius.MAN_A_PHASE||a.phase()==Polybius.MAN_B_PHASE)).size();}

    @GameTest(template="empty",batch="poly_build",timeoutTicks=400)
    public static void theArcadeHasThreeWorkingCabinetsTheirTablesAndABlackOne(GameTestHelper h){
        var f=fixture(h);var p=f.player("poly_build",P);var b=f.base(P);
        h.startSequence().thenExecute(()->hold(f)).thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the arcade is resident"))
            .thenExecute(()->{
                h.assertTrue(LiteraryRooms.isLiterary(P)&&!LiteraryRooms.outside(P)&&!DarkHalls.horror(P)&&P.kind()==LabyrinthPlace.Kind.MULTI_VISIT,"an ordinary literary room, over as many visits as it takes");
                var story=WitnessAccount.Story.of(P.id());h.assertTrue(story!=null&&story.kind.equals("survival")&&WitnessAccount.Story.values().length==WitnessPool.SOURCES&&WitnessAccount.REQUIRED==WitnessPool.REQUIRED,"a survival story, and the Witness pool grows by one");
                h.assertTrue(LiteraryRooms.source(P).equals(PolybiusSite.NOTICE)&&LiteraryRooms.ending(P).equals(PolybiusSite.CLIPBOARD),"the notice board is the source and the clipboard the account");
                for(int g=0;g<PolybiusSite.GAMES;g++){var c=PolybiusSite.CABINETS.get(g);var low=f.in.getBlockState(b.offset(c));var high=f.in.getBlockState(b.offset(c).above());
                    h.assertTrue(PolybiusBlock.is(low,PolybiusBlock.Kind.CABINET)&&PolybiusBlock.is(high,PolybiusBlock.Kind.SCREEN)&&low.getValue(PolybiusBlock.STAGE)==g&&high.getValue(PolybiusBlock.STAGE)==g,"cabinet "+g+", body and screen");
                    var at=Vec3.atBottomCenterOf(b.offset(c)).add(0,2.15,0);int board=g;
                    h.assertTrue(!f.in.getEntitiesOfClass(Display.TextDisplay.class,new AABB(at,at).inflate(1),e->e.getTags().contains("polybius_board_"+board)).isEmpty(),"a high-score table over cabinet "+g);}
                h.assertTrue(PolybiusBlock.is(f.in.getBlockState(b.offset(PolybiusSite.NOTICE)),PolybiusBlock.Kind.NOTICE)&&PolybiusBlock.is(f.in.getBlockState(b.offset(PolybiusSite.CLIPBOARD)),PolybiusBlock.Kind.CLIPBOARD),"the notice board, and the clipboard's place on the change machine");
                h.assertTrue(f.in.getBlockState(b.offset(0,0,-3)).isAir()&&f.in.getBlockState(b.offset(0,1,-3)).isAir()&&f.in.getBlockState(b.offset(0,1,-17)).isAir(),"the way in and the way down the middle to the black cabinet are clear");
                var text=PolybiusTexts.source().get(DataComponents.WRITTEN_BOOK_CONTENT);var all=text==null?"":String.join(" ",text.pages().stream().map(page->page.raw().getString()).toList());
                h.assertTrue(all.contains("GAME TAKEN OUT OF MALL ARCADE")&&all.contains("two men in suits")&&all.contains("DON'T"),"the clipping and the card: "+all);
            }).thenSucceed();
    }
    @AfterBatch(batch="poly_build") public static void buildDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="poly_scores",timeoutTicks=600)
    public static void aScoreGoesOnTheServersTableOnlyWhenTheServerBelievesIt(GameTestHelper h){
        var f=fixture(h);var p=f.player("poly_alice",P);var q=f.player("poly_bram",P);var s=h.getLevel().getServer();
        h.startSequence().thenExecute(()->hold(f)).thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the arcade is resident"))
            .thenExecute(()->{
                cabinet(f,p,PolybiusSite.MOTHLIGHT);var ses=Polybius.session(p.getUUID());h.assertTrue(ses!=null&&ses.game()==PolybiusSite.MOTHLIGHT,"the cabinet opens a game for its reader");
                cabinet(f,q,PolybiusSite.MOTHLIGHT);h.assertTrue(Polybius.session(q.getUUID())==null,"one reader plays a cabinet at a time");
                h.assertTrue(!Polybius.submit(p,token(p)+1,50,30),"a score with another session's token is not believed");
                h.assertTrue(!Polybius.submit(p,token(p),100000,30)&&PolybiusScores.table(s,PolybiusSite.MOTHLIGHT).isEmpty(),"nor one faster than the game can score");
                cabinet(f,p,PolybiusSite.MOTHLIGHT);h.assertTrue(Polybius.submit(p,token(p),120,30),"a believable game over stands");
                cabinet(f,q,PolybiusSite.MOTHLIGHT);h.assertTrue(Polybius.submit(q,token(q),90,30),"and the next reader's");
                cabinet(f,p,PolybiusSite.MOTHLIGHT);h.assertTrue(Polybius.submit(p,token(p),60,30),"a worse game stands too");
                var t=PolybiusScores.table(s,PolybiusSite.MOTHLIGHT);
                h.assertTrue(t.size()==2&&t.get(0).id().equals(p.getUUID())&&t.get(0).score()==120&&t.get(1).id().equals(q.getUUID()),"one place each, best kept, in order: "+t);
                h.assertTrue(PolybiusScores.table(s,PolybiusSite.UNDERTOW).isEmpty()&&PolybiusScores.tag(s,PolybiusSite.BLACK).getList("Rows",10).isEmpty(),"each game keeps its own table, and the black cabinet none");
                var b=f.base(P);var at=Vec3.atBottomCenterOf(b.offset(PolybiusSite.CABINETS.get(PolybiusSite.MOTHLIGHT))).add(0,2.15,0);
                var board=f.in.getEntitiesOfClass(Display.TextDisplay.class,new AABB(at,at).inflate(1),e->e.getTags().contains("polybius_board_0"));
                var tag=new net.minecraft.nbt.CompoundTag();if(!board.isEmpty())board.get(0).saveWithoutId(tag);
                h.assertTrue(tag.getString("text").contains("poly_alice"),"the table over the cabinet shows the top name: "+tag.getString("text"));
                f.reload();h.assertTrue(PolybiusScores.table(s,PolybiusSite.MOTHLIGHT).size()==2,"the tables are the world's, kept across a reload");
            }).thenSucceed();
    }
    @AfterBatch(batch="poly_scores") public static void scoresDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="poly_walk",timeoutTicks=1200)
    public static void threePlaysAndAWalkToTheFrontLeaveTheClipboard(GameTestHelper h){
        var f=fixture(h);var p=f.player("poly_walk",P);
        var seq=h.startSequence().thenExecute(()->hold(f)).thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the arcade is resident"))
            .thenWaitUntil(()->h.assertTrue(men(f,p)==2,"two men in suits stand by the black cabinet, the reader's own"));
        for(int i=1;i<=Polybius.PLAYS;i++){int n=i;
            seq=seq.thenExecute(()->cabinet(f,p,PolybiusSite.BLACK)).thenIdle(Polybius.MIN_PLAY-30)
                .thenExecute(()->{h.assertTrue(Polybius.submit(p,token(p),40,Polybius.MIN_PLAY),"a play of the black cabinet that lasted counts");h.assertTrue(f.own(p,P).getInt("Plays217")==n,"play "+n);});
        }
        seq.thenExecute(()->{h.assertTrue(f.own(p,P).contains("Call217")&&!f.own(p,P).getBoolean("Ready"),"after three, it calls them by name");at(f,p,.5,-1.5);})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Ready"),"walking to the front without a fourth play"))
            .thenWaitUntil(()->h.assertTrue(men(f,p)==0,"and the men are gone"))
            .thenExecute(()->{var own=f.own(p,P);h.assertTrue(own.getString("Outcome").equals("walked_away"),"the outcome: "+own);
                String text=PolybiusTexts.form("poly_walk",own)+"\n"+PolybiusTexts.afterwards("poly_walk",own);
                h.assertTrue(text.contains("SUBJECT: poly_walk")&&text.contains("stopped")&&!text.matches("(?s).*\\b(he|she|his|her|him|hers)\\b.*"),"the clipboard names the reader and never gives them a gender: "+text);
                cabinet(f,p,PolybiusSite.BLACK);h.assertTrue(Polybius.session(p.getUUID())==null,"the black cabinet is dark to a reader who walked away");})
            .thenSucceed();
    }
    @AfterBatch(batch="poly_walk") public static void walkDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="poly_fourth",timeoutTicks=1400)
    public static void aFourthPlayTakesTheEveningAndEveryPlaceOnTheTables(GameTestHelper h){
        var f=fixture(h);var p=f.player("poly_fourth",P);var s=h.getLevel().getServer();
        var seq=h.startSequence().thenExecute(()->hold(f)).thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the arcade is resident"))
            .thenExecute(()->{cabinet(f,p,PolybiusSite.SIGNAL);h.assertTrue(Polybius.submit(p,token(p),75,30)&&PolybiusScores.rank(s,PolybiusSite.SIGNAL,p.getUUID())==1,"a place on the signal table first");});
        for(int i=1;i<=Polybius.PLAYS;i++)seq=seq.thenExecute(()->cabinet(f,p,PolybiusSite.BLACK)).thenIdle(Polybius.MIN_PLAY-30).thenExecute(()->Polybius.submit(p,token(p),40,Polybius.MIN_PLAY));
        seq.thenExecute(()->{h.assertTrue(f.own(p,P).getInt("Plays217")==Polybius.PLAYS,"three plays");cabinet(f,p,PolybiusSite.BLACK);
                var ses=Polybius.session(p.getUUID());h.assertTrue(ses!=null&&ses.taken(),"the fourth is the one it keeps");
                h.assertTrue(!Polybius.submit(p,token(p),40,Polybius.MIN_PLAY),"and no game over from the screen ends it");})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Lost217"),"it takes the evening"))
            .thenExecute(()->{var own=f.own(p,P);var b=f.base(P);
                h.assertTrue(PolybiusScores.table(s,PolybiusSite.SIGNAL).isEmpty()&&own.getInt("Struck217")==1,"every place the reader held is gone from the tables");
                h.assertTrue(!own.getBoolean("Ready")&&own.getInt("Plays217")==0&&p.position().z-b.getZ()>-3,"no account; they come to at the front: "+p.position());
                cabinet(f,p,PolybiusSite.BLACK);h.assertTrue(Polybius.session(p.getUUID())==null,"the screen is dark for the rest of the evening");})
            .thenWaitUntil(()->h.assertTrue(men(f,p)==0,"the men are gone with it"))
            .thenExecute(()->{f.arrive(p,P);var own=f.own(p,P);h.assertTrue(!own.getBoolean("Lost217")&&own.getInt("Plays217")==0&&own.getInt("Taken217")==1,"another night, and it starts again from nothing");})
            .thenWaitUntil(()->h.assertTrue(men(f,p)==2,"and the men are back"))
            .thenSucceed();
    }
    @AfterBatch(batch="poly_fourth") public static void fourthDone(ServerLevel l){close();}
}
