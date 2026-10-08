package io.github.knaitoe.theoldesthouse.labyrinth;
import io.github.knaitoe.theoldesthouse.house.HouseBlocks;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public final class NavigationItems {
    private NavigationItems(){}
    public static final class Chalk extends Item {
        public Chalk(Properties properties){super(properties);}
        @Override public InteractionResult useOn(UseOnContext context) {
            if(!(context.getPlayer() instanceof ServerPlayer player))return InteractionResult.SUCCESS;
            if(!NavigationAids.allowed(player))return InteractionResult.PASS;
            var clicked=context.getClickedPos();
            if(player.isShiftKeyDown()&&player.serverLevel().getBlockState(clicked).is(HouseBlocks.CHALK_MARK.get())) {
                if(!NavigationAids.erase(player,clicked))ExpeditionRhythm.refuse(player,clicked,"chalk_owner","You can only rub out your own chalk mark.");return InteractionResult.CONSUME;
            }
            var pos=clicked.relative(context.getClickedFace());
            if(NavigationAids.placeChalk(player.serverLevel(),pos,context.getClickedFace(),player.getDirection())) {
                NavigationAids.remember(player.serverLevel(),pos,player.getUUID(),true);
                context.getItemInHand().hurtAndBreak(1,player,context.getHand()==InteractionHand.MAIN_HAND?EquipmentSlot.MAINHAND:EquipmentSlot.OFFHAND);
                return InteractionResult.CONSUME;
            }
            ExpeditionRhythm.refuse(player,pos,"chalk_surface","The chalk needs a solid surface and room for its mark.");return InteractionResult.FAIL;
        }
    }
    public static final class Spool extends Item {
        public Spool(Properties properties){super(properties);}
        @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand) {
            if(player instanceof ServerPlayer server)NavigationAids.toggle(server);
            return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand),level.isClientSide());
        }
        @Override public InteractionResult useOn(UseOnContext context) {
            if(context.getPlayer() instanceof ServerPlayer player)NavigationAids.toggle(player);
            return InteractionResult.SUCCESS;
        }
    }
}
