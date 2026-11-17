package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.661, things to swear they heard (docs/HEARD_APART_0_4_661.md). The owner: "Two people stop walking in the same hall. One hears
 * their partner's footsteps carry on around the corner. Let them compare stories while they're still standing there."
 *
 * Two readers who stand still in the same ordinary hall, a few steps apart, deep enough into the House, may each hear something the
 * other does not: one hears the other's footsteps go on down the hall and round the turn, as if they had not stopped; the other hears
 * a door close softly somewhere behind the first. Nobody else hears either. Nothing is said or written about it.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class HeardApart {
    /** How long both must have stood still, together. */
    public static final int STILL=60;
    /** How many crossings deep both must be (the House's strangeness starts at six). */
    public static final int DEPTH=6;
    /** How long before a reader can be one of a pair again (ten minutes of the world's time). */
    public static final long REST=12000;
    /** How far apart the two must stand. */
    public static final double NEAR=2,FAR=14;
    /** About one stop together in two. */
    public static final int CHANCE=2;
    /** Ten footsteps, one every eight ticks and one in every stride and a bit: someone walking, not hurrying. */
    public static final int STEPS=10,PACE=8;
    public static final double STRIDE=1.6,SUB=.4;
    /** As loud as a reader's own footsteps at the first, a third of that at the last. */
    public static final float LOUD=.15F;
    /** The reader's own record: when they were last one of a pair. */
    public static final String LAST="HeardApart0661";
    /** A stop chosen by a test; the next one rolls as usual. */
    public static boolean force;

    /** Where each reader was last seen, and since when they have stood still there. Not saved. */
    private static final Map<UUID,Still> STILLS=new HashMap<>();
    private static final List<Cue> CUES=new ArrayList<>();
    private record Still(Vec3 at,long since,boolean rolled){}
    /** One sound for one reader alone, due at a game time. */
    public record Cue(UUID to,long due,Vec3 at,SoundEvent sound,float volume,float pitch,boolean step){}

    private HeardApart(){}

    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();var level=server.getLevel(HouseDimensions.INTERIOR);var origin=HouseSavedData.get(server).houseOrigin();
        if(level==null||origin==null){STILLS.clear();CUES.clear();return;}
        long now=level.getGameTime();
        Map<LabyrinthPlace,List<ServerPlayer>> halls=new HashMap<>();
        for(var p:level.players()){
            var place=LabyrinthPlaces.placeAt(origin,p.blockPosition());
            if(!hall(place)){STILLS.remove(p.getUUID());continue;}
            track(p,now);halls.computeIfAbsent(place,k->new ArrayList<>()).add(p);
        }
        STILLS.keySet().removeIf(id->level.getPlayerByUUID(id)==null);
        if(now%10==0)for(var group:halls.values())if(group.size()>1)consider(level,group,now);
        deliver(level,now);
    }
    @SubscribeEvent public static void left(PlayerEvent.PlayerLoggedOutEvent e){forget(e.getEntity().getUUID());}

    /** An ordinary hall: the domestic and stone halls, not the camp, the stories or the staircase. */
    public static boolean hall(LabyrinthPlace place){return place!=null&&place!=LabyrinthPlace.EXPLORER_CAMP&&HallAtmosphere.room(place);}

    /** Notes where a reader stands; moving more than a little starts their stillness again. */
    public static void track(ServerPlayer p,long now){
        var was=STILLS.get(p.getUUID());var at=p.position();
        boolean moved=was==null||Math.abs(at.x-was.at.x)>.05||Math.abs(at.z-was.at.z)>.05||Math.abs(at.y-was.at.y)>.5||now<was.since;
        STILLS.put(p.getUUID(),moved?new Still(at,now,false):new Still(was.at,was.since,was.rolled));
    }
    /** How long this reader has stood still, or -1. */
    public static long still(ServerPlayer p,long now){var s=STILLS.get(p.getUUID());return s==null?-1:now-s.since;}

    /** Whether a reader may be one of a pair now: alive, not watching, deep enough, rested, and still long enough. */
    public static boolean ready(ServerPlayer p,long now){
        if(!p.isAlive()||p.isSpectator())return false;
        if(LabyrinthData.get(p.server).returnDepth(p.getUUID())<DEPTH)return false;
        long last=p.getPersistentData().contains(LAST)?p.getPersistentData().getLong(LAST):Long.MIN_VALUE;
        if(last!=Long.MIN_VALUE&&now-last<REST&&now>=last)return false;
        return still(p,now)>=STILL;
    }

    /**
     * Readers in one hall: the first pair standing apart who are both ready, and not yet rolled for in this stop, may hear apart.
     * True when it happens.
     */
    public static boolean consider(ServerLevel level,List<ServerPlayer> group,long now){
        var ready=new ArrayList<ServerPlayer>();for(var p:group)if(ready(p,now)&&!STILLS.get(p.getUUID()).rolled)ready.add(p);
        ready.sort(Comparator.comparing(p->p.getUUID().toString()));
        for(int i=0;i<ready.size();i++)for(int j=i+1;j<ready.size();j++){
            var a=ready.get(i);var b=ready.get(j);double d=a.position().distanceTo(b.position());
            if(d<NEAR||d>FAR)continue;
            for(var p:List.of(a,b)){var s=STILLS.get(p.getUUID());STILLS.put(p.getUUID(),new Still(s.at,s.since,true));}
            if(!force&&level.getRandom().nextInt(CHANCE)!=0)return false;
            force=false;
            // Either may be the one who hears the footsteps; if one has no hall to walk on into, the other is.
            boolean flip=level.getRandom().nextBoolean();var first=flip?b:a;var second=flip?a:b;
            return hear(level,first,second,now)||hear(level,second,first,now);
        }
        return false;
    }

    /** The listener hears the partner walk on; the partner hears a door behind the listener. True when anything was planned. */
    public static boolean hear(ServerLevel level,ServerPlayer listener,ServerPlayer partner,long now){
        var away=partner.position().subtract(listener.position());
        var steps=walk(level,partner.position(),away);
        if(steps.size()<3)return false;
        for(int i=0;i<steps.size();i++){
            var at=steps.get(i);var below=BlockPos.containing(at).below();
            var type=level.getBlockState(below).getSoundType(level,below,partner);
            float fade=1-.65F*i/(steps.size()-1);
            CUES.add(new Cue(listener.getUUID(),now+12+(long)PACE*i,at,type.getStepSound(),LOUD*fade*type.getVolume(),type.getPitch(),true));
        }
        var back=listener.position().subtract(partner.position()).multiply(1,0,1).normalize().scale(8);
        var door=listener.position().add(back).add(0,1,0);
        CUES.add(new Cue(partner.getUUID(),now+12+PACE*4L,door,SoundEvents.WOODEN_DOOR_CLOSE,.45F,.85F,false));
        for(var p:List.of(listener,partner))p.getPersistentData().putLong(LAST,now);
        return true;
    }

    /**
     * The way the footsteps go: on from where they start, down the hall away from the listener, over floor a body could walk on,
     * turning to whichever side runs further when a wall stops them, never back towards the listener, and stopping where nothing is
     * open. One footstep in each stride of {@link #STRIDE} (a walking pace), looked at every {@link #SUB} so no thin wall is stepped
     * over.
     */
    public static List<Vec3> walk(ServerLevel level,Vec3 from,Vec3 away){
        var out=new ArrayList<Vec3>();if(away.x*away.x+away.z*away.z<1e-6)return out;
        var first=Math.abs(away.x)>=Math.abs(away.z)?new Vec3(Math.signum(away.x),0,0):new Vec3(0,0,Math.signum(away.z));
        var dir=first;Vec3 at=from;int per=(int)Math.round(STRIDE/SUB),sub=0;
        while(out.size()<STEPS){
            var next=at.add(dir.scale(SUB));
            if(!walkable(level,BlockPos.containing(next))){
                var left=new Vec3(dir.z,0,-dir.x);var right=new Vec3(-dir.z,0,dir.x);
                int l=left.dot(first)<0?0:open(level,at,left),r=right.dot(first)<0?0:open(level,at,right);
                if(l==0&&r==0)break;
                dir=l>=r?left:right;next=at.add(dir.scale(SUB));
            }
            at=next;if(++sub%per==0)out.add(at);
        }
        return out;
    }
    private static int open(ServerLevel level,Vec3 at,Vec3 dir){int n=0;while(n<20&&walkable(level,BlockPos.containing(at.add(dir.scale(SUB*(n+1))))))n++;return n;}
    public static boolean walkable(ServerLevel level,BlockPos feet){
        return level.isLoaded(feet)&&level.getBlockState(feet).getCollisionShape(level,feet).isEmpty()&&level.getBlockState(feet.above()).getCollisionShape(level,feet.above()).isEmpty()
                &&!level.getBlockState(feet.below()).getCollisionShape(level,feet.below()).isEmpty();
    }

    /** Sends what is due, to its reader alone; a reader gone from the House hears no more of it. */
    public static void deliver(ServerLevel level,long now){
        for(Iterator<Cue> it=CUES.iterator();it.hasNext();){
            var c=it.next();if(c.due>now)continue;it.remove();
            if(!(level.getPlayerByUUID(c.to) instanceof ServerPlayer p)||!p.isAlive())continue;
            p.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(c.sound),c.step?SoundSource.PLAYERS:SoundSource.BLOCKS,
                    c.at.x,c.at.y,c.at.z,c.volume,c.pitch,p.getRandom().nextLong()));
        }
    }
    /** What is still to come for a reader. For tests. */
    public static List<Cue> pending(UUID reader){var out=new ArrayList<Cue>();for(var c:CUES)if(c.to.equals(reader))out.add(c);return out;}
    public static void forget(UUID reader){STILLS.remove(reader);CUES.removeIf(c->c.to.equals(reader));}
}
