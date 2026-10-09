package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseConfig;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.util.RandomSource;

/**
 * The house deals the doors. Players can tilt the odds, but never summon a
 * place on demand.
 *
 * Physical doors and new ordinary links are shared. Story availability,
 * saved discoveries, dry spells, leaks and Hillary's scent remain personal.
 *
 * A new route is dealt once; revisits restore it. Ordinary links discovered
 * on the same new route are shared, while story availability and deliberate
 * companion searches remain personal. A long eligible dry spell still
 * guarantees a story. One discovery budget prevents stacking a story,
 * anomaly and physical hazard at the same arrival.
 *
 * The approach stays domestic for the first five crossings. Stories become
 * possible at six, impossible geometry at eight, and larger expansions at
 * twelve and sixteen. An active pet rescue or a deliberately given scent
 * can still find its story without waiting for the ordinary approach.
 */
public final class LabyrinthDealer {
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
                    || place == LabyrinthPlace.SHALLOWS
                    || place == LabyrinthPlace.PHONE_CANOE
                    || place == LabyrinthPlace.GOATMAN
                    || place == LabyrinthPlace.TED_CAVER
                    || place == LabyrinthPlace.HOLLOWAY_CAMP
                    || (NovelVignettes.isNovel(place) && place.kind()!=LabyrinthPlace.Kind.RECURRING)
                    || LiteraryRooms.isLiterary(place)
                    || (place.isFinishable() && data.isCompleted(place.id()))
                    || (place.needsMaking() && !data.isReady(place.id()))) {
                continue;
            }
            places.add(place);
        }
        return places;
    }

    /** A shared ending can still be examined by an explorer who has not recorded its aftermath. */
    public static List<LabyrinthPlace> vignettesAvailable(LabyrinthData data,UUID player) {
        List<LabyrinthPlace> places=new ArrayList<>(vignettesAvailable(data));
        for(var site:LabyrinthPlace.values())if(LiteraryVignettes.canDeal(data,player,site))places.add(site);
        if (IndianLakeProgress.canDealShallows(data, player)) places.add(LabyrinthPlace.SHALLOWS);
        if (PhoneCanoe.canDeal(data,player)) places.add(LabyrinthPlace.PHONE_CANOE);
        if (GoatmanVignette.canDeal(data,player)) places.add(LabyrinthPlace.GOATMAN);
        if (CaverVignette.canDeal(data,player)) places.add(LabyrinthPlace.TED_CAVER);
        if (HollowayVignette.canDeal(data,player)) places.add(LabyrinthPlace.HOLLOWAY_CAMP);
        for(var p:NovelVignettes.PLACES)if(NovelVignettes.canDeal(data,player,p)&&!places.contains(p))places.add(p);
        if(data.isCompleted(PreservedCave.ID)&&PreservedCave.phoneWaiting(data,player))places.add(LabyrinthPlace.PRESERVED_CAVE);
        for(LabyrinthPlace place:LabyrinthPlace.values()){
            WitnessAccount.Story story=WitnessAccount.Story.of(place.id());
            if(story!=null&&!LiteraryRooms.isLiterary(place)&&!NovelVignettes.isNovel(place)&&place.isFinishable()&&data.isCompleted(place.id())&&!WitnessAccount.has(data,player,story)&&!places.contains(place))places.add(place);
        }
        // A recurring home copy needs an interval; a deliberate scent may still seek it.
        if(!data.hillaryScent(player))places.removeIf(p->p==LabyrinthPlace.RED_ROOM
                && data.recentVisit(player,p)>=0 && data.recentVisit(player,p)<6);
        places.removeIf(site->LiteraryCabinChoices.closed(data,player,site));
        // Only places that already stand can be dealt; deeper ones are still being built ahead of explorers.
        places.removeIf(site->site!=LabyrinthPlace.FAMILY_COPY&&site!=LabyrinthPlace.OLD_CABIN&&!LabyrinthBuilder.isPlaceReady(data,site));
        return places;
    }

    public static int vignetteChance(LabyrinthData data, UUID player) {
        if (vignettesAvailable(data,player).isEmpty()) {
            return 0;
        }
        boolean rescue = rescueNeeded(data, player);
        int depth = data.returnDepth(player);
        if (!rescue && LabyrinthPacing.domestic(depth)) return 0;
        if (!rescue && !LabyrinthPacing.storyDue(data, player)) return 0;
        int base = rescue ? 65 : HouseConfig.setting(depth < 10 ? HouseConfig.STORY_EARLY_CHANCE : HouseConfig.STORY_BASE_CHANCE);
        int step = rescue ? 25 : HouseConfig.setting(HouseConfig.STORY_CHANCE_STEP);
        if (!rescue && data.dryDeals(player)>=HouseConfig.setting(HouseConfig.STORY_DRY_GUARANTEE)) return 100;
        return (int) Math.min(100L, base + (long) step * data.dryDeals(player));
    }

    public static int dealWeight(LabyrinthData data, LabyrinthPlace place) {
        return place.isMultiVisit() && !data.isCompleted(place.id()) && data.state(place.id()).getInt("Visit") > 0
                ? UNFINISHED_WEIGHT : 1;
    }

    public static int rememberedWeight(LabyrinthData data, UUID player, LabyrinthPlace place, int ordinaryWeight) {
        if(place==LabyrinthPlace.MOTHER_DEN&&rescueNeeded(data,player))return Math.max(1,ordinaryWeight)*96;
        // 0.4.53: what followed a reader home from the trailer keeps the trailer near; the way to be rid of it is there.
        if(place==LabyrinthPlace.GOATMAN&&GoatmanHaunt.haunted(data,player))return Math.max(1,ordinaryWeight)*24;
        boolean special = place.isVignette() || !LabyrinthPacing.ordinary(place);
        int age = data.recentVisit(player, place);
        if (!special) return ordinaryWeight * (age < 0 ? 12 : age < 3 ? 3 : 8);
        int factor = age < 0 ? 12 : age == 0 ? 1 : age < 3 ? 2 : age < 6 ? 4 : 8;
        return io.github.knaitoe.theoldesthouse.house.HouseExperience.weight(data,player,place,ordinaryWeight * factor);
    }

    public static boolean rescueNeeded(LabyrinthData data,UUID player) {
        return data.state(MotherOfStrays.ID).getCompound("LivingPetOwners").getBoolean(player.toString());
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

    /** Route depth controls pressure, even in a world with completed stories. */
    public static int mazeTier(LabyrinthData data, UUID player) {
        int depth = data.returnDepth(player);
        return depth < LabyrinthPacing.STORY_DEPTH ? 0 : Math.min(3, 1 + (depth - LabyrinthPacing.STORY_DEPTH) / 4);
    }

    public static List<LabyrinthPlace> grayAvailable(LabyrinthData data, UUID player) {
        int tier = mazeTier(data, player);
        List<LabyrinthPlace> gray = new ArrayList<>();
        gray.add(LabyrinthPlace.JUNCTION);
        gray.add(LabyrinthPlace.GRAY_CORRIDOR);
        gray.add(LabyrinthPlace.STRAIGHT_HALL);
        gray.add(LabyrinthPlace.BENT_HALL);
        gray.add(LabyrinthPlace.CROSS_HALL);
        gray.add(LabyrinthPlace.ALCOVE_HALL);
        gray.add(LabyrinthPlace.OFFSET_HALL);
        gray.add(LabyrinthPlace.SERVICE_LANDING);
        gray.add(LabyrinthPlace.QUIET_ROOM);
        int depth = data.returnDepth(player);
        if (depth >= LabyrinthPacing.STRANGE_DEPTH) gray.add(LabyrinthPlace.FOLDED_MAZE);
        if (depth >= LabyrinthPacing.DEEP_DEPTH) gray.add(LabyrinthPlace.DEEP_MAZE);
        if (depth >= LabyrinthPacing.ABYSS_DEPTH) gray.add(LabyrinthPlace.ABYSS_MAZE);
        // After seven forward crossings, newly discovered ordinary halls are stone.
        if (depth >= LabyrinthPacing.STONE_DEPTH) {
            gray.add(LabyrinthPlace.STONE_GALLERY);
            gray.add(LabyrinthPlace.STONE_CROSSING);
            gray.add(LabyrinthPlace.STONE_DESCENT);
            gray.add(LabyrinthPlace.STONE_ARCADE);
            gray.add(LabyrinthPlace.STONE_BEND);
            gray.add(LabyrinthPlace.STONE_LANDING);
        }
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
            gray.add(LabyrinthPlace.BLIND_STRETCH);
            gray.add(LabyrinthPlace.MOVING_THRESHOLD);
        }
        if (tier >= 3) {
            gray.add(LabyrinthPlace.HOTEL_HALLWAY);
            gray.add(LabyrinthPlace.COMPRESSION_PASSAGE);
            gray.add(LabyrinthPlace.GRAVITY_DRIFT);
            gray.add(LabyrinthPlace.DUPLICATE_PASSAGE);
        }
        gray.removeIf(place -> !LabyrinthBuilder.isPlaceReady(data, place));
        return gray;
    }

    public enum Scent {
        SEEKING,
        ALREADY,
        NOTHING
    }

    public static Scent giveScent(LabyrinthData data, UUID player) {
        if (vignettesAvailable(data,player).isEmpty()) {
            return Scent.NOTHING;
        }
        if (data.hillaryScent(player)) {
            return Scent.ALREADY;
        }
        data.setHillaryScent(player, true);
        return Scent.SEEKING;
    }

    private static List<LabyrinthData.Door> dealtDoors(LabyrinthData data, LabyrinthPlace place) {
        List<LabyrinthData.Door> doors = new ArrayList<>();
        for (LabyrinthPlace.DoorSpec spec : place.doors()) {
            LabyrinthData.Door door = data.door(place.doorId(spec));
            if (door != null && LabyrinthData.DEALT.equals(door.destination)) {
                doors.add(door);
            }
        }
        return doors;
    }

    /**
     * Arriving through a door: a place reached by the same route as before
     * keeps what lies behind its doors, so the labyrinth can be mapped and
     * returned to. Somewhere new is dealt from its route, so explorers who
     * take the same doors from the same hallway find the same halls. Only a
     * door whose story has closed to this player, or a hunt for a story or a
     * lost pet, deals afresh.
     */
    public static void arriveAt(LabyrinthData data, UUID player, LabyrinthPlace place, long salt) {
        arriveAt(data, player, place, salt, true);
    }

    /** As above. A spectator ({@code participant} false) keeps a personal map but never writes the shared hall cache. */
    public static void arriveAt(LabyrinthData data, UUID player, LabyrinthPlace place, long salt, boolean participant) {
        List<LabyrinthData.Door> doors = dealtDoors(data, place);
        if (doors.isEmpty()) return;
        long key = data.nodeKey(player, place);
        Map<String, LabyrinthData.Deal> remembered = data.node(player, key);
        boolean fresh = remembered == null;
        boolean searching = data.hillaryScent(player) || rescueNeeded(data, player);
        RandomSource random = RandomSource.create(key ^ salt);
        // Existing shared discoveries and personal maps remain exact. Searches take priority.
        boolean breathe=fresh&&participant&&!searching&&(LabyrinthPacing.ordinary(place)||LabyrinthPacing.quiet(place))
                &&ExpeditionRhythm.pending(data,player)&&data.stateEntry("shared_halls_0448",Long.toUnsignedString(key^salt)).isEmpty();
        if (remembered == null || data.hillaryScent(player) || rescueNeeded(data, player)) {
            if(!breathe||!ExpeditionRhythm.deal(data,player,doors,place,random)){breathe=false;dealPlace(data, player, place, random);}
        } else {
            List<LabyrinthData.Door> closed = new ArrayList<>();
            for (LabyrinthData.Door door : doors) {
                LabyrinthData.Deal deal = remembered.get(door.id);
                if (deal == null || !stillDealable(data, player, deal.place())) closed.add(door);
                else data.deal(player, door, deal.place(), deal.leak(), false);
            }
            if (!closed.isEmpty()) deal(data, player, closed, place, random);
        }
        Map<String, LabyrinthData.Deal> now = new LinkedHashMap<>();
        for (LabyrinthData.Door door : doors) {
            LabyrinthData.Deal deal = data.deal(player, door);
            if (deal != null) now.put(door.id, deal);
        }
        if (fresh && !searching && participant) {
            var shared = data.stateEntry("shared_halls_0448", Long.toUnsignedString(key ^ salt));
            for (var door : doors) {
                var own = now.get(door.id);
                var current = own == null ? null : LabyrinthPlace.byId(own.place());
                if (current == null || !(LabyrinthPacing.ordinary(current)||LabyrinthPacing.quiet(current))) continue;
                var held = LabyrinthPlace.byId(shared.getString(door.id));
                if (held != null && held != place && (LabyrinthPacing.ordinary(held)||LabyrinthPacing.quiet(held))
                        && grayAvailable(data, player).contains(held) && grayWeight(held, data.returnDepth(player)) > 0) {
                    data.deal(player, door, held.id(), own.leak(), own.bark());
                    now.put(door.id, data.deal(player, door));
                } else if (held == null) shared.putString(door.id, current.id());
            }
            data.setBoundedStateEntry("shared_halls_0448", Long.toUnsignedString(key ^ salt), shared, 2048);
        }
        data.rememberNode(player, key, now);
        if(breathe)ExpeditionRhythm.consumed(data,player);
        // A remembered map never strands anyone: if nothing here leads on, an ordinary door does.
        if (!hasWayOn(data, player, place, null)) {
            for (LabyrinthData.Door door : doors) {
                LabyrinthData.Deal deal = data.deal(player, door);
                LabyrinthPlace dealt = deal == null ? null : LabyrinthPlace.byId(deal.place());
                if (deal != null && FinaleArchitecture.ID.equals(deal.place())) continue;
                if (dealt != null && dealt.isVignette()) continue; // a story behind the only door is kept; its door leads on afterwards
                if (VignetteGate.dormant(data, player, door)) continue;
                openWayOn(data, player, door, place, random);
                break;
            }
        }
    }

    // ------------------------------------------------------------------
    // The way on: exploring deeper is never left to chance.

    /** Whether a place has doors of its own that lead further in. Loops only lead on slowly, so they do not count. */
    public static boolean onward(LabyrinthPlace place) {
        if (LabyrinthPacing.loop(place)) return false;
        for (LabyrinthPlace.DoorSpec spec : place.doors()) if (LabyrinthData.DEALT.equals(spec.destination())) return true;
        return false;
    }

    /** Whether this door, for this player, leads somewhere they can go on from. The great staircase is the deepest way on. */
    public static boolean wayOn(LabyrinthData data, UUID player, LabyrinthData.Door door) {
        LabyrinthData.Deal deal = data.deal(player, door);
        if (deal == null || VignetteGate.dormant(data, player, door)) return false;
        if (FinaleArchitecture.ID.equals(deal.place())) return finaleOffered(data, player);
        LabyrinthPlace place = LabyrinthPlace.byId(deal.place());
        return place != null && onward(place) && LabyrinthBuilder.isPlaceReady(data, place);
    }

    /** Whether any of a place's dealt doors (but {@code except}) leads on for this player. */
    public static boolean hasWayOn(LabyrinthData data, UUID player, LabyrinthPlace place, @Nullable LabyrinthData.Door except) {
        for (LabyrinthData.Door door : dealtDoors(data, place)) if (door != except && wayOn(data, player, door)) return true;
        return false;
    }

    /** Turns one door of the place the player is in into an ordinary way on, and remembers it there. */
    public static void openWayOn(LabyrinthData data, UUID player, LabyrinthData.Door door, LabyrinthPlace here, RandomSource random) {
        List<LabyrinthPlace> gray = new ArrayList<>(grayAvailable(data, player));
        gray.remove(here);
        gray.removeIf(p -> !onward(p) || LabyrinthPacing.anomaly(p) || LabyrinthPacing.physicalTrial(p));
        LabyrinthPlace next = gray.isEmpty() ? LabyrinthPlace.JUNCTION : weightedGray(gray, data, player, random);
        data.deal(player, door, next.id(), false);
        long key = data.nodeKey(player, here);
        Map<String, LabyrinthData.Deal> now = new LinkedHashMap<>();
        for (LabyrinthData.Door each : dealtDoors(data, here)) {
            LabyrinthData.Deal deal = data.deal(player, each);
            if (deal != null) now.put(each.id, deal);
        }
        data.rememberNode(player, key, now);
    }

    /** The great staircase waits in the deep labyrinth, never in its early halls. */
    public static boolean finaleOffered(LabyrinthData data, UUID player) {
        return data.returnDepth(player) >= LabyrinthPacing.STAIRCASE_DEPTH && FinaleController.canOffer(data, player);
    }

    /** Whether a remembered destination can still be found behind its door by this player. */
    private static boolean stillDealable(LabyrinthData data, UUID player, String id) {
        if (FinaleArchitecture.ID.equals(id)) return finaleOffered(data, player);
        LabyrinthPlace place = LabyrinthPlace.byId(id);
        if (place == null || !LabyrinthBuilder.isPlaceReady(data, place)) return false;
        return !place.isVignette() || vignettesAvailable(data, player).contains(place);
    }

    public static void dealPlace(LabyrinthData data, UUID player, LabyrinthPlace place, RandomSource random) {
        List<LabyrinthData.Door> doors = dealtDoors(data, place);
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
        List<LabyrinthPlace> vignettes = vignettesAvailable(data,player);
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
            leak = lyingLeak(data, player, random);
        }

        for (LabyrinthData.Door door : doors) {
            if (door == real) {
                data.deal(player, door, realDestination, leak, bark);
            } else {
                data.deal(player, door, LabyrinthPlace.DUPLICATE_PASSAGE.id(), false, false);
            }
        }
        recordDryDeal(data, player, vignette);
    }

    public static int grayWeight(LabyrinthPlace place, int depth) {
        return switch (place) {
            case FOLDED_MAZE -> 5 + Math.min(6, Math.max(0, depth - LabyrinthPacing.STRANGE_DEPTH));
            case DEEP_MAZE -> 10 + Math.min(8, Math.max(0, depth - LabyrinthPacing.DEEP_DEPTH));
            case ABYSS_MAZE -> 18 + Math.min(12, Math.max(0, depth - LabyrinthPacing.ABYSS_DEPTH));
            case STRAIGHT_HALL, BENT_HALL -> depth >= LabyrinthPacing.STONE_DEPTH ? 0 : 26;
            case CROSS_HALL -> depth >= LabyrinthPacing.STONE_DEPTH ? 0 : 12;
            case ALCOVE_HALL, OFFSET_HALL -> depth >= LabyrinthPacing.STONE_DEPTH ? 0 : 22;
            case SERVICE_LANDING -> depth >= LabyrinthPacing.STONE_DEPTH ? 0 : 12;
            case STONE_GALLERY -> depth < LabyrinthPacing.STONE_DEPTH ? 0 : 26;
            case STONE_CROSSING -> depth < LabyrinthPacing.STONE_DEPTH ? 0 : 14;
            case STONE_DESCENT -> depth < LabyrinthPacing.STONE_DEPTH ? 0 : 16;
            case STONE_ARCADE, STONE_BEND -> depth < LabyrinthPacing.STONE_DEPTH ? 0 : 22;
            case STONE_LANDING -> depth < LabyrinthPacing.STONE_DEPTH ? 0 : 12;
            case LONG_HALLWAY, HOTEL_HALLWAY, SPIRAL_STAIR, DUPLICATE_PASSAGE -> 1;
            case JUNCTION -> depth >= LabyrinthPacing.STONE_DEPTH ? 0 : 8;
            case GRAY_CORRIDOR -> depth < 4 || depth >= LabyrinthPacing.STONE_DEPTH ? 0 : 2;
            case QUIET_ROOM -> 3;
            case EXPLORER_CAMP -> depth < 3 ? 0 : 2;
            default -> depth < 4 ? 0 : place.grayWeight();
        };
    }

    private static boolean lyingLeak(LabyrinthData data, UUID player, RandomSource random) {
        return !LabyrinthPacing.domestic(data.returnDepth(player)) && random.nextInt(100) < LYING_LEAK_CHANCE;
    }

    private static void recordDryDeal(LabyrinthData data, UUID player, boolean story) {
        // Exploring the quiet approach cannot bank an immediate guaranteed story.
        if (story) data.setDryDeals(player, 0);
        else if ((!LabyrinthPacing.domestic(data.returnDepth(player))&&LabyrinthPacing.storyDue(data,player)) || rescueNeeded(data, player))
            data.setDryDeals(player, (int) Math.min(100L, (long) data.dryDeals(player) + 1));
    }

    private static LabyrinthPlace pickGray(List<LabyrinthPlace> gray, LabyrinthData data, UUID player, RandomSource random) {
        gray = gray.stream().filter(place -> !LabyrinthPacing.anomaly(place)&&!LabyrinthPacing.physicalTrial(place)).toList();
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
        List<LabyrinthPlace> vignettes = vignettesAvailable(data,player);
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
        // Ordinary stretches do not repeatedly lead straight back into themselves,
        // nor ping-pong back into the hall the explorer has just come from.
        if (here != null) gray.remove(here);
        if (gray.stream().filter(p -> data.recentVisit(player, p) != 1).count() >= 2)
            gray.removeIf(p -> data.recentVisit(player, p) == 1);
        List<LabyrinthPlace> odd = gray.stream().filter(LabyrinthPacing::anomaly)
                .filter(p -> !LabyrinthPacing.loop(p) || LabyrinthPacing.loopDue(data, player)).toList();
        List<LabyrinthData.Door> choices = new ArrayList<>(doors);
        choices.remove(lucky);
        LabyrinthData.Door oddDoor = null;
        if (lucky == null && !odd.isEmpty() && !choices.isEmpty() && random.nextInt(100) < LabyrinthPacing.anomalyChance(data, player)) {
            oddDoor = choices.remove(random.nextInt(choices.size()));
        }
        LabyrinthData.Door restDoor = null;
        List<LabyrinthPlace> trials=gray.stream().filter(LabyrinthPacing::physicalTrial).toList();
        LabyrinthData.Door trialDoor=null;
        if(lucky==null&&oddDoor==null&&!trials.isEmpty()&&!choices.isEmpty()&&random.nextInt(100)<LabyrinthPacing.trialChance(data,player))trialDoor=choices.remove(random.nextInt(choices.size()));
        if (!choices.isEmpty() && LabyrinthPacing.restDue(data, player) && random.nextInt(100) < 35) {
            restDoor = choices.get(random.nextInt(choices.size()));
        }
        for (LabyrinthData.Door door : doors) {
            if (door == lucky) {
                data.deal(player, door, pickVignette(data, player, vignettes, random).id(), true, scent);
            } else if (door == oddDoor) {
                data.deal(player, door, weightedGray(odd, data, player, random).id(),
                        lyingLeak(data, player, random));
            } else if (door == restDoor) {
                boolean camp = data.returnDepth(player) >= 3 && random.nextBoolean()
                        && data.recentVisit(player, LabyrinthPlace.EXPLORER_CAMP) < 0;
                data.deal(player, door, (camp ? LabyrinthPlace.EXPLORER_CAMP : LabyrinthPlace.QUIET_ROOM).id(), false);
            } else if(door==trialDoor){
                data.deal(player,door,weightedGray(trials,data,player,random).id(),lyingLeak(data,player,random));
            } else {
                data.deal(player, door, pickGray(gray, data, player, random).id(), lyingLeak(data, player, random));
            }
        }
        if (finaleOffered(data, player) && random.nextInt(100) < 18) {
            LabyrinthData.Door vignetteDoor = lucky;
            List<LabyrinthData.Door> finaleDoors = doors.stream().filter(d -> d != vignetteDoor).toList();
            if (!finaleDoors.isEmpty()) data.deal(player, finaleDoors.get(random.nextInt(finaleDoors.size())), FinaleArchitecture.ID, false);
        }
        recordDryDeal(data, player, lucky != null);
    }
}
