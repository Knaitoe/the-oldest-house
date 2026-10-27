package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** Small recognized shelf/window repairs, without rebuilding the institute or touching its originals. */
public final class WhaleFinishing {
    private static final String STATE="whale_finishing134";
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE|Block.UPDATE_SUPPRESS_DROPS;
    private record Window(BlockPos lower,boolean side){}
    private static final List<Window> WINDOWS=windows();
    private WhaleFinishing(){}
    private static List<Window> windows(){var out=new ArrayList<Window>();for(int z:new int[]{-11,-15,-19,-23})for(int x:new int[]{-14,14})out.add(new Window(new BlockPos(x,2,z),true));for(int x:new int[]{0,4,8})out.add(new Window(new BlockPos(x,2,-33),false));out.add(new Window(new BlockPos(0,7,-33),false));return List.copyOf(out);}
    public static BlockState pane(BlockState s,boolean side){return s.setValue(IronBarsBlock.NORTH,side).setValue(IronBarsBlock.SOUTH,side).setValue(IronBarsBlock.EAST,!side).setValue(IronBarsBlock.WEST,!side);}
    private static boolean safe(ServerLevel l,AABB area){
        for(int x=(int)Math.floor(area.minX)>>4;x<=((int)Math.ceil(area.maxX)-1)>>4;x++)for(int z=(int)Math.floor(area.minZ)>>4;z<=((int)Math.ceil(area.maxZ)-1)>>4;z++)if(!l.hasChunk(x,z)||!l.areEntitiesLoaded(ChunkPos.asLong(x,z)))return false;
        return l.players().stream().noneMatch(p->area.inflate(24).intersects(p.getCamera().getBoundingBox()))&&l.getEntitiesOfClass(LivingEntity.class,area,LivingEntity::isAlive).isEmpty();
    }
    /** One small unit per call. Edited panes, shelf destinations and all block entities are retained. */
    public static boolean repair(ServerLevel l,BlockPos b){
        var d=LabyrinthData.get(l.getServer());var state=d.state(STATE);String key=Long.toString(b.asLong());int next=state.getInt(key);if(next>WINDOWS.size())return true;
        if(next==0){
            var area=new AABB(b.offset(-12,0,-27)).expandTowards(1,3,2);if(!safe(l,area))return false;
            boolean move=true;for(int y=0;y<3;y++){var from=b.offset(-12,y,-27);var to=from.south();var s=l.getBlockState(from);move&=s.is(NovelRegistry.WARD_FIXTURE.get())&&s.getValue(InstituteFixtureBlock.KIND)==InstituteFixtureBlock.Kind.SHELF&&s.getValue(InstituteFixtureBlock.FACING)==Direction.NORTH&&l.isEmptyBlock(to)&&l.getBlockEntity(from)==null&&l.getBlockEntity(to)==null;}
            if(move)for(int y=0;y<3;y++){var from=b.offset(-12,y,-27);l.setBlock(from.south(),l.getBlockState(from),F);l.setBlock(from,Blocks.AIR.defaultBlockState(),F);}
        }else{
            var w=WINDOWS.get(next-1);var at=b.offset(w.lower);if(!safe(l,new AABB(at).expandTowards(0,1,0)))return false;
            for(int y=0;y<2;y++){var cell=at.above(y);var s=l.getBlockState(cell);if(l.getBlockEntity(cell)!=null||!s.is(NovelRegistry.WARD_GLASS.get())&&!s.is(Blocks.WHITE_STAINED_GLASS_PANE))continue;
                if(!s.getValue(IronBarsBlock.NORTH)&&!s.getValue(IronBarsBlock.SOUTH)&&!s.getValue(IronBarsBlock.EAST)&&!s.getValue(IronBarsBlock.WEST))l.setBlock(cell,pane(s,w.side),F);
            }
        }
        state.putInt(key,next+1);d.setState(STATE,state);return next>=WINDOWS.size();
    }
}
