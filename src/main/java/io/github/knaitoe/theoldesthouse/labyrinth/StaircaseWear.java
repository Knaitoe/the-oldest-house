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
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Sparse edge damage leaves the seven central walking columns and every landing intact. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class StaircaseWear {
    public static final String STATE="staircase_wear_0448";
    public record Mark(BlockPos route,BlockPos hole,BlockPos rail){
        public List<BlockPos> cells(){return List.of(hole,hole.below(),rail);}
    }
    private StaircaseWear(){}
    public static List<Mark> marks(BlockPos origin){
        var route=FinaleArchitecture.fullRoute(origin);var base=FinaleArchitecture.base(origin);var out=new ArrayList<Mark>();
        for(int i=217;i+1<route.size();i+=401){
            var at=route.get(i);var next=route.get(i+1);int x=at.getX()-base.getX(),z=at.getZ()-base.getZ();
            if(at.getY()>FinaleArchitecture.TOP-220||Math.abs(at.getY()-FinaleArchitecture.ARENA)<160||at.getY()<FinaleArchitecture.LOOP_BOTTOM+80)continue;
            boolean alongX=next.getX()!=at.getX();if(Math.abs(alongX?x:z)>15)continue;
            Direction forward=Direction.getNearest(next.getX()-at.getX(),0,next.getZ()-at.getZ());
            Direction side=(out.size()%2==0?forward.getClockWise():forward.getCounterClockWise());
            var hole=at.relative(side,4).below();
            if(StaircaseFire.braziers(origin).stream().anyMatch(p->p.distSqr(hole)<100)||StaircaseLeaves.positions(origin).stream().anyMatch(p->p.distSqr(hole)<100))continue;
            out.add(new Mark(at,hole,at.relative(side,5)));
        }return List.copyOf(out);
    }
    public static void decoratePlan(BlockPos origin,Map<BlockPos,BlockState> blocks){
        for(var mark:marks(origin))for(var at:mark.cells())if(blocks.containsKey(at))blocks.put(at,Blocks.AIR.defaultBlockState());
    }
    private static boolean authored(BlockState s){return s.isAir()||s.is(Blocks.IRON_BARS)||s.is(HouseBlocks.STAIRCASE_STONE.get())||s.is(HouseBlocks.STAIRCASE_STAIRS.get())
            ||s.is(Blocks.TUFF_BRICKS)||s.is(Blocks.TUFF_BRICK_STAIRS)||s.is(Blocks.DEEPSLATE_BRICKS)||s.is(Blocks.DEEPSLATE_BRICK_STAIRS)
            ||s.is(Blocks.POLISHED_BLACKSTONE_BRICKS)||s.is(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS);}
    /** False means wait. True means finished or honored player edits, without forcing any native load. */
    public static boolean apply(ServerLevel level,Mark mark){
        var area=new AABB(mark.hole()).inflate(6,8,6);
        for(var p:level.players())if(area.inflate(24).intersects(p.getBoundingBox()))return false;
        for(int x=((int)Math.floor(area.minX))>>4;x<=((int)Math.floor(area.maxX))>>4;x++)for(int z=((int)Math.floor(area.minZ))>>4;z<=((int)Math.floor(area.maxZ))>>4;z++)
            if(!level.isLoaded(new BlockPos(x<<4,mark.route().getY(),z<<4))||!level.areEntitiesLoaded(ChunkPos.asLong(x,z)))return false;
        if(!level.getEntitiesOfClass(LivingEntity.class,area,LivingEntity::isAlive).isEmpty())return false;
        for(var at:mark.cells())if(level.getBlockEntity(at)!=null||!authored(level.getBlockState(at)))return true;
        for(var at:mark.cells())level.setBlock(at,Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);
        return true;
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();if(server.getTickCount()%20!=0)return;
        var origin=HouseSavedData.get(server).houseOrigin();var level=server.getLevel(HouseDimensions.INTERIOR);if(origin==null||level==null)return;
        var data=LabyrinthData.get(server);if(!data.state("finale_architecture_049").getBoolean("Ready"))return;
        String key=Long.toString(origin.asLong());var own=data.stateEntry(STATE,key);
        for(var mark:marks(origin)){
            String id=Long.toString(mark.hole().asLong());if(own.getBoolean(id))continue;
            if(apply(level,mark)){own.putBoolean(id,true);data.setStateEntry(STATE,key,own);return;}
        }
    }
}
