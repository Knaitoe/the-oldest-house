package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.HouseReadRegistry;
import io.github.knaitoe.theoldesthouse.labyrinth.HouseReadSites;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.lwjgl.opengl.GL11;

/**
 * 0.4.167 (owner: "develop more tools for textured hair - the player should be able to see a preview when picking up the tool
 * ... reflected in the mirror"; "More hair styles would still be appropriate"): the style each bathroom tool gives, drawn on the
 * reader in the glass while the tool is in their hand, and kept afterwards on the House's version of them. The hair is tinted to
 * the commonest colour on the crown of the reader's own skin; wraps, bonnets, ties, clips and rollers keep their own colours.
 * Only ever drawn in glass and on the reader's own copies, never on the live model.
 */
public final class HouseReadHair {
    private HouseReadHair(){}
    public static final ModelLayerLocation LAYER=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"read_hair"),"main");
    /** A cuboid on the head: corner and size in the head's own pixels (the head is -4,-8,-4 to 4,0,4). */
    private record Box(float x,float y,float z,float w,float h,float d){}
    /** A style: its hair pattern (tinted) and the cuboids in it, and its fabric (untinted) and the cuboids in that. */
    private record Style(String name,String hair,List<Box> hairBoxes,String fabric,List<Box> fabricBoxes){}
    private static Box b(float x,float y,float z,float w,float h,float d){return new Box(x,y,z,w,h,d);}
    /** By routine tool, the same order as HouseReadMoments.ROUTINES. */
    static final List<Style> STYLES=List.of(
        new Style("silk headwrap",null,List.of(),"silk",List.of(b(-4.6f,-9.8f,-4.6f,9.2f,5f,9.2f),b(-1.6f,-11.4f,-3.6f,3.2f,1.8f,3.2f),b(-1.2f,-6f,4.4f,2.4f,4f,1f))),
        new Style("defined curls","curl",List.of(b(-4.7f,-9.2f,-4.7f,9.4f,4f,9.4f),b(-4.7f,-5.2f,-1.5f,1f,3.5f,6.2f),b(3.7f,-5.2f,-1.5f,1f,3.5f,6.2f),b(-4.7f,-5.2f,3.7f,9.4f,4.5f,1f)),null,List.of()),
        // 0.4.172 (owner: "Fro less intense, or add a shorter fro"): closer to the head and rounded off at the crown, three
        // pixels high rather than four and a half, a pixel and a bit out at the sides rather than two.
        new Style("picked-out afro","coil",List.of(b(-5.3f,-11f,-5.1f,10.6f,5f,10.4f),b(-4.3f,-11.8f,-4.1f,8.6f,.8f,8.4f),b(-5.3f,-6f,-2.2f,1.3f,4f,7.5f),b(4f,-6f,-2.2f,1.3f,4f,7.5f),b(-5.3f,-6f,4f,10.6f,4.5f,1.3f)),null,List.of()),
        new Style("oiled braids","braid",List.of(b(-4.5f,-8.7f,-4.5f,9f,3.4f,9f),b(-4f,-5.3f,4.2f,8f,10f,.8f)),null,List.of()),
        new Style("cornrows","rows",List.of(b(-4.3f,-8.5f,-4.3f,8.6f,3f,8.6f),b(-4.3f,-5.5f,3.6f,8.6f,2.2f,.7f)),null,List.of()),
        new Style("two puffs","coil",List.of(b(-4.4f,-8.6f,-4.4f,8.8f,2.6f,8.8f),b(-6.4f,-11.2f,-2f,4.2f,4.2f,4.2f),b(2.2f,-11.2f,-2f,4.2f,4.2f,4.2f)),"tie",List.of(b(-5.2f,-8.3f,-1.2f,1.8f,1f,2.4f),b(3.4f,-8.3f,-1.2f,1.8f,1f,2.4f))),
        new Style("brushed straight","straight",List.of(b(-4.5f,-8.7f,-4.5f,9f,3.6f,9f),b(-4.5f,-5.1f,-3f,.7f,6.5f,7f),b(3.8f,-5.1f,-3f,.7f,6.5f,7f),b(-4.5f,-5.1f,3.8f,9f,10f,.7f)),null,List.of()),
        new Style("blown-out volume","volume",List.of(b(-5f,-10.2f,-5f,10f,5f,10f),b(-5f,-5.2f,-3f,1f,6f,8f),b(4f,-5.2f,-3f,1f,6f,8f),b(-5f,-5.2f,4f,10f,8f,1f)),null,List.of()),
        new Style("clipped-up twist","straight",List.of(b(-4.5f,-8.7f,-4.5f,9f,3.6f,9f),b(-2.2f,-10.8f,2.2f,4.4f,4f,4f)),"clip",List.of(b(-2.6f,-9.8f,5.9f,5.2f,1.6f,.9f))),
        new Style("side part","short",List.of(b(-4.4f,-8.6f,-4.4f,8.8f,2.6f,8.8f),b(-4.4f,-6f,-2.5f,.5f,1.5f,6.5f),b(3.9f,-6f,-2.5f,.5f,1.5f,6.5f),b(-4.4f,-6f,3.9f,8.8f,2f,.5f)),null,List.of()),
        new Style("slicked back","slick",List.of(b(-4.4f,-8.6f,-4.4f,8.8f,2.4f,8.8f),b(-4.4f,-6.2f,3.9f,8.8f,3f,.6f)),null,List.of()),
        new Style("close crop","crop",List.of(b(-4.2f,-8.3f,-4.2f,8.4f,1.6f,8.4f)),null,List.of()),
        new Style("satin bonnet",null,List.of(),"satin",List.of(b(-5.1f,-10.4f,-5.1f,10.2f,5.8f,10.6f))),
        new Style("laid edges and a ponytail","slick",List.of(b(-4.35f,-8.5f,-4.35f,8.7f,2.8f,8.7f),b(-1f,-7.4f,4.3f,2f,2f,3f),b(-1.2f,-6f,6.8f,2.4f,6.5f,1.6f)),"tie",List.of(b(-1.3f,-7.6f,6.4f,2.6f,2.4f,.6f))),
        new Style("set in rollers","straight",List.of(b(-4.5f,-8.7f,-4.5f,9f,3.6f,9f)),"rollers",List.of(b(-4.2f,-10.2f,-3f,2.6f,1.8f,2.6f),b(-1.3f,-10.2f,-3f,2.6f,1.8f,2.6f),b(1.6f,-10.2f,-3f,2.6f,1.8f,2.6f),b(-4.2f,-10.2f,.8f,2.6f,1.8f,2.6f),b(-1.3f,-10.2f,.8f,2.6f,1.8f,2.6f),b(1.6f,-10.2f,.8f,2.6f,1.8f,2.6f))),
        new Style("360 waves","wave",List.of(b(-4.25f,-8.35f,-4.25f,8.5f,1.9f,8.5f)),null,List.of()));
    public static String name(int style){return STYLES.get(style).name();}

    public static LayerDefinition layer(){
        var mesh=new MeshDefinition();var root=mesh.getRoot();
        for(int i=0;i<STYLES.size();i++){var style=STYLES.get(i);
            for(var kind:List.of("h","f")){var boxes=kind.equals("h")?style.hairBoxes():style.fabricBoxes();if(boxes.isEmpty())continue;
                var cubes=CubeListBuilder.create().texOffs(0,0);for(var c:boxes)cubes.addBox(c.x(),c.y(),c.z(),c.w(),c.h(),c.d());
                root.addOrReplaceChild(kind+i,cubes,PartPose.ZERO);}}
        return LayerDefinition.create(mesh,64,32);
    }
    private static ModelPart model;
    private static ModelPart model(){if(model==null)model=Minecraft.getInstance().getEntityModels().bakeLayer(LAYER);return model;}
    private static ResourceLocation sheet(String name){return ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/house_read/hair/"+name+".png");}

    /** The style to show on this client's own reader in the glass: the tool in their hand, else what the House kept, else none. */
    public static int shown(AbstractClientPlayer reader){
        var held=reader.getMainHandItem();
        if(HouseReadSites.loan(held,reader.getUUID()))for(int i=0;i<HouseReadRegistry.ROUTINES.size();i++)if(held.is(HouseReadRegistry.ROUTINES.get(i).get()))return i;
        return HouseReadLookClient.look().hair();
    }
    /** Draws one style on a posed body's head, the hair in {@code colour} (RGB). */
    public static void draw(HumanoidModel<?> body,int style,int colour,PoseStack pose,MultiBufferSource out,int light){
        if(style<0||style>=STYLES.size())return;var s=STYLES.get(style);var root=model();
        pose.pushPose();body.head.translateAndRotate(pose);
        if(s.hair()!=null)root.getChild("h"+style).render(pose,out.getBuffer(RenderType.entityCutoutNoCull(sheet(s.hair()))),light,OverlayTexture.NO_OVERLAY,0xFF000000|lift(colour));
        if(s.fabric()!=null)root.getChild("f"+style).render(pose,out.getBuffer(RenderType.entityCutoutNoCull(sheet(s.fabric()))),light,OverlayTexture.NO_OVERLAY,-1);
        pose.popPose();
    }
    /** The patterns are drawn in light greys; the tint is lifted a little so darker hair still shows its texture. */
    private static int lift(int rgb){int r=rgb>>16&255,g=rgb>>8&255,b=rgb&255;return Math.min(255,r+r/4+16)<<16|Math.min(255,g+g/4+16)<<8|Math.min(255,b+b/4+16);}

    private static final Map<ResourceLocation,Integer> COLOURS=new HashMap<>();
    /** The reader's hair colour: the commonest colour on the crown of their skin (the hat layer if they drew hair there). */
    public static int colour(AbstractClientPlayer reader){
        var texture=reader.getSkin().texture();var known=COLOURS.get(texture);if(known!=null)return known;
        var mc=Minecraft.getInstance();var tex=mc.getTextureManager().getTexture(texture);RenderSystem.bindTexture(tex.getId());
        int w=GlStateManager._getTexLevelParameter(GL11.GL_TEXTURE_2D,0,GL11.GL_TEXTURE_WIDTH),h=GlStateManager._getTexLevelParameter(GL11.GL_TEXTURE_2D,0,GL11.GL_TEXTURE_HEIGHT);
        if(w<64||h<64||w!=h)return HouseBodyClient.defaults(reader.getUUID())[1];
        try(var image=new NativeImage(w,h,false)){
            image.downloadTexture(0,false);int scale=w/64;var hat=new HashMap<Integer,Integer>();var crown=new HashMap<Integer,Integer>();
            for(int y=0;y<8;y++)for(int x=0;x<8;x++){int top=argb(image.getPixelRGBA((40+x)*scale,y*scale)),base=argb(image.getPixelRGBA((8+x)*scale,y*scale));
                if(top>>>24>=128)hat.merge(top&0xffffff,1,Integer::sum);if(base>>>24>=128)crown.merge(base&0xffffff,1,Integer::sum);}
            var counts=hat.values().stream().mapToInt(Integer::intValue).sum()>=24?hat:crown;
            int found=counts.entrySet().stream().max(Map.Entry.<Integer,Integer>comparingByValue().thenComparing(Map.Entry.comparingByKey())).map(Map.Entry::getKey).orElse(HouseBodyClient.defaults(reader.getUUID())[1]);
            COLOURS.put(texture,found);return found;
        }
    }
    private static int argb(int abgr){return (abgr&0xff00ff00)|(abgr&255)<<16|(abgr>>16&255);}

    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Registration {
        @SubscribeEvent public static void definitions(EntityRenderersEvent.RegisterLayerDefinitions e){e.registerLayerDefinition(LAYER,HouseReadHair::layer);}
    }
}
