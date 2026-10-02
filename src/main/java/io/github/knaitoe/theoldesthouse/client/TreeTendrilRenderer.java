package io.github.knaitoe.theoldesthouse.client;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.resources.ResourceLocation;
/** Small joint motions on real tree branches; no forced camera or server animation traffic. */
public final class TreeTendrilRenderer implements BlockEntityRenderer<TreeTendrilBlockEntity> {
    public static final ModelLayerLocation LAYER=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"tree_tendril"),"main");
    private static final ResourceLocation BARK=ResourceLocation.withDefaultNamespace("textures/block/oak_log.png");
    private final ModelPart root,tip,left,right;
    public TreeTendrilRenderer(BlockEntityRendererProvider.Context context){this(context.bakeLayer(LAYER));}
    public TreeTendrilRenderer(ModelPart part){root=part;tip=root.getChild("tip");left=tip.getChild("left");right=tip.getChild("right");}
    public static LayerDefinition layer(){
        var mesh=new MeshDefinition();var root=mesh.getRoot();
        root.addOrReplaceChild("stem",CubeListBuilder.create().texOffs(0,0).addBox(-2,-2,-8,4,4,10),PartPose.ZERO);
        var tip=root.addOrReplaceChild("tip",CubeListBuilder.create().texOffs(0,0).addBox(-1.4F,-1.4F,0,2.8F,2.8F,7),PartPose.offset(0,0,2));
        tip.addOrReplaceChild("left",CubeListBuilder.create().texOffs(0,0).addBox(-.8F,-.8F,0,1.6F,1.6F,6),PartPose.offsetAndRotation(-.8F,0,5,-.12F,-.4F,.1F));
        tip.addOrReplaceChild("right",CubeListBuilder.create().texOffs(0,0).addBox(-.6F,-.6F,0,1.2F,1.2F,5),PartPose.offsetAndRotation(.8F,0,5,.2F,.5F,-.1F));
        return LayerDefinition.create(mesh,32,32);
    }
    public void renderAt(float age,int seed,net.minecraft.core.Direction facing,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        root.getAllParts().forEach(ModelPart::resetPose);float pulse=(float)Math.sin(age*.045F+seed*.71F);
        tip.xRot=pulse*.045F;tip.yRot=(float)Math.sin(age*.026F+seed)*.06F;left.yRot-=pulse*.08F;right.xRot+=pulse*.06F;
        pose.pushPose();pose.translate(.5,.5,.5);pose.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        root.render(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(BARK)),light,overlay);pose.popPose();
    }
    @Override public void render(TreeTendrilBlockEntity e,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        float age=e.getLevel()==null?partial:e.getLevel().getGameTime()+partial;renderAt(age,e.getBlockPos().hashCode(),e.getBlockState().getValue(TreeTendrilBlock.FACING),pose,buffers,light,overlay);
    }
}
