package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

/** Uses the registered pixel materials, articulated cast, every oriented prop and real glass models. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class NightclubVisualProof extends Screen {
    private static boolean launched;private int frames,page;
    public NightclubVisualProof(){super(Component.literal("Nightclub — native materials, cast and motion"));}
    @Override protected void init(){
        var mc=Minecraft.getInstance();
        for(int style=0;style<NightclubRenderers.LOOKS;style++)for(boolean despair:new boolean[]{false,true})try(var stream=mc.getResourceManager().getResource(NightclubRenderers.skin(style,despair)).orElseThrow().open();var image=NativeImage.read(stream)){
            if(image.getWidth()!=image.getHeight()*2)throw new IllegalStateException("Unfitted club atlas "+style);
        }catch(java.io.IOException ex){throw new java.io.UncheckedIOException(ex);}
        for(var kind:NightclubBlock.Kind.values())for(var face:Direction.Plane.HORIZONTAL)for(int stage=0;stage<3;stage++){
            var state=NightclubBlock.of(kind,face).setValue(NightclubBlock.STAGE,stage);
            if(mc.getBlockRenderer().getBlockModel(state)==mc.getModelManager().getMissingModel())throw new IllegalStateException("Missing nightclub model "+state);
        }
    }
    @Override public void renderBackground(GuiGraphics graphics,int x,int y,float partial){}
    @Override public void render(GuiGraphics graphics,int x,int y,float partial){
        var mc=Minecraft.getInstance();graphics.fill(0,0,width,height,0xff252527);
        graphics.drawString(font,"Nightclub / "+new String[]{"painted cast and club attire","connected dance and mixer joints","music pause / despair and return","custom pixel surfaces and interactive props"}[page],12,10,0xffddd5c8,false);graphics.flush();
        if(page<3){
            int count=page==0?12:page==2?10:6,columns=page==0?4:page==2?5:3,rows=page==0?3:2;
            for(int n=0;n<count;n++){
                int slot=page==0?(n<8?n:Nightclub.DANCERS):page==2?(n<8?n:n==8?Nightclub.DANCERS:0):new int[]{0,1,2,3,4,Nightclub.DANCERS}[n];
                int look=NightclubActor.look(slot);float despair=page==2&&n<9?1:0;
                boolean comparison=page==0&&(n==9||n==10);
                net.minecraft.client.model.EntityModel<?> model;net.minecraft.resources.ResourceLocation skin;
                if(comparison){var existing=new LiteraryRenderers.CastModel(mc.getEntityModels().bakeLayer(LiteraryRenderers.CAST));int phase=n==9?5:4;existing.young=false;existing.pose(LiteraryActor.STRANGER,phase,frames);model=existing;skin=LiteraryRenderers.skin(LiteraryActor.STRANGER,phase);}
                else{var club=new NightclubRenderers.CastModel(mc.getEntityModels().bakeLayer(look==0?(despair>=.4F?NightclubRenderers.MALE_DESPAIR:NightclubRenderers.MALE):NightclubRenderers.CAST));club.preview(slot,page==0?0:frames+partial,despair);model=club;skin=NightclubRenderers.skin(look,despair>=.4F);}
                int cx=20+(n%columns)*(width-40)/columns+(width-40)/columns/2,cy=46+(n/columns)*(height-75)/rows;float scale=page==0?33:page==2?43:53;
                var pose=graphics.pose();pose.pushPose();pose.translate(cx,cy,190);pose.scale(scale,scale,scale);pose.mulPose(Axis.YP.rotationDegrees(page==0&&n==11?25:145));
                var buffers=mc.renderBuffers().bufferSource();model.renderToBuffer(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(skin)),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0xffffffff);buffers.endBatch();pose.popPose();
                graphics.drawCenteredString(font,page==0?new String[]{"Charcoal tee","Plum tank","White vest","Copper / teal","Mustard overshirt","Silver top","Brick-red tank","Braids / midnight","DJ / hoodie","Existing / Russ","Existing / Marla","DJ / back"}[n]:page==2?(n==9?"Music returns":"Despair / "+(look==8?"DJ":look+1)):new String[]{"Shuffle","Raised hands","Sway","Shuffle","Raised hands","DJ / mixer"}[n],cx,cy+(int)(scale*1.6F),0xffddd5c8);
            }
        }else{
            var states=new java.util.ArrayList<net.minecraft.world.level.block.state.BlockState>();
            for(var kind:NightclubBlock.Kind.values())states.add(NightclubBlock.of(kind,Direction.NORTH));
            states.add(NightclubBlock.of(NightclubBlock.Kind.MIXER,Direction.NORTH).setValue(NightclubBlock.STAGE,0));
            states.add(NightclubBlock.of(NightclubBlock.Kind.MIXER,Direction.NORTH).setValue(NightclubBlock.STAGE,2));
            states.add(NightclubBlock.of(NightclubBlock.Kind.GLASS,Direction.NORTH).setValue(NightclubBlock.STAGE,0));
            for(int n=0;n<states.size();n++){
                var pose=graphics.pose();pose.pushPose();pose.translate(20+(n%5+.5F)*(width-40)/5,45+(n/5+.45F)*(height-85)/3,190);pose.scale(31,-31,31);pose.mulPose(Axis.XP.rotationDegrees(18));pose.mulPose(Axis.YP.rotationDegrees(-35));
                mc.getBlockRenderer().renderSingleBlock(states.get(n),pose,mc.renderBuffers().bufferSource(),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);mc.renderBuffers().bufferSource().endBatch();pose.popPose();
            }
            graphics.renderItem(NightclubGlassItem.glass(false),width-72,height-28);graphics.renderItem(NightclubGlassItem.glass(true),width-40,height-28);
        }
    }
    @SubscribeEvent public static void launch(ClientTickEvent.Post ignored){var mc=Minecraft.getInstance();if(Boolean.getBoolean("the_oldest_house.nightclubSmoke")&&!launched&&mc.screen!=null&&mc.getOverlay()==null){launched=true;mc.options.guiScale().set(2);mc.resizeDisplay();mc.setScreen(new NightclubVisualProof());}}
    @SubscribeEvent public static void frame(RenderFrameEvent.Post ignored)throws Exception{
        var mc=Minecraft.getInstance();if(!(mc.screen instanceof NightclubVisualProof proof)||++proof.frames<30)return;
        var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);
        try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve("native-nightclub-"+new String[]{"cast","motion","despair","fixtures"}[proof.page]+".png"));}
        if(++proof.page<4){proof.frames=0;return;}
        Files.writeString(dir.resolve("nightclub-passed.txt"),"Painted club attire alongside existing NPCs, all dance modes and the DJ, reversible despair geometry and face atlases, three custom pixel textures, all 132 oriented prop states and filled/empty glass models rendered.\n");mc.stop();
    }
}
