package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/**
 * 0.4.172 (owner: "add a command to trigger specific film days for testing"): operator-only. Starts a chosen filming day for
 * the reader in the set, as a fresh arrival on that day; "pickup" starts day four at the upstairs pickup, where Marjorie
 * crawls into the cardboard house. Nothing else about the filming changes, and earlier days are not filmed by it.
 */
public final class GhostsCommands {
    private GhostsCommands(){}
    public static LiteralArgumentBuilder<CommandSourceStack> build(){
        return Commands.literal(GhostsSet.PLACE.id()).requires(s->s.hasPermission(2))
            .then(Commands.literal("day").then(Commands.argument("day",IntegerArgumentType.integer(1,GhostsSet.DAYS))
                .executes(c->day(c.getSource(),IntegerArgumentType.getInteger(c,"day"),false))
                .then(Commands.literal("pickup").executes(c->day(c.getSource(),IntegerArgumentType.getInteger(c,"day"),true)))))
            .then(Commands.literal("status").executes(c->{var p=c.getSource().getPlayerOrException();c.getSource().sendSuccess(()->Component.literal(GhostsSet.status(p)),false);return 1;}));
    }
    private static int day(CommandSourceStack source,int day,boolean pickup)throws CommandSyntaxException{
        var p=source.getPlayerOrException();var refused=GhostsSet.jumpToDay(p,day,pickup);
        if(refused!=null){source.sendFailure(Component.literal(refused));return 0;}
        source.sendSuccess(()->Component.literal("Testing: filming day "+day+(pickup?" from the upstairs pickup":"")+" is ready. Earlier days are not filmed by this, so the account cannot be finished from it."),false);
        return 1;
    }
}
