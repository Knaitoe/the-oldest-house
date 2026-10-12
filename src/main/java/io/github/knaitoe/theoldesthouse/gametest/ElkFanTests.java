package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;

/** Native movement up the actual A-frame, a native fan click and guarded saved-room repair. */
@GameTestHolder(TheOldestHouse.MOD_ID+"_fan488") @PrefixGameTestTemplate(false)
public final class ElkFanTests {
    private static Fixture active;
    private static final class Fixture implements AutoCloseable {
        final GameTestHelper h;final ServerLevel l;final NativeTestChunks chunks=new NativeTestChunks();
        final BlockPos origin=new BlockPos(63500,80,63500),b;final HouseSavedData oldHouse;final LabyrinthData oldData;
        final List<ServerPlayer> players=new ArrayList<>();final List<Entity> extras=new ArrayList<>();
        Fixture(GameTestHelper h){this.h=h;l=HouseTestLevel.get(h.getLevel().getServer(),HouseDimensions.INTERIOR);oldHouse=HouseSavedData.get(l.getServer());oldData=LabyrinthData.get(l.getServer());var house=new HouseSavedData();house.markSpawned(origin);var store=l.getServer().overworld().getDataStorage();store.set("the_oldest_house",house);store.set("the_oldest_house_labyrinth",new LabyrinthData());d().setBuilt(LabyrinthBuilder.VERSION,origin);b=LabyrinthPlaces.base(origin,LabyrinthPlace.ELK_FAN);chunks.hold(l,new AABB(Vec3.atLowerCornerOf(b.offset(-12,-2,-30)),Vec3.atLowerCornerOf(b.offset(13,10,4))));LiteraryRooms.build(l,b,LabyrinthPlace.ELK_FAN);LabyrinthBuilder.registerDoors(d(),LabyrinthPlace.ELK_FAN,b);active=this;}
        LabyrinthData d(){return LabyrinthData.get(l.getServer());}
        ServerPlayer player(String name){var p=NativeTestPlayers.survival(h,name);players.add(p);p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-3.5,180,0);p.hasChangedDimension();p.setNoGravity(true);LiteraryVignettes.onArrive(p,LabyrinthPlace.ELK_FAN);return p;}
        void readSource(ServerPlayer p){var at=b.offset(LiteraryRooms.source(LabyrinthPlace.ELK_FAN));p.moveTo(at.getX()+.5,b.getY(),at.getZ()+2.5);p.lookAt(EntityAnchorArgument.Anchor.EYES,at.getCenter());var hit=l.clip(new ClipContext(p.getEyePosition(),p.getEyePosition().add(p.getLookAngle().scale(4.5)),ClipContext.Block.OUTLINE,ClipContext.Fluid.NONE,p));h.assertTrue(hit.getBlockPos().equals(at),"the original bedside paper is reached by a native ray");p.gameMode.useItemOn(p,l,ItemStack.EMPTY,InteractionHand.MAIN_HAND,hit);h.assertTrue(p.containerMenu instanceof LiteraryVignettes.Pages,"the original scene paper opens");var menu=(LiteraryVignettes.Pages)p.containerMenu;h.assertTrue(menu.clickMenuButton(p,101),"the real last source page is read");p.closeContainer();}
        @Override public void close(){LiteraryVignettes.clearAll();extras.forEach(Entity::discard);players.forEach(NativeTestPlayers::remove);chunks.close();var store=l.getServer().overworld().getDataStorage();store.set("the_oldest_house",oldHouse);store.set("the_oldest_house_labyrinth",oldData);}
    }
    @AfterBatch(batch="elk_fan_climb0480") public static void climbed(ServerLevel l){if(active!=null){active.close();active=null;}}
    @AfterBatch(batch="elk_fan_saved0480") public static void repaired(ServerLevel l){if(active!=null){active.close();active=null;}}
    @AfterBatch(batch="elk_fan_ending0488") public static void ended(ServerLevel l){if(active!=null){active.close();active=null;}}
    @AfterBatch(batch="elk_fan_bulb0488") public static void bulbs(ServerLevel l){if(active!=null){active.close();active=null;}}

    @GameTest(template="empty",batch="elk_fan_ending0488",timeoutTicks=360)
    public static void FiveContinuousSecondsKnockOffCompleteAndReturnThroughTheActualSavedDoor(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("fan_five_seconds");var peer=f.player("fan_uninvolved");var back=Vec3.atBottomCenterOf(f.b.offset(0,0,40));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the actual fan room is entity-ready")).thenExecute(()->{
            f.readSource(p);p.moveTo(f.b.getX()+.5,f.b.getY()+5.25,f.b.getZ()-16.5);p.lookAt(EntityAnchorArgument.Anchor.EYES,f.b.offset(0,0,-17).getCenter());
            f.d().pushReturn(p.getUUID(),new LabyrinthData.Waypoint(f.l.dimension(),back,0,true));
        }).thenIdle(60).thenExecute(()->{
            h.assertTrue(!LiteraryVignettes.personal(f.d(),p.getUUID(),LabyrinthPlace.ELK_FAN).getBoolean("BladeStrike"),"three seconds cannot complete the five-second platform wait");p.moveTo(f.b.getX()+4,f.b.getY()+5.25,f.b.getZ()-16.5);
        }).thenIdle(10).thenExecute(()->{
            h.assertTrue(LiteraryVignettes.personal(f.d(),p.getUUID(),LabyrinthPlace.ELK_FAN).getInt("FanTicks")==0,"stepping away resets the continuous platform wait");p.moveTo(f.b.getX()+.5,f.b.getY()+5.25,f.b.getZ()-16.5);p.lookAt(EntityAnchorArgument.Anchor.EYES,f.b.offset(0,0,-17).getCenter());
        }).thenIdle(90).thenExecute(()->h.assertTrue(!LiteraryVignettes.personal(f.d(),p.getUUID(),LabyrinthPlace.ELK_FAN).getBoolean("BladeStrike"),"even ninety continuous native ticks are short of five seconds"))
        .thenWaitUntil(()->h.assertTrue(!LabyrinthDoors.isBusy(p)&&!LiteraryVignettes.inside(p,LabyrinthPlace.ELK_FAN)&&f.d().returnDepth(p.getUUID())==0,"the blade strike uses the actual native saved door return"))
        .thenExecute(()->{
            var own=LiteraryVignettes.personal(f.d(),p.getUUID(),LabyrinthPlace.ELK_FAN);
            h.assertTrue(own.getBoolean("BladeStrike")&&own.getBoolean("Completed")&&own.getBoolean("ElkThroughFan")&&WitnessAccount.has(f.d(),p.getUUID(),WitnessAccount.Story.of(LabyrinthPlace.ELK_FAN.id()))&&p.position().distanceToSqr(back.add(0,0,1))<.01,"the real hold resolves and returns beside the original door");
            h.assertTrue(p.getInventory().countItem(LiteraryRegistry.TOOTH.get())==1&&!LiteraryVignettes.personal(f.d(),peer.getUUID(),LabyrinthPlace.ELK_FAN).getBoolean("BladeStrike"),"the ending has one finite tooth and does not complete the uninvolved peer");
        }).thenSucceed();
    }
    @GameTest(template="empty",batch="elk_fan_bulb0488",timeoutTicks=160)
    public static void BorrowedBulbsAndEmptyHandsCannotRepairAndTheTrayIsFinite(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("fan_bulb_owner");var peer=f.player("fan_bulb_borrower");
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the native fitting is ready")).thenExecute(()->{
            var own=LiteraryVignettes.personal(f.d(),p.getUUID(),LabyrinthPlace.ELK_FAN);var other=LiteraryVignettes.personal(f.d(),peer.getUUID(),LabyrinthPlace.ELK_FAN);ElkFanSequence.click(p,f.b,SceneHuntReview.TOOLS,own);
            var bulb=p.getInventory().items.stream().filter(s->s.is(LiteraryRegistry.LIGHTBULB.get())).findFirst().orElseThrow().copy();p.moveTo(f.b.getX()+.5,f.b.getY()+5.25,f.b.getZ()-16.5);peer.moveTo(p.position());peer.setItemInHand(InteractionHand.MAIN_HAND,bulb.copy());
            ElkFanSequence.click(peer,f.b,LiteraryRooms.FAN,other);h.assertTrue(!other.getBoolean("Repaired")&&peer.getMainHandItem().getCount()==1,"another reader's bulb cannot authorize repair");p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);ElkFanSequence.click(p,f.b,LiteraryRooms.FAN,own);h.assertTrue(!own.getBoolean("Repaired"),"an empty hand does not repair a missing bulb");
            p.setItemInHand(InteractionHand.MAIN_HAND,bulb);ElkFanSequence.click(p,f.b,LiteraryRooms.FAN,own);h.assertTrue(own.getBoolean("Repaired")&&p.getMainHandItem().isEmpty(),"the actual owned replacement is consumed once");ElkFanSequence.click(p,f.b,SceneHuntReview.TOOLS,own);h.assertTrue(p.getInventory().countItem(LiteraryRegistry.LIGHTBULB.get())==0&&p.getInventory().countItem(Items.OAK_PLANKS)==0,"the tray never refills an issued bulb or supplies planks");
        }).thenSucceed();
    }

    @GameTest(template="empty",batch="elk_fan_climb0480",timeoutTicks=700)
    public static void nativeAFrameClimbReachesTheFanAndItsOpenPlatformKeepsTheImageVisible(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("fan_ladder_reader");final boolean[] walking={false};
        h.onEachTick(()->{if(walking[0]&&(p.getY()<f.b.getY()+5.24||p.getZ()>f.b.getZ()-16.5)){p.setYRot(180);p.travel(new Vec3(0,0,1));}});
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the room's native entity sections are ready")).thenExecute(()->{
            h.assertTrue(ElkFanRoom.standing(f.l,f.b),"the whole custom A-frame stands, with actual collision parts");
            f.readSource(p);
            var tray=f.b.offset(SceneHuntReview.TOOLS);p.moveTo(tray.getX()+.5,f.b.getY(),tray.getZ()+2.5);p.lookAt(EntityAnchorArgument.Anchor.EYES,f.l.getBlockState(tray).getShape(f.l,tray).bounds().getCenter().add(Vec3.atLowerCornerOf(tray)));
            var ray=f.l.clip(new ClipContext(p.getEyePosition(),p.getEyePosition().add(p.getLookAngle().scale(4.5)),ClipContext.Block.OUTLINE,ClipContext.Fluid.NONE,p));
            h.assertTrue(ray.getBlockPos().equals(tray),"the visible tool tray is within native reach: hit="+ray.getBlockPos().subtract(f.b));p.gameMode.useItemOn(p,f.l,ItemStack.EMPTY,InteractionHand.MAIN_HAND,ray);
            h.assertTrue(p.getInventory().countItem(LiteraryRegistry.LIGHTBULB.get())==1&&p.getInventory().countItem(Items.OAK_PLANKS)==0,"the native tool tray provides one real bulb and no planks");
            for(int slot=0;slot<9;slot++)if(p.getInventory().getItem(slot).is(LiteraryRegistry.LIGHTBULB.get()))p.getInventory().selected=slot;
            p.moveTo(f.b.getX()+.5,f.b.getY(),f.b.getZ()-13.5);p.setDeltaMovement(Vec3.ZERO);p.setNoGravity(false);walking[0]=true;
        }).thenWaitUntil(()->h.assertTrue(p.getY()>=f.b.getY()+5.24&&p.getZ()<=f.b.getZ()-16.5,"native travel climbs the A-frame and walks onto its working platform: "+p.position().subtract(Vec3.atLowerCornerOf(f.b))))
        .thenExecute(()->{
            walking[0]=false;p.setNoGravity(true);p.setDeltaMovement(Vec3.ZERO);
            h.assertTrue(ElkFanRoom.onLadder(p,f.b),"the reader reaches the real working platform without placed blocks");
            var at=f.b.offset(LiteraryRooms.FAN);p.lookAt(EntityAnchorArgument.Anchor.EYES,new Vec3(at.getX()+.5,at.getY()+.72,at.getZ()+.5));
            var hit=f.l.clip(new ClipContext(p.getEyePosition(),p.getEyePosition().add(p.getLookAngle().scale(4.5)),ClipContext.Block.OUTLINE,ClipContext.Fluid.NONE,p));
            h.assertTrue(hit.getBlockPos().equals(at),"the visible fan motor is within native survival interaction reach: hit="+hit.getBlockPos().subtract(f.b)+", reader="+p.position().subtract(Vec3.atLowerCornerOf(f.b)));p.gameMode.useItemOn(p,f.l,p.getMainHandItem(),InteractionHand.MAIN_HAND,hit);
            h.assertTrue(LiteraryVignettes.personal(f.d(),p.getUUID(),LabyrinthPlace.ELK_FAN).getBoolean("Repaired"),"a native click consumes the owned bulb and repairs the actual fan");
            h.assertTrue(p.getInventory().countItem(LiteraryRegistry.LIGHTBULB.get())==0&&f.l.getBlockState(at).getLightEmission(f.l,at)==11,"the actual bulb is consumed and the repaired native fitting emits light");
            p.lookAt(EntityAnchorArgument.Anchor.EYES,f.b.offset(0,0,-17).getCenter());
            h.assertTrue(HouseWatchers.sees(p,f.b.offset(0,0,-17).getCenter()),"the open platform preserves a real line of sight to the image below the blades");
            h.assertTrue(!WitnessAccount.has(f.d(),p.getUUID(),WitnessAccount.Story.of(LabyrinthPlace.ELK_FAN.id())),"climbing and repair alone never grant the later personal ending");
            h.succeed();
        });
    }

    @GameTest(template="empty",batch="elk_fan_saved0480",timeoutTicks=450)
    public static void savedFanLadderWaitsForBodiesAndCamerasAndPreservesOriginalsAndRemovedParts(GameTestHelper h){
        var f=new Fixture(h);var p=f.player("saved_fan_reader");
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"saved-room native sections are ready")).thenExecute(()->{
            f.readSource(p);var record=LiteraryVignettes.personal(f.d(),p.getUUID(),LabyrinthPlace.ELK_FAN).copy();
            ElkFanRoom.frame(f.b).keySet().forEach(at->f.l.setBlock(at,Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE));
            for(int y=0;y<4;y++){f.l.setBlock(f.b.offset(0,y,-18),Blocks.DARK_OAK_PLANKS.defaultBlockState(),3);f.l.setBlock(f.b.offset(0,y,-17),Blocks.LADDER.defaultBlockState().setValue(net.minecraft.world.level.block.LadderBlock.FACING,Direction.SOUTH),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);}
            f.d().setState(ElkFanRoom.STATE,new CompoundTag());
            var at=f.b.offset(7,0,-13);f.l.setBlock(at,Blocks.BARREL.defaultBlockState(),3);var barrel=(BarrelBlockEntity)f.l.getBlockEntity(at);var original=new ItemStack(Items.DIAMOND,3);original.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Already here"));barrel.setItem(4,original);var exact=original.copy();
            h.assertTrue(!ElkFanRoom.repair(f.l,f.b),"a native reader camera postpones the saved repair");
            p.moveTo(f.b.getX()+100,f.b.getY(),f.b.getZ());var cat=EntityType.CAT.create(f.l);cat.setNoAi(true);cat.tame(p);cat.setNoGravity(true);cat.moveTo(f.b.getX()+.5,f.b.getY()+4,f.b.getZ()-16.5);h.assertTrue(f.l.addFreshEntity(cat),"the resident has a native tracked body");f.extras.add(cat);var uuid=cat.getUUID();
            h.assertTrue(!ElkFanRoom.repair(f.l,f.b),"a living resident prevents the repair");cat.moveTo(f.b.getX()+80,f.b.getY(),f.b.getZ());
            h.assertTrue(ElkFanRoom.repair(f.l,f.b)&&ElkFanRoom.standing(f.l,f.b),"an empty, unwatched, loaded saved room receives the complete ladder");
            h.assertTrue(barrel==f.l.getBlockEntity(at)&&ItemStack.matches(barrel.getItem(4),exact)&&record.equals(LiteraryVignettes.personal(f.d(),p.getUUID(),LabyrinthPlace.ELK_FAN))&&cat.isAlive()&&cat.getUUID().equals(uuid),"native container identity, finite components, reader records and resident identity stay exact");
            var done=new CompoundTag();done.putBoolean("Done",true);done.putInt("Version",ElkFanRoom.VERSION);f.d().setStateEntry(ElkFanRoom.STATE,Long.toString(f.origin.asLong()),done);var removed=ElkFanRoom.frame(f.b).keySet().iterator().next();f.l.setBlock(removed,Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);
            h.runAfterDelay(90,()->{h.assertTrue(f.l.getBlockState(removed).isAir(),"the saved once-only checkpoint does not replace a later removed part");h.succeed();});
        });
    }
}
