package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.PuzzleRegistry;
import io.github.knaitoe.theoldesthouse.labyrinth.Sphinx;
import io.github.knaitoe.theoldesthouse.labyrinth.SphinxFaceBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * 0.4.337, the Sphinx's face over the first crossing's hall (docs/SPHINX_FACE_0_4_337.md). Her face is one picture eighteen blocks across,
 * leaning out over the wall and looking down, with holes where her eyes are. Behind each hole: the white, an iris that turns smoothly
 * toward whoever is nearest (the same reader the server's look names), and a lid that comes down when she blinks. She rises out of the
 * dark over a second and a half when she starts to look, sliding up and forward from behind the wall as she brightens, and sinks the same
 * way.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class SphinxFaceRenderer implements BlockEntityRenderer<SphinxFaceBlock.Face> {
    static ResourceLocation tex(String name){return ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/sphinx/"+name+".png");}
    public static final ResourceLocation FACE=tex("face"),WHITE=tex("face_white"),IRIS=tex("face_iris"),LID=tex("face_lid");
    /** Ticks for her face to rise out of the dark, or sink back into it. */
    static final float RISE=30;
    /** How far she starts below and behind where she looks from, in blocks. */
    static final double SINK_DOWN=2.5,SINK_BACK=2.0;
    /** Half the size of each eye's white, its iris, and how far the iris turns, in blocks across and up the face. */
    static final double WHITE_W=1.85,WHITE_H=.82,IRIS_R=.8,TURN_X=.8,TURN_Y=.3;
    public SphinxFaceRenderer(BlockEntityRendererProvider.Context ignored){}
    @SubscribeEvent public static void register(EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(PuzzleRegistry.SPHINX_FACE_BE.get(),SphinxFaceRenderer::new);}

    @Override public void render(SphinxFaceBlock.Face face,float partial,PoseStack pose,MultiBufferSource out,int light,int overlay){
        var level=face.getLevel();if(level==null)return;
        boolean shown=face.getBlockState().getValue(SphinxFaceBlock.SHOWN);
        float now=level.getGameTime()+partial;
        float a=risen(face,shown,now);if(a<=.002F)return;
        // Positions are worked out relative to the room's base, then moved into this block's frame.
        var base=Vec3.atLowerCornerOf(face.getBlockPos().subtract(Sphinx.FACE));var corner=Vec3.atLowerCornerOf(face.getBlockPos());
        var right=new Vec3(1,0,0);var up=up();var n=Sphinx.faceNormal();
        Player near=nearest(level,base);
        for(int k=0;k<2;k++){var centre=Sphinx.faceAt(Sphinx.FACE_EYES[k][0],Sphinx.FACE_EYES[k][1]);gaze(face,k,near==null?null:near.getEyePosition(partial).subtract(base.add(centre)),right,up,n,now);}
        face.gazeAt=now;
        draw(pose,out,base.subtract(corner),a,face.gaze,blink(face,now),Math.max(light,LightTexture.pack(11,0)));
    }
    static Vec3 up(){return Sphinx.faceAt(0,0).subtract(Sphinx.faceAt(0,Sphinx.FACE_PX_H)).normalize();}
    /**
     * Draws her face with the room's base at {@code rel} in the pose's frame: risen {@code a} (0 to 1, sliding up and forward out of the dark
     * as it brightens), each iris turned by {@code gaze} (across and up the face, two numbers an eye) and her lids {@code closed} (0 to 1).
     */
    public static void draw(PoseStack pose,MultiBufferSource out,Vec3 rel,float a,float[] gaze,float closed,int lit){
        if(a<=.002F)return;
        var right=new Vec3(1,0,0);var up=up();var n=Sphinx.faceNormal();
        float e=1-a;rel=rel.add(up.scale(-SINK_DOWN*e)).add(n.scale(-SINK_BACK*e));
        int c=(int)(255*Math.pow(a,1.4));var m=pose.last();
        // Her face.
        var tl=Sphinx.faceAt(0,0).add(rel);var tr=Sphinx.faceAt(Sphinx.FACE_PX_W,0).add(rel);var br=Sphinx.faceAt(Sphinx.FACE_PX_W,Sphinx.FACE_PX_H).add(rel);var bl=Sphinx.faceAt(0,Sphinx.FACE_PX_H).add(rel);
        var buf=out.getBuffer(RenderType.entityCutoutNoCull(FACE));
        vertex(buf,m,tl,0,0,c,lit);vertex(buf,m,bl,0,1,c,lit);vertex(buf,m,br,1,1,c,lit);vertex(buf,m,tr,1,0,c,lit);
        // Her eyes, behind the holes: the whites, the irises, and the lids when she blinks.
        for(int k=0;k<2;k++){
            var at=Sphinx.faceAt(Sphinx.FACE_EYES[k][0],Sphinx.FACE_EYES[k][1]).add(rel);
            quad(out.getBuffer(RenderType.entityCutoutNoCull(WHITE)),m,at.add(n.scale(-.06)),right,up,WHITE_W,WHITE_H,0,0,1,1,c,lit);
            var iris=at.add(n.scale(-.04)).add(right.scale(gaze[k*2])).add(up.scale(gaze[k*2+1]));
            quad(out.getBuffer(RenderType.entityCutoutNoCull(IRIS)),m,iris,right,up,IRIS_R,IRIS_R,0,0,1,1,c,lit);
            if(closed>.01F){
                double h=WHITE_H*closed;var lid=at.add(n.scale(-.02)).add(up.scale(WHITE_H-h));
                quad(out.getBuffer(RenderType.entityCutoutNoCull(LID)),m,lid,right,up,WHITE_W,h,0,1-closed,1,1,c,lit);
            }
        }
    }
    /** How far her face has risen, 0 to 1, easing from wherever it was when she started or stopped looking. */
    static float risen(SphinxFaceBlock.Face face,boolean shown,float now){
        if(!face.seen){face.seen=true;face.lastShown=shown;face.changedAt=now-RISE;}
        if(shown!=face.lastShown){float cur=at(face.lastShown,face.changedAt,now);face.lastShown=shown;face.changedAt=shown?now-cur*RISE:now-(1-cur)*RISE;}
        float t=at(shown,face.changedAt,now);return t*t*(3-2*t);
    }
    private static float at(boolean shown,float since,float now){float t=Mth.clamp((now-since)/RISE,0,1);return shown?t:1-t;}
    /** The reader she looks at: whoever is nearest the middle of the hall below her, as the server reckons it. */
    static Player nearest(net.minecraft.world.level.Level level,Vec3 base){
        double cx=base.x+.5,cz=base.z+Sphinx.DOOR_Z+1+2.5;Player best=null;double d=Double.MAX_VALUE;
        for(var p:level.players()){
            if(p.isSpectator()||Math.abs(p.getY()-base.y)>=24)continue;double x=p.getX()-base.x,z=p.getZ()-base.z;
            boolean hall=z<Sphinx.HALL_Z0+1&&z>Sphinx.DOOR_Z-.2&&x>-9.5&&x<Sphinx.ONWARD_X;
            double dd=Math.pow(p.getX()-cx,2)+Math.pow(p.getZ()-cz,2)+(hall?0:10000);if(dd<d&&dd<10000+30*30){d=dd;best=p;}
        }
        return best;
    }
    /** Turns eye k toward the reader (relative to the eye), smoothly; with nobody there she looks down the middle of the hall. */
    static void gaze(SphinxFaceBlock.Face face,int k,Vec3 d,Vec3 right,Vec3 up,Vec3 n,float now){
        double gx=0,gy=-TURN_Y*.8;
        if(d!=null&&d.lengthSqr()>1e-4){double len=d.length();gx=Mth.clamp(d.dot(right)/len*1.6,-TURN_X,TURN_X);gy=Mth.clamp(d.dot(up)/len*1.3,-TURN_Y,TURN_Y);}
        float dt=face.gazeAt<0?100:Math.max(0,now-face.gazeAt);float f=(float)(1-Math.exp(-dt*.3));
        face.gaze[k*2]+=(float)((gx-face.gaze[k*2])*f);face.gaze[k*2+1]+=(float)((gy-face.gaze[k*2+1])*f);
    }
    /** Every seven or so seconds she blinks, both eyes together, quickly. */
    static float blink(SphinxFaceBlock.Face face,float now){
        long seed=face.getBlockPos().asLong();float phase=(float)((now+Math.floorMod(seed,97))%143);
        return phase<8?Mth.sin((float)Math.PI*phase/8):0;
    }
    private static void quad(VertexConsumer buf,PoseStack.Pose m,Vec3 centre,Vec3 right,Vec3 up,double hw,double hh,float u0,float v0,float u1,float v1,int c,int light){
        var tl=centre.add(right.scale(-hw)).add(up.scale(hh));var tr=centre.add(right.scale(hw)).add(up.scale(hh));
        var br=centre.add(right.scale(hw)).add(up.scale(-hh));var bl=centre.add(right.scale(-hw)).add(up.scale(-hh));
        vertex(buf,m,tl,u0,v0,c,light);vertex(buf,m,bl,u0,v1,c,light);vertex(buf,m,br,u1,v1,c,light);vertex(buf,m,tr,u1,v0,c,light);
    }
    private static void vertex(VertexConsumer buf,PoseStack.Pose m,Vec3 at,float u,float v,int c,int light){
        buf.addVertex(m,(float)at.x,(float)at.y,(float)at.z).setColor(c,c,c,255).setUv(u,v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(m,0,1,0);
    }
    @Override public boolean shouldRenderOffScreen(SphinxFaceBlock.Face face){return true;}
    @Override public int getViewDistance(){return 96;}
    @Override public AABB getRenderBoundingBox(SphinxFaceBlock.Face face){var p=face.getBlockPos();return new AABB(p.getX()-10,p.getY()-11,p.getZ()-3,p.getX()+11,p.getY()+10,p.getZ()+8);}
}
