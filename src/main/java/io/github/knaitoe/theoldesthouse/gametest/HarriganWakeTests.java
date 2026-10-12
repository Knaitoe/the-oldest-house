package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID+"_wake") @PrefixGameTestTemplate(false)
public final class HarriganWakeTests {
    private static LiteraryTests.Fixture active;
    @AfterBatch(batch="harrigan_wake0488") public static void done(ServerLevel ignored){if(active!=null){active.close();active=null;}}
    @GameTest(template="empty",batch="harrigan_wake0488",timeoutTicks=280)
    public static void theSecondVisitOpensOntoTheSameBodyAndThreePersistentMournersWithoutGrantingAnEnding(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var place=LabyrinthPlace.HARRIGAN;var b=f.base(place);
        f.built.add(place);f.chunks.hold(f.in,HarriganWake.bounds(b).inflate(2));
        HarriganVignette.build(f.in.getServer(),f.in,b);LabyrinthBuilder.registerDoors(f.data(),place,b);
        var player=f.player("wake_reader",place);player.setGameMode(GameType.ADVENTURE);HarriganVignette.onArrive(player,place);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready()&&f.data().state(HarriganVignette.ID).hasUUID("OriginalBody0488"),"the original study body is present"))
            .thenExecute(()->{
                var state=f.data().state(HarriganVignette.ID);var identity=state.getUUID("OriginalBody0488");
                h.assertTrue(f.in.getEntity(identity) instanceof ArmorStand body&&HarriganAppearance.variant(body)==1,"the first visit has the living original");
                // The already-read first visit is an explicit fixture prerequisite.
                state.putBoolean("BeatDone",true);f.data().setState(HarriganVignette.ID,state);HarriganVignette.onDepart(player);
                player.moveTo(b.getX()+.5,b.getY(),b.getZ()+5.5);
            }).thenIdle(12).thenExecute(()->{
                player.moveTo(b.getX()+.5,b.getY(),b.getZ()-3.5);HarriganVignette.onArrive(player,place);
                var state=f.data().state(HarriganVignette.ID);var identity=state.getUUID("OriginalBody0488");
                h.assertTrue(state.getInt("Visit")==2&&f.in.getEntity(identity) instanceof ArmorStand body&&HarriganAppearance.variant(body)==2,"a genuine departure and second arrival make the same saved body deceased");
                player.moveTo(b.getX()+.5,b.getY(),b.getZ()-11.5);var door=b.offset(HarriganVignette.FUNERAL_DOOR);
                player.lookAt(EntityAnchorArgument.Anchor.EYES,door.getCenter());
                var used=player.gameMode.useItemOn(player,f.in,player.getMainHandItem(),InteractionHand.MAIN_HAND,new BlockHitResult(door.getCenter().add(0,0,.5),Direction.SOUTH,door,false));
                h.assertTrue(used.consumesAction()&&f.in.getBlockState(door).getValue(DoorBlock.OPEN),"the real Adventure funeral-door interaction opens the doorway");
                state=f.data().state(HarriganVignette.ID);
                h.assertTrue(state.getBoolean("FuneralEntered")&&f.in.getEntity(identity) instanceof ArmorStand body&&HarriganAppearance.variant(body)==3&&body.getZ()==b.getZ()+HarriganVignette.CASKET.getZ()+.5,"the first view beyond the opened door has the same actual corpse in its casket");
                h.assertTrue(f.in.getEntitiesOfClass(ArmorStand.class,HarriganWake.bounds(b),a->a.getTags().contains(HarriganWake.TAG)).size()==3,"three distinct native mourners occupy the wake");
                var saved=state.copy();f.reload();HarriganWake.ensureCast(f.in,b);
                state=f.data().state(HarriganVignette.ID);for(int role=0;role<3;role++)h.assertTrue(state.getUUID("Mourner0488_"+role).equals(saved.getUUID("Mourner0488_"+role)),"reloading retains the original mourner "+role);
                h.assertTrue(!WitnessAccount.has(f.data(),player.getUUID(),WitnessAccount.Story.HARRIGAN),"opening and observing a wake does not replace the personal kept/buried-phone choice");
            }).thenSucceed();
    }
}
