package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.NovelRegistry;
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

/** A quiet listener-local breath, never broadcast to a peer outside the well. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class WellBreathing {
    private static Breath sound;private static int retry;
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){var mc=Minecraft.getInstance();
        if(sound!=null&&(!NovelSceneClient.wellActive()||mc.level!=sound.world||mc.player!=sound.reader)){mc.getSoundManager().stop(sound);sound=null;}
        if(sound!=null&&!mc.getSoundManager().isActive(sound)&&++retry>=40){retry=0;sound=null;}
        if(sound==null&&NovelSceneClient.wellActive()&&NovelSceneClient.wellDepth()>.2F){sound=new Breath(mc.level,mc.player);retry=0;mc.getSoundManager().play(sound);}
        if(sound!=null&&mc.options.getCameraType().isFirstPerson()&&sound.held%96>=44&&sound.held%96<=62&&sound.held%6==0){
            var look=mc.player.getLookAngle();var mouth=mc.player.getEyePosition().add(look.scale(.22)).add(0,-.12,0);
            mc.level.addParticle(net.minecraft.core.particles.ParticleTypes.CLOUD,mouth.x,mouth.y,mouth.z,look.x*.025,.008,look.z*.025);
        }
    }
    private static final class Breath extends AbstractTickableSoundInstance {
        final ClientLevel world;final LocalPlayer reader;private int held;
        Breath(ClientLevel w,LocalPlayer p){super(NovelRegistry.WELL_BREATH.get(),SoundSource.PLAYERS,RandomSource.create());world=w;reader=p;looping=true;relative=true;attenuation=Attenuation.NONE;volume=.16F;x=y=z=0;}
        @Override public boolean canPlaySound(){return reader.isAlive();}
        @Override public void tick(){if(!NovelSceneClient.wellActive()||Minecraft.getInstance().player!=reader||Minecraft.getInstance().level!=world){stop();return;}
            float strain=Math.min(1,++held/1200F);volume=.055F+NovelSceneClient.wellDepth()*.055F+strain*.22F;pitch=1+strain*.20F;
        }
    }
}
