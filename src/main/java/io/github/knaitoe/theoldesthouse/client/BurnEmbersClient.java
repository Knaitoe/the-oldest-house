package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseText;
import io.github.knaitoe.theoldesthouse.network.BurnEmbersPayload;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/** Five-second, owner-private smoke writing. Native depth testing hides it behind real walls. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class BurnEmbersClient {
    private static BurnEmbersPayload state;private static ClientLevel level;private static LocalPlayer reader;
    private static int ticks;private static List<FormattedCharSequence> lines=List.of();
    private BurnEmbersClient() {}
    public static Component words(BurnEmbersPayload p) {
        return HouseText.color(Component.literal(p.text()).withStyle(s->s.withFont(p.hand())));
    }
    public static void accept(BurnEmbersPayload p) {
        var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null||p.text().length()>256)return;
        state=p;level=mc.level;reader=mc.player;ticks=0;lines=mc.font.split(words(p),150);
        if(p.memory()==1||p.memory()==2)mc.level.playLocalSound(p.fire(),p.memory()==1?SoundEvents.GRAVEL_STEP:SoundEvents.WOOD_PLACE,SoundSource.AMBIENT,.18F,.8F,false);
    }
    public static boolean showing(){return active();}
    public static BurnEmbersPayload current(){return active()?state:null;}
    private static boolean active() {
        var mc=Minecraft.getInstance();return state!=null&&ticks<100&&mc.level==level&&mc.player==reader&&reader!=null&&reader.isAlive();
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post e) {
        if(state==null)return;
        if(!active()){state=null;level=null;reader=null;lines=List.of();return;}
        if(++ticks%8==0) {
            var at=state.fire();level.addParticle(ParticleTypes.SMOKE,at.getX()+.5,at.getY()+1.3,at.getZ()+.5,0,.025,0);
        }
    }
    @SubscribeEvent public static void render(RenderLevelStageEvent e) {
        if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS||!active())return;
        var mc=Minecraft.getInstance();var camera=e.getCamera().getPosition();var at=state.fire();
        if(camera.distanceToSqr(at.getCenter())>144)return;
        int alpha=Math.min(220,Math.min(ticks*22,(100-ticks)*11));if(alpha<8)return;
        var pose=e.getPoseStack();var buffers=mc.renderBuffers().bufferSource();
        pose.pushPose();pose.translate(at.getX()+.5-camera.x,at.getY()+1.7+ticks*.006-camera.y,at.getZ()+.5-camera.z);
        pose.mulPose(e.getCamera().rotation());pose.scale(.009F,-.009F,.009F);
        int y=-lines.size()*9;
        for(var line:lines) {
            mc.font.drawInBatch(line,-mc.font.width(line)/2F,y,alpha<<24|0xF0BD85,false,pose.last().pose(),buffers,Font.DisplayMode.NORMAL,0,LightTexture.FULL_BRIGHT);y+=9;
        }
        pose.popPose();buffers.endBatch();
    }
}
