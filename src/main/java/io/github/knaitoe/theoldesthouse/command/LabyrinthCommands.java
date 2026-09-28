package io.github.knaitoe.theoldesthouse.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthBuilder;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthData;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthDoors;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthPlace;
import io.github.knaitoe.theoldesthouse.labyrinth.TellTaleFloorboards;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Test controls for the labyrinth and its vignettes:
 * {@code /oldesthouse door}, {@code /oldesthouse labyrinth} and
 * {@code /oldesthouse vignette}.
 */
public final class LabyrinthCommands {
    private LabyrinthCommands() {
    }

    /** {@code door <place|dealt>} places a test door; {@code door remove} takes the nearest one away. */
    public static LiteralArgumentBuilder<CommandSourceStack> door() {
        LiteralArgumentBuilder<CommandSourceStack> door = Commands.literal("door");
        for (LabyrinthPlace place : LabyrinthPlace.values()) {
            if (place.base() != null) {
                door.then(Commands.literal(place.id()).executes(context -> placeDoor(context.getSource(), place.id())));
            }
        }
        door.then(Commands.literal(LabyrinthData.DEALT).executes(context -> placeDoor(context.getSource(), LabyrinthData.DEALT)));
        door.then(Commands.literal("remove").executes(context -> removeDoor(context.getSource())));
        return door;
    }

    public static LiteralArgumentBuilder<CommandSourceStack> labyrinth() {
        return Commands.literal("labyrinth")
                .then(Commands.literal("go").executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    if (!LabyrinthDoors.goToJunction(player)) {
                        context.getSource().sendFailure(Component.literal("Could not reach the labyrinth."));
                        return 0;
                    }
                    return 1;
                }))
                .then(Commands.literal("build").executes(context -> {
                    boolean built = LabyrinthBuilder.buildAll(context.getSource().getServer());
                    context.getSource().sendSuccess(() -> Component.literal(built
                            ? "Carved every labyrinth place again (and put the floorboards back if they are unfinished)."
                            : "The labyrinth dimension is missing."), true);
                    return built ? 1 : 0;
                }))
                .then(Commands.literal("status").executes(context -> {
                    ServerPlayer viewer = context.getSource().getEntity() instanceof ServerPlayer p ? p : null;
                    for (String line : LabyrinthDoors.describe(context.getSource().getServer(), viewer)) {
                        context.getSource().sendSuccess(() -> Component.literal(line), false);
                    }
                    return 1;
                }));
    }

    public static LiteralArgumentBuilder<CommandSourceStack> vignette() {
        return Commands.literal("vignette")
                .then(Commands.literal(TellTaleFloorboards.ID)
                        .then(Commands.literal("reset").executes(context -> {
                            if (!LabyrinthBuilder.ensureBuilt(context.getSource().getServer())
                                    || !TellTaleFloorboards.reset(context.getSource().getServer())) {
                                context.getSource().sendFailure(Component.literal("The labyrinth dimension is missing."));
                                return 0;
                            }
                            context.getSource().sendSuccess(() -> Component.literal(
                                    "The floorboards are as they were: board down, heartbeat going, dealable again."), true);
                            return 1;
                        }))
                        .then(Commands.literal("complete").executes(context -> {
                            LabyrinthData.get(context.getSource().getServer()).setCompleted(TellTaleFloorboards.ID, true);
                            context.getSource().sendSuccess(() -> Component.literal(
                                    "The floorboards are marked finished; the dealer will not deal them."), true);
                            return 1;
                        })));
    }

    private static int placeDoor(CommandSourceStack source, String destination) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        if (!LabyrinthBuilder.ensureBuilt(source.getServer())) {
            source.sendFailure(Component.literal("The labyrinth dimension is missing."));
            return 0;
        }
        String error = LabyrinthDoors.placeCommandDoor(player, destination);
        if (error != null) {
            source.sendFailure(Component.literal(error));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("A door to " + destination
                + " stands in front of you. Open it to go through; the door you arrive at leads back here."), true);
        return 1;
    }

    private static int removeDoor(CommandSourceStack source) throws CommandSyntaxException {
        String removed = LabyrinthDoors.removeCommandDoor(source.getPlayerOrException());
        if (removed == null) {
            source.sendFailure(Component.literal("No test door within eight blocks."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Removed test door " + removed + "."), true);
        return 1;
    }
}
