package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.network.*;
import java.util.*;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** One occupied clock per vertical section. No shared fire, route, creature or inventory authority. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class StaircaseAtmosphere {
    public static final String STATE="staircase_atmosphere_0448";
    private StaircaseAtmosphere(){}
    /** Geometric distance to the child, not another explorer's progress or fire count. */
    public static int growlPeriod(double distance){return 30+(int)Math.round(30*Math.min(1,Math.max(0,distance)/1280));}
    public static int shakePeriod(double distance){return growlPeriod(distance);}
    public static float growlVolume(double distance){return (float)(4.5-1.7*Math.min(1,Math.max(0,distance)/1280));}
    public static boolean occupiedSecond(CompoundTag clock,int period){
        return HallAtmosphere.period(clock,period);
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();if(server.getTickCount()%20!=0)return;
        var origin=HouseSavedData.get(server).houseOrigin();var level=server.getLevel(HouseDimensions.INTERIOR);if(origin==null||level==null)return;
        Map<Integer,List<ServerPlayer>> sections=new TreeMap<>();
        for(var p:level.players())if(p.isAlive()&&!p.isSpectator()&&FinaleArchitecture.contains(origin,p.blockPosition())){
            var phase=FinaleProgress.phase(server,p.getUUID());
            if(phase==FinaleProgress.Phase.STAIRCASE||phase==FinaleProgress.Phase.HOMEWARD)
                sections.computeIfAbsent(Math.floorDiv(FinaleArchitecture.TOP-p.blockPosition().getY(),128),ignored->new ArrayList<>()).add(p);
        }
        // Neighbors on either side of a section boundary share the same clock.
        // Distant groups in a section still advance that clock only once.
        Map<Integer,Integer> parents=new TreeMap<>();for(var id:sections.keySet())parents.put(id,id);
        var present=sections.values().stream().flatMap(Collection::stream).toList();
        for(int i=0;i<present.size();i++)for(int j=i+1;j<present.size();j++)if(present.get(i).distanceToSqr(present.get(j))<=StaircaseDebris.NEARBY*StaircaseDebris.NEARBY){
            int a=root(parents,section(present.get(i))),b=root(parents,section(present.get(j)));parents.put(Math.max(a,b),Math.min(a,b));
        }
        Map<Integer,List<ServerPlayer>> merged=new TreeMap<>();for(var entry:sections.entrySet())merged.computeIfAbsent(root(parents,entry.getKey()),ignored->new ArrayList<>()).addAll(entry.getValue());
        var data=LabyrinthData.get(server);
        for(var entry:merged.entrySet()){
            String key=origin.asLong()+":"+entry.getKey();var clock=data.stateEntry(STATE,key);
            double distance=entry.getValue().stream().mapToDouble(p->p.position().distanceTo(FinaleArchitecture.cell(origin).getCenter())).min().orElse(1280);
            if(occupiedSecond(clock,growlPeriod(distance))){
                for(var p:entry.getValue()){
                    var at=p.position().add(0,-2,0);
                    var voice=Holder.direct(SoundEvent.createVariableRangeEvent(Growl.Kind.BELOW.sound()));
                    // The existing Minotaur recording, heard below the tread; no GrowlChanges side effects.
                    p.connection.send(new ClientboundSoundPacket(voice,SoundSource.HOSTILE,at.x,at.y,at.z,growlVolume(p.position().distanceTo(FinaleArchitecture.cell(origin).getCenter())),.76F,clock.getInt("Pulses")));
                    HousePackets.send(p,new NovelScenePayload(13,0,"",0,.65F+(float)(.3*(1-Math.min(1,distance/1280)))));
                    level.sendParticles(p,new BlockParticleOption(ParticleTypes.FALLING_DUST,Blocks.DEEPSLATE.defaultBlockState()),false,
                            p.getX(),p.getY()+3,p.getZ(),14,2,.3,2,.02);
                }
                StaircaseDebris.fallForPlayers(level,origin,entry.getValue());
                clock.putInt("Pulses",clock.getInt("Pulses")+1);
            }
            for(var id:entry.getValue().stream().map(StaircaseAtmosphere::section).distinct().toList())data.setStateEntry(STATE,origin.asLong()+":"+id,clock.copy());
        }
    }
    private static int section(ServerPlayer p){return Math.floorDiv(FinaleArchitecture.TOP-p.blockPosition().getY(),128);}
    private static int root(Map<Integer,Integer> parents,int id){while(parents.get(id)!=id)id=parents.get(id);return id;}
}
