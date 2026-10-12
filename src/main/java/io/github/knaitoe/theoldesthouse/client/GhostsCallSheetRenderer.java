package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.LecternBlockEntity;

/** A single sheet lies on the native lectern's actual sloping top; its original block entity and book remain. */
public final class GhostsCallSheetRenderer {
    private static final ResourceLocation PAPER=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/block/ghosts/call_sheet.png");
    private GhostsCallSheetRenderer(){}
    public static boolean source(LecternBlockEntity lectern){var mc=Minecraft.getInstance();if(mc.level==null||!mc.level.dimension().equals(HouseDimensions.INTERIOR)||!HouseSightlineState.hasOrigin())return false;var b=LabyrinthPlaces.base(HouseSightlineState.origin(),LabyrinthPlace.GHOSTS_SET);return b!=null&&lectern.getBlockPos().equals(b.offset(LiteraryRooms.source(LabyrinthPlace.GHOSTS_SET)));}
    public static void render(PoseStack stack,MultiBufferSource buffers,Direction facing,int light,int overlay){
        stack.pushPose();stack.translate(.5,.5,.5);stack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()-180));stack.mulPose(Axis.XP.rotationDegrees(-22.5F));stack.translate(0,.503,.09375);
        var vertex=buffers.getBuffer(RenderType.entityCutoutNoCull(PAPER));var pose=stack.last();
        vertex.addVertex(pose,-.37F,0,-.33F).setColor(-1).setUv(1,1).setOverlay(overlay).setLight(light).setNormal(pose,0,1,0);
        vertex.addVertex(pose,-.37F,0,.33F).setColor(-1).setUv(1,0).setOverlay(overlay).setLight(light).setNormal(pose,0,1,0);
        vertex.addVertex(pose,.37F,0,.33F).setColor(-1).setUv(0,0).setOverlay(overlay).setLight(light).setNormal(pose,0,1,0);
        vertex.addVertex(pose,.37F,0,-.33F).setColor(-1).setUv(0,1).setOverlay(overlay).setLight(light).setNormal(pose,0,1,0);
        stack.translate(0,.001,0);stack.mulPose(Axis.XP.rotationDegrees(-90));stack.scale(-.004F,.004F,.004F);var font=Minecraft.getInstance().font;
        String[] lines={"CALL SHEET","THE POSSESSION", "DAY "+Math.max(1,GhostsTakeClient.day()),"CAMERA / SOUND","BARRETT HOUSE","INTERIORS"};int[] ys={-65,-53,-37,-15,8,31};
        for(int i=0;i<lines.length;i++)font.drawInBatch(lines[i],-font.width(lines[i])/2F,ys[i],0xff3c392e,false,stack.last().pose(),buffers,Font.DisplayMode.POLYGON_OFFSET,0,light);
        stack.popPose();
    }
}
