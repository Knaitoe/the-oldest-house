package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.phys.Vec3;

/** Native model pixels. The car faces north; its driver's door is on the west.
 * Materials repeat at Minecraft's sixteen-pixels-per-block density. */
public final class BackseatCarMesh {
    public record Box(float x0,float y0,float z0,float x1,float y1,float z1,String material) {}
    public static final Vec3 DRIVER = new Vec3(-10/16D,9/16D,-3/16D);
    public static final Vec3 MIRROR = new Vec3(0,32/16D,-19/16D);
    public static final List<Box> SHELL=shell(),DRIVER_DOOR=door(),STEERING=wheel();
    private BackseatCarMesh() {}
    private static void add(List<Box> out,float x,float y,float z,float X,float Y,float Z,String mat){out.add(new Box(x,y,z,X,Y,Z,mat));}
    private static List<Box> shell(){
        var b=new ArrayList<Box>();
        add(b,-19,7,-43,19,9,41,"rubber");
        add(b,-19,9,-43,19,11,-20,"cream");
        add(b,-19,9,23,19,13,41,"vinyl");
        // Hood, front face, rear tailgate and sill; the cabin stays hollow.
        add(b,-19,12,-44,19,19,-22,"cream");
        add(b,-19,19,-44,19,20,-21,"cream");
        add(b,-20,10,-45,20,13,-43,"chrome");
        add(b,-20,10,41,20,13,43,"chrome");
        add(b,-18,13,38,18,23,41,"cream");
        add(b,-17,24,39,17,34,40,"glass");
        add(b,-18,23,38,18,24,41,"chrome");
        add(b,-18,34,38,18,36,41,"cream");
        for(int side:new int[]{-1,1}){
            float x=side<0?-20:18;
            add(b,x,9,-38,x+2,13,37,"cream");
            add(b,x,13,12,x+2,23,38,"cream");
            float trim=side<0?x-.125F:x+2.0625F;
            add(b,trim,16,12,trim+.0625F,20,37,"wood");
            // Narrow native pillars and a rubber seal around each window.
            add(b,x,23,10,x+2,36,12,"cream");
            add(b,x,23,36,x+2,36,39,"cream");
            add(b,x+.5F,24,12,x+1.5F,34,36,"glass");
            add(b,x,34,11,x+2,36,38,"cream");
            add(b,x,23,-22,x+2,36,-20,"cream");
            // Wheel wells have separate thickness, with blocky tires/hubs.
            for(int z:new int[]{-32,29}){
                add(b,x-1,2,z-6,x+3,12,z+6,"rubber");
                add(b,side<0?x-1.1F:x+2.9F,5,z-3,side<0?x-.9F:x+3.1F,9,z+3,"chrome");
            }
        }
        // Passenger door. Its matching driver leaf is independently hinged.
        for(var q:door())add(b,-q.x1,q.y0,q.z0,-q.x0,q.y1,q.z1,q.material);
        add(b,-19,36,-23,19,38,40,"cream");
        add(b,-17,35,-20,17,36,37,"headliner");
        add(b,-17,25,-22,17,35,-21.5F,"glass");
        add(b,-18,23,-22,18,25,-19,"rubber");
        add(b,-18,21,-21,18,24,-16,"rubber");
        add(b,-16,23,-16.5F,-4,27,-15.8F,"gauges");
        add(b,1,22,-16.6F,11,25,-16,"wood");
        add(b,-2,23,-16.7F,-1,24,-15.7F,"chrome");
        // Two front seats and an undivided rear bench, mounted to the floor.
        for(int x:new int[]{-16,3}){
            add(b,x,9,-7,x+13,12,9,"rubber");
            add(b,x,12,-8,x+13,16,9,"vinyl");
            add(b,x,16,6,x+13,29,10,"vinyl");
            add(b,x+3,29,6,x+10,32,10,"vinyl");
        }
        add(b,-17,12,18,17,16,32,"vinyl");
        add(b,-17,16,29,17,30,33,"vinyl");
        add(b,-.5F,32,-21, .5F,35,-20,"rubber");
        add(b,-4,30.5F,-20,4,33.5F,-18.5F,"rubber");
        add(b,-3.5F,31,-18.49F,3.5F,33,-18.4F,"mirror");
        add(b,-2,34.6F,-3,2,35.2F,3,"lamp");
        // Lamps are flat painted diffusers; their warmth comes from lighting.
        add(b,-17,14,-44.3F,-10,18,-44.1F,"lamp");
        add(b,10,14,-44.3F,17,18,-44.1F,"lamp");
        add(b,-8,14,-44.4F,8,17,-44.2F,"rubber");
        add(b,-17,15,41.0625F,-13,21,41.125F,"tail");
        add(b,13,15,41.0625F,17,21,41.125F,"tail");
        return List.copyOf(b);
    }
    private static List<Box> door(){
        var b=new ArrayList<Box>();
        add(b,-20,13,-20,-18,23,10,"cream");
        add(b,-20.1F,16,-20,-20,20,10,"wood");
        add(b,-18,14,-19,-17.5F,23,9,"vinyl");
        add(b,-20,23,-20,-18,25,10,"chrome");
        add(b,-20,25,-20,-18,35,-18,"cream");
        add(b,-20,25,8,-18,35,10,"cream");
        add(b,-20,34,-19,-18,36,10,"cream");
        add(b,-19.5F,25,-18,-19,34,8,"glass");
        add(b,-20.6F,21,4,-20,22,8,"chrome");
        add(b,-17.5F,20,3,-17,21,7,"chrome");
        return List.copyOf(b);
    }
    private static List<Box> wheel(){
        var b=new ArrayList<Box>();
        add(b,-14,24,-14,-6,25,-13,"rubber");
        add(b,-14,30,-14,-6,31,-13,"rubber");
        add(b,-14,25,-14,-13,30,-13,"rubber");
        add(b,-7,25,-14,-6,30,-13,"rubber");
        add(b,-11,26,-16,-9,29,-12.9F,"chrome");
        return List.copyOf(b);
    }
}
