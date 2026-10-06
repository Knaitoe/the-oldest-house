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
            if(occupiedSecond(clock,55+Math.floorMod(entry.getKey()*7,18))){
                for(var p:entry.getValue()){
                    var at=p.position().add(0,-8,0);
                    var voice=Holder.direct(SoundEvent.createVariableRangeEvent(Growl.Kind.BELOW.sound()));
                    // The existing Minotaur recording, heard below the tread; no GrowlChanges side effects.
                    p.connection.send(new ClientboundSoundPacket(voice,SoundSource.HOSTILE,at.x,at.y,at.z,1.25F,.76F,clock.getInt("Pulses")));
                    HousePackets.send(p,new NovelScenePayload(13,0,"",0,.38F));
                    level.sendParticles(p,new BlockParticleOption(ParticleTypes.FALLING_DUST,Blocks.DEEPSLATE.defaultBlockState()),false,
                            p.getX(),p.getY()+3,p.getZ(),14,2,.3,2,.02);
                }
                clock.putInt("Pulses",clock.getInt("Pulses")+1);
            }
            data.setStateEntry(STATE,key,clock);
        }
    }
}
