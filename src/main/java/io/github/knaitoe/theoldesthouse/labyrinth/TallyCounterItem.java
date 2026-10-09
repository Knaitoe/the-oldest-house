package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import java.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * The count (0.4.53): what the reader who got the trailer's night right takes home. Nobody saw the Goatman; arithmetic
 * caught it. Inside the House the counter counts what is really there, shows what is hiding or pretending, and clicks
 * on its own when something comes up behind its keeper. Outside, it keeps count while its keeper sleeps, and wakes
 * them when something comes close. It works only for the reader it was left for.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class TallyCounterItem extends Item {
    public static final String PRETENDER="HousePretender";
    /** Ten seconds between warnings from behind; one waking each four seconds of sleep at most. */
    public static final int BEHIND_EVERY=200,WATCH_EVERY=80,BEHIND_RANGE=8,WATCH_RANGE=10,COUNT_RANGE=16,OUT_THERE=32;
    private static final Map<UUID,Long> LAST=new HashMap<>();
    private static final String[] WORDS={"None","One","Two","Three","Four","Five","Six","Seven","Eight","Nine","Ten","Eleven","Twelve","Thirteen","Fourteen","Fifteen","Sixteen","Seventeen","Eighteen","Nineteen","Twenty"};
    public TallyCounterItem(Properties properties){super(properties);}

    public static ItemStack forReader(UUID reader){
        var stack=new ItemStack(GoatmanRegistry.COUNTER.get());CustomData.update(DataComponents.CUSTOM_DATA,stack,t->t.putUUID(LiteraryVignettes.OWNER,reader));return stack;
    }
    static boolean ownedBy(ItemStack stack,Player p){var t=stack.get(DataComponents.CUSTOM_DATA);return t!=null&&t.copyTag().hasUUID(LiteraryVignettes.OWNER)&&t.copyTag().getUUID(LiteraryVignettes.OWNER).equals(p.getUUID());}
    public static boolean carries(ServerPlayer p){
        for(var list:List.of(p.getInventory().items,p.getInventory().offhand))for(var stack:list)if(stack.getItem() instanceof TallyCounterItem&&ownedBy(stack,p))return true;
        return false;
    }
    public static String words(int n){return n>=0&&n<WORDS.length?WORDS[n]:Integer.toString(n);}

    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){
        var stack=player.getItemInHand(hand);player.getCooldowns().addCooldown(this,20);
        if(player instanceof ServerPlayer p)click(p,stack);
        return InteractionResultHolder.sidedSuccess(stack,level.isClientSide());
    }
    public static void click(ServerPlayer p,ItemStack stack){
        p.serverLevel().playSound(null,p.blockPosition(),GoatmanRegistry.CLICK.get(),SoundSource.PLAYERS,.7F,1);
        if(!ownedBy(stack,p)){p.displayClientMessage(Component.literal("It clicks, but the number doesn't turn for you."),true);return;}
        if(HouseDimensions.isHouseDimension(p.level().dimension())){
            var present=present(p,COUNT_RANGE);
            for(var e:present)if(pretending(e,p))e.addEffect(new MobEffectInstance(MobEffects.GLOWING,100,0,false,false));
            p.displayClientMessage(Component.literal("Click. "+words(present.size())+"."),true);
        }else{
            int out=p.serverLevel().getEntitiesOfClass(Mob.class,p.getBoundingBox().inflate(OUT_THERE),m->m.isAlive()&&m instanceof Enemy).size();
            p.displayClientMessage(Component.literal("Click. "+words(out)+" out there."),true);
        }
    }
    /** Everything alive that is really here with its keeper, the keeper included; another reader's private visions are not. */
    public static List<LivingEntity> present(ServerPlayer p,double range){
        return p.serverLevel().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(range),e->e.isAlive()&&!e.isSpectator()&&!(e instanceof ArmorStand)
            &&e.distanceToSqr(p)<=range*range&&privateTo(e,p));
    }
    static boolean privateTo(Entity e,ServerPlayer p){
        if(e instanceof GoatmanChild c&&c.viewer().isPresent())return c.viewer().get().equals(p.getUUID());
        if(e instanceof GoatmanFigure f)return f.viewer().filter(p.getUUID()::equals).isPresent();
        return true;
    }
    /** Hiding or pretending: invisible, wearing someone else's shape, or the thing itself. Never another player. */
    public static boolean pretending(LivingEntity e,ServerPlayer p){
        if(e instanceof Player)return false;
        if(e instanceof GoatmanFigure)return true;
        if(e instanceof GoatmanChild c&&(c.tells()!=0||c.getTags().contains(PRETENDER)))return true;
        return e.isInvisible()||e.getTags().contains(PRETENDER);
    }
    static boolean hostile(LivingEntity e,ServerPlayer p){
        if(e instanceof Player)return false;
        if(e instanceof GoatmanFigure f)return f.viewer().filter(p.getUUID()::equals).isPresent();
        return e instanceof Enemy||e instanceof Mob m&&m.getTarget()==p;
    }

    @SubscribeEvent public static void tick(ServerTickEvent.Post event){
        var server=event.getServer();if(server.getTickCount()%10!=0)return;
        for(var p:server.getPlayerList().getPlayers())if(p.isAlive()&&!p.isSpectator()&&carries(p))watch(p);
    }
    /** Inside: something behind its keeper. Outside: something close to a sleeping keeper. */
    public static void watch(ServerPlayer p){
        long now=p.serverLevel().getGameTime();long last=LAST.getOrDefault(p.getUUID(),-100000L);
        if(HouseDimensions.isHouseDimension(p.level().dimension())){
            if(now-last<BEHIND_EVERY)return;
            Vec3 look=p.getLookAngle().multiply(1,0,1).normalize();
            for(var e:p.serverLevel().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(BEHIND_RANGE),e->e.isAlive()&&hostile(e,p))){
                Vec3 to=e.position().subtract(p.position()).multiply(1,0,1);
                if(to.lengthSqr()>BEHIND_RANGE*BEHIND_RANGE||to.lengthSqr()<.01||look.dot(to.normalize())>-.25)continue;
                LAST.put(p.getUUID(),now);privateClick(p);p.displayClientMessage(Component.literal("Click."),true);return;
            }
        }else if(p.isSleeping()){
            if(now-last<WATCH_EVERY)return;
            var near=p.serverLevel().getEntitiesOfClass(Mob.class,p.getBoundingBox().inflate(WATCH_RANGE),m->m.isAlive()&&m instanceof Enemy&&m.distanceToSqr(p)<=WATCH_RANGE*WATCH_RANGE);
            if(near.isEmpty())return;
            LAST.put(p.getUUID(),now);
            near.stream().min(Comparator.comparingDouble(m->m.distanceToSqr(p))).ifPresent(m->m.addEffect(new MobEffectInstance(MobEffects.GLOWING,100,0,false,false)));
            p.stopSleepInBed(true,true);privateClick(p);
            p.displayClientMessage(Component.literal("Click. "+words(near.size())+"."),true);
        }
    }
    static void privateClick(ServerPlayer p){
        p.connection.send(new ClientboundSoundPacket(Holder.direct(GoatmanRegistry.CLICK.get()),SoundSource.PLAYERS,p.getX(),p.getEyeY(),p.getZ(),.8F,1,p.getRandom().nextLong()));
    }
    public static void forget(UUID id){LAST.remove(id);}
    /** When this keeper was last warned, for the native tests. */
    public static long lastWarning(UUID id){return LAST.getOrDefault(id,-1L);}

    @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<Component> lines,TooltipFlag flag){
        lines.add(Component.literal("In the House it counts what is really there, and clicks when something is behind you.").withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC));
        lines.add(Component.literal("Outside, it keeps count while you sleep.").withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC));
        lines.add(Component.literal("Only for the one who counted right.").withStyle(ChatFormatting.DARK_GRAY));
    }
}
