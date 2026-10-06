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
    public static int growlPeriod(double distance){return 8+(int)Math.round(22*Math.min(1,Math.max(0,distance)/1280));}
    public static int shakePeriod(double distance){return 4+(int)Math.round(8*Math.min(1,Math.max(0,distance)/1280));}
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
        var data=LabyrinthData.get(server);
        for(var entry:sections.entrySet()){
            String key=origin.asLong()+":"+entry.getKey();var clock=data.stateEntry(STATE,key);
            double distance=entry.getValue().stream().mapToDouble(p->p.position().distanceTo(FinaleArchitecture.cell(origin).getCenter())).min().orElse(1280);
            if(occupiedSecond(clock,growlPeriod(distance))){
                for(var p:entry.getValue()){
                    var at=p.position().add(0,-2,0);
                    var voice=Holder.direct(SoundEvent.createVariableRangeEvent(Growl.Kind.BELOW.sound()));
                    // The existing Minotaur recording, heard below the tread; no GrowlChanges side effects.
                    p.connection.send(new ClientboundSoundPacket(voice,SoundSource.HOSTILE,at.x,at.y,at.z,growlVolume(p.position().distanceTo(FinaleArchitecture.cell(origin).getCenter())),.76F,clock.getInt("Pulses")));
                }
                clock.putInt("Pulses",clock.getInt("Pulses")+1);
            }
            var dust=clock.getCompound("Dust");
            if(occupiedSecond(dust,shakePeriod(distance))){
                for(var p:entry.getValue()){
                    HousePackets.send(p,new NovelScenePayload(13,0,"",0,.65F+(float)(.3*(1-Math.min(1,distance/1280)))));
                    level.sendParticles(p,new BlockParticleOption(ParticleTypes.FALLING_DUST,Blocks.DEEPSLATE.defaultBlockState()),false,
                            p.getX(),p.getY()+3,p.getZ(),14,2,.3,2,.02);
                }
            }
            clock.put("Dust",dust);
            data.setStateEntry(STATE,key,clock);
        }
    }
}
