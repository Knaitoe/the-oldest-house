package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import javax.annotation.Nullable;

/**
 * 0.4.401, the owner: "it's also feeding me yard maps when i'm not in the yard". The yard's places were dealt behind ordinary doors
 * of the halls by depth alone, so a storm field or a stock gate opened off a plaster corridor. The owner's draft ("The House Has a
 * Yard", 4 November) has the yard as one place, entered by the back door: "You open a back door onto wet grass. There's a bulb over
 * the porch, a turnaround beside the garage". Its parts make sense together and lead into each other.
 * <p>
 * So the yard is a stretch of the walk now, not a scatter of rooms. From the House's halls only a back door onto the yard is dealt:
 * the turn-around, or one of its once-only variants that start on the same porch (the long drive, the porch turn). From inside the
 * yard, its ways on lead further into the yard (its connectors, its puzzles and hazards at their own pacing, Truant's sheds, the
 * Sphinx's field), and its stories branch off it, for up to {@link #LONGEST} yard places in a row. After that, or when nothing more of
 * the yard is due, the way on leads back inside. Its four stories (The Supper, In the Tall Grass, Growing Things, The Last Crossing)
 * open only off the yard; a reader whose next story waits out there is dealt the back door instead.
 */
public final class YardStretch {
    private YardStretch(){}
    /** How many yard places in a row before the way on leads back inside. */
    public static final int LONGEST = 3;
    /** The back door's weight among the halls: the yard's whole share (the turn-around's own before the ditch and line split it). */
    public static final int BACK_DOOR = 6;
    /** The back doors: the yard places that start on the House's back porch, the only ones dealt from inside. */
    public static final List<LabyrinthPlace> DOORS = List.of(LabyrinthPlace.YARD_TURNAROUND, LabyrinthPlace.YARD_LONG_DRIVE, LabyrinthPlace.YARD_PORCH_TURN);
    /** The yard's stories, which branch off it. */
    public static final List<LabyrinthPlace> STORIES = List.of(LabyrinthPlace.MARSH_SUPPER, LabyrinthPlace.TALL_GRASS, LabyrinthPlace.GROWING_THINGS, LabyrinthPlace.LAST_CROSSING);

    /** A gray place of the yard. */
    public static boolean place(@Nullable LabyrinthPlace p) {
        return p != null && (YardConnectors.any(p) || YardCrossings.is(p) || TruantYard.is(p) || YardStock.is(p) || YardWind.is(p) || YardStorm.is(p)
                || p == LabyrinthPlace.YARD_REUNION || p == LabyrinthPlace.YARD_SURVEY || p == LabyrinthPlace.YARD_SURVEY_DEEP
                || p == LabyrinthPlace.YARD_FLOOD || p == LabyrinthPlace.SPHINX_FIELD);
    }
    /** Somewhere in the yard: one of its places or its stories. */
    public static boolean in(@Nullable LabyrinthPlace p) { return place(p) || STORIES.contains(p); }

    /** How many yard places in a row this reader has come through, counting {@code here}; 0 when here is not the yard. */
    public static int stretch(LabyrinthData data, UUID player, @Nullable LabyrinthPlace here) {
        if (!place(here)) return 0;
        int n = 1;
        boolean first = true;
        for (String id : data.recent(player)) {
            if (first && id.equals(here.id())) { first = false; continue; }
            first = false;
            if (!place(LabyrinthPlace.byId(id))) break;
            n++;
        }
        return n;
    }

    /**
     * Fits a hall pool to where its doors are. Outside the yard, its places go but the back doors. In the yard, the pool is the rest of
     * the yard while the stretch lasts and some of it is due; otherwise the yard goes and the way leads back inside. Returns true when
     * the pool is the yard's.
     */
    public static boolean fit(List<LabyrinthPlace> gray, LabyrinthData data, UUID player, @Nullable LabyrinthPlace here) {
        if (!place(here)) { gray.removeIf(p -> place(p) && !DOORS.contains(p)); return false; }
        if (stretch(data, player, here) < LONGEST) {
            int depth = data.returnDepth(player);
            List<LabyrinthPlace> yard = new ArrayList<>(gray);
            yard.removeIf(p -> !place(p) || p == here);
            // Inside the yard the drive does not keep coming back round to the porch; the long drive is the one authored time it does.
            if (yard.stream().anyMatch(p -> p != LabyrinthPlace.YARD_TURNAROUND && dealable(data, player, p, depth))) yard.remove(LabyrinthPlace.YARD_TURNAROUND);
            boolean due = yard.stream().anyMatch(p -> dealable(data, player, p, depth));
            if (due) { gray.clear(); gray.addAll(yard); return true; }
        }
        gray.removeIf(YardStretch::place);
        return false;
    }

    /** A yard place an ordinary door can be dealt to now: not a trial (those come by their own chance), and due at this depth. */
    private static boolean dealable(LabyrinthData data, UUID player, LabyrinthPlace p, int depth) {
        return !LabyrinthPacing.anomaly(p) && !LabyrinthPacing.physicalTrial(p) && LabyrinthDealer.grayWeight(data, player, p, depth) > 0;
    }

    /** The stories a door of {@code here} may open on: the yard's only off the yard, and off the yard those first. */
    public static List<LabyrinthPlace> stories(List<LabyrinthPlace> vignettes, @Nullable LabyrinthPlace here) {
        List<LabyrinthPlace> out = new ArrayList<>(vignettes);
        if (!place(here)) { out.removeIf(STORIES::contains); return out; }
        List<LabyrinthPlace> yard = out.stream().filter(STORIES::contains).toList();
        return yard.isEmpty() ? out : new ArrayList<>(yard);
    }

    /** Whether a story out in the yard is waiting for this reader when nothing else is: the back door is dealt in its place. */
    public static boolean waitsOutside(LabyrinthData data, List<LabyrinthPlace> vignettes, List<LabyrinthPlace> fitted, @Nullable LabyrinthPlace here) {
        return !place(here) && fitted.isEmpty() && vignettes.stream().anyMatch(STORIES::contains)
                && LabyrinthBuilder.isPlaceReady(data, LabyrinthPlace.YARD_TURNAROUND);
    }
}
