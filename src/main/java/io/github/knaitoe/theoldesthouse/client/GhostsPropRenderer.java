package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Reader-owned cardboard collapse and opening drawer. Other filming days never alter a peer's props. */
public final class GhostsPropRenderer extends EntityRenderer<LiteraryActor> {
    public static final ResourceLocation PLAYHOUSE=ResourceLocation.fromNamespaceAndPath("the_oldest_house","textures/entity/ghosts_playhouse.png");
    private final java.util.WeakHashMap<LiteraryActor,Transition> transitions=new java.util.WeakHashMap<>();
    /** A phase first seen already reached (a reload, a late arrival) is drawn finished, never replayed. */
    private static final class Transition{int phase;float start=-1e6F,from,value;Transition(int phase){this.phase=phase;from=value=phase;}}
    public GhostsPropRenderer(EntityRendererProvider.Context c){super(c);}
    private static void block(GhostsSetBlock.Kind k,PoseStack p,MultiBufferSource out,int light){Minecraft.getInstance().getBlockRenderer().renderSingleBlock(GhostsSetBlock.of(k,Direction.NORTH),p,out,light,OverlayTexture.NO_OVERLAY);}
    @Override public void render(LiteraryActor a,float yaw,float partial,PoseStack p,MultiBufferSource out,int light){
        var state=transitions.computeIfAbsent(a,actor->new Transition(actor.phase()));float age=a.tickCount+partial;
        if(state.phase!=a.phase()){state.phase=a.phase();state.from=state.value;state.start=age;}
        if(a.role()==LiteraryActor.GHOST_PLAYHOUSE){
            float since=age-state.start;
            house(state.phase>=2?1:Mth.clamp(since/20,0,1)*(state.phase==1?1:0),state.phase>=2?Mth.clamp(since/36,0,1):0,age,p,out,light);return;
        }
        float t=Mth.clamp((age-state.start)/8,0,1);t=t*t*(3-2*t);state.value=Mth.lerp(t,state.from,state.phase);
        preview(a.role(),state.value,p,out,light);
    }
    /** The playhouse at 0 (standing) to 1 (bulging with her inside), then 1 to 2 (torn open and flat); the drawer opening. */
    static void preview(int role,float phase,PoseStack p,MultiBufferSource out,int light){
        if(role==LiteraryActor.GHOST_PLAYHOUSE){house(Mth.clamp(phase,0,1),Mth.clamp(phase-1,0,1),7,p,out,light);return;}
        p.pushPose();p.translate(-.5,0,-.5);
        block(GhostsSetBlock.Kind.COUNTER,p,out,light);
        float extension=phase<=1?-.24F*phase:-.24F-.18F*(phase-1);
        p.pushPose();p.translate(0,.48,extension);block(GhostsSetBlock.Kind.DRAWER,p,out,light);p.popPose();
        if(phase>1){p.translate(0,.48,extension);block(GhostsSetBlock.Kind.DRAWER_RIG,p,out,light);}
        p.popPose();
    }

    // 0.4.172 (owner: "improve Marjorie's crawl into and destruction of the cardboard box"): the playhouse as cardboard panels
    // on hinges, drawn by tools/draw_ghosts_playhouse.py, so it can bulge round her and split open rather than squash.
    // Model space (pixels, y down, drawn flipped like any mob): the doorway faces +z, toward the bedroom door and the camera.
    private static ModelPart west,east,back,front,roofLeft,roofRight;
    private static final float RIDGE=-16,PITCH=.487F;
    private static void parts(){
        if(west!=null)return;
        var mesh=new MeshDefinition();var root=mesh.getRoot();
        root.addOrReplaceChild("west",CubeListBuilder.create().texOffs(0,0).addBox(-1,-12,-8,1,12,16),PartPose.offset(8,0,0));
        root.addOrReplaceChild("east",CubeListBuilder.create().texOffs(0,0).mirror().addBox(0,-12,-8,1,12,16),PartPose.offset(-8,0,0));
        root.addOrReplaceChild("back",CubeListBuilder.create().texOffs(34,0).addBox(-7,-12,0,14,12,1).texOffs(34,13).addBox(-5,-14,0,10,2,1).texOffs(34,16).addBox(-3,-16,0,6,2,1),PartPose.offset(0,0,-8));
        root.addOrReplaceChild("front",CubeListBuilder.create().texOffs(0,28).addBox(-7,-12,-1,4,12,1).texOffs(10,28).addBox(3,-12,-1,4,12,1).texOffs(20,28).addBox(-3,-12,-1,6,3,1)
            .texOffs(34,19).addBox(-5,-14,-1,10,2,1).texOffs(48,16).addBox(-3,-16,-1,6,2,1),PartPose.offset(0,0,8));
        root.addOrReplaceChild("roof_left",CubeListBuilder.create().texOffs(0,41).addBox(0,-1,-9,10,1,18),PartPose.offsetAndRotation(0,RIDGE,0,0,0,PITCH));
        root.addOrReplaceChild("roof_right",CubeListBuilder.create().texOffs(0,41).mirror().addBox(-10,-1,-9,10,1,18),PartPose.offsetAndRotation(0,RIDGE,0,0,0,-PITCH));
        var baked=LayerDefinition.create(mesh,64,64).bakeRoot();
        west=baked.getChild("west");east=baked.getChild("east");back=baked.getChild("back");front=baked.getChild("front");roofLeft=baked.getChild("roof_left");roofRight=baked.getChild("roof_right");
    }
    /** A panel falling open: eased, with a small settle as it lands. */
    private static float fall(float tear,float delay){float x=Mth.clamp((tear-delay)/(1-delay),0,1);float eased=1-(1-x)*(1-x)*(1-x);return eased*1.53F+Mth.sin(x*(float)Math.PI)*.08F;}
    /**
     * {@code bulge} 0..1: Marjorie inside, the walls pushed out in turn and the roof lifting; {@code tear} 0..1: the front
     * gives first, then the sides and the back fall flat outward, and the roof halves are thrown off and land face down.
     */
    static void house(float bulge,float tear,float age,PoseStack p,MultiBufferSource out,int light){
        parts();var buffer=out.getBuffer(RenderType.entityCutoutNoCull(PLAYHOUSE));
        for(var part:new ModelPart[]{west,east,back,front,roofLeft,roofRight})part.resetPose();
        float push=bulge*(1-tear);
        west.zRot=push*(.06F+.05F*Mth.sin(age*.9F))+fall(tear,.08F);west.yRot=tear*.12F;
        east.zRot=-push*(.06F+.05F*Mth.sin(age*1.13F+1.7F))-fall(tear,.14F);east.yRot=-tear*.1F;
        back.xRot=push*(.04F+.04F*Mth.sin(age*.71F+.6F))+fall(tear,.22F);back.yRot=tear*.08F;
        front.xRot=-push*.03F*Mth.sin(age*1.3F+2.2F)-fall(tear,0);front.yRot=-tear*.06F;
        float lift=push*.6F*Mth.abs(Mth.sin(age*.8F));roofLeft.y-=lift;roofRight.y-=lift*.7F;
        if(tear>0){
            float v=Mth.clamp(tear*1.25F,0,1),arc=Mth.sin(v*(float)Math.PI)*10;
            roofLeft.x=Mth.lerp(v,0,14);roofLeft.y=Mth.lerp(v,RIDGE,-1)-arc;roofLeft.z=Mth.lerp(v,0,2);roofLeft.zRot=Mth.lerp(v,PITCH,(float)Math.PI);
            roofRight.x=Mth.lerp(v,0,-14);roofRight.y=Mth.lerp(v,RIDGE,-1)-arc*.8F;roofRight.z=Mth.lerp(v,0,-3);roofRight.zRot=Mth.lerp(v,-PITCH,-(float)Math.PI);roofRight.yRot=v*.3F;
        }
        p.pushPose();
        // Shaken from inside: the whole house jitters while she thrashes.
        if(push>0)p.translate(Mth.sin(age*7.1F)*.012F*push,0,Mth.cos(age*6.3F)*.012F*push);
        p.pushPose();p.scale(-1,-1,1);
        for(var part:new ModelPart[]{west,east,back,front,roofLeft,roofRight})part.render(p,buffer,light,OverlayTexture.NO_OVERLAY);
        p.popPose();
        // Torn scraps on the floor where it stood, once it has come apart.
        if(tear>.5F){p.translate(-.5,0,-.5);block(GhostsSetBlock.Kind.TORN_CARD,p,out,light);}
        p.popPose();
    }
    @Override public ResourceLocation getTextureLocation(LiteraryActor a){return PLAYHOUSE;}
}
