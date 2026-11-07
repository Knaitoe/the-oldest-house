package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.SpokenPayload;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

/**
 * 0.4.329: another reader's words (labyrinth/Spoken), drawn over their head above their name, wrapped to a short width, fading in
 * and out. Their own client never gets them; they have the panel.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class SpokenClient {
    private SpokenClient(){}
    static final int WIDTH=150,TEXT=0xF0EBDD;
    private record Words(List<FormattedCharSequence> lines,long from,int ticks){}
    private static final Map<Integer,Words> SAID=new HashMap<>();

    public static void accept(SpokenPayload payload){
        var mc=Minecraft.getInstance();if(mc.level==null)return;
        SAID.put(payload.entity(),new Words(mc.font.split(Component.literal(payload.text()),WIDTH),mc.level.getGameTime(),payload.ticks()));
    }
    /** Whether a player has words showing (for the client proofs). */
    public static boolean speaking(int entity){return SAID.containsKey(entity);}

    @SubscribeEvent public static void render(RenderPlayerEvent.Post e){
        var mc=Minecraft.getInstance();var player=e.getEntity();if(mc.level==null||mc.player==null)return;
        var words=SAID.get(player.getId());if(words==null)return;
        float age=mc.level.getGameTime()-words.from()+e.getPartialTick();
        if(age>=words.ticks()){SAID.remove(player.getId());return;}
        if(player.isInvisibleTo(mc.player))return;
        float shown=Mth.clamp(age/4f,0,1)*Mth.clamp((words.ticks()-age)/20f,0,1);
        var pose=e.getPoseStack();pose.pushPose();
        // Above where the name tag sits (half a block over the head), so both can be read.
        pose.translate(0,player.getBbHeight()+.85,0);
        pose.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
        pose.scale(.025f,-.025f,.025f);
        var font=mc.font;var matrix=pose.last().pose();
        int alpha=Math.max(0x10,(int)(shown*255));int back=((int)(shown*mc.options.getBackgroundOpacity(.25f)*255))<<24;
        int n=words.lines().size();
        for(int i=0;i<n;i++){
            var line=words.lines().get(i);float x=-font.width(line)/2f,y=(i-n+1)*(font.lineHeight+1);
            font.drawInBatch(line,x,y,(alpha<<24)|TEXT,false,matrix,e.getMultiBufferSource(),Font.DisplayMode.NORMAL,back,e.getPackedLight());
        }
        pose.popPose();
    }
    @SubscribeEvent public static void left(ClientPlayerNetworkEvent.LoggingOut e){SAID.clear();}
}
