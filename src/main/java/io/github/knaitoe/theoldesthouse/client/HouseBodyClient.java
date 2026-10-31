package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.HouseBodies;
import io.github.knaitoe.theoldesthouse.network.HouseBodiesPayload;
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

/**
 * The House's body on the client (0.4.163). The bodies the server sends are painted sheets recoloured to each reader's tone
 * and hair colour, and stand in for that reader's skin wherever a player is drawn: the live model, first-person hands,
 * mirrors, doubles and the tab list. The sheets' lower half marks which pixels the painter took from the skin and the hair,
 * around a grey of 128, so the recolouring adds the painter's own shading to the new colours. 0.4.201: every reader inside
 * the House wears one; no reader's own skin is read, checked or recoloured.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class HouseBodyClient {
    private HouseBodyClient(){}
    private record Worn(HouseBodies.Body body,PlayerSkin skin){}
    private static final Map<UUID,HouseBodies.Body> BODIES=new HashMap<>();
    private static final Map<UUID,Worn> WORN=new HashMap<>();
    private static final Map<UUID,int[]> DEFAULTS=new HashMap<>();

    public static void accept(HouseBodiesPayload payload){
        var next=new HashMap<UUID,HouseBodies.Body>();for(var e:payload.bodies())next.put(e.reader(),e.body());
        WORN.entrySet().removeIf(e->{if(e.getValue().body().equals(next.get(e.getKey())))return false;release(e.getKey());return true;});
        BODIES.clear();BODIES.putAll(next);
    }
    /** Whether a reader wears the House's body. */
    public static boolean wearing(UUID reader){return BODIES.containsKey(reader);}
    /** The skin to draw for a reader: their House body, or null for their own. */
    public static @Nullable PlayerSkin skin(UUID reader){
        var body=BODIES.get(reader);if(body==null||!RenderSystem.isOnRenderThread())return null;
        var worn=WORN.get(reader);if(worn!=null&&worn.body().equals(body))return worn.skin();
        var skin=compose(reader,body);if(skin!=null)WORN.put(reader,new Worn(body,skin));return skin;
    }
    /** A skin's own tone: the commonest skin colour on the lower face, below the eyes where no hair falls, or -1 for none. */
    static int base(NativeImage skin){
        int scale=Math.max(1,skin.getWidth()/64);var counts=new HashMap<Integer,Integer>();
        for(int y=12*scale;y<16*scale;y++)for(int x=9*scale;x<15*scale;x++){int c=argb(skin.getPixelRGBA(x,y));if(c>>>24>=128&&HouseBodies.skinColour(c>>16&255,c>>8&255,c&255))counts.merge(c&0xffffff,1,Integer::sum);}
        return counts.isEmpty()?-1:commonest(counts,0);
    }
    /** Each drawn face's marks on a skin's own face, as column and row of its eight by eight front; the sad face's tear apart. */
    private static final int[][][] FACES={{{2,6},{5,6},{3,7},{4,7}},{{3,6},{4,6},{2,7},{5,7}},{{3,6},{4,6},{3,7},{4,7}},{{1,3},{2,3},{5,3},{6,3},{2,6},{3,6},{4,6},{5,6}},{{3,6},{4,6}}};
    private static final int[] TEAR={2,5};
    /**
     * 0.4.196: the face the reader drew, on the body's face: the lower face cleared to its skin and the drawn mouth put on it in
     * the colour of its own, with brows for a cross face and a tear for a sad one. A face that is mostly not skin below the eyes
     * or has anything worn over its mouth keeps its own.
     */
    public static void face(NativeImage out,NativeImage original,int tone,int mood){
        if(mood<1||mood>FACES.length)return;int scale=Math.max(1,original.getWidth()/64),base=base(original);if(base<0)return;
        int br=base>>16&255,bg=base>>8&255,bb=base&255,skin=tone>=0?tone:base,kept=0,ink=-1,inkSum=999;
        for(int j=5;j<8;j++)for(int i=1;i<7;i++){
            if(argb(original.getPixelRGBA((40+i)*scale,(8+j)*scale))>>>24>=128)return;
            int c=argb(original.getPixelRGBA((8+i)*scale,(8+j)*scale)),r=c>>16&255,g=c>>8&255,b=c&255;
            if(c>>>24>=128&&HouseBodies.skinColour(r,g,b)&&Math.abs(r-br)+Math.abs(g-bg)+Math.abs(b-bb)<=110)kept++;
            else if(c>>>24>=128&&r+g+b<inkSum){inkSum=r+g+b;ink=c&0xffffff;}
        }
        if(kept<9)return;
        if(ink<0)ink=(int)((skin>>16&255)*.45f)<<16|(int)((skin>>8&255)*.45f)<<8|(int)((skin&255)*.45f);
        for(int j=5;j<8;j++)for(int i=1;i<7;i++){int c=argb(original.getPixelRGBA((8+i)*scale,(8+j)*scale));if(Math.abs((c>>16&255)-br)+Math.abs((c>>8&255)-bg)+Math.abs((c&255)-bb)>40)paint(out,scale,i,j,skin);}
        for(int[] m:FACES[mood-1])paint(out,scale,m[0],m[1],ink);
        if(mood==2)paint(out,scale,TEAR[0],TEAR[1],0x7fb2e5);
    }
    private static void paint(NativeImage out,int scale,int i,int j,int rgb){
        int abgr=0xff000000|(rgb&255)<<16|(rgb>>8&255)<<8|(rgb>>16&255);
        for(int y=0;y<scale;y++)for(int x=0;x<scale;x++)out.setPixelRGBA((8+i)*scale+x,(8+j)*scale+y,abgr);
    }
    private static ResourceLocation location(UUID reader){return ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"house_body/"+reader);}
    private static void release(UUID reader){Minecraft.getInstance().getTextureManager().release(location(reader));}
    private static @Nullable PlayerSkin compose(UUID reader,HouseBodies.Body body){
        try{
            Minecraft.getInstance().getTextureManager().register(location(reader),new DynamicTexture(painted(body,defaults(reader))));
            return new PlayerSkin(location(reader),null,null,null,PlayerSkin.Model.WIDE,true);
        }catch(IOException|RuntimeException e){TheOldestHouse.LOGGER.warn("The House's body {} could not be drawn",body.sheet(),e);return null;}
    }
    /**
     * A body as it is worn: its sheet in the drawn tone and hair colour (or, where none was drawn, those of the account's
     * default skin, {@code defaults}), with (0.4.196) the face drawn at the kitchen table.
     */
    public static NativeImage painted(HouseBodies.Body body,int[] defaults)throws IOException{
        try(var sheet=sheet(body)){
            int tone=body.tone()<0?defaults[0]:body.tone(),hair=body.hair()<0?defaults[1]:body.hair();
            var image=recolour(sheet,tone,hair);
            if(body.face()>0){try(var painted=new NativeImage(image.getWidth(),image.getHeight(),true)){painted.copyFrom(image);face(image,painted,tone,body.face());}}
            return image;
        }
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

    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){BODIES.clear();WORN.keySet().forEach(HouseBodyClient::release);WORN.clear();}
}
