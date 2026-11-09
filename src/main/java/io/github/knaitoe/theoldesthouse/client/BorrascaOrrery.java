package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

/**
 * 0.4.439, the owner: "Well the orrery needs to be rebuilt too. Maybe redesign them both". The machine at the plant as the story has it: "a
 * large silver, conically shaped machine. A conveyer belt fed into it", and the children's Shiny Gentleman, "whirling, twirling", that "lit
 * up the valley". Until now it was a drum turning out of sight inside a polished box. Now a silver cone stands on that box (the housing
 * round the drum, whose walls are real blocks) and rises ten blocks to a tip bent over like his hat. Three arms turn round it at three
 * heights and speeds, each carrying a polished ball, the middle one a smaller ball turning round its own; two tilted hoops go round it
 * slower, the other way. A streak of light goes round the cone, its bands of vents glow, and its balls shine while it runs; when the breaker
 * is pulled, all of it slows to a stop with the drum. The belt runs into a mouth at its foot: a lip over the slot with steel teeth hanging
 * from it, and dark behind.
 * <p>
 * The children make a man of it, the Shiny Gentleman in the pines, with its bent tip for his hat (BorrascaGentlemanModel); the owner:
 * "the monster is the child's imagination seeing him as an adult is that trauma made real". On the last night, for two seconds, the grown
 * reader sees him here in its place ({@link #devour}).
 * <p>
 * Everything here is drawn by the machine block's renderer; its materials are on the right half of his sheet (tools/draw_shiny_gentleman.py's
 * TILES). None of it is solid: the cone stands above the housing's walls and the arms and hoops turn above head height.
 */
public final class BorrascaOrrery {
    private BorrascaOrrery(){}
    /** The cone, in blocks above the machine block's floor (its corner the origin): from on top of the housing's walls to its tip. */
    public static final float BASE=5,TOP=15,R0=3.2F,R1=.22F;
    static final int SIDES=16;
    /** The slot: the south face of the housing's south wall, over the belt (blocks from the machine block's corner), and the belt's top. */
    public static final float SLOT_Z=3,BELT_TOP=9/16F;
    record Arm(float y,float reach,float ball,float speed,float phase){}
    /** Three arms, low and long and slow to high and short and quick, the middle one turning the other way. */
    static final Arm[] ARMS={new Arm(6.6F,4.6F,.55F,.42F,0),new Arm(10.4F,3.8F,.45F,-.66F,2.1F),new Arm(13.4F,2.2F,.35F,1.1F,4.4F)};
    record Hoop(float y,float radius,float tilt,float speed,int segments){}
    /** Two hoops, tilted against each other, turning slower. */
    static final Hoop[] HOOPS={new Hoop(8.6F,4F,.31F,.21F,28),new Hoop(11.9F,2.6F,-.38F,-.33F,18)};
    /** The cone's tiers, a block each from the bottom: plain plate, a riveted band, a band of vents. */
    private static final int[] TIERS={0,1,0,2,0,1,0,2,0,1};
    // Places on the shared atlas, in pixels (tools/draw_shiny_gentleman.py's TILES and PARTS).
    private static final int[][] TILE={{64,0},{88,0},{64,16}};
    private static final int TILE_W=24,TILE_H=16;
    private static final int[] SPHERE={88,16,16},TOOTH={116,16},THROAT={116,24};
    private static final int ROD_U=104,ROD_V=16;
    private static final float W=BorrascaGentlemanModel.ATLAS_W,H=BorrascaGentlemanModel.ATLAS_H;

    static RenderType type(){return RenderType.entityCutoutNoCull(BorrascaGentlemanModel.TEXTURE);}
    /** The cone's radius at a height. */
    static float radius(float y){return R0+(R1-R0)*(y-BASE)/(TOP-BASE);}
    /** The world's light, or brighter for what glows while it runs: {@code power} from 0 (stopped) to 1. */
    static int glow(int light,float power){return LightTexture.pack(Math.max(LightTexture.block(light),Math.round(15*Mth.clamp(power,0,1))),LightTexture.sky(light));}

    /**
     * The machine above its housing, the pose's origin at the machine block's corner: the cone, its hoops and arms turned by {@code run}
     * (radians the drum has turned, never wrapped), {@code power} how fast it runs now (0 stopped, 1 full), at the world's {@code light}.
     */
    public static void draw(PoseStack p,MultiBufferSource buffers,int light,double run,float power,float time){
        var out=buffers.getBuffer(type());int bright=glow(light,power);
        p.pushPose();p.translate(.5,0,.5);
        cone(p,out,light,bright,time);
        tip(p,out,light);
        for(var a:ARMS)arm(p,out,a,run,light,bright);
        for(var h:HOOPS)hoop(p,out,h,run,light);
        p.popPose();
    }
    private static void cone(PoseStack p,VertexConsumer out,int light,int bright,float time){
        float step=Mth.TWO_PI/SIDES;double sweep=time*.02;
        for(int tier=0;tier<TIERS.length;tier++){
            float y0=BASE+tier,y1=Math.min(TOP,y0+1),r0=radius(y0),r1=radius(y1);int[] t=TILE[TIERS[tier]];int l=TIERS[tier]==2?bright:light;
            // Each face of the cone takes as much of its tile as it is wide, so its pixels stay the size of a block's.
            float half0=16*r0*Mth.sin(step/2),half1=16*r1*Mth.sin(step/2),u=t[0]+TILE_W/2F;
            for(int j=0;j<SIDES;j++){
                float a0=j*step,a1=a0+step,am=a0+step/2;
                // A streak of light goes slowly round it.
                float shine=(float)Math.pow(Math.max(0,Math.cos(am-sweep)),12);int c=Math.round(205+50*shine);int colour=FastColor.ARGB32.color(255,c,c,c);
                quad(p,out,new float[][]{{r0*Mth.cos(a0),y0,r0*Mth.sin(a0)},{r0*Mth.cos(a1),y0,r0*Mth.sin(a1)},{r1*Mth.cos(a1),y1,r1*Mth.sin(a1)},{r1*Mth.cos(a0),y1,r1*Mth.sin(a0)}},
                        new float[][]{{u-half0,t[1]+TILE_H},{u+half0,t[1]+TILE_H},{u+half1,t[1]},{u-half1,t[1]}},Mth.cos(am),.3F,Mth.sin(am),l,colour);
            }
        }
        // Its underside, where it overhangs the housing.
        float half=16*R0*Mth.sin(step/2),u=TILE[0][0]+TILE_W/2F,v=TILE[0][1];
        for(int j=0;j<SIDES;j++){float a0=j*step,a1=a0+step;
            quad(p,out,new float[][]{{0,BASE,0},{R0*Mth.cos(a1),BASE,R0*Mth.sin(a1)},{R0*Mth.cos(a0),BASE,R0*Mth.sin(a0)},{0,BASE,0}},
                    new float[][]{{u,v},{u+half,v+TILE_H},{u-half,v+TILE_H},{u,v}},0,-1,0,light,0xff9a9a9a);}
    }
    /** The tip, bent over toward the belt like the crown of his hat. */
    private static void tip(PoseStack p,VertexConsumer out,int light){
        Vec3[] at={new Vec3(0,TOP-.4,0),new Vec3(0,TOP+.55,.12),new Vec3(0,TOP+1.15,.55),new Vec3(0,TOP+1.3,1.15)};float[] thick={.42F,.34F,.26F};
        for(int i=0;i<thick.length;i++)rod(p,out,at[i],at[i+1],thick[i],light);
    }
    private static void arm(PoseStack p,VertexConsumer out,Arm a,double run,int light,int bright){
        float angle=(float)(run*a.speed()+a.phase()),r=radius(a.y());var dir=new Vec3(Mth.cos(angle),0,Mth.sin(angle));
        band(p,out,a.y()-.2F,a.y()+.2F,r+.07F,light);
        var to=new Vec3(dir.x*a.reach(),a.y(),dir.z*a.reach());
        rod(p,out,new Vec3(dir.x*r,a.y(),dir.z*r),to,.19F,light);ball(p,out,to,a.ball(),bright);
        // A counterweight the other way.
        var back=new Vec3(-dir.x*(r+a.reach()*.3),a.y(),-dir.z*(r+a.reach()*.3));
        rod(p,out,new Vec3(-dir.x*r,a.y(),-dir.z*r),back,.15F,light);ball(p,out,back,a.ball()*.5F,bright);
        // The middle one carries a ball of its own, turning round it quicker.
        if(a==ARMS[1]){float m=(float)(run*2.4);var moon=to.add(Mth.cos(m)*.95,0,Mth.sin(m)*.95);rod(p,out,to,moon,.1F,light);ball(p,out,moon,.2F,bright);}
    }
    private static void hoop(PoseStack p,VertexConsumer out,Hoop h,double run,int light){
        p.pushPose();p.translate(0,h.y(),0);p.mulPose(Axis.YP.rotation((float)(run*h.speed())));p.mulPose(Axis.XP.rotation(h.tilt()));
        for(int k=0;k<h.segments();k++){float a0=k*Mth.TWO_PI/h.segments(),a1=(k+1)*Mth.TWO_PI/h.segments();
            rod(p,out,new Vec3(h.radius()*Mth.cos(a0),0,h.radius()*Mth.sin(a0)),new Vec3(h.radius()*Mth.cos(a1),0,h.radius()*Mth.sin(a1)),.19F,light);}
        p.popPose();
    }
    /** A collar round the cone. */
    private static void band(PoseStack p,VertexConsumer out,float y0,float y1,float r,int light){
        float step=Mth.TWO_PI/SIDES,half=16*r*Mth.sin(step/2),u=TILE[1][0]+TILE_W/2F,v=TILE[1][1]+5;
        for(int j=0;j<SIDES;j++){float a0=j*step,a1=a0+step,am=a0+step/2;
            quad(p,out,new float[][]{{r*Mth.cos(a0),y0,r*Mth.sin(a0)},{r*Mth.cos(a1),y0,r*Mth.sin(a1)},{r*Mth.cos(a1),y1,r*Mth.sin(a1)},{r*Mth.cos(a0),y1,r*Mth.sin(a0)}},
                    new float[][]{{u-half,v+6},{u+half,v+6},{u+half,v},{u-half,v}},Mth.cos(am),0,Mth.sin(am),light,-1);}
    }

    /**
     * The mouth the belt runs into, the pose's origin at the machine block's corner: a lip along the top of the slot with steel teeth hanging
     * from it, and the dark of the throat behind (her body is cut off at the slot, so nothing past it shows).
     */
    public static void mouth(PoseStack p,MultiBufferSource buffers,int light){
        var out=buffers.getBuffer(type());
        quad(p,out,new float[][]{{-.05F,BELT_TOP,SLOT_Z-.02F},{1.05F,BELT_TOP,SLOT_Z-.02F},{1.05F,1,SLOT_Z-.02F},{-.05F,1,SLOT_Z-.02F}},
                new float[][]{{THROAT[0],THROAT[1]+8},{THROAT[0]+8,THROAT[1]+8},{THROAT[0]+8,THROAT[1]},{THROAT[0],THROAT[1]}},0,0,1,LightTexture.pack(0,0),-1);
        rod(p,out,new Vec3(-.2,1,SLOT_Z+.06),new Vec3(1.2,1,SLOT_Z+.06),.2F,light);
        for(int i=0;i<6;i++){float x=.04F+i*.16F,z=SLOT_Z+.1F;
            quad(p,out,new float[][]{{x,.68F,z},{x+.14F,.68F,z},{x+.14F,.98F,z},{x,.98F,z}},new float[][]{{TOOTH[0],TOOTH[1]+8},{TOOTH[0]+4,TOOTH[1]+8},{TOOTH[0]+4,TOOTH[1]},{TOOTH[0],TOOTH[1]}},0,0,1,light,-1);}
    }

    /**
     * 0.4.439: the two seconds (client.BorrascaFeedingClient). The pose's origin at the machine block's corner, {@code slot} where her slot is
     * from there, {@code age} the ticks since he came: the cone is gone (its renderer draws this instead) and he stands in its housing,
     * there at once, held, and gone in a few frames, his face opening wider as he goes on.
     */
    public static void devour(PoseStack p,MultiBufferSource buffers,Vec3 slot,float age,float time,ResourceLocation flesh){
        float alpha=age<2?age/2:age>BorrascaFeedingClient.FLASH-4?(BorrascaFeedingClient.FLASH-age)/4:1;float open=.55F+.25F*Mth.clamp(age/12F,0,1)+.06F*Mth.sin(age*1.7F);
        p.pushPose();p.translate(slot.x,slot.y,slot.z);
        // He stands on the housing's floor in its middle; the frame is the slot's, +z the way her head lies (south, along the belt).
        BorrascaGentlemanModel.renderFeeding(p,buffers,alpha,time,open,flesh,new Vec3(.5-slot.x,-slot.y,.5-slot.z));
        p.popPose();
    }

    /** A rod {@code t} thick from {@code a} to {@code b}, a block of the rod's texture at a time. */
    static void rod(PoseStack p,VertexConsumer out,Vec3 a,Vec3 b,float t,int light){
        var d=b.subtract(a);double length=d.length();if(length<1e-4)return;
        p.pushPose();p.translate(a.x,a.y,a.z);p.mulPose(new Quaternionf().rotationTo(0,1,0,(float)(d.x/length),(float)(d.y/length),(float)(d.z/length)));
        float h=t/2;
        for(double s=0;s<length-1e-4;s+=1){float y0=(float)s,y1=(float)Math.min(length,s+1),v1=ROD_V+3+16*(y1-y0);
            float[][] sides={{ROD_U,ROD_V+3,ROD_U+3,v1},{ROD_U+3,ROD_V+3,ROD_U+6,v1},{ROD_U+6,ROD_V+3,ROD_U+9,v1},{ROD_U+9,ROD_V+3,ROD_U+12,v1}};
            float[] cap={ROD_U+3,ROD_V,ROD_U+6,ROD_V+3};
            box(p,out,-h,y0,-h,h,y1,h,new float[][]{sides[0],sides[2],cap,cap,sides[1],sides[3]},light,-1);}
        p.popPose();
    }
    /** A polished ball of radius {@code r} at {@code at}: three boxes crossed, so it reads round at a block's pixels. */
    static void ball(PoseStack p,VertexConsumer out,Vec3 at,float r,int light){
        p.pushPose();p.translate(at.x,at.y,at.z);float s=r*.62F;
        for(int axis=0;axis<3;axis++){float x=axis==0?r:s,y=axis==1?r:s,z=axis==2?r:s;
            box(p,out,-x,-y,-z,x,y,z,new float[][]{sphere(z,y,r),sphere(z,y,r),sphere(x,z,r),sphere(x,z,r),sphere(x,y,r),sphere(x,y,r)},light,-1);}
        p.popPose();
    }
    /** The middle of the sphere tile, as much of it as a face {@code w} by {@code h} (half-sizes) of a ball of radius {@code r} covers. */
    private static float[] sphere(float w,float h,float r){float c=SPHERE[2]/2F,hu=c*w/r,hv=c*h/r;return new float[]{SPHERE[0]+c-hu,SPHERE[1]+c-hv,SPHERE[0]+c+hu,SPHERE[1]+c+hv};}
    /** A box; each face's rectangle of the atlas in pixels {u0,v0,u1,v1}, in the order -x, +x, -y, +y, -z, +z. */
    private static void box(PoseStack p,VertexConsumer out,float x0,float y0,float z0,float x1,float y1,float z1,float[][] uv,int light,int colour){
        face(p,out,new float[][]{{x0,y0,z0},{x0,y0,z1},{x0,y1,z1},{x0,y1,z0}},uv[0],-1,0,0,light,colour);
        face(p,out,new float[][]{{x1,y0,z1},{x1,y0,z0},{x1,y1,z0},{x1,y1,z1}},uv[1],1,0,0,light,colour);
        face(p,out,new float[][]{{x0,y0,z1},{x1,y0,z1},{x1,y0,z0},{x0,y0,z0}},uv[2],0,-1,0,light,colour);
        face(p,out,new float[][]{{x0,y1,z0},{x1,y1,z0},{x1,y1,z1},{x0,y1,z1}},uv[3],0,1,0,light,colour);
        face(p,out,new float[][]{{x1,y0,z0},{x0,y0,z0},{x0,y1,z0},{x1,y1,z0}},uv[4],0,0,-1,light,colour);
        face(p,out,new float[][]{{x0,y0,z1},{x1,y0,z1},{x1,y1,z1},{x0,y1,z1}},uv[5],0,0,1,light,colour);
    }
    private static void face(PoseStack p,VertexConsumer out,float[][] q,float[] uv,float nx,float ny,float nz,int light,int colour){
        quad(p,out,q,new float[][]{{uv[0],uv[3]},{uv[2],uv[3]},{uv[2],uv[1]},{uv[0],uv[1]}},nx,ny,nz,light,colour);
    }
    /** Four corners in blocks with their places on the atlas in pixels, bottom left, bottom right, top right, top left. */
    private static void quad(PoseStack p,VertexConsumer out,float[][] q,float[][] uv,float nx,float ny,float nz,int light,int colour){
        var e=p.last();
        for(int i=0;i<4;i++)out.addVertex(e,q[i][0],q[i][1],q[i][2]).setColor(colour).setUv(uv[i][0]/W,uv[i][1]/H).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(e,nx,ny,nz);
    }
}
