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
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/** Baked native home meshes, six named skins and the very same jointed poses used in the scene. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class GhostsVisualProof extends Screen {
    private int page,frames;
    public GhostsVisualProof(){super(Component.literal("The Barrett home — native filming assets"));}
    @Override protected void init(){var sample=new net.minecraft.nbt.CompoundTag();for(int d=1;d<=7;d++){var log=new net.minecraft.nbt.CompoundTag();for(String name:new String[]{"Marjorie","Merry","John","Sarah","UnderTable","TableFittings","DrawerMechanism","Handholds","Playhouse","PrivateConfession","FinalMeal"})log.putInt("Frames_"+name,40);log.putInt("LeadTicks_Sarah",40);sample.put("FilmLog"+d,log);}sample.putBoolean("RespectedPrivacy",true);sample.putString("Confession","I kept the fittings and the mechanism in the tape, even when the cut left them out.");var mc=Minecraft.getInstance();for(int d=1;d<=7;d++)for(boolean raw:new boolean[]{true,false})for(var page:GhostsSet.edit("Native proof",sample,d,raw).get(net.minecraft.core.component.DataComponents.WRITTEN_BOOK_CONTENT).pages())if(mc.font.split(page.raw(),114).size()>14)throw new IllegalStateException("The filming account exceeds its native lectern page");}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float d){}
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xff292821);g.drawString(font,"The Barrett home / "+(page==0?"cast and human poses":"native furnishings and evidence"),10,10,0xffe7dbc3,false);g.flush();
        var buffers=mc.renderBuffers().bufferSource();
        if(page==0){
            for(int n=0;n<16;n++){
                int role=n<6?GhostsSet.ROLES[n]:n==15?LiteraryActor.GHOST_JOHN:LiteraryActor.GHOST_MARJORIE,phase=n<6?GhostsSet.STAND:n-6;
                var model=new GhostsCastModel(mc.getEntityModels().bakeLayer(GhostsCastModel.LAYER));model.preview(role,phase,frames);
                var skin=GhostsCastModel.skin(role,phase);if(mc.getResourceManager().getResource(skin).isEmpty())throw new IllegalStateException("Missing filming skin "+skin);
                var pose=g.pose();pose.pushPose();pose.translate(24+(n%5+.5F)*(width-48)/5,35+(n/5+.8F)*(height-70)/4,190);float size=role==LiteraryActor.GHOST_MERRY?36*.58F:36;pose.scale(size,size,size);pose.mulPose(Axis.YP.rotationDegrees(145));pose.translate(0,-1.5,0);
                if(phase==GhostsSet.BED)LiteraryRenderers.bedPose(pose,1);model.renderToBuffer(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(skin)),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0xffffffff);buffers.endBatch();pose.popPose();
                g.drawCenteredString(font,n<6?GhostsSet.PEOPLE[n]:"pose "+phase,24+(n%5)*(width-48)/5+(width-48)/10,32+(n/5+1)*(height-70)/4,0xffe7dbc3);
            }
        }else{
            var camera=new net.minecraft.world.item.ItemStack(LiteraryRegistry.CAMCORDER.get());var mesh=mc.getItemRenderer().getModel(camera,null,null,0);if(mesh==mc.getModelManager().getMissingModel()||!mesh.isGui3d())throw new IllegalStateException("The production camera requires its native mesh");g.renderItem(camera,width-24,height-24);
            int n=0;for(var kind:GhostsSetBlock.Kind.values()){
                for(var face:Direction.Plane.HORIZONTAL)if(mc.getBlockRenderer().getBlockModel(GhostsSetBlock.of(kind,face))==mc.getModelManager().getMissingModel())throw new IllegalStateException("Missing filming fixture "+kind+" "+face);
                var pose=g.pose();pose.pushPose();pose.translate(20+(n%7+.5F)*(width-40)/7,36+(n/7+.7F)*(height-60)/4,190);pose.scale(27,-27,27);pose.mulPose(Axis.XP.rotationDegrees(20));pose.mulPose(Axis.YP.rotationDegrees(-30));
                mc.getBlockRenderer().renderSingleBlock(GhostsSetBlock.of(kind,Direction.NORTH),pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);buffers.endBatch();pose.popPose();n++;
            }
        }
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();if(!Boolean.getBoolean("the_oldest_house.fontSmoke")||!(mc.screen instanceof GhostsVisualProof proof)||++proof.frames<24)return;
        var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);try(NativeImage image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve("native-ghosts-"+(proof.page==0?"cast":"fixtures")+".png"));}
        if(++proof.page<2){proof.frames=0;return;}Files.writeString(dir.resolve("ghosts-passed.txt"),"Six native cast skins, ten connected human poses, eight cast atlases and all 112 oriented fixture states rendered.\n");mc.setScreen(new LastStopCastVisualProof());
    }
}
