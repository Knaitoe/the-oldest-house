package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.time.LocalTime;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Moving hands on a wall clock's painted face (0.4.185, owner: "would be cool if you could animate clock arms"). The face is
 * the block's own model; the hour, minute and second hands are drawn over it, the second hand ticking once a second and the
 * others creeping, in pixel widths that match the face. Each clock says its own time: the kitchen's a little after four from
 * the moment it is first seen, the waiting room's office clock the hour it really is, a few minutes slow.
 */
public final class ClockHands {
    private ClockHands(){}
    private static final ResourceLocation WHITE=ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    /** The hands' ink and the second hand's red. */
    static final int INK=0x24211e,RED=0x9b2b22;
    /** When each kitchen clock was first seen, so it starts at a little after four for whoever is in the room. */
    private static final Map<BlockEntity,Long> FIRST=new WeakHashMap<>();

    /** The time a clock shows, in seconds past midnight. */
    public enum Time {
        /** "The kitchen clock says a little after four": 4:05 when first seen, then running. */
        AFTER_FOUR,
        /** "A plain office clock, a few minutes slow": the real hour, four minutes behind. */
        OFFICE_SLOW;
        double seconds(BlockEntity clock){
            long now=System.currentTimeMillis();
            return switch(this){
                case AFTER_FOUR->{long first=FIRST.computeIfAbsent(clock,c->now);yield 4*3600+5*60+(now-first)/1000.0;}
                case OFFICE_SLOW->{var t=LocalTime.now();yield t.toSecondOfDay()-4*60+(now%1000)/1000.0;}
            };
        }
    }

    /** The quiet rooms' wall clock: a twelve-pixel face on a plate against the north of the block, facing out of the wall. */
    public static void wall(io.github.knaitoe.theoldesthouse.labyrinth.HouseReadPropBlock.Fitting clock,PoseStack pose,MultiBufferSource out,int light){
        var time=io.github.knaitoe.theoldesthouse.labyrinth.HouseReadKitchen.CLOCK_AFTER_FOUR.equals(clock.read().getString("Clock"))?Time.AFTER_FOUR:Time.OFFICE_SLOW;
        draw(clock,clock.getBlockState().getValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING),time,.5f,.5f,1.5f/16,pose,out,light);
    }

    /**
     * Draws the hands over a face that, facing south, lies in the plane {@code faceZ} (block units) with its centre at
     * ({@code cx},{@code cy}); {@code facing} turns it as the block's model is turned.
     */
    public static void draw(BlockEntity clock,Direction facing,Time time,float cx,float cy,float faceZ,PoseStack pose,MultiBufferSource out,int light){
        double s=time.seconds(clock);s=((s%43200)+43200)%43200;
        double second=Math.floor(s%60),minute=(s/60)%60,hour=(s/3600)%12;
        var vc=out.getBuffer(RenderType.entityCutoutNoCull(WHITE));
        pose.pushPose();
        pose.translate(.5,0,.5);pose.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));pose.translate(-.5,0,-.5);
        pose.translate(cx,cy,faceZ);
        // Pixel widths on a twelve-pixel face: a short broad hour hand, a long minute hand, a fine second hand; each a hair
        // in front of the last so none flickers through another.
        hand(vc,pose,hour*30,1.1f,3.2f,.6f,.0015f,INK,light);
        hand(vc,pose,minute*6,.8f,4.6f,.6f,.003f,INK,light);
        hand(vc,pose,second*6,.35f,4.9f,1.2f,.0045f,RED,light);
        hand(vc,pose,0,.9f,.45f,.45f,.006f,INK,light);
        pose.popPose();
    }

    /** One hand, turned clockwise from twelve by {@code degrees}: {@code width} and {@code length} in sixteenths, with a short tail. */
    private static void hand(VertexConsumer vc,PoseStack pose,double degrees,float width,float length,float tail,float lift,int rgb,int light){
        pose.pushPose();pose.translate(0,0,lift);pose.mulPose(Axis.ZP.rotationDegrees((float)-degrees));
        float w=width/32f,l=length/16f,t=-tail/16f;var m=pose.last();
        float r=(rgb>>16&255)/255f,g=(rgb>>8&255)/255f,b=(rgb&255)/255f;
        vc.addVertex(m,-w,t,0).setColor(r,g,b,1f).setUv(0,1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(m,0,0,1);
        vc.addVertex(m,w,t,0).setColor(r,g,b,1f).setUv(1,1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(m,0,0,1);
        vc.addVertex(m,w,l,0).setColor(r,g,b,1f).setUv(1,0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(m,0,0,1);
        vc.addVertex(m,-w,l,0).setColor(r,g,b,1f).setUv(0,0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(m,0,0,1);
        pose.popPose();
    }
}
