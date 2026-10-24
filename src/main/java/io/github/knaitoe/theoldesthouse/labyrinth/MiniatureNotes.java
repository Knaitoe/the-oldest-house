package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * The workshop's models answer a touch (0.4.121). Each model is a room the reader walked, and its card says, in the third
 * person and only from what the House recorded, what the reader did there: how often they came, how long they stayed, what
 * they read and how it ended. A model of a room still ahead says only that they have not been there yet. The mother in the
 * upper corner is the client's; the server only says where the corners are and when she may come.
 */
public final class MiniatureNotes {
    private MiniatureNotes() {}

    /** The three model stands, left to right; the block STAGE matches the index. */
    static final BlockPos[] STANDS = {new BlockPos(-8, 1, -18), new BlockPos(0, 1, -18), new BlockPos(8, 1, -18)};
    /** The room's four upper corners, a block under the ceiling. */
    static final BlockPos[] CORNERS = {new BlockPos(-13, 6, -2), new BlockPos(13, 6, -2), new BlockPos(-13, 6, -29), new BlockPos(13, 6, -29)};
    /** Twenty seconds in the room before she can be at the edge of sight. */
    public static final int MOTHER_AFTER = 400;

    public static boolean click(ServerPlayer p, BlockPos b, BlockPos rel, CompoundTag own) {
        int index = -1;
        for (int i = 0; i < STANDS.length; i++) if (STANDS[i].equals(rel)) index = i;
        if (index < 0) return false;
        var models = own.getList("Models", Tag.TAG_COMPOUND);
        if (index >= models.size()) {
            p.displayClientMessage(Component.literal("The board is bare. Nothing has been built on it yet."), true);
            return true;
        }
        var model = models.getCompound(index);
        var place = LabyrinthPlace.byId(model.getString("Room"));
        String card = place == null ? "The card under it is blank." : card(p, place, model.getBoolean("Preview"), model.getBoolean("Behind"));
        p.displayClientMessage(Component.literal(card).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), false);
        own.putInt("CardsRead", own.getInt("CardsRead") | (1 << index));
        return true;
    }

    /** Where the client may put her, once the reader has been in the room a while; null until then. */
    public static @Nullable CompoundTag mother(ServerPlayer p, BlockPos b, CompoundTag own) {
        if (own.getInt("ThisVisitTicks") < MOTHER_AFTER) return null;
        var corners = new long[CORNERS.length];
        for (int i = 0; i < CORNERS.length; i++) corners[i] = b.offset(CORNERS[i]).asLong();
        var mother = new CompoundTag();
        mother.putLongArray("Corners", corners);
        mother.putUUID("Reader", p.getUUID());
        var t = new CompoundTag();
        t.put("MiniatureMother", mother);
        return t;
    }

    /** The card for one model: the reader's name and the room, then what they did there, each fact as a short clause. */
    public static String card(ServerPlayer p, LabyrinthPlace place, boolean preview, boolean behind) {
        String name = p.getGameProfile().getName();
        var d = LabyrinthData.get(p.server);
        var lines = new ArrayList<String>();
        lines.add(name + ", in " + room(place) + ".");
        if (preview || !d.visited(p.getUUID()).contains(place.id())) {
            lines.add("Has not been here.");
            lines.add("The figure is already standing where it will stand.");
            return String.join(" ", lines);
        }
        lines.addAll(facts(p, d, place));
        if (behind) lines.add("No one came in with " + name + ".");
        return String.join(" ", lines);
    }

    static List<String> facts(ServerPlayer p, LabyrinthData d, LabyrinthPlace place) {
        var out = new ArrayList<String>();
        int age = d.recentVisit(p.getUUID(), place);
        String when = age == 1 ? "The room just before this one." : age > 1 ? number(age) + " rooms back." : "Some time ago, by the House's count.";
        var story = WitnessAccount.Story.of(place.id());
        String resolved = story == null ? "" : WitnessAccount.record(d, p.getUUID()).getCompound("Stories").getString(story.id);
        if (LiteraryRooms.isLiterary(place)) {
            var own = LiteraryVignettes.personal(d, p.getUUID(), place);
            int visits = Math.max(1, own.getInt("Visit"));
            String came = visits == 1 ? "Came in once" : "Came in " + times(visits);
            int minutes = Math.round(own.getInt("Present") / 1200F);
            String stay = minutes <= 0 ? "stayed under a minute" : minutes == 1 ? "stayed about a minute" : "stayed about " + number(minutes) + " minutes";
            out.add(came + (visits == 1 ? "; " : "; the last time, ") + stay + ".");
            out.add(when);
            if (own.getBoolean("Read_Source")) out.add("Read what had been left out to read.");
            else out.add("Did not read what had been left out.");
            String outcome = own.getString("Outcome");
            if (own.getBoolean("UncannyFailed") && !outcome.isEmpty()) out.add(capital(phrase(outcome)) + ". Did not get out the way it was meant.");
            else if (own.getBoolean("Completed")) { if (!outcome.isEmpty()) out.add(capital(phrase(outcome)) + "."); out.add("Read the last account before leaving."); }
            else if (own.getBoolean("Ready")) { if (!outcome.isEmpty()) out.add(capital(phrase(outcome)) + "."); out.add("Left the last account where it lay."); }
            else out.add("Left before it was finished.");
        } else {
            out.add(when);
            if (!resolved.isEmpty()) out.add(capital(phrase(resolved)) + ".");
            else if (story != null) out.add("Left before it was finished.");
            else out.add("Walked through and on.");
        }
        return out;
    }

    static String room(LabyrinthPlace place) {
        String name = UncannyRooms.isUncanny(place) ? UncannyTexts.title(place) : StaircaseProse.place(place.id());
        return name.startsWith("a room the House") ? "a room with no name" : name;
    }

    /** A recorded outcome in the reader's third person, with no pronoun. Unlisted outcomes read as their own words. */
    static String phrase(String outcome) {
        String known = PHRASES.get(outcome);
        if (known != null) return known;
        return outcome.replaceAll("_?[0-9]+$", "").replace("_own_", "_").replace('_', ' ').trim();
    }

    private static final Map<String, String> PHRASES = Map.ofEntries(
        Map.entry("examined_three_personal_model_arrangements", "bent over three of these models in turn"),
        Map.entry("heard_each_cellar_knock_return", "knocked, and heard every knock come back from the cellar"),
        Map.entry("left_after_finding_mothers_room", "found Mother's room, and was let go"),
        Map.entry("reached_the_upper_construction_ledger", "climbed to the ledger at the top of the unfinished stairs"),
        Map.entry("stopped_the_seventh_room_clock", "stopped the clock in the seventh room"),
        Map.entry("left_the_mark_and_read_own_confession_transcript", "left the mark and read the transcript of the confession"),
        Map.entry("read_the_sealed_journal_signed_with_own_name", "opened a sealed journal and found the name in it"),
        Map.entry("completed_the_filming_days_and_read_own_cut", "sat through every filming day and watched the cut"),
        Map.entry("escaped_the_looping_field_and_read_own_clipping", "got out of the field that kept coming round, and read the clipping"),
        Map.entry("escaped_past_the_last_cabins", "ran past the last cabins and out"),
        Map.entry("reached_living_grass_beyond_the_costumes", "reached the grass beyond the costumes"),
        Map.entry("rowed_to_the_far_pier_between_firework_exposures", "rowed for the far pier between the fireworks"),
        Map.entry("broke_ice_waited_beneath_it_and_reached_the_bank", "went through the ice, waited under it, and came up at the bank"),
        Map.entry("waited_in_a_shut_closet_until_the_lake", "waited in a shut closet until the lake came"),
        Map.entry("held_still_beneath_the_hides_and_left_by_service_path", "lay still under the hides, then went out by the service path"),
        Map.entry("held_the_fan_platform_and_was_thrown_out", "held on to the fan's platform until thrown clear"),
        Map.entry("gave_two_hearts_and_an_arm", "gave two hearts and an arm"),
        Map.entry("made_the_last_wish_before_the_door", "made the last wish before anyone opened the door"),
        Map.entry("opened_the_door_onto_the_empty_road", "opened the door onto an empty road"),
        Map.entry("gave_the_paw_back_to_the_fire", "gave the paw back to the fire"),
        Map.entry("heard_all_three_cylinders_and_exposed_red_floor", "played all three cylinders and uncovered the red floor"),
        Map.entry("heard_how_he_came_to_sit_here", "listened to how he came to sit there"),
        Map.entry("ate_and_followed_the_fathers_drag_trail", "ate, then followed the trail where the father had been dragged"),
        Map.entry("broke_the_fathers_things_and_returned_to_the_brother", "broke the father's things and went back to the brother"),
        Map.entry("kept_the_fathers_things_and_saw_the_brother_fade", "kept the father's things and watched the brother fade"),
        Map.entry("refused_the_familys_request_to_leave", "would not leave when the family asked"),
        Map.entry("left_when_the_family_asked", "left when the family asked"),
        Map.entry("drove_the_family_out", "drove the family out"),
        Map.entry("examined_the_empty_sitting", "looked over the empty sitting"),
        Map.entry("heard_the_medium", "heard the medium out"),
        Map.entry("found_her_outside_the_open_coffin", "found her outside the open coffin"),
        Map.entry("returned_to_the_closed_lid", "came back to the closed lid"),
        Map.entry("kept_the_hidden_pages", "kept the hidden pages"),
        Map.entry("read_all_eight_original_pages", "read all eight pages"),
        Map.entry("read_the_final_page", "read the last page"),
        Map.entry("left_by_the_unpainted_door", "left by the unpainted door"),
        Map.entry("left_the_second_waitress_meal", "left the meal she brought where it was"),
        Map.entry("let_her_clear_the_table", "let her clear the table"),
        Map.entry("ate_what_she_brought", "ate what she brought"),
        Map.entry("passed_the_last_green_signal", "walked on the green to the last signal"),
        Map.entry("paced_to_the_last_red_light", "kept walking on the red"),
        Map.entry("pressed_the_buzzer_release", "pressed the buzzer to let it in"),
        Map.entry("waited_out_the_empty_bed", "watched the empty bed until morning"),
        Map.entry("watched_the_hand_reach_the_door", "watched the hand reach the door"),
        Map.entry("knitted_into_the_mass", "stood too long and was taken into the tallow"),
        Map.entry("backed_into_the_corners", "backed into the corners"),
        Map.entry("refused_and_examined_the_closed_rooms_screen", "refused, and looked at the screen of closed rooms"),
        Map.entry("closed_the_door_on_the_other_feed", "closed the door on the other feed"),
        Map.entry("closed_the_cellar_after_the_blade_broke", "closed the cellar after the blade broke"),
        Map.entry("brought_the_cellar_key_up", "brought the cellar key up"),
        Map.entry("opened_the_hinged_service_door", "opened the hinged service door"),
        Map.entry("returned_to_the_deer_outline", "went back to the outline of the deer"),
        Map.entry("returned_to_sender", "sent the letter back"),
        Map.entry("held_the_distant_frame", "held the far figure in the frame"),
        Map.entry("silenced_the_mountain", "silenced the mountain"),
        Map.entry("retraced_the_squeeze", "crawled back out through the squeeze"),
        Map.entry("waited_and_climbed_out", "waited, then climbed out"),
        Map.entry("three_rooms_and_service_latch", "went through three rooms and pulled the service latch"),
        Map.entry("gave_an_original_to_permanent_sealed_custody", "gave something to be kept sealed for good"));

    static String capital(String s) { return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1); }
    static String number(int n) {
        String[] words = {"no", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "eleven", "twelve"};
        return n >= 0 && n < words.length ? words[n] : Integer.toString(n);
    }
    static String times(int n) { return n == 2 ? "twice" : n == 3 ? "three times" : number(n) + " times"; }
}
