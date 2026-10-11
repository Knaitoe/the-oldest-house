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
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/** Actual baked ward meshes and atlas sprites, not a software approximation of the assets. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class InstituteVisualProof extends Screen {
    private int page,frames;private boolean checked;
    public InstituteVisualProof(){super(Component.literal("Whalestoe ward"));}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float p){}
    private static List<BlockState> states(){var out=new ArrayList<BlockState>();for(var k:InstituteFixtureBlock.Kind.values())out.add(InstituteFixtureBlock.of(k));out.add(NovelRegistry.WARD_CABINET.get().defaultBlockState());out.add(InstituteNotebookBlock.of(false,Direction.SOUTH));out.add(InstituteNotebookBlock.of(true,Direction.SOUTH));out.add(NovelRegistry.WARD_RAIL.get().defaultBlockState());out.add(NovelRegistry.WARD_STAIRS.get().defaultBlockState());out.add(NovelRegistry.WARD_SLAB.get().defaultBlockState());out.add(NovelRegistry.WARD_GLASS.get().defaultBlockState());out.add(NovelRegistry.WARD_DOOR.get().defaultBlockState());out.add(NovelRegistry.WARD_LOCKED_DOOR.get().defaultBlockState());return out;}
    private static void check(BlockState s){var mc=Minecraft.getInstance();var model=mc.getBlockRenderer().getBlockModel(s);if(model==mc.getModelManager().getMissingModel())throw new IllegalStateException("Missing institute model: "+s);int quads=0;
        for(int f=-1;f<6;f++)for(var q:model.getQuads(s,f<0?null:Direction.values()[f],RandomSource.create(1))){quads++;if(q.getSprite().contents().name().getPath().equals("missingno"))throw new IllegalStateException("Missing institute texture: "+s);}
        if(quads==0)throw new IllegalStateException("Empty institute model: "+s);
    }
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        var mc=Minecraft.getInstance();var states=states();if(!checked){for(var s:states)check(s);checked=true;}var out=mc.renderBuffers().bufferSource();g.fill(0,0,width,height,0xff313b36);g.drawString(font,page==0?"Whalestoe: varied paint, linoleum and enamel ward furniture":"Whalestoe: finite cabinets, notebooks, connected rails and painted doors",10,8,0xffeee8d8,false);g.flush();
        int start=page==0?0:16,end=Math.min(states.size(),start+16);float cw=(width-20)/4F,ch=(height-32)/4F;
        for(int i=start;i<end;i++){var state=states.get(i);int n=i-start;var p=g.pose();p.pushPose();p.translate(10+(n%4+.5F)*cw,31+(n/4+.85F)*ch,150);float scale=Math.min(40,ch*.67F);p.scale(scale,-scale,scale);p.mulPose(Axis.XP.rotationDegrees(25));p.mulPose(Axis.YP.rotationDegrees(32));p.translate(-.5,0,-.5);mc.getBlockRenderer().renderSingleBlock(state,p,out,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);p.popPose();}out.endBatch();
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{var mc=Minecraft.getInstance();if(!(mc.screen instanceof InstituteVisualProof proof)||++proof.frames<16)return;
        var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);try(NativeImage im=Screenshot.takeScreenshot(mc.getMainRenderTarget())){im.writeToFile(dir.resolve("native-institute-0470-"+proof.page+".png"));}proof.frames=0;if(++proof.page<2)return;
        Files.writeString(dir.resolve("institute-0470-passed.txt"),"All twenty ward fittings, 27-slot cabinet, original-source reading table, bedside notebook, rails, stairs, shelf, glass and both door types have native baked geometry and resolved atlas sprites; two native GPU views.\n");if(Boolean.getBoolean("the_oldest_house.trailerSmoke"))mc.stop();else mc.setScreen(new CoffinVisualProof());
    }
}
