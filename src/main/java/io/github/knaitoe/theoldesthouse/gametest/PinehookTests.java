package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import io.github.knaitoe.theoldesthouse.labyrinth.PinehookBlock.Kind;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.364, Last Weekend at Pinehook, first slice, with real readers. The cabin built whole and not dealt: the woodshed and the drive, the
 * cabin with its porch, living room, kitchen, hall, bathroom and bedroom, the shed in the pines, the beach, the dock and Len's boathouse,
 * the lake; Casey's invitation on its stand. Two readers take parts from Casey on the porch and are handed their things; the cooler is
 * carried to the kitchen and Casey tells the carrier his secret; the music goes on, the film is chosen, the power is predicted; the
 * duffel's prop knife and the bag's strap are found; Len walks up the lane and Nina goes quiet in front of a reader who then asks her
 * about it. At dusk Jude slips away, the breaker goes, the lights go out (as predicted) and the mask is at the window; a reader pulls it
 * off and asks Casey whether he was going to tell them. Jude goes to change with nobody in the shed; Nina goes after him and comes out in
 * the costume; Jude comes back cut and asks Casey who else he hired while a reader on the back step watches a masked figure cross the
 * far end of the dock; Nina comes back "from the bathroom"; a reader takes the kit from the bathroom and dresses Jude's cut; the account
 * so far tells the evening as that reader had it. Then two shorter evenings: a reader who stays in the shed with Jude, so Nina waits
 * behind it and gives up; and a reader who gets into the shed while she is at him, and sees her.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_pinehook") @PrefixGameTestTemplate(false)
public final class PinehookTests {
    private PinehookTests(){}
    private static final LabyrinthPlace P=LabyrinthPlace.PINEHOOK;
    private static LiteraryTests.Fixture active;
    private static LiteraryTests.Fixture fixture(GameTestHelper h){if(active!=null)active.close();active=new LiteraryTests.Fixture(h);Pinehook.clearAll();return active;}
    private static AABB box(LiteraryTests.Fixture f){var b=f.base(P);return new AABB(b.getX()-37,b.getY()-5,b.getZ()-97,b.getX()+37,b.getY()+18,b.getZ()+3);}
    private static boolean use(LiteraryTests.Fixture f,ServerPlayer p,BlockPos rel){var at=f.base(P).offset(rel);return Pinehook.use(p,at,f.level(P).getBlockState(at));}
    private static int stage(ServerLevel l,BlockPos b,BlockPos rel){return PinehookBlock.stage(l.getBlockState(b.offset(rel)));}
    private static ItemStack thing(ServerPlayer p,String what){for(var st:p.getInventory().items){var c=st.get(DataComponents.CUSTOM_DATA);if(c!=null&&c.copyTag().getString("Pinehook").equals(what))return st;}return ItemStack.EMPTY;}
    private static LiteraryActor cast(LiteraryTests.Fixture f,String who){return (LiteraryActor)Pinehook.actor(f.level(P),f.base(P),who);}
    private static void close(){if(active!=null){active.close();active=null;}Pinehook.clearAll();}
    @AfterBatch(batch="pinehook") public static void done(ServerLevel l){close();}
    @AfterBatch(batch="pinehook_watched") public static void watchedDone(ServerLevel l){close();}
    @AfterBatch(batch="pinehook_interrupted") public static void interruptedDone(ServerLevel l){close();}
    @AfterBatch(batch="pinehook_night") public static void nightDone(ServerLevel l){close();}
    @AfterBatch(batch="pinehook_burn") public static void burnDone(ServerLevel l){close();}
    @AfterBatch(batch="pinehook_boathouse") public static void boathouseDone(ServerLevel l){close();}
    @AfterBatch(batch="pinehook_nina") public static void ninaDone(ServerLevel l){close();}
    @AfterBatch(batch="pinehook_exposed") public static void exposedDone(ServerLevel l){close();}
    @AfterBatch(batch="pinehook_performance") public static void performanceDone(ServerLevel l){close();}
    @AfterBatch(batch="pinehook_protected") public static void protectedDone(ServerLevel l){close();}
    private static boolean near(LiteraryTests.Fixture f,String who,Vec3 rel,double d){var a=cast(f,who);var b=f.base(P);return a!=null&&a.position().distanceTo(new Vec3(b.getX()+rel.x,b.getY()+rel.y,b.getZ()+rel.z))<d;}
    private static String account(LiteraryTests.Fixture f,ServerPlayer p){return String.join(" ",LiteraryTexts.ending(p.getGameProfile().getName(),P,f.own(p,P)).get(DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().map(pg->pg.raw().getString()).toList());}
    private static void look(LiteraryTests.Fixture f,ServerPlayer p,Vec3 rel){var b=f.base(P);p.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,new Vec3(b.getX()+rel.x,b.getY()+rel.y,b.getZ()+rel.z));}
    /** Readers at the porch, greeted by Casey, each taking the part given. */
    private static GameTestSequence parts(LiteraryTests.Fixture f,GameTestHelper h,ServerPlayer[] c,String prefix,String... parts){
        var b=f.base(P);var l=f.level(P);
        return h.startSequence().thenExecute(()->f.chunks.hold(l,box(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the cabin is resident"))
            .thenExecute(()->{for(int i=0;i<c.length;i++){c[i]=f.player(prefix+i,P);f.at(c[i],P,0.5+2*i,0,-16.5);}})
            .thenWaitUntil(()->h.assertTrue(cast(f,"casey")!=null&&cast(f,"jude")!=null&&cast(f,"nina")!=null,"Casey on the porch, Jude, Nina inside"))
            .thenWaitUntil(()->{for(var p:c)h.assertTrue(Talk.waiting(p,Pinehook.TALK),"Casey greets "+p.getGameProfile().getName());})
            .thenExecute(()->{for(int i=0;i<c.length;i++)h.assertTrue(Talk.choose(c[i],parts[i]),"the part offered: "+parts[i]);})
            .thenWaitUntil(()->h.assertTrue(Pinehook.phase(b)==Pinehook.EVENING,"the evening begins once everyone has a part"));
    }
    /** The evening run on to dusk, the scare started, and then run on until Jude is in the shed. */
    private static GameTestSequence toShed(GameTestSequence s,LiteraryTests.Fixture f,GameTestHelper h){
        var b=f.base(P);
        return s.thenExecute(()->Pinehook.skip(b,Pinehook.DUSK_LATE))
            .thenWaitUntil(()->h.assertTrue(Pinehook.phase(b)==Pinehook.SCARE,"dusk: the scare"))
            .thenExecute(()->Pinehook.skip(b,2400))
            .thenWaitUntil(()->h.assertTrue(Pinehook.phase(b)==Pinehook.SHED&&Pinehook.flag(b,"judeIn"),"Jude in the shed, changing"));
    }

    /** From the shed's outcome through the turn and the hold to the night: Len shut in, Jude and Casey in the kitchen. */
    private static GameTestSequence toNight(GameTestSequence s,LiteraryTests.Fixture f,GameTestHelper h,int outcome){
        var b=f.base(P);
        s=s.thenWaitUntil(()->h.assertTrue(Pinehook.outcome(b)==outcome,"the shed: "+outcome));
        if(outcome==Pinehook.WOUNDED)s=s.thenWaitUntil(()->h.assertTrue(Pinehook.flag(b,"figure"),"the figure across the dock")).thenExecute(()->Pinehook.skip(b,Pinehook.NINA_BACK));
        return s.thenWaitUntil(()->h.assertTrue(Pinehook.phase(b)==Pinehook.HOLD,"the hold"))
            .thenExecute(()->Pinehook.skip(b,Pinehook.NIGHT_AT))
            .thenWaitUntil(()->h.assertTrue(Pinehook.phase(b)==Pinehook.NIGHT&&Pinehook.flag(b,"lenInside")&&near(f,"jude",PinehookSite.JUDE_KITCHEN,1.2)&&near(f,"casey",PinehookSite.CASEY_KITCHEN,1.5),"the night: Len shut in, Jude and Casey in the kitchen"));
    }

    @GameTest(template="empty",batch="pinehook",timeoutTicks=8000)
    public static void theWeekendOpensAndThePrankGoesWrong(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);
        ServerPlayer[] c=new ServerPlayer[2];
        h.startSequence().thenExecute(()->f.chunks.hold(l,box(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the cabin is resident"))
            .thenIdle(20)
            .thenExecute(()->{
                h.assertTrue(LiteraryRooms.isLiterary(P)&&LiteraryRooms.outside(P)&&P.isOneShot()&&P.authored()&&SceneClock.at(f.origin,b.offset(0,0,-30),io.github.knaitoe.theoldesthouse.house.HouseDimensions.OUTSIDE)==3,"a literary cabin outside, one visit, built whole, at dusk");
                h.assertTrue(WitnessAccount.Story.of(P.id())==WitnessAccount.Story.PINEHOOK&&WitnessAccount.Story.PINEHOOK.kind.equals("understanding")&&!Pinehook.dealable(f.data(),UUID.randomUUID()),"an understanding Witness source, dealt only to readers standing together");
                h.assertTrue(l.getBlockState(b.offset(0,-1,-8)).is(Blocks.GRAVEL)&&l.getBlockState(b.offset(0,-1,-24)).is(Blocks.SPRUCE_PLANKS)&&l.getBlockState(b.offset(0,-2,-70)).is(Blocks.WATER)&&l.getBlockState(b.offset(6,-1,-60)).is(Blocks.SPRUCE_PLANKS),"the drive, the cabin's floor, the lake and the dock");
                h.assertTrue(l.getBlockState(b.offset(PinehookSite.BOATHOUSE_DOOR)).is(Blocks.IRON_DOOR)&&l.getBlockState(b.offset(PinehookSite.HEARTH)).is(Blocks.CAMPFIRE),"Len's boathouse shut, the fire lit");
                for(var k:List.of(PinehookSite.STEREO,PinehookSite.TV,PinehookSite.BREAKER,PinehookSite.STOVE,PinehookSite.DUFFEL,PinehookSite.BAG,PinehookSite.KIT,PinehookSite.COOLER_START,PinehookSite.TAPES))h.assertTrue(stage(l,b,k)==0,"a fitting at "+k);
                for(var lamp:PinehookSite.LAMPS)h.assertTrue(stage(l,b,lamp)==1,"a lamp lit at "+lamp);
                var words=String.join(" ",LiteraryTexts.source(P).get(DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().map(pg->pg.raw().getString()).toList());
                h.assertTrue(words.contains("PINEHOOK")&&words.contains("Len asked for her")&&words.contains("never forget"),"Casey's invitation: "+words);
                h.assertTrue(!SceneHuntReview.sourceBook(l,b,P).isEmpty(),"the invitation on its stand on the porch");})
            .thenExecute(()->{for(int i=0;i<2;i++){c[i]=f.player("ph"+i,P);f.at(c[i],P,0.5+2*i,0,-16.5);}})
            .thenWaitUntil(()->h.assertTrue(cast(f,"casey")!=null&&cast(f,"nina")!=null,"Casey on the porch"))
            .thenWaitUntil(()->h.assertTrue(Talk.waiting(c[0],Pinehook.TALK)&&Talk.waiting(c[1],Pinehook.TALK),"Casey greets both, the parts offered"))
            .thenExecute(()->{
                h.assertTrue(Talk.replies(c[0]).size()==7,"six parts and none: "+Talk.replies(c[0]));
                h.assertTrue(Talk.choose(c[0],"The Skeptic")&&!thing(c[0],"camera").isEmpty()&&!thing(c[0],"torch").isEmpty(),"the skeptic handed a camera and a torch");
                h.assertTrue(Pinehook.phase(b)==Pinehook.ARRIVAL,"the evening waits for the other");
                h.assertTrue(Talk.choose(c[1],"The Athlete")&&f.own(c[1],P).getString("Part").equals("ATHLETE"),"the athlete on the cooler");})
            .thenWaitUntil(()->h.assertTrue(Pinehook.phase(b)==Pinehook.EVENING,"the evening"))
            // The evening's things: the cooler in and the secret, the music, the film, the power, the knife, the strap.
            .thenExecute(()->{
                h.assertTrue(use(f,c[1],PinehookSite.COOLER_START)&&Pinehook.cooler(b).equals("carried")&&!thing(c[1],"cooler").isEmpty()&&l.getBlockState(b.offset(PinehookSite.COOLER_START)).isAir(),"the cooler lifted off the porch");
                f.at(c[1],P,5.5,0,-31.5);cast(f,"casey").moveTo(b.getX()+4.5,b.getY(),b.getZ()-30.5,0,0);
                h.assertTrue(Pinehook.useThingOn(c[1],thing(c[1],"cooler"),b.offset(PinehookSite.COUNTER))&&Pinehook.cooler(b).equals("kitchen")&&stage(l,b,PinehookSite.COOLER_SPOT)==0,"set down in the kitchen");
                h.assertTrue(Talk.waiting(c[1],Pinehook.TALK)&&Talk.choose(c[1],"I won't")&&f.own(c[1],P).getBoolean("KeptSecret")&&Pinehook.flag(b,"secret"),"Casey's secret told to the carrier, and kept");
                f.at(c[0],P,-4.5,0,-25.5);
                h.assertTrue(use(f,c[0],PinehookSite.STEREO)&&Talk.choose(c[0],"Put one of Casey's")&&Pinehook.flag(b,"music")&&stage(l,b,PinehookSite.STEREO)==1,"the music on");
                h.assertTrue(use(f,c[0],PinehookSite.TV)&&Talk.choose(c[0],"Put Casey's film")&&Pinehook.film(b)==1&&stage(l,b,PinehookSite.TV)==1,"Casey's film on");
                Pinehook.talk(c[0],cast(f,"casey"));h.assertTrue(Talk.choose(c[0],"Bet you")&&f.own(c[0],P).getBoolean("PredictedPower"),"the power predicted");
                Talk.close(c[0]);
                h.assertTrue(use(f,c[1],PinehookSite.DUFFEL)&&f.own(c[1],P).getBoolean("SawPropBlade")&&stage(l,b,PinehookSite.DUFFEL)==1,"the rubber knife in Jude's duffel");
                h.assertTrue(use(f,c[1],PinehookSite.BAG)&&!thing(c[1],"elastic").isEmpty()&&stage(l,b,PinehookSite.BAG)==1&&f.own(c[1],P).getBoolean("FoundElastic"),"a strap of elastic cut to size, behind the shed");
                h.assertTrue(use(f,c[1],PinehookSite.BAG)&&thing(c[1],"elastic").getCount()==1,"and only the one");
                // Len comes up the lane while the skeptic is in the living room with Nina in view.
                f.at(c[0],P,-1.5,0,-22.5);look(f,c[0],new Vec3(-4.5,1.6,-22.5));f.at(c[1],P,0.5,0,-16.5);
                Pinehook.skip(b,Pinehook.LEN_AT);})
            .thenWaitUntil(()->{look(f,c[0],new Vec3(-4.5,1.6,-22.5));h.assertTrue(Pinehook.flag(b,"len")&&Pinehook.flag(b,"quiet"),"Len up the lane and on the porch");})
            .thenExecute(()->{
                h.assertTrue(f.own(c[0],P).getBoolean("SawNinaQuiet"),"Nina goes quiet, and the skeptic sees it");
                Pinehook.talk(c[0],cast(f,"nina"));h.assertTrue(Talk.choose(c[0],"What did Len")&&f.own(c[0],P).getBoolean("AskedNina"),"and asks her what he said");Talk.close(c[0]);
                Pinehook.talk(c[1],cast(f,"len"));h.assertTrue(Talk.choose(c[1],"We'll stay")&&f.own(c[1],P).getBoolean("TookLenSeriously"),"Len taken at his word");Talk.close(c[1]);
                Pinehook.skip(b,Pinehook.DUSK_EARLY);})
            .thenWaitUntil(()->h.assertTrue(f.own(c[0],P).getBoolean("HeardJudeLeave")||f.own(c[1],P).getBoolean("HeardJudeLeave")||Pinehook.phase(b)==Pinehook.SCARE,"Jude slips away to look at the lake"))
            .thenExecute(()->Pinehook.skip(b,Pinehook.JUDE_GOES))
            .thenWaitUntil(()->h.assertTrue(Pinehook.phase(b)==Pinehook.SCARE,"dusk, and the scare"))
            .thenExecute(()->{
                h.assertTrue(!Pinehook.flag(b,"power")&&stage(l,b,PinehookSite.BREAKER)==1&&stage(l,b,PinehookSite.LAMPS.get(0))==0&&stage(l,b,PinehookSite.STEREO)==0,"the breaker gone, the lamps dark, the music off");
                h.assertTrue(f.own(c[0],P).getBoolean("CalledIt"),"as the skeptic said it would");
                f.at(c[0],P,-3.5,0,-17.5);f.at(c[1],P,1.5,0,-16.5);})
            .thenWaitUntil(()->{var jude=cast(f,"jude");h.assertTrue(jude!=null&&!jude.isInvisible()&&jude.phase()==Pinehook.MASKED&&jude.gesture()==Pinehook.KNOCKING,"the masked figure at the window, knocking");})
            // The early unmasking.
            .thenExecute(()->{
                var jude=cast(f,"jude");f.at(c[0],P,jude.getX()-b.getX()+1,0,jude.getZ()-b.getZ()+.8);
                Pinehook.talk(c[0],jude);h.assertTrue(Talk.choose(c[0],"Pull the mask"),"a hand on the mask");
                h.assertTrue(Pinehook.flag(b,"unmasked")&&f.own(c[0],P).getBoolean("Unmasked")&&cast(f,"jude").phase()==Pinehook.COVERALL,"the mask off: Jude, in the coverall");
                h.assertTrue(Pinehook.flag(b,"power")&&stage(l,b,PinehookSite.LAMPS.get(0))==1,"Casey owns up and puts the power back");
                Pinehook.talk(c[0],cast(f,"casey"));h.assertTrue(Talk.choose(c[0],"Were you going")&&f.own(c[0],P).getBoolean("AskedToldUs"),"and is asked whether he was going to tell them");Talk.close(c[0]);
                f.at(c[0],P,0.5,0,-16.5);})
            .thenWaitUntil(()->h.assertTrue(Pinehook.phase(b)==Pinehook.SHED,"Jude goes off to the shed to change"))
            // Nobody with him: Nina goes after him.
            .thenWaitUntil(()->h.assertTrue(Pinehook.ninaStep(b)>=1,"Nina out of the back door"))
            .thenWaitUntil(()->h.assertTrue(Pinehook.outcome(b)==Pinehook.WOUNDED,"nobody came: Jude is cut and the costume taken"))
            .thenExecute(()->{
                h.assertTrue(stage(l,b,PinehookSite.DUFFEL)==2&&cast(f,"jude").gesture()==Pinehook.HURT&&cast(f,"nina").phase()==Pinehook.MASKED&&cast(f,"nina").isInvisible(),"the duffel emptied, Jude holding his side, Nina in the costume and out of sight");
                for(var p:c)h.assertTrue(f.own(p,P).getString("Outcome").equals("wounded"),"the outcome on the record");
                f.at(c[0],P,4.5,0,-36.5);f.at(c[1],P,6.5,0,-37.5);})
            .thenWaitUntil(()->{look(f,c[1],new Vec3(6.5,1,-63.5));h.assertTrue(f.own(c[0],P).getBoolean("HeardTurn"),"Jude back on the back step: who else did you hire?");})
            .thenWaitUntil(()->{look(f,c[1],new Vec3(6.5,1,-63.5));h.assertTrue(Pinehook.flag(b,"figure"),"a masked figure across the far end of the dock");})
            .thenExecute(()->h.assertTrue(f.own(c[1],P).getBoolean("SawFigure")&&f.own(c[1],P).getBoolean("SawJudeHurt"),"seen from the back step"))
            .thenWaitUntil(()->h.assertTrue(Pinehook.phase(b)==Pinehook.HOLD&&Pinehook.flag(b,"ninaBack")&&cast(f,"nina").phase()==Pinehook.NINA&&!cast(f,"nina").isInvisible(),"Nina back in the cabin, from the bathroom, she says; the night holds"))
            .thenExecute(()->{
                h.assertTrue(use(f,c[1],PinehookSite.KIT)&&!thing(c[1],"firstaid").isEmpty()&&stage(l,b,PinehookSite.KIT)==1,"the first-aid kit off the bathroom wall");
                var jude=cast(f,"jude");f.at(c[1],P,jude.getX()-b.getX()+1,0,jude.getZ()-b.getZ());
                h.assertTrue(Pinehook.useThing(c[1],thing(c[1],"firstaid"),jude)&&Pinehook.flag(b,"bandaged")&&f.own(c[1],P).getBoolean("HelpedJude")&&thing(c[1],"firstaid").isEmpty(),"Jude's cut dressed");
                var account=String.join(" ",LiteraryTexts.ending(c[0].getGameProfile().getName(),P,f.own(c[0],P)).get(DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().map(pg->pg.raw().getString()).toList());
                h.assertTrue(account.contains("the skeptic")&&account.contains("pulled it off")&&account.contains("asked Casey who else")&&account.contains("lake was low"),"the evening as the skeptic had it: "+account);
                h.assertTrue(WitnessAccount.count(f.data(),c[0].getUUID())==0&&WitnessAccount.count(f.data(),c[1].getUUID())==0,"and nothing is earned");})
            .thenSucceed();
    }

    @GameTest(template="empty",batch="pinehook_watched",timeoutTicks=5000)
    public static void someoneStaysWithJudeAndNinaGivesUp(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);
        ServerPlayer[] c=new ServerPlayer[1];
        toShed(parts(f,h,c,"pw","The Responsible One"),f,h)
            .thenExecute(()->{h.assertTrue(!thing(c[0],"firstaid").isEmpty(),"the responsible one brought the first-aid bag");
                f.at(c[0],P,-16.5,0,-41.5);Pinehook.skip(b,Pinehook.NINA_AFTER);})
            .thenWaitUntil(()->h.assertTrue(Pinehook.ninaStep(b)==2,"Nina at the shed door, someone inside with Jude: she goes round the back"))
            .thenWaitUntil(()->h.assertTrue(Pinehook.outcome(b)==Pinehook.PREVENTED,"Jude done changing with company: she gives up"))
            .thenExecute(()->{h.assertTrue(f.own(c[0],P).getBoolean("WithJude")&&f.own(c[0],P).getString("Outcome").equals("prevented"),"stayed with him, on the record");
                h.assertTrue(stage(l,b,PinehookSite.DUFFEL)<2&&cast(f,"jude").phase()==Pinehook.JUDE,"the costume back in the duffel, Jude in his own clothes");
                f.at(c[0],P,4.5,0,-36.5);})
            .thenWaitUntil(()->h.assertTrue(Pinehook.phase(b)==Pinehook.HOLD,"back at the cabin; the night holds"))
            .thenExecute(()->h.assertTrue(f.own(c[0],P).getBoolean("HeardTurn")&&!Pinehook.flag(b,"figure"),"thanks for the company; nobody on the dock"))
            .thenSucceed();
    }

    @GameTest(template="empty",batch="pinehook_interrupted",timeoutTicks=5000)
    public static void aReaderGetsIntoTheShedInTime(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);
        ServerPlayer[] c=new ServerPlayer[1];
        toShed(parts(f,h,c,"pi","The Horror Fan"),f,h)
            .thenExecute(()->{h.assertTrue(!thing(c[0],"tape").isEmpty(),"the horror fan brought a film");
                f.at(c[0],P,-12.5,0,-38.5);Pinehook.skip(b,Pinehook.NINA_AFTER);})
            .thenWaitUntil(()->h.assertTrue(Pinehook.ninaStep(b)==3,"Nina in the shed with Jude"))
            .thenExecute(()->{f.at(c[0],P,-18.5,0,-40.5);var nina=cast(f,"nina");look(f,c[0],nina.getEyePosition().subtract(b.getX(),b.getY(),b.getZ()));})
            .thenWaitUntil(()->h.assertTrue(Pinehook.outcome(b)==Pinehook.WITNESSED,"in through the door while she's at him"))
            .thenExecute(()->{var own=f.own(c[0],P);h.assertTrue(own.getBoolean("InterruptedNina")&&own.getBoolean("WitnessedAttack")&&own.getBoolean("HeardCrash"),"stopped her, saw her, heard it: "+own);
                h.assertTrue(stage(l,b,PinehookSite.DUFFEL)<2&&cast(f,"jude").gesture()!=Pinehook.HURT,"Jude not cut, the costume still his");
                h.assertTrue(l.getBlockState(b.offset(PinehookSite.SHED_BACK)).isAir()&&PinehookBlock.is(l.getBlockState(b.offset(PinehookSite.BEACH_PRINTS.get(0))),PinehookBlock.Kind.PRINTS),"the shed's back window broken, her prints along the beach");
                h.assertTrue(use(f,c[0],PinehookSite.BEACH_PRINTS.get(0))&&f.own(c[0],P).getBoolean("SawBeachPrints"),"running, toward the boathouse");
                f.at(c[0],P,4.5,0,-36.5);})
            .thenWaitUntil(()->h.assertTrue(Pinehook.phase(b)==Pinehook.HOLD,"back at the cabin, Jude naming her; the night holds"))
            .thenExecute(()->h.assertTrue(f.own(c[0],P).getBoolean("HeardTurn")&&!Pinehook.flag(b,"figure"),"it was Nina, with a knife; nobody on the dock"))
            .thenSucceed();
    }

    /** 0.4.365, the night after a wounding: the player set down by the back door before the shed; Jude in the kitchen, Casey talking over
     * him until he is told to let Jude talk; the strap; who knew; the dock's evidence and the costume in the boat; Casey's plan read and two
     * things told to him, so that he turns and never burns it; the tape; Len through the door; Nina denying it; the account. */
    @GameTest(template="empty",batch="pinehook_night",timeoutTicks=7000)
    public static void theNightTurnsCasey(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);
        ServerPlayer[] c=new ServerPlayer[2];
        var recorder=new BlockPos(3,0,-36);
        toShed(parts(f,h,c,"pn","The Wildcard","The Athlete")
            .thenExecute(()->{
                h.assertTrue(!thing(c[0],"player").isEmpty(),"the wildcard's portable player");
                f.at(c[0],P,3.5,0,-35.5);
                h.assertTrue(Pinehook.useThingOn(c[0],thing(c[0],"player"),b.offset(recorder.below()))&&PinehookBlock.is(l.getBlockState(b.offset(recorder)),PinehookBlock.Kind.RECORDER)&&thing(c[0],"player").isEmpty(),"set down by the back step, recording");
                f.at(c[1],P,-15.5,0,-45.5);
                h.assertTrue(use(f,c[1],PinehookSite.BAG)&&f.own(c[1],P).getBoolean("FoundElastic"),"the strap behind the shed");
                f.at(c[0],P,0.5,0,-16.5);f.at(c[1],P,1.5,0,-16.5);}),f,h)
            .thenWaitUntil(()->h.assertTrue(Pinehook.outcome(b)==Pinehook.WOUNDED,"nobody with Jude: wounded"))
            .thenWaitUntil(()->h.assertTrue(Pinehook.flag(b,"figure"),"the figure across the dock"))
            .thenIdle(Pinehook.NINA_IN_DELAY+10)
            .thenExecute(()->{
                h.assertTrue(PinehookBlock.is(l.getBlockState(b.offset(PinehookSite.STERN_WEST)),PinehookBlock.Kind.BOAT_STERN)&&stage(l,b,PinehookSite.STERN_WEST)==1&&l.getBlockState(b.offset(PinehookSite.STERN_EAST)).is(Blocks.WATER),"the rowboat pulled round, the tarp over something");
                h.assertTrue(PinehookBlock.is(l.getBlockState(b.offset(PinehookSite.DOCK_TIE)),PinehookBlock.Kind.TIE)&&PinehookBlock.is(l.getBlockState(b.offset(PinehookSite.WET_PRINTS.get(2))),PinehookBlock.Kind.PRINTS)&&PinehookBlock.is(l.getBlockState(b.offset(PinehookSite.SHED_BLOOD)),PinehookBlock.Kind.BLOOD),"the tie, the wet prints, the shed floor");
                Pinehook.skip(b,Pinehook.NINA_BACK);})
            .thenWaitUntil(()->h.assertTrue(Pinehook.phase(b)==Pinehook.HOLD,"Nina back; the hold"))
            .thenExecute(()->Pinehook.skip(b,Pinehook.NIGHT_AT))
            .thenWaitUntil(()->h.assertTrue(Pinehook.phase(b)==Pinehook.NIGHT,"the night"))
            .thenWaitUntil(()->h.assertTrue(near(f,"jude",PinehookSite.JUDE_KITCHEN,1.2)&&near(f,"casey",PinehookSite.JUDE_KITCHEN,8)&&Pinehook.flag(b,"lenInside"),"Jude against the kitchen counter, Casey with him, Len locked in his boathouse"))
            .thenExecute(()->{
                f.at(c[1],P,6.5,0,-30.5);
                Pinehook.talk(c[1],cast(f,"jude"));h.assertTrue(Talk.replies(c[1]).size()==2&&Talk.choose(c[1],"Casey. Let him")&&Pinehook.flag(b,"hushed")&&f.own(c[1],P).getBoolean("CaseyHushed"),"Casey talks over him until he's told to let him talk");
                Pinehook.talk(c[1],cast(f,"jude"));h.assertTrue(Talk.choose(c[1],"Do you tie")&&f.own(c[1],P).getBoolean("StrapMismatch"),"Jude knots his; the strap had buckles");
                Pinehook.talk(c[1],cast(f,"jude"));h.assertTrue(Talk.choose(c[1],"Who knew")&&f.own(c[1],P).getBoolean("HeardCaseyToldNina"),"and Casey told Nina");
                Pinehook.talk(c[1],cast(f,"jude"));h.assertTrue(Talk.choose(c[1],"Did you see anyone")&&f.own(c[1],P).getBoolean("HeardJudeSawNina"),"Nina at the boathouse door");Talk.close(c[1]);
                // The tape.
                f.at(c[1],P,3.5,0,-35.5);
                h.assertTrue(use(f,c[1],recorder)&&l.getBlockState(b.offset(recorder)).isAir()&&!thing(c[1],"player").isEmpty(),"picked up, stopped");
                h.assertTrue(Pinehook.useThing(c[1],thing(c[1],"player"),null)&&f.own(c[1],P).getBoolean("TapeNina"),"the back door, and much later someone coming back in dripping");Talk.close(c[1]);
                // The dock.
                f.at(c[0],P,5.5,0,-47.5);
                h.assertTrue(use(f,c[0],PinehookSite.WET_PRINTS.get(0))&&f.own(c[0],P).getBoolean("SawWetPrints"),"wet prints from the water");
                f.at(c[0],P,5.5,0,-62.5);
                h.assertTrue(use(f,c[0],PinehookSite.DOCK_BLOOD.get(1))&&f.own(c[0],P).getBoolean("SawDockBlood"),"spots along the T");
                h.assertTrue(use(f,c[0],PinehookSite.DOCK_TIE)&&!thing(c[0],"tie").isEmpty()&&l.getBlockState(b.offset(PinehookSite.DOCK_TIE)).isAir(),"the tie off the post");
                h.assertTrue(use(f,c[0],PinehookSite.STERN_WEST)&&stage(l,b,PinehookSite.STERN_WEST)==2&&f.own(c[0],P).getBoolean("FoundCostume"),"the mask and coverall under the tarp");
                h.assertTrue(use(f,c[1],PinehookSite.STERN_WEST)&&!thing(c[1],"costume").isEmpty()&&Pinehook.flag(b,"costume"),"lifted out by the other");
                // The costume laid out on the kitchen table by the one who knows Jude ties his strap.
                f.at(c[1],P,5.5,0,-30.5);
                h.assertTrue(Pinehook.useThingOn(c[1],thing(c[1],"costume"),b.offset(PinehookSite.TABLE))&&Pinehook.flag(b,"timeline")&&f.own(c[1],P).getBoolean("Timeline"),"the strap cut before tonight");Talk.close(c[1]);
                // The plan.
                f.at(c[0],P,-5.5,0,-30.5);
                h.assertTrue(use(f,c[0],PinehookSite.SUITCASE)&&!thing(c[0],"plan").isEmpty()&&stage(l,b,PinehookSite.SUITCASE)==1&&Pinehook.flag(b,"planTaken"),"Casey's plan out of his bag");
                h.assertTrue(Pinehook.useThing(c[0],thing(c[0],"plan"),null)&&f.own(c[0],P).getBoolean("ReadPlan"),"read: midnight on the dock");Talk.close(c[0]);
                // Telling Casey.
                f.at(c[0],P,6.5,0,-29.5);
                Pinehook.talk(c[0],cast(f,"casey"));h.assertTrue(Talk.choose(c[0],"Not yet"),"he wants his plan back; not yet");Talk.close(c[0]);
                Pinehook.talk(c[0],cast(f,"casey"));h.assertTrue(Talk.choose(c[0],"There are wet footprints")&&!Pinehook.flag(b,"turned"),"one thing: people swim");Talk.close(c[0]);
                Pinehook.talk(c[0],cast(f,"casey"));h.assertTrue(Talk.choose(c[0],"Your plan puts")&&Pinehook.flag(b,"turned")&&f.own(c[0],P).getBoolean("TurnedCasey")&&f.own(c[1],P).getBoolean("HeardCaseyTurn"),"two: it's not a bit");Talk.close(c[0]);
                h.assertTrue(Integer.bitCount(Pinehook.caseyKnows(b))==2,"Casey told two things");
                // Len and Nina.
                f.at(c[1],P,15.5,0,-43.5);
                h.assertTrue(use(f,c[1],PinehookSite.WET_PRINTS.get(1))&&f.own(c[1],P).getBoolean("SawWetPrints"),"the prints up from the water");
                h.assertTrue(Pinehook.knockAt(c[1])&&Talk.waiting(c[1],Pinehook.TALK)&&Talk.choose(c[1],"Somebody came up out of the water")&&f.own(c[1],P).getBoolean("LenSlipped"),"through the door, Len says she");Talk.close(c[1]);
                // The slip opened from the dock, and in by the water; Len tells where the key is.
                f.at(c[0],P,7.5,0,-52.5);
                h.assertTrue(use(f,c[0],PinehookSite.RELEASE)&&Pinehook.flag(b,"slip")&&l.getBlockState(b.offset(PinehookSite.GATE)).isAir(),"the rope off the cleat: the slip gate open onto the lake");
                f.at(c[1],P,15.5,0,-52.5);
                Pinehook.talk(c[1],cast(f,"len"));h.assertTrue(Talk.choose(c[1],"Whatever's in that safe")&&f.own(c[1],P).getBoolean("KnowsSafeKey"),"Len: the key's under the bench");Talk.close(c[1]);
                h.assertTrue(Pinehook.benchKey(c[1])&&!thing(c[1],"key").isEmpty(),"under the bench, the key");
                h.assertTrue(Pinehook.useThingOn(c[1],thing(c[1],"key"),b.offset(PinehookSite.SAFE))&&stage(l,b,PinehookSite.SAFE)==1,"the safe open");
                h.assertTrue(use(f,c[1],PinehookSite.SAFE)&&!thing(c[1],"envelope").isEmpty()&&Pinehook.flag(b,"envelope"),"the envelope");
                h.assertTrue(Pinehook.useThing(c[1],thing(c[1],"envelope"),null)&&f.own(c[1],P).getBoolean("ReadEnvelope"),"a year of payments marked N");Talk.close(c[1]);
                var nina=cast(f,"nina");f.at(c[1],P,nina.getX()-b.getX()+1,0,nina.getZ()-b.getZ());
                Pinehook.talk(c[1],nina);h.assertTrue(Talk.choose(c[1],"Did you go outside")&&f.own(c[1],P).getBoolean("NinaDenied"),"no, the bathroom");Talk.close(c[1]);
                Pinehook.talk(c[1],nina);h.assertTrue(Talk.choose(c[1],"Somebody came in the back door")&&f.own(c[1],P).getBoolean("ConfrontedNina"),"then somebody else went swimming");Talk.close(c[1]);
                Pinehook.skip(b,Pinehook.BURN_AT);})
            .thenIdle(40)
            .thenExecute(()->{
                h.assertTrue(!Pinehook.flag(b,"burned")&&Pinehook.burnStep(b)==0,"turned, and the plan already taken: nothing for him to burn");
                var one=account(f,c[1]);h.assertTrue(one.contains("marked N")&&one.contains("before tonight")&&one.contains("let Jude talk")&&one.contains("buckle at each end")&&one.contains("dripping")&&one.contains("hadn't said she"),"the athlete's night: "+one);
                var zero=account(f,c[0]);h.assertTrue(zero.contains("rowboat")&&zero.contains("midnight")&&zero.contains("stopped saying it was a bit"),"the wildcard's night: "+zero);})
            .thenSucceed();
    }

    /** 0.4.365: nobody finds the plan, so Casey goes to his bag and puts it in the fire, seen from the living room; a watched shed leaves
     * prints behind it, and Jude, unhurt, is not talked over. */
    @GameTest(template="empty",batch="pinehook_burn",timeoutTicks=5000)
    public static void caseyBurnsHisPlanIfNobodyFindsIt(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);
        ServerPlayer[] c=new ServerPlayer[1];
        toShed(parts(f,h,c,"pb","The Charmer"),f,h)
            .thenExecute(()->{f.at(c[0],P,-16.5,0,-41.5);Pinehook.skip(b,Pinehook.NINA_AFTER);})
            .thenExecute(()->h.assertTrue(stage(l,b,PinehookSite.FLOOD_T)==0&&stage(l,b,PinehookSite.FLOOD_REAR)==0,"the outside floods went with the breaker"))
            .thenWaitUntil(()->h.assertTrue(Pinehook.outcome(b)==Pinehook.PREVENTED,"watched: prevented"))
            .thenExecute(()->{f.at(c[0],P,-14.5,0,-44.5);h.assertTrue(use(f,c[0],PinehookSite.SHED_PRINTS.get(0))&&f.own(c[0],P).getBoolean("SawShedPrints"),"someone stood behind the shed a long time");
                f.at(c[0],P,4.5,0,-36.5);})
            .thenWaitUntil(()->h.assertTrue(Pinehook.phase(b)==Pinehook.HOLD,"the hold"))
            .thenExecute(()->Pinehook.skip(b,Pinehook.NIGHT_AT))
            .thenWaitUntil(()->h.assertTrue(Pinehook.phase(b)==Pinehook.NIGHT&&near(f,"jude",PinehookSite.JUDE_KITCHEN,1.2),"the night, Jude in the kitchen"))
            .thenExecute(()->{
                f.at(c[0],P,6.5,0,-30.5);
                Pinehook.talk(c[0],cast(f,"jude"));h.assertTrue(Talk.choose(c[0],"Who was behind the shed")&&f.own(c[0],P).getBoolean("HeardJudeAccount"),"unhurt, not talked over");Talk.close(c[0]);
                h.assertTrue(stage(l,b,PinehookSite.FLOOD_T)==0&&Pinehook.flag(b,"power"),"the cabin's lights back, the outside still dark");
                f.at(c[0],P,10.5,0,-31.5);
                h.assertTrue(use(f,c[0],PinehookSite.JUNCTION)&&Talk.choose(c[0],"Throw it to the dock")&&Pinehook.junction(b)==1&&stage(l,b,PinehookSite.FLOOD_T)==1&&stage(l,b,PinehookSite.FLOOD_REAR)==0,"the dock lit, the back dark");
                h.assertTrue(use(f,c[0],PinehookSite.JUNCTION)&&Talk.choose(c[0],"Throw it to the back")&&Pinehook.junction(b)==2&&stage(l,b,PinehookSite.FLOOD_T)==0&&stage(l,b,PinehookSite.FLOOD_REAR)==1,"one or the other");
                h.assertTrue(use(f,c[0],PinehookSite.JUNCTION)&&Talk.choose(c[0],"Work on the burnt wire"),"to work on it");})
            .thenWaitUntil(()->h.assertTrue(Pinehook.mends(b)>=1,"one wire"))
            .thenExecute(()->h.assertTrue(use(f,c[0],PinehookSite.JUNCTION)&&Talk.choose(c[0],"Work on the burnt wire"),"the next"))
            .thenWaitUntil(()->h.assertTrue(Pinehook.mends(b)>=2,"two"))
            .thenExecute(()->h.assertTrue(use(f,c[0],PinehookSite.JUNCTION)&&Talk.choose(c[0],"Work on the burnt wire"),"the last"))
            .thenWaitUntil(()->h.assertTrue(Pinehook.junction(b)==3&&stage(l,b,PinehookSite.FLOOD_T)==1&&stage(l,b,PinehookSite.FLOOD_REAR)==1&&stage(l,b,PinehookSite.FLOOD_BEACH)==1,"mended: both ways lit"))
            .thenExecute(()->{h.assertTrue(f.own(c[0],P).getBoolean("FinishedJunction"),"stood out there in the dark until it was done");
                f.at(c[0],P,-5.5,0,-21.5);look(f,c[0],new Vec3(-6.5,1.2,-24.5));
                Pinehook.skip(b,Pinehook.BURN_AT);})
            .thenWaitUntil(()->h.assertTrue(Pinehook.burnStep(b)>=2,"Casey to his bag, the plan out of it"))
            .thenExecute(()->h.assertTrue(stage(l,b,PinehookSite.SUITCASE)==1,"his bag open, empty"))
            .thenWaitUntil(()->{look(f,c[0],new Vec3(-6.5,1.2,-24.5));h.assertTrue(Pinehook.flag(b,"burned"),"and into the fire");})
            .thenExecute(()->h.assertTrue(f.own(c[0],P).getBoolean("SawCaseyBurn")&&account(f,c[0]).contains("sheet of paper in the fire"),"seen from the living room, and on the record"))
            .thenSucceed();
    }

    /** 0.4.366, the boathouse and the boat, in a watched night: the brace across the door and what Len makes of it; the slip opened from the
     * dock and Len shouting; in by the water; the rack's hook and oars; the safe's rusted back pried; the envelope; the oars in the boat; the
     * paddle in the woodshed. */
    @GameTest(template="empty",batch="pinehook_boathouse",timeoutTicks=5000)
    public static void theBoathouseAndTheBoat(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);
        ServerPlayer[] c=new ServerPlayer[1];
        toShed(parts(f,h,c,"pd","The Athlete"),f,h)
            .thenExecute(()->{f.at(c[0],P,-16.5,0,-41.5);Pinehook.skip(b,Pinehook.NINA_AFTER);})
            .thenWaitUntil(()->h.assertTrue(Pinehook.outcome(b)==Pinehook.PREVENTED,"watched: prevented"))
            .thenExecute(()->f.at(c[0],P,4.5,0,-36.5))
            .thenWaitUntil(()->h.assertTrue(Pinehook.phase(b)==Pinehook.HOLD,"the hold"))
            .thenExecute(()->Pinehook.skip(b,Pinehook.NIGHT_AT))
            .thenWaitUntil(()->h.assertTrue(Pinehook.phase(b)==Pinehook.NIGHT&&Pinehook.flag(b,"lenInside"),"the night, Len shut in"))
            .thenExecute(()->{
                f.at(c[0],P,14.5,0,-43.5);
                h.assertTrue(use(f,c[0],PinehookSite.BRACE_LEANING)&&Pinehook.flag(b,"braced")&&stage(l,b,PinehookSite.BRACE_ACROSS)==1&&l.getBlockState(b.offset(PinehookSite.BRACE_LEANING)).isAir(),"the brace across the door");
                h.assertTrue(Pinehook.knockAt(c[0])&&Talk.choose(c[0],"It's to keep her out")&&f.own(c[0],P).getBoolean("BracedForLen"),"to keep her out, and Len lets it stay");Talk.close(c[0]);
                f.at(c[0],P,7.5,0,-52.5);
                h.assertTrue(use(f,c[0],PinehookSite.RELEASE)&&Pinehook.flag(b,"slip")&&f.own(c[0],P).getBoolean("HeardLenSlip"),"the slip open, and Len shouting");
                f.at(c[0],P,15.5,0,-52.5);})
            .thenWaitUntil(()->h.assertTrue(f.own(c[0],P).getBoolean("WentInBySlip"),"in by the water"))
            .thenExecute(()->{
                h.assertTrue(use(f,c[0],PinehookSite.RACK)&&Talk.choose(c[0],"Take the boat hook")&&!thing(c[0],"hook").isEmpty()&&stage(l,b,PinehookSite.RACK)==2,"the boat hook off the rack");
                h.assertTrue(use(f,c[0],PinehookSite.RACK)&&Talk.choose(c[0],"Take the oars")&&!thing(c[0],"oars").isEmpty()&&stage(l,b,PinehookSite.RACK)==3,"and the oars");
                h.assertTrue(use(f,c[0],PinehookSite.SAFE)&&f.own(c[0],P).getBoolean("SawSafeBack"),"its back rusted where the boards stay wet");
                for(int i=0;i<Pinehook.PRY-1;i++)h.assertTrue(Pinehook.useThingOn(c[0],thing(c[0],"hook"),b.offset(PinehookSite.SAFE))&&stage(l,b,PinehookSite.SAFE)==0,"the hook in the seam");
                h.assertTrue(Pinehook.useThingOn(c[0],thing(c[0],"hook"),b.offset(PinehookSite.SAFE))&&stage(l,b,PinehookSite.SAFE)==1&&f.own(c[0],P).getBoolean("PriedSafe"),"the back torn away");
                h.assertTrue(use(f,c[0],PinehookSite.SAFE)&&!thing(c[0],"envelope").isEmpty()&&stage(l,b,PinehookSite.SAFE)==2,"the envelope out");
                h.assertTrue(Pinehook.useThing(c[0],thing(c[0],"envelope"),null)&&f.own(c[0],P).getBoolean("ReadEnvelope"),"read");Talk.close(c[0]);
                f.at(c[0],P,8.5,0,-62.5);
                boolean used=Pinehook.useThingOn(c[0],thing(c[0],"oars"),b.offset(PinehookSite.STERN_EAST));
                h.assertTrue(used&&Pinehook.boatReady(b)==1&&stage(l,b,PinehookSite.BOW_EAST)==1&&thing(c[0],"oars").isEmpty(),"the oars in the rowboat: ready ("+used+", ready "+Pinehook.boatReady(b)+", stern "+l.getBlockState(b.offset(PinehookSite.STERN_EAST))+", bow "+l.getBlockState(b.offset(PinehookSite.BOW_EAST))+", phase "+Pinehook.phase(b)+", oars "+!thing(c[0],"oars").isEmpty()+")");
                f.at(c[0],P,1.5,0,-1.5);
                h.assertTrue(use(f,c[0],PinehookSite.PADDLE)&&!thing(c[0],"paddle").isEmpty()&&l.getBlockState(b.offset(PinehookSite.PADDLE)).isAir(),"the paddle from the woodshed");
                var acc=account(f,c[0]);h.assertTrue(acc.contains("keep her out")&&acc.contains("marked N")&&acc.contains("rowboat ready"),"the night on the record: "+acc);})
            .thenSucceed();
    }

    /** 0.4.367, the crisis, Nina's way: Jude's cut dressed; the brace across Len's door to keep him in; a reader in Nina's way at the cleat
     * who opens the slip for her instead; Len trapped with her, shouting, then quiet; the envelope with her into the lake; first light, and
     * who opened the slip owned up to; every account written, and a reader who wasn't there owed theirs. */
    @GameTest(template="empty",batch="pinehook_nina",timeoutTicks=9000)
    public static void ninaFinishesItWhenTheSlipIsOpenedForHer(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);f.build(P);
        ServerPlayer[] c=new ServerPlayer[2];var absent=UUID.randomUUID();
        toNight(toShed(parts(f,h,c,"pf","The Athlete","The Charmer"),f,h),f,h,Pinehook.WOUNDED)
            .thenExecute(()->{
                Pinehook.roll(b,absent);
                f.at(c[1],P,1.5,0,-31.5);h.assertTrue(use(f,c[1],PinehookSite.KIT)&&!thing(c[1],"firstaid").isEmpty(),"the kit off the wall");
                f.at(c[1],P,6.5,0,-31.5);h.assertTrue(Pinehook.useThing(c[1],thing(c[1],"firstaid"),cast(f,"jude"))&&Pinehook.flag(b,"bandaged"),"Jude's cut dressed");
                f.at(c[0],P,14.5,0,-43.5);
                h.assertTrue(use(f,c[0],PinehookSite.BRACE_LEANING)&&Pinehook.flag(b,"braced"),"the brace across the boathouse door");
                h.assertTrue(Pinehook.knockAt(c[0])&&Talk.choose(c[0],"So you stay")&&f.own(c[0],P).getBoolean("BracedAgainstLen"),"to keep Len where he is");Talk.close(c[0]);
                f.at(c[0],P,7.5,0,-51.5);f.at(c[1],P,4.5,0,-28.5);
                Pinehook.skip(b,Pinehook.CRISIS_AT);})
            .thenWaitUntil(()->h.assertTrue(Pinehook.phase(b)==Pinehook.CRISIS,"the crisis"))
            .thenWaitUntil(()->h.assertTrue(Pinehook.ninaCrisis(b)==Pinehook.N_CLEAT&&f.own(c[0],P).getBoolean("GuardedCleat"),"Nina down at the cleat, and someone in her way"))
            .thenExecute(()->{Pinehook.talk(c[0],cast(f,"nina"));h.assertTrue(Talk.choose(c[0],"I'll open it for you")&&Pinehook.flag(b,"helped")&&Pinehook.flag(b,"slip")&&f.own(c[0],P).getBoolean("HelpedNina"),"the slip opened for her");Talk.close(c[0]);})
            .thenWaitUntil(()->h.assertTrue(Pinehook.ninaCrisis(b)==Pinehook.N_INSIDE,"in by the water, and Len braced in with her"))
            .thenExecute(()->Pinehook.skip(b,Pinehook.ATTACK))
            .thenWaitUntil(()->h.assertTrue(Pinehook.flag(b,"lenDead")&&f.own(c[0],P).getBoolean("HeardLenAttacked")&&f.own(c[0],P).getBoolean("HeardLenGoQuiet"),"Len shouting for help, then quiet"))
            .thenExecute(()->Pinehook.skip(b,Pinehook.SAFE_TIME))
            .thenWaitUntil(()->h.assertTrue(Pinehook.flag(b,"ninaGone")&&Pinehook.flag(b,"ninaEnvelope"),"the envelope out of the safe and into the lake with her"))
            .thenExecute(()->Pinehook.skip(b,Pinehook.SETTLE))
            .thenWaitUntil(()->h.assertTrue(Pinehook.phase(b)==Pinehook.DAWN&&Pinehook.family(b).equals(Pinehook.NINA_FINISHED),"first light: hers"))
            .thenExecute(()->{
                h.assertTrue(!Pinehook.flag(b,"judeDead"),"Jude, dressed, lived");
                Pinehook.talk(c[0],cast(f,"casey"));h.assertTrue(Talk.choose(c[0],"I did.")&&f.own(c[0],P).getBoolean("Admitted"),"who opened the slip, owned up to");Talk.close(c[0]);
                h.assertTrue(WitnessAccount.count(f.data(),c[0].getUUID())==0,"not written until the morning is over");
                Pinehook.skip(b,Pinehook.DAWN_HOLD);})
            .thenWaitUntil(()->h.assertTrue(Pinehook.flag(b,"resolved"),"every account written"))
            .thenExecute(()->{
                for(var p:c)h.assertTrue(f.own(p,P).getBoolean("Completed")&&WitnessAccount.count(f.data(),p.getUUID())==1,"a Witness source for "+p.getGameProfile().getName());
                h.assertTrue(LiteraryVignettes.personal(f.data(),absent,P).getString("OwedFamily").equals(Pinehook.NINA_FINISHED),"a reader not online is owed theirs");
                h.assertTrue(!Pinehook.dealable(f.data(),c[0].getUUID()),"not dealt again to one who has finished it");
                var zero=account(f,c[0]);h.assertTrue(zero.contains("opened the slip for her")&&zero.contains("Len didn't come out")&&zero.contains("told him it was me")&&zero.contains("Nobody went back"),"the athlete's weekend: "+zero);
                var one=account(f,c[1]);h.assertTrue(one.contains("held a dressing")&&one.contains("Len didn't come out")&&!one.contains("Nobody helped her"),"the charmer's: "+one);})
            .thenSucceed();
    }

    /** 0.4.367: a watched shed; the envelope out of the safe by the slip and the hook, read, and told to Casey, so it is out in the open;
     * Nina held off at the cleat and talked out of her knife by the one who knows; first light, both exposed. */
    @GameTest(template="empty",batch="pinehook_exposed",timeoutTicks=9000)
    public static void theEnvelopeInTheOpenExposesBoth(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);f.build(P);
        ServerPlayer[] c=new ServerPlayer[2];
        toNight(toShed(parts(f,h,c,"px","The Skeptic","The Responsible One"),f,h)
            .thenExecute(()->{f.at(c[0],P,-16.5,0,-41.5);Pinehook.skip(b,Pinehook.NINA_AFTER);}),f,h,Pinehook.PREVENTED)
            .thenExecute(()->{
                f.at(c[1],P,7.5,0,-51.5);h.assertTrue(use(f,c[1],PinehookSite.RELEASE)&&Pinehook.flag(b,"slip"),"the slip open");
                f.at(c[1],P,15.5,0,-52.5);
                h.assertTrue(use(f,c[1],PinehookSite.RACK)&&Talk.choose(c[1],"Take the boat hook")&&!thing(c[1],"hook").isEmpty(),"the boat hook");
                for(int i=0;i<Pinehook.PRY;i++)Pinehook.useThingOn(c[1],thing(c[1],"hook"),b.offset(PinehookSite.SAFE));
                h.assertTrue(use(f,c[1],PinehookSite.SAFE)&&!thing(c[1],"envelope").isEmpty(),"the envelope");
                h.assertTrue(Pinehook.useThing(c[1],thing(c[1],"envelope"),null)&&f.own(c[1],P).getBoolean("ReadEnvelope"),"read");Talk.close(c[1]);
                f.at(c[1],P,7.5,0,-51.5);h.assertTrue(use(f,c[1],PinehookSite.RELEASE)&&!Pinehook.flag(b,"slip"),"the slip made fast again");
                f.at(c[1],P,6.5,0,-29.5);Pinehook.talk(c[1],cast(f,"casey"));h.assertTrue(Talk.choose(c[1],"Len has been taking")&&Pinehook.flag(b,"open")&&!Pinehook.flag(b,"turned"),"told to Casey: out in the open");Talk.close(c[1]);
                f.at(c[0],P,7.5,0,-51.5);f.at(c[1],P,6.5,0,-50.5);
                Pinehook.skip(b,Pinehook.CRISIS_AT);})
            .thenWaitUntil(()->h.assertTrue(Pinehook.phase(b)==Pinehook.CRISIS,"the crisis"))
            .thenWaitUntil(()->h.assertTrue(Pinehook.ninaCrisis(b)==Pinehook.N_CLEAT&&f.own(c[0],P).getBoolean("GuardedCleat"),"Nina at the cleat, held off"))
            .thenExecute(()->{
                Pinehook.talk(c[1],cast(f,"nina"));h.assertTrue(Talk.choose(c[1],"Everyone knows what's in it")&&Pinehook.flag(b,"disarmed")&&f.own(c[1],P).getBoolean("DisarmedNina"),"the knife given up to the one who knows");Talk.close(c[1]);
                Pinehook.skip(b,Pinehook.SETTLE);})
            .thenWaitUntil(()->h.assertTrue(Pinehook.phase(b)==Pinehook.DAWN&&Pinehook.family(b).equals(Pinehook.EXPOSED),"first light: both exposed"))
            .thenExecute(()->Pinehook.skip(b,Pinehook.DAWN_HOLD))
            .thenWaitUntil(()->h.assertTrue(Pinehook.flag(b,"resolved"),"every account written"))
            .thenExecute(()->{
                var one=account(f,c[1]);h.assertTrue(one.contains("everyone knew what was in the envelope")&&one.contains("out where anyone could read it")&&one.contains("without her knife"),"the responsible one's: "+one);
                var zero=account(f,c[0]);h.assertTrue(zero.contains("stood by the cleat")&&zero.contains("stayed with Jude while he changed"),"the skeptic's: "+zero);
                for(var p:c)h.assertTrue(WitnessAccount.count(f.data(),p.getUUID())==1,"a Witness source for "+p.getGameProfile().getName());})
            .thenSucceed();
    }

    /** 0.4.367: Jude cut and never dressed, cold through the night; Nina held at the cleat by two; Casey, never told, sinking the costume;
     * first light with Jude dead, and Len's and Nina's story the mask: challenged by one who saw the prints, gone along with by the other. */
    @GameTest(template="empty",batch="pinehook_performance",timeoutTicks=9000)
    public static void whenJudeDiesTheMaskBecomesTheStory(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);f.build(P);
        ServerPlayer[] c=new ServerPlayer[2];
        toNight(toShed(parts(f,h,c,"pq","The Wildcard","The Horror Fan"),f,h),f,h,Pinehook.WOUNDED)
            .thenExecute(()->{
                f.at(c[0],P,5.5,0,-47.5);h.assertTrue(use(f,c[0],PinehookSite.WET_PRINTS.get(0))&&f.own(c[0],P).getBoolean("SawWetPrints"),"wet prints from the water");
                f.at(c[1],P,6.5,0,-30.5);Pinehook.skip(b,Pinehook.JUDE_COLD_AT[0]);})
            .thenWaitUntil(()->h.assertTrue(f.own(c[1],P).getBoolean("HeardJudeCold"),"Jude, never dressed, getting cold"))
            .thenExecute(()->{f.at(c[0],P,7.5,0,-51.5);f.at(c[1],P,6.5,0,-51.5);Pinehook.skip(b,Pinehook.CRISIS_AT);})
            .thenWaitUntil(()->h.assertTrue(Pinehook.phase(b)==Pinehook.CRISIS,"the crisis"))
            .thenWaitUntil(()->h.assertTrue(Pinehook.ninaCrisis(b)==Pinehook.N_CLEAT,"Nina at the cleat"))
            .thenExecute(()->{Pinehook.talk(c[1],cast(f,"nina"));h.assertTrue(Talk.choose(c[1],"Give me the knife")&&!Pinehook.flag(b,"disarmed"),"not while Len still has it");Talk.close(c[1]);
                Pinehook.talk(c[0],cast(f,"nina"));h.assertTrue(Talk.choose(c[0],"Get hold of her")&&Pinehook.flag(b,"held")&&f.own(c[0],P).getBoolean("HeldNina")&&f.own(c[1],P).getBoolean("HeldNina"),"two of them: held");Talk.close(c[0]);})
            .thenWaitUntil(()->h.assertTrue(Pinehook.flag(b,"sunk"),"Casey down at the rowboat, the costume over the side"))
            .thenExecute(()->Pinehook.skip(b,Pinehook.SETTLE))
            .thenWaitUntil(()->h.assertTrue(Pinehook.phase(b)==Pinehook.DAWN&&Pinehook.flag(b,"judeDead")&&Pinehook.family(b).equals(Pinehook.PERFORMANCE),"first light: Jude dead, and the mask the story"))
            .thenExecute(()->{
                Pinehook.talk(c[0],cast(f,"len"));h.assertTrue(Talk.choose(c[0],"That isn't what happened")&&f.own(c[0],P).getBoolean("ChallengedStory"),"challenged with what was seen");Talk.close(c[0]);
                Pinehook.talk(c[1],cast(f,"nina"));h.assertTrue(Talk.choose(c[1],"That's what happened")&&f.own(c[1],P).getBoolean("ToldTheStory"),"gone along with");Talk.close(c[1]);
                Pinehook.skip(b,Pinehook.DAWN_HOLD);})
            .thenWaitUntil(()->h.assertTrue(Pinehook.flag(b,"resolved"),"every account written"))
            .thenExecute(()->{
                var zero=account(f,c[0]);h.assertTrue(zero.contains("wasn't breathing")&&zero.contains("the story was the mask")&&zero.contains("I said it wasn't true")&&zero.contains("got hold of her"),"the wildcard's: "+zero);
                var one=account(f,c[1]);h.assertTrue(one.contains("I said it too")&&one.contains("got hold of her"),"the horror fan's: "+one);})
            .thenSucceed();
    }

    /** 0.4.367: one reader. The woodshed paddle in the rowboat and Casey rowing Jude across to the store; the envelope out of the safe and
     * read; Nina in by the open slip, Len out of his unbraced door and running; the safe empty, so she hunts the cabin; Len given his
     * envelope back; Nina giving up; first light, and Len telling it his way. */
    @GameTest(template="empty",batch="pinehook_protected",timeoutTicks=9000)
    public static void lenRunsAndTellsItHisWay(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);f.build(P);
        ServerPlayer[] c=new ServerPlayer[1];
        toNight(toShed(parts(f,h,c,"pl","The Athlete"),f,h),f,h,Pinehook.WOUNDED)
            .thenExecute(()->{
                f.at(c[0],P,1.5,0,-1.5);h.assertTrue(use(f,c[0],PinehookSite.PADDLE)&&!thing(c[0],"paddle").isEmpty(),"the paddle from the woodshed");
                f.at(c[0],P,4.5,0,-62.5);h.assertTrue(Pinehook.useThingOn(c[0],thing(c[0],"paddle"),b.offset(PinehookSite.STERN_WEST))&&Pinehook.boatReady(b)==2,"the rowboat ready, slowly");
                f.at(c[0],P,6.5,0,-30.5);
                Pinehook.talk(c[0],cast(f,"jude"));h.assertTrue(Talk.choose(c[0],"Casey. Let him"),"let him talk");Talk.close(c[0]);
                Pinehook.talk(c[0],cast(f,"jude"));h.assertTrue(Talk.choose(c[0],"The boat's ready")&&f.own(c[0],P).getBoolean("SentJudeAcross"),"Casey to row him across");Talk.close(c[0]);})
            .thenWaitUntil(()->h.assertTrue(Pinehook.flag(b,"judeAcross")&&Pinehook.flag(b,"caseyGone"),"Jude and Casey across the lake"))
            .thenExecute(()->{
                f.at(c[0],P,7.5,0,-51.5);h.assertTrue(use(f,c[0],PinehookSite.RELEASE)&&Pinehook.flag(b,"slip"),"the slip open");
                f.at(c[0],P,15.5,0,-52.5);
                h.assertTrue(use(f,c[0],PinehookSite.RACK)&&Talk.choose(c[0],"Take the boat hook"),"the boat hook");
                for(int i=0;i<Pinehook.PRY;i++)Pinehook.useThingOn(c[0],thing(c[0],"hook"),b.offset(PinehookSite.SAFE));
                h.assertTrue(use(f,c[0],PinehookSite.SAFE)&&!thing(c[0],"envelope").isEmpty(),"the envelope");
                h.assertTrue(Pinehook.useThing(c[0],thing(c[0],"envelope"),null)&&f.own(c[0],P).getBoolean("ReadEnvelope"),"read");Talk.close(c[0]);
                f.at(c[0],P,1.5,0,-23.5);Pinehook.skip(b,Pinehook.CRISIS_AT);})
            .thenWaitUntil(()->h.assertTrue(Pinehook.phase(b)==Pinehook.CRISIS,"the crisis"))
            .thenWaitUntil(()->h.assertTrue(Pinehook.flag(b,"lenFled"),"Nina in by the open slip, and Len out of his door and running"))
            .thenWaitUntil(()->h.assertTrue(Pinehook.ninaCrisis(b)==Pinehook.N_HUNT,"the safe empty: she comes up to the cabin"))
            .thenWaitUntil(()->h.assertTrue(near(f,"len",new Vec3(2.5,0,-24.5),1.5),"Len up in the cabin"))
            .thenExecute(()->{Pinehook.talk(c[0],cast(f,"len"));h.assertTrue(Talk.choose(c[0],"Give him his envelope back")&&Pinehook.flag(b,"envelopeBack")&&f.own(c[0],P).getBoolean("GaveLenEnvelope"),"his envelope, back");Talk.close(c[0]);
                Pinehook.skip(b,Pinehook.STANDOFF_MAX);})
            .thenWaitUntil(()->h.assertTrue(Pinehook.flag(b,"gaveUp"),"she gives up"))
            .thenExecute(()->Pinehook.skip(b,Pinehook.SETTLE))
            .thenWaitUntil(()->h.assertTrue(Pinehook.phase(b)==Pinehook.DAWN&&Pinehook.family(b).equals(Pinehook.PROTECTED)&&!Pinehook.flag(b,"judeDead"),"first light: Len's account, and Jude alive across the lake"))
            .thenExecute(()->Pinehook.skip(b,Pinehook.DAWN_HOLD))
            .thenWaitUntil(()->h.assertTrue(Pinehook.flag(b,"resolved"),"written"))
            .thenExecute(()->{var acc=account(f,c[0]);h.assertTrue(acc.contains("Casey rowed Jude across")&&acc.contains("gave Len his envelope back")&&acc.contains("told it his way")&&acc.contains("let him tell it"),"the athlete's: "+acc);})
            .thenSucceed();
    }
}
