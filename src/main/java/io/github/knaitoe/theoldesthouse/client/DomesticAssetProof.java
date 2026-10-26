package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/** Production baked models, including the reported open mailbox and pictured projector. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class DomesticAssetProof extends Screen {
    private int page,frames;
    public DomesticAssetProof(){super(Component.literal("Native domestic objects"));}
    private record View(String label,BlockState state){}
    private List<View> views(){
        var out=new ArrayList<View>();
        if(page<2){
            var a=page==0?DomesticFittingBlock.Kind.BREAKFAST:DomesticFittingBlock.Kind.RECORD_PLAYER;
            var b=page==0?DomesticFittingBlock.Kind.BOTTLES:DomesticFittingBlock.Kind.RADIO;
            for(int n=0;n<8;n++)out.add(new View(a.getSerializedName()+" "+n,DomesticFittingBlock.of(a,Direction.NORTH).setValue(DomesticFittingBlock.STAGE,n)));
            for(int n=0;n<8;n++)out.add(new View(b.getSerializedName()+" "+(page==0?8+n:n),DomesticFittingBlock.of(b,Direction.NORTH).setValue(DomesticFittingBlock.STAGE,page==0?8+n:n)));
        }else if(page==2){
            for(var k:List.of(DomesticFittingBlock.Kind.TOPPLED_CHAIR,DomesticFittingBlock.Kind.TOPPLED_TABLE,DomesticFittingBlock.Kind.SCRAPES))out.add(new View(k.getSerializedName(),DomesticFittingBlock.of(k,Direction.NORTH)));
            for(String m:List.of("family_paper","student_paper","smoked_paper","rose_paper","parquet","linoleum"))out.add(new View(m,DomesticRegistry.material(m).defaultBlockState()));
        }else if(page==3){
            out.add(new View("desk projector",NovelRegistry.PROP.get().defaultBlockState().setValue(NovelPropBlock.KIND,NovelPropBlock.Kind.PROJECTOR)));
            for(int n=0;n<4;n++)out.add(new View("scene camera "+n,LiteraryRegistry.PROP.get().defaultBlockState().setValue(LiteraryPropBlock.KIND,LiteraryPropBlock.Kind.CAMERA).setValue(LiteraryPropBlock.STAGE,n)));
            for(var f:Direction.Plane.HORIZONTAL)for(boolean open:new boolean[]{false,true})out.add(new View("mailbox "+f+" "+(open?"open":"closed"),UncannyFurnitureBlock.of(UncannyFurnitureBlock.Kind.MAILBOX,f).setValue(UncannyFurnitureBlock.OPEN,open)));
        }
        if(page==4){
            for(var k:List.of(DomesticFittingBlock.Kind.PACKED_CLOTHES,DomesticFittingBlock.Kind.BOOK_BOX,DomesticFittingBlock.Kind.CLOSET_LOWER,DomesticFittingBlock.Kind.CLOSET_UPPER,DomesticFittingBlock.Kind.DESK_REMNANTS,DomesticFittingBlock.Kind.PHOTO_FAMILY,DomesticFittingBlock.Kind.PHOTO_COUPLE,DomesticFittingBlock.Kind.BOTTLE_RETURNS))for(int n=0;n<2;n++)out.add(new View(k.getSerializedName()+" "+n,DomesticFittingBlock.of(k,Direction.NORTH).setValue(DomesticFittingBlock.STAGE,n)));
            for(var k:List.of(DomesticFittingBlock.Kind.EMPTY_BOTTLES,DomesticFittingBlock.Kind.DIRTY_DISHES,DomesticFittingBlock.Kind.FOLDED_SHIRT))out.add(new View(k.getSerializedName(),DomesticFittingBlock.of(k,Direction.NORTH)));
        }
        return out;
    }
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xffb1b3a2);g.drawString(font,"Native household states / empty homes / matching collision cuboids",12,10,0xff252e2b,false);g.flush();
        var views=views();int columns=4,rows=(views.size()+columns-1)/columns;float cellW=width/(float)columns,cellH=(height-50F)/rows,scale=Math.min(cellW*.65F,cellH*.63F);
        var buffers=mc.renderBuffers().bufferSource();
        for(int i=0;i<views.size();i++){
            var v=views.get(i);if(mc.getBlockRenderer().getBlockModel(v.state)==mc.getModelManager().getMissingModel())throw new IllegalStateException("Missing production model "+v.label);
            var p=g.pose();p.pushPose();p.translate((i%columns+.5)*cellW,35+(i/columns+.7)*cellH,150);p.scale(scale,-scale,scale);p.mulPose(Axis.XP.rotationDegrees(24));p.mulPose(Axis.YP.rotationDegrees(25));p.translate(-.5,-.3,-.5);
            mc.getBlockRenderer().renderSingleBlock(v.state,p,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);buffers.endBatch();p.popPose();
            g.drawCenteredString(font,v.label,(int)((i%columns+.5)*cellW),(int)(35+(i/columns+1)*cellH),0xff25302a);
        }
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();if(!(mc.screen instanceof DomesticAssetProof s)||++s.frames<12)return;
        var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);try(NativeImage im=Screenshot.takeScreenshot(mc.getMainRenderTarget())){im.writeToFile(dir.resolve("native-domestic-"+s.page+".png"));}
        if(++s.page<5){s.frames=0;return;}
        Files.writeString(dir.resolve("domestic-assets-passed.txt"),"Five native baked-model views: independent cups and bottles, record lid/arm/side, tuner/power, inert disturbances, household materials, inspectable boxes/closets/photographs, actual projector/camera and eight open/closed mailbox facings. Full bright model review, not a lighting playthrough.\n");mc.stop();
    }
}
