package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.UUID;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

/** Real native ticks, sight lines and saved snapshots; no same-tick draining of an animation. */
@GameTestHolder(TheOldestHouse.MOD_ID+"_literary") @PrefixGameTestTemplate(false)
public final class VignetteAnimationTests {
    private static LiteraryTests.Fixture active;
    private static LiteraryTests.Fixture fixture(GameTestHelper h){return active=new LiteraryTests.Fixture(h);}
    private static void close(){if(active!=null){active.close();active=null;}}

    @GameTest(template="empty",batch="literary_bly_motion",timeoutTicks=1900)
    public static void originalLadyWalksEveryTickSinksGraduallyAndKeepsThePersonalClosetGate(GameTestHelper h){
        var f=fixture(h);var place=LabyrinthPlace.BLY_ROUTE;var p=f.player("bly_motion",place);var peer=f.player("bly_observer",place);
        f.source(p,place);f.at(p,place,-8,0,-9);f.look(p,f.base(place).offset(-11,1,-9));f.at(peer,place,10,0,-3);
        final UUID[] id={null};final Vec3[] previous={null};final long[] cycle={-1};final int[] strides={0},sinks={0};final boolean[] checked={false};
        h.onEachTick(()->{
            var state=LiteraryVignettes.shared(f.data(),place);if(!state.hasUUID("Lady"))return;
            var lady=(LiteraryActor)f.in.getEntity(state.getUUID("Lady"));if(lady==null)return;
            if(id[0]==null)id[0]=lady.getUUID();h.assertTrue(lady.getUUID().equals(id[0]),"the actual Lady retains her original UUID throughout both route and lake");
            long now=f.in.getGameTime();int clock=(int)Math.floorMod(now,900);
            if(cycle[0]<0&&clock>=100&&clock<=105){cycle[0]=Math.floorDiv(now,900);previous[0]=lady.position();}
            if(cycle[0]!=Math.floorDiv(now,900)||previous[0]==null)return;
            Vec3 step=lady.position().subtract(previous[0]);previous[0]=lady.position();
            if(clock>105&&clock<700){h.assertTrue(step.horizontalDistance()<=.061,"native Lady strides never jump a five-tick distance: "+step);if(step.horizontalDistance()>.04)strides[0]++;}
            if(clock>741&&clock<790){h.assertTrue(Math.abs(step.y)<=.081,"the original sinks through native water in small continuous steps: "+step);if(step.y<-.05)sinks[0]++;}
            if(clock==760){f.at(p,place,3,0,-37);h.assertTrue(!f.own(p,place).getBoolean("Ready"),"leaving the closet before the actual body is under the lake does not resolve it");}
            if(clock>=805&&!checked[0]){checked[0]=true;h.assertTrue(strides[0]>550&&sinks[0]>40,"the route and sinking advance on real native ticks, not five-tick snaps");h.assertTrue(lady.getY()<=f.base(place).getY()-3.95&&f.own(p,place).getInt("RefugeTicks")>=200&&f.own(p,place).getBoolean("Ready"),"the actual closed-closet wait and submerged original open the reader's account");h.assertTrue(!f.own(peer,place).getBoolean("Ready"),"sharing the original route grants no observer a refuge or outcome");f.reload();h.assertTrue(f.own(p,place).getBoolean("Ready")&&LiteraryVignettes.shared(f.data(),place).getUUID("Lady").equals(id[0]),"native SavedData reload keeps both original identity and personal outcome");h.succeed();}
        });
    }
    @AfterBatch(batch="literary_bly_motion") public static void blyDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="literary_mapping_motion",timeoutTicks=260)
    public static void privateMappingSilhouetteUsesContinuousNativeCollisionBetweenScriptStops(GameTestHelper h){
        var f=fixture(h);var place=LabyrinthPlace.MAPPING_INTERIOR;var p=f.player("mapping_motion",place);f.at(p,place,3,0,-26);
        final LiteraryActor[] actor={null};final UUID[] id={null};final Vec3[] previous={null};final int[] ticks={0},steps={0};
        h.onEachTick(()->{int tick=++ticks[0];if(actor[0]==null){var world=LiteraryVignettes.shared(f.data(),place);String key="Father_"+p.getUUID();if(world.hasUUID(key)&&f.level(place).getEntity(world.getUUID(key)) instanceof LiteraryActor a){actor[0]=a;id[0]=a.getUUID();previous[0]=a.position();}return;}
            Vec3 at=actor[0].position(),step=at.subtract(previous[0]);previous[0]=at;
            if(tick>100&&tick<190){h.assertTrue(step.horizontalDistance()<=.025,"mapping movement is bounded on each native tick: "+step);if(step.horizontalDistance()>.015)steps[0]++;}
            h.assertTrue(actor[0].getUUID().equals(id[0])&&actor[0].owner().orElseThrow().equals(p.getUUID()),"the walking silhouette stays the same privately tracked original");
        });
        h.runAfterDelay(230,()->{h.assertTrue(steps[0]>=65,"the original moves between decisions instead of resting four ticks then jumping: "+steps[0]);h.assertTrue(!f.own(p,place).getBoolean("Ready"),"watching continuous movement does not bypass the crawl, choice and return");h.succeed();});
    }
    @AfterBatch(batch="literary_mapping_motion") public static void mappingDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="literary_movie_observation",timeoutTicks=480)
    public static void nativeFireworksCountOnlyThePassengersActualUnobstructedView(GameTestHelper h){
        var f=fixture(h);var place=LabyrinthPlace.MOVIE_NIGHT;var p=f.player("movie_looks",place);var peer=f.player("movie_peer",place);f.at(peer,place,20,0,-3);f.at(p,place,.5,0,-14);
        final LakeCanoeEntity[] boat={null};final LakeWitchEntity[] witch={null};
        h.runAfterDelay(10,()->{var world=LiteraryVignettes.shared(f.data(),place);boat[0]=(LakeCanoeEntity)f.out.getEntity(world.getUUID("Canoe"));witch[0]=(LakeWitchEntity)f.out.getEntity(world.getUUID("Witch"));p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);h.assertTrue(boat[0]!=null&&witch[0]!=null&&LiteraryVignettes.boardMovie(p,boat[0]),"the real passenger boards the original canoe beside the native hunt body");});
        h.onEachTick(()->{if(witch[0]==null)return;p.setHealth(20);int present=f.own(p,place).getInt("Present");Vec3 point=witch[0].getEyePosition();if(present<170||present>=320)point=p.getEyePosition().scale(2).subtract(point);p.lookAt(EntityAnchorArgument.Anchor.EYES,point);});
        h.runAfterDelay(160,()->h.assertTrue(f.own(p,place).getInt("Flashes")==0,"an actual native firework behind the passenger grants no observed exposure"));
        h.runAfterDelay(300,()->h.assertTrue(f.own(p,place).getInt("Flashes")==1,"turning toward the original body's real clear sight line records the next exposure"));
        h.runAfterDelay(450,()->{h.assertTrue(f.own(p,place).getInt("Flashes")==1&&!f.own(p,place).getBoolean("Ready")&&!f.own(peer,place).getBoolean("Ready"),"later off-camera flashes, standing peers and no genuine rowing cannot manufacture the far-pier outcome");var body=witch[0].getUUID();f.reload();h.assertTrue(f.own(p,place).getInt("Flashes")==1&&LiteraryVignettes.shared(f.data(),place).getUUID("Witch").equals(body),"the actual saved exposure and original body survive reload");h.succeed();});
    }
    @AfterBatch(batch="literary_movie_observation") public static void movieDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="literary_miniature_cold",timeoutTicks=100)
    public static void coldMiniatureSamplingReturnsBeforeAnyNativeBlockReadCanGenerateASection(GameTestHelper h){
        var f=fixture(h);var cold=f.origin.offset(400000,0,400000);
        h.assertTrue(f.in.getChunkSource().getChunkNow(cold.getX()>>4,(cold.getZ()-3)>>4)==null,"the native sample section really starts absent");
        h.assertTrue(MiniatureSamples.capture(f.in,cold)==null,"cold sampling schedules bounded asynchronous chunks and defers the entire snapshot");
        h.succeed();
    }
    @AfterBatch(batch="literary_miniature_cold") public static void coldDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="literary_miniature_visits",timeoutTicks=370)
    public static void threeActualMiniatureVisitsKeepExactBoundedSamplesPrivateAcrossReload(GameTestHelper h){
        var f=fixture(h);var place=LabyrinthPlace.MINIATURES;var p=f.player("models_reader",place);var peer=f.player("models_peer",place);f.at(peer,place,9,0,-3);f.build(LabyrinthPlace.HILL_NURSERY);f.data().visit(p.getUUID(),LabyrinthPlace.HILL_NURSERY);f.source(p,place);
        var sampled=f.base(LabyrinthPlace.HILL_NURSERY).offset(-4,0,-3);f.in.setBlock(sampled,Blocks.GOLD_BLOCK.defaultBlockState(),3);
        Runnable look=()->{f.at(p,place,.5,0,-16);f.look(p,f.base(place).offset(0,1,-18));};look.run();
        h.runAfterDelay(100,()->{h.assertTrue(f.own(p,place).getInt("ModelsExamined")==1&&!f.own(p,place).getBoolean("Ready"),"one real present examination is one visit, never a whole account");f.arrive(p,place);look.run();});
        h.runAfterDelay(200,()->{h.assertTrue(f.own(p,place).getInt("ModelsExamined")==2,"the returned reader examines the second native arrangement");f.reload();f.arrive(p,place);look.run();});
        h.runAfterDelay(320,()->{var own=f.own(p,place);var models=own.getList("Models",Tag.TAG_COMPOUND);h.assertTrue(own.getInt("ModelsExamined")==3&&own.getBoolean("Ready")&&models.size()==1,"three actual visits resolve the bounded personal examination");int[] blocks=models.getCompound(0).getIntArray("Blocks");h.assertTrue(blocks.length==256&&blocks[64]==Block.getId(Blocks.GOLD_BLOCK.defaultBlockState()),"the model contains the exact observed native block, not a decorative substitute");h.assertTrue(f.own(peer,place).getList("Models",Tag.TAG_COMPOUND).isEmpty()&&!f.own(peer,place).getBoolean("Ready"),"the peer sees and earns no borrowed miniature history");f.read(p,place,LiteraryRooms.ending(place));h.assertTrue(WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.of(place.id()))&&!WitnessAccount.has(f.data(),peer.getUUID(),WitnessAccount.Story.of(place.id())),"only the reader who examined three visits and read their original account receives this source");h.succeed();});
    }
    @AfterBatch(batch="literary_miniature_visits") public static void miniatureDone(ServerLevel l){close();}
}
