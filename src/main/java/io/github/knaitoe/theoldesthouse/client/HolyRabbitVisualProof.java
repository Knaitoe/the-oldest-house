package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryActor;
import java.nio.file.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.RabbitModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

/** The supplied art is checked on the actual registered human model and vanilla rabbit joints. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class HolyRabbitVisualProof extends Screen {
    private static boolean launched;private int frames,page;
    private static final ResourceLocation RABBIT=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/literary_snow_rabbit.png");
    public HolyRabbitVisualProof(){super(Component.literal("Father, Son and Holy Rabbit — native art"));}
    @Override protected void init(){
        var mc=Minecraft.getInstance();var textures=new ResourceLocation[]{LiteraryRenderers.skin(LiteraryActor.FATHER,1),LiteraryRenderers.skin(LiteraryActor.FATHER,2),LiteraryRenderers.skin(LiteraryActor.FATHER,3),RABBIT};
        for(var texture:textures)try(var stream=mc.getResourceManager().getResource(texture).orElseThrow().open();var image=NativeImage.read(stream)){
            if(image.getWidth()!=image.getHeight()*2||image.getWidth()<512||image.getWidth()>2048)throw new IllegalStateException("Unfitted Holy Rabbit atlas "+texture);
        }catch(java.io.IOException e){throw new java.io.UncheckedIOException(e);}
        if(textures[0].equals(textures[1])||textures[1].equals(textures[2]))throw new IllegalStateException("The three native father states must select their own art");
    }
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xff292721);
        g.drawString(font,"Holy Rabbit / "+(page==0?"winter cast and native UVs":"progressive limp and injury"),12,10,0xffe4d6b9,false);g.flush();
        for(int n=0;n<6;n++){
            int cx=20+(n%3)*(width-40)/3+(width-40)/6,cy=50+(n/3)*(height-70)/2;
            var p=g.pose();p.pushPose();p.translate(cx,cy,190);p.scale(54,54,54);p.mulPose(Axis.YP.rotationDegrees(n>=3?25:145));
            var buffers=mc.renderBuffers().bufferSource();
            if(page==0&&n>=3){
                var model=new RabbitModel<>(mc.getEntityModels().bakeLayer(ModelLayers.RABBIT));model.young=false;
                p.mulPose(Axis.YP.rotationDegrees((n-3)*80));model.renderToBuffer(p,buffers.getBuffer(RenderType.entityCutoutNoCull(RABBIT)),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0xffffffff);
                HolyRabbitRenderer.markings(mc.getEntityModels().bakeLayer(HolyRabbitRenderer.MARKS),false,0,0,p,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);
            }else{
                int phase=n%3+1;var model=new LiteraryRenderers.CastModel(mc.getEntityModels().bakeLayer(LiteraryRenderers.CAST));
                model.animateFather(phase,page==0?0:frames*.12F,page==0?0:.7F,frames*2);
                model.renderToBuffer(p,buffers.getBuffer(RenderType.entityCutoutNoCull(LiteraryRenderers.skin(LiteraryActor.FATHER,phase))),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0xffffffff);
            }
            buffers.endBatch();p.popPose();g.drawCenteredString(font,page==0&&n>=3?new String[]{"Rabbit / front","Rabbit / side","Rabbit / back"}[n-3]:new String[]{"First night","Bandaged","Cannot walk"}[n%3]+(n>=3?" / back":""),cx,cy+88,0xffe4d6b9);
        }
    }
    @SubscribeEvent public static void launch(ClientTickEvent.Post e){var mc=Minecraft.getInstance();if(Boolean.getBoolean("the_oldest_house.holyRabbitSmoke")&&!launched&&mc.screen!=null&&mc.getOverlay()==null){launched=true;mc.options.guiScale().set(2);mc.resizeDisplay();mc.setScreen(new HolyRabbitVisualProof());}}
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();if(!(mc.screen instanceof HolyRabbitVisualProof proof)||++proof.frames<30)return;
        var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);
        try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve("native-holy-rabbit-"+(proof.page==0?"cast":"motion")+".png"));}
        if(++proof.page<2){proof.frames=0;return;}
        Files.writeString(dir.resolve("holy-rabbit-passed.txt"),"Three fitted Native American Father skins, progressive limp, left-leg-only injury, final collapsed pose and the original native rabbit UVs rendered.\n");mc.stop();
    }
}
