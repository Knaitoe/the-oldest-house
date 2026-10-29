package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID+"_multiplayer")
@PrefixGameTestTemplate(false)
public final class SceneSupportTests {
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private static final List<Fixture> ACTIVE=new ArrayList<>();
    private static final class Fixture implements AutoCloseable {
        final ServerLevel level;final BlockPos origin,base;final LabyrinthPlace place;
        final NativeTestChunks chunks=new NativeTestChunks();final Set<BlockPos> touched=new HashSet<>();
        final List<Entity> entities=new ArrayList<>();final List<ServerPlayer> players=new ArrayList<>();
        HouseSavedData oldHouse;LabyrinthData oldData;boolean started,finished;
        Fixture(GameTestHelper h,LabyrinthPlace p,int coordinate){
            place=p;level=HouseTestLevel.get(h.getLevel().getServer(),NovelRooms.dimension(p));
            origin=new BlockPos(coordinate,0,coordinate);base=LabyrinthPlaces.base(origin,p);
            chunks.hold(level,SceneSupportRepairs.area(base,p).inflate(2));
        }
        void start(){
            var s=level.getServer();oldHouse=HouseSavedData.get(s);oldData=LabyrinthData.get(s);
            var house=new HouseSavedData();house.markSpawned(origin);house.markInteriorInitialized();
            s.overworld().getDataStorage().set("the_oldest_house",house);
            var data=new LabyrinthData();data.setBuilt(LabyrinthBuilder.VERSION,origin);
            s.overworld().getDataStorage().set("the_oldest_house_labyrinth",data);started=true;
            var area=SceneSupportRepairs.area(base,place);
            for(var at:BlockPos.betweenClosed(BlockPos.containing(area.minX,area.minY,area.minZ),BlockPos.containing(area.maxX,area.maxY,area.maxZ)))touched.add(at.immutable());
        }
        LabyrinthData data(){return LabyrinthData.get(level.getServer());}
        void put(BlockPos at,net.minecraft.world.level.block.state.BlockState state){touched.add(at.immutable());level.setBlock(at,state,F);}
        ServerPlayer player(GameTestHelper h,String name){var p=NativeTestPlayers.survival(h,name);p.setNoGravity(true);players.add(p);return p;}
        @Override public void close(){
            entities.forEach(Entity::discard);players.forEach(NativeTestPlayers::remove);
            for(var at:touched)level.setBlock(at,Blocks.AIR.defaultBlockState(),F);chunks.close();
            if(started){var storage=level.getServer().overworld().getDataStorage();storage.set("the_oldest_house",oldHouse);storage.set("the_oldest_house_labyrinth",oldData);}ACTIVE.remove(this);
        }
    }
    private static void run(GameTestHelper h,LabyrinthPlace place,int coordinate,Consumer<Fixture> check){
        var f=new Fixture(h,place,coordinate);ACTIVE.add(f);
        h.onEachTick(()->{if(f.finished||!f.chunks.ready())return;f.finished=true;
            try{f.start();check.accept(f);}finally{f.close();}h.succeed();});
    }
    @AfterBatch(batch="cave_supports") public static void caveCleanup(ServerLevel l){for(var f:new ArrayList<>(ACTIVE))f.close();}
    @AfterBatch(batch="barn_connections") public static void barnCleanup(ServerLevel l){for(var f:new ArrayList<>(ACTIVE))f.close();}

    @GameTest(template="empty",batch="cave_supports",timeoutTicks=1200)
    public static void caveOutcropAndCeilingCrustRespectNativeResidentsOriginalsAndReload(GameTestHelper h){run(h,LabyrinthPlace.TED_CAVER,548000,f->{
        var p=f.player(h,"support_cave_owner");var peer=f.player(h,"support_cave_peer");
        for(int x=-9;x<=-4;x++)for(int z=-41;z<=-40;z++)f.put(f.base.offset(x,-4,z),Blocks.STONE.defaultBlockState());
        for(int y=-3;y<=-2;y++)for(int z=-41;z<=-40;z++)f.put(f.base.offset(-8,y,z),Blocks.STONE.defaultBlockState());
        var mark=f.base.offset(CaverCave.MARK);f.put(mark,Blocks.CHISELED_DEEPSLATE.defaultBlockState());
        f.put(mark.below(),Blocks.TORCH.defaultBlockState());
        var node=f.base.offset(-6,0,-43);f.put(node,Blocks.CALCITE.defaultBlockState());
        f.put(node.above(),Blocks.POINTED_DRIPSTONE.defaultBlockState());f.put(node.above(2),Blocks.STONE.defaultBlockState());
        var supplies=f.base.offset(CaverCave.CACHE);f.put(supplies,Blocks.BARREL.defaultBlockState());
        var cache=(BarrelBlockEntity)f.level.getBlockEntity(supplies);var original=new ItemStack(Items.PAPER,2);
        original.set(DataComponents.CUSTOM_NAME,Component.literal("Kept cave account"));cache.setItem(4,original.copy());
        var story=new CompoundTag();story.putInt("Work",CaverVignette.STROKES);story.putBoolean("StoneMoved",true);story.putBoolean("Supplied",true);
        var own=new CompoundTag();own.putBoolean("Worked",true);own.putBoolean("Mark",true);story.put(p.getUUID().toString(),own);
        f.data().setState(CaverVignette.ID,story);var before=f.data().state(CaverVignette.ID);
        p.teleportTo(f.level,mark.getX()+2.5,mark.getY()-1,mark.getZ()+1.5,0,0);p.connection.resetPosition();
        h.assertTrue(!SceneSupportRepairs.repairOnce(f.level,f.origin,f.place)&&f.level.getBlockState(mark.north()).isAir(),"an actual nearby observer postpones the saved repair");
        p.teleportTo(h.getLevel(),0.5,100,0.5,0,0);p.connection.resetPosition();
        var hole=mark.north().west();var cat=EntityType.CAT.create(f.level);h.assertTrue(cat!=null,"the native resident exists");
        cat.setNoGravity(true);cat.setNoAi(true);cat.setTame(true,false);cat.setOwnerUUID(p.getUUID());cat.setOrderedToSit(true);cat.moveTo(Vec3.atBottomCenterOf(hole));f.level.addFreshEntity(cat);f.entities.add(cat);var id=cat.getUUID();
        h.assertTrue(!SceneSupportRepairs.repairOnce(f.level,f.origin,f.place)&&f.level.getBlockState(hole).isAir()&&cat.isAlive()&&cat.getUUID().equals(id),"the actual sitting companion is never buried in a new support");
        cat.moveTo(Vec3.atBottomCenterOf(supplies.south(2)));
        h.assertTrue(SceneSupportRepairs.repairOnce(f.level,f.origin,f.place),"the empty support finishes once its resident clears it");
        h.assertTrue(f.level.getBlockState(mark).is(Blocks.CHISELED_DEEPSLATE)&&f.level.getBlockState(mark.north()).is(Blocks.STONE)&&f.level.getBlockState(hole).is(Blocks.STONE)&&f.level.getBlockState(mark.below()).is(Blocks.TORCH),"the same marked stone joins the native wall while the placed torch survives");
        var tip=f.level.getBlockState(node.below());
        h.assertTrue(f.level.getBlockState(node.above()).is(Blocks.CALCITE)&&f.level.getBlockState(node.above(2)).is(Blocks.STONE)&&tip.is(Blocks.POINTED_DRIPSTONE)&&tip.getValue(PointedDripstoneBlock.TIP_DIRECTION)==Direction.DOWN&&tip.canSurvive(f.level,node.below()),"the mineral crust meets the real ceiling and its native tip hangs downward");
        h.assertTrue(f.level.getBlockEntity(supplies)==cache&&cache.getItem(0).isEmpty()&&ItemStack.isSameItemSameComponents(cache.getItem(4),original)&&f.data().state(CaverVignette.ID).equals(before),"the original finite container and all shared/personal cave records remain exact");
        h.assertTrue(cat.isAlive()&&cat.getUUID().equals(id)&&cat.getOwnerUUID().equals(p.getUUID())&&cat.isOrderedToSit()&&WitnessAccount.count(f.data(),peer.getUUID())==0,"the native companion retains its identity, owner and order, and an observer gets no evidence");
        var loaded=LabyrinthData.FACTORY.deserializer().apply(f.data().save(new CompoundTag(),f.level.registryAccess()),f.level.registryAccess());f.level.getServer().overworld().getDataStorage().set("the_oldest_house_labyrinth",loaded);
        f.put(hole,Blocks.AIR.defaultBlockState());h.assertTrue(SceneSupportRepairs.repairOnce(f.level,f.origin,f.place)&&f.level.getBlockState(hole).isAir(),"the saved checkpoint survives reload and never restages later edits");
    });}

    @GameTest(template="empty",batch="barn_connections",timeoutTicks=1200)
    public static void barnRailsConnectWithNativeCollisionAndRetainTheAnimalsAndOpenGate(GameTestHelper h){run(h,LabyrinthPlace.BARN_WELL,548500,f->{
        // 0.4.164: the farm is built whole; its pens stand inside the long walls and no support repair touches it.
        NovelRooms.build(f.level.getServer(),f.level,f.base,f.place);
        var p=f.player(h,"support_barn_owner");var peer=f.player(h,"support_barn_peer");
        h.assertTrue(!SceneSupportRepairs.repairOnce(f.level,f.origin,f.place),"the support repair has nothing to do on the farm built whole");
        for(int z=-34;z<=-17;z++)for(int x:new int[]{5,15}){var foot=f.level.getBlockState(f.base.offset(x,0,z));
            h.assertTrue(!(foot.getBlock() instanceof FenceBlock)&&foot.isCollisionShapeFullBlock(f.level,f.base.offset(x,0,z)),"no rail stands in the barn's wall line; the wall comes down to its stone footing at x="+x+" z="+z);}
        for(int z=-33;z<=-22;z++)for(int x:new int[]{8,12}){if(z==-25||z==-29)continue;var state=f.level.getBlockState(f.base.offset(x,0,z));
            h.assertTrue(state.is(Blocks.SPRUCE_FENCE)&&state.getValue(FenceBlock.NORTH)&&state.getValue(FenceBlock.SOUTH),"the pen rail joins its neighbours at x="+x+" z="+z+": "+state);}
        var gate=f.base.offset(12,0,-29);h.assertTrue(f.level.getBlockState(gate).is(Blocks.SPRUCE_FENCE_GATE)&&!f.level.getBlockState(gate).getValue(FenceGateBlock.OPEN),"each pen has one real closed gate on the aisle");
        var sheep=EntityType.SHEEP.create(f.level);h.assertTrue(sheep!=null,"the original native sheep exists");sheep.setNoGravity(true);sheep.setNoAi(true);sheep.setPersistenceRequired();sheep.moveTo(f.base.getX()+13.5,f.base.getY(),f.base.getZ()-31.0,0,0);f.level.addFreshEntity(sheep);f.entities.add(sheep);var id=sheep.getUUID();
        sheep.move(MoverType.SELF,new Vec3(-4,0,0));h.assertTrue(sheep.getX()>f.base.getX()+12.6,"the rail holds the sheep in its pen");
        p.teleportTo(f.level,f.base.getX()+10.5,f.base.getY(),f.base.getZ()-31.0,0,0);p.connection.resetPosition();p.move(MoverType.SELF,new Vec3(2,0,0));
        h.assertTrue(p.getX()<f.base.getX()+12.3,"the connected rail actually stops a native Survival body between posts");
        f.level.setBlock(gate,f.level.getBlockState(gate).setValue(FenceGateBlock.OPEN,true),F);
        p.teleportTo(f.level,f.base.getX()+10.5,f.base.getY(),f.base.getZ()-28.5,0,0);p.connection.resetPosition();p.move(MoverType.SELF,new Vec3(2,0,0));
        h.assertTrue(p.getX()>f.base.getX()+12.4,"the opened gate admits the same native body");
        h.assertTrue(sheep.isAlive()&&sheep.getUUID().equals(id)&&FinaleProgress.phase(p.server,peer.getUUID())==FinaleProgress.Phase.UNSEEN&&WitnessAccount.count(f.data(),peer.getUUID())==0,"the animal is never replaced and the peer inherits no story or finale progress");
    });}
}
