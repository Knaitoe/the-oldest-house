package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.PellPeople;
import io.github.knaitoe.theoldesthouse.network.PellSensePayload;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.common.util.TriState;

/**
 * 0.4.546, Tilly's sight (docs/PELL_HOUSE_SENSES_0_4_546.md), on the screen of the reader playing her and nobody else's: "Her view is light
 * and shape, never black. Edges and faces dissolve past a few blocks, and strong light blooms." This client is Tilly while the server draws
 * it as her (PellTillyLook).
 * <ul>
 * <li><b>The haze.</b> At full strength things are clear to {@link #CLEAR} blocks and gone into the haze by {@link #GONE}; pale by day, a
 * dim grey at night, never black. Faces (the nameplates of other readers) go into it too. The game's own view under water is left as it
 * is.</li>
 * <li><b>The bloom.</b> In bright light (a lamp, the pool lit from below, a window by day) the edges of the view go pale with it.</li>
 * <li><b>The cane's tap</b> (labyrinth.PellSenses): what was in reach is outlined for a moment, through the haze.</li>
 * <li><b>The line's cold:</b> a frost at the edges of the screen for a second and a half.</li>
 * <li><b>June, quietly:</b> her lines on the telephone that only Tilly hears, as captions from where she stands, in grey.</li>
 * </ul>
 * The haze, the bloom and the cold follow the screen-effects setting (Comfort), down to nothing: at nothing, Tilly's view is the reader's
 * own. The outlines are what the cane found, and stay.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class PellTillySight {
    private PellTillySight(){}
    /** The haze at full strength: clear to here, gone by here (blocks). */
    public static final float CLEAR=2F,GONE=9F;
    /** The haze's colour by day and at night. */
    static final float[] DAY={.86F,.85F,.81F},NIGHT={.2F,.2F,.22F};
    /** How long the tap's outlines stay, the last part of it fading; how long the line's cold lasts (ticks). */
    public static final int SHOWN=40,FADE=15,COLD=30;
    private static final ResourceLocation VIGNETTE=ResourceLocation.withDefaultNamespace("textures/misc/vignette.png"),
        FROST=ResourceLocation.withDefaultNamespace("textures/misc/powder_snow_outline.png");
    private static float[] outline=new float[0];private static long shownAt=Long.MIN_VALUE;
    private static int cold;private static float bloom;private static boolean registered;
    private record Hushed(Vec3 at,String words,long until){}
    private static final List<Hushed> HUSHED=new ArrayList<>();

    public static void accept(PellSensePayload p){
        var mc=Minecraft.getInstance();if(mc.level==null)return;
        if(!registered){SpeechCaptions.voices(PellTillySight::voices);registered=true;}
        switch(p.kind()){
            case PellSensePayload.OUTLINE->{var f=new float[p.lines().size()];for(int i=0;i<f.length;i++)f[i]=p.lines().get(i);outline=f;shownAt=mc.level.getGameTime();}
            case PellSensePayload.COLD->cold=COLD;
            case PellSensePayload.HUSH->{int ticks=p.lines().isEmpty()?60:Math.round(p.lines().get(0));HUSHED.add(new Hushed(new Vec3(p.x(),p.y(),p.z()),p.words(),mc.level.getGameTime()+ticks));}
            default->{}
        }
    }
    /** Whether this client's player is Tilly at the Pell house now. */
    public static boolean tilly(){var mc=Minecraft.getInstance();return mc.player!=null&&mc.level!=null&&PellTillyLook.gesture(mc.player.getUUID())>=0;}
    /** How strongly Tilly's sight shows on this screen: nothing unless they are Tilly; then the screen-effects setting. */
    public static float strength(){return tilly()?Comfort.effects():0F;}
    /** For the native proof: the outlines shown now, and how many ticks the cold has left. */
    public static float[] outlines(){var mc=Minecraft.getInstance();return mc.level!=null&&mc.level.getGameTime()-shownAt<SHOWN?outline:new float[0];}
    public static int cold(){return cold;}

    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        var mc=Minecraft.getInstance();
        if(mc.level==null||mc.player==null){clear();return;}
        if(cold>0)cold--;
        long now=mc.level.getGameTime();HUSHED.removeIf(h->h.until()<now);
        float target=0;
        if(tilly()){int light=mc.level.getMaxLocalRawBrightness(BlockPos.containing(mc.player.getEyePosition()));target=Mth.clamp((light-11)/4F,0F,1F);}
        bloom+=(target-bloom)*.08F;
    }
    @SubscribeEvent public static void out(ClientPlayerNetworkEvent.LoggingOut e){clear();}
    private static void clear(){outline=new float[0];shownAt=Long.MIN_VALUE;cold=0;bloom=0;HUSHED.clear();}

    // ------------------------------------------------------------------------------------------------ the haze
    /** Toward the haze by the strength, as a ratio, so half strength is halfway by eye. */
    static float toward(float from,float to,float k){return from<=to?from:(float)(from*Math.pow(to/from,k));}
    @SubscribeEvent(priority=EventPriority.LOW) public static void haze(ViewportEvent.RenderFog e){
        float k=strength();if(k<=0||e.getType()!=FogType.NONE)return;
        if(e.getMode()!=FogRenderer.FogMode.FOG_TERRAIN&&e.getMode()!=FogRenderer.FogMode.FOG_SKY)return;
        float near=e.getNearPlaneDistance(),far=e.getFarPlaneDistance();
        e.setNearPlaneDistance(Math.min(near,toward(Math.max(near,CLEAR),CLEAR,k)));e.setFarPlaneDistance(Math.min(far,toward(Math.max(far,GONE),GONE,k)));
        e.setFogShape(FogShape.SPHERE);e.setCanceled(true);
    }
    @SubscribeEvent(priority=EventPriority.LOW) public static void colour(ViewportEvent.ComputeFogColor e){
        float k=strength();var mc=Minecraft.getInstance();if(k<=0||mc.level==null||e.getCamera().getFluidInCamera()!=FogType.NONE)return;
        float t=mc.level.getTimeOfDay((float)e.getPartialTick()),day=Mth.clamp(Mth.cos(t*Mth.TWO_PI)*2F+.5F,0F,1F);
        e.setRed(Mth.lerp(k,e.getRed(),Mth.lerp(day,NIGHT[0],DAY[0])));
        e.setGreen(Mth.lerp(k,e.getGreen(),Mth.lerp(day,NIGHT[1],DAY[1])));
        e.setBlue(Mth.lerp(k,e.getBlue(),Mth.lerp(day,NIGHT[2],DAY[2])));
    }
    /** Faces go into the haze: another reader's name is not shown past where the haze closes. */
    @SubscribeEvent public static void names(RenderNameTagEvent e){
        float k=strength();if(k<=0||!(e.getEntity() instanceof Player other))return;var mc=Minecraft.getInstance();if(other==mc.player)return;
        float far=toward(64F,GONE*.6F,k);
        if(other.distanceToSqr(mc.player)>far*far)e.setCanRender(TriState.FALSE);
    }

    // ------------------------------------------------------------------------------------------------ the cane's outlines
    @SubscribeEvent public static void render(RenderLevelStageEvent e){
        if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS)return;
        var mc=Minecraft.getInstance();if(mc.level==null||outline.length<6)return;
        float age=mc.level.getGameTime()-shownAt+e.getPartialTick().getGameTimeDeltaPartialTick(false);if(age<0||age>=SHOWN)return;
        float alpha=.85F*Mth.clamp((SHOWN-age)/FADE,0F,1F);
        var camera=e.getCamera().getPosition();var pose=e.getPoseStack();var buffers=mc.renderBuffers().bufferSource();
        var lines=buffers.getBuffer(RenderType.lines());
        pose.pushPose();pose.translate(-camera.x,-camera.y,-camera.z);var last=pose.last();
        for(int i=0;i+5<outline.length;i+=6){
            float x0=outline[i],y0=outline[i+1],z0=outline[i+2],x1=outline[i+3],y1=outline[i+4],z1=outline[i+5];
            float dx=x1-x0,dy=y1-y0,dz=z1-z0,len=Mth.sqrt(dx*dx+dy*dy+dz*dz);if(len<1e-4F)continue;dx/=len;dy/=len;dz/=len;
            lines.addVertex(last,x0,y0,z0).setColor(.95F,.93F,.86F,alpha).setNormal(last,dx,dy,dz);
            lines.addVertex(last,x1,y1,z1).setColor(.95F,.93F,.86F,alpha).setNormal(last,dx,dy,dz);
        }
        pose.popPose();
        // Through the haze: the lines are drawn with the fog held off, and it is put back after.
        float start=RenderSystem.getShaderFogStart();RenderSystem.setShaderFogStart(Float.MAX_VALUE);
        buffers.endBatch(RenderType.lines());
        RenderSystem.setShaderFogStart(start);
    }

    // ------------------------------------------------------------------------------------------------ June, quietly
    static List<SpeechCaptions.Voice> voices(){
        if(HUSHED.isEmpty())return List.of();var out=new ArrayList<SpeechCaptions.Voice>();
        for(var h:HUSHED)out.add(new SpeechCaptions.Voice(h.at(),PellPeople.name(PellPeople.JUNE),Component.literal(h.words()).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC)));
        return out;
    }

    // ------------------------------------------------------------------------------------------------ the bloom and the cold
    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Overlay {
        @SubscribeEvent public static void register(RegisterGuiLayersEvent event){
            event.registerBelowAll(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"pell_tilly_sight"),(g,delta)->{
                float k=strength();if(k<=0)return;var mc=Minecraft.getInstance();if(mc.player==null||mc.player.isUnderWater())return;
                int w=g.guiWidth(),h=g.guiHeight();
                RenderSystem.disableDepthTest();RenderSystem.depthMask(false);RenderSystem.enableBlend();
                // The bloom: the edges of the view going pale toward white, by how bright it is where they stand.
                float s=bloom*k*.55F;
                if(s>.01F){
                    RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE,GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR);
                    g.setColor(s,s,s,1F);g.blit(VIGNETTE,0,0,-90,0F,0F,w,h,w,h);g.setColor(1F,1F,1F,1F);
                    RenderSystem.defaultBlendFunc();
                }
                // The cold of the line: frost at the edges, gone in a second and a half.
                float a=cold<=0?0:Math.min(1F,cold/(float)COLD*1.4F)*k*.8F;
                if(a>.01F){g.setColor(1F,1F,1F,a);g.blit(FROST,0,0,-90,0F,0F,w,h,w,h);g.setColor(1F,1F,1F,1F);}
                RenderSystem.disableBlend();RenderSystem.depthMask(true);RenderSystem.enableDepthTest();
            });
        }
    }
}
