package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseCalendar;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * The contests' trades (0.4.191), from the success-and-failure audit: winning sometimes takes, failing sometimes gives, each
 * from the room's own story, never wounding, never touching a House original or what the Mother keeps.
 * <ul>
 * <li><b>Ted the Caver, won:</b> the cave's breath, a quarter more air under water for good; and the cave keeps a hand on their
 * line: three times, now and then while they lay a trail, the spool's winds pay out and it loses some or all of what is left.
 * Their torches, chalk and other navigation are never touched.</li>
 * <li><b>The passing boots (elk carcasses), won:</b> the hides' smell comes out with them; animals shy from them until they wash
 * in water.</li>
 * <li><b>The second apron (diner):</b> won after tasting the meal, a moment of nausea from what was on the plate; failed, they
 * leave in her black frost, frozen and slow a few seconds, never hurt.</li>
 * <li><b>The floorboards (Tell-Tale), failed:</b> calm is taken; the heart starts louder each time they come back, up to three.</li>
 * </ul>
 * The Shallows' failure sends the boys home in {@link Shallows} itself.
 */
@EventBusSubscriber(modid = TheOldestHouse.MOD_ID)
public final class ContestTrades {
    public static final String STATE = "contest_trades_0191";
    /** Unwindings the cave takes before it lets go, and the chance on each stretch of trail laid (at most once a House day). */
    public static final int UNWINDS = 3, UNWIND_CHANCE = 200;
    /** Under water, one breath back every this many ticks: a quarter more air. */
    public static final int BREATH_EVERY = 4;
    /** The hides' smell reaches animals this far away. */
    public static final double SMELL = 10;
    /** Tell-Tale: failures remembered at most, and how much each starts the heart at. */
    public static final int UNEASE_MAX = 3;
    public static final double UNEASE_HEAT = 20;
    private ContestTrades() {}

    public static CompoundTag record(ServerPlayer p) { return LabyrinthData.get(p.server).stateEntry(STATE, p.getUUID().toString()); }
    private static void save(ServerPlayer p, CompoundTag own) { LabyrinthData.get(p.server).setStateEntry(STATE, p.getUUID().toString(), own); }
    private static void say(ServerPlayer p, String line) { p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), false); }

    // ------------------------------------------------------------------
    // Ted the Caver
    // ------------------------------------------------------------------

    public static boolean caver(ServerPlayer p) { return WitnessAccount.has(LabyrinthData.get(p.server), p.getUUID(), WitnessAccount.Story.TED_CAVER); }

    /** The cave's breath: under water, a reader who retraced the squeeze loses air three quarters as fast. */
    public static boolean breathe(ServerPlayer p) {
        if (!p.isEyeInFluid(net.minecraft.tags.FluidTags.WATER) || p.getAirSupply() >= p.getMaxAirSupply() || p.getAirSupply() <= -20) { UNDER.remove(p.getUUID());return false; }
        // Counted per reader under water, so every fourth breath lost comes back however their ticks are driven.
        if (UNDER.merge(p.getUUID(), 1, Integer::sum) % BREATH_EVERY != 0 || !caver(p)) return false;
        p.setAirSupply(p.getAirSupply() + 1);
        return true;
    }
    private static final java.util.Map<java.util.UUID, Integer> UNDER = new java.util.HashMap<>();

    /** After a stretch of trail is laid with the spool: now and then the cave pulls the line back through their fingers. */
    public static void laid(ServerPlayer p, ItemStack spool) {
        if (spool.isEmpty() || p.getRandom().nextInt(UNWIND_CHANCE) != 0 || !caver(p)) return;
        unwind(p, spool, p.getRandom().nextInt(6) == 0);
    }
    /** One unwinding: a share of what is left on the spool, or all of it; the third is the last. */
    public static boolean unwind(ServerPlayer p, ItemStack spool, boolean all) {
        var own = record(p);
        long today = HouseCalendar.today(p.server);
        int done = own.getInt("Unwound");
        if (done >= UNWINDS || (own.contains("UnwoundDay") && own.getLong("UnwoundDay") == today)) return false;
        int left = spool.getMaxDamage() - spool.getDamageValue();
        int lost = all ? left : Math.max(1, Math.round(left * (.35F + .3F * p.getRandom().nextFloat())));
        own.putInt("Unwound", done + 1);own.putLong("UnwoundDay", today);save(p, own);
        spool.hurtAndBreak(lost, p, EquipmentSlot.MAINHAND);
        say(p, done + 1 >= UNWINDS ? "The line pays out through your fingers, back the way you came. Then it goes slack, as if something at the far end let go."
                : all ? "The line pays out through your fingers, back the way you came, all of it." : "The line pays out through your fingers, back the way you came.");
        return true;
    }

    // ------------------------------------------------------------------
    // The passing boots
    // ------------------------------------------------------------------

    /** Leaving by the service path under the hides: their smell comes out too. */
    public static void hides(ServerPlayer p) {
        var own = record(p);if (own.getBoolean("HideSmell")) return;
        own.putBoolean("HideSmell", true);save(p, own);
        say(p, "The smell of the hides has come out with you.");
    }
    public static boolean smells(ServerPlayer p) { return record(p).getBoolean("HideSmell"); }
    /** Animals shy from the smell; water washes it off. Returns how many animals turned away. */
    public static int smell(ServerPlayer p) {
        if (!smells(p)) return 0;
        if (p.isInWater()) {
            var own = record(p);own.remove("HideSmell");save(p, own);
            say(p, "The water takes the smell of the hides.");
            return 0;
        }
        int shied = 0;
        for (var animal : p.serverLevel().getEntitiesOfClass(Animal.class, p.getBoundingBox().inflate(SMELL),
                a -> a.isAlive() && !a.isLeashed() && !(a instanceof TamableAnimal t && t.isTame()))) {
            var away = animal.position().subtract(p.position()).multiply(1, 0, 1);
            if (away.lengthSqr() < 1e-4) away = new Vec3(1, 0, 0);
            var to = animal.position().add(away.normalize().scale(8));
            animal.getNavigation().moveTo(to.x, to.y, to.z, 1.6);
            shied++;
        }
        return shied;
    }

    // ------------------------------------------------------------------
    // The second apron
    // ------------------------------------------------------------------

    /** Won after tasting her meal: what was on the plate turns over in them for a moment. */
    public static void nausea(ServerPlayer p) {
        p.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 120, 0, false, false, true));
        say(p, "Something on the plate turns over in you.");
    }
    /** Failed: they leave in her black frost, frozen and slowed a few seconds, never hurt. */
    public static void blackFrost(ServerPlayer p) {
        // One tick short of fully frozen: the frost shows and slows, and freezing never wounds.
        p.setTicksFrozen(Math.max(p.getTicksFrozen(), p.getTicksRequiredToFreeze() - 1));
        p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1, false, false, true));
        p.serverLevel().sendParticles(p, ParticleTypes.SQUID_INK, true, p.getX(), p.getY() + 1, p.getZ(), 30, .4, .6, .4, .01);
    }

    // ------------------------------------------------------------------
    // The floorboards
    // ------------------------------------------------------------------

    /** The heart peaked and sent them out: the next time, it starts louder. */
    public static int unsettle(ServerPlayer p) {
        var own = record(p);int unease = Math.min(UNEASE_MAX, own.getInt("Unease") + 1);own.putInt("Unease", unease);save(p, own);return unease;
    }
    /** Where the heart starts when this reader walks back in. */
    public static double startingHeat(ServerPlayer p) { return UNEASE_HEAT * record(p).getInt("Unease"); }

    @SubscribeEvent public static void tick(PlayerTickEvent.Post e) {
        if (!(e.getEntity() instanceof ServerPlayer p) || !p.isAlive() || p.isSpectator()) return;
        breathe(p);
        if (p.tickCount % 20 == 0) smell(p);
    }
}
