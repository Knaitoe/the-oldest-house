package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.713, The list, part 4b-2 (docs/THE_LIST_0_4_713.md): the line. A sprinkler line laid down the home sideline for the night's
 * watering: aluminum pipe from a cap by the north end zone to an elbow level with the end of the home bench, a rubber hose from there
 * to the valve on its standpipe past the bench, and four risers off it with an impact head on each. The valve is anyone's: open, the
 * heads turn and throw water out over the field in their arcs, ticking, whether or not a hunt is on. The line's own part in the hunt,
 * one of the five forced onto a riser and the water turned on, is TheListHunt's (State.LINE).
 */
public final class TheListLine {
    private TheListLine(){}
    /** Where the line runs, in the town's coordinates: down the home sideline, from its cap to its elbow. */
    public static final int X=TheListSite.FIELD_W,N=TheListSite.FIELD_N+2,S=TheListSite.FIELD_Z+12;
    /** The risers along it; the third is level with the middle of the home bench, where Deek and Wes sit. */
    public static final int[] RISERS={N+2,N+14,TheListSite.FIELD_Z-2,TheListSite.FIELD_Z+10};
    /** The hose from the elbow, and the valve at its end, past the end of the bench. */
    public static final BlockPos HOSE=new BlockPos(X-1,0,S),VALVE=new BlockPos(X-2,0,S);
    /** The fitting's LINE stages: a length of pipe, the cap, a riser and a bloodied one, the elbow, the hose, the valve shut and open. */
    public static final int PIPE=0,CAP=1,RISER=2,BLOODY=3,ELBOW=4,HOSE_RUN=5,SHUT=6,OPEN=7;
    /** How far from a riser one held can be forced onto it. */
    public static final double REACH=3.5;
    /** How long the water runs through one on a riser before they go still. */
    public static final int BLEEDS=60;
    /** How far a reader in town hears the water come on through one of them. */
    public static final double HEARD=40;

    public static BlockPos riser(int i){return new BlockPos(X,0,RISERS[i]);}
    /** Whether a cell of the town is one of the risers, and which. */
    public static int riserAt(BlockPos rel){
        if(rel.getX()!=X||rel.getY()!=0)return -1;
        for(int i=0;i<RISERS.length;i++)if(RISERS[i]==rel.getZ())return i;
        return -1;
    }
    /** Whether the water is on: the valve stands open. */
    public static boolean open(ServerLevel l,BlockPos b){
        var at=b.offset(VALVE);if(!l.isLoaded(at))return false;
        var st=l.getBlockState(at);return TheListBlock.is(st,TheListBlock.Kind.LINE)&&st.getValue(TheListBlock.STAGE)==OPEN;
    }

    // ------------------------------------------------------------------------------------------------ a hand on it
    /**
     * A hand or a blow on the line (TheList.click, TheListBody.punched). The valve: anyone not a spectator opens or shuts it. A riser:
     * the slasher with hold of one of the five near enough forces them onto it (TheListHunt.riser); anything else is left to the town's
     * own answer. True when it was either.
     */
    public static boolean hand(ServerPlayer p,BlockPos b,BlockPos rel){
        if(p.isSpectator())return false;
        var l=p.serverLevel();var st=l.getBlockState(b.offset(rel));
        if(!TheListBlock.is(st,TheListBlock.Kind.LINE))return false;
        int stage=st.getValue(TheListBlock.STAGE);
        if(rel.equals(VALVE)&&stage>=SHUT){turn(l,b,p,stage==SHUT);return true;}
        int i=riserAt(rel);
        return i>=0&&stage==RISER&&TheListHunt.riser(p,b,rel);
    }
    /** The valve turned: the wheel up on its stem or down on the bonnet; the line knocks as it fills, or the heads run down. */
    static void turn(ServerLevel l,BlockPos b,ServerPlayer p,boolean on){
        var at=b.offset(VALVE);var st=l.getBlockState(at);
        l.setBlock(at,st.setValue(TheListBlock.STAGE,on?OPEN:SHUT),3);
        l.playSound(null,at.getX()+.5,at.getY()+1,at.getZ()+.5,TheListRegistry.VALVE.get(),SoundSource.BLOCKS,1F,on?1F:.9F);
        TheList.cue(p,on?TheListTexts.VALVE_OPEN:TheListTexts.VALVE_SHUT);
    }

    // ------------------------------------------------------------------------------------------------ the water
    /** The town's tick: with the valve open, each head turns in its arc and throws water, and ticks as it goes. */
    static void tick(ServerLevel l,BlockPos b,List<ServerPlayer> here){
        if(!open(l,b))return;
        // The level's own time, so that where a jet falls is the same wherever it is asked (TheListHunt's stains).
        int tick=(int)l.getGameTime();
        for(int i=0;i<RISERS.length;i++){
            var rel=riser(i);var at=b.offset(rel);if(!l.isLoaded(at))continue;
            boolean body=TheListHunt.onRiser(b,rel);
            if(tick%2==0)throwWater(l,b,i,tick,body);
            if(tick%100==i*25&&near(here,at,24))l.playSound(null,at,TheListRegistry.SPRINKLER.get(),SoundSource.AMBIENT,body?.5F:.7F,body?.8F:1F);
        }
    }
    /** Which way a head points now: an impact head steps across its half of the circle toward the field and is thrown back. */
    public static double angle(int i,int tick){
        int steps=24,k=(tick/2+i*7)%(steps+4);
        double t=k<steps?k/(double)(steps-1):1-(k-steps)/3.0;
        // East is the field; the arc runs from north to south across it.
        return Math.toRadians(-90+180*t);
    }
    /** The jet from a head, out along its angle and falling: water, or, from the head inside one of the five, what comes out of them. */
    static void throwWater(ServerLevel l,BlockPos b,int i,int tick,boolean body){
        double a=angle(i,tick);var rel=riser(i);
        double cx=b.getX()+rel.getX()+.5,cy=b.getY()+.9,cz=b.getZ()+rel.getZ()+.5;
        double dx=Math.cos(a),dz=Math.sin(a);
        if(body){
            var rand=l.getRandom();
            for(int n=0;n<3;n++){double s=.18+rand.nextDouble()*.22;
                l.sendParticles(TheListRegistry.BLOOD.get(),cx,cy,cz,0,dx*s,.1+rand.nextDouble()*.12,dz*s,1);}
            return;
        }
        for(int k=1;k<=5;k++){
            double r=k*.9,h=.3+.28*k-.075*k*k;
            l.sendParticles(ParticleTypes.SPLASH,cx+dx*r,cy+h,cz+dz*r,2,.06,.03,.06,0);
        }
    }
    /** Where the jet from a head comes down now, at a reach along its arc: on the ground in the town. */
    public static Vec3 falls(BlockPos b,int i,int tick,double reach){
        double a=angle(i,tick);var rel=riser(i);
        return new Vec3(b.getX()+rel.getX()+.5+Math.cos(a)*reach,b.getY(),b.getZ()+rel.getZ()+.5+Math.sin(a)*reach);
    }
    static boolean near(List<ServerPlayer> here,BlockPos at,double reach){for(var p:here)if(p.blockPosition().closerThan(at,reach))return true;return false;}
}
