package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.labyrinth.BorrascaArcade;
import io.github.knaitoe.theoldesthouse.network.BorrascaArcadePayload;
import io.github.knaitoe.theoldesthouse.network.BorrascaArcadeScorePayload;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/**
 * 0.4.436: a Borrasca drugstore cabinet's screen (labyrinth.BorrascaArcade has the rules, shared with the server). The game is drawn on
 * a 200 by 150 field of fat pixels and played with the movement keys (or the arrows) and the space bar. A game over, or the screen
 * closed early, sends the keys held on each tick and no score; the server plays the game again and keeps what it scores. Afterwards
 * the screen shows the cabinet's table with the reader's own initials where their score would sit.
 */
public final class BorrascaArcadeScreen extends Screen {
    static final int W=BorrascaArcade.W,H=BorrascaArcade.H;
    final int game;final long token;final String name;final List<String[]> table=new ArrayList<>();final Set<Integer> keys=new HashSet<>();
    final BorrascaArcade.Game play;
    /** The keys held on each tick, as runs ({@code count<<5|keys}), sent when the game is over. */
    final List<Integer> runs=new ArrayList<>();int held=-1,count;
    int ticks;boolean over,sent;

    BorrascaArcadeScreen(BorrascaArcadePayload p){
        super(Component.literal(BorrascaArcade.TITLES[Mth.clamp(p.game(),0,1)]));
        game=Mth.clamp(p.game(),0,1);token=p.token();name=p.name();
        for(Tag t:p.table().getList("Rows",Tag.TAG_COMPOUND)){var c=(CompoundTag)t;table.add(new String[]{c.getString("N"),Integer.toString(c.getInt("S"))});}
        play=BorrascaArcade.of(game,token,this::sound);
    }
    public static void accept(BorrascaArcadePayload p){
        var mc=Minecraft.getInstance();
        if(p.game()<0){if(mc.screen instanceof BorrascaArcadeScreen s){s.send();mc.setScreen(null);}return;}
        mc.setScreen(new BorrascaArcadeScreen(p));
    }
    @Override public boolean isPauseScreen(){return false;}
    @Override public void tick(){
        if(over){ticks++;return;}
        int k=mask();if(k==held)count++;else{flush();held=k;count=1;}
        play.tick(k);ticks++;
        if(play.over()||play.ticks>=BorrascaArcade.LONGEST[game]){over=true;send();beep(SoundEvents.NOTE_BLOCK_BIT.value(),.5F);}
    }
    void flush(){if(count>0)runs.add(count<<5|held);count=0;}
    void send(){if(sent)return;sent=true;flush();PacketDistributor.sendToServer(new BorrascaArcadeScorePayload(token,List.copyOf(runs)));}
    @Override public void removed(){send();super.removed();}
    @Override public boolean keyPressed(int key,int scan,int mods){keys.add(key);return super.keyPressed(key,scan,mods)||key!=GLFW.GLFW_KEY_ESCAPE;}
    @Override public boolean keyReleased(int key,int scan,int mods){keys.remove(key);return true;}
    int mask(){
        int k=0;
        if(keys.contains(GLFW.GLFW_KEY_A)||keys.contains(GLFW.GLFW_KEY_LEFT))k|=BorrascaArcade.LEFT;
        if(keys.contains(GLFW.GLFW_KEY_D)||keys.contains(GLFW.GLFW_KEY_RIGHT))k|=BorrascaArcade.RIGHT;
        if(keys.contains(GLFW.GLFW_KEY_W)||keys.contains(GLFW.GLFW_KEY_UP))k|=BorrascaArcade.UP;
        if(keys.contains(GLFW.GLFW_KEY_S)||keys.contains(GLFW.GLFW_KEY_DOWN))k|=BorrascaArcade.DOWN;
        if(keys.contains(GLFW.GLFW_KEY_SPACE))k|=BorrascaArcade.SPACE;
        return k;
    }
    static final SoundEvent[] SOUNDS={SoundEvents.NOTE_BLOCK_HAT.value(),SoundEvents.NOTE_BLOCK_CHIME.value(),SoundEvents.NOTE_BLOCK_BASEDRUM.value(),SoundEvents.GENERIC_SPLASH,SoundEvents.NOTE_BLOCK_DIDGERIDOO.value()};
    void sound(int kind,float pitch){beep(SOUNDS[Mth.clamp(kind,0,SOUNDS.length-1)],pitch);}
    void beep(SoundEvent s,float pitch){Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(s,pitch,.35F));}

    @Override public void renderBackground(GuiGraphics g,int mx,int my,float partial){g.fill(0,0,width,height,0xF0000000);}
    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        renderBackground(g,mx,my,partial);
        float scale=Math.max(1,Math.min((width-24)/(float)W,(height-48)/(float)H));
        int sw=Math.round(W*scale),sh=Math.round(H*scale),x0=(width-sw)/2,y0=(height-sh)/2+8;
        g.fill(x0-6,y0-6,x0+sw+6,y0+sh+6,game==BorrascaArcade.SEAM?0xFF2A1E12:0xFF12261C);g.fill(x0-2,y0-2,x0+sw+2,y0+sh+2,0xFF000000);
        var title=BorrascaArcade.TITLES[game];g.drawString(font,title,(width-font.width(title))/2,y0-20,game==BorrascaArcade.SEAM?0xFFF0C040:0xFF90E0B0,false);
        g.enableScissor(x0,y0,x0+sw,y0+sh);
        g.pose().pushPose();g.pose().translate(x0,y0,0);g.pose().scale(scale,scale,1);
        draw(g,font,play,ticks);
        if(over)table(g);
        g.pose().popPose();
        g.disableScissor();
        String hint=over?"GAME OVER   [Esc]":game==BorrascaArcade.SEAM?"[W][S] change rails   [Space] hop   [Esc] leave":"[A][D] steer   hold [Space] reel in   [Esc] leave";
        g.drawString(font,hint,(width-font.width(hint))/2,y0+sh+10,0xFFA09078,false);
    }
    /** The table, after a game: the reader's initials where their score would sit. */
    void table(GuiGraphics g){
        rect(g,40,20,120,110,0xE0000000);centred(g,font,"BEST",28,0xFFFFE060);
        int mine=play.score();boolean placed=false;int row=0;
        for(int i=0;i<table.size()&&row<BorrascaArcade.ROWS;i++){
            if(!placed&&mine>Integer.parseInt(table.get(i)[1])){text(g,font,String.format("%d  %s  %5d",row+1,name,mine),58,44+row*13,0xFF60FFFF);placed=true;row++;if(row>=BorrascaArcade.ROWS)break;}
            text(g,font,String.format("%d  %s  %5s",row+1,table.get(i)[0],table.get(i)[1]),58,44+row*13,0xFFE0E0E0);row++;
        }
    }

    /** One frame of either game at the pose's origin, on its own 200 by 150 field. Also used by the visual proof. */
    public static void draw(GuiGraphics g,Font font,BorrascaArcade.Game play,int ticks){
        if(play instanceof BorrascaArcade.DeepSeam s)seam(g,font,s,ticks);
        else if(play instanceof BorrascaArcade.BigCat c)cat(g,font,c,ticks);
    }
    static void text(GuiGraphics g,Font font,String s,int x,int y,int colour){g.drawString(font,s,x,y,colour,false);}
    static void centred(GuiGraphics g,Font font,String s,int y,int colour){g.drawString(font,s,(W-font.width(s))/2,y,colour,false);}
    static void rect(GuiGraphics g,float x,float y,float w,float h,int colour){g.fill(Math.round(x),Math.round(y),Math.round(x+w),Math.round(y+h),colour);}

    // ------------------------------------------------------------------------------------------------ DEEP SEAM
    static void seam(GuiGraphics g,Font font,BorrascaArcade.DeepSeam s,int ticks){
        rect(g,0,0,W,H,0xFF0E0A07);
        // The seam's walls going by: a scatter of lighter rock and coal glints.
        for(int i=0;i<60;i++){int hash=i*1103515245+12345;float x=(((hash>>>8)%(W+20))-s.dist*.6F)%(W+20);if(x<0)x+=W+20;int y=((hash>>>3)*31)%H;if(y<0)y+=H;rect(g,x-10,y,i%3==0?3:2,2,i%7==0?0xFF3A3A44:0xFF2A2018);}
        for(int lane=0;lane<3;lane++){
            int y=BorrascaArcade.DeepSeam.RAILS[lane];
            for(float x=-((s.dist)%12);x<W;x+=12)rect(g,x,y+2,7,2,0xFF3E2A18);
            rect(g,0,y,W,1,0xFF8A8C90);rect(g,0,y+3,W,1,0xFF6A6C70);
        }
        for(var t:s.things){
            int lane=(int)t[1],kind=(int)t[2];int y=BorrascaArcade.DeepSeam.RAILS[lane];
            switch(kind){
                case BorrascaArcade.DeepSeam.GAP->{rect(g,t[0],y-1,t[3],7,0xFF0E0A07);rect(g,t[0],y+5,t[3],10,0xFF000000);}
                case BorrascaArcade.DeepSeam.TIMBER->{rect(g,t[0],y-12,t[3],13,0xFF6A4A26);rect(g,t[0]+1,y-11,t[3]-2,1,0xFF8A6A3A);rect(g,t[0]-2,y-7,t[3]+4,2,0xFF4A3218);}
                default->{rect(g,t[0]-3,y-11,11,11,0x30F0C060);rect(g,t[0],y-8,5,7,0xFFF0C048);rect(g,t[0]+1,y-10,3,2,0xFF5A5040);}
            }
        }
        // The cart, hopping when it hops.
        float hop=s.air>0?Mth.sin(Mth.PI*s.air/BorrascaArcade.DeepSeam.HOP)*10:0;float cx=BorrascaArcade.DeepSeam.CART_X,cy=s.shown-hop;
        if(s.hurt%8<4){
            rect(g,cx-7,cy-9,14,7,0xFF4A4E56);rect(g,cx-6,cy-11,12,2,0xFF2A2020);for(int i=0;i<4;i++)rect(g,cx-5+i*3,cy-12,2,2,0xFF1A1414);
            rect(g,cx-6,cy-2,3,3,0xFF202024);rect(g,cx+3,cy-2,3,3,0xFF202024);
            rect(g,cx+7,cy-8,2,3,0xFFFFE8A0);
        }
        // The light: the further from the cart's lamp, the darker, and the light shrinks as it goes.
        float reach=24+s.light*1.1F;
        for(int ring=0;ring<6;ring++){float r=reach*(1-ring*.13F);shadeOutside(g,cx+10-r*.5F,cy-r*.7F,cx+10+r*1.6F,cy+r*.7F,0x24000000);}
        text(g,font,Integer.toString(s.score()),4,4,0xFFF0E8D0);
        for(int i=0;i<s.lives;i++){rect(g,W-12-i*12,6,9,4,0xFF8A8E96);rect(g,W-11-i*12,10,2,2,0xFF505058);rect(g,W-6-i*12,10,2,2,0xFF505058);}
        rect(g,W-46,16,40,3,0xFF201810);rect(g,W-46,16,s.light*.4F,3,s.light<25?0xFFE06040:0xFFF0C048);
    }
    /** Darkens everything outside a box, to build the lamp's light out of rings. */
    static void shadeOutside(GuiGraphics g,float x0,float y0,float x1,float y1,int colour){
        rect(g,0,0,W,Math.max(0,y0),colour);rect(g,0,y1,W,H-y1,colour);rect(g,0,Math.max(0,y0),Math.max(0,x0),y1-Math.max(0,y0),colour);rect(g,x1,Math.max(0,y0),W-x1,y1-Math.max(0,y0),colour);
    }

    // ------------------------------------------------------------------------------------------------ BIG CAT
    static final int[][] FISH={{7,3,0xFFC8C040},{10,4,0xFF4A7A3A},{22,6,0xFF6A5E50},{9,5,0xFF2E4A26}};
    static void cat(GuiGraphics g,Font font,BorrascaArcade.BigCat c,int ticks){
        float dawn=Mth.clamp(c.ticks/(float)BorrascaArcade.LONGEST[BorrascaArcade.CAT],0,1);
        int sky=0xFF000000|lerp(0x0A0E24,0x5A4A6A,dawn);rect(g,0,0,W,BorrascaArcade.BigCat.SURFACE,sky);
        rect(g,160,6,6,6,0xFFE8E4C8);for(int i=0;i<10;i++)rect(g,(i*41)%W,(i*7)%14+2,1,1,0xFFC0C0D0);
        for(int y=(int)BorrascaArcade.BigCat.SURFACE;y<H;y+=6){int d=(y-24)*3/2;rect(g,0,y,W,6,0xFF000000|Math.max(0,20-d/8)<<16|Math.max(6,48-d/4)<<8|Math.max(10,60-d/4));}
        rect(g,0,BorrascaArcade.BigCat.BOTTOM+6,W,H,0xFF2A2014);for(int x=0;x<W;x+=9)rect(g,x,BorrascaArcade.BigCat.BOTTOM+5,5,2,0xFF3A2C1A);
        rect(g,0,BorrascaArcade.BigCat.SURFACE,W,1,0xFF6080A0);
        // The johnboat, and the line from the rod's tip to the hook.
        float bx=BorrascaArcade.BigCat.BOAT_X;rect(g,bx-15,17,30,6,0xFF5A4630);rect(g,bx-13,15,26,2,0xFF7A6040);rect(g,bx-3,7,5,8,0xFF3A3030);rect(g,bx-2,4,3,3,0xFFD0A880);
        float tipX=bx+14,tipY=6;rect(g,bx+1,8,13,1,0xFF8A6A40);
        int steps=Math.max(1,(int)Math.max(Math.abs(c.hx-tipX),Math.abs(c.hy-tipY)));for(int i=0;i<=steps;i+=2){float f=i/(float)steps;rect(g,tipX+(c.hx-tipX)*f,tipY+(c.hy-tipY)*f,1,1,0x90E0E0E0);}
        for(var f:c.swimmers)fish(g,(int)f[2],f[0],f[1],f[3]>0,ticks);
        if(c.hooked>=0)fish(g,c.hooked,c.hx,c.hy,true,ticks);
        else{rect(g,c.hx-1,c.hy-1,2,4,0xFFC8C8D0);if(c.worms>0&&c.hurt%6<3)rect(g,c.hx-2,c.hy+2,4,2,0xFFE08090);}
        text(g,font,Integer.toString(c.score),4,4,0xFFF0F0F0);
        for(int i=0;i<c.worms;i++)rect(g,W-10-i*7,6,5,2,0xFFE08090);
        if(c.hooked>=0){rect(g,4,140,62,6,0xFF101010);rect(g,5,141,c.tension*.6F,4,c.tension>70?0xFFE04030:c.tension>40?0xFFE0B040:0xFF60C060);}
    }
    static void fish(GuiGraphics g,int kind,float x,float y,boolean right,int ticks){
        int w=FISH[kind][0],h=FISH[kind][1],colour=FISH[kind][2];float x0=x-w/2F,y0=y-h/2F;
        rect(g,x0,y0,w,h,colour);rect(g,right?x0-3:x0+w,y0+(ticks/6%2==0?0:1),3,h-1,colour);
        rect(g,right?x0+w-2:x0+1,y0+1,1,1,0xFF101010);
        if(kind==BorrascaArcade.BigCat.CATFISH){rect(g,right?x0+w:x0-4,y0+h-2,4,1,0xFF9A8E7E);rect(g,right?x0+w:x0-4,y0+h,3,1,0xFF9A8E7E);rect(g,x0+4,y0-1,w-8,1,0xFF4A4036);}
        if(kind==BorrascaArcade.BigCat.TURTLE){rect(g,x0+1,y0,w-2,2,0xFF4A6A36);rect(g,right?x0+w:x0-2,y0+2,2,2,0xFF5A7A46);}
        if(kind==BorrascaArcade.BigCat.PERCH)for(int i=1;i<w-1;i+=2)rect(g,x0+i,y0,1,h,0xFF8A8A20);
    }
    static int lerp(int a,int b,float t){int r=(int)Mth.lerp(t,a>>16&255,b>>16&255),gr=(int)Mth.lerp(t,a>>8&255,b>>8&255),bl=(int)Mth.lerp(t,a&255,b&255);return r<<16|gr<<8|bl;}
}
