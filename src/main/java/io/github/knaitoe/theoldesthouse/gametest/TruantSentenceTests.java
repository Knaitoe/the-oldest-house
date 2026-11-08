package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseExperience;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.359, Johnny Truant's copying room (docs/TRUANT_SENTENCE_0_4_359.md), the missing sentence, played with readers' hands. For help,
 * the page on his desk is the reader's own account with one line run by the drip; he gives the line back, and the blotter, which took
 * the wet ink off backwards, shows the same words; that help is kept. Turned, he reads the account aloud with his own harm left out, so
 * the fall sounds like the reader's; the harm is kept against him; a friend who reads the page on the desk says the missing line and he
 * stops. With a reader under twenty there, or nothing in the account to leave out, it is a claim.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_exploration") @PrefixGameTestTemplate(false)
public final class TruantSentenceTests {
    private TruantSentenceTests(){}
    private static LiteraryTests.Fixture active;
    static void done(){
        Talk.clearAll();
        if(active!=null){var b=active.base(TruantSentence.PLACE);var r=TruantSentence.PLACE.room();
            for(var a:active.in.getEntitiesOfClass(LiteraryActor.class,new AABB(b.getX()+r.minX(),b.getY()+r.minY(),b.getZ()+r.minZ(),b.getX()+r.maxX()+1,b.getY()+r.maxY()+1,b.getZ()+r.maxZ()+1)))a.discard();
            active.close();active=null;}
        Truant.fresh();Truant.apart();Truant.force(null);TruantSentence.clearAll();
    }
    @AfterBatch(batch="truant_sentence_help") public static void doneHelp(ServerLevel l){done();}
    @AfterBatch(batch="truant_sentence_turned") public static void doneTurned(ServerLevel l){done();}
    private static void hold(LiteraryTests.Fixture f,LabyrinthPlace place){var slot=LabyrinthPlaces.slotBounds(f.origin,place);f.chunks.hold(f.in,new AABB(slot.minX(),slot.minY(),slot.minZ(),slot.maxX()+1,slot.maxY()+1,slot.maxZ()+1));}
    private static void at(ServerPlayer p,ServerLevel l,BlockPos b,double x,double z){p.teleportTo(l,b.getX()+x,b.getY(),b.getZ()+z,180,0);p.setNoGravity(true);p.setDeltaMovement(Vec3.ZERO);}
    private static void deepest(LiteraryTests.Fixture f,ServerPlayer p,int depth){var d=f.data();var exp=HouseExperience.record(d,p.getUUID());exp.putInt("Deepest",depth);HouseExperience.save(d,p.getUUID(),exp);}
    private static void away(ServerPlayer p,LiteraryTests.Fixture f){var b=f.base(LabyrinthPlace.JUNCTION);p.teleportTo(f.in,b.getX()+.5,b.getY(),b.getZ()-3.5,180,0);p.setNoGravity(true);}
    private static void setUp(LiteraryTests.Fixture f){Truant.fresh();Truant.apart();TruantSentence.clearAll();TruantPlaces.build(f.in,f.base(TruantSentence.PLACE),TruantSentence.PLACE);LabyrinthBuilder.registerDoors(f.data(),TruantSentence.PLACE,f.base(TruantSentence.PLACE));}
    /** A veteran's history with him: three meetings, twenty noted at the second, held through his door and steadied, the frozen lake told and answered, a promise made. */
    private static CompoundTag history(){
        var own=new CompoundTag();own.putInt("Meetings",3);own.putInt("Talked",3);own.putInt("Help",Truant.HELP_DOOR|Truant.HELP_STEADY);own.putInt("Revealed",2);own.putBoolean("Twenty",true);own.putInt("TwentyAt",2);own.putBoolean("Named",true);
        var told=new ListTag();told.add(StringTag.valueOf("winter_lake"));own.put("Told",told);var answers=new CompoundTag();answers.putInt("winter_lake",0);own.put("Answers",answers);
        own.putString("Promise","agreed");own.putInt("PromisedAt",2);
        return own;
    }
    private static String said(ServerLevel l){var a=Truant.body(l);return a==null||!a.hasCustomName()?"":a.getCustomName().getString();}
    private static void use(ServerPlayer p,ServerLevel l,BlockPos at){TruantSentence.use(p,at,l.getBlockState(at));}

    @GameTest(template="empty",batch="truant_sentence_help",timeoutTicks=1600)
    public static void johnnyGivesBackTheLostLineAndTheBlotterBearsItOut(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var place=TruantSentence.PLACE;var b=f.base(place);var l=f.in;var server=l.getServer();hold(f,place);hold(f,LabyrinthPlace.JUNCTION);
        var a=NativeTestPlayers.survival(h,"copy_reader");f.players.add(a);
        var page=b.offset(TruantSentence.PAGE_AT);var blotter=b.offset(TruantSentence.BLOTTER_AT);
        String answer=TruantSentence.account(history()).get(3).text();
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the copying room is resident"))
            .thenExecute(()->{
                setUp(f);deepest(f,a,25);Truant.remember(server,a.getUUID(),history());
                h.assertTrue(place.isGray()&&TruantPlaces.is(place)&&!TruantPlaces.dealt(LabyrinthPacing.DEEP_DEPTH-1).contains(place)&&TruantPlaces.dealt(LabyrinthPacing.DEEP_DEPTH).contains(place),"one of his places, dealt only from the deep halls");
                h.assertTrue(TruantFittingBlock.is(l.getBlockState(page),TruantFittingBlock.Kind.PAGE)&&TruantFittingBlock.is(l.getBlockState(blotter),TruantFittingBlock.Kind.BLOTTER)&&l.getBlockState(b.offset(TruantSentence.LANTERN_AT)).is(Blocks.LANTERN)
                    &&l.getBlockState(b.offset(TruantSentence.DRIP_AT)).is(Blocks.POINTED_DRIPSTONE)&&l.getBlockState(b.offset(TruantSentence.DRIP_AT.getX(),4,TruantSentence.DRIP_AT.getZ())).is(Blocks.WATER),"a page and a blotter on the desk, one lantern, and a drip over the page");
                var account=TruantSentence.account(history());
                h.assertTrue(account.size()==5&&account.get(3).role()==TruantSentence.Role.ANSWER&&account.get(3).text().contains(StaircaseProse.place("winter_lake"))&&!answer.contains("\"")&&TruantSentence.lost(account)==3,"the account is theirs, in their words, and the wet takes their own line: "+account);
                Truant.force(Truant.Turn.HELP);at(a,l,b,.5,-13.5);})
            .thenWaitUntil(()->h.assertTrue(Truant.body(l)!=null&&Truant.turn()==Truant.Turn.HELP&&TruantFittingBlock.stage(l.getBlockState(page))==1,"he is at his desk, and the page has a run line: "+Truant.debug(server)))
            .thenExecute(()->{
                use(a,l,page);
                h.assertTrue(Talk.lines(a).stream().anyMatch(s->s.startsWith("you: ~~"))&&Talk.lines(a).stream().noneMatch(s->s.startsWith("you: "+answer)),"the page shows the line run to nothing but its length: "+Talk.lines(a));
                h.assertTrue(said(l).contains(answer)&&Talk.lines(a).stream().anyMatch(s->s.startsWith("Johnny:")&&s.contains(answer)),"and he gives it back, aloud: "+said(l));
                h.assertTrue(Truant.record(server,a.getUUID()).getString("SentenceLost").equals(answer)&&(Truant.record(server,a.getUUID()).getInt("Help")&TruantSentence.HELP_SENTENCE)==0,"which is not yet help: they have not looked");
                use(a,l,blotter);
                h.assertTrue(Talk.lines(a).stream().anyMatch(s->s.contains(new StringBuilder(answer).reverse().toString())),"the blotter has the line backwards: "+Talk.lines(a));
                var own=Truant.record(server,a.getUUID());
                h.assertTrue((own.getInt("Help")&TruantSentence.HELP_SENTENCE)!=0&&own.getInt("SentenceRestoredAt")==own.getInt("Meetings"),"compared with the blotter, his line is the same words, and that help is kept: "+own);
                h.assertTrue(said(l).equals("Same words. I don't change anybody's words."),"he says so");})
            .thenWaitUntil(()->h.assertTrue(TruantFittingBlock.stage(l.getBlockState(page))==0,"the page is whole for them now"))
            .thenExecute(()->{
                use(a,l,page);
                h.assertTrue(Talk.lines(a).stream().anyMatch(s->s.contains(answer))&&Talk.lines(a).stream().noneMatch(s->s.contains("~~~")),"read again, the line is there: "+Talk.lines(a));})
            .thenSucceed();
    }

    @GameTest(template="empty",batch="truant_sentence_turned",timeoutTicks=2400)
    public static void johnnyReadsTheirWordsBackWithHisHarmLeftOut(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var place=TruantSentence.PLACE;var b=f.base(place);var l=f.in;var server=l.getServer();hold(f,place);hold(f,LabyrinthPlace.JUNCTION);
        var a=NativeTestPlayers.survival(h,"misquoted_reader");var c=NativeTestPlayers.survival(h,"misquoted_friend");var e=NativeTestPlayers.survival(h,"misquoted_early");
        f.players.add(a);f.players.add(c);f.players.add(e);
        var page=b.offset(TruantSentence.PAGE_AT);
        String act="He came up behind me on the ledge and pushed.";
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the copying room is resident"))
            .thenExecute(()->{
                setUp(f);deepest(f,a,25);deepest(f,c,25);deepest(f,e,5);away(c,f);away(e,f);
                // Nothing in the account to leave out: a claim.
                Truant.remember(server,a.getUUID(),history());
                h.assertTrue(!TruantSentence.due(history()),"a promise not yet kept gives him nothing to leave out");
                Truant.force(Truant.Turn.SENTENCE);at(a,l,b,.5,-3.5);})
            .thenWaitUntil(()->h.assertTrue(Truant.turn()==Truant.Turn.CLAIM,"so his turn is a claim"))
            .thenExecute(()->{away(a,f);})
            .thenIdle(5)
            .thenExecute(()->{
                var own=history();own.putString("Harm","shove");own.putInt("HarmAt",3);own.putInt("Shoved",1);own.putInt("ShovedAt",3);own.putInt("AfterHarm",0);own.putInt("ForboreAt",3);
                Truant.remember(server,a.getUUID(),own);
                var account=TruantSentence.account(own);int omit=TruantSentence.omitted(account);
                h.assertTrue(TruantSentence.due(own)&&account.get(omit).text().equals(act)&&account.get(omit+1).text().equals("I went over the edge."),"with his shove in it, he has the line to leave out: "+account);
                Truant.fresh();Truant.apart();TruantSentence.clearAll();Truant.force(Truant.Turn.SENTENCE);at(a,l,b,.5,-3.5);at(e,l,b,-.5,-3.5);})
            .thenWaitUntil(()->h.assertTrue(Truant.turn()==Truant.Turn.CLAIM,"with a reader under twenty there, it is still a claim"))
            .thenIdle(20)
            .thenExecute(()->{h.assertTrue(TruantSentence.stage()==0,"and nothing is read out");away(a,f);away(e,f);})
            .thenIdle(5)
            .thenExecute(()->{Truant.fresh();Truant.apart();TruantSentence.clearAll();Truant.force(Truant.Turn.SENTENCE);at(a,l,b,.5,-3.5);at(c,l,b,-.5,-3.5);})
            .thenWaitUntil(()->h.assertTrue(Truant.turn()==Truant.Turn.SENTENCE,"alone with a friend past twenty, he turns: "+Truant.debug(server)))
            .thenIdle(20)
            .thenExecute(()->{h.assertTrue(TruantSentence.stage()==0,"he waits until they are in among the pigeonholes");at(a,l,b,.5,-10.5);})
            .thenWaitUntil(()->h.assertTrue(TruantSentence.stage()==1&&said(l).startsWith("Listen."),"then he reads their account aloud"))
            .thenWaitUntil(()->h.assertTrue(TruantSentence.spoken().contains("\"I went over the edge.\""),"line by line, to the fall"))
            .thenExecute(()->{
                h.assertTrue(TruantSentence.spoken().stream().noneMatch(s->s.contains("pushed"))&&TruantSentence.left().equals(act),"with his push left out, the fall sounds like theirs: "+TruantSentence.spoken());
                var own=Truant.record(server,a.getUUID());
                h.assertTrue(own.getString("Harm").equals("sentence")&&own.getInt("Misquoted")==1&&own.getString("SentenceOmitted").equals(act),"the misquoting is kept against him: "+own);
                h.assertTrue(Truant.record(server,c.getUUID()).getInt("HeardMisquoted")==1,"and the friend heard it");
                // The friend reads the page on the desk: every line is there, and they can say the missing one.
                use(c,l,page);
                h.assertTrue(Talk.lines(c).stream().anyMatch(s->s.contains(act)&&!s.contains("~"))&&Talk.lines(c).stream().anyMatch(s->s.contains("misquoted_reader's account")),"the friend can see the whole account on the desk: "+Talk.lines(c));
                h.assertTrue(Talk.choose(c,"\""+act),"and say the line he left out: "+Talk.replies(c));
                h.assertTrue(TruantSentence.stage()==3&&said(l).equals("I know what it says."),"he stops");
                h.assertTrue(Truant.record(server,c.getUUID()).getInt("Interrupted")==1&&Truant.record(server,a.getUUID()).getInt("Contradicted")==1,"the friend's word against his is kept for both");})
            .thenSucceed();
    }
}
