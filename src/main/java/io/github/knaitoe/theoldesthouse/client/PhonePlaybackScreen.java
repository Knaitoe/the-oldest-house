package io.github.knaitoe.theoldesthouse.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * The recovered phone plays its clip back (0.4.121). Each frame the server stored (when, and what the lens faced) is shown
 * in turn on the phone's cracked screen as a small, grainy night picture of that thing: the sky over the lake, the drowned
 * steeple, the canoe's boards, the dark bank, the turn as it fell, and the same sky from under the water, which it holds.
 * The pictures are drawn here from the frame's own description; nothing is read from the world.
 */
public final class PhonePlaybackScreen extends Screen {
    /** The phone's picture is this many cells across and down; each frame shows for two and a half seconds. */
    static final int COLS=96,ROWS=54,FRAME=50,FADE=8;
    enum Shot{SKY,STEEPLE,BOARDS,BANK,TURN,UNDER,STATIC}
    private final String name;
    private final ListTag frames;
    private int age,proof=-1;
    public PhonePlaybackScreen(CompoundTag clip){super(Component.literal("Recovered recording"));name=clip.getString("Name");frames=clip.getList("Frames",Tag.TAG_COMPOUND);}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void tick(){age++;}
    /** For the native proof: hold one frame, fully faded in. */
    void proof(int frame){proof=frame;}

    static Shot shot(String image){
        if(image.startsWith("Night sky"))return Shot.SKY;
        if(image.startsWith("The drowned steeple"))return Shot.STEEPLE;
        if(image.startsWith("The canoe's wet boards"))return Shot.BOARDS;
        if(image.startsWith("Dark water"))return Shot.BANK;
        if(image.startsWith("The image turns"))return Shot.TURN;
        if(image.startsWith("The same sky"))return Shot.UNDER;
        return Shot.STATIC;
    }
    static float hash(int x,int y,int seed){int h=x*374761393+y*668265263+seed*1442695041;h=(h^(h>>>13))*1274126177;return ((h^(h>>>16))&0xffff)/65535F;}
    static int rgb(float r,float g,float b){return 0xff000000|(Mth.clamp((int)(r*255),0,255)<<16)|(Mth.clamp((int)(g*255),0,255)<<8)|Mth.clamp((int)(b*255),0,255);}
    static int mix(int a,int b,float k){
        int r=(int)Mth.lerp(k,a>>16&255,b>>16&255),g=(int)Mth.lerp(k,a>>8&255,b>>8&255),bl=(int)Mth.lerp(k,a&255,b&255);return 0xff000000|r<<16|g<<8|bl;
    }
    /** One cell of a picture: u across and v down, both 0 to 1, at time t in ticks. */
    static int cell(Shot shot,float u,float v,float t,int x,int y){
        float aspect=16F/9F;
        switch(shot){
            case SKY->{float sky=1-v;int c=rgb(.02F+.03F*v,.03F+.04F*v,.07F+.07F*v);
                if(hash(x,y,7)<.014F)c=mix(c,0xffd8dcef,.55F+.45F*hash(x,y,8));
                float dx=(u-.72F)*aspect,dy=v-.26F,d=(float)Math.sqrt(dx*dx+dy*dy);if(d<.075F)return mix(0xffe6e2cc,0xffbcb8a4,hash(x,y,9)*.4F+(d>.06F?.4F:0));if(d<.2F)c=mix(c,0xff3a4256,(.2F-d)/.2F*.5F*sky);return c;}
            case STEEPLE->{if(v<.16F)return v<.12F&&hash(x,0,3)*.04F+.08F<v?0xff030504:rgb(.03F,.04F,.07F);
                float depth=(v-.16F)/.84F;int c=rgb(.02F+.03F*(1-depth),.06F+.05F*(1-depth),.08F+.05F*(1-depth));
                if(Mth.sin(v*90+t*.12F+u*6)>.93F)c=mix(c,0xff3c5658,.5F);
                float a=Math.abs(u-.5F);int stone=rgb(.13F,.19F,.17F),shade=rgb(.06F,.1F,.09F);
                // The cross, the spire, then the belfry with its dark arch and the bell's glint, all under the water.
                if(v>.34F&&v<.40F&&a<.004F)c=mix(c,stone,.8F*(1-depth));
                if(v>.36F&&v<.372F&&a<.016F)c=mix(c,stone,.8F*(1-depth));
                float spire=Mth.clamp((v-.40F)/.22F,0,1)*.05F;if(v>.40F&&v<.62F&&a<spire)c=mix(shade,stone,(1-a/Math.max(.001F,spire))*.7F);
                if(v>=.62F&&a<.06F){c=mix(shade,stone,.55F-a*4);if(v>.67F&&v<.8F&&a<.025F&&(v>.69F||a<.02F))c=0xff050a0a;if(v>.74F&&v<.78F&&a<.009F)c=0xffb39a52;if(Math.abs(v-.645F)<.006F&&a<.065F)c=stone;}
                float wob=Mth.sin(v*55+t*.3F)*.012F+Mth.sin(v*23-t*.17F)*.008F;if(Math.abs(u-.66F-wob)<.006F+.012F*(1-depth)&&Mth.sin(v*61-t*.4F)>-.3F)c=mix(c,0xffb8bca8,.5F*(1-depth));return c;}
            case BOARDS->{if(u<.06F||u>.94F)return mix(rgb(.03F,.07F,.09F),0xff22383c,Mth.sin(v*50+t*.2F)>.85F?.6F:0);
                if(u<.09F||u>.91F)return mix(rgb(.16F,.11F,.07F),rgb(.28F,.2F,.12F),.5F+.5F*Mth.sin(v*12));
                // Planks seven cells wide run the canoe's length; three ribs cross them, each with its shadow.
                int plank=y/7;if(y%7==0)return 0xff0e0905;
                float tone=.86F+.1F*Mth.sin(u*23+plank*2.3F+Mth.sin(u*6+plank)*2)+.06F*(hash(plank,0,4)-.5F);int c=rgb(.25F*tone,.17F*tone,.1F*tone);
                if(y%7==1)c=mix(c,0xff4a3a28,.35F);
                for(float rib:new float[]{.24F,.5F,.76F}){float r=Math.abs(u-rib);if(r<.02F)c=mix(rgb(.3F,.21F,.13F),rgb(.4F,.29F,.18F),1-r/.02F);else if(r<.03F&&u>rib)c=mix(c,0xff0e0905,.45F);}
                if(hash(x,y,11)<.05F)c=mix(c,0xff8a7a62,.4F);return c;}
            case BANK->{if(v<.3F)return mix(rgb(.03F,.04F,.07F),rgb(.09F,.11F,.15F),v/.3F);float tree=.31F+hash((int)(u*30),0,5)*.08F+Mth.sin(u*31)*.02F;if(v<.43F)return v>tree?0xff020303:rgb(.1F,.12F,.16F);
                int c=mix(rgb(.05F,.08F,.1F),rgb(.02F,.04F,.06F),(v-.43F)/.57F);if(Mth.sin(v*120+t*.15F+u*4)>.9F)c=mix(c,0xff2a3a3e,.5F);
                float wob=Mth.sin(v*50+t*.3F)*.01F;if(Math.abs(u-.3F-wob)<.006F+.02F*(v-.43F)&&Mth.sin(v*70-t*.25F)>0)c=mix(c,0xff9ea292,.3F);return c;}
            case TURN->{float side=(u-.5F)*Mth.cos(.6F)+(v-.5F)*Mth.sin(.6F);int c=side<0?rgb(.03F,.04F,.08F):rgb(.04F,.12F,.14F);
                if(Math.abs(side)<.02F)c=0xffcfe0e0;if(side>0&&hash(x,y,13)<.05F)c=mix(c,0xffd0e8f0,.7F);if(Mth.sin((u+v)*60)>.97F)c=mix(c,0xffffffff,.2F);return c;}
            case UNDER->{int c=rgb(.01F+.03F*(1-v),.07F+.06F*(1-v),.08F+.06F*(1-v));
                float dy=v-.38F,shift=Mth.sin(t*.25F+v*40)*.02F+Mth.sin(t*.11F+v*13)*.01F,dx=(u-.5F-shift)*aspect,d=(float)Math.sqrt(dx*dx+dy*dy);
                if(d<.12F&&((int)(v*ROWS)+(int)(t/3))%6!=0)c=mix(c,0xffe4ecd8,.9F-d*3);else if(d<.3F)c=mix(c,0xff467072,(.3F-d)/.3F*.5F);
                if(Mth.sin(u*30+t*.07F+Mth.sin(v*9))*Mth.sin(v*24-t*.05F)>.9F&&hash(x/2,y/2,17)<.5F)c=mix(c,0xff5e8c88,.2F);return c;}
            default->{float n=hash(x,y,(int)t);return rgb(n*.5F,n*.5F,n*.52F);}
        }
    }
    /** A picture drawn into a box: cells, then grain, scanlines, a dark edge and the crack across the glass. */
    static void picture(GuiGraphics g,int x0,int y0,int w,int h,Shot shot,Shot previous,float fade,float t){
        for(int y=0;y<ROWS;y++)for(int x=0;x<COLS;x++){
            float u=(x+.5F)/COLS,v=(y+.5F)/ROWS;int c=cell(shot,u,v,t,x,y);if(previous!=null&&fade<1)c=mix(cell(previous,u,v,t,x,y),c,fade);
            float grain=(hash(x,y,(int)t*31)-.5F)*.09F;float edge=Math.max(Math.abs(u-.5F)*1.6F,Math.abs(v-.5F)*1.9F);float dark=Mth.clamp((edge-.55F)*1.4F,0,.6F)+(y%3==0?.06F:0);
            c=mix(c,grain>0?0xffffffff:0xff000000,Math.abs(grain)+ (grain>0?0:dark));
            int px=x0+x*w/COLS,py=y0+y*h/ROWS,qx=x0+(x+1)*w/COLS,qy=y0+(y+1)*h/ROWS;g.fill(px,py,qx,qy,c);
        }
        // The crack: from the lower left corner into the glass, with two branches.
        int[][] crack={{0,46},{14,40},{23,41},{31,33},{40,30},{52,24}, {23,41},{27,50},{31,54}, {31,33},{29,22},{33,12}};
        for(int i=0;i+1<crack.length;i++){if(i==5||i==8)continue;line(g,x0+crack[i][0]*w/COLS,y0+crack[i][1]*h/ROWS,x0+crack[i+1][0]*w/COLS,y0+crack[i+1][1]*h/ROWS,0x8CDDE6E8);}
    }
    static void line(GuiGraphics g,int x0,int y0,int x1,int y1,int c){int steps=Math.max(Math.abs(x1-x0),Math.abs(y1-y0));for(int i=0;i<=steps;i++){int x=x0+(x1-x0)*i/Math.max(1,steps),y=y0+(y1-y0)*i/Math.max(1,steps);g.fill(x,y,x+1,y+1,c);}}

    int index(){return proof>=0?Math.min(proof,Math.max(0,frames.size()-1)):Math.min(age/FRAME,Math.max(0,frames.size()-1));}
    boolean ended(){return proof<0&&age>=frames.size()*FRAME;}

    @Override public void renderBackground(GuiGraphics g,int mouseX,int mouseY,float delta){g.fill(0,0,width,height,0xF0060708);}
    @Override public void render(GuiGraphics g,int mouseX,int mouseY,float delta){
        renderBackground(g,mouseX,mouseY,delta);
        int pw=Math.min(width-40,(height-70)*16/9),ph=pw*9/16,px=(width-pw)/2,py=(height-ph)/2-6;
        g.fill(px-10,py-22,px+pw+10,py+ph+30,0xFF141618);g.fill(px-9,py-21,px+pw+9,py+ph+29,0xFF1C1F22);
        g.drawString(font,"RECOVERED · "+name+" · Indian Lake",px,py-15,0xFFB9BDB8,false);
        if(frames.isEmpty()){g.fill(px,py,px+pw,py+ph,0xFF000000);g.drawCenteredString(font,"No recording.",px+pw/2,py+ph/2-4,0xFF9A9E9A);return;}
        int i=index();float t=age+delta;var frame=frames.getCompound(i);float into=proof>=0?FADE:(age%FRAME)+delta;
        Shot now=shot(frame.getString("Image")),before=i>0?shot(frames.getCompound(i-1).getString("Image")):null;
        picture(g,px,py,pw,ph,now,ended()?null:before,proof>=0||ended()?1:Mth.clamp(into/FADE,0,1),t);
        if(((int)t/10)%2==0)g.fill(px+8,py+8,px+13,py+13,0xFFE0453A);
        String time=String.format(java.util.Locale.ROOT,"00:00:%02d",frame.getInt("Tick")/20);g.drawString(font,time,px+18,py+7,0xFFF0EDE6,false);
        int bar=pw*(Math.min(age,frames.size()*FRAME))/Math.max(1,frames.size()*FRAME);g.fill(px,py+ph+4,px+pw,py+ph+6,0xFF2C3034);g.fill(px,py+ph+4,px+(proof>=0?pw*(i+1)/frames.size():bar),py+ph+6,0xFFD8D4CA);
        g.drawString(font,Component.literal(frame.getString("Image")).withStyle(net.minecraft.ChatFormatting.ITALIC),px,py+ph+11,0xFF9EA39E,false);
        if(ended())g.drawString(font,"END OF CLIP",px+pw-font.width("END OF CLIP")-6,py+7,0xFFF0EDE6,false);
    }
}
