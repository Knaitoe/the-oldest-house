package io.github.knaitoe.theoldesthouse.labyrinth;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
/** Actual native inventory transfer is the key ending and the immediate cleaver prompt. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class CellarActions {
    private CellarActions(){}
    @SubscribeEvent public static void pickup(ItemEntityPickupEvent.Post e){
        if(!(e.getPlayer() instanceof ServerPlayer p)||!LiteraryVignettes.inside(p,LabyrinthPlace.LAYERED_MAN))return;
        var item=e.getOriginalStack();if(!item.is(UncannyRegistry.CLEAVER.get())&&!item.is(UncannyRegistry.CELLAR_KEY.get())&&!item.is(LiteraryRegistry.KEY.get()))return;
        var b=LiteraryVignettes.base(p,LabyrinthPlace.LAYERED_MAN);var data=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(data,p.getUUID(),LabyrinthPlace.LAYERED_MAN);
        CellarDialogue.actions(p,b,own,UncannyStories.world(p.serverLevel(),b,LabyrinthPlace.LAYERED_MAN));LiteraryVignettes.save(data,p.getUUID(),LabyrinthPlace.LAYERED_MAN,own);
    }
}
