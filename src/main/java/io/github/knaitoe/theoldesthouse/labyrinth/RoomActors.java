package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.phys.AABB;

/**
 * 0.4.263 (the owner, of the window cleaner's room: "the glass panels aren't appropriately faced. NPCs are jumbled together.").
 * <p>
 * A room that kept its one actor by a saved id made a new one whenever that id did not answer; and it does not answer while the
 * actor's chunk has its blocks but not yet its entities, which is exactly when a reader walks back in. The old actor then loaded
 * beside the new one, and the reader found two cleaners in one cradle, each with its own line over its head. Here a room waits until
 * every chunk under it has its entities, keeps the saved actor or else the first of its kind standing there, and lets any other go.
 * <p>
 * And the rooms' windows: panes set by hand in their plain state stand as bare posts, not glass across the opening, because nothing
 * told them their neighbours. {@link #settlePanes} gives each the joins it would have had if it had been put there by hand.
 */
public final class RoomActors {
    private RoomActors(){}
    private static final int FLAGS=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;

    /** The room's own box in the world, a block wider all round. */
    public static AABB box(BlockPos b,LabyrinthPlace place){
        var r=place.room();return new AABB(b.getX()+r.minX()-1,b.getY()+r.minY()-1,b.getZ()+r.minZ()-1,b.getX()+r.maxX()+2,b.getY()+r.maxY()+2,b.getZ()+r.maxZ()+2);
    }
    /** Whether every chunk under the box has its entities: until then an actor saved there may simply not have arrived yet. */
    public static boolean ready(ServerLevel l,AABB box){
        for(int x=Mth.floor(box.minX)>>4;x<=Mth.floor(box.maxX)>>4;x++)for(int z=Mth.floor(box.minZ)>>4;z<=Mth.floor(box.maxZ)>>4;z++)
            if(!l.areEntitiesLoaded(ChunkPos.asLong(x,z)))return false;
        return true;
    }
    /** Every live actor of this kind in the box. */
    public static List<LiteraryActor> all(ServerLevel l,AABB box,Predicate<LiteraryActor> kind){return l.getEntitiesOfClass(LiteraryActor.class,box,a->a.isAlive()&&kind.test(a));}
    /**
     * The room's one actor of this kind: the saved one if it is here, else the first of its kind standing in the box (saved now under
     * {@code key} in {@code state}, which the caller saves); every other of its kind in the box is let go. Null when there is none.
     */
    @Nullable public static LiteraryActor one(ServerLevel l,AABB box,CompoundTag state,String key,Predicate<LiteraryActor> kind){
        LiteraryActor kept=null;
        if(state.hasUUID(key)&&l.getEntity(state.getUUID(key)) instanceof LiteraryActor a&&a.isAlive()&&kind.test(a))kept=a;
        for(var a:all(l,box,kind)){
            if(kept==null){kept=a;state.putUUID(key,a.getUUID());}
            else if(a!=kept)a.discard();
        }
        return kept;
    }
    /** Lets every actor of this kind in the box go. */
    public static void clear(ServerLevel l,AABB box,Predicate<LiteraryActor> kind){for(var a:all(l,box,kind))a.discard();}
    /** Wipes a spoken line from every actor of this kind in the box. */
    public static void hush(ServerLevel l,AABB box,Predicate<LiteraryActor> kind){for(var a:all(l,box,kind)){a.setCustomName(null);a.setCustomNameVisible(false);}}

    /** Every pane and bar in the box joined to its neighbours the way it would be if it had been placed by hand. */
    public static void settlePanes(ServerLevel l,BlockPos min,BlockPos max){
        var at=new BlockPos.MutableBlockPos();
        for(int x=min.getX();x<=max.getX();x++)for(int y=min.getY();y<=max.getY();y++)for(int z=min.getZ();z<=max.getZ();z++){
            at.set(x,y,z);var s=l.getBlockState(at);if(!(s.getBlock() instanceof IronBarsBlock))continue;
            var settled=s;var pos=at.immutable();
            for(var d:Direction.Plane.HORIZONTAL){var n=pos.relative(d);settled=settled.updateShape(d,l.getBlockState(n),l,pos,n);}
            if(settled!=s)l.setBlock(pos,settled,FLAGS);
        }
    }
    /** {@link #settlePanes} over a place's whole room. */
    public static void settlePanes(ServerLevel l,BlockPos b,LabyrinthPlace place){
        var r=place.room();settlePanes(l,b.offset(r.minX(),r.minY(),r.minZ()),b.offset(r.maxX(),r.maxY(),r.maxZ()));
    }
}
