package io.github.knaitoe.theoldesthouse.labyrinth;
import java.util.*;
import javax.annotation.Nullable;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * The cabin television's pictures: a reader's own home seen from high above, or a room of theirs going dark. Nothing
 * is loaded synchronously: the chunks are ticketed, and the 24 by 16 picture is taken once they stand (or, after half a
 * minute, from whatever stands). Pictures are saved with the reader's cabin record.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class CabinScreen {
    public static final int WIDTH=24,HEIGHT=16,PIXELS=WIDTH*HEIGHT;
    /** Saved keys: the home picture, the closed room's picture, and the picture an earlier reader's choice kept. */
    public static final String HOME="HomeScreen",ROOM="RoomScreen",LEGACY="Screen";
    private static final TicketType<String> TICKET=TicketType.create(TheOldestHouse.MOD_ID+"_cabin_screen",String::compareTo);
    private static final int TIMEOUT=600;
    private record Job(UUID reader,String key,@Nullable LabyrinthPlace room,ServerLevel level,BlockPos at,List<ChunkPos> chunks,long started){}
    private static final Map<String,Job> JOBS=new LinkedHashMap<>();
    private CabinScreen(){}

    public static boolean pending(UUID reader,String key){return JOBS.containsKey(reader+"/"+key);}
    /** The reader's home: their own bed in the Overworld, or where the world first put them. */
    public static BlockPos home(ServerPlayer p){var bed=p.getRespawnPosition();return bed!=null&&p.getRespawnDimension().equals(Level.OVERWORLD)?bed:p.server.overworld().getSharedSpawnPos();}
    public static void requestHome(ServerPlayer p){request(p.server,p.getUUID(),HOME,null,p.server.overworld(),home(p));}
    /** False when the room has nowhere to be pictured; the caller keeps a dark screen instead. */
    public static boolean requestRoom(ServerPlayer p,LabyrinthPlace room,String key){
        var origin=HouseSavedData.get(p.server).houseOrigin();var level=p.server.getLevel(NovelRooms.dimension(room));var b=origin==null?null:LabyrinthPlaces.base(origin,room);
        if(level==null||b==null)return false;
        request(p.server,p.getUUID(),key,room,level,b);return true;
    }
    private static void request(MinecraftServer server,UUID reader,String key,@Nullable LabyrinthPlace room,ServerLevel level,BlockPos at){
        String id=reader+"/"+key;if(JOBS.containsKey(id))return;var chunks=new ArrayList<ChunkPos>();
        int minX,maxX,minZ,maxZ;
        if(room==null){minX=at.getX()-25;maxX=at.getX()+24;minZ=at.getZ()-17;maxZ=at.getZ()+16;}
        else{var box=IndianLakeRooms.bounds(at,room);minX=(int)Math.floor(box.minX);maxX=(int)Math.ceil(box.maxX);minZ=(int)Math.floor(box.minZ);maxZ=(int)Math.ceil(box.maxZ);}
        for(int x=minX>>4;x<=maxX>>4;x++)for(int z=minZ>>4;z<=maxZ>>4;z++){var c=new ChunkPos(x,z);level.getChunkSource().addRegionTicket(TICKET,c,1,id);chunks.add(c);}
        JOBS.put(id,new Job(reader,key,room,level,at.immutable(),chunks,server.getTickCount()));
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();if(JOBS.isEmpty()||server.getTickCount()%5!=0)return;
        for(var it=JOBS.entrySet().iterator();it.hasNext();){var entry=it.next();var job=entry.getValue();
            boolean ready=job.chunks.stream().allMatch(c->job.level.getChunkSource().getChunkNow(c.x,c.z)!=null);
            if(!ready&&server.getTickCount()-job.started<TIMEOUT)continue;
            var view=new Loaded(job.level);
            store(server,job.reader,job.key,job.room,job.room==null?map(view,job.at):room(view,job.at));
            release(entry.getKey(),job);it.remove();
        }
    }
    private static void store(MinecraftServer server,UUID reader,String key,@Nullable LabyrinthPlace room,int[] pixels){
        var d=LabyrinthData.get(server);var own=LiteraryVignettes.personal(d,reader,LabyrinthPlace.END_WORLD_CABIN);
        own.putIntArray(key,pixels);if(room!=null)own.putString(key.equals(LEGACY)?"ScreenRoom":key+"Room",room.id());
        LiteraryVignettes.save(d,reader,LabyrinthPlace.END_WORLD_CABIN,own);
    }
    private static void release(String id,Job job){for(var c:job.chunks)job.level.getChunkSource().removeRegionTicket(TICKET,c,1,id);}
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){JOBS.clear();}
    public static int[] blank(){var p=new int[PIXELS];Arrays.fill(p,0x111519);return p;}

    /** From high above, as a map draws it: each column's top, brighter where it rises, with the reader's bed lit. */
    static int[] map(BlockGetter view,BlockPos home){
        int[] pixels=new int[PIXELS];int[] heights=new int[WIDTH*(HEIGHT+1)];
        for(int y=-1;y<HEIGHT;y++)for(int x=0;x<WIDTH;x++){int wx=home.getX()+(x-WIDTH/2)*2,wz=home.getZ()+(y-HEIGHT/2)*2;heights[x+(y+1)*WIDTH]=top(view,wx,wz);}
        for(int y=0;y<HEIGHT;y++)for(int x=0;x<WIDTH;x++){
            int wx=home.getX()+(x-WIDTH/2)*2,wz=home.getZ()+(y-HEIGHT/2)*2,h=heights[x+(y+1)*WIDTH],north=heights[x+y*WIDTH];
            if(h==Integer.MIN_VALUE){pixels[y*WIDTH+x]=0x111519;continue;}
            var pos=new BlockPos(wx,h,wz);var state=view.getBlockState(pos);int color=state.getMapColor(view,pos).col;
            double shade=north==Integer.MIN_VALUE||h==north?.86:h>north?1:.71;pixels[y*WIDTH+x]=shade(color,shade);
        }
        pixels[(HEIGHT/2)*WIDTH+WIDTH/2]=0xF2C26A;
        return pixels;
    }
    private static int top(BlockGetter view,int x,int z){
        if(view instanceof Loaded loaded){var chunk=loaded.level.getChunkSource().getChunkNow(x>>4,z>>4);if(chunk==null)return Integer.MIN_VALUE;
            int y=chunk.getHeight(Heightmap.Types.WORLD_SURFACE,x&15,z&15);for(;y>loaded.level.getMinBuildHeight();y--){var s=chunk.getBlockState(new BlockPos(x,y,z));if(!s.isAir()&&s.getMapColor(view,new BlockPos(x,y,z))!=MapColor.NONE)return y;}}
        return Integer.MIN_VALUE;
    }
    /** Looking into the room from just inside its door, as the earlier television did, without loading anything. */
    static int[] room(BlockGetter view,BlockPos base){
        int[] pixels=new int[PIXELS];var eye=base.offset(0,1,-4).getCenter();
        for(int y=0;y<HEIGHT;y++)for(int x=0;x<WIDTH;x++){var ray=new Vec3((x-11.5)/18D,(7.5-y)/20D,-1).normalize();pixels[y*WIDTH+x]=trace(view,eye,ray,96);}
        return pixels;
    }
    /** A voxel walk: the first block with a collision shape or a fluid, shaded by distance and by face. */
    static int trace(BlockGetter view,Vec3 eye,Vec3 dir,double range){
        int x=(int)Math.floor(eye.x),y=(int)Math.floor(eye.y),z=(int)Math.floor(eye.z);int sx=dir.x>0?1:-1,sy=dir.y>0?1:-1,sz=dir.z>0?1:-1;
        double dx=Math.abs(1/dir.x),dy=Math.abs(1/dir.y),dz=Math.abs(1/dir.z);
        double tx=(dir.x>0?x+1-eye.x:eye.x-x)*dx,ty=(dir.y>0?y+1-eye.y:eye.y-y)*dy,tz=(dir.z>0?z+1-eye.z:eye.z-z)*dz,t=0;int axis=-1;
        while(t<=range){
            var pos=new BlockPos(x,y,z);var state=view.getBlockState(pos);
            if(!state.isAir()&&(!state.getCollisionShape(view,pos).isEmpty()||!state.getFluidState().isEmpty())){var color=state.getMapColor(view,pos);if(color!=MapColor.NONE)return shade(color.col,Math.max(.32,1-t/115)*(axis==1?.88:1));}
            if(tx<ty&&tx<tz){x+=sx;t=tx;tx+=dx;axis=0;}else if(ty<tz){y+=sy;t=ty;ty+=dy;axis=1;}else{z+=sz;t=tz;tz+=dz;axis=2;}
        }
        return 0x111519;
    }
    private static int shade(int color,double s){return ((int)((color>>16&255)*s)<<16)|((int)((color>>8&255)*s)<<8)|(int)((color&255)*s);}
    /** Only what is already loaded: anything else reads as empty air, so a picture can never load a chunk. */
    private record Loaded(ServerLevel level) implements BlockGetter {
        @Override public @Nullable BlockEntity getBlockEntity(BlockPos pos){return null;}
        @Override public BlockState getBlockState(BlockPos pos){var chunk=level.getChunkSource().getChunkNow(pos.getX()>>4,pos.getZ()>>4);return chunk==null||level.isOutsideBuildHeight(pos)?Blocks.AIR.defaultBlockState():chunk.getBlockState(pos);}
        @Override public FluidState getFluidState(BlockPos pos){return getBlockState(pos).getFluidState();}
        @Override public int getHeight(){return level.getHeight();}
        @Override public int getMinBuildHeight(){return level.getMinBuildHeight();}
    }
}
