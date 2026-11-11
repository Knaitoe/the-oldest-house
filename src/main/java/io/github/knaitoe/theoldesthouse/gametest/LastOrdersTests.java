package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.416, Last orders, with real readers. The bar built whole: the counter and its till, five booths, the pool table under its lamp, the
 * board and the league beside it, the clock at ten to, the window onto the street. The man in booth four plays a reader at pool on the real
 * table, with a second reader on their side; he loses the first frames and doubles what is on the table, the next booth starts talking, and
 * the last frame is played for everything. One winner walks out and leaves the roll, and has the account; the other takes it and is back
 * at the door with nothing. At darts, a reader takes a glass from Benny, which puts a week on the league board under their name and turns
 * the window to another season, then loses the last leg and has the evening taken. The regulars play among themselves, and a reader
 * waiting for their next game goes on the chalk first. The one who walked out finds the house chalk and a tin of darts with the note
 * (0.4.417), and makes a table and a board of their own with them; nobody else's grid makes anything with those.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_literary") @PrefixGameTestTemplate(false)
public final class LastOrdersTests {
    private LastOrdersTests(){}
    private static final LabyrinthPlace P=LabyrinthPlace.LAST_ORDERS;
    private static LiteraryTests.Fixture active;
    private static LiteraryTests.Fixture fixture(GameTestHelper h){if(active!=null)active.close();active=new LiteraryTests.Fixture(h);LastOrders.clearAll();PoolTables.clearAll();DartBoards.clearAll();return active;}
    /** Run 1493: the last case of each batch leaves no players behind for whatever the runner takes up next. */
    private static void close(){if(active!=null){active.close();active=null;}LastOrders.clearAll();PoolTables.clearAll();DartBoards.clearAll();}
    @net.minecraft.gametest.framework.AfterBatch(batch="orders_darts") public static void ordersDartsDone(net.minecraft.server.level.ServerLevel l){close();}
    @net.minecraft.gametest.framework.AfterBatch(batch="orders_pool") public static void ordersPoolDone(net.minecraft.server.level.ServerLevel l){close();}
    @net.minecraft.gametest.framework.AfterBatch(batch="orders_regulars") public static void ordersRegularsDone(net.minecraft.server.level.ServerLevel l){close();}
    private static AABB box(LiteraryTests.Fixture f){var b=f.base(P);return new AABB(b.getX()-15,b.getY()-3,b.getZ()-35,b.getX()+17,b.getY()+9,b.getZ()+4);}
    private static String text(net.minecraft.world.item.ItemStack book){var c=book.get(DataComponents.WRITTEN_BOOK_CONTENT);return String.join(" ",c.pages().stream().map(pg->pg.raw().getString()).toList());}
    private static void ask(LiteraryTests.Fixture f,ServerPlayer p,String choice){
        var a=LastOrders.actor(f.level(P),f.base(P),0);f.at(p,P,-8.5,0,-18.5);LastOrders.talk(p,a);Talk.choose(p,choice);
    }
    private static net.minecraft.world.item.ItemStack find(ServerPlayer p,net.minecraft.world.item.Item item){var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(inv.getItem(i).is(item))return inv.getItem(i);return net.minecraft.world.item.ItemStack.EMPTY;}
    /** What a crafting table offers this player for a grid of nine. */
    private static net.minecraft.world.item.ItemStack craft(ServerPlayer p,net.minecraft.world.item.ItemStack[] grid){
        var menu=new net.minecraft.world.inventory.CraftingMenu(0,p.getInventory(),net.minecraft.world.inventory.ContainerLevelAccess.create(p.level(),p.blockPosition()));
        for(int i=0;i<9;i++)menu.getSlot(1+i).set(grid[i].copy());
        return menu.getSlot(0).getItem().copy();
    }
    private static int houseDarts(ServerPlayer p){int n=0;for(var s:p.getInventory().items)if(DartBoards.isHouseDart(s))n+=s.getCount();for(var s:p.getInventory().offhand)if(DartBoards.isHouseDart(s))n+=s.getCount();return n;}
    private static String sign(LiteraryTests.Fixture f,net.minecraft.core.BlockPos rel,int line){return f.level(P).getBlockEntity(f.base(P).offset(rel)) instanceof SignBlockEntity s?s.getFrontText().getMessage(line,false).getString():"";}

    @GameTest(template="empty",batch="orders_pool",timeoutTicks=3000)
    public static void aFrameForEverythingWonAndLeftOnTheTable(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);
        ServerPlayer[] r=new ServerPlayer[2];
        h.startSequence().thenExecute(()->f.chunks.hold(l,box(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the bar is resident"))
            .thenExecute(()->{
                h.assertTrue(LiteraryRooms.isLiterary(P)&&!LiteraryRooms.outside(P)&&P.kind()==LabyrinthPlace.Kind.RECURRING&&P.authored(),"a literary place inside the House, built whole");
                var story=WitnessAccount.Story.of(P.id());h.assertTrue(story!=null&&story.kind.equals("survival")&&WitnessAccount.Story.values().length==WitnessPool.SOURCES&&WitnessAccount.REQUIRED==WitnessPool.REQUIRED,"a survival story, the quota derived");
                h.assertTrue(RoadhouseBlock.is(l.getBlockState(b.offset(LastOrdersSite.RULES)),RoadhouseBlock.Kind.CORKBOARD)&&RoadhouseBlock.is(l.getBlockState(b.offset(LastOrdersSite.TILL)),RoadhouseBlock.Kind.REGISTER),"the house rules on the corkboard and the till on the counter");
                h.assertTrue(PoolTableBlock.is(l.getBlockState(b.offset(LastOrdersSite.TABLE)))&&DartboardBlock.is(l.getBlockState(b.offset(LastOrdersSite.BOARD))),"a real pool table and a real dartboard");
                for(int c:LastOrdersSite.BOOTHS)h.assertTrue(RoadhouseBlock.is(l.getBlockState(b.offset(-10,0,c)),RoadhouseBlock.Kind.BOOTH_TABLE)&&RoadhouseBlock.is(l.getBlockState(b.offset(-10,0,c-1)),RoadhouseBlock.Kind.BOOTH),"a booth at "+c);
                h.assertTrue(l.getBlockState(b.offset(LastOrdersSite.EAST,2,LastOrdersSite.WINDOW_Z0)).is(Blocks.GLASS_PANE)&&l.getBlockState(b.offset(LastOrdersSite.EAST+2,2,(LastOrdersSite.WINDOW_Z0+LastOrdersSite.WINDOW_Z1)/2)).is(Blocks.CHERRY_LEAVES),"the window, onto a street in spring");
                var all=text(LiteraryTexts.source(P));h.assertTrue(all.contains("Booth four is taken")&&all.contains("stays on the tables"),"the house rules: "+all);
                r[0]=f.player("orders_walks",P);r[1]=f.player("orders_takes",P);
                f.source(r[0],P);f.source(r[1],P);
                f.at(r[0],P,-1.5,0,-9.5);f.at(r[1],P,-2.5,0,-9.5);
            })
            .thenWaitUntil(()->{var a=LastOrders.actor(l,b,0);h.assertTrue(a!=null&&a.phase()==LastOrders.STRANGER&&LastOrders.stage(b)==LastOrders.IDLE,"the man in booth four, waiting");})
            // 0.4.447: the bar's lean has the same number as Costume Night's crawl, and the two in the next booth were drawn on all fours.
            .thenExecute(()->{for(int who=1;who<=2;who++){var a=LastOrders.actor(l,b,who);h.assertTrue(a!=null&&a.gesture()==LastOrders.LEAN&&a.ownGestures()&&!a.crawling()&&!a.reaching()&&a.getBbHeight()>1.5F,"the two in the next booth sit up and lean in over their table, neither of them on all fours: "+(a==null?null:a.gesture()+" "+a.getBbHeight()));}
                var man=LastOrders.actor(l,b,0);int was=man.gesture();man.gesture(LastOrders.WHISTLE);h.assertTrue(!man.reaching()&&!man.crawling(),"his whistle is his own gesture, and not the plain reach with the same number");man.gesture(was);})
            .thenWaitUntil(()->{var bd=DartBoards.at(l,b.offset(LastOrdersSite.BOARD));h.assertTrue(bd!=null&&bd.house&&bd.seats.stream().filter(DartBoards::regular).count()==3,"the three regulars on the chalk at the board");})
            .thenExecute(()->{
                ask(f,r[0],"Pool");
                h.assertTrue(LastOrders.stage(b)==LastOrders.WAITING&&LastOrders.game(b)==LastOrders.Game.POOL,"he gets up to meet the reader at the table");
                f.at(r[0],P,-1.5,0,-9.5);
            })
            .thenWaitUntil(()->h.assertTrue(LastOrders.stage(b)==LastOrders.PLAYING,"the game starts when the reader comes to the table"))
            .thenExecute(()->{
                var t=PoolTables.at(l,b.offset(LastOrdersSite.TABLE));
                h.assertTrue(LastOrders.stage(b)==LastOrders.PLAYING&&LastOrders.game(b)==LastOrders.Game.POOL&&LastOrders.frame(b)==1,"a game of pool, the first frame");
                h.assertTrue(t!=null&&t.seated(r[0].getUUID())&&t.seated(PoolTables.REGULAR)&&t.sideOf(t.seatOf(r[0].getUUID()))==0&&t.sideOf(t.seatOf(PoolTables.REGULAR))==1&&t.names.contains(LastOrdersTexts.CHALK),"the reader on one side, booth four on the other");
                PoolTables.use(r[1],b.offset(LastOrdersSite.TABLE));
                h.assertTrue(t.seated(r[1].getUUID())&&t.sideOf(t.seatOf(r[1].getUUID()))==0,"a second reader joins the reader's side, not his");
                LastOrders.endFrame(l,b,false);
                h.assertTrue(LastOrders.stage(b)==LastOrders.BETWEEN&&f.own(r[0],P).getInt("Frames")==1,"the first frame to the readers, and counted");
            })
            .thenWaitUntil(()->h.assertTrue(LastOrders.boothLines(b)>=1,"once the stakes are high, the next booth starts talking"))
            .thenWaitUntil(()->h.assertTrue(LastOrders.stage(b)==LastOrders.PLAYING&&LastOrders.frame(b)==2,"the second frame"))
            .thenExecute(()->{
                var t=PoolTables.at(l,b.offset(LastOrdersSite.TABLE));h.assertTrue(t!=null&&t.game.phase==PoolGame.Phase.BREAK,"racked again for the same players");
                LastOrders.endFrame(l,b,false);
            })
            .thenWaitUntil(()->h.assertTrue(LastOrders.stage(b)==LastOrders.PLAYING&&LastOrders.frame(b)==3,"the last frame, for everything"))
            .thenExecute(()->{
                var t=PoolTables.at(l,b.offset(LastOrdersSite.TABLE));h.assertTrue(Math.abs(t.regularSkill-LastOrders.POOL_HARD)<1e-6,"he plays the last frame well: "+t.regularSkill);
                LastOrders.endFrame(l,b,false);
                h.assertTrue(LastOrders.stage(b)==LastOrders.WON&&LastOrders.winner(b,r[0].getUUID())&&LastOrders.winner(b,r[1].getUUID()),"both readers beat him");
                h.assertTrue(!l.getEntitiesOfClass(ItemEntity.class,box(f),e->e.getItem().getHoverName().getString().contains("roll of notes")).isEmpty(),"his roll lies on the table in booth four");
                f.at(r[0],P,.5,0,-1.5);
            })
            .thenWaitUntil(()->h.assertTrue(f.own(r[0],P).getBoolean("Ready")&&f.own(r[0],P).getBoolean("Walked")&&f.own(r[0],P).getString("Outcome").equals(LastOrders.OUTCOME),"a winner who leaves the roll and walks out has the account"))
            .thenWaitUntil(()->{var a=LastOrders.actor(l,b,0);h.assertTrue(LastOrders.stage(b)==LastOrders.STARING&&a!=null&&a.gesture()==LastOrders.STARE,"he sits on, staring at the seat across from him");})
            .thenExecute(()->{
                f.click(r[1],P,new net.minecraft.core.BlockPos(-10,0,LastOrdersSite.BOOTHS[LastOrdersSite.STRANGER_BOOTH]));
                h.assertTrue(f.own(r[1],P).getInt("Took")==1&&!LastOrders.winner(b,r[1].getUUID())&&!f.own(r[1],P).getBoolean("Ready"),"the other winner takes the roll, and has no account");
                h.assertTrue(l.getEntitiesOfClass(ItemEntity.class,box(f),e->e.getItem().getHoverName().getString().contains("roll of notes")).isEmpty(),"the roll is gone from the table");
            })
            .thenWaitUntil(()->{var at=r[1].position().subtract(b.getX(),b.getY(),b.getZ());h.assertTrue(at.z>-2&&at.z<-1&&Math.abs(at.x-.5)<.1,"the feast, and then the door again at ten to: "+at);})
            .thenExecute(()->{
                f.read(r[0],P,LastOrdersSite.TILL);
                h.assertTrue(f.own(r[0],P).getBoolean("Completed")&&WitnessAccount.has(f.data(),r[0].getUUID(),WitnessAccount.Story.LAST_ORDERS)&&!WitnessAccount.has(f.data(),r[1].getUUID(),WitnessAccount.Story.LAST_ORDERS),"the note under the till read: the story resolved for the reader who walked out only");
                var walked=text(LastOrdersTexts.account(r[0].getGameProfile().getName(),f.own(r[0],P)));
                h.assertTrue(walked.contains("staring at the seat across from him")&&walked.contains("left it there")&&!walked.contains("Neither of them breathing"),"the account for the one who left it: "+walked);
                var took=text(LastOrdersTexts.account(r[1].getGameProfile().getName(),f.own(r[1],P)));
                h.assertTrue(took.contains("two of them")&&took.contains("Same coat"),"the note's other page, for the one who took it: "+took);
                // 0.4.417: the keepsakes under the till, for the one who walked out, and what they make at home.
                h.assertTrue(walked.contains("house chalk and a tin of darts")&&!took.contains("house chalk"),"the note says what was left with it, for the one who walked out only");
                var chalk=find(r[0],BarRegistry.HOUSE_CHALK.get());var tin=find(r[0],BarRegistry.DART_TIN.get());
                h.assertTrue(KeepsakeKeyItem.mine(chalk,r[0])&&KeepsakeKeyItem.mine(tin,r[0])&&!KeepsakeKeyItem.mine(chalk,r[1]),"the house chalk and the tin of darts, marked as theirs alone");
                h.assertTrue(find(r[1],BarRegistry.HOUSE_CHALK.get()).isEmpty()&&find(r[1],BarRegistry.DART_TIN.get()).isEmpty(),"nothing for the one who took the roll");
                h.assertTrue(r[0].getRecipeBook().contains(BarRegistry.POOL_TABLE_RECIPE)&&r[0].getRecipeBook().contains(BarRegistry.DARTBOARD_RECIPE),"both recipes in their book");
                var wool=new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.GREEN_WOOL);var plank=new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.OAK_PLANKS);
                var iron=new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_INGOT);var none=net.minecraft.world.item.ItemStack.EMPTY;
                net.minecraft.world.item.ItemStack[] table={wool,wool,wool,plank,chalk,plank,iron,none,iron};
                net.minecraft.world.item.ItemStack[] board={none,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STRING),none,plank,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.TARGET),plank,none,tin,none};
                h.assertTrue(craft(r[0],table).is(BarRegistry.POOL_TABLE_ITEM.get())&&craft(r[0],board).is(BarRegistry.DARTBOARD_ITEM.get()),"their chalk makes a pool table and their tin a dartboard");
                h.assertTrue(craft(r[1],table).isEmpty()&&craft(r[1],board).isEmpty(),"the same keepsakes in another reader's grid make nothing");
                h.assertTrue(KeepsakeKeyItem.mine(chalk.getItem().getCraftingRemainingItem(chalk),r[0])&&KeepsakeKeyItem.mine(tin.getItem().getCraftingRemainingItem(tin),r[0]),"each comes back from the grid as it went in, still theirs");
                var key=new net.minecraft.world.item.ItemStack(PolybiusRegistry.SERVICE_KEY.get());
                h.assertTrue(!KeepsakeKeyItem.mine(key,r[0])&&!KeepsakeKeyItem.mine(chalk,r[1]),"a key nobody was given is nobody's");
                LastOrders.resetNow(l,b);
            })
            .thenSucceed();
    }

    @GameTest(template="empty",batch="orders_darts",timeoutTicks=3000)
    public static void aGlassFromBennyAndALegOfDartsLost(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);
        ServerPlayer[] r=new ServerPlayer[1];
        h.startSequence().thenExecute(()->f.chunks.hold(l,box(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the bar is resident"))
            .thenExecute(()->{r[0]=f.player("orders_darts",P);f.at(r[0],P,5.5,0,-10.5);})
            .thenWaitUntil(()->h.assertTrue(!l.getEntitiesOfClass(ItemEntity.class,box(f),e->r[0].getUUID().equals(LastOrders.glassOwner(e.getItem()))).isEmpty(),"coming along the bar, Benny pushes a glass toward the reader"))
            .thenExecute(()->{
                var glass=l.getEntitiesOfClass(ItemEntity.class,box(f),e->r[0].getUUID().equals(LastOrders.glassOwner(e.getItem()))).getFirst();
                h.assertTrue(glass.getX()>b.getX()+LastOrdersSite.COUNTER_X&&glass.getX()<b.getX()+LastOrdersSite.COUNTER_X+1,"on the counter");
                glass.discard();LastOrders.drink(r[0]);
                h.assertTrue(f.own(r[0],P).getInt("Glasses")==1&&f.own(r[0],P).getInt("VisitGlasses")==1,"the glass drunk");
                h.assertTrue(sign(f,LastOrdersSite.LEAGUE[0],1).equals(r[0].getGameProfile().getName())&&sign(f,LastOrdersSite.LEAGUE[0],0).equals("Week 9"),"another week on the league board, under the reader's name: "+sign(f,LastOrdersSite.LEAGUE[0],1));
                h.assertTrue(l.getBlockState(b.offset(LastOrdersSite.EAST+2,2,(LastOrdersSite.WINDOW_Z0+LastOrdersSite.WINDOW_Z1)/2)).is(Blocks.OAK_LEAVES),"and the street outside the window in summer");
                ask(f,r[0],"Darts");
                h.assertTrue(LastOrders.stage(b)==LastOrders.WAITING,"he gets up to meet the reader at the board");
                f.at(r[0],P,2.5,0,-25.5);
            })
            .thenWaitUntil(()->h.assertTrue(LastOrders.stage(b)==LastOrders.PLAYING,"the leg starts when the reader comes to the board"))
            .thenExecute(()->{
                var bd=DartBoards.at(l,b.offset(LastOrdersSite.BOARD));
                h.assertTrue(LastOrders.game(b)==LastOrders.Game.DARTS&&bd!=null&&!bd.house&&bd.start==LastOrders.LEG&&bd.seats.size()==2&&bd.seats.get(0).equals(r[0].getUUID())&&bd.names.contains(LastOrdersTexts.CHALK),"a short leg against booth four alone, the regulars off the chalk");
                h.assertTrue(houseDarts(r[0])==3,"the reader is handed three darts");
                LastOrders.endFrame(l,b,true);
            })
            .thenWaitUntil(()->h.assertTrue(LastOrders.stage(b)==LastOrders.PLAYING&&LastOrders.frame(b)==2,"the second leg"))
            .thenExecute(()->LastOrders.endFrame(l,b,true))
            .thenWaitUntil(()->h.assertTrue(LastOrders.stage(b)==LastOrders.PLAYING&&LastOrders.frame(b)==3,"the last leg, for everything"))
            .thenExecute(()->{
                var bd=DartBoards.at(l,b.offset(LastOrdersSite.BOARD));h.assertTrue(bd.game==null||bd.game.score[0]==LastOrders.LEG,"a fresh leg of a hundred and one");
                LastOrders.endFrame(l,b,true);
                h.assertTrue(LastOrders.stage(b)==LastOrders.WHISTLING,"his: it's done, he's won");
            })
            .thenWaitUntil(()->h.assertTrue(f.own(r[0],P).getInt("Lost")==1&&LastOrders.stage(b)==LastOrders.IDLE,"three whistles, and the evening is taken"))
            .thenWaitUntil(()->{var at=r[0].position().subtract(b.getX(),b.getY(),b.getZ());h.assertTrue(at.z>-2&&at.z<-1,"back at the door: "+at);})
            .thenWaitUntil(()->{var bd=DartBoards.at(l,b.offset(LastOrdersSite.BOARD));h.assertTrue(bd!=null&&bd.house&&bd.seats.stream().filter(DartBoards::regular).count()==3&&houseDarts(r[0])==0,"the regulars have the board back, and the house's darts are gone from the reader");})
            .thenExecute(()->{h.assertTrue(!f.own(r[0],P).getBoolean("Ready"),"no account for a lost evening");LastOrders.resetNow(l,b);})
            .thenSucceed();
    }

    @GameTest(template="empty",batch="orders_regulars",timeoutTicks=2400)
    public static void theRegularsNextGameTakesAReaderFirst(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);
        ServerPlayer[] r=new ServerPlayer[1];
        h.startSequence().thenExecute(()->f.chunks.hold(l,box(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the bar is resident"))
            .thenExecute(()->{r[0]=f.player("orders_waits",P);f.at(r[0],P,2.5,0,-24.5);})
            .thenWaitUntil(()->{var bd=DartBoards.at(l,b.offset(LastOrdersSite.BOARD));h.assertTrue(bd!=null&&bd.game!=null&&!bd.game.over(),"the regulars' game is on, real darts in the board");})
            .thenExecute(()->{
                f.click(r[0],P,LastOrdersSite.BOARD);
                var bd=DartBoards.at(l,b.offset(LastOrdersSite.BOARD));h.assertTrue(!bd.seated(r[0].getUUID()),"mid-game, the reader waits for the next one");
                bd.game.winner=0;
            })
            .thenWaitUntil(()->{var bd=DartBoards.at(l,b.offset(LastOrdersSite.BOARD));h.assertTrue(bd!=null&&bd.game==null&&!bd.seats.isEmpty()&&bd.seats.get(0).equals(r[0].getUUID())&&bd.seats.size()==4,"the next game: the reader on the chalk first, then three regulars");})
            .thenExecute(()->{
                h.assertTrue(houseDarts(r[0])==3,"and the reader throws first");
                var a=LastOrders.actor(l,b,3);h.assertTrue(a!=null&&a.phase()==LastOrders.HOB,"Hob, at the board");
                LastOrders.resetNow(l,b);
            })
            .thenSucceed();
    }
}
