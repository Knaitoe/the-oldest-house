package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseConfig;
import io.github.knaitoe.theoldesthouse.house.HouseSitting;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.257, A Place for You, played with real readers through the conversation panel: a reader the House knows nothing about,
 * a guest at the welcome, who clears the plates, keeps the bathroom to themselves, hands Kit back the bag, promises Mara an evening,
 * holds the door, stops the routine, says it to everyone, props the door, takes the picture down and sits with Mara before she
 * decides (the house opened); a reader the quiet rooms read, welcomed back as family, whose things are laid out and whose glass is
 * turned, who asks for the morning, watches Kit's door become a cupboard and takes the road; and a reader with inference off, who
 * is told nothing remembered, and whose stale reply is ignored.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_literary") @PrefixGameTestTemplate(false)
public final class PlaceForYouTests {
    private PlaceForYouTests(){}
    private static final LabyrinthPlace P=LabyrinthPlace.PLACE_FOR_YOU;
    private static LiteraryTests.Fixture active;
    private static LiteraryTests.Fixture fixture(GameTestHelper h){if(active!=null)active.close();active=new LiteraryTests.Fixture(h);PlaceForYou.clearAll();Talk.clearAll();return active;}
    private static void close(){if(active!=null){active.close();active=null;}PlaceForYou.clearAll();Talk.clearAll();}
    private static void at(LiteraryTests.Fixture f,ServerPlayer p,double x,double y,double z,float yaw,float pitch){var b=f.base(P);p.stopRiding();p.teleportTo(f.level(P),b.getX()+x,b.getY()+y,b.getZ()+z,yaw,pitch);p.setYHeadRot(yaw);p.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);}
    private static boolean click(LiteraryTests.Fixture f,ServerPlayer p,BlockPos rel){var own=f.own(p,P);boolean handled=PlaceForYou.click(p,f.base(P),rel,own);LiteraryVignettes.save(f.data(),p.getUUID(),P,own);return handled;}
    private static void talk(LiteraryTests.Fixture f,ServerPlayer p,String who){var a=PlaceForYou.actor(f.level(P),f.base(P),who);if(a!=null)PlaceForYou.talk(p,a);}
    private static void say(GameTestHelper h,ServerPlayer p,String reply){h.assertTrue(Talk.choose(p,reply),"the panel offers \""+reply+"\": "+Talk.replies(p)+" after "+Talk.lines(p));}
    /** 0.4.689: at the sink Mara first asks the reader upstairs; a case about the sink itself would rather talk there. */
    private static void sinkTalk(LiteraryTests.Fixture f,GameTestHelper h,ServerPlayer p){talk(f,p,"mara");if(Talk.waiting(p,"room_ask"))say(h,p,"Can we talk here?");}
    /** The account as the mailbox gives it to this reader, from their own record. */
    private static String account(LiteraryTests.Fixture f,ServerPlayer p){var c=LiteraryTexts.ending(p,P,f.own(p,P)).get(DataComponents.WRITTEN_BOOK_CONTENT);return c==null?"":String.join(" ",c.pages().stream().map(pg->pg.raw().getString()).toList());}
    private static AABB lot(BlockPos b){return new AABB(b.getX()-19,b.getY()-4,b.getZ()-35,b.getX()+19,b.getY()+19,b.getZ()+1);}
    private static int chapter(LiteraryTests.Fixture f,ServerPlayer p){return f.own(p,P).getInt("Chapter");}

    /** The house resident, as built: the card at the reader's place, the bag nowhere, the gate shut; then in at the front door to Mara's welcome. */
    private static GameTestSequence welcome(GameTestHelper h,LiteraryTests.Fixture f,ServerPlayer p){
        var b=f.base(P);var l=f.level(P);
        return h.startSequence().thenExecute(()->f.chunks.hold(l,lot(b)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready()&&f.chunks.entityTicking(l,b,b.offset(0,0,-20)),"the house is resident"))
            .thenExecute(()->{
                h.assertTrue(LiteraryRooms.isLiterary(P)&&LiteraryRooms.outside(P)&&P.isOneShot()&&P.authored()&&SceneInstances.copyable(P),"a literary house under the open sky, one evening, built whole, a copy for each reader");
                var story=WitnessAccount.Story.of(P.id());h.assertTrue(story!=null&&story.kind.equals("understanding")&&WitnessAccount.Story.values().length==WitnessPool.SOURCES&&WitnessAccount.REQUIRED==WitnessPool.REQUIRED,"an understanding story, and the quota derives from the pool");
                h.assertTrue(PlaceForYouBlock.is(l.getBlockState(b.offset(PlaceForYouSite.CARD)),PlaceForYouBlock.Kind.CARD)&&l.getBlockState(b.offset(PlaceForYouSite.BAG_GATE)).isAir()&&!PlaceForYouSite.routineOut(l,b)&&PlaceForYouSite.pictureUp(l,b)&&!l.getBlockState(b.offset(PlaceForYouSite.GATE)).getValue(FenceGateBlock.OPEN),"the card at the reader's place, no bag yet, nothing laid out, a picture on the fridge, the gate shut");
                h.assertTrue(chapter(f,p)==PlaceForYou.APPROACH&&f.own(p,P).contains("Snap"),"the House's memory of this reader taken once, on the road");
                at(f,p,.5,0,-14.5,180,0);})
            .thenWaitUntil(()->h.assertTrue(Talk.waiting(p,"welcome")&&PlaceForYou.actor(l,b,"mara")!=null&&PlaceForYou.actor(l,b,"kit")!=null&&PlaceForYou.actor(l,b,"alex")!=null&&PlaceForYou.actor(l,b,"ren")!=null,"in at the door, the household home, and Mara's welcome open"))
            .thenExecute(()->h.assertTrue(Talk.lines(p).get(0).equals("Mara: You came.")&&Talk.replies(p).size()==3,"Mara says the reader came, and three answers are offered: "+Talk.lines(p)));
    }

    @GameTest(template="empty",batch="place_guest",timeoutTicks=7000)
    public static void aGuestOpensTheHouse(GameTestHelper h){
        var f=fixture(h);var p=f.player("place_guest",P);var b=f.base(P);var l=f.level(P);
        welcome(h,f,p)
            .thenExecute(()->{
                h.assertTrue(Talk.lines(p).contains("Mara: Come in, it's cold. The kettle's on.")&&Talk.lines(p).contains("Mara: Stay the night, at least. The spare room's made up."),"nothing the House saw: no remembered thing, and the spare room offered: "+Talk.lines(p));
                say(h,p,"I don't think I've been here before.");
                var own=f.own(p,P);h.assertTrue(own.getString("Claim").equals("guest")&&own.getInt("Chapter")==PlaceForYou.SUPPER&&!Talk.waiting(p),"a guest, and supper");
                f.source(p,P);h.assertTrue(f.own(p,P).getBoolean("Read_Source"),"the place card, with the reader's own name, read to the end");
                talk(f,p,"mara");say(h,p,"Who's Theo?");h.assertTrue(f.own(p,P).getBoolean("TheoNamed"),"Theo named");
                say(h,p,"Of course.");own=f.own(p,P);h.assertTrue(own.getString("Activity").equals("plates")&&own.getString("Helper").equals("Mara"),"clearing the plates with Mara");
                for(var plate:PlaceForYouSite.PLATES){h.assertTrue(click(f,p,plate)&&PlaceForYou.holding(p)==PlaceForYouRegistry.PLATE&&l.getBlockState(b.offset(plate)).isAir(),"a plate off the table, carried");
                    h.assertTrue(click(f,p,PlaceForYouSite.SINK)&&PlaceForYou.holding(p)==0,"and into the sink");}
                h.assertTrue(f.own(p,P).getInt("Plates")==3&&f.own(p,P).getBoolean("ActivityDone"),"three plates to the sink");})
            .thenWaitUntil(()->h.assertTrue(chapter(f,p)==PlaceForYou.BATHROOM&&PlaceForYouSite.routineOut(l,b),"the bathroom made ready: something laid out (a washcloth, for a reader the House never saw)"))
            .thenExecute(()->at(f,p,6.5,0,-25.5,0,0))
            .thenWaitUntil(()->h.assertTrue(Talk.waiting(p,"care"),"Mara asks how the reader wants it"))
            .thenExecute(()->{say(h,p,"I'd like to do this on my own.");
                h.assertTrue(f.own(p,P).getString("Care").equals("private")&&!l.getBlockState(b.offset(PlaceForYouSite.BATH_DOOR)).getValue(DoorBlock.OPEN),"left alone, the door shut");
                h.assertTrue(click(f,p,PlaceForYouSite.ROUTINE)&&!PlaceForYouSite.routineOut(l,b)&&f.own(p,P).getBoolean("RoutineUsed"),"done, and put away");})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Repeated")&&PlaceForYouSite.routineOut(l,b),"laid out again, exactly as before"))
            .thenExecute(()->at(f,p,-1.5,0,-22.5,180,0))
            .thenWaitUntil(()->h.assertTrue(chapter(f,p)==PlaceForYou.BAG&&PlaceForYouBlock.is(l.getBlockState(b.offset(PlaceForYouSite.BAG_GATE)),PlaceForYouBlock.Kind.BAG),"out of the bathroom: Kit's bag at the gate"))
            .thenExecute(()->at(f,p,.5,0,-8.5,-14,15))
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("BagSeen")&&f.own(p,P).getInt("BagStage")==1,"the bag seen from the porch"))
            .thenExecute(()->at(f,p,6.5,0,-18.5,180,0))
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getInt("BagStage")==2&&PlaceForYouBlock.is(l.getBlockState(b.offset(PlaceForYouSite.BAG_KITCHEN)),PlaceForYouBlock.Kind.BAG),"while the reader was in the kitchen and not watching the gate, the bag came back in"))
            .thenWaitUntil(()->h.assertTrue(Talk.waiting(p,"bag"),"Mara and Kit, about the bag"))
            .thenExecute(()->{say(h,p,"Kit, where were you planning to go?");h.assertTrue(f.own(p,P).getBoolean("KitPlan")&&Talk.waiting(p,"bag"),"Kit's plan, and the question still open");
                say(h,p,"I shouldn't be deciding");var own=f.own(p,P);h.assertTrue(own.getBoolean("NotMine")&&own.getBoolean("Acknowledged")&&own.getBoolean("BagFree")&&!Talk.waiting(p),"not the reader's to decide; the bag free to pick up");})
            .thenWaitUntil(()->h.assertTrue(chapter(f,p)==PlaceForYou.SINK,"Mara at the sink"))
            .thenExecute(()->{
                h.assertTrue(click(f,p,PlaceForYouSite.BAG_KITCHEN)&&PlaceForYou.holding(p)==PlaceForYouRegistry.BAG,"Kit's bag, carried");
                talk(f,p,"kit");var own=f.own(p,P);h.assertTrue(own.getString("BagWhere").equals("kit")&&PlaceForYou.holding(p)==0&&PlaceForYouBlock.is(l.getBlockState(b.offset(PlaceForYouSite.BAG_GATE)),PlaceForYouBlock.Kind.BAG),"handed back to Kit, who sets it by the gate");
                sinkTalk(f,h,p);h.assertTrue(Talk.waiting(p,"sink"),"Mara about Theo: "+Talk.key(p));say(h,p,"I'll sit with you tonight.");
                own=f.own(p,P);h.assertTrue(own.getString("Promise").equals("company")&&own.getBoolean("TheoTold")&&own.getInt("Chapter")==PlaceForYou.DINNER,"a promise made; dinner again");
                talk(f,p,"mara");say(h,p,"Take the spoon.");own=f.own(p,P);h.assertTrue(own.getBoolean("SpoonTaken")&&PlaceForYouBlock.is(l.getBlockState(b.offset(PlaceForYouSite.SPOON)),PlaceForYouBlock.Kind.SPOON),"the spoon out of her hand, set down on the counter");
                h.assertTrue(PlaceForYouSite.routineOut(l,b),"the routine going on in the empty bathroom");
                PlaceForYouSite.frontDoor(l,b,true);at(f,p,.5,0,-13.5,0,0);})
            .thenWaitUntil(()->h.assertTrue(!PlaceForYouSite.doorOpen(l,b),"the front door swings shut ahead of a reader who helped Kit"))
            .thenExecute(()->{boolean clicked=click(f,p,PlaceForYouSite.FRONT_DOOR);h.assertTrue(clicked&&f.own(p,P).getBoolean("DoorHeld")&&PlaceForYouSite.doorOpen(l,b),"held open: clicked "+clicked+" open "+PlaceForYouSite.doorOpen(l,b)+" own "+f.own(p,P));
                h.assertTrue(click(f,p,PlaceForYouSite.ROUTINE)&&f.own(p,P).getBoolean("RoutineStopped")&&!PlaceForYouSite.routineOut(l,b),"a hand on it, and it stops");
                talk(f,p,"ren");h.assertTrue(Talk.waiting(p,"confront"),"at the table");say(h,p,"I'd like to say this to everyone.");
                h.assertTrue(Talk.lines(p).stream().anyMatch(s->s.startsWith("you: ")&&s.contains("The bag keeps coming back in.")&&s.contains("put themselves back out")),"the reader says what they saw: "+Talk.lines(p));
                say(h,p,"Let them go.");var own=f.own(p,P);h.assertTrue(own.getInt("Chapter")==PlaceForYou.OPEN&&own.getString("Confront").equals("open")&&own.getBoolean("KitGoing"),"opening the house; Kit picks up the bag");
                h.assertTrue(l.getBlockState(b.offset(PlaceForYouSite.GATE)).getValue(FenceGateBlock.OPEN)&&l.getBlockState(b.offset(PlaceForYouSite.BAG_GATE)).isAir(),"the gate left open behind Kit, the bag gone with them");
                at(f,p,.5,0,-13.5,0,0);h.assertTrue(click(f,p,PlaceForYouSite.FRONT_DOOR)&&f.own(p,P).getBoolean("Propped")&&PlaceForYouSite.doorOpen(l,b),"the hall chair set against the front door");
                at(f,p,6.5,0,-14.5,90,0);h.assertTrue(click(f,p,PlaceForYouSite.FRIDGE)&&f.own(p,P).getBoolean("PictureDown")&&!PlaceForYouSite.pictureUp(l,b),"the picture down off the fridge");})
            .thenWaitUntil(()->h.assertTrue(Talk.waiting(p,"mara_end"),"in the kitchen, Mara asks the reader to sit with her"))
            .thenExecute(()->{say(h,p,"Yes. I said I would.");h.assertTrue(f.own(p,P).getString("MaraEnd").equals("sat"),"she will decide after");
                h.assertTrue(HouseSitting.sit(p,b.offset(PlaceForYouSite.READER_CHAIR))&&p.getVehicle()!=null,"the reader sits at the table");
                talk(f,p,"alex");h.assertTrue(Talk.waiting(p,"alex_end"),"Alex is going");say(h,p,"As far as the road.");h.assertTrue(f.own(p,P).getString("AlexEnd").equals("walk"),"walked as far as the road");})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("SatDone")&&f.own(p,P).getBoolean("Ready"),"sat with her long enough; the house opened"))
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("KitLeft")&&PlaceForYou.actor(l,b,"kit")==null,"Kit gone from the road once out of sight"))
            .thenExecute(()->{var own=f.own(p,P);h.assertTrue(own.getString("Outcome").equals("opened_the_house")&&own.getBoolean("PromiseKept"),"the house opened, and the promise kept");
                h.assertTrue(own.getBoolean("Knocked")&&own.getBoolean("VoiceHeard")&&own.getBoolean("TheoSilent")&&own.getInt("BargainMax")==0,"0.4.686: the knock on the propped door's frame, the voice by the gate, and its silence; no bargain, since the picture was already down");
                p.stopRiding();f.read(p,P,LiteraryRooms.ending(P));})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Completed"),"the account read"))
            .thenExecute(()->{h.assertTrue(f.data().isCompleted(P.id()),"the story resolved");
                var text=account(f,p);
                h.assertTrue(text.contains("talked as if I had lived there")&&text.contains("cleared the plates")&&text.contains("asked to be on my own")&&text.contains("laid out again")&&text.contains("I gave it back to Kit")&&text.contains("\"I'll sit with you tonight.\" I did.")&&text.contains("I held it")&&text.contains("it stopped")&&text.contains("hall chair")&&text.contains("Kit went out through the gate")&&text.contains("try it, and see")&&text.contains("as far as the road")&&text.contains("somebody knocked at the front door")&&text.contains("said her name once more"),"the account says what this reader did and was told: "+text);
            })
            .thenSucceed();
    }
    @AfterBatch(batch="place_guest") public static void guestDone(ServerLevel l){close();}

    /**
     * 0.4.686 (docs/A_PLACE_FOR_YOU_0_4_686.md): somebody at the door. A reader who asked Mara for the other room asks Kit at the gate what
     * help they want; the front door swings shut and somebody knocks; Mara, and Alex and Ren, are asked their own questions; through the
     * door the voice asks after the spare key and answers who it is and what is for supper; opened, there is nobody on the step. While it
     * stands open the gap in the household's pictures fills as the reader looks at it, their chair goes from the table, Ren says what it
     * would cost, and Ren asks if they have met; shut, things go back. Propped open with the picture down, the voice stops, and only then
     * does Mara decide.
     */
    @GameTest(template="empty",batch="place_knock",timeoutTicks=7000)
    public static void somebodyAtTheDoor(GameTestHelper h){
        var f=fixture(h);var p=f.player("place_knock",P);var b=f.base(P);var l=f.level(P);
        welcome(h,f,p)
            .thenExecute(()->{
                say(h,p,"Thank you. I'll stay for supper.");f.source(p,P);
                // The evening as it stands after a confrontation in the other room.
                var own=f.own(p,P);own.putInt("Chapter",PlaceForYou.OPEN);own.putString("Confront","private");own.putString("First","Ren");own.putString("Helper","Ren");
                own.putString("Activity","tea");own.putBoolean("ActivityDone",true);own.putBoolean("Acknowledged",true);own.putBoolean("TheoTold",true);own.putString("Promise","listen");
                LiteraryVignettes.save(f.data(),p.getUUID(),P,own);
                at(f,p,.5,0,-14.5,180,0);})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("KitGoing")&&PlaceForYou.actor(l,b,"kit")!=null,"Kit going, the bag over a shoulder"))
            .thenExecute(()->{talk(f,p,"kit");h.assertTrue(Talk.waiting(p,"kit_gate"),"Kit on the way out: "+Talk.key(p));
                say(h,p,"What do you need from me?");h.assertTrue(f.own(p,P).getBoolean("AskedKit")&&f.own(p,P).getString("KitWants").equals("gate"),"Kit asked what help they want: the gate left open");})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Knocked")&&!PlaceForYouSite.doorOpen(l,b),"with Kit out through the gate, the front door swings shut, and somebody knocks"))
            .thenExecute(()->{
                talk(f,p,"mara");h.assertTrue(Talk.waiting(p,"mara_knock")&&Talk.lines(p).get(0).contains("He never knocked"),"Mara, at the knock: "+Talk.lines(p));
                say(h,p,"Do you want them to stay");h.assertTrue(Talk.lines(p).stream().anyMatch(t->t.contains("Unable to leave. That's the true answer.")),"her answer, after the other room: "+Talk.lines(p));
                say(h,p,"Is it him?");say(h,p,"I'll leave you be.");
                talk(f,p,"ren");h.assertTrue(Talk.waiting(p,"peers"),"Alex and Ren together: "+Talk.key(p));
                say(h,p,"Will you two go together");say(h,p,"Stay with me a minute.");
                var own=f.own(p,P);h.assertTrue(own.getString("AskedMara").equals("private")&&own.getBoolean("AskedIsHim")&&own.getBoolean("AskedPeers")&&!own.getBoolean("CostHeard"),"each asked their own question; nobody has said the cost yet");
                h.assertTrue(click(f,p,PlaceForYouSite.FRONT_DOOR)&&Talk.waiting(p,"door")&&Talk.lines(p).contains("A voice: Mara? Is the spare key still where it was? It isn't under the step."),"through the door, the voice: "+Talk.lines(p));
                say(h,p,"Who is this?");h.assertTrue(Talk.lines(p).stream().anyMatch(t->t.startsWith("A voice: It's Theo.")),"it says it is Theo: "+Talk.lines(p));
                say(h,p,"What's for supper?");say(h,p,"Open the door.");
                h.assertTrue(PlaceForYouSite.doorOpen(l,b)&&f.own(p,P).getBoolean("DoorAnswered")&&!Talk.waiting(p),"opened: nobody on the step");
                // In the sitting room, looking at the household's pictures over the sideboard.
                at(f,p,-5.5,0,-17.5,180,-24);})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getInt("Bargain")>=1&&PlaceForYouSite.picturesFilled(l,b),"while the door stands open, the gap in the row fills as the reader looks at it"))
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getInt("Bargain")>=2&&!PlaceForYouSite.readerChair(l,b),"then the reader's chair goes from the table while they are out of the kitchen"))
            .thenExecute(()->at(f,p,6.5,0,-18.5,90,0))
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("ChairSeen")&&f.own(p,P).getBoolean("CostHeard"),"the empty place seen, and Ren says what letting him in would cost"))
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Forgot")&&f.own(p,P).getString("Forgetter").equals("Ren")&&f.own(p,P).getInt("BargainMax")==3,"Ren, the first they sat with, asks if they have met"))
            .thenExecute(()->{at(f,p,.5,0,-13.5,0,0);
                h.assertTrue(click(f,p,PlaceForYouSite.FRONT_DOOR)&&Talk.waiting(p,"door_open"),"a hand on the open door");say(h,p,"Shut it.");
                h.assertTrue(!PlaceForYouSite.doorOpen(l,b)&&f.own(p,P).getBoolean("Shut"),"shut on it");})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getInt("Bargain")==0&&!PlaceForYouSite.picturesFilled(l,b)&&PlaceForYouSite.readerChair(l,b),"shut, the pictures and the chair go back as they were"))
            .thenExecute(()->{
                h.assertTrue(click(f,p,PlaceForYouSite.FRONT_DOOR)&&Talk.waiting(p,"door")&&Talk.lines(p).contains("A voice: It's only me. It's cold out here."),"the voice again: "+Talk.lines(p));
                say(h,p,"Open the door.");
                h.assertTrue(click(f,p,PlaceForYouSite.FRONT_DOOR)&&Talk.waiting(p,"door_open"),"the open door");say(h,p,"Set the hall chair against it.");
                h.assertTrue(f.own(p,P).getBoolean("Propped")&&PlaceForYouSite.doorOpen(l,b),"propped open with the hall chair");
                h.assertTrue(click(f,p,PlaceForYouSite.FRONT_DOOR)&&!Talk.waiting(p),"the chair holds it");
                at(f,p,6.5,0,-14.5,90,0);h.assertTrue(click(f,p,PlaceForYouSite.FRIDGE)&&f.own(p,P).getBoolean("PictureDown"),"the picture down off the fridge");
                h.assertFalse(Talk.waiting(p,"mara_end"),"Mara does not decide while the voice is still there");})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("TheoSilent"),"the way out open and the picture down: the voice says her name once more, and stops"))
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getString("MaraEnd").equals("gate")&&f.own(p,P).getString("RenEnd").equals("goes"),"then Mara sees Kit to the gate, and Ren goes with her"))
            .thenExecute(()->{talk(f,p,"alex");h.assertTrue(Talk.waiting(p,"alex_end"),"Alex is going");say(h,p,"No. Good luck.");})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Ready")&&f.own(p,P).getString("Outcome").equals("opened_the_house"),"the house opened"))
            .thenExecute(()->f.read(p,P,LiteraryRooms.ending(P)))
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Completed"),"the account read"))
            .thenExecute(()->{var text=account(f,p);
                for(var said:java.util.List.of("To leave the gate open behind them","somebody knocked at the front door","spare key","He said Theo","Whatever she had made","nobody on the step",
                        "a man in a work shirt came into the gap","My chair went from the table","Ren asked if we had met","if she let him in","I shut the door on it",
                        "Unable to leave, she said","sounded like him","Alex was going to the coast","said her name once more"))
                    h.assertTrue(text.contains(said),"the account has \""+said+"\": "+text);})
            .thenSucceed();
    }
    @AfterBatch(batch="place_knock") public static void knockDone(ServerLevel l){close();}

    /** 0.4.687 (docs/A_PLACE_FOR_YOU_0_4_687.md): the picture put back up keeps the evening. Kit is not brought back; Alex, who sat
     *  with the reader first and has said nothing about going, sits beside them; a new question gets the old answer; the gesture comes
     *  again; the reader's things are laid out again in the bathroom; the account says each. */
    @GameTest(template="empty",batch="place_keep",timeoutTicks=7000)
    public static void keepingAPlace(GameTestHelper h){
        var f=fixture(h);var p=f.player("place_keep",P);var b=f.base(P);var l=f.level(P);
        welcome(h,f,p)
            .thenExecute(()->{
                say(h,p,"It's good to be back.");f.source(p,P);
                var own=f.own(p,P);own.putInt("Chapter",PlaceForYou.OPEN);own.putString("Confront","open");own.putString("First","Alex");own.putString("Helper","Alex");
                own.putString("Activity","radio");own.putBoolean("ActivityDone",true);own.putBoolean("Acknowledged",true);own.putString("Promise","listen");
                LiteraryVignettes.save(f.data(),p.getUUID(),P,own);
                at(f,p,6.5,0,-14.5,90,0);})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("KitGoing"),"Kit going, out through the gate"))
            .thenExecute(()->{
                h.assertTrue(click(f,p,PlaceForYouSite.FRIDGE)&&f.own(p,P).getBoolean("PictureDown")&&!PlaceForYouSite.pictureUp(l,b),"the picture down off the fridge");
                h.assertTrue(click(f,p,PlaceForYouSite.FRIDGE)&&Talk.waiting(p,"fridge"),"a hand on the fridge again offers to put it back: "+Talk.key(p));
                say(h,p,"Put your picture back up.");
                var own=f.own(p,P);
                h.assertTrue(own.getInt("Chapter")==PlaceForYou.KEEP&&own.getBoolean("Kept")&&!own.getBoolean("PictureDown")&&PlaceForYouSite.pictureUp(l,b)&&PlaceForYouSite.readerChair(l,b),"the picture back up, the evening kept, the reader's chair at the table");
                h.assertTrue(HouseSitting.sit(p,b.offset(PlaceForYouSite.READER_CHAIR))&&p.getVehicle()!=null,"the reader sits down in their chair");})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getInt("KeepStage")>=1&&f.own(p,P).getString("Beside").equals("Alex"),"Alex, the first they sat with, is beside them: "+f.own(p,P).getString("Beside")))
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getInt("KeepStage")>=2,"the familiar gesture"))
            .thenWaitUntil(()->h.assertTrue(Talk.waiting(p,"keep_ask"),"Mara, to be asked something new: "+Talk.key(p)))
            .thenExecute(()->{
                say(h,p,"What are we doing tomorrow?");
                var own=f.own(p,P);var old=PlaceForYouTexts.detailLine(own.getString("Detail"),own.getCompound("Snap"));
                h.assertTrue(own.getString("KeepAsked").equals("tomorrow")&&own.getString("OldAnswer").equals(old)&&Talk.lines(p).contains("Mara: "+old),"a new question, and the answer she gave at the door: "+Talk.lines(p));})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("GestureAgain"),"the gesture again, the same way, with the same words"))
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Ready")&&f.own(p,P).getString("Outcome").equals("kept_a_place"),"the evening kept"))
            .thenExecute(()->{var kit=PlaceForYou.actor(l,b,"kit");h.assertTrue(f.own(p,P).getBoolean("KitLeft")||kit==null||kit.position().z>b.getZ()-12,"Kit is not sat back down");
                p.stopRiding();at(f,p,5.5,0,-24.5,180,20);})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("RoutineAgain")&&PlaceForYouSite.routineOut(l,b),"the reader's things laid out again in the bathroom, untouched"))
            .thenExecute(()->f.read(p,P,LiteraryRooms.ending(P)))
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Completed"),"the account read"))
            .thenExecute(()->{var text=account(f,p);
                for(var said:java.util.List.of("I put it back up","Alex sat down next to me","the last of the jug","I asked Mara what we were doing tomorrow",
                        "It was what she had said when I came in","did it again, the same way","laid out again, untouched","Nobody set a place for Kit","my chair was still pulled out"))
                    h.assertTrue(text.contains(said),"the account has \""+said+"\": "+text);
                h.assertFalse(text.contains("hall chair against the front door"),"and not the opened house's: "+text);})
            .thenSucceed();
    }
    @AfterBatch(batch="place_keep") public static void keepDone(ServerLevel l){close();}

    /**
     * 0.4.688, invite the past (E3): somebody knocks and the door is opened; Ren is asked what letting him in would cost; the reader's
     * picture comes down, Theo's photograph goes up in its place, and the reader tells him to come in. He walks in from the road, made
     * where nobody sees it; Mara knows him; Alex and Ren are gone the moment nobody is looking at them; Mara doesn't know the reader; she
     * tells him his things are out; he puts a cup in the right cupboard and she lets it go. The account says so, and not the opened house.
     */
    @GameTest(template="empty",batch="place_invite",timeoutTicks=9000)
    public static void invitingThePast(GameTestHelper h){
        var f=fixture(h);var p=f.player("place_invite",P);var b=f.base(P);var l=f.level(P);
        welcome(h,f,p)
            .thenExecute(()->{
                say(h,p,"It's good to be back.");f.source(p,P);
                var own=f.own(p,P);own.putInt("Chapter",PlaceForYou.OPEN);own.putString("Confront","open");own.putString("First","Mara");own.putString("Helper","Mara");
                own.putString("Activity","radio");own.putBoolean("ActivityDone",true);own.putBoolean("Acknowledged",true);own.putString("Promise","listen");
                LiteraryVignettes.save(f.data(),p.getUUID(),P,own);
                at(f,p,.5,0,-14.5,180,0);})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Knocked"),"somebody knocks once Kit is out of the gate"))
            .thenExecute(()->{
                h.assertTrue(click(f,p,PlaceForYouSite.FRONT_DOOR)&&Talk.waiting(p,"door"),"through the door: "+Talk.key(p));say(h,p,"Open the door.");
                h.assertTrue(PlaceForYouSite.doorOpen(l,b),"the door open");
                talk(f,p,"ren");h.assertTrue(Talk.waiting(p,"peers"),"Alex and Ren: "+Talk.key(p));say(h,p,"What happens if she lets him in?");say(h,p,"Stay with me a minute.");
                h.assertTrue(f.own(p,P).getBoolean("CostHeard"),"the cost said aloud before anything is decided");
                h.assertTrue(click(f,p,PlaceForYouSite.PHOTO)&&Talk.waiting(p,"photo")&&Talk.replies(p).equals(java.util.List.of("Leave it.")),"with the reader's picture still up, there is no taking the photograph: "+Talk.replies(p));
                say(h,p,"Leave it.");
                h.assertTrue(click(f,p,PlaceForYouSite.FRIDGE)&&f.own(p,P).getBoolean("PictureDown"),"the reader's picture down off the fridge");
                h.assertTrue(click(f,p,PlaceForYouSite.PHOTO)&&Talk.waiting(p,"photo"),"Theo's photograph: "+Talk.key(p));say(h,p,"Take it.");
                h.assertTrue(PlaceForYou.holding(p)==PlaceForYouRegistry.PHOTO&&!PlaceForYouBlock.is(l.getBlockState(b.offset(PlaceForYouSite.PHOTO)),PlaceForYouBlock.Kind.PHOTO),"carried off the chest of drawers");
                h.assertTrue(click(f,p,PlaceForYouSite.FRIDGE)&&f.own(p,P).getBoolean("TheoUp")&&PlaceForYouSite.pictureUp(l,b)&&PlaceForYou.holding(p)==0,"up on the fridge, where the reader's picture was");
                h.assertTrue(click(f,p,PlaceForYouSite.FRONT_DOOR)&&Talk.waiting(p,"door_open")&&Talk.replies(p).contains("Come in, Theo."),"the open door offers the invitation: "+Talk.replies(p));
                say(h,p,"Come in, Theo.");
                h.assertTrue(f.own(p,P).getInt("Chapter")==PlaceForYou.INVITE,"invited");
                // In the hall, facing into the house: neither end of the road in sight.
                at(f,p,.5,0,-16.5,180,0);})
            .thenWaitUntil(()->h.assertTrue(PlaceForYou.actor(l,b,"theo")!=null,"Theo, made at an end of the road nobody can see"))
            .thenExecute(()->{var theo=PlaceForYou.actor(l,b,"theo");h.assertTrue(Math.abs(theo.getX()-b.getX())>14,"out among the trees at the road's end: "+theo.position());})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Rewritten"),"he comes straight in"))
            .thenExecute(()->at(f,p,.5,0,-13.2,0,0))
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("AlexOut")&&f.own(p,P).getBoolean("RenOut"),"Alex and Ren were never in the old evening: gone while nobody looks"))
            .thenExecute(()->at(f,p,6.5,0,-14.5,90,10))
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("MaraStranger"),"Mara doesn't know the reader"))
            .thenExecute(()->{talk(f,p,"mara");h.assertTrue(Talk.waiting(p,"invite_mara"),"Mara: "+Talk.key(p));say(h,p,"It's me.");
                h.assertTrue(Talk.lines(p).contains("Mara: I'm sorry. I don't think we've met."),"she doesn't think they've met: "+Talk.lines(p));})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("CareHeard"),"his things are out in the bathroom"))
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("CupAway")&&f.own(p,P).getBoolean("WrongSeen"),"the cup, in the right cupboard"))
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Ready")&&f.own(p,P).getString("Outcome").equals("invited_the_past"),"the past let in"))
            .thenExecute(()->{h.assertFalse(PlaceForYouSite.readerChair(l,b),"no place at the table for the reader");at(f,p,5.5,0,-24.5,180,20);})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("CareSeen"),"the reader's things, laid out for him"))
            .thenExecute(()->f.read(p,P,LiteraryRooms.ending(P)))
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Completed"),"the account read"))
            .thenExecute(()->{var text=account(f,p);
                for(var said:java.util.List.of("put Theo's photograph up where it had been","straight in without knocking","Mara said, \"There you are.\"","Alex and Ren weren't in the house",
                        "She said she didn't think we'd met","his things were out in the bathroom","the one she had laid out for me","the right cupboard","Theo was in his place at the table"))
                    h.assertTrue(text.contains(said),"the account has \""+said+"\": "+text);
                h.assertFalse(text.contains("hall chair against the front door")||text.contains("I put it back up"),"and neither the opened house nor the kept evening: "+text);})
            .thenSucceed();
    }
    @AfterBatch(batch="place_invite") public static void inviteDone(ServerLevel l){close();}

    /**
     * 0.4.689, Mara's room (N6A): at the sink Mara asks the reader upstairs; her door opens; three folds, the work shirt taken from the
     * reader's hands; the top drawer out with the photograph in it; what she tells; a question back, kept for the account; the drawer
     * shut and supper laid again. A reader who would rather talk at the sink still can.
     */
    @GameTest(template="empty",batch="place_room",timeoutTicks=4000)
    public static void marasRoom(GameTestHelper h){
        var f=fixture(h);var p=f.player("place_room",P);var b=f.base(P);var l=f.level(P);int up=PlaceForYouSite.UP;
        welcome(h,f,p)
            .thenExecute(()->{
                say(h,p,"It's good to be back.");
                var own=f.own(p,P);own.putInt("Chapter",PlaceForYou.SINK);own.putString("First","Mara");own.putString("Helper","Mara");
                own.putString("Activity","radio");own.putBoolean("ActivityDone",true);own.putBoolean("Acknowledged",true);own.putInt("BagStage",3);
                LiteraryVignettes.save(f.data(),p.getUUID(),P,own);
                at(f,p,6.5,0,-19.5,180,0);})
            .thenWaitUntil(()->h.assertTrue(PlaceForYou.actor(l,b,"mara")!=null,"Mara at the sink"))
            .thenExecute(()->{
                h.assertTrue(click(f,p,PlaceForYouSite.SHUT_DOORS.get(0))&&!PlaceForYouSite.maraDoorOpen(l,b),"her door, shut until she asks: "+PlaceForYouSite.maraDoorOpen(l,b));
                talk(f,p,"mara");h.assertTrue(Talk.waiting(p,"room_ask"),"Mara asks the reader upstairs: "+Talk.key(p));say(h,p,"All right.");
                h.assertTrue(PlaceForYouSite.maraDoorOpen(l,b)&&f.own(p,P).getInt("RoomStage")==1,"her door open, upstairs");
                at(f,p,-4.5,up,-15.5,90,30);})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getInt("RoomStage")==2,"in her room, by the washing"))
            .thenExecute(()->{
                for(int i=0;i<3;i++)h.assertTrue(click(f,p,PlaceForYouSite.BASKET),"a fold");
                h.assertTrue(f.own(p,P).getInt("Folds")==3&&PlaceForYouBlock.stage(l.getBlockState(b.offset(PlaceForYouSite.BASKET)))==2,"three folds, the basket empty");})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getInt("RoomStage")==3&&PlaceForYouBlock.stage(l.getBlockState(b.offset(PlaceForYouSite.DRESSER)))==1,"the top drawer out, the photograph in it"))
            .thenWaitUntil(()->h.assertTrue(Talk.waiting(p,"room"),"what she tells, with the drawer open: "+Talk.key(p)))
            .thenExecute(()->{
                h.assertTrue(Talk.lines(p).contains("Mara: I don't want another conversation in an empty kitchen."),"her words: "+Talk.lines(p));
                h.assertTrue(Talk.replies(p).size()==5,"five things to say back: "+Talk.replies(p));
                say(h,p,"How is keeping them different");
                var own=f.own(p,P);
                h.assertTrue(own.getInt("Chapter")==PlaceForYou.DINNER&&own.getBoolean("RoomTold")&&own.getString("Promise").equals("different")
                        &&PlaceForYouBlock.stage(l.getBlockState(b.offset(PlaceForYouSite.DRESSER)))==0,"the drawer shut, what was said kept, supper laid again");
                var text=PlaceForYouTexts.afterwards(p.getGameProfile().getName(),own);
                for(var said:java.util.List.of("asked me upstairs to help her fold the washing","she took it from me and folded it herself","on the porch steps, the day before",
                        "how keeping them was different from losing them"))
                    h.assertTrue(text.contains(said),"the account has \""+said+"\": "+text);
                h.assertFalse(text.contains("At the sink"),"and not the sink's: "+text);
                at(f,p,5.5,0,-17.5,0,0);})
            .thenWaitUntil(()->h.assertFalse(PlaceForYouSite.maraDoorOpen(l,b),"her door shut again behind them, while nobody looks"))
            .thenSucceed();
    }
    @AfterBatch(batch="place_room") public static void roomDone(ServerLevel l){close();}

    /** Puts a reader at the sink chapter with the bag settled in the kitchen, as the back passage begins. */
    static void sinkReady(LiteraryTests.Fixture f,ServerPlayer p,BlockPos b,ServerLevel l){
        var own=f.own(p,P);own.putInt("Chapter",PlaceForYou.SINK);own.putString("First","Mara");own.putString("Helper","Mara");
        own.putString("Activity","radio");own.putBoolean("ActivityDone",true);own.putBoolean("Acknowledged",true);own.putInt("BagStage",3);own.putBoolean("BagSeen",true);
        LiteraryVignettes.save(f.data(),p.getUUID(),P,own);PlaceForYouSite.bag(l,b,"kitchen");
    }
    /**
     * 0.4.690, the back passage (N6B): Kit waits by the back door while Mara is at the sink and shows it to the reader, asking for quiet
     * until the bag is outside. The catch is out of reach until the cupboard has been shoved clear; under it lies the wedge; the drawn
     * catch lets the door swing back shut with nothing holding it; the wedge holds it; the back gate opens; Kit carries the bag out
     * through it and comes back; the quiet kept is in the account.
     */
    @GameTest(template="empty",batch="place_back",timeoutTicks=4000)
    public static void theBackPassage(GameTestHelper h){
        var f=fixture(h);var p=f.player("place_back",P);var b=f.base(P);var l=f.level(P);
        welcome(h,f,p)
            .thenExecute(()->{say(h,p,"It's good to be back.");sinkReady(f,p,b,l);at(f,p,-.5,0,-24.5,180,10);})
            .thenWaitUntil(()->h.assertTrue(PlaceForYou.actor(l,b,"kit")!=null,"Kit by the back door"))
            .thenExecute(()->{
                h.assertTrue(click(f,p,PlaceForYouSite.BACK_DOOR)&&!PlaceForYouSite.backDoorOpen(l,b),"the back door, its catch stiff");
                talk(f,p,"kit");h.assertTrue(Talk.waiting(p,"back_kit"),"Kit shows the back door: "+Talk.key(p));
                h.assertTrue(Talk.lines(p).contains("Kit: Will you keep it quiet? Only until the bag's outside."),"the quiet asked for: "+Talk.lines(p));
                h.assertTrue(Talk.replies(p).size()==3,"three answers: "+Talk.replies(p));
                say(h,p,"I'll keep it quiet.");
                var own=f.own(p,P);h.assertTrue(own.getInt("BackStage")==1&&own.getString("Quiet").equals("agreed"),"shown, and the quiet agreed");
                h.assertTrue(click(f,p,PlaceForYouSite.BACK_DOOR)&&!PlaceForYouSite.backDoorOpen(l,b),"the catch out of reach while the cupboard stands there");
                for(int i=0;i<3;i++)h.assertTrue(click(f,p,PlaceForYouSite.BACK_PRESS),"a shove");
                h.assertTrue(PlaceForYouBlock.is(l.getBlockState(b.offset(PlaceForYouSite.PRESS_MOVED)),PlaceForYouBlock.Kind.PRESS)
                        &&PlaceForYouBlock.is(l.getBlockState(b.offset(PlaceForYouSite.BACK_PRESS)),PlaceForYouBlock.Kind.WEDGE),"the cupboard along the wall, the wedge where it stood");
                h.assertTrue(click(f,p,PlaceForYouSite.BACK_DOOR)&&PlaceForYouSite.backDoorOpen(l,b),"the catch drawn, the door open");})
            .thenWaitUntil(()->h.assertFalse(PlaceForYouSite.backDoorOpen(l,b),"it swings back shut with nothing holding it"))
            .thenExecute(()->{
                h.assertTrue(f.own(p,P).getBoolean("Swung")&&!f.own(p,P).getBoolean("CatchDrawn"),"and the catch drops in again");
                h.assertTrue(click(f,p,PlaceForYouSite.BACK_PRESS)&&PlaceForYou.holding(p)==PlaceForYouRegistry.WEDGE,"the wedge picked up");
                h.assertTrue(click(f,p,PlaceForYouSite.BACK_DOOR)&&PlaceForYouSite.backDoorOpen(l,b),"the catch drawn again, the wedge in the other hand");
                h.assertTrue(click(f,p,PlaceForYouSite.BACK_DOOR)&&f.own(p,P).getString("BackHeld").equals("wedge")&&PlaceForYou.holding(p)==0
                        &&PlaceForYouBlock.is(l.getBlockState(b.offset(PlaceForYouSite.HOLD)),PlaceForYouBlock.Kind.WEDGE),"the wedge in under it");
                at(f,p,.5,0,-28.5,180,20);
                h.assertTrue(click(f,p,PlaceForYouSite.BACK_GATE)&&PlaceForYouSite.backGateOpen(l,b)&&f.own(p,P).getString("BackWay").equals("door"),"the back gate tried, and open");
                at(f,p,6.5,0,-20.5,180,0);})
            .thenWaitUntil(()->h.assertTrue(PlaceForYouSite.backDoorOpen(l,b)&&f.own(p,P).getInt("BackStage")==3,"the door stays held, and Kit carries the bag out and back: "+f.own(p,P).getInt("BagLeg")))
            .thenExecute(()->{
                var own=f.own(p,P);
                h.assertTrue(PlaceForYouBlock.is(l.getBlockState(b.offset(PlaceForYouSite.BAG_BACK)),PlaceForYouBlock.Kind.BAG)&&own.getString("BagWhere").equals("back")
                        &&!PlaceForYouBlock.is(l.getBlockState(b.offset(PlaceForYouSite.BAG_KITCHEN)),PlaceForYouBlock.Kind.BAG),"the bag outside, beyond the gate, and not in the kitchen");
                h.assertTrue(own.getBoolean("QuietKept"),"the quiet kept");
                h.assertTrue(PlaceForYouBack.backWay(own),"when Kit goes, it will be by the back");
                var text=PlaceForYouTexts.afterwards(p.getGameProfile().getName(),own);
                for(var said:java.util.List.of("Kit showed me the back door","I said I would.","Under where it stood was a wedge",
                        "swung back shut on its own, so the next time I pushed the wedge in under it","onto a path between the trees","Kit carried the bag out to the path","Kit thanked me for keeping it"))
                    h.assertTrue(text.contains(said),"the account has \""+said+"\": "+text);})
            .thenSucceed();
    }
    @AfterBatch(batch="place_back") public static void backDone(ServerLevel l){close();}

    /**
     * 0.4.691, the older pictures (N6C): at the sink, the one the reader actually met asks them up (here Alex: nobody under the eaves, and
     * the reader talked to Mara first). The reader's picture is copied onto the landing wall among the household's older photographs.
     * On the landing Alex shows them all, tells the claimed lake and then what happened (supper, tonight), and takes one of five answers.
     * Taken down, the picture is carried and Alex loses the sentence and starts another; hung back, the claim comes back; down again, it
     * is gone again; laid in the back room's box, it stays gone. The account says so.
     */
    @GameTest(template="empty",batch="place_older",timeoutTicks=4000)
    public static void theOlderPictures(GameTestHelper h){
        var f=fixture(h);var p=f.player("place_older",P);var b=f.base(P);var l=f.level(P);
        welcome(h,f,p)
            .thenExecute(()->{say(h,p,"It's good to be back.");sinkReady(f,p,b,l);var own=f.own(p,P);own.putBoolean("RoomDeclined",true);LiteraryVignettes.save(f.data(),p.getUUID(),P,own);at(f,p,-4.5,0,-18.5,180,0);})
            .thenWaitUntil(()->h.assertTrue(PlaceForYou.actor(l,b,"alex")!=null,"Alex in the house"))
            .thenExecute(()->{
                h.assertFalse(l.getBlockState(b.offset(PlaceForYouSite.PORTRAIT)).is(PlaceForYouRegistry.PORTRAIT_BLOCK.get()),"no picture of the reader on the landing yet");
                for(var at:java.util.List.of(PlaceForYouSite.OLDER_A,PlaceForYouSite.OLDER_B,PlaceForYouSite.OLDER_C))
                    h.assertTrue(PlaceForYouBlock.is(l.getBlockState(b.offset(at)),PlaceForYouBlock.Kind.OLDER),"an older picture on the landing at "+at);
                talk(f,p,"alex");h.assertTrue(Talk.waiting(p,"older_ask"),"Alex asks the reader up: "+Talk.key(p));
                say(h,p,"Show me.");
                var own=f.own(p,P);
                h.assertTrue(own.getInt("OlderStage")==1&&own.getString("OlderGuide").equals("Alex")&&PlaceForYouSite.portraitUp(l,b),"asked up, and the reader's picture on the wall: "+own.getInt("OlderStage")+" "+own.getString("OlderGuide"));
                at(f,p,-1.5,PlaceForYouSite.UP,-21.5,0,0);})
            .thenWaitUntil(()->h.assertTrue(Talk.waiting(p,"older"),"the pictures shown on the landing: "+Talk.key(p)+" "+f.own(p,P).getInt("OlderStage")))
            .thenExecute(()->{
                h.assertTrue(Talk.lines(p).contains("Alex: And that's us. At the lake, the summer we went every weekend. You had the end of the dock and wouldn't give it up."),"the claim: "+Talk.lines(p));
                h.assertTrue(Talk.lines(p).contains("Alex: That isn't right. I met you tonight, at the table. That's the part that happened.")
                        &&Talk.lines(p).contains("Alex: I liked sitting across from you. That part happened. I don't know where she got the rest."),"what happened, and the rest: "+Talk.lines(p));
                h.assertTrue(Talk.replies(p).size()==5,"five answers: "+Talk.replies(p));
                say(h,p,"What do you want tomorrow?");
                var own=f.own(p,P);h.assertTrue(own.getInt("OlderStage")==3&&own.getString("OlderSaid").equals("tomorrow"),"answered");
                h.assertTrue(click(f,p,PlaceForYouSite.PORTRAIT)&&PlaceForYou.holding(p)==PlaceForYouRegistry.PORTRAIT&&!PlaceForYouSite.portraitUp(l,b),"the picture taken down");
                h.assertTrue(Talk.waiting(p,"older_falter")&&Talk.lines(p).contains("Alex: You always had the end of the dock, with your feet in the—")
                        &&Talk.lines(p).contains("Alex: I don't know where you like to sit. I'd like to find out."),"the sentence lost, a new one begun: "+Talk.lines(p));
                say(h,p,"Go on.");
                h.assertTrue(PlaceForYouSite.storeDoorOpen(l,b),"the back room open");
                h.assertTrue(click(f,p,PlaceForYouSite.PORTRAIT)&&PlaceForYouSite.portraitUp(l,b)&&PlaceForYou.holding(p)==0&&f.own(p,P).getString("Portrait").equals("rehung"),"hung back on its nail");
                h.assertTrue(click(f,p,PlaceForYouSite.PORTRAIT)&&!PlaceForYouSite.portraitUp(l,b)&&Talk.lines(p).contains("Alex: No. Gone again."),"down again, and the claim gone again: "+Talk.lines(p));
                say(h,p,"Go on.");
                at(f,p,-5.5,PlaceForYouSite.UP,-23.5,180,30);
                h.assertTrue(click(f,p,PlaceForYouSite.PHOTO_BOX)&&PlaceForYouBlock.stage(l.getBlockState(b.offset(PlaceForYouSite.PHOTO_BOX)))==1&&PlaceForYou.holding(p)==0,"laid in the box");
                var done=f.own(p,P);h.assertTrue(done.getInt("OlderStage")==5&&done.getString("Portrait").equals("boxed"),"boxed, for good");
                var text=PlaceForYouTexts.afterwards(p.getGameProfile().getName(),done);
                for(var said:java.util.List.of("Alex took me up to the pictures on the landing","said it was the two of us at the lake","What happened was supper, that night.",
                        "To wake up somewhere he hadn't fallen asleep.","with the ones nobody hangs"))
                    h.assertTrue(text.contains(said),"the account has \""+said+"\": "+text);})
            .thenSucceed();
    }
    @AfterBatch(batch="place_older") public static void olderDone(ServerLevel l){close();}

    /**
     * 0.4.692, the letter: the quiet agreed and then broken (the key, asked for and explained before the bag was out). Once the bag is
     * outside, Kit, back by the back door, says the letter is gone from the clasp; the reader says they will get it back; Mara's door
     * is open; her top drawer gives up Kit's letter; her door shuts again once the reader is out of the room; the letter back in Kit's
     * hand; the account says so.
     */
    @GameTest(template="empty",batch="place_letter",timeoutTicks=5000)
    public static void theLetterTakenUpstairs(GameTestHelper h){
        var f=fixture(h);var p=f.player("place_letter",P);var b=f.base(P);var l=f.level(P);int up=PlaceForYouSite.UP;
        welcome(h,f,p)
            .thenExecute(()->{say(h,p,"It's good to be back.");sinkReady(f,p,b,l);at(f,p,-.5,0,-24.5,180,10);})
            .thenWaitUntil(()->h.assertTrue(PlaceForYou.actor(l,b,"kit")!=null&&PlaceForYou.actor(l,b,"mara")!=null,"Kit by the back door, Mara at the sink"))
            .thenExecute(()->{
                talk(f,p,"kit");say(h,p,"I'll keep it quiet.");
                talk(f,p,"mara");say(h,p,"Is there a key");say(h,p,"Kit wants to go out that way.");
                h.assertTrue(f.own(p,P).getBoolean("BrokeQuiet"),"the quiet broken");
                h.assertTrue(click(f,p,PlaceForYouSite.SINK)&&click(f,p,PlaceForYouSite.BACK_DOOR),"the key, and the door");
                at(f,p,.5,0,-28.5,180,20);h.assertTrue(click(f,p,PlaceForYouSite.BACK_GATE),"the gate");
                at(f,p,6.5,0,-20.5,180,0);})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getInt("BackStage")==3,"the bag out: "+f.own(p,P).getInt("BagLeg")))
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getInt("LetterStage")==1,"the letter gone from the clasp"))
            .thenExecute(()->{h.assertTrue(PlaceForYouLetter.holding(f.own(p,P)),"supper waits");at(f,p,-.5,0,-24.5,180,10);})
            .thenWaitUntil(()->h.assertTrue(Talk.waiting(p,"letter"),"Kit says so, once the reader is by the back door: "+Talk.key(p)))
            .thenExecute(()->{
                h.assertTrue(Talk.lines(p).contains("Kit: The letter's gone. I wrapped it round the clasp, and it isn't there."),"Kit's words: "+Talk.lines(p));
                h.assertTrue(Talk.replies(p).size()==3,"three answers: "+Talk.replies(p));
                say(h,p,"I'll get it back.");
                h.assertTrue(f.own(p,P).getInt("LetterStage")==2&&PlaceForYouSite.maraDoorOpen(l,b),"her door open");
                h.assertTrue(click(f,p,PlaceForYouSite.SHUT_DOORS.get(0))&&PlaceForYouSite.maraDoorOpen(l,b),"her door, left open");
                at(f,p,-5.5,up,-18.5,180,30);
                h.assertTrue(click(f,p,PlaceForYouSite.DRESSER)&&PlaceForYou.holding(p)==PlaceForYouRegistry.LETTER&&f.own(p,P).getInt("LetterStage")==3,"Kit's letter from her top drawer");
                h.assertTrue(f.own(p,P).getInt("Chapter")==PlaceForYou.SINK,"still before supper");
                at(f,p,6.5,0,-20.5,180,0);})
            .thenWaitUntil(()->h.assertFalse(PlaceForYouSite.maraDoorOpen(l,b),"her door shut again, the reader downstairs"))
            .thenExecute(()->{
                at(f,p,-.5,0,-24.5,180,10);talk(f,p,"kit");
                h.assertTrue(Talk.waiting(p,"letter_back")&&PlaceForYou.holding(p)==0,"the letter in Kit's hand: "+Talk.key(p));
                say(h,p,"Go on.");
                var own=f.own(p,P);h.assertTrue(own.getInt("LetterStage")==4&&own.getString("KitTrust").equals("returned")&&!PlaceForYouLetter.holding(own),"settled, and supper free to come");
                var text=PlaceForYouTexts.afterwards(p.getGameProfile().getName(),own);
                h.assertTrue(text.contains("Mara had put it in her top drawer. I went up and got it back, and gave it to Kit."),"the account: "+text);})
            .thenSucceed();
    }
    @AfterBatch(batch="place_letter") public static void letterDone(ServerLevel l){close();}


    /**
     * 0.4.692, the porch: told openly, with Kit there, and nothing else done with Mara. When Kit goes, Mara has the bag and carries it out
     * onto the porch; Kit waits at the open gate. She says she can't go past the porch; the reader carries the bag down to Kit; Kit goes
     * by the front with it. The account says so, and the other two of her ways are written as they should be.
     */
    @GameTest(template="empty",batch="place_porch",timeoutTicks=5000)
    public static void thePorchWithTheBag(GameTestHelper h){
        var f=fixture(h);var p=f.player("place_porch",P);var b=f.base(P);var l=f.level(P);
        welcome(h,f,p)
            .thenExecute(()->{say(h,p,"It's good to be back.");sinkReady(f,p,b,l);at(f,p,-.5,0,-24.5,180,10);})
            .thenWaitUntil(()->h.assertTrue(PlaceForYou.actor(l,b,"kit")!=null&&PlaceForYou.actor(l,b,"mara")!=null,"Kit and Mara in the house"))
            .thenExecute(()->{
                talk(f,p,"kit");say(h,p,"I'd rather tell Mara");
                talk(f,p,"mara");say(h,p,"Kit's found a way out the back.");
                h.assertTrue(f.own(p,P).getBoolean("ToldOpen"),"told openly");
                // On to the house opened, after the open confrontation, as the evening stands then.
                var own=f.own(p,P);own.putInt("Chapter",PlaceForYou.OPEN);own.putString("Confront","open");own.putBoolean("TheoTold",true);own.putString("Promise","listen");
                LiteraryVignettes.save(f.data(),p.getUUID(),P,own);
                at(f,p,.5,0,-14.5,180,0);})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getInt("PorchStage")==1&&PlaceForYouSite.doorOpen(l,b),"Mara takes the bag out; the front door open"))
            .thenExecute(()->{
                h.assertFalse(f.own(p,P).getBoolean("KitGoing"),"Kit not gone yet");
                var mara=PlaceForYou.actor(l,b,"mara");h.assertTrue(mara!=null&&!mara.getMainHandItem().isEmpty(),"the bag in Mara's hands");
                // In the yard, facing the front of the house, so both walk out where the reader can see them.
                at(f,p,2.5,0,-6.5,180,0);})
            .thenWaitUntil(()->{var m=PlaceForYou.actor(l,b,"mara");var k=PlaceForYou.actor(l,b,"kit");var o=net.minecraft.world.phys.Vec3.atLowerCornerOf(b);
                h.assertTrue(Talk.waiting(p,"porch"),"on the porch: "+Talk.key(p)+" "+f.own(p,P).getInt("PorchStage")+" Mara "+(m==null?"none":m.position().subtract(o))+" Kit "+(k==null?"none":k.position().subtract(o)));})
            .thenExecute(()->{
                var gateKit=PlaceForYou.actor(l,b,"kit");
                h.assertTrue(gateKit!=null&&gateKit.position().distanceToSqr(net.minecraft.world.phys.Vec3.atBottomCenterOf(b.offset(0,0,-6)))<4,"Kit at the open gate: "+(gateKit==null?"none":gateKit.position().subtract(net.minecraft.world.phys.Vec3.atLowerCornerOf(b))));
                h.assertTrue(Talk.lines(p).contains("Mara: I can't go past the porch. I've tried."),"Mara at the threshold: "+Talk.lines(p));
                h.assertTrue(Talk.replies(p).size()==2,"two answers: "+Talk.replies(p));
                say(h,p,"Carry the bag down to Kit.");
                var own=f.own(p,P);h.assertTrue(own.getInt("PorchStage")==2&&own.getString("PorchBag").equals("carried")&&own.getBoolean("KitGoing"),"carried, and Kit going");
                var kit=PlaceForYou.actor(l,b,"kit");var mara=PlaceForYou.actor(l,b,"mara");
                h.assertTrue(kit!=null&&!kit.getMainHandItem().isEmpty()&&mara!=null&&mara.getMainHandItem().isEmpty(),"the bag with Kit, not Mara");
                h.assertFalse(PlaceForYouBack.backWay(own)&&!own.contains("PorchBag"),"Kit goes by the front");
                own.putBoolean("Propped",true);own.putBoolean("PictureDown",true);
                var text=PlaceForYouTexts.afterwards(p.getGameProfile().getName(),own);
                h.assertTrue(text.contains("Mara carried the bag out onto the porch, and Kit waited at the open gate. She said she couldn't go past the porch. I carried the bag down the path to Kit."),"the account: "+text);
                h.assertFalse(text.contains("Kit went out through the gate with the bag."),"in place of the plain sentence: "+text);
                var keep=new net.minecraft.nbt.CompoundTag();keep.putString("BagPorch","keep");keep.putString("PorchBag","left");
                h.assertTrue(PlaceForYouLetter.porchAccount(keep).endsWith("Kit said no, and went without the bag."),"kept back: "+PlaceForYouLetter.porchAccount(keep));
                var aside=new net.minecraft.nbt.CompoundTag();aside.putString("BagPorch","aside");aside.putString("PorchBag","kit");
                h.assertTrue(PlaceForYouLetter.porchAccount(aside).endsWith("She went down the path and put it in Kit's hands."),"handed over: "+PlaceForYouLetter.porchAccount(aside));})
            .thenSucceed();
    }
    @AfterBatch(batch="place_porch") public static void porchDone(ServerLevel l){close();}

    /**
     * 0.4.690: the quiet agreed, then broken: asked for the key, the reader tells Mara what it is for before the bag is out. The key is
     * in the drawer with the tea towels; it opens the door with the cupboard still standing there, and the door stays open; the gate;
     * the bag out; Kit answers what was done, and the account says so.
     */
    @GameTest(template="empty",batch="place_back_key",timeoutTicks=4000)
    public static void theBackDoorKey(GameTestHelper h){
        var f=fixture(h);var p=f.player("place_back_key",P);var b=f.base(P);var l=f.level(P);
        welcome(h,f,p)
            .thenExecute(()->{say(h,p,"It's good to be back.");sinkReady(f,p,b,l);at(f,p,-.5,0,-24.5,180,10);})
            .thenWaitUntil(()->h.assertTrue(PlaceForYou.actor(l,b,"kit")!=null&&PlaceForYou.actor(l,b,"mara")!=null,"Kit by the back door, Mara at the sink"))
            .thenExecute(()->{
                talk(f,p,"kit");say(h,p,"I'll keep it quiet.");
                talk(f,p,"mara");h.assertTrue(Talk.waiting(p,"back_mara"),"Mara asked about the back door, before the sheets: "+Talk.key(p));
                say(h,p,"Is there a key");say(h,p,"Kit wants to go out that way.");
                var own=f.own(p,P);
                h.assertTrue(own.getBoolean("KeyOffered")&&own.getBoolean("ToldMara")&&own.getBoolean("BrokeQuiet"),"told, before the bag was out, and the key offered");
                h.assertTrue(click(f,p,PlaceForYouSite.SINK)&&PlaceForYou.holding(p)==PlaceForYouRegistry.KEY,"the key from the drawer with the tea towels");
                h.assertTrue(click(f,p,PlaceForYouSite.BACK_DOOR)&&PlaceForYouSite.backDoorOpen(l,b)&&f.own(p,P).getBoolean("KeyUsed"),"the key turns, the cupboard still where it was");
                h.assertTrue(f.own(p,P).getInt("Shoves")==0,"nothing shoved");})
            .thenIdle(PlaceForYouBack.SWING+20)
            .thenExecute(()->{
                h.assertTrue(PlaceForYouSite.backDoorOpen(l,b),"unlocked, it stays open");
                at(f,p,.5,0,-28.5,180,20);
                h.assertTrue(click(f,p,PlaceForYouSite.BACK_GATE)&&f.own(p,P).getString("BackWay").equals("key"),"the gate, opened with the key");
                at(f,p,6.5,0,-20.5,180,0);})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getInt("BackStage")==3,"the bag out: "+f.own(p,P).getInt("BagLeg")))
            .thenExecute(()->{
                var own=f.own(p,P);h.assertFalse(own.getBoolean("QuietKept"),"the quiet was not kept");
                h.assertTrue(PlaceForYouBack.back(own).contains("You said you'd wait"),"Kit says so: "+PlaceForYouBack.back(own));
                var text=PlaceForYouTexts.afterwards(p.getGameProfile().getName(),own);
                for(var said:java.util.List.of("Mara told me the key was in the drawer with the tea towels, with his","I told Mara before the bag was out.","Kit said I had said I would wait."))
                    h.assertTrue(text.contains(said),"the account has \""+said+"\": "+text);
                h.assertFalse(text.contains("I shoved the cupboard"),"and not the cupboard's: "+text);})
            .thenSucceed();
    }
    @AfterBatch(batch="place_back_key") public static void backKeyDone(ServerLevel l){close();}

    /**
     * 0.4.690: told openly. The reader would rather tell Mara with Kit there; Kit goes and stands by the sink; told, Mara says she put the
     * cupboard there herself and gives the key. And Alex's window: asked, Alex goes to the sitting room and lifts it only on getting
     * there; Kit hands the bag out to Alex; the window goes back.
     */
    @GameTest(template="empty",batch="place_back_window",timeoutTicks=5000)
    public static void theSideWindowAndTheOpenTelling(GameTestHelper h){
        var f=fixture(h);var p=f.player("place_back_window",P);var b=f.base(P);var l=f.level(P);
        welcome(h,f,p)
            .thenExecute(()->{say(h,p,"It's good to be back.");sinkReady(f,p,b,l);at(f,p,-.5,0,-24.5,180,10);})
            .thenWaitUntil(()->h.assertTrue(PlaceForYou.actor(l,b,"kit")!=null&&PlaceForYou.actor(l,b,"alex")!=null,"Kit and Alex in the house"))
            .thenExecute(()->{
                talk(f,p,"kit");say(h,p,"I'd rather tell Mara");
                talk(f,p,"mara");h.assertTrue(Talk.waiting(p,"back_told"),"told with Kit there: "+Talk.key(p));
                say(h,p,"Kit's found a way out the back.");
                var own=f.own(p,P);h.assertTrue(own.getBoolean("ToldOpen")&&own.getBoolean("KeyOffered")&&!own.getBoolean("BrokeQuiet"),"told openly, the key offered, no promise broken");
                h.assertTrue(String.join(" ",Talk.lines(p)).contains("I put the cupboard there"),"what she says: "+Talk.lines(p));
                talk(f,p,"alex");h.assertTrue(Talk.waiting(p,"back_alex"),"Alex offers the window: "+Talk.key(p));
                say(h,p,"Hold it for Kit.");
                h.assertTrue(!PlaceForYouSite.sideWindowOpen(l,b)&&f.own(p,P).getBoolean("AlexWindow"),"not open until Alex gets there");
                at(f,p,6.5,0,-26.5,0,0);})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getString("BackWay").equals("window")&&PlaceForYouSite.sideWindowOpen(l,b),"Alex at the window, lifting it"))
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getInt("BackStage")==3,"the bag handed out: "+f.own(p,P).getInt("BagLeg")))
            .thenExecute(()->{
                var own=f.own(p,P);
                h.assertTrue(PlaceForYouBlock.is(l.getBlockState(b.offset(PlaceForYouSite.BAG_SIDE)),PlaceForYouBlock.Kind.BAG)&&!PlaceForYouSite.sideWindowOpen(l,b),"the bag in the side yard, the window shut again");
                h.assertFalse(PlaceForYouBack.backWay(own),"Kit will go by the front");
                var text=PlaceForYouTexts.afterwards(p.getGameProfile().getName(),own);
                for(var said:java.util.List.of("I said I'd rather tell Mara, with Kit there.","We told her together.","Alex lifted the sitting room window and held it","Then Kit came back in for supper."))
                    h.assertTrue(text.contains(said),"the account has \""+said+"\": "+text);})
            .thenSucceed();
    }
    @AfterBatch(batch="place_back_window") public static void backWindowDone(ServerLevel l){close();}

    /** 0.4.690: refused. Kit does it alone, while nobody is looking at the back of the hall. */
    @GameTest(template="empty",batch="place_back_alone",timeoutTicks=3000)
    public static void kitOpensTheBackAlone(GameTestHelper h){
        var f=fixture(h);var p=f.player("place_back_alone",P);var b=f.base(P);var l=f.level(P);
        welcome(h,f,p)
            .thenExecute(()->{say(h,p,"It's good to be back.");sinkReady(f,p,b,l);at(f,p,-.5,0,-24.5,180,10);})
            .thenWaitUntil(()->h.assertTrue(PlaceForYou.actor(l,b,"kit")!=null,"Kit by the back door"))
            .thenExecute(()->{talk(f,p,"kit");say(h,p,"I won't help with this.");at(f,p,-.5,0,-24.5,180,10);})
            .thenIdle(PlaceForYouBack.ALONE+40)
            .thenExecute(()->h.assertTrue(f.own(p,P).getString("BackWay").isEmpty(),"not while the reader watches the back door"))
            .thenExecute(()->at(f,p,6.5,0,-19.5,0,0))
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getString("BackWay").equals("kit"),"done alone, unseen"))
            .thenExecute(()->{
                var own=f.own(p,P);
                h.assertTrue(PlaceForYouSite.backDoorOpen(l,b)&&PlaceForYouSite.backGateOpen(l,b)&&PlaceForYouBlock.is(l.getBlockState(b.offset(PlaceForYouSite.HOLD)),PlaceForYouBlock.Kind.WEDGE)
                        &&PlaceForYouBlock.is(l.getBlockState(b.offset(PlaceForYouSite.BAG_BACK)),PlaceForYouBlock.Kind.BAG),"the door wedged, the gate open, the bag out");
                var text=PlaceForYouTexts.afterwards(p.getGameProfile().getName(),own);
                for(var said:java.util.List.of("I said I wouldn't help.","Kit had done it alone.","The bag was already outside."))
                    h.assertTrue(text.contains(said),"the account has \""+said+"\": "+text);})
            .thenSucceed();
    }
    @AfterBatch(batch="place_back_alone") public static void backAloneDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="place_road",timeoutTicks=7000)
    public static void aReaderTheHouseKnowsTakesTheRoad(GameTestHelper h){
        var f=fixture(h);var p=f.player("place_family",P);var b=f.base(P);var l=f.level(P);
        // What the quiet rooms would have seen of this reader: a routine item used, the glass turned, the eaves shared, the porch, the drawing hung.
        HouseRead.signal(p,HouseRead.Trait.AGE,"ANALOG",false);HouseRead.signal(p,HouseRead.Trait.AGE,"ANALOG",true);
        HouseRead.signal(p,HouseRead.Trait.HOME,"HAVEN",false);HouseRead.signal(p,HouseRead.Trait.HOME,"HAVEN",true);
        HouseRead.see(p,HouseReadSites.Room.BATHROOM,t->t.putInt("Used",3));HouseRead.see(p,HouseReadSites.Room.COATROOM,t->t.putString("Mirror","turned"));
        HouseRead.see(p,HouseReadSites.Room.SHELTER,t->t.putString("Stood","middle"));
        HouseRead.see(p,HouseReadSites.Room.KITCHEN,t->{t.putBoolean("Hung",true);t.putInt("Face",0xC08A6A);t.putInt("Hands",0xC08A6A);t.putInt("Hair",-1);t.putInt("Clothes",0x3A5A8A);t.putInt("Body",1);t.putInt("Mood",2);});
        var own0=f.own(p,P);own0.remove("Snap");PlaceForYou.arrive(p,b,own0);LiteraryVignettes.save(f.data(),p.getUUID(),P,own0);
        welcome(h,f,p)
            .thenExecute(()->{
                var snap=f.own(p,P).getCompound("Snap");
                h.assertTrue(snap.getBoolean("Read")&&snap.getInt("Routine")==3&&snap.getString("Glass").equals("turned")&&snap.getString("Home").equals("HAVEN")&&snap.getString("Age").equals("ANALOG")&&snap.getCompound("Drawing").getInt("Clothes")==0x3A5A8A,"the House's memory of this reader: "+snap);
                var glass=l.getBlockState(b.offset(PlaceForYouSite.GLASS));h.assertTrue(PlaceForYouBlock.is(glass,PlaceForYouBlock.Kind.GLASS)&&glass.getValue(PlaceForYouBlock.STAGE)==1,"the hall glass turned to the wallpaper, as the reader left the coatroom's");
                h.assertTrue(Talk.lines(p).stream().anyMatch(s->s.contains("I put your "+PlaceForYouTexts.routine(3)+" out in the bathroom"))&&Talk.lines(p).contains("Mara: Sit down, sit. We'll do all that. You don't have to lift a thing."),"the one remembered thing, and what she offers: "+Talk.lines(p));
                say(h,p,"It's good to be back.");h.assertTrue(f.own(p,P).getString("Claim").equals("family")&&f.own(p,P).getString("Detail").equals("routine"),"family, and the routine remembered");
                f.source(p,P);
                talk(f,p,"ren");h.assertTrue(Talk.lines(p).stream().anyMatch(s->s.contains("under the eaves")),"Ren remembers the eaves: "+Talk.lines(p));
                say(h,p,"Too much how?");say(h,p,"Of course.");h.assertTrue(f.own(p,P).getString("Activity").equals("tea"),"making the tea with Ren");
                h.assertTrue(click(f,p,PlaceForYouSite.CUPS)&&!f.own(p,P).getBoolean("ActivityDone"),"the kettle first");
                h.assertTrue(click(f,p,PlaceForYouSite.KETTLE)&&click(f,p,PlaceForYouSite.CUPS)&&f.own(p,P).getBoolean("ActivityDone"),"the kettle, then the cups");})
            .thenWaitUntil(()->h.assertTrue(chapter(f,p)==PlaceForYou.BATHROOM&&PlaceForYouSite.routineOut(l,b)&&PlaceForYouBlock.stage(l.getBlockState(b.offset(PlaceForYouSite.ROUTINE)))==3,"the reader's own routine item laid out"))
            .thenExecute(()->at(f,p,6.5,0,-25.5,0,0))
            .thenWaitUntil(()->h.assertTrue(Talk.waiting(p,"care"),"Ren asks how the reader wants it"))
            .thenExecute(()->{say(h,p,"Would you help me?");h.assertTrue(click(f,p,PlaceForYouSite.ROUTINE)&&f.own(p,P).getString("Care").equals("help"),"done together");})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Repeated"),"laid out again"))
            .thenExecute(()->at(f,p,-1.5,0,-22.5,180,0))
            .thenWaitUntil(()->h.assertTrue(chapter(f,p)==PlaceForYou.BAG,"the bag at the gate"))
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getInt("BagStage")==1,"in time, the bag is seen without looking for it"))
            .thenExecute(()->at(f,p,6.5,0,-18.5,180,0))
            .thenWaitUntil(()->h.assertTrue(Talk.waiting(p,"bag"),"the bag brought back in, and Mara and Kit about it"))
            .thenExecute(()->{say(h,p,"Could this wait until morning?");var own=f.own(p,P);h.assertTrue(own.getBoolean("Delayed")&&own.getBoolean("Acknowledged")&&!own.getBoolean("DoorTaken"),"until morning; Kit's door still a door");
                at(f,p,-.5,0,-24.5,63,27);})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("DoorTaken")&&PlaceForYouSite.cupboard(l,b)&&f.own(p,P).getBoolean("ChairTaken")&&l.getBlockState(b.offset(PlaceForYouSite.KIT_CHAIR)).isAir(),"looked at, Kit's door is a cupboard door, and Kit's chair is gone: looking "+PlaceForYou.looking(p,b,PlaceForYouSite.KIT_DOOR,10)+" at "+p.position().subtract(b.getX(),b.getY(),b.getZ())+" yaw "+p.getYRot()+" pitch "+p.getXRot()+" eye "+p.getEyePosition().subtract(b.getX(),b.getY(),b.getZ())+" view "+p.getViewVector(1)+" clip "+l.clip(new net.minecraft.world.level.ClipContext(p.getEyePosition(),net.minecraft.world.phys.Vec3.atCenterOf(b.offset(PlaceForYouSite.KIT_DOOR)),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,p)).getBlockPos().subtract(b)+" door "+l.getBlockState(b.offset(PlaceForYouSite.KIT_DOOR))+" chapter "+chapter(f,p)))
            .thenExecute(()->{sinkTalk(f,h,p);say(h,p,"What does Kit want?");h.assertTrue(f.own(p,P).getString("Promise").equals("kit")&&chapter(f,p)==PlaceForYou.DINNER,"what Kit wants; dinner again");
                h.assertTrue(click(f,p,PlaceForYouSite.KIT_DOOR)&&f.own(p,P).getBoolean("CupboardOpened"),"Kit's cupboard opened, and Kit behind the reader");
                talk(f,p,"mara");say(h,p,"Take the spoon.");talk(f,p,"mara");h.assertTrue(Talk.waiting(p,"confront"),"at the table");
                say(h,p,"This is more than I can take on.");h.assertTrue(chapter(f,p)==PlaceForYou.ROAD&&f.own(p,P).getString("Confront").equals("road"),"the road");
                at(f,p,.5,0,-2.5,180,0);})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Ready")&&f.own(p,P).getString("Outcome").equals("took_the_road"),"through the gate onto the road"))
            .thenExecute(()->f.read(p,P,LiteraryRooms.ending(P)))
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Completed"),"the account read"))
            .thenExecute(()->{var text=account(f,p);
                h.assertTrue(text.contains("good to have me back")&&text.contains("out in the bathroom, where my hand had gone")&&text.contains("Ren and I made tea")&&text.contains("asked Ren to help me")&&text.contains("wait until morning")&&text.contains("Kit's door become a cupboard door")&&text.contains("I opened Kit's cupboard")&&text.contains("more than I could take on")&&text.contains("exactly as I left it"),"the road account: "+text);
                h.assertTrue(!text.contains("hall chair"),"no door was propped");})
            .thenSucceed();
    }
    @AfterBatch(batch="place_road") public static void roadDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="place_off",timeoutTicks=2000)
    public static void withInferenceOffNothingIsRemembered(GameTestHelper h){
        var f=fixture(h);ServerPlayer p;
        HouseConfig.READ_READERS.set(false);try{p=f.player("place_unread",P);}finally{HouseConfig.READ_READERS.set(true);}
        var reader=p;var b=f.base(P);var l=f.level(P);
        welcome(h,f,reader)
            .thenExecute(()->{var snap=f.own(reader,P).getCompound("Snap");
                h.assertTrue(!snap.getBoolean("Read")&&snap.getInt("Routine")==-1&&!snap.contains("Drawing")&&PlaceForYouTexts.detail(snap).isEmpty(),"with inference off, the House kept nothing");
                h.assertTrue(Talk.lines(reader).contains("Mara: Come in, it's cold. The kettle's on.")&&PlaceForYouSite.pictureUp(l,b),"no remembered thing; a pencil figure with no face on the fridge");
                int stale=Talk.token(reader);say(h,reader,"Thank you. I'll stay for supper.");h.assertTrue(f.own(reader,P).getString("Claim").equals("hosp"),"staying for supper, nothing claimed");
                Talk.reply(reader,stale,1);h.assertTrue(f.own(reader,P).getString("Claim").equals("hosp")&&f.own(reader,P).getInt("Chapter")==PlaceForYou.SUPPER,"a reply to an exchange already answered changes nothing");
                talk(f,reader,"kit");h.assertTrue(Talk.waiting(reader,"first_Kit"),"Kit, the first time");var asked=Talk.replies(reader);
                talk(f,reader,"kit");h.assertTrue(Talk.waiting(reader,"first_Kit")&&Talk.replies(reader).equals(asked),"stepping away and talking again picks the same exchange up where it was");
                Talk.close(reader);talk(f,reader,"kit");h.assertTrue(Talk.waiting(reader,"offer_Kit"),"after it is gone, Kit asks the one thing left to ask");})
            .thenSucceed();
    }
    @AfterBatch(batch="place_off") public static void offDone(ServerLevel l){close();}
}
