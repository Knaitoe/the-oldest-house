package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.KingBreath;
import io.github.knaitoe.theoldesthouse.labyrinth.KingRegistry;
import io.github.knaitoe.theoldesthouse.network.KingStatePayload;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;

/**
 * 0.4.642, how much of a thing each reader in the King's country is, as their client shows it (labyrinth.KingBreath,
 * docs/SLEEPING_KING_0_4_642.md). A reader reads it off themselves, never off a number: their own body, other readers' bodies, and the
 * sound of their feet.
 * <ul>
 * <li>A thing, and too much of one, is drawn as they are.</li>
 * <li>A sort of thing is drawn flat, its depth pressed to a card's, and casts no shadow; its feet are soft.</li>
 * <li>A line is drawn in outline: every edge of the body as it would have been drawn, in pencil, and nothing filled; no shadow, no
 *     hands in front of the reader's own eyes, and no sound of feet.</li>
 * </ul>
 * The states come from the server every quarter second and lapse two seconds after it stops sending them.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class KingClient {
    private KingClient(){}
    private static final Map<UUID,Long> UNTIL=new HashMap<>();
    private static final int LAPSE=40;
    /** Card-thin: how deep a sort of thing is drawn, of its whole depth. */
    public static final float FLAT=.12F;
    /** The pencil: a soft dark grey, nearly opaque. */
    private static final int INK_R=58,INK_G=54,INK_B=50,INK_A=230;
    private static boolean outlining;
    /** 0.4.648: the feast's guests are drawn in pencil the same way, and their names are not written in pencil either. */
    static void outlining(boolean on){outlining=on;}

    public static void accept(KingStatePayload p){
        var level=Minecraft.getInstance().level;if(level==null)return;long until=level.getGameTime()+LAPSE;
        for(var e:p.readers()){KingBreath.clientSet(e.reader(),e.state());UNTIL.put(e.reader(),until);}
    }
    /** How much of a thing a player is, as this client knows it; anybody it does not know of is a thing. */
    public static int state(Entity e){return e instanceof Player&&UNTIL.containsKey(e.getUUID())?KingBreath.of(e):KingBreath.THING;}
    /** A sort of thing or a line casts no shadow (client.mixin.KingShadowMixin). */
    public static boolean shadowless(Entity e){int s=state(e);return s==KingBreath.SORT||s==KingBreath.LINE;}

    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        var level=Minecraft.getInstance().level;if(level==null||UNTIL.isEmpty())return;long now=level.getGameTime();
        UNTIL.entrySet().removeIf(en->{if(en.getValue()>=now&&en.getValue()-now<=LAPSE+5)return false;KingBreath.clientForget(en.getKey());return true;});
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){UNTIL.keySet().forEach(KingBreath::clientForget);UNTIL.clear();}

    // ------------------------------------------------------------------------------------------------ the body
    @SubscribeEvent public static void before(RenderPlayerEvent.Pre e){
        // Drawing the outline: only the body itself, after the renderer has set the skin's layers for this frame. The outer layers
        // would draw every edge twice, a cage round the figure; the next frame sets them again.
        if(outlining){var m=e.getRenderer().getModel();m.hat.visible=m.jacket.visible=m.leftSleeve.visible=m.rightSleeve.visible=m.leftPants.visible=m.rightPants.visible=false;return;}
        var player=e.getEntity();int s=state(player);
        if(s==KingBreath.SORT){
            // Pressed flat along the body's own depth: the renderer turns the body by (180 - yaw) after this, so the press is turned to
            // meet it and the card faces the way the body faces.
            float yaw=Mth.rotLerp(e.getPartialTick(),player.yBodyRotO,player.yBodyRot);var pose=e.getPoseStack();pose.pushPose();
            pose.mulPose(Axis.YP.rotationDegrees(180-yaw));pose.scale(1,1,FLAT);pose.mulPose(Axis.YP.rotationDegrees(yaw-180));
        }else if(s==KingBreath.LINE){
            e.setCanceled(true);
            var pencil=e.getMultiBufferSource().getBuffer(RenderType.lines());
            MultiBufferSource outline=type->new Edges(pencil);
            outlining=true;
            try{e.getRenderer().render((net.minecraft.client.player.AbstractClientPlayer)player,Mth.lerp(e.getPartialTick(),player.yRotO,player.getYRot()),e.getPartialTick(),e.getPoseStack(),outline,e.getPackedLight());}
            finally{outlining=false;}
        }
    }
    @SubscribeEvent public static void after(RenderPlayerEvent.Post e){if(!outlining&&state(e.getEntity())==KingBreath.SORT)e.getPoseStack().popPose();}
    /** Nobody's name is written in pencil. */
    @SubscribeEvent public static void name(net.neoforged.neoforge.client.event.RenderNameTagEvent e){if(outlining)e.setCanRender(net.neoforged.neoforge.common.util.TriState.FALSE);}
    /** A line holds nothing and has no hands before its eyes. */
    @SubscribeEvent public static void hands(RenderHandEvent e){var p=Minecraft.getInstance().player;if(p!=null&&state(p)==KingBreath.LINE)e.setCanceled(true);}

    /**
     * Every face the renderer would have filled, as its four edges in pencil. The renderer hands each quad over a vertex at a time,
     * already placed; every fourth vertex closes one.
     */
    static final class Edges implements VertexConsumer {
        private final VertexConsumer pencil;private final float[] at=new float[12];private int n;
        Edges(VertexConsumer pencil){this.pencil=pencil;}
        @Override public VertexConsumer addVertex(float x,float y,float z){
            at[n*3]=x;at[n*3+1]=y;at[n*3+2]=z;
            if(++n==4){n=0;for(int i=0;i<4;i++){int j=(i+1)%4;line(at[i*3],at[i*3+1],at[i*3+2],at[j*3],at[j*3+1],at[j*3+2]);}}
            return this;
        }
        private void line(float x0,float y0,float z0,float x1,float y1,float z1){
            float dx=x1-x0,dy=y1-y0,dz=z1-z0,len=Mth.sqrt(dx*dx+dy*dy+dz*dz);if(len<1e-5F)return;dx/=len;dy/=len;dz/=len;
            pencil.addVertex(x0,y0,z0).setColor(INK_R,INK_G,INK_B,INK_A).setNormal(dx,dy,dz);
            pencil.addVertex(x1,y1,z1).setColor(INK_R,INK_G,INK_B,INK_A).setNormal(dx,dy,dz);
        }
        @Override public VertexConsumer setColor(int r,int g,int b,int a){return this;}
        @Override public VertexConsumer setUv(float u,float v){return this;}
        @Override public VertexConsumer setUv1(int u,int v){return this;}
        @Override public VertexConsumer setUv2(int u,int v){return this;}
        @Override public VertexConsumer setNormal(float x,float y,float z){return this;}
    }

    // ------------------------------------------------------------------------------------------------ the feet
    /** A sort of thing's feet are soft; a line's make no sound. Whose feet a step is, is the player standing where it sounds. */
    @SubscribeEvent public static void feet(PlaySoundEvent e){
        var sound=e.getSound();var level=Minecraft.getInstance().level;if(sound==null||level==null||UNTIL.isEmpty())return;
        if(!sound.getLocation().getPath().endsWith(".step"))return;
        Player whose=null;double best=2.25;
        for(var p:level.players()){double dx=p.getX()-sound.getX(),dz=p.getZ()-sound.getZ(),dy=p.getY()-sound.getY();double d=dx*dx+dz*dz;if(Math.abs(dy)<1.6&&d<best){best=d;whose=p;}}
        if(whose==null)return;int s=state(whose);
        if(s==KingBreath.LINE)e.setSound(null);
        else if(s==KingBreath.SORT)e.setSound(new SimpleSoundInstance(KingRegistry.SOFT_STEP.get(),SoundSource.PLAYERS,.35F,.9F+level.random.nextFloat()*.2F,level.random,sound.getX(),sound.getY(),sound.getZ()));
    }
}
