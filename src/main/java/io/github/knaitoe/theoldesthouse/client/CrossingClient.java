package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.CrossingPayload;
import java.io.IOException;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

/**
 * 0.4.351, The Last Crossing: the station's own hour on this client (morning, the day running to dusk), sent by the server.
 *
 * <p>0.4.354, Night Two: who of the crew is turned tonight and whose clothes a claw has torn. At the station a turned reader is drawn in
 * their own body gone drowned: the skin slick and dulled toward the river's murk, the eyes filmed over, gills cut either side of the
 * neck, so that whoever sees them still knows whose face it is. A torn reader has a rent in the cloth across the shoulder, a line of
 * blood in it, drawn over whatever they wear; it stays after dawn. Both are painted over the skin the reader is already drawn in (their
 * House body, or the account's default), never over a skin this client can't read; a turned reader whose skin can't be read wears the
 * Undertow's own skin.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class CrossingClient {
    private CrossingClient(){}
    private static long time=-1;
    private static final Set<UUID> TURNED=new HashSet<>(),TORN=new HashSet<>();
    private record Drawn(ResourceLocation base,boolean turned,boolean torn,@Nullable PlayerSkin skin){}
    private static final Map<UUID,Drawn> DRAWN=new HashMap<>();
    private static final ResourceLocation UNDERTOW=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/crossing_undertow.png");
    public static void accept(CrossingPayload payload){
        time=payload.time();
        TURNED.clear();TURNED.addAll(payload.turned());TORN.clear();TORN.addAll(payload.torn());
    }
    /** In the station's scene clock (mode 17), the hour its day has reached. */
    public static long time(Level level,long ordinary){return time>=0&&NovelSceneClient.clockMode(level)==17?time:ordinary;}
    public static boolean turned(UUID reader){return TURNED.contains(reader);}
    public static boolean torn(UUID reader){return TORN.contains(reader);}

    /** The skin to draw a reader in at the station tonight, painted over the one they're drawn in otherwise; null to leave it alone. */
    public static @Nullable PlayerSkin skin(UUID reader,@Nullable PlayerSkin under){
        boolean turned=TURNED.contains(reader),torn=TORN.contains(reader);if(!turned&&!torn)return null;
        var mc=Minecraft.getInstance();if(mc.level==null||NovelSceneClient.clockMode(mc.level)!=17)return null;
        var base=under==null?DefaultPlayerSkin.get(reader).texture():under.texture();
        var drawn=DRAWN.get(reader);
        if(drawn!=null&&drawn.base().equals(base)&&drawn.turned()==turned&&drawn.torn()==torn)return drawn.skin();
        PlayerSkin skin=null;
        try(var source=read(base)){
            NativeImage image=source;
            if(image==null&&turned)image=read(UNDERTOW);
            if(image!=null){
                var out=new NativeImage(image.getWidth(),image.getHeight(),true);out.copyFrom(image);
                if(image!=source)image.close();
                if(turned)drown(out);
                if(torn)tear(out);
                var at=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"crossing_skin/"+reader);
                mc.getTextureManager().release(at);mc.getTextureManager().register(at,new DynamicTexture(out));
                skin=new PlayerSkin(at,null,null,null,under==null?DefaultPlayerSkin.get(reader).model():under.model(),true);
            }
        }catch(IOException|RuntimeException e){TheOldestHouse.LOGGER.warn("The station could not draw {}",reader,e);}
        DRAWN.put(reader,new Drawn(base,turned,torn,skin));
        return skin;
    }
    /** A copy of a texture this client can read: one painted here, or one shipped in the resources; null for a downloaded skin. */
    private static @Nullable NativeImage read(ResourceLocation at)throws IOException{
        var mc=Minecraft.getInstance();
        if(mc.getTextureManager().getTexture(at,null) instanceof DynamicTexture dynamic&&dynamic.getPixels()!=null){
            var src=dynamic.getPixels();var copy=new NativeImage(src.getWidth(),src.getHeight(),true);copy.copyFrom(src);return copy;}
        var resource=mc.getResourceManager().getResource(at);if(resource.isEmpty())return null;
        try(var in=resource.get().open()){return NativeImage.read(in);}
    }

    // ------------------------------------------------------------------------------------------------ painting
    /** The body gone drowned: every pixel dulled and drawn toward the river's murk, the eyes filmed, gills either side of the neck. */
    static void drown(NativeImage img){
        for(int y=0;y<img.getHeight();y++)for(int x=0;x<img.getWidth();x++){
            int c=img.getPixelRGBA(x,y);int a=c>>>24;if(a==0)continue;
            int r=c&255,g=c>>8&255,b=c>>16&255;double lum=r*.3+g*.59+b*.11;
            int nr=(int)(r*.3+(lum*.5+26)*.7),ng=(int)(g*.3+(lum*.58+40)*.7),nb=(int)(b*.3+(lum*.55+36)*.7);
            img.setPixelRGBA(x,y,abgr(a,clamp(nr),clamp(ng),clamp(nb)));
        }
        int s=Math.max(1,img.getWidth()/64);
        // The eyes, filmed over: the usual eye cells on the face's front.
        for(int[] e:new int[][]{{9,12},{10,12},{13,12},{14,12}})block(img,s,e[0],e[1],abgr(255,196,212,188));
        // Gills: three cuts down each side of the head, low by the jaw.
        for(int[] g:new int[][]{{2,13},{3,14},{4,13},{5,14},{18,13},{19,14},{20,13},{21,14}})block(img,s,g[0],g[1],abgr(255,34,48,44));
    }
    /** A rent in the cloth across the right shoulder and chest, a line of blood in it: on the outer layer where the skin has one. */
    static void tear(NativeImage img){
        int s=Math.max(1,img.getWidth()/64);boolean layered=img.getHeight()>=img.getWidth();
        int body=layered?36:20,arm=layered?36:20,armX=layered?44:44;
        int rag=abgr(255,58,40,34),blood=abgr(255,128,18,16);
        for(int i=0;i<6;i++){block(img,s,21+i,body+i,blood);block(img,s,20+i,body+i,rag);block(img,s,22+i,body+i,rag);}
        for(int i=0;i<3;i++){block(img,s,armX+i,arm+i,blood);block(img,s,armX+1+i,arm+i,rag);}
    }
    private static void block(NativeImage img,int s,int x,int y,int abgr){for(int j=0;j<s;j++)for(int i=0;i<s;i++){int px=x*s+i,py=y*s+j;if(px<img.getWidth()&&py<img.getHeight())img.setPixelRGBA(px,py,abgr);}}
    private static int abgr(int a,int r,int g,int b){return a<<24|b<<16|g<<8|r;}
    private static int clamp(int v){return Math.max(0,Math.min(255,v));}

    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){
        time=-1;TURNED.clear();TORN.clear();
        for(var id:DRAWN.keySet())Minecraft.getInstance().getTextureManager().release(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"crossing_skin/"+id));
        DRAWN.clear();
    }
}
