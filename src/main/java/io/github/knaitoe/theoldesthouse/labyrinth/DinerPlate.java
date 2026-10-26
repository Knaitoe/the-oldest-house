package io.github.knaitoe.theoldesthouse.labyrinth;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.InteractionResult;
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
        var meal=f.meal;f.meal=ItemStack.EMPTY;
        if(p.isShiftKeyDown())LiteraryVignettes.give(p,meal);else{UncannyStories.ate(p,meal);p.getFoodData().eat(1,0);p.serverLevel().playSound(null,at,SoundEvents.GENERIC_EAT,SoundSource.PLAYERS,.55F,.9F+p.getRandom().nextFloat()*.2F);}
        p.serverLevel().setBlock(at,Blocks.AIR.defaultBlockState(),UncannyRooms.FLAGS);return true;
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
