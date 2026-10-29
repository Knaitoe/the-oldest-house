package io.github.knaitoe.theoldesthouse.client;

import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/**
 * 0.4.167: the bathroom's sixteen hair styles as the glass shows them, from the front and from behind, each tinted to a hair
 * colour as a reader's crown would give it; and the counter's eight routine blocks (two tools to a block, one lifted) with the
 * new bath rug. Runs after the House's lines in the same client.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class HouseReadHairProof extends Screen {
    private static final String[] NAMES={"hair_styles","hair_backs","routine_counter"};
    /** Four crowns: black, dark brown, auburn, blonde. */
    private static final int[] COLOURS={0x1d1714,0x4a3326,0x7a3a22,0xc8a25c};
    private int page,frames;
    public HouseReadHairProof(){super(Component.literal("The bathroom's hair"));}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        g.fill(0,0,width,height,0xff343630);g.drawString(font,"Bathroom glass / "+NAMES[page].replace('_',' ')+" / tinted to the reader's crown",10,9,0xffe5dcc8,false);
        if(page==2){counter(g);return;}
        var mc=Minecraft.getInstance();var out=mc.renderBuffers().bufferSource();g.flush();
        float cw=width/4f,ch=(height-30)/4f,scale=Math.min(cw*.32f,ch*.36f);
        for(int i=0;i<16;i++){
            var model=new PlayerModel<AbstractClientPlayer>(mc.getEntityModels().bakeLayer(ModelLayers.PLAYER),false);model.setAllVisible(true);model.young=false;model.hat.visible=false;
            float cx=(i%4+.5f)*cw,cy=30+(i/4)*ch+ch*.62f;var p=g.pose();p.pushPose();p.translate(cx,cy,180);p.scale(scale,scale,scale);p.mulPose(Axis.YP.rotationDegrees(page==0?160:-20));
            model.head.render(p,out.getBuffer(RenderType.entityCutoutNoCull(DefaultPlayerSkin.getDefaultTexture())),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);
            HouseReadHair.draw(model,i,COLOURS[i%4],p,out,LightTexture.FULL_BRIGHT);out.endBatch();p.popPose();
            g.drawCenteredString(font,HouseReadHair.name(i),(int)cx,(int)(30+(i/4+1)*ch-18),0xffe5dcc8);
            g.drawCenteredString(font,"("+HouseReadMoments.ROUTINES[i]+")",(int)cx,(int)(30+(i/4+1)*ch-8),0xffa59d8c);
        }
    }
    /** The eight routine blocks on the vanity, two tools each, the first of each lifted on the lower row; the rug below. */
    private void counter(GuiGraphics g){
        var mc=Minecraft.getInstance();var out=mc.renderBuffers().bufferSource();g.flush();float cw=width/8f,scale=Math.min(cw*.5f,height*.12f);
        for(int row=0;row<2;row++)for(int i=0;i<8;i++){
            BlockState state=HouseReadRegistry.FITTING.get().defaultBlockState().setValue(HouseReadPropBlock.PART,HouseReadPropBlock.ROUTINE_BLOCKS.get(i)).setValue(HouseReadPropBlock.LIFTED,row);
            float cx=(i+.5f)*cw,cy=height*(row==0?.26f:.5f);block(g,state,cx,cy,scale,out);
            if(row==0){int[] pair=HouseReadMoments.PAIRS[i];g.drawCenteredString(font,HouseReadMoments.ROUTINES[pair[0]],(int)cx,(int)(cy+scale*.55f),0xffe5dcc8);g.drawCenteredString(font,HouseReadMoments.ROUTINES[pair[1]],(int)cx,(int)(cy+scale*.55f+10),0xffa59d8c);}
        }
        var rug=HouseReadRegistry.FITTING.get().defaultBlockState().setValue(HouseReadPropBlock.PART,HouseReadPropBlock.Part.BATH_MAT).setValue(HorizontalDirectionalBlock.FACING,Direction.SOUTH);
        block(g,rug,width*.5f,height*.8f,scale*1.6f,out);g.drawCenteredString(font,"the woven bath rug, nearly two blocks long",(int)(width*.5f),(int)(height*.93f),0xffe5dcc8);
    }
    private static void block(GuiGraphics g,BlockState state,float x,float y,float scale,net.minecraft.client.renderer.MultiBufferSource.BufferSource out){
        var mc=Minecraft.getInstance();var p=g.pose();p.pushPose();p.translate(x,y,180);p.scale(scale,-scale,scale);p.mulPose(Axis.XP.rotationDegrees(35));p.mulPose(Axis.YP.rotationDegrees(200));p.translate(-.5f,-.5f,-.5f);
        mc.getBlockRenderer().renderSingleBlock(state,p,out,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);out.endBatch();p.popPose();
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();if(!(mc.screen instanceof HouseReadHairProof s)||++s.frames<12)return;
        var folder=Path.of("../build/house-read-hair");Files.createDirectories(folder);
        try(var image=net.minecraft.client.Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(folder.resolve(NAMES[s.page]+".png"));}
        if(++s.page<NAMES.length){s.frames=0;return;}
        Files.writeString(folder.resolve("passed.txt"),"Sixteen hair styles from the bathroom's tools, front and back, tinted to four crowns, and the eight two-tool routine blocks with the woven bath rug. Shipped native models.\n");
        mc.stop();
    }
}
