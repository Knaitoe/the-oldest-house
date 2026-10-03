package io.github.knaitoe.theoldesthouse.client;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.SeanceActor;
import io.github.knaitoe.theoldesthouse.network.SeanceViewPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
/** Borrow the medium's native camera for two seconds, then restore the actual player camera. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class SeanceView {
    private static int lease;private static boolean empty;private static ClientLevel world;
    private SeanceView(){}
    public static void accept(SeanceViewPayload payload){var mc=Minecraft.getInstance();restore();if(mc.level==null||mc.player==null||mc.player.isSpectator()||payload.ticks()<=0)return;
        var entity=mc.level.getEntity(payload.actorId());if(!(entity instanceof SeanceActor actor)||actor.role()!=0)return;lease=Math.min(60,payload.ticks());empty=payload.empty();world=mc.level;mc.setCameraEntity(actor);}
    public static boolean emptyRoom(){return lease>0&&empty&&Minecraft.getInstance().level==world;}
    public static void restore(){var mc=Minecraft.getInstance();if(world!=null&&mc.level==world&&mc.player!=null)mc.setCameraEntity(mc.player);lease=0;empty=false;world=null;}
    @SubscribeEvent public static void tick(ClientTickEvent.Post event){var mc=Minecraft.getInstance();if(lease<=0)return;if(--lease<=0||mc.level!=world||mc.player==null||!mc.player.isAlive()||mc.player.isSpectator())restore();}
    @SubscribeEvent public static void player(RenderPlayerEvent.Pre event){if(emptyRoom())event.setCanceled(true);}
    @SubscribeEvent public static void input(MovementInputUpdateEvent event){if(lease>0){event.getInput().forwardImpulse=0;event.getInput().leftImpulse=0;event.getInput().jumping=false;}}
}
