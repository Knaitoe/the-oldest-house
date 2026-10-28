package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.PlaytestLog;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID+"_goatman_entry")
@PrefixGameTestTemplate(false)
public final class GoatmanBedtimeTests {
    private static GoatmanTests.Fixture fixture;
    @AfterBatch(batch="goat_bedside131") public static void cleanHaunt(ServerLevel ignored){clean();}
    @AfterBatch(batch="goat_bed_morning131") public static void cleanMorning(ServerLevel ignored){clean();}
    private static void clean(){if(fixture!=null){fixture.close();fixture=null;}}
    private static void prepare(GoatmanTests.Fixture f,List<ServerPlayer> readers,boolean open){
        for(var p:readers)GoatmanVignette.enter(p);
        var r=f.run();GoatmanTests.Fixture.extras(r);r.putInt("Wrong",0);r.putInt("Runner",7);r.putInt("Phase",GoatmanVignette.VIGIL);r.putInt("Clock",1200);r.putInt("ExtraState",GoatmanVignette.X_DOOR);r.putInt("RunnerState",GoatmanVignette.R_INSIDE);
        r.putInt("StalkLeg0465",99);r.putInt("StalkWindowLeg0465",99);r.putInt("Circuits0494",GoatmanStalking.CIRCUITS);r.putInt("CircuitGoal0494",GoatmanStalking.CIRCUITS);r.putBoolean("AssaultDone0465",true);r.putBoolean("DoorPressureDone0497",true);r.putInt("DemandStage0465",7);r.putInt("Demand",107);r.putInt("DemandAt",1200);GoatmanFear.begin(r);r.putInt("FearLine0472",21);r.putInt("FearLease0472",100);r.putIntArray("FearQueue0472",new int[]{2,8});r.putInt("FearPending0472",22);
        f.cohort(r,readers,1200);f.run(r);GoatmanVignette.stage(f.l,f.b,r);GoatmanWoods.setWindow(f.l,f.b,!open);GoatmanWoods.door(f.l,f.b,false);
        var original=f.cousin(0).orElseThrow();original.disguise(3);original.moveTo(f.b.getX()+10.25,f.b.getY(),f.b.getZ()-74.5,90,0);
    }
    private static void bed(GoatmanTests.Fixture f,ServerPlayer p,int index){var head=GoatmanWoods.bunkHead(index);p.moveTo(f.b.getX()+(index%2==0?-3.5:4.5),f.b.getY()+1,f.b.getZ()+head.getZ()+.5);f.use(p,head);}
    private static void reload(GoatmanTests.Fixture f){var d=LabyrinthData.get(f.server);var restored=LabyrinthData.FACTORY.deserializer().apply(d.save(new CompoundTag(),f.l.registryAccess()),f.l.registryAccess());f.server.overworld().getDataStorage().set("the_oldest_house_labyrinth",restored);}

    @GameTest(template="empty",batch="goat_bedside131",timeoutTicks=4800)
    public static void theOriginalWindowEntrantLaughsLiesDownTwiceCrawlsBelowTwoRealBedViewsAndHauntsOnlyAtEachBedside(GameTestHelper h){
        fixture=new GoatmanTests.Fixture(h,57100);var f=fixture;f.finishedCamper();var p=f.nativePlayer("bedside_first");var peer=f.nativePlayer("bedside_peer");f.chunks.hold(f.l,IndianLakeRooms.bounds(f.b,LabyrinthPlace.GOATMAN));
        var respawn=new BlockPos(14,75,29);p.setRespawnPosition(Level.OVERWORLD,respawn,37,true,false);peer.setRespawnPosition(Level.OVERWORLD,respawn.east(),81,true,false);
        UUID[] id={null};boolean[] begun={false},slept={false},loaded={false},leftBath={false};var stages=new HashSet<Integer>();int[] ticks={0};
        h.onEachTick(()->{
            if(!begun[0])return;var r=f.run();if(r.getInt("Phase")==GoatmanVignette.DAWN)return;var c=f.cousin(0).orElseThrow();
            h.assertTrue(c.getUUID().equals(id[0])&&!c.noPhysics,"every window, bed and crawl beat keeps the original colliding body");
            if(r.contains("BedQuietAt131")&&!r.getBoolean("BedtimeCalled131"))h.assertTrue(r.getInt("Clock")-r.getInt("BedQuietAt131")<=GoatmanBedtime.QUIET_TICKS,"the quiet interval is bounded before bedtime");
            if(r.getInt("ExtraState")==GoatmanVignette.X_WINDOW&&f.rel(c).x<3.1){leftBath[0]=true;h.assertTrue(c.indoors(),"eye glow is disabled at the actual bathroom exit");}
            if(r.getInt("BedStage131")<5&&r.getInt("ExtraState")!=GoatmanVignette.X_DOOR)h.assertTrue(f.rel(c).z< -60,"window admission and its bunk route never detour to the front door: "+f.rel(c));
            if(r.getBoolean("BedtimeCalled131")&&!slept[0]){h.assertTrue(r.getInt("Clock")-r.getInt("BedQuietAt131")>=GoatmanBedtime.QUIET_TICKS,"twelve quiet seconds precede the first bed invitation");bed(f,p,8);bed(f,peer,9);h.assertTrue(p.isSleeping()&&peer.isSleeping(),"both readers use actual available native beds");slept[0]=true;}
            int stage=r.getInt("BedStage131");stages.add(stage);
            if(stage==2&&c.isSleeping()&&!loaded[0]){c.load(c.saveWithoutId(new CompoundTag()));reload(f);loaded[0]=true;}
            if(stage==5&&c.crawling()){var sleeping=p.isSleeping()?p:peer;h.assertTrue(c.hasPose(Pose.SWIMMING)&&c.getBoundingBox().maxY<sleeping.getEyeY()&&c.isInvisible(),"0.4.145: the ground crawl stays below the real in-bed eye height and out of sight altogether");}
            if(stage==6)h.assertTrue(!c.isInvisible()&&!c.crawling()&&c.hasPose(Pose.STANDING),"it jumps up visible at the foot of the bed");
            if(stage>=1)h.assertTrue(GoatmanBedtime.risen(r),"the in-bed chat fades once it has first got up");
            var own=r.getCompound("Cohort").getCompound(p.getUUID().toString());var other=r.getCompound("Cohort").getCompound(peer.getUUID().toString());
            if(GoatmanBedtime.quiet(r)){
                var payload=GoatmanVignette.scenePayload(r);h.assertTrue(payload.demand()==0&&payload.cousinLine()==0&&!GoatmanVignette.speaking(r)&&r.getIntArray("FearQueue0472").length==0&&r.getInt("FearPending0472")==0,"both the black-backed demand and the interrupted fear queue stay cleared throughout the sleep discussion");
            }
            if(slept[0])for(var reader:List.of(p,peer))if(!r.getCompound("Cohort").getCompound(reader.getUUID().toString()).getBoolean("BedsideEnded131")){
                reader.connection.handlePlayerCommand(new net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket(reader,net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action.STOP_SLEEPING));
                reader.stopSleepInBed(true,true);
                h.assertTrue(reader.isSleeping()&&GoatmanBedtime.locked(reader),"native Leave Bed packets and automatic waking cannot interrupt a committed reader, including after their bedside beat");
                var direction=reader.getBedOrientation();var head=reader.getSleepingPos().orElseThrow();
                h.assertTrue(direction!=null&&direction.getOpposite().getStepX()*(f.b.getX()+.5-head.getX()-.5)>0,"the native pillows stay by the walls and both views look over their feet toward the actual center table");
                h.assertTrue(!GoatmanVignette.personal(LabyrinthData.get(f.server),reader.getUUID()).getBoolean("EncounterEnded131"),"a reader still in bed has not been carried off because a peer's bedside beat is finished");
            }
            for(var sleeper:f.cousins())if(sleeper.isSleeping())h.assertTrue(!sleeper.showsHeldItems()&&sleeper.getMainHandItem().is(net.minecraft.world.item.Items.LEATHER),"sleep hides the original held item without deleting its inventory stack");
            for(var reader:List.of(p,peer)){var record=r.getCompound("Cohort").getCompound(reader.getUUID().toString());if(GoatmanHaunt.haunted(LabyrinthData.get(f.server),reader.getUUID()))h.assertTrue(record.getBoolean("BedsideEnded131"),"haunting follows that reader's actual bedside reveal, never merely entering the camper");}
            if(++ticks[0]%100==0)TheOldestHouse.LOGGER.info("GOATMAN BEDTIME: clock={} state={} stage={} beat={} feet={} sleep={}/{}",r.getInt("Clock"),r.getInt("ExtraState"),stage,r.getInt("BedBeat131"),f.rel(c),p.isSleeping(),peer.isSleeping());
        });
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the complete camper and native actors are loaded")).thenExecute(()->{prepare(f,List.of(p,peer),true);for(var child:f.cousins())child.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.LEATHER));id[0]=f.cousin(0).orElseThrow().getUUID();begun[0]=true;})
            .thenWaitUntil(()->h.assertTrue(List.of(p,peer).stream().allMatch(reader->GoatmanVignette.personal(LabyrinthData.get(f.server),reader.getUUID()).getBoolean("EncounterEnded131")),"both actual bedside endings finish: stage="+f.run().getInt("BedStage131")+" feet="+f.cousin(0).map(f::rel)))
            .thenExecute(()->{
                h.assertTrue(!p.isSleeping()&&!peer.isSleeping()&&!GoatmanBedtime.locked(p)&&!GoatmanBedtime.locked(peer),"each bedside ending releases its reader");
                h.assertTrue(!GoatmanVignette.inside(p)&&!GoatmanVignette.inside(peer)&&List.of(p,peer).stream().allMatch(reader->f.run().getCompound("Cohort").getCompound(reader.getUUID().toString()).getBoolean("Failed")),"0.4.145: each reader goes black and out of the trailer, their night over");
                h.assertTrue(f.run().getInt("BedLaughs131")>=GoatmanBedtime.LAUGHS,"it stands laughing "+GoatmanBedtime.LAUGHS+" times before it crawls to a bed");
                h.assertTrue(leftBath[0]&&loaded[0]&&stages.containsAll(List.of(1,2,3,4,5,6,7)),"the same saved actor leaves the bathroom, laughs, lies down, repeats, crawls and rises beside each actual bed");
                for(var reader:List.of(p,peer))h.assertTrue(GoatmanHaunt.haunted(LabyrinthData.get(f.server),reader.getUUID())&&GoatmanVignette.personal(LabyrinthData.get(f.server),reader.getUUID()).getBoolean("EncounterEnded131")&&!WitnessAccount.has(LabyrinthData.get(f.server),reader.getUUID(),WitnessAccount.Story.GOATMAN),"the bedside haunt concludes personally while preserving the successful-vigil Witness rule");
                h.assertTrue(respawn.equals(p.getRespawnPosition())&&respawn.east().equals(peer.getRespawnPosition())&&p.getRespawnDimension()==Level.OVERWORLD&&p.getRespawnAngle()==37&&peer.getRespawnAngle()==81,"neither bed changes either reader's saved respawn location, dimension or angle");
                h.assertTrue(f.children().stream().noneMatch(c->c.getTags().contains("TrailerWalk")),"the bedside ending does not mint another child for an obsolete dawn haunting");
            }).thenSucceed();
    }

    @GameTest(template="empty",batch="goat_bed_morning131",timeoutTicks=1800)
    public static void keepingItOutGivesQuietCousinDialogueRealSleepAndTheOriginalCousinsWalkingTheMorningTrail(GameTestHelper h){
        fixture=new GoatmanTests.Fixture(h,57400);var f=fixture;f.finishedCamper();var p=f.nativePlayer("bed_morning");f.chunks.hold(f.l,IndianLakeRooms.bounds(f.b,LabyrinthPlace.GOATMAN));
        var respawn=new BlockPos(73,80,32);p.setRespawnPosition(Level.OVERWORLD,respawn,16,true,false);var originals=new HashSet<UUID>();boolean[] begun={false},slept={false};int[] ticks={0};
        h.onEachTick(()->{
            if(!begun[0])return;var r=f.run();
            if(r.getBoolean("BedtimeCalled131")&&!slept[0]){bed(f,p,8);h.assertTrue(p.isSleeping(),"the successful reader enters their actual bed");slept[0]=true;}
            h.assertTrue(!GoatmanHaunt.haunted(LabyrinthData.get(f.server),p.getUUID()),"a shut bathroom window keeps this reader unhaunted");
            if(r.getInt("Phase")==GoatmanVignette.VIGIL)h.assertTrue(!WitnessAccount.has(LabyrinthData.get(f.server),p.getUUID(),WitnessAccount.Story.GOATMAN),"survival is awarded only after the actual night's sleep");
            if(++ticks[0]%100==0)TheOldestHouse.LOGGER.info("GOATMAN MORNING: phase={} called={} dialogue={} sleeping={} trail={}",r.getInt("Phase"),r.getBoolean("BedtimeCalled131"),r.getInt("BedTalk131"),f.cousins().stream().filter(GoatmanChild::isSleeping).count(),f.cousins().stream().filter(c->c.getPersistentData().getBoolean("MorningTrail131")).count());
        });
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the production camper collision sections are loaded")).thenExecute(()->{prepare(f,List.of(p),false);for(var c:f.cousins())if(c.getPersistentData().getInt(GoatmanVignette.INDEX)!=0)originals.add(c.getUUID());begun[0]=true;})
            .thenWaitUntil(()->h.assertTrue(f.run().getInt("Phase")==GoatmanVignette.DAWN,"the closed-window night reaches morning after sleep and settling dialogue"))
            .thenExecute(()->{
                var r=f.run();h.assertTrue(r.getBoolean("Right")&&r.getInt("BedTalk131")==4&&WitnessAccount.has(LabyrinthData.get(f.server),p.getUUID(),WitnessAccount.Story.GOATMAN),"the quiet interval and all four spaced settling lines precede the successful dawn");
                h.assertTrue(f.cousins().stream().map(GoatmanChild::sleepPose).distinct().count()>=3,"the original cousins keep at least three distinct stable sleeping positions");
                h.assertTrue(f.cousins().stream().allMatch(GoatmanChild::isSleeping)&&originals.equals(f.cousins().stream().map(Entity::getUUID).collect(java.util.stream.Collectors.toSet())),"all seven real original cousins actually slept, even though the extra child was excluded");
                h.assertTrue(respawn.equals(p.getRespawnPosition())&&p.getRespawnAngle()==16&&!p.isSleeping(),"morning wakes the reader while keeping the prior respawn");f.at(p,51);
            }).thenWaitUntil(()->h.assertTrue(f.cousins().stream().allMatch(c->c.getPersistentData().getBoolean("MorningTrail131")),"the original cousins physically get out of bed, through the door and down the finished porch"))
            .thenExecute(()->{h.assertTrue(originals.equals(f.cousins().stream().map(Entity::getUUID).collect(java.util.stream.Collectors.toSet())),"the morning companions are the original cousins");f.at(p,38);})
            .thenWaitUntil(()->h.assertTrue(f.run().getInt("MorningLine131")>=1&&f.cousins().stream().anyMatch(c->GoatmanWoods.project(f.rel(c)).progress()<58),"the original companions walk the real trail and speak to the party"))
            .thenSucceed();
    }

}
