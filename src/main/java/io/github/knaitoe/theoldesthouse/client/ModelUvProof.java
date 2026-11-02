package io.github.knaitoe.theoldesthouse.client;

import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/**
 * 0.4.240: models whose faces reached past their block and took their pixels from beside their texture (ModelUvRangeMixin), drawn
 * as the game draws them: the dust hall's chair and covered furniture (one piece, and a tall piece's two blocks), the plain's note
 * stand, a dining chair and the trailer's chair. Run the client with -PuvSmoke; it saves build/font-smoke/native-model-uvs.png.
 * 0.4.241: a second page, native-devils-rock-props.png, draws Devil's Rock's props redrawn as pixel art and the Sandersons'
 * household (tools/draw_devils_rock_props.py).
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class ModelUvProof extends Screen {
    private int frames,page;
    static final String[] PAGES={"native-model-uvs.png","native-devils-rock-props.png"};
    public ModelUvProof(){super(Component.literal("Model faces on their textures"));}
    private static BlockState dr(VignetteDetailBlock.Kind k){return VignetteDetailBlock.state(k,Direction.NORTH);}
    private static List<BlockState[]> devilsRock(){
        var all=new java.util.ArrayList<BlockState[]>();
        for(var k:List.of(VignetteDetailBlock.Kind.BEAR,VignetteDetailBlock.Kind.MISSING_NOTICE,VignetteDetailBlock.Kind.CASSETTE,VignetteDetailBlock.Kind.SANDERSON_COATS,
                VignetteDetailBlock.Kind.SANDERSON_MAIL,VignetteDetailBlock.Kind.SANDERSON_CALENDAR,VignetteDetailBlock.Kind.SANDERSON_CASSEROLES,VignetteDetailBlock.Kind.SANDERSON_SEARCH_BOARD,
                VignetteDetailBlock.Kind.SANDERSON_COFFEE_TABLE,VignetteDetailBlock.Kind.SANDERSON_LAUNDRY,VignetteDetailBlock.Kind.SANDERSON_PLANT,VignetteDetailBlock.Kind.SANDERSON_SKATEBOARD,
                VignetteDetailBlock.Kind.SANDERSON_CLOTHES,VignetteDetailBlock.Kind.SANDERSON_POSTER,VignetteDetailBlock.Kind.SANDERSON_PAINTINGS,VignetteDetailBlock.Kind.SANDERSON_PAINT_JARS,
                VignetteDetailBlock.Kind.SANDERSON_PHOTO,VignetteDetailBlock.Kind.TOMMY_PHOTO,VignetteDetailBlock.Kind.SIBLINGS_PHOTO))all.add(new BlockState[]{dr(k)});
        all.add(new BlockState[]{dr(VignetteDetailBlock.Kind.SANDERSON_FRIDGE_LOW),dr(VignetteDetailBlock.Kind.SANDERSON_FRIDGE_HIGH)});
        return all;
    }
    private List<BlockState[]> columns(){
        if(page==1)return devilsRock();
        return List.of(new BlockState[]{TraceBlock.of(TraceBlock.Kind.CHAIR,Direction.EAST)},new BlockState[]{TraceBlock.of(TraceBlock.Kind.SHEET,Direction.NORTH,0)},
            new BlockState[]{TraceBlock.of(TraceBlock.Kind.SHEET,Direction.NORTH,1),TraceBlock.of(TraceBlock.Kind.SHEET,Direction.NORTH,2)},
            new BlockState[]{VignetteDetailBlock.state(VignetteDetailBlock.Kind.PLAIN_NOTES,Direction.SOUTH)},
            new BlockState[]{HouseBlocks.TRACT_HOUSE.get().defaultBlockState().setValue(TractHouseBlock.KIND,TractHouseBlock.Kind.DINING_CHAIR)},
            new BlockState[]{GoatmanRegistry.CHAIR.get().defaultBlockState()});
    }
    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xff2a2724);g.drawString(font,page==1?"Devil's Rock: the props and the Sandersons' household":"Model faces on their own textures",10,8,0xffddd5c8,false);
        var cols=columns();int rows=page==1?2:1,per=(cols.size()+rows-1)/rows;float cell=(width-40F)/per;
        for(int i=0;i<cols.size();i++){
            int col=i%per,row=i/per;float rowY=rows==1?height*.62F:height*(.42F+.42F*row);
            var stack=cols.get(i);var pose=g.pose();pose.pushPose();float zoom=page==1?.72F:.42F;pose.translate(20+cell*(col+.5F),rowY,200);pose.scale(cell*zoom,-cell*zoom,cell*zoom);
            pose.mulPose(Axis.XP.rotationDegrees(24));pose.mulPose(Axis.YP.rotationDegrees(215));pose.translate(-.5,0,-.5);
            for(int y=0;y<stack.length;y++){pose.pushPose();pose.translate(0,y,0);mc.getBlockRenderer().renderSingleBlock(stack[y],pose,mc.renderBuffers().bufferSource(),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();}
            pose.popPose();
        }
        mc.renderBuffers().bufferSource().endBatch();
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();if(!Boolean.getBoolean("the_oldest_house.uvSmoke"))return;
        if(mc.screen instanceof TitleScreen&&mc.getOverlay()==null){mc.setScreen(new ModelUvProof());return;}
        if(!(mc.screen instanceof ModelUvProof s)||++s.frames<20)return;
        var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);
        try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve(PAGES[s.page]));}
        if(++s.page<PAGES.length){s.frames=0;return;}
        mc.stop();
    }
}
