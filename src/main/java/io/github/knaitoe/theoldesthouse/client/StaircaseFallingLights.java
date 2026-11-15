package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.StaircaseFallingLight;
import io.github.knaitoe.theoldesthouse.network.StairLightFallPayload;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * 0.4.594: a light let fall into the great staircase's well, as each reader sees it (docs/STAIR_FALLING_LIGHT_0_4_594.md).
 *
 * <p>On the stair a reader sees about twelve blocks, and the well is thousands deep. A light that has been let fall is drawn here
 * as a point of its own colour, past where the stair's dark would have hidden it, smaller and fainter as it goes, until at REACH
 * the dark has it. Whatever really stands in front of it hides it: a flight, a landing, another reader. It lights nothing. The
 * stair a reader stands on is one of those things, so they watch it go by leaning over a rail ({@link StaircaseLeanView}).
 *
 * <p>The server has already followed the fall ({@link StaircaseFallingLight}); this steps the same motion and draws it.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class StaircaseFallingLights {
    /** One point of light: where it is, its colour, how strong it is at its source (0 to 1), and its half-width there in blocks. */
    public record Light(double x,double y,double z,int rgb,float strength,float size){}
    /** How far off a falling light is still seen; past this the dark has it. */
    public static final float REACH=260;
    /** How long one that has come down on something goes on burning there (ticks), and how many may be falling at once. */
    public static final int REST=160,MOST=32;
    /** Not drawn below this strength; never smaller on the screen than this (half-width over distance); a flame's half-width. */
    static final float FAINT=.02F,ANGLE=.0024F,SIZE=.11F;

    /** One light on its way down, or burning where it came to rest. */
    public static final class Falling {
        final StaircaseFallingLight.Motion motion;final int ticks,tint;final Vec3 end;final boolean hit;final float seed;
        double px,py,pz;int age;
        public Falling(Vec3 start,Vec3 speed,int ticks,Vec3 end,boolean hit,int tint,float seed){
            motion=new StaircaseFallingLight.Motion(start,speed);this.ticks=ticks;this.end=end;this.hit=hit;this.tint=tint;this.seed=seed;
            px=start.x;py=start.y;pz=start.z;
        }
        /** One tick: it falls as the server followed it; at the end of its fall it is set down where the server found it came to rest. */
        public void step(){
            px=motion.x;py=motion.y;pz=motion.z;age++;
            if(age<ticks)motion.step();
            else{motion.x=end.x;motion.y=end.y;motion.z=end.z;if(age>ticks){px=end.x;py=end.y;pz=end.z;}}
        }
        public boolean gone(){return age>=ticks+(hit?REST:0);}
        public double y(){return motion.y;}
        public Light light(float partial){
            float flicker=.8F+.2F*Mth.sin((age+partial)*.9F+seed);
            // Come to rest, it gutters out over its last seconds.
            float left=age<ticks?1:Mth.clamp((ticks+REST-age-partial)/REST,0,1);
            return new Light(Mth.lerp(partial,px,motion.x),Mth.lerp(partial,py,motion.y),Mth.lerp(partial,pz,motion.z),tint,flicker*left,SIZE);
        }
    }

    private static final List<Falling> falling=new ArrayList<>();
    private static ClientLevel level;
    private StaircaseFallingLights(){}

    public static void accept(StairLightFallPayload p){
        var mc=Minecraft.getInstance();if(mc.level==null||!mc.level.dimension().equals(HouseDimensions.INTERIOR))return;
        if(level!=mc.level){falling.clear();level=mc.level;}
        if(falling.size()>=MOST)falling.remove(0);
        falling.add(new Falling(p.start(),p.speed(),p.ticks(),p.end(),p.hit(),p.tint(),mc.level.random.nextFloat()*100));
    }
    public static int inTheAir(){return falling.size();}

    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        if(falling.isEmpty())return;var mc=Minecraft.getInstance();
        if(mc.level==null||mc.level!=level){falling.clear();level=null;return;}
        if(mc.isPaused())return;
        for(var it=falling.iterator();it.hasNext();){var light=it.next();light.step();if(light.gone())it.remove();}
    }

    @SubscribeEvent public static void render(RenderLevelStageEvent e){
        if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES||falling.isEmpty())return;
        var mc=Minecraft.getInstance();if(mc.level==null||mc.level!=level)return;
        float partial=mc.isPaused()?1:e.getPartialTick().getGameTimeDeltaPartialTick(false);
        var lights=new ArrayList<Light>(falling.size());for(var light:falling)lights.add(light.light(partial));
        // The event's matrix turns the world about the eye; with the stack cleared it is the only turn applied, whatever the stage left there.
        var stack=RenderSystem.getModelViewStack();stack.pushMatrix();stack.identity();RenderSystem.applyModelViewMatrix();
        try{draw(e.getModelViewMatrix(),e.getCamera().getPosition(),lights,mc.gameRenderer.getDepthFar(),true);}
        finally{stack.popMatrix();RenderSystem.applyModelViewMatrix();}
    }

    /** How strongly a light of full strength shows at a distance: whole close by, nothing at REACH. */
    public static float shown(float distance){return (float)Math.pow(Mth.clamp(1-distance/REACH,0,1),1.3);}
    /**
     * Draws the lights as squares facing the eye. {@code view} turns an offset from the eye into view space; the model-view stack
     * must be clear. With {@code depth}, a light is hidden by whatever the frame has drawn in front of it. Returns how many were drawn.
     */
    public static int draw(Matrix4f view,Vec3 eye,Collection<Light> lights,float far,boolean depth){
        RenderSystem.enableBlend();RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);if(depth)RenderSystem.enableDepthTest();else RenderSystem.disableDepthTest();
        RenderSystem.disableCull();RenderSystem.setShaderColor(1,1,1,1);RenderSystem.setShader(GameRenderer::getPositionColorShader);
        var builder=Tesselator.getInstance().begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.POSITION_COLOR);int drawn=0;var v=new Vector3f();
        try{
            for(var light:lights){
                v.set((float)(light.x()-eye.x),(float)(light.y()-eye.y),(float)(light.z()-eye.z));view.transformPosition(v);
                if(v.z>-.3F)continue;
                float distance=v.length(),alpha=shown(distance)*light.strength();if(alpha<FAINT)continue;
                float half=Math.max(light.size(),distance*ANGLE);
                // Never past the edge of the frame's depth: a light further off than that is drawn at the edge, as small as it would be.
                float at=Math.min(distance,far*.96F),scale=at/distance;v.mul(scale);half*=scale;
                // A square of light with two fainter ones round it: a glow in the game's own blocky hand.
                square(builder,v,half,light.rgb(),alpha);square(builder,v,half*1.8F,light.rgb(),alpha*.28F);square(builder,v,half*3F,light.rgb(),alpha*.1F);drawn++;
            }
            var mesh=builder.build();if(mesh!=null)BufferUploader.drawWithShader(mesh);
        }finally{
            RenderSystem.depthMask(true);RenderSystem.enableDepthTest();RenderSystem.enableCull();RenderSystem.defaultBlendFunc();RenderSystem.disableBlend();
        }
        return drawn;
    }
    private static void square(BufferBuilder b,Vector3f c,float half,int rgb,float alpha){
        int a=Mth.clamp(Math.round(alpha*255),0,255),r=rgb>>16&255,g=rgb>>8&255,blue=rgb&255;
        b.addVertex(c.x-half,c.y-half,c.z).setColor(r,g,blue,a);b.addVertex(c.x+half,c.y-half,c.z).setColor(r,g,blue,a);
        b.addVertex(c.x+half,c.y+half,c.z).setColor(r,g,blue,a);b.addVertex(c.x-half,c.y+half,c.z).setColor(r,g,blue,a);
    }
}
