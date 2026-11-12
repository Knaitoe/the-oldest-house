package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.labyrinth.ChainedBike;
import io.github.knaitoe.theoldesthouse.labyrinth.NightBike;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.model.data.ModelData;

/**
 * 0.4.511, Bike night: a caught reader's bike chained to a school fence (ChainedBike). The fence stands along the wall behind it, a
 * little out from it; the bike stands along the fence on the hall's side, leaning in on it, its front wheel buckled; and a chain
 * goes round its seat tube and through the wire, with a padlock on it.
 */
public final class ChainedBikeRenderer extends EntityRenderer<ChainedBike> {
    /** In blocks out from the wall: the fence's plane, and the bike's. How far over the bike leans on the fence, in degrees. How far along the fence its back wheel is from the fence's middle. */
    public static final float FENCE_OUT=.1F,BIKE_OUT=.36F,LEAN=9,BIKE_BACK=.55F;
    private static final ModelResourceLocation FENCE=BikeLook.standalone("bike_night/school_fence"),CHAIN=BikeLook.standalone("bike_night/bike_chain");
    static void register(java.util.function.Consumer<ModelResourceLocation> to){to.accept(FENCE);to.accept(CHAIN);}
    /** The models it is drawn from, for the proof that they are there. */
    public static ModelResourceLocation[] models(){return new ModelResourceLocation[]{FENCE,CHAIN};}

    public ChainedBikeRenderer(EntityRendererProvider.Context c){super(c);shadowRadius=0;}
    @Override public ResourceLocation getTextureLocation(ChainedBike b){return InventoryMenu.BLOCK_ATLAS;}
    @Override public void render(ChainedBike b,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){
        draw(pose,buffers.getBuffer(Sheets.cutoutBlockSheet()),b.getYRot(),b.make(),light);
        super.render(b,yaw,partial,pose,buffers,light);
    }
    /** The whole of it at the pose's origin, which is the foot of the wall it stands against; {@code yaw} is the way out from that wall. */
    public static void draw(PoseStack pose,VertexConsumer out,float yaw,NightBike.Make make,int light){
        pose.pushPose();
        // Its own z is out from the wall, and its own x along it.
        pose.mulPose(Axis.YP.rotationDegrees(-yaw));
        pose.pushPose();pose.translate(-.5F,0,FENCE_OUT-.5F);part(pose,out,FENCE,light);pose.popPose();
        var look=BikeLook.of(make);
        // The bike: along the fence, its back wheel toward the fence's one end, leaning in on the wire. (BikeLook draws a bike whose
        // yaw is -90 along the pose's own x.)
        pose.pushPose();pose.translate(-BIKE_BACK+.5F,0,BIKE_OUT);
        look.draw(pose,out,-90,-LEAN/io.github.knaitoe.theoldesthouse.labyrinth.RiddenBike.LEAN_DEGREES,0,0,0,light,false,true);
        pose.popPose();
        // The chain: from the seat tube, which is over the crank axle, back through the wire.
        float between=BIKE_OUT-FENCE_OUT;
        pose.pushPose();pose.translate(-BIKE_BACK+look.axleX()/16F,look.axleY()/16F+.12F,FENCE_OUT+between/2);pose.scale(1,1,between/(5.2F/16F));pose.translate(-.5F,-.5F,-.5F);
        part(pose,out,CHAIN,light);pose.popPose();
        pose.popPose();
    }
    private static void part(PoseStack pose,VertexConsumer out,ModelResourceLocation model,int light){
        var mc=Minecraft.getInstance();
        mc.getBlockRenderer().getModelRenderer().renderModel(pose.last(),out,null,mc.getModelManager().getModel(model),1F,1F,1F,light,OverlayTexture.NO_OVERLAY,ModelData.EMPTY,null);
    }
}
