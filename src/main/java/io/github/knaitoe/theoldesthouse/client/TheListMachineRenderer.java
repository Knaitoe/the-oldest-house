package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.TheListBlock;
import io.github.knaitoe.theoldesthouse.labyrinth.TheListMachineBlock;
import io.github.knaitoe.theoldesthouse.labyrinth.TheListMachineBlock.Machine;
import io.github.knaitoe.theoldesthouse.labyrinth.TheListMachines;
import io.github.knaitoe.theoldesthouse.labyrinth.TheListRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * 0.4.701, The list (docs/THE_LIST_0_4_701.md): the pasture's machines in motion. The windmill's wheel turns on its hub in front of the
 * head; a pumpjack's beam rocks on its saddle with the horsehead at its front, the cranks and their weights go round on the gearbox's
 * shaft, the pitman arms run from the crank pins up to the equalizer, and the rod rises and falls under the horsehead on its bridle. Each
 * part is a model of the Lamesa fitting drawn facing north, set where TheListMachines puts it for the level's time.
 */
public final class TheListMachineRenderer implements BlockEntityRenderer<TheListMachineBlock.Turning> {
    public TheListMachineRenderer(BlockEntityRendererProvider.Context context){}
    @Override public void render(TheListMachineBlock.Turning t,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        var s=t.getBlockState();if(!s.is(TheListRegistry.MACHINE.get()))return;
        long time=t.getLevel()==null?0:t.getLevel().getGameTime();
        draw(pose,buffers,s.getValue(TheListMachineBlock.FACING),s.getValue(TheListMachineBlock.MACHINE),time,partial,t.getBlockPos(),light,overlay);
    }
    private static void part(PoseStack pose,MultiBufferSource buffers,BlockState s,int light,int overlay){
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(s,pose,buffers,light,overlay);
    }
    private static BlockState jack(int stage){return TheListBlock.of(TheListBlock.Kind.PUMPJACK,Direction.NORTH,stage);}
    /** One machine's moving parts at a time, in its block's frame. */
    public static void draw(PoseStack pose,MultiBufferSource buffers,Direction facing,Machine m,long time,float partial,BlockPos at,int light,int overlay){
        pose.pushPose();
        pose.translate(.5,0,.5);pose.mulPose(Axis.YP.rotationDegrees(180-facing.toYRot()));pose.translate(-.5,0,-.5);
        if(m==Machine.CABLE){pose.popPose();return;}
        if(m==Machine.WINDMILL){
            // The wheel's hub is half a block up, an eighth of a block in front of the head.
            pose.pushPose();
            pose.translate(.5,.5,-.125);pose.mulPose(Axis.ZP.rotation((float)TheListMachines.wheel(time,partial,at)));pose.translate(-.5,-.5,.125);
            part(pose,buffers,TheListBlock.of(TheListBlock.Kind.WINDMILL,Direction.NORTH,5),light,overlay);
            pose.popPose();
        }else{
            double crank=TheListMachines.crank(time,partial,at),beam=TheListMachines.beam(crank);
            // The beam's halves about the pivot; its back end up is a turn back about the x axis.
            for(int half=2;half<=3;half++){
                pose.pushPose();
                pose.translate(.5,TheListMachines.PIVOT_Y,TheListMachines.PIVOT_Z);pose.mulPose(Axis.XP.rotation((float)-beam));
                pose.translate(-.5,-.5,half==2?-2.0:1.0);
                part(pose,buffers,jack(half),light,overlay);
                pose.popPose();
            }
            // The cranks on the gearbox's shaft.
            pose.pushPose();
            pose.translate(.5,TheListMachines.CRANK_Y,TheListMachines.CRANK_Z);pose.mulPose(Axis.XP.rotation((float)crank));pose.translate(-.5,-.5,-.5);
            part(pose,buffers,jack(4),light,overlay);
            pose.popPose();
            // The pitman arms from the pins to the equalizer.
            var c=TheListMachines.crankPin(crank);var e=TheListMachines.equalizer(beam);
            pose.pushPose();
            pose.translate(.5,c[0],c[1]);pose.mulPose(Axis.XP.rotation((float)Math.atan2(e[1]-c[1],e[0]-c[0])));pose.translate(-.5,0,-.5);
            part(pose,buffers,jack(5),light,overlay);
            pose.popPose();
            // The rod and its carrier bar, and the bridle from the bar up to where it leaves the horsehead's face.
            double carrier=TheListMachines.CARRIER_Y+TheListMachines.rod(beam);
            pose.pushPose();
            pose.translate(0,carrier-1.75,TheListMachines.ROD_Z-.5);
            part(pose,buffers,jack(6),light,overlay);
            pose.popPose();
            pose.pushPose();
            pose.translate(0,carrier+.06,TheListMachines.ROD_Z-.5);pose.scale(1,(float)Math.max(.05,TheListMachines.PIVOT_Y-carrier-.06),1);
            part(pose,buffers,TheListMachineBlock.of(Machine.CABLE,Direction.NORTH),light,overlay);
            pose.popPose();
        }
        pose.popPose();
    }
    /** The wheel reaches a block and a half from its hub; the pumpjack runs three blocks before its saddle and three behind. */
    @Override public AABB getRenderBoundingBox(TheListMachineBlock.Turning t){return new AABB(t.getBlockPos()).inflate(4,4,4);}

    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Registration {
        private Registration(){}
        @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(TheListRegistry.MACHINE_ENTITY.get(),TheListMachineRenderer::new);}
    }
}
