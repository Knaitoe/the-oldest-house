package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import io.github.knaitoe.theoldesthouse.opening.CompanionOrders;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID+"_exploration")
@PrefixGameTestTemplate(false)
public final class RhythmTests {
    private static void depth(LabyrinthData d,UUID p,int n){for(int i=0;i<n;i++)d.pushReturn(p,new LabyrinthData.Waypoint(HouseDimensions.INTERIOR,new Vec3(0,80,i*4),0,true));}
    private static Map<String,String> map(LabyrinthData d,UUID p,LabyrinthPlace room){var out=new LinkedHashMap<String,String>();for(var spec:room.doors())if(LabyrinthData.DEALT.equals(spec.destination())){var door=d.door(room.doorId(spec));var deal=d.deal(p,door);if(deal!=null)out.put(door.id,deal.place());}return out;}
    private static LabyrinthData routes(){var d=new LabyrinthData();for(var p:LabyrinthPlace.values())d.setReady(p.id(),true);LabyrinthBuilder.registerDoors(d,LabyrinthPlace.CROSS_HALL,new BlockPos(0,80,0));return d;}
    @GameTest(template="empty")
    public static void personalResolutionDealsCalmChoicesOnceWithoutSharingCredit(GameTestHelper h){
        for(int salt=0;salt<40;salt++){
            var d=routes();var id=UUID.randomUUID();var peer=UUID.randomUUID();depth(d,id,18);
            h.assertTrue(WitnessAccount.resolve(d,id,WitnessAccount.Story.CLAP,"heard"),"an actual new personal resolution is recorded");
            h.assertTrue(ExpeditionRhythm.pending(d,id)&&!ExpeditionRhythm.pending(d,peer)&&WitnessAccount.count(d,peer)==0,"credit and the calm deal belong to the resolver only");
            LabyrinthDealer.arriveAt(d,id,LabyrinthPlace.CROSS_HALL,salt);
            var choices=map(d,id,LabyrinthPlace.CROSS_HALL);h.assertTrue(!choices.isEmpty()&&!ExpeditionRhythm.pending(d,id),"one successful fresh deal consumes the breather");
            for(var name:choices.values()){var p=LabyrinthPlace.byId(name);h.assertTrue((LabyrinthPacing.ordinary(p)||LabyrinthPacing.quiet(p))&&!LabyrinthPacing.physicalTrial(p)&&!LabyrinthPacing.anomaly(p),"every new choice is calm: "+name);}
            h.assertTrue(choices.values().stream().map(LabyrinthPlace::byId).anyMatch(LabyrinthPacing::quiet),"one choice is a real quiet room");
            h.assertTrue(!WitnessAccount.resolve(d,id,WitnessAccount.Story.CLAP,"again")&&!ExpeditionRhythm.pending(d,id),"replaying credit cannot bank another breather");
        }h.succeed();
    }
    @GameTest(template="empty")
    public static void pendingBreatherSurvivesReloadAndNeverRedrawsKnownRoutes(GameTestHelper h){
        var d=routes();var id=UUID.randomUUID();depth(d,id,8);LabyrinthDealer.arriveAt(d,id,LabyrinthPlace.CROSS_HALL,91);
        var before=map(d,id,LabyrinthPlace.CROSS_HALL);ExpeditionRhythm.request(d,id);
        var loaded=LabyrinthData.load(d.save(new CompoundTag(),h.getLevel().registryAccess()),h.getLevel().registryAccess());
        LabyrinthDealer.arriveAt(loaded,id,LabyrinthPlace.CROSS_HALL,91);
        h.assertTrue(before.equals(map(loaded,id,LabyrinthPlace.CROSS_HALL))&&ExpeditionRhythm.pending(loaded,id),"a remembered route stays exact and keeps the pending breather");
        loaded.pushReturn(id,new LabyrinthData.Waypoint(HouseDimensions.INTERIOR,new Vec3(70,80,90),0,true));
        LabyrinthDealer.arriveAt(loaded,id,LabyrinthPlace.CROSS_HALL,91);
        h.assertTrue(!ExpeditionRhythm.pending(loaded,id),"a genuinely new route can spend it after reconnect");h.succeed();
    }
    @GameTest(template="empty")
    public static void sharedDiscoveriesAreNeverOverwrittenByALaterBreather(GameTestHelper h){
        var d=routes();var first=UUID.randomUUID();var later=UUID.randomUUID();depth(d,first,8);depth(d,later,8);
        ExpeditionRhythm.request(d,first);LabyrinthDealer.arriveAt(d,first,LabyrinthPlace.CROSS_HALL,61);
        String key=Long.toUnsignedString(d.nodeKey(first,LabyrinthPlace.CROSS_HALL)^61L);var shared=d.stateEntry("shared_halls_0448",key);
        ExpeditionRhythm.request(d,later);LabyrinthDealer.arriveAt(d,later,LabyrinthPlace.CROSS_HALL,61);
        h.assertTrue(shared.equals(d.stateEntry("shared_halls_0448",key))&&ExpeditionRhythm.pending(d,later),"the earlier shared discoveries are preserved and the later player's breather waits");h.succeed();
    }
    @GameTest(template="empty")
    public static void deliberateScentAndActivePetRescueKeepPriority(GameTestHelper h){
        for(boolean rescue:new boolean[]{false,true}){
            var d=routes();var id=UUID.randomUUID();depth(d,id,8);ExpeditionRhythm.request(d,id);
            if(rescue){var state=d.state(MotherOfStrays.ID);var owners=new CompoundTag();owners.putBoolean(id.toString(),true);state.put("LivingPetOwners",owners);d.setState(MotherOfStrays.ID,state);}else d.setHillaryScent(id,true);
            LabyrinthDealer.arriveAt(d,id,LabyrinthPlace.CROSS_HALL,43);
            h.assertTrue(ExpeditionRhythm.pending(d,id),"a search never spends or suppresses the earned breather");
        }h.succeed();
    }
    @GameTest(template="empty")
    public static void onlyACompletedHazardFarDoorArmsABreather(GameTestHelper h){
        var p=NativeTestPlayers.survival(h,"hazard_walker");var d=LabyrinthData.get(p.server);var fixture=new LabyrinthData();
        LabyrinthBuilder.registerDoors(fixture,LabyrinthPlace.FLOODED_PASSAGE,new BlockPos(0,80,0));
        try{
            var entry=fixture.door(LabyrinthPlace.FLOODED_PASSAGE.entryDoorId());ExpeditionRhythm.crossedHazard(p,entry);
            h.assertTrue(!ExpeditionRhythm.pending(d,p.getUUID()),"a retreat does not claim completion");
            var far=fixture.door(LabyrinthPlace.FLOODED_PASSAGE.doorId(LabyrinthPlace.FLOODED_PASSAGE.doors().get(1)));
            p.setGameMode(GameType.SPECTATOR);ExpeditionRhythm.crossedHazard(p,far);h.assertTrue(!ExpeditionRhythm.pending(d,p.getUUID()),"an observer never banks a hazard beat");
            p.setGameMode(GameType.SURVIVAL);ExpeditionRhythm.crossedHazard(p,far);
            h.assertTrue(ExpeditionRhythm.pending(d,p.getUUID())&&WitnessAccount.count(d,p.getUUID())==0,"the completed crossing grants a calm deal, never Witness credit");h.succeed();
        }finally{NativeTestPlayers.remove(p);}
    }
    @GameTest(template="empty")
    public static void retreatGuidanceIsPersonalOnceAndNeverPromisesALockedExit(GameTestHelper h){
        var p=NativeTestPlayers.survival(h,"retreat_reader");var peer=NativeTestPlayers.survival(h,"retreat_peer");var fixture=new LabyrinthData();
        var base=h.absolutePos(BlockPos.ZERO).offset(100,10,100);
        LabyrinthBuilder.registerDoors(fixture,LabyrinthPlace.FLOORBOARDS,base);LabyrinthBuilder.registerDoors(fixture,LabyrinthPlace.BARN_WELL,base);
        var entry=fixture.door(LabyrinthPlace.FLOORBOARDS.entryDoorId());var well=fixture.door(LabyrinthPlace.BARN_WELL.entryDoorId());
        try{
            var outside=p.server.getLevel(well.dimension);p.teleportTo(outside,well.lower.getX()+.5,well.lower.getY(),well.lower.getZ()+.5,0,0);
            h.assertTrue(!ExpeditionRhythm.offerRetreat(p,LabyrinthPlace.BARN_WELL,well),"the well's authored locked exit cannot consume the first explanation");
            var interior=p.server.getLevel(entry.dimension);p.teleportTo(interior,entry.lower.getX()+.5,entry.lower.getY(),entry.lower.getZ()+.5,0,0);
            peer.teleportTo(interior,entry.lower.getX()+.5,entry.lower.getY(),entry.lower.getZ()+.5,0,0);
            h.assertTrue(ExpeditionRhythm.offerRetreat(p,LabyrinthPlace.FLOORBOARDS,entry)&&!ExpeditionRhythm.offerRetreat(p,LabyrinthPlace.FLOORBOARDS,entry)&&ExpeditionRhythm.offerRetreat(peer,LabyrinthPlace.FLOORBOARDS,entry),"the explanation is once per actual explorer");
            var d=LabyrinthData.get(p.server);var loaded=LabyrinthData.load(d.save(new CompoundTag(),p.registryAccess()),p.registryAccess());
            h.assertTrue(loaded.stateEntry(ExpeditionRhythm.STATE,p.getUUID().toString()).getBoolean("RetreatExplained")&&WitnessAccount.count(d,p.getUUID())==0,"the hint survives restart without awarding progress");h.succeed();
        }finally{NativeTestPlayers.remove(p);NativeTestPlayers.remove(peer);}
    }
    private static void buildHall(ServerLevel l,BlockPos b,LabyrinthPlace p){if(HallVariations.domestic(p))HallVariations.buildDomestic(l,b,p);else HallVariations.buildStone(l,b,p);}
    @GameTest(template="empty",batch="exploration_hall_changes",timeoutTicks=1200)
    public static void unseenChangesWaitForSpectatorCamerasAndLivingStayPets(GameTestHelper h){
        var l=h.getLevel();var b=h.absolutePos(BlockPos.ZERO).offset(6000,10,700);var place=LabyrinthPlace.ALCOVE_HALL;var lease=new NativeTestChunks();lease.hold(l,HallChanges.area(b,place));buildHall(l,b,place);
        var own=new CompoundTag();var p=NativeTestPlayers.survival(h,"hall_camera");p.setGameMode(GameType.SPECTATOR);p.teleportTo(l,b.getX()+25,b.getY()+1,b.getZ()-16,0,0);
        var cat=EntityType.CAT.create(l);cat.moveTo(b.getX(),b.getY(),b.getZ()-15);cat.setTame(true,false);cat.setOwnerUUID(p.getUUID());cat.setOrderedToSit(true);cat.setHealth(5);l.addFreshEntity(cat);var id=cat.getUUID();boolean[] done={false};
        h.onEachTick(()->{if(done[0]||!lease.ready())return;done[0]=true;try{
            h.assertTrue(!HallChanges.advance(l,b,place,own,1)&&!own.getBoolean("Staged"),"a real spectator camera prevents staging");p.teleportTo(l,b.getX()+150,b.getY(),b.getZ(),0,0);
            h.assertTrue(!HallChanges.advance(l,b,place,own,1)&&cat.getUUID().equals(id)&&cat.getHealth()==5&&cat.isOrderedToSit(),"a living Stay pet prevents the change without being moved");
            cat.moveTo(b.getX()+150,b.getY(),b.getZ());h.assertTrue(HallChanges.advance(l,b,place,own,1),"a native-loaded unseen room stages once");
            p.teleportTo(l,b.getX()+25,b.getY()+1,b.getZ()-16,0,0);h.assertTrue(!HallChanges.advance(l,b,place,own,3),"a camera prevents the later turn too");p.teleportTo(l,b.getX()+150,b.getY(),b.getZ(),0,0);
            h.assertTrue(HallChanges.advance(l,b,place,own,3)&&l.getBlockState(HallChanges.position(b,place)).getValue(HallChangeBlock.KIND)==HallChangeBlock.Kind.PICTURE_TURNED,"after two further real visits the unseen picture turns");h.succeed();
        }finally{cat.discard();NativeTestPlayers.remove(p);lease.close();}});
    }
    @GameTest(template="empty",batch="exploration_hall_changes",timeoutTicks=1200)
    public static void hallChangesPreservePlayerEditsAndRemovedPropsAcrossReload(GameTestHelper h){
        var l=h.getLevel();var b=h.absolutePos(BlockPos.ZERO).offset(6400,10,700);var place=LabyrinthPlace.STONE_LANDING;var lease=new NativeTestChunks();lease.hold(l,HallChanges.area(b,place));buildHall(l,b,place);boolean[] done={false};
        h.onEachTick(()->{if(done[0]||!lease.ready())return;done[0]=true;try{
            var own=new CompoundTag();var d=new LabyrinthData();h.assertTrue(HallChanges.advance(l,b,place,own,1),"the initial rug is staged");d.setStateEntry(HallChanges.STATE,"fixture",own);
            l.setBlock(HallChanges.position(b,place),Blocks.AIR.defaultBlockState(),2);
            var saved=LabyrinthData.load(d.save(new CompoundTag(),l.registryAccess()),l.registryAccess());own=saved.stateEntry(HallChanges.STATE,"fixture");
            h.assertTrue(HallChanges.advance(l,b,place,own,3)&&l.getBlockState(HallChanges.position(b,place)).isAir(),"a removed original stays removed after restart");
            l.setBlock(HallChanges.position(b,place),Blocks.DIAMOND_BLOCK.defaultBlockState(),2);h.assertTrue(!HallChanges.advance(l,b,place,own,9)&&l.getBlockState(HallChanges.position(b,place)).is(Blocks.DIAMOND_BLOCK),"later visits never restage or overwrite an edit");h.succeed();
        }finally{lease.close();}});
    }
    @GameTest(template="empty",batch="exploration_hall_changes",timeoutTicks=1200)
    public static void allThreeChangesKeepSupportRoutesAndFiniteContainers(GameTestHelper h){
        var l=h.getLevel();var start=h.absolutePos(BlockPos.ZERO).offset(6800,10,700);var lease=new NativeTestChunks();
        for(int i=0;i<HallChanges.PLACES.size();i++){var p=HallChanges.PLACES.get(i);var b=start.offset(i*200,0,0);lease.hold(l,HallChanges.area(b,p));buildHall(l,b,p);}
        boolean[] done={false};h.onEachTick(()->{if(done[0]||!lease.ready())return;done[0]=true;try{
            for(int i=0;i<HallChanges.PLACES.size();i++){
                var p=HallChanges.PLACES.get(i);var b=start.offset(i*200,0,0);var at=HallChanges.position(b,p);var own=new CompoundTag();
                var cache=b.offset(0,0,-4);l.setBlock(cache,Blocks.BARREL.defaultBlockState(),2);var box=(net.minecraft.world.Container)l.getBlockEntity(cache);box.setItem(0,new ItemStack(Items.DIAMOND,3));box.setItem(2,ItemStack.EMPTY);
                h.assertTrue(HallChanges.advance(l,b,p,own,1)&&l.getBlockState(at).canSurvive(l,at),"each staged mesh has native support: "+p);
                h.assertTrue(!HallChanges.advance(l,b,p,own,2),"one return is too early");h.assertTrue(HallChanges.advance(l,b,p,own,3)&&l.getBlockState(at).canSurvive(l,at),"the later change keeps support");
                var kind=l.getBlockState(at).getValue(HallChangeBlock.KIND);h.assertTrue(kind==HallChangeBlock.Kind.PICTURE_TURNED||kind==HallChangeBlock.Kind.LAMP_OFF||kind==HallChangeBlock.Kind.RUG_FOLDED,"the actual after state is installed");
                h.assertTrue(box==l.getBlockEntity(cache)&&box.getItem(0).getCount()==3&&box.getItem(2).isEmpty()&&l.getBlockState(b.offset(0,0,-8)).isAir(),"containers, finite contents and the central walking route stay exact");
                if(p==LabyrinthPlace.STONE_ARCADE)h.assertTrue(l.getBlockState(at).getLightEmission()==0,"the actual light is off");
            }h.succeed();
        }finally{lease.close();}});
    }
    @GameTest(template="empty")
    public static void chalkGuidanceNeverSpendsFailedToolsOrErasesAPeersOriginal(GameTestHelper h){
        var p=NativeTestPlayers.survival(h,"chalk_reader");var l=p.serverLevel();var clicked=h.absolutePos(new BlockPos(2,2,2));var target=clicked.above();var chalk=new ItemStack(LabyrinthRegistry.CHALK.get());p.setItemSlot(EquipmentSlot.MAINHAND,chalk);NativeTestPlayers.messages(p);
        try{
            l.setBlock(clicked,Blocks.STONE.defaultBlockState(),2);l.setBlock(target,Blocks.DIAMOND_BLOCK.defaultBlockState(),2);
            var hit=new BlockHitResult(clicked.getCenter(),Direction.UP,clicked,false);var item=(NavigationItems.Chalk)chalk.getItem();item.useOn(new UseOnContext(p,InteractionHand.MAIN_HAND,hit));
            h.assertTrue(chalk.getDamageValue()==0&&l.getBlockState(target).is(Blocks.DIAMOND_BLOCK)&&NativeTestPlayers.messages(p).stream().anyMatch(s->s.contains("solid surface")),"an actual failed placement explains itself without consuming chalk or overwriting a block");
            l.setBlock(target,Blocks.AIR.defaultBlockState(),2);NavigationAids.placeChalk(l,target,Direction.UP,Direction.NORTH);NavigationAids.remember(l,target,UUID.randomUUID(),true);p.setShiftKeyDown(true);
            item.useOn(new UseOnContext(p,InteractionHand.MAIN_HAND,new BlockHitResult(target.getCenter(),Direction.UP,target,false)));
            h.assertTrue(l.getBlockState(target).is(HouseBlocks.CHALK_MARK.get())&&chalk.getDamageValue()==0&&NativeTestPlayers.messages(p).stream().anyMatch(s->s.contains("your own")),"a peer's actual marked original remains intact and the refusal is delivered");h.succeed();
        }finally{NativeTestPlayers.remove(p);}
    }
    @GameTest(template="empty")
    public static void companionGuidanceKeepsNativeOrdersHealthAndOwnership(GameTestHelper h){
        var p=NativeTestPlayers.survival(h,"companion_reader");var l=p.serverLevel();var wolf=EntityType.WOLF.create(l);wolf.moveTo(p.position());wolf.setTame(true,false);wolf.setOwnerUUID(p.getUUID());wolf.setHealth(5);l.addFreshEntity(wolf);var id=wolf.getUUID();
        try{
            h.assertTrue(CompanionOrders.issue(wolf,p,CompanionOrders.Order.STAY),"the original owner sets Stay");NativeTestPlayers.messages(p);
            h.assertTrue(!CompanionOrders.issue(wolf,p,CompanionOrders.Order.EXIT)&&NativeTestPlayers.messages(p).stream().anyMatch(s->s.contains("route here")),"an unavailable route gives an actual native message");
            wolf.moveTo(p.getX()+20,p.getY(),p.getZ());h.assertTrue(!CompanionOrders.command(p,wolf.getId(),CompanionOrders.Order.FOLLOW.ordinal())&&NativeTestPlayers.messages(p).stream().anyMatch(s->s.contains("Stand close")),"an out-of-reach wheel request explains how to try again");
            h.assertTrue(wolf.getUUID().equals(id)&&wolf.getHealth()==5&&p.getUUID().equals(wolf.getOwnerUUID())&&CompanionOrders.order(wolf)==CompanionOrders.Order.STAY&&wolf.isOrderedToSit(),"both refusals preserve the actual companion, health, owner and chosen order");h.succeed();
        }finally{wolf.discard();NativeTestPlayers.remove(p);}
    }
}
