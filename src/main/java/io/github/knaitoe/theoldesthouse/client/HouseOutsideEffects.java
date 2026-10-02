package io.github.knaitoe.theoldesthouse.client;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;

/** Pocket landscapes sit below overworld sea level; never draw the black lower-sky hemisphere. */
public final class HouseOutsideEffects extends DimensionSpecialEffects {
    public HouseOutsideEffects(){super(Float.NaN,false,SkyType.NORMAL,false,false);}
    @Override public Vec3 getBrightnessDependentFogColor(Vec3 color,float daylight){return color.multiply(daylight*.94F+.06F,daylight*.94F+.06F,daylight*.91F+.09F);}
    @Override public boolean isFoggyAt(int x,int z){return false;}
    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Registration {
        @SubscribeEvent public static void register(RegisterDimensionSpecialEffectsEvent e){e.register(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"outside"),new HouseOutsideEffects());}
    }
}
