package io.github.knaitoe.theoldesthouse.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.knaitoe.theoldesthouse.journal.TornPages;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;

/** Moderation for readers' journals (JOURNAL_DESIGN.md): every page a reader has torn out goes back into its stub, wherever it is. */
public final class JournalCommands {
    private JournalCommands(){}
    public static LiteralArgumentBuilder<CommandSourceStack> build(){
        return Commands.literal("journal")
                .then(Commands.literal("clearpages").then(Commands.argument("player",EntityArgument.player()).executes(context->{
                    var reader=EntityArgument.getPlayer(context,"player");int n=TornPages.clear(context.getSource().getServer(),reader.getUUID());
                    context.getSource().sendSuccess(()->Component.literal(n+" torn page"+(n==1?"":"s")+" of "+reader.getGameProfile().getName()+"'s went back into their journal."),true);
                    return n;
                })));
    }
}
