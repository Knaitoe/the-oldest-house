package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWatchers;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * Usher's artifact (0.4.184): a chip of the house's stone with the crack running through it, given to a reader who came
 * through the house's last night, whether they got out over the causeway as it fell or found her standing in the wing.
 * Held to the ear, it lets them hear as Roderick heard: the nearest thing hiding out of their sight answers, once a minute,
 * a knock from where it is, heard by them alone. Nothing answers where nothing hides. It only listens; it changes nothing.
 */
public final class UsherStoneItem extends Item {
    /** How far it hears, and how long it must rest between listenings. */
    public static final int RANGE = 32, REST = 1200;
    public UsherStoneItem(Properties p) { super(p); }

    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer p) {
            player.getCooldowns().addCooldown(this, REST);
            var hiding = nearestHidden(p);
            if (hiding == null) p.displayClientMessage(Component.literal("Nothing answers.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
            else {
                LiteraryVignettes.heard(p, SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, hiding.blockPosition(), .5F, .55F);
                p.displayClientMessage(Component.literal("Something answers.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    /** The nearest living thing within range that the holder cannot see: never a player, and never anyone's own animal. */
    public static Mob nearestHidden(ServerPlayer p) {
        Mob best = null;
        for (var m : p.serverLevel().getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(RANGE), m -> m.isAlive() && !m.isInvisible() && !(m instanceof OwnableEntity own && own.getOwnerUUID() != null))) {
            if (m.distanceToSqr(p) > RANGE * RANGE || HouseWatchers.sees(p, m.getEyePosition())) continue;
            if (best == null || m.distanceToSqr(p) < best.distanceToSqr(p)) best = m;
        }
        return best;
    }

    @Override public void appendHoverText(ItemStack s, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.literal("A chip of the house's stone, the crack running through it.").withStyle(ChatFormatting.GRAY));
        lines.add(Component.literal("Use: hold it to your ear").withStyle(ChatFormatting.DARK_GRAY));
    }
}
