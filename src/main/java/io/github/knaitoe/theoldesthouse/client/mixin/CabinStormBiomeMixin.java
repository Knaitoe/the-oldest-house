package io.github.knaitoe.theoldesthouse.client.mixin;
import io.github.knaitoe.theoldesthouse.client.CabinStormClient;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The outside pockets stand in a biome without weather; a reader's own storm rains there, on the render thread only. */
@Mixin(Biome.class)
public abstract class CabinStormBiomeMixin {
    @Inject(method="hasPrecipitation",at=@At("RETURN"),cancellable=true)
    private void house$cabinStorm(CallbackInfoReturnable<Boolean> cir){
        // 0.4.767: on the render thread only, as the line above says. Biome is shared with the server a single-player or LAN
        // game runs inside the client, and two of these asked nothing about the thread: while the host sat in the second
        // waitress's diner every dry biome counted as rained on for the server too (lightning, fires put out, cauldrons
        // filling), and the car among the pines read the client's world from the server's thread.
        if(!com.mojang.blaze3d.systems.RenderSystem.isOnRenderThread())return;
        if(!cir.getReturnValueZ()&&(CabinStormClient.precipitating()||io.github.knaitoe.theoldesthouse.client.DeerRoadClient.precipitating()||io.github.knaitoe.theoldesthouse.client.LongerClient.precipitating()||io.github.knaitoe.theoldesthouse.client.StateRouteClient.precipitating()||io.github.knaitoe.theoldesthouse.client.StormFieldClient.precipitating()||io.github.knaitoe.theoldesthouse.client.BackseatClient.precipitating()||io.github.knaitoe.theoldesthouse.client.UncannyClient.room().equals("second_waitress")))cir.setReturnValue(true);}
}
