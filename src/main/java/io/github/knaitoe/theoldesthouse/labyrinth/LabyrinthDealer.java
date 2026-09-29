package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.util.RandomSource;

/**
 * The house deals the doors. Players can tilt the odds, but never summon a
 * place on demand.
 *
 * Whenever someone arrives in a place with dealt doors, all of them are
 * dealt again (so backtracking never quite works). At most one of them leads
 * to a vignette; the rest open onto the gray. The chance of a vignette door
 * rises with every dealing that had none, so a long dry spell guarantees
 * one. Vignette doors leak: its heartbeat can be heard at the door before it
 * is opened. Now and then a gray door leaks too, and lies.
 */
public final class LabyrinthDealer {
    public static final int VIGNETTE_BASE_CHANCE = 30;
    public static final int VIGNETTE_CHANCE_STEP = 25;
    /** Percent of gray doors that leak anyway. */
    public static final int LYING_LEAK_CHANCE = 15;

    private LabyrinthDealer() {
    }

    /**
     * Vignettes that can be dealt: one-shots not yet finished, and recurring
     * ones, once they have been made (the Red Room needs a room of the
     * player's to copy).
     */
    public static List<LabyrinthPlace> vignettesAvailable(LabyrinthData data) {
        List<LabyrinthPlace> places = new ArrayList<>();
        for (LabyrinthPlace place : LabyrinthPlace.values()) {
            if (!place.isVignette()
                    || (place.isOneShot() && data.isCompleted(place.id()))
                    || (place.needsMaking() && !data.isReady(place.id()))) {
                continue;
            }
            places.add(place);
        }
        return places;
    }

    /** The chance, in percent, that the next dealing includes a vignette door. */
    public static int vignetteChance(LabyrinthData data) {
        if (vignettesAvailable(data).isEmpty()) {
            return 0;
        }
        return (int) Math.min(100L, VIGNETTE_BASE_CHANCE + (long) VIGNETTE_CHANCE_STEP * data.dryDeals());
    }

    /** Deals every dealt door of a place, as when someone arrives there. */
    public static void dealPlace(LabyrinthData data, LabyrinthPlace place, RandomSource random) {
        List<LabyrinthData.Door> doors = new ArrayList<>();
        for (LabyrinthPlace.DoorSpec spec : place.doors()) {
            LabyrinthData.Door door = data.door(place.doorId(spec));
            if (door != null && LabyrinthData.DEALT.equals(door.destination)) {
                doors.add(door);
            }
        }
        deal(data, doors, place, random);
    }

    /** Deals a set of doors in one go; {@code here} is never dealt back to itself. */
    public static void deal(LabyrinthData data, List<LabyrinthData.Door> doors, @Nullable LabyrinthPlace here, RandomSource random) {
        if (doors.isEmpty()) {
            return;
        }
        List<LabyrinthPlace> vignettes = vignettesAvailable(data);
        LabyrinthData.Door lucky = null;
        if (!vignettes.isEmpty() && random.nextInt(100) < vignetteChance(data)) {
            lucky = doors.get(random.nextInt(doors.size()));
        }

        List<LabyrinthPlace> gray = new ArrayList<>();
        for (LabyrinthPlace place : LabyrinthPlace.values()) {
            if (place.isGray() && place != here) {
                gray.add(place);
            }
        }
        for (LabyrinthData.Door door : doors) {
            if (door == lucky) {
                data.deal(door, vignettes.get(random.nextInt(vignettes.size())).id(), true);
            } else {
                LabyrinthPlace place = gray.isEmpty() ? LabyrinthPlace.JUNCTION : gray.get(random.nextInt(gray.size()));
                data.deal(door, place.id(), random.nextInt(100) < LYING_LEAK_CHANCE);
            }
        }
        data.setDryDeals(lucky != null ? 0 : data.dryDeals() + 1);
    }
}
