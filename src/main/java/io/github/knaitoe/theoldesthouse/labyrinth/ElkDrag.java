package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.github.knaitoe.theoldesthouse.network.LiteraryViewPayload;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.joml.Vector3f;

/**
 * Dying at the elk killer's hand (0.4.120). The killing blow does not kill. The reader's sight goes in pulses; they come
 * to on their back, dragged by the ankle down the passage into the cave, his back and boots above them; it goes dark
 * again; and they wake inside the pile of carcasses with the gap packed shut. They fight their way out through the meat.
 * He walks away out of the cave while they are under it and for a while after they are out, then goes back to his round:
 * he is still hunting them. It happens once a crossing; a second killing blow on the same crossing kills (0.4.121).
 *
 * <p>Their body waits in the pile from the first moment; what they see is a camera of their own carried along the drag,
 * with their own killer pulling ahead of it. The carcasses packed into the gap are theirs alone (the shared pile is not
 * touched), and so is every sound.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class ElkDrag {
    /** The pulsing blackout, the drag, the dark before waking; then the hits it takes to tear one carcass loose. */
    public static final int BLACKOUT = 50, DRAGGING = 230, DARK = 40, HITS = 2;
    /**
     * How long he keeps away while they are under the pile, and once they are out; and when the pile settles by itself,
     * so no one is left in it for good.
     */
    public static final int BURIED_GRACE = 6000, ESCAPE_GRACE = 600, SETTLE = 1200;
    /** How far ahead of the reader's head he walks, holding the ankle. */
    static final double LEAD = 1.6;
    /** The reader is on their back: their eyes ride this far above the ground. */
    static final double EYE = .3;
    /** The reader wakes with this much health, however little the blow left. */
    public static final float WAKE_HEALTH = 6;
    private static final LabyrinthPlace PLACE = LabyrinthPlace.ELK_CARCASSES;
    /**
     * A reader in the drag or in the pile, with their clock. The clocks live here, not in the saved record: the reader's
     * scene tick saves its own copy of that record every few ticks and would set a saved clock back.
     */
    private static final class Reader {
        final ServerPlayer p; final BlockPos b; int t;
        Reader(ServerPlayer p, BlockPos b) { this.p = p; this.b = b; }
        ServerPlayer p() { return p; }
        BlockPos b() { return b; }
    }
    private static final Map<UUID, Reader> DRAGGED = new HashMap<>(), BURIED = new HashMap<>();
    private static final int[] HEAVE;
    static {
        // He pulls in heaves: the body slides on each, then nearly stops while he takes a fresh grip.
        HEAVE = new int[DRAGGING + 1];
        double total = 0, sum = 0;
        for (int k = 0; k < DRAGGING; k++) total += weight(k);
        for (int k = 0; k <= DRAGGING; k++) { HEAVE[k] = (int) Math.round(sum / total * 10000); if (k < DRAGGING) sum += weight(k); }
    }
    private ElkDrag() {}

    private static double weight(int k) {
        double phase = Math.sin(Math.PI * (k % 24) / 24.0);
        return .18 + phase * phase;
    }

    /** How far along the drag the reader's head is, k ticks into it. */
    public static double along(int k) {
        return HEAVE[Math.max(0, Math.min(DRAGGING, k))] / 10000.0 * (ElkCarcassMap.DRAG_LENGTH - LEAD);
    }

    public static boolean active(ServerPlayer p) {
        var r = DRAGGED.get(p.getUUID());
        return r != null && r.p() == p;
    }

    public static boolean buried(ServerPlayer p) {
        var r = BURIED.get(p.getUUID());
        return r != null && r.p() == p;
    }

    private static BlockPos base(ServerPlayer p) {
        var origin = HouseSavedData.get(p.server).houseOrigin();
        return origin == null ? null : LabyrinthPlaces.base(origin, PLACE);
    }

    private static boolean killedBy(ServerPlayer p, net.minecraft.world.entity.Entity source) {
        return source instanceof LiteraryActor a && a.role() == LiteraryActor.KILLER && a.scene().equals(PLACE.id())
                && a.owner().isPresent() && a.owner().get().equals(p.getUUID());
    }

    /** Before the death is recorded anywhere (portraits, logs, the hotel's count). Only his blow, only his reader. */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void killed(LivingDeathEvent e) {
        if (e.isCanceled() || !(e.getEntity() instanceof ServerPlayer p) || p.isSpectator() || e.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        if (!killedBy(p, e.getSource().getEntity())) return;
        // Already dying here, so the scene is checked by place, not by the living-reader rule.
        var b = base(p);
        if (b == null || !p.level().dimension().equals(NovelRooms.dimension(PLACE)) || !IndianLakeRooms.bounds(b, PLACE).contains(p.position())) return;
        // He has dragged them once this crossing: this time the blow is what it looks like.
        if (LiteraryVignettes.personal(LabyrinthData.get(p.server), p.getUUID(), PLACE).getBoolean("ElkDraggedThisCrossing")) return;
        e.setCanceled(true);
        begin(p, b, (LiteraryActor) e.getSource().getEntity());
    }

    /** Nothing reaches the reader while the drag is running. */
    @SubscribeEvent
    public static void spared(LivingIncomingDamageEvent e) {
        if (e.getEntity() instanceof ServerPlayer p && active(p)) e.setCanceled(true);
    }

    /**
     * The drag's start in a reader's record. His blow lands inside the reader's own scene tick, which saves its copy of
     * the record afterwards, so the blow's caller marks that copy too (ElkHunt does, after any blow that began a drag).
     */
    public static void mark(CompoundTag own) {
        own.putBoolean("ElkDraggedThisCrossing", true);
        if (own.getInt("ElkStage") == ElkHunt.DRAGGED) return;
        own.putInt("ElkStage", ElkHunt.DRAGGED);
        own.putInt("ElkWindup", 0);
        own.putInt("ElkStagger", 0);
        own.putInt("ElkWork", 0);
        own.putInt("ElkDrags", own.getInt("ElkDrags") + 1);
    }

    public static void begin(ServerPlayer p, BlockPos b, LiteraryActor killer) {
        p.setHealth(1);
        p.clearFire();
        var d = LabyrinthData.get(p.server);
        var own = LiteraryVignettes.personal(d, p.getUUID(), PLACE);
        mark(own);
        LiteraryVignettes.save(d, p.getUUID(), PLACE, own);
        KillerNavigation.stop(killer);
        killer.setNoGravity(true);
        DRAGGED.put(p.getUUID(), new Reader(p, b));
        frame(p, b, own, killer, 0);
        // The body goes where it will wake, under the pile; the reader's sight is already gone.
        p.stopRiding();
        var wake = ElkCarcassMap.WAKE.add(b.getX(), b.getY(), b.getZ());
        p.teleportTo(p.serverLevel(), wake.x, wake.y, wake.z, 0, 0);
        p.setDeltaMovement(Vec3.ZERO);
    }

    @SubscribeEvent
    public static void tick(ServerTickEvent.Post e) {
        for (var entry : new ArrayList<>(DRAGGED.entrySet())) {
            var r = entry.getValue();
            var p = r.p();
            if (p.isRemoved() || !p.isAlive() || p.hasDisconnected()) { DRAGGED.remove(entry.getKey()); continue; }
            var d = LabyrinthData.get(p.server);
            var own = LiteraryVignettes.personal(d, p.getUUID(), PLACE);
            if (own.getInt("ElkStage") != ElkHunt.DRAGGED) { DRAGGED.remove(entry.getKey()); continue; }
            int t = ++r.t;
            var killer = ElkHunt.killer(p, r.b());
            if (killer == null) continue;
            // The body keeps still in the pile while the reader is elsewhere in their sight.
            var wake = ElkCarcassMap.WAKE.add(r.b().getX(), r.b().getY(), r.b().getZ());
            if (p.position().distanceToSqr(wake) > .2) p.teleportTo(p.serverLevel(), wake.x, wake.y, wake.z, p.getYRot(), p.getXRot());
            p.setDeltaMovement(Vec3.ZERO);
            if (t > BLACKOUT + DRAGGING + DARK) { wake(p, r.b(), own, killer); continue; }
            frame(p, r.b(), own, killer, t);
        }
        for (var entry : new ArrayList<>(BURIED.entrySet())) {
            var r = entry.getValue();
            var p = r.p();
            if (p.isRemoved() || p.hasDisconnected()) { BURIED.remove(entry.getKey()); continue; }
            var d = LabyrinthData.get(p.server);
            var own = LiteraryVignettes.personal(d, p.getUUID(), PLACE);
            int left = own.getInt("ElkBurial");
            var wake = ElkCarcassMap.WAKE.add(r.b().getX(), r.b().getY(), r.b().getZ());
            boolean away = !LiteraryVignettes.inside(p, PLACE) || p.position().distanceToSqr(wake) > 9;
            if (left == 0 || away && free(left)) {
                // Out: the carcasses no one else ever saw go with them, and he stays away a little longer.
                show(p, r.b(), 0);
                own.putInt("ElkBurial", 0);
                own.putLong("ElkGraceUntil", p.serverLevel().getGameTime() + ESCAPE_GRACE);
                LiteraryVignettes.save(d, p.getUUID(), PLACE, own);
                BURIED.remove(entry.getKey());
                continue;
            }
            int t = ++r.t;
            if (t % 10 == 0 || own.getBoolean("ElkBurialResend")) {
                show(p, r.b(), left);
                if (own.getBoolean("ElkBurialResend")) { own.putBoolean("ElkBurialResend", false); LiteraryVignettes.save(d, p.getUUID(), PLACE, own); }
            }
            if (t == 60 && own.getInt("ElkBurialHitsTotal") == 0) p.displayClientMessage(Component.literal("Strike at them. Tear a way through."), true);
            if (t == SETTLE && !free(left)) {
                // The pile slumps under its own weight and one side of the gap opens.
                left &= Integer.bitCount(left & 0b0101) <= Integer.bitCount(left & 0b1010) ? ~0b0101 : ~0b1010;
                own.putInt("ElkBurial", left);
                LiteraryVignettes.save(d, p.getUUID(), PLACE, own);
                show(p, r.b(), left);
                sound(p, wake, SoundEvents.MUD_BREAK, 1, .5F);
                p.displayClientMessage(Component.literal("The pile slumps. A seam of air opens to one side."), true);
            }
        }
    }

    /** One tick of what the reader sees and hears: the camera and the killer on the drag, and the overlay's clock. */
    private static void frame(ServerPlayer p, BlockPos b, CompoundTag own, LiteraryActor killer, int t) {
        int phase = t <= BLACKOUT ? 0 : t <= BLACKOUT + DRAGGING ? 1 : 2;
        double s = phase == 0 ? 0 : along(t - BLACKOUT);
        var head = ElkCarcassMap.drag(s).add(b.getX(), b.getY(), b.getZ());
        float yaw = ElkCarcassMap.dragHeading(s);
        var camera = LiteraryVignettes.actor(p, PLACE, "DragCamera", LiteraryActor.CAMERA, BlockPos.containing(ElkCarcassMap.drag(0)), true);
        if (camera == null) return;
        camera.setInvisible(true);
        camera.setNoGravity(true);
        // Flat on their back, a hand above the ground, looking down their own body at him.
        camera.moveTo(head.x, head.y + EYE - camera.getEyeHeight(), head.z, yaw, -22);
        camera.setYHeadRot(yaw);
        camera.setYBodyRot(yaw);
        double ahead = Math.min(ElkCarcassMap.DRAG_LENGTH, s + LEAD);
        var lead = ElkCarcassMap.drag(ahead).add(b.getX(), b.getY(), b.getZ());
        float heading = ElkCarcassMap.dragHeading(ahead);
        killer.moveTo(lead.x, lead.y, lead.z, heading, 12);
        killer.setYHeadRot(heading);
        killer.setYBodyRot(heading);
        killer.appearance(LiteraryActor.KILLER, phase == 1 ? ElkHunt.DRAG : ElkHunt.WATCH);
        // The heart in the dark; the body scraping over roots and then gravel on each heave; his boots.
        if (phase != 1 ? t % 22 == 0 : t % 70 == 35) sound(p, head, LiteraryRegistry.CABIN_HEART, .9F, 1);
        if (phase == 1) {
            int k = t - BLACKOUT;
            boolean inside = ElkCarcassMap.inCave(head.subtract(b.getX(), b.getY(), b.getZ()));
            if (k % 24 == 4) sound(p, head, inside ? SoundEvents.GRAVEL_STEP : SoundEvents.ROOTED_DIRT_STEP, 1, .5F + p.getRandom().nextFloat() * .1F);
            if (k % 24 == 10) sound(p, head, inside ? SoundEvents.GRAVEL_HIT : SoundEvents.MOSS_CARPET_STEP, .7F, .6F);
            if (k % 12 == 0) sound(p, lead, inside ? SoundEvents.STONE_STEP : SoundEvents.MUD_STEP, .7F, .7F);
        }
        var tag = new CompoundTag();
        var drag = new CompoundTag();
        drag.putInt("T", t);
        drag.putInt("Phase", phase);
        tag.put("ElkDrag", drag);
        HousePackets.send(p, new LiteraryViewPayload(camera.getId(), 10, 0, tag));
    }

    /**
     * They come to inside the pile. The camera is theirs again; the gap and the cell in front of it are packed with
     * carcasses only they see; and he walks away out of the cave, hunting no one for a while.
     */
    public static void wake(ServerPlayer p, BlockPos b, CompoundTag own, LiteraryActor killer) {
        DRAGGED.remove(p.getUUID());
        long now = p.serverLevel().getGameTime();
        own.putInt("ElkStage", ElkHunt.ASHORE);
        own.putBoolean("ElkLanded", true);
        own.putLong("ElkGraceUntil", now + BURIED_GRACE);
        own.putInt("ElkHidden", 0);
        own.putBoolean("ElkFound", false);
        own.putBoolean("ElkBetrayed", false);
        own.remove("ElkLastSeen");
        own.putInt("ElkWaypoint", -1);
        own.putInt("ElkBurial", (1 << ElkCarcassMap.BURIAL.size()) - 1);
        own.putIntArray("ElkBurialHits", new int[ElkCarcassMap.BURIAL.size()]);
        own.putInt("ElkBurialHitsTotal", 0);
        LiteraryVignettes.save(LabyrinthData.get(p.server), p.getUUID(), PLACE, own);
        var wake = ElkCarcassMap.WAKE.add(b.getX(), b.getY(), b.getZ());
        p.teleportTo(p.serverLevel(), wake.x, wake.y, wake.z, 0, 0);
        p.setDeltaMovement(Vec3.ZERO);
        p.setHealth(Math.max(p.getHealth(), WAKE_HEALTH));
        if (killer != null) {
            killer.setNoGravity(false);
            killer.appearance(LiteraryActor.KILLER, ElkHunt.WATCH);
        }
        BURIED.put(p.getUUID(), new Reader(p, b));
        show(p, b, own.getInt("ElkBurial"));
        var tag = new CompoundTag();
        var drag = new CompoundTag();
        drag.putInt("T", -1);
        drag.putInt("Phase", 3);
        tag.put("ElkDrag", drag);
        HousePackets.send(p, new LiteraryViewPayload(-1, 0, 0, tag));
        sound(p, wake, SoundEvents.SLIME_SQUISH, .8F, .5F);
        p.displayClientMessage(Component.literal("You come to under them, in the dark and the smell. The gap is packed shut."), true);
    }

    /** Whether a whole way out is clear: the gap and the cell beyond it, on either side. */
    static boolean free(int left) {
        return (left & 0b0101) == 0 || (left & 0b1010) == 0;
    }

    /** The reader's own packed carcasses, as their client should see them; cleared cells show what is really there. */
    private static void show(ServerPlayer p, BlockPos b, int left) {
        for (int i = 0; i < ElkCarcassMap.BURIAL.size(); i++) {
            var at = b.offset(ElkCarcassMap.BURIAL.get(i));
            var state = (left & 1 << i) != 0
                    ? LiteraryRegistry.PROP.get().defaultBlockState().setValue(LiteraryPropBlock.KIND, LiteraryPropBlock.Kind.CARCASS)
                            .setValue(LiteraryPropBlock.FACING, Direction.from2DDataValue(i)).setValue(LiteraryPropBlock.STAGE, i % 2 == 0 ? 1 : 3)
                    : p.serverLevel().getBlockState(at);
            p.connection.send(new ClientboundBlockUpdatePacket(at, state));
        }
    }

    /**
     * A blow at one of the packed carcasses. Each takes a few to tear loose (one, if the reader's own client has already
     * broken it out with a tool). The noise carries once his patience has run out.
     */
    public static boolean hit(ServerPlayer p, BlockPos at, boolean broken) {
        var r = BURIED.get(p.getUUID());
        if (r == null || r.p() != p) return false;
        int index = ElkCarcassMap.BURIAL.indexOf(at.subtract(r.b()));
        var d = LabyrinthData.get(p.server);
        var own = LiteraryVignettes.personal(d, p.getUUID(), PLACE);
        int left = own.getInt("ElkBurial");
        if (index < 0 || (left & 1 << index) == 0) return false;
        int[] hits = own.getIntArray("ElkBurialHits");
        if (hits.length != ElkCarcassMap.BURIAL.size()) hits = new int[ElkCarcassMap.BURIAL.size()];
        hits[index] = broken ? HITS : hits[index] + 1;
        own.putIntArray("ElkBurialHits", hits);
        own.putInt("ElkBurialHitsTotal", own.getInt("ElkBurialHitsTotal") + 1);
        var centre = at.getCenter();
        p.swing(InteractionHand.MAIN_HAND, true);
        sound(p, centre, SoundEvents.SLIME_SQUISH, .9F, .55F + p.getRandom().nextFloat() * .15F);
        sound(p, centre, SoundEvents.PLAYER_ATTACK_WEAK, .8F, .6F);
        p.serverLevel().sendParticles(p, new DustParticleOptions(new Vector3f(.42F, .05F, .04F), 1.1F), true, centre.x, centre.y, centre.z, 8, .3, .2, .3, .02);
        if (hits[index] >= HITS) {
            left &= ~(1 << index);
            own.putInt("ElkBurial", left);
            sound(p, centre, SoundEvents.MUD_BREAK, 1, .6F);
            if (free(left)) p.displayClientMessage(Component.literal("Something gives. There is air on the other side."), true);
        }
        own.putBoolean("ElkBurialResend", true);
        LiteraryVignettes.save(d, p.getUUID(), PLACE, own);
        ElkHunt.noise(p, at);
        return true;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void struck(PlayerInteractEvent.LeftClickBlock e) {
        if (!(e.getEntity() instanceof ServerPlayer p) || !buried(p)) return;
        var action = e.getAction();
        if (action == PlayerInteractEvent.LeftClickBlock.Action.START) hit(p, e.getPos(), false);
        else if (action == PlayerInteractEvent.LeftClickBlock.Action.STOP) hit(p, e.getPos(), true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void pushed(PlayerInteractEvent.RightClickBlock e) {
        if (!(e.getEntity() instanceof ServerPlayer p) || e.getHand() != InteractionHand.MAIN_HAND || !buried(p)) return;
        if (hit(p, e.getPos(), false)) e.setCanceled(true);
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent e) {
        DRAGGED.remove(e.getEntity().getUUID());
        BURIED.remove(e.getEntity().getUUID());
    }

    /** A returning reader who was still buried is buried again where they lie. */
    public static void resume(ServerPlayer p, BlockPos b, CompoundTag own) {
        if (own.getInt("ElkBurial") != 0 && !buried(p)) BURIED.put(p.getUUID(), new Reader(p, b));
    }

    public static void clearAll() {
        DRAGGED.clear();
        BURIED.clear();
    }

    /** A sound only this reader hears, from where it happens. */
    public static void sound(ServerPlayer p, Vec3 at, SoundEvent event, float volume, float pitch) {
        sound(p, at, BuiltInRegistries.SOUND_EVENT.wrapAsHolder(event), volume, pitch);
    }

    public static void sound(ServerPlayer p, Vec3 at, Holder<SoundEvent> event, float volume, float pitch) {
        p.connection.send(new ClientboundSoundPacket(event, SoundSource.HOSTILE, at.x, at.y, at.z, volume, pitch, p.getRandom().nextLong()));
    }
}
