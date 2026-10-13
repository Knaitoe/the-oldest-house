package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LakeHouseRegistry;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import org.joml.Quaternionf;

/** Native floor-facing wet marks, sent only to the reader and fading without changing home blocks. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class LakeHouseFootprint extends TextureSheetParticle {
    private final float yaw;
    private LakeHouseFootprint(ClientLevel l,double x,double y,double z,double yaw,SpriteSet sprites){
        super(l,x,y,z);pickSprite(sprites);this.yaw=(float)yaw;lifetime=600;gravity=0;hasPhysics=false;xd=yd=zd=0;quadSize=.8F;rCol=gCol=bCol=.12F;alpha=.7F;
    }
    @Override public ParticleRenderType getRenderType(){return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;}
    @Override public void render(VertexConsumer v,Camera c,float partial){renderRotatedQuad(v,c,new Quaternionf().rotateX(-(float)Math.PI/2).rotateZ(yaw),partial);}
    @Override public void tick(){xo=x;yo=y;zo=z;if(age++>=lifetime)remove();alpha=.7F*Math.min(1,(lifetime-age)/100F);}
    @SubscribeEvent public static void register(RegisterParticleProvidersEvent e){e.registerSpriteSet(LakeHouseRegistry.WET_FOOTPRINT.get(),s->(type,l,x,y,z,dx,dy,dz)->new LakeHouseFootprint(l,x,y,z,dx,s));}
}
