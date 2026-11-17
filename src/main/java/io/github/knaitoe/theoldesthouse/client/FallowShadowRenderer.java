package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.FallowRegistry;
import io.github.knaitoe.theoldesthouse.labyrinth.FallowShadowBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.lwjgl.opengl.GL11;

/**
 * 0.4.680, Fallow's light (docs/FALLOW_0_4_680.md): a reader's shadow on the station's front wall. The body they wore when the light came,
 * hair and coat and all, in the stance it caught them in, pressed flat against the wall the way the light threw it, and burnt there. It is
 * drawn in one flat soot, the body's outline and nothing of its colours, with a scorch two pixels wide round it, paler toward the wall's own
 * colour; lit as the wall round it is, so it darkens with the wall at night. Their House body when the House had them in one; their own skin
 * only when its bodies are off.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class FallowShadowRenderer implements BlockEntityRenderer<FallowShadowBlock.Shadow> {
    /** The shape, and the two pixels of scorch round it: the nearer darker, the outer nearer the siding's own colour. */
    private static final int SOOT=0xFF2F2A25,SCORCH=0xFF5B5148,SINGE=0xFF877B6C;
    /** The eight ways round, for a ring of even width whatever the body's shape. */
    private static final float[][] ROUND={{1,0},{-1,0},{0,1},{0,-1},{.7071F,.7071F},{-.7071F,.7071F},{.7071F,-.7071F},{-.7071F,-.7071F}};
    private static final float PIXEL=1/16F;
    /** Each skin's outline in white, kept by the skin's own texture so a body drawn again gets a fresh one. */
    private record Mask(AbstractTexture from,ResourceLocation at){}
    private static final Map<ResourceLocation,Mask> MASKS=new HashMap<>();
    private final PlayerModel<AbstractClientPlayer> wide,slim;
    public FallowShadowRenderer(BlockEntityRendererProvider.Context ctx){
        wide=new PlayerModel<>(ctx.bakeLayer(ModelLayers.PLAYER),false);
        slim=new PlayerModel<>(ctx.bakeLayer(ModelLayers.PLAYER_SLIM),true);
    }
    @SubscribeEvent public static void register(EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(FallowRegistry.SHADOW_ENTITY.get(),FallowShadowRenderer::new);}
    @Override public int getViewDistance(){return 96;}

    /** 0.4.681: the plain body a person's shadow is cut from (Boyd's, FallowBuried.light): only its outline is ever drawn. */
    private static final PlayerSkin PLAIN=new PlayerSkin(DefaultPlayerSkin.getDefaultTexture(),null,null,null,PlayerSkin.Model.WIDE,true);
    static PlayerSkin skin(FallowShadowBlock.Shadow s){
        if(s.person)return PLAIN;
        if(s.body!=null){var kept=HouseBodyClient.kept(s.reader,s.body);if(kept!=null)return kept;}
        var conn=Minecraft.getInstance().getConnection();var info=conn==null?null:conn.getPlayerInfo(s.reader);
        return info!=null?info.getSkin():DefaultPlayerSkin.get(s.reader);
    }
    /**
     * The skin's outline: every pixel the skin has drawn made solid white and every other clear, so the shape is drawn in one colour
     * with the hair's and the coat's edges as they are. Read from the card, as the reflection does; null while the skin is not there yet.
     */
    static @Nullable ResourceLocation mask(ResourceLocation skin){
        var tm=Minecraft.getInstance().getTextureManager();var tex=tm.getTexture(skin,null);if(tex==null)return null;
        var had=MASKS.get(skin);if(had!=null&&had.from()==tex)return had.at();
        RenderSystem.bindTexture(tex.getId());
        int w=GlStateManager._getTexLevelParameter(GL11.GL_TEXTURE_2D,0,GL11.GL_TEXTURE_WIDTH),h=GlStateManager._getTexLevelParameter(GL11.GL_TEXTURE_2D,0,GL11.GL_TEXTURE_HEIGHT);
        if(w<64||h<32)return null;
        var image=new NativeImage(w,h,false);image.downloadTexture(0,false);
        // Wholly there or wholly not: a half-clear pixel of cloth would let what lay behind it show through the soot in specks.
        for(int y=0;y<h;y++)for(int x=0;x<w;x++)image.setPixelRGBA(x,y,(image.getPixelRGBA(x,y)>>>24)>=26?0xFFFFFFFF:0);
        var at=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"fallow_shadow/"+skin.getNamespace()+"/"+skin.getPath());
        if(had!=null)tm.release(had.at());
        var made=new DynamicTexture(image);made.setFilter(false,false);tm.register(at,made);
        MASKS.put(skin,new Mask(tex,at));return at;
    }
    @Override public void render(FallowShadowBlock.Shadow s,float partial,PoseStack p,MultiBufferSource out,int light,int overlay){
        if(s.reader==null)return;
        var skin=skin(s);var m=skin.model()==PlayerSkin.Model.SLIM?slim:wide;
        pose(m,s);
        var facing=s.getBlockState().getValue(FallowShadowBlock.FACING);var wall=facing.getOpposite();
        var mask=mask(skin.texture());
        // Square-cut bodies thrown at a slant run head, shoulders and arms together into one block; the nearest of face-on, back-on
        // or side-on to the wall keeps the outline a person's.
        float yaw=Math.round(s.yaw/90F)*90F;
        // Unshaded and lit by the wall's own light; a skin not yet read is drawn darkened instead, its colours all but gone.
        var buf=out.getBuffer(RenderType.text(mask!=null?mask:skin.texture()));
        int soot=mask!=null?SOOT:0xFF141210;
        // The singe furthest back, the scorch over it, the shape nearest: each far enough off the last, and the singe off the wall, that
        // none fights another for depth even across the station. No polygon offset: it would pull the pressed body's edge-on sides
        // forward through the soot.
        for(var d:ROUND)draw(m,p,buf,wall,facing,yaw,.01F,d[0]*2*PIXEL,d[1]*2*PIXEL,SINGE,light);
        for(var d:ROUND)draw(m,p,buf,wall,facing,yaw,.02F,d[0]*PIXEL,d[1]*PIXEL,SCORCH,light);
        draw(m,p,buf,wall,facing,yaw,.03F,0,0,soot,light);
    }
    /**
     * The body at the wall's face behind the block, its depth toward the wall pressed out of it, a hair's breadth in front of the wall, moved
     * {@code along} the wall and {@code up} it by as much as the ring round it needs.
     */
    private static void draw(PlayerModel<?> m,PoseStack p,VertexConsumer buf,Direction wall,Direction facing,float yaw,float off,float along,float up,int colour,int light){
        boolean alongX=facing.getAxis()==Direction.Axis.X;
        p.pushPose();
        p.translate(.5+wall.getStepX()*(.5-off)+(alongX?0:along),up,.5+wall.getStepZ()*(.5-off)+(alongX?along:0));
        p.scale(alongX?.001F:1F,1F,alongX?1F:.001F);
        p.mulPose(Axis.YP.rotationDegrees(180-yaw));
        p.scale(-1,-1,1);p.scale(.9375F,.9375F,.9375F);p.translate(0,-1.501,0);
        m.renderToBuffer(p,buf,light,OverlayTexture.NO_OVERLAY,colour);
        p.popPose();
    }
    /** The stance the light caught them in: mid-stride by how fast they were going, or standing; crouched if they were down low. */
    private static void pose(PlayerModel<?> m,FallowShadowBlock.Shadow s){
        m.young=false;m.riding=false;m.crouching=s.crouched;m.setAllVisible(true);
        for(var part:new ModelPart[]{m.body,m.head,m.rightArm,m.leftArm,m.rightLeg,m.leftLeg})part.resetPose();
        float sw=Mth.clamp(s.stride,0,1),ph=s.phase,pi=(float)Math.PI;
        // A step as a photograph catches it, not the far end of a sprint: the legs to about sixty degrees apart, the arms less.
        m.rightLeg.xRot=Mth.cos(ph)*.55F*sw;m.leftLeg.xRot=Mth.cos(ph+pi)*.55F*sw;
        m.rightArm.xRot=Mth.cos(ph+pi)*.45F*sw;m.leftArm.xRot=Mth.cos(ph)*.45F*sw;
        m.rightArm.zRot=.06F;m.leftArm.zRot=-.06F;
        // 0.4.681: a person caught reaching for something (Boyd at the rig's door): the arm up and a little out, the head back.
        if(s.reach){m.rightArm.xRot=-2.6F;m.rightArm.yRot=0;m.rightArm.zRot=.35F;m.head.xRot=-.3F;}
        if(s.crouched){
            m.body.xRot=.5F;m.rightArm.xRot+=.4F;m.leftArm.xRot+=.4F;
            m.rightLeg.z+=4F;m.leftLeg.z+=4F;m.rightLeg.y+=.2F;m.leftLeg.y+=.2F;
            m.head.y+=4.2F;m.body.y+=3.2F;m.leftArm.y+=3.2F;m.rightArm.y+=3.2F;
        }
        m.hat.copyFrom(m.head);m.jacket.copyFrom(m.body);m.rightSleeve.copyFrom(m.rightArm);m.leftSleeve.copyFrom(m.leftArm);
        m.rightPants.copyFrom(m.rightLeg);m.leftPants.copyFrom(m.leftLeg);
    }
}
