package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LakeWitchEntity;
import io.github.knaitoe.theoldesthouse.labyrinth.Shallows;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class LakeWitchRenderer extends MobRenderer<LakeWitchEntity, LakeWitchModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, "textures/entity/lake_witch.png");
    public static final ResourceLocation MEMORY = ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/lake_witch_memory.png");
    public LakeWitchRenderer(EntityRendererProvider.Context context) { super(context, new LakeWitchModel(context.bakeLayer(LakeWitchModel.LAYER)), .4F); }
    @Override public void render(LakeWitchEntity e,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){
        // Once she is in the trees there is nothing left of her to draw.
        if(e.memory()&&e.memoryPhase()==Shallows.GONE)return;
        super.render(e,yaw,partial,pose,buffers,light);
    }
    @Override protected void setupRotations(LakeWitchEntity e,PoseStack pose,float age,float yaw,float partial,float scale){
        if(!e.memory()){super.setupRotations(e,pose,age,yaw,partial,scale);return;}
        // A recollection is placed and turned by the server every tick; draw it by its own facing.
        super.setupRotations(e,pose,age,Mth.rotLerp(partial,e.yRotO,e.getYRot()),partial,scale);
        memory(pose,e.memoryPhase(),e.swingAt(partial),e.phaseAge(partial),e.getId());
    }
    /**
     * The Shallows body in the frame just after the facing turn: entity origin at the body's centre except while she lies,
     * crawls or rises on her own feet. Her head points back past her own facing, toward the reader at her wrists.
     */
    public static void memory(PoseStack pose,int phase,float swing,float t,int seed){
        switch(phase){
            // On her back the head is twice the body's depth: her back clears the sand by a little so the head does not sink into it.
            case Shallows.LYING->{pose.translate(0,.26,0);lay(pose,90);}
            case Shallows.CARRIED->{pose.mulPose(Axis.ZP.rotation(-swing*.55F));lay(pose,90);}
            case Shallows.FLYING->{pose.mulPose(Axis.ZP.rotationDegrees(t*11));pose.mulPose(Axis.XP.rotationDegrees(90+t*34));pose.translate(0,-.95,0);}
            // Face down with her front on the surface; only a slight list, so no side of her goes under.
            case Shallows.BROKEN->{pose.translate(0,BROKEN_LIFT,0);pose.mulPose(Axis.ZP.rotationDegrees(BROKEN_LIST));lay(pose,-90);}
            case Shallows.RISING->{
                float jerk=(float)Math.sin(t*2.9F+seed)*1.5F;
                if(t<LakeWitchModel.RISE_SPLIT){pose.translate(0,BROKEN_LIFT,0);pose.mulPose(Axis.ZP.rotationDegrees(BROKEN_LIST*(1-t/LakeWitchModel.RISE_SPLIT)+jerk));lay(pose,-90);}
                else{float k=(t-LakeWitchModel.RISE_SPLIT)/(Shallows.RISE_TICKS-LakeWitchModel.RISE_SPLIT);
                    // Pressed flat at first, then up onto her hands in three hitches.
                    float hitch=Math.min(1,(float)Math.floor(k*3+.35F)/3);pose.translate(0,-.42*(1-hitch),0);pose.mulPose(Axis.ZP.rotationDegrees(jerk*2));}
            }
            case Shallows.SCURRY,Shallows.GONE->pose.mulPose(Axis.ZP.rotationDegrees((float)Math.sin(t*.9F)*4));
            default->{}
        }
    }
    private static final float BROKEN_LIFT=.15F,BROKEN_LIST=4;
    /** Turn the standing body about its own centre so it lies along its length: 90 face up, -90 face down. */
    private static void lay(PoseStack pose,float degrees){pose.mulPose(Axis.XP.rotationDegrees(degrees));pose.translate(0,-.95,0);}
    @Override public ResourceLocation getTextureLocation(LakeWitchEntity entity) { return entity.memory() ? MEMORY : TEXTURE; }
}
