package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.BoardRegistry;
import io.github.knaitoe.theoldesthouse.labyrinth.LetterPlateBlock;
import io.github.knaitoe.theoldesthouse.labyrinth.LetterPlateEntity;
import io.github.knaitoe.theoldesthouse.labyrinth.LetterPlates;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import java.util.List;

/**
 * 0.4.678: the words on a lettered plate ({@link LetterPlates}), in the hand of whoever wrote them, over the part of its face its
 * picture leaves for them. The lines share the face's height; a line too long for it is drawn narrower, never cut.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class LetterPlateRenderer implements BlockEntityRenderer<LetterPlateEntity> {
    private final Font font;
    @SubscribeEvent public static void register(EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(BoardRegistry.PLATE_ENTITY.get(),LetterPlateRenderer::new);}
    public LetterPlateRenderer(BlockEntityRendererProvider.Context c){font=c.getFont();}
    @Override public void render(LetterPlateEntity e,float partial,PoseStack p,MultiBufferSource buffers,int light,int overlay){
        var state=e.getBlockState();if(!LetterPlateBlock.is(state))return;var words=e.words();if(words.isEmpty())return;
        draw(font,state.getValue(LetterPlateBlock.KIND),words,state.getValue(LetterPlateBlock.FACING),p,buffers,light);
    }
    static Component line(LetterPlates.Kind kind,String text,boolean heading){
        var style=kind.hand.font()==null?Style.EMPTY:Style.EMPTY.withFont(kind.hand.font());
        return Component.literal(text).withStyle(heading&&kind==LetterPlates.Kind.NOTICE?style.withBold(true):style);
    }
    /** Letters the words onto a plate of {@code kind} in the block the pose stands at the corner of, looking {@code facing}. */
    public static void draw(Font font,LetterPlates.Kind kind,List<String> words,Direction facing,PoseStack p,MultiBufferSource buffers,int light){
        int rows=Math.min(words.size(),kind.lines);if(rows==0)return;
        // The face, in blocks from the block's middle: x to the reader's right, y up, z toward the reader.
        float left=-.5F+kind.left/16F,right=-.5F+kind.right/16F,top=.5F-kind.top/16F,bottom=.5F-kind.bottom/16F;
        float pitch=(top-bottom)/kind.lines;
        float scale=pitch/(font.lineHeight+1);
        float width=(right-left)*.92F/scale;
        p.pushPose();
        p.translate(.5,.5,.5);p.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        p.translate(0,0,-.5+kind.proud/16F+.004F);
        float y0=top-((top-bottom)-rows*pitch)/2;
        for(int i=0;i<rows;i++){
            var text=line(kind,words.get(i),i==0);float w=font.width(text),fit=Math.min(1,width/Math.max(1,w));
            p.pushPose();
            p.translate((left+right)/2,y0-i*pitch-(pitch-font.lineHeight*scale)/2,0);p.scale(scale*fit,-scale,scale);
            font.drawInBatch(text,-w/2,0,kind.ink,false,p.last().pose(),buffers,Font.DisplayMode.POLYGON_OFFSET,0,light);
            p.popPose();
        }
        p.popPose();
    }
}
