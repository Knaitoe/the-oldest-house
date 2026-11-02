package io.github.knaitoe.theoldesthouse.client;

import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.house.HouseText;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;

/**
 * 0.4.240 (owner: "make the splash a bit more haunting/ominous? maybe play with the text format, treat it as the ergodic style of the
 * original text"). The title screen's splash names the mod as before, where the splash goes and at its angle, but not in cheerful
 * pulsing yellow: an ashen line that does not hold still, set the way the book sets its pages.
 * <ul>
 * <li>"house" in its blue, as it is everywhere;</li>
 * <li>"has", with "had" struck through in red above it: a correction, the way the book strikes what it takes back. Now and then, for a
 * moment, the correction does not hold and the line reads "had";</li>
 * <li>the letters of "ghosts" drift and fade each on its own, as if not quite all there;</li>
 * <li>"in it" goes away from the reader letter by letter, smaller and fainter, a hallway longer than it should be;</li>
 * <li>and, faint under it all, the whole line again backwards, as if read through the page from the other side.</li>
 * </ul>
 * The light it is drawn in fails now and then, for a quarter of a second.
 */
public final class HouseSplash extends SplashRenderer {
    public static final String LINE="This house has ghosts in it";
    /** Ash, not yellow. */
    public static final int ASH=0xD8D2C2;
    /** How long between the moments the correction fails, and how long it fails for, in seconds. */
    public static final double SLIP_EVERY=11,SLIP_FOR=.22;
    /** For the client proof: a fixed time, in seconds, instead of the clock. */
    @Nullable public static Double fixed;
    public HouseSplash(){super(LINE);}

    static double now(){return fixed!=null?fixed:Util.getMillis()/1000.0;}
    /** Whether the light the splash is drawn in has failed at this moment: a quarter-second here and there, never regular. */
    static boolean failing(double t){long window=(long)Math.floor(t*4);long h=window*0x9E3779B97F4A7C15L;h^=h>>>29;return Math.floorMod(h,29)==0;}
    /** Whether the correction has slipped and the line reads "had". */
    public static boolean slipped(double t){return t%SLIP_EVERY<SLIP_FOR;}
    static int argb(int rgb,float alpha,int fade){int a=(int)(Mth.clamp(alpha,0,1)*((fade>>>24)&255));return (Math.max(4,a)<<24)|(rgb&0xFFFFFF);}

    @Override public void render(GuiGraphics g,int screenWidth,Font font,int color){
        double t=now();int fade=(color>>>24)==0?0xFF000000:color;float light=failing(t)?.32F:(float)(.9+.1*Math.sin(t*.7));
        var pose=g.pose();pose.pushPose();
        pose.translate(screenWidth/2F+123F,69F,0F);pose.mulPose(Axis.ZP.rotationDegrees(-20F));
        float scale=(float)(1.02+.015*Math.sin(t*.5));pose.scale(scale,scale,scale);
        boolean slip=slipped(t);
        int wThis=font.width("This "),wHouse=font.width("house"),wHas=font.width(" has"),wGhosts=font.width(" ghosts"),wIn=font.width(" in it");
        int total=wThis+wHouse+wHas+wGhosts+wIn;float x=-total/2F,y=-8;
        // Backwards and faint underneath, as if seen through the page.
        pose.pushPose();pose.translate(0,y+10,0);pose.scale(-.86F,.86F,1);JournalPageRenderer.uncull(g);
        try{g.drawString(font,Component.literal("This ").append(Component.literal("house").withColor(HouseText.SCREEN_BLUE)).append(" has ghosts in it"),-total/2,0,argb(ASH,.13F*light,fade),false);}
        finally{JournalPageRenderer.recull(g);pose.popPose();}
        // This house
        HouseText.enterLiterature();
        try{
            g.drawString(font,"This ",(int)x,(int)y,argb(ASH,light,fade),true);x+=wThis;
            g.drawString(font,Component.literal("house").withStyle(Style.EMPTY.withColor(HouseText.SCREEN_BLUE)),(int)x,(int)y,argb(HouseText.SCREEN_BLUE,light,fade),true);x+=wHouse;
            // has, and above it had, struck out in red; for a moment now and then the correction fails.
            float hasX=x+font.width(" ");
            if(slip){g.drawString(font," had",(int)x,(int)y,argb(HouseText.SCREEN_RED,light,fade),true);}
            else{
                g.drawString(font," has",(int)x,(int)y,argb(ASH,light,fade),true);
                pose.pushPose();pose.translate(hasX+1,y-6.5F,0);pose.scale(.62F,.62F,1);
                int w=font.width("had"),red=argb(HouseText.SCREEN_RED,.85F*light,fade);
                g.drawString(font,"had",0,0,red,false);g.fill(-1,3,w+1,4,red);
                pose.popPose();
            }
            x+=wHas;
            // ghosts, each letter drifting and fading by itself.
            x+=font.width(" ");String ghosts="ghosts";
            for(int i=0;i<ghosts.length();i++){
                String c=String.valueOf(ghosts.charAt(i));float dy=(float)(Math.sin(t*.9+i*1.9)*1.1),a=(float)(.5+.5*(.5+.5*Math.sin(t*.6+i*2.3)));
                pose.pushPose();pose.translate(x,y+dy,0);g.drawString(font,c,0,0,argb(ASH,a*light,fade),true);pose.popPose();x+=font.width(c);
            }
            // in it, going away down a hall longer than it should be.
            x+=font.width(" ");String in="in it";
            for(int i=0;i<in.length();i++){
                String c=String.valueOf(in.charAt(i));float s=1F-i*.14F;
                pose.pushPose();pose.translate(x,y+(1-s)*font.lineHeight*.42F,0);pose.scale(s,s,1);g.drawString(font,c,0,0,argb(ASH,(1-i*.13F)*light,fade),true);pose.popPose();
                x+=font.width(c)*s;
            }
        }finally{HouseText.leaveLiterature();}
        pose.popPose();
    }
}
