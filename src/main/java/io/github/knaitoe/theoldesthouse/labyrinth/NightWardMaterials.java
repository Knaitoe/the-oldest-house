package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import java.util.LinkedHashMap;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import static io.github.knaitoe.theoldesthouse.labyrinth.NightWardBlock.Kind.*;

/** Cosmetic, bounded refit of the night-duty hospital; its incubator, call and original chart stay in place. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class NightWardMaterials {
    public static final String STATE="night_ward_materials0507";
    private NightWardMaterials(){}
    public static void fresh(ServerLevel l,BlockPos b){refit(l,b,false);}
    public static boolean apply(ServerLevel l,BlockPos b){return refit(l,b,true);}
    private static boolean refit(ServerLevel l,BlockPos b,boolean guarded){
        var data=LabyrinthData.get(l.getServer());var done=data.state(STATE);String key=Long.toString(b.asLong());
        if(guarded&&done.getBoolean(key))return true;
        var area=new AABB(Vec3.atLowerCornerOf(b.offset(-13,-1,-26)),Vec3.atLowerCornerOf(b.offset(14,8,3)));
        if(guarded){
            for(int x=(int)area.minX>>4;x<=(int)area.maxX>>4;x++)for(int z=(int)area.minZ>>4;z<=(int)area.maxZ>>4;z++)
                if(!l.hasChunk(x,z)||!l.areEntitiesLoaded(ChunkPos.asLong(x,z)))return false;
            if(l.players().stream().anyMatch(p->area.inflate(48).intersects(p.getCamera().getBoundingBox()))||!l.getEntitiesOfClass(LivingEntity.class,area).isEmpty())return false;
        }
        var edits=new LinkedHashMap<BlockPos,BlockState>();
        for(var pos:BlockPos.betweenClosed(b.offset(-12,-1,-25),b.offset(12,6,1))){
            var rel=pos.subtract(b);var old=l.getBlockState(pos);var next=replacement(old,rel);
            if(next!=null&&!next.equals(old)){
                var be=l.getBlockEntity(pos);if(be!=null&&!(be instanceof BedBlockEntity))continue;
                edits.put(pos.immutable(),next);
            }
        }
        for(var e:edits.entrySet())l.setBlock(e.getKey(),e.getValue(),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);
        for(var pos:edits.keySet()){var s=l.getBlockState(pos);if(s.is(NovelRegistry.NIGHT_WARD_GLASS.get()))l.setBlock(pos,Block.updateFromNeighbourShapes(s,l,pos),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);}
        done.putBoolean(key,true);data.setState(STATE,done);return true;
    }
    private static BlockState replacement(BlockState s,BlockPos r){
        int x=r.getX(),y=r.getY(),z=r.getZ();
        if(r.equals(new BlockPos(6,1,-14))&&s.is(NovelRegistry.INSTITUTE.get()))return NightWardBlock.of(CALL_PANEL,Direction.WEST);
        if(y==-1&&(s.is(Blocks.WHITE_CONCRETE)||s.is(Blocks.LIGHT_GRAY_CONCRETE)))return NightWardBlock.of(FLOOR);
        if(y==5&&(s.is(Blocks.BIRCH_PLANKS)||s.is(Blocks.WHITE_CONCRETE)||s.is(Blocks.LIGHT_GRAY_CONCRETE)))return NightWardBlock.of(CEILING);
        if(s.is(NovelRegistry.INSTITUTE.get())||s.is(Blocks.WHITE_CONCRETE)||s.is(Blocks.LIGHT_GRAY_CONCRETE)||s.is(Blocks.SMOOTH_QUARTZ))return NightWardBlock.of(y<=1?DADO:WALL);
        if(s.is(Blocks.WHITE_WOOL))return NightWardBlock.of(CURTAIN);
        if(s.is(Blocks.SEA_LANTERN))return NightWardBlock.of(LIGHT);
        if(s.is(Blocks.IRON_BLOCK)&&y==4)return NightWardBlock.of(CEILING);
        if(s.is(Blocks.LIGHT_BLUE_STAINED_GLASS))return NightWardBlock.of(WINDOW);
        if(s.is(Blocks.WHITE_STAINED_GLASS_PANE)){
            var n=NovelRegistry.NIGHT_WARD_GLASS.get().defaultBlockState();for(var p:new BooleanProperty[]{IronBarsBlock.NORTH,IronBarsBlock.SOUTH,IronBarsBlock.EAST,IronBarsBlock.WEST,IronBarsBlock.WATERLOGGED})n=n.setValue(p,s.getValue(p));return n;
        }
        if(s.is(Blocks.WHITE_BED))return NightWardBlock.of(s.getValue(BedBlock.PART)==BedPart.HEAD?BED_HEAD:BED_FOOT,s.getValue(BedBlock.FACING));
        if(s.is(Blocks.DARK_OAK_DOOR)||s.is(Blocks.BIRCH_DOOR)){
            var n=NovelRegistry.NIGHT_WARD_DOOR.get().defaultBlockState();return n.setValue(DoorBlock.FACING,s.getValue(DoorBlock.FACING)).setValue(DoorBlock.HALF,s.getValue(DoorBlock.HALF)).setValue(DoorBlock.HINGE,s.getValue(DoorBlock.HINGE)).setValue(DoorBlock.OPEN,s.getValue(DoorBlock.OPEN)).setValue(DoorBlock.POWERED,s.getValue(DoorBlock.POWERED));
        }
        if(s.is(HouseBlocks.HOUSEHOLD_FURNITURE.get())){
            var k=switch(s.getValue(HouseholdFurnitureBlock.KIND)){
                case CANE_CHAIR,GREEN_ARMCHAIR,FLORAL_ARMCHAIR->CHAIR;
                case BEDSIDE_TABLE,CHEST_OF_DRAWERS->BEDSIDE;
                case WALNUT_DESK,READING_DESK,FORMICA_TABLE->TABLE;
                case KITCHEN_STOOL,FOOTSTOOL->STOOL;
                default->null;
            };if(k!=null)return NightWardBlock.of(k,s.getValue(HouseholdFurnitureBlock.FACING));
        }
        return null;
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post event){
        var server=event.getServer();if(server instanceof net.minecraft.gametest.framework.GameTestServer||server.getTickCount()%40!=29||LabyrinthBuilder.isCarving())return;
        var origin=HouseSavedData.get(server).houseOrigin();if(origin==null)return;var data=LabyrinthData.get(server);
        var l=server.getLevel(HouseDimensions.INTERIOR);if(l!=null&&LabyrinthBuilder.isPlaceReady(data,LabyrinthPlace.HOSPITAL))apply(l,LabyrinthPlaces.base(origin,LabyrinthPlace.HOSPITAL));
    }
}
