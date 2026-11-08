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
@GameTestHolder(TheOldestHouse.MOD_ID+"_literary") @PrefixGameTestTemplate(false)
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

    @GameTest(template="empty",batch="pinehook",timeoutTicks=8000)
    public static void theWeekendOpensAndThePrankGoesWrong(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);
        ServerPlayer[] c=new ServerPlayer[2];
        h.startSequence().thenExecute(()->f.chunks.hold(l,box(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the cabin is resident"))
            .thenIdle(20)
            .thenExecute(()->{
                h.assertTrue(LiteraryRooms.isLiterary(P)&&LiteraryRooms.outside(P)&&P.isOneShot()&&P.authored()&&SceneClock.at(f.origin,b.offset(0,0,-30),io.github.knaitoe.theoldesthouse.house.HouseDimensions.OUTSIDE)==3,"a literary cabin outside, one visit, built whole, at dusk");
                h.assertTrue(WitnessAccount.Story.of(P.id())==null&&!Pinehook.dealable(f.data(),UUID.randomUUID()),"no Witness source yet, and not dealt until the weekend can be finished");
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
                f.at(c[0],P,4.5,0,-36.5);})
            .thenWaitUntil(()->h.assertTrue(Pinehook.phase(b)==Pinehook.HOLD,"back at the cabin, Jude naming her; the night holds"))
            .thenExecute(()->h.assertTrue(f.own(c[0],P).getBoolean("HeardTurn")&&!Pinehook.flag(b,"figure"),"it was Nina, with a knife; nobody on the dock"))
            .thenSucceed();
    }
}
