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
        helper.assertTrue(Hillary.tagOf(dog).recipient().equals(recipient.getUUID()) && dog.isTame() && dog.getHealth()==7
                && dog.getPersistentData().getBoolean("HillaryFindExit"),"health, owner, tag and requested route survive");
        helper.succeedWhen(()->{
            helper.assertTrue(target.getEntity(id)==dog,"the companion is tracked by the destination world");
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
}
