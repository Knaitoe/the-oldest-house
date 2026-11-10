package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.nbt.CompoundTag;

/**
 * 0.4.414 (docs/BAR_GAMES_PROPOSAL.md): a frame of eight-ball, as pure logic: the balls, the table, a shot played out, and the rules.
 * No game or client classes are touched here, so the server plays a shot and every client watching plays the same shot from the same
 * state and arrives at the same place: Java's arithmetic is the same everywhere.
 *
 * <p>The table is 254 by 127 units (about centimetres), with six pockets. Ball 0 is the cue ball, 1 to 7 the spots, 8 the eight and 9
 * to 15 the stripes. Up to four players, in two sides that alternate (doubles: 0 and 2 against 1 and 3).
 */
public final class PoolGame {
    public static final double W=254,H=127,R=3.0,MAX_SPEED=640,FRICTION=70,CUSHION=.78,DT=1/240.0;
    public static final int BALLS=16,MAX_STEPS=240*25;
    /** The six pockets, corner and middle, and how near a ball's centre must come to fall. */
    public static final double[][] POCKETS={{0,0},{W/2,-1.5},{W,0},{0,H},{W/2,H+1.5},{W,H}};
    public static final double CORNER_MOUTH=6.2,SIDE_MOUTH=5.4;
    public static double mouth(int pocket){return pocket==1||pocket==4?SIDE_MOUTH:CORNER_MOUTH;}
    /** Where the cue ball goes when it is put back, and the line behind which it may be placed after the break. */
    public static final double HEAD_X=W*.25,FOOT_X=W*.73;

    public enum Phase{BREAK,OPEN,PLAY,OVER}
    public final double[] x=new double[BALLS],y=new double[BALLS],vx=new double[BALLS],vy=new double[BALLS];
    public final boolean[] on=new boolean[BALLS];
    public Phase phase=Phase.BREAK;
    /** Whose turn: side 0 or 1. Which group each side has: 0 none yet, 1 spots, 2 stripes. */
    public int side;
    public final int[] group=new int[2];
    public boolean ballInHand;
    /** The side that won, once over; and a word on the last shot, for the table and the screen. */
    public int winner=-1;
    public String last="";

    public static int groupOf(int ball){return ball>=1&&ball<=7?1:ball>=9&&ball<=15?2:0;}

    /** A fresh rack: the eight in the middle of the triangle, a spot and a stripe in the back corners, the rest by the seed. */
    public static PoolGame rack(long seed){
        var g=new PoolGame();var rnd=new Random(seed);
        List<Integer> rest=new ArrayList<>();for(int b=2;b<=15;b++)if(b!=8&&b!=9)rest.add(b);
        java.util.Collections.shuffle(rest,rnd);
        int[] order=new int[15];int k=0;
        for(int i=0;i<15;i++){
            if(i==0)order[i]=1;else if(i==4)order[i]=8;else if(i==10)order[i]=9;else if(i==14){int pick=-1;for(int j=0;j<rest.size();j++)if(groupOf(rest.get(j))==1){pick=j;break;}order[i]=pick<0?rest.remove(0):rest.remove(pick);}
            else order[i]=rest.remove(0);
        }
        double dx=Math.sqrt(3)*R*1.01;int i=0;
        for(int row=0;row<5;row++)for(int col=0;col<=row;col++){int b=order[i++];g.x[b]=FOOT_X+row*dx;g.y[b]=H/2+(col-row/2.0)*2*R*1.01;g.on[b]=true;}
        g.x[0]=HEAD_X;g.y[0]=H/2;g.on[0]=true;
        return g;
    }
    public PoolGame copy(){var g=new PoolGame();g.load(save());return g;}

    // ------------------------------------------------------------------------------------------------ a shot played out
    /** What happened in a shot, for the rules: the first ball the cue ball touched, and the balls that fell, in order. */
    public static final class Result{public int first=-1;public final List<Integer> potted=new ArrayList<>();public int steps;
        /** For the sounds: knocks between balls, and balls off a cushion, each loud enough to hear. */
        public int clacks,cushions;}
    /** Strikes the cue ball (angle in radians, power 0 to 1) and plays the table on until everything is still. */
    public Result strike(double angle,double power){
        begin(angle,power);
        var r=new Result();
        while(r.steps<MAX_STEPS&&step(r))r.steps++;
        for(int b=0;b<BALLS;b++){vx[b]=0;vy[b]=0;}
        return r;
    }
    /** Sets the cue ball rolling, for a table played a step at a time (a client animating a shot). */
    public void begin(double angle,double power){
        power=Math.max(0,Math.min(1,power));double speed=MAX_SPEED*(.06+.94*power*power);
        vx[0]=Math.cos(angle)*speed;vy[0]=Math.sin(angle)*speed;
    }
    /** One step of the table. Returns false once nothing moves. */
    public boolean step(Result r){
        boolean moving=false;
        for(int b=0;b<BALLS;b++){
            if(!on[b])continue;double s=Math.hypot(vx[b],vy[b]);if(s==0)continue;
            double ns=Math.max(0,s-FRICTION*DT);if(ns<1.2){vx[b]=0;vy[b]=0;continue;}
            vx[b]*=ns/s;vy[b]*=ns/s;x[b]+=vx[b]*DT;y[b]+=vy[b]*DT;moving=true;
        }
        for(int a=0;a<BALLS;a++){if(!on[a])continue;
            for(int b=a+1;b<BALLS;b++){if(!on[b])continue;
                double dx=x[b]-x[a],dy=y[b]-y[a],d2=dx*dx+dy*dy;if(d2>=4*R*R||d2==0)continue;
                double d=Math.sqrt(d2),nx=dx/d,ny=dy/d,rel=(vx[a]-vx[b])*nx+(vy[a]-vy[b])*ny;
                if(rel>0){vx[a]-=rel*nx;vy[a]-=rel*ny;vx[b]+=rel*nx;vy[b]+=rel*ny;
                    if(r!=null){if(r.first<0&&(a==0||b==0))r.first=a==0?b:a;if(rel>12)r.clacks++;}}
                double push=(2*R-d)/2;x[a]-=nx*push;y[a]-=ny*push;x[b]+=nx*push;y[b]+=ny*push;
            }}
        for(int b=0;b<BALLS;b++){if(!on[b])continue;
            int pocket=pocketAt(x[b],y[b]);
            if(pocket>=0){on[b]=false;vx[b]=0;vy[b]=0;if(r!=null)r.potted.add(b);continue;}
            if(nearPocket(x[b],y[b]))continue;
            boolean hit=false;double before=Math.hypot(vx[b],vy[b]);
            if(x[b]<R){x[b]=R;vx[b]=Math.abs(vx[b])*CUSHION;hit=true;}else if(x[b]>W-R){x[b]=W-R;vx[b]=-Math.abs(vx[b])*CUSHION;hit=true;}
            if(y[b]<R){y[b]=R;vy[b]=Math.abs(vy[b])*CUSHION;hit=true;}else if(y[b]>H-R){y[b]=H-R;vy[b]=-Math.abs(vy[b])*CUSHION;hit=true;}
            if(hit&&r!=null&&before>20)r.cushions++;
        }
        // A ball that slipped past a mouth without falling is put back on the cloth.
        for(int b=0;b<BALLS;b++)if(on[b]){x[b]=Math.max(-2,Math.min(W+2,x[b]));y[b]=Math.max(-2,Math.min(H+2,y[b]));}
        return moving;
    }
    static int pocketAt(double px,double py){for(int p=0;p<6;p++)if(Math.hypot(px-POCKETS[p][0],py-POCKETS[p][1])<mouth(p))return p;return -1;}
    static boolean nearPocket(double px,double py){for(int p=0;p<6;p++)if(Math.hypot(px-POCKETS[p][0],py-POCKETS[p][1])<mouth(p)+R*1.6)return true;return false;}

    // ------------------------------------------------------------------------------------------------ the rules
    public boolean cleared(int s){int gr=group[s];if(gr==0)return false;for(int b=1;b<BALLS;b++)if(on[b]&&groupOf(b)==gr)return false;return true;}
    /** Where the cue ball may be placed with the ball in hand: on the cloth, touching nothing; behind the line after a foul on the break. */
    public boolean canPlace(double px,double py){
        if(px<R||px>W-R||py<R||py>H-R)return false;
        for(int b=1;b<BALLS;b++)if(on[b]&&Math.hypot(px-x[b],py-y[b])<2*R+.01)return false;
        return true;
    }
    private double[] cueSpot(){
        // There are more grid points than object balls, and each ball can block at most one.
        double spacing=4*R+.04;
        for(double px=HEAD_X;px>=2*R;px-=spacing){
            for(double offset=0;offset<=H/2-2*R;offset+=spacing){
                if(canPlace(px,H/2+offset))return new double[]{px,H/2+offset};
                if(offset>0&&canPlace(px,H/2-offset))return new double[]{px,H/2-offset};
            }
        }
        throw new IllegalStateException("No legal cue ball spot");
    }
    /**
     * A shot taken by the side whose turn it is: the cue ball placed first if they have it in hand, then struck. Returns what the shot
     * did, or null (changing nothing) for a shot that cannot be taken. Afterwards the turn, the groups, the ball in hand and the winner follow the
     * rules: a scratch, no contact or the wrong ball first is a foul, and the other side has the cue ball in hand; the eight down with
     * the side's group cleared wins, and down any earlier, or with a foul, loses.
     */
    public Result shoot(double angle,double power,double placeX,double placeY){
        if(phase==Phase.OVER)return null;
        if(ballInHand){if(!canPlace(placeX,placeY))return null;x[0]=placeX;y[0]=placeY;on[0]=true;ballInHand=false;}
        var r=strike(angle,power);
        boolean breakShot=phase==Phase.BREAK;
        int mine=group[side];
        boolean foul=r.first<0||r.potted.contains(0);
        if(!foul&&!breakShot){
            if(mine==0&&r.first==8)foul=true;
            if(mine!=0&&!(groupOf(r.first)==mine||(r.first==8&&cleared(side))))foul=true;
        }
        if(r.potted.contains(8)){
            boolean legal=!foul&&mine!=0&&cleared(side)&&!breakShot;
            winner=legal?side:1-side;phase=Phase.OVER;
            last=legal?"The eight drops. That's the frame.":breakShot?"The eight goes down on the break, and the frame with it.":"The eight goes down too soon.";
            return r;
        }
        boolean kept=false;
        if(!foul){
            for(int b:r.potted){if(b==0||b==8)continue;
                if(mine==0&&!breakShot){group[side]=groupOf(b);group[1-side]=3-groupOf(b);mine=group[side];}
                if(breakShot||groupOf(b)==mine)kept=true;}
        }
        if(breakShot)phase=Phase.OPEN;
        if(phase==Phase.OPEN&&group[0]!=0)phase=Phase.PLAY;
        if(foul){
            if(!on[0]){var spot=cueSpot();x[0]=spot[0];y[0]=spot[1];on[0]=true;}
            side=1-side;ballInHand=true;
            last=r.first<0?"Nothing touched. Ball in hand.":r.potted.contains(0)?"The cue ball drops. Ball in hand.":"Wrong ball first. Ball in hand.";
        }else if(kept){
            last=r.potted.size()==1?"One down. Shoot again.":r.potted.size()+" down. Shoot again.";
        }else{side=1-side;last=r.potted.isEmpty()?"Nothing down.":"Not yours. Over to the other side.";}
        return r;
    }

    // ------------------------------------------------------------------------------------------------ the regular's eye
    /** A shot for a player the table plays: the easiest of its own balls into the nearest open pocket, off by a little. */
    public double[] aim(long seed,double skill){
        var rnd=new Random(seed);int mine=group[side];boolean eightNow=mine!=0&&cleared(side);
        double best=-1,bestAngle=rnd.nextDouble()*Math.PI*2,bestPower=.5;
        double cx=x[0],cy=y[0];
        if(ballInHand){var spot=cueSpot();cx=spot[0];cy=spot[1];}
        for(int b=1;b<BALLS;b++){
            if(!on[b])continue;
            boolean ok=phase==Phase.BREAK?b==1:eightNow?b==8:mine==0?b!=8:groupOf(b)==mine;if(!ok)continue;
            for(int p=0;p<6;p++){
                double px=POCKETS[p][0],py=POCKETS[p][1],tx=px-x[b],ty=py-y[b],tl=Math.hypot(tx,ty);
                double gx=x[b]-tx/tl*2*R,gy=y[b]-ty/tl*2*R,ax=gx-cx,ay=gy-cy,al=Math.hypot(ax,ay);
                double cut=(ax*tx+ay*ty)/(al*tl);if(cut<.25)continue;
                if(blocked(cx,cy,gx,gy,b)||blocked(x[b],y[b],px,py,b))continue;
                double score=cut*cut/(1+(al+tl)/200);
                if(score>best){best=score;bestAngle=Math.atan2(ay,ax);bestPower=Math.min(1,.35+(al+tl)/420);}
            }
        }
        if(phase==Phase.BREAK){bestAngle=Math.atan2(y[1]-cy,x[1]-cx);bestPower=1;}
        double err=(1-skill)*.06;bestAngle+=rnd.nextGaussian()*err;
        return new double[]{bestAngle,bestPower,cx,cy};
    }
    boolean blocked(double ax,double ay,double bx,double by,int ignore){
        double dx=bx-ax,dy=by-ay,l2=dx*dx+dy*dy;if(l2==0)return false;
        for(int k=1;k<BALLS;k++){if(!on[k]||k==ignore)continue;
            double t=((x[k]-ax)*dx+(y[k]-ay)*dy)/l2;if(t<=0||t>=1)continue;
            double qx=ax+t*dx-x[k],qy=ay+t*dy-y[k];if(qx*qx+qy*qy<4*R*R)return true;}
        return false;
    }

    // ------------------------------------------------------------------------------------------------ saved and sent
    public CompoundTag save(){
        var t=new CompoundTag();
        int mask=0;
        for(int b=0;b<BALLS;b++){t.putDouble("X"+b,x[b]);t.putDouble("Y"+b,y[b]);if(on[b])mask|=1<<b;}
        t.putInt("On",mask);t.putString("Phase",phase.name());t.putInt("Side",side);t.putInt("G0",group[0]);t.putInt("G1",group[1]);
        t.putBoolean("Hand",ballInHand);t.putInt("Winner",winner);t.putString("Last",last);
        return t;
    }
    public void load(CompoundTag t){
        int mask=t.getInt("On");
        for(int b=0;b<BALLS;b++){x[b]=t.getDouble("X"+b);y[b]=t.getDouble("Y"+b);on[b]=(mask>>b&1)!=0;vx[b]=0;vy[b]=0;}
        try{phase=Phase.valueOf(t.getString("Phase"));}catch(IllegalArgumentException e){phase=Phase.BREAK;}
        side=t.getInt("Side");group[0]=t.getInt("G0");group[1]=t.getInt("G1");ballInHand=t.getBoolean("Hand");winner=t.getInt("Winner");last=t.getString("Last");
    }
}
