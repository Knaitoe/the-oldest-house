package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.212, the line (docs/LINE_0_4_212.md): a short dark hall, a doorframe, and past it a space with no walls a reader can find in
 * the dark: forty-nine wide, eighty deep, six high, with thick columns standing about in it. A line is tied to the doorframe and runs
 * off into the black at hand height, winding between the columns, to an explorer's pack at the far end.
 *
 * <p>Relative to the base: the hall x -1 to 1, z 0 to -6; the frame at z -7; the space x -24 to 24, z -8 to -88, y 0 to 5; the pack
 * at the end of the line, the way on in the far wall behind it.
 */
public final class LineSite {
    private LineSite(){}
    public static final int HALL_END=-6,FRAME_Z=-7,NEAR_Z=-8,FAR_Z=-88,SIDE=24,HIGH=6;
    /** The tag tied to the line where it is knotted round the doorframe (the source). */
    public static final BlockPos TAG=new BlockPos(1,1,FRAME_Z);
    /** The pack at the end of the line, and the tape recorder beside it (the account). */
    public static final BlockPos PACK=new BlockPos(-3,0,-85),RECORDER=new BlockPos(-2,0,-85);
    public static final BlockPos ONWARD=new BlockPos(-3,0,FAR_Z-1);
    /** Hand height. */
    public static final double HAND=1.1;
    /** The line's course, at hand height: from the knot to the pack, winding between the columns. */
    public static final List<Vec3> PATH=List.of(
        new Vec3(1.5,HAND,-7.5),new Vec3(2.5,HAND,-14.5),new Vec3(-5.5,HAND,-22.5),new Vec3(-10.5,HAND,-34.5),new Vec3(-2.5,HAND,-44.5),
        new Vec3(8.5,HAND,-52.5),new Vec3(12.5,HAND,-64.5),new Vec3(4.5,HAND,-74.5),new Vec3(-2.5,HAND,-82.5),new Vec3(-2.5,HAND,-84.2));
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;

    static net.minecraft.world.level.block.state.BlockState ash(){return DarkHallRegistry.material("ash").defaultBlockState();}
    static net.minecraft.world.level.block.state.BlockState floor(){return DarkHallRegistry.material("floor").defaultBlockState();}
    static net.minecraft.world.level.block.state.BlockState ceiling(){return DarkHallRegistry.material("ceiling").defaultBlockState();}

    /** The line's whole length. */
    public static double length(){double s=0;for(int i=1;i<PATH.size();i++)s+=flat(PATH.get(i-1),PATH.get(i));return s;}
    static double flat(Vec3 a,Vec3 c){return Math.hypot(a.x-c.x,a.z-c.z);}
    /** The point this far along the line. */
    public static Vec3 at(double s){
        s=Math.max(0,s);for(int i=1;i<PATH.size();i++){var a=PATH.get(i-1);var c=PATH.get(i);double d=flat(a,c);if(s<=d)return a.add(c.subtract(a).scale(d<1e-6?0:s/d));s-=d;}
        return PATH.get(PATH.size()-1);
    }
    /** How far along the line the nearest point to this place is, and how far off the line it stands: {along, off}. */
    public static double[] nearest(Vec3 r){
        double best=1e9,along=0,run=0;
        for(int i=1;i<PATH.size();i++){var a=PATH.get(i-1);var c=PATH.get(i);double dx=c.x-a.x,dz=c.z-a.z,len2=dx*dx+dz*dz,len=Math.sqrt(len2);
            double u=len2<1e-9?0:Math.max(0,Math.min(1,((r.x-a.x)*dx+(r.z-a.z)*dz)/len2));
            double px=a.x+dx*u,pz=a.z+dz*u,d=Math.hypot(r.x-px,r.z-pz);
            if(d<best){best=d;along=run+len*u;}run+=len;}
        return new double[]{along,best};
    }
    public static boolean inSpace(Vec3 r){return r.x>=-SIDE&&r.x<SIDE+1&&r.z>=FAR_Z&&r.z<NEAR_Z+1&&r.y>=-.5&&r.y<HIGH;}
    public static boolean inside(Vec3 r){return inSpace(r)||r.x>=-1&&r.x<2&&r.z>=FRAME_Z&&r.z<1;}
    /** A column stands here: two by two, fixed by the place, never within three blocks of the line. */
    public static boolean column(int x,int z){
        if(x<-SIDE+2||x>SIDE-2||z>NEAR_Z-3||z<FAR_Z+2)return false;
        int cx=Math.floorDiv(x,7),cz=Math.floorDiv(z,7);long h=(cx*73856093L)^(cz*19349663L)^0x5DEECE66DL;h^=(h>>>17);h*=0x9E3779B97F4A7C15L;h^=(h>>>29);
        if(Math.floorMod(h,5)>1)return false;
        int ox=cx*7+(int)Math.floorMod(h>>>8,4)+1,oz=cz*7+(int)Math.floorMod(h>>>16,4)+1;
        if(x<ox||x>ox+1||z<oz||z>oz+1)return false;
        return nearest(new Vec3(x+.5,HAND,z+.5))[1]>=3.2;
    }

    public static void build(ServerLevel l,BlockPos b){
        BuildBlocks.box(l,b.offset(-SIDE-1,-1,FAR_Z-1),b.offset(SIDE+1,HIGH,0),ash(),F);
        for(int z=FRAME_Z;z<=0;z++)for(int x=-1;x<=1;x++){BuildBlocks.set(l,b.offset(x,-1,z),floor(),F);BuildBlocks.set(l,b.offset(x,3,z),ceiling(),F);for(int y=0;y<3;y++)BuildBlocks.set(l,b.offset(x,y,z),Blocks.AIR.defaultBlockState(),F);}
        for(int z=FAR_Z;z<=NEAR_Z;z++)for(int x=-SIDE;x<=SIDE;x++){
            BuildBlocks.set(l,b.offset(x,-1,z),floor(),F);BuildBlocks.set(l,b.offset(x,HIGH,z),ceiling(),F);
            boolean c=column(x,z);for(int y=0;y<HIGH;y++)BuildBlocks.set(l,b.offset(x,y,z),c?ash():Blocks.AIR.defaultBlockState(),F);
        }
        BuildBlocks.set(l,b.offset(TAG),HorrorBlock.of(HorrorBlock.Kind.TAG,Direction.WEST),F);
        BuildBlocks.set(l,b.offset(PACK),HorrorBlock.of(HorrorBlock.Kind.PACK,Direction.SOUTH),F);
        BuildBlocks.set(l,b.offset(RECORDER),HorrorBlock.of(HorrorBlock.Kind.RECORDER,Direction.SOUTH),F);
    }
}
