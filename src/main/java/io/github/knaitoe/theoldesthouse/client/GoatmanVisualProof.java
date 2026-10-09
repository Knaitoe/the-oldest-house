package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.GoatmanRegistry;
import java.nio.file.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/**
 * The Goatman's night (0.4.53), drawn with the shipped mesh and atlases: the thing itself from the front, the side and
 * behind (as the trail shows it), walking, and laughing without a sound, its eyes in their own glowing layer; and the
 * counter and the brat it is all about.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class GoatmanVisualProof extends Screen {
    private int frames;
    public GoatmanVisualProof(){super(Component.literal("The Goatman - the thing itself"));}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        var mc=Minecraft.getInstance();
        for(var res:new ResourceLocation[]{GoatmanFigureRenderer.SKIN,GoatmanFigureRenderer.EYES,
                ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/item/tally_counter.png"),ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/item/goatman_brat.png"),
                ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/particle/goatman_copper_0.png"),ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"particles/goatman_copper.json")})
            if(mc.getResourceManager().getResource(res).isEmpty())throw new IllegalStateException("Missing Goatman asset "+res);
        var baked=mc.getEntityModels().bakeLayer(GoatmanFigureModel.LAYER);var head=baked.getChild("root").getChild("head");
        for(var part:new String[]{"muzzle","beard","right_horn","left_horn","right_ear","left_ear"})head.getChild(part);
        g.fill(0,0,width,height,0xFF14161A);
        g.drawString(font,"The Goatman: front, three-quarter, side, from behind (as on the trail), walking, laughing without a sound",10,8,0xFFEEE0C4,false);
        g.drawString(font,"The tally counter and a brat",10,height-58,0xFFEEE0C4,false);
        g.flush();
        var buffers=mc.renderBuffers().bufferSource();var model=new GoatmanFigureModel(baked);
        float[][] views={{180,0,0,0},{140,0,0,0},{90,0,0,0},{0,0,0,0},{150,frames*.6F,1,0},{170,0,0,1}};
        for(int i=0;i<views.length;i++){
            float[] v=views[i];model.pose(frames,v[1],v[2],v[3]>0,0,0);
            var p=g.pose();p.pushPose();p.translate(48+i*(width-80)/6F,height-90,150);p.scale(48,48,48);p.mulPose(Axis.YP.rotationDegrees(v[0]));p.translate(0,-1.5,0);
            model.renderToBuffer(p,buffers.getBuffer(RenderType.entityCutoutNoCull(GoatmanFigureRenderer.SKIN)),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0xFFFFFFFF);
            model.renderToBuffer(p,buffers.getBuffer(RenderType.eyes(GoatmanFigureRenderer.EYES)),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0xFFFFFFFF);
            buffers.endBatch();p.popPose();
        }
        int item=0;for(var stack:new ItemStack[]{new ItemStack(GoatmanRegistry.COUNTER.get()),new ItemStack(GoatmanRegistry.BRAT.get())}){
            var p=g.pose();p.pushPose();p.translate(16+item*56,height-44,0);p.scale(3,3,1);g.renderItem(stack,0,0);p.popPose();item++;}
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();if(!Boolean.getBoolean("the_oldest_house.fontSmoke")||!(mc.screen instanceof GoatmanVisualProof proof)||++proof.frames<24)return;
        var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);
        try(NativeImage image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve("native-goatman.png"));}
        Files.writeString(dir.resolve("goatman-passed.txt"),"24 native frames: the Goatman's mesh (horns, muzzle, beard, ears) and skin in six views with its glowing eyes, and the counter and brat items.\n");
        TheOldestHouse.LOGGER.info("GOATMAN CHECK PASSED: the thing itself, its eyes, the counter and the brat rendered");
        mc.setScreen(new CoffinVisualProof());
    }
}
