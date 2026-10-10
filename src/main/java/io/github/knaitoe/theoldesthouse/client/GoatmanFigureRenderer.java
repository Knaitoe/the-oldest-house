package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.GoatmanFigure;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

/** Only its viewer ever sees it; its eyes catch what little light there is. */
public final class GoatmanFigureRenderer extends MobRenderer<GoatmanFigure,GoatmanFigureModel<GoatmanFigure>> {
    public static final ResourceLocation SKIN=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/goatman.png");
    public static final ResourceLocation EYES=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/goatman_eyes.png");
    public GoatmanFigureRenderer(EntityRendererProvider.Context c){
        super(c,new GoatmanFigureModel<>(c.bakeLayer(GoatmanFigureModel.LAYER)),.35F);
        addLayer(new EyesLayer<>(this){@Override public RenderType renderType(){return RenderType.eyes(EYES);}});
    }
    @Override public boolean shouldRender(GoatmanFigure e,Frustum f,double x,double y,double z){
        var player=Minecraft.getInstance().player;
        return player!=null&&e.viewer().filter(player.getUUID()::equals).isPresent()&&super.shouldRender(e,f,x,y,z);
    }
    @Override public ResourceLocation getTextureLocation(GoatmanFigure e){return SKIN;}
    @Override protected boolean shouldShowName(GoatmanFigure e){return false;}
}
