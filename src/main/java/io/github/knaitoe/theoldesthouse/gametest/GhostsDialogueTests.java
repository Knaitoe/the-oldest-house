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
        var f=fixture(h);var p=f.player("sisters_reader",GhostsSet.PLACE);var q=f.player("sisters_friend",GhostsSet.PLACE);GhostsSetTests.lead(f,p,q);f.source(p,GhostsSet.PLACE);
        f.chunks.hold(f.in,IndianLakeRooms.bounds(f.base(GhostsSet.PLACE),GhostsSet.PLACE));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready()&&actor(f,p,LiteraryActor.GHOST_MERRY)!=null&&GhostsCrew.castOf(q.getUUID()).equals(p.getUUID()),"the lead's original sisters are resident and the friend is a second camera"))
            .thenExecute(()->{
                var merry=actor(f,p,LiteraryActor.GHOST_MERRY);var id=merry.getUUID();approach(p,merry);
                merry.interact(p,InteractionHand.MAIN_HAND);
                h.assertTrue(f.own(p,GhostsSet.PLACE).getInt("GhostTalk_1_1")==1,"one actual interaction consumes exactly the first spoken line");
                merry.interact(p,InteractionHand.MAIN_HAND);
                h.assertTrue(f.own(p,GhostsSet.PLACE).getInt("GhostTalk_1_1")==2,"the next interaction reaches the second line without skipping it");
                h.assertTrue(f.own(q,GhostsSet.PLACE).getInt("GhostTalk_1_1")==0&&merry.broadcastToPlayer(q),"the friend sees the same sister; the dialogue cursor stays personal");
                var talks=TalkHistory.conversations(p.server,p.getUUID());h.assertTrue(talks.stream().map(CompoundTag.class::cast).anyMatch(t->t.getString("Where").contains("filming day 1")&&t.getList("Lines",10).stream().map(CompoundTag.class::cast).filter(l->l.getString("S").equals("Merry")).count()==2),"the two actual family lines enter the reader's heard history with the filming day");
                var heard=TalkHistory.conversations(p.server,p.getUUID()).stream().map(CompoundTag.class::cast).flatMap(t->t.getList("Lines",10).stream()).map(CompoundTag.class::cast).filter(l->l.getString("S").equals("Merry")).map(l->l.getString("T")).collect(java.util.stream.Collectors.toSet());h.assertTrue(TalkHistory.conversations(q.server,q.getUUID()).stream().map(CompoundTag.class::cast).flatMap(t->t.getList("Lines",10).stream()).map(CompoundTag.class::cast).noneMatch(l->l.getString("S").equals("Merry")&&heard.contains(l.getString("T"))),"the friend never receives the owner's private family conversation; their own heard idle lines may remain");
                f.reload();h.assertTrue(f.in.getEntity(id)==merry&&WitnessAccount.count(f.data(),p.getUUID())==0,"talking and saved-data reload retain the original body without completing the story");
            }).thenSucceed();
    }

    @GameTest(template="empty",batch="ghosts_barry_questions",timeoutTicks=1800)
    public static void barrysProductionChoicesStayOnTheirDayAndStaleRepliesCannotStartATake(GameTestHelper h){
        var f=fixture(h);var p=f.player("barry_choices",GhostsSet.PLACE);f.source(p,GhostsSet.PLACE);var b=f.base(GhostsSet.PLACE);
        f.chunks.hold(f.in,IndianLakeRooms.bounds(b,GhostsSet.PLACE));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready()&&actor(f,p,LiteraryActor.GHOST_PRODUCER)!=null,"the original director is resident"))
            .thenExecute(()->{
                var barry=actor(f,p,LiteraryActor.GHOST_PRODUCER);var original=barry.getUUID();
                for(int day=1;day<=GhostsSet.DAYS;day++){
                    var own=f.own(p,GhostsSet.PLACE);own.putInt("FilmDay",day);own.putInt("ShotClock",0);own.putBoolean("DayDone",false);own.putBoolean("Rolling",false);own.putBoolean("CastPlaced",false);GhostsSet.reader(p,b,own);save(f,p,own);approach(p,barry);
                    barry.interact(p,InteractionHand.MAIN_HAND);
                    h.assertTrue(Talk.lines(p).size()==1&&Talk.replies(p).size()==2,"one short greeting offers only the meaningful start and leave choices on day "+day);
                    int stale=Talk.token(p);Talk.reply(p,stale,1);barry.interact(p,InteractionHand.MAIN_HAND);int live=Talk.token(p);var lines=Talk.lines(p);
                    Talk.reply(p,stale,0);h.assertTrue(Talk.token(p)==live&&Talk.lines(p).equals(lines)&&!f.own(p,GhostsSet.PLACE).getBoolean("Rolling"),"the previous offer cannot start filming or repeat a line");
                    h.assertTrue(!f.own(p,GhostsSet.PLACE).getBoolean("RespectedPrivacy"),"talking cannot stand in for putting the actual camera down");
                    Talk.reply(p,live,0);h.assertTrue(f.own(p,GhostsSet.PLACE).getBoolean("Rolling")&&GhostsSet.objectiveAccepted(f.own(p,GhostsSet.PLACE)),"the actual current reply starts this day's own take");
                }
                h.assertTrue(f.in.getEntity(original)==barry&&WitnessAccount.count(f.data(),p.getUUID())==0,"the choices retain the original director and personal completion requirements");
            }).thenSucceed();
    }
}
