package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Copies of private scenes (owner, October 28; docs/SCENE_COPIES_0_4_153.md). A private scene stands once in the House; a
 * reader who would find it held by someone else gets a copy of their own. Copies stand a million blocks west of the House,
 * built in bounded slices by the scene's own builder and dressed by the same passes as the original, run against a shifted
 * origin so that their once-only checkpoints are the copy's own. A reader keeps the copy they entered until they finish its
 * story, so whatever they changed in it is still there when they come back; then it can be another reader's. The House
 * builds the next copy ahead, and keeps no more copies of a scene than the server's {@code sceneCopies} setting.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class SceneInstances {
    public static final String STATE="scene_copies_0153";
    /** Copies stand this far west of their original, and each further copy of a scene this much further on. Both are wider than the whole slot grid, so no copy overlaps another scene. */
    public static final int WEST=1<<20,STEP=1<<16;
    /** The private scenes that have copies so far. Each keeps its state with each reader and takes its place from the reader. */
    private static final Set<LabyrinthPlace> COPYABLE=EnumSet.of(LabyrinthPlace.CONFESSION,LabyrinthPlace.HOLY_RABBIT,LabyrinthPlace.WHEEL,LabyrinthPlace.SECOND_WAITRESS,LabyrinthPlace.MISSING_SLEEP);
    private static final TicketType<ChunkPos> TICKET=TicketType.create("scene_copy",Comparator.comparingLong(ChunkPos::toLong));
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE|Block.UPDATE_SUPPRESS_DROPS;
    /** Columns of an interior copy's slot filled solid per tick, before its rooms are carved into it, as the original's are. */
    private static final int FILL_COLUMNS=48;
    private SceneInstances(){}

    public static boolean copyable(@Nullable LabyrinthPlace p){return p!=null&&COPYABLE.contains(p);}
    public static Set<LabyrinthPlace> copyable(){return Collections.unmodifiableSet(COPYABLE);}
    public static int cap(){return HouseConfig.setting(HouseConfig.SCENE_COPIES);}

    // ------------------------------------------------------------------------------------------------ where copies stand
    /** The House origin moved so that a scene's own place falls on its copy: everything keyed by origin and place then reaches the copy. */
    public static BlockPos shiftedOrigin(BlockPos origin,int copy){return copy<=0?origin:origin.offset(-WEST-(copy-1)*STEP,0,0);}
    /** Copy 0 is the original. */
    public static @Nullable BlockPos base(MinecraftServer s,LabyrinthPlace p,int copy){
        var o=HouseSavedData.get(s).houseOrigin();return o==null?null:LabyrinthPlaces.base(shiftedOrigin(o,copy),p);
    }
    /** The original and every copy that stands, for the passes that walk each scene's ground. */
    public static List<BlockPos> bases(MinecraftServer s,LabyrinthPlace p){
        var out=new ArrayList<BlockPos>();var original=base(s,p,0);if(original!=null)out.add(original);
        for(var a:areas(s))if(a.place==p)out.add(a.base);return out;
    }

    // ------------------------------------------------------------------------------------------------ saved copies and keepers
    private static CompoundTag scene(LabyrinthData d,LabyrinthPlace p){return d.stateEntry(STATE,p.id());}
    private static void scene(LabyrinthData d,LabyrinthPlace p,CompoundTag t){d.setStateEntry(STATE,p.id(),t);version++;}
    public static int copies(LabyrinthData d,LabyrinthPlace p){return scene(d,p).getInt("Copies");}
    private static CompoundTag copy(LabyrinthData d,LabyrinthPlace p,int k){return scene(d,p).getCompound("C"+k);}
    private static void copy(LabyrinthData d,LabyrinthPlace p,int k,CompoundTag t){var all=scene(d,p);all.put("C"+k,t);scene(d,p,all);}
    public static boolean built(LabyrinthData d,LabyrinthPlace p,int k){return k==0?LabyrinthBuilder.isPlaceReady(d,p):k<=copies(d,p)&&copy(d,p,k).getBoolean("Built");}
    /** The copy a reader keeps, or -1. */
    public static int kept(LabyrinthData d,UUID reader,LabyrinthPlace p){var b=scene(d,p).getCompound("Kept");return b.contains(reader.toString())?b.getInt(reader.toString()):-1;}
    private static void keep(LabyrinthData d,UUID reader,LabyrinthPlace p,int k){var all=scene(d,p);var b=all.getCompound("Kept");b.putInt(reader.toString(),k);all.put("Kept",b);scene(d,p,all);}
    /** A reader keeps their copy while its story can still be dealt to them. */
    private static boolean unfinished(LabyrinthData d,UUID reader,LabyrinthPlace p){return LiteraryVignettes.canDeal(d,reader,p);}
    /** Someone other than {@code reader} keeps copy {@code k} for a story they have not finished. */
    private static boolean keptByAnother(LabyrinthData d,LabyrinthPlace p,int k,UUID reader){
        var b=scene(d,p).getCompound("Kept");
        for(var key:b.getAllKeys()){if(b.getInt(key)!=k||key.equals(reader.toString()))continue;try{if(unfinished(d,UUID.fromString(key),p))return true;}catch(IllegalArgumentException ignored){}}
        return false;
    }
    /** Readers already partway through a scene before it had copies keep the original, which still holds what they did there. */
    private static void settle(LabyrinthData d,LabyrinthPlace p){
        var all=scene(d,p);if(all.getBoolean("Settled"))return;var b=all.getCompound("Kept");
        for(var key:LiteraryVignettes.readers(d,p)){if(b.contains(key))continue;try{var id=UUID.fromString(key);if(LiteraryVignettes.personal(d,id,p).getInt("Visit")>0)b.putInt(key,0);}catch(IllegalArgumentException ignored){}}
        all.put("Kept",b);all.putBoolean("Settled",true);scene(d,p,all);
    }

    // ------------------------------------------------------------------------------------------------ which copy a position is in
    private record Area(LabyrinthPlace place,int copy,BlockPos base,ResourceKey<Level> dimension,BoundingBox slot,AABB room){}
    private static List<Area> areas=List.of();private static LabyrinthData cached;private static int version,cachedVersion=-1;
    private static List<Area> areas(MinecraftServer s){
        var d=LabyrinthData.get(s);
        if(d!=cached||cachedVersion!=version){
            cached=d;cachedVersion=version;var out=new ArrayList<Area>();var o=HouseSavedData.get(s).houseOrigin();
            if(o!=null)for(var p:COPYABLE)for(int k=1;k<=copies(d,p);k++){
                if(!copy(d,p,k).getBoolean("Built"))continue;var shifted=shiftedOrigin(o,k);var b=LabyrinthPlaces.base(shifted,p);var slot=LabyrinthPlaces.slotBounds(shifted,p);
                if(b!=null&&slot!=null)out.add(new Area(p,k,b,NovelRooms.dimension(p),slot,IndianLakeRooms.bounds(b,p)));
            }
            areas=List.copyOf(out);
        }
        return areas;
    }
    /**
     * The scene whose copy's slot holds a position, for {@link LabyrinthPlaces#placeAt}. Copies stand at fixed offsets, so
     * this needs only the origin and the position: the client asks it too, for the scene's sky and view.
     */
    public static @Nullable LabyrinthPlace placeAt(BlockPos origin,BlockPos pos){
        long west=(long)origin.getX()-WEST-pos.getX();if(west<-STEP/2)return null;
        long k=Math.floorDiv(west+STEP/2,(long)STEP)+1;if(k<1||k>64)return null;
        var shifted=shiftedOrigin(origin,(int)k);
        for(var p:COPYABLE){var slot=LabyrinthPlaces.slotBounds(shifted,p);if(slot!=null&&slot.isInside(pos))return p;}
        return null;
    }
    /** Which copy of a scene a position is in: 0 for the original, -1 for neither. */
    public static int at(MinecraftServer s,LabyrinthPlace p,ResourceKey<Level> dimension,Vec3 pos){
        if(!dimension.equals(NovelRooms.dimension(p)))return -1;
        var original=base(s,p,0);if(original!=null&&IndianLakeRooms.bounds(original,p).contains(pos))return 0;
        for(var a:areas(s))if(a.place==p&&a.room.contains(pos))return a.copy;
        return -1;
    }
    /** The copy this reader stands in, else the one they keep, else the original. */
    public static int of(ServerPlayer p,LabyrinthPlace place){
        int k=at(p.server,place,p.level().dimension(),p.position());if(k>=0)return k;
        return Math.max(0,kept(LabyrinthData.get(p.server),p.getUUID(),place));
    }
    public static @Nullable BlockPos base(ServerPlayer p,LabyrinthPlace place){return base(p.server,place,of(p,place));}
    /** Whether a living player other than {@code reader} is in copy {@code k}. Spectators hold nothing. */
    private static boolean occupied(MinecraftServer s,LabyrinthPlace p,int k,UUID reader){
        for(var q:s.getPlayerList().getPlayers())if(!q.getUUID().equals(reader)&&q.isAlive()&&!q.isSpectator()&&at(s,p,q.level().dimension(),q.position())==k)return true;
        return false;
    }

    // ------------------------------------------------------------------------------------------------ doors
    /** A copy's doors carry its number after the original's id, so everything that reads a door's scene from its id still reads it. */
    public static String doorId(LabyrinthPlace p,int copy,String name){return copy<=0?p.id()+"/"+name:p.id()+"/"+name+"@"+copy;}
    /** The original door a copy's door stands for. */
    public static String plain(String id){int at=id.lastIndexOf('@');return at<0?id:id.substring(0,at);}
    public static int copyOf(String id){int at=id.lastIndexOf('@');if(at<0)return 0;try{return Integer.parseInt(id.substring(at+1));}catch(NumberFormatException e){return 0;}}
    public static @Nullable LabyrinthData.Door entry(LabyrinthData d,LabyrinthPlace p,int copy){return d.door(doorId(p,copy,"entry"));}
    /** The entry of the copy a reader stands in; the original's otherwise. */
    public static @Nullable LabyrinthData.Door entryAt(ServerPlayer p,LabyrinthPlace place){
        int k=copyable(place)?at(p.server,place,p.level().dimension(),p.position()):0;return entry(LabyrinthData.get(p.server),place,Math.max(0,k));
    }
    /** The entry belonging to the same copy as one of its doors. */
    public static @Nullable LabyrinthData.Door entryOf(LabyrinthData d,LabyrinthPlace place,LabyrinthData.Door door){return entry(d,place,copyOf(door.id));}

    /** What a reader's door into a scene with copies opens onto: a door, a copy still being made ready, or nothing free. */
    public record Entry(@Nullable LabyrinthData.Door door,int copy,boolean waiting){}
    public static Entry enter(ServerPlayer p,LabyrinthPlace place){
        var s=p.server;var d=LabyrinthData.get(s);var me=p.getUUID();settle(d,place);
        int mine=kept(d,me,place);
        if(mine>=0&&built(d,place,mine)){
            // A copy they keep and nobody else is in. Only an original shared by readers from before copies can be busy.
            if(!occupied(s,place,mine,me)){ahead(s,d,place,mine);return new Entry(entry(d,place,mine),mine,false);}
            return new Entry(null,mine,false);
        }
        for(int k=0;k<=copies(d,place);k++){
            if(!built(d,place,k)||occupied(s,place,k,me)||keptByAnother(d,place,k,me))continue;
            keep(d,me,place,k);ahead(s,d,place,k);return new Entry(entry(d,place,k),k,false);
        }
        // Nothing stands free: a copy is made ready, unless every copy the server allows exists already.
        boolean making=making(d,place)||allocate(d,place);
        return new Entry(null,-1,making);
    }
    /** While one copy stands free beside the one just taken, nothing more is built; otherwise the next is made ready ahead. */
    private static void ahead(MinecraftServer s,LabyrinthData d,LabyrinthPlace place,int taken){
        // A reader alone on the server never needs another copy.
        if(s.getPlayerList().getPlayers().stream().filter(q->!q.isSpectator()).count()<2)return;
        for(int k=0;k<=copies(d,place);k++)if(k!=taken&&(!built(d,place,k)||!occupied(s,place,k,new UUID(0,0))&&!keptByAnother(d,place,k,new UUID(0,0))))return;
        allocate(d,place);
    }
    private static boolean making(LabyrinthData d,LabyrinthPlace p){for(int k=1;k<=copies(d,p);k++)if(!copy(d,p,k).getBoolean("Built"))return true;return false;}
    private static boolean allocate(LabyrinthData d,LabyrinthPlace p){
        int n=copies(d,p);if(n>=cap()||making(d,p))return false;
        var all=scene(d,p);all.putInt("Copies",n+1);all.put("C"+(n+1),new CompoundTag());scene(d,p,all);return true;
    }

    // ------------------------------------------------------------------------------------------------ building
    private static final class Job {
        final LabyrinthPlace place;final int copy;final ServerLevel level;final BlockPos base,origin;final BoundingBox slot;final List<ChunkPos> chunks=new ArrayList<>();
        BuildBlocks.Plan plan,dressing;
        Job(LabyrinthPlace place,int copy,ServerLevel level,BlockPos base,BlockPos origin,BoundingBox slot){this.place=place;this.copy=copy;this.level=level;this.base=base;this.origin=origin;this.slot=slot;}
    }
    private static Job job;private static LabyrinthData store;
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){advance(e.getServer());}
    /** One copy at a time, in bounded slices that resume from their saved cursors after a restart. */
    public static void advance(MinecraftServer s){
        var d=LabyrinthData.get(s);if(store!=d){release();store=d;}
        if(job==null){job=next(s,d);if(job==null)return;}
        var j=job;var l=j.level;
        if(j.chunks.isEmpty()){
            // An outdoor copy holds the same wider ground its original is built over, woods and banks included.
            var box=NovelRooms.outside(j.place)?OutdoorRelocation.bounds(j.base,j.place).minmax(AABB.of(j.slot)):AABB.of(j.slot);
            for(int x=((int)Math.floor(box.minX)-2)>>4;x<=((int)Math.ceil(box.maxX)+2)>>4;x++)for(int z=((int)Math.floor(box.minZ)-2)>>4;z<=((int)Math.ceil(box.maxZ)+2)>>4;z++){var c=new ChunkPos(x,z);l.getChunkSource().addRegionTicket(TICKET,c,1,c);j.chunks.add(c);}
        }
        // Never call a synchronous chunk getter as a fallback; the entity sections take the scene's own installs.
        for(var c:j.chunks)if(l.getChunkSource().getChunkNow(c.x,c.z)==null||!l.areEntitiesLoaded(c.toLong()))return;
        var t=copy(d,j.place,j.copy);
        if(!t.getBoolean("Filled")){if(fill(j,t)){t.putBoolean("Filled",true);t.remove("FillColumn");}copy(d,j.place,j.copy,t);return;}
        if(!t.getBoolean("Carved")){
            if(j.plan==null){j.plan=BuildBlocks.record(l,()->LiteraryRooms.build(l,j.base,j.place));j.plan.resume(t.getCompound("BuildCursor"));}
            if(!j.plan.tick()){t.put("BuildCursor",j.plan.checkpoint());copy(d,j.place,j.copy,t);return;}
            t.putBoolean("Carved",true);t.remove("BuildCursor");copy(d,j.place,j.copy,t);
            // The original's finishing passes, at the copy: their checkpoints are keyed by the shifted origin.
            ScenePolish.polishOnce(l,j.origin,j.place);j.dressing=SceneHuntReview.prepareFresh(l,j.origin,j.place);return;
        }
        if(j.dressing!=null&&!j.dressing.tick())return;
        OnwardDoors.install(l,j.base,j.place,doorId(j.place,j.copy,OnwardDoors.name(j.place)));
        for(var spec:j.place.doors()){if(OnwardDoors.isOnward(spec))continue;d.putDoor(new LabyrinthData.Door(doorId(j.place,j.copy,spec.name()),NovelRooms.dimension(j.place),j.base.offset(spec.rel()),spec.facing(),spec.destination(),false));}
        t.putBoolean("Built",true);copy(d,j.place,j.copy,t);
        TheOldestHouse.LOGGER.info("A copy of {} stands ready: copy {} at {}",j.place.id(),j.copy,j.base.toShortString());
        release();
    }
    private static @Nullable Job next(MinecraftServer s,LabyrinthData d){
        var o=HouseSavedData.get(s).houseOrigin();if(o==null)return null;
        for(var p:COPYABLE)for(int k=1;k<=copies(d,p);k++){
            if(copy(d,p,k).getBoolean("Built"))continue;
            var l=s.getLevel(NovelRooms.dimension(p));var shifted=shiftedOrigin(o,k);var b=LabyrinthPlaces.base(shifted,p);var slot=LabyrinthPlaces.slotBounds(shifted,p);
            if(l!=null&&b!=null&&slot!=null)return new Job(p,k,l,b,shifted,slot);
        }
        return null;
    }
    /** An interior copy's whole slot is filled solid first, a few columns a tick, as the original's was. */
    private static boolean fill(Job j,CompoundTag t){
        if(!LabyrinthBuilder.prefills(j.place))return true;
        int width=j.slot.getXSpan(),columns=width*j.slot.getZSpan(),at=t.getInt("FillColumn");
        int minY=Math.max(j.slot.minY(),j.level.getMinBuildHeight()),maxY=Math.min(j.slot.maxY(),j.level.getMaxBuildHeight()-1);var pos=new BlockPos.MutableBlockPos();
        for(int n=0;n<FILL_COLUMNS&&at<columns;n++,at++){
            int x=j.slot.minX()+at%width,z=j.slot.minZ()+at/width;
            for(int y=minY;y<=maxY;y++){pos.set(x,y,z);if(j.level.getBlockState(pos)!=LabyrinthBuilder.SOLID)j.level.setBlock(pos,LabyrinthBuilder.SOLID,F);}
        }
        t.putInt("FillColumn",at);return at>=columns;
    }
    private static void release(){if(job!=null)for(var c:job.chunks)job.level.getChunkSource().removeRegionTicket(TICKET,c,1,c);job=null;}
    /** Fixtures only: whether a copy is under construction. */
    public static boolean building(){return job!=null;}
    public static void clear(){release();store=null;cached=null;areas=List.of();}
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){clear();}
}
