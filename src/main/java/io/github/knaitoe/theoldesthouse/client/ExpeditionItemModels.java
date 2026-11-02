package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryRegistry;
import io.github.knaitoe.theoldesthouse.labyrinth.NovelRegistry;
import io.github.knaitoe.theoldesthouse.labyrinth.WitheredPawItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Native baked shield geometry, with a real blocking model in both hands; and (0.4.147) the paw's finger curling down in
 * the hand that holds it, from the moment its stack carries, so everyone who can see that hand sees it happen.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class ExpeditionItemModels {
    @SubscribeEvent public static void setup(FMLClientSetupEvent event){event.enqueueWork(()->{
        ItemProperties.register(NovelRegistry.HOLLOWAY_SHIELD.get(),ResourceLocation.withDefaultNamespace("blocking"),
            (stack,level,entity,seed)->entity!=null&&entity.isUsingItem()&&entity.getUseItem()==stack?1:0);
        ItemProperties.register(LiteraryRegistry.PAW.get(),ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"clench"),(stack,level,entity,seed)->{
            var world=level!=null?level:entity!=null?entity.level():Minecraft.getInstance().level;return world==null?0:WitheredPawItem.clench(stack,world.getGameTime());});
        // 0.4.262: Nell's compass is a small brass thing in the hand now, and its needle points north whichever way its holder turns; it
        // never quite settles. Sixteen positions (models/item/borrasca_compass_*.json); on a shelf or in a frame it rests pointing up.
        ItemProperties.register(io.github.knaitoe.theoldesthouse.labyrinth.BorrascaRegistry.COMPASS.get(),ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"needle"),(stack,level,entity,seed)->{
            if(entity==null)return 0;var world=level!=null?level:entity.level();double time=world==null?0:world.getGameTime();
            double turn=(entity.getVisualRotationYInDegrees()-180)/360.0+.012*Math.sin(time*.13+seed)+.006*Math.sin(time*.41);
            return (float)(turn-Math.floor(turn));});
    });}
}
