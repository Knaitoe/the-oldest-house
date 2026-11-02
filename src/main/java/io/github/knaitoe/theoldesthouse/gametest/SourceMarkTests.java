package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.273 (owner: "Vignettes that requires literature to be read at start need to have literature highlighted in some form to
 * prompt players to engage"). Two readers in the Hill nursery are each told where its first paper lies; reading it puts out the
 * mark for the reader who read it and nobody else. A spectator is told nothing, and the uncanny tunnel, where reading is optional,
 * marks nothing.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_literary") @PrefixGameTestTemplate(false)
public final class SourceMarkTests {
    private SourceMarkTests(){}
    private static LiteraryTests.Fixture active;
    @GameTest(template="empty",batch="source_mark273",timeoutTicks=900)
    public static void theFirstPaperIsMarkedForEachReaderUntilTheyReadIt(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var place=LabyrinthPlace.HILL_NURSERY;var p=f.player("mark_reader",place);var q=f.player("mark_peer",place);
        SourceMark.clearAll();
        h.startSequence().thenExecute(()->f.chunks.hold(f.level(place),IndianLakeRooms.bounds(f.base(place),place)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the nursery is resident"))
            .thenExecute(()->{
                var paper=f.base(place).offset(LiteraryRooms.source(place));
                h.assertTrue(SourceMark.waits(place)&&!SourceMark.waits(LabyrinthPlace.TUNNEL_PERIPHERY)&&!SourceMark.waits(LabyrinthPlace.BORRASCA),"an ordinary literary scene waits on its paper; an uncanny room and Borrasca do not");
                // Test readers have no client to carry the mod's channel, so what each was last sent is read from the server's own record.
                SourceMark.update(p);SourceMark.update(q);
                h.assertTrue(paper.equals(SourceMark.sent(p))&&paper.equals(SourceMark.wanted(p)),"the reader is told where the unread paper lies: sent "+SourceMark.sent(p)+" paper "+paper+" current "+LiteraryVignettes.current(p)+" at "+p.position());
                h.assertTrue(paper.equals(SourceMark.sent(q)),"so is the other reader, separately: "+SourceMark.sent(q));
                f.source(p,place);p.closeContainer();
                h.assertTrue(f.own(p,place).getBoolean("Read_Source"),"the reader has read the paper");
                SourceMark.update(p);SourceMark.update(q);
                h.assertTrue(SourceMark.sent(p)==null&&SourceMark.wanted(p)==null,"once read, the mark goes out for that reader: "+SourceMark.sent(p));
                h.assertTrue(paper.equals(SourceMark.sent(q)),"and stays for the reader who has not read it: "+SourceMark.sent(q));
                q.setGameMode(GameType.SPECTATOR);SourceMark.update(q);
                h.assertTrue(SourceMark.sent(q)==null&&SourceMark.wanted(q)==null,"a spectator is told nothing");
                q.setGameMode(GameType.SURVIVAL);
            }).thenSucceed();
    }
    @AfterBatch(batch="source_mark273") public static void markDone(ServerLevel l){if(active!=null){active.close();active=null;}SourceMark.clearAll();}
}
