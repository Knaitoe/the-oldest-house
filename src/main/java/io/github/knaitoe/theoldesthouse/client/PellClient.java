package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.PellPayload;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * 0.4.526, the Pell house: the house's own hour on this client (PellPayload), in its scene clock (mode 22). Between packets the hour
 * goes on at the rate it was sent with, a tick of the day to a tick of the game, so the sun moves smoothly; held, it stays put.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class PellClient {
    private PellClient(){}
    private static long time=-1,at,ticks;private static int rate;
    public static void accept(PellPayload payload){time=payload.time();rate=payload.rate();at=ticks;}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){ticks++;
        // 0.4.566 (owner, playing the house: "idle minecraft music shouldn't play in this vignette"): what the game was playing stops
        // as the reader comes to the house, as the scored scenes' does.
        var mc=net.minecraft.client.Minecraft.getInstance();boolean now=here();if(now&&!wasHere)mc.getMusicManager().stopPlaying();wasHere=now;}
    private static boolean wasHere;
    /** 0.4.566: whether this client is at the Pell house, its street and its lot (the house's own clock, mode 22). */
    public static boolean here(){var mc=net.minecraft.client.Minecraft.getInstance();return mc.level!=null&&mc.player!=null&&NovelSceneClient.clockMode(mc.level)==22;}
    /** 0.4.566: and none of the game's own music starts while the reader is here. */
    @SubscribeEvent public static void music(net.neoforged.neoforge.client.event.SelectMusicEvent e){if(here())e.setMusic(null);}
    @SubscribeEvent public static void out(ClientPlayerNetworkEvent.LoggingOut e){time=-1;}
    /** The house's hour now, as far as this client knows it. */
    public static long current(){return time<0?-1:time+(long)rate*(ticks-at);}
    /** At the Pell house (mode 22), its own hour; anywhere else, what it would be. */
    public static long time(Level level,long ordinary){return time>=0&&NovelSceneClient.clockMode(level)==22?current():ordinary;}

    /** 0.4.531: the game lays a sleeper at a bed's height, and the camp bed's canvas is lower by this much, so a reader asleep on it is
     *  drawn lowered onto it (as the park's swing tilts a reader's body, SwingClient). */
    private static final float CAMP_BED_DROP=2/16F;
    private static final java.util.Set<net.minecraft.world.entity.player.Player> LOWERED=java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
    @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOWEST) public static void lower(net.neoforged.neoforge.client.event.RenderPlayerEvent.Pre e){
        var p=e.getEntity();var at=p.getSleepingPos().orElse(null);
        if(at==null||!io.github.knaitoe.theoldesthouse.labyrinth.PellThing.is(p.level().getBlockState(at),io.github.knaitoe.theoldesthouse.labyrinth.PellThing.Kind.CAMP_BED))return;
        e.getPoseStack().pushPose();e.getPoseStack().translate(0,-CAMP_BED_DROP,0);LOWERED.add(p);
    }
    @SubscribeEvent public static void raise(net.neoforged.neoforge.client.event.RenderPlayerEvent.Post e){if(LOWERED.remove(e.getEntity()))e.getPoseStack().popPose();}
}
