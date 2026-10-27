package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.UncannyRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;

/** The audible gust is the same force driving the native fitting and floodlight. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class FoyerAtmosphere {
    private static Wind wind;private static int retry;
    private FoyerAtmosphere(){}
    public static boolean active(){return UncannyClient.room().equals("motion_light");}
    public static void clear(){if(wind!=null)Minecraft.getInstance().getSoundManager().stop(wind);wind=null;retry=0;}
    @SubscribeEvent public static void tick(ClientTickEvent.Post ignored){var mc=Minecraft.getInstance();if(!active()){clear();return;}mc.getMusicManager().stopPlaying();if(wind==null){wind=new Wind();mc.getSoundManager().play(wind);}else if(mc.options.getSoundSourceVolume(SoundSource.MASTER)>0&&mc.options.getSoundSourceVolume(SoundSource.AMBIENT)>0&&!mc.getSoundManager().isActive(wind)){if(++retry>40)clear();}else retry=0;}
    @SubscribeEvent public static void music(PlaySoundEvent e){if(active()&&e.getSound()!=null&&e.getSound().getSource()==SoundSource.MUSIC&&e.getSound().getLocation().getNamespace().equals("minecraft"))e.setSound(null);}
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut ignored){clear();}
    private static final class Wind extends AbstractTickableSoundInstance {
        Wind(){super(UncannyRegistry.FOYER_WIND.get(),SoundSource.AMBIENT,RandomSource.create());looping=true;delay=0;attenuation=Attenuation.NONE;volume=.001F;}
        @Override public boolean canStartSilent(){return true;}
        @Override public void tick(){if(!active()){stop();return;}var b=UncannyClient.base();x=b.getX()+.5;y=b.getY()+3;z=b.getZ()-19.5;float target=UncannyClient.foyerWind()*.65F;volume+=(target-volume)*.22F;}
    }
}
