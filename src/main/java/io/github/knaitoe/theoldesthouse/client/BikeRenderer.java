package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.knaitoe.theoldesthouse.labyrinth.NightBike;
import io.github.knaitoe.theoldesthouse.labyrinth.RiddenBike;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

/**
 * A child's bike being ridden: Borrasca's two (0.4.435) and Bike night's six (0.4.463), each drawn from its own look (BikeLook) with
 * its lean, the line it is travelling along, how far it has rolled and its cranks.
 */
public final class BikeRenderer extends EntityRenderer<RiddenBike> {
    public BikeRenderer(EntityRendererProvider.Context c){super(c);shadowRadius=.3F;}
    @Override public ResourceLocation getTextureLocation(RiddenBike b){return InventoryMenu.BLOCK_ATLAS;}
    @Override public void render(RiddenBike b,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){
        BikeLook look=b instanceof NightBike night?BikeLook.of(night.make()):b instanceof io.github.knaitoe.theoldesthouse.labyrinth.PackBike pack?BikeLook.pack(pack.look()):BikeLook.BORRASCA;
        if(b instanceof io.github.knaitoe.theoldesthouse.labyrinth.PackBike pack&&pack.laid()!=0)laid(look,pose,buffers.getBuffer(Sheets.cutoutBlockSheet()),yaw,pack.laid(),b.rolled(partial),b.crank(partial),light);
        else look.draw(pose,buffers.getBuffer(Sheets.cutoutBlockSheet()),yaw,b.shownLean(partial),b.shownPitch(partial),b.rolled(partial),b.crank(partial),light,b instanceof NightBike night&&night.lit()>0);
        super.render(b,yaw,partial,pose,buffers,light);
    }
    /** 0.4.509: a bike lying on that side where it was dropped (PackBike.lay), its lower pedal and the end of its bars on the ground. */
    public static void laid(BikeLook look,PoseStack pose,VertexConsumer out,float yaw,int side,float rolled,float crank,int light){
        pose.pushPose();pose.translate(0,io.github.knaitoe.theoldesthouse.labyrinth.PackBike.LAID_LIFT,0);
        look.draw(pose,out,yaw,side*io.github.knaitoe.theoldesthouse.labyrinth.PackBike.LAID_DEGREES/RiddenBike.LEAN_DEGREES,0,rolled,crank,light,false);
        pose.popPose();
    }

    /** Patrick's or Kendra's bike standing at the pose's origin, its front toward yaw, leaning that far over, its wheels (by the angle they have turned) and cranks turned. */
    public static void draw(PoseStack pose,VertexConsumer out,float yaw,float lean,float spin,float crank,int light){
        BikeLook.BORRASCA.draw(pose,out,yaw,lean,0,spin*RiddenBike.WHEEL,crank,light,false);
    }
}
