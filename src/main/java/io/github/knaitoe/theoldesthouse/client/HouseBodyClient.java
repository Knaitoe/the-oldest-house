package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.HouseBodies;
import io.github.knaitoe.theoldesthouse.network.HouseBodiesPayload;
import io.github.knaitoe.theoldesthouse.network.HouseBodyReportPayload;
import java.io.IOException;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.opengl.GL11;

/**
 * The House's body on the client (0.4.163). This client checks the face of its own reader's skin and tells the server only
 * whether it looks human. The bodies the server sends are painted sheets recoloured to each reader's tone and hair colour,
 * and stand in for that reader's skin wherever a player is drawn: the live model, first-person hands, mirrors, doubles and
 * the tab list. The sheets' lower half marks which pixels the painter took from the skin and the hair, around a grey of 128,
 * so the recolouring adds the painter's own shading to the new colours.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class HouseBodyClient {
    private HouseBodyClient(){}
    private record Worn(HouseBodies.Body body,PlayerSkin skin){}
    private static final Map<UUID,HouseBodies.Body> BODIES=new HashMap<>();
    private static final Map<UUID,Worn> WORN=new HashMap<>();
    private static final Map<UUID,int[]> DEFAULTS=new HashMap<>();
    /** Set while this client reads its reader's own skin, so the body never stands in for it there. */
    public static boolean reading;
    private static @Nullable ResourceLocation checked;private static int settle;

    public static void accept(HouseBodiesPayload payload){
        var next=new HashMap<UUID,HouseBodies.Body>();for(var e:payload.bodies())next.put(e.reader(),e.body());
        WORN.entrySet().removeIf(e->{if(e.getValue().body().equals(next.get(e.getKey())))return false;release(e.getKey());return true;});
        BODIES.clear();BODIES.putAll(next);
    }
    public static boolean wearing(UUID reader){return BODIES.containsKey(reader);}
    /** The skin to draw for a reader: their House body while they wear one, otherwise null for their own. */
    public static @Nullable PlayerSkin skin(UUID reader){
        var body=BODIES.get(reader);if(body==null||!RenderSystem.isOnRenderThread())return null;
        var worn=WORN.get(reader);if(worn!=null&&worn.body().equals(body))return worn.skin();
        var skin=compose(reader,body);if(skin!=null)WORN.put(reader,new Worn(body,skin));return skin;
    }
    private static ResourceLocation location(UUID reader){return ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"house_body/"+reader);}
    private static void release(UUID reader){Minecraft.getInstance().getTextureManager().release(location(reader));}
    private static @Nullable PlayerSkin compose(UUID reader,HouseBodies.Body body){
        var mc=Minecraft.getInstance();
        try(var sheet=sheet(body)){
            int tone=body.tone(),hair=body.hair();if(tone<0||hair<0){var own=defaults(reader);if(tone<0)tone=own[0];if(hair<0)hair=own[1];}
            mc.getTextureManager().register(location(reader),new DynamicTexture(recolour(sheet,tone,hair)));
            return new PlayerSkin(location(reader),null,null,null,PlayerSkin.Model.WIDE,true);
        }catch(IOException|RuntimeException e){TheOldestHouse.LOGGER.warn("The House's body {} could not be drawn",body.sheet(),e);return null;}
    }
    public static NativeImage sheet(HouseBodies.Body body)throws IOException{
        try(var in=Minecraft.getInstance().getResourceManager().open(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/house_body/"+body.sheet()+".png"))){return NativeImage.read(in);}
    }
    /** A painted sheet in the given tone and hair colour (RGB): each marked pixel is the new colour plus the painter's shading. */
    public static NativeImage recolour(NativeImage sheet,int tone,int hair){
        int w=sheet.getWidth(),h=sheet.getHeight()/2;var out=new NativeImage(w,h,true);
        for(int y=0;y<h;y++)for(int x=0;x<w;x++){
            int c=sheet.getPixelRGBA(x,y),m=sheet.getPixelRGBA(x,y+h);
            if(m>>>24==0){out.setPixelRGBA(x,y,c);continue;}
            int ref=(m&255)>0?tone:hair;
            int r=clamp((ref>>16&255)+(c&255)-128),g=clamp((ref>>8&255)+(c>>8&255)-128),b=clamp((ref&255)+(c>>16&255)-128);
            out.setPixelRGBA(x,y,(c&0xff000000)|b<<16|g<<8|r);
        }
        return out;
    }
    private static int clamp(int v){return Math.max(0,Math.min(255,v));}
    /**
     * The tone and hair colour of the default skin Minecraft gives this account: the commonest skin colour on the lower face
     * (below the eyes, where no hair falls), and the commonest colour on the crown.
     */
    public static int[] defaults(UUID reader){
        return DEFAULTS.computeIfAbsent(reader,id->{
            try(var in=Minecraft.getInstance().getResourceManager().open(DefaultPlayerSkin.get(id).texture());var image=NativeImage.read(in)){
                var skin=new HashMap<Integer,Integer>();var crown=new HashMap<Integer,Integer>();
                for(int y=12;y<16;y++)for(int x=9;x<15;x++){int c=argb(image.getPixelRGBA(x,y));if(c>>>24>=128&&HouseBodies.skinColour(c>>16&255,c>>8&255,c&255))skin.merge(c&0xffffff,1,Integer::sum);}
                for(int y=0;y<8;y++)for(int x=8;x<16;x++){int c=argb(image.getPixelRGBA(x,y));if(c>>>24>=128)crown.merge(c&0xffffff,1,Integer::sum);}
                return new int[]{commonest(skin,0xc9966f),commonest(crown,0x4a3326)};
            }catch(IOException|RuntimeException e){return new int[]{0xc9966f,0x4a3326};}
        });
    }
    private static int argb(int abgr){return (abgr&0xff00ff00)|(abgr&255)<<16|(abgr>>16&255);}
    private static int commonest(Map<Integer,Integer> counts,int fallback){
        return counts.entrySet().stream().max(Map.Entry.<Integer,Integer>comparingByValue().thenComparing(Map.Entry.comparingByKey())).map(Map.Entry::getKey).orElse(fallback);
    }

    // ---- The check of this reader's own face.
    @SubscribeEvent public static void login(ClientPlayerNetworkEvent.LoggingIn e){checked=null;settle=0;}
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){BODIES.clear();WORN.keySet().forEach(HouseBodyClient::release);WORN.clear();checked=null;}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        var mc=Minecraft.getInstance();if(mc.player==null||mc.getConnection()==null)return;var info=mc.getConnection().getPlayerInfo(mc.player.getUUID());if(info==null)return;
        PlayerSkin own;reading=true;try{own=info.getSkin();}finally{reading=false;}
        if(own.texture().equals(checked)){settle=0;return;}if(++settle<20)return;
        Boolean human=forced();if(human==null)human=human(mc,own.texture());if(human==null)return;
        checked=own.texture();settle=0;PacketDistributor.sendToServer(new HouseBodyReportPayload(human));
    }
    /** For the connected-client proofs only: a reader told to wear an inhuman skin. */
    private static @Nullable Boolean forced(){return Boolean.getBoolean("the_oldest_house.inhumanSkin")?false:null;}
    /** Whether the face of this skin looks human, or null while it is still loading. */
    private static @Nullable Boolean human(Minecraft mc,ResourceLocation texture){
        var tex=mc.getTextureManager().getTexture(texture);RenderSystem.bindTexture(tex.getId());
        int w=GlStateManager._getTexLevelParameter(GL11.GL_TEXTURE_2D,0,GL11.GL_TEXTURE_WIDTH),h=GlStateManager._getTexLevelParameter(GL11.GL_TEXTURE_2D,0,GL11.GL_TEXTURE_HEIGHT);
        if(w<64||h<64||w!=h)return null;
        try(var image=new NativeImage(w,h,false)){
            image.downloadTexture(0,false);int scale=w/64;boolean[] seen={false};
            boolean human=HouseBodies.humanFace((x,y)->{int c=argb(image.getPixelRGBA(x*scale,y*scale));if(c>>>24>=128)seen[0]=true;return c;});
            return seen[0]?human:null;
        }
    }
}
