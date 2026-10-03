package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** Separate checkpoints repair old wall notes without repeating the earlier paper/actor migration. */
public final class WallNotesRepairs {
    public static final String STATE="wall_notes_0430";
    private WallNotesRepairs(){}
    public static void tick(MinecraftServer server){
        if(server.getTickCount()%20!=0||LabyrinthBuilder.isCarving())return;
        var house=HouseSavedData.get(server);var origin=house.houseOrigin();var inside=server.getLevel(HouseDimensions.INTERIOR);
        if(origin==null||inside==null)return;var data=LabyrinthData.get(server);
        if(!origin.equals(data.builtOrigin())||data.builtVersion()<21)return;
        var state=data.state(STATE);String prefix=origin.asLong()+":";
        String approach=prefix+"approach";
        var hall=new AABB(origin.getX()+HouseLayout.AXIS_X-2,origin.getY(),origin.getZ()+HouseImpossibleHallway.START_Z_OFFSET,
                origin.getX()+HouseLayout.AXIS_X+3,origin.getY()+6,origin.getZ()+HouseImpossibleHallway.END_Z_OFFSET+1);
        if(house.isImpossibleDoorRevealed()&&!state.getBoolean(approach)&&vacant(inside,hall)&&loaded(inside,hall,origin)){
            HouseImpossibleHallway.dressDomesticApproach(inside,origin);state.putBoolean(approach,true);
        }
        for(var place:List.of(LabyrinthPlace.BENT_HALL,LabyrinthPlace.GOATMAN,LabyrinthPlace.PLAIN)){
            String key=prefix+place.id();if(state.getBoolean(key)||data.door(place.entryDoorId())==null)continue;
            var level=server.getLevel(NovelRooms.dimension(place));var base=LabyrinthPlaces.base(origin,place);if(level==null||base==null)continue;
            var room=IndianLakeRooms.bounds(base,place);
            // The shared cousins stay in the clearing. Only bodies at the repaired throat can obstruct this edit.
            var work=place==LabyrinthPlace.GOATMAN?new AABB(base.getX()-1,base.getY(),base.getZ(),base.getX()+2,base.getY()+3,base.getZ()+1):room;
            if(level.players().stream().anyMatch(p->room.contains(p.position()))||!vacant(level,work)||!loaded(level,room,base))continue;
            if(place==LabyrinthPlace.BENT_HALL)neutralBedroom(level,base);
            else if(place==LabyrinthPlace.GOATMAN)GoatmanWoods.repairEntrance(level,base);
            else NovelRooms.repairPlainGround(level,base);
            state.putBoolean(key,true);
        }
        String maze=prefix+"west_connector";var b=FinaleArchitecture.base(origin);
        var cap=new AABB(b.getX()-25,FinaleArchitecture.ARENA-1,b.getZ()+38,b.getX()-17,FinaleArchitecture.ARENA+5,b.getZ()+39);
        if(!state.getBoolean(maze)&&FinaleArchitecture.ready(server)
                &&inside.players().stream().noneMatch(p->FinaleArchitecture.contains(origin,p.blockPosition()))&&vacant(inside,cap)&&loaded(inside,cap,b)){
            repairWestConnector(inside,origin);state.putBoolean(maze,true);
        }
        data.setState(STATE,state);
    }
    private static boolean vacant(ServerLevel l,AABB area){
        return l.players().stream().noneMatch(p->area.contains(p.position()))
                &&l.getEntitiesOfClass(LivingEntity.class,area,LivingEntity::isAlive).isEmpty();
    }
    /** Tickets let disk/entity loading proceed between ticks; never synchronously pull a whole old scene. */
    private static boolean loaded(ServerLevel l,AABB area,BlockPos ticket){
        boolean ready=true;
        for(int x=((int)Math.floor(area.minX))>>4;x<=((int)Math.ceil(area.maxX)-1)>>4;x++)
            for(int z=((int)Math.floor(area.minZ))>>4;z<=((int)Math.ceil(area.maxZ)-1)>>4;z++){
                var chunk=new ChunkPos(x,z);l.getChunkSource().addRegionTicket(TicketType.PORTAL,chunk,3,ticket);
                ready&=l.isLoaded(new BlockPos(x<<4,ticket.getY(),z<<4))&&l.areEntitiesLoaded(chunk.toLong());
            }
        return ready;
    }
    public static void neutralBedroom(ServerLevel l,BlockPos base){
        for(var f:LabyrinthDomestic.fragments(LabyrinthPlace.BENT_HALL))if(f.room()==LabyrinthDomestic.Room.BEDROOM)
            for(int x=f.x0()-1;x<=f.x1()+1;x++)for(int z=f.z0()-1;z<=f.z1()+1;z++){
                if(x!=f.x0()-1&&x!=f.x1()+1&&z!=f.z0()-1&&z!=f.z1()+1)continue;
                for(int y=0;y<=3;y++){var at=base.offset(x,y,z);if(l.getBlockState(at).is(Blocks.PINK_TERRACOTTA))l.setBlock(at,Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState(),LabyrinthBuilder.flags());}
            }
    }
    public static void repairWestConnector(ServerLevel l,BlockPos origin){
        var plan=new LinkedHashMap<BlockPos,BlockState>();StaircaseMazes.connectorCap(plan,FinaleArchitecture.base(origin));
        plan.forEach((at,s)->{if(l.getBlockState(at).isAir())l.setBlock(at,s,LabyrinthBuilder.flags());});
    }
}
