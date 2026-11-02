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
                talk(f,p,"mara");h.assertTrue(Talk.waiting(p,"sink"),"Mara about Theo");say(h,p,"I'll sit with you tonight.");
                own=f.own(p,P);h.assertTrue(own.getString("Promise").equals("company")&&own.getBoolean("TheoTold")&&own.getInt("Chapter")==PlaceForYou.DINNER,"a promise made; dinner again");
                talk(f,p,"mara");say(h,p,"Take the spoon.");own=f.own(p,P);h.assertTrue(own.getBoolean("SpoonTaken")&&PlaceForYouBlock.is(l.getBlockState(b.offset(PlaceForYouSite.SPOON)),PlaceForYouBlock.Kind.SPOON),"the spoon out of her hand, set down on the counter");
                h.assertTrue(PlaceForYouSite.routineOut(l,b),"the routine going on in the empty bathroom");
                PlaceForYouSite.frontDoor(l,b,true);at(f,p,.5,0,-13.5,0,0);})
            .thenWaitUntil(()->h.assertTrue(!PlaceForYouSite.doorOpen(l,b),"the front door swings shut ahead of a reader who helped Kit"))
            .thenExecute(()->{h.assertTrue(click(f,p,PlaceForYouSite.FRONT_DOOR)&&f.own(p,P).getBoolean("DoorHeld")&&PlaceForYouSite.doorOpen(l,b),"held open");
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
                p.stopRiding();f.read(p,P,LiteraryRooms.ending(P));})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Completed"),"the account read"))
            .thenExecute(()->{h.assertTrue(f.data().isCompleted(P.id()),"the story resolved");
                var text=account(f,p);
                h.assertTrue(text.contains("talked as if I had lived there")&&text.contains("cleared the plates")&&text.contains("asked to be on my own")&&text.contains("laid out again")&&text.contains("I gave it back to Kit")&&text.contains("\"I'll sit with you tonight.\" I did.")&&text.contains("I held it")&&text.contains("it stopped")&&text.contains("hall chair")&&text.contains("Kit went out through the gate")&&text.contains("try it, and see")&&text.contains("as far as the road"),"the account says what this reader did and was told: "+text);
            })
            .thenSucceed();
    }
    @AfterBatch(batch="place_guest") public static void guestDone(ServerLevel l){close();}

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
            .thenExecute(()->{talk(f,p,"mara");say(h,p,"What does Kit want?");h.assertTrue(f.own(p,P).getString("Promise").equals("kit")&&chapter(f,p)==PlaceForYou.DINNER,"what Kit wants; dinner again");
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
