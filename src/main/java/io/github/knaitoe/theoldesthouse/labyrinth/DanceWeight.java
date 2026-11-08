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
