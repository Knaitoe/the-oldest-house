package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** 0.4.173, "Third Floor Girls'": the bathroom's wide glass, and the two kids under their bedsheets. */
public final class DareRenderers {
    private DareRenderers(){}
    public static final ModelLayerLocation SHEET=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"dare_sheet"),"main");
    public static ResourceLocation sheet(int role){return ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/dare_sheet_"+(role==LiteraryActor.DARE_KID_A?"a":"b")+".png");}

    /** The wide glass over the sinks: the lower left block asks for the true reflection, with the dare's changes in it. */
    public static final class Mirror implements BlockEntityRenderer<DareMirrorBlock.Glass> {
        Mirror(BlockEntityRendererProvider.Context c){}
        static HouseReadReflection.Glass glass(DareMirrorBlock.Glass be){
            var facing=be.getBlockState().getValue(DareMirrorBlock.FACING);
            return new HouseReadReflection.Glass(be.getBlockPos(),facing,1/16F,DareMirrorBlock.WIDTH-1/16F,1/16F,DareMirrorBlock.HEIGHT-1/16F,.75F/16,DareReflection.HOOK);
        }
        @Override public void render(DareMirrorBlock.Glass be,float partial,PoseStack pose,MultiBufferSource out,int light,int overlay){
            var s=be.getBlockState();if(!DareMirrorBlock.anchor(s)||s.getValue(DareMirrorBlock.SHATTERED))return;
            var glass=glass(be);HouseReadReflection.want(glass);
            if(HouseReadReflection.showing(glass.at()))HouseReadReflection.drawGlass(glass,pose,out);
        }
        @Override public boolean shouldRenderOffScreen(DareMirrorBlock.Glass be){return DareMirrorBlock.anchor(be.getBlockState());}
    }

    /**
     * A kid under a bedsheet with two eye holes cut in it, sneakers showing at the hem. They fidget while they wait, shake
     * while they giggle, and from the second count stand perfectly still.
     */
    public static final class SheetModel extends EntityModel<LiteraryActor> {
        private final ModelPart root,head,body,hem,leftFoot,rightFoot;
        public SheetModel(ModelPart root){this.root=root;head=root.getChild("head");body=root.getChild("body");hem=root.getChild("hem");leftFoot=root.getChild("left_foot");rightFoot=root.getChild("right_foot");}
        public static LayerDefinition layer(){
            var mesh=new MeshDefinition();var r=mesh.getRoot();
            r.addOrReplaceChild("head",CubeListBuilder.create().texOffs(0,0).addBox(-4.5F,-8,-4.5F,9,8,9),PartPose.offset(0,10,0));
            r.addOrReplaceChild("body",CubeListBuilder.create().texOffs(0,17).addBox(-5.5F,0,-4,11,7,8),PartPose.offset(0,10,0));
            r.addOrReplaceChild("hem",CubeListBuilder.create().texOffs(0,32).addBox(-6.5F,0,-5,13,6,10),PartPose.offset(0,16,0));
            r.addOrReplaceChild("left_foot",CubeListBuilder.create().texOffs(46,0).addBox(-1.25F,0,-2.5F,2.5F,2,4),PartPose.offset(1.8F,22,0));
            r.addOrReplaceChild("right_foot",CubeListBuilder.create().texOffs(46,6).addBox(-1.25F,0,-2.5F,2.5F,2,4),PartPose.offset(-1.8F,22,0));
            return LayerDefinition.create(mesh,64,64);
        }
        @Override public void setupAnim(LiteraryActor a,float walk,float amount,float age,float yaw,float pitch){pose(a.role(),a.phase(),age,yaw,pitch);}
        /** For the actor and for still proofs alike. */
        public void pose(int role,int phase,float age,float yaw,float pitch){
            root.getAllParts().forEach(ModelPart::resetPose);
            float seed=role==LiteraryActor.DARE_KID_A?0:2.3F;
            if(phase==0){float sway=Mth.sin(age*.05F+seed)*.04F;body.zRot=hem.zRot=sway;head.zRot=sway*1.5F;head.y+=Mth.sin(age*.11F+seed)*.15F;}
            else if(phase==1){float shake=Mth.sin(age*2.1F+seed)*.05F;body.zRot=hem.zRot=shake;head.zRot=shake*1.6F;head.y+=Mth.abs(Mth.sin(age*1.4F+seed))*.6F;body.y+=Mth.abs(Mth.sin(age*1.4F+seed))*.3F;}
            head.yRot=Mth.clamp(yaw,-50,50)*Mth.DEG_TO_RAD;head.xRot=phase==2?.12F:Mth.clamp(pitch,-20,25)*Mth.DEG_TO_RAD;
        }
        @Override public void renderToBuffer(PoseStack p,com.mojang.blaze3d.vertex.VertexConsumer b,int light,int overlay,int color){root.render(p,b,light,overlay,color);}
    }
    public static final class Kid extends MobRenderer<LiteraryActor,SheetModel> {
        public Kid(EntityRendererProvider.Context c){super(c,new SheetModel(c.bakeLayer(SHEET)),.3F);}
        @Override public ResourceLocation getTextureLocation(LiteraryActor a){return sheet(a.role());}
        @Override protected boolean shouldShowName(LiteraryActor a){return false;}
    }

    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Registration {
        @SubscribeEvent public static void layers(EntityRenderersEvent.RegisterLayerDefinitions e){e.registerLayerDefinition(SHEET,SheetModel::layer);}
        @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(DareRegistry.MIRROR_ENTITY.get(),Mirror::new);}
    }
}
