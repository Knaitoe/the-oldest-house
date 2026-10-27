package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Papers and the real hatch remain usable while carrying ordinary belongings. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class SiloControls {
    private SiloControls(){}
    @SubscribeEvent public static void fall(net.neoforged.neoforge.event.entity.living.LivingFallEvent e){if(e.getEntity() instanceof ServerPlayer p&&SiloSites.inside(p))e.setDamageMultiplier(0);}
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void use(PlayerInteractEvent.RightClickBlock e){
        if(e.isCanceled()||e.getHand()!=InteractionHand.MAIN_HAND||!(e.getEntity() instanceof ServerPlayer p)||!LiteraryVignettes.participant(p))return;
        var source=LabyrinthData.get(p.server).doorAt(p.level().dimension(),e.getPos());
        if(source!=null&&SiloSites.SOURCE.equals(source.destination)){e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);SiloSites.enter(p,source);return;}
        if(!SiloSites.inside(p)||p.distanceToSqr(e.getPos().getCenter())>36)return;
        if(p.level().getBlockEntity(e.getPos()) instanceof SiloPaperBlock.Paper paper){paper.open(p);e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);}
        else if(p.level().getBlockState(e.getPos()).is(SiloRegistry.HATCH.get())){
            var state=p.level().getBlockState(e.getPos()).cycle(TrapDoorBlock.OPEN);p.level().setBlock(e.getPos(),state,3);
            p.level().playSound(null,e.getPos(),state.getValue(TrapDoorBlock.OPEN)?net.minecraft.sounds.SoundEvents.COPPER_TRAPDOOR_OPEN:net.minecraft.sounds.SoundEvents.COPPER_TRAPDOOR_CLOSE,net.minecraft.sounds.SoundSource.BLOCKS,.7F,.8F);
            if(state.getValue(TrapDoorBlock.OPEN))SiloSites.openedHatch(p,e.getPos());e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);
        }
    }
}
