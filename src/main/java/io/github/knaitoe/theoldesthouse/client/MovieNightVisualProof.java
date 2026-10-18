package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/**
 * Movie Night (0.4.102) on the production code: five frames of *Teeth of Summer* built by MovieFilm from the drawn atlas,
 * the shark on its own model and skin, and the lake's props with the projector's shadows on the film. Checks confirm each
 * frame is a picture rather than a blank, the beach is brighter than the night sea, the last title appears, and the shark
 * is long with its fin standing clear of the water; they report after every view is saved. Two more views show the
 * audience's things with the lake's own boards, and the painted canoes on the lake house canoe's hull.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class MovieNightVisualProof extends Screen {
    private record View(String name,int shot,int tick){}
    private static final View[] VIEWS={new View("the title",1,80),new View("the night swim",2,160),new View("the fin",3,120),new View("the rise",8,115),
            new View("dawn, and THE END?",11,220),new View("the shark",-1,0),new View("the props and the shadows",4,90),
            new View("the audience's things and the boards",-1,0),new View("the painted canoes",-1,0)};
    private final List<String> problems=new ArrayList<>();
    private int page,frames;
    public MovieNightVisualProof(){super(Component.literal("Movie Night"));}
    private static long time(int shot,int tick){return MovieNight.shotStart(shot)+tick;}

    @Override protected void init(){
        problems.clear();
        double[] beach=stats(MovieFilm.pixels(time(4,90))),night=stats(MovieFilm.pixels(time(7,90)));
        if(beach==null||night==null){problems.add("the film atlas could not be read");return;}
        for(var v:VIEWS){if(v.shot()<0)continue;var s=stats(MovieFilm.pixels(time(v.shot(),v.tick())));if(s[1]<10)problems.add("'"+v.name()+"' is a blank frame: "+Arrays.toString(s));}
        if(beach[0]<night[0]+30)problems.add("the morning beach is not brighter than the night sea: "+beach[0]+" against "+night[0]);
        int[] before=MovieFilm.pixels(time(11,100)),after=MovieFilm.pixels(time(11,220));double diff=0;int n=0;
        for(int y=18;y<34;y++)for(int x=32;x<128;x++){int i=y*MovieFilm.W+x;diff+=Math.abs((before[i]>>8&255)-(after[i]>>8&255));n++;}
        if(diff/n<12)problems.add("THE END? does not come up over the dawn: "+diff/n);
        int[] leader=MovieFilm.pixels(time(0,10)),later=MovieFilm.pixels(time(0,50));diff=0;n=0;
        for(int y=31;y<59;y++)for(int x=70;x<90;x++){int i=y*MovieFilm.W+x;diff+=Math.abs((leader[i]&255)-(later[i]&255));n++;}
        if(diff/n<6)problems.add("the leader does not count down: "+diff/n);
        var shark=new Bounds();var p=new PoseStack();var model=new LakeSharkModel(Minecraft.getInstance().getEntityModels().bakeLayer(LakeSharkModel.LAYER));
        p.mulPose(Axis.YP.rotationDegrees(180));p.scale(-1,-1,1);model.setupAnim(null,0,0,0,0,0);model.renderToBuffer(p,shark,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,-1);
        if(shark.z1-shark.z0<4.5F||shark.z1-shark.z0>6.2F)problems.add("the shark is not a large one, nose to tail: "+shark);
        if(shark.y1<1.5F)problems.add("its fin does not stand clear above its back: "+shark);
        var hull=new Bounds();var canoe=new LakeHouseCanoeModel<LakeCanoeEntity>(Minecraft.getInstance().getEntityModels().bakeLayer(LakeHouseCanoeModel.LAYER));canoe.renderToBuffer(new PoseStack(),hull,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,-1);
        if(hull.x1-hull.x0<38)problems.add("the painted canoes are not a long, narrow canoe hull: "+hull);
        for(var paint:LakeCanoeRenderer.PAINT)if(Minecraft.getInstance().getResourceManager().getResource(paint).isEmpty())problems.add("a canoe's paint is missing: "+paint);
    }
    /** Mean and spread of a frame's brightness. */
    private static double[] stats(int[] rgb){
        if(rgb==null)return null;double sum=0,sq=0;
        for(int c:rgb){double l=.3*(c>>16&255)+.59*(c>>8&255)+.11*(c&255);sum+=l;sq+=l*l;}
        double mean=sum/rgb.length;return new double[]{mean,Math.sqrt(Math.max(0,sq/rgb.length-mean*mean))};
    }
    private static final class Bounds implements VertexConsumer {
        float x0=Float.POSITIVE_INFINITY,y0=x0,z0=x0,x1=Float.NEGATIVE_INFINITY,y1=x1,z1=x1;
        public VertexConsumer addVertex(float x,float y,float z){x0=Math.min(x0,x);y0=Math.min(y0,y);z0=Math.min(z0,z);x1=Math.max(x1,x);y1=Math.max(y1,y);z1=Math.max(z1,z);return this;}
        public VertexConsumer setColor(int r,int g,int b,int a){return this;}public VertexConsumer setUv(float u,float v){return this;}public VertexConsumer setUv1(int u,int v){return this;}public VertexConsumer setUv2(int u,int v){return this;}public VertexConsumer setNormal(float x,float y,float z){return this;}
        @Override public String toString(){return String.format(Locale.ROOT,"x %.2f..%.2f y %.2f..%.2f z %.2f..%.2f",x0,x1,y0,y1,z0,z1);}
    }

    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    @Override public void render(GuiGraphics g,int mouseX,int mouseY,float partial){
        var view=VIEWS[page];g.fill(0,0,width,height,0xff0d1018);g.drawString(font,"Movie Night / "+view.name(),10,9,0xfff2ece0,false);
        if(view.shot()>=0&&page!=6){film(g,view,width*.5F,height*.52F,Math.min((width-20)/(float)MovieFilm.W,(height-40)/(float)MovieFilm.H));return;}
        if(page==5){shark(g);return;}
        if(page==7){things(g);return;}
        if(page==8){canoes(g);return;}
        // A smaller frame with a rower's head and something on all fours thrown across it, and the props below.
        float scale=Math.min((width-20)/(float)MovieFilm.W,(height*.55F)/MovieFilm.H);film(g,view,width*.5F,height*.36F,scale);
        float left=width*.5F-MovieFilm.W*scale/2,top=height*.36F-MovieFilm.H*scale/2;
        shadow(g,MovieFilm.ROWER,left+30*scale,top+48*scale,scale*1.1F);shadow(g,MovieFilm.CRAWLER,left+96*scale,top+52*scale,scale*.9F);
        props(g);
    }
    private void film(GuiGraphics g,View view,float cx,float cy,float scale){
        var frame=MovieFilm.frame(time(view.shot(),view.tick()));if(frame==null)return;
        var p=g.pose();p.pushPose();p.translate(cx-MovieFilm.W*scale/2,cy-MovieFilm.H*scale/2,0);p.scale(scale,scale,1);
        g.blit(frame,0,0,0,0,MovieFilm.W,MovieFilm.H,MovieFilm.W,MovieFilm.H);p.popPose();
        var line=MovieFilm.caption(time(view.shot(),view.tick()));
        if(line!=null)g.drawCenteredString(font,line,(int)cx,(int)(cy+MovieFilm.H*scale/2-14),0xfff2eedc);
    }
    private static void shadow(GuiGraphics g,MovieFilm.Sprite s,float x,float y,float scale){
        var p=g.pose();p.pushPose();p.translate(x,y,10);p.scale(scale,scale,1);
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();g.setColor(0,0,0,.85F);
        g.blit(MovieFilm.ATLAS,0,0,s.x(),s.y(),s.w(),s.h(),640,320);g.setColor(1,1,1,1);p.popPose();
    }
    private void shark(GuiGraphics g){
        var mc=Minecraft.getInstance();var out=mc.renderBuffers().bufferSource();var p=g.pose();float scale=Math.min(width,height)/7.5F;
        p.pushPose();p.translate(width*.5F,height*.58F,400);p.scale(scale,-scale,scale);p.mulPose(Axis.XP.rotationDegrees(14));p.mulPose(Axis.YP.rotationDegrees(-62));
        var blocks=mc.getBlockRenderer();
        // The water's surface at the fin's waist: the body below it, the fin above.
        for(int x=-3;x<=3;x++)for(int z=-4;z<=4;z++){p.pushPose();p.translate(x-.5,-.48-.889,z-.5);p.scale(1,.889F,1);blocks.renderSingleBlock(Blocks.BLUE_STAINED_GLASS.defaultBlockState(),p,out,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);p.popPose();}
        var model=new LakeSharkModel(mc.getEntityModels().bakeLayer(LakeSharkModel.LAYER));model.setupAnim(null,0,0,12,0,0);
        p.pushPose();p.mulPose(Axis.YP.rotationDegrees(180));p.scale(-1,-1,1);
        model.renderToBuffer(p,out.getBuffer(RenderType.entityCutoutNoCull(LakeSharkRenderer.TEXTURE)),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,-1);p.popPose();
        out.endBatch();p.popPose();
    }
    private void props(GuiGraphics g){
        var mc=Minecraft.getInstance();var out=mc.renderBuffers().bufferSource();var p=g.pose();float scale=Math.min(width,height)/9F;
        MoviePropBlock.Kind[] kinds={MoviePropBlock.Kind.SPEAKER,MoviePropBlock.Kind.PROJECTOR,MoviePropBlock.Kind.MORTAR};
        for(int i=0;i<kinds.length;i++){
            p.pushPose();p.translate(width*(.25F+.25F*i),height*.86F,400);p.scale(scale,-scale,scale);p.mulPose(Axis.XP.rotationDegrees(24));p.mulPose(Axis.YP.rotationDegrees(150));p.translate(-.5,-.5,-.5);
            mc.getBlockRenderer().renderSingleBlock(MoviePropBlock.of(kinds[i],Direction.NORTH),p,out,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);p.popPose();
        }
        out.endBatch();
    }
    /** The lawn chair (and knocked over), blanket, cooler, popcorn (and spilled), poster, life ring, film cans and buoy, then the six boards. */
    private void things(GuiGraphics g){
        var mc=Minecraft.getInstance();var out=mc.renderBuffers().bufferSource();var p=g.pose();
        var states=new ArrayList<net.minecraft.world.level.block.state.BlockState>();
        for(var k:MoviePropBlock.Kind.values())if(k.ordinal()>MoviePropBlock.Kind.MORTAR.ordinal())states.add(MoviePropBlock.of(k,Direction.NORTH));
        for(var k:MovieMaterialBlock.Kind.values())states.add(MovieMaterialBlock.of(k));
        int columns=6;float cell=Math.min(width/(columns+.5F),(height-30)/3.4F),scale=cell*.52F;
        for(int i=0;i<states.size();i++){
            p.pushPose();p.translate(width*.5F+(i%columns-(columns-1)/2F)*cell,30+cell*(i/columns+.6F),400);p.scale(scale,-scale,scale);p.mulPose(Axis.XP.rotationDegrees(24));p.mulPose(Axis.YP.rotationDegrees(150));p.translate(-.5,-.5,-.5);
            mc.getBlockRenderer().renderSingleBlock(states.get(i),p,out,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);p.popPose();
        }
        out.endBatch();
    }
    /** Each of the five paints on the canoe's own hull, three-quarter view from above. */
    private void canoes(GuiGraphics g){
        var mc=Minecraft.getInstance();var out=mc.renderBuffers().bufferSource();var p=g.pose();
        var model=new LakeHouseCanoeModel<LakeCanoeEntity>(mc.getEntityModels().bakeLayer(LakeHouseCanoeModel.LAYER));float scale=Math.min(width/3.2F,height/3.6F)/2.6F;
        for(int i=0;i<LakeCanoeRenderer.PAINT.length;i++){
            p.pushPose();p.translate(width*(i<3?.2F+.3F*i:.35F+.3F*(i-3)),height*(i<3?.36F:.74F),400);p.scale(scale,-scale,scale);p.mulPose(Axis.XP.rotationDegrees(28));p.mulPose(Axis.YP.rotationDegrees(-30));
            p.scale(-1,-1,1);p.mulPose(Axis.YP.rotationDegrees(90));
            model.renderToBuffer(p,out.getBuffer(RenderType.entityCutoutNoCull(LakeCanoeRenderer.PAINT[i])),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,-1);p.popPose();
        }
        out.endBatch();
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();if(!(mc.screen instanceof MovieNightVisualProof proof)||++proof.frames<8)return;
        var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);
        try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve("native-movie-"+proof.page+".png"));}
        if(++proof.page<VIEWS.length){proof.frames=0;return;}
        if(!proof.problems.isEmpty())throw new IllegalStateException("MOVIE NIGHT VIEW CHECK FAILED: "+String.join("; ",proof.problems));
        Files.writeString(dir.resolve("movie-night-passed.txt"),"Nine native Movie Night views: five frames of Teeth of Summer from the drawn atlas (title, night swim, fin, rise, dawn with THE END?), the shark with its fin clear of the water, the props with the projector's shadows on the film, the audience's things with the lake's boards, and the five painted canoes.\n");
        TheOldestHouse.LOGGER.info("MOVIE NIGHT VIEW CHECK PASSED: nine native views");
        mc.setScreen(new CostumeNightVisualProof());
    }
}
