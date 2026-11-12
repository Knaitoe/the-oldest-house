package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.BikeNightMistPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

/**
 * 0.4.491, Bike night: one reader's mist in the graveyard. The proposal: "It rises off the grass and thickens with each row. The
 * Lamp's headlight cuts it. Without it, the stones come up close and sudden." As answered: "The mist is each reader's own."
 *
 * <p>The server says how thick it is for this reader (BikeNightGraveyard.mist: by how many rows they are past, and less beside a lit
 * Lamp). Here it is how far they see: with none of it as far as the game draws, with all of it seven blocks, and between the two by
 * shares, not by blocks, so that each row takes about the same part of what is left. It comes and goes over a few seconds, never at
 * once. It is fog, and a few wisps off the ground: nothing flashes and nothing shakes.
 *
 * <p>0.4.495: but for somebody in the Hollow who has missed a jump and gone down into it (BikeNightHollow). Then it is over
 * everything at once, as near as {@link #OVER}, held for as long as the server says and gone again in a second. That is a sudden
 * change of the whole view, so it is as much of one as the comfort setting lets through, and none at all with that at nothing.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class BikeNightMistClient {
    /** How far somebody sees with no mist and with all of it, in blocks; the fog's near plane, as a share of that; how fast it comes and goes, a tick. */
    public static final float THIN=96,THICK=7,NEAR=.2F,EASE=.008F;
    /** With the mist over everything: how far somebody sees, in blocks, and how fast it goes again, a tick. */
    public static final float OVER=1.5F,OVER_EASE=.05F;
    private static int lease,hold;private static float target,mist,oMist,over,oOver;private static ClientLevel world;
    private BikeNightMistClient(){}
    public static void accept(BikeNightMistPayload p){var mc=Minecraft.getInstance();if(mc.level==null)return;world=mc.level;lease=40;target=Mth.clamp(p.mist()/100F,0,1);
        if(p.over()>0){hold=p.over();over=Math.max(over,(float)Comfort.effects());}
    }
    /** How much the mist is over everything, from nothing to one. */
    public static float over(float partial){return Mth.lerp(partial,oOver,over);}
    private static boolean active(){var mc=Minecraft.getInstance();return mc.level!=null&&mc.level==world&&mc.player!=null;}
    public static float level(float partial){return Mth.lerp(partial,oMist,mist);}
    /** How far somebody sees in this much mist. */
    public static float sight(float mist){return (float)(THIN*Math.pow(THICK/THIN,mist));}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        var mc=Minecraft.getInstance();
        if(lease>0)lease--;
        if(lease<=0||!active())target=0;
        if(!active()){oMist=mist=0;oOver=over=0;hold=0;return;}
        oMist=mist;mist+=Mth.clamp(target-mist,-EASE,EASE);
        oOver=over;if(hold>0)hold--;else over=Math.max(0,over-OVER_EASE);
        if(mist<=.02F||mc.isPaused())return;
        // "It rises off the grass": wisps low over the ground round whoever is in it, fewer for whoever has asked for fewer.
        var cam=mc.gameRenderer.getMainCamera().getPosition();var r=mc.level.random;
        var setting=mc.options.particles().get();float share=setting==ParticleStatus.MINIMAL?.25F:setting==ParticleStatus.DECREASED?.55F:1;
        int wisps=(int)(mist*3*share+r.nextFloat());
        for(int i=0;i<wisps;i++){
            double x=cam.x+(r.nextDouble()-.5)*18,z=cam.z+(r.nextDouble()-.5)*18;
            int top=mc.level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,Mth.floor(x),Mth.floor(z));
            if(Math.abs(top-cam.y)>4)continue;
            mc.level.addParticle(ParticleTypes.WHITE_SMOKE,x,top+.1+r.nextDouble()*.4,z,(r.nextDouble()-.5)*.01,.004+r.nextDouble()*.006,(r.nextDouble()-.5)*.01);
        }
    }
    @SubscribeEvent(priority=EventPriority.LOW) public static void fog(ViewportEvent.RenderFog e){
        float m=level((float)e.getPartialTick()),o=over((float)e.getPartialTick());if((m<=.01F&&o<=.01F)||!active()||e.getMode()!=FogRenderer.FogMode.FOG_TERRAIN)return;
        float far=Math.min(e.getFarPlaneDistance(),Mth.lerp(o,m<=.01F?e.getFarPlaneDistance():sight(m),OVER));
        e.setFarPlaneDistance(far);e.setNearPlaneDistance(Math.min(e.getNearPlaneDistance(),far*NEAR));e.setCanceled(true);
    }
    @SubscribeEvent public static void colour(ViewportEvent.ComputeFogColor e){
        float m=level((float)e.getPartialTick()),o=over((float)e.getPartialTick());if((m<=.01F&&o<=.01F)||!active())return;float s=Math.max(Math.min(1,m*1.4F),o);
        e.setRed(Mth.lerp(s,e.getRed(),.30F));e.setGreen(Mth.lerp(s,e.getGreen(),.33F));e.setBlue(Mth.lerp(s,e.getBlue(),.37F));
    }
}
