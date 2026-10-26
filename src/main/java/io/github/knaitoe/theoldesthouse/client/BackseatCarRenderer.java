package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Draw the same fitted mesh that supplies every parked car cell's collision. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class BackseatCarRenderer implements BlockEntityRenderer<BackseatCarBlock.Car> {
    public BackseatCarRenderer(BlockEntityRendererProvider.Context ignored){}
    @SubscribeEvent public static void register(EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(BackseatRegistry.CAR_ENTITY.get(),BackseatCarRenderer::new);e.registerEntityRenderer(BackseatRegistry.SEAT.get(),NoopRenderer::new);}
    @Override public void render(BackseatCarBlock.Car car,float partial,PoseStack p,MultiBufferSource out,int light,int overlay){
        var mc=Minecraft.getInstance();if(car.owner!=null&&mc.player!=null&&!car.owner.equals(mc.player.getUUID()))return;
        float age=car.getLevel()==null?0:car.getLevel().getGameTime()+partial;
        float elapsed=car.clock+Math.max(0,age-car.syncedAt);
        float door=car.phase<BackseatCarBlock.Car.CLOSING?1:car.phase>BackseatCarBlock.Car.CLOSING?0:Math.max(0,1-elapsed/6F);
        float wiper=car.phase<BackseatCarBlock.Car.LOCKED?12+66*(.5F+.5F*(float)Math.sin(age*.13F)):12;
        p.pushPose();p.translate(.5,0,.5);BackseatArt.draw(p,out,light,overlay,door,wiper);p.popPose();
    }
    @Override public boolean shouldRenderOffScreen(BackseatCarBlock.Car car){return true;}
    @Override public AABB getRenderBoundingBox(BackseatCarBlock.Car car){var b=car.getBlockPos();return new AABB(b.getX()-3,b.getY(),b.getZ()-3,b.getX()+2,b.getY()+3,b.getZ()+4);}
}
