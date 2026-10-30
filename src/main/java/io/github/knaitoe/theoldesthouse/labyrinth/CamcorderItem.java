package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import java.util.List;
import net.minecraft.world.level.Level;

/**
 * A wide documentary lens. Like the canoe's phone, one use starts it rolling and the next stops it; while it is in
 * its owner's hand and rolling, it records only what their actual camera can see.
 */
public final class CamcorderItem extends Item {
    public static final String ROLLING="Rolling0576";
    public CamcorderItem(Properties p) { super(p); }
    @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> text,TooltipFlag f){text.add(Component.literal(rolling(s)?"Rolling. Use it again to stop. Put it away to talk.":"Use it to start filming the set, and again to stop. Put it away to talk."));}
    @Override public UseAnim getUseAnimation(ItemStack s) { return UseAnim.NONE; }
    // Held down, the button stays one use: it starts or stops once rather than flickering.
    @Override public int getUseDuration(ItemStack s, LivingEntity p) { return 72000; }
    @Override public InteractionResultHolder<ItemStack> use(Level l, Player p, InteractionHand hand) {
        var stack=p.getItemInHand(hand);
        if(p instanceof ServerPlayer reader)toggle(reader,stack);
        p.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }
    /** The set is the only place it films; carried out of it, it stops. */
    @Override public void inventoryTick(ItemStack stack,Level level,Entity holder,int slot,boolean selected){
        if(!level.isClientSide&&holder instanceof ServerPlayer p&&rolling(stack)&&!LiteraryVignettes.inside(p,LabyrinthPlace.GHOSTS_SET))setRolling(stack,false);
    }
    public static boolean rolling(ItemStack s){
        var custom=s.get(DataComponents.CUSTOM_DATA);
        return s.getItem() instanceof CamcorderItem&&custom!=null&&custom.copyTag().getBoolean(ROLLING);
    }
    public static void setRolling(ItemStack s,boolean on){CustomData.update(DataComponents.CUSTOM_DATA,s,t->{if(on)t.putBoolean(ROLLING,true);else t.remove(ROLLING);});}
    public static void toggle(ServerPlayer p,ItemStack s){
        if(!(s.getItem() instanceof CamcorderItem))return;
        boolean on=!rolling(s)&&LiteraryVignettes.inside(p,LabyrinthPlace.GHOSTS_SET);
        if(!on&&!rolling(s)){p.displayClientMessage(Component.literal("There is nothing here to film."),true);return;}
        setRolling(s,on);
        p.serverLevel().playSound(null,p.blockPosition(),DrownedTownRegistry.PHONE_RECORD.get(),SoundSource.PLAYERS,.55F,on?1F:.75F);
        p.displayClientMessage(Component.literal(on?"Recording.":"Stopped recording."),true);
    }
    /** Whether this living reader has their own camera in hand, rolling. */
    public static boolean filming(Player p){
        if(p.isSpectator()||!p.isAlive())return false;
        for(var hand:InteractionHand.values()){
            var s=p.getItemInHand(hand);if(!rolling(s))continue;
            var t=s.get(DataComponents.CUSTOM_DATA).copyTag();
            if(t.hasUUID(LiteraryVignettes.OWNER)&&t.getUUID(LiteraryVignettes.OWNER).equals(p.getUUID()))return true;
        }
        return false;
    }
}
