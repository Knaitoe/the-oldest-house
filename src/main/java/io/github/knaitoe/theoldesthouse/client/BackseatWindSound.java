package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

/** One continuous, owner-only cabin whistle, following the actual acceleration. */
final class BackseatWindSound extends AbstractTickableSoundInstance {
    BackseatWindSound(){super(BackseatRegistry.WIND.get(),SoundSource.BLOCKS,RandomSource.create());looping=true;relative=true;attenuation=Attenuation.NONE;volume=.025F;pitch=.75F;}
    void end(){stop();}
    @Override public boolean canStartSilent(){return true;}
    @Override public void tick(){
        var p=Minecraft.getInstance().player;
        if(p==null||!p.isAlive()||!(p.getVehicle() instanceof BackseatSeat seat)||seat.owner().filter(p.getUUID()::equals).isEmpty()){stop();return;}
        var car=seat.car();if(car==null||!car.sequenceEnabled||car.phase!=BackseatCarBlock.Car.REVEAL){stop();return;}
        float q=car.acceleration(0);volume=.025F+.73F*q*q;pitch=.75F+.45F*q;
    }
}
