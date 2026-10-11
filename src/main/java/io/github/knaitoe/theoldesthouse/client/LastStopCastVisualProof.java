package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.ElkLotNight;
import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryActor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/** The actual Last Stop atlases on the baked model, from both sides and in dim light. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class LastStopCastVisualProof extends Screen {
    private static final String[] VIEWS={"front","back","night"};
    private int page,frames;
    public LastStopCastVisualProof(){super(Component.literal("The Last Stop — native cast"));}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float delta){}
    @Override protected void init(){
        var mc=Minecraft.getInstance();
        for(var person:ElkLotNight.CAST){
            var skin=LiteraryRenderers.skin(LiteraryActor.STRANGER,person.phase());
            if(!skin.getPath().endsWith("literary_bar_"+person.key()+".png"))throw new IllegalStateException("Wrong Last Stop skin for "+person.name());
            try(var stream=mc.getResourceManager().open(skin);var image=NativeImage.read(stream)){
                if(image.getWidth()!=512||image.getHeight()!=256)throw new IllegalStateException("Wrong Last Stop atlas size: "+skin);
                for(var rect:new int[][]{{32,32,32,32},{80,80,32,48},{176,80,16,48},{16,80,16,48}}){
                    int opaque=0;
                    for(int y=rect[1];y<rect[1]+rect[3];y++)for(int x=rect[0];x<rect[0]+rect[2];x++)if((image.getPixelRGBA(x,y)>>>24)>=128)opaque++;
                    if(opaque<rect[2]*rect[3]*.95)throw new IllegalStateException("Incomplete native face on "+skin);
                }
            }catch(IOException e){throw new IllegalStateException("Cannot decode Last Stop skin "+skin,e);}
        }
    }
    @Override public void render(GuiGraphics g,int x,int y,float delta){
        var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xff252724);
        g.drawString(font,"The Last Stop / "+VIEWS[page]+" / existing bodies, named skins",10,10,0xffeee0c4,false);g.flush();
        var buffers=mc.renderBuffers().bufferSource();int i=0;
        for(var person:ElkLotNight.CAST){
            var model=new LiteraryRenderers.CastModel(mc.getEntityModels().bakeLayer(LiteraryRenderers.CAST));model.young=false;
            model.pose(LiteraryActor.STRANGER,person.phase(),frames);
            if(model.body.getChild("coat").visible!=person.key().equals("marla"))throw new IllegalStateException("Only Marla wears the apron extension");
            float centre=12+(i+++.5F)*(width-24)/5F,scale=Math.min((width-30)/5F*.7F,height*.34F);
            var pose=g.pose();pose.pushPose();pose.translate(centre,height*.80F,190);pose.scale(scale,scale,scale);
            pose.mulPose(Axis.YP.rotationDegrees(page==1?-25:page==2?135:180));pose.translate(0,-1.5,0);
            model.renderToBuffer(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(LiteraryRenderers.skin(LiteraryActor.STRANGER,person.phase()))),page==2?LightTexture.pack(10,0):LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0xffffffff);
            pose.popPose();buffers.endBatch();g.drawCenteredString(font,person.name(),(int)centre,height-24,0xffeee0c4);
        }
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();if(!Boolean.getBoolean("the_oldest_house.fontSmoke")||!(mc.screen instanceof LastStopCastVisualProof proof)||++proof.frames<24)return;
        var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);
        try(NativeImage image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve("native-last-stop-cast-"+VIEWS[proof.page]+".png"));}
        if(++proof.page<VIEWS.length){proof.frames=0;return;}
        Files.writeString(dir.resolve("last-stop-cast-passed.txt"),"Five named 512x256 atlases on the native 128x64 humanoid UV layout, front/back/night views, opaque base faces and Marla-only apron; existing stranger phases preserved.\n");
        TheOldestHouse.LOGGER.info("LAST STOP CAST CHECK PASSED: five native skins, three views and the apron");mc.setScreen(new ElkVisualProof());
    }
}
