package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;

/** The descent keeps its own ambience; footsteps and gameplay sounds remain audible. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class StaircaseSoundscape {
    private StaircaseSoundscape(){}
    @SubscribeEvent public static void sound(PlaySoundEvent event){
        var mc=Minecraft.getInstance();var sound=event.getSound();
        if(sound==null||mc.player==null||mc.level==null||!mc.level.dimension().equals(HouseDimensions.INTERIOR)
                ||!StaircaseScore.at(HouseSightlineState.origin(),mc.player.blockPosition()))return;
        var id=sound.getLocation();if(!id.getNamespace().equals("minecraft")||id.getPath().endsWith(".step"))return;
        var source=sound.getSource();
        if(source==SoundSource.AMBIENT||source==SoundSource.WEATHER||source==SoundSource.MUSIC
                ||id.getPath().contains(".ambient")||id.getPath().endsWith(".loop"))event.setSound(null);
    }
}
