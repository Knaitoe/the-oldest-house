package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseBlocks;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.UUID;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
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
    private static LiteraryTests.Fixture rabbitMotion,rabbitEnding,rabbitNights,rabbitShelter;

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
        h.startSequence().thenWaitUntil(()->{
            var mine=f.own(p,place);var theirs=f.own(peer,place);
            h.assertTrue(mine.hasUUID("CastFather")&&mine.hasUUID("CastSergeant")&&theirs.hasUUID("CastFather")
                &&l.getEntity(mine.getUUID("CastFather")) instanceof LiteraryActor
                &&l.getEntity(mine.getUUID("CastSergeant")) instanceof LiteraryActor
                &&l.getEntity(theirs.getUUID("CastFather")) instanceof LiteraryActor,"the real private cottage cast has loaded");
        }).thenExecute(()->{
            var mine=f.own(p,place);var theirs=f.own(peer,place);
            var father=(LiteraryActor)l.getEntity(mine.getUUID("CastFather"));var sergeant=(LiteraryActor)l.getEntity(mine.getUUID("CastSergeant"));
            var peerFather=(LiteraryActor)l.getEntity(theirs.getUUID("CastFather"));var fatherId=father.getUUID();
            f.at(peer,place,-6,0,-9);LiteraryVignettes.talk(peer,peerFather);
            f.at(p,place,-6,0,-9);LiteraryVignettes.talk(p,father);
            h.assertTrue(father.hasCustomName()&&peerFather.hasCustomName(),"each household may speak independently");
            LiteraryVignettes.talk(p,sergeant);
            h.assertTrue(sergeant.hasCustomName()&&!father.hasCustomName()&&peerFather.hasCustomName(),
                "the soldier's next line replaces this household's bubble without erasing a peer's speech");
            mine=f.own(p,place);mine.putInt("Stage",ThreeFingers.WISHED);mine.putInt("WishVisit",mine.getInt("Visit"));
            LiteraryVignettes.save(f.data(),p.getUUID(),place,mine);LiteraryVignettes.talk(p,father);
            h.assertTrue(father.getCustomName()!=null&&father.getCustomName().getString().contains("It moved")&&!sergeant.hasCustomName(),
                "the same father speaks the actual later-stage response");
            father.getPersistentData().putLong("SayUntil0488",l.getGameTime()-1);
            ThreeFingers.reader(p,f.base(place),f.own(p,place));
            h.assertTrue(!father.hasCustomName()&&!father.isCustomNameVisible()&&father.getUUID().equals(fatherId)&&peerFather.hasCustomName(),
                "expired speech clears the native hover name, preserving actor identity and the peer's bubble");
        }).thenSucceed();
    }
    @AfterBatch(batch="notes488_paw_speech") public static void speechDone(ServerLevel ignored){if(speech!=null){speech.close();speech=null;}}

    @GameTest(template="empty",batch="notes488_rabbit_motion",timeoutTicks=250)
    public static void theOriginalFatherHobblesLessEachNightAndTheNamedRabbitNeverChangesItsSpot(GameTestHelper h){
        rabbitMotion=new LiteraryTests.Fixture(h);var f=rabbitMotion;var place=LabyrinthPlace.HOLY_RABBIT;
        var p=f.player("rabbit_motion",place);var b=f.base(place);f.at(p,place,3.5,0,-21.5);
        h.startSequence().thenWaitUntil(()->h.assertTrue(LiteraryVignettes.shared(f.data(),place).hasUUID("Father_"+p.getUUID()),"the real father has arrived"))
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
        h.runAfterDelay(10,()->{
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
        h.onEachTick(()->{
            if(done[0])return;
            var world=LiteraryVignettes.shared(f.data(),place);if(!world.hasUUID("Father_"+p.getUUID()))return;
            var father=(LiteraryActor)f.out.getEntity(world.getUUID("Father_"+p.getUUID()));if(father==null)return;
            if(!started[0]){LiteraryVignettes.talk(p,father);p.containerMenu.clicked(0,0,ClickType.PICKUP,p);p.closeContainer();started[0]=true;}
            var own=f.own(p,place);int night=own.getInt("Night");
            var rabbit=world.hasUUID("Rabbit_"+p.getUUID())?(Rabbit)f.out.getEntity(world.getUUID("Rabbit_"+p.getUUID())):null;
            if(rabbit!=null){if(rabbitId[0]==null){rabbitId[0]=rabbit.getUUID();health[0]=rabbit.getHealth();}h.assertTrue(rabbit.getUUID().equals(rabbitId[0])&&rabbit.isAlive()&&rabbit.getHealth()==health[0],"the apparent harvest preserves the original living rabbit and health");if(rabbit.getVehicle()==father)carried[0]=true;if(own.getInt("HarvestTicks0491")>35&&own.getInt("HarvestTicks0491")<75&&rabbit.getY()>b.getY()+.1)tossed[0]=true;}
            if(night<3&&own.getInt("NightTicks")<95){f.at(p,place,3.5,0,-17.5);f.look(p,b.offset(HolyRabbit.RABBIT));}
            else{f.at(p,place,3.5,0,-20.5);p.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,father.position().add(.18,.45,0));}
            for(var item:p.getInventory().items)if(item.is(LiteraryRegistry.MEAL.get())){
                h.assertTrue(night==own.getInt("MealNight")&&own.getInt("AteNight")!=night,"the present night issues exactly one fresh original meal (night="+night+", meal="+own.getInt("MealNight")+", ate="+own.getInt("AteNight")+", count="+item.getCount()+")");
                h.assertTrue(night>=3||own.getInt("HarvestDay0491")==night,"each finite supper follows the visible apparent harvest and return");
                item.getItem().finishUsingItem(item,f.out,p);h.assertTrue(p.getFoodData().getFoodLevel()==1,"the actual native consumption leaves hunger at one immediately");break;
            }
            own=f.own(p,place);
            if(night>=3&&father.phase()==3&&own.getInt("NightTicks")>=120){
                h.assertTrue(carried[0]&&tossed[0],"the actual native rabbit was held and tossed before the next morning");
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
}
