package io.github.knaitoe.theoldesthouse.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import io.github.knaitoe.theoldesthouse.house.HouseMarginalia;
import io.github.knaitoe.theoldesthouse.house.HouseBlocks;
import io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock;
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
                .then(Commands.literal("notes").executes(context -> giveNotes(context.getSource(),context.getSource().getPlayerOrException())))
                .then(Commands.literal("correspondence").then(Commands.argument("id",StringArgumentType.word())
                        .suggests((context,builder)->net.minecraft.commands.SharedSuggestionProvider.suggest(
                                io.github.knaitoe.theoldesthouse.house.CorrespondenceTexts.all().stream().map(n->n.id()),builder))
                        .executes(context->giveCorrespondence(context.getSource(),StringArgumentType.getString(context,"id")))))
                .then(Commands.literal("furniture").executes(context -> giveFurniture(context.getSource(),context.getSource().getPlayerOrException())))
                .then(Commands.literal("samples")
                        .executes(context -> giveSamples(
                                context.getSource(),
                                context.getSource().getPlayerOrException()))
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(context -> giveSamples(
                                        context.getSource(),
                                        EntityArgument.getPlayer(context, "target")))));
    }
    private static void give(ServerPlayer player,ItemStack stack) { if(!player.getInventory().add(stack)) player.drop(stack,false); }
    private static int giveNotes(CommandSourceStack source,ServerPlayer player) {
        var samples=HouseMarginalia.samples(player); for(var book:samples) give(player,book);
        source.sendSuccess(()->Component.literal("Gave 17 serial-note and poem previews. Reading progress is unchanged."),false);return samples.size();
    }
    private static int giveCorrespondence(CommandSourceStack source,String id) throws CommandSyntaxException {
        var player=source.getPlayerOrException();var book=io.github.knaitoe.theoldesthouse.house.HouseCorrespondence.preview(player,id);
        if(book.isEmpty()){source.sendFailure(Component.literal("Unknown correspondence ID."));return 0;}
        give(player,book);source.sendSuccess(()->Component.literal("Gave one correspondence specimen. Reading progress is unchanged."),false);return 1;
    }
    private static int giveFurniture(CommandSourceStack source,ServerPlayer player) {
        for(var kind:HouseholdFurnitureBlock.Kind.values()) {
            ItemStack piece=new ItemStack(HouseBlocks.FURNITURE_ITEM.get());
            piece.set(net.minecraft.core.component.DataComponents.BLOCK_STATE,new net.minecraft.world.item.component.BlockItemStateProperties(java.util.Map.of("kind",kind.getSerializedName())));
            piece.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,Component.literal(kind.getSerializedName().replace('_',' '))); give(player,piece);
        }
        source.sendSuccess(()->Component.literal("Gave twelve placeable furniture samples."),false);return HouseholdFurnitureBlock.Kind.values().length;
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
                        + " " + count + " writing samples: Will, Karen, Zampano, child, Pelafina, and claw."),
                false);
        return given;
    }
}
