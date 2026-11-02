package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.WorkPrint;
import io.github.knaitoe.theoldesthouse.network.WorkPrintFilmPayload;
import io.github.knaitoe.theoldesthouse.network.WorkPrintHauntPayload;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

/**
 * 0.4.203, Work print, on the reader's own client: <i>Hallstand</i>, frame by frame, on the screen their viewing is playing on,
 * and nobody else's. Each game tick one 128 by 96 frame is built from the atlas tools/draw_work_print_art.py draws (the title
 * card, the hall, the door from the wrong side, the corridor with its figure at three distances, the page being written, a
 * room, the well) and from the footage the House has taken of this reader, then run through a tired tape: grain, a tracking
 * band rolling down, line jitter, colour slipping off the edges, the head-switch at the foot. Over their own sight it draws
 * the film's grain when it has them, and a frame of their footage for a moment.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class WorkPrintClient {
    private WorkPrintClient(){}
    public static final ResourceLocation ATLAS=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/work_print/hallstand.png");
    public static final int W=128,H=96;
    /** The atlas tools/draw_work_print_art.py draws: two rows of shots and a row of sprites. */
    static final int ATLAS_W=512,ATLAS_H=288;
    /** The atlas: shots 128 by 96 in two rows of four, then the figure at three distances, the hand, and the handprints. */
    record Region(int x,int y,int w,int h){}
    static final Region TITLE=new Region(0,0,W,H),HALL=new Region(128,0,W,H),DOOR=new Region(256,0,W,H),CORRIDOR=new Region(384,0,W,H),
        ROOM=new Region(0,96,W,H),PAGE=new Region(128,96,W,H),END=new Region(256,96,W,H),WELL=new Region(384,96,W,H),
        FIG_FAR=new Region(0,192,12,24),FIG_MID=new Region(16,192,24,48),FIG_NEAR=new Region(48,192,48,96),HAND=new Region(96,192,32,24),HANDS=new Region(128,192,W,H);
    /** Shot boundaries, in ticks from the start of the tape (the film is WorkPrint.FILM_TICKS long). */
    static final int[] SHOTS={0,160,360,540,740,920,1100,1300,1480,1800};

    // ------------------------------------------------------------------------------------------------ state
    private static int kind,number;private static long start;private static BlockPos screen=BlockPos.ZERO,lastScreen=null;private static Direction facing=Direction.SOUTH;
    private static final List<int[]> footage=new ArrayList<>();private static int[] scene;private static String[] credits=new String[0];
    private static int grain,flash,hands;private static int[] flashFrame;
    private static @Nullable NativeImage atlas;private static @Nullable DynamicTexture texture,flashTexture;private static @Nullable ResourceLocation location,flashLocation;private static boolean failed;
    private static long built=Long.MIN_VALUE;private static final int[] rgb=new int[W*H];

    public static void film(WorkPrintFilmPayload p){
        kind=p.kind();if(kind==0){return;}
        start=p.start();number=p.number();screen=p.screen();lastScreen=p.screen();facing=Direction.from2DDataValue(p.facing());
        footage.clear();for(var f:p.footage())footage.add(grey(f));scene=p.scene().length==WorkPrint.FW*WorkPrint.FH?grey(p.scene()):null;
        credits=p.credits().split("\n");built=Long.MIN_VALUE;
    }
    public static void haunt(WorkPrintHauntPayload p){
        switch(p.kind()){
            case 0->grain=Math.max(grain,p.ticks());
            case 1->{flash=p.ticks();flashFrame=p.frame().length==WorkPrint.FW*WorkPrint.FH?grey(p.frame()):null;uploadFlash();}
            case 2->hands=p.ticks();
            default->{}
        }
    }
    /** Whether a viewing is playing for this reader, and what it is (for the native visual proof). */
    public static int playing(){return kind;}
    @SubscribeEvent public static void out(ClientPlayerNetworkEvent.LoggingOut e){kind=0;grain=0;flash=0;hands=0;footage.clear();scene=null;lastScreen=null;}
    @SubscribeEvent public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post e){if(grain>0)grain--;if(flash>0)flash--;if(hands>0)hands--;
        var mc=Minecraft.getInstance();if(kind!=0&&mc.level!=null&&mc.level.getGameTime()>start+length()+40)kind=0;}
    static int length(){return kind==3?WorkPrint.REEL_TICKS:kind==4?WorkPrint.STAINED_TICKS:WorkPrint.FILM_TICKS;}
    static int[] grey(byte[] b){var out=new int[b.length];for(int i=0;i<b.length;i++)out[i]=b[i]&255;return out;}

    private static boolean ready(){
        if(location!=null)return true;if(failed)return false;var mc=Minecraft.getInstance();
        try(var in=mc.getResourceManager().open(ATLAS)){atlas=NativeImage.read(in);}catch(Exception e){failed=true;TheOldestHouse.LOGGER.warn("Work print could not read its film atlas",e);return false;}
        texture=new DynamicTexture(W,H,false);location=mc.getTextureManager().register("the_oldest_house_work_print_frame",texture);
        flashTexture=new DynamicTexture(WorkPrint.FW,WorkPrint.FH,false);flashLocation=mc.getTextureManager().register("the_oldest_house_work_print_flash",flashTexture);
        return true;
    }
    private static void uploadFlash(){
        if(!ready()||flashFrame==null)return;var img=flashTexture.getPixels();if(img==null)return;
        for(int y=0;y<WorkPrint.FH;y++)for(int x=0;x<WorkPrint.FW;x++){int v=flashFrame[x+y*WorkPrint.FW];int r=(int)(v*.86),g=(int)(v*.97),b=v;img.setPixelRGBA(x,y,0xFF000000|b<<16|g<<8|r);}
        flashTexture.upload();
    }

    // ------------------------------------------------------------------------------------------------ composing
    private static int seed;
    private static float rand(){seed=seed*1103515245+12345;return ((seed>>>8)&0xFFFF)/65535F;}
    /** The current frame's pixels as 0xRRGGBB, for the native visual proof. */
    public static @Nullable int[] pixels(long gameTime){if(!ready()||kind==0)return null;compose(gameTime);built=Long.MIN_VALUE;return rgb.clone();}
    static void compose(long gameTime){
        int t=(int)(gameTime-start);seed=(int)(gameTime*7919+31);
        if(kind==4){snow(1);if(t>40&&t<160&&(t/3)%7!=0)plate(WELL,.85F);vhs(t);return;}
        if(kind==3&&t>=WorkPrint.FILM_TICKS){
            int r=t-WorkPrint.FILM_TICKS;
            if(r<200){if(scene!=null)footageFrame(scene,1+r/400F);else plate(CORRIDOR,1);fade(Math.min(1,r/30F));}
            else black();
            vhs(t);return;
        }
        int shot=0;for(int i=0;i<SHOTS.length-1;i++)if(t>=SHOTS[i])shot=i;int local=t-SHOTS[shot];int n=Math.max(1,number);
        switch(shot){
            case 0->{plate(TITLE,1);fade(Math.min(1,local/30F));}
            case 1->{if(n>=3&&footage.size()>=2)footageFrame(footage.get(footage.size()-2),1);else plate(HALL,1);}
            case 2->plate(DOOR,1);
            case 3->figureShot(n,0,local);
            case 4->{plate(PAGE,1);writing(local);sprite(HAND,40+Math.min(60,local/3),48+(int)(Mth.sin(local*.4F)*2));}
            case 5->figureShot(n,1,local);
            case 6->{if(n>=2&&!footage.isEmpty())footageFrame(footage.get(footage.size()-1),1);else plate(ROOM,1);}
            case 7->figureShot(n,2,local);
            default->{plate(END,1);fade(1-Math.min(1,Math.max(0,local-200)/100F));}
        }
        vhs(t);
    }
    /** The corridor with its figure: nearer at each of the three shots, nearer still with every viewing; by the fifth, it is them. */
    private static void figureShot(int n,int which,int local){
        if(n>=5&&!footage.isEmpty()){footageFrame(footage.get(Math.floorMod(footage.size()-1-which,footage.size())),1+which*.15F);return;}
        plate(CORRIDOR,1);
        int step=Math.min(2,which+(n-1)/2);
        Region r=step==0?FIG_FAR:step==1?FIG_MID:FIG_NEAR;
        float scale=step==2&&n>=4?1.5F:1;int w=(int)(r.w()*scale),h=(int)(r.h()*scale);
        sprite(r,W/2-w/2,(int)(H*.62F)-h+(step==2?h/3:0),scale);
    }
    private static void plate(Region r,float a){
        for(int y=0;y<H;y++)for(int x=0;x<W;x++){int c=atlas.getPixelRGBA(r.x()+x,r.y()+y);int R=c&255,G=c>>8&255,B=c>>16&255;int old=rgb[y*W+x];
            rgb[y*W+x]=a>=1?(R<<16|G<<8|B):mix(old,R<<16|G<<8|B,a);}
    }
    private static void sprite(Region r,int ox,int oy){sprite(r,ox,oy,1);}
    private static void sprite(Region r,int ox,int oy,float scale){
        int w=(int)(r.w()*scale),h=(int)(r.h()*scale);
        for(int y=0;y<h;y++)for(int x=0;x<w;x++){int px=ox+x,py=oy+y;if(px<0||py<0||px>=W||py>=H)continue;
            int c=atlas.getPixelRGBA(r.x()+(int)(x/scale),r.y()+(int)(y/scale));int a=c>>>24;if(a<40)continue;rgb[py*W+px]=(c&255)<<16|(c>>8&255)<<8|(c>>16&255);}
    }
    private static void footageFrame(int[] f,float zoom){
        for(int y=0;y<H;y++)for(int x=0;x<W;x++){
            float fx=(x-W/2F)/zoom+W/2F,fy=(y-H/2F)/zoom+H/2F;int sx=Mth.clamp((int)(fx*WorkPrint.FW/W),0,WorkPrint.FW-1),sy=Mth.clamp((int)(fy*WorkPrint.FH/H),0,WorkPrint.FH-1);
            int v=f[sx+sy*WorkPrint.FW];rgb[y*W+x]=(int)(v*.86)<<16|(int)(v*.97)<<8|v;
        }
    }
    /** The hand writes a line at a time across the page. */
    private static void writing(int local){
        int lines=Math.min(6,local/24);
        for(int l=0;l<=lines;l++){int y=22+l*9;int len=l<lines?70:Math.min(70,(local%24)*3);
            for(int x=0;x<len;x++){int px=28+x,py=y+(int)(Mth.sin((x+l*13)*.7F)*1.2F);if((x+l)%11<9&&px<W&&py<H)rgb[py*W+px]=0x2a2622;}}
    }
    private static void snow(float a){for(int i=0;i<rgb.length;i++){int v=(int)(rand()*200);rgb[i]=a>=1?(v<<16|v<<8|v):mix(rgb[i],v<<16|v<<8|v,a);}}
    private static void black(){java.util.Arrays.fill(rgb,0x050505);}
    private static void fade(float k){if(k>=1)return;for(int i=0;i<rgb.length;i++){int c=rgb[i];rgb[i]=(int)((c>>16&255)*k)<<16|(int)((c>>8&255)*k)<<8|(int)((c&255)*k);}}
    private static int mix(int a,int b,float k){int r=(int)((a>>16&255)*(1-k)+(b>>16&255)*k),g=(int)((a>>8&255)*(1-k)+(b>>8&255)*k),bl=(int)((a&255)*(1-k)+(b&255)*k);return r<<16|g<<8|bl;}
    /** The tape: grain, a tracking band, jittered lines, colour slipping off its edges, the head-switch at the foot. */
    private static void vhs(int t){
        int band=(int)((t*1.3F)%(H+40))-20;int[] row=new int[W];
        for(int y=0;y<H;y++){
            int shift=Math.abs(y-band)<4?(int)(rand()*6)+1:rand()<.04F?(rand()<.5F?-1:1):0;
            if(y>=H-4)shift+=(int)(rand()*8);
            for(int x=0;x<W;x++)row[x]=rgb[y*W+Math.floorMod(x-shift,W)];
            for(int x=0;x<W;x++){
                int c=row[x],cr=row[Math.max(0,x-1)];
                int r=(cr>>16&255),g=(c>>8&255),b=(c&255);
                int lum=(int)(.3*r+.59*g+.11*b);r=(int)(r*.55+lum*.45);g=(int)(g*.55+lum*.45);b=(int)(b*.55+lum*.45+6);
                int noise=(int)((rand()-.5F)*34);if(Math.abs(y-band)<4&&rand()<.25F)noise+=90;
                float dx=(x-W/2F)/(W/2F),dy=(y-H/2F)/(H/2F);float vig=1-.35F*(dx*dx+dy*dy);
                r=Mth.clamp((int)((r+noise)*vig),0,255);g=Mth.clamp((int)((g+noise)*vig),0,255);b=Mth.clamp((int)((b+noise)*vig),0,255);
                rgb[y*W+x]=r<<16|g<<8|b;
            }
        }
    }
    private static void upload(){
        var image=texture.getPixels();if(image==null)return;
        for(int y=0;y<H;y++)for(int x=0;x<W;x++){int c=rgb[y*W+x];image.setPixelRGBA(x,y,0xFF000000|(c&255)<<16|(c>>8&255)<<8|(c>>16&255));}
        texture.upload();
    }

    // ------------------------------------------------------------------------------------------------ the screen in the world
    /** A screen's rectangle in its block, in sixteenths, as drawn facing north: x0, y0, x1, y1 and how far in front of the block's north face. */
    static float[] rect(boolean flatbed){return flatbed?new float[]{3,17,13,25,11.9F}:new float[]{3,3,13,11,0.9F};}
    @SubscribeEvent public static void render(RenderLevelStageEvent e){
        if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS)return;var mc=Minecraft.getInstance();if(mc.level==null)return;
        boolean showFilm=kind!=0&&mc.level.getGameTime()<start+length();boolean showHands=hands>0&&lastScreen!=null;
        if(!showFilm&&!showHands)return;if(!ready())return;
        var eye=e.getCamera().getPosition();var buffers=mc.renderBuffers().bufferSource();
        if(showFilm){
            long time=mc.level.getGameTime();if(time!=built){built=time;compose(time);upload();}
            var pose=new PoseStack();var at=Vec3.atLowerCornerOf(screen).subtract(eye);pose.translate(at.x,at.y,at.z);
            quad(buffers.getBuffer(RenderType.text(location)),pose.last().pose(),facing,rect(kind==3),0,0,1,1,255);
            if(kind==3){int r=(int)(time-start)-WorkPrint.FILM_TICKS-200;if(r>=0)credits(pose,buffers,r);}
        }
        if(showHands){
            var pose=new PoseStack();var at=Vec3.atLowerCornerOf(lastScreen).subtract(eye);pose.translate(at.x,at.y,at.z);
            int alpha=(int)(255*Math.min(1,Math.min(hands/30F,(120-hands)/30F+.2F)));
            quad(buffers.getBuffer(RenderType.text(ATLAS)),pose.last().pose(),facing,rect(false),HANDS.x()/(float)ATLAS_W,HANDS.y()/(float)ATLAS_H,(HANDS.x()+W)/(float)ATLAS_W,(HANDS.y()+H)/(float)ATLAS_H,Math.max(0,alpha));
        }
        buffers.endBatch();
    }
    /** A point in the block, as drawn facing north (sixteenths), turned to the screen's facing. */
    private static float[] turn(Direction f,float x,float y,float z){
        float dx=x/16-.5F,dz=z/16-.5F;float rx,rz;
        switch(f){case EAST->{rx=-dz;rz=dx;}case SOUTH->{rx=-dx;rz=-dz;}case WEST->{rx=dz;rz=-dx;}default->{rx=dx;rz=dz;}}
        return new float[]{rx+.5F,y/16,rz+.5F};
    }
    /** The picture, seen from in front of the screen: its left edge is the drawn x1, its right the drawn x0. */
    private static void quad(VertexConsumer out,Matrix4f m,Direction f,float[] r,float u0,float v0,float u1,float v1,int alpha){
        float[] a=turn(f,r[2],r[1],r[4]),b=turn(f,r[0],r[1],r[4]),c=turn(f,r[0],r[3],r[4]),d=turn(f,r[2],r[3],r[4]);
        int light=LightTexture.FULL_BRIGHT,col=alpha<<24|0xFFFFFF;
        out.addVertex(m,a[0],a[1],a[2]).setColor(col).setUv(u0,v1).setLight(light);out.addVertex(m,b[0],b[1],b[2]).setColor(col).setUv(u1,v1).setLight(light);
        out.addVertex(m,c[0],c[1],c[2]).setColor(col).setUv(u1,v0).setLight(light);out.addVertex(m,d[0],d[1],d[2]).setColor(col).setUv(u0,v0).setLight(light);
        out.addVertex(m,d[0],d[1],d[2]).setColor(col).setUv(u0,v0).setLight(light);out.addVertex(m,c[0],c[1],c[2]).setColor(col).setUv(u1,v0).setLight(light);
        out.addVertex(m,b[0],b[1],b[2]).setColor(col).setUv(u1,v1).setLight(light);out.addVertex(m,a[0],a[1],a[2]).setColor(col).setUv(u0,v1).setLight(light);
    }
    /** The credits, white on the black frame. */
    private static void credits(PoseStack pose,net.minecraft.client.renderer.MultiBufferSource buffers,int r){
        var mc=Minecraft.getInstance();float[] rr=rect(true);var c=turn(facing,(rr[0]+rr[2])/2,(rr[1]+rr[3])/2,rr[4]-.05F);
        pose.pushPose();pose.translate(c[0],c[1],c[2]);pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-facing.toYRot()+180));pose.scale(-.0045F,-.0045F,.0045F);
        var lines=new ArrayList<String>();lines.add("HALLSTAND");lines.add("");lines.add("THE FIGURE");if(credits.length>0)lines.add(credits[0]);
        if(credits.length>1){lines.add("");lines.add("CAMERA");lines.add(credits[1]);}
        if(r>120){lines.add("");lines.add("THE END");}
        int y=-lines.size()*5;for(String line:lines){mc.font.drawInBatch(line,-mc.font.width(line)/2F,y,0xffe8e4dc,false,pose.last().pose(),buffers,Font.DisplayMode.NORMAL,0,LightTexture.FULL_BRIGHT);y+=10;}
        pose.popPose();
    }

    // ------------------------------------------------------------------------------------------------ over their own sight
    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Layers {
        @SubscribeEvent public static void register(RegisterGuiLayersEvent e){e.registerAboveAll(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"work_print_grain"),(g,d)->{
            if(grain<=0&&flash<=0)return;int w=g.guiWidth(),h=g.guiHeight();var mc=Minecraft.getInstance();long t=mc.level==null?0:mc.level.getGameTime();
            if(flash>0&&flashLocation!=null&&flashFrame!=null){g.blit(flashLocation,0,0,w,h,0F,0F,WorkPrint.FW,WorkPrint.FH,WorkPrint.FW,WorkPrint.FH);}
            float k=(flash>0?.6F:Math.min(1,grain/20F)*.45F)*Comfort.effects();seed=(int)(t*31+7);
            for(int y=0;y<h;y+=2)if(rand()<.5F)g.fill(0,y,w,y+1,(int)(k*60)<<24|0x0a0a0a);
            for(int i=0;i<w*h/90;i++){int x=(int)(rand()*w),y=(int)(rand()*h);int v=rand()<.5F?0xffffff:0x000000;g.fill(x,y,x+1,y+1,(int)(k*160)<<24|v);}
            int band=(int)((t*3)%(h+60))-30;g.fill(0,band,w,band+6,(int)(k*120)<<24|0xd8d8d8);
        });}
    }
}
