package io.github.knaitoe.theoldesthouse.gametest;

import com.mojang.authlib.GameProfile;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseSitting;
import io.github.knaitoe.theoldesthouse.house.SeatEntity;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import io.github.knaitoe.theoldesthouse.opening.*;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;

@GameTestHolder(TheOldestHouse.MOD_ID)
@net.neoforged.neoforge.gametest.PrefixGameTestTemplate(false)
public final class CompanionTests {
    private static final java.util.List<net.minecraft.server.level.ServerPlayer> MOCKS = new java.util.ArrayList<>();
    private static net.minecraft.server.level.ServerPlayer mock(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel(); MOCKS.add(player); return player;
    }
    private static void remove(net.minecraft.server.level.ServerPlayer player) {
        if (player.server.getPlayerList().getPlayers().contains(player)) player.server.getPlayerList().remove(player);
        MOCKS.remove(player);
    }
    @net.minecraft.gametest.framework.AfterBatch(batch = "companions")
    public static void cleanPlayers(net.minecraft.server.level.ServerLevel level) {
        for (var player : java.util.List.copyOf(MOCKS)) remove(player);
    }
    @GameTest(template = "empty", batch = "companions")
    public static void seatsMountOnePlayerAndCleanUpAfterDismount(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        level.setBlock(pos, Blocks.DARK_OAK_STAIRS.defaultBlockState(), 3);
        var a = mock(helper);
        var b = mock(helper);
        a.moveTo(pos.getX()+.5, pos.getY()+1, pos.getZ()+.5); b.moveTo(a.getX(),a.getY(),a.getZ());
        helper.assertTrue(HouseSitting.sit(a, pos), "the reader can sit in a chair");
        SeatEntity seat = (SeatEntity)a.getVehicle();
        helper.assertTrue(!HouseSitting.sit(b, pos), "two players cannot occupy one chair");
        a.stopRiding(); seat.tick();
        helper.assertTrue(seat.isRemoved() && !a.isPassenger(), "dismount removes the anchor");
        level.setBlock(pos, Blocks.BROWN_CARPET.defaultBlockState(), 3);
        helper.assertTrue(HouseSitting.sit(a, pos), "camp bedrolls support sitting");
        seat = (SeatEntity)a.getVehicle(); level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3); seat.tick();
        helper.assertTrue(!a.isPassenger() && seat.isRemoved(), "removing the seat frees its occupant");
        remove(a); remove(b); helper.succeed();
    }
    @GameTest(template = "empty", batch = "companions")
    public static void recentRoomsAreSuppressedPerPlayerAndPersist(GameTestHelper helper) {
        LabyrinthData data = new LabyrinthData(); UUID a=UUID.randomUUID(), b=UUID.randomUUID();
        int ordinary = LabyrinthDealer.rememberedWeight(data,a,LabyrinthPlace.HARRIGAN,3);
        data.visit(a,LabyrinthPlace.HARRIGAN);
        helper.assertTrue(LabyrinthDealer.rememberedWeight(data,a,LabyrinthPlace.HARRIGAN,3)*12 == ordinary,
                "the latest story is twelve times less likely");
        helper.assertTrue(LabyrinthDealer.rememberedWeight(data,b,LabyrinthPlace.HARRIGAN,3)==ordinary, "visits are personal");
        data.visit(a,LabyrinthPlace.EXPLORER_CAMP);
        helper.assertTrue(LabyrinthDealer.rememberedWeight(data,a,LabyrinthPlace.EXPLORER_CAMP,3)==3, "special gray rooms are suppressed too");
        var saved=LabyrinthData.FACTORY.deserializer().apply(data.save(new CompoundTag(),helper.getLevel().registryAccess()),helper.getLevel().registryAccess());
        helper.assertTrue(saved.recentVisit(a,LabyrinthPlace.EXPLORER_CAMP)==0 && saved.recentVisit(a,LabyrinthPlace.HARRIGAN)==1, "recent order survives restart");
        for(int i=0;i<8;i++)data.visit(a,LabyrinthPlace.GRAY_CORRIDOR);
        helper.assertTrue(LabyrinthDealer.rememberedWeight(data,a,LabyrinthPlace.HARRIGAN,3)==ordinary, "weight recovers after exploration");
        helper.assertTrue(LabyrinthDealer.giveScent(data,a)==LabyrinthDealer.Scent.SEEKING, "recent rooms do not disable the dog's scent");
        helper.succeed();
    }
    @GameTest(template = "empty", batch = "companions")
    public static void houseWaitsUntilTheMorningAfterHillaryAndPersistsThatDay(GameTestHelper helper) {
        OpeningPlayerState state = new OpeningPlayerState(); state.markLetterDelivered(10);
        helper.assertTrue(!state.houseDue(11), "the letter cannot spawn the House");
        state.markHillaryArrived(11);
        helper.assertTrue(!state.houseDue(11) && state.houseDue(12), "Hillary has one day at the player's base");
        var tag=OpeningPlayerState.CODEC.encodeStart(NbtOps.INSTANCE,state).getOrThrow();
        var saved=OpeningPlayerState.CODEC.parse(NbtOps.INSTANCE,tag).getOrThrow();
        helper.assertTrue(saved.hillaryDay()==11 && saved.houseDue(12), "restart keeps the three-day order");
        helper.succeed();
    }
    @GameTest(template = "empty", batch = "companions")
    public static void hillaryTransfersWithIdentityHealthAndHerExitRequest(GameTestHelper helper) {
        var source=helper.getLevel(); var target=source.getServer().getLevel(Level.NETHER);
        var recipient=mock(helper);
        BlockPos home=helper.absolutePos(new BlockPos(2,2,4));
        source.setBlock(home.below(),Blocks.STONE.defaultBlockState(),3);
        recipient.moveTo(home.getX()+.5,home.getY(),home.getZ()+.5);
        var wolf=Hillary.spawn(source,home,recipient.getUUID());
        helper.assertTrue(wolf!=null,"Hillary spawned"); wolf.tame(recipient); Hillary.acknowledge(wolf); wolf.setHealth(7);
        wolf.getPersistentData().putBoolean("HillaryFindExit",true); UUID id=wolf.getUUID();
        target.getChunkAt(BlockPos.ZERO);
        for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++) {
            target.setBlock(new BlockPos(x,99,z),Blocks.STONE.defaultBlockState(),3);
            for(int y=100;y<=103;y++)target.setBlock(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState(),3);
        }
        recipient.teleportTo(target,.5,100,.5,0,0);
        var dog=Hillary.followAcross(wolf,recipient);
        helper.assertTrue(dog!=null && dog.level()==target && dog.getUUID().equals(id),"the same UUID crossed dimensions");
        BlockPos arrival=BlockPos.containing(dog.position());
        helper.assertTrue(Hillary.tagOf(dog).recipient().equals(recipient.getUUID()) && dog.isTame() && dog.getHealth()==7
                && dog.getPersistentData().getBoolean("HillaryFindExit"),"health, owner, tag and requested route survive");
        helper.succeedWhen(()->{
            helper.assertTrue(target.getEntity(id)==dog,"the companion is tracked by the destination world");
            // The crossing's vanilla portal ticket would outlive the whole run by
            // fifteen seconds. Release it, so no ticket of this test's is still
            // held when the server shuts down.
            String held=ShutdownWatch.ticketsAt(target,new ChunkPos(arrival));
            helper.assertTrue(held.contains("portal"),"the crossing holds her arrival with a portal ticket: "+held);
            target.getChunkSource().removeRegionTicket(TicketType.PORTAL,new ChunkPos(arrival),3,arrival);
            String left=ShutdownWatch.ticketsAt(target,new ChunkPos(arrival));
            helper.assertTrue(!left.contains("portal"),"the portal ticket is released: "+left);
            dog.discard(); remove(recipient);
        });
    }
    @GameTest(template = "empty", batch = "companions")
    public static void dogCanTraceBackFromDeepMazeAndBothSidesOfFolds(GameTestHelper helper) {
        var layout=MazeLayout.create(LabyrinthPlace.ABYSS_MAZE,747);
        BlockPos goal=new BlockPos(0,0,-1);
        java.util.Set<BlockPos> starts=new java.util.HashSet<>();
        layout.floor().stream().filter(p->Math.floorMod(p.hashCode(),31)==0).limit(24).forEach(starts::add);
        for(var sleeve:layout.sleeves())for(int side:new int[]{-2,2})starts.add(sleeve.block(new BlockPos(0,0,side)));
        for(BlockPos floor:starts)helper.assertTrue(HillaryPaths.nextStep(layout,floor,goal)!=null,
                "each corridor has an exit route without requiring a fold crossing: "+floor);
        helper.succeed();
    }

    @GameTest(template="empty",batch="companions")
    public static void rescueAnimalsAreVulnerableNativePetsAndScenesStayPrivate(GameTestHelper helper) {
        var level=helper.getLevel();var owner=mock(helper);
        BlockPos at=helper.absolutePos(new BlockPos(2,2,2));
        owner.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5);
        for(boolean cat:java.util.List.of(false,true)) {
            var pet=LabyrinthEncounters.spawnStray(level,owner.position().add(1,0,0),cat);
            helper.assertTrue(pet!=null&&!pet.isTame()&&!pet.isNoAi()&&!pet.isInvulnerable(),
                    "lost animals begin as vulnerable native animals");
            pet.tame(owner);
            helper.assertTrue(CompanionOrders.canCommand(owner,pet)
                    &&CompanionOrders.issue(pet,owner,CompanionOrders.Order.STAY),
                    "rescued cats and dogs use native owners and companion orders");
            pet.discard();
        }
        helper.assertTrue(!LabyrinthEncounters.eligible(LabyrinthPlace.HIDE_AND_CLAP)
                &&!LabyrinthEncounters.eligible(LabyrinthPlace.HARRIGAN)
                &&!LabyrinthEncounters.eligible(LabyrinthPlace.MOTHER_DEN),
                "ambient encounters do not interrupt authored scenes");
        remove(owner);helper.succeed();
    }

    @GameTest(template="empty",batch="companions")
    public static void commandWheelChecksOwnerReachAndSavesStay(GameTestHelper helper) {
        var level=helper.getLevel();var owner=mock(helper);var stranger=mock(helper);
        BlockPos at=helper.absolutePos(new BlockPos(2,2,2));
        owner.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5);stranger.moveTo(owner.position());
        var cat=net.minecraft.world.entity.EntityType.CAT.create(level);
        cat.moveTo(owner.position().add(1,0,0));cat.tame(owner);level.addFreshEntity(cat);
        helper.assertTrue(!CompanionOrders.command(stranger,cat.getId(),CompanionOrders.Order.STAY.ordinal()),"another player cannot command your animal");
        helper.assertTrue(CompanionOrders.command(owner,cat.getId(),CompanionOrders.Order.STAY.ordinal()),"an owned cat can stay");
        helper.assertTrue(cat.isOrderedToSit()&&cat.getOwnerUUID().equals(owner.getUUID()),"stay retains native ownership");
        CompoundTag saved=new CompoundTag();cat.save(saved);
        var restored=net.minecraft.world.entity.EntityType.CAT.create(level);restored.load(saved);
        helper.assertTrue(CompanionOrders.order(restored)==CompanionOrders.Order.STAY&&restored.isOrderedToSit()
                &&owner.getUUID().equals(restored.getOwnerUUID()),"restart keeps the command and the owner");
        owner.moveTo(owner.position().add(20,0,0));
        helper.assertTrue(!CompanionOrders.command(owner,cat.getId(),CompanionOrders.Order.FOLLOW.ordinal()),"a packet cannot command a faraway animal");
        helper.assertTrue(!CompanionOrders.command(stranger,cat.getId(),99),"invalid wheel choices are rejected");
        helper.assertTrue(CompanionOrders.hesitationTicks(1)==0&&CompanionOrders.hesitationTicks(50)<=40,
                "fear is a bounded pause");
        cat.discard();remove(owner);remove(stranger);helper.succeed();
    }
    @GameTest(template="empty",batch="companions",timeoutTicks=200)
    public static void rescuedCatCrossesWithOwnerAndItsSavedOrder(GameTestHelper helper) {
        var source=helper.getLevel();var target=source.getServer().getLevel(Level.NETHER);var owner=mock(helper);
        BlockPos at=helper.absolutePos(new BlockPos(2,2,2));source.setBlock(at.below(),Blocks.STONE.defaultBlockState(),3);
        owner.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5);
        var pet=LabyrinthEncounters.spawnStray(source,owner.position().add(1,0,0),true);
        helper.assertTrue(pet instanceof net.minecraft.world.entity.animal.Cat,"a stray cat is a real cat");
        pet.tame(owner);pet.setHealth(4);CompanionOrders.issue(pet,owner,CompanionOrders.Order.FOLLOW);
        UUID id=pet.getUUID();
        helper.assertTrue(CompanionOrders.followingAll(owner).contains(pet),"a rescued cat can replace the original companion at a threshold");
        target.getChunkAt(BlockPos.ZERO);
        for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++) {
            target.setBlock(new BlockPos(x,107,z),Blocks.STONE.defaultBlockState(),3);
            for(int y=108;y<=111;y++)target.setBlock(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState(),3);
        }
        // Share the established Nether test chunk with Hillary's transfer fixture,
        // on a separate floor so both native animals can be observed independently.
        owner.teleportTo(target,.5,108,.5,0,0);
        var cat=CompanionOrders.followAcross(pet,owner);
        helper.assertTrue(cat!=null&&cat.getUUID().equals(id)&&cat.level()==target&&cat.getHealth()==4
                &&owner.getUUID().equals(cat.getOwnerUUID())&&CompanionOrders.order(cat)==CompanionOrders.Order.FOLLOW,
                "health, identity, ownership and the selected order survive the crossing");
        BlockPos arrival=cat.blockPosition();
        helper.onEachTick(()->{
            if(target.getEntity(id)!=cat)return;
            helper.assertTrue(target.getEntity(id)==cat,"the cat is tracked in the new world; removed="+cat.isRemoved()
                    +"; position="+cat.position()+"; tickets="+ShutdownWatch.ticketsAt(target,new ChunkPos(cat.blockPosition())));
            target.getChunkSource().removeRegionTicket(TicketType.PORTAL,new ChunkPos(arrival),3,arrival);
            cat.discard();remove(owner);
            helper.succeed();
        });
    }
    @GameTest(template="empty",batch="companions")
    public static void dogAndCatCanFindRoutesAroundOrdinaryCorners(GameTestHelper helper) {
        for(var place:java.util.List.of(LabyrinthPlace.BENT_HALL,LabyrinthPlace.CROSS_HALL,LabyrinthPlace.STRAIGHT_HALL)) {
            var floor=LabyrinthHalls.floor(place);BlockPos goal=new BlockPos(0,0,-1);
            for(BlockPos pos:floor)helper.assertTrue(HillaryPaths.nextStep(floor,pos,goal)!=null,
                    "the physical hall has a route back from "+place+"/"+pos);
        }
        helper.succeed();
    }
    @GameTest(template="empty",batch="companions")
    public static void marksStayPhysicalAndObservedMarksAreProtected(GameTestHelper helper) {
        var level=helper.getLevel();BlockPos pos=helper.absolutePos(new BlockPos(2,2,2));
        level.getChunkAt(pos);
        for(int x=0;x<=3;x++){level.setBlock(pos.east(x),Blocks.AIR.defaultBlockState(),3);level.setBlock(pos.east(x).below(),Blocks.STONE.defaultBlockState(),3);}
        helper.assertTrue(NavigationAids.placeChalk(level,pos,net.minecraft.core.Direction.UP,net.minecraft.core.Direction.NORTH),"chalk draws on a floor");
        helper.assertTrue(level.getBlockState(pos).getCollisionShape(level,pos).isEmpty(),"a painted mark cannot obstruct a corridor");
        level.setBlock(pos.east(3),Blocks.STONE.defaultBlockState(),3);
        helper.assertTrue(!NavigationAids.placeChalk(level,pos.east(3),net.minecraft.core.Direction.UP,net.minecraft.core.Direction.NORTH),"chalk cannot replace a solid block");
        helper.assertTrue(NavigationAids.placeLine(level,pos.east())&&NavigationAids.placeLine(level,pos.east(2)),"two real trail pieces are placed");
        helper.assertTrue(level.getBlockState(pos.east()).getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.EAST)
                &&level.getBlockState(pos.east(2)).getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WEST),"adjacent pieces meet without a HUD line");
        var owner=mock(helper);var observer=mock(helper);
        owner.moveTo(pos.getX()+20,pos.getY(),pos.getZ()+.5);owner.setYRot(0);
        observer.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+3);observer.setYRot(180);
        NavigationAids.remember(level,pos,owner.getUUID(),true);
        helper.assertTrue(!NavigationAids.erase(observer,pos),"another player cannot erase your mark");
        helper.assertTrue(!NavigationAids.mayAlter(level,pos,owner),"any player watching protects the mark");
        observer.setYRot(0);
        helper.assertTrue(NavigationAids.mayAlter(level,pos,owner),"a distant unseen mark can be disturbed");
        helper.assertTrue(NavigationAids.erase(owner,pos),"the owner can rub their chalk out");
        remove(owner);remove(observer);helper.succeed();
    }
}

