package io.github.knaitoe.theoldesthouse.labyrinth;

/**
 * 0.4.397, the owner: "the dancer in the personhood vignette needs weight, so the player swings when they move while dancing".
 * The shelter's slow dance in the rain (HouseReadShelter): while a reader holds a partner, the keys no longer walk them about the
 * floor. A push to either side turns the two of them about the partner, and the turn carries on after the keys are let go, dying
 * away as a weight does. A push away leans back against the partner's hands, which give a little and draw the reader back to
 * arm's length; a push in closes only so far. The view turns with the swing, so the partner stays in front.
 * <p>
 * The client (DanceClient) runs this on the reader's own movement. The server goes on moving the partner to arm's length on the
 * line between them, as before, so the partner is the one that stays and the reader is the one that swings.
 */
public final class DanceWeight {
    private DanceWeight(){}
    /** Arm's length from the partner, how far the partner's hands give when pulled against, and how close a push in can come. */
    public static final double HOLD=.85,GIVE=.3,CLOSEST=.55;
    /** How much one tick of pushing adds to the turn, how much of the turn is kept each tick, and the most it carries (blocks a tick). */
    public static final double PUSH=.012,KEEP=.9,MOST=.14;
    /** How fast a push away or in moves the reader, how hard the hands draw them back to arm's length, and the fastest that is. */
    public static final double LEAN=.05,DRAW=.3,REIN=.12;
    /** How fast the partner closes on the reader while they dance, in blocks a tick: slowly, as a weight. */
    public static final double SETTLE=.05;

    /**
     * 0.4.399, the owner: "continuing to pull against the weight should swirl your partner". Leaning away for long enough swings
     * the partner round the reader instead: how far behind arm's length the partner has to be pulled to count, how many ticks the
     * pull must last, how much each further tick adds to the swirl, how much of it carries on each tick once the pull stops, and
     * the fastest it goes (radians a tick, about a turn in a second and a half).
     */
    public static final double PULL=.1,SWIRL_BUILD=.008,SWIRL_KEEP=.93,SWIRL_MOST=.2;
    /**
     * How far a tick the reader has to keep going away from the partner to count as pulling too. A lean the partner is following
     * holds the gap behind arm's length near LEAN/1.3, short of PULL, while the reader keeps backing away at about three quarters of
     * LEAN a tick; a swing round them drifts outward far less than this.
     */
    public static final double DRIFT=.02;
    public static final int PULL_TICKS=16;
    /**
     * One tick of the swirl: {@code spin} is the partner's turn about the reader carried from the last tick (radians, signed),
     * {@code pulled} how many ticks the reader has been pulling, and {@code way} the way it goes if it has not begun (+1 or -1:
     * the way the reader was last swinging). Pulled long enough, it builds; let go, it carries on and dies away.
     */
    public static double swirl(double spin,boolean pulling,int pulled,int way){
        if(pulling&&pulled>=PULL_TICKS){int dir=spin!=0?(int)Math.signum(spin):way<0?-1:1;return Math.max(-SWIRL_MOST,Math.min(SWIRL_MOST,spin+dir*SWIRL_BUILD));}
        spin*=SWIRL_KEEP;return Math.abs(spin)<.004?0:spin;
    }

    /** One tick: the reader's horizontal velocity, the turn carried into the next tick, and how far the view turns, in degrees. */
    public record Step(double vx,double vz,double turn,float yaw){}

    /**
     * One tick of the swing about a partner at ({@code cx},{@code cz}) for a reader at ({@code px},{@code pz}), pushing toward
     * ({@code wx},{@code wz}) (the keys turned into the world, at most 1 long), with {@code turn} carried from the last tick.
     */
    public static Step step(double px,double pz,double cx,double cz,double wx,double wz,double turn){
        double dx=px-cx,dz=pz-cz,r=Math.sqrt(dx*dx+dz*dz);
        if(r<1e-3)return new Step(0,0,turn*KEEP,0);
        double nx=dx/r,nz=dz/r,tx=-nz,tz=nx;
        double out=wx*nx+wz*nz,round=wx*tx+wz*tz;
        // Pushed round, the turn builds; let go, it carries on and dies away.
        turn=Math.max(-MOST,Math.min(MOST,turn*KEEP+round*PUSH));
        // Away or in, against the hands: a lean that is drawn back to arm's length, never past the hands' give or into the partner.
        double radial=Math.max(-REIN,Math.min(REIN,out*LEAN-(r-HOLD)*DRAW));
        if(r>=HOLD+GIVE)radial=Math.min(radial,0);
        if(r<=CLOSEST)radial=Math.max(radial,0);
        return new Step(nx*radial+tx*turn,nz*radial+tz*turn,turn,(float)Math.toDegrees(turn/r));
    }
}
