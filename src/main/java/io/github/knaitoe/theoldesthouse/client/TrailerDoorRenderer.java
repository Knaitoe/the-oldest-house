package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.labyrinth.TrailerDoorBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;

public final class TrailerDoorRenderer implements BlockEntityRenderer<TrailerDoorBlockEntity> {
    public TrailerDoorRenderer(BlockEntityRendererProvider.Context c){}
    @Override public void render(TrailerDoorBlockEntity door,float partial,PoseStack p,MultiBufferSource out,int light,int overlay){
        var s=door.getBlockState();if(s.getValue(DoorBlock.HALF)!=DoubleBlockHalf.LOWER)return;
        draw(s,door.recoil(partial),door.stress(),p,out,light,overlay);
    }
    public static void draw(BlockState state,float recoil,int stress,PoseStack p,MultiBufferSource out,int light,int overlay){
        var nativeDoor=Blocks.SPRUCE_DOOR.defaultBlockState().setValue(DoorBlock.FACING,state.getValue(DoorBlock.FACING)).setValue(DoorBlock.HINGE,state.getValue(DoorBlock.HINGE)).setValue(DoorBlock.OPEN,state.getValue(DoorBlock.OPEN));
        p.pushPose();p.translate(.5,0,.5);p.mulPose(Axis.YP.rotationDegrees(recoil));p.translate(-.5,0,-.5);
        var renderer=Minecraft.getInstance().getBlockRenderer();
        renderer.renderSingleBlock(nativeDoor.setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER),p,out,light,overlay);
        p.translate(0,1,0);renderer.renderSingleBlock(nativeDoor.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER),p,out,light,overlay);p.popPose();
        if(stress>=8){
            // Thin dark seams in the fixed frame accumulate without changing the door's native collision.
            var wood=Blocks.DARK_OAK_PLANKS.defaultBlockState();
            for(int i=0;i<Math.min(4,stress/4);i++){p.pushPose();p.translate(.02+i*.235,1.35+i*.13,state.getValue(DoorBlock.FACING)==net.minecraft.core.Direction.SOUTH?.825:.175);p.scale(.16F,.012F,.008F);renderer.renderSingleBlock(wood,p,out,light,overlay);p.popPose();}
        }
    }
    @Override public int getViewDistance(){return 48;}
    @Override public boolean shouldRenderOffScreen(TrailerDoorBlockEntity e){return true;}
}
