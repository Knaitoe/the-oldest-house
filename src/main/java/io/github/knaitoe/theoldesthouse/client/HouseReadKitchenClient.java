package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

/**
 * The kitchen table on the client (0.4.168): the crayons in the tin, in this reader's own order and without the one they are
 * holding; the colours on the drawing; and the picture on the fridge once it is up. Every colour comes from the fitting's own
 * data, which only the reader's private copy of the kitchen carries.
 */
public final class HouseReadKitchenClient {
    private HouseReadKitchenClient(){}
    private static ResourceLocation tex(String path){return ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/"+path+".png");}
    static final ResourceLocation CRAYON=tex("entity/house_read/kitchen/crayon"),SHEET=tex("block/house_read/sheet");
    static final ResourceLocation[] MASKS={tex("entity/house_read/kitchen/sheet_face"),tex("entity/house_read/kitchen/sheet_hands"),tex("entity/house_read/kitchen/sheet_hair"),tex("entity/house_read/kitchen/sheet_clothes")};
    private static final String[] KEYS={"Face","Hands","Hair","Clothes"};

    static boolean draws(HouseReadPropBlock.Part part){return part==HouseReadPropBlock.Part.TIN_LEFT||part==HouseReadPropBlock.Part.TIN_RIGHT||part==HouseReadPropBlock.Part.SHEET||part==HouseReadPropBlock.Part.FRIDGE_TOP;}
    static void render(HouseReadPropBlock.Fitting be,PoseStack pose,MultiBufferSource out,int light){
        var state=be.getBlockState();var part=state.getValue(HouseReadPropBlock.PART);var read=be.read();
        pose.pushPose();pose.translate(.5,0,.5);pose.mulPose(Axis.YP.rotationDegrees(-state.getValue(HorizontalDirectionalBlock.FACING).toYRot()));pose.translate(-.5,0,-.5);
        switch(part){
            case TIN_LEFT,TIN_RIGHT->crayons(read,pose,out,light);
            case SHEET->{if(!state.getValue(HouseReadPropBlock.TURNED))colours(read,pose,out,light,false);}
            case FRIDGE_TOP->{if(state.getValue(HouseReadPropBlock.TURNED))hung(read,pose,out,light);}
            default->{}
        }
        pose.popPose();
    }
    /** The tin's ten crayons, lying in their rows, each with its paper wrapper and its point toward the lid. */
    static void crayons(CompoundTag read,PoseStack pose,MultiBufferSource out,int light){
        int[] colours=read.getIntArray("Colours");int taken=read.contains("Taken")?read.getInt("Taken"):-1;var vc=out.getBuffer(RenderType.entityCutoutNoCull(CRAYON));
        for(int slot=0;slot<colours.length&&slot<10;slot++){
            if(slot==taken)continue;double[] at=HouseReadKitchen.slot(slot);int rgb=HouseReadKitchen.rgb(colours[slot]);
            float x0=(float)at[0]/16,x1=(float)at[1]/16,z0=(float)at[2]/16,z1=(float)at[3]/16,mid=(z0+z1)/2;
            box(pose,vc,x0,.5f/16,z0,x1,1.7f/16,z1,rgb,light);
            box(pose,vc,x0-.05f/16,.45f/16,mid-1.2f/16,x1+.05f/16,1.75f/16,mid+1.2f/16,shade(rgb,.72f),light);
            box(pose,vc,x0+.4f/16,.7f/16,z0-.6f/16,x1-.4f/16,1.5f/16,z0,shade(rgb,.9f),light);
        }
    }
    /** The parts of the drawing that have been coloured, as crayon over the pencil lines. */
    static void colours(CompoundTag read,PoseStack pose,MultiBufferSource out,int light,boolean upright){
        for(int i=0;i<KEYS.length;i++){
            if(!read.contains(KEYS[i])||read.getInt(KEYS[i])<0)continue;var vc=out.getBuffer(RenderType.entityTranslucent(MASKS[i]));int rgb=HouseReadKitchen.rgb(read.getInt(KEYS[i]));
            if(upright)page(pose,vc,15.07f+i*.01f,rgb,light);else flat(pose,vc,(.3f+i*.02f)/16,rgb,light);
        }
    }
    /** The picture on the upper door, under a magnet: the page, then its colours. */
    static void hung(CompoundTag read,PoseStack pose,MultiBufferSource out,int light){
        page(pose,out.getBuffer(RenderType.entityCutoutNoCull(SHEET)),15.05f,0xffffff,light);colours(read,pose,out,light,true);
        box(pose,out.getBuffer(RenderType.entityCutoutNoCull(CRAYON)),6.5f/16,11.8f/16,15f/16,8f/16,12.8f/16,15.6f/16,0xc4473a,light);
    }
    private static final float PX0=2.5f/16,PX1=11.5f/16,PY0=3f/16,PY1=12f/16;
    /** A page standing on the fridge's front face, its top edge up. */
    private static void page(PoseStack pose,VertexConsumer vc,float z,int rgb,int light){
        var p=pose.last();float zz=z/16;
        vertex(p,vc,PX0,PY0,zz,0,1,rgb,0,0,1,light);vertex(p,vc,PX1,PY0,zz,1,1,rgb,0,0,1,light);vertex(p,vc,PX1,PY1,zz,1,0,rgb,0,0,1,light);vertex(p,vc,PX0,PY1,zz,0,0,rgb,0,0,1,light);
    }
    /** A page lying on the table: the texture's top row away from the chair. */
    private static void flat(PoseStack pose,VertexConsumer vc,float y,int rgb,int light){
        var p=pose.last();
        vertex(p,vc,0,y,0,0,0,rgb,0,1,0,light);vertex(p,vc,0,y,1,0,1,rgb,0,1,0,light);vertex(p,vc,1,y,1,1,1,rgb,0,1,0,light);vertex(p,vc,1,y,0,1,0,rgb,0,1,0,light);
    }
    private static int shade(int rgb,float f){return (int)((rgb>>16&255)*f)<<16|(int)((rgb>>8&255)*f)<<8|(int)((rgb&255)*f);}
    private static void vertex(PoseStack.Pose p,VertexConsumer vc,float x,float y,float z,float u,float v,int rgb,float nx,float ny,float nz,int light){
        vc.addVertex(p,x,y,z).setColor((rgb>>16&255)/255f,(rgb>>8&255)/255f,(rgb&255)/255f,1f).setUv(u,v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p,nx,ny,nz);
    }
    /** A small box in one colour. */
    static void box(PoseStack pose,VertexConsumer vc,float x0,float y0,float z0,float x1,float y1,float z1,int rgb,int light){
        var p=pose.last();
        vertex(p,vc,x0,y1,z0,0,0,rgb,0,1,0,light);vertex(p,vc,x0,y1,z1,0,1,rgb,0,1,0,light);vertex(p,vc,x1,y1,z1,1,1,rgb,0,1,0,light);vertex(p,vc,x1,y1,z0,1,0,rgb,0,1,0,light);
        vertex(p,vc,x0,y0,z0,0,0,rgb,0,-1,0,light);vertex(p,vc,x1,y0,z0,1,0,rgb,0,-1,0,light);vertex(p,vc,x1,y0,z1,1,1,rgb,0,-1,0,light);vertex(p,vc,x0,y0,z1,0,1,rgb,0,-1,0,light);
        vertex(p,vc,x0,y0,z1,0,1,rgb,0,0,1,light);vertex(p,vc,x1,y0,z1,1,1,rgb,0,0,1,light);vertex(p,vc,x1,y1,z1,1,0,rgb,0,0,1,light);vertex(p,vc,x0,y1,z1,0,0,rgb,0,0,1,light);
        vertex(p,vc,x1,y0,z0,0,1,rgb,0,0,-1,light);vertex(p,vc,x0,y0,z0,1,1,rgb,0,0,-1,light);vertex(p,vc,x0,y1,z0,1,0,rgb,0,0,-1,light);vertex(p,vc,x1,y1,z0,0,0,rgb,0,0,-1,light);
        vertex(p,vc,x1,y0,z1,0,1,rgb,1,0,0,light);vertex(p,vc,x1,y0,z0,1,1,rgb,1,0,0,light);vertex(p,vc,x1,y1,z0,1,0,rgb,1,0,0,light);vertex(p,vc,x1,y1,z1,0,0,rgb,1,0,0,light);
        vertex(p,vc,x0,y0,z0,0,1,rgb,-1,0,0,light);vertex(p,vc,x0,y0,z1,1,1,rgb,-1,0,0,light);vertex(p,vc,x0,y1,z1,1,0,rgb,-1,0,0,light);vertex(p,vc,x0,y1,z0,0,0,rgb,-1,0,0,light);
    }

    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Registration {
        /** The crayon in the hand takes its colour from its dye; the paper wrapper keeps its own. */
        @SubscribeEvent public static void colours(RegisterColorHandlersEvent.Item e){
            e.register((stack,tint)->{var dye=stack.get(DataComponents.DYED_COLOR);return tint==0&&dye!=null?0xff000000|dye.rgb():-1;},HouseReadRegistry.CRAYON.get());
        }
    }
}
