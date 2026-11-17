package io.github.knaitoe.theoldesthouse.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 0.4.679, the variety pass (docs/NPC_VARIETY_0_4_679.md). The owner: "can you do a pass over each vignette and work to vary the
 * npcs? Their faces, hairstyles, and even body compositions?" Every painted person has a build (tools/paint_character_skins.py,
 * BUILDS), written by the painter to cast_builds.json against the sheet they are painted on. Whatever renderer draws them, the build
 * is laid on its humanoid model after the pose is set (mixin.CastBuildMixin) and taken off again once they are drawn: the height from
 * the feet, or from the hips when they sit or lie; the torso across and front to back, the arms set out to its new width; the arms'
 * and legs' thickness. The head keeps its own size. The game's own size for a person (a child's, the giant's) is untouched, and so is
 * their hitbox: a build is how they look, not where they stand.
 */
public final class CastBuilds {
    private CastBuilds(){}
    public record Build(float height,float shoulders,float depth,float limbs){}
    private static Map<ResourceLocation,Build> builds;
    /** Nothing to take off again. */
    public static final Object NONE=new Object();

    /** The build of whoever is painted on this sheet, or null for the common build (and for anything that is not the cast's). */
    public static Build of(ResourceLocation texture){
        if(builds==null)builds=load();
        return builds.get(texture);
    }
    private static Map<ResourceLocation,Build> load(){
        var out=new HashMap<ResourceLocation,Build>();
        try(var reader=Minecraft.getInstance().getResourceManager().openAsReader(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"cast_builds.json"))){
            JsonObject root=JsonParser.parseReader(reader).getAsJsonObject();
            for(var e:root.entrySet()){
                var b=e.getValue().getAsJsonObject();
                out.put(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/"+e.getKey()+".png"),
                    new Build(b.get("height").getAsFloat(),b.get("shoulders").getAsFloat(),b.get("depth").getAsFloat(),b.get("limbs").getAsFloat()));
            }
        }catch(Exception missing){TheOldestHouse.LOGGER.warn("No cast builds: {}",missing.toString());}
        return out;
    }

    /** The parts a build changed and what they were, to put back. */
    private record Saved(List<ModelPart> parts,float[] values){}

    /**
     * Lays the build of the sheet {@code texture} on {@code model} (already posed for this frame) and on the pose stack, in model
     * space; returns what to give {@link #restore} once the person and their layers are drawn.
     */
    public static Object apply(Object model,ResourceLocation texture,LivingEntity entity,PoseStack pose){
        return apply(model,texture,entity.getPose()==Pose.SLEEPING,pose);
    }
    /** As {@link #apply(Object,ResourceLocation,LivingEntity,PoseStack)}, for a figure drawn without an entity (a review sheet), lying
     * down or not. */
    public static Object apply(Object model,ResourceLocation texture,boolean lying,PoseStack pose){
        if(!(model instanceof HumanoidModel<?> m)||texture==null)return NONE;
        Build b=of(texture);if(b==null)return NONE;
        var parts=new ArrayList<ModelPart>();
        parts.add(m.head);parts.add(m.hat);parts.add(m.body);parts.add(m.rightArm);parts.add(m.leftArm);parts.add(m.rightLeg);parts.add(m.leftLeg);
        if(m instanceof PlayerModel<?> p){parts.add(p.jacket);parts.add(p.rightSleeve);parts.add(p.leftSleeve);parts.add(p.rightPants);parts.add(p.leftPants);}
        float[] values=new float[parts.size()*6];
        for(int i=0;i<parts.size();i++){var part=parts.get(i);values[i*6]=part.x;values[i*6+1]=part.y;values[i*6+2]=part.z;values[i*6+3]=part.xScale;values[i*6+4]=part.yScale;values[i*6+5]=part.zScale;}
        float wider=b.shoulders()-1;
        for(var torso:List.of(m.body)){torso.xScale*=b.shoulders();torso.zScale*=b.depth();}
        if(m instanceof PlayerModel<?> p){p.jacket.xScale*=b.shoulders();p.jacket.zScale*=b.depth();}
        // The arms hang from the torso's new width, the legs from a hip as much wider as the shoulders are by half.
        armLeg(m.rightArm,-4*wider,b.limbs());armLeg(m.leftArm,4*wider,b.limbs());
        armLeg(m.rightLeg,-2*wider,b.limbs());armLeg(m.leftLeg,2*wider,b.limbs());
        if(m instanceof PlayerModel<?> p){armLeg(p.rightSleeve,-4*wider,b.limbs());armLeg(p.leftSleeve,4*wider,b.limbs());armLeg(p.rightPants,-2*wider,b.limbs());armLeg(p.leftPants,2*wider,b.limbs());}
        if(m instanceof LiteraryRenderers.CastModel cast){var bent=cast.bentArms();parts.add(bent[0]);parts.add(bent[1]);
            values=java.util.Arrays.copyOf(values,parts.size()*6);for(int i=parts.size()-2;i<parts.size();i++){var part=parts.get(i);values[i*6]=part.x;values[i*6+1]=part.y;values[i*6+2]=part.z;values[i*6+3]=part.xScale;values[i*6+4]=part.yScale;values[i*6+5]=part.zScale;}
            armLeg(bent[0],-4*wider,b.limbs());armLeg(bent[1],4*wider,b.limbs());}
        if(b.height()!=1){
            // Model space: y runs down from 1.501 blocks above the feet; the hips are 0.75 above them. The head is drawn at its own
            // size, on the taller or shorter neck.
            boolean low=m.riding||lying||m.rightLeg.xRot<-1F&&m.leftLeg.xRot<-1F;
            float pivot=low?.751F:1.501F;
            pose.translate(0,pivot,0);pose.scale(1,b.height(),1);pose.translate(0,-pivot,0);
            m.head.yScale/=b.height();m.hat.yScale/=b.height();
        }
        return new Saved(parts,values);
    }
    private static void armLeg(ModelPart part,float out,float limbs){part.x+=out;part.xScale*=limbs;part.zScale*=limbs;}

    /** Puts back what {@link #apply} changed. */
    public static void restore(Object saved){
        if(!(saved instanceof Saved s))return;
        for(int i=0;i<s.parts().size();i++){var part=s.parts().get(i);float[] v=s.values();part.x=v[i*6];part.y=v[i*6+1];part.z=v[i*6+2];part.xScale=v[i*6+3];part.yScale=v[i*6+4];part.zScale=v[i*6+5];}
    }
}
