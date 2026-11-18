package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayList;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/**
 * 0.4.705 (docs/THE_LIST_0_4_705.md): operator-only. The list is not dealt until night one; until then the slasher's body is tried on in
 * the town by hand: {@code /ghosthouse test the_list body} gives it to the operator standing in the town, {@code body off} takes it back,
 * and {@code status} says where it stands. 0.4.706: {@code night} begins night one round the operator, as the one Deek picks;
 * {@code night party|seed|shut|after} moves it straight there; {@code night off} ends it and puts the town back. 0.4.707: {@code night light}
 * brings first light, and {@code night off} also holds the town quiet, so that no night begins round anyone coming in until {@code night}
 * begins one. 0.4.708: {@code hunt} begins the hunt round the operator (given the body and, 0.4.709, the hay hook; the night put away,
 * the town held quiet), and
 * {@code hunt off} ends it. 0.4.718: {@code hunt <night>} begins it on one of the four nights (one if none is given); on the fourth the gin
 * runs. 0.4.720: {@code night two} begins night two round the operator. Nothing here changes anyone else's record.
 */
public final class TheListCommands {
    private TheListCommands(){}

    public static LiteralArgumentBuilder<CommandSourceStack> build(){
        return Commands.literal(TheList.PLACE.id()).requires(s->s.hasPermission(2))
            .then(Commands.literal("body").executes(c->body(c.getSource(),true)).then(Commands.literal("off").executes(c->body(c.getSource(),false))))
            .then(Commands.literal("status").executes(c->status(c.getSource())))
            .then(Commands.literal("night").executes(c->night(c.getSource(),-1))
                .then(Commands.literal("off").executes(c->night(c.getSource(),TheListNight.NONE)))
                .then(Commands.literal("party").executes(c->night(c.getSource(),TheListNight.PARTY)))
                .then(Commands.literal("seed").executes(c->night(c.getSource(),TheListNight.SEED)))
                .then(Commands.literal("shut").executes(c->night(c.getSource(),TheListNight.SHUT)))
                .then(Commands.literal("after").executes(c->night(c.getSource(),TheListNight.AFTER)))
                .then(Commands.literal("light").executes(c->night(c.getSource(),TheListNight.DONE)))
                .then(Commands.literal("two").executes(c->two(c.getSource()))))
            .then(Commands.literal("hunt").executes(c->hunt(c.getSource(),true,1)).then(Commands.literal("off").executes(c->hunt(c.getSource(),false,1)))
                .then(Commands.argument("night",com.mojang.brigadier.arguments.IntegerArgumentType.integer(1,4)).executes(c->hunt(c.getSource(),true,com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(c,"night")))));
    }

    private static int night(CommandSourceStack source,int phase)throws CommandSyntaxException{
        var p=source.getPlayerOrException();var b=TheListBody.town(p);
        if(b==null){source.sendFailure(Component.literal("Go through the back door into Lamesa first: /ghosthouse test vignette the_list places its door."));return 0;}
        var l=p.serverLevel();
        if(phase==TheListNight.NONE){TheListNight.end(l,b);TheListNight.quiet(p.server,b,true);source.sendSuccess(()->Component.literal("Night one is over and the town is put back; no night begins round anyone coming in until /ghosthouse test the_list night."),false);return 1;}
        if(TheListNight.phase(p.server,b)==TheListNight.NONE&&!TheListNight.begin(p,b)){source.sendFailure(Component.literal("Spectators and the dead cannot be picked."));return 0;}
        if(phase>TheListNight.PARTY)TheListNight.jump(l,b,phase);
        int now=TheListNight.phase(p.server,b);
        source.sendSuccess(()->Component.literal("Night one: "+TheListNight.NAMES[now]+", "+TheListNight.hour(TheListNight.time(p.server,b))+". /ghosthouse test the_list night off ends it."),false);
        return 1;
    }

    /** 0.4.720: night two begun round the operator, as the one who went in, night one put behind them. */
    private static int two(CommandSourceStack source)throws CommandSyntaxException{
        var p=source.getPlayerOrException();var b=TheListBody.town(p);
        if(b==null){source.sendFailure(Component.literal("Go through the back door into Lamesa first: /ghosthouse test vignette the_list places its door."));return 0;}
        if(!TheListDen.begin(p,b)){source.sendFailure(Component.literal("Spectators and the dead cannot be the one who went in."));return 0;}
        source.sendSuccess(()->Component.literal("Night two: ten o'clock, Amber in her den. Knock at her back door. /ghosthouse test the_list night off ends it."),false);
        return 1;
    }

    private static int hunt(CommandSourceStack source,boolean on,int night)throws CommandSyntaxException{
        var p=source.getPlayerOrException();var b=TheListBody.town(p);
        if(b==null){source.sendFailure(Component.literal("Go through the back door into Lamesa first: /ghosthouse test vignette the_list places its door."));return 0;}
        var l=p.serverLevel();
        if(!on){TheListHunt.end(l,b);source.sendSuccess(()->Component.literal("The hunt is over; the five are back at their places and the doors put back."),false);return 1;}
        if(!TheListBody.give(p)){source.sendFailure(Component.literal("Spectators and the dead are not given the body."));return 0;}
        TheListBody.giveHook(p);
        if(TheListNight.phase(p.server,b)!=TheListNight.NONE)TheListNight.end(l,b);
        TheListNight.quiet(p.server,b,true);
        TheListHunt.start(l,b,night);
        source.sendSuccess(()->Component.literal("The hunt is on, night "+night+": the five are at their places and run when they see you, and the hay hook is in your hands"+(night>=TheListGin.NIGHT?"; the gin runs":"")+". /ghosthouse test the_list hunt off ends it."),false);
        return 1;
    }

    private static int body(CommandSourceStack source,boolean on)throws CommandSyntaxException{
        var p=source.getPlayerOrException();
        if(!on){TheListBody.end(p);source.sendSuccess(()->Component.literal("The slasher's body is taken back, the sack with it."),false);return 1;}
        if(TheListBody.town(p)==null){source.sendFailure(Component.literal("Go through the back door into Lamesa first: /ghosthouse test vignette the_list places its door."));return 0;}
        if(!TheListBody.give(p)){source.sendFailure(Component.literal("Spectators and the dead are not given the body."));return 0;}
        source.sendSuccess(()->Component.literal("The slasher's body is yours while you are in the town. /ghosthouse test the_list body off takes it back."),false);
        return 1;
    }

    private static int status(CommandSourceStack source)throws CommandSyntaxException{
        var p=source.getPlayerOrException();var own=TheListBody.own(p);
        var faced=new ArrayList<String>();int bits=own.getInt("Faced");
        for(int who=0;who<TheListTexts.NAMES.length;who++)if((bits&1<<who)!=0&&TheListTexts.NAMES[who]!=null)faced.add(TheListTexts.NAMES[who]);
        String text="The list: body "+(own.getBoolean("Body")?"yes":"no")+"; in town "+(TheListBody.town(p)!=null)+"; walking "+TheListBody.walking(p)
            +"; down "+TheListBody.down(p)+"; sack "+(TheListSack.worn(p)?"worn":TheListBody.hasSack(p)?"carried":"none")
            +"; pull "+String.format("%.2f",TheListBody.pull(p))+"; seen the face: "+(faced.isEmpty()?"nobody":String.join(", ",faced))
            +(TheListBody.town(p)!=null?"; hunt: "+TheListHunt.status(TheListBody.town(p)):"");
        source.sendSuccess(()->Component.literal(text),false);
        return 1;
    }
}
