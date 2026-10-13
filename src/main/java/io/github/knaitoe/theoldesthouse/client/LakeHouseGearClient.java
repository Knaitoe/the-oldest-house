package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LakeHouseRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

/**
 * The lake house's lent gear as others see it and as the wearer sees through it: air tanks strapped to the back, and the
 * diving helmet's round window framing the first-person view. The helmet itself is an item model worn on the head.
 */
public final class LakeHouseGearClient {
    public static final ModelLayerLocation TANKS=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"lake_house_tanks"),"main");
    static final ResourceLocation TANKS_TEXTURE=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/lake_house_tanks.png");
    static final ResourceLocation PORTHOLE=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/misc/lake_house_porthole.png");
    private LakeHouseGearClient(){}

    /** Two tanks side by side on the back, their valves, and a strap round the chest. In the body's own frame. */
    public static LayerDefinition tanksLayer(){
        var mesh=new MeshDefinition();var root=mesh.getRoot();
        root.addOrReplaceChild("tanks",CubeListBuilder.create().texOffs(0,0).addBox(-3.75F,1,2.1F,3,9,3).texOffs(0,0).addBox(.75F,1,2.1F,3,9,3)
                .texOffs(12,0).addBox(-2.75F,-.5F,3.1F,1,2,1).texOffs(12,0).addBox(1.75F,-.5F,3.1F,1,2,1)
                .texOffs(0,12).addBox(-4.25F,3,-2.25F,8.5F,1,4.5F),PartPose.ZERO);
        return LayerDefinition.create(mesh,32,32);
    }
    public static boolean wearsTanks(Player p){return p.getItemBySlot(EquipmentSlot.CHEST).is(LakeHouseRegistry.TANKS.get());}
    public static boolean wearsHelmet(Player p){return p.getItemBySlot(EquipmentSlot.HEAD).is(LakeHouseRegistry.HELMET.get())||io.github.knaitoe.theoldesthouse.labyrinth.LakeHouseArtifactItem.helmet(p);}

    public static final class TanksLayer extends RenderLayer<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> {
        private final ModelPart tanks;
        public TanksLayer(RenderLayerParent<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> parent,EntityModelSet models){super(parent);tanks=models.bakeLayer(TANKS).getChild("tanks");}
        @Override public void render(PoseStack pose,MultiBufferSource out,int light,AbstractClientPlayer player,float walk,float amount,float partial,float age,float yaw,float pitch){
            if(!wearsTanks(player)||player.isInvisible())return;
            pose.pushPose();getParentModel().body.translateAndRotate(pose);
            tanks.render(pose,out.getBuffer(RenderType.entityCutoutNoCull(TANKS_TEXTURE)),light,LivingEntityRenderer.getOverlayCoords(player,0));
            pose.popPose();
        }
    }

    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Registration {
        @SubscribeEvent public static void definitions(EntityRenderersEvent.RegisterLayerDefinitions e){e.registerLayerDefinition(TANKS,LakeHouseGearClient::tanksLayer);}
        @SubscribeEvent public static void layers(EntityRenderersEvent.AddLayers e){
            for(var skin:e.getSkins()){EntityRenderer<? extends Player> renderer=e.getSkin(skin);if(renderer instanceof PlayerRenderer player)player.addLayer(new TanksLayer(player,e.getEntityModels()));}
        }
        /** In first person, the helmet's round window: the view a square of the screen's height, brass rim, dark beyond. */
        @SubscribeEvent public static void overlay(RegisterGuiLayersEvent e){
            e.registerBelowAll(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"lake_house_porthole"),(g,delta)->{
                var mc=Minecraft.getInstance();if(mc.player==null||!mc.options.getCameraType().isFirstPerson()||!wearsHelmet(mc.player))return;
                int w=g.guiWidth(),h=g.guiHeight(),x=(w-h)/2;
                RenderSystem.enableBlend();
                g.blit(PORTHOLE,x,0,-90,0F,0F,h,h,h,h);
                if(x>0){g.fill(0,0,x,h,0xFF0A0906);g.fill(x+h,0,w,h,0xFF0A0906);}
                RenderSystem.disableBlend();
            });
        }
    }
}
