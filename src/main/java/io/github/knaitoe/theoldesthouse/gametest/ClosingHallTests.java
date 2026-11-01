package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.209, the closing hall, played with real readers: dealt only in the dark band, the children at its far end; Daisy's drawing
 * sealing the way in and the walls stepping in, stage by stage; the hall waiting a hand's width off a child and shoving a reader;
 * the children put through the squeeze first and the reader after them, which readies Karen's interview; a reader who goes in
 * first losing the child, both left in the wall, and the handprint waiting in the dark halls; going back for the last child with
 * the hall nearly shut, which shuts it on the reader; and two readers closing it twice as fast.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_literary") @PrefixGameTestTemplate(false)
public final class ClosingHallTests {
    private ClosingHallTests(){}
    private static final LabyrinthPlace P=LabyrinthPlace.CLOSING_HALL;
    private static LiteraryTests.Fixture active;
    private static LiteraryTests.Fixture fixture(GameTestHelper h){if(active!=null)active.close();active=new LiteraryTests.Fixture(h);ClosingHall.clearAll();return active;}
    private static void close(){if(active!=null){active.close();active=null;}ClosingHall.clearAll();}
    private static net.minecraft.nbt.CompoundTag set(LiteraryTests.Fixture f){return ClosingHall.set(f.in.getServer());}
    private static void hold(LiteraryTests.Fixture f){var b=f.base(P);f.chunks.hold(f.in,new AABB(b.getX()-21,b.getY()-3,b.getZ()-50,b.getX()+7,b.getY()+10,b.getZ()+3));}
    private static boolean ash(LiteraryTests.Fixture f,int x,int y,int z){return f.in.getBlockState(f.base(P).offset(x,y,z)).is(DarkHallRegistry.material("ash"));}
    private static boolean air(LiteraryTests.Fixture f,int x,int y,int z){return f.in.getBlockState(f.base(P).offset(x,y,z)).isAir();}
    private static void clickFrom(LiteraryTests.Fixture f,ServerPlayer p,BlockPos rel){var at=f.base(P).offset(rel);NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(p,InteractionHand.MAIN_HAND,at,new BlockHitResult(at.getCenter(),Direction.EAST,at,false)));}
    private static LiteraryActor kid(ServerPlayer p,String who){return ClosingHall.child(p,who);}
    /** Reads Daisy's drawing from where she left it, between the two of them. */
    private static void begin(LiteraryTests.Fixture f,ServerPlayer p){f.source(p,P);f.at(p,P,.5,0,-41.5);}

    @GameTest(template="empty",batch="closing_deal",timeoutTicks=400)
    public static void dealtOnlyInTheDarkBandWithTheChildrenAtTheFarEnd(GameTestHelper h){
        var f=fixture(h);var p=f.player("closing_deal",P);
        h.startSequence().thenExecute(()->hold(f)).thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the hall is resident"))
            .thenExecute(()->{
                h.assertTrue(LiteraryRooms.isLiterary(P)&&!LiteraryRooms.outside(P)&&DarkHalls.horror(P)&&P.kind()==LabyrinthPlace.Kind.ONE_SHOT,"an interior horror of the dark halls, played in one visit");
                var story=WitnessAccount.Story.of(P.id());h.assertTrue(story!=null&&story.kind.equals("survival")&&WitnessAccount.Story.values().length==WitnessPool.SOURCES&&WitnessAccount.REQUIRED==WitnessPool.REQUIRED,"a survival story, and the Witness pool grows by one");
                h.assertTrue(LiteraryRooms.source(P).equals(ClosingHallSite.DRAWING)&&LiteraryRooms.ending(P).equals(ClosingHallSite.TRANSCRIPT_DOOR),"Daisy's drawing is the source and the interview by the way in the account");
                var d=new LabyrinthData();var id=UUID.randomUUID();
                for(int depth=1;depth<=17;depth++){d.pushReturn(id,new LabyrinthData.Waypoint(HouseDimensions.INTERIOR,new Vec3(depth,64,0),0F,true));
                    h.assertTrue(LiteraryVignettes.canDeal(d,id,P)==(depth>=DarkHalls.DARK_DEPTH),"dealt only from depth sixteen (depth "+depth+")");}
                var chad=kid(p,"Chad");var daisy=kid(p,"Daisy");
                h.assertTrue(chad!=null&&daisy!=null&&chad.phase()==ClosingHall.CHAD_PHASE&&daisy.phase()==ClosingHall.DAISY_PHASE,"Chad and Daisy are there");
                h.assertTrue(chad.getBbHeight()<1.3F&&daisy.getBbHeight()<1.3F,"at a child's height");
                h.assertTrue(chad.blockPosition().distManhattan(f.base(P).offset(ClosingHallSite.CHAD))<=1&&daisy.blockPosition().distManhattan(f.base(P).offset(ClosingHallSite.DAISY))<=1,"at the far end of the hall");
                h.assertTrue(HorrorBlock.is(f.in.getBlockState(f.base(P).offset(ClosingHallSite.DRAWING)),HorrorBlock.Kind.DRAWING),"her drawing lies between them");
                h.assertTrue(air(f,3,0,-20)&&air(f,0,5,-20)&&air(f,-3,0,-30),"the hall is open, seven wide and six high");
                h.assertTrue(ash(f,ClosingHallSite.TUNNEL_IN,0,ClosingHallSite.SQUEEZE_Z)&&air(f,ClosingHallSite.TUNNEL_IN-1,0,ClosingHallSite.SQUEEZE_Z),"the squeeze is there behind the wall, its mouth shut");
            }).thenSucceed();
    }
    @AfterBatch(batch="closing_deal") public static void dealDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="closing_steps",timeoutTicks=400)
    public static void theDrawingSealsTheWayInAndTheHallClosesInSteps(GameTestHelper h){
        var f=fixture(h);var p=f.player("closing_steps",P);
        h.startSequence().thenExecute(()->hold(f)).thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the hall is resident"))
            .thenExecute(()->{kid(p,"Chad");kid(p,"Daisy");begin(f,p);h.assertTrue(f.own(p,P).getBoolean("Read_Source"),"the drawing is read");})
            .thenWaitUntil(()->h.assertTrue(set(f).getBoolean("Run"),"looking up from the drawing starts it"))
            .thenExecute(()->{ClosingHall.TEST_HOLD=true;
                h.assertTrue(ClosingHall.stage(set(f))==1,"the first step is at once");
                h.assertTrue(ash(f,0,0,-1)&&ash(f,0,2,-1)&&ash(f,1,5,-1),"the way in is sealed behind them");
                h.assertTrue(air(f,ClosingHallSite.TUNNEL_IN,0,ClosingHallSite.SQUEEZE_Z),"and low in the west wall near it, the squeeze is open");
                h.assertTrue(ash(f,3,0,-20)&&ash(f,-3,4,-30)&&air(f,2,0,-20)&&air(f,-2,5,-30),"the walls have stepped in a block");
                ClosingHall.stage(p,4);
                h.assertTrue(ash(f,2,0,-20)&&air(f,1,0,-20)&&ash(f,0,4,-20)&&air(f,0,3,-20),"by the fourth step the hall is three wide and four high");
                h.assertTrue(air(f,-2,0,ClosingHallSite.SQUEEZE_Z)&&air(f,-3,0,ClosingHallSite.SQUEEZE_Z),"and the squeeze is longer for it");
                ClosingHall.stage(p,8);
                h.assertTrue(ash(f,1,0,-20)&&air(f,0,0,-20)&&ash(f,0,1,-20),"at the eighth it is a block wide and a block high");
                h.assertTrue(air(f,-1,0,ClosingHallSite.SQUEEZE_Z)&&ash(f,-1,0,ClosingHallSite.SQUEEZE_Z-1),"the squeeze runs on through what was hall");
                h.assertTrue(ClosingHall.crawling(p,f.base(P)),"and the whole hall is a crawl");
                h.assertTrue(LiteraryVignettes.retreatLocked(p),"nobody in it goes back out the way they came");
            }).thenSucceed();
    }
    @AfterBatch(batch="closing_steps") public static void stepsDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="closing_wait",timeoutTicks=500)
    public static void theHallWaitsForAChildAndShovesAReader(GameTestHelper h){
        var f=fixture(h);var p=f.player("closing_wait",P);float[] health=new float[1];
        h.startSequence().thenExecute(()->hold(f)).thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the hall is resident"))
            .thenExecute(()->{kid(p,"Chad");kid(p,"Daisy");begin(f,p);})
            .thenWaitUntil(()->h.assertTrue(set(f).getBoolean("Run"),"it has started"))
            .thenExecute(()->{ClosingHall.TEST_HOLD=true;ClosingHall.stage(p,3);var b=f.base(P);
                kid(p,"Daisy").moveTo(b.getX()+2.5,b.getY(),b.getZ()-19.5);
                f.at(p,P,-1.5,0,-25.5);p.invulnerableTime=0;health[0]=p.getHealth();
                ClosingHall.TEST_HOLD=false;ClosingHall.edit(p.server,t->t.putInt("Next",1));})
            .thenIdle(6)
            .thenExecute(()->{var t=set(f);
                h.assertTrue(ClosingHall.stage(t)==3&&t.getInt("Wait")>0,"a step that would close on her waits: stage "+ClosingHall.stage(t)+", waited "+t.getInt("Wait"));
                h.assertTrue(air(f,2,0,-20)&&air(f,2,0,-24),"a hand's width off her, nothing has closed");
                f.at(p,P,-1.5,0,-25.5);ClosingHall.edit(p.server,s->s.putInt("Wait",ClosingHall.WAIT_MAX));})
            .thenIdle(3)
            .thenExecute(()->{var t=set(f);
                h.assertTrue(ClosingHall.stage(t)>=4,"it waits only so long");
                h.assertTrue(air(f,2,0,-20)&&ash(f,2,0,-24),"and then closes round her, never on her");
                var r=p.position().subtract(Vec3.atLowerCornerOf(f.base(P)));
                h.assertTrue(r.x>=-1&&r.x<2,"a reader in its way is shoved into what is left: x "+r.x);
                h.assertTrue(p.getHealth()<health[0],"and hurt");
            }).thenSucceed();
    }
    @AfterBatch(batch="closing_wait") public static void waitDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="closing_through",timeoutTicks=1600)
    public static void theChildrenGoFirstThroughTheSqueezeAndTheReaderAfterThem(GameTestHelper h){
        var f=fixture(h);var p=f.player("closing_through",P);double[] speed=new double[1];
        h.startSequence().thenExecute(()->hold(f)).thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the hall is resident"))
            .thenExecute(()->{kid(p,"Chad");kid(p,"Daisy");speed[0]=p.getAttributeValue(Attributes.MOVEMENT_SPEED);begin(f,p);})
            .thenWaitUntil(()->h.assertTrue(set(f).getBoolean("Run"),"it has started"))
            .thenExecute(()->{ClosingHall.TEST_HOLD=true;ClosingHall.stage(p,2);f.at(p,P,.5,0,-42.5);
                LiteraryVignettes.talk(p,kid(p,"Chad"));LiteraryVignettes.talk(p,kid(p,"Daisy"));
                var t=set(f);h.assertTrue(t.hasUUID("ChadHand")&&t.hasUUID("DaisyBack"),"Chad by the hand, Daisy on the reader's back");})
            .thenIdle(2)
            .thenExecute(()->{h.assertTrue(p.getAttributeValue(Attributes.MOVEMENT_SPEED)<speed[0]*.6,"carrying her slows the reader to a crawl's pace");
                f.at(p,P,-1.5,0,-2.5);})
            .thenIdle(2)
            .thenExecute(()->{var m=ClosingHallSite.mouth(2);clickFrom(f,p,m.above());clickFrom(f,p,m.above());var t=set(f);
                h.assertTrue(t.getInt("DaisyThrough")>0&&t.getInt("ChadThrough")>0,"at the squeeze's mouth they go through, first");})
            .thenIdle(2)
            .thenExecute(()->h.assertTrue(p.getAttributeValue(Attributes.MOVEMENT_SPEED)>=speed[0]*.99,"with her through, the reader is light again"))
            .thenWaitUntil(()->{var t=set(f);h.assertTrue(t.getBoolean("ChadGone")&&t.getBoolean("DaisyGone"),"they crawl through and run off up the passage past it");})
            .thenExecute(()->f.at(p,P,-13.5,0,-3.5))
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Ready"),"the reader comes through after them, and the interview is ready"))
            .thenExecute(()->{h.assertTrue(f.own(p,P).getString("Outcome").equals("sent_them_through"),"they sent them through first");
                f.read(p,P,ClosingHallSite.TRANSCRIPT);
                h.assertTrue(f.own(p,P).getBoolean("Completed")&&WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.CLOSING_HALL),"reading it in the room past the squeeze resolves the story");
                var text=ClosingHallTexts.accountText(p.getGameProfile().getName(),f.own(p,P));
                h.assertTrue(text.contains(p.getGameProfile().getName())&&text.contains("came out last"),"Karen names the reader, and says who came out last");
            }).thenSucceed();
    }
    @AfterBatch(batch="closing_through") public static void throughDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="closing_left",timeoutTicks=600)
    public static void aReaderWhoGoesFirstLosesTheChildAndBothAreLeftInTheWall(GameTestHelper h){
        var f=fixture(h);var p=f.player("closing_left",P);
        h.startSequence().thenExecute(()->hold(f)).thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the hall is resident"))
            .thenExecute(()->{kid(p,"Chad");kid(p,"Daisy");begin(f,p);})
            .thenWaitUntil(()->h.assertTrue(set(f).getBoolean("Run"),"it has started"))
            .thenExecute(()->{ClosingHall.TEST_HOLD=true;ClosingHall.stage(p,2);f.at(p,P,.5,0,-42.5);LiteraryVignettes.talk(p,kid(p,"Chad"));
                f.at(p,P,-1.5,0,-2.5);})
            .thenIdle(2)
            .thenExecute(()->f.at(p,P,-3.5,0,-2.5))
            .thenIdle(2)
            .thenExecute(()->{h.assertTrue(!set(f).hasUUID("ChadHand"),"going into the squeeze ahead of him, the reader loses Chad's hand");
                var b=f.base(P);p.teleportTo(f.in,b.getX()-13.5,b.getY(),b.getZ()-3.5,90,0);})
            .thenWaitUntil(()->h.assertTrue(set(f).getBoolean("Failed"),"with nobody left in the hall, the children are left in the wall"))
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getString("LeftBehind209").equals("Chad and Daisy"),"the account remembers who: "+f.own(p,P).getString("LeftBehind209")))
            .thenExecute(()->{h.assertTrue(!f.own(p,P).getBoolean("Ready"),"the visit fails");
                h.assertTrue(LiteraryVignettes.canDeal(depth(f,p),p.getUUID(),P),"and the hall is dealt again");
                ClosingHall.firstDarkDoor(p,f.base(P).offset(0,0,0));h.assertTrue(ClosingHall.printPending(p),"the first door of the next visit to the dark halls has a small handprint on its inside");
            }).thenSucceed();
    }
    private static LabyrinthData depth(LiteraryTests.Fixture f,ServerPlayer p){var d=f.data();while(d.returnDepth(p.getUUID())<DarkHalls.DARK_DEPTH)d.pushReturn(p.getUUID(),new LabyrinthData.Waypoint(HouseDimensions.INTERIOR,new Vec3(d.returnDepth(p.getUUID()),64,0),0F,true));return d;}
    @AfterBatch(batch="closing_left") public static void leftDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="closing_back",timeoutTicks=500)
    public static void goingBackForTheLastChildShutsTheHallOnTheReader(GameTestHelper h){
        var f=fixture(h);var p=f.player("closing_back",P);
        h.startSequence().thenExecute(()->hold(f)).thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the hall is resident"))
            .thenExecute(()->{kid(p,"Chad");kid(p,"Daisy");begin(f,p);})
            .thenWaitUntil(()->h.assertTrue(set(f).getBoolean("Run"),"it has started"))
            .thenExecute(()->{ClosingHall.TEST_HOLD=true;ClosingHall.stage(p,2);f.at(p,P,.5,0,-42.5);LiteraryVignettes.talk(p,kid(p,"Daisy"));f.at(p,P,-1.5,0,-2.5);})
            .thenIdle(2)
            .thenExecute(()->{clickFrom(f,p,ClosingHallSite.mouth(2).above());h.assertTrue(set(f).getInt("DaisyThrough")>0,"Daisy goes through");
                ClosingHall.stage(p,8);f.at(p,P,.5,0,-43.5);})
            .thenIdle(1)
            .thenExecute(()->{h.assertTrue(set(f).getInt("ChadThrough")==0&&!set(f).getBoolean("ChadLeft"),"Chad is still in the hall, in a pocket it left round him");
                p.invulnerableTime=0;LiteraryVignettes.talk(p,kid(p,"Chad"));
                h.assertTrue(set(f).getInt("ChadThrough")>0,"going back for him pushes him through ahead");
                h.assertTrue(ClosingHall.stage(set(f))==9,"and the hall shuts");
                h.assertTrue(p.isDeadOrDying(),"on the reader: a real death");
                var own=f.own(p,P);h.assertTrue(own.getString("WentBack209").equals("Chad")&&own.getBoolean("Ready")&&own.getString("Outcome").equals("went_back"),"the interview is ready, for whenever they come back to read it");
                var text=ClosingHallTexts.accountText("Reader",own);h.assertTrue(text.contains("Nobody came out last")&&text.contains("gone back for Chad")&&text.contains("Daisy was through"),"Karen says nobody came out last, and who went back for whom");
            }).thenSucceed();
    }
    @AfterBatch(batch="closing_back") public static void backDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="closing_pair",timeoutTicks=400)
    public static void twoReadersCloseItTwiceAsFast(GameTestHelper h){
        var f=fixture(h);var a=f.player("closing_pair_a",P);var b=f.player("closing_pair_b",P);
        h.startSequence().thenExecute(()->hold(f)).thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the hall is resident"))
            .thenExecute(()->{kid(a,"Chad");kid(a,"Daisy");f.at(b,P,-.5,0,-40.5);begin(f,a);})
            .thenWaitUntil(()->h.assertTrue(set(f).getBoolean("Run"),"it has started"))
            .thenExecute(()->{var t=set(f);
                h.assertTrue(t.getBoolean("Coop")&&t.getInt("Next")<=ClosingHall.INTERVAL/2&&t.getInt("Next")>=ClosingHall.INTERVAL/2-3,"with two in the hall it closes twice as fast: next step in "+t.getInt("Next"));
                LiteraryVignettes.talk(a,kid(a,"Chad"));LiteraryVignettes.talk(b,kid(b,"Daisy"));t=set(f);
                h.assertTrue(t.hasUUID("ChadHand")&&t.hasUUID("DaisyHand")&&!t.hasUUID("ChadBack")&&!t.hasUUID("DaisyBack"),"a hand each, and nobody carries");
            }).thenSucceed();
    }
    @AfterBatch(batch="closing_pair") public static void pairDone(ServerLevel l){close();}
}
