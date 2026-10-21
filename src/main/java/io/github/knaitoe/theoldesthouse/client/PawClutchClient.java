package io.github.knaitoe.theoldesthouse.client;

import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.network.PawClutchPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

/** A brief close focus on a jointed paw: wrist twists, one finger curls, the remaining count stays exact. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class PawClutchClient {
    public static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/paw_clutch.png");
    private static int pending=-1,wait;
    private PawClutchClient(){}
    public static void accept(PawClutchPayload p){if(p.fingers()<0||p.fingers()>2)return;pending=p.fingers();wait=0;}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){var mc=Minecraft.getInstance();if(pending<0)return;
        if(mc.player==null||!mc.player.isAlive()||mc.player.isSpectator()||mc.level==null||!mc.level.dimension().equals(HouseDimensions.INTERIOR)){pending=-1;return;}
        // A level still loading (a reader carried across a door with the paw) is not time the view could have opened.
        if(mc.screen instanceof net.minecraft.client.gui.screens.ReceivingLevelScreen)return;
        // The choice menu's native close packet must finish before the view opens.
        if(++wait>5&&mc.screen==null){int fingers=pending;pending=-1;mc.setScreen(new Focus(fingers));}else if(wait>80)pending=-1;
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){pending=-1;var mc=Minecraft.getInstance();if(mc.screen instanceof Focus)mc.setScreen(null);}
    public static final class Model {
        public final ModelPart root;
        public Model(){var mesh=new MeshDefinition();var r=mesh.getRoot();
            r.addOrReplaceChild("palm",CubeListBuilder.create().texOffs(0,0).addBox(-4,-2,-1.5F,8,7,3),PartPose.ZERO);
            r.addOrReplaceChild("wrist",CubeListBuilder.create().texOffs(24,0).addBox(-2.5F,5,-1,5,5,2),PartPose.ZERO);
            for(int i=0;i<3;i++){var finger=r.addOrReplaceChild("finger"+i,CubeListBuilder.create().texOffs(0,14).addBox(-1,-4,-1,2,4,2),PartPose.offset((i-1)*2.65F,-2,0));
                finger.addOrReplaceChild("tip",CubeListBuilder.create().texOffs(12,14).addBox(-.9F,-3,-.9F,1.8F,3,1.8F),PartPose.offset(0,-4,0));}
            r.addOrReplaceChild("thumb",CubeListBuilder.create().texOffs(24,14).addBox(-1,-4,-1,2,4,2),PartPose.offsetAndRotation(-3.8F,1.3F,-.2F,0,-.35F,-1.15F));
            root=LayerDefinition.create(mesh,64,32).bakeRoot();
        }
        public void curl(int left,float progress){root.getAllParts().forEach(ModelPart::resetPose);float smooth=Mth.clamp(progress,0,1);smooth=smooth*smooth*(3-2*smooth);
            for(int i=0;i<3;i++){float bend=i>left?1:i==left?smooth:0;var f=root.getChild("finger"+i);f.xRot=-bend*1.7F;f.getChild("tip").xRot=-bend*1.9F;}
            // Opposable digit sweeps across the palm rather than rising as a fourth finger.
            var thumb=root.getChild("thumb");thumb.zRot-=smooth*.35F;thumb.yRot-=smooth*.45F;thumb.xRot+=smooth*.25F;
        }
    }
    public static final class Focus extends Screen {
        private final int fingers;private final Model model=new Model();private int age;
        public Focus(int fingers){super(Component.literal("The paw"));this.fingers=fingers;}
        public int age(){return age;}public int fingers(){return fingers;}
        @Override public boolean isPauseScreen(){return false;}
        @Override public void renderBackground(GuiGraphics g,int x,int y,float delta){}
        @Override public void tick(){var mc=Minecraft.getInstance();if(++age>=64||mc.player==null||!mc.player.isAlive()||mc.player.isSpectator()||mc.level==null||!mc.level.dimension().equals(HouseDimensions.INTERIOR))mc.setScreen(null);}
        @Override public void render(GuiGraphics g,int x,int y,float delta){var mc=Minecraft.getInstance();float time=age+delta;
            float fade=Math.min(Mth.clamp(time/8,0,1),Mth.clamp((64-time)/12,0,1));g.fill(0,0,width,height,((int)(fade*170)<<24));g.flush();
            model.curl(fingers,(time-15)/24);var p=g.pose();p.pushPose();p.translate(width*.5,height*.48,180);
            float zoom=Math.min(width,height)*(.43F+.04F*Mth.clamp(time/12,0,1));p.scale(zoom,zoom,zoom);
            p.mulPose(Axis.YP.rotationDegrees(18+Mth.sin(time*.14F)*7));p.mulPose(Axis.ZP.rotationDegrees(Mth.sin(time*.18F)*4));
            var out=mc.renderBuffers().bufferSource();model.root.render(p,out.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)),LightTexture.pack(12,0),OverlayTexture.NO_OVERLAY,0xffffffff);out.endBatch();p.popPose();
        }
    }
}
