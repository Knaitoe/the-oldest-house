package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import org.joml.Matrix4f;

/** Native material/model inspection. This is art coverage, not a playable-scene claim. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class BackseatArtProof extends Screen {
    private static final String[] VIEWS={"front-open","rear-open","side-closed","front-closed","door-half","rear-quarter","driver-forward","driver-mirror","body-floorboard","body-raised","body-half-rise","driver-reveal","mirror-flat","mirror-raised","driver-impact","body-front","body-profile","body-dim"};
    private static boolean launched;
    private static NativeImage flatMirror;
    private int page,frames;
    private BackseatArtProof(){super(Component.literal("Backseat native car materials"));}
    @SubscribeEvent public static void start(ClientTickEvent.Post ignored){
        var mc=Minecraft.getInstance();
        if(Boolean.getBoolean("the_oldest_house.backseatArtSmoke")&&!launched&&mc.screen!=null&&mc.getOverlay()==null){
            launched=true;mc.options.guiScale().set(2);mc.resizeDisplay();mc.setScreen(new BackseatArtProof());
        }
    }
    @Override protected void init(){
        var mc=Minecraft.getInstance();
        for(String name:BackseatArt.NEW_MATERIALS)try(var in=mc.getResourceManager().open(BackseatArt.texture(name));var image=NativeImage.read(in)){
            if(image.getWidth()!=16||image.getHeight()!=16)throw new IllegalStateException("Non-native Backseat material size: "+name);
            var colors=new HashSet<Integer>();
            for(int y=0;y<16;y++)for(int x=0;x<16;x++){int color=image.getPixelRGBA(x,y);colors.add(color);if((color>>>24)!=255)throw new IllegalStateException("Holes in opaque Backseat material: "+name);}
            if(colors.size()>6)throw new IllegalStateException("Backseat palette exceeds the authored flat colors: "+name);
        }catch(java.io.IOException e){throw new IllegalStateException(e);}
        for(String name:new String[]{"wood","glass","lamp","tail"})if(mc.getResourceManager().getResource(BackseatArt.texture(name)).isEmpty())throw new IllegalStateException("Missing established Backseat material: "+name);
        var body=BackseatMirror.body();body.pose(1);var pose=new PoseStack();BackseatStowawayModel.carPose(pose,1);
        body.root().visit(pose,(part,path,index,cube)->{
            for(float x:new float[]{cube.minX,cube.maxX})for(float y:new float[]{cube.minY,cube.maxY})for(float z:new float[]{cube.minZ,cube.maxZ}){
                var at=part.pose().transformPosition(new org.joml.Vector3f(x/16,y/16,z/16));
                if(at.y>35/16F||at.x< -1.19F||at.x>1.19F)throw new IllegalStateException("The raised Backseat creature clips the production cabin at "+path+": "+at);
            }
        });
    }
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xff303838);
        g.drawString(font,"Backseat / "+VIEWS[page],10,10,0xffede1ca,false);
        g.drawString(font,"Native 16px materials beside spruce, iron and black wool",10,height-19,0xffede1ca,false);g.flush();
        BackseatMirror.capture(page==9||page==11||page==13||page==14?1:page==10?.5F:0,LightTexture.FULL_BRIGHT);
        if(page==6||page==7||page==11||page==14){driver(g);return;}
        if(page==12||page==13){
            try(var mirror=BackseatMirror.copy()){
                var tmp=new net.minecraft.client.renderer.texture.DynamicTexture(mirror);var id=mc.getTextureManager().register("backseat_mirror_review",tmp);
                int w=Math.min(width-40,448),h=w*32/112;g.blit(id,(width-w)/2,(height-h)/2,0,0,w,h,w,h);g.flush();mc.getTextureManager().release(id);
            }return;
        }
        var out=mc.renderBuffers().bufferSource();var p=g.pose();p.pushPose();
        float scale=Math.min(width*.10F,height*.14F);
        p.translate(width*.50F,height*.50F,180);p.scale(scale,-scale,scale);
        p.mulPose(Axis.XP.rotationDegrees(24));
        p.mulPose(Axis.YP.rotationDegrees(page==16?90:page>=15?180:page>=8?145:new float[]{142,38,90,142,142,-35}[page]));
        int light=page==17?LightTexture.pack(6,0):LightTexture.FULL_BRIGHT;
        if(page<8)BackseatArt.draw(p,out,light,OverlayTexture.NO_OVERLAY,page<2||page==5?1:page==4?.5F:0,page==4?62:18);
        BackseatMirror.body().draw(p,out,light,OverlayTexture.NO_OVERLAY,page==9||page>=15?1:page==10?.5F:0);
        if(page<8)BackseatMirror.draw(p,out,light,OverlayTexture.NO_OVERLAY);
        out.endBatch();p.popPose();
        for(int i=0;i<3;i++){
            p.pushPose();p.translate(width*(.22F+i*.28F),height*.84F,180);p.scale(scale,-scale,scale);
            p.mulPose(Axis.XP.rotationDegrees(24));p.mulPose(Axis.YP.rotationDegrees(142));
            p.translate(-.5,0,-.5);
            mc.getBlockRenderer().renderSingleBlock((i==0?Blocks.SPRUCE_PLANKS:i==1?Blocks.IRON_BLOCK:Blocks.BLACK_WOOL).defaultBlockState(),p,out,light,OverlayTexture.NO_OVERLAY);p.popPose();
        }
        out.endBatch();
    }
    private void driver(GuiGraphics g){
        var mc=Minecraft.getInstance();var oldProjection=new Matrix4f(RenderSystem.getProjectionMatrix());var oldSort=RenderSystem.getVertexSorting();
        var stack=RenderSystem.getModelViewStack();stack.pushMatrix();stack.identity();RenderSystem.applyModelViewMatrix();
        try{
            RenderSystem.setProjectionMatrix(new Matrix4f().setPerspective((float)Math.toRadians(70),(float)mc.getWindow().getWidth()/mc.getWindow().getHeight(),.04F,32),VertexSorting.DISTANCE_TO_ORIGIN);
            // GUI background depth is in a different projection from the cabin.
            // Retain its color while clearing that depth before the perspective pass.
            RenderSystem.clear(org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT,Minecraft.ON_OSX);
            RenderSystem.enableDepthTest();RenderSystem.depthMask(true);
            var p=new PoseStack();p.mulPose(Axis.YP.rotationDegrees(page==7||page==11?32:0));
            var seat=io.github.knaitoe.theoldesthouse.labyrinth.BackseatCarMesh.DRIVER;p.translate(-seat.x,-seat.y-1.62,-seat.z);
            var out=mc.renderBuffers().bufferSource();BackseatArt.draw(p,out,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0,12,true,page==14?.25F:-1);BackseatMirror.body().draw(p,out,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,page==11||page==14?1:0);BackseatMirror.draw(p,out,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);out.endBatch();
        }finally{stack.popMatrix();RenderSystem.applyModelViewMatrix();RenderSystem.setProjectionMatrix(oldProjection,oldSort);RenderSystem.disableDepthTest();}
        g.drawString(font,"Backseat / "+VIEWS[page]+" / native seated eye",10,10,0xffede1ca,false);
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post ignored)throws Exception{
        var mc=Minecraft.getInstance();if(!(mc.screen instanceof BackseatArtProof proof)||++proof.frames<20)return;
        var dir=Path.of("../build/backseat-art");Files.createDirectories(dir);
        try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){
            image.writeToFile(dir.resolve("native-backseat-"+VIEWS[proof.page]+".png"));
            if(proof.page==6||proof.page==7||proof.page==11||proof.page==14){int seen=0;for(int y=image.getHeight()/6;y<image.getHeight()*5/6;y++)for(int x=image.getWidth()/6;x<image.getWidth()*5/6;x++)if((image.getPixelRGBA(x,y)&0x00ffffff)!=0x00383830)seen++;
                if(seen<1200)throw new IllegalStateException("The native seated view rendered no actual cabin: "+VIEWS[proof.page]+" pixels="+seen);}
        }
        if(proof.page==12||proof.page==13)try(var mirror=BackseatMirror.copy()){
            mirror.writeToFile(dir.resolve("native-backseat-"+VIEWS[proof.page]+"-readback.png"));
            if(proof.page==12){flatMirror=new NativeImage(112,32,false);flatMirror.copyFrom(mirror);}
            else{int changed=0;for(int y=0;y<32;y++)for(int x=0;x<112;x++){int a=mirror.getPixelRGBA(x,y),b=flatMirror.getPixelRGBA(x,y);if(Math.abs((a&255)-(b&255))+Math.abs((a>>8&255)-(b>>8&255))+Math.abs((a>>16&255)-(b>>16&255))>24)changed++;}
                if(changed<80)throw new IllegalStateException("The real rearview contains no rising body: changed="+changed);Files.writeString(dir.resolve("mirror-pixels.txt"),"Native live body pose changed "+changed+" rearview pixels.\n");flatMirror.close();flatMirror=null;}
        }
        if(++proof.page<VIEWS.length){proof.frames=0;return;}
        Files.writeString(dir.resolve("passed.txt"),"Eighteen native production car/body views, including the asymmetric shoulders, bent long neck, narrow low-eyed skull, front/profile/dim views, four seated perspectives, inward windshield fragments and pixel-checked live rearview. The actual raised cuboids clear the cabin ceiling and sides. Native pixel materials beside vanilla spruce, iron and black wool.\n");BackseatMirror.clear();mc.stop();
    }
}
