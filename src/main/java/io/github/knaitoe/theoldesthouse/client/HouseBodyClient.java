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
    private record Worn(HouseBodies.Body body,PlayerSkin skin,@Nullable ResourceLocation source){}
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
    /** Whether a reader wears a painted House body (their own skin in a drawn tone is not a body). */
    public static boolean wearing(UUID reader){var body=BODIES.get(reader);return body!=null&&!body.own();}
    /** The skin to draw for a reader: their House body, or their own skin in the tone they drew, otherwise null for their own. */
    public static @Nullable PlayerSkin skin(UUID reader){
        var body=BODIES.get(reader);if(body==null||!RenderSystem.isOnRenderThread())return null;
        var worn=WORN.get(reader);
        if(body.own()){
            var real=real(reader);if(real==null)return null;
            if(worn!=null&&worn.body().equals(body)&&real.texture().equals(worn.source()))return worn.skin();
            var skin=toned(reader,real,body);if(skin!=null)WORN.put(reader,new Worn(body,skin,real.texture()));return skin;
        }
        if(worn!=null&&worn.body().equals(body))return worn.skin();
        var skin=compose(reader,body);if(skin!=null)WORN.put(reader,new Worn(body,skin,null));return skin;
    }
    /** The reader's own skin, read past the House's stand-in. */
    private static @Nullable PlayerSkin real(UUID reader){
        var connection=Minecraft.getInstance().getConnection();var info=connection==null?null:connection.getPlayerInfo(reader);if(info==null)return null;
        reading=true;try{return info.getSkin();}finally{reading=false;}
    }
    /**
     * The reader's own skin as they drew themselves at the kitchen table, or null while the skin is still loading: its skin
     * recoloured to the drawn tone, (0.4.196) slim arms for a dress or wide arms for trousers, and the face they drew.
     */
    private static @Nullable PlayerSkin toned(UUID reader,PlayerSkin real,HouseBodies.Body body){
        var mc=Minecraft.getInstance();var tex=mc.getTextureManager().getTexture(real.texture());RenderSystem.bindTexture(tex.getId());
        int w=GlStateManager._getTexLevelParameter(GL11.GL_TEXTURE_2D,0,GL11.GL_TEXTURE_WIDTH),h=GlStateManager._getTexLevelParameter(GL11.GL_TEXTURE_2D,0,GL11.GL_TEXTURE_HEIGHT);
        if(w<64||h<64||w!=h)return null;
        var model=switch(body.presentation()){case 1->PlayerSkin.Model.WIDE;case 2->PlayerSkin.Model.SLIM;default->real.model();};
        try(var image=new NativeImage(w,h,false)){
            image.downloadTexture(0,false);
            mc.getTextureManager().register(location(reader),new DynamicTexture(drawn(image,body.tone(),real.model(),model,body.face())));
            return new PlayerSkin(location(reader),null,real.capeTexture(),real.elytraTexture(),model,true);
        }catch(RuntimeException e){TheOldestHouse.LOGGER.warn("A reader's own skin could not be toned",e);return null;}
    }
    /** A skin as drawn: toned (a tone of -1 keeps its own), its arms refitted from one model's width to the other's, and a face. */
    public static NativeImage drawn(NativeImage skin,int tone,PlayerSkin.Model from,PlayerSkin.Model to,int face){
        NativeImage out;if(tone>=0)out=tonedImage(skin,tone);else{out=new NativeImage(skin.getWidth(),skin.getHeight(),true);out.copyFrom(skin);}
        if(from!=to){var refitted=arms(out,from==PlayerSkin.Model.SLIM?3:4,to==PlayerSkin.Model.SLIM?3:4);out.close();out=refitted;}
        if(face>0)face(out,skin,tone,face);
        return out;
    }
    /**
     * A skin with its skin recoloured: the skin's own tone is the commonest skin colour on the lower face; every pixel near it
     * in colour becomes the drawn tone plus that pixel's own difference from it, so the painter's shading stays.
     */
    public static NativeImage tonedImage(NativeImage skin,int tone){
        int w=skin.getWidth(),h=skin.getHeight();var out=new NativeImage(w,h,true);out.copyFrom(skin);
        int base=base(skin);if(base<0)return out;
        int br=base>>16&255,bg=base>>8&255,bb=base&255,tr=tone>>16&255,tg=tone>>8&255,tb=tone&255;
        for(int y=0;y<h;y++)for(int x=0;x<w;x++){
            int abgr=skin.getPixelRGBA(x,y),c=argb(abgr);if(c>>>24<128)continue;
            int r=c>>16&255,g=c>>8&255,b=c&255;
            if(!HouseBodies.skinColour(r,g,b)||Math.abs(r-br)+Math.abs(g-bg)+Math.abs(b-bb)>110)continue;
            out.setPixelRGBA(x,y,(abgr&0xff000000)|clamp(tb+b-bb)<<16|clamp(tg+g-bg)<<8|clamp(tr+r-br));
        }
        return out;
    }
    /** A skin's own tone: the commonest skin colour on the lower face, below the eyes where no hair falls, or -1 for none. */
    static int base(NativeImage skin){
        int scale=Math.max(1,skin.getWidth()/64);var counts=new HashMap<Integer,Integer>();
        for(int y=12*scale;y<16*scale;y++)for(int x=9*scale;x<15*scale;x++){int c=argb(skin.getPixelRGBA(x,y));if(c>>>24>=128&&HouseBodies.skinColour(c>>16&255,c>>8&255,c&255))counts.merge(c&0xffffff,1,Integer::sum);}
        return counts.isEmpty()?-1:commonest(counts,0);
    }
    /**
     * 0.4.196: the arms refitted from one width to the other (4 wide, 3 slim). Each arm and sleeve keeps its six faces; the
     * faces that run across the arm keep their outer columns, losing or doubling one in the middle.
     */
    public static NativeImage arms(NativeImage src,int from,int to){
        int scale=Math.max(1,src.getWidth()/64);var out=new NativeImage(src.getWidth(),src.getHeight(),true);out.copyFrom(src);
        for(int[] o:new int[][]{{40,16},{40,32},{32,48},{48,48}}){
            int u=o[0],v=o[1];
            for(int y=0;y<16*scale;y++)for(int x=0;x<16*scale;x++)out.setPixelRGBA(u*scale+x,v*scale+y,0);
            across(src,out,scale,u+4,v,from,u+4,v,to,4);            // top
            across(src,out,scale,u+4+from,v,from,u+4+to,v,to,4);    // bottom
            across(src,out,scale,u,v+4,4,u,v+4,4,12);               // outer side
            across(src,out,scale,u+4,v+4,from,u+4,v+4,to,12);       // front
            across(src,out,scale,u+4+from,v+4,4,u+4+to,v+4,4,12);   // inner side
            across(src,out,scale,u+8+from,v+4,from,u+8+to,v+4,to,12); // back
        }
        return out;
    }
    private static void across(NativeImage src,NativeImage out,int scale,int sx,int sy,int sw,int dx,int dy,int dw,int h){
        for(int y=0;y<h*scale;y++)for(int x=0;x<dw*scale;x++){
            int col=x/scale,from=dw==sw||dw==1?col:Math.round(col*(sw-1)/(float)(dw-1));
            out.setPixelRGBA(dx*scale+x,dy*scale+y,src.getPixelRGBA((sx+from)*scale+x%scale,sy*scale+y));
        }
    }
    /** Each drawn face's marks on a skin's own face, as column and row of its eight by eight front; the sad face's tear apart. */
    private static final int[][][] FACES={{{2,6},{5,6},{3,7},{4,7}},{{3,6},{4,6},{2,7},{5,7}},{{3,6},{4,6},{3,7},{4,7}},{{1,3},{2,3},{5,3},{6,3},{2,6},{3,6},{4,6},{5,6}},{{3,6},{4,6}}};
    private static final int[] TEAR={2,5};
    /**
     * 0.4.196: the face the reader drew, on their own face: the lower face cleared to their skin and the drawn mouth put on it
     * in the colour of their own, with brows for a cross face and a tear for a sad one. A face that is mostly not skin below the
     * eyes (a beard, a mask) or has anything worn over its mouth keeps its own.
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
        var mc=Minecraft.getInstance();
        try(var sheet=sheet(body)){
            int tone=body.tone(),hair=body.hair();if(tone<0||hair<0){var own=defaults(reader);if(tone<0)tone=own[0];if(hair<0)hair=own[1];}
            var image=recolour(sheet,tone,hair);
            // 0.4.196: the face drawn at the kitchen table.
            if(body.face()>0){try(var painted=new NativeImage(image.getWidth(),image.getHeight(),true)){painted.copyFrom(image);face(image,painted,tone,body.face());}}
            mc.getTextureManager().register(location(reader),new DynamicTexture(image));
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
