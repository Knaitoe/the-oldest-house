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
import net.minecraft.server.level.ServerLevel;
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
    private static void at(LakeHouseTests.Fixture f,ServerPlayer p,double x,double y,double z){at(f,p,x,y,z,180);}
    private static void at(LakeHouseTests.Fixture f,ServerPlayer p,double x,double y,double z,float yaw){
        p.stopRiding();p.setForcedPose(y<-2?Pose.SWIMMING:null);p.setPose(y<-2?Pose.SWIMMING:Pose.STANDING);p.refreshDimensions();var b=f.base();p.teleportTo(f.out,b.getX()+x,b.getY()+y,b.getZ()+z,yaw,0);p.setDeltaMovement(Vec3.ZERO);p.baseTick();
    }
    /**
     * 0.4.560: a reader on the dock looking back at the boathouse, so that Tess, who never comes into being where they can see, is
     * there on the dock's planks behind them.
     */
    private static void onTheDock(LakeHouseTests.Fixture f,ServerPlayer p){at(f,p,.5,0,-15.5,0);}
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
        at(f,p,-2.5,-24.5,-175.5);p.lookAt(EntityAnchorArgument.Anchor.EYES,f.base().offset(LakeHouseInterior.CLOSET).getCenter());readTick(f,p,10);
        f.h.assertTrue(own(f,p).getBoolean("ClosetHint0505"),"bubbles round the study wardrobe show the way down without saying it");
        at(f,p,-5.5,-19,-166.5);p.lookAt(EntityAnchorArgument.Anchor.EYES,f.base().offset(LakeHouseInterior.DRESSING).getCenter());readTick(f,p,5);
        at(f,p,3.5,-19,-164.5);readTick(f,p,5);at(f,p,5.5,-40,-175.5);readTick(f,p,5);at(f,p,7.5,0,-173.5);readTick(f,p,5);
        f.h.assertTrue(own(f,p).getInt("Beat")==3&&own(f,p).getBoolean("HomePending"),"the dressing room, bedroom and pool earn the third return and the private home haunting");
        returnVisit(f,p);at(f,p,-2.5,-24.5,-175.5);readTick(f,p,10);f.h.assertTrue(own(f,p).getBoolean("WarmHint0505"),"on the last visit warm water rising behind the wardrobe points to the sauna");
        at(f,p,5.5,-40,-165.5);readTick(f,p,LakeHouseStory.SAUNA_TICKS+10);
        f.h.assertTrue(own(f,p).getBoolean("Lit")&&own(f,p).getBoolean("HelloStage")&&!own(f,p).getBoolean("Ready"),"the fourth visit's actual sauna wait lights the house without granting an ending");
    }
    // ------------------------------------------------------------------------------------------------ 0.4.156: in pairs
    private static LakeHouseTests.Fixture pairs;
    @AfterBatch(batch="lake_house_pairs156") public static void pairsDone(net.minecraft.server.level.ServerLevel l){SceneInstances.clear();if(pairs!=null){pairs.close();pairs=null;}}
    private static ServerPlayer reader(LakeHouseTests.Fixture f,String name){var p=NativeTestPlayers.survival(f.h,name);f.players.add(p);away(f,p);return p;}
    private static void away(LakeHouseTests.Fixture f,ServerPlayer p){p.stopRiding();p.teleportTo(f.in,0,90,0,0,0);p.setDeltaMovement(Vec3.ZERO);p.hasChangedDimension();}
    private static void in(LakeHouseTests.Fixture f,ServerPlayer p,BlockPos b,double x,double y,double z){p.stopRiding();p.teleportTo(f.out,b.getX()+x,b.getY()+y,b.getZ()+z,0,0);p.setDeltaMovement(Vec3.ZERO);p.hasChangedDimension();p.setNoGravity(true);}
    private static AABB grounds(BlockPos b){return new AABB(Vec3.atLowerCornerOf(b.offset(-12,-44,-183)),Vec3.atLowerCornerOf(b.offset(12,12,8)));}
    @GameTest(template="empty",batch="lake_house_pairs156",timeoutTicks=9000)
    public static void aSecondReaderGoesDownInTessPlaceAndAThirdHasALakeOfTheirOwn(GameTestHelper h){
        var f=new LakeHouseTests.Fixture(h);pairs=f;var s=f.in.getServer();
        var diver=reader(f,"pair_diver");var partner=reader(f,"pair_partner");var third=reader(f,"pair_third");var fourth=reader(f,"pair_fourth");var fifth=reader(f,"pair_fifth");
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.lease.ready(),"the lake is resident"))
            .thenExecute(()->{
                h.assertTrue(SceneInstances.copyable(PLACE),"the lake house has copies now");
                var first=SceneInstances.enter(diver,PLACE);h.assertTrue(first.door()!=null&&first.copy()==0,"the first reader takes the lake");
                in(f,diver,f.base(),.5,0,-3.5);LiteraryVignettes.onArrive(diver,PLACE);
                var second=SceneInstances.enter(partner,PLACE);
                h.assertTrue(second.door()!=null&&second.copy()==0&&diver.getUUID().equals(SceneInstances.diverOf(f.data(),partner.getUUID())),"the second reader joins them, in Tess's place");
                in(f,partner,f.base(),1.5,0,-4.5);LiteraryVignettes.onArrive(partner,PLACE);
                // 0.4.558: they begin where Tess waits for the diver, on the dock, facing the way she does, with her flashlight.
                var dock=Vec3.atLowerCornerOf(f.base()).add(LakeHousePairs.tessSpot(1));
                h.assertTrue(partner.position().distanceTo(dock)<1.5&&Math.abs(net.minecraft.util.Mth.wrapDegrees(partner.getYRot()-180))<1&&own(f,partner).getInt(LakeHousePairs.STOOD_AT)==1,"the partner begins on the dock where Tess waits: "+partner.position().subtract(Vec3.atLowerCornerOf(f.base())));
                h.assertTrue(LakeHouseGear.has(partner,LakeHouseRegistry.FLASHLIGHT.get()),"with her flashlight, though they never passed the nail by the door");
                h.assertTrue(f.out.getEntitiesOfClass(LakeHouseCanoe.class,LakeHouse.box(f.base()),c->partner.getUUID().equals(c.owner())).isEmpty(),"the partner rides in their diver's canoe rather than one of their own");
                var full=SceneInstances.enter(third,PLACE);h.assertTrue(full.door()==null&&full.waiting(),"a third reader waits while a lake of their own is made ready");
                // The diver walks down to the dock to meet them, facing back the way they came: a Tess who had already come for the
                // diver alone leaves unseen.
                in(f,diver,f.base(),.5,0,-12.5);diver.setYRot(0);diver.setYHeadRot(0);readTick(f,diver,10);readTick(f,partner,5);var mine=own(f,diver);
                h.assertTrue(LakeHouseTess.actor(diver,mine)==null&&mine.getBoolean("SoloGone"),"with a reader in her place, Tess stays away");
                h.assertTrue(mine.getBoolean(LakeHousePartner.MET)&&own(f,partner).getBoolean(LakeHousePartner.MET),"the diver has met their partner on the dock, and the partner has had Tess's first line");
                h.assertTrue(LakeHouseGear.has(partner,LakeHouseRegistry.FLASHLIGHT.get()),"the partner takes their own flashlight by the door");
                var dress=LakeHouseRegistry.DRESS.get().create(f.out);dress.reader(diver.getUUID());dress.shape(LakeHouseFittingBlock.of(LakeHouseFittingBlock.Kind.DRESS,Direction.SOUTH),0,0);
                h.assertTrue(dress.broadcastToPlayer(partner)&&!dress.broadcastToPlayer(third),"the diver's dresses are seen by their partner and nobody else");
                var lit=own(f,diver);lit.putBoolean("Lit",true);LiteraryVignettes.save(f.data(),diver.getUUID(),PLACE,lit);
                h.assertTrue(LakeHouseStory.lit(partner)&&!LakeHouseStory.lit(third),"the house is lit for the partner when it is lit for their diver");
                lit.putBoolean("Lit",false);LiteraryVignettes.save(f.data(),diver.getUUID(),PLACE,lit);
                // The diver's ending lands: the partner has an account of their own and comes up with them.
                LakeHouseStory.complete(diver,own(f,diver),LakeHouse.LEFT_THE_LIGHTS_ON);var side=own(f,partner);
                h.assertTrue(side.getBoolean("Ready")&&side.getBoolean(LakeHousePairs.SIDE)&&LakeHouse.LEFT_THE_LIGHTS_ON.equals(side.getString("Outcome"))&&side.getInt("Finish")>0,"the diver's ending readies the partner's own, and they come up together");
                var account=LakeHouse.account(side);
                h.assertTrue(account.contains("pair_diver")&&account.contains("THE LIGHTS")&&!account.equals(LakeHouse.account(own(f,diver))),"the partner's account is their own, from Tess's side: "+account);
                readTick(f,partner,110);readTick(f,diver,110);
                h.assertTrue(own(f,partner).getInt("Finish")==0&&f.base().equals(LakeHouse.readerBase(partner)),"the partner comes to on the raft beside their diver");
                f.read(partner,LakeHouse.SOURCE);f.read(partner,LakeHouse.ACCOUNT);
                h.assertTrue(WitnessAccount.has(f.data(),partner.getUUID(),WitnessAccount.Story.LAKE_HOUSE),"reading it gives the partner the lake's Witness credit");
                h.assertTrue(LakeHousePartner.witness(own(f,partner),"").contains("waited on the raft"),"their Witness line is from Tess's side");
                // On the helmet visit Tess keeps the compressor going on the raft, and that is where the reader in her place begins.
                var helmet=own(f,diver);helmet.putInt("Visit",2);LiteraryVignettes.save(f.data(),diver.getUUID(),PLACE,helmet);
                LakeHouseStory.depart(partner);in(f,partner,f.base(),1.5,0,-4.5);LiteraryVignettes.onArrive(partner,PLACE);
                var raft=Vec3.atLowerCornerOf(f.base()).add(LakeHousePairs.tessSpot(2));var rel=partner.position().subtract(Vec3.atLowerCornerOf(f.base()));
                h.assertTrue(partner.position().distanceTo(raft)<1.5&&LakeHouse.onRaft(rel)&&own(f,partner).getInt(LakeHousePairs.STOOD_AT)==2,"on the helmet visit the partner begins on the raft by the compressor: "+rel);
                // A reconnect partway through a visit leaves them where they are.
                in(f,partner,f.base(),.5,0,-8.5);LiteraryVignettes.onArrive(partner,PLACE);
                h.assertTrue(partner.position().subtract(Vec3.atLowerCornerOf(f.base())).distanceTo(new Vec3(.5,0,-8.5))<.1,"a reconnect partway through a visit does not move them: "+partner.position().subtract(Vec3.atLowerCornerOf(f.base())));
            })
            .thenWaitUntil(()->h.assertTrue(SceneInstances.built(f.data(),PLACE,1),"a second lake stands ready"))
            // A reader arriving keeps the lake loaded; these fixture readers do not load chunks themselves.
            .thenExecute(()->f.lease.hold(f.out,grounds(SceneInstances.base(s,PLACE,1))))
            .thenWaitUntil(()->h.assertTrue(f.lease.ready(),"the second lake is resident"))
            .thenExecute(()->{
                var lake=SceneInstances.base(s,PLACE,1);
                var own3=SceneInstances.enter(third,PLACE);h.assertTrue(own3.door()!=null&&own3.copy()==1,"the third reader's door opens onto a lake of their own");
                in(f,third,lake,.5,0,-3.5);LiteraryVignettes.onArrive(third,PLACE);
                h.assertTrue(lake.equals(LakeHouse.readerBase(third))&&LakeHouse.inside(third)&&f.base().equals(LakeHouse.readerBase(partner)),"each reader's lake is where they are");
                h.assertTrue(f.out.getBlockState(lake.offset(LakeHouse.COMPRESSOR)).is(LakeHouseRegistry.COMPRESSOR.get()),"the second lake is built like the first, its raft and compressor included");
                var join=SceneInstances.enter(fourth,PLACE);
                h.assertTrue(join.door()!=null&&join.copy()==1&&third.getUUID().equals(SceneInstances.diverOf(f.data(),fourth.getUUID())),"the fourth reader joins the third, in Tess's place");
                var alone=SceneInstances.enter(fifth,PLACE);h.assertTrue(alone.door()==null&&alone.waiting(),"a fifth reader is on their own again, in a lake still being made ready");
                // The third reader leaves: their partner is not dealt the lake, and its door waits for them.
                away(f,third);LakeHousePairs.refresh(s);
                h.assertTrue(LakeHousePairs.waiting(f.data(),fourth.getUUID())&&!LabyrinthDealer.vignettesAvailable(f.data(),fourth.getUUID()).contains(PLACE),"a partner is not dealt the lake while their diver is elsewhere");
                var wait=SceneInstances.enter(fourth,PLACE);h.assertTrue(wait.door()==null&&third.getUUID().equals(wait.waitsFor()),"their door waits for their diver");
            })
            .thenIdle(15)
            .thenExecute(()->{
                var go=SceneInstances.enter(fourth,PLACE);
                h.assertTrue(SceneInstances.diverOf(f.data(),fourth.getUUID())==null&&SceneInstances.kept(f.data(),fourth.getUUID(),PLACE)!=1,"a second try lets them go down without their diver, as a reader of their own");
                h.assertTrue(go.door()==null||go.copy()!=1,"and never into their diver's lake");
            }).thenSucceed();
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
            // As after a blackout on the raft: swimming back in through the half door is not leaving.
            var back=own(f,p);back.putBoolean("InsideLit0505",false);LiteraryVignettes.save(f.data(),p.getUUID(),PLACE,back);
            at(f,p,.5,-24,-159.5);readTick(f,p,5);h.assertTrue(!own(f,p).getBoolean("Ready"),"coming back in through the half door is not leaving the lit house");
            at(f,p,.5,-24,-164.5);readTick(f,p,5);p.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST,ItemStack.EMPTY);p.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD,ItemStack.EMPTY);
            LakeHouseDive.setAir(p,20);LakeHouseDive.breathe(p,f.base());h.assertTrue(p.isEyeInFluid(net.minecraft.tags.FluidTags.WATER)&&p.getAirSupply()>20,"inside the lit house the reader breathes without gear");
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
        Runnable[] action=new Runnable[1];action[0]=()->{if(!lease.ready()){h.runAfterDelay(1,()->action[0].run());return;}try{
            onTheDock(f,p);readTick(f,p,5);var tess=LakeHouseTess.actor(p,own(f,p));h.assertTrue(tess!=null&&tess.role()==LiteraryActor.LAKE_TESS,"a real original Tess appears for a solo reader");
            var observer=f.player("lake_observer");observer.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.SPECTATOR);h.assertTrue(!tess.broadcastToPlayer(observer),"spectators never receive Tess and do not dismiss her");readTick(f,p,5);h.assertTrue(!own(f,p).getBoolean("SoloGone"),"a spectator is not a partner");
            var peer=f.player("lake_partner");p.lookAt(EntityAnchorArgument.Anchor.EYES,tess.getEyePosition());readTick(f,p,5);h.assertTrue(tess.isAlive()&&!tess.broadcastToPlayer(peer)&&own(f,p).getBoolean("SoloGone"),"a watched Tess stays physically present while starting her departure");
            p.setYRot(p.getYRot()+180);p.setXRot(0);readTick(f,p,5);h.assertTrue(tess.isRemoved(),"she can leave once her reader's actual camera turns away");
            peer.teleportTo(f.in,0,90,0,0,0);readTick(f,p,10);LiteraryVignettes.onArrive(p,PLACE);readTick(f,p,5);h.assertTrue(LakeHouseTess.actor(p,own(f,p))==null,"the partner leaving or reconnecting does not bring her back mid-visit");
            // 0.4.560: she never comes into being where her reader can see; back from the door, the reader turns from the dock.
            returnVisit(f,p);at(f,p,.5,0,-3.5,0);readTick(f,p,5);h.assertTrue(LakeHouseTess.actor(p,own(f,p))!=null,"a later real departure and solo arrival can bring her back, out of the reader's sight");h.succeed();
        }finally{lease.close();f.close();}};action[0].run();
    }
    private static void drawnTo(ServerPlayer p,String value,String strength){var d=LabyrinthData.get(p.server);var read=d.stateEntry(HouseRead.STATE,p.getUUID().toString());var t=new CompoundTag();t.putString("Value",value);t.putString("Strength",strength);read.put(HouseRead.Trait.DRAWN_TO.name(),t);d.setStateEntry(HouseRead.STATE,p.getUUID().toString(),read);}
    /** The reader arrives afresh on their first visit, after the House has read them. */
    private static void firstArrival(LakeHouseTests.Fixture f,ServerPlayer p){var o=own(f,p);o.remove(LakeHousePartner.KEY);LiteraryVignettes.save(f.data(),p.getUUID(),PLACE,o);LiteraryVignettes.onArrive(p,PLACE);}
    private static String book(LakeHouseTests.Fixture f,ServerPlayer p){var text=new StringBuilder();for(var page:WitnessAccount.book(f.data(),p.getUUID(),p.getGameProfile().getName(),false).get(net.minecraft.core.component.DataComponents.WRITTEN_BOOK_CONTENT).pages())text.append(page.raw().getString()).append('\n');return text.toString();}
    @GameTest(template="empty",batch="lake_house_partner152",timeoutTicks=800)
    public static void thePartnerFollowsASettledReadOfWhoTheReaderIsDrawnToAndIsKeptForTheStory(GameTestHelper h){
        var f=new LakeHouseTests.Fixture(h);var lease=new NativeTestChunks();lease.hold(f.out,new AABB(Vec3.atLowerCornerOf(f.base().offset(-8,-1,-22)),Vec3.atLowerCornerOf(f.base().offset(8,12,2))));
        Runnable[] action=new Runnable[1];action[0]=()->{if(!lease.ready()){h.runAfterDelay(1,()->action[0].run());return;}try{
            // Drawn to men, settled: a young man in Tess's place, on a first date.
            var men=f.player("lake_drawn_men");drawnTo(men,"MEN","SETTLED");firstArrival(f,men);
            onTheDock(f,men);readTick(f,men,5);var theo=LakeHouseTess.actor(men,own(f,men));
            h.assertTrue(theo!=null&&theo.role()==LiteraryActor.LAKE_THEO&&"theo".equals(own(f,men).getString(LakeHousePartner.KEY)),"a reader settled as drawn to men meets the young man in Tess's place");
            h.assertTrue(own(f,men).getBoolean(LakeHousePartner.MET)&&LakeHousePartner.firstGreeting(LakeHousePartner.Partner.THEO).contains("first date"),"they meet on the dock, and it is a first date");
            var observer=f.player("lake_drawn_observer");observer.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.SPECTATOR);h.assertTrue(!theo.broadcastToPlayer(observer),"he is the reader's alone");
            var o=own(f,men);o.putString("Outcome",LakeHouse.SAID_HELLO);h.assertTrue(LakeHouse.account(o).startsWith("Theo and I took the green canoe out on our first date.\n\n"),"the account opens with him: "+LakeHouse.account(o));
            WitnessAccount.resolve(men,WitnessAccount.Story.LAKE_HOUSE,LakeHouse.SAID_HELLO);h.assertTrue(book(f,men).contains("Theo and I went out on the lake on our first date"),"the Witness line names him");
            // The partner is kept: a read that changes later changes nothing in this story.
            drawnTo(men,"WOMEN","SETTLED");var kept=own(f,men);h.assertTrue(LakeHousePartner.choose(men,kept)==LakeHousePartner.Partner.THEO,"a later read keeps the partner they met");
            men.teleportTo(f.in,0,90,0,0,0);observer.teleportTo(f.in,0,92,0,0,0);
            // Drawn to no one, settled: Tess, as a friend rather than a date.
            var none=f.player("lake_drawn_none");drawnTo(none,"NO_ONE","SETTLED");firstArrival(f,none);
            onTheDock(f,none);readTick(f,none,5);var friend=LakeHouseTess.actor(none,own(f,none));
            h.assertTrue(friend!=null&&friend.role()==LiteraryActor.LAKE_TESS&&"friend".equals(own(f,none).getString(LakeHousePartner.KEY)),"a reader settled as drawn to no one meets Tess");
            h.assertTrue(!LakeHousePartner.firstGreeting(LakeHousePartner.Partner.FRIEND).contains("date"),"and it is not a date");
            o=own(f,none);o.putString("Outcome",LakeHouse.STAYED);h.assertTrue(LakeHouse.account(o).startsWith("Tess and I took the green canoe out one weekend, the way friends do.\n\n"),"her account says they went as friends");
            none.teleportTo(f.in,0,94,0,0,0);
            // A tentative read is not enough for a larger change; no read at all is Tess on a date.
            var tentative=f.player("lake_drawn_tentative");drawnTo(tentative,"MEN","TENTATIVE");firstArrival(f,tentative);
            h.assertTrue("tess".equals(own(f,tentative).getString(LakeHousePartner.KEY)),"a tentative read keeps Tess");
            tentative.teleportTo(f.in,0,96,0,0,0);
            var unread=f.player("lake_drawn_unread");h.assertTrue("tess".equals(own(f,unread).getString(LakeHousePartner.KEY)),"an unread reader meets Tess");
            onTheDock(f,unread);readTick(f,unread,5);o=own(f,unread);o.putString("Outcome",LakeHouse.LEFT_THE_LIGHTS_ON);h.assertTrue(LakeHouse.account(o).startsWith("Tess and I took the green canoe out on our first date."),"on a first date");
            unread.teleportTo(f.in,0,98,0,0,0);
            // A reader who met Tess before the partner was chosen keeps her, whatever the read says now.
            var older=f.player("lake_drawn_older");var before=own(f,older);before.remove(LakeHousePartner.KEY);before.putBoolean("TessGreeting",true);before.putInt("Visit",2);LiteraryVignettes.save(f.data(),older.getUUID(),PLACE,before);drawnTo(older,"MEN","SETTLED");
            h.assertTrue(LakeHousePartner.choose(older,own(f,older))==LakeHousePartner.Partner.TESS,"a reader partway through keeps Tess");
            var old=own(f,older);old.remove(LakeHousePartner.KEY);old.putString("Outcome",LakeHouse.SAID_HELLO);h.assertTrue(LakeHouse.account(old).startsWith("THE EMPTY LAKE"),"and a record from before 0.4.152 keeps its account word for word");
            h.succeed();
        }finally{lease.close();f.close();}};action[0].run();
    }
    @GameTest(template="empty",batch="lake_house_swim0490",timeoutTicks=1000)
    public static void theOriginalTessSwimsThroughTheDoorAndUpTheNativeStair(GameTestHelper h){
        var f=new LakeHouseTests.Fixture(h);var p=f.player("lake_swim");p.setItemSlot(EquipmentSlot.CHEST,LakeHouseGear.lend(p,LakeHouseRegistry.TANKS.get()));
        f.ready(()->{at(f,p,.5,-24,-164.5);readTick(f,p,5);var tess=LakeHouseTess.actor(p,own(f,p));h.assertTrue(tess!=null,"the solo reader has the original Tess");var id=tess.getUUID();var b=f.base();tess.appearance(LiteraryActor.LAKE_TESS,2);tess.moveTo(b.getX()+.5,b.getY()-24,b.getZ()-159.5);at(f,p,7.5,-18.8,-175.5);
            int[] steps={0};Runnable[] swim=new Runnable[1];swim[0]=()->{try{readTick(f,p,5);h.assertTrue(tess.getUUID().equals(id)&&f.out.getEntity(id)==tess&&f.out.noCollision(tess,tess.getBoundingBox()),"the same body moves without crossing a native wall or fitting");
                if(tess.getY()>b.getY()-20&&tess.distanceToSqr(p)<10){h.assertTrue(tess.phase()==2&&Float.isFinite(tess.getXRot()),"her actual swimming body reaches the upper bedroom through the stair opening");h.succeed();f.close();return;}
                h.assertTrue(++steps[0]<150,"Tess finds the real front door and central staircase; at="+tess.position()+", goal="+p.position());h.runAfterDelay(5,()->swim[0].run());
            }catch(Throwable error){f.close();throw error;}};swim[0].run();});
    }
    // ------------------------------------------------------------------------------------------------ 0.4.560: Tess, held to the cast checks
    /** Waits a tick at a time for something to be so, failing with what was expected after {@code limit} ticks; then goes on. */
    private static void await(GameTestHelper h,int limit,java.util.function.BooleanSupplier done,java.util.function.Supplier<String> expected,Runnable cleanup,Runnable then){
        int[] waited={0};Runnable[] poll=new Runnable[1];
        poll[0]=()->{
            boolean ready;try{ready=done.getAsBoolean();}catch(Throwable e){cleanup.run();throw e;}
            if(ready){try{then.run();}catch(Throwable e){cleanup.run();throw e;}return;}
            if(++waited[0]>limit){String why=expected.get();cleanup.run();h.fail(why);return;}
            h.runAfterDelay(1,()->poll[0].run());
        };poll[0].run();
    }
    /** Takes a canoe along a way a tick at a time, as a reader paddling and pushing would, with its reader kept in the front seat. */
    private static void row(GameTestHelper h,LakeHouseCanoe canoe,ServerPlayer reader,List<Vec3> way,double perTick,Runnable then){
        int[] leg={0};Runnable[] step=new Runnable[1];
        step[0]=()->{
            if(leg[0]>=way.size()){then.run();return;}
            var to=way.get(leg[0]);var d=to.subtract(canoe.position());
            if(d.length()<=perTick){canoe.setPos(to);leg[0]++;}else canoe.setPos(canoe.position().add(d.normalize().scale(perTick)));
            canoe.setYRot(180);canoe.setDeltaMovement(Vec3.ZERO);canoe.positionRider(reader);
            h.runAfterDelay(1,()->step[0].run());
        };step[0].run();
    }
    private static java.util.function.Predicate<LiteraryActor> tessSwims(){return a->LiteraryActor.lakePartner(a.role())&&(a.phase()==2||a.phase()==6);}
    private static LakeHouseCanoe canoeOf(LakeHouseTests.Fixture f,ServerPlayer p){return f.out.getEntitiesOfClass(LakeHouseCanoe.class,LakeHouse.box(f.base()),c->p.getUUID().equals(c.owner())).getFirst();}
    private static List<Vec3> toTheRaft(BlockPos b){
        return List.of(new Vec3(4.5,-.35,-24),new Vec3(1,-.35,-30),new Vec3(1,-.35,-96),new Vec3(1,-.35,-131),new Vec3(1,-.35,-160),new Vec3(1.6,-.35,-173.5))
            .stream().map(v->v.add(b.getX(),b.getY(),b.getZ())).toList();
    }
    /**
     * 0.4.560 (the owner: "also ensure the npc behaves correctly in that vignette, please - tess", and "standardize your tests with
     * Tess"): a first visit played through while the standard cast checks watch her every tick. She meets the reader on the dock
     * planks rather than standing on the water, walks to the canoe and climbs in from beside it, ducks under the culvert's roof
     * instead of putting her head through it, steps out onto the raft after the reader, and goes into the water at its edge to
     * swim down after them to the house.
     */
    /** The fixtures of the two cases below, closed after their batches however they end (a case that failed mid-way left its reader in the shared scene, and every later case counted two readers). */
    private static Runnable tessConduct,tessHelmet,tessCulvert;
    private static Runnable once(Runnable r){boolean[] ran={false};return ()->{if(!ran[0]){ran[0]=true;r.run();}};}
    @AfterBatch(batch="lake_house_tess_conduct") public static void tessConductDone(ServerLevel l){if(tessConduct!=null){tessConduct.run();tessConduct=null;}}
    @AfterBatch(batch="lake_house_tess_helmet") public static void tessHelmetDone(ServerLevel l){if(tessHelmet!=null){tessHelmet.run();tessHelmet=null;}}
    @AfterBatch(batch="lake_house_tess_culvert") public static void tessCulvertDone(ServerLevel l){if(tessCulvert!=null){tessCulvert.run();tessCulvert=null;}}
    @GameTest(template="empty",batch="lake_house_tess_conduct",timeoutTicks=3600)
    public static void tessKeepsTheCastChecksFromTheDockDownToTheHouse(GameTestHelper h){
        var f=new LakeHouseTests.Fixture(h);var p=f.player("lake_tess_conduct");p.setItemSlot(EquipmentSlot.CHEST,LakeHouseGear.lend(p,LakeHouseRegistry.TANKS.get()));
        tessConduct=once(f::close);
        f.ready(()->SceneReady.whenTicking(h,f.out,f.held(),600,()->{
            var b=f.base();var watch=new CastConduct(h,f.out,LakeHouse.box(b),()->List.of(p)).swimming(tessSwims());Runnable done=once(()->{watch.close();f.close();});tessConduct=done;
            // Out on the dock, looking at the lake: she is not made where the reader can see, so she comes out of the boathouse behind
            // them and up the planks.
            at(f,p,.5,0,-14.5);
            await(h,400,()->{var t=LakeHouseTess.actor(p,own(f,p));return t!=null&&own(f,p).getBoolean("TessGreeting")&&t.distanceTo(p)<3.5;},()->"Tess comes along the dock to the reader and greets them; she is at "+(LakeHouseTess.actor(p,own(f,p))==null?"nowhere":LakeHouseTess.actor(p,own(f,p)).position())+"; "+watch.faults(),done,()->{
                var tess=LakeHouseTess.actor(p,own(f,p));var canoe=canoeOf(f,p);
                h.assertTrue(tess.phase()==0&&!tess.isInWater()&&tess.getY()>b.getY()-.1,"she walked up the dock's planks, standing: "+tess.position()+" in phase "+tess.phase());
                h.assertTrue(p.startRiding(canoe),"the reader gets into their canoe");
                await(h,300,()->tess.getVehicle()==canoe,()->"Tess climbs into the canoe from beside it; she is at "+tess.position()+", the canoe at "+canoe.position()+"; "+watch.faults(),done,()->{
                    row(h,canoe,p,toTheRaft(b),.3,()->{try{
                        h.assertTrue(tess.getVehicle()==canoe,"she rides through the culvert to the third lake");
                        at(f,p,4.5,0,-173.5);
                        await(h,160,()->!tess.isPassenger()&&tess.phase()==0&&LakeHouse.onRaft(tess.position().subtract(Vec3.atLowerCornerOf(b))),()->"Tess steps out of the canoe onto the raft; she is at "+tess.position()+" in phase "+tess.phase(),done,()->{
                            at(f,p,.5,-24,-158.5);
                            await(h,900,()->tess.distanceTo(p)<6&&tess.phase()==2&&tess.isInWater(),()->"Tess goes into the water and swims down after the reader; she is at "+tess.position()+" in phase "+tess.phase()+", the reader at "+p.position()+"; "+watch.faults(),done,()->{
                                try{watch.assertClean("the lake house, Tess from the dock down to the house");h.assertTrue(watch.ticks()>600,"the checks watched the whole visit");}finally{done.run();}
                                h.succeed();
                            });
                        });
                    }catch(Throwable e){done.run();throw e;}});
                });
            });
        }));
    }
    /**
     * 0.4.560: on the helmet visit Tess keeps the compressor going on the raft. A body kept from the first visit is put by the
     * compressor the first moment the reader is not looking, she never climbs into the reader's canoe, and the cast checks hold while
     * the reader paddles out to her.
     */
    @GameTest(template="empty",batch="lake_house_tess_helmet",timeoutTicks=3000)
    public static void onTheHelmetVisitTessStaysWithTheCompressor(GameTestHelper h){
        var f=new LakeHouseTests.Fixture(h);var p=f.player("lake_tess_helmet");
        Runnable[] done={once(f::close)};tessHelmet=()->done[0].run();
        f.ready(()->SceneReady.whenTicking(h,f.out,f.held(),600,()->{
            var b=f.base();
            at(f,p,.5,0,-14.5);
            await(h,300,()->LakeHouseTess.actor(p,own(f,p))!=null,()->"Tess is there on the first visit",()->done[0].run(),()->{
                var tess=LakeHouseTess.actor(p,own(f,p));var o=own(f,p);o.putInt("Beat",1);LiteraryVignettes.save(f.data(),p.getUUID(),PLACE,o);
                returnVisit(f,p);h.assertTrue(own(f,p).getInt("Visit")==2&&LakeHouseTess.actor(p,own(f,p))==tess,"the second visit keeps her body from the first");
                var watch=new CastConduct(h,f.out,LakeHouse.box(b),()->List.of(p)).swimming(tessSwims());done[0]=once(()->{watch.close();f.close();});
                at(f,p,.5,0,-14.5);var canoe=canoeOf(f,p);h.assertTrue(p.startRiding(canoe),"the reader gets into their canoe");
                var post=Vec3.atBottomCenterOf(b.offset(LakeHouse.COMPRESSOR.offset(1,0,0)));
                row(h,canoe,p,toTheRaft(b),.3,()->{try{
                    h.assertTrue(!tess.isPassenger()&&tess.distanceToSqr(post)<9,"she never got into the canoe, and is by the compressor: "+tess.position());
                    at(f,p,4.5,0,-173.5);
                    await(h,200,()->tess.phase()==4&&own(f,p).getBoolean("TessGreeting"),()->"Tess minds the compressor and greets the reader at the raft; phase "+tess.phase(),()->done[0].run(),()->{
                        try{watch.assertClean("the lake house, Tess on the helmet visit");}finally{done[0].run();}
                        h.succeed();
                    });
                }catch(Throwable e){done[0].run();throw e;}});
            });
        }));
    }
    /**
     * 0.4.561 (run 1595's live expedition, which seats its reader in their canoe in the culvert's mouth before Tess has come): a reader
     * already sitting under the roof is reached by her on foot and swimming. She comes down the dock, steps down from its end, swims
     * the lake and the open trough, and climbs in from beside the canoe; the cast checks hold the whole way.
     */
    @GameTest(template="empty",batch="lake_house_tess_culvert",timeoutTicks=2400)
    public static void tessSwimsOutToACanoeAlreadyUnderTheRoof(GameTestHelper h){
        var f=new LakeHouseTests.Fixture(h);var p=f.player("lake_tess_culvert");
        tessCulvert=once(f::close);
        f.ready(()->SceneReady.whenTicking(h,f.out,f.held(),600,()->{
            var b=f.base();var watch=new CastConduct(h,f.out,LakeHouse.box(b),()->List.of(p)).swimming(tessSwims());Runnable done=once(()->{watch.close();f.close();});tessCulvert=done;
            var canoe=canoeOf(f,p);at(f,p,1,.2,-104.5);canoe.moveTo(b.getX()+1,b.getY()-.35,b.getZ()-104.5,180,0);canoe.setDeltaMovement(Vec3.ZERO);
            h.assertTrue(p.startRiding(canoe),"the reader sits in their canoe in the culvert's mouth");
            await(h,1600,()->{var t=LakeHouseTess.actor(p,own(f,p));return t!=null&&t.getVehicle()==canoe;},()->{var t=LakeHouseTess.actor(p,own(f,p));return "Tess comes out to the canoe under the roof and climbs in; she is "+(t==null?"not there":"at "+t.position().subtract(Vec3.atLowerCornerOf(b))+" in phase "+t.phase()+(t.isInWater()?", in the water":""))+", the canoe at "+canoe.position().subtract(Vec3.atLowerCornerOf(b))+"; "+watch.faults();},done,()->{
                try{watch.assertClean("the lake house, Tess swimming out to the culvert");}finally{done.run();}
                h.succeed();
            });
        }));
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
