package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.HouseReadLookPayload;
import java.util.*;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

/**
 * The House's version of this client's own reader (0.4.159), as the server last sent it. Only glass and copies of the reader wear
 * it: the coat they chose, reading glasses, an obscured face. The live model never does, and another reader's copy never does.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class HouseReadLookClient {
    private static HouseReadLookPayload look=new HouseReadLookPayload("",false,false,false);
    private HouseReadLookClient(){}
    public static void accept(HouseReadLookPayload payload){look=payload;}
    public static HouseReadLookPayload look(){return look;}
    public static void set(HouseReadLookPayload payload){look=payload;}
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){look=new HouseReadLookPayload("",false,false,false);}
    /** Whether a copy of a reader belongs to this client's own reader. */
    public static boolean mine(Optional<UUID> owner){var p=Minecraft.getInstance().player;return p!=null&&owner.filter(p.getUUID()::equals).isPresent();}
    /** Dresses a drawn copy of the reader. {@code coat} overrides the read's coat (the coatroom glass shows what is really worn). */
    public static void dress(PlayerModel<?> body,ModelPart cloth,String coat,PoseStack pose,MultiBufferSource out,int light){
        String wear=coat!=null?coat:look.coat();if(wear!=null&&!wear.isEmpty())HouseReadClothes.coat(body,cloth,wear,pose,out,light);
        if(look.glasses()&&!look.unseen())HouseReadClothes.glasses(body,cloth,pose,out,light);if(look.unseen())HouseReadClothes.veil(body,cloth,pose,out,light);
    }
    /** A render layer for an owned copy of the reader (the doubles), drawn only on that reader's own client. */
    public static final class Layer<T extends LivingEntity,M extends PlayerModel<T>> extends RenderLayer<T,M> {
        private final ModelPart standard,slim;private final Function<T,Optional<UUID>> owner;
        public Layer(RenderLayerParent<T,M> parent,EntityModelSet models,Function<T,Optional<UUID>> owner){super(parent);standard=models.bakeLayer(HouseReadClothes.STANDARD);slim=models.bakeLayer(HouseReadClothes.SLIM);this.owner=owner;}
        @Override public void render(PoseStack pose,MultiBufferSource out,int light,T e,float walk,float amount,float partial,float age,float yaw,float pitch){
            if(e.isInvisible()||!mine(owner.apply(e)))return;var p=Minecraft.getInstance().player;boolean narrow=p!=null&&p.getSkin().model()==net.minecraft.client.resources.PlayerSkin.Model.SLIM;dress(getParentModel(),narrow?slim:standard,null,pose,out,light);
        }
    }
}
