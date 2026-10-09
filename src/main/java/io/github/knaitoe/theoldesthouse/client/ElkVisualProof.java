package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import org.joml.Vector3f;

/**
 * The elk carcasses' cast (0.4.50), rendered with the shipped meshes and atlases: the killer
 * watching, searching, running, crouched at the pile and mid-strike; the carcasses in their four
 * falls; and the guests and the road crew in their four. Every lying body and carcass is checked
 * to rest on its floor rather than sink through it or float above it.
 */
@EventBusSubscriber(modid = TheOldestHouse.MOD_ID, value = Dist.CLIENT)
public final class ElkVisualProof extends Screen {
    private int frames;

    public ElkVisualProof() {
        super(Component.literal("The elk carcasses - cast"));
    }

    @Override
    public void renderBackground(GuiGraphics g, int x, int y, float partial) {}

    private static float lowest(PoseStack p, float[][] corners) {
        var m = p.last().pose();
        float low = Float.MAX_VALUE;
        for (float[] c : corners) low = Math.min(low, m.transformPosition(new Vector3f(c[0] / 16F, c[1] / 16F, c[2] / 16F)).y);
        return low;
    }

    /** Torso and legs of the humanoid, and the elk's barrel, in model pixels. */
    private static final float[][] HUMAN = corners(-4, -8, -2, 4, 24, 2), ELK = corners(-5, 5, -10, 5, 17, 13);

    private static float[][] corners(float x0, float y0, float z0, float x1, float y1, float z1) {
        float[][] out = new float[8][];
        int i = 0;
        for (float x : new float[]{x0, x1}) for (float y : new float[]{y0, y1}) for (float z : new float[]{z0, z1}) out[i++] = new float[]{x, y, z};
        return out;
    }

    private static void bounds() {
        for (var facing : Direction.Plane.HORIZONTAL) {
            for (int stage : new int[]{0, 1, 3}) {
                var p = new PoseStack();
                LiteraryRenderers.ModelRenderer.corpsePose(p, facing, stage);
                float low = lowest(p, HUMAN);
                if (low < -.07F || low > .2F) throw new IllegalStateException("A lying body does not rest on its floor: stage " + stage + " " + facing + " " + low);
            }
            for (int stage = 0; stage < 4; stage++) {
                var p = new PoseStack();
                LiteraryRenderers.ModelRenderer.carcassPose(p, facing, 0, stage);
                float low = lowest(p, ELK);
                if (low < -.07F || low > .15F) throw new IllegalStateException("A carcass does not rest on its floor: stage " + stage + " " + facing + " " + low);
            }
        }
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, "textures/entity/literary_" + name + ".png");
    }

    @Override
    public void render(GuiGraphics g, int x, int y, float partial) {
        bounds();
        var mc = Minecraft.getInstance();
        for (String name : new String[]{"killer", "elk_carcass", "elk_carcass_skinned", "guest_0", "guest_1", "guest_2", "guest_3", "crew_0", "crew_1", "crew_2", "crew_3"})
            if (mc.getResourceManager().getResource(texture(name)).isEmpty()) throw new IllegalStateException("Missing elk atlas " + name);
        g.fill(0, 0, width, height, 0xFF1F1C19);
        g.drawString(font, "The elk carcasses / the killer: watching, searching, running, at the pile, striking", 10, 8, 0xFFEEE0C4, false);
        g.drawString(font, "The carcasses and the dead, as the cave and the yacht hold them", 10, height / 2 + 4, 0xFFEEE0C4, false);
        g.flush();
        var buffers = mc.renderBuffers().bufferSource();
        var model = new LiteraryRenderers.CastModel(mc.getEntityModels().bakeLayer(LiteraryRenderers.CAST));
        float[][] states = {{ElkHunt.WATCH, 0, 0, 0}, {ElkHunt.WALK, frames * .5F, .8F, 0}, {ElkHunt.RUN, frames * .9F, 1, 0}, {ElkHunt.PEER, 0, 0, 0}, {ElkHunt.RUN, 0, 0, .15F}}; // the strike at its wind-up, axe over the head
        for (int i = 0; i < states.length; i++) {
            float[] s = states[i];
            model.animateKiller((int) s[0], s[1], s[2], frames, s[3]);
            var p = g.pose();
            p.pushPose();
            p.translate(45 + i * (width - 70) / 5F, 52, 180);
            p.scale(36, 36, 36);
            p.mulPose(Axis.YP.rotationDegrees(i == 3 ? 120 : 150));
            model.renderToBuffer(p, buffers.getBuffer(RenderType.entityCutoutNoCull(texture("killer"))), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
            buffers.endBatch();
            p.popPose();
        }
        var elk = new LiteraryRenderers.ElkModel(mc.getEntityModels().bakeLayer(LiteraryRenderers.ELK));
        var body = new LiteraryRenderers.CastModel(mc.getEntityModels().bakeLayer(LiteraryRenderers.CAST));
        var floor = LiteraryRegistry.DRAG_MUD.get().defaultBlockState();
        for (int i = 0; i < 12; i++) {
            var p = g.pose();
            p.pushPose();
            p.translate(40 + (i % 6) * (width - 60) / 6F, height / 2F + 70 + (i / 6) * 80, 150);
            p.scale(24, -24, 24);
            p.mulPose(Axis.XP.rotationDegrees(28));
            p.mulPose(Axis.YP.rotationDegrees(-35));
            p.translate(-.5, 0, -.5);
            p.pushPose();
            p.translate(0, -1, 0);
            mc.getBlockRenderer().renderSingleBlock(floor, p, buffers, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
            p.popPose();
            var prop = LiteraryRegistry.PROP.get().defaultBlockState().setValue(LiteraryPropBlock.FACING, Direction.EAST);
            if (i < 4) LiteraryRenderers.ModelRenderer.carcass(elk, prop.setValue(LiteraryPropBlock.KIND, LiteraryPropBlock.Kind.CARCASS).setValue(LiteraryPropBlock.STAGE, i), 7L * i, p, buffers, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
            else {
                boolean crew = i >= 8;
                int stage = i % 4;
                LiteraryRenderers.ModelRenderer.corpse(body, prop.setValue(LiteraryPropBlock.KIND, crew ? LiteraryPropBlock.Kind.CREW_BODY : LiteraryPropBlock.Kind.GUEST_BODY).setValue(LiteraryPropBlock.STAGE, stage), (long) i << 7, crew, p, buffers, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
            }
            buffers.endBatch();
            p.popPose();
        }
        // A block entity renderer never resets the model's age; a body left young draws at a child's size.
        g.drawString(font,"Yacht glass: intact / shattered frames",10,height/2-51,0xFFEEE0C4,false);
        int glass=0;for(var block:new YachtGlazingBlock[]{LiteraryRegistry.YACHT_WINDOW.get(),LiteraryRegistry.YACHT_PORTHOLE.get()})for(boolean broken:new boolean[]{false,true}){
            var state=block.defaultBlockState().setValue(YachtGlazingBlock.BROKEN,broken);
            if(mc.getBlockRenderer().getBlockModel(state)==mc.getModelManager().getMissingModel())throw new IllegalStateException("Missing yacht glazing state "+state);
            var pose=g.pose();pose.pushPose();pose.translate(310+(glass++)*60,height/2-13,180);pose.scale(28,-28,28);pose.mulPose(Axis.YP.rotationDegrees(-18));
            mc.getBlockRenderer().renderSingleBlock(state,pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);buffers.endBatch();pose.popPose();
        }
        if (body.young) throw new IllegalStateException("A body renders at a child's size");
    }

    @SubscribeEvent
    public static void frame(RenderFrameEvent.Post e) throws Exception {
        var mc = Minecraft.getInstance();
        if (!Boolean.getBoolean("the_oldest_house.fontSmoke") || !(mc.screen instanceof ElkVisualProof proof) || ++proof.frames < 24) return;
        var dir = Path.of("../build/font-smoke");
        Files.createDirectories(dir);
        try (NativeImage image = Screenshot.takeScreenshot(mc.getMainRenderTarget())) {
            image.writeToFile(dir.resolve("native-elk.png"));
        }
        Files.writeString(dir.resolve("elk-passed.txt"), "24 native frames: the retextured killer in five motions, four carcasses and eight bodies; every lying body and carcass rests on its floor.\n");
        TheOldestHouse.LOGGER.info("ELK CAST CHECK PASSED: killer motions, carcasses and bodies rendered and grounded");
        mc.setScreen(new GoatmanVisualProof());
    }
}
