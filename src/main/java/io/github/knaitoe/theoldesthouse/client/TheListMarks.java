package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.TheListMarksPayload;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderArmEvent;

/**
 * 0.4.710, The list, part 4a-3 (docs/THE_LIST_0_4_710.md): the blood on a reader wearing the slasher's body, drawn by every viewer:
 * flecks thrown up the front of the shirt and down the sleeve of the hand that holds the hook, more with each time it goes in or comes
 * out (three drawings, tools/draw_the_list_art.py). Drawn on the outer layer of the player's own skin (the jacket and the sleeves), so
 * it lies over whatever they wear; and on the reader's own arm in first person, which is where they see it.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class TheListMarks {
    private TheListMarks(){}
    private static final Map<UUID,Integer> BLOOD=new HashMap<>();
    private static final ResourceLocation[] SPATTER=new ResourceLocation[4];
    static{for(int i=1;i<=3;i++)SPATTER[i]=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/the_list/spatter_"+i+".png");}
    public static void accept(TheListMarksPayload p){if(p.blood()>0)BLOOD.put(p.player(),Math.min(3,p.blood()));else BLOOD.remove(p.player());}
    /** How much blood a player is drawn with, 0 to 3. */
    public static int blood(UUID player){return BLOOD.getOrDefault(player,0);}
    /** For the review frames: as if the server had said so. */
    public static void set(UUID player,int blood){if(blood>0)BLOOD.put(player,blood);else BLOOD.remove(player);}
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){BLOOD.clear();}

    /** The jacket and the sleeve of the main hand drawn again with the blood on them, over a model already posed. */
    static void draw(PlayerModel<?> model,boolean leftHanded,PoseStack pose,MultiBufferSource out,int blood,int light,int overlay){
        var buffer=out.getBuffer(RenderType.entityCutoutNoCull(SPATTER[blood]));
        for(var part:new ModelPart[]{model.jacket,leftHanded?model.leftSleeve:model.rightSleeve}){
            boolean was=part.visible;part.visible=true;
            part.render(pose,buffer,light,overlay);
            part.visible=was;
        }
    }
    public static final class Layer extends RenderLayer<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> {
        public Layer(RenderLayerParent<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> parent){super(parent);}
        @Override public void render(PoseStack pose,MultiBufferSource out,int light,AbstractClientPlayer player,float walk,float amount,float partial,float age,float yaw,float pitch){
            int blood=blood(player.getUUID());if(blood<=0||player.isInvisible())return;
            draw(getParentModel(),player.getMainArm()==HumanoidArm.LEFT,pose,out,blood,light,LivingEntityRenderer.getOverlayCoords(player,0));
        }
    }
    /** The reader's own arm in first person: the hand that holds the hook, with the blood down its sleeve. */
    @SubscribeEvent public static void ownArm(RenderArmEvent e){
        var player=e.getPlayer();int blood=blood(player.getUUID());if(blood<=0||e.getArm()!=player.getMainArm())return;
        if(!(Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player) instanceof PlayerRenderer renderer))return;
        var model=renderer.getModel();
        var sleeve=e.getArm()==HumanoidArm.RIGHT?model.rightSleeve:model.leftSleeve;
        // The game draws the arm and its sleeve after posing them for the hand; the same pose, again, with the blood.
        model.attackTime=0;model.crouching=false;model.swimAmount=0;model.setupAnim(player,0,0,0,0,0);
        var arm=e.getArm()==HumanoidArm.RIGHT?model.rightArm:model.leftArm;arm.xRot=0;sleeve.copyFrom(arm);sleeve.xRot=0;
        boolean was=sleeve.visible;sleeve.visible=true;
        sleeve.render(e.getPoseStack(),e.getMultiBufferSource().getBuffer(RenderType.entityCutoutNoCull(SPATTER[blood])),e.getPackedLight(),OverlayTexture.NO_OVERLAY);
        sleeve.visible=was;
    }
    /**
     * The hook's wound on one of the five, once it has gone in (TheListHunt.WOUND): torn through the front of whatever they wear under
     * the left collarbone, and blood soaked out round it and run down to the hem; 0.4.711, the boards' mark across the nape and the
     * collar behind (NECK); 0.4.713, the line's, where the riser went in at the small of the back and out through the belly (GUT). Drawn
     * over the cast's own model on the same faces.
     */
    static final ResourceLocation WOUND=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/the_list/wound.png");
    /** 0.4.711: the boards' mark across the back of the neck, from the second stamp (TheListHunt.NECK). */
    static final ResourceLocation NECK=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/the_list/neck.png");
    /** 0.4.713: through the belly and the small of the back, from the riser (TheListHunt.GUT). */
    static final ResourceLocation GUT=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/the_list/gut.png");
    /** 0.4.714: the face, scalded in the fryer (TheListHunt.BURN). */
    static final ResourceLocation BURN=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/the_list/burn.png");
    /** 0.4.715: the back of the head where the car door came down on it (TheListHunt.CRUSH). */
    static final ResourceLocation CRUSH=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/the_list/crush.png");
    /** 0.4.716: the side of the head where the pumpjack's weight came down on it (TheListHunt.FLAT). */
    static final ResourceLocation FLAT=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/the_list/flat.png");
    public static final class Wound extends RenderLayer<io.github.knaitoe.theoldesthouse.labyrinth.LiteraryActor,LiteraryRenderers.CastModel> {
        public Wound(RenderLayerParent<io.github.knaitoe.theoldesthouse.labyrinth.LiteraryActor,LiteraryRenderers.CastModel> parent){super(parent);}
        public static boolean wounded(io.github.knaitoe.theoldesthouse.labyrinth.LiteraryActor a){
            return a.role()==io.github.knaitoe.theoldesthouse.labyrinth.LiteraryActor.STRANGER&&io.github.knaitoe.theoldesthouse.labyrinth.TheListPeople.cast(a.phase())
                &&(a.marks()&(io.github.knaitoe.theoldesthouse.labyrinth.TheListHunt.WOUND|io.github.knaitoe.theoldesthouse.labyrinth.TheListHunt.NECK|io.github.knaitoe.theoldesthouse.labyrinth.TheListHunt.GUT|io.github.knaitoe.theoldesthouse.labyrinth.TheListHunt.BURN|io.github.knaitoe.theoldesthouse.labyrinth.TheListHunt.CRUSH|io.github.knaitoe.theoldesthouse.labyrinth.TheListHunt.FLAT))!=0;
        }
        @Override public void render(PoseStack pose,MultiBufferSource out,int light,io.github.knaitoe.theoldesthouse.labyrinth.LiteraryActor a,float walk,float amount,float partial,float age,float yaw,float pitch){
            if(!wounded(a)||a.isInvisible())return;
            int overlay=LivingEntityRenderer.getOverlayCoords(a,0);
            if((a.marks()&io.github.knaitoe.theoldesthouse.labyrinth.TheListHunt.WOUND)!=0)getParentModel().renderToBuffer(pose,out.getBuffer(RenderType.entityCutoutNoCull(WOUND)),light,overlay,-1);
            if((a.marks()&io.github.knaitoe.theoldesthouse.labyrinth.TheListHunt.NECK)!=0)getParentModel().renderToBuffer(pose,out.getBuffer(RenderType.entityCutoutNoCull(NECK)),light,overlay,-1);
            if((a.marks()&io.github.knaitoe.theoldesthouse.labyrinth.TheListHunt.GUT)!=0)getParentModel().renderToBuffer(pose,out.getBuffer(RenderType.entityCutoutNoCull(GUT)),light,overlay,-1);
            if((a.marks()&io.github.knaitoe.theoldesthouse.labyrinth.TheListHunt.BURN)!=0)getParentModel().renderToBuffer(pose,out.getBuffer(RenderType.entityCutoutNoCull(BURN)),light,overlay,-1);
            if((a.marks()&io.github.knaitoe.theoldesthouse.labyrinth.TheListHunt.CRUSH)!=0)getParentModel().renderToBuffer(pose,out.getBuffer(RenderType.entityCutoutNoCull(CRUSH)),light,overlay,-1);
            if((a.marks()&io.github.knaitoe.theoldesthouse.labyrinth.TheListHunt.FLAT)!=0)getParentModel().renderToBuffer(pose,out.getBuffer(RenderType.entityCutoutNoCull(FLAT)),light,overlay,-1);
        }
    }
    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Registration {
        private Registration(){}
        @SubscribeEvent public static void layers(EntityRenderersEvent.AddLayers e){
            for(var skin:e.getSkins()){EntityRenderer<? extends Player> renderer=e.getSkin(skin);if(renderer instanceof PlayerRenderer player)player.addLayer(new Layer(player));}
        }
    }
}
