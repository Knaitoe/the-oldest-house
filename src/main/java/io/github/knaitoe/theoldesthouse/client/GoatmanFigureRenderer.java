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
    @Override public void render(GoatmanFigure e,float yaw,float partial,com.mojang.blaze3d.vertex.PoseStack poses,net.minecraft.client.renderer.MultiBufferSource buffers,int light){
        // During the vigil a figure is the private lap round the trailer: give it the same dim light floor as the
        // original body's window silhouette, so it reads against the glass instead of sinking into the night.
        if(GoatmanClient.night())light=net.minecraft.client.renderer.LightTexture.pack(Math.max(7,net.minecraft.client.renderer.LightTexture.block(light)),net.minecraft.client.renderer.LightTexture.sky(light));
        super.render(e,yaw,partial,poses,buffers,light);
    }
    @Override public ResourceLocation getTextureLocation(GoatmanFigure e){return SKIN;}
    @Override protected boolean shouldShowName(GoatmanFigure e){return false;}
}
