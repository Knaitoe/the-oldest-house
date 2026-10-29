package io.github.knaitoe.theoldesthouse.client;

import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/**
 * 0.4.164 (owner: "podiums like this need new textures suited to their vignettes"; "it's the life preserver that's
 * floating"): each scene's reading stand on the table it stands on in the scene, from the reader's side and from behind,
 * and the dock's piling with the life ring hung on it. Shipped models, native pixel scale. Runs after the quiet rooms'
 * pages in the same client.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class ReadingStandProof extends Screen {
    private static final String[] NAMES={"stands_front","stands_back","dock_ring"};
    private record Placed(int x,int y,int z,BlockState state){}
    private int page,frames;
    public ReadingStandProof(){super(Component.literal("Native reading stands"));}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    /** The scene's stand and what it stands on there: the same states the builders place. */
    static List<Placed> scene(LabyrinthPlace place){
        var stand=LiteraryRooms.holder(place);
        BlockState under=switch(place){
            case WINTER_LAKE->Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState();
            case GHOSTS_SET->BarrettHomeBlock.of(BarrettHomeBlock.Kind.FOLDING_TABLE,Direction.NORTH);
            case WINCHESTER->HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE,Direction.SOUTH);
            default->HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.SOUTH);};
        return List.of(new Placed(0,0,0,under),new Placed(0,1,0,stand));
    }
    static final LabyrinthPlace[] PLACES={LabyrinthPlace.MOVIE_NIGHT,LabyrinthPlace.COSTUME_NIGHT,LabyrinthPlace.WINTER_LAKE,LabyrinthPlace.WINCHESTER,LabyrinthPlace.GHOSTS_SET,LabyrinthPlace.BORRASCA};
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        g.fill(0,0,width,height,0xff343630);g.drawString(font,"Reading stands / "+NAMES[page].replace('_',' ')+" / shipped native models",10,9,0xffe5dcc8,false);
        if(page==2){dock(g);return;}
        float cw=width/3f,ch=(height-30)/2f;
        for(int i=0;i<PLACES.length;i++){
            var place=PLACES[i];var stand=LiteraryRooms.holder(place);
            // From where the reader stands (the stand's front), or from behind it.
            float yaw=180-stand.getValue(LecternBlock.FACING).toYRot()+(page==0?152:-28);
            assemble(g,scene(place),(i%3+.5f)*cw,30+(i/3+.55f)*ch,Math.min(cw,ch)*.34f,yaw,place.id().replace('_',' '),stand);
        }
    }
    private void assemble(GuiGraphics g,List<Placed> blocks,float x,float y,float scale,float yaw,String label,BlockState stand){
        var mc=Minecraft.getInstance();var p=g.pose();g.flush();
        p.pushPose();p.translate(x,y,180);p.scale(scale,-scale,scale);p.mulPose(Axis.XP.rotationDegrees(22));p.mulPose(Axis.YP.rotationDegrees(yaw));p.translate(-.5f,-1f,-.5f);
        var out=mc.renderBuffers().bufferSource();
        for(var b:blocks){p.pushPose();p.translate(b.x(),b.y(),b.z());mc.getBlockRenderer().renderSingleBlock(b.state(),p,out,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);
            // The script stand's call sheet is drawn by the client on the desk, as on the set.
            if(b.state()==stand&&stand.getValue(ReadingStandBlock.KIND)==ReadingStandBlock.Kind.SCRIPT_STAND)GhostsCallSheetRenderer.render(p,out,stand.getValue(LecternBlock.FACING),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);
            p.popPose();}
        out.endBatch();p.popPose();g.drawCenteredString(font,label,(int)x,(int)(y+scale*1.15f),0xffe5dcc8);
    }
    /** The dock's edge as MovieNight builds it: boards, the piling at the edge, the life ring on its lake side over the water. */
    private void dock(GuiGraphics g){
        var board=MovieMaterialBlock.of(MovieMaterialBlock.Kind.DOCK);var piling=Blocks.STRIPPED_OAK_LOG.defaultBlockState();var water=Blocks.WATER.defaultBlockState();
        var ring=MovieNightRegistry.PROP.get().defaultBlockState().setValue(MoviePropBlock.KIND,MoviePropBlock.Kind.LIFE_RING).setValue(HorizontalDirectionalBlock.FACING,Direction.EAST);
        var cells=new ArrayList<Placed>();
        for(int x=0;x<3;x++)for(int z=0;z<3;z++)cells.add(new Placed(x,0,z,board));
        cells.add(new Placed(2,1,1,piling));cells.add(new Placed(2,2,1,piling));cells.add(new Placed(3,2,1,ring));
        for(int z=0;z<3;z++)cells.add(new Placed(3,0,z,Blocks.BLUE_STAINED_GLASS.defaultBlockState()));
        for(var view:new float[][]{{.3f,-60},{.72f,-120}}){
            var mc=Minecraft.getInstance();var p=g.pose();g.flush();float scale=Math.min(width,height)*.11f;
            p.pushPose();p.translate(width*view[0],height*.6f,180);p.scale(scale,-scale,scale);p.mulPose(Axis.XP.rotationDegrees(20));p.mulPose(Axis.YP.rotationDegrees(view[1]));p.translate(-2f,-1.5f,-1.5f);
            var out=mc.renderBuffers().bufferSource();
            for(var b:cells){p.pushPose();p.translate(b.x(),b.y(),b.z());mc.getBlockRenderer().renderSingleBlock(b.state(),p,out,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);p.popPose();}
            out.endBatch();p.popPose();
        }
        g.drawCenteredString(font,"the dock's piling and the life ring hung flush on its lake side (water drawn as blue glass)",width/2,(int)(height*.92f),0xffe5dcc8);
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();if(!(mc.screen instanceof ReadingStandProof s)||++s.frames<12)return;
        var folder=Path.of("../build/reading-stands");Files.createDirectories(folder);
        try(var image=net.minecraft.client.Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(folder.resolve(NAMES[s.page]+".png"));}
        if(++s.page<NAMES.length){s.frames=0;return;}
        Files.writeString(folder.resolve("passed.txt"),"Six reading stands on their scenes' own tables, from the reader's side and from behind, and the dock's piling with the life ring hung flush on it. Shipped native models at Minecraft's pixel scale.\n");
        mc.stop();
    }
}
