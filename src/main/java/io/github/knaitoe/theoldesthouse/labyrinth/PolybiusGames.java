package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.util.Mth;

/**
 * The arcade's four games as rules alone, shared by the reader's screen and the server (0.4.225). Each game is a pure function of
 * the session's seed and the keys held on each tick: the screen ({@code client/PolybiusScreen}) plays it and draws it, and sends
 * the keys it held; the server replays the same keys from the same seed ({@link #replay}) and keeps the score it gets, never one
 * the screen reports. Nothing here draws or plays a sound; a game asks its {@link Sound} for those, and the server's is silent.
 * Anything only drawn (snow on the dial, colours) uses the screen's own randomness, never the game's.
 */
public final class PolybiusGames {
    private PolybiusGames(){}
    public static final int W=200,H=150;
    /** The keys a game reads, one bit each. */
    public static final int LEFT=1,RIGHT=2,UP=4,DOWN=8,SPACE=16,KEYS=31;
    /** The sounds a game asks for. */
    public static final int CHIME=0,HARP=1,BELL=2,SNARE=3,BASS=4;
    public interface Sound{void play(int kind,float pitch);}
    static final Sound SILENT=(k,p)->{};

    public abstract static class Game{
        final Random random;final Sound sound;public int ticks;int keys;
        Game(long seed,Sound sound){random=new Random(seed);this.sound=sound==null?SILENT:sound;}
        /** One tick with these keys held. */
        public final void tick(int keys){this.keys=keys;step();ticks++;}
        abstract void step();
        public abstract int score();
        public abstract boolean over();
        boolean left(){return (keys&LEFT)!=0;}boolean right(){return (keys&RIGHT)!=0;}boolean up(){return (keys&UP)!=0;}boolean down(){return (keys&DOWN)!=0;}
        boolean push(){return (keys&(SPACE|UP))!=0;}
    }
    public static Game of(int game,long seed,String name,boolean taken,Sound sound){
        return switch(game){case 0->new Moth(seed,sound);case 1->new Undertow(seed,sound);case 2->new Signal(seed,sound);default->new Spiral(seed,sound,name,taken);};
    }

    /** What the server makes of a game: its own score, how many ticks the keys covered, and whether it was over. */
    public record Result(int score,int ticks,boolean over){}
    /**
     * The game played again from its seed with the keys the screen held, as runs ({@code count<<5|keys}); a game over stops it, and
     * keys after that are ignored. Null for a malformed log: a run of nothing, or keys the game has no use for.
     */
    public static Result replay(int game,long seed,List<Integer> runs,int longest){
        var g=of(game,seed,"",false,SILENT);int ticks=0;
        for(int run:runs){
            int keys=run&KEYS,count=run>>>5;if(count<=0)return null;
            for(int i=0;i<count&&!g.over();i++){if(++ticks>longest)return null;g.tick(keys);}
            if(g.over())break;
        }
        return new Result(Math.max(0,g.score()),ticks,g.over());
    }
    /** The ticks a log covers, or -1 for a malformed one. */
    public static int length(List<Integer> runs){long n=0;for(int run:runs){int count=run>>>5;if(count<=0)return -1;n+=count;if(n>Integer.MAX_VALUE)return -1;}return (int)n;}

    // ------------------------------------------------------------------------------------------------ Mothlight
    /** A moth round a porch lamp: gather the motes of light drifting in to it; bats cross the dark; touching the lamp burns. */
    public static final class Moth extends Game{
        public float x=100,y=110;public int lives=3,hurt,score;public final List<float[]> motes=new ArrayList<>(),bats=new ArrayList<>();
        public static final float LX=100,LY=70,LAMP=7;
        Moth(long seed,Sound sound){super(seed,sound);}
        void step(){
            float speed=2;if(left())x-=speed;if(right())x+=speed;if(up())y-=speed;if(down())y+=speed;
            // The lamp pulls, a little, at anything with wings.
            float dx=LX-x,dy=LY-y,d=Mth.sqrt(dx*dx+dy*dy);if(d>1){x+=dx/d*.35F;y+=dy/d*.35F;}
            x=Mth.clamp(x,3,W-3);y=Mth.clamp(y,3,H-3);
            if(hurt>0)hurt--;
            if(d<LAMP&&hurt==0)hit();
            if(ticks%12==0){int edge=random.nextInt(4);motes.add(edge==0?new float[]{random.nextInt(W),0}:edge==1?new float[]{random.nextInt(W),H}:edge==2?new float[]{0,random.nextInt(H)}:new float[]{W,random.nextInt(H)});}
            for(var it=motes.iterator();it.hasNext();){var m=it.next();float mx=LX-m[0],my=LY-m[1],md=Mth.sqrt(mx*mx+my*my);
                if(md<LAMP){it.remove();continue;}m[0]+=mx/md*.6F+(random.nextFloat()-.5F)*.6F;m[1]+=my/md*.6F+(random.nextFloat()-.5F)*.6F;
                if(Math.abs(m[0]-x)<5&&Math.abs(m[1]-y)<5){it.remove();score+=10;sound.play(CHIME,1.6F+random.nextFloat()*.3F);}}
            int every=Math.max(25,80-ticks/50);
            if(ticks%every==0){boolean fromLeft=random.nextBoolean();float by;do{by=8+random.nextInt(H-16);}while(Math.abs(by-LY)<12);bats.add(new float[]{fromLeft?-8:W+8,by,(fromLeft?1:-1)*Math.min(4,1.8F+ticks/1500F)});}
            for(var it=bats.iterator();it.hasNext();){var b=it.next();b[0]+=b[2];b[1]+=Mth.sin((ticks+b[1])*.3F)*.8F;if(b[0]<-12||b[0]>W+12){it.remove();continue;}
                if(hurt==0&&Math.abs(b[0]-x)<5&&Math.abs(b[1]-y)<4)hit();}
            if(ticks%10==0)score++;
        }
        void hit(){lives--;hurt=40;sound.play(BASS,.6F);float dx=x-LX,dy=y-LY,d=Math.max(1,Mth.sqrt(dx*dx+dy*dy));x=LX+dx/d*24;y=LY+dy/d*24;}
        public int score(){return score;}public boolean over(){return lives<=0;}
    }

    // ------------------------------------------------------------------------------------------------ Undertow
    /** A swimmer in a black lake, under what the current brings down; catch the bubbles coming up before the air runs out. */
    public static final class Undertow extends Game{
        public float x=100;public float air=100;public int hurt,score;public final List<float[]> debris=new ArrayList<>(),bubbles=new ArrayList<>();
        public static final float Y=135;
        Undertow(long seed,Sound sound){super(seed,sound);}
        void step(){
            if(left())x-=2.2F;if(right())x+=2.2F;x=Mth.clamp(x,6,W-6);
            if(hurt>0)hurt--;
            if(ticks%4==0)air--;
            int every=Math.max(8,26-ticks/200);
            if(ticks%every==0){float w=8+random.nextInt(13);debris.add(new float[]{random.nextInt((int)(W-w)),-8,w,Math.min(3.2F,1+ticks/2000F)});}
            if(ticks%30==0)bubbles.add(new float[]{8+random.nextInt(W-16),H+4});
            for(var it=debris.iterator();it.hasNext();){var d=it.next();d[1]+=d[3];if(d[1]>H){it.remove();continue;}
                if(hurt==0&&d[1]+6>Y-4&&d[1]<Y+4&&x+4>d[0]&&x-4<d[0]+d[2]){air-=20;hurt=20;sound.play(BASS,.5F);}}
            for(var it=bubbles.iterator();it.hasNext();){var b=it.next();b[1]-=1.2F;b[0]+=Mth.sin((ticks+b[0])*.1F)*.4F;if(b[1]<-4){it.remove();continue;}
                if(Math.abs(b[0]-x)<6&&Math.abs(b[1]-Y)<6){it.remove();air=Math.min(100,air+25);score+=5;sound.play(HARP,1.8F);}}
            if(ticks%5==0)score++;
        }
        public int score(){return score;}public boolean over(){return air<=0;}
    }

    // ------------------------------------------------------------------------------------------------ Signal
    /** A needle on a radio dial: hold the space bar against the drift to keep it inside the station as it wanders and narrows. */
    public static final class Signal extends Game{
        public float needle=90,v;public int off,score;public float centre=90;
        Signal(long seed,Sound sound){super(seed,sound);}
        public float width(){return Math.max(14,44-ticks/60F);}
        void step(){
            centre=90+60*Mth.sin(ticks*.013F)*Mth.cos(ticks*.0071F)+15*Mth.sin(ticks*.041F);
            v+=push()?.18F:-.12F;v*=.96F;needle+=v;if(needle<0){needle=0;v=0;}if(needle>180){needle=180;v=0;}
            float half=width()/2,dist=Math.abs(needle-centre);
            if(dist<=half){score+=dist<half/3?2:1;if(ticks%20==0)sound.play(BELL,1.2F+(1-dist/half)*.6F);}
            else{off++;if(ticks%6==0)sound.play(SNARE,.6F+random.nextFloat()*.3F);}
        }
        public int score(){return score;}public boolean over(){return off>60;}
    }

    // ------------------------------------------------------------------------------------------------ the black cabinet
    /** A spiral that turns, and a dot that rides out along it; turn the dot to keep it on the line. */
    public static final class Spiral extends Game{
        public static final float CX=100,CY=75,A=5,B=3.1F,TURNS=3;
        public float rot,speed=.012F,r=8,phi;public int offLine,score,loop;
        public String word;public int wordAt=-1,nextWord=90;
        final String[] words;final boolean taken;
        Spiral(long seed,Sound sound,String name,boolean taken){super(seed,sound);this.taken=taken;words=new String[]{"OBEY","NO THOUGHT","STAY","SLEEP","CONSUME","PLAY AGAIN",name.toUpperCase(Locale.ROOT)};phi=line(r);}
        float line(float radius){return (radius-A)/B+rot;}
        void step(){
            if(taken&&ticks>=90)return;
            rot+=speed;r+=.16F;
            if(r>A+B*TURNS*Mth.TWO_PI-2){r=8;loop++;speed+=.004F;phi=line(r);}
            if(left())phi-=.07F;if(right())phi+=.07F;
            float d=Mth.wrapDegrees((phi-line(r))*Mth.RAD_TO_DEG)*Mth.DEG_TO_RAD;
            if(Math.abs(d)<.26F){offLine=0;score++;}else offLine++;
            if(ticks%20==0)sound.play(BASS,.5F+loop*.05F);
            if(ticks>=nextWord){word=words[random.nextInt(words.length)];wordAt=ticks;nextWord=ticks+70+random.nextInt(60);}
        }
        public boolean taken(){return taken;}
        public int score(){return score;}public boolean over(){return !taken&&(offLine>40||ticks>=1400);}
    }
}
