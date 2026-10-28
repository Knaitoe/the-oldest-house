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
    private int page,frames;private NativeImage unlitJar;
    public DomesticAssetProof(){super(Component.literal("Native domestic objects"));}
    private record View(String label,BlockState state,BlockState paper){View(String label,BlockState state){this(label,state,null);}}
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
        if(page==5){
            for(var k:List.of(UncannyFurnitureBlock.Kind.WARDROBE,UncannyFurnitureBlock.Kind.FRIDGE,UncannyFurnitureBlock.Kind.SIDEBOARD))for(boolean open:new boolean[]{false,true})out.add(new View(k.getSerializedName()+" "+(open?"open":"closed"),UncannyFurnitureBlock.of(k,Direction.NORTH).setValue(UncannyFurnitureBlock.OPEN,open)));
            for(var f:Direction.Plane.HORIZONTAL)out.add(new View("red chair "+f,UncannyFurnitureBlock.of(UncannyFurnitureBlock.Kind.DINER_CHAIR,f)));
        }
        if(page==6){
            for(var k:List.of(VignetteInteriorBlock.Kind.HOLIDAY_SINK,VignetteInteriorBlock.Kind.DUMPSTER_LEFT,VignetteInteriorBlock.Kind.DUMPSTER_RIGHT))for(boolean active:new boolean[]{false,true})out.add(new View(k.getSerializedName()+" "+active,VignetteInteriorBlock.of(k,Direction.NORTH).setValue(VignetteInteriorBlock.ACTIVE,active)));
            for(var k:List.of(VignetteInteriorBlock.Kind.CABIN_TABLE,VignetteInteriorBlock.Kind.CABIN_CHAIR,VignetteInteriorBlock.Kind.CABIN_STOOL,VignetteInteriorBlock.Kind.CABIN_FERN))out.add(new View(k.getSerializedName(),VignetteInteriorBlock.of(k,Direction.NORTH)));
            for(int n=0;n<3;n++)out.add(new View("glass / independent glow "+n,LiteraryRegistry.PROP.get().defaultBlockState().setValue(LiteraryPropBlock.KIND,LiteraryPropBlock.Kind.JAR).setValue(LiteraryPropBlock.STAGE,n)));
            out.add(new View("cabin journal",LiteraryRegistry.PROP.get().defaultBlockState().setValue(LiteraryPropBlock.KIND,LiteraryPropBlock.Kind.LEDGER).setValue(LiteraryPropBlock.STAGE,3)));
        }
        if(page==7){var kinds=List.of(io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock.Kind.CANE_CHAIR,io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock.Kind.KITCHEN_STOOL,io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock.Kind.FLORAL_ARMCHAIR,io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock.Kind.BLUE_SOFA,io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock.Kind.FOOTSTOOL);for(int n=0;n<6;n++){out.add(new View(kinds.get(n).getSerializedName(),io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock.state(kinds.get(n),Direction.NORTH)));out.add(new View("same, fallen",DomesticFittingBlock.of(DomesticFittingBlock.Kind.TOPPLED_CHAIR,Direction.NORTH).setValue(DomesticFittingBlock.STAGE,n)));}}
        if(page==8)for(var kind:DomesticRecordBlock.Kind.values())for(boolean present:new boolean[]{true,false})out.add(new View(kind.getSerializedName()+(present?" original":" taken"),DomesticRecordBlock.of(kind,Direction.NORTH).setValue(DomesticRecordBlock.PRESENT,present)));
        if(page==9)for(var support:List.of(UncannyFurnitureBlock.of(UncannyFurnitureBlock.Kind.COFFEE_TABLE,Direction.NORTH),UncannyFurnitureBlock.of(UncannyFurnitureBlock.Kind.DESK,Direction.NORTH),DomesticFittingBlock.of(DomesticFittingBlock.Kind.PACKED_CLOTHES,Direction.NORTH))){
            int drop=(int)Math.round((1-support.getShape(net.minecraft.world.level.EmptyBlockGetter.INSTANCE,net.minecraft.core.BlockPos.ZERO).bounds().maxY)*16);
            var paper=DomesticRecordBlock.of(DomesticRecordBlock.Kind.NOTE,Direction.NORTH);
            out.add(new View("old full-cell anchor",support,paper));out.add(new View("same original, on surface",support,paper.setValue(DomesticRecordBlock.DROP,Math.max(0,Math.min(16,drop)))));
        }
        return out;
    }
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        var mc=Minecraft.getInstance();g.fill(0,0,width,height,page==6?0xff252827:0xffb1b3a2);g.drawString(font,"Native household states / empty homes / matching collision cuboids",12,10,page==6?0xffe4d9bf:0xff252e2b,false);g.flush();
        var views=views();int columns=4,rows=(views.size()+columns-1)/columns;float cellW=width/(float)columns,cellH=(height-50F)/rows,scale=Math.min(cellW*.65F,cellH*.63F);
        var buffers=mc.renderBuffers().bufferSource();
        for(int i=0;i<views.size();i++){
            var v=views.get(i);if(mc.getBlockRenderer().getBlockModel(v.state)==mc.getModelManager().getMissingModel())throw new IllegalStateException("Missing production model "+v.label);
            boolean tall=v.state.is(UncannyRegistry.FURNITURE.get())&&(v.state.getValue(UncannyFurnitureBlock.KIND)==UncannyFurnitureBlock.Kind.WARDROBE||v.state.getValue(UncannyFurnitureBlock.KIND)==UncannyFurnitureBlock.Kind.FRIDGE);float size=tall?scale*.65F:scale;
            var p=g.pose();p.pushPose();p.translate((i%columns+.5)*cellW,35+(i/columns+.7)*cellH,150);p.scale(size,-size,size);p.mulPose(Axis.XP.rotationDegrees(24));p.mulPose(Axis.YP.rotationDegrees(page>=5?155:25));p.translate(-.5,tall?-.8:-.3,-.5);
            if(v.state.is(LiteraryRegistry.PROP.get())&&v.state.getValue(LiteraryPropBlock.KIND)==LiteraryPropBlock.Kind.LEDGER)mc.getBlockRenderer().getModelRenderer().renderModel(p.last(),buffers.getBuffer(RenderType.cutout()),v.state,mc.getBlockRenderer().getBlockModel(v.state),1F,1F,1F,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);
            else mc.getBlockRenderer().renderSingleBlock(v.state,p,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);
            if(v.paper!=null){p.pushPose();p.translate(0,1,0);mc.getBlockRenderer().renderSingleBlock(v.paper,p,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);p.popPose();}
            if(page==6&&frames>=6&&v.state.is(LiteraryRegistry.PROP.get())&&v.state.getValue(LiteraryPropBlock.KIND)==LiteraryPropBlock.Kind.JAR)FireflyJar.render(p,buffers,v.state.getValue(LiteraryPropBlock.STAGE)*80,net.minecraft.core.BlockPos.ZERO,OverlayTexture.NO_OVERLAY);
            buffers.endBatch();p.popPose();
            g.drawCenteredString(font,v.label,(int)((i%columns+.5)*cellW),(int)(35+(i/columns+1)*cellH),page==6?0xffe4d9bf:0xff25302a);
        }
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();if(!(mc.screen instanceof DomesticAssetProof s))return;
        if(s.page==6&&++s.frames==6)s.unlitJar=Screenshot.takeScreenshot(mc.getMainRenderTarget());else if(s.page!=6)s.frames++;
        if(s.frames<12)return;
        var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);try(NativeImage im=Screenshot.takeScreenshot(mc.getMainRenderTarget())){
            im.writeToFile(dir.resolve("native-domestic-"+s.page+".png"));
            if(s.page==6){
              if(s.unlitJar==null)throw new IllegalStateException("Missing native unlit jar comparison");s.unlitJar.writeToFile(dir.resolve("native-domestic-6-unlit.png"));
              for(int cell:new int[]{10,11,12}){
                double scale=im.getWidth()/(double)s.width,cellW=s.width/4.,cellH=(s.height-50.)/4;int count=0;
                for(int x=(int)(cell%4*cellW*scale);x<(cell%4+1)*cellW*scale;x++)for(int y=(int)((35+cell/4*cellH)*scale);y<Math.min(im.getHeight(),(35+(cell/4+1)*cellH)*scale);y++){
                    int color=im.getPixelRGBA(x,y),before=s.unlitJar.getPixelRGBA(x,y);int difference=Math.max(Math.abs((color&255)-(before&255)),Math.max(Math.abs((color>>>8&255)-(before>>>8&255)),Math.abs((color>>>16&255)-(before>>>16&255))));if(difference>12)count++;
                }
                if(count==0)throw new IllegalStateException("The native jar glow is invisible at sample "+(cell-10));
              }
              s.unlitJar.close();s.unlitJar=null;
            }
        }
        if(++s.page<10){s.frames=0;return;}
        Files.writeString(dir.resolve("domestic-assets-passed.txt"),"Ten native baked-model views, including six upright/fallen same-material pairs, fourteen original/empty household record mounts and three original/settled paper-support pairs. Full bright model review, not a lighting playthrough.\n");mc.setScreen(new DinerAssetProof());
    }
}
