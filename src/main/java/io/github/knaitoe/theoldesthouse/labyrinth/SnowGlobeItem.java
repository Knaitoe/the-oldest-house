package io.github.knaitoe.theoldesthouse.labyrinth;
import java.util.List;
import javax.annotation.Nullable;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

/**
 * What the visitors leave. The whole globe, for the reader who gave everything: the world in it did not end, and once
 * each day it will not let its keeper die either. The cracked globe, for the reader who refused: it leaks, and it will
 * catch its keeper once, while there is water left in it. Each works only for the reader it was left for.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class SnowGlobeItem extends Item {
    /** The snow settles in one day. */
    public static final long SETTLE=24000;
    private final boolean cracked;
    public SnowGlobeItem(Properties properties,boolean cracked){super(properties);this.cracked=cracked;}
    public boolean cracked(){return cracked;}
    public static boolean dry(ItemStack stack){var t=stack.get(DataComponents.CUSTOM_DATA);return t!=null&&t.copyTag().getBoolean("Dry");}
    private static boolean ownedBy(ItemStack stack,Player p){var t=stack.get(DataComponents.CUSTOM_DATA);return t!=null&&t.copyTag().hasUUID(LiteraryVignettes.OWNER)&&t.copyTag().getUUID(LiteraryVignettes.OWNER).equals(p.getUUID());}

    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){
        var stack=player.getItemInHand(hand);player.getCooldowns().addCooldown(this,30);
        if(player instanceof ServerPlayer p)shake(p,stack);
        return InteractionResultHolder.sidedSuccess(stack,level.isClientSide());
    }
    private void shake(ServerPlayer p,ItemStack stack){
        var level=p.serverLevel();var hand=p.getEyePosition().add(p.getLookAngle().scale(.6)).add(0,-.35,0);
        level.sendParticles(cracked?ParticleTypes.DRIPPING_WATER:ParticleTypes.SNOWFLAKE,hand.x,hand.y,hand.z,cracked?4:14,.12,.12,.12,.01);
        level.playSound(null,p.blockPosition(),cracked?LiteraryRegistry.GLOBE_CRACKED.get():LiteraryRegistry.GLOBE_SHAKE.get(),SoundSource.PLAYERS,.7F,1);
        var own=LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),LabyrinthPlace.END_WORLD_CABIN);long now=p.server.overworld().getGameTime();
        String line;
        if(!ownedBy(stack,p))line="It is someone else's world. Nothing in it moves for you.";
        else if(cracked)line=own.getBoolean("CrackedSpent")||dry(stack)?"It is dry. The cabin inside is only painted now.":"A little water is left inside. The snow lifts, barely, and lies down again.";
        else line=own.contains("GlobeFell")&&now-own.getLong("GlobeFell")<SETTLE?"Snow is still falling inside. It has not settled since it caught you.":"Snow drifts down over a small cabin, a lake and a jetty. Everything in it is still there.";
        p.displayClientMessage(Component.literal(line),true);
    }
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<Component> lines,TooltipFlag flag){
        String text=cracked?(dry(stack)?"Dry. It caught you once.":"It leaks. While there is water in it, it will catch you once."):"The world in it did not end. Once a day, it will not let you die.";
        lines.add(Component.literal(text).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC));
        lines.add(Component.literal("Only for the one it was left for.").withStyle(ChatFormatting.DARK_GRAY));
    }

    /** The keeper's own globe, anywhere on them. */
    public static @Nullable ItemStack carried(ServerPlayer p){
        for(var list:List.of(p.getInventory().items,p.getInventory().offhand))for(var stack:list)if(stack.getItem() instanceof SnowGlobeItem&&ownedBy(stack,p))return stack;
        return null;
    }
    /** Before the death is recorded anywhere. Not against the void or a kill command, and never inside a committed finale. */
    @SubscribeEvent(priority=EventPriority.HIGH) public static void caught(LivingDeathEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p)||p.isSpectator()||e.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY))return;
        if(FinaleProgress.committed(FinaleProgress.phase(p.server,p.getUUID())))return;
        var stack=carried(p);if(stack==null)return;var globe=(SnowGlobeItem)stack.getItem();
        var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),LabyrinthPlace.END_WORLD_CABIN);long now=p.server.overworld().getGameTime();
        if(globe.cracked){if(own.getBoolean("CrackedSpent")||dry(stack))return;own.putBoolean("CrackedSpent",true);CustomData.update(DataComponents.CUSTOM_DATA,stack,t->t.putBoolean("Dry",true));}
        else{if(own.contains("GlobeFell")&&now-own.getLong("GlobeFell")<SETTLE)return;own.putLong("GlobeFell",now);}
        own.putInt("GlobeCatches",own.getInt("GlobeCatches")+1);LiteraryVignettes.save(d,p.getUUID(),LabyrinthPlace.END_WORLD_CABIN,own);
        e.setCanceled(true);p.setHealth(2);p.clearFire();
        p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,40,4));p.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,200,0));
        p.serverLevel().sendParticles(globe.cracked?ParticleTypes.FALLING_WATER:ParticleTypes.SNOWFLAKE,p.getX(),p.getY()+1,p.getZ(),globe.cracked?24:70,.5,.8,.5,.02);
        p.serverLevel().playSound(null,p.blockPosition(),globe.cracked?LiteraryRegistry.GLOBE_CRACKED.get():LiteraryRegistry.GLOBE_SHAKE.get(),SoundSource.PLAYERS,1,.8F);
        p.displayClientMessage(Component.literal(globe.cracked?"The last of the water runs out through the crack. The snow lies still. You are still here."
                :"The snow in the globe falls all at once. Somewhere a world does not end. Neither do you.").withStyle(ChatFormatting.ITALIC),false);
        io.github.knaitoe.theoldesthouse.house.PlaytestLog.event(p,"globe_caught","cracked",globe.cracked);
    }
}
