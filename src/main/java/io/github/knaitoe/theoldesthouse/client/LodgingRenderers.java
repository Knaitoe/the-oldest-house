package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryActor;
import io.github.knaitoe.theoldesthouse.labyrinth.LodgingDoubles;
import io.github.knaitoe.theoldesthouse.labyrinth.LodgingSite;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;

/**
 * 0.4.604, The Ones Already Inside (N2): the body asleep in a reader's guest bed (labyrinth.LodgingDoubles), drawn in that reader's
 * House body, the same skin the House keeps for them (HouseBodyClient), and seen by everybody in the house.
 *
 * <ul>
 * <li>It lies on its back on the mattress, its head on the pillow, the rest of it under the bed's own blanket: the blanket is drawn
 * over the body here in the bed's textures (block/lodging/thing/blanket and sheet), raised where the chest, the legs and the two feet
 * are, its top turned down under the chin to show the sheet. Under it the chest rises and falls, slowly, as a sleeper's does.</li>
 * <li>Asleep, its lids are shut: each eye covered with the skin from just below it on the same face, so the lid is that face's own
 * colour, and under each lid the eye moves, a small rounded shift from side to side in bursts, as eyes do in dreams.</li>
 * <li>Awake, the lids are open, and the head is turned on the pillow toward wherever Vera is (the server turns it).</li>
 * <li>On its own reader's screen it also has their hair as the House keeps it (HouseReadHair), the silk wrap if they wear one at night.</li>
 * </ul>
 */
public final class LodgingRenderers {
    private LodgingRenderers(){}
    static final ResourceLocation BLANKET=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"block/lodging/thing/blanket"),
        SHEET=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"block/lodging/thing/sheet");

    /** A body under a blanket: from its feet, how far along the bed its head's middle lies (a player's 28 pixels, at a player's size). */
    static final float SIZE=.9375F,HEAD_REACH=28*SIZE/16;
    /** Where on the head cell the head's middle lies, from the bed's foot end of the cell, in pixels; and the body's middle plane above the cell's floor. */
    static final float PILLOW=11.5F,PLANE=9.9F;

    public static final class Model extends PlayerModel<LiteraryActor> {
        public Model(net.minecraft.client.model.geom.ModelPart part,boolean slim){super(part,slim);}
        @Override public void setupAnim(LiteraryActor a,float walk,float amount,float age,float yaw,float pitch){
            super.setupAnim(a,0,0,age,a.gesture()==LodgingDoubles.AWAKE?yaw:0,0);
            lie(a.gesture()==LodgingDoubles.AWAKE,age);
        }
        /** The pose under the blanket, from the native pose it was given (and the head's turn, awake). */
        public void lie(boolean awake,float age){
            riding=false;crouching=false;
            // Under the blanket: nothing of it shows but its head, so nothing else is drawn to come through.
            body.visible=jacket.visible=false;leftArm.visible=rightArm.visible=leftSleeve.visible=rightSleeve.visible=false;
            leftLeg.visible=rightLeg.visible=leftPants.visible=rightPants.visible=false;
            // The head resting on the pillow, a little above the line of the back, and settled the way a sleeping head goes.
            head.z=-3.2F;head.xRot=-.08F+Mth.sin(age*.07F)*.012F;
            if(!awake)head.zRot=.06F;
            hat.copyFrom(head);
        }
    }

    public static final class DoubleRenderer extends MobRenderer<LiteraryActor,Model> {
        private final Model wide,slim;
        public DoubleRenderer(EntityRendererProvider.Context c){
            super(c,new Model(c.bakeLayer(ModelLayers.PLAYER),false),0);wide=model;slim=new Model(c.bakeLayer(ModelLayers.PLAYER_SLIM),true);
            addLayer(new Lids(this));addLayer(new Hair(this));
        }
        static PlayerSkin skin(LiteraryActor a){
            if(a.owner().isPresent()){
                var s=HouseBodyClient.skin(a.owner().get());if(s!=null)return s;
                var mc=Minecraft.getInstance();if(mc.getConnection()!=null){var info=mc.getConnection().getPlayerInfo(a.owner().get());if(info!=null)return info.getSkin();}
                return DefaultPlayerSkin.get(a.owner().get());
            }
            return DefaultPlayerSkin.get(a.getUUID());
        }
        @Override public void render(LiteraryActor a,float yaw,float partial,PoseStack p,MultiBufferSource out,int light){
            var s=skin(a);model=s.model()==PlayerSkin.Model.SLIM?slim:wide;
            super.render(a,yaw,partial,p,out,light);
            blanket(headward(a).getOpposite(),a.tickCount+partial,a.gesture()==LodgingDoubles.AWAKE,p,out,light);
        }
        @Override public ResourceLocation getTextureLocation(LiteraryActor a){return skin(a).texture();}
        @Override protected void scale(LiteraryActor a,PoseStack p,float partial){p.scale(SIZE,SIZE,SIZE);}
        /** Laid along its bed: feet toward the foot, head on the pillow, face up (as LiteraryRenderers.lie, measured for this body). */
        @Override protected void setupRotations(LiteraryActor a,PoseStack p,float age,float yaw,float partial,float scale){layDown(headward(a),p);}
        @Override protected boolean shouldShowName(LiteraryActor a){return false;}
    }
    /** From the middle of the head cell's floor to the body laid along the bed with its head this way (before the model's flip). */
    public static void layDown(Direction head,PoseStack p){
            float back=HEAD_REACH-(PILLOW-8)/16F;
            p.translate(-head.getStepX()*back,PLANE/16F,-head.getStepZ()*back);
            p.mulPose(Axis.YP.rotationDegrees(switch(head){case SOUTH->90F;case WEST->0F;case NORTH->270F;default->180F;}));
            p.mulPose(Axis.ZP.rotationDegrees(90F));p.mulPose(Axis.YP.rotationDegrees(270F));
    }
    /** Which way the head lies: toward the head of its bed, the opposite of the way the bed runs from head to foot. */
    static Direction headward(LiteraryActor a){return LodgingSite.bedFacing(Math.max(0,Math.min(5,a.phase()))).getOpposite();}

    // ------------------------------------------------------------------------------------------------ the blanket over it
    /**
     * The blanket, in the bed's own pixels: drawn for a bed whose foot is north (the head cell's z 0 to 16, the foot cell's -16 to 0)
     * and turned with the bed. From the foot board to just under the chin: a layer the bed's width, the body's ridge on it (the chest
     * breathing), the two feet, and the turned-down top, sheet side up.
     */
    public static void blanket(Direction foot,float age,boolean awake,PoseStack p,MultiBufferSource out,int light){
        var atlas=Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS);var wool=atlas.apply(BLANKET);var sheet=atlas.apply(SHEET);
        var vc=out.getBuffer(RenderType.entityCutoutNoCull(InventoryMenu.BLOCK_ATLAS));
        // A sleeper breathes about once every four and a half seconds; asleep a little deeper than awake.
        float breath=(Mth.sin(age*(awake?.09F:.07F))*.5F+.5F)*.7F;
        p.pushPose();
        // From the actor (standing in the head cell's middle, on its floor) to the head cell's corner, turned so the bed's foot is north.
        p.mulPose(Axis.YP.rotationDegrees(switch(foot){case SOUTH->180F;case EAST->270F;case WEST->90F;default->0F;}));
        p.translate(-.5,0,-.5);
        var pose=p.last();
        box(vc,pose,wool,light,.3F,8.4F,-15.7F,15.7F,10.6F,6.4F);                         // the layer, the bed's width
        box(vc,pose,wool,light,3.2F,10.6F,-3F,12.8F,12.2F+breath,6.4F);                    // over the chest, rising and falling
        box(vc,pose,wool,light,3.6F,10.6F,-14.2F,12.4F,11.5F,-3F);                         // over the legs
        box(vc,pose,wool,light,3.8F,11.5F,-14.2F,7.4F,12.6F,-11.6F);                       // the two feet
        box(vc,pose,wool,light,8.6F,11.5F,-14.2F,12.2F,12.6F,-11.6F);
        box(vc,pose,sheet,light,.2F,8.4F,6.4F,15.8F,11.9F+breath*.6F,8F);                 // the top turned down under the chin, the sheet showing
        p.popPose();
    }
    /** A box in pixels (the bed's frame), split where it crosses a cell's edge so every face takes its texture inside one sprite. */
    private static void box(VertexConsumer vc,PoseStack.Pose pose,net.minecraft.client.renderer.texture.TextureAtlasSprite s,int light,float x0,float y0,float z0,float x1,float y1,float z1){
        if(z0<0&&z1>0){box(vc,pose,s,light,x0,y0,z0,x1,y1,0);box(vc,pose,s,light,x0,y0,0,x1,y1,z1);return;}
        float cz=z0<0?16:0;
        // top
        quad(vc,pose,s,light,0,1,0,x0,y1,z0,x0,y1,z1,x1,y1,z1,x1,y1,z0,x0,z0+cz,x0,z1+cz,x1,z1+cz,x1,z0+cz);
        // sides
        quad(vc,pose,s,light,0,0,-1,x1,y0,z0,x0,y0,z0,x0,y1,z0,x1,y1,z0,16-x1,16-y0,16-x0,16-y0,16-x0,16-y1,16-x1,16-y1);
        quad(vc,pose,s,light,0,0,1,x0,y0,z1,x1,y0,z1,x1,y1,z1,x0,y1,z1,x0,16-y0,x1,16-y0,x1,16-y1,x0,16-y1);
        quad(vc,pose,s,light,-1,0,0,x0,y0,z0,x0,y0,z1,x0,y1,z1,x0,y1,z0,z0+cz,16-y0,z1+cz,16-y0,z1+cz,16-y1,z0+cz,16-y1);
        quad(vc,pose,s,light,1,0,0,x1,y0,z1,x1,y0,z0,x1,y1,z0,x1,y1,z1,16-(z1+cz),16-y0,16-(z0+cz),16-y0,16-(z0+cz),16-y1,16-(z1+cz),16-y1);
    }
    private static void quad(VertexConsumer vc,PoseStack.Pose pose,net.minecraft.client.renderer.texture.TextureAtlasSprite s,int light,float nx,float ny,float nz,float... v){
        for(int i=0;i<4;i++){
            float x=v[i*3]/16F,y=v[i*3+1]/16F,z=v[i*3+2]/16F,u=Mth.clamp(v[12+i*2],0,16),w=Mth.clamp(v[13+i*2],0,16);
            vc.addVertex(pose,x,y,z).setColor(-1).setUv(s.getU(u/16F),s.getV(w/16F)).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose,nx,ny,nz);
        }
    }

    // ------------------------------------------------------------------------------------------------ the shut eyes
    /**
     * Each eye's lid, asleep: a patch of the face's own skin, taken from just below that eye, laid over it a hair proud of the face, and
     * a smaller one over that where the eye moves under it. The eyes are in a player's face's fifth row, in its second and third columns
     * and its sixth and seventh; a face drawn otherwise still has its eyes shut over the same place.
     */
    public static final class Lids extends RenderLayer<LiteraryActor,Model> {
        Lids(DoubleRenderer parent){super(parent);}
        @Override public void render(PoseStack p,MultiBufferSource out,int light,LiteraryActor a,float walk,float amount,float partial,float age,float yaw,float pitch){
            if(a.gesture()==LodgingDoubles.AWAKE)return;
            shut(getParentModel(),DoubleRenderer.skin(a).texture(),age,a.getId(),p,out,light);
        }
        /** The lids over a model's eyes, drawn in its head's own frame. */
        public static void shut(Model model,ResourceLocation skin,float age,int seed,PoseStack p,MultiBufferSource out,int light){
            var vc=out.getBuffer(RenderType.entityCutoutNoCull(skin));
            p.pushPose();model.head.translateAndRotate(p);var pose=p.last();
            for(int eye=0;eye<2;eye++){
                float cx=eye==0?-2:2,u0=eye==0?9:13;
                // The lid: the two skin pixels under the eye, stretched over it and a little above.
                face(vc,pose,light,cx-1.3F,-4.55F,cx+1.3F,-2.7F,-4.04F,u0,13,u0+2,14,1);
                // Under it, the eye going from side to side in a dream.
                float look=dream(seed,age);
                face(vc,pose,light,cx-.55F+look,-4.25F,cx+.55F+look,-3.2F,-4.1F,u0+.5F,13.2F,u0+1.5F,13.8F,.94F);
            }
            p.popPose();
        }
        /** How far, in pixels, the eye under the lid has moved: still for a while, then a few quick moves, the same both sides. */
        static float dream(int seed,float age){
            float t=age+(Math.floorMod(seed,97))*7;float cycle=t%150;if(cycle>70)return 0;
            float step=(float)Math.floor(cycle/9);float within=(cycle%9)/9F;
            float from=pick(seed,step),to=pick(seed,step+1);float ease=within<.35F?Mth.sin(within/.35F*Mth.HALF_PI):1;
            return Mth.lerp(ease,from,to)*.55F;
        }
        private static float pick(int seed,float step){long h=(long)step*341873128712L+seed*132897987541L;h^=h>>>13;return ((h&0xff)/255F)*2-1;}
        private static void face(VertexConsumer vc,PoseStack.Pose pose,int light,float x0,float y0,float x1,float y1,float z,float u0,float v0,float u1,float v1,float shade){
            int c=0xff000000|((int)(255*shade)<<16)|((int)(255*shade)<<8)|(int)(255*shade);
            float[][] corners={{x0,y1,u0,v1},{x1,y1,u1,v1},{x1,y0,u1,v0},{x0,y0,u0,v0}};
            for(var k:corners)vc.addVertex(pose,k[0]/16F,k[1]/16F,z/16F).setColor(c).setUv(k[2]/64F,k[3]/64F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose,0,0,-1);
        }
    }

    /** Its reader's hair as the House keeps it, on that reader's own screen only (the House keeps it only there). */
    static final class Hair extends RenderLayer<LiteraryActor,Model> {
        Hair(DoubleRenderer parent){super(parent);}
        @Override public void render(PoseStack p,MultiBufferSource out,int light,LiteraryActor a,float walk,float amount,float partial,float age,float yaw,float pitch){
            var me=Minecraft.getInstance().player;if(me==null||a.owner().isEmpty()||!a.owner().get().equals(me.getUUID()))return;
            int style=HouseReadLookClient.style(HouseReadLookClient.look(),a.gesture()!=LodgingDoubles.AWAKE);
            if(style>=0)HouseReadHair.draw(getParentModel(),style,HouseReadHair.colour(me),p,out,light);
        }
    }
}
