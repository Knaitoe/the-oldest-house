package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.GoatmanRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

/** The smell made visible (0.4.53): copper motes that hang in the air, drift and fade. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class GoatmanParticles {
    private GoatmanParticles(){}
    @SubscribeEvent public static void providers(RegisterParticleProvidersEvent e){e.registerSpriteSet(GoatmanRegistry.COPPER.get(),CopperProvider::new);}
    static final class Copper extends TextureSheetParticle {
        private final float fade;
        Copper(ClientLevel level,double x,double y,double z,double vx,double vy,double vz,SpriteSet sprites){
            super(level,x,y,z);pickSprite(sprites);xd=vx;yd=vy;zd=vz;gravity=-.002F;friction=.96F;lifetime=60+random.nextInt(60);hasPhysics=false;
            quadSize=.035F+random.nextFloat()*.035F;fade=.55F+random.nextFloat()*.3F;alpha=0;
        }
        @Override public void tick(){
            super.tick();xd+=(random.nextFloat()-.5F)*.002;zd+=(random.nextFloat()-.5F)*.002;
            float life=(float)age/lifetime;alpha=fade*Math.min(1,Math.min(life*5,(1-life)*3));
        }
        @Override public ParticleRenderType getRenderType(){return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;}
    }
    record CopperProvider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override public Particle createParticle(SimpleParticleType type,ClientLevel level,double x,double y,double z,double vx,double vy,double vz){return new Copper(level,x,y,z,vx,vy,vz,sprites);}
    }
}
