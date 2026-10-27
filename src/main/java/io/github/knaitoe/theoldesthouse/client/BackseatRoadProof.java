package io.github.knaitoe.theoldesthouse.client;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import org.joml.Matrix4f;

/** The actual server-exported road, with production car and notebook. Full bright material review only. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class BackseatRoadProof extends Screen {
    private record Voxel(int x,int y,int z,BlockState state){}
    private static final String[] NAMES={"road","approach","notebook"};
    private static final Path FOLDER=Path.of("../build/backseat-road");
    private static boolean launched;
    private final List<Voxel> blocks=new ArrayList<>();
    private int page,frames;
    private BackseatRoadProof()throws Exception{
        super(Component.literal("Backseat native road and notebook"));
        var json=JsonParser.parseString(Files.readString(FOLDER.resolve("road.json"))).getAsJsonObject();
        var palette=new ArrayList<BlockState>();for(var value:json.getAsJsonArray("palette"))palette.add(BlockState.CODEC.parse(JsonOps.INSTANCE,value).getOrThrow());
        for(var row:json.getAsJsonArray("blocks")){var a=row.getAsJsonArray();blocks.add(new Voxel(a.get(0).getAsInt(),a.get(1).getAsInt(),a.get(2).getAsInt(),palette.get(a.get(3).getAsInt())));}
        if(blocks.size()<1000)throw new IllegalStateException("The actual native road export is missing");
    }
    @SubscribeEvent public static void start(ClientTickEvent.Post ignored)throws Exception{
        var mc=Minecraft.getInstance();if(Boolean.getBoolean("the_oldest_house.backseatRoadSmoke")&&!launched&&mc.screen!=null&&mc.getOverlay()==null){launched=true;mc.options.guiScale().set(2);mc.resizeDisplay();mc.setScreen(new BackseatRoadProof());}
    }
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xff10181b);g.flush();
        var projection=new Matrix4f(RenderSystem.getProjectionMatrix());var sort=RenderSystem.getVertexSorting();
        var model=RenderSystem.getModelViewStack();model.pushMatrix();model.identity();RenderSystem.applyModelViewMatrix();
        try{
            RenderSystem.setProjectionMatrix(new Matrix4f().setPerspective((float)Math.toRadians(70),(float)mc.getWindow().getWidth()/mc.getWindow().getHeight(),.04F,160),VertexSorting.DISTANCE_TO_ORIGIN);
            RenderSystem.clear(org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT,Minecraft.ON_OSX);RenderSystem.enableDepthTest();RenderSystem.depthMask(true);
            Vec3 eye=page==0?new Vec3(20,17,6):page==1?new Vec3(-2.3,1.62,-10):new Vec3(-.125,1.87,-15.6875);
            Vec3 target=page==0?new Vec3(0,3,-23):page==1?new Vec3(.2,1.15,-16):new Vec3(1,1.04,-15.5);
            var delta=target.subtract(eye);var p=new PoseStack();p.mulPose(Axis.XP.rotation((float)Math.atan2(-delta.y,delta.horizontalDistance())));p.mulPose(Axis.YP.rotation((float)Math.atan2(delta.x,-delta.z)));p.translate(-eye.x,-eye.y,-eye.z);
            var out=mc.renderBuffers().bufferSource();int light=LightTexture.FULL_BRIGHT,overlay=OverlayTexture.NO_OVERLAY;
            for(var v:blocks){p.pushPose();p.translate(v.x,v.y,v.z);mc.getBlockRenderer().renderSingleBlock(v.state,p,out,light,overlay);p.popPose();}
            p.pushPose();p.translate(.5,0,-15.5);BackseatArt.draw(p,out,light,overlay,1,12);BackseatArt.notebook(p,out,light,overlay);BackseatMirror.body().draw(p,out,light,overlay,0);p.popPose();out.endBatch();
        }finally{model.popMatrix();RenderSystem.applyModelViewMatrix();RenderSystem.setProjectionMatrix(projection,sort);RenderSystem.disableDepthTest();}
        g.drawString(font,"Backseat / native "+NAMES[page],10,10,0xffede1ca,false);g.drawString(font,"Architecture/material review / full bright / actual server export",10,height-19,0xffede1ca,false);
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post ignored)throws Exception{
        var mc=Minecraft.getInstance();if(!(mc.screen instanceof BackseatRoadProof proof)||++proof.frames<20)return;
        Files.createDirectories(FOLDER);
        try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){
            var colors=new HashSet<Integer>();for(int y=image.getHeight()/6;y<image.getHeight()*5/6;y+=3)for(int x=image.getWidth()/6;x<image.getWidth()*5/6;x+=3)colors.add(image.getPixelRGBA(x,y));
            if(colors.size()<20)throw new IllegalStateException("The native road view has no actual material detail: "+NAMES[proof.page]);
            image.writeToFile(FOLDER.resolve("native-"+NAMES[proof.page]+".png"));
        }
        if(++proof.page<NAMES.length){proof.frames=0;return;}
        Files.writeString(FOLDER.resolve("passed.txt"),"Three actual server-exported private-road architecture/material views, with the production open car, floorboard body and passenger notebook. Client night/rain and a complete socket traversal remain separately required.\n");BackseatMirror.clear();mc.stop();
    }
}
