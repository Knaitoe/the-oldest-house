package io.github.knaitoe.theoldesthouse.client;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import org.joml.Matrix4f;

/** Actual native server exports, including the real opened hatch. These are material views, not a socket-playthrough claim. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class SiloArtProof extends Screen {
    private record Voxel(int x,int y,int z,BlockState state){}
    private static final String[] NAMES={"column","grain","kitchen-table","closed-hatch","painted-tin","timber-states"};
    private static final Path FOLDER=Path.of("../build/silo-art");
    private static boolean launched;
    private final List<Voxel> closed,opened;
    private int page,frames;
    private int[] closedRoof;
    private SiloArtProof()throws Exception{super(Component.literal("The Silo"));closed=load("silo");opened=load("roof-open");}
    private static List<Voxel> load(String name)throws Exception{
        var json=JsonParser.parseString(Files.readString(FOLDER.resolve(name+".json"))).getAsJsonObject();var palette=new ArrayList<BlockState>();
        for(var value:json.getAsJsonArray("palette"))palette.add(BlockState.CODEC.parse(JsonOps.INSTANCE,value).getOrThrow());var out=new ArrayList<Voxel>();
        for(var row:json.getAsJsonArray("blocks")){var a=row.getAsJsonArray();out.add(new Voxel(a.get(0).getAsInt(),a.get(1).getAsInt(),a.get(2).getAsInt(),palette.get(a.get(3).getAsInt())));}
        if(out.size()<5000)throw new IllegalStateException("Missing actual silo architecture: "+name);return out;
    }
    @SubscribeEvent public static void start(ClientTickEvent.Post ignored)throws Exception{
        var mc=Minecraft.getInstance();if(Boolean.getBoolean("the_oldest_house.siloSmoke")&&!launched&&mc.screen!=null&&mc.getOverlay()==null){launched=true;mc.options.guiScale().set(2);mc.resizeDisplay();mc.setScreen(new SiloArtProof());}
    }
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xff242a2b);g.flush();if(page==5){timber(g);return;}
        var projection=new Matrix4f(RenderSystem.getProjectionMatrix());var sort=RenderSystem.getVertexSorting();var model=RenderSystem.getModelViewStack();model.pushMatrix();model.identity();RenderSystem.applyModelViewMatrix();
        try{
            RenderSystem.setProjectionMatrix(new Matrix4f().setPerspective((float)Math.toRadians(70),(float)mc.getWindow().getWidth()/mc.getWindow().getHeight(),.04F,180),VertexSorting.DISTANCE_TO_ORIGIN);
            RenderSystem.clear(org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT,Minecraft.ON_OSX);RenderSystem.enableDepthTest();RenderSystem.depthMask(true);
            Vec3 eye=page==0?new Vec3(45,40,52):page==1?new Vec3(3.2,1.62,7):page==2?new Vec3(1.1,32.62,-7.7):new Vec3(.5,62.4,-7.5);
            Vec3 target=page==0?new Vec3(0,32,0):page==1?new Vec3(0,21,-7):page==2?new Vec3(-3,31.3,-4):new Vec3(.5,66,-7.5);
            var delta=target.subtract(eye);var p=new PoseStack();p.mulPose(Axis.XP.rotation((float)Math.atan2(-delta.y,delta.horizontalDistance())));p.mulPose(Axis.YP.rotation((float)Math.atan2(delta.x,-delta.z)));p.translate(-eye.x,-eye.y,-eye.z);
            var out=mc.renderBuffers().bufferSource();
            for(var v:page==4?opened:closed){
                if(page==0&&(v.z>0&&(v.x*v.x+v.z*v.z>=90||v.y>=64)))continue;
                p.pushPose();p.translate(v.x,v.y,v.z);mc.getBlockRenderer().renderSingleBlock(v.state,p,out,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);p.popPose();
            }out.endBatch();
        }finally{model.popMatrix();RenderSystem.applyModelViewMatrix();RenderSystem.setProjectionMatrix(projection,sort);RenderSystem.disableDepthTest();}
        g.drawString(font,"The Silo / "+NAMES[page],10,10,0xffece3ca,false);g.drawString(font,"Native material review / actual server-built blocks / full bright",10,height-18,0xffece3ca,false);
    }
    private void timber(GuiGraphics g){
        var mc=Minecraft.getInstance();var out=mc.renderBuffers().bufferSource();float cw=width/5F,ch=(height-40)/5F;
        for(var kind:SiloTimberBlock.Kind.values())for(int strain=0;strain<5;strain++){
            var p=g.pose();p.pushPose();p.translate((strain+.5F)*cw,30+(kind.ordinal()+.8F)*ch,150);float scale=Math.min(cw*.43F,ch*.58F);p.scale(scale,-scale,scale);p.mulPose(Axis.XP.rotationDegrees(25));p.mulPose(Axis.YP.rotationDegrees(135));p.translate(-.5,-.3,-.5);
            var state=SiloTimberBlock.of(kind,Direction.NORTH).setValue(SiloTimberBlock.STRAIN,strain);mc.getBlockRenderer().renderSingleBlock(state,p,out,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);p.popPose();
        }out.endBatch();g.drawString(font,"Five real timber types / five physical failure states",10,10,0xffece3ca,false);
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post ignored)throws Exception{
        var mc=Minecraft.getInstance();if(!(mc.screen instanceof SiloArtProof proof)||++proof.frames<20)return;Files.createDirectories(FOLDER);
        try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){
            image.writeToFile(FOLDER.resolve("native-"+NAMES[proof.page]+".png"));
            var colors=new HashSet<Integer>();for(int y=image.getHeight()/6;y<image.getHeight()*5/6;y+=3)for(int x=image.getWidth()/6;x<image.getWidth()*5/6;x+=3)colors.add(image.getPixelRGBA(x,y));
            // The authored tiles deliberately have at most seven flat colors. A roof close-up
            // needs the real hatch/sky pixel change, not an arbitrary photorealistic color count.
            if(colors.size()<(proof.page==3||proof.page==4?5:15))throw new IllegalStateException("Blank or untextured native silo view "+NAMES[proof.page]);
            if(proof.page==3){proof.closedRoof=new int[image.getWidth()*image.getHeight()];for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++)proof.closedRoof[y*image.getWidth()+x]=image.getPixelRGBA(x,y);}
            if(proof.page==4){int changed=0;for(int y=image.getHeight()/4;y<image.getHeight()*3/4;y++)for(int x=image.getWidth()/4;x<image.getWidth()*3/4;x++)if(proof.closedRoof[y*image.getWidth()+x]!=image.getPixelRGBA(x,y))changed++;if(changed<500)throw new IllegalStateException("The actual hatch did not reveal painted tin: "+changed);Files.writeString(FOLDER.resolve("roof-pixels.txt"),"Actual closed/open hatch central pixel changes: "+changed+"\n");}
        }
        if(++proof.page<NAMES.length){proof.frames=0;return;}Files.writeString(FOLDER.resolve("passed.txt"),"Six native silo material views from actual server exports, including the real opened hatch and all 25 production timber states. Full-bright review; actual client traversal remains separately required.\n");mc.stop();
    }
}
