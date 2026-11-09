package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.BorrascaBike;
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
 * 0.4.435: a child's bike being ridden (BorrascaBike), drawn from three of the town's block models (tools/draw_borrasca_town_art.py).
 * The frame has no wheels, kickstand or pedals. Each wheel turns about its hub, and the cranks turn about the axle, a half turn for every
 * push. The whole bike leans over about the line its wheels touch the road.
 */
public final class BorrascaBikeRenderer extends EntityRenderer<BorrascaBike> {
    public static final ModelResourceLocation FRAME=standalone("bike_frame"),WHEEL=standalone("bike_wheel"),CRANKS=standalone("bike_cranks");
    private static ModelResourceLocation standalone(String name){return ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"block/borrasca_shop/"+name));}
    /** Where the wheels' hubs and the crank axle are in the frame, in the model's pixels (it runs along x, its front at +x). */
    private static final float[] HUBS={1.5F,14.5F};private static final float HUB_Y=5.5F,AXLE_X=7,AXLE_Y=4;

    public BorrascaBikeRenderer(EntityRendererProvider.Context c){super(c);shadowRadius=.3F;}
    @Override public ResourceLocation getTextureLocation(BorrascaBike b){return InventoryMenu.BLOCK_ATLAS;}
    @Override public void render(BorrascaBike b,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){
        draw(pose,buffers.getBuffer(Sheets.cutoutBlockSheet()),yaw,b.shownLean(partial),b.spin(partial),b.crank(partial),light);
        super.render(b,yaw,partial,pose,buffers,light);
    }

    /** The bike standing at the pose's origin, its front toward yaw, leaning that far over (one is just before it goes), its wheels and cranks turned. */
    public static void draw(PoseStack pose,VertexConsumer out,float yaw,float lean,float spin,float crank,int light){
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(-(yaw+90)));
        pose.mulPose(Axis.XP.rotationDegrees(lean*BorrascaBike.LEAN_DEGREES));
        pose.translate(-.5,0,-.5);
        part(pose,out,FRAME,light);
        for(float hub:HUBS)turned(pose,out,WHEEL,hub,HUB_Y,spin,light);
        turned(pose,out,CRANKS,AXLE_X,AXLE_Y,crank,light);
        pose.popPose();
    }
    /** A part drawn about the middle of its block, turned about the axle at (x, y) in the frame's pixels; rolling forward turns it clockwise seen from the right. */
    private static void turned(PoseStack pose,VertexConsumer out,ModelResourceLocation model,float x,float y,float angle,int light){
        pose.pushPose();pose.translate(x/16F,y/16F,.5F);pose.mulPose(Axis.ZP.rotation(-angle));pose.translate(-.5F,-.5F,-.5F);part(pose,out,model,light);pose.popPose();
    }
    private static void part(PoseStack pose,VertexConsumer out,ModelResourceLocation model,int light){
        var mc=Minecraft.getInstance();
        mc.getBlockRenderer().getModelRenderer().renderModel(pose.last(),out,null,mc.getModelManager().getModel(model),1F,1F,1F,light,OverlayTexture.NO_OVERLAY,ModelData.EMPTY,null);
    }
}
