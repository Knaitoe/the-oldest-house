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
        if(files.size()!=69)throw new IllegalStateException("Expected sixty-nine generated architecture views, found "+files.size());load();
    }
    private void load()throws Exception{
        var json=JsonParser.parseString(Files.readString(files.get(index))).getAsJsonObject();name=json.get("name").getAsString();
        List<BlockState> palette=new ArrayList<>();for(var e:json.getAsJsonArray("palette"))palette.add(BlockState.CODEC.parse(JsonOps.INSTANCE,e).getOrThrow());
        voxels.clear();int x0=Integer.MAX_VALUE,x1=Integer.MIN_VALUE,z0=Integer.MAX_VALUE,z1=Integer.MIN_VALUE,y0=Integer.MAX_VALUE,y1=Integer.MIN_VALUE;
        for(var e:json.getAsJsonArray("blocks")){var a=e.getAsJsonArray();int x=a.get(0).getAsInt(),y=a.get(1).getAsInt(),z=a.get(2).getAsInt();
            voxels.add(new Voxel(x,y,z,palette.get(a.get(3).getAsInt())));x0=Math.min(x0,x);x1=Math.max(x1,x);z0=Math.min(z0,z);z1=Math.max(z1,z);y0=Math.min(y0,y);y1=Math.max(y1,y);
        }
        cx=(x0+x1+1)/2.;cz=(z0+z1+1)/2.;cy=(y0+y1+1)/2.;span=Math.max((x1-x0+z1-z0+2)*.72,(y1-y0+1)*1.8);frames=0;
    }
    @Override public void renderBackground(GuiGraphics g,int x,int y,float delta){}
    @Override public void render(GuiGraphics g,int x,int y,float delta){
        var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xFF292723);g.drawString(font,"THE OLDEST HOUSE / 0.4.50",16,12,0xFFE0D5BF,false);
        g.drawString(font,name.replace('_',' '),16,29,0xFFF3EEE3,false);g.drawString(font,"Native geometry view / interiors cut away / actors omitted",16,height-19,0xFFD4C8B3,false);g.flush();
        var pose=g.pose();pose.pushPose();pose.translate(width/2.,height/2.+12,200);float scale=(float)Math.min((width-40)/span,(height-85)/(span*.62));
        pose.scale(scale,-scale,scale);pose.mulPose(Axis.XP.rotationDegrees(32));pose.mulPose(Axis.YP.rotationDegrees(-45));pose.translate(-cx,-cy,-cz);
        var buffers=mc.renderBuffers().bufferSource();
        for(var v:voxels){pose.pushPose();pose.translate(v.x,v.y,v.z);
            if(v.state.is(net.minecraft.world.level.block.Blocks.WATER))water(pose,buffers,v.state);else if(v.state.getBlock() instanceof net.minecraft.world.level.block.BedBlock bed){if(v.state.getValue(net.minecraft.world.level.block.BedBlock.PART)==net.minecraft.world.level.block.state.properties.BedPart.FOOT){var entity=(net.minecraft.world.level.block.entity.BedBlockEntity)bed.newBlockEntity(net.minecraft.core.BlockPos.ZERO,v.state);mc.getBlockEntityRenderDispatcher().getRenderer(entity).render(entity,0,pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);}}else mc.getBlockRenderer().renderSingleBlock(v.state,pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();}
        buffers.endBatch();pose.popPose();
    }
    private static void water(com.mojang.blaze3d.vertex.PoseStack pose,MultiBufferSource buffers,BlockState state){
        var sprite=Minecraft.getInstance().getTextureAtlas(net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS).apply(net.minecraft.resources.ResourceLocation.withDefaultNamespace("block/water_still"));
        var out=buffers.getBuffer(RenderType.entityTranslucent(net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS));var matrix=pose.last().pose();float y=state.getFluidState().getOwnHeight();
        float[][] points={{0,y,0,sprite.getU0(),sprite.getV0()},{0,y,1,sprite.getU0(),sprite.getV1()},{1,y,1,sprite.getU1(),sprite.getV1()},{1,y,0,sprite.getU1(),sprite.getV0()}};
        for(var p:points)out.addVertex(matrix,p[0],p[1],p[2]).setColor(63,118,228,185).setUv(p[3],p[4]).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0,1,0);
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();if(!Boolean.getBoolean("the_oldest_house.fontSmoke")||!(mc.screen instanceof SceneVisualProof s)||++s.frames<3)return;
        Path folder=Path.of("../build/font-smoke/scenes");Files.createDirectories(folder);
        try(NativeImage image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(folder.resolve(s.name+".png"));}
        if(++s.index<s.files.size())s.load();else{
            Files.writeString(Path.of("../build/font-smoke/architecture-passed.txt"),"All sixty-nine native generated architecture views rendered successfully.\n");
            TheOldestHouse.LOGGER.info("HOUSE ARCHITECTURE CHECK PASSED: sixty-nine native architecture screenshots saved");mc.setScreen(new OutsideSkyProof());
        }
    }
}
