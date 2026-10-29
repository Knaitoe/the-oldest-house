package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.*;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Every vignette has a way on into the ordinary halls (owner, October 28): one fitted door per scene, set where it
 * belongs in that scene, or a far door the scene already had. It always leads into a hall, obeys the same locks as the
 * scene's entry, and going on through it is leaving the scene.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class OnwardDoors {
    public static final String NAME="onward";
    record Spec(BlockPos rel,Direction facing,Supplier<? extends Block> door,@Nullable Supplier<? extends Block> frame){}
    private static final Map<LabyrinthPlace,Spec> SPECS=new EnumMap<>(LabyrinthPlace.class);
    private static final Map<LabyrinthPlace,String> ADOPTED=new EnumMap<>(LabyrinthPlace.class);
    private static final int FRAME=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private OnwardDoors(){}
    interface Registrar{
        void put(LabyrinthPlace place,int x,int y,int z,Direction facing,Supplier<? extends Block> door,@Nullable Supplier<? extends Block> frame);
        default void put(LabyrinthPlace place,int x,int y,int z,Direction facing,Supplier<? extends Block> door){put(place,x,y,z,facing,door,null);}
        void adopt(LabyrinthPlace place,String door);
    }
    static {
        OnwardPlacements.register(new Registrar(){
            @Override public void put(LabyrinthPlace place,int x,int y,int z,Direction facing,Supplier<? extends Block> door,@Nullable Supplier<? extends Block> frame){SPECS.put(place,new Spec(new BlockPos(x,y,z),facing,door,frame));}
            @Override public void adopt(LabyrinthPlace place,String door){ADOPTED.put(place,door);}
        });
    }

    /** Every scene with a way on, fitted or adopted. */
    public static Set<LabyrinthPlace> places(){var all=EnumSet.noneOf(LabyrinthPlace.class);all.addAll(SPECS.keySet());all.addAll(ADOPTED.keySet());return all;}
    /** The fitted door's spec, appended to the scene's own doors; null where the scene has none or adopts one of its own. */
    public static @Nullable LabyrinthPlace.DoorSpec doorSpec(LabyrinthPlace place){var s=SPECS.get(place);return s==null?null:new LabyrinthPlace.DoorSpec(NAME,s.rel(),s.facing(),LabyrinthData.DEALT);}
    public static boolean isOnward(LabyrinthPlace.DoorSpec spec){return NAME.equals(spec.name());}
    public static boolean isOnward(LabyrinthData.Door door){return placeOf(door)!=null;}
    public static @Nullable LabyrinthPlace placeOf(LabyrinthData.Door door){
        // A copy's way on carries its copy's number (SceneInstances.doorId).
        var id=SceneInstances.plain(door.id);
        int slash=id.lastIndexOf('/');if(slash<0)return null;var place=LabyrinthPlace.byId(id.substring(0,slash));
        return place!=null&&places().contains(place)&&id(place).equals(id)?place:null;
    }
    public static String id(LabyrinthPlace place){return place.id()+"/"+name(place);}
    /** The way on's own name: the fitted door's, or the scene's own door it adopts. */
    public static String name(LabyrinthPlace place){return ADOPTED.getOrDefault(place,NAME);}
    public static @Nullable BlockPos lower(BlockPos base,LabyrinthPlace place){var s=SPECS.get(place);return s==null||base==null?null:base.offset(s.rel());}

    /** The door, its lintel and the two cells before it, kept clear of every dressing pass so the door always has its wall and its way up. */
    public static boolean reserved(LabyrinthPlace place,int x,int y,int z){
        // Called for every cell the dressing passes consider, so it allocates nothing.
        var s=SPECS.get(place);if(s==null)return false;var r=s.rel();int dy=y-r.getY();if(dy<0||dy>2)return false;
        int dx=x-r.getX(),dz=z-r.getZ();var f=s.facing();
        for(int i=0;i<=2;i++)if(dx==f.getStepX()*i&&dz==f.getStepZ()*i)return i==0||dy<=1;
        return false;
    }

    /** Whatever holds a scene's entry shut during a committed beat holds its way on shut too. */
    public static boolean locked(ServerPlayer p,LabyrinthData.Door door){
        var place=placeOf(door);if(place==null)return false;var entry=SceneInstances.entryOf(LabyrinthData.get(p.server),place,door);
        return LiteraryVignettes.retreatLocked(p)||entry!=null&&(NovelVignettes.exitLocked(p,entry)||VignetteGate.exitLocked(p,entry));
    }

    private static boolean passable(ServerLevel l,BlockPos at){var s=l.getBlockState(at);return s.getCollisionShape(l,at).isEmpty()&&s.getFluidState().isEmpty();}
    /** The scene's own wall or open air: never a container, a fitting or something a player has placed there since. */
    private static boolean authored(ServerLevel l,BlockPos at){var s=l.getBlockState(at);return s.isAir()||s.getBlock() instanceof DoorBlock||!s.hasBlockEntity()&&s.isCollisionShapeFullBlock(l,at);}
    /** Open ground a frame may stand in: air, snow or grass, never water or anything kept. */
    private static boolean open(ServerLevel l,BlockPos at){var s=l.getBlockState(at);return s.canBeReplaced()&&!s.hasBlockEntity()&&s.getFluidState().isEmpty();}

    /** Sets the fitted door and registers it, unless its cells or the way to it have changed. */
    public static boolean install(ServerLevel level,@Nullable BlockPos base,LabyrinthPlace place){return install(level,base,place,id(place));}
    /** The same at a copy of the scene, under the copy's own door id. */
    public static boolean install(ServerLevel level,@Nullable BlockPos base,LabyrinthPlace place,String id){
        var spec=SPECS.get(place);if(spec==null||base==null||!level.dimension().equals(NovelRooms.dimension(place)))return false;
        var at=base.offset(spec.rel());var front=at.relative(spec.facing());
        if(!authored(level,at)||!authored(level,at.above())||!passable(level,front)||!passable(level,front.above()))return false;
        NovelRooms.door(level,at,spec.facing(),spec.door().get(),false);
        if(spec.frame()!=null)frame(level,at,spec.facing(),spec.frame().get());
        LabyrinthData.get(level.getServer()).putDoor(new LabyrinthData.Door(id,level.dimension(),at,spec.facing(),LabyrinthData.DEALT,false));
        return true;
    }
    /** Two posts and a lintel round a door that stands where the ground runs out, wherever they find open ground. */
    private static void frame(ServerLevel level,BlockPos at,Direction facing,Block block){
        var along=facing.getClockWise();
        for(var side:new Direction[]{along,along.getOpposite()})for(int y=0;y<=1;y++){
            var post=at.relative(side).above(y);if(open(level,post))BuildBlocks.set(level,post,axis(block,Direction.Axis.Y),FRAME);
        }
        for(int i=-1;i<=1;i++){var beam=at.above(2).relative(along,i);if(open(level,beam))BuildBlocks.set(level,beam,axis(block,along.getAxis()),FRAME);}
    }
    private static BlockState axis(Block block,Direction.Axis axis){var s=block.defaultBlockState();return s.hasProperty(BlockStateProperties.AXIS)?s.setValue(BlockStateProperties.AXIS,axis):s;}

    /** Whether any camera, a spectator's included, is near enough and turned towards the cell to see a door appear. */
    static boolean seen(ServerLevel level,BlockPos at){
        var middle=Vec3.atCenterOf(at.above());
        for(var player:level.players()){
            var camera=player.getCamera();if(camera.level()!=level)continue;var eye=camera.getEyePosition();var to=middle.subtract(eye);double distance=to.length();
            if(distance<3)return true;if(distance>96)continue;
            if(camera.getViewVector(1).dot(to.scale(1/distance))>.17)return true; // within about eighty degrees of the view
        }
        return false;
    }
    private static boolean ready(ServerLevel level,BlockPos at){return level.isLoaded(at)&&level.isLoaded(at.above(2))&&level.areEntitiesLoaded(ChunkPos.asLong(at));}

    /** Before a reader comes into a scene whose door is missing, it is set while nobody can see it. */
    public static void prepare(ServerLevel level,LabyrinthPlace place){
        var server=level.getServer();var data=LabyrinthData.get(server);var spec=SPECS.get(place);
        if(spec==null||data.door(id(place))!=null||!LabyrinthBuilder.isPlaceReady(data,place))return;
        var origin=HouseSavedData.get(server).houseOrigin();var base=origin==null?null:LabyrinthPlaces.base(origin,place);
        if(base==null||!level.dimension().equals(NovelRooms.dimension(place)))return;var at=base.offset(spec.rel());
        if(ready(level,at)&&!seen(level,at))install(level,base,place);
    }
    /** A rebuilt room that had its door gets it back. */
    public static void restore(ServerLevel level,BlockPos base,LabyrinthPlace place){
        if(LabyrinthData.get(level.getServer()).door(id(place))!=null)install(level,base,place);
    }

    /** Scenes built before 0.4.144 get their door once nobody can see it go in; the adopted far doors lead on. */
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();if(server.getTickCount()%20!=7)return;
        var origin=HouseSavedData.get(server).houseOrigin();if(origin==null)return;var data=LabyrinthData.get(server);
        for(var entry:ADOPTED.entrySet()){
            var place=entry.getKey();var door=data.door(id(place));if(door==null)continue;
            var spec=place.doors().stream().filter(s->s.name().equals(entry.getValue())).findFirst().orElse(null);
            if(spec!=null&&(!LabyrinthData.DEALT.equals(door.destination)||door.facing!=spec.facing()))
                data.putDoor(new LabyrinthData.Door(door.id,door.dimension,door.lower,spec.facing(),LabyrinthData.DEALT,false));
        }
        for(var entry:SPECS.entrySet()){
            var place=entry.getKey();if(data.door(id(place))!=null||data.door(place.entryDoorId())==null||!LabyrinthBuilder.isPlaceReady(data,place))continue;
            var level=server.getLevel(NovelRooms.dimension(place));var base=LabyrinthPlaces.base(origin,place);if(level==null||base==null)continue;
            var at=base.offset(entry.getValue().rel());
            if(ready(level,at)&&!seen(level,at))install(level,base,place);
        }
    }
}
