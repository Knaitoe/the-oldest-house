package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Two small saved repairs; no restaging, replenishment or recurring enforcement. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class ReviewScenery0491 {
    public static final String STATE="review_scenery0491";
    private ReviewScenery0491(){}
    private static String key(BlockPos b,LabyrinthPlace p){return b.asLong()+":"+p.id();}
    public static void fresh(ServerLevel l,BlockPos b,LabyrinthPlace p){BuildBlocks.after(l,()->{var d=LabyrinthData.get(l.getServer());var t=d.state(STATE);t.putBoolean(key(b,p),true);d.setState(STATE,t);});}
    public static boolean repair(ServerLevel l,BlockPos b,LabyrinthPlace p){
        var d=LabyrinthData.get(l.getServer());var state=d.state(STATE);if(state.getBoolean(key(b,p)))return true;
        var area=p==LabyrinthPlace.GOATMAN?new AABB(Vec3.atLowerCornerOf(b.offset(-1,0,-69)),Vec3.atLowerCornerOf(b.offset(2,3,-60))):new AABB(Vec3.atLowerCornerOf(b.offset(0,-1,-25)),Vec3.atLowerCornerOf(b.offset(1,6,-23)));
        for(int x=(int)area.minX>>4;x<=(int)area.maxX>>4;x++)for(int z=(int)area.minZ>>4;z<=(int)area.maxZ>>4;z++)if(!l.hasChunk(x,z)||!l.areEntitiesLoaded(ChunkPos.asLong(x,z)))return false;
        if(l.players().stream().anyMatch(reader->area.inflate(40).intersects(reader.getCamera().getBoundingBox())))return false;
        var edits=new LinkedHashMap<BlockPos,BlockState>();
        if(p==LabyrinthPlace.GOATMAN){for(int x=-1;x<=1;x++)for(int z=-69;z<=-61;z++){
            var at=b.offset(x,1,z);var old=l.getBlockState(at);
            if(old.is(HouseBlocks.HOUSEHOLD_FURNITURE.get())&&old.getValue(HouseholdFurnitureBlock.KIND)==HouseholdFurnitureBlock.Kind.FORMICA_TABLE||old.is(Blocks.SPRUCE_SLAB)&&old.getValue(SlabBlock.TYPE)==SlabType.TOP)edits.put(at,TrailerTableBlock.tile(x,z));
        }}else if(p==LabyrinthPlace.HOLY_RABBIT&&l.getBlockState(b.offset(0,2,-25)).is(LiteraryRegistry.CARVED_TRUNK.get())){
            for(int y:new int[]{0,1,4}){var at=b.offset(0,y,-25);if(l.getBlockState(at).isAir()||l.getBlockState(at).is(Blocks.SPRUCE_LEAVES))edits.put(at,Blocks.OAK_LOG.defaultBlockState());}
        }
        for(var e:edits.entrySet()){
            var at=e.getKey();if(l.getBlockEntity(at)!=null)return false;
            var change=Shapes.joinUnoptimized(l.getBlockState(at).getCollisionShape(l,at),e.getValue().getCollisionShape(l,at),BooleanOp.NOT_SAME);
            for(var box:change.toAabbs())if(!l.getEntitiesOfClass(LivingEntity.class,box.move(at).inflate(.02),LivingEntity::isAlive).isEmpty())return false;
        }
        edits.forEach((at,next)->l.setBlock(at,next,18));state.putBoolean(key(b,p),true);d.setState(STATE,state);return true;
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){var s=e.getServer();if(s instanceof net.minecraft.gametest.framework.GameTestServer||s.getTickCount()%40!=23||LabyrinthBuilder.isCarving())return;var o=HouseSavedData.get(s).houseOrigin();if(o==null)return;for(var p:List.of(LabyrinthPlace.GOATMAN,LabyrinthPlace.HOLY_RABBIT)){if(!LabyrinthBuilder.isPlaceReady(s,p))continue;var l=s.getLevel(NovelRooms.dimension(p));if(l!=null)repair(l,LabyrinthPlaces.base(o,p),p);}}
}
