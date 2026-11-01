package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import io.github.knaitoe.theoldesthouse.opening.CompanionOrders;
import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.218, the replacement, played with real readers and a real companion: dealt only to a reader with a companion following them;
 * the companion taken to the crate the first moment it is out of sight, and the replacement at the reader's heels in its place; the
 * crate opened, the companion given back, and the street walked out with it at the reader's side; and a reader who leaves without
 * it, whose companion is sent home to wait for them.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_literary") @PrefixGameTestTemplate(false)
public final class ReplacementTests {
    private ReplacementTests(){}
    private static final LabyrinthPlace P=LabyrinthPlace.REPLACEMENT;
    private static LiteraryTests.Fixture active;
    private static LiteraryTests.Fixture fixture(GameTestHelper h){if(active!=null)active.close();active=new LiteraryTests.Fixture(h);Replacement.clearAll();return active;}
    private static void close(){if(active!=null){active.close();active=null;}Replacement.clearAll();}
    private static ServerLevel street(LiteraryTests.Fixture f){return HouseTestLevel.get(f.in.getServer(),HouseDimensions.OUTSIDE);}
    private static void hold(LiteraryTests.Fixture f){var b=f.base(P);f.chunks.hold(street(f),new AABB(b.getX()-18,b.getY()-2,b.getZ()-64,b.getX()+19,b.getY()+12,b.getZ()+2));}
    private static void at(LiteraryTests.Fixture f,ServerPlayer p,double x,double z,float yaw){var b=f.base(P);p.teleportTo(street(f),b.getX()+x,b.getY(),b.getZ()+z,yaw,0);}
    /** A tamed cat named Pip, following the reader, behind them where they aren't looking. */
    private static Cat pip(LiteraryTests.Fixture f,ServerPlayer p){
        var l=street(f);var cat=EntityType.CAT.create(l);var b=f.base(P);cat.moveTo(b.getX()+.5,b.getY(),b.getZ()-1.2,0,0);cat.tame(p);cat.setCustomName(Component.literal("Pip"));
        l.addFreshEntity(cat);CompanionOrders.issue(cat,p,CompanionOrders.Order.FOLLOW);cat.setOrderedToSit(false);return cat;
    }

    @GameTest(template="empty",batch="repl_build",timeoutTicks=400)
    public static void dealtOnlyToAReaderWithACompanionOnAStreetOfBoardedHouses(GameTestHelper h){
        var f=fixture(h);var p=f.player("repl_build",P);var b=f.base(P);var l=street(f);
        h.startSequence().thenExecute(()->hold(f)).thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the street is resident"))
            .thenExecute(()->{
                h.assertTrue(LiteraryRooms.isLiterary(P)&&LiteraryRooms.outside(P)&&P.kind()==LabyrinthPlace.Kind.MULTI_VISIT,"a literary street under the outside sky, over as many visits as it takes");
                var story=WitnessAccount.Story.of(P.id());h.assertTrue(story!=null&&story.kind.equals("understanding")&&WitnessAccount.Story.values().length==WitnessPool.SOURCES&&WitnessAccount.REQUIRED==WitnessPool.REQUIRED,"an understanding story, and the pool grows by one");
                var d=new LabyrinthData();var id=UUID.randomUUID();
                h.assertTrue(!LiteraryVignettes.canDeal(d,id,P),"no companion following, no street");
                var own=LiteraryVignettes.personal(d,id,P);own.putBoolean("Companion218",true);LiteraryVignettes.save(d,id,P,own);
                h.assertTrue(LiteraryVignettes.canDeal(d,id,P),"a companion following, and it can be dealt");
                h.assertTrue(ReplacementBlock.is(l.getBlockState(b.offset(ReplacementSite.POSTER)),ReplacementBlock.Kind.POSTER)&&ReplacementBlock.is(l.getBlockState(b.offset(ReplacementSite.MAILBOX)),ReplacementBlock.Kind.MAILBOX),"the posters on the pole and the mailbox");
                h.assertTrue(ReplacementBlock.is(l.getBlockState(b.offset(ReplacementSite.CRATE)),ReplacementBlock.Kind.CRATE)&&l.getBlockState(b.offset(ReplacementSite.PEN)).isAir(),"the crate in the open house, shut, with room behind its door");
                h.assertTrue(ReplacementBlock.is(l.getBlockState(b.offset(-8,0,-12)),ReplacementBlock.Kind.BOARD_DOOR)&&ReplacementBlock.is(l.getBlockState(b.offset(8,1,-22)),ReplacementBlock.Kind.BOARDS),"the houses either side are boarded up");
                h.assertTrue(l.getBlockState(b.offset(0,0,-3)).isAir()&&l.getBlockState(b.offset(0,1,-30)).isAir()&&l.getBlockState(b.offset(0,1,-52)).isAir(),"the way down the street and into the open house is clear");
                var text=LiteraryTexts.source(P).get(DataComponents.WRITTEN_BOOK_CONTENT);var all=text==null?"":String.join(" ",text.pages().stream().map(page->page.raw().getString()).toList());
                h.assertTrue(all.contains("LOST")&&all.contains("your own handwriting"),"the posters, the newest in the reader's hand: "+all);
            }).thenSucceed();
    }
    @AfterBatch(batch="repl_build") public static void buildDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="repl_take",timeoutTicks=600)
    public static void outOfSightTheCompanionIsTakenAndSomethingIsAtTheReadersHeels(GameTestHelper h){
        var f=fixture(h);var p=f.player("repl_take",P);Cat[] cat={null};
        h.startSequence().thenExecute(()->hold(f)).thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the street is resident"))
            .thenExecute(()->{at(f,p,.5,-3.5,180);cat[0]=pip(f,p);})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Taken218"),"the first moment it is out of sight"))
            .thenExecute(()->{var b=f.base(P);var own=f.own(p,P);
                h.assertTrue(cat[0].position().distanceTo(Vec3.atBottomCenterOf(b.offset(ReplacementSite.PEN)))<.8&&cat[0].isInvulnerable()&&cat[0].isOrderedToSit(),"Pip is in the crate, sitting, and cannot be hurt");
                h.assertTrue(own.getString("Species218").equals("cat")&&own.getString("Name218").equals("Pip"),"the street knows what it took: "+own);
                var a=Replacement.replacement(p);h.assertTrue(a!=null&&a.position().distanceTo(p.position())<4,"and something is at the reader's heels");
                at(f,p,.5,-20.5,180);})
            .thenWaitUntil(()->{var a=Replacement.replacement(p);h.assertTrue(a!=null&&a.position().distanceTo(p.position())<4,"it cannot be shaken off");})
            .thenSucceed();
    }
    @AfterBatch(batch="repl_take") public static void takeDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="repl_home",timeoutTicks=800)
    public static void openingTheCrateGivesItBackAndTheStreetTakesItHome(GameTestHelper h){
        var f=fixture(h);var p=f.player("repl_home",P);Cat[] cat={null};
        h.startSequence().thenExecute(()->hold(f)).thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the street is resident"))
            .thenExecute(()->{at(f,p,.5,-3.5,180);cat[0]=pip(f,p);})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Taken218"),"taken"))
            .thenExecute(()->{f.click(p,P,ReplacementSite.CRATE);var own=f.own(p,P);
                h.assertTrue(own.getBoolean("Freed218")&&!cat[0].isInvulnerable()&&!cat[0].isOrderedToSit()&&CompanionOrders.order(cat[0])==CompanionOrders.Order.FOLLOW,"the crate opened, Pip is given back and follows again");
                h.assertTrue(own.getBoolean("Stood218"),"and the thing behind the reader stands up");
                var b=f.base(P);at(f,p,.5,-46.5,0);cat[0].teleportTo(b.getX()+1.5,b.getY(),b.getZ()-46.5);})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Ready"),"out on the street with Pip at their side"))
            .thenExecute(()->{var own=f.own(p,P);var b=f.base(P);
                h.assertTrue(own.getString("Outcome").equals("took_it_home")&&street(f).getBlockState(b.offset(ReplacementSite.MAILBOX)).getValue(ReplacementBlock.STAGE)==1,"took it home; a page in the mailbox: "+own);
                String text=ReplacementTexts.ledger("repl_home",own)+"\n"+ReplacementTexts.afterwards("repl_home",own);
                h.assertTrue(text.contains("Pip .......... went home")&&!text.matches("(?s).*\\b(he|she|his|her|him|hers)\\b.*"),"the ledger names Pip, and the account never gives the reader a gender: "+text);})
            .thenSucceed();
    }
    @AfterBatch(batch="repl_home") public static void homeDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="repl_left",timeoutTicks=600)
    public static void leavingWithoutItSendsItHomeToWait(GameTestHelper h){
        var f=fixture(h);var p=f.player("repl_left",P);Cat[] cat={null};
        h.startSequence().thenExecute(()->hold(f)).thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the street is resident"))
            .thenExecute(()->{at(f,p,.5,-3.5,180);cat[0]=pip(f,p);})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Taken218"),"taken"))
            .thenExecute(()->{var home=Replacement.home(f.in.getServer());h.assertTrue(home!=null,"the manor is there to go home to");f.chunks.hold(f.in.getServer().getLevel(HouseDimensions.INTERIOR),new AABB(home,home).inflate(8));})
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the manor's hall is resident"))
            .thenExecute(()->{var b=f.base(P);p.teleportTo(f.in,b.getX()+.5,b.getY()+40,b.getZ()+.5,0,0);})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("LeftBehind218"),"leaving the street with Pip still in the crate"))
            .thenExecute(()->{var own=f.own(p,P);
                h.assertTrue(!own.getBoolean("Ready")&&own.getList("Held218",11).isEmpty(),"no account, and nothing held any longer");
                var at=Replacement.home(f.in.getServer());var home=f.in.getServer().getLevel(HouseDimensions.INTERIOR).getEntitiesOfClass(Cat.class,new AABB(at,at).inflate(6),c->"Pip".equals(c.getCustomName()==null?"":c.getCustomName().getString()));
                h.assertTrue(!home.isEmpty()&&home.get(0).isOrderedToSit()&&!home.get(0).isInvulnerable()&&p.getUUID().equals(home.get(0).getOwnerUUID()),"Pip has gone home to the manor, and waits there, sitting, still the reader's");})
            .thenSucceed();
    }
    @AfterBatch(batch="repl_left") public static void leftDone(ServerLevel l){close();}
}
