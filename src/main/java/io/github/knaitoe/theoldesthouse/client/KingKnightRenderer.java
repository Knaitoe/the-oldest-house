package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.KingForest;
import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryActor;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

/**
 * 0.4.647, A sort of thing, part 7 (labyrinth.KingForest, docs/SLEEPING_KING_0_4_647.md): the Knight and his horse.
 *
 * <p>He is a drawing until somebody looks at him: every edge of him and his horse in pencil and nothing filled, as a reader who is a
 * line is drawn (KingClient.Edges). Looked at, he fills in over a third of a second, and thins again as slowly when nobody is. On his
 * horse he sits up with both hands on the reins and rises with its walk; off it he lies on his back in the path beside it, and the horse
 * stands with its head down. The horse is its own model (barrel, neck and head, ears, tail, four legs, saddle and the cake box), drawn
 * from tools/draw_king_horse.py's sheet; its boxes and where each starts on the sheet are the tool's BOXES.
 */
final class KingKnightRenderer {
    static final ModelLayerLocation HORSE=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"king_horse"),"main");
    private static final ResourceLocation HORSE_SHEET=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/literary_king_horse.png");
    /** How fast he fills in or thins, a share of the whole each tick. */
    private static final float FILL_RATE=.16F;
    /** Each Knight's fill as drawn (0 a drawing, 1 filled in), and the age it was last drawn at. */
    private static final Map<Integer,float[]> FILL=new HashMap<>();
    private final LiteraryRenderers.CastModel knight;
    private final ModelPart horse;

    KingKnightRenderer(EntityRendererProvider.Context c){knight=new LiteraryRenderers.CastModel(c.bakeLayer(LiteraryRenderers.CAST));horse=c.bakeLayer(HORSE);}

    static LayerDefinition horseLayer(){
        var mesh=new MeshDefinition();var root=mesh.getRoot();
        // The barrel's back is two pixels up from the model's origin (the rider's seat, CastModel.SADDLE); its legs are twelve long.
        var barrel=root.addOrReplaceChild("barrel",CubeListBuilder.create().texOffs(0,0).addBox(-5,-10,-11,10,10,22).texOffs(26,48).addBox(-5,-11,-4,10,1,8)
            .texOffs(48,32).addBox(5,-9,1,3,4,3),PartPose.offset(0,12,0));
        var neck=barrel.addOrReplaceChild("neck",CubeListBuilder.create().texOffs(16,32).addBox(-2,-9,-3,4,9,6),PartPose.offsetAndRotation(0,-7,-9,.6F,0,0));
        var head=neck.addOrReplaceChild("head",CubeListBuilder.create().texOffs(0,48).addBox(-2.5F,-4,-7,5,5,8),PartPose.offsetAndRotation(0,-8,0,.25F,0,0));
        head.addOrReplaceChild("ear_left",CubeListBuilder.create().texOffs(48,40).addBox(-.5F,-2,-.5F,1,2,1),PartPose.offsetAndRotation(1.5F,-4,0,-.2F,0,.15F));
        head.addOrReplaceChild("ear_right",CubeListBuilder.create().texOffs(48,40).addBox(-.5F,-2,-.5F,1,2,1),PartPose.offsetAndRotation(-1.5F,-4,0,-.2F,0,-.15F));
        barrel.addOrReplaceChild("tail",CubeListBuilder.create().texOffs(36,32).addBox(-1.5F,0,0,3,12,3),PartPose.offsetAndRotation(0,-9,11,.5F,0,0));
        for(var leg:new String[]{"front_left","front_right","back_left","back_right"})
            root.addOrReplaceChild(leg,CubeListBuilder.create().texOffs(0,32).addBox(-2,0,-2,4,12,4),PartPose.offset(leg.endsWith("left")?3:-3,12,leg.startsWith("front")?-8:8));
        return LayerDefinition.create(mesh,64,64);
    }

    /** The horse walking (a slow four-beat, the barrel rising and falling with it), standing, or standing with its head down to graze. */
    private static void poseHorse(ModelPart horse,float age,boolean moving,boolean fallen){
        horse.getAllParts().forEach(ModelPart::resetPose);
        var barrel=horse.getChild("barrel");var neck=barrel.getChild("neck");var head=neck.getChild("head");var tail=barrel.getChild("tail");
        tail.zRot=Mth.sin(age*.09F)*.18F;tail.xRot=.5F+Mth.sin(age*.05F)*.06F;
        if(moving){
            float step=age*.55F,beat=Mth.sin(step*2);
            barrel.y=12-Math.abs(beat)*.5F;
            horse.getChild("front_left").xRot=Mth.sin(step)*.45F;horse.getChild("back_right").xRot=Mth.sin(step)*.45F;
            horse.getChild("front_right").xRot=-Mth.sin(step)*.45F;horse.getChild("back_left").xRot=-Mth.sin(step)*.45F;
            neck.xRot=.6F+Mth.sin(step*2)*.05F;head.xRot=-.05F+Mth.sin(step*2)*.04F;
            for(var leg:new String[]{"front_left","front_right","back_left","back_right"})horse.getChild(leg).y=12-Math.abs(beat)*.5F;
        }else if(fallen){
            // Waiting for its rider: head down in the grass, nosing a little.
            neck.xRot=1.55F;head.xRot=.35F+Mth.sin(age*.07F)*.08F;head.yRot=Mth.sin(age*.03F)*.15F;
        }else{
            neck.xRot=.6F+Mth.sin(age*.04F)*.03F;head.xRot=-.05F;
            head.getChild("ear_left").xRot=-.2F+Mth.sin(age*.11F)*.12F;
        }
    }

    void render(LiteraryActor a,float partial,PoseStack p,MultiBufferSource out,int light){
        int g=a.gesture();boolean filled=(g&KingForest.FILLED)!=0;
        float age=a.tickCount+partial;
        // Filling in and thinning take a few ticks either way.
        var fill=FILL.computeIfAbsent(a.getId(),k->new float[]{filled?1:0,age});
        float dt=Mth.clamp(age-fill[1],0,4);fill[1]=age;fill[0]=Mth.clamp(fill[0]+(filled?1:-1)*FILL_RATE*dt,0,1);
        if(FILL.size()>64)FILL.keySet().removeIf(id->a.level().getEntity(id)==null);

        float body=Mth.rotLerp(partial,a.yBodyRotO,a.yBodyRot),head=Mth.rotLerp(partial,a.yHeadRotO,a.yHeadRot),pitch=Mth.lerp(partial,a.xRotO,a.getXRot());
        knight.young=false;knight.attackTime=0;knight.riding=false;
        // Turned in the saddle, his head goes so far round and no further.
        knight.setupAnim(a,0,0,age,Mth.clamp(Mth.wrapDegrees(head-body),-80,80),pitch);

        p.pushPose();
        p.mulPose(Axis.YP.rotationDegrees(180-body));
        p.scale(-1,-1,1);
        p.translate(0,-1.501,0);
        paint(knight,horse,p,out,light,g,age,fill[0]);
        p.popPose();
    }

    /**
     * The horse and the Knight in the model's own space (the renderer's flip already made): his edges in pencil while he is not wholly
     * filled, and his fill over them as far as he is.
     */
    private static void paint(LiteraryRenderers.CastModel knight,ModelPart horse,PoseStack p,MultiBufferSource out,int light,int g,float age,float shown){
        boolean fallen=(g&KingForest.FALLEN)!=0,moving=(g&KingForest.MOVING)!=0;
        poseHorse(horse,age,moving,fallen);
        var skin=LiteraryRenderers.skin(LiteraryActor.STRANGER,KingForest.KNIGHT);
        if(shown<1){
            var pencil=out.getBuffer(RenderType.lines());
            MultiBufferSource outline=type->new KingClient.Edges(pencil);
            drawBoth(knight,horse,p,outline,skin,light,-1,fallen,false);
        }
        if(shown>0){
            int colour=shown>=1?-1:FastColor.ARGB32.color((int)(shown*255),255,255,255);
            drawBoth(knight,horse,p,out,skin,light,colour,fallen,shown<1);
        }
    }

    /** For the look review and the proof's page: the Knight as the game draws him, at a gesture, an age, a head turn and a fill. */
    public static void drawPage(PoseStack p,MultiBufferSource out,int light,int gesture,float age,float headYaw,float shown){
        var models=net.minecraft.client.Minecraft.getInstance().getEntityModels();
        var knight=new LiteraryRenderers.CastModel(models.bakeLayer(LiteraryRenderers.CAST));knight.young=false;knight.riding=false;
        knight.pose(LiteraryActor.STRANGER,KingForest.KNIGHT,age);knight.knight(gesture,age);
        knight.head.yRot=headYaw*Mth.DEG_TO_RAD;knight.hat.copyFrom(knight.head);
        paint(knight,models.bakeLayer(HORSE),p,out,light,gesture,age,shown);
    }

    private static void drawBoth(LiteraryRenderers.CastModel knight,ModelPart horse,PoseStack p,MultiBufferSource out,ResourceLocation skin,int light,int colour,boolean fallen,boolean faint){
        horse.render(p,out.getBuffer(faint?RenderType.entityTranslucent(HORSE_SHEET):RenderType.entityCutoutNoCull(HORSE_SHEET)),light,OverlayTexture.NO_OVERLAY,colour);
        p.pushPose();
        if(fallen){
            // Off his horse: on his back in the path a little to its near side, face up, head toward its tail.
            p.translate(-1.05,1.375,.45);
            p.mulPose(Axis.XP.rotationDegrees(-90));
        }
        knight.renderToBuffer(p,out.getBuffer(faint?RenderType.entityTranslucent(skin):RenderType.entityCutoutNoCull(skin)),light,OverlayTexture.NO_OVERLAY,colour);
        p.popPose();
    }
}
