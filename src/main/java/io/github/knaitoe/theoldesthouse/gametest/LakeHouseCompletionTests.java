package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.*;

/** Native survival readers, real fitted rooms, owner tracking, saved originals and all three physical choices. */
@GameTestHolder(TheOldestHouse.MOD_ID+"_lake_house") @PrefixGameTestTemplate(false)
public final class LakeHouseCompletionTests {
    private static final LabyrinthPlace PLACE=LabyrinthPlace.LAKE_HOUSE;
    private LakeHouseCompletionTests(){}
    private static CompoundTag own(LakeHouseTests.Fixture f,ServerPlayer p){return LiteraryVignettes.personal(f.data(),p.getUUID(),PLACE);}
    private static void at(LakeHouseTests.Fixture f,ServerPlayer p,double x,double y,double z){
        p.stopRiding();p.setForcedPose(y<-2?Pose.SWIMMING:null);p.setPose(y<-2?Pose.SWIMMING:Pose.STANDING);p.refreshDimensions();var b=f.base();p.teleportTo(f.out,b.getX()+x,b.getY()+y,b.getZ()+z,180,0);p.setDeltaMovement(Vec3.ZERO);p.baseTick();
    }
    private static void readTick(LakeHouseTests.Fixture f,ServerPlayer p,int ticks){for(int n=0;n<ticks;n+=5){var state=own(f,p);LakeHouse.reader(p,LakeHouse.readerBase(p),state);LiteraryVignettes.save(f.data(),p.getUUID(),PLACE,state);}}
    private static void returnVisit(LakeHouseTests.Fixture f,ServerPlayer p){
        LakeHouseStory.depart(p);p.teleportTo(f.in,0,90,0,0,0);at(f,p,.5,0,-3.5);LiteraryVignettes.onArrive(p,PLACE);
    }
    private static void hello(LakeHouseTests.Fixture f,ServerPlayer p){
        f.read(p,LakeHouse.SOURCE);
        at(f,p,.5,-24,-164.5);readTick(f,p,5);at(f,p,7.5,0,-173.5);readTick(f,p,5);f.h.assertTrue(own(f,p).getInt("Beat")==1,"a real foyer dive and return earns the first beat");
        returnVisit(f,p);at(f,p,7.5,0,-173.5);readTick(f,p,5);f.h.assertTrue(LakeHouseGear.has(p,LakeHouseRegistry.HELMET.get()),"visit two lends the helmet at the raft");
        at(f,p,.5,-24,-175.5);readTick(f,p,5);at(f,p,7.5,0,-173.5);readTick(f,p,5);f.h.assertTrue(own(f,p).getInt("Beat")==2,"the actual back of the ground floor and return earn beat two");
        returnVisit(f,p);at(f,p,7.5,0,-173.5);readTick(f,p,5);f.h.assertTrue(LakeHouseGear.has(p,LakeHouseRegistry.TANKS.get()),"visit three lends the tanks");
        at(f,p,-5.5,-19,-166.5);p.lookAt(EntityAnchorArgument.Anchor.EYES,f.base().offset(LakeHouseInterior.DRESSING).getCenter());readTick(f,p,5);
        at(f,p,3.5,-19,-164.5);readTick(f,p,5);at(f,p,5.5,-40,-175.5);readTick(f,p,5);at(f,p,7.5,0,-173.5);readTick(f,p,5);
        f.h.assertTrue(own(f,p).getInt("Beat")==3&&own(f,p).getBoolean("HomePending"),"the dressing room, bedroom and pool earn the third return and the private home haunting");
        returnVisit(f,p);at(f,p,5.5,-40,-165.5);readTick(f,p,LakeHouseStory.SAUNA_TICKS+10);
        f.h.assertTrue(own(f,p).getBoolean("Lit")&&own(f,p).getBoolean("HelloStage")&&!own(f,p).getBoolean("Ready"),"the fourth visit's actual sauna wait lights the house without granting an ending");
    }
    private static void empty(LakeHouseTests.Fixture f){var b=LakeHouse.emptyBase(f.in.getServer());LakeHouse.buildEmpty(f.out,b);LabyrinthBuilder.registerDoors(f.data(),LabyrinthPlace.LAKE_HOUSE_EMPTY,b);}
    @GameTest(template="empty",batch="lake_house_hello",timeoutTicks=800)
    public static void touchingThePrivateFigureEndsInTheEmptyLakeWithOneBoundFlashlight(GameTestHelper h){
        var f=new LakeHouseTests.Fixture(h);empty(f);var end=LakeHouse.emptyBase(f.in.getServer());f.lease.hold(f.out,new AABB(Vec3.atLowerCornerOf(end.offset(-6,-1,-23)),Vec3.atLowerCornerOf(end.offset(6,8,8))));var p=f.player("lake_hello");var peer=f.player("lake_unfinished");f.ready(()->{try{
            hello(f,p);
            at(f,p,-4.5,-19,-166.5);readTick(f,p,5);var tag=own(f,p);var figure=f.out.getEntity(tag.getUUID("Figure"));h.assertTrue(figure instanceof LiteraryActor,"a real native wax figure is there");
            h.assertTrue(!figure.broadcastToPlayer(peer),"the peer never receives the reader's wax figure");LiteraryVignettes.talk(p,(LiteraryActor)figure);readTick(f,p,110);
            h.assertTrue(LakeHouse.empty(p)&&LakeHouse.SAID_HELLO.equals(own(f,p).getString("Outcome"))&&!own(f,peer).getBoolean("Ready"),"the touch takes only this reader to the shared empty lake");
            var artifacts=p.getInventory().items.stream().filter(s->s.is(LakeHouseRegistry.FOUND_FLASHLIGHT.get())).toList();h.assertTrue(artifacts.size()==1&&LakeHouseArtifactItem.owned(artifacts.getFirst(),p),"exactly one original bound flashlight");
            var chosen=LakeHouse.destinationEntry(p,f.data().door(PLACE.entryDoorId()),PLACE);h.assertTrue(chosen.id.equals(LabyrinthPlace.LAKE_HOUSE_EMPTY.entryDoorId()),"the same future story door routes a finished reader to the empty lake");
            var b=LakeHouse.readerBase(p);h.assertTrue(f.out.getBlockState(b.offset(LakeHouse.HOUSE)).is(Blocks.WATER)&&f.out.getBlockEntity(b.offset(LakeHouse.SOURCE)) instanceof net.minecraft.world.level.block.entity.LecternBlockEntity,"no house, with the rental log still accessible");
            f.read(p,LakeHouse.ACCOUNT);h.assertTrue(WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.LAKE_HOUSE)&&!WitnessAccount.has(f.data(),peer.getUUID(),WitnessAccount.Story.LAKE_HOUSE),"the read account resolves its own reader only");
            LakeHouseStory.complete(p,own(f,p),LakeHouse.SAID_HELLO);h.assertTrue(p.getInventory().countItem(LakeHouseRegistry.FOUND_FLASHLIGHT.get())==1,"an ending cannot mint another artifact");h.succeed();
        }finally{f.close();}});
    }
    @GameTest(template="empty",batch="lake_house_lights",timeoutTicks=800)
    public static void leavingThroughTheHalfDoorKeepsOnlyThisReadersLightsOn(GameTestHelper h){
        var f=new LakeHouseTests.Fixture(h);var p=f.player("lake_lights");var peer=f.player("lake_dark");f.ready(()->{try{hello(f,p);
            at(f,p,.5,-24,-159.5);readTick(f,p,110);h.assertTrue(LakeHouse.LEFT_THE_LIGHTS_ON.equals(own(f,p).getString("Outcome"))&&own(f,p).getBoolean("Lit")&&!own(f,peer).getBoolean("Lit"),"the actual half-door exit keeps just one reader's lights");
            h.assertTrue(p.getInventory().countItem(LakeHouseRegistry.HOUSE_LIGHT.get())==1&&!LakeHouse.empty(p),"one house light and the original physical house");
            for(var lamp:LakeHouseInterior.LAMPS)h.assertTrue(!f.out.getBlockState(f.base().offset(lamp)).getValue(LakeHouseFittingBlock.LIT),"the server's shared lamps stay dark for other readers");
            returnVisit(f,p);at(f,p,.5,-24,-164.5);readTick(f,p,10);h.assertTrue(p.hasEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION)&&!peer.hasEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION),"the lit rooms have independent actual native brightness");h.succeed();
        }finally{f.close();}});
    }
    @GameTest(template="empty",batch="lake_house_chair",timeoutTicks=800)
    public static void stayingInTheDryChairWakesOnTheRaftWithoutChangingWorldTime(GameTestHelper h){
        var f=new LakeHouseTests.Fixture(h);var p=f.player("lake_stayed");f.ready(()->{try{hello(f,p);var chair=f.base().offset(LakeHouseInterior.TABLE_PLACE);
            at(f,p,-2.7,-25,-165.5);var click=new PlayerInteractEvent.RightClickBlock(p,InteractionHand.MAIN_HAND,chair,new BlockHitResult(chair.getCenter(),Direction.EAST,chair,false));NeoForge.EVENT_BUS.post(click);
            h.assertTrue(p.getVehicle() instanceof LakeHouseSeat&&!f.out.getFluidState(chair).is(net.minecraft.world.level.material.Fluids.WATER),"a native seated reader in a deliberately dry chair");
            LakeHouseDive.setAir(p,20);LakeHouseDive.breathe(p,f.base());h.assertTrue(p.getAirSupply()>20,"the chair's own air lets the seated reader breathe");
            long time=f.in.getServer().overworld().getDayTime();readTick(f,p,LakeHouseStory.STAY_TICKS+110);
            h.assertTrue(LakeHouse.STAYED.equals(own(f,p).getString("Outcome"))&&p.getInventory().countItem(LakeHouseRegistry.GOLD_HELMET.get())==1&&!p.isPassenger(),"the actual seated wait ends on the raft with the gold helmet");
            h.assertTrue(time==f.in.getServer().overworld().getDayTime()&&!own(f,p).getBoolean("Lit"),"the private dawn changes no world time and the house stays dark");h.succeed();
        }finally{f.close();}});
    }
    @GameTest(template="empty",batch="lake_house_tess",timeoutTicks=800)
    public static void tessIsPrivateLeavesWithoutPoppingAndNeverReturnsMidVisit(GameTestHelper h){
        var f=new LakeHouseTests.Fixture(h);var lease=new NativeTestChunks();lease.hold(f.out,new AABB(Vec3.atLowerCornerOf(f.base().offset(-8,-1,-22)),Vec3.atLowerCornerOf(f.base().offset(8,12,2))));var p=f.player("lake_solo");
        Runnable[] action=new Runnable[1];action[0]=()->{if(!lease.ready()){h.runAfterDelay(1,action[0]);return;}try{
            at(f,p,.5,0,-15.5);readTick(f,p,5);var tess=LakeHouseTess.actor(p,own(f,p));h.assertTrue(tess!=null&&tess.role()==LiteraryActor.LAKE_TESS,"a real original Tess appears for a solo reader");
            var observer=f.player("lake_observer");observer.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.SPECTATOR);h.assertTrue(!tess.broadcastToPlayer(observer),"spectators never receive Tess and do not dismiss her");readTick(f,p,5);h.assertTrue(!own(f,p).getBoolean("SoloGone"),"a spectator is not a partner");
            var peer=f.player("lake_partner");p.lookAt(EntityAnchorArgument.Anchor.EYES,tess.getEyePosition());readTick(f,p,5);h.assertTrue(tess.isAlive()&&!tess.broadcastToPlayer(peer)&&own(f,p).getBoolean("SoloGone"),"a watched Tess stays physically present while starting her departure");
            p.setYRot(p.getYRot()+180);p.setXRot(0);readTick(f,p,5);h.assertTrue(tess.isRemoved(),"she can leave once her reader's actual camera turns away");
            peer.teleportTo(f.in,0,90,0,0,0);readTick(f,p,10);LiteraryVignettes.onArrive(p,PLACE);readTick(f,p,5);h.assertTrue(LakeHouseTess.actor(p,own(f,p))==null,"the partner leaving or reconnecting does not bring her back mid-visit");
            returnVisit(f,p);readTick(f,p,5);h.assertTrue(LakeHouseTess.actor(p,own(f,p))!=null,"a later real departure and solo arrival can bring her back");h.succeed();
        }finally{lease.close();f.close();}};action[0].run();
    }
    @GameTest(template="empty",batch="lake_house_rule",timeoutTicks=800)
    public static void miningLiftsAnOriginalWarnsThenBreaksTheRuleAndPersists(GameTestHelper h){
        var f=new LakeHouseTests.Fixture(h);var p=f.player("lake_rule");at(f,p,-5.5,-24,-174.5);var at=f.base().offset(LakeHouseInterior.ASH);var original=f.out.getBlockState(at);
        var first=new PlayerInteractEvent.LeftClickBlock(p,at,Direction.SOUTH,PlayerInteractEvent.LeftClickBlock.Action.START);NeoForge.EVENT_BUS.post(first);
        h.assertTrue(first.isCanceled()&&own(f,p).getInt("RuleAttempts")==1&&!own(f,p).getBoolean("RuleBroken")&&f.out.getBlockState(at)==original,"the first native mining packet warns and leaves the actual original intact");
        h.runAfterDelay(25,()->{try{var second=new PlayerInteractEvent.LeftClickBlock(p,at,Direction.SOUTH,PlayerInteractEvent.LeftClickBlock.Action.START);NeoForge.EVENT_BUS.post(second);
            h.assertTrue(second.isCanceled()&&own(f,p).getBoolean("RuleBroken")&&f.out.getBlockState(at)==original,"a later real attempt breaks the personal rule, without breaking or dropping the shared prop");
            var saved=f.data().save(new CompoundTag(),f.in.registryAccess());var reloaded=LabyrinthData.load(saved,f.in.registryAccess());h.assertTrue(LiteraryVignettes.personal(reloaded,p.getUUID(),PLACE).getBoolean("RuleBroken"),"the rule survives native SavedData serialization");h.succeed();
        }finally{f.close();}});
    }
    @GameTest(template="empty",batch="lake_house_artifacts",timeoutTicks=800)
    public static void earnedHelmetAndLightKeepTheirOwnerAndTheirEffectsOutsideTheHouse(GameTestHelper h){
        var f=new LakeHouseTests.Fixture(h);var p=f.player("lake_artifact");var peer=f.player("lake_borrower");var home=f.in.getServer().overworld();var homeAt=new BlockPos(12000+Math.floorMod(p.getUUID().hashCode(),1024)*16,90,12000);peer.teleportTo(home,homeAt.getX()+3.5,90,homeAt.getZ()+.5,0,0);peer.hasChangedDimension();f.lease.hold(home,new AABB(Vec3.atLowerCornerOf(homeAt.offset(-2,-5,-2)),Vec3.atLowerCornerOf(homeAt.offset(6,5,6))));f.ready(()->{try{var gold=LiteraryVignettes.mark(p,PLACE,"Artifact",new ItemStack(LakeHouseRegistry.GOLD_HELMET.get()));
            var world=p.server.overworld();p.setForcedPose(null);p.teleportTo(world,homeAt.getX()+.5,90,homeAt.getZ()+.5,0,0);peer.teleportTo(world,homeAt.getX()+3.5,90,homeAt.getZ()+.5,0,0);
            p.setItemSlot(EquipmentSlot.HEAD,gold);peer.setItemSlot(EquipmentSlot.HEAD,gold.copy());p.setAirSupply(10);peer.setAirSupply(10);LakeHouseArtifactItem.tick(new PlayerTickEvent.Post(p));LakeHouseArtifactItem.tick(new PlayerTickEvent.Post(peer));
            h.assertTrue(p.getAirSupply()==p.getMaxAirSupply()&&peer.getAirSupply()==10&&!(gold.getItem() instanceof ArmorItem),"unlimited outside breath, no armour, and no borrowed effect");
            at(f,p,.5,-24,-164.5);LakeHouseDive.setAir(p,50);LakeHouseArtifactItem.tick(new PlayerTickEvent.Post(p));LakeHouseDive.breathe(p,f.base());h.assertTrue(p.getAirSupply()==49,"the earned helmet never bypasses the vignette's lent-gear rule");
            p.setForcedPose(null);p.teleportTo(world,homeAt.getX()+.5,90,homeAt.getZ()+.5,0,0);var support=homeAt.offset(0,-1,2);world.setBlock(support,Blocks.STONE.defaultBlockState(),3);
            var lamp=LiteraryVignettes.mark(p,PLACE,"Artifact",new ItemStack(LakeHouseRegistry.HOUSE_LIGHT.get()));lamp.set(DataComponents.CUSTOM_NAME,Component.literal("My original house light"));p.setItemInHand(InteractionHand.MAIN_HAND,lamp);
            var context=new UseOnContext(p,InteractionHand.MAIN_HAND,new BlockHitResult(support.getCenter(),Direction.UP,support,false));LakeHouseRegistry.HOUSE_LIGHT.get().useOn(context);var at=support.above();
            h.assertTrue(world.getBlockEntity(at) instanceof LakeHouseLightBlock.Lamp placed&&LakeHouseArtifactItem.owned(placed.original(),p)&&placed.original().getHoverName().getString().equals("My original house light")&&p.getMainHandItem().isEmpty(),"placing the lamp moves the exact finite original into native block custody");
            p.hasChangedDimension();p.invulnerableTime=0;p.hurt(world.damageSources().genericKill(),Float.MAX_VALUE);h.assertTrue(!p.isAlive(),"the reader actually dies outside the House");
            var fresh=p.server.getPlayerList().respawn(p,false,Entity.RemovalReason.KILLED);fresh.connection.player=fresh;f.players.remove(p);f.players.add(fresh);fresh.hasChangedDimension();fresh.setNoGravity(true);
            h.assertTrue(fresh.isAlive()&&fresh.serverLevel()==world&&fresh.blockPosition().distManhattan(at)<=2,"the original bound lamp returns its reader through actual native death and respawn");
            world.destroyBlock(at,false,fresh);var drops=world.getEntitiesOfClass(ItemEntity.class,new AABB(at).inflate(2),q->q.getItem().is(LakeHouseRegistry.HOUSE_LIGHT.get()));h.assertTrue(drops.size()==1&&LakeHouseArtifactItem.owned(drops.getFirst().getItem(),p)&&drops.getFirst().getItem().getHoverName().getString().equals("My original house light"),"removal returns exactly that named owner-bound original once");h.succeed();
        }finally{f.close();}});
    }
}
