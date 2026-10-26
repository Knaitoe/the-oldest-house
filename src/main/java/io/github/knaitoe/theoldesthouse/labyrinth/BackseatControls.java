package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Native block and riding packets own the door and seat; no screen button starts the trap. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class BackseatControls {
    private BackseatControls(){}
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void use(PlayerInteractEvent.RightClickBlock e){
        if(e.isCanceled()||e.getHand()!=InteractionHand.MAIN_HAND||!(e.getEntity() instanceof ServerPlayer p))return;
        var car=BackseatCarBlock.root(p.level(),e.getPos());
        if(car!=null&&car.click(p,e.getHitVec().getLocation())){e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);}
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void strike(PlayerInteractEvent.LeftClickBlock e){
        if(e.getAction()!=PlayerInteractEvent.LeftClickBlock.Action.START||!(e.getEntity() instanceof ServerPlayer p))return;
        var car=BackseatCarBlock.root(p.level(),e.getPos());
        if(car!=null){e.setCanceled(true);if(car.mine(p)&&car.committed())car.rattle(p);}
    }
    @SubscribeEvent public static void dismount(EntityMountEvent e){
        if(!e.isDismounting()||!(e.getEntityBeingMounted() instanceof BackseatSeat seat)||!(e.getEntityMounting() instanceof ServerPlayer p)||p.level()!=seat.level())return;
        var car=seat.car();if(car!=null&&car.mine(p)&&car.committed())e.setCanceled(true);
    }
}
