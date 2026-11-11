package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** The Drisking cast on their own skins, and the machine house's fan drawn from explicit per-face islands of the pixel atlas. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class BorrascaNativeArt {
    public static final ResourceLocation ATLAS=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/borrasca_cast_pixels.png");
    public static final ModelLayerLocation CAST=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"borrasca_cast"),"main");
    private BorrascaNativeArt(){}
    private static final String[] PEOPLE={"kyle","kimber","sheriff","prescott","worker","worker_two","eli","wren","kelly_grown","dana","dana_aside","dana_scream"};
    /** Shared cast painter: six 256-pixel exports on the original 64x64 humanoid UV layout; 4 and 5 are the two plant workers (0.4.262: no longer one face twice). */
    public static ResourceLocation skin(int person){return ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/borrasca_"+PEOPLE[Math.max(0,Math.min(PEOPLE.length-1,person))]+".png");}
    public static ResourceLocation skin(int person,boolean worker){return skin(worker?4:Math.min(3,person));}
    /** Which painting an actor wears: a worker's phase says which of the two men he is. */
    public static int person(LiteraryActor a){if(a.role()==LiteraryActor.BORRASCA_KIMBER&&a.phase()==BorrascaStory.GROWN)return 8;if(dana(a))return 9;if(BorrascaStory.townChild(a.role(),a.phase()))return a.phase()==BorrascaStory.ELI?6:7;return a.phase()>=BorrascaStory.WORKER?(a.phase()==BorrascaStory.WORKER+1?5:4):Math.max(0,Math.min(3,a.role()-LiteraryActor.BORRASCA_KYLE));}
    /** 0.4.439: Dana, 141-A (BorrascaFeeding), and whether she is lying down. */
    public static boolean dana(LiteraryActor a){return a.role()==LiteraryActor.BORRASCA_KYLE&&a.phase()==BorrascaStory.DANA;}
    public static boolean laid(LiteraryActor a){int g=a.gesture();return dana(a)&&(g==BorrascaStory.LAID||g==BorrascaStory.LAID_STILL||g==BorrascaStory.TORN_LIVE||g==BorrascaStory.TORN_DEAD);}
    /** At the blade: the machine has her feet first, and the renderer cuts her body where the slot is. */
    public static boolean torn(LiteraryActor a){int g=a.gesture();return dana(a)&&(g==BorrascaStory.TORN_LIVE||g==BorrascaStory.TORN_DEAD);}
    /**
     * How hard her caption shakes (client.SpeechCaptions), 0 when she is not screaming. The owner: "text should be garbled and wobbly
     * like marjorie's and her awwwoo turning into an AAHHHHH and then -". It grows as the machine takes more of her.
     */
    public static float scream(LiteraryActor a){if(!dana(a)||a.gesture()!=BorrascaStory.TORN_LIVE)return 0;return .35F+Math.min(1,a.torn()/900F)*2.4F;}
    /** About one moment in nine her eyes are off to one side, each glance a second and a half. */
    public static boolean glancing(LiteraryActor a){return Math.floorMod(a.tickCount+a.getId()*37,180)<30;}
    @SubscribeEvent public static void layers(EntityRenderersEvent.RegisterLayerDefinitions e){e.registerLayerDefinition(CAST,BorrascaNativeArt::castLayer);}
    private static LayerDefinition castLayer(){
        var mesh=HumanoidModel.createMesh(CubeDeformation.NONE,0);var root=mesh.getRoot();
        // Head accessories inherit the actual head transform and the standard hat UV island.
        var hat=root.getChild("head").addOrReplaceChild("painted_hat",CubeListBuilder.create().texOffs(32,0).addBox(-4,-8,-4,8,8,8,new CubeDeformation(.5F)),PartPose.ZERO);
        // 0.4.439: every part again in one-pixel slices on the same UV islands, hidden unless a body is being cut (CastModel.cut).
        slices(root.getChild("head"),0,0,-4,-8,-4,8,8,8,false,CubeDeformation.NONE);
        slices(hat,32,0,-4,-8,-4,8,8,8,false,new CubeDeformation(.5F,0,.5F));
        slices(root.getChild("body"),16,16,-4,0,-2,8,12,4,false,CubeDeformation.NONE);
        slices(root.getChild("right_arm"),40,16,-3,-2,-2,4,12,4,false,CubeDeformation.NONE);
        slices(root.getChild("left_arm"),40,16,-1,-2,-2,4,12,4,true,CubeDeformation.NONE);
        slices(root.getChild("right_leg"),0,16,-2,0,-2,4,12,4,false,CubeDeformation.NONE);
        slices(root.getChild("left_leg"),0,16,-2,0,-2,4,12,4,true,CubeDeformation.NONE);
        return LayerDefinition.create(mesh,64,64);
    }
    /**
     * One part's box again as a stack of one-pixel slices along its length. Each slice's side faces read the same rows of the skin as
     * the whole box's; its top and bottom faces are inside the stack except at a cut, where a torn end is drawn over them.
     */
    private static void slices(PartDefinition part,int u,int v,float x,float y,float z,int w,int h,int d,boolean mirror,CubeDeformation grow){
        for(int i=0;i<h;i++){var cubes=CubeListBuilder.create().texOffs(u,v+i);if(mirror)cubes.mirror();part.addOrReplaceChild("cut"+i,cubes.addBox(x,y+i,z,w,1,d,grow),PartPose.ZERO);}
    }
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(BorrascaRegistry.MACHINE_ENTITY.get(),MachineRenderer::new);e.registerBlockEntityRenderer(BorrascaRegistry.CARVING_ENTITY.get(),BorrascaCarvingRenderer::new);e.registerEntityRenderer(BorrascaRegistry.BIKE.get(),BikeRenderer::new);e.registerEntityRenderer(io.github.knaitoe.theoldesthouse.labyrinth.BikeNightRegistry.BIKE.get(),BikeRenderer::new);e.registerEntityRenderer(io.github.knaitoe.theoldesthouse.labyrinth.BikeNightRegistry.SAND_BALL_THROWN.get(),net.minecraft.client.renderer.entity.ThrownItemRenderer::new);e.registerEntityRenderer(BorrascaRegistry.DOG.get(),BorrascaDogRenderer::new);}
    /** 0.4.435: the ridden bike's frame, wheel and cranks, which no block state names. 0.4.463: and each of Bike night's six bikes'. */
    @SubscribeEvent public static void models(net.neoforged.neoforge.client.event.ModelEvent.RegisterAdditional e){BikeLook.register(e::register);}
    /** 0.4.444: the blood under the Sheriff's body, and how far off the ground his body is drawn so that it lies on it. */
    public static final ResourceLocation POOL=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/borrasca_blood_pool.png");
    static final float FALLEN_LIFT=.15F;
    public static final class CastRenderer extends MobRenderer<LiteraryActor,CastModel>{
        private Matrix4f renderToEntity;
        private Vec3 entityPosition;

        public CastRenderer(EntityRendererProvider.Context c){
            super(c,new CastModel(c.bakeLayer(CAST)),.25F);
            addLayer(new ItemInHandLayer<LiteraryActor,CastModel>(this,c.getItemInHandRenderer()){
                @Override public void render(PoseStack pose,MultiBufferSource buffers,int light,LiteraryActor a,
                                             float walk,float amount,float partial,float age,float yaw,float pitch){
                    super.render(pose,buffers,light,a,walk,amount,partial,age,yaw,pitch);
                    ItemStack stack=a.getMainHandItem();
                    if(renderToEntity==null||!stack.is(BorrascaRegistry.FLASHLIGHT.get()))return;

                    // The lens is at the top centre of the model. Follow the same hand and item transforms as the held mesh.
                    HumanoidArm arm=a.getMainArm();
                    boolean left=arm==HumanoidArm.LEFT;
                    var context=left?ItemDisplayContext.THIRD_PERSON_LEFT_HAND:ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
                    pose.pushPose();
                    getParentModel().translateToHand(arm,pose);
                    pose.mulPose(Axis.XP.rotationDegrees(-90));
                    pose.mulPose(Axis.YP.rotationDegrees(180));
                    pose.translate((left?-1:1)/16F,.125F,-.625F);
                    var item=Minecraft.getInstance().getItemRenderer().getModel(stack,a.level(),a,a.getId()+context.ordinal());
                    item.getTransforms().getTransform(context).apply(left,pose);
                    var toWorld=new Matrix4f(renderToEntity).mul(pose.last().pose());
                    var lens=toWorld.transformPosition(0,.5F,0,new Vector3f());
                    var forward=toWorld.transformDirection(0,1,0,new Vector3f());
                    pose.popPose();
                    BorrascaAtmosphere.sheriffLens(entityPosition.add(lens.x,lens.y,lens.z),
                            new Vec3(forward.x,forward.y,forward.z).normalize());
                }
            });
            // 0.4.444 (owner: "pool blood around corpse?"): under the Sheriff where he lies. In the model's own space he lies along y
            // with the ground in front of him (the way setupRotations turns him), so the pool is a sheet just above that ground, about
            // his chest and head. It spreads with his synced torn, and has dried dark on a later visit.
            addLayer(new net.minecraft.client.renderer.entity.layers.RenderLayer<LiteraryActor,CastModel>(this){
                @Override public void render(PoseStack pose,MultiBufferSource buffers,int light,LiteraryActor a,float walk,float amount,float partial,float age,float yaw,float pitch){
                    if(a.role()!=LiteraryActor.BORRASCA_SHERIFF||a.gesture()!=BorrascaStory.FALLEN||a.torn()<=0||a.isInvisible())return;
                    boolean dried=a.torn()>BorrascaStory.POOL_FULL;float s=(float)Math.sqrt(Math.min(BorrascaStory.POOL_FULL,a.torn())/(float)BorrascaStory.POOL_FULL);
                    float cx=-.08F,cy=.3F,hw=.95F*s,hh=.85F*s,z=-FALLEN_LIFT+1/64F;int c=dried?0xFF7A5148:0xFFFFFFFF;
                    var out=buffers.getBuffer(RenderType.entityTranslucent(POOL));var m=pose.last();
                    float[][] q={{cx-hw,cy-hh,0,0},{cx+hw,cy-hh,1,0},{cx+hw,cy+hh,1,1},{cx-hw,cy+hh,0,1}};
                    for(var v:q)out.addVertex(m,v[0],v[1],z).setColor(c).setUv(v[2],v[3]).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(light).setNormal(m,0,0,1);
                }
            });
        }

        @Override public void render(LiteraryActor a,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){
            if(BorrascaAtmosphere.rendersSheriffBeam(a)){
                renderToEntity=new Matrix4f(pose.last().pose()).invert();
                entityPosition=a.getPosition(partial);
            }
            try{super.render(a,yaw,partial,pose,buffers,light);}
            finally{renderToEntity=null;}
        }

        /** 0.4.439: Dana's eyes, now and then, go to one side for a moment (the owner: "eyes occasionally looking to one side"). */
        @Override public ResourceLocation getTextureLocation(LiteraryActor a){int person=person(a);if(person==9&&a.gesture()==BorrascaStory.LAID&&glancing(a))person=10;if(person==9&&torn(a))person=11;return skin(person);}
        /** 0.4.427: the Sheriff sat down in the dirt, or lying on his back where he fell. */
        @Override protected void setupRotations(LiteraryActor a,PoseStack pose,float age,float yaw,float partial,float scale){
            super.setupRotations(a,pose,age,yaw,partial,scale);
            // 0.4.439: Dana on her back on the cot or the belt, feet the way she faces.
            if(laid(a)){pose.translate(0,.13F,0);pose.mulPose(Axis.XP.rotationDegrees(90));
                // At the blade, alive: she writhes, rolling about her own length and arching off the belt.
                if(a.gesture()==BorrascaStory.TORN_LIVE){float t=age+a.getId()*7;pose.translate(0,0,-.035F*Math.max(0,net.minecraft.util.Mth.sin(t*1.3F)));
                    pose.mulPose(Axis.YP.rotationDegrees(7*net.minecraft.util.Mth.sin(t*.9F)+4*net.minecraft.util.Mth.sin(t*2.7F)));}
                return;}
            if(a.role()!=LiteraryActor.BORRASCA_SHERIFF)return;
            if(a.gesture()==BorrascaStory.KNELT)pose.translate(0,-.6F,0);
            else if(a.gesture()==BorrascaStory.FALLEN){pose.translate(0,FALLEN_LIFT,0);pose.mulPose(Axis.XP.rotationDegrees(-90));}
        }
        @Override protected boolean shouldShowName(LiteraryActor a){return a.isCustomNameVisible()&&!a.isInvisible()&&super.shouldShowName(a);}
    }
    public static final class CastModel extends HumanoidModel<LiteraryActor>{
        private float brow,tilt,mouth;
        /**
         * 0.4.439: each part again in one-pixel slices (castLayer) and where a body is cut through them. Parts in order: head, hair,
         * body, right arm, left arm, right leg, left leg. NaN when nobody is cut, and then only the whole parts are drawn.
         */
        private final ModelPart paintedHat;private final ModelPart[] owners;private final ModelPart[][] cuts;private final boolean[][] shown;
        private float cutAt=Float.NaN,split;
        private record Wet(ModelPart outer,ModelPart part,ModelPart slice,float depth){}
        private final java.util.List<Wet> wet=new java.util.ArrayList<>();
        private static final float[] FROM={-8,-8,0,-2,-2,0,0},MID={0,0,0,-1,1,0,0},HALF_W={4,4,4,2,2,2,2},HALF_D={4,4,2,2,2,2,2};
        public CastModel(ModelPart root){super(root);hat.visible=false;paintedHat=head.getChild("painted_hat");
            owners=new ModelPart[]{head,paintedHat,body,rightArm,leftArm,rightLeg,leftLeg};cuts=new ModelPart[owners.length][];shown=new boolean[owners.length][];
            for(int i=0;i<owners.length;i++){int n=i<2?8:12;cuts[i]=new ModelPart[n];shown[i]=new boolean[n];for(int k=0;k<n;k++)cuts[i][k]=owners[i].getChild("cut"+k);}
            whole();}
        @Override public void renderToBuffer(PoseStack p,VertexConsumer out,int light,int overlay,int colour){
            super.renderToBuffer(p,out,light,overlay,colour);
            if(brow!=0||tilt!=0||mouth!=0)GhostsSkinMesh.standardExpression(head,p,out,light,overlay,colour,brow,tilt,mouth);
            if(!Float.isNaN(cutAt))torn(p,out,light,overlay);
        }
        /** Whether this client has the actor riding a carrier it can see. */
        static boolean carriedHere(LiteraryActor a){return a.getVehicle() instanceof LiteraryActor v&&!v.isInvisible();}
        /** Every part drawn whole and the slices hidden: everyone but a body at the blade. */
        void whole(){cutAt=Float.NaN;wet.clear();for(int i=0;i<owners.length;i++){owners[i].skipDraw=false;for(var cut:cuts[i])cut.visible=false;}}
        /** A point of a part, as high in the model as it sits after the part's own turn (pixels; the feet are at 24). */
        private static float height(ModelPart part,float x,float y,float z){var v=new Vector3f(x,y,z);new org.joml.Quaternionf().rotationZYX(part.zRot,part.yRot,part.xRot).transform(v);return part.y+v.y;}
        /**
         * 0.4.439 (owner: "the game should track the splitting/tearing of her body so it doesn't tear all at once"). The machine has
         * {@code consumed} pixels of her, measured along her length from her feet. Every slice whose middle is past that is gone into the
         * slot; the two nearest the cut are drawn soaked, and the skin above it is split open further the more it has taken.
         */
        void cut(float consumed){
            cutAt=24-consumed;split=Math.max(0,Math.min(7,(consumed-2)*.45F));wet.clear();
            for(int i=0;i<owners.length;i++){var part=owners[i];part.skipDraw=true;var frame=i==1?head:part;
                for(int k=0;k<cuts[i].length;k++){float y=height(frame,MID[i],FROM[i]+k+.5F,0);boolean in=y<cutAt;shown[i][k]=in;cuts[i][k].visible=in;
                    if(in&&y>cutAt-2.2F){wet.add(new Wet(i==1?head:null,part,cuts[i][k],cutAt-y));cuts[i][k].visible=false;}}}
        }
        private static boolean any(boolean[] row){for(boolean b:row)if(b)return true;return false;}
        /** The soaked slices, the torn ends where the body stops, the skin split up from the cut, and broken bone. */
        private void torn(PoseStack p,VertexConsumer out,int light,int overlay){
            for(var w:wet){p.pushPose();if(w.outer()!=null)w.outer().translateAndRotate(p);w.part().translateAndRotate(p);w.slice().visible=true;
                w.slice().render(p,out,light,overlay,w.depth()<1.2F?0xFF7A1C16:0xFFB0544A);w.slice().visible=false;p.popPose();}
            for(int i:new int[]{0,2,3,4,5,6}){
                p.pushPose();owners[i].translateAndRotate(p);
                for(int k=0;k+1<cuts[i].length;k++)if(shown[i][k]!=shown[i][k+1])end(p,out,light,overlay,i,FROM[i]+k+1,shown[i][k]?1:-1);
                // The hips once the legs are gone, and the neck once the body is.
                if(i==2&&shown[2][11]&&!any(shown[5])&&!any(shown[6]))end(p,out,light,overlay,2,12,1);
                if(i==0&&shown[0][7]&&!any(shown[2]))end(p,out,light,overlay,0,0,1);
                // Split open: up the front of the belly and the thighs from the cut, a slice at a time.
                if(split>0&&(i==2||i>=5))for(int k=0;k<cuts[i].length;k++){if(!shown[i][k])continue;float y=height(owners[i],MID[i],FROM[i]+k+.5F,0);
                    if(y>cutAt-split){float w=i==2?1:.5F,y0=FROM[i]+k,z=-HALF_D[i]-.03F;quad(p,out,light,overlay,new float[][]{{MID[i]-w,y0,z},{MID[i]+w,y0,z},{MID[i]+w,y0+1,z},{MID[i]-w,y0+1,z}},56,24,58,26,0,0,-1);}}
                p.popPose();
            }
        }
        /** A torn end across part {@code i} at its own height {@code y}, facing the way the body has gone ({@code side} +1 or -1). */
        private void end(PoseStack p,VertexConsumer out,int light,int overlay,int i,float y,int side){
            float x0=MID[i]-HALF_W[i],x1=MID[i]+HALF_W[i],d=HALF_D[i],e=y+side*.03F;
            quad(p,out,light,overlay,new float[][]{{x0,e,-d},{x1,e,-d},{x1,e,d},{x0,e,d}},56,16,64,24,0,side,0);
            // Bone out of the middle of a limb, the spine near the back of the body or neck, and two strips of meat hanging off the rim.
            float bx=MID[i],bz=i==0||i==2?1.2F:0,len=i==0||i==2?1F:1.4F;
            box(p,out,light,overlay,bx-.45F,side>0?y:y-len,bz-.45F,bx+.45F,side>0?y+len:y,bz+.45F,59,19,61,21);
            for(int t=0;t<2;t++){float tx=t==0?x0+.4F:x1-1.2F,tz=t==0?-d+.2F:d-1F,hang=.6F+.3F*t;
                box(p,out,light,overlay,tx,side>0?y:y-hang,tz,tx+.8F,side>0?y+hang:y,tz+.8F,57,17,58,18);}
        }
        private static void box(PoseStack p,VertexConsumer out,int light,int overlay,float x0,float y0,float z0,float x1,float y1,float z1,float u0,float v0,float u1,float v1){
            quad(p,out,light,overlay,new float[][]{{x0,y0,z0},{x1,y0,z0},{x1,y1,z0},{x0,y1,z0}},u0,v0,u1,v1,0,0,-1);
            quad(p,out,light,overlay,new float[][]{{x1,y0,z1},{x0,y0,z1},{x0,y1,z1},{x1,y1,z1}},u0,v0,u1,v1,0,0,1);
            quad(p,out,light,overlay,new float[][]{{x0,y0,z1},{x0,y0,z0},{x0,y1,z0},{x0,y1,z1}},u0,v0,u1,v1,-1,0,0);
            quad(p,out,light,overlay,new float[][]{{x1,y0,z0},{x1,y0,z1},{x1,y1,z1},{x1,y1,z0}},u0,v0,u1,v1,1,0,0);
            quad(p,out,light,overlay,new float[][]{{x0,y0,z1},{x1,y0,z1},{x1,y0,z0},{x0,y0,z0}},u0,v0,u1,v1,0,-1,0);
            quad(p,out,light,overlay,new float[][]{{x0,y1,z0},{x1,y1,z0},{x1,y1,z1},{x0,y1,z1}},u0,v0,u1,v1,0,1,0);
        }
        /** One quad in model pixels, textured from the cast skin's own 64-pixel layout. */
        private static void quad(PoseStack p,VertexConsumer out,int light,int overlay,float[][] q,float u0,float v0,float u1,float v1,float nx,float ny,float nz){
            float[][] uv={{u0,v0},{u1,v0},{u1,v1},{u0,v1}};
            for(int i=0;i<4;i++)out.addVertex(p.last(),q[i][0]/16,q[i][1]/16,q[i][2]/16).setColor(-1).setUv(uv[i][0]/64,uv[i][1]/64).setOverlay(overlay).setLight(light).setNormal(p.last(),nx,ny,nz);
        }
        /**
         * 0.4.262 (owner: the workers "need proper walking animations"). The pose is eased between gestures as before, but the stride is laid
         * on afterwards, so the easing never damps a step: before, it smoothed the leg swing away to a shuffle and the cadence was twice a
         * walk's. Now a full stride takes about two blocks, the legs swing their whole way, and an arm a gesture is not using swings opposite
         * its leg.
         */
        @Override public void setupAnim(LiteraryActor a,float walk,float amount,float age,float yaw,float pitch){HumanoidMotion.reset(this);whole();young=false;if(a.gesture()==BorrascaStory.CARRIED&&!carriedHere(a))riding=false;
            // 0.4.444 (owner: "Sheriff continues to wiggle after 'death'"): a body does not breathe, sway its arms or turn its head.
            boolean dead=a.gesture()==BorrascaStory.FALLEN;
            if(dead)super.setupAnim(a,0,0,0,0,0);else super.setupAnim(a,walk,amount,age,yaw,pitch);var motion=HumanoidMotion.sample(a,age-a.tickCount);if(!dead)body.xRot+=.015F*(float)Math.sin(age*.055F+a.getId());
            // 0.4.440 (the owner's screenshot: Kelly sitting in the air): she is drawn carried only while this client has her in the arms
            // of a man it can see; otherwise she is drawn being led, on her feet.
            int gesture=carriedHere(a)||a.gesture()!=BorrascaStory.CARRIED?a.gesture():BorrascaStory.LED;
            pose(gesture,a.role()==LiteraryActor.BORRASCA_SHERIFF,age);HumanoidMotion.connect(this);RecentActorMotion.blend(a,age,this);
            if(gesture!=BorrascaStory.CARRIED&&!BorrascaNativeArt.laid(a))stride(gesture,motion.phase()*.45F,motion.amount());
            head.yRot=net.minecraft.util.Mth.clamp(head.yRot,-.95F,.95F);
            if(BorrascaNativeArt.laid(a))lie(a.gesture()==BorrascaStory.LAID,age+a.getId()*13);
            if(BorrascaNativeArt.torn(a)){if(a.gesture()==BorrascaStory.TORN_LIVE)thrash(age+a.getId()*13);else limp();cut(a.torn()/1000F*16);}
            brow=tilt=mouth=0;
            gesture=a.gesture();
            if(a.role()==LiteraryActor.BORRASCA_KIMBER){
                if(gesture==10){brow=.28F;mouth=.8F;}
                if(gesture==BorrascaStory.SCARED||gesture==BorrascaStory.CARRIED){brow=.85F;tilt=.45F;mouth=-.65F;}
            }else if(BorrascaStory.worker(a.role(),a.phase())&&
                    (gesture==BorrascaStory.WARNING||gesture==BorrascaStory.TAKING_HAND||gesture==BorrascaStory.CARRY)){
                brow=-.85F;tilt=-.62F;mouth=-.6F;
            }
        }
        /**
         * 0.4.439: Dana on her back. She stares straight up; her arms hang out over the sides of the belt (or the cot) and, while she
         * lives, sway a little with it. Nothing else moves.
         */
        void lie(boolean living,float age){
            head.xRot=head.yRot=head.zRot=0;body.xRot=body.yRot=body.zRot=0;
            leftLeg.xRot=rightLeg.xRot=0;leftLeg.yRot=rightLeg.yRot=0;leftLeg.zRot=-.04F;rightLeg.zRot=.04F;
            float sway=living?.11F*(float)Math.sin(age*.06F):0,drift=living?.07F*(float)Math.sin(age*.047F+1.3F):0;
            rightArm.xRot=.72F+drift;rightArm.yRot=0;rightArm.zRot=1.05F+sway;
            leftArm.xRot=.72F-drift;leftArm.yRot=0;leftArm.zRot=-1.05F+sway;
        }
        /** 0.4.439: alive at the blade. Her head thrown back and from side to side, both arms clawing up off the belt. */
        void thrash(float t){
            head.xRot=-.35F+.18F*net.minecraft.util.Mth.sin(t*.37F);head.yRot=.6F*net.minecraft.util.Mth.sin(t*.83F)+.25F*net.minecraft.util.Mth.sin(t*2.1F);head.zRot=0;
            rightArm.xRot=-.95F+.85F*net.minecraft.util.Mth.sin(t*.61F)+.3F*net.minecraft.util.Mth.sin(t*1.9F);rightArm.yRot=.3F*net.minecraft.util.Mth.sin(t*1.3F);rightArm.zRot=.6F+.45F*net.minecraft.util.Mth.sin(t*.47F+1);
            leftArm.xRot=-.95F+.85F*net.minecraft.util.Mth.sin(t*.57F+2)+.3F*net.minecraft.util.Mth.sin(t*2.2F);leftArm.yRot=.3F*net.minecraft.util.Mth.sin(t*1.1F+.5F);leftArm.zRot=-.6F-.45F*net.minecraft.util.Mth.sin(t*.51F);
        }
        /** After: her head fallen to one side, her arms hanging off the belt. */
        void limp(){head.xRot=-.2F;head.yRot=.55F;head.zRot=0;rightArm.xRot=leftArm.xRot=.95F;rightArm.yRot=leftArm.yRot=0;rightArm.zRot=1.25F;leftArm.zRot=-1.25F;}
        /** Legs and free arms swinging with the distance actually walked; a sheltering crouch keeps its legs. */
        void stride(int gesture,float phase,float amount){
            if(gesture==BorrascaStory.KNELT||gesture==BorrascaStory.FALLEN)return;
            if(amount<=.01F)return;float c=net.minecraft.util.Mth.cos(phase),swing=.72F*amount;
            if(gesture!=5&&gesture!=BorrascaStory.CARRIED){rightLeg.xRot+=c*swing;leftLeg.xRot-=c*swing;}
            boolean right=gesture==0||gesture==8,left=gesture!=3&&gesture!=5&&gesture!=8&&gesture!=BorrascaStory.CARRY&&gesture!=10&&(gesture<BorrascaSheriffFight.GUARD||gesture>BorrascaSheriffFight.REEL);
            if(right)rightArm.xRot-=c*.5F*amount;if(left)leftArm.xRot+=c*.5F*amount;
            float bob=-Math.abs(net.minecraft.util.Mth.sin(phase))*.22F*amount;for(var part:new ModelPart[]{head,body,leftArm,rightArm,leftLeg,rightLeg})part.y+=bob;
        }
        private void pose(int gesture,boolean sheriff,float age){
            switch(gesture){
                case 1->{rightArm.xRot=-2.1F;rightArm.zRot=-.25F;head.xRot+=.15F;}
                case 2->{rightArm.xRot=-1.4F;rightArm.yRot=-.25F;}
                case 3->{body.yRot=-.15F;head.xRot=.5F;leftArm.xRot=-1.1F;rightArm.xRot=-1.3F+.18F*(float)Math.sin(age*.5F);}
                case 4->{rightArm.xRot=sheriff?-1.35F:-1.1F;rightArm.zRot=-.1F;}
                case 5->{body.xRot=.42F;leftLeg.xRot=-.3F;rightLeg.xRot=-.3F;leftArm.xRot=-.35F;rightArm.xRot=-.35F;head.xRot=-.1F;}
                // 0.4.235: led by the arm, the right arm drawn forward and up, the head turned back over the shoulder.
                case 6->{rightArm.xRot=-1.25F;rightArm.zRot=.15F;leftArm.xRot=.35F;head.yRot=.9F;head.xRot=-.1F;body.xRot=.08F;}
                // A taker's hand held out, low, the other arm at his side.
                case 7->{rightArm.xRot=-1.05F;rightArm.zRot=-.05F;body.xRot=.06F;head.xRot=.25F;}
                // 0.4.262: walking her by the arm, his hand on her upper arm: she is on his left (8) or his right (9); his head turned to her.
                case 8->{leftArm.xRot=-.62F;leftArm.zRot=-.3F;body.yRot=.1F;head.yRot=.35F;head.xRot=.12F;}
                case 9->{rightArm.xRot=-.62F;rightArm.zRot=.3F;body.yRot=-.1F;head.yRot=-.35F;head.xRot=.12F;}
                case 10->{leftArm.xRot=-1.45F;leftArm.yRot=.24F;head.xRot=.24F;}
                case BorrascaStory.CARRIED->{
                    body.xRot=-.5F;body.zRot=.25F;head.xRot=.4F;head.yRot=.8F;
                    leftLeg.xRot=-1.3F;rightLeg.xRot=-1.1F;rightArm.xRot=-.55F;leftArm.xRot=-.6F;
                }
                case BorrascaStory.CARRY->{rightArm.xRot=leftArm.xRot=-1.55F;rightArm.yRot=-.28F;leftArm.yRot=.28F;}
                case BorrascaStory.WARNING->{rightArm.xRot=-1.28F;rightArm.yRot=-.35F;head.xRot=.15F;}
                // 0.4.427: the Sheriff beaten down, sitting in the dirt with his legs out and his hands up; and dead, flat on his back.
                case BorrascaStory.KNELT->{leftLeg.xRot=rightLeg.xRot=-1.45F;leftLeg.zRot=-.12F;rightLeg.zRot=.12F;body.xRot=.22F;head.xRot=.38F;leftArm.xRot=rightArm.xRot=-1.2F;leftArm.zRot=.28F;rightArm.zRot=-.28F;}
                case BorrascaStory.FALLEN->{leftArm.zRot=-.32F;rightArm.zRot=.4F;leftLeg.zRot=-.08F;rightLeg.zRot=.1F;head.yRot=.55F;head.xRot=0;}
                // 0.4.457 (owner: "Punches should be sluggish with real weight. The sheriff should use his baton ... He swings with one fist
                // then the other."): the Sheriff's fight (BorrascaSheriffFight). His hands up between blows; the baton raised over his right
                // shoulder with that shoulder drawn back, and brought down across him as he leans into it; each fist drawn back low with its
                // shoulder, then thrown level and across with that shoulder behind it; and reeling from a blow, his head back and his arms out.
                case BorrascaSheriffFight.GUARD->{body.xRot=.1F;head.xRot=.08F;rightArm.xRot=-1.15F;rightArm.yRot=-.35F;leftArm.xRot=-1.05F;leftArm.yRot=.4F;}
                case BorrascaSheriffFight.BATON_UP->{body.yRot=.5F;body.xRot=-.06F;rightArm.xRot=-3.0F;rightArm.zRot=.2F;leftArm.xRot=-.9F;leftArm.yRot=.5F;rightLeg.xRot=.22F;leftLeg.xRot=-.18F;}
                case BorrascaSheriffFight.BATON_DOWN->{body.yRot=-.45F;body.xRot=.3F;head.xRot=.15F;rightArm.xRot=-.5F;rightArm.yRot=-.5F;leftArm.xRot=.4F;rightLeg.xRot=-.3F;leftLeg.xRot=.25F;}
                case BorrascaSheriffFight.LEFT_BACK->{body.yRot=-.45F;body.xRot=.06F;leftArm.xRot=-.35F;leftArm.zRot=-.2F;rightArm.xRot=-1.2F;rightArm.yRot=-.4F;}
                case BorrascaSheriffFight.LEFT_OUT->{body.yRot=.5F;body.xRot=.18F;leftArm.xRot=-1.5F;leftArm.yRot=.4F;rightArm.xRot=-.9F;rightArm.yRot=-.3F;leftLeg.xRot=-.25F;rightLeg.xRot=.2F;}
                case BorrascaSheriffFight.RIGHT_BACK->{body.yRot=.45F;body.xRot=.06F;rightArm.xRot=-.35F;rightArm.zRot=.2F;leftArm.xRot=-1.2F;leftArm.yRot=.4F;}
                case BorrascaSheriffFight.RIGHT_OUT->{body.yRot=-.5F;body.xRot=.18F;rightArm.xRot=-1.5F;rightArm.yRot=-.4F;leftArm.xRot=-.9F;leftArm.yRot=.3F;rightLeg.xRot=-.25F;leftLeg.xRot=.2F;}
                case BorrascaSheriffFight.REEL->{body.xRot=-.28F;head.xRot=-.4F;rightArm.xRot=-.35F;rightArm.zRot=.55F;leftArm.xRot=-.35F;leftArm.zRot=-.55F;leftLeg.xRot=.3F;rightLeg.xRot=-.1F;}
                default->{}
            }}
        /** Keep the approved child review poses; adults use the same full-length body as their live renderer. */
        public void preview(int person,float age,float stride){brow=tilt=mouth=0;HumanoidMotion.reset(this);whole();young=person<2||person==6||person==7;crouching=false;riding=false;attackTime=0;HumanoidMotion.gait(this,age*.22F,stride,false);head.yRot=.22F*(float)Math.sin(age*.02F);HumanoidMotion.connect(this);}
        /** 0.4.439: Dana as she lies on the belt, for the proof. */
        public void previewLaid(float age){preview(9,age,0);lie(true,age);HumanoidMotion.connect(this);}
        /** 0.4.439: Dana at the blade with {@code consumed} pixels of her taken, alive or after, for the proof. */
        public void previewTorn(float age,float consumed,boolean living){preview(9,age,0);lie(false,age);if(living)thrash(age);else limp();HumanoidMotion.connect(this);cut(consumed);}
        public void previewPose(int person,int gesture,float age){preview(person,age,0);pose(gesture,person==2,age);HumanoidMotion.connect(this);}
    }
    public static void cube(PoseStack p,VertexConsumer out,float x0,float y0,float z0,float x1,float y1,float z1,int tile,int light,int overlay,int colour){
        quad(p,out,new float[][]{{x0,y0,z0},{x1,y0,z0},{x1,y1,z0},{x0,y1,z0}},tile,0,0,-1,light,overlay,colour);
        quad(p,out,new float[][]{{x1,y0,z1},{x0,y0,z1},{x0,y1,z1},{x1,y1,z1}},tile,0,0,1,light,overlay,colour);
        quad(p,out,new float[][]{{x0,y0,z1},{x0,y0,z0},{x0,y1,z0},{x0,y1,z1}},tile,-1,0,0,light,overlay,colour);
        quad(p,out,new float[][]{{x1,y0,z0},{x1,y0,z1},{x1,y1,z1},{x1,y1,z0}},tile,1,0,0,light,overlay,colour);
        quad(p,out,new float[][]{{x0,y0,z1},{x1,y0,z1},{x1,y0,z0},{x0,y0,z0}},tile,0,-1,0,light,overlay,colour);
        quad(p,out,new float[][]{{x0,y1,z0},{x1,y1,z0},{x1,y1,z1},{x0,y1,z1}},tile,0,1,0,light,overlay,colour);
    }
    private static void quad(PoseStack p,VertexConsumer out,float[][] q,int tile,float nx,float ny,float nz,int light,int overlay,int colour){boolean hair=tile>=16;if(hair)tile-=16;float u0=(tile%4+.006F)/4,v0=(tile/4+.006F)/4,u1=(tile%4+.994F)/4,v1=(tile/4+(hair?.20F:.994F))/4;float[][] uv={{u0,v0},{u1,v0},{u1,v1},{u0,v1}};for(int i=0;i<4;i++)out.addVertex(p.last(),q[i][0]/16,q[i][1]/16,q[i][2]/16).setColor(colour).setUv(uv[i][0],uv[i][1]).setOverlay(overlay).setLight(light).setNormal(p.last(),nx,ny,nz);}
    /**
     * The drum in the housing; 0.4.439: the silver cone above it, its arms and hoops turning (BorrascaOrrery), and the mouth the belt runs into.
     * For the two seconds a reader sees the Shiny Gentleman there (BorrascaFeedingClient), he is drawn in the cone's place, not over it.
     */
    public static final class MachineRenderer implements BlockEntityRenderer<BorrascaMachineBlock.Machine>{public MachineRenderer(BlockEntityRendererProvider.Context ignored){}
        @Override public void render(BorrascaMachineBlock.Machine e,float partial,PoseStack p,MultiBufferSource buffers,int light,int overlay){
            drawMachine(p,buffers,light,overlay,e.angle(partial));
            var level=Minecraft.getInstance().level;float time=level==null?0:level.getGameTime()+partial;
            var him=BorrascaFeedingClient.at(e.getBlockPos(),partial);
            if(him==null)BorrascaOrrery.draw(p,buffers,light,e.run(partial),e.power(),time);
            BorrascaOrrery.mouth(p,buffers,light);
            if(him!=null)BorrascaOrrery.devour(p,buffers,him.slot(),him.age(),time,skin(11));
        }
        @Override public boolean shouldRenderOffScreen(BorrascaMachineBlock.Machine e){return true;}
        /** 0.4.440: it stands in the refinery now, seen from inside or through its doorway. */
        @Override public int getViewDistance(){return 64;}
        @Override public net.minecraft.world.phys.AABB getRenderBoundingBox(BorrascaMachineBlock.Machine e){var at=e.getBlockPos();return new net.minecraft.world.phys.AABB(at.getX()-5,at.getY(),at.getZ()-5,at.getX()+6,at.getY()+17,at.getZ()+6);}
    }
    public static void drawMachine(PoseStack p,MultiBufferSource buffers,int light,int overlay,float angle){var out=buffers.getBuffer(RenderType.entityCutoutNoCull(ATLAS));p.pushPose();p.translate(.5,.7,.5);p.mulPose(Axis.XP.rotation(angle));for(int i=0;i<8;i++){p.pushPose();p.mulPose(Axis.XP.rotation((float)(i*Math.PI/4)));cube(p,out,-11,-7,-2,11,-5,2,13,light,overlay,0xffffffff);p.popPose();}cube(p,out,-13,-2,-2,13,2,2,14,light,overlay,0xffffffff);p.popPose();}
}
