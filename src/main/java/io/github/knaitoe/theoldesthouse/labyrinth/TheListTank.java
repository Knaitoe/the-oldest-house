package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * 0.4.717, The list part 4d-2: the stock tank in the pasture (TheListSite.TANK_AT), as the slasher uses it. A straight cell of its wall
 * (one water cell beside it) is where one of the five held is forced over the rim with the head under the water; what happens then is
 * TheListHunt's (tank, dunked, tanking, surfaced, drowned).
 */
public final class TheListTank{
    private TheListTank(){}
    /** How near the wall the one held must be; a full breath under the water, in ticks; how long the head is up before they tear loose. */
    public static final double REACH=2.6;
    public static final int BREATH=140,SURFACE=24;
    /** How many times the head goes under before it stays under. */
    public static final int LAST=3;

    /** The way from a straight cell of the tank's wall (in the town's coordinates) into its water; null for anything else. */
    public static @Nullable Direction into(BlockPos rel){
        if(rel.getY()!=0)return null;
        int dx=rel.getX()-TheListSite.TANK_AT.getX(),dz=rel.getZ()-TheListSite.TANK_AT.getZ();
        if(TheListSite.inTank(dx,dz)||Math.abs(dx)>4||Math.abs(dz)>4)return null;
        Direction in=null;int n=0;
        for(var f:Direction.Plane.HORIZONTAL)if(TheListSite.inTank(dx+f.getStepX(),dz+f.getStepZ())){in=f;n++;}
        return n==1?in:null;
    }
    static Vec3 middle(BlockPos b,BlockPos wall){return Vec3.atBottomCenterOf(b.offset(wall));}
    /** Where one forced over the rim is held: the rim is higher than their hips, so they lie over it, the hips on its top and the feet
     * off the ground on the dry side. */
    public static Vec3 stand(BlockPos b,BlockPos wall,Direction in){return middle(b,wall).add(in.getStepX()*.42,.3,in.getStepZ()*.42);}
    /** Where the head is held, under the water past the rim. */
    public static Vec3 head(BlockPos b,BlockPos wall,Direction in){return middle(b,wall).add(in.getStepX()*1.2,.6,in.getStepZ()*1.2);}
    /** The water over the head, where it churns. */
    public static Vec3 surface(BlockPos b,BlockPos wall,Direction in){return middle(b,wall).add(in.getStepX()*1.1,.9,in.getStepZ()*1.1);}
    public static float yaw(Direction in){return in.toYRot();}

    /** A hand or a blow on the tank's wall: the hunt's first (TheListHunt.tank); false for a look. */
    public static boolean hand(ServerPlayer p,BlockPos b,BlockPos rel){
        var in=into(rel);if(in==null)return false;
        return TheListHunt.tank(p,b,rel,in);
    }
}
