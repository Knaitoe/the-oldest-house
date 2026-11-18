package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * 0.4.716, The list, part 4d-1 (docs/THE_LIST_0_4_716.md): the pasture's pumpjacks, by hand. Each has a band brake on its gearbox, whose
 * lever anyone who is not a spectator can haul on or knock off: on, the well slows and stands where it is; off, it runs on from there. The
 * brake is the well's own (TheListMachineBlock.Turning), seen and heard by everyone near it.
 * <p>
 * What the slasher does at a pumpjack is TheListHunt's (State.JACK): this keeps where things are at a well, the brake, and a well stalled
 * on what was put in its weight's way (the stall), which holds until the hunt ends.
 * <p>
 * Everything here is worked out in the machine block's own frame, as its renderer draws it: facing north, x across, y up, z back toward the
 * gearbox (TheListMachines). The gearbox's cell is two down and two back of the machine block; its shaft half a block into it and a quarter
 * of a block over its top. The near weight swings in the slot at the gearbox's west edge (x 0 to 3.5 pixels); a head laid on the ground
 * at that edge, a little behind the shaft, is under it as it comes down.
 */
public final class TheListJack {
    private TheListJack(){}
    /** The head laid in the weight's slot, in the frame: its middle across (just short of the gearbox's side), up from the ground, and
     * back from the shaft; and half its size. */
    static final double HEAD_X=-.03,HEAD_Y=.22,HEAD_Z=.35,HEAD_HALF=.24;
    /** The shaft, in the frame. */
    static final double SHAFT_Y=TheListMachines.CRANK_Y,SHAFT_Z=TheListMachines.CRANK_Z;
    /** How far in front of the head one forced down beside the gearbox kneels, bent over with the head on the ground (the pose's own
     * reach from the knees to the middle of the head: LiteraryRenderers.CastModel.hunted, PUMPED). */
    static final double BOWED=.93;
    /** How near the kneel one held must be for the slasher to force them down. */
    public static final double REACH=2.6;
    /**
     * The crank's angle (nought with the pins up, turning back) at which the near weight first meets a head laid in its slot, and the
     * last at which it is still over it, worked out from the weights' boxes (tools/draw_the_list_art.py, pumpjack_model 4: the weight
     * from 7 to 18 pixels out from the shaft and 8 either side of its middle, its lip from 18 to 20 out and 5 either side).
     */
    public static final double JAM,CLEAR;
    /** How far the motor grinds the weight on into what it has met, and over how many ticks. */
    public static final double GRIND=Math.toRadians(7);
    public static final int GRIND_TICKS=30;
    /** The least the weight must still have to come before a head is put in under it. */
    public static final double LEAD=Math.toRadians(48);
    /** How long the motor labours against the stall, heard every two seconds, before it is only stood there. */
    static final int STRAIN=110,STRAIN_EVERY=40;
    static{
        double first=Double.NaN,last=Double.NaN;
        for(double a=-180;a<180;a+=.25){
            if(!touches(Math.toRadians(a)))continue;
            if(Double.isNaN(first))first=a;last=a;
        }
        JAM=Math.toRadians(first);CLEAR=Math.toRadians(last);
    }
    /** Whether the near weight, at a crank angle, is over the head's box in its slot (y and z from the shaft). */
    static boolean touches(double c){
        // The head's box from the shaft: the ground is two blocks under the machine block's corner, and the shaft SHAFT_Y from that corner.
        double ground=-2-SHAFT_Y,hy0=ground+HEAD_Y-HEAD_HALF,hy1=ground+HEAD_Y+HEAD_HALF,hz0=HEAD_Z-HEAD_HALF,hz1=HEAD_Z+HEAD_HALF;
        double cos=Math.cos(c),sin=Math.sin(c);
        for(double[] box:new double[][]{{7/16D,18/16D,-.5,.5},{18/16D,20/16D,-5/16D,5/16D}})
            for(int i=0;i<=16;i++)for(int j=0;j<=16;j++){
                double r=box[0]+(box[1]-box[0])*i/16,t=box[2]+(box[3]-box[2])*j/16;
                double y=-r*cos-t*sin,z=-r*sin+t*cos;
                if(y>=hy0&&y<=hy1&&z>=hz0&&z<=hz1)return true;
            }
        return false;
    }

    // ------------------------------------------------------------------------------------------------ where things are
    public static int count(){return TheListSite.JACKS.length;}
    /** The machine block, in the level. */
    public static BlockPos machine(BlockPos b,int i){return b.offset(TheListSite.jackMachine(i));}
    /** The gearbox, in the town. */
    public static BlockPos gearbox(int i){return TheListSite.JACKS[i].relative(TheListSite.JACK_FACING[i].getOpposite(),2);}
    /** Which well a block of the town is the gearbox of, or -1. */
    public static int of(BlockPos rel){for(int i=0;i<count();i++)if(gearbox(i).equals(rel))return i;return -1;}
    /** A point in a well's frame (from its machine block's lower corner, facing north), in the level: turned as the renderer turns it. */
    public static Vec3 frame(BlockPos b,int i,double x,double y,double z){
        double th=Math.toRadians(180-TheListSite.JACK_FACING[i].toYRot()),cos=Math.cos(th),sin=Math.sin(th);
        double px=x-.5,pz=z-.5;
        var m=machine(b,i);
        return new Vec3(m.getX()+.5+px*cos+pz*sin,m.getY()+y,m.getZ()+.5-px*sin+pz*cos);
    }
    /** A yaw in the frame (north-facing), in the level. */
    static float yaw(int i,float inFrame){return Mth.wrapDegrees(inFrame-(180-TheListSite.JACK_FACING[i].toYRot()));}
    /** Where the head is laid: on the ground at the gearbox's west edge, a little behind the shaft. */
    public static Vec3 head(BlockPos b,int i){return frame(b,i,HEAD_X,-2+HEAD_Y,SHAFT_Z+HEAD_Z);}
    /** Where one forced down kneels: back from the head on its own side of the gearbox, facing it. */
    public static Vec3 kneel(BlockPos b,int i){return frame(b,i,HEAD_X-BOWED,-2,SHAFT_Z+HEAD_Z);}
    /** The way they face, kneeling there: across the frame toward the gearbox (east, in a well that faces north). */
    public static float kneelYaw(int i){return yaw(i,-90F);}
    /** Where blood off the head lands: on the ground beside the gearbox, just out from it. */
    public static Vec3 under(BlockPos b,int i){return frame(b,i,HEAD_X-.22,-2,SHAFT_Z+HEAD_Z);}

    // ------------------------------------------------------------------------------------------------ the well's own tick
    static @Nullable TheListMachineBlock.Turning turning(ServerLevel l,BlockPos b,int i){
        var at=machine(b,i);if(!l.isLoaded(at))return null;
        return l.getBlockEntity(at) instanceof TheListMachineBlock.Turning t?t:null;
    }
    /** The crank's angle now, at the well's own tick. */
    public static double angle(ServerLevel l,BlockPos b,int i){var t=turning(l,b,i);return TheListMachines.crankAt(t==null?l.getGameTime():t.tick(l.getGameTime(),0),machine(b,i));}
    /** An angle less another, turned into nought to a whole turn. */
    public static double ahead(double from,double to){double d=(to-from)%(2*Math.PI);return d<0?d+2*Math.PI:d;}
    /** Whether the weight is in the slot now, over where a head would lie. */
    public static boolean inSlot(double a){return ahead(JAM,a)<=ahead(JAM,CLEAR);}
    /** Whether a head may be put in under it now: out of the slot, and with at least LEAD still to come before it reaches it. */
    public static boolean clearOf(double a){return ahead(CLEAR,a)>0&&ahead(CLEAR,a)<ahead(CLEAR,JAM-LEAD);}
    /** Whether the brake is on. */
    public static boolean braked(ServerLevel l,BlockPos b,int i){var t=turning(l,b,i);return t!=null&&t.braked();}
    /** Whether the well runs. */
    public static boolean running(ServerLevel l,BlockPos b,int i){var t=turning(l,b,i);return t==null||t.running();}
    /** The well's own tick now. */
    public static double tick(ServerLevel l,BlockPos b,int i){var t=turning(l,b,i);return t==null?l.getGameTime():t.tick(l.getGameTime(),0);}
    /** For tests: the well set running with its crank at an angle (degrees, nought with the pins up) now. */
    public static void setAngle(ServerLevel l,BlockPos b,int i,double degrees){
        var t=turning(l,b,i);if(t==null)return;
        t.run(l.getGameTime());
        double now=TheListMachines.crankAt(t.tick(l.getGameTime(),0),machine(b,i));
        t.offset+=ahead(now,Math.toRadians(degrees))/(2*Math.PI)*TheListMachines.STROKE;t.sync();
    }

    // ------------------------------------------------------------------------------------------------ a hand on it
    /**
     * A hand or a blow on a pumpjack's gearbox (TheList.click, TheListBody.punched). Spectators touch nothing. The hunt's first
     * (TheListHunt.jack); otherwise the brake, hauled on or knocked off.
     */
    public static boolean hand(ServerPlayer p,BlockPos b,BlockPos rel){
        int i=of(rel);if(i<0)return false;
        if(p.isSpectator())return true;
        if(TheListHunt.jack(p,b,i))return true;
        brake(p.serverLevel(),b,i,!braked(p.serverLevel(),b,i),p);
        return true;
    }
    /** The brake hauled on or knocked off: the lever and the band heard, the one whose hand it was told. */
    static void brake(ServerLevel l,BlockPos b,int i,boolean on,@Nullable ServerPlayer p){
        var t=turning(l,b,i);if(t==null)return;
        if(on)t.brake(l.getGameTime());else t.run(l.getGameTime());
        var at=frame(b,i,.6,-.4,2.65);
        l.playSound(null,at.x,at.y,at.z,TheListRegistry.BRAKE.get(),SoundSource.BLOCKS,1F,on?.95F:1.1F);
        if(p!=null)TheList.cue(p,on?TheListTexts.BRAKE_ON:TheListTexts.BRAKE_OFF);
    }

    // ------------------------------------------------------------------------------------------------ stalled on something
    /** A well stalled on what was put in its weight's way: the tick it met it, and how long since. */
    static final class Stall{final double met;int t;Stall(double met){this.met=met;}}
    static final Map<Long,Stall> STALLS=new HashMap<>();
    static long key(BlockPos b,int i){return b.offset(TheListSite.jackMachine(i)).asLong();}
    /** Whether a well stands stalled. */
    public static boolean stalled(BlockPos b,int i){return STALLS.containsKey(key(b,i));}
    /** The weight down on what is in its way: the well held there, the motor straining, and it grinds on a little into it. */
    static void stall(ServerLevel l,BlockPos b,int i,double met){
        var t=turning(l,b,i);if(t==null)return;
        STALLS.put(key(b,i),new Stall(met));t.stall(met,true);
        var at=head(b,i);l.playSound(null,at.x,at.y,at.z,TheListRegistry.STRAIN.get(),SoundSource.BLOCKS,1.3F,.9F);
    }
    /** The town's tick: each stalled well ground on into what holds it, the motor heard labouring for a while, then only stood there. */
    static void tick(ServerLevel l,BlockPos b,List<ServerPlayer> here){
        for(int i=0;i<count();i++){
            var s=STALLS.get(key(b,i));if(s==null)continue;
            var t=turning(l,b,i);if(t==null)continue;
            s.t++;
            if(s.t<=GRIND_TICKS&&s.t%2==0)t.stall(s.met+GRIND/(2*Math.PI)*TheListMachines.STROKE*s.t/GRIND_TICKS,true);
            if(s.t<STRAIN&&s.t%STRAIN_EVERY==0){var at=head(b,i);l.playSound(null,at.x,at.y,at.z,TheListRegistry.STRAIN.get(),SoundSource.BLOCKS,1.1F,.85F+l.getRandom().nextFloat()*.1F);}
            if(s.t==STRAIN)t.stall(t.tick(l.getGameTime(),0),false);
        }
    }
    /** Every well let go and running from where it stood, its brake off, nothing stalled on it: the hunt begun or ended. */
    public static void reset(ServerLevel l,BlockPos b){
        for(int i=0;i<count();i++){STALLS.remove(key(b,i));var t=turning(l,b,i);if(t!=null&&(!t.running()||t.braked()||t.strain()))t.run(l.getGameTime());}
    }
    public static void clearAll(){STALLS.clear();}
    /** The direction a blood stain under the head runs: back toward where they kneel. */
    static Direction runs(int i){return TheListSite.JACK_FACING[i].getClockWise().getOpposite();}
}
