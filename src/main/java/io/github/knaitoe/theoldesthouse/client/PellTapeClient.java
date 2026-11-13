package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.PellTapes;
import io.github.knaitoe.theoldesthouse.labyrinth.PellTexts;
import io.github.knaitoe.theoldesthouse.labyrinth.PellThing;
import io.github.knaitoe.theoldesthouse.network.PellTapePayload;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

/**
 * 0.4.536, the Pell house's tapes (labyrinth/PellTapes), on a reader's own client: the tape the shed's deck is playing, on the portable
 * television's screen, and the woman's lines as captions from it (SpeechCaptions). Each game tick a 96 by 72 frame is built from the
 * shot tools/draw_pell_tapes.py draws for that tape (one to a tape, its date stamped in the corner) and run through an old tape: the
 * picture rolling until the deck locks on, grain, a tracking band, jittered lines, colour slipping, the head-switch at the foot,
 * dropouts (worse on the tape in the deck when a visit begins, the most watched), and snow before and after.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class PellTapeClient {
    private PellTapeClient(){}
    public static final ResourceLocation ATLAS=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/pell/tapes.png");
    public static final int W=96,H=72,COLS=3;
    /** The screen's picture in its block, in sixteenths as drawn facing north: x0, y0, x1, y1, and its depth in the block. */
    static final float[] RECT={5,5.5F,13,11.5F,4.9F};

    private static int tape=-1,pace=100;private static long start;private static BlockPos screen=BlockPos.ZERO;private static Direction facing=Direction.SOUTH;
    private static @Nullable NativeImage atlas;private static @Nullable DynamicTexture texture;private static @Nullable ResourceLocation location;private static boolean failed,registered;
    private static long built=Long.MIN_VALUE;private static final int[] rgb=new int[W*H];

    public static void accept(PellTapePayload p){
        if(!registered){SpeechCaptions.voices(PellTapeClient::voices);registered=true;}
        tape=p.tape();if(tape<0)return;
        screen=p.screen();facing=Direction.from2DDataValue(p.facing());start=p.start();pace=p.pace();built=Long.MIN_VALUE;
    }
    /** The tape playing for this client, or -1 (for the native visual proof). */
    public static int playing(){return tape;}
    @SubscribeEvent public static void out(ClientPlayerNetworkEvent.LoggingOut e){tape=-1;}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        var mc=Minecraft.getInstance();if(tape>=0&&mc.level!=null&&mc.level.getGameTime()>start+PellTapes.length(tape,pace)+20)tape=-1;
    }
    /** Whether the set is where the tape was sent from, in this client's level, and lit. */
    private static boolean there(){
        var mc=Minecraft.getInstance();if(tape<0||mc.level==null)return false;
        var s=mc.level.getBlockState(screen);return PellThing.is(s,PellThing.Kind.SHED_TV);
    }
    /** Her line now, from the screen, while one is up. */
    static List<SpeechCaptions.Voice> voices(){
        if(!there())return List.of();var mc=Minecraft.getInstance();
        int line=PellTapes.lineAt(tape,pace,(int)(mc.level.getGameTime()-start));if(line<0)return List.of();
        return List.of(new SpeechCaptions.Voice(Vec3.atCenterOf(screen),PellTexts.TAPE_VOICE,Component.literal(PellTexts.TAPE_LINES[tape][line])));
    }

    private static boolean ready(){
        if(location!=null)return true;if(failed)return false;var mc=Minecraft.getInstance();
        try(var in=mc.getResourceManager().open(ATLAS)){atlas=NativeImage.read(in);}catch(Exception e){failed=true;TheOldestHouse.LOGGER.warn("The Pell house's tapes could not read their atlas",e);return false;}
        texture=new DynamicTexture(W,H,false);location=mc.getTextureManager().register("the_oldest_house_pell_tape",texture);
        return true;
    }

    // ------------------------------------------------------------------------------------------------ composing
    private static int seed;
    private static float rand(){seed=seed*1103515245+12345;return ((seed>>>8)&0xFFFF)/65535F;}
    /** The current frame's pixels as 0xRRGGBB, for the native visual proof. */
    public static @Nullable int[] pixels(long gameTime){if(tape<0||!ready())return null;compose(gameTime);built=Long.MIN_VALUE;return rgb.clone();}
    static void compose(long gameTime){
        int t=(int)(gameTime-start),length=PellTapes.length(tape,pace);seed=(int)(gameTime*7919+tape*131+17);
        if(t<10||t>=length-10){snow();vhs(t,0);return;}
        int roll=t<26?(26-t)*(26-t)/6:0;
        int ox=(tape%COLS)*W,oy=(tape/COLS)*H;
        for(int y=0;y<H;y++){int sy=Math.floorMod(y+roll,H+4);
            for(int x=0;x<W;x++){
                if(sy>=H){rgb[y*W+x]=0x080808;continue;}
                int c=atlas.getPixelRGBA(ox+x,oy+sy);rgb[y*W+x]=(c&255)<<16|(c>>8&255)<<8|(c>>16&255);
            }
        }
        vhs(t,tape==PellTapes.IN_DECK?3:PellTexts.writtenOver(tape)?2:1);
    }
    private static void snow(){for(int i=0;i<rgb.length;i++){int v=(int)(rand()*190);rgb[i]=v<<16|v<<8|v;}}
    /** The tape: a tracking band, jittered lines, colour slipping, grain, the head-switch at the foot, and its dropouts. */
    private static void vhs(int t,int wear){
        float k=Comfort.effects();
        int band=(int)((t*1.1F)%(H+30))-15;int[] row=new int[W];
        for(int y=0;y<H;y++){
            int shift=Math.abs(y-band)<3?(int)(rand()*4)+1:rand()<.03F?(rand()<.5F?-1:1):0;
            if(y>=H-3)shift+=(int)(rand()*6);
            for(int x=0;x<W;x++)row[x]=rgb[y*W+Math.floorMod(x-shift,W)];
            for(int x=0;x<W;x++){
                int c=row[x],cr=row[Math.max(0,x-1)];
                int r=(cr>>16&255),g=(c>>8&255),b=(c&255);
                int lum=(int)(.3*r+.59*g+.11*b);r=(int)(r*.6+lum*.4);g=(int)(g*.6+lum*.4);b=(int)(b*.6+lum*.4+5);
                int noise=(int)((rand()-.5F)*30);if(Math.abs(y-band)<3&&rand()<.2F)noise+=(int)(70*k);
                float dx=(x-W/2F)/(W/2F),dy=(y-H/2F)/(H/2F);float vig=1-.3F*(dx*dx+dy*dy);
                r=Mth.clamp((int)((r+noise)*vig),0,255);g=Mth.clamp((int)((g+noise)*vig),0,255);b=Mth.clamp((int)((b+noise)*vig),0,255);
                rgb[y*W+x]=r<<16|g<<8|b;
            }
        }
        // Dropouts: a white streak across a line, here and there; more on a worn tape.
        for(int i=0;i<wear;i++)if(rand()<.18F*wear){int y=(int)(rand()*H),x0=(int)(rand()*W),len=4+(int)(rand()*W/3);
            for(int x=x0;x<Math.min(W,x0+len);x++){int c=rgb[y*W+x];int v=(int)(150+90*k);rgb[y*W+x]=Math.max(c>>16&255,v)<<16|Math.max(c>>8&255,v)<<8|Math.max(c&255,v);}}
    }
    private static void upload(){
        var image=texture.getPixels();if(image==null)return;
        for(int y=0;y<H;y++)for(int x=0;x<W;x++){int c=rgb[y*W+x];image.setPixelRGBA(x,y,0xFF000000|(c&255)<<16|(c>>8&255)<<8|(c>>16&255));}
        texture.upload();
    }

    // ------------------------------------------------------------------------------------------------ the screen in the world
    @SubscribeEvent public static void render(RenderLevelStageEvent e){
        if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS||!there()||!ready())return;
        var mc=Minecraft.getInstance();long time=mc.level.getGameTime();
        if(time!=built){built=time;compose(time);upload();}
        var eye=e.getCamera().getPosition();var buffers=mc.renderBuffers().bufferSource();
        var pose=new PoseStack();var at=Vec3.atLowerCornerOf(screen).subtract(eye);pose.translate(at.x,at.y,at.z);
        quad(buffers.getBuffer(RenderType.text(location)),pose.last().pose(),facing,RECT);
        buffers.endBatch();
    }
    /** A point in the block, as drawn facing north (sixteenths), turned to the screen's facing. */
    private static float[] turn(Direction f,float x,float y,float z){
        float dx=x/16-.5F,dz=z/16-.5F;float rx,rz;
        switch(f){case EAST->{rx=-dz;rz=dx;}case SOUTH->{rx=-dx;rz=-dz;}case WEST->{rx=dz;rz=-dx;}default->{rx=dx;rz=dz;}}
        return new float[]{rx+.5F,y/16,rz+.5F};
    }
    /** The picture, seen from in front of the screen: its left edge is the drawn x1, its right the drawn x0. */
    private static void quad(VertexConsumer out,Matrix4f m,Direction f,float[] r){
        float[] a=turn(f,r[2],r[1],r[4]),b=turn(f,r[0],r[1],r[4]),c=turn(f,r[0],r[3],r[4]),d=turn(f,r[2],r[3],r[4]);
        int light=LightTexture.FULL_BRIGHT,col=0xFFFFFFFF;
        out.addVertex(m,a[0],a[1],a[2]).setColor(col).setUv(0,1).setLight(light);out.addVertex(m,b[0],b[1],b[2]).setColor(col).setUv(1,1).setLight(light);
        out.addVertex(m,c[0],c[1],c[2]).setColor(col).setUv(1,0).setLight(light);out.addVertex(m,d[0],d[1],d[2]).setColor(col).setUv(0,0).setLight(light);
        // Wound both ways, as the work print's screen is, so whichever way the quad faces it is drawn.
        out.addVertex(m,d[0],d[1],d[2]).setColor(col).setUv(0,0).setLight(light);out.addVertex(m,c[0],c[1],c[2]).setColor(col).setUv(1,0).setLight(light);
        out.addVertex(m,b[0],b[1],b[2]).setColor(col).setUv(1,1).setLight(light);out.addVertex(m,a[0],a[1],a[2]).setColor(col).setUv(0,1).setLight(light);
    }
}
