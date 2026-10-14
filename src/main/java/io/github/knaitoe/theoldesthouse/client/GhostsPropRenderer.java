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
    private final java.util.WeakHashMap<LiteraryActor,Transition> transitions=new java.util.WeakHashMap<>();
    private static final class Transition{int phase;float start,from,value;Transition(int phase){this.phase=phase;from=value=phase;}}
    public GhostsPropRenderer(EntityRendererProvider.Context c){super(c);}
    private static void block(GhostsSetBlock.Kind k,PoseStack p,MultiBufferSource out,int light){Minecraft.getInstance().getBlockRenderer().renderSingleBlock(GhostsSetBlock.of(k,Direction.NORTH),p,out,light,OverlayTexture.NO_OVERLAY);}
    @Override public void render(LiteraryActor a,float yaw,float partial,PoseStack p,MultiBufferSource out,int light){
        var state=transitions.computeIfAbsent(a,actor->new Transition(actor.phase()));float age=a.tickCount+partial;
        if(state.phase!=a.phase()){state.phase=a.phase();state.from=state.value;state.start=age;}
        float t=net.minecraft.util.Mth.clamp((age-state.start)/8,0,1);t=t*t*(3-2*t);state.value=net.minecraft.util.Mth.lerp(t,state.from,state.phase);
        preview(a.role(),state.value,p,out,light);
    }
    static void preview(int role,float phase,PoseStack p,MultiBufferSource out,int light){
        p.pushPose();p.translate(-.5,0,-.5);
        if(role==LiteraryActor.GHOST_PLAYHOUSE){
            if(phase<1.999F){float fold=net.minecraft.util.Mth.clamp(phase,0,1),collapse=net.minecraft.util.Mth.clamp(phase-1,0,1);p.translate(.1*fold,0,.15*fold);p.mulPose(Axis.ZP.rotationDegrees(12*fold));p.scale(1,Math.max(.05F,1-.3F*fold-.65F*collapse),1);block(GhostsSetBlock.Kind.PLAYHOUSE,p,out,light);}
            else block(GhostsSetBlock.Kind.TORN_CARD,p,out,light);
        }else{
            block(GhostsSetBlock.Kind.COUNTER,p,out,light);
            float extension=phase<=1?-.24F*phase:-.24F-.18F*(phase-1);
            p.pushPose();p.translate(0,.48,extension);block(GhostsSetBlock.Kind.DRAWER,p,out,light);p.popPose();
            if(phase>1){p.translate(0,.48,extension);block(GhostsSetBlock.Kind.DRAWER_RIG,p,out,light);}
        }
        p.popPose();
    }
    @Override public ResourceLocation getTextureLocation(LiteraryActor a){return ResourceLocation.fromNamespaceAndPath("the_oldest_house","textures/block/ghosts/cardboard.png");}
}
