package io.github.knaitoe.theoldesthouse.client;

import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.house.HouseText;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * 0.4.240 (owner: "make the splash a bit more haunting/ominous? maybe play with the text format, treat it as the ergodic style of the
 * original text"); 0.4.252 (owner: "just keep the mirrored, faded, barely visible this house has ghosts in it"). Where the title screen's
 * splash goes, and at its angle, there is only the mod's name backwards and faint, as if read through the page from the other side, its
 * "house" in the House's blue. The light it is drawn in fails now and then, for a quarter of a second.
 */
public final class HouseSplash extends SplashRenderer {
    public static final String LINE="This house has ghosts in it";
    /** Ash, not yellow. */
    public static final int ASH=0xD8D2C2;
    /** How faint the backwards line is. */
    public static final float FAINT=.13F;
    /** For the client proof: a fixed time, in seconds, instead of the clock. */
    @Nullable public static Double fixed;
    public HouseSplash(){super(LINE);}

    static double now(){return fixed!=null?fixed:Util.getMillis()/1000.0;}
    /** Whether the light the splash is drawn in has failed at this moment: a quarter-second here and there, never regular. */
    static boolean failing(double t){long window=(long)Math.floor(t*4);long h=window*0x9E3779B97F4A7C15L;h^=h>>>29;return Math.floorMod(h,29)==0;}
    static int argb(int rgb,float alpha,int fade){int a=(int)(Mth.clamp(alpha,0,1)*((fade>>>24)&255));return (Math.max(4,a)<<24)|(rgb&0xFFFFFF);}

    @Override public void render(GuiGraphics g,int screenWidth,Font font,int color){
        double t=now();int fade=(color>>>24)==0?0xFF000000:color;float light=failing(t)?.32F:(float)(.9+.1*Math.sin(t*.7));
        var pose=g.pose();pose.pushPose();
        pose.translate(screenWidth/2F+123F,69F,0F);pose.mulPose(Axis.ZP.rotationDegrees(-20F));
        float scale=(float)(1.02+.015*Math.sin(t*.5));pose.scale(scale,scale,scale);
        int total=font.width(LINE);
        // Backwards and faint, as if seen through the page.
        pose.pushPose();pose.translate(0,2,0);pose.scale(-.86F,.86F,1);JournalPageRenderer.uncull(g);
        try{g.drawString(font,Component.literal("This ").append(Component.literal("house").withColor(HouseText.SCREEN_BLUE)).append(" has ghosts in it"),-total/2,0,argb(ASH,FAINT*light,fade),false);}
        finally{JournalPageRenderer.recull(g);pose.popPose();}
        pose.popPose();
    }
}
