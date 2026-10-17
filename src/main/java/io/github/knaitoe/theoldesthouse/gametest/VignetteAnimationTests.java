package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
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
        h.assertTrue(MovieNight.footing(Blocks.WATER.defaultBlockState())&&MovieNight.footing(Blocks.OAK_PLANKS.defaultBlockState())&&MovieNight.footing(Blocks.DARK_OAK_PLANKS.defaultBlockState())
                &&!MovieNight.footing(Blocks.SPRUCE_PLANKS.defaultBlockState())&&!MovieNight.footing(Blocks.SAND.defaultBlockState())&&!MovieNight.footing(Blocks.GRASS_BLOCK.defaultBlockState()),"she keeps to the water and the planks out on it; never the far pier or a bank");
        h.assertTrue(f.out.getBlockState(b.offset(0,-1,-64)).is(Blocks.SPRUCE_PLANKS)&&f.out.getBlockState(b.offset(2,-1,-64)).is(Blocks.WATER)&&f.out.getBlockState(b.offset(-2,-1,-64)).is(Blocks.WATER),"the far pier runs out over open water");
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

    @GameTest(template="empty",batch="literary_winter_sequence",timeoutTicks=350)
    public static void actualIceBreakUnderwaterWaitAndSupportedFarBankResolveOnlyTheirReader(GameTestHelper h){
        var f=fixture(h);var place=LabyrinthPlace.WINTER_LAKE;var p=f.player("winter_route",place);var peer=f.player("winter_dry_peer",place);var b=f.base(place);
        f.source(p,place);f.source(peer,place);f.at(peer,place,.5,.125,-80.5);f.at(p,place,.5,0,-24.5);
        var ice=b.offset(0,-1,-25);final int[] phase={0},nativeWetTicks={0},fluidWarmup={0};
        h.runAfterDelay(20,()->{
            h.assertTrue(!f.own(peer,place).getBoolean("Ready")&&!f.own(p,place).getBoolean("Ready"),"dry bank arrival and the source alone do not resolve this lake");
            h.assertTrue(f.out.getBlockState(ice).is(Blocks.ICE)&&p.gameMode.destroyBlock(ice)&&!f.out.getBlockState(ice).is(Blocks.ICE),"the actual allowed native mining action opens original lake ice");
            h.assertTrue(f.own(p,place).getBoolean("BrokeIce")&&!f.own(peer,place).getBoolean("BrokeIce"),"only the real ice miner retains the break fact");phase[0]=1;
        });
        h.onEachTick(()->{
            if(phase[0]!=1)return;f.at(p,place,.5,-3,-24.5);p.baseTick();p.setDeltaMovement(Vec3.ZERO);p.setHealth(20);
            h.assertTrue(p.isEyeInFluid(net.minecraft.tags.FluidTags.WATER)&&p.getY()+p.getBbHeight()<b.getY(),"native eye fluid and complete body are genuinely beneath the ice: eye="+p.getEyePosition()+", body="+p.getBoundingBox());
            if(!p.isUnderWater()){h.assertTrue(++fluidWarmup[0]<=5,"native underwater state settles after actual fluid ticks, never from a forced flag");return;}nativeWetTicks[0]++;
            if(nativeWetTicks[0]==65)h.assertTrue(!f.own(p,place).getBoolean("Ready"),"a shorter real underwater interval cannot manufacture the bank outcome");
            if(f.own(p,place).getInt("SubmergedTicks")>=110){phase[0]=2;f.at(p,place,.5,.125,-80.5);p.baseTick();p.setDeltaMovement(Vec3.ZERO);h.assertTrue(!p.isEyeInFluid(net.minecraft.tags.FluidTags.WATER)&&f.out.noCollision(p,p.getBoundingBox()),"the real far bank supports the living full-height reader above native water");}
        });
        h.startSequence().thenWaitUntil(()->h.assertTrue(phase[0]==2&&f.own(p,place).getBoolean("Ready"),"actual ice, at least one hundred native wet ticks and physical bank arrival all complete"))
            .thenExecute(()->{h.assertTrue(nativeWetTicks[0]>=100&&!f.own(peer,place).getBoolean("Ready"),"the dry peer inherits neither water time nor outcome");f.reload();h.assertTrue(f.own(p,place).getBoolean("Ready")&&f.own(p,place).getInt("SubmergedTicks")>=100,"the actual escape facts survive SavedData reload");f.read(p,place,LiteraryRooms.ending(place));h.assertTrue(WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.of(place.id()))&&!WitnessAccount.has(f.data(),peer.getUUID(),WitnessAccount.Story.of(place.id())),"the owned final reading resolves only the real participant");}).thenSucceed();
    }
    @AfterBatch(batch="literary_winter_sequence") public static void winterDone(ServerLevel l){close();}

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

    @GameTest(template="empty",batch="literary_wheel_sequence",timeoutTicks=350)
    public static void sixNativeStoryDoorsKeepTheMotherGatePrivateAndRequireTheRealFrontReturn(GameTestHelper h){
        var f=fixture(h);var place=LabyrinthPlace.WHEEL;var p=f.player("wheel_route",place);var peer=f.player("wheel_peer",place);var b=f.base(place);f.source(p,place);
        h.startSequence().thenWaitUntil(()->{for(int x:new int[]{-14,14})for(int z:new int[]{-8,-25,-42,-59}){var cell=b.offset(x,0,z);h.assertTrue(f.in.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(cell.getX()>>4,cell.getZ()>>4)),"actual destination sections are entity-ready before native door use");}}).thenExecute(()->{
        f.click(p,place,new BlockPos(0,0,-7));h.assertTrue(f.own(p,place).getBoolean("FrontLoop")&&!f.own(p,place).getBoolean("Ready"),"the actual front door loops before Mother is found");
        int[] order={4,0,1,2,3,5,4};
        for(int n=0;n<order.length;n++){final int at=n;h.runAfterDelay(20+n*15,()->{
            int door=order[at];var rel=new BlockPos(door%2==0?-14:14,0,-17*(door/2+1));
            h.assertTrue(f.in.getBlockState(b.offset(rel)).getBlock() instanceof net.minecraft.world.level.block.DoorBlock,"each numbered story threshold is an actual native door");
            f.click(p,place,rel);h.assertTrue((f.own(p,place).getInt("WheelDoors")&(1<<door))!=0,"the actual use of door "+door+" records its own route bit");
            h.assertTrue(f.in.noCollision(p,p.getBoundingBox()),"native story door "+door+" lands a real reader in clear supported space");
            if(at==0)h.assertTrue(!f.own(p,place).getBoolean("ReachedMother"),"using Mother's door too early does not skip the four-room gate");
        });}
        h.runAfterDelay(135,()->{var own=f.own(p,place);h.assertTrue(own.getInt("WheelDoors")==63&&own.getBoolean("ReachedMother")&&!own.getBoolean("Ready"),"all six native doors still require seeing Mother and returning");
            var world=LiteraryVignettes.shared(f.data(),place);var mother=(LiteraryActor)f.in.getEntity(world.getUUID("Mother_"+p.getUUID()));h.assertTrue(mother!=null&&mother.owner().orElseThrow().equals(p.getUUID()),"the route finds the same privately bound native Mother");p.lookAt(EntityAnchorArgument.Anchor.EYES,mother.getEyePosition());
        });
        h.runAfterDelay(155,()->{h.assertTrue(f.own(p,place).getBoolean("LeftMother")&&!f.own(peer,place).getBoolean("ReachedMother"),"only the actual reader has seen Mother at the end of their route");f.at(p,place,.5,0,-4.5);});
        h.runAfterDelay(180,()->{h.assertTrue(f.own(p,place).getBoolean("Ready")&&!f.own(p,place).getBoolean("Memory")&&p.getBbHeight()>1.5&&!f.own(peer,place).getBoolean("Ready"),"the physical front return ends only that reader's memory and restores the actual full-height native body");f.reload();f.read(p,place,LiteraryRooms.ending(place));h.assertTrue(WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.of(place.id()))&&!WitnessAccount.has(f.data(),peer.getUUID(),WitnessAccount.Story.of(place.id())),"the actual original account completes only this route's reader");h.succeed();});
        });
    }
    @AfterBatch(batch="literary_wheel_sequence") public static void wheelDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="literary_movie_original",timeoutTicks=450)
    public static void uncertainLoadedAbsenceCannotReplaceTheActualSerializedMovieCanoe(GameTestHelper h){
        var f=fixture(h);var place=LabyrinthPlace.MOVIE_NIGHT;var p=f.player("movie_original",place);var b=f.base(place);
        h.startSequence().thenWaitUntil(()->{var world=LiteraryVignettes.shared(f.data(),place);h.assertTrue(world.hasUUID("Canoe0")&&f.out.getEntity(world.getUUID("Canoe0")) instanceof LakeCanoeEntity,"the actual original canoe has entered a native entity section");})
            .thenExecute(()->{var world=LiteraryVignettes.shared(f.data(),place);var original=(LakeCanoeEntity)f.out.getEntity(world.getUUID("Canoe0"));var id=original.getUUID();var saved=original.saveWithoutId(new CompoundTag());original.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);f.reload();
                h.runAfterDelay(145,()->{var state=LiteraryVignettes.shared(f.data(),place);h.assertTrue(state.getUUID("Canoe0").equals(id)&&!state.getBoolean("Removed0")&&f.out.getEntitiesOfClass(LakeCanoeEntity.class,IndianLakeRooms.bounds(b,place)).size()==3,"even loaded native absence after 140 ticks cannot invent a removal or a fifth canoe");var restored=DrownedTownRegistry.CAVE_CANOE.get().create(f.out);h.assertTrue(restored!=null,"the saved original has its real native type");restored.load(saved);h.assertTrue(f.out.addFreshEntity(restored),"the actual serialized body returns through native entity loading");
                    h.runAfterDelay(15,()->{var latest=LiteraryVignettes.shared(f.data(),place);h.assertTrue(latest.getUUID("Canoe0").equals(id)&&f.out.getEntity(id)==restored&&f.out.getEntitiesOfClass(LakeCanoeEntity.class,IndianLakeRooms.bounds(b,place)).size()==4,"the exact original UUID is recovered once without replacements");h.assertTrue(!f.own(p,place).getBoolean("Ready")&&f.own(p,place).getDouble("RowDistance")==0,"waiting for native loading grants no fabricated travel or outcome");h.succeed();});
                });
            });
    }
    @AfterBatch(batch="literary_movie_original") public static void movieOriginalDone(ServerLevel l){close();}
}
