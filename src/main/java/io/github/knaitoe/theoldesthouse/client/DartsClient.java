package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import io.github.knaitoe.theoldesthouse.network.DartsStatePayload;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

/**
 * 0.4.415 (docs/DARTS_0_4_415.md): darts as everyone near a board sees them. The game sent by the server is chalked on the wall beside
 * the board; thrown darts are drawn as darts, their flights in their thrower's colour; and while a dart is held the view wanders with
 * the same sway the server throws along (DartItem#sway), so the dart goes where the crosshair was.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class DartsClient {
    private DartsClient(){}
    static final Map<BlockPos,CompoundTag> STATES=new HashMap<>();
    public static void state(DartsStatePayload p){STATES.put(p.board().immutable(),p.info());}
    public static CompoundTag state(BlockPos board){return STATES.get(board);}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        var mc=Minecraft.getInstance();
        if(mc.level==null||mc.player==null){STATES.clear();return;}
        STATES.keySet().removeIf(p->p.distSqr(mc.player.blockPosition())>48*48||!DartboardBlock.is(mc.level.getBlockState(p)));
    }
    /** The view follows the dart's wander while it is held. */
    @SubscribeEvent public static void sway(ViewportEvent.ComputeCameraAngles e){
        var p=Minecraft.getInstance().player;
        if(p==null||!p.isUsingItem()||!p.getUseItem().is(BarRegistry.DART.get())||!DartBoards.isHouseDart(p.getUseItem()))return;
        float held=p.getTicksUsingItem()+(float)e.getPartialTick();
        var s=DartItem.sway(held,p.isShiftKeyDown(),DartItem.phase(p.getUUID(),p.getUseItem().getCount()));
        e.setYaw(e.getYaw()+(float)s[0]);e.setPitch(e.getPitch()+(float)s[1]);
    }
    /** The flights' colours, by place at the board. */
    static final int[] FLIGHTS={0xC8301E,0x2850B8,0x1E7A3C,0xE8C020};
    static final ResourceLocation WHITE=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/block/bar/ball.png");

    /** The lines chalked beside a board, from what the server sent. A tab parts a name from the score chalked at the right. */
    public static List<String> chalk(CompoundTag info){
        var out=new ArrayList<String>();
        DartsGame.Mode mode;try{mode=DartsGame.Mode.valueOf(info.getString("Mode"));}catch(IllegalArgumentException ex){mode=DartsGame.Mode.X01;}
        out.add(mode.title());
        var names=new ArrayList<String>();for(Tag t:info.getList("Names",Tag.TAG_STRING))names.add(t.getAsString());
        var g=info.contains("Game")?DartsGame.load(info.getCompound("Game")):null;int shooter=info.getInt("Shooter");
        for(int i=0;i<names.size();i++){
            String n=names.get(i);if(n.length()>12)n=n.substring(0,12);
            String score;
            if(g==null||i>=g.players())score=mode==DartsGame.Mode.X01?String.valueOf(DartsGame.START):"1";
            else if(mode==DartsGame.Mode.X01)score=String.valueOf(g.score[i]);
            else score=g.score[i]>DartsGame.BULL_TARGET?"done":g.score[i]==DartsGame.BULL_TARGET?"bull":String.valueOf(g.score[i]);
            boolean won=g!=null&&g.winner==i;
            out.add((won?"* ":i==shooter&&(g==null||!g.over())?"> ":"  ")+n+"\t"+score);
        }
        if(g!=null){
            var darts=g.thisTurn.isEmpty()?g.lastTurn:g.thisTurn;
            if(!darts.isEmpty()){var line=new StringBuilder();for(var h:darts){if(line.length()>0)line.append("  ");line.append(h.chalk());}out.add("");out.add(line.toString());}
        }
        return out;
    }

    /** The chalked score, on the wall to the right of the board. */
    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Chalk implements BlockEntityRenderer<DartboardBlockEntity> {
        final Font font;
        public Chalk(BlockEntityRendererProvider.Context c){font=c.getFont();}
        @SubscribeEvent public static void register(EntityRenderersEvent.RegisterRenderers e){
            e.registerBlockEntityRenderer(BarRegistry.DARTBOARD_ENTITY.get(),Chalk::new);
            e.registerBlockEntityRenderer(BarRegistry.SLATE_ENTITY.get(),BarSlateRenderer::new);
            e.registerEntityRenderer(BarRegistry.DART_ENTITY.get(),Dart::new);
        }
        @Override public void render(DartboardBlockEntity be,float partial,PoseStack pose,MultiBufferSource out,int light,int overlay){
            var info=STATES.get(be.getBlockPos());var state=be.getBlockState();if(info==null||!DartboardBlock.is(state))return;
            var facing=state.getValue(DartboardBlock.FACING);var right=DartboardBlock.right(facing);
            var level=be.getLevel();int lit=level==null?light:LevelRenderer.getLightColor(level,be.getBlockPos().relative(right));
            pose.pushPose();
            // To the board's right, flat on the wall behind it.
            pose.translate(.5+right.getStepX()*1.05-facing.getStepX()*.49,.5,.5+right.getStepZ()*1.05-facing.getStepZ()*.49);
            pose.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
            // The slate: a dark board with a wooden edge.
            var buffer=out.getBuffer(RenderType.entityCutoutNoCull(WHITE));var m=pose.last();
            panel(buffer,m,-.47F,-.47F,.47F,.47F,0F,0x4A3020,lit);
            panel(buffer,m,-.44F,-.44F,.44F,.44F,.002F,0x23272A,lit);
            float s=1/100F;int w=82;
            pose.translate(-.40,.40,.006);pose.scale(s,-s,s);
            int y=0;
            for(var line:chalk(info)){
                int colour=y==0?0xFFF2EEE0:0xFFD8D8CC;var parts=line.split("\t",2);
                // A long name gives way to its score.
                String left=parts.length>1?font.plainSubstrByWidth(parts[0],w-font.width(parts[1])-5):parts[0];
                font.drawInBatch(left,0,y,colour,false,pose.last().pose(),out,Font.DisplayMode.POLYGON_OFFSET,0,lit);
                if(parts.length>1)font.drawInBatch(parts[1],w-font.width(parts[1]),y,colour,false,pose.last().pose(),out,Font.DisplayMode.POLYGON_OFFSET,0,lit);
                y+=10;
            }
            pose.popPose();
        }
        static void panel(VertexConsumer b,PoseStack.Pose m,float x0,float y0,float x1,float y1,float z,int rgb,int light){
            int r=rgb>>16&255,g=rgb>>8&255,bl=rgb&255;
            b.addVertex(m,x0,y0,z).setColor(r,g,bl,255).setUv(0,1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(m,0,0,1);
            b.addVertex(m,x1,y0,z).setColor(r,g,bl,255).setUv(1,1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(m,0,0,1);
            b.addVertex(m,x1,y1,z).setColor(r,g,bl,255).setUv(1,0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(m,0,0,1);
            b.addVertex(m,x0,y1,z).setColor(r,g,bl,255).setUv(0,0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(m,0,0,1);
        }
        @Override public AABB getRenderBoundingBox(DartboardBlockEntity be){return be.renderBox();}
    }

    /** A dart: steel point, tungsten barrel, a black shaft and flights in the thrower's colour. Drawn pointing the way it flew. */
    public static final class Dart extends EntityRenderer<DartEntity> {
        public Dart(EntityRendererProvider.Context c){super(c);}
        @Override public ResourceLocation getTextureLocation(DartEntity e){return WHITE;}
        @Override public void render(DartEntity e,float yaw,float partial,PoseStack pose,MultiBufferSource out,int light){
            pose.pushPose();
            pose.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partial,e.yRotO,e.getYRot())-90F));
            pose.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partial,e.xRotO,e.getXRot())));
            float shake=e.shakeTime-partial;if(shake>0)pose.mulPose(Axis.ZP.rotationDegrees(-Mth.sin(shake*3)*shake*.6F));
            var b=out.getBuffer(RenderType.entityCutoutNoCull(WHITE));var m=pose.last();
            int flight=FLIGHTS[Math.floorMod(e.seat(),FLIGHTS.length)];
            box(b,m,-.02F,.06F,.006F,.006F,0xD8D8D8,light);    // the point
            box(b,m,-.11F,-.02F,.018F,.018F,0x6E7074,light);   // the barrel
            box(b,m,-.20F,-.11F,.007F,.007F,0x161616,light);   // the shaft
            box(b,m,-.27F,-.19F,.004F,.045F,flight,light);     // the flights, crossed
            box(b,m,-.27F,-.19F,.045F,.004F,flight,light);
            pose.popPose();
            super.render(e,yaw,partial,pose,out,light);
        }
        static void box(VertexConsumer b,PoseStack.Pose m,float x0,float x1,float hy,float hz,int rgb,int light){
            int r=rgb>>16&255,g=rgb>>8&255,bl=rgb&255;
            face(b,m,x0,-hy,-hz,x0,hy,-hz,x1,hy,-hz,x1,-hy,-hz,0,0,-1,r,g,bl,light);
            face(b,m,x0,-hy,hz,x1,-hy,hz,x1,hy,hz,x0,hy,hz,0,0,1,r,g,bl,light);
            face(b,m,x0,hy,-hz,x0,hy,hz,x1,hy,hz,x1,hy,-hz,0,1,0,r*9/10,g*9/10,bl*9/10,light);
            face(b,m,x0,-hy,-hz,x1,-hy,-hz,x1,-hy,hz,x0,-hy,hz,0,-1,0,r*7/10,g*7/10,bl*7/10,light);
            face(b,m,x1,-hy,-hz,x1,hy,-hz,x1,hy,hz,x1,-hy,hz,1,0,0,r*8/10,g*8/10,bl*8/10,light);
            face(b,m,x0,-hy,-hz,x0,-hy,hz,x0,hy,hz,x0,hy,-hz,-1,0,0,r*8/10,g*8/10,bl*8/10,light);
        }
        static void face(VertexConsumer b,PoseStack.Pose m,float ax,float ay,float az,float bx,float by,float bz,float cx,float cy,float cz,float dx,float dy,float dz,float nx,float ny,float nz,int r,int g,int bl,int light){
            b.addVertex(m,ax,ay,az).setColor(r,g,bl,255).setUv(0,0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(m,nx,ny,nz);
            b.addVertex(m,bx,by,bz).setColor(r,g,bl,255).setUv(0,1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(m,nx,ny,nz);
            b.addVertex(m,cx,cy,cz).setColor(r,g,bl,255).setUv(1,1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(m,nx,ny,nz);
            b.addVertex(m,dx,dy,dz).setColor(r,g,bl,255).setUv(1,0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(m,nx,ny,nz);
        }
    }
}
