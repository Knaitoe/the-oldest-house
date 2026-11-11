package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.482, Bike night: the ways through Calder Falls, for those who know them. The proposal: "The readers can take any way; the
 * bullies know them all", and "they split up. One stays on the readers' tail, and the others take the town's back ways to head
 * them off at a corner."
 *
 * <p>A place is a point a bike can stand on, relative to the park's base. A way is a straight run between two places that a
 * bike rides without touching anything. Streets are ways along the middle of the road. The back ways are here too: the gaps
 * between the shops, the two alleys, the dock, the empty lot's trail, the drive-thru lane, the grocery's yard, the cut and the
 * path out of Elm Court. A tight way is ridden slowly and on its line.
 *
 * <p>What only a reader fits through is not here: the two holes in the school's fence, and the two yards with boards gone. The
 * three go round.
 */
public final class CalderRoutes {
    private CalderRoutes(){}
    /** A place: where, how high the ground is there (the dock is a block up), and the street it is on, for the account. */
    public record Place(String name,double x,double y,double z,String street){}
    /** A way between two places: how long, whether it is ridden slowly on its line, and whether it is a back way. */
    public record Way(int a,int b,double length,boolean tight,boolean back){
        public int other(int from){return from==a?b:a;}
    }
    private static final List<Place> PLACES=new ArrayList<>();
    private static final List<Way> WAYS=new ArrayList<>();
    private static final Map<String,Integer> BY_NAME=new HashMap<>();
    private static List<List<Way>> from=List.of();

    private static void place(String name,double x,double z,String street){place(name,x,0,z,street);}
    private static void place(String name,double x,double y,double z,String street){
        if(BY_NAME.put(name,PLACES.size())!=null)throw new IllegalStateException("two places called "+name);
        PLACES.add(new Place(name,x,y,z,street));
    }
    private static void way(boolean tight,boolean back,String... names){
        for(int i=0;i+1<names.length;i++){
            int a=id(names[i]),b=id(names[i+1]);var p=PLACES.get(a);var q=PLACES.get(b);
            WAYS.add(new Way(a,b,Math.hypot(p.x()-q.x(),p.z()-q.z()),tight,back));
        }
    }
    private static void street(String... names){way(false,false,names);}
    private static void back(String... names){way(false,true,names);}
    private static void tight(String... names){way(true,true,names);}
    public static int id(String name){var i=BY_NAME.get(name);if(i==null)throw new IllegalArgumentException("no place called "+name);return i;}
    public static Place place(int id){return PLACES.get(id);}
    public static int places(){return PLACES.size();}
    public static List<Way> ways(){return WAYS;}
    public static List<Way> from(int id){return from.get(id);}
    /** Where a place is in the world. */
    public static Vec3 at(BlockPos base,int id){var p=PLACES.get(id);return new Vec3(base.getX()+p.x(),base.getY()+p.y(),base.getZ()+p.z());}

    static{
        // ---- The streets, along the middle of the road. A street whose middle row is c has the middle of its road at c + .5.
        place("gate_in",-.5,-43.5,"the park");place("gate_out",-.5,-47.8,"the park");
        place("park_mill",.5,-51.5,"Park Street");place("park_lot",-8.5,-51.5,"Park Street");place("park_bigtop",25,-51.5,"Park Street");
        place("park_west",-49.5,-51.5,"Park Street");place("park_east",50.5,-51.5,"Park Street");
        place("mill_lot",.5,-63.5,"Mill Street");place("mill_alley",.5,-70.5,"Mill Street");place("mill_back",.5,-80,"Mill Street");place("mill_main",.5,-91.5,"Mill Street");
        place("main_west",-49.5,-91.5,"Main Street");place("main_gap_hb",-33,-91.5,"Main Street");place("main_food",-27.5,-91.5,"Main Street");place("main_gap_bt",-22.5,-91.5,"Main Street");
        place("main_north",2.5,-91.5,"Main Street");place("main_gap_wv",20,-91.5,"Main Street");place("main_bigtop",22.5,-91.5,"Main Street");
        place("main_gap_vb",30.5,-91.5,"Main Street");place("main_lane",34.5,-91.5,"Main Street");place("main_flags",40.5,-91.5,"Main Street");place("main_east",49.6,-91.5,"Main Street");
        place("west_alley",-49.5,-71.5,"West Avenue");place("west_food",-49.5,-103.5,"West Avenue");place("west_bend",-45.5,-115.5,"West Avenue");
        place("west_elm",-45.5,-128.5,"West Avenue");place("west_church",-45.5,-164.5,"West Avenue");
        place("east_front",50.5,-62,"East Avenue");place("east_back",50.5,-80,"East Avenue");place("east_bend",50.5,-89.5,"East Avenue");place("east_bend2",46.5,-99.5,"East Avenue");
        place("east_alley",46.5,-110.5,"East Avenue");place("east_elm",46.5,-120.5,"East Avenue");place("east_church",46.5,-164.5,"East Avenue");
        place("north_lot",2.5,-101.5,"Mill Street");place("north_alley",2.5,-112.5,"Mill Street");place("north_elm",2.5,-123.4,"Mill Street");
        place("elm_bend",-11.5,-128.5,"Elm Street");place("elm_yard",-6.5,-126.7,"Elm Street");place("elm_cut",-5,-126.2,"Elm Street");place("elm_bend2",10.5,-120.5,"Elm Street");
        place("elm_court",22.5,-120.5,"Elm Street");place("elm_holes",29,-120.5,"Elm Street");
        place("church_bend",-31.5,-164.5,"Church Lane");place("church_bend2",-21.5,-170.5,"Church Lane");place("church_cut",-5,-170.5,"Church Lane");place("church_gate",-.5,-170.5,"Church Lane");
        place("church_bend3",14.5,-170.5,"Church Lane");place("church_path",22,-166.6,"Church Lane");place("church_bend4",26.5,-164.5,"Church Lane");
        place("cemetery_gate",-.5,-175.4,"the cemetery's gate");
        street("gate_in","gate_out","park_mill");
        street("park_west","park_lot","park_mill","park_bigtop","park_east");
        street("park_mill","mill_lot","mill_alley","mill_back","mill_main","main_north");
        street("main_west","main_gap_hb","main_food","main_gap_bt","mill_main");
        street("main_north","main_gap_wv","main_bigtop","main_gap_vb","main_lane","main_flags","main_east");
        street("park_west","west_alley","main_west","west_food","west_bend","west_elm","west_church");
        street("park_east","east_front","east_back","east_bend","main_east","east_bend2","east_alley","east_elm","east_church");
        street("main_north","north_lot","north_alley","north_elm");
        street("west_elm","elm_bend","elm_yard","elm_cut","north_elm","elm_bend2","elm_court","elm_holes","east_elm");
        street("west_church","church_bend","church_bend2","church_cut","church_gate","church_bend3","church_path","church_bend4","east_church");
        street("church_gate","cemetery_gate");

        // ---- Behind and between the shops south of Main Street, and the empty lot. The lot's way goes beside its trail and not along
        // it: the ramp on the trail is ridden up from the east and off its high end, and from the west that end is a wall.
        place("gap_hb",-33,-86,"the gap by the bank");place("gap_hb_south",-33,-76,"the gap by the bank");
        place("gap_bt",-22.5,-86,"the gap by the theater");place("gap_bt_south",-22.5,-73.5,"the gap by the theater");
        place("alley_south_west",-43.5,-71.5,"the alley behind the theater");place("alley_south_gap",-33,-71.5,"the alley behind the theater");
        place("alley_south_mid",-25,-71.5,"the alley behind the theater");place("alley_south_bend",-22.4,-70.5,"the alley behind the theater");
        place("alley_south_east",-6.5,-70.5,"the alley behind the theater");
        place("lot_gap",-22.4,-68.2,"the empty lot");place("lot_turn",-22.4,-65.6,"the empty lot");place("lot_ramp",-12.5,-65.6,"the empty lot");
        place("lot_corner",-8.5,-63.5,"the empty lot");place("lot_mouth",-8.5,-58,"the empty lot");
        tight("main_gap_hb","gap_hb","gap_hb_south","alley_south_gap");
        tight("main_gap_bt","gap_bt","gap_bt_south","alley_south_bend");
        back("west_alley","alley_south_west","alley_south_gap","alley_south_mid");
        tight("alley_south_mid","alley_south_bend");
        back("alley_south_bend","alley_south_east","mill_alley");
        tight("alley_south_bend","lot_gap","lot_turn");
        back("lot_turn","lot_ramp","lot_corner","lot_mouth","park_lot");

        // ---- Big Top's lot: in front, behind, and the drive-thru lane between.
        place("bigtop_front",25,-62,"Big Top's lot");place("bigtop_front_west",10,-62,"Big Top's lot");place("lane_south",34.5,-63,"the drive-thru");
        place("lane_north",34.5,-80,"the drive-thru");place("bigtop_back",22.5,-81.5,"Big Top's lot");place("bigtop_back_west",10,-81,"Big Top's lot");
        back("park_bigtop","bigtop_front","lane_south");back("mill_lot","bigtop_front_west","bigtop_front");back("east_front","lane_south");
        tight("lane_south","lane_north");
        back("lane_north","bigtop_back","main_bigtop");back("lane_north","main_lane");back("mill_back","bigtop_back_west","bigtop_back");back("lane_north","east_back");

        // ---- Behind and between the shops north of Main Street.
        place("gap_wv",20,-98.5,"the gap by the laundromat");place("gap_wv_north",20,-109.5,"the gap by the laundromat");
        place("gap_vb",30.5,-98.5,"the gap by the barber's");place("gap_vb_north",30.5,-109.5,"the gap by the barber's");
        place("alley_north_west",7.5,-112.5,"the alley behind the laundromat");place("alley_north_dock",20,-112.5,"the alley behind the laundromat");
        place("alley_north_mid",21.5,-110.6,"the alley behind the laundromat");place("alley_north_holes",29,-112.6,"the alley behind the laundromat");
        place("alley_north_east",38.5,-110.6,"the alley behind the laundromat");place("flags",40.5,-108,"the alley behind the laundromat");place("flags_south",40.5,-99,"the alley behind the laundromat");
        place("dock_foot",8.6,-110.5,"the loading dock");place("dock_west",11.5,1,-110.5,"the loading dock");place("dock_east",18.4,1,-110.5,"the loading dock");
        tight("main_gap_wv","gap_wv","gap_wv_north","alley_north_mid");
        tight("main_gap_vb","gap_vb","gap_vb_north");back("gap_vb_north","alley_north_mid");back("gap_vb_north","alley_north_east");back("gap_vb_north","alley_north_holes");
        back("north_alley","alley_north_west","alley_north_dock");tight("alley_north_dock","alley_north_mid");
        back("alley_north_mid","alley_north_east","flags");tight("flags","flags_south","main_flags");back("alley_north_east","east_alley");
        tight("alley_north_west","dock_foot","dock_west","dock_east","alley_north_mid");
        back("alley_north_mid","alley_north_holes");tight("alley_north_holes","elm_holes");

        // ---- The grocery's lot and its yard.
        place("food_east",-4.5,-101.5,"the Food Fair's lot");place("food_mid",-27.5,-101.5,"the Food Fair's lot");place("food_west",-42.5,-101.5,"the Food Fair's lot");
        place("yard_south",-6.5,-112,"the Food Fair's yard");place("yard_north",-6.5,-122.5,"the Food Fair's yard");
        back("north_lot","food_east","food_mid","food_west","west_food");back("main_food","food_mid");
        back("food_east","yard_south");tight("yard_south","yard_north","elm_yard");

        // ---- The cut, Elm Court and the path out of its head.
        place("cut_south",-5,-132,"the cut");place("cut_north",-5,-166,"the cut");
        place("court",22.5,-131,"Elm Court");place("circle",22.5,-140.5,"Elm Court");place("path_south",22,-147.5,"the path out of Elm Court");place("path_north",22,-162,"the path out of Elm Court");
        tight("elm_cut","cut_south","cut_north","church_cut");
        street("elm_court","court","circle");tight("circle","path_south","path_north","church_path");

        var out=new ArrayList<List<Way>>();for(int i=0;i<PLACES.size();i++)out.add(new ArrayList<>());
        for(var w:WAYS){out.get(w.a()).add(w);out.get(w.b()).add(w);}
        from=List.copyOf(out);
    }

    /** The place nearest a point, of those whose ground is at the same height (the dock's two are a block up). */
    public static int nearest(BlockPos base,Vec3 at){
        int best=0;double least=Double.MAX_VALUE;
        for(int i=0;i<PLACES.size();i++){
            var p=PLACES.get(i);if(Math.abs(base.getY()+p.y()-at.y)>.6)continue;
            double dx=base.getX()+p.x()-at.x,dz=base.getZ()+p.z()-at.z,d=dx*dx+dz*dz;
            if(d<least){least=d;best=i;}
        }
        return best;
    }
    /** The way a point is nearest to, by its distance from the way's line; null on the dock's height mismatch only if nothing else is near. */
    public static Way nearestWay(BlockPos base,Vec3 at){
        Way best=WAYS.get(0);double least=Double.MAX_VALUE;
        for(var w:WAYS){
            var p=PLACES.get(w.a());var q=PLACES.get(w.b());if(p.y()>0||q.y()>0)continue;
            double d=fromSegment(at.x-base.getX(),at.z-base.getZ(),p.x(),p.z(),q.x(),q.z());
            if(d<least){least=d;best=w;}
        }
        return best;
    }
    static double fromSegment(double x,double z,double ax,double az,double bx,double bz){
        double dx=bx-ax,dz=bz-az,len=dx*dx+dz*dz,t=len==0?0:Math.max(0,Math.min(1,((x-ax)*dx+(z-az)*dz)/len));
        return Math.hypot(x-(ax+dx*t),z-(az+dz*t));
    }

    /**
     * The shortest way from one place to another, as the places gone through, the first being {@code from}. A place to keep
     * clear of is gone round if there is a way round, and through if there is not. Back ways count for a little less than their
     * length: those who know them like them.
     */
    public static int[] path(int from,int to,int avoid){
        int[] found=search(from,to,avoid);
        return found!=null||avoid<0?found==null?new int[]{from}:found:search(from,to,-1);
    }
    private static @Nullable int[] search(int start,int goal,int avoid){
        int n=PLACES.size();double[] cost=new double[n];int[] came=new int[n];boolean[] done=new boolean[n];
        Arrays.fill(cost,Double.MAX_VALUE);Arrays.fill(came,-1);cost[start]=0;
        for(;;){
            int at=-1;for(int i=0;i<n;i++)if(!done[i]&&cost[i]<Double.MAX_VALUE&&(at<0||cost[i]<cost[at]))at=i;
            if(at<0)return null;
            if(at==goal)break;
            done[at]=true;
            for(var w:CalderRoutes.from.get(at)){
                int next=w.other(at);if(done[next]||next==avoid&&next!=goal)continue;
                double c=cost[at]+w.length()*(w.back()?.9:1)+(w.tight()?2:0);
                if(c<cost[next]){cost[next]=c;came[next]=at;}
            }
        }
        var back=new ArrayList<Integer>();for(int at=goal;at>=0;at=came[at])back.add(at);
        int[] out=new int[back.size()];for(int i=0;i<out.length;i++)out[i]=back.get(out.length-1-i);
        return out;
    }
    /** How far it is along a path. */
    public static double length(int[] path){
        double sum=0;for(int i=0;i+1<path.length;i++){var p=PLACES.get(path[i]);var q=PLACES.get(path[i+1]);sum+=Math.hypot(p.x()-q.x(),p.z()-q.z());}
        return sum;
    }

    /**
     * Whether a bike gets along a way without touching anything: a body its size is put down every quarter of a block of it.
     * Going up onto the dock or off it, the step itself is not looked at.
     */
    public static @Nullable String blocked(ServerLevel l,BlockPos base,Way w){
        var p=PLACES.get(w.a());var q=PLACES.get(w.b());double len=Math.max(.25,w.length());int steps=(int)Math.ceil(len/.25);
        for(int i=0;i<=steps;i++){
            double t=i/(double)steps,x=base.getX()+p.x()+(q.x()-p.x())*t,z=base.getZ()+p.z()+(q.z()-p.z())*t;
            double y=base.getY()+(p.y()==q.y()?p.y():t<.5?p.y():q.y());
            if(p.y()!=q.y()&&t>.15&&t<.85)continue;
            if(!l.noCollision(new AABB(x-.3,y+.05,z-.3,x+.3,y+.9,z+.3)))return p.name()+" to "+q.name()+" at "+(x-base.getX())+","+(z-base.getZ());
            if(l.noCollision(new AABB(x-.2,y-.6,z-.2,x+.2,y-.05,z+.2)))return p.name()+" to "+q.name()+": nothing under it at "+(x-base.getX())+","+(z-base.getZ());
        }
        return null;
    }
}
