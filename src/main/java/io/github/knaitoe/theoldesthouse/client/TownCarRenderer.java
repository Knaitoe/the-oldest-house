package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.labyrinth.TownCar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.model.data.ModelData;

/**
 * 0.4.484, Bike night: one of Calder Falls's cars (TownCar), drawn from the models tools/draw_calder_cars.py makes. A car is longer
 * than a model can be, so it is two, its front half and its back half, end to end; then its four wheels, turned by how far it has
 * rolled; then its headlights and tail lights, at full brightness whatever the light it stands in.
 */
public final class TownCarRenderer extends EntityRenderer<TownCar> {
    /**
     * The numbers the models were drawn with, in pixels: what was added to a car's own x to put each half in a model's reach, and
     * to its z; where its axles are along it; how high a hub is and a wheel's radius; and how far out from the middle each wheel's
     * own middle is, the fast car's back ones being wider.
     */
    public static final float FRONT=-10,BACK=26,ACROSS=8,AXLE=21,HUB=5.5F,RADIUS=5.5F,OUT=13,OUT_WIDE=12.5F;
    private static final ModelResourceLocation[][] BODY=new ModelResourceLocation[TownCar.KINDS][],LIT=new ModelResourceLocation[TownCar.KINDS][];
    private static final ModelResourceLocation WHEEL=BikeLook.standalone("calder_car/wheel"),WHEEL_WIDE=BikeLook.standalone("calder_car/wheel_wide");
    static{
        for(int k=0;k<TownCar.KINDS;k++){
            String name="calder_car/"+TownCar.NAMES[k];
            BODY[k]=new ModelResourceLocation[]{BikeLook.standalone(name+"_front"),BikeLook.standalone(name+"_back")};
            LIT[k]=new ModelResourceLocation[]{BikeLook.standalone(name+"_lit_front"),BikeLook.standalone(name+"_lit_back")};
        }
    }
    /** Every model a car is drawn from, which no block state names. */
    static void register(java.util.function.Consumer<ModelResourceLocation> to){
        for(var pair:BODY)for(var m:pair)to.accept(m);
        for(var pair:LIT)for(var m:pair)to.accept(m);
        to.accept(WHEEL);to.accept(WHEEL_WIDE);
    }
    /** Any of them that is missing, for the proof. */
    static java.util.List<ModelResourceLocation> missing(){
        var mc=Minecraft.getInstance();var none=mc.getModelManager().getMissingModel();var out=new java.util.ArrayList<ModelResourceLocation>();
        register(m->{if(mc.getModelManager().getModel(m)==none)out.add(m);});
        return out;
    }

    public TownCarRenderer(EntityRendererProvider.Context c){super(c);shadowRadius=1.3F;}
    @Override public ResourceLocation getTextureLocation(TownCar car){return InventoryMenu.BLOCK_ATLAS;}
    @Override public void render(TownCar car,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){
        draw(pose,buffers.getBuffer(Sheets.cutoutBlockSheet()),car.kind(),Mth.rotLerp(partial,car.yRotO,car.getYRot()),car.rolled(partial),light);
        super.render(car,yaw,partial,pose,buffers,light);
    }

    /** A car standing on the road at the pose's origin, its nose toward yaw, having rolled that many blocks. */
    public static void draw(PoseStack pose,VertexConsumer out,int kind,float yaw,float rolled,int light){
        kind=Mth.clamp(kind,0,TownCar.KINDS-1);
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(-(yaw+90)));
        for(int half=0;half<2;half++){
            pose.pushPose();pose.translate(-(half==0?FRONT:BACK)/16F,0,-ACROSS/16F);
            part(pose,out,BODY[kind][half],light);part(pose,out,LIT[kind][half],LightTexture.FULL_BRIGHT);
            pose.popPose();
        }
        float angle=rolled/(RADIUS/16F);
        for(int end=-1;end<=1;end+=2)for(int side=-1;side<=1;side+=2){
            boolean wide=kind==TownCar.MUSCLE&&end<0;
            pose.pushPose();pose.translate(end*AXLE/16F,HUB/16F,side*(wide?OUT_WIDE:OUT)/16F);pose.mulPose(Axis.ZP.rotation(-angle));pose.translate(-.5F,-.5F,-.5F);
            part(pose,out,wide?WHEEL_WIDE:WHEEL,light);
            pose.popPose();
        }
        pose.popPose();
    }
    private static void part(PoseStack pose,VertexConsumer out,ModelResourceLocation model,int light){
        var mc=Minecraft.getInstance();
        mc.getBlockRenderer().getModelRenderer().renderModel(pose.last(),out,null,mc.getModelManager().getModel(model),1F,1F,1F,light,OverlayTexture.NO_OVERLAY,ModelData.EMPTY,null);
    }
}
