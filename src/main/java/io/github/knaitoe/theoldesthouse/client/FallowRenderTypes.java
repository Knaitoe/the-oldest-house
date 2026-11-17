package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;

/**
 * 0.4.674, Fallow's four minutes (FallowStrikeClient): what goes up out of the pads is drawn in colour alone, far off over the wheat. Its
 * body is solid and writes depth; its smoke is soft and writes none, so the puffs lie over one another without cutting each other off;
 * its fire is light added to whatever is behind it.
 */
final class FallowRenderTypes extends RenderType {
    private FallowRenderTypes(String name,VertexFormat format,VertexFormat.Mode mode,int size,boolean crumbling,boolean sort,Runnable setup,Runnable clear){super(name,format,mode,size,crumbling,sort,setup,clear);}
    static final RenderType BODY=create("the_oldest_house_fallow_body",DefaultVertexFormat.POSITION_COLOR,VertexFormat.Mode.QUADS,1536,false,false,
            CompositeState.builder().setShaderState(POSITION_COLOR_SHADER).setTransparencyState(NO_TRANSPARENCY).setCullState(NO_CULL).createCompositeState(false));
    static final RenderType SMOKE=create("the_oldest_house_fallow_smoke",DefaultVertexFormat.POSITION_COLOR,VertexFormat.Mode.QUADS,1<<16,false,false,
            CompositeState.builder().setShaderState(POSITION_COLOR_SHADER).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL).createCompositeState(false));
    static final RenderType FIRE=create("the_oldest_house_fallow_fire",DefaultVertexFormat.POSITION_COLOR,VertexFormat.Mode.QUADS,1536,false,false,
            CompositeState.builder().setShaderState(RENDERTYPE_LIGHTNING_SHADER).setTransparencyState(LIGHTNING_TRANSPARENCY).setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL).createCompositeState(false));
}
