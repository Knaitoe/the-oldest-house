package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.common.util.TriState;

/**
 * 0.4.349, In the Tall Grass: the line of telegraph poles across the far side of the field, the same distance away from everywhere. They
 * stand a fixed way north of the eye, wherever it is, so walking toward them never brings them nearer; sideways they pass as poles do.
 * And in the grass, nobody's name shows over the stems: readers there find each other by their tracks or not at all.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class TallGrassClient {
    private TallGrassClient(){}
    /** How far north of the eye the poles stand, how far apart, how tall, and how far either side they are drawn. */
    static final double AWAY=72,APART=26;static final int TALL=10,REACH=6;

    static boolean onRoad(){
        var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||!mc.level.dimension().equals(HouseDimensions.OUTSIDE)||!HouseSightlineState.hasOrigin())return false;
        return LabyrinthPlaces.placeAt(HouseSightlineState.origin(),mc.player.blockPosition())==TallGrass.PLACE;
    }
    @SubscribeEvent public static void render(RenderLevelStageEvent e){
        if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS||!onRoad())return;
        // 0.4.555: the road the reader stands on, whichever copy it is.
        var mc=Minecraft.getInstance();var base=LabyrinthPlaces.base(SceneInstances.originAt(HouseSightlineState.origin(),mc.player.blockPosition()),TallGrass.PLACE);if(base==null)return;
        var camera=e.getCamera().getPosition();var pose=e.getPoseStack();var buffers=mc.renderBuffers().bufferSource();
        int light=LevelRenderer.getLightColor(mc.level,BlockPos.containing(camera));
        double z=camera.z-AWAY;double first=Math.floor((camera.x-base.getX())/APART)*APART+base.getX();
        var post=Blocks.SPRUCE_LOG.defaultBlockState();var arm=Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.X);
        var cap=Blocks.SPRUCE_FENCE.defaultBlockState();
        for(int i=-REACH;i<=REACH;i++){
            double x=first+i*APART;
            pose.pushPose();pose.translate(x-camera.x,base.getY()-camera.y,z-camera.z);
            pose.pushPose();pose.scale(.4F,1F,.4F);pose.translate(.75,0,.75);
            for(int y=0;y<TALL;y++){mc.getBlockRenderer().renderSingleBlock(post,pose,buffers,light,OverlayTexture.NO_OVERLAY);pose.translate(0,1,0);}
            pose.popPose();
            // The crossarm near the top, its insulators.
            pose.pushPose();pose.translate(-1,TALL-1.2,.35);pose.scale(3F,.3F,.3F);mc.getBlockRenderer().renderSingleBlock(arm,pose,buffers,light,OverlayTexture.NO_OVERLAY);pose.popPose();
            for(double ix:new double[]{-.8,1.6}){pose.pushPose();pose.translate(ix,TALL-.9,.38);pose.scale(.25F,.3F,.25F);mc.getBlockRenderer().renderSingleBlock(cap,pose,buffers,light,OverlayTexture.NO_OVERLAY);pose.popPose();}
            pose.popPose();
        }
        buffers.endBatch();
        // The wires, sagging between the insulators.
        var lines=buffers.getBuffer(RenderType.lines());var last=pose.last();
        for(int i=-REACH;i<REACH;i++)for(double ix:new double[]{-.7,1.7}){
            double x0=first+i*APART+ix,x1=x0+APART;double y=base.getY()+TALL-.6;
            for(int k=0;k<8;k++){double a=k/8.0,b=(k+1)/8.0;double sa=Math.sin(Math.PI*a)*.9,sb=Math.sin(Math.PI*b)*.9;
                float ax=(float)(x0+(x1-x0)*a-camera.x),ay=(float)(y-sa-camera.y),bx=(float)(x0+(x1-x0)*b-camera.x),by=(float)(y-sb-camera.y),zz=(float)(z+.5-camera.z);
                float nx=bx-ax,ny=by-ay;float len=(float)Math.sqrt(nx*nx+ny*ny);
                lines.addVertex(last,ax,ay,zz).setColor(.12F,.11F,.1F,.9F).setNormal(last,nx/len,ny/len,0);
                lines.addVertex(last,bx,by,zz).setColor(.12F,.11F,.1F,.9F).setNormal(last,nx/len,ny/len,0);}
        }
        buffers.endBatch(RenderType.lines());
    }
    /** No names over the grass. */
    @SubscribeEvent public static void nameTag(RenderNameTagEvent e){if(e.getEntity() instanceof Player&&onRoad())e.setCanRender(TriState.FALSE);}
}
