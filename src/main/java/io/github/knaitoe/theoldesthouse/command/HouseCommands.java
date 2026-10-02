package io.github.knaitoe.theoldesthouse.command;

import io.github.knaitoe.theoldesthouse.network.HousePackets;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.knaitoe.theoldesthouse.house.HouseBetweenRoom;
import io.github.knaitoe.theoldesthouse.house.HouseBuilder;
import io.github.knaitoe.theoldesthouse.house.HouseCalendar;
import io.github.knaitoe.theoldesthouse.house.HouseDays;
import io.github.knaitoe.theoldesthouse.house.HouseDimensionMirror;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseInteriorInitializer;
import io.github.knaitoe.theoldesthouse.house.HouseLayout;
import io.github.knaitoe.theoldesthouse.house.HouseMemory;
import io.github.knaitoe.theoldesthouse.house.HouseMirrorSyncEvents;
import io.github.knaitoe.theoldesthouse.house.HouseProgression;
import io.github.knaitoe.theoldesthouse.house.HouseShifts;
import io.github.knaitoe.theoldesthouse.house.HouseTransitionEvents;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthData;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthDoors;
import io.github.knaitoe.theoldesthouse.network.HouseSightlineStatePayload;
import io.github.knaitoe.theoldesthouse.opening.NavidsonPhoto;
import io.github.knaitoe.theoldesthouse.opening.OpeningSequence;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

public final class HouseCommands {
    private HouseCommands() {
    }

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("oldesthouse")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("spawn")
                                .executes(HouseCommands::spawn))
                        .then(Commands.literal("status")
                                .executes(HouseCommands::status))
                        .then(Commands.literal("age")
                                .then(Commands.argument("days", IntegerArgumentType.integer(0))
                                        .executes(context -> setAge(
                                                context.getSource(),
                                                IntegerArgumentType.getInteger(context, "days")
                                        ))))
                        .then(Commands.literal("advance")
                                .executes(context -> advanceAge(context.getSource(), 1))
                                .then(Commands.argument("days", IntegerArgumentType.integer(1))
                                        .executes(context -> advanceAge(
                                                context.getSource(),
                                                IntegerArgumentType.getInteger(context, "days")
                                        ))))
                        .then(Commands.literal("day")
                                .executes(context -> advanceDays(context.getSource(), 1))
                                .then(Commands.argument("days", IntegerArgumentType.integer(1, 60))
                                        .executes(context -> advanceDays(
                                                context.getSource(),
                                                IntegerArgumentType.getInteger(context, "days")
                                        ))))
                        .then(Commands.literal("reveal")
                                .then(Commands.literal("rugs")
                                        .executes(context -> reveal(context.getSource(), "rugs")))
                                .then(Commands.literal("room")
                                        .executes(context -> reveal(context.getSource(), "room")))
                                .then(Commands.literal("hallway")
                                        .executes(context -> reveal(context.getSource(), "hallway"))))
                        .then(shiftCommand())
                        .then(LabyrinthCommands.door())
                        .then(LabyrinthCommands.labyrinth())
                        .then(LabyrinthCommands.vignette())
                        .then(LabyrinthCommands.growl())
                        .then(io.github.knaitoe.theoldesthouse.labyrinth.FinaleCommands.build())
                        .then(Commands.literal("restock")
                                .executes(context -> restock(context.getSource())))
                        .then(Commands.literal("visit")
                                .executes(HouseCommands::incrementVisit))
                        .then(Commands.literal("reconcile")
                                .executes(HouseCommands::reconcileMirror))
                        .then(Commands.literal("reset")
                                .executes(HouseCommands::reset))
                        .then(WritingCommands.build())
                        .then(OpeningCommands.build())
        );
    }

    private static int spawn(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context)
            throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = source.getLevel();
        HouseSavedData data = HouseSavedData.get(source.getServer());

        if (data.isSpawned()) {
            source.sendFailure(Component.literal(
                    "The Oldest House is already marked as spawned. Use /oldesthouse reset before another test spawn."
            ));
            return 0;
        }

        Direction facing = player.getDirection();
        BlockPos projectedCenter = player.blockPosition().relative(facing, 36);

        int floorY = level.getHeight(
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                projectedCenter.getX(),
                projectedCenter.getZ()
        );

        BlockPos origin = new BlockPos(
                projectedCenter.getX() - HouseLayout.CENTER_X,
                floorY,
                projectedCenter.getZ() - HouseLayout.CENTER_Z
        );

        HouseBuilder.build(level, origin);
        data.markSpawned(origin);
        LabyrinthDoors.syncSealedDoors(source.getServer());

        source.sendSuccess(
                () -> Component.literal(
                        "Spawned prototype of The Oldest House at " +
                                origin.getX() + ", " + origin.getY() + ", " + origin.getZ() +
                                " (north-west floor corner)."
                ),
                true
        );
        return 1;
    }

    private static int status(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        MinecraftServer server = source.getServer();
        HouseSavedData data = HouseSavedData.get(server);

        String housePosition = formatPosition(data.housePosition());
        List<String> lines = new ArrayList<>();
        lines.add("The Oldest House state | spawned=" + data.isSpawned() +
                ", houseOrigin=" + housePosition +
                ", age=" + data.houseAge() +
                ", impossibleDoor=" + data.isImpossibleDoorRevealed() +
                ", interiorInitialized=" + data.isInteriorInitialized() +
                ", layout=v" + data.layoutVersion() +
                (data.isOutdated()
                        ? " (outdated: the House has stood down; reset and respawn for v" + HouseLayout.LAYOUT_VERSION + ")"
                        : "") +
                ", visits=" + data.visitCount() +
                ", sleptInManor=" + data.sleptInManor());
        lines.add("Day " + HouseCalendar.today(server) + " as the mod counts them (time of day "
                + HouseCalendar.timeOfDay(server) + "). Mornings come from waking after a night's sleep, or /oldesthouse day.");
        if (!data.isSpawned()) {
            lines.add("The House appears through the opening sequence (see /oldesthouse opening status).");
        } else if (data.visitCount() <= 0) {
            lines.add("Perceived age stays at " + data.houseAge() + " until somebody enters the manor.");
        }
        lines.addAll(HouseProgression.describe(data));
        lines.addAll(LabyrinthDoors.describe(server, source.getEntity() instanceof ServerPlayer viewer ? viewer : null));
        if (source.getEntity() instanceof ServerPlayer player) {
            lines.add(OpeningSequence.describeProgress(player));
        }

        for (String line : lines) {
            source.sendSuccess(() -> Component.literal(line), false);
        }
        return 1;
    }

    /**
     * Passes whole nights, as the mod sees them: the clock jumps to the next
     * dawn and every online player counts as having slept (in the manor if
     * they are standing in it), then each morning runs exactly as a real one.
     */
    private static int advanceDays(CommandSourceStack source, int days) {
        MinecraftServer server = source.getServer();
        ServerLevel overworld = server.overworld();
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        int passed = 0;

        for (int i = 0; i < days; i++) {
            long dawn = (Math.floorDiv(overworld.getDayTime(), HouseCalendar.TICKS_PER_DAY) + 1L) * HouseCalendar.TICKS_PER_DAY;
            overworld.setDayTime(dawn);
            long day = HouseCalendar.today(server);
            passed++;

            boolean anyInManor = false;
            for (ServerPlayer player : players) {
                OpeningSequence.state(player).recordSleep(day);
                anyInManor |= HouseDays.isInManor(player);
            }
            List<String> changes = new ArrayList<>(HouseProgression.onMorningWake(server, day, anyInManor));
            for (ServerPlayer player : players) {
                String before = OpeningSequence.state(player).stage().name();
                OpeningSequence.checkMorning(player, OpeningSequence.state(player), day);
                String after = OpeningSequence.state(player).stage().name();
                if (!before.equals(after)) {
                    changes.add(player.getGameProfile().getName() + "'s opening: "
                            + before.toLowerCase() + " -> " + after.toLowerCase());
                }
            }

            HouseSavedData data = HouseSavedData.get(server);
            String summary = "Day " + day + (anyInManor ? " (slept in the manor)" : "")
                    + ": House age " + data.houseAge()
                    + (changes.isEmpty() ? "." : "; " + String.join("; ", changes) + ".");
            source.sendSuccess(() -> Component.literal(summary), true);

            boolean developing = false;
            for (ServerPlayer player : players) {
                developing |= NavidsonPhoto.isRunning(player.getUUID());
            }
            if (developing && i < days - 1) {
                source.sendSuccess(() -> Component.literal(
                        "Navidson is photographing the house; stopping here so the letter can arrive first."), false);
                break;
            }
        }
        return passed;
    }

    /**
     * Gives every empty, untouched container in the manor its room's loot
     * table (and books to empty lecterns and shelves), for houses built
     * before the room tables existed.
     */
    private static int restock(CommandSourceStack source) {
        HouseSavedData data = HouseSavedData.get(source.getServer());
        BlockPos origin = data.houseOrigin();
        ServerLevel interior = source.getServer().getLevel(HouseDimensions.INTERIOR);
        if (origin == null || interior == null || !data.isInteriorInitialized()) {
            source.sendFailure(Component.literal("The House interior is not available yet."));
            return 0;
        }
        HouseBuilder.applyInteriorContents(interior, origin);
        source.sendSuccess(() -> Component.literal(
                "Empty containers in the manor will fill from their room's loot table when opened."), true);
        return 1;
    }

    /** {@code /oldesthouse shift [kind]}: one subtle change now, picked by weight or named. */
    private static LiteralArgumentBuilder<CommandSourceStack> shiftCommand() {
        LiteralArgumentBuilder<CommandSourceStack> shift = Commands.literal("shift")
                .executes(context -> shift(context.getSource(), null));
        for (HouseShifts.Shift kind : HouseShifts.Shift.values()) {
            shift.then(Commands.literal(kind.id()).executes(context -> shift(context.getSource(), kind)));
        }
        return shift;
    }

    private static int shift(CommandSourceStack source, @Nullable HouseShifts.Shift kind) {
        HouseSavedData data = HouseSavedData.get(source.getServer());
        if (!data.isSpawned()) {
            source.sendFailure(Component.literal("The Oldest House has not spawned yet."));
            return 0;
        }
        String change = HouseShifts.trigger(source.getServer(), data, kind);
        if (change == null) {
            source.sendFailure(Component.literal(kind == null
                    ? "Nothing in the house can change right now (everything may be in view)."
                    : kind.id() + " cannot happen right now: it may be in view, used up, or have nothing to change."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("The house changed: " + change + ". Subtle changes so far: "
                + data.shiftsTriggered() + "."), true);
        return 1;
    }

    private static int reveal(CommandSourceStack source, String what) {
        String error = HouseProgression.reveal(source.getServer(), what);
        if (error != null) {
            source.sendFailure(Component.literal(error));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Revealed: " + what + "."), true);
        return 1;
    }

    private static int setAge(CommandSourceStack source, int days) {
        HouseSavedData data = HouseSavedData.get(source.getServer());
        data.setHouseAge(days);

        source.sendSuccess(
                () -> Component.literal("The Oldest House age set to " + days + " day(s)."),
                true
        );
        return 1;
    }

    private static int advanceAge(CommandSourceStack source, int days) {
        HouseSavedData data = HouseSavedData.get(source.getServer());

        if (!data.isSpawned()) {
            source.sendFailure(Component.literal(
                    "The Oldest House has not spawned yet. Spawn it before advancing its perceived age."
            ));
            return 0;
        }

        int newAge = data.advanceHouseAge(days);
        source.sendSuccess(
                () -> Component.literal(
                        "Advanced The Oldest House by " + days + " day(s). Perceived age is now " + newAge + " day(s)."
                ),
                true
        );
        return newAge;
    }

    private static int incrementVisit(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        HouseSavedData data = HouseSavedData.get(source.getServer());
        data.incrementVisitCount();

        source.sendSuccess(
                () -> Component.literal("The Oldest House visit count is now " + data.visitCount() + "."),
                true
        );
        return 1;
    }

    private static int reconcileMirror(
            com.mojang.brigadier.context.CommandContext<CommandSourceStack> context
    ) {
        CommandSourceStack source = context.getSource();
        HouseSavedData data = HouseSavedData.get(source.getServer());
        BlockPos origin = data.houseOrigin();
        ServerLevel interior = source.getServer().getLevel(HouseDimensions.INTERIOR);

        if (!data.isSpawned() || origin == null || interior == null) {
            source.sendFailure(Component.literal(
                    "The Oldest House interior is not available to reconcile."
            ));
            return 0;
        }

        int changed = HouseDimensionMirror.reconcileAuthoritativeDomestic(
                interior,
                source.getServer().overworld(),
                origin
        );

        source.sendSuccess(
                () -> Component.literal(
                        "Reconciled the authoritative House interior into the Overworld proxy; "
                                + changed + " shared position(s) changed."
                ),
                false
        );
        return 1;
    }

    private static int reset(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        HouseSavedData data = HouseSavedData.get(source.getServer());
        for(var player:source.getServer().getPlayerList().getPlayers())io.github.knaitoe.theoldesthouse.labyrinth.PhoneCanoe.interrupt(player);
        data.reset();
        LabyrinthData.get(source.getServer()).setState(io.github.knaitoe.theoldesthouse.labyrinth.PhoneCanoe.ID,new net.minecraft.nbt.CompoundTag());
        LabyrinthData.get(source.getServer()).setState(io.github.knaitoe.theoldesthouse.labyrinth.FinaleProgress.STATE, new net.minecraft.nbt.CompoundTag());
        LabyrinthData.get(source.getServer()).setState("finale_architecture_049", new net.minecraft.nbt.CompoundTag());
        LabyrinthData.get(source.getServer()).setState(io.github.knaitoe.theoldesthouse.labyrinth.WitnessAccount.STATE, new net.minecraft.nbt.CompoundTag());
        LabyrinthData.get(source.getServer()).setState(io.github.knaitoe.theoldesthouse.labyrinth.GrowlChanges.STATE, new net.minecraft.nbt.CompoundTag());
        io.github.knaitoe.theoldesthouse.labyrinth.CaverVignette.clearAll();
        io.github.knaitoe.theoldesthouse.labyrinth.HollowayVignette.clearAll();
        io.github.knaitoe.theoldesthouse.labyrinth.NovelVignettes.clearAll();
        io.github.knaitoe.theoldesthouse.labyrinth.FinaleController.clearAll();
        HouseShifts.refreshCache(data);
        HouseMemory.get(source.getServer()).clear();
        HouseMirrorSyncEvents.clearPending();
        HouseInteriorInitializer.cancel();
        HouseTransitionEvents.clearAll();
        HouseBetweenRoom.clearAll();
        LabyrinthData.get(source.getServer()).removeDoor("hallway_end");
        LabyrinthDoors.syncSealedDoors(source.getServer());
        HousePackets.sendToAll(source.getServer(), new HouseSightlineStatePayload(BlockPos.ZERO, false));
        HousePackets.sendToAll(source.getServer(), HouseBetweenRoom.doorPayload(data));

        source.sendSuccess(
                () -> Component.literal(
                        "Persistent state for The Oldest House reset. Existing prototype blocks were left untouched."
                ),
                true
        );
        return 1;
    }

    private static String formatPosition(Optional<BlockPos> position) {
        if (position.isEmpty()) {
            return "unset";
        }

        BlockPos pos = position.get();
        return pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }
}
