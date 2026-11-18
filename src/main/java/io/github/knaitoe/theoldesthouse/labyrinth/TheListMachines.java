package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * 0.4.701, The list (docs/THE_LIST_0_4_701.md): how the pasture's machines stand at a given tick, worked out from the level's own time
 * alone, so that the server (which plays their sounds) and every client (which draws them) agree without a word between them.
 * <p>
 * The pumpjack, in its machine block's frame facing north, in blocks: the walking beam pivots at (0.5, 1.25, 0.5); its horsehead's face
 * is three blocks in front of the pivot; the equalizer hangs from its back end two blocks behind and a little under; the cranks turn on
 * the gearbox's shaft at (0.5, -0.75, 2.5) with their pins half a block out; the pitman arms run from the pins to the equalizer and are
 * the length that keeps the beam level when the pins are at the back. Each well keeps its own place in the stroke.
 * <p>
 * The windmill's wheel turns about once in two and a half seconds, a little faster and slower as the wind goes.
 * <p>
 * 0.4.716: a pumpjack keeps its own tick, the level's but for its brake (TheListMachineBlock.Turning): braked or stalled it stands, and
 * let go it runs on from where it stood.
 */
public final class TheListMachines {
    private TheListMachines(){}
    public static final double PIVOT_Y=1.25,PIVOT_Z=.5,HEAD_R=3.0,REAR=2.0,EQ_DROP=.15,CRANK_Y=-.75,CRANK_Z=2.5,CRANK_R=.5;
    public static final double PITMAN=Math.sqrt((PIVOT_Y-EQ_DROP-CRANK_Y)*(PIVOT_Y-EQ_DROP-CRANK_Y)+CRANK_R*CRANK_R);
    /** Ticks to a stroke: eight strokes a minute. */
    public static final int STROKE=150;
    /** Where the carrier bar hangs, in the machine block's frame, when the beam is level. */
    public static final double CARRIER_Y=-.1,ROD_Z=PIVOT_Z-HEAD_R;
    /** A well's own place in its stroke. */
    public static int phase(BlockPos at){return Math.floorMod(at.getX()*31+at.getZ()*17,STROKE);}
    /** The crank's angle: nought with its pins straight up, turning toward the back. */
    public static double crank(long time,float partial,BlockPos at){return crankAt(time%(STROKE*1000L)+partial,at);}
    /** 0.4.716: the crank's angle at a well's own tick. */
    public static double crankAt(double tick,BlockPos at){return 2*Math.PI*((tick%(STROKE*1000D)+phase(at))/STROKE);}
    /** 0.4.716: a well's own tick now: the level's, or its block entity's if it has one (braked, stalled, or run on after either). */
    public static double tick(@Nullable BlockEntity e,long time,float partial){return e instanceof TheListMachineBlock.Turning t?t.tick(time,partial):time+partial;}
    public static double[] crankPin(double crank){return new double[]{CRANK_Y+CRANK_R*Math.cos(crank),CRANK_Z+CRANK_R*Math.sin(crank)};}
    /** The equalizer's pin (y, z) with the beam turned `beam` radians, its back end up. */
    public static double[] equalizer(double beam){
        double y=-EQ_DROP,z=REAR;
        return new double[]{PIVOT_Y+y*Math.cos(beam)+z*Math.sin(beam),PIVOT_Z-y*Math.sin(beam)+z*Math.cos(beam)};
    }
    /** The beam's angle for a crank's: where the equalizer is a pitman arm's length from the pin. */
    public static double beam(double crank){
        var c=crankPin(crank);double th=0;
        for(int i=0;i<10;i++){
            double f=reach(th,c)-PITMAN,h=1e-5,d=(reach(th+h,c)-PITMAN-f)/h;
            if(Math.abs(d)<1e-9)break;
            double next=th-f/d;if(Math.abs(next-th)<1e-7){th=next;break;}th=next;
        }
        return th;
    }
    private static double reach(double beam,double[] c){var e=equalizer(beam);double dy=e[0]-c[0],dz=e[1]-c[1];return Math.sqrt(dy*dy+dz*dz);}
    /** How far the rod has risen from where it hangs when the beam is level (the horsehead's arc rolled out); down is negative. */
    public static double rod(double beam){return -HEAD_R*beam;}
    /** The bottom of the stroke falls as the pins come over the top; the knock is heard there. */
    public static boolean knocks(long time,BlockPos at){return Math.floorMod(time+phase(at)+8,STROKE)==0;}
    /** 0.4.716: the same for a well with its own tick: heard on the tick its stroke's bottom is passed, and never while it stands. */
    public static boolean knocks(@Nullable BlockEntity e,long time,BlockPos at){
        if(!(e instanceof TheListMachineBlock.Turning t))return knocks(time,at);
        if(!t.running())return false;
        double k=t.tick(time,0)+phase(at)+8;return Math.floor(k/STROKE)!=Math.floor((k-1)/STROKE);
    }
    /** The wheel's angle. */
    public static double wheel(long time,float partial,BlockPos at){
        double t=time%2000000L+partial+Math.floorMod(at.getX()*13+at.getZ()*7,400);
        return 2*Math.PI*(t/50.0)+.9*Math.sin(t/173.0)+.5*Math.sin(t/61.0);
    }
    /** The wheel came round past the top this tick: the pump rod turns over and the head creaks. */
    public static boolean creaks(long time,BlockPos at){return Math.floor(wheel(time,0,at)/(2*Math.PI))!=Math.floor(wheel(time-1,0,at)/(2*Math.PI));}
}
