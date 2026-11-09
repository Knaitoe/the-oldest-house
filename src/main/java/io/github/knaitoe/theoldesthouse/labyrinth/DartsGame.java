package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;

/**
 * 0.4.415 (docs/DARTS_0_4_415.md): a game of darts as plain rules. Where a dart lands on the face gives its ring and number, by the
 * standard board's proportions and number order; 301 counts down to exactly nothing with a bust over, and round the clock goes 1 to
 * 20, then the bull. Three darts a turn, one to four players.
 */
public final class DartsGame {
    public enum Mode { X01, CLOCK;
        public String title(){return this==X01?"301":"Round the clock";}
        public Mode other(){return this==X01?CLOCK:X01;}
    }
    public enum Ring { MISS, SINGLE, DOUBLE, TREBLE, OUTER, BULL }
    /** A dart's landing: its ring and number (25 for the outer bull and bull, 0 for a miss). */
    public record Hit(Ring ring,int number){
        public static final Hit MISS=new Hit(Ring.MISS,0);
        public int value(){return switch(ring){case MISS->0;case SINGLE->number;case DOUBLE->number*2;case TREBLE->number*3;case OUTER->25;case BULL->50;};}
        /** As it is called at the oche: "Treble 20", "Bull", "Missed". */
        public String say(){return switch(ring){case MISS->"Missed";case SINGLE->String.valueOf(number);case DOUBLE->"Double "+number;case TREBLE->"Treble "+number;case OUTER->"Outer bull";case BULL->"Bull";};}
        /** As it is chalked: "T20", "D5", "25", "B". */
        public String chalk(){return switch(ring){case MISS->"-";case SINGLE->String.valueOf(number);case DOUBLE->"D"+number;case TREBLE->"T"+number;case OUTER->"25";case BULL->"B";};}
        int code(){return ring.ordinal()*100+number;}
        static Hit of(int code){return new Hit(Ring.values()[Math.max(0,Math.min(5,code/100))],code%100);}
    }
    /** The numbers clockwise from the top. */
    public static final int[] ORDER={20,1,18,4,13,6,10,15,2,17,3,19,7,16,8,11,14,9,12,5};
    /** Wires as fractions of the double's outer wire (170 mm on a standard board). */
    public static final double BULL=6.35/170,OUTER=15.9/170,TREBLE_IN=99/170.0,TREBLE_OUT=107/170.0,DOUBLE_IN=162/170.0;
    public static final int DARTS=3,START=301,BULL_TARGET=21;

    /** The ring and number at a point on the face: u to the right and v up from the centre, 1 being the double's outer wire. */
    public static Hit score(double u,double v){
        double r=Math.hypot(u,v);
        if(r>1)return Hit.MISS;
        if(r<BULL)return new Hit(Ring.BULL,25);
        if(r<OUTER)return new Hit(Ring.OUTER,25);
        double angle=Math.toDegrees(Math.atan2(u,v));if(angle<0)angle+=360;
        int n=ORDER[(int)Math.floor((angle+9)/18)%20];
        if(r>=TREBLE_IN&&r<TREBLE_OUT)return new Hit(Ring.TREBLE,n);
        if(r>=DOUBLE_IN)return new Hit(Ring.DOUBLE,n);
        return new Hit(Ring.SINGLE,n);
    }
    /** The centre of a target on the face, in the same units: a number's treble, or the bull. */
    public static double[] aimAt(Ring ring,int number){
        if(ring==Ring.BULL||ring==Ring.OUTER||number==25)return new double[]{0,0};
        int i=0;for(;i<20;i++)if(ORDER[i]==number)break;
        double r=switch(ring){case TREBLE->(TREBLE_IN+TREBLE_OUT)/2;case DOUBLE->(DOUBLE_IN+1)/2;default->(OUTER+TREBLE_IN)/2;};
        double a=Math.toRadians(i*18);
        return new double[]{Math.sin(a)*r,Math.cos(a)*r};
    }

    public final Mode mode;
    /** 301: what each player has left. Round the clock: the number each is on (21 is the bull). */
    public final int[] score;
    public int turn,darts,turnStart,winner=-1;
    public final List<Hit> thisTurn=new ArrayList<>();
    /** The last finished turn's darts, for the chalk. */
    public final List<Hit> lastTurn=new ArrayList<>();
    public String last="";

    public DartsGame(Mode mode,int players){
        this.mode=mode;score=new int[Math.max(1,players)];
        for(int i=0;i<score.length;i++)score[i]=mode==Mode.X01?START:1;
        turnStart=score[0];
    }
    public boolean over(){return winner>=0;}
    public int players(){return score.length;}
    /** The current player's next target in round the clock (21 for the bull). */
    public int target(){return score[turn];}
    /** True once a turn has thrown its darts, gone bust or won: the darts come out before the next. */
    public boolean turnOver(){return over()||darts>=DARTS||busted;}
    boolean busted;

    /** One dart for the player whose turn it is. Nothing happens once the turn is over. */
    public void dart(Hit h){
        if(turnOver())return;
        darts++;thisTurn.add(h);
        if(mode==Mode.X01){
            int left=score[turn]-h.value();
            if(left<0){score[turn]=turnStart;busted=true;last=h.say()+". Bust.";}
            else if(left==0){score[turn]=0;winner=turn;last=h.say()+". Game shot.";}
            else{score[turn]=left;last=h.say()+". "+left+" left.";}
        }else{
            int t=score[turn];
            boolean on=t<BULL_TARGET?(h.ring==Ring.SINGLE||h.ring==Ring.DOUBLE||h.ring==Ring.TREBLE)&&h.number==t:(h.ring==Ring.OUTER||h.ring==Ring.BULL);
            if(on){score[turn]=t+1;if(score[turn]>BULL_TARGET){winner=turn;last=h.say()+". Round the clock.";}else last=h.say()+". On to "+(score[turn]==BULL_TARGET?"the bull":String.valueOf(score[turn]))+".";}
            else last=h.say()+". Still on "+(t==BULL_TARGET?"the bull":String.valueOf(t))+".";
        }
    }
    /** Hands the oche to the next player. */
    public void nextTurn(){
        if(over())return;
        lastTurn.clear();lastTurn.addAll(thisTurn);thisTurn.clear();
        turn=(turn+1)%score.length;darts=0;busted=false;turnStart=score[turn];
    }

    /** The same game with one player gone: the others keep their scores, and a turn that was theirs passes on. */
    public DartsGame without(int player){
        var g=new DartsGame(mode,Math.max(1,score.length-1));
        for(int i=0,j=0;i<score.length&&j<g.score.length;i++)if(i!=player)g.score[j++]=score[i];
        if(turn==player){g.turn=player%g.score.length;g.turnStart=g.score[g.turn];}
        else{g.turn=turn>player?turn-1:turn;g.darts=darts;g.busted=busted;g.turnStart=turnStart;g.thisTurn.addAll(thisTurn);}
        g.lastTurn.addAll(lastTurn);g.last=last;
        return g;
    }
    public CompoundTag save(){
        var t=new CompoundTag();t.putString("Mode",mode.name());t.putIntArray("Score",score.clone());
        t.putInt("Turn",turn);t.putInt("Darts",darts);t.putInt("TurnStart",turnStart);t.putInt("Winner",winner);t.putBoolean("Bust",busted);
        t.putIntArray("This",thisTurn.stream().mapToInt(Hit::code).toArray());t.putIntArray("Last",lastTurn.stream().mapToInt(Hit::code).toArray());
        t.putString("Said",last);
        return t;
    }
    public static DartsGame load(CompoundTag t){
        Mode m;try{m=Mode.valueOf(t.getString("Mode"));}catch(IllegalArgumentException e){m=Mode.X01;}
        int[] s=t.getIntArray("Score");var g=new DartsGame(m,Math.max(1,s.length));
        System.arraycopy(s,0,g.score,0,Math.min(s.length,g.score.length));
        g.turn=Math.max(0,Math.min(g.score.length-1,t.getInt("Turn")));g.darts=t.getInt("Darts");g.turnStart=t.getInt("TurnStart");g.winner=t.getInt("Winner");g.busted=t.getBoolean("Bust");
        for(int c:t.getIntArray("This"))g.thisTurn.add(Hit.of(c));for(int c:t.getIntArray("Last"))g.lastTurn.add(Hit.of(c));
        g.last=t.getString("Said");
        return g;
    }
}
