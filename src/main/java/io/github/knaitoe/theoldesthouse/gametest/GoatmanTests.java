package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import io.github.knaitoe.theoldesthouse.opening.CompanionOrders;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.util.Mth;
import net.minecraft.world.*;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class GoatmanTests {
    private static Fixture supper;
    @GameTest(template="empty",batch="goat_supper",timeoutTicks=100)
    public static void freshPacketsOpenIntoFourFiniteFranksAndPlayersServeWithoutFreezingAnEvening(GameTestHelper h){
        supper=new Fixture(h,28600);var f=supper;var p=f.player();h.assertTrue(GoatmanVignette.enter(p),"the child joins a fresh evening");
        var r=f.run();h.assertTrue(GoatmanVignette.count(r)==8,"a fresh round stages eight apparent cousins");
        r.putInt("Phase",GoatmanVignette.GATHERING);r.putInt("Clock",GoatmanVignette.SUPPER+20);r.putInt("Expected",8);r.putInt("Pan",0);f.cohort(r,List.of(p),0);f.run(r);
        p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);f.click(p,GoatmanWoods.STOVE);
        h.assertTrue(p.getMainHandItem().is(GoatmanRegistry.FRANKS.get())&&f.run().getInt("PacketsIssued0464")==1,"a real stove interaction gives one finite unopened pack");
        f.click(p,GoatmanWoods.STOVE);h.assertTrue(p.getMainHandItem().is(GoatmanRegistry.BRAT.get())&&p.getMainHandItem().getCount()==4,"opening the held pack produces exactly four franks in that hand");
        f.click(p,new BlockPos(-1,1,-61));f.click(p,new BlockPos(-1,1,-61));
        h.assertTrue(p.getMainHandItem().getCount()==3&&Integer.bitCount(f.run().getInt("Plated0464"))==1,"serving consumes one frank; clicking the same plate cannot double serve it");
        f.click(p,GoatmanWoods.STOVE);p.getInventory().selected=1;h.assertTrue(p.getMainHandItem().is(GoatmanRegistry.FRANKS.get()),"the second pack is still unopened");f.click(p,GoatmanWoods.STOVE);
        h.assertTrue(p.getInventory().countItem(GoatmanRegistry.BRAT.get())==7,"two opened packs contain eight total franks including the plated one");
        f.click(p,GoatmanWoods.STOVE);h.assertTrue(f.run().getInt("PacketsIssued0464")==2&&p.getInventory().countItem(GoatmanRegistry.FRANKS.get())==0,"the wrappers cannot replenish supplies");
        GoatmanVignette.hurry(f.l,f.b);h.assertTrue(Integer.bitCount(f.run().getInt("Plated0464"))==1,"cousin arrivals never serve food for the player");
        r=f.run();r.putInt("Clock",200);f.run(r);var late=f.player();h.assertTrue(GoatmanVignette.enter(late),"an early latecomer can still join");
        GoatmanVignette.tick(f.server);h.assertTrue(f.run().getInt("Clock")>200,"an evening already underway advances while an early latecomer walks the path");h.succeed();
    }
    @AfterBatch(batch="goat_supper") public static void supperDone(ServerLevel level){if(supper!=null){supper.close();supper=null;}}
    @GameTest(template="empty") public static void woodsAppendWithoutMovingOldRoomsOrReinterpretingLeaks(GameTestHelper h){
        for(int y:new int[]{65,80,150,250}){
            var origin=new BlockPos(100,y,100);var b=LabyrinthPlaces.base(origin,LabyrinthPlace.GOATMAN);var slot=LabyrinthPlaces.slotBounds(origin,LabyrinthPlace.GOATMAN);
            h.assertTrue(slot.isInside(b.offset(-23,-1,-82))&&slot.isInside(b.offset(23,13,0)),"woods and trailer fit their native slot at manor height "+y);
        }
        h.assertTrue(LabyrinthPlace.PHONE_CANOE.slot()==30&&LabyrinthPlace.GOATMAN.slot()==31&&DoorLeakKind.CANOE.ordinal()==10&&DoorLeakKind.WOODS.ordinal()==11,"the new site and sensory hint append to the existing saved layout");
        h.assertTrue(WitnessAccount.REQUIRED==33&&WitnessAccount.Story.values().length==43,"the current forty-three playable sources require thirty-three personal resolutions");h.succeed();
    }
    private static final class Fixture implements AutoCloseable {
        final GameTestHelper h;final net.minecraft.server.MinecraftServer server;final ServerLevel l;final BlockPos origin,b;
        final HouseSavedData oldHouse;final LabyrinthData oldData;final MotherCollection oldMother;final boolean keep;
        final List<ServerPlayer> players=new ArrayList<>();
        Fixture(GameTestHelper h,int coordinate){
            this.h=h;server=h.getLevel().getServer();l=HouseTestLevel.get(server);origin=new BlockPos(coordinate,80,coordinate);
            oldHouse=HouseSavedData.get(server);oldData=LabyrinthData.get(server);oldMother=MotherCollection.get(server);keep=l.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY);
            var house=new HouseSavedData();house.markSpawned(origin);server.overworld().getDataStorage().set("the_oldest_house",house);
            var data=new LabyrinthData();data.setBuilt(LabyrinthBuilder.VERSION,origin);server.overworld().getDataStorage().set("the_oldest_house_labyrinth",data);
            server.overworld().getDataStorage().set("the_oldest_house_mother",new MotherCollection());
            b=LabyrinthPlaces.base(origin,LabyrinthPlace.GOATMAN);GoatmanVignette.build(server,l,b);LabyrinthBuilder.registerDoors(data,LabyrinthPlace.GOATMAN,b);IndianLakeRooms.keepLoaded(l,b,LabyrinthPlace.GOATMAN);
        }
        ServerPlayer player(){var p=h.makeMockServerPlayerInLevel();p.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-.5,180,0);p.hasChangedDimension();players.add(p);return p;}
        void at(ServerPlayer p,double progress){p.moveTo(GoatmanWoods.path(progress).add(b.getX(),b.getY(),b.getZ()));}
        /** Inside, by the table, looking up the trailer away from the door. */
        void indoors(ServerPlayer p){p.moveTo(b.getX()+.5,b.getY()+1,b.getZ()-58.5,180,0);}
        CompoundTag run(){return GoatmanVignette.run(LabyrinthData.get(server));}
        void run(CompoundTag r){LabyrinthData data=LabyrinthData.get(server);CompoundTag all=data.state(GoatmanVignette.ID);all.put("Run",r);data.setState(GoatmanVignette.ID,all);}
        List<GoatmanChild> children(){return l.getEntitiesOfClass(GoatmanChild.class,IndianLakeRooms.bounds(b,LabyrinthPlace.GOATMAN));}
        List<GoatmanChild> cousins(){return children().stream().filter(c->!c.girl()&&c.getPersistentData().getInt(GoatmanVignette.INDEX)>=0).toList();}
        Optional<GoatmanChild> cousin(int i){return cousins().stream().filter(c->c.getPersistentData().getInt(GoatmanVignette.INDEX)==i).findFirst();}
        Vec3 rel(Entity e){return e.position().subtract(b.getX(),b.getY(),b.getZ());}
        void cohort(CompoundTag r,List<ServerPlayer> participants,int vigil){
            var cohort=r.getCompound("Cohort");for(var p:participants){var own=cohort.getCompound(p.getUUID().toString());own.putBoolean("Active",true);own.putBoolean("Arrived",true);own.putBoolean("GirlGone",true);own.putBoolean("Greeted",true);own.putInt("Vigil",vigil);cohort.put(p.getUUID().toString(),own);indoors(p);}
            r.put("Cohort",cohort);
        }
        /** Dusk at the given clock: everyone has arrived, the pan was filled for them, and the cousin who went for gas is gone. */
        void evening(List<ServerPlayer> participants,int clock){
            var r=run();r.remove("Cousins0464");r.remove("PlayerServes0464");r.putInt("Wrong",r.getInt("Wrong")%5);r.putInt("Runner",(r.getInt("Wrong")+1)%5);cousins().forEach(Entity::discard);r.putInt("Phase",GoatmanVignette.GATHERING);r.putInt("Clock",clock);int pan=4+participants.size();
            r.putInt("Expected",pan);r.putInt("Pan",pan);r.putInt("PanFor",pan);r.putInt("RunnerState",clock>GoatmanVignette.RUNNER_LEAVES?GoatmanVignette.R_AWAY:GoatmanVignette.R_HOME);
            cohort(r,participants,0);run(r);GoatmanVignette.stage(l,b,r);GoatmanWoods.pan(l,b,pan);
        }
        /** Night at the given clock, the thing at the door and the cousin who went for gas in. */
        void night(List<ServerPlayer> participants,int clock,boolean windowShut){
            var r=run();if(r.contains("Cousins0464")){r.remove("Cousins0464");r.remove("PlayerServes0464");r.putInt("Wrong",r.getInt("Wrong")%5);r.putInt("Runner",(r.getInt("Wrong")+1)%5);cousins().forEach(Entity::discard);}
            r.putInt("Phase",GoatmanVignette.VIGIL);r.putInt("Clock",clock);r.putInt("ExtraState",GoatmanVignette.X_DOOR);r.putInt("RunnerState",GoatmanVignette.R_INSIDE);r.putInt("Expected",4+participants.size());
            cohort(r,participants,clock);run(r);GoatmanWoods.atmosphere(l,b,true);GoatmanWoods.door(l,b,false);GoatmanWoods.setWindow(l,b,windowShut);GoatmanVignette.stage(l,b,r);
        }
        void click(ServerPlayer p,BlockPos rel){BlockPos at=b.offset(rel);NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(p,InteractionHand.MAIN_HAND,at,new BlockHitResult(Vec3.atCenterOf(at),Direction.SOUTH,at,false)));}
        /** The whole native interaction: the event, then the block's own use. */
        void use(ServerPlayer p,BlockPos rel){BlockPos at=b.offset(rel);p.gameMode.useItemOn(p,l,ItemStack.EMPTY,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(at),Direction.WEST,at,false));}
        public void close(){
            for(var p:players){GoatmanHaunt.leave(p);GoatmanHaunt.vanish(p,null);GoatmanVignette.depart(p);TallyCounterItem.forget(p.getUUID());if(server.getPlayerList().getPlayers().contains(p))server.getPlayerList().remove(p);else p.discard();}
            AABB bounds=IndianLakeRooms.bounds(b,LabyrinthPlace.GOATMAN);for(var e:l.getEntitiesOfClass(Entity.class,bounds,e->e instanceof GoatmanChild||e instanceof GoatmanFigure||e instanceof Display||e instanceof Wolf||e instanceof Zombie||e instanceof net.minecraft.world.entity.item.ItemEntity))e.discard();
            for(int x=((int)bounds.minX-1)>>4;x<=((int)bounds.maxX+1)>>4;x++)for(int z=((int)bounds.minZ-1)>>4;z<=((int)bounds.maxZ+1)>>4;z++)l.getChunkSource().removeRegionTicket(TicketType.PORTAL,new ChunkPos(x,z),3,b);
            l.getGameRules().getRule(GameRules.RULE_KEEPINVENTORY).set(keep,server);
            server.overworld().getDataStorage().set("the_oldest_house",oldHouse);server.overworld().getDataStorage().set("the_oldest_house_labyrinth",oldData);server.overworld().getDataStorage().set("the_oldest_house_mother",oldMother);LabyrinthBuilder.clearAll();LabyrinthDoors.clearAll();
        }
    }
    private static Fixture path,night,taken,window,lost,counter,resume,upgrade;
    @AfterBatch(batch="goat_path") public static void cleanPath(ServerLevel l){if(path!=null){path.close();path=null;}}
    @AfterBatch(batch="goat_night") public static void cleanNight(ServerLevel l){if(night!=null){night.close();night=null;}}
    @AfterBatch(batch="goat_taken") public static void cleanTaken(ServerLevel l){if(taken!=null){taken.close();taken=null;}}
    @AfterBatch(batch="goat_window") public static void cleanWindow(ServerLevel l){if(window!=null){window.close();window=null;}}
    @AfterBatch(batch="goat_lost") public static void cleanLost(ServerLevel l){if(lost!=null){lost.close();lost=null;}}
    @AfterBatch(batch="goat_counter") public static void cleanCounter(ServerLevel l){if(counter!=null){counter.close();counter=null;}}
    @AfterBatch(batch="goat_resume") public static void cleanResume(ServerLevel l){if(resume!=null){resume.close();resume=null;}}
    @AfterBatch(batch="goat_upgrade") public static void cleanUpgrade(ServerLevel l){if(upgrade!=null){upgrade.close();upgrade=null;}}

    @GameTest(template="empty",batch="goat_path",timeoutTicks=230)
    public static void nativeBackwardGirlAndTheShapesInTheHollowsMeetTheLatecomerThenLeave(GameTestHelper h){
        path=new Fixture(h,28000);var f=path;var p=f.player();p.getAttribute(Attributes.SCALE).setBaseValue(1.2);h.assertTrue(GoatmanVignette.enter(p),"the actual latecomer enrolls");
        var data=LabyrinthData.get(f.server);CompoundTag original=GoatmanVignette.run(data);UUID round=original.getUUID("Id");int wrong=original.getInt("Wrong"),tells=original.getInt("Tells"),runner=original.getInt("Runner");
        h.assertTrue(Integer.bitCount(tells)>=2&&Integer.bitCount(tells)<=3&&wrong>=0&&wrong<8&&runner!=wrong&&runner>=0&&runner<8,"one cousin receives two or three tells, and a different real cousin will go for gas");
        h.assertTrue(Math.abs(p.getAttributeValue(Attributes.SCALE)-.84)<.001,"child view multiplies the existing scale instead of destroying it");
        var pet=EntityType.WOLF.create(f.l);pet.tame(p);pet.setPersistenceRequired();pet.moveTo(GoatmanWoods.path(4).add(f.b.getX(),f.b.getY(),f.b.getZ()));f.l.addFreshEntity(pet);CompanionOrders.issue(pet,p,CompanionOrders.Order.DEEPER);
        int[] tick={0};float[] stopped={0};
        h.onEachTick(()->{
            tick[0]++;double progress=tick[0]<=10?0:tick[0]<=30?(tick[0]-10)*.12:tick[0]<=50?2.4+(tick[0]-30)*.4:tick[0]<=70?10.4:Math.min(73,10.4+(tick[0]-70)*.6);f.at(p,progress);
        });
        h.runAfterDelay(8,()->h.assertTrue(f.cousins().size()==8&&f.children().stream().filter(GoatmanChild::girl).count()==1,"eight shared apparent cousins and the private path girl are staged"));
        h.runAfterDelay(45,()->{
            var girl=f.children().stream().filter(GoatmanChild::girl).findFirst().orElseThrow();var own=GoatmanVignette.run(LabyrinthData.get(f.server)).getCompound("Cohort").getCompound(p.getUUID().toString());
            double girlProgress=GoatmanWoods.project(f.rel(girl)).progress();
            h.assertTrue(Math.abs(girlProgress-own.getDouble("Progress")-7)<.02&&girl.speed()<0,"sprinting retains a seven-block gap and a reversed walk phase");
            h.assertTrue(GoatmanWoods.project(f.rel(pet)).progress()<own.getDouble("Progress"),"a DEEPER command cannot send the actual companion ahead");
        });
        h.runAfterDelay(60,()->{
            // Progress 10.4: the first hollow is ahead. Something stands in it, its back to the trail, for this latecomer only.
            var shapes=f.l.getEntitiesOfClass(GoatmanFigure.class,p.getBoundingBox().inflate(40),s->s.viewer().filter(p.getUUID()::equals).isPresent());
            h.assertTrue(shapes.size()==1&&shapes.getFirst().purpose.equals("hollow0")&&Math.abs(Mth.wrapDegrees(shapes.getFirst().getYRot()-GoatmanWoods.HOLLOW_YAW[0]))<1,"one private shape stands in the first hollow, facing away from the trail");
            var hollow=f.b.offset(-3,0,-18);h.assertTrue(f.l.getBlockState(hollow).isAir()&&f.l.getBlockState(hollow.above()).isAir()&&!f.l.getBlockState(hollow.below()).isAir(),"the hollow is a real space in the woods beside the trail");
            stopped[0]=f.children().stream().filter(GoatmanChild::girl).findFirst().orElseThrow().walk(0);
        });
        h.runAfterDelay(66,()->{var girl=f.children().stream().filter(GoatmanChild::girl).findFirst().orElseThrow();h.assertTrue(girl.speed()==0&&Math.abs(girl.walk(0)-stopped[0])<.001,"stopping also stops her reverse animation");
            var loaded=LabyrinthData.FACTORY.deserializer().apply(LabyrinthData.get(f.server).save(new CompoundTag(),f.l.registryAccess()),f.l.registryAccess());f.server.overworld().getDataStorage().set("the_oldest_house_labyrinth",loaded);
        });
        h.runAfterDelay(160,()->{
            var r=GoatmanVignette.run(LabyrinthData.get(f.server));h.assertTrue(r.getUUID("Id").equals(round)&&r.getInt("Wrong")==wrong&&r.getInt("Tells")==tells&&r.getInt("Runner")==runner,"saved reload never rerolls the identity, tells or the cousin who goes for gas");
            h.assertTrue(r.getCompound("Cohort").getCompound(p.getUUID().toString()).getBoolean("Arrived")&&f.children().stream().noneMatch(GoatmanChild::girl),"the trail reaches the clearing after the apparition disappears around its bend");
            h.assertTrue(f.l.getEntitiesOfClass(GoatmanFigure.class,p.getBoundingBox().inflate(80),s->s.viewer().filter(p.getUUID()::equals).isPresent()).isEmpty(),"both shapes are gone once the latecomer is past them");
            h.assertTrue(GoatmanVignette.expectedCount(r)==8&&GoatmanVignette.presentCount(f.l,f.b)==9&&!WitnessAccount.has(LabyrinthData.get(f.server),p.getUUID(),WitnessAccount.Story.GOATMAN),"arrival presents the larger cast's discrepancy without resolution credit");
            h.assertTrue(CompanionOrders.order(pet)==CompanionOrders.Order.DEEPER&&pet.getOwnerUUID().equals(p.getUUID()),"temporary refusal preserves the native pet and saved order");
            p.teleportTo(f.server.overworld(),100,80,100,0,0);p.hasChangedDimension();GoatmanVignette.tick(f.server);h.assertTrue(Math.abs(p.getAttributeValue(Attributes.SCALE)-1.2)<.001,"leaving restores the pre-existing scale");h.succeed();
        });
    }

    @GameTest(template="empty",batch="goat_night",timeoutTicks=600)
    public static void aNightDoneRightCountsLetsTheCousinInKeepsItOutAndEndsAnOldHaunting(GameTestHelper h){
        night=new Fixture(h,28300);var f=night;var a=f.player();var b=f.player();
        // b brought something home from an earlier night; getting this one right ends it.
        GoatmanHaunt.haunt(b,3,GoatmanVignette.FACE|GoatmanVignette.SILENT);
        h.assertTrue(GoatmanVignette.enter(a)&&GoatmanVignette.enter(b),"both native players join the same evening");
        var observer=f.player();observer.gameMode.changeGameModeForPlayer(GameType.SPECTATOR);f.indoors(observer);
        f.evening(List.of(a,b),GoatmanVignette.SUPPER-2);var start=f.run();int wrong=start.getInt("Wrong"),runner=start.getInt("Runner");
        h.assertTrue(f.cousins().size()==4&&f.cousin(runner).isEmpty(),"the cousin who went for gas is not at the camp");
        h.assertTrue(f.l.getBlockState(f.b.offset(-2,1,-61)).getValue(StairBlock.FACING)==Direction.WEST&&f.l.getBlockState(f.b.offset(2,1,-61)).getValue(StairBlock.FACING)==Direction.EAST,"every chair's back is behind its sitter, who faces the table");
        h.assertTrue(f.l.getBlockState(f.b.offset(0,4,-62)).is(Blocks.LANTERN)&&f.l.getBlockState(f.b.offset(-7,3,-59)).is(Blocks.LANTERN)&&!f.l.getBlockState(f.b.offset(0,5,-63)).is(Blocks.SEA_LANTERN),"warm lamps hang over the table and stand by the bunks, and the cold ceiling lights are gone");
        f.click(a,GoatmanWoods.STOVE);h.assertTrue(f.run().getInt("Pan")==6&&a.getInventory().countItem(GoatmanRegistry.BRAT.get())==0,"a pan is not served before supper");
        h.runAfterDelay(4,()->{
            GoatmanVignette.hurry(f.l,f.b);
            for(var c:f.cousins()){int i=c.getPersistentData().getInt(GoatmanVignette.INDEX);var seat=f.rel(c);
                h.assertTrue(c.seated()&&Math.abs(seat.y-1.5)<.01&&f.l.getBlockState(BlockPos.containing(c.position())).getBlock() instanceof StairBlock,"cousin "+i+" sits on a real chair at seat height: "+seat);}
            var r=f.run();h.assertTrue(r.getInt("Pan")==2&&Integer.bitCount(r.getInt("Served"))==4,"four sit down and four brats leave the pan: three cousins and the one that is not");
            f.click(a,GoatmanWoods.STOVE);f.click(b,GoatmanWoods.PAN);f.click(a,GoatmanWoods.STOVE);
            h.assertTrue(f.run().getInt("Pan")==0&&a.getInventory().countItem(GoatmanRegistry.BRAT.get())==1&&b.getInventory().countItem(GoatmanRegistry.BRAT.get())==1,"each child takes one brat and the pan is empty; nothing is left for the cousin out getting gas");
            r=f.run();r.putInt("Clock",GoatmanVignette.EXTRA_OUT-1);f.run(r);
        });
        h.runAfterDelay(8,()->{
            GoatmanVignette.hurry(f.l,f.b);var extra=f.cousin(wrong).orElseThrow();
            h.assertTrue(f.run().getInt("ExtraState")==GoatmanVignette.X_FIRE&&!GoatmanWoods.indoors(f.rel(extra))&&extra.position().distanceTo(Vec3.atCenterOf(f.b.offset(GoatmanWoods.FIRE)))<3,"after supper the one that is not a cousin goes and stands by the fire");
            f.click(a,GoatmanWoods.DOOR);h.assertTrue(!GoatmanWoods.doorOpen(f.l,f.b),"a child shuts the door at dusk");
            var r=f.run();r.putInt("Clock",GoatmanVignette.RUNNER_BACK-1);f.run(r);
        });
        h.runAfterDelay(12,()->{
            GoatmanVignette.hurry(f.l,f.b);h.assertTrue(f.run().getInt("RunnerState")==GoatmanVignette.R_KNOCKING&&f.cousin(runner).isPresent(),"the cousin comes back up the trail calling, and knocks at the shut door");
            var r=f.run();r.putInt("Clock",GoatmanVignette.RUNNER_BACK+79);f.run(r);
        });
        h.runAfterDelay(15,()->{
            h.assertTrue(f.run().getInt("Demand")<0,"his words at the door are his own, in an ordinary voice");
            f.click(a,GoatmanWoods.DOOR);GoatmanVignette.hurry(f.l,f.b);
            h.assertTrue(f.run().getInt("RunnerState")==GoatmanVignette.R_INSIDE&&GoatmanWoods.indoors(f.rel(f.cousin(runner).orElseThrow())),"opening for him lets him in");
            f.click(a,GoatmanWoods.DOOR);
            h.assertTrue(!GoatmanWoods.windowShut(f.l,f.b),"the bathroom window was left propped open");f.use(b,GoatmanWoods.WINDOW);
            h.assertTrue(GoatmanWoods.windowShut(f.l,f.b),"a native use shuts the bathroom window");
            var r=f.run();r.putInt("Clock",GoatmanVignette.SILENCE-1);f.run(r);
        });
        h.runAfterDelay(18,()->{
            var r=f.run();h.assertTrue(r.getBoolean("Silence")&&r.getInt("ExtraState")==GoatmanVignette.X_APPROACH&&r.getInt("RunnerState")==GoatmanVignette.R_INSIDE,"the woods go quiet with the cousin safely in, and the thing starts for the door");
            h.assertTrue(f.l.getBlockState(f.b.offset(GoatmanWoods.PORCH_LIGHT)).isAir(),"the porch light goes out in the quiet");
            GoatmanVignette.hurry(f.l,f.b);
            h.assertTrue(f.run().getInt("ExtraState")==GoatmanVignette.X_DOOR&&!GoatmanWoods.indoors(f.rel(f.cousin(wrong).orElseThrow())),"unwatched, it reaches the shut door and stays outside it");
            r=f.run();r.putInt("Clock",GoatmanVignette.GATHER_TICKS-1);f.run(r);
        });
        h.runAfterDelay(21,()->{
            var r=f.run();h.assertTrue(r.getInt("Phase")==GoatmanVignette.VIGIL&&!GoatmanWoods.doorOpen(f.l,f.b),"night falls on a shut door");
            r.putInt("Clock",GoatmanVignette.WINDOW_TRY-1);f.run(r);
        });
        h.runAfterDelay(24,()->{
            var r=f.run();h.assertTrue(r.getInt("ExtraState")==GoatmanVignette.X_DOOR&&r.getInt("Demand")>0,"the shut window keeps it out; what knocks uses the cousin's words without his voice");
            r.putInt("Clock",GoatmanVignette.VIGIL_TICKS-3);var cohort=r.getCompound("Cohort");for(var p:List.of(a,b)){var own=cohort.getCompound(p.getUUID().toString());own.putInt("Vigil",GoatmanVignette.VIGIL_TICKS-3);cohort.put(p.getUUID().toString(),own);}r.put("Cohort",cohort);f.run(r);
        });
        h.runAfterDelay(28,()->{
            var data=LabyrinthData.get(f.server);var r=f.run();
            h.assertTrue(r.getInt("Phase")==GoatmanVignette.DAWN&&r.getBoolean("Right"),"morning comes on a night done right");
            h.assertTrue(WitnessAccount.has(data,a.getUUID(),WitnessAccount.Story.GOATMAN)&&WitnessAccount.has(data,b.getUUID(),WitnessAccount.Story.GOATMAN),"both children who sat out the whole night resolve their own story");
            for(var p:List.of(a,b)){var held=p.getInventory().items.stream().filter(s->s.getItem() instanceof TallyCounterItem).toList();
                h.assertTrue(held.size()==1&&TallyCounterItem.carries(p),p.getGameProfile().getName()+" takes home one counter, theirs alone");}
            h.assertTrue(!GoatmanHaunt.haunted(data,b.getUUID())&&f.children().stream().anyMatch(c->c.getTags().contains("TrailerWalk")&&c.viewer().filter(b.getUUID()::equals).isPresent()),"b's old haunting ends, and what followed them home walks out with them for the last time");
            h.assertTrue(!WitnessAccount.has(data,observer.getUUID(),WitnessAccount.Story.GOATMAN)&&!GoatmanVignette.childScale(observer),"a spectator receives neither credit nor forced child scale");
            h.assertTrue(f.cousin(wrong).isEmpty()&&f.cousin(runner).isPresent(),"in the morning the thing is not among them, and the cousin is");
            h.assertTrue(f.l.getBrightness(LightLayer.BLOCK,f.b.offset(-6,2,-58))>=8&&f.l.getBrightness(LightLayer.BLOCK,f.b.offset(-5,1,-75))>=8,"the bunks and the kitchenette are lit well enough to see a face");
            h.assertTrue(!GoatmanVignette.canDeal(data,a.getUUID())&&GoatmanVignette.canDeal(data,UUID.randomUUID())&&!data.isCompleted(GoatmanVignette.ID),"a personal ending exhausts only its own explorer's encounter");h.succeed();
        });
    }

    @GameTest(template="empty",batch="goat_taken",timeoutTicks=200)
    public static void openingTheDoorAtNightTakesTheOpenerAliveAndWholeAndItComesHomeWithThem(GameTestHelper h){
        taken=new Fixture(h,28600);var f=taken;var a=f.player();var peer=f.player();GoatmanVignette.enter(a);GoatmanVignette.enter(peer);f.night(List.of(a,peer),200,true);
        f.l.getGameRules().getRule(GameRules.RULE_KEEPINVENTORY).set(false,f.server);
        var axe=new ItemStack(Items.IRON_AXE);axe.setDamageValue(37);a.getInventory().setItem(4,axe.copy());a.getInventory().setItem(5,new ItemStack(Items.BREAD,3));
        h.runAfterDelay(4,()->{
            f.click(a,GoatmanWoods.DOOR);var data=LabyrinthData.get(f.server);
            h.assertTrue(a.isAlive()&&!a.isDeadOrDying()&&a.getInventory().getItem(4).is(Items.IRON_AXE)&&a.getInventory().getItem(4).getDamageValue()==37&&a.getInventory().countItem(Items.BREAD)==3,"the opener is neither killed nor stripped of anything");
            h.assertTrue(!GoatmanVignette.inside(a)&&a.position().distanceToSqr(HideAndClap.manorRespawn(f.origin))<1&&!GoatmanVignette.childScale(a),"the opener is simply gone from the trailer, at normal size");
            h.assertTrue(GoatmanHaunt.haunted(data,a.getUUID())&&!WitnessAccount.has(data,a.getUUID(),WitnessAccount.Story.GOATMAN)&&!GoatmanWoods.doorOpen(f.l,f.b),"it went with them; the door is shut again behind");
            h.assertTrue(peer.isAlive()&&GoatmanVignette.inside(peer)&&!GoatmanHaunt.haunted(data,peer.getUUID()),"the peer's night goes on untouched");
            // At home: the next day it has eaten with them, and it is seen standing off to one side.
            var floor=a.blockPosition().offset(-20,-1,-20);for(var at:BlockPos.betweenClosed(floor,floor.offset(40,0,40)))f.l.setBlock(at,Blocks.STONE.defaultBlockState(),2);
            for(var at:BlockPos.betweenClosed(floor.above(),floor.offset(40,4,40)))if(!f.l.getBlockState(at).isAir())f.l.setBlock(at,Blocks.AIR.defaultBlockState(),2);
            var own=GoatmanVignette.personal(data,a.getUUID());own.putLong("HauntDay",f.server.overworld().getDayTime()/24000-1);own.putLong("NextGlimpse",0);
            CompoundTag all=data.state(GoatmanVignette.ID),players=all.getCompound("Players");players.put(a.getUUID().toString(),own);all.put("Players",players);data.setState(GoatmanVignette.ID,all);
        });
        h.runAfterDelay(30,()->{
            h.assertTrue(a.getInventory().countItem(Items.BREAD)==2,"one piece of food is gone from the pack for the day: "+a.getInventory().countItem(Items.BREAD));
            var seen=f.l.getEntitiesOfClass(GoatmanFigure.class,a.getBoundingBox().inflate(20),s->s.viewer().filter(a.getUUID()::equals).isPresent());
            h.assertTrue(seen.size()==1&&seen.getFirst().distanceTo(a)>9&&seen.getFirst().purpose.equals("glimpse"),"the thing itself is glimpsed, ten or more blocks off, by them alone");
            var look=a.getLookAngle();var to=seen.getFirst().position().subtract(a.position()).normalize();h.assertTrue(look.dot(to)<.6,"it stands off to one side of where they look, not in front of them");
            h.assertTrue(GoatmanRegistry.FIGURE.get().getDimensions().height()>2,"it is taller than a man");h.succeed();
        });
    }

    @GameTest(template="empty",batch="goat_window",timeoutTicks=200)
    public static void anOpenBathroomWindowLetsItInAndItWalksHomeWithEveryone(GameTestHelper h){
        window=new Fixture(h,28900);var f=window;var a=f.player();var b=f.player();GoatmanVignette.enter(a);GoatmanVignette.enter(b);f.night(List.of(a,b),GoatmanVignette.WINDOW_TRY-2,false);
        int wrong=f.run().getInt("Wrong");
        h.runAfterDelay(4,()->{
            h.assertTrue(f.run().getInt("ExtraState")==GoatmanVignette.X_WINDOW,"with the window propped open it does not wait at the door");
            GoatmanVignette.hurry(f.l,f.b);var it=f.cousin(wrong).orElseThrow();
            h.assertTrue(f.run().getInt("ExtraState")==GoatmanVignette.X_FLOOR&&GoatmanWoods.indoors(f.rel(it))&&it.hasPose(Pose.SLEEPING),"it comes out of the bathroom and lies down on the floor among them");
            h.assertTrue(f.cousins().stream().filter(c->GoatmanWoods.indoors(f.rel(c))).count()==5,"there is one more child inside than went to bed");
            var r=f.run();r.putInt("Clock",GoatmanVignette.VIGIL_TICKS-3);var cohort=r.getCompound("Cohort");for(var p:List.of(a,b)){var own=cohort.getCompound(p.getUUID().toString());own.putInt("Vigil",GoatmanVignette.VIGIL_TICKS-3);cohort.put(p.getUUID().toString(),own);}r.put("Cohort",cohort);f.run(r);
        });
        h.runAfterDelay(8,()->{
            var data=LabyrinthData.get(f.server);var r=f.run();
            h.assertTrue(r.getInt("Phase")==GoatmanVignette.DAWN&&!r.getBoolean("Right"),"morning comes on a night it got into");
            for(var p:List.of(a,b)){
                h.assertTrue(GoatmanHaunt.haunted(data,p.getUUID())&&!WitnessAccount.has(data,p.getUUID(),WitnessAccount.Story.GOATMAN)&&!TallyCounterItem.carries(p),p.getGameProfile().getName()+" takes it home instead of the count");
                h.assertTrue(f.children().stream().anyMatch(c->c.getTags().contains("TrailerWalk")&&c.viewer().filter(p.getUUID()::equals).isPresent()&&c.tells()==r.getInt("Tells")),"it walks out with "+p.getGameProfile().getName()+", wearing the cousin's shape it wore");
                h.assertTrue(LabyrinthDealer.rememberedWeight(data,p.getUUID(),LabyrinthPlace.GOATMAN,1)>=24,"the trailer stays near for a reader it followed home");
            }
            h.assertTrue(GoatmanVignette.canDeal(data,a.getUUID()),"a haunted reader may come back for another night");h.succeed();
        });
    }

    @GameTest(template="empty",batch="goat_lost",timeoutTicks=200)
    public static void theCousinLeftKnockingAtDuskIsGoneWhenTheWoodsGoQuietAndItTakesHisPlace(GameTestHelper h){
        lost=new Fixture(h,29200);var f=lost;var a=f.player();GoatmanVignette.enter(a);f.evening(List.of(a),GoatmanVignette.RUNNER_BACK-1);
        var start=f.run();int runner=start.getInt("Runner");start.putInt("ExtraState",GoatmanVignette.X_FIRE);f.run(start);GoatmanWoods.door(f.l,f.b,false);GoatmanWoods.setWindow(f.l,f.b,true);
        h.runAfterDelay(3,()->{
            GoatmanVignette.hurry(f.l,f.b);h.assertTrue(f.run().getInt("RunnerState")==GoatmanVignette.R_KNOCKING,"he is back and knocking at a shut door");
            var r=f.run();r.putInt("Clock",GoatmanVignette.SILENCE-1);f.run(r);
        });
        h.runAfterDelay(6,()->{
            var r=f.run();h.assertTrue(r.getInt("RunnerState")==GoatmanVignette.R_LOST&&r.getInt("Demand")==-99&&f.cousin(runner).isEmpty(),"when the woods go quiet his knocking stops mid-word and he is not there");
            f.night(List.of(a),GoatmanVignette.VIGIL_TICKS-3,true);r=f.run();r.putInt("RunnerState",GoatmanVignette.R_LOST);f.run(r);
        });
        h.runAfterDelay(10,()->{
            var data=LabyrinthData.get(f.server);var r=f.run();
            h.assertTrue(r.getInt("Phase")==GoatmanVignette.DAWN&&!r.getBoolean("Right")&&GoatmanHaunt.haunted(data,a.getUUID())&&!WitnessAccount.has(data,a.getUUID(),WitnessAccount.Story.GOATMAN),"keeping the door and window shut is not enough without him");
            h.assertTrue(f.children().stream().anyMatch(c->c.getTags().contains("TrailerWalk")&&c.skin()==r.getInt("Skin"+runner)&&c.tells()==0),"what walks out with them in the morning wears his shape");h.succeed();
        });
    }

    @GameTest(template="empty",batch="goat_counter",timeoutTicks=80)
    public static void theCounterCountsWhatIsReallyThereInsideAndKeepsWatchOutside(GameTestHelper h){
        counter=new Fixture(h,29500);var f=counter;var p=f.player();var other=f.player();
        var spot=f.b.offset(30,30,-30);for(var at:BlockPos.betweenClosed(spot.offset(-8,-1,-8),spot.offset(8,-1,8)))f.l.setBlock(at,Blocks.STONE.defaultBlockState(),2);
        p.teleportTo(f.l,spot.getX()+.5,spot.getY(),spot.getZ()+.5,0,0);other.teleportTo(f.l,spot.getX()+3.5,spot.getY(),spot.getZ()+.5,0,0);
        var pretender=GoatmanRegistry.CHILD.get().create(f.l);pretender.appearance(2,GoatmanVignette.FACE|GoatmanVignette.STILL,null);pretender.moveTo(Vec3.atBottomCenterOf(spot.offset(0,0,4)));f.l.addFreshEntity(pretender);
        var dog=EntityType.WOLF.create(f.l);dog.moveTo(Vec3.atBottomCenterOf(spot.offset(-3,0,3)));f.l.addFreshEntity(dog);
        var mine=TallyCounterItem.forReader(p.getUUID());p.setItemInHand(InteractionHand.MAIN_HAND,mine);
        TallyCounterItem.click(other,mine.copy());h.assertTrue(!pretender.hasEffect(MobEffects.GLOWING),"someone else's counter shows them nothing");
        h.assertTrue(TallyCounterItem.present(p,TallyCounterItem.COUNT_RANGE).size()==4,"it counts everyone really here: its keeper, the other child, the dog and the one pretending");
        TallyCounterItem.click(p,p.getMainHandItem());
        h.assertTrue(pretender.hasEffect(MobEffects.GLOWING)&&!dog.hasEffect(MobEffects.GLOWING)&&!other.hasEffect(MobEffects.GLOWING),"inside the House it shows the one pretending, and nobody else");
        var behind=EntityType.ZOMBIE.create(f.l);behind.setNoAi(true);behind.moveTo(Vec3.atBottomCenterOf(spot.offset(0,0,-4)));f.l.addFreshEntity(behind);
        TallyCounterItem.watch(p);h.assertTrue(TallyCounterItem.lastWarning(p.getUUID())==f.l.getGameTime(),"it clicks on its own when something comes up behind its keeper");
        // Outside, it keeps count over a sleeping keeper.
        var world=f.server.overworld();var bed=new BlockPos(29500,120,29500);
        for(var at:BlockPos.betweenClosed(bed.offset(-6,-1,-6),bed.offset(6,-1,6)))world.setBlock(at,Blocks.STONE.defaultBlockState(),2);
        world.setBlock(bed,Blocks.RED_BED.defaultBlockState().setValue(BedBlock.PART,net.minecraft.world.level.block.state.properties.BedPart.HEAD),2);
        var sleeper=f.player();sleeper.teleportTo(world,bed.getX()+.5,bed.getY(),bed.getZ()+.5,0,0);sleeper.hasChangedDimension();sleeper.getInventory().add(TallyCounterItem.forReader(sleeper.getUUID()));
        sleeper.startSleeping(bed);h.assertTrue(sleeper.isSleeping(),"the keeper is asleep");
        var creeper=EntityType.ZOMBIE.create(world);creeper.setNoAi(true);creeper.moveTo(Vec3.atBottomCenterOf(bed.offset(4,0,3)));world.addFreshEntity(creeper);
        TallyCounterItem.watch(sleeper);
        h.assertTrue(!sleeper.isSleeping()&&creeper.hasEffect(MobEffects.GLOWING),"outside, something close wakes its keeper and is shown to them");
        creeper.discard();world.setBlock(bed,Blocks.AIR.defaultBlockState(),2);behind.discard();pretender.discard();dog.discard();h.succeed();
    }

    @GameTest(template="empty",batch="goat_resume",timeoutTicks=100)
    public static void savedVigilPausesWithoutVisitorsAndDoesNotCreditSomeoneWhoLeft(GameTestHelper h){
        resume=new Fixture(h,29800);var f=resume;var a=f.player();var b=f.player();GoatmanVignette.enter(a);GoatmanVignette.enter(b);f.night(List.of(a,b),GoatmanVignette.VIGIL_TICKS-30,true);
        var data=LabyrinthData.get(f.server);var saved=data.save(new CompoundTag(),f.l.registryAccess());var loaded=LabyrinthData.FACTORY.deserializer().apply(saved,f.l.registryAccess());f.server.overworld().getDataStorage().set("the_oldest_house_labyrinth",loaded);
        a.moveTo(100,80,100);b.moveTo(100,80,100);GoatmanVignette.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(a));
        GoatmanVignette.tick(f.server);h.assertTrue(GoatmanVignette.run(loaded).getInt("Clock")==GoatmanVignette.VIGIL_TICKS-30&&!GoatmanVignette.childScale(a),"no visitors means the saved night pauses, and logout removes the transient scale");
        f.indoors(a);GoatmanVignette.enter(a);GoatmanVignette.depart(b);
        h.runAfterDelay(42,()->{
            var current=LabyrinthData.get(f.server);h.assertTrue(WitnessAccount.has(current,a.getUUID(),WitnessAccount.Story.GOATMAN)&&!WitnessAccount.has(current,b.getUUID(),WitnessAccount.Story.GOATMAN),"the saved night resumes but a departed peer gains no credit");
            h.assertTrue(!GoatmanHaunt.haunted(current,b.getUUID()),"leaving before the night is out brings nothing home either");
            int count=WitnessAccount.count(current,a.getUUID());GoatmanVignette.tick(f.server);h.assertTrue(WitnessAccount.count(current,a.getUUID())==count,"the ending cannot farm repeated resolutions");h.succeed();
        });
    }

    @GameTest(template="empty",batch="goat_upgrade",timeoutTicks=200)
    public static void nativeVersionSeventeenUpgradePreservesCachesActorsAndExistingEvidence(GameTestHelper h){
        upgrade=new Fixture(h,30100);var f=upgrade;var data=LabyrinthData.get(f.server);
        BlockPos junction=LabyrinthPlaces.base(f.origin,LabyrinthPlace.JUNCTION);LabyrinthBuilder.buildJunction(f.l,junction);LabyrinthLighting.buildEarlyAid(f.server,f.l,junction);
        var cache=(BarrelBlockEntity)f.l.getBlockEntity(junction.offset(LabyrinthLighting.TOM_CACHE));cache.setItem(0,ItemStack.EMPTY);cache.setItem(3,new ItemStack(Items.DIAMOND));
        BlockPos canoeBase=LabyrinthPlaces.base(f.origin,LabyrinthPlace.PHONE_CANOE);PhoneCanoe.build(f.server,f.l,canoeBase);var canoe=PhoneCanoe.stage(f.l,canoeBase);UUID canoeId=canoe.getUUID();
        CompoundTag reading=data.state(HarriganVignette.ID);reading.putBoolean("ReadingFinished",true);data.setState(HarriganVignette.ID,reading);
        UUID player=UUID.randomUUID();WitnessAccount.resolve(data,player,WitnessAccount.Story.HARRIGAN,"kept_phone");data.setBuilt(17,f.origin);
        h.assertTrue(!LabyrinthBuilder.ensureBuilt(f.server),"the old layout starts its incremental upgrade");LabyrinthBuilder.finishGameTest(f.server);
        h.assertTrue(data.builtVersion()==LabyrinthBuilder.VERSION&&data.door(LabyrinthPlace.GOATMAN.entryDoorId())!=null,"the trailer's physical slot and return door are appended");
        h.assertTrue(f.l.getBlockEntity(junction.offset(LabyrinthLighting.TOM_CACHE))==cache&&cache.getItem(0).isEmpty()&&cache.getItem(3).is(Items.DIAMOND),"existing finite supplies are neither rebuilt nor replenished");
        h.assertTrue(!canoe.isRemoved()&&canoe.getUUID().equals(canoeId)&&data.state(HarriganVignette.ID).getBoolean("ReadingFinished")&&WitnessAccount.has(data,player,WitnessAccount.Story.HARRIGAN),"the existing recording actor, finished dialogue and personal evidence survive unchanged");h.succeed();
    }
}
