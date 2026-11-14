package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryActor;
import io.github.knaitoe.theoldesthouse.labyrinth.PellPeople;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.568 (owner, of the Pell house: "tilly should also have visible hair growth. we have the texture overlay for that, right? mimicking
 * the sheet physics"). Tilly's skin paints her hair as she came, to her jaw ("Dad did it in one go. It always came out crooked."). June
 * brushes it every night: "There. It'll grow long here." And it does, faster than hair grows: a week in it is past her shoulders; on the
 * last night it is as long as Nessa's, down her back. Nessa, under the freezer's lid, has the same length, dark, with frost in it.
 * <ul>
 * <li>The length is the visit's (labyrinth.PellPeople.hairFor), in the look's marks; a reader playing Tilly has it in their look
 * (network.PellTillyPayload).</li>
 * <li>It is drawn over the cast model from the body, so it hangs as she stands whichever way she turns her head: four strands down her
 * back from under her nape, and a lock over each shoulder onto her chest, each in a few links, from its own sheet
 * (tools/draw_pell_hair.py, which reads the lengths here), ragged at the ends.</li>
 * <li>It moves as Tess's hair moves in the lake (LakeHairDynamics), but on land: each link on a spring, swung back as she walks and
 * further as she runs, aside as she steps sideways or turns, lifted as she drops and pressed as she rises, bobbing with her stride, and
 * stirring a little while she stands. The locks never swing into her chest.</li>
 * <li>Lying down (asleep in Nessa's bed, or Nessa under the lid) it lies spread on the pillow above her head, and does not move.</li>
 * </ul>
 */
public final class PellHair {
    private PellHair(){}
    public static final ModelLayerLocation LAYER=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"pell_hair"),"main");
    /** How long it hangs, in the model's pixels, by length (1 a week in, 2 the last night): down her back strand by strand from her right
     *  to her left, and over each shoulder (right, left). A little uneven, as hair grown out is. */
    static final float[][] BACK={{},{4.2F,4.8F,4.5F,3.9F},{10.2F,10.9F,11.2F,10.4F}},FRONT={{},{2.4F,2.2F},{6.2F,5.8F}};
    static final int STRANDS=4,LINKS=3,LOCKS=2,LOCK_LINKS=2;
    /** A strand's width and thickness, and a lock's. */
    static final float STRAND_W=2,STRAND_D=.8F,LOCK_W=2.2F,LOCK_D=.6F;
    /** Where the strands hang from (behind her neck, a pixel up under her head), and the locks (over her collarbones), from the body. */
    static final float BACK_Y=-1,BACK_Z=3.4F,LOCK_X=2.9F,LOCK_Y=-.5F,LOCK_Z=-2.65F;
    /** Their sheets' places, in the 64 by 32 sheet: a strand's link, and a lock's. */
    static int backU(int length,int strand){return (length-1)*32+strand*8;}
    static int backV(int link){return link*8;}
    static int lockU(int length,int lock,int link){return (length-1)*32+lock*16+link*8;}
    static final int LOCK_V=24;
    private static final ResourceLocation TILLY=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/pell_hair_tilly.png"),
        NESSA=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/pell_hair_nessa.png");

    public static LayerDefinition layer(){
        var mesh=new MeshDefinition();var root=mesh.getRoot();
        for(int s=1;s<=2;s++){
            var length=root.addOrReplaceChild("length_"+s,CubeListBuilder.create(),PartPose.ZERO);
            for(int k=0;k<STRANDS;k++){
                float link=BACK[s][k]/LINKS;PartDefinition parent=length;
                for(int j=0;j<LINKS;j++)parent=parent.addOrReplaceChild("back_"+k+"_"+j,CubeListBuilder.create().texOffs(backU(s,k),backV(j))
                    .addBox(-STRAND_W/2,0,-STRAND_D/2,STRAND_W,link,STRAND_D),j==0?PartPose.offset(-3+2*k,BACK_Y,BACK_Z):PartPose.offset(0,link,0));
            }
            for(int m=0;m<LOCKS;m++){
                float link=FRONT[s][m]/LOCK_LINKS;PartDefinition parent=length;
                for(int j=0;j<LOCK_LINKS;j++)parent=parent.addOrReplaceChild("lock_"+m+"_"+j,CubeListBuilder.create().texOffs(lockU(s,m,j),LOCK_V)
                    .addBox(-LOCK_W/2,0,-LOCK_D/2,LOCK_W,link,LOCK_D),j==0?PartPose.offset(m==0?-LOCK_X:LOCK_X,LOCK_Y,LOCK_Z):PartPose.offset(0,link,0));
            }
        }
        return LayerDefinition.create(mesh,64,32);
    }

    /** How long a cast member's hair has grown past what their skin paints: Tilly's by the visit, Nessa's always the longest; nobody
     *  else's. */
    public static int length(LiteraryActor a){
        if(a.role()!=LiteraryActor.STRANGER)return 0;
        if(a.phase()==PellPeople.FIRST+PellPeople.NESSA)return PellPeople.HAIR_LONGEST;
        return a.phase()==PellPeople.FIRST+PellPeople.TILLY?PellPeople.hair(a.marks()):0;
    }
    /** Lying down: asleep, or still under the freezer's lid. */
    static boolean lying(LiteraryActor a){return a.gesture()==PellPeople.ASLEEP||a.gesture()==PellPeople.STILL;}

    /** One person's hair as it moves: each link's swing back (pitch) and aside (roll), on its spring, once a tick. */
    static final class Sway {
        static final int N=STRANDS*LINKS+LOCKS*LOCK_LINKS;
        final float[] pitch=new float[N],roll=new float[N],oldPitch=new float[N],oldRoll=new float[N],vp=new float[N],vr=new float[N];
        int tick=Integer.MIN_VALUE;Vec3 at;float yaw;boolean lay;
        /** The links of strand {@code k}, then of lock {@code m}. */
        static int back(int k,int j){return k*LINKS+j;}
        static int lock(int m,int j){return STRANDS*LINKS+m*LOCK_LINKS+j;}
        /** How much of a swing each link of a strand takes, root first, and how stiffly it follows (the ends lag). */
        static final float[] SHARE={.55F,.3F,.15F},STIFF={.22F,.16F,.12F},LOCK_SHARE={.7F,.3F},LOCK_STIFF={.2F,.14F};
        static final float TUCK=-.09F,LOCK_REST=-.06F,DAMP=.7F;

        void update(LiteraryActor a){
            int now=a.tickCount;if(now==tick)return;
            boolean flat=lying(a);
            if(flat){lie();tick=now;at=a.position();yaw=a.yBodyRot;return;}
            if(lay)hang();
            int steps=tick==Integer.MIN_VALUE?0:Math.min(4,Math.max(1,now-tick));
            var d=at==null||steps==0?Vec3.ZERO:a.position().subtract(at).scale(1F/steps);
            // Put somewhere by the game rather than walked there: no swing for it.
            if(d.lengthSqr()>1)d=Vec3.ZERO;
            float turn=at==null||steps==0?0:Mth.wrapDegrees(a.yBodyRot-yaw)/steps;
            double r=Math.toRadians(a.yBodyRot);
            float forward=(float)(-d.x*Math.sin(r)+d.z*Math.cos(r)),side=(float)(d.x*Math.cos(r)+d.z*Math.sin(r)),rise=(float)d.y;
            float stride=Mth.sin(a.walkAnimation.position()*.6662F*2)*Math.min(1,a.walkAnimation.speed());
            for(int n=0;n<Math.max(1,steps);n++)step(forward,side,rise,turn,stride,now-Math.max(1,steps)+n+1);
            tick=now;at=a.position();yaw=a.yBodyRot;
        }
        /** One tick: back as she goes forward, aside as she goes sideways or turns (the ends lag the other way), lifted as she drops. */
        void step(float forward,float side,float rise,float turn,float stride,int t){
            System.arraycopy(pitch,0,oldPitch,0,N);System.arraycopy(roll,0,oldRoll,0,N);
            float back=Mth.clamp(forward*3.2F,-.15F,.8F),lift=Mth.clamp(-rise*2.5F,-.2F,.5F),bob=stride*.06F;
            float aside=Mth.clamp(side*3-turn*.012F,-.35F,.35F);
            for(int k=0;k<STRANDS;k++){
                float idlePitch=Mth.sin(t*.05F+k*1.7F)*.02F,idleRoll=Mth.cos(t*.043F+k*2.3F)*.015F,fan=(k-1.5F)*.025F;
                for(int j=0;j<LINKS;j++)spring(back(k,j),(j==0?TUCK:0)+SHARE[j]*(back+lift+bob+idlePitch),fan*(j==0?1:0)+SHARE[j]*(aside+idleRoll),STIFF[j]);
            }
            for(int m=0;m<LOCKS;m++){
                float idleRoll=Mth.cos(t*.047F+m*1.9F)*.012F;
                for(int j=0;j<LOCK_LINKS;j++){
                    int i=lock(m,j);
                    spring(i,(j==0?LOCK_REST:0)-LOCK_SHARE[j]*(Math.abs(bob)*1.5F+Math.max(0,lift)*.3F),LOCK_SHARE[j]*(aside*.6F+idleRoll),LOCK_STIFF[j]);
                }
                // Never into her chest: the lock as a whole leans out from it, or hangs straight.
                float whole=pitch[lock(m,0)]+pitch[lock(m,1)];
                if(whole>.02F){pitch[lock(m,1)]-=whole-.02F;vp[lock(m,1)]=Math.min(0,vp[lock(m,1)]);}
            }
        }
        private void spring(int i,float toPitch,float toRoll,float stiff){
            vp[i]=vp[i]*DAMP+(toPitch-pitch[i])*stiff;pitch[i]+=vp[i];
            vr[i]=vr[i]*DAMP+(toRoll-roll[i])*stiff;roll[i]+=vr[i];
        }
        /** Lying on her back: the strands up over the pillow past her head, opening out a little toward their ends; the locks down her
         *  front. */
        void lie(){
            for(int k=0;k<STRANDS;k++)for(int j=0;j<LINKS;j++){int i=back(k,j);pitch[i]=j==0?Mth.PI:0;roll[i]=(k-1.5F)*(j==0?.03F:.07F);}
            for(int m=0;m<LOCKS;m++)for(int j=0;j<LOCK_LINKS;j++){int i=lock(m,j);pitch[i]=j==0?LOCK_REST:0;roll[i]=0;}
            System.arraycopy(pitch,0,oldPitch,0,N);System.arraycopy(roll,0,oldRoll,0,N);
            java.util.Arrays.fill(vp,0);java.util.Arrays.fill(vr,0);lay=true;
        }
        /** Hanging still, as at rest: from lying down, or when first seen. */
        void hang(){
            for(int k=0;k<STRANDS;k++)for(int j=0;j<LINKS;j++){int i=back(k,j);pitch[i]=oldPitch[i]=j==0?TUCK:0;roll[i]=oldRoll[i]=0;}
            for(int m=0;m<LOCKS;m++)for(int j=0;j<LOCK_LINKS;j++){int i=lock(m,j);pitch[i]=oldPitch[i]=j==0?LOCK_REST:0;roll[i]=oldRoll[i]=0;}
            java.util.Arrays.fill(vp,0);java.util.Arrays.fill(vr,0);lay=false;
        }
        float pitch(int i,float partial){return Mth.lerp(partial,oldPitch[i],pitch[i]);}
        float roll(int i,float partial){return Mth.lerp(partial,oldRoll[i],roll[i]);}
    }
    private static final Map<LiteraryActor,Sway> SWAY=new WeakHashMap<>();

    /** The hair as baked: each length's strands and locks. */
    static final class Parts {
        final ModelPart[] lengths=new ModelPart[3];
        final ModelPart[][][] back=new ModelPart[3][STRANDS][LINKS],locks=new ModelPart[3][LOCKS][LOCK_LINKS];
        Parts(ModelPart root){
            for(int s=1;s<=2;s++){
                lengths[s]=root.getChild("length_"+s);
                for(int k=0;k<STRANDS;k++){var part=lengths[s];for(int j=0;j<LINKS;j++){part=part.getChild("back_"+k+"_"+j);back[s][k][j]=part;}}
                for(int m=0;m<LOCKS;m++){var part=lengths[s];for(int j=0;j<LOCK_LINKS;j++){part=part.getChild("lock_"+m+"_"+j);locks[s][m][j]=part;}}
            }
        }
        void pose(int s,Sway sway,float partial){
            for(int k=0;k<STRANDS;k++)for(int j=0;j<LINKS;j++){var part=back[s][k][j];int i=Sway.back(k,j);part.xRot=sway.pitch(i,partial);part.yRot=0;part.zRot=sway.roll(i,partial);}
            for(int m=0;m<LOCKS;m++)for(int j=0;j<LOCK_LINKS;j++){var part=locks[s][m][j];int i=Sway.lock(m,j);part.xRot=sway.pitch(i,partial);part.yRot=0;part.zRot=sway.roll(i,partial);}
        }
        /** From the body of {@code model}, as it is posed. */
        void draw(int s,LiteraryRenderers.CastModel model,ResourceLocation sheet,float alpha,PoseStack p,MultiBufferSource out,int light,int overlay){
            p.pushPose();model.body.translateAndRotate(p);
            lengths[s].render(p,out.getBuffer(alpha<1?RenderType.entityTranslucent(sheet):RenderType.entityCutoutNoCull(sheet)),light,overlay,FastColor.ARGB32.color(Math.round(alpha*255),255,255,255));
            p.popPose();
        }
    }

    /** Drawn over the cast model, from its body. */
    public static final class Layer extends RenderLayer<LiteraryActor,LiteraryRenderers.CastModel>{
        private final Parts parts;
        public Layer(RenderLayerParent<LiteraryActor,LiteraryRenderers.CastModel> parent,EntityRendererProvider.Context c){super(parent);parts=new Parts(c.bakeLayer(LAYER));}
        @Override public void render(PoseStack p,MultiBufferSource out,int light,LiteraryActor a,float limb,float amount,float partial,float age,float yaw,float pitch){
            int s=length(a);if(s<=0||a.isInvisible())return;
            var sway=SWAY.computeIfAbsent(a,key->{var fresh=new Sway();fresh.hang();return fresh;});
            sway.update(a);
            parts.pose(s,sway,partial);
            var model=getParentModel();
            parts.draw(s,model,a.phase()==PellPeople.FIRST+PellPeople.NESSA?NESSA:TILLY,Mth.clamp(model.alpha,0,1),p,out,light,LivingEntityRenderer.getOverlayCoords(a,0));
        }
    }

    /**
     * For proofs, drawn without an actor: the hair of length {@code s} on {@code model} as it is posed, Tilly's or Nessa's. At rest; or
     * lying spread; or swung as by {@code forward} and {@code side} blocks a tick and {@code turn} degrees a tick, kept up for two seconds.
     */
    public static void draw(LiteraryRenderers.CastModel model,int s,boolean nessa,boolean flat,float forward,float side,float turn,PoseStack p,MultiBufferSource out,int light){
        var parts=new Parts(net.minecraft.client.Minecraft.getInstance().getEntityModels().bakeLayer(LAYER));
        var sway=new Sway();
        if(flat)sway.lie();
        else{sway.hang();for(int t=0;t<40;t++)sway.step(forward,side,0,turn,0,t);}
        parts.pose(s,sway,1);
        parts.draw(s,model,nessa?NESSA:TILLY,1,p,out,light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
    }
}
