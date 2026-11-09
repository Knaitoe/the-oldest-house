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
    private static final String[] PEOPLE={"kyle","kimber","sheriff","prescott","worker","worker_two","eli","wren"};
    /** Shared cast painter: six 256-pixel exports on the original 64x64 humanoid UV layout; 4 and 5 are the two plant workers (0.4.262: no longer one face twice). */
    public static ResourceLocation skin(int person){return ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/borrasca_"+PEOPLE[Math.max(0,Math.min(PEOPLE.length-1,person))]+".png");}
    public static ResourceLocation skin(int person,boolean worker){return skin(worker?4:Math.min(3,person));}
    /** Which painting an actor wears: a worker's phase says which of the two men he is. */
    public static int person(LiteraryActor a){if(BorrascaStory.townChild(a.role(),a.phase()))return a.phase()==BorrascaStory.ELI?6:7;return a.phase()>=BorrascaStory.WORKER?(a.phase()==BorrascaStory.WORKER+1?5:4):Math.max(0,Math.min(3,a.role()-LiteraryActor.BORRASCA_KYLE));}
    @SubscribeEvent public static void layers(EntityRenderersEvent.RegisterLayerDefinitions e){e.registerLayerDefinition(CAST,BorrascaNativeArt::castLayer);}
    private static LayerDefinition castLayer(){
        var mesh=HumanoidModel.createMesh(CubeDeformation.NONE,0);
        // Head accessories inherit the actual head transform and the standard hat UV island.
        mesh.getRoot().getChild("head").addOrReplaceChild("painted_hat",CubeListBuilder.create().texOffs(32,0).addBox(-4,-8,-4,8,8,8,new CubeDeformation(.5F)),PartPose.ZERO);
        return LayerDefinition.create(mesh,64,64);
    }
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(BorrascaRegistry.MACHINE_ENTITY.get(),MachineRenderer::new);e.registerBlockEntityRenderer(BorrascaRegistry.CARVING_ENTITY.get(),BorrascaCarvingRenderer::new);e.registerEntityRenderer(BorrascaRegistry.BIKE.get(),BorrascaBikeRenderer::new);}
    /** 0.4.435: the ridden bike's frame, wheel and cranks, which no block state names. */
    @SubscribeEvent public static void models(net.neoforged.neoforge.client.event.ModelEvent.RegisterAdditional e){e.register(BorrascaBikeRenderer.FRAME);e.register(BorrascaBikeRenderer.WHEEL);e.register(BorrascaBikeRenderer.CRANKS);}
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
        }

        @Override public void render(LiteraryActor a,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){
            if(BorrascaAtmosphere.rendersSheriffBeam(a)){
                renderToEntity=new Matrix4f(pose.last().pose()).invert();
                entityPosition=a.getPosition(partial);
            }
            try{super.render(a,yaw,partial,pose,buffers,light);}
            finally{renderToEntity=null;}
        }

        @Override public ResourceLocation getTextureLocation(LiteraryActor a){return skin(person(a));}
        /** 0.4.427: the Sheriff sat down in the dirt, or lying on his back where he fell. */
        @Override protected void setupRotations(LiteraryActor a,PoseStack pose,float age,float yaw,float partial,float scale){
            super.setupRotations(a,pose,age,yaw,partial,scale);
            if(a.role()!=LiteraryActor.BORRASCA_SHERIFF)return;
            if(a.gesture()==BorrascaStory.KNELT)pose.translate(0,-.6F,0);
            else if(a.gesture()==BorrascaStory.FALLEN){pose.translate(0,.13F,0);pose.mulPose(Axis.XP.rotationDegrees(-90));}
        }
        @Override protected boolean shouldShowName(LiteraryActor a){return a.isCustomNameVisible()&&!a.isInvisible()&&super.shouldShowName(a);}
    }
    public static final class CastModel extends HumanoidModel<LiteraryActor>{
        private float brow,tilt,mouth;
        public CastModel(ModelPart root){super(root);hat.visible=false;}
        @Override public void renderToBuffer(PoseStack p,VertexConsumer out,int light,int overlay,int colour){
            super.renderToBuffer(p,out,light,overlay,colour);
            if(brow!=0||tilt!=0||mouth!=0)GhostsSkinMesh.standardExpression(head,p,out,light,overlay,colour,brow,tilt,mouth);
        }
        /**
         * 0.4.262 (owner: the workers "need proper walking animations"). The pose is eased between gestures as before, but the stride is laid
         * on afterwards, so the easing never damps a step: before, it smoothed the leg swing away to a shuffle and the cadence was twice a
         * walk's. Now a full stride takes about two blocks, the legs swing their whole way, and an arm a gesture is not using swings opposite
         * its leg.
         */
        @Override public void setupAnim(LiteraryActor a,float walk,float amount,float age,float yaw,float pitch){HumanoidMotion.reset(this);young=false;super.setupAnim(a,walk,amount,age,yaw,pitch);var motion=HumanoidMotion.sample(a,age-a.tickCount);body.xRot+=.015F*(float)Math.sin(age*.055F+a.getId());
            pose(a.gesture(),a.role()==LiteraryActor.BORRASCA_SHERIFF,age);HumanoidMotion.connect(this);RecentActorMotion.blend(a,age,this);
            if(a.gesture()!=BorrascaStory.CARRIED)stride(a.gesture(),motion.phase()*.45F,motion.amount());
            head.yRot=net.minecraft.util.Mth.clamp(head.yRot,-.95F,.95F);
            brow=tilt=mouth=0;
            int gesture=a.gesture();
            if(a.role()==LiteraryActor.BORRASCA_KIMBER){
                if(gesture==10){brow=.28F;mouth=.8F;}
                if(gesture==BorrascaStory.SCARED||gesture==BorrascaStory.CARRIED){brow=.85F;tilt=.45F;mouth=-.65F;}
            }else if(BorrascaStory.worker(a.role(),a.phase())&&
                    (gesture==BorrascaStory.WARNING||gesture==BorrascaStory.TAKING_HAND||gesture==BorrascaStory.CARRY)){
                brow=-.85F;tilt=-.62F;mouth=-.6F;
            }
        }
        /** Legs and free arms swinging with the distance actually walked; a sheltering crouch keeps its legs. */
        void stride(int gesture,float phase,float amount){
            if(gesture==BorrascaStory.KNELT||gesture==BorrascaStory.FALLEN)return;
            if(amount<=.01F)return;float c=net.minecraft.util.Mth.cos(phase),swing=.72F*amount;
            if(gesture!=5&&gesture!=BorrascaStory.CARRIED){rightLeg.xRot+=c*swing;leftLeg.xRot-=c*swing;}
            boolean right=gesture==0||gesture==8,left=gesture!=3&&gesture!=5&&gesture!=8&&gesture!=BorrascaStory.CARRY&&gesture!=10;
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
                default->{}
            }}
        /** Keep the approved child review poses; adults use the same full-length body as their live renderer. */
        public void preview(int person,float age,float stride){brow=tilt=mouth=0;HumanoidMotion.reset(this);young=person<2||person>=6;crouching=false;riding=false;attackTime=0;HumanoidMotion.gait(this,age*.22F,stride,false);head.yRot=.22F*(float)Math.sin(age*.02F);HumanoidMotion.connect(this);}
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
    public static final class MachineRenderer implements BlockEntityRenderer<BorrascaMachineBlock.Machine>{public MachineRenderer(BlockEntityRendererProvider.Context ignored){}
        @Override public void render(BorrascaMachineBlock.Machine e,float partial,PoseStack p,MultiBufferSource buffers,int light,int overlay){drawMachine(p,buffers,light,overlay,e.angle(partial));}
        @Override public boolean shouldRenderOffScreen(BorrascaMachineBlock.Machine e){return true;}
    }
    public static void drawMachine(PoseStack p,MultiBufferSource buffers,int light,int overlay,float angle){var out=buffers.getBuffer(RenderType.entityCutoutNoCull(ATLAS));p.pushPose();p.translate(.5,.7,.5);p.mulPose(Axis.XP.rotation(angle));for(int i=0;i<8;i++){p.pushPose();p.mulPose(Axis.XP.rotation((float)(i*Math.PI/4)));cube(p,out,-11,-7,-2,11,-5,2,13,light,overlay,0xffffffff);p.popPose();}cube(p,out,-13,-2,-2,13,2,2,14,light,overlay,0xffffffff);p.popPose();}
}
