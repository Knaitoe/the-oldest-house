package io.github.knaitoe.theoldesthouse.gametest;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import com.mojang.authlib.GameProfile;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LakeAmbushTests {
    private static LakeWitchEntity witch;private static ServerPlayer target;private static BlockPos base;private static CompoundTag old;
    @AfterBatch(batch="witch_surface_0427") public static void clean(ServerLevel l){
        if(witch!=null)witch.discard();if(target!=null)target.discard();
        if(base!=null)for(int x=(base.getX()-30)>>4;x<=(base.getX()+30)>>4;x++)for(int z=(base.getZ()-65)>>4;z<=(base.getZ()+18)>>4;z++)l.getChunkSource().removeRegionTicket(TicketType.PORTAL,new ChunkPos(x,z),3,base);
        if(old!=null)LabyrinthData.get(l.getServer()).setState(DrownedTown.ID,old);witch=null;target=null;base=null;old=null;
    }
    @GameTest(template="empty",batch="witch_surface_0427",timeoutTicks=360)
    public static void realCrawlingHunterCrossesWaterAndRushesFromBehind(GameTestHelper h){
        var l=h.getLevel();base=h.absolutePos(new BlockPos(0,3,0));old=LabyrinthData.get(l.getServer()).state(DrownedTown.ID).copy();
        for(int x=-12;x<=12;x++)for(int z=-42;z<=-13;z++){
            l.setBlock(base.offset(x,-2,z),Blocks.STONE.defaultBlockState(),3);
            l.setBlock(base.offset(x,-1,z),(x>=-5&&x<=5?Blocks.WATER:Blocks.COARSE_DIRT).defaultBlockState(),3);
            for(int y=0;y<=3;y++)l.setBlock(base.offset(x,y,z),Blocks.AIR.defaultBlockState(),3);
        }
        target=FakePlayerFactory.get(l,new GameProfile(UUID.randomUUID(),"lake_back_0427"));target.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        target.moveTo(base.getX()+9.5,base.getY(),base.getZ()-25.5,-90,0);l.addNewPlayer(target);
        witch=DrownedTownRegistry.LAKE_WITCH.get().create(l);witch.shore(base,1);witch.moveTo(base.getX()-8.5,base.getY(),base.getZ()-25.5);l.addFreshEntity(witch);
        h.assertTrue(witch.getBbHeight()<1.0&&witch.getEyeHeight()<.8,"the native hunting collision body is low, as well as its rendered mesh");
        int[] wet={0},rush={0};double[] previous={witch.getX()};float health=target.getHealth();
        h.onEachTick(()->{
            IndianLakeRooms.keepLoaded(l,base,LabyrinthPlace.DROWNED_TOWN);target.setDeltaMovement(Vec3.ZERO);
            h.assertTrue(Math.abs(witch.getX()-previous[0])<.7,"the ambush follows a physical route rather than teleporting");previous[0]=witch.getX();
            var node=BlockPos.containing(witch.getX(),base.getY(),witch.getZ());
            if(l.getFluidState(node.below()).is(net.minecraft.tags.FluidTags.WATER)){
                wet[0]++;h.assertTrue(Math.abs(witch.getY()-LakeWitchEntity.supportHeight(l,base,witch.getX(),witch.getZ(),witch.getBbWidth()))<.02,"the actual ticking actor rests on the fluid surface or the bank supporting its footprint: y="+witch.getY());
            }
            if(witch.huntPhase()==LakeWitchEntity.LUNGE)rush[0]++;
            h.assertTrue(witch.tickCount<230,"the flank and short strike have a strict native movement budget: offset="+witch.position().subtract(target.position())+", phase="+witch.huntPhase()+", targetHealth="+target.getHealth()+", wet="+wet[0]+", rush="+rush[0]+", watched="+LakeWitchEntity.inView(target,witch.position().add(0,.6,0))+", sight="+witch.getSensing().hasLineOfSight(target)+", "+witch.huntDiagnostic());
        });
        h.succeedWhen(()->{
            h.assertTrue(target.getHealth()<health,"the quick approach must deliver an actual native strike");
            h.assertTrue(wet[0]>=10&&rush[0]>=3,"the physical pursuit crosses a substantial water span before lunging");
            h.assertTrue(witch.huntPhase()==LakeWitchEntity.WITHDRAW,"one strike sends her back toward cover");
            target.moveTo(base.getX()+2.5,base.getY()-.7,base.getZ()-25.5);h.assertTrue(!LakeWitchEntity.canAttack(witch,target),"genuine submersion remains a refuge");
            l.setBlock(base.offset(9,-1,-25),Blocks.GRASS_BLOCK.defaultBlockState(),3);target.moveTo(base.getX()+9.5,base.getY(),base.getZ()-24.5);
            target.moveTo(Vec3.atBottomCenterOf(base.offset(9,0,-25)));h.assertTrue(!LakeWitchEntity.canAttack(witch,target),"living grass still interrupts the hunt");
        });
    }
    @GameTest(template="empty") public static void ambushSearchPrefersAReachableBackAndRespectsRealCover(GameTestHelper h){
        var l=h.getLevel();var b=h.absolutePos(new BlockPos(0,3,0));
        for(int x=-12;x<=12;x++)for(int z=-40;z<=-13;z++){l.setBlock(b.offset(x,-1,z),Blocks.COARSE_DIRT.defaultBlockState(),3);for(int y=0;y<=3;y++)l.setBlock(b.offset(x,y,z),Blocks.AIR.defaultBlockState(),3);}
        var p=FakePlayerFactory.get(l,new GameProfile(UUID.randomUUID(),"flank_search"));p.moveTo(b.getX()+.5,b.getY(),b.getZ()-25.5,0,0);
        Vec3 front=Vec3.atBottomCenterOf(b.offset(0,0,-18)).add(0,.6,0);h.assertTrue(LakeWitchEntity.inView(p,front),"the native view test sees a clear forward approach");
        for(int x=-4;x<=4;x++)for(int y=0;y<=3;y++)l.setBlock(b.offset(x,y,-21),Blocks.STONE.defaultBlockState(),3);
        h.assertTrue(!LakeWitchEntity.inView(p,front),"physical masonry hides an approach");
        var goal=LakeWitchEntity.ambushGoal(l,b,b.offset(-9,0,-25),p,false);
        h.assertTrue(goal!=null&&LakeWitchEntity.behindScore(p,Vec3.atBottomCenterOf(goal))>.35,"a reachable rear position wins over waiting directly in front");p.discard();h.succeed();
    }
    @GameTest(template="empty") public static void townUpgradeKeepsDesksAndFiniteCanoeAndBodyIdentities(GameTestHelper h){
        var s=h.getLevel().getServer();var l=HouseTestLevel.get(s,HouseDimensions.OUTSIDE);var prior=LabyrinthData.get(s);var d=new LabyrinthData();s.overworld().getDataStorage().set("the_oldest_house_labyrinth",d);
        var b=new BlockPos(87500,60,87500);
        try{
            // Construct the prior lake shell and skip its new checkpoint while preparing an old-save fixture.
            DrownedTownArchitecture.build(l,b);LakeLandscape.liftSchool(l,b);
            var flags=d.state(LakeSettlement.STATE);flags.putBoolean(b.asLong()+":"+LabyrinthPlace.DROWNED_TOWN.id(),true);d.setState(LakeSettlement.STATE,flags);LakeLandscape.dress(l,b,LabyrinthPlace.DROWNED_TOWN);
            var desk=(BarrelBlockEntity)l.getBlockEntity(b.offset(DrownedTown.PAPERS[0]));var original=new ItemStack(Items.DIAMOND,2);desk.setItem(0,ItemStack.EMPTY);desk.setItem(7,original);
            LakeSettlement.forget(l,b,LabyrinthPlace.DROWNED_TOWN);LakeSettlement.decorateOnce(l,b,LabyrinthPlace.DROWNED_TOWN);
            h.assertTrue(l.getBlockEntity(desk.getBlockPos())==desk&&desk.getItem(0).isEmpty()&&desk.getItem(7).getCount()==2,"the town refit retains the exact depleted native school desk");
            h.assertTrue(l.getBlockState(b.offset(-27,7,-30)).is(Blocks.DEEPSLATE_TILE_STAIRS)&&l.getBlockState(b.offset(-12,5,-44)).is(Blocks.DARK_PRISMARINE_STAIRS)
                    &&l.getBlockState(b.offset(-27,5,-17)).is(Blocks.OXIDIZED_CUT_COPPER_STAIRS),"school, market and boat shed have distinct real roof silhouettes and materials");
            h.assertTrue(LakeSettlement.shoreline(-17)!=LakeSettlement.shoreline(-28),"the actual shore bends across the map");
            h.assertTrue(DrownedTown.witchSpawn(l,b).getZ()<b.getZ()-30,"the initial hunter belongs behind the town, well away from the entrance");
            DrownedTown.stageShore(l,b,d);var state=d.state(DrownedTown.ID);UUID canoe=state.getUUID("TownCanoeUUID"),body=state.getUUID("ShoreBodyUUID");DrownedTown.stageShore(l,b,d);
            h.assertTrue(canoe.equals(d.state(DrownedTown.ID).getUUID("TownCanoeUUID"))&&body.equals(d.state(DrownedTown.ID).getUUID("ShoreBodyUUID")),"visits preserve the same canoe and shore body without replacements");
            var removed=l.getEntity(canoe);if(removed!=null)removed.discard();DrownedTown.stageShore(l,b,d);h.assertTrue(canoe.equals(d.state(DrownedTown.ID).getUUID("TownCanoeUUID")),"a recovered or destroyed canoe is never replenished");h.succeed();
        }finally{l.getEntitiesOfClass(Entity.class,IndianLakeRooms.bounds(b,LabyrinthPlace.DROWNED_TOWN),e->e.getTags().contains(DrownedTown.TOWN_CANOE)||e.getTags().contains(DrownedTown.SHORE_BODY)).forEach(Entity::discard);s.overworld().getDataStorage().set("the_oldest_house_labyrinth",prior);}
    }
    @GameTest(template="empty") public static void unclaimedPreparationShieldIsRetiredWithoutTakingEarnedEquipment(GameTestHelper h){
        var l=h.getLevel();var origin=new BlockPos(88500,60,88500);var at=FinaleArchitecture.base(origin).offset(-10,FinaleArchitecture.ARENA,39);var d=LabyrinthData.get(l.getServer());var old=d.state("finale_architecture_049").copy();
        l.setBlock(at,Blocks.BARREL.defaultBlockState(),3);var cache=(BarrelBlockEntity)l.getBlockEntity(at);cache.setItem(0,new ItemStack(Items.SHIELD));var earned=new ItemStack(Items.SHIELD);earned.setDamageValue(13);cache.setItem(4,earned);
        try{FinaleArchitecture.retirePreparationShield(l,origin);h.assertTrue(cache.getItem(0).isEmpty()&&cache.getItem(4)==earned,"only the untouched authored shortcut is retired; other shield equipment stays native");FinaleArchitecture.retirePreparationShield(l,origin);h.assertTrue(cache.getItem(0).isEmpty(),"the preparation barrel never restocks the bypass");h.succeed();}
        finally{d.setState("finale_architecture_049",old);}
    }
}
