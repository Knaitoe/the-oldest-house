package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.joml.Matrix4f;

/**
 * The lake's screen (0.4.102): the film itself, its burned-in words, the projector's beam reaching it from the dock, and the
 * shadows of whatever is in that beam (a rower's head and shoulders, a swimmer, or something on all fours) thrown up across
 * the picture, larger the nearer they are to the lens. Drawn from the one screen block at the foot of the black wall.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class MovieScreenRenderer implements BlockEntityRenderer<MovieScreenBlock.Screen> {
    private final Font font;
    public MovieScreenRenderer(BlockEntityRendererProvider.Context context){font=context.getFont();}
    @SubscribeEvent public static void register(EntityRenderersEvent.RegisterRenderers e){
        e.registerBlockEntityRenderer(MovieNightRegistry.SCREEN_ENTITY.get(),MovieScreenRenderer::new);
        e.registerEntityRenderer(MovieNightRegistry.SHARK.get(),LakeSharkRenderer::new);
    }
    @SubscribeEvent public static void layers(EntityRenderersEvent.RegisterLayerDefinitions e){e.registerLayerDefinition(LakeSharkModel.LAYER,LakeSharkModel::createBodyLayer);}

    /** The film's corners and the lens, in this block's own frame. */
    static final float X0=(float)MovieNight.FILM_X0,X1=(float)MovieNight.FILM_X1,Y0=(float)MovieNight.FILM_Y0,Y1=(float)MovieNight.FILM_Y1,Z=(float)(MovieNight.FILM_Z-MovieNight.SCREEN.getZ());
    static final Vec3 LENS=MovieNight.LENS.subtract(Vec3.atLowerCornerOf(MovieNight.SCREEN));

    @Override public void render(MovieScreenBlock.Screen screen,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        var mc=Minecraft.getInstance();if(mc.level==null)return;long time=mc.level.getGameTime();
        BlockPos base=screen.getBlockPos().subtract(MovieNight.SCREEN);var matrix=pose.last().pose();
        var frame=MovieFilm.frame(time);
        if(frame!=null)film(buffers.getBuffer(RenderType.text(frame)),matrix,flicker(time));
        shadows(base,buffers.getBuffer(RenderType.text(MovieFilm.ATLAS)),matrix,partial);
        beam(buffers.getBuffer(MovieRenderTypes.BEAM),matrix,time,partial);
        var line=MovieFilm.caption(time);
        if(line!=null)caption(line,pose,buffers);
    }
    private static int flicker(long time){int v=232+(int)(Math.floorMod(time*2654435761L,17));return v;}
    private static void film(VertexConsumer out,Matrix4f m,int v){
        quad(out,m,X0,Y0,X1,Y1,Z,0,1,1,0,v,v,v,255);
    }
    /** A flat quad on the film's plane, counter-clockwise seen from the south; u/v at its lower-left and upper-right corners. */
    private static void quad(VertexConsumer out,Matrix4f m,float x0,float y0,float x1,float y1,float z,float u0,float v0,float u1,float v1,int r,int g,int b,int a){
        out.addVertex(m,x0,y0,z).setColor(r,g,b,a).setUv(u0,v0).setLight(LightTexture.FULL_BRIGHT);
        out.addVertex(m,x1,y0,z).setColor(r,g,b,a).setUv(u1,v0).setLight(LightTexture.FULL_BRIGHT);
        out.addVertex(m,x1,y1,z).setColor(r,g,b,a).setUv(u1,v1).setLight(LightTexture.FULL_BRIGHT);
        out.addVertex(m,x0,y1,z).setColor(r,g,b,a).setUv(u0,v1).setLight(LightTexture.FULL_BRIGHT);
    }
    /** The words at the foot of the frame, white on a dark band, in the film's plane. */
    private void caption(String line,PoseStack pose,MultiBufferSource buffers){
        float scale=.045F;pose.pushPose();pose.translate((X0+X1)/2,Y0+.75F,Z+.02F);pose.scale(scale,-scale,scale);
        var text=Component.literal(line);float width=font.width(text);
        font.drawInBatch(text,-width/2,0,0xFFF2EEDC,false,pose.last().pose(),buffers,Font.DisplayMode.POLYGON_OFFSET,0x90000000,LightTexture.FULL_BRIGHT);
        pose.popPose();
    }
    /** Pale light from the lens to the four corners of the film, brightest at the lens. */
    private static void beam(VertexConsumer out,Matrix4f m,long time,float partial){
        float lx=(float)LENS.x,ly=(float)LENS.y,lz=(float)LENS.z;float pulse=.85F+.15F*Mth.sin((time+partial)*.9F);
        float[][] corners={{X0,Y0},{X1,Y0},{X1,Y1},{X0,Y1}};
        for(int i=0;i<4;i++){
            float[] a=corners[i],b=corners[(i+1)%4];int near=(int)(30*pulse),far=(int)(9*pulse);
            out.addVertex(m,lx,ly,lz).setColor(205,215,235,near);
            out.addVertex(m,lx,ly,lz).setColor(205,215,235,near);
            out.addVertex(m,b[0],b[1],Z+.03F).setColor(205,215,235,far);
            out.addVertex(m,a[0],a[1],Z+.03F).setColor(205,215,235,far);
        }
    }
    /** Every reader and the hunter in the beam throws a shadow on the film, clipped to the picture. */
    private static void shadows(BlockPos base,VertexConsumer out,Matrix4f m,float partial){
        var level=Minecraft.getInstance().level;if(level==null)return;
        var area=new AABB(Vec3.atLowerCornerOf(base).add(-16,-3,-42),Vec3.atLowerCornerOf(base).add(17,8,-16));
        for(var p:level.getEntitiesOfClass(Player.class,area,e->!e.isSpectator()))
            shadow(base,out,m,p,partial,p.getVehicle()!=null?MovieFilm.ROWER:MovieFilm.PERSON,p.getVehicle()!=null?1.1:p.getBbHeight());
        for(var w:level.getEntitiesOfClass(LakeWitchEntity.class,area,e->!e.memory()))shadow(base,out,m,w,partial,MovieFilm.CRAWLER,w.getBbHeight());
    }
    private static void shadow(BlockPos base,VertexConsumer out,Matrix4f m,Entity e,float partial,MovieFilm.Sprite sprite,double height){
        Vec3 at=e.getPosition(partial);Vec3 top=new Vec3(at.x,e instanceof Player p&&p.getVehicle()!=null?p.getEyePosition(partial).y+.3:at.y+height,at.z);
        var f=MovieNight.filmPoint(base,top);if(f==null)return;
        double w=X1-X0,h=Y1-Y0,size=height*f[2],sh=size/h,sw=size*sprite.w()/sprite.h()/w;
        double u0=f[0]-sw/2,u1=f[0]+sw/2,v1=f[1],v0=f[1]-sh;
        double cu0=Math.max(0,u0),cu1=Math.min(1,u1),cv0=Math.max(0,v0),cv1=Math.min(1,v1);if(cu0>=cu1||cv0>=cv1)return;
        // Atlas coordinates of the visible part; the sprite's top row is at the shadow's top.
        float ax0=(float)(sprite.x()+(cu0-u0)/(u1-u0)*sprite.w())/640F,ax1=(float)(sprite.x()+(cu1-u0)/(u1-u0)*sprite.w())/640F;
        float ayTop=(float)(sprite.y()+(v1-cv1)/(v1-v0)*sprite.h())/320F,ayBottom=(float)(sprite.y()+(v1-cv0)/(v1-v0)*sprite.h())/320F;
        quad(out,m,(float)(X0+cu0*w),(float)(Y0+cv0*h),(float)(X0+cu1*w),(float)(Y0+cv1*h),Z+.012F,ax0,ayBottom,ax1,ayTop,0,0,0,215);
    }
    @Override public boolean shouldRenderOffScreen(MovieScreenBlock.Screen screen){return true;}
    @Override public int getViewDistance(){return 160;}
    @Override public AABB getRenderBoundingBox(MovieScreenBlock.Screen screen){
        var at=screen.getBlockPos();return new AABB(Vec3.atLowerCornerOf(at.offset(-11,-1,0)),Vec3.atLowerCornerOf(at.offset(12,13,25)));
    }
}
