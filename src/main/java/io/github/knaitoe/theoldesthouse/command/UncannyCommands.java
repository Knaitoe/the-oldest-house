package io.github.knaitoe.theoldesthouse.command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.commands.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
/** Explicit operator travel and status. Starting never rebuilds, restocks or replaces an existing original. */
public final class UncannyCommands {
    private UncannyCommands(){}
    public static LiteralArgumentBuilder<CommandSourceStack> build(){
        var root=Commands.literal("uncanny");
        for(var place:UncannyRooms.PLACES)root.then(Commands.literal(place.id())
            .then(Commands.literal("start").executes(c->start(c.getSource(),place)))
            .then(Commands.literal("status").executes(c->{var p=c.getSource().getPlayerOrException();var own=LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),place);c.getSource().sendSuccess(()->Component.literal(place.id()+": age "+own.getInt("UncannyAge")+"; stage "+own.getInt("UncannyStage")+"; source "+own.getBoolean("Read_Source")+"; ready "+own.getBoolean("Ready")+"; complete "+own.getBoolean("Completed")+"; failed "+own.getBoolean("UncannyFailed")+"; peeled "+own.getInt("Peeled")+"; feeds "+own.getInt("FeedsSeen")+"/15; sleeps "+own.getInt("Sleeps")),false);return 1;})));
        root.then(Commands.literal("return").executes(c->back(c.getSource())));return root;
    }
    private static int start(CommandSourceStack source,LabyrinthPlace place)throws com.mojang.brigadier.exceptions.CommandSyntaxException{
        var p=source.getPlayerOrException();var origin=HouseSavedData.get(p.server).houseOrigin();var data=LabyrinthData.get(p.server);
        if(origin==null||!LabyrinthBuilder.isPlaceReady(data,place)){source.sendFailure(Component.literal("The room has not finished building."));return 0;}
        var b=LabyrinthPlaces.base(origin,place);var level=p.server.getLevel(NovelRooms.dimension(place));
        if(b==null||level==null||!level.getBlockState(b.offset(UncannyRooms.source(place))).is(UncannyRegistry.PROP.get())){source.sendFailure(Component.literal("This room needs a new build. Use /ghosthouse labyrinth build in a fresh world."));return 0;}
        if(!p.getPersistentData().contains("UncannyTestReturn")){var saved=new CompoundTag();saved.putString("Dimension",p.level().dimension().location().toString());saved.putDouble("X",p.getX());saved.putDouble("Y",p.getY());saved.putDouble("Z",p.getZ());saved.putFloat("Yaw",p.getYRot());saved.putFloat("Pitch",p.getXRot());p.getPersistentData().put("UncannyTestReturn",saved);}
        IndianLakeRooms.keepLoaded(level,b,place);p.teleportTo(level,b.getX()+.5,b.getY(),b.getZ()-3,180,0);p.hasChangedDimension();p.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);LiteraryVignettes.onArrive(p,place);return 1;
    }
    private static int back(CommandSourceStack source)throws com.mojang.brigadier.exceptions.CommandSyntaxException{
        var p=source.getPlayerOrException();var saved=p.getPersistentData().getCompound("UncannyTestReturn");var id=ResourceLocation.tryParse(saved.getString("Dimension"));if(id==null)return 0;
        var level=p.server.getLevel(ResourceKey.create(Registries.DIMENSION,id));if(level==null)return 0;
        p.teleportTo(level,saved.getDouble("X"),saved.getDouble("Y"),saved.getDouble("Z"),saved.getFloat("Yaw"),saved.getFloat("Pitch"));p.hasChangedDimension();p.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);p.getPersistentData().remove("UncannyTestReturn");return 1;
    }
}
