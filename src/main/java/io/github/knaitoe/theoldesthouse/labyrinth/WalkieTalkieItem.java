package io.github.knaitoe.theoldesthouse.labyrinth;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
public final class WalkieTalkieItem extends Item {
    public WalkieTalkieItem(Properties p){super(p);}
    @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand hand){if(p instanceof net.minecraft.server.level.ServerPlayer s)NovelVignettes.radio(s);return InteractionResultHolder.sidedSuccess(p.getItemInHand(hand),l.isClientSide);}
}
