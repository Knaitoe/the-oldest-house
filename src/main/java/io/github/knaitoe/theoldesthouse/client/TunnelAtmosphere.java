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

/** Owner-local road wash through the concrete; no vanilla score can start over the pursuit. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class TunnelAtmosphere {
    private static Traffic sound;private static int retry;
    private TunnelAtmosphere(){}
    public static boolean active(){
        var mc=Minecraft.getInstance();if(!UncannyClient.room().equals("tunnel_periphery")||mc.player==null)return false;
        var b=UncannyClient.base();var at=mc.player.position().subtract(b.getX(),b.getY(),b.getZ());return Math.abs(at.x)<5&&at.y> -2&&at.y<5&&at.z> -148&&at.z<4;
    }
    public static boolean playing(){var mc=Minecraft.getInstance();return sound!=null&&mc.getSoundManager().isActive(sound);}
    public static void clear(){if(sound!=null)Minecraft.getInstance().getSoundManager().stop(sound);sound=null;retry=0;}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        var mc=Minecraft.getInstance();if(!active()){clear();return;}mc.getMusicManager().stopPlaying();
        if(sound==null){sound=new Traffic();mc.getSoundManager().play(sound);}
        else if(mc.options.getSoundSourceVolume(SoundSource.MASTER)>0&&mc.options.getSoundSourceVolume(SoundSource.AMBIENT)>0&&!playing()){if(++retry>40)clear();}else retry=0;
    }
    @SubscribeEvent public static void sound(PlaySoundEvent e){if(active()&&e.getSound()!=null&&e.getSound().getSource()==SoundSource.MUSIC&&e.getSound().getLocation().getNamespace().equals("minecraft"))e.setSound(null);}
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){clear();}
    private static final class Traffic extends AbstractTickableSoundInstance {
        Traffic(){super(UncannyRegistry.TUNNEL_TRAFFIC.get(),SoundSource.AMBIENT,RandomSource.create());looping=true;delay=0;attenuation=Attenuation.NONE;volume=.001F;}
        @Override public boolean canStartSilent(){return true;}
        @Override public void tick(){if(!active()){stop();return;}var mc=Minecraft.getInstance();var p=mc.player;double t=mc.level.getGameTime()/20.;
            // The source travels beyond the wall; no in-world vehicle or approaching monster is implied.
            x=p.getX()+Math.sin(t*.28)*5;y=p.getY()+2;z=p.getZ()+Math.cos(t*.28)*12;
            volume+=(.23F-volume)*.08F;
        }
    }
}
