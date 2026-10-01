package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import io.github.knaitoe.theoldesthouse.house.*;

/** Operator fixtures make saved endings practical to test in a disposable world. */
public final class FinaleCommands {
    private FinaleCommands(){}
    public static LiteralArgumentBuilder<CommandSourceStack> build(){
        return Commands.literal("finale")
            .then(Commands.literal("prepare").executes(c->{FinaleArchitecture.request(c.getSource().getServer());c.getSource().sendSuccess(()->Component.literal("The staircase is being built in bounded batches. Use /oldesthouse finale go once ready."),false);return 1;}))
            .then(Commands.literal("go").executes(c->{var source=c.getSource();var player=source.getPlayerOrException();var origin=HouseSavedData.get(source.getServer()).houseOrigin();
                FinaleArchitecture.request(source.getServer());if(origin==null||!FinaleArchitecture.ready(source.getServer())){source.sendFailure(Component.literal("Spawn the House and prepare the finale first."));return 0;}
                var level=source.getServer().getLevel(HouseDimensions.INTERIOR);if(level==null)return 0;var at=FinaleArchitecture.base(origin).offset(0,FinaleArchitecture.TOP,12);level.getChunkAt(at);
                player.teleportTo(level,at.getX()+.5,at.getY(),at.getZ()+.5,180,0);FinaleProgress.phase(source.getServer(),player.getUUID(),FinaleProgress.Phase.STAIRCASE);return 1;}))
            .then(Commands.literal("cell").executes(c->{var player=c.getSource().getPlayerOrException();var origin=HouseSavedData.get(player.server).houseOrigin();if(origin==null||!FinaleArchitecture.ready(player.server))return 0;
                var level=player.server.getLevel(HouseDimensions.INTERIOR);if(level==null)return 0;var at=FinaleArchitecture.cell(origin).north(10);level.getChunkAt(at);player.teleportTo(level,at.getX()+.5,at.getY(),at.getZ()+.5,0,0);
                FinaleProgress.phase(player.server,player.getUUID(),FinaleProgress.Phase.STAIRCASE);return 1;}))
            .then(Commands.literal("start").executes(c->FinaleController.start(c.getSource().getPlayerOrException())?1:0))
            .then(Commands.literal("witness")
                .then(Commands.literal("ready").executes(c->{var player=c.getSource().getPlayerOrException();var data=LabyrinthData.get(player.server);
                    for(var story:WitnessAccount.Story.values()){
                        if(WitnessAccount.ready(data,player.getUUID()))break;
                        WitnessAccount.resolve(data,player.getUUID(),story,"operator_fixture");
                    }
                    WitnessAccount.updateBook(player,true);c.getSource().sendSuccess(()->Component.literal(WitnessAccount.count(data,player.getUUID())+"/"+WitnessAccount.REQUIRED+" personal resolutions recorded. Read the cell's lectern, lay down the original weapon, empty both hands, and crouch at the bars."),false);return 1;}))
                .then(Commands.literal("begin").executes(c->WitnessEnding.begin(c.getSource().getPlayerOrException())?1:0))
                .then(Commands.literal("status").executes(c->{var player=c.getSource().getPlayerOrException();var data=LabyrinthData.get(player.server);
                    c.getSource().sendSuccess(()->Component.literal("Resolutions: "+WitnessAccount.count(data,player.getUUID())+"/"+WitnessAccount.REQUIRED
                            +"; eligible sources: "+WitnessAccount.Story.values().length+". "+WitnessAccount.record(data,player.getUUID())),false);return 1;})))
            .then(Commands.literal("status").executes(c->{var player=c.getSource().getPlayerOrException();c.getSource().sendSuccess(()->Component.literal(FinaleProgress.player(player.server,player.getUUID()).toString()),false);return 1;}));
    }
}
