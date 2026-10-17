package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;

/** The projector's beam: light added to whatever lies behind it, writing no depth, so the water and the film still draw through it. */
final class MovieRenderTypes extends RenderType {
    private MovieRenderTypes(String name,VertexFormat format,VertexFormat.Mode mode,int size,boolean crumbling,boolean sort,Runnable setup,Runnable clear){super(name,format,mode,size,crumbling,sort,setup,clear);}
    static final RenderType BEAM=create("the_oldest_house_movie_beam",DefaultVertexFormat.POSITION_COLOR,VertexFormat.Mode.QUADS,1536,false,true,
            CompositeState.builder().setShaderState(RENDERTYPE_LIGHTNING_SHADER).setTransparencyState(LIGHTNING_TRANSPARENCY).setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL).createCompositeState(false));
}
