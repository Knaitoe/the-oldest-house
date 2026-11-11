package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.labyrinth.BarSlateBlock;
import io.github.knaitoe.theoldesthouse.labyrinth.BarSlateEntity;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

/**
 * 0.4.450: the chalk in a square of the league slate. Three lines in the game's own letters, flat on the slate: the week in yellow
 * chalk, the name in white, who they beat in grey. A line too long for the square is drawn narrower, never cut.
 */
public final class BarSlateRenderer implements BlockEntityRenderer<BarSlateEntity> {
    private static final int[] CHALK={0xFFE3CF7A,0xFFEDEBE0,0xFFAAB2AD};
    /** Eighty of the font's pixels to the block: five to each pixel of the slate. */
    private static final float SCALE=1/80F;
    /** The square's clear width, the first line's place from the block's top and the lines' pitch, all in the font's pixels. */
    private static final float WIDTH=62,TOP=16,PITCH=13;
    private final Font font;
    public BarSlateRenderer(BlockEntityRendererProvider.Context c){font=c.getFont();}
    @Override public void render(BarSlateEntity e,float partial,PoseStack p,MultiBufferSource buffers,int light,int overlay){
        var state=e.getBlockState();if(e.blank()||!BarSlateBlock.is(state))return;
        draw(font,new String[]{e.line(0),e.line(1),e.line(2)},state.getValue(BarSlateBlock.FACING),p,buffers,light);
    }
    /** Chalk the lines into the block the pose stands at the corner of, on a slate that looks {@code facing}. */
    public static void draw(Font font,String[] lines,Direction facing,PoseStack p,MultiBufferSource buffers,int light){
        p.pushPose();
        p.translate(.5,.5,.5);p.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        // The slate is a pixel thick against the wall behind it; the chalk lies just proud of its face, from the block's top down.
        p.translate(0,.5,-.5+1/16F+.004F);p.scale(SCALE,-SCALE,SCALE);
        for(int i=0;i<lines.length&&i<CHALK.length;i++){
            var text=lines[i];if(text==null||text.isEmpty())continue;
            float width=font.width(text),fit=Math.min(1,WIDTH/Math.max(1,width));
            p.pushPose();p.translate(0,TOP+i*PITCH,0);p.scale(fit,1,1);
            font.drawInBatch(text,-width/2,0,CHALK[i],false,p.last().pose(),buffers,Font.DisplayMode.POLYGON_OFFSET,0,light);
            p.popPose();
        }
        p.popPose();
    }
}
