package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseExperience;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.Level;

/**
 * 0.4.517, Bike night's keepsake (docs/BIKE_NIGHT_KEEPSAKE_0_4_517.md). The proposal: "The keepsake: Claude suggests the bike itself,
 * owner-bound, rideable in the Overworld and the House with its perk." Gus's page: "there are always some in the rack that nobody
 * comes back for."
 *
 * <p>Whoever rides out by the back gate keeps the bike they rode. It is this, in the hand: one of the six, the one it was. Used, it is
 * under its reader and they are on it, and it rides as it did that night, with the one thing it does that the others do not. Getting
 * off it, or coming off it, it is in the hand again: it is never left standing anywhere, so it cannot be lost or taken.
 *
 * <p>It is ridden in the reader's own world and in the manor, as the other keepsakes that do something are used; not in the House's
 * halls or its scenes. It is its reader's alone: in anyone else's hand it is a bike that will not be got on.
 */
public class KeptBikeItem extends Item {
    /** The keepsake's key with the night's place (LiteraryVignettes.KEY). */
    public static final String KEY="Bike";
    /** Ticks between getting off it and getting on again. */
    public static final int WAIT=20;
    public static final String NOT_HERE="Not in here.",NO_ROOM="There is no room for it here.",NOT_YOURS="It isn't yours.";

    public KeptBikeItem(Properties p){super(p);}

    /** The bike a reader rode out on, as they keep it. Marked as theirs by LiteraryVignettes.reward. */
    public static ItemStack of(NightBike.Make make){
        var s=new ItemStack(BikeNightRegistry.KEPT_BIKE.get());
        s.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(make.ordinal()));
        s.set(DataComponents.ITEM_NAME,Component.literal(make.title()));
        return s;
    }
    /**
     * Which of the six a stack is: its model data, which is what draws it too. (Not a tag of its own: marking a keepsake as its
     * reader's replaces the stack's tags, VignetteYields.mark.)
     */
    public static @Nullable NightBike.Make make(ItemStack s){
        var data=s.get(DataComponents.CUSTOM_MODEL_DATA);var all=NightBike.Make.values();
        return s.is(BikeNightRegistry.KEPT_BIKE.get())&&data!=null&&data.value()>=0&&data.value()<all.length?all[data.value()]:null;
    }
    /** Why it cannot be ridden where this reader is, or null if it can: their own world, or the manor. */
    public static @Nullable String refuse(ServerPlayer p){
        if(LiteraryVignettes.current(p)!=null)return NOT_HERE;
        return Keepsakes.ownWorld(p)||HouseExperience.inManor(p,p.blockPosition())?null:NOT_HERE;
    }

    /** The bike is under its reader, and they are on it. Null if it could not be: they are told why. */
    public static @Nullable NightBike ride(ServerPlayer p,ItemStack s){
        var make=make(s);if(make==null||p.isPassenger()||p.isSpectator())return null;
        if(!Keepsakes.mine(s,p,BikeNight.PLACE,KEY)){NightBikes.cue(p,NOT_YOURS);return null;}
        String no=refuse(p);if(no!=null){NightBikes.cue(p,no);return null;}
        var l=p.serverLevel();var bike=BikeNightRegistry.BIKE.get().create(l);if(bike==null)return null;
        bike.moveTo(p.getX(),p.getY(),p.getZ(),p.getYRot(),0);bike.setUp(p,make,p.getYRot());bike.keep();
        if(!l.noCollision(bike)){bike.discard();NightBikes.cue(p,NO_ROOM);return null;}
        l.addFreshEntity(bike);
        if(!p.startRiding(bike,true)){bike.discard();return null;}
        NightBikes.keys(p,make);
        return bike;
    }

    @Override public InteractionResultHolder<ItemStack> use(Level level,Player p,InteractionHand hand){
        var stack=p.getItemInHand(hand);
        if(level.isClientSide)return InteractionResultHolder.success(stack);
        if(p instanceof ServerPlayer reader&&!p.getCooldowns().isOnCooldown(this)&&ride(reader,stack)!=null){p.getCooldowns().addCooldown(this,WAIT);return InteractionResultHolder.consume(stack);}
        return InteractionResultHolder.pass(stack);
    }
}
