package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.FallowSite;
import io.github.knaitoe.theoldesthouse.labyrinth.FallowStrike;
import io.github.knaitoe.theoldesthouse.network.FallowStrikePayload;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

/**
 * 0.4.674, Fallow's four minutes (docs/FALLOW_0_4_674.md), on its reader's screen. The server says how far along the strike is
 * (FallowStrikePayload); from that the client draws what goes up out of each pad: a long body climbing out of the hole on its fire,
 * slowly, then faster, leaning away north as it goes, and the smoke it leaves standing over the wheat, which spreads, drifts east on the
 * wind and thins over the minutes after. All of it is drawn with the haze held off, so it stands up out of the haze that hides the pads
 * themselves, and tinted toward the haze the farther it is. The ground shakes at each launch, and at the light; the light itself whites
 * out a reader caught in it and comes in round the cellar doors for one below. The shaking and the light follow the screen-effects
 * setting (Comfort).
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class FallowStrikeClient {
    private FallowStrikeClient(){}
    private static @Nullable BlockPos base;private static int since=-1;private static long at;
    public static void accept(FallowStrikePayload p){var mc=Minecraft.getInstance();if(mc.level==null)return;base=p.base();since=p.since();at=mc.level.getGameTime();}
    /** Ticks since the tone, now, for a reader at the station; -1 if there is no strike or no word of it lately. */
    static float now(float partial){
        var mc=Minecraft.getInstance();if(base==null||since<0||mc.level==null||FallowClient.at()==null)return -1;
        long age=mc.level.getGameTime()-at;if(age<0||age>100)return -1;
        return since+age+partial;
    }
    static boolean below(){var r=FallowClient.at();return r!=null&&FallowSite.inCellar(r);}

    // ------------------------------------------------------------------------------------------------ the flight
    /** How far the body has climbed, in blocks, after so many seconds: slow off the mark, then faster. */
    static double climbed(double seconds){return 2.2*seconds*seconds;}
    /** The body's length and its nose; how far below the mouth it starts. */
    static final double LENGTH=15,NOSE=3,RADIUS=.85,START=LENGTH+NOSE-1;
    /** Where the foot of the body is, having climbed so far from below the mouth: it leans north the higher it goes. */
    static Vec3 foot(Vec3 mouth,double climbed){double up=climbed-START;return mouth.add(0,up,-.0016*Math.max(0,up)*Math.max(0,up));}
    static Vec3 axis(double climbed){return new Vec3(0,1,-.0032*Math.max(0,climbed-START)).normalize();}
    /** The haze's own colour, and how much of it lies over something so far off. */
    static final float[] HAZE={204,184,138};
    static float haze(double distance){return (float)Mth.clamp((distance-24)/190,0,.62);}
    static int tint(float c,int channel,float k){return (int)Mth.lerp(k,c,HAZE[channel]);}
    /** The late sun, low in the west. */
    static final Vec3 SUN=new Vec3(-.75,.5,.12).normalize();

    @SubscribeEvent public static void draw(RenderLevelStageEvent e){
        if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS)return;
        float partial=e.getPartialTick().getGameTimeDeltaPartialTick(false);
        float t=now(partial);if(t<FallowStrike.LAUNCH_A||t>FallowStrike.LIGHT+2||below())return;
        var mc=Minecraft.getInstance();var camera=e.getCamera().getPosition();var pose=e.getPoseStack();var buffers=mc.renderBuffers().bufferSource();
        var left=new Vec3(e.getCamera().getLeftVector());var up=new Vec3(e.getCamera().getUpVector());
        pose.pushPose();pose.translate(-camera.x,-camera.y,-camera.z);
        for(int i=0;i<2;i++){
            var pad=i==0?FallowSite.PAD_A:FallowSite.PAD_B;int launch=i==0?FallowStrike.LAUNCH_A:FallowStrike.LAUNCH_B;if(t<launch)continue;
            var mouth=Vec3.atLowerCornerOf(base).add(FallowSite.shaft(pad)).add(0,-FallowSite.SHAFT_FLOOR-1,0);
            double seconds=(t-launch)/20D,climbed=climbed(seconds);
            if(climbed<620)body(pose,buffers.getBuffer(FallowRenderTypes.BODY),mouth,climbed,camera);
            if(seconds<26&&climbed<620)fire(pose,buffers.getBuffer(FallowRenderTypes.FIRE),mouth,climbed,seconds,left,up,camera);
            smoke(pose,buffers.getBuffer(FallowRenderTypes.SMOKE),mouth,seconds,climbed,i,left,up,camera);
        }
        pose.popPose();
        // Over the haze: drawn with the fog held off, and the fog put back after.
        float fog=RenderSystem.getShaderFogStart();RenderSystem.setShaderFogStart(Float.MAX_VALUE);
        buffers.endBatch(FallowRenderTypes.BODY);buffers.endBatch(FallowRenderTypes.SMOKE);buffers.endBatch(FallowRenderTypes.FIRE);
        RenderSystem.setShaderFogStart(fog);
    }
    /** The body: eight sides, a nose drawn to a point, two dark bands where its stages join; lit on the side to the sun. */
    static void body(PoseStack pose,VertexConsumer out,Vec3 mouth,double climbed,Vec3 camera){
        var a=axis(climbed);var foot=foot(mouth,climbed);var u=a.cross(new Vec3(1,0,0)).normalize();var v=a.cross(u).normalize();
        float k=haze(foot.add(a.scale(LENGTH/2)).distanceTo(camera));var last=pose.last();
        double[] rings={0,.6,4.6,5.2,9.6,10.2,LENGTH};
        for(int r=0;r+1<rings.length;r++){
            boolean band=r==1||r==3||r==5;float base=band?54:232;
            for(int s=0;s<8;s++){
                double a0=s*Math.PI/4,a1=(s+1)*Math.PI/4;var n=u.scale(Math.cos(a0+Math.PI/8)).add(v.scale(Math.sin(a0+Math.PI/8)));
                float light=(float)(.6+.4*Math.max(0,n.dot(SUN)));
                int cr=tint(base*light,0,k),cg=tint(base*light*(band?1:.995F),1,k),cb=tint(base*light*(band?1.04F:.96F),2,k);
                var p00=foot.add(a.scale(rings[r])).add(u.scale(Math.cos(a0)*RADIUS)).add(v.scale(Math.sin(a0)*RADIUS));
                var p01=foot.add(a.scale(rings[r])).add(u.scale(Math.cos(a1)*RADIUS)).add(v.scale(Math.sin(a1)*RADIUS));
                var p11=p01.add(a.scale(rings[r+1]-rings[r]));var p10=p00.add(a.scale(rings[r+1]-rings[r]));
                for(var p:new Vec3[]{p00,p01,p11,p10})out.addVertex(last,(float)p.x,(float)p.y,(float)p.z).setColor(cr,cg,cb,255);
            }
        }
        var tip=foot.add(a.scale(LENGTH+NOSE));
        for(int s=0;s<8;s++){
            double a0=s*Math.PI/4,a1=(s+1)*Math.PI/4;var n=u.scale(Math.cos(a0+Math.PI/8)).add(v.scale(Math.sin(a0+Math.PI/8))).add(a.scale(.3)).normalize();
            float light=(float)(.6+.4*Math.max(0,n.dot(SUN)));int c=(int)(214*light);
            var p0=foot.add(a.scale(LENGTH)).add(u.scale(Math.cos(a0)*RADIUS)).add(v.scale(Math.sin(a0)*RADIUS));
            var p1=foot.add(a.scale(LENGTH)).add(u.scale(Math.cos(a1)*RADIUS)).add(v.scale(Math.sin(a1)*RADIUS));
            for(var p:new Vec3[]{p0,p1,tip,tip})out.addVertex(last,(float)p.x,(float)p.y,(float)p.z).setColor(tint(c,0,k),tint(c,1,k),tint(c*.97F,2,k),255);
        }
    }
    /** The fire under it: a flickering cone of light, and a glare round the nozzle. */
    static void fire(PoseStack pose,VertexConsumer out,Vec3 mouth,double climbed,double seconds,Vec3 left,Vec3 up,Vec3 camera){
        var a=axis(climbed);var foot=foot(mouth,climbed);var u=a.cross(new Vec3(1,0,0)).normalize();var v=a.cross(u).normalize();var last=pose.last();
        double flicker=Math.sin(seconds*47)*.6+Math.sin(seconds*31)*.4;var tip=foot.subtract(a.scale(6+flicker+Math.min(6,seconds*.8)));
        for(int s=0;s<8;s++){
            double a0=s*Math.PI/4,a1=(s+1)*Math.PI/4;
            var p0=foot.add(u.scale(Math.cos(a0)*RADIUS*.95)).add(v.scale(Math.sin(a0)*RADIUS*.95));var p1=foot.add(u.scale(Math.cos(a1)*RADIUS*.95)).add(v.scale(Math.sin(a1)*RADIUS*.95));
            out.addVertex(last,(float)p0.x,(float)p0.y,(float)p0.z).setColor(255,238,190,220);out.addVertex(last,(float)p1.x,(float)p1.y,(float)p1.z).setColor(255,238,190,220);
            out.addVertex(last,(float)tip.x,(float)tip.y,(float)tip.z).setColor(255,120,30,0);out.addVertex(last,(float)tip.x,(float)tip.y,(float)tip.z).setColor(255,120,30,0);
        }
        puff(last,out,foot.subtract(a.scale(1)),2.6+flicker*.3,left,up,255,214,150,200);
    }
    /** The smoke it leaves: a column of puffs up the way it went, each spreading and drifting east from when it was left, thinning over the
     * minutes; and the cloud that rolls out across the pad from the mouth. */
    static void smoke(PoseStack pose,VertexConsumer out,Vec3 mouth,double seconds,double climbed,int which,Vec3 left,Vec3 up,Vec3 camera){
        var last=pose.last();
        double top=Math.min(climbed,560);
        for(double h=START+2;h<top;h+=1.6){
            double left0=Math.sqrt(h/2.2),age=seconds-left0;if(age<0)continue;
            int seed=(int)(h*7.3)+which*977;double jx=hash(seed)-.5,jz=hash(seed+1)-.5;
            var at=foot(mouth,h).add(age*.42+jx*Math.min(age,8)*.35,age*.04,jz*Math.min(age,8)*.35);
            double r=Math.min(10,1.1+.42*age+h*.004);float fade=(float)Mth.clamp(1-age/230,0,1);
            float k=haze(at.distanceTo(camera));int c=(int)(236-18*(hash(seed+2)));
            puff(last,out,at,r,left,up,tint(c,0,k),tint(c,1,k),tint(c-6,2,k),(int)(150*fade*(1-k*.4F)));
        }
        if(seconds<1)return;
        for(int i=0;i<14;i++){
            double angle=i*Math.PI*2/14+which,age=seconds-.2-hash(i+which*31)*1.5;if(age<0)continue;
            double d=Math.min(18,2+age*1.6),r=Math.min(9,2+age*.6);
            var at=mouth.add(Math.cos(angle)*d+age*.3,Math.min(6,1+age*.25),Math.sin(angle)*d);
            float fade=(float)Mth.clamp(1-age/200,0,1),k=haze(at.distanceTo(camera));
            puff(last,out,at,r,left,up,tint(226,0,k),tint(222,1,k),tint(212,2,k),(int)(170*fade*(1-k*.4F)));
        }
    }
    /** One soft round puff facing the eye: thick in the middle, nothing at its edge. */
    static void puff(PoseStack.Pose last,VertexConsumer out,Vec3 at,double r,Vec3 left,Vec3 up,int cr,int cg,int cb,int alpha){
        if(alpha<=2)return;
        Vec3[] rim=new Vec3[9];for(int s=0;s<=8;s++){double a=s*Math.PI/4;rim[s]=at.add(left.scale(Math.cos(a)*r)).add(up.scale(Math.sin(a)*r));}
        for(int q=0;q<4;q++){
            out.addVertex(last,(float)at.x,(float)at.y,(float)at.z).setColor(cr,cg,cb,alpha);
            for(int s=0;s<3;s++){var p=rim[q*2+s];out.addVertex(last,(float)p.x,(float)p.y,(float)p.z).setColor(cr,cg,cb,0);}
        }
    }
    static double hash(int n){n=(n<<13)^n;return ((n*(n*n*15731+789221)+1376312589)&0x7fffffff)/(double)0x7fffffff;}

    // ------------------------------------------------------------------------------------------------ the ground and the light
    @SubscribeEvent public static void shake(ViewportEvent.ComputeCameraAngles e){
        float t=now((float)e.getPartialTick());if(t<0)return;
        float roll=0,pitch=0;
        for(int launch:new int[]{FallowStrike.LAUNCH_A,FallowStrike.LAUNCH_B}){
            float k=t-launch;if(k<0||k>120)continue;float a=.55F*(1-k/120);
            roll+=Mth.sin(t*1.9F)*a;pitch+=Mth.sin(t*2.7F)*a*.6F;
        }
        float k=t-FallowStrike.LIGHT;
        if(k>=0){
            float a=below()?(k<170?3.2F*(1-k/170):0):(k<45?6F:0);
            roll+=Mth.sin(t*2.3F)*a;pitch+=Mth.sin(t*3.1F)*a*.7F;
        }
        float effect=Comfort.effects();
        if(roll!=0||pitch!=0){e.setRoll(e.getRoll()+roll*effect);e.setPitch(e.getPitch()+pitch*effect);}
    }
    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Layers {
        @SubscribeEvent public static void register(RegisterGuiLayersEvent e){e.registerAboveAll(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"fallow_light"),(g,delta)->{
            float t=now(delta.getGameTimeDeltaPartialTick(false));if(t<FallowStrike.LIGHT)return;
            float k=t-FallowStrike.LIGHT,effect=.35F+.65F*Comfort.effects();int w=g.guiWidth(),h=g.guiHeight();
            if(below()){
                // In round the doors, at every crack: a pale wash over the dark, gone again.
                if(k<34)g.fill(0,0,w,h,((int)(150*effect*(1-k/34))<<24)|0xFFF6E8);
                return;
            }
            // Out in it: nothing but the light, until the House takes the reader.
            float a=k<3?k/3:k<FallowStrike.OUT_FAINT+40?1:Math.max(0,1-(k-FallowStrike.OUT_FAINT-40)/20);
            g.fill(0,0,w,h,((int)(255*a*effect)<<24)|0xFFFBF2);
        });}
    }
}
