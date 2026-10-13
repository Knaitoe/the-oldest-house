package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LakeHouseRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

/** The reader's exhaled bubbles hang still, then fade. There is no explanatory monster. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class LakeHouseBubble extends TextureSheetParticle {
    private LakeHouseBubble(ClientLevel l,double x,double y,double z,SpriteSet sprites){super(l,x,y,z);pickSprite(sprites);lifetime=70;gravity=0;hasPhysics=false;xd=yd=zd=0;quadSize=.035F;alpha=.65F;}
    @Override public ParticleRenderType getRenderType(){return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;}
    @Override public void tick(){xo=x;yo=y;zo=z;if(age++>=lifetime)remove();alpha=.65F*(1-age/(float)lifetime);}
    @SubscribeEvent public static void register(RegisterParticleProvidersEvent e){e.registerSpriteSet(LakeHouseRegistry.STILL_BUBBLE.get(),sprites->(type,level,x,y,z,dx,dy,dz)->new LakeHouseBubble(level,x,y,z,sprites));}
}
