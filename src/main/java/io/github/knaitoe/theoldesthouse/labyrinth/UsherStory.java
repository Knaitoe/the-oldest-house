package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Usher's people and its last night (0.4.182; shared by those there together and tailored to the reader, 0.4.183).
 * <p>Roderick sits in his chair in the studio, on stranger phase {@link #RODERICK}, and talks when spoken to; whoever is in
 * the room hears him. Each reader's storm night is every visit after the one on which they first found the lid chosen.
 * While anyone there is on theirs, the storm is up for everyone in the house: the vapour about it glows behind the windows
 * (their glow stages, sent to each reader's own view), the casements rattle and an exhalation drifts over the tarn.
 * <p>On a closed lid he asks to be read to. A hand on the strewn books begins the old romance for everyone there, three
 * passages read by the one who picked it up, each answered from the vault below. Then she stands at the studio doors,
 * goes to him, and the two go down together, for everyone who was in it. The fissure opens on the front with the moon's
 * light through it, and each reader who gets back over the causeway to the bank has fled as the house fell. On an open
 * lid the storm comes too; once she has been seen standing in the wing she comes to the studio doors, and does not come in.
 * <p>Nothing here changes a block: the glow, the open doors and the opened fissure are views sent to each reader. A
 * reader who has seen the house fall no longer sees Roderick, and Madeline at the doors is seen only by that night's readers.
 */
public final class UsherStory {
    /** Roderick's stranger phase. */
    public static final int RODERICK = 38;
    /** How long each passage of the romance is read before the vault answers it, and before the next is begun, in ticks. */
    public static final int ANSWER = 60, PASSAGE = 110;
    /** How long she stands on the threshold before she goes to him. */
    public static final int THRESHOLD = 40;
    /** The night's stage once the three passages are read: she is at the doors. */
    public static final int AT_DOOR = 4;
    /** How long the house may stand empty before an unfinished night is forgotten. */
    public static final int FORGET = 200;
    /** 0.4.184: how long after the fall a reader has to get back over the causeway before the house closes over them. Tests shorten it. */
    public static int FLIGHT = 400;
    private static final LabyrinthPlace PLACE = LabyrinthPlace.USHER;
    private static final String NAME = "Roderick", MADELINE = "Madeline", NIGHT_TAG = "UsherNight0507";

    private UsherStory() {}

    // ------------------------------------------------------------------------------------------------ who, and when

    /** The reader's own storm night: every visit after the visit on which they first found the lid chosen. */
    public static boolean ownStorm(CompoundTag own, CompoundTag world) {
        return world.contains("LidClosed") && own.contains("LidKnown0507") && own.getInt("Visit") > own.getInt("LidKnown0507");
    }
    /** The storm night for this reader: their own, or the one someone in the house with them is on. */
    public static boolean storm(ServerPlayer p, CompoundTag own) {
        var d = LabyrinthData.get(p.server);
        var world = LiteraryVignettes.shared(d, PLACE);
        var night = world.getCompound("Night0507");
        return ownStorm(own, world) || night.contains("Up") && p.serverLevel().getGameTime() - night.getLong("Up") <= 10;
    }
    /** The night's stage (0 before the reading, 1 to 3 the passages, {@link #AT_DOOR}); saved with the house. */
    public static int stage(LabyrinthData d) { return LiteraryVignettes.shared(d, PLACE).getCompound("Night0507").getInt("Stage"); }
    /** How many of the passages the vault has answered tonight. */
    public static int answered(LabyrinthData d) { return LiteraryVignettes.shared(d, PLACE).getCompound("Night0507").getInt("Heard"); }

    private static CompoundTag night(ServerPlayer p) { return LiteraryVignettes.shared(LabyrinthData.get(p.server), PLACE).getCompound("Night0507"); }
    private static void night(ServerPlayer p, CompoundTag n) {
        var d = LabyrinthData.get(p.server);
        var world = LiteraryVignettes.shared(d, PLACE);
        world.put("Night0507", n);
        LiteraryVignettes.shared(d, PLACE, world);
    }
    /** The readers in the house now. */
    static List<ServerPlayer> present(ServerPlayer p) {
        var d = LabyrinthData.get(p.server);
        var out = new ArrayList<ServerPlayer>();
        for (var q : p.server.getPlayerList().getPlayers())
            if (q.isAlive() && !q.isSpectator() && LiteraryVignettes.current(q) == PLACE && LiteraryVignettes.personal(d, q.getUUID(), PLACE).getBoolean("Here")) out.add(q);
        if (!out.contains(p)) out.add(p);
        return out;
    }
    /** A reader's record: the one being ticked is the caller's, the rest are read and written back. */
    private interface Edit { void apply(ServerPlayer q, CompoundTag own); }
    private static void each(ServerPlayer p, CompoundTag mine, List<ServerPlayer> who, Edit edit) {
        var d = LabyrinthData.get(p.server);
        for (var q : who) {
            if (q == p) { edit.apply(q, mine); continue; }
            var own = LiteraryVignettes.personal(d, q.getUUID(), PLACE);
            edit.apply(q, own);
            LiteraryVignettes.save(d, q.getUUID(), PLACE, own);
        }
    }
    private static CompoundTag own(ServerPlayer p, CompoundTag mine, ServerPlayer q) { return q == p ? mine : LiteraryVignettes.personal(LabyrinthData.get(p.server), q.getUUID(), PLACE); }
    /** Tonight's readers in the house: those who took part in the night and have not yet seen it end. */
    private static List<ServerPlayer> readers(ServerPlayer p, CompoundTag mine, int id) {
        var out = new ArrayList<ServerPlayer>();
        for (var q : present(p)) { var own = own(p, mine, q); if (own.getInt("NightId0507") == id && !gone(own)) out.add(q); }
        return out;
    }
    /** Whoever in the house still sees him. */
    private static List<ServerPlayer> listeners(ServerPlayer p, CompoundTag mine) {
        var out = new ArrayList<ServerPlayer>();
        for (var q : present(p)) if (!gone(own(p, mine, q))) out.add(q);
        return out;
    }

    /** He is gone for this reader: they have seen the house fall, or (0.4.184) it closed over them on this visit. */
    static boolean gone(CompoundTag own) { return own.getBoolean("Fell0507") || own.contains("BuriedVisit0507") && own.getInt("BuriedVisit0507") == own.getInt("Visit"); }
    /** Roderick is not there for a reader who has seen the house fall; Madeline at the doors is only that night's. */
    public static boolean sees(ServerPlayer viewer, LiteraryActor a) {
        if (!a.scene().equals(PLACE.id())) return true;
        var own = LiteraryVignettes.personal(LabyrinthData.get(viewer.server), viewer.getUUID(), PLACE);
        if (a.role() == LiteraryActor.STRANGER && a.phase() == RODERICK) return !gone(own);
        if (a.getPersistentData().contains(NIGHT_TAG)) return own.getInt("NightId0507") == a.getPersistentData().getInt(NIGHT_TAG) && !own.getBoolean("Fell0507");
        return true;
    }

    // ------------------------------------------------------------------------------------------------ each reader's turn

    /** Each present reader's turn, every five ticks, from {@link LiteraryVignettes}. */
    static void tick(ServerPlayer p, BlockPos b, CompoundTag own) {
        var d = LabyrinthData.get(p.server);
        long now = p.serverLevel().getGameTime();
        var world = LiteraryVignettes.shared(d, PLACE);
        // Each reader knows the lid was chosen from the visit they first found it so: the chooser, those who watched, or a later reader.
        if (world.contains("LidClosed") && !own.contains("LidKnown0507"))
            own.putInt("LidKnown0507", own.getBoolean("LidChosen") ? own.getInt("ChoiceVisit") : own.getInt("Visit"));
        var n = night(p);
        if (n.getInt("Stage") > 0 && now - n.getLong("Seen") > FORGET) {
            // The house stood empty in the middle of a night: the night is forgotten, and she with it.
            forget(p, MADELINE);
            long up = n.getLong("Up");
            n = new CompoundTag();
            if (up != 0) n.putLong("Up", up);
        }
        n.putLong("Seen", now);
        if (ownStorm(own, world)) n.putLong("Up", now);
        night(p, n);
        // A reader still in the house joins a night already begun.
        if (n.getInt("Stage") > 0 && !gone(own)) own.putInt("NightId0507", n.getInt("Id"));

        var roderick = LiteraryVignettes.actor(p, PLACE, NAME, LiteraryActor.STRANGER, UsherHouse.CHAIR, false);
        if (roderick != null) seat(p, own, roderick);
        boolean storm = storm(p, own);
        if (storm) weather(p, b, own);
        if (storm && n.getLong("Tick") != now) {
            // The night's own beats run once a tick, whoever's turn in the storm it is.
            n.putLong("Tick", now);
            night(p, n);
            if (world.getBoolean("LidClosed")) reading(p, b, own, roderick);
            else if (world.contains("LidClosed")) door(p, b, own, roderick);
        }
        if (storm && n.getInt("Stage") >= AT_DOOR && own.getInt("NightId0507") == n.getInt("Id") || storm && LiteraryVignettes.shared(d, PLACE).getBoolean("AtDoor0507")) doors(p, b);
        if (own.getBoolean("Fell0507")) {
            // The opened fissure stays open in this reader's view, and getting back to the bank is the flight.
            if (own.getInt("Present") % 100 == 0) { fissure(p, b); glow(p, b, true); }
            if (!own.getBoolean("Ready")) {
                if (p.blockPosition().getZ() - b.getZ() >= -3 && p.getY() >= b.getY() - 1.5) { own.remove("Buried0507"); giveBack(p, own); LiteraryVignettes.ready(p, PLACE, own, "fled_as_the_house_fell"); }
                else if (now - own.getLong("FellAt0507") > FLIGHT) buried(p, b, own);
            }
        }
    }

    /**
     * 0.4.184, the failure: still inside when the house comes down. The tarn gives the reader back on the bank and nothing is
     * resolved. Roderick is gone for the rest of this visit; the house stands again for the next. 0.4.185, the trade (owner:
     * "Winning sometimes takes, too. Failing sometimes gives."): the tarn keeps what was in the reader's hands until a later
     * night they get out in time, and gives them a leaf of the vault's register with their own name on it. Nothing follows
     * them home: in Poe nothing follows the narrator out.
     */
    private static void buried(ServerPlayer p, BlockPos b, CompoundTag own) {
        var l = p.serverLevel();
        own.putBoolean("Fell0507", false);
        own.putBoolean("Buried0507", true);
        own.putBoolean("BuriedOnce0507", true);
        own.putInt("BuriedVisit0507", own.getInt("Visit"));
        own.putInt("Beat", own.getInt("Visit"));
        own.putInt("BuriedDay0507", (int) (p.server.overworld().getDayTime() / 24000L) + 1);
        own.putInt("Buried0507Count", own.getInt("Buried0507Count") + 1);
        for (var cell : opened()) p.connection.send(new ClientboundBlockUpdatePacket(l, b.offset(cell)));
        for (int i = 0; i < 2; i++) for (int y = 0; y < 2; y++) p.connection.send(new ClientboundBlockUpdatePacket(l, b.offset(UsherHouse.STUDIO_DOOR.getX() + i, y, UsherHouse.STUDIO_DOOR.getZ())));
        LiteraryVignettes.heard(p, SoundEvents.DEEPSLATE_BREAK, p.blockPosition().above(3), 1F, .3F);
        LiteraryVignettes.heard(p, SoundEvents.GENERIC_SPLASH, b.offset(0, -1, -4), 1F, .6F);
        p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DARKNESS, 80, 0, false, false));
        p.stopRiding();
        p.teleportTo(l, b.getX() + .5, b.getY(), b.getZ() - 1.5, 180, 0);
        p.setDeltaMovement(Vec3.ZERO);
        p.connection.resetPosition();
        var held = p.getMainHandItem();boolean kept = !own.contains(KEPT) && tarnTakes(held);
        if (kept) { own.put(KEPT, held.save(p.registryAccess())); own.putBoolean("TarnKeptOnce0507", true); p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, net.minecraft.world.item.ItemStack.EMPTY); }
        p.displayClientMessage(Component.literal(kept ? "The house came down over you. The tarn gave you back, and kept what you had in your hands." : "The house came down over you. The tarn gave you back.").withStyle(net.minecraft.ChatFormatting.GRAY, net.minecraft.ChatFormatting.ITALIC), false);
        LiteraryVignettes.reward(p, PLACE, own, "RegisterLeaf", registerLeaf(p));
    }

    /** What the tarn is keeping of a reader the house closed over: one stack from their hands. */
    public static final String KEPT = "TarnKept0507";
    /** Only an ordinary thing: never anything of the House's, an original, or anything bound to a reader. */
    public static boolean tarnTakes(net.minecraft.world.item.ItemStack s) {
        return !s.isEmpty() && !s.has(net.minecraft.core.component.DataComponents.CUSTOM_DATA) && !net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(s.getItem()).getNamespace().equals(io.github.knaitoe.theoldesthouse.TheOldestHouse.MOD_ID);
    }
    /** On the night they get out in time, what the tarn kept lies on the bank by the causeway. */
    static void giveBack(ServerPlayer p, CompoundTag own) {
        if (!own.contains(KEPT)) return;
        var s = net.minecraft.world.item.ItemStack.parseOptional(p.registryAccess(), own.getCompound(KEPT));own.remove(KEPT);
        if (s.isEmpty()) return;
        p.displayClientMessage(Component.literal("On the bank by the causeway: what the tarn kept of yours. " + s.getHoverName().getString() + ".").withStyle(net.minecraft.ChatFormatting.GRAY, net.minecraft.ChatFormatting.ITALIC), false);
        LiteraryVignettes.give(p, s);
    }
    /** The failure's keepsake: a leaf of the register R. keeps of who is carried down beneath the house. */
    static net.minecraft.world.item.ItemStack registerLeaf(ServerPlayer p) {
        String name = p.getGameProfile().getName();
        return io.github.knaitoe.theoldesthouse.house.HouseWriting.book("A leaf of the vault register", "R.", io.github.knaitoe.theoldesthouse.house.HouseWriting.WritingStyle.PLAIN, List.of(
            "Carried down beneath the house on the night of the storm:\n\n" + name + "\n\nThe house came down over " + name + ", and the tarn gave " + name + " back.",
            "I have written the name here all the same. A house does not give back what it has once taken in. It only lends."));
    }
    /**
     * 0.4.185, what fleeing takes: the reader saw the house go into the tarn, so its door no longer comes to them, as the
     * cabin closes a room. Finding her in the wing takes nothing; the open lid already cost the storm night.
     */
    static void afterAccount(ServerPlayer p, CompoundTag own) {
        if (!"fled_as_the_house_fell".equals(own.getString("Outcome")) || own.getBoolean(LiteraryVignettes.GONE)) return;
        own.putBoolean(LiteraryVignettes.GONE, true);
        LiteraryVignettes.save(LabyrinthData.get(p.server), p.getUUID(), PLACE, own);
        p.displayClientMessage(Component.literal("There is no door to that house any more.").withStyle(net.minecraft.ChatFormatting.GRAY, net.minecraft.ChatFormatting.ITALIC), false);
    }

    /** He sits facing the doors, and turns his head toward the nearest reader who still sees him. */
    private static void seat(ServerPlayer p, CompoundTag own, LiteraryActor roderick) {
        if (roderick.phase() != RODERICK) roderick.appearance(LiteraryActor.STRANGER, RODERICK);
        roderick.setNoGravity(true);
        roderick.setYRot(0); roderick.setYBodyRot(0);
        ServerPlayer near = null;
        for (var q : listeners(p, own)) if (near == null || q.distanceToSqr(roderick) < near.distanceToSqr(roderick)) near = q;
        if (near != null && night(p).getInt("Stage") < 3) {
            float toward = (float) (Mth.atan2(near.getZ() - roderick.getZ(), near.getX() - roderick.getX()) * Mth.RAD_TO_DEG) - 90;
            roderick.setYHeadRot(Mth.clamp(Mth.wrapDegrees(toward), -70, 70));
        } else roderick.setYHeadRot(0);
        var said = roderick.getPersistentData();
        if (said.contains("SayUntil0488") && p.serverLevel().getGameTime() > said.getLong("SayUntil0488")) { roderick.setCustomNameVisible(false); roderick.setCustomName(null); said.remove("SayUntil0488"); }
    }

    /** The storm night: the vapour's glow behind the windows, coming and going, the casements and the wind, the exhalation over the tarn. */
    private static void weather(ServerPlayer p, BlockPos b, CompoundTag own) {
        int t = own.getInt("Present");
        boolean on = own.getBoolean("Fell0507") || Math.sin(t * .021) + .45 * Math.sin(t * .057 + 1.3) > .15;
        if (on != own.getBoolean("GlowOn0507") || t % 100 == 0) { glow(p, b, on); own.putBoolean("GlowOn0507", on); }
        var r = p.getRandom();
        if (t % 60 == 0) {
            var window = UsherHouse.STUDIO_WINDOWS.get(r.nextInt(UsherHouse.STUDIO_WINDOWS.size()));
            LiteraryVignettes.heard(p, SoundEvents.WOODEN_TRAPDOOR_CLOSE, b.offset(window), .35F, .6F + r.nextFloat() * .2F);
        }
        if (t % 140 == 0) LiteraryVignettes.heard(p, SoundEvents.AMBIENT_CAVE.value(), b.offset(0, 6, -27), .5F, .55F);
        if (t % 10 == 0 && p.blockPosition().getZ() - b.getZ() > UsherHouse.FRONT)
            p.serverLevel().sendParticles(p, ParticleTypes.WHITE_ASH, false, b.getX() + .5, b.getY() + 1.5, b.getZ() - 8, 24, 9, 1.2, 3.5, .01);
    }

    /** Every window cell's glow (stage + 2) in this reader's view, or its real state again. */
    private static void glow(ServerPlayer p, BlockPos b, boolean on) {
        var l = p.serverLevel();
        for (var cell : UsherHouse.windowCells()) {
            var at = b.offset(cell);
            var s = l.getBlockState(at);
            if (!UsherBlock.is(s, UsherBlock.Kind.WINDOW)) continue;
            p.connection.send(on && s.getValue(UsherBlock.STAGE) < 2 ? new ClientboundBlockUpdatePacket(at, s.setValue(UsherBlock.STAGE, s.getValue(UsherBlock.STAGE) + 2)) : new ClientboundBlockUpdatePacket(l, at));
        }
    }

    // ------------------------------------------------------------------------------------------------ the reading, the door and the fall

    /** The romance's three passages, original to this room; each ends on the sound the vault answers with. */
    public static final String[] PASSAGES = {
        "The knight would wait no longer at the hermit's door. He raised his mace and struck, and the dry old planks split and cracked and tore, and the noise of the wood went echoing all through the forest.",
        "Inside he found no hermit, but a dragon on guard before a palace of gold, and over it a shield of brass. He struck the dragon down, and it died with a shriek so harsh and so long that he put his hands to his ears.",
        "He went up to the wall where the shield hung, and before he could reach it, it fell of itself at his feet, upon the silver floor, with a great and terrible ringing.",
    };
    private static final String[] AFTER = {"Go on.", "Read. Do not stop.", "Not hear it? I have heard it. Long, long, many days I have heard it."};

    /** A reader's hand on the strewn books in the studio. True when the click was the room's. */
    static boolean books(ServerPlayer p, BlockPos rel, CompoundTag own) {
        var world = LiteraryVignettes.shared(LabyrinthData.get(p.server), PLACE);
        if (!storm(p, own) || !world.getBoolean("LidClosed")) {
            p.displayClientMessage(Component.literal("Old romances, swollen with damp. He has been reading them again.").withStyle(net.minecraft.ChatFormatting.GRAY, net.minecraft.ChatFormatting.ITALIC), true);
            return true;
        }
        var n = night(p);
        if (own.contains("BuriedVisit0507") && own.getInt("BuriedVisit0507") == own.getInt("Visit")) {
            p.displayClientMessage(Component.literal("Not tonight. The house is still settling over you.").withStyle(net.minecraft.ChatFormatting.GRAY, net.minecraft.ChatFormatting.ITALIC), true);
            return true;
        }
        if (n.getInt("Stage") > 0 || own.getBoolean("Fell0507")) return true;
        int id = world.getInt("Nights0507") + 1;
        world.putInt("Nights0507", id);
        LiteraryVignettes.shared(LabyrinthData.get(p.server), PLACE, world);
        n = night(p);
        n.putInt("Id", id); n.putInt("Stage", 1); n.putLong("At", p.serverLevel().getGameTime()); n.putInt("Heard", 0);
        n.putUUID("Reader", p.getUUID()); n.putString("ReaderName", p.getGameProfile().getName());
        night(p, n);
        // Everyone in the house who still sees him is in it from the first line.
        each(p, own, listeners(p, own), (q, theirs) -> theirs.putInt("NightId0507", id));
        passage(p, own, 0);
        return true;
    }
    private static void passage(ServerPlayer p, CompoundTag mine, int i) {
        var n = night(p);
        String reader = n.getString("ReaderName");
        for (var q : readers(p, mine, n.getInt("Id"))) {
            boolean reading = n.hasUUID("Reader") && n.getUUID("Reader").equals(q.getUUID());
            q.displayClientMessage(Component.literal((reading ? "You read: “" : reader + " reads: “") + PASSAGES[i] + "”").withStyle(net.minecraft.ChatFormatting.GRAY, net.minecraft.ChatFormatting.ITALIC), false);
        }
    }

    /** The reading, the answers from below, her coming, and the fall: once a tick, for everyone in it. */
    private static void reading(ServerPlayer p, BlockPos b, CompoundTag own, @Nullable LiteraryActor roderick) {
        var n = night(p);
        int stage = n.getInt("Stage"), id = n.getInt("Id");
        if (stage == 0) return;
        long since = p.serverLevel().getGameTime() - n.getLong("At");
        var below = b.offset(LiteraryRooms.COFFIN);
        var them = readers(p, own, id);
        if (stage <= 3) {
            if (since >= ANSWER && n.getInt("Heard") < stage) {
                n.putInt("Heard", stage);
                night(p, n);
                for (var q : them) switch (stage) {
                    case 1 -> LiteraryVignettes.heard(q, SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, below, .55F, .7F);
                    case 2 -> LiteraryVignettes.heard(q, SoundEvents.GRINDSTONE_USE, below, .9F, .5F);
                    default -> LiteraryVignettes.heard(q, SoundEvents.ANVIL_LAND, below, .6F, .55F);
                }
                if (roderick != null) say(p, own, roderick, AFTER[stage - 1]);
            }
            if (since >= PASSAGE) {
                n = night(p);
                if (stage < 3) { n.putInt("Stage", stage + 1); n.putLong("At", p.serverLevel().getGameTime()); night(p, n); passage(p, own, stage); }
                else {
                    n.putInt("Stage", AT_DOOR); n.putLong("At", p.serverLevel().getGameTime()); night(p, n);
                    if (roderick != null) say(p, own, roderick, "We have put her living in the tomb. I tell you she is standing outside the door.");
                    var her = LiteraryVignettes.actor(p, PLACE, MADELINE, LiteraryActor.COFFIN_WOMAN, UsherHouse.STUDIO_DOOR, false);
                    if (her != null) {
                        var at = threshold(b);
                        her.getPersistentData().putInt(NIGHT_TAG, id);
                        her.appearance(LiteraryActor.COFFIN_WOMAN, 2); her.setNoGravity(true); her.moveTo(at.x, at.y, at.z, 180, 0); her.setYHeadRot(180);
                    }
                    for (var q : them) { doors(q, b); LiteraryVignettes.heard(q, SoundEvents.WOODEN_DOOR_OPEN, b.offset(UsherHouse.STUDIO_DOOR), 1F, .6F); }
                }
            }
            return;
        }
        // She stands on the threshold, reeling, then goes to him; the two go down together.
        var her = LiteraryVignettes.actor(p, PLACE, MADELINE, LiteraryActor.COFFIN_WOMAN, UsherHouse.STUDIO_DOOR, false);
        if (her == null) return;
        if (!her.getPersistentData().contains(NIGHT_TAG)) her.getPersistentData().putInt(NIGHT_TAG, id);
        var to = Vec3.atBottomCenterOf(b.offset(UsherHouse.CHAIR)).add(0, 0, 1.2);
        if (since >= THRESHOLD) {
            var step = to.subtract(her.position());
            if (step.horizontalDistance() > .5) {
                var move = step.normalize().scale(Math.min(.45, step.horizontalDistance()));
                her.moveTo(her.getX() + move.x, her.getY(), her.getZ() + move.z, (float) (Mth.atan2(-move.x, move.z) * Mth.RAD_TO_DEG), 0);
                her.setYHeadRot(her.getYRot());
            } else fall(p, b, own, them, her);
        } else {
            var at = threshold(b);
            her.moveTo(at.x + Math.sin(since * .4) * .12, at.y, at.z, 180, 0);
        }
    }
    private static Vec3 threshold(BlockPos b) { return new Vec3(b.getX() + UsherHouse.STUDIO_DOOR.getX() + 1, b.getY(), b.getZ() + UsherHouse.STUDIO_DOOR.getZ() + .5); }

    private static void fall(ServerPlayer p, BlockPos b, CompoundTag own, List<ServerPlayer> them, LiteraryActor her) {
        var n = night(p);
        String reader = n.getString("ReaderName");
        var readerId = n.hasUUID("Reader") ? n.getUUID("Reader") : null;
        var names = them.stream().map(q -> q.getGameProfile().getName()).toList();
        each(p, own, them, (q, theirs) -> {
            theirs.putBoolean("Fell0507", true);
            theirs.putLong("FellAt0507", q.serverLevel().getGameTime());
            // The night is a beat of the story: the next arrival is a new visit.
            theirs.putInt("Beat", theirs.getInt("Visit"));
            theirs.putBoolean("ReadAloud0507", q.getUUID().equals(readerId));
            theirs.putString("Reader0507", reader);
            var others = new ArrayList<>(names);
            others.remove(q.getGameProfile().getName());
            theirs.putString("With0507", CoopAccounts.names(others));
            theirs.putString("Home0507", HouseRead.category(q, HouseRead.Trait.HOME).map(HouseRead.Category::value).orElse(""));
        });
        for (var q : them) {
            LiteraryVignettes.heard(q, SoundEvents.PLAYER_BIG_FALL, b.offset(UsherHouse.CHAIR), 1F, .6F);
            LiteraryVignettes.heard(q, SoundEvents.DEEPSLATE_BREAK, b.offset(0, 8, UsherHouse.FRONT), 1F, .4F);
            fissure(q, b);
            glow(q, b, true);
            q.displayClientMessage(Component.literal("The house groans. Get out.").withStyle(net.minecraft.ChatFormatting.GRAY, net.minecraft.ChatFormatting.ITALIC), true);
        }
        her.discard();
        forget(p, MADELINE);
        // The night is over; the house, and Roderick, stand for whoever comes after.
        var next = new CompoundTag();
        next.putLong("Seen", n.getLong("Seen")); next.putLong("Tick", n.getLong("Tick"));
        if (n.contains("Up")) next.putLong("Up", n.getLong("Up"));
        night(p, next);
    }

    /** On an open lid: once she has been seen standing in the wing, she comes to the studio doors when nobody is looking, and stays there. */
    private static void door(ServerPlayer p, BlockPos b, CompoundTag own, @Nullable LiteraryActor roderick) {
        var d = LabyrinthData.get(p.server);
        var world = LiteraryVignettes.shared(d, PLACE);
        if (!world.getBoolean("WomanLeft") || world.getBoolean("AtDoor0507") || !world.hasUUID("Woman")) return;
        boolean seen = false;
        for (var q : present(p)) { var theirs = own(p, own, q); if ("found_her_outside_the_open_coffin".equals(theirs.getString("Outcome"))) seen = true; }
        if (!seen || !(p.serverLevel().getEntity(world.getUUID("Woman")) instanceof LiteraryActor woman)) return;
        var at = threshold(b);
        if (LiteraryVignettes.watched(p.serverLevel(), woman.position().add(0, 1, 0)) || LiteraryVignettes.watched(p.serverLevel(), at.add(0, 1, 0))) return;
        woman.moveTo(at.x, at.y, at.z, 180, 0);
        woman.setYHeadRot(180);
        world = LiteraryVignettes.shared(d, PLACE);
        world.putBoolean("AtDoor0507", true);
        LiteraryVignettes.shared(d, PLACE, world);
        if (roderick != null) say(p, own, roderick, "She is at the door. She was never in the coffin long enough to die. I knew it, and I let the house be quiet.");
    }

    /** The studio doors stand open in this reader's view. */
    private static void doors(ServerPlayer p, BlockPos b) {
        var l = p.serverLevel();
        for (int i = 0; i < 2; i++) for (int y = 0; y < 2; y++) {
            var at = b.offset(UsherHouse.STUDIO_DOOR.getX() + i, y, UsherHouse.STUDIO_DOOR.getZ());
            var s = l.getBlockState(at);
            if (s.getBlock() instanceof DoorBlock && !s.getValue(DoorBlock.OPEN)) p.connection.send(new ClientboundBlockUpdatePacket(at, s.setValue(DoorBlock.OPEN, true)));
        }
    }

    /** The fissure opened wide, the moon through it, and the stone either side of it split, in this reader's view. */
    public static List<BlockPos> opened() {
        var out = new ArrayList<BlockPos>();
        for (var cell : UsherHouse.fissure()) for (int dx = -1; dx <= 1; dx++) {
            var at = new BlockPos(cell.x() + dx, cell.y(), UsherHouse.FRONT);
            if (cell.y() >= 0 && !out.contains(at)) out.add(at);
        }
        return out;
    }
    private static void fissure(ServerPlayer p, BlockPos b) {
        var l = p.serverLevel();
        for (var cell : opened()) {
            var at = b.offset(cell);
            var s = l.getBlockState(at);
            if (UsherBlock.is(s, UsherBlock.Kind.STONE) || UsherBlock.is(s, UsherBlock.Kind.FISSURE) || UsherBlock.is(s, UsherBlock.Kind.FISSURE_B))
                p.connection.send(new ClientboundBlockUpdatePacket(at, UsherBlock.of(UsherBlock.Kind.FISSURE_B, s.getValue(UsherBlock.FACING), UsherBlock.MOON)));
        }
    }

    /** He says it aloud: over his head, and in the chat of everyone in the house who still sees him. */
    private static void say(ServerPlayer p, CompoundTag mine, LiteraryActor a, String line) {
        a.say(line);
        a.getPersistentData().putLong("SayUntil0488", p.serverLevel().getGameTime() + Math.max(80, line.length() * 2 + 20));
        var text = Component.literal(NAME + ": “" + line + "”");
        for (var q : listeners(p, mine)) q.displayClientMessage(text, false);
    }

    /** A shared actor's record is dropped with it, so a later night can bring it back. */
    private static void forget(ServerPlayer p, String key) {
        var d = LabyrinthData.get(p.server);
        var s = LiteraryVignettes.shared(d, PLACE);
        if (s.hasUUID(key)) {
            if (p.serverLevel().getEntity(s.getUUID(key)) instanceof LiteraryActor a && !a.isRemoved()) a.discard();
            s.remove(key);
            LiteraryVignettes.shared(d, PLACE, s);
        }
    }

    // ------------------------------------------------------------------------------------------------ what he says

    private static final String[] FIRST = {
        "Forgive the dark. Light hurts me now. So does most music. Only the strings are bearable.",
        "Look at the tarn. It holds the house upside down and never lets the picture go.",
        "The stones know us. I have come to believe the house has made us what we are.",
        "My sister is gone. We will keep her below a fortnight, in the vault. I will not have the doctors at her.",
    };
    private static final String[] AFTER_LID = {
        "Then it is done. Do not tell me how you left her. I will hear it, either way.",
        "Sit with me a while. I cannot bear the room empty.",
    };
    private static final String[] STORM_CLOSED = {
        "You hear it too. You have heard it for days and said nothing.",
        "Read to me. Anything. The old romance, there on the floor by my chair. Read it loud.",
        "Do not open the window. The light is not from the sky. It is coming off the house.",
    };
    private static final String[] STORM_OPEN = {
        "You left it open. She has been walking all night. I hear her on the boards of the wing.",
        "She does not come in here. She stands where there used to be a chair and waits.",
        "Do not open the window. The light is not from the sky. It is coming off the house.",
    };
    /** 0.4.183: what he sees of the house the reader came from, by the House's read of them (HOME), once known. */
    public static String homeLine(String home) {
        return switch (home) {
            case "HAVEN" -> "You were kept safe somewhere. I can see it on you. I envy you that more than I can say.";
            case "DANGER" -> "You know what it is to be afraid of the house you grew up in. I saw it when you came under the arch.";
            case "RUNAWAY" -> "You have left a house before. Then you know the causeway is longer than it looks.";
            default -> "";
        };
    }
    /** His greeting, by how the House has heard the reader spoken of; the plain one otherwise. */
    public static String greeting(ServerPlayer p) {
        return HouseRead.address(p, "You came. I wrote because I could not be alone in it any longer.",
            "You came, old fellow. I wrote because I could not be alone in it any longer.",
            "You came, my dear. I wrote because I could not be alone in it any longer.",
            "You came, old friend. I wrote because I could not be alone in it any longer.");
    }

    /** A word from him when spoken to; the others in the room hear it too. */
    static void talk(ServerPlayer p, LiteraryActor a, CompoundTag own) {
        if (a.role() != LiteraryActor.STRANGER || a.phase() != RODERICK || gone(own)) return;
        var world = LiteraryVignettes.shared(LabyrinthData.get(p.server), PLACE);
        int k = own.getInt("Talk0507");
        own.putInt("Talk0507", k + 1);
        if (night(p).getInt("Stage") > 0) { say(p, own, a, "Do not stop. Read."); return; }
        if (storm(p, own)) { var lines = world.getBoolean("LidClosed") ? STORM_CLOSED : STORM_OPEN; say(p, own, a, lines[k % lines.length]); return; }
        if (world.contains("LidClosed")) { say(p, own, a, AFTER_LID[k % AFTER_LID.length]); return; }
        var lines = new ArrayList<String>();
        lines.add(greeting(p));
        String home = homeLine(HouseRead.category(p, HouseRead.Trait.HOME).map(HouseRead.Category::value).orElse(""));
        if (!home.isEmpty()) lines.add(home);
        lines.addAll(List.of(FIRST));
        say(p, own, a, lines.get(k % lines.size()));
    }

    // ------------------------------------------------------------------------------------------------ the account

    /** The account of the night the house fell, as this reader was in it. */
    public static String account(CompoundTag own) {
        var out = new StringBuilder();
        // 0.4.184: a reader the house once closed over says so first.
        if (own.getBoolean("BuriedOnce0507")) out.append(own.getBoolean("TarnKeptOnce0507") ? "The first time the house fell, I was still inside it. The tarn gave me back, and kept what I had in my hands until I got out. " : "The first time the house fell, I was still inside it. The tarn gave me back. ");
        out.append(own.getBoolean("LidChosen") && own.getBoolean("LidClosed") ? "I closed the lid and came back on the night of the storm. " : "The lid had been closed. I came back on the night of the storm. ");
        String reader = own.getString("Reader0507");
        out.append(own.getBoolean("ReadAloud0507") || reader.isEmpty() ? "He asked me to read to him, and I read the old romance aloud. " : reader + " read the old romance aloud to him, because he asked. ");
        out.append("Each thing that broke in the story broke below us: wood, then a long grating cry, then iron. He had heard her for days and said nothing. When the doors gave, she was standing there, and she went to him, and they went down together. ");
        String with = own.getString("With0507");
        if (!with.isEmpty()) out.append(with).append(" ran with me. ");
        out.append("I did not stop until I was over the causeway. When I looked back, the crack ran from the roof into the water, and light came through it.");
        String closing = switch (own.getString("Home0507")) {
            case "HAVEN" -> " All the way across I thought of a house that had never asked anything of me.";
            case "DANGER" -> " It was not the first house I had watched close over the people in it.";
            case "RUNAWAY" -> " I had run from a house before. I knew not to look back until I reached the bank.";
            default -> "";
        };
        return out.append(closing).toString();
    }
}
