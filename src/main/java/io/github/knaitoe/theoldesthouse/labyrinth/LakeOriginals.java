package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

/** Finite native lake props: an absent recorded UUID is never a request for a replacement. */
final class LakeOriginals {
    private LakeOriginals() {}

    @SuppressWarnings("unchecked")
    static <T extends Entity> @Nullable T ensure(ServerLevel level,BlockPos base,LabyrinthPlace place,
                                               String stateId,String role,String tag,EntityType<T> type,
                                               boolean fresh,Consumer<T> initialize) {
        var data=LabyrinthData.get(level.getServer());var state=data.state(stateId);
        var originals=state.getCompound("NativeOriginals0493");var record=originals.getCompound(role);
        if(record.getBoolean("Recorded")) {
            if(!record.hasUUID("Id"))return null;
            var entity=level.getEntity(record.getUUID("Id"));
            return entity!=null&&!entity.isRemoved()&&entity.getType()==type?(T)entity:null;
        }
        var area=IndianLakeRooms.bounds(base,place);
        if(!fresh&&!ScenePolish.loaded(level,area,base))return null;
        T original=null;
        for(Entity entity:level.getEntitiesOfClass(Entity.class,area,e->e.getType()==type&&e.getTags().contains(tag))) {
            if(original==null)original=(T)entity;else entity.discard();
        }
        if(original==null&&fresh) {
            original=type.create(level);if(original==null)return null;
            initialize.accept(original);if(!level.addFreshEntity(original))return null;
        }
        record.putBoolean("Recorded",true);
        if(original!=null)record.putUUID("Id",original.getUUID());
        originals.put(role,record);state.put("NativeOriginals0493",originals);data.setState(stateId,state);
        return original;
    }
}
