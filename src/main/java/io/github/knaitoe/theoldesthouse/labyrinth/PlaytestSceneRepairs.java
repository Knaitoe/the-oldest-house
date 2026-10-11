package io.github.knaitoe.theoldesthouse.labyrinth;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
/** Small, once-only changes to authored cells in a vacant saved scene. No room, actor, container or paper is rebuilt. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class PlaytestSceneRepairs {
    public static final String STATE="scene_playtest_0483";
    private static final int FLAGS=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE|Block.UPDATE_SUPPRESS_DROPS;
    private PlaytestSceneRepairs(){}
    private static BlockState furniture(HouseholdFurnitureBlock.Kind kind,Direction facing){return HouseBlocks.HOUSEHOLD_FURNITURE.get().defaultBlockState().setValue(HouseholdFurnitureBlock.KIND,kind).setValue(HouseholdFurnitureBlock.FACING,facing);}
    public static boolean cottage(ServerLevel l,BlockPos b){var empty=Blocks.AIR.defaultBlockState();var wanted=new LinkedHashMap<BlockPos,BlockState>();var old=new HashMap<BlockPos,BlockState>();
        old.put(new BlockPos(-4,0,-11),furniture(HouseholdFurnitureBlock.Kind.CHESS_TABLE,Direction.EAST));
        old.put(new BlockPos(-5,0,-11),furniture(HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,Direction.EAST));
        old.put(new BlockPos(-3,0,-11),furniture(HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.WEST));
        old.put(new BlockPos(-6,0,-14),furniture(HouseholdFurnitureBlock.Kind.FLORAL_ARMCHAIR,Direction.NORTH));
        old.put(new BlockPos(-6,0,-8),furniture(HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.SOUTH));
        old.put(new BlockPos(-6,0,-13),furniture(HouseholdFurnitureBlock.Kind.FOOTSTOOL,Direction.NORTH));
        old.keySet().forEach(at->wanted.put(at,empty));
        wanted.put(new BlockPos(-6,0,-10),furniture(HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,Direction.NORTH));
        wanted.put(new BlockPos(-6,0,-11),furniture(HouseholdFurnitureBlock.Kind.CHESS_TABLE,Direction.NORTH));
        wanted.put(new BlockPos(-4,0,-11),furniture(HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.WEST));
        wanted.put(new BlockPos(-7,0,-14),furniture(HouseholdFurnitureBlock.Kind.FLORAL_ARMCHAIR,Direction.SOUTH));
        wanted.put(new BlockPos(-7,0,-13),furniture(HouseholdFurnitureBlock.Kind.FOOTSTOOL,Direction.NORTH));
        wanted.put(new BlockPos(-6,0,-12),furniture(HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.SOUTH));
        wanted.put(new BlockPos(-6,1,-11),CottageBlock.of(CottageBlock.Kind.CHESSBOARD,Direction.NORTH));
        for(var entry:wanted.entrySet()){var current=l.getBlockState(b.offset(entry.getKey()));if(!current.isAir()&&!(current.getBlock() instanceof RugFloorBlock)&&!current.equals(entry.getValue())&&!current.equals(old.get(entry.getKey())))return false;}
        wanted.forEach((at,s)->l.setBlock(b.offset(at),s,FLAGS));return true;
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){if(e.getServer().getTickCount()%40!=0)return;var origin=HouseSavedData.get(e.getServer()).houseOrigin();if(origin==null)return;var d=LabyrinthData.get(e.getServer());
        for(var scene:List.of(LabyrinthPlace.THREE_FINGERS,LabyrinthPlace.CAMP_BLOOD)){if(!LabyrinthBuilder.isPlaceReady(d,scene))continue;String key=origin.asLong()+":"+scene.id();if(d.stateEntry(STATE,key).getBoolean("Done"))continue;
            var l=e.getServer().getLevel(LiteraryRooms.outside(scene)?HouseDimensions.OUTSIDE:HouseDimensions.INTERIOR);var b=LabyrinthPlaces.base(origin,scene);if(l==null)continue;var r=scene.room();var area=new AABB(Vec3.atLowerCornerOf(b.offset(r.minX(),-2,r.minZ())),Vec3.atLowerCornerOf(b.offset(r.maxX()+1,7,r.maxZ()+1)));
            if(!ScenePolish.loaded(l,area,b)||l.players().stream().anyMatch(p->area.inflate(24).intersects(p.getCamera().getBoundingBox())))continue;
            if(scene==LabyrinthPlace.THREE_FINGERS&&!cottage(l,b))continue;
            var done=d.stateEntry(STATE,key);if(scene==LabyrinthPlace.CAMP_BLOOD){int next=CampBloodDecor.applySlice(l,b,done.getInt("Index"));done.putInt("Index",next);done.putBoolean("Done",next>=CampBloodDecor.cells().size());}
            else done.putBoolean("Done",true);d.setStateEntry(STATE,key,done);
        }
    }
}
