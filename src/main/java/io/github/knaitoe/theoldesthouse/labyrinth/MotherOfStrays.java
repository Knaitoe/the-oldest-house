package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.house.HouseWatchers;
import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.portal.DimensionTransition;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.item.ItemExpireEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Possessive care: gradual deformation, concealed stalking, violent satiation, and a merciful way through. */
public final class MotherOfStrays {
    public static final String ID = "mother_of_strays";
    public static final String FRAME = "the_oldest_house_mother_shelf";
    public static final String PET = "the_oldest_house_mother_pet";
    private static final String ENTRY = "MotherEntry";
    private static final String DROPPER = "MotherDropper";
    private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    public static final BlockPos CHAIR = new BlockPos(0, 0, -12);
    public static final BlockPos SHELF_BELL = new BlockPos(4, 0, -4);
    public static final String RELEASED = "the_oldest_house_mother_released";
    private static final String LITTLE_DOG = "the_oldest_house_bandaged_dog";
    private static final Map<UUID, Long> NEXT_LURK = new HashMap<>();
    private static final Map<UUID, Integer> LAST_WARNING = new HashMap<>();
    private static final Map<UUID, Long> LAST_INTERACTION = new HashMap<>();
    private static final Map<UUID, MotherEntity> FOLLOWERS = new HashMap<>();
    private static long shelfRevision = -1;
    private static int shelfPage;

    private MotherOfStrays() {}

    public record Shelf(BlockPos relative, Direction facing) {}

    private static AABB box(BlockPos lower, BlockPos upper) {
        return new AABB(lower.getX(), lower.getY(), lower.getZ(), upper.getX(), upper.getY(), upper.getZ());
    }

    public static List<Shelf> shelves() {
        List<Shelf> result = new ArrayList<>();
        for (int y : new int[] {1, 3, 5}) {
            for (int z = -3; z >= -23; z -= 2) {
                result.add(new Shelf(new BlockPos(-9, y, z), Direction.EAST));
                result.add(new Shelf(new BlockPos(9, y, z), Direction.WEST));
            }
            for (int x = -8; x <= 8; x += 2) {
                result.add(new Shelf(new BlockPos(x, y, -24), Direction.SOUTH));
            }
        }
        return List.copyOf(result);
    }

    public static void build(MinecraftServer server, ServerLevel level, BlockPos base) {
        BlockState wall = Blocks.DARK_OAK_PLANKS.defaultBlockState();
        LabyrinthBuilder.room(level, base, -10, 10, 13, -25, -1,
                wall, Blocks.STRIPPED_SPRUCE_WOOD.defaultBlockState(), Blocks.DARK_OAK_PLANKS.defaultBlockState());
        // Shelves run along the walls. The middle is a clear route back to the door.
        for (int x : new int[] {-9, 9}) {
            for (int z = -2; z >= -24; z--) {
                level.setBlock(base.offset(x, 0, z), Blocks.DARK_OAK_SLAB.defaultBlockState(), FLAGS);
            }
        }
        for (int z = -7; z >= -15; z--) {
            for (int x = -2; x <= 2; x++) {
                level.setBlock(base.offset(x, 0, z), Blocks.BROWN_CARPET.defaultBlockState(), FLAGS);
            }
        }
        level.setBlock(base.offset(CHAIR), Blocks.DARK_OAK_STAIRS.defaultBlockState(), FLAGS);
        level.setBlock(base.offset(CHAIR).west(), Blocks.DARK_OAK_TRAPDOOR.defaultBlockState(), FLAGS);
        level.setBlock(base.offset(CHAIR).east(), Blocks.DARK_OAK_TRAPDOOR.defaultBlockState(), FLAGS);
        level.setBlock(base.offset(0, 0, -13), Blocks.DARK_OAK_PLANKS.defaultBlockState(), FLAGS);
        // An upper gallery looks down into a dry well. The ladder is an intervention route, not a trap.
        for (int x = -2; x <= 2; x++) for (int z = -18; z >= -22; z--) {
            level.setBlock(base.offset(x, -4, z), Blocks.DEEPSLATE.defaultBlockState(), FLAGS);
            for (int y = -3; y <= 0; y++) level.setBlock(base.offset(x, y, z), Blocks.AIR.defaultBlockState(), FLAGS);
        }
        for (int x = -6; x <= -1; x++) for (int z = -20; z >= -23; z--)
            level.setBlock(base.offset(x, 8, z), Blocks.DARK_OAK_PLANKS.defaultBlockState(), FLAGS);
        for (int y = 0; y <= 8; y++) {
            level.setBlock(base.offset(-6, y, -23), Blocks.DARK_OAK_LOG.defaultBlockState(), FLAGS);
            level.setBlock(base.offset(-7, y, -23), Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.WEST), FLAGS);
        }
        level.setBlock(base.offset(SHELF_BELL), Blocks.BELL.defaultBlockState(), FLAGS);
        BlockPos note = base.offset(-4, 0, -4);
        level.setBlock(note, Blocks.LECTERN.defaultBlockState()
                .setValue(LecternBlock.FACING, Direction.SOUTH).setValue(LecternBlock.HAS_BOOK, true), FLAGS);
        if (level.getBlockEntity(note) instanceof LecternBlockEntity lectern) lectern.setBook(denNote());
        LabyrinthBuilder.hangLantern(level, base.offset(-6, 13, -6), true);
        LabyrinthBuilder.hangLantern(level, base.offset(6, 13, -18), true);
        LabyrinthBuilder.entrance(level, base, wall, Blocks.STRIPPED_SPRUCE_WOOD.defaultBlockState(), wall);
        LabyrinthBuilder.doors(level, base, LabyrinthPlace.MOTHER_DEN);

        MotherCollection collection = MotherCollection.get(server);
        if (collection.seedOnce()) {
            keepKeepsake(collection, server, Items.COMPASS.getDefaultInstance(), "A compass somebody stopped trusting");
            keepKeepsake(collection, server, Items.RED_DYE.getDefaultInstance(), "A small red collar");
            keepKeepsake(collection, server, Items.IRON_NUGGET.getDefaultInstance(), "A bent teaspoon");
        }
        refreshShelves(level, base, collection);
        if (!collection.banished()) ensureKeeper(level, base);
        ensureStrays(level, base);
        refreshPets(level, base, collection);
    }

    private static void keepKeepsake(MotherCollection collection, MinecraftServer server, ItemStack stack, String name) {
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        collection.keepItem(stack, server.registryAccess(), null, server.overworld().getGameTime());
    }

    public static ItemStack denNote() {
        return VignetteYields.mark(HouseWriting.book("Things kept", "No name", HouseWriting.WritingStyle.PLAIN,
                List.of("A room full of things is not always a room full of thieves.\n\n"
                        + "She remembers the things nobody came back for. Some still have their names.",
                        "Take one thing, if you must. Only one each time you come.\n\n"
                        + "If she follows, bring it back, or put something you have loved into her hands. "
                        + "Do not offer rubble.\n\nThe animals are waiting too.",
                        "I watched her hands grow wrong while she waited. Then she took a thing back, "
                        + "hard enough to hurt, and her face was almost gentle again.\n\nShe called it a kindness.",
                        "A thing returned. A thing freely given. A life allowed to come home.\n\n"
                        + "Perhaps those can teach her another kindness. If she learns it, bring this record, "
                        + "bow your head, and ask her to leave.\n\nThe little dog is still alive. There is a ladder beside the gallery.")), ID);
    }

    private static MotherEntity ensureKeeper(ServerLevel level, BlockPos base) {
        for (Entity entity : level.getAllEntities())
            if (entity instanceof MotherEntity visitor && LabyrinthEncounters.ambient(visitor)) return visitor;
        for (MotherEntity mother : level.getEntitiesOfClass(MotherEntity.class,
                box(base.offset(-10, -4, -25), base.offset(11, 14, 1)))) {
            if (mother.following() == null) return mother;
        }
        MotherEntity mother = MotherRegistry.MOTHER.get().create(level);
        if (mother == null) throw new IllegalStateException("Mother entity could not be created");
        mother.moveTo(base.getX() + 0.5D, base.getY(), base.getZ() - 10.5D, 0.0F, 0.0F);
        level.addFreshEntity(mother);
        return mother;
    }

    private static void ensureStrays(ServerLevel level, BlockPos base) {
        AABB box = box(base.offset(-10, -1, -25), base.offset(11, 8, 1));
        if (level.getEntitiesOfClass(Mob.class, box, mob -> mob.getTags().contains(PET)
                && !mob.getPersistentData().hasUUID(ENTRY)).isEmpty()) {
            for (int i = 0; i < 3; i++) {
                Cat cat = EntityType.CAT.create(level);
                if (cat == null) continue;
                cat.moveTo(base.getX() - 3.5D + i * 3.0D, base.getY(), base.getZ() - 18.5D, 0.0F, 0.0F);
                cat.setCustomName(Component.literal(new String[] {"A cat with no caller", "A cat from the courtyard", "A cat nobody counted"}[i]));
                prepareKeptPet(cat);
                level.addFreshEntity(cat);
            }
        }
        MotherCollection collection = MotherCollection.get(level.getServer());
        if (!collection.dogGone() && !collection.dogRescued()
                && level.getEntitiesOfClass(MotherPekingese.class, box, pet -> pet.getTags().contains(LITTLE_DOG)).isEmpty()) {
            MotherPekingese dog = MotherRegistry.PEKINGESE.get().create(level);
            if (dog != null) {
                dog.moveTo(base.getX() + 3.5D, base.getY(), base.getZ() - 10.5D, 0.0F, 0.0F);
                dog.setCustomName(Component.literal("The little Pekingese"));
                dog.addTag(LITTLE_DOG);
                prepareKeptPet(dog);
                level.addFreshEntity(dog);
            }
        }
    }

    private static void prepareKeptPet(Mob pet) {
        pet.addTag(PET);
        pet.setPersistenceRequired();
        pet.setInvulnerable(true);
        if (pet instanceof TamableAnimal tame) {
            tame.setOwnerUUID(null);
            tame.setOrderedToSit(false);
        }
        if (pet instanceof AbstractHorse horse) horse.setOwnerUUID(null);
    }

    private static void refreshShelves(ServerLevel level, BlockPos base, MotherCollection collection) {
        for (ItemFrame frame : level.getEntitiesOfClass(ItemFrame.class,
                box(base.offset(-11, -1, -26), base.offset(12, 8, 1)))) {
            if (frame.getTags().contains(FRAME)) frame.discard();
        }
        List<MotherCollection.Entry> items = collection.visible();
        List<Shelf> shelves = shelves();
        int pages = shelfPages(collection);
        shelfPage = Math.floorMod(shelfPage, pages);
        for (int i = 0; i < shelves.size(); i++) {
            Shelf shelf = shelves.get(i);
            ItemFrame frame = new ItemFrame(level, base.offset(shelf.relative()), shelf.facing());
            frame.addTag(FRAME);
            CompoundTag frameData = frame.saveWithoutId(new CompoundTag());
            frameData.putBoolean("Fixed", true);
            frame.load(frameData);
            frame.setInvulnerable(true);
            int index = shelfPage * shelves.size() + i;
            if (index < items.size()) {
                MotherCollection.Entry entry = items.get(index);
                frame.setItem(entry.item(level.registryAccess()), false);
                frame.getPersistentData().putUUID(ENTRY, entry.id);
            }
            level.addFreshEntity(frame);
        }
        shelfRevision = collection.revision();
    }

    private static void refreshPets(ServerLevel level, BlockPos base, MotherCollection collection) {
        AABB box = box(base.offset(-10, -4, -25), base.offset(11, 14, 1));
        List<MotherCollection.Entry> pets = collection.all().stream().filter(e -> e.pet).toList();
        int end = Math.max(0, pets.size() - shelfPage * 12);
        int start = Math.max(0, end - 12);
        List<MotherCollection.Entry> page = pets.subList(start, end);
        Map<UUID, Mob> displayed = new HashMap<>();
        for (Mob pet : level.getEntitiesOfClass(Mob.class, box, mob -> mob.getTags().contains(PET))) {
            CompoundTag data = pet.getPersistentData();
            if (data.hasUUID(ENTRY)) {
                UUID id = data.getUUID(ENTRY);
                if (page.stream().noneMatch(entry -> entry.id.equals(id))) pet.discard();
                else displayed.put(id, pet);
            }
        }
        // The bell pages through every archived pet, twelve live bodies at a time.
        for (int i = start; i < end; i++) {
            MotherCollection.Entry entry = pets.get(i);
            if (displayed.containsKey(entry.id)) continue;
            Entity entity = restorePetEntity(level, entry);
            if (!(entity instanceof Mob pet)) continue;
            int offset = i - start;
            pet.moveTo(base.getX() - 6.5D + (offset % 6) * 2.0D, base.getY(),
                    base.getZ() - 11.5D - (offset / 6) * 3.0D, 0.0F, 0.0F);
            prepareKeptPet(pet);
            pet.restrictTo(base.offset(0, 0, -12), 8);
            pet.getPersistentData().putUUID(ENTRY, entry.id);
            level.addFreshEntity(pet);
        }
    }

    private static int shelfPages(MotherCollection collection) {
        int itemPages = (collection.visible().size() + shelves().size() - 1) / shelves().size();
        int petPages = (int) ((collection.all().stream().filter(e -> e.pet).count() + 11) / 12);
        return Math.max(1, Math.max(itemPages, petPages));
    }

    @Nullable
    public static Entity restorePetEntity(ServerLevel level, MotherCollection.Entry entry) {
        CompoundTag copy = entry.contents.copy();
        copy.remove("UUID");
        copy.remove("Passengers");
        // Equipment and carried inventory already drop through the ordinary death pipeline.
        // Recovering the animal must not also reproduce those drops.
        for (String key : new String[]{"Items", "Inventory", "HandItems", "ArmorItems",
                "SaddleItem", "ArmorItem", "body_armor_item"}) copy.remove(key);
        copy.putBoolean("ChestedHorse", false);
        copy.putBoolean("Saddle", false);
        copy.putShort("DeathTime", (short) 0);
        copy.putShort("HurtTime", (short) 0);
        copy.putFloat("Health", 20.0F);
        Entity restored = EntityType.loadEntityRecursive(copy, level, entity -> entity);
        if (restored instanceof LivingEntity living) {
            living.setHealth(living.getMaxHealth());
            living.deathTime = 0;
            living.hurtTime = 0;
        }
        return restored;
    }

    // Loss is recorded across all dimensions, even before the manor exists.
    public static void onItemExpire(ItemExpireEvent event) {
        ItemEntity item = event.getEntity();
        if (event.getExtraLife() > 0 || !(item.level() instanceof ServerLevel level)) return;
        CompoundTag tag = item.getPersistentData();
        UUID owner = tag.hasUUID(DROPPER) ? tag.getUUID(DROPPER) : null;
        MotherCollection collection = MotherCollection.get(level.getServer());
        if (collection.banished()) return;
        if (collection.keepItem(item.getItem(), level.registryAccess(),
                owner, level.getGameTime()) != null) item.setItem(ItemStack.EMPTY);
    }

    public static void onItemToss(ItemTossEvent event) {
        if (!event.getPlayer().level().isClientSide) recordDropOwner(event.getEntity(), event.getPlayer().getUUID());
    }

    /** Also used by vignette deaths that deliberately override keepInventory. */
    public static void recordDropOwner(ItemEntity item, UUID player) {
        item.getPersistentData().putUUID(DROPPER, player);
    }

    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        for (ItemEntity item : event.getDrops()) recordDropOwner(item, player.getUUID());
    }

    public static void onPetDeath(LivingDeathEvent event) {
        LivingEntity pet = event.getEntity();
        if (!(pet.level() instanceof ServerLevel level) || pet.getTags().contains(PET)) return;
        if (MotherCollection.get(level.getServer()).banished()) return;
        UUID owner = pet instanceof TamableAnimal tame && tame.isTame() ? tame.getOwnerUUID()
                : pet instanceof AbstractHorse horse && horse.isTamed() ? horse.getOwnerUUID() : null;
        if (owner == null) return;
        CompoundTag saved = new CompoundTag();
        if (!pet.save(saved)) return;
        MotherCollection.get(level.getServer()).keepPet(pet.getUUID(), saved, owner, pet.getName().getString());
    }

    public static void onArrive(ServerPlayer player, LabyrinthPlace place) {
        MotherCollection collection = MotherCollection.get(player.server);
        boolean here = place == LabyrinthPlace.MOTHER_DEN;
        collection.presence(player.getUUID(), here);
        if (here) {
            say(player, "I keep what nobody comes back for.");
            BlockPos base = base(player.server);
            if (base != null) {
                refreshShelves(player.serverLevel(), base, collection);
                refreshPets(player.serverLevel(), base, collection);
            }
        }
    }

    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (interact(event.getEntity(), event.getTarget(), event.getEntity().getItemInHand(event.getHand()))) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (interact(event.getEntity(), event.getTarget(), event.getEntity().getItemInHand(event.getHand()))) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    private static boolean interact(Player actor, Entity target, ItemStack held) {
        boolean ours = target instanceof MotherEntity || target.getTags().contains(FRAME) || target.getTags().contains(PET);
        if (!ours) return false;
        if (!(actor instanceof ServerPlayer player) || actor.isSpectator()) return true;
        long now = player.serverLevel().getGameTime();
        if (now - LAST_INTERACTION.getOrDefault(player.getUUID(), -20L) < 4) return true;
        LAST_INTERACTION.put(player.getUUID(), now);
        MotherCollection collection = MotherCollection.get(player.server);
        if (target instanceof MotherEntity) {
            boolean settledBefore=collection.salved()||collection.banished();
            if (player.isShiftKeyDown() && ID.equals(VignetteYields.of(held)) && collection.banish()) {
                held.shrink(1);
                banishFromHouse(player.server);
                say(player, "Then I will stop keeping you.");
            } else if (collection.dogThreatOwner() != null
                    && collection.offer(player.getUUID(), held, player.registryAccess(), now)) {
                releaseLittleDog(player, collection);
            } else if (collection.returnItem(player.getUUID(), held, player.registryAccess())) say(player, "Then I will keep it safe again.");
            else if (collection.trade(player.getUUID(), held, player.registryAccess(), now)) {
                clearClaimInInventory(player);
                say(player, "You know what it is to keep something. Take it home.");
            } else if (collection.offer(player.getUUID(), held, player.registryAccess(), now)) {
                say(player, collection.salved() ? "A thing returned. A thing given. A life brought home. I remember now."
                        : "You gave this without asking for anything back?");
            } else if (collection.debt(player.getUUID()) != null) say(player, "Something loved. Not something you can spare.");
            else if (collection.salved()) say(player, "Bring the record and bow your head, if you mean for me to leave.");
            else say(player, "Look carefully. Somebody did love these once.");
            if(!settledBefore&&(collection.salved()||collection.banished()))WitnessAccount.resolve(player,WitnessAccount.Story.MOTHER,"salved");
            else if(settledBefore&&player.isShiftKeyDown()&&held.isEmpty())WitnessAccount.resolve(player,WitnessAccount.Story.MOTHER,"aftermath");
        } else if (target instanceof ItemFrame frame) {
            if (!inDen(player)) return true;
            CompoundTag tag = frame.getPersistentData();
            if (!tag.hasUUID(ENTRY)) {
                say(player, "There is a place here for what has not been lost yet.");
                return true;
            }
            ItemStack returned = collection.claimItem(player.getUUID(), tag.getUUID(ENTRY), player.registryAccess());
            if (returned.isEmpty()) say(player, "One thing. And we have not settled the last one.");
            else {
                frame.setItem(ItemStack.EMPTY, false);
                giveWithoutLoss(player, returned);
                say(player, collection.salved() || collection.banished() ? "Take it home." : "You will bring it back, won't you?");
            }
        } else if (target instanceof Mob pet) {
            if (target.getTags().contains(LITTLE_DOG)) {
                if (collection.salved() || collection.banished()
                        || collection.offer(player.getUUID(), held, player.registryAccess(), now)) {
                    releaseLittleDog(player, collection);
                } else if (collection.beginDogThreat(player.getUUID())) {
                    say(player, "You would take this one? I can be kind to it. It need not stay lost.");
                    player.displayClientMessage(Component.literal(
                            "She gathers the dog into her hands. You can still offer something loved. The ladder reaches the gallery.")
                            .withStyle(ChatFormatting.DARK_GRAY), false);
                } else say(player, "Something loved, while there is still time.");
                return true;
            }
            CompoundTag tag = pet.getPersistentData();
            if (!tag.hasUUID(ENTRY)) {
                say(player, "No one came for this one.");
                return true;
            }
            UUID id = tag.getUUID(ENTRY);
            MotherCollection.Entry entry = collection.entry(id);
            if (entry == null || !collection.canRecoverPet(player.getUUID(), id, held, now)) {
                say(player, entry != null && !player.getUUID().equals(entry.owner)
                        ? "This one remembers somebody else." : "Give me something you would miss, and call them again.");
                return true;
            }
            Entity restored = restorePetEntity(player.serverLevel(), entry);
            if (!(restored instanceof Mob living)) return true;
            Vec3 restoredSpot = nearbyStandingSpot(player.serverLevel(), player);
            living.moveTo(restoredSpot.x, restoredSpot.y, restoredSpot.z, player.getYRot(), 0.0F);
            living.removeTag(PET);
            living.addTag(RELEASED);
            living.setInvulnerable(false);
            living.setPersistenceRequired();
            if (living instanceof TamableAnimal tame) {
                tame.setTame(true, true);
                tame.setOwnerUUID(player.getUUID());
                tame.setOrderedToSit(false);
            }
            if (living instanceof AbstractHorse horse) horse.setOwnerUUID(player.getUUID());
            if (player.serverLevel().addFreshEntity(living)
                    && collection.recoverPet(player.getUUID(), id, held, player.registryAccess(), now)) {
                pet.discard();
                say(player, "Then do not make me keep them twice.");
            } else living.discard();
        }
        return true;
    }

    private static void releaseLittleDog(ServerPlayer player, MotherCollection collection) {
        for (Entity entity : player.serverLevel().getAllEntities()) {
            if (!(entity instanceof MotherPekingese dog) || !dog.getTags().contains(LITTLE_DOG) || !dog.isAlive()) continue;
            dog.removeTag(PET);
            dog.removeTag(LITTLE_DOG);
            dog.addTag(RELEASED);
            dog.setNoAi(false);
            dog.setNoGravity(false);
            dog.setInvulnerable(false);
            dog.setTame(true, true);
            dog.setOwnerUUID(player.getUUID());
            dog.setOrderedToSit(false);
            Vec3 safe = nearbyStandingSpot(player.serverLevel(), player);
            dog.teleportTo(safe.x, safe.y, safe.z);
            collection.recordDogRecovery(player.getUUID());
            if(collection.salved())WitnessAccount.resolve(player,WitnessAccount.Story.MOTHER,"saved_dog");
            BlockPos base = base(player.server);
            if (base != null && !collection.banished()) {
                MotherEntity mother = ensureKeeper(player.serverLevel(), base);
                mother.setCarrying(false);
                mother.setNoGravity(false);
                mother.teleportTo(base.getX() + .5D, base.getY(), base.getZ() - 10.5D);
            }
            say(player, collection.salved() ? "So there is a way to let something go without losing it."
                    : "Then take it. Do not let it become lost again.");
            return;
        }
    }

    private static Vec3 nearbyStandingSpot(ServerLevel level, ServerPlayer player) {
        for (BlockPos offset : new BlockPos[]{new BlockPos(1,0,0), new BlockPos(-1,0,0),
                new BlockPos(0,0,1), new BlockPos(0,0,-1), BlockPos.ZERO}) {
            BlockPos pos = player.blockPosition().offset(offset);
            if (level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir()
                    && level.getBlockState(pos.below()).isCollisionShapeFullBlock(level, pos.below())) return Vec3.atBottomCenterOf(pos);
        }
        return player.position();
    }

    private static void banishFromHouse(MinecraftServer server) {
        ServerLevel house = server.getLevel(HouseDimensions.INTERIOR);
        if (house == null) return;
        List<Entity> gone = new ArrayList<>();
        for (Entity entity : house.getAllEntities()) if (entity instanceof MotherEntity) gone.add(entity);
        for (Entity mother : gone) {
            house.sendParticles(ParticleTypes.ASH, mother.getX(), mother.getY() + 1, mother.getZ(), 30, .35, .7, .35, .02);
            mother.discard();
        }
        FOLLOWERS.clear();
    }

    public static void onDimensionChanged(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ServerLevel from = player.server.getLevel(event.getFrom());
        if (from == null) return;
        List<Mob> pets = new ArrayList<>();
        for (Entity entity : from.getAllEntities()) {
            if (entity instanceof Mob mob && entity.getTags().contains(RELEASED)) {
                UUID owner = mob instanceof TamableAnimal tame ? tame.getOwnerUUID()
                        : mob instanceof AbstractHorse horse ? horse.getOwnerUUID() : null;
                if (player.getUUID().equals(owner) && mob.isAlive()) pets.add(mob);
            }
        }
        Vec3 safe = nearbyStandingSpot(player.serverLevel(), player);
        for (Mob pet : pets) pet.changeDimension(new DimensionTransition(player.serverLevel(), safe,
                Vec3.ZERO, player.getYRot(), 0.0F, DimensionTransition.DO_NOTHING));
    }

    private static void clearClaimInInventory(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (MotherCollection.claimOf(stack) != null) player.getInventory().setItem(i, MotherCollection.withoutClaim(stack));
        }
    }

    private static void giveWithoutLoss(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            ItemEntity dropped = player.drop(stack, false);
            if (dropped != null) dropped.getPersistentData().putUUID(DROPPER, player.getUUID());
        }
    }

    public static void onAttack(AttackEntityEvent event) {
        Entity target = event.getTarget();
        if (!(target instanceof MotherEntity) && !target.getTags().contains(FRAME) && !target.getTags().contains(PET)) return;
        event.setCanceled(true);
        if (!(event.getEntity() instanceof ServerPlayer player) || player.isSpectator()) return;
        if (target instanceof ItemFrame) {
            interact(player, target, player.getMainHandItem());
            return;
        }
        long now = player.serverLevel().getGameTime();
        if (now - LAST_INTERACTION.getOrDefault(player.getUUID(), -20L) < 20) return;
        LAST_INTERACTION.put(player.getUUID(), now);
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (MotherCollection.meaningful(stack) && MotherCollection.claimOf(stack) == null) {
                MotherCollection.get(player.server).keepItem(stack.copyWithCount(1), player.registryAccess(), player.getUUID(), now);
                stack.shrink(1);
                say(player, "I will keep that too.");
                return;
            }
        }
        say(player, "You have already come here empty-handed.");
    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !inDen(player)) return;
        BlockPos base = base(player.server);
        if (base == null || !event.getPos().equals(base.offset(SHELF_BELL))) return;
        MotherCollection collection = MotherCollection.get(player.server);
        int pages = shelfPages(collection);
        shelfPage = collection.nextPage(player.getUUID(), pages);
        refreshShelves(player.serverLevel(), base, collection);
        refreshPets(player.serverLevel(), base, collection);
        say(player, pages == 1 ? "These are the things nearest the door." : "There are more things than there are walls.");
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % 20 != 0) return;
        MotherCollection collection = MotherCollection.get(server);
        ServerLevel level = server.getLevel(HouseDimensions.INTERIOR);
        BlockPos origin = HouseSavedData.get(server).houseOrigin();
        BlockPos base = origin == null ? null : LabyrinthPlaces.base(origin, LabyrinthPlace.MOTHER_DEN);
        boolean activeDebt = server.getPlayerList().getPlayers().stream().anyMatch(p -> p.serverLevel() == level
                && collection.debt(p.getUUID()) != null);
        ServerPlayer dogOwner = collection.dogThreatOwner() == null ? null
                : server.getPlayerList().getPlayer(collection.dogThreatOwner());
        collection.advance(20, activeDebt, dogOwner != null && inDen(dogOwner));
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            boolean here = inDen(player);
            collection.presence(player.getUUID(), here);
            if (server.getTickCount() % 200 == 0) {
                long now = server.overworld().getGameTime();
                for (int i = 0; i < player.getInventory().getContainerSize(); i++)
                    MotherCollection.rememberCarried(player.getInventory().getItem(i), player.getUUID(), now);
            }
            if (level != null && origin != null) {
                boolean owed = collection.debt(player.getUUID()) != null && !here && !collection.salved() && !collection.banished();
                follow(player, level, origin, owed);
                if (owed && collection.hungerTicks() >= 3600 && LAST_WARNING.getOrDefault(player.getUUID(), 0) == 0) {
                    LAST_WARNING.put(player.getUUID(), 1);
                    say(player, "I will not leave it lost. Bring it back while I can still ask.");
                }
                MotherEntity follower = FOLLOWERS.get(player.getUUID());
                if (owed && collection.hungerTicks() >= 4200 && follower != null && follower.distanceToSqr(player) < 25.0D) {
                    brutalReclamation(player, follower, collection);
                }
            }
        }
        if (level == null || base == null || !LabyrinthBuilder.isBuilt(server)) return;
        AABB bounds = box(base.offset(-11, -4, -26), base.offset(12, 14, 1));
        if (level.players().stream().noneMatch(p -> bounds.contains(p.position()))) return;
        MotherEntity keeper = collection.banished() ? null : ensureKeeper(level, base);
        if (keeper != null) keeper.setCorruption(collection.corruption());
        if (keeper != null && LabyrinthEncounters.ambient(keeper)) return;
        if (keeper != null && dogOwner != null && inDen(dogOwner)) tickDogThreat(level, base, keeper, collection, dogOwner);
        if (shelfRevision != collection.revision()) {
            refreshShelves(level, base, collection);
            refreshPets(level, base, collection);
        }
        for (Mob pet : level.getEntitiesOfClass(Mob.class, bounds, mob -> mob.getTags().contains(PET))) {
            if (keeper != null && !pet.isNoAi() && pet.distanceToSqr(keeper) > 16.0D) pet.getNavigation().moveTo(keeper, 0.7D);
        }
    }

    private static void brutalReclamation(ServerPlayer player, MotherEntity mother, MotherCollection collection) {
        MotherCollection.Entry debt = collection.debt(player.getUUID());
        if (debt == null) return;
        ItemStack selected = ItemStack.EMPTY;
        ItemStack gathered = ItemStack.EMPTY;
        List<ItemStack> fragments = new ArrayList<>();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (debt.id.equals(MotherCollection.claimOf(stack))) {
                fragments.add(stack);
                if (gathered.isEmpty()) gathered = stack.copy(); else gathered.grow(stack.getCount());
            } else if (MotherCollection.claimOf(stack) == null && selected.isEmpty() && MotherCollection.meaningful(stack)) selected = stack;
        }
        int due = debt.remaining;
        boolean wholeClaim = !gathered.isEmpty() && gathered.getCount() >= due;
        if (wholeClaim) selected = gathered;
        if (!collection.brutalReclaim(player.getUUID(), selected, player.registryAccess(), player.serverLevel().getGameTime())) return;
        if (wholeClaim) {
            for (ItemStack fragment : fragments) {
                int take = Math.min(due, fragment.getCount());
                fragment.shrink(take);
                due -= take;
            }
        }
        clearClaimInInventory(player);
        float damage = Math.min(6.0F, Math.max(0.0F, player.getHealth() - 1.0F));
        if (damage > 0) player.hurt(player.damageSources().mobAttack(mother), damage);
        player.causeFoodExhaustion(3.0F);
        mother.setCorruption(0);
        mother.getNavigation().stop();
        LAST_WARNING.clear();
        player.displayClientMessage(Component.literal(
                "Her fingers close hard enough to hurt. She takes what she came for. Her face is ordinary again.")
                .withStyle(ChatFormatting.DARK_GRAY), false);
        say(player, "There. That was kinder.");
    }

    private static void tickDogThreat(ServerLevel level, BlockPos base, MotherEntity mother,
                                      MotherCollection collection, ServerPlayer owner) {
        MotherPekingese dog = null;
        for (MotherPekingese candidate : level.getEntitiesOfClass(MotherPekingese.class,
                box(base.offset(-11,-4,-26), base.offset(12,14,1)))) {
            if (candidate.getTags().contains(LITTLE_DOG)) { dog = candidate; break; }
        }
        if (dog == null || !dog.isAlive()) {
            collection.endDogThreat(false);
            mother.setCorruption(0);
            mother.setCarrying(false);
            mother.setNoGravity(false);
            mother.teleportTo(base.getX() + .5D, base.getY(), base.getZ() - 10.5D);
            say(owner, "It will not have to stay lost now.");
            return;
        }
        if (collection.dogThrown()) return;
        mother.setCarrying(true);
        dog.setNoAi(true);
        dog.setNoGravity(true);
        Vec3 forward = mother.getLookAngle().multiply(1, 0, 1).normalize();
        dog.moveTo(mother.getX() + forward.x * .5D, mother.getY() + .78D,
                mother.getZ() + forward.z * .5D, mother.getYRot(), 0.0F);
        if (collection.dogThreatTicks() >= 600 && !collection.dogAtLedge()) {
            Vec3 ledge = Vec3.atBottomCenterOf(base.offset(-1, 9, -21));
            if (!HouseWatchers.isWatched(level, mother.getEyePosition())
                    && !HouseWatchers.isWatched(level, ledge.add(0, 1.98, 0))) {
                mother.setNoGravity(true);
                mother.teleportTo(ledge.x, ledge.y, ledge.z);
                collection.dogAtLedge(true);
                say(owner, "You offered to take it. I heard you. I can still be kinder.");
                owner.displayClientMessage(Component.literal(
                        "She stands above the well with the dog. There is still time to reach her or offer something loved.")
                        .withStyle(ChatFormatting.DARK_GRAY), false);
            }
            // Looking at her postpones the disposal; its last fifteen seconds begin on the ledge.
            return;
        }
        if (collection.dogLedgeTicks() >= 300 && collection.dogAtLedge()) {
            dog.setNoAi(false);
            dog.setNoGravity(false);
            dog.setInvulnerable(false);
            dog.moveTo(base.getX() + .5D, base.getY() + 10.5D, base.getZ() - 20.5D, 0.0F, 0.0F);
            dog.setDeltaMovement(0, -.35D, 0);
            collection.dogThrown(true);
            mother.setCarrying(false);
            owner.displayClientMessage(Component.literal(
                    "She opens her hands over the well. She watches all the way down.")
                    .withStyle(ChatFormatting.DARK_GRAY), false);
        }
    }

    private static void follow(ServerPlayer player, ServerLevel house, BlockPos origin, boolean owed) {
        MotherEntity mother = FOLLOWERS.get(player.getUUID());
        if (mother == null || mother.isRemoved()) {
            mother = null;
            for (Entity entity : house.getAllEntities()) {
                if (entity instanceof MotherEntity found && player.getUUID().equals(found.following())) { mother = found; break; }
            }
            if (mother != null) FOLLOWERS.put(player.getUUID(), mother);
        }
        if (!owed || player.serverLevel() != house || player.isSpectator()) {
            if (mother != null && !HouseWatchers.isWatched(house, mother.getEyePosition())) {
                mother.discard();
                FOLLOWERS.remove(player.getUUID());
            }
            return;
        }
        MotherCollection collection = MotherCollection.get(player.server);
        float corruption = collection.corruption();
        if (mother != null) mother.setCorruption(corruption);
        if (mother != null && collection.hungerTicks() >= 4200 && mother.distanceToSqr(player) < 26.0D * 26.0D) {
            // Only after a spoken warning and three and a half active minutes does she leave cover to reclaim.
            mother.getNavigation().moveTo(player, 1.0D);
            mother.getLookControl().setLookAt(player, 20.0F, 20.0F);
            return;
        }
        if (mother != null && watchedBody(house, mother)) {
            mother.getNavigation().stop();
            mother.getLookControl().setLookAt(player, 20.0F, 20.0F);
            return;
        }
        long now = house.getGameTime();
        if (now < NEXT_LURK.getOrDefault(player.getUUID(), 0L)) return;
        NEXT_LURK.put(player.getUUID(), now + lurkInterval(corruption));
        Vec3 destination = lurkSpot(house, origin, player, corruption);
        if (destination == null) return; // A short room cannot fit her distance. She waits for space.
        if (mother == null) {
            mother = MotherRegistry.MOTHER.get().create(house);
            if (mother == null) return;
            mother.follow(player.getUUID());
            mother.setCorruption(corruption);
            mother.moveTo(destination.x, destination.y, destination.z, player.getYRot(), 0.0F);
            if (!house.addFreshEntity(mother)) return;
            FOLLOWERS.put(player.getUUID(), mother);
        } else mother.teleportTo(destination.x, destination.y, destination.z);
        mother.getLookControl().setLookAt(player, 20.0F, 20.0F);
    }

    private static boolean watchedBody(ServerLevel level, MotherEntity mother) {
        return HouseWatchers.isWatched(level, mother.getEyePosition())
                || HouseWatchers.isWatched(level, mother.position().add(0, .7D, 0))
                || HouseWatchers.isWatched(level, mother.position().add(0, 2.6D, 0));
    }

    public static int lurkInterval(float corruption) {
        return Math.round(200.0F - Math.max(0, Math.min(1, corruption)) * 150.0F);
    }

    public static int[] lurkDistances(float corruption) {
        if (corruption < .33F) return new int[]{35, 32, 38, 40};
        if (corruption < .66F) return new int[]{28, 24, 32};
        if (corruption < .9F) return new int[]{20, 16, 24};
        return new int[]{12, 8, 16};
    }

    /** Corner/obstruction score: two perpendicular surfaces beat the middle of an empty dark corridor. */
    public static int coverScore(ServerLevel level, BlockPos feet) {
        boolean north = false, south = false, east = false, west = false;
        for (int distance = 1; distance <= 2; distance++) {
            north |= !level.getBlockState(feet.north(distance).above()).getCollisionShape(level, feet.north(distance).above()).isEmpty();
            south |= !level.getBlockState(feet.south(distance).above()).getCollisionShape(level, feet.south(distance).above()).isEmpty();
            east |= !level.getBlockState(feet.east(distance).above()).getCollisionShape(level, feet.east(distance).above()).isEmpty();
            west |= !level.getBlockState(feet.west(distance).above()).getCollisionShape(level, feet.west(distance).above()).isEmpty();
        }
        int surfaces = (north ? 1 : 0) + (south ? 1 : 0) + (east ? 1 : 0) + (west ? 1 : 0);
        return surfaces + ((north || south) && (east || west) ? 4 : 0);
    }

    @Nullable
    public static Vec3 followSpot(ServerLevel level, BlockPos origin, ServerPlayer player) {
        return lurkSpot(level, origin, player, 0.0F);
    }

    @Nullable
    public static Vec3 lurkSpot(ServerLevel level, BlockPos origin, ServerPlayer player, float corruption) {
        Vec3 best = null;
        double bestScore = -Double.MAX_VALUE;
        for (int distance : lurkDistances(corruption)) {
            for (int angle = 0; angle < 360; angle += 20) {
                double radians = Math.toRadians(angle);
                BlockPos feet = BlockPos.containing(player.position().add(Math.cos(radians) * distance, 0, Math.sin(radians) * distance));
                if (!level.hasChunkAt(feet)) continue;
                LabyrinthPlace place = LabyrinthPlaces.placeAt(origin, feet);
                if (place == null) continue;
                for (int dy : new int[] {0, -1, 1}) {
                    BlockPos spot = feet.offset(0, dy, 0);
                    if (!level.getBlockState(spot).isAir() || !level.getBlockState(spot.above()).isAir()
                            || !level.getBlockState(spot.above(2)).isAir()
                            || !level.getBlockState(spot.below()).isCollisionShapeFullBlock(level, spot.below())
                            || level.getBrightness(LightLayer.BLOCK, spot.above()) > (corruption >= .66F ? 2 : 4)) continue;
                    Vec3 candidate = Vec3.atBottomCenterOf(spot);
                    if (HouseWatchers.isWatched(level, candidate.add(0, 1.98D, 0))
                            || HouseWatchers.isWatched(level, candidate.add(0, .7D, 0))
                            || HouseWatchers.isWatched(level, candidate.add(0, 2.6D, 0))) continue;
                    boolean obscured = level.clip(new ClipContext(player.getEyePosition(), candidate.add(0, 1.2D, 0),
                            ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getType() != HitResult.Type.MISS;
                    int cover = coverScore(level, spot);
                    double score = cover * (2.0D + corruption * 5.0D) + (obscured ? 12.0D + corruption * 12.0D : 0.0D)
                            - level.getBrightness(LightLayer.BLOCK, spot.above()) * 2.0D - distance * .05D;
                    if (score > bestScore) { bestScore = score; best = candidate; }
                }
            }
        }
        return best;
    }

    public static boolean inDen(ServerPlayer player) {
        if (!player.serverLevel().dimension().equals(HouseDimensions.INTERIOR)) return false;
        BlockPos origin = HouseSavedData.get(player.server).houseOrigin();
        return origin != null && LabyrinthPlaces.placeAt(origin, player.blockPosition()) == LabyrinthPlace.MOTHER_DEN;
    }

    @Nullable private static BlockPos base(MinecraftServer server) {
        BlockPos origin = HouseSavedData.get(server).houseOrigin();
        return origin == null ? null : LabyrinthPlaces.base(origin, LabyrinthPlace.MOTHER_DEN);
    }

    private static void say(ServerPlayer player, String words) {
        player.displayClientMessage(Component.literal("The Mother: " + words).withStyle(ChatFormatting.GRAY), false);
    }

    public static void clearAll() {
        LAST_INTERACTION.clear();
        NEXT_LURK.clear();
        LAST_WARNING.clear();
        FOLLOWERS.clear();
        shelfRevision = -1;
        shelfPage = 0;
    }
}
