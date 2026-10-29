package io.github.knaitoe.theoldesthouse.client;

import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import java.util.UUID;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/**
 * 0.4.169: the wardrobe through the production models and the fitted coat layer: all twelve coats on their hooks and six pairs
 * of boots on the mat, then each coat worn with a pair of boots on a standard body from the front, and on a slim body from the
 * back mid-stride, so every cut's collar, skirt and belt shows. Runs after the kitchen in the same client.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class HouseReadWardrobeProof extends Screen {
    private static final String[] NAMES={"wardrobe_hanging","wardrobe_worn_front","wardrobe_worn_back"};
    private int page,frames;
    public HouseReadWardrobeProof(){super(Component.literal("The wardrobe"));}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        g.fill(0,0,width,height,0xff343630);g.drawString(font,"Coatroom wardrobe / "+NAMES[page].replace('_',' ')+" / shipped native models and the fitted coat layer",10,9,0xffe5dcc8,false);
        if(page==0)hanging(g);else worn(g,page==2);
    }
    /** Every garment as it hangs or stands, with its name. */
    private void hanging(GuiGraphics g){
        var mc=Minecraft.getInstance();var all=HouseReadWardrobe.ALL;int columns=6;float cw=width/(float)columns,ch=(height-30)/3f;
        for(int i=0;i<all.size();i++){
            var garment=all.get(i);float cx=(i%columns+.5f)*cw,cy=26+(i/columns+.5f)*ch;var state=HouseReadRegistry.FITTING.get().defaultBlockState().setValue(HouseReadPropBlock.PART,HouseReadWardrobe.part(garment)).setValue(HorizontalDirectionalBlock.FACING,Direction.SOUTH);
            var p=g.pose();g.flush();var out=mc.renderBuffers().bufferSource();float scale=Math.min(cw,ch)*(garment.boots()?.62f:.42f);
            p.pushPose();p.translate(cx,cy+(garment.boots()?ch*.1f:-ch*.05f),180);p.scale(scale,-scale,scale);p.mulPose(Axis.XP.rotationDegrees(garment.boots()?28:8));p.mulPose(Axis.YP.rotationDegrees(-24));p.translate(-.5,garment.boots()?-.3:-.95,-.5);
            mc.getBlockRenderer().renderSingleBlock(state,p,out,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);out.endBatch();p.popPose();
            g.drawCenteredString(font,garment.name(),(int)cx,(int)(26+(i/columns+1)*ch-10),0xffe5dcc8);
        }
    }
    /** Each coat on a body, with the boots of the same place in the list. */
    private void worn(GuiGraphics g,boolean back){
        var mc=Minecraft.getInstance();var coats=HouseReadWardrobe.coats();var boots=HouseReadWardrobe.footwear();int columns=6;float cw=width/(float)columns,ch=(height-30)/2f,scale=Math.min(cw*.42f,ch*.36f);
        g.flush();var out=mc.renderBuffers().bufferSource();
        for(int i=0;i<coats.size();i++){
            boolean slim=back;var body=new PlayerModel<AbstractClientPlayer>(mc.getEntityModels().bakeLayer(slim?ModelLayers.PLAYER_SLIM:ModelLayers.PLAYER),slim);body.setAllVisible(true);body.young=false;
            if(back){body.rightArm.xRot=.5f;body.leftArm.xRot=-.5f;body.rightLeg.xRot=-.45f;body.leftLeg.xRot=.45f;}
            var p=g.pose();p.pushPose();p.translate((i%columns+.5f)*cw,30+(i/columns)*ch+ch*.28f,180);p.scale(scale,scale,scale);p.mulPose(Axis.YP.rotationDegrees(back?-20:200));
            var skin=DefaultPlayerSkin.get(new UUID(169,i)).texture();body.renderToBuffer(p,out.getBuffer(RenderType.entityCutoutNoCull(skin)),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);
            var cloth=mc.getEntityModels().bakeLayer(slim?HouseReadClothes.SLIM:HouseReadClothes.STANDARD);HouseReadClothes.coat(body,cloth,coats.get(i).id(),p,out,LightTexture.FULL_BRIGHT);HouseReadClothes.boots(body,cloth,boots.get(i%boots.size()).id(),p,out,LightTexture.FULL_BRIGHT);
            out.endBatch();p.popPose();
            g.drawCenteredString(font,coats.get(i).name(),(int)((i%columns+.5f)*cw),(int)(30+(i/columns+1)*ch-18),0xffe5dcc8);g.drawCenteredString(font,boots.get(i%boots.size()).name(),(int)((i%columns+.5f)*cw),(int)(30+(i/columns+1)*ch-8),0xffa59d8c);
        }
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();if(!(mc.screen instanceof HouseReadWardrobeProof s)||++s.frames<12)return;
        var folder=Path.of("../build/house-read-wardrobe");Files.createDirectories(folder);
        try(var image=net.minecraft.client.Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(folder.resolve(NAMES[s.page]+".png"));}
        if(++s.page<NAMES.length){s.frames=0;return;}
        Files.writeString(folder.resolve("passed.txt"),"All eighteen wardrobe garments through the production block models, and the twelve coats with boots through the fitted coat layer on standard and slim bodies, from the front and from the back mid-stride.\n");
        mc.stop();
    }
}
