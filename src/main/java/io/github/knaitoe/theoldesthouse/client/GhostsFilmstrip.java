package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import java.util.List;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/**
 * 0.4.309, the Ghosts Set filmstrips (docs/GHOSTS_ANIMATION_0_4_309.md): motion as images. Each row is one person in one pose; each column
 * is the same pose a few ticks later, so a row reads left to right as the movement. The very model the scene uses renders every frame.
 * Run with {@code gradle runClient -PghostsFilmstrip}; pages are written to build/font-smoke/ghosts-film-*.png.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class GhostsFilmstrip extends Screen {
    /** One row: who, which pose, seen from which side, whether speaking, on which day, from which tick, every how many ticks, face only. */
    record Row(String label,int role,int phase,float yaw,boolean speaking,int day,float start,float step,boolean face){}
    record Page(String name,String title,List<Row> rows){}
    static final int M=LiteraryActor.GHOST_MARJORIE,Y=LiteraryActor.GHOST_MERRY,J=LiteraryActor.GHOST_JOHN,S=LiteraryActor.GHOST_SARAH;
    static final List<Page> PAGES=List.of(
        new Page("strain","The fall, the landing, the wall, a flinch, helping (every 3 ticks)",List.of(
            new Row("Marjorie falls",M,GhostsSet.FALL,180,false,3,0,3,false),new Row("John lands",J,GhostsSet.IMPACT,90,false,3,0,3,false),
            new Row("Marjorie on the wall",M,GhostsSet.WALL,90,false,3,0,6,false),new Row("Merry flinches",Y,GhostsSet.FLINCH,180,false,3,0,3,false),
            new Row("John helps",J,GhostsSet.HELP,90,false,3,0,4,false))),
        new Page("story","The crawl, the slide off the chair, the leap, choking (every 4 ticks)",List.of(
            new Row("Marjorie crawls",M,GhostsSet.CRAWL,90,false,2,0,4,false),new Row("Marjorie slips",M,GhostsSet.SLIP,90,false,2,0,4,false),
            new Row("Marjorie leaps",M,GhostsSet.LEAP,90,false,6,0,4,false),new Row("John chokes",J,GhostsSet.CHOKING,180,false,7,0,4,false),
            new Row("Sarah chokes",S,GhostsSet.CHOKING,180,false,7,0,4,false))),
        new Page("speaking","Faces mid-line (top three) and silent (bottom), every 2 ticks",List.of(
            new Row("John speaking",J,GhostsSet.SIT,180,true,1,0,2,true),new Row("Sarah speaking",S,GhostsSet.SIT,180,true,1,0,2,true),
            new Row("Merry speaking",Y,GhostsSet.SIT,180,true,1,0,2,true),new Row("John silent",J,GhostsSet.SIT,180,false,1,0,2,true))),
        new Page("table","Day one at the table: John and Sarah across a sip (every 10 ticks)",List.of(
            new Row("John, day one",J,GhostsSet.SIT,135,false,1,420,10,false),new Row("Sarah, day one",S,GhostsSet.SIT,135,false,1,262,10,false))),
        new Page("walks","Walking (every 3 ticks): John, Sarah, Merry, Marjorie",List.of(
            new Row("John walks",J,-1,90,false,3,0,3,false),new Row("Sarah walks",S,-1,90,false,3,0,3,false),
            new Row("Merry walks",Y,-1,90,false,3,0,3,false),new Row("Marjorie walks",M,-1,90,false,3,0,3,false))),
        new Page("idle","Standing idle (every 25 ticks), and Marjorie at dinner beside John",List.of(
            new Row("John standing",J,GhostsSet.STAND,180,false,3,0,25,false),new Row("Sarah standing",S,GhostsSet.STAND,180,false,3,0,25,false),
            new Row("Merry standing",Y,GhostsSet.STAND,180,false,3,0,25,false),new Row("Marjorie at dinner",M,GhostsSet.SIT,180,false,2,0,25,false),
            new Row("John at dinner",J,GhostsSet.SIT,180,false,2,0,25,false))),
        new Page("marjorie","Marjorie: young face, long hair, floor clearance and fear",List.of(
            new Row("Marjorie, face",M,GhostsSet.STAND,180,false,1,0,4,true),
            new Row("Long hair, front",M,GhostsSet.STAND,180,false,1,0,4,false),
            new Row("Long hair, back",M,GhostsSet.STAND,0,false,1,0,4,false),
            new Row("Long hair, side",M,GhostsSet.STAND,90,false,1,0,4,false),
            new Row("Crawl and hair",M,GhostsSet.CRAWL,90,false,2,0,4,false),
            new Row("Frightened, bed",M,GhostsSet.BED,180,false,6,0,4,true))),
        new Page("merry","Merry: ponytail and movement; both sisters' faces",List.of(
            new Row("Merry, front",Y,GhostsSet.STAND,180,false,1,0,4,false),
            new Row("Ponytail, back",Y,GhostsSet.STAND,0,false,1,0,4,false),
            new Row("Ponytail, side",Y,GhostsSet.STAND,90,false,1,0,4,false),
            new Row("Merry walks",Y,-1,90,false,1,0,3,false),
            new Row("Merry, face",Y,GhostsSet.STAND,180,false,1,0,4,true),
            new Row("Marjorie, face",M,GhostsSet.STAND,180,false,1,0,4,true))),
        new Page("sisters","Merry's ponytail / Marjorie's smaller eyes",List.of()));
    static final int FRAMES=8;
    private int page,frames;private static boolean launched;
    public GhostsFilmstrip(){super(Component.literal("The Barrett home: filmstrips"));}
    @SubscribeEvent public static void launch(ClientTickEvent.Post ignored){
        var mc=Minecraft.getInstance();
        if(Boolean.getBoolean("the_oldest_house.ghostsFilmstrip")&&!launched&&mc.screen!=null&&mc.getOverlay()==null){launched=true;mc.options.guiScale().set(2);mc.resizeDisplay();mc.setScreen(new GhostsFilmstrip());}
    }
    @Override public void renderBackground(GuiGraphics g,int x,int y,float d){}
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        if(page>=PAGES.size())return;
        var mc=Minecraft.getInstance();var p=PAGES.get(page);var buffers=mc.renderBuffers().bufferSource();
        g.fill(0,0,width,height,0xff292821);g.drawString(font,"Filmstrip / "+p.title(),10,8,0xffe7dbc3,false);g.flush();
        if(p.name().equals("sisters")){sisters(g,buffers);return;}
        int rows=p.rows().size();float rowH=(height-30)/(float)rows,colW=(width-110)/(float)FRAMES;
        for(int r=0;r<rows;r++){
            var row=p.rows().get(r);float cy=26+(r+.5F)*rowH;
            g.drawString(font,row.label(),6,(int)cy,0xffe7dbc3,false);
            for(int f=0;f<FRAMES;f++){
                float age=row.start()+f*row.step();
                GhostsCastModel.previewSpeaking=row.speaking();GhostsCastModel.previewDay=row.day();
                var model=new GhostsCastModel(mc.getEntityModels().bakeLayer(GhostsCastModel.LAYER));model.preview(row.role(),row.phase(),age);
                GhostsCastModel.previewSpeaking=false;GhostsCastModel.previewDay=0;
                if(row.face()){model.body.visible=model.leftArm.visible=model.rightArm.visible=model.leftLeg.visible=model.rightLeg.visible=false;model.head.x=model.head.y=model.head.z=0;model.head.yRot=0;model.head.getChild("long_hair").visible=false;model.head.getChild("ponytail").visible=false;}
                var pose=g.pose();pose.pushPose();
                float scale=row.face()?Math.min(rowH*.85F,colW*.85F):rowH*.32F*(row.role()==Y?.58F:row.role()==M?.86F:1);
                pose.translate(100+(f+.5F)*colW,cy+(row.face()?0:rowH*.36F),190);pose.scale(scale,scale,scale);
                pose.mulPose(Axis.YP.rotationDegrees(row.yaw()));
                if(row.face())pose.translate(0,.25,0);else pose.translate(0,-1.5,0);
                model.renderToBuffer(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(GhostsCastModel.skin(row.role(),row.phase()))),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0xffffffff);
                buffers.endBatch();pose.popPose();
                if(r==0)g.drawCenteredString(font,"+"+(int)(f*row.step()),(int)(100+(f+.5F)*colW),18,0xff9a9282);
            }
        }
    }
    private void sisters(GuiGraphics g,MultiBufferSource.BufferSource buffers){
        var mc=Minecraft.getInstance();float col=(width-40)/4F;
        for(int n=0;n<4;n++){
            int role=n==3?M:Y;float yaw=n==1?0:n==2?90:180,cx=20+(n+.5F)*col;
            for(boolean detail:new boolean[]{false,true}){
                GhostsCastModel.previewDay=1;
                var model=new GhostsCastModel(mc.getEntityModels().bakeLayer(GhostsCastModel.LAYER));model.preview(role,GhostsSet.STAND,40);
                GhostsCastModel.previewDay=0;
                if(detail){
                    model.body.visible=model.leftArm.visible=model.rightArm.visible=model.leftLeg.visible=model.rightLeg.visible=false;
                    model.head.x=model.head.y=model.head.z=model.head.xRot=model.head.yRot=model.head.zRot=0;
                    model.head.getChild("long_hair").visible=false;model.head.getChild("ponytail").visible=n==1||n==2;
                }
                var pose=g.pose();pose.pushPose();pose.translate(cx,height*(detail?.83F:.61F),190);
                float size=detail?90:120*(role==Y?.58F:.86F);pose.scale(size,size,size);pose.mulPose(Axis.YP.rotationDegrees(yaw));
                pose.translate(0,detail?.25:-1.5,0);
                model.renderToBuffer(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(GhostsCastModel.skin(role,GhostsSet.STAND))),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0xffffffff);
                buffers.endBatch();pose.popPose();
            }
            g.drawCenteredString(font,n==0?"Merry / front":n==1?"Merry / back":n==2?"Merry / side":"Marjorie",(int)cx,(int)(height*.65F),0xffe7dbc3);
        }
        g.flush();
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();if(!Boolean.getBoolean("the_oldest_house.ghostsFilmstrip")||!(mc.screen instanceof GhostsFilmstrip film)||film.page>=PAGES.size()||++film.frames<12)return;
        var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);
        try(NativeImage image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve("ghosts-film-"+PAGES.get(film.page).name()+".png"));}
        if(++film.page<PAGES.size()){film.frames=0;return;}
        Files.writeString(dir.resolve("ghosts-film-passed.txt"),PAGES.size()+" filmstrip pages written.\n");mc.stop();
    }
}
