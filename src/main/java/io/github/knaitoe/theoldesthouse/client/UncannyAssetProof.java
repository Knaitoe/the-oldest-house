package io.github.knaitoe.theoldesthouse.client;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

/** Native readback of all new paintings, folded bodies, nineteen fittings and both real item models. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class UncannyAssetProof extends Screen {
    private static boolean launched;private int page,frames;
    public UncannyAssetProof(){super(Component.literal("Six uncanny rooms / native assets"));}
    @SubscribeEvent public static void launch(ClientTickEvent.Post ignored){var mc=Minecraft.getInstance();if(Boolean.getBoolean("the_oldest_house.uncannySmoke")&&!launched&&mc.screen!=null&&mc.getOverlay()==null){launched=true;mc.options.guiScale().set(2);mc.resizeDisplay();mc.setScreen(new UncannyAssetProof());}}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xff252827);g.drawString(font,page<2?"Native eight-pixel faces and poses / "+(page==0?"front":"side"):"Painted boards, CRTs, signs, flesh folds and physical artifacts",10,9,0xffe4d9bf,false);g.flush();
        var p=g.pose();var buffers=mc.renderBuffers().bufferSource();
        if(page<2){
            int[] roles={32,33,34,34,35,36,37,38};int[] phases={1,0,1,3,2,1,1,1};
            for(int i=0;i<roles.length;i++){
                var texture=UncannyTextures.skin(roles[i],phases[i]);
                if(roles[i]==LiteraryActor.UNCANNY_WAITRESS||roles[i]==LiteraryActor.UNCANNY_FALSE_WAITRESS){
                    try(var stream=mc.getResourceManager().open(texture);var painting=NativeImage.read(stream)){
                        if(painting.getWidth()!=256||painting.getHeight()!=256||(painting.getPixelRGBA(40,44)>>>24)!=255)throw new IllegalStateException("The waitress must use a complete standard-UV cast painting");
                    }catch(java.io.IOException e){throw new IllegalStateException("Missing waitress painting",e);}
                }else{
                    var nativeTexture=mc.getTextureManager().getTexture(texture);if(!(nativeTexture instanceof DynamicTexture d)||d.getPixels()==null)throw new IllegalStateException("Uncanny skin is not a native pixel painting");
                    if((d.getPixels().getPixelRGBA(10,11)>>>24)!=255)throw new IllegalStateException("A native eight-pixel face has a hole");
                }
                var model=new UncannyActorRenderer.Model(mc.getEntityModels().bakeLayer(ModelLayers.PLAYER),false);model.pose(roles[i],phases[i],false,24,1,.5F,0,0);
                p.pushPose();p.translate((i%4+.5)*width/4,70+(i/4+1)*(height-100)/2.,150);float size=roles[i]==35?38:58;p.scale(size,size,-size);if(page==1)p.mulPose(Axis.YP.rotationDegrees(80));if(roles[i]==35)p.scale(2.5F,2.5F,2.5F);p.translate(0,-1.501,0);
                model.renderToBuffer(p,buffers.getBuffer(roles[i]==34&&phases[i]<2?RenderType.entityTranslucent(texture):RenderType.entityCutoutNoCull(texture)),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,roles[i]==34&&phases[i]<2?0x50ffffff:0xffffffff);buffers.endBatch();p.popPose();
                g.drawCenteredString(font,new String[]{"The Propped","The waitress","The draft","The second apron","Stooping","Direct hallway","The peeler","On the step"}[i],(int)((i%4+.5)*width/4),(int)(75+(i/4+1)*(height-100)/2.),0xffe4d9bf);
            }
        }else{
            int i=0;for(var kind:UncannyPropBlock.Kind.values()){
                p.pushPose();p.translate((i%5+.5)*width/5,60+(i/5)*(height-120)/4.,150);p.scale(43,-43,43);p.mulPose(Axis.XP.rotationDegrees(22));p.mulPose(Axis.YP.rotationDegrees(28));p.translate(-.5,0,-.5);
                UncannyPropRenderer.draw(UncannyPropBlock.of(kind,Direction.SOUTH),p,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);buffers.endBatch();p.popPose();g.drawCenteredString(font,kind.getSerializedName(),(int)((i%5+.5)*width/5),75+(i/5)*(height-120)/4,0xffe4d9bf);i++;
            }
            int n=0;for(var item:new ItemStack[]{new ItemStack(UncannyRegistry.BONE_RING.get()),new ItemStack(UncannyRegistry.SMOKY_MIRROR.get()),new ItemStack(UncannyRegistry.CLEAVER.get()),new ItemStack(UncannyRegistry.BAT.get()),new ItemStack(UncannyRegistry.MEAL.get())}){p.pushPose();p.translate(24+n*80,height-65,0);p.scale(3,3,1);g.renderItem(item,0,0);p.popPose();n++;}
        }
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post ignored)throws Exception{
        var mc=Minecraft.getInstance();if(!(mc.screen instanceof UncannyAssetProof proof)||++proof.frames<16)return;
        var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);var file=dir.resolve("native-uncanny-"+proof.page+".png");
        try(var shot=Screenshot.takeScreenshot(mc.getMainRenderTarget())){shot.writeToFile(file);}
        if(Boolean.getBoolean("the_oldest_house.uncannySmoke")){String data=java.util.Base64.getEncoder().encodeToString(Files.readAllBytes(file));TheOldestHouse.LOGGER.info("UNCANNY_ASSET_VIEW_BEGIN {}",proof.page);for(int i=0;i<data.length();i+=3800)TheOldestHouse.LOGGER.info("UNCANNY_ASSET_VIEW_DATA {}",data.substring(i,Math.min(i+3800,data.length())));TheOldestHouse.LOGGER.info("UNCANNY_ASSET_VIEW_END");}
        proof.frames=0;if(++proof.page<3)return;
        var atlas=mc.getTextureManager().getTexture(UncannyTextures.atlas());if(!(atlas instanceof DynamicTexture d)||d.getPixels()==null||d.getPixels().getWidth()!=64||d.getPixels().getHeight()!=64)throw new IllegalStateException("The native sixteen-pixel material atlas changed");
        d.getPixels().writeToFile(dir.resolve("native-uncanny-materials.png"));Files.writeString(dir.resolve("uncanny-assets-passed.txt"),"Native front/side poses, eight pixel-painted skin variants, nineteen fitted props, a 64-pixel material atlas and five physical item models. Existing PNG resources are unchanged.\n");
        if(Boolean.getBoolean("the_oldest_house.uncannySmoke")||Boolean.getBoolean("the_oldest_house.uncannyEnvironmentSmoke"))mc.stop();else mc.setScreen(new PlaytestVisualProof());
    }
}
