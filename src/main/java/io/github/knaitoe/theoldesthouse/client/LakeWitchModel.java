package io.github.knaitoe.theoldesthouse.client;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LakeWitchEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
/** Human proportions and ordinary hands; terror comes from how she moves. */
public final class LakeWitchModel extends PlayerModel<LakeWitchEntity> {
    public static final ModelLayerLocation LAYER=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"lake_witch"),"main");
    public LakeWitchModel(ModelPart root){super(root,true);}
    public static LayerDefinition createBodyLayer(){return LayerDefinition.create(PlayerModel.createMesh(CubeDeformation.NONE,true),64,64);}
    @Override public void setupAnim(LakeWitchEntity e,float walk,float speed,float age,float yaw,float pitch){
        super.setupAnim(e,walk,speed,age,yaw,pitch);head.xRot+=.12F;body.xRot=.06F;
        if(e.memory()){leftArm.xRot=-.2F;rightArm.xRot=-.25F;head.xRot=.25F;}
        else{leftArm.zRot=.07F;rightArm.zRot=-.07F;}
        hat.copyFrom(head);jacket.copyFrom(body);leftSleeve.copyFrom(leftArm);rightSleeve.copyFrom(rightArm);
    }
}
