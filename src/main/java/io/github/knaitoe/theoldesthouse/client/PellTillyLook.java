package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.SeatEntity;
import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryActor;
import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryRegistry;
import io.github.knaitoe.theoldesthouse.labyrinth.PellPeople;
import io.github.knaitoe.theoldesthouse.labyrinth.PellRegistry;
import io.github.knaitoe.theoldesthouse.network.PellTillyPayload;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

/**
 * 0.4.545: a reader playing Tilly at the Pell house is drawn as Tilly (docs/PELL_HOUSE_TILLY_0_4_545.md), on every client at the house and
 * their own: a client-only figure of the house's Tilly, at a child's size, follows the player, where they stand, which way they face and
 * their stride; it holds the cane, or has its hand out in June's, or lies asleep in Nessa's bed, as the server says (PellTillyPayload). The
 * player's own name and skin are not drawn. In a chair or on the edge of the bed the figure sits as the house's people sit, on the seat's
 * own cell. The look lapses three seconds after the server stops sending it, when they leave the house.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class PellTillyLook {
    private record Look(int gesture,long until){}
    private static final Map<UUID,Look> LOOKS=new HashMap<>();
    private static final Map<UUID,LiteraryActor> FIGURES=new HashMap<>();
    private static final int LAPSE=60;
    private PellTillyLook(){}

    public static void accept(PellTillyPayload p){
        var level=Minecraft.getInstance().level;if(level==null)return;
        if(p.gesture()<0){LOOKS.remove(p.player());FIGURES.remove(p.player());return;}
        LOOKS.put(p.player(),new Look(p.gesture(),level.getGameTime()+LAPSE));
    }
    /** The pose a player is drawn as Tilly in, or -1 if they are not. For tests and proofs. */
    public static int gesture(UUID player){var level=Minecraft.getInstance().level;var look=LOOKS.get(player);return level==null||look==null||level.getGameTime()>look.until()?-1:look.gesture();}

    @SubscribeEvent public static void render(RenderPlayerEvent.Pre e){
        var player=e.getEntity();int gesture=gesture(player.getUUID());if(gesture<0)return;
        var figure=figure(player,gesture);if(figure==null)return;
        e.setCanceled(true);
        float partial=e.getPartialTick();
        figure.yBodyRot=player.yBodyRot;figure.yBodyRotO=player.yBodyRotO;figure.yHeadRot=player.yHeadRot;figure.yHeadRotO=player.yHeadRotO;
        figure.setXRot(player.getXRot());figure.xRotO=player.xRotO;figure.setYRot(player.getYRot());figure.yRotO=player.yRotO;
        var pose=e.getPoseStack();pose.pushPose();
        // Sat on a seat, the figure is drawn from its seat's floor, as the house's people are in its chairs (LiteraryRenderers.seat).
        if(player.getVehicle() instanceof SeatEntity)pose.translate(0,-(player.getY()-Math.floor(player.getY())),0);
        var renderer=Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(figure);
        renderer.render(figure,Mth.lerp(partial,player.yRotO,player.getYRot()),partial,pose,e.getMultiBufferSource(),e.getPackedLight());
        pose.popPose();
    }
    private static @Nullable LiteraryActor figure(Player player,int gesture){
        var figure=FIGURES.get(player.getUUID());
        if(figure==null||figure.level()!=player.level()){
            figure=LiteraryRegistry.ACTOR.get().create(player.level());if(figure==null)return null;
            figure.moveTo(player.getX(),player.getY(),player.getZ(),player.getYRot(),player.getXRot());figure.setCustomNameVisible(false);
            figure.appearance(LiteraryActor.STRANGER,PellPeople.FIRST+PellPeople.TILLY);
            FIGURES.put(player.getUUID(),figure);
        }
        if(figure.gesture()!=gesture){
            figure.gesture(gesture);
            figure.setItemInHand(InteractionHand.MAIN_HAND,gesture==PellPeople.CANE?new ItemStack(PellRegistry.CANE.get()):ItemStack.EMPTY);
        }
        return figure;
    }

    /** The figures follow their players every tick, so their stride is the player's own. */
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        var level=Minecraft.getInstance().level;if(level==null){LOOKS.clear();FIGURES.clear();return;}
        long now=level.getGameTime();LOOKS.values().removeIf(look->now>look.until());
        FIGURES.entrySet().removeIf(entry->{
            if(!LOOKS.containsKey(entry.getKey()))return true;
            var player=level.getPlayerByUUID(entry.getKey());var figure=entry.getValue();if(player==null||figure.level()!=level)return true;
            figure.xo=figure.getX();figure.yo=figure.getY();figure.zo=figure.getZ();
            figure.setPos(player.getX(),player.getY(),player.getZ());figure.tickCount++;
            figure.calculateEntityAnimation(false);
            return false;
        });
    }
    @SubscribeEvent public static void left(ClientPlayerNetworkEvent.LoggingOut e){LOOKS.clear();FIGURES.clear();}
}
