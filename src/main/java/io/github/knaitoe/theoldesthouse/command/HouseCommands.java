package io.github.knaitoe.theoldesthouse.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.knaitoe.theoldesthouse.house.HouseBuilder;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Optional;

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
                        .then(Commands.literal("eligible")
                                .executes(HouseCommands::markEligible))
                        .then(Commands.literal("ineligible")
                                .executes(HouseCommands::markIneligible))
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
                        .then(Commands.literal("visit")
                                .executes(HouseCommands::incrementVisit))
                        .then(Commands.literal("reset")
                                .executes(HouseCommands::reset))
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
        BlockPos projectedCenter = player.blockPosition().relative(facing, 24);

        int floorY = level.getHeight(
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                projectedCenter.getX(),
                projectedCenter.getZ()
        );

        BlockPos origin = new BlockPos(
                projectedCenter.getX() - HouseBuilder.WIDTH / 2,
                floorY,
                projectedCenter.getZ() - HouseBuilder.DEPTH / 2
        );

        HouseBuilder.build(level, origin);
        data.markSpawned(origin);

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
        HouseSavedData data = HouseSavedData.get(source.getServer());

        String housePosition = formatPosition(data.housePosition());
        String anchorPosition = formatPosition(data.anchorPosition());

        source.sendSuccess(
                () -> Component.literal(
                        "The Oldest House state | settlementNights=" + data.settlementNights() +
                                ", anchor=" + anchorPosition +
                                ", eligible=" + data.isEligible() +
                                ", eligibleSinceDay=" + data.eligibleSinceDay() +
                                ", spawnChance=" + data.spawnChancePercent() + "%" +
                                ", spawned=" + data.isSpawned() +
                                ", houseOrigin=" + housePosition +
                                ", age=" + data.houseAge() +
                                ", visits=" + data.visitCount()
                ),
                false
        );
        return 1;
    }

    private static int markEligible(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context)
            throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        HouseSavedData data = HouseSavedData.get(source.getServer());

        long currentDay = source.getServer().overworld().getDayTime() / 24000L;
        data.markEligible(player.blockPosition(), currentDay);

        source.sendSuccess(
                () -> Component.literal(
                        "Debug override: The Oldest House eligibility enabled at the player's current location. Spawn chance reset to 5%."
                ),
                true
        );
        return 1;
    }

    private static int markIneligible(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        HouseSavedData data = HouseSavedData.get(source.getServer());
        data.markIneligible();

        source.sendSuccess(
                () -> Component.literal("The Oldest House eligibility disabled."),
                true
        );
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

    private static int reset(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        HouseSavedData data = HouseSavedData.get(source.getServer());
        data.reset();

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
