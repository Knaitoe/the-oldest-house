package io.github.knaitoe.theoldesthouse.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.knaitoe.theoldesthouse.house.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/** Operator controls for the two domestic encounters; never restage a story or fabricate possessions. */
public final class HomeTestCommands {
    private HomeTestCommands() {}

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("home").requires(s -> s.hasPermission(2)).executes(c -> help(c.getSource()))
                .then(Commands.literal("status").executes(c -> status(c.getSource())))
                .then(Commands.literal("knocking").executes(c -> knocking(c.getSource()))
                        .then(Commands.literal("start").executes(c -> knocking(c.getSource())))
                        .then(Commands.literal("status").executes(c -> status(c.getSource())))
                        .then(Commands.literal("reset").executes(c -> {
                            HomeKnocking.reset(c.getSource().getPlayerOrException());
                            return say(c.getSource(), "Stopped your home knocking. Use 'knocking start' to replay it.");
                        })))
                .then(Commands.literal("belongings").executes(c -> belongings(c.getSource()))
                        .then(Commands.literal("stage").executes(c -> belongings(c.getSource())))
                        .then(Commands.literal("return").executes(c -> {
                            if (!HomeBelongings.hurry(c.getSource().getPlayerOrException()))
                                return fail(c.getSource(), "Stand inside the main manor with a pending deposit or staged item first.");
                            return say(c.getSource(), "The return is ready. Leave the upstairs reading bedroom unoccupied and out of view, then check its floor.");
                        }))
                        .then(Commands.literal("status").executes(c -> status(c.getSource())))
                        .then(Commands.literal("reset").executes(c -> {
                            if (!HomeBelongings.reset(c.getSource().getPlayerOrException()))
                                return fail(c.getSource(), "Make one inventory slot free, then reset again. Your original is still safely in pending custody.");
                            return say(c.getSource(), "Stopped your return. Undelivered test custody was refunded; cache contents and already returned originals stay where they are.");
                        })));
    }

    private static int help(CommandSourceStack source) {
        say(source, "Run these inside the main manor, rather than a copied home or separate vignette. /ghosthouse test home status shows both personal events.");
        say(source, "/ghosthouse test home knocking starts faint knocks at the parlour/study partition. Wait through the three groups, then crouch close to that wall and listen for an answer from the study's hall wall. 'knocking reset' stops it.");
        return say(source, "/ghosthouse test home belongings stages ONE item from your main hand. After six seconds at home, it can turn up on the upstairs reading-bedroom floor while nobody watches. 'belongings return' skips the wait; 'belongings reset' refunds pending test custody. Ordinary play instead watches items you actually deposit in the explorer's camp barrel, then return home without.");
    }

    private static int knocking(CommandSourceStack source) throws CommandSyntaxException {
        if (!HomeKnocking.start(source.getPlayerOrException()))
            return fail(source, "Enter the main manor first. Its original parlour/study wall must still exist.");
        return say(source, "Home knocking started for you. Listen downstairs near the parlour/study wall; the first group begins in four seconds. Crouch by it after the third group.");
    }

    private static int belongings(CommandSourceStack source) throws CommandSyntaxException {
        if (!HomeBelongings.stageHeld(source.getPlayerOrException()))
            return fail(source, "Stand inside the main manor, close your menu and hold an item in your main hand. Finish or reset an existing pending return first.");
        return say(source, "One held original is in test custody. Stay home for six seconds with the upstairs reading bedroom out of view, then look for it on the floor. Reset refunds it if delivery is still pending.");
    }

    private static int status(CommandSourceStack source) throws CommandSyntaxException {
        var player = source.getPlayerOrException();
        var knocks = HomeKnocking.record(player);
        var belongings = HomeBelongings.record(player);
        say(source, "Home knocking: active=" + knocks.getBoolean("Active") + ", done=" + knocks.getBoolean("Done")
                + ", phase=" + knocks.getInt("Phase") + ", present ticks=" + knocks.getInt("Elapsed")
                + ", heard groups=" + knocks.getInt("Heard") + ", listened=" + knocks.getBoolean("Listened"));
        return say(source, "Home belongings: pending=" + belongings.getBoolean("Pending") + ", done=" + belongings.getBoolean("Done")
                + ", present ticks=" + belongings.getInt("HomeTicks") + ", test custody=" + belongings.getBoolean("TestCustody")
                + ", entries=" + belongings.getList("Entries", net.minecraft.nbt.Tag.TAG_COMPOUND).size());
    }

    private static int say(CommandSourceStack source, String message) {
        source.sendSuccess(() -> Component.literal(message), false); return 1;
    }

    private static int fail(CommandSourceStack source, String message) {
        source.sendFailure(Component.literal(message)); return 0;
    }
}
