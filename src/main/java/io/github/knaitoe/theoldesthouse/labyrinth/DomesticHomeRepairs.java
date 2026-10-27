package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

/** Repair authored fittings and join the original snippet without replacing edited rooms. */
public final class DomesticHomeRepairs {
    public static final String STATE="domestic_home_routes_0129";
    private static final Map<String,List<DomesticLives.Edit>> BEFORE=new HashMap<>();
    private DomesticHomeRepairs(){}
    public static List<DomesticLives.Edit> legacyPlan(DomesticLives.Home home){return BEFORE.computeIfAbsent(home.id(),id->{
        var old=DomesticLives0128.HOMES.stream().filter(h->h.id().equals(id)).findFirst().orElseThrow();
        return DomesticLives0128.plan(old).stream().map(e->new DomesticLives.Edit(e.at(),e.next())).toList();
    });}
    public static void complete(ServerLevel l,BlockPos b,DomesticLives.Home h){var d=LabyrinthData.get(l.getServer());var state=d.state(STATE);state.putBoolean(DomesticLives.key(b,h),true);d.setState(STATE,state);}
    private static boolean fabric(BlockState s){return s.isAir()||s.is(Blocks.WHITE_TERRACOTTA)||s.is(Blocks.LIGHT_GRAY_TERRACOTTA)||s.is(Blocks.YELLOW_TERRACOTTA)||s.is(Blocks.OAK_PLANKS)||s.is(Blocks.BIRCH_PLANKS)||s.is(Blocks.SPRUCE_PLANKS)||s.is(Blocks.STONE);}
    /** True only when a bounded slice actually ran, so another loaded home is not starved. */
    public static boolean step(ServerLevel l,BlockPos b,DomesticLives.Home home){
        var d=LabyrinthData.get(l.getServer());var state=d.state(STATE);String key=DomesticLives.key(b,home);
        if(state.getBoolean(key)||!d.state(DomesticLives.STATE).getBoolean(key))return false;
        var area=DomesticLives.workArea(b,home);
        for(int x=(int)Math.floor(area.minX)>>4;x<=((int)Math.ceil(area.maxX)-1)>>4;x++)for(int z=(int)Math.floor(area.minZ)>>4;z<=((int)Math.ceil(area.maxZ)-1)>>4;z++)if(!l.hasChunk(x,z)||!l.areEntitiesLoaded(ChunkPos.asLong(x,z)))return false;
        if(l.players().stream().anyMatch(p->area.inflate(16).intersects(p.getCamera().getBoundingBox()))||!l.getEntitiesOfClass(LivingEntity.class,area,e->e.isAlive()).isEmpty())return false;
        var old=new HashMap<BlockPos,BlockState>();for(var e:legacyPlan(home))old.put(e.at(),e.next());
        var retained=DomesticLives0133.HOMES.stream().filter(h->h.id().equals(home.id())).findFirst().orElseThrow();var plan=DomesticLives0133.plan(retained);int cursor=state.getInt(key+":cursor"),visits=0;long deadline=System.nanoTime()+3_000_000;
        while(cursor<plan.size()&&visits++<256&&(visits==1||System.nanoTime()<deadline)){
            var e=plan.get(cursor++);var expected=old.get(e.at());if(e.next().equals(expected))continue;
            var at=b.offset(e.at());if(l.getBlockEntity(at)!=null)continue;var current=l.getBlockState(at);var next=e.next();
            boolean same=current.equals(expected);
            if(expected!=null&&expected.is(UncannyRegistry.FURNITURE.get())&&current.is(expected.getBlock())&&current.getValue(UncannyFurnitureBlock.KIND)==expected.getValue(UncannyFurnitureBlock.KIND)&&current.getValue(UncannyFurnitureBlock.FACING)==expected.getValue(UncannyFurnitureBlock.FACING)&&next.is(current.getBlock())&&next.getValue(UncannyFurnitureBlock.KIND)==current.getValue(UncannyFurnitureBlock.KIND)){
                same=true;next=next.setValue(UncannyFurnitureBlock.OPEN,current.getValue(UncannyFurnitureBlock.OPEN));
            }
            if(same||expected==null&&fabric(current))l.setBlock(at,next,Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);
        }
        state.putInt(key+":cursor",cursor);if(cursor>=plan.size())state.putBoolean(key,true);d.setState(STATE,state);return true;
    }
}
