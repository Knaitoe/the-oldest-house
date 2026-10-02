package io.github.knaitoe.theoldesthouse.gametest;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(TheOldestHouse.MOD_ID) @PrefixGameTestTemplate(false)
public final class CollapseTests {
    private static final class Fixture implements AutoCloseable {
        final GameTestHelper h;final ServerLevel l;final BlockPos origin,b;final HouseSavedData oldHouse;final LabyrinthData oldData;final MotherCollection oldMother;final long oldDayTime;
        final List<ServerPlayer> players=new ArrayList<>();final List<net.minecraft.world.level.ChunkPos> chunks=new ArrayList<>();
        Fixture(GameTestHelper h,int n){this.h=h;var s=h.getLevel().getServer();l=HouseTestLevel.get(s);origin=new BlockPos(n,80,n);b=FinaleArchitecture.base(origin);
            oldHouse=HouseSavedData.get(s);oldData=LabyrinthData.get(s);oldMother=MotherCollection.get(s);oldDayTime=s.overworld().getDayTime();var house=new HouseSavedData();house.markSpawned(origin);
            s.overworld().getDataStorage().set("the_oldest_house",house);s.overworld().getDataStorage().set("the_oldest_house_labyrinth",new LabyrinthData());s.overworld().getDataStorage().set("the_oldest_house_mother",new MotherCollection());
            for(int x=(b.getX()-32)>>4;x<=(b.getX()+32)>>4;x++)for(int z=(b.getZ()+28)>>4;z<=(b.getZ()+122)>>4;z++){var c=new net.minecraft.world.level.ChunkPos(x,z);chunks.add(c);l.getChunkSource().addRegionTicket(TicketType.PORTAL,c,3,b);}
            for(var p:FinaleArchitecture.plan(origin))if(p.pos().getZ()>=b.getZ()+30)l.setBlock(p.pos(),p.block(),2);
            var architecture=new CompoundTag();architecture.putBoolean("Requested",true);architecture.putBoolean("Ready",true);architecture.putLong("Origin",origin.asLong());architecture.putInt("CarveVersion",410);LabyrinthData.get(s).setState("finale_architecture_049",architecture);LabyrinthData.get(s).setBuilt(LabyrinthBuilder.VERSION,origin);
        }
        ServerPlayer player(){var p=h.makeMockServerPlayerInLevel();p.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);var at=FinaleArchitecture.cell(origin).north(8);p.teleportTo(l,at.getX()+.5,at.getY(),at.getZ()+.5,0,0);p.hasChangedDimension();players.add(p);return p;}
        @Override public void close(){for(var p:players)l.getServer().getPlayerList().remove(p);List<Entity> cleanup=new ArrayList<>();for(var e:l.getAllEntities())if(e!=null&&e.blockPosition().distSqr(b)<250*250&&(e instanceof MinotaurEntity||e instanceof NovelActor||e.getTags().contains("HouseCollapseDebris")||e.getTags().contains("HouseFinaleWords")))cleanup.add(e);cleanup.forEach(Entity::discard);for(var c:chunks)l.getChunkSource().removeRegionTicket(TicketType.PORTAL,c,3,b);
            var s=l.getServer();s.overworld().getDataStorage().set("the_oldest_house",oldHouse);s.overworld().getDataStorage().set("the_oldest_house_labyrinth",oldData);s.overworld().getDataStorage().set("the_oldest_house_mother",oldMother);s.overworld().setDayTime(oldDayTime);FinaleController.clearAll();}
    }
    private static Fixture scene,finite;
    @AfterBatch(batch="collapse_walk") public static void clean(ServerLevel l){if(scene!=null){scene.close();scene=null;}}
    @AfterBatch(batch="collapse_finite") public static void cleanFinite(ServerLevel l){if(finite!=null){finite.close();finite=null;}}
    @GameTest(template="empty",batch="collapse_walk",timeoutTicks=2400) public static void woundedCreatureSurvivesRealBreachFallObstaclesAndDoorStruggle(GameTestHelper h){
        scene=new Fixture(h,34100);var f=scene;var p=f.player();var peer=f.player();peer.gameMode.changeGameModeForPlayer(GameType.SPECTATOR);
        ItemStack sword=new ItemStack(Items.IRON_SWORD);p.setItemInHand(InteractionHand.MAIN_HAND,sword);WeaponHistory.record(p,sword,100);h.assertTrue(FinaleController.start(p),"the real cell commits the encounter");
        int[] stage={0},point={0},pullClock={0},clock={0};UUID[] prisoner={null};double[][] upper={{-14,51},{-14,65},{-20,65},{-20,76}};var route=FinaleArchitecture.escapeRoute(f.origin);
        h.onEachTick(()->{p.baseTick();FinaleController.tickPlayer(p,f.origin);var own=FinaleProgress.player(p.server,p.getUUID());if(++clock[0]%200==0)TheOldestHouse.LOGGER.info("Collapse native walk: stage={} point={} at={} phase={} collapse={} escape={}",stage[0],point[0],p.position(),FinaleProgress.phase(own),own.getInt("CollapseTicks"),own.getInt("EscapeTicks"));
            if(stage[0]==0){if(!own.hasUUID("Creature")||!(f.l.getEntity(own.getUUID("Creature")) instanceof MinotaurEntity m))return;prisoner[0]=m.getUUID();m.stagger();h.assertTrue(m.hurt(p.damageSources().playerAttack(p),8),"one authentic hit wounds the actually spawned Minotaur");stage[0]=1;return;}
            h.assertTrue(p.isAlive(),"the authored route remains survivable; stage="+stage[0]+" point="+point[0]+" at="+p.position());
            h.assertTrue(f.l.getEntity(prisoner[0]) instanceof MinotaurEntity m&&m.isAlive()&&m.motion()==MinotaurEntity.WOUNDED,"the same wounded prisoner persists after its retreat timer");
            if(stage[0]==1){if(own.getInt("CollapseTicks")<125)return;h.assertTrue(f.l.getBlockState(FinaleCollapse.breach(f.origin)).isAir(),"the visible broken wall is a real passage");h.assertTrue(f.l.getBlockState(f.b.offset(12,91,41)).isAir(),"the arena loses actual floor blocks");stage[0]=2;}
            if(stage[0]==2){double[] target=upper[point[0]];Vec3 to=new Vec3(f.b.getX()+target[0]+.5,p.getY(),f.b.getZ()+target[1]+.5);if(p.position().distanceToSqr(to)<.25){if(++point[0]==upper.length){point[0]=0;stage[0]=3;return;}}else walk(p,to,false);return;}
            if(stage[0]==3){move(p,Vec3.ZERO,false);if(own.getBoolean("Descended")){h.assertTrue(p.getHealth()>0&&p.getY()<8,"native movement falls through the continuous shaft into its water catch");stage[0]=4;point[0]=1;}return;}
            if(stage[0]==4){if(point[0]>=route.size()-1){p.setShiftKeyDown(false);p.setForcedPose(null);p.setPose(Pose.STANDING);stage[0]=5;return;}var target=Vec3.atBottomCenterOf(route.get(point[0]));
                if(p.position().multiply(1,0,1).distanceToSqr(target.multiply(1,0,1))<.35){point[0]++;return;}
                boolean low=point[0]>=68&&point[0]<=77;p.setShiftKeyDown(low);p.setForcedPose(low?Pose.CROUCHING:null);p.setPose(low?Pose.CROUCHING:Pose.STANDING);
                boolean jump=point[0]>=26&&point[0]<=31||point[0]>=106&&point[0]<=111;
                walk(p,target,jump);return;}
            if(stage[0]==5){p.setDeltaMovement(Vec3.ZERO);if(++pullClock[0]%22!=0)return;var exit=FinaleArchitecture.exit(f.origin);var e=new PlayerInteractEvent.RightClickBlock(p,InteractionHand.MAIN_HAND,exit,new BlockHitResult(exit.getCenter(),Direction.WEST,exit,false));NeoForge.EVENT_BUS.post(e);
                if(FinaleProgress.phase(p.server,p.getUUID())==FinaleProgress.Phase.ESCAPED){h.assertTrue(HouseSavedData.get(p.server).isCollapsed()&&p.getInventory().countItem(Items.IRON_SWORD)==1,"the fourth actual pull reaches the empty lot with the original weapon");h.assertTrue(!FinaleProgress.terminal(FinaleProgress.phase(p.server,peer.getUUID())),"an observer inherits no personal ending");h.succeed();}}
        });
    }
    // Server players receive movement from clients; drive the real collision mover instead of the client-only travel path.
    private static void walk(ServerPlayer p,Vec3 target,boolean jump){var d=target.subtract(p.position()).multiply(1,0,1);p.setYRot((float)(Math.atan2(d.z,d.x)*180/Math.PI)-90);move(p,d.lengthSqr()<.0256?d:d.normalize().scale(.16),jump);}
    private static void move(ServerPlayer p,Vec3 horizontal,boolean jump){double vertical=p.getDeltaMovement().y;
        if(p.isInWater())vertical=p.horizontalCollision?.3:Math.min(.12,Math.max(0,vertical)+.03);else if(p.onGround())vertical=jump?.42:0;else vertical=(vertical-.08)*.98;
        var velocity=new Vec3(horizontal.x,vertical,horizontal.z);p.setDeltaMovement(velocity);p.move(MoverType.SELF,velocity);if(p.verticalCollision)p.setDeltaMovement(horizontal.x,0,horizontal.z);
    }
    @GameTest(template="empty",batch="collapse_finite",timeoutTicks=80) public static void collapseUpgradeKeepsCacheIdentityAndReloadedWoundCannotDie(GameTestHelper h){
        finite=new Fixture(h,34400);var f=finite;FinaleCollapse.ensure(f.l,f.origin);var at=FinaleCollapse.cache(f.origin);var cache=(BarrelBlockEntity)f.l.getBlockEntity(at);cache.clearContent();
        var data=LabyrinthData.get(f.l.getServer());var saved=data.save(new CompoundTag(),f.l.registryAccess());f.l.getServer().overworld().getDataStorage().set("the_oldest_house_labyrinth",LabyrinthData.FACTORY.deserializer().apply(saved,f.l.registryAccess()));FinaleCollapse.ensure(f.l,f.origin);
        h.assertTrue(f.l.getBlockEntity(at)==cache&&cache.isEmpty(),"in-place dressing never rebuilds or replenishes the depleted supply container");
        var creature=FinaleRegistry.MINOTAUR.get().create(f.l);creature.owner(UUID.randomUUID());creature.wounded();CompoundTag tag=new CompoundTag();creature.saveWithoutId(tag);tag.putInt("FinaleRemaining",1);var restored=FinaleRegistry.MINOTAUR.get().create(f.l);restored.load(tag);restored.moveTo(Vec3.atBottomCenterOf(FinaleArchitecture.cell(f.origin).south(4)));f.l.addFreshEntity(restored);
        h.runAfterDelay(30,()->{h.assertTrue(restored.isAlive()&&restored.motion()==MinotaurEntity.WOUNDED&&!restored.hurt(restored.damageSources().genericKill(),Float.MAX_VALUE),"expired retreat and lethal damage do not turn a wound into a death");creature.discard();h.succeed();});}
    @GameTest(template="empty") public static void spectatorAndUnwalkedRouteCannotForceTheFinalLatch(GameTestHelper h){
        var p=h.makeMockServerPlayerInLevel();var own=new CompoundTag();own.putBoolean("Descended",true);own.putBoolean("Passed0",true);own.putBoolean("Passed1",true);
        h.assertTrue(!FinaleCollapse.pull(p,BlockPos.ZERO,own)&&own.getInt("DoorPulls")==0,"two checkpoints do not replace crossing the last damaged passage");p.server.getPlayerList().remove(p);h.succeed();}
}
