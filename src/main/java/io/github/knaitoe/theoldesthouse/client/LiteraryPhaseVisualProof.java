package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryActor;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/** Full native cast UVs, changed appearances and action joints; the saved image covers every page. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class LiteraryPhaseVisualProof extends Screen {
    private record Cast(int role,int phase,float attack,String label) {}
    private static final String[] GROUPS={"cast","changes","people","actions"},VIEWS={"front","side","back","dim"};
    private static final String[] NAMES={"Mother","Dancer","Red figure","Coffin","Lady","Silhouette","Brother","Father","Visitor","Killer","Family father","Family mother","Family child","Old man","Stranger","Camera"};
    private int page,frames;
    public LiteraryPhaseVisualProof(){super(Component.literal("Literary cast — complete phase and angle review"));}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float delta){}
    private static List<Cast> cast(int group){
        var list=new ArrayList<Cast>();
        if(group==0){for(int role=0;role<16;role++)list.add(new Cast(role,0,0,NAMES[role]));}
        if(group==1){for(int phase=0;phase<6;phase++)list.add(new Cast(LiteraryActor.SILHOUETTE,phase,0,"Silhouette / "+phase));
            for(int phase=0;phase<5;phase++)list.add(new Cast(LiteraryActor.FATHER,phase,0,"Father / "+phase));
            for(int phase=0;phase<4;phase++)list.add(new Cast(LiteraryActor.BROTHER,phase,0,"Brother / "+phase));
            for(int role=10;role<=12;role++)list.add(new Cast(role,1,0,NAMES[role]+" / changed"));
            for(int phase=0;phase<3;phase++)list.add(new Cast(LiteraryActor.COFFIN_WOMAN,phase,0,phase==2?"Coffin / hidden":"Coffin / "+phase));}
        if(group==2){for(int phase=0;phase<14;phase++)list.add(new Cast(LiteraryActor.STRANGER,phase,0,"Stranger / "+phase));}
        if(group==3){for(int phase=0;phase<4;phase++)list.add(new Cast(LiteraryActor.KILLER,phase,0,"Killer / "+phase));
            for(float attack:new float[]{.15F,.5F,.9F})list.add(new Cast(LiteraryActor.KILLER,2,attack,"Strike / "+attack));
            list.add(new Cast(LiteraryActor.DANCER,1,0,"Dancer / turning"));
            list.add(new Cast(LiteraryActor.FATHER,4,0,"Father / two poles"));
            list.add(new Cast(LiteraryActor.FAMILY_CHILD,3,0,"Child / behind lens"));}
        return list;
    }
    @Override public void render(GuiGraphics g,int x,int y,float delta){
        var mc=Minecraft.getInstance();int group=page/4,view=page%4;
        g.fill(0,0,width,height,0xff262320);g.drawString(font,"Literary / "+GROUPS[group]+" / "+VIEWS[view]+" / original atlases and native joints",10,9,0xffeee0c4,false);g.flush();
        var cast=cast(group);int columns=6,rows=(cast.size()+columns-1)/columns;
        float cellWidth=(width-20F)/columns,cellHeight=(height-40F)/rows,scale=Math.min(cellWidth*.45F,cellHeight*.38F);
        var buffers=mc.renderBuffers().bufferSource();int n=0;
        for(var state:cast){
            var skin=LiteraryRenderers.skin(state.role,state.phase);if(mc.getResourceManager().getResource(skin).isEmpty())throw new IllegalStateException("Missing phase atlas "+skin);
            var model=new LiteraryRenderers.CastModel(mc.getEntityModels().bakeLayer(LiteraryRenderers.CAST));
            if(state.role==LiteraryActor.KILLER)model.animateKiller(state.phase,frames*.45F,.75F,frames,state.attack);
            else if(state.role==LiteraryActor.FATHER)model.animateFather(state.phase,frames*.4F,.65F,frames);
            else model.preview(state.role,state.phase,frames);
            float size=state.role==LiteraryActor.BROTHER||state.role==LiteraryActor.FAMILY_CHILD||state.role==LiteraryActor.SILHOUETTE&&state.phase==4?.58F:1;
            float cx=10+(n%columns+.5F)*cellWidth,top=29+(n/columns)*cellHeight;
            var pose=g.pose();pose.pushPose();pose.translate(cx,top+scale*.52F+cellHeight*.06F+scale*1.5F*(1-size),190);pose.scale(scale*size,scale*size,scale*size);
            pose.mulPose(Axis.YP.rotationDegrees(new float[]{180,90,0,145}[view]));
            if(state.role==LiteraryActor.COFFIN_WOMAN&&state.phase==1)LiteraryRenderers.coffinPose(pose);
            if(state.role==LiteraryActor.BROTHER&&state.phase>0)LiteraryRenderers.bedPose(pose,.58F);
            if(!(state.role==LiteraryActor.COFFIN_WOMAN&&state.phase==2))model.renderToBuffer(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(skin)),view==3?LightTexture.pack(7,0):LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0xffffffff);
            if(state.role==LiteraryActor.FATHER&&state.phase==4)LiteraryRenderers.crutches(model,mc.getEntityModels().bakeLayer(LiteraryRenderers.CRUTCH).getChild("pole"),pose,buffers,view==3?LightTexture.pack(7,0):LightTexture.FULL_BRIGHT);
            buffers.endBatch();pose.popPose();g.drawCenteredString(font,state.label,(int)cx,(int)(top+cellHeight-12),0xffeee0c4);n++;
        }
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();if((!Boolean.getBoolean("the_oldest_house.fontSmoke")&&!VignetteReviewSmoke.active())||!(mc.screen instanceof LiteraryPhaseVisualProof proof)||++proof.frames<24)return;
        var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);
        try(NativeImage image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve("native-literary-"+GROUPS[proof.page/4]+"-"+VIEWS[proof.page%4]+".png"));}
        if(++proof.page<16){proof.frames=0;return;}
        Files.writeString(dir.resolve("literary-phases-passed.txt"),"Sixteen saved native views: all sixteen original roles; six silhouette forms, five Father states, four Brother states, all changed family skins and three coffin states; all fourteen stranger phases; killer walking/searching/crouching/three strike samples, dancing and the final two-pole pose. Front/side/back/dim light; native adult proportions and original child scales. Hidden coffin phase intentionally remains absent. These are staged mesh/UV checks, not ordinary complete playthroughs.\n");
        TheOldestHouse.LOGGER.info("LITERARY PHASE CHECK PASSED: sixteen complete native cast views");if(VignetteReviewSmoke.active())mc.stop();else mc.setScreen(new GhostsVisualProof());
    }
}
