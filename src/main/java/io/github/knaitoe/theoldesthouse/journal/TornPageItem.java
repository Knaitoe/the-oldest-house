package io.github.knaitoe.theoldesthouse.journal;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * A page torn from a reader's journal (TornPages). Anyone holding it can read it; used on a wall it is pinned there, used on a
 * table top it lies there. Its writer, crouching, tucks it back into its stub. A copy, or a page its writer has taken back, is blank
 * and does not stay in a pocket.
 */
public final class TornPageItem extends Item {
    public TornPageItem(Properties p){super(p);}

    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){
        var stack=player.getItemInHand(hand);
        if(player instanceof ServerPlayer p){
            var ref=TornPages.ref(stack);
            if(ref!=null&&ref.owner().equals(p.getUUID())&&p.isShiftKeyDown()&&TornPages.valid(p.server,ref)){
                if(TornPages.reclaim(p,ref.page()))return InteractionResultHolder.consume(ItemStack.EMPTY);
                p.displayClientMessage(Component.literal("It has to go back into your journal, and you aren't carrying it."),true);
            }else if(!TornPages.read(p,ref,TornPages.nameOf(stack)))stack.setCount(0);
        }
        return InteractionResultHolder.sidedSuccess(stack,level.isClientSide);
    }
    /** On a wall it is pinned; on a table top it lies flat. Anywhere else it is only read. */
    @Override public InteractionResult useOn(UseOnContext c){
        var level=c.getLevel();var face=c.getClickedFace();var support=c.getClickedPos();var player=c.getPlayer();
        if(face==Direction.DOWN||player==null||player.isShiftKeyDown())return InteractionResult.PASS;
        if(!level.getBlockState(support).isFaceSturdy(level,support,face))return InteractionResult.PASS;
        var at=support.relative(face);if(!level.getBlockState(at).getCollisionShape(level,at).isEmpty())return InteractionResult.PASS;
        if(!(level instanceof ServerLevel server)||!(player instanceof ServerPlayer p))return InteractionResult.SUCCESS;
        var stack=c.getItemInHand();var ref=TornPages.ref(stack);var page=TornPages.page(server.getServer(),ref);
        if(page==null){p.displayClientMessage(Component.literal("The page is blank."),true);stack.setCount(0);return InteractionResult.CONSUME;}
        float turn=face==Direction.UP?Direction.fromYRot(p.getYRot()).toYRot():face.toYRot();
        var pinned=PinnedPage.create(server,support,face,turn,ref,TornPages.nameOf(stack),TornPages.glance(page));
        if(!server.getEntitiesOfClass(PinnedPage.class,pinned.getBoundingBox().inflate(.05)).isEmpty()){p.displayClientMessage(Component.literal("There is a page there already."),true);return InteractionResult.CONSUME;}
        TornPages.left(server,ref,TornPages.Where.PINNED,pinned);
        server.addFreshEntity(pinned);stack.shrink(1);
        server.playSound(null,at,SoundEvents.ITEM_FRAME_ADD_ITEM,SoundSource.PLAYERS,.7F,1.3F);
        return InteractionResult.CONSUME;
    }
    /** A blank copy, or a page taken back into its stub, does not stay in anyone's pocket. */
    @Override public void inventoryTick(ItemStack stack,Level level,Entity holder,int slot,boolean selected){
        if(level instanceof ServerLevel server&&server.getGameTime()%20==slot%20&&!TornPages.valid(server.getServer(),TornPages.ref(stack)))stack.setCount(0);
    }
    @Override public Component getName(ItemStack s){
        String name=TornPages.nameOf(s);return name.isEmpty()?super.getName(s):Component.translatable("item.the_oldest_house.torn_page.from",name);
    }
    @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> lines,TooltipFlag f){
        lines.add(Component.translatable("item.the_oldest_house.torn_page.tooltip").withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC));
    }
}
