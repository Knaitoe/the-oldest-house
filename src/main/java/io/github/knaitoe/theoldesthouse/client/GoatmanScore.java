package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.GoatmanRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.SelectMusicEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;

/** Original quiet music; the knocks stay positional and much louder than the score. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class GoatmanScore {
    private static Score sound;
    private static int retry;
    private GoatmanScore(){}
    public static boolean active(){return GoatmanClient.sceneActive();}
    public static boolean playing(){var mc=Minecraft.getInstance();return sound!=null&&mc.getSoundManager().isActive(sound);}
    @SubscribeEvent public static void tick(ClientTickEvent.Post event){
        var mc=Minecraft.getInstance();
        if(sound!=null&&(!active()||mc.level!=sound.world||mc.player!=sound.reader||sound.finished)){
            mc.getSoundManager().stop(sound);sound=null;retry=0;
        }
        if(!active())return;
        // Stop music already playing on arrival, including while the Music slider is muted.
        mc.getMusicManager().stopPlaying();
        if(sound!=null&&!mc.getSoundManager().isActive(sound)&&++retry>=40){mc.getSoundManager().stop(sound);sound=null;retry=0;}
        if(sound==null&&mc.options.getSoundSourceVolume(SoundSource.MUSIC)>0&&mc.options.getSoundSourceVolume(SoundSource.MASTER)>0){
            sound=new Score(mc.level,mc.player);retry=0;mc.getSoundManager().play(sound);
        }
    }
    @SubscribeEvent public static void choose(SelectMusicEvent event){if(active())event.setMusic(null);}
    @SubscribeEvent public static void music(PlaySoundEvent event){
        var requested=event.getSound();
        if(active()&&requested!=null&&requested.getSource()==SoundSource.MUSIC&&requested.getLocation().getNamespace().equals("minecraft"))event.setSound(null);
    }
    private static final class Score extends AbstractTickableSoundInstance {
        final ClientLevel world;final LocalPlayer reader;boolean finished;
        Score(ClientLevel world,LocalPlayer reader){super(GoatmanRegistry.SCORE.get(),SoundSource.MUSIC,RandomSource.create());
            this.world=world;this.reader=reader;looping=true;delay=0;relative=true;attenuation=Attenuation.NONE;volume=.001F;x=y=z=0;
        }
        @Override public boolean canPlaySound(){return reader.isAlive();}
        @Override public void tick(){
            if(!active()||Minecraft.getInstance().level!=world||Minecraft.getInstance().player!=reader){finished=true;stop();return;}
            float target=GoatmanClient.night()?.16F:.11F;volume=Math.min(target,volume+.003F);
        }
    }
}
