package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseSitting;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.348, The Supper, with real readers. The farm built whole and dealt only after the yard; the maze solvable from the House's door to
 * the clearing; the upstairs windows lit; the oak across the drive. One reader: the oak seen, "Wipe your boots" from inside, a chair at
 * the table, Ida's question and the jug; Corinne goes out to look at the porch light, and when nobody is watching her chair is gone and
 * her coat and keys are in the kitchen; Ida says her ride came; the kitchen seen through the hatch; her name said at the table and both
 * answers; the meat carved, and eaten, with an account that says only what this reader did. Then together: the kitchen door kept by Ida,
 * then slipped through while another reader keeps her talking; the keys taken and passed; a reader who leaves into the corn; a reader
 * who comes in partway; the keys laid in front of Ida, the porch light out, and each reader's own account. The farm set back after.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_literary") @PrefixGameTestTemplate(false)
public final class MarshSupperTests {
    private MarshSupperTests(){}
    private static final LabyrinthPlace P=LabyrinthPlace.MARSH_SUPPER;
    private static LiteraryTests.Fixture active;
    private static LiteraryTests.Fixture fixture(GameTestHelper h){if(active!=null)active.close();active=new LiteraryTests.Fixture(h);MarshSupper.clearAll();return active;}
    private static AABB box(LiteraryTests.Fixture f){var b=f.base(P);return new AABB(b.getX()-33,b.getY()-5,b.getZ()-87,b.getX()+33,b.getY()+18,b.getZ()+3);}
    private static String account(ServerPlayer p){for(var st:p.getInventory().items)if(st.is(Items.WRITTEN_BOOK)){var c=st.get(DataComponents.WRITTEN_BOOK_CONTENT);if(c!=null&&c.title().raw().equals(MarshSupperTexts.TITLE))return String.join(" ",c.pages().stream().map(pg->pg.raw().getString()).toList());}return "";}
    private static boolean holds(ServerPlayer p,net.minecraft.world.item.Item item){for(var st:p.getInventory().items)if(st.is(item))return true;return false;}
    /** A right-click on a block from where the reader stands. */
    private static void use(ServerPlayer p,BlockPos at){NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(p,InteractionHand.MAIN_HAND,at,new BlockHitResult(at.getCenter(),Direction.UP,at,false)));}
    private static void sit(LiteraryTests.Fixture f,ServerPlayer p,BlockPos seat){var b=f.base(P);var side=seat.getX()<MarshSupperSite.TABLE_W?-1.5:1.5;f.at(p,P,seat.getX()+.5+side,1,seat.getZ()+.5);HouseSitting.sit(p,b.offset(seat));}
    private static void lookUp(ServerPlayer p){p.setXRot(-90);p.setYHeadRot(p.getYRot());}
    private static boolean mazeSolves(){
        var seen=new HashSet<Long>();var queue=new ArrayDeque<int[]>();queue.add(new int[]{0,9});seen.add(0L*100+9);
        while(!queue.isEmpty()){var c=queue.poll();if(c[0]==MarshSupperSite.MAZE_ROWS-1&&c[1]==9)return true;
            for(var d:new int[][]{{1,0},{-1,0},{0,1},{0,-1}}){int r=c[0]+d[0],k=c[1]+d[1];if(MarshSupperSite.openCell(r,k)&&seen.add(r*100L+k))queue.add(new int[]{r,k});}}
        return false;
    }

    @GameTest(template="empty",batch="marsh_supper",timeoutTicks=4000)
    public static void aSupperFromTheMazeToTheMeat(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);
        ServerPlayer[] me=new ServerPlayer[1];
        h.startSequence().thenExecute(()->f.chunks.hold(l,box(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the farm is resident"))
            .thenExecute(()->{
                h.assertTrue(LiteraryRooms.isLiterary(P)&&LiteraryRooms.outside(P)&&P.isOneShot()&&P.authored()&&SceneClock.at(f.origin,b.offset(0,0,-66),io.github.knaitoe.theoldesthouse.house.HouseDimensions.OUTSIDE)==3,"a literary farm outside, one visit, built whole, at dusk");
                var story=WitnessAccount.Story.of(P.id());h.assertTrue(story!=null&&story.kind.equals("understanding")&&WitnessAccount.Story.values().length==WitnessPool.SOURCES&&WitnessAccount.REQUIRED==WitnessPool.REQUIRED,"an understanding story, the quota derived");
                h.assertTrue(mazeSolves(),"the maze runs from the House's door to the clearing");
                h.assertTrue(YardBlock.is(l.getBlockState(b.offset(MarshSupperSite.MAZE_X0,0,-3)),YardBlock.Kind.STALK)&&l.getBlockState(b.offset(0,0,-3)).isAir(),"corn round the trodden way in");
                h.assertTrue(MarshBlock.is(l.getBlockState(b.offset(-9,7,MarshSupperSite.HOUSE_S)),MarshBlock.Kind.WINDOW)&&l.getBlockState(b.offset(-9,7,MarshSupperSite.HOUSE_S)).getValue(MarshBlock.STAGE)==0,"the upstairs windows lit");
                h.assertTrue(l.getBlockState(b.offset(MarshSupperSite.TREE_X,0,-60)).is(net.minecraft.world.level.block.Blocks.OAK_LOG)&&l.getBlockState(b.offset(MarshSupperSite.TREE_X,1,-60)).getBlock() instanceof net.minecraft.world.level.block.RotatedPillarBlock,"the oak across the drive, two logs high");
                h.assertTrue(l.getBlockState(b.offset(MarshSupperSite.TREE_X,0,MarshSupperSite.LEDGE_Z)).isAir()&&!l.getBlockState(b.offset(MarshSupperSite.TREE_X,0,MarshSupperSite.ROOTS_Z)).isAir()&&l.getBlockState(b.offset(MarshSupperSite.TREE_X,-1,MarshSupperSite.DITCH_Z)).is(net.minecraft.world.level.block.Blocks.WATER),"a ledge between the root plate and the ditch");
                h.assertTrue(l.getBlockState(b.offset(MarshSupperSite.FRONT_DOOR)).getBlock() instanceof DoorBlock&&MarshBlock.is(l.getBlockState(b.offset(MarshSupperSite.COAT_HOOK)),MarshBlock.Kind.COAT)&&MarshBlock.is(l.getBlockState(b.offset(MarshSupperSite.MAT)),MarshBlock.Kind.MAT),"the front door, the coat on the hook, the mat");
                var text=LiteraryTexts.source(P).get(DataComponents.WRITTEN_BOOK_CONTENT);var all=String.join(" ",text.pages().stream().map(pg->pg.raw().getString()).toList());
                h.assertTrue(all.contains("There is always a plate")&&all.contains("Wipe your boots"),"the supper card: "+all);
                var probe=UUID.randomUUID();h.assertTrue(!MarshSupper.dealable(f.data(),probe),"not before the yard");
                f.data().visit(probe,LabyrinthPlace.YARD_TURNAROUND);h.assertTrue(MarshSupper.dealable(f.data(),probe),"after it");
                me[0]=f.player("supper_one",P);
            })
            .thenWaitUntil(()->h.assertTrue(MarshSupper.actor(l,b,MarshSupper.IDA)!=null&&MarshSupper.actor(l,b,MarshSupper.CORINNE)!=null,"the household and the guest at the table"))
            // The oak, seen from the drive; the porch; the mat.
            .thenExecute(()->f.at(me[0],P,MarshSupperSite.TREE_X-3.5,0,-60.5))
            .thenWaitUntil(()->h.assertTrue(f.own(me[0],P).getBoolean("SawTree"),"the oak across the drive is seen"))
            .thenExecute(()->f.at(me[0],P,-3.5,1,-57.5))
            .thenWaitUntil(()->h.assertTrue(f.own(me[0],P).getBoolean("Greeted"),"wipe your boots, from inside"))
            .thenExecute(()->f.at(me[0],P,MarshSupperSite.MAT.getX()+.5,1,MarshSupperSite.MAT.getZ()+.5))
            .thenWaitUntil(()->h.assertTrue(f.own(me[0],P).getBoolean("Wiped"),"boots wiped on the mat"))
            .thenExecute(()->sit(f,me[0],MarshSupperSite.READER_SEATS.get(0)))
            .thenWaitUntil(()->h.assertTrue(MarshSupper.stage(b)==MarshSupper.SOUP&&f.own(me[0],P).getBoolean("Seated"),"sat down, the meal begins: stage "+MarshSupper.stage(b)+" vehicle "+me[0].getVehicle()+" at "+me[0].position().subtract(Vec3.atLowerCornerOf(b))+" own "+f.own(me[0],P)))
            .thenExecute(()->MarshSupper.skip(b,500))
            .thenWaitUntil(()->h.assertTrue(Talk.waiting(me[0],MarshSupper.TALK)&&f.own(me[0],P).contains("Poured"),"Ida asks where they have come from, and the glass is filled"))
            .thenExecute(()->{Talk.choose(me[0],"From the road");var o=f.own(me[0],P);h.assertTrue(o.getString("Answer").equals("road")&&o.getString("Poured").equals("milk"),"answered, and poured milk with no read of an older reader: "+o);
                Talk.close(me[0]);lookUp(me[0]);MarshSupper.skip(b,MarshSupper.SOUP_LENGTH);})
            .thenWaitUntil(()->h.assertTrue(MarshSupper.stage(b)>=MarshSupper.CORINNE_OUT,"Corinne goes out to look at the porch light"))
            .thenWaitUntil(()->h.assertTrue(MarshSupper.stage(b)>=MarshSupper.CHAIR_GONE&&MarshSupper.flag(b,"corinneGone")&&MarshSupper.flag(b,"props")&&MarshSupper.flag(b,"chair"),"nobody watching: she is gone, her coat and keys in the kitchen, her chair taken away"))
            .thenExecute(()->{
                h.assertTrue(l.getBlockState(b.offset(MarshSupperSite.CORINNE_SEAT)).isAir()&&l.getBlockState(b.offset(MarshSupperSite.COAT_HOOK)).isAir(),"her chair gone and the hook bare");
                h.assertTrue(MarshBlock.is(l.getBlockState(b.offset(MarshSupperSite.KEYS)),MarshBlock.Kind.KEYS)&&MarshBlock.is(l.getBlockState(b.offset(MarshSupperSite.COAT_CHAIR)),MarshBlock.Kind.COAT),"her keys by the butcher's block, her coat over a chair");})
            .thenWaitUntil(()->h.assertTrue(MarshSupper.flag(b,"ride")&&f.own(me[0],P).getBoolean("HeardRide")&&f.own(me[0],P).getBoolean("Doubted"),"her ride came; but the drive was seen"))
            // The kitchen through the serving hatch.
            .thenExecute(()->{f.at(me[0],P,-.5,1,-65.5);f.look(me[0],b.offset(MarshSupperSite.KEYS));})
            .thenWaitUntil(()->h.assertTrue(f.own(me[0],P).getBoolean("SawKitchen")&&f.own(me[0],P).getString("KitchenHow").equals("hatch"),"through the hatch, the keys and the coat: "+f.own(me[0],P)))
            // Her name at the table.
            .thenExecute(()->{f.at(me[0],P,-1.5,1,-64.5);MarshSupper.talk(me[0],MarshSupper.actor(l,b,MarshSupper.IDA));h.assertTrue(Talk.choose(me[0],"Where's Corinne"),"her name is among the things to say: "+Talk.replies(me[0]));})
            .thenWaitUntil(()->h.assertTrue(MarshSupper.flag(b,"name")&&f.own(me[0],P).getBoolean("SaidName"),"her name said at the table"))
            .thenExecute(()->{sit(f,me[0],MarshSupperSite.READER_SEATS.get(0));lookUp(me[0]);})
            .thenWaitUntil(()->h.assertTrue(MarshSupper.stage(b)==MarshSupper.MEAT,"the meat is ready"))
            .thenWaitUntil(()->h.assertTrue(MarshSupper.flag(b,"meat")&&f.own(me[0],P).getBoolean("Served"),"Royce carves and serves"))
            .thenExecute(()->use(me[0],b.offset(MarshSupperSite.setting(MarshSupperSite.READER_SEATS.get(0)))))
            .thenWaitUntil(()->h.assertTrue(f.own(me[0],P).getBoolean("Completed"),"they eat what was served"))
            .thenExecute(()->{
                var o=f.own(me[0],P);h.assertTrue(o.getString("Outcome").equals("ate_at_the_marsh_table")&&WitnessAccount.has(f.data(),me[0].getUUID(),WitnessAccount.Story.MARSH_SUPPER),"resolved: ate at the Marsh table");
                var text=account(me[0]);
                h.assertTrue(text.contains("I had seen the drive")&&text.contains("Through the serving hatch")&&text.contains("At the table I asked where Corinne was")&&text.contains("Del said he had heard her calling")&&text.contains("I woke in the House with the taste"),"the account follows the record: "+text);
                h.assertTrue(text.contains("Milk.")&&!text.contains("Tea."),"the drink the reader was poured: "+text);
                h.assertTrue(holds(me[0],MarshRegistry.CLAPPER.get()),"the bell's clapper in their pocket");
                MarshSupper.resetNow(l,b);
                h.assertTrue(!l.getBlockState(b.offset(MarshSupperSite.CORINNE_SEAT)).isAir()&&MarshBlock.is(l.getBlockState(b.offset(MarshSupperSite.COAT_HOOK)),MarshBlock.Kind.COAT)&&l.getBlockState(b.offset(MarshSupperSite.KEYS)).isAir(),"the farm set back: her chair, her coat on the hook, no keys in the kitchen");
            })
            .thenExecute(()->{if(active!=null){active.close();active=null;}MarshSupper.clearAll();})
            .thenSucceed();
    }

    @AfterBatch(batch="marsh_supper") public static void done(net.minecraft.server.level.ServerLevel l){close();}
    @AfterBatch(batch="marsh_supper_together") public static void togetherDone(net.minecraft.server.level.ServerLevel l){close();}
    private static void close(){if(active!=null){active.close();active=null;}MarshSupper.clearAll();}

    @GameTest(template="empty",batch="marsh_supper_together",timeoutTicks=4000)
    public static void togetherTheKeysGoOnTheTable(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);
        ServerPlayer[] r=new ServerPlayer[4];
        h.startSequence().thenExecute(()->f.chunks.hold(l,box(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the farm is resident"))
            .thenExecute(()->{r[0]=f.player("supper_talker",P);r[1]=f.player("supper_sneak",P);r[2]=f.player("supper_leaver",P);})
            .thenWaitUntil(()->h.assertTrue(MarshSupper.actor(l,b,MarshSupper.IDA)!=null,"the household at the table"))
            .thenExecute(()->{for(int i=0;i<3;i++)sit(f,r[i],MarshSupperSite.READER_SEATS.get(i));})
            .thenWaitUntil(()->h.assertTrue(MarshSupper.stage(b)==MarshSupper.SOUP,"one supper for the party"))
            .thenExecute(()->{for(int i=0;i<3;i++){Talk.close(r[i]);lookUp(r[i]);}MarshSupper.skip(b,MarshSupper.SOUP_LENGTH);})
            .thenWaitUntil(()->h.assertTrue(MarshSupper.stage(b)>=MarshSupper.CHAIR_GONE,"her chair is gone for everyone"))
            .thenWaitUntil(()->h.assertTrue(f.own(r[0],P).getBoolean("HeardRide")&&f.own(r[1],P).getBoolean("HeardRide")&&f.own(r[2],P).getBoolean("HeardRide"),"every reader at the table hears her ride came"))
            .thenExecute(()->{
                // A fourth comes in partway, after her chair is gone.
                r[3]=f.player("supper_late",P);h.assertTrue(f.own(r[3],P).getBoolean("Partway"),"a reader who comes in after the chair is gone");
                // Ida keeps her kitchen: the door from the dining room, with nobody keeping her talking.
                f.at(r[1],P,-1.5,1,-70.5);for(int i=0;i<3;i++)Talk.close(r[i]);
                use(r[1],b.offset(MarshSupperSite.KITCHEN_DOOR));
                h.assertTrue(f.own(r[1],P).getInt("TurnedBack")==1,"Ida turns them back from the kitchen door");
                // Kept talking, she does not look up.
                f.at(r[0],P,-4.5,1,-62.5);MarshSupper.talk(r[0],MarshSupper.actor(l,b,MarshSupper.IDA));
                h.assertTrue(Talk.waiting(r[0],MarshSupper.TALK),"Ida talking about the farm");
                use(r[1],b.offset(MarshSupperSite.KITCHEN_DOOR));
                h.assertTrue(f.own(r[1],P).getBoolean("SlippedIn")&&f.own(r[0],P).getString("CoveredFor").equals("supper_sneak"),"through the door while Ida is kept talking");
                // In the kitchen, the keys.
                f.at(r[1],P,4.5,1,-66.5);use(r[1],b.offset(MarshSupperSite.KEYS));
                h.assertTrue(f.own(r[1],P).getBoolean("TookKeys")&&holds(r[1],MarshRegistry.KEYS.get())&&l.getBlockState(b.offset(MarshSupperSite.KEYS)).isAir(),"her keys taken from the side");
                // The third gets up and leaves, back into the corn.
                f.at(r[2],P,.5,0,-36.5);
            })
            .thenWaitUntil(()->h.assertTrue(f.own(r[2],P).getBoolean("Completed")&&f.own(r[2],P).getString("Outcome").equals("left_before_the_meat"),"a reader who leaves into the corn"))
            .thenExecute(()->{
                h.assertTrue(account(r[2]).contains("back into the corn in the dark"),"their account: "+account(r[2]));
                // The keys passed hand to hand, and laid down in front of Ida.
                Talk.close(r[0]);for(var st:r[1].getInventory().items)if(st.is(MarshRegistry.KEYS.get()))st.setCount(0);
                r[0].setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(MarshRegistry.KEYS.get()));
                f.at(r[1],P,-1.5,1,-66.5);f.at(r[3],P,-8.5,1,-65.5);
                MarshSupper.talk(r[0],MarshSupper.actor(l,b,MarshSupper.IDA));
                h.assertTrue(MarshSupper.stage(b)==MarshSupper.OVER,"the keys on the table end the meal");
                var light=l.getBlockState(b.offset(MarshSupperSite.PORCH_LIGHT));h.assertTrue(MarshBlock.is(light,MarshBlock.Kind.PORCH_LIGHT)&&light.getValue(MarshBlock.STAGE)==1,"the porch light goes out");
                h.assertTrue(MarshBlock.is(l.getBlockState(b.offset(MarshSupperSite.setting(MarshSupperSite.IDA_SEAT))),MarshBlock.Kind.KEYS),"her keys in front of Ida");
                var a=f.own(r[0],P);var w=f.own(r[1],P);var late=f.own(r[3],P);
                h.assertTrue(a.getString("Outcome").equals("put_her_keys_on_the_table")&&a.getString("SupperRole").equals("actor"),"the reader who laid them down: "+a);
                h.assertTrue(w.getString("Outcome").equals("put_her_keys_on_the_table")&&w.getString("SupperRole").equals("witness")&&w.getString("KeysBy").equals("supper_talker"),"the one who took them, a witness: "+w);
                h.assertTrue(late.getBoolean("Completed")&&late.getString("SupperRole").equals("partway"),"the late reader's part: "+late);
                h.assertTrue(account(r[0]).contains("I put her keys on the table")&&account(r[0]).contains("I kept Ida talking about the farm while supper_sneak"),"the actor's account: "+account(r[0]));
                h.assertTrue(account(r[1]).contains("supper_talker put her keys on the table")&&account(r[1]).contains("I had brought them out of the kitchen myself")&&account(r[1]).contains("I took her keys")&&account(r[1]).contains("Ida turned me back from the kitchen door"),"the witness's account: "+account(r[1]));
                h.assertTrue(account(r[3]).contains("I came in late"),"the partway account: "+account(r[3]));
                for(var p:r)h.assertTrue(WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.MARSH_SUPPER),"each reader resolved "+p.getGameProfile().getName());
                MarshSupper.resetNow(l,b);
                var lit=l.getBlockState(b.offset(MarshSupperSite.PORCH_LIGHT));h.assertTrue(lit.getValue(MarshBlock.STAGE)==0&&!l.getBlockState(b.offset(MarshSupperSite.CORINNE_SEAT)).isAir(),"set back: the porch light on, her chair at the table");
            })
            .thenExecute(()->{if(active!=null){active.close();active=null;}MarshSupper.clearAll();})
            .thenSucceed();
    }
}
