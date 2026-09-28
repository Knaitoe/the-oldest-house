package io.github.knaitoe.theoldesthouse.opening;

import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidType;

/**
 * An item left for one player: it never despawns, only its recipient can
 * pick it up, and running water does not carry it off the doorstep.
 */
public final class DeliveredItemEntity extends ItemEntity {
    public DeliveredItemEntity(EntityType<? extends DeliveredItemEntity> type, Level level) {
        super(type, level);
    }

    public static DeliveredItemEntity create(ServerLevel level, ItemStack stack, Vec3 pos, UUID recipient) {
        DeliveredItemEntity entity = new DeliveredItemEntity(OpeningRegistry.DELIVERED_ITEM.get(), level);
        entity.setItem(stack);
        entity.moveTo(pos.x, pos.y, pos.z, level.getRandom().nextFloat() * 360.0F, 0.0F);
        entity.setDeltaMovement(Vec3.ZERO);
        entity.setUnlimitedLifetime();
        entity.setNoPickUpDelay();
        entity.setTarget(recipient);
        return entity;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean isPushedByFluid(FluidType type) {
        return false;
    }
}
