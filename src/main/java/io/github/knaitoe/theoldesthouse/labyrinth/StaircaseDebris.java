package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** One real piece of masonry per nearby group, with a sealed wall left behind it. */
public final class StaircaseDebris {
    public static final double NEARBY=12;
    public static final String TAG="HouseStaircaseDebris";
    private static final int FLAGS=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private StaircaseDebris(){}

    public static int fallForPlayers(ServerLevel level,BlockPos origin,List<ServerPlayer> readers){
        var remaining=new ArrayList<>(readers.stream().filter(p->p.serverLevel()==level&&p.isAlive()&&!p.isSpectator()&&FinaleArchitecture.contains(origin,p.blockPosition())).sorted(Comparator.comparing(p->p.getUUID().toString())).toList());
        int fallen=0;
        while(!remaining.isEmpty()){
            var group=new ArrayList<ServerPlayer>();group.add(remaining.removeFirst());
            for(int i=0;i<group.size();i++)for(var it=remaining.iterator();it.hasNext();){var peer=it.next();if(group.get(i).distanceToSqr(peer)<=NEARBY*NEARBY){group.add(peer);it.remove();}}
            // Try another member if the first one's nearby wall was edited or unloaded.
            for(var p:group)if(dislodge(level,origin,p.position())){fallen++;break;}
        }
        return fallen;
    }

    private record Piece(BlockPos source,BlockPos backing){}
    private static boolean masonry(BlockState s){return s.is(Blocks.DEEPSLATE_TILES)||s.is(Blocks.CHISELED_DEEPSLATE);}
    private static boolean loaded(ServerLevel level,BlockPos p){return level.hasChunkAt(p)&&level.areEntitiesLoaded(new ChunkPos(p).toLong());}
    private static boolean dislodge(ServerLevel level,BlockPos origin,Vec3 viewer){
        var base=FinaleArchitecture.base(origin);int radius=FinaleArchitecture.SHAFT_RADIUS;
        int px=(int)Math.floor(viewer.x),py=(int)Math.floor(viewer.y),pz=(int)Math.floor(viewer.z);
        var pieces=new ArrayList<Piece>();
        if(Math.abs(px-base.getX())<=radius&&Math.abs(pz-base.getZ())<=radius){
            int x=Math.max(base.getX()-radius+2,Math.min(base.getX()+radius-2,px));
            int z=Math.max(base.getZ()-radius+2,Math.min(base.getZ()+radius-2,pz));
            for(int up=3;up<=7;up++)for(int side=-2;side<=2;side++){
                pieces.add(new Piece(new BlockPos(base.getX()-radius,py+up,z+side),new BlockPos(base.getX()-radius-1,py+up,z+side)));
                pieces.add(new Piece(new BlockPos(base.getX()+radius,py+up,z+side),new BlockPos(base.getX()+radius+1,py+up,z+side)));
                pieces.add(new Piece(new BlockPos(x+side,py+up,base.getZ()-radius),new BlockPos(x+side,py+up,base.getZ()-radius-1)));
                pieces.add(new Piece(new BlockPos(x+side,py+up,base.getZ()+radius),new BlockPos(x+side,py+up,base.getZ()+radius+1)));
            }
        }else if(px>=base.getX()-15&&px<=base.getX()+15&&pz>=base.getZ()+31&&pz<=base.getZ()+67&&py>=FinaleArchitecture.ARENA&&py<FinaleArchitecture.ARENA+11){
            for(int dx=-3;dx<=3;dx++)for(int dz=-3;dz<=3;dz++)if(dx*dx+dz*dz>=4){var at=new BlockPos(px+dx,FinaleArchitecture.ARENA+13,pz+dz);pieces.add(new Piece(at,at.above()));}
        }
        pieces.sort(Comparator.comparingDouble(piece->piece.source().distToCenterSqr(viewer)));
        for(var piece:pieces){
            var at=piece.source();var back=piece.backing();
            if(!loaded(level,at)||!loaded(level,back)||level.getBlockEntity(at)!=null||level.getBlockEntity(back)!=null)continue;
            var state=level.getBlockState(at);var behind=level.getBlockState(back);
            if(!masonry(state)||!behind.isAir()&&!behind.isCollisionShapeFullBlock(level,back))continue;
            // Removing the inner face never opens the black shell to the world.
            if(behind.isAir()&&!level.setBlock(back,state,FLAGS))continue;
            FallingBlockEntity debris=FallingBlockEntity.fall(level,at,state);
            // A wall fragment must clear the face below it before gravity takes
            // over; otherwise vanilla immediately lands it on the next wall block.
            Vec3 inward=new Vec3(at.getX()-back.getX(),0,at.getZ()-back.getZ());
            if(inward.lengthSqr()>0){debris.setPos(debris.getX()+inward.x*1.01,debris.getY(),debris.getZ()+inward.z*1.01);debris.setDeltaMovement(inward.scale(.06));}
            CompoundTag tag=new CompoundTag();debris.saveWithoutId(tag);tag.putBoolean("DropItem",false);tag.putBoolean("CancelDrop",true);tag.putBoolean("HurtEntities",false);debris.load(tag);debris.addTag(TAG);
            level.levelEvent(2001,at,Block.getId(state));
            return true;
        }
        return false;
    }
}
