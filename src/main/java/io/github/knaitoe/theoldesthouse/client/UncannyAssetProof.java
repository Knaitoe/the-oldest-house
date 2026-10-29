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
                if(roles[i]==LiteraryActor.UNCANNY_PROPPED||roles[i]==LiteraryActor.UNCANNY_GIANT||roles[i]==LiteraryActor.UNCANNY_WAITRESS||roles[i]==LiteraryActor.UNCANNY_FALSE_WAITRESS||roles[i]==LiteraryActor.UNCANNY_PEELER||roles[i]==LiteraryActor.UNCANNY_VISITOR){
                    try(var stream=mc.getResourceManager().open(texture);var painting=NativeImage.read(stream)){
                        if(painting.getWidth()!=256||painting.getHeight()!=256||(painting.getPixelRGBA(40,44)>>>24)!=255)throw new IllegalStateException("The human cast must use complete standard-UV paintings");
                    }catch(java.io.IOException e){throw new IllegalStateException("Missing human cast painting",e);}
                }else{
                    var nativeTexture=mc.getTextureManager().getTexture(texture);if(!(nativeTexture instanceof DynamicTexture d)||d.getPixels()==null)throw new IllegalStateException("Uncanny skin is not a native pixel painting");
                    if((d.getPixels().getPixelRGBA(10,11)>>>24)!=255)throw new IllegalStateException("A native eight-pixel face has a hole");
                }
                var model=new UncannyActorRenderer.Model(mc.getEntityModels().bakeLayer(roles[i]==LiteraryActor.UNCANNY_GIANT?UncannyActorRenderer.BENT:UncannyActorRenderer.WIDE),false);model.pose(roles[i],phases[i],false,24,1,.5F,0,0);
                p.pushPose();p.translate((i%4+.5)*width/4,70+(i/4+1)*(height-100)/2.,150);float size=roles[i]==35?38:58;p.scale(size,size,-size);if(page==1)p.mulPose(Axis.YP.rotationDegrees(80));if(roles[i]==35)p.scale(2.5F,2.5F,2.5F);p.translate(0,-1.501,0);
                model.renderToBuffer(p,buffers.getBuffer(roles[i]==34&&phases[i]<2?RenderType.entityTranslucent(texture):RenderType.entityCutoutNoCull(texture)),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,roles[i]==34&&phases[i]<2?0x50ffffff:0xffffffff);buffers.endBatch();p.popPose();
                g.drawCenteredString(font,new String[]{"The Propped","The waitress","The draft","The second apron","Stooping","Direct hallway","The peeler","On the step"}[i],(int)((i%4+.5)*width/4),(int)(75+(i/4+1)*(height-100)/2.),0xffe4d9bf);
            }
        }else if(page==2){
            int i=0;for(var kind:UncannyPropBlock.Kind.values()){
                int rows=(UncannyPropBlock.Kind.values().length+4)/5;float spacing=(height-130F)/rows;
                p.pushPose();p.translate((i%5+.5)*width/5,50+(i/5)*spacing,150);p.scale(36,-36,36);p.mulPose(Axis.XP.rotationDegrees(22));p.mulPose(Axis.YP.rotationDegrees(28));p.translate(-.5,0,-.5);
                UncannyPropRenderer.draw(UncannyPropBlock.of(kind,Direction.SOUTH),p,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);buffers.endBatch();p.popPose();g.drawCenteredString(font,kind.getSerializedName(),(int)((i%5+.5)*width/5),(int)(63+(i/5)*spacing),0xffe4d9bf);i++;
            }
            int n=0;for(var item:new ItemStack[]{new ItemStack(UncannyRegistry.BONE_RING.get()),new ItemStack(UncannyRegistry.SMOKY_MIRROR.get()),new ItemStack(UncannyRegistry.CLEAVER.get()),new ItemStack(UncannyRegistry.BAT.get()),new ItemStack(UncannyRegistry.MEAL.get())}){p.pushPose();p.translate(24+n*80,height-65,0);p.scale(3,3,1);g.renderItem(item,0,0);p.popPose();n++;}
        }else if(page==3){
            for(int i=0;i<8;i++){
                var state=UncannyRegistry.FLESH.get().defaultBlockState().setValue(UncannyFleshBlock.LAYER,i);
                if(mc.getBlockRenderer().getBlockModel(state)==mc.getModelManager().getMissingModel())throw new IllegalStateException("Missing native tissue model "+i);
                p.pushPose();p.translate((i%4+.5F)*width/4,height*(.3F+i/4*.3F),150);p.scale(48,-48,48);p.mulPose(Axis.XP.rotationDegrees(22));p.mulPose(Axis.YP.rotationDegrees(30));p.translate(-.5,0,-.5);mc.getBlockRenderer().renderSingleBlock(state,p,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);buffers.endBatch();p.popPose();
            }
            var key=new ItemStack(UncannyRegistry.CELLAR_KEY.get());p.pushPose();p.translate(width*.45F,height*.72F,0);p.scale(4,4,1);g.renderItem(key,0,0);p.popPose();g.drawCenteredString(font,key.getHoverName(),width/2,(int)(height*.72F)+75,0xffe4d9bf);
            var icon=net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/mob_effect/engulfed.png");g.blit(icon,20,height-60,0,0,18,18,18,18);
        }else if(page==7){
            for(int i=0;i<3;i++){
                var model=new UncannyActorRenderer.Model(mc.getEntityModels().bakeLayer(UncannyActorRenderer.WIDE),false);model.pose(LiteraryActor.UNCANNY_VISITOR,i+1,false,24,0,0,0,0);
                p.pushPose();p.translate((i+.5)*width/3,height*.52F,150);p.scale(67,67,-67);p.translate(0,-1.501,0);model.renderToBuffer(p,buffers.getBuffer(RenderType.entityCutoutNoCull(UncannyTextures.skin(LiteraryActor.UNCANNY_VISITOR,i+1))),LightTexture.pack(9,0),OverlayTexture.NO_OVERLAY);buffers.endBatch();p.popPose();
                g.drawCenteredString(font,new String[]{"First stillness: distant","Second stillness: closer","Third stillness: hand to glass"}[i],(int)((i+.5)*width/3),(int)(height*.57F),0xffe4d9bf);
                p.pushPose();p.translate((i+.5)*width/3,height*.82F,150);p.scale(64,-64,64);p.translate(-.5,0,-.5);UncannyPropRenderer.draw(UncannyPropBlock.of(UncannyPropBlock.Kind.COURTYARD_PENDULUM,Direction.SOUTH),p,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,new float[]{-.38F,0,.38F}[i]);buffers.endBatch();p.popPose();
            }
        }else if(page==8){
            g.drawCenteredString(font,"Stooping under the ceiling / normal stride above / great catch-up stride below",width/2,27,0xffe4d9bf);
            for(int i=0;i<6;i++){
                var model=new UncannyActorRenderer.Model(mc.getEntityModels().bakeLayer(UncannyActorRenderer.BENT),false);model.pose(LiteraryActor.UNCANNY_GIANT,2,false,24,(i%3)*2.1F,.65F,0,0,i>=3);
                checkTunnelHeight(model);
                p.pushPose();p.translate((i%3+.5)*width/3,58+(i/3+.9)*(height-88)/2.,150);p.scale(42,42,-42);if(i>=3)p.mulPose(Axis.YP.rotationDegrees(90));p.scale(2.5F,2.5F,2.5F);p.translate(0,-1.501,0);model.renderToBuffer(p,buffers.getBuffer(RenderType.entityCutoutNoCull(UncannyTextures.skin(LiteraryActor.UNCANNY_GIANT,2))),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);buffers.endBatch();p.popPose();
            }
        }else{
            g.fill(0,0,width,height,0xff424c47);for(int column=0;column<width;column+=32)g.fill(column,height/2,column+16,height/2+2,0xffc9c4af);
            UncannyClient.engulfed(g,width,height,new float[]{.2F,.6F,.95F}[page-4],24);
        }
    }
    private static void checkTunnelHeight(UncannyActorRenderer.Model model){
        for(var part:new net.minecraft.client.model.geom.ModelPart[]{model.head,model.body,model.leftArm,model.rightArm,model.leftLeg,model.rightLeg,model.neck})part.visit(new com.mojang.blaze3d.vertex.PoseStack(),(pose,path,index,cube)->{
            for(float x:new float[]{cube.minX,cube.maxX})for(float y:new float[]{cube.minY,cube.maxY})for(float z:new float[]{cube.minZ,cube.maxZ}){
                var vertex=pose.pose().transformPosition(new org.joml.Vector3f(x/16,y/16,z/16));float height=(1.501F-vertex.y)*2.5F;
                if(height>=3)throw new IllegalStateException("The production injured-woman gait crosses the actual recessed-light ceiling: "+height+" at "+path);
            }
        });
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post ignored)throws Exception{
        var mc=Minecraft.getInstance();if(!(mc.screen instanceof UncannyAssetProof proof)||++proof.frames<16)return;
        var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);var file=dir.resolve("native-uncanny-"+proof.page+".png");
        try(var shot=Screenshot.takeScreenshot(mc.getMainRenderTarget())){shot.writeToFile(file);}
        if(Boolean.getBoolean("the_oldest_house.uncannySmoke")){String data=java.util.Base64.getEncoder().encodeToString(Files.readAllBytes(file));TheOldestHouse.LOGGER.info("UNCANNY_ASSET_VIEW_BEGIN {}",proof.page);for(int i=0;i<data.length();i+=3800)TheOldestHouse.LOGGER.info("UNCANNY_ASSET_VIEW_DATA {}",data.substring(i,Math.min(i+3800,data.length())));TheOldestHouse.LOGGER.info("UNCANNY_ASSET_VIEW_END");}
        proof.frames=0;if(++proof.page<9)return;
        var atlas=mc.getTextureManager().getTexture(UncannyTextures.atlas());if(!(atlas instanceof DynamicTexture d)||d.getPixels()==null||d.getPixels().getWidth()!=64||d.getPixels().getHeight()!=64)throw new IllegalStateException("The native sixteen-pixel material atlas changed");
        d.getPixels().writeToFile(dir.resolve("native-uncanny-materials.png"));Files.writeString(dir.resolve("uncanny-assets-passed.txt"),"Nine native pages: front/side poses, eight skin variants including the exhausted worker, fitted props, material atlas, four actual tissue models, the dedicated cellar key and status icon, three engulfment overlay levels, three courtyard visitor stages and physical pendulum angles, and six production segmented injured-woman gait views.\n");
        if(Boolean.getBoolean("the_oldest_house.uncannySmoke")||Boolean.getBoolean("the_oldest_house.uncannyEnvironmentSmoke"))mc.stop();else mc.setScreen(new PlaytestVisualProof());
    }
}
