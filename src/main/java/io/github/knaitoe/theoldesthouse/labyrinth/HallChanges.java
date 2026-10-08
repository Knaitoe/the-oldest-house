package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import static io.github.knaitoe.theoldesthouse.labyrinth.HallChangeBlock.Kind.*;

/** Three ordinary halls remember one unseen change; removed or edited pieces are never restaged. */
public final class HallChanges {
    public static final String STATE="hall_changes_0457";
    public static final List<LabyrinthPlace> PLACES=List.of(LabyrinthPlace.ALCOVE_HALL,LabyrinthPlace.STONE_ARCADE,LabyrinthPlace.STONE_LANDING);
    private HallChanges(){}
    public static BlockPos position(BlockPos base,LabyrinthPlace place){return switch(place){
        case ALCOVE_HALL->base.offset(-6,1,-16);case STONE_ARCADE->base.offset(-4,2,-26);case STONE_LANDING->base.offset(-7,0,-19);default->throw new IllegalArgumentException(place.id());};}
    public static net.minecraft.world.level.block.state.BlockState initial(LabyrinthPlace p){return switch(p){
        case ALCOVE_HALL->HallChangeBlock.state(PICTURE,Direction.NORTH);case STONE_ARCADE->HallChangeBlock.state(LAMP,Direction.EAST);case STONE_LANDING->HallChangeBlock.state(RUG,Direction.NORTH);default->throw new IllegalArgumentException(p.id());};}
    public static AABB area(BlockPos b,LabyrinthPlace p){var box=p.room();return new AABB(b.getX()+box.minX(),b.getY()+box.minY(),b.getZ()+box.minZ(),b.getX()+box.maxX()+1,b.getY()+box.maxY()+1,b.getZ()+box.maxZ()+1);}
    public static void tick(ServerLevel l,BlockPos origin,LabyrinthData data){
        for(var p:PLACES){if(!LabyrinthBuilder.isPlaceReady(data,p))continue;
            String key=origin.asLong()+":"+p.id();int visits=data.stateEntry(HallAtmosphere.STATE,key).getInt("Returns");
            var own=data.stateEntry(STATE,key);
            if(visits>0&&advance(l,LabyrinthPlaces.base(origin,p),p,own,visits))data.setStateEntry(STATE,key,own);
        }
    }
    /** The caller saves every completed stage. Deferred stages do not consume visits or load chunks. */
    public static boolean advance(ServerLevel l,BlockPos base,LabyrinthPlace p,CompoundTag own,int visits){
        if(own.getBoolean("Done")||visits<=0||own.getBoolean("Staged")&&visits-own.getInt("FirstVisit")<2)return false;
        if(!SceneVacancy.ready(l,area(base,p),48))return false;
        var at=position(base,p);var expected=initial(p);
        if(!own.getBoolean("Staged")){
            own.putBoolean("Staged",true);own.putInt("FirstVisit",visits);
            // No deferred retry can put a prop back over a later player edit.
            if(!l.getBlockState(at).isAir()||l.getBlockEntity(at)!=null||!expected.canSurvive(l,at)){own.putBoolean("Done",true);return true;}
            l.setBlock(at,expected,Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);return true;
        }
        own.putBoolean("Done",true);
        if(!l.getBlockState(at).equals(expected)||l.getBlockEntity(at)!=null||!expected.canSurvive(l,at))return true;
        var next=switch(expected.getValue(HallChangeBlock.KIND)){case PICTURE->PICTURE_TURNED;case LAMP->LAMP_OFF;default->RUG_FOLDED;};
        l.setBlock(at,expected.setValue(HallChangeBlock.KIND,next),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);return true;
    }
}
