package io.github.knaitoe.theoldesthouse.client;

import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.nio.file.*;
import java.util.UUID;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/** Production layers on native standard/slim bodies; separate from actual socket traversal. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class HouseReadClothesProof extends Screen {
    private int page,frames;
    public HouseReadClothesProof(){super(Component.literal("Native borrowed layers"));}
    @Override protected void init(){if(HouseReadClothes.registeredSkins()<2||HouseReadClothes.registeredSlimSkins()!=1)throw new IllegalStateException("Both real skin renderers must receive their correct standard/slim coat layer");}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        if(page==4){mirrors(g);return;}
        if(page==5){house(g);return;}
        var mc=Minecraft.getInstance();String type=new String[]{"wool","trench","barn","returned","mirror"}[page];g.fill(0,0,width,height,0xff343630);g.drawString(font,TheOldestHouse.displayName()+" / "+type+" / native skin and fitted layer",10,9,0xffe5dcc8,false);g.flush();var out=mc.renderBuffers().bufferSource();float cw=width/4f,ch=(height-36)/2f,scale=Math.min(cw*.43f,ch*.40f);
        for(int i=0;i<8;i++){
            boolean narrow=i%2==1;int view=i/2;var body=new PlayerModel<AbstractClientPlayer>(mc.getEntityModels().bakeLayer(narrow?ModelLayers.PLAYER_SLIM:ModelLayers.PLAYER),narrow);body.setAllVisible(true);body.young=false;
            if(view==3){body.rightArm.xRot=.4f;body.leftArm.xRot=-.4f;body.rightLeg.xRot=-.3f;body.leftLeg.xRot=.3f;body.head.yRot=.35f;body.hat.copyFrom(body.head);}
            var p=g.pose();p.pushPose();p.translate((i%4+.5f)*cw,36+(i/4)*ch+ch*.30f,180);p.scale(scale,scale,scale);p.mulPose(Axis.YP.rotationDegrees(new float[]{180,90,0,180}[view]));
            var skin=DefaultPlayerSkin.get(new UUID(138,narrow?1:0)).texture();body.renderToBuffer(p,out.getBuffer(RenderType.entityCutoutNoCull(skin)),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);
            if(page<3){var cloth=mc.getEntityModels().bakeLayer(narrow?HouseReadClothes.SLIM:HouseReadClothes.STANDARD);HouseReadClothes.coat(body,cloth,type,p,out,LightTexture.FULL_BRIGHT);HouseReadClothes.boots(body,cloth,type+"_boots",p,out,LightTexture.FULL_BRIGHT);}
            out.endBatch();p.popPose();g.drawCenteredString(font,(narrow?"slim":"standard")+" / "+new String[]{"front","side","back","step + turn"}[view],(int)((i%4+.5f)*cw),(int)(32+(i/4+1)*ch-6),0xffe5dcc8);
        }
    }
    /** 0.4.159: the House's version of a reader, only ever in glass and copies: reading glasses, the veiled face, each with and without the coat. */
    private void house(GuiGraphics g){
        var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xff343630);g.drawString(font,TheOldestHouse.displayName()+" / the House's version / glasses and the obscured face over the reader's own skin",10,9,0xffe5dcc8,false);g.flush();var out=mc.renderBuffers().bufferSource();float cw=width/4f,ch=(height-36)/2f,scale=Math.min(cw*.43f,ch*.40f);
        String[] labels={"glasses","veiled","trench + glasses","barn + veiled"};
        for(int i=0;i<8;i++){
            boolean narrow=i/4==1;int view=i%4;var body=new PlayerModel<AbstractClientPlayer>(mc.getEntityModels().bakeLayer(narrow?ModelLayers.PLAYER_SLIM:ModelLayers.PLAYER),narrow);body.setAllVisible(true);body.young=false;body.head.yRot=-.3f;body.hat.copyFrom(body.head);
            var p=g.pose();p.pushPose();p.translate((view+.5f)*cw,36+(i/4)*ch+ch*.30f,180);p.scale(scale,scale,scale);p.mulPose(Axis.YP.rotationDegrees(160));
            var skin=DefaultPlayerSkin.get(new UUID(138,narrow?1:0)).texture();body.renderToBuffer(p,out.getBuffer(RenderType.entityCutoutNoCull(skin)),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);
            var cloth=mc.getEntityModels().bakeLayer(narrow?HouseReadClothes.SLIM:HouseReadClothes.STANDARD);if(view==2)HouseReadClothes.coat(body,cloth,"trench",p,out,LightTexture.FULL_BRIGHT);if(view==3)HouseReadClothes.coat(body,cloth,"barn",p,out,LightTexture.FULL_BRIGHT);
            if(view%2==0)HouseReadClothes.glasses(body,cloth,p,out,LightTexture.FULL_BRIGHT);else HouseReadClothes.veil(body,cloth,p,out,LightTexture.FULL_BRIGHT);
            out.endBatch();p.popPose();g.drawCenteredString(font,(narrow?"slim":"standard")+" / "+labels[view],(int)((view+.5f)*cw),(int)(32+(i/4+1)*ch-6),0xffe5dcc8);
        }
    }
    private void mirrors(GuiGraphics g){
        var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xff343630);g.drawString(font,"Native two-block glass / the reader a step back, as the plane mirror shows them / turned wooden back",10,9,0xffe5dcc8,false);g.flush();var out=mc.renderBuffers().bufferSource();float cw=width/4f,scale=Math.min(cw*.75f,(height-65)/2.1f);
        for(int i=0;i<4;i++){boolean narrow=i%2==1,turned=i>=2;var p=g.pose();p.pushPose();p.translate((i+.5f)*cw-scale*.5f,height-30,180);p.scale(scale,-scale,scale);
            var bottom=io.github.knaitoe.theoldesthouse.labyrinth.HouseReadRegistry.FITTING.get().defaultBlockState().setValue(io.github.knaitoe.theoldesthouse.labyrinth.HouseReadPropBlock.PART,io.github.knaitoe.theoldesthouse.labyrinth.HouseReadPropBlock.Part.MIRROR).setValue(io.github.knaitoe.theoldesthouse.labyrinth.HouseReadPropBlock.TURNED,turned);mc.getBlockRenderer().renderSingleBlock(bottom,p,out,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);p.pushPose();p.translate(0,1,0);mc.getBlockRenderer().renderSingleBlock(bottom.setValue(io.github.knaitoe.theoldesthouse.labyrinth.HouseReadPropBlock.PART,io.github.knaitoe.theoldesthouse.labyrinth.HouseReadPropBlock.Part.MIRROR_TOP),p,out,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);p.popPose();
            if(!turned){var body=new PlayerModel<AbstractClientPlayer>(mc.getEntityModels().bakeLayer(narrow?ModelLayers.PLAYER_SLIM:ModelLayers.PLAYER),narrow);body.young=false;body.setAllVisible(true);HouseReadMirror.drawImage(body,mc.getEntityModels().bakeLayer(narrow?HouseReadClothes.SLIM:HouseReadClothes.STANDARD),DefaultPlayerSkin.get(new UUID(138,narrow?1:0)).texture(),narrow?"barn":"wool",true,p,out,LightTexture.FULL_BRIGHT);}
            out.endBatch();p.popPose();g.drawCenteredString(font,(narrow?"slim":"standard")+(turned?" / turned":" / own skin + loan"),(int)((i+.5f)*cw),height-17,0xffe5dcc8);
        }
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{var mc=Minecraft.getInstance();if(!Boolean.getBoolean("the_oldest_house.houseReadSmoke")||!(mc.screen instanceof HouseReadClothesProof s)||++s.frames<12)return;var folder=Path.of("../build/house-read139-art");Files.createDirectories(folder);try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(folder.resolve("layers-"+new String[]{"wool","trench","barn","returned","mirror","house"}[s.page]+".png"));}if(++s.page<6){s.frames=0;return;}mc.setScreen(new HouseReadMomentsProof());return;}
}
