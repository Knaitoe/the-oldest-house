package io.github.knaitoe.theoldesthouse.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Developer controls for checking the authored writing styles in game. */
public final class WritingCommands {
    private WritingCommands() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("writing")
                .then(Commands.literal("samples")
                        .executes(context -> giveSamples(
                                context.getSource(),
                                context.getSource().getPlayerOrException()))
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(context -> giveSamples(
                                        context.getSource(),
                                        EntityArgument.getPlayer(context, "target")))));
    }

    private static int giveSamples(CommandSourceStack source, ServerPlayer player) throws CommandSyntaxException {
        int given = 0;
        for (ItemStack sample : HouseWriting.samples()) {
            if (!player.getInventory().add(sample.copy())) {
                player.drop(sample.copy(), false);
            }
            given++;
        }
        int count = given;
        source.sendSuccess(() -> Component.literal(
                "Gave " + player.getGameProfile().getName()
                        + " " + count + " writing samples: Will, Karen, Zampano, and child."),
                false);
        return given;
    }
}
