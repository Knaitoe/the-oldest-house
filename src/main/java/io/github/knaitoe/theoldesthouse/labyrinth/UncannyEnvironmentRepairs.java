package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** One bounded, loaded and unobserved refit. Never executes either construction plan's callbacks. */
public final class UncannyEnvironmentRepairs {
    public static final String STATE="uncanny_environment_0123";
    private static Work work;
    private static final class Work {
        final ServerLevel level;final BlockPos origin,base;final LabyrinthPlace place;
        final BuildBlocks.Plan before,previous,after;final AABB area;final boolean cellarDoorOpen;final int width,depth,total;int cursor;
        Work(ServerLevel level,BlockPos origin,LabyrinthPlace place){
            this.level=level;this.origin=origin.immutable();this.place=place;base=LabyrinthPlaces.base(origin,place);area=IndianLakeRooms.bounds(base,place);
            before=BuildBlocks.record(level,()->UncannyRooms0121.build(level,base,place));
            previous=BuildBlocks.record(level,()->UncannyRooms0122.build(level,base,place));
            after=BuildBlocks.record(level,()->UncannyRooms.build(level,base,place));
            var cellarDoor=level.getBlockState(base.offset(UncannyRooms0122.CELLAR_DOOR));cellarDoorOpen=!(cellarDoor.getBlock() instanceof DoorBlock)||cellarDoor.getValue(DoorBlock.OPEN);
            var r=place.room();width=r.maxX()-r.minX()+1;depth=r.maxZ()-r.minZ()+1;total=width*depth*(r.maxY()-r.minY()+1);
        }
    }
    private UncannyEnvironmentRepairs(){}
    public static void tick(MinecraftServer server){
        if(server instanceof net.minecraft.gametest.framework.GameTestServer||LabyrinthBuilder.isCarving())return;
        var origin=HouseSavedData.get(server).houseOrigin();if(origin==null){work=null;return;}
        if(work!=null){
            if(work.level.getServer()!=server||!work.origin.equals(origin)){work=null;return;}
            step(work.level,origin,work.place);return;
        }
        if(server.getTickCount()%40!=0)return;
        var data=LabyrinthData.get(server);if(!origin.equals(data.builtOrigin()))return;
        for(var place:UncannyRooms.PLACES){
            if(data.door(place.entryDoorId())==null||data.state(STATE).getBoolean(key(origin,place)))continue;
            var l=server.getLevel(NovelRooms.dimension(place));if(l==null)continue;
            var area=IndianLakeRooms.bounds(LabyrinthPlaces.base(origin,place),place);
            if(loaded(l,area)&&vacant(l,area)){step(l,origin,place);break;}
        }
    }
    private static String key(BlockPos origin,LabyrinthPlace p){return origin.asLong()+":"+p.id();}
    private static boolean loaded(ServerLevel l,AABB a){
        for(int x=((int)Math.floor(a.minX))>>4;x<=((int)Math.ceil(a.maxX)-1)>>4;x++)
            for(int z=((int)Math.floor(a.minZ))>>4;z<=((int)Math.ceil(a.maxZ)-1)>>4;z++)
                if(!l.hasChunk(x,z)||!l.areEntitiesLoaded(ChunkPos.asLong(x,z)))return false;
        return true;
    }
    private static boolean vacant(ServerLevel l,AABB a){return l.players().stream().noneMatch(p->a.inflate(8).intersects(p.getBoundingBox()));}
    /** Also used by the native persistence fixture; each invocation retains the production visit/time cap. */
    public static boolean step(ServerLevel l,BlockPos origin,LabyrinthPlace place){
        var data=LabyrinthData.get(l.getServer());String key=key(origin,place);
        if(data.state(STATE).getBoolean(key))return true;
        var area=IndianLakeRooms.bounds(LabyrinthPlaces.base(origin,place),place);
        if(!loaded(l,area)||!vacant(l,area))return false;
        if(work==null)work=new Work(l,origin,place);
        if(work.level!=l||work.place!=place||!work.origin.equals(origin))return false;
        // Saved NPCs, dropped originals and other real entities keep their bodies and locations.
        List<AABB> occupied=l.getEntities((Entity)null,area,Entity::isAlive).stream().map(Entity::getBoundingBox).toList();
        var r=place.room();long end=System.nanoTime()+4_000_000L;int visits=0;
        while(work.cursor<work.total&&visits++<512&&System.nanoTime()<end){
            int i=work.cursor++;var at=work.base.offset(r.minX()+i%work.width,r.minY()+i/(work.width*work.depth),r.minZ()+i/work.width%work.depth);
            var old=work.before.plannedState(at);var recent=work.previous.plannedState(at);var next=work.after.plannedState(at);var actual=l.getBlockState(at);
            if(old==null||recent==null||next==null||actual.equals(next))continue;
            if(occupied.stream().anyMatch(a->a.intersects(new AABB(at))))continue;
            // Peel holes, moving doors, switches, staged fittings and player edits are never reset.
            if(actual.getBlock() instanceof DoorBlock&&old.getBlock()==actual.getBlock()&&next.getBlock() instanceof DoorBlock){
                next=next.setValue(DoorBlock.FACING,actual.getValue(DoorBlock.FACING)).setValue(DoorBlock.HALF,actual.getValue(DoorBlock.HALF))
                    .setValue(DoorBlock.HINGE,actual.getValue(DoorBlock.HINGE)).setValue(DoorBlock.OPEN,actual.getValue(DoorBlock.OPEN)).setValue(DoorBlock.POWERED,actual.getValue(DoorBlock.POWERED));
            }else if(!actual.equals(old)&&!actual.equals(recent))continue;
            if(place==LabyrinthPlace.LAYERED_MAN&&next.getBlock() instanceof DoorBlock&&(at.equals(work.base.offset(UncannyRooms.CELLAR_DOOR))||at.equals(work.base.offset(UncannyRooms.CELLAR_DOOR.above()))))next=next.setValue(DoorBlock.OPEN,work.cellarDoorOpen);
            var be=l.getBlockEntity(at);if(be!=null&&(!(be instanceof Container c)||!c.isEmpty()))continue;
            l.setBlock(at,next,UncannyRooms.FLAGS);
        }
        if(work.cursor<work.total)return false;
        var saved=data.state(STATE);saved.putBoolean(key,true);data.setState(STATE,saved);work=null;return true;
    }
}
