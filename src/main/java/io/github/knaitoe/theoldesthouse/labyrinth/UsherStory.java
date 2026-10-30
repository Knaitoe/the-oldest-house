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
 * Usher's people and its last night (0.4.182), each reader's own.
 * <p>Roderick sits in his chair in the studio, a private actor on stranger phase {@link #RODERICK}, and talks when spoken to.
 * After the lid is chosen, every later visit is the storm night: the vapour about the house glows behind the windows
 * (their glow stages, shown to that reader alone) and the casements rattle. On a closed lid he asks to be read to; the
 * strewn books open the old romance, three passages, and each thing that breaks in it is answered from the vault below.
 * Then she stands at the studio doors, goes to him, and the two of them go down together. The fissure opens on the
 * front with the moon's light through it, and the reader who gets back over the causeway to the bank has fled as the house
 * fell. On an open lid the storm comes too, and Roderick only listens to her walking in the wing.
 * <p>Nothing here changes a block for anyone: the glow, the open doors and the opened fissure are views sent to the one
 * reader, and Roderick and the Madeline at the door are owner-bound actors.
 */
public final class UsherStory {
    /** Roderick's stranger phase. */
    public static final int RODERICK = 38;
    /** How long each passage of the romance is read before the vault answers it, and before the next is begun, in ticks. */
    public static final int ANSWER = 60, PASSAGE = 110;
    /** How long she stands on the threshold before she goes to him. */
    public static final int THRESHOLD = 40;
    /** The reading's stages, kept as Trist0507. */
    public static final int AT_DOOR = 4, FALLEN = 5;
    private static final LabyrinthPlace PLACE = LabyrinthPlace.USHER;
    private static final String NAME = "Roderick";

    private UsherStory() {}

    /** The night after the lid was chosen, on any later visit. */
    public static boolean storm(CompoundTag own) { return own.getBoolean("LidChosen") && own.getInt("Visit") > own.getInt("ChoiceVisit"); }

    /** Each present reader's turn, every five ticks, from {@link LiteraryVignettes}. */
    static void tick(ServerPlayer p, BlockPos b, CompoundTag own, CompoundTag world) {
        int visit = own.getInt("Visit");
        if (own.getInt("TristVisit0507") != visit) {
            // A fresh arrival begins the night again; a reading left unfinished is forgotten with its Madeline.
            own.putInt("TristVisit0507", visit);
            if (own.getInt("Trist0507") < FALLEN) { own.putInt("Trist0507", 0); own.putInt("TristHeard0507", 0); forget(p, "Madeline"); }
            own.remove("GlowOn0507");
        }
        var roderick = own.getBoolean("Fell0507") ? null : LiteraryVignettes.actor(p, PLACE, NAME, LiteraryActor.STRANGER, UsherHouse.CHAIR, true);
        if (roderick != null) {
            if (roderick.phase() != RODERICK) roderick.appearance(LiteraryActor.STRANGER, RODERICK);
            roderick.setNoGravity(true);
            // He sits facing the doors, and turns his head toward the reader when there is nothing else to watch.
            roderick.setYRot(0); roderick.setYBodyRot(0);
            float toward = (float) (Mth.atan2(p.getZ() - roderick.getZ(), p.getX() - roderick.getX()) * Mth.RAD_TO_DEG) - 90;
            roderick.setYHeadRot(own.getInt("Trist0507") >= 3 ? 0 : Mth.clamp(Mth.wrapDegrees(toward), -70, 70));
            var said = roderick.getPersistentData();
            if (said.contains("SayUntil0488") && p.serverLevel().getGameTime() > said.getLong("SayUntil0488")) { roderick.setCustomNameVisible(false); roderick.setCustomName(null); said.remove("SayUntil0488"); }
        }
        if (!storm(own)) return;
        weather(p, b, own);
        if (world.getBoolean("LidClosed")) reading(p, b, own, roderick);
        if (own.getBoolean("Fell0507")) {
            // The opened fissure stays open in this reader's view, and getting back to the bank is the flight.
            if (own.getInt("Present") % 100 == 0) fissure(p, b);
            if (p.blockPosition().getZ() - b.getZ() >= -3 && p.getY() >= b.getY() - 1.5) LiteraryVignettes.ready(p, PLACE, own, "fled_as_the_house_fell");
        }
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
        if (!storm(own) || !world.getBoolean("LidClosed")) {
            p.displayClientMessage(Component.literal("Old romances, swollen with damp. He has been reading them again.").withStyle(net.minecraft.ChatFormatting.GRAY, net.minecraft.ChatFormatting.ITALIC), true);
            return true;
        }
        if (own.getInt("TristVisit0507") != own.getInt("Visit")) { own.putInt("TristVisit0507", own.getInt("Visit")); own.putInt("Trist0507", 0); own.putInt("TristHeard0507", 0); }
        if (own.getInt("Trist0507") > 0 || own.getBoolean("Fell0507")) return true;
        own.putInt("Trist0507", 1);
        own.putInt("TristAt0507", own.getInt("Present"));
        passage(p, 0);
        return true;
    }
    private static void passage(ServerPlayer p, int i) {
        p.displayClientMessage(Component.literal("You read: “" + PASSAGES[i] + "”").withStyle(net.minecraft.ChatFormatting.GRAY, net.minecraft.ChatFormatting.ITALIC), false);
    }

    /** The reading, the answers from below, her coming, and the fall. */
    private static void reading(ServerPlayer p, BlockPos b, CompoundTag own, @Nullable LiteraryActor roderick) {
        int stage = own.getInt("Trist0507");
        if (stage == 0 || stage >= FALLEN) return;
        int since = own.getInt("Present") - own.getInt("TristAt0507");
        var below = b.offset(LiteraryRooms.COFFIN);
        if (stage <= 3) {
            if (since >= ANSWER && own.getInt("TristHeard0507") < stage) {
                own.putInt("TristHeard0507", stage);
                switch (stage) {
                    case 1 -> LiteraryVignettes.heard(p, SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, below, .55F, .7F);
                    case 2 -> LiteraryVignettes.heard(p, SoundEvents.GRINDSTONE_USE, below, .9F, .5F);
                    default -> LiteraryVignettes.heard(p, SoundEvents.ANVIL_LAND, below, .6F, .55F);
                }
                if (roderick != null) say(p, roderick, AFTER[stage - 1]);
            }
            if (since >= PASSAGE) {
                if (stage < 3) { own.putInt("Trist0507", stage + 1); own.putInt("TristAt0507", own.getInt("Present")); passage(p, stage); }
                else {
                    if (roderick != null) say(p, roderick, "We have put her living in the tomb. I tell you she is standing outside the door.");
                    own.putInt("Trist0507", AT_DOOR); own.putInt("TristAt0507", own.getInt("Present"));
                    doors(p, b);
                    var her = LiteraryVignettes.actor(p, PLACE, "Madeline", LiteraryActor.COFFIN_WOMAN, UsherHouse.STUDIO_DOOR, true);
                    if (her != null) { var at = threshold(b); her.appearance(LiteraryActor.COFFIN_WOMAN, 2); her.setNoGravity(true); her.moveTo(at.x, at.y, at.z, 180, 0); her.setYHeadRot(180); }
                    LiteraryVignettes.heard(p, SoundEvents.WOODEN_DOOR_OPEN, b.offset(UsherHouse.STUDIO_DOOR), 1F, .6F);
                }
            }
            return;
        }
        // She stands on the threshold, reeling, then goes to him; the two go down together.
        doors(p, b);
        var her = LiteraryVignettes.actor(p, PLACE, "Madeline", LiteraryActor.COFFIN_WOMAN, UsherHouse.STUDIO_DOOR, true);
        if (her == null) return;
        var to = Vec3.atBottomCenterOf(b.offset(UsherHouse.CHAIR)).add(0, 0, 1.2);
        if (since >= THRESHOLD) {
            var step = to.subtract(her.position());
            if (step.horizontalDistance() > .5) {
                var move = step.normalize().scale(Math.min(.45, step.horizontalDistance()));
                her.moveTo(her.getX() + move.x, her.getY(), her.getZ() + move.z, (float) (Mth.atan2(-move.x, move.z) * Mth.RAD_TO_DEG), 0);
                her.setYHeadRot(her.getYRot());
            } else fall(p, b, own, roderick, her);
        } else {
            var at = threshold(b);
            her.moveTo(at.x + Math.sin(since * .4) * .12, at.y, at.z, 180, 0);
        }
    }
    private static Vec3 threshold(BlockPos b) { return new Vec3(b.getX() + UsherHouse.STUDIO_DOOR.getX() + 1, b.getY(), b.getZ() + UsherHouse.STUDIO_DOOR.getZ() + .5); }

    private static void fall(ServerPlayer p, BlockPos b, CompoundTag own, @Nullable LiteraryActor roderick, LiteraryActor her) {
        LiteraryVignettes.heard(p, SoundEvents.PLAYER_BIG_FALL, b.offset(UsherHouse.CHAIR), 1F, .6F);
        LiteraryVignettes.heard(p, SoundEvents.DEEPSLATE_BREAK, b.offset(0, 8, UsherHouse.FRONT), 1F, .4F);
        her.discard(); forget(p, "Madeline");
        if (roderick != null) { roderick.discard(); forget(p, NAME); }
        own.putInt("Trist0507", FALLEN);
        own.putBoolean("Fell0507", true);
        fissure(p, b);
        glow(p, b, true);
        own.putBoolean("GlowOn0507", true);
        p.displayClientMessage(Component.literal("The house groans. Get out.").withStyle(net.minecraft.ChatFormatting.GRAY, net.minecraft.ChatFormatting.ITALIC), true);
    }

    /** The studio doors stand open in this reader's view. */
    private static void doors(ServerPlayer p, BlockPos b) {
        var l = p.serverLevel();
        for (int i = 0; i < 2; i++) for (int y = 0; y < 2; y++) {
            var at = b.offset(UsherHouse.STUDIO_DOOR.getX() + i, y, UsherHouse.STUDIO_DOOR.getZ());
            var s = l.getBlockState(at);
            if (s.getBlock() instanceof DoorBlock) p.connection.send(new ClientboundBlockUpdatePacket(at, s.setValue(DoorBlock.OPEN, true)));
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

    private static void say(ServerPlayer p, LiteraryActor a, String line) {
        a.say(line);
        a.getPersistentData().putLong("SayUntil0488", p.serverLevel().getGameTime() + Math.max(80, line.length() * 2 + 20));
        p.displayClientMessage(Component.literal(NAME + ": “" + line + "”"), false);
    }

    /** A private actor's record is dropped with it, so a later night can bring it back. */
    private static void forget(ServerPlayer p, String key) {
        var d = LabyrinthData.get(p.server);
        var s = LiteraryVignettes.shared(d, PLACE);
        String id = key + "_" + p.getUUID();
        if (s.hasUUID(id)) {
            if (p.serverLevel().getEntity(s.getUUID(id)) instanceof LiteraryActor a && !a.isRemoved()) a.discard();
            s.remove(id);
            LiteraryVignettes.shared(d, PLACE, s);
        }
    }

    // ------------------------------------------------------------------------------------------------ what he says

    private static final String[] FIRST = {
        "You came. I wrote because I could not be alone in it any longer.",
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

    /** A word from him when spoken to. */
    static void talk(ServerPlayer p, LiteraryActor a, CompoundTag own) {
        if (a.role() != LiteraryActor.STRANGER || a.phase() != RODERICK) return;
        var world = LiteraryVignettes.shared(LabyrinthData.get(p.server), PLACE);
        int k = own.getInt("Talk0507");
        own.putInt("Talk0507", k + 1);
        String[] lines = !own.getBoolean("LidChosen") ? FIRST : !storm(own) ? AFTER_LID : world.getBoolean("LidClosed") ? STORM_CLOSED : STORM_OPEN;
        if (own.getInt("Trist0507") > 0) { say(p, a, "Do not stop. Read."); return; }
        say(p, a, lines[k % lines.length]);
    }
}
