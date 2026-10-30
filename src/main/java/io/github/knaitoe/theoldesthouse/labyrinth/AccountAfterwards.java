package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseRemembers;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;

/**
 * What happened afterwards, in the reader's own account (0.4.194). The owner: "Ensure you're updating literature too.
 * Accounts, house or leaves etc." Each line is written from what the reader's records actually hold, never from what might
 * have happened: the trades a room took or gave (0.4.186–0.4.191), what followed them home, and what the manor kept of a room
 * for them (0.4.190). The Witness account adds these after a story's own line; the rooms' own accounts use the same words.
 * Lines are first person and past tense, and, as all House writing, never name the reader's sex, a colour or an age.
 */
public final class AccountAfterwards {
    private AccountAfterwards() {}

    static String times(int n) { return n == 1 ? "once" : n == 2 ? "twice" : n == 3 ? "three times" : n + " times"; }

    /** The sentence or two after a story's line in the Witness account, with a leading space, or nothing. */
    public static String coda(LabyrinthData d, UUID id, WitnessAccount.Story story) {
        String line = switch (story) {
            case HARRIGAN -> harrigan(HarriganVignette.playerState(d, id));
            case TED_CAVER -> caver(d.stateEntry(ContestTrades.STATE, id.toString()));
            case FLOORBOARDS -> floorboards(d.stateEntry(ContestTrades.STATE, id.toString()));
            case ELK_LOT -> elk(LiteraryVignettes.personal(d, id, LabyrinthPlace.ELK_LOT));
            case BLY_ROUTE -> bly(LiteraryVignettes.personal(d, id, LabyrinthPlace.BLY_ROUTE));
            case ELK_CARCASSES -> hides(d.stateEntry(ContestTrades.STATE, id.toString()));
            case SECOND_WAITRESS -> plate(LiteraryVignettes.personal(d, id, LabyrinthPlace.SECOND_WAITRESS));
            default -> "";
        };
        String manor = manor(d.stateEntry(HouseRemembers.STATE, id.toString()), story);
        String all = (line + " " + manor).trim();
        return all.isEmpty() ? "" : " " + all;
    }

    /** Kept, he is freed; buried, the rot goes as far as the names he has carried. */
    public static String harrigan(CompoundTag s) {
        if (s.getBoolean("KeptHarriganPhone") && !s.getBoolean("Contact"))
            return s.getInt("KindSent0188") > 0 ? "His number still sends me a line now and then, though his phone is in my pocket. He has never asked me for a name." : "";
        if (!s.getBoolean("Contact")) return "";
        int names = s.getInt("TotalCalls");
        if (names <= 0) return "";
        if (names < 3) return "Every name I gave him afterwards slowed his greeting a little.";
        if (names < HarriganPhones.RINGS_AFTER + 2) return "By the third name his replies came with letters missing, and the phone smelled of the casket.";
        return "Later he stopped waiting for me to call. Some nights the phone rings, and rings, and goes to a missed call.";
    }
    /** The cave's breath, and the hand it kept on the line. */
    public static String caver(CompoundTag trades) {
        int n = trades.getInt("Unwound");
        String breath = "Under water I can hold my breath longer than I used to.";
        if (n <= 0) return breath;
        return breath + " Since then the cave has pulled my line back through my fingers " + times(n) + "." + (n >= ContestTrades.UNWINDS ? " Then it let go." : "");
    }
    public static String floorboards(CompoundTag trades) {
        int n = trades.getInt("Unease");
        return n <= 0 ? "" : "Before that, it had sent me out " + times(n) + ", and each time the heart was louder when I came back.";
    }
    /** Struck once, she followed; the last night they kept their hands down. */
    public static String elk(CompoundTag own) {
        if (!own.contains(ElkFollows.STRUCK_VISIT)) return "";
        return own.getBoolean(ElkFollows.FOLLOWS) ? "Once I raised a hand to her, and she followed me home. She still does."
                : "Once I raised a hand to her, and she followed me home. On the last night I kept my hands at my sides, and she let me pass.";
    }
    public static String bly(CompoundTag own) {
        int n = own.getInt(BlyFollows.CARRIED);
        if (n <= 0) return "";
        return own.getBoolean(BlyFollows.FOLLOWS) ? "Near water at home she has walked me to the edge " + times(n) + "."
                : "Near water at home she walked me to the edge " + times(n) + ", until I waited out a night in the cupboard.";
    }
    public static String hides(CompoundTag trades) {
        if (trades.getBoolean("HideWashed")) return "The smell of the hides came out with me, until water took it off.";
        return trades.getBoolean("HideSmell") ? "The smell of the hides came out with me. Animals turn away from it." : "";
    }
    public static String plate(CompoundTag own) {
        return own.getBoolean("MealEaten127") ? "What was on the plate turned over in me on the way out." : "";
    }

    /** What the manor kept of the room for this reader, once it began keeping it. */
    public static String manor(CompoundTag remembers, WitnessAccount.Story story) {
        if (!remembers.contains("Since_" + story.id)) return "";
        return switch (story) {
            case WALLPAPER -> "Afterwards the pattern came to the manor, a strip of the great room's west wall at a time. Something moves in it when I look away.";
            case MODEL_HOME -> "Afterwards a tree stood outside the manor's front window. From outside, there is no tree.";
            case WINCHESTER -> "Afterwards a door stood against the study wall in the manor. It opens onto plaster.";
            case CHILD_ROOM -> "Afterwards the bear from the ceiling sat on the manor's stairs, one step higher every day.";
            case PRESERVED_CAVE -> "Afterwards, now and then, a pew stands in the manor's hall, turned to face the way I came in.";
            case MINIATURES -> "Afterwards a small figure of me stands in the manor, where I last stood the day before.";
            case HILL_NURSERY -> "Afterwards the manor's walls knocked too." + (remembers.getInt("HillAnswered") > 0 ? " When I knocked back the same number, they answered." : "");
            case BACKSEAT -> remembers.getInt("Seats") > 0 ? "Afterwards, at a manor window at night, I saw it in the glass, sitting behind my shoulder." : "";
            default -> "";
        };
    }
}
