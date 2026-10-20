package io.github.knaitoe.theoldesthouse.labyrinth;
import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
/** The mirror only wakes after native health was actually lost; cancelled damage never buys sight. */
public final class UncannyArtifactItem extends Item {
    private final boolean mirror;
    public UncannyArtifactItem(Properties p,boolean mirror){super(p);this.mirror=mirror;}
    @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand hand){
        var stack=p.getItemInHand(hand);
        if(p instanceof ServerPlayer reader){
            if(p.isShiftKeyDown()||!mirror)lore(reader,mirror);
            else if(UncannySenses.wakeMirror(reader)){p.startUsingItem(hand);p.getCooldowns().addCooldown(this,20);}
        }
        return InteractionResultHolder.sidedSuccess(stack,l.isClientSide);
    }
    @Override public int getUseDuration(ItemStack s,LivingEntity e){return mirror?72000:0;}
    @Override public UseAnim getUseAnimation(ItemStack s){return mirror?UseAnim.BOW:UseAnim.NONE;}
    @Override public void appendHoverText(ItemStack s,TooltipContext context,List<Component> lines,TooltipFlag flag){
        lines.add(Component.literal(mirror?"A dark face. It takes a little blood.":"Hold in the off hand.").withStyle(net.minecraft.ChatFormatting.GRAY));
        lines.add(Component.literal("Sneak-use: read the tag").withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
    }
    private static void lore(ServerPlayer p,boolean mirror){
        var book=UncannyTexts.artifact(mirror);
        p.openMenu(new SimpleMenuProvider((id,inv,who)->new LecternMenu(id,container(book),new SimpleContainerData(1)){
            @Override public boolean clickMenuButton(Player reader,int button){return reader==p&&button!=3&&super.clickMenuButton(reader,button);}
        },book.getHoverName()));
    }
    private static Container container(ItemStack s){var c=new SimpleContainer(1);c.setItem(0,s);return c;}
}
