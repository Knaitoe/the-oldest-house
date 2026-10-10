package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DoorBlock;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/** Four native GPU views of all cousins, complete strides, connected fear/laughter and the actual fixtures. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class TrailerVisualProof extends Screen {
    private int page,frames;
    public TrailerVisualProof(){super(Component.literal("Trailer update"));}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    private static ResourceLocation skin(int i){return ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/trailer_child_"+i+".png");}
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        var mc=Minecraft.getInstance();var out=mc.renderBuffers().bufferSource();g.fill(0,0,width,height,0xff22262b);
        String[] titles={"All nine cousins","Walking and running: consecutive strides","Seated, cowering and laughing: connected joints","Porcelain fixtures, glazed sash, food and stressed door"};g.drawString(font,titles[page],8,8,0xffe8e0d3,false);g.flush();
        if(page<3){
            int total=page==0?9:8,columns=page==0?3:4,rows=page==0?3:2;
            for(int i=0;i<total;i++){
                var m=new GoatmanChildModel(mc.getEntityModels().bakeLayer(ModelLayers.PLAYER));float phase=(i%4)*((float)Math.PI/2);
                boolean seat=page==2&&i<2,cower=page==2&&i>=2&&i<5,heave=page==2&&i>=5;
                float speed=page==1?(i<4?.075F:.18F):0;
                m.pose(phase,speed,i*11,seat,cower,heave);joints(m);
                float cellWidth=(width-16F)/columns,cellHeight=(height-24F)/rows;
                var p=g.pose();p.pushPose();p.translate(8+(i%columns+.5F)*cellWidth,24+(i/columns+1)*cellHeight-5,150);float scale=Math.min(39,cellHeight/2.1F);p.scale(scale,scale,scale);p.mulPose(Axis.YP.rotationDegrees(i%2==0?160:200));p.translate(0,-1.5,0);
                m.renderToBuffer(p,out.getBuffer(RenderType.entityCutoutNoCull(skin(page==0?i:i%9))),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0xffffffff);p.popPose();
            }out.endBatch();return;
        }
        var states=new net.minecraft.world.level.block.state.BlockState[]{
            GoatmanRegistry.TOILET.get().defaultBlockState(),GoatmanRegistry.SINK.get().defaultBlockState(),GoatmanRegistry.SINK.get().defaultBlockState().setValue(TrailerFixtureBlock.FILLED,true),
            GoatmanRegistry.WINDOW.get().defaultBlockState(),GoatmanRegistry.WINDOW.get().defaultBlockState().setValue(TrailerFixtureBlock.OPEN,true)
        };
        for(int i=0;i<states.length;i++){
            if(mc.getBlockRenderer().getBlockModel(states[i])==mc.getModelManager().getMissingModel())throw new IllegalStateException("Missing trailer fixture "+states[i]);
            var p=g.pose();p.pushPose();p.translate(12+i*(width-32F)/5,height*.48F,150);p.scale(48,-48,48);p.mulPose(Axis.XP.rotationDegrees(20));p.mulPose(Axis.YP.rotationDegrees(140));mc.getBlockRenderer().renderSingleBlock(states[i],p,out,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);p.popPose();
        }
        var door=GoatmanRegistry.DOOR.get().defaultBlockState().setValue(DoorBlock.FACING,Direction.SOUTH);
        for(int i=0;i<2;i++){var p=g.pose();p.pushPose();p.translate(width-108+i*52,height-20,150);p.scale(38,-38,38);p.mulPose(Axis.YP.rotationDegrees(155));TrailerDoorRenderer.draw(door,i==0?0:1.8F,i==0?0:20,p,out,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);p.popPose();}out.endBatch();
        int i=0;for(var item:new ItemStack[]{new ItemStack(GoatmanRegistry.FRANKS.get()),new ItemStack(GoatmanRegistry.RAW_FRANK.get()),new ItemStack(GoatmanRegistry.BRAT.get())}){var p=g.pose();p.pushPose();p.translate(12+i++*58,height-62,0);p.scale(3,3,1);g.renderItem(item,0,0);p.popPose();}
    }
    private static void joints(GoatmanChildModel m){
        var rotation=new Quaternionf().rotationZYX(m.body.zRot,m.body.yRot,m.body.xRot);var hip=rotation.transform(new Vector3f(0,12,0)).add(m.body.x,m.body.y,m.body.z);
        var legs=new Vector3f((m.leftLeg.x+m.rightLeg.x)/2,(m.leftLeg.y+m.rightLeg.y)/2,(m.leftLeg.z+m.rightLeg.z)/2);
        if(hip.distance(legs)>.01||new Vector3f(m.head.x,m.head.y,m.head.z).distance(m.body.x,m.body.y,m.body.z)>.01)throw new IllegalStateException("Detached torso or neck in the actual child model");
        for(var arm:new net.minecraft.client.model.geom.ModelPart[]{m.leftArm,m.rightArm}){
            var shoulder=new Quaternionf(rotation).transform(new Vector3f(arm==m.leftArm?5:-5,2,0)).add(m.body.x,m.body.y,m.body.z);
            if(shoulder.distance(arm.x,arm.y,arm.z)>.01)throw new IllegalStateException("Detached child shoulder");
        }
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();if(!Boolean.getBoolean("the_oldest_house.fontSmoke")||!(mc.screen instanceof TrailerVisualProof proof)||++proof.frames<16)return;
        var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);try(NativeImage im=Screenshot.takeScreenshot(mc.getMainRenderTarget())){im.writeToFile(dir.resolve("native-trailer-0465-"+proof.page+".png"));}
        proof.frames=0;if(++proof.page<4)return;
        Files.writeString(dir.resolve("trailer-0465-passed.txt"),"All nine native cousin atlases; eight walking/running stride poses; connected hips, shoulders and neck in fear/laughter/seating; actual toilet, sink states, sash states, raw/cooked food and door stress rendered in four GPU views.\n");mc.setScreen(new CoffinVisualProof());
    }
}
