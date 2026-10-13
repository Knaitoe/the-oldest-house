package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import io.github.knaitoe.theoldesthouse.network.LakeHouseViewPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;

/** The deep lake is dark; the personal finale lights it. The earned helmet narrows and muffles the outside world. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class LakeHouseAtmosphere {
    private static int state;
    private LakeHouseAtmosphere(){}
    public static void accept(LakeHouseViewPayload p){state=p.state();}
    private static boolean lake(){var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null||mc.level.dimension()!=HouseDimensions.OUTSIDE)return false;var place=LabyrinthPlaces.placeAt(HouseSightlineState.origin(),mc.player.blockPosition());return place==LabyrinthPlace.LAKE_HOUSE||place==LabyrinthPlace.LAKE_HOUSE_EMPTY;}
    public static long time(long normal){return lake()&&(state&4)!=0?0:normal;}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){if(!lake())state=0;}
    @SubscribeEvent public static void fog(ViewportEvent.RenderFog e){
        var mc=Minecraft.getInstance();if(!lake()||mc.player==null||!mc.player.isUnderWater()||(state&2)!=0)return;
        var place=LabyrinthPlaces.placeAt(HouseSightlineState.origin(),mc.player.blockPosition());var b=LabyrinthPlaces.base(HouseSightlineState.origin(),place);double depth=b.getY()-mc.player.getEyeY();
        if(depth<15)return;e.setNearPlaneDistance(0);e.setFarPlaneDistance((float)Math.max(4,12-(depth-15)*.6));e.setCanceled(true);
    }
    @SubscribeEvent public static void color(ViewportEvent.ComputeFogColor e){var mc=Minecraft.getInstance();if(!lake()||mc.player==null||!mc.player.isUnderWater())return;var place=LabyrinthPlaces.placeAt(HouseSightlineState.origin(),mc.player.blockPosition());var b=LabyrinthPlaces.base(HouseSightlineState.origin(),place);var r=mc.player.position().subtract(net.minecraft.world.phys.Vec3.atLowerCornerOf(b));if(r.y<-37&&r.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(LakeHouseInterior.POOL))<64){e.setRed(e.getRed()*.8F+.08F);e.setGreen(e.getGreen()*.75F+.04F);e.setBlue(e.getBlue()*.5F);return;}if(r.z<-125&&(state&2)==0){e.setRed(e.getRed()*.45F);e.setGreen(e.getGreen()*.65F);e.setBlue(e.getBlue()*.57F);}}
    @SubscribeEvent public static void sound(PlaySoundEvent e){var mc=Minecraft.getInstance();if(mc.player!=null&&mc.level!=null&&!HouseDimensions.isHouseDimension(mc.level.dimension())&&LakeHouseArtifactItem.helmet(mc.player)&&e.getSound()!=null&&!(e.getSound() instanceof Muffled))e.setSound(e.getSound() instanceof net.minecraft.client.resources.sounds.TickableSoundInstance t?new MuffledLoop(t):new Muffled(e.getSound()));}
    private static final class MuffledLoop extends Muffled implements net.minecraft.client.resources.sounds.TickableSoundInstance {
        private final net.minecraft.client.resources.sounds.TickableSoundInstance loop;MuffledLoop(net.minecraft.client.resources.sounds.TickableSoundInstance s){super(s);loop=s;}
        @Override public boolean isStopped(){return loop.isStopped();}
        @Override public void tick(){loop.tick();}
    }
    private static class Muffled implements SoundInstance {
        protected final SoundInstance sound;Muffled(SoundInstance sound){this.sound=sound;}
        @Override public ResourceLocation getLocation(){return sound.getLocation();}
        @Override public WeighedSoundEvents resolve(SoundManager m){return sound.resolve(m);}
        @Override public net.minecraft.client.resources.sounds.Sound getSound(){return sound.getSound();}
        @Override public SoundSource getSource(){return sound.getSource();}
        @Override public boolean isLooping(){return sound.isLooping();}
        @Override public boolean isRelative(){return sound.isRelative();}
        @Override public int getDelay(){return sound.getDelay();}
        @Override public float getVolume(){return sound.getVolume()*.45F;}
        @Override public float getPitch(){return sound.getPitch()*.88F;}
        @Override public double getX(){return sound.getX();}
        @Override public double getY(){return sound.getY();}
        @Override public double getZ(){return sound.getZ();}
        @Override public Attenuation getAttenuation(){return sound.getAttenuation();}
        @Override public boolean canStartSilent(){return sound.canStartSilent();}
        @Override public boolean canPlaySound(){return sound.canPlaySound();}
    }
}
