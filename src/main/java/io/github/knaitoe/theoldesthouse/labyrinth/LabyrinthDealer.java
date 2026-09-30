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

    public static int rememberedWeight(LabyrinthData data, UUID player, LabyrinthPlace place, int ordinaryWeight) {
        boolean special = place.isVignette() || !LabyrinthPacing.ordinary(place);
        if (!special) return ordinaryWeight * 12;
        int age = data.recentVisit(player, place);
        int factor = age < 0 ? 12 : age == 0 ? 1 : age < 3 ? 2 : age < 6 ? 4 : 8;
        return ordinaryWeight * factor;
    }

    private static LabyrinthPlace pickVignette(LabyrinthData data, UUID player, List<LabyrinthPlace> vignettes, RandomSource random) {
        int total = 0;
        for (LabyrinthPlace place : vignettes) {
            total += rememberedWeight(data, player, place, dealWeight(data, place));
        }
        int roll = random.nextInt(total);
        for (LabyrinthPlace place : vignettes) {
            roll -= rememberedWeight(data, player, place, dealWeight(data, place));
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
        int depthTier = Math.min(3, data.returnDepth(player) / 3);
        return Math.min(3, Math.max(depthTier, Math.max(data.vignettesVisited(player), data.completed().size())));
    }

    public static List<LabyrinthPlace> grayAvailable(LabyrinthData data, UUID player) {
        int tier = mazeTier(data, player);
        List<LabyrinthPlace> gray = new ArrayList<>();
        gray.add(LabyrinthPlace.JUNCTION);
        gray.add(LabyrinthPlace.GRAY_CORRIDOR);
        gray.add(LabyrinthPlace.STRAIGHT_HALL);
        gray.add(LabyrinthPlace.BENT_HALL);
        gray.add(LabyrinthPlace.CROSS_HALL);
        gray.add(LabyrinthPlace.QUIET_ROOM);
        int depth = data.returnDepth(player);
        if (depth >= 3) gray.add(LabyrinthPlace.FOLDED_MAZE);
        if (depth >= 6) gray.add(LabyrinthPlace.DEEP_MAZE);
        if (depth >= 9) gray.add(LabyrinthPlace.ABYSS_MAZE);
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
            realDestination = pickVignette(data, player, vignettes, random).id();
            leak = true;
        } else {
            List<LabyrinthPlace> gray = new ArrayList<>(grayAvailable(data, player));
            gray.remove(LabyrinthPlace.DUPLICATE_PASSAGE);
            realDestination = pickGray(gray, data, player, random).id();
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

    public static int grayWeight(LabyrinthPlace place, int depth) {
        return switch (place) {
            case FOLDED_MAZE -> 5 + Math.min(6, Math.max(0, depth - 3));
            case DEEP_MAZE -> 10 + Math.min(8, Math.max(0, depth - 6));
            case ABYSS_MAZE -> 18 + Math.min(12, Math.max(0, depth - 9));
            case STRAIGHT_HALL -> 24;
            case BENT_HALL -> 18;
            case CROSS_HALL -> 15;
            case JUNCTION -> depth >= 9 ? 4 : 8;
            case GRAY_CORRIDOR -> depth < 4 ? 0 : 2;
            case QUIET_ROOM -> 3;
            case EXPLORER_CAMP -> depth < 3 ? 0 : 2;
            default -> depth < 4 ? 0 : place.grayWeight();
        };
    }

    private static LabyrinthPlace pickGray(List<LabyrinthPlace> gray, LabyrinthData data, UUID player, RandomSource random) {
        gray = gray.stream().filter(place -> !LabyrinthPacing.anomaly(place)).toList();
        return weightedGray(gray, data, player, random);
    }

    private static LabyrinthPlace weightedGray(List<LabyrinthPlace> gray, LabyrinthData data, UUID player, RandomSource random) {
        int depth = data.returnDepth(player);
        int totalWeight = 0;
        for (LabyrinthPlace place : gray) {
            totalWeight += rememberedWeight(data, player, place, grayWeight(place, depth));
        }
        if (gray.isEmpty() || totalWeight <= 0) {
            return LabyrinthPlace.JUNCTION;
        }
        int roll = random.nextInt(totalWeight);
        for (LabyrinthPlace place : gray) {
            roll -= rememberedWeight(data, player, place, grayWeight(place, depth));
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

        List<LabyrinthPlace> gray = new ArrayList<>(grayAvailable(data, player));
        // Ordinary stretches do not repeatedly lead straight back into themselves.
        if (here != null) gray.remove(here);
        List<LabyrinthPlace> odd = gray.stream().filter(LabyrinthPacing::anomaly).toList();
        List<LabyrinthData.Door> choices = new ArrayList<>(doors);
        choices.remove(lucky);
        LabyrinthData.Door oddDoor = null;
        if (!odd.isEmpty() && !choices.isEmpty() && random.nextInt(100) < LabyrinthPacing.anomalyChance(data, player)) {
            oddDoor = choices.remove(random.nextInt(choices.size()));
        }
        LabyrinthData.Door restDoor = null;
        if (!choices.isEmpty() && LabyrinthPacing.restDue(data, player) && random.nextInt(100) < 75) {
            restDoor = choices.get(random.nextInt(choices.size()));
        }
        for (LabyrinthData.Door door : doors) {
            if (door == lucky) {
                data.deal(player, door, pickVignette(data, player, vignettes, random).id(), true, scent);
            } else if (door == oddDoor) {
                data.deal(player, door, weightedGray(odd, data, player, random).id(),
                        random.nextInt(100) < LYING_LEAK_CHANCE);
            } else if (door == restDoor) {
                boolean camp = data.returnDepth(player) >= 3 && random.nextBoolean()
                        && data.recentVisit(player, LabyrinthPlace.EXPLORER_CAMP) < 0;
                data.deal(player, door, (camp ? LabyrinthPlace.EXPLORER_CAMP : LabyrinthPlace.QUIET_ROOM).id(), false);
            } else {
                data.deal(player, door, pickGray(gray, data, player, random).id(), random.nextInt(100) < LYING_LEAK_CHANCE);
            }
        }
        data.setDryDeals(player, lucky != null ? 0 : data.dryDeals(player) + 1);
    }
}
