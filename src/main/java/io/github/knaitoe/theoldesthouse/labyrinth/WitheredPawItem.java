package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.Level;

/**
 * Three Fingers (0.4.81): the old soldier's withered paw. Since 0.4.147 there is one for each thread, bound to whoever
 * pulled it from the fire, and it counts down its fingers as they wish. Only the paw last pulled from the fire answers:
 * one lost and pulled out again leaves the old one a dried paw and nothing more, so there is never a second paw to wish on.
 * <p>Its art is drawn in code by tools/draw_three_fingers_art.py; the model shows three, two, one or no fingers. As a
 * finger goes down the stack carries the moment ({@code ClenchAt}), and the held paw curls that finger frame by frame
 * (tools/draw_paw_clench.py) for everyone who can see the hand.
 */
public final class WitheredPawItem extends Item {
    public WitheredPawItem(Properties properties){super(properties);}
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){
        var stack=player.getItemInHand(hand);player.getCooldowns().addCooldown(this,20);
        if(player instanceof ServerPlayer p)ThreeFingers.use(p,stack);
        return InteractionResultHolder.sidedSuccess(stack,level.isClientSide());
    }
    private static CompoundTag data(ItemStack s){var t=s.get(DataComponents.CUSTOM_DATA);return t==null?new CompoundTag():t.copyTag();}
    public static boolean owned(ItemStack s,Player p){var t=data(s);return s.is(LiteraryRegistry.PAW.get())&&t.hasUUID(ThreeFingers.PAW_OWNER)&&t.getUUID(ThreeFingers.PAW_OWNER).equals(p.getUUID());}
    /** The ticks a finger takes to curl down on the held paw. */
    public static final int CLENCH=24;
    public static final String CLENCH_AT="ClenchAt";
    /** This reader's working paw: theirs, and the one their thread's story last pulled from the fire. */
    public static boolean mine(ItemStack s,Player p,CompoundTag story){return owned(s,p)&&data(s).getInt(ThreeFingers.ISSUE)==story.getInt(ThreeFingers.ISSUE);}
    public static boolean carries(ServerPlayer p,CompoundTag story){var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(mine(inv.getItem(i),p,story))return true;return mine(p.containerMenu.getCarried(),p,story);}
    /** How far through its curl the finger going down is, from one at the start to nothing once it is down; nothing when no finger is curling. */
    public static float clench(ItemStack s,long now){var t=data(s);if(!t.contains(CLENCH_AT))return 0;long age=now-t.getLong(CLENCH_AT);return age<0||age>=CLENCH?0:1-age/(float)CLENCH;}
    public static int fingers(ItemStack s){return data(s).getInt(ThreeFingers.FINGERS);}
    /** A fresh paw from the fire, with the thread's count of fingers. */
    public static ItemStack issue(ServerPlayer p,CompoundTag story){
        var stack=new ItemStack(LiteraryRegistry.PAW.get());var t=new CompoundTag();
        t.putUUID(ThreeFingers.PAW_OWNER,p.getUUID());t.putInt(ThreeFingers.ISSUE,story.getInt(ThreeFingers.ISSUE));t.putInt(ThreeFingers.FINGERS,story.getInt(ThreeFingers.FINGERS));
        stack.set(DataComponents.CUSTOM_DATA,CustomData.of(t));stack.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(3-Math.max(0,Math.min(3,story.getInt(ThreeFingers.FINGERS)))));
        return stack;
    }
    /** The holder's working paw shows the fingers the story says are left; {@code clench} curls the one just gone down where it can be seen. */
    public static void refresh(ServerPlayer p,CompoundTag story,boolean clench){
        var inv=p.getInventory();int left=Math.max(0,Math.min(3,story.getInt(ThreeFingers.FINGERS)));long now=p.level().getGameTime();
        for(int i=0;i<inv.getContainerSize();i++){var s=inv.getItem(i);if(!mine(s,p,story))continue;
            CustomData.update(DataComponents.CUSTOM_DATA,s,t->{t.putInt(ThreeFingers.FINGERS,left);if(clench)t.putLong(CLENCH_AT,now);});s.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(3-left));}
        p.inventoryMenu.broadcastChanges();
    }
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<Component> lines,TooltipFlag flag){
        String text=switch(fingers(stack)){case 3->"Three fingers.";case 2->"Two fingers. One is curled down.";case 1->"One finger left.";default->"No fingers. Only a fist.";};
        lines.add(Component.literal(text).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC));
    }
}
