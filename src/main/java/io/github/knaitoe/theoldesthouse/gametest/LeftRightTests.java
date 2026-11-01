package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.205, the Left-Right Game, played on the road with real readers on it: the teletype and the night begun at the toll; the
 * right turns carrying the reader on, fork after fork, until the road is closed for the night; the wrong one leaving them on
 * the salt, and backing out with her held in sight; the first night's lesson and a later lunge that ends the night; the flare
 * on the divider, the key from the phone box and the chained gate; a lit dormer slowing a reader until they look down; and the
 * wagon's tailgate and the convoy log.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_literary") @PrefixGameTestTemplate(false)
public final class LeftRightTests {
    private LeftRightTests(){}
    private static final LabyrinthPlace P=LabyrinthPlace.LEFT_RIGHT;
    private static LiteraryTests.Fixture active;
    private static LiteraryTests.Fixture fixture(GameTestHelper h){if(active!=null)active.close();active=new LiteraryTests.Fixture(h);LeftRightGame.clearAll();return active;}
    private static void close(){if(active!=null){active.close();active=null;}LeftRightGame.clearAll();}

    private static int count(ServerPlayer p,Item item){int n=0;var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(inv.getItem(i).is(item))n+=inv.getItem(i).getCount();return n;}
    private static void hold(ServerPlayer p,Item item){var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(inv.getItem(i).is(item)){var s=inv.getItem(i).copy();inv.setItem(i,ItemStack.EMPTY);p.setItemInHand(InteractionHand.MAIN_HAND,s);return;}}
    private static net.minecraft.nbt.CompoundTag road(LiteraryTests.Fixture f){return LeftRightGame.road(f.in.getServer());}
    /** Walks the reader down a branch past where the road carries them on: 0 left, 1 right. */
    private static void take(LiteraryTests.Fixture f,ServerPlayer p,int side){f.at(p,P,side==0?-7.5:7.5,0,-53.5);p.setYRot(180);p.setXRot(0);}
    private static void night(LiteraryTests.Fixture f,ServerPlayer p,int done){var own=f.own(p,P);own.putInt("Visit",done);own.putInt("Beat",done);LiteraryVignettes.save(f.data(),p.getUUID(),P,own);f.arrive(p,P);}
    private static void hold(LiteraryTests.Fixture f){f.chunks.hold(f.out,new AABB(f.base(P)).inflate(48,8,64).move(16,0,-30));}

    @GameTest(template="empty",batch="left_right_toll",timeoutTicks=400)
    public static void theTeletypeGivesTheRuleAndTheFirstNightStartsOnTheLeft(GameTestHelper h){
        var f=fixture(h);var p=f.player("left_right_toll",P);var l=f.out;var b=f.base(P);
        h.assertTrue(LiteraryRooms.isLiterary(P)&&LiteraryRooms.outside(P)&&P.kind()==LabyrinthPlace.Kind.MULTI_VISIT&&WitnessAccount.Story.of(P.id())==WitnessAccount.Story.LEFT_RIGHT&&WitnessAccount.Story.LEFT_RIGHT.kind.equals("survival"),"an outdoor literary survival source over four nights");
        h.assertTrue(LiteraryRooms.source(P).equals(LeftRightRoad.TTY)&&LiteraryRooms.ending(P).equals(LeftRightRoad.LOGBOOK),"the teletype is the source and the convoy log the account");
        h.assertTrue(LeftRightBlock.is(l.getBlockState(b.offset(LeftRightRoad.TTY)),LeftRightBlock.Kind.TTY)&&LeftRightBlock.is(l.getBlockState(b.offset(LeftRightRoad.RADIO)),LeftRightBlock.Kind.RADIO)
            &&LeftRightBlock.is(l.getBlockState(b.offset(LeftRightRoad.DIVIDER_HEAD)),LeftRightBlock.Kind.JERSEY)&&LeftRightBlock.is(l.getBlockState(b.offset(-7,5,LeftRightRoad.FORK_Z)),LeftRightBlock.Kind.SIGN_L),"the booth's teletype and CB, the divider and the gantry's signs");
        var t=road(f);h.assertTrue(t.getBoolean("Run")&&t.getInt("RunVisit")==1&&t.getInt("J")==1&&t.getInt("Expect")==0&&t.getInt("Mode")==LeftRightGame.ROAD,"arriving begins the first night at the toll, starting on the left");
        f.source(p,P);h.assertTrue(f.own(p,P).getBoolean("Read_Source"),"the teletype roll is read");
        h.succeed();
    }
    @AfterBatch(batch="left_right_toll") public static void tollDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="left_right_turns",timeoutTicks=800)
    public static void eachRightTurnCarriesTheReaderOnUntilTheRoadIsClosedForTheNight(GameTestHelper h){
        var f=fixture(h);var p=f.player("left_right_turns",P);var b=f.base(P);
        var seq=h.startSequence().thenExecute(()->hold(f)).thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the road is resident"));
        int[] sides={0,1,0,1};
        for(int i=0;i<4;i++){int side=sides[i],n=i;
            seq=seq.thenExecute(()->take(f,p,side))
                .thenWaitUntil(()->h.assertTrue(road(f).getInt("J")==n+2,"turn "+(n+1)+" the right way carries the reader on"))
                .thenExecute(()->h.assertTrue(p.getZ()-b.getZ()>-27&&p.getZ()-b.getZ()<-14&&Math.abs(p.getX()-b.getX())<5,"back on the approach, the next fork ahead in the fog"));
        }
        seq.thenExecute(()->{var t=road(f);var own=f.own(p,P);
                h.assertTrue(own.getString("Turns205_1").equals("LRLR")&&t.getInt("Mode")==LeftRightGame.CLOSED&&own.getInt("Beat")==1,"four forks, left, right, left, right, and the night is done");
                h.assertTrue(LeftRightBlock.is(f.out.getBlockState(b.offset(-6,0,LeftRightRoad.FORK_Z-2)),LeftRightBlock.Kind.JERSEY),"barriers are out across both branches");})
            .thenSucceed();
    }
    @AfterBatch(batch="left_right_turns") public static void turnsDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="left_right_salt",timeoutTicks=800)
    public static void aWrongTurnEndsInSaltAndBackingOutWithHerInSightMakesItAForkAgain(GameTestHelper h){
        var f=fixture(h);var p=f.player("left_right_salt",P);var b=f.base(P);
        h.startSequence().thenExecute(()->hold(f)).thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the road is resident"))
            .thenExecute(()->{f.at(p,P,7.5,0,-40.5);p.setYRot(180);})
            .thenWaitUntil(()->h.assertTrue(road(f).getInt("Mode")==LeftRightGame.SAC,"the first fork taken right, the wrong way, ends in salt"))
            .thenExecute(()->{var t=road(f);h.assertTrue(p.getX()-b.getX()>LeftRightRoad.SAC_X0&&f.own(p,P).getInt("Sacs205")==1,"the reader is out on the salt");
                h.assertTrue(LeftRightBlock.is(f.out.getBlockState(b.offset(t.getInt("MarjX"),0,t.getInt("MarjZ"))),LeftRightBlock.Kind.MARJORIE),"she is standing by the cars");})
            .thenIdle(20)
            .thenExecute(()->{var t=road(f);h.assertTrue(t.getInt("Mode")==LeftRightGame.SAC&&t.getInt("MarjZ")==LeftRightRoad.MARJORIE.getZ(),"held in sight, she does not move");
                f.at(p,P,LeftRightRoad.SAC_ENTRY.x,0,LeftRightRoad.SAC_LINE+.8);p.lookAt(EntityAnchorArgument.Anchor.EYES,LeftRightRoad.at(b,t.getInt("MarjX")+.5,1.6,t.getInt("MarjZ")+.5));})
            .thenWaitUntil(()->h.assertTrue(road(f).getInt("Mode")==LeftRightGame.ROAD&&f.own(p,P).getInt("Backed205")==1,"backing out across the salt's edge with her in sight makes it the road again"))
            .thenExecute(()->h.assertTrue(road(f).getInt("J")==1&&p.getX()-b.getX()<5&&p.getZ()-b.getZ()<-14,"at the same fork, on the approach"))
            .thenSucceed();
    }
    @AfterBatch(batch="left_right_salt") public static void saltDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="left_right_lunge",timeoutTicks=900)
    public static void theFirstLungeIsALessonAndTheNextEndsTheNight(GameTestHelper h){
        var f=fixture(h);var p=f.player("left_right_lunge",P);var b=f.base(P);
        h.startSequence().thenExecute(()->hold(f)).thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the road is resident"))
            .thenExecute(()->{f.at(p,P,7.5,0,-40.5);p.setYRot(180);})
            .thenWaitUntil(()->h.assertTrue(road(f).getInt("Mode")==LeftRightGame.SAC,"on the salt"))
            .thenExecute(()->{p.setYRot(0);p.setXRot(0);})
            .thenWaitUntil(()->h.assertTrue(road(f).getBoolean("Taught")&&road(f).getInt("Mode")==LeftRightGame.ROAD,"turning away the first night, she comes, and Apollo explains; the reader is back at the fork"))
            .thenExecute(()->{h.assertTrue(f.own(p,P).getBoolean("Taught205")&&f.own(p,P).getInt("Lunged205")==0,"the lesson costs nothing");f.at(p,P,7.5,0,-40.5);p.setYRot(180);})
            .thenWaitUntil(()->h.assertTrue(road(f).getInt("Mode")==LeftRightGame.SAC,"the wrong way again"))
            .thenExecute(()->{p.setYRot(0);p.setXRot(0);})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getInt("Lunged205")==1,"turning away a second time, the night is over"))
            .thenExecute(()->{var own=f.own(p,P);h.assertTrue(own.getInt("Beat")==0&&own.getString("Turns205_1").isEmpty(),"the night is not done");
                h.assertTrue(!(p.getX()-b.getX()>LeftRightRoad.SAC_X0-1),"the reader is off the salt");})
            .thenSucceed();
    }
    @AfterBatch(batch="left_right_lunge") public static void lungeDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="left_right_bluejay",timeoutTicks=1000)
    public static void aFlareOnTheDividerLetsTheReaderWalkTheWrongWayForTheKeyToTheChainedGate(GameTestHelper h){
        var f=fixture(h);var p=f.player("left_right_bluejay",P);var b=f.base(P);
        h.startSequence().thenExecute(()->{hold(f);night(f,p,1);})
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready()&&road(f).getInt("RunVisit")==2&&road(f).getInt("Expect")==1,"the second night starts on the right"))
            .thenExecute(()->{f.click(p,P,LeftRightRoad.CRATE);h.assertTrue(count(p,LeftRightRegistry.FLARE.get())==1,"a flare from the crate");take(f,p,1);})
            .thenWaitUntil(()->h.assertTrue(road(f).getInt("J")==2&&road(f).getInt("Expect")==0,"right at the first fork"))
            .thenExecute(()->{h.assertTrue(LeftRightBlock.is(f.out.getBlockState(b.offset(-7,0,LeftRightRoad.GATE_Z)),LeftRightBlock.Kind.GATE)&&LeftRightBlock.is(f.out.getBlockState(b.offset(LeftRightRoad.KEY_HOOK)),LeftRightBlock.Kind.KEY_HOOK),"the left is chained and the phone box is down the right");
                f.out.setBlock(b.offset(0,1,LeftRightRoad.FORK_Z),LeftRightBlock.of(LeftRightBlock.Kind.FLARE,Direction.NORTH),3);
                f.at(p,P,8.5,0,-43.5);})
            .thenIdle(10)
            .thenExecute(()->{h.assertTrue(road(f).getInt("Mode")==LeftRightGame.ROAD,"with the flare burning on the divider the wrong branch is only a road");
                f.click(p,P,LeftRightRoad.PAYPHONE);h.assertTrue(f.own(p,P).getInt("Bonnie205")==1,"Bonnie on the line");
                f.click(p,P,LeftRightRoad.KEY_HOOK);h.assertTrue(count(p,LeftRightRegistry.KEY.get())==1&&f.own(p,P).getBoolean("Key205"),"the conduit key from the hook");
                hold(p,LeftRightRegistry.KEY.get());f.click(p,P,new BlockPos(-7,0,LeftRightRoad.GATE_Z));
                h.assertTrue(f.out.getBlockState(b.offset(-7,0,LeftRightRoad.GATE_Z)).getValue(LeftRightBlock.STAGE)==1&&f.own(p,P).getBoolean("Gate205"),"the key opens the chained gate");
                take(f,p,0);})
            .thenWaitUntil(()->h.assertTrue(road(f).getInt("J")==3&&f.own(p,P).getString("Turns205_2").equals("RL"),"through the gate and left, the right way"))
            .thenSucceed();
    }
    @AfterBatch(batch="left_right_bluejay") public static void bluejayDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="left_right_jubilation",timeoutTicks=800)
    public static void inJubilationALitWindowSlowsTheReaderUntilTheyLookDownAtTheLine(GameTestHelper h){
        var f=fixture(h);var p=f.player("left_right_jubilation",P);var b=f.base(P);var window=new BlockPos(-8,4,-21);
        h.startSequence().thenExecute(()->{hold(f);night(f,p,2);LeftRightGame.HOLD_LIT.add(window);})
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready()&&road(f).getInt("RunVisit")==3&&LeftRightBlock.is(f.out.getBlockState(b.offset(window)),LeftRightBlock.Kind.DORMER),"Jubilation's houses with only dormer windows"))
            .thenWaitUntil(()->h.assertTrue(f.out.getBlockState(b.offset(window)).getValue(LeftRightBlock.STAGE)==1,"a window lights"))
            // The other dormers light at random as the night goes on, and one may already have held the reader on the way in:
            // the count starts clean here, so it counts this window alone.
            .thenExecute(()->{f.at(p,P,.5,0,-16.5);var own=f.own(p,P);own.putInt("Gaze205",0);own.putInt("Locks205",0);own.putBoolean("GazeEp205",false);LiteraryVignettes.save(f.data(),p.getUUID(),P,own);
                p.lookAt(EntityAnchorArgument.Anchor.EYES,b.offset(window).getCenter());})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getInt("Gaze205")>=5&&p.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"left_right_gaze"))!=null,"looking at the lit window, the reader's legs go slow"))
            .thenExecute(()->p.setXRot(60))
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getInt("Gaze205")==0&&p.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"left_right_gaze"))==null,"looking down at the yellow line lets it go"))
            .thenExecute(()->h.assertTrue(f.own(p,P).getInt("Locks205")==1,"the account will say it held them once"))
            .thenSucceed();
    }
    @AfterBatch(batch="left_right_jubilation") public static void jubilationDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="left_right_wagon",timeoutTicks=900)
    public static void onTheLastNightTheWagonsTailgateEndsTheRoadAndTheLogResolvesIt(GameTestHelper h){
        var f=fixture(h);var p=f.player("left_right_wagon",P);var b=f.base(P);
        h.startSequence().thenExecute(()->{hold(f);f.source(p,P);night(f,p,3);})
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready()&&road(f).getInt("RunVisit")==4&&road(f).getInt("Expect")==1,"the fourth night, starting on the right"))
            .thenExecute(()->take(f,p,1)).thenWaitUntil(()->h.assertTrue(road(f).getInt("J")==2,"right"))
            .thenExecute(()->take(f,p,0)).thenWaitUntil(()->h.assertTrue(road(f).getInt("Mode")==LeftRightGame.END,"left, and the road ends"))
            .thenExecute(()->{h.assertTrue(LeftRightBlock.is(f.out.getBlockState(b.offset(LeftRightRoad.TAILGATE)),LeftRightBlock.Kind.TAILGATE)&&f.out.getBlockState(b.offset(0,-1,-32)).isAir(),"Alice's wagon idles before the sinkhole");
                h.assertTrue(!f.own(p,P).getBoolean("Ready"),"nothing is done until the tailgate comes down");
                f.click(p,P,LeftRightRoad.TAILGATE);var own=f.own(p,P);
                h.assertTrue(own.getBoolean("Ready")&&own.getString("Outcome").equals("opened_the_tailgate")&&f.out.getBlockState(b.offset(LeftRightRoad.TAILGATE)).getValue(LeftRightBlock.STAGE)==1,"the tailgate down ends the road");
                var flare=p.getInventory().items.stream().filter(s->s.is(LeftRightRegistry.FLARE.get())).findFirst();
                h.assertTrue(flare.isPresent()&&flare.get().get(DataComponents.CUSTOM_DATA).copyTag().hasUUID(LiteraryVignettes.OWNER),"Apollo's last flare is the reader's own");
                var text=LeftRightTexts.accountText("x",own);h.assertTrue(text.contains("Night four")&&text.contains("tailgate"),"the road log ends at the wagon");
                h.assertTrue(!WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.LEFT_RIGHT),"nothing resolves until the log is read");
                f.read(p,P,LeftRightRoad.LOGBOOK);
                h.assertTrue(WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.LEFT_RIGHT)&&f.own(p,P).getBoolean("Completed"),"reading the log at the booth resolves the road for this reader");})
            .thenSucceed();
    }
    @AfterBatch(batch="left_right_wagon") public static void wagonDone(ServerLevel l){close();}
}
