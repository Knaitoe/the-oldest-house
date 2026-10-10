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
    @GameTest(template="empty") public static void sceneClockBelongsToTheActualSiteAcrossNativeTimeUpdates(GameTestHelper h){
        var origin=new BlockPos(89100,70,89100);
        for(var site:LakeLandscape.SITES){
            var b=LabyrinthPlaces.base(origin,site);var p=b.offset(0,0,-8);
            for(long nativeTime:new long[]{0,6000,12000,18000,23999,24000,48001})
                h.assertTrue(SceneClock.time(SceneClock.at(origin,p,HouseDimensions.OUTSIDE),1200,nativeTime)==18000,"the actual lake stays at dusk even without a leased cue or after a native day update");
            h.assertTrue(SceneClock.at(origin,p,HouseDimensions.INTERIOR)==0&&SceneClock.at(origin,p,Level.OVERWORLD)==0,"matching coordinates in another dimension never inherit the lake clock");
        }
        var plain=LabyrinthPlaces.base(origin,LabyrinthPlace.PLAIN);h.assertTrue(SceneClock.time(SceneClock.at(origin,plain,HouseDimensions.OUTSIDE),0,21000)==6000,"a direct transition from the lake to the plain changes presentation without waiting for another packet");
        h.assertTrue(SceneClock.time(SceneClock.at(origin,origin.offset(0,1,0),HouseDimensions.INTERIOR),0,7311)==7311,"returning to the manor restores its native time immediately");h.succeed();
    }
    private static LakeWitchEntity witch;private static ServerPlayer target;private static BlockPos base;private static CompoundTag old;
    @AfterBatch(batch="witch_surface_0427") public static void clean(ServerLevel l){
        if(witch!=null)witch.discard();if(target!=null)NativeTestPlayers.remove(target);
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
        target=NativeTestPlayers.survival(h,"native_witch_target");
        target.moveTo(base.getX()+9.5,base.getY(),base.getZ()-25.5,-90,0);
        target.setYHeadRot(-90);target.yHeadRotO=-90;
        witch=DrownedTownRegistry.LAKE_WITCH.get().create(l);witch.shore(base,1);witch.moveTo(base.getX()-8.5,base.getY(),base.getZ()-25.5);l.addFreshEntity(witch);
        h.assertTrue(!target.isCreative()&&!target.isSpectator()&&l.players().contains(target)&&LakeWitchEntity.canAttack(witch,target),"the target has real native survival abilities and is actually eligible for the hunt");
        h.assertTrue(witch.getBbHeight()<1.0&&witch.getEyeHeight()<.8,"the native hunting collision body is low, as well as its rendered mesh");
        int[] wet={0},rush={0};double[] previous={witch.getX()};float health=target.getHealth();
        h.onEachTick(()->{
            IndianLakeRooms.keepLoaded(l,base,LabyrinthPlace.DROWNED_TOWN);target.setDeltaMovement(Vec3.ZERO);
            h.assertTrue(Math.abs(witch.getX()-previous[0])<1.3,"the doubled-speed ambush follows a physical route rather than teleporting");previous[0]=witch.getX();
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
    @GameTest(template="empty") public static void proofrockRebuildCarriesDesksAirToolsAndKeepsCanoeAndBodyIdentities(GameTestHelper h){
        var s=h.getLevel().getServer();var l=HouseTestLevel.get(s,HouseDimensions.OUTSIDE);var prior=LabyrinthData.get(s);var d=new LabyrinthData();s.overworld().getDataStorage().set("the_oldest_house_labyrinth",d);
        var b=new BlockPos(87500,60,87500);
        try{
            // The town as a saved world held it before 0.4.67: its raised school, a depleted desk, fuel in the furnace, an air door in the lake.
            DrownedTownArchitecture.build(l,b);LakeLandscape.liftSchool(l,b);
            var desk=(net.minecraft.world.Container)l.getBlockEntity(b.offset(-23,1,-33));var original=new ItemStack(Items.DIAMOND,2);original.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Kept"));
            desk.setItem(0,ItemStack.EMPTY);desk.setItem(7,original.copy());
            var oldFurnace=(net.minecraft.world.Container)l.getBlockEntity(b.offset(13,0,-8));oldFurnace.setItem(1,new ItemStack(Items.COAL,3));
            var airDoor=b.offset(5,-11,-30);var lower=Blocks.OAK_DOOR.defaultBlockState();
            l.setBlock(airDoor,lower,3);l.setBlock(airDoor.above(),lower.setValue(DoorBlock.HALF,net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER),3);
            var state=d.state(DrownedTown.ID);var placed=new CompoundTag();placed.putString(Long.toString(airDoor.asLong()),"door");placed.putString(Long.toString(airDoor.above().asLong()),"door");
            state.put("PlacedAirTools",placed);state.putBoolean("PaperStocked",true);state.putInt("Visit",1);d.setState(DrownedTown.ID,state);
            DrownedTown.stageShore(l,b,d);state=d.state(DrownedTown.ID);UUID canoe=state.getUUID("TownCanoeUUID"),body=state.getUUID("ShoreBodyUUID");
            l.getEntity(canoe).moveTo(b.getX()+13.5,b.getY()-.25,b.getZ()-34.5);
            var witch=DrownedTownRegistry.LAKE_WITCH.get().create(l);witch.shore(b,1);witch.moveTo(b.getX()+23.5,b.getY(),b.getZ()-6.5);l.addFreshEntity(witch);var hunter=witch.getUUID();
            TownCarry.capture(l,b);
            h.assertTrue(desk.getItem(7).isEmpty()&&oldFurnace.getItem(1).isEmpty()&&d.state(DrownedTown.ID).contains("Carry0467"),"custody empties the old containers before any block removal could drop them");
            DrownedTown.build(s,l,b);
            var next=(net.minecraft.world.Container)l.getBlockEntity(b.offset(DrownedTown.PAPERS[0]));
            h.assertTrue(next!=null&&next.getItem(0).isEmpty()&&ItemStack.isSameItemSameComponents(next.getItem(7),original)&&next.getItem(7).getCount()==2,"the essay desk's exact remaining contents move into Indian Lake High, and it is never restocked");
            var furnace=(net.minecraft.world.Container)l.getBlockEntity(b.offset(DrownedTown.FURNACE));var supplies=(net.minecraft.world.Container)l.getBlockEntity(b.offset(DrownedTown.SUPPLIES));
            h.assertTrue(furnace.getItem(1).is(Items.COAL)&&furnace.getItem(1).getCount()==3&&supplies.countItem(Items.OAK_DOOR)==1&&!d.state(DrownedTown.ID).contains("PlacedAirTools"),"the furnace keeps its fuel and the reader's air door comes back with the supplies");
            var movedCanoe=l.getEntity(canoe);var movedBody=l.getEntity(body);var movedHunter=l.getEntity(hunter);
            h.assertTrue(movedCanoe!=null&&movedCanoe.position().distanceToSqr(Vec3.atCenterOf(b.offset(ProofrockTown.CANOE)))<4&&movedBody!=null&&movedBody.position().distanceToSqr(Vec3.atBottomCenterOf(b.offset(ProofrockTown.SHORE_BODY)))<1,"the same canoe and shore body are set down where Proofrock keeps them");
            h.assertTrue(movedHunter instanceof LakeWitchEntity w&&LakeWitchEntity.walkable(l,b,w.blockPosition())&&!d.state(DrownedTown.ID).contains("Carry0467"),"the same hunter stands on her new ground, and custody is released");
            h.assertTrue(DrownedTown.witchSpawn(l,b).getZ()<b.getZ()-30,"the initial hunter belongs behind the town, well away from the entrance");
            DrownedTown.stageShore(l,b,d);h.assertTrue(canoe.equals(d.state(DrownedTown.ID).getUUID("TownCanoeUUID"))&&body.equals(d.state(DrownedTown.ID).getUUID("ShoreBodyUUID")),"visits preserve the same canoe and shore body without replacements");
            h.succeed();
        }finally{l.getEntitiesOfClass(Entity.class,IndianLakeRooms.bounds(b,LabyrinthPlace.DROWNED_TOWN).inflate(2),e->e.getTags().contains(DrownedTown.TOWN_CANOE)||e.getTags().contains(DrownedTown.SHORE_BODY)||e instanceof LakeWitchEntity||e instanceof LakeCongregantEntity||e instanceof net.minecraft.world.entity.item.ItemEntity).forEach(Entity::discard);s.overworld().getDataStorage().set("the_oldest_house_labyrinth",prior);}
    }
    @GameTest(template="empty") public static void unclaimedPreparationShieldIsRetiredWithoutTakingEarnedEquipment(GameTestHelper h){
        var l=h.getLevel();var origin=new BlockPos(88500,60,88500);var at=FinaleArchitecture.base(origin).offset(-10,FinaleArchitecture.ARENA,39);var d=LabyrinthData.get(l.getServer());var old=d.state("finale_architecture_049").copy();
        l.setBlock(at,Blocks.BARREL.defaultBlockState(),3);var cache=(BarrelBlockEntity)l.getBlockEntity(at);cache.setItem(0,new ItemStack(Items.SHIELD));var earned=new ItemStack(Items.SHIELD);earned.setDamageValue(13);cache.setItem(4,earned);
        try{FinaleArchitecture.retirePreparationShield(l,origin);h.assertTrue(cache.getItem(0).isEmpty()&&cache.getItem(4)==earned,"only the untouched authored shortcut is retired; other shield equipment stays native");FinaleArchitecture.retirePreparationShield(l,origin);h.assertTrue(cache.getItem(0).isEmpty(),"the preparation barrel never restocks the bypass");h.succeed();}
        finally{d.setState("finale_architecture_049",old);}
    }
}
