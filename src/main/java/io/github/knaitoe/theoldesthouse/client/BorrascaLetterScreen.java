package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.knaitoe.theoldesthouse.network.BorrascaLetterPayload;
import io.github.knaitoe.theoldesthouse.network.BorrascaLetterSeenPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 0.4.459, Borrasca (labyrinth.BorrascaGarritys): the letter Mr. Dorsey hands a reader for the Garritys, held up. It is an envelope
 * and nothing else. Its face has who it is from printed in the corner, who it is to typed in the middle, and a stamp under its
 * postmark. A click, or space, turns it over: its back is a flap, and the flap is sealed. Nothing opens it.
 *
 * <p>Nothing is written on the screen about it. A side the reader has had before them for a moment is told to the server once
 * (BorrascaLetterSeenPayload), so the reader's account can say what was printed in the corner only if they looked.
 */
public final class BorrascaLetterScreen extends Screen {
    /** The envelope, in its own units. */
    public static final int EW=176,EH=100;
    /** Ticks a side is before the reader before it counts as looked at. */
    static final int COUNTS=15;
    public static final String FROM_ONE="PRESCOTT",FROM_TWO="ORE SERVICES",FROM_THREE="Ridge Depot";
    public static final String TO_ONE="Mr. & Mrs. Garrity",TO_TWO="Drisking";
    public static final String MARK_ONE="JUN",MARK_TWO="1992";
    static final int PAPER=0xFFEFE6CF,EDGE=0xFFD9CDB0,PRINT=0xFF4D6480,PRINT_FAINT=0xFF7F8FA3,INK=0xFF2B2A33,CANCEL=0xD0474B50;
    static final int OLD_PAPER=0xFFD9C690,OLD_EDGE=0xFFB59E63,OLD_PRINT=0xFF6E7468,OLD_PRINT_FAINT=0xFF8E8F7C,OLD_INK=0xFF5A4A3A,OLD_CANCEL=0xB0605A4C;

    final boolean aged;boolean turned;int before;int told;

    BorrascaLetterScreen(boolean aged){super(Component.literal("A letter for the Garritys"));this.aged=aged;}
    public static void accept(BorrascaLetterPayload p){Minecraft.getInstance().setScreen(new BorrascaLetterScreen(p.aged()));}
    @Override public boolean isPauseScreen(){return false;}
    /** What the side says, for a reader who is read to. */
    public static String text(boolean back){
        return back?"The back of the envelope. The flap is sealed.":"An envelope. Printed in its corner: "+FROM_ONE+" "+FROM_TWO+", "+FROM_THREE+". Typed on it: "+TO_ONE+", "+TO_TWO+". A stamp, postmarked "+MARK_ONE+" "+MARK_TWO+".";
    }
    @Override public Component getNarrationMessage(){return Component.literal(text(turned));}
    @Override public void tick(){
        int bit=turned?2:1;
        if(++before==COUNTS&&(told&bit)==0){told|=bit;PacketDistributor.sendToServer(new BorrascaLetterSeenPayload(turned));}
    }
    /** Screen pixels to the envelope's unit: the largest whole number that fits. */
    static int zoom(int width,int height){return Math.max(1,Math.min((width-24)/EW,(height-24)/EH));}
    /** Why the lettering does not fit the envelope, or null: a proof asks. */
    public static String problem(Font font){
        // The postmark's ring begins sixty-six from the right edge.
        if(Math.max(19+Math.max(font.width(FROM_ONE),font.width(FROM_TWO)),8+font.width(FROM_THREE))>EW-70)return "the sender's corner runs under the postmark";
        if(58+Math.max(font.width(TO_ONE),font.width(TO_TWO))>EW-8)return "the address runs off the envelope";
        if(Math.max(font.width(MARK_ONE),font.width(MARK_TWO))>28)return "the postmark's date is wider than the postmark";
        return null;
    }
    private static void outline(GuiGraphics g,int x0,int y0,int x1,int y1,int colour){g.fill(x0,y0,x1,y0+1,colour);g.fill(x0,y1-1,x1,y1,colour);g.fill(x0,y0+1,x0+1,y1-1,colour);g.fill(x1-1,y0+1,x1,y1-1,colour);}
    private static void line(GuiGraphics g,int x0,int y0,int x1,int y1,int colour){
        int n=Math.max(1,Math.max(Math.abs(x1-x0),Math.abs(y1-y0)));
        for(int i=0;i<=n;i++){int x=x0+Math.round((x1-x0)*i/(float)n),y=y0+Math.round((y1-y0)*i/(float)n);g.fill(x,y,x+1,y+1,colour);}
    }
    /** The envelope from its top left corner, in its own units: its face, or its back. */
    public static void envelope(GuiGraphics g,Font font,boolean aged,boolean back){
        int paper=aged?OLD_PAPER:PAPER,edge=aged?OLD_EDGE:EDGE;
        g.fill(4,4,EW+4,EH+4,0x70000000);g.fill(0,0,EW,EH,paper);outline(g,0,0,EW,EH,edge);
        if(aged){
            // Years under a door: a tide line across one end, and the corner that showed gone darker.
            // Turned over, they are at the other end.
            for(int y=1;y<EH-1;y++){int x=EW-34+(int)Math.round(5*Math.sin(y*.21));
                if(back){g.fill(1,y,EW-x,y+1,0x18705020);g.fill(EW-x-1,y,EW-x,y+1,0x30705020);}else{g.fill(x,y,EW-1,y+1,0x18705020);g.fill(x,y,x+1,y+1,0x30705020);}}
            for(int y=1;y<26;y++){int w=Math.max(0,38-y*3/2);if(back)g.fill(EW-1-w,y,EW-1,y+1,0x22604010);else g.fill(1,y,1+w,y+1,0x22604010);}
        }
        if(back){
            int flap=aged?0xFF9A7A46:edge,point=54;
            // The flap, down from the top corners to its point, a shade off the paper; the two seams up from the bottom corners.
            for(int y=1;y<point;y++){int w=(EW/2-2)*(point-y)/point;g.fill(EW/2-w,y,EW/2+w,y+1,0x0C000000);}
            line(g,1,1,EW/2,point,flap);line(g,EW-2,1,EW/2,point,flap);
            if(aged){line(g,2,1,EW/2,point-1,0x60705020);line(g,EW-3,1,EW/2,point-1,0x60705020);}
            line(g,1,EH-2,EW/2-14,point-10,edge);line(g,EW-2,EH-2,EW/2+14,point-10,edge);
            return;
        }
        int print=aged?OLD_PRINT:PRINT,faint=aged?OLD_PRINT_FAINT:PRINT_FAINT,ink=aged?OLD_INK:INK,cancel=aged?OLD_CANCEL:CANCEL;
        // Who it is from, printed: the company's mark, its name on two lines, a rule, and where it is.
        for(int i=0;i<4;i++){g.fill(11-i,8+i,12+i,9+i,print);g.fill(11-i,14-i,12+i,15-i,print);}
        g.fill(10,10,13,13,paper);
        g.drawString(font,FROM_ONE,19,7,print,false);g.drawString(font,FROM_TWO,19,16,print,false);
        g.fill(8,26,19+Math.max(font.width(FROM_ONE),font.width(FROM_TWO)),27,print);
        g.drawString(font,FROM_THREE,8,29,faint,false);
        // The stamp: its teeth, a hill and a pine.
        int sx=EW-30,sy=8;
        g.fill(sx,sy,sx+20,sy+24,aged?0xFFE4D8AE:0xFFF6F1E2);
        for(int i=1;i<20;i+=3){g.fill(sx+i,sy,sx+i+1,sy+1,paper);g.fill(sx+i,sy+23,sx+i+1,sy+24,paper);}
        for(int i=1;i<24;i+=3){g.fill(sx,sy+i,sx+1,sy+i+1,paper);g.fill(sx+19,sy+i,sx+20,sy+i+1,paper);}
        g.fill(sx+2,sy+2,sx+18,sy+22,aged?0xFF8FA08A:0xFF9CC3D8);g.fill(sx+2,sy+15,sx+18,sy+22,aged?0xFF6F7F5C:0xFF5E8A66);
        for(int i=0;i<6;i++)g.fill(sx+10-i,sy+5+i*2,sx+11+i,sy+7+i*2,aged?0xFF4C5A40:0xFF2F5A3C);
        g.fill(sx+10,sy+17,sx+11,sy+20,aged?0xFF5A4A3A:0xFF5A3D25);
        // The postmark: a ring with the month and the year in it, and its wavy lines across the stamp.
        int cx=EW-50,cy=21,r=16;
        for(int a=0;a<96;a++){double t=a*Math.PI/48;int x=cx+(int)Math.round(r*Math.cos(t)),y=cy+(int)Math.round(r*Math.sin(t));g.fill(x,y,x+1,y+1,cancel);}
        g.drawString(font,MARK_ONE,cx-font.width(MARK_ONE)/2,cy-9,cancel,false);g.drawString(font,MARK_TWO,cx-font.width(MARK_TWO)/2,cy+1,cancel,false);
        for(int k=0;k<4;k++)for(int x=cx+r+1;x<EW-6;x++){int y=cy-9+k*6+(int)Math.round(1.5*Math.sin(x*.7+k));g.fill(x,y,x+1,y+1,cancel);}
        // Who it is to, typed.
        g.drawString(font,TO_ONE,58,56,ink,false);g.drawString(font,TO_TWO,58,67,ink,false);
    }
    private void sound(float pitch){Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN,pitch));}
    private void turn(){turned=!turned;before=0;sound(1.4F);}
    @Override public void renderBackground(GuiGraphics g,int mx,int my,float partial){}
    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        g.fill(0,0,width,height,0xB4000000);int z=zoom(width,height);var pose=g.pose();
        pose.pushPose();pose.translate((width-EW*z)/2F,(height-EH*z)/2F,0);pose.scale(z,z,1);envelope(g,font,aged,turned);pose.popPose();
        super.render(g,mx,my,partial);
    }
    @Override public boolean mouseClicked(double mx,double my,int button){
        int z=zoom(width,height),x=(width-EW*z)/2,y=(height-EH*z)/2;
        if(mx>=x&&mx<x+EW*z&&my>=y&&my<y+EH*z){turn();return true;}
        onClose();return true;
    }
    @Override public boolean keyPressed(int key,int scan,int mods){
        if(key==InputConstants.KEY_SPACE||key==InputConstants.KEY_RETURN||key==InputConstants.KEY_LEFT||key==InputConstants.KEY_RIGHT||key==InputConstants.KEY_A||key==InputConstants.KEY_D){turn();return true;}
        if(minecraft!=null&&minecraft.options.keyInventory.matches(key,scan)){onClose();return true;}
        return super.keyPressed(key,scan,mods);
    }
}
