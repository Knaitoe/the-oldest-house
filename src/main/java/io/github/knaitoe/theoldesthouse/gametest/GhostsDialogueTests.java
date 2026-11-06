package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;

/** Real family interactions and single-use conversation replies, with separate readers' original casts. */
@GameTestHolder(TheOldestHouse.MOD_ID+"_ghosts") @PrefixGameTestTemplate(false)
public final class GhostsDialogueTests {
    private static LiteraryTests.Fixture active;
    @AfterBatch(batch="ghosts_dialogue_once") public static void onceDone(ServerLevel l){close();}
    @AfterBatch(batch="ghosts_barry_questions") public static void questionsDone(ServerLevel l){close();}
    private static void close(){Talk.clearAll();if(active!=null){active.close();active=null;}}
    private static LiteraryTests.Fixture fixture(GameTestHelper h){active=new LiteraryTests.Fixture(h);return active;}
    private static LiteraryActor actor(LiteraryTests.Fixture f,ServerPlayer p,int role){return f.in.getEntitiesOfClass(LiteraryActor.class,IndianLakeRooms.bounds(f.base(GhostsSet.PLACE),GhostsSet.PLACE),a->a.role()==role&&a.owner().filter(p.getUUID()::equals).isPresent()).stream().findFirst().orElse(null);}
    private static void save(LiteraryTests.Fixture f,ServerPlayer p,CompoundTag own){LiteraryVignettes.save(f.data(),p.getUUID(),GhostsSet.PLACE,own);}
    private static void approach(ServerPlayer p,LiteraryActor a){p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);p.moveTo(a.position().add(0,0,1.5));p.setDeltaMovement(Vec3.ZERO);}

    @GameTest(template="empty",batch="ghosts_dialogue_once",timeoutTicks=1800)
    public static void eachNativeFamilyClickSaysOneLineAndKeepsTheFriendsCursorAndOriginals(GameTestHelper h){
        var f=fixture(h);var p=f.player("sisters_reader",GhostsSet.PLACE);var q=f.player("sisters_friend",GhostsSet.PLACE);f.source(p,GhostsSet.PLACE);
        f.chunks.hold(f.in,IndianLakeRooms.bounds(f.base(GhostsSet.PLACE),GhostsSet.PLACE));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready()&&actor(f,p,LiteraryActor.GHOST_MERRY)!=null&&actor(f,q,LiteraryActor.GHOST_MERRY)!=null,"both original sisters are resident"))
            .thenExecute(()->{
                var merry=actor(f,p,LiteraryActor.GHOST_MERRY);var id=merry.getUUID();approach(p,merry);
                merry.interact(p,InteractionHand.MAIN_HAND);
                h.assertTrue(f.own(p,GhostsSet.PLACE).getInt("GhostTalk_1_1")==1,"one actual interaction consumes exactly the first spoken line");
                merry.interact(p,InteractionHand.MAIN_HAND);
                h.assertTrue(f.own(p,GhostsSet.PLACE).getInt("GhostTalk_1_1")==0,"the next interaction reaches the second line before wrapping");
                h.assertTrue(f.own(q,GhostsSet.PLACE).getInt("GhostTalk_1_1")==0&&actor(f,q,LiteraryActor.GHOST_MERRY)!=merry,"the friend's original sister and dialogue stay personal");
                f.reload();h.assertTrue(f.in.getEntity(id)==merry&&WitnessAccount.count(f.data(),p.getUUID())==0,"talking and saved-data reload retain the original body without completing the story");
            }).thenSucceed();
    }

    @GameTest(template="empty",batch="ghosts_barry_questions",timeoutTicks=1800)
    public static void barrysQuestionsBelongToTheActualDayAndAStaleReplyCannotRepeatOrStartATake(GameTestHelper h){
        var f=fixture(h);var p=f.player("barry_questions",GhostsSet.PLACE);f.source(p,GhostsSet.PLACE);var b=f.base(GhostsSet.PLACE);
        f.chunks.hold(f.in,IndianLakeRooms.bounds(b,GhostsSet.PLACE));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready()&&actor(f,p,LiteraryActor.GHOST_PRODUCER)!=null,"the original director is resident"))
            .thenExecute(()->{
                var barry=actor(f,p,LiteraryActor.GHOST_PRODUCER);var original=barry.getUUID();var prompts=new HashSet<String>();
                for(int day=1;day<=GhostsSet.DAYS;day++){
                    var own=f.own(p,GhostsSet.PLACE);own.putInt("FilmDay",day);own.putInt("ShotClock",0);own.putBoolean("DayDone",false);own.putBoolean("Rolling",false);own.putBoolean("CastPlaced",false);GhostsSet.reader(p,b,own);save(f,p,own);approach(p,barry);
                    barry.interact(p,InteractionHand.MAIN_HAND);var question=GhostsConversations.question(day);
                    h.assertTrue(prompts.add(question.prompt())&&Talk.replies(p).contains(question.prompt()),"the actual conversation offers a distinct question for visit "+day);
                    int token=Talk.token(p),choice=Talk.replies(p).indexOf(question.prompt());Talk.reply(p,token,choice);var lines=Talk.lines(p);
                    h.assertTrue(lines.stream().filter(line->line.equals("Barry: "+question.answer())).count()==1,"Barry answers the selected day's question once");
                    Talk.reply(p,token,choice);h.assertTrue(Talk.lines(p).equals(lines),"a duplicate network reply cannot repeat the answer or select the next offer");
                    own=f.own(p,GhostsSet.PLACE);h.assertTrue(!own.getBoolean("Rolling")&&!own.getBoolean("RespectedPrivacy")&&!own.getBoolean("Ready"),"asking neither calls the take nor substitutes for actually respecting privacy");Talk.close(p);
                }
                h.assertTrue(f.in.getEntity(original)==barry&&WitnessAccount.count(f.data(),p.getUUID())==0,"all questions leave the original director and personal account requirements intact");
            }).thenSucceed();
    }
}
