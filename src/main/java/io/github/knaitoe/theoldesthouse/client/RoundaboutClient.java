package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.RoundaboutBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * 0.4.469, Bike night: a reader standing on the park's merry-go-round goes round with it. Where a reader is, is their own game's
 * to say, so it is their own game that carries them: after each tick, if they are standing on a wheel's deck, they are turned
 * about its hub by as far as it turned in that tick. Standing up on it while it goes fast they are pushed outward as well; keeping
 * low (sneaking), they hold on.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class RoundaboutClient {
    private RoundaboutClient(){}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        var mc=Minecraft.getInstance();var p=mc.player;
        if(p==null||mc.level==null||mc.isPaused()||p.isPassenger()||p.isSpectator())return;
        // The deck is three blocks across: its hub is under them or beside them.
        var feet=BlockPos.containing(p.getX(),p.getY()-.1,p.getZ());
        for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++){
            var at=feet.offset(dx,0,dz);var s=mc.level.getBlockState(at);
            if(!RoundaboutBlock.is(s)||s.getValue(RoundaboutBlock.CELL)!=RoundaboutBlock.Cell.HUB)continue;
            if(mc.level.getBlockEntity(at) instanceof RoundaboutBlock.Wheel wheel&&wheel.carry(p,p.isShiftKeyDown()))return;
        }
    }
}
