package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseImpossibleHallway;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.network.DoorLeaksPayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class LabyrinthDoorLeaks {
    private LabyrinthDoorLeaks() {}
    public record Cue(DoorLeakKind kind, boolean bark) {}

    @Nullable
    public static Cue cue(LabyrinthData data, UUID player, LabyrinthData.Door door) {
        LabyrinthData.Deal deal = LabyrinthData.DEALT.equals(door.destination) ? data.deal(player, door) : null;
        boolean leaks = deal != null ? deal.leak() : door.leak;
        boolean bark = deal != null ? deal.bark() : door.bark;
        if (!leaks) return null;
        String destination = deal != null ? deal.place() : door.destination.startsWith("place:") ? door.destination.substring(6) : door.dealt;
        if (destination == null) return null;
        LabyrinthPlace place = LabyrinthPlace.byId(destination);
        if(place==LabyrinthPlace.FLOORBOARDS&&data.isCompleted(place.id()))return null;
        int salt = door.id.hashCode() ^ player.hashCode() ^ destination.hashCode();
        return new Cue(DoorLeakKind.forDestination(place, salt), bark);
    }
    public static void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer().getTickCount() % 20 != 0) return;
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) send(player);
    }
    public static void send(ServerPlayer player) {
        LabyrinthData data = LabyrinthData.get(player.server);
        List<DoorLeaksPayload.Leak> hints = new ArrayList<>();
        for (LabyrinthData.Door door : data.doors()) {
            if (!door.dimension.equals(player.level().dimension()) || player.position().distanceToSqr(Vec3.atCenterOf(door.lower)) > 144) continue;
            Cue cue = cue(data, player.getUUID(), door);
            if (cue == null) continue;
            BlockState state = player.level().getBlockState(door.lower);
            if (!(state.getBlock() instanceof DoorBlock) || state.getValue(DoorBlock.OPEN)) continue;
            hints.add(new DoorLeaksPayload.Leak(door.lower, door.facing.get3DDataValue(), cue.kind.ordinal()));
            if (player.server.getTickCount() % 80 == 0 && player.position().distanceToSqr(Vec3.atCenterOf(door.lower)) <= 36) {
                sound(player, door, cue);
            }
            if (hints.size() >= 32) break;
        }
        BlockPos origin = HouseSavedData.get(player.server).houseOrigin();
        boolean inside = origin != null && player.level().dimension().equals(HouseDimensions.INTERIOR)
                && (FinaleArchitecture.contains(origin, player.blockPosition()) || LabyrinthPlaces.isInStack(origin, player.blockPosition())
                    || HouseImpossibleHallway.isInsideWalkableVolume(origin, player.getX(), player.getY(), player.getZ()));
        HousePackets.send(player, new DoorLeaksPayload(player.level().dimension().location(), inside, hints));
    }
    private static void sound(ServerPlayer player, LabyrinthData.Door door, Cue cue) {
        Vec3 at = Vec3.atCenterOf(door.lower.relative(door.facing.getOpposite()));
        Holder<SoundEvent> sound;
        float volume = .25F, pitch = .85F;
        if (cue.bark) {
            sound = Holder.direct(player.getRandom().nextBoolean() ? SoundEvents.WOLF_AMBIENT : SoundEvents.WOLF_WHINE);
        } else {
            sound = switch (cue.kind) {
                case HEARTBEAT -> Holder.direct(LabyrinthRegistry.FLOORBOARD_HEARTBEAT.get());
                case WATER -> Holder.direct(SoundEvents.WATER_AMBIENT);
                case LAKE -> Holder.direct(DrownedTownRegistry.LAKE_LEAK.get());
                case MOTHER -> Holder.direct(SoundEvents.CAT_PURR);
                case CLOTH -> custom("vignette.clap_muffled");
                case WARM_TV -> custom("leak.television");
                case PHONE -> custom("leak.phone");
                case HOTEL -> custom("leak.hotel_music");
            };
            if (cue.kind == DoorLeakKind.PHONE) pitch = 1;
        }
        player.connection.send(new ClientboundSoundPacket(sound, SoundSource.BLOCKS, at.x, at.y, at.z,
                volume, pitch, player.getRandom().nextLong()));
    }
    private static Holder<SoundEvent> custom(String path) {
        return Holder.direct(SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, path)));
    }
}
