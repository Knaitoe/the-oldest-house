package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.network.HallFeedPayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.662, the feed (docs/HALL_FEED_0_4_662.md). The owner, for a reader alone: "a CCTV feed could show the same hall a few seconds
 * after you left it, with a set of footsteps still going."
 *
 * Now and then, when a reader deep in the House comes alone into an ordinary hall, the hall is watched for them: their own game keeps a
 * picture of it from high above the door they came in by, without them in it, and remembers the way they walked. If they leave it, still
 * alone, by another door into another ordinary hall, a monitor is on the wall of that one, high up, near where they come in. It shows the
 * hall they have just left, empty, and after a few seconds footprints come into it one after another along the way they walked, out to the
 * door they left by, and the monitor gives out footsteps as they come. Then the picture breaks up and the screen goes dark. The monitor,
 * the picture and the footsteps are that reader's alone; nothing is placed in the world. The server decides when and where; each reader's
 * game draws it.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class HallFeed {
    /** How many crossings deep (the House's strangeness starts at six). */
    public static final int DEPTH=6;
    /** How long before a reader can be shown the feed again (fifteen minutes of the world's time). */
    public static final long REST=18000;
    /** About one hall in three, once a reader is deep enough and rested. */
    public static final int CHANCE=3;
    /** How long a reader must have been in the watched hall for there to be anything to show (its picture is taken once a second). */
    public static final int STAY=60;
    /** How far from where the reader comes in the monitor may hang, and how high above their feet its middle is. */
    public static final double NEAREST=2,FURTHEST=7,HIGH=2.2;
    /** The reader's own record: when they were last shown the feed. */
    public static final String LAST="HallFeed0662";
    /** The next hall a test brings a reader into is watched, if it may be. */
    public static boolean force;

    /** A watched hall: which, since when, and where its camera hangs. Not saved. */
    public record Watch(LabyrinthPlace place,long since,Vec3 camera){}
    /** The monitor: the open cell it hangs in, against the wall behind it, its screen turned to {@code facing}. */
    public record Spot(BlockPos cell,Direction facing){}

    /** Where the reader stood, and what the floor under them sounded like. Kept by the reader's game. */
    public record Mark(Vec3 at,SoundEvent step,float volume,float pitch){}
    /** A footprint: where, which way it points, which foot, and what it sounds like. */
    public record Print(Vec3 at,Vec3 heading,boolean left,SoundEvent step,float volume,float pitch){}
    /** A footprint in each stride of this length (a walking pace), over at most this much of the way walked. */
    public static final double STRIDE=1.6,LENGTH=16;

    private static final Map<UUID,LabyrinthPlace> WHERE=new HashMap<>();
    private static final Map<UUID,Watch> WATCHING=new HashMap<>();
    private static final Map<UUID,LabyrinthPlace> SHOWING=new HashMap<>();

    private HallFeed(){}

    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();var level=server.getLevel(HouseDimensions.INTERIOR);var origin=HouseSavedData.get(server).houseOrigin();
        if(level==null||origin==null){WHERE.clear();WATCHING.clear();SHOWING.clear();return;}
        long now=level.getGameTime();
        Map<UUID,LabyrinthPlace> here=new HashMap<>();Map<LabyrinthPlace,Integer> company=new HashMap<>();
        for(var p:level.players()){
            if(!p.isAlive()||p.isSpectator())continue;
            var place=LabyrinthPlaces.placeAt(origin,p.blockPosition());if(place==null)continue;
            here.put(p.getUUID(),place);company.merge(place,1,Integer::sum);
        }
        // Someone else come into a watched hall: it is no longer the reader's alone, and is forgotten.
        for(var it=WATCHING.entrySet().iterator();it.hasNext();){
            var watch=it.next();var place=watch.getValue().place();
            if(here.get(watch.getKey())!=place||company.getOrDefault(place,0)<2)continue;
            it.remove();if(level.getPlayerByUUID(watch.getKey()) instanceof ServerPlayer p)HousePackets.send(p,new HallFeedPayload(HallFeedPayload.DROP,0,0,0,0,now));
        }
        for(var p:level.players()){
            var place=here.get(p.getUUID());if(place==null)continue;
            var was=WHERE.put(p.getUUID(),place);
            if(was==place)continue;
            boolean aloneThen=was!=null&&company.getOrDefault(was,0)==0,aloneNow=company.getOrDefault(place,0)==1;
            if(was==null)arrive(level,p,place,aloneNow,now);
            else moved(level,p,was,place,aloneThen,aloneNow,now);
        }
        // A reader gone from the House, or out of it into a place that is not one of its own, keeps nothing of it.
        for(var it=WHERE.keySet().iterator();it.hasNext();){
            var id=it.next();if(level.getPlayerByUUID(id) instanceof ServerPlayer p&&p.isAlive()&&!p.isSpectator())continue;
            it.remove();boolean had=WATCHING.remove(id)!=null|SHOWING.remove(id)!=null;
            if(had&&server.getPlayerList().getPlayer(id) instanceof ServerPlayer gone)HousePackets.send(gone,new HallFeedPayload(HallFeedPayload.DROP,0,0,0,0,0));
        }
    }
    @SubscribeEvent public static void left(PlayerEvent.PlayerLoggedOutEvent e){forget(e.getEntity().getUUID());}

    /** An ordinary hall: the same halls as readers together hear things apart in. */
    public static boolean hall(LabyrinthPlace place){return HeardApart.hall(place);}

    /** Whether a reader may have a hall watched for them now: alive, not watching, deep enough, rested. */
    public static boolean ready(ServerPlayer p,long now){
        if(!p.isAlive()||p.isSpectator())return false;
        if(LabyrinthData.get(p.server).returnDepth(p.getUUID())<DEPTH)return false;
        if(!p.getPersistentData().contains(LAST))return true;
        long last=p.getPersistentData().getLong(LAST);return now<last||now-last>=REST;
    }

    /** A reader has come into a place; if it is an ordinary hall, they are alone in it and it rolls their way, it is watched. */
    public static boolean arrive(ServerLevel level,ServerPlayer p,LabyrinthPlace place,boolean alone,long now){
        if(!hall(place)||!alone||!ready(p,now))return false;
        if(!force&&level.getRandom().nextInt(CHANCE)!=0)return false;
        force=false;
        var camera=camera(level,p);if(camera==null)return false;
        WATCHING.put(p.getUUID(),new Watch(place,now,camera));
        HousePackets.send(p,new HallFeedPayload(HallFeedPayload.ARM,camera.x,camera.y,camera.z,0,now));
        return true;
    }

    /**
     * A reader has gone from one place to another. A monitor they were shown is gone. If the place they left was watched, and they leave
     * it alone, having been in it long enough, for an ordinary hall where they are alone and a monitor can hang, it is shown there.
     * Otherwise their game forgets the watched hall, and the place they come into may be watched in its turn. True when shown.
     */
    public static boolean moved(ServerLevel level,ServerPlayer p,LabyrinthPlace from,LabyrinthPlace to,boolean aloneThen,boolean aloneNow,long now){
        var id=p.getUUID();
        if(SHOWING.remove(id)!=null)HousePackets.send(p,new HallFeedPayload(HallFeedPayload.HIDE,0,0,0,0,now));
        var watch=WATCHING.remove(id);
        if(watch!=null){
            var spot=watch.place()==from&&hall(to)&&aloneThen&&aloneNow&&now-watch.since()>=STAY?spot(level,p):null;
            if(spot!=null){
                var at=spot.cell().getCenter();
                HousePackets.send(p,new HallFeedPayload(HallFeedPayload.SHOW,at.x,at.y,at.z,spot.facing().get2DDataValue(),now));
                p.getPersistentData().putLong(LAST,now);SHOWING.put(id,to);
                return true;
            }
            HousePackets.send(p,new HallFeedPayload(HallFeedPayload.DROP,0,0,0,0,now));
        }
        arrive(level,p,to,aloneNow,now);
        return false;
    }

    /**
     * Where the camera hangs: just under the ceiling above where the reader stands as they come in, set back towards the door behind them
     * as far as the room lets it. Null where there is no ceiling in reach.
     */
    public static Vec3 camera(ServerLevel level,ServerPlayer p){
        var at=p.position();var feet=BlockPos.containing(at);var look=Vec3.directionFromRotation(0,p.getYRot());
        int top=Integer.MIN_VALUE;
        for(int dy=2;dy<=8;dy++){var cell=feet.above(dy);if(!level.isLoaded(cell))return null;if(!open(level,cell)){top=cell.getY();break;}}
        if(top==Integer.MIN_VALUE)return null;
        double y=top-.35;
        for(double back:new double[]{.6,.35,0}){var c=new Vec3(at.x-look.x*back,y,at.z-look.z*back);if(open(level,BlockPos.containing(c)))return c;}
        return null;
    }

    /**
     * Where the monitor hangs: on the first wall the reader faces, or failing that the first a little to one side, then further round,
     * then behind them, between {@link #NEAREST} and {@link #FURTHEST} blocks off, with its middle {@link #HIGH} above their feet. The
     * wall must be solid, not glass; the cell it hangs in empty of everything, the cell below open, and nothing hung there already.
     */
    public static Spot spot(ServerLevel level,ServerPlayer p){return spot(level,p.position(),p.getYRot());}
    /** The same, for a reader standing at {@code at} and facing {@code yaw}. */
    public static Spot spot(ServerLevel level,Vec3 at,float yaw){
        var feet=BlockPos.containing(at);int y=feet.getY()+2;
        for(int turn:new int[]{0,-35,35,-70,70,-110,110,180}){
            var dir=Vec3.directionFromRotation(0,yaw+turn);BlockPos last=null;
            for(double d=.5;d<=FURTHEST+.01;d+=.25){
                var cell=BlockPos.containing(at.x+dir.x*d,y,at.z+dir.z*d);
                if(!level.isLoaded(cell))break;
                if(open(level,cell)){last=cell;continue;}
                if(last==null||d<NEAREST)break;
                var facing=facing(level,last,cell);
                if(facing!=null&&fits(level,last,facing))return new Spot(last,facing);
                break;
            }
        }
        return null;
    }
    /** Which way a monitor in {@code open} turns, against the wall it ran into at {@code wall}; null at a corner it cannot tell. */
    private static Direction facing(ServerLevel level,BlockPos open,BlockPos wall){
        int dx=Integer.signum(wall.getX()-open.getX()),dz=Integer.signum(wall.getZ()-open.getZ());
        if(dx!=0&&dz!=0){
            if(!open(level,open.offset(dx,0,0)))dz=0;
            else if(!open(level,open.offset(0,0,dz)))dx=0;
            else return null;
        }
        return dx!=0?Direction.fromDelta(-dx,0,0):dz!=0?Direction.fromDelta(0,0,-dz):null;
    }
    /** Whether a monitor hangs in {@code cell} with its back to the wall behind it. */
    public static boolean fits(ServerLevel level,BlockPos cell,Direction facing){
        var wall=cell.relative(facing.getOpposite());
        if(!level.getBlockState(cell).isAir()||!open(level,cell.below()))return false;
        if(!level.getBlockState(wall).isSolidRender(level,wall)||!level.getBlockState(wall.below()).isSolidRender(level,wall.below()))return false;
        return level.getEntitiesOfClass(HangingEntity.class,new AABB(cell).inflate(.45,.6,.45)).isEmpty();
    }
    private static boolean open(ServerLevel level,BlockPos cell){return level.isLoaded(cell)&&level.getBlockState(cell).getCollisionShape(level,cell).isEmpty();}

    /**
     * The footprints: the last {@code length} of the way walked, a print every {@code stride} along it, feet turn about. Fewer than three is
     * none.
     */
    public static List<Print> walked(List<Mark> marks,double stride,double length){
        if(marks.size()<2)return List.of();
        double run=0;int from=marks.size()-1;
        while(from>0&&run<length){run+=marks.get(from).at().distanceTo(marks.get(from-1).at());from--;}
        var out=new java.util.ArrayList<Print>();double carry=stride*.5;boolean leftFoot=true;
        for(int i=from+1;i<marks.size();i++){
            var a=marks.get(i-1);var b=marks.get(i);var seg=b.at().subtract(a.at());double len=Math.hypot(seg.x,seg.z);
            if(len<1e-3)continue;
            var heading=new Vec3(seg.x/len,0,seg.z/len);double d=carry;
            for(;d<=len;d+=stride){out.add(new Print(a.at().add(seg.scale(d/len)),heading,leftFoot,b.step(),b.volume(),b.pitch()));leftFoot=!leftFoot;}
            carry=d-len;
        }
        return out.size()>=3?out:List.of();
    }

    /** The hall watched for a reader now, or null. For tests. */
    public static Watch watching(UUID reader){return WATCHING.get(reader);}
    /** The place a reader's monitor hangs in now, or null. For tests. */
    public static LabyrinthPlace showing(UUID reader){return SHOWING.get(reader);}
    public static void forget(UUID reader){WHERE.remove(reader);WATCHING.remove(reader);SHOWING.remove(reader);}
}
