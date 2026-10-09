package io.github.knaitoe.theoldesthouse.client;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

/** The cabin storm's own particles: driven rain that splashes where it lands, and leaves torn off by the wind. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class CabinWeatherParticles {
    private CabinWeatherParticles(){}
    @SubscribeEvent public static void providers(RegisterParticleProvidersEvent e){
        e.registerSpriteSet(LiteraryRegistry.CABIN_RAIN.get(),RainProvider::new);
        e.registerSpriteSet(LiteraryRegistry.CABIN_LEAF.get(),LeafProvider::new);
    }
    /** A streak falling fast and slanted by the wind; it ends in a splash on whatever it hits. */
    static final class Rain extends TextureSheetParticle {
        Rain(ClientLevel level,double x,double y,double z,double vx,double vy,double vz,SpriteSet sprites){
            super(level,x,y,z);pickSprite(sprites);xd=vx;yd=vy;zd=vz;gravity=0;friction=1;lifetime=80;hasPhysics=true;
            quadSize=.2F+random.nextFloat()*.14F;alpha=.55F+random.nextFloat()*.25F;setColor(.82F,.87F,.96F);
        }
        @Override public void tick(){
            xo=x;yo=y;zo=z;
            if(age++>=lifetime){remove();return;}
            double ex=x+xd,ey=y+yd,ez=z+zd;move(xd,yd,zd);
            // Wherever it was stopped short, it struck something.
            if(onGround||Math.abs(x-ex)>1e-4||Math.abs(y-ey)>1e-4||Math.abs(z-ez)>1e-4){if(random.nextInt(3)==0)level.addParticle(ParticleTypes.RAIN,x,y+.05,z,0,0,0);remove();}
        }
        @Override public ParticleRenderType getRenderType(){return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;}
    }
    /** A leaf blown along near the ground, turning over as it goes. */
    static final class Leaf extends TextureSheetParticle {
        private final float spin;
        Leaf(ClientLevel level,double x,double y,double z,double vx,double vy,double vz,SpriteSet sprites){
            super(level,x,y,z);pickSprite(sprites);xd=vx;yd=vy;zd=vz;gravity=.06F;friction=.99F;lifetime=70+random.nextInt(60);hasPhysics=true;
            quadSize=.06F+random.nextFloat()*.06F;spin=(random.nextFloat()-.5F)*.6F;roll=random.nextFloat()*6.28F;oRoll=roll;
        }
        @Override public void tick(){oRoll=roll;roll+=spin*(onGround?.2F:1);yd+=Math.sin(age*.35)*.006;super.tick();}
        @Override public ParticleRenderType getRenderType(){return ParticleRenderType.PARTICLE_SHEET_OPAQUE;}
    }
    record RainProvider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override public Particle createParticle(SimpleParticleType type,ClientLevel level,double x,double y,double z,double vx,double vy,double vz){return new Rain(level,x,y,z,vx,vy,vz,sprites);}
    }
    record LeafProvider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override public Particle createParticle(SimpleParticleType type,ClientLevel level,double x,double y,double z,double vx,double vy,double vz){return new Leaf(level,x,y,z,vx,vy,vz,sprites);}
    }
}
