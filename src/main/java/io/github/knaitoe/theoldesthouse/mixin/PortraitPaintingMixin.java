package io.github.knaitoe.theoldesthouse.mixin;

import io.github.knaitoe.theoldesthouse.house.PortraitPainting;
import io.github.knaitoe.theoldesthouse.house.PortraitProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.decoration.Painting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Keep the actual painting, its UUID, backing, frame, collision, variant and native tracking. */
@Mixin(Painting.class)
public abstract class PortraitPaintingMixin implements PortraitPainting {
    @Unique private static final EntityDataAccessor<String> HOUSE_PORTRAIT =
            SynchedEntityData.defineId(Painting.class, EntityDataSerializers.STRING);
    @Unique private static final String HOUSE_PORTRAIT_NBT = "HouseFirstDeathPortrait04111";

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void house$definePortrait(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(HOUSE_PORTRAIT, "");
    }

    @Override public String housePortrait() {
        return ((Painting) (Object) this).getEntityData().get(HOUSE_PORTRAIT);
    }

    @Override public void housePortrait(String encoded) {
        ((Painting) (Object) this).getEntityData().set(HOUSE_PORTRAIT,
                PortraitProfile.decode(encoded) == null ? "" : encoded);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void house$savePortrait(CompoundTag tag, CallbackInfo ci) {
        if (!housePortrait().isEmpty()) tag.putString(HOUSE_PORTRAIT_NBT, housePortrait());
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void house$loadPortrait(CompoundTag tag, CallbackInfo ci) {
        housePortrait(tag.getString(HOUSE_PORTRAIT_NBT));
    }
}
