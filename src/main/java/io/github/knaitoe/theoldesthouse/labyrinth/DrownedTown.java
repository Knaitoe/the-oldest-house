package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Indian Lake, verb: breath. A shared two-visit sequence with personal witnessing at its ending. */
public final class DrownedTown {
    public static final String ID = "drowned_town";
    public static final int FINAL_VISIT = 2;
    // 0.4.67: Proofrock and Indian Lake High. The furnace is on the beach; the essays and key are in teachers' desks.
    public static final BlockPos FURNACE = ProofrockTown.FURNACE, SUPPLIES = ProofrockTown.SUPPLIES;
    public static final BlockPos SCHOOL_DOOR = IndianLakeHigh.ENTRANCE;
    public static final BlockPos[] PAPERS = IndianLakeHigh.ESSAY_DESKS;
    public static final BlockPos KEY_DESK = IndianLakeHigh.KEY_DESK;
    public static final BlockPos CHURCH_DOOR = ProofrockTown.CHURCH_DOOR, ROOF_HATCH = ProofrockTown.ROOF_HATCH;
    private static final String BODY = "the_oldest_house_indian_lake_body";
    public static final String SHORE_BODY="the_oldest_house_lake_shore_body", TOWN_CANOE="the_oldest_house_town_canoe";
    private static final Map<UUID, BlockPos> OPEN_FURNACES = new HashMap<>();
    private static long nextHymn;
    private DrownedTown() {}

    public static int nextVisit(int visit, boolean beatDone) { return visit < 1 ? 1 : beatDone && visit < FINAL_VISIT ? visit + 1 : visit; }
    public static boolean contains(BlockPos base, Vec3 point) { return bounds(base).contains(point); }
    private static AABB bounds(BlockPos base) { return IndianLakeRooms.bounds(base, LabyrinthPlace.DROWNED_TOWN); }
    public static @Nullable BlockPos base(MinecraftServer server) {
        BlockPos origin = HouseSavedData.get(server).houseOrigin();
        return origin == null ? null : LabyrinthPlaces.base(origin, LabyrinthPlace.DROWNED_TOWN);
    }
    private static List<ServerPlayer> visitors(ServerLevel level, BlockPos base) {
        return level.players().stream().filter(p -> p.isAlive() && !p.isSpectator() && contains(base, p.position())).toList();
    }

    /** Recorded in slices when a plan is open; the stocking and staging run after the blocks they fill exist. */
    public static void build(MinecraftServer server, ServerLevel level, BlockPos base) {
        ProofrockTown.build(level, base);
        BuildBlocks.after(level, () -> stock(server, level, base));
    }
    private static void stock(MinecraftServer server, ServerLevel level, BlockPos base) {
        LabyrinthData data = LabyrinthData.get(server);
        CompoundTag state = data.state(ID);
        // First construction stocks finite native inventories. Ordinary arrivals and later rebuilds never refill them.
        if (!state.getBoolean("PaperStocked")) {
            for (int i = 0; i < PAPERS.length; i++) if (level.getBlockEntity(base.offset(PAPERS[i])) instanceof Container desk) {
                desk.setItem(0, new ItemStack(DrownedTownRegistry.wetEssay(i))); desk.setChanged();
            }
            if (level.getBlockEntity(base.offset(SUPPLIES)) instanceof Container box) {
                box.setItem(0, new ItemStack(Items.COAL, 4)); box.setItem(1, new ItemStack(Items.OAK_DOOR, 2));
                box.setItem(2, new ItemStack(Items.COOKED_COD, 2)); box.setChanged();
            }
            state.putBoolean("PaperStocked", true);
        }
        state.putBoolean("Built", true); data.setState(ID, state);
        TownCarry.restore(level, base, data);
        stage(level, base, data);
    }

    public static void onArrive(ServerPlayer player, LabyrinthPlace place) {
        if (place != LabyrinthPlace.DROWNED_TOWN) return;
        ServerLevel level = player.server.getLevel(HouseDimensions.OUTSIDE); BlockPos base = base(player.server);
        if (level == null || base == null) return;
        keepLoaded(level, base);
        if (visitors(level, base).stream().anyMatch(other -> !other.getUUID().equals(player.getUUID()))) {guidance(player);return;}
        LabyrinthData data = LabyrinthData.get(player.server); CompoundTag state = data.state(ID);
        int next = nextVisit(state.getInt("Visit"), state.getBoolean("BeatDone"));
        if (next != state.getInt("Visit")) {
            state.putInt("Visit", next); state.putBoolean("BeatDone", false);
            state.remove("WitchDefeatedVisit"); data.setState(ID, state);
            for (LakeWitchEntity witch : level.getEntitiesOfClass(LakeWitchEntity.class, bounds(base))) witch.discard();
        }
        stage(level, base, data);
        player.displayClientMessage(Component.literal(data.isCompleted(ID) ? "The roof is open. The hymn carries over the lake."
                : next == 1 ? "The high school is up Main Street on the left, by the water. Dry its three damp essays at the beach furnace; then leave and return."
                : "The church key is in the school. The steeple stands out of the lake; open the church door beneath it, then release a breath beneath the roof hatch."), false);
    }

    public static void guidance(ServerPlayer p){
        var state=LabyrinthData.get(p.server).state(ID);int mask=state.getInt("DryMask");
        p.displayClientMessage(Component.literal(state.getBoolean("RoofOpened")?"The church roof is open. Follow the hymn beneath its steeple.":state.getInt("Visit")<2?"The high school is at the top of Main Street, on the left. Dry its three damp essays at the beach furnace ("+Integer.bitCount(mask)+"/3); then leave and return.":state.getBoolean("ChurchUnlocked")?"In the drowned church, release a breath directly beneath the roof hatch.":"The church key waits in the school. The steeple stands out of the lake."),false);
    }
    /** In-place changes only: player doors, drops, furnace contents and depleted desks survive visits/restarts. */
    public static void stage(ServerLevel level, BlockPos base, LabyrinthData data) {
        CompoundTag state = data.state(ID);
        if (state.getInt("Visit") >= 2 && !state.getBoolean("KeyPlaced")) {
            // The principal's own desk takes the key; a missing desk is set back first, never refilled once given.
            BlockPos at = base.offset(KEY_DESK);
            if (!(level.getBlockEntity(at) instanceof Container)) level.setBlock(at, SchoolDeskBlock.facing(net.minecraft.core.Direction.SOUTH), LabyrinthBuilder.flags());
            if (level.getBlockEntity(at) instanceof Container desk) {
                int slot = 0; while (slot < desk.getContainerSize() && !desk.getItem(slot).isEmpty()) slot++;
                // A drawer a reader has filled keeps everything in it; the key is left on top.
                if (slot < desk.getContainerSize()) { desk.setItem(slot, new ItemStack(DrownedTownRegistry.CHURCH_KEY.get())); desk.setChanged(); }
                else net.minecraft.world.Containers.dropItemStack(level, at.getX() + .5, at.getY() + 1, at.getZ() + .5, new ItemStack(DrownedTownRegistry.CHURCH_KEY.get()));
                state.putBoolean("KeyPlaced", true); data.setState(ID, state);
            }
        }
        setChurchDoor(level, base, state.getBoolean("ChurchUnlocked"));
        setRoof(level, base, state.getBoolean("RoofOpened"));
        ensureBodies(level, base, data);
        stageShore(level,base,data);
    }

    public static boolean dried(LabyrinthData data, int essay) {
        if (essay < 0 || essay >= 3 || data.isCompleted(ID)) return false;
        CompoundTag state = data.state(ID); int mask = state.getInt("DryMask"), next = mask | (1 << essay);
        if (mask == next) return false;
        state.putInt("DryMask", next);
        if (next == 7 && state.getInt("Visit") == 1) state.putBoolean("BeatDone", true);
        data.setState(ID, state); return true;
    }
    public static int essayIndex(ItemStack stack) {
        for (int i = 0; i < 3; i++) if (stack.is(DrownedTownRegistry.dryEssay(i))) return i; return -1;
    }
    private static void recordDrying(MinecraftServer server, int essay) {
        LabyrinthData data = LabyrinthData.get(server);
        if (dried(data, essay) && data.state(ID).getInt("DryMask") == 7) {
            ServerLevel level = server.getLevel(HouseDimensions.OUTSIDE); BlockPos base = base(server);
            if (level != null && base != null) for (ServerPlayer player : visitors(level, base))
                player.displayClientMessage(Component.literal("The last page dries. The school essay says to leave, and return."), false);
        }
    }
    public static void onSmelted(PlayerEvent.ItemSmeltedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !player.level().dimension().equals(HouseDimensions.OUTSIDE)) return;
        BlockPos base = base(player.server), opened = OPEN_FURNACES.get(player.getUUID());
        if (base != null && opened != null && opened.equals(base.offset(FURNACE)) && contains(base, player.position())
                && player.containerMenu instanceof net.minecraft.world.inventory.FurnaceMenu)
            recordDrying(player.server, essayIndex(event.getSmelting()));
    }

    /** The key is never consumed, and there is no native redstone shortcut into the authored gate. */
    public static boolean unlockChurch(ServerPlayer player, BlockPos at) {
        BlockPos base = base(player.server);
        if (base == null || !player.serverLevel().dimension().equals(HouseDimensions.OUTSIDE)
                || (at.distManhattan(base.offset(CHURCH_DOOR)) > 1) || player.distanceToSqr(Vec3.atCenterOf(at)) > 36) return false;
        LabyrinthData data = LabyrinthData.get(player.server); CompoundTag state = data.state(ID);
        if (state.getInt("Visit") < 2 || state.getInt("DryMask") != 7) return false;
        if (!state.getBoolean("ChurchUnlocked") && !player.getMainHandItem().is(DrownedTownRegistry.CHURCH_KEY.get())
                && !player.getOffhandItem().is(DrownedTownRegistry.CHURCH_KEY.get())) return false;
        state.putBoolean("ChurchUnlocked", true); data.setState(ID, state); setChurchDoor(player.serverLevel(), base, true); return true;
    }
    public static boolean openRoof(ServerPlayer player, BlockPos at) {
        BlockPos base = base(player.server);
        if (base == null || player.isSpectator() || !player.level().dimension().equals(HouseDimensions.OUTSIDE)
                || !at.equals(base.offset(ROOF_HATCH)) || player.distanceToSqr(Vec3.atCenterOf(at)) > 36) return false;
        LabyrinthData data = LabyrinthData.get(player.server); CompoundTag state = data.state(ID);
        if (state.getInt("Visit") < 2 || state.getInt("DryMask") != 7 || !state.getBoolean("ChurchUnlocked")) return false;
        if (state.getBoolean("RoofOpened")) { setRoof(player.serverLevel(), base, true); return false; }
        state.putBoolean("RoofOpened", true); state.putBoolean("BeatDone", true); data.setState(ID, state); data.setCompleted(ID, true);
        setRoof(player.serverLevel(), base, true);
        player.serverLevel().playSound(null, at, DrownedTownRegistry.HYMN.get(), SoundSource.AMBIENT, 1.4F, 1);
        nextHymn = player.serverLevel().getGameTime() + 160;
        WitnessAccount.resolve(player, WitnessAccount.Story.DROWNED_TOWN, "released_hymn");
        player.displayClientMessage(Component.literal("The hymn goes out across the water. The preacher keeps singing below."), false);
        return true;
    }
    private static void setChurchDoor(ServerLevel level, BlockPos base, boolean open) {
        for (BlockPos at : new BlockPos[]{base.offset(CHURCH_DOOR), base.offset(CHURCH_DOOR).above()}) {
            BlockState state = level.getBlockState(at);
            if (state.is(Blocks.IRON_DOOR) && state.getValue(DoorBlock.OPEN) != open)
                level.setBlock(at, state.setValue(DoorBlock.OPEN, open), LabyrinthBuilder.flags());
        }
    }
    private static void setRoof(ServerLevel level, BlockPos base, boolean open) {
        BlockPos at = base.offset(ROOF_HATCH); BlockState state = level.getBlockState(at);
        if (state.is(Blocks.IRON_TRAPDOOR) && state.getValue(TrapDoorBlock.OPEN) != open)
            level.setBlock(at, state.setValue(TrapDoorBlock.OPEN, open), LabyrinthBuilder.flags());
    }

    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.isSpectator() || event.getHand() != InteractionHand.MAIN_HAND
                || !player.level().dimension().equals(HouseDimensions.OUTSIDE)) return;
        BlockPos base = base(player.server); if (base == null || !contains(base, player.position())) return;
        BlockPos at = event.getPos();
        if (at.equals(base.offset(FURNACE))) { OPEN_FURNACES.put(player.getUUID(), at.immutable()); return; }
        if (at.equals(base.offset(CHURCH_DOOR)) || at.equals(base.offset(CHURCH_DOOR).above())) {
            if (!unlockChurch(player, at)) {
                // Say what actually holds the door: the school's essays come before the key does.
                var st = LabyrinthData.get(player.server).state(ID);
                player.displayClientMessage(Component.literal(st.getInt("Visit") < 2 || st.getInt("DryMask") != 7
                        ? "The church door won't move yet. The school's essays come first."
                        : "The church door needs its key. The school kept it."), true);
                io.github.knaitoe.theoldesthouse.house.PlaytestLog.refused(player, "church_door",base.offset(CHURCH_DOOR));
            }
            event.setCanceled(true); event.setCancellationResult(InteractionResult.SUCCESS);
        } else if (at.equals(base.offset(ROOF_HATCH))) {
            boolean wasOpen = LabyrinthData.get(player.server).state(ID).getBoolean("RoofOpened");
            if (!openRoof(player, at) && !wasOpen) player.displayClientMessage(Component.literal("The hymn presses against the roof. The church door is still sealed."), true);
            event.setCanceled(true); event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    /** Portable air doors/soul sand work in the lake, below its surface, away from the church and its steeple. */
    public static boolean allowsPlacing(Level level, BlockPos pos, BlockState state) {
        if (!(level instanceof ServerLevel serverLevel) || !level.dimension().equals(HouseDimensions.OUTSIDE)) return false;
        BlockPos base = base(serverLevel.getServer()); if (base == null) return false;
        BlockPos r = pos.subtract(base);
        if (!ProofrockTown.water(r.getX(), r.getZ()) || r.getY() < -11 || r.getY() > -2) return false;
        if (r.getX() >= 20 && r.getX() <= 40 && r.getZ() >= -125 && r.getZ() <= -100) return false;
        return state.getBlock() instanceof DoorBlock || state.is(Blocks.SOUL_SAND);
    }
    public static void onPlaced(BlockEvent.EntityPlaceEvent event) {
        if (event.isCanceled() || !(event.getLevel() instanceof ServerLevel level) || !allowsPlacing(level, event.getPos(), event.getPlacedBlock())) return;
        LabyrinthData data = LabyrinthData.get(level.getServer()); CompoundTag state = data.state(ID), placed = state.getCompound("PlacedAirTools");
        placed.putString(Long.toString(event.getPos().asLong()), event.getPlacedBlock().getBlock() instanceof DoorBlock ? "door" : "sand");
        if (event.getPlacedBlock().getBlock() instanceof DoorBlock) placed.putString(Long.toString(event.getPos().above().asLong()), "door");
        state.put("PlacedAirTools", placed); data.setState(ID, state);
    }
    public static boolean canBreak(ServerLevel level, BlockPos pos) {
        if (!level.dimension().equals(HouseDimensions.OUTSIDE)) return false;
        String expected = LabyrinthData.get(level.getServer()).state(ID).getCompound("PlacedAirTools").getString(Long.toString(pos.asLong()));
        return expected.equals("door") && level.getBlockState(pos).getBlock() instanceof DoorBlock
                || expected.equals("sand") && level.getBlockState(pos).is(Blocks.SOUL_SAND);
    }

    private static void keepLoaded(ServerLevel level, BlockPos base) { IndianLakeRooms.keepLoaded(level, base, LabyrinthPlace.DROWNED_TOWN); }
    private static void ensureWitch(ServerLevel level, BlockPos base, LabyrinthData data) {
        int visit = Math.max(1, data.state(ID).getInt("Visit"));
        if (data.state(ID).getInt("WitchDefeatedVisit") == visit) return;
        List<LakeWitchEntity> witches = level.getEntitiesOfClass(LakeWitchEntity.class, bounds(base));
        if (!witches.isEmpty()) { for (int i = 1; i < witches.size(); i++) witches.get(i).discard(); return; }
        LakeWitchEntity witch = DrownedTownRegistry.LAKE_WITCH.get().create(level); if (witch == null) return;
        BlockPos spawn = witchSpawn(level,base); witch.shore(base, visit);
        witch.moveTo(Vec3.atBottomCenterOf(spawn)); level.addFreshEntity(witch);
    }
    public static BlockPos witchSpawn(ServerLevel level,BlockPos base){
        for(var relative:ProofrockTown.LURKS){
            var at=base.offset(relative);if(LakeWitchEntity.walkable(level,base,at)
                    &&visitors(level,base).stream().noneMatch(p->LakeWitchEntity.inView(p,Vec3.atBottomCenterOf(at).add(0,.6,0))))return at;
        }return base.offset(ProofrockTown.LURKS.getFirst());
    }
    /** One original usable canoe and one shore casualty, independent of the later church aftermath. */
    public static void stageShore(ServerLevel level,BlockPos base,LabyrinthData data){
        var state=data.state(ID);
        if(!state.getBoolean("TownCanoePlaced")){
            var canoe=net.minecraft.world.entity.EntityType.BOAT.create(level);
            if(canoe!=null){canoe.setVariant(net.minecraft.world.entity.vehicle.Boat.Type.SPRUCE);canoe.addTag(TOWN_CANOE);
                canoe.moveTo(base.getX()+ProofrockTown.CANOE.getX()+.5,base.getY()-.25,base.getZ()+ProofrockTown.CANOE.getZ()+.5,90,0);
                if(level.addFreshEntity(canoe)){state.putBoolean("TownCanoePlaced",true);state.putUUID("TownCanoeUUID",canoe.getUUID());}}
        }
        if(!state.getBoolean("ShoreBodyPlaced")){
            var body=DrownedTownRegistry.CONGREGANT.get().create(level);
            if(body!=null){body.addTag(SHORE_BODY);body.pose(false,false);body.lying(true);body.preservedEra(2);
                body.moveTo(base.getX()+ProofrockTown.SHORE_BODY.getX()+.5,base.getY()+.03,base.getZ()+ProofrockTown.SHORE_BODY.getZ()+.5,32,0);
                if(level.addFreshEntity(body)){state.putBoolean("ShoreBodyPlaced",true);state.putUUID("ShoreBodyUUID",body.getUUID());}}
        }
        data.setState(ID,state);
    }
    private static void ensureBodies(ServerLevel level, BlockPos base, LabyrinthData data) {
        boolean shore = IndianLakeProgress.deadOnShore(data);
        List<LakeCongregantEntity> bodies = level.getEntitiesOfClass(LakeCongregantEntity.class, bounds(base), b -> b.getTags().contains(BODY));
        for (int index = 0; index < 7; index++) {
            String tag = BODY + "_" + index; LakeCongregantEntity body = bodies.stream().filter(b -> b.getTags().contains(tag)).findFirst().orElse(null);
            boolean preacher = index == 0;
            if (body == null) { body = DrownedTownRegistry.CONGREGANT.get().create(level); if (body == null) continue; body.addTag(BODY); body.addTag(tag); }
            // The preacher stands on the floor behind the pulpit; the congregation sits in the pews facing it.
            BlockPos at = preacher ? base.offset(ProofrockTown.PREACHER)
                    : shore ? base.offset(-16 + index * 5, 0, -90)
                    : base.offset(index % 2 == 0 ? 26 : 34, -11, -107 - (index - 1) / 2 * 2);
            body.pose(preacher, !preacher && !shore);
            body.moveTo(at.getX() + .5, at.getY(), at.getZ() + .5, preacher ? 0 : shore ? 0 : 180, 0);
            if (!bodies.contains(body)) level.addFreshEntity(body);
        }
    }
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer(); if (server.getTickCount() % 10 != 0) return;
        ServerLevel level = server.getLevel(HouseDimensions.OUTSIDE); BlockPos base = base(server);
        LabyrinthData data = LabyrinthData.get(server);
        if (level == null || base == null || !data.state(ID).getBoolean("Built") || visitors(level, base).isEmpty()) return;
        if (server.getTickCount() % 20 == 0) keepLoaded(level, base);
        ensureWitch(level, base, data);
        if (level.getBlockEntity(base.offset(FURNACE)) instanceof FurnaceBlockEntity furnace) recordDrying(server, essayIndex(furnace.getItem(2)));
        if (level.getGameTime() >= nextHymn) {
            boolean open = data.state(ID).getBoolean("RoofOpened");
            nextHymn = level.getGameTime() + (open ? 160 : 200);
            level.playSound(null, base.offset(ProofrockTown.HYMN.getX(), open ? -1 : ProofrockTown.HYMN.getY(), ProofrockTown.HYMN.getZ()), DrownedTownRegistry.HYMN.get(), SoundSource.AMBIENT,
                    open ? 1.0F : .45F, open ? 1 : .8F);
        }
    }

    public static boolean reset(MinecraftServer server) {
        BlockPos base = base(server); ServerLevel level = server.getLevel(HouseDimensions.OUTSIDE);
        if (base == null || level == null) return false;
        for (Entity actor : level.getEntitiesOfClass(Entity.class, bounds(base), e -> e instanceof LakeWitchEntity || e instanceof LakeCongregantEntity)) actor.discard();
        LabyrinthData data = LabyrinthData.get(server); data.setState(ID, new CompoundTag()); data.setCompleted(ID, false);
        build(server, level, base); clearAll(); return true;
    }
    public static List<String> describe(MinecraftServer server) {
        LabyrinthData data = LabyrinthData.get(server); CompoundTag state = data.state(ID);
        return List.of("Drowned Town: visit " + state.getInt("Visit") + "/2; essays dried " + Integer.bitCount(state.getInt("DryMask") & 7)
                + "/3; church " + (state.getBoolean("ChurchUnlocked") ? "unlocked" : "locked")
                + "; roof " + (state.getBoolean("RoofOpened") ? "open; hymn escaped" : "closed") + "; finished " + data.isCompleted(ID) + ".");
    }
    public static void clearAll() { OPEN_FURNACES.clear(); nextHymn = 0; }
}
