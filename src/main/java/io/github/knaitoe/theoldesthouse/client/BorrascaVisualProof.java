package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Native renderer review, explicitly staged: four faces, connected joints, fitted materials and rotating rollers. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class BorrascaVisualProof extends Screen {
    private int page,frames;private BorrascaNativeArt.CastModel cast;private static boolean launched;
    public BorrascaVisualProof(){super(Component.literal("Drisking / native materials and joints"));}
    @SubscribeEvent public static void launch(ClientTickEvent.Post ignored){var mc=Minecraft.getInstance();if(Boolean.getBoolean("the_oldest_house.borrascaSmoke")&&!launched&&mc.screen!=null&&mc.getOverlay()==null){launched=true;mc.options.guiScale().set(2);mc.resizeDisplay();NativeModelTextures.verify();mc.setScreen(new BorrascaVisualProof());}}
    @Override protected void init(){var mc=Minecraft.getInstance();cast=new BorrascaNativeArt.CastModel(mc.getEntityModels().bakeLayer(BorrascaNativeArt.CAST));var books=new java.util.ArrayList<net.minecraft.world.item.ItemStack>();books.add(BorrascaTexts.source());for(String key:java.util.List.of("Bedroom","Register","Map","Service","Evidence","Testimony"))books.add(BorrascaTexts.document(key));for(boolean stopped:new boolean[]{false,true}){var own=new net.minecraft.nbt.CompoundTag();own.putBoolean("PowerOff",stopped);books.add(BorrascaTexts.accountBook("Native reader",own));}for(var book:books)for(var page:book.get(net.minecraft.core.component.DataComponents.WRITTEN_BOOK_CONTENT).pages())if(mc.font.split(page.raw(),114).size()>14)throw new IllegalStateException("A Drisking document exceeds its native lectern page");}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    @Override public void render(GuiGraphics g,int x,int y,float partial){var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xff252925);g.drawString(font,"BORRASCA / "+(page<4?"four original faces / "+new String[]{"front","side","back","dim"}[page]:"native fitted wood, steel, paper and continuously turning drum"),10,10,0xffe8ddc9,false);g.flush();var buffers=mc.renderBuffers().bufferSource();int light=page==3?LightTexture.pack(7,0):LightTexture.FULL_BRIGHT;
        if(page<4){for(int person=0;person<4;person++)for(int stride=0;stride<3;stride++){var pose=g.pose();pose.pushPose();float row=(height-60)/3F,scale=Math.min(width/4*.35F,row*.42F);pose.translate((person+.5F)*width/4,40+stride*row+scale*.5F,200);pose.scale(scale,scale,scale);pose.mulPose(Axis.YP.rotationDegrees(new float[]{180,90,0,150}[page]));cast.preview(person,stride*7+frames,.7F);cast.renderToBuffer(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(BorrascaNativeArt.ATLAS)),light,OverlayTexture.NO_OVERLAY,0xffffffff);buffers.endBatch();pose.popPose();}}
        else{int i=0;for(var kind:BorrascaFittingBlock.Kind.values())for(var facing:Direction.Plane.HORIZONTAL){var state=BorrascaFittingBlock.of(kind,facing);if(mc.getBlockRenderer().getBlockModel(state)==mc.getModelManager().getMissingModel())throw new IllegalStateException("Missing Borrasca fitting "+kind+" "+facing);var pose=g.pose();pose.pushPose();pose.translate(20+(i%9+.5F)*(width-40)/9,35+(i/9+.7F)*(height-90)/4,200);pose.scale(24,-24,24);pose.mulPose(Axis.XP.rotationDegrees(20));pose.mulPose(Axis.YP.rotationDegrees(30));mc.getBlockRenderer().renderSingleBlock(state,pose,buffers,light,OverlayTexture.NO_OVERLAY);buffers.endBatch();pose.popPose();i++;}var pose=g.pose();pose.pushPose();pose.translate(width*.5F,height-35,210);pose.scale(35,-35,35);pose.mulPose(Axis.YP.rotationDegrees(35));BorrascaNativeArt.drawMachine(pose,buffers,light,OverlayTexture.NO_OVERLAY,frames*.12F+page);buffers.endBatch();pose.popPose();}
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{var mc=Minecraft.getInstance();boolean direct=Boolean.getBoolean("the_oldest_house.borrascaSmoke");if(!(direct||Boolean.getBoolean("the_oldest_house.fontSmoke"))||!(mc.screen instanceof BorrascaVisualProof s)||++s.frames<24)return;var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);String name=s.page<4?new String[]{"front","side","back","dim"}[s.page]:"fixtures-"+(s.page-4);try(NativeImage image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve("native-borrasca-"+name+".png"));}if(++s.page<6){s.frames=0;return;}Files.writeString(dir.resolve("borrasca-assets-passed.txt"),"Six native GPU views: four painted faces at three connected gait samples, front/side/back/dim; every fitted material in four facings; original machine roller at two continuous angles. Staged assets, not a complete ordinary playthrough.\n");if(direct)mc.stop();else mc.setScreen(new HallChangeVisualProof());}
}
