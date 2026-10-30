package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.journal.PinnedPage;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

/** A torn page pinned to a wall or lying on a table: the paper, its pin, and its first few lines in tiny graphite. */
public final class PinnedPageRenderer extends EntityRenderer<PinnedPage> {
    public static final ResourceLocation PAPER=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/pinned_page.png");
    /** Tiny lines: about twenty-four letters across the page. */
    static final float TEXT=.0032F;
    public PinnedPageRenderer(EntityRendererProvider.Context c){super(c);}
    @Override public ResourceLocation getTextureLocation(PinnedPage page){return PAPER;}
    @Override public void render(PinnedPage page,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){
        pose.pushPose();
        float w=PinnedPage.WIDTH,h=PinnedPage.HEIGHT;
        if(page.face()==Direction.UP){pose.translate(0,.012,0);pose.mulPose(Axis.YP.rotationDegrees(180-page.turn()));pose.mulPose(Axis.XP.rotationDegrees(-90));pose.translate(0,-h/2,0);}
        else{pose.mulPose(Axis.YP.rotationDegrees(-page.face().toYRot()));pose.translate(0,0,PinnedPage.THICK/2-.03);}
        draw(pose,buffers,getFont(),page.glance(),light,w,h);
        pose.popPose();
        super.render(page,yaw,partial,pose,buffers,light);
    }
    /** The paper facing +z, its foot at y 0, centred on x; then the lines on it, top down. */
    static void draw(PoseStack pose,MultiBufferSource buffers,Font font,String glance,int light,float w,float h){
        var out=buffers.getBuffer(RenderType.entityCutoutNoCull(PAPER));var m=pose.last();
        out.addVertex(m,-w/2,0,0).setColor(-1).setUv(0,1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(m,0,0,1);
        out.addVertex(m,w/2,0,0).setColor(-1).setUv(1,1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(m,0,0,1);
        out.addVertex(m,w/2,h,0).setColor(-1).setUv(1,0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(m,0,0,1);
        out.addVertex(m,-w/2,h,0).setColor(-1).setUv(0,0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(m,0,0,1);
        if(glance.isEmpty())return;
        pose.pushPose();pose.translate(-w/2+.07,h-.13,.002);pose.scale(TEXT,-TEXT,TEXT);
        String[] lines=glance.split("\n");
        for(int i=0;i<lines.length;i++)font.drawInBatch(lines[i],0,i*10,JournalPageRenderer.INK,false,pose.last().pose(),buffers,Font.DisplayMode.NORMAL,0,light);
        pose.popPose();
    }
}
