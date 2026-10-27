package io.github.knaitoe.theoldesthouse.labyrinth;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
/** Native mining input remains the verb even where the House protects ordinary construction. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class UncannyInteractions {
    private UncannyInteractions(){}
    @SubscribeEvent(priority=EventPriority.HIGHEST,receiveCanceled=true)
    public static void attack(PlayerInteractEvent.LeftClickBlock event){
        if(event.getAction()!=PlayerInteractEvent.LeftClickBlock.Action.START||!(event.getEntity() instanceof ServerPlayer p)||!LiteraryVignettes.participant(p)||p.distanceToSqr(event.getPos().getCenter())>36)return;
        var place=LiteraryVignettes.current(p);if(!UncannyRooms.isUncanny(place))return;var b=LiteraryVignettes.base(p,place);var s=p.serverLevel().getBlockState(event.getPos());
        if(place==LabyrinthPlace.PAINTED_INTERIOR&&s.is(UncannyRegistry.PROP.get())&&(s.getValue(UncannyPropBlock.KIND)==UncannyPropBlock.Kind.PAINTED_BOARD||s.getValue(UncannyPropBlock.KIND)==UncannyPropBlock.Kind.SUN_BOARD)||place==LabyrinthPlace.LAYERED_MAN&&s.is(UncannyRegistry.PROP.get())&&s.getValue(UncannyPropBlock.KIND)==UncannyPropBlock.Kind.FLESH){
            var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),place);UncannyStories.click(p,b,event.getPos().subtract(b),place,own);LiteraryVignettes.save(d,p.getUUID(),place,own);event.setCanceled(true);
        }
    }
}
