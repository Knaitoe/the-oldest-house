package io.github.knaitoe.theoldesthouse.client.mixin;

import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 0.4.163 (owner: "remove the player character from the inventory menu while in the house so the player only sees
 * themselves through the eyes of others or reflected in mirrors"): inside the House the inventory's figure is left empty.
 * Every other entity drawn there (a horse, a llama) still is, and the reader's own figure returns in the manor.
 */
@Mixin(InventoryScreen.class)
public abstract class InventorySelfMixin {
    @Inject(method = "renderEntityInInventory(Lnet/minecraft/client/gui/GuiGraphics;FFFLorg/joml/Vector3f;Lorg/joml/Quaternionf;Lorg/joml/Quaternionf;Lnet/minecraft/world/entity/LivingEntity;)V", at = @At("HEAD"), cancellable = true)
    private static void house$noSelf(GuiGraphics graphics, float x, float y, float scale, Vector3f translate, Quaternionf pose,
                                     Quaternionf camera, LivingEntity entity, CallbackInfo ci) {
        var mc = Minecraft.getInstance();
        if (mc.player != null && entity == mc.player && HouseDimensions.isHouseDimension(mc.player.level().dimension())) ci.cancel();
    }
}
