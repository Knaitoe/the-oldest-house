package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

/** Reader-owned cardboard collapse and opening drawer. Other filming days never alter a peer's props. */
public final class GhostsPropRenderer extends EntityRenderer<LiteraryActor> {
    public GhostsPropRenderer(EntityRendererProvider.Context c){super(c);}
    private static void block(GhostsSetBlock.Kind k,PoseStack p,MultiBufferSource out,int light){Minecraft.getInstance().getBlockRenderer().renderSingleBlock(GhostsSetBlock.of(k,Direction.NORTH),p,out,light,OverlayTexture.NO_OVERLAY);}
    @Override public void render(LiteraryActor a,float yaw,float partial,PoseStack p,MultiBufferSource out,int light){
        p.pushPose();p.translate(-.5,0,-.5);
        if(a.role()==LiteraryActor.GHOST_PLAYHOUSE){
            if(a.phase()<2){if(a.phase()==1){p.translate(.1,0,.15);p.mulPose(Axis.ZP.rotationDegrees(12));p.scale(1,.7F,1);}block(GhostsSetBlock.Kind.PLAYHOUSE,p,out,light);}
            else block(GhostsSetBlock.Kind.TORN_CARD,p,out,light);
        }else{
            block(GhostsSetBlock.Kind.COUNTER,p,out,light);
            p.pushPose();p.translate(0,.48,a.phase()>=2?-.42:a.phase()==1?-.24:0);block(GhostsSetBlock.Kind.DRAWER,p,out,light);p.popPose();
            if(a.phase()>=2){p.translate(0,.48,-.42);block(GhostsSetBlock.Kind.DRAWER_RIG,p,out,light);}
        }
        p.popPose();
    }
    @Override public ResourceLocation getTextureLocation(LiteraryActor a){return ResourceLocation.fromNamespaceAndPath("the_oldest_house","textures/block/ghosts/cardboard.png");}
}
