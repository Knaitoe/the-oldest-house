package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Bounded, vacant, recognized-cell additions; never rebuild the occupied foyer or its finite originals. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class FoyerRefit {
    public static final String STATE="foyer_routes_04136";
    private record Work(BlockPos b,BuildBlocks.Plan before,BuildBlocks.Plan after){}
    private static final Map<ServerLevel,Work> WORK=new WeakHashMap<>();private static boolean baseline;
    private FoyerRefit(){}
    private static void put(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,UncannyRooms.FLAGS);}
    public static void structure(ServerLevel l,BlockPos b){
        if(baseline)return;
        for(var rel:UncannyRooms.FALSE_DOORS){
            boolean west=rel.getX()<0;int start=west?-8:5,end=west?-5:8;
            for(int x=start;x<=end;x++)for(int y=-1;y<=3;y++)for(int z=rel.getZ()-1;z<=rel.getZ()+1;z++){
                boolean side=z!=rel.getZ(),back=west?x==start:x==end;
                var s=y==-1?Blocks.DARK_OAK_PLANKS.defaultBlockState():back?Blocks.BLACK_WOOL.defaultBlockState():y==3||side?UncannyRegistry.material("lobby_wall").defaultBlockState():Blocks.AIR.defaultBlockState();put(l,b,x,y,z,s);
            }
        }
        // A short enclosed native doorway crosses back to the actual saved House return.
        for(int x=9;x<=10;x++)for(int y=-1;y<=3;y++)for(int z=-6;z<=-4;z++)put(l,b,x,y,z,y==-1?Blocks.DARK_OAK_PLANKS.defaultBlockState():x==10||z!=-5||y==3?UncannyRegistry.material("lobby_wall").defaultBlockState():Blocks.AIR.defaultBlockState());
        NovelRooms.door(l,b.offset(FoyerEncounter.EXIT),Direction.EAST,Blocks.DARK_OAK_DOOR,false);
    }
    public static boolean step(ServerLevel l,BlockPos origin){
        if(BuildBlocks.isRecording(l))return false;var d=LabyrinthData.get(l.getServer());var b=LabyrinthPlaces.base(origin,FoyerEncounter.PLACE);var tag=d.state(STATE);String key=origin.asLong()+":routes";
        if(tag.getBoolean(key))return true;
        var area=IndianLakeRooms.bounds(b,FoyerEncounter.PLACE).inflate(2);
        for(int x=((int)Math.floor(area.minX))>>4;x<=((int)Math.floor(area.maxX))>>4;x++)for(int z=((int)Math.floor(area.minZ))>>4;z<=((int)Math.floor(area.maxZ))>>4;z++)if(!l.hasChunk(x,z)||!l.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(x,z)))return false;
        if(l.players().stream().anyMatch(p->area.contains(p.position())||area.contains(p.getCamera().position())||HouseWatchers.sees(p,b.offset(-5,1,-5).getCenter())||HouseWatchers.sees(p,b.offset(FoyerEncounter.EXIT).getCenter())))return false;
        var work=WORK.get(l);
        if(work==null||!work.b.equals(b)){
            BuildBlocks.Plan before;baseline=true;try{before=BuildBlocks.record(l,()->UncannyRooms.build(l,b,FoyerEncounter.PLACE));}finally{baseline=false;}
            work=new Work(b,before,BuildBlocks.record(l,()->structure(l,b)));WORK.put(l,work);
        }
        int cursor=tag.getInt(key+":cursor"),visits=0;long until=System.nanoTime()+3_000_000;
        // Only the footprint of the new recesses and service doorway is visited.
        var cells=cells();while(cursor<cells.size()&&visits++<128&&(visits==1||System.nanoTime()<until)){
            var at=b.offset(cells.get(cursor));var next=work.after.plannedState(at);var old=work.before.plannedState(at);var actual=l.getBlockState(at);
            if(next!=null&&!next.equals(actual)&&old!=null&&old.equals(actual)&&l.getBlockEntity(at)==null){
                if(!l.getEntitiesOfClass(LivingEntity.class,new AABB(at).inflate(.15),LivingEntity::isAlive).isEmpty())break;
                l.setBlock(at,next,UncannyRooms.FLAGS);
            }cursor++;
        }
        tag.putInt(key+":cursor",cursor);if(cursor==cells.size()){
            // The legacy east frame was drawn parallel to the wrong wall. Rotate only
            // that recognized native frame; retain its actual open stage and block entity.
            for(int y=0;y<2;y++){var frame=b.offset(4,y,-11);var actual=l.getBlockState(frame);if(actual.is(UncannyRegistry.PROP.get())&&actual.getValue(UncannyPropBlock.KIND)==UncannyPropBlock.Kind.FALSE_DOOR&&actual.getValue(UncannyPropBlock.FACING)==Direction.SOUTH)l.setBlock(frame,actual.setValue(UncannyPropBlock.FACING,Direction.EAST),UncannyRooms.FLAGS);}
            var at=b.offset(FoyerEncounter.EXIT);if(l.getBlockState(at).getBlock() instanceof DoorBlock)d.putDoor(new LabyrinthData.Door(FoyerEncounter.PLACE.id()+"/service_exit",l.dimension(),at,Direction.EAST,LabyrinthData.RETURN,false));
            tag.putBoolean(key,true);WORK.remove(l);
        }d.setState(STATE,tag);return tag.getBoolean(key);
    }
    private static List<BlockPos> cells(){var out=new LinkedHashSet<BlockPos>();for(var rel:UncannyRooms.FALSE_DOORS)for(var at:BlockPos.betweenClosed(new BlockPos(rel.getX()<0?-8:5,-1,rel.getZ()-1),new BlockPos(rel.getX()<0?-5:8,3,rel.getZ()+1)))out.add(at.immutable());for(var at:BlockPos.betweenClosed(new BlockPos(8,-1,-6),new BlockPos(10,3,-4)))out.add(at.immutable());return List.copyOf(out);}
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){var s=e.getServer();if(s instanceof net.minecraft.gametest.framework.GameTestServer||s.getTickCount()%20!=0||LabyrinthBuilder.isCarving())return;var origin=HouseSavedData.get(s).houseOrigin();var l=s.getLevel(HouseDimensions.INTERIOR);if(origin!=null&&l!=null&&LabyrinthData.get(s).door(FoyerEncounter.PLACE.entryDoorId())!=null)step(l,origin);}
}
