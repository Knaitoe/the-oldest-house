package io.github.knaitoe.theoldesthouse.client;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.knaitoe.theoldesthouse.labyrinth.UncannyRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemDisplayContext;
/** A served plate rests flat; ordinary dropped items retain their native renderer. */
public final class ServedMealRenderer extends ItemEntityRenderer {
    public ServedMealRenderer(EntityRendererProvider.Context context){super(context);}
    @Override public void render(ItemEntity item,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){
        var stack=item.getItem();if(!stack.is(UncannyRegistry.MEAL.get())||!item.isNoGravity()){super.render(item,yaw,partial,pose,buffers,light);return;}
        var mc=Minecraft.getInstance();var custom=stack.get(DataComponents.CUSTOM_DATA);
        if(custom!=null&&custom.copyTag().hasUUID("UncannyMealOwner")&&(mc.player==null||!custom.copyTag().getUUID("UncannyMealOwner").equals(mc.player.getUUID())))return;
        pose.pushPose();pose.translate(0,.5,0);mc.getItemRenderer().renderStatic(stack,ItemDisplayContext.FIXED,light,OverlayTexture.NO_OVERLAY,pose,buffers,item.level(),item.getId());pose.popPose();
    }
}
