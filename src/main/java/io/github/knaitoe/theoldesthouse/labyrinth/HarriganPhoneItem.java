package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * The player's phone from the Harrigan vignette.
 *
 * It deliberately uses the Overworld clock instead of the holder's dimension
 * clock. Inside the House, where a vanilla clock is useless, the item's name
 * still advances with the time back home.
 */
public final class HarriganPhoneItem extends Item {
    public HarriganPhoneItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean selected) {
        if (!(entity instanceof ServerPlayer player) || level.isClientSide || player.tickCount % 20 != 0) {
            return;
        }
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Phone · " + homeTime(player.server.overworld().getDayTime()))
                        .withStyle(style -> style.withItalic(false).withColor(ChatFormatting.GRAY)));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            HarriganVignette.beginPhone(serverPlayer, stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("The time shown is the time back home.")
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal("Use it, then type a name in chat.")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    static String homeTime(long dayTime) {
        long ticks = Math.floorMod(dayTime, 24000L);
        int hour = (int) ((ticks / 1000L + 6L) % 24L);
        int minute = (int) ((ticks % 1000L) * 60L / 1000L);
        return String.format(Locale.ROOT, "%02d:%02d", hour, minute);
    }
}
