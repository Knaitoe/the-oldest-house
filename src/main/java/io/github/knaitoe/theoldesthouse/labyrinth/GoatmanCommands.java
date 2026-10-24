package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Display;
import net.minecraft.world.item.component.CustomData;

/** Operator-only supper preparation. The original evening, cast and ending conditions keep running. */
public final class GoatmanCommands {
    private static final String PLATED="TestPlated04115";
    private GoatmanCommands(){}

    public static LiteralArgumentBuilder<CommandSourceStack> build(){
        return Commands.literal(GoatmanVignette.ID).requires(s->s.hasPermission(2))
            .then(Commands.literal("plate").executes(c->plate(c.getSource())))
            .then(Commands.literal("status").executes(c->status(c.getSource())));
    }

    private static int fail(CommandSourceStack source,String message){source.sendFailure(Component.literal(message));return 0;}

    private static int plate(CommandSourceStack source)throws CommandSyntaxException{
        var player=source.getPlayerOrException();
        if(!GoatmanVignette.inside(player))return fail(source,"Enter the Goatman scene and reach the campsite first.");
        var data=LabyrinthData.get(player.server);var run=GoatmanVignette.run(data);var cohort=run.getCompound("Cohort");var own=cohort.getCompound(player.getUUID().toString());
        if(!run.hasUUID("Id")||!own.getBoolean("Active")||own.getBoolean("Failed"))return fail(source,"Join a fresh Goatman evening before preparing its supper.");
        if(!GoatmanVignette.fresh(run)||!run.getBoolean("PlayerServes0464")||run.getInt("Phase")!=GoatmanVignette.GATHERING)
            return fail(source,"Use this during supper preparation, before the knocking night. It does not restart a night or a finished round.");
        if(run.getBoolean(PLATED)||run.getBoolean("MealReady0465")){
            source.sendSuccess(()->Component.literal("Supper is already prepared. The evening continues normally; eaten servings are not refilled."),false);return 1;
        }
        if(run.getInt("Clock")>=GoatmanVignette.supperAt(run))return fail(source,"Supper has already started; this shortcut does not replace eaten food.");
        // Count the saved cohort, including a reader who joined since the last scene tick.
        int expected=GoatmanVignette.count(run)-1+cohort.getAllKeys().size();
        if(expected<1||expected>24)return fail(source,"The supper has no valid set of diners yet.");
        var level=player.serverLevel();var base=GoatmanVignette.base(player.server);
        var settings=level.getEntitiesOfClass(Display.ItemDisplay.class,IndianLakeRooms.bounds(base,LabyrinthPlace.GOATMAN),e->e.getTags().contains(GoatmanWoods.PLATE)&&!e.getTags().contains(GoatmanWoods.BRAT)&&e.getPersistentData().contains("Plate"));
        // Validate every place before creating anything; do not force chunks or restore edited furniture.
        for(int seat=0;seat<expected;seat++){
            var at=BlockPos.containing(GoatmanWoods.plate(seat).add(base.getX(),base.getY(),base.getZ()));final int place=seat;
            if(!level.hasChunkAt(at)||!level.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(at.getX()>>4,at.getZ()>>4)))return fail(source,"The plates are still loading. Try again in a moment.");
            if(!GoatmanWoods.tableSupport(level.getBlockState(at.below()))||settings.stream().noneMatch(e->e.getPersistentData().getInt("Plate")==place))return fail(source,"Place setting "+(seat+1)+" is missing or unsupported. Restore it before plating.");
            var shown=GoatmanWoods.serving(level,base,seat);
            if(shown!=null&&!GoatmanWoods.food(level,shown).is(GoatmanRegistry.BRAT.get()))return fail(source,"Place setting "+(seat+1)+" already holds another item; it has been left intact.");
        }
        int mask=run.getInt("Plated0464"),empty=run.getInt("EmptyPlates0465"),burnt=run.getInt("BurntPlates0476"),added=0;
        for(int seat=0;seat<expected;seat++){
            var shown=GoatmanWoods.serving(level,base,seat);
            if(shown==null){
                var food=GoatmanSupper.frank(run,false);CustomData.update(DataComponents.CUSTOM_DATA,food,t->t.putBoolean("GoatmanTest04115",true));
                shown=GoatmanWoods.served(level,base,seat,food);
                if(shown==null)return fail(source,"A serving could not be placed. Existing food is intact; try again.");
                added++;
            }
            int bit=1<<seat;mask|=bit;empty&=~bit;
            burnt=GoatmanSupper.burnt(GoatmanWoods.food(level,shown))?burnt|bit:burnt&~bit;
        }
        run.putInt("Expected",expected);run.putInt("Plated0464",mask);run.putInt("EmptyPlates0465",empty);run.putInt("BurntPlates0476",burnt);
        run.putBoolean("MealReady0465",true);run.putBoolean("MealFrozen0465",true);run.putBoolean(PLATED,true);GoatmanVignette.saveRun(data,run);
        final int served=added;
        source.sendSuccess(()->Component.literal("Supper ready for "+expected+" diners ("+served+" test servings added). The evening continues toward the knocking; use the door and bathroom window to test the ending."),false);
        return 1;
    }

    private static int status(CommandSourceStack source)throws CommandSyntaxException{
        var player=source.getPlayerOrException();var data=LabyrinthData.get(player.server);var run=GoatmanVignette.run(data);
        source.sendSuccess(()->Component.literal("Trailer: phase "+run.getInt("Phase")+"; elapsed "+run.getInt("Clock")+"; expected children "+GoatmanVignette.expectedCount(run)+"; supper ready "+GoatmanSupper.ready(run)+"; plated "+Integer.bitCount(run.getInt("Plated0464")&~run.getInt("EmptyPlates0465"))+"; evening leg "+run.getCompound("EveningCircuit0497").getInt("StalkLeg0465")+"; circuit leg "+run.getInt("StalkLeg0465")+"; completed circuits "+run.getInt("Circuits0494")+"; window approach "+run.getInt("StalkWindowLeg0465")+"; continuing door knocks "+run.getInt("DoorPulseSerial04112")+"; your vigil "+run.getCompound("Cohort").getCompound(player.getUUID().toString()).getInt("Vigil")+"; personal resolution "+WitnessAccount.has(data,player.getUUID(),WitnessAccount.Story.GOATMAN)),false);return 1;
    }
}
