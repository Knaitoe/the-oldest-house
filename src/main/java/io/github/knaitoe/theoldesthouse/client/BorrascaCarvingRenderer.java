package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;

/** Actual names cut into native bark. The gouge remains underneath the restored name. */
public final class BorrascaCarvingRenderer implements BlockEntityRenderer<BorrascaCarvingEntity> {
    private final Font font;
    public BorrascaCarvingRenderer(BlockEntityRendererProvider.Context c){font=c.getFont();}
    @Override public void render(BorrascaCarvingEntity e,float partial,PoseStack p,MultiBufferSource buffers,int light,int overlay){
        draw(font,e.friend(),e.reader(),e.getBlockState().getValue(BorrascaCarvingBlock.STAGE),p,buffers,light);
    }
    public static void draw(Font font,String friend,String reader,int stage,PoseStack p,MultiBufferSource buffers,int light){
        if(stage==0)return;
        p.pushPose();p.translate(.5,.70,1.003);p.scale(.009F,-.009F,.009F);
        if(stage!=3)line(font,friend,0,stage==4?0xFFE4B47D:0xFF24160D,p,buffers,light);
        if(stage>=2)line(font,reader,32,0xFF24160D,p,buffers,light);
        p.popPose();
    }
    private static void line(Font font,String text,float y,int colour,PoseStack p,MultiBufferSource b,int light){
        float width=font.width(text);p.pushPose();float fit=Math.min(1,94/Math.max(1,width));p.scale(fit,1,1);
        font.drawInBatch(text,-width/2,y,colour,false,p.last().pose(),b,Font.DisplayMode.POLYGON_OFFSET,0,light);p.popPose();
    }
}
