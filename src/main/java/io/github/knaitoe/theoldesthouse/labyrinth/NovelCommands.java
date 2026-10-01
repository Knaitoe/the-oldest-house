package io.github.knaitoe.theoldesthouse.labyrinth;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.knaitoe.theoldesthouse.house.*;
import net.minecraft.commands.*;
import net.minecraft.network.chat.Component;
/** Direct operator access still runs the scene's real arrival and personal progress. */
public final class NovelCommands {
    private NovelCommands(){}
    public static LiteralArgumentBuilder<CommandSourceStack> build(){var root=Commands.literal("novel");
        for(var place:NovelVignettes.PLACES)root.then(Commands.literal(place.id())
            .then(Commands.literal("go").executes(c->{var p=c.getSource().getPlayerOrException();if(FinaleProgress.committed(FinaleProgress.phase(p.server,p.getUUID()))||FinaleController.lockedOut(p))return 0;
                if(!LabyrinthBuilder.ensureBuilt(p.server)){c.getSource().sendFailure(Component.literal("Spawn the House first; its appended rooms are being built. Try again in a moment."));return 0;}
                var origin=HouseSavedData.get(p.server).houseOrigin();var b=LabyrinthPlaces.base(origin,place);var level=p.server.getLevel(NovelRooms.dimension(place));if(b==null||level==null)return 0;
                var data=LabyrinthData.get(p.server);data.pushReturn(p.getUUID(),new LabyrinthData.Waypoint(p.level().dimension(),p.position(),p.getYRot()));level.getChunkAt(b);p.teleportTo(level,b.getX()+.5,b.getY(),b.getZ()-3.5,180,0);
                data.visit(p.getUUID(),place);NovelVignettes.onArrive(p,place);return 1;}))
            .then(Commands.literal("status").executes(c->{var p=c.getSource().getPlayerOrException();c.getSource().sendSuccess(()->Component.literal(place.id()+": "+NovelVignettes.personal(LabyrinthData.get(p.server),p.getUUID())),false);return 1;})));
        return root;
    }
}
