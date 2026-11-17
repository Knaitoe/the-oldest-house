package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.NotYet;
import io.github.knaitoe.theoldesthouse.labyrinth.NotYetBlock;
import io.github.knaitoe.theoldesthouse.labyrinth.NotYetMending;
import io.github.knaitoe.theoldesthouse.labyrinth.NotYetRegistry;
import io.github.knaitoe.theoldesthouse.network.NotYetStatePayload;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.resources.PlayerSkin;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/**
 * 0.4.664, Not Yet (docs/NOT_YET_0_4_664.md), as one reader's game draws it.
 *
 * <p><b>The counter.</b> Seven windows over the arch, drawn by this game alone with the years the machine has kept this reader: nought
 * until the first mending, then one year for every three seconds since, by the world's clock, whether they are in the ward or not. The
 * windows are of the kind the House's read of their age gives them: drums that roll the next figure up (analog), cards that fall in two
 * halves (transitional), or lit bars that change at once (digital). Each turn of a window clicks, for this reader only.
 *
 * <p><b>The seams.</b> Every reader the machine has mended is drawn by every viewer with a seam for each mending, in a fixed order over
 * the brow, the wrists, the cheek, the crown, a forearm and the jaw, on the skin under any outer layer: a dark line closed with pale
 * stitches. Out of the place they fade over two seconds. A reader sees their own on their hands.
 *
 * <p><b>Held.</b> While the machine has a reader on the table or on their cot, the bed screen offers no way up and draws nothing over the
 * dark (GoatmanBedWakeMixin asks {@link #held}).
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class NotYetClient {
    private NotYetClient(){}
    static final ResourceLocation SEAMS=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/not_yet_seams.png");
    static final ResourceLocation[] DIGITS={tex("drum"),tex("flap"),tex("segment")};
    private static ResourceLocation tex(String kind){return ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/misc/not_yet_digits_"+kind+".png");}
    /** Ticks a window takes to turn: a drum rolls, a card falls, a bar changes at once. */
    static final int[] TURN={8,6,0};
    private static boolean held;private static long countFrom=-1;private static int kind;private static BlockPos counter;
    private static final int[] shown=new int[7],from=new int[7];private static final long[] turnedAt=new long[7];private static boolean primed;
    private record Seams(int count,long fadingFrom){}
    private static final Map<UUID,Seams> SEAMED=new HashMap<>();

    public static boolean held(){return held;}
    public static void state(NotYetStatePayload p){held=p.held();if(p.countFrom()!=countFrom||p.kind()!=kind||!p.counter().equals(counter))primed=false;countFrom=p.countFrom();kind=Math.max(0,Math.min(2,p.kind()));counter=p.counter();}
    public static void seams(UUID player,int count){
        var was=SEAMED.get(player);var level=Minecraft.getInstance().level;long now=level==null?0:level.getGameTime();
        if(count>0)SEAMED.put(player,new Seams(count,-1));else if(was!=null&&was.count()>0&&was.fadingFrom()<0)SEAMED.put(player,new Seams(was.count(),now));
    }
    /** How many seams to draw on a player, and how solid (1 while in the place, fading to nothing over two seconds after). */
    static int seamsOf(UUID id){var s=SEAMED.get(id);return s==null?0:s.count();}
    static float seamAlpha(UUID id,float partial){
        var s=SEAMED.get(id);if(s==null)return 0;if(s.fadingFrom()<0)return 1;var level=Minecraft.getInstance().level;if(level==null)return 0;
        float a=1-(level.getGameTime()+partial-s.fadingFrom())/40F;if(a<=0){SEAMED.remove(id);return 0;}return a;
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){held=false;countFrom=-1;counter=null;SEAMED.clear();primed=false;}

    // ------------------------------------------------------------------------------------------------ the counter
    /** The figure in one window (0, the most significant, at the west end) for this many years. */
    static int digit(long years,int window){long v=years;for(int k=0;k<6-window;k++)v/=10;return (int)(v%10);}
    static long years(long gameTime){return countFrom<0?0:Math.max(0,Math.min(9_999_999L,(gameTime-countFrom)/NotYetMending.YEAR));}
    static boolean counterHere(){var mc=Minecraft.getInstance();return counter!=null&&mc.level!=null&&mc.player!=null&&NotYetBlock.is(mc.level.getBlockState(counter),NotYetBlock.Kind.COUNTER)&&mc.player.distanceToSqr(counter.getCenter())<48*48;}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        var mc=Minecraft.getInstance();if(mc.level==null||!counterHere()){primed=false;return;}
        long now=mc.level.getGameTime(),years=years(now);boolean turned=false;
        for(int i=0;i<7;i++){int d=digit(years,i);if(!primed){shown[i]=d;from[i]=d;turnedAt[i]=-100;continue;}if(d!=shown[i]){from[i]=shown[i];shown[i]=d;turnedAt[i]=now;turned=true;}}
        primed=true;
        if(turned&&mc.player.distanceToSqr(counter.getCenter())<24*24)
            mc.getSoundManager().play(new SimpleSoundInstance(NotYetRegistry.TICK.get(),SoundSource.BLOCKS,.45F,.95F+mc.level.random.nextFloat()*.1F,RandomSource.create(),counter.getX()+3.5,counter.getY()+.5,counter.getZ()+.3));
    }
    /** The windows' figures, drawn in each window's slot (two pixels in from the sides, one from top and bottom, at the plate's face). */
    @SubscribeEvent public static void render(RenderLevelStageEvent e){
        if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS||!counterHere())return;
        var mc=Minecraft.getInstance();var cam=e.getCamera().getPosition();float partial=e.getPartialTick().getGameTimeDeltaPartialTick(false);
        long now=mc.level.getGameTime();
        if(!primed){long years=years(now);for(int i=0;i<7;i++){shown[i]=digit(years,i);from[i]=shown[i];}}
        var buffers=mc.renderBuffers().bufferSource();var buffer=buffers.getBuffer(RenderType.entityCutoutNoCull(DIGITS[kind]));
        var pose=e.getPoseStack();
        for(int i=0;i<7;i++){
            // The counter faces south in its build (NotYetSite.COUNTER_FIRST), its windows running east; the plate's face is three pixels out.
            var cell=counter.offset(i,0,0);
            int light=kind==NotYet.SEGMENT?LightTexture.FULL_BRIGHT:LevelRenderer.getLightColor(mc.level,cell.south());
            pose.pushPose();pose.translate(cell.getX()-cam.x,cell.getY()-cam.y,cell.getZ()+3/16.0+.002-cam.z);
            float t=TURN[kind]==0?1:Mth.clamp((now-turnedAt[i]+partial)/TURN[kind],0,1);
            if(t>=1)window(buffer,pose,shown[i],0,14,light);
            else if(kind==NotYet.DRUM){float roll=Math.floorMod(shown[i]-from[i],10)*t;window(buffer,pose,from[i],roll,14,light);}
            else flap(buffer,pose,from[i],shown[i],t,light);
            pose.popPose();
        }
        buffers.endBatch(RenderType.entityCutoutNoCull(DIGITS[kind]));
    }
    /** One figure (or, rolling, the strip from one figure on by so many figures), filling the slot. */
    static void window(VertexConsumer b,PoseStack pose,int figure,float roll,int rows,int light){
        float top=(figure+roll)*14;top=((top%140)+140)%140;
        float first=Math.min(14,140-top);
        quad(b,pose,15,15-first,top/140F,(top+first)/140F,light);
        if(first<14)quad(b,pose,15-first,1,0,(14-first)/140F,light);
    }
    /** A slot-wide strip between two heights (in the block's pixels, from its foot), showing the strip's rows v0..v1. */
    static void quad(VertexConsumer b,PoseStack pose,float yTop,float yBottom,float v0,float v1,int light){
        var last=pose.last();float x0=2/16F,x1=14/16F,y0=yBottom/16F,y1=yTop/16F;
        b.addVertex(last,x0,y0,0).setColor(-1).setUv(0,v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(last,0,0,1);
        b.addVertex(last,x1,y0,0).setColor(-1).setUv(1,v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(last,0,0,1);
        b.addVertex(last,x1,y1,0).setColor(-1).setUv(1,v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(last,0,0,1);
        b.addVertex(last,x0,y1,0).setColor(-1).setUv(0,v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(last,0,0,1);
    }
    /**
     * A card falling: the new figure's top half and the old figure's bottom half stand still; between them the old top half swings down
     * toward the reader on the hinge across the middle, and past level the new bottom half comes down onto the old.
     */
    static void flap(VertexConsumer b,PoseStack pose,int old,int now,float t,int light){
        float mid=8;
        quad(b,pose,15,mid,now*14/140F,(now*14+7)/140F,light);
        quad(b,pose,mid,1,(old*14+7)/140F,(old*14+14)/140F,light);
        float angle=t*180;
        pose.pushPose();pose.translate(0,mid/16F,.001);
        if(angle<90){pose.mulPose(Axis.XP.rotationDegrees(angle));quad(b,pose,15-mid,0,old*14/140F,(old*14+7)/140F,light);}
        else{pose.mulPose(Axis.XP.rotationDegrees(angle-180));quad(b,pose,0,1-mid,(now*14+7)/140F,(now*14+14)/140F,light);}
        pose.popPose();
    }

    // ------------------------------------------------------------------------------------------------ the seams
    /**
     * Where each mending's seam goes, in order: a part, a face of it, the seam's middle on that face and its length, whether it runs
     * across (false) or down (true) the face, and which of the four drawn seams it uses. Positions are in the part's own pixels.
     */
    private enum Part{HEAD,BODY,LEFT_ARM,RIGHT_ARM}
    private enum Face{FRONT,LEFT,RIGHT,TOP,RING}
    private record Seam(Part part,Face face,float a,float b,float length,boolean down,int row){}
    private static final Seam[] ORDER={
        new Seam(Part.HEAD,Face.FRONT,0,-5.5F,6,false,1),
        new Seam(Part.LEFT_ARM,Face.RING,0,8.5F,0,false,0),
        new Seam(Part.HEAD,Face.FRONT,-2.5F,-1.5F,3,true,3),
        new Seam(Part.RIGHT_ARM,Face.RING,0,8.5F,0,false,0),
        new Seam(Part.HEAD,Face.TOP,1,0,6,true,2),
        new Seam(Part.LEFT_ARM,Face.FRONT,0,5,4,true,3),
        new Seam(Part.HEAD,Face.LEFT,-1,-1.5F,5,false,1),
        new Seam(Part.RIGHT_ARM,Face.RING,0,4.5F,0,false,0)};
    public static final int MOST=ORDER.length;
    /** The four drawn seams in the texture: the row each lies on (its stitches above and below it) and where each starts and how long. */
    private static final int[][] DRAWN={{1,1,14},{5,3,10},{9,2,12},{13,4,8}};

    /** One seam strip, a rectangle on a face: from (x0,y0) to (x1,y1) in the face's own coordinates, drawn with its length along x or y. */
    private static void strip(VertexConsumer b,PoseStack.Pose last,float[] p0,float[] du,float[] dv,float[] normal,int row,float length,int light,int overlay,int alpha){
        var d=DRAWN[row];float u0=d[1]/16F,u1=Math.min(d[1]+length,d[1]+d[2])/16F,v0=(d[0]-1)/16F,v1=(d[0]+2)/16F;
        int color=(alpha<<24)|0xFFFFFF;
        float[][] at={p0,{p0[0]+du[0],p0[1]+du[1],p0[2]+du[2]},{p0[0]+du[0]+dv[0],p0[1]+du[1]+dv[1],p0[2]+du[2]+dv[2]},{p0[0]+dv[0],p0[1]+dv[1],p0[2]+dv[2]}};
        float[][] uv={{u0,v0},{u1,v0},{u1,v1},{u0,v1}};
        for(int i=0;i<4;i++)b.addVertex(last,at[i][0]/16,at[i][1]/16,at[i][2]/16).setColor(color).setUv(uv[i][0],uv[i][1]).setOverlay(overlay).setLight(light).setNormal(last,normal[0],normal[1],normal[2]);
    }
    /** A part's box, in its own pixels: x0,y0,z0,x1,y1,z1. */
    private static float[] box(Part part,boolean slim){
        return switch(part){
            case HEAD->new float[]{-4,-8,-4,4,0,4};
            case BODY->new float[]{-4,0,-2,4,12,2};
            case RIGHT_ARM->slim?new float[]{-2,-2,-2,1,10,2}:new float[]{-3,-2,-2,1,10,2};
            case LEFT_ARM->slim?new float[]{-1,-2,-2,2,10,2}:new float[]{-1,-2,-2,3,10,2};
        };
    }
    /** Draws one seam on its part (the part's transform already applied). A pixel's sixtieth off the skin, under any outer layer. */
    static void seam(VertexConsumer b,PoseStack pose,Seam s,boolean slim,int light,int overlay,int alpha){
        var c=box(s.part(),slim);float o=.03F;var last=pose.last();float half=1.5F;
        float midX=(c[0]+c[3])/2+s.a();
        switch(s.face()){
            case FRONT->{
                float z=c[2]-o;
                if(!s.down())strip(b,last,new float[]{midX-s.length()/2,s.b()-half,z},new float[]{s.length(),0,0},new float[]{0,3,0},new float[]{0,0,-1},s.row(),s.length(),light,overlay,alpha);
                else strip(b,last,new float[]{midX+half,s.b()-s.length()/2,z},new float[]{0,s.length(),0},new float[]{-3,0,0},new float[]{0,0,-1},s.row(),s.length(),light,overlay,alpha);
            }
            case LEFT->{float x=c[3]+o;strip(b,last,new float[]{x,s.b()-half,s.a()-s.length()/2},new float[]{0,0,s.length()},new float[]{0,3,0},new float[]{1,0,0},s.row(),s.length(),light,overlay,alpha);}
            case RIGHT->{float x=c[0]-o;strip(b,last,new float[]{x,s.b()-half,s.a()+s.length()/2},new float[]{0,0,-s.length()},new float[]{0,3,0},new float[]{-1,0,0},s.row(),s.length(),light,overlay,alpha);}
            case TOP->{float y=c[1]-o;strip(b,last,new float[]{midX-half,y,-s.length()/2},new float[]{0,0,s.length()},new float[]{3,0,0},new float[]{0,-1,0},s.row(),s.length(),light,overlay,alpha);}
            case RING->{
                float y0=s.b()-half,w=c[3]-c[0],dep=c[5]-c[2];
                strip(b,last,new float[]{c[0],y0,c[2]-o},new float[]{w,0,0},new float[]{0,3,0},new float[]{0,0,-1},s.row(),w,light,overlay,alpha);
                strip(b,last,new float[]{c[3],y0,c[5]+o},new float[]{-w,0,0},new float[]{0,3,0},new float[]{0,0,1},s.row(),w,light,overlay,alpha);
                strip(b,last,new float[]{c[3]+o,y0,c[2]},new float[]{0,0,dep},new float[]{0,3,0},new float[]{1,0,0},s.row(),dep,light,overlay,alpha);
                strip(b,last,new float[]{c[0]-o,y0,c[5]},new float[]{0,0,-dep},new float[]{0,3,0},new float[]{-1,0,0},s.row(),dep,light,overlay,alpha);
            }
        }
    }
    static ModelPart part(PlayerModel<?> model,Part part){return switch(part){case HEAD->model.head;case BODY->model.body;case LEFT_ARM->model.leftArm;case RIGHT_ARM->model.rightArm;};}
    /** A body's first so many seams, on a player model already posed: for every player in the place, and for the review frames. */
    static void seams(PlayerModel<?> model,PoseStack pose,VertexConsumer buffer,int count,boolean slim,int light,int overlay,int alpha){
        for(int i=0;i<Math.min(MOST,count);i++){var s=ORDER[i];var p=part(model,s.part());if(!p.visible)continue;pose.pushPose();p.translateAndRotate(pose);seam(buffer,pose,s,slim,light,overlay,alpha);pose.popPose();}
    }

    public static final class Layer extends RenderLayer<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> {
        public Layer(RenderLayerParent<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> parent){super(parent);}
        @Override public void render(PoseStack pose,MultiBufferSource out,int light,AbstractClientPlayer player,float walk,float amount,float partial,float age,float yaw,float pitch){
            int count=Math.min(MOST,seamsOf(player.getUUID()));if(count<=0||player.isInvisible())return;
            float a=seamAlpha(player.getUUID(),partial);if(a<=0)return;
            var buffer=out.getBuffer(RenderType.entityTranslucent(SEAMS));int overlay=LivingEntityRenderer.getOverlayCoords(player,0);
            boolean slim=player.getSkin().model()==PlayerSkin.Model.SLIM;var model=getParentModel();
            seams(model,pose,buffer,count,slim,light,overlay,(int)(a*255));
        }
    }
    /** The reader's own arm in first person, with the seams on it. */
    @SubscribeEvent public static void ownArm(RenderArmEvent e){
        var player=e.getPlayer();int count=Math.min(MOST,seamsOf(player.getUUID()));if(count<=0)return;
        float a=seamAlpha(player.getUUID(),0);if(a<=0)return;
        if(!(Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player) instanceof PlayerRenderer renderer))return;
        var model=renderer.getModel();model.attackTime=0;model.crouching=false;model.swimAmount=0;model.setupAnim(player,0,0,0,0,0);
        var which=e.getArm()==HumanoidArm.RIGHT?Part.RIGHT_ARM:Part.LEFT_ARM;ModelPart arm=part(model,which);arm.xRot=0;
        boolean slim=player.getSkin().model()==PlayerSkin.Model.SLIM;var buffer=e.getMultiBufferSource().getBuffer(RenderType.entityTranslucent(SEAMS));
        var pose=e.getPoseStack();pose.pushPose();arm.translateAndRotate(pose);
        for(int i=0;i<count;i++)if(ORDER[i].part()==which)seam(buffer,pose,ORDER[i],slim,e.getPackedLight(),OverlayTexture.NO_OVERLAY,(int)(a*255));
        pose.popPose();
    }
    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Registration {
        @SubscribeEvent public static void layers(EntityRenderersEvent.AddLayers e){
            for(var skin:e.getSkins()){EntityRenderer<? extends Player> renderer=e.getSkin(skin);if(renderer instanceof PlayerRenderer player)player.addLayer(new Layer(player));}
        }
    }
}
