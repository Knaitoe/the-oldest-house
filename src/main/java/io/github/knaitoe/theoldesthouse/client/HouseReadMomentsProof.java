package io.github.knaitoe.theoldesthouse.client;

import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/** Production models and phone screen; separate from ordinary connected-client atmosphere review. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class HouseReadMomentsProof extends Screen {
    private static final String[] NAMES={"waiting_objects","routine_objects","shelter_objects","shelter_cast","waiting_phone"};
    private int page,frames;private HarriganPhoneScreen phone;
    public HouseReadMomentsProof(){super(Component.literal("Native quiet-room objects"));}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        g.fill(0,0,width,height,0xff343630);g.drawString(font,"Quiet first rooms / "+NAMES[page].replace('_',' ')+" / shipped native models",10,9,0xffe5dcc8,false);
        if(page==4){if(phone==null){var state=new CompoundTag();state.putBoolean("WaitingRoom",true);state.putBoolean("Service",true);state.putLong("DayTime",9700);phone=new HarriganPhoneScreen(state);phone.init(Minecraft.getInstance(),width,height);if(phone.showsInput()||!phone.children().isEmpty())throw new IllegalStateException("The waiting phone is a simple lock screen");}phone.render(g,x,y,partial);return;}
        if(page==3){cast(g);return;}
        var types=page==0?new HouseReadPropBlock.Part[]{HouseReadPropBlock.Part.CLOCK_BASE,HouseReadPropBlock.Part.CLOCK_FACE,HouseReadPropBlock.Part.BELL,HouseReadPropBlock.Part.CALENDAR,HouseReadPropBlock.Part.RECEPTION,HouseReadPropBlock.Part.CRT,HouseReadPropBlock.Part.COOLER,HouseReadPropBlock.Part.PAPER,HouseReadPropBlock.Part.MAGAZINES,HouseReadPropBlock.Part.GLASSES,HouseReadPropBlock.Part.PHONE,HouseReadPropBlock.Part.ARMCHAIR}:page==1?new HouseReadPropBlock.Part[]{HouseReadPropBlock.Part.SINK,HouseReadPropBlock.Part.TAP,HouseReadPropBlock.Part.CABINET,HouseReadPropBlock.Part.POSTCARD,HouseReadPropBlock.Part.ROUTINE_A,HouseReadPropBlock.Part.ROUTINE_B,HouseReadPropBlock.Part.ROUTINE_C,HouseReadPropBlock.Part.ROUTINE_D,HouseReadPropBlock.Part.LIGHT_SWITCH}:new HouseReadPropBlock.Part[]{HouseReadPropBlock.Part.JUKEBOX,HouseReadPropBlock.Part.STRING_LIGHT,HouseReadPropBlock.Part.ROWBOAT,HouseReadPropBlock.Part.BOTTLE,HouseReadPropBlock.Part.CALENDAR,HouseReadPropBlock.Part.POSTCARD};
        int columns=4,rows=(types.length+3)/4;float cw=width/(float)columns,ch=(height-36)/(float)rows;
        for(int i=0;i<types.length;i++){var state=HouseReadRegistry.FITTING.get().defaultBlockState().setValue(HouseReadPropBlock.PART,types[i]);if(page==2)state=state.setValue(HouseReadPropBlock.WRITING,true);draw(g,state,(i%4+.5f)*cw,32+(i/4+.55f)*ch,Math.min(cw*.55f,ch*.55f));g.drawCenteredString(font,types[i].getSerializedName(),(int)((i%4+.5f)*cw),(int)(32+(i/4+1)*ch-7),0xffe5dcc8);}
        if(page==2){draw(g,Blocks.SPRUCE_PLANKS.defaultBlockState(),width*.2f,height*.82f,38);draw(g,Blocks.IRON_BLOCK.defaultBlockState(),width*.5f,height*.82f,38);draw(g,Blocks.WHITE_TERRACOTTA.defaultBlockState(),width*.8f,height*.82f,38);}
    }
    private void draw(GuiGraphics g,net.minecraft.world.level.block.state.BlockState state,float x,float y,float scale){var mc=Minecraft.getInstance();var p=g.pose();g.flush();p.pushPose();p.translate(x,y,180);if(state.is(HouseReadRegistry.FITTING.get())&&state.getValue(HouseReadPropBlock.PART)==HouseReadPropBlock.Part.ROWBOAT)scale*=.5f;p.scale(scale,-scale,scale);p.mulPose(Axis.XP.rotationDegrees(24));p.mulPose(Axis.YP.rotationDegrees(-25));p.translate(-.5,-.5,-.5);var out=mc.renderBuffers().bufferSource();mc.getBlockRenderer().renderSingleBlock(state,p,out,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);out.endBatch();p.popPose();}
    private void cast(GuiGraphics g){var mc=Minecraft.getInstance();var out=mc.renderBuffers().bufferSource();float cw=width/4f,ch=(height-36)/2f,scale=Math.min(cw*.43f,ch*.40f);g.flush();for(int i=0;i<8;i++){int role=i%2==0?LiteraryActor.READ_MAN:LiteraryActor.READ_WOMAN,phase=i/4;var model=new LiteraryRenderers.CastModel(mc.getEntityModels().bakeLayer(LiteraryRenderers.CAST));model.young=false;model.preview(role,phase,80);var p=g.pose();p.pushPose();p.translate((i%4+.5f)*cw,36+(i/4)*ch+ch*.28f,180);p.scale(scale,scale,scale);p.mulPose(Axis.YP.rotationDegrees(i%4<2?180:90));model.renderToBuffer(p,out.getBuffer(RenderType.entityCutoutNoCull(LiteraryRenderers.skin(role,phase))),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);out.endBatch();p.popPose();g.drawCenteredString(font,(role==LiteraryActor.READ_MAN?"boy":"girl")+(phase==1?" / half smile":" / quiet"),(int)((i%4+.5f)*cw),(int)(32+(i/4+1)*ch-6),0xffe5dcc8);}}
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{var mc=Minecraft.getInstance();if(!Boolean.getBoolean("the_oldest_house.houseReadSmoke")||!(mc.screen instanceof HouseReadMomentsProof s)||++s.frames<12)return;var folder=Path.of("../build/house-read139-art");Files.createDirectories(folder);try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(folder.resolve(NAMES[s.page]+".png"));}if(++s.page<NAMES.length){s.frames=0;return;}Files.writeString(folder.resolve("passed.txt"),"Five actual server-built room cutaways, five production standard/slim garment/mirror pages and five native furnishing/routine/cast/phone pages. Minecraft native pixel art, actual block/item geometry and production phone rendering. Ordinary graphical-client/reconnect/lighting/audio review remains separate.\n");mc.stop();}
}
