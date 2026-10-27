package io.github.knaitoe.theoldesthouse.labyrinth;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
/** Choose a different recorded variation for every actual wet action. */
public final class CellarSounds {
    private static final int[] LAST={-1,-1,-1};
    private CellarSounds(){}
    private static synchronized SoundEvent next(int kind,List<DeferredHolder<SoundEvent,SoundEvent>> clips){
        int n=clips.size(),last=LAST[kind],pick=last<0?ThreadLocalRandom.current().nextInt(n):(last+1+ThreadLocalRandom.current().nextInt(n-1))%n;
        LAST[kind]=pick;return clips.get(pick).get();
    }
    public static SoundEvent grow(){return next(0,UncannyRegistry.GROW_VARIANTS);}
    public static SoundEvent tear(){return next(1,UncannyRegistry.TEAR_VARIANTS);}
    public static SoundEvent gurgle(){return next(2,UncannyRegistry.GURGLE_VARIANTS);}
}
