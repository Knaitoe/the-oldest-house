package io.github.knaitoe.theoldesthouse.opening;

import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseLayout;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthData;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthMaze;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthPlace;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthPlaces;
import io.github.knaitoe.theoldesthouse.labyrinth.MazeLayout;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.phys.Vec3;

/** A companion's nose points to real doors; the player still has to walk and cross them. */
public final class HillaryPaths {
    private HillaryPaths() {}
    public static void askForExit(Wolf wolf, ServerPlayer player) {
        if (!player.serverLevel().dimension().equals(HouseDimensions.INTERIOR)) {
            wolf.playSound(SoundEvents.WOLF_WHINE, 0.7F, 1.1F);
            return;
        }
        CompanionOrders.issue(wolf, player, CompanionOrders.Order.EXIT);
        wolf.getPersistentData().putBoolean("HillaryFindExit", true);
        wolf.setOrderedToSit(false); wolf.setInSittingPose(false);
        wolf.getNavigation().stop();
        wolf.playSound(SoundEvents.WOLF_AMBIENT, 0.8F, 1.2F);
    }
    public static boolean tickExit(Wolf wolf, ServerLevel level, UUID recipient) {
        if (!wolf.getPersistentData().getBoolean("HillaryFindExit") || wolf.isOrderedToSit()) {
            if (wolf.isTame() && wolf.getOwnerUUID() == null) wolf.setOwnerUUID(recipient);
            return false;
        }
        if (!level.dimension().equals(HouseDimensions.INTERIOR)) {
            wolf.getPersistentData().remove("HillaryFindExit");
            if (wolf.isTame()) wolf.setOwnerUUID(recipient);
            return false;
        }
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(recipient);
        if (player == null || player.level() != level) return true;
        BlockPos origin = HouseSavedData.get(level.getServer()).houseOrigin();
        if (origin == null) return true;
        LabyrinthPlace place = LabyrinthPlaces.placeAt(origin, player.blockPosition());
        BlockPos goal;
        BlockPos base = place == null ? null : LabyrinthPlaces.base(origin, place);
        if (base != null) {
            goal = base.offset(0, 0, -1);
        } else if (player.getZ() - origin.getZ() > HouseLayout.THRESHOLD_Z) {
            goal = origin.offset(HouseLayout.AXIS_X, 1, HouseLayout.THRESHOLD_Z - 1);
        } else {
            goal = origin.offset(HouseLayout.AXIS_X, 1, HouseLayout.FRONT_DOOR_Z + 1);
        }
        lead(wolf, player, goal, place, base);
        return true;
    }
    public static boolean tickDeeper(Wolf wolf, ServerLevel level, UUID recipient) {
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(recipient);
        BlockPos origin = HouseSavedData.get(level.getServer()).houseOrigin();
        if (player == null || player.level() != level || origin == null) return false;
        LabyrinthPlace place = LabyrinthPlaces.placeAt(origin, player.blockPosition());
        BlockPos base = place == null ? null : LabyrinthPlaces.base(origin, place);
        if (place == null || base == null) {
            lead(wolf, player, origin.offset(HouseLayout.AXIS_X, 1, HouseLayout.THRESHOLD_Z + 4), null, null);
            return true;
        }
        LabyrinthData data = LabyrinthData.get(level.getServer());
        for (LabyrinthPlace.DoorSpec spec : place.doors()) {
            LabyrinthData.Door door = data.door(place.doorId(spec));
            LabyrinthData.Deal deal = door == null ? null : data.deal(recipient, door);
            if (deal != null && deal.bark()) {
                lead(wolf, player, door.lower.relative(door.facing, 2), place, base);
                return true;
            }
        }
        return false;
    }
    public static void lead(TamableAnimal wolf, ServerPlayer player, BlockPos goal, @Nullable LabyrinthPlace place, @Nullable BlockPos base) {
        if (wolf.distanceToSqr(player) > 100) {
            wolf.getNavigation().stop();
            wolf.getLookControl().setLookAt(player, 30, 30);
            return;
        }
        wolf.setOrderedToSit(false); wolf.setInSittingPose(false); wolf.clearRestriction();
        Vec3 destination = Vec3.atBottomCenterOf(goal);
        if (wolf.position().distanceToSqr(destination) <= 2.25) {
            wolf.getNavigation().stop();
            wolf.getLookControl().setLookAt(destination.add(0, 1, 0));
            if (wolf.level().getGameTime() % 80 == 0) wolf.playSound(wolf instanceof net.minecraft.world.entity.animal.Cat ? SoundEvents.CAT_AMBIENT : SoundEvents.WOLF_WHINE, 0.65F, 1.15F);
            return;
        }
        if (wolf.level().getGameTime() % 10 != 0) return;
        BlockPos step = goal;
        if (base != null && LabyrinthMaze.isMaze(place)) {
            MazeLayout layout = LabyrinthMaze.layout(player.server, place);
            BlockPos next = nextStep(layout, wolf.blockPosition().subtract(base), goal.subtract(base));
            if (next != null) step = base.offset(next);
        }
        if (base != null && io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthHalls.isHall(place)) {
            var floor = io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthHalls.floor(place).stream()
                    .filter(pos -> player.level().getBlockState(base.offset(pos)).getCollisionShape(player.level(), base.offset(pos)).isEmpty()
                            && player.level().getBlockState(base.offset(pos).above()).getCollisionShape(player.level(), base.offset(pos).above()).isEmpty())
                    .collect(java.util.stream.Collectors.toSet());
            BlockPos next = nextStep(floor, wolf.blockPosition().subtract(base), goal.subtract(base));
            if (next != null) step = base.offset(next);
        }
        wolf.getNavigation().moveTo(step.getX() + 0.5, step.getY(), step.getZ() + 0.5, 1.15);
    }
    /** Route across the actual carved floor, avoiding fold planes by using their connected ends. */
    @Nullable public static BlockPos nextStep(MazeLayout layout, BlockPos from, BlockPos goal) {
        BlockPos start = new BlockPos(from.getX(), 0, from.getZ());
        if (!layout.floor().contains(start) || !layout.floor().contains(goal)) return null;
        Map<BlockPos, BlockPos> previous = new HashMap<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        previous.put(start, start); queue.add(start);
        while (!queue.isEmpty()) {
            BlockPos pos = queue.removeFirst();
            if (pos.equals(goal)) {
                BlockPos next = pos;
                while (!previous.get(next).equals(start) && !next.equals(start)) next = previous.get(next);
                return next;
            }
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos next = pos.relative(direction);
                if (!layout.floor().contains(next) || previous.containsKey(next)) continue;
                boolean fold = layout.sleeves().stream().anyMatch(s -> MazeLayout.crosses(s,
                        Vec3.atBottomCenterOf(pos), Vec3.atBottomCenterOf(next)));
                if (fold) continue;
                previous.put(next, pos); queue.addLast(next);
            }
        }
        return null;
    }
    @Nullable public static BlockPos nextStep(java.util.Set<BlockPos> floor, BlockPos from, BlockPos goal) {
        BlockPos start = new BlockPos(from.getX(), 0, from.getZ());
        if (!floor.contains(start) || !floor.contains(goal)) return null;
        Map<BlockPos, BlockPos> previous = new HashMap<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        previous.put(start, start); queue.add(start);
        while (!queue.isEmpty()) {
            BlockPos pos = queue.removeFirst();
            if (pos.equals(goal)) {
                BlockPos next = pos;
                while (!previous.get(next).equals(start) && !next.equals(start)) next = previous.get(next);
                return next;
            }
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos next = pos.relative(direction);
                if (!floor.contains(next) || previous.containsKey(next)) continue;
                previous.put(next, pos); queue.addLast(next);
            }
        }
        return null;
    }
    public static Vec3 safeBeside(TamableAnimal wolf, ServerPlayer player) {
        double angle = Math.toRadians(player.getYRot());
        Vec3 forward = new Vec3(-Math.sin(angle), 0, Math.cos(angle));
        Vec3 side = new Vec3(forward.z, 0, -forward.x);
        for (Vec3 offset : new Vec3[]{forward.scale(0.9), side.scale(0.9), side.scale(-0.9), forward.scale(-0.9), Vec3.ZERO}) {
            Vec3 p = player.position().add(offset);
            if (player.level().noCollision(wolf, wolf.getBoundingBox().move(p.subtract(wolf.position())))) return p;
        }
        return player.position();
    }
}
