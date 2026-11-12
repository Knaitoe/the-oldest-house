package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.NightBike;
import io.github.knaitoe.theoldesthouse.labyrinth.RiddenBike;
import java.util.EnumMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.data.ModelData;

/**
 * How a ridden bike is drawn (0.4.435, made general in 0.4.463): a frame with no wheels, kickstand or pedals, a wheel at each hub
 * turning with how far the bike has rolled, and the cranks turning about the axle, a half turn for every push. The whole bike leans
 * over about the line its wheels touch the road, and noses up or down with the line it is travelling along. The models are block
 * models drawn in code (Borrasca's by tools/draw_borrasca_town_art.py, Bike night's by tools/draw_bike_night_art.py), running along x
 * with the front at +x; the numbers are in the models' pixels.
 */
public record BikeLook(ModelResourceLocation frame,ModelResourceLocation frontWheel,ModelResourceLocation backWheel,ModelResourceLocation cranks,@Nullable ModelResourceLocation glow,
                       float backX,float backY,float frontX,float frontY,float axleX,float axleY,float backRadius,float frontRadius) {
    static ModelResourceLocation standalone(String path){return ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"block/"+path));}

    /** Patrick's bike and Kendra's. */
    public static final BikeLook BORRASCA=new BikeLook(standalone("borrasca_shop/bike_frame"),standalone("borrasca_shop/bike_wheel"),standalone("borrasca_shop/bike_wheel"),standalone("borrasca_shop/bike_cranks"),null,
        1.5F,5.5F,14.5F,5.5F,7,4,5.5F,5.5F);
    private static final ModelResourceLocation NIGHT_CRANKS=standalone("bike_night/cranks"),LAMP_GLOW=standalone("bike_night/lamp_glow");
    private static final Map<NightBike.Make,BikeLook> NIGHT=new EnumMap<>(NightBike.Make.class);
    static{
        for(var m:NightBike.Make.values()){
            var front=standalone("bike_night/"+m.id()+"_wheel");
            NIGHT.put(m,new BikeLook(standalone("bike_night/"+m.id()+"_frame"),front,m==NightBike.Make.RATTLER?standalone("bike_night/"+m.id()+"_wheel_back"):front,NIGHT_CRANKS,
                m==NightBike.Make.LAMP?LAMP_GLOW:null,m.backX(),m.backY(),m.frontX(),m.frontY(),m.axleX(),m.axleY(),m.backRadius(),m.frontRadius()));
        }
    }
    public static BikeLook of(NightBike.Make make){return NIGHT.get(make);}
    /** 0.4.510: each of the six left out and rusted (PackBike.RUSTED): the same pieces, in rust. */
    private static final Map<NightBike.Make,BikeLook> RUST=new EnumMap<>(NightBike.Make.class);
    static{
        var cranks=standalone("bike_night/cranks_rust");
        for(var m:NightBike.Make.values()){
            var front=standalone("bike_night/"+m.id()+"_rust_wheel");
            RUST.put(m,new BikeLook(standalone("bike_night/"+m.id()+"_rust_frame"),front,m==NightBike.Make.RATTLER?standalone("bike_night/"+m.id()+"_rust_wheel_back"):front,cranks,
                null,m.backX(),m.backY(),m.frontX(),m.frontY(),m.axleX(),m.axleY(),m.backRadius(),m.frontRadius()));
        }
    }
    public static BikeLook rusted(NightBike.Make make){return RUST.get(make);}
    /** 0.4.479: the bikes of the three who come to the park (PackBike), and (0.4.483) Gus's: the Wasp's frame and wheels in their own paint. */
    private static final String[] PACK_NAMES={"tully","ray","duff","gus"};
    private static final BikeLook[] PACK=new BikeLook[PACK_NAMES.length];
    static{
        var m=NightBike.Make.WASP;var wheel=standalone("bike_night/wasp_wheel");
        for(int i=0;i<PACK.length;i++)PACK[i]=new BikeLook(standalone("bike_night/"+PACK_NAMES[i]+"_frame"),wheel,wheel,NIGHT_CRANKS,null,m.backX(),m.backY(),m.frontX(),m.frontY(),m.axleX(),m.axleY(),m.backRadius(),m.frontRadius());
    }
    /** How a PackBike of that look is drawn: one of the three, or (from PackBike.MAKES up) one of the six makes. */
    public static BikeLook pack(int look){
        int first=io.github.knaitoe.theoldesthouse.labyrinth.PackBike.MAKES,rusted=io.github.knaitoe.theoldesthouse.labyrinth.PackBike.RUSTED;var makes=NightBike.Make.values();
        if(look>=rusted)return RUST.get(makes[Math.min(makes.length-1,look-rusted)]);
        if(look>=first)return NIGHT.get(makes[Math.min(makes.length-1,look-first)]);
        return PACK[Math.max(0,Math.min(PACK.length-1,look))];
    }
    /** Every model a ridden bike is drawn from, which no block state names. */
    static void register(java.util.function.Consumer<ModelResourceLocation> to){
        for(var look:new BikeLook[]{BORRASCA})look.each(to);
        for(var look:NIGHT.values())look.each(to);
        for(var look:RUST.values())look.each(to);
        ChainedBikeRenderer.register(to);
        for(var look:PACK)look.each(to);
    }
    private void each(java.util.function.Consumer<ModelResourceLocation> to){to.accept(frame);to.accept(frontWheel);to.accept(backWheel);to.accept(cranks);if(glow!=null)to.accept(glow);}

    /**
     * The bike standing at the pose's origin, its front toward yaw, leaning that far over (one is just before it goes), its nose up by
     * pitch (radians), having rolled that many blocks, its cranks turned. With {@code lit}, the lamp's lens is drawn over, full bright.
     */
    public void draw(PoseStack pose,VertexConsumer out,float yaw,float lean,float pitch,float rolled,float crank,int light,boolean lit){draw(pose,out,yaw,lean,pitch,rolled,crank,light,lit,false);}
    /** 0.4.511: how a buckled front wheel is drawn: turned out of the bike's plane about its hub, and tipped, in degrees. */
    public static final float BENT_TURN=26,BENT_TIP=13;
    /** The same, and with {@code bent} its front wheel buckled (ChainedBike). */
    public void draw(PoseStack pose,VertexConsumer out,float yaw,float lean,float pitch,float rolled,float crank,int light,boolean lit,boolean bent){
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(-(yaw+90)));
        pose.mulPose(Axis.XP.rotationDegrees(lean*RiddenBike.LEAN_DEGREES));
        // Nose up about the back wheel's foot when it climbs, as a bike lifting its front wheel does; nose down about the front's.
        if(pitch!=0){float px=((pitch>0?backX:frontX)-8)/16F;pose.translate(px,0,0);pose.mulPose(Axis.ZP.rotation(pitch));pose.translate(-px,0,0);}
        pose.translate(-.5,0,-.5);
        part(pose,out,frame,light);
        turned(pose,out,backWheel,backX,backY,rolled/(backRadius/16F),light);
        if(bent){
            pose.pushPose();pose.translate(frontX/16F,frontY/16F,.5F);pose.mulPose(Axis.YP.rotationDegrees(BENT_TURN));pose.mulPose(Axis.XP.rotationDegrees(BENT_TIP));
            pose.translate(-.5F,-.5F,-.5F);part(pose,out,frontWheel,light);pose.popPose();
        }else turned(pose,out,frontWheel,frontX,frontY,rolled/(frontRadius/16F),light);
        turned(pose,out,cranks,axleX,axleY,crank,light);
        if(lit&&glow!=null)part(pose,out,glow,LightTexture.FULL_BRIGHT);
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
