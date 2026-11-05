package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.310, A Deer in the Road (docs/DEER_ROAD_0_4_310.md): the sedan, in model pixels about its root cell's centre. It faces north;
 * the driver's door is on the west. Every collision cell and every rendered face come from these boxes, so the two never disagree.
 * Materials repeat at sixteen pixels to the block (tools/draw_deer_road_art.py).
 *
 * <p>Three doors move: the driver's door, the rear door behind it and the front passenger door. Each swings a right angle about a
 * hinge at its front edge, so its open collision is as exact as its closed one. The rear door on the passenger side does not open.
 * The boot lid lifts, drawn only. The rear bench has three states: whole, pushed up against its belt, and split at the seam.
 */
public final class DeerCarMesh {
    public record Box(float x0,float y0,float z0,float x1,float y1,float z1,String material) {
        public Box moved(float dx,float dy,float dz){return new Box(x0+dx,y0+dy,z0+dz,x1+dx,y1+dy,z1+dz,material);}
    }
    /** A door: its boxes, closed, and the hinge it swings on (the outward side is the sign of {@code out}). */
    public record Door(List<Box> boxes,float hingeX,float hingeZ,int out) {
        /**
         * The same boxes swung a right angle outward about the hinge: a point a distance back along the leaf ends that distance out from
         * the car's side, and the leaf's inner face turns to face the doorway behind it. As a rotation about the vertical through the
         * hinge, (dx, dz) becomes (out * dz, -out * dx): {@code out} times ninety degrees.
         */
        public List<Box> open(){
            var swung=new ArrayList<Box>();
            for(var b:boxes){
                float ax=hingeX+out*(b.z0-hingeZ),bx=hingeX+out*(b.z1-hingeZ);
                float az=hingeZ-out*(b.x0-hingeX),bz=hingeZ-out*(b.x1-hingeX);
                swung.add(new Box(Math.min(ax,bx),b.y0,Math.min(az,bz),Math.max(ax,bx),b.y1,Math.max(az,bz),b.material));
            }
            return List.copyOf(swung);
        }
    }
    public static final int DRIVER=0,REAR=1,PASSENGER=2;
    /** The seat a rider sits in: the native riding pose holds the hips twelve pixels over the origin, the cushion's top at fourteen. */
    public static final Vec3 DRIVER_SEAT=new Vec3(-9/16D,2/16D,1/16D),PASSENGER_SEAT=new Vec3(9/16D,2/16D,1/16D);
    /**
     * Where the first woman sags against the driver's door: sitting on the road with her back to its outer panel when it is shut (she
     * faces west, away from the car), or against the inner face of the open leaf, in the doorway, when it stands open (she faces south).
     */
    public static final Vec3 AGAINST_SHUT=new Vec3(-28/16D,0,-2/16D),AGAINST_OPEN=new Vec3(-27/16D,0,-6/16D);
    /** The rear bench's middle, where the seam gives; and the rear doorway on the driver's side she squeezes out through. */
    public static final Vec3 BENCH=new Vec3(-4/16D,15/16D,20/16D),REAR_DOORWAY=new Vec3(-30/16D,0,19/16D);
    /** The boot, where the triangle is kept; the glovebox side of the dashboard. */
    public static final Vec3 BOOT=new Vec3(0,20/16D,34/16D);
    public static final List<Box> SHELL=shell(),BENCH_WHOLE=bench(0),BENCH_PUSHED=bench(1),BENCH_SPLIT=bench(2),BOOT_LID=bootLid(),WHEEL=wheel();
    public static final Door DRIVER_DOOR=door(-1,-14,10),REAR_DOOR=door(-1,10,27),PASSENGER_DOOR=door(1,-14,10);
    public static final List<Door> DOORS=List.of(DRIVER_DOOR,REAR_DOOR,PASSENGER_DOOR);
    /** Lamps drawn bright: the low beams, the four hazard lamps, the tail lamps and the dome light. */
    public static final List<Box> HEADLAMPS=List.of(new Box(-15,12,-40.2F,-9,16,-40.05F,"headlamp"),new Box(9,12,-40.2F,15,16,-40.05F,"headlamp"));
    public static final List<Box> HAZARDS=List.of(new Box(-18.1F,12,-38,-17.95F,15,-35,"amber"),new Box(17.95F,12,-38,18.1F,15,-35,"amber"),
            new Box(-16,13,39.05F,-12,17,39.2F,"amber"),new Box(12,13,39.05F,16,17,39.2F,"amber"));
    public static final List<Box> TAILS=List.of(new Box(-11,13,39.05F,-7,17,39.2F,"tail"),new Box(7,13,39.05F,11,17,39.2F,"tail"));
    public static final Box DOME=new Box(-2,33.4F,4,2,34,8,"dome");
    private DeerCarMesh(){}
    private static void add(List<Box> out,float x,float y,float z,float X,float Y,float Z,String m){out.add(new Box(x,y,z,X,Y,Z,m));}
    private static List<Box> shell(){
        var b=new ArrayList<Box>();
        // The floor pan and the sills, down to three pixels so nothing under the car shows through from the side.
        add(b,-17,4,-36,17,10,36,"under");
        add(b,-18,3,-14,-16,10,28,"paint");add(b,16,3,-14,18,10,28,"paint");
        // The front: bumper, grille between the lamps, the wings and the bonnet.
        add(b,-18,8,-40,18,12,-38,"chrome");
        add(b,-18,12,-40,18,17,-38,"grille");
        add(b,-18,10,-38,-16,18,-15,"paint");add(b,16,10,-38,18,18,-15,"paint");
        add(b,-16,10,-38,16,17,-16,"paint");add(b,-16,17,-38,16,18,-16,"paint");
        // The windscreen and its pillars; the roof; the rear screen and the C-pillars.
        add(b,-18,18,-16,-16,33,-14,"paint");add(b,16,18,-16,18,33,-14,"paint");
        add(b,-16,18,-15.5F,16,33,-14.5F,"glass");
        add(b,-18,33,-16,18,36,30,"paint");add(b,-16,32,-14,16,33,28,"headliner");
        add(b,-18,18,27,-16,33,30,"paint");add(b,16,18,27,18,33,30,"paint");
        add(b,-16,18,28.5F,16,33,29.5F,"glass");
        // The B-pillars between the front and rear doors, on both sides.
        add(b,-18,10,10,-16,33,10.01F,"paint");add(b,16,10,10,18,33,10.01F,"paint");
        // The rear door on the passenger side does not open: it is part of the body.
        add(b,16,10,10,18,20,27,"paint");add(b,16.5F,20,11,17.5F,32,26,"glass");add(b,16,20,26,18,33,27,"paint");add(b,16,32,10,18,33,26,"paint");
        add(b,15.5F,11,11,16,20,26,"vinyl");
        // The boot: its sides, its floor and the back panel with the lamps; the lid lifts (BOOT_LID).
        add(b,-18,10,30,-16,20,39,"paint");add(b,16,10,30,18,20,39,"paint");
        add(b,-16,10,37,16,20,39,"paint");add(b,-18,8,38,18,12,40,"chrome");
        add(b,-16,10,30,16,11,38,"carpet");add(b,-16,10,29,16,19,30,"paint");
        // Wheels: tyres proud of the body, hubs on their faces.
        for(int side:new int[]{-1,1})for(int z:new int[]{-28,26}){
            float x=side<0?-19:15;
            add(b,x,0,z-5,x+4,10,z+5,"tyre");
            add(b,side<0?x-.1F:x+4,3,z-2,side<0?x:x+4.1F,7,z+2,"hub");
        }
        // The dashboard, its gauges and the steering column.
        add(b,-16,16,-16,16,22,-12,"dash");
        add(b,-14,19,-12.1F,-4,22,-12,"gauges");
        add(b,-10,17,-12,-8,19,-9,"dash");
        // The two front seats.
        for(int x:new int[]{-16,2}){
            add(b,x,10,-4,x+14,14,8,"vinyl");
            add(b,x,14,6,x+14,29,9,"vinyl");
            add(b,x+3,29,6,x+11,32,9,"vinyl");
        }
        // The rear seatback, behind the bench, and the parcel shelf.
        add(b,-16,15,25,16,30,28,"vinyl");add(b,-16,30,26,16,31,29,"carpet");
        // The interior mirror at the head of the windscreen.
        add(b,-3,29,-14,3,31,-13,"tyre");
        return List.copyOf(b);
    }
    /** The rear bench's cushion: whole; pushed up in the middle against the belt; split along the seam, the foam showing. */
    private static List<Box> bench(int stage){
        var b=new ArrayList<Box>();
        add(b,-16,10,13,16,12,25,"vinyl");
        if(stage==0)add(b,-16,12,14,16,15,25,"vinyl");
        else if(stage==1){add(b,-16,12,14,-8,15,25,"vinyl");add(b,8,12,14,16,15,25,"vinyl");add(b,-8,12,14,8,17,25,"vinyl");add(b,-5,17,15,5,18,24,"vinyl");}
        else{
            add(b,-16,12,14,-7,15,25,"vinyl");add(b,4,12,14,16,15,25,"vinyl");
            // The vinyl turned back at the seam and the foam it held.
            add(b,-7,12,14,-5,16,25,"foam");add(b,2,12,14,4,16,25,"foam");
            add(b,-8,15,14,-6,16.5F,25,"vinyl");add(b,3,15,14,5,16.5F,25,"vinyl");
        }
        return List.copyOf(b);
    }
    private static List<Box> bootLid(){var b=new ArrayList<Box>();add(b,-16,19,30,16,20,37,"paint");return List.copyOf(b);}
    /** The steering wheel, at the driver's chest. */
    private static List<Box> wheel(){
        var b=new ArrayList<Box>();
        add(b,-13,19,-10,-5,20,-9,"tyre");add(b,-13,25,-10,-5,26,-9,"tyre");
        add(b,-13,20,-10,-12,25,-9,"tyre");add(b,-6,20,-10,-5,25,-9,"tyre");
        add(b,-10,21,-11,-8,24,-9.5F,"chrome");
        return List.copyOf(b);
    }
    /** A door between {@code z0} and {@code z1} on the side {@code side} (-1 west, 1 east), hinged at its front edge. */
    private static Door door(int side,float z0,float z1){
        var b=new ArrayList<Box>();float x0=side<0?-18:16,x1=x0+2;
        add(b,x0,10,z0,x1,20,z1,"paint");
        add(b,x0,20,z0,x1,33,z0+1,"paint");add(b,x0,20,z1-1,x1,33,z1,"paint");add(b,x0,32,z0,x1,33,z1,"paint");
        add(b,x0+.5F,20,z0+1,x1-.5F,32,z1-1,"glass");
        // The trim inside and the handle outside.
        float in=side<0?x1:x0-.5F;add(b,in,11,z0+1,in+.5F,20,z1-1,"vinyl");
        float out=side<0?x0-.4F:x1;add(b,out,16,z1-6,out+.4F,17,z1-3,"chrome");
        return new Door(List.copyOf(b),side<0?x0+1:x1-1,z0,side);
    }
    /** Everything solid that can stand in the cells for a set of open doors, glass included or not. */
    public static List<Box> solid(int mask,boolean glass,int bench){
        var out=new ArrayList<Box>();
        for(var b:SHELL)if(glass||!b.material.equals("glass"))out.add(b);
        for(var b:bench==0?BENCH_WHOLE:bench==1?BENCH_PUSHED:BENCH_SPLIT)out.add(b);
        for(int i=0;i<DOORS.size();i++)for(var b:(mask&(1<<i))!=0?DOORS.get(i).open():DOORS.get(i).boxes())if(glass||!b.material.equals("glass"))out.add(b);
        return out;
    }
}
