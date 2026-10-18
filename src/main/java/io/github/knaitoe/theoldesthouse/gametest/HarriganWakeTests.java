package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID+"_wake") @PrefixGameTestTemplate(false)
public final class HarriganWakeTests {
    private static LiteraryTests.Fixture active;
    @AfterBatch(batch="harrigan_wake0488") public static void done(ServerLevel ignored){if(active!=null){active.close();active=null;}}
    @GameTest(template="empty",batch="harrigan_wake0488",timeoutTicks=280)
    public static void theFuneralIsTheThirdVisitWithTheSameBodyAndThreePersistentMournersAndGrantsNoEnding(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var place=LabyrinthPlace.HARRIGAN;var b=f.base(place);
        f.built.add(place);f.chunks.hold(f.in,HarriganWake.bounds(b).inflate(2));
        HarriganVignette.build(f.in.getServer(),f.in,b);LabyrinthBuilder.registerDoors(f.data(),place,b);
        var player=f.player("wake_reader",place);player.setGameMode(GameType.ADVENTURE);HarriganVignette.onArrive(player,place);
        java.util.function.Supplier<LiteraryActor> harrigan=()->{var state=f.data().state(HarriganVignette.ID);return state.hasUUID("Harrigan0505")&&f.in.getEntity(state.getUUID("Harrigan0505")) instanceof LiteraryActor a?a:null;};
        java.util.function.Supplier<Integer> mourners=()->f.in.getEntitiesOfClass(LiteraryActor.class,HarriganWake.bounds(b),a->a.role()==LiteraryActor.HARRIGAN_MOURNER).size();
        java.util.function.Consumer<Boolean> visit=beat->{var state=f.data().state(HarriganVignette.ID);state.putBoolean("BeatDone",beat);f.data().setState(HarriganVignette.ID,state);
            HarriganVignette.onDepart(player);player.moveTo(b.getX()+.5,b.getY(),b.getZ()+5.5);player.moveTo(b.getX()+.5,b.getY(),b.getZ()-3.5);HarriganVignette.onArrive(player,place);};
        h.startSequence().thenWaitUntil(()->{if(f.chunks.ready()&&harrigan.get()==null)HarriganVignette.onArrive(player,place);h.assertTrue(f.chunks.ready()&&harrigan.get()!=null,"the original study actor is present");})
            .thenExecute(()->{
                var identity=harrigan.get().getUUID();
                h.assertTrue(f.data().state(HarriganVignette.ID).getInt("Visit")==1&&harrigan.get().phase()==HarriganVignette.ALIVE_PHASE&&mourners.get()==0,"the first visit has him alive and nobody else");
                // The finished reading is an explicit fixture prerequisite.
                visit.accept(true);
                h.assertTrue(f.data().state(HarriganVignette.ID).getInt("Visit")==2&&harrigan.get().getUUID().equals(identity)&&harrigan.get().phase()==HarriganVignette.DEAD_PHASE,"a genuine departure and second arrival find the same saved body dead in his chair");
                h.assertTrue(!f.in.getBlockState(b.offset(HarriganVignette.FUNERAL_DOOR)).isAir()&&mourners.get()==0,"the second visit is his study alone: no funeral beyond it, no mourners");
                visit.accept(false);
                h.assertTrue(f.data().state(HarriganVignette.ID).getInt("Visit")==2,"until his phone is taken, the study stays the study");
                // Taking his phone is the second visit's beat.
                visit.accept(true);
                var state=f.data().state(HarriganVignette.ID);
                h.assertTrue(state.getInt("Visit")==3&&harrigan.get().getUUID().equals(identity)&&harrigan.get().phase()==HarriganVignette.CASKET_PHASE
                        &&Math.abs(harrigan.get().getZ()-(b.getZ()+HarriganVignette.CASKET.getZ()+.5))<.01,"the third visit is the funeral, and the same body lies in its casket");
                h.assertTrue(f.in.getBlockState(b.offset(HarriganVignette.FUNERAL_DOOR)).isAir()&&mourners.get()==3,"the far doorway stands open onto three mourners");
                var saved=state.copy();f.reload();HarriganWake.ensureCast(f.in,b,true);
                state=f.data().state(HarriganVignette.ID);for(int role=0;role<3;role++)h.assertTrue(state.getUUID("Mourner0505_"+role).equals(saved.getUUID("Mourner0505_"+role)),"reloading retains the original mourner "+role);
                h.assertTrue(mourners.get()==3,"and adds none");
                h.assertTrue(!WitnessAccount.has(f.data(),player.getUUID(),WitnessAccount.Story.HARRIGAN),"attending the funeral does not replace the personal kept/buried-phone choice");
            }).thenSucceed();
    }
}
