package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.phys.AABB;

/** One incidental reading per physical room. Authored story documents are never removed. */
public final class RoomNotes {
    public static final String ID = "room_notes_0121";
    private RoomNotes() {}

    public record Scope(String id, AABB bounds, List<AABB> excluded) {
        public boolean contains(BlockPos pos) {
            var center = pos.getCenter();
            return bounds.contains(center) && excluded.stream().noneMatch(box -> box.contains(center));
        }
    }

    private static AABB box(BlockPos base, int x0, int y0, int z0, int x1, int y1, int z1) {
        return new AABB(base.getX()+x0, base.getY()+y0, base.getZ()+z0,
                base.getX()+x1+1, base.getY()+y1+1, base.getZ()+z1+1);
    }

    /** A side room is independent of its corridor; two rooms separated by a wall do not share a quota. */
    public static List<Scope> scopes(ServerLevel level, BlockPos base, LabyrinthPlace place) {
        if (!ordinary(place)) return List.of();
        var out = new ArrayList<Scope>();
        int index = 0;
        for (var f : LabyrinthDomestic.fragments(place))
            out.add(new Scope("fragment_"+index++, box(base,f.x0(),0,f.z0(),f.x1(),3,f.z1()),List.of()));
        if (LabyrinthMaze.isMaze(place)) {
            for (var f : LabyrinthDomestic.mazeFragments(place,LabyrinthMaze.layout(level.getServer(),place))) {
                var a = base.offset(f.back()).relative(f.into().getCounterClockWise(),2).relative(f.into());
                var b = base.offset(f.back()).relative(f.into().getClockWise(),2).relative(f.into().getOpposite(),2);
                out.add(new Scope("nook_"+index++, new AABB(Math.min(a.getX(),b.getX()),a.getY(),Math.min(a.getZ(),b.getZ()),
                        Math.max(a.getX(),b.getX())+1,a.getY()+4,Math.max(a.getZ(),b.getZ())+1),List.of()));
            }
        }
        var r = place.room();
        var excluded = out.stream().map(Scope::bounds).toList();
        out.add(new Scope("room",box(base,r.minX(),0,r.minZ(),r.maxX(),4,r.maxZ()),excluded));
        return List.copyOf(out);
    }

    private static boolean ordinary(LabyrinthPlace place) {
        // Multiple source/account documents in authored vignettes, archives and the staircase are intentional.
        return place==LabyrinthPlace.JUNCTION || LabyrinthHalls.isHall(place) || LabyrinthMaze.isMaze(place);
    }

    public static List<Scope> manorScopes(BlockPos origin) {
        return HouseLayout.ROOMS.stream().map(room -> {
            var b=room.box();
            return new Scope(room.name(),box(origin,b.x0(),b.y0(),b.z0(),b.x1(),b.y1(),b.z1()),List.of());
        }).toList();
    }

    private static Scope scopeAt(ServerLevel level, BlockPos pos) {
        BlockPos origin=HouseSavedData.get(level.getServer()).houseOrigin();
        if(origin==null || !level.dimension().equals(HouseDimensions.INTERIOR)) return null;
        for(var scope:manorScopes(origin)) if(scope.contains(pos)) return scope;
        for(var place:LabyrinthPlace.values()) {
            if(!ordinary(place)) continue;
            var base=LabyrinthPlaces.base(origin,place);
            if(base==null || !place.room().isInside(pos.subtract(base))) continue;
            for(var scope:scopes(level,base,place)) if(scope.contains(pos)) return scope;
        }
        return null;
    }

    public static boolean canAdd(ServerLevel level, BlockPos pos) {
        var room=scopeAt(level,pos);
        if(room==null || !loaded(level,room.bounds(),false)) return false;
        for(var at:positions(room.bounds())) if(room.contains(at) && reading(level,at)) return false;
        return true;
    }

    private static boolean reading(ServerLevel level, BlockPos pos) {
        var state=level.getBlockState(pos);
        return state.getBlock() instanceof NoteSurfaceBlock
                || state.is(Blocks.LECTERN) && level.getBlockEntity(pos) instanceof LecternBlockEntity lectern && lectern.hasBook();
    }

    private static Iterable<BlockPos> positions(AABB bounds) {
        return BlockPos.betweenClosed(BlockPos.containing(bounds.minX,bounds.minY,bounds.minZ),
                BlockPos.containing(bounds.maxX-1,bounds.maxY-1,bounds.maxZ-1));
    }

    /** Keep the authored book, or the first existing loose sheet. Never move, replace or refill a reading. */
    public static int prune(ServerLevel level, Scope room) {
        if(!loaded(level,room.bounds(),false)) return 0;
        var papers=new ArrayList<BlockPos>();
        boolean authoredBook=false;
        for(var at:positions(room.bounds())) {
            if(!room.contains(at)) continue;
            var state=level.getBlockState(at);
            if(state.getBlock() instanceof NoteSurfaceBlock) papers.add(at.immutable());
            else if(state.is(Blocks.LECTERN) && level.getBlockEntity(at) instanceof LecternBlockEntity lectern && lectern.hasBook()) authoredBook=true;
        }
        int removed=0;
        for(int i=authoredBook?0:1;i<papers.size();i++) {
            level.setBlock(papers.get(i),Blocks.AIR.defaultBlockState(),HouseCanvas.BUILD_FLAGS);
            removed++;
        }
        return removed;
    }

    private static boolean loaded(ServerLevel level,AABB bounds,boolean entities) {
        for(int x=((int)Math.floor(bounds.minX))>>4;x<=((int)Math.ceil(bounds.maxX)-1)>>4;x++)
            for(int z=((int)Math.floor(bounds.minZ))>>4;z<=((int)Math.ceil(bounds.maxZ)-1)>>4;z++)
                if(!level.isLoaded(new BlockPos(x<<4,(int)bounds.minY,z<<4))
                        || entities && !level.areEntitiesLoaded(ChunkPos.asLong(x,z))) return false;
        return true;
    }

    private static boolean repair(ServerLevel level,Scope room) {
        if(!loaded(level,room.bounds(),true)
                || level.players().stream().anyMatch(p->room.bounds().inflate(2).contains(p.position()))
                || !level.getEntitiesOfClass(LivingEntity.class,room.bounds(),LivingEntity::isAlive).isEmpty()) return false;
        prune(level,room);
        return true;
    }

    /** Old rooms are reduced once, only while loaded and vacant. No chunk generation or paper restocking. */
    public static void tick(MinecraftServer server) {
        // Isolated native fixtures exercise prune directly; their shared saved House is not a live room.
        if(server instanceof net.minecraft.gametest.framework.GameTestServer
                || server.getTickCount()%20!=0 || LabyrinthBuilder.isCarving()) return;
        var origin=HouseSavedData.get(server).houseOrigin();
        var level=server.getLevel(HouseDimensions.INTERIOR);
        var data=LabyrinthData.get(server);
        if(origin==null || level==null || !origin.equals(data.builtOrigin())) return;
        var done=data.state(ID);
        String prefix=origin.asLong()+":";
        boolean changed=false;
        for(var place:LabyrinthPlace.values()) {
            if(!ordinary(place) || data.door(place.entryDoorId())==null || done.getBoolean(prefix+place.id())) continue;
            var base=LabyrinthPlaces.base(origin,place);
            if(base==null) continue;
            boolean complete=true;
            for(var scope:scopes(level,base,place)) {
                String key=prefix+place.id()+":"+scope.id();
                if(done.getBoolean(key)) continue;
                if(repair(level,scope)) { done.putBoolean(key,true);changed=true; }
                else complete=false;
            }
            if(complete) { done.putBoolean(prefix+place.id(),true);changed=true; }
        }
        for(var scope:manorScopes(origin)) {
            String key=prefix+"manor:"+scope.id();
            if(!done.getBoolean(key) && repair(level,scope)) { done.putBoolean(key,true);changed=true; }
        }
        if(changed) data.setState(ID,done);
    }
}
