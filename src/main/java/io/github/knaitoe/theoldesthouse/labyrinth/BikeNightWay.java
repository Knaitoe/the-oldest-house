package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.675 (owner: "the players need guidance towards the cemetery. Considerations must be made for this both on and off their bikes,
 * ie whether their bike was stolen by the bully npcs or not"). Once the three go for their bikes, a reader who is not getting nearer
 * the cemetery on Calder Hill is told which way it lies and what to make for next, by the town's own ways (CalderRoutes): on a bike, the
 * streets and the back ways a bike rides; on foot (left on the ground, or their bike taken), the same ways, and it says so. It tells
 * nobody who is already at the gate or through it, and nobody twice in a short while; a reader keeping on toward the hill hears
 * nothing.
 */
public final class BikeNightWay {
    private BikeNightWay(){}
    /** How often a reader's way is looked at; how much nearer they must have come since to be left alone; how near the gate is "there". */
    static final int EVERY=200;static final double PROGRESS=6,THERE=10;
    /** Each reader's distance from the gate when their way was last looked at. */
    private static final Map<UUID,Double> LAST=new HashMap<>();
    public static void clear(){LAST.clear();}

    static void tick(ServerLevel l,BikeNight.Night n,List<ServerPlayer> here){
        if(n.pack.stage<BikeNightBullies.GET_THEM||n.tick%20!=0)return;
        var gate=CalderRoutes.at(n.base,CalderRoutes.id("cemetery_gate"));
        for(var p:here){
            if(p.isSpectator())continue;var own=BikeNight.own(p);
            if(own.getBoolean("Through")||BikeNightGraveyard.in(n.base,p.position())||BikeNightGraveyard.dawn(n,p)){LAST.remove(p.getUUID());continue;}
            double far=Math.sqrt(BikeNightGus.flat(p.position(),gate));
            if(far<THERE){LAST.remove(p.getUUID());continue;}
            // Carried on someone's pegs: their rider is told.
            if(p.getVehicle() instanceof NightBike bike&&bike.getFirstPassenger()!=p)continue;
            var was=LAST.get(p.getUUID());
            if(was==null){if(n.tick%EVERY==0||n.pack.stage==BikeNightBullies.GET_THEM){LAST.put(p.getUUID(),far);}continue;}
            if(n.tick%EVERY!=0)continue;
            LAST.put(p.getUUID(),far);
            if(was-far>=PROGRESS)continue;
            BikeNight.cue(p,line(n,p));
        }
    }
    /** What a reader is told: which way the hill is from where they face, and the next place on the shortest way there. */
    static String line(BikeNight.Night n,ServerPlayer p){
        int next=next(n,p.position());var to=CalderRoutes.at(n.base,next);
        boolean riding=p.getVehicle() instanceof NightBike;
        String way=side(p,to),place=CalderRoutes.place(next).street();
        if(riding)return String.format(BikeNightTexts.WAY_RIDE,way,place);
        return String.format(BikeNightTexts.WAY_FOOT,way,place);
    }
    /** "ahead", "to your left", "to your right" or "behind you", of a point from where the reader is looking. */
    static String side(ServerPlayer p,Vec3 to){
        double dx=to.x-p.getX(),dz=to.z-p.getZ();float want=(float)(Mth.atan2(dz,dx)*Mth.RAD_TO_DEG)-90;
        float off=Mth.wrapDegrees(want-p.getYRot());
        if(Math.abs(off)<=40)return BikeNightTexts.AHEAD;if(Math.abs(off)>=135)return BikeNightTexts.BEHIND;
        return off>0?BikeNightTexts.RIGHT:BikeNightTexts.LEFT;
    }
    /** The first place on the shortest way from where the reader is to the cemetery's gate that is not right beside them. */
    static int next(BikeNight.Night n,Vec3 at){
        int from=CalderRoutes.nearest(n.base,at),goal=CalderRoutes.id("cemetery_gate"),size=CalderRoutes.places();
        double[] dist=new double[size];int[] prev=new int[size];boolean[] done=new boolean[size];Arrays.fill(dist,Double.MAX_VALUE);Arrays.fill(prev,-1);dist[from]=0;
        for(int k=0;k<size;k++){
            int u=-1;for(int i=0;i<size;i++)if(!done[i]&&(u<0||dist[i]<dist[u]))u=i;
            if(u<0||dist[u]==Double.MAX_VALUE||u==goal)break;done[u]=true;
            for(var w:CalderRoutes.from(u)){int v=w.other(u);double d=dist[u]+w.length();if(d<dist[v]){dist[v]=d;prev[v]=u;}}
        }
        var path=new ArrayList<Integer>();for(int v=goal;v>=0;v=prev[v]){path.add(0,v);if(v==from)break;}
        for(int id:path)if(BikeNightGus.flat(at,CalderRoutes.at(n.base,id))>=5*5)return id;
        return goal;
    }
}
