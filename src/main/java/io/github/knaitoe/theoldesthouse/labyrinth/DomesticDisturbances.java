package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import io.github.knaitoe.theoldesthouse.house.*;

/** A rare variation of the actual room reached by a route, never a second permanently overturned template. */
public final class DomesticDisturbances {
    public static final String STATE="domestic_disturbances134";
    private record Request(ServerLevel level,BlockPos base,LabyrinthPlace place,long route,int depth,int until){}
    private static final Map<UUID,Request> PENDING=new HashMap<>();
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private static final List<HouseholdFurnitureBlock.Kind> CHAIRS=List.of(HouseholdFurnitureBlock.Kind.CANE_CHAIR,HouseholdFurnitureBlock.Kind.KITCHEN_STOOL,HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,HouseholdFurnitureBlock.Kind.FLORAL_ARMCHAIR,HouseholdFurnitureBlock.Kind.BLUE_SOFA,HouseholdFurnitureBlock.Kind.FOOTSTOOL);
    public record Candidate(BlockPos at,BlockState normal){public BlockState tipped(){var k=normal.getValue(HouseholdFurnitureBlock.KIND);return DomesticFittingBlock.of(k==HouseholdFurnitureBlock.Kind.FORMICA_TABLE?DomesticFittingBlock.Kind.TOPPLED_TABLE:DomesticFittingBlock.Kind.TOPPLED_CHAIR,normal.getValue(HouseholdFurnitureBlock.FACING)).setValue(DomesticFittingBlock.STAGE,k==HouseholdFurnitureBlock.Kind.FORMICA_TABLE?0:CHAIRS.indexOf(k));}}
    private DomesticDisturbances(){}
    public static double probability(int depth){return Math.min(.14,.02+Math.max(0,depth)*.0045);}
    public static boolean chosen(long route,int depth){long x=route+0x9e3779b97f4a7c15L;x=(x^(x>>>30))*0xbf58476d1ce4e5b9L;x=(x^(x>>>27))*0x94d049bb133111ebL;x^=x>>>31;return (x>>>11)*0x1.0p-53<probability(depth);}
    private static boolean supported(BlockState s){return s.is(HouseBlocks.HOUSEHOLD_FURNITURE.get())&&(CHAIRS.contains(s.getValue(HouseholdFurnitureBlock.KIND))||s.getValue(HouseholdFurnitureBlock.KIND)==HouseholdFurnitureBlock.Kind.FORMICA_TABLE);}
    public static List<Candidate> candidates(ServerLevel l,BlockPos b,LabyrinthPlace place){
        var out=new LinkedHashMap<BlockPos,Candidate>();
        for(var h:DomesticLives.HOMES)if(h.hall()==place&&DomesticLives.selected(l,b,h))for(var e:DomesticLives.plan(h))if(supported(e.next()))out.put(e.at(),new Candidate(e.at(),e.next()));
        for(var f:LabyrinthDomestic.fragments(place))if(f.room()==LabyrinthDomestic.Room.DINING){
            int x=(f.x0()+f.x1())/2,z=(f.z0()+f.z1())/2;var back=switch(f.entrance()){case EAST->new BlockPos(f.x0(),0,z);case WEST->new BlockPos(f.x1(),0,z);case NORTH->new BlockPos(x,0,f.z1());default->new BlockPos(x,0,f.z0());};
            var into=f.entrance().getOpposite();var along=into.getClockWise();
            out.put(back,new Candidate(back,HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.FORMICA_TABLE,f.entrance())));
            var at=back.relative(along);out.put(at,new Candidate(at,HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.CANE_CHAIR,along.getOpposite())));
            at=back.relative(along.getOpposite());out.put(at,new Candidate(at,HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.KITCHEN_STOOL,along)));
        }
        if(place==LabyrinthPlace.QUIET_ROOM)for(int i=0;i<4;i++){var at=new BlockPos(-4,0,-8-i);out.put(at,new Candidate(at,HouseholdFurnitureBlock.state(CHAIRS.get(i+2),Direction.EAST)));}
        return List.copyOf(out.values());
    }
    private static boolean safe(ServerLevel l,BlockPos b,LabyrinthPlace place){
        var area=IndianLakeRooms.bounds(b,place);
        for(int x=(int)Math.floor(area.minX)>>4;x<=((int)Math.ceil(area.maxX)-1)>>4;x++)for(int z=(int)Math.floor(area.minZ)>>4;z<=((int)Math.ceil(area.maxZ)-1)>>4;z++)if(!l.hasChunk(x,z)||!l.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(x,z)))return false;
        return l.players().stream().noneMatch(p->area.inflate(12).intersects(p.getCamera().getBoundingBox()))&&l.getEntitiesOfClass(LivingEntity.class,area,LivingEntity::isAlive).isEmpty();
    }
    private static boolean resident(ServerLevel l,BlockPos b,LabyrinthPlace place){var a=IndianLakeRooms.bounds(b,place);for(int x=(int)Math.floor(a.minX)>>4;x<=((int)Math.ceil(a.maxX)-1)>>4;x++)for(int z=(int)Math.floor(a.minZ)>>4;z<=((int)Math.ceil(a.maxZ)-1)>>4;z++)if(!l.hasChunk(x,z)||!l.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(x,z)))return false;return true;}
    private static boolean hidden(ServerLevel l,BlockPos at){return l.players().stream().noneMatch(p->p.getCamera().position().distanceToSqr(at.getCenter())<16*16||HouseWatchers.sees(p,at.getCenter()))&&l.getEntitiesOfClass(LivingEntity.class,new AABB(at).inflate(.15),LivingEntity::isAlive).isEmpty();}
    /** Removes only the six exact old inert additions, once and while the hall is vacant. */
    public static void apply(ServerLevel l,BlockPos b,LabyrinthPlace place){
        if(BuildBlocks.isRecording(l)||!LabyrinthHalls.isHall(place))return;
        var d=LabyrinthData.get(l.getServer());var t=d.state(STATE);String key="legacy:"+place.id()+":"+b.asLong();if(t.getBoolean(key)||!safe(l,b,place))return;
        int[][] cells=switch(place){case STRAIGHT_HALL->new int[][]{{4,-29,0,1},{4,-28,2,1}};case CROSS_HALL->new int[][]{{9,-19,1,3},{8,-19,2,3}};case QUIET_ROOM->new int[][]{{-4,-6,0,2},{-3,-6,2,2}};default->new int[0][];};
        for(var c:cells){var at=b.offset(c[0],0,c[1]);var k=c[2]==0?DomesticFittingBlock.Kind.TOPPLED_CHAIR:c[2]==1?DomesticFittingBlock.Kind.TOPPLED_TABLE:DomesticFittingBlock.Kind.SCRAPES;var facing=new Direction[]{Direction.NORTH,Direction.EAST,Direction.SOUTH,Direction.WEST}[c[3]];
            if(l.getBlockEntity(at)==null&&l.getBlockState(at).equals(DomesticFittingBlock.of(k,facing)))l.setBlock(at,Blocks.AIR.defaultBlockState(),F);
        }
        t.putBoolean(key,true);d.setState(STATE,t);
    }
    public static boolean select(ServerLevel l,BlockPos b,LabyrinthPlace place,long route,int depth){
        if(!LabyrinthHalls.isHall(place)||!resident(l,b,place))return false;apply(l,b,place);
        var d=LabyrinthData.get(l.getServer());String key=place.id()+":"+b.asLong();var t=d.stateEntry(STATE,key);
        if(t.contains("Route")&&t.getLong("Route")==route)return true;
        var options=candidates(l,b,place);
        if(options.stream().anyMatch(c->!hidden(l,b.offset(c.at))))return false;
        if(t.contains("At")){var at=BlockPos.of(t.getLong("At"));for(var c:options)if(c.at.equals(at)){var cell=b.offset(at);if(l.getBlockEntity(cell)==null&&l.getBlockState(cell).equals(c.tipped()))l.setBlock(cell,c.normal,F);break;}}
        t.remove("At");t.putLong("Route",route);t.putInt("Depth",depth);
        if(chosen(route,depth)&&!options.isEmpty()){
            int index=Math.floorMod(Long.hashCode(route^0x48f9379aL),options.size());
            for(int i=0;i<options.size();i++){var c=options.get((index+i)%options.size());var at=b.offset(c.at);
                // A paper on a table and every edited block keep their original support.
                if(l.getBlockEntity(at)!=null||!l.getBlockState(at).equals(c.normal)||!l.isEmptyBlock(at.above()))continue;
                var old=c.normal.getCollisionShape(l,at);var next=c.tipped().getCollisionShape(l,at);boolean clear=true;
                for(var box:next.toAabbs())if(!l.getEntitiesOfClass(LivingEntity.class,box.move(at).inflate(.01),LivingEntity::isAlive).isEmpty())clear=false;
                if(!clear)continue;l.setBlock(at,c.tipped(),F);t.putLong("At",c.at.asLong());break;
            }
        }
        d.setStateEntry(STATE,key,t);return true;
    }
    public static void prepare(ServerPlayer p,ServerLevel l,LabyrinthPlace place,LabyrinthData.Door from){
        if(p.isSpectator()||!LabyrinthHalls.isHall(place)||!l.dimension().equals(HouseDimensions.INTERIOR))return;
        var origin=HouseSavedData.get(p.server).houseOrigin();if(origin==null)return;var d=LabyrinthData.get(p.server);
        long route=d.nodeKey(p.getUUID(),place)^from.lower.asLong()^from.dimension.location().toString().hashCode();
        var b=LabyrinthPlaces.base(origin,place);int depth=d.returnDepth(p.getUUID())+1;
        if(!select(l,b,place,route,depth))PENDING.put(p.getUUID(),new Request(l,b,place,route,depth,p.server.getTickCount()+80));
    }
    public static void tick(MinecraftServer s){
        if(s instanceof net.minecraft.gametest.framework.GameTestServer||LabyrinthBuilder.isCarving())return;
        if(s.getTickCount()%5==0){var it=PENDING.entrySet().iterator();while(it.hasNext()){var e=it.next();var r=e.getValue();if(r.level.getServer()!=s||s.getTickCount()>r.until||s.getPlayerList().getPlayer(e.getKey())==null){it.remove();continue;}if(select(r.level,r.base,r.place,r.route,r.depth))it.remove();}}
        if(s.getTickCount()%40!=0)return;
        var origin=HouseSavedData.get(s).houseOrigin();if(origin==null||!origin.equals(LabyrinthData.get(s).builtOrigin()))return;var l=s.getLevel(HouseDimensions.INTERIOR);if(l==null)return;
        for(var p:List.of(LabyrinthPlace.STRAIGHT_HALL,LabyrinthPlace.BENT_HALL,LabyrinthPlace.CROSS_HALL,LabyrinthPlace.QUIET_ROOM))if(LabyrinthData.get(s).door(p.entryDoorId())!=null)apply(l,LabyrinthPlaces.base(origin,p),p);
    }
}
