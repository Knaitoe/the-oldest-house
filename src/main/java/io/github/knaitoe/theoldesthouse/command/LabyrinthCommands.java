package io.github.knaitoe.theoldesthouse.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthBuilder;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthData;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthDoors;
import io.github.knaitoe.theoldesthouse.labyrinth.Growl;
import io.github.knaitoe.theoldesthouse.labyrinth.HideAndClap;
import io.github.knaitoe.theoldesthouse.labyrinth.HomeRooms;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthPlace;
import io.github.knaitoe.theoldesthouse.labyrinth.ModelHome;
import io.github.knaitoe.theoldesthouse.labyrinth.RedRoom;
import io.github.knaitoe.theoldesthouse.labyrinth.TellTaleFloorboards;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Test controls for the labyrinth and its vignettes:
 * {@code /oldesthouse door}, {@code /oldesthouse labyrinth} and
 * {@code /oldesthouse vignette}.
 */
public final class LabyrinthCommands {
    private LabyrinthCommands() {
    }

    /** {@code door <place|dealt>} places a test door; {@code door remove} takes the nearest one away. */
    public static LiteralArgumentBuilder<CommandSourceStack> door() {
        LiteralArgumentBuilder<CommandSourceStack> door = Commands.literal("door");
        for (LabyrinthPlace place : LabyrinthPlace.values()) {
            if (place.slot() >= 0) {
                door.then(Commands.literal(place.id()).executes(context -> placeDoor(context.getSource(), place.id())));
            }
        }
        door.then(Commands.literal(LabyrinthData.DEALT).executes(context -> placeDoor(context.getSource(), LabyrinthData.DEALT)));
        door.then(Commands.literal("remove").executes(context -> removeDoor(context.getSource())));
        return door;
    }

    public static LiteralArgumentBuilder<CommandSourceStack> labyrinth() {
        return Commands.literal("labyrinth")
                .then(Commands.literal("go").executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    if (!LabyrinthDoors.goToJunction(player)) {
                        context.getSource().sendFailure(Component.literal("Could not reach the labyrinth."));
                        return 0;
                    }
                    return 1;
                }))
                .then(Commands.literal("build").executes(context -> {
                    boolean started = LabyrinthBuilder.rebuild(context.getSource().getServer());
                    context.getSource().sendSuccess(() -> Component.literal(started
                            ? "Carving every labyrinth place again above the manor, a place a tick (the floorboards go back if unfinished)."
                            : "The Navidsons' house must exist first."), true);
                    return started ? 1 : 0;
                }))
                .then(Commands.literal("status").executes(context -> {
                    ServerPlayer viewer = context.getSource().getEntity() instanceof ServerPlayer p ? p : null;
                    for (String line : LabyrinthDoors.describe(context.getSource().getServer(), viewer)) {
                        context.getSource().sendSuccess(() -> Component.literal(line), false);
                    }
                    return 1;
                }));
    }

    public static LiteralArgumentBuilder<CommandSourceStack> vignette() {
        return Commands.literal("vignette")
                .then(io.github.knaitoe.theoldesthouse.labyrinth.NovelCommands.build())
                .then(Commands.literal("holloway_camp").then(Commands.literal("status").executes(context->{
                    var p=context.getSource().getPlayerOrException();var own=io.github.knaitoe.theoldesthouse.labyrinth.HollowayVignette.personal(LabyrinthData.get(p.server),p.getUUID());
                    context.getSource().sendSuccess(()->Component.literal("Holloway: visits "+own.getInt("Visits")+"; supplies taken "+own.getBoolean("Looted")+"; pursuit "+own.getBoolean("Run")+"; arena "+own.getInt("Arena")+"/3; escaped "+own.getBoolean("Escaped")),false);return 1;
                })))
                .then(Commands.literal("ted_caver").then(Commands.literal("status").executes(context->{
                    var player=context.getSource().getPlayerOrException();var data=LabyrinthData.get(player.server);var own=io.github.knaitoe.theoldesthouse.labyrinth.CaverVignette.personal(data,player.getUUID());
                    context.getSource().sendSuccess(()->Component.literal("Cave: work "+data.state("ted_caver").getInt("Work")+"/24; your squeeze "+own.getBoolean("Squeezed")+"; mark "+own.getBoolean("MarkRead")+"; rope pulling "+own.getBoolean("Pursuit")+"; escaped "+own.getBoolean("Escaped")+"; personal resolution "+io.github.knaitoe.theoldesthouse.labyrinth.WitnessAccount.has(data,player.getUUID(),io.github.knaitoe.theoldesthouse.labyrinth.WitnessAccount.Story.TED_CAVER)),false);return 1;
                })))
                .then(Commands.literal("goatman").then(Commands.literal("status").executes(context->{
                    var player=context.getSource().getPlayerOrException();var data=LabyrinthData.get(player.server);var run=io.github.knaitoe.theoldesthouse.labyrinth.GoatmanVignette.run(data);
                    context.getSource().sendSuccess(()->Component.literal("Trailer: phase "+run.getInt("Phase")+"; elapsed "+run.getInt("Clock")+"; expected children "+io.github.knaitoe.theoldesthouse.labyrinth.GoatmanVignette.expectedCount(run)+"; your vigil "+run.getCompound("Cohort").getCompound(player.getUUID().toString()).getInt("Vigil")+"; personal resolution "+io.github.knaitoe.theoldesthouse.labyrinth.WitnessAccount.has(data,player.getUUID(),io.github.knaitoe.theoldesthouse.labyrinth.WitnessAccount.Story.GOATMAN)),false);return 1;
                })))
                .then(Commands.literal(TellTaleFloorboards.ID)
                        .then(Commands.literal("reset").executes(context -> {
                            if (!LabyrinthBuilder.ensureBuilt(context.getSource().getServer())) {
                                context.getSource().sendFailure(Component.literal(
                                        "The labyrinth is not carved yet (it needs the Navidsons' house; if that exists, try again in a moment)."));
                                return 0;
                            }
                            if (!TellTaleFloorboards.reset(context.getSource().getServer())) {
                                context.getSource().sendFailure(Component.literal("The Navidsons' house must exist first."));
                                return 0;
                            }
                            context.getSource().sendSuccess(() -> Component.literal(
                                    "The floorboards are as they were: board down, heartbeat going, dealable again."), true);
                            return 1;
                        }))
                        .then(Commands.literal("complete").executes(context -> {
                            LabyrinthData.get(context.getSource().getServer()).setCompleted(TellTaleFloorboards.ID, true);
                            context.getSource().sendSuccess(() -> Component.literal(
                                    "The floorboards are marked finished; the dealer will not deal them."), true);
                            return 1;
                        })))
                .then(Commands.literal(HideAndClap.ID)
                        .then(Commands.literal("reset").executes(context -> {
                            if (!LabyrinthBuilder.ensureBuilt(context.getSource().getServer())) {
                                context.getSource().sendFailure(Component.literal(
                                        "The labyrinth is not carved yet (it needs the Navidsons' house; if that exists, try again in a moment)."));
                                return 0;
                            }
                            if (!HideAndClap.reset(context.getSource().getServer())) {
                                context.getSource().sendFailure(Component.literal("The Navidsons' house must exist first."));
                                return 0;
                            }
                            context.getSource().sendSuccess(() -> Component.literal(
                                    "The child's room is as it was: no wardrobe, the blindfold on the wall, dealable again."), true);
                            return 1;
                        }))
                        .then(Commands.literal("complete").executes(context -> {
                            LabyrinthData.get(context.getSource().getServer()).setCompleted(HideAndClap.ID, true);
                            context.getSource().sendSuccess(() -> Component.literal(
                                    "Hide-and-clap is marked finished; the dealer will not deal it."), true);
                            return 1;
                        })))
                .then(Commands.literal(ModelHome.ID)
                        .then(Commands.literal("reset").executes(context -> {
                            if (!LabyrinthBuilder.ensureBuilt(context.getSource().getServer())) {
                                context.getSource().sendFailure(Component.literal(
                                        "The labyrinth is not carved yet (it needs the Navidsons' house; if that exists, try again in a moment)."));
                                return 0;
                            }
                            if (!ModelHome.reset(context.getSource().getServer())) {
                                context.getSource().sendFailure(Component.literal("The Navidsons' house must exist first."));
                                return 0;
                            }
                            context.getSource().sendSuccess(() -> Component.literal(
                                    "The model home is as new: never visited, the binder on the counter, dealable again."), true);
                            return 1;
                        }))
                        .then(Commands.literal("visit")
                                .then(Commands.argument("visit", IntegerArgumentType.integer(1, ModelHome.FINAL_VISIT)).executes(context -> {
                                    int visit = IntegerArgumentType.getInteger(context, "visit");
                                    if (!LabyrinthBuilder.ensureBuilt(context.getSource().getServer())
                                            || !ModelHome.setVisit(context.getSource().getServer(), visit)) {
                                        context.getSource().sendFailure(Component.literal(
                                                "The labyrinth is not carved yet (it needs the Navidsons' house; if that exists, try again in a moment)."));
                                        return 0;
                                    }
                                    context.getSource().sendSuccess(() -> Component.literal(
                                            "The model home is set for visit " + visit + " of " + ModelHome.FINAL_VISIT
                                                    + ", its beat not yet seen (/oldesthouse door model_home to go in)."), true);
                                    return 1;
                                })))
                        .then(Commands.literal("complete").executes(context -> {
                            LabyrinthData.get(context.getSource().getServer()).setCompleted(ModelHome.ID, true);
                            context.getSource().sendSuccess(() -> Component.literal(
                                    "The model home is marked finished; the dealer will not deal it."), true);
                            return 1;
                        })))
                .then(Commands.literal(RedRoom.ID)
                        .then(Commands.literal("capture").executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            if (!RedRoom.captureHere(player)) {
                                context.getSource().sendFailure(Component.literal(
                                        "There is no room here to copy: stand inside an enclosed room with a floor and a ceiling."));
                                return 0;
                            }
                            context.getSource().sendSuccess(() -> Component.literal(
                                    "Copied the room you are standing in as your room. The Red Room will be it the next time a door leads there"
                                            + " (/oldesthouse door red_room)."), true);
                            return 1;
                        }))
                        .then(Commands.literal("forget").executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            HomeRooms.get(context.getSource().getServer()).forget(player.getUUID());
                            context.getSource().sendSuccess(() -> Component.literal(
                                    "Forgot where you spend your time and your copied room. The Red Room keeps whatever stands in it now."), true);
                            return 1;
                        }))
                        .then(Commands.literal("status").executes(context -> {
                            ServerPlayer viewer = context.getSource().getEntity() instanceof ServerPlayer p ? p : null;
                            for (String line : RedRoom.describe(context.getSource().getServer(), viewer)) {
                                context.getSource().sendSuccess(() -> Component.literal(line), false);
                            }
                            return 1;
                        })))
                .then(Commands.literal("phone_canoe")
                        .then(Commands.literal("status").executes(context -> {
                            ServerPlayer player=context.getSource().getPlayerOrException();
                            context.getSource().sendSuccess(()->Component.literal(io.github.knaitoe.theoldesthouse.labyrinth.PhoneCanoe.describe(player)),false);return 1;
                        })))
                .then(Commands.literal("preserved_cave")
                        .then(Commands.literal("status").executes(context -> {
                            for(String line:io.github.knaitoe.theoldesthouse.labyrinth.PreservedCave.describe(context.getSource().getServer()))
                                context.getSource().sendSuccess(() -> Component.literal(line),false);
                            return 1;
                        }))
                        .then(Commands.literal("reset").executes(context -> {
                            if(!LabyrinthBuilder.ensureBuilt(context.getSource().getServer()) || !io.github.knaitoe.theoldesthouse.labyrinth.PreservedCave.reset(context.getSource().getServer()))return 0;
                            context.getSource().sendSuccess(() -> Component.literal("Cave visit reset. The church's permanent consequence is retained."),true);return 1;
                        })))
                .then(Commands.literal("shallows")
                        .then(Commands.literal("status").executes(context -> {
                            ServerPlayer player=context.getSource().getPlayerOrException();var data=io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthData.get(player.server);
                            context.getSource().sendSuccess(() -> Component.literal("Shallows: hunted "+io.github.knaitoe.theoldesthouse.labyrinth.IndianLakeProgress.wasHunted(data,player.getUUID())+"; participated "+io.github.knaitoe.theoldesthouse.labyrinth.IndianLakeProgress.hasThrown(data,player.getUUID())+"."),false);return 1;
                        }))
                        .then(Commands.literal("unlock").executes(context -> {
                            ServerPlayer player=context.getSource().getPlayerOrException();io.github.knaitoe.theoldesthouse.labyrinth.IndianLakeProgress.hunted(io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthData.get(player.server),player.getUUID());
                            context.getSource().sendSuccess(() -> Component.literal("Debug only: your shore hunt recorded; Shallows can now be dealt."),true);return 1;
                        }))
                        .then(Commands.literal("reset").executes(context -> {
                            io.github.knaitoe.theoldesthouse.labyrinth.Shallows.reset(context.getSource().getPlayerOrException());
                            context.getSource().sendSuccess(() -> Component.literal("Your Shallows participation reset. Other explorers are unchanged."),true);return 1;
                        })))
                .then(Commands.literal("drowned_town")
                        .then(Commands.literal("status").executes(context -> {
                            for (String line : io.github.knaitoe.theoldesthouse.labyrinth.DrownedTown.describe(context.getSource().getServer()))
                                context.getSource().sendSuccess(() -> Component.literal(line), false);
                            return 1;
                        }))
                        .then(Commands.literal("reset").executes(context -> {
                            if (!LabyrinthBuilder.ensureBuilt(context.getSource().getServer())
                                    || !io.github.knaitoe.theoldesthouse.labyrinth.DrownedTown.reset(context.getSource().getServer())) {
                                context.getSource().sendFailure(Component.literal("The House must exist and its labyrinth must be carved first.")); return 0;
                            }
                            context.getSource().sendSuccess(() -> Component.literal("Drowned Town reset: school stocked, church sealed, essays not yet dried."), true); return 1;
                        })));
    }

    /** {@code growl far|below|near} plays the Growl for you now; {@code growl basement} wakes you in the cellar. */
    public static LiteralArgumentBuilder<CommandSourceStack> growl() {
        LiteralArgumentBuilder<CommandSourceStack> growl = Commands.literal("growl");
        for (Growl.Kind kind : Growl.Kind.values()) {
            growl.then(Commands.literal(kind.name().toLowerCase(java.util.Locale.ROOT)).executes(context -> {
                Growl.growl(context.getSource().getPlayerOrException(), kind);
                return 1;
            }));
        }
        growl.then(Commands.literal("change").executes(context -> {
            if (!io.github.knaitoe.theoldesthouse.labyrinth.GrowlChanges.request(context.getSource().getPlayerOrException())) {
                context.getSource().sendFailure(Component.literal("Enter an active gray hall or the ordinary manor. Quiet rooms, vignettes and the finale are excluded."));
                return 0;
            }
            context.getSource().sendSuccess(() -> Component.literal("One change is waiting for an unwitnessed opportunity. Use /oldesthouse growl status to inspect it."), false);
            return 1;
        }));
        growl.then(Commands.literal("basement").executes(context -> {
            if (!Growl.wakeInBasement(context.getSource().getPlayerOrException())) {
                context.getSource().sendFailure(Component.literal("The Navidsons' house must exist first."));
                return 0;
            }
            return 1;
        }));
        growl.then(Commands.literal("status").executes(context -> {
            ServerPlayer viewer = context.getSource().getEntity() instanceof ServerPlayer p ? p : null;
            for (String line : Growl.describe(context.getSource().getServer(), viewer)) {
                context.getSource().sendSuccess(() -> Component.literal(line), false);
            }
            return 1;
        }));
        return growl;
    }

    public static int placeDoor(CommandSourceStack source, String destination) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        if (HouseSavedData.get(source.getServer()).houseOrigin() == null) {
            source.sendFailure(Component.literal("The Navidsons' house must exist first (/oldesthouse opening house, or /oldesthouse spawn)."));
            return 0;
        }
        LabyrinthBuilder.ensureBuilt(source.getServer());
        String error = LabyrinthDoors.placeCommandDoor(player, destination);
        if (error != null) {
            source.sendFailure(Component.literal(error));
            return 0;
        }
        boolean noRoom = RedRoom.ID.equals(destination) && HomeRooms.get(source.getServer()).choiceFor(player.getUUID()) == null;
        source.sendSuccess(() -> Component.literal("A door to " + destination
                + " stands in front of you. Open it to go through; walk back out through the door you arrive at to come back."
                + (LabyrinthBuilder.isCarving() ? " The labyrinth is being carved; give it a moment." : "")
                + (noRoom ? " No room has been copied yet, so it stays locked: stand in a room of yours and run"
                        + " /oldesthouse vignette red_room capture first." : "")), true);
        return 1;
    }

    public static int removeDoor(CommandSourceStack source) throws CommandSyntaxException {
        String removed = LabyrinthDoors.removeCommandDoor(source.getPlayerOrException());
        if (removed == null) {
            source.sendFailure(Component.literal("No test door within eight blocks."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Removed test door " + removed + "."), true);
        return 1;
    }
}
