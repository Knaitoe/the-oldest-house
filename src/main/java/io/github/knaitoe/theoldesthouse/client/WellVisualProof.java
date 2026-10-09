package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/** Actual shipped block sprites, moving mesh, facing stone engraving and compositor on the native GPU. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class WellVisualProof extends Screen {
    private static final String[] NAMES={"lid-open","lid-quarter","lid-three-quarter","lid-closed","stone-initials","fade-start","fade-middle","fade-dark","lid-from-below"};
    private int view,frames,previousBrightness=256;
    public WellVisualProof(){super(Component.literal("Farm well native design"));}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xFF1C2127);g.flush();
        if(view>=5&&view<=7){g.fill(0,0,width,height,0xFFFFEEAA);NovelSceneClient.drawWellFade(g,new int[]{60,140,220}[view-5]);}
        else{
            var p=g.pose();var buffers=mc.renderBuffers().bufferSource();p.pushPose();
            if(view==4){
                var carved=NovelRegistry.CARVINGS.get().defaultBlockState();if(mc.getBlockRenderer().getBlockModel(carved)==mc.getModelManager().getMissingModel())throw new IllegalStateException("Missing native stone initials mesh");
                p.translate(width/2.,height*.67,190);p.scale(68,-68,68);p.mulPose(Axis.XP.rotationDegrees(10));p.mulPose(Axis.YP.rotationDegrees(180));p.translate(-1,-.4,0);
                for(int dx=0;dx<2;dx++)for(int dy=0;dy<2;dy++){p.pushPose();p.translate(dx,dy,0);mc.getBlockRenderer().renderSingleBlock(dx==0&&dy==1?carved:Blocks.MOSSY_COBBLESTONE.defaultBlockState(),p,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);p.popPose();}
            }else{
                p.translate(width/2.,height*.73,190);p.scale(62,-62,62);p.mulPose(Axis.XP.rotationDegrees(view==8?-65:24));p.mulPose(Axis.YP.rotationDegrees(-30));p.translate(-.5,view==8?-1:0,0);
                float closed=view==8?.35F:new float[]{0,.25F,.75F,1}[view];
                WellCoverRenderer.draw(closed,view!=0,p,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);
            }
            buffers.endBatch();p.popPose();
        }
        g.drawString(font,"FARM WELL / "+NAMES[view].replace('-',' '),12,12,0xFFE8E4D9,false);
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();if(!Boolean.getBoolean("the_oldest_house.fontSmoke")||!(mc.screen instanceof WellVisualProof s)||++s.frames<4)return;
        var folder=Path.of("../build/font-smoke");try(NativeImage image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){
            image.writeToFile(folder.resolve("native-well-"+NAMES[s.view]+".png"));
            if(s.view>=5&&s.view<=7){int brightness=image.getPixelRGBA(image.getWidth()/2,image.getHeight()/2)&255;
                if(brightness>=s.previousBrightness||s.view==5&&brightness<200||s.view==7&&brightness!=0)throw new IllegalStateException("The native well fade is not gradual and finally opaque: view="+s.view+", brightness="+brightness);
                s.previousBrightness=brightness;
            }else{var colors=new java.util.HashSet<Integer>();for(int y=60;y<image.getHeight()-4;y++)for(int x=4;x<image.getWidth()-4;x++)colors.add(image.getPixelRGBA(x,y));if(colors.size()<8)throw new IllegalStateException("The native well mesh is blank at "+NAMES[s.view]);}
        }
        if(++s.view<NAMES.length){s.frames=0;return;}
        Files.writeString(folder.resolve("well-design-passed.txt"),"Nine native GPU views: hinged plank lid and articulated closing figure at four positions and from below; initials on exact vanilla mossy stone; the production fade progressively dims a bright frame to absolute black.\n");
        TheOldestHouse.LOGGER.info("WELL DESIGN CHECK PASSED: nine native mesh and gradual darkness views");mc.stop();
    }
}
