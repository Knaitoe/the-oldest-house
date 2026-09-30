package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.util.RandomSource;

/**
 * The house deals the doors. Players can tilt the odds, but never summon a
 * place on demand.
 *
 * The physical doors are shared, but every player's changing graph is their
 * own: destinations, dry spells, leaks and Hillary's scent are stored per
 * player. Two people can therefore stand at the same door and be led
 * somewhere different.
 *
 * Whenever someone arrives in a place with dealt doors, that player's doors
 * are dealt again. At most one leads to a vignette; the rest open onto the
 * gray. A long dry spell guarantees a vignette. Gray places may deal back to
 * themselves: walking through a door and arriving somewhere impossibly
 * familiar is part of the maze, not an error.
 *
 * The gray starts small and grows with progress. The junction and plain
 * corridor are always in the pool. Reaching/finishing vignettes adds the
 * long hallway, flooded passage, false distance and the explorer camp;
 * then the spiral stair, fractured walkway, light sink and moving threshold;
 * then the hotel hallway, compression passage, gravity drift and duplicate passage.
 */
public final class LabyrinthDealer {
    public static final int VIGNETTE_BASE_CHANCE = 30;
    public static final int VIGNETTE_CHANCE_STEP = 25;
    /** Percent of gray doors that leak anyway. */
    public static final int LYING_LEAK_CHANCE = 15;
    /** How much likelier a begun, unfinished multi-visit vignette is than any other vignette. */
    public static final int UNFINISHED_WEIGHT = 3;

    private LabyrinthDealer() {
    }

    public static List<LabyrinthPlace> vignettesAvailable(LabyrinthData data) {
        List<LabyrinthPlace> places = new ArrayList<>();
        for (LabyrinthPlace place : LabyrinthPlace.values()) {
            if (!place.isVignette()
                    || (place.isFinishable() && data.isCompleted(place.id()))
                    || (place.needsMaking() && !data.isReady(place.id()))) {
                continue;
            }
            places.add(place);
        }
        return places;
    }

    public static int vignetteChance(LabyrinthData data, UUID player) {
        if (vignettesAvailable(data).isEmpty()) {
            return 0;
        }
        return (int) Math.min(100L,
                VIGNETTE_BASE_CHANCE + (long) VIGNETTE_CHANCE_STEP * data.dryDeals(player));
    }

    public static int dealWeight(LabyrinthData data, LabyrinthPlace place) {
        return place.isMultiVisit() && !data.isCompleted(place.id()) && data.state(place.id()).getInt("Visit") > 0
                ? UNFINISHED_WEIGHT : 1;
    }

    private static LabyrinthPlace pickVignette(LabyrinthData data, List<LabyrinthPlace> vignettes, RandomSource random) {
        int total = 0;
        for (LabyrinthPlace place : vignettes) {
            total += dealWeight(data, place);
        }
        int roll = random.nextInt(total);
        for (LabyrinthPlace place : vignettes) {
            roll -= dealWeight(data, place);
            if (roll < 0) {
                return place;
            }
        }
        return vignettes.get(vignettes.size() - 1);
    }

    /**
     * How far this player's gray maze has grown. Shared completed vignettes
     * also count, so an established world's House does not shrink for a new
     * player or after upgrading an older save.
     */
    public static int mazeTier(LabyrinthData data, UUID player) {
        return Math.min(3, Math.max(data.vignettesVisited(player), data.completed().size()));
    }

    public static List<LabyrinthPlace> grayAvailable(LabyrinthData data, UUID player) {
        int tier = mazeTier(data, player);
        List<LabyrinthPlace> gray = new ArrayList<>();
        gray.add(LabyrinthPlace.JUNCTION);
        gray.add(LabyrinthPlace.GRAY_CORRIDOR);
        if (tier >= 1) {
            gray.add(LabyrinthPlace.LONG_HALLWAY);
            gray.add(LabyrinthPlace.FLOODED_PASSAGE);
            gray.add(LabyrinthPlace.FALSE_DISTANCE);
            gray.add(LabyrinthPlace.EXPLORER_CAMP);
        }
        if (tier >= 2) {
            gray.add(LabyrinthPlace.SPIRAL_STAIR);
            gray.add(LabyrinthPlace.FRACTURED_WALKWAY);
            gray.add(LabyrinthPlace.LIGHT_SINK);
            gray.add(LabyrinthPlace.MOVING_THRESHOLD);
        }
        if (tier >= 3) {
            gray.add(LabyrinthPlace.HOTEL_HALLWAY);
            gray.add(LabyrinthPlace.COMPRESSION_PASSAGE);
            gray.add(LabyrinthPlace.GRAVITY_DRIFT);
            gray.add(LabyrinthPlace.DUPLICATE_PASSAGE);
        }
        return gray;
    }

    public enum Scent {
        SEEKING,
        ALREADY,
        NOTHING
    }

    public static Scent giveScent(LabyrinthData data, UUID player) {
        if (vignettesAvailable(data).isEmpty()) {
            return Scent.NOTHING;
        }
        if (data.hillaryScent(player)) {
            return Scent.ALREADY;
        }
        data.setHillaryScent(player, true);
        return Scent.SEEKING;
    }

    public static void dealPlace(LabyrinthData data, UUID player, LabyrinthPlace place, RandomSource random) {
        List<LabyrinthData.Door> doors = new ArrayList<>();
        for (LabyrinthPlace.DoorSpec spec : place.doors()) {
            LabyrinthData.Door door = data.door(place.doorId(spec));
            if (door != null && LabyrinthData.DEALT.equals(door.destination)) {
                doors.add(door);
            }
        }
        if (place == LabyrinthPlace.DUPLICATE_PASSAGE) {
            dealDuplicatePassage(data, player, doors, random);
            return;
        }
        deal(data, player, doors, place, random);
    }

    private static void dealDuplicatePassage(
            LabyrinthData data,
            UUID player,
            List<LabyrinthData.Door> doors,
            RandomSource random
    ) {
        if (doors.isEmpty()) {
            return;
        }

        LabyrinthData.Door real = doors.get(random.nextInt(doors.size()));
        List<LabyrinthPlace> vignettes = vignettesAvailable(data);
        boolean scent = data.hillaryScent(player) && !vignettes.isEmpty();
        boolean vignette = !vignettes.isEmpty()
                && (scent || random.nextInt(100) < vignetteChance(data, player));

        String realDestination;
        boolean leak;
        boolean bark = false;
        if (vignette) {
            if (scent) {
                List<LabyrinthPlace> unfound = new ArrayList<>();
                for (LabyrinthPlace candidate : vignettes) {
                    if (candidate.isFinishable()) {
                        unfound.add(candidate);
                    }
                }
                if (!unfound.isEmpty()) {
                    vignettes = unfound;
                }
                data.setHillaryScent(player, false);
                bark = true;
            }
            realDestination = pickVignette(data, vignettes, random).id();
            leak = true;
        } else {
            List<LabyrinthPlace> gray = new ArrayList<>(grayAvailable(data, player));
            gray.remove(LabyrinthPlace.DUPLICATE_PASSAGE);
            realDestination = pickGray(gray, random).id();
            leak = random.nextInt(100) < LYING_LEAK_CHANCE;
        }

        for (LabyrinthData.Door door : doors) {
            if (door == real) {
                data.deal(player, door, realDestination, leak, bark);
            } else {
                data.deal(player, door, LabyrinthPlace.DUPLICATE_PASSAGE.id(), false, false);
            }
        }
        data.setDryDeals(player, vignette ? 0 : data.dryDeals(player) + 1);
    }

    private static LabyrinthPlace pickGray(List<LabyrinthPlace> gray, RandomSource random) {
        int totalWeight = 0;
        for (LabyrinthPlace place : gray) {
            totalWeight += place.grayWeight();
        }
        if (gray.isEmpty() || totalWeight <= 0) {
            return LabyrinthPlace.JUNCTION;
        }
        int roll = random.nextInt(totalWeight);
        for (LabyrinthPlace place : gray) {
            roll -= place.grayWeight();
            if (roll < 0) {
                return place;
            }
        }
        return gray.get(gray.size() - 1);
    }

    public static void deal(LabyrinthData data, UUID player, List<LabyrinthData.Door> doors,
                            @Nullable LabyrinthPlace here, RandomSource random) {
        if (doors.isEmpty()) {
            return;
        }
        List<LabyrinthPlace> vignettes = vignettesAvailable(data);
        LabyrinthData.Door lucky = null;
        boolean scent = data.hillaryScent(player) && !vignettes.isEmpty();
        if (scent) {
            List<LabyrinthPlace> unfound = new ArrayList<>();
            for (LabyrinthPlace place : vignettes) {
                if (place.isFinishable()) {
                    unfound.add(place);
                }
            }
            if (!unfound.isEmpty()) {
                vignettes = unfound;
            }
            data.setHillaryScent(player, false);
        }
        if (!vignettes.isEmpty() && (scent || random.nextInt(100) < vignetteChance(data, player))) {
            lucky = doors.get(random.nextInt(doors.size()));
        }

        List<LabyrinthPlace> gray = grayAvailable(data, player);
        for (LabyrinthData.Door door : doors) {
            if (door == lucky) {
                data.deal(player, door, pickVignette(data, vignettes, random).id(), true, scent);
            } else {
                data.deal(player, door, pickGray(gray, random).id(), random.nextInt(100) < LYING_LEAK_CHANCE);
            }
        }
        data.setDryDeals(player, lucky != null ? 0 : data.dryDeals(player) + 1);
    }
}
