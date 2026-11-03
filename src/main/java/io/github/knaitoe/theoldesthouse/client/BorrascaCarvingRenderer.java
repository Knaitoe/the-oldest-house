package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;

/** Actual names cut into native bark. The gouge remains underneath the restored name. 0.4.237: and names scratched into a cell wall. */
public final class BorrascaCarvingRenderer implements BlockEntityRenderer<BorrascaCarvingEntity> {
    private final Font font;
    public BorrascaCarvingRenderer(BlockEntityRendererProvider.Context c){font=c.getFont();}
    @Override public void render(BorrascaCarvingEntity e,float partial,PoseStack p,MultiBufferSource buffers,int light,int overlay){
        var state=e.getBlockState();if(state.getValue(BorrascaCarvingBlock.WALL)){scratched(font,e.friend(),e.reader(),state.getValue(BorrascaCarvingBlock.STAGE),p,buffers,light);return;}
        draw(font,e.friend(),e.reader(),state.getValue(BorrascaCarvingBlock.STAGE),p,buffers,light);
    }
    public static void draw(Font font,String friend,String reader,int stage,PoseStack p,MultiBufferSource buffers,int light){
        p.pushPose();p.translate(.5,.82,1.003);p.scale(.008F,-.008F,.008F);
        cut(font,"DANA",-19,-2,0xFFAF9369,p,buffers,light);
        cut(font,"JESS + M",18,12,0xFF927B58,p,buffers,light);
        cut(font,"LEE",-24,28,0xFF9D855C,p,buffers,light);
        if(stage>=1&&stage!=3)cut(font,friend,2,56,stage==4?0xFFF0C18A:0xFFC4A078,p,buffers,light);
        if(stage>=2)cut(font,reader,0,78,0xFFC4A078,p,buffers,light);
        p.popPose();
    }
    private static void cut(Font font,String name,float x,float y,int colour,PoseStack p,MultiBufferSource buffers,int light){
        p.pushPose();p.translate(x,0,0);p.mulPose(com.mojang.math.Axis.ZP.rotationDegrees((x%7)*.7F));
        p.pushPose();p.translate(.8,-.9,-.03);
        line(font,name,y,0xFF2C2116,p,buffers,light);
        p.popPose();
        line(font,name,y,colour,p,buffers,light);
        p.popPose();
    }
    /** 0.4.237: names scratched into a cell wall with something small and hard: pale, a little uneven, in capitals. */
    public static void scratched(Font font,String first,String second,int stage,PoseStack p,MultiBufferSource buffers,int light){
        if(stage==0)return;
        p.pushPose();p.translate(.5,.66,1.003);p.scale(.008F,-.008F,.008F);
        line(font,first.toUpperCase(java.util.Locale.ROOT),0,0xFFD9D5CB,p,buffers,light);
        if(stage>=2&&!second.isEmpty()){p.pushPose();p.translate(3,0,0);p.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(-2));line(font,second.toUpperCase(java.util.Locale.ROOT),30,0xFFCFCBC1,p,buffers,light);p.popPose();}
        p.popPose();
    }
    private static void line(Font font,String text,float y,int colour,PoseStack p,MultiBufferSource b,int light){
        float width=font.width(text);p.pushPose();float fit=Math.min(1,94/Math.max(1,width));p.scale(fit,1,1);
        font.drawInBatch(text,-width/2,y,colour,false,p.last().pose(),b,Font.DisplayMode.POLYGON_OFFSET,0,light);p.popPose();
    }
}
