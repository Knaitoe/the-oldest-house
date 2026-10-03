package io.github.knaitoe.theoldesthouse.client;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.network.HotelAtmospherePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
/** Darkness is spatial and fully opaque; only this passage's custom arrow can lie. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class HotelAtmosphere {
    private static int lease,flash;private static HotelAtmospherePayload cue;
    private HotelAtmosphere(){}
    public static void accept(HotelAtmospherePayload p){cue=p;lease=40;flash=Math.max(flash,Math.min(8,p.flash()));}
    private static LabyrinthPlace scene(){var mc=Minecraft.getInstance();var origin=HouseSightlineState.origin();if(mc.player==null||mc.level==null||mc.player.isSpectator()||origin==null)return null;var p=LabyrinthPlaces.placeAt(origin,mc.player.blockPosition());return p!=null&&mc.level.dimension().equals(NovelRooms.dimension(p))?p:null;}
    public static boolean blind(){var mc=Minecraft.getInstance();return scene()==LabyrinthPlace.BLIND_STRETCH&&mc.player.getZ()<LabyrinthPlaces.base(HouseSightlineState.origin(),LabyrinthPlace.BLIND_STRETCH).getZ()-4;}
    public static String arrow(Vec3 listener,Vec3 source,float yaw,boolean lie){var forward=Vec3.directionFromRotation(0,yaw);var delta=source.subtract(listener).multiply(1,0,1).normalize();double ahead=forward.dot(delta),side=forward.cross(delta).y;String honest=Math.abs(ahead)>.65?(ahead>0?"↑":"↓"):(side>0?"←":"→");if(!lie)return honest;return switch(honest){case "↑"->"↓";case "↓"->"↑";case "←"->"→";default->"←";};}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){if(lease>0)lease--;if(flash>0)flash--;if(scene()==null){lease=0;flash=0;cue=null;}}
    @SubscribeEvent public static void fog(ViewportEvent.ComputeFogColor e){if(blind()){e.setRed(0);e.setGreen(0);e.setBlue(0);return;}if(scene()!=LabyrinthPlace.HOTEL_GROUNDS)return;e.setRed(.66F);e.setGreen(.7F);e.setBlue(.75F);}
    @SubscribeEvent public static void blizzard(ViewportEvent.RenderFog e){if(blind()){e.setNearPlaneDistance(0);e.setFarPlaneDistance(.01F);e.setCanceled(true);return;}if(scene()!=LabyrinthPlace.HOTEL_GROUNDS||e.getMode()!=net.minecraft.client.renderer.FogRenderer.FogMode.FOG_TERRAIN)return;e.setNearPlaneDistance(.8F);e.setFarPlaneDistance(6);e.setCanceled(true);}
    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Layers {
        @SubscribeEvent public static void register(RegisterGuiLayersEvent e){e.registerBelowAll(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"blind_stretch"),(g,delta)->{if(blind())g.fill(0,0,g.guiWidth(),g.guiHeight(),0xFF000000);});
            e.registerAboveAll(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"hotel_weather"),(g,delta)->{var mc=Minecraft.getInstance();var scene=scene();
                if(blind()&&lease>0&&cue!=null&&cue.mode()==1){String text=arrow(mc.player.position(),cue.source().getCenter(),mc.player.getYRot(),cue.lie())+"  A bell in the dark";int x=g.guiWidth()-mc.font.width(text)-16,y=g.guiHeight()-78;g.fill(x-5,y-3,g.guiWidth()-10,y+13,0xAA252525);g.drawString(mc.font,text,x,y,0xFFF1ECE1);}
                if(scene==LabyrinthPlace.HOTEL_GROUNDS){int time=mc.player.tickCount;for(int i=0;i<85;i++){int x=Math.floorMod(i*79+time*2,g.guiWidth()),y=Math.floorMod(i*43+time*(2+i%3),g.guiHeight());g.fill(x,y,x+1+i%2,y+2,0xAADCDFE2);}}
                if(scene==LabyrinthPlace.HOTEL&&flash>0){int alpha=(int)(170*flash/8F*(double)mc.options.screenEffectScale().get());g.fill(0,0,g.guiWidth(),g.guiHeight(),(alpha<<24)|0xFFFFFF);}
            });}
    }
}
