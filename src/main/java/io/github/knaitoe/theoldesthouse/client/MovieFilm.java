package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.MovieNight;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * *Teeth of Summer*, frame by frame (0.4.102). Each game tick the client builds one 160 x 90 frame from the atlas that
 * tools/draw_movie_night_art.py draws (backdrops, sprites, leader digits), keyed to the level's clock so every reader
 * sees the same moment, then runs it through an old print: grain, dust, scratches, gate weave, flicker and a vignette.
 * The sprite rectangles below mirror that tool's SPRITES table.
 */
public final class MovieFilm {
    public static final ResourceLocation ATLAS=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/movie/teeth_of_summer.png");
    public static final int W=160,H=90;
    record Sprite(int x,int y,int w,int h){}
    static final Sprite LEGS_A=new Sprite(0,180,32,40),LEGS_B=new Sprite(32,180,32,40),LIFEGUARD=new Sprite(64,180,24,40),HALE=new Sprite(88,180,24,44),
            BOY=new Sprite(112,180,16,28),FIN=new Sprite(128,180,24,16),FIN_FAR=new Sprite(128,200,10,6),SANDAL=new Sprite(152,180,16,8),
            FLARE=new Sprite(152,192,12,12),LANTERN=new Sprite(168,180,16,16),THE_END=new Sprite(184,180,96,16),SWIMMERS=new Sprite(380,180,40,16),
            GULL=new Sprite(380,200,8,4),BOAT=new Sprite(0,228,64,32),CONE=new Sprite(64,228,64,40),JAWS=new Sprite(128,228,96,64);
    /** The projector's shadows, drawn by the screen renderer straight from the atlas. */
    public static final Sprite ROWER=new Sprite(224,228,48,40),PERSON=new Sprite(272,228,32,64),CRAWLER=new Sprite(304,228,64,40);
    static Sprite digit(int n){return new Sprite(280+20*(n-1),180,20,28);}
    /** Words on the film, burned in at the foot of the frame: shot, first tick, last tick, line. */
    public record Caption(int shot,int from,int to,String line){}
    public static final List<Caption> CAPTIONS=List.of(
            new Caption(2,20,130,"LIFEGUARD (far off): Beach is closed, miss!"),new Caption(2,150,280,"GIRL: Just out to the buoy!"),
            new Caption(4,30,180,"RADIO: ...and it's another scorcher along the coast today..."),
            new Caption(5,20,200,"LIFEGUARD: Everybody out of the water! Out!"),
            new Caption(6,20,190,"MR. HALE: You'll close my beach over a fish story?"),
            new Caption(7,30,140,"SKIPPER: Hold that light steady."),new Caption(7,170,290,"SKIPPER: ...Listen. The bell's stopped."),
            new Caption(9,10,150,"SWIM! SWIM!"),new Caption(11,40,150,"BOY: Mom? Is it gone?"));

    private static @Nullable NativeImage atlas;
    private static @Nullable DynamicTexture texture;
    private static @Nullable ResourceLocation location;
    private static long built=Long.MIN_VALUE;
    private static boolean failed;
    private static final int[] rgb=new int[W*H];
    private MovieFilm(){}

    /** The current frame's texture, built at most once a tick; null if the atlas could not be read. */
    public static @Nullable ResourceLocation frame(long gameTime){
        if(!ready())return null;
        if(gameTime!=built){built=gameTime;compose(gameTime);upload();}
        return location;
    }
    /** The caption on screen now, if any. */
    public static @Nullable String caption(long gameTime){
        int frame=MovieNight.frame(gameTime),shot=MovieNight.shot(frame),t=frame-MovieNight.shotStart(shot);
        for(var c:CAPTIONS)if(c.shot()==shot&&t>=c.from()&&t<=c.to())return c.line();
        return null;
    }
    /** A frame's pixels as 0xRRGGBB, for the native visual proof; null if the atlas could not be read. */
    static @Nullable int[] pixels(long gameTime){if(!ready())return null;compose(gameTime);built=Long.MIN_VALUE;return rgb.clone();}
    public static void release(){if(location!=null)Minecraft.getInstance().getTextureManager().release(location);texture=null;location=null;if(atlas!=null)atlas.close();atlas=null;built=Long.MIN_VALUE;failed=false;}
    private static boolean ready(){
        if(location!=null)return true;if(failed)return false;
        var mc=Minecraft.getInstance();
        try(var in=mc.getResourceManager().open(ATLAS)){atlas=NativeImage.read(in);}
        catch(Exception e){failed=true;TheOldestHouse.LOGGER.warn("Movie Night could not read its film atlas",e);return false;}
        texture=new DynamicTexture(W,H,false);location=mc.getTextureManager().register("the_oldest_house_movie_frame",texture);
        return true;
    }
    private static void upload(){
        var image=texture.getPixels();if(image==null)return;
        // NativeImage keeps ABGR in an int.
        for(int y=0;y<H;y++)for(int x=0;x<W;x++){int c=rgb[y*W+x];image.setPixelRGBA(x,y,0xFF000000|(c&255)<<16|(c>>8&255)<<8|(c>>16&255));}
        texture.upload();
    }

    // ------------------------------------------------------------------------------------------------ composing
    private static int seed;
    private static float rand(){seed=seed*1103515245+12345;return ((seed>>>8)&0xFFFF)/65535F;}
    static void compose(long gameTime){
        int frame=MovieNight.frame(gameTime),shot=MovieNight.shot(frame),t=frame-MovieNight.shotStart(shot),len=MovieNight.SHOTS[shot];
        seed=(int)(gameTime*7919+13);
        int weave=rand()<.12F?1:0,shakeX=0,shakeY=weave;
        switch(shot){
            case 0->leader(t);
            case 1->{plate(0,0,0);sprite(GULL,(int)(-8+t*1.1F),18+(int)(Mth.sin(t*.2F)*2),1);fade(Math.min(1,t/20F));}
            case 2->{plate(1,0,0);sprite((t/8)%2==0?LEGS_A:LEGS_B,64+(int)(Mth.sin(t*.05F)*6),(int)(Mth.sin(t*.13F)*2)-2,1);}
            case 3->{plate(2,0,0);float k=t/(float)len;int y=t>len-40?44+(t-(len-40))/4:44;sprite(FIN,(int)(170-k*80),y+(int)(Mth.sin(t*.3F)),1);}
            case 4->{plate(3,0,0);sprite(SANDAL,72,76,1);sprite(SWIMMERS,100,32+(int)(Mth.sin(t*.15F)*1.5F),1);sprite(GULL,(int)(170-t*.9F),10,1);}
            case 5->{plate(4,0,0);sprite(LIFEGUARD,52,46+(t%10<5?0:1),1);sprite(SWIMMERS,96,(int)(30+Math.min(18,t*.12F)),1);}
            case 6->{plate(5,0,0);sprite(HALE,68,42+(int)(Mth.sin(t*.08F)),1);}
            case 7->{plate(7,0,0);int by=26+(int)(Mth.sin(t*.08F)*1.5F);sprite(BOAT,48,by,1);add(CONE,104,by-11+(int)(Mth.sin(t*.03F)*4),.9F);}
            case 8->{plate(7,0,0);darken(.85F);int by=26+(int)(Mth.sin(t*.3F)*(t>60?3:1.5F));sprite(BOAT,48,by,1);
                if(t>=60){int jy=Math.max(30,90-(t-60)*2);sprite(JAWS,32,jy,1);}
                if(t>=80&&t<140){shakeX=(int)(rand()*5)-2;shakeY+=(int)(rand()*3)-1;}
                if(t>=80&&t<84||t>=88&&t<91)flash(.75F);if(t>=150)fade(1-(t-150)/50F);}
            case 9->{plate(7,0,0);tint(1F,.32F,.28F);brightness(.75F+rand()*.4F);sprite(FLARE,40,20+t/12,1);sprite(FLARE,110,14+t/10,1);sprite(SWIMMERS,60,50+(int)(rand()*3),1);}
            case 10->{plate(7,0,0);darken(.6F);sprite(LANTERN,74,44+(int)(Mth.sin(t*.06F)*1.5F),1);if(t>len-30)fade((len-t)/30F);}
            case 11->{plate(6,0,0);sprite(BOY,40,58,1);if(t>100)sprite(FIN_FAR,(int)(124-(t-100)*.05F),45,1);if(t>=160)sprite(THE_END,32,18,Math.min(1,(t-160)/30F));fade(Math.min(1,t/30F));}
            default->runout(t,len);
        }
        print(shakeX,shakeY,shot);
    }
    /** The countdown leader: a grey card, a sweeping hand, five down to one. */
    private static void leader(int t){
        int digit=Math.max(1,5-t/20);float sweep=(t%20)/20F*Mth.TWO_PI;
        for(int y=0;y<H;y++)for(int x=0;x<W;x++){
            float dx=x-W/2F,dy=y-H/2F,r=Mth.sqrt(dx*dx+dy*dy);float a=(float)Math.atan2(dx,-dy);if(a<0)a+=Mth.TWO_PI;
            int v=a<sweep?150:196;if(Math.abs(r-34)<1.2F||Math.abs(r-28)<.8F||Math.abs(dx)<.6F||Math.abs(dy)<.6F)v=40;
            rgb[y*W+x]=v<<16|(v-4)<<8|(v-14);
        }
        var d=digit(digit);sprite(d,W/2-d.w()/2,H/2-d.h()/2,1);
    }
    /** The end of the reel: frames jump, then the gate shows bare white light until the film comes round again. */
    private static void runout(int t,int len){
        if(t<len-50){int v=rand()<.5F?236:24;fill(v<<16|v<<8|v);}
        else fill(0xF2F0E8);
    }
    private static void fill(int c){java.util.Arrays.fill(rgb,c);}
    private static void plate(int i,int ox,int oy){
        int px=(i%4)*W,py=(i/4)*H;
        for(int y=0;y<H;y++)for(int x=0;x<W;x++)rgb[y*W+x]=abgrToRgb(atlas.getPixelRGBA(Mth.clamp(px+x+ox,px,px+W-1),Mth.clamp(py+y+oy,py,py+H-1)));
    }
    private static int abgrToRgb(int c){return (c&255)<<16|(c>>8&255)<<8|(c>>16&255);}
    /** Paints a sprite over the frame with its own alpha, scaled by {@code alpha}. */
    private static void sprite(Sprite s,int x0,int y0,float alpha){
        for(int y=0;y<s.h();y++)for(int x=0;x<s.w();x++){
            int fx=x0+x,fy=y0+y;if(fx<0||fy<0||fx>=W||fy>=H)continue;
            int c=atlas.getPixelRGBA(s.x()+x,s.y()+y);float a=(c>>>24)/255F*alpha;if(a<=0)continue;
            int i=fy*W+fx,o=rgb[i];
            rgb[i]=mix(o>>16&255,c&255,a)<<16|mix(o>>8&255,c>>8&255,a)<<8|mix(o&255,c>>16&255,a);
        }
    }
    /** Adds a sprite as light (the searchlight's cone). */
    private static void add(Sprite s,int x0,int y0,float gain){
        for(int y=0;y<s.h();y++)for(int x=0;x<s.w();x++){
            int fx=x0+x,fy=y0+y;if(fx<0||fy<0||fx>=W||fy>=H)continue;
            int c=atlas.getPixelRGBA(s.x()+x,s.y()+y);float a=(c>>>24)/255F*gain;if(a<=0)continue;
            int i=fy*W+fx,o=rgb[i];
            rgb[i]=Math.min(255,(int)((o>>16&255)+(c&255)*a))<<16|Math.min(255,(int)((o>>8&255)+(c>>8&255)*a))<<8|Math.min(255,(int)((o&255)+(c>>16&255)*a));
        }
    }
    private static int mix(int a,int b,float k){return (int)(a+(b-a)*k);}
    private static void each(java.util.function.IntUnaryOperator op){for(int i=0;i<rgb.length;i++)rgb[i]=op.applyAsInt(rgb[i]);}
    private static int scale(int c,float r,float g,float b){return Mth.clamp((int)((c>>16&255)*r),0,255)<<16|Mth.clamp((int)((c>>8&255)*g),0,255)<<8|Mth.clamp((int)((c&255)*b),0,255);}
    private static void fade(float k){float f=Mth.clamp(k,0,1);each(c->scale(c,f,f,f));}
    private static void darken(float k){each(c->scale(c,k,k,k));}
    private static void brightness(float k){each(c->scale(c,k,k,k));}
    private static void tint(float r,float g,float b){each(c->scale(c,r,g,b));}
    private static void flash(float k){each(c->mix(c>>16&255,255,k)<<16|mix(c>>8&255,255,k)<<8|mix(c&255,255,k));}
    /** The old print: gate weave and shake, grain, dust and scratches, a little flicker, darker corners. */
    private static void print(int shakeX,int shakeY,int shot){
        if(shakeX!=0||shakeY!=0){int[] copy=rgb.clone();for(int y=0;y<H;y++)for(int x=0;x<W;x++)rgb[y*W+x]=copy[Mth.clamp(y-shakeY,0,H-1)*W+Mth.clamp(x-shakeX,0,W-1)];}
        float flicker=.95F+rand()*.07F;int scratch=rand()<.35F?(int)(rand()*W):-1,scratch2=rand()<.1F?(int)(rand()*W):-1;
        for(int y=0;y<H;y++)for(int x=0;x<W;x++){
            float dx=(x-W/2F)/(W/2F),dy=(y-H/2F)/(H/2F),vig=1-.28F*(dx*dx*.7F+dy*dy);
            float grain=(rand()-.5F)*(shot==12?40:16);int c=rgb[y*W+x];
            int r=(int)((c>>16&255)*flicker*vig+grain),g=(int)((c>>8&255)*flicker*vig+grain),b=(int)((c&255)*flicker*vig+grain);
            if(x==scratch||x==scratch2){r=r/2+110;g=g/2+105;b=b/2+95;}
            rgb[y*W+x]=Mth.clamp(r,0,255)<<16|Mth.clamp(g,0,255)<<8|Mth.clamp(b,0,255);
        }
        for(int k=0,n=(int)(rand()*5);k<n;k++){int x=(int)(rand()*W),y=(int)(rand()*H),v=rand()<.6F?12:230;rgb[y*W+x]=v<<16|v<<8|v;if(x+1<W)rgb[y*W+x+1]=v<<16|v<<8|v;}
    }
}
