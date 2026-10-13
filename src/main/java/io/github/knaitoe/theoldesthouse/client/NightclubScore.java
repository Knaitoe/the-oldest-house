package io.github.knaitoe.theoldesthouse.client;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
/** One room-wide loop per actual listener, cleared immediately on exit/death/reconnect. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class NightclubScore {
    private static Score sound;private static int retry;
    private NightclubScore(){}
    public static boolean inside(){var mc=Minecraft.getInstance();return mc.level!=null&&mc.player!=null&&mc.player.isAlive()&&!mc.player.isSpectator()&&HouseSightlineState.hasOrigin()&&mc.level.dimension().equals(HouseDimensions.INTERIOR)&&Nightclub.bounds(LabyrinthPlaces.base(HouseSightlineState.origin(),LabyrinthPlace.NIGHTCLUB)).contains(mc.player.position());}
    private static int volume(){var mc=Minecraft.getInstance();if(!inside())return 0;var b=LabyrinthPlaces.base(HouseSightlineState.origin(),LabyrinthPlace.NIGHTCLUB);var s=mc.level.getBlockState(b.offset(Nightclub.MIXER));return s.is(NightclubRegistry.BLOCK.get())?s.getValue(NightclubBlock.STAGE):0;}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){var mc=Minecraft.getInstance();
        if(sound!=null&&(!inside()||volume()==0||mc.level!=sound.world||mc.player!=sound.reader)){mc.getSoundManager().stop(sound);sound=null;}
        if(sound!=null&&!mc.getSoundManager().isActive(sound)&&++retry>=40){retry=0;sound=null;}
        if(sound==null&&inside()&&volume()>0&&mc.options.getSoundSourceVolume(SoundSource.RECORDS)>0&&mc.options.getSoundSourceVolume(SoundSource.MASTER)>0){retry=0;mc.getMusicManager().stopPlaying();sound=new Score(mc.level,mc.player);mc.getSoundManager().play(sound);}
    }
    @SubscribeEvent public static void music(SelectMusicEvent e){if(inside())e.setMusic(null);}
    private static final class Score extends AbstractTickableSoundInstance{
        final ClientLevel world;final LocalPlayer reader;
        Score(ClientLevel world,LocalPlayer reader){super(NightclubRegistry.BEAT.get(),SoundSource.RECORDS,RandomSource.create());this.world=world;this.reader=reader;looping=true;delay=0;relative=true;attenuation=Attenuation.NONE;volume=.7F;var b=LabyrinthPlaces.base(HouseSightlineState.origin(),LabyrinthPlace.NIGHTCLUB);x=0;y=0;z=0;}
        @Override public void tick(){if(!inside()||Minecraft.getInstance().level!=world||Minecraft.getInstance().player!=reader){stop();return;}volume=NightclubScore.volume()==2?.3F:.7F;}
    }
}
