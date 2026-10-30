package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

/**
 * 0.4.199 (owner: "Can we also give the Goatman curled ram horns?"): a ram's horns, one each side, rising off the crown,
 * sweeping back over the skull, down behind the ear and forward again under it, the tip turned up beside the cheek.
 * Each horn is a chain of seven ridged segments, thickest at the root, each turned a little further round than the one before,
 * the spiral set just outside the side of the head. They ride the head's own pose, so they nod with the
 * laugh and follow its look. Drawn with their own texture (tools/draw_goatman_horns.py), so the Goatman's skin is unchanged.
 */
public final class GoatmanHorns {
    public static final ModelLayerLocation LAYER=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"goatman_horns"),"main");
    public static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/goatman_horns.png");
    /** Each segment: thickness, length, and how far it turns from the last (radians, curling back and down). */
    static final float[][] SEGMENTS={{3,3,.7F},{3,3,-.7F},{2,3,-.7F},{2,3,-.7F},{2,3,-.7F},{2,3,-.7F},{1,3,-.7F}};
    /** Each segment's strip of the 32x32 texture; the left horn mirrors the right's. Matches tools/draw_goatman_horns.py. */
    static final int[][] UV={{0,0},{0,6},{0,12},{0,17},{16,0},{16,5},{16,10}};
    /**
     * Where each horn grows from: the top corner of the skull, a little forward of the middle, in the head's own space. The
     * spiral lies just outside the side of the head and leans out a touch as it comes down, so it never cuts into the skull.
     */
    static final float ROOT_X=4.3F,ROOT_Y=-7,ROOT_Z=-2,TILT=.06F;
    private static ModelPart baked;
    private GoatmanHorns(){}

    public static LayerDefinition layer(){
        MeshDefinition mesh=new MeshDefinition();var root=mesh.getRoot();
        for(boolean left:new boolean[]{false,true}){
            float side=left?1:-1;
            PartDefinition parent=root;String name=left?"left_horn":"right_horn";
            for(int i=0;i<SEGMENTS.length;i++){
                float s=SEGMENTS[i][0],l=SEGMENTS[i][1];
                var box=CubeListBuilder.create().texOffs(UV[i][0],UV[i][1]).mirror(left).addBox(-s/2,-s/2,0,s,s,l);
                PartPose pose=i==0?PartPose.offsetAndRotation(side*ROOT_X,ROOT_Y,ROOT_Z,SEGMENTS[0][2],0,-side*TILT)
                        :PartPose.offsetAndRotation(0,0,SEGMENTS[i-1][1]-.4F,SEGMENTS[i][2],0,0);
                parent=parent.addOrReplaceChild(i==0?name:"segment"+i,box,pose);
            }
        }
        return LayerDefinition.create(mesh,32,32);
    }
    static ModelPart horns(){if(baked==null)baked=Minecraft.getInstance().getEntityModels().bakeLayer(LAYER);return baked;}
    /** The horns on a posed Goatman: through its root and head, then drawn. */
    public static void render(GoatmanFigureModel<?> model,PoseStack pose,VertexConsumer buffer,int light,int overlay){
        pose.pushPose();model.root().translateAndRotate(pose);model.head().translateAndRotate(pose);
        horns().render(pose,buffer,light,overlay);pose.popPose();
    }
    public static void render(GoatmanFigureModel<?> model,PoseStack pose,MultiBufferSource out,int light,int overlay){
        render(model,pose,out.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)),light,overlay);
    }

    /** The horns as a layer on either renderer that draws the Goatman. */
    public static final class Layer<T extends LivingEntity> extends RenderLayer<T,GoatmanFigureModel<T>> {
        public Layer(RenderLayerParent<T,GoatmanFigureModel<T>> parent){super(parent);}
        @Override public void render(PoseStack pose,MultiBufferSource out,int light,T e,float swing,float amount,float partial,float age,float yaw,float pitch){
            if(e.isInvisible())return;
            GoatmanHorns.render(getParentModel(),pose,out,light,LivingEntityRenderer.getOverlayCoords(e,0));
        }
    }
}
