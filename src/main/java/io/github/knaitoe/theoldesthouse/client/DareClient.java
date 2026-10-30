package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import io.github.knaitoe.theoldesthouse.network.DareStatePayload;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

/** 0.4.173, "Third Floor Girls'": what the reader's own glass is showing, and no music on the third floor. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class DareClient {
    private static int count;private static boolean inside;private static long since;
    /** For the client proofs: a count and its age, as if the server had said so. */
    private static Integer proofCount;private static float proofAge;
    private DareClient(){}
    public static void accept(DareStatePayload p){
        var mc=Minecraft.getInstance();long now=mc.level==null?0:mc.level.getGameTime();
        if(p.count()!=count)since=now;
        if(p.inside()&&!inside)mc.getMusicManager().stopPlaying();
        count=p.count();inside=p.inside();DareDoorBlock.clientHeld=p.held();
    }
    /** How many times it has been said, as this reader's glass shows it: 0 to 3, or 4 once they are out. */
    public static int count(){return proofCount!=null?proofCount:inside?count:0;}
    /** Ticks since the last count was said. */
    public static float age(float partial){if(proofCount!=null)return proofAge;var mc=Minecraft.getInstance();return mc.level==null?0:mc.level.getGameTime()-since+partial;}
    public static void proof(Integer count,float age){proofCount=count;proofAge=age;}
    @SubscribeEvent public static void music(SelectMusicEvent e){if(inside)e.setMusic(null);}
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){count=0;inside=false;DareDoorBlock.clientHeld=false;}
}
