package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LakeHouseCanoe;
import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryActor;
import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryRegistry;
import io.github.knaitoe.theoldesthouse.network.LakePartnerPayload;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

/**
 * 0.4.156: a lake house partner is drawn as Tess, or as the young man in her place, on their own client and their diver's
 * (docs/LAKE_HOUSE_PAIRS_0_4_156.md). The body drawn is a client-only cast figure that follows the player: where they stand,
 * which way they face, their stride, sitting in the canoe and swimming. The player's own name and skin are not drawn. The
 * look lapses three seconds after the server stops sending it, when either of them leaves the lake.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class LakePartnerLook {
    private record Look(UUID diver,int role,long until){}
    private static final Map<UUID,Look> LOOKS=new HashMap<>();
    private static final Map<UUID,LiteraryActor> FIGURES=new HashMap<>();
    private static final int LAPSE=60;
    private LakePartnerLook(){}

    public static void accept(LakePartnerPayload p){
        var level=Minecraft.getInstance().level;if(level==null)return;
        LOOKS.put(p.partner(),new Look(p.diver(),p.role(),level.getGameTime()+LAPSE));
    }
    /** The role a player is drawn as, or -1. */
    public static int role(UUID player){var level=Minecraft.getInstance().level;var look=LOOKS.get(player);return level==null||look==null||level.getGameTime()>look.until()?-1:look.role();}
    /** Whether the local player goes down with this diver, and so sees the diver's own figure. */
    public static boolean shared(UUID owner){
        var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null)return false;var look=LOOKS.get(mc.player.getUUID());
        return look!=null&&look.diver().equals(owner)&&mc.level.getGameTime()<=look.until();
    }

    @SubscribeEvent public static void render(RenderPlayerEvent.Pre e){
        var player=e.getEntity();int role=role(player.getUUID());if(role<0)return;
        var figure=figure(player,role);if(figure==null)return;
        e.setCanceled(true);
        float partial=e.getPartialTick();
        figure.yBodyRot=player.yBodyRot;figure.yBodyRotO=player.yBodyRotO;figure.yHeadRot=player.yHeadRot;figure.yHeadRotO=player.yHeadRotO;
        figure.setXRot(player.getXRot());figure.xRotO=player.xRotO;figure.setYRot(player.getYRot());figure.yRotO=player.yRotO;
        var renderer=Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(figure);
        renderer.render(figure,Mth.lerp(partial,player.yRotO,player.getYRot()),partial,e.getPoseStack(),e.getMultiBufferSource(),e.getPackedLight());
    }
    private static @Nullable LiteraryActor figure(Player player,int role){
        var figure=FIGURES.get(player.getUUID());
        if(figure==null||figure.level()!=player.level()){
            figure=LiteraryRegistry.ACTOR.get().create(player.level());if(figure==null)return null;
            figure.moveTo(player.getX(),player.getY(),player.getZ(),player.getYRot(),player.getXRot());figure.setCustomNameVisible(false);
            FIGURES.put(player.getUUID(),figure);
        }
        // Sitting in the canoe, swimming, or on foot: the partner poses Tess has herself.
        int phase=player.getVehicle() instanceof LakeHouseCanoe?1:player.isEyeInFluid(FluidTags.WATER)?2:0;
        if(figure.role()!=role||figure.phase()!=phase)figure.appearance(role,phase);
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
