package io.github.knaitoe.theoldesthouse.command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.commands.*;
import net.minecraft.network.chat.Component;
/** Reuses the original, without rebuilding, restocking or story credit. */
public final class HotelBathCommands {
    private HotelBathCommands(){}
    public static LiteralArgumentBuilder<CommandSourceStack> build(){return Commands.literal("hotel_bath")
        .then(Commands.literal("start").executes(c->{var p=c.getSource().getPlayerOrException();if(!HotelBathHaunt.begin(p,true)){c.getSource().sendFailure(Component.literal("Enter the built hotel first. Its original bath creature must be loaded and dormant."));return 0;}return 1;}))
        .then(Commands.literal("dismiss").executes(c->{HotelBathHaunt.dismiss(c.getSource().getPlayerOrException());return 1;}))
        .then(Commands.literal("status").executes(c->{var p=c.getSource().getPlayerOrException();var state=HotelBathHaunt.state(p.serverLevel());var own=HotelVignette.personal(LabyrinthData.get(p.server),p.getUUID());c.getSource().sendSuccess(()->Component.literal("Bath: phase "+state.getInt("Phase")+"; age "+state.getInt("Age")+"; end "+state.getString("LastEnd")+"; your rest "+own.getInt("BathRest04113")+"; entries rolled "+own.getInt("BathRolls04113")+"; touches "+own.getInt("BathTouches04113")),false);return 1;}));}
}
