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
 * and {@code status} says where it stands. Nothing here changes anyone else's record.
 */
public final class TheListCommands {
    private TheListCommands(){}

    public static LiteralArgumentBuilder<CommandSourceStack> build(){
        return Commands.literal(TheList.PLACE.id()).requires(s->s.hasPermission(2))
            .then(Commands.literal("body").executes(c->body(c.getSource(),true)).then(Commands.literal("off").executes(c->body(c.getSource(),false))))
            .then(Commands.literal("status").executes(c->status(c.getSource())));
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
            +"; pull "+String.format("%.2f",TheListBody.pull(p))+"; seen the face: "+(faced.isEmpty()?"nobody":String.join(", ",faced));
        source.sendSuccess(()->Component.literal(text),false);
        return 1;
    }
}
