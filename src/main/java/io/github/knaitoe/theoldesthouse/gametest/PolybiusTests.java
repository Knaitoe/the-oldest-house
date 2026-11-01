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

    /** A key log, as the screen sends it: pairs of how many ticks and which keys. */
    private static java.util.List<Integer> keys(int... pairs){var out=new java.util.ArrayList<Integer>();for(int i=0;i+1<pairs.length;i+=2)out.add(pairs[i]<<5|pairs[i+1]);return out;}
    /** Idle for so many ticks: a play of the black cabinet, as far as the server is concerned. */
    private static java.util.List<Integer> idle(int ticks){return keys(ticks,0);}
    /** What the server will make of a log for a reader's open session. */
    private static int replayed(ServerPlayer p,java.util.List<Integer> log){var ses=Polybius.session(p.getUUID());var r=PolybiusGames.replay(ses.game(),ses.token(),log,Polybius.LONGEST[ses.game()]);return r==null?-1:r.score();}
    private static int count(ServerPlayer p,net.minecraft.world.item.Item item){int n=0;var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(inv.getItem(i).is(item))n+=inv.getItem(i).getCount();return n;}

    @GameTest(template="empty",batch="poly_party",timeoutTicks=900)
    public static void blockPartySeatsFourPlaysOnTheServerAndPaysInTickets(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);ServerPlayer[] r=new ServerPlayer[5];for(int i=0;i<5;i++)r[i]=f.player("poly_party"+i,P);
        double[] x0={0};
        h.startSequence().thenExecute(()->hold(f)).thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the arcade is resident"))
            .thenExecute(()->{
                h.assertTrue(PolybiusBlock.is(f.in.getBlockState(b.offset(PolybiusSite.BRAWL)),PolybiusBlock.Kind.BRAWL)&&PolybiusBlock.is(f.in.getBlockState(b.offset(PolybiusSite.BRAWL_RIGHT).above()),PolybiusBlock.Kind.BRAWL_SCREEN),"Block Party stands two blocks wide on the west wall");
                for(var p:r)f.click(p,P,PolybiusSite.BRAWL);
                var seats=new java.util.HashSet<Integer>();for(int i=0;i<4;i++)seats.add(PolybiusBrawl.seat(r[i].getUUID()));
                h.assertTrue(seats.equals(java.util.Set.of(0,1,2,3))&&PolybiusBrawl.seat(r[4].getUUID())<0,"four readers take the four sticks and a fifth is turned away: "+seats);
                f.click(r[0],P,PolybiusSite.CABINETS.get(PolybiusSite.MOTHLIGHT));h.assertTrue(Polybius.session(r[0].getUUID())==null,"a reader at Block Party cannot play another cabinet at once");at(f,r[0],-6.5,-15.5);
                PolybiusBrawl.start(r[0].getUUID());x0[0]=PolybiusBrawl.fighterX(r[0].getUUID());PolybiusBrawl.keys(r[0],PolybiusBrawl.RIGHT);})
            .thenIdle(10)
            .thenExecute(()->{h.assertTrue(PolybiusBrawl.fighterX(r[0].getUUID())>x0[0]+8,"the keys a reader holds move their fighter, on the server: "+x0[0]+" to "+PolybiusBrawl.fighterX(r[0].getUUID()));
                PolybiusBrawl.keys(r[0],0);PolybiusBrawl.foe(r[0].getUUID(),PolybiusBrawl.ZOMBIE,12);PolybiusBrawl.keys(r[0],PolybiusBrawl.PUNCH);})
            .thenIdle(2).thenExecute(()->PolybiusBrawl.keys(r[0],0)).thenIdle(12).thenExecute(()->PolybiusBrawl.keys(r[0],PolybiusBrawl.PUNCH))
            .thenIdle(2).thenExecute(()->PolybiusBrawl.keys(r[0],0)).thenIdle(6)
            .thenExecute(()->{h.assertTrue(PolybiusBrawl.score(r[0].getUUID())>=100,"two punches knock a zombie down and it is scored: "+PolybiusBrawl.score(r[0].getUUID()));
                PolybiusBrawl.keys(r[3],PolybiusBrawl.LEAVE);h.assertTrue(PolybiusBrawl.seat(r[3].getUUID())<0,"closing the screen gives the seat up");
                f.click(r[4],P,PolybiusSite.BRAWL);h.assertTrue(PolybiusBrawl.seat(r[4].getUUID())==3,"and the fifth reader drops into it");
                PolybiusBrawl.end(r[0].getUUID());})
            .thenWaitUntil(()->h.assertTrue(PolybiusBrawl.seat(r[0].getUUID())<0,"when every fighter is out of lives the game ends and the seats empty"))
            .thenExecute(()->{h.assertTrue(count(r[0],PolybiusRegistry.TICKETS.get())>=1&&count(r[1],PolybiusRegistry.TICKETS.get())==0,"the one who knocked something down is paid in tickets, and nobody is paid for nothing");})
            .thenSucceed();
    }
    @AfterBatch(batch="poly_party") public static void partyDone(ServerLevel l){close();}

    /** 0.4.224: classes picked before the start, a chain whose third hit is the finisher, a special, and items walked over. */
    @GameTest(template="empty",batch="poly_classes",timeoutTicks=900)
    public static void blockPartyHasClassesChainsSpecialsAndItems(GameTestHelper h){
        var f=fixture(h);ServerPlayer a=f.player("poly_class0",P),k=f.player("poly_class1",P);var A=a.getUUID();var K=k.getUUID();double[] x0={0};
        h.startSequence().thenExecute(()->hold(f)).thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the arcade is resident"))
            .thenExecute(()->{f.click(a,P,PolybiusSite.BRAWL);f.click(k,P,PolybiusSite.BRAWL);at(f,a,-6.5,-15.5);at(f,k,-6.5,-15.5);
                h.assertTrue(PolybiusBrawl.fighterClass(A)==PolybiusBrawl.BRAWLER&&PolybiusBrawl.fighterClass(K)==PolybiusBrawl.KNIGHT,"each stick starts on a class of its own");
                PolybiusBrawl.keys(a,PolybiusBrawl.RIGHT);PolybiusBrawl.keys(k,PolybiusBrawl.LEFT);})
            .thenIdle(1).thenExecute(()->{PolybiusBrawl.keys(a,0);PolybiusBrawl.keys(k,0);})
            .thenIdle(1).thenExecute(()->PolybiusBrawl.keys(a,PolybiusBrawl.RIGHT))
            .thenIdle(1).thenExecute(()->{PolybiusBrawl.keys(a,0);
                h.assertTrue(PolybiusBrawl.fighterClass(A)==PolybiusBrawl.ARCHER&&PolybiusBrawl.fighterClass(K)==PolybiusBrawl.BRAWLER,"left and right, while the game waits, pick who you are: "+PolybiusBrawl.fighterClass(A)+", "+PolybiusBrawl.fighterClass(K));
                h.assertTrue(PolybiusBrawl.fighterHp(A)==16&&PolybiusBrawl.fighterHp(K)==20,"and each class has its own health");
                PolybiusBrawl.start(A);PolybiusBrawl.calm(A);PolybiusBrawl.foe(K,PolybiusBrawl.ZOMBIE,12,40);PolybiusBrawl.keys(k,PolybiusBrawl.PUNCH);})
            .thenIdle(2).thenExecute(()->PolybiusBrawl.keys(k,0)).thenIdle(11).thenExecute(()->PolybiusBrawl.keys(k,PolybiusBrawl.PUNCH))
            .thenIdle(2).thenExecute(()->PolybiusBrawl.keys(k,0)).thenIdle(11).thenExecute(()->PolybiusBrawl.keys(k,PolybiusBrawl.PUNCH))
            .thenIdle(2).thenExecute(()->PolybiusBrawl.keys(k,0)).thenIdle(4)
            .thenExecute(()->{h.assertTrue(PolybiusBrawl.combo(K)==3&&PolybiusBrawl.foeHp(K)==40-5-5-9,"three punches in a row are a chain, and the third is the uppercut: chain "+PolybiusBrawl.combo(K)+", zombie at "+PolybiusBrawl.foeHp(K));
                x0[0]=PolybiusBrawl.fighterX(K);PolybiusBrawl.keys(k,PolybiusBrawl.SPECIAL);})
            .thenIdle(2).thenExecute(()->PolybiusBrawl.keys(k,0)).thenIdle(8)
            .thenExecute(()->{h.assertTrue(PolybiusBrawl.fighterX(K)>x0[0]+20,"the brawler's special is a dash: "+x0[0]+" to "+PolybiusBrawl.fighterX(K));
                PolybiusBrawl.calm(A);PolybiusBrawl.wound(A,10);PolybiusBrawl.drop(A,PolybiusBrawl.STEAK,0);})
            .thenIdle(2).thenExecute(()->{h.assertTrue(PolybiusBrawl.fighterHp(A)==12&&PolybiusBrawl.drops(A)==0,"a steak, walked over, heals: "+PolybiusBrawl.fighterHp(A));
                PolybiusBrawl.drop(A,PolybiusBrawl.TOTEM,0);})
            .thenIdle(2).thenExecute(()->{h.assertTrue(PolybiusBrawl.lives(A)==4,"a totem is a life: "+PolybiusBrawl.lives(A));
                PolybiusBrawl.drop(A,PolybiusBrawl.TNT,0);})
            .thenIdle(2).thenExecute(()->{PolybiusBrawl.foe(A,PolybiusBrawl.ZOMBIE,30,40);PolybiusBrawl.keys(a,PolybiusBrawl.PUNCH);})
            .thenIdle(2).thenExecute(()->PolybiusBrawl.keys(a,0)).thenIdle(26)
            .thenExecute(()->h.assertTrue(PolybiusBrawl.foeHp(A)==28,"carried TNT goes with the next attack, in place of an arrow, and goes up where it lands: zombie at "+PolybiusBrawl.foeHp(A)))
            .thenSucceed();
    }
    @AfterBatch(batch="poly_classes") public static void classesDone(ServerLevel l){close();}

    /** 0.4.224: the pizza counter, its sullen teenager, and what a slice and a soda cost. */
    @GameTest(template="empty",batch="poly_pizza",timeoutTicks=600)
    public static void thePizzaCounterSellsGreasySlicesForTickets(GameTestHelper h){
        var f=fixture(h);var p=f.player("poly_pizza",P);var b=f.base(P);
        h.startSequence().thenExecute(()->hold(f)).thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the arcade is resident"))
            .thenWaitUntil(()->h.assertTrue(f.in.getEntitiesOfClass(LiteraryActor.class,new AABB(b).inflate(30),a->a.phase()==Polybius.TEEN_PHASE&&a.owner().isEmpty()).size()==1,"one teenager behind the pizza counter, the same for everyone"))
            .thenExecute(()->{
                h.assertTrue(PolybiusBlock.is(f.in.getBlockState(b.offset(PolybiusSite.WARMER)),PolybiusBlock.Kind.PIZZA_WARMER)&&PolybiusBlock.is(f.in.getBlockState(b.offset(PolybiusSite.SODA)),PolybiusBlock.Kind.SODA),"a warmer of slices and a soda fountain on the counter");
                f.click(p,P,PolybiusSite.WARMER);
                h.assertTrue(count(p,PolybiusRegistry.PIZZA.get())==0,"no tickets, no slice");
                p.getInventory().add(new net.minecraft.world.item.ItemStack(PolybiusRegistry.TICKETS.get(),8));
                f.click(p,P,PolybiusSite.WARMER);
                h.assertTrue(count(p,PolybiusRegistry.PIZZA.get())==1&&count(p,PolybiusRegistry.TICKETS.get())==8-PolybiusRegistry.PIZZA_COST,"a slice for "+PolybiusRegistry.PIZZA_COST+" tickets");
                f.click(p,P,PolybiusSite.SODA);
                h.assertTrue(count(p,PolybiusRegistry.SODA.get())==1&&count(p,PolybiusRegistry.TICKETS.get())==8-PolybiusRegistry.PIZZA_COST-PolybiusRegistry.SODA_COST,"and a soda for "+PolybiusRegistry.SODA_COST);
                var slice=new net.minecraft.world.item.ItemStack(PolybiusRegistry.PIZZA.get());
                h.assertTrue(slice.getFoodProperties(p)!=null&&slice.getFoodProperties(p).nutrition()>=6,"the slice is a meal");
            }).thenSucceed();
    }
    @AfterBatch(batch="poly_pizza") public static void pizzaDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="poly_prizes",timeoutTicks=600)
    public static void ticketsBuyWhatIsOnTheShelfAndTheRegularsKeepToTheirCabinets(GameTestHelper h){
        var f=fixture(h);var p=f.player("poly_prizes",P);var b=f.base(P);
        h.startSequence().thenExecute(()->hold(f)).thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the arcade is resident"))
            .thenWaitUntil(()->h.assertTrue(f.in.getEntitiesOfClass(LiteraryActor.class,new AABB(b).inflate(30),a->Polybius.regular(a.phase())&&a.owner().isEmpty()).size()==3,"three regulars, the same for everyone, at their own cabinets"))
            .thenExecute(()->{
                for(int i=0;i<PolybiusSite.REGULARS.size();i++){int phase=Polybius.REGULAR_PHASE+i;var at=Vec3.atBottomCenterOf(b.offset(PolybiusSite.REGULARS.get(i)));
                    h.assertTrue(!f.in.getEntitiesOfClass(LiteraryActor.class,new AABB(at,at).inflate(.6),a->a.phase()==phase).isEmpty(),"regular "+i+" stands at their cabinet");
                    h.assertTrue(PolybiusBlock.is(f.in.getBlockState(b.offset(PolybiusSite.OTHERS.get(i))),PolybiusBlock.Kind.OTHER),"and the cabinet is one they are playing");}
                for(int k=0;k<8;k++){var cell=new net.minecraft.core.BlockPos(5+k%4,k/4,PolybiusSite.FAR);var st=f.in.getBlockState(b.offset(cell));
                    h.assertTrue(PolybiusBlock.is(st,PolybiusBlock.Kind.PRIZES)&&st.getValue(PolybiusBlock.STAGE)==k&&PolybiusSite.prize(cell)==k,"shelf cell "+k+" holds its own prize");}
                h.assertTrue(Polybius.tickets(0)==0&&Polybius.tickets(50)==1&&Polybius.tickets(450)==5&&Polybius.tickets(100000)==30,"a game pays a ticket and one more a hundred points, up to thirty");
                p.getInventory().add(new net.minecraft.world.item.ItemStack(PolybiusRegistry.TICKETS.get(),12));
                var plush=new net.minecraft.core.BlockPos(8,1,PolybiusSite.FAR);f.click(p,P,plush);
                h.assertTrue(count(p,PolybiusRegistry.TICKETS.get())==12&&count(p,PolybiusRegistry.PRIZES.get(7).get())==0,"twelve tickets don't buy the plush dog");
                var ball=new net.minecraft.core.BlockPos(7,0,PolybiusSite.FAR);f.click(p,P,ball);
                h.assertTrue(count(p,PolybiusRegistry.TICKETS.get())==2&&count(p,PolybiusRegistry.PRIZES.get(2).get())==1,"ten of them buy the bouncy ball off its cell");
            }).thenSucceed();
    }
    @AfterBatch(batch="poly_prizes") public static void prizesDone(ServerLevel l){close();}

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
                h.assertTrue(!Polybius.submit(p,token(p)+1,idle(30)),"a game with another session's token is not believed");
                // 0.4.225 (review: "Polybius trusts client-reported scores too much"): the screen sends its keys, never a score.
                h.assertTrue(!Polybius.submit(p,token(p),keys())&&PolybiusScores.table(s,PolybiusSite.MOTHLIGHT).isEmpty()&&count(p,PolybiusRegistry.TICKETS.get())==0,"nothing played, nothing scored or paid");
                cabinet(f,p,PolybiusSite.MOTHLIGHT);
                h.assertTrue(!Polybius.submit(p,token(p),keys(2000,PolybiusGames.LEFT))&&PolybiusScores.table(s,PolybiusSite.MOTHLIGHT).isEmpty(),"nor a game longer than the session has been open");
                cabinet(f,p,PolybiusSite.MOTHLIGHT);Polybius.backdate(p.getUUID(),100);h.assertTrue(!Polybius.submit(p,token(p),keys(0,PolybiusGames.LEFT,20,0)),"nor a log with a run of nothing in it");
                cabinet(f,p,PolybiusSite.MOTHLIGHT);Polybius.backdate(p.getUUID(),Polybius.LONGEST[PolybiusSite.MOTHLIGHT]+500);
                h.assertTrue(!Polybius.submit(p,token(p),idle(Polybius.LONGEST[PolybiusSite.MOTHLIGHT]+1))&&PolybiusScores.table(s,PolybiusSite.MOTHLIGHT).isEmpty(),"nor one longer than the game runs");
                // A game played: the server plays it again from the session's seed with the reader's keys and keeps what it scores.
                var drift=keys(30,PolybiusGames.LEFT,30,PolybiusGames.UP|PolybiusGames.RIGHT,40,PolybiusGames.DOWN,400,0);
                cabinet(f,p,PolybiusSite.MOTHLIGHT);int first=replayed(p,drift);Polybius.backdate(p.getUUID(),500);
                h.assertTrue(first>0&&Polybius.submit(p,token(p),drift)&&PolybiusScores.best(s,PolybiusSite.MOTHLIGHT,p.getUUID())==first,"a game played stands, at the score the server's replay reaches: "+first);
                h.assertTrue(count(p,PolybiusRegistry.TICKETS.get())==Polybius.tickets(first),"and it is paid for that score");
                cabinet(f,q,PolybiusSite.MOTHLIGHT);int theirs=replayed(q,idle(300));Polybius.backdate(q.getUUID(),300);h.assertTrue(Polybius.submit(q,token(q),idle(300)),"and the next reader's");
                cabinet(f,p,PolybiusSite.MOTHLIGHT);int worse=replayed(p,idle(10));h.assertTrue(worse<first&&Polybius.submit(p,token(p),idle(10)),"a shorter game stands too, closed after ten ticks: "+worse);
                var t=PolybiusScores.table(s,PolybiusSite.MOTHLIGHT);
                h.assertTrue(t.size()==2&&t.get(0).score()>=t.get(1).score()&&PolybiusScores.best(s,PolybiusSite.MOTHLIGHT,p.getUUID())==first&&PolybiusScores.best(s,PolybiusSite.MOTHLIGHT,q.getUUID())==theirs,"one place each, best kept, in order: "+t);
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
                .thenExecute(()->{h.assertTrue(Polybius.submit(p,token(p),idle(Polybius.MIN_PLAY)),"a play of the black cabinet that lasted counts");h.assertTrue(f.own(p,P).getInt("Plays217")==n,"play "+n);});
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
            .thenExecute(()->{cabinet(f,p,PolybiusSite.SIGNAL);var tune=keys(20,PolybiusGames.SPACE,20,0,20,PolybiusGames.SPACE,20,0);Polybius.backdate(p.getUUID(),100);
                h.assertTrue(replayed(p,tune)>0&&Polybius.submit(p,token(p),tune)&&PolybiusScores.rank(s,PolybiusSite.SIGNAL,p.getUUID())==1,"a place on the signal table first");});
        for(int i=1;i<=Polybius.PLAYS;i++)seq=seq.thenExecute(()->cabinet(f,p,PolybiusSite.BLACK)).thenIdle(Polybius.MIN_PLAY-30).thenExecute(()->Polybius.submit(p,token(p),idle(Polybius.MIN_PLAY)));
        seq.thenExecute(()->{h.assertTrue(f.own(p,P).getInt("Plays217")==Polybius.PLAYS,"three plays");cabinet(f,p,PolybiusSite.BLACK);
                var ses=Polybius.session(p.getUUID());h.assertTrue(ses!=null&&ses.taken(),"the fourth is the one it keeps");
                h.assertTrue(!Polybius.submit(p,token(p),idle(Polybius.MIN_PLAY)),"and no game over from the screen ends it");})
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
