package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** Once-only, recognized-cell repairs for the tunnel and foyer, including their old dynamic false frame. */
public final class UncannyPlaytestRepairs {
    public static final String STATE="uncanny_playtest_shell_0131c";
    private static Work work;
    private static final List<LabyrinthPlace> ROOMS=List.of(LabyrinthPlace.PAINTED_INTERIOR,LabyrinthPlace.MISSING_SLEEP,LabyrinthPlace.TUNNEL_PERIPHERY,LabyrinthPlace.MOTION_LIGHT);
    private static final class Work {
        final ServerLevel l;final BlockPos origin,b;final LabyrinthPlace place;final BuildBlocks.Plan before,after;final int width,depth,total;final boolean filled131;int cursor;
        Work(ServerLevel l,BlockPos origin,LabyrinthPlace p,int cursor){this.l=l;this.origin=origin.immutable();place=p;b=LabyrinthPlaces.base(origin,p);before=BuildBlocks.record(l,()->UncannyRooms0129.build(l,b,p));after=BuildBlocks.record(l,()->{if(p==LabyrinthPlace.TUNNEL_PERIPHERY)UncannyRooms0132.build(l,b,p);else UncannyRooms.build(l,b,p);});var r=p.room();width=r.maxX()-r.minX()+1;depth=r.maxZ()-r.minZ()+1;total=width*depth*(r.maxY()-r.minY()+1);this.cursor=cursor;
            var data=LabyrinthData.get(l.getServer());String k=key(origin,p);boolean prior=data.state("uncanny_playtest_shell_0131").getBoolean(k)||data.state("uncanny_playtest_shell_0131b").getBoolean(k);
            // Fresh 0.4.131 rooms could be entered before the vacant repair checkpoint ran.
            // Recognize the whole erroneous fill pattern, rather than treating a lone dirt edit as authored.
            boolean solid=p==LabyrinthPlace.PAINTED_INTERIOR;for(var sample:List.of(new BlockPos(-5,1,-12),new BlockPos(5,1,-12),new BlockPos(0,2,-10),new BlockPos(-5,2,-24),new BlockPos(5,2,-24),new BlockPos(0,3,-20),new BlockPos(10,1,-12)))solid&=l.getBlockState(b.offset(sample)).is(Blocks.DIRT);
            filled131=p==LabyrinthPlace.PAINTED_INTERIOR&&(prior||solid);
        }
    }
    private UncannyPlaytestRepairs(){}
    private static String key(BlockPos o,LabyrinthPlace p){return o.asLong()+":"+p.id();}
    private static boolean resident(ServerLevel l,AABB a){for(int x=(int)Math.floor(a.minX)>>4;x<=((int)Math.ceil(a.maxX)-1)>>4;x++)for(int z=(int)Math.floor(a.minZ)>>4;z<=((int)Math.ceil(a.maxZ)-1)>>4;z++)if(!l.hasChunk(x,z)||!l.areEntitiesLoaded(ChunkPos.asLong(x,z)))return false;return true;}
    public static void tick(MinecraftServer s){
        if(s instanceof net.minecraft.gametest.framework.GameTestServer||LabyrinthBuilder.isCarving())return;var origin=HouseSavedData.get(s).houseOrigin();if(origin==null){work=null;return;}var d=LabyrinthData.get(s);if(!origin.equals(d.builtOrigin()))return;
        if(work!=null){if(work.l.getServer()!=s||!work.origin.equals(origin)){work=null;return;}step(work.l,origin,work.place);return;}
        if(s.getTickCount()%20!=0)return;for(var p:ROOMS)if(d.door(p.entryDoorId())!=null&&!d.state(STATE).getBoolean(key(origin,p))){var l=s.getLevel(NovelRooms.dimension(p));if(l!=null&&step(l,origin,p))return;}
    }
    public static boolean step(ServerLevel l,BlockPos origin,LabyrinthPlace p){
        var d=LabyrinthData.get(l.getServer());var state=d.state(STATE);String key=key(origin,p);if(state.getBoolean(key))return false;
        var area=IndianLakeRooms.bounds(LabyrinthPlaces.base(origin,p),p);if(!resident(l,area)||l.players().stream().anyMatch(a->area.inflate(12).intersects(a.getCamera().getBoundingBox())))return false;
        if(work==null)work=new Work(l,origin,p,state.getInt(key+":cursor"));if(work.l!=l||work.place!=p||!work.origin.equals(origin))return false;
        var bodies=l.getEntities((Entity)null,area,Entity::isAlive);var r=p.room();int visits=0;long end=System.nanoTime()+3_000_000;
        while(work.cursor<work.total&&visits++<256&&(visits==1||System.nanoTime()<end)){
            int n=work.cursor++;var rel=new BlockPos(r.minX()+n%work.width,r.minY()+n/(work.width*work.depth),r.minZ()+n/work.width%work.depth);var at=work.b.offset(rel);var old=work.before.plannedState(at);var next=work.after.plannedState(at);var actual=l.getBlockState(at);
            boolean mainAir=rel.getX()>=-7&&rel.getX()<=7&&rel.getZ()>=-27&&rel.getZ()<=-8&&rel.getY()>=0&&rel.getY()<=4;
            boolean bayAir=rel.getX()>=9&&rel.getX()<=11&&rel.getZ()>=-13&&rel.getZ()<=-9&&rel.getY()>=0&&rel.getY()<=2;
            if(work.filled131&&(mainAir||bayAir)&&next!=null&&next.isAir()&&actual.is(Blocks.DIRT))old=actual;
            boolean retired=p==LabyrinthPlace.MOTION_LIGHT&&rel.getX()==0&&rel.getZ()==-2&&rel.getY()>=0&&rel.getY()<=1&&actual.is(UncannyRegistry.PROP.get())&&actual.getValue(UncannyPropBlock.KIND)==UncannyPropBlock.Kind.FALSE_DOOR;
            boolean oldLamp=p==LabyrinthPlace.TUNNEL_PERIPHERY&&rel.getX()==0&&rel.getY()==2&&java.util.Set.of(-6,-20,-36,-52).contains(rel.getZ())&&actual.is(UncannyRegistry.FURNITURE.get())&&actual.getValue(UncannyFurnitureBlock.KIND)==UncannyFurnitureBlock.Kind.FLUORESCENT;
            if(oldLamp){state.putBoolean(key+":lamp:"+rel.getZ(),actual.getValue(UncannyFurnitureBlock.OPEN));next=Blocks.AIR.defaultBlockState();retired=true;}
            if(p==LabyrinthPlace.TUNNEL_PERIPHERY&&rel.getX()==0&&rel.getY()==3&&java.util.Set.of(-6,-20,-36,-52).contains(rel.getZ())&&next!=null&&next.is(UncannyRegistry.FURNITURE.get())&&next.getValue(UncannyFurnitureBlock.KIND)==UncannyFurnitureBlock.Kind.FLUSH_LIGHT){
                next=next.setValue(UncannyFurnitureBlock.OPEN,state.getBoolean(key+":lamp:"+rel.getZ()));
                if(actual.is(UncannyRegistry.material("workshop_wall"))||actual.is(Blocks.SMOOTH_STONE))old=actual;
            }
            if(retired)next=Blocks.AIR.defaultBlockState();
            if(next==null||actual.equals(next)||!retired&&(old==null||!actual.equals(old)))continue;
            if(bodies.stream().anyMatch(a->a.getBoundingBox().intersects(new AABB(at))))continue;
            var be=l.getBlockEntity(at);if(be!=null&&!(be instanceof UncannyPropBlock.Fitting))continue;
            // Real door state, stored papers and authored used controls are never rebuilt.
            if(actual.getBlock() instanceof DoorBlock&&!(p==LabyrinthPlace.PAINTED_INTERIOR&&rel.getX()==0&&rel.getZ()==-15&&next.is(UncannyRegistry.PROP.get())&&next.getValue(UncannyPropBlock.KIND)==UncannyPropBlock.Kind.SUN_BOARD))continue;
            l.setBlock(at,next,UncannyRooms.FLAGS);
        }
        state.putInt(key+":cursor",work.cursor);if(work.cursor>=work.total){state.putBoolean(key,true);work=null;}d.setState(STATE,state);return true;
    }
}
