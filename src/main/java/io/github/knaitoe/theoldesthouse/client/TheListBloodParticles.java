package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.TheListRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

/**
 * 0.4.709, The list's catch (docs/THE_LIST_0_4_709.md): blood off the hay hook. Each drop is thrown, falls, and where it lands it stays a
 * moment, flattened, before it goes.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class TheListBloodParticles {
    private TheListBloodParticles(){}
    @SubscribeEvent public static void providers(RegisterParticleProvidersEvent e){e.registerSpriteSet(TheListRegistry.BLOOD.get(),Provider::new);}
    static final class Drop extends TextureSheetParticle {
        private boolean landed;
        Drop(ClientLevel level,double x,double y,double z,double vx,double vy,double vz,SpriteSet sprites){
            super(level,x,y,z);pickSprite(sprites);xd=vx;yd=vy;zd=vz;gravity=1.1F;friction=.98F;lifetime=50+random.nextInt(40);hasPhysics=true;
            quadSize=.04F+random.nextFloat()*.05F;
        }
        @Override public void tick(){
            super.tick();
            if(onGround&&!landed){landed=true;quadSize*=1.6F;xd=zd=0;}
            if(landed){xd=zd=0;}
            if(age>lifetime-12)alpha=Math.max(0,(lifetime-age)/12F);
        }
        @Override public ParticleRenderType getRenderType(){return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;}
    }
    record Provider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override public Particle createParticle(SimpleParticleType type,ClientLevel level,double x,double y,double z,double vx,double vy,double vz){return new Drop(level,x,y,z,vx,vy,vz,sprites);}
    }
}
