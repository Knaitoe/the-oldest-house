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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;

/** Native Adventure clicks, actual drinking and seating, shared finite props and saved bodies. */
@GameTestHolder(TheOldestHouse.MOD_ID+"_nightclub") @PrefixGameTestTemplate(false)
public final class NightclubTests {
    private static Fixture active;
    private static final class Fixture implements AutoCloseable {
        final GameTestHelper h;final NativeTestChunks chunks=new NativeTestChunks();final ServerLevel level;
        final HouseSavedData oldHouse;final LabyrinthData oldData;final BlockPos origin=new BlockPos(57000,80,57000),base;
        final List<ServerPlayer> players=new ArrayList<>();
        Fixture(GameTestHelper h){
            this.h=h;level=HouseTestLevel.get(h.getLevel().getServer(),HouseDimensions.INTERIOR);
            oldHouse=HouseSavedData.get(level.getServer());oldData=LabyrinthData.get(level.getServer());
            var house=new HouseSavedData();house.markSpawned(origin);var store=level.getServer().overworld().getDataStorage();
            store.set("the_oldest_house",house);store.set("the_oldest_house_labyrinth",new LabyrinthData());
            base=LabyrinthPlaces.base(origin,LabyrinthPlace.NIGHTCLUB);chunks.hold(level,Nightclub.bounds(base).inflate(2));
            data().setBuilt(LabyrinthBuilder.VERSION,origin);Nightclub.build(level,base);LabyrinthBuilder.registerDoors(data(),LabyrinthPlace.NIGHTCLUB,base);active=this;
        }
        LabyrinthData data(){return LabyrinthData.get(level.getServer());}
        ServerPlayer player(String name){var player=NativeTestPlayers.survival(h,name);players.add(player);player.teleportTo(level,base.getX()+.5,base.getY(),base.getZ()-3,180,0);player.hasChangedDimension();player.setNoGravity(true);player.setGameMode(GameType.ADVENTURE);return player;}
        void at(ServerPlayer player,int x,int z){player.stopRiding();player.moveTo(base.getX()+x+.5,base.getY(),base.getZ()+z+.5);player.setDeltaMovement(Vec3.ZERO);}
        InteractionResult click(ServerPlayer player,BlockPos relative,Direction face){var pos=base.offset(relative);return player.gameMode.useItemOn(player,level,player.getMainHandItem(),InteractionHand.MAIN_HAND,new BlockHitResult(pos.getCenter().relative(face,.5),face,pos,false));}
        void reload(){var access=level.registryAccess();level.getServer().overworld().getDataStorage().set("the_oldest_house_labyrinth",LabyrinthData.FACTORY.deserializer().apply(data().save(new CompoundTag(),access),access));}
        @Override public void close(){players.forEach(NativeTestPlayers::remove);for(var entity:level.getEntitiesOfClass(Entity.class,Nightclub.bounds(base).inflate(3)))entity.discard();chunks.close();var store=level.getServer().overworld().getDataStorage();store.set("the_oldest_house",oldHouse);store.set("the_oldest_house_labyrinth",oldData);}
    }
    private static void close(){if(active!=null){active.close();active=null;}}
    @AfterBatch(batch="nightclub_glass") public static void glassDone(ServerLevel ignored){close();}
    @AfterBatch(batch="nightclub_cast") public static void castDone(ServerLevel ignored){close();}

    @GameTest(template="empty",batch="nightclub_glass",timeoutTicks=220)
    public static void adventureGuestsCanTakeOneActualGlassDrinkAndReturnItEmptyAndSit(GameTestHelper h){
        var f=new Fixture(h);var player=f.player("club_glass");var peer=f.player("club_peer");var glass=new BlockPos(14,1,-16);var bar=new BlockPos(14,0,-16);
        f.at(player,12,-16);f.at(peer,12,-17);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native room entity sections are ready"))
            .thenExecute(()->{
                var colours=new HashSet<Integer>();for(int x:new int[]{-7,0,7})for(int z:new int[]{-18,-32}){var light=f.level.getBlockState(f.base.offset(x,4,z));h.assertTrue(light.is(NightclubRegistry.BLOCK.get())&&NightclubBlock.light(light)==2,"hanging coloured lamps keep native white illumination dim");colours.add(light.getValue(NightclubBlock.STAGE));}
                h.assertTrue(colours.size()==3,"the physical dance-floor lights use all three neon colours");
                h.assertTrue(Nightclub.strobe(2600,true)&&!Nightclub.strobe(2622,true)&&!Nightclub.strobe(2600,false),"rare flashes finish promptly and stay off when music stops");
                h.assertTrue(f.click(player,glass,Direction.WEST).consumesAction()&&f.level.getBlockState(f.base.offset(glass)).isAir(),"native click takes the one actual counter glass");
                h.assertTrue(player.getInventory().items.stream().filter(s->s.is(NightclubRegistry.GLASS.get())).mapToInt(ItemStack::getCount).sum()==1,"one glass enters native inventory");
                f.click(peer,glass,Direction.WEST);h.assertTrue(peer.getInventory().items.stream().noneMatch(s->s.is(NightclubRegistry.GLASS.get())),"another reader cannot take the removed glass");
                var carried=player.getInventory().items.stream().filter(s->s.is(NightclubRegistry.GLASS.get())).findFirst().orElseThrow();
                player.getInventory().selected=player.getInventory().items.indexOf(carried);
                h.assertTrue(player.gameMode.useItem(player,f.level,player.getMainHandItem(),InteractionHand.MAIN_HAND).consumesAction()&&player.isUsingItem(),"drinking starts through actual native item use");
                // Mock connections do not dispatch native player movement/use ticks.
                for(int tick=0;tick<36;tick++)player.doTick();
            }).thenExecute(()->{
                h.assertTrue(player.getMainHandItem().is(NightclubRegistry.GLASS.get())&&NightclubGlassItem.empty(player.getMainHandItem())&&!player.isUsingItem(),"native use completion leaves the same finite empty glass");
                h.assertTrue(f.click(player,bar,Direction.UP).consumesAction()&&player.getMainHandItem().isEmpty(),"an Adventure guest puts the carried glass back and spends the actual item");
                h.assertTrue(f.level.getBlockState(f.base.offset(glass)).getValue(NightclubBlock.STAGE)==0,"the real returned block stays empty");
                f.reload();h.assertTrue(f.level.getBlockState(f.base.offset(glass)).getValue(NightclubBlock.STAGE)==0,"saved state reload does not refill the original glass");
                f.at(player,-12,-21);h.assertTrue(f.click(player,new BlockPos(-14,0,-21),Direction.EAST).consumesAction()&&player.getVehicle() instanceof SeatEntity,"the club bench mounts a real native seat");
                player.stopRiding();h.assertTrue(!player.isPassenger(),"the native guest can leave the seat");
                f.at(player,10,-16);h.assertTrue(f.click(player,new BlockPos(11,0,-16),Direction.WEST).consumesAction()&&player.getVehicle() instanceof SeatEntity,"the bar stool is also a real usable seat");
            }).thenSucceed();
    }
    @GameTest(template="empty",batch="nightclub_cast",timeoutTicks=320)
    public static void originalBodiesKeepDancingWhenMutedAndSurviveReloadAndARevisitWithAnOpenExit(GameTestHelper h){
        var f=new Fixture(h);var player=f.player("club_dance");var peer=f.player("club_dance_peer");var identity=new HashSet<UUID>();var start=new HashMap<UUID,Vec3>();var previous=new HashMap<UUID,Vec3>();final int[] movingFrames={0};final boolean[] sample={false};
        h.onEachTick(()->{if(!sample[0])return;boolean moved=false;for(var a:f.level.getEntitiesOfClass(NightclubActor.class,Nightclub.bounds(f.base))){if(a.dj())continue;var before=previous.put(a.getUUID(),a.position());if(before==null)continue;double step=a.position().subtract(before).horizontalDistance();h.assertTrue(step<=.028,"every actual nightclub stride is a native-tick step, not a two-tick jump: "+step);moved|=step>.001;}if(moved)movingFrames[0]++;});
        h.startSequence().thenWaitUntil(()->{
            h.assertTrue(f.chunks.ready(),"native room sections are ready");Nightclub.ensureCast(f.level,f.base);
            h.assertTrue(f.level.getEntitiesOfClass(NightclubActor.class,Nightclub.bounds(f.base)).size()==19,"eighteen shared dancers and their original DJ arrive");
            h.assertTrue(f.level.getEntitiesOfClass(NightclubActor.class,Nightclub.bounds(f.base)).stream().map(NightclubActor::look).distinct().count()==9,"the native cast selects eight distinct dancer looks and a dedicated DJ");
        }).thenExecute(()->{
            f.level.getEntitiesOfClass(NightclubActor.class,Nightclub.bounds(f.base)).forEach(a->{identity.add(a.getUUID());start.put(a.getUUID(),a.position());previous.put(a.getUUID(),a.position());});sample[0]=true;
            f.at(player,0,-40);f.click(player,Nightclub.MIXER,Direction.SOUTH);f.click(player,Nightclub.MIXER,Direction.SOUTH);
            h.assertTrue(f.level.getBlockState(f.base.offset(Nightclub.MIXER)).getValue(NightclubBlock.STAGE)==0,"the native mixer physically switches the shared speakers off");
        }).thenIdle(90).thenExecute(()->{
            sample[0]=false;h.assertTrue(movingFrames[0]>=70,"the original dancers move between decisions on most actual native frames: "+movingFrames[0]);
            h.assertTrue(f.level.getEntitiesOfClass(NightclubActor.class,Nightclub.bounds(f.base)).stream().filter(a->!a.dj()).anyMatch(a->a.position().distanceTo(start.get(a.getUUID()))>.03),"actual native dancer bodies keep moving in silence");
            h.assertTrue(f.level.getEntitiesOfClass(NightclubActor.class,Nightclub.bounds(f.base)).stream().allMatch(a->a.despair()==1),"the actual mixer pause has transformed every original body into synced despair");
            f.reload();f.at(player,0,5);f.at(peer,0,5);
        }).thenIdle(35).thenExecute(()->{
            f.at(player,0,-3);Nightclub.ensureCast(f.level,f.base);
            h.assertTrue(identity.equals(f.level.getEntitiesOfClass(NightclubActor.class,Nightclub.bounds(f.base)).stream().map(Entity::getUUID).collect(java.util.stream.Collectors.toSet())),"reload and a later visit retain all nineteen original actor UUIDs");
            h.assertTrue(f.level.getBlockState(f.base.offset(Nightclub.MIXER)).getValue(NightclubBlock.STAGE)==0,"the mixer setting is physical and persists on revisit");
            var entry=new BlockPos(0,0,1);f.at(player,0,-1);f.click(player,entry,Direction.NORTH);
            var door=f.level.getBlockState(f.base.offset(entry));h.assertTrue(door.getBlock() instanceof DoorBlock&&door.getValue(DoorBlock.OPEN),"the original native return door opens after a prolonged visit with no timer or resolution lock");
            f.at(player,0,-40);f.click(player,Nightclub.MIXER,Direction.SOUTH);
        }).thenIdle(4).thenExecute(()->{
            h.assertTrue(f.level.getEntitiesOfClass(NightclubActor.class,Nightclub.bounds(f.base)).stream().allMatch(a->a.despair()==0),"restarting actual music immediately restores the same original dancers' passive or gleeful state");
        }).thenSucceed();
    }
}
