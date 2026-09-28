package io.github.knaitoe.theoldesthouse.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.knaitoe.theoldesthouse.opening.Doorsteps;
import io.github.knaitoe.theoldesthouse.opening.NavidsonLetter;
import io.github.knaitoe.theoldesthouse.opening.NavidsonPhoto;
import io.github.knaitoe.theoldesthouse.opening.OpeningPlayerState;
import io.github.knaitoe.theoldesthouse.opening.OpeningSequence;
import io.github.knaitoe.theoldesthouse.opening.OpeningStage;
import io.github.knaitoe.theoldesthouse.opening.OpeningWorldData;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Debug controls for the opening sequence, under {@code /oldesthouse opening}.
 * Each takes an optional target player (defaults to the caller).
 */
public final class OpeningCommands {
    private interface PlayerAction {
        int run(CommandSourceStack source, ServerPlayer player) throws CommandSyntaxException;
    }

    private OpeningCommands() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("opening")
                .then(withTarget("status", OpeningCommands::status))
                .then(withTarget("advance", OpeningCommands::advance))
                .then(withTarget("eligible", OpeningCommands::eligible))
                .then(withTarget("letter", OpeningCommands::letter))
                .then(withTarget("photo", OpeningCommands::photo))
                .then(withTarget("copy", OpeningCommands::visitCopy))
                .then(withTarget("hillary", OpeningCommands::hillary))
                .then(withTarget("reset", OpeningCommands::reset));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> withTarget(String name, PlayerAction action) {
        return Commands.literal(name)
                .executes(context -> action.run(context.getSource(), context.getSource().getPlayerOrException()))
                .then(Commands.argument("target", EntityArgument.player())
                        .executes(context -> action.run(context.getSource(), target(context))));
    }

    private static ServerPlayer target(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return EntityArgument.getPlayer(context, "target");
    }

    private static int status(CommandSourceStack source, ServerPlayer player) {
        OpeningPlayerState state = OpeningSequence.state(player);
        long day = OpeningSequence.currentDay(source.getServer());
        Optional<BlockPos> bed = Doorsteps.bedPosition(player);

        String text = player.getGameProfile().getName() + " | stage=" + state.stage().name().toLowerCase()
                + ", day=" + day
                + ", firstJoinDay=" + state.firstJoinDay()
                + ", nightsSlept=" + state.nightsSlept()
                + ", bed=" + bed.map(OpeningCommands::format).orElse("none")
                + ", eligibleDay=" + state.eligibleDay()
                + ", letterDay=" + state.letterDay()
                + ", lastMorning=" + state.lastMorningDay()
                + ", trackedDoors=" + state.doorUse().size()
                + ", hillary=" + (state.hillaryUuid() == null ? "none" : state.hillaryUuid())
                + ", enteredHouse=" + state.enteredHouse()
                + photoSummary(OpeningWorldData.get(source.getServer()).photoOf(player.getUUID()));
        source.sendSuccess(() -> Component.literal(text), false);
        return 1;
    }

    private static String photoSummary(@Nullable OpeningWorldData.PhotoRecord photo) {
        if (photo == null) {
            return ", photo=none";
        }
        return ", photo=copy at " + format(photo.copyMin()) + " in " + photo.dimension()
                + (photo.window() == null ? "" : (photo.windowCarved() ? ", window cut at " : ", window lit at ") + format(photo.window()));
    }

    /** The next step, as the next morning would run it. */
    private static int advance(CommandSourceStack source, ServerPlayer player) {
        String result = OpeningSequence.advance(player);
        source.sendSuccess(() -> Component.literal(player.getGameProfile().getName() + ": " + result + "."), true);
        return 1;
    }

    /** Puts you where Navidson's camera stood, looking at the altered copy of the player's house. */
    private static int visitCopy(CommandSourceStack source, ServerPlayer player) throws CommandSyntaxException {
        OpeningWorldData.PhotoRecord photo = OpeningWorldData.get(source.getServer()).photoOf(player.getUUID());
        if (photo == null) {
            source.sendFailure(Component.literal("No photo has been taken of " + player.getGameProfile().getName()
                    + "'s house yet; try /oldesthouse opening photo."));
            return 0;
        }
        ServerLevel level = source.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, photo.dimension()));
        if (level == null) {
            source.sendFailure(Component.literal("The copy's dimension " + photo.dimension() + " is not loaded."));
            return 0;
        }
        ServerPlayer caller = source.getPlayerOrException();
        caller.teleportTo(level, photo.camera().x, photo.camera().y, photo.camera().z, photo.yaw(), photo.pitch());
        source.sendSuccess(() -> Component.literal("Standing where Navidson's camera stood, in " + photo.dimension() + "."), false);
        return 1;
    }

    /** A new Hillary on the doorstep, replacing any earlier one. */
    private static int hillary(CommandSourceStack source, ServerPlayer player) {
        BlockPos at = OpeningSequence.respawnHillary(player);
        if (at == null) {
            source.sendFailure(Component.literal("No bed or doorstep for Hillary near " + player.getGameProfile().getName() + "."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Hillary is on " + player.getGameProfile().getName() + "'s doorstep at " + format(at) + "."), true);
        return 1;
    }

    /** Skips the settling-in requirements: the letter arrives on the next morning check. */
    private static int eligible(CommandSourceStack source, ServerPlayer player) {
        OpeningPlayerState state = OpeningSequence.state(player);
        state.setStageForTesting(OpeningStage.ELIGIBLE, OpeningSequence.currentDay(source.getServer()) - 1L);
        source.sendSuccess(() -> Component.literal(player.getGameProfile().getName()
                + " is eligible; the letter arrives on the next morning (time 0-1000)."), true);
        return 1;
    }

    private static int letter(CommandSourceStack source, ServerPlayer player) {
        Optional<BlockPos> bed = Doorsteps.bedPosition(player);
        if (bed.isEmpty()) {
            source.sendFailure(Component.literal(player.getGameProfile().getName() + " has no bed respawn point."));
            return 0;
        }
        long day = OpeningSequence.currentDay(source.getServer());
        NavidsonPhoto.Result photo = takePhoto(source, player, bed.get());
        if (!OpeningSequence.deliverLetter(player, OpeningSequence.state(player), bed.get(), day, photo == null ? null : photo.pixels())) {
            source.sendFailure(Component.literal("No doorstep near the bed to leave the letter on."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Navidson's letter delivered to " + player.getGameProfile().getName() + "."), true);
        return 1;
    }

    /** Photographs the player's house again and hands the snapshot to whoever ran the command. */
    private static int photo(CommandSourceStack source, ServerPlayer player) throws CommandSyntaxException {
        Optional<BlockPos> bed = Doorsteps.bedPosition(player);
        if (bed.isEmpty()) {
            source.sendFailure(Component.literal(player.getGameProfile().getName() + " has no bed respawn point."));
            return 0;
        }
        NavidsonPhoto.Result photo = takePhoto(source, player, bed.get());
        ServerPlayer caller = source.getPlayerOrException();
        ItemStack snapshot = NavidsonLetter.createSnapshot(source.getServer().overworld(), photo == null ? null : photo.pixels());
        if (!caller.getInventory().add(snapshot)) {
            caller.drop(snapshot, false);
        }
        return photo != null && photo.pixels() != null ? 1 : 0;
    }

    @Nullable
    private static NavidsonPhoto.Result takePhoto(CommandSourceStack source, ServerPlayer player, BlockPos bed) {
        BlockPos porch = OpeningSequence.navidsonPorch(source.getServer(), bed);
        NavidsonPhoto.Result photo = NavidsonPhoto.takeNow(source.getServer(), player.getUUID(), bed, porch);
        if (photo == null || photo.pixels() == null) {
            source.sendFailure(Component.literal("The House could not photograph the house around " + format(bed)
                    + "; the stock print is used."));
            return photo;
        }
        String window = photo.window() == null ? "no window in view"
                : (photo.windowCarved() ? "carved a lit window at " : "lit the window at ") + format(photo.window());
        source.sendSuccess(() -> Component.literal("Photographed the copy in the_oldest_house:outside from "
                + format(photo.copyMin()) + " (" + window + ")."), false);
        return photo;
    }

    /** Morning two now: Hillary on the doorstep and the door placed at once (even in view). */
    /** Clears this player's opening progression. */
    private static int reset(CommandSourceStack source, ServerPlayer player) {
        OpeningSequence.state(player).reset();
        source.sendSuccess(() -> Component.literal(
                "Opening sequence reset for " + player.getGameProfile().getName() + "."
        ), true);
        return 1;
    }

    private static String format(BlockPos pos) {
        return pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }
}
