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
 * data, which only the reader's private copy of the kitchen carries. 0.4.196: the page itself is blank paper; its pencil lines,
 * the body and face the reader's pencil drew, the crayon over them and the pencil down the margin are all drawn here, pixel by
 * pixel from {@link HouseReadKitchen#figure(int)}, the same map the server reads a click by.
 */
public final class HouseReadKitchenClient {
    private HouseReadKitchenClient(){}
    private static ResourceLocation tex(String path){return ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/"+path+".png");}
    static final ResourceLocation CRAYON=tex("entity/house_read/kitchen/crayon"),SHEET=tex("block/house_read/sheet");
    private static final String[] KEYS={"Face","Hands","Hair","Clothes"};
    private static final char[] LETTERS={'f','h','r','c'};
    private static final int LINE=0x8e8a82,MARK=0x5e5a54,TEAR=0x5b8fd6;

    static boolean draws(HouseReadPropBlock.Part part){return part==HouseReadPropBlock.Part.TIN_LEFT||part==HouseReadPropBlock.Part.TIN_RIGHT||part==HouseReadPropBlock.Part.SHEET||part==HouseReadPropBlock.Part.FRIDGE_TOP;}
    static void render(HouseReadPropBlock.Fitting be,PoseStack pose,MultiBufferSource out,int light){
        var state=be.getBlockState();var part=state.getValue(HouseReadPropBlock.PART);var read=be.read();
        pose.pushPose();pose.translate(.5,0,.5);pose.mulPose(Axis.YP.rotationDegrees(-state.getValue(HorizontalDirectionalBlock.FACING).toYRot()));pose.translate(-.5,0,-.5);
        switch(part){
            case TIN_LEFT,TIN_RIGHT->crayons(read,pose,out,light);
            case SHEET->{if(!state.getValue(HouseReadPropBlock.TURNED)){colours(read,pose,out,light,false);if(!read.getBoolean("PencilTaken"))pencil(pose,out,light);}}
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
    /**
     * The drawing: the figure's pencil lines (the edge of each part, and a dress's legs), the parts that have been coloured as
     * crayon over them, lighter where it crosses a line and with gaps where the paper shows, then the face the pencil drew.
     */
    static void colours(CompoundTag read,PoseStack pose,MultiBufferSource out,int light,boolean upright){
        var vc=out.getBuffer(RenderType.entityTranslucent(CRAYON));var figure=HouseReadKitchen.figure(read.getInt("Body"));int mood=read.getInt("Mood");
        var marks=new java.util.HashSet<Integer>();if(mood>0&&mood<=HouseReadKitchen.FACE_MARKS.length)for(int[] m:HouseReadKitchen.FACE_MARKS[mood-1])marks.add(m[1]*16+m[0]);
        boolean sad=mood==2;
        for(int y=0;y<16;y++)for(int x=0;x<16;x++){
            char c=figure[y].charAt(x);if(c=='.')continue;boolean edge=edge(figure,x,y);
            if(edge)pixel(pose,vc,x,y,upright,0,LINE,1f,light);
            for(int i=0;i<KEYS.length;i++){
                if(LETTERS[i]!=c||!read.contains(KEYS[i])||read.getInt(KEYS[i])<0)continue;
                if(marks.contains(y*16+x)||sad&&x==HouseReadKitchen.TEAR[0]&&y==HouseReadKitchen.TEAR[1])continue;
                // Crayon strokes run on the slant, with a few gaps where the paper shows; over the pencil line, lighter.
                if((x*7+y*3)%11==0&&!edge)continue;
                int rgb=HouseReadKitchen.rgb(read.getInt(KEYS[i]));float v=(x+y)%3==0?228/255f:1f;
                pixel(pose,vc,x,y,upright,1+i,shade(rgb,v),edge?150/255f:235/255f,light);
            }
        }
        if(read.getInt("Body")==2)for(int[] leg:HouseReadKitchen.LEGS)pixel(pose,vc,leg[0],leg[1],upright,0,LINE,1f,light);
        for(int m:marks)pixel(pose,vc,m%16,m/16,upright,6,MARK,1f,light);
        if(sad)pixel(pose,vc,HouseReadKitchen.TEAR[0],HouseReadKitchen.TEAR[1],upright,6,TEAR,1f,light);
    }
    /** Whether a marked pixel is on its part's edge: a neighbour off the page or of another part. */
    static boolean edge(String[] figure,int x,int y){
        char c=figure[y].charAt(x);
        for(int[] d:new int[][]{{1,0},{-1,0},{0,1},{0,-1}}){int nx=x+d[0],ny=y+d[1];if(nx<0||nx>15||ny<0||ny>15||figure[ny].charAt(nx)!=c)return true;}
        return false;
    }
    /** One pixel of the page, in layers above the paper: 0 the pencil lines, 1 to 4 the parts' crayon, 6 the face. */
    private static void pixel(PoseStack pose,VertexConsumer vc,int x,int y,boolean upright,int layer,int rgb,float alpha,int light){
        var p=pose.last();
        if(upright){
            float w=(PX1-PX0)/16,h=(PY1-PY0)/16,x0=PX0+x*w,x1=x0+w,y1=PY1-y*h,y0=y1-h,z=(15.06f+layer*.01f)/16;
            vertex(p,vc,x0,y0,z,.5f,.5f,rgb,alpha,0,0,1,light);vertex(p,vc,x1,y0,z,.5f,.5f,rgb,alpha,0,0,1,light);vertex(p,vc,x1,y1,z,.5f,.5f,rgb,alpha,0,0,1,light);vertex(p,vc,x0,y1,z,.5f,.5f,rgb,alpha,0,0,1,light);
        }else{
            float x0=x/16f,x1=(x+1)/16f,z0=y/16f,z1=(y+1)/16f,yy=(.27f+layer*.02f)/16;
            vertex(p,vc,x0,yy,z0,.5f,.5f,rgb,alpha,0,1,0,light);vertex(p,vc,x0,yy,z1,.5f,.5f,rgb,alpha,0,1,0,light);vertex(p,vc,x1,yy,z1,.5f,.5f,rgb,alpha,0,1,0,light);vertex(p,vc,x1,yy,z0,.5f,.5f,rgb,alpha,0,1,0,light);
        }
    }
    /** 0.4.196: the pencil down the margin, its eraser away from the chair and its point toward it. */
    static void pencil(PoseStack pose,MultiBufferSource out,int light){
        var vc=out.getBuffer(RenderType.entityCutoutNoCull(CRAYON));double[] at=HouseReadKitchen.PENCIL_LIES;float x0=(float)at[0]/16,x1=(float)at[1]/16,z0=(float)at[2]/16,z1=(float)at[3]/16,len=z1-z0;
        float y0=.3f/16,y1=1.3f/16;
        box(pose,vc,x0,y0,z0,x1,y1,z0+len*.11f,0xe896a0,light);
        box(pose,vc,x0-.05f/16,y0-.05f/16,z0+len*.11f,x1+.05f/16,y1+.05f/16,z0+len*.19f,0xb8b8b0,light);
        box(pose,vc,x0,y0,z0+len*.19f,x1,y1,z0+len*.82f,0xf0c440,light);
        box(pose,vc,x0+.1f/16,y0+.1f/16,z0+len*.82f,x1-.1f/16,y1-.1f/16,z0+len*.93f,0xe2be8c,light);
        box(pose,vc,x0+.3f/16,y0+.3f/16,z0+len*.93f,x1-.3f/16,y1-.3f/16,z1,0x403c3a,light);
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
    private static int shade(int rgb,float f){return (int)((rgb>>16&255)*f)<<16|(int)((rgb>>8&255)*f)<<8|(int)((rgb&255)*f);}
    private static void vertex(PoseStack.Pose p,VertexConsumer vc,float x,float y,float z,float u,float v,int rgb,float nx,float ny,float nz,int light){vertex(p,vc,x,y,z,u,v,rgb,1f,nx,ny,nz,light);}
    private static void vertex(PoseStack.Pose p,VertexConsumer vc,float x,float y,float z,float u,float v,int rgb,float alpha,float nx,float ny,float nz,int light){
        vc.addVertex(p,x,y,z).setColor((rgb>>16&255)/255f,(rgb>>8&255)/255f,(rgb&255)/255f,alpha).setUv(u,v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p,nx,ny,nz);
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
