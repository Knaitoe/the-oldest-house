package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.PuzzleRegistry;
import io.github.knaitoe.theoldesthouse.labyrinth.SphinxDreamEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * 0.4.342, the Sphinx's whole body in the dream (docs/SPHINX_DREAM_0_4_342.md). The owner: "The dream should build the sphinx's whole
 * body as a massive entity"; "It should be so large as to occupy most of the player's screen"; "give it more detail ... rely on multiple
 * blocks for design. You also need to give it actual forearms."
 * <p>
 * She is built of about a hundred and seventy blocks in model pixels, drawn twelve times a mob's scale, each face of her coat taking its
 * own window of the coat's picture at one texel to a model pixel:
 * <ul>
 * <li>each foreleg a shoulder over the joint, an upper arm with the triceps behind it, an elbow standing out at the back, a forearm thick
 * below the elbow and slimmer above the wrist, a wrist with its dewclaw, and a paw: the pad, the heel pad behind, four toes and their
 * claws;</li>
 * <li>her body a breast and keel, a deep chest, the withers, the ribs, the loin and soft belly tucked up behind them, the haunch and rump,
 * and the line of her spine;</li>
 * <li>each hind leg the great thigh, the stifle, the shin with its calf, the hock standing out behind, the long bone down to the foot,
 * and a paw like the front ones; the joints set so that her feet stand on the ground behind the house whatever the ridge's height;</li>
 * <li>a neck under the headcloth's falling back and plait, the two striped lappets down in front of her shoulders, and a broad collar of
 * lapis, gold and carnelian across her breast;</li>
 * <li>her head: the headcloth's crown and flaring sides, and the face of the first crossing, some twenty blocks across, its eyes on the
 * camera, its blink and its mouth;</li>
 * <li>a tail of six blocks narrowing to its tuft;</li>
 * <li>and two wings, each the scapulars at her back, a thick leading edge, rows of coverts with a faded painted band, eight separate
 * secondaries scalloping the trailing edge and nine primaries that fan out as she opens to nearly two hundred blocks.</li>
 * </ul>
 * Perched: her forepaws are the entity's position, on the ridge, the house small under her; her body runs back over it and her hind feet
 * stand in the wheat; her head is up, turned and bowed to the reader. Flying: her body levels, her legs swing back and down with the
 * paws hanging, toes angled down, her wings open over twenty ticks and beat slowly; the server moves her along her flight. Her coat is
 * the worn umber of her face's stone.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class SphinxDreamRenderer extends EntityRenderer<SphinxDreamEntity> {
    public static final ResourceLocation BODY=SphinxFaceRenderer.tex("body");
    /** Model pixels to blocks: twelve times a mob's sixteen to the block. */
    public static final float SCALE=12F/16F;

    /**
     * A region of body.png (tools/draw_sphinx_dream.py REGIONS), in texels. {@code fit} stretches it whole over a face; otherwise a face
     * takes a window of it one texel to a model pixel, placed by the block's own position, so every block of her coat has the same grain
     * and no two the same patch.
     */
    record R(int u,int v,int w,int h,boolean fit){
        static R of(int u,int v,int w,int h){return new R(u,v,w,h,false);}
        static R fit(int u,int v,int w,int h){return new R(u,v,w,h,true);}
        float[] window(float du,float dv,int seed){
            if(fit)return new float[]{u/256F,v/256F,(u+w)/256F,(v+h)/256F};
            int ww=Mth.clamp(Math.round(Math.abs(du)),1,w),hh=Mth.clamp(Math.round(Math.abs(dv)),1,h),s=seed&0x7fffffff;
            int ou=s%(w-ww+1),ov=(s/7919)%(h-hh+1);
            return new float[]{(u+ou)/256F,(v+ov)/256F,(u+ou+ww)/256F,(v+ov+hh)/256F};
        }
    }
    static final R FUR=R.of(0,0,64,64),BACK=R.of(64,0,64,64),BELLY=R.of(128,0,64,64),NEMES=R.of(128,128,64,64),COVERT=R.of(32,192,64,32),
        SHADE=R.of(96,192,64,64),UNDERWING=R.of(128,64,128,64),TUFT=R.fit(192,128,32,32),DARK=R.fit(224,128,32,32),FEATHER=R.fit(0,192,16,64),
        FEATHER_UNDER=R.fit(16,192,16,64),BAND=R.fit(32,224,64,16),COLLAR=R.fit(32,240,64,16),CLAW=R.fit(192,160,16,16),PAD=R.fit(208,160,16,16),
        FEATHER_DOWN=R.fit(160,192,16,64),FEATHER_UNDER_UP=R.fit(176,192,16,64);
    /** What one block is covered with: its sides and back, top, bottom and front. */
    record Skin(R side,R top,R bottom,R front){
        static Skin of(R side,R top,R bottom){return new Skin(side,top,bottom,side);}
        static Skin all(R r){return new Skin(r,r,r,r);}
    }
    static final Skin COAT=Skin.of(FUR,BACK,BELLY),LIMB=Skin.of(FUR,FUR,SHADE),INNER=Skin.of(SHADE,FUR,SHADE),FOOT=Skin.of(FUR,FUR,PAD),
        HORN=Skin.all(CLAW),CLOTH=Skin.of(NEMES,NEMES,DARK),HEAD=new Skin(NEMES,NEMES,DARK,DARK),BEADS=Skin.all(COLLAR),TIP=Skin.all(TUFT),
        COVERTS=Skin.of(COVERT,COVERT,UNDERWING),PAINTED=new Skin(BAND,BAND,UNDERWING,BAND),QUILL=new Skin(FEATHER,FEATHER,FEATHER_UNDER,FEATHER),
        // The folded inner wing faces her flank with its upper side, so its outer face is drawn with the feathers' tops.
        COVERTS_OUT=new Skin(COVERT,UNDERWING,COVERT,COVERT),PAINTED_OUT=new Skin(BAND,UNDERWING,BAND,BAND),QUILL_OUT=new Skin(FEATHER,FEATHER_UNDER_UP,FEATHER_DOWN,FEATHER);
    /** The face picture's size on her head, in model pixels, and its centre. */
    static final float FACE_W=30,FACE_H=25,FACE_Y=4,FACE_Z=10.3F;
    /** The foreleg: its shoulder joint over the paws, and the bones below it. */
    static final float SHOULDER_Y=30.4F,SHOULDER_Z=4.5F,HUMERUS=13,FOREARM=15,PAW_H=4;
    /** The hind leg, from the hip in her body: its bones, and where the hip sits behind the shoulder. */
    static final float FEMUR=15,TIBIA=16,META=12,HIP_Y=-2,HIP_Z=-54,STANCE=38;
    /** Each wing's two segments along the span, and how far each swings as she beats. */
    static final float INNER_SPAN=42,OUTER_SPAN=46,BEAT=24,LAG=16;

    public SphinxDreamRenderer(EntityRendererProvider.Context ctx){super(ctx);shadowRadius=0;}
    @SubscribeEvent public static void register(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(PuzzleRegistry.SPHINX_DREAM.get(),SphinxDreamRenderer::new);}
    @Override public ResourceLocation getTextureLocation(SphinxDreamEntity e){return BODY;}
    @Override public boolean shouldRender(SphinxDreamEntity e,net.minecraft.client.renderer.culling.Frustum f,double x,double y,double z){return true;}

    @Override public void render(SphinxDreamEntity e,float yaw,float partial,PoseStack pose,MultiBufferSource out,int light){
        float now=e.tickCount+partial;
        float fly=e.phase()==SphinxDreamEntity.FLYING?Mth.clamp((e.tickCount-e.phaseSeenAt+partial)/20F,0,1):0;
        fly=fly*fly*(3-2*fly);
        // A camera inside the world's light, never black: she takes the morning's light.
        light=Math.max(light,net.minecraft.client.renderer.LightTexture.pack(12,15));
        drawAt(pose,out,light,now,fly,e.perch(),Mth.rotLerp(partial,e.yRotO,e.getYRot()),Mth.lerp(partial,e.xRotO,e.getXRot()));
        super.render(e,yaw,partial,pose,out,light);
    }
    /** Her whole body at her entity's position (the pose's origin), turned to {@code yaw} and, in flight, pitched by {@code pitch}. */
    public static void drawAt(PoseStack pose,MultiBufferSource out,int light,float now,float fly,float perch,float yaw,float pitch){
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(-yaw));
        pose.mulPose(Axis.XP.rotationDegrees(pitch*fly));
        pose.scale(SCALE,SCALE,SCALE);
        draw(pose,out,light,now,fly,perch,true);
        pose.popPose();
    }

    /**
     * Draws her whole body in model pixels, her forepaws at the origin, facing +z: {@code fly} 0 perched to 1 in flight, {@code perch}
     * the height of the ridge under her paws in blocks, {@code look} turning her head toward the camera.
     */
    public static void draw(PoseStack pose,MultiBufferSource out,int light,float now,float fly,float perch,boolean look){
        var b=out.getBuffer(RenderType.entityCutoutNoCull(BODY));
        float breathe=Mth.sin(now*.06F)*(1-fly);
        // How her body slopes back from the shoulders: enough that her hind legs stand easily on the ground behind the house.
        float perchPx=perch/SCALE;
        float slope=(float)Math.toDegrees(Math.asin(Mth.clamp((perchPx+SHOULDER_Y+HIP_Y-STANCE)/-HIP_Z,-.14F,.5F)))*(1-fly);
        for(int side=-1;side<=1;side+=2)foreleg(pose,b,side,fly,light);
        // The collar, neck and head, from the shoulders. Her face is drawn with its own pictures, which ends the body's batch.
        pose.pushPose();pose.translate(0,SHOULDER_Y,SHOULDER_Z);
        collar(pose,b,light);
        neck(pose,out,b,light,now,fly,breathe,look);
        pose.popPose();
        b=out.getBuffer(RenderType.entityCutoutNoCull(BODY));
        // The body, sloping back from the shoulders, with the hind legs, tail and wings.
        pose.pushPose();pose.translate(0,SHOULDER_Y,SHOULDER_Z);pose.mulPose(Axis.XP.rotationDegrees(-slope));
        pose.pushPose();pose.scale(1,1+breathe*.015F,1+breathe*.006F);torso(pose,b,light);pose.popPose();
        double s=Math.toRadians(slope);
        float hip=(float)(perchPx+SHOULDER_Y+HIP_Y*Math.cos(s)+HIP_Z*Math.sin(s));
        for(int side=-1;side<=1;side+=2)hindleg(pose,b,side,fly,slope,hip,light);
        tail(pose,b,light,now,fly);
        for(int side=-1;side<=1;side+=2)wing(pose,b,side,light,now,fly);
        pose.popPose();
    }

    // ------------------------------------------------------------------ the forelegs
    /** One foreleg, from the shoulder down: perched, the paw flat on the ridge under the shoulder; in flight, folded back along her belly. */
    static void foreleg(PoseStack pose,VertexConsumer b,int side,float fly,int light){
        // Each bone's angle from straight down, toward her back. In flight the foreleg swings back and down, and the paw hangs from the
        // wrist with its toes angled down.
        float upper=28+22*fly,lower=-6+76*fly,paw=100*fly;
        pose.pushPose();pose.translate(side*9.5F,SHOULDER_Y,SHOULDER_Z);
        // The shoulder: the great muscle over the joint, standing out from her chest.
        box(pose,b,-5.5F,-6,-7,5.5F,8,7,LIMB,light);
        box(pose,b,-4.5F,6,-6,4.5F,10,4,LIMB,light);
        box(pose,b,-3,9,-8,3,12,-1,LIMB,light);           // the blade of the shoulder, up toward the withers
        pose.mulPose(Axis.XP.rotationDegrees(upper));
        // The upper arm, down and back to the elbow, the triceps heavy behind it.
        box(pose,b,-4.5F,-HUMERUS-1,-4.5F,4.5F,1,5.5F,LIMB,light);
        box(pose,b,-4,-HUMERUS+2,-6.5F,4,-2,-4.5F,LIMB,light);
        pose.translate(0,-HUMERUS,0);
        // The elbow, standing out behind the joint.
        box(pose,b,-3.5F,-3,-6.5F,3.5F,2.5F,-3,INNER,light);
        box(pose,b,-2.4F,-4.2F,-7.6F,2.4F,-.6F,-6,LIMB,light); // the tuft of coarse hair at the elbow
        pose.mulPose(Axis.XP.rotationDegrees(lower-upper));
        // The forearm: thick below the elbow, slimmer above the wrist, the long muscle down its front.
        box(pose,b,-4,-7.5F,-4,4,1,4.5F,LIMB,light);
        box(pose,b,-3.3F,-FOREARM+1,-3.3F,3.3F,-7,3.8F,LIMB,light);
        box(pose,b,-2.4F,-10,3.5F,2.4F,-1,4.8F,LIMB,light);
        pose.translate(0,-FOREARM,0);
        // The wrist, and the dewclaw on its inside.
        box(pose,b,-3.7F,-1.5F,-3.6F,3.7F,1.2F,4,LIMB,light);
        float xi=-side*4.2F;
        box(pose,b,xi-.8F,-1.2F,.5F,xi+.8F,.6F,2.6F,LIMB,light);
        box(pose,b,xi-.35F,-1.9F,1.6F,xi+.35F,-1,3.1F,HORN,light);
        pose.mulPose(Axis.XP.rotationDegrees(paw-lower));
        paw(pose,b,1F,light);
        pose.popPose();
    }
    /** A paw under the wrist joint (its sole {@link #PAW_H} below): the pad, the heel pad behind, four toes, their claws. */
    static void paw(PoseStack pose,VertexConsumer b,float size,int light){
        pose.pushPose();pose.scale(size,size,size);
        box(pose,b,-4.6F,-PAW_H,-3,4.6F,-1,5,FOOT,light);
        box(pose,b,-3,-PAW_H,-4,3,-2.4F,-1.5F,FOOT,light);
        float[] xs={-3.45F,-1.15F,1.15F,3.45F},reach={7.8F,8.8F,8.8F,7.8F};
        for(int t=0;t<4;t++){
            float x=xs[t],r=reach[t];
            box(pose,b,x-1.1F,-PAW_H,4.5F,x+1.1F,-1.3F,r,FOOT,light);
            box(pose,b,x-.8F,-1.6F,5,x+.8F,-.9F,r-1.2F,LIMB,light);
            box(pose,b,x-.5F,-3.4F,r,x+.5F,-2,r+1.4F,HORN,light);
            box(pose,b,x-.38F,-PAW_H,r+.9F,x+.38F,-3.1F,r+1.8F,HORN,light);
        }
        pose.popPose();
    }

    // ------------------------------------------------------------------ the body
    /** Her body from the shoulders back, in the sloping frame of her spine: the shoulder joint at the origin. */
    static void torso(PoseStack pose,VertexConsumer b,int light){
        box(pose,b,-9,-17,-2,9,6,9.5F,COAT,light);          // the breast, deep in front of the forelegs
        box(pose,b,-11.5F,-14,-11,11.5F,11.5F,6,COAT,light);// the chest
        box(pose,b,-7,9,-20,7,15,3,COAT,light);             // the withers over the shoulders
        box(pose,b,-12.5F,-13,-31,12.5F,12,-9,COAT,light);  // the ribs
        box(pose,b,-9.5F,-16,-27,9.5F,-12,-8,COAT,light);   // the keel under them
        box(pose,b,-13,-7,-26,13,7,-14,COAT,light);         // the widest of the ribs
        box(pose,b,-10.5F,-6,-47,10.5F,11.5F,-29,COAT,light);// the loin
        box(pose,b,-8.5F,-9.5F,-45,8.5F,-5,-28,COAT,light); // the soft belly tucked up behind the ribs
        box(pose,b,-12.5F,-9,-65,12.5F,13,-45,COAT,light);  // the haunch
        box(pose,b,-9,12,-63,9,15.5F,-46,COAT,light);       // the rump
        box(pose,b,-10,-6,-67,10,10,-63,COAT,light);        // the rump's back, round to the tail
        box(pose,b,-2,11.5F,-46,2,13,-18,COAT,light);       // the line of her spine
        for(int k=0;k<3;k++){float z=-12-k*4.6F;box(pose,b,-13.6F,-9+k,z-1.6F,13.6F,5-k,z,COAT,light);}  // the ribs showing along her flanks
        for(int side=-1;side<=1;side+=2){float x=side*9.5F;box(pose,b,x-2.6F,12,-52,x+2.6F,15,-47,COAT,light);}  // the points of her hips
        box(pose,b,-11,-7,-60,11,9,-52,COAT,light);         // the muscle of the haunch, round toward the thighs
    }
    /** The broad collar across her breast where the neck comes down into it: rows of lapis, gold and carnelian beads. */
    static void collar(PoseStack pose,VertexConsumer b,int light){
        for(int k=0;k<3;k++){float y=8-k*3.4F,w=8.4F+k*1.7F,z=9.6F+k*.9F;box(pose,b,-w,y-3.4F,z-6,w,y,z+1.2F,BEADS,light);}
        box(pose,b,-10.5F,4.6F,-4,10.5F,8,6,BEADS,light);
    }
    /** The neck, under the headcloth falling behind it, the lappets down in front of her shoulders; then her head. */
    static void neck(PoseStack pose,MultiBufferSource out,VertexConsumer b,int light,float now,float fly,float breathe,boolean look){
        pose.pushPose();pose.translate(0,8,3);pose.mulPose(Axis.XP.rotationDegrees(12));
        box(pose,b,-8.5F,-4,-7,8.5F,12,8,COAT,light);
        box(pose,b,-7,10,-6,7,19+breathe*.5F,7,COAT,light);
        // The headcloth falls behind the head over the nape and down the neck in a plait; the lappets hang in front of the shoulders.
        box(pose,b,-11.5F,6,-9.5F,11.5F,20,-6,CLOTH,light);
        box(pose,b,-10,1,-9,10,6,-6.5F,CLOTH,light);
        box(pose,b,-2.2F,-14,-9.6F,2.2F,6,-6.6F,CLOTH,light);
        box(pose,b,-1.6F,-17,-9.2F,1.6F,-13,-7,CLOTH,light);
        for(int side=-1;side<=1;side+=2){
            float x0=side>0?7.5F:-12.5F,x1=side>0?12.5F:-7.5F;
            box(pose,b,x0,-6,5,x1,20,8.5F,CLOTH,light);
            box(pose,b,x0+.4F,-13,5.5F,x1-.4F,-5,8.6F,CLOTH,light);
        }
        pose.translate(0,18,0);pose.mulPose(Axis.XP.rotationDegrees(-12));
        // Perched, her head turns to the reader and bows to look down at them; in flight it looks ahead.
        float headPitch=25*(1-fly)+6*fly,headYaw=0;
        if(look){var cam=camera(pose);if(cam!=null){headYaw=Mth.clamp((float)Math.toDegrees(Math.atan2(cam.x,cam.z)),-35,35)*(1-fly);
            float down=(float)Math.toDegrees(Math.atan2(-cam.y,Math.hypot(cam.x,cam.z)));headPitch=Mth.clamp(down,10,70)*(1-fly)+6*fly;}}
        pose.mulPose(Axis.YP.rotationDegrees(headYaw));pose.mulPose(Axis.XP.rotationDegrees(headPitch));
        // Her head: the headcloth's crown drawn tight over it, its sides flaring out behind her face.
        box(pose,b,-11,-6,-8,11,14,10,HEAD,light);
        box(pose,b,-11.6F,13,-8.6F,11.6F,15.6F,9.4F,CLOTH,light);
        for(int side=-1;side<=1;side+=2){float x0=side>0?11:-13.6F,x1=side>0?13.6F:-11;box(pose,b,x0,-8,-7,x1,12.5F,8.6F,CLOTH,light);box(pose,b,x0,-12,-5,x1,-8,6,CLOTH,light);}
        face(pose,out,light,now);
        pose.popPose();
    }

    // ------------------------------------------------------------------ the hind legs and tail
    /**
     * One hind leg, from the hip, in a frame upright again whatever her body's slope: perched, its joints set so that the foot stands on
     * the ground {@code hip} model pixels below the hip; in flight, trailing behind her.
     */
    static void hindleg(PoseStack pose,VertexConsumer b,int side,float fly,float slope,float hip,int light){
        float th=stance(hip);
        // Each bone's angle from straight down, toward her back.
        // In flight the hind leg trails back and down from the hip, the foot hanging with its toes angled down.
        float thigh=Mth.lerp(fly,-th,38),shin=Mth.lerp(fly,th+15,62),foot=Mth.lerp(fly,-8,78),sole=Mth.lerp(fly,0,102);
        pose.pushPose();pose.translate(side*10.5F,HIP_Y,HIP_Z);pose.mulPose(Axis.XP.rotationDegrees(slope));
        pose.mulPose(Axis.XP.rotationDegrees(thigh));
        // The thigh: the great muscle of her haunch, and the bone down to the stifle.
        box(pose,b,-5.5F,-9,-8,5.5F,6,8,LIMB,light);
        box(pose,b,-4.8F,-12,-6,4.8F,-8,6.5F,LIMB,light);
        box(pose,b,-3.8F,-FEMUR,-4,3.8F,-11,4.5F,LIMB,light);
        pose.translate(0,-FEMUR,0);
        box(pose,b,-3.2F,-2.5F,2,3.2F,2,5,INNER,light);     // the stifle
        pose.mulPose(Axis.XP.rotationDegrees(shin-thigh));
        // The shin, the calf heavy behind it, down and back to the hock.
        box(pose,b,-3.6F,-7.5F,-4.5F,3.6F,1,3.5F,LIMB,light);
        box(pose,b,-2.8F,-TIBIA+1,-3,2.8F,-7,2.8F,LIMB,light);
        pose.translate(0,-TIBIA,0);
        box(pose,b,-2.9F,-1.8F,-4.8F,2.9F,1.6F,2.6F,INNER,light); // the hock, its point behind
        pose.mulPose(Axis.XP.rotationDegrees(foot-shin));
        // The long bone down to the foot.
        box(pose,b,-2.6F,-META+.5F,-2.6F,2.6F,0,2.8F,LIMB,light);
        pose.translate(0,-META,0);
        pose.mulPose(Axis.XP.rotationDegrees(sole-foot));
        box(pose,b,-3.1F,-1,-3,3.1F,1,3.6F,LIMB,light);
        paw(pose,b,.92F,light);
        pose.popPose();
    }
    /** The thigh's forward angle at which the hind leg reaches {@code hip} model pixels down to the ground. */
    static float stance(float hip){
        float lo=5,hi=75;
        for(int i=0;i<14;i++){float mid=(lo+hi)/2;if(legHeight(mid)>hip)lo=mid;else hi=mid;}
        return (lo+hi)/2;
    }
    static float legHeight(float th){
        double r=Math.toRadians(th);
        return (float)(FEMUR*Math.cos(r)+TIBIA*Math.cos(r+Math.toRadians(15))+META*Math.cos(Math.toRadians(8))+PAW_H*.92F);
    }
    /** The tail, drooping off the haunch and curling back over the field while she waits, streaming behind her when she flies. */
    static void tail(PoseStack pose,VertexConsumer b,int light,float now,float fly){
        pose.pushPose();pose.translate(0,8,-66);float sway=Mth.sin(now*.05F)*(6+8*fly);
        pose.mulPose(Axis.XP.rotationDegrees(-38*(1-fly)-8*fly));pose.mulPose(Axis.YP.rotationDegrees(sway));
        float[] thick={2.7F,2.5F,2.3F,2.1F,1.95F,1.8F};
        for(int i=0;i<thick.length;i++){
            float t=thick[i];box(pose,b,-t,-t,-9.4F,t,t,.4F,COAT,light);
            pose.translate(0,0,-9);pose.mulPose(Axis.XP.rotationDegrees((i<2?-4:9)*(1-fly)+fly));pose.mulPose(Axis.YP.rotationDegrees(sway*.25F));
        }
        box(pose,b,-3.2F,-3.2F,-7,3.2F,3.2F,.5F,TIP,light);
        box(pose,b,-1.2F,-1.2F,-9.5F,1.2F,1.2F,-6,TIP,light);
        box(pose,b,-2.6F,1,-8.5F,-.6F,3.4F,-5.5F,TIP,light);
        box(pose,b,.6F,-3.4F,-8.5F,2.6F,-1,-5.5F,TIP,light);
        pose.popPose();
    }

    // ------------------------------------------------------------------ the wings
    /** One wing, from the top of her back behind the shoulders: folded flat along her flank, or open and beating. */
    static void wing(PoseStack pose,VertexConsumer b,int side,int light,float now,float fly){
        float beat=Mth.sin(now*(float)(Math.PI*2/36))*BEAT*fly,lag=Mth.sin(now*(float)(Math.PI*2/36)-.6F)*LAG*fly;
        pose.pushPose();pose.translate(side*(11+2*(1-fly)),13,-8);
        pose.mulPose(Axis.YP.rotationDegrees(side*90*(1-fly)));pose.mulPose(Axis.ZP.rotationDegrees(side*(beat+6*(1-fly))));
        // Folded, each wing lies flat against her flank, its leading edge along the top of her back and its feathers hanging down.
        pose.mulPose(Axis.XP.rotationDegrees(-80*(1-fly)));
        arm(pose,b,side,light,fly);
        pose.translate(side*INNER_SPAN,0,0);pose.translate(0,-2.4F*(1-fly),0);
        // Folded, the hand lies back along the arm in the same plane, turned over so that its feathers lie the same way as the arm's.
        pose.mulPose(Axis.YP.rotationDegrees(side*170*(1-fly)));pose.mulPose(Axis.XP.rotationDegrees(180*(1-fly)));pose.mulPose(Axis.ZP.rotationDegrees(side*(lag+8*fly)));
        hand(pose,b,side,light,fly);
        pose.popPose();
    }
    /** A block running along the wing's span from {@code a} to {@code c} on its own side. */
    static void span(PoseStack pose,VertexConsumer b,int side,float a,float c,float y0,float z0,float y1,float z1,Skin s,int light){
        box(pose,b,side>0?a:-c,y0,z0,side>0?c:-a,y1,z1,s,light);
    }
    /** The inner wing: the scapulars where it meets her back, its thick leading edge, rows of coverts and the painted band, the secondaries. */
    static void arm(PoseStack pose,VertexConsumer b,int side,int light,float fly){
        boolean folded=fly<.5F;Skin coverts=folded?COVERTS_OUT:COVERTS,painted=folded?PAINTED_OUT:PAINTED,quill=folded?QUILL_OUT:QUILL;
        span(pose,b,side,0,9,-1.5F,-16,2.6F,5,coverts,light);
        span(pose,b,side,0,INNER_SPAN,-1.5F,-1,3.2F,5,coverts,light);
        span(pose,b,side,1,INNER_SPAN,-1,-9,2.2F,0,coverts,light);
        span(pose,b,side,1,INNER_SPAN,-.8F,-14,1.6F,-8,painted,light);
        span(pose,b,side,1,INNER_SPAN-1,-.6F,-20,1.1F,-13,coverts,light);
        // Eight secondaries, each its own feather, their tips scalloping the trailing edge. Folded, they lie back toward her tail like
        // shingles along her flank.
        for(int i=0;i<8;i++){
            float x=2.6F+i*5F,len=26+(i%2)*2.2F+i*.3F;
            pose.pushPose();pose.translate(side*x,-.1F*i,-12);pose.mulPose(Axis.YP.rotationDegrees(side*(3.5F-i)*1.1F*fly-side*70*(1-fly)));
            box(pose,b,-2.65F,-.5F,-len,2.65F,.55F,0,quill,light);
            pose.popPose();
        }
    }
    /** The outer wing: the leading edge tapering to the tip, the primary coverts, and nine primaries fanning out as she opens. */
    static void hand(PoseStack pose,VertexConsumer b,int side,int light,float fly){
        span(pose,b,side,0,30,-1.4F,-1,2.8F,4.5F,COVERTS,light);
        span(pose,b,side,30,OUTER_SPAN,-1,-.5F,2,3.5F,COVERTS,light);
        span(pose,b,side,0,36,-.9F,-11,1.8F,0,COVERTS,light);
        for(int i=0;i<9;i++){
            // Open, they fan out toward the tip; folded, they lie back toward her tail over the secondaries, their tips crossing her rump.
            float x=3+i*4.4F,len=28+i*1.8F-(i==8?5:0),fan=i*7.5F;
            pose.pushPose();pose.translate(side*x,-.25F-.08F*i,-7);pose.mulPose(Axis.YP.rotationDegrees(-side*fan*fly+side*(60+i*1.5F)*(1-fly)));
            box(pose,b,-2.5F,-.5F,-len,2.5F,.5F,0,QUILL,light);
            pose.popPose();
        }
    }

    // ------------------------------------------------------------------ her face
    /** The face of the first crossing on the front of her head, with its eyes behind the holes and her mouth when she speaks. */
    static void face(PoseStack pose,MultiBufferSource out,int light,float now){
        var m=pose.last();
        float hw=FACE_W/2,y0=FACE_Y-FACE_H/2,y1=FACE_Y+FACE_H/2;
        // The eyes, behind the face's holes, turned toward the camera.
        var cam=camera(pose);
        float ww=SphinxFaceRenderer.eyeScale(SphinxFaceRenderer.WHITE_W,FACE_W),wh=SphinxFaceRenderer.eyeScale(SphinxFaceRenderer.WHITE_H,FACE_W),ir=SphinxFaceRenderer.eyeScale(SphinxFaceRenderer.IRIS_R,FACE_W);
        for(int k=0;k<2;k++){
            float ex=px(Sphinx_EYES[k][0]),ey=py(Sphinx_EYES[k][1]);
            float gx=0,gy=0;
            if(cam!=null){var d=new Vector3f(cam).sub(ex,ey,FACE_Z);float len=d.length();if(len>1e-3){gx=Mth.clamp(d.x/len*1.6F,-1,1)*ww*.5F;gy=Mth.clamp(d.y/len*1.3F,-1,1)*wh*.4F;}}
            rect(m,out.getBuffer(RenderType.entityCutoutNoCull(SphinxFaceRenderer.WHITE)),ex-ww,ey-wh,ex+ww,ey+wh,FACE_Z-.15F,0,0,1,1,light);
            rect(m,out.getBuffer(RenderType.entityCutoutNoCull(SphinxFaceRenderer.IRIS)),ex+gx-ir,ey+gy-ir,ex+gx+ir,ey+gy+ir,FACE_Z-.1F,0,0,1,1,light);
            float closed=blink(now);
            if(closed>.01F){float h=wh*2*closed;rect(m,out.getBuffer(RenderType.entityCutoutNoCull(SphinxFaceRenderer.LID)),ex-ww,ey+wh-h,ex+ww,ey+wh,FACE_Z-.05F,0,1-closed,1,1,light);}
        }
        rect(m,out.getBuffer(RenderType.entityCutoutNoCull(SphinxFaceRenderer.FACE)),hw,y1,-hw,y0,FACE_Z,0,0,1,1,light);
        int mouth=SphinxMouth.frame(clientTime());
        if(mouth>0){
            float v0=(float)(mouth-1)/SphinxFaceRenderer.MOUTH_FRAMES,v1=v0+1F/SphinxFaceRenderer.MOUTH_FRAMES;
            int mx=SphinxFaceRenderer.MOUTH_X0,my=SphinxFaceRenderer.MOUTH_Y0;
            rect(m,out.getBuffer(RenderType.entityCutoutNoCull(SphinxFaceRenderer.MOUTH)),px(mx),py(my),px(mx+SphinxFaceRenderer.MOUTH_W),py(my+SphinxFaceRenderer.MOUTH_H),FACE_Z+.02F,0,v0,1,v1,light);
        }
    }
    static final int[][] Sphinx_EYES={{100,100},{188,100}};
    /** Picture pixels to head pixels: the picture's left is her right (+x), seen from in front. */
    static float px(float p){return (.5F-p/288F)*FACE_W;}
    static float py(float p){return FACE_Y+(.5F-p/240F)*FACE_H;}
    static float clientTime(){var mc=Minecraft.getInstance();return mc.level==null?0:mc.level.getGameTime()+mc.getTimer().getGameTimeDeltaPartialTick(false);}
    static float blink(float now){float phase=now%151;return phase<8?Mth.sin((float)Math.PI*phase/8):0;}
    /** The camera, in the pose's current frame; null if the frame cannot be inverted. */
    static Vector3f camera(PoseStack pose){var inv=new Matrix4f(pose.last().pose());if(Math.abs(inv.determinant())<1e-12)return null;inv.invert();return inv.transformPosition(new Vector3f());}

    // ------------------------------------------------------------------ blocks and quads
    /** One block of her body, from (x0, y0, z0) to (x1, y1, z1) in model pixels, each face covered from its skin. */
    static void box(PoseStack pose,VertexConsumer b,float x0,float y0,float z0,float x1,float y1,float z1,Skin s,int light){
        var m=pose.last();
        int seed=Float.floatToIntBits(x0*31.7F+y0*17.3F+z0*7.1F+x1*3.3F+y1*5.9F+z1*11.3F);seed^=seed>>>15;seed*=0x2c1b3c6d;seed^=seed>>>12;
        float dx=x1-x0,dy=y1-y0,dz=z1-z0;
        quad(m,b,x0,y1,z0,x0,y1,z1,x1,y1,z1,x1,y1,z0,s.top().window(dx,dz,seed),0,1,0,light);
        quad(m,b,x0,y0,z1,x0,y0,z0,x1,y0,z0,x1,y0,z1,s.bottom().window(dx,dz,seed*31+1),0,-1,0,light);
        quad(m,b,x0,y1,z1,x0,y0,z1,x1,y0,z1,x1,y1,z1,s.front().window(dx,dy,seed*37+2),0,0,1,light);
        quad(m,b,x1,y1,z0,x1,y0,z0,x0,y0,z0,x0,y1,z0,s.side().window(dx,dy,seed*41+3),0,0,-1,light);
        quad(m,b,x1,y1,z1,x1,y0,z1,x1,y0,z0,x1,y1,z0,s.side().window(dz,dy,seed*43+4),1,0,0,light);
        quad(m,b,x0,y1,z0,x0,y0,z0,x0,y0,z1,x0,y1,z1,s.side().window(dz,dy,seed*47+5),-1,0,0,light);
    }
    static void quad(PoseStack.Pose m,VertexConsumer b,float ax,float ay,float az,float bx,float by,float bz,float cx,float cy,float cz,float dx,float dy,float dz,float[] uv,float nx,float ny,float nz,int light){
        v(m,b,ax,ay,az,uv[0],uv[1],nx,ny,nz,light);v(m,b,bx,by,bz,uv[0],uv[3],nx,ny,nz,light);v(m,b,cx,cy,cz,uv[2],uv[3],nx,ny,nz,light);v(m,b,dx,dy,dz,uv[2],uv[1],nx,ny,nz,light);
    }
    /** A flat picture facing +z, from (x0, y0) at its top left to (x1, y1), at depth z. */
    static void rect(PoseStack.Pose m,VertexConsumer b,float x0,float y0,float x1,float y1,float z,float u0,float v0,float u1,float v1,int light){
        float top=Math.max(y0,y1),bottom=Math.min(y0,y1);
        v(m,b,x0,top,z,u0,v0,0,0,1,light);v(m,b,x0,bottom,z,u0,v1,0,0,1,light);v(m,b,x1,bottom,z,u1,v1,0,0,1,light);v(m,b,x1,top,z,u1,v0,0,0,1,light);
    }
    static void v(PoseStack.Pose m,VertexConsumer b,float x,float y,float z,float u,float vv,float nx,float ny,float nz,int light){
        b.addVertex(m,x,y,z).setColor(255,255,255,255).setUv(u,vv).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(m,nx,ny,nz);
    }
}
