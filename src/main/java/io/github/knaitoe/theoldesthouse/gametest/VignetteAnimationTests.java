package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.UUID;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
        f.source(p,place);f.at(p,place,-6,0,-9);f.look(p,f.base(place).offset(-8,1,-9));f.at(peer,place,4,0,-3);
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

    @GameTest(template="empty",batch="literary_bly_hold",timeoutTicks=1400)
    public static void standingInTheLadysWayCostsTheReaderTheirPlace(GameTestHelper h){
        var f=fixture(h);var place=LabyrinthPlace.BLY_ROUTE;var p=f.player("bly_held",place);var b=f.base(place);f.at(p,place,4,0,-3);
        final boolean[] placed={false};final int[] carried={0};
        h.onEachTick(()->{
            var state=LiteraryVignettes.shared(f.data(),place);if(!state.hasUUID("Lady"))return;
            var lady=(LiteraryActor)f.in.getEntity(state.getUUID("Lady"));if(lady==null)return;
            int clock=(int)Math.floorMod(f.in.getGameTime(),900);var own=f.own(p,place);
            if(!placed[0]){if(clock<150||clock>500)return;own.putInt("RefugeTicks",200);own.putBoolean("WaitedInCloset",true);LiteraryVignettes.save(f.data(),p.getUUID(),place,own);p.moveTo(lady.getX(),lady.getY(),lady.getZ()-.6);p.setDeltaMovement(Vec3.ZERO);placed[0]=true;return;}
            if(own.contains("HeldUntil0506")){h.assertTrue(Math.abs(p.getX()-lady.getX())<.2&&p.getZ()<lady.getZ()&&p.getZ()>lady.getZ()-1,"the held reader is carried a pace ahead of her on her route: "+p.position().subtract(lady.position()));carried[0]++;return;}
            if(!own.getBoolean("Dragged"))return;
            h.assertTrue(carried[0]>=20,"she holds the reader long enough to be seen: "+carried[0]);
            h.assertTrue(own.getInt("RefugeTicks")==0&&!own.getBoolean("WaitedInCloset"),"the cupboard the reader waited in no longer counts");
            h.assertTrue(p.getY()<b.getY()-1&&p.getZ()<b.getZ()-40&&p.getZ()>b.getZ()-51&&p.isAlive()&&p.getHealth()>=2,"the reader comes up in the lake room's water, alive: "+p.position().subtract(Vec3.atLowerCornerOf(b)));
            h.assertTrue(!own.getBoolean("Ready"),"being taken is not the wait");
            h.assertTrue(BlyFollows.follows(p),"0.4.189: the one she held, she follows home");h.succeed();
        });
    }
    @AfterBatch(batch="literary_bly_hold") public static void blyHoldDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="literary_mapping_motion",timeoutTicks=900)
    public static void privateMappingSilhouetteUsesContinuousNativeCollisionBetweenScriptStops(GameTestHelper h){
        var f=fixture(h);var place=LabyrinthPlace.MAPPING_INTERIOR;var p=f.player("mapping_motion",place);f.at(p,place,3,0,-26);
        final LiteraryActor[] actor={null};final UUID[] id={null};final Vec3[] previous={null};final int[] ticks={0},steps={0};
        // The clock starts when the silhouette is there: in a full run its section can take a while to load.
        h.onEachTick(()->{if(actor[0]==null){var world=LiteraryVignettes.shared(f.data(),place);String key="Father_"+p.getUUID();if(world.hasUUID(key)&&f.level(place).getEntity(world.getUUID(key)) instanceof LiteraryActor a){actor[0]=a;id[0]=a.getUUID();previous[0]=a.position();ticks[0]=0;}return;}
            int tick=++ticks[0];Vec3 at=actor[0].position(),step=at.subtract(previous[0]);previous[0]=at;
            if(tick>100&&tick<190){h.assertTrue(step.horizontalDistance()<=.025,"mapping movement is bounded on each native tick: "+step);if(step.horizontalDistance()>.015)steps[0]++;}
            h.assertTrue(actor[0].getUUID().equals(id[0])&&actor[0].owner().orElseThrow().equals(p.getUUID()),"the walking silhouette stays the same privately tracked original");
            if(tick==230){h.assertTrue(steps[0]>=65,"the original moves between decisions instead of resting four ticks then jumping: "+steps[0]);h.assertTrue(!f.own(p,place).getBoolean("Ready"),"watching continuous movement does not bypass the crawl, choice and return");h.succeed();}
        });
    }
    @AfterBatch(batch="literary_mapping_motion") public static void mappingDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="literary_movie_observation",timeoutTicks=900)
    public static void burstsFreezeHerAndCountOnlyWhatThePassengerActuallySees(GameTestHelper h){
        var f=fixture(h);var place=LabyrinthPlace.MOVIE_NIGHT;var p=f.player("movie_looks",place);var peer=f.player("movie_peer",place);f.at(peer,place,20,0,-3);f.at(p,place,-2.5,0,-13);
        final LakeCanoeEntity[] boat={null};final int[] bursts={0},seen={0},still={0},frozenFor={0};final Vec3[] frozenAt={null},last={null};final double[] moved={0};
        h.runAfterDelay(10,()->{boat[0]=MovieNight.canoe(f.out,LiteraryVignettes.shared(f.data(),place),0);p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
            h.assertTrue(boat[0]!=null&&MovieNight.board(p,boat[0]),"the real passenger boards an original canoe at the dock");});
        h.onEachTick(()->{if(boat[0]==null)return;var b=f.base(place);var witch=MovieNight.witch(f.out,b);if(witch==null)return;
            p.setPos(boat[0].getX(),b.getY(),boat[0].getZ());p.setHealth(20);
            // Looking straight away from her for the first burst, then at her for the rest.
            Vec3 point=witch.getEyePosition();if(bursts[0]<1)point=p.getEyePosition().scale(2).subtract(point);p.lookAt(EntityAnchorArgument.Anchor.EYES,point);
            int now=f.own(p,place).getInt("Bursts0502");
            if(now>bursts[0]){if(MovieNight.observed(p,witch))seen[0]++;bursts[0]=now;frozenAt[0]=witch.position();frozenFor[0]=0;}
            else if(frozenAt[0]!=null&&frozenFor[0]<15){frozenFor[0]++;h.assertTrue(witch.position().distanceToSqr(frozenAt[0])<1e-6,"she does not move while a burst lights the lake");still[0]++;}
            if(last[0]!=null)moved[0]+=witch.position().distanceTo(last[0]);last[0]=witch.position();
            h.assertTrue(f.own(p,place).getInt("Flashes")==seen[0],"exposures are exactly the bursts that found her in the passenger's clear view: "+f.own(p,place).getInt("Flashes")+" against "+seen[0]);
        });
        h.runAfterDelay(820,()->{h.assertTrue(bursts[0]>=4&&still[0]>=40,"bursts go up from the barge every few seconds and each one holds her: "+bursts[0]+" bursts, "+still[0]+" frozen ticks");
            h.assertTrue(moved[0]>2,"between the bursts, in the dark, she comes on: "+moved[0]);
            h.assertTrue(f.own(peer,place).getInt("Bursts0502")==0&&!f.own(p,place).getBoolean("Ready")&&!f.own(peer,place).getBoolean("Ready"),"a reader on the beach is not out under them, and nobody has reached the pier");
            int flashes=f.own(p,place).getInt("Flashes");var body=LiteraryVignettes.shared(f.data(),place).getUUID("Witch");f.reload();
            h.assertTrue(f.own(p,place).getInt("Flashes")==flashes&&LiteraryVignettes.shared(f.data(),place).getUUID("Witch").equals(body),"the saved exposures and her original body survive reload");h.succeed();});
    }
    @AfterBatch(batch="literary_movie_observation") public static void movieDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="literary_movie_beam",timeoutTicks=200)
    public static void theBeamThrowsShadowsTurnsHerBackAndTheSpeakersFallQuietNearHer(GameTestHelper h){
        var f=fixture(h);var place=LabyrinthPlace.MOVIE_NIGHT;var p=f.player("movie_beam",place);var b=f.base(place);Vec3 o=Vec3.atLowerCornerOf(b);
        h.assertTrue(MovieNight.inBeam(b,o.add(.5,1.2,-29)),"the water between the dock and the screen is in the beam");
        h.assertTrue(!MovieNight.inBeam(b,o.add(9.5,1.2,-24))&&!MovieNight.inBeam(b,o.add(.5,1.2,-16))&&!MovieNight.inBeam(b,o.add(.5,1.2,-43)),"off to the side, behind the lens and beyond the screen are not");
        h.assertTrue(f.out.getBlockState(b.offset(MovieNight.SCREEN)).is(MovieNightRegistry.SCREEN.get())&&MoviePropBlock.is(f.out.getBlockState(b.offset(MovieNight.PROJECTOR)),MoviePropBlock.Kind.PROJECTOR),"the screen and the projector stand where the beam runs between them");
        f.at(p,place,.5,0,-27);final LakeWitchEntity[] witch={null};final int[] stage={0};
        h.onEachTick(()->{if(witch[0]!=null&&stage[0]>=1&&stage[0]<=2)p.lookAt(EntityAnchorArgument.Anchor.EYES,witch[0].getEyePosition());});
        h.runAfterDelay(10,()->{h.assertTrue(f.own(p,place).getBoolean("ShadowSeen0502"),"standing in the beam puts the reader's own head on the film");
            witch[0]=MovieNight.witch(f.out,b);h.assertTrue(witch[0]!=null,"her original body is out on the lake");
            // On the raft, watching her out of the beam: she keeps coming.
            f.at(p,place,-6,0,-39.5);witch[0].moveTo(b.getX()-19.5,b.getY()-.108,b.getZ()-30.5,0,0);stage[0]=1;});
        h.runAfterDelay(20,()->{h.assertTrue(witch[0].huntPhase()!=LakeWitchEntity.WITHDRAW,"seen in the dark she does not turn back: "+witch[0].huntDiagnostic());
            witch[0].moveTo(b.getX()+.5,b.getY()-.108,b.getZ()-34.5,0,0);stage[0]=2;});
        h.runAfterDelay(27,()->{h.assertTrue(witch[0].huntPhase()==LakeWitchEntity.WITHDRAW,"caught in the beam she withdraws: "+witch[0].huntDiagnostic());
            h.assertTrue(f.own(p,place).getBoolean("Muted0502")&&f.own(p,place).getBoolean("Quiet0502"),"near her in the dark the speakers go quiet for this reader");
            witch[0].moveTo(b.getX()+22.5,b.getY()-.108,b.getZ()-68,0,0);stage[0]=3;});
        h.runAfterDelay(40,()->{h.assertTrue(!f.own(p,place).getBoolean("Muted0502")&&f.own(p,place).getBoolean("Quiet0502"),"they come back once she is far off, and the night remembers they went quiet");
            // Watching the film while she crosses the beam: her shadow goes up on it.
            f.at(p,place,.5,0,-22);witch[0].moveTo(b.getX()+3,b.getY()-.108,b.getZ()-33,0,0);p.lookAt(EntityAnchorArgument.Anchor.EYES,o.add(.5,3,MovieNight.FILM_Z));stage[0]=4;});
        h.runAfterDelay(56,()->{h.assertTrue(f.own(p,place).getBoolean("WitchShadow0502"),"her shadow crosses the film the reader is watching");h.succeed();});
    }
    @AfterBatch(batch="literary_movie_beam") public static void movieBeamDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="literary_movie_shark",timeoutTicks=420)
    public static void theSharkComesOnlyToItsReaderAndCannotTouchAnyone(GameTestHelper h){
        var f=fixture(h);var place=LabyrinthPlace.MOVIE_NIGHT;var p=f.player("movie_shark",place);var peer=f.player("movie_shore",place);f.at(peer,place,-8,0,-4);f.at(p,place,-2.5,0,-13);
        final LakeCanoeEntity[] boat={null};final LakeSharkEntity[] shark={null};final int[] oldest={0};
        h.runAfterDelay(10,()->{boat[0]=MovieNight.canoe(f.out,LiteraryVignettes.shared(f.data(),place),0);p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);h.assertTrue(boat[0]!=null&&MovieNight.board(p,boat[0]),"the reader takes a canoe");
            var own=f.own(p,place);own.putBoolean("SharkVisit0502",true);own.remove("SharkDone0502");LiteraryVignettes.save(f.data(),p.getUUID(),place,own);});
        h.onEachTick(()->{if(boat[0]==null)return;var b=f.base(place);
            boat[0].moveTo(b.getX()-16,b.getY()-.4,b.getZ()-50,180,0);boat[0].setDeltaMovement(Vec3.ZERO);p.setPos(boat[0].getX(),b.getY(),boat[0].getZ());
            // Keep her far off, so nothing but the shark is near the canoe.
            var witch=MovieNight.witch(f.out,b);if(witch!=null)witch.moveTo(b.getX()+25,b.getY()-.108,b.getZ()-70,0,0);
            var s=LakeSharkEntity.of(p.getUUID());if(s==null)return;if(shark[0]==null)shark[0]=s;
            h.assertTrue(s==shark[0],"one shark, the same one, for the whole of its visit");
            h.assertTrue(s.broadcastToPlayer(p)&&!s.broadcastToPlayer(peer),"only its own reader is ever sent it");
            h.assertTrue(!s.canBeCollidedWith()&&!s.isPickable()&&!s.hurt(f.out.damageSources().generic(),5)&&!s.shouldBeSaved(),"it has no body to touch, takes no harm and is never saved");
            h.assertTrue(p.getHealth()==20&&peer.getHealth()==20,"it hurts nobody");oldest[0]=Math.max(oldest[0],s.age());
            p.lookAt(EntityAnchorArgument.Anchor.EYES,s.position().add(0,1.3,0));
        });
        h.runAfterDelay(400,()->{h.assertTrue(shark[0]!=null&&oldest[0]>=LakeSharkEntity.CIRCLE+LakeSharkEntity.DIVE,"the shark came, circled and went under the canoe: "+oldest[0]);
            h.assertTrue(shark[0].isRemoved()&&LakeSharkEntity.of(p.getUUID())==null&&f.out.getEntitiesOfClass(LakeSharkEntity.class,IndianLakeRooms.bounds(f.base(place),place)).isEmpty(),"then it sank away and is gone");
            h.assertTrue(f.own(p,place).getBoolean("SharkDone0502")&&f.own(p,place).getBoolean("SharkSeen0502")&&!f.own(peer,place).getBoolean("SharkSeen0502"),"it comes once a visit, and only its reader saw it");h.succeed();});
    }
    @AfterBatch(batch="literary_movie_shark") public static void movieSharkDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="literary_movie_pier",timeoutTicks=260)
    public static void herFootingEndsAtTheFarPierAndTheAccountRemembersTheNight(GameTestHelper h){
        var f=fixture(h);var place=LabyrinthPlace.MOVIE_NIGHT;var p=f.player("movie_pier",place);var rower=f.player("movie_alongside",place);var b=f.base(place);
        for(var at:new BlockPos[]{b.offset(0,0,-2),b.offset(-30,4,-30),b.offset(30,0,-50),b.offset(0,-1,MovieNight.PIER_END),b.offset(0,1,-86),b.offset(0,12,-41)})
            h.assertTrue(SceneClock.time(SceneClock.at(f.origin,at,HouseDimensions.OUTSIDE),0,6000)==18000,"the whole of Movie Night, from the entrance to the far pier and the screen's top, keeps the midnight clock: "+at);
        h.assertTrue(MovieNight.footing(Blocks.WATER.defaultBlockState())&&MovieNight.footing(MovieMaterialBlock.of(MovieMaterialBlock.Kind.DOCK))&&MovieNight.footing(MovieMaterialBlock.of(MovieMaterialBlock.Kind.RAFT))
                &&!MovieNight.footing(MovieMaterialBlock.of(MovieMaterialBlock.Kind.PIER))&&!MovieNight.footing(Blocks.SAND.defaultBlockState())&&!MovieNight.footing(Blocks.GRASS_BLOCK.defaultBlockState()),"she keeps to the water and the grey boards out on it; never the far pier's cedar or a bank");
        h.assertTrue(MovieMaterialBlock.is(f.out.getBlockState(b.offset(0,-1,-64)),MovieMaterialBlock.Kind.PIER)&&MovieMaterialBlock.is(f.out.getBlockState(b.offset(0,-1,-10)),MovieMaterialBlock.Kind.DOCK)&&MovieMaterialBlock.is(f.out.getBlockState(b.offset(0,-1,-42)),MovieMaterialBlock.Kind.RAFT)&&f.out.getBlockState(b.offset(2,-1,-64)).is(Blocks.WATER)&&f.out.getBlockState(b.offset(-2,-1,-64)).is(Blocks.WATER),"the far pier runs out over open water");
        // Every account needs its source read first.
        f.source(p,place);f.at(p,place,.5,0,-64);final LakeCanoeEntity[] boat={null};final LakeWitchEntity[] witch={null};
        h.runAfterDelay(10,()->{h.assertTrue(MovieNight.onPier(p,b)&&!MovieNight.exposed(p),"up on the pier the reader is out of her reach");
            boat[0]=MovieNight.canoe(f.out,LiteraryVignettes.shared(f.data(),place),1);f.at(rower,place,3.5,0,-13);rower.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);h.assertTrue(boat[0]!=null&&MovieNight.board(rower,boat[0]),"a second reader takes another canoe");
            witch[0]=MovieNight.witch(f.out,b);h.assertTrue(witch[0]!=null,"her original body is out on the lake");witch[0].moveTo(b.getX()+3.5,b.getY()-.108,b.getZ()-64,0,0);
            var own=f.own(p,place);own.putDouble("RowDistance",40);own.putInt("Flashes",1);own.putBoolean("SharkSeen0502",true);own.putBoolean("ShadowSeen0502",true);LiteraryVignettes.save(f.data(),p.getUUID(),place,own);});
        h.onEachTick(()->{if(boat[0]==null||witch[0]==null)return;
            // The rower sits on the far side of the pier; she must go round its end, never across it.
            boat[0].moveTo(b.getX()-4.5,b.getY()-.4,b.getZ()-66,180,0);boat[0].setDeltaMovement(Vec3.ZERO);rower.setPos(boat[0].getX(),b.getY(),boat[0].getZ());rower.setHealth(20);
            double x=witch[0].getX()-b.getX(),z=witch[0].getZ()-b.getZ(),half=witch[0].getBbWidth()/2;
            h.assertTrue(!(x+half>-1&&x-half<2&&z+half>-72&&z-half<-58),"she never sets foot on the far pier: "+witch[0].position());});
        h.runAfterDelay(40,()->{h.assertTrue(!f.own(p,place).getBoolean("Ready")&&f.own(p,place).getBoolean("PierHint0502"),"the pier alone is not the end: she has to have been seen in two bursts");
            var own=f.own(p,place);own.putInt("Flashes",2);LiteraryVignettes.save(f.data(),p.getUUID(),place,own);});
        h.runAfterDelay(60,()->{h.assertTrue(f.own(p,place).getBoolean("Ready")&&!f.own(rower,place).getBoolean("Ready"),"seen twice and up on the pier, the reader's night is over; the rower's is not");
            var text=LiteraryTexts.ending(p,place,f.own(p,place)).get(net.minecraft.core.component.DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().map(page->page.raw().getString()).reduce("",String::concat);
            h.assertTrue(text.contains("Teeth of Summer")&&text.contains("no sharks in a lake")&&text.contains("my own head went up on the screen")&&text.contains("speakers went quiet")==f.own(p,place).getBoolean("Quiet0502")&&!text.contains("caught me"),"the account tells the night the reader had and no other: "+text);
            f.read(p,place,LiteraryRooms.ending(place));h.assertTrue(WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.of(place.id()))&&!WitnessAccount.has(f.data(),rower.getUUID(),WitnessAccount.Story.of(place.id())),"reading it completes only this reader's scene");h.succeed();});
    }
    @AfterBatch(batch="literary_movie_pier") public static void moviePierDone(ServerLevel l){close();}

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

    private static void winterState(LiteraryTests.Fixture f,java.util.function.Consumer<CompoundTag> change){var w=LiteraryVignettes.shared(f.data(),LabyrinthPlace.WINTER_LAKE);change.accept(w);LiteraryVignettes.shared(f.data(),LabyrinthPlace.WINTER_LAKE,w);}
    private static void winterChunks(LiteraryTests.Fixture f,BlockPos b){
        for(int x=-24;x<=24;x+=16)for(int z=-72;z<=-8;z+=16){var c=new net.minecraft.world.level.ChunkPos(b.offset(x,0,z));if(!FORCED.contains(c)){FORCED.add(c);f.out.setChunkForced(c.x,c.z,true);}}
    }
    private static void unforce(ServerLevel l){var out=HouseTestLevel.get(l.getServer(),io.github.knaitoe.theoldesthouse.house.HouseDimensions.OUTSIDE);for(var c:FORCED)out.setChunkForced(c.x,c.z,false);FORCED.clear();}
    private static int ribbons(ServerPlayer p){return p.getInventory().countItem(WinterLakeRegistry.RIBBON.get());}
    private static ItemStack ribbonOf(ServerPlayer p){for(var stack:p.getInventory().items)if(stack.is(WinterLakeRegistry.RIBBON.get()))return stack;return ItemStack.EMPTY;}

    @GameTest(template="empty",batch="literary_winter_hearing",timeoutTicks=560)
    public static void underTheIceSheGoesToWhatSheHearsAndBreaksThroughUnderANoisyReader(GameTestHelper h){
        var f=fixture(h);var place=LabyrinthPlace.WINTER_LAKE;var p=f.player("winter_walker",place);var peer=f.player("winter_bank",place);var b=f.base(place);winterChunks(f,b);
        f.at(peer,place,-2.5,0,-6.5);f.at(p,place,1.5,0,-4.5);final int[] phase={0};final double[] wobble={.15};
        h.runAfterDelay(15,()->{var witch=WinterLake.witch(f.out);
            h.assertTrue(witch!=null&&witch.driven()&&witch.underIce()&&Math.abs(witch.getY()-(b.getY()+WinterLake.UNDER))<.01,"she is under the ice, hanging from its underside: "+(witch==null?null:witch.position()));
            for(var who:WinterLake.BOYS){var a=WinterLake.cast(f.out,who);h.assertTrue(a!=null&&a.role()==LiteraryActor.STRANGER&&a.phase()==who.phase()&&a.getScale()<.9F&&!a.isInvisible(),"the four boys from the shallows are on the bank, still boys: "+who.name());}
            h.assertTrue(f.out.getBlockState(b.offset(WinterLake.HOLE)).is(Blocks.WATER)&&WinterPropBlock.kind(f.out.getBlockState(b.offset(2,-1,-46)))==WinterPropBlock.Kind.BLACK_ICE
                    &&WinterPropBlock.kind(f.out.getBlockState(b.offset(-26,-1,-40)))==WinterPropBlock.Kind.SNOW_ICE,"the hole never froze; black ice round it, snow drifted over the shore");
            // Hank has stopped the snowball fight; a snowball thudding onto the ice is the loudest thing on the lake.
            winterState(f,w->w.putInt("Night",WinterLake.QUIET_AT+10));
            witch.setPos(b.getX()+10.5,b.getY()+WinterLake.UNDER,b.getZ()-36.5);
            var ball=new net.minecraft.world.entity.projectile.Snowball(f.out,b.getX()+10.5,b.getY()+2,b.getZ()-30.5);ball.setOwner(peer);ball.setDeltaMovement(0,-.6,0);f.out.addFreshEntity(ball);});
        h.runAfterDelay(35,()->{var w=LiteraryVignettes.shared(f.data(),place);
            h.assertTrue(w.getFloat("Heard")>30&&Math.abs(w.getDouble("HeardX")-(b.getX()+10.5))<1&&Math.abs(w.getDouble("HeardZ")-(b.getZ()-30.5))<1&&f.own(peer,place).getBoolean("Lured0504"),"the snowball's thud is heard where it landed, and the thrower drew her: "+w);});
        h.runAfterDelay(110,()->{var witch=WinterLake.witch(f.out);
            h.assertTrue(Math.hypot(witch.getX()-(b.getX()+10.5),witch.getZ()-(b.getZ()-30.5))<1.5&&witch.underIce(),"she goes under the ice to where it landed: "+witch.position());
            f.at(p,place,witch.getX()-b.getX(),0,witch.getZ()-b.getZ());p.setShiftKeyDown(true);phase[0]=1;});
        // Crouched right over her, creeping: no sound, and nothing happens.
        h.onEachTick(()->{if(phase[0]!=1&&phase[0]!=2||f.own(p,place).getBoolean("FellThrough0504"))return;var witch=WinterLake.witch(f.out);
            if(phase[0]==1){f.at(p,place,witch.getX()-b.getX()+wobble[0],0,witch.getZ()-b.getZ());wobble[0]=-wobble[0];return;}
            // Running back and forth over her on black ice.
            f.at(p,place,witch.getX()-b.getX()+wobble[0]*2,0,witch.getZ()-b.getZ());wobble[0]=-wobble[0];});
        h.runAfterDelay(150,()->{h.assertTrue(WinterLake.suspicion(p)==0&&!f.own(p,place).getBoolean("Knocked0504")&&p.getHealth()==20,"crouched and creeping, right over her, the reader makes no sound she can hear");
            p.setShiftKeyDown(false);p.setSprinting(true);phase[0]=2;});
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.own(p,place).getBoolean("FellThrough0504"),"running over her she knocks, cracks the ice and comes through: suspicion="+WinterLake.suspicion(p)))
            .thenExecute(()->{phase[0]=3;var w=LiteraryVignettes.shared(f.data(),place);
                h.assertTrue(f.own(p,place).getBoolean("Knocked0504")&&!w.getList("Changed",Tag.TAG_COMPOUND).isEmpty()&&w.getInt("Rest")>0,"she knocked first, the broken ice is remembered, and she lets go");
                h.assertTrue(p.getY()<b.getY()-1.5&&p.getHealth()<20,"the reader is pulled down under the ice: "+p.position());
                h.assertTrue(peer.getHealth()==20&&!f.own(peer,place).getBoolean("FellThrough0504")&&!f.own(peer,place).getBoolean("Knocked0504"),"nothing happens to the reader on the bank");
                p.setSprinting(false);f.at(p,place,.5,0,-8.5);})
            .thenIdle(12).thenExecute(()->{h.assertTrue(f.own(p,place).getBoolean("CameUp0504"),"climbing out again counts as coming up");h.succeed();});
    }
    @AfterBatch(batch="literary_winter_hearing") public static void winterHearingDone(ServerLevel l){close();unforce(l);}

    @GameTest(template="empty",batch="literary_winter_ribbon",timeoutTicks=360)
    public static void hankGivesTheRibbonAHandTakesItAtTheHoleAndTheBoysRun(GameTestHelper h){
        var f=fixture(h);var place=LabyrinthPlace.WINTER_LAKE;var p=f.player("winter_dare",place);var peer=f.player("winter_other",place);var b=f.base(place);winterChunks(f,b);
        f.at(peer,place,3.5,0,-8.5);
        h.runAfterDelay(15,()->{var hank=WinterLake.cast(f.out,WinterLake.HANK);h.assertTrue(hank!=null,"Hank is on the bank");
            f.at(p,place,-4.5,0,-6.5);LiteraryVignettes.talk(p,hank);LiteraryVignettes.talk(p,hank);
            h.assertTrue(!f.own(p,place).getBoolean("TookDare0504")&&ribbons(p)==0,"he tells it before he gives it");
            LiteraryVignettes.talk(p,hank);
            h.assertTrue(f.own(p,place).getBoolean("TookDare0504")&&ribbons(p)==1&&ribbons(peer)==0,"the third time, Hank gives this reader her ribbon, and only this reader");
            var ribbon=ribbonOf(p);
            h.assertTrue(!WinterLake.giveBack(p,ribbon)&&ribbons(p)==1,"on the bank it is only kept");
            f.at(peer,place,2.5,0,-45.5);h.assertTrue(!WinterLake.giveBack(peer,ribbon)&&ribbons(p)==1,"nobody else can give it back for them");
            f.at(p,place,2.5,0,-45.5);
            h.assertTrue(WinterLake.giveBack(p,ribbon)&&ribbons(p)==0&&f.own(p,place).getBoolean("GaveRibbon0504")&&!f.own(peer,place).getBoolean("GaveRibbon0504"),"held out over the hole, it is taken");
            var w=LiteraryVignettes.shared(f.data(),place);h.assertTrue(w.getInt("Hand")==WinterLake.HAND_TICKS&&w.getBoolean("Fled"),"her hand comes up for it, and the boys run");});
        h.runAfterDelay(20,()->{var witch=WinterLake.witch(f.out);var hole=Vec3.atBottomCenterOf(b.offset(WinterLake.HOLE));
            h.assertTrue(!witch.underIce()&&witch.striking()&&Math.hypot(witch.getX()-hole.x,witch.getZ()-hole.z)<.6&&witch.getY()>b.getY()-1.5,"up in the hole, reaching: "+witch.position());});
        h.runAfterDelay(15+WinterLake.HAND_TICKS+5,()->{var witch=WinterLake.witch(f.out);h.assertTrue(witch.underIce()&&!witch.striking(),"then she is back under the ice");});
        h.runAfterDelay(15+WinterLake.RUN_TICKS+20,()->{
            for(var who:WinterLake.BOYS)h.assertTrue(WinterLake.cast(f.out,who).isInvisible(),"the boys have run into the trees: "+who.name());
            h.assertTrue(!f.out.getBlockState(b.offset(WinterLake.BONFIRE)).getValue(net.minecraft.world.level.block.CampfireBlock.LIT),"they kicked snow over the fire");h.succeed();});
    }
    @AfterBatch(batch="literary_winter_ribbon") public static void winterRibbonDone(ServerLevel l){close();unforce(l);}

    @GameTest(template="empty",batch="literary_winter_bank",timeoutTicks=420)
    public static void walkingBackToTheBankEndsTheDareAndTheNightResets(GameTestHelper h){
        var f=fixture(h);var place=LabyrinthPlace.WINTER_LAKE;var p=f.player("winter_back",place);var peer=f.player("winter_stayed",place);var b=f.base(place);winterChunks(f,b);
        f.source(p,place);f.source(peer,place);var cracked=b.offset(10,-1,-30);
        h.runAfterDelay(15,()->{var hank=WinterLake.cast(f.out,WinterLake.HANK);f.at(p,place,-4.5,0,-6.5);for(int i=0;i<3;i++)LiteraryVignettes.talk(p,hank);
            f.at(p,place,2.5,0,-45.5);h.assertTrue(WinterLake.giveBack(p,ribbonOf(p)),"the ribbon is given back at the hole");
            f.at(peer,place,4.5,0,-30.5);});
        h.runAfterDelay(30,()->{h.assertTrue(!f.own(p,place).getBoolean("Ready"),"out on the ice, the dare is not over");f.at(p,place,.5,0,-8.5);f.at(peer,place,3.5,0,-8.5);});
        h.runAfterDelay(45,()->{h.assertTrue(f.own(p,place).getBoolean("Ready"),"back on the bank with the ribbon given, the night is done");
            h.assertTrue(!f.own(peer,place).getBoolean("Ready"),"a reader who walked out and back without it has not done the dare");
            var text=LiteraryTexts.ending(p,place,f.own(p,place)).get(net.minecraft.core.component.DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().map(page->page.raw().getString()).reduce("",String::concat);
            h.assertTrue(text.contains("A hand came up and took it")&&text.contains("Hank had carried her ribbon")&&!text.contains("The ice opened"),"the account tells this reader's night: "+text);
            f.read(p,place,LiteraryRooms.ending(place));h.assertTrue(WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.of(place.id()))&&!WitnessAccount.has(f.data(),peer.getUUID(),WitnessAccount.Story.of(place.id())),"reading it completes only this reader's scene");
            // A crack she made earlier that night, for the reset to mend.
            winterState(f,w->{var c=new CompoundTag();c.putLong("Pos",cracked.asLong());c.putString("Kind","BLACK_ICE");var list=new net.minecraft.nbt.ListTag();list.add(c);w.put("Changed",list);});
            f.out.setBlock(cracked,WinterPropBlock.of(WinterPropBlock.Kind.SPLIT),3);
            for(var r:java.util.List.of(p,peer)){r.stopRiding();r.teleportTo(f.in,f.origin.getX()+.5,f.origin.getY()+1,f.origin.getZ()+.5,0,0);}});
        h.runAfterDelay(260,()->{var w=LiteraryVignettes.shared(f.data(),place);
            h.assertTrue(!w.getBoolean("Fled")&&!w.contains("Night")&&!w.contains("Changed")&&WinterPropBlock.kind(f.out.getBlockState(cracked))==WinterPropBlock.Kind.BLACK_ICE,"once the lake is empty and unwatched, the night starts over and the ice is whole: "+w);
            for(var who:WinterLake.BOYS){var a=WinterLake.cast(f.out,who);h.assertTrue(!a.isInvisible()&&a.position().distanceTo(who.home(b))<.5,"the boys are back by the fire: "+who.name());}
            h.assertTrue(f.out.getBlockState(b.offset(WinterLake.BONFIRE)).getValue(net.minecraft.world.level.block.CampfireBlock.LIT),"and the fire is lit");
            var witch=WinterLake.witch(f.out);var start=Vec3.atBottomCenterOf(b.offset(WinterLake.WITCH_START));
            h.assertTrue(witch!=null&&witch.underIce()&&Math.hypot(witch.getX()-start.x,witch.getZ()-start.z)<.5,"and she is back under the ice");h.succeed();});
    }
    @AfterBatch(batch="literary_winter_bank") public static void winterBankDone(ServerLevel l){close();unforce(l);}

    @GameTest(template="empty",batch="literary_diary_sequence",timeoutTicks=700)
    public static void eightActualDiaryOriginalsRequireThreeSeparateVisitsAndRetainTheirPrivateWords(GameTestHelper h){
        var f=fixture(h);var place=LabyrinthPlace.DEVILS_ROCK;var p=f.player("diary_reader",place);var peer=f.player("diary_peer",place);var b=f.base(place);f.source(p,place);
        final int[] returns={0};final boolean[] finished={false};final CompoundTag[] firstOriginal={null};
        h.onEachTick(()->{
            if(finished[0])return;f.at(peer,place,.5,0,-3);f.look(peer,b.offset(0,1,5));
            var own=f.own(p,place);int placed=own.getInt("DiaryPlaced"),read=own.getInt("DiaryRead");
            for(int i=0;i<8;i++)if((placed&(1<<i))!=0&&(read&(1<<i))==0){
                h.assertTrue(f.in.getBlockState(b.offset(LiteraryVignettes.diaryPosition(i))).is(LiteraryRegistry.PROP.get()),"a genuine unseen native page appears before it is read");
                f.read(p,place,LiteraryVignettes.diaryPosition(i));if(i==0)firstOriginal[0]=f.own(p,place).getCompound("Original_Diary0").copy();break;
            }
            own=f.own(p,place);int visit=own.getInt("Visit"),target=Math.min(8,visit*3),expected=(1<<target)-1;
            if(own.getInt("DiaryRead")==expected&&own.getInt("Beat")>=visit){
                h.assertTrue(visit==returns[0]+1,"the native arrival sequence advances only one distinct visit at a time");
                if(visit<3){h.assertTrue(!own.getBoolean("Ready")&&Integer.bitCount(own.getInt("DiaryRead"))==visit*3,"one or two visits cannot read all eight original pages");f.reload();f.arrive(p,place);returns[0]++;}
                else{finished[0]=true;h.assertTrue(returns[0]==2&&own.getInt("DiaryRead")==255&&own.getBoolean("Ready")&&firstOriginal[0]!=null,"all eight native page menus and three visits produce the original personal ending");f.reload();h.assertTrue(f.own(p,place).getCompound("Original_Diary0").equals(firstOriginal[0])&&f.own(peer,place).getInt("DiaryRead")==0&&!f.own(peer,place).getBoolean("Ready"),"reload retains exact reader-original words while the actual peer reads none");f.read(p,place,LiteraryRooms.ending(place));h.assertTrue(WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.of(place.id()))&&!WitnessAccount.has(f.data(),peer.getUUID(),WitnessAccount.Story.of(place.id())),"no shared page appearance grants a peer this memory");h.succeed();return;}
            }
            f.at(p,place,.5,0,-3);f.look(p,b.offset(0,1,5));
        });
    }
    @AfterBatch(batch="literary_diary_sequence") public static void diaryDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="literary_wheel_sequence",timeoutTicks=420)
    public static void theWheelGoesRoundUntilTheReaderGoesBackThenOpensOnMotherAndLetsThemOutGrown(GameTestHelper h){
        var f=fixture(h);var place=LabyrinthPlace.WHEEL;var p=f.player("wheel_route",place);var peer=f.player("wheel_peer",place);var b=f.base(place);f.source(p,place);
        java.util.function.IntSupplier room=()->f.own(p,place).getInt("WheelRoom");
        h.startSequence().thenWaitUntil(()->{
            var cells=new java.util.ArrayList<BlockPos>();for(var r:WheelHouse.ROOMS){cells.add(r.back());cells.add(r.forward());}cells.add(WheelHouse.MOTHERS.back());
            for(var rel:cells){var cell=b.offset(rel);h.assertTrue(f.in.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(cell.getX()>>4,cell.getZ()>>4)),"the rooms behind every door are entity-ready before it is used");}
        }).thenExecute(()->{
            f.click(p,place,WheelHouse.FRONT_DOOR);var own=f.own(p,place);
            h.assertTrue(room.getAsInt()==1&&own.getBoolean("Memory")&&own.getBoolean("FrontLoop")&&!own.getBoolean("Ready"),"the front door takes them to the beginning, the nursery, as a child: "+own);
            for(int k=1;k<=5;k++){var r=WheelHouse.ROOMS.get(k-1);h.assertTrue(f.in.getBlockState(b.offset(r.forward())).getBlock() instanceof net.minecraft.world.level.block.DoorBlock,"room "+k+"'s way on is a real door");
                f.click(p,place,r.forward());h.assertTrue(room.getAsInt()==k+1&&f.own(p,place).getBoolean("Memory")==(k+1==2),"door "+(k+1)+" opens on room "+(k+1)+", a child only in the childhood rooms: "+f.own(p,place));
                h.assertTrue(f.in.noCollision(p,p.getBoundingBox()),"room "+(k+1)+" lands them in clear space");}
            f.click(p,place,WheelHouse.ROOMS.get(5).forward());own=f.own(p,place);
            h.assertTrue(room.getAsInt()==1&&!own.getBoolean("ReachedMother")&&own.getBoolean("Memory"),"the hospital room's door says 1 and goes round to the nursery: the wheel: "+own);
            f.click(p,place,WheelHouse.ROOMS.get(0).forward());f.click(p,place,WheelHouse.ROOMS.get(1).back());own=f.own(p,place);
            h.assertTrue(room.getAsInt()==1&&own.getBoolean("WentBack")&&!own.getBoolean("Memory"),"going back through two, they are their own height in one: "+own);
            h.assertTrue(io.github.knaitoe.theoldesthouse.house.LifeStageBlock.is(f.in.getBlockState(b.offset(WheelHouse.BIG_CHAIRS.get(0))),io.github.knaitoe.theoldesthouse.house.LifeStageBlock.Kind.CHAIR_BIG_LOWER),"the chair is smaller only on that reader's screen; the room keeps its own");
            for(int k=1;k<=5;k++)f.click(p,place,WheelHouse.ROOMS.get(k-1).forward());
            f.click(p,place,WheelHouse.ROOMS.get(5).forward());own=f.own(p,place);
            h.assertTrue(room.getAsInt()==7&&own.getBoolean("ReachedMother")&&own.getBoolean("Memory")&&!own.getBoolean("LeftMother"),"having gone back and seen all six, the 1 opens on Mother's room, and they are small again: "+own);
            f.click(p,place,WheelHouse.MOTHERS.back());h.assertTrue(room.getAsInt()==7,"her door does not open before she has been seen");
            f.at(p,place,.5,0,-54.5);
        });
        h.runAfterDelay(140,()->{var world=LiteraryVignettes.shared(f.data(),place);var mother=(LiteraryActor)f.in.getEntity(world.getUUID("Mother_"+p.getUUID()));
            h.assertTrue(mother!=null&&mother.owner().orElseThrow().equals(p.getUUID()),"the reader's own Mother waits in her room");p.lookAt(EntityAnchorArgument.Anchor.EYES,mother.getEyePosition());});
        h.runAfterDelay(170,()->{h.assertTrue(f.own(p,place).getBoolean("LeftMother")&&!f.own(peer,place).getBoolean("ReachedMother"),"only the reader has seen her");
            f.click(p,place,WheelHouse.MOTHERS.back());h.assertTrue(room.getAsInt()==0,"the door with no number lets them out to the hall");});
        h.runAfterDelay(200,()->{h.assertTrue(f.own(p,place).getBoolean("Ready")&&!f.own(p,place).getBoolean("Memory")&&p.getBbHeight()>1.5&&!f.own(peer,place).getBoolean("Ready"),"out in the hall they are their own height again, and only they");f.reload();f.read(p,place,LiteraryRooms.ending(place));h.assertTrue(WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.of(place.id()))&&!WitnessAccount.has(f.data(),peer.getUUID(),WitnessAccount.Story.of(place.id())),"the account completes only this reader");h.succeed();});
    }
    @AfterBatch(batch="literary_wheel_sequence") public static void wheelDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="literary_winchester_house",timeoutTicks=200)
    public static void theWinchesterIsNeverFinishedAndItsCarpentersRebuildOnlyAnEmptyUnwatchedHouse(GameTestHelper h){
        var f=fixture(h);var place=LabyrinthPlace.WINCHESTER;var p=f.player("winchester_reader",place);var b=f.base(place);var in=f.in;
        h.startSequence().thenExecute(()->{
            h.assertTrue(in.getBlockState(b.offset(-30,3,-12)).is(LiteraryRegistry.WINCHESTER_WALLPAPER.get())&&in.getBlockState(b.offset(5,-1,-30)).is(LiteraryRegistry.WINCHESTER_PARQUET.get()),"damask walls on a parquet floor");
            h.assertTrue(in.getBlockState(b.offset(WinchesterHouse.FLOOR_WINDOW)).is(LiteraryRegistry.WINCHESTER_LEADED_GLASS.get()),"a window in the ledger's floor");
            h.assertTrue(in.getBlockState(b.offset(-30,3,-22)).is(LiteraryRegistry.WINCHESTER_LEADED_GLASS.get())&&in.getBlockState(b.offset(-31,3,-22)).is(LiteraryRegistry.WINCHESTER_WALLPAPER.get()),"a leaded window looks onto more wallpaper");
            for(int k=0;k<WinchesterHouse.STAIR_STEPS;k++)h.assertTrue(in.getBlockState(b.offset(-28,k,WinchesterHouse.STAIR_Z-k)).getBlock() instanceof net.minecraft.world.level.block.StairBlock,"step "+(k+1)+" of thirteen");
            h.assertTrue(!in.getBlockState(b.offset(-28,13,WinchesterHouse.STAIR_Z-12)).isAir()&&!in.getBlockState(b.offset(-28,12,WinchesterHouse.STAIR_Z-11)).isAir(),"the last two steps go on into the ceiling");
            var wallDoor=b.offset(-29,0,-40);
            h.assertTrue(in.getBlockState(wallDoor).getBlock() instanceof net.minecraft.world.level.block.DoorBlock&&!in.getBlockState(wallDoor.west()).isAir(),"a door opens onto the wall");
            var highDoor=b.offset(29,9,-40);
            h.assertTrue(in.getBlockState(highDoor).getBlock() instanceof net.minecraft.world.level.block.DoorBlock&&in.getBlockState(highDoor.west()).isAir()&&in.getBlockState(highDoor.west().below()).isAir(),"a door high in the east wall opens onto nothing");
            int robes=0;for(int x=25;x<=29;x++)for(int z=-76;z<=-65;z++){var s=in.getBlockState(b.offset(x,2,z));if(s.is(io.github.knaitoe.theoldesthouse.house.HouseBlocks.SCENE_DETAIL.get())&&s.getValue(io.github.knaitoe.theoldesthouse.house.SceneDetailBlock.KIND)==io.github.knaitoe.theoldesthouse.house.SceneDetailBlock.Kind.COAT)robes++;}
            h.assertTrue(robes==13&&WinchesterFittingBlock.is(in.getBlockState(b.offset(WinchesterHouse.TABLE)),WinchesterFittingBlock.Kind.SEANCE_TABLE)&&WinchesterFittingBlock.is(in.getBlockState(b.offset(WinchesterHouse.CHAIR)),WinchesterFittingBlock.Kind.SEANCE_CHAIR)&&in.getBlockState(b.offset(24,3,-70)).is(LiteraryRegistry.WINCHESTER_SEANCE_PAPER.get()),"the blue séance room keeps its table, one chair and thirteen robes: "+robes);
            h.assertTrue(WinchesterFittingBlock.is(in.getBlockState(b.offset(WinchesterHouse.CLOCK.above())),WinchesterFittingBlock.Kind.CLOCK_TOP),"the hall clock stands in the entrance bay");
            var doors=WinchesterHouse.CHANGING_DOORS;
            for(var d:doors)h.assertTrue(in.getBlockState(b.offset(d)).getBlock() instanceof net.minecraft.world.level.block.DoorBlock,"every side passage keeps its own door");
            h.assertTrue(!WinchesterHouse.walled(in,b,doors.get(0))&&WinchesterHouse.walled(in,b,doors.get(1))&&WinchesterHouse.walled(in,b,doors.get(2))&&!WinchesterHouse.walled(in,b,doors.get(3)),"on the first visit two side doors open onto wall");
            // Another reader is in the house: the carpenters wait.
            var peer=f.player("winchester_peer",place);f.at(peer,place,.5,0,-40.5);
            p.teleportTo(in,b.getX()+.5,b.getY(),b.getZ()-2.5,0,0);LiteraryVignettes.onArrive(p,place);
            h.assertTrue(WinchesterHouse.layout(f.data(),b)==0&&WinchesterHouse.walled(in,b,doors.get(1)),"nothing is rebuilt while someone else is inside");
            // The house to themselves, facing the front door: the carpenters have been at work since the last visit.
            f.at(peer,place,.5,0,160.5);
            p.teleportTo(in,b.getX()+.5,b.getY(),b.getZ()-2.5,0,0);LiteraryVignettes.onArrive(p,place);
            h.assertTrue(WinchesterHouse.layout(f.data(),b)==1,"an empty, unwatched house is rebuilt between visits");
            h.assertTrue(WinchesterHouse.walled(in,b,doors.get(0))&&!WinchesterHouse.walled(in,b,doors.get(1))&&!WinchesterHouse.walled(in,b,doors.get(2))&&WinchesterHouse.walled(in,b,doors.get(3)),"the other two side doors open onto wall now");
            for(var d:doors)h.assertTrue(in.getBlockState(b.offset(d)).getBlock() instanceof net.minecraft.world.level.block.DoorBlock,"the doors themselves stay");
            h.assertTrue(WinchesterFittingBlock.is(in.getBlockState(b.offset(-27,0,-40)),WinchesterFittingBlock.Kind.SAWHORSE)&&in.getBlockState(b.offset(27,0,-40)).isAir(),"the sawhorse has moved across the house");
            // The clock and the carpenters are the reader's own.
            var own=f.own(p,place);h.assertTrue(own.getInt("Strikes")==0&&own.getInt("StrikeAt")==WinchesterHouse.STRIKE_AT,"the clock waits for its hour on a new visit");
            int heard=0;for(int t=0;t<=1200;t+=5){own.putInt("ThisVisitTicks",t);int before=own.getInt("Carpenter");WinchesterHouse.reader(p,b,own);if(own.getInt("Carpenter")!=before)heard++;
                if(t==WinchesterHouse.STRIKE_AT-5)h.assertTrue(own.getInt("Strikes")==0,"not before its hour");}
            h.assertTrue(own.getInt("Strikes")==13,"the clock strikes thirteen, and no more: "+own.getInt("Strikes"));
            h.assertTrue(heard>=5,"the carpenters are heard again and again behind the walls: "+heard);
            h.succeed();
        });
    }
    @AfterBatch(batch="literary_winchester_house") public static void winchesterDone(ServerLevel l){close();}

    private static final java.util.List<net.minecraft.world.level.ChunkPos> FORCED=new java.util.ArrayList<>();
    @GameTest(template="empty",batch="literary_movie_cast",timeoutTicks=520)
    public static void theAudienceTalksAndGoesMissingOnlyInTheDarkWhileNobodyLooks(GameTestHelper h){
        var f=fixture(h);var place=LabyrinthPlace.MOVIE_NIGHT;var p=f.player("movie_audience",place);var peer=f.player("movie_watcher",place);var b=f.base(place);
        // Keep the beach, the mooring and the hut loaded while the readers are away, so the reset can be seen.
        for(var at:java.util.List.of(b.offset(-6,0,-6),b.offset(12,0,-34),b.offset(14,0,-30),b.offset(0,0,-86),b.offset(0,0,-91))){var c=new net.minecraft.world.level.ChunkPos(at);if(!FORCED.contains(c)){FORCED.add(c);f.out.setChunkForced(c.x,c.z,true);}}
        f.at(p,place,.5,0,-10);p.lookAt(EntityAnchorArgument.Anchor.EYES,Vec3.atLowerCornerOf(b).add(.5,2,-40));f.at(peer,place,-5.5,0,-9.5);
        final boolean[] watching={true};final Vec3[] drifted={null};
        h.onEachTick(()->{var jamie=MovieNightCast.cast(f.out,MovieNightCast.JAMIE);if(jamie==null)return;
            // Looking straight up sees nobody on the lake or the sand.
            if(watching[0])peer.lookAt(EntityAnchorArgument.Anchor.EYES,jamie.getEyePosition());else peer.lookAt(EntityAnchorArgument.Anchor.EYES,peer.getEyePosition().add(0,10,0));
            if(p.level()==f.out)p.lookAt(EntityAnchorArgument.Anchor.EYES,p.getEyePosition().add(0,10,0));});
        h.runAfterDelay(15,()->{
            for(var who:MovieNightCast.CAST){var a=MovieNightCast.cast(f.out,who);h.assertTrue(a!=null&&a.role()==LiteraryActor.STRANGER&&a.phase()==who.phase()&&!a.isInvisible(),"everyone in the audience is here, in their own skin: "+who.name());}
            for(var who:java.util.List.of(MovieNightCast.FRANK,MovieNightCast.JAMIE,MovieNightCast.JUNE)){var a=MovieNightCast.cast(f.out,who);
                h.assertTrue(MoviePropBlock.is(f.out.getBlockState(a.blockPosition()),MoviePropBlock.Kind.LAWN_CHAIR),"each Pruitt sits in a lawn chair on the sand: "+who.name()+" at "+a.blockPosition().subtract(b));}
            h.assertTrue(MovieNightCast.cast(f.out,MovieNightCast.JAMIE).getScale()<.7F,"the boy is a child");
            var canoe=MovieNightCast.teenCanoe(f.out,LiteraryVignettes.shared(f.data(),place));
            h.assertTrue(canoe!=null&&canoe.paint()==5&&canoe.getPassengers().size()==2&&canoe.getPassengers().stream().allMatch(e->e instanceof LiteraryActor a&&MovieNightCast.teen(a)),"Bobby and Lynn sit together in their own painted canoe by the screen");
            for(int i=0;i<4;i++){var dock=MovieNight.canoe(f.out,LiteraryVignettes.shared(f.data(),place),i);h.assertTrue(dock!=null&&dock.paint()==1+i,"each dock canoe has its own paint");}
            h.assertTrue(!p.startRiding(canoe,false),"no reader climbs in with them");
            var dot=MovieNightCast.cast(f.out,MovieNightCast.DOT);h.assertTrue(dot.blockPosition().equals(b.offset(0,0,-87))&&f.out.getBlockState(b.offset(0,0,-86)).is(Blocks.SPRUCE_SLAB),"Dot stands behind her counter in the hut");
            var frank=MovieNightCast.cast(f.out,MovieNightCast.FRANK);f.at(p,place,-6.5,0,-8.5);LiteraryVignettes.talk(p,frank);
            h.assertTrue(f.own(p,place).getInt("Talk_Frank")==1&&frank.isCustomNameVisible(),"spoken to, Frank answers");f.at(p,place,.5,0,-10);
            // The boy is due to go; while the other reader watches him, he stays.
            var w=LiteraryVignettes.shared(f.data(),place);w.putInt("CastNight",MovieNightCast.JAMIE_GOES);LiteraryVignettes.shared(f.data(),place,w);});
        h.runAfterDelay(75,()->{h.assertTrue((MovieNightCast.gone(f.out)&MovieNightCast.JAMIE_BIT)==0&&!MovieNightCast.cast(f.out,MovieNightCast.JAMIE).isInvisible(),"nobody goes missing while a reader is looking at them");watching[0]=false;});
        h.runAfterDelay(160,()->{var jamie=MovieNightCast.cast(f.out,MovieNightCast.JAMIE);
            h.assertTrue((MovieNightCast.gone(f.out)&MovieNightCast.JAMIE_BIT)!=0&&jamie.isInvisible(),"looked away from in the dark, the boy is not there anymore");
            h.assertTrue(MoviePropBlock.is(f.out.getBlockState(b.offset(MovieNightCast.POPCORN)),MoviePropBlock.Kind.POPCORN_SPILLED),"his popcorn is spilled on the blanket");
            h.assertTrue((f.own(p,place).getInt("Missing0502")&MovieNightCast.JAMIE_BIT)!=0&&(f.own(peer,place).getInt("Missing0502")&MovieNightCast.JAMIE_BIT)!=0,"each reader here has him missing from their night");
            h.assertTrue((MovieNightCast.gone(f.out)&MovieNightCast.FRANK_BIT)==0&&!MovieNightCast.cast(f.out,MovieNightCast.FRANK).isInvisible(),"nobody else goes before their time");
            var w=LiteraryVignettes.shared(f.data(),place);w.putInt("CastNight",MovieNightCast.TEENS_GO);LiteraryVignettes.shared(f.data(),place,w);});
        h.runAfterDelay(240,()->{var canoe=MovieNightCast.teenCanoe(f.out,LiteraryVignettes.shared(f.data(),place));
            h.assertTrue((MovieNightCast.gone(f.out)&MovieNightCast.TEENS_BIT)!=0&&canoe.getPassengers().isEmpty()&&MovieNightCast.cast(f.out,MovieNightCast.LYNN).isInvisible(),"the two in the canoe are gone and the canoe is empty");
            drifted[0]=canoe.position();
            var text=LiteraryTexts.ending(p,place,f.own(p,place)).get(net.minecraft.core.component.DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().map(page->page.raw().getString()).reduce("",String::concat);
            h.assertTrue(text.contains("wasn't in his chair")&&text.contains("nobody in it")&&!text.contains("on its side"),"the account names who went missing in this reader's night, and only them: "+text);});
        h.runAfterDelay(262,()->{h.assertTrue(MovieNightCast.teenCanoe(f.out,LiteraryVignettes.shared(f.data(),place)).position().distanceTo(drifted[0])>.15,"the empty canoe drifts off while the scene is occupied");
            // Everyone leaves; once the lake has been empty a while, the night resets for the next reader.
            for(var r:java.util.List.of(p,peer)){r.stopRiding();r.teleportTo(f.in,f.origin.getX()+.5,f.origin.getY()+1,f.origin.getZ()+.5,0,0);}});
        h.runAfterDelay(480,()->{
            h.assertTrue(MovieNightCast.gone(f.out)==0,"the night resets once the lake is empty and unwatched");
            for(var who:MovieNightCast.CAST){var a=MovieNightCast.cast(f.out,who);h.assertTrue(a!=null&&!a.isInvisible(),"everyone is back for the next reader: "+who.name());}
            h.assertTrue(MovieNightCast.cast(f.out,MovieNightCast.JAMIE).blockPosition().equals(b.offset(MovieNightCast.JAMIE_CHAIR))&&MoviePropBlock.is(f.out.getBlockState(b.offset(MovieNightCast.POPCORN)),MoviePropBlock.Kind.POPCORN),"the boy is back in his chair with his popcorn");
            var canoe=MovieNightCast.teenCanoe(f.out,LiteraryVignettes.shared(f.data(),place));
            h.assertTrue(canoe.getPassengers().size()==2&&canoe.position().distanceTo(Vec3.atLowerCornerOf(b).add(MovieNightCast.MOORING))<.5,"the two are back in their canoe at its mooring");h.succeed();});
    }
    @AfterBatch(batch="literary_movie_cast") public static void movieCastDone(ServerLevel l){close();var out=HouseTestLevel.get(l.getServer(),io.github.knaitoe.theoldesthouse.house.HouseDimensions.OUTSIDE);for(var c:FORCED)out.setChunkForced(c.x,c.z,false);FORCED.clear();}

    // ------------------------------------------------------------------------------------------------ Costume Night (0.4.103)
    private static void costumeState(LiteraryTests.Fixture f,java.util.function.Consumer<CompoundTag> change){var w=LiteraryVignettes.shared(f.data(),LabyrinthPlace.COSTUME_NIGHT);change.accept(w);LiteraryVignettes.shared(f.data(),LabyrinthPlace.COSTUME_NIGHT,w);}
    private static void lookUp(ServerPlayer p){p.lookAt(EntityAnchorArgument.Anchor.EYES,p.getEyePosition().add(0,10,0));}
    @GameTest(template="empty",batch="literary_costume_still",timeoutTicks=420)
    public static void theThirteenthWitchHoldsStillWhileWatchedAndHuntsOnlyOnceTheLightsAreOut(GameTestHelper h){
        var f=fixture(h);var place=LabyrinthPlace.COSTUME_NIGHT;var p=f.player("costume_judge",place);var peer=f.player("costume_peer",place);var b=f.base(place);
        var room=place.room();
        f.chunks.hold(f.out,new net.minecraft.world.phys.AABB(b.getX()+room.minX(),b.getY()+room.minY(),b.getZ()+room.minZ(),b.getX()+room.maxX()+1,b.getY()+room.maxY()+1,b.getZ()+room.maxZ()+1));
        h.startSequence().thenWaitUntil(()->{
            h.assertTrue(f.chunks.ready(),"the contest's actual native entity sections are resident");
            for(var who:CostumeNight.EVERYONE)h.assertTrue(CostumeNight.cast(f.out,who)!=null,"the original contestant has loaded: "+who.name());
            h.assertTrue(CostumeNight.witch(f.out)!=null,"the original thirteenth witch has loaded");
        }).thenExecute(()->{
        f.at(p,place,13.5,0,-23.5);f.at(peer,place,.5,0,-4);final int[] stage={0};final Vec3[] still={null};final double[] reach={0};
        h.onEachTick(()->{var witch=CostumeNight.witch(f.out);if(witch==null)return;lookUp(peer);
            // She may strike and go straight back to the very spot she waited in, so measure how far she came, not where she ends.
            if(stage[0]==2&&still[0]!=null)reach[0]=Math.max(reach[0],witch.position().distanceTo(still[0]));
            if(stage[0]==1){p.lookAt(EntityAnchorArgument.Anchor.EYES,witch.getEyePosition());if(still[0]==null)still[0]=witch.position();
                h.assertTrue(witch.position().distanceToSqr(still[0])<1e-8,"while a reader looks at her she does not move at all, lights or no lights: "+witch.position()+" from "+still[0]);}
            else lookUp(p);
            h.assertTrue(!f.out.getBlockState(witch.blockPosition().below()).is(Blocks.GRASS_BLOCK),"she never sets foot on the park grass");});
        h.runAfterDelay(15,()->{
            for(var who:CostumeNight.EVERYONE){var a=CostumeNight.cast(f.out,who);h.assertTrue(a!=null&&a.role()==LiteraryActor.STRANGER&&a.phase()==who.phase(),"everyone at the contest is here in their own costume: "+who.name());
                h.assertTrue(a.gesture()==(who.crawls()?CostumeNight.CRAWL:0)&&(a.gesture()!=CostumeNight.CRAWL||a.getBbHeight()<1.3F),"the crawlers are on all fours, low to the sand: "+who.name());}
            h.assertTrue(CostumeNight.cast(f.out,CostumeNight.ENTRANTS.get(1)).getScale()<.7F&&CostumeNight.cast(f.out,CostumeNight.HANK).getY()>=b.getY()+.99,"the youngest entrants are children; Hank judges from the stage");
            h.assertTrue(CostumeNight.witch(f.out)!=null&&f.out.getBlockState(b.offset(CostumeNight.bulbs().getFirst())).getValue(CostumePropBlock.LIT),"the thirteenth witch is at the water and the bulbs are lit");});
        // Unseen beside her while the bulbs are lit: the contest is still on, and she does nothing.
        h.runAfterDelay(75,()->{h.assertTrue(p.getHealth()==20&&!f.own(p,place).getBoolean("Struck0503"),"before the lights go she never strikes, even unseen beside her");
            var witch=CostumeNight.witch(f.out);var start=Vec3.atBottomCenterOf(b.offset(CostumeNight.WITCH_START));witch.moveTo(start.x,witch.getY(),start.z,90,0);
            f.at(p,place,10.5,0,-24.5);costumeState(f,w->{w.putBoolean("LightsOut",true);w.putInt("RibbonAt",-10000);});stage[0]=1;});
        h.runAfterDelay(120,()->{stage[0]=2;var witch=CostumeNight.witch(f.out);still[0]=witch.position();});
        h.runAfterDelay(175,()->{var witch=CostumeNight.witch(f.out);
            h.assertTrue(reach[0]>1&&p.getHealth()<20&&f.own(p,place).getBoolean("Struck0503"),"the moment nobody looks she comes, fast, and strikes the reader unseen: health="+p.getHealth()+" came="+reach[0]);
            p.setHealth(20);f.at(p,place,.5,0,-60.5);});
        h.runAfterDelay(300,()->{h.assertTrue(p.getHealth()==20&&peer.getHealth()==20,"on the park grass, and where she cannot come by the entrance, nobody is hurt");h.succeed();TheOldestHouse.LOGGER.info("COSTUME NATIVE PASS: original cast, watched stillness, unseen strike and living-grass refuge");});
        });
    }
    @AfterBatch(batch="literary_costume_still") public static void costumeStillDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="literary_costume_ribbon",timeoutTicks=760)
    public static void hankCallsTheNumbersGivesTheRibbonAndTheLightsGoOut(GameTestHelper h){
        var f=fixture(h);var place=LabyrinthPlace.COSTUME_NIGHT;var p=f.player("costume_ribbon",place);var peer=f.player("costume_crowd",place);var b=f.base(place);
        f.at(p,place,2.5,0,-15);f.at(peer,place,-2.5,0,-8);var tina=CostumeNight.ENTRANTS.getFirst();final int[] start={0};
        h.onEachTick(()->{lookUp(p);lookUp(peer);});
        h.runAfterDelay(10,()->{var hank=CostumeNight.cast(f.out,CostumeNight.HANK);h.assertTrue(hank!=null,"Hank is on the stage");
            f.at(p,place,-2.5,0,-23.5);LiteraryVignettes.talk(p,hank);LiteraryVignettes.talk(p,hank);
            h.assertTrue(f.own(p,place).getInt("Talk_Hank")==2&&f.own(p,place).getBoolean("HankStory0503"),"spoken to twice, Hank tells how the contest began");
            CostumeNight.use(p,CostumePropBlock.Kind.APPLE_TUB,b.offset(CostumeNight.APPLES));CostumeNight.use(p,CostumePropBlock.Kind.APPLE_TUB,b.offset(CostumeNight.APPLES));
            CostumeNight.use(p,CostumePropBlock.Kind.CANDY_BOWL,b.offset(CostumeNight.CANDY));CostumeNight.use(p,CostumePropBlock.Kind.CANDY_BOWL,b.offset(CostumeNight.CANDY));
            h.assertTrue(p.getInventory().countItem(Items.APPLE)==1&&p.getInventory().countItem(Items.COOKIE)==1,"one apple from the tub and one sweet from the bowl, however often the reader tries");
            f.at(p,place,2.5,0,-15);costumeState(f,w->w.putInt("Party",CostumeNight.FIRST_CALL-1));});
        h.runAfterDelay(110,()->{var w=LiteraryVignettes.shared(f.data(),place);var a=CostumeNight.cast(f.out,tina);
            h.assertTrue(w.getInt("Called")==0&&a.position().distanceTo(Vec3.atLowerCornerOf(b).add(CostumeNight.STAGE_FRONT))<1.5&&a.gesture()==4,"number one is called up and shows off at the front of the stage: "+a.position());});
        h.runAfterDelay(290,()->{var a=CostumeNight.cast(f.out,tina);var night=LiteraryVignettes.shared(f.data(),place);
            h.assertTrue(!(night.contains("Called")&&night.getInt("Called")==0)&&a.position().distanceTo(tina.home(b))<.6,"then she goes back to her place (Hank may already be calling number two): "+a.position()+" home "+tina.home(b));
            costumeState(f,w->w.putInt("Party",CostumeNight.RIBBON-1));start[0]=1;});
        h.runAfterDelay(540,()->{h.assertTrue(!LiteraryVignettes.shared(f.data(),place).getBoolean("LightsOut")&&p.getHealth()==20&&peer.getHealth()==20,"through the ribbon the bulbs stay on and nobody is hurt");});
        h.runAfterDelay(590,()->{var w=LiteraryVignettes.shared(f.data(),place);
            h.assertTrue(w.getBoolean("LightsOut"),"nobody comes up for the ribbon, and the lights go out");
            h.assertTrue(CostumeNight.bulbs().stream().allMatch(rel->!f.out.getBlockState(b.offset(rel)).getValue(CostumePropBlock.LIT))&&!f.out.getBlockState(b.offset(CostumeNight.RADIO)).getValue(CostumePropBlock.LIT),"every bulb and the radio go dark");
            for(var who:CostumeNight.ENTRANTS)h.assertTrue(CostumeNight.cast(f.out,who).gesture()==CostumeNight.CRAWL,"every entrant drops to all fours for the crawl: "+who.name());
            h.assertTrue(CostumeNight.cast(f.out,CostumeNight.HANK).gesture()==0,"Hank stays on his feet");h.succeed();});
    }
    @AfterBatch(batch="literary_costume_ribbon") public static void costumeRibbonDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="literary_costume_park",timeoutTicks=420)
    public static void crossingToTheParkWithHerInSightEndsTheNightAndTheContestResets(GameTestHelper h){
        var f=fixture(h);var place=LabyrinthPlace.COSTUME_NIGHT;var p=f.player("costume_crosser",place);var peer=f.player("costume_waiter",place);var b=f.base(place);
        for(int x=-24;x<=24;x+=16)for(int z=-40;z<=-8;z+=16){var c=new net.minecraft.world.level.ChunkPos(b.offset(x,0,z));if(!FORCED.contains(c)){FORCED.add(c);f.out.setChunkForced(c.x,c.z,true);}}
        f.source(p,place);f.at(peer,place,5.5,0,-60.5);f.at(p,place,.5,0,-30.5);final boolean[] watching={true};
        h.onEachTick(()->{var witch=CostumeNight.witch(f.out);if(witch==null)return;
            if(watching[0]){p.lookAt(EntityAnchorArgument.Anchor.EYES,witch.getEyePosition());peer.lookAt(EntityAnchorArgument.Anchor.EYES,witch.getEyePosition());}else{lookUp(p);lookUp(peer);}});
        h.runAfterDelay(10,()->costumeState(f,w->{w.putBoolean("LightsOut",true);w.putInt("RibbonAt",-10000);}));
        h.runAfterDelay(40,()->{h.assertTrue(f.own(p,place).getBoolean("SeenHer0503")&&f.own(p,place).getBoolean("Crossing0503"),"out on the sand after the lights go, the reader keeps her in sight");
            f.at(p,place,.5,0,-60.5);});
        h.runAfterDelay(70,()->{h.assertTrue(f.own(p,place).getBoolean("Ready"),"the park grass with her in sight is the end of the night");
            h.assertTrue(!f.own(peer,place).getBoolean("Ready")&&f.own(peer,place).getBoolean("CrossHint0503"),"a reader who waited on the grass from before the lights went has not crossed the dark");
            var text=LiteraryTexts.ending(p,place,f.own(p,place)).get(net.minecraft.core.component.DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().map(page->page.raw().getString()).reduce("",String::concat);
            h.assertTrue(text.contains("one down by the water")&&text.contains("one of them was not playing")&&!text.contains("Hank told me"),"the account tells this reader's night, and only what they heard: "+text);
            f.read(p,place,LiteraryRooms.ending(place));h.assertTrue(WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.of(place.id()))&&!WitnessAccount.has(f.data(),peer.getUUID(),WitnessAccount.Story.of(place.id())),"reading it completes only this reader's scene");
            watching[0]=false;for(var r:java.util.List.of(p,peer)){r.stopRiding();r.teleportTo(f.in,f.origin.getX()+.5,f.origin.getY()+1,f.origin.getZ()+.5,0,0);}});
        h.runAfterDelay(260,()->{var w=LiteraryVignettes.shared(f.data(),place);
            h.assertTrue(!w.getBoolean("LightsOut")&&!w.contains("Party")&&CostumeNight.bulbs().stream().allMatch(rel->f.out.getBlockState(b.offset(rel)).getValue(CostumePropBlock.LIT)),"once the beach is empty and unwatched, the bulbs come back on for the next reader");
            var tina=CostumeNight.ENTRANTS.getFirst();var danny=CostumeNight.ENTRANTS.get(1);
            h.assertTrue(CostumeNight.cast(f.out,tina).gesture()==0&&CostumeNight.cast(f.out,tina).position().distanceTo(tina.home(b))<.5&&CostumeNight.cast(f.out,danny).gesture()==CostumeNight.CRAWL,"everyone is back in their place, standing or crawling as they came");
            var witch=CostumeNight.witch(f.out);var start=Vec3.atBottomCenterOf(b.offset(CostumeNight.WITCH_START));
            h.assertTrue(witch!=null&&Math.hypot(witch.getX()-start.x,witch.getZ()-start.z)<1.5,"and she is back at the water's edge");h.succeed();});
    }
    @AfterBatch(batch="literary_costume_park") public static void costumeParkDone(ServerLevel l){close();var out=HouseTestLevel.get(l.getServer(),io.github.knaitoe.theoldesthouse.house.HouseDimensions.OUTSIDE);for(var c:FORCED)out.setChunkForced(c.x,c.z,false);FORCED.clear();}

    @GameTest(template="empty",batch="literary_movie_original",timeoutTicks=450)
    public static void uncertainLoadedAbsenceCannotReplaceTheActualSerializedMovieCanoe(GameTestHelper h){
        var f=fixture(h);var place=LabyrinthPlace.MOVIE_NIGHT;var p=f.player("movie_original",place);var b=f.base(place);
        h.startSequence().thenWaitUntil(()->{var world=LiteraryVignettes.shared(f.data(),place);h.assertTrue(world.hasUUID("Canoe0")&&f.out.getEntity(world.getUUID("Canoe0")) instanceof LakeCanoeEntity,"the actual original canoe has entered a native entity section");})
            .thenExecute(()->{var world=LiteraryVignettes.shared(f.data(),place);var original=(LakeCanoeEntity)f.out.getEntity(world.getUUID("Canoe0"));var id=original.getUUID();var saved=original.saveWithoutId(new CompoundTag());original.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);f.reload();
                h.runAfterDelay(145,()->{var state=LiteraryVignettes.shared(f.data(),place);h.assertTrue(state.getUUID("Canoe0").equals(id)&&!state.getBoolean("Removed0")&&f.out.getEntitiesOfClass(LakeCanoeEntity.class,IndianLakeRooms.bounds(b,place),c->c.getTags().contains(MovieNight.CANOE)).size()==3,"even loaded native absence after 140 ticks cannot invent a removal or a fifth canoe");var restored=DrownedTownRegistry.CAVE_CANOE.get().create(f.out);h.assertTrue(restored!=null,"the saved original has its real native type");restored.load(saved);h.assertTrue(f.out.addFreshEntity(restored),"the actual serialized body returns through native entity loading");
                    h.runAfterDelay(15,()->{var latest=LiteraryVignettes.shared(f.data(),place);h.assertTrue(latest.getUUID("Canoe0").equals(id)&&f.out.getEntity(id)==restored&&f.out.getEntitiesOfClass(LakeCanoeEntity.class,IndianLakeRooms.bounds(b,place),c->c.getTags().contains(MovieNight.CANOE)).size()==4,"the exact original UUID is recovered once without replacements");h.assertTrue(!f.own(p,place).getBoolean("Ready")&&f.own(p,place).getDouble("RowDistance")==0,"waiting for native loading grants no fabricated travel or outcome");h.succeed();});
                });
            });
    }
    @AfterBatch(batch="literary_movie_original") public static void movieOriginalDone(ServerLevel l){close();}
}
