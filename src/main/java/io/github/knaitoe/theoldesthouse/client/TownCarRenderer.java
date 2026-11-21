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
    private static final ModelResourceLocation[][] BODY=new ModelResourceLocation[TownCar.CARS][],LIT=new ModelResourceLocation[TownCar.CARS][];
    private static final ModelResourceLocation WHEEL=BikeLook.standalone("calder_car/wheel"),WHEEL_WIDE=BikeLook.standalone("calder_car/wheel_wide");
    /**
     * 0.4.715, The list's drive-in car: the blue sedan's body with its driver's door cut out of it, its two halves, and the door, drawn
     * turned about its hinge. The door's model was moved into a model's reach by DOOR_DX and DOOR_DZ (tools/draw_calder_cars.py); its
     * hinge is at HINGE_X, HINGE_Z in the car's own pixels.
     */
    private static final ModelResourceLocation[] OPEN={BikeLook.standalone("calder_car/blue_open_front"),BikeLook.standalone("calder_car/blue_open_back")};
    private static final ModelResourceLocation DOOR=BikeLook.standalone("calder_car/blue_door");
    public static final float DOOR_DX=3,DOOR_DZ=24,HINGE_X=13,HINGE_Z=-15;
    /**
     * 0.4.735, The list's dawn bus (tools/draw_list_bus.py): five models end to end, each a fifth of its length moved into a model's reach
     * (its back end at -4), and each fifth's lamps over it; and what was added to its y and z. Its axles, how far out its wheels stand
     * and their radius: the cars' wide wheel, drawn larger.
     */
    public static final int BUS_PARTS=5;
    public static final float BUS_L=80,BUS_PART=32,BUS_DY=-12,BUS_DZ=8,BUS_REAR=-50,BUS_FRONT=54,BUS_OUT=16.3F,BUS_RADIUS=8;
    private static final ModelResourceLocation[] BUS=new ModelResourceLocation[BUS_PARTS],BUS_LIT=new ModelResourceLocation[BUS_PARTS];
    static{
        for(int k=0;k<TownCar.CARS;k++){
            String name="calder_car/"+TownCar.NAMES[k];
            BODY[k]=new ModelResourceLocation[]{BikeLook.standalone(name+"_front"),BikeLook.standalone(name+"_back")};
            LIT[k]=new ModelResourceLocation[]{BikeLook.standalone(name+"_lit_front"),BikeLook.standalone(name+"_lit_back")};
        }
        for(int i=0;i<BUS_PARTS;i++){BUS[i]=BikeLook.standalone("list_bus/part"+i);BUS_LIT[i]=BikeLook.standalone("list_bus/lit"+i);}
    }
    /** Every model a car is drawn from, which no block state names. */
    static void register(java.util.function.Consumer<ModelResourceLocation> to){
        for(var pair:BODY)for(var m:pair)to.accept(m);
        for(var pair:LIT)for(var m:pair)to.accept(m);
        to.accept(WHEEL);to.accept(WHEEL_WIDE);
        for(var m:OPEN)to.accept(m);to.accept(DOOR);
        for(var m:BUS)to.accept(m);for(var m:BUS_LIT)to.accept(m);
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
        shadowRadius=car.kind()==TownCar.CART?.4F:car.kind()==TownCar.BUS?2F:1.3F;
        draw(pose,buffers.getBuffer(Sheets.cutoutBlockSheet()),car.kind(),Mth.rotLerp(partial,car.yRotO,car.getYRot()),car.rolled(partial),light,car.lit(),car.working()?car.doorAt(partial):0);
        super.render(car,yaw,partial,pose,buffers,light);
    }

    /** A car standing on the road at the pose's origin, its nose toward yaw, having rolled that many blocks. */
    public static void draw(PoseStack pose,VertexConsumer out,int kind,float yaw,float rolled,int light){draw(pose,out,kind,yaw,rolled,light,kind<TownCar.MOVING);}
    /** 0.4.706: and with its lights on or off, whatever it is: The list's pickup stands in the pasture with its lights on. */
    public static void draw(PoseStack pose,VertexConsumer out,int kind,float yaw,float rolled,int light,boolean lit){draw(pose,out,kind,yaw,rolled,light,lit,0);}
    /** 0.4.715: and the blue sedan with its driver's door that far out from shut, in degrees. */
    public static void draw(PoseStack pose,VertexConsumer out,int kind,float yaw,float rolled,int light,boolean lit,float door){
        if(kind==TownCar.CART){cart(pose,out,yaw,light);return;}
        if(kind==TownCar.BUS){bus(pose,out,yaw,rolled,light);return;}
        boolean open=kind==TownCar.BLUE&&door>.5F;
        kind=Mth.clamp(kind,0,TownCar.CARS-1);
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(-(yaw+90)));
        for(int half=0;half<2;half++){
            pose.pushPose();pose.translate(-(half==0?FRONT:BACK)/16F,0,-ACROSS/16F);
            // 0.4.487: one that stands parked has its lights off: its lamps are drawn in the night's own light, as the rest of it is.
            part(pose,out,open?OPEN[half]:BODY[kind][half],light);part(pose,out,LIT[kind][half],lit?LightTexture.FULL_BRIGHT:light);
            pose.popPose();
        }
        if(open){
            // Out from its hinge: the back edge swings out to the car's left.
            pose.pushPose();
            pose.translate(HINGE_X/16F,0,HINGE_Z/16F);pose.mulPose(Axis.YP.rotationDegrees(-door));pose.translate(-HINGE_X/16F,0,-HINGE_Z/16F);
            pose.translate(-DOOR_DX/16F,0,-DOOR_DZ/16F);
            part(pose,out,DOOR,light);
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
    /** 0.4.735: The list's dawn bus, its nose toward yaw: its five fifths end to end, their lamps lit, and its four wheels turning. */
    private static void bus(PoseStack pose,VertexConsumer out,float yaw,float rolled,int light){
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(-(yaw+90)));
        for(int i=0;i<BUS_PARTS;i++){
            float dx=-4-(-BUS_L+BUS_PART*i);
            pose.pushPose();pose.translate(-dx/16F,-BUS_DY/16F,-BUS_DZ/16F);
            part(pose,out,BUS[i],light);part(pose,out,BUS_LIT[i],LightTexture.FULL_BRIGHT);
            pose.popPose();
        }
        float angle=rolled/(BUS_RADIUS/16F),scale=BUS_RADIUS/RADIUS;
        for(float axle:new float[]{BUS_REAR,BUS_FRONT})for(int side=-1;side<=1;side+=2){
            pose.pushPose();pose.translate(axle/16F,BUS_RADIUS/16F,side*BUS_OUT/16F);pose.mulPose(Axis.ZP.rotation(-angle));pose.scale(scale,scale,scale);pose.translate(-.5F,-.5F,-.5F);
            part(pose,out,WHEEL_WIDE,light);
            pose.popPose();
        }
        pose.popPose();
    }
    /** 0.4.488: a grocery cart, which is the town's own cart block's model, its basket toward yaw and its handle behind. */
    private static void cart(PoseStack pose,VertexConsumer out,float yaw,int light){
        var mc=Minecraft.getInstance();var state=io.github.knaitoe.theoldesthouse.labyrinth.CalderTownBlock.of(io.github.knaitoe.theoldesthouse.labyrinth.CalderTownBlock.Kind.CART,net.minecraft.core.Direction.NORTH,0);
        pose.pushPose();pose.mulPose(Axis.YP.rotationDegrees(180-yaw));pose.translate(-.5F,0,-.5F);
        mc.getBlockRenderer().getModelRenderer().renderModel(pose.last(),out,state,mc.getBlockRenderer().getBlockModel(state),1F,1F,1F,light,OverlayTexture.NO_OVERLAY,ModelData.EMPTY,null);
        pose.popPose();
    }
    private static void part(PoseStack pose,VertexConsumer out,ModelResourceLocation model,int light){
        var mc=Minecraft.getInstance();
        mc.getBlockRenderer().getModelRenderer().renderModel(pose.last(),out,null,mc.getModelManager().getModel(model),1F,1F,1F,light,OverlayTexture.NO_OVERLAY,ModelData.EMPTY,null);
    }
}
