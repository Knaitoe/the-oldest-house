package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock;
import io.github.knaitoe.theoldesthouse.labyrinth.HouseReadRegistry;
import io.github.knaitoe.theoldesthouse.labyrinth.SecondPersonBlock;
import io.github.knaitoe.theoldesthouse.labyrinth.SecondPersonRegistry;
import java.nio.file.*;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/**
 * 0.4.370, the Second Person animation review: the resident in every gesture, on the furniture the story puts them on, at the heights
 * the server gives (the armchair and sofa at the floor, the cane chairs 0.1 higher, the bed's edge 0.12, the box as a perch) and with
 * the seated drop the renderer applies, holding what the server puts in their hand. Run with -PsecondPersonPoseSmoke; it saves a front
 * and a side view to build/font-smoke and stops. Staged poses, not a playthrough.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class SecondPersonPoseProof extends Screen {
    private record Cell(String label,int gesture,@Nullable BlockState under,float lift,@Nullable ItemStack held){}
    private static final String[] VIEWS={"front","side"};
    private int page,frames;
    private static boolean launched;
    public SecondPersonPoseProof(){super(Component.literal("Second Person — the resident's poses"));}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float delta){}

    private static ItemStack carried(int model){var s=new ItemStack(SecondPersonRegistry.CARRIED.get());s.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(model));return s;}
    private static List<Cell> cells(){
        var armchair=HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.FLORAL_ARMCHAIR,Direction.SOUTH);
        var cane=HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.SOUTH);
        var sofa=HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.BLUE_SOFA,Direction.SOUTH);
        var bed=Blocks.WHITE_BED.defaultBlockState().setValue(BedBlock.FACING,Direction.SOUTH).setValue(BedBlock.PART,BedPart.FOOT);
        var box=SecondPersonBlock.of(SecondPersonBlock.Kind.BOX,Direction.NORTH,0);
        var phone=new ItemStack(HouseReadRegistry.PHONE.get());
        return List.of(
            new Cell("standing",SecondPersonRenderers.STAND,null,0,null),
            new Cell("armchair",SecondPersonRenderers.SIT,armchair,0,null),
            new Cell("sofa",SecondPersonRenderers.SIT,sofa,0,null),
            new Cell("cane chair",SecondPersonRenderers.SIT,cane,.1F,null),
            new Cell("bed's edge",SecondPersonRenderers.SIT,bed,.12F,null),
            new Cell("on the box",SecondPersonRenderers.PERCH,box,0,null),
            new Cell("knitting",SecondPersonRenderers.KNIT,armchair,0,carried(SecondPersonRegistry.SLEEVE_MODEL)),
            new Cell("landline",SecondPersonRenderers.PHONE,null,0,carried(SecondPersonRegistry.RECEIVER_MODEL)),
            new Cell("camera",SecondPersonRenderers.CAMERA,null,0,phone),
            new Cell("the picture",SecondPersonRenderers.LOOK,null,0,phone),
            new Cell("the last box",SecondPersonRenderers.CARRY,null,0,null),
            new Cell("one bag",SecondPersonRenderers.BAG,null,0,new ItemStack(Items.BUNDLE)),
            new Cell("asleep, walking",SecondPersonRenderers.DRIFT,null,0,null),
            new Cell("the latch",SecondPersonRenderers.REACH,null,0,null));
    }

    @Override public void render(GuiGraphics g,int x,int y,float delta){
        var mc=Minecraft.getInstance();
        g.fill(0,0,width,height,0xff262320);g.drawString(font,"Second Person / the resident / "+VIEWS[page]+" / seats at the server's heights",10,9,0xffeee0c4,false);g.flush();
        var cells=cells();int columns=7,rows=(cells.size()+columns-1)/columns;
        float cellWidth=(width-20F)/columns,cellHeight=(height-40F)/rows,scale=Math.min(cellWidth*.42F,cellHeight*.32F);
        var buffers=mc.renderBuffers().bufferSource();var skin=net.minecraft.client.resources.DefaultPlayerSkin.getDefaultTexture();
        var model=new SecondPersonRenderers.Model(mc.getEntityModels().bakeLayer(ModelLayers.PLAYER),false);int n=0;
        for(var c:cells){
            float cx=10+(n%columns+.5F)*cellWidth,top=29+(n/columns)*cellHeight;
            var pose=g.pose();pose.pushPose();pose.translate(cx,top+scale*.62F,190);pose.scale(scale,scale,scale);
            pose.mulPose(Axis.YP.rotationDegrees(page==0?155:90));
            int light=LightTexture.FULL_BRIGHT;
            // The furniture, standing on the floor (model space: y down, the floor at 1.501, the body facing -z).
            if(c.under()!=null){pose.pushPose();pose.translate(-.5,1.501,.5);pose.scale(1,-1,-1);mc.getBlockRenderer().renderSingleBlock(c.under(),pose,buffers,light,OverlayTexture.NO_OVERLAY);pose.popPose();buffers.endBatch();}
            // The body: lifted by the server's seat height, dropped by the renderer's seated offset.
            boolean low=c.gesture()==SecondPersonRenderers.SIT||c.gesture()==SecondPersonRenderers.KNIT;
            pose.pushPose();pose.translate(0,-c.lift()+(low?.3F:0),0);
            model.preview(c.gesture(),frames*.35F+n,frames);
            model.renderToBuffer(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(skin)),light,OverlayTexture.NO_OVERLAY,0xffffffff);
            if(c.gesture()==SecondPersonRenderers.CARRY)SecondPersonRenderers.Held.box(pose,buffers,light,frames*.35F,1);
            if(c.held()!=null){pose.pushPose();model.translateToHand(HumanoidArm.RIGHT,pose);pose.mulPose(Axis.XP.rotationDegrees(-90));pose.mulPose(Axis.YP.rotationDegrees(180));pose.translate(1/16F,.125F,-.625F);
                mc.getItemRenderer().renderStatic(c.held(),ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,light,OverlayTexture.NO_OVERLAY,pose,buffers,null,0);pose.popPose();}
            buffers.endBatch();pose.popPose();pose.popPose();
            g.drawString(font,c.label(),(int)cx-font.width(c.label())/2,(int)(top+cellHeight-14),0xffeee0c4,false);n++;
        }
    }

    @SubscribeEvent public static void launch(ClientTickEvent.Post e){var mc=Minecraft.getInstance();if(Boolean.getBoolean("the_oldest_house.secondPersonPoseSmoke")&&!launched&&mc.screen!=null&&mc.getOverlay()==null){launched=true;mc.options.guiScale().set(2);mc.resizeDisplay();mc.setScreen(new SecondPersonPoseProof());}}
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();if(!(mc.screen instanceof SecondPersonPoseProof proof)||++proof.frames<24)return;
        var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);
        try(NativeImage image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve("native-second-person-poses-"+VIEWS[proof.page]+".png"));}
        if(++proof.page<VIEWS.length){proof.frames=0;return;}
        TheOldestHouse.LOGGER.info("SECOND PERSON POSE CHECK PASSED: fourteen resident poses, front and side");mc.stop();
    }
}
