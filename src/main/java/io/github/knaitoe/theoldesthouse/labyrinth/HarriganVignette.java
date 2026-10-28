package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseCalendar;
import io.github.knaitoe.theoldesthouse.house.HouseConfig;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.LecternMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * Mr Harrigan's Phone: a three-visit vignette followed by a persistent artifact. On the first visit he is alive in his
 * chair and the reader reads to him; on the second he is dead in it, and his phone lies on the table beside him; on the
 * third the door opens on the funeral home built in the study's place, and he lies in his casket in its chapel (0.4.105).
 *
 * The vignette never reaches into another vignette. Everything the phone does
 * after the funeral happens outside the House dimension, and targeting while
 * the caller is inside the House is rejected as dead air.
 */
public final class HarriganVignette {
    public static final String ID = "harrigan";
    public static final int FINAL_VISIT = 3;
    /** Mr. Harrigan is one scene actor (0.4.105), never an armour stand: alive, dead in his chair, or in his casket. */
    public static final int ALIVE_PHASE = 0, DEAD_PHASE = 1, CASKET_PHASE = 2;
    private static final String ACTOR = "Harrigan0505";
    public static final double KILL_RADIUS = 64.0D;
    private static final int NIGHT_START = 13000;
    private static final int NIGHT_END = 23000;
    private static final String PROP_TAG = TheOldestHouse.MOD_ID + "_harrigan_prop";
    private static final String GHOST_TAG = TheOldestHouse.MOD_ID + "_harrigan_ghost";
    private static final String OWNER = "HarriganOwner";
    private static final String GHOST_OWNER = "HarriganGhostOwner";
    private static final String GHOST_TARGET = "HarriganGhostTarget";
    private static final String GHOST_MODE = "HarriganGhostMode";

    public static final BlockPos CHAIR = new BlockPos(0, 0, -9);
    public static final BlockPos LECTERN = new BlockPos(-3, 0, -7);
    public static final BlockPos READER_CHAIR = new BlockPos(-3, 0, -5);
    private static final java.util.Set<UUID> READERS = new java.util.HashSet<>();
    public static final BlockPos SIDE_TABLE = new BlockPos(3, 0, -8);
    public static final BlockPos FUNERAL_DOOR = new BlockPos(0, 0, -13);
    public static final BlockPos CASKET = new BlockPos(0, 0, -20);

    private static final String[] SMALL_TALK = {
            "Mr. Harrigan: The book is on the lectern. Read me a little, would you?",
            "Mr. Harrigan: Most people read too quickly. They are afraid of silence.",
            "Mr. Harrigan: Money is useful because everybody agrees to pretend it means the same thing.",
            "Mr. Harrigan: You learn a person by what they skip.",
            "Mr. Harrigan: Take your time. We have nowhere else to be."
    };
    private static final String[] PAGE_LINES = {
            "Mr. Harrigan: She kept going. Most people would have turned back.",
            "Mr. Harrigan: A light in a window can look like an invitation.",
            "Mr. Harrigan: Roads are promises. Mm. Read on.",
            "Mr. Harrigan: Let's hear what he tells her. This is the last page."
    };
    private static final List<String> READING_PAGES = List.of(
            "The road narrowed after the mill, though the map showed no bend. Mara walked on because turning back would have meant admitting the map was right.",
            "At dusk she found a house with one lamp burning. The old man inside asked no questions. He only moved another chair close to the fire.",
            "He said roads were promises made by people who expected the land to cooperate. Mara laughed until she saw the mud on his boots was red.",
            "In the morning the road was behind the house. There was no road in front of it. The old man was already awake, reading yesterday's newspaper.",
            "Mara asked how long he had lived there. He folded the paper carefully and said, 'Long enough to stop asking the house.'"
    );
    private static final String[] ACK = {
            "delivered",
            "i heard you",
            "yes",
            "done"
    };
    private static final String[] DISTORTED = {
            "d_liv_red",
            "i h...rd y_u",
            "y s",
            "do_e"
    };

    private static final Map<UUID, Long> NEXT_SPEECH = new HashMap<>();
    /** Caller -> the one active post-vignette Harrigan entity. */
    private static final Map<UUID, UUID> ACTIVE_GHOSTS = new HashMap<>();

    private HarriganVignette() {
    }

    // ---------------------------------------------------------------------
    // Room and visits

    public static void build(MinecraftServer server, ServerLevel level, BlockPos base) {
        buildStudy(level, base);
        CompoundTag fresh = LabyrinthData.get(server).state(ID);
        fresh.putBoolean("FuneralHome0505", false);
        LabyrinthData.get(server).setState(ID, fresh);
        stage(level, base, LabyrinthData.get(server), visitFor(LabyrinthData.get(server)));
    }

    /** His study, with the sealed room behind it; also what the funeral home goes back to if the visits ever start over. */
    private static void buildStudy(ServerLevel level, BlockPos base) {
        int flags = LabyrinthBuilder.flags();
        BlockState wall = Blocks.DARK_OAK_PLANKS.defaultBlockState();
        BlockState floor = Blocks.SPRUCE_PLANKS.defaultBlockState();
        BlockState ceiling = Blocks.DEEPSLATE_TILES.defaultBlockState();
        LabyrinthBuilder.room(level, base, -7, 7, 5, -24, 0, wall, floor, ceiling);

        // The study's back wall. There is no doorway in it: the funeral is somewhere else, on the third visit.
        for (int x = -7; x <= 7; x++) {
            for (int y = 0; y <= 5; y++) {
                level.setBlock(base.offset(x, y, -13), wall, flags);
            }
        }

        // Study shelving and furniture.
        for (int z = -11; z <= -3; z++) {
            level.setBlock(base.offset(-6, 0, z), Blocks.BOOKSHELF.defaultBlockState(), flags);
            level.setBlock(base.offset(-6, 1, z), Blocks.BOOKSHELF.defaultBlockState(), flags);
        }
        level.setBlock(base.offset(CHAIR), LabyrinthBuilder.stairs(Blocks.DARK_OAK_STAIRS, Direction.SOUTH), flags);
        level.setBlock(base.offset(READER_CHAIR), LabyrinthBuilder.stairs(Blocks.DARK_OAK_STAIRS, Direction.SOUTH), flags);
        BlockState table = Blocks.DARK_OAK_SLAB.defaultBlockState().setValue(net.minecraft.world.level.block.SlabBlock.TYPE,
                net.minecraft.world.level.block.state.properties.SlabType.TOP);
        level.setBlock(base.offset(SIDE_TABLE), table, flags);
        level.setBlock(base.offset(SIDE_TABLE).east(), table, flags);
        level.setBlock(base.offset(SIDE_TABLE).south(), table, flags);
        level.setBlock(base.offset(SIDE_TABLE).east().south(), table, flags);
        level.setBlock(base.offset(LECTERN), Blocks.LECTERN.defaultBlockState()
                .setValue(LecternBlock.FACING, Direction.SOUTH)
                .setValue(LecternBlock.HAS_BOOK, true), flags);
        if (level.getBlockEntity(base.offset(LECTERN)) instanceof LecternBlockEntity lectern) {
            lectern.setBook(readingBook());
        }

        HarriganFinish.build(level,base);

        LabyrinthBuilder.entrance(level, base, wall, floor, ceiling);
        LabyrinthBuilder.doors(level,base,LabyrinthPlace.HARRIGAN);
    }

    public static void onArrive(ServerPlayer player, LabyrinthPlace place) {
        if (place != LabyrinthPlace.HARRIGAN) {
            return;
        }
        BlockPos occupiedBase = base(player.server);
        ServerLevel occupiedLevel = player.server.getLevel(HouseDimensions.INTERIOR);
        if (occupiedBase != null && occupiedLevel != null && occupiedLevel.getEntitiesOfClass(ServerPlayer.class, placeBounds(occupiedBase))
                .stream().anyMatch(other -> !other.getUUID().equals(player.getUUID()))) return;
        LabyrinthData data = LabyrinthData.get(player.server);
        CompoundTag state = data.state(ID);
        int visit = state.getInt("Visit");
        if (visit < 1) {
            visit = 1;
            state.putInt("Visit", visit);
            state.putBoolean("BeatDone", false);
            data.setState(ID, state);
        } else if (state.getBoolean("BeatDone") && visit < FINAL_VISIT) {
            visit++;
            state.putInt("Visit", visit);
            state.putBoolean("BeatDone", false);
            state.putBoolean("TicketVisible", false);
            data.setState(ID, state);
        }
        BlockPos base = base(player.server);
        ServerLevel interior = player.server.getLevel(HouseDimensions.INTERIOR);
        if (base != null && interior != null) {
            boolean someoneElseIsAlreadyHere = interior.getEntitiesOfClass(ServerPlayer.class, placeBounds(base))
                    .stream().anyMatch(other -> !other.getUUID().equals(player.getUUID()));
            if (!someoneElseIsAlreadyHere) {
                stage(interior, base, data, visitFor(data));
            }
        }
        NEXT_SPEECH.put(player.getUUID(), player.serverLevel().getGameTime() + 80L);
        READERS.remove(player.getUUID());
    }

    public static void onDepart(ServerPlayer player) {
        if (!player.serverLevel().dimension().equals(HouseDimensions.INTERIOR)) {
            return;
        }
        BlockPos origin = HouseSavedData.get(player.server).houseOrigin();
        if (origin == null || LabyrinthPlaces.placeAt(origin, player.blockPosition()) != LabyrinthPlace.HARRIGAN) {
            return;
        }
        READERS.remove(player.getUUID());
        NEXT_SPEECH.remove(player.getUUID());
        LabyrinthData data = LabyrinthData.get(player.server);
        if (visitFor(data) == FINAL_VISIT && hasItem(player, LabyrinthRegistry.HARRIGANS_PHONE.get())) {
            // Leaving his funeral with his phone keeps it: that closes the story but does not connect the player's phone.
            // Carried out of the study on the second visit, it is only on its way to the funeral.
            data.setCompleted(ID, true);
            WitnessAccount.resolve(player,WitnessAccount.Story.HARRIGAN,"kept_phone");
            CompoundTag p = playerState(data, player.getUUID());
            p.putBoolean("KeptHarriganPhone", true);
            p.putBoolean("Contact", false);
            savePlayerState(data, player.getUUID(), p);
            player.displayClientMessage(Component.literal("The phone in your pocket feels heavier outside the room.")
                    .withStyle(ChatFormatting.DARK_GRAY), false);
            player.displayClientMessage(Component.literal("You kept his phone as a keepsake. Your own still keeps time, but his number has no connection.")
                    .withStyle(ChatFormatting.GRAY), false);
        }
    }

    private static int visitFor(LabyrinthData data) {
        return data.isCompleted(ID) ? FINAL_VISIT : Math.max(1, data.state(ID).getInt("Visit"));
    }

    private static void stage(ServerLevel level, BlockPos base, LabyrinthData data, int visit) {
        // His original actor and prop UUIDs survive visits and entity-section loading.
        if (visit >= FINAL_VISIT) {
            funeralHome(level, base, data, true);
            var harrigan = harrigan(level, base, CASKET_PHASE);
            if (harrigan != null) pose(harrigan, base, CASKET_PHASE);
            HarriganWake.ensureCast(level, base, true);
            return;
        }
        funeralHome(level, base, data, false);
        int phase = visit == 1 && !data.isCompleted(ID) ? ALIVE_PHASE : DEAD_PHASE;
        var harrigan = harrigan(level, base, phase);
        if (harrigan != null) pose(harrigan, base, phase);
        if (visit == 1 && !data.isCompleted(ID)) {
            placeProp(level, base.offset(SIDE_TABLE).above(), Direction.UP, LabyrinthRegistry.PHONE.get().getDefaultInstance(), "player_phone");
            placeProp(level, base.offset(SIDE_TABLE).east().above(), Direction.UP, LabyrinthRegistry.HARRIGANS_PHONE.get().getDefaultInstance(), "harrigan_phone");
            if (data.state(ID).getBoolean("TicketVisible")) {
                placeProp(level, base.offset(SIDE_TABLE).south().above(), Direction.UP,
                        LabyrinthRegistry.SCRATCH_TICKET.get().getDefaultInstance(), "ticket");
            }
        } else {
            placeProp(level, base.offset(SIDE_TABLE).east().above(), Direction.UP,
                    LabyrinthRegistry.HARRIGANS_PHONE.get().getDefaultInstance(), "harrigan_phone");
        }
    }

    /** Where the guest book lies in the funeral home's foyer. */
    public static final BlockPos GUEST_BOOK = new BlockPos(-4, 0, -2);
    /** The funeral home's own sign, facing whoever comes in. */
    public static final BlockPos HOME_SIGN = new BlockPos(4, 0, -2);

    /**
     * On the third visit the door no longer opens on his study: it opens on Peabody &amp; Sons, a funeral home, built whole
     * in the study's place (0.4.105) and kept from then on. If the visits ever start over, the study comes back. Only the
     * study's own plain walls are refaced; its windows, fireplace and ceiling stay.
     */
    private static void funeralHome(ServerLevel level, BlockPos base, LabyrinthData data, boolean funeral) {
        boolean built = data.state(ID).getBoolean("FuneralHome0505");
        if (built != funeral) {
            if (funeral) buildFuneralHome(level, base);
            else buildStudy(level, base);
            OnwardDoors.restore(level, base, LabyrinthPlace.HARRIGAN);
            CompoundTag state = data.state(ID);
            state.putBoolean("FuneralHome0505", funeral);
            data.setState(ID, state);
        }
        if (!funeral) {
            // The study's back wall stays shut on the first two visits.
            for (int x = -1; x <= 1; x++) for (int y = 0; y <= 3; y++)
                level.setBlock(base.offset(x, y, FUNERAL_DOOR.getZ()), Blocks.DARK_OAK_PLANKS.defaultBlockState(), LabyrinthBuilder.flags());
        }
    }

    private static boolean plainWall(BlockState state) {
        return state.is(Blocks.DARK_OAK_PLANKS) || state.is(Blocks.STRIPPED_DARK_OAK_LOG) || state.is(Blocks.STRIPPED_DARK_OAK_WOOD)
                || state.is(Blocks.DARK_OAK_LOG) || state.is(Blocks.GREEN_TERRACOTTA) || state.is(Blocks.SPRUCE_PLANKS)
                || state.is(LabyrinthRegistry.HARRIGAN_FINISH.get()) && state.getValue(HarriganFinishBlock.KIND) != HarriganFinishBlock.Kind.CASKET;
    }

    private static void buildFuneralHome(ServerLevel level, BlockPos base) {
        int flags = LabyrinthBuilder.flags();
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState panel = HarriganFinishBlock.of(HarriganFinishBlock.Kind.PANEL);
        BlockState paper = HarriganFinishBlock.of(HarriganFinishBlock.Kind.WALLPAPER);
        // Nothing of the study is left inside: its furniture, its props, the wall between its two rooms, its lamps.
        for (var frame : level.getEntitiesOfClass(ItemFrame.class, placeBounds(base), f -> f.getTags().contains(PROP_TAG))) frame.discard();
        for (int x = -7; x <= 7; x++) for (int z = -24; z <= 0; z++) {
            for (int y = 0; y <= 4; y++) {
                BlockPos at = base.offset(x, y, z);
                if (!level.getBlockState(at).isAir()) level.setBlock(at, air, flags);
            }
            BlockPos chain = base.offset(x, 5, z);
            if (level.getBlockState(chain).is(Blocks.CHAIN)) level.setBlock(chain, air, flags);
            // A boarded floor in the foyer; the funeral carpet through the chapel.
            level.setBlock(base.offset(x, -1, z), z >= -4 ? Blocks.DARK_OAK_PLANKS.defaultBlockState() : HarriganFinishBlock.of(HarriganFinishBlock.Kind.CARPET), flags);
        }
        // Panelled to the rail, papered above it, wherever the study had plain wall. Windows and the hearth are left.
        for (int y = 0; y <= 4; y++) {
            BlockState face = y <= 1 ? panel : paper;
            for (int z = -24; z <= 0; z++) for (int side : new int[]{-8, 8}) {
                BlockPos at = base.offset(side, y, z);
                if (plainWall(level.getBlockState(at))) level.setBlock(at, face, flags);
            }
            for (int x = -7; x <= 7; x++) {
                BlockPos back = base.offset(x, y, -25);
                if (plainWall(level.getBlockState(back))) level.setBlock(back, face, flags);
                BlockPos front = base.offset(x, y, 1);
                if (plainWall(level.getBlockState(front)) && level.getBlockEntity(front) == null) level.setBlock(front, face, flags);
            }
        }

        // The foyer: the guest book on its stand, the home's sign, flowers, a bench either side.
        level.setBlock(base.offset(GUEST_BOOK), Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING, Direction.EAST).setValue(LecternBlock.HAS_BOOK, true), flags);
        if (level.getBlockEntity(base.offset(GUEST_BOOK)) instanceof LecternBlockEntity book) book.setBook(guestBook());
        level.setBlock(base.offset(HOME_SIGN), HarriganFinishBlock.of(HarriganFinishBlock.Kind.PLAQUE), flags);
        for (int side : new int[]{-1, 1}) {
            pedestal(level, base.offset(side * 6, 0, -1), side < 0 ? Blocks.POTTED_WHITE_TULIP : Blocks.POTTED_LILY_OF_THE_VALLEY);
            for (int z = -4; z <= -3; z++) level.setBlock(base.offset(side * 7, 0, z), LabyrinthBuilder.stairs(Blocks.DARK_OAK_STAIRS, side < 0 ? Direction.WEST : Direction.EAST), flags);
        }
        // Between foyer and chapel, a panelled partition with a wide opening.
        for (int x = -7; x <= 7; x++) for (int y = 0; y <= 4; y++) {
            boolean opening = Math.abs(x) <= 2 && y <= 3;
            if (!opening) level.setBlock(base.offset(x, y, -5), y <= 1 || Math.abs(x) <= 2 ? panel : paper, flags);
        }

        // The chapel: an aisle runner, five rows of pews either side of it, all facing the casket.
        for (int x = -1; x <= 1; x++) for (int z = -17; z <= -6; z++) level.setBlock(base.offset(x, 0, z), Blocks.RED_CARPET.defaultBlockState(), flags);
        for (int row = 0; row < 5; row++) {
            int z = -8 - row * 2;
            for (int x = 2; x <= 6; x++) for (int side : new int[]{-1, 1})
                level.setBlock(base.offset(side * x, 0, z), LabyrinthBuilder.stairs(Blocks.DARK_OAK_STAIRS, Direction.SOUTH), flags);
        }
        // At the front he lies in his casket, flowers and candles either side.
        level.setBlock(base.offset(CASKET), HarriganFinishBlock.of(HarriganFinishBlock.Kind.CASKET), flags);
        for (int side : new int[]{-1, 1}) {
            pedestal(level, base.offset(side * 3, 0, -20), side < 0 ? Blocks.POTTED_WHITE_TULIP : Blocks.POTTED_OXEYE_DAISY);
            pedestal(level, base.offset(side * 5, 0, -23), Blocks.POTTED_LILY_OF_THE_VALLEY);
            for (int z : new int[]{-18, -22}) {
                level.setBlock(base.offset(side * 2, 0, z), Blocks.DARK_OAK_FENCE.defaultBlockState(), flags);
                level.setBlock(base.offset(side * 2, 1, z), LabyrinthBuilder.candle(3, true), flags);
            }
        }
        // Lamps on chains down the middle and over the side aisles.
        for (BlockPos lamp : List.of(new BlockPos(0, 4, -2), new BlockPos(0, 4, -9), new BlockPos(0, 4, -15), new BlockPos(-5, 4, -12),
                new BlockPos(5, 4, -12), new BlockPos(-5, 4, -20), new BlockPos(5, 4, -20))) {
            level.setBlock(base.offset(lamp).above(), Blocks.CHAIN.defaultBlockState(), flags);
            level.setBlock(base.offset(lamp), Blocks.LANTERN.defaultBlockState().setValue(net.minecraft.world.level.block.LanternBlock.HANGING, true), flags);
        }
    }

    /** A flower arrangement on a short white stand. */
    private static void pedestal(ServerLevel level, BlockPos at, net.minecraft.world.level.block.Block pot) {
        level.setBlock(at, Blocks.QUARTZ_PILLAR.defaultBlockState(), LabyrinthBuilder.flags());
        level.setBlock(at.above(), pot.defaultBlockState(), LabyrinthBuilder.flags());
    }

    private static ItemStack guestBook() {
        return HouseWriting.book("In memory", "The mourners", HouseWriting.WritingStyle.PLAIN, List.of(
                "Please sign.\n\nHe hired me to read the stock pages to him the winter his eyes went. He tipped by the page and never once said thank you. I miss it already.\n\nR. Dean",
                "He paid for the new roof on the library and made them promise not to put his name on it.\n\nThe church\n\nYou kept him company at the end. That was more than most of us managed.\n\nM."));
    }

    /** Replace only the unchanged authored old sign, in a loaded, unseen and empty entrance. */
    private static void funeralPlaque(ServerLevel level, BlockPos base, LabyrinthData data) {
        BlockPos at = base.offset(HOME_SIGN);
        if (!data.state(ID).getBoolean("FuneralHome0505") || !level.hasChunkAt(at)
                || !level.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(at.getX() >> 4, at.getZ() >> 4))
                || !(level.getBlockEntity(at) instanceof net.minecraft.world.level.block.entity.SignBlockEntity sign)) return;
        String[] words = {"PEABODY & SONS", "Funeral Home", "Visitation for", "Mr. Harrigan"};
        for (int line = 0; line < words.length; line++)
            if (!sign.getFrontText().getMessage(line, false).getString().equals(words[line])
                    || !sign.getBackText().getMessage(line, false).getString().equals(words[line])) return;
        if (io.github.knaitoe.theoldesthouse.house.HouseWatchers.isWatched(level, Vec3.atCenterOf(at))
                || !level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,
                    new AABB(at).expandTowards(0, .8, 0), net.minecraft.world.entity.LivingEntity::isAlive).isEmpty()) return;
        level.setBlock(at, HarriganFinishBlock.of(HarriganFinishBlock.Kind.PLAQUE), LabyrinthBuilder.flags());
    }

    private static ItemStack readingBook() {
        return HouseWriting.book(
                "The Western Road",
                "A. Vale",
                HouseWriting.WritingStyle.PLAIN,
                READING_PAGES);
    }

    /** The phase he belongs in for a visit. */
    private static int phaseFor(LabyrinthData data, int visit) {
        return visit >= FINAL_VISIT ? CASKET_PHASE : visit == 1 && !data.isCompleted(ID) ? ALIVE_PHASE : DEAD_PHASE;
    }

    /**
     * His one original actor: created once (in the given phase, or not at all when it is negative), found again by its
     * saved UUID, and never replaced while that body exists.
     */
    @Nullable
    public static LiteraryActor harrigan(ServerLevel level, BlockPos base, int createAs) {
        if (base == null) return null;
        var data = LabyrinthData.get(level.getServer());
        var state = data.state(ID);
        if (state.hasUUID(ACTOR)) {
            if (level.getEntity(state.getUUID(ACTOR)) instanceof LiteraryActor found) {
                // Arrival can run before this original's entity section has loaded. Its saved
                // phase/position then belong to the previous visit, even though the story advanced.
                if (createAs >= 0 && (found.role() != LiteraryActor.HARRIGAN || found.phase() != createAs
                        || found.position().distanceToSqr(bodyPosition(base, createAs)) > .0001D)) {
                    pose(found, base, createAs);
                }
                return found;
            }
            // An unloaded original is not a missing one: wait until both his places are loaded to know.
            for (BlockPos place : List.of(base.offset(CHAIR), base.offset(CASKET)))
                if (!level.hasChunkAt(place) || !level.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(place.getX() >> 4, place.getZ() >> 4))) return null;
        }
        if (createAs < 0) return null;
        BlockPos chair = base.offset(CHAIR);
        if (!level.hasChunkAt(chair) || !level.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(chair.getX() >> 4, chair.getZ() >> 4))) return null;
        LiteraryActor actor = LiteraryRegistry.ACTOR.get().create(level);
        if (actor == null) return null;
        actor.bind(LabyrinthPlace.HARRIGAN, null);
        actor.setNoGravity(true);
        actor.setInvulnerable(true);
        actor.setSilent(true);
        pose(actor, base, createAs);
        if (!level.addFreshEntity(actor)) return null;
        state = data.state(ID);
        state.putUUID(ACTOR, actor.getUUID());
        data.setState(ID, state);
        return actor;
    }

    /** Seated in his chair facing away from the door, alive or dead, or laid out in the casket. */
    private static void pose(LiteraryActor actor, BlockPos base, int phase) {
        actor.appearance(LiteraryActor.HARRIGAN, phase);
        Vec3 at = bodyPosition(base, phase);
        actor.moveTo(at.x, at.y, at.z, 180.0F, 0.0F);
        actor.setYHeadRot(180.0F);
        actor.yBodyRot = actor.yBodyRotO = actor.yHeadRotO = actor.yRotO = 180.0F;
        actor.xRotO = 0.0F;
        actor.setDeltaMovement(Vec3.ZERO);
        actor.setNoAi(true);
        actor.setNoGravity(true);
        actor.setSilent(true);
        actor.getNavigation().stop();
        actor.setTarget(null);
    }

    private static Vec3 bodyPosition(BlockPos base, int phase) {
        if (phase == CASKET_PHASE) {
            BlockPos casket = base.offset(CASKET);
            return new Vec3(casket.getX() + .5D, casket.getY() + .9D, casket.getZ() + .5D);
        }
        // The model's hip is thirteen pixels above its feet: just above the stair's half-height seat, ahead of its back.
        BlockPos chair = base.offset(CHAIR);
        return new Vec3(chair.getX() + .5D, chair.getY() + .55D - 13.0D / 16.0D, chair.getZ() + .3D);
    }

    /** Whether his own body is in this chair, so nobody else can sit in it through him. */
    public static boolean occupiesChair(ServerLevel level, BlockPos chair) {
        return !level.getEntitiesOfClass(LiteraryActor.class, new AABB(chair).inflate(0, 1, 0),
                actor -> actor.role() == LiteraryActor.HARRIGAN && actor.phase() != CASKET_PHASE).isEmpty();
    }

    /** Right-clicking him or a mourner. Alive he answers; dead he does not; the mourners each say their piece. */
    public static void talk(ServerPlayer player, LiteraryActor actor) {
        if (!player.isAlive() || player.isSpectator() || !inVignette(player) || player.distanceToSqr(actor) > 36) return;
        if (actor.role() == LiteraryActor.HARRIGAN_MOURNER) { HarriganWake.talk(player, actor); return; }
        LabyrinthData data = LabyrinthData.get(player.server);
        int phase = phaseFor(data, visitFor(data));
        // The saved visit, not a just-loaded actor's stale metadata, decides whether he can answer.
        if (actor.role() != LiteraryActor.HARRIGAN || phase != ALIVE_PHASE) {
            if (actor.role() == LiteraryActor.HARRIGAN) harrigan(player.serverLevel(), base(player.server), phase);
            return;
        }
        long now = player.serverLevel().getGameTime();
        if (player.getPersistentData().getLong("HarriganTalk0505") > now) return;
        player.getPersistentData().putLong("HarriganTalk0505", now + 40);
        CompoundTag state = data.state(ID);
        String line;
        if (readingFinished(state)) line = "Mr. Harrigan: That's enough for today. The ticket is yours. So is the phone.";
        else if (!state.getBoolean("ReadingStarted")) line = SMALL_TALK[0];
        else {
            int said = state.getInt("SmallTalk0505");
            line = SMALL_TALK[1 + Math.floorMod(said, SMALL_TALK.length - 1)];
            state.putInt("SmallTalk0505", said + 1);
            data.setState(ID, state);
        }
        player.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY), false);
    }

    private static void equipSuit(LivingEntity entity) {
        ItemStack chest = new ItemStack(Items.LEATHER_CHESTPLATE);
        ItemStack legs = new ItemStack(Items.LEATHER_LEGGINGS);
        ItemStack boots = new ItemStack(Items.LEATHER_BOOTS);
        chest.set(DataComponents.DYED_COLOR, new DyedItemColor(0x151419, false));
        legs.set(DataComponents.DYED_COLOR, new DyedItemColor(0x101014, false));
        boots.set(DataComponents.DYED_COLOR, new DyedItemColor(0x211915, false));
        entity.setItemSlot(EquipmentSlot.CHEST, chest);
        entity.setItemSlot(EquipmentSlot.LEGS, legs);
        entity.setItemSlot(EquipmentSlot.FEET, boots);
    }

    private static ItemFrame originalProp(ServerLevel level,String role){
        var data=LabyrinthData.get(level.getServer());var state=data.state(ID);String key="OriginalProp0490_"+role;
        if(state.hasUUID(key))return level.getEntity(state.getUUID(key)) instanceof ItemFrame frame?frame:null;
        var originals=level.getEntitiesOfClass(ItemFrame.class,placeBounds(base(level.getServer())),e->e.getTags().contains(PROP_TAG+"_"+role)||role.equals("harrigan_phone")&&e.getTags().contains(PROP_TAG+"_harrigan_phone_display"));
        if(originals.isEmpty())return null;var frame=originals.getFirst();frame.addTag(PROP_TAG+"_"+role);state.putUUID(key,frame.getUUID());data.setState(ID,state);return frame;
    }
    private static void placeProp(ServerLevel level,BlockPos pos,Direction direction,ItemStack item,String role){
        var data=LabyrinthData.get(level.getServer());var state=data.state(ID);String key="OriginalProp0490_"+role;
        if(state.getBoolean("PropTaken0490_"+role)||originalProp(level,role)!=null||data.state(ID).hasUUID(key))return;
        if(!level.hasChunkAt(pos)||!level.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(pos.getX()>>4,pos.getZ()>>4)))return;
        var frame=new ItemFrame(level,pos,direction);frame.addTag(PROP_TAG);frame.addTag(PROP_TAG+"_"+role);frame.setItem(item,false);frame.setInvulnerable(true);
        if(level.addFreshEntity(frame)){state=data.state(ID);state.putUUID(key,frame.getUUID());data.setState(ID,state);}
    }
    private static void propTaken(LabyrinthData data,String role){var state=data.state(ID);state.putBoolean("PropTaken0490_"+role,true);data.setState(ID,state);}
    private static void ensureProps(ServerLevel level,BlockPos b,int visit){var state=LabyrinthData.get(level.getServer()).state(ID);if(visit==1&&!state.getBoolean("BeatDone")){placeProp(level,b.offset(SIDE_TABLE).above(),Direction.UP,LabyrinthRegistry.PHONE.get().getDefaultInstance(),"player_phone");if(state.getBoolean("TicketVisible"))placeProp(level,b.offset(SIDE_TABLE).south().above(),Direction.UP,LabyrinthRegistry.SCRATCH_TICKET.get().getDefaultInstance(),"ticket");}if(visit<=2)placeProp(level,b.offset(SIDE_TABLE).east().above(),Direction.UP,LabyrinthRegistry.HARRIGANS_PHONE.get().getDefaultInstance(),"harrigan_phone");}

    // ---------------------------------------------------------------------
    // Reading and first visit

    public static void onContainerClose(PlayerContainerEvent.Close event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !(event.getContainer() instanceof LecternMenu lectern)) {
            return;
        }
        if(!player.isAlive()||player.isSpectator()){READERS.remove(player.getUUID());return;}
        if (!inVignette(player) || visitFor(LabyrinthData.get(player.server)) != 1) {
            return;
        }
        if (!READERS.remove(player.getUUID())) return;
        LabyrinthData data = LabyrinthData.get(player.server);
        if (readingFinished(data.state(ID))) return;
        observeReadingPage(player, data, lectern.getPage());
        if (data.state(ID).getInt("ReadingPage") < READING_PAGES.size() - 1) return;
        CompoundTag state = data.state(ID);
        state.putBoolean("ReadingFinished", true);
        if (!state.getBoolean("TicketVisible")) {
            state.putBoolean("TicketVisible", true);
            data.setState(ID, state);
            BlockPos base = base(player.server);
            if (base != null) {
                placeProp(player.serverLevel(), base.offset(SIDE_TABLE).south().above(), Direction.UP,
                        LabyrinthRegistry.SCRATCH_TICKET.get().getDefaultInstance(), "ticket");
            }
        }
        NEXT_SPEECH.remove(player.getUUID());
        player.displayClientMessage(Component.literal("Mr. Harrigan: That's enough for today. Scratch the ticket on the table; the spare phone is yours.")
                .withStyle(ChatFormatting.GRAY), false);
    }

    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (handleEntityInteract(event.getEntity(), event.getTarget())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (handleEntityInteract(event.getEntity(), event.getTarget())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    private static boolean handleEntityInteract(Player playerEntity, Entity target) {
        if (!(playerEntity instanceof ServerPlayer player) || !(target instanceof ItemFrame frame)
                || !frame.getTags().contains(PROP_TAG)) {
            return false;
        }
        if (frame.getTags().contains(PROP_TAG + "_ticket")) {
            scratchTicket(player, frame);
        } else if (frame.getTags().contains(PROP_TAG + "_harrigan_phone")) {
            takeHarriganPhone(player, frame);
        }
        return true;
    }

    private static void scratchTicket(ServerPlayer player, ItemFrame frame) {
        LabyrinthData data = LabyrinthData.get(player.server);
        CompoundTag state = data.state(ID);
        if (!player.isAlive()||player.isSpectator()||!inVignette(player) || visitFor(data) != 1 || !state.getBoolean("TicketVisible")) {
            return;
        }
        propTaken(data,"ticket");frame.discard();
        ItemStack emeralds = new ItemStack(Items.EMERALD, 12);
        if (!player.getInventory().add(emeralds)) {
            player.drop(emeralds, false);
        }
        if (!hasOwnedPhone(player)) {
            var spare=originalProp(player.serverLevel(),"player_phone");ItemStack phone=spare==null?ItemStack.EMPTY:spare.getItem().copy();
            if(!phone.isEmpty()){CustomData.update(DataComponents.CUSTOM_DATA,phone,tag->tag.putUUID(OWNER,player.getUUID()));spare.setItem(ItemStack.EMPTY,false);propTaken(data,"player_phone");spare.discard();var personal=playerState(data,player.getUUID());personal.putBoolean("PhoneGiven0490",true);savePlayerState(data,player.getUUID(),personal);}
            if (!player.getInventory().add(phone)) {
                player.drop(phone, false);
            }
        }
        // Transfer the exact original displayed spare, retaining its components.
        state=data.state(ID);state.putBoolean("BeatDone", true);
        state.putBoolean("TicketVisible", false);
        data.setState(ID, state);
        player.serverLevel().playSound(null, frame.blockPosition(), SoundEvents.BRUSH_GENERIC, SoundSource.PLAYERS, 0.8F, 1.25F);
        player.displayClientMessage(Component.literal("The silver coating curls away. Twelve emeralds.")
                .withStyle(ChatFormatting.GREEN), false);
        player.displayClientMessage(Component.literal("You take the spare phone. Its display keeps the time back home, even inside the House. Harrigan's number is saved, but there is no signal here.")
                .withStyle(ChatFormatting.GRAY), false);
    }

    private static void takeHarriganPhone(ServerPlayer player, ItemFrame frame) {
        LabyrinthData data = LabyrinthData.get(player.server);
        if (!player.isAlive()||player.isSpectator()||!inVignette(player) || visitFor(data) != 2 || hasItem(player, LabyrinthRegistry.HARRIGANS_PHONE.get())) {
            return;
        }
        ItemStack phone=frame.getItem().copy();if(!phone.is(LabyrinthRegistry.HARRIGANS_PHONE.get()))return;frame.setItem(ItemStack.EMPTY,false);propTaken(data,"harrigan_phone");frame.discard();
        // Taking his phone is the second visit's beat: the next time the room opens, it is for his funeral.
        CompoundTag beat=data.state(ID);beat.putBoolean("BeatDone",true);data.setState(ID,beat);
        if (!player.getInventory().add(phone)) {
            player.drop(phone, false);
        }
        player.displayClientMessage(Component.literal("His phone is cold.")
                .withStyle(ChatFormatting.DARK_GRAY), false);
        player.displayClientMessage(Component.literal("Keep his phone as a keepsake, or bring it to his funeral and lay it beside him. Your own phone may reach him if his goes with him.")
                .withStyle(ChatFormatting.GRAY), false);
        // 0.4.105: controls are the House's grey italic small print, never a character's line.
        player.displayClientMessage(Component.literal("To bury it, hold Mr. Harrigan's phone and right-click the casket.")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), false);
    }

    public static void onAttackEntity(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        Entity target = event.getTarget();
        if (target.getTags().contains(PROP_TAG)) {
            event.setCanceled(true);
            return;
        }
        if (target.getTags().contains(GHOST_TAG)) {
            event.setCanceled(true);
            Vec3 away = target.position().subtract(player.position());
            if (away.lengthSqr() < 0.001D) {
                away = new Vec3(0, 0, 1);
            }
            Vec3 push = away.normalize().scale(0.7D);
            target.push(push.x, 0.25D, push.z);
            target.hurtMarked = true;
        }
    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getLevel().isClientSide() && event.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND
                && event.getEntity() instanceof ServerPlayer reader && reader.isAlive() && !reader.isSpectator() && inVignette(reader)
                && event.getPos().equals(base(reader.server).offset(HOME_SIGN))
                && event.getLevel().getBlockState(event.getPos()).is(LabyrinthRegistry.HARRIGAN_FINISH.get())
                && event.getLevel().getBlockState(event.getPos()).getValue(HarriganFinishBlock.KIND) == HarriganFinishBlock.Kind.PLAQUE) {
            reader.displayClientMessage(Component.literal("Peabody & Sons Funeral Home. Visitation for Mr. Harrigan.").withStyle(ChatFormatting.GRAY), false);
            event.setCanceled(true);
            return;
        }
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof ServerPlayer reader
                && event.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND && inVignette(reader)
                && reader.isAlive()&&!reader.isSpectator()
                && event.getPos().equals(base(reader.server).offset(LECTERN))
                && visitFor(LabyrinthData.get(reader.server)) == 1
                && event.getLevel().getBlockEntity(event.getPos()) instanceof LecternBlockEntity lectern && lectern.hasBook()) {
            READERS.add(reader.getUUID());
        }
        if (!(event.getEntity() instanceof ServerPlayer player) || !player.isAlive()||player.isSpectator()||!inVignette(player)
                || visitFor(LabyrinthData.get(player.server)) != FINAL_VISIT
                || !isCasket(player.server, event.getPos())) {
            return;
        }
        ItemStack held = player.getItemInHand(event.getHand());
        if (!held.is(LabyrinthRegistry.HARRIGANS_PHONE.get())) {
            return;
        }
        held.shrink(1);
        LabyrinthData data = LabyrinthData.get(player.server);
        CompoundTag p = playerState(data, player.getUUID());
        p.putBoolean("Contact", true);
        p.putBoolean("KeptHarriganPhone", false);
        savePlayerState(data, player.getUUID(), p);
        data.setCompleted(ID, true);
        WitnessAccount.resolve(player,WitnessAccount.Story.HARRIGAN,"buried_phone");
        player.displayClientMessage(Component.literal("You tuck the phone beside his hand.")
                .withStyle(ChatFormatting.DARK_GRAY), false);
        explainCalling(player, data);
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    // ---------------------------------------------------------------------
    // The phone

    public static ItemStack phoneFor(ServerPlayer player) {
        ItemStack stack = LabyrinthRegistry.PHONE.get().getDefaultInstance();
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putUUID(OWNER, player.getUUID()));
        return stack;
    }

    public static boolean repairLegacyPhone(ServerPlayer player,ItemStack stack){
        if(!stack.is(LabyrinthRegistry.PHONE.get())||phoneOwner(stack)!=null||hasOwnedPhone(player))return false;var data=LabyrinthData.get(player.server);var own=playerState(data,player.getUUID());
        if(own.getBoolean("PhoneOwnerRepair0490")||!(own.getBoolean("Contact")||own.getBoolean("KeptHarriganPhone")||own.getBoolean("PhoneGiven0490")))return false;
        CustomData.update(DataComponents.CUSTOM_DATA,stack,t->t.putUUID(OWNER,player.getUUID()));own.putBoolean("PhoneOwnerRepair0490",true);savePlayerState(data,player.getUUID(),own);return true;
    }
    /** Using the phone opens it (0.4.105): its clock back home everywhere, and its one contact, him, to call or to text. */
    public static void beginPhone(ServerPlayer player, ItemStack stack) {
        repairLegacyPhone(player,stack);
        if (!isOwnedPhone(stack, player.getUUID())) {
            player.displayClientMessage(Component.literal("The phone has no number stored in it.")
                    .withStyle(ChatFormatting.DARK_GRAY), true);
            return;
        }
        LabyrinthData data = LabyrinthData.get(player.server);
        if (noService(player)) {
            player.displayClientMessage(Component.literal("No service. Calls cannot leave the House; the clock still keeps the time back home.")
                    .withStyle(ChatFormatting.DARK_GRAY), true);
        } else if (playerState(data, player.getUUID()).getBoolean("Contact")) {
            explainCalling(player, data);
        }
        sendPhone(player, true, null);
    }

    /** Whose turn a line in the thread is: the reader's, his, or the phone's own small print. */
    public static final int ME = 0, HIM = 1, NOTE = 2;
    private static final int THREAD = 40;
    static final String GREETING = "This is Harrigan. I don't pick up this phone. If you have a name for me, leave it after the tone.";

    /** Nothing leaves the House: not a call, not a text. The clock still works. */
    private static boolean noService(ServerPlayer player) {
        return HouseDimensions.isHouseDimension(player.level().dimension());
    }

    private static boolean usedToday(CompoundTag p, MinecraftServer server) {
        return p.contains("LastCallDay") && p.getLong("LastCallDay") == HouseCalendar.today(server);
    }

    /** One line of the thread with him, kept with the reader's phone record. */
    private static void log(ServerPlayer player, int who, String text) {
        LabyrinthData data = LabyrinthData.get(player.server);
        CompoundTag p = playerState(data, player.getUUID());
        var thread = p.getList("Thread0505", net.minecraft.nbt.Tag.TAG_COMPOUND);
        CompoundTag line = new CompoundTag();
        // Numbered, so the screen can tell a new line from an old one once the oldest start falling off.
        int number = p.getInt("ThreadSeq0505") + 1;
        p.putInt("ThreadSeq0505", number);
        line.putInt("N", number);
        line.putInt("Who", who);
        line.putString("Text", text);
        thread.add(line);
        while (thread.size() > THREAD) thread.remove(0);
        p.put("Thread0505", thread);
        savePlayerState(data, player.getUUID(), p);
    }

    /** What the phone's screen shows. */
    public static CompoundTag phoneState(ServerPlayer player, @Nullable CompoundTag call) {
        CompoundTag p = playerState(LabyrinthData.get(player.server), player.getUUID());
        CompoundTag state = new CompoundTag();
        state.putLong("DayTime", player.server.overworld().getDayTime());
        state.putBoolean("Service", !noService(player));
        state.putString("Line", p.getBoolean("Severed") ? "severed" : p.getBoolean("Contact") ? "connected"
                : p.getBoolean("KeptHarriganPhone") ? "kept" : "none");
        state.putBoolean("Today", usedToday(p, player.server));
        state.put("Thread", p.getList("Thread0505", net.minecraft.nbt.Tag.TAG_COMPOUND).copy());
        if (call != null) state.put("Call", call);
        return state;
    }

    private static void sendPhone(ServerPlayer player, boolean open, @Nullable CompoundTag call) {
        io.github.knaitoe.theoldesthouse.network.HousePackets.send(player,
                new io.github.knaitoe.theoldesthouse.network.HarriganPhonePayload(open, phoneState(player, call)));
    }

    /** From the phone's screen: a text, a call, or a name left on his voicemail. */
    public static void phoneAction(ServerPlayer player, int action, String text) {
        if (!player.isAlive() || player.isSpectator() || !hasOwnedPhone(player)) return;
        long now = player.serverLevel().getGameTime();
        if (player.getPersistentData().getLong("PhoneAction0505") > now) return;
        player.getPersistentData().putLong("PhoneAction0505", now + 10);
        switch (action) {
            case io.github.knaitoe.theoldesthouse.network.HarriganPhoneActionPayload.CALL -> phoneCall(player);
            case io.github.knaitoe.theoldesthouse.network.HarriganPhoneActionPayload.TEXT -> phoneText(player, text, false);
            case io.github.knaitoe.theoldesthouse.network.HarriganPhoneActionPayload.VOICEMAIL -> phoneText(player, text, true);
            default -> { }
        }
    }

    /**
     * A name sent to his number, by text or left on his voicemail. It is the call the phone's notes describe: once a day,
     * a hostile species dies near the caller's Overworld bed at the next dawn; a harmless creature or a named one brings
     * him instead. Nothing goes from inside the House, or to a number that is not connected.
     */
    public static void phoneText(ServerPlayer player, String raw, boolean voicemail) {
        String name = raw == null ? "" : raw.trim();
        if (name.isEmpty()) return;
        if (name.length() > 32) name = name.substring(0, 32);
        LabyrinthData data = LabyrinthData.get(player.server);
        log(player, ME, voicemail ? "Voicemail: " + name : name);
        CompoundTag p = playerState(data, player.getUUID());
        if (noService(player)) log(player, NOTE, "Not delivered. No service.");
        else if (p.getBoolean("Severed")) log(player, NOTE, "Not delivered.");
        else if (!p.getBoolean("Contact"))
            log(player, NOTE, hasItem(player, LabyrinthRegistry.HARRIGANS_PHONE.get()) ? "His phone buzzes in your pocket." : "Not delivered.");
        else if (usedToday(p, player.server)) log(player, NOTE, "No answer.");
        else if (p.hasUUID("PendingPlayer")) log(player, NOTE, "The last message is still waiting.");
        else placeCall(player, name);
        sendPhone(player, false, null);
    }

    /** A call to his number: it rings, and then whatever his number does now. */
    public static void phoneCall(ServerPlayer player) {
        CompoundTag p = playerState(LabyrinthData.get(player.server), player.getUUID());
        CompoundTag call = new CompoundTag();
        String result;
        if (noService(player)) result = "noservice";
        else if (p.getBoolean("Severed")) result = "disconnected";
        else if (!p.getBoolean("Contact") && hasItem(player, LabyrinthRegistry.HARRIGANS_PHONE.get())) {
            // His phone never went with him, so the call finds it where it is: on the caller.
            result = "pocket";
            player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(), PhoneSounds.HIS_RING.get(), SoundSource.PLAYERS, .9F, 1.0F);
        } else if (!p.getBoolean("Contact") || usedToday(p, player.server)) result = "unanswered";
        else {
            result = "voicemail";
            call.putString("Greeting", GREETING);
        }
        call.putString("Result", result);
        log(player, NOTE, switch (result) {
            case "voicemail" -> "Call · his voicemail";
            case "pocket" -> "Call · it rang in your pocket";
            case "disconnected" -> "Call · not in service";
            case "noservice" -> "Call · no service";
            default -> "Call · no answer";
        });
        sendPhone(player, false, call);
    }

    public enum TargetKind {
        HOSTILE,
        FRIENDLY,
        FORBIDDEN,
        UNKNOWN
    }

    public static TargetKind classify(EntityType<?> type, boolean customNamed, @Nullable Entity probe) {
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        if (key != null && TheOldestHouse.MOD_ID.equals(key.getNamespace())) {
            return TargetKind.FORBIDDEN;
        }
        if (type == EntityType.ENDER_DRAGON || type == EntityType.WITHER
                || type == EntityType.ELDER_GUARDIAN || type == EntityType.WARDEN) {
            return TargetKind.FORBIDDEN;
        }
        if (!(probe instanceof LivingEntity)) {
            return TargetKind.UNKNOWN;
        }
        if (customNamed) {
            return TargetKind.FRIENDLY;
        }
        return type.getCategory() == MobCategory.MONSTER ? TargetKind.HOSTILE : TargetKind.FRIENDLY;
    }

    private static void placeCall(ServerPlayer caller, String rawTarget) {
        LabyrinthData data = LabyrinthData.get(caller.server);
        CompoundTag p = playerState(data, caller.getUUID());
        long today = HouseCalendar.today(caller.server);
        if (p.contains("LastCallDay") && p.getLong("LastCallDay") == today) {
            log(caller, NOTE, "No answer.");
            return;
        }

        ServerPlayer onlinePlayer = caller.server.getPlayerList().getPlayerByName(rawTarget);
        if (onlinePlayer != null) {
            callPlayer(caller, data, p, today, onlinePlayer.getUUID(), onlinePlayer.getGameProfile().getName());
            return;
        }

        LivingEntity named = findNamedLiving(caller.serverLevel(), caller, rawTarget);
        if (named != null) {
            TargetKind kind = classify(named.getType(), true, named);
            if (kind == TargetKind.FORBIDDEN) {
                consumeBlocked(caller, data, p, today);
                return;
            }
            scheduleFriendly(caller, data, p, today);
            return;
        }

        String normalized = rawTarget.toLowerCase(Locale.ROOT).trim().replace(' ', '_');
        // Typed on the phone, so anything can arrive here; tryParse refuses what no id could be instead of throwing.
        ResourceLocation key = ResourceLocation.tryParse(normalized);
        if (key == null || !BuiltInRegistries.ENTITY_TYPE.containsKey(key)) {
            // A name that is no creature may still be a player who is offline; the cache is absent on some servers.
            var cache = caller.server.getProfileCache();
            var profile = cache == null ? java.util.Optional.<com.mojang.authlib.GameProfile>empty() : cache.get(rawTarget);
            if (profile.isPresent()) {
                callPlayer(caller, data, p, today, profile.get().getId(), profile.get().getName());
            } else {
                log(caller, HIM, "?");
            }
            return;
        }
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(key);
        Entity probe = type.create(caller.serverLevel());
        TargetKind kind = classify(type, false, probe);
        if (probe != null) {
            probe.discard();
        }
        if (kind == TargetKind.FORBIDDEN) {
            consumeBlocked(caller, data, p, today);
        } else if (kind == TargetKind.HOSTILE) {
            scheduleHostile(caller, data, p, today, key);
        } else if (kind == TargetKind.FRIENDLY) {
            scheduleFriendly(caller, data, p, today);
        } else {
            log(caller, HIM, "?");
        }
    }

    private static void callPlayer(ServerPlayer caller, LabyrinthData data, CompoundTag p, long today, UUID targetId, String targetName) {
        if (!HouseConfig.HARRIGAN_PLAYER_TARGETS.get() || targetId.equals(caller.getUUID())) {
            scrambled(caller);
            return;
        }
        p.putLong("LastCallDay", today);
        p.putUUID("PendingPlayer", targetId);
        p.putString("PendingPlayerName", targetName);
        p.putLong("PlayerNightDay", today + (isNight(caller.server) ? 1L : 0L));
        savePlayerState(data, caller.getUUID(), p);
        acknowledge(caller, data, p);
    }

    private static void scheduleHostile(ServerPlayer caller, LabyrinthData data, CompoundTag p, long today, ResourceLocation type) {
        if (!caller.getRespawnDimension().equals(Level.OVERWORLD) || caller.getRespawnPosition() == null) {
            consumeBlocked(caller, data, p, today);
            return;
        }
        p.putLong("LastCallDay", today);
        p.putString("HostileType", type.toString());
        p.putLong("HostileBed", caller.getRespawnPosition().asLong());
        p.putLong("HostileDay", today + 1L);
        savePlayerState(data, caller.getUUID(), p);
        acknowledge(caller, data, p);
    }

    private static void scheduleFriendly(ServerPlayer caller, LabyrinthData data, CompoundTag p, long today) {
        int strikes = p.getInt("FriendlyStrikes") + 1;
        p.putInt("FriendlyStrikes", strikes);
        p.putLong("LastCallDay", today);
        p.putBoolean("FriendlyPending", true);
        p.putLong("FriendlyNightDay", today);
        p.putInt("TotalCalls", p.getInt("TotalCalls") + 1);
        savePlayerState(data, caller.getUUID(), p);
        // No text reply. He answers in person.
        log(caller, NOTE, "Delivered");
    }

    private static void consumeBlocked(ServerPlayer caller, LabyrinthData data, CompoundTag p, long today) {
        p.putLong("LastCallDay", today);
        p.putInt("TotalCalls", p.getInt("TotalCalls") + 1);
        savePlayerState(data, caller.getUUID(), p);
        scrambled(caller);
    }

    private static void acknowledge(ServerPlayer player, LabyrinthData data, CompoundTag state) {
        int calls = state.getInt("TotalCalls");
        state.putInt("TotalCalls", calls + 1);
        savePlayerState(data, player.getUUID(), state);
        String[] pool = calls < 2 ? ACK : DISTORTED;
        log(player, HIM, pool[Math.floorMod(calls, pool.length)]);
    }

    private static void scrambled(ServerPlayer player) {
        log(player, HIM, "d̷o̴n̵'̷t̴");
    }

    // ---------------------------------------------------------------------
    // Night, dawn and the thing that answers

    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % 10 != 0) {
            return;
        }
        tickVignette(server);
        tickPhones(server);
        tickGhosts(server);
        tickDrownedPhones(server);
    }

    private static void tickVignette(MinecraftServer server) {
        ServerLevel interior = server.getLevel(HouseDimensions.INTERIOR);
        BlockPos base = base(server);
        if (interior == null || base == null || !interior.isLoaded(base)) {
            return;
        }
        LabyrinthData data = LabyrinthData.get(server);
        int visit = visitFor(data);
        List<ServerPlayer> visitors = interior.getEntitiesOfClass(ServerPlayer.class, placeBounds(base));
        if (visitors.isEmpty()) {
            if (visit >= FINAL_VISIT) funeralPlaque(interior, base, data);
            return;
        }

        ensureProps(interior,base,visit);
        var harrigan = harrigan(interior, base, phaseFor(data, visit));
        if (visit == 1 && !data.isCompleted(ID)) {
            // Alive, he turns his head toward whoever is nearest, as far as an old neck turns.
            ServerPlayer nearest = null;
            for (ServerPlayer player : visitors) {
                tickReading(player, data);
                if (harrigan != null && player.isAlive() && !player.isSpectator() && player.distanceToSqr(harrigan) < 64
                        && (nearest == null || player.distanceToSqr(harrigan) < nearest.distanceToSqr(harrigan))) nearest = player;
            }
            if (harrigan != null && harrigan.phase() == ALIVE_PHASE) {
                float toward = nearest == null ? 180.0F : (float) Math.toDegrees(Math.atan2(harrigan.getX() - nearest.getX(), nearest.getZ() - harrigan.getZ()));
                harrigan.setYHeadRot(180.0F + Math.max(-60.0F, Math.min(60.0F, net.minecraft.util.Mth.wrapDegrees(toward - 180.0F))));
            }
            return;
        }
        if (visit == 2) {
            for (ServerPlayer player : visitors) explainDeath(player, data);
            return;
        }
        HarriganWake.ensureCast(interior, base, false);
        for (ServerPlayer player : visitors) explainFuneral(player, data);
    }

    private static void tickReading(ServerPlayer player, LabyrinthData data) {
        if(!player.isAlive()||player.isSpectator()){READERS.remove(player.getUUID());return;}
        CompoundTag state = data.state(ID);
        if (readingFinished(state)) return;
        long now = player.serverLevel().getGameTime();
        if (player.containerMenu instanceof LecternMenu lectern && READERS.contains(player.getUUID())) {
            observeReadingPage(player,data,lectern.getPage());readingView(player,lectern.getPage());
            return;
        }
        // An occupied study alone never starts his reading commentary.
    }

    private static void readingView(ServerPlayer player,int page){var tag=new CompoundTag();tag.putUUID("Reader",player.getUUID());tag.putBoolean("HarriganReading",true);tag.putString("HarriganLine",page>0?PAGE_LINES[Math.min(PAGE_LINES.length-1,page-1)]:"Mr. Harrigan: Take your time. I'm listening.");io.github.knaitoe.theoldesthouse.network.HousePackets.send(player,new io.github.knaitoe.theoldesthouse.network.LiteraryViewPayload(-1,20,0,tag));}
    private static boolean readingFinished(CompoundTag state) {
        // Older saves have already spoken the closing line when the ticket appears.
        return state.getBoolean("ReadingFinished") || state.getBoolean("TicketVisible") || state.getBoolean("BeatDone");
    }

    private static void observeReadingPage(ServerPlayer player, LabyrinthData data, int page) {
        CompoundTag state = data.state(ID);
        if (readingFinished(state)) return;
        page = Math.max(0, Math.min(READING_PAGES.size() - 1, page));
        int highest = state.contains("ReadingPage") ? state.getInt("ReadingPage") : -1;
        if (state.getBoolean("ReadingStarted") && page <= highest) return;
        state.putBoolean("ReadingStarted", true);
        if (page > highest) {
            state.putInt("ReadingPage", page);
            if (page > 0) state.putInt("PagesTurned", state.getInt("PagesTurned") + 1);
        }
        data.setState(ID, state);
        if (page > highest && page > 0) player.displayClientMessage(Component.literal(PAGE_LINES[page - 1])
                .withStyle(ChatFormatting.GRAY), false);
    }

    private static void explainDeath(ServerPlayer player, LabyrinthData data) {
        CompoundTag personal = playerState(data, player.getUUID());
        if (personal.getBoolean("DeathExplained0505")) return;
        personal.putBoolean("DeathExplained0505", true);
        savePlayerState(data, player.getUUID(), personal);
        player.displayClientMessage(Component.literal("Mr. Harrigan has died in his chair. His phone is on the table beside him.")
                .withStyle(ChatFormatting.GRAY), false);
        player.displayClientMessage(Component.literal("Your spare phone stays yours. Take his if you mean to see it buried with him; the funeral is the next time this room opens.")
                .withStyle(ChatFormatting.GRAY), false);
    }

    private static void explainFuneral(ServerPlayer player, LabyrinthData data) {
        CompoundTag personal = playerState(data, player.getUUID());
        if (personal.getBoolean("FuneralExplained")) return;
        personal.putBoolean("FuneralExplained", true);
        savePlayerState(data, player.getUUID(), personal);
        player.displayClientMessage(Component.literal("The door opens on a funeral home now. He lies at the front of the chapel, made ready for his funeral.")
                .withStyle(ChatFormatting.GRAY), false);
        player.displayClientMessage(Component.literal("Hold his phone and right-click his casket to lay it beside him, or leave with it to keep it. Only if it goes with him may your own phone reach his number outside the House.")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), false);
    }

    private static void explainCalling(ServerPlayer player, LabyrinthData data) {
        CompoundTag personal = playerState(data, player.getUUID());
        if (personal.getBoolean("CallingExplained")) return;
        personal.putBoolean("CallingExplained", true);
        savePlayerState(data, player.getUUID(), personal);
        player.displayClientMessage(Component.literal("Your own phone can reach his number now. Outside the House, open it to call him, or text him a creature's name.")
                .withStyle(ChatFormatting.GRAY), false);
        player.displayClientMessage(Component.literal("One call a day. Name a hostile species, such as zombie: those creatures within 64 blocks of your Overworld bed die at the next dawn. Keep a bed set as your respawn point.")
                .withStyle(ChatFormatting.DARK_GRAY), false);
        player.displayClientMessage(Component.literal("The clock works whether or not his number answers.")
                .withStyle(ChatFormatting.DARK_GRAY), false);
    }

    private static void tickPhones(MinecraftServer server) {
        LabyrinthData data = LabyrinthData.get(server);
        long today = HouseCalendar.today(server);
        long tod = HouseCalendar.timeOfDay(server);

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            CompoundTag p = playerState(data, player.getUUID());

            if (p.contains("MorningDay") && today >= p.getLong("MorningDay") && tod <= 1500L) {
                String name = p.getString("MorningName");
                if (!name.isEmpty()) {
                    player.sendSystemMessage(Component.literal(name).withStyle(ChatFormatting.GRAY));
                }
                p.remove("MorningDay");
                p.remove("MorningName");
                savePlayerState(data, player.getUUID(), p);
            }

            if (p.contains("HostileType") && today >= p.getLong("HostileDay") && tod <= 1500L) {
                resolveHostileCall(server, player, data, p);
                p = playerState(data, player.getUUID());
            }

            if (p.getBoolean("FriendlyPending") && today >= p.getLong("FriendlyNightDay")
                    && isNight(server) && player.serverLevel().dimension().equals(Level.OVERWORLD)
                    && !hasActiveGhost(server, player.getUUID())) {
                int strike = Math.max(1, p.getInt("FriendlyStrikes"));
                spawnGhost(player.serverLevel(), player, player.getUUID(), strike, null);
                p.putBoolean("FriendlyPending", false);
                savePlayerState(data, player.getUUID(), p);
            }

            if (p.hasUUID("PendingPlayer") && today >= p.getLong("PlayerNightDay") && isNight(server)
                    && !hasActivePlayerAssignment(server, player.getUUID())) {
                ServerPlayer target = server.getPlayerList().getPlayer(p.getUUID("PendingPlayer"));
                if (target != null
                        && !target.serverLevel().dimension().equals(HouseDimensions.INTERIOR)
                        && target.serverLevel().dimension().equals(Level.OVERWORLD)) {
                    spawnGhost(target.serverLevel(), target, player.getUUID(), 3, target.getUUID());
                }
            }
        }
    }

    private static void resolveHostileCall(MinecraftServer server, ServerPlayer caller, LabyrinthData data, CompoundTag p) {
        ResourceLocation key = ResourceLocation.tryParse(p.getString("HostileType"));
        if (key == null || !BuiltInRegistries.ENTITY_TYPE.containsKey(key)) {
            p.remove("HostileType");
            savePlayerState(data, caller.getUUID(), p);
            return;
        }
        EntityType<?> targetType = BuiltInRegistries.ENTITY_TYPE.get(key);
        BlockPos bed = BlockPos.of(p.getLong("HostileBed"));
        ServerLevel level = server.overworld();

        int minCx = (bed.getX() - (int) KILL_RADIUS) >> 4;
        int maxCx = (bed.getX() + (int) KILL_RADIUS) >> 4;
        int minCz = (bed.getZ() - (int) KILL_RADIUS) >> 4;
        int maxCz = (bed.getZ() + (int) KILL_RADIUS) >> 4;
        for (int cx = minCx; cx <= maxCx; cx++) {
            for (int cz = minCz; cz <= maxCz; cz++) {
                level.getChunk(cx, cz);
            }
        }

        AABB area = new AABB(
                bed.getX() - KILL_RADIUS, level.getMinBuildHeight(), bed.getZ() - KILL_RADIUS,
                bed.getX() + KILL_RADIUS + 1.0D, level.getMaxBuildHeight(), bed.getZ() + KILL_RADIUS + 1.0D);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area,
                e -> e.getType() == targetType && !e.hasCustomName())) {
            entity.hurt(level.damageSources().genericKill(), Float.MAX_VALUE);
        }

        p.remove("HostileType");
        p.remove("HostileBed");
        p.remove("HostileDay");
        savePlayerState(data, caller.getUUID(), p);
    }

    private static void spawnGhost(ServerLevel level, ServerPlayer target, UUID owner, int mode, @Nullable UUID playerTarget) {
        Vec3 spot = mode == 1 ? lightEdgeSpawn(level, target) : standingSpot(level, behind(target, 8.0D));

        Zombie ghost = EntityType.ZOMBIE.create(level);
        if (ghost == null) {
            return;
        }
        ghost.addTag(GHOST_TAG);
        ghost.getPersistentData().putUUID(GHOST_OWNER, owner);
        ghost.getPersistentData().putInt(GHOST_MODE, mode);
        if (playerTarget != null) {
            ghost.getPersistentData().putUUID(GHOST_TARGET, playerTarget);
        }
        ghost.moveTo(spot.x, spot.y, spot.z, target.getYRot() + 180.0F, 0.0F);
        ghost.setCustomName(Component.literal("Mr. Harrigan").withStyle(ChatFormatting.DARK_GRAY));
        ghost.setCustomNameVisible(false);
        ghost.setPersistenceRequired();
        ghost.setCanPickUpLoot(false);
        ghost.setSilent(true);
        ghost.setInvulnerable(true);
        equipSuit(ghost);
        ghost.setDropChance(EquipmentSlot.CHEST, 0.0F);
        ghost.setDropChance(EquipmentSlot.LEGS, 0.0F);
        ghost.setDropChance(EquipmentSlot.FEET, 0.0F);
        var speed = ghost.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.setBaseValue(0.18D);
        }
        var damage = ghost.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
        if (damage != null) {
            damage.setBaseValue(10.0D);
        }
        // The first two appearances may look threatening, but cannot hurt the
        // caller. Only the third-and-later version is actually an attacker.
        if (mode < 3 && damage != null) {
            damage.setBaseValue(0.0D);
        }
        level.addFreshEntity(ghost);
        ACTIVE_GHOSTS.put(owner, ghost.getUUID());
        if (mode >= 3) {
            ghost.setTarget(target);
        } else {
            ghost.getNavigation().stop();
            ghost.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }
    }

    private static void tickGhosts(MinecraftServer server) {
        boolean night = isNight(server);
        long today = HouseCalendar.today(server);

        for (UUID owner : List.copyOf(ACTIVE_GHOSTS.keySet())) {
            Zombie ghost = activeGhost(server, owner);
            if (ghost == null) {
                continue;
            }
            if (!night) {
                finishGhost(server, ghost, today);
                continue;
            }

            CompoundTag tag = ghost.getPersistentData();
            UUID targetId = tag.hasUUID(GHOST_TARGET) ? tag.getUUID(GHOST_TARGET) : owner;
            ServerPlayer target = server.getPlayerList().getPlayer(targetId);
            if (target == null || target.serverLevel() != (ServerLevel) ghost.level()
                    || target.serverLevel().dimension().equals(HouseDimensions.INTERIOR)) {
                // A player assignment keeps waiting for its target. A friendly
                // appearance deferred by logout/House entry tries again next night.
                if (!tag.hasUUID(GHOST_TARGET)) {
                    LabyrinthData data = LabyrinthData.get(server);
                    CompoundTag p = playerState(data, owner);
                    p.putBoolean("FriendlyPending", true);
                    p.putLong("FriendlyNightDay", today + 1L);
                    savePlayerState(data, owner, p);
                }
                ACTIVE_GHOSTS.remove(owner);
                ghost.discard();
                continue;
            }

            int mode = tag.getInt(GHOST_MODE);
            if (ghost.tickCount % 18 == 0) {
                dirtShoulders((ServerLevel) ghost.level(), ghost);
            }
            if (mode == 1) {
                ghost.setTarget(null);
                ghost.getNavigation().stop();
                ghost.getLookControl().setLookAt(target, 30.0F, 30.0F);
                if (ghost.distanceToSqr(target) < 36.0D) {
                    ACTIVE_GHOSTS.remove(owner);
                    ghost.discard();
                }
            } else if (mode == 2) {
                ghost.setTarget(null);
                followAtDistance(ghost, target, 8.0D);
                if (ghost.tickCount % 200 == 0) {
                    disturbHouse((ServerLevel) ghost.level(), ghost.blockPosition(), target);
                }
            } else {
                ghost.setTarget(target);
                if (!target.isAlive() && !target.getUUID().equals(owner)) {
                    LabyrinthData data = LabyrinthData.get(server);
                    CompoundTag p = playerState(data, owner);
                    p.putString("MorningName", target.getGameProfile().getName());
                    p.putLong("MorningDay", today + 1L);
                    p.remove("PendingPlayer");
                    p.remove("PendingPlayerName");
                    p.remove("PlayerNightDay");
                    savePlayerState(data, owner, p);
                    ACTIVE_GHOSTS.remove(owner);
                    ghost.discard();
                }
            }
        }
    }

    private static void finishGhost(MinecraftServer server, Zombie ghost, long today) {
        CompoundTag tag = ghost.getPersistentData();
        if (tag.hasUUID(GHOST_OWNER)) {
            UUID owner = tag.getUUID(GHOST_OWNER);
            ACTIVE_GHOSTS.remove(owner);
            if (tag.hasUUID(GHOST_TARGET)) {
                LabyrinthData data = LabyrinthData.get(server);
                CompoundTag p = playerState(data, owner);
                p.remove("PendingPlayer");
                p.remove("PendingPlayerName");
                p.remove("PlayerNightDay");
                savePlayerState(data, owner, p);
            }
        }
        ghost.discard();
    }

    @Nullable
    private static Zombie activeGhost(MinecraftServer server, UUID owner) {
        UUID entityId = ACTIVE_GHOSTS.get(owner);
        if (entityId == null) {
            return null;
        }
        for (ServerLevel level : server.getAllLevels()) {
            Entity entity = level.getEntity(entityId);
            if (entity instanceof Zombie zombie && !zombie.isRemoved()) {
                return zombie;
            }
        }
        ACTIVE_GHOSTS.remove(owner);
        return null;
    }

    private static void followAtDistance(Zombie ghost, ServerPlayer target, double desired) {
        double distance = ghost.distanceTo(target);
        if (distance > desired + 1.5D) {
            ghost.getNavigation().moveTo(target, 0.72D);
        } else if (distance < desired - 2.0D) {
            Vec3 away = ghost.position().subtract(target.position()).normalize();
            Vec3 goal = target.position().add(away.scale(desired));
            ghost.getNavigation().moveTo(goal.x, goal.y, goal.z, 0.65D);
        } else {
            ghost.getNavigation().stop();
            ghost.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }
    }

    private static void disturbHouse(ServerLevel level, BlockPos around, ServerPlayer target) {
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(around.offset(-5, -2, -5), around.offset(5, 3, 5))) {
            BlockState state = level.getBlockState(pos);
            if (!(state.is(Blocks.TORCH) || state.is(Blocks.WALL_TORCH)
                    || state.is(Blocks.GLASS_PANE) || state.getBlock() instanceof DoorBlock)) {
                continue;
            }
            double d = pos.distSqr(target.blockPosition());
            if (d < bestDistance) {
                bestDistance = d;
                best = pos.immutable();
            }
        }
        if (best != null) {
            level.destroyBlock(best, true);
        }
    }

    private static void tickDrownedPhones(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.serverLevel().dimension().equals(HouseDimensions.INTERIOR)) {
                continue;
            }
            AABB area = player.getBoundingBox().inflate(96.0D);
            for (ItemEntity item : player.serverLevel().getEntitiesOfClass(ItemEntity.class, area,
                    e -> e.getItem().is(LabyrinthRegistry.PHONE.get()))) {
                UUID owner = phoneOwner(item.getItem());
                if (owner == null || !deepWater(player.serverLevel(), item.blockPosition())) {
                    continue;
                }
                item.discard();
                sever(server, owner);
            }
        }
    }

    private static boolean deepWater(ServerLevel level, BlockPos pos) {
        return level.getFluidState(pos).is(FluidTags.WATER)
                && level.getFluidState(pos.below()).is(FluidTags.WATER)
                && level.getFluidState(pos.below(2)).is(FluidTags.WATER);
    }

    private static void sever(MinecraftServer server, UUID owner) {
        LabyrinthData data = LabyrinthData.get(server);
        CompoundTag p = playerState(data, owner);
        p.putBoolean("Severed", true);
        p.putBoolean("Contact", false);
        p.putBoolean("FriendlyPending", false);
        p.remove("HostileType");
        p.remove("HostileBed");
        p.remove("HostileDay");
        p.remove("PendingPlayer");
        p.remove("PendingPlayerName");
        p.remove("PlayerNightDay");
        savePlayerState(data, owner, p);

        Zombie ghost = activeGhost(server, owner);
        if (ghost != null) {
            ghost.discard();
        }
        ACTIVE_GHOSTS.remove(owner);
        ServerPlayer player = server.getPlayerList().getPlayer(owner);
        if (player != null) {
            player.displayClientMessage(Component.literal("The screen goes black.")
                    .withStyle(ChatFormatting.DARK_GRAY), false);
        }
    }

    // ---------------------------------------------------------------------
    // State and utility

    private static CompoundTag playerState(LabyrinthData data, UUID player) {
        CompoundTag state = data.state(ID);
        CompoundTag players = state.getCompound("Players");
        String key = player.toString();
        return players.contains(key) ? players.getCompound(key).copy() : new CompoundTag();
    }

    private static void savePlayerState(LabyrinthData data, UUID player, CompoundTag value) {
        CompoundTag state = data.state(ID);
        CompoundTag players = state.getCompound("Players");
        players.put(player.toString(), value.copy());
        state.put("Players", players);
        data.setState(ID, state);
    }

    private static boolean isNight(MinecraftServer server) {
        long time = HouseCalendar.timeOfDay(server);
        return time >= NIGHT_START && time < NIGHT_END;
    }

    private static boolean inVignette(ServerPlayer player) {
        if (!player.serverLevel().dimension().equals(HouseDimensions.INTERIOR)) {
            return false;
        }
        BlockPos origin = HouseSavedData.get(player.server).houseOrigin();
        return origin != null && LabyrinthPlaces.placeAt(origin, player.blockPosition()) == LabyrinthPlace.HARRIGAN;
    }

    @Nullable
    private static BlockPos base(MinecraftServer server) {
        BlockPos origin = HouseSavedData.get(server).houseOrigin();
        return origin == null ? null : LabyrinthPlaces.base(origin, LabyrinthPlace.HARRIGAN);
    }

    private static AABB placeBounds(@Nullable BlockPos base) {
        if (base == null) {
            return new AABB(0, 0, 0, 0, 0, 0);
        }
        return new AABB(base.getX() - 8, base.getY() - 1, base.getZ() - 25,
                base.getX() + 8, base.getY() + 7, base.getZ() + 2);
    }

    private static boolean isCasket(MinecraftServer server, BlockPos pos) {
        BlockPos base = base(server);
        if (base == null) {
            return false;
        }
        BlockPos rel = pos.subtract(base);
        return rel.getY() >= 0 && rel.getY() <= 1
                && rel.getX() >= -2 && rel.getX() <= 2
                && rel.getZ() >= -22 && rel.getZ() <= -19;
    }

    private static boolean hasItem(ServerPlayer player, net.minecraft.world.item.Item item) {
        return player.getInventory().contains(new ItemStack(item));
    }

    private static boolean hasOwnedPhone(ServerPlayer player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(LabyrinthRegistry.PHONE.get()) && isOwnedPhone(stack, player.getUUID())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isOwnedPhone(ItemStack stack, UUID player) {
        UUID owner = phoneOwner(stack);
        return owner != null && owner.equals(player);
    }

    @Nullable
    private static UUID phoneOwner(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.hasUUID(OWNER) ? tag.getUUID(OWNER) : null;
    }

    @Nullable
    private static LivingEntity findNamedLiving(ServerLevel level, ServerPlayer caller, String name) {
        return level.getEntitiesOfClass(LivingEntity.class, caller.getBoundingBox().inflate(KILL_RADIUS),
                        e -> e.hasCustomName() && e.getName().getString().equalsIgnoreCase(name))
                .stream().findFirst().orElse(null);
    }

    private static Vec3 behind(ServerPlayer player, double distance) {
        Vec3 look = player.getLookAngle();
        Vec3 horizontal = new Vec3(look.x, 0.0D, look.z);
        if (horizontal.lengthSqr() < 0.001D) {
            horizontal = new Vec3(0, 0, 1);
        }
        return player.position().subtract(horizontal.normalize().scale(distance));
    }

    /**
     * First warning: put him where a human eye tends to read "the edge of my
     * light", not simply a fixed distance behind the caller.
     *
     * We sample the forward 140-degree field, prefer block-light 2-4, and
     * mildly prefer 12-16 blocks. Block light is intentional: moonlight and
     * shader sky light must not shove him into a wall merely because the sky
     * is bright.
     */
    static Vec3 lightEdgeSpawn(ServerLevel level, ServerPlayer player) {
        Vec3 look = player.getLookAngle();
        Vec3 forward = new Vec3(look.x, 0.0D, look.z);
        if (forward.lengthSqr() < 0.001D) {
            forward = new Vec3(0.0D, 0.0D, 1.0D);
        }
        forward = forward.normalize();
        Vec3 right = new Vec3(-forward.z, 0.0D, forward.x);

        Vec3 best = null;
        double bestScore = Double.MAX_VALUE;
        for (int radius = 10; radius <= 18; radius += 2) {
            for (int step = -7; step <= 7; step++) {
                double angle = Math.toRadians(step * 10.0D);
                Vec3 direction = forward.scale(Math.cos(angle)).add(right.scale(Math.sin(angle))).normalize();
                Vec3 raw = player.position().add(direction.scale(radius));
                Vec3 candidate = standingSpotOrNull(level, raw);
                if (candidate == null) {
                    continue;
                }

                BlockPos eyes = BlockPos.containing(candidate.x, candidate.y + 1.5D, candidate.z);
                int blockLight = level.getBrightness(LightLayer.BLOCK, eyes);
                if (blockLight > 7) {
                    continue;
                }

                double lightScore = Math.abs(blockLight - 3) * 4.0D;
                double radiusScore = Math.abs(radius - 14) * 0.65D;
                double angleScore = Math.abs(step) * 0.35D;
                double score = lightScore + radiusScore + angleScore;
                if (score < bestScore) {
                    bestScore = score;
                    best = candidate;
                }
            }
        }
        return best != null ? best : standingSpot(level, behind(player, 14.0D));
    }

    private static Vec3 standingSpot(ServerLevel level, Vec3 raw) {
        Vec3 found = standingSpotOrNull(level, raw);
        return found != null ? found : raw;
    }

    @Nullable
    private static Vec3 standingSpotOrNull(ServerLevel level, Vec3 raw) {
        BlockPos feet = BlockPos.containing(raw);
        for (int dy = 3; dy >= -4; dy--) {
            BlockPos candidate = feet.offset(0, dy, 0);
            if (level.getBlockState(candidate).isAir()
                    && level.getBlockState(candidate.above()).isAir()
                    && level.getBlockState(candidate.below()).isCollisionShapeFullBlock(level, candidate.below())) {
                return Vec3.atBottomCenterOf(candidate);
            }
        }
        return null;
    }

    private static void dirtShoulders(ServerLevel level, Zombie ghost) {
        BlockParticleOption dirt = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState());
        double y = ghost.getY() + 1.55D;
        Vec3 right = new Vec3(
                Math.cos(Math.toRadians(ghost.getYRot())),
                0.0D,
                Math.sin(Math.toRadians(ghost.getYRot()))
        ).scale(0.26D);

        level.sendParticles(dirt,
                ghost.getX() + right.x, y, ghost.getZ() + right.z,
                2, 0.05D, 0.04D, 0.05D, 0.005D);
        level.sendParticles(dirt,
                ghost.getX() - right.x, y, ghost.getZ() - right.z,
                2, 0.05D, 0.04D, 0.05D, 0.005D);
    }

    private static boolean hasActiveGhost(MinecraftServer server, UUID owner) {
        return activeGhost(server, owner) != null;
    }

    private static boolean hasActivePlayerAssignment(MinecraftServer server, UUID owner) {
        Zombie ghost = activeGhost(server, owner);
        return ghost != null && ghost.getPersistentData().hasUUID(GHOST_TARGET);
    }

    private static void clearTagged(ServerLevel level, AABB area, String tag) {
        for (Entity entity : level.getEntities((Entity) null, area, e -> e.getTags().contains(tag))) {
            entity.discard();
        }
    }

    private static void clearPropRole(ServerLevel level, AABB area, String role) {
        for (Entity entity : level.getEntities((Entity) null, area, e -> e.getTags().contains(PROP_TAG + "_" + role))) {
            entity.discard();
        }
    }

    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        READERS.remove(event.getEntity().getUUID());
        NEXT_SPEECH.remove(event.getEntity().getUUID());
    }

    public static void clearAll(MinecraftServer server) {
        READERS.clear();
        for (UUID owner : List.copyOf(ACTIVE_GHOSTS.keySet())) {
            Zombie ghost = activeGhost(server, owner);
            if (ghost != null) {
                ghost.discard();
            }
        }
        ACTIVE_GHOSTS.clear();
        NEXT_SPEECH.clear();
    }

    public static List<String> describe(MinecraftServer server, @Nullable ServerPlayer viewer) {
        LabyrinthData data = LabyrinthData.get(server);
        List<String> out = new ArrayList<>();
        CompoundTag state = data.state(ID);
        out.add("Harrigan: visit " + Math.max(1, state.getInt("Visit")) + "/" + FINAL_VISIT
                + (data.isCompleted(ID) ? ", vignette complete." : "."));
        if (viewer != null) {
            CompoundTag p = playerState(data, viewer.getUUID());
            out.add("Your phone: " + (p.getBoolean("Severed") ? "severed"
                    : p.getBoolean("Contact") ? "connected" : "not connected")
                    + "; friendly strikes " + p.getInt("FriendlyStrikes") + ".");
        }
        return out;
    }
}
