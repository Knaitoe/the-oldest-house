package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.BikeRackBlock;
import io.github.knaitoe.theoldesthouse.labyrinth.BikeRampBlock;
import io.github.knaitoe.theoldesthouse.labyrinth.NightBike;
import io.github.knaitoe.theoldesthouse.labyrinth.NightBikeBlock;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/**
 * 0.4.463, Bike night's bikes as the game draws them (gradle runClient -PbikeNightSmoke): the six on their stands; each as its ridden
 * renderer draws it, upright with the Lamp lit, and leaning with its nose up, wheels and cranks turned; the six lying where they went
 * over; and six in a rack, with the long ramp and the kicker. Every state of every block and every part of every ridden bike must have
 * its model, and every bike must put something on the page.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class BikeNightVisualProof extends Screen {
    static final String[] PAGES={"bikes","ridden","lying","rack","hud"};
    private static final int SKY=0xff6d8496,GROUND=0xff5f513e;
    private int page,frames;private static boolean launched;
    public BikeNightVisualProof(){super(Component.literal("Bike night / the six bikes"));}
    @SubscribeEvent public static void launch(ClientTickEvent.Post ignored){
        var mc=Minecraft.getInstance();
        if(Boolean.getBoolean("the_oldest_house.bikeNightSmoke")&&!launched&&mc.screen!=null&&mc.getOverlay()==null){launched=true;mc.options.guiScale().set(2);mc.resizeDisplay();mc.setScreen(new BikeNightVisualProof());}
    }
    @Override protected void init(){
        var mc=Minecraft.getInstance();var missing=mc.getModelManager().getMissingModel();
        for(var make:NightBike.Make.values()){
            for(var facing:Direction.Plane.HORIZONTAL)for(boolean lying:new boolean[]{false,true})need(NightBikeBlock.of(make,facing,lying));
            var look=BikeLook.of(make);
            for(var part:new net.minecraft.client.resources.model.ModelResourceLocation[]{look.frame(),look.frontWheel(),look.backWheel(),look.cranks(),look.glow()})
                if(part!=null&&mc.getModelManager().getModel(part)==missing)throw new IllegalStateException("Missing ridden bike part "+part);
        }
        for(var facing:Direction.Plane.HORIZONTAL){need(BikeRackBlock.of(facing));for(var part:BikeRampBlock.Part.values())need(BikeRampBlock.of(facing,part));}
    }
    private static void need(BlockState s){var mc=Minecraft.getInstance();if(mc.getBlockRenderer().getBlockModel(s)==mc.getModelManager().getMissingModel())throw new IllegalStateException("Missing Bike night model for "+s);}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}

    @Override public void render(GuiGraphics g,int x,int y,float partial){
        g.fill(0,0,width,height,0xff252925);
        g.drawString(font,"BIKE NIGHT / "+new String[]{"the six, on their stands","ridden: upright (the Lamp lit), and leaning with the nose up","lying where they went over","six in the rack, the long ramp and the kicker","what a rider sees under the crosshair"}[page],8,8,0xffffffff);
        g.fill(0,24,width,height,SKY);
        switch(page){case 0->parked(g,false);case 1->ridden(g);case 2->parked(g,true);case 3->rack(g);default->hud(g);}
    }
    /** The cell a bike is shown in: three across, two down. */
    private float cx(int i){return width*(.19F+.31F*(i%3));}
    private float floor(int i){return 24+(height-24)*(i<3?.47F:.95F);}

    private void parked(GuiGraphics g,boolean lying){
        var mc=Minecraft.getInstance();var buffers=mc.renderBuffers().bufferSource();
        for(int row=0;row<2;row++){int y=(int)floor(row*3);g.fill(0,y,width,y+(lying?2:10),GROUND);}g.flush();
        float s=width*.105F;
        for(var make:NightBike.Make.values()){
            int i=make.ordinal();var pose=g.pose();pose.pushPose();pose.translate(cx(i),floor(i)-(lying?s*.25F:0),200);pose.scale(s,-s,s);
            pose.mulPose(Axis.XP.rotationDegrees(lying?62:12));pose.mulPose(Axis.YP.rotationDegrees(lying?0:-24));pose.translate(-.5F,0,-.5F);
            mc.getBlockRenderer().renderSingleBlock(NightBikeBlock.of(make,Direction.EAST,lying),pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);
            buffers.endBatch();pose.popPose();
            g.drawString(font,make.title(),(int)cx(i)-font.width(make.title())/2,(int)floor(i)-(int)(s*1.42F),0xffffffff);
        }
    }

    private void ridden(GuiGraphics g){
        var mc=Minecraft.getInstance();var buffers=mc.renderBuffers().bufferSource();
        for(int row=0;row<2;row++){int y=(int)floor(row*3);g.fill(0,y,width,y+10,GROUND);}g.flush();
        float s=width*.074F;
        for(var make:NightBike.Make.values()){
            int i=make.ordinal();var look=BikeLook.of(make);
            // Upright, side on. Then leaning (one way and the other by turns), turned a little toward us, its nose up, having rolled.
            float[][] views={{-90,0,0,0,0},{-62,i%2==0?.6F:-.6F,.3F,1.3F,(float)Math.PI/2}};
            for(int v=0;v<2;v++){
                var pose=g.pose();pose.pushPose();pose.translate(cx(i)+(v==0?-s*.95F:s*.95F),floor(i),200);pose.scale(s,-s,s);pose.mulPose(Axis.XP.rotationDegrees(10));
                look.draw(pose,buffers.getBuffer(Sheets.cutoutBlockSheet()),views[v][0],views[v][1],views[v][2],views[v][3],views[v][4],LightTexture.FULL_BRIGHT,v==0);
                buffers.endBatch();pose.popPose();
            }
            g.drawString(font,make.title(),(int)cx(i)-font.width(make.title())/2,(int)floor(i)-(int)(s*2.1F),0xffffffff);
        }
    }

    private void rack(GuiGraphics g){
        var mc=Minecraft.getInstance();var buffers=mc.renderBuffers().bufferSource();g.flush();
        float s=width*.058F;var pose=g.pose();pose.pushPose();pose.translate(width*.5F,24+(height-24)*.62F,300);pose.scale(s,-s,s);
        pose.mulPose(Axis.XP.rotationDegrees(24));pose.mulPose(Axis.YP.rotationDegrees(-32));pose.translate(-6F,0,-1.5F);
        java.util.function.BiConsumer<BlockState,int[]> at=(state,p)->{pose.pushPose();pose.translate(p[0],p[1],p[2]);mc.getBlockRenderer().renderSingleBlock(state,pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();};
        for(int x=-1;x<=12;x++)for(int z=-1;z<=3;z++)at.accept(Blocks.SMOOTH_STONE.defaultBlockState(),new int[]{x,-1,z});
        var makes=NightBike.Make.values();
        for(int i=0;i<makes.length;i++){
            // Each stands behind its place in the rack, its front wheel between the hoops.
            at.accept(BikeRackBlock.of(Direction.NORTH),new int[]{i,0,0});
            at.accept(NightBikeBlock.of(makes[i],Direction.NORTH,false),new int[]{i,0,1});
        }
        at.accept(BikeRampBlock.of(Direction.EAST,BikeRampBlock.Part.LOW),new int[]{8,0,0});
        at.accept(BikeRampBlock.of(Direction.EAST,BikeRampBlock.Part.HIGH),new int[]{9,0,0});
        at.accept(BikeRampBlock.of(Direction.EAST,BikeRampBlock.Part.STEEP),new int[]{9,0,2});
        buffers.endBatch();pose.popPose();
    }

    /** The rider's needle as BikeClient draws it, over a dark road and a bright one: upright; over to one side with the hop half wound; nearly over the other way with the hop wound right up; and the Rattler's card whole, half gone and spent. */
    private void hud(GuiGraphics g){
        g.fill(0,24,width,24+(height-24)/2,0xff1b2026);g.fill(0,24+(height-24)/2,width,height,0xffb9c4cc);
        float[][] shown={{0,0,-1},{.45F,.5F,-1},{-.9F,1,-1},{.1F,0,1},{-.3F,.3F,.5F},{.2F,0,0}};
        String[] names={"upright","leaning, the hop half wound","nearly over, the hop wound right up","the Rattler: the card whole","half of it gone","spent"};
        for(int band=0;band<2;band++)for(int i=0;i<shown.length;i++){
            int cx=(int)(width*(.19F+.31F*(i%3))),y=24+(height-24)/2*band+(int)((height-24)/2*(i<3?.3F:.72F));
            BikeClient.draw(g,cx,y,shown[i][0],shown[i][1],shown[i][2]);
            g.drawString(font,names[i],cx-font.width(names[i])/2,y-20,band==0?0xffd8dde2:0xff20262c,false);
        }
    }

    /** Every bike's cell must have something in it that is not the sky or the ground: its model drew. */
    private void drawn(NativeImage image){
        if(page>2)return;
        for(int i=0;i<6;i++){
            int w=image.getWidth(),h=image.getHeight(),x0=(int)(w*(.04F+.31F*(i%3))),x1=x0+(int)(w*.29F),y0=(int)(h*(i<3?.12F:.56F)),y1=y0+(int)(h*.36F),seen=0;
            for(int y=y0;y<y1;y+=2)for(int x=x0;x<x1;x+=2){int c=image.getPixelRGBA(x,y)&0xffffff;if(c!=(abgr(SKY))&&c!=(abgr(GROUND))&&c!=0xffffff)seen++;}
            if(seen<60)throw new IllegalStateException("Nothing is drawn of "+NightBike.Make.values()[i]+" on page "+PAGES[page]+": "+seen+" pixels");
        }
    }
    private static int abgr(int argb){return (argb&0xff)<<16|(argb&0xff00)|(argb>>16&0xff);}

    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();
        if(!Boolean.getBoolean("the_oldest_house.bikeNightSmoke")||!(mc.screen instanceof BikeNightVisualProof s)||++s.frames<24)return;
        var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);
        try(NativeImage image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve("native-bike-night-"+PAGES[s.page]+".png"));s.drawn(image);}
        if(++s.page<PAGES.length){s.frames=0;return;}
        Files.writeString(dir.resolve("bike-night-assets-passed.txt"),"Five native GPU views of Bike night's bikes: the six on their stands; each as its ridden renderer draws it, upright (the Lamp's lens lit) and leaning with its nose up, wheels and cranks turned; the six lying on their sides; six in a rack, with the long ramp and the kicker; and the rider's needle, the hop winding up and the Rattler's card, over a dark road and a bright one. Every state of the bike, rack and ramp blocks and every part of every ridden bike was required to have its model, and every bike to have drawn something on its page.");
        mc.stop();
    }
}
