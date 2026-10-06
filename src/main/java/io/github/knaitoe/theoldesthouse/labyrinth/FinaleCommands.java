package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import io.github.knaitoe.theoldesthouse.house.*;

/** Operator fixtures make saved endings practical to test in a disposable world. */
public final class FinaleCommands {
    private FinaleCommands(){}
    private static int rehearse(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var p=source.getPlayerOrException();var origin=HouseSavedData.get(p.server).houseOrigin();
        if(origin==null||!FinaleArchitecture.ready(p.server)){FinaleArchitecture.request(p.server);source.sendFailure(Component.literal("Spawn the House and /oldesthouse finale prepare first. Retry once the staircase is ready."));return 0;}
        if(FinaleController.lockedOut(p)||FinaleProgress.committed(FinaleProgress.phase(p.server,p.getUUID())))return 0;
        var level=p.server.getLevel(HouseDimensions.INTERIOR);if(level==null)return 0;
        var id=WeaponHistory.favorite(p);net.minecraft.world.item.ItemStack original=net.minecraft.world.item.ItemStack.EMPTY;int slot=-1;
        for(int i=0;i<p.getInventory().getContainerSize();i++)if(WeaponHistory.wounds(p.getInventory().getItem(i),id)){original=p.getInventory().removeItemNoUpdate(i);slot=i;break;}
        if(original.isEmpty()){original=new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_SWORD);WeaponHistory.record(p,original,Integer.MAX_VALUE-1001);}
        var old=p.getMainHandItem();p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,net.minecraft.world.item.ItemStack.EMPTY);if(!old.isEmpty()&&!p.getInventory().add(old))p.drop(old,false);
        old=p.getOffhandItem();p.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,net.minecraft.world.item.ItemStack.EMPTY);if(!old.isEmpty()&&!p.getInventory().add(old))p.drop(old,false);
        p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,original);p.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.SHIELD));
        var at=FinaleArchitecture.cell(origin).north(12);level.getChunkAt(at);p.teleportTo(level,at.getX()+.5,at.getY(),at.getZ()+.5,0,0);p.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        FinaleProgress.phase(p.server,p.getUUID(),FinaleProgress.Phase.STAIRCASE);source.sendSuccess(()->Component.literal("Original weapon and shield equipped. Open the cell or use /oldesthouse finale start. Block its rush, then strike while it is stunned. This plays a permanent ending; use a test world."),false);return 1;
    }
    public static LiteralArgumentBuilder<CommandSourceStack> build(){
        return Commands.literal("finale")
            .then(Commands.literal("rewrite").executes(c->{
                boolean changed=StaircaseStory.rewrite(c.getSource().getPlayerOrException());
                if(changed)c.getSource().sendSuccess(()->Component.literal("The carried original now has the revised account. Bound leaves and burned fires are retained; the previous words remain in the record."),false);
                else c.getSource().sendFailure(Component.literal("Carry your own unfinished House of Leaves first."));return changed?1:0;
            }))
            .then(Commands.literal("prepare").executes(c->{FinaleArchitecture.request(c.getSource().getServer());c.getSource().sendSuccess(()->Component.literal("The staircase is being built in bounded batches. Use /oldesthouse finale go once ready."),false);return 1;}))
            .then(Commands.literal("go").executes(c->{var source=c.getSource();var player=source.getPlayerOrException();var origin=HouseSavedData.get(source.getServer()).houseOrigin();
                FinaleArchitecture.request(source.getServer());if(origin==null||!FinaleArchitecture.ready(source.getServer())){source.sendFailure(Component.literal("Spawn the House and prepare the finale first."));return 0;}
                var level=source.getServer().getLevel(HouseDimensions.INTERIOR);if(level==null)return 0;var at=FinaleArchitecture.base(origin).offset(0,FinaleArchitecture.TOP,12);level.getChunkAt(at);
                player.teleportTo(level,at.getX()+.5,at.getY(),at.getZ()+.5,180,0);FinaleProgress.phase(source.getServer(),player.getUUID(),FinaleProgress.Phase.STAIRCASE);return 1;}))
            .then(Commands.literal("cell").executes(c->{var player=c.getSource().getPlayerOrException();var origin=HouseSavedData.get(player.server).houseOrigin();if(origin==null||!FinaleArchitecture.ready(player.server))return 0;
                var level=player.server.getLevel(HouseDimensions.INTERIOR);if(level==null)return 0;var at=FinaleArchitecture.cell(origin).north(10);level.getChunkAt(at);player.teleportTo(level,at.getX()+.5,at.getY(),at.getZ()+.5,0,0);
                FinaleProgress.phase(player.server,player.getUUID(),FinaleProgress.Phase.STAIRCASE);return 1;}))
            .then(Commands.literal("rehearse").executes(c->rehearse(c.getSource())))
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
