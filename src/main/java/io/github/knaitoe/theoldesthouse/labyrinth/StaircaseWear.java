package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
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
        for(var mark:marks(origin)){
            for(var at:mark.cells())if(blocks.containsKey(at))blocks.put(at,Blocks.AIR.defaultBlockState());
            // The bars either side of the break stop at it instead of reaching into the gap.
            for(var side:Direction.Plane.HORIZONTAL){var next=mark.rail().relative(side);var bars=blocks.get(next);
                if(bars!=null&&bars.getBlock() instanceof IronBarsBlock)blocks.put(next,bars.setValue(CrossCollisionBlock.PROPERTY_BY_DIRECTION.get(side.getOpposite()),false));}
        }
    }
    private static boolean authored(BlockState s){return s.isAir()||s.is(Blocks.IRON_BARS)||s.is(HouseBlocks.STAIRCASE_STONE.get())||s.is(HouseBlocks.STAIRCASE_STAIRS.get())
            ||s.is(Blocks.TUFF_BRICKS)||s.is(Blocks.TUFF_BRICK_STAIRS)||s.is(Blocks.DEEPSLATE_BRICKS)||s.is(Blocks.DEEPSLATE_BRICK_STAIRS)
            ||s.is(Blocks.POLISHED_BLACKSTONE_BRICKS)||s.is(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS);}
    /**
     * Cameras are kept further off than the open shaft is wide (69 blocks), so no one across the
     * spiral or on a flight above or below watches the break appear.
     */
    public static final double CAMERA_MARGIN=72;
    /** False means wait. True means finished or honored player edits, without forcing any native load. */
    public static boolean apply(ServerLevel level,Mark mark){
        var area=new AABB(mark.hole()).inflate(6,8,6);
        if(!SceneVacancy.ready(level,area,CAMERA_MARGIN))return false;
        for(var at:mark.cells())if(level.getBlockEntity(at)!=null||!authored(level.getBlockState(at)))return true;
        for(var at:mark.cells())level.setBlock(at,Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);
        for(var side:Direction.Plane.HORIZONTAL){var next=mark.rail().relative(side);var bars=level.getBlockState(next);
            if(bars.getBlock() instanceof IronBarsBlock)level.setBlock(next,bars.setValue(CrossCollisionBlock.PROPERTY_BY_DIRECTION.get(side.getOpposite()),false),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);}
        return true;
    }
    // The marks follow from the origin alone; worlds whose wear is finished stop looking.
    private static BlockPos cachedOrigin;private static List<Mark> cachedMarks=List.of();private static boolean finished;
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();if(server.getTickCount()%20!=0)return;
        var origin=HouseSavedData.get(server).houseOrigin();var level=server.getLevel(HouseDimensions.INTERIOR);if(origin==null||level==null)return;
        var data=LabyrinthData.get(server);if(!data.state("finale_architecture_049").getBoolean("Ready"))return;
        if(!origin.equals(cachedOrigin)){cachedOrigin=origin;cachedMarks=marks(origin);finished=false;}
        if(finished)return;
        String key=Long.toString(origin.asLong());var own=data.stateEntry(STATE,key);boolean pending=false;
        for(var mark:cachedMarks){
            String id=Long.toString(mark.hole().asLong());if(own.getBoolean(id))continue;
            pending=true;
            if(apply(level,mark)){own.putBoolean(id,true);data.setStateEntry(STATE,key,own);return;}
        }
        finished=!pending;
    }
}
