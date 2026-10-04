package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID + "_doors")
@PrefixGameTestTemplate(false)
public final class HallwayCarveTests {
    private record Held(ServerLevel level, ChunkPos chunk, BlockPos key) {}
    private static final List<Held> held = new ArrayList<>();
    private static BuildBlocks.Plan plan;
    private static ServerPlayer player;
    private static HouseSavedData oldHouse;
    private static LabyrinthData oldLab;

    private static void hold(ServerLevel level, BlockPos base, LabyrinthPlace place) {
        var r = place.room();
        for (int x = (base.getX()+r.minX()-10)>>4; x <= (base.getX()+r.maxX()+10)>>4; x++)
            for (int z = (base.getZ()+r.minZ()-10)>>4; z <= (base.getZ()+22)>>4; z++) {
                var chunk = new ChunkPos(x,z);
                level.getChunkSource().addRegionTicket(TicketType.PORTAL,chunk,3,base);
                level.getChunk(x,z); held.add(new Held(level,chunk,base));
            }
    }

    private static void cleanup(ServerLevel level) {
        if (player != null) { NativeTestPlayers.remove(player); player = null; }
        if (oldHouse != null) {
            level.getServer().overworld().getDataStorage().set("the_oldest_house",oldHouse);
            level.getServer().overworld().getDataStorage().set("the_oldest_house_labyrinth",oldLab);
            oldHouse = null; oldLab = null;
        }
        for (Held ticket : held)
            ticket.level.getChunkSource().removeRegionTicket(TicketType.PORTAL,ticket.chunk,3,ticket.key);
        held.clear(); plan = null;
        HouseTransitionEvents.clearAll(); LabyrinthBuilder.clearAll();
        HouseChunkKeeper.release(level.getServer());
    }

    @AfterBatch(batch="door_boundary") public static void boundaryDone(ServerLevel level) { cleanup(level); }
    @AfterBatch(batch="door_winchester") public static void winchesterDone(ServerLevel level) { cleanup(level); }
    @AfterBatch(batch="door_rabbit") public static void rabbitDone(ServerLevel level) { cleanup(level); }

    @GameTest(template="empty",batch="door_boundary",timeoutTicks=100)
    public static void actualWaitingPlayerAtTheFarDoorNeverStartsAnOverworldBreach(GameTestHelper h) {
        var server=h.getLevel().getServer(); var level=HouseTestLevel.get(server,HouseDimensions.INTERIOR);
        var origin=new BlockPos(106000,80,106000); oldHouse=HouseSavedData.get(server); oldLab=LabyrinthData.get(server);
        var house=new HouseSavedData(); house.markSpawned(origin); house.markImpossibleDoorRevealed();
        server.overworld().getDataStorage().set("the_oldest_house",house);
        server.overworld().getDataStorage().set("the_oldest_house_labyrinth",new LabyrinthData());
        var at=new Vec3(origin.getX()+HouseLayout.AXIS_X+.5,origin.getY()+1,
                origin.getZ()+HouseImpossibleHallway.END_Z_OFFSET+.51);
        var block=BlockPos.containing(at); level.getChunkAt(block);
        level.setBlock(block.below(),Blocks.SPRUCE_PLANKS.defaultBlockState(),2);
        player=NativeTestPlayers.survival(h,"waiting_at_far_door");
        player.teleportTo(level,at.x,at.y,at.z,0,0); player.setNoGravity(true); player.setDeltaMovement(Vec3.ZERO);
        for (int i=0;i<3;i++) HouseTransitionEvents.onPlayerTick(new PlayerTickEvent.Post(player));
        h.assertTrue(player.serverLevel()==level && HouseTransitionEvents.pendingPhase(player)==null,
                "a native player pressed into the closed door stays inside without scheduling BREACH");
        h.assertTrue(!LabyrinthBuilder.ensureBuilt(server) && LabyrinthBuilder.isCarving(),
                "the fresh world schedules construction and leaves its waiting player in the hallway");
        h.assertTrue(HouseTransitionEvents.pendingPhase(player)==null && player.position().distanceToSqr(at)<.001,
                "scheduling an unfinished carve creates neither a teleport nor an Overworld transition");
        h.succeed();
    }

    @GameTest(template="empty",batch="door_winchester",timeoutTicks=1200)
    public static void slicedWinchesterRetainsItsNativeStairsDoorAndOriginalPaper(GameTestHelper h) {
        var level=HouseTestLevel.get(h.getLevel().getServer(),HouseDimensions.INTERIOR);
        var base=new BlockPos(107000,200,107000); var place=LabyrinthPlace.WINCHESTER; hold(level,base,place);
        plan=BuildBlocks.record(level,()->LiteraryRooms.build(level,base,place));
        h.assertTrue(level.getBlockState(base.offset(LiteraryRooms.source(place))).isAir(),
                "authoring commands does not synchronously construct the scene or its native paper");
        h.assertTrue(!plan.tick() && plan.lastVisits()<=BuildBlocks.MAX_VISITS,
                "the large mansion cannot be placed in one construction tick");
        h.onEachTick(()->{
            boolean done=plan.tick(); h.assertTrue(plan.lastVisits()<=BuildBlocks.MAX_VISITS,"every slice has a hard position limit");
            if (!done) return;
            var note=level.getBlockEntity(base.offset(LiteraryRooms.source(place)));
            h.assertTrue(note instanceof LecternBlockEntity desk && !desk.getBook().isEmpty(),"the authored original is on its native lectern");
            h.assertTrue(level.getBlockState(base).getBlock() instanceof DoorBlock,"the finished entrance is a real native door");
            h.assertTrue(level.getBlockState(base.offset(0,0,-8)).is(Blocks.OAK_SLAB)
                    && level.getBlockState(base.offset(0,7,-68)).is(Blocks.OAK_SLAB),"both ends of the original switchback staircase survive ordered slicing");
            h.succeed();
        });
    }

    @GameTest(template="empty",batch="door_rabbit",timeoutTicks=1200)
    public static void slicedRabbitKeepsThePhysicalTrenchAndDiscoveryWriting(GameTestHelper h) {
        var level=HouseTestLevel.get(h.getLevel().getServer(),HouseDimensions.OUTSIDE);
        var base=new BlockPos(109000,80,109000); var place=LabyrinthPlace.HOLY_RABBIT; hold(level,base,place);
        plan=BuildBlocks.record(level,()->LiteraryRooms.build(level,base,place));
        h.assertTrue(!plan.tick(),"the outdoor forest is paced as well as interior terracotta");
        h.onEachTick(()->{
            boolean done=plan.tick(); h.assertTrue(plan.lastVisits()<=BuildBlocks.MAX_VISITS,"outdoor construction respects the same hard limit");
            if (!done) return;
            h.assertTrue(level.getBlockState(base.offset(0,0,-80)).isAir()
                    && level.getBlockState(base.offset(0,-1,-80)).is(LiteraryRegistry.DRAG_SNOW.get())
                    && level.getBlockState(base.offset(2,1,-80)).is(Blocks.SNOW_BLOCK),"the deep trench remains supported and enclosed by real drifts");
            h.assertTrue(level.getBlockState(base.offset(0,2,-25)).is(LiteraryRegistry.CARVED_TRUNK.get()),"the custom trunk remains integrated into the authored tree");
            h.assertTrue(level.getBlockEntity(base.offset(LiteraryRooms.source(place))) instanceof LecternBlockEntity desk
                    && !desk.getBook().isEmpty(),"sliced outdoor construction includes the original discovery notes");
            h.succeed();
        });
    }
}
