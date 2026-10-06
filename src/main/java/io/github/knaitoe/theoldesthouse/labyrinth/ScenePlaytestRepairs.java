package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Small saved repairs; reads and writes are both limited to 4096 cells / six milliseconds a tick. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class ScenePlaytestRepairs {
    public static final String STATE="scene_playtest_0449";
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private static Work work;
    private static int nextPlace;
    private ScenePlaytestRepairs(){}
    public static void forget(MinecraftServer server,BlockPos origin,LabyrinthPlace place){
        var data=LabyrinthData.get(server);var state=data.state(STATE);state.remove(origin.asLong()+":"+place.id());data.setState(STATE,state);
        if(work!=null&&work.server==server&&work.origin.equals(origin)&&work.place==place){work.close();work=null;}
    }
    @SubscribeEvent public static void stopped(net.neoforged.neoforge.event.server.ServerStoppingEvent event){
        if(work!=null&&work.server==event.getServer()){work.close();work=null;}nextPlace=0;
    }
    static boolean applies(LabyrinthPlace p){return p!=LabyrinthPlace.FAMILY_COPY&&p!=LabyrinthPlace.OLD_CABIN
        &&(p.isVignette()||NovelRooms.outside(p)||p==LabyrinthPlace.FLOODED_PASSAGE);}
    public static BlockState connected(ServerLevel level,BlockPos at){
        var s=level.getBlockState(at);
        if(!(s.getBlock() instanceof IronBarsBlock)||s.is(Blocks.IRON_BARS))return s;
        for(var d:Direction.Plane.HORIZONTAL)s=s.updateShape(d,level.getBlockState(at.relative(d)),level,at,at.relative(d));
        return s;
    }
    /** Same full-block collision and native return geometry; the outdoor arrival is a timber annex. */
    public static BlockState arrivalSkin(BlockPos rel,BlockState state){
        if(!state.is(Blocks.WHITE_TERRACOTTA)||!LabyrinthPlaces.localVestibule().isInside(rel))return state;
        boolean lining=rel.getZ()>=2&&Math.abs(rel.getX())<=2&&rel.getY()>=-1&&rel.getY()<=4;
        return lining?state:rel.getY()<0?Blocks.STONE_BRICKS.defaultBlockState():LiteraryRegistry.SIDING.get().defaultBlockState();
    }
    /** The dry vestibule is retained; the actual swimming channel has headroom. */
    public static void flooded(ServerLevel level,BlockPos base){
        for(var feet:LabyrinthHazards.floodRoute(base)){
            var head=feet.above();if(BuildBlocks.state(level,head).is(Blocks.LIGHT_GRAY_TERRACOTTA)||BuildBlocks.state(level,head).is(Blocks.SMOOTH_STONE)||BuildBlocks.state(level,head).is(Blocks.WHITE_TERRACOTTA))
                BuildBlocks.set(level,head,Blocks.WATER.defaultBlockState(),F);
        }
        for(int x=0;x<=1;x++){
            var sill=base.offset(x,0,-2);var old=BuildBlocks.state(level,sill);
            if(old.isAir()||old.is(Blocks.WATER))BuildBlocks.set(level,sill,Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH),F);
        }
    }
    private static boolean originalFloor(BlockState s){return s.is(Blocks.STONE_BRICKS)||s.is(Blocks.BIRCH_PLANKS)||s.is(Blocks.SPRUCE_PLANKS);}
    /** Contains no actor construction, paper, inventory, reward or personal progress. */
    public static void cabin(ServerLevel level,BlockPos b){
        for(int x=-12;x<=12;x++)for(int z=-35;z<=-9;z++){
            if(x==0&&z==-17)continue; // The foundation used to seal this ladder hatch.
            var at=b.offset(x,-1,z);if(level.getBlockEntity(at)==null&&BuildBlocks.state(level,at).is(Blocks.STONE_BRICKS))
                BuildBlocks.set(level,at,(Math.abs(x)>=11||z==-35||z==-9?Blocks.DARK_OAK_PLANKS:Blocks.BIRCH_PLANKS).defaultBlockState(),F);
        }
        for(int y:new int[]{-2,-1}){
            var at=b.offset(0,y,-17);if(BuildBlocks.state(level,at).is(Blocks.STONE_BRICKS))
                BuildBlocks.set(level,at,Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.SOUTH),F);
        }
        for(int x=-12;x<=12;x++)if(BuildBlocks.state(level,b.offset(x,4,-24)).isAir())BuildBlocks.set(level,b.offset(x,4,-24),Blocks.CALCITE.defaultBlockState(),F);
        for(int x:new int[]{0,7})if(BuildBlocks.state(level,b.offset(x,2,-24)).isAir())BuildBlocks.set(level,b.offset(x,2,-24),Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(),F);
        rug(level,b,-10,-5,-22,-17);rug(level,b,3,8,-16,-11);
        piece(level,b,-11,-12,HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.EAST,SceneDetailBlock.Kind.DISH_RACK);
        piece(level,b,-10,-12,HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.EAST,SceneDetailBlock.Kind.CROCK);
        piece(level,b,5,-13,HouseholdFurnitureBlock.Kind.FORMICA_TABLE,Direction.SOUTH,SceneDetailBlock.Kind.TEA_SET);
        piece(level,b,5,-11,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.NORTH,null);
        piece(level,b,7,-13,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.WEST,null);
    }
    private static void rug(ServerLevel l,BlockPos b,int x0,int x1,int z0,int z1){
        for(int x=x0;x<=x1;x++)for(int z=z0;z<=z1;z++){
            var at=b.offset(x,-1,z);if(!originalFloor(BuildBlocks.state(l,at))||l.getBlockEntity(at)!=null)continue;
            String ns=z==z0?"NORTH":z==z1?"SOUTH":"",ew=x==x0?"WEST":x==x1?"EAST":"";
            var rim=RugFloorBlock.Rim.valueOf(ns.isEmpty()?(ew.isEmpty()?"CENTER":ew):ns+(ew.isEmpty()?"":"_"+ew));
            BuildBlocks.set(l,at,HouseBlocks.RUG_FLOOR.get().defaultBlockState().setValue(RugFloorBlock.TONE,RugFloorBlock.Tone.BROWN).setValue(RugFloorBlock.RIM,rim),F);
        }
    }
    private static void piece(ServerLevel l,BlockPos b,int x,int z,HouseholdFurnitureBlock.Kind kind,Direction facing,SceneDetailBlock.Kind top){
        var at=b.offset(x,0,z);if(!BuildBlocks.state(l,at).isAir()||!BuildBlocks.state(l,at.below()).isCollisionShapeFullBlock(l,at.below()))return;
        BuildBlocks.set(l,at,HouseBlocks.HOUSEHOLD_FURNITURE.get().defaultBlockState().setValue(HouseholdFurnitureBlock.KIND,kind).setValue(HouseholdFurnitureBlock.FACING,facing),F);
        if(top!=null&&BuildBlocks.state(l,at.above()).isAir())BuildBlocks.set(l,at.above(),HouseBlocks.SCENE_DETAIL.get().defaultBlockState().setValue(SceneDetailBlock.KIND,top).setValue(SceneDetailBlock.FACING,facing),F);
    }
    public static BuildBlocks.Plan plan(ServerLevel l,BlockPos b,LabyrinthPlace p){return BuildBlocks.record(l,()->{
        if(p==LabyrinthPlace.MAPPING_INTERIOR)cabin(l,b);if(p==LabyrinthPlace.FLOODED_PASSAGE)flooded(l,b);
        if(NovelRooms.outside(p))arrivalGround(l,b);
    });}
    public static void arrivalGround(ServerLevel l,BlockPos b){
        var near=BuildBlocks.state(l,b.offset(10,-1,-1));var surface=near.is(Blocks.SNOW_BLOCK)?Blocks.SNOW_BLOCK:near.is(Blocks.SAND)?Blocks.SAND:near.is(Blocks.PODZOL)?Blocks.PODZOL:Blocks.GRASS_BLOCK;
        for(int x=-10;x<=10;x++)for(int z=0;z<=18;z++)for(int y=-3;y<=-1;y++){
            var at=b.offset(x,y,z);if(BuildBlocks.state(l,at).isAir())BuildBlocks.set(l,at,(y==-1?surface:Blocks.DIRT).defaultBlockState(),F);
        }
    }
    /** Never delete authoring inside a story or the copied arrival vestibule. */
    public static boolean strayFill(LabyrinthPlace place,BlockPos rel,BlockState state){
        if(!NovelRooms.outside(place)||!state.is(Blocks.WHITE_TERRACOTTA)||rel.getY()<0)return false;
        if(LabyrinthPlaces.localVestibule().isInside(rel))return false;
        var r=place.room();return rel.getX()<r.minX()||rel.getX()>r.maxX()||rel.getZ()<r.minZ()||rel.getZ()>r.maxZ()||rel.getY()>r.maxY()+4;
    }
    private static final class Work {
        final MinecraftServer server;final ServerLevel level;final BlockPos origin,base;final LabyrinthPlace place;final String key;final BoundingBox box;final AABB area;
        final Set<ChunkPos> tickets=new HashSet<>();BuildBlocks.Plan plan;int cursor;boolean prepared,deferred;
        Work(MinecraftServer s,BlockPos o,LabyrinthPlace p){server=s;level=s.getLevel(NovelRooms.dimension(p));origin=o;base=LabyrinthPlaces.base(o,p);place=p;key=o.asLong()+":"+p.id();
            var r=p.room();int margin=NovelRooms.outside(p)?8:1;
            box=new BoundingBox(r.minX()-margin,Math.max(-12,r.minY()),r.minZ()-margin,r.maxX()+margin,NovelRooms.outside(p)?96:r.maxY()+1,Math.max(r.maxZ()+margin,20));
            area=new AABB(base.offset(box.minX(),box.minY(),box.minZ())).minmax(new AABB(base.offset(box.maxX(),box.maxY(),box.maxZ())));
            cursor=LabyrinthData.get(s).stateEntry(STATE,key).getInt("Cursor");
        }
        boolean visible(){for(var p:level.players())if(area.inflate(16).intersects(p.getCamera().getBoundingBox()))return true;return false;}
        boolean loaded(){boolean ready=true;
            for(int x=(int)Math.floor(area.minX)>>4;x<=((int)Math.ceil(area.maxX)-1)>>4;x++)for(int z=(int)Math.floor(area.minZ)>>4;z<=((int)Math.ceil(area.maxZ)-1)>>4;z++){
                var c=new ChunkPos(x,z);tickets.add(c);level.getChunkSource().addRegionTicket(TicketType.PORTAL,c,3,base);
                ready&=level.isLoaded(new BlockPos(x<<4,base.getY(),z<<4))&&level.areEntitiesLoaded(c.toLong());
            }return ready;
        }
        void close(){for(var c:tickets)level.getChunkSource().removeRegionTicket(TicketType.PORTAL,c,3,base);tickets.clear();}
        boolean safe(BlockPos at,BlockState next,List<LivingEntity> bodies){
            var old=level.getBlockState(at);if(old.isCollisionShapeFullBlock(level,at)&&next.isCollisionShapeFullBlock(level,at))return true;
            var affected=new AABB(at);if(next.isAir())affected=affected.expandTowards(0,1,0);
            for(var body:bodies)if(affected.intersects(body.getBoundingBox()))return false;return true;
        }
        boolean step(){
            if(visible()){close();return false;}if(!loaded())return false;
            var entry=LabyrinthData.get(server).door(place.entryDoorId());
            if(entry==null||!(level.getBlockState(entry.lower).getBlock() instanceof DoorBlock)){close();return true;}
            var bodies=level.getEntitiesOfClass(LivingEntity.class,area,LivingEntity::isAlive);
            // A Stay pet over the hatch prevents opening it; existing actors retain their identity and position.
            if(!prepared||plan!=null){
                if(NovelRooms.outside(place)&&bodies.stream().anyMatch(e->new AABB(base.getX()-10,base.getY()-3,base.getZ(),base.getX()+11,base.getY(),base.getZ()+19).intersects(e.getBoundingBox()))){close();return true;}
                if(place==LabyrinthPlace.MAPPING_INTERIOR&&bodies.stream().anyMatch(e->new AABB(base.offset(0,-2,-17)).expandTowards(0,3,0).intersects(e.getBoundingBox()))){close();return true;}
                if(place==LabyrinthPlace.MAPPING_INTERIOR)for(var rel:List.of(new BlockPos(-11,0,-12),new BlockPos(-10,0,-12),new BlockPos(5,0,-13),new BlockPos(5,0,-11),new BlockPos(7,0,-13)))
                    if(bodies.stream().anyMatch(e->new AABB(base.offset(rel)).expandTowards(0,1,0).intersects(e.getBoundingBox()))){close();return true;}
                if(place==LabyrinthPlace.FLOODED_PASSAGE&&bodies.stream().anyMatch(e->new AABB(base.offset(0,0,-2)).expandTowards(1,2,0).intersects(e.getBoundingBox()))){close();return true;}
                if(!prepared){plan=plan(level,base,place);prepared=true;}}
            // Construction and the large read sweep get separate ticks, each with its own bounded slice.
            if(plan!=null){if(plan.tick())plan=null;return false;}
            int width=box.getXSpan(),depth=box.getZSpan(),total=width*depth*box.getYSpan(),visited=0;long started=System.nanoTime();
            while(cursor<total&&visited++<4096&&System.nanoTime()-started<6_000_000L){
                int n=cursor++;var rel=new BlockPos(box.minX()+n%width,box.minY()+n/(width*depth),box.minZ()+(n/width)%depth);var at=base.offset(rel);
                var before=level.getBlockState(at);var next=connected(level,at);
                if(NovelRooms.outside(place))next=arrivalSkin(rel,next);
                if(strayFill(place,rel,before))next=Blocks.AIR.defaultBlockState();
                if(!next.equals(before)&&level.getBlockEntity(at)==null){if(safe(at,next,bodies))level.setBlock(at,next,F);else deferred=true;}
            }
            var own=new CompoundTag();boolean done=cursor>=total&&!deferred;own.putInt("Cursor",cursor>=total?0:cursor);own.putBoolean("Done",done);
            LabyrinthData.get(server).setStateEntry(STATE,key,own);
            if(cursor>=total){close();return true;}return false;
        }
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post event){
        var s=event.getServer();var origin=HouseSavedData.get(s).houseOrigin();if(s instanceof net.minecraft.gametest.framework.GameTestServer||origin==null)return;
        if(work!=null&&(work.server!=s||!work.origin.equals(origin))){work.close();work=null;}
        if(LabyrinthBuilder.isCarving())return;
        if(work!=null){if(work.step())work=null;return;}
        if(s.getTickCount()%40!=31)return;var data=LabyrinthData.get(s);
        var places=LabyrinthPlace.values();for(int i=0;i<places.length;i++){int index=(nextPlace+i)%places.length;var p=places[index];if(applies(p)&&data.door(p.entryDoorId())!=null&&LabyrinthBuilder.isPlaceReady(data,p)&&s.getLevel(NovelRooms.dimension(p))!=null
            &&!data.stateEntry(STATE,origin.asLong()+":"+p.id()).getBoolean("Done")){
            var candidate=new Work(s,origin,p);if(!candidate.visible()){work=candidate;nextPlace=(index+1)%places.length;return;}
        }}
    }
}
