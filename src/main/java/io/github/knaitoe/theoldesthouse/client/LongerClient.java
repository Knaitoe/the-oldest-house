package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.SelectMusicEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;

/**
 * 0.4.621, A Little Longer: rain over Rowan's shop for whoever is in it (on the roof and on every window; indoors only heard), and no
 * game music: the only music here will be the radio's.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class LongerClient {
    private LongerClient(){}
    static boolean here(){
        var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||!mc.level.dimension().equals(HouseDimensions.OUTSIDE)||!HouseSightlineState.hasOrigin())return false;
        return LabyrinthPlaces.placeAt(HouseSightlineState.origin(),mc.player.blockPosition())==LabyrinthPlace.LITTLE_LONGER;
    }
    public static boolean precipitating(){return com.mojang.blaze3d.systems.RenderSystem.isOnRenderThread()&&here();}
    public static float rain(net.minecraft.world.level.Level level,float nativeRain){return level==Minecraft.getInstance().level&&here()?1:nativeRain;}
    @SubscribeEvent public static void music(SelectMusicEvent e){if(here())e.setMusic(null);}
    @SubscribeEvent public static void music(PlaySoundEvent e){if(here()&&e.getSound()!=null&&e.getSound().getSource()==SoundSource.MUSIC)e.setSound(null);}
}
