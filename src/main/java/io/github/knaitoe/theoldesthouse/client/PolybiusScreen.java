package io.github.knaitoe.theoldesthouse.client;

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
 * pixels and played with the movement keys (or the arrows) and the space bar. A game over sends its score to the server, which
 * decides whether it goes on the table; closing the screen early sends what there is. Nothing here flashes: the black cabinet's
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
    static final int W=200,H=150;
    static final String[] TITLES={"MOTHLIGHT","UNDERTOW","SIGNAL",""};
    final int game;final long token;final String name;final int plays;final boolean taken;final List<String[]> table=new ArrayList<>();
    final Random random;final Set<Integer> keys=new HashSet<>();
    private final Game play;
    int ticks;boolean over,sent;

    PolybiusScreen(PolybiusPayload p){
        super(Component.literal(p.game()<3?TITLES[p.game()]:"..."));
        game=p.game();token=p.token();name=p.name();plays=p.plays();taken=p.taken();random=new Random(p.token());
        for(Tag t:p.table().getList("Rows",Tag.TAG_COMPOUND)){var c=(CompoundTag)t;table.add(new String[]{c.getString("Name"),Integer.toString(c.getInt("Score"))});}
        play=switch(game){case 0->new Moth();case 1->new Undertow();case 2->new Signal();default->new Spiral();};
    }
    public static void accept(PolybiusPayload p){
        var mc=Minecraft.getInstance();
        if(p.game()<0){if(mc.screen instanceof PolybiusScreen s){s.send();mc.setScreen(null);}return;}
        mc.setScreen(new PolybiusScreen(p));
    }
    @Override public boolean isPauseScreen(){return false;}
    @Override public void tick(){
        if(over){ticks++;return;}
        play.tick();ticks++;
        if(play.over()){over=true;send();beep(SoundEvents.NOTE_BLOCK_BIT.value(),.5F);}
    }
    void send(){if(sent)return;sent=true;PacketDistributor.sendToServer(new PolybiusScorePayload(token,Math.max(0,play.score()),ticks));}
    @Override public void removed(){send();super.removed();}
    @Override public boolean keyPressed(int key,int scan,int mods){keys.add(key);return super.keyPressed(key,scan,mods)||key!=GLFW.GLFW_KEY_ESCAPE;}
    @Override public boolean keyReleased(int key,int scan,int mods){keys.remove(key);return true;}
    boolean left(){return keys.contains(GLFW.GLFW_KEY_A)||keys.contains(GLFW.GLFW_KEY_LEFT);}
    boolean right(){return keys.contains(GLFW.GLFW_KEY_D)||keys.contains(GLFW.GLFW_KEY_RIGHT);}
    boolean up(){return keys.contains(GLFW.GLFW_KEY_W)||keys.contains(GLFW.GLFW_KEY_UP);}
    boolean down(){return keys.contains(GLFW.GLFW_KEY_S)||keys.contains(GLFW.GLFW_KEY_DOWN);}
    boolean push(){return keys.contains(GLFW.GLFW_KEY_SPACE)||up();}
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
        play.draw(g,partial);
        if(over&&game<3)table(g);
        g.pose().popPose();
        g.disableScissor();
        String hint=over?(game<3?"GAME OVER   [Esc]":""):game==2?"[Space] hold   [Esc] leave":game==3?"[A] [D]":"[W][A][S][D]   [Esc] leave";
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

    interface Game{void tick();void draw(GuiGraphics g,float partial);int score();boolean over();}

    // ------------------------------------------------------------------------------------------------ Mothlight
    final class Moth implements Game{
        float x=100,y=110;int lives=3,hurt,score;final List<float[]> motes=new ArrayList<>(),bats=new ArrayList<>();
        static final float LX=100,LY=70,LAMP=7;
        public void tick(){
            float speed=2;if(left())x-=speed;if(right())x+=speed;if(up())y-=speed;if(down())y+=speed;
            // The lamp pulls, a little, at anything with wings.
            float dx=LX-x,dy=LY-y,d=Mth.sqrt(dx*dx+dy*dy);if(d>1){x+=dx/d*.35F;y+=dy/d*.35F;}
            x=Mth.clamp(x,3,W-3);y=Mth.clamp(y,3,H-3);
            if(hurt>0)hurt--;
            if(d<LAMP&&hurt==0)hit();
            if(ticks%12==0){int edge=random.nextInt(4);motes.add(edge==0?new float[]{random.nextInt(W),0}:edge==1?new float[]{random.nextInt(W),H}:edge==2?new float[]{0,random.nextInt(H)}:new float[]{W,random.nextInt(H)});}
            for(var it=motes.iterator();it.hasNext();){var m=it.next();float mx=LX-m[0],my=LY-m[1],md=Mth.sqrt(mx*mx+my*my);
                if(md<LAMP){it.remove();continue;}m[0]+=mx/md*.6F+(random.nextFloat()-.5F)*.6F;m[1]+=my/md*.6F+(random.nextFloat()-.5F)*.6F;
                if(Math.abs(m[0]-x)<5&&Math.abs(m[1]-y)<5){it.remove();score+=10;beep(SoundEvents.NOTE_BLOCK_CHIME.value(),1.6F+random.nextFloat()*.3F);}}
            int every=Math.max(25,80-ticks/50);
            if(ticks%every==0){boolean fromLeft=random.nextBoolean();float by;do{by=8+random.nextInt(H-16);}while(Math.abs(by-LY)<12);bats.add(new float[]{fromLeft?-8:W+8,by,(fromLeft?1:-1)*Math.min(4,1.8F+ticks/1500F)});}
            for(var it=bats.iterator();it.hasNext();){var b=it.next();b[0]+=b[2];b[1]+=Mth.sin((ticks+b[1])*.3F)*.8F;if(b[0]<-12||b[0]>W+12){it.remove();continue;}
                if(hurt==0&&Math.abs(b[0]-x)<5&&Math.abs(b[1]-y)<4)hit();}
            if(ticks%10==0)score++;
        }
        void hit(){lives--;hurt=40;beep(SoundEvents.NOTE_BLOCK_BASS.value(),.6F);float dx=x-LX,dy=y-LY,d=Math.max(1,Mth.sqrt(dx*dx+dy*dy));x=LX+dx/d*24;y=LY+dy/d*24;}
        public void draw(GuiGraphics g,float partial){
            rect(g,0,0,W,H,0xFF06060E);
            for(int r=26;r>LAMP;r-=4)rect(g,LX-r,LY-r*.8F,r*2,r*1.6F,0x10F0D080);
            rect(g,LX-4,LY-5,8,10,0xFFFFE8A0);rect(g,LX-2,LY-9,4,4,0xFF806040);
            for(var m:motes)rect(g,m[0]-1,m[1]-1,2,2,0xFFFFF0B0);
            for(var b:bats){boolean up=(ticks/4)%2==0;rect(g,b[0]-1,b[1]-1,3,3,0xFF302838);rect(g,b[0]-4,b[1]+(up?-2:0),3,2,0xFF302838);rect(g,b[0]+2,b[1]+(up?-2:0),3,2,0xFF302838);}
            if(hurt%8<4){rect(g,x-1,y-1,3,3,0xFFC8B8A0);rect(g,x-3,y-2+(ticks/3%2),2,3,0xFFE8DCC8);rect(g,x+2,y-2+(ticks/3%2),2,3,0xFFE8DCC8);}
            text(g,Integer.toString(score),4,4,0xFFFFFFFF);for(int i=0;i<lives;i++)rect(g,W-10-i*8,5,5,4,0xFFE8DCC8);
        }
        public int score(){return score;}public boolean over(){return lives<=0;}
    }

    // ------------------------------------------------------------------------------------------------ Undertow
    final class Undertow implements Game{
        float x=100;float air=100;int hurt,score;final List<float[]> debris=new ArrayList<>(),bubbles=new ArrayList<>();
        static final float Y=135;
        public void tick(){
            if(left())x-=2.2F;if(right())x+=2.2F;x=Mth.clamp(x,6,W-6);
            if(hurt>0)hurt--;
            if(ticks%4==0)air--;
            int every=Math.max(8,26-ticks/200);
            if(ticks%every==0){float w=8+random.nextInt(13);debris.add(new float[]{random.nextInt((int)(W-w)),-8,w,Math.min(3.2F,1+ticks/2000F)});}
            if(ticks%30==0)bubbles.add(new float[]{8+random.nextInt(W-16),H+4});
            for(var it=debris.iterator();it.hasNext();){var d=it.next();d[1]+=d[3];if(d[1]>H){it.remove();continue;}
                if(hurt==0&&d[1]+6>Y-4&&d[1]<Y+4&&x+4>d[0]&&x-4<d[0]+d[2]){air-=20;hurt=20;beep(SoundEvents.NOTE_BLOCK_BASS.value(),.5F);}}
            for(var it=bubbles.iterator();it.hasNext();){var b=it.next();b[1]-=1.2F;b[0]+=Mth.sin((ticks+b[0])*.1F)*.4F;if(b[1]<-4){it.remove();continue;}
                if(Math.abs(b[0]-x)<6&&Math.abs(b[1]-Y)<6){it.remove();air=Math.min(100,air+25);score+=5;beep(SoundEvents.NOTE_BLOCK_HARP.value(),1.8F);}}
            if(ticks%5==0)score++;
        }
        public void draw(GuiGraphics g,float partial){
            for(int y=0;y<H;y+=10)rect(g,0,y,W,10,0xFF000000|(8+y/12)<<8|(12+y/8));
            for(var d:debris){rect(g,d[0],d[1],d[2],6,0xFF4A3A2A);rect(g,d[0]+1,d[1]+1,d[2]-2,1,0xFF6A5A42);}
            for(var b:bubbles){rect(g,b[0]-2,b[1]-2,4,4,0x90A0D8F0);rect(g,b[0]-1,b[1]-1,1,1,0xFFFFFFFF);}
            if(hurt%6<3){rect(g,x-3,Y-2,6,4,0xFFD8C0A0);rect(g,x-5+(ticks/5%2)*2,Y+2,2,3,0xFFD8C0A0);rect(g,x+3-(ticks/5%2)*2,Y+2,2,3,0xFFD8C0A0);}
            rect(g,4,4,52,5,0xFF203040);rect(g,5,5,Math.max(0,air)/2,3,air<25?0xFFE06060:0xFF80D0F0);
            text(g,Integer.toString(score),W-4-font.width(Integer.toString(score)),3,0xFFFFFFFF);
        }
        public int score(){return score;}public boolean over(){return air<=0;}
    }

    // ------------------------------------------------------------------------------------------------ Signal
    final class Signal implements Game{
        float needle=90,v;int off,score;float centre=90;
        float width(){return Math.max(14,44-ticks/60F);}
        public void tick(){
            centre=90+60*Mth.sin(ticks*.013F)*Mth.cos(ticks*.0071F)+15*Mth.sin(ticks*.041F);
            v+=push()?.18F:-.12F;v*=.96F;needle+=v;if(needle<0){needle=0;v=0;}if(needle>180){needle=180;v=0;}
            float half=width()/2,dist=Math.abs(needle-centre);
            if(dist<=half){score+=dist<half/3?2:1;if(ticks%20==0)beep(SoundEvents.NOTE_BLOCK_BELL.value(),1.2F+(1-dist/half)*.6F);}
            else{off++;if(ticks%6==0)beep(SoundEvents.NOTE_BLOCK_SNARE.value(),.6F+random.nextFloat()*.3F);}
        }
        public void draw(GuiGraphics g,float partial){
            rect(g,0,0,W,H,0xFF0C0A08);
            rect(g,8,60,184,30,0xFF2A2218);rect(g,10,62,180,26,0xFFD8C890);
            for(int i=0;i<=18;i++)rect(g,10+i*10,i%2==0?62:66,1,i%2==0?8:4,0xFF3A3020);
            float half=width()/2;rect(g,10+centre-half,74,width(),12,0x8060A040);
            rect(g,10+needle,58,1,34,0xFFC02018);
            text(g,"MHz",168,94,0xFFA09070);
            for(int i=0;i<3;i++)rect(g,W-14-i*8,8,5,5,off>i*20?0xFF402018:0xFFE04030);
            text(g,Integer.toString(score),6,6,0xFFFFFFFF);
            // the snow, when the station is lost
            if(Math.abs(needle-centre)>half)for(int i=0;i<40;i++)rect(g,random.nextInt(W),random.nextInt(50),1,1,0x60FFFFFF);
        }
        public int score(){return score;}public boolean over(){return off>60;}
    }

    // ------------------------------------------------------------------------------------------------ the black cabinet
    final class Spiral implements Game{
        static final float CX=100,CY=75,A=5,B=3.1F,TURNS=3;
        float rot,speed=.012F,r=8,phi;int offLine,score,loop;
        String word;int wordAt=-1,nextWord=90;
        final String[] words;
        Spiral(){words=new String[]{"OBEY","NO THOUGHT","STAY","SLEEP","CONSUME","PLAY AGAIN",name.toUpperCase(Locale.ROOT)};phi=line(r);}
        float line(float radius){return (radius-A)/B+rot;}
        public void tick(){
            if(taken&&ticks>=90)return;
            rot+=speed;r+=.16F;
            if(r>A+B*TURNS*Mth.TWO_PI-2){r=8;loop++;speed+=.004F;phi=line(r);}
            if(left())phi-=.07F;if(right())phi+=.07F;
            float d=Mth.wrapDegrees((phi-line(r))*Mth.RAD_TO_DEG)*Mth.DEG_TO_RAD;
            if(Math.abs(d)<.26F){offLine=0;score++;}else offLine++;
            if(ticks%20==0)beep(SoundEvents.NOTE_BLOCK_BASS.value(),.5F+loop*.05F);
            if(ticks>=nextWord){word=words[random.nextInt(words.length)];wordAt=ticks;nextWord=ticks+70+random.nextInt(60);}
        }
        int colour(float t){float h=(ticks*.002F+t)%1;int r=(int)(80+60*Mth.sin(h*Mth.TWO_PI)),gr=(int)(170+60*Mth.sin(h*Mth.TWO_PI+2)),b=(int)(150+80*Mth.sin(h*Mth.TWO_PI+4));return 0xFF000000|r<<16|gr<<8|b;}
        public void draw(GuiGraphics g,float partial){
            rect(g,0,0,W,H,0xFF000000);
            for(float t=0;t<TURNS*Mth.TWO_PI;t+=.045F){float rr=A+B*t,a=t+rot;rect(g,CX+rr*Mth.cos(a),CY+rr*Mth.sin(a),1,1,colour(t/40));}
            rect(g,CX+r*Mth.cos(phi)-1,CY+r*Mth.sin(phi)-1,3,3,0xFFFFFFFF);
            boolean gentle=Minecraft.getInstance().options.hideLightningFlash().get();
            if(word!=null&&ticks-wordAt<(gentle?14:2))centred(g,word,(int)CY-4,gentle?0x40FFFFFF:0xFFFFFFFF);
            if(taken&&ticks>=90){rect(g,0,0,W,H,0xC0000000);centred(g,name.toUpperCase(Locale.ROOT),60,0xFFB0FFE0);centred(g,"PLAY AGAIN",76,0xFFB0FFE0);}
            String count=String.format("%06d",score*7+plays*1000);text(g,count,4,4,0xFF406050);
        }
        public int score(){return score;}public boolean over(){return !taken&&(offLine>40||ticks>=1400);}
    }
}
