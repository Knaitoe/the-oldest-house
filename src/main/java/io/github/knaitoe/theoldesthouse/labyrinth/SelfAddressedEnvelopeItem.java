package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * What the institute gives the reader whose letters came home (0.4.66): an envelope already addressed to them. Inside the
 * House it returns its sender to the manor, once a day, from any hall but never out of a room's story. Outside, it holds
 * one sealed stack, and if its keeper dies away from the House it is waiting for them when they wake. It works only for
 * the reader it was addressed to.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class SelfAddressedEnvelopeItem extends Item {
    /** Per reader: the in-game day of the last return, and anything held for them across a death outside. */
    public static final String STATE="envelope_0466",SEALED="Sealed";
    public SelfAddressedEnvelopeItem(Properties properties){super(properties);}

    public static ItemStack forReader(UUID reader){
        var stack=VignetteYields.mark(new ItemStack(NovelRegistry.ENVELOPE.get()),LabyrinthPlace.WHALE.id());
        CustomData.update(DataComponents.CUSTOM_DATA,stack,t->t.putUUID(LiteraryVignettes.OWNER,reader));return stack;
    }
    static boolean ownedBy(ItemStack stack,Player p){var t=stack.get(DataComponents.CUSTOM_DATA);return t!=null&&t.copyTag().hasUUID(LiteraryVignettes.OWNER)&&t.copyTag().getUUID(LiteraryVignettes.OWNER).equals(p.getUUID());}
    static boolean sealed(ItemStack stack){var t=stack.get(DataComponents.CUSTOM_DATA);return t!=null&&t.copyTag().contains(SEALED);}

    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){
        var stack=player.getItemInHand(hand);
        if(player instanceof ServerPlayer p){player.getCooldowns().addCooldown(this,20);
            if(!ownedBy(stack,p))p.displayClientMessage(Component.literal("It is addressed to someone else."),true);
            else if(p.isShiftKeyDown()&&sealed(stack))open(p,stack);
            else if(HouseDimensions.isHouseDimension(p.level().dimension()))recall(p);
            else if(!p.isShiftKeyDown())seal(p,stack,hand);
            else p.displayClientMessage(Component.literal("It is empty."),true);}
        return InteractionResultHolder.sidedSuccess(stack,level.isClientSide());
    }

    // ------------------------------------------------------------------------------------------------ inside: return to sender
    /** Why the envelope will not go now, or null when it may. */
    static String refusal(ServerPlayer p){
        if(p.gameMode.getGameModeForPlayer()==GameType.SPECTATOR||!p.isAlive())return "Nothing happens.";
        var origin=HouseSavedData.get(p.server).houseOrigin();
        if(origin==null||!p.level().dimension().equals(HouseDimensions.INTERIOR))return "There is no return address here.";
        if(FinaleProgress.committed(FinaleProgress.phase(p.server,p.getUUID()))||FinaleArchitecture.contains(origin,p.blockPosition()))return "The envelope will not go anywhere from here.";
        if(LabyrinthDoors.isBusy(p)||LiteraryVignettes.retreatLocked(p)||StaircaseLeaks.active(p))return "Not now.";
        var place=LabyrinthPlaces.placeAt(origin,p.blockPosition());
        if(place!=null&&place.isVignette()||LiteraryCopies.placeAt(p.server,p.blockPosition())!=null||NovelVignettes.current(p)!=null)return "Not from inside a room. Find a hall.";
        var own=record(p);if(own.contains("RecallDay")&&own.getLong("RecallDay")>=day(p))return "It has already come back once today.";
        return null;
    }
    static boolean recall(ServerPlayer p){
        String no=refusal(p);if(no!=null){p.displayClientMessage(Component.literal(no),true);return false;}
        var origin=HouseSavedData.get(p.server).houseOrigin();var data=LabyrinthData.get(p.server);
        var own=record(p);own.putLong("RecallDay",day(p));save(p,own);
        // As if every door had been walked back through: the way back is spent.
        data.clearReturns(p.getUUID());
        HouseInternalTeleport.shift(p,HideAndClap.manorRespawn(origin),180);
        p.serverLevel().playSound(null,p.blockPosition(),SoundEvents.BOOK_PAGE_TURN,SoundSource.PLAYERS,.8F,.7F);
        p.displayClientMessage(Component.literal("Returned to sender."),true);
        return true;
    }
    private static long day(ServerPlayer p){return p.server.overworld().getDayTime()/24000;}

    // ------------------------------------------------------------------------------------------------ outside: one sealed stack
    static void seal(ServerPlayer p,ItemStack envelope,InteractionHand hand){
        if(sealed(envelope)){p.displayClientMessage(Component.literal("It is already sealed. Sneak to open it."),true);return;}
        var other=p.getItemInHand(hand==InteractionHand.MAIN_HAND?InteractionHand.OFF_HAND:InteractionHand.MAIN_HAND);
        if(other.isEmpty()){p.displayClientMessage(Component.literal("Hold what you want to send in your other hand."),true);return;}
        if(other.getItem() instanceof SelfAddressedEnvelopeItem||other.has(DataComponents.CONTAINER)||other.has(DataComponents.BUNDLE_CONTENTS)){
            p.displayClientMessage(Component.literal("It will not close over that."),true);return;}
        var contents=other.copy();other.setCount(0);
        CustomData.update(DataComponents.CUSTOM_DATA,envelope,t->t.put(SEALED,contents.save(p.registryAccess())));
        p.serverLevel().playSound(null,p.blockPosition(),SoundEvents.BOOK_PUT,SoundSource.PLAYERS,.7F,1.2F);
        p.displayClientMessage(Component.literal("Sealed, and addressed to you."),true);
    }
    static void open(ServerPlayer p,ItemStack envelope){
        var contents=contents(p,envelope);
        if(contents.isEmpty()){p.displayClientMessage(Component.literal("It is empty."),true);return;}
        CustomData.update(DataComponents.CUSTOM_DATA,envelope,t->t.remove(SEALED));
        NovelVignettes.give(p,contents);
        p.serverLevel().playSound(null,p.blockPosition(),SoundEvents.BOOK_PAGE_TURN,SoundSource.PLAYERS,.7F,1.1F);
    }
    static ItemStack contents(ServerPlayer p,ItemStack envelope){
        var t=envelope.get(DataComponents.CUSTOM_DATA);if(t==null||!t.copyTag().contains(SEALED))return ItemStack.EMPTY;
        return ItemStack.parseOptional(p.registryAccess(),t.copyTag().getCompound(SEALED));
    }

    /** A sealed envelope its keeper dies with outside the House does not fall with them. */
    @SubscribeEvent(priority=EventPriority.HIGH) public static void drops(LivingDropsEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p)||HouseDimensions.isHouseDimension(p.level().dimension()))return;
        var own=record(p);var held=own.getList("Held",Tag.TAG_COMPOUND);boolean kept=false;
        for(var it=e.getDrops().iterator();it.hasNext();){var item=it.next();var stack=item.getItem();
            if(stack.getItem() instanceof SelfAddressedEnvelopeItem&&ownedBy(stack,p)&&sealed(stack)){held.add(stack.save(p.registryAccess()));it.remove();kept=true;}}
        if(kept){own.put("Held",held);save(p,own);}
    }
    /** On waking it is in their hands again, opened: what it carried beside it, and the envelope ready to be sealed again. */
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p)||e.isEndConquered())return;
        var own=record(p);var held=own.getList("Held",Tag.TAG_COMPOUND);if(held.isEmpty())return;
        own.remove("Held");save(p,own);
        for(int i=0;i<held.size();i++){var envelope=ItemStack.parseOptional(p.registryAccess(),held.getCompound(i));if(envelope.isEmpty())continue;
            var contents=contents(p,envelope);CustomData.update(DataComponents.CUSTOM_DATA,envelope,t->t.remove(SEALED));
            NovelVignettes.give(p,envelope);if(!contents.isEmpty())NovelVignettes.give(p,contents);}
        p.displayClientMessage(Component.literal("The envelope came back to you. It will need sealing again."),true);
    }

    public static CompoundTag record(ServerPlayer p){return LabyrinthData.get(p.server).state(STATE).getCompound(p.getUUID().toString()).copy();}
    private static void save(ServerPlayer p,CompoundTag own){var data=LabyrinthData.get(p.server);var all=data.state(STATE);all.put(p.getUUID().toString(),own);data.setState(STATE,all);}

    @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<Component> lines,TooltipFlag flag){
        var t=stack.get(DataComponents.CUSTOM_DATA);var registries=context.registries();
        if(t!=null&&t.copyTag().contains(SEALED)&&registries!=null){var inside=ItemStack.parseOptional(registries,t.copyTag().getCompound(SEALED));
            if(!inside.isEmpty())lines.add(Component.literal("Sealed: "+inside.getCount()+" × "+inside.getHoverName().getString()).withStyle(ChatFormatting.GRAY));}
        lines.add(Component.literal("In the House, once a day, it returns its sender to the manor.").withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC));
        lines.add(Component.literal("Outside, it holds one sealed stack, and finds you again if you die.").withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC));
        lines.add(Component.literal("Addressed in your own hand.").withStyle(ChatFormatting.DARK_GRAY));
    }
}
