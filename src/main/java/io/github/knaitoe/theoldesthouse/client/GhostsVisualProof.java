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
        var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xff292821);g.drawString(font,"The Barrett home / "+(page==0?"cast and human poses":page==3?"final supper / mouth animation":"native furnishings and evidence"),10,10,0xffe7dbc3,false);g.flush();
        var buffers=mc.renderBuffers().bufferSource();
        if(page==0||page==3){
            for(int n=0;n<(page==0?17:12);n++){
                int role=page==3?new int[]{LiteraryActor.GHOST_MARJORIE,LiteraryActor.GHOST_JOHN,LiteraryActor.GHOST_SARAH}[n%3]:n<6?GhostsSet.ROLES[n]:n==15?LiteraryActor.GHOST_JOHN:LiteraryActor.GHOST_MARJORIE,phase=page==3?(n<3?GhostsSet.EATING:n<6?GhostsSet.CHOKING:GhostsSet.DEAD):n<6?GhostsSet.STAND:n-6;
                var model=new GhostsCastModel(mc.getEntityModels().bakeLayer(GhostsCastModel.LAYER));float age=page==3?frames+(n/3)*13:frames;model.preview(role,phase,age);
                var skin=GhostsCastModel.skin(role,phase);if(mc.getResourceManager().getResource(skin).isEmpty())throw new IllegalStateException("Missing filming skin "+skin);
                var pose=g.pose();pose.pushPose();float centreY=38+(n/5+.73F)*(height-70)/4;pose.translate(24+(n%5+.5F)*(width-48)/5,centreY,190);float size=role==LiteraryActor.GHOST_MERRY?28*.58F:28;pose.scale(size,size,size);pose.mulPose(Axis.YP.rotationDegrees(145));pose.translate(0,-1.5,0);
                if(phase==GhostsSet.BED)LiteraryRenderers.bedPose(pose,1);model.renderToBuffer(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(skin)),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0xffffffff);if(page==3)GhostsMouthLayer.preview(model,role,phase,age,pose,buffers,LightTexture.FULL_BRIGHT);buffers.endBatch();pose.popPose();
                g.drawCenteredString(font,page==3?GhostsSet.PEOPLE[role-LiteraryActor.GHOST_MARJORIE]+" / "+phase:n<6?GhostsSet.PEOPLE[n]:"pose "+phase,24+(n%5)*(width-48)/5+(width-48)/10,(int)centreY+12,0xffe7dbc3);
            }
        }else if(page==1){
            var camera=new net.minecraft.world.item.ItemStack(LiteraryRegistry.CAMCORDER.get());var mesh=mc.getItemRenderer().getModel(camera,null,null,0);if(mesh==mc.getModelManager().getMissingModel()||!mesh.isGui3d())throw new IllegalStateException("The production camera requires its native mesh");g.renderItem(camera,width-24,height-24);
            int n=0;for(var kind:GhostsSetBlock.Kind.values()){
                for(var face:Direction.Plane.HORIZONTAL)if(mc.getBlockRenderer().getBlockModel(GhostsSetBlock.of(kind,face))==mc.getModelManager().getMissingModel())throw new IllegalStateException("Missing filming fixture "+kind+" "+face);
                var pose=g.pose();pose.pushPose();pose.translate(20+(n%7+.5F)*(width-40)/7,36+(n/7+.7F)*(height-60)/5,190);pose.scale(23,-23,23);pose.mulPose(Axis.XP.rotationDegrees(20));pose.mulPose(Axis.YP.rotationDegrees(-30));
                mc.getBlockRenderer().renderSingleBlock(GhostsSetBlock.of(kind,Direction.NORTH),pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);buffers.endBatch();pose.popPose();n++;
            }
        }else{
            int n=0;for(var facing:Direction.Plane.HORIZONTAL){var pose=g.pose();pose.pushPose();pose.translate(30+(n+.5F)*(width-60)/4,height*.58F,190);pose.scale(48,-48,48);pose.mulPose(Axis.XP.rotationDegrees(25));pose.mulPose(Axis.YP.rotationDegrees(-30));
                mc.getBlockRenderer().renderSingleBlock(net.minecraft.world.level.block.Blocks.LECTERN.defaultBlockState().setValue(net.minecraft.world.level.block.LecternBlock.FACING,facing),pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);GhostsCallSheetRenderer.render(pose,buffers,facing,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);buffers.endBatch();pose.popPose();n++;}
            for(boolean open:new boolean[]{false,true}){var state=LiteraryRegistry.GHOST_CASE.get().defaultBlockState().setValue(GhostsCaseBlock.OPEN,open);if(mc.getBlockRenderer().getBlockModel(state)==mc.getModelManager().getMissingModel())throw new IllegalStateException("Missing native case lid "+open);var pose=g.pose();pose.pushPose();pose.translate(width*(open?.7F:.3F),height*.8F,190);pose.scale(27,-27,27);pose.mulPose(Axis.XP.rotationDegrees(25));pose.mulPose(Axis.YP.rotationDegrees(-30));mc.getBlockRenderer().renderSingleBlock(state,pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);buffers.endBatch();pose.popPose();}
            GhostsTakeClient.renderCaption(g,width,height,"Marjorie: You will die, Merry. So will everyone sitting here. My hat isn't for dead things.",frames);
        }
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();if(!Boolean.getBoolean("the_oldest_house.fontSmoke")||!(mc.screen instanceof GhostsVisualProof proof)||++proof.frames<24)return;
        var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);try(NativeImage image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve("native-ghosts-"+(proof.page==0?"cast":proof.page==1?"fixtures":proof.page==2?"paper-and-cases":"final-dinner")+".png"));}
        if(++proof.page<4){proof.frames=0;return;}Files.writeString(dir.resolve("ghosts-passed.txt"),"Six painted cast skins, fourteen jointed human poses, animated mouth strands/drops, eight cast atlases, 140 fixture states, native case lids, four fitted call-sheet orientations and rolling native-font caption rendered.\n");mc.setScreen(new LastStopCastVisualProof());
    }
}
