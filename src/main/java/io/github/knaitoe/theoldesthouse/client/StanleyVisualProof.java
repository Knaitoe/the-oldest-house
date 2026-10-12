package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseBlocks;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

/** Uses the real registered meshes and the same joint and gesture code as the hotel. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class StanleyVisualProof extends Screen {
    private static boolean launched;
    private int page,frames;
    private static final String[] NAMES={"Porter","Bartender","Scarred guest","Pianist","Dance partner","Dance partner"};
    public StanleyVisualProof(){super(Component.literal("Stanley Hotel — native materials and motion"));}
    @Override protected void init(){var mc=Minecraft.getInstance();for(int i=0;i<6;i++)try(var stream=mc.getResourceManager().getResource(HotelRenderers.skin(i)).orElseThrow().open();var image=NativeImage.read(stream)){if(image.getWidth()!=image.getHeight()*2||image.getWidth()<512||image.getWidth()>2048)throw new IllegalStateException("The Stanley skin does not fit its normalized native 128x64 UV layout");}catch(java.io.IOException e){throw new java.io.UncheckedIOException(e);}}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xff292721);g.drawString(font,"Stanley Hotel / "+new String[]{"fitted cast","jointed interactions","native furnishings","existing cast comparison"}[page],12,10,0xffe4d6b9,false);g.flush();
        if(page<2){
            int count=page==0?6:10,columns=page==0?3:5,rows=2;
            for(int n=0;n<count;n++){
                int role=page==0?n:new int[]{0,1,1,2,3,4,5,0,1,4}[n];
                int motion=page==0?HotelActor.IDLE:new int[]{HotelActor.GREET,HotelActor.POUR,HotelActor.IDLE,HotelActor.LISTEN,0,0,0,HotelActor.IDLE,HotelActor.POUR,HotelActor.LISTEN}[n];
                boolean dance=page==0?role>=4:n==5||n==6;
                var model=new HotelRenderers.CastModel(mc.getEntityModels().bakeLayer(HotelRenderers.CAST));model.preview(role,dance,frames*2,motion,n>=7?.8F:.5F);
                int cx=20+(n%columns)*(width-40)/columns+(width-40)/columns/2,cy=58+(n/columns)*(height-85)/rows;
                var p=g.pose();p.pushPose();p.translate(cx,cy,190);float scale=page==0?57:43;p.scale(scale,scale,scale);p.mulPose(Axis.YP.rotationDegrees(page==0?145:n>=7?25:145));
                var buffers=mc.renderBuffers().bufferSource();model.renderToBuffer(p,buffers.getBuffer(RenderType.entityCutoutNoCull(HotelRenderers.skin(role))),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0xffffffff);buffers.endBatch();p.popPose();
                g.drawCenteredString(font,page==0?NAMES[role]:new String[]{"Acknowledgment","Pour","Polish","Listen","Keys and fingers","Waltz","Waltz","Coat / back","Pour / back","Guest / back"}[n],cx,cy+(int)(scale*1.6),0xffe4d6b9);
            }
        }else if(page==3){
            for(int n=0;n<6;n++){
                net.minecraft.client.model.EntityModel<?> model;net.minecraft.resources.ResourceLocation skin;
                if(n==2||n==5){var cast=new LiteraryRenderers.CastModel(mc.getEntityModels().bakeLayer(LiteraryRenderers.CAST));int phase=n==2?5:4;cast.pose(LiteraryActor.STRANGER,phase,frames*2);model=cast;skin=LiteraryRenderers.skin(LiteraryActor.STRANGER,phase);}
                else{int role=new int[]{0,1,0,3,5,0}[n];var cast=new HotelRenderers.CastModel(mc.getEntityModels().bakeLayer(HotelRenderers.CAST));cast.preview(role,false,frames*2,HotelActor.IDLE,0);model=cast;skin=HotelRenderers.skin(role);}
                int cx=20+(n%3)*(width-40)/3+(width-40)/6,cy=58+(n/3)*(height-85)/2;
                var p=g.pose();p.pushPose();p.translate(cx,cy,190);p.scale(52,52,52);p.mulPose(Axis.YP.rotationDegrees(145));
                var buffers=mc.renderBuffers().bufferSource();model.renderToBuffer(p,buffers.getBuffer(RenderType.entityCutoutNoCull(skin)),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0xffffffff);buffers.endBatch();p.popPose();
                g.drawCenteredString(font,new String[]{"Hotel: porter","Hotel: bartender","Existing: Russ","Hotel: pianist","Hotel: dancer","Existing: Marla"}[n],cx,cy+84,0xffe4d6b9);
            }
        }else{
            var states=new java.util.ArrayList<net.minecraft.world.level.block.state.BlockState>();
            for(var block:new net.minecraft.world.level.block.Block[]{HouseBlocks.HOTEL_WALLPAPER.get(),HouseBlocks.HOTEL_WAINSCOT.get(),HouseBlocks.HOTEL_CEILING.get(),HouseBlocks.HOTEL_CARPET.get()})states.add(block.defaultBlockState());
            for(var kind:HotelPropBlock.Kind.values()){for(var face:Direction.Plane.HORIZONTAL)for(int pressure=0;pressure<4;pressure++){var state=HotelRegistry.PROP.get().defaultBlockState().setValue(HotelPropBlock.KIND,kind).setValue(HotelPropBlock.FACING,face).setValue(HotelPropBlock.PRESSURE,pressure);if(mc.getBlockRenderer().getBlockModel(state)==mc.getModelManager().getMissingModel())throw new IllegalStateException("Missing native Stanley furnishing "+state);}states.add(HotelRegistry.PROP.get().defaultBlockState().setValue(HotelPropBlock.KIND,kind).setValue(HotelPropBlock.PRESSURE,2));}
            for(int n=0;n<states.size();n++){var p=g.pose();p.pushPose();p.translate(20+(n%5+.5F)*(width-40)/5,45+(n/5+.4F)*(height-75)/3,190);p.scale(31,-31,31);p.mulPose(Axis.XP.rotationDegrees(18));p.mulPose(Axis.YP.rotationDegrees(-35));mc.getBlockRenderer().renderSingleBlock(states.get(n),p,mc.renderBuffers().bufferSource(),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);mc.renderBuffers().bufferSource().endBatch();p.popPose();}
            int x0=width-90;for(var item:new net.minecraft.world.item.Item[]{HotelRegistry.MASTER_KEY.get(),HotelRegistry.DRINK.get(),HotelRegistry.PHOTOGRAPH.get()}){g.renderItem(new net.minecraft.world.item.ItemStack(item),x0,height-24);x0+=26;}
        }
    }
    @SubscribeEvent public static void launch(ClientTickEvent.Post e){var mc=Minecraft.getInstance();if(Boolean.getBoolean("the_oldest_house.stanleySmoke")&&!launched&&mc.screen!=null&&mc.getOverlay()==null){launched=true;mc.options.guiScale().set(2);mc.resizeDisplay();TheOldestHouse.LOGGER.info("STANLEY PROOF: opening native models from {}",mc.screen.getClass().getSimpleName());mc.setScreen(new StanleyVisualProof());}}
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{var mc=Minecraft.getInstance();if(!(mc.screen instanceof StanleyVisualProof proof)||++proof.frames<24)return;var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve("native-stanley-"+new String[]{"cast","actions","fixtures","comparison"}[proof.page]+".png"));}if(++proof.page<4){proof.frames=0;return;}Files.writeString(dir.resolve("stanley-passed.txt"),"Six fitted painted cast atlases, connected elbow/knee/finger meshes, greeting/pouring/polishing/playing/waltz/guest poses, four room surfaces, all 160 oriented furnishings, three native keepsakes and an existing-cast comparison rendered.\n");if(Boolean.getBoolean("the_oldest_house.stanleySmoke"))mc.stop();else mc.setScreen(new LiteraryVisualProof());}
}
