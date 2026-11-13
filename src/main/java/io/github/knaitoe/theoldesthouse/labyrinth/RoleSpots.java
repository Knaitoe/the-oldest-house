package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.558 (owner, 13 November: "Did you ensure players are placed in their appropriate spots? Player 2 in Goatman spawns at a cousin's
 * location, replacing said cousin for that vignette and custom instructions for their role? Etc"): where a reader who takes a part in a
 * story begins. They stand on that part's own spot when it is clear and has ground under it; else on the nearest of the given ways up
 * to it; else beside it. A spot with nowhere to stand leaves them where they came in.
 */
public final class RoleSpots {
    private RoleSpots(){}

    /**
     * Puts the reader at the first place, relative to {@code base}, that has room and ground for them, facing {@code yaw}. {@code ways}
     * run from farthest to nearest, so the nearest is tried first. Returns whether they were moved.
     */
    public static boolean stand(ServerPlayer p,BlockPos base,Vec3 at,float yaw,Vec3... ways){
        var l=p.serverLevel();
        var tries=new ArrayList<Vec3>();tries.add(at);tries.add(new Vec3(at.x,Math.floor(at.y),at.z));
        for(int i=ways.length-1;i>=0;i--)tries.add(ways[i]);
        for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)if(dx!=0||dz!=0)tries.add(new Vec3(at.x+dx,Math.floor(at.y),at.z+dz));
        // The spot is the story's own ground, already built: it is loaded to be looked at, as a reader's own arrival would load it.
        l.getChunkAt(BlockPos.containing(at.add(base.getX(),base.getY(),base.getZ())));
        var size=p.getDimensions(p.getPose());
        for(var rel:tries){
            Vec3 to=rel.add(base.getX(),base.getY(),base.getZ());var box=size.makeBoundingBox(to);
            if(!l.hasChunkAt(BlockPos.containing(to))||!l.noCollision(p,box)||l.noCollision(p,box.move(0,-.3,0)))continue;
            p.stopRiding();
            p.connection.teleport(to.x,to.y,to.z,yaw,0);p.setYHeadRot(yaw);p.setDeltaMovement(Vec3.ZERO);p.fallDistance=0;return true;
        }
        return false;
    }
}
