package io.github.knaitoe.theoldesthouse.labyrinth;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** The original meal moves into a persistent table fitting, then into a hand or an actual bite. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class DinerPlate {
    private static final String BITE="DinerPlateBite133";
    private DinerPlate(){}
    public static UncannyPropBlock.Fitting at(ServerLevel l,BlockPos at){
        return l.getBlockEntity(at) instanceof UncannyPropBlock.Fitting f&&f.getBlockState().getValue(UncannyPropBlock.KIND)==UncannyPropBlock.Kind.PLATE?f:null;
    }
    private static boolean owner(ServerPlayer p,ItemStack s){var c=s.get(DataComponents.CUSTOM_DATA);return c==null||!c.copyTag().hasUUID("UncannyMealOwner")||c.copyTag().getUUID("UncannyMealOwner").equals(p.getUUID());}
    private static boolean space(ServerLevel l,BlockPos at){
        if(!l.hasChunkAt(at)||!l.isEmptyBlock(at))return false;var below=at.below();
        return l.getBlockState(below).isFaceSturdy(l,below,Direction.UP);
    }
    public static boolean place(ServerLevel l,BlockPos at,ItemStack meal){
        if(meal.isEmpty()||!meal.is(UncannyRegistry.MEAL.get())||!space(l,at))return false;
        if(!l.setBlock(at,UncannyPropBlock.of(UncannyPropBlock.Kind.PLATE,Direction.NORTH),UncannyRooms.FLAGS))return false;
        var f=at(l,at);if(f==null){l.setBlock(at,Blocks.AIR.defaultBlockState(),UncannyRooms.FLAGS);return false;}
        f.meal=meal.copyWithCount(1);meal.shrink(1);f.syncMotion();l.playSound(null,at,SoundEvents.STONE_PLACE,SoundSource.BLOCKS,.25F,1.6F);return true;
    }
    public static BlockPos serve(ServerPlayer p,BlockPos b,ItemStack meal){
        var candidates=new ArrayList<BlockPos>();for(int x:new int[]{5,6})for(int z:new int[]{-19,-15,-11,-7})candidates.add(b.offset(x,1,z));
        candidates.sort(Comparator.comparingDouble(at->at.distSqr(b.offset(5,1,-19))));
        for(var at:candidates)if(place(p.serverLevel(),at,meal))return at;return null;
    }
    public static boolean use(ServerPlayer p,BlockPos at){
        var f=at(p.serverLevel(),at);if(f==null||f.meal.isEmpty()||!LiteraryVignettes.participant(p)||p.containerMenu!=p.inventoryMenu||p.distanceToSqr(at.getCenter())>36||!owner(p,f.meal))return false;
        if(p.isUsingItem()||p.getPersistentData().contains(BITE))return false;var meal=f.meal;
        if(p.isShiftKeyDown()){f.meal=ItemStack.EMPTY;p.serverLevel().setBlock(at,Blocks.AIR.defaultBlockState(),UncannyRooms.FLAGS);LiteraryVignettes.give(p,meal);return true;}
        var context=new net.minecraft.nbt.CompoundTag();context.putLong("Plate",at.asLong());context.putInt("HandSlot",p.getInventory().selected);context.putInt("Stash",-1);
        var custom=meal.get(DataComponents.CUSTOM_DATA);if(custom==null||!custom.copyTag().hasUUID("UncannyMeal"))return false;
        context.putUUID("Meal",custom.copyTag().getUUID("UncannyMeal"));var held=p.getMainHandItem();
        if(!held.isEmpty()){
            int stash=p.getInventory().getFreeSlot();if(stash<0){p.displayClientMessage(net.minecraft.network.chat.Component.literal("Your hands are full."),true);return false;}
            context.putInt("Stash",stash);context.put("StashStack",held.save(p.registryAccess()));p.getInventory().setItem(stash,held);
        }
        f.meal=ItemStack.EMPTY;f.syncMotion();p.getPersistentData().put(BITE,context);p.setItemInHand(InteractionHand.MAIN_HAND,meal);p.getInventory().setChanged();p.inventoryMenu.broadcastChanges();p.startUsingItem(InteractionHand.MAIN_HAND);
        var values=p.getEntityData().getNonDefaultValues();if(values!=null)p.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket(p.getId(),values));return true;
    }
    private static boolean sameMeal(ItemStack meal,net.minecraft.nbt.CompoundTag context){var c=meal.get(DataComponents.CUSTOM_DATA);return c!=null&&c.copyTag().hasUUID("UncannyMeal")&&context.hasUUID("Meal")&&c.copyTag().getUUID("UncannyMeal").equals(context.getUUID("Meal"));}
    /** Return the actual remaining serving and restore only an unchanged displaced hand object. */
    public static ItemStack finishBite(ServerPlayer p,ItemStack remainder){
        var context=p.getPersistentData().getCompound(BITE);if(context.isEmpty())return remainder;p.getPersistentData().remove(BITE);
        if(p.getInventory().selected!=context.getInt("HandSlot"))return remainder;
        var at=BlockPos.of(context.getLong("Plate"));boolean returned=false;
        if(LiteraryVignettes.inside(p,LabyrinthPlace.SECOND_WAITRESS)&&p.distanceToSqr(at.getCenter())<=36){
            var plate=at(p.serverLevel(),at);
            if(plate!=null&&plate.meal.isEmpty()&&(remainder.isEmpty()||sameMeal(remainder,context))){
                if(remainder.isEmpty())p.serverLevel().setBlock(at,Blocks.AIR.defaultBlockState(),UncannyRooms.FLAGS);else{plate.meal=remainder;plate.syncMotion();}returned=true;
            }
        }
        if(!returned&&!remainder.isEmpty())return remainder;int stash=context.getInt("Stash");
        if(stash>=0&&stash<p.getInventory().getContainerSize()){
            var held=p.getInventory().getItem(stash);var original=ItemStack.parseOptional(p.registryAccess(),context.getCompound("StashStack"));
            if(ItemStack.matches(held,original)){p.getInventory().setItem(stash,ItemStack.EMPTY);return held;}
        }return ItemStack.EMPTY;
    }
    @SubscribeEvent public static void recoverInterruptedBites(net.neoforged.neoforge.event.tick.ServerTickEvent.Post e){
        for(var p:e.getServer().getPlayerList().getPlayers())if(p.getPersistentData().contains(BITE)&&!p.isUsingItem()){
            var c=p.getPersistentData().getCompound(BITE);var hand=p.getMainHandItem();
            if(sameMeal(hand,c)&&p.getInventory().selected==c.getInt("HandSlot")){p.setItemInHand(InteractionHand.MAIN_HAND,finishBite(p,hand));p.getInventory().setChanged();p.inventoryMenu.broadcastChanges();}
            else p.getPersistentData().remove(BITE);
        }
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void left(PlayerInteractEvent.LeftClickBlock e){
        if(e.getAction()!=PlayerInteractEvent.LeftClickBlock.Action.START||!(e.getEntity() instanceof ServerPlayer p)||!LiteraryVignettes.participant(p)||p.containerMenu!=p.inventoryMenu||p.distanceToSqr(e.getPos().getCenter())>36)return;
        var held=p.getMainHandItem();boolean handled=false;
        if(held.is(UncannyRegistry.MEAL.get())&&owner(p,held)){
            var target=p.serverLevel().isEmptyBlock(e.getPos())?e.getPos():e.getFace()==Direction.UP?e.getPos().above():null;
            if(target!=null)handled=place(p.serverLevel(),target,held);
        }else{var f=at(p.serverLevel(),e.getPos());if(f!=null&&!f.meal.isEmpty()&&owner(p,f.meal)){
            var original=f.meal;f.meal=ItemStack.EMPTY;p.serverLevel().setBlock(e.getPos(),Blocks.AIR.defaultBlockState(),UncannyRooms.FLAGS);LiteraryVignettes.give(p,original);handled=true;
        }}
        if(handled){p.getInventory().setChanged();p.inventoryMenu.broadcastChanges();e.setCanceled(true);}
    }
    /** Transfer an old floating original once, preserving its exact native meal components. */
    public static void migrate(ServerPlayer p,BlockPos b,CompoundTag own){
        if(!own.getBoolean("MealServed")||own.contains("PlatePos131")||!own.hasUUID("PlateEntity"))return;
        if(p.serverLevel().getEntity(own.getUUID("PlateEntity")) instanceof ItemEntity old&&owner(p,old.getItem())){
            var target=serve(p,b,old.getItem());if(target!=null){own.putLong("PlatePos131",target.asLong());old.discard();}
        }
    }
}
