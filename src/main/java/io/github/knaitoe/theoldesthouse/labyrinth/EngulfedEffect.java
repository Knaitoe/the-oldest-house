package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.ai.attributes.*;

/** The reader can still cut and move while the tissue closes over them. */
public final class EngulfedEffect extends MobEffect {
    public EngulfedEffect(){super(MobEffectCategory.HARMFUL,0xba7d79);addAttributeModifier(Attributes.MOVEMENT_SPEED,ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"engulfed"),-.08,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);}
}
