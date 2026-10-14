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
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Baked native home meshes, six named skins and the very same jointed poses used in the scene. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class GhostsVisualProof extends Screen {
    private int page,frames;
    private static boolean launched;
    public GhostsVisualProof(){super(Component.literal("The Barrett home — native filming assets"));}
    @SubscribeEvent public static void launch(ClientTickEvent.Post ignored){var mc=Minecraft.getInstance();if(Boolean.getBoolean("the_oldest_house.ghostsSmoke")&&!launched&&mc.screen!=null&&mc.getOverlay()==null){launched=true;mc.options.guiScale().set(2);mc.resizeDisplay();NativeModelTextures.verify();mc.setScreen(new GhostsVisualProof());}}
    @Override protected void init(){var sample=new net.minecraft.nbt.CompoundTag();for(int d=1;d<=7;d++){var log=new net.minecraft.nbt.CompoundTag();for(String name:new String[]{"Marjorie","Merry","John","Sarah","UnderTable","TableFittings","DrawerMechanism","Handholds","Playhouse","PrivateConfession","FinalMeal"})log.putInt("Frames_"+name,40);log.putInt("LeadTicks_Sarah",40);sample.put("FilmLog"+d,log);}sample.putBoolean("RespectedPrivacy",true);sample.putString("Confession","I kept the fittings and the mechanism in the tape, even when the cut left them out.");var mc=Minecraft.getInstance();for(int d=1;d<=7;d++)for(boolean raw:new boolean[]{true,false})for(var page:GhostsSet.edit("Native proof",sample,d,raw).get(net.minecraft.core.component.DataComponents.WRITTEN_BOOK_CONTENT).pages())if(mc.font.split(page.raw(),114).size()>14)throw new IllegalStateException("The filming account exceeds its native lectern page");}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float d){}
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xff292821);g.drawString(font,"The Barrett home / "+pageName(page),10,10,0xffe7dbc3,false);g.flush();
        var buffers=mc.renderBuffers().bufferSource();
        if(page>=6&&page<=11){
            reviewCast(g,buffers);
        }else if(page==15){
            for(int n=0;n<12;n++){var pose=g.pose();pose.pushPose();pose.translate(24+(n%6+.5F)*(width-48)/6,52+(n/6+.6F)*(height-72)/2,190);pose.scale(32,-32,32);pose.mulPose(Axis.XP.rotationDegrees(18));pose.mulPose(Axis.YP.rotationDegrees(145));GhostsPropRenderer.preview(n<6?LiteraryActor.GHOST_DRAWER:LiteraryActor.GHOST_PLAYHOUSE,(n%6)*.4F,pose,buffers,LightTexture.FULL_BRIGHT);buffers.endBatch();pose.popPose();}
        }else if(page==4||page==5){
            var p=g.pose();p.pushPose();p.translate(width/2.,height*.72,180);p.scale(48,-48,48);p.mulPose(Axis.XP.rotationDegrees(18));p.mulPose(Axis.YP.rotationDegrees(-35));
            for(int bx=-1;bx<=1;bx++)for(int z=-1;z<=0;z++){p.pushPose();p.translate(bx-.5,0,z);mc.getBlockRenderer().renderSingleBlock(GhostsSetBlock.of(GhostsSetBlock.Kind.TABLE,Direction.NORTH),p,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);p.popPose();}
            for(int i=0;i<4;i++){boolean left=i==0||i==2;int row=i<2?-1:1;
                p.pushPose();p.translate(left?-3.5:2.5,0,row);mc.getBlockRenderer().renderSingleBlock(GhostsSetBlock.of(GhostsSetBlock.Kind.CHAIR,left?Direction.EAST:Direction.WEST),p,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);p.popPose();
                int role=GhostsSet.ROLES[i],phase=page==5&&role==LiteraryActor.GHOST_MERRY?GhostsSet.PRAY:GhostsSet.SIT;float size=i==0?.86F:i==1?.58F:1F;var model=new GhostsCastModel(mc.getEntityModels().bakeLayer(GhostsCastModel.LAYER));model.preview(role,phase,frames);
                if(Math.abs(model.leftLeg.xRot+(float)Math.PI/2)>.001||Math.abs(model.leftLeg.getChild("knee").xRot-(float)Math.PI/2)>.001)throw new IllegalStateException("Seated thighs and knees must fit the actual chair, including Merry's prayer pose");
                p.pushPose();p.translate(left?-2.9:2.9,GhostsSet.seatedHeight(size),row+.5);p.mulPose(Axis.YP.rotationDegrees(left?270:90));p.scale(-size,-size,size);p.translate(0,-1.501,0);model.renderToBuffer(p,buffers.getBuffer(RenderType.entityCutoutNoCull(GhostsCastModel.skin(role,phase))),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0xffffffff);p.popPose();
            }buffers.endBatch();p.popPose();g.drawCenteredString(font,page==5?"Native dining chairs / Merry praying on her original seat":"Native dining chairs / four actual cast scales and seated poses",width/2,34,0xffe7dbc3);
        }else if(page==0||page==3){
            for(int n=0;n<(page==0?17:12);n++){
                int role=page==3?new int[]{LiteraryActor.GHOST_MARJORIE,LiteraryActor.GHOST_JOHN,LiteraryActor.GHOST_SARAH}[n%3]:n<6?GhostsSet.ROLES[n]:n==15?LiteraryActor.GHOST_JOHN:LiteraryActor.GHOST_MARJORIE,phase=page==3?(n<3?GhostsSet.EATING:n<6?GhostsSet.CHOKING:GhostsSet.DEAD):n<6?GhostsSet.STAND:n-6;
                var model=new GhostsCastModel(mc.getEntityModels().bakeLayer(GhostsCastModel.LAYER));float age=page==3?frames+(n/3)*13:frames;model.preview(role,phase,age);
                var skin=GhostsCastModel.skin(role,phase);if(mc.getResourceManager().getResource(skin).isEmpty())throw new IllegalStateException("Missing filming skin "+skin);
                var pose=g.pose();pose.pushPose();float centreY=38+(n/5+.73F)*(height-70)/4;pose.translate(24+(n%5+.5F)*(width-48)/5,centreY,190);float size=28*(role==LiteraryActor.GHOST_MERRY?.58F:role==LiteraryActor.GHOST_MARJORIE?.86F:1);pose.scale(size,size,size);pose.mulPose(Axis.YP.rotationDegrees(145));pose.translate(0,-1.5,0);
                if(phase==GhostsSet.BED)LiteraryRenderers.bedPose(pose,1);model.renderToBuffer(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(skin)),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0xffffffff);if(page==3)GhostsMouthLayer.preview(model,role,phase,age,pose,buffers,LightTexture.FULL_BRIGHT);buffers.endBatch();pose.popPose();
                g.drawCenteredString(font,page==3?GhostsSet.PEOPLE[role-LiteraryActor.GHOST_MARJORIE]+" / "+phase:n<6?GhostsSet.PEOPLE[n]:"pose "+phase,24+(n%5)*(width-48)/5+(width-48)/10,(int)centreY+12,0xffe7dbc3);
            }
        }else if(page==1||page>=12&&page<=14){
            var camera=new net.minecraft.world.item.ItemStack(LiteraryRegistry.CAMCORDER.get());var mesh=mc.getItemRenderer().getModel(camera,null,null,0);if(mesh==mc.getModelManager().getMissingModel()||!mesh.isGui3d())throw new IllegalStateException("The production camera requires its native mesh");g.renderItem(camera,width-24,height-24);
            int n=0;for(var kind:GhostsSetBlock.Kind.values()){
                for(var face:Direction.Plane.HORIZONTAL)if(mc.getBlockRenderer().getBlockModel(GhostsSetBlock.of(kind,face))==mc.getModelManager().getMissingModel())throw new IllegalStateException("Missing filming fixture "+kind+" "+face);
                var pose=g.pose();pose.pushPose();pose.translate(20+(n%7+.5F)*(width-40)/7,36+(n/7+.7F)*(height-60)/((GhostsSetBlock.Kind.values().length+6)/7),190);pose.scale(23,-23,23);pose.mulPose(Axis.XP.rotationDegrees(20));pose.mulPose(Axis.YP.rotationDegrees(-30));
                mc.getBlockRenderer().renderSingleBlock(GhostsSetBlock.of(kind,page==12?Direction.EAST:page==13?Direction.SOUTH:page==14?Direction.WEST:Direction.NORTH),pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);buffers.endBatch();pose.popPose();n++;
            }
        }else{
            int n=0;for(var facing:Direction.Plane.HORIZONTAL){var pose=g.pose();pose.pushPose();pose.translate(30+(n+.5F)*(width-60)/4,height*.58F,190);pose.scale(48,-48,48);pose.mulPose(Axis.XP.rotationDegrees(25));pose.mulPose(Axis.YP.rotationDegrees(-30));
                mc.getBlockRenderer().renderSingleBlock(net.minecraft.world.level.block.Blocks.LECTERN.defaultBlockState().setValue(net.minecraft.world.level.block.LecternBlock.FACING,facing),pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);GhostsCallSheetRenderer.render(pose,buffers,facing,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);buffers.endBatch();pose.popPose();n++;}
            for(boolean open:new boolean[]{false,true}){var state=LiteraryRegistry.GHOST_CASE.get().defaultBlockState().setValue(GhostsCaseBlock.OPEN,open);if(mc.getBlockRenderer().getBlockModel(state)==mc.getModelManager().getMissingModel())throw new IllegalStateException("Missing native case lid "+open);var pose=g.pose();pose.pushPose();pose.translate(width*(open?.7F:.3F),height*.8F,190);pose.scale(27,-27,27);pose.mulPose(Axis.XP.rotationDegrees(25));pose.mulPose(Axis.YP.rotationDegrees(-30));mc.getBlockRenderer().renderSingleBlock(state,pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);buffers.endBatch();pose.popPose();}
            GhostsTakeClient.renderCaption(g,width,height,"Marjorie: You will die, Merry. So will everyone sitting here. My hat isn't for dead things.",frames);
        }
    }
    private void reviewCast(GuiGraphics g,net.minecraft.client.renderer.MultiBufferSource.BufferSource buffers){
        var mc=Minecraft.getInstance();int count=page<=8?18:page==9?24:12;
        for(int n=0;n<count;n++){
            int role=page<=8||page==10?LiteraryActor.GHOST_MARJORIE:page==9?n>=21?LiteraryActor.GHOST_JOHN:GhostsSet.ROLES[(n/3)%6]:new int[]{LiteraryActor.GHOST_MARJORIE,LiteraryActor.GHOST_JOHN,LiteraryActor.GHOST_SARAH}[n%3];
            int phase=page<=8?n:page==9?(n/3==6?GhostsSet.VOMIT:n/3==7?GhostsSet.HAND:GhostsSet.STAND):page==10?(n<6?GhostsSet.STAND:GhostsSet.VOMIT):(n<3?GhostsSet.CHOKING:n<6?GhostsSet.DEAD:n<9?GhostsSet.HELP:GhostsSet.FALL);
            float age=page==9?93-Math.floorMod(role*13,93)+3:page==10?93-Math.floorMod(role*13,93)+(n%6):frames+n*11;
            var model=new GhostsCastModel(mc.getEntityModels().bakeLayer(GhostsCastModel.LAYER));model.preview(role,phase,age);
            boolean faceOnly=page==9||page==10;
            if(faceOnly){model.body.visible=model.leftArm.visible=model.rightArm.visible=model.leftLeg.visible=model.rightLeg.visible=false;model.head.x=model.head.y=model.head.z=0;model.head.xRot=model.head.yRot=model.head.zRot=0;}
            var p=g.pose();p.pushPose();int columns=6,rows=(count+5)/6;float cy=35+(n/columns+.55F)*(height-60)/rows;
            p.translate(20+(n%columns+.5F)*(width-40)/columns,cy,190);float size=faceOnly?76:22*(role==LiteraryActor.GHOST_MERRY?.58F:role==LiteraryActor.GHOST_MARJORIE?.86F:1);p.scale(size,size,size);
            p.mulPose(Axis.YP.rotationDegrees(page==7?90:page==8?0:page==9?180-(n%3)*90:180));if(!faceOnly){p.translate(0,-1.5,0);if(phase==GhostsSet.BED)LiteraryRenderers.bedPose(p,1);}else p.translate(0,.25,0);
            model.renderToBuffer(p,buffers.getBuffer(RenderType.entityCutoutNoCull(GhostsCastModel.skin(role,phase))),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0xffffffff);
            if(!faceOnly)GhostsMouthLayer.preview(model,role,phase,age,p,buffers,LightTexture.FULL_BRIGHT);buffers.endBatch();p.popPose();
            g.drawCenteredString(font,page==10?(n<6?"normal / ":"dinner / ")+(n%6):GhostsSet.PEOPLE[role-LiteraryActor.GHOST_MARJORIE].replace("Father Wanderly","Wanderly")+" / "+phase,20+(n%columns)*(width-40)/columns+(width-40)/12,(int)cy+(faceOnly?30:15),0xffe7dbc3);
        }
    }
    private static String pageName(int page){return switch(page){case 0->"cast";case 1->"fixtures";case 2->"paper-and-cases";case 3->"final-dinner";case 4->"seating";case 5->"seating-prayer";case 6->"all-poses-front";case 7->"all-poses-side";case 8->"all-poses-back";case 9->"eight-atlases-and-closed-eyes";case 10->"marjorie-blink-and-jaw";case 11->"help-fall-and-death";case 12->"fixtures-east";case 13->"fixtures-south";case 14->"fixtures-west";default->"prop-transitions";};}
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();if(!(Boolean.getBoolean("the_oldest_house.fontSmoke")||Boolean.getBoolean("the_oldest_house.ghostsSmoke"))||!(mc.screen instanceof GhostsVisualProof proof)||++proof.frames<24)return;
        var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);try(NativeImage image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve("native-ghosts-"+pageName(proof.page)+".png"));}
        if(++proof.page<16){proof.frames=0;return;}Files.writeString(dir.resolve("ghosts-passed.txt"),"Sixteen native views: all eighteen human poses from front/side/back, eight unchanged painted atlases, normal/dinner blink and jaw frames, helping/falling/distinct deaths and mouth strands/drops, all four fixture orientations, drawer/cardboard transitions, case lids, four fitted call-sheet orientations, native caption and four fitted dining chairs including Merry's prayer pose rendered.\n");if(Boolean.getBoolean("the_oldest_house.ghostsSmoke"))mc.stop();else mc.setScreen(new LastStopCastVisualProof());
    }
}
