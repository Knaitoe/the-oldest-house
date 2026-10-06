package io.github.knaitoe.theoldesthouse.client;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.world.level.material.FogType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import org.joml.Matrix4f;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;

/** Pocket landscapes sit below overworld sea level; never draw the black lower-sky hemisphere. */
public final class HouseOutsideEffects extends DimensionSpecialEffects {
    public HouseOutsideEffects(){super(Float.NaN,false,SkyType.NORMAL,false,false);}
    @Override public Vec3 getBrightnessDependentFogColor(Vec3 color,float daylight){return color.multiply(daylight*.94F+.06F,daylight*.94F+.06F,daylight*.91F+.09F);}
    @Override public boolean isFoggyAt(int x,int z){return false;}
    /** One complete sky, including below the horizon: no sea-level black hemisphere can overlap it. */
    @Override public boolean renderSky(ClientLevel level,int ticks,float partial,Matrix4f view,Camera camera,Matrix4f projection,boolean foggy,Runnable setupFog){
        setupFog.run();if(foggy||camera.getFluidInCamera()!=FogType.NONE)return true;
        drawSky(view,level.getSkyColor(camera.getPosition(),partial),level.getTimeOfDay(partial));return true;
    }
    public static void drawSky(Matrix4f view,Vec3 color,float time){
        RenderSystem.depthMask(false);RenderSystem.disableCull();RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1,1,1,1);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        try{
            var builder=Tesselator.getInstance().begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.POSITION_COLOR);
            float s=96;float r=(float)color.x,g=(float)color.y,b=(float)color.z;
            // Split the four sides at the horizon instead of drawing overlapping upper/lower discs.
            float[][] corners={{-s,-s},{s,-s},{s,s},{-s,s}};
            for(int i=0;i<4;i++){
                float[] a=corners[i],c=corners[(i+1)%4];
                skyVertex(builder,view,a[0],-s,a[1],r*.8F,g*.8F,b*.9F);skyVertex(builder,view,c[0],-s,c[1],r*.8F,g*.8F,b*.9F);
                skyVertex(builder,view,c[0],0,c[1],r,g,b);skyVertex(builder,view,a[0],0,a[1],r,g,b);
                skyVertex(builder,view,a[0],0,a[1],r,g,b);skyVertex(builder,view,c[0],0,c[1],r,g,b);
                skyVertex(builder,view,c[0],s,c[1],r*.42F,g*.48F,b*.62F);skyVertex(builder,view,a[0],s,a[1],r*.42F,g*.48F,b*.62F);
            }
            for(float y:new float[]{-s,s})for(var c:corners)skyVertex(builder,view,c[0],y,c[1],r*(y>0?.42F:.8F),g*(y>0?.48F:.8F),b*(y>0?.62F:.9F));
            BufferUploader.drawWithShader(builder.buildOrThrow());
            float night=1-Math.min(1,Math.max(0,(float)Math.cos(time*Math.PI*2)*2+.5F));
            if(night>.05F){
                builder=Tesselator.getInstance().begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.POSITION_COLOR);
                var random=new java.util.Random(48721);
                for(int i=0;i<140;i++){
                    double az=random.nextDouble()*Math.PI*2,alt=.12+random.nextDouble()*1.35;
                    float x=(float)(Math.cos(az)*Math.cos(alt)*80),y=(float)(Math.sin(alt)*80),z=(float)(Math.sin(az)*Math.cos(alt)*80),size=.045F+random.nextFloat()*.07F;
                    float light=(.4F+random.nextFloat()*.55F)*night;
                    skyVertex(builder,view,x-size,y-size,z,light,light,light);skyVertex(builder,view,x+size,y-size,z,light,light,light);
                    skyVertex(builder,view,x+size,y+size,z,light,light,light);skyVertex(builder,view,x-size,y+size,z,light,light,light);
                }
                // A quiet, small moon over the lake; it shares the scene clock with the gradient.
                for(float[] c:new float[][]{{-2,34,-72},{2,34,-72},{2,38,-72},{-2,38,-72}})skyVertex(builder,view,c[0],c[1],c[2],.72F*night,.75F*night,.79F*night);
                BufferUploader.drawWithShader(builder.buildOrThrow());
            }
        }finally{RenderSystem.disableBlend();RenderSystem.enableCull();RenderSystem.depthMask(true);RenderSystem.setShaderColor(1,1,1,1);}
    }
    private static void skyVertex(BufferBuilder b,Matrix4f m,float x,float y,float z,float r,float g,float blue){b.addVertex(m,x,y,z).setColor(r,g,blue,1);}
    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Registration {
        @SubscribeEvent public static void register(RegisterDimensionSpecialEffectsEvent e){e.register(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"outside"),new HouseOutsideEffects());e.register(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"interior"),new HouseInteriorEffects());}
    }
}
