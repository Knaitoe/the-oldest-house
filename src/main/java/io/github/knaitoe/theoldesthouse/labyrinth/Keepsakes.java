package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseExperience;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Every artifact does something (0.4.192). The owner: "most of the items you provided the player with don't really have an
 * effect on the gameplay, either inside or outside of the house." This gives the keepsakes the literary rooms already hand out
 * one effect each, from their room's story. Each works only for the reader it was left for (its {@link LiteraryVignettes#OWNER}
 * mark), never adds Witness credit, and keeps the item itself unchanged.
 * <ul>
 * <li><b>The elk's tooth</b> (elk fan), in the off hand: the elk's weight, hard to knock back, the Minotaur's throw included.</li>
 * <li><b>The bulb</b> (elk fan), used: a small light where they stand for two minutes; once every five minutes; in their own
 * world or the manor, never in the halls, whose darkness is the House's.</li>
 * <li><b>Father's knife</b> (Holy Rabbit): an animal killed with it drops one more cooked portion. Father wasted nothing.</li>
 * <li><b>The rabbit's foot</b> (Holy Rabbit), carried: luck.</li>
 * <li><b>The brother's photograph</b> (mapping), carried: their own companions within 16 blocks take half damage.</li>
 * <li><b>The wax cylinders</b> (Crimson Hall), played in a jukebox by their keeper: no monster spawns within 48 blocks while it
 * plays.</li>
 * <li><b>The diary's last page</b> (Devil's Rock), read at night in their world: which way and how far to their bed.</li>
 * <li><b>The leaf of the vault register</b> (Usher, failed), carried in their world: the dead do not notice them until within
 * four blocks, unless struck first.</li>
 * <li><b>The cap from the broken post</b> (Last Stop), worn in their world: passive animals follow them as if led.</li>
 * </ul>
 * 0.4.198, four more:
 * <ul>
 * <li><b>Zampanò's leaves</b>, held in a plain hall: ash drifts at the handle of the door back toward the manor. The survey
 * measured the way through.</li>
 * <li><b>The photograph with the vulture</b> (plain), held while standing still: monsters more than six blocks off lose track of
 * them. The frame was made by holding still while the bird waited.</li>
 * <li><b>Her compass</b> (Borrasca), used: away from the House it points to where they last fell; in the House, to the Mother's
 * den, where lost things are kept. It found the missing.</li>
 * <li><b>Her name patch</b> (Borrasca), carried: once a House day, dying away from the House keeps what is in their main hand
 * with them, given back when they wake. The names restored to what was left.</li>
 * </ul>
 */
@EventBusSubscriber(modid = TheOldestHouse.MOD_ID)
public final class Keepsakes {
    private Keepsakes() {}
    public static final ResourceLocation TOOTH_WEIGHT = ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, "elk_tooth_weight");
    /** The tooth's knockback resistance, the bulb's light and its times, and the reaches. */
    public static final double TOOTH_RESIST = .6, PHOTO_REACH = 16, WARD = 48, UNDEAD_NOTICE = 4, HERD = 12;
    public static final int BULB_LIGHT = 12, BULB_TICKS = 2400, BULB_COOLDOWN = 6000;
    private static final Map<BlockPos, Light> LIGHTS = new HashMap<>();
    private static final List<Ward> WARDS = new ArrayList<>();
    /** 0.4.198: the photograph's reach, how long a keeper must stand still, and where the patch keeps its stitch. */
    public static final double STILL_REACH = 6;
    public static final int STILL_TICKS = 20;
    public static final String STATE = "keepsakes_0198";
    private static final Map<UUID, Still> STILL = new HashMap<>();
    record Still(net.minecraft.world.phys.Vec3 at, long since) {}
    record Light(ResourceKey<Level> level, long until) {}
    record Ward(ResourceKey<Level> level, BlockPos pos, UUID owner) {}

    /** True if the stack is this reader's own keepsake from that room. */
    public static boolean mine(ItemStack s, ServerPlayer p, LabyrinthPlace place, String key) {
        var data = s.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        if (data == null) return false;
        var t = data.copyTag();
        return t.hasUUID(LiteraryVignettes.OWNER) && t.getUUID(LiteraryVignettes.OWNER).equals(p.getUUID())
                && key.equals(t.getString(LiteraryVignettes.KEY)) && place.id().equals(t.getString(LiteraryVignettes.PLACE));
    }
    /** True if the reader carries their keepsake anywhere on them. */
    public static boolean carries(ServerPlayer p, LabyrinthPlace place, String key) {
        var inv = p.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) if (mine(inv.getItem(i), p, place, key)) return true;
        return false;
    }
    /**
     * 0.4.194 (owner: "Ensure you're updating literature too"): each keepsake says, in a line of its own, what it does, in
     * the room's words rather than the game's. Added under any lines it already has.
     */
    public static String line(LabyrinthPlace place, String key) {
        return switch (place.id() + "/" + key) {
            case "elk_fan/Tooth" -> "Heavier than it looks. In your other hand it keeps your feet under you.";
            case "elk_fan/Lightbulb" -> "It still lights for a little while, if you ask it.";
            case "holy_rabbit/Knife" -> "Father wasted nothing he killed.";
            case "holy_rabbit/RabbitFoot" -> "Carried, things fall your way a little more often.";
            case "mapping_interior/Photograph" -> "He kept watch over his boys. Kept close, it watches over yours.";
            case "crimson_hall/Cylinder0", "crimson_hall/Cylinder1", "crimson_hall/Cylinder2" -> "Played, the voices keep the house. Nothing else comes near it.";
            case "devils_rock/Diary7" -> "Read it at night, away from the House. The last page knows the way home.";
            case "usher/RegisterLeaf" -> "Your name among those carried down. The dead read it too.";
            case "elk_lot/Cap" -> "Worn, the herd takes you for one of its own.";
            case "plain/Photo" -> "Held still, nothing far off can keep its eyes on you. The bird waited the same way.";
            case "borrasca/Compass" -> "It found the missing. Away from the House it points to where you last fell; inside, to where lost things are kept.";
            case "borrasca/Patch" -> "Her name, sewn in. Once a day, if you fall away from the House, what is in your hand stays with you.";
            default -> null;
        };
    }
    public static void inscribe(ItemStack s, LabyrinthPlace place, String key) {
        String line = line(place, key);
        if (line != null) lore(s, line);
    }
    /** Adds one grey line under a stack's own. */
    public static void lore(ItemStack s, String line) {
        var lines = new ArrayList<Component>(s.getOrDefault(net.minecraft.core.component.DataComponents.LORE, net.minecraft.world.item.component.ItemLore.EMPTY).lines());
        lines.add(Component.literal(line).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        s.set(net.minecraft.core.component.DataComponents.LORE, new net.minecraft.world.item.component.ItemLore(lines));
    }
    /** Their own world: anywhere outside the House's dimensions. */
    public static boolean ownWorld(ServerPlayer p) { return !HouseDimensions.isHouseDimension(p.level().dimension()); }

    // ------------------------------------------------------------------
    // Carried and worn
    // ------------------------------------------------------------------

    @SubscribeEvent public static void tick(PlayerTickEvent.Post e) {
        if (!(e.getEntity() instanceof ServerPlayer p) || !p.isAlive() || p.tickCount % 10 != 0) return;
        tooth(p);
        if (p.isSpectator()) return;
        if (p.tickCount % 40 == 0 && carries(p, LabyrinthPlace.HOLY_RABBIT, "RabbitFoot"))
            p.addEffect(new MobEffectInstance(MobEffects.LUCK, 80, 0, true, false, true));
        if (p.tickCount % 20 == 0) herd(p);
        still(p);
        if (p.tickCount % 20 == 0) { leaves(p);photograph(p); }
    }
    /** The elk's weight while the tooth is in the off hand. */
    public static boolean tooth(ServerPlayer p) {
        var resist = p.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (resist == null) return false;
        boolean on = p.isAlive() && mine(p.getOffhandItem(), p, LabyrinthPlace.ELK_FAN, "Tooth");
        if (on && !resist.hasModifier(TOOTH_WEIGHT)) resist.addTransientModifier(new AttributeModifier(TOOTH_WEIGHT, TOOTH_RESIST, AttributeModifier.Operation.ADD_VALUE));
        else if (!on && resist.hasModifier(TOOTH_WEIGHT)) resist.removeModifier(TOOTH_WEIGHT);
        return on;
    }
    /** Worn, the cap leads passive animals after the reader. Returns how many follow. */
    public static int herd(ServerPlayer p) {
        if (!ownWorld(p) || !mine(p.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD), p, LabyrinthPlace.ELK_LOT, "Cap")) return 0;
        int led = 0;
        for (var a : p.serverLevel().getEntitiesOfClass(Animal.class, p.getBoundingBox().inflate(HERD),
                a -> a.isAlive() && !a.isLeashed() && !(a instanceof TamableAnimal t && t.isTame()) && a.distanceToSqr(p) > 9)) {
            a.getNavigation().moveTo(p, 1.0);led++;
        }
        return led;
    }

    // ------------------------------------------------------------------
    // The bulb
    // ------------------------------------------------------------------

    @SubscribeEvent public static void use(PlayerInteractEvent.RightClickItem e) {
        if (!(e.getEntity() instanceof ServerPlayer p)) return;
        var s = e.getItemStack();
        if (mine(s, p, LabyrinthPlace.ELK_FAN, "Lightbulb") && bulb(p, s)) { e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS); }
        else if (mine(s, p, LabyrinthPlace.DEVILS_ROCK, "Diary7")) diary(p);
    }
    /** Hangs a small light where the reader stands, for two minutes. */
    public static boolean bulb(ServerPlayer p, ItemStack s) {
        if (LiteraryVignettes.current(p) != null || p.getCooldowns().isOnCooldown(s.getItem())) return false;
        if (!ownWorld(p) && !HouseExperience.inManor(p, p.blockPosition())) {
            p.displayClientMessage(Component.literal("It will not light here.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);return false;
        }
        var at = p.blockPosition().above();var level = p.serverLevel();
        if (!level.getBlockState(at).isAir()) at = p.blockPosition();
        if (!level.getBlockState(at).isAir()) return false;
        level.setBlock(at, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, BULB_LIGHT), 3);
        LIGHTS.put(at.immutable(), new Light(level.dimension(), level.getGameTime() + BULB_TICKS));
        p.getCooldowns().addCooldown(s.getItem(), BULB_COOLDOWN);
        level.playSound(null, at, net.minecraft.sounds.SoundEvents.COPPER_BULB_TURN_ON, net.minecraft.sounds.SoundSource.PLAYERS, .6F, 1.2F);
        return true;
    }
    /** Lights hung by bulbs go out when their two minutes are up. */
    public static void lightsOut(net.minecraft.server.MinecraftServer server, boolean all) {
        for (var it = LIGHTS.entrySet().iterator(); it.hasNext(); ) {
            var e = it.next();var level = server.getLevel(e.getValue().level());
            if (level == null) { it.remove();continue; }
            if (!all && level.getGameTime() < e.getValue().until()) continue;
            if (level.isLoaded(e.getKey()) && level.getBlockState(e.getKey()).is(Blocks.LIGHT)) level.setBlock(e.getKey(), Blocks.AIR.defaultBlockState(), 3);
            it.remove();
        }
    }

    // ------------------------------------------------------------------
    // Father's knife
    // ------------------------------------------------------------------

    static Item cooked(EntityType<?> type) {
        if (type == EntityType.COW || type == EntityType.MOOSHROOM) return Items.COOKED_BEEF;
        if (type == EntityType.PIG) return Items.COOKED_PORKCHOP;
        if (type == EntityType.SHEEP) return Items.COOKED_MUTTON;
        if (type == EntityType.CHICKEN) return Items.COOKED_CHICKEN;
        if (type == EntityType.RABBIT) return Items.COOKED_RABBIT;
        return null;
    }
    @SubscribeEvent(priority = EventPriority.LOWEST) public static void drops(LivingDropsEvent e) {
        if (!(e.getSource().getEntity() instanceof ServerPlayer p) || !(e.getEntity() instanceof Animal a)) return;
        if (!mine(p.getMainHandItem(), p, LabyrinthPlace.HOLY_RABBIT, "Knife") || ElkFollows.follows(p)) return;
        var meat = cooked(a.getType());
        if (meat == null) return;
        e.getDrops().add(new ItemEntity(a.level(), a.getX(), a.getY() + .3, a.getZ(), new ItemStack(meat)));
    }

    // ------------------------------------------------------------------
    // The brother's photograph
    // ------------------------------------------------------------------

    @SubscribeEvent public static void hurt(LivingIncomingDamageEvent e) {
        if (!(e.getEntity() instanceof TamableAnimal pet) || !pet.isTame() || pet.getOwnerUUID() == null || !(pet.level() instanceof ServerLevel level)) return;
        var owner = level.getServer().getPlayerList().getPlayer(pet.getOwnerUUID());
        if (owner == null || owner.level() != pet.level() || owner.distanceToSqr(pet) > PHOTO_REACH * PHOTO_REACH || !carries(owner, LabyrinthPlace.MAPPING_INTERIOR, "Photograph")) return;
        e.setAmount(e.getAmount() * .5F);
    }

    // ------------------------------------------------------------------
    // The wax cylinders
    // ------------------------------------------------------------------

    static boolean cylinder(ItemStack s, ServerPlayer p) {
        return mine(s, p, LabyrinthPlace.CRIMSON_HALL, "Cylinder0") || mine(s, p, LabyrinthPlace.CRIMSON_HALL, "Cylinder1") || mine(s, p, LabyrinthPlace.CRIMSON_HALL, "Cylinder2");
    }
    /** Putting a cylinder into an empty jukebox: the voices keep that house while they play. */
    @SubscribeEvent(priority = EventPriority.LOWEST) public static void jukebox(PlayerInteractEvent.RightClickBlock e) {
        if (e.isCanceled() || !(e.getEntity() instanceof ServerPlayer p) || !cylinder(e.getItemStack(), p)) return;
        var state = p.level().getBlockState(e.getPos());
        if (!(state.getBlock() instanceof JukeboxBlock) || state.getValue(JukeboxBlock.HAS_RECORD)) return;
        ward(p.serverLevel(), e.getPos(), p.getUUID());
    }
    public static void ward(ServerLevel level, BlockPos pos, UUID owner) {
        WARDS.removeIf(w -> w.level() == level.dimension() && w.pos().equals(pos));
        WARDS.add(new Ward(level.dimension(), pos.immutable(), owner));
    }
    /** A ward holds while its jukebox still plays one of the cylinders. */
    static boolean playing(ServerLevel level, Ward w) {
        if (!level.isLoaded(w.pos()) || !(level.getBlockEntity(w.pos()) instanceof JukeboxBlockEntity box)) return false;
        var item = box.getTheItem();
        return box.getSongPlayer().isPlaying() && (item.is(LiteraryRegistry.CYLINDER_ONE.get()) || item.is(LiteraryRegistry.CYLINDER_TWO.get()) || item.is(LiteraryRegistry.CYLINDER_THREE.get()));
    }
    public static boolean warded(ServerLevel level, net.minecraft.world.phys.Vec3 at) {
        for (var w : WARDS) if (w.level() == level.dimension() && w.pos().getCenter().distanceToSqr(at) <= WARD * WARD && playing(level, w)) return true;
        return false;
    }
    @SubscribeEvent public static void spawn(FinalizeSpawnEvent e) {
        if (WARDS.isEmpty() || e.getSpawnType() != MobSpawnType.NATURAL || !(e.getEntity() instanceof Enemy) || !(e.getLevel().getLevel() instanceof ServerLevel level)) return;
        if (warded(level, new net.minecraft.world.phys.Vec3(e.getX(), e.getY(), e.getZ()))) e.setSpawnCancelled(true);
    }

    // ------------------------------------------------------------------
    // The diary's last page
    // ------------------------------------------------------------------

    /** Read at night in their world: which way and how far to their bed. */
    public static String diary(ServerPlayer p) {
        long time = p.serverLevel().getDayTime() % 24000L;
        if (!ownWorld(p) || time < 13000 || time >= 23000) return null;
        var bed = p.getRespawnPosition();
        String line;
        if (bed == null || !p.getRespawnDimension().equals(p.level().dimension())) line = "On the last page, under the last entry: \"I don't know the way from here.\"";
        else {
            var d = bed.getCenter().subtract(p.position());
            int blocks = (int) Math.round(Math.hypot(d.x, d.z));
            String way = net.minecraft.core.Direction.getNearest(d.x, 0, d.z).getName();
            line = blocks < 4 ? "On the last page: \"You are home.\"" : "On the last page, in pencil: \"" + blocks + " blocks " + way + ". The path was still there.\"";
        }
        p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), false);
        return line;
    }

    // ------------------------------------------------------------------
    // The leaf of the vault register
    // ------------------------------------------------------------------

    @SubscribeEvent public static void target(LivingChangeTargetEvent e) {
        if (!(e.getNewAboutToBeSetTarget() instanceof ServerPlayer p)) return;
        if (e.getEntity().getType().is(EntityTypeTags.UNDEAD) && unnoticed(p, e.getEntity())) e.setCanceled(true);
        else if (e.getEntity() instanceof Enemy && lostTrack(p, e.getEntity())) e.setCanceled(true);
    }
    /** The dead pass over a reader the house wrote among them, until close, or until struck by them. */
    public static boolean unnoticed(ServerPlayer p, LivingEntity dead) {
        return ownWorld(p) && dead.getLastHurtByMob() != p && dead.distanceToSqr(p) > UNDEAD_NOTICE * UNDEAD_NOTICE && carries(p, LabyrinthPlace.USHER, "RegisterLeaf");
    }

    // ------------------------------------------------------------------
    // Zampanò's leaves (0.4.198)
    // ------------------------------------------------------------------

    static boolean leaf(ItemStack s) { return io.github.knaitoe.theoldesthouse.journal.ZampanoLeafItem.leafOf(s) >= 0; }
    /** Held in a plain hall: the door back toward the manor, where the ash drifts, or null. */
    public static BlockPos leafWay(ServerPlayer p) {
        if (!leaf(p.getMainHandItem()) && !leaf(p.getOffhandItem())) return null;
        if (!p.level().dimension().equals(HouseDimensions.INTERIOR)) return null;
        var origin = LabyrinthData.get(p.server).builtOrigin();
        if (origin == null) return null;
        var place = LabyrinthPlaces.placeAt(origin, p.blockPosition());
        if (place == null || !LabyrinthPacing.ordinary(place)) return null;
        var base = LabyrinthPlaces.base(origin, place);
        if (base == null) return null;
        for (var door : place.doors()) if (LabyrinthData.RETURN.equals(door.destination())) return base.offset(door.rel());
        return null;
    }
    static void leaves(ServerPlayer p) {
        var door = leafWay(p);
        if (door != null) p.serverLevel().sendParticles(p, net.minecraft.core.particles.ParticleTypes.WHITE_ASH, true, door.getX() + .5, door.getY() + 1.1, door.getZ() + .5, 10, .2, .35, .2, .01);
    }

    // ------------------------------------------------------------------
    // The photograph with the vulture (0.4.198)
    // ------------------------------------------------------------------

    static boolean photo(ItemStack s, ServerPlayer p) {
        var data = s.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        if (data == null) return false;
        var t = data.copyTag();
        return t.hasUUID(NovelVignettes.PHOTO_OWNER) && t.getUUID(NovelVignettes.PHOTO_OWNER).equals(p.getUUID());
    }
    /** Where the reader has stood since when; a step resets it. */
    static void still(ServerPlayer p) {
        var was = STILL.get(p.getUUID());
        if (was == null || was.at().distanceToSqr(p.position()) > .01) STILL.put(p.getUUID(), new Still(p.position(), p.serverLevel().getGameTime()));
    }
    /** Holding the photograph and standing still a second or more. */
    public static boolean holdingStill(ServerPlayer p) {
        if (!photo(p.getMainHandItem(), p) && !photo(p.getOffhandItem(), p)) return false;
        var was = STILL.get(p.getUUID());
        return was != null && was.at().distanceToSqr(p.position()) <= .01 && p.serverLevel().getGameTime() - was.since() >= STILL_TICKS;
    }
    /** A monster more than six blocks off cannot keep its eyes on a keeper holding still. */
    public static boolean lostTrack(ServerPlayer p, LivingEntity monster) {
        return monster.distanceToSqr(p) > STILL_REACH * STILL_REACH && holdingStill(p);
    }
    /** Those already hunting them look away. Returns how many. */
    public static int photograph(ServerPlayer p) {
        if (!holdingStill(p)) return 0;
        int lost = 0;
        for (var m : p.serverLevel().getEntitiesOfClass(net.minecraft.world.entity.Mob.class, p.getBoundingBox().inflate(48), m -> m instanceof Enemy && m.getTarget() == p && lostTrack(p, m))) {
            m.setTarget(null);lost++;
        }
        return lost;
    }

    // ------------------------------------------------------------------
    // Her compass and her name patch (Borrasca, 0.4.198)
    // ------------------------------------------------------------------

    /** Used away from the trail: which way, and how far. */
    public static String compass(ServerPlayer p) {
        String line;
        if (ownWorld(p)) {
            var last = p.getLastDeathLocation();
            line = last.isEmpty() || !last.get().dimension().equals(p.level().dimension()) ? "The needle turns slowly under the red tape and will not settle."
                    : way(p, last.get().pos(), "where you last fell");
        } else {
            var origin = LabyrinthData.get(p.server).builtOrigin();
            var den = origin == null ? null : LabyrinthPlaces.base(origin, LabyrinthPlace.MOTHER_DEN);
            line = den == null || !p.level().dimension().equals(NovelRooms.dimension(LabyrinthPlace.MOTHER_DEN)) ? "The needle turns slowly under the red tape and will not settle."
                    : way(p, den, "where lost things are kept");
        }
        p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), false);
        return line;
    }
    static String way(ServerPlayer p, BlockPos to, String what) {
        var d = to.getCenter().subtract(p.position());
        int blocks = (int) Math.round(Math.hypot(d.x, d.z));
        if (blocks < 4) return "The needle swings round and round. It is here.";
        return "The needle settles " + net.minecraft.core.Direction.getNearest(d.x, 0, d.z).getName() + ", toward " + what + ". About " + blocks + " blocks.";
    }
    public static CompoundTag record(ServerPlayer p) { return LabyrinthData.get(p.server).stateEntry(STATE, p.getUUID().toString()); }
    static void save(ServerPlayer p, CompoundTag own) { LabyrinthData.get(p.server).setStateEntry(STATE, p.getUUID().toString(), own); }
    @SubscribeEvent(priority = EventPriority.LOWEST) public static void death(net.neoforged.neoforge.event.entity.living.LivingDeathEvent e) {
        if (e.isCanceled() || !(e.getEntity() instanceof ServerPlayer p) || p.level().getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_KEEPINVENTORY)) return;
        sew(p);
    }
    /** Dying away from the House with the patch, once a House day: what is in their main hand stays with them. */
    public static ItemStack sew(ServerPlayer p) {
        if (!ownWorld(p) || !carries(p, LabyrinthPlace.BORRASCA, "Patch")) return ItemStack.EMPTY;
        var hand = p.getMainHandItem();
        var own = record(p);long today = io.github.knaitoe.theoldesthouse.house.HouseCalendar.today(p.server);
        if (hand.isEmpty() || own.contains("Sewn") || own.contains("SewnDay") && own.getLong("SewnDay") == today) return ItemStack.EMPTY;
        var kept = hand.copy();p.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        own.put("Sewn", kept.save(p.registryAccess()));own.putLong("SewnDay", today);own.putInt("SewnCount", own.getInt("SewnCount") + 1);save(p, own);
        return kept;
    }
    @SubscribeEvent public static void respawn(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent e) {
        if (e.getEntity() instanceof ServerPlayer p) unsew(p);
    }
    /** Waking, what the patch kept is in their hand again. */
    public static boolean unsew(ServerPlayer p) {
        var own = record(p);
        if (!own.contains("Sewn")) return false;
        var kept = ItemStack.parseOptional(p.registryAccess(), own.getCompound("Sewn"));own.remove("Sewn");save(p, own);
        if (kept.isEmpty()) return false;
        if (!p.getInventory().add(kept)) p.drop(kept, false);
        p.displayClientMessage(Component.literal("Her name, stitched in the cloth. What you were holding is still with you.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), false);
        return true;
    }

    @SubscribeEvent public static void serverTick(ServerTickEvent.Post e) {
        if (e.getServer().getTickCount() % 20 == 0) lightsOut(e.getServer(), false);
        if (e.getServer().getTickCount() % 200 == 0) WARDS.removeIf(w -> { var l = e.getServer().getLevel(w.level());return l == null || l.isLoaded(w.pos()) && !playing(l, w); });
    }
    @SubscribeEvent public static void stopping(net.neoforged.neoforge.event.server.ServerStoppingEvent e) { lightsOut(e.getServer(), true);WARDS.clear();STILL.clear(); }
}
