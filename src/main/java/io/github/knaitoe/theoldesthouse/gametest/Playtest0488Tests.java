package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseBlocks;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.UUID;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.gametest.*;

/** Reproduces the reported native interactions rather than calling an item's implementation directly. */
@GameTestHolder(TheOldestHouse.MOD_ID+"_playtest488") @PrefixGameTestTemplate(false)
public final class Playtest0488Tests {
    private static LiteraryTests.Fixture speech;
    private static LiteraryTests.Fixture rabbitMotion,rabbitEnding,rabbitNights,rabbitShelter,rabbitFloor,rabbitTrail,rabbitMorning;

    @GameTest(template="empty",batch="notes488_navigation",timeoutTicks=100)
    public static void adventureReaderCanDrawChalkAndUseStringBesideProtectedHouseStone(GameTestHelper h){
        try(var f=new LiteraryTests.Fixture(h)){
            var place=LabyrinthPlace.MINIATURES;var p=f.player("adventure_chalk",place);var l=f.level(place);
            var mark=f.base(place).offset(5,0,-10);var wall=mark.north();l.getChunkAt(wall);
            l.setBlock(wall,Blocks.STONE.defaultBlockState(),3);l.setBlock(mark,Blocks.AIR.defaultBlockState(),3);
            p.moveTo(Vec3.atBottomCenterOf(mark.south()));p.setGameMode(GameType.ADVENTURE);
            h.assertTrue(!p.getAbilities().mayBuild,"this actual reader cannot place structural blocks");
            var chalk=new ItemStack(LabyrinthRegistry.CHALK.get());p.setItemInHand(InteractionHand.MAIN_HAND,chalk);
            var hit=new BlockHitResult(wall.getCenter().add(0,0,.5),Direction.SOUTH,wall,false);
            var drawn=p.gameMode.useItemOn(p,l,chalk,InteractionHand.MAIN_HAND,hit);
            h.assertTrue(drawn.consumesAction()&&l.getBlockState(mark).is(HouseBlocks.CHALK_MARK.get())&&chalk.getDamageValue()==1,
                "the native adventure interaction places one chalk mark and spends its actual chalk");
            h.assertTrue(l.getBlockState(wall).is(Blocks.STONE),"drawing keeps the protected original wall");
            var broken=new BlockEvent.BreakEvent(l,wall,l.getBlockState(wall),p);NeoForge.EVENT_BUS.post(broken);
            h.assertTrue(broken.isCanceled(),"drawing grants no permission to break the House");
            var spool=new ItemStack(LabyrinthRegistry.TRAIL_SPOOL.get());p.setItemInHand(InteractionHand.MAIN_HAND,spool);
            var tied=p.gameMode.useItemOn(p,l,spool,InteractionHand.MAIN_HAND,hit);
            h.assertTrue(tied.consumesAction()&&p.getPersistentData().getBoolean("HouseTrailActive"),"the same native adventure click knots the string");
            p.gameMode.useItemOn(p,l,spool,InteractionHand.MAIN_HAND,hit);
            h.assertTrue(!p.getPersistentData().getBoolean("HouseTrailActive"),"a second deliberate click winds it back in");
            h.succeed();
        }
    }

    @GameTest(template="empty",batch="notes488_paw_speech",timeoutTicks=180)
    public static void cottageSpeechChangesWithItsSpeakerAndStageAndLeavesNoStaleHoverName(GameTestHelper h){
        speech=new LiteraryTests.Fixture(h);var f=speech;var place=LabyrinthPlace.THREE_FINGERS;
        var p=f.player("speech_reader",place);var peer=f.player("speech_peer",place);var l=f.level(place);
        // 0.4.147: two households at once are two threads, each with its own cottage people.
        VignetteThreads.forget(f.data(),place,peer.getUUID());VignetteThreads.solo(peer,place);
        h.startSequence().thenWaitUntil(()->{
            var mine=story(f,p);var theirs=story(f,peer);
            h.assertTrue(mine.hasUUID("CastFather")&&mine.hasUUID("CastSergeant")&&theirs.hasUUID("CastFather")
                &&l.getEntity(mine.getUUID("CastFather")) instanceof LiteraryActor
                &&l.getEntity(mine.getUUID("CastSergeant")) instanceof LiteraryActor
                &&l.getEntity(theirs.getUUID("CastFather")) instanceof LiteraryActor,"the real private cottage cast has loaded");
        }).thenExecute(()->{
            var mine=story(f,p);var theirs=story(f,peer);
            var father=(LiteraryActor)l.getEntity(mine.getUUID("CastFather"));var sergeant=(LiteraryActor)l.getEntity(mine.getUUID("CastSergeant"));
            var peerFather=(LiteraryActor)l.getEntity(theirs.getUUID("CastFather"));var fatherId=father.getUUID();
            f.at(peer,place,-6,0,-9);LiteraryVignettes.talk(peer,peerFather);
            f.at(p,place,-6,0,-9);LiteraryVignettes.talk(p,father);
            h.assertTrue(father.hasCustomName()&&peerFather.hasCustomName(),"each household may speak independently");
            LiteraryVignettes.talk(p,sergeant);
            h.assertTrue(sergeant.hasCustomName()&&!father.hasCustomName()&&peerFather.hasCustomName(),
                "the soldier's next line replaces this household's bubble without erasing a peer's speech");
            var thread=VignetteThreads.thread(f.data(),place,p.getUUID());mine=thread.getCompound("Story");mine.putInt("Stage",ThreeFingers.WISHED);mine.putInt("WishVisit",thread.getInt("Visit"));
            thread.put("Story",mine);VignetteThreads.saveThread(f.data(),place,p.getUUID(),thread);LiteraryVignettes.talk(p,father);
            h.assertTrue(father.getCustomName()!=null&&father.getCustomName().getString().contains("It moved")&&!sergeant.hasCustomName(),
                "the same father speaks the actual later-stage response");
            father.getPersistentData().putLong("SayUntil0488",l.getGameTime()-1);
            ThreeFingers.reader(p,f.base(place),f.own(p,place));
            h.assertTrue(!father.hasCustomName()&&!father.isCustomNameVisible()&&father.getUUID().equals(fatherId)&&peerFather.hasCustomName(),
                "expired speech clears the native hover name, preserving actor identity and the peer's bubble");
        }).thenSucceed();
    }
    private static CompoundTag story(LiteraryTests.Fixture f,ServerPlayer p){return VignetteThreads.thread(f.data(),LabyrinthPlace.THREE_FINGERS,p.getUUID()).getCompound("Story");}
    @AfterBatch(batch="notes488_paw_speech") public static void speechDone(ServerLevel ignored){if(speech!=null){speech.close();speech=null;}}

    @GameTest(template="empty",batch="notes488_rabbit_motion",timeoutTicks=600)
    public static void theOriginalFatherHobblesLessEachNightAndTheNamedRabbitNeverChangesItsSpot(GameTestHelper h){
        rabbitMotion=new LiteraryTests.Fixture(h);var f=rabbitMotion;var place=LabyrinthPlace.HOLY_RABBIT;
        var p=f.player("rabbit_motion",place);var b=f.base(place);f.at(p,place,3.5,0,-21.5);
        h.startSequence().thenWaitUntil(()->h.assertTrue(LiteraryVignettes.shared(f.data(),place).hasUUID("Father_"+p.getUUID())&&f.out.getEntity(LiteraryVignettes.shared(f.data(),place).getUUID("Father_"+p.getUUID())) instanceof LiteraryActor,"the real father has arrived"))
            .thenExecute(()->{
                var father=(LiteraryActor)f.out.getEntity(LiteraryVignettes.shared(f.data(),place).getUUID("Father_"+p.getUUID()));
                LiteraryVignettes.talk(p,father);h.assertTrue(p.containerMenu instanceof LiteraryChoiceMenu,"the stay uses the real personal choice");
                p.containerMenu.clicked(0,0,ClickType.PICKUP,p);p.closeContainer();
                h.assertTrue(p.getFoodData().getFoodLevel()==1,"commitment starts the persistent one-point hunger");
                var own=f.own(p,place);own.putInt("Night",1);own.putInt("NightTicks",35);LiteraryVignettes.save(f.data(),p.getUUID(),place,own);
                var start=father.position();var fatherId=father.getUUID();f.at(p,place,7.5,0,-21.5);f.look(p,b.offset(HolyRabbit.RABBIT));
                h.runAfterDelay(20,()->{
                    double first=father.position().distanceTo(start);h.assertTrue(first>.3&&first<2.5&&father.phase()==1,"the original father takes actual guarded steps through the shelter entrance");
                    var state=LiteraryVignettes.shared(f.data(),place);var rabbit=(Rabbit)f.out.getEntity(state.getUUID("Rabbit_"+p.getUUID()));
                    h.assertTrue(rabbit!=null&&HolyRabbit.owner(rabbit).orElseThrow().equals(p.getUUID()),"the original native rabbit carries the synced private-renderer marker");
                    rabbit.setCustomName(net.minecraft.network.chat.Component.literal("Snow"));var rabbitId=rabbit.getUUID();var rabbitAt=rabbit.position();
                    var next=f.own(p,place);next.putInt("Night",2);next.putInt("NightTicks",35);next.putInt("FatherWaypoint0488",1);LiteraryVignettes.save(f.data(),p.getUUID(),place,next);
                    father.moveTo(start);father.setDeltaMovement(Vec3.ZERO);
                    h.runAfterDelay(20,()->{
                        double second=father.position().distanceTo(start);h.assertTrue(second>.05&&second<first*.8&&father.phase()==2&&father.getUUID().equals(fatherId),"the same wounded father actually covers less ground on the second night");
                        h.assertTrue(!rabbit.isPushable()&&rabbit.getUUID().equals(rabbitId)&&rabbit.position().equals(rabbitAt)&&rabbit.getCustomName().getString().equals("Snow")&&HolyRabbit.owner(rabbit).orElseThrow().equals(p.getUUID()),"naming preserves its identity, words, owner marker and exact stationary position");
                        var finalNight=f.own(p,place);finalNight.putInt("Night",3);finalNight.putInt("NightTicks",35);LiteraryVignettes.save(f.data(),p.getUUID(),place,finalNight);var stopped=father.position();
                        h.runAfterDelay(20,()->{
                            h.assertTrue(father.phase()==3&&father.position().distanceTo(stopped)<.01&&father.getBbHeight()<1.3,"the final father cannot walk and has a physical collapsed body");
                            h.assertTrue(p.getFoodData().getFoodLevel()==1&&p.getTicksFrozen()>=60,"hunger and a native freezing overlay persist");
                            f.reload();h.assertTrue(LiteraryVignettes.shared(f.data(),place).getUUID("Father_"+p.getUUID()).equals(fatherId)&&LiteraryVignettes.shared(f.data(),place).getUUID("Rabbit_"+p.getUUID()).equals(rabbitId),"saved data reload preserves both exact original bodies");
                            f.arrive(p,LabyrinthPlace.MINIATURES);
                            h.runAfterDelay(10,()->{h.assertTrue(p.getTicksFrozen()==0,"leaving clears only the vignette's leased freezing effect");h.succeed();});
                        });
                    });
                });
            });
    }
    @AfterBatch(batch="notes488_rabbit_motion") public static void rabbitMotionDone(ServerLevel ignored){if(rabbitMotion!=null){rabbitMotion.close();rabbitMotion=null;}}

    @GameTest(template="empty",batch="notes488_rabbit_ending",timeoutTicks=1200)
    public static void seeingTheFathersWoundOpensAnEarnedTrailThatRemainsFinishableWithOneHunger(GameTestHelper h){
        rabbitEnding=new LiteraryTests.Fixture(h);var f=rabbitEnding;var place=LabyrinthPlace.HOLY_RABBIT;
        var p=f.player("rabbit_witness",place);var peer=f.player("rabbit_observer",place);var b=f.base(place);f.at(peer,place,15,0,-5);
        var own=f.own(p,place);own.putBoolean("Committed",true);own.putString("CommitAction","agreed_to_stay_with_father");own.putInt("Night",3);
        own.putInt("Meals",2);own.putInt("RabbitMornings",6);own.putBoolean("CarvingRead",true);LiteraryVignettes.save(f.data(),p.getUUID(),place,own);
        f.at(p,place,3.5,0,-20.5);p.setYRot(0);p.setXRot(0);
        h.startSequence().thenWaitUntil(()->{
            var world=LiteraryVignettes.shared(f.data(),place);
            h.assertTrue(world.hasUUID("Father_"+p.getUUID())&&f.out.getEntity(world.getUUID("Father_"+p.getUUID())) instanceof LiteraryActor,"the original father's actual native entity section has loaded before the injury observation");
        }).thenExecute(()->{
            h.assertTrue(!f.own(p,place).getBoolean("TrailOpen"),"earned meals alone do not skip the actual injury reveal");
            var father=(LiteraryActor)f.out.getEntity(LiteraryVignettes.shared(f.data(),place).getUUID("Father_"+p.getUUID()));
            p.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,father.position().add(.18,.45,0));
            h.runAfterDelay(125,()->{
                h.assertTrue(f.own(p,place).getBoolean("WoundSeen0488")&&f.own(p,place).getBoolean("TrailOpen")&&!father.isInvisible(),"the reader really sees the collapsed father's wound before he can disappear");
                f.at(p,place,.5,0,-119.5);p.setYRot(180);p.setXRot(0);
                h.startSequence().thenWaitUntil(()->{h.assertTrue(f.own(p,place).getBoolean("FatherGone"),"the original father has physically retreated on his poles: "+father.position());}).thenExecute(()->{
                    h.assertTrue(father.phase()==4&&!father.isInvisible()&&father.getZ()<b.getZ()-33,"the departure preserves the visible original body beyond the shelter");
                    h.assertTrue(f.own(p,place).getBoolean("Ready")&&p.getFoodData().getFoodLevel()==1,"the earned physical trail finishes without an impossible higher-food requirement");
                    h.assertTrue(!f.own(peer,place).getBoolean("WoundSeen0488")&&!f.own(peer,place).getBoolean("Ready"),"the other reader inherits neither reveal nor completion");h.succeed();
                });
            });
        });
    }
    @AfterBatch(batch="notes488_rabbit_ending") public static void rabbitEndingDone(ServerLevel ignored){if(rabbitEnding!=null){rabbitEnding.close();rabbitEnding=null;}}

    @GameTest(template="empty",batch="notes488_rabbit_nights",timeoutTicks=4200)
    public static void twoActualNightsReturnFiniteMealsWithoutTrappingFatherOnTheFoliage(GameTestHelper h){
        rabbitNights=new LiteraryTests.Fixture(h);var f=rabbitNights;var place=LabyrinthPlace.HOLY_RABBIT;
        var p=f.player("rabbit_nights",place);var b=f.base(place);f.at(p,place,3.5,0,-21.5);boolean[] started={false},done={false},carried={false},tossed={false};UUID[] rabbitId={null};float[] health={0};
        final Vec3[] previousFather={null},previousRabbit={null};final int[] throwFrames={0};
        h.onEachTick(()->{
            if(done[0])return;
            var world=LiteraryVignettes.shared(f.data(),place);if(!world.hasUUID("Father_"+p.getUUID()))return;
            var father=(LiteraryActor)f.out.getEntity(world.getUUID("Father_"+p.getUUID()));if(father==null)return;
            if(!started[0]){LiteraryVignettes.talk(p,father);p.containerMenu.clicked(0,0,ClickType.PICKUP,p);p.closeContainer();started[0]=true;}
            var own=f.own(p,place);int night=own.getInt("Night");
            if(previousFather[0]!=null)h.assertTrue(father.position().subtract(previousFather[0]).horizontalDistance()<=.091,"Father's actual weighted gait advances every native tick, without five-tick jumps");previousFather[0]=father.position();
            var rabbit=world.hasUUID("Rabbit_"+p.getUUID())?(Rabbit)f.out.getEntity(world.getUUID("Rabbit_"+p.getUUID())):null;
            if(rabbit!=null){if(rabbitId[0]==null){rabbitId[0]=rabbit.getUUID();health[0]=rabbit.getHealth();}h.assertTrue(rabbit.getUUID().equals(rabbitId[0])&&rabbit.isAlive()&&rabbit.getHealth()==health[0],"the apparent harvest preserves the original living rabbit and health");if(rabbit.getVehicle()==father)carried[0]=true;if(own.getInt("HarvestTicks0491")>35&&own.getInt("HarvestTicks0491")<75&&rabbit.getY()>b.getY()+.1)tossed[0]=true;}
            if(rabbit!=null){if(!rabbit.isPassenger()&&previousRabbit[0]!=null&&own.getInt("HarvestTicks0491")>40&&own.getInt("HarvestTicks0491")<75){double step=rabbit.position().distanceTo(previousRabbit[0]);h.assertTrue(step<=.2,"the original rabbit's native throw advances continuously: "+step);if(step>.005)throwFrames[0]++;}previousRabbit[0]=rabbit.position();}
            if(night<3&&own.getInt("NightTicks")<95){f.at(p,place,3.5,0,-17.5);f.look(p,b.offset(HolyRabbit.RABBIT));}
            else{f.at(p,place,3.5,0,-20.5);p.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,father.position().add(.18,.45,0));}
            for(var item:p.getInventory().items)if(item.is(LiteraryRegistry.MEAL.get())){
                h.assertTrue(night==own.getInt("MealNight")&&own.getInt("AteNight")!=night,"the present night issues exactly one fresh original meal (night="+night+", meal="+own.getInt("MealNight")+", ate="+own.getInt("AteNight")+", count="+item.getCount()+")");
                h.assertTrue(night>=3||own.getInt("HarvestDay0491")==night,"each finite supper follows the visible apparent harvest and return");
                item.getItem().finishUsingItem(item,f.out,p);h.assertTrue(p.getFoodData().getFoodLevel()==1,"the actual native consumption leaves hunger at one immediately");break;
            }
            own=f.own(p,place);
            if(night>=3&&father.phase()==3&&own.getInt("NightTicks")>=120){
                h.assertTrue(carried[0]&&tossed[0]&&throwFrames[0]>=40,"the actual native rabbit was held and tossed continuously before both mornings: "+throwFrames[0]);
                h.assertTrue(own.getInt("Meals")==2&&own.getInt("AteNight")==2,"both actual completed routes issued one finite meal and native eating counted each once");
                h.assertTrue(Integer.bitCount(own.getInt("RabbitMornings"))>=2,"the unchanged rabbit was really seen alive in both mornings");
                h.assertTrue(own.getBoolean("RabbitBelieved0490")&&own.getBoolean("RabbitTruth0490")&&!own.getBoolean("RabbitBelief0490"),"the observed returning named rabbit builds hope before the witnessed wound changes its explanation");h.assertTrue(own.getBoolean("WoundSeen0488")&&own.getBoolean("TrailOpen"),"the two actual nights lead to the physical injury reveal and earned escape");done[0]=true;h.succeed();
            }
        });
    }
    @AfterBatch(batch="notes488_rabbit_nights") public static void rabbitNightsDone(ServerLevel ignored){if(rabbitNights!=null){rabbitNights.close();rabbitNights=null;}}

    @GameTest(template="empty",batch="notes488_rabbit_shelter",timeoutTicks=1200)
    public static void oldShelterHeadroomWaitsForCamerasAndKeepsTheOriginalFatherTreeAndEdits(GameTestHelper h){
        rabbitShelter=new LiteraryTests.Fixture(h);var f=rabbitShelter;var place=LabyrinthPlace.HOLY_RABBIT;
        var p=f.player("old_rabbit_shelter",place);var b=f.base(place);var l=f.out;
        var father=LiteraryVignettes.actor(p,place,"Father",LiteraryActor.FATHER,new net.minecraft.core.BlockPos(3,0,-22),true);var fatherId=father.getUUID();
        var leaves=b.offset(3,1,-22);var edited=b.offset(4,1,-21);var carving=l.getBlockEntity(b.offset(0,3,-25));
        l.setBlock(leaves,Blocks.SPRUCE_LEAVES.defaultBlockState(),18);l.setBlock(edited,Blocks.STONE.defaultBlockState(),18);
        var old=f.data().state("rabbit_shelter0488");old.remove(Long.toString(b.asLong()));f.data().setState("rabbit_shelter0488",old);
        f.chunks.hold(l,new net.minecraft.world.phys.AABB(Vec3.atLowerCornerOf(b.offset(3,0,-23)),Vec3.atLowerCornerOf(b.offset(5,3,-17))));
        p.setGameMode(GameType.SPECTATOR);f.at(p,place,3.5,0,-20.5);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the old shelter's actual entity sections are ready")).thenExecute(()->{
            h.assertTrue(!HolyRabbit.repairShelter(l,b)&&l.getBlockState(leaves).is(Blocks.SPRUCE_LEAVES),"a real spectator camera prevents the saved opening from changing");
            p.moveTo(b.getX()+100,b.getY(),b.getZ()+100);
            h.assertTrue(HolyRabbit.repairShelter(l,b)&&l.getBlockState(leaves).isAir(),"when unwatched, clearing expected foliage safely creates headroom around the original father");
            h.assertTrue(l.getBlockState(edited).is(Blocks.STONE)&&l.getBlockEntity(b.offset(0,3,-25))==carving&&father.getUUID().equals(fatherId)&&!father.isRemoved(),"the edit, original carving block entity and father's exact body survive");
            l.setBlock(leaves,Blocks.SPRUCE_LEAVES.defaultBlockState(),18);
            h.assertTrue(HolyRabbit.repairShelter(l,b)&&l.getBlockState(leaves).is(Blocks.SPRUCE_LEAVES),"the saved checkpoint prevents repeatedly clearing later edits");
        }).thenSucceed();
    }
    @AfterBatch(batch="notes488_rabbit_shelter") public static void rabbitShelterDone(ServerLevel ignored){if(rabbitShelter!=null){rabbitShelter.close();rabbitShelter=null;}}

    // ---------------------------------------------------------------- 0.4.101 Holy Rabbit pass
    private static void commit(LiteraryTests.Fixture f,net.minecraft.server.level.ServerPlayer p,int night,int ticks,java.util.function.Consumer<CompoundTag> extra){
        var own=f.own(p,LabyrinthPlace.HOLY_RABBIT);own.putBoolean("Committed",true);own.putString("CommitAction","agreed_to_stay_with_father");
        own.putInt("Night",night);own.putInt("NightTicks",ticks);extra.accept(own);LiteraryVignettes.save(f.data(),p.getUUID(),LabyrinthPlace.HOLY_RABBIT,own);
    }
    private static String text(ItemStack book){var out=new StringBuilder();for(var page:book.get(net.minecraft.core.component.DataComponents.WRITTEN_BOOK_CONTENT).pages())out.append(page.raw().getString()).append(' ');return out.toString();}

    @GameTest(template="empty",batch="rabbit_pass_floor",timeoutTicks=400)
    public static void theColdAndFathersShakingNeverTakeTheLastThreeHearts(GameTestHelper h){
        rabbitFloor=new LiteraryTests.Fixture(h);var f=rabbitFloor;var place=LabyrinthPlace.HOLY_RABBIT;
        var p=f.player("rabbit_floor",place);commit(f,p,1,400,own->own.putInt("Drowse",300));
        f.at(p,place,5.5,0,-21.5);p.setHealth(7);
        h.startSequence().thenWaitUntil(()->h.assertTrue(LiteraryVignettes.shared(f.data(),place).hasUUID("Father_"+p.getUUID())&&f.out.getEntity(LiteraryVignettes.shared(f.data(),place).getUUID("Father_"+p.getUUID())) instanceof LiteraryActor,"the real father has arrived"))
            .thenIdle(40).thenExecute(()->{
                h.assertTrue(f.own(p,place).getInt("Drowse")<320&&p.getHealth()>=HolyRabbit.FLOOR,"Father still shakes the drowsing child awake, and the shaking leaves three hearts: "+p.getHealth());
                h.assertTrue(p.getTicksFrozen()>=100&&p.getTicksFrozen()<p.getTicksRequiredToFreeze(),"outside the shelter the native frost stays just short of the freeze that hurts: "+p.getTicksFrozen());
                f.at(p,place,.5,0,-40.5);
            })
            .thenIdle(60).thenExecute(()->{
                h.assertTrue(p.getHealth()>=HolyRabbit.FLOOR&&f.own(p,place).getBoolean("TooSoon0501"),"before the trail opens the wind past the trees turns the child back without taking the last hearts: "+p.getHealth());
                h.succeed();
            });
    }
    @AfterBatch(batch="rabbit_pass_floor") public static void rabbitFloorDone(ServerLevel ignored){if(rabbitFloor!=null){rabbitFloor.close();rabbitFloor=null;}}

    @GameTest(template="empty",batch="rabbit_pass_trail",timeoutTicks=900)
    public static void theEscapeColdWaitsForFathersRetreatAndTheCarvingIsRememberedNotRequired(GameTestHelper h){
        rabbitTrail=new LiteraryTests.Fixture(h);var f=rabbitTrail;var place=LabyrinthPlace.HOLY_RABBIT;
        var p=f.player("rabbit_trail",place);var b=f.base(place);
        commit(f,p,3,400,own->{own.putInt("Meals",2);own.putInt("AteNight",2);own.putInt("MealNight",3);own.putInt("RabbitMornings",6);own.putBoolean("WoundSeen0488",true);});
        var carving=new Vec3(b.getX()+.5,b.getY()+3.3,b.getZ()-23.95);
        h.assertTrue(f.out.getBlockState(b.offset(0,3,-24)).isAir(),"the carved words face open air, not the canopy's leaves");
        f.at(p,place,.5,0,-22.5);p.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,carving);
        h.startSequence().thenIdle(15).thenExecute(()->{
            h.assertTrue(!f.own(p,place).getBoolean("CarvingAppeared"),"nothing is cut into the tree while the reader is watching it");
            p.setYRot(0);p.setXRot(0);
        }).thenIdle(15).thenExecute(()->{
            var now=f.own(p,place);
            h.assertTrue(now.getBoolean("CarvingAppeared")&&!now.getBoolean("CarvingRead"),"on the last day the carving appears the first moment nobody is looking at it");
            h.assertTrue(now.getBoolean("TrailOpen"),"the seen wound, two meals and two mornings open the trail");
            f.at(p,place,.5,0,-40.5);p.setYRot(180);
        }).thenIdle(40).thenExecute(()->{
            var now=f.own(p,place);
            h.assertTrue(now.getBoolean("LastHunt0490")&&!now.getBoolean("FatherGone")&&now.getInt("TrailTicks")==0,"the escape cold waits while Father is still going: "+now.getInt("TrailTicks"));
            now.putBoolean("FatherGone",true);now.putInt("LastHuntStep0490",99);LiteraryVignettes.save(f.data(),p.getUUID(),place,now);
        }).thenIdle(30).thenExecute(()->{
            h.assertTrue(f.own(p,place).getInt("TrailTicks")>0,"once he has gone, the trail's cold starts counting");
            f.at(p,place,.5,0,-21.5);p.setYRot(0);
        }).thenIdle(15).thenExecute(()->{
            h.assertTrue(f.own(p,place).getInt("TrailTicks")==0,"back under the tree the cold starts over");
            f.at(p,place,.5,0,-119.5);p.setYRot(180);
        }).thenWaitUntil(()->h.assertTrue(f.own(p,place).getBoolean("Ready")&&!f.own(p,place).getBoolean("CarvingRead"),"the way out does not depend on having looked up at the carving"))
        .thenExecute(()->{
            var unread=f.own(p,place);unread.remove("CarvingRead");var read=unread.copy();read.putBoolean("CarvingRead",true);
            var plain=text(LiteraryTexts.ending(p,place,unread));
            h.assertTrue(plain.contains("Slaney")&&!plain.contains("initials"),"the account names the rabbit and leaves out a carving the reader never looked at");
            h.assertTrue(text(LiteraryTexts.ending(p,place,read)).contains("initials"),"a carving the reader did look up at is remembered in the account");
            h.succeed();
        });
    }
    @AfterBatch(batch="rabbit_pass_trail") public static void rabbitTrailDone(ServerLevel ignored){if(rabbitTrail!=null){rabbitTrail.close();rabbitTrail=null;}}

    @GameTest(template="empty",batch="rabbit_pass_morning",timeoutTicks=600)
    public static void fatherWaitsForTheReaderToSeeTheRabbitBeforeHeTakesIt(GameTestHelper h){
        rabbitMorning=new LiteraryTests.Fixture(h);var f=rabbitMorning;var place=LabyrinthPlace.HOLY_RABBIT;
        var p=f.player("rabbit_morning",place);var b=f.base(place);
        commit(f,p,1,120,own->own.putInt("FatherWaypoint0488",4));
        // East of the rabbit, facing further east: it is behind the reader.
        f.at(p,place,3.5,0,-17.5);p.setYRot(-90);p.setXRot(0);
        h.startSequence().thenIdle(60).thenExecute(()->{
            var now=f.own(p,place);
            h.assertTrue(now.getInt("HarvestTicks0491")==0&&now.getInt("RabbitWait0501")>0&&(now.getInt("RabbitMornings")&2)==0,"Father will not take the rabbit before the reader has seen it alive this morning");
            f.look(p,b.offset(HolyRabbit.RABBIT));
        }).thenWaitUntil(()->h.assertTrue((f.own(p,place).getInt("RabbitMornings")&2)!=0&&f.own(p,place).getInt("HarvestTicks0491")>0,"once the reader has looked, the morning counts and the apparent harvest begins"))
        .thenExecute(h::succeed);
    }
    @AfterBatch(batch="rabbit_pass_morning") public static void rabbitMorningDone(ServerLevel ignored){if(rabbitMorning!=null){rabbitMorning.close();rabbitMorning=null;}}
}
