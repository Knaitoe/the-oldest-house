package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.Neighbours;
import io.github.knaitoe.theoldesthouse.labyrinth.Artifacts;
import io.github.knaitoe.theoldesthouse.labyrinth.BlyFollows;
import io.github.knaitoe.theoldesthouse.labyrinth.ChurchKeyItem;
import io.github.knaitoe.theoldesthouse.labyrinth.DreamStela;
import io.github.knaitoe.theoldesthouse.labyrinth.DrownedTownRegistry;
import io.github.knaitoe.theoldesthouse.labyrinth.ElkFollows;
import io.github.knaitoe.theoldesthouse.labyrinth.FeetUp;
import io.github.knaitoe.theoldesthouse.labyrinth.GoatmanHaunt;
import io.github.knaitoe.theoldesthouse.labyrinth.GoatmanVignette;
import io.github.knaitoe.theoldesthouse.labyrinth.HarriganPhones;
import io.github.knaitoe.theoldesthouse.labyrinth.HarriganVignette;
import io.github.knaitoe.theoldesthouse.labyrinth.HotelVignette;
import io.github.knaitoe.theoldesthouse.labyrinth.Keepsakes;
import io.github.knaitoe.theoldesthouse.labyrinth.KeptBikeItem;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthData;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthPlace;
import io.github.knaitoe.theoldesthouse.labyrinth.LakeHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LakeHouseRegistry;
import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryVignettes;
import io.github.knaitoe.theoldesthouse.labyrinth.NotYetBlock;
import io.github.knaitoe.theoldesthouse.labyrinth.NotYetMending;
import io.github.knaitoe.theoldesthouse.labyrinth.NotYetSite;
import io.github.knaitoe.theoldesthouse.labyrinth.OwnLight;
import io.github.knaitoe.theoldesthouse.labyrinth.OwnLightLate;
import io.github.knaitoe.theoldesthouse.labyrinth.PhoneSounds;
import io.github.knaitoe.theoldesthouse.labyrinth.PolybiusPrize;
import io.github.knaitoe.theoldesthouse.labyrinth.PrizeWear;
import io.github.knaitoe.theoldesthouse.labyrinth.SelfAddressedEnvelopeItem;
import io.github.knaitoe.theoldesthouse.labyrinth.StelaPassage;
import io.github.knaitoe.theoldesthouse.labyrinth.Storeroom;
import io.github.knaitoe.theoldesthouse.labyrinth.TallyCounterItem;
import io.github.knaitoe.theoldesthouse.labyrinth.WeaponHistory;
import io.github.knaitoe.theoldesthouse.labyrinth.WorkPrint;
import io.github.knaitoe.theoldesthouse.network.LateBodiesPayload;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.AfterBatch;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.BundlePacket;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDestroyBlockEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * The Games are not the reader's own world (docs/NEIGHBOURS.md, R3). A neighbour lists the dimension types of its Games in
 * {@code #the_oldest_house:not_own_world}; here the Nether's stands in for A Reaping's arena, put in the tag for the batch as a
 * neighbour's jar would put the arena's. The House keeps out of it: the elk, the Lake House lamp, the hotel tab, the storeroom's pocket,
 * weapon history, the prizes, lateness, the Goatman, Bly's Lady, the film, Harrigan's line, the envelope, the keepsakes and artifacts that
 * work in the reader's own world. Each case takes the same world out of the tag for a moment, in the same tick, to show the House doing
 * there what it does in any world of the reader's own. And a reader taken out of the ward while the machine mends them is let go where
 * they are. Every case stands on a floor of its own in the Nether, far from every other's.
 */
@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class NotOwnWorldTests {
    private NotOwnWorldTests() {
    }

    private static final String BATCH = "not_own_world";
    /** The ward's case builds the place with a fixture that stands in for the House's whole record, so it has a batch of its own. */
    private static final String WARD = "not_own_world_ward";
    private static final List<NativeTestChunks> HELD = new ArrayList<>();
    private static LiteraryTests.Fixture ward;

    @BeforeBatch(batch = BATCH)
    public static void theGamesOpen(ServerLevel level) {
        games(nether(level.getServer()), true);
    }

    @AfterBatch(batch = BATCH)
    public static void theGamesClose(ServerLevel level) {
        games(nether(level.getServer()), false);
        games(level.getServer().overworld(), false);
        HELD.forEach(NativeTestChunks::close);
        HELD.clear();
    }

    @BeforeBatch(batch = WARD)
    public static void theWardOpens(ServerLevel level) {
        theGamesOpen(level);
    }

    @AfterBatch(batch = WARD)
    public static void theWardCloses(ServerLevel level) {
        if (ward != null) {
            ward.close();
            ward = null;
        }
        theGamesClose(level);
    }

    private static ServerLevel nether(MinecraftServer server) {
        return server.getLevel(Level.NETHER);
    }

    /** Puts a world's dimension type in {@code #not_own_world}, or takes it out, as a neighbour's jar would list its Games there. */
    @SuppressWarnings("unchecked")
    private static void games(ServerLevel level, boolean on) {
        Holder.Reference<DimensionType> holder = (Holder.Reference<DimensionType>) level.dimensionTypeRegistration();
        var tags = holder.tags().collect(Collectors.toCollection(HashSet::new));
        if (on) {
            tags.add(Neighbours.NOT_OWN_WORLD);
        } else {
            tags.remove(Neighbours.NOT_OWN_WORLD);
        }
        try {
            Method bind = Holder.Reference.class.getDeclaredMethod("bindTags", Collection.class);
            bind.setAccessible(true);
            bind.invoke(holder, tags);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Runs {@code body} with {@code level} in the tag or out of it, within this tick, and puts it back as it was. */
    private static void listed(ServerLevel level, boolean on, Runnable body) {
        boolean was = Neighbours.notOwnWorld(level);
        games(level, on);
        try {
            body.run();
        } finally {
            games(level, was);
        }
    }

    /**
     * A shut stone room in the Nether round {@code spot}, its floor at {@code spot}'s row, nine blocks across and five high, out of the
     * way of the Nether's own lava. Runs {@code body} once its chunks and their entities are resident; they are held until the batch ends.
     */
    private static void inTheGames(GameTestHelper h, BlockPos spot, Consumer<ServerLevel> body) {
        ServerLevel nether = nether(h.getLevel().getServer());
        NativeTestChunks chunks = new NativeTestChunks();
        HELD.add(chunks);
        chunks.request(nether, new AABB(spot).inflate(12));
        SceneReady.when(h, () -> chunks.ready() ? null : "the floor in the Nether round " + spot, 2, 1100, () -> {
            h.assertTrue(Neighbours.notOwnWorld(nether), "the Nether's dimension type stands in the tag for the batch, as the arena's would");
            for (BlockPos at : BlockPos.betweenClosed(spot.offset(-5, -1, -5), spot.offset(5, 5, 5))) {
                boolean shell = Math.abs(at.getX() - spot.getX()) == 5 || Math.abs(at.getZ() - spot.getZ()) == 5
                        || at.getY() == spot.getY() - 1 || at.getY() == spot.getY() + 5;
                nether.setBlock(at, (shell ? Blocks.STONE : Blocks.AIR).defaultBlockState(), Block.UPDATE_CLIENTS);
            }
            body.accept(nether);
        });
    }

    private static void into(ServerPlayer p, ServerLevel level, BlockPos at) {
        p.teleportTo(level, at.getX() + .5, at.getY(), at.getZ() + .5, 0F, 0F);
    }

    private static List<ItemEntity> dropsAround(ServerLevel level, Vec3 at) {
        return level.getEntitiesOfClass(ItemEntity.class, new AABB(at, at).inflate(3));
    }

    // ------------------------------------------------------------------------------------------------ the elk
    @GameTest(template = "empty", batch = BATCH, timeoutTicks = 1200, skyAccess = true)
    public static void theElkLeavesTheGamesAloneAndHerFieldStaysInItsWorld(GameTestHelper h) {
        BlockPos spot = new BlockPos(6000, 100, 6000);
        ServerPlayer p = NativeTestPlayers.survival(h, "elk_in_the_games");
        inTheGames(h, spot, nether -> {
            ServerLevel overworld = h.getLevel();
            LabyrinthData d = LabyrinthData.get(overworld.getServer());
            long time = overworld.getDayTime();
            try {
                into(p, nether, spot);
                CompoundTag own = LiteraryVignettes.personal(d, p.getUUID(), LabyrinthPlace.ELK_LOT);
                own.putBoolean(ElkFollows.FOLLOWS, true);
                own.putInt(ElkFollows.BASE, 0);
                LiteraryVignettes.save(d, p.getUUID(), LabyrinthPlace.ELK_LOT, own);

                // In the Games the meat and the hide do not spoil, nothing is counted against them, and a young one costs no night.
                var cow = EntityType.COW.create(nether);
                cow.moveTo(spot.getX() + 3.5, spot.getY(), spot.getZ() + .5);
                nether.addFreshEntity(cow);
                cow.hurt(nether.damageSources().playerAttack(p), 100);
                var drops = dropsAround(nether, cow.position());
                h.assertTrue(!cow.isAlive() && drops.stream().anyMatch(i -> i.getItem().is(Items.BEEF))
                                && drops.stream().noneMatch(i -> i.getItem().is(Items.ROTTEN_FLESH)),
                        "in the Games the meat does not spoil as it falls: " + drops.stream().map(i -> i.getItem().toString()).toList());
                drops.forEach(ItemEntity::discard);
                var calf = EntityType.COW.create(nether);
                calf.setBaby(true);
                calf.moveTo(spot.getX() - 3.5, spot.getY(), spot.getZ() + .5);
                nether.addFreshEntity(calf);
                calf.hurt(nether.damageSources().playerAttack(p), 100);
                dropsAround(nether, calf.position()).forEach(ItemEntity::discard);
                h.assertTrue(LiteraryVignettes.personal(d, p.getUUID(), LabyrinthPlace.ELK_LOT).getInt(ElkFollows.IN_KIND) == 0 && !ElkFollows.restless(p),
                        "nothing killed in the Games is owed in kind, and a young one there costs no night");

                // The same world out of the tag is a world of the reader's own: she answers what they do there.
                listed(nether, false, () -> {
                    var other = EntityType.COW.create(nether);
                    other.moveTo(spot.getX() + .5, spot.getY(), spot.getZ() + 3.5);
                    nether.addFreshEntity(other);
                    other.hurt(nether.damageSources().playerAttack(p), 100);
                    var spoiled = dropsAround(nether, other.position());
                    h.assertTrue(spoiled.stream().noneMatch(i -> i.getItem().is(Items.BEEF) || i.getItem().is(Items.LEATHER))
                                    && spoiled.stream().anyMatch(i -> i.getItem().is(Items.ROTTEN_FLESH))
                                    && LiteraryVignettes.personal(d, p.getUUID(), LabyrinthPlace.ELK_LOT).getInt(ElkFollows.IN_KIND) == 1,
                            "out of the tag it spoils and is owed, as in any world of their own: " + spoiled.stream().map(i -> i.getItem().toString()).toList());
                    spoiled.forEach(ItemEntity::discard);
                });

                // The morning in kind: owed one, at dawn in the Games nothing comes and the day is not marked, so it waits for their first dawn back.
                int dawn = (int) (time / 24000L) + 1;
                h.assertTrue(!ElkFollows.dawn(p, dawn)
                                && LiteraryVignettes.personal(d, p.getUUID(), LabyrinthPlace.ELK_LOT).getInt(ElkFollows.IN_KIND_DAY) != dawn,
                        "at dawn in the Games the morning in kind does not come, and the day is not spent");
                listed(nether, false, () -> {
                    CompoundTag owed = LiteraryVignettes.personal(d, p.getUUID(), LabyrinthPlace.ELK_LOT);
                    h.assertTrue(ElkFollows.dawn(p, dawn)
                                    && LiteraryVignettes.personal(d, p.getUUID(), LabyrinthPlace.ELK_LOT).getInt(ElkFollows.IN_KIND_DAY) == dawn
                                    && LiteraryVignettes.personal(d, p.getUUID(), LabyrinthPlace.ELK_LOT).getInt(ElkFollows.IN_KIND) == owed.getInt(ElkFollows.IN_KIND),
                            "out of the tag it comes at dawn; with no home of their own nothing is taken and the debt is kept");
                });

                // The calf's night: anchored in the Overworld at night, let go when they change worlds, and never set in the Games.
                long night = Math.floorDiv(time, 24000L) * 24000L + 18000L;
                overworld.setDayTime(night);
                overworld.updateSkyBrightness();
                own = LiteraryVignettes.personal(d, p.getUUID(), LabyrinthPlace.ELK_LOT);
                own.putLong(ElkFollows.CALF_UNTIL, night + 24000L);
                LiteraryVignettes.save(d, p.getUUID(), LabyrinthPlace.ELK_LOT, own);
                BlockPos field = h.absolutePos(new BlockPos(1, 2, 1));
                into(p, overworld, field);
                h.assertTrue(ElkFollows.restless(p) && overworld.isNight() && overworld.canSeeSky(p.blockPosition().above()),
                        "a restless reader out under the night sky: restless=" + ElkFollows.restless(p) + " night=" + overworld.isNight()
                                + " sky=" + overworld.canSeeSky(p.blockPosition().above()));
                Vec3 stood = p.position();
                listed(overworld, true, () -> {
                    ElkFollows.field(p);
                    h.assertTrue(ElkFollows.anchor(p).isEmpty() && p.position().distanceTo(stood) < .01,
                            "in a world listed as the Games the field never finds them");
                });
                ElkFollows.field(p);
                var anchor = ElkFollows.anchor(p).orElse(null);
                h.assertTrue(anchor != null && anchor.dimension().equals(Level.OVERWORLD) && anchor.at().distanceTo(stood) < .01,
                        "in their own world the field finds them where they stand, in that world: " + anchor);
                into(p, nether, spot);
                h.assertTrue(ElkFollows.anchor(p).isEmpty(), "taken into another world, where the field found them is let go");
                ElkFollows.field(p);
                h.assertTrue(ElkFollows.anchor(p).isEmpty() && p.level() == nether && p.position().distanceTo(Vec3.atBottomCenterOf(spot)) < .01,
                        "and nothing brings them back to it there");
            } finally {
                overworld.setDayTime(time);
                overworld.updateSkyBrightness();
                CompoundTag own = LiteraryVignettes.personal(d, p.getUUID(), LabyrinthPlace.ELK_LOT);
                own.remove(ElkFollows.FOLLOWS);
                own.remove(ElkFollows.CALF_UNTIL);
                own.remove(ElkFollows.IN_KIND);
                own.remove(ElkFollows.IN_KIND_DAY);
                LiteraryVignettes.save(d, p.getUUID(), LabyrinthPlace.ELK_LOT, own);
                NativeTestPlayers.remove(p);
            }
            h.succeed();
        });
    }

    // ------------------------------------------------------------------------------------------------ returns and the tab
    @GameTest(template = "empty", batch = BATCH, timeoutTicks = 1200)
    public static void theLampThePocketTheTabAndTheEnvelopeWaitOutsideTheGames(GameTestHelper h) {
        BlockPos spot = new BlockPos(6000, 100, 6100);
        ServerPlayer p = NativeTestPlayers.survival(h, "returns_in_the_games");
        inTheGames(h, spot, nether -> {
            LabyrinthData d = LabyrinthData.get(nether.getServer());
            try {
                into(p, nether, spot);

                // The Lake House lamp: a death in the Games does not arm its respawn.
                CompoundTag lake = LiteraryVignettes.personal(d, p.getUUID(), LakeHouse.PLACE);
                lake.putLong("HomeLight", spot.offset(0, 0, 3).asLong());
                LiteraryVignettes.save(d, p.getUUID(), LakeHouse.PLACE, lake);
                NeoForge.EVENT_BUS.post(new LivingDeathEvent(p, p.damageSources().generic()));
                h.assertTrue(!LiteraryVignettes.personal(d, p.getUUID(), LakeHouse.PLACE).getBoolean("LightRespawn"),
                        "a death in the Games does not arm the lamp");
                listed(nether, false, () -> {
                    NeoForge.EVENT_BUS.post(new LivingDeathEvent(p, p.damageSources().generic()));
                    h.assertTrue(LiteraryVignettes.personal(d, p.getUUID(), LakeHouse.PLACE).getBoolean("LightRespawn"),
                            "out of the tag, a death in a world of their own arms it, as it always has");
                });
                lake = LiteraryVignettes.personal(d, p.getUUID(), LakeHouse.PLACE);
                lake.remove("LightRespawn");
                lake.remove("HomeLight");
                LiteraryVignettes.save(d, p.getUUID(), LakeHouse.PLACE, lake);

                // The storeroom: the pocket return waits in the Games, and finds them in their own world.
                CompoundTag store = LiteraryVignettes.personal(d, p.getUUID(), Storeroom.PLACE);
                store.put("Return210", new ItemStack(Items.COMPASS).save(p.registryAccess()));
                store.putLong("ReturnDay210", nether.getDayTime() / 24000L - 3);
                LiteraryVignettes.save(d, p.getUUID(), Storeroom.PLACE, store);
                h.assertTrue(!Storeroom.returnTo(p) && p.getInventory().countItem(Items.COMPASS) == 0
                                && LiteraryVignettes.personal(d, p.getUUID(), Storeroom.PLACE).contains("Return210"),
                        "with no home bed, the thing on the tag is not put in their pocket in the Games");
                listed(nether, false, () -> h.assertTrue(Storeroom.returnTo(p) && p.getInventory().countItem(Items.COMPASS) == 1,
                        "out of the tag, it is in their pocket as it always was"));

                // The hotel tab: a chest opened in the Games collects nothing. The tab takes the first thing in the pack: here, only a diamond.
                p.getInventory().clearContent();
                BlockPos at = spot.offset(-3, 0, -3);
                nether.setBlock(at, Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);
                p.getInventory().add(new ItemStack(Items.DIAMOND));
                CompoundTag hotel = HotelVignette.personal(d, p.getUUID());
                hotel.putInt("Debt", 1);
                HotelVignette.save(d, p.getUUID(), hotel);
                p.openMenu((ChestBlockEntity) nether.getBlockEntity(at));
                h.assertTrue(HotelVignette.personal(d, p.getUUID()).getInt("Debt") == 1 && p.getInventory().countItem(Items.DIAMOND) == 1,
                        "a chest opened in the Games collects nothing on the tab");
                p.closeContainer();
                listed(nether, false, () -> {
                    p.openMenu((ChestBlockEntity) nether.getBlockEntity(at));
                    h.assertTrue(HotelVignette.personal(d, p.getUUID()).getInt("Debt") == 0 && p.getInventory().countItem(Items.DIAMOND) == 0,
                            "out of the tag, the same chest collects one place, as any chest does");
                    p.closeContainer();
                });

                // The envelope: in the Games it neither seals nor keeps a stack across a death.
                ItemStack envelope = SelfAddressedEnvelopeItem.forReader(p.getUUID());
                p.setItemInHand(InteractionHand.MAIN_HAND, envelope);
                p.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.STICK));
                p.getMainHandItem().use(nether, p, InteractionHand.MAIN_HAND);
                h.assertTrue(!sealed(p.getMainHandItem()) && p.getOffhandItem().is(Items.STICK), "in the Games the envelope seals nothing");
                listed(nether, false, () -> {
                    p.getMainHandItem().use(nether, p, InteractionHand.MAIN_HAND);
                    h.assertTrue(sealed(p.getMainHandItem()) && p.getOffhandItem().isEmpty(), "out of the tag it seals what the other hand holds");
                });
                var dying = new ArrayList<ItemEntity>(List.of(new ItemEntity(nether, p.getX(), p.getY(), p.getZ(), p.getMainHandItem().copy())));
                NeoForge.EVENT_BUS.post(new LivingDropsEvent(p, p.damageSources().generic(), dying, false));
                h.assertTrue(dying.size() == 1 && SelfAddressedEnvelopeItem.record(p).getList("Held", 10).isEmpty(),
                        "a sealed envelope falls with its keeper in the Games, and is not kept for them");
                listed(nether, false, () -> {
                    NeoForge.EVENT_BUS.post(new LivingDropsEvent(p, p.damageSources().generic(), dying, false));
                    h.assertTrue(dying.isEmpty() && SelfAddressedEnvelopeItem.record(p).getList("Held", 10).size() == 1,
                            "out of the tag it is kept for them, as away from the House anywhere of their own");
                });
            } finally {
                NativeTestPlayers.remove(p);
            }
            h.succeed();
        });
    }

    private static boolean sealed(ItemStack envelope) {
        var data = envelope.get(DataComponents.CUSTOM_DATA);
        return data != null && data.copyTag().contains(SelfAddressedEnvelopeItem.SEALED);
    }

    // ------------------------------------------------------------------------------------------------ what the House watches and marks
    @GameTest(template = "empty", batch = BATCH, timeoutTicks = 1200)
    public static void theHouseNeitherWatchesNorFollowsAReaderInTheGames(GameTestHelper h) {
        BlockPos spot = new BlockPos(6000, 100, 6200);
        ServerPlayer p = NativeTestPlayers.survival(h, "watched_in_the_games");
        inTheGames(h, spot, nether -> {
            LabyrinthData d = LabyrinthData.get(nether.getServer());
            into(p, nether, spot);
            try {
                // Weapon history: a blow, a bow drawn and a weapon let fall are not recorded there.
                var target = EntityType.COW.create(nether);
                p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_SWORD));
                NeoForge.EVENT_BUS.post(new AttackEntityEvent(p, target));
                h.assertTrue(!WeaponHistory.watched(p) && WeaponHistory.identity(p.getMainHandItem()) == null, "a blow in the Games is not recorded");
                ItemStack sword = p.getMainHandItem();
                p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOW));
                NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickItem(p, InteractionHand.MAIN_HAND));
                h.assertTrue(WeaponHistory.identity(p.getMainHandItem()) == null, "nor a bow drawn");
                var axe = new ItemEntity(nether, p.getX(), p.getY(), p.getZ(), new ItemStack(Items.IRON_AXE));
                NeoForge.EVENT_BUS.post(new ItemTossEvent(axe, p));
                h.assertTrue(WeaponHistory.identity(axe.getItem()) == null, "nor a weapon let fall");
                p.setItemInHand(InteractionHand.MAIN_HAND, sword);
                listed(nether, false, () -> {
                    NeoForge.EVENT_BUS.post(new AttackEntityEvent(p, target));
                    h.assertTrue(WeaponHistory.watched(p) && WeaponHistory.identity(p.getMainHandItem()) != null,
                            "out of the tag a blow is recorded, as anywhere of their own");
                });

                // The prizes: nothing comes off there, and the mood ring shows nothing.
                h.assertTrue(PrizeWear.putOn(p, HumanoidArm.RIGHT, PolybiusPrize.Kind.MOOD_RING), "the ring goes on");
                p.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                p.setPose(Pose.CROUCHING);
                h.assertTrue(!PrizeWear.takeOff(p) && (PrizeWear.worn(p) & PrizeWear.RIGHT_RING) != 0 && p.getMainHandItem().isEmpty(),
                        "in the Games no prize comes off into the hand");
                p.setHealth(5F);
                h.assertTrue(PolybiusPrize.mood(p) == 0, "badly hurt in the Games, the stone shows nothing: " + PolybiusPrize.mood(p));
                p.hurtTime = 5;
                h.assertTrue(PolybiusPrize.mood(p) == 0, "nor while they are being hurt");
                listed(nether, false, () -> {
                    h.assertTrue(PolybiusPrize.mood(p) == 4, "out of the tag, black while being hurt");
                    p.hurtTime = 0;
                    h.assertTrue(PolybiusPrize.mood(p) == 3, "and amber badly hurt");
                    h.assertTrue(PrizeWear.takeOff(p) && (PrizeWear.worn(p) & PrizeWear.RIGHT_RING) == 0 && !p.getMainHandItem().isEmpty(),
                            "and the ring comes off into the hand");
                });
                p.hurtTime = 0;
                p.setHealth(p.getMaxHealth());
                p.setPose(Pose.STANDING);
                p.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);

                // Lateness: a late reader is not drawn late there.
                CompoundTag light = LiteraryVignettes.personal(d, p.getUUID(), OwnLight.PLACE);
                light.putBoolean(OwnLightLate.KEY, true);
                LiteraryVignettes.save(d, p.getUUID(), OwnLight.PLACE, light);
                h.assertTrue(OwnLightLate.late(p) && !OwnLightLate.late(nether.getServer()).contains(p.getUUID()),
                        "late, but not drawn late in the Games");
                listed(nether, false, () -> h.assertTrue(OwnLightLate.late(nether.getServer()).contains(p.getUUID()), "out of the tag, drawn late"));
                // Everyone is told on the tick they come out of the Games, and on the tick they go back in, not at the next second's sync.
                OwnLightLate.sync(nether.getServer());
                NativeTestPlayers.packets(p);
                into(p, h.getLevel(), h.absolutePos(new BlockPos(1, 2, 1)));
                NativeTestPlayers.flush(p);
                List<LateBodiesPayload> out = lateness(p);
                h.assertTrue(!out.isEmpty() && out.get(out.size() - 1).readers().contains(p.getUUID()),
                        "out of the Games, they are drawn late from the tick they arrive: " + out);
                into(p, nether, spot);
                NativeTestPlayers.flush(p);
                List<LateBodiesPayload> in = lateness(p);
                h.assertTrue(!in.isEmpty() && !in.get(in.size() - 1).readers().contains(p.getUUID()),
                        "and back in, not from the tick they arrive: " + in);

                // Harrigan's line and the film: no service there, and the film does not follow them in.
                h.assertTrue(HarriganVignette.noService(p) && !WorkPrint.followsHere(p), "in the Games no call comes or goes, and the film is not with them");
                listed(nether, false, () -> h.assertTrue(!HarriganVignette.noService(p) && WorkPrint.followsHere(p),
                        "out of the tag the phone has service and the film follows, as anywhere away from the Overworld"));
                // A night ring begun in their own world, and they are taken into the Games while it rings: nothing more rings there, and it
                // is a missed call in the thread, without a word, for them to find when they open the phone.
                rings(d, p, 3);
                NativeTestPlayers.packets(p);
                HarriganPhones.ringing(p);
                NativeTestPlayers.flush(p);
                List<String> heard = rung(p);
                CompoundTag phone = HarriganVignette.playerState(d, p.getUUID());
                var thread = HarriganVignette.phoneState(p, null).getList("Thread", 10);
                h.assertTrue(heard.isEmpty() && phone.getInt("RingsLeft0188") == 0 && !thread.isEmpty()
                        && thread.getCompound(thread.size() - 1).getString("Text").equals("Missed call · Mr. Harrigan"),
                        "a ring under way rings no more in the Games, and goes to a missed call: heard " + heard + ", left " + phone.getInt("RingsLeft0188"));
                listed(nether, false, () -> {
                    rings(d, p, 3);
                    HarriganPhones.ringing(p);
                    NativeTestPlayers.flush(p);
                    List<String> rang = rung(p);
                    h.assertTrue(rang.size() == 1 && HarriganVignette.playerState(d, p.getUUID()).getInt("RingsLeft0188") == 2,
                            "out of the tag it rings on: heard " + rang);
                });
                rings(d, p, 0);

                // The Goatman: it is not with them there, and does not eat with them.
                CompoundTag all = d.state(GoatmanVignette.ID), players = all.getCompound("Players");
                CompoundTag goat = players.getCompound(p.getUUID().toString());
                long today = nether.getServer().overworld().getDayTime() / 24000L;
                goat.putBoolean("Haunted", true);
                goat.putLong("HauntDay", today - 1);
                goat.putLong("NextGlimpse", 0L);
                players.put(p.getUUID().toString(), goat);
                all.put("Players", players);
                d.setState(GoatmanVignette.ID, all);
                p.getInventory().add(new ItemStack(Items.BREAD, 3));
                h.assertTrue(GoatmanHaunt.haunted(d, p.getUUID()) && !GoatmanHaunt.hauntsHere(d, p), "haunted, but it is not with them in the Games");
                listed(nether, false, () -> h.assertTrue(GoatmanHaunt.hauntsHere(d, p), "out of the tag, it would be"));

                // Bly's Lady: near water in the Games she does not come for them.
                nether.setBlock(spot.offset(3, -1, 0), Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
                CompoundTag bly = LiteraryVignettes.personal(d, p.getUUID(), LabyrinthPlace.BLY_ROUTE);
                bly.putBoolean(BlyFollows.FOLLOWS, true);
                LiteraryVignettes.save(d, p.getUUID(), LabyrinthPlace.BLY_ROUTE, bly);
                h.assertTrue(!BlyFollows.carry(p) && !BlyFollows.carrying(p), "near water in the Games she does not take their arm");
                listed(nether, false, () -> h.assertTrue(BlyFollows.carry(p) && BlyFollows.carrying(p), "out of the tag she does"));
                // She no longer follows, so a step cannot begin another carry where they stand.
                bly = LiteraryVignettes.personal(d, p.getUUID(), LabyrinthPlace.BLY_ROUTE);
                bly.putBoolean(BlyFollows.FOLLOWS, false);
                LiteraryVignettes.save(d, p.getUUID(), LabyrinthPlace.BLY_ROUTE, bly);
                // Taken into another world of their own while she has them, she still has them, as she always did: still in her hold,
                // they are kept where they are.
                into(p, h.getLevel(), h.absolutePos(new BlockPos(1, 2, 1)));
                Vec3 there = p.position();
                BlyFollows.tick(new ServerTickEvent.Post(() -> true, nether.getServer()));
                h.assertTrue(BlyFollows.carrying(p) && p.level() == h.getLevel() && p.position().distanceTo(there) < .01,
                        "taken into a world of their own, she still holds them there: carrying=" + BlyFollows.carrying(p) + " at " + p.position());
                // Taken into the Games while she has them, she lets go on her next step, and her dark and her hold lift with her. They stay
                // there for the Goatman's rounds.
                into(p, nether, spot);
                Vec3 here = p.position();
                h.assertTrue(BlyFollows.carrying(p) && p.hasEffect(MobEffects.DARKNESS) && p.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),
                        "the carry, her dark and her hold are still on them as they arrive");
                BlyFollows.tick(new ServerTickEvent.Post(() -> true, nether.getServer()));
                h.assertTrue(!BlyFollows.carrying(p) && p.level() == nether && p.position().distanceTo(here) < .01,
                        "in the Games her carry is let go and they stand where they are: carrying=" + BlyFollows.carrying(p)
                                + " at " + p.position() + " in " + p.level().dimension().location());
                h.assertTrue(!p.hasEffect(MobEffects.DARKNESS) && !p.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),
                        "and her dark and her hold go with her: " + p.getActiveEffects());
            } catch (RuntimeException e) {
                NativeTestPlayers.remove(p);
                throw e;
            }
            // Two of its rounds in the Games (it goes round on each twentieth tick, after this tick's cases): nothing is eaten and nothing
            // is glimpsed.
            int from = nether.getServer().getTickCount();
            SceneReady.when(h, () -> rounds(from, nether.getServer().getTickCount()) >= 2 ? null : "two of the Goatman's rounds since tick " + from, 1, 100, () -> {
                try {
                    CompoundTag goat = GoatmanVignette.personal(d, p.getUUID());
                    h.assertTrue(p.level() == nether && p.getInventory().countItem(Items.BREAD) == 3 && goat.getInt("Eaten") == 0 && goat.getLong("NextGlimpse") == 0L,
                            "two of its rounds in the Games, and nothing is eaten or glimpsed: bread " + p.getInventory().countItem(Items.BREAD) + ", " + goat);
                    h.assertTrue(nether.getEntitiesOfClass(io.github.knaitoe.theoldesthouse.labyrinth.GoatmanFigure.class, p.getBoundingBox().inflate(24)).isEmpty(),
                            "nothing stands at the edge of their sight there");
                } finally {
                    CompoundTag all = d.state(GoatmanVignette.ID), players = all.getCompound("Players");
                    CompoundTag goat = players.getCompound(p.getUUID().toString());
                    goat.putBoolean("Haunted", false);
                    players.put(p.getUUID().toString(), goat);
                    all.put("Players", players);
                    d.setState(GoatmanVignette.ID, all);
                    NativeTestPlayers.remove(p);
                }
                h.succeed();
            });
        });
    }

    /**
     * The Goatman's rounds that have surely gone by between a case's tick {@code from} and its tick {@code now}: it goes round on every
     * twentieth tick, after the tick's cases, so a round on {@code from} itself or on {@code now} is not counted.
     */
    private static int rounds(int from, int now) {
        return Math.max(0, Math.floorDiv(now - 1, 20) - Math.floorDiv(from, 20));
    }

    /** Sets how many rings are left of Harrigan's night call for this reader. */
    private static void rings(LabyrinthData d, ServerPlayer p, int left) {
        CompoundTag line = HarriganVignette.playerState(d, p.getUUID());
        line.putInt("RingsLeft0188", left);
        HarriganVignette.savePlayerState(d, p.getUUID(), line);
    }

    /** His rings this reader has heard since last asked. */
    private static List<String> rung(ServerPlayer p) {
        List<String> out = new ArrayList<>();
        for (var sound : NativeTestPlayers.sounds(p)) {
            if (sound.getSound().value() == PhoneSounds.HIS_RING.get()) {
                out.add(sound.getSound().value().getLocation().toString());
            }
        }
        return out;
    }

    /** Who is late, as each payload this reader's game was sent since last asked told it. */
    private static List<LateBodiesPayload> lateness(ServerPlayer p) {
        List<LateBodiesPayload> out = new ArrayList<>();
        for (Packet<?> packet : NativeTestPlayers.packets(p)) {
            collect(packet, out);
        }
        return out;
    }

    private static void collect(Packet<?> packet, List<LateBodiesPayload> out) {
        if (packet instanceof ClientboundCustomPayloadPacket custom && custom.payload() instanceof LateBodiesPayload late) {
            out.add(late);
        }
        if (packet instanceof BundlePacket<?> bundle) {
            for (var child : bundle.subPackets()) {
                collect(child, out);
            }
        }
    }

    // ------------------------------------------------------------------------------------------------ keepsakes and artifacts
    @GameTest(template = "empty", batch = BATCH, timeoutTicks = 1200)
    public static void theKeepsakesWorkOnlyInTheReadersOwnWorld(GameTestHelper h) {
        BlockPos spot = new BlockPos(6000, 100, 6300);
        ServerPlayer p = NativeTestPlayers.survival(h, "keeper_in_the_games");
        inTheGames(h, spot, nether -> {
            try {
                h.assertTrue(nether.registryAccess().registryOrThrow(Registries.DIMENSION_TYPE).getTag(Neighbours.NOT_OWN_WORLD)
                                .map(t -> t.size() == 0).orElse(false),
                        "the House declares #the_oldest_house:not_own_world, empty, for a neighbour to fill");
                h.assertTrue(HouseDimensions.ownWorld(h.getLevel()) && !Neighbours.notOwnWorld(h.getLevel()), "the Overworld is the reader's own world");
                into(p, nether, spot);
                h.assertTrue(!HouseDimensions.ownWorld(nether) && !Keepsakes.ownWorld(p) && !Artifacts.ownWorld(p),
                        "a world in the tag is not the reader's own, for the keepsakes and the artifacts alike");
                listed(nether, false, () -> h.assertTrue(HouseDimensions.ownWorld(nether) && Keepsakes.ownWorld(p) && Artifacts.ownWorld(p),
                        "out of the tag, the same world is theirs"));

                // Bud's note: ice is ordinary ground and snow bears them only in their own world.
                p.getInventory().add(LiteraryVignettes.mark(p, Artifacts.Kind.NOTE.place(), Artifacts.Kind.NOTE.key, new ItemStack(Artifacts.Kind.NOTE.item())));
                h.assertTrue(Artifacts.friction(p, .98F) == .98F && !Artifacts.snowBears(p), "in the Games the note does nothing for them");
                listed(nether, false, () -> h.assertTrue(Artifacts.friction(p, .98F) == Artifacts.NOTE_GRIP && Artifacts.snowBears(p),
                        "out of the tag ice grips and snow bears"));

                // The scrawled note: nothing dead is kept from a door near them there.
                p.getInventory().add(LiteraryVignettes.mark(p, Artifacts.Kind.SCRAWL.place(), Artifacts.Kind.SCRAWL.key, new ItemStack(Artifacts.Kind.SCRAWL.item())));
                var zombie = EntityType.ZOMBIE.create(nether);
                zombie.moveTo(spot.getX() + 2.5, spot.getY(), spot.getZ() + .5);
                var breaking = new LivingDestroyBlockEvent(zombie, spot.offset(3, 0, 0), Blocks.OAK_DOOR.defaultBlockState());
                NeoForge.EVENT_BUS.post(breaking);
                h.assertTrue(!breaking.isCanceled(), "in the Games the note keeps no zombie from a door");
                listed(nether, false, () -> {
                    var again = new LivingDestroyBlockEvent(zombie, spot.offset(3, 0, 0), Blocks.OAK_DOOR.defaultBlockState());
                    NeoForge.EVENT_BUS.post(again);
                    h.assertTrue(again.isCanceled(), "out of the tag it does");
                });

                // The children's chalk is the House's: a fall in the Games is not forgiven.
                p.getInventory().add(LiteraryVignettes.mark(p, LabyrinthPlace.FEET_UP, FeetUp.CHALK_KEY, new ItemStack(Items.WHITE_DYE)));
                var fall = new LivingFallEvent(p, 5F, 1F);
                NeoForge.EVENT_BUS.post(fall);
                h.assertTrue(fall.getDamageMultiplier() == 1F && Keepsakes.record(p).getInt("ChalkKept") == 0, "a fall in the Games costs what it costs");

                // The kept bike is not ridden there.
                h.assertTrue(KeptBikeItem.NOT_OWN_WORLD.equals(KeptBikeItem.refuse(p)), "the bike is not ridden in the Games, and is not \"in\" there: " + KeptBikeItem.refuse(p));
                listed(nether, false, () -> h.assertTrue(KeptBikeItem.refuse(p) == null, "out of the tag it is"));

                // The Lake House flashlight lights nothing there, and is put out on going back in.
                for (BlockPos at : BlockPos.betweenClosed(spot.offset(-4, 0, 3), spot.offset(4, 4, 3))) {
                    nether.setBlock(at, Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
                ItemStack torch = new ItemStack(LakeHouseRegistry.FOUND_FLASHLIGHT.get());
                CustomData.update(DataComponents.CUSTOM_DATA, torch, t -> t.putUUID(LiteraryVignettes.OWNER, p.getUUID()));
                p.getInventory().clearContent();
                p.setItemInHand(InteractionHand.MAIN_HAND, torch);
                into(p, nether, spot);
                AABB ahead = new AABB(spot.offset(-1, 0, 0).getCenter(), spot.offset(1, 3, 3).getCenter()).inflate(.5);
                // The beam moves on a body's every fourth tick; the level counts a body's ticks, so the case puts it on one.
                beamTick(p);
                h.assertTrue(lights(nether, ahead) == 0, "in the Games the flashlight lights nothing");
                listed(nether, false, () -> {
                    beamTick(p);
                    h.assertTrue(lights(nether, ahead) == 1, "out of the tag its beam lights the wall ahead");
                });
                beamTick(p);
                h.assertTrue(lights(nether, ahead) == 0, "and back in the Games it is put out");
                p.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);

                // The stela's stone: nothing gives to it there.
                BlockPos sand = spot.offset(2, 0, 1);
                nether.setBlock(sand, Blocks.SAND.defaultBlockState(), Block.UPDATE_CLIENTS);
                nether.setBlock(sand.south(), Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
                into(p, nether, spot);
                ItemStack stone = DreamStela.make(p.getUUID(), 3, "patient", "smooth", false);
                NativeTestPlayers.messages(p);
                h.assertTrue(StelaPassage.press(p, stone, sand, InteractionHand.MAIN_HAND) == InteractionResult.CONSUME, "the stone is pressed");
                NativeTestPlayers.flush(p);
                var said = NativeTestPlayers.messages(p);
                h.assertTrue(said.contains("Nothing here gives to it.") && nether.getBlockState(sand).is(Blocks.SAND), "in the Games nothing gives to it: " + said);
                listed(nether, false, () -> {
                    StelaPassage.press(p, stone, sand, InteractionHand.MAIN_HAND);
                    NativeTestPlayers.flush(p);
                    var heard = NativeTestPlayers.messages(p);
                    h.assertTrue(heard.stream().noneMatch("Nothing here gives to it."::equals) && !heard.isEmpty(),
                            "out of the tag the fill is tried: " + heard);
                });

                // Something that means them harm, close behind them, out of their sight and after them.
                into(p, nether, spot);
                var behind = EntityType.ZOMBIE.create(nether);
                behind.setNoAi(true);
                behind.moveTo(spot.getX() + .5, spot.getY(), spot.getZ() - 2.5);
                nether.addFreshEntity(behind);
                behind.setTarget(p);
                try {
                    // The Goatman's counter counts nobody through the walls there, and keeps no watch over a sleeping keeper.
                    ItemStack counter = TallyCounterItem.forReader(p.getUUID());
                    p.setItemInHand(InteractionHand.MAIN_HAND, counter);
                    NativeTestPlayers.messages(p);
                    TallyCounterItem.click(p, counter);
                    NativeTestPlayers.flush(p);
                    var clicked = NativeTestPlayers.messages(p);
                    h.assertTrue(clicked.contains(TallyCounterItem.NOT_HERE) && clicked.stream().noneMatch(m -> m.contains("out there")),
                            "in the Games the counter clicks and counts nobody: " + clicked);
                    listed(nether, false, () -> {
                        int out = TallyCounterItem.nearbyHostiles(p, TallyCounterItem.OUT_THERE).size();
                        TallyCounterItem.click(p, counter);
                        NativeTestPlayers.flush(p);
                        var counted = NativeTestPlayers.messages(p);
                        h.assertTrue(out >= 1 && counted.contains("Click. " + TallyCounterItem.words(out) + " out there."),
                                "out of the tag it counts what is out there: " + counted);
                    });
                    p.startSleeping(spot);
                    TallyCounterItem.watch(p);
                    h.assertTrue(p.isSleeping() && TallyCounterItem.lastWarning(p.getUUID()) == -1L,
                            "asleep in the Games with something close, nothing wakes them");
                    listed(nether, false, () -> {
                        TallyCounterItem.watch(p);
                        h.assertTrue(!p.isSleeping() && TallyCounterItem.lastWarning(p.getUUID()) == nether.getGameTime(),
                                "out of the tag the counter wakes them");
                    });
                    p.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                    into(p, nether, spot);

                    // The church key keeps no drowned off them there; the essay page keeps out of the Games too.
                    p.getInventory().add(new ItemStack(DrownedTownRegistry.CHURCH_KEY.get()));
                    var drowned = EntityType.DROWNED.create(nether);
                    drowned.moveTo(spot.getX() + 2.5, spot.getY(), spot.getZ() + .5);
                    drowned.setTarget(p);
                    ChurchKeyItem.onEntityTick(new EntityTickEvent.Post(drowned));
                    h.assertTrue(!ChurchKeyItem.protects(p) && drowned.getTarget() == p, "in the Games a drowned keeps after the key's bearer");
                    listed(nether, false, () -> {
                        h.assertTrue(ChurchKeyItem.protects(p), "out of the tag the key masks them");
                        ChurchKeyItem.onEntityTick(new EntityTickEvent.Post(drowned));
                        h.assertTrue(drowned.getTarget() == null, "and the drowned after them gives up");
                        drowned.setTarget(p);
                        h.assertTrue(drowned.getTarget() == null, "nor takes them up again");
                    });
                    drowned.discard();
                    p.getInventory().clearContent();

                    // The planchette points at nothing that stalks them there.
                    h.assertTrue(behind.getTarget() == p && Artifacts.unseenHarm(p) == null, "in the Games the planchette points at nothing");
                    listed(nether, false, () -> h.assertTrue(Artifacts.unseenHarm(p) != null, "out of the tag it drifts toward what is after them"));
                } finally {
                    behind.discard();
                    TallyCounterItem.forget(p.getUUID());
                }
            } finally {
                NativeTestPlayers.remove(p);
            }
            h.succeed();
        });
    }

    private static void beamTick(ServerPlayer p) {
        p.tickCount = (p.tickCount / 4 + 1) * 4;
        p.doTick();
    }

    private static int lights(ServerLevel level, AABB box) {
        int n = 0;
        for (BlockPos at : BlockPos.betweenClosed(BlockPos.containing(box.minX, box.minY, box.minZ), BlockPos.containing(box.maxX, box.maxY, box.maxZ))) {
            if (level.getBlockState(at).is(Blocks.LIGHT)) {
                n++;
            }
        }
        return n;
    }

    // ------------------------------------------------------------------------------------------------ Not Yet
    @GameTest(template = "empty", batch = BATCH, timeoutTicks = 1200)
    public static void aReaderTakenOutOfTheWardWhileMendedIsLetGoWhereTheyAre(GameTestHelper h) {
        BlockPos spot = new BlockPos(6000, 100, 6400);
        ServerPlayer p = NativeTestPlayers.survival(h, "taken_from_the_ward");
        inTheGames(h, spot, nether -> {
            LabyrinthData d = LabyrinthData.get(nether.getServer());
            into(p, nether, spot);
            Vec3 stood = p.position();
            // The machine took them in its ward, and before it was done something else took them into the Games.
            NotYetMending.refuse(p, spot.offset(0, 0, -2), "fall");
            h.assertTrue(NotYetMending.held(p), "the machine holds them");
            SceneReady.when(h, () -> NotYetMending.held(p) ? "the machine letting them go" : null, 1, 200, () -> {
                try {
                    CompoundTag own = LiteraryVignettes.personal(d, p.getUUID(), LabyrinthPlace.NOT_YET);
                    h.assertTrue(!NotYetMending.held(p) && own.getInt("Mended") == 1 && own.contains("CountFrom"),
                            "out of the ward's world, the mending is finished at once, with its seam: " + own);
                    h.assertTrue(NotYetMending.cot(own) != null,
                            "and with their cot, as one who wakes on it has, so the machine has one to keep them on when they lie down: " + own);
                    h.assertTrue(p.level() == nether && p.position().distanceTo(stood) < .5 && !p.isSleeping(),
                            "and they are left where they are, standing, never laid on a table there: " + p.position());
                } finally {
                    NativeTestPlayers.remove(p);
                }
                h.succeed();
            });
        });
    }
    /**
     * The path that matters in play: a reader lying on the mending table in the ward, the arms at work over them, taken into the Games
     * before the machine is done. The game's own move between worlds does not get a sleeper up, so the machine does on its next step: they
     * stand where they arrived, the arms in the ward fold, and the seam and the cot are theirs.
     */
    @GameTest(template = "empty", batch = WARD, timeoutTicks = 1200)
    public static void aReaderTakenOffTheMendingTableIsLetGoWhereTheyArrive(GameTestHelper h) {
        BlockPos spot = new BlockPos(6000, 100, 6500);
        LabyrinthPlace place = LabyrinthPlace.NOT_YET;
        ward = new LiteraryTests.Fixture(h);
        ServerPlayer p = ward.player("taken_off_the_table", place);
        ServerLevel l = ward.level(place);
        BlockPos b = ward.base(place);
        var r = place.room();
        ward.chunks.request(l, new AABB(b.getX() + r.minX(), b.getY() + r.minY(), b.getZ() + r.minZ(), b.getX() + r.maxX() + 1, b.getY() + r.maxY() + 1, b.getZ() + r.maxZ() + 1));
        inTheGames(h, spot, nether -> SceneReady.when(h, () -> ward.chunks.ready() ? null : "the ward's chunks", 1, 400, () -> {
            // Down the ward's aisle, well off the hatch, where the machine takes them.
            p.teleportTo(l, b.getX() + .5, b.getY(), b.getZ() - 20.5, 180F, 0F);
            NotYetMending.refuse(p, b, "fall");
            SceneReady.when(h, () -> NotYetMending.onTable(p.getUUID()) ? null : "the reader on the mending table", 1, 200, () -> {
                h.assertTrue(p.getSleepingPos().filter(b.offset(NotYetSite.TABLE)::equals).isPresent(), "lying on the table: " + p.getSleepingPos());
                for (BlockPos arm : NotYetSite.ARMS) {
                    h.assertTrue(armStage(l, b, arm) == 2, "the arms at work over them");
                }
                into(p, nether, spot);
                Vec3 arrived = p.position();
                h.assertTrue(NotYetMending.held(p) && p.isSleeping(), "still the machine's, and still lying, as they arrive: held "
                        + NotYetMending.held(p) + ", sleeping " + p.isSleeping());
                SceneReady.when(h, () -> NotYetMending.held(p) ? "the machine letting them go" : null, 1, 40, () -> {
                    CompoundTag own = ward.own(p, place);
                    h.assertTrue(!p.isSleeping(), "they are up, not lying in the Games: " + p.getSleepingPos());
                    for (BlockPos arm : NotYetSite.ARMS) {
                        h.assertTrue(armStage(l, b, arm) == 0, "the arms in the ward folded: " + armStage(l, b, arm) + " at " + arm);
                    }
                    h.assertTrue(own.getInt("Mended") == 1 && own.contains("CountFrom") && NotYetMending.cot(own) != null,
                            "with their seam and their cot: " + own);
                    h.assertTrue(p.level() == nether && p.position().distanceTo(arrived) < .5,
                            "standing where they arrived: " + p.position() + " in " + p.level().dimension().location());
                    h.succeed();
                });
            });
        }));
    }

    private static int armStage(ServerLevel l, BlockPos b, BlockPos arm) {
        return l.getBlockState(b.offset(arm)).getValue(NotYetBlock.STAGE);
    }
}
