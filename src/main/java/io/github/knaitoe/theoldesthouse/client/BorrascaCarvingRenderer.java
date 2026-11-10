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
        // 0.4.444 (owner: "Names should be easier to read."): cut wood is pale, and stays pale in the shade of the Tree. The names are
        // a little larger, their strokes doubled, and never drawn darker than a lit evening (the bark itself keeps the place's light).
        light=LightTexture.pack(Math.max(LightTexture.block(light),9),Math.max(LightTexture.sky(light),9));
        p.pushPose();p.translate(.5,.86,1.003);p.scale(.0092F,-.0092F,.0092F);
        cut(font,"DANA",-19,-2,0xFFD9C59A,p,buffers,light);
        cut(font,"JESS + M",16,12,0xFFCDB88C,p,buffers,light);
        cut(font,"LEE",-24,28,0xFFD2BE92,p,buffers,light);
        if(stage>=1&&stage!=3)cut(font,friend,2,54,stage==4?0xFFFFE3B0:0xFFF2DDB0,p,buffers,light);
        if(stage>=2)cut(font,reader,0,74,0xFFF2DDB0,p,buffers,light);
        p.popPose();
    }
    private static void cut(Font font,String name,float x,float y,int colour,PoseStack p,MultiBufferSource buffers,int light){
        p.pushPose();p.translate(x,0,0);p.mulPose(com.mojang.math.Axis.ZP.rotationDegrees((x%7)*.7F));
        p.pushPose();p.translate(.8,-.9,-.03);
        line(font,name,y,0xFF1E160E,p,buffers,light);
        p.popPose();
        line(font,name,y,colour,p,buffers,light);
        p.pushPose();p.translate(.55,0,-.01);line(font,name,y,colour,p,buffers,light);p.popPose();
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
