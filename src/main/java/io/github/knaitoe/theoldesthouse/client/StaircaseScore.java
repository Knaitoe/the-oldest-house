package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.FinaleArchitecture;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** An original sparse score follows the listener; one instance clears on exit, death or reconnect. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class StaircaseScore {
    private static Score sound;
    private StaircaseScore(){}
    public static boolean at(BlockPos origin,BlockPos listener){
        if(origin==null||listener==null||!FinaleArchitecture.contains(origin,listener))return false;
        var base=FinaleArchitecture.base(origin);
        return Math.abs(listener.getX()-base.getX())<=FinaleArchitecture.SHAFT_RADIUS
                &&Math.abs(listener.getZ()-base.getZ())<=FinaleArchitecture.SHAFT_RADIUS;
    }
    private static boolean inside(){var mc=Minecraft.getInstance();return mc.level!=null&&mc.player!=null&&mc.player.isAlive()&&!mc.player.isSpectator()
            &&mc.level.dimension().equals(HouseDimensions.INTERIOR)&&at(HouseSightlineState.origin(),mc.player.blockPosition());}
    /**
     * 0.4.660: the listener has come off the stair's last tread into the chamber before the cell (FinaleArchitecture.atFoot). The
     * score dies away there over FADE_TICKS and does not start again until they are back on the stair; nothing else plays.
     */
    public static boolean foot(){var mc=Minecraft.getInstance();var origin=HouseSightlineState.origin();return origin!=null&&mc.level!=null&&mc.player!=null&&mc.player.isAlive()
            &&!mc.player.isSpectator()&&mc.level.dimension().equals(HouseDimensions.INTERIOR)&&FinaleArchitecture.atFoot(origin,mc.player.position());}
    public static final int FADE_TICKS=40;
    private static int retry;
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        var mc=Minecraft.getInstance();boolean foot=foot();
        if(sound!=null&&(mc.level!=sound.world||mc.player!=sound.reader||sound.finished||!foot&&!inside()&&!StaircaseLeakClient.active())){mc.getSoundManager().stop(sound);sound=null;}
        // The engine can drop the score (music muted and restored, a sound reload); start it again once it can be heard.
        if(sound!=null&&!mc.getSoundManager().isActive(sound)&&++retry>=40){retry=0;sound=null;}
        if(sound==null&&inside()&&!foot&&mc.options.getSoundSourceVolume(SoundSource.MUSIC)>0&&mc.options.getSoundSourceVolume(SoundSource.MASTER)>0){
            retry=0;mc.getMusicManager().stopPlaying();sound=new Score(mc.level,mc.player);mc.getSoundManager().play(sound);}
    }
    /** No game music starts over the score while the listener is in the shaft, nor in the silence at its foot. */
    @SubscribeEvent public static void music(net.neoforged.neoforge.client.event.SelectMusicEvent e){if(inside()||foot()||StaircaseLeakClient.active())e.setMusic(null);}
    private static final class Score extends AbstractTickableSoundInstance {
        final ClientLevel world;final LocalPlayer reader;boolean finished;
        Score(ClientLevel world,LocalPlayer reader){super(LabyrinthRegistry.STAIRCASE_SCORE.get(),SoundSource.MUSIC,RandomSource.create());
            this.world=world;this.reader=reader;looping=true;delay=0;relative=true;attenuation=Attenuation.NONE;volume=.28F;x=y=z=0;
        }
        @Override public boolean canPlaySound(){return reader.isAlive();}
        @Override public void tick(){if(Minecraft.getInstance().level!=world||Minecraft.getInstance().player!=reader){finished=true;stop();return;}if(foot()){volume=Math.max(0,volume-.28F/FADE_TICKS);if(volume==0){finished=true;stop();}}else if(inside())volume=Math.min(.28F,volume+.028F);else if(StaircaseLeakClient.active()){volume=Math.max(0,volume-.028F);if(volume==0){finished=true;stop();}}else{finished=true;stop();}}
    }
}
