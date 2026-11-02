package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.labyrinth.Polybius;
import io.github.knaitoe.theoldesthouse.labyrinth.PolybiusGames;
import io.github.knaitoe.theoldesthouse.network.PolybiusPayload;
import io.github.knaitoe.theoldesthouse.network.PolybiusScorePayload;
import java.util.*;
import net.minecraft.client.Minecraft;
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
 * 0.4.217, Polybius: a cabinet's screen. Three original games and the black cabinet's, each drawn on a 200 by 150 field of fat
 * pixels and played with the movement keys (or the arrows) and the space bar. 0.4.225: the games' rules are
 * {@link PolybiusGames}, shared with the server; this screen plays them, draws them and keeps the keys held on every tick, and a
 * game over (or the screen closed early) sends those keys, not a score. The server plays the game again from the same seed and
 * keeps the score it gets. Nothing here flashes: the black cabinet's
 * colours drift slowly, and its words come and go (held longer and fainter for a reader who hides lightning flashes).
 *
 * <ul>
 * <li><b>Mothlight</b>: a moth round a porch lamp. Gather the motes of light drifting in to it; bats cross the dark; the lamp
 * pulls at the moth, and touching it burns. Three wings' worth of lives.</li>
 * <li><b>Undertow</b>: a swimmer in a black lake, under what the current brings down. Catch the bubbles coming up before the
 * air runs out.</li>
 * <li><b>Signal</b>: a needle on a radio dial. Hold the space bar against the drift to keep it inside the station as the station
 * wanders and narrows.</li>
 * <li><b>The black cabinet</b>: a spiral that turns, and a dot that rides out along it; turn the dot to keep it on the line.</li>
 * </ul>
 */
public final class PolybiusScreen extends Screen {
    static final int W=PolybiusGames.W,H=PolybiusGames.H;
    static final String[] TITLES={"MOTHLIGHT","UNDERTOW","SIGNAL",""};
    final int game;final long token;final String name;final int plays;final boolean taken;final List<String[]> table=new ArrayList<>();
    /** Randomness for what is only drawn (the dial's snow); the game's own comes from its seed, on both sides. */
    final Random fx=new Random();final Set<Integer> keys=new HashSet<>();
    private final PolybiusGames.Game play;
    /** The keys held on each tick, as runs ({@code count<<5|keys}), sent when the game is over. */
    final List<Integer> runs=new ArrayList<>();int held=-1,count;
    int ticks;boolean over,sent;

    PolybiusScreen(PolybiusPayload p){
        super(Component.literal(p.game()<3?TITLES[p.game()]:"..."));
        game=p.game();token=p.token();name=p.name();plays=p.plays();taken=p.taken();
        for(Tag t:p.table().getList("Rows",Tag.TAG_COMPOUND)){var c=(CompoundTag)t;table.add(new String[]{c.getString("Name"),Integer.toString(c.getInt("Score"))});}
        play=PolybiusGames.of(game,token,name,taken,this::sound);
    }
    public static void accept(PolybiusPayload p){
        var mc=Minecraft.getInstance();
        if(p.game()<0){if(mc.screen instanceof PolybiusScreen s){s.send();mc.setScreen(null);}return;}
        mc.setScreen(new PolybiusScreen(p));
    }
    @Override public boolean isPauseScreen(){return false;}
    @Override public void tick(){
        if(over){ticks++;return;}
        int k=mask();if(k==held)count++;else{flush();held=k;count=1;}
        play.tick(k);ticks++;
        // A game ends at its longest, before the server closes the session.
        if(play.over()||play.ticks>=Polybius.LONGEST[game]){over=true;send();beep(SoundEvents.NOTE_BLOCK_BIT.value(),.5F);}
    }
    void flush(){if(count>0)runs.add(count<<5|held);count=0;}
    void send(){if(sent)return;sent=true;flush();PacketDistributor.sendToServer(new PolybiusScorePayload(token,List.copyOf(runs)));}
    @Override public void removed(){send();super.removed();}
    @Override public boolean keyPressed(int key,int scan,int mods){keys.add(key);return super.keyPressed(key,scan,mods)||key!=GLFW.GLFW_KEY_ESCAPE;}
    @Override public boolean keyReleased(int key,int scan,int mods){keys.remove(key);return true;}
    int mask(){
        int k=0;if(keys.contains(GLFW.GLFW_KEY_A)||keys.contains(GLFW.GLFW_KEY_LEFT))k|=PolybiusGames.LEFT;if(keys.contains(GLFW.GLFW_KEY_D)||keys.contains(GLFW.GLFW_KEY_RIGHT))k|=PolybiusGames.RIGHT;
        if(keys.contains(GLFW.GLFW_KEY_W)||keys.contains(GLFW.GLFW_KEY_UP))k|=PolybiusGames.UP;if(keys.contains(GLFW.GLFW_KEY_S)||keys.contains(GLFW.GLFW_KEY_DOWN))k|=PolybiusGames.DOWN;
        if(keys.contains(GLFW.GLFW_KEY_SPACE))k|=PolybiusGames.SPACE;return k;
    }
    static final SoundEvent[] SOUNDS={SoundEvents.NOTE_BLOCK_CHIME.value(),SoundEvents.NOTE_BLOCK_HARP.value(),SoundEvents.NOTE_BLOCK_BELL.value(),SoundEvents.NOTE_BLOCK_SNARE.value(),SoundEvents.NOTE_BLOCK_BASS.value()};
    void sound(int kind,float pitch){beep(SOUNDS[kind],pitch);}
    void beep(SoundEvent s,float pitch){Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(s,pitch,.35F));}

    @Override public void renderBackground(GuiGraphics g,int mx,int my,float partial){g.fill(0,0,width,height,0xF0000000);}
    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        renderBackground(g,mx,my,partial);
        float scale=Math.max(1,Math.min((width-24)/(float)W,(height-48)/(float)H));
        int sw=Math.round(W*scale),sh=Math.round(H*scale),x0=(width-sw)/2,y0=(height-sh)/2+8;
        // The bezel, and the marquee over it.
        g.fill(x0-6,y0-6,x0+sw+6,y0+sh+6,game==3?0xFF050505:0xFF1A1424);g.fill(x0-2,y0-2,x0+sw+2,y0+sh+2,0xFF000000);
        if(game<3){var title=TITLES[game];g.drawString(font,title,(width-font.width(title))/2,y0-20,0xFFE070F0,false);}
        g.enableScissor(x0,y0,x0+sw,y0+sh);
        g.pose().pushPose();g.pose().translate(x0,y0,0);g.pose().scale(scale,scale,1);
        switch(play){
            case PolybiusGames.Moth m->moth(g,m);
            case PolybiusGames.Undertow u->undertow(g,u);
            case PolybiusGames.Signal s->signal(g,s);
            case PolybiusGames.Spiral s->spiral(g,s);
            default->{}
        }
        if(over&&game<3)table(g);
        g.pose().popPose();
        g.disableScissor();
        String hint=over?(game<3?"GAME OVER   [Esc]":""):game==2?"[Space] hold   [Esc] leave":game==3?"[A] [D]   keep it on the line":"[W][A][S][D]   [Esc] leave";
        if(!hint.isEmpty())g.drawString(font,hint,(width-font.width(hint))/2,y0+sh+10,0xFF8A80A0,false);
    }
    void text(GuiGraphics g,String s,int x,int y,int colour){g.drawString(font,s,x,y,colour,false);}
    void centred(GuiGraphics g,String s,int y,int colour){g.drawString(font,s,(W-font.width(s))/2,y,colour,false);}
    void rect(GuiGraphics g,float x,float y,float w,float h,int colour){g.fill(Math.round(x),Math.round(y),Math.round(x+w),Math.round(y+h),colour);}
    /** The table, after a game: where the reader's score would sit. */
    void table(GuiGraphics g){
        rect(g,30,14,140,122,0xE0000000);centred(g,"HIGH SCORES",20,0xFFFFE060);
        int mine=play.score();boolean placed=false;int row=0;
        for(int i=0;i<table.size()&&row<10;i++){
            if(!placed&&mine>Integer.parseInt(table.get(i)[1])){text(g,String.format("%2d %-12s %6d",row+1,name,mine),38,34+row*10,0xFF60FFFF);placed=true;row++;if(row>=10)break;}
            if(table.get(i)[0].equals(name))continue;
            text(g,String.format("%2d %-12s %6s",row+1,table.get(i)[0],table.get(i)[1]),38,34+row*10,0xFFE0E0E0);row++;
        }
        if(!placed&&row<10&&mine>0)text(g,String.format("%2d %-12s %6d",row+1,name,mine),38,34+row*10,0xFF60FFFF);
    }

    // ------------------------------------------------------------------------------------------------ Mothlight
    void moth(GuiGraphics g,PolybiusGames.Moth m){
        float LX=PolybiusGames.Moth.LX,LY=PolybiusGames.Moth.LY,LAMP=PolybiusGames.Moth.LAMP;
        rect(g,0,0,W,H,0xFF06060E);
        for(int r=26;r>LAMP;r-=4)rect(g,LX-r,LY-r*.8F,r*2,r*1.6F,0x10F0D080);
        rect(g,LX-4,LY-5,8,10,0xFFFFE8A0);rect(g,LX-2,LY-9,4,4,0xFF806040);
        for(var mo:m.motes)rect(g,mo[0]-1,mo[1]-1,2,2,0xFFFFF0B0);
        for(var b:m.bats){boolean up=(ticks/4)%2==0;rect(g,b[0]-1,b[1]-1,3,3,0xFF302838);rect(g,b[0]-4,b[1]+(up?-2:0),3,2,0xFF302838);rect(g,b[0]+2,b[1]+(up?-2:0),3,2,0xFF302838);}
        if(m.hurt%8<4){rect(g,m.x-1,m.y-1,3,3,0xFFC8B8A0);rect(g,m.x-3,m.y-2+(ticks/3%2),2,3,0xFFE8DCC8);rect(g,m.x+2,m.y-2+(ticks/3%2),2,3,0xFFE8DCC8);}
        text(g,Integer.toString(m.score),4,4,0xFFFFFFFF);for(int i=0;i<m.lives;i++)rect(g,W-10-i*8,5,5,4,0xFFE8DCC8);
    }

    // ------------------------------------------------------------------------------------------------ Undertow
    void undertow(GuiGraphics g,PolybiusGames.Undertow u){
        float Y=PolybiusGames.Undertow.Y;
        for(int y=0;y<H;y+=10)rect(g,0,y,W,10,0xFF000000|(8+y/12)<<8|(12+y/8));
        for(var d:u.debris){rect(g,d[0],d[1],d[2],6,0xFF4A3A2A);rect(g,d[0]+1,d[1]+1,d[2]-2,1,0xFF6A5A42);}
        for(var b:u.bubbles){rect(g,b[0]-2,b[1]-2,4,4,0x90A0D8F0);rect(g,b[0]-1,b[1]-1,1,1,0xFFFFFFFF);}
        if(u.hurt%6<3){rect(g,u.x-3,Y-2,6,4,0xFFD8C0A0);rect(g,u.x-5+(ticks/5%2)*2,Y+2,2,3,0xFFD8C0A0);rect(g,u.x+3-(ticks/5%2)*2,Y+2,2,3,0xFFD8C0A0);}
        rect(g,4,4,52,5,0xFF203040);rect(g,5,5,Math.max(0,u.air)/2,3,u.air<25?0xFFE06060:0xFF80D0F0);
        text(g,Integer.toString(u.score),W-4-font.width(Integer.toString(u.score)),3,0xFFFFFFFF);
    }

    // ------------------------------------------------------------------------------------------------ Signal
    void signal(GuiGraphics g,PolybiusGames.Signal s){
        rect(g,0,0,W,H,0xFF0C0A08);
        rect(g,8,60,184,30,0xFF2A2218);rect(g,10,62,180,26,0xFFD8C890);
        for(int i=0;i<=18;i++)rect(g,10+i*10,i%2==0?62:66,1,i%2==0?8:4,0xFF3A3020);
        float half=s.width()/2;rect(g,10+s.centre-half,74,s.width(),12,0x8060A040);
        rect(g,10+s.needle,58,1,34,0xFFC02018);
        text(g,"MHz",168,94,0xFFA09070);
        for(int i=0;i<3;i++)rect(g,W-14-i*8,8,5,5,s.off>i*20?0xFF402018:0xFFE04030);
        text(g,Integer.toString(s.score),6,6,0xFFFFFFFF);
        // the snow, when the station is lost
        if(Math.abs(s.needle-s.centre)>half)for(int i=0;i<40;i++)rect(g,fx.nextInt(W),fx.nextInt(50),1,1,0x60FFFFFF);
    }

    // ------------------------------------------------------------------------------------------------ the black cabinet
    int colour(float t){float h=(ticks*.002F+t)%1;int r=(int)(80+60*Mth.sin(h*Mth.TWO_PI)),gr=(int)(170+60*Mth.sin(h*Mth.TWO_PI+2)),b=(int)(150+80*Mth.sin(h*Mth.TWO_PI+4));return 0xFF000000|r<<16|gr<<8|b;}
    void spiral(GuiGraphics g,PolybiusGames.Spiral s){
        float CX=PolybiusGames.Spiral.CX,CY=PolybiusGames.Spiral.CY,A=PolybiusGames.Spiral.A,B=PolybiusGames.Spiral.B,TURNS=PolybiusGames.Spiral.TURNS;
        rect(g,0,0,W,H,0xFF000000);
        for(float t=0;t<TURNS*Mth.TWO_PI;t+=.045F){float rr=A+B*t,a=t+s.rot;rect(g,CX+rr*Mth.cos(a),CY+rr*Mth.sin(a),1,1,colour(t/40));}
        rect(g,CX+s.r*Mth.cos(s.phi)-1,CY+s.r*Mth.sin(s.phi)-1,3,3,0xFFFFFFFF);
        boolean gentle=Minecraft.getInstance().options.hideLightningFlash().get();
        if(s.word!=null&&s.ticks-s.wordAt<(gentle?14:2))centred(g,s.word,(int)CY-4,gentle?0x40FFFFFF:0xFFFFFFFF);
        if(taken&&s.ticks>=90){rect(g,0,0,W,H,0xC0000000);centred(g,name.toUpperCase(Locale.ROOT),60,0xFFB0FFE0);centred(g,"PLAY AGAIN",76,0xFFB0FFE0);}
        String count=String.format("%06d",s.score*7+plays*1000);text(g,count,4,4,0xFF406050);
    }
}
