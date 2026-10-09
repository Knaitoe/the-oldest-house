package io.github.knaitoe.theoldesthouse.labyrinth;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
/** The player opens and places the finite supper. Old rounds retain their saved flow. */
final class GoatmanSupper {
    static boolean interact(ServerPlayer player,PlayerInteractEvent.RightClickBlock event){
        var data=LabyrinthData.get(player.server);var run=GoatmanVignette.run(data);
        if(!run.getBoolean("PlayerServes0464")||run.getInt("Phase")!=GoatmanVignette.GATHERING)return false;
        var member=run.getCompound("Cohort").getCompound(player.getUUID().toString());
        if(!member.getBoolean("Active")||member.getBoolean("Failed"))return false;
        var base=GoatmanVignette.base(player.server);var rel=event.getPos().subtract(base);var held=player.getItemInHand(event.getHand());
        boolean stove=rel.equals(GoatmanWoods.STOVE)||rel.equals(GoatmanWoods.PAN);
        if(stove){
            int issued=run.getInt("PacketsIssued0464"),limit=(run.getInt("Expected")+3)/4;
            if(held.is(GoatmanRegistry.FRANKS.get()))player.setItemInHand(event.getHand(),held.getItem().use(player.level(),player,event.getHand()).getObject());
            else if(issued<limit){
                var packet=new ItemStack(GoatmanRegistry.FRANKS.get());
                CustomData.update(DataComponents.CUSTOM_DATA,packet,t->t.putUUID(GoatmanVignette.ROUND,run.getUUID("Id")));
                packet.set(DataComponents.LORE,new ItemLore(List.of(Component.literal("Packaged in 4"),Component.literal("Use to open"))));
                run.putInt("PacketsIssued0464",issued+1);GoatmanVignette.saveRun(data,run);
                if(!player.getInventory().add(packet))player.drop(packet,false);
            }else player.displayClientMessage(Component.literal("Only the empty wrappers are left."),true);
        }else if(held.is(GoatmanRegistry.BRAT.get())&&rel.getY()>=1&&rel.getY()<=3&&rel.getX()>=-1&&rel.getX()<=2&&rel.getZ()<=-59&&rel.getZ()>=-73){
            var custom=held.get(DataComponents.CUSTOM_DATA);var tag=custom==null?null:custom.copyTag();
            if(tag==null||!tag.hasUUID(GoatmanVignette.ROUND)||!tag.getUUID(GoatmanVignette.ROUND).equals(run.getUUID("Id")))return false;
            var hit=event.getHitVec().getLocation().subtract(base.getX(),base.getY(),base.getZ());int seat=-1;double distance=1.6;
            for(int i=0;i<run.getInt("Expected");i++){double d=GoatmanWoods.plate(i).distanceToSqr(hit);if(d<distance){distance=d;seat=i;}}
            if(seat>=0){
                int mask=run.getInt("Plated0464");if((mask&(1<<seat))==0){
                    held.shrink(1);run.putInt("Plated0464",mask|(1<<seat));GoatmanVignette.saveRun(data,run);GoatmanWoods.served(player.serverLevel(),base,seat,true);
                }else player.displayClientMessage(Component.literal("There's already one here."),true);
            }
        }else return false;
        event.setCanceled(true);event.setCancellationResult(InteractionResult.SUCCESS);return true;
    }
}
