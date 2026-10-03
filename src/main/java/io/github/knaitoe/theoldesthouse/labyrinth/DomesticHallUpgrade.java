package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Once-only repair of ordinary hall fabric and retired side doors. Originals are retained. */
public final class DomesticHallUpgrade {
    public static final String STATE="domestic_halls_0430";
    private static final Set<String> RETIRED=Set.of("straight_hall/west_near","straight_hall/east_middle","straight_hall/west_far",
            "bent_hall/east_near","bent_hall/bend","bent_hall/west_far","cross_hall/west_near","cross_hall/east_far");
    private DomesticHallUpgrade(){}
    public static void tick(MinecraftServer server){
        var l=server.getLevel(HouseDimensions.INTERIOR);var origin=HouseSavedData.get(server).houseOrigin();
        if(l==null||origin==null||l.getGameTime()%20!=0||LabyrinthBuilder.isCarving())return;
        var data=LabyrinthData.get(server);if(!origin.equals(data.builtOrigin())||data.builtVersion()<17)return;
        var state=data.state(STATE);String prefix=origin.asLong()+":";
        for(var place:List.of(LabyrinthPlace.STRAIGHT_HALL,LabyrinthPlace.BENT_HALL,LabyrinthPlace.CROSS_HALL)){
            if(state.getBoolean(prefix+place.id()))continue;
            var base=LabyrinthPlaces.base(origin,place);var bounds=LabyrinthPlaces.placeBounds(origin,place);
            if(base==null||bounds==null||l.players().stream().anyMatch(p->bounds.isInside(p.blockPosition())))continue;
            // A Stay order can leave a real pet here after every explorer departs.
            // Do not close an old door around that resident's living collision body.
            if(!l.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,new net.minecraft.world.phys.AABB(bounds.minX(),bounds.minY(),bounds.minZ(),bounds.maxX()+1,bounds.maxY()+1,bounds.maxZ()+1),e->e.isAlive()).isEmpty())continue;
            if(data.door(place.entryDoorId())==null)continue;
            apply(l,base,place);state.putBoolean(prefix+place.id(),true);data.setState(STATE,state);
        }
    }
    public static void apply(ServerLevel l,BlockPos base,LabyrinthPlace place){
        if(!LabyrinthHalls.isHall(place))return;
        var data=LabyrinthData.get(l.getServer());var floor=LabyrinthHalls.floor(place);int height=LabyrinthHalls.height(place),f=LabyrinthBuilder.flags();
        // Only corridor columns change. The furnished fragments and their inventories remain native.
        for(var rel:floor){
            if(inFragment(place,rel))continue;
            var at=base.offset(rel);BlockState below=l.getBlockState(at.below());
            if(fabric(below)&&l.getBlockEntity(at.below())==null)l.setBlock(at.below(),LabyrinthHalls.floorState(place),f);
            for(int y=0;y<=height;y++){
                var p=at.above(y);var old=l.getBlockState(p);
                if(fabric(old)&&l.getBlockEntity(p)==null)l.setBlock(p,Blocks.AIR.defaultBlockState(),f);
            }
            var ceiling=at.above(height+1);if(fabric(l.getBlockState(ceiling))&&l.getBlockEntity(ceiling)==null)l.setBlock(ceiling,Blocks.WHITE_CONCRETE.defaultBlockState(),f);
            for(Direction direction:Direction.Plane.HORIZONTAL){
                var neighbor=rel.relative(direction);if(floor.contains(neighbor)||inFragment(place,neighbor))continue;
                for(int y=0;y<=height;y++){
                    var wall=base.offset(neighbor).above(y);var old=l.getBlockState(wall);
                    if(fabric(old)&&l.getBlockEntity(wall)==null)l.setBlock(wall,(y==0?Blocks.STRIPPED_OAK_WOOD:place==LabyrinthPlace.BENT_HALL?Blocks.LIGHT_GRAY_TERRACOTTA:Blocks.WHITE_TERRACOTTA).defaultBlockState(),f);
                }
            }
        }
        var onward=place.doors().stream().filter(d->d.name().equals("far")).findFirst().orElse(null);
        for(String id:RETIRED){
            if(!id.startsWith(place.id()+"/"))continue;var old=data.door(id);if(old==null)continue;
            if(onward!=null){var dest=base.offset(onward.rel());data.remapReturns(point->point.door()&&point.dimension().equals(old.dimension)&&point.pos().distanceToSqr(Vec3.atBottomCenterOf(old.lower))<.01
                    ?new LabyrinthData.Waypoint(old.dimension,Vec3.atBottomCenterOf(dest),onward.facing().toYRot(),true):point);}
            for(int y=0;y<2;y++){var p=old.lower.above(y);if(l.getBlockState(p).getBlock() instanceof DoorBlock)l.setBlock(p,(y==0?Blocks.STRIPPED_OAK_WOOD:Blocks.WHITE_TERRACOTTA).defaultBlockState(),f);}
            data.removeDoor(id);
        }
        // Move only native corridor lamps when raising the junction ceiling.
        if(place==LabyrinthPlace.CROSS_HALL)for(var rel:floor){
            if(inFragment(place,rel))continue;var at=base.offset(rel).above(3);
            if(l.getBlockState(at).is(Blocks.LANTERN)&&l.getBlockState(at.above()).isAir()){
                l.setBlock(at,Blocks.AIR.defaultBlockState(),f);LabyrinthBuilder.hangLantern(l,at.above(),false);
            }
        }
    }
    private static boolean inFragment(LabyrinthPlace place,BlockPos p){
        for(var fragment:LabyrinthDomestic.fragments(place))if(p.getX()>=fragment.x0()-1&&p.getX()<=fragment.x1()+1&&p.getZ()>=fragment.z0()-1&&p.getZ()<=fragment.z1()+1)return true;
        return false;
    }
    private static boolean fabric(BlockState s){return s.is(Blocks.WHITE_TERRACOTTA)||s.is(Blocks.LIGHT_GRAY_TERRACOTTA)||s.is(Blocks.OAK_PLANKS)||s.is(Blocks.SPRUCE_PLANKS)
            ||s.is(Blocks.BIRCH_PLANKS)||s.is(Blocks.DARK_OAK_PLANKS)||s.is(Blocks.SMOOTH_STONE)||s.is(Blocks.WHITE_CONCRETE)||s.is(Blocks.POLISHED_ANDESITE)||s.is(Blocks.CRACKED_STONE_BRICKS);}
}
