package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LakeWitchEntity;
import java.nio.file.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/** Actual baked production poses and untouched atlases; staged views do not certify a whole playthrough. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class CharacterPhaseVisualProof extends Screen {
    private static final String[] GROUPS={"mother","witch","clap"},VIEWS={"front","side","back","dim"};
    private int page,frames;
    public CharacterPhaseVisualProof(){super(Component.literal("Native character phase review"));}
    @Override protected void init(){WitchUvProof.verify();}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float delta){}
    private static ResourceLocation skin(String name){return ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/"+name+".png");}
    private static void witchPose(LakeWitchModel m,int n,float age){
        m.young=false;m.crouching=false;m.riding=false;
        if(n>=7)m.memoryPose(.7F,.3F,false,age,1);
        else {m.huntPose(n*.7F,.7F,age,n==1?LakeWitchEntity.WITHDRAW:n==2?LakeWitchEntity.LUNGE:LakeWitchEntity.STALK,n==2||n==3);m.lookPose(12,-5);if(n>=3)m.attackPose(n==3?0:n==4?.5F:n==5?.2F:.8F,n>=5,n==3);}
    }
    @Override public void render(GuiGraphics g,int x,int y,float delta){
        var mc=Minecraft.getInstance();boolean close=page>=12;int group=close?1:page/4,view=close?page-12:page%4;
        int count=close?6:group==0?17:group==1?10:4,columns=close?3:group==0?6:group==1?5:4,rows=(count+columns-1)/columns;
        float cw=(width-20F)/columns,ch=(height-42F)/rows,scale=Math.min(cw*.44F,ch*.38F)*(close?2.3F:1);
        g.fill(0,0,width,height,0xff27231f);g.drawString(font,"Native "+GROUPS[group]+(close?" / jaw closeups":" / "+VIEWS[view])+" / original textures and production joints",10,9,0xffe9dfcd,false);g.flush();
        var buffers=mc.renderBuffers().bufferSource();int light=!close&&view==3?LightTexture.pack(7,0):LightTexture.FULL_BRIGHT;
        for(int n=0;n<count;n++){
            float cx=10+(n%columns+.5F)*cw,top=29+(n/columns)*ch;
            var p=g.pose();p.pushPose();p.translate(cx,top+(close?ch*.58F:scale*.52F+ch*.06F),190);p.scale(scale,scale,scale);p.mulPose(Axis.YP.rotationDegrees(new float[]{180,90,0,145}[view]));
            ResourceLocation texture;String label;
            if(group==0){int stage=n==16?0:n;texture=skin("mother_of_strays"+(stage==0?"":"_"+stage));var m=new MotherModel(mc.getEntityModels().bakeLayer(MotherModel.LAYER));m.pose(stage/15F,n==16,.8F,.5F,frames,12,0);label=n==16?"Carrying":"Corruption / "+stage;
                require(mc,texture);m.renderToBuffer(p,buffers.getBuffer(RenderType.entityCutoutNoCull(texture)),light,OverlayTexture.NO_OVERLAY,-1);
            }else if(group==2){texture=skin("clap_ghost_girl");var m=new ClapGhostModel(mc.getEntityModels().bakeLayer(ClapGhostModel.LAYER));m.pose(n*7,10);label="Native step / "+n;
                require(mc,texture);m.renderToBuffer(p,buffers.getBuffer(RenderType.entityCutoutNoCull(texture)),light,OverlayTexture.NO_OVERLAY,-1);
            }else{
                int sample=close?(n==5?7:n==0?0:n==1?3:n==2?5:n==3?6:5):n;
                texture=skin(sample>=7?"lake_witch_memory":"lake_witch");var m=new LakeWitchModel(mc.getEntityModels().bakeLayer(LakeWitchModel.LAYER));witchPose(m,sample,frames);
                if(close){if(n==4)m.attackPose(.5F,true,false);p.translate(-m.head.x/16F,-m.head.y/16F,-m.head.z/16F);for(var part:new ModelPart[]{m.body,m.leftArm,m.rightArm,m.leftLeg,m.rightLeg,m.jacket,m.leftSleeve,m.rightSleeve,m.leftPants,m.rightPants})part.visible=false;}
                else if(n==8){p.translate(0,.9F,0);p.mulPose(Axis.ZP.rotationDegrees(90));}else if(n==9)p.translate(0,.65F,0);
                label=close?new String[]{"Closed","Wind-up","Bite / early","Bite / late","Bite / open","Human memory"}[n]:new String[]{"Stalk","Withdraw","Lunge","Wind-up","Hand rake","Bite / early","Bite / late","Human memory","Carried memory","Sinking memory"}[n];
                require(mc,texture);m.renderToBuffer(p,buffers.getBuffer(RenderType.entityCutoutNoCull(texture)),light,OverlayTexture.NO_OVERLAY,-1);
            }
            buffers.endBatch();p.popPose();g.drawCenteredString(font,label,(int)cx,(int)(top+ch-12),0xffe9dfcd);
        }
    }
    private static void require(Minecraft mc,ResourceLocation texture){if(mc.getResourceManager().getResource(texture).isEmpty())throw new IllegalStateException("Missing character atlas "+texture);}
    @SubscribeEvent public static void frame(RenderFrameEvent.Post event)throws Exception{
        var mc=Minecraft.getInstance();if((!Boolean.getBoolean("the_oldest_house.fontSmoke")&&!VignetteReviewSmoke.active())||!(mc.screen instanceof CharacterPhaseVisualProof proof)||++proof.frames<24)return;
        var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);String name=proof.page<12?GROUPS[proof.page/4]+"-"+VIEWS[proof.page%4]:"witch-jaw-"+(proof.page==12?"front":"side");
        try(NativeImage image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve("native-character-"+name+".png"));}
        if(++proof.page<14){proof.frames=0;return;}
        Files.writeString(dir.resolve("character-phases-passed.txt"),"Fourteen saved native views: all sixteen unchanged Mother corruption atlases and carrying; lake-witch stalking, withdrawal, lunge, wind-up, hand rake and bite plus standing/carried/sinking human memory; four cloth-girl stride samples; front/side/back/dim and six jaw closeups from front/side. Actual production pose helpers and baked layers. Staged UV/mesh review, not complete ordinary play.\n");
        TheOldestHouse.LOGGER.info("CHARACTER PHASE CHECK PASSED: fourteen complete native views");mc.setScreen(VignetteReviewSmoke.active()?new ClassicsVisualProof():new StaceyScrabbleVisualProof());
    }
}
