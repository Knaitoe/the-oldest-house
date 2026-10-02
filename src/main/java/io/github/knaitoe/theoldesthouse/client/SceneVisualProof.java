package io.github.knaitoe.theoldesthouse.client;

import com.google.gson.*;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.math.Axis;
import com.mojang.serialization.JsonOps;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/** Cutaways of server-generated scenes, rendered with the shipped native block models and textures. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class SceneVisualProof extends Screen {
    private record Voxel(int x,int y,int z,BlockState state){}
    private final List<Path> files;
    private final List<Voxel> voxels=new ArrayList<>();
    private int index,frames;private String name;private double cx,cz,cy,span;
    public SceneVisualProof()throws Exception{
        super(Component.literal("Native scene architecture"));
        try(var stream=Files.list(Path.of("../build/architecture-proof"))){files=stream.filter(p->p.toString().endsWith(".json")).sorted().toList();}
        if(files.size()!=20)throw new IllegalStateException("Expected twenty generated architecture scenes, found "+files.size());load();
    }
    private void load()throws Exception{
        var json=JsonParser.parseString(Files.readString(files.get(index))).getAsJsonObject();name=json.get("name").getAsString();
        List<BlockState> palette=new ArrayList<>();for(var e:json.getAsJsonArray("palette"))palette.add(BlockState.CODEC.parse(JsonOps.INSTANCE,e).getOrThrow());
        voxels.clear();int x0=999,x1=-999,z0=999,z1=-999,y0=999,y1=-999;
        for(var e:json.getAsJsonArray("blocks")){var a=e.getAsJsonArray();int x=a.get(0).getAsInt(),y=a.get(1).getAsInt(),z=a.get(2).getAsInt();
            voxels.add(new Voxel(x,y,z,palette.get(a.get(3).getAsInt())));x0=Math.min(x0,x);x1=Math.max(x1,x);z0=Math.min(z0,z);z1=Math.max(z1,z);y0=Math.min(y0,y);y1=Math.max(y1,y);
        }
        cx=(x0+x1+1)/2.;cz=(z0+z1+1)/2.;cy=(y0+y1+1)/2.;span=Math.max((x1-x0+z1-z0+2)*.72,(y1-y0+1)*1.8);frames=0;
    }
    @Override public void renderBackground(GuiGraphics g,int x,int y,float delta){}
    @Override public void render(GuiGraphics g,int x,int y,float delta){
        var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xFF292723);g.drawString(font,"THE OLDEST HOUSE / 0.4.27",16,12,0xFFE0D5BF,false);
        g.drawString(font,name.replace('_',' '),16,29,0xFFF3EEE3,false);g.drawString(font,"Native generated blocks / roofs and near walls cut away for inspection",16,height-19,0xFFD4C8B3,false);g.flush();
        var pose=g.pose();pose.pushPose();pose.translate(width/2.,height/2.+12,200);float scale=(float)Math.min((width-40)/span,(height-85)/(span*.62));
        pose.scale(scale,-scale,scale);pose.mulPose(Axis.XP.rotationDegrees(32));pose.mulPose(Axis.YP.rotationDegrees(-45));pose.translate(-cx,-cy,-cz);
        var buffers=mc.renderBuffers().bufferSource();
        for(var v:voxels){pose.pushPose();pose.translate(v.x,v.y,v.z);mc.getBlockRenderer().renderSingleBlock(v.state,pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();}
        buffers.endBatch();pose.popPose();
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();if(!Boolean.getBoolean("the_oldest_house.fontSmoke")||!(mc.screen instanceof SceneVisualProof s)||++s.frames<3)return;
        Path folder=Path.of("../build/font-smoke/scenes");Files.createDirectories(folder);
        try(NativeImage image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(folder.resolve(s.name+".png"));}
        if(++s.index<s.files.size())s.load();else{
            Files.writeString(Path.of("../build/font-smoke/architecture-passed.txt"),"All twenty native generated scene cutaways rendered successfully.\n");
            TheOldestHouse.LOGGER.info("HOUSE ARCHITECTURE CHECK PASSED: twenty native scene screenshots saved");mc.stop();
        }
    }
}
