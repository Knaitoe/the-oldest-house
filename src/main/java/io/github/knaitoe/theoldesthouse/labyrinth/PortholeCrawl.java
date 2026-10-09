package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;

/** Common collision rule; client anchors cannot authorize any server action. */
public final class PortholeCrawl {
    private static final Map<Player,BlockPos> LOCAL=new WeakHashMap<>();
    private PortholeCrawl(){}
    public static void local(Player p,BlockPos base){if(base==null)LOCAL.remove(p);else LOCAL.put(p,base.immutable());}
    public static boolean leavingSill(Player p){
        if(p.getForcedPose()!=Pose.SWIMMING||p.getPose()!=Pose.SWIMMING)return false;
        BlockPos base;
        if(p instanceof ServerPlayer s){
            if(!LiteraryVignettes.participant(s)||!LiteraryVignettes.inside(s,LabyrinthPlace.ELK_CARCASSES))return false;
            var origin=HouseSavedData.get(s.server).houseOrigin();if(origin==null)return false;base=LabyrinthPlaces.base(origin,LabyrinthPlace.ELK_CARCASSES);
        }else base=LOCAL.get(p);
        if(base==null)return false;var r=p.position().subtract(base.getX(),base.getY(),base.getZ());if(!ElkCarcassMap.nearPorthole(r))return false;
        int x=r.x<0?-9:9;
        for(int z:new int[]{-1,-3,-5,-10,-16})if(Math.abs(r.z-(z+.5))<.85&&p.level().getBlockState(base.offset(x,1,z)).isAir())return true;
        return false;
    }
}
