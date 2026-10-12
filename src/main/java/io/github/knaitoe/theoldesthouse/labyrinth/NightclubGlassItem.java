package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** A finite glass leaves the counter, is drunk through native item use, and can be put back empty. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class NightclubGlassItem extends Item {
    public NightclubGlassItem(Properties properties){super(properties);}
    public static boolean empty(ItemStack stack){return stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getBoolean("Empty");}
    public static ItemStack glass(boolean empty){
        ItemStack stack=new ItemStack(NightclubRegistry.GLASS.get());
        CompoundTag tag=new CompoundTag();tag.putBoolean("Empty",empty);stack.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));return stack;
    }
    @Override public String getDescriptionId(ItemStack stack){return empty(stack)?"item.the_oldest_house.nightclub_glass_empty":super.getDescriptionId(stack);}
    @Override public UseAnim getUseAnimation(ItemStack stack){return UseAnim.DRINK;}
    @Override public int getUseDuration(ItemStack stack,LivingEntity entity){return 32;}
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){
        ItemStack held=player.getItemInHand(hand);
        if(empty(held))return InteractionResultHolder.pass(held);
        player.startUsingItem(hand);return InteractionResultHolder.consume(held);
    }
    @Override public ItemStack finishUsingItem(ItemStack stack,Level level,LivingEntity entity){
        if(empty(stack))return stack;
        ItemStack result=stack.copy();CompoundTag tag=result.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
        tag.putBoolean("Empty",true);result.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
        if(!level.isClientSide){entity.gameEvent(GameEvent.DRINK);level.playSound(null,entity.blockPosition(),SoundEvents.GENERIC_DRINK,SoundSource.PLAYERS,.4F,1F);}
        return result;
    }
    @Override public InteractionResult useOn(UseOnContext context){
        if(!(context.getPlayer() instanceof ServerPlayer player)||!player.isAlive()||player.isSpectator()||!Nightclub.inside(player)||context.getClickedFace()!=Direction.UP)return InteractionResult.PASS;
        var at=context.getClickedPos();var level=player.serverLevel();var support=level.getBlockState(at);
        double reach=player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);
        if(player.getEyePosition().distanceToSqr(context.getClickLocation())>reach*reach||!support.is(NightclubRegistry.BLOCK.get())||support.getValue(NightclubBlock.KIND)!=NightclubBlock.Kind.BAR||!level.getBlockState(at.above()).isAir())return InteractionResult.PASS;
        ItemStack held=context.getItemInHand();
        if(!held.is(NightclubRegistry.GLASS.get()))return InteractionResult.PASS;
        var placed=NightclubBlock.of(NightclubBlock.Kind.GLASS,support.getValue(NightclubBlock.FACING)).setValue(NightclubBlock.STAGE,empty(held)?0:1);
        if(!level.setBlock(at.above(),placed,3))return InteractionResult.PASS;
        held.shrink(1);level.playSound(null,at,SoundEvents.GLASS_PLACE,SoundSource.BLOCKS,.25F,1.3F);return InteractionResult.SUCCESS;
    }
    @SubscribeEvent(priority=EventPriority.HIGH) public static void place(PlayerInteractEvent.RightClickBlock event){
        if(event.isCanceled()||!(event.getEntity() instanceof ServerPlayer player)||!Nightclub.inside(player)||!event.getItemStack().is(NightclubRegistry.GLASS.get()))return;
        // Adventure's generic CanPlaceOn check does not grant permission to edit the room.
        // This item can only return its own finite glass to an empty native bar top.
        InteractionResult result=event.getItemStack().getItem().useOn(new UseOnContext(player,event.getHand(),event.getHitVec()));
        if(result.consumesAction()){event.setCanceled(true);event.setCancellationResult(result);}
    }
}
