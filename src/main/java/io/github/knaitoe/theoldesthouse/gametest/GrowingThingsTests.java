package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.350, Growing Things, with real readers. The Barretts' yard built whole and dealt only after the yard: the gate and its Saturday
 * list, the washing on the line, the table by the bowing window, shoots thicker toward the house, the back step. One reader: the washing
 * taken in, the table carried with John on the other end, John going out with the spade and gone the first moment nobody is looking, his
 * spade standing in the shoots, the window splitting, the floor lifting in three stages, Merry's question, the pot, the loaf and his
 * plate carried upstairs; an account from that reader's record and a dry seed head. Years later, the drawing on the Ghosts Set's fridge,
 * for that reader alone. Then together: two readers on the table's two ends without John, a reader who goes back out through the gate,
 * a reader who comes in partway, supper carried up without his plate, and each their own account.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_literary") @PrefixGameTestTemplate(false)
public final class GrowingThingsTests {
    private GrowingThingsTests(){}
    private static final LabyrinthPlace P=LabyrinthPlace.GROWING_THINGS;
    private static LiteraryTests.Fixture active;
    private static LiteraryTests.Fixture fixture(GameTestHelper h){if(active!=null)active.close();active=new LiteraryTests.Fixture(h);GrowingThings.clearAll();return active;}
    private static AABB box(LiteraryTests.Fixture f){var b=f.base(P);return new AABB(b.getX()-31,b.getY()-5,b.getZ()-61,b.getX()+31,b.getY()+16,b.getZ()+3);}
    private static String account(ServerPlayer p){for(var st:p.getInventory().items)if(st.is(Items.WRITTEN_BOOK)){var c=st.get(DataComponents.WRITTEN_BOOK_CONTENT);if(c!=null&&c.title().raw().equals(GrowingThingsTexts.TITLE))return String.join(" ",c.pages().stream().map(pg->pg.raw().getString()).toList());}return "";}
    private static boolean holds(ServerPlayer p,net.minecraft.world.item.Item item){for(var st:p.getInventory().items)if(st.is(item))return true;return false;}
    private static boolean use(LiteraryTests.Fixture f,ServerPlayer p,BlockPos rel){var at=f.base(P).offset(rel);return GrowingThings.use(p,at,f.level(P).getBlockState(at));}
    private static java.util.function.BiConsumer<LiteraryActor,String> ear;
    private static void close(){if(ear!=null){LiteraryActor.SAID.remove(ear);ear=null;}if(active!=null){active.close();active=null;}GrowingThings.clearAll();}
    @AfterBatch(batch="growing_things") public static void done(net.minecraft.server.level.ServerLevel l){close();}
    @AfterBatch(batch="growing_things_together") public static void togetherDone(net.minecraft.server.level.ServerLevel l){close();}
    @AfterBatch(batch="growing_things_asked") public static void askedDone(net.minecraft.server.level.ServerLevel l){close();}
    private static int shoots(net.minecraft.server.level.ServerLevel l,BlockPos b,int z0,int z1){int n=0;for(int z=z0;z>=z1;z--)for(int x=GrowingThingsSite.YARD_W;x<=GrowingThingsSite.YARD_E;x++)if(GrowingBlock.is(l.getBlockState(b.offset(x,0,z)),GrowingBlock.Kind.SHOOT))n++;return n;}

    @GameTest(template="empty",batch="growing_things",timeoutTicks=6000)
    public static void aSaturdayAtTheBarretts(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);
        ServerPlayer[] me=new ServerPlayer[1];
        h.startSequence().thenExecute(()->f.chunks.hold(l,box(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the yard is resident"))
            .thenExecute(()->{
                h.assertTrue(LiteraryRooms.isLiterary(P)&&LiteraryRooms.outside(P)&&P.isOneShot()&&P.authored()&&SceneClock.at(f.origin,b.offset(0,0,-20),io.github.knaitoe.theoldesthouse.house.HouseDimensions.OUTSIDE)==16,"a literary yard outside, one visit, built whole, in the afternoon");
                var story=WitnessAccount.Story.of(P.id());h.assertTrue(story!=null&&story.kind.equals("understanding")&&WitnessAccount.Story.values().length==WitnessPool.SOURCES&&WitnessAccount.REQUIRED==WitnessPool.REQUIRED,"an understanding story, the quota derived");
                h.assertTrue(GrowingBlock.is(l.getBlockState(b.offset(GrowingThingsSite.NOTE)),GrowingBlock.Kind.NOTE)&&l.getBlockState(b.offset(GrowingThingsSite.GATE)).getBlock() instanceof net.minecraft.world.level.block.FenceGateBlock,"the gate and its list");
                for(int x:GrowingThingsSite.GARMENTS)h.assertTrue(GrowingBlock.is(l.getBlockState(b.offset(x,1,GrowingThingsSite.LINE_Z)),GrowingBlock.Kind.WASHING),"washing on the line at "+x);
                h.assertTrue(GrowingBlock.is(l.getBlockState(b.offset(GrowingThingsSite.TABLE_AT)),GrowingBlock.Kind.TABLE),"the table by the window");
                for(var w:GrowingThingsSite.WINDOW)h.assertTrue(GrowingBlock.is(l.getBlockState(b.offset(w)),GrowingBlock.Kind.FRAME)&&l.getBlockState(b.offset(w)).getValue(GrowingBlock.STAGE)==1,"the window frame bowing");
                for(int x=-1;x<=1;x++)h.assertTrue(GrowingBlock.is(l.getBlockState(b.offset(x,0,GrowingThingsSite.STEP_Z)),GrowingBlock.Kind.STEPS),"the back step");
                h.assertTrue(GrowingBlock.is(l.getBlockState(b.offset(GrowingThingsSite.COUNTER)),GrowingBlock.Kind.COUNTER)&&GrowingBlock.is(l.getBlockState(b.offset(GrowingThingsSite.UP_TABLE)),GrowingBlock.Kind.UP_TABLE),"the counter and the card table upstairs");
                int far=shoots(l,b,-6,-11),near=shoots(l,b,-23,-28);h.assertTrue(near>far&&near>12,"shoots thicker toward the house: "+far+" by the fence, "+near+" by the house");
                var text=LiteraryTexts.source(P).get(DataComponents.WRITTEN_BOOK_CONTENT);var all=String.join(" ",text.pages().stream().map(pg->pg.raw().getString()).toList());
                h.assertTrue(all.contains("washing in off the line")&&all.contains("they came up in the night again"),"the list: "+all);
                var probe=UUID.randomUUID();h.assertTrue(!GrowingThings.dealable(f.data(),probe),"not before the yard");
                f.data().visit(probe,LabyrinthPlace.YARD_TURNAROUND);h.assertTrue(GrowingThings.dealable(f.data(),probe),"after it");
                me[0]=f.player("grow_one",P);
            })
            .thenWaitUntil(()->h.assertTrue(GrowingThings.actor(l,b,0)!=null&&GrowingThings.actor(l,b,1)!=null&&GrowingThings.actor(l,b,2)!=null,"Merry, Marjorie and John at home"))
            // The washing.
            .thenExecute(()->f.at(me[0],P,-4.5,0,-14.5))
            .thenWaitUntil(()->h.assertTrue(f.own(me[0],P).getBoolean("SawShoots"),"shoots in the lawn"))
            .thenExecute(()->{for(int x:GrowingThingsSite.GARMENTS)h.assertTrue(use(f,me[0],new BlockPos(x,1,GrowingThingsSite.LINE_Z)),"a piece of washing at "+x);
                h.assertTrue(f.own(me[0],P).getInt("Washing")==5&&GrowingThings.flag(b,"washing")&&GrowingBlock.is(l.getBlockState(b.offset(GrowingThingsSite.BASKET)),GrowingBlock.Kind.BASKET)&&l.getBlockState(b.offset(GrowingThingsSite.BASKET)).getValue(GrowingBlock.STAGE)==1,"the washing in, the basket full");})
            // The table, with John on the other end.
            .thenExecute(()->f.at(me[0],P,-7.5,1,-32.5))
            .thenWaitUntil(()->h.assertTrue(f.own(me[0],P).getBoolean("Inside"),"in the kitchen"))
            .thenExecute(()->h.assertTrue(use(f,me[0],GrowingThingsSite.TABLE_AT),"a hand on the table's end"))
            .thenWaitUntil(()->h.assertTrue(GrowingThings.flag(b,"table")&&f.own(me[0],P).getString("TableWith").equals("John"),"the table carried with John: "+f.own(me[0],P)))
            .thenExecute(()->h.assertTrue(GrowingBlock.is(l.getBlockState(b.offset(GrowingThingsSite.TABLE_MOVED)),GrowingBlock.Kind.TABLE)&&l.getBlockState(b.offset(GrowingThingsSite.TABLE_AT)).isAir(),"the table set down away from the window"))
            .thenWaitUntil(()->h.assertTrue(GrowingThings.phase(b)==GrowingThings.AFTERNOON,"the afternoon"))
            // John goes out to the step, and is gone the first moment nobody is looking.
            .thenExecute(()->{f.at(me[0],P,5.5,1,-40.5);f.look(me[0],b.offset(5,2,-46));GrowingThings.skip(b,GrowingThings.JOHN_OUT);})
            .thenWaitUntil(()->h.assertTrue(GrowingThings.flag(b,"out")&&f.own(me[0],P).getBoolean("SawJohnGo"),"John goes out with the spade"))
            .thenExecute(()->GrowingThings.skip(b,GrowingThings.JOHN_GONE))
            .thenWaitUntil(()->h.assertTrue(GrowingThings.flag(b,"gone")&&GrowingThings.actor(l,b,2)==null&&f.own(me[0],P).getBoolean("JohnGone"),"John gone while nobody was looking"))
            .thenExecute(()->{h.assertTrue(GrowingBlock.is(l.getBlockState(b.offset(GrowingThingsSite.SPADE)),GrowingBlock.Kind.SPADE)&&l.getBlockState(b.offset(0,0,GrowingThingsSite.STEP_Z)).getValue(GrowingBlock.STAGE)==2,"the spade standing where the step went under");
                h.assertTrue(GrowingThings.flag(b,"frame"),"the window split");
                f.at(me[0],P,2.5,0,-24.5);f.look(me[0],b.offset(GrowingThingsSite.SPADE));})
            .thenWaitUntil(()->h.assertTrue(f.own(me[0],P).getBoolean("SawSpade"),"the spade seen in the shoots"))
            // The floor, and supper.
            .thenExecute(()->{f.at(me[0],P,5.5,1,-40.5);GrowingThings.skip(b,GrowingThings.FLOOR_3);})
            .thenWaitUntil(()->h.assertTrue(GrowingThings.floor(b)==3&&GrowingThings.phase(b)==GrowingThings.SUPPER&&f.own(me[0],P).getBoolean("HeardAsk"),"the floor up, and Merry's question"))
            .thenExecute(()->{var cell=GrowingThingsSite.floorCells().get(10);var s=l.getBlockState(b.offset(cell));h.assertTrue(GrowingBlock.is(s,GrowingBlock.Kind.FLOOR)&&s.getValue(GrowingBlock.STAGE)==3,"the kitchen floor heaved: "+s);
                f.at(me[0],P,.5,1,-36.5);
                for(var what:List.of("Take the pot","Take the loaf","Take Dad's plate")){h.assertTrue(use(f,me[0],GrowingThingsSite.COUNTER),"the counter");h.assertTrue(Talk.choose(me[0],what),what+": "+Talk.replies(me[0]));}
                h.assertTrue(holds(me[0],GrowingRegistry.POT.get())&&holds(me[0],GrowingRegistry.LOAF.get())&&holds(me[0],GrowingRegistry.PLATE.get()),"the pot, the loaf and his plate in hand");
                f.at(me[0],P,-3.5,6,-31.5);h.assertTrue(use(f,me[0],GrowingThingsSite.UP_TABLE),"set down on the card table");})
            .thenWaitUntil(()->h.assertTrue(f.own(me[0],P).getBoolean("Completed"),"supper up"))
            .thenExecute(()->{
                var o=f.own(me[0],P);h.assertTrue(o.getString("Outcome").equals(GrowingThings.CARRIED)&&o.getString("GrowingRole").equals("actor")&&WitnessAccount.has(f.data(),me[0].getUUID(),WitnessAccount.Story.GROWING_THINGS),"his plate carried up: "+o);
                h.assertTrue(holds(me[0],GrowingRegistry.SEED_HEAD.get())&&!holds(me[0],GrowingRegistry.POT.get())&&!holds(me[0],GrowingRegistry.PLATE.get()),"a dry seed head in hand, and supper left on the table");
                var text=account(me[0]);
                h.assertTrue(text.contains("I took 5 things off the line")&&text.contains("I took one end of the table and John took the other")&&text.contains("took a spade out")&&text.contains("He didn't come back in")&&text.contains("his spade was standing")
                    &&text.contains("kitchen floor came up")&&text.contains("Merry asked whether we should bring Dad's plate")&&text.contains("I carried the pot and the loaf up, and his plate")&&text.contains("Marjorie said he would come up for it"),"the account follows the record: "+text);
                for(var colour:List.of("green","white","red","blue","yellow","pale"))h.assertTrue(!text.toLowerCase(Locale.ROOT).contains(colour),"no colour in the account: "+colour);
            })
            // Years later, in the Ghosts Set: the drawing on the fridge, for this reader alone.
            .thenExecute(()->{var g=LabyrinthPlace.GHOSTS_SET;f.build(g);var gb=f.base(g);f.chunks.hold(f.level(g),new AABB(gb.getX()-13,gb.getY()-5,gb.getZ()-28,gb.getX()+13,gb.getY()+12,gb.getZ()+1));})
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the Barretts' house, years later, is resident"))
            .thenExecute(()->{var g=LabyrinthPlace.GHOSTS_SET;var gb=f.base(g);var gl=f.level(g);
                h.assertTrue(BarrettHomeBlock.is(gl.getBlockState(gb.offset(-10,0,-25)),BarrettHomeBlock.Kind.FRIDGE)&&gl.getBlockState(gb.offset(GrowingThings.DRAWING_AT)).isAir(),"the Barretts' fridge, the drawing's place clear in front of it");
                h.assertTrue(GrowingThings.knows(f.data(),me[0].getUUID())&&GrowingThings.ghosts(me[0],gb,new CompoundTag()),"the drawing shown to the reader who was there");
                var other=NativeTestPlayers.survival(h,"grow_never");h.assertTrue(!GrowingThings.ghosts(other,gb,new CompoundTag()),"and to nobody else");
                h.assertTrue(gl.getBlockState(gb.offset(GrowingThings.DRAWING_AT)).isAir(),"the world itself unchanged");})
            .thenExecute(()->{GrowingThings.resetNow(l,b);
                h.assertTrue(GrowingBlock.is(l.getBlockState(b.offset(GrowingThingsSite.TABLE_AT)),GrowingBlock.Kind.TABLE)&&GrowingBlock.is(l.getBlockState(b.offset(GrowingThingsSite.GARMENTS.get(0),1,GrowingThingsSite.LINE_Z)),GrowingBlock.Kind.WASHING)&&l.getBlockState(b.offset(GrowingThingsSite.SPADE)).isAir(),"the yard set back");})
            .thenExecute(()->{if(active!=null){active.close();active=null;}GrowingThings.clearAll();})
            .thenSucceed();
    }

    /**
     * 0.4.571 (run 1601, the two-reader conduct check): Merry asks for a hand with the washing once for everybody near enough to hear
     * her, and John with the table. Each reader was asked on their own first step into the yard and the house, so two readers coming in
     * a moment apart were told the whole sentence twice. A reader who comes into hearing while she is still saying it has it as the
     * others did; one who comes near after she has finished is asked, and no sooner than ASK_AGAIN after the first asking. Merry
     * does not speak over the line about the shoots.
     */
    @GameTest(template="empty",batch="growing_things_asked",timeoutTicks=3000)
    public static void twoReadersComingInTogetherAreAskedOnceAndOneOutOfHearingWhenTheyComeNear(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);
        ServerPlayer[] r=new ServerPlayer[4];var washing=new ArrayList<Long>();var table=new ArrayList<Long>();final long[] in={0};
        ear=(a,text)->{if(text.startsWith("Can you help me get the washing in"))washing.add(l.getGameTime());else if(text.startsWith("That window's going."))table.add(l.getGameTime());};
        h.startSequence().thenExecute(()->f.chunks.hold(l,box(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the yard is resident"))
            // Two in the alley; two more out at the far corner of the yard, past where Merry can be heard.
            .thenExecute(()->{r[0]=f.player("ask_a",P);r[1]=f.player("ask_b",P);r[2]=f.player("ask_far",P);r[3]=f.player("ask_soon",P);f.at(r[2],P,11.5,0,-27.5);f.at(r[3],P,10.5,0,-26.5);})
            .thenWaitUntil(()->h.assertTrue(GrowingThings.actor(l,b,0)!=null&&GrowingThings.actor(l,b,2)!=null,"the family at home"))
            .thenExecute(()->{LiteraryActor.SAID.add(ear);in[0]=l.getGameTime();f.at(r[0],P,-4.5,0,-13.5);})
            .thenIdle(10)
            .thenExecute(()->f.at(r[1],P,-6.5,0,-13.5))
            .thenWaitUntil(()->h.assertTrue(f.own(r[0],P).getBoolean("Asked")&&f.own(r[1],P).getBoolean("Asked"),"the two by the line asked: "+washing))
            // One of the far two walks up while she is still saying it: they have it as the others did.
            .thenExecute(()->{
                h.assertTrue(GrowingThings.actor(l,b,0).saying()&&!f.own(r[3],P).getBoolean("Asked")&&!f.own(r[2],P).getBoolean("Asked"),"Merry still saying it, the two out of hearing not asked");
                f.at(r[3],P,-3.5,0,-15.5);
            })
            .thenWaitUntil(()->h.assertTrue(f.own(r[3],P).getBoolean("Asked"),"the one who walked up while she was saying it has it too"))
            .thenWaitUntil(()->h.assertTrue(!GrowingThings.actor(l,b,0).saying(),"Merry has finished saying it"))
            .thenExecute(()->{
                h.assertTrue(washing.size()==1,"Merry asked once for the three of them, not "+washing.size()+" times");
                h.assertTrue(washing.get(0)-in[0]>=GrowingThings.READ_FIRST,"and not over the line about the shoots: "+(washing.get(0)-in[0])+" ticks after the first came in");
                h.assertTrue(f.own(r[2],P).getBoolean("SawShoots")&&!f.own(r[2],P).getBoolean("Asked"),"the one still out of hearing has not been asked: "+f.own(r[2],P));
                f.at(r[2],P,-4.5,0,-15.5);
            })
            // The other comes near once she has finished: asked, and not straight after.
            .thenWaitUntil(()->h.assertTrue(f.own(r[2],P).getBoolean("Asked"),"the last asked on coming near"))
            .thenExecute(()->h.assertTrue(washing.size()==2&&washing.get(1)-washing.get(0)>=GrowingThings.ASK_AGAIN,"asked again for the one who had not heard, and not straight after: "+washing))
            // The kitchen: the two of them in at the back door a moment apart, and John asks once.
            .thenExecute(()->f.at(r[0],P,-7.5,1,-32.5))
            .thenIdle(5)
            .thenExecute(()->f.at(r[1],P,-3.5,1,-33.5))
            .thenWaitUntil(()->h.assertTrue(f.own(r[0],P).getBoolean("AskedTable")&&f.own(r[1],P).getBoolean("AskedTable"),"the two in the kitchen asked: "+table))
            .thenIdle(60)
            .thenExecute(()->h.assertTrue(table.size()==1&&f.own(r[0],P).getBoolean("Inside")&&f.own(r[1],P).getBoolean("Inside"),"John asked once for the two of them, not "+table.size()+" times"))
            .thenExecute(GrowingThingsTests::close)
            .thenSucceed();
    }

    @GameTest(template="empty",batch="growing_things_together",timeoutTicks=6000)
    public static void togetherWhatReachesTheTable(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);
        ServerPlayer[] r=new ServerPlayer[4];
        h.startSequence().thenExecute(()->f.chunks.hold(l,box(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the yard is resident"))
            .thenExecute(()->{r[0]=f.player("grow_a",P);r[1]=f.player("grow_b",P);r[3]=f.player("grow_gone",P);})
            .thenWaitUntil(()->h.assertTrue(GrowingThings.actor(l,b,2)!=null,"the family at home"))
            // A reader takes a piece of washing and goes back out through the gate.
            .thenExecute(()->{f.at(r[3],P,-4.5,0,-13.5);h.assertTrue(use(f,r[3],new BlockPos(-8,1,GrowingThingsSite.LINE_Z)),"a piece of washing");})
            .thenExecute(()->f.at(r[3],P,3.5,0,-2.5))
            .thenWaitUntil(()->h.assertTrue(f.own(r[3],P).getBoolean("Completed")&&f.own(r[3],P).getString("Outcome").equals(GrowingThings.WENT),"back out through the gate"))
            .thenExecute(()->{var text=account(r[3]);h.assertTrue(text.contains("I took a piece off the line")&&text.contains("I went back out through the gate before supper was up"),"the one who left: "+text);})
            // Two readers on the table's two ends: no John needed.
            .thenExecute(()->{f.at(r[0],P,-7.5,1,-32.5);f.at(r[1],P,-3.5,1,-33.5);})
            .thenWaitUntil(()->h.assertTrue(f.own(r[0],P).getBoolean("Inside")&&f.own(r[1],P).getBoolean("Inside"),"both in the kitchen"))
            .thenExecute(()->{h.assertTrue(use(f,r[0],GrowingThingsSite.TABLE_AT),"the west end");h.assertTrue(use(f,r[1],GrowingThingsSite.TABLE_AT.east()),"the east end");})
            .thenWaitUntil(()->h.assertTrue(GrowingThings.flag(b,"table")&&f.own(r[0],P).getString("TableWith").equals("grow_b")&&f.own(r[1],P).getString("TableWith").equals("grow_a"),"carried by the two of them: "+f.own(r[0],P)))
            // Merry finishes the washing herself; the afternoon passes with nobody watching the step.
            .thenExecute(()->GrowingThings.skip(b,GrowingThings.CHORE_LIMIT))
            .thenWaitUntil(()->h.assertTrue(GrowingThings.phase(b)==GrowingThings.AFTERNOON,"the afternoon"))
            .thenExecute(()->{for(int i=0;i<2;i++){f.at(r[i],P,5.5+i,1,-40.5);f.look(r[i],b.offset(5,2,-46));}GrowingThings.skip(b,GrowingThings.JOHN_GONE);})
            .thenWaitUntil(()->h.assertTrue(GrowingThings.flag(b,"gone"),"John gone"))
            .thenExecute(()->GrowingThings.skip(b,GrowingThings.FLOOR_3))
            .thenWaitUntil(()->h.assertTrue(GrowingThings.phase(b)==GrowingThings.SUPPER,"supper"))
            // A reader who comes in partway, and supper carried up without his plate.
            .thenExecute(()->{r[2]=f.player("grow_late",P);h.assertTrue(f.own(r[2],P).getBoolean("Partway"),"a reader who comes in partway");f.at(r[2],P,4.5,1,-41.5);
                f.at(r[0],P,.5,1,-36.5);for(var what:List.of("Take the pot","Take the loaf")){h.assertTrue(use(f,r[0],GrowingThingsSite.COUNTER),"the counter");h.assertTrue(Talk.choose(r[0],what),what);}
                f.at(r[0],P,-3.5,6,-31.5);h.assertTrue(use(f,r[0],GrowingThingsSite.UP_TABLE),"set down upstairs");})
            .thenWaitUntil(()->{for(int i=0;i<3;i++)h.assertTrue(f.own(r[i],P).getBoolean("Completed"),r[i].getGameProfile().getName()+" resolved");})
            .thenExecute(()->{
                for(int i=0;i<3;i++)h.assertTrue(f.own(r[i],P).getString("Outcome").equals(GrowingThings.LEFT)&&WitnessAccount.has(f.data(),r[i].getUUID(),WitnessAccount.Story.GROWING_THINGS),"his plate left downstairs: "+r[i].getGameProfile().getName());
                h.assertTrue(f.own(r[0],P).getString("GrowingRole").equals("actor")&&f.own(r[1],P).getString("GrowingRole").equals("witness")&&f.own(r[2],P).getString("GrowingRole").equals("partway")&&f.own(r[3],P).getString("GrowingRole").equals("actor"),"actor, witness, partway, and the one who left");
                var a=account(r[0]);var w=account(r[1]);var late=account(r[2]);
                h.assertTrue(a.contains("I carried the pot and the loaf up")&&a.contains("His plate stayed downstairs")&&a.contains("I took one end of the table and grow_b took the other"),"the carrier's account: "+a);
                h.assertTrue(w.contains("I didn't carry anything")&&w.contains("His plate stayed downstairs")&&w.contains("I took one end of the table and grow_a took the other"),"the witness's account: "+w);
                h.assertTrue(late.contains("I came in late"),"the late reader's account: "+late);
                for(var p:r)h.assertTrue(holds(p,GrowingRegistry.SEED_HEAD.get()),"a seed head each: "+p.getGameProfile().getName());
            })
            .thenExecute(()->{if(active!=null){active.close();active=null;}GrowingThings.clearAll();})
            .thenSucceed();
    }
}
