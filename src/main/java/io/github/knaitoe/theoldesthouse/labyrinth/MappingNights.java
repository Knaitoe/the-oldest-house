package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWatchers;
import io.github.knaitoe.theoldesthouse.network.HouseFadePayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Mapping the Interior's four nights (0.4.201; docs/MAPPING_INTERIOR_REWORK.md). Every night the reader comes to at the front
 * window at 2:49. The father is one private actor per reader, heard by the bells at his ankles before he is seen, his outline a
 * little more of him each night; Dino is another, in his bed. Night one he crosses once and leaves a bead; night two the floor
 * plan maps the crawl past the roofline, deadfooting shows the hatch and Dino has a seizure; night three the dogs are gone and
 * he stands over Dino's bed; night four he goes down to the water under the house, and the reader holds him under, then lets
 * the bead go into the water after him or keeps it. On the next night seeing Dino readies the account.
 */
public final class MappingNights {
    static final LabyrinthPlace PLACE = LabyrinthPlace.MAPPING_INTERIOR;
    /** How long the crawl takes to hold him still, in ticks of crouching on him in the water. */
    public static final int HOLD = 160;
    /** Crouched still this long and the feet go to sleep; then they stay asleep this long. */
    public static final int NUMB = 80, DEADFOOT = 300;
    /** Where he waits out of sight, in the utility room. */
    public static final Vec3 PARKED = new Vec3(6.5, 2, -26.5);

    private MappingNights() {}

    /** The night it is: one to four, staying on four until the water is done with. */
    public static int night(CompoundTag own) { return Math.max(1, Math.min(4, own.getInt("Visit"))); }

    public static void arrive(ServerPlayer p, BlockPos b, CompoundTag own) {
        own.putInt("Route201", -1);own.putInt("Numb201", 0);own.putLong("Deadfoot201", 0);own.putInt("Hold201", 0);
        own.remove("Seizure201");
    }

    // ------------------------------------------------------------------------------------------------ the actors
    /** He walks through the house as he always walked it: nothing in it stops him, and nothing holds him up or lets him fall. */
    static LiteraryActor father(ServerPlayer p, CompoundTag own) {
        var a = LiteraryVignettes.actor(p, PLACE, "Father", LiteraryActor.SILHOUETTE, BlockPos.containing(PARKED), true);
        if (a != null) { a.noPhysics = true;a.setNoGravity(true); }
        return a;
    }
    static LiteraryActor dino(ServerPlayer p) { return LiteraryVignettes.actor(p, PLACE, "Brother", LiteraryActor.BROTHER, MappingHouse.DINO_BED, true); }
    private static void place(LiteraryActor a, BlockPos b, Vec3 rel) { a.moveTo(b.getX() + rel.x, b.getY() + rel.y, b.getZ() + rel.z, a.getYRot(), 0);a.setDeltaMovement(Vec3.ZERO); }
    private static Vec3 at(BlockPos b, Vec3 rel) { return new Vec3(b.getX() + rel.x, b.getY() + rel.y, b.getZ() + rel.z); }

    // ------------------------------------------------------------------------------------------------ every five ticks
    public static void reader(ServerPlayer p, BlockPos b, CompoundTag own) {
        var l = p.serverLevel();int visit = own.getInt("Visit"), night = night(own), t = own.getInt("ThisVisitTicks");
        if (night == 4 && own.getBoolean("Chosen") && visit > own.getInt("ChoiceVisit")) { after(p, b, own);return; }
        // 2:49, at the front window.
        if (own.getInt("WokeVisit201") != visit && t >= 5) {
            own.putInt("WokeVisit201", visit);own.putInt("WokeAt201", t);
            HousePackets.send(p, new HouseFadePayload(4, 20, 20));
            var w = at(b, MappingHouse.WAKE);p.teleportTo(l, w.x, w.y, w.z, 0, 0);p.setDeltaMovement(Vec3.ZERO);p.connection.resetPosition();
            cue(p, night == 1 ? "You are standing at the front window. You don't remember getting up. The clock on the stove says 2:49." : "The front window again. 2:49 again.");
        }
        var father = father(p, own);var dino = dino(p);
        if (dino != null) dinoTick(p, b, own, dino, night, t);
        if (father != null) switch (night) {
            case 1, 2 -> crossing(p, b, own, father, night, t);
            case 3 -> overBed(p, b, own, father, false);
            default -> water(p, b, own, father);
        }
        if (night >= 2) deadfoot(p, b, own);
        dogs(p, b, own, night);
        if (MappingHouse.beyondRoof(p, b) && !own.getBoolean("BeyondRoof")) {
            own.putBoolean("BeyondRoof", true);if (night == 2) own.putInt("Beat", visit);
            cue(p, "The house stopped a long way back. The dirt keeps going.");
        }
        // The shaft's lid lifts for whoever is climbing it.
        MappingHouse.lid(l, b, MappingHouse.inShaft(p, b));
    }

    /** Nights one and two: once he has been looked for, he crosses from the kitchen doorway down the hall, bells first. */
    static void crossing(ServerPlayer p, BlockPos b, CompoundTag own, LiteraryActor father, int night, int t) {
        int visit = own.getInt("Visit"), route = own.getInt("Route201");
        father.appearance(LiteraryActor.SILHOUETTE, night - 1);
        if (own.getInt("CrossedVisit201") == visit) { park(b, father);return; }
        if (route < 0) {
            park(b, father);
            boolean looking = p.getViewVector(1).dot(at(b, MappingHouse.CROSSING[1]).subtract(p.getEyePosition()).normalize()) > .55;
            if (own.getInt("WokeVisit201") == visit && t - own.getInt("WokeAt201") >= 60 && (looking || t - own.getInt("WokeAt201") >= 240)) {
                own.putInt("Route201", 1);place(father, b, MappingHouse.CROSSING[0]);father.setInvisible(false);
                bells(p, father.position(), .5F);
            }
            return;
        }
        var next = at(b, MappingHouse.CROSSING[route]);
        if (father.position().distanceToSqr(next) < .09) {
            if (route + 1 >= MappingHouse.CROSSING.length) {
                own.putInt("CrossedVisit201", visit);own.putInt("Route201", -1);park(b, father);if (night == 1) own.putInt("Beat", visit);
                if (night == 1 && !own.getBoolean("Yield_Bead")) bead(p, b, own);
                return;
            }
            own.putInt("Route201", route + 1);
        } else LiteraryMotion.toward(father, next, .45);
        if (t % 10 == 0) bells(p, father.position(), .35F);
        if (HouseWatchers.sees(p, father.getEyePosition())) own.putBoolean("SilhouetteSeen", true);
    }
    /** Out of sight in the utility room, invisible, until he is needed. */
    static void park(BlockPos b, LiteraryActor father) {
        if (father.position().distanceToSqr(at(b, PARKED)) > .25) place(father, b, PARKED);father.setInvisible(true);
    }
    /** The bead where he walked: one, for this reader alone. */
    static void bead(ServerPlayer p, BlockPos b, CompoundTag own) {
        own.putBoolean("Yield_Bead", true);
        var at = at(b, MappingHouse.BEAD);var item = new ItemEntity(p.serverLevel(), at.x, at.y + .05, at.z, LiteraryVignettes.mark(p, PLACE, "Bead", new ItemStack(LiteraryRegistry.BEAD.get())));
        item.setDeltaMovement(Vec3.ZERO);item.setTarget(p.getUUID());item.setUnlimitedLifetime();item.setNoPickUpDelay();p.serverLevel().addFreshEntity(item);
    }

    /** Night three, and the start of night four: standing over Dino's bed. */
    static void overBed(ServerPlayer p, BlockPos b, CompoundTag own, LiteraryActor father, boolean last) {
        int route = own.getInt("Route201");father.appearance(LiteraryActor.SILHOUETTE, last ? 3 : 2);
        if (route < 0 && !own.getBoolean(last ? "FatherWent201" : "OverSeenVisit201_" + own.getInt("Visit"))) {
            place(father, b, MappingHouse.OVER_DINO);father.setInvisible(false);father.setYRot(90);father.setYHeadRot(90);father.yBodyRot = 90;
            if (MappingHouse.boysRoom(p, b) && HouseWatchers.sees(p, father.getEyePosition())) {
                own.putInt("Route201", 0);cue(p, last ? "He straightens up from Dino's bed. The bells go out into the hall." : "He is bent over Dino's bed. Then he is in the hall, going back the way he always goes.");
                if (!last) { own.putBoolean("OverSeenVisit201_" + own.getInt("Visit"), true);own.putInt("Beat", own.getInt("Visit")); }
            }
            if (p.serverLevel().getGameTime() % 40 == 0) bells(p, father.position(), .2F);
            return;
        }
        if (route < 0) { if (!last) park(b, father);return; }
        // Out of the boys' room, up the hall the other way, in at the utility room door.
        Vec3[] out = {new Vec3(-.5, 2, -21.5), new Vec3(.5, 2, -22.5), new Vec3(.5, 2, -23.5), new Vec3(2.5, 2, -23.5), new Vec3(5.5, 2, -24.5)};
        var next = at(b, out[Math.min(route, out.length - 1)]);
        if (father.position().distanceToSqr(next) < .09) {
            if (route + 1 >= out.length) {
                own.putInt("Route201", -1);
                if (last) { own.putBoolean("FatherWent201", true);MappingHouse.plywood(p.serverLevel(), b, true);place(father, b, MappingHouse.WATER);cue(p, "The bells go down through the floor.");sound(p, Vec3.atCenterOf(b.offset(MappingHouse.HATCH)), SoundEvents.WOODEN_TRAPDOOR_OPEN, .6F, .7F); }
                else park(b, father);
                return;
            }
            own.putInt("Route201", route + 1);
        } else LiteraryMotion.toward(father, next, .5);
        if (p.serverLevel().getGameTime() % 10 == 0) bells(p, father.position(), .35F);
    }

    /** Night four: over the bed, then the water, and holding him under it. */
    static void water(ServerPlayer p, BlockPos b, CompoundTag own, LiteraryActor father) {
        if (!own.getBoolean("FatherWent201")) { overBed(p, b, own, father, true);return; }
        var l = p.serverLevel();father.setInvisible(false);
        if (own.getBoolean("Drowned201")) {
            // Face down and still.
            father.appearance(LiteraryActor.SILHOUETTE, 3);place(father, b, MappingHouse.WATER.add(0, -.35, 0));father.setXRot(90);
            tossed(p, b, own);
            if (!MappingHouse.inWater(p, b) && !own.getBoolean("Chosen") && (MappingHouse.inShaft(p, b) || p.getY() > b.getY() - 2.5)) choose(p, own);
            return;
        }
        father.appearance(LiteraryActor.SILHOUETTE, 3);
        if (!MappingHouse.inWater(p, b)) { place(father, b, MappingHouse.WATER);return; }
        if (!own.getBoolean("Rose201")) { own.putBoolean("Rose201", true);cue(p, "He comes up out of the water. You have to hold him under.");sound(p, father.position(), SoundEvents.PLAYER_SPLASH, 1F, .8F); }
        int hold = own.getInt("Hold201");boolean on = p.isShiftKeyDown() && p.position().distanceToSqr(father.position().add(0, .3, 0)) < 2.6;
        if (on) {
            hold += 5;l.sendParticles(ParticleTypes.SPLASH, father.getX(), father.getY() + .9, father.getZ(), 8, .4, .1, .4, .2);
            // He thrashes, and every so often he throws the reader back off him.
            if (hold % 35 == 0 && hold < HOLD) {
                var away = p.position().subtract(father.position()).multiply(1, 0, 1).normalize().scale(.7);
                p.setDeltaMovement(away.x, .25, away.z);p.hurtMarked = true;sound(p, father.position(), SoundEvents.GENERIC_SPLASH, 1F, .9F);
                cue(p, "He throws you off him. Get back on him.");
            }
        } else hold = Math.max(0, hold - 3);
        own.putInt("Hold201", hold);
        place(father, b, MappingHouse.WATER.add(0, on ? -.25 : 0, 0));
        if (hold >= HOLD) {
            own.putBoolean("Drowned201", true);sound(p, father.position(), SoundEvents.AMETHYST_BLOCK_CHIME, .15F, .5F);
            cue(p, p.getInventory().hasAnyMatching(s -> s.is(LiteraryRegistry.BEAD.get())) ? "He stops. The water goes flat around him. The bead is still in your hand." : "He stops. The water goes flat around him.");
        }
    }
    /** The bead dropped into the water after him: let go. */
    static void tossed(ServerPlayer p, BlockPos b, CompoundTag own) {
        if (own.getBoolean("LetGo201")) return;
        var box = new AABB(b.getX() + 2, b.getY() - 6, b.getZ() - 67, b.getX() + 8, b.getY() - 3.5, b.getZ() - 60);
        for (var item : p.serverLevel().getEntitiesOfClass(ItemEntity.class, box, e -> e.getItem().is(LiteraryRegistry.BEAD.get()))) {
            item.discard();own.putBoolean("LetGo201", true);cue(p, "The bead goes down into the water after him. You don't see where.");
            sound(p, item.position(), SoundEvents.AMETHYST_BLOCK_CHIME, .2F, 1.6F);break;
        }
    }
    static void choose(ServerPlayer p, CompoundTag own) {
        own.putBoolean("Chosen", true);own.putInt("ChoiceVisit", own.getInt("Visit"));own.putInt("Beat", own.getInt("Visit"));
        own.putBoolean("PileChosen", true);own.putBoolean("PileBroken", own.getBoolean("LetGo201"));
    }

    /** The next night: Dino at the kitchen table, or asleep and hard to wake with the bells in the hall. Seeing him readies it. */
    static void after(ServerPlayer p, BlockPos b, CompoundTag own) {
        var father = father(p, own);var dino = dino(p);boolean free = own.getBoolean("LetGo201");
        if (own.getInt("WokeVisit201") != own.getInt("Visit")) {
            own.putInt("WokeVisit201", own.getInt("Visit"));own.putInt("WokeAt201", own.getInt("ThisVisitTicks"));
            HousePackets.send(p, new HouseFadePayload(4, 20, 20));var w = at(b, MappingHouse.WAKE);p.teleportTo(p.serverLevel(), w.x, w.y, w.z, 0, 0);p.connection.resetPosition();
            cue(p, free ? "The front window. The clock on the stove has gone past 2:49." : "The front window. 2:49. Somewhere in the house, bells.");
        }
        if (father != null) {
            if (free) { park(b, father); }
            else { place(father, b, new Vec3(.5, 2, -23.5));father.setInvisible(false);father.appearance(LiteraryActor.SILHOUETTE, 3);if (p.serverLevel().getGameTime() % 60 == 0) bells(p, father.position(), .2F); }
        }
        if (dino != null) {
            if (free) { dino.appearance(LiteraryActor.BROTHER, 0);place(dino, b, new Vec3(5.5, 2, -14.5));dino.setYRot(180); }
            else { dino.appearance(LiteraryActor.BROTHER, 3);place(dino, b, Vec3.atBottomCenterOf(MappingHouse.DINO_BED)); }
            if (p.distanceToSqr(dino) < 25 && HouseWatchers.sees(p, dino.getEyePosition()))
                LiteraryVignettes.ready(p, PLACE, own, free ? (own.getBoolean("Drowned201") ? "drowned_him_again_and_let_the_bead_go" : "let_the_bead_go") : own.getBoolean("Drowned201") ? "drowned_him_and_kept_the_bead" : "kept_the_bead");
        }
    }

    /** Dino in his bed, a little further gone each night; on night two the seizure. */
    static void dinoTick(ServerPlayer p, BlockPos b, CompoundTag own, LiteraryActor dino, int night, int t) {
        dino.appearance(LiteraryActor.BROTHER, Math.min(3, night));
        if (night == 2 && own.getInt("CrossedVisit201") == own.getInt("Visit") && !own.contains("Seizure201") && MappingHouse.indoors(p, b)) own.putInt("Seizure201", t);
        if (!own.contains("Seizure201") || own.getBoolean("SeizureDone201")) { place(dino, b, Vec3.atBottomCenterOf(MappingHouse.DINO_BED));return; }
        int age = t - own.getInt("Seizure201");
        var bed = Vec3.atBottomCenterOf(MappingHouse.DINO_BED);var r = p.getRandom();
        place(dino, b, bed.add((r.nextFloat() - .5) * .08, 0, (r.nextFloat() - .5) * .08));dino.setYRot(dino.getYRot() + (r.nextFloat() - .5F) * 12F);
        if (age == 0) cue(p, "From the boys' room, the bed frame knocking against the wall.");
        if (t % 10 == 0) sound(p, dino.position(), SoundEvents.WOOD_HIT, .5F, .8F + r.nextFloat() * .2F);
        boolean kneeling = p.isShiftKeyDown() && p.distanceToSqr(dino) < 5;
        own.putInt("Kneel201", kneeling ? own.getInt("Kneel201") + 5 : 0);
        if (own.getInt("Kneel201") >= 40) { own.putBoolean("SeizureDone201", true);own.putBoolean("DinoHelped", true);cue(p, "You turn him onto his side and keep a hand on his back until it passes. He sleeps."); }
        else if (age >= 400) { own.putBoolean("SeizureDone201", true);cue(p, "It passes on its own. He sleeps."); }
    }

    /** Crouched still until the feet go to sleep, then walked on: his steps show, and the seam in the utility room floor. */
    static void deadfoot(ServerPlayer p, BlockPos b, CompoundTag own) {
        long now = p.serverLevel().getGameTime();boolean still = p.isShiftKeyDown() && p.getDeltaMovement().horizontalDistanceSqr() < 4.0E-4 && MappingHouse.indoors(p, b);
        if (now < own.getLong("Deadfoot201")) {
            for (var step : MappingHouse.CROSSING) particle(p, at(b, step).add(0, .05, 0));
            if (!MappingHouse.hatchShown(p.serverLevel(), b)) { MappingHouse.showHatch(p.serverLevel(), b);cue(p, "Where he walked, the carpet keeps his steps. There is a seam in the utility room floor."); }
            return;
        }
        int numb = still ? own.getInt("Numb201") + 5 : 0;own.putInt("Numb201", numb);
        if (numb >= NUMB) {
            own.putInt("Numb201", 0);own.putLong("Deadfoot201", now + DEADFOOT);own.putBoolean("Deadfooted201", true);
            p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, DEADFOOT, 0, false, false, true));
            cue(p, "Your feet have gone to sleep. You walk on them anyway.");
        }
    }

    /** The neighbour's dogs: in their run on the first two nights, gone after. */
    static void dogs(ServerPlayer p, BlockPos b, CompoundTag own, int night) {
        var l = p.serverLevel();var gate = b.offset(MappingHouse.DOG_GATE);var s = l.getBlockState(gate);
        if (night >= 3) {
            for (int i = 0; i < 2; i++) if (own.hasUUID("Dog201_" + i) && l.getEntity(own.getUUID("Dog201_" + i)) instanceof Wolf w) w.discard();
            if (s.getBlock() instanceof FenceGateBlock && !s.getValue(FenceGateBlock.OPEN)) l.setBlock(gate, s.setValue(FenceGateBlock.OPEN, true), 3);
            var at = b.offset(MappingHouse.REMAINS);
            if (!l.getBlockState(at.below()).is(Blocks.BONE_BLOCK)) { l.setBlock(at.below(), Blocks.BONE_BLOCK.defaultBlockState(), 3);l.setBlock(at, Blocks.SKELETON_SKULL.defaultBlockState(), 3); }
            if (!own.getBoolean("RemainsSeen201") && p.distanceToSqr(Vec3.atCenterOf(at)) < 9) { own.putBoolean("RemainsSeen201", true);cue(p, "What is left of the neighbour's dogs. Somebody tied a rag over the eyes of the one that still has a face."); }
            return;
        }
        for (int i = 0; i < 2; i++) {
            if (own.hasUUID("Dog201_" + i) && l.getEntity(own.getUUID("Dog201_" + i)) instanceof Wolf w && w.isAlive()) {
                w.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, p.getEyePosition());
                if (p.distanceToSqr(w) < 144 && l.getGameTime() % 30 == i * 15) sound(p, w.position(), SoundEvents.WOLF_GROWL, .8F, .9F);
                continue;
            }
            var dog = EntityType.WOLF.create(l);if (dog == null) continue;
            var at = at(b, MappingHouse.DOGS[i]);dog.moveTo(at.x, at.y, at.z, 90, 0);dog.setNoAi(true);dog.setPersistenceRequired();dog.setInvulnerable(true);dog.setSilent(true);
            dog.setRemainingPersistentAngerTime(Integer.MAX_VALUE);l.addFreshEntity(dog);own.putUUID("Dog201_" + i, dog.getUUID());
        }
    }

    /** Hands on the floor plan, the plywood door before it will lift. */
    public static boolean click(ServerPlayer p, BlockPos b, BlockPos rel, CompoundTag own) {
        if (rel.equals(MappingHouse.PLYWOOD) && !own.getBoolean("FatherWent201")) { cue(p, "It won't lift. Something underneath is holding it shut.");return true; }
        if (rel.equals(MappingHouse.MOM_DOOR) || rel.equals(MappingHouse.MOM_DOOR.above())) { cue(p, "Her door is shut. She has work in the morning.");return true; }
        return false;
    }
    /** The floor plan as a real map of the lot, given with the plan: under the house its mark slides out past the roof. */
    public static void floorPlan(ServerPlayer p, BlockPos b, CompoundTag own) {
        if (own.getBoolean("Yield_FloorPlan")) return;
        var map = MapItem.create(p.serverLevel(), b.getX(), b.getZ() - 40, (byte) 0, true, true);
        map.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.literal("The floor plan").withStyle(s -> s.withItalic(false)));
        LiteraryVignettes.reward(p, PLACE, own, "FloorPlan", map);
    }

    // ------------------------------------------------------------------------------------------------ small things
    static void bells(ServerPlayer p, Vec3 at, float volume) { sound(p, at, SoundEvents.AMETHYST_BLOCK_CHIME, volume, 1.7F + p.getRandom().nextFloat() * .2F); }
    static void sound(ServerPlayer p, Vec3 at, SoundEvent s, float volume, float pitch) {
        p.connection.send(new ClientboundSoundPacket(Holder.direct(s), SoundSource.AMBIENT, at.x, at.y, at.z, volume, pitch, p.getRandom().nextLong()));
    }
    static void particle(ServerPlayer p, Vec3 at) { p.connection.send(new ClientboundLevelParticlesPacket(ParticleTypes.WHITE_ASH, false, at.x, at.y, at.z, .15F, 0, .15F, 0, 2)); }
    static void cue(ServerPlayer p, String line) { p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY), true); }
}
