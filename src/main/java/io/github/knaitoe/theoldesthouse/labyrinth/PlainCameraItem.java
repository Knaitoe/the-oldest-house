package io.github.knaitoe.theoldesthouse.labyrinth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
/** Hold the camera to compose an exposure. Its viewfinder and picture belong to the holder. */
public final class PlainCameraItem extends Item {
    public PlainCameraItem(Properties properties){super(properties);}
    @Override public UseAnim getUseAnimation(ItemStack stack){return UseAnim.SPYGLASS;}
    @Override public int getUseDuration(ItemStack stack,LivingEntity reader){return 72000;}
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player reader,InteractionHand hand){reader.startUsingItem(hand);return InteractionResultHolder.consume(reader.getItemInHand(hand));}
}
