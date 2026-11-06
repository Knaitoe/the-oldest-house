package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/** Renders the actual paper/browser screens and baked props, including a narrow GUI, then closes. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class GhostsLoreProof {
    private record View(String name,int kind,int day,int page,int scale,int scene){}
    private static final List<View> VIEWS=List.of(
        new View("diary-early",GhostsLore.DIARY,1,0,2,0),
        new View("diary-crew",GhostsLore.DIARY,7,1,2,0),
        new View("diary-bank",GhostsLore.DIARY,7,2,2,0),
        new View("diary-california",GhostsLore.DIARY,7,3,2,0),
        new View("contact-slip",GhostsLore.DIARY,7,4,2,0),
        new View("bank-notice",GhostsLore.BANK,1,0,2,0),
        new View("bank-production",GhostsLore.BANK,1,1,2,0),
        new View("study",GhostsLore.LAPTOP,7,0,2,0),
        new View("articles",GhostsLore.LAPTOP,7,1,2,0),
        new View("bookmarks",GhostsLore.LAPTOP,7,2,2,0),
        new View("sister-cats",GhostsLore.DRAWING,1,0,2,0),
        new View("sister-vines",GhostsLore.DRAWING,7,0,2,0),
        new View("narrow-study",GhostsLore.LAPTOP,7,0,3,0),
        new View("narrow-drawing",GhostsLore.DRAWING,7,0,3,0),
        new View("desk-closed",0,1,0,2,1),
        new View("desk-study",0,7,0,2,2),
        new View("stored-cross",0,7,0,2,3),
        new View("lens-effigy",0,7,0,2,4));
    private static boolean launched;private static int index,frames;
    private GhostsLoreProof(){}
    @SubscribeEvent public static void launch(ClientTickEvent.Post ignored){var mc=Minecraft.getInstance();if(Boolean.getBoolean("the_oldest_house.ghostsLoreSmoke")&&!launched&&mc.screen!=null&&mc.getOverlay()==null){launched=true;show();}}
    private static void show(){
        var mc=Minecraft.getInstance();var view=VIEWS.get(index);mc.options.guiScale().set(view.scale);mc.resizeDisplay();
        if(view.scene==0){var paper=new GhostsLoreScreen(view.kind,view.day);paper.proofPage(view.page);mc.setScreen(paper);}else mc.setScreen(new Props(view));frames=0;
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post ignored)throws Exception{
        var mc=Minecraft.getInstance();if(!Boolean.getBoolean("the_oldest_house.ghostsLoreSmoke")||!launched||index>=VIEWS.size()||++frames<24)return;
        var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);
        try(NativeImage image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve("native-ghosts-lore-"+VIEWS.get(index).name+".png"));}
        if(++index<VIEWS.size()){show();return;}
        Files.writeString(dir.resolve("ghosts-lore-passed.txt"),"Rendered "+VIEWS.size()+" native paper, browser, drawing and baked-model views.\n");mc.stop();
    }
    private static final class Props extends Screen {
        private final View view;
        Props(View view){super(Component.literal(view.name));this.view=view;}
        @Override public void renderBackground(GuiGraphics g,int mx,int my,float partial){}
        private void block(GuiGraphics g,net.minecraft.client.renderer.MultiBufferSource.BufferSource buffers,GhostsSetBlock.Kind kind,double x,double y,double z){
            var pose=g.pose();pose.pushPose();pose.translate(x,y,z);
            var mc=Minecraft.getInstance();var state=GhostsSetBlock.of(kind,Direction.SOUTH);
            if(mc.getBlockRenderer().getBlockModel(state)==mc.getModelManager().getMissingModel())throw new IllegalStateException("Missing Ghosts lore model: "+kind);
            mc.getBlockRenderer().renderSingleBlock(state,pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();
        }
        @Override public void render(GuiGraphics g,int mx,int my,float partial){
            g.fill(0,0,width,height,0xff302b27);g.drawCenteredString(font,"Ghosts / "+view.name,width/2,15,0xffeee3c9);g.flush();
            var mc=Minecraft.getInstance();var buffers=mc.renderBuffers().bufferSource();var pose=g.pose();pose.pushPose();
            pose.translate(width/2F,height*.65F,400);float scale=Math.min(width,height)*(view.scene>=3?.24F:.31F);pose.scale(scale,-scale,scale);pose.mulPose(Axis.XP.rotationDegrees(18));pose.mulPose(Axis.YP.rotationDegrees(-25));pose.translate(-.5,0,-.5);
            block(g,buffers,GhostsSetBlock.Kind.FLOOR,-1,-1,0);block(g,buffers,GhostsSetBlock.Kind.FLOOR,0,-1,0);block(g,buffers,GhostsSetBlock.Kind.FLOOR,1,-1,0);
            if(view.scene<=2){
                block(g,buffers,GhostsSetBlock.Kind.WRITING_DESK,0,0,0);
                block(g,buffers,view.scene==1?GhostsSetBlock.Kind.LAPTOP_CLOSED:GhostsSetBlock.Kind.LAPTOP_STUDY,0,1,0);
                block(g,buffers,GhostsSetBlock.Kind.BANK_SLIPS,1,0,0);
            }else{
                block(g,buffers,GhostsSetBlock.Kind.STORED_CROSS,0,0,0);
                if(GhostsLore.lensImage(view.day,view.scene==4,true))GhostsLoreRenderer.image(pose,buffers,LightTexture.FULL_BRIGHT);
            }
            buffers.endBatch();pose.popPose();
        }
    }
}
