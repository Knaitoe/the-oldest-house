package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/** Native baked support assemblies, interactive mesh states, reaching cast and the same painted herd with emissive eyes. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class Vignette0492VisualProof extends Screen {
    private static final String[] VIEWS={"supports","interactions","men","herd","courtyard","supper"};
    private int page,frames;
    public Vignette0492VisualProof(){super(Component.literal("October 13 vignette pass"));}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    private void block(BlockState state,GuiGraphics g,MultiBufferSource out,int light){var mc=Minecraft.getInstance();if(mc.getBlockRenderer().getBlockModel(state)==mc.getModelManager().getMissingModel())throw new IllegalStateException("Missing 0.4.92 mesh: "+state);mc.getBlockRenderer().renderSingleBlock(state,g.pose(),out,light,OverlayTexture.NO_OVERLAY);}
    private void assembly(GuiGraphics g,MultiBufferSource out,BlockState support,BlockState toy,float x,float y){var p=g.pose();p.pushPose();p.translate(x,y,150);p.scale(70,-70,70);p.mulPose(Axis.XP.rotationDegrees(25));p.mulPose(Axis.YP.rotationDegrees(28));p.translate(-.5,0,-.5);block(support,g,out,LightTexture.FULL_BRIGHT);p.translate(0,1,0);block(toy,g,out,LightTexture.FULL_BRIGHT);p.popPose();}
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        var mc=Minecraft.getInstance();var out=mc.renderBuffers().bufferSource();g.fill(0,0,width,height,0xff25272c);g.drawString(font,"0.4.92 / "+VIEWS[page],12,10,0xffeadfc9,false);g.flush();
        if(page==0){
            assembly(g,out,NurseryBlock.of(NurseryBlock.Kind.TOY_CHEST,Direction.NORTH),NurseryBlock.of(NurseryBlock.Kind.DOLL,Direction.NORTH,2),width/6F,height*.64F);
            assembly(g,out,NurseryBlock.of(NurseryBlock.Kind.SHELF,Direction.NORTH),NurseryBlock.of(NurseryBlock.Kind.DOLL,Direction.NORTH,1),width/2F,height*.64F);
            assembly(g,out,NurseryBlock.of(NurseryBlock.Kind.SHELF,Direction.NORTH),NurseryBlock.of(NurseryBlock.Kind.MUSIC_BOX,Direction.NORTH,1),width*5/6F,height*.64F);
            g.drawCenteredString(font,"Chest lid",width/6,height-28,0xffeadfc9);g.drawCenteredString(font,"Shelf doll",width/2,height-28,0xffeadfc9);g.drawCenteredString(font,"Shelf music box",width*5/6,height-28,0xffeadfc9);
        }else if(page==1){
            for(int i=0;i<8;i++){BlockState state=i<2?SceneDetailBlock.state(SceneDetailBlock.Kind.TABLE_LAMP,Direction.NORTH).setValue(SceneDetailBlock.LIT,i==0):i<4?CottageBlock.of(CottageBlock.Kind.CHESSBOARD,Direction.NORTH,i-2):RoadhouseBlock.of(RoadhouseBlock.Kind.DARTBOARD,Direction.NORTH,i-4);
                var p=g.pose();p.pushPose();p.translate(20+(i%4+.5F)*(width-40)/4F,60+(i/4+1)*(height-90)/2F,150);p.scale(70,-70,70);p.mulPose(Axis.XP.rotationDegrees(28));p.mulPose(Axis.YP.rotationDegrees(20));p.translate(-.5,0,-.5);block(state,g,out,i==1?LightTexture.pack(2,0):LightTexture.FULL_BRIGHT);p.popPose();}
        }else if(page==2){
            for(int i=0;i<3;i++){var who=ElkLotNight.MEN.get(i);var model=new LiteraryRenderers.CastModel(mc.getEntityModels().bakeLayer(LiteraryRenderers.CAST));model.young=false;model.pose(LiteraryActor.STRANGER,who.phase(),frames);model.reaching();
                var p=g.pose();p.pushPose();p.translate((i+.5F)*width/3F,height*.83F,190);p.scale(105,105,105);p.mulPose(Axis.YP.rotationDegrees(i==0?150:i==1?180:210));p.translate(0,-1.5,0);model.renderToBuffer(p,out.getBuffer(RenderType.entityCutoutNoCull(LiteraryRenderers.skin(LiteraryActor.STRANGER,who.phase()))),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0xffffffff);p.popPose();out.endBatch();g.drawCenteredString(font,who.name(),(int)((i+.5F)*width/3F),height-24,0xffeadfc9);}
        }else if(page==3){
            for(int i=0;i<3;i++){var model=new LiteraryRenderers.ElkModel(mc.getEntityModels().bakeLayer(LiteraryRenderers.ELK));var eyes=mc.getEntityModels().bakeLayer(LiteraryRenderers.ELK_EYES).getChild("eyes");var p=g.pose();p.pushPose();p.translate((i+.5F)*width/3F,height*.78F,190);p.scale(72,72,72);p.mulPose(Axis.YP.rotationDegrees(155+i*20));p.translate(0,-1.5,0);
                model.renderToBuffer(p,out.getBuffer(RenderType.entityCutoutNoCull(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/literary_elk.png"))),LightTexture.pack(3,0),OverlayTexture.NO_OVERLAY,0xffffffff);LiteraryRenderers.ElkEyes.draw(model,eyes,p,out);p.popPose();out.endBatch();}
        }else if(page==4){
            for(int i=0;i<2;i++){var p=g.pose();p.pushPose();p.translate((i+.5)*width/2,height*.74,190);p.scale(68,-68,68);p.mulPose(Axis.XP.rotationDegrees(25));p.mulPose(Axis.YP.rotationDegrees(-35));p.translate(-.5,0,-.5);
                block(HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.CHESS_TABLE,Direction.SOUTH),g,out,LightTexture.FULL_BRIGHT);p.pushPose();p.translate(0,1,0);block(VignetteDetailBlock.state(VignetteDetailBlock.Kind.CHESS,Direction.NORTH),g,out,LightTexture.FULL_BRIGHT);p.popPose();
                for(int side:new int[]{-2,2}){p.pushPose();p.translate(0,0,side);block(HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.CANE_CHAIR,side>0?Direction.NORTH:Direction.SOUTH),g,out,LightTexture.FULL_BRIGHT);p.popPose();}p.popPose();}
        }else{
            var p=g.pose();p.pushPose();p.translate(width/2,height*.76,190);p.scale(68,-68,68);p.mulPose(Axis.XP.rotationDegrees(28));p.mulPose(Axis.YP.rotationDegrees(-32));
            for(int bx=-1;bx<=1;bx++)for(int z=-3;z<=1;z++){p.pushPose();p.translate(bx,0,z);block(TrailerTableBlock.tile(bx,-64+z),g,out,LightTexture.FULL_BRIGHT);p.popPose();}
            for(int i=0;i<3;i++){double z=1.5-i*2;p.pushPose();p.translate(-.55,1.02,z);p.mulPose(Axis.XP.rotationDegrees(-90));p.scale(.85F,.85F,.85F);mc.getItemRenderer().renderStatic(new ItemStack(GoatmanRegistry.PLATE.get()),ItemDisplayContext.FIXED,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,p,out,null,0);p.popPose();
                if(i>0){var food=new ItemStack(GoatmanRegistry.BRAT.get());if(i==2)food.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(1));p.pushPose();p.translate(-.55,1.07,z);p.mulPose(Axis.XP.rotationDegrees(-90));p.scale(.55F,.55F,.55F);mc.getItemRenderer().renderStatic(food,ItemDisplayContext.FIXED,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,p,out,null,0);p.popPose();}
            }p.popPose();g.drawCenteredString(font,"Empty / cooked / burnt: the native place-setting transforms",width/2,34,0xffeadfc9);
        }
        out.endBatch();
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{var mc=Minecraft.getInstance();if(!Boolean.getBoolean("the_oldest_house.fontSmoke")||!(mc.screen instanceof Vignette0492VisualProof s)||++s.frames<20)return;var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);try(NativeImage image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve("native-vignette0492-"+VIEWS[s.page]+".png"));}if(++s.page<VIEWS.length){s.frames=0;return;}Files.writeString(dir.resolve("vignette0492-passed.txt"),"Native fitted toy assemblies, lamps, chess/dart states, original reaching bar cast and painted herd with emissive eyes rendered.\n");mc.setScreen(new LakeHouseVisualProof());}
}
